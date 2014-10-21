package com.tmoncorp.model;

import java.util.Calendar;
import java.util.Date;

import org.codehaus.jackson.JsonNode;

public class LogInfo {

	private String type;
	private long campaignSerial;
	private long userNo;
	private String deviceToken;
	private String loginYn;
	private Date date;

	public LogInfo(JsonNode source) throws Exception {
		validateSource(source);

		setType(source);	// send, fail, receive, read
		setCampaignSerial(source);
		setUserNo(source);
		setDeviceToken(source);
		setLoginYn(source);	// Y(login), N(logout), U(unknown)
		date = Calendar.getInstance().getTime();
	}

	private void validateSource(JsonNode node) throws Exception {
		if(node == null || node.get("type") == null || node.get("campaign") == null) {	// type, campaign 값 필요.
			throw new Exception("Incorrect data. recheck json string.");
		}
	}

	void setType(JsonNode source) {
		this.type = source.get("type").toString().replaceAll("\"", "");
	}

	void setCampaignSerial(JsonNode source) {
		JsonNode node = source.get("campaign");
		this.campaignSerial = (node == null ? 0L : node.getLongValue());
	}

	void setUserNo(JsonNode source) {
		this.userNo = source.get("mNo").getLongValue();
	}

	void setDeviceToken(JsonNode source) {
		this.deviceToken = source.get("token").toString().replaceAll("\"", "");
	}

	void setLoginYn(JsonNode source) {
		JsonNode node = source.get("login");
		this.loginYn = (node == null ? "U" : node.toString().replaceAll("\"", ""));
	}

	public String getType() {
		return type;
	}

	public long getCampaignSerial() {
		return campaignSerial;
	}

	public long getUserNo() {
		return userNo;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public String getLoginYn() {
		return loginYn;
	}

	public Date getEventDate() {
		return this.date;
	}

	public boolean isSendLog() {
		return ("send".equals(type) || "fail".equals(type));
	}

	// 이하는 조회 조건으로 인한 로그 테이블 추가 때문에 덧붙인 속성들. 추후 job 단위를 분리하는 등의 처리를 통해 분리할 필요가 있을 수 있다.
	public int getReaction() {
		if ("send".equals(type)) {
			return 0;
		} else if ("fail".equals(type)) {
			return -1;
		} else if ("receive".equals(type)) {
			return 1;
		} else if ("read".equals(type)) {
			return 2;
		} else {
			return -1;	// 아직 정의하지 않은 상태, 혹은 잘못 들어온 코드값은 모두 실패로 간주
		}
	}

	public boolean isReceive() {
		return this.getReaction() == 1;
	}

	public boolean isRead() {
		return this.getReaction() == 2;
	}
}
