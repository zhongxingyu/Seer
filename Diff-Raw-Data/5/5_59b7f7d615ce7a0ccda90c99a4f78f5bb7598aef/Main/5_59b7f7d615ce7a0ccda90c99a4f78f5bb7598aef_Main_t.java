 /*
  * rss2blogger
 * Copyright (C) 2008 Philipp Hgelmeyer
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package com.blogspot.huegelmeyer.rss2blogger;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Properties;
 import java.util.Vector;
 
 import com.google.gdata.client.GoogleService;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import com.sun.syndication.io.FeedException;
 
 /**
  * @author phuegelm
  *
  */
 public class Main {
 
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		try {
 		    System.out.println(new File(".").getCanonicalPath());
 		}
 		catch (Exception e) {
 		    e.printStackTrace();
 		}
 		Properties properties = new Properties();
 		try {
 			properties.load(new FileInputStream("./rss2blogger.properties"));
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 
 		RssReader r= null;
 		Vector<Entry> entries; 
 		try {
 			r = new RssReader(new URL(properties.getProperty("rss_source")));
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			if(r != null)
 				{
 				    entries = r.readfeed();
 					for(Entry e: entries)
 					{
 						String blogId=properties.getProperty("b_id");
 						GoogleService myService = new GoogleService("blogger", "rss2blogger");
 						myService.setUserCredentials(properties.getProperty("b_username"), 
								properties.getProperty("b_password"));
 						BloggerWriter.createPost(myService, blogId, e);
 					}
 				}
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (FeedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (AuthenticationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ServiceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
