package br.com.cupcake.srv.controller;

import br.com.cupcake.srv.request.carrinho.ItemCarrinhoRequest;
import br.com.cupcake.srv.response.carrinho.CarrinhoResponse;
import br.com.cupcake.srv.service.CarrinhoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/carrinho")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('CLIENTE')")
public class CarrinhoController {
    private final CarrinhoService carrinhoService;

    public CarrinhoController(CarrinhoService carrinhoService) {
        this.carrinhoService = carrinhoService;
    }

    @GetMapping("/{idUsuario}")
    public CarrinhoResponse obter(Authentication authentication, @PathVariable UUID idUsuario) {
        return carrinhoService.obter(authentication.getName(), idUsuario);
    }

    @PostMapping("/itens")
    @ResponseStatus(HttpStatus.CREATED)
    public CarrinhoResponse adicionar(
            Authentication authentication,
            @Valid @RequestBody ItemCarrinhoRequest request
    ) {
        return carrinhoService.adicionar(authentication.getName(), request);
    }

    @PutMapping("/itens")
    public CarrinhoResponse atualizar(
            Authentication authentication,
            @Valid @RequestBody ItemCarrinhoRequest request
    ) {
        return carrinhoService.adicionar(authentication.getName(), request);
    }

    @DeleteMapping("/itens/{produtoId}")
    public CarrinhoResponse remover(Authentication authentication, @PathVariable UUID produtoId) {
        return carrinhoService.remover(authentication.getName(), produtoId);
    }
}
