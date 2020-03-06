package com.jsontoform.type;

/**
 * @date 2020年03月06日 10:50
 * @auth leesin
 */
public enum SupportType {
	XLSX, CSV;

	public static SupportType parse(String value) {
		SupportType[] elements = values();
		for (SupportType ele : elements) {
			if (ele.toString().equalsIgnoreCase(value)) {
				return ele;
			}
		}
		return null;
	}

	public static String getSupportList() {
		SupportType[] elements = values();
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (SupportType ele : elements) {
			sb.append(ele.toString().toLowerCase());
			sb.append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(']');
		return sb.toString();
	}
}
