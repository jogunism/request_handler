package com.tmoncorp.domain;

import com.lmax.disruptor.EventFactory;
import com.tmoncorp.model.LogInfo;

public class Event {

	private LogInfo loginfo;

	public LogInfo getLoginfo() {
		return loginfo;
	}

	public void setLoginfo(LogInfo loginfo) {
		this.loginfo = loginfo;
	}

	public final static EventFactory<Event> EVENT_FACTORY = new EventFactory<Event>() {
        public Event newInstance() {
            return new Event();
        }
    };
}