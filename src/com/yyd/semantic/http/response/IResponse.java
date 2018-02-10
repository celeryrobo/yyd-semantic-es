package com.yyd.semantic.http.response;

import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;

public interface IResponse {
	public RestStatus getStatus();

	public void setStatus(RestStatus status);

	public String getContent();

	public void setContent(String content);

	public RestResponse buildResponse();
}
