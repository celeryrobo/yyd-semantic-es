package com.yyd.semantic.http.handler;

import com.yyd.semantic.http.request.IRequest;
import com.yyd.semantic.http.response.IResponse;

public interface IHandler {
	public void execute(IRequest request, IResponse response);
}
