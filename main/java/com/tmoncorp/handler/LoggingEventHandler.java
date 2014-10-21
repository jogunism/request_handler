package com.tmoncorp.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.tmoncorp.domain.Event;
import com.tmoncorp.model.LogInfo;
import com.tmoncorp.util.DateUtil;

public class LoggingEventHandler implements EventHandler<Event> {
	private static final Logger logger = LoggerFactory.getLogger(LoggingEventHandler.class);
	private static final String SEND_LOG_FORM = "{} {} {} {} {} {}"; 
	private static final String REACTION_LOG_FORM = "{} {} {} {} {}"; 

	@Override
	public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
		LogInfo log = event.getLoginfo();
		if (log.isSendLog()) {
			logger.info(SEND_LOG_FORM, DateUtil.logDate(log.getEventDate()), log.getType(), log.getCampaignSerial(), log.getUserNo(), log.getDeviceToken(), log.getLoginYn());
		} else {
			logger.info(REACTION_LOG_FORM, DateUtil.logDate(log.getEventDate()), log.getType(), log.getCampaignSerial(), log.getUserNo(), log.getDeviceToken());
		}
	}
}
