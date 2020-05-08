 /*
 	Adventure App - Allows you to create an Adventure Book, or Download
  	books from other authors.
     Copyright (C) Fall 2013 Team 5 CMPUT 301 University of Alberta
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.uofa.adventure_app.controller.http;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.UUID;
 
 import com.google.gson.Gson;
 import com.uofa.adventure_app.enums.HttpRequestType;
 import com.uofa.adventure_app.model.Story;
 
  public class HttpObjectStory {
 	
 	private static final String commonUrlString = "http://cmput301.softwareprocess.es:8080/cmput301f13t05/story/";
 
 
 	public HttpObject fetchAll() {
 		HttpObject obj = null;
 		String searchQuery = "{  \"fields\" : [\"id\", \"title\", \"users\"]}";
 		try {
 			obj = new HttpObject(HttpRequestType.POST,searchQuery , new URL(commonUrlString + "_search?pretty=1&size=1000000"));
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return obj;
 	}
 	
 	/**
 	 * Returns a story with a given id in JSON
 	 * 
 	 * @param id
 	 * @return
 	 */
 
 	// TODO: implement
 	public HttpObject fetch(UUID id) {
 		// This should search all Fields
 		HttpObject obj = null;
 		//String searchQuery = "{\"query\" : {\"term\" : {\"_id\" : \"" + id.toString() + "\"}}}";
 		//searchQuery = "";
 		//String searchQuery = "{ \"query\": { \"match_all\": {}}}";
 		try {
 			obj = new HttpObject(HttpRequestType.POST, "" , new URL(commonUrlString+ "_search?q=_id:" + id.toString()));
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return obj;
 	}
 
 	/**
 	 * Publishes a Story to the Internet!
 	 * 
 	 * @param story
 	 * @return
 	 */
 	public HttpObject publishObject(Story story) {
 		Gson gson = new Gson();
 		HttpObject obj = null;
 		try {
 			obj = new HttpObject(HttpRequestType.POST, gson.toJson(story), new URL(commonUrlString + story.id()));
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return obj;
 	}
 
 	/**
 	 * Searches the Server for the given key
 	 * 
 	 * @param searchKey
 	 * @return
 	 */
 
 	public HttpObject searchObject(String searchKey) {
 		
 		// This should search all Fields
 		String searchQuery = "{  \"fields\" : [\"id\", \"title\", \"users\"],\"query\": { \"query_string\" : { \"query\" : \"" + searchKey + "\" }}}";
 		//String searchQuery = "{\"query\" : { \"query_string\" : { \"query\" : \"" + searchKey + "\" }}}";
 		HttpObject obj = null;
 		//String searchQuery = "{ \"query\": { \"match_all\": {}}}";
 		try {
 			obj = new HttpObject(HttpRequestType.POST,searchQuery , new URL(commonUrlString + "_search?&size=1000000"));
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return obj;
 	}
 
 	public HttpObject deleteObject(String id) {
 		
 		String searchQuery = "";
 		HttpObject obj = null;
 		try {
			obj = new HttpObject(HttpRequestType.DELETE, searchQuery , new URL(commonUrlString + id + "/"));
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return obj;
 	}
 	
 }
