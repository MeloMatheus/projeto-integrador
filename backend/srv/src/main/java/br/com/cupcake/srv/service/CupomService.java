package br.com.cupcake.srv.service;

import br.com.cupcake.srv.model.CupomModel;
import br.com.cupcake.srv.repository.CupomRepository;
import br.com.cupcake.srv.repository.TipoDescontoRepository;
import br.com.cupcake.srv.request.cupom.CupomRequest;
import br.com.cupcake.srv.response.cupom.CupomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;

@Service
public class CupomService {
    private final CupomRepository cupomRepository;
    private final TipoDescontoRepository tipoDescontoRepository;

    public CupomService(CupomRepository cupomRepository, TipoDescontoRepository tipoDescontoRepository) {
        this.cupomRepository = cupomRepository;
        this.tipoDescontoRepository = tipoDescontoRepository;
    }

    @Transactional(readOnly = true)
    public Page<CupomResponse> listar(Pageable pageable) {
        return cupomRepository.findAll(pageable).map(CupomResponse::from);
    }

    @Transactional
    public CupomResponse criar(CupomRequest request) {
        CupomModel cupom = new CupomModel();
        apply(request, cupom);
        return CupomResponse.from(cupomRepository.save(cupom));
    }

    @Transactional
    public CupomResponse atualizar(UUID id, CupomRequest request) {
        CupomModel cupom = cupomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cupom não encontrado."));
        apply(request, cupom);
        return CupomResponse.from(cupom);
    }

    @Transactional
    public void excluir(UUID id) {
        CupomModel cupom = cupomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cupom não encontrado."));
        cupom.setAtivo(false);
    }

    private void apply(CupomRequest request, CupomModel cupom) {
        String tipo = request.tipoDesconto().trim().toUpperCase(Locale.ROOT);
        if (!tipo.equals("PERCENTUAL") && !tipo.equals("VALOR_FIXO")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de desconto inválido.");
        }
        if (request.dataFim().isBefore(request.dataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data final deve ser posterior à data inicial.");
        }
        if (tipo.equals("PERCENTUAL") && request.valorDesconto().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O desconto percentual não pode exceder 100%.");
        }
        cupom.setCodigo(request.codigo().trim().toUpperCase(Locale.ROOT));
        cupom.setDescricao(request.descricao());
        cupom.setTipoDesconto(tipoDescontoRepository.findByNome(tipo)
                .orElseThrow(() -> new IllegalStateException("Tipo de desconto ausente: " + tipo)));
        cupom.setValorDesconto(request.valorDesconto().setScale(2, RoundingMode.HALF_UP));
        cupom.setDataInicio(request.dataInicio());
        cupom.setDataFim(request.dataFim());
        cupom.setLimiteUtilizacoes(request.limiteUtilizacoes());
        cupom.setValorMinimoPedido(request.valorMinimoPedido().setScale(2, RoundingMode.HALF_UP));
        cupom.setAtivo(request.ativo());
    }
}
