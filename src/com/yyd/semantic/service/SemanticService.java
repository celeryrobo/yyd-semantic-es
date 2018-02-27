package com.yyd.semantic.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.nlpcn.commons.lang.tire.domain.Forest;

public class SemanticService {
	private Client client;
	private String serviceName;
	private List<String> forestNames;
	private Forest[] forests;

	public SemanticService(Client client, String serviceName, List<String> forestNames) {
		this.client = client;
		this.serviceName = serviceName;
		this.forestNames = forestNames;
		Forest[] _forests = DicLibrary.gets(forestNames);
		forests = new Forest[_forests.length + 1];
		forests[0] = DicLibrary.get();
		for (int i = 0; i < _forests.length; i++) {
			forests[i + 1] = _forests[i];
		}
	}

	public SemanticResult parse(String lang) {
		Result result = DicAnalysis.parse(lang, forests);
		SemanticResult semanticResult = convert(lang, result);
		return semanticResult;
	}

	private SemanticResult convert(String lang, Result result) {
		System.out.println(result);
		List<String> entities = new LinkedList<>();
		List<String> categories = new LinkedList<>();
		List<String> keywords = new LinkedList<>();
		for (Term term : result) {
			String natureStr = term.getNatureStr();
			String name = term.getName();
			if (natureStr.startsWith("c:")) {
				String category = natureStr.substring(2);
				if (forestNames.contains(category)) {
					categories.add(category);
					entities.add(name);
				}
			} else if ("keyword".equals(natureStr)) {
				keywords.add(name);
			}
		}
		return new SemanticResult(client, serviceName, lang, entities, categories, keywords);
	}

	public static class SemanticResult {
		private Client client;
		private String serviceName;
		private String lang;
		private List<String> entities;
		private List<String> categories;
		private List<String> keywords;

		protected SemanticResult(Client client, String serviceName, String lang, List<String> entities,
				List<String> categories, List<String> keywords) {
			this.client = client;
			this.serviceName = serviceName;
			this.lang = lang;
			this.entities = entities;
			this.categories = categories;
			this.keywords = keywords;
		}

		public List<SearchHit> search() {
			BoolQueryBuilder query = QueryBuilders.boolQuery();
			query.must(QueryBuilders.matchQuery("template", lang));
			if (!categories.isEmpty()) {
				BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
				for (String category : new HashSet<>(categories)) {
					subQuery.should(QueryBuilders.termQuery("template", category.toLowerCase()));
				}
				query.must(subQuery);
			}
			if (!keywords.isEmpty()) {
				BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
				for (String keyword : keywords) {
					subQuery.should(QueryBuilders.matchQuery("template", keyword));
				}
				query.must(subQuery);
			}
			SearchResponse searchResponse = client.prepareSearch().setIndices(serviceName).setQuery(query).setSize(6)
					.get();
			SearchHits hits = searchResponse.getHits();
			return Arrays.asList(hits.getHits());
		}

		public String getLang() {
			return lang;
		}

		public List<String> getEntities() {
			return entities;
		}

		public List<String> getCategories() {
			return categories;
		}

		public List<String> getKeywords() {
			return keywords;
		}

		@Override
		public String toString() {
			return serviceName + " {template=" + getLang() + ", categories=" + getCategories() + ", entities="
					+ getEntities() + ", keywords=" + getKeywords() + "}";
		}
	}
}
