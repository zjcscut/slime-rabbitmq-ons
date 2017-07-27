package org.throwable.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.throwable.server.model.TransactionLog;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/27 17:33
 */
@Repository
public class TransactionLogDao {

    private final BeanPropertyRowMapper<TransactionLog> beanPropertyRowMapper = BeanPropertyRowMapper.newInstance(TransactionLog.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public TransactionLog save(TransactionLog transactionLog, Long transactionMessageId) {
        final String sql = "INSERT INTO t_transaction_log(transactionMessageId, messageId, uniqueCode, sendStats," +
                "transactionStats, pushStats,transactionId) VALUES(?,?,?,?,?,?,?)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, transactionMessageId);
            ps.setString(2, transactionLog.getMessageId());
            ps.setString(3, transactionLog.getUniqueCode());
            ps.setString(4, transactionLog.getSendStats());
            ps.setString(5, transactionLog.getTransactionStats());
            ps.setString(6, transactionLog.getPushStats());
            ps.setLong(7, transactionLog.getTransactionId());
            return ps;
        }, keyHolder);
        transactionLog.setId(keyHolder.getKey().longValue());
        return transactionLog;
    }

    public TransactionLog fetchByUniqueCode(String uniqueCode) {
        final String sql = "SELECT * FROM t_transaction_log WHERE uniqueCode = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{uniqueCode}, beanPropertyRowMapper);
    }

    public int updatePushStats(Long id, String pushStats, String updateTime) {
        final String sql = "UPDATE t_transaction_log SET pushStats = ?,updateTime =? WHERE id = ?";
        return jdbcTemplate.update(sql, pushStats, updateTime, id);
    }
}
