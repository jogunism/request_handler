package com.tmoncorp.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tmoncorp.model.LogAggregationBuffer;
import com.tmoncorp.model.LogInfo;
import com.tmoncorp.util.DateUtil;
import com.tmoncorp.util.MySqlConnector;
import com.tmoncorp.util.SqlResourceManager;

public class PushLogDatabaseTask implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(PushLogDatabaseTask.class);
	private static final String ZERO_DATETIME = "0000-00-00 00:00:00";

	private static final String SQL_COMBINED_INSERT = new StringBuffer()
		.append("INSERT INTO mobile_push_log_combined (")
		.append("	push_item_srl, m_no, token, send_date, reaction_status, login_yn, birth_date, gender")
		.append(") VALUES (")
		.append("	?, ?, ?, ?, ?, ?,")
		.append("	IFNULL((SELECT birth_date FROM mobile_push_member WHERE m_no = ?), '0000-00-00'),")
		.append("	IFNULL((SELECT gender FROM mobile_push_member WHERE m_no = ?), 'U')")
		.append(");").toString();

	private static final String SQL_COMBINED_UPDATE = new StringBuffer()
		.append("UPDATE mobile_push_log_combined SET ")
		.append("	receive_date = IF(receive_date = '0000-00-00 00:00:00', ?, receive_date),")
		.append("	read_date = IF(read_date = '0000-00-00 00:00:00', ?, read_date),")
		.append("	reaction_status = IF(reaction_status >= ? OR reaction_status < 0, reaction_status, ?)")
		.append("WHERE push_item_srl = ? AND token = ?;").toString();

	private static final String SQL_AGGREGATION = new StringBuffer()
		.append("UPDATE mobile_push_aggregation SET ")
		.append("	send_success_count = send_success_count + ?,")
		.append("	send_fail_count = send_fail_count + ?,")
		.append("	receive_count = receive_count + ?,")
		.append("	read_count = read_count + ?,")
		.append("	first_sent_date = IF(first_sent_date = '0000-00-00 00:00:00', ?, first_sent_date),")
		.append("	last_sent_date = ? ")
		.append("WHERE push_item_srl = ?;").toString();

	private MySqlConnector connector;

	private List<LogInfo> combinedInserts;
	private List<LogInfo> combinedUpdates;
	private Map<Long, LogAggregationBuffer> aggregation;
	private long sleepTime;

	public PushLogDatabaseTask(MySqlConnector connector, List<LogInfo> buffer, long sleepTime) {
		this.connector = connector;
		this.sleepTime = sleepTime;
		combinedInserts = new ArrayList<LogInfo>(buffer.size());
		combinedUpdates = new ArrayList<LogInfo>(buffer.size());
		aggregation = new HashMap<Long, LogAggregationBuffer>();
		for (LogInfo each : buffer) {
			if (each.isSendLog()) {
				combinedInserts.add(each);
			} else {
				combinedUpdates.add(each);
			}

			calculateLogAggregation(each);
		}
	}

	public void execute() {
		if (isReady()) {
			Connection conn = null;
			PreparedStatement pstmt = null;

			try {
				conn = this.connector.getConnection();
				updateAggregation(conn, pstmt);
				insertCombinedLog(conn, pstmt);
				updateCombinedLog(conn, pstmt);
			} catch (Exception e) {
				log.error("DB LOGGING FAIL with: ", e);
				// TODO case of DB error, flush into file error log
			} finally {
				SqlResourceManager.close(pstmt);
				SqlResourceManager.close(conn);
			}
		}
	}

	private boolean isReady() {
		return !(combinedInserts.isEmpty() && combinedUpdates.isEmpty());
	}

	private void insertCombinedLog(Connection conn, PreparedStatement pstmt) throws SQLException {
		if (combinedInserts.isEmpty()) {
			return;
		}
		pstmt = conn.prepareStatement(SQL_COMBINED_INSERT);
		int pidx;
		for (LogInfo each : combinedInserts) {
			pidx = 0;
			pstmt.setLong(++pidx, each.getCampaignSerial());
			pstmt.setLong(++pidx, each.getUserNo());
			pstmt.setString(++pidx, each.getDeviceToken());
			pstmt.setString(++pidx, DateUtil.dbDate(each.getEventDate()));
			pstmt.setInt(++pidx, each.getReaction());
			pstmt.setString(++pidx, each.getLoginYn());
			pstmt.setLong(++pidx, each.getUserNo());
			pstmt.setLong(++pidx, each.getUserNo());
			pstmt.addBatch();
		}
		pstmt.executeBatch();
		log.info("INSERT {}pcs of send log with batch.", combinedInserts.size());
	}

	private void updateCombinedLog(Connection conn, PreparedStatement pstmt) throws SQLException {
		if (combinedUpdates.isEmpty()) {
			return;
		}
		pstmt = conn.prepareStatement(SQL_COMBINED_UPDATE);
		int pidx;
		for (LogInfo each : combinedUpdates) {
			pidx = 0;
			pstmt.setString(++pidx, this.receiveDate(each, DateUtil.dbDate(each.getEventDate())));
			pstmt.setString(++pidx, this.readDate(each, DateUtil.dbDate(each.getEventDate())));
			pstmt.setInt(++pidx, each.getReaction());
			pstmt.setInt(++pidx, each.getReaction());
			pstmt.setLong(++pidx, each.getCampaignSerial());
			pstmt.setString(++pidx, each.getDeviceToken());
			pstmt.addBatch();
		}
		pstmt.executeBatch();
		log.info("UPDATE {}pcs of receive/read log with batch.", combinedUpdates.size());
	}

	private String receiveDate(LogInfo info, String logDate) {
		if (info.isReceive()) {
			return logDate;
		} else {
			return ZERO_DATETIME;
		}
	}

	private String readDate(LogInfo info, String logDate) {
		if (info.isRead()) {
			return logDate;
		} else {
			return ZERO_DATETIME;
		}
	}

	private void calculateLogAggregation(LogInfo logInfo) {
		Long key = logInfo.getCampaignSerial();
		if (key.equals(0L)) {
			return;
		}

		if (aggregation.containsKey(key)) {
			aggregation.get(key).updateCount(logInfo);
		} else {
			aggregation.put(key, new LogAggregationBuffer(logInfo));
		}
	}

	private void updateAggregation(Connection conn, PreparedStatement pstmt) throws SQLException {
		int pidx;
		pstmt = conn.prepareStatement(SQL_AGGREGATION);
		for (Map.Entry<Long, LogAggregationBuffer> entry : aggregation.entrySet()) {
			pidx = 0;
			LogAggregationBuffer value = entry.getValue();
			pstmt.setInt(++pidx, value.getSendSuccessCount());
			pstmt.setInt(++pidx, value.getSendFailCount());
			pstmt.setInt(++pidx, value.getReceiveCount());
			pstmt.setInt(++pidx, value.getReadCount());
			pstmt.setString(++pidx, DateUtil.dbDate(value.getFirstSentDate()));
			pstmt.setString(++pidx, DateUtil.dbDate(value.getLastSentDate()));
			pstmt.setLong(++pidx, entry.getKey());
			pstmt.addBatch();
		}
		pstmt.executeBatch();
		log.info("UPDATE aggregaion with push_item_srl: {}", aggregation.keySet());
	}

	@Override
	public void run() {
		this.execute();
		try {
			Thread.sleep(this.sleepTime);
		} catch (Exception e) {
			
		}
	}
}