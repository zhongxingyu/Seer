 /*
  * Copyright 2012 Feedlr
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.chalmers.feedlr.parser;
 
 import java.io.IOException;
 import java.util.List;
 
 import com.chalmers.feedlr.model.FacebookItem;
 import com.chalmers.feedlr.model.User;
 import com.fasterxml.jackson.core.JsonParseException;
 
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.DeserializationFeature;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.ObjectReader;
 
 import android.util.Log;
 
 /**
  * Class description
  * 
  * @author Daniel Larsson
  * 
  *         A parser for Facebook responses. The parsing is done with the Jackson
  *         JSON Processor.
  */
 
 public class FacebookJSONParser {
 
 	private static ObjectMapper mapper;
 	private static ObjectReader itemReader;
 	private static ObjectReader userReader;
 
 	public FacebookJSONParser() {
 		if (mapper == null) {
 			mapper = new ObjectMapper();
 			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
 					false);
 		}
 	}
 
 	/*
 	 * Parse a JSON response for a list of statuses from Facebook's REST API
 	 * with Jackson's databinding parse method.
 	 * 
 	 * @return list containing <code>FacebookItems</code>
 	 */
 	public List<FacebookItem> parseFeed(String json) {
 		long time = System.currentTimeMillis();
 
		String data = json.substring(json.indexOf("statuses") + 18);

 		if (itemReader == null) {
 			itemReader = mapper.reader(new TypeReference<List<FacebookItem>>() {
 			});
 		}
 
 		List<FacebookItem> list = null;
 
		if (data.contains("statuses")) {
 			try {
 				list = itemReader.readValue(data);
 				Log.i(FacebookJSONParser.class.getName(),
 						"Parsed " + list.size() + " Facebook items in "
 								+ (System.currentTimeMillis() - time)
 								+ " millis.");
 			} catch (JsonParseException e) {
 				Log.e(getClass().getName(), e.getMessage());
 			} catch (JsonMappingException e) {
 				Log.e(getClass().getName(), e.getMessage());
 			} catch (IOException e) {
 				Log.e(getClass().getName(), e.getMessage());
 			}
 		}
 
 		return list;
 	}
 
 	/*
 	 * Parse a JSON response for a list of users from Facebook's REST API with
 	 * Jackson's databinding parse method. The list will be filled with
 	 * 
 	 * @return list containing <code>Users</code>
 	 */
 	public List<User> parseUsers(String json) {
 		long time = System.currentTimeMillis();
 		String data = json.substring(8);
 
 		if (userReader == null) {
 			userReader = mapper.reader(new TypeReference<List<User>>() {
 			});
 		}
 		List<User> list = null;
 
 		try {
 			list = userReader.readValue(data);
 		} catch (JsonParseException e) {
 			Log.e(getClass().getName(), e.getMessage());
 		} catch (JsonMappingException e) {
 			Log.e(getClass().getName(), e.getMessage());
 		} catch (IOException e) {
 			Log.e(getClass().getName(), e.getMessage());
 		}
 
 		Log.i(FacebookJSONParser.class.getName(), "Parsed " + list.size()
 				+ " Facebook users in " + (System.currentTimeMillis() - time)
 				+ " millis.");
 
 		return list;
 	}
 }
