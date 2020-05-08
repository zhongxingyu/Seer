 /*
  * This file is a component of thundr, a software library from 3wks.
  * Read more: http://www.3wks.com.au/thundr
  * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.threewks.thundr.test.mock.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletResponse;
 
 import jodd.io.StringOutputStream;
 
 import com.threewks.thundr.exception.BaseException;
 
 public class MockHttpServletResponse implements HttpServletResponse {
 	private Map<String, String> headers = new HashMap<String, String>();
 	private String characterEncoding = "utf-8";
 	private String contentType = null;
 	private StringOutputStream sos;
 	private int contentLength;
 	private boolean committed = false;
 	private List<Cookie> cookies = new ArrayList<Cookie>();
 	private int status = -1;
 	private ServletOutputStream servletOutputStream;
 	private PrintWriter writer;
 
 	public String content() {
 		if (writer != null) {
 			writer.flush();
 		}
 		if (servletOutputStream != null) {
 			try {
 				servletOutputStream.flush();
 			} catch (IOException e) {
 				throw new BaseException(e);
 			}
 		}
 		return sos.toString();
 	}
 
 	public int status() {
 		return status;
 	}
 
 	public int getContentLength() {
 		return contentLength;
 	}
 
 	@Override
 	public String getCharacterEncoding() {
 		return characterEncoding;
 	}
 
 	@Override
 	public String getContentType() {
 		return contentType;
 	}
 
 	@Override
 	public ServletOutputStream getOutputStream() throws IOException {
 		if (writer != null) {
 			throw new IllegalStateException("This request attempted to get a ServletOutputStream after getting a PrintWriter from the HttpServletRepsonse");
 		}
 		if (servletOutputStream == null) {
 			sos = createOutputStream();
 			servletOutputStream = new ServletOutputStream() {
 				@Override
 				public void write(int arg0) throws IOException {
 					sos.write(arg0);
 				}
 			};
 		}
 		return servletOutputStream;
 	}
 
 	@Override
 	public PrintWriter getWriter() throws IOException {
 		if (servletOutputStream != null) {
 			throw new IllegalStateException("This request attempted to get a PrintWriter after getting a ServletOutputStream from the HttpServletRepsonse");
 		}
 		if (writer == null) {
 			sos = createOutputStream();
 			writer = new PrintWriter(sos, true);
 		}
 		return writer;
 	}
 
 	@Override
 	public void setCharacterEncoding(String charset) {
 		this.characterEncoding = charset;
 	}
 
 	@Override
 	public void setContentLength(int len) {
 		this.contentLength = len;
 	}
 
 	@Override
 	public void setContentType(String type) {
 		this.contentType = type;
 	}
 
 	@Override
 	public void setBufferSize(int size) {
 	}
 
 	@Override
 	public int getBufferSize() {
 		return 0;
 	}
 
 	@Override
 	public void flushBuffer() throws IOException {
 		sos.flush();
 	}
 
 	@Override
 	public void resetBuffer() {
 	}
 
 	@Override
 	public boolean isCommitted() {
 		return committed;
 	}
 
 	@Override
 	public void reset() {
 
 	}
 
 	@Override
 	public void setLocale(Locale loc) {
 
 	}
 
 	@Override
 	public Locale getLocale() {
 		return Locale.getDefault();
 	}
 
 	@Override
 	public void addCookie(Cookie cookie) {
 		cookies.add(cookie);
 	}
 
 	@Override
 	public boolean containsHeader(String name) {
 		return headers.containsKey(name);
 	}
 
 	@Override
 	public String encodeURL(String url) {
 		return url;
 	}
 
 	@Override
 	public String encodeRedirectURL(String url) {
 		return url;
 	}
 
 	@Override
 	public String encodeUrl(String url) {
 		return url;
 	}
 
 	@Override
 	public String encodeRedirectUrl(String url) {
 		return url;
 	}
 
 	@Override
 	public void sendError(int sc, String msg) throws IOException {
 		sendError(sc);
 	}
 
 	@Override
 	public void sendError(int sc) throws IOException {
 		if (committed) {
 			throw new IllegalStateException("Response already committed");
 		}
 		this.status = sc;
 		committed = true;
 	}
 
 	@Override
 	public void sendRedirect(String location) throws IOException {
 		if (committed) {
 			throw new IllegalStateException("Response already committed");
 		}
 		committed = true;
 	}
 
 	@Override
 	public void setDateHeader(String name, long date) {
 		headers.put(name, Long.toString(date));
 	}
 
 	@Override
 	public void addDateHeader(String name, long date) {
 		headers.put(name, Long.toString(date));
 	}
 
 	@Override
 	public void setHeader(String name, String value) {
 		headers.put(name, value);
 	}
 
 	@Override
 	public void addHeader(String name, String value) {
 		headers.put(name, value);
 	}
 
 	@Override
 	public void setIntHeader(String name, int value) {
 		headers.put(name, Integer.toString(value));
 	}
 
 	@Override
 	public void addIntHeader(String name, int value) {
 		headers.put(name, Integer.toString(value));
 	}
 
 	@Override
 	public void setStatus(int sc) {
 		if (committed) {
 			throw new IllegalStateException("Response already committed");
 		}
 		this.status = sc;
 	}
 
 	@Override
 	public void setStatus(int sc, String sm) {
 		setStatus(sc);
 	}
 
 	@SuppressWarnings("serial")
 	private StringOutputStream createOutputStream() {
 		sos = new StringOutputStream(characterEncoding) {
 			@Override
 			public void flush() throws IOException {
 				committed = true;
 				super.flush();
 			}
 
 			@Override
 			public void close() {
 			}
 		};
 		return sos;
 	}
 
 	public List<Cookie> getCookies() {
 		return cookies;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> T header(String name) {
 		return (T) headers.get(name);
 	}
 
 }
