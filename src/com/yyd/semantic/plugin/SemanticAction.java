package com.yyd.semantic.plugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestRequest.Method;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;

import com.yyd.semantic.handler.SemanticHandler;
import com.yyd.semantic.http.error.HTTPError;
import com.yyd.semantic.http.error.InternalServerError;
import com.yyd.semantic.http.error.NotFoundError;
import com.yyd.semantic.http.handler.AbstractHandler;
import com.yyd.semantic.http.request.Request;
import com.yyd.semantic.http.response.Response;

public class SemanticAction extends BaseRestHandler {
	private static Map<String, AbstractHandler> handlers;

	static {
		handlers = new HashMap<>();
		handlers.put("", new SemanticHandler());
	}

	protected SemanticAction(Settings settings, RestController restController) {
		super(settings);
		restController.registerHandler(Method.GET, "/_semantic/", this);
		restController.registerHandler(Method.GET, "/_semantic/{action}", this);
	}

	@Override
	public String getName() {
		return "yyd-semantic-action";
	}

	@Override
	protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
		Response response = new Response(RestStatus.OK);
		try {
			String action = request.param("action");
			AbstractHandler handler = handlers.get(action == null ? "" : action);
			if (handler == null) {
				throw new NotFoundError();
			}
			if (null == handler.getClient()) {
				handler.setClient(client);
			}
			handler.execute(new Request(request), response);
		} catch (Throwable e) {
			HTTPError error = null;
			if (e instanceof HTTPError) {
				error = (HTTPError) e;
			} else {
				error = new InternalServerError();
			}
			response.setStatus(error.getStatus());
			response.setContent(error.getMessage());
			e.printStackTrace();
		}
		RestResponse restResponse = response.buildResponse();
		return channel -> {
			channel.sendResponse(restResponse);
		};
	}

	@Override
	protected Set<String> responseParams() {
		Set<String> params = new HashSet<>(super.responseParams());
		params.addAll(Arrays.asList("lang", "userIdentify"));
		return params;
	}
}
