 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
  *
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jbasics.jee.servlet;
 
 import java.io.IOException;
 import java.util.regex.Pattern;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
 
 public class RootRedirectSuppressingFilter implements Filter {
 	private Pattern pattern;
 
 	public void init(FilterConfig config) throws ServletException {
 		String temp = config.getInitParameter("match");
 		if (temp != null) {
 			this.pattern = Pattern.compile(temp);
 		}
 	}
 
 	public void destroy() {
 		// TODO Auto-generated method stub
 	}
 
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest httpRequest = (HttpServletRequest) request;
 		if (httpRequest.getPathInfo() == null || "".equals(httpRequest.getPathInfo())
 		        || ("/".equals(httpRequest.getPathInfo()) && !httpRequest.getRequestURI().endsWith("/"))) {
 			// Problem may occur when path info already has a / and request does not. than both gets it attached even path info
 			// should not get it attached!
 			chain.doFilter(new RequestAppendWrapper(httpRequest, "/"), response);
 		} else {
 			chain.doFilter(request, response);
 		}
 	}
 
 }
