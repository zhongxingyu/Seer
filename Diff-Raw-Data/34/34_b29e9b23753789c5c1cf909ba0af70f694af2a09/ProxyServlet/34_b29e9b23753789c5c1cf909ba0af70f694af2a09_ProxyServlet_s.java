 package com.gu.upload.proxy;
 
import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
import java.net.URLConnection;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 public class ProxyServlet extends HttpServlet {
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, IOException {
 		
 		String url = request.getParameter("url");
 		
		URLConnection connection = new URL(url).openConnection();
		response.setHeader("Content-Type", connection.getContentType());
		response.setHeader("Cache-Control", "max-age=86000");
 		
		InputStream in = new BufferedInputStream(connection.getInputStream());
		OutputStream out = new BufferedOutputStream(response.getOutputStream());
		int x;
 		
		while ((x = in.read()) != -1) {
			out.write(x);
 		}
 		out.flush();
 	}
 }
