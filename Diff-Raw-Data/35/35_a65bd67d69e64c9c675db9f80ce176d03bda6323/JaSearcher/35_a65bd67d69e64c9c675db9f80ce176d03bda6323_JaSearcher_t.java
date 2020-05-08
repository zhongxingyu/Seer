 package com.missingstatement.yeahapp;
 
 import com.missingstatement.yeahapp.utils.Keys;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class JaSearcher
 {
 	private final String BASE_URL = "http://ja.is";
	private final String URL_STRING = BASE_URL + "/?q=";
 	private String mNextUrl;
 
 	public JaSearcher()
 	{
 		mNextUrl = null;
 	}
 	
 	public boolean hasNext()
 	{
 		return mNextUrl != null;
 	}
 	
 	public ArrayList<HashMap<String, ArrayList<String>>> getNext()
 	{
 		ArrayList<HashMap<String, ArrayList<String>>> results = fetchResult(mNextUrl);
 		int tryCount = 0;
 		//TODO: move magic number: 5 to appropriate location
 		while(results == null && tryCount < 5)
 		{
 			//assuming the error was a connection error
 			//try again
 			results = fetchResult(mNextUrl);
 		}
 		return results;
 	}
 
     public String getNextUrl()
     {
         return mNextUrl;
     }
 
 	public ArrayList<HashMap<String, ArrayList<String>>> search(String queryString)
     {
         String url = getQueryUrl(queryString);
 
 		return fetchResult(url);
     }
 
     private String getQueryUrl(String queryString)
     {
         return URL_STRING + queryString.replaceAll(" ", "+");
     }
 
 	private ArrayList<HashMap<String, ArrayList<String>>> fetchResult(String url)
 	{
 		ArrayList<HashMap<String, ArrayList<String>>> results = new ArrayList<HashMap<String, ArrayList<String>>>();
     	try
         {
 	    	Document doc = Jsoup.connect(url).get();
	    	Elements infos = doc.select("div.result");	
 	    	Elements pagingLinks = doc.select("div.paging").select("a");
 	    	for(Element info : infos)
 	    	{
 	    		HashMap<String, ArrayList<String>> res = new HashMap<String, ArrayList<String>>();
 	    		ArrayList<String> names = new ArrayList<String>();
 	    		ArrayList<String> address = new ArrayList<String>();
 	    		ArrayList<String> phoneNrs = new ArrayList<String>();
 	    		ArrayList<String> titles = new ArrayList<String>();
 	    		
	    		Elements nm = info.select("span.cut");
	    		if(nm.size() == 0)
	    		{
	    			//Businesses have have links not spans
	    			nm = info.select("a.cut");
	    		}
	    		
 	    		for(Element name : nm)
 	    		{
	    			names.add(name.text());
	    			if (name.children().size() > 0)
 	    			{
	    				Element theTitle = name.child(0);
	    				if(theTitle != null)
	    				{
	    					titles.add(theTitle.text());
	    				}
	    			}
 	    		}
 	    		
	    		Elements adrs = info.select("a.addressinfo");
 	    		for(Element adr : adrs)
 	    		{
 	    			address.add(adr.text());
 	    		}
 	    		
	    		Elements pNrs = info.select("a.phone");
 	    		for(Element phoneNr : pNrs)
 	    		{
 	    			phoneNrs.add(phoneNr.text());
 	    		}
 	    		res.put(Keys.KEY_NAMES, names);
 	    		res.put(Keys.KEY_ADDRESSES, address);
 	    		res.put(Keys.KEY_PHONE_NUMBERS, phoneNrs);
 	    		res.put(Keys.KEY_TITLES, titles);
 	    		results.add(res);
 	    	}
 	    	if(!pagingLinks.isEmpty())
 	    	{
 	    		Element lastUrl = pagingLinks.last();
 	    		String linkText = lastUrl.text().replaceAll("\\s","").toLowerCase();
 	    		if(linkText.equals(Keys.KEY_MORE_RESULTS))
 	    		{
 	    			mNextUrl = BASE_URL + lastUrl.attr("href").replace(" ", "+");
 	    		}
 	    		else
 	    		{
 	    			mNextUrl = null;
 	    		}
 	    	}
 	    	else
 	    	{
 	    		mNextUrl = null;
 	    	}
     	}
     	catch (Exception e)
     	{
     		e.printStackTrace();
     	}
     	finally
     	{
     		return results;
     	}
 	}
 }
