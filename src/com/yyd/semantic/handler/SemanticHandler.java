package com.yyd.semantic.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHit;

import com.yyd.semantic.http.error.BadRequestError;
import com.yyd.semantic.http.handler.RequestHandler;
import com.yyd.semantic.http.request.IRequest;
import com.yyd.semantic.http.response.IResponse;
import com.yyd.semantic.plugin.Configration;
import com.yyd.semantic.service.SemanticService;
import com.yyd.semantic.service.SemanticService.SemanticResult;

public class SemanticHandler extends RequestHandler {
	@Override
	public void get(IRequest request, IResponse response) {
		String userIdentify = request.getParamter("userIdentify");
		String lang = request.getParamter("lang");
		if (lang == null || userIdentify == null) {
			throw new BadRequestError();
		}
		Map<String, Object> slots = new HashMap<>();
		Map<String, Object> semantic = new HashMap<>();
		Map<String, Object> result = new HashMap<>();
		result.put("slots", slots);
		result.put("semantic", semantic);
		List<SearchHit> searchResults = new ArrayList<>();
		Map<String, List<String>> params = new HashMap<>();
		try (Client client = getClient()) {
			Map<String, List<String>> WORDS = Configration.WORDS;
			for (Map.Entry<String, List<String>> entry : WORDS.entrySet()) {
				System.out.println("\nCut Word Service : " + entry.getKey() + " Lang : " + lang);
				SemanticService semanticService = new SemanticService(client, entry.getKey(), entry.getValue());
				SemanticResult semanticResult = semanticService.parse(lang);
				System.out.println(semanticResult);
				searchResults.addAll(semanticResult.search());
				params.put(entry.getKey(), semanticResult.getEntities());
			}
		}
		Collections.sort(searchResults, (e1, e2) -> {
			return (int) (e2.getScore() * 1000) - (int) (e1.getScore() * 1000);
		});
		searchResults.forEach((e) -> {
			System.out.println(e.getSourceAsString() + "   " + e.getScore());
		});
		System.out.println("=============================");
		for (SearchHit searchHit : searchResults) {
			if (buildResult(result, searchHit, params)) {
				break;
			}
		}
		try {
			String rs = XContentFactory.jsonBuilder().map(result).string();
			response.setContent(rs);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private boolean buildResult(Map<String, Object> result, SearchHit searchHit, Map<String, List<String>> kvs) {
		System.out.println(searchHit.getSourceAsString());
		System.out.println(kvs);
		Map<String, Object> slots = (Map<String, Object>) result.get("slots");
		Map<String, Object> semantic = (Map<String, Object>) result.get("semantic");
		List<String> entities = kvs.get(searchHit.getIndex());
		semantic.put("service", searchHit.getIndex());
		Map<String, Object> source = searchHit.getSourceAsMap();
		String paramStr = (String) source.get("params");
		String[] params = paramStr == null || paramStr.isEmpty() ? new String[0] : paramStr.split(",");
		if (params.length == entities.size()) {
			semantic.put("template", source.get("orgTemplate"));
			semantic.put("intent", source.get("intent"));
			for (int i = 0; i < params.length; i++) {
				slots.put(params[i], entities.get(i));
			}
			result.put("success", true);
			return true;
		}
		result.put("success", false);
		return false;
	}
}
