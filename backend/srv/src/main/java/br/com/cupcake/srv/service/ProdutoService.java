package br.com.cupcake.srv.service;

import br.com.cupcake.srv.model.ProdutoModel;
import br.com.cupcake.srv.repository.ProdutoRepository;
import br.com.cupcake.srv.request.produto.ProdutoRequest;
import br.com.cupcake.srv.response.produto.ProdutoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.RoundingMode;
import java.util.UUID;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listar(String nome, Pageable pageable) {
        Page<ProdutoModel> produtos = nome == null || nome.isBlank()
                ? produtoRepository.findByDisponivelTrue(pageable)
                : produtoRepository.findByDisponivelTrueAndNomeContainingIgnoreCase(nome.trim(), pageable);
        return produtos.map(ProdutoResponse::from);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscar(UUID id) {
        return produtoRepository.findById(id)
                .filter(ProdutoModel::getDisponivel)
                .map(ProdutoResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        ProdutoModel produto = new ProdutoModel();
        apply(request, produto);
        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(UUID id, ProdutoRequest request) {
        ProdutoModel produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        apply(request, produto);
        return ProdutoResponse.from(produto);
    }

    @Transactional
    public void excluir(UUID id) {
        ProdutoModel produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
        produto.setDisponivel(false);
    }

    @Transactional(readOnly = true)
    public ProdutoModel buscarDisponivel(UUID id) {
        return produtoRepository.findById(id)
                .filter(ProdutoModel::getDisponivel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Produto indisponível."));
    }

    private void apply(ProdutoRequest request, ProdutoModel produto) {
        produto.setNome(request.nome().trim());
        produto.setDescricao(request.descricao());
        produto.setImagemUrl(request.imagemUrl());
        produto.setPreco(request.preco().setScale(2, RoundingMode.HALF_UP));
        produto.setDisponivel(request.disponivel());
    }
}
