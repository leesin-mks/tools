package com.jsontoform.util;

import java.util.Objects;

/**
 * @date 2020年03月05日 18:44
 * @auth zm
 */
public final class StringUtil {

	/**
	 * 获取文件前缀 1.txt return 1,1.2.txt return 1.2
	 * 
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFilePrefix(String fileName) {
		Objects.requireNonNull(fileName);
		String[] split = fileName.split("\\.");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < split.length - 1; i++) {
			sb.append(split[i]);
		}
		return sb.toString();
	}

	/**
	 * 获取文件后缀 1.txt return txt,txt return ""
	 *
	 *
	 * @param fileName
	 * @return
	 */
	public static String getFileSuffix(String fileName) {
		Objects.requireNonNull(fileName);
		String[] split = fileName.split("\\.");
		return split.length > 1 ? split[1] : "";
	}

	/**
	 * 判断字符串是否为空。
	 *
	 * @param src
	 * @return
	 */
	public static boolean isNullOrEmpty(String src) {
		if (src == null || src.isEmpty() || src.trim().length() <= 0) {
			return true;
		}

		return false;
	}

}
