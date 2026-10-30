package br.com.cupcake.srv.controller;

import br.com.cupcake.srv.request.produto.ProdutoRequest;
import br.com.cupcake.srv.response.produto.ProdutoResponse;
import br.com.cupcake.srv.service.ProdutoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public Page<ProdutoResponse> listar(
            @RequestParam(required = false) String nome,
            Pageable pageable
    ) {
        return produtoService.listar(nome, PageRequest.of(
                pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), pageable.getSort()
        ));
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable UUID id) {
        return produtoService.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ProdutoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ProdutoRequest request) {
        return produtoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
