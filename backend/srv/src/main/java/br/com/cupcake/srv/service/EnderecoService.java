package br.com.cupcake.srv.service;

import br.com.cupcake.srv.model.EnderecoModel;
import br.com.cupcake.srv.model.UsuarioModel;
import br.com.cupcake.srv.repository.EnderecoRepository;
import br.com.cupcake.srv.request.endereco.EnderecoRequest;
import br.com.cupcake.srv.response.endereco.EnderecoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class EnderecoService {
    private final EnderecoRepository enderecoRepository;
    private final UsuarioService usuarioService;

    public EnderecoService(EnderecoRepository enderecoRepository, UsuarioService usuarioService) {
        this.enderecoRepository = enderecoRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<EnderecoResponse> listar(String email) {
        UsuarioModel usuario = usuarioService.buscarAtivoPorEmail(email);
        return enderecoRepository.findByUsuarioIdOrderByPrincipalDescCriadoEmDesc(usuario.getId())
                .stream().map(EnderecoResponse::from).toList();
    }

    @Transactional
    public EnderecoResponse criar(String email, EnderecoRequest request) {
        UsuarioModel usuario = usuarioService.buscarAtivoPorEmail(email);
        EnderecoModel endereco = new EnderecoModel();
        endereco.setUsuario(usuario);
        endereco.setIdentificacao(request.identificacao());
        endereco.setDestinatario(request.destinatario().trim());
        endereco.setCep(request.cep().trim());
        endereco.setLogradouro(request.logradouro().trim());
        endereco.setNumero(request.numero().trim());
        endereco.setComplemento(request.complemento());
        endereco.setBairro(request.bairro().trim());
        endereco.setCidade(request.cidade().trim());
        endereco.setUf(request.uf().toUpperCase(Locale.ROOT));
        endereco.setReferencia(request.referencia());
        endereco.setPrincipal(request.principal());
        return EnderecoResponse.from(enderecoRepository.save(endereco));
    }

    @Transactional(readOnly = true)
    public EnderecoModel buscarDoUsuario(UUID id, UUID usuarioId) {
        return enderecoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado."));
    }
}
