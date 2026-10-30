package br.com.cupcake.srv.controller;

import br.com.cupcake.srv.request.pagamento.ConfirmarPagamentoRequest;
import br.com.cupcake.srv.request.pagamento.CriarPagamentoRequest;
import br.com.cupcake.srv.response.pagamento.PagamentoResponse;
import br.com.cupcake.srv.service.PagamentoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@SecurityRequirement(name = "bearerAuth")
public class PagamentoController {
    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping("/api/pedidos/{pedidoId}/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLIENTE')")
    public PagamentoResponse criarTentativa(
            Authentication authentication,
            @PathVariable UUID pedidoId,
            @Valid @RequestBody CriarPagamentoRequest request
    ) {
        return pagamentoService.criarTentativa(authentication.getName(), pedidoId, request);
    }

    @GetMapping("/api/pedidos/{pedidoId}/pagamentos")
    public List<PagamentoResponse> listar(
            Authentication authentication,
            @PathVariable UUID pedidoId
    ) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return pagamentoService.listar(authentication.getName(), admin, pedidoId);
    }

    @PostMapping("/api/admin/pagamentos/{pagamentoId}/confirmacao")
    @PreAuthorize("hasRole('ADMIN')")
    public PagamentoResponse confirmar(
            Authentication authentication,
            @PathVariable UUID pagamentoId,
            @Valid @RequestBody ConfirmarPagamentoRequest request
    ) {
        return pagamentoService.confirmar(
                pagamentoId, request.resultado(), request.transacaoExterna(), authentication.getName()
        );
    }
}
