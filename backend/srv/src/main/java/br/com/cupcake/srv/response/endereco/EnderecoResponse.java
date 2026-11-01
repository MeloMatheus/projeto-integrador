package br.com.cupcake.srv.response.endereco;

import br.com.cupcake.srv.model.EnderecoModel;

import java.util.UUID;

public record EnderecoResponse(
        UUID idEndereco,
        String identificacao,
        String destinatario,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String referencia,
        boolean principal
) {
    public static EnderecoResponse from(EnderecoModel endereco) {
        return new EnderecoResponse(
                endereco.getId(),
                endereco.getIdentificacao(),
                endereco.getDestinatario(),
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getUf(),
                endereco.getReferencia(),
                endereco.getPrincipal()
        );
    }
}
