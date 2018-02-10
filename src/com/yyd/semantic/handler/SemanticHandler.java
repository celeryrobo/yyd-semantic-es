package com.yyd.semantic.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse.AnalyzeToken;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.yyd.semantic.http.error.BadRequestError;
import com.yyd.semantic.http.handler.RequestHandler;
import com.yyd.semantic.http.request.IRequest;
import com.yyd.semantic.http.response.IResponse;

public class SemanticHandler extends RequestHandler {
	@Override
	public void get(IRequest request, IResponse response) {
		String userIdentify = request.getParamter("userIdentify");
		String lang = request.getParamter("lang");
		if (lang == null || userIdentify == null) {
			throw new BadRequestError();
		}
		String[] indexs = { "song", "story", "poetry" };
		Map<String, Object> slots = new HashMap<>();
		Map<String, Object> semantic = new HashMap<>();
		Map<String, Object> result = new HashMap<>();
		result.put("slots", slots);
		result.put("semantic", semantic);
		try (Client client = getClient()) {
			for (String index : indexs) {
				slots.clear();
				semantic.clear();
				result.put("words", "");
				System.out.println("\nCut Word Index : " + index + " Lang : " + lang);
				AnalyzeResponse analyzeResponse = client.admin().indices().prepareAnalyze(index, lang)
						.setAnalyzer(index + "_ansj").get();
				ActionListener<AnalyzeResponse> analyzeAction = new AnalyzeActionListener(client, result, index);
				try {
					analyzeAction.onResponse(analyzeResponse);
					break;
				} catch (Exception e) {
					continue;
				}
			}
		}
		try {
			String rs = XContentFactory.jsonBuilder().map(result).string();
			response.setContent(rs);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final class AnalyzeActionListener implements ActionListener<AnalyzeResponse> {
		private Client client;
		private Map<String, Object> result;
		private String indexName;

		public AnalyzeActionListener(Client client, Map<String, Object> result, String indexName) {
			this.client = client;
			this.result = result;
			this.indexName = indexName;
		}

		@Override
		public void onResponse(AnalyzeResponse response) {
			StringBuilder langs = new StringBuilder();
			StringBuilder words = new StringBuilder();
			List<String> entities = new LinkedList<>();
			for (AnalyzeToken e : response) {
				String term = e.getTerm();
				String type = e.getType();
				words.append(term).append("/").append(type).append(" ");
				if (type.startsWith("c:")) {
					String category = type.substring(2);
					entities.add(term);
					langs.append("{").append(category).append("}");
				} else {
					langs.append(term);
				}
			}
			result.put("words", words.toString().trim());
			System.out.println(words);
			System.out.println(langs);
			SearchResponse searchResponse = client.prepareSearch().setQuery(QueryBuilders.matchQuery("template", langs))
					.setIndices(indexName).setSize(10).get();
			ActionListener<SearchResponse> searchAction = new SearchActionListener(result, entities);
			try {
				searchAction.onResponse(searchResponse);
			} catch (Exception e) {
				searchAction.onFailure(e);
			}
		}

		@Override
		public void onFailure(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static final class SearchActionListener implements ActionListener<SearchResponse> {
		private Map<String, Object> result;
		private List<String> entities;

		public SearchActionListener(Map<String, Object> result, List<String> entities) {
			this.result = result;
			this.entities = entities;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onResponse(SearchResponse response) {
			SearchHits hits = response.getHits();
			hits.forEach((e) -> {
				System.out.println(e.getSourceAsString() + " : " + e.getScore());
			});
			if (hits.totalHits == 0) {
				throw new RuntimeException();
			}
			Map<String, Object> semantic = (Map<String, Object>) result.get("semantic");
			for (SearchHit hit : hits) {
				semantic.put("service", hit.getIndex());
				Map<String, Object> source = hit.getSourceAsMap();
				String paramStr = (String) source.get("params");
				String[] params = paramStr == null || paramStr.isEmpty() ? new String[0] : paramStr.split(",");
				if (params.length == entities.size()) {
					semantic.put("template", source.get("orgTemplate"));
					semantic.put("intent", source.get("intent"));
					Map<String, Object> slots = (Map<String, Object>) result.get("slots");
					for (int i = 0; i < params.length; i++) {
						slots.put(params[i], entities.get(i));
					}
					return;
				}
			}
			throw new RuntimeException();
		}

		@Override
		public void onFailure(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
