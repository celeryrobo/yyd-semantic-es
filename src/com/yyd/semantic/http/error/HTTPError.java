package com.yyd.semantic.http.error;

import org.elasticsearch.rest.RestStatus;

public class HTTPError extends Error {
	private static final long serialVersionUID = 1L;
	private RestStatus status;

	public HTTPError(String msg, RestStatus status) {
		super(msg);
		this.status = status;
	}

	public RestStatus getStatus() {
		return status;
	}
}
