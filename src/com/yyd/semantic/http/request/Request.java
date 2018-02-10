package com.yyd.semantic.http.request;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestRequest.Method;

public class Request implements IRequest {
	private RestRequest request;

	public Request(RestRequest request) {
		this.request = request;
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return request.getHeaders();
	}

	@Override
	public String getHeader(String name) {
		return request.header(name);
	}

	@Override
	public Map<String, String> getParamters() {
		return request.params();
	}

	@Override
	public String getParamter(String name) {
		return request.param(name);
	}

	@Override
	public String getParamter(String name, String defaultValue) {
		return request.param(name, defaultValue);
	}

	@Override
	public SocketAddress getLocalAddress() {
		return request.getLocalAddress();
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return request.getRemoteAddress();
	}

	@Override
	public Method getMethod() {
		return request.method();
	}

	@Override
	public String getUri() {
		return request.uri();
	}

	@Override
	public String getPath() {
		return request.path();
	}

}
