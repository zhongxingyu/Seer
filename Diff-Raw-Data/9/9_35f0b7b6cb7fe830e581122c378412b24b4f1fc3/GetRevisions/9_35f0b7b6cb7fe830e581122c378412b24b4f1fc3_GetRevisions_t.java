 package edu.mit.cci.wikipedia.collector;
 
 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.logging.Logger;
 
 import com.google.appengine.api.urlfetch.HTTPHeader;
 import com.google.appengine.api.urlfetch.HTTPMethod;
 import com.google.appengine.api.urlfetch.HTTPRequest;
 import com.google.appengine.api.urlfetch.HTTPResponse;
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 
 
 public class GetRevisions {
 	/**
 	 * @param args
 	 */
 	private static final Logger log = Logger.getLogger(GetRevisions.class.getName());
 
 	
 
 	public String getArticleRevisions(String lang, String title, String _limit) {
 		String data = "";
 		int limit = Integer.parseInt(_limit);
 		Result result = new Result();
 
 		try {
 			title = title.replaceAll(" ", "_");
 			String xml = getArticleRevisionsXML(lang, title,"");
			log.info(xml);
 			XMLParseRevision parse = new XMLParseRevision(title,result,xml);
 			//parse.setUserName(userName);
 			parse.parse();
 			int count = 0;
 			while(result.hasNextId()) {
 				count++;
 				if (limit != 0 && count >= limit)
 					break;
 				String nextId = result.getNextId();
 				data += result.getResult();
 				result.clear();
 				//tmpData.clear();
 				xml = getArticleRevisionsXML(lang, title, nextId);
 				parse = new XMLParseRevision(title,result,xml);
 				parse.parse();
 				//Thread.sleep(1000);
 			}
 			data += result.getResult();
 		}catch(Exception e) {
 			e.printStackTrace();
 		}
 		return data;
 	}
 
 
 	private String getArticleRevisionsXML(String lang, String pageid, String nextId) {
 		// TODO Auto-generated method stub
 		String rvstartid = "&rvstartid=" + nextId;
 		if (nextId.equals("")) {
 			rvstartid = "";
 		}
 		String xml = "";
 
 		try {
 			//String urlStr = "http://en.wikipedia.org/w/api.php";
 			pageid = URLEncoder.encode(pageid,"UTF-8");
 			
 			String urlStr = "http://" + lang + ".wikipedia.org/w/api.php?format=xml&action=query&prop=revisions&titles="+pageid+"&rvlimit=500&rvprop=flags%7Ctimestamp%7Cuser%7Csize&rvdir=older"+rvstartid;
 			//urlStr = URLEncoder.encode(urlStr);
 			log.info(urlStr);
 			URL url = new URL(urlStr);
 			HttpURLConnection urlCon = (HttpURLConnection)url.openConnection();
 			urlCon.setRequestMethod("GET");
 			urlCon.setInstanceFollowRedirects(false);
 			/*urlCon.addRequestProperty("format", "xml");
             urlCon.addRequestProperty("action", "query");
             urlCon.addRequestProperty("prop", "revisions");
             urlCon.addRequestProperty("titles", pageid);
             urlCon.addRequestProperty("rvlimit", "500");
             urlCon.addRequestProperty("rvprop", "flags|timestamp|user|size");
             urlCon.addRequestProperty("rvdir", "newer");*/
 
 			urlCon.connect();
 			//urlCon.setRequestProperty("titles", pageid);
 
 			BufferedReader reader = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
 			//BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
 			String line;
 
 			while ((line = reader.readLine()) != null) {
 				xml += line + "\n";
 			}
 			//log.info(xml);
 			reader.close();
 
 		} catch (MalformedURLException e) {
 			// ...
 			log.info(e.getMessage());
 		} catch (IOException e) {
 			// ...
 			log.info(e.getMessage());
 		}
 		return xml;
 	}
 
 }
