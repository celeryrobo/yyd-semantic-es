package com.yyd.semantic.http.error;

import org.elasticsearch.rest.RestStatus;

public class InternalServerError extends HTTPError {
	private static final long serialVersionUID = 1L;

	public InternalServerError() {
		super("Internal Server Error", RestStatus.INTERNAL_SERVER_ERROR);
	}

}
