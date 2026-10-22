package com.reservahub.api.config;

import com.reservahub.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        String detalhes = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .filter(mensagem -> mensagem != null && !mensagem.isBlank())
                .distinct()
                .collect(Collectors.joining(" "));

        String mensagem = detalhes.isBlank()
                ? "Confira os dados preenchidos e corrija os campos inválidos."
                : "Revise os dados informados: " + detalhes;

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "Não foi possível interpretar os dados enviados. Confira os campos preenchidos e tente novamente."
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        String caminho = request.getRequestURI();

        if ("/api/auth/solicitar-reset-senha".equals(caminho)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Não foi possível atender sua solicitação de redefinição de senha com os dados informados."
                    ));
        }

        String mensagem = exception.getMessage();

        if (mensagem == null || mensagem.isBlank()) {
            mensagem = "Confira os dados informados e tente novamente.";
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(mensagem));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException exception
    ) {
        String mensagem = exception.getMessage();

        if (mensagem == null || mensagem.isBlank()) {
            mensagem = "Não foi possível concluir a operação no estado atual. Confira o status da solicitação e tente novamente.";
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(mensagem));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        logger.warn(
                "Conflito de integridade de dados na requisição {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "Não foi possível concluir a operação porque os dados entram em conflito com um registro existente. Confira as informações e tente novamente."
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception exception,
            HttpServletRequest request
    ) {
        String caminho = request.getRequestURI();

        logger.error(
                "Erro inesperado na requisição {} {}",
                request.getMethod(),
                caminho,
                exception
        );

        String mensagem;

        if ("/api/contato".equals(caminho)) {
            mensagem =
                    "Não foi possível enviar sua mensagem neste momento. Confira os campos preenchidos e tente novamente. Se o problema persistir, entre em contato com o suporte.";
        } else if ("/api/auth/solicitar-reset-senha".equals(caminho)) {
            mensagem =
                    "Não foi possível processar sua solicitação de recuperação de senha neste momento. Confira o e-mail informado e tente novamente mais tarde.";
        } else if ("/api/auth/redefinir-senha".equals(caminho)) {
            mensagem =
                    "Não foi possível redefinir sua senha. Verifique se sua solicitação foi aprovada e continua válida. Se o problema persistir, entre em contato com o suporte.";
        } else if ("/api/compras/validar-cupom".equals(caminho)) {
            mensagem =
                    "Não foi possível validar seu cupom de desconto neste momento. Tente novamente. Se o problema persistir, entre em contato com o suporte.";
        } else if (caminho.startsWith("/api/compras")) {
            mensagem =
                    "Não foi possível concluir sua compra. A disponibilidade de um ou mais smartphones pode ter mudado enquanto você finalizava o pedido. Confira os itens e as quantidades no carrinho e tente novamente. Se o problema persistir, entre em contato com o suporte.";
        } else {
            mensagem =
                    "Ocorreu um erro inesperado ao processar sua solicitação. Tente novamente mais tarde. Se o problema persistir, entre em contato com o suporte.";
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(mensagem));
    }
}
