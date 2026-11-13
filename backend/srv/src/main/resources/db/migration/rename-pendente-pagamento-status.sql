DO $$
DECLARE
    pending_id UUID;
    old_id UUID;
    next_order SMALLINT;
BEGIN
    SELECT id_status_pedido INTO pending_id
    FROM status_pedido
    WHERE nome = 'PENDENTE_PAGAMENTO';

    SELECT id_status_pedido INTO old_id
    FROM status_pedido
    WHERE nome = 'PAGAMENTO_ENTREGA';

    IF pending_id IS NOT NULL AND old_id IS NOT NULL THEN
        UPDATE pedido
        SET id_status_pedido = pending_id
        WHERE id_status_pedido = old_id;

        UPDATE historico_status_pedido
        SET id_status_pedido = pending_id
        WHERE id_status_pedido = old_id;

        DELETE FROM status_pedido
        WHERE id_status_pedido = old_id;
    ELSIF old_id IS NOT NULL THEN
        UPDATE status_pedido
        SET nome = 'PENDENTE_PAGAMENTO',
            descricao = 'Pagamento deve ser confirmado antes do preparo'
        WHERE id_status_pedido = old_id;
    ELSIF pending_id IS NULL THEN
        SELECT order_candidate INTO next_order
        FROM generate_series(0, 255) AS candidates(order_candidate)
        WHERE NOT EXISTS (
            SELECT 1 FROM status_pedido WHERE ordem = order_candidate
        )
        ORDER BY order_candidate
        LIMIT 1;

        IF next_order IS NULL THEN
            RAISE EXCEPTION 'Não há uma ordem disponível para o status PENDENTE_PAGAMENTO';
        END IF;

        INSERT INTO status_pedido (id_status_pedido, nome, descricao, ordem)
        VALUES (
            gen_random_uuid(),
            'PENDENTE_PAGAMENTO',
            'Pagamento deve ser confirmado antes do preparo',
            next_order
        );
    END IF;
END $$;
