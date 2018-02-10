package com.yyd.semantic.http.handler;

import org.elasticsearch.client.Client;
import org.elasticsearch.rest.RestStatus;

import com.yyd.semantic.http.error.HTTPError;
import com.yyd.semantic.http.request.IRequest;
import com.yyd.semantic.http.response.IResponse;

public abstract class AbstractHandler implements IHandler {
	private Client client;
	
	@Override
	public void execute(IRequest request, IResponse response) {
		switch (request.getMethod()) {
		case GET:
			get(request, response);
			break;
		case POST:
			post(request, response);
			break;
		case PUT:
			put(request, response);
			break;
		case DELETE:
			delete(request, response);
			break;
		default:
			throw new HTTPError("Method Not Implemented", RestStatus.NOT_IMPLEMENTED);
		}

	}
	
	public abstract void get(IRequest request, IResponse response);

	public abstract void post(IRequest request, IResponse response);

	public abstract void put(IRequest request, IResponse response);

	public abstract void delete(IRequest request, IResponse response);

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
}
