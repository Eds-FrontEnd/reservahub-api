
package com.reservahub.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatusCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.Locale;

@Service
public class ProdutoImagemService {

    private static final long TAMANHO_MAXIMO = 5L * 1024 * 1024;

    private final Path diretorio;

    public ProdutoImagemService(
            @Value("${app.upload.dir:uploads}") String diretorioUpload
    ) {
        this.diretorio = Path.of(diretorioUpload)
                .toAbsolutePath()
                .normalize()
                .resolve("products")
                .normalize();
    }

    public String salvar(MultipartFile arquivo, String modeloProduto) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecione uma imagem."
            );
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new ResponseStatusException(
                    HttpStatusCode.valueOf(413),
                    "A imagem deve ter no máximo 5 MB."
            );
        }

        if (modeloProduto == null || modeloProduto.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o modelo do produto."
            );
        }

        String nomeOriginal = arquivo.getOriginalFilename();

        if (nomeOriginal == null || nomeOriginal.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome da imagem é inválido."
            );
        }

        String extensao = obterExtensao(nomeOriginal);
        validarExtensaoEMime(extensao, arquivo.getContentType());

        String nomeSeguro = gerarNomeArquivo(modeloProduto, extensao);

        try {
            byte[] conteudo = arquivo.getBytes();

            validarAssinatura(conteudo, extensao);

            Files.createDirectories(diretorio);

            Path destino = diretorio.resolve(nomeSeguro).normalize();

            if (!destino.startsWith(diretorio)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O caminho da imagem é inválido."
                );
            }

            Files.write(
                    destino,
                    conteudo,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            return "/uploads/products/" + nomeSeguro;
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível armazenar a imagem."
            );
        }
    }

    private String gerarNomeArquivo(
            String modeloProduto,
            String extensao
    ) {
        String normalizado = Normalizer.normalize(
                        modeloProduto.trim(),
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        String slug = normalizado
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        if (slug.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O modelo do produto não gera um nome de imagem válido."
            );
        }

        return slug + "." + extensao;
    }

    private String obterExtensao(String nomeOriginal) {
        int ponto = nomeOriginal.lastIndexOf('.');

        if (ponto < 0 || ponto == nomeOriginal.length() - 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A imagem precisa ter uma extensão válida."
            );
        }

        return nomeOriginal.substring(ponto + 1)
                .toLowerCase(Locale.ROOT);
    }

    private void validarExtensaoEMime(
            String extensao,
            String tipoConteudo
    ) {
        boolean tipoValido = switch (extensao) {
            case "png" -> "image/png".equals(tipoConteudo);
            case "jpg", "jpeg" -> "image/jpeg".equals(tipoConteudo);
            case "webp" -> "image/webp".equals(tipoConteudo);
            default -> false;
        };

        if (!tipoValido) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato não permitido. Utilize PNG, JPEG ou WebP."
            );
        }
    }

    private void validarAssinatura(byte[] bytes, String extensao) {
        boolean assinaturaValida = switch (extensao) {
            case "png" -> bytes.length >= 8
                    && (bytes[0] & 0xFF) == 0x89
                    && bytes[1] == 0x50
                    && bytes[2] == 0x4E
                    && bytes[3] == 0x47
                    && bytes[4] == 0x0D
                    && bytes[5] == 0x0A
                    && bytes[6] == 0x1A
                    && bytes[7] == 0x0A;

            case "jpg", "jpeg" -> bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xFF
                    && (bytes[1] & 0xFF) == 0xD8
                    && (bytes[2] & 0xFF) == 0xFF;

            case "webp" -> bytes.length >= 12
                    && bytes[0] == 'R'
                    && bytes[1] == 'I'
                    && bytes[2] == 'F'
                    && bytes[3] == 'F'
                    && bytes[8] == 'W'
                    && bytes[9] == 'E'
                    && bytes[10] == 'B'
                    && bytes[11] == 'P';

            default -> false;
        };

        if (!assinaturaValida) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O conteúdo do arquivo não corresponde a uma imagem válida."
            );
        }
    }
}
