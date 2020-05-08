 /**
  * Copyright (c) 2011, Mikael Svahn, Softhouse Consulting AB
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so:
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package se.softhouse.garden.orchid.spring.utils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.jsp.JspException;
 
 /**
  * @author Mikael Svahn
  * 
  */
 public class LinkUtil {
 
 	static final String URL_TYPE_ABSOLUTE = "://";
 
 	/**
 	 * Sets the value of the URL
 	 */
 	public static UrlType getType(String url) {
 		if (url.contains(URL_TYPE_ABSOLUTE)) {
 			return UrlType.ABSOLUTE;
 		} else if (url.startsWith("/")) {
 			return UrlType.CONTEXT_RELATIVE;
 		} else {
 			return UrlType.RELATIVE;
 		}
 	}
 
 	/**
 	 * Build the URL for the tag from the tag attributes and parameters.
 	 * 
 	 * @param request2
 	 * 
 	 * @param response
 	 * 
 	 * @return the URL value as a String
 	 * @throws JspException
 	 */
 	public static String createUrl(String link, HttpServletRequest request, HttpServletResponse response) {
 		StringBuilder url = new StringBuilder();
 		UrlType type = getType(link);
 		if (type == UrlType.CONTEXT_RELATIVE) {
 			// add application context to url
 			url.append(request.getContextPath());
 		}
 		if (type != UrlType.RELATIVE && type != UrlType.ABSOLUTE && !link.startsWith("/")) {
 			url.append("/");
 		}
 
 		url.append(link);
 
 		String urlStr = url.toString();
 		if (type != UrlType.ABSOLUTE && response != null) {
 			// Add the session identifier if needed
 			// (Do not embed the session identifier in a remote link!)
			urlStr = response.encodeURL(urlStr);
 		}
 
 		return urlStr;
 	}
 }
 
 /**
  * Internal enum that classifies URLs by type.
  */
 enum UrlType {
 	CONTEXT_RELATIVE, RELATIVE, ABSOLUTE
 }
