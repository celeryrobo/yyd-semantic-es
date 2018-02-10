package com.yyd.semantic.http.error;

import org.elasticsearch.rest.RestStatus;

public class NotFoundError extends HTTPError {
	private static final long serialVersionUID = 1L;

	public NotFoundError() {
		super("Not Found", RestStatus.NOT_FOUND);
	}

}
