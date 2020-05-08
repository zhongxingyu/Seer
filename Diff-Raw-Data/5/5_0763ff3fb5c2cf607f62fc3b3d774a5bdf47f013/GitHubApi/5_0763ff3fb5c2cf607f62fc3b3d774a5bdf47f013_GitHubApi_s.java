 package com.github.nyao.gwtgithub.client;
 
 import com.github.nyao.gwtgithub.client.models.Issues;
 import com.github.nyao.gwtgithub.client.models.Repositories;
 import com.github.nyao.gwtgithub.client.models.Repository;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.jsonp.client.JsonpRequestBuilder;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class GitHubApi {
 
 	private String accessToken = null;
 	
 	public void setAuthorization(String accessToken) {
 		this.accessToken = accessToken;
 	}
 	
 	private static final String BASE_URL = "https://api.github.com/";
 
 	public void getMyRepository(AsyncCallback<Repositories> callback) {
 		String url = addAutorization(BASE_URL + "user/repos");
 		GWT.log(url);
 		JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
 		jsonp.requestObject(url, callback);
 	}
 	
 	public void getRepositories(String user, AsyncCallback<Repositories> callback) {
 		String url = BASE_URL + "users/" + user + "/repos";
         GWT.log(url);
 		JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
 		jsonp.requestObject(url, callback);
 	}
     
     public void getIssues(String user, String repository, AsyncCallback<Issues> callback) {
         String url = BASE_URL + "repos/" + user + "/" + repository + "/issues";
         getIssues(url, callback);
     }
     
     public void getIssues(Repository repository, AsyncCallback<Issues> callback) {
         getIssues(repository.getUrl() + "/issues", callback);
     }
     
     protected void getIssues(String url, AsyncCallback<Issues> callback) {
         GWT.log(url);
         JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
         jsonp.requestObject(url, callback);
     }
 	
 	private String addAutorization(String url) {
 		String prefix = "?";
 		if(url.contains("?")) {
 			prefix = "&";
 		}
 		
		url += prefix;

 		if(accessToken != null) {
			url += "access_token=" + accessToken;
 		}
 		return url;
 	}
 }
