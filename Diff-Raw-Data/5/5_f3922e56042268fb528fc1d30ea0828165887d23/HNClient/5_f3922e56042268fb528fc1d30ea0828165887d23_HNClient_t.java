 package com.oguzdev.hnclient;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.oguzdev.hnclient.utils.BadStatusException;
 
 
 public class HNClient 
 {
 	private String nextLink;
 	public HNClient()
 	{
 		
 	}
 	
 	// Homepage Stuff
 	public ArrayList<NewsItem> getNewsIndex() throws IOException, BadStatusException
 	{
 		// parse homepage
 		News news = new News();
 		news.download();
 		news.parse();
 		nextLink = news.getNext();
 		return news.getNewsList();
 	}
 	public ArrayList<NewsItem> getNewsPage(String fnLink) throws IOException, BadStatusException
 	{
 		// parse some news page
 		News news = new News(fnLink);
 		news.download();
 		news.parse();
 		nextLink = news.getNext();
 		return news.getNewsList();
 	}
 	public ArrayList<NewsItem> getNewest() throws IOException, BadStatusException
 	{
		// parse newest
 		News news = new News(Urls.newPage);
 		news.download();
 		news.parse();
 		nextLink = news.getNext();
 		return news.getNewsList();
 	}
 	public ArrayList<NewsItem> getAsk() throws IOException, BadStatusException
 	{
		// parse ask hn
 		News news = new News(Urls.askPage);
 		news.download();
 		news.parse();
 		nextLink = news.getNext();
 		return news.getNewsList();
 	}
 	
 	public String getNextLink()
 	{
 		return nextLink;
 	}
 	
 	// Comments Stuff
 	public ArrayList<CommentItem> getComments(String itemLink) throws IOException, BadStatusException
 	{
 		Comments comments = new Comments(itemLink);
 		comments.download();
 		comments.parse();
 		return comments.getComments();
 	}
 }
