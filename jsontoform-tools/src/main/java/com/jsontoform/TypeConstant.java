package com.jsontoform;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * @date 2020年03月05日 10:28
 * @auth zm
 */
public final class TypeConstant {

	public static final Type LIST_JSON_JE= new TypeToken<List<JsonElement>>() {
	}.getType();

	public static final Type MAP_STRING_JE = new TypeToken<Map<String, JsonElement>>() {
	}.getType();
}
