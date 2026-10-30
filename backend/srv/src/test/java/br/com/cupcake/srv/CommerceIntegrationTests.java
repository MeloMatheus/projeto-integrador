package br.com.cupcake.srv;

import br.com.cupcake.srv.model.PerfilModel;
import br.com.cupcake.srv.model.StatusPagamentoModel;
import br.com.cupcake.srv.model.StatusPedidoModel;
import br.com.cupcake.srv.model.TipoDescontoModel;
import br.com.cupcake.srv.model.UsuarioModel;
import br.com.cupcake.srv.repository.PerfilRepository;
import br.com.cupcake.srv.repository.StatusPagamentoRepository;
import br.com.cupcake.srv.repository.StatusPedidoRepository;
import br.com.cupcake.srv.repository.TipoDescontoRepository;
import br.com.cupcake.srv.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CommerceIntegrationTests {
    private static final String ADMIN_PASSWORD = "admin-password-123";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PerfilRepository perfilRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private StatusPedidoRepository statusPedidoRepository;
    @Autowired
    private StatusPagamentoRepository statusPagamentoRepository;
    @Autowired
    private TipoDescontoRepository tipoDescontoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminEmail;

    @BeforeEach
    void seedLookupData() {
        ensureProfile("CLIENTE");
        PerfilModel adminProfile = ensureProfile("ADMIN");
        ensureOrderStatus(1, "PENDENTE_PAGAMENTO");
        ensureOrderStatus(2, "PAGO");
        ensureOrderStatus(3, "EM_PREPARACAO");
        ensureOrderStatus(4, "ENVIADO");
        ensureOrderStatus(5, "CONCLUIDO");
        ensureOrderStatus(6, "CANCELADO");
        ensurePaymentStatus(1, "PENDENTE");
        ensurePaymentStatus(2, "APROVADO");
        ensurePaymentStatus(3, "RECUSADO");
        ensurePaymentStatus(4, "CANCELADO");
        if (tipoDescontoRepository.findByNome("PERCENTUAL").isEmpty()) {
            TipoDescontoModel type = new TipoDescontoModel();
            type.setId(1);
            type.setNome("PERCENTUAL");
            tipoDescontoRepository.save(type);
        }

        adminEmail = "admin-" + UUID.randomUUID() + "@example.com";
        UsuarioModel admin = new UsuarioModel();
        admin.setName("Administrador");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setPerfil(adminProfile);
        admin.setAtivo(true);
        usuarioRepository.save(admin);
    }

    @Test
    void orderCouponAndPaymentFlowIsAtomicAndEnforcesOwnership() throws Exception {
        String clientEmail = "buyer-" + UUID.randomUUID() + "@example.com";
        register(clientEmail, "buyer-password-123");
        String clientToken = token(clientEmail, "buyer-password-123");
        String adminToken = token(adminEmail, ADMIN_PASSWORD);

        MvcResult productResult = mockMvc.perform(post("/api/produtos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Cupcake de teste","descricao":"Baunilha","preco":12.50,"disponivel":true}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String productId = json(productResult).get("idProduto").asText();

        mockMvc.perform(post("/api/carrinho/itens")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "idProduto", productId,
                                "quantidade", 2
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(25.0));

        mockMvc.perform(post("/api/admin/cupons")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "codigo", "DOCE10",
                                "descricao", "Desconto de teste",
                                "tipoDesconto", "PERCENTUAL",
                                "valorDesconto", 10,
                                "dataInicio", LocalDateTime.now().minusDays(1).toString(),
                                "dataFim", LocalDateTime.now().plusDays(1).toString(),
                                "valorMinimoPedido", 0,
                                "ativo", true
                        ))))
                .andExpect(status().isCreated());

        MvcResult orderResult = mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigoCupom\":\"doce10\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(25.0))
                .andExpect(jsonPath("$.valorDesconto").value(2.5))
                .andExpect(jsonPath("$.valorTotal").value(22.5))
                .andExpect(jsonPath("$.status").value("PENDENTE_PAGAMENTO"))
                .andExpect(jsonPath("$.historico.length()").value(1))
                .andReturn();
        JsonNode order = json(orderResult);
        String orderId = order.get("idPedido").asText();

        MvcResult paymentResult = mockMvc.perform(post("/api/pedidos/{id}/pagamentos", orderId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metodo\":\"PIX\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.valor").value(22.5))
                .andReturn();
        String paymentId = json(paymentResult).get("idPagamento").asText();

        String confirmation = "{\"resultado\":\"APROVADO\",\"transacaoExterna\":\"tx-"
                + UUID.randomUUID() + "\"}";
        mockMvc.perform(post("/api/admin/pagamentos/{id}/confirmacao", paymentId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));
        mockMvc.perform(post("/api/admin/pagamentos/{id}/confirmacao", paymentId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmation))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faturamentoConfirmado").value(22.5));
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/pedidos/{id}", orderId)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGO"))
                .andExpect(jsonPath("$.historico.length()").value(2));

        String otherEmail = "other-" + UUID.randomUUID() + "@example.com";
        register(otherEmail, "other-password-123");
        mockMvc.perform(get("/api/pedidos/{id}", orderId)
                        .header("Authorization", "Bearer " + token(otherEmail, "other-password-123")))
                .andExpect(status().isNotFound());
    }

    private PerfilModel ensureProfile(String name) {
        return perfilRepository.findByNomeIgnoreCase(name).orElseGet(() -> {
            PerfilModel profile = new PerfilModel();
            profile.setNome(name);
            return perfilRepository.save(profile);
        });
    }

    private void ensureOrderStatus(int id, String name) {
        if (statusPedidoRepository.findByNome(name).isEmpty()) {
            StatusPedidoModel status = new StatusPedidoModel();
            status.setId(id);
            status.setNome(name);
            statusPedidoRepository.save(status);
        }
    }

    private void ensurePaymentStatus(int id, String name) {
        if (statusPagamentoRepository.findByNome(name).isEmpty()) {
            StatusPagamentoModel status = new StatusPagamentoModel();
            status.setId(id);
            status.setNome(name);
            statusPagamentoRepository.save(status);
        }
    }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of(
                        "nome", "Cliente",
                        "email", email,
                        "senha", password
                )))).andExpect(status().isCreated());
    }

    private String token(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "email", email,
                                "senha", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        return json(result).get("token").asText();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
