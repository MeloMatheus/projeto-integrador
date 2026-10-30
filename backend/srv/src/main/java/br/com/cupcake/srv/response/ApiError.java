package br.com.cupcake.srv.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(LocalDateTime timestamp, int status, String erro, Map<String, String> campos) {
    public ApiError(LocalDateTime timestamp, int status, String erro) {
        this(timestamp, status, erro, Map.of());
    }
}
