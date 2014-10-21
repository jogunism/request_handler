package com.tmoncorp.component;

import java.util.concurrent.Executors;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.tmoncorp.domain.Event;

public class PushCollectorDisruptorFactory implements FactoryBean<Disruptor<Event>>, InitializingBean {

	private Disruptor<Event> disruptor;
	private int ringSize;
	private EventHandler<Event> dataManagingHandler;
	private EventHandler<Event> loggingEventHandler;

	public void setRingSize(int ringSize) {
		this.ringSize = ringSize;
	}

	public void setDataManagingHandler(EventHandler<Event> dataManagingHandler) {
		this.dataManagingHandler = dataManagingHandler;
	}

	public void setLoggingEventHandler(EventHandler<Event> loggingEventHandler) {
		this.loggingEventHandler = loggingEventHandler;
	}


	@Override
	public Disruptor<Event> getObject() throws Exception {
		return this.disruptor;
	}

	@Override
	public Class<?> getObjectType() {
		return disruptor.getClass();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void afterPropertiesSet() throws Exception {
		disruptor = new Disruptor<Event>(Event.EVENT_FACTORY, 
				                        Executors.newCachedThreadPool(), 
			                            new SingleThreadedClaimStrategy(this.ringSize),
			                            new SleepingWaitStrategy());
		disruptor.handleEventsWith(this.dataManagingHandler, this.loggingEventHandler);
		disruptor.start();
	}
}