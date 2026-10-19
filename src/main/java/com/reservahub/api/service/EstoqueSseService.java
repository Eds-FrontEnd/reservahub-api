
package com.reservahub.api.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class EstoqueSseService {

    private final List<SseEmitter> conexoes = new CopyOnWriteArrayList<>();

    public SseEmitter conectar() {
        SseEmitter emitter = new SseEmitter(0L);

        conexoes.add(emitter);

        emitter.onCompletion(() -> conexoes.remove(emitter));

        emitter.onTimeout(() -> {
            conexoes.remove(emitter);
            emitter.complete();
        });

        emitter.onError(erro -> conexoes.remove(emitter));

        return emitter;
    }

    public void notificarEstoqueAtualizado(List<String> idsProdutos) {
        if (idsProdutos == null || idsProdutos.isEmpty()) {
            return;
        }

        List<String> idsUnicos = idsProdutos.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();

        if (idsUnicos.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : conexoes) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("estoque-atualizado")
                                .data(idsUnicos)
                );
            } catch (IOException | IllegalStateException erro) {
                conexoes.remove(emitter);
                emitter.complete();
            }
        }
    }

    public void notificarCupomAtualizado() {
        for (SseEmitter emitter : conexoes) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("cupom-atualizado")
                                .data("atualizado")
                );
            } catch (IOException | IllegalStateException erro) {
                conexoes.remove(emitter);
                emitter.complete();
            }
        }
    }
}
