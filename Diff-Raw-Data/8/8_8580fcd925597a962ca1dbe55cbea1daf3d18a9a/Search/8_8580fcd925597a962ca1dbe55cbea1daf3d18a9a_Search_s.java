 package com.bmat.ella;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 public abstract class Search extends SearchObject {
 	
 	protected Request request;
 	protected final String RESPONSE_TYPE = ".json";
 	private int RESULTS_PER_PAGE = 10;
 	protected String method;
 	protected boolean fuzzy = false;
     protected HashMap<String, String> searchTerms;
     protected Double threshold;
     
 	
 	public Search(EllaConnection ellaConnection, String collection){
 		super(ellaConnection, collection);
 		request = new Request(ellaConnection);
 	}
 
 	public JSONArray retrievePage(long pageIndex) throws ServiceException, IOException{		
 		if(this.method.indexOf("resolve") == -1){
 			long offset = 0;
 			if(pageIndex !=0)
 				offset = this.RESULTS_PER_PAGE * pageIndex;
 			this.searchTerms.put("offset", Long.toString(offset));
 		}
 		
 		JSONObject response = this.request.execute(this.method, this.collection, this.searchTerms);
 		return response != null ? (JSONArray)response.get("results") : null;
 		
 	}
 	
 	public void getNextPage(){
 		
 	}
 	
 	public void getTotalResultCount(){
 		
 	}
 	
 	
 	@SuppressWarnings("rawtypes")
	public abstract ArrayList getPage(Long pageIndex) throws Exception;
	
	
	
//	public abstract void getNextPage();
//	
	
 }
