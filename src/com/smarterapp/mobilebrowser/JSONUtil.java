//*******************************************************************************
// Educational Online Test Delivery System
// Copyright (c) 2015 American Institutes for Research
//
// Distributed under the AIR Open Source License, Version 1.0
// See accompanying file AIR-License-1_0.txt or at
// http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
//*******************************************************************************
package com.smarterapp.mobilebrowser;

import java.util.Arrays;

import org.json.JSONArray;

/**
 * Utility class for converting standard data
 * structures to json objects.
 */
public class JSONUtil {

	public static JSONArray toJSONArray(int[] array) {
		JSONArray json = new JSONArray();
		for (int data : array) {
			json.put(data);
		}
		return json;
	}

	public static <T> JSONArray toJSONArray(T[] objects) {
		return new JSONArray(Arrays.asList(objects));
	}
	
}
