package com.yyd.semantic.http.request;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import org.elasticsearch.rest.RestRequest.Method;

public interface IRequest {
	public Map<String, List<String>> getHeaders();

	public String getHeader(String name);

	public Map<String, String> getParamters();

	public String getParamter(String name);

	public String getParamter(String name, String defaultValue);

	public SocketAddress getLocalAddress();

	public SocketAddress getRemoteAddress();

	public Method getMethod();

	public String getUri();
	
	public String getPath();
}
