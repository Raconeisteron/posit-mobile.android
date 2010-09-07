/*
 * File: ResponseParser.java
 * 
 * Copyright (C) 2009 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */
package org.hfoss.posit.android.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Class to parse JSON response from the server and convert to required formats.
 * 
 * 
 */
public class ResponseParser {
	String response = null;
	public static final String TAG = "ResponseParser";

	public ResponseParser(String response) {
		this.response = response;
	}

	/**
	 * check the first character and parse it accordingly if it's a list or an
	 * object
	 * 
	 * @return
	 * @throws JSONException
	 */
	public Object parse() throws JSONException {
		Log.i(TAG, "parse() response= " + response);
		if (response.equals(null))
			throw new NullPointerException("Pass a response first");
		if (response.charAt(0) == '[') {
			return parseList();
		} else if (response.charAt(0) == '{') {
			return parseObject();
		} else {
			return null;
		}
	}

	/**
	 * Parses the response and returns the HashMap equivalent for the program to
	 * use.
	 * 
	 * @return
	 * @throws JSONException
	 */
	public List<HashMap<String, Object>> parseList() throws JSONException {
		Log.i(TAG, "parseList() response = " + response);

		if (response.equals(null))
			throw new NullPointerException("Pass a response first");
		List<HashMap<String, Object>> findsList = new ArrayList<HashMap<String, Object>>();
		JSONArray j = new JSONArray(response);
		for (int i = 0; i < j.length(); i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			JSONObject json = j.getJSONObject(i);
			findsList.add(jsonObjectToMap(json));
		}
		return findsList;
	}

	public HashMap<String, Object> parseObject() throws JSONException {
		Log.i(TAG, "parseObject() response = " + response);
		HashMap<String, String> responseMessage = new HashMap<String, String>();
		if (response.equals(null))
			throw new NullPointerException("Pass a response first");
		JSONObject json = new JSONObject(response);
		return jsonObjectToMap(json);

	}

	private HashMap<String, Object> jsonObjectToMap(JSONObject json)
			throws JSONException {
		Log.i(TAG, "jsonObjectToMap()");

		HashMap<String, Object> map = new HashMap<String, Object>();
		Iterator<String> iterKeys = json.keys();
		while (iterKeys.hasNext()) {
			String key = iterKeys.next();
			map.put(key, json.get(key));
		}
		return map;
	}
}
