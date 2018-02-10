package com.yyd.semantic.http.response;

import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;

public class Response implements IResponse {
	private RestStatus status;
	private String content;
	
	public Response(RestStatus status) {
		this.status = status;
	}
	
	@Override
	public RestStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(RestStatus status) {
		this.status = status;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public RestResponse buildResponse() {
		return new BytesRestResponse(status, content);
	}

}
