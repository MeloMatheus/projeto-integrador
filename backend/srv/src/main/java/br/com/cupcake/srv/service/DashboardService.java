package br.com.cupcake.srv.service;

import br.com.cupcake.srv.repository.CupomRepository;
import br.com.cupcake.srv.repository.PagamentoRepository;
import br.com.cupcake.srv.repository.PedidoRepository;
import br.com.cupcake.srv.repository.ProdutoRepository;
import br.com.cupcake.srv.repository.UsuarioRepository;
import br.com.cupcake.srv.response.DashboardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final CupomRepository cupomRepository;
    private final PedidoRepository pedidoRepository;
    private final PagamentoRepository pagamentoRepository;

    public DashboardService(
            UsuarioRepository usuarioRepository,
            ProdutoRepository produtoRepository,
            CupomRepository cupomRepository,
            PedidoRepository pedidoRepository,
            PagamentoRepository pagamentoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.cupomRepository = cupomRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse carregar() {
        return new DashboardResponse(
                usuarioRepository.countByAtivoTrue(),
                produtoRepository.countByDisponivelTrue(),
                cupomRepository.countByAtivoTrue(),
                pedidoRepository.countByStatusPedidoNome("PENDENTE_PAGAMENTO"),
                pedidoRepository.count(),
                pagamentoRepository.totalAprovado()
        );
    }
}
