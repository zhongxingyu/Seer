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
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
 public class RequestInfoLoggingFilter implements Filter {
 	private String loggerName = getClass().getName();
 	private Level logLevel = Level.INFO;
 
 	public void init(FilterConfig config) throws ServletException {
 		String temp = config.getInitParameter("LoggerName");
 		if (temp != null) {
 			this.loggerName = temp;
 		}
 		temp = config.getInitParameter("LogLevel");
 		if (temp != null) {
 			this.logLevel = Level.parse(temp);
 		}
 	}
 
 	public void destroy() {
 		// Nothing to do to destruct
 	}
 
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		Logger logger = Logger.getLogger(this.loggerName);
 		if (logger.isLoggable(this.logLevel)) {
 			if (request instanceof HttpServletRequest) {
 				HttpServletRequest temp = (HttpServletRequest)request;
 				logger.log(this.logLevel, "HttpServletRequest.getPathInfo() = " + temp.getPathInfo());
 				logger.log(this.logLevel, "HttpServletRequest.getRequestURI() = " + temp.getRequestURI());
 				logger.log(this.logLevel, "HttpServletRequest.getRequestURL() = " + temp.getRequestURL());
 			}
 		}
 	}
 }
