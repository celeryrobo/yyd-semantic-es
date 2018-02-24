package com.yyd.semantic.service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.recognition.impl.UserDicNatureRecognition;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.nlpcn.commons.lang.tire.domain.Forest;

public class SemanticService {
	private Client client;
	private String serviceName;
	private Forest[] forests;

	public SemanticService(Client client, String serviceName, List<String> forestNames) {
		this.client = client;
		this.serviceName = serviceName;
		Forest[] _forests = DicLibrary.gets(forestNames);
		forests = new Forest[_forests.length + 1];
		forests[0] = DicLibrary.get();
		for (int i = 0; i < _forests.length; i++) {
			forests[i + 1] = _forests[i];
		}
	}

	public SemanticResult parse(String lang) {
		Result result = IndexAnalysis.parse(lang, forests);
		new UserDicNatureRecognition(forests).recognition(result);
		SemanticResult semanticResult = convert(lang, result);
		return semanticResult;
	}

	private SemanticResult convert(String lang, Result result) {
		System.out.println(result);
		StringBuilder sb = new StringBuilder();
		List<String> entities = new LinkedList<>();
		int fromIndex = 0, count = 0;
		for (int i = 0; i < result.size(); i++) {
			Term term = result.get(i);
			int pos = lang.indexOf(term.getRealName(), fromIndex) + term.getRealName().length();
			if (pos > fromIndex) {
				String natureStr = term.getNatureStr();
				if (natureStr.startsWith("c:")) {
					sb.append("{").append(natureStr.substring(2)).append("}");
					count += 1;
					entities.add(term.getRealName());
				} else {
					sb.append(term.getRealName());
				}
				fromIndex = pos;
			}
		}
		return new SemanticResult(client, serviceName, sb.toString(), count, entities);
	}

	public static class SemanticResult {
		private Client client;
		private String serviceName;
		private String template;
		private int entitiesCount;
		private List<String> entities;

		public SemanticResult(Client client, String serviceName, String template, int entitiesCount,
				List<String> entities) {
			this.client = client;
			this.serviceName = serviceName;
			this.template = template;
			this.entitiesCount = entitiesCount;
			this.entities = entities;
		}

		public List<SearchHit> search() {
			SearchResponse searchResponse = client.prepareSearch().setIndices(serviceName)
					.setQuery(QueryBuilders.matchQuery("template", template)).setSize(6).get();
			SearchHits hits = searchResponse.getHits();
			return Arrays.asList(hits.getHits());
		}

		public String getTemplate() {
			return template;
		}

		public int getEntitiesCount() {
			return entitiesCount;
		}

		public List<String> getEntities() {
			return entities;
		}

		@Override
		public String toString() {
			return serviceName + " {template=" + getTemplate() + ", entitiesCount=" + getEntitiesCount() + ", entities="
					+ getEntities() + "}";
		}
	}
}
