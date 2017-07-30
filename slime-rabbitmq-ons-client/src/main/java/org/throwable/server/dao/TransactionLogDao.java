package org.throwable.server.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.throwable.server.model.TransactionLog;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

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
		final String sql = "INSERT INTO t_transaction_log(transactionMessageId, messageId, uniqueCode," +
				"transactionStats, pushStats,transactionId,fireTransactionStats,checkerClassName) VALUES(?,?,?,?,?,?,?,?)";
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(con -> {
			PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setLong(1, transactionMessageId);
			ps.setString(2, transactionLog.getMessageId());
			ps.setString(3, transactionLog.getUniqueCode());
			ps.setString(4, transactionLog.getTransactionStats());
			ps.setString(5, transactionLog.getPushStats());
			ps.setLong(6, transactionLog.getTransactionId());
			ps.setString(7, transactionLog.getFireTransactionStats());
			ps.setString(8, transactionLog.getCheckerClassName());
			return ps;
		}, keyHolder);
		transactionLog.setId(keyHolder.getKey().longValue());
		return transactionLog;
	}

	public TransactionLog fetchByUniqueCode(String uniqueCode) {
		final String sql = "SELECT * FROM t_transaction_log WHERE uniqueCode = ?";
		return jdbcTemplate.queryForObject(sql, new Object[]{uniqueCode}, beanPropertyRowMapper);
	}

	public int updatePushStats(Long id, String pushStats, Date pushTime) {
		final String sql = "UPDATE t_transaction_log SET pushStats = ?,pushTime = ? WHERE id = ?";
		return jdbcTemplate.update(sql, pushStats, pushTime, id);
	}

	public int[] batchUpdatePushStats(List<TransactionLog> records) {
		final String sql = "UPDATE t_transaction_log SET pushStats = ?,pushTime = ?,pushAttemptTime = ? WHERE id = ?";
		return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int index) throws SQLException {
				TransactionLog transactionLog = records.get(index);
				ps.setString(1, transactionLog.getPushStats());
				ps.setTimestamp(2, new Timestamp(transactionLog.getPushTime().getTime()));
				ps.setInt(3, transactionLog.getPushAttemptTime());
				ps.setLong(4, transactionLog.getId());
			}

			@Override
			public int getBatchSize() {
				return records.size();
			}
		});
	}

	public int updateFireTransactionStats(Long id, String fireTransactionStats, Date fireTransactionTime) {
		final String sql = "UPDATE t_transaction_log SET fireTransactionStats = ?,fireTransactionTime = ? WHERE id = ?";
		return jdbcTemplate.update(sql, fireTransactionStats, fireTransactionTime, id);
	}

	public int[] batchUpdateFireTransactionStats(List<TransactionLog> records) {
		final String sql = "UPDATE t_transaction_log SET fireTransactionStats = ?,fireTransactionTime = ?,checkAttemptTime = ? WHERE id = ?";
		return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int index) throws SQLException {
				TransactionLog transactionLog = records.get(index);
				ps.setString(1, transactionLog.getFireTransactionStats());
				ps.setTimestamp(2, new Timestamp(transactionLog.getFireTransactionTime().getTime()));
				ps.setInt(3, transactionLog.getCheckAttemptTime());
				ps.setLong(4, transactionLog.getId());
			}

			@Override
			public int getBatchSize() {
				return records.size();
			}
		});
	}

	public int updateTransactionStats(Long id, String transactionStats, Date transactionEndTime) {
		final String sql = "UPDATE t_transaction_log SET transactionStats = ?,transactionEndTime = ? WHERE id = ?";
		return jdbcTemplate.update(sql, transactionStats, transactionEndTime, id);
	}

	public List<TransactionLog> queryRecordsToFire(String transactionStats, int pageNumber, int pageSize, Date delta, Integer maxCheckAttemptTime) {
		final String sql = "SELECT * FROM t_transaction_log WHERE transactionStats = ? AND checkAttemptTime < ? AND createTime < ? LIMIT ?,?";
		return jdbcTemplate.query(sql, new Object[]{transactionStats, maxCheckAttemptTime, delta, pageNumber, pageSize}, beanPropertyRowMapper);
	}

	public List<TransactionLog> queryRecordsToPush(String transactionStats, String pushStatsInit, String pushStatsFail
			, int pageNumber, int pageSize, Date delta, Integer maxPushAttemptTime) {
		final String sql = "SELECT * FROM t_transaction_log WHERE transactionStats = ? AND pushStats IN (?,?) AND pushAttemptTime < ? AND createTime < ? LIMIT ?,?";
		return jdbcTemplate.query(sql, new Object[]{transactionStats, pushStatsFail, pushStatsInit, maxPushAttemptTime, delta, pageNumber, pageSize}, beanPropertyRowMapper);
	}
}
