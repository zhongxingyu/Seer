 package com.bananity.controllers;
 
 
 // Bananity Classes
 import com.bananity.models.IndexModelBean;
 import com.bananity.nlp.AnalyzerModule;
 import com.bananity.util.SearchesTokenizer;
 import com.bananity.util.HashBag;
 import com.bananity.util.ResultItemComparator;
 
 // Cache
 import com.google.common.cache.Cache;
 
 // Java utils
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.ejb.EJB;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 /**
  *  This webservlet allows to insert items into the index collections (and remove it)
  *
  *  @author 	Andreu Correa Casablanca
  *  @version 	0.4
  */
 @WebServlet("/index")
 public class IndexController extends BaseController {
 
 	/**
 	 *  IndexModelBean reference
 	 */
 	@EJB
 	private IndexModelBean imB;
 
 	/**
 	 *  Handles Post requests, and calls insert or remove methods
 	 *
 	 *  @see javax.servlet.http.HttpServlet#doPost
 	 */
 	@Override
 		public void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 			try {
 				String 	method 		= request.getParameter("m");
 				String 	collName 	= request.getParameter("c");
 				String 	item 		= request.getParameter("item");
 
 				if (collName == null || collName.length() == 0 || item == null || item.length() == 0) {
 					throw new Exception( "Invalid parameters" );
 				}
 
 				if (method.equals("insert")) {
 					insertLogic(collName, item);
 				} else if (method.equals("remove")) {
 					removeLogic(collName, item);
 				} else {
 					throw new Exception("Inexistent method : m="+method);
 				}
 
 				sendResponse(request, response, HttpServletResponse.SC_OK, 0, null);
 
 			} catch (Exception e) {
 				log.warn("BAD_REQUEST from "+request.getRemoteAddr()+" with exception "+e.getMessage()+", cause: "+e.getCause()+", params: "+request.getQueryString());
 				e.printStackTrace();
 
 				sendResponse(request, response, HttpServletResponse.SC_BAD_REQUEST, 1, null);
 			}
 		}
 
 	/**
 	 *  Insert an item into the specifiec collection (and updates caches)
 	 *
 	 *  @param collName Index collection name
 	 *  @param item 	Item to be inserted in the collection
 	 */
 		private void insertLogic (String collName, String item) throws Exception {
 			imB.insert(collName, item, SearchesTokenizer.getSubTokensList(item));

 			addToCache(collName, item);
 		}
 
 	/**
 	 *  Removes an item from the specifiec collection (and updates caches)
 	 *
 	 *  @param collName Index collection name
 	 *  @param item 	Item to be inserted in the collection
 	 */
 		private void removeLogic (String collName, String item) throws Exception {
 			
 		}
 
 	/**
 	 *  Adds an item to the results cache tied to the specified collection
 	 *
 	 *  @param collName Index collection name
 	 *  @param item 	Item to be inserted in the collection
 	 */
 		private void  addToCache (String collName, String item) throws Exception {
 			Cache<String, ArrayList<String>> cache = cB.getResultCache(collName);
 			
 			if (cache == null) {
 				throw new Exception("¡Cache not foud for collection \""+collName+"\"!");
 			}
 
 			String cacheKeyBase = AnalyzerModule.normalize( item );
 
 			ArrayList<String> expandedCacheKeyBase = AnalyzerModule.getAllSubstrings(cacheKeyBase, 2);
 
 			for (String cacheKeyBaseItem : expandedCacheKeyBase) {
 				addToCacheToken (cache, item, cacheKeyBaseItem, 5);
 				addToCacheToken (cache, item, cacheKeyBaseItem, 10);
 				addToCacheToken (cache, item, cacheKeyBaseItem, 15);
 				addToCacheToken (cache, item, cacheKeyBaseItem, 20);
 			}
 		}
 
 	/**
 	 *  Adds an item to a results cache value (given with the key cacheKeyBaseItem@limit)
 	 *
 	 *  @param cache 			cache to be updated
 	 *  @param item 			item 
 	 *  @param cacheKeyBaseItem complete or partial searchTerm (as a part of cache key)
 	 *  @param limit 			limit imposed to search result size (as a part of cache key)
 	 */
 		private void addToCacheToken (Cache<String, ArrayList<String>> cache, String item, String cacheKeyBaseItem, Integer limit) throws Exception {
 			String cacheKey = new StringBuilder(cacheKeyBaseItem).append("@").append(limit.toString()).toString();
 			ArrayList<String>  cachedResult = cache.getIfPresent(cacheKey);
 
 			if (cachedResult == null) return;
 
 			String sortingTmpValue;
 			boolean addedItem;
 
 			if (cachedResult.size() < limit) {
 				cachedResult.add(item);
 				addedItem = true;
 			} else if (cachedResult.get(limit-1).compareTo(item) > 0) {
 				cachedResult.set(limit-1, item);
 				addedItem = true;
 			} else {
 				addedItem = false;
 			}
 
 			if (addedItem) {
 				for (int i=cachedResult.size()-1; i>0 && cachedResult.get(i).compareTo(cachedResult.get(i-1)) < 0; i--) {
 					sortingTmpValue = cachedResult.get(i);
 					cachedResult.set(i, cachedResult.get(i-1));
 					cachedResult.set(i-1, sortingTmpValue);
 				}
 			}
 
 			cache.put(cacheKey, cachedResult);
 		}
 }
