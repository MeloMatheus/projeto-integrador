package br.com.cupcake.srv.controller;

import br.com.cupcake.srv.request.cupom.CupomRequest;
import br.com.cupcake.srv.response.cupom.CupomResponse;
import br.com.cupcake.srv.service.CupomService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/cupons")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class CupomController {
    private final CupomService cupomService;

    public CupomController(CupomService cupomService) {
        this.cupomService = cupomService;
    }

    @GetMapping
    public Page<CupomResponse> listar(Pageable pageable) {
        return cupomService.listar(PageRequest.of(
                pageable.getPageNumber(), Math.min(pageable.getPageSize(), 100), pageable.getSort()
        ));
    }

    @PostMapping
    public ResponseEntity<CupomResponse> criar(@Valid @RequestBody CupomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cupomService.criar(request));
    }

    @PutMapping("/{id}")
    public CupomResponse atualizar(@PathVariable UUID id, @Valid @RequestBody CupomRequest request) {
        return cupomService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        cupomService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
