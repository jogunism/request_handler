package com.tmoncorp.component;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;

public class PushCollectorHttpServer implements InitializingBean, DisposableBean {

	private HttpServer server;	
	private int port;
	private Handler<HttpServerRequest> httpRequestHandler;		

	public void setPort(int port) {
		this.port = port;
	}

	public void setHttpRequestHandler(Handler<HttpServerRequest> httpRequestHandler) {
		this.httpRequestHandler = httpRequestHandler;
	}

	@Override
	public void destroy() throws Exception {
		this.server.close();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Vertx vertx = Vertx.newVertx();
		this.server = vertx.createHttpServer();
		server.requestHandler(this.httpRequestHandler);
		server.listen(this.port);
	}
}
