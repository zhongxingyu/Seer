 package com.gu.upload.proxy;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

 @SuppressWarnings("serial")
 public class ProxyServlet extends HttpServlet {
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, IOException {
 		
 		String url = request.getParameter("url");
 		
		HTTPResponse sourceResponse = URLFetchServiceFactory.getURLFetchService().fetch(new URL(url));
 		
		byte[] contents = sourceResponse.getContent();
 		
		for (HTTPHeader header : sourceResponse.getHeaders()) {
			String name = header.getName().toLowerCase();
			if (name.equals("content-type")) {
				response.setHeader("Content-Type", header.getValue());
			}
 		}
		
		response.setHeader("Cache-Control", "max-age=86000");
		
		OutputStream out = new BufferedOutputStream(response.getOutputStream());
		out.write(contents);
 		out.flush();
 	}
 }
