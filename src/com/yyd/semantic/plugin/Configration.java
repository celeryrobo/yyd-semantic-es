package com.yyd.semantic.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ansj.library.DicLibrary;
import org.ansj.util.MyStaticValue;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

public class Configration {

	private final static String CONFIG_DIRNAME = "yyd-semantic-es";
	private final static String CONFIG_FILENAME = "yyd.cfg.yml";
	
	public final static Map<String, List<String>> WORDS = new HashMap<>();

	@Inject
	public Configration(Environment env) {
		System.out.println("init env ...");
		Path configPath = env.configFile().resolve(CONFIG_DIRNAME).resolve(CONFIG_FILENAME);
		if (!Files.exists(configPath)) {
			throw new RuntimeException("config " + CONFIG_DIRNAME + "/" + CONFIG_FILENAME + " not exsit ...");
		}
		Settings.Builder builder = Settings.builder();
		try {
			builder.loadFromPath(configPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Settings settings = builder.build();
		Settings envs = settings.getAsSettings("env");
		if(envs != null) {
			for (String key : envs.keySet()) {
				System.out.println(key);
				MyStaticValue.ENV.put(key, envs.get(key));
			}
		}
		Settings words = settings.getAsSettings("word");
		if (words != null) {
			Map<String, Settings> groups = words.getAsGroups(true);
			for (Map.Entry<String, Settings> entry : groups.entrySet()) {
				System.out.println(entry.getKey());
				initWords(entry.getKey(), entry.getValue().getAsGroups());
			}
		}
	}

	private void initWords(String service, Map<String, Settings> words) {
		List<String> list = new LinkedList<>();
		WORDS.put(service, list);
		for (Map.Entry<String, Settings> entry : words.entrySet()) {
			System.out.println("-- " + entry.getKey());
			String dicFilename = entry.getValue().get("dic");
			System.out.println("---- doc:" + dicFilename);
			initWord(entry.getKey(), dicFilename);
			list.add(entry.getKey());
		}
	}

	private void initWord(String key, String dicFilename) {
		System.out.println("init " + key + " dic " + dicFilename);
		DicLibrary.put(key, dicFilename);
	}
}
