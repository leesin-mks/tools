package com.jsontoform.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

public class HashUtil {
	public static String md5(String string) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(string.getBytes(Charset.forName("UTF8")));
			byte[] resultByte = messageDigest.digest();
			String result = new String(Hex.encodeHex(resultByte));
			return result;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
}
