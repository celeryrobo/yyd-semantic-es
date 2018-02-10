package com.yyd.semantic.http.error;

import org.elasticsearch.rest.RestStatus;

public class NotImplementedError extends HTTPError {
	private static final long serialVersionUID = 1L;

	public NotImplementedError() {
		super("Not Implemented", RestStatus.NOT_IMPLEMENTED);
	}

}
