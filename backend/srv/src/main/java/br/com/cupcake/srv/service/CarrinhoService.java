package br.com.cupcake.srv.service;

import br.com.cupcake.srv.model.CarrinhoModel;
import br.com.cupcake.srv.model.ItemCarrinhoModel;
import br.com.cupcake.srv.model.ProdutoModel;
import br.com.cupcake.srv.model.StatusCarrinho;
import br.com.cupcake.srv.model.UsuarioModel;
import br.com.cupcake.srv.repository.CarrinhoRepository;
import br.com.cupcake.srv.repository.ItemCarrinhoRepository;
import br.com.cupcake.srv.request.carrinho.ItemCarrinhoRequest;
import br.com.cupcake.srv.response.carrinho.CarrinhoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CarrinhoService {
    private final CarrinhoRepository carrinhoRepository;
    private final ItemCarrinhoRepository itemRepository;
    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;

    public CarrinhoService(
            CarrinhoRepository carrinhoRepository,
            ItemCarrinhoRepository itemRepository,
            UsuarioService usuarioService,
            ProdutoService produtoService
    ) {
        this.carrinhoRepository = carrinhoRepository;
        this.itemRepository = itemRepository;
        this.usuarioService = usuarioService;
        this.produtoService = produtoService;
    }

    @Transactional
    public CarrinhoResponse obter(String email) {
        UsuarioModel usuario = usuarioService.buscarAtivoPorEmail(email);
        CarrinhoModel carrinho = getOrCreate(usuario);
        return response(carrinho);
    }

    @Transactional
    public CarrinhoResponse adicionar(String email, ItemCarrinhoRequest request) {
        UsuarioModel usuario = usuarioService.buscarAtivoPorEmail(email);
        CarrinhoModel carrinho = getOrCreate(usuario);
        ProdutoModel produto = produtoService.buscarDisponivel(request.idProduto());
        ItemCarrinhoModel item = itemRepository.findByCarrinhoIdAndProdutoId(carrinho.getId(), produto.getId())
                .orElseGet(() -> {
                    ItemCarrinhoModel created = new ItemCarrinhoModel();
                    created.setCarrinho(carrinho);
                    created.setProduto(produto);
                    return created;
                });
        item.setQuantidade(request.quantidade().longValue());
        item.setPrecoUnitario(produto.getPreco());
        itemRepository.save(item);
        return response(carrinho);
    }

    @Transactional
    public CarrinhoResponse remover(String email, UUID produtoId) {
        UsuarioModel usuario = usuarioService.buscarAtivoPorEmail(email);
        CarrinhoModel carrinho = getOrCreate(usuario);
        ItemCarrinhoModel item = itemRepository.findByCarrinhoIdAndProdutoId(carrinho.getId(), produtoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado no carrinho."));
        itemRepository.delete(item);
        return response(carrinho);
    }

    private CarrinhoModel getOrCreate(UsuarioModel usuario) {
        List<CarrinhoModel> openCarts = carrinhoRepository.findOpenByUsuarioIdForUpdate(
                usuario.getId(), StatusCarrinho.ATIVO
        );
        if (!openCarts.isEmpty()) {
            return openCarts.getFirst();
        }
        return carrinhoRepository.findFirstByUsuarioIdAndStatusOrderByCriadoEmDesc(usuario.getId(), StatusCarrinho.ATIVO)
                .orElseGet(() -> {
                    CarrinhoModel carrinho = new CarrinhoModel();
                    carrinho.setUsuario(usuario);
                    carrinho.setStatus(StatusCarrinho.ATIVO);
                    return carrinhoRepository.save(carrinho);
                });
    }

    private CarrinhoResponse response(CarrinhoModel carrinho) {
        List<ItemCarrinhoModel> items = itemRepository.findByCarrinhoId(carrinho.getId());
        return CarrinhoResponse.from(carrinho, items);
    }
}
