package br.com.cupcake.srv.controller;

import br.com.cupcake.srv.request.endereco.EnderecoRequest;
import br.com.cupcake.srv.response.endereco.EnderecoResponse;
import br.com.cupcake.srv.service.EnderecoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/enderecos")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('CLIENTE')")
public class EnderecoController {
    private final EnderecoService enderecoService;

    public EnderecoController(EnderecoService enderecoService) {
        this.enderecoService = enderecoService;
    }

    @GetMapping("/{idUsuario}")
    public List<EnderecoResponse> listar(Authentication authentication, @PathVariable UUID idUsuario) {
        return enderecoService.listar(authentication.getName(), idUsuario);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnderecoResponse criar(
            Authentication authentication,
            @Valid @RequestBody EnderecoRequest request
    ) {
        return enderecoService.criar(authentication.getName(), request);
    }
}
