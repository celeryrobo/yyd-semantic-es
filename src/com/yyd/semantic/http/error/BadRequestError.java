package com.yyd.semantic.http.error;

import org.elasticsearch.rest.RestStatus;

public class BadRequestError extends HTTPError {
	private static final long serialVersionUID = 1L;

	public BadRequestError() {
		super("Bad Request", RestStatus.BAD_REQUEST);
	}

}
