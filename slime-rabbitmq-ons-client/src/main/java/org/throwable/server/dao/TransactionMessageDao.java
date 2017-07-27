package org.throwable.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.throwable.server.model.TransactionMessage;

import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 17:33
 */
@Repository
public class TransactionMessageDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public TransactionMessage save(final TransactionMessage transactionMessage) {
        final String sql = "INSERT INTO t_transaction_message(queue, exchange, routingKey, content) VALUES (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, transactionMessage.getQueue());
            ps.setString(2, transactionMessage.getExchange());
            ps.setString(3, transactionMessage.getRoutingKey());
            ps.setString(4, transactionMessage.getContent());
            return ps;
        }, keyHolder);
        transactionMessage.setId(keyHolder.getKey().longValue());
        return transactionMessage;
    }

    public TransactionMessage fetchById(Long id) {
        final String sql = "SELECT * FROM t_transaction_message WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new BeanPropertyRowMapper<>(TransactionMessage.class));
    }
}
