package com.tmoncorp.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.lmax.disruptor.EventHandler;
import com.tmoncorp.domain.Event;
import com.tmoncorp.model.LogInfo;
import com.tmoncorp.task.PushLogDatabaseTask;
import com.tmoncorp.util.MySqlConnector;

public class DataManagingHandler implements EventHandler<Event>, InitializingBean {
	private static final Logger log = LoggerFactory.getLogger(DataManagingHandler.class);
	private static final ExecutorService JOB_QUEUE = Executors.newSingleThreadExecutor();
	private static final String TRIGGERED_CALL = "TRIGGERED";
	private static final String TIMER_CALL = "TIMER";

	private IdleEventMonitoringTimer customTimer;
	private MySqlConnector connector;
	private long sleepTime;
	private int batchMaximum;
	private int timerInterval;

	private final Object lock = new Object(); 
	private Future<?> lastBusyTask;
	private List<LogInfo> eventBuffer = new ArrayList<>();
	private AtomicInteger counter = new AtomicInteger(0);

	public void setConnector(MySqlConnector connector) {
		this.connector = connector;
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public void setBatchMaximum(int batchMaximum) {
		this.batchMaximum = batchMaximum;
	}

	public void setTimerInterval(int timerInterval) {
		this.timerInterval = timerInterval;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.customTimer = new IdleEventMonitoringTimer(this, timerInterval);
		this.customTimer.runTimer();
	}

	@Override
	public void onEvent(Event event, long arg1, boolean arg2) {
		LogInfo logInfo = event.getLoginfo();
		if (logInfo == null) {
			return;
		}

		this.queueEvent(logInfo);
	}

	private void queueEvent(LogInfo logInfo) {
		int internalCounter;
		synchronized (lock) {
			eventBuffer.add(logInfo);
			internalCounter = counter.incrementAndGet();
		}

		if (internalCounter >= this.batchMaximum) {
			this.flushEvents(TRIGGERED_CALL, this.sleepTime);
		}
	}

	private void flushEvents(String flushCaller, long sleepTime) {
		List<LogInfo> bufferReference;
		synchronized (lock) {
			bufferReference = eventBuffer;
			eventBuffer = new ArrayList<>(this.batchMaximum);
			counter.set(0);
		}

		if (bufferReference.isEmpty()) {
			log.info("queue empty. nothing to do.");
			return;
		}

		log.info("flush invoked {} mode with batch queue size {}" , flushCaller, bufferReference.size());
		PushLogDatabaseTask job = new PushLogDatabaseTask(connector, bufferReference, sleepTime);
		lastBusyTask = JOB_QUEUE.submit(job);
	}

	private boolean isBusy() {
		if (lastBusyTask == null) {
			return false;
		}

		return !lastBusyTask.isDone();
	}

	public static class IdleEventMonitoringTimer {
		private EventFlushTask timerTask;
		private int delay = 50;
		private int period;
		private Timer timer;
		private DataManagingHandler handler;

		public IdleEventMonitoringTimer(DataManagingHandler handler, int interval) {
			this.period = interval;
			this.handler = handler;

			this.timerTask = new EventFlushTask(this.handler);
			this.timer = new Timer();
		}

		public void runTimer() {
			this.timer.scheduleAtFixedRate(this.timerTask, delay, period);
		}

		public void reset() {
			this.timerTask.cancel();
			this.timer.purge();
			this.timerTask = new EventFlushTask(this.handler);
		}
	}

	public static class EventFlushTask extends TimerTask {
		private DataManagingHandler handler;

		public EventFlushTask(DataManagingHandler handler) {
			this.handler = handler;
		}

		@Override
		public void run() {
			try {
				if (handler.isBusy()) {
					log.info("busy for event handling; skip timer task");
					return;
				}

				this.handler.flushEvents(TIMER_CALL, 0L);
			} catch (Exception e) {
				log.error("Exception", e);
			}
		}
	}
}