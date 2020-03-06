package com.jsontoform.tools.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalConfigManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(GlobalConfigManager.class);

	public static final String FILE_PATH = "file-path";
	public static final String OUTPUT_PATH = "output-path";
	public static final String FILE_FORMAT = "file-format";
	public static final String CSV_CHARACTER_SET = "csv-character-set";

	private String jsonFilePath;
	private String outPutPath;
	private String fileFormat;
	private String csvCharacterSet;

	private GlobalConfigManager() {
	}

	/**
	 * 单例加载器
	 */
	private static class LazyHolder {
		private static final GlobalConfigManager INSTANCE = new GlobalConfigManager();
	}

	/**
	 * 获取实例
	 *
	 * @return
	 */
	public static GlobalConfigManager getInstance() {
		return LazyHolder.INSTANCE;
	}

	public boolean init(String configPath) {
		Properties properties = new Properties();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			File fileTemp = new File("");
			System.out.println("path: " + fileTemp.getCanonicalPath());
			fr = new FileReader(configPath);
			br = new BufferedReader(fr);
			properties.load(br);
			jsonFilePath = properties.getProperty(FILE_PATH);
			outPutPath = properties.getProperty(OUTPUT_PATH);
			fileFormat = properties.getProperty(FILE_FORMAT);
			csvCharacterSet = properties.getProperty(CSV_CHARACTER_SET);
			if (csvCharacterSet == null) {
				csvCharacterSet = StandardCharsets.UTF_8.name();
			}
			return true;
		} catch (FileNotFoundException e) {
			LOGGER.error("Init config file error", e);
		} catch (IOException e) {
			LOGGER.error("Init config file error", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					LOGGER.error("Init config file error", e);
				}
			}
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					LOGGER.error("Init config file error", e);
				}
			}
		}
		return false;
	}

	public String getJsonFilePath() {
		return jsonFilePath;
	}

	public String getOutPutPath() {
		return outPutPath;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public String getCsvCharacterSet() {
		return csvCharacterSet;
	}
}
