package com.yyd.semantic.http.handler;

import org.elasticsearch.rest.RestStatus;

import com.yyd.semantic.http.error.HTTPError;
import com.yyd.semantic.http.request.IRequest;
import com.yyd.semantic.http.response.IResponse;

public class RequestHandler extends AbstractHandler {

	@Override
	public void get(IRequest request, IResponse response) {
		throw new HTTPError("Method Not Implemented", RestStatus.NOT_IMPLEMENTED);
	}

	@Override
	public void post(IRequest request, IResponse response) {
		throw new HTTPError("Method Not Implemented", RestStatus.NOT_IMPLEMENTED);
	}

	@Override
	public void put(IRequest request, IResponse response) {
		throw new HTTPError("Method Not Implemented", RestStatus.NOT_IMPLEMENTED);
	}

	@Override
	public void delete(IRequest request, IResponse response) {
		throw new HTTPError("Method Not Implemented", RestStatus.NOT_IMPLEMENTED);
	}

}
