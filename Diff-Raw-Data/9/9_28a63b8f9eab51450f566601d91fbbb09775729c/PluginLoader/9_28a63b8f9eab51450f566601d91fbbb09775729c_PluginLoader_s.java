 package org.bridgejs.android.phonebridge.library.pluginmanager;
 
 
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.util.Log;
 import android.webkit.WebView;
 
 public class PluginLoader {
 
 	private PluginRequests requests;
 
 	private WebView webView;
 
 	private PluginManager pluginManager;
 	
 	public PluginLoader(PluginManager pluginManager, WebView webView, PluginRequests requests){
 		this.webView = webView;
 		this.requests = requests;
 		this.pluginManager = pluginManager;
 	}
 
 	public void loadUrl(final String url){
 		final class ContentLoaderTask extends AsyncTask<String, Void, String> {
 			@Override
 			protected String doInBackground(String... strings) {
 				requests.setProgressBar(10);
 				final String content = getContent(url);
 				return content;
 			}
 			
 			private void safelyLoadDataAndUpdateProgress(String content) {
 				try {
 					requests.setProgressBar(50);
					webView.loadDataWithBaseURL(url, content, "text/html", "utf-8", null);
 				}
 				catch (Exception e) {
 					Log.e("Exception", "Safely caught exception");	
 				}
 			}
 			
 			@Override
 			protected void onPostExecute(String content) {
 				
 				safelyLoadDataAndUpdateProgress(content);
 			}
 		}
 		ContentLoaderTask contentLoaderTask = new ContentLoaderTask();
 		contentLoaderTask.execute((String[])null);
 	}
 
 	public String getContent(String url){
 		String content = requests.getUrlData(url);
 		content = injectJSAtBeginning(content, pluginManager.getPluginsJS());
 //		System.out.println(content);
 		return content;
 	}
 
 	public String getBaseUrl(String url){
 		return url.substring(0,url.lastIndexOf("/")+1);
 	}
 
 	private String injectJSAtBeginning(String html, String js){
 		String script = "<script type=\"text/javascript\"> " + js + " </script>";
 		return script + html;
 	}
 
 }
