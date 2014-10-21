package com.tmoncorp.component;

import com.lmax.disruptor.EventTranslator;
import com.tmoncorp.domain.Event;
import com.tmoncorp.model.LogInfo;

public class PushCollectorTranslator implements EventTranslator<Event> {

	private LogInfo loginfo;
	
	public void setLoginfo(LogInfo loginfo) {
		this.loginfo = loginfo;
	}

	@Override
	public void translateTo(Event event, long sequence) {
		event.setLoginfo(loginfo);
	}
}