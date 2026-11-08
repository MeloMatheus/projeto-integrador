package br.com.cupcake.srv.controller;

import br.com.cupcake.srv.request.pedido.AlterarStatusPedidoRequest;
import br.com.cupcake.srv.request.pedido.CriarPedidoRequest;
import br.com.cupcake.srv.response.pedido.PedidoResponse;
import br.com.cupcake.srv.service.PedidoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public Page<PedidoResponse> listar(
            Authentication authentication,
            Pageable pageable
    ) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return pedidoService.listar(authentication.getName(), admin,
                PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), pageable.getSort()));
    }

    @GetMapping("/usuario/{idUsuario}")
    public Page<PedidoResponse> listarPorUsuario(
            @PathVariable UUID idUsuario,
            Authentication authentication,
            Pageable pageable
    ) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return pedidoService.listarPorUsuario(
                authentication.getName(),
                admin,
                idUsuario,
                PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), pageable.getSort())
        );
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable UUID id, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return pedidoService.buscar(authentication.getName(), admin, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLIENTE')")
    public PedidoResponse criar(Authentication authentication, @Valid @RequestBody CriarPedidoRequest request) {
        return pedidoService.criar(authentication.getName(), request);
    }

    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public PedidoResponse alterarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AlterarStatusPedidoRequest request,
            Authentication authentication
    ) {
        return pedidoService.alterarStatus(id, request.status(), authentication.getName(), request.observacao());
    }
}
