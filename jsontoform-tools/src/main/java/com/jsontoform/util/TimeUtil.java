package com.jsontoform.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

	public final static String YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 解析时间
	 * 
	 * @param dateStr 时间字符串 格式为yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static Date parseDate(String dateStr) {
		SimpleDateFormat df = new SimpleDateFormat(YYYYMMDDHHMMSS);
		Date date;
		try {
			date = df.parse(dateStr);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
