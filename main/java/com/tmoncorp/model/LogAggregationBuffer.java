package com.tmoncorp.model;

import java.util.Date;

public class LogAggregationBuffer {

	private int sendSuccessCount = 0;
	private int sendFailCount = 0;
	private int receiveCount = 0;
	private int readCount = 0;
	private Date firstSentDate;
	private Date lastSentDate;


	public LogAggregationBuffer(LogInfo log) {
		firstSentDate = log.getEventDate();
		accumulate(log);
	}

	private void accumulate(LogInfo log) {
		final String type = log.getType();
		if (type.equals("send")) {
			++sendSuccessCount;
		}

		if (type.equals("fail")) {
			++sendFailCount;
		}

		if (type.equals("receive")) {
			++receiveCount;
		}

		if (type.equals("read")) {
			++readCount;
		}
		lastSentDate = log.getEventDate();
	}

	public void updateCount(LogInfo log) {
		accumulate(log);
	}

	public int getSendSuccessCount() {
		return sendSuccessCount;
	}

	public int getSendFailCount() {
		return sendFailCount;
	}

	public int getReceiveCount() {
		return receiveCount;
	}

	public int getReadCount() {
		return readCount;
	}

	public Date getFirstSentDate() {
		return firstSentDate;
	}

	public Date getLastSentDate() {
		return lastSentDate;
	}
}