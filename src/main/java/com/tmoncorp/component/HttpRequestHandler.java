package com.tmoncorp.component;

import java.net.URLDecoder;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

import com.lmax.disruptor.dsl.Disruptor;
import com.tmoncorp.domain.Event;
import com.tmoncorp.model.LogInfo;

public class HttpRequestHandler implements Handler<HttpServerRequest> {
	private Disruptor<Event> disruptor;
	private PushCollectorTranslator translator;

	public HttpRequestHandler(Disruptor<Event> disruptor) {
		this.disruptor = disruptor;
		this.translator = new PushCollectorTranslator();
	}

	@Override
	public void handle(HttpServerRequest req) {
		String path = req.path;
		if ("".equals(path)) {
			req.response.statusCode = 404;
			req.response.statusMessage = "NOT FOUND";
		} else {
			req.response.statusCode = 200;
			req.response.statusMessage = "OK";
		}

		Handler<Buffer> bodyHandler = new EventBodyHandler(this.disruptor, this.translator, path);
		req.bodyHandler(bodyHandler);

		req.response.putHeader("Content-Length", "0");
		req.response.end();
		req.response.close();
	}

	static class EventBodyHandler implements Handler<Buffer> {
		private Disruptor<Event> disruptor;
		private PushCollectorTranslator translator;
		private String path;

		public EventBodyHandler(Disruptor<Event> disruptor, PushCollectorTranslator translator, String path) {
			this.disruptor = disruptor;
			this.translator = translator;
			this.path = path;
		}

		@Override
		public void handle(Buffer body) {
			try {
				this.path = URLDecoder.decode(this.path, "UTF-8");
				this.path = this.path.substring(1);

				ObjectMapper mapper = new ObjectMapper();
				JsonNode node = mapper.readTree(this.path);

				this.translator.setLoginfo(new LogInfo(node));
				this.disruptor.publishEvent(this.translator); 			
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
