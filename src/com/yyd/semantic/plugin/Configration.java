package com.yyd.semantic.plugin;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.env.Environment;

public class Configration {

	@Inject
	public Configration(Environment env) {
		System.out.println("init env ...");
		System.out.println(env.settings());
		System.out.println(env.pluginsFile());
	}
}
