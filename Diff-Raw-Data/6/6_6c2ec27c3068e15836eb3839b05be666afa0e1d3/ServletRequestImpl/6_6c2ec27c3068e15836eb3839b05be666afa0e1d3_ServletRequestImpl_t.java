 /* Copyright (c) 2007 Bug Labs, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.buglabs.osgi.http;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.Socket;
 import java.net.URISyntaxException;
 import java.security.Principal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletInputStream;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.osgi.service.log.LogService;
 
 import freenet.support.URIPreEncoder;
 import freenet.support.io.LineReadingInputStream;
 
 /**
  * A ServletRequest implementation in part based on tutorial available at
  * http://www.onjava.com/pub/a/onjava/2003/05/14/java_webserver.html
  * 
  * @author ken
  * 
  */
 class ServletRequestImpl implements HttpServletRequest {
 	private static final String HTTP_PROTOCOL_STRING = "HTTP/1.1";
 
 	private static final String PROTOCOL_SCHEME = "http";
 
 	private static final String HTTP_SERVER_NAME = "BUG HTTP Server";
 
 	private static final int BUFFER_SIZE = 16384;
 
 	private final Socket connection;
 
 	private String uri = null;
	private String origUri = null;
 
 	private String method;
 
 	private String header;
 
 
 	private static int[] terminator = { 13, 10, 13, 10 };
 
 	private boolean secure = false;
 
 	private final Dictionary attribs;
 
 	private Locale preferredLocal;
 
 	private Map parameterMap;
 
 	private boolean parsedParameters = false;
 	
 	private String query;
 
 	private BufferedReader reader;
 
 	private ServletInputStream servletInputStream;
 
 	private Map headerMap;
 	InputStream inputStream;
 
 	final static SimpleDateFormat headerdateformat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US); // rfc1123-date
 
 	private final SimpleDateFormat rfc850DateFmt = new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss 'GMT'", Locale.US); // rfc850-date
 
 	private final SimpleDateFormat asciiDateFmt = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US); // ASCII date, used in headers 
 
 	public ServletRequestImpl(Socket connection, LogService logService) {
 		this.connection = connection;
 		headerMap = new Hashtable();
 		header = getHeader();
 		final int pos = uri.indexOf("?");
 		query = pos == -1 ? "" : uri.substring(pos+1);
 		attribs = new Hashtable();
 
 	}
 
 	private Map parseHeaderMap(String h) {
 		Map header = new Hashtable();
 		int spos = h.indexOf('\n');
 
 		String kvh = h.substring(spos + 1);
 
 		String[] lines = HttpServer.split(kvh, "\n"); 
 		Object lastKey = null;
 		String line;
 		String value;
 		for (int i = 0; i < lines.length; ++i) {
 			line = lines[i];
 
 			if (line.trim().length() > 0) {
 				if (line.indexOf(':') > -1) {
 					// this is a normal header line
 
 					int cl = line.indexOf(':');
 
 					lastKey = line.substring(0, cl).trim().toLowerCase();
 					value = line.substring(cl + 1).trim();
 
 					header.put(lastKey, value);
 				} else {
 					// this is more from the previous line
 					value = (String) header.get(lastKey);
 					header.put(lastKey, value + line);
 				}
 			}
 		}
 
 		return header;
 	}
 
 	// Following two methods taken from
 	// http://www.onjava.com/pub/a/onjava/2003/05/14/java_webserver.html
 	private String parseUri(String requestString) {
 		int index1, index2;
 		index1 = requestString.indexOf(' ');
 		if (index1 != -1) {
 			index2 = requestString.indexOf(' ', index1 + 1);
 			if (index2 > index1)
 				return requestString.substring(index1 + 1, index2);
 		}
 		throw new RuntimeException("parseURi shoiuld have a path!!!! "+requestString);
 		// return null;
 	}
 
 	private String getHeader() {
 
 		try {
 			
 			inputStream = connection.getInputStream();
 			
 			LineReadingInputStream lis = new LineReadingInputStream(inputStream);
 
 			String firstLine;
 			while (true) {
 				firstLine = lis.readLine(32768, 128, false); // ISO-8859-1 or US-ASCII, _not_ UTF-8
 				if (firstLine == null) {
 					connection.close();
 					return null;
 				} else if (firstLine.equals("")) {
 					continue;
 				}
 				break;
 			}
 
 			String[] split = firstLine.split(" ");
 
 			if(split.length != 3)
 				throw new IOException("Could not parse request line (split.length="+split.length+"): "+firstLine);
 
 			if(!split[2].startsWith("HTTP/1."))
 				throw new IOException("Unrecognized protocol "+split[2]);
 
 			
 			try {
 				uri = URIPreEncoder.encodeURI(split[1]).normalize().toString();
				origUri = uri;
 				//if(logMINOR) Logger.minor(ToadletContextImpl.class, "URI: "+uri+" path "+uri.getPath()+" host "+uri.getHost()+" frag "+uri.getFragment()+" port "+uri.getPort()+" query "+uri.getQuery()+" scheme "+uri.getScheme());
 			} catch (URISyntaxException e) {
 				e.printStackTrace();
 				//sendURIParseError(connection.getOutputStream(), true, e);
 				return null;
 			}
 			method = split[0];
 
 			while(true) {
 				String line = lis.readLine(32768, 128, false); // ISO-8859 or US-ASCII, not UTF-8
 				if (line == null) {
 					connection.close();
 					return null;
 				}
 				//System.out.println("Length="+line.length()+": "+line);
 				if(line.length() == 0) break;
 				int index = line.indexOf(':');
 				if (index < 0) {
 					throw new IOException("Missing ':' in request header field");
 				}
 				String before = line.substring(0, index).toLowerCase();
 				String after = line.substring(index+1);
 				after = after.trim();
 				headerMap.put(before, after);
 			}
 
 
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		return null;
 	}
 
 	private String parseMethod(String requestString) {
 		int index1;
 		index1 = requestString.indexOf(' ');
 		if (index1 != -1) {
 			return requestString.substring(0, index1);
 		}
 		return null;
 	}
 
 	public Object getAttribute(String arg0) {
 		return attribs.get(arg0);
 	}
 
 	public Enumeration getAttributeNames() {
 		return attribs.keys();
 	}
 
 	public String getCharacterEncoding() {
 		return "charset=utf-8";
 	}
 
 	public int getContentLength() {
 		String s = getHeader("Content-Length");
 		if (s == null) {
 			return -1;
 		}
 		return Integer.parseInt(s);
 	}
 
 	public String getContentType() {
 		return getHeader("Content-Type");
 	}
 
 	public ServletInputStream getInputStream() throws IOException {
 		if (servletInputStream == null) {
 			if ("chunked".equalsIgnoreCase(getHeader("transfer-encoding")))
 				servletInputStream = new ServletInputStreamImpl(new ChunkedInputStream(inputStream));
 			else
 				servletInputStream = new ServletInputStreamImpl(inputStream);
 		}
 
 		return servletInputStream;
 	}
 
 	public Locale getLocale() {
 		if (preferredLocal == null) {
 			preferredLocal = parsePreferredLocal(header);
 		}
 		return preferredLocal;
 	}
 
 	private Locale parsePreferredLocal(String httpHeader) {
 		int sp = httpHeader.indexOf("Accept-Language:") + "Accept-Language:".length();
 
 		if (sp > "Accept-Language".length()) {
 			int ep = httpHeader.indexOf(httpHeader, sp);
 
 			if (ep > sp) {
 				return new Locale(httpHeader.substring(sp, ep).trim(), "");
 			}
 		}
 
 		return null;
 	}
 
 	public Enumeration getLocales() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getParameter(String arg0) {
 		if (!parsedParameters) {
 			parameterMap = parseParameters(header);
 		}
 
 		if (parameterMap == null) {
 			return null;
 		}
 
 		return (String) parameterMap.get(arg0);
 	}
 
 	private Map parseParameters(String header) {
 		parsedParameters = true;
 
 		final String paramStr = getQueryString();
 
 		final String[] c2 = HttpServer.split(paramStr, "&");
 
 		Map params = new Hashtable();
 
 		for (int i = 0; i < c2.length; ++i) {
 			String expr = c2[i];
 			String[] nvp = HttpServer.split(expr, "=");
 
 			if (nvp.length != 2) {
 				// TODO throw some sort of parse error perhaps.
 				// Also research if it's valid to have '=' character in value
 				// element.
 				return null;
 			}
 
 			params.put(nvp[0], nvp[1]);
 		}
 
 		return params;
 	}
 
 	public Enumeration getParameterNames() {
 		if (!parsedParameters) {
 			parameterMap = parseParameters(header);
 		}
 
 		if (parameterMap == null) {
 			return null;
 		}
 
 		return new MapKeyEnumeration(parameterMap);
 	}
 
 	public Map getParameterMap() {
 		if (!parsedParameters) {
 			parameterMap = parseParameters(header);
 		}
 
 		return parameterMap;
 	}
 
 	public String[] getParameterValues(String arg0) {
 		synchronized (parameterMap) {
 			return (String[]) parameterMap.values().toArray(new String[parameterMap.size()]);
 		}
 	}
 
 	public String getProtocol() {
 		return HTTP_PROTOCOL_STRING;
 	}
 
 	public BufferedReader getReader() throws IOException {
 		if (reader == null) {
 			reader = new BufferedReader(new InputStreamReader(inputStream));
 		}
 
 		return reader;
 	}
 
 	public String getRealPath(String arg0) {
 		if (uri == null) {
 			uri = parseUri(header);
 		}
 
 		return uri;
 	}
 
 	public String getRemoteAddr() {
 		return connection.getLocalAddress().getHostName();
 	}
 
 	public String getRemoteHost() {
 		return connection.getInetAddress().getHostName();
 	}
 
 	public RequestDispatcher getRequestDispatcher(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getScheme() {
 		return PROTOCOL_SCHEME;
 	}
 
 	public String getServerName() {
 		return HTTP_SERVER_NAME;
 	}
 
 	public int getServerPort() {
 		return connection.getLocalPort();
 	}
 
 	public boolean isSecure() {
 		return secure;
 	}
 
 	public void removeAttribute(String arg0) {
 		attribs.remove(arg0);
 	}
 
 	public void setAttribute(String arg0, Object arg1) {
 		attribs.put(arg0, arg1);
 	}
 
 	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
 		throw new UnsupportedEncodingException(arg0);
 	}
 
 	public String getAlias() {
 		// FIXME:		
 		return uri;
 	}
 
 	public String getAuthType() {
 		return null;
 	}
 
 	public String getContextPath() {
 		if (uri == null) {
			uri = origUri;
 		}
 		int idx = uri.lastIndexOf('/');
 
 		return uri.substring(0, idx);
 	}
 
 	public Cookie[] getCookies() {
 		throw new RuntimeException("This feature is not implmemented: getCookies()");
 	}
 
 	public long getDateHeader(String name) {
 		String val = getHeader(name);
 		if (val == null)
 			return -1;
 		try {
 			return headerdateformat.parse(val).getTime();
 		} catch (ParseException pe) {
 			try {
 				return rfc850DateFmt.parse(val).getTime();
 			} catch (ParseException pe1) {
 				try {
 					return asciiDateFmt.parse(val).getTime();
 				} catch (ParseException pe3) {
 					throw new IllegalArgumentException("Value " + val
 							+ " can't be converted to Date using any of formats: [" + headerdateformat.toPattern()
 							+ "][ " + rfc850DateFmt.toPattern() + "][" + asciiDateFmt.toPattern());
 				}
 			}
 		}
 	}
 
 	public String getHeader(String arg0) {
 		return (String) headerMap.get(arg0.toLowerCase());
 	}
 
 	public Enumeration getHeaderNames() {
 		return Collections.enumeration(headerMap.keySet());
 	}
 
 	public Enumeration getHeaders(final String arg0) {
 		return new Enumeration() {
 			boolean finished = false;
 
 			public boolean hasMoreElements() {
 				return finished;
 			}
 
 			public Object nextElement() {
 				finished = true;
 
 				return headerMap.get(arg0);
 			}
 		};
 	}
 
 	public int getIntHeader(String arg0) {
 		Object o = headerMap.get(arg0);
 
 		if (o != null) {
 			return Integer.parseInt(o.toString());
 		}
 
 		return -1;
 	}
 
 	public String getMethod() {
 		return method;
 	}
 
 	public String getPathInfo() {
 		// FIXME:
 		return uri;
 	}
 
 	public String getPathTranslated() {
 		// FIXME:
 		return uri;
 	}
 
 	public String getQueryString() {
 		return query;
 	}
 
 	public String getRemoteUser() {
 		// TODO add Auth
 		return "anonymous";
 	}
 
 	public String getRequestURI() {
 		return connection.getInetAddress().toString();
 	}
 
 	public StringBuffer getRequestURL() {
 		throw new RuntimeException("This feature is not implmemented: getRequestURL()");
 	}
 
 	public String getRequestedSessionId() {
 		throw new RuntimeException("This feature is not implmemented: getRequestedSessionId()");
 	}
 
 	public String getServletPath() {
 		return uri;
 	}
 
 	public HttpSession getSession() {
 		throw new RuntimeException("This feature is not implmemented: getSession()");
 	}
 
 	public HttpSession getSession(boolean arg0) {
 		throw new RuntimeException("This feature is not implmemented: getSession()");
 	}
 
 	public Principal getUserPrincipal() {
 		throw new RuntimeException("This feature is not implmemented: getUserPrincipal()");
 	}
 
 	public boolean isRequestedSessionIdFromCookie() {
 		throw new RuntimeException("This feature is not implmemented: isRequestedSessionIdFromCookie()");
 	}
 
 	public boolean isRequestedSessionIdFromURL() {
 		throw new RuntimeException("This feature is not implmemented: isRequestedSessionIdFromURL()");
 	}
 
 	public boolean isRequestedSessionIdFromUrl() {
 		// Deprecated per Servlet API javadoc
 		return false;
 	}
 
 	public boolean isRequestedSessionIdValid() {
 		throw new RuntimeException("This feature is not implmemented: isRequestedSessionIdValid()");
 	}
 
 	public boolean isUserInRole(String arg0) {
 		return false;
 	}
 
 	protected void setUri(String uri) {
 		this.uri = uri;
 	}
 
 	private class MapKeyEnumeration implements Enumeration {
 
 		private Iterator iter;
 
 		public MapKeyEnumeration(Map map) {
 			iter = map.keySet().iterator();
 		}
 
 		public boolean hasMoreElements() {
 
 			return iter.hasNext();
 		}
 
 		public Object nextElement() {
 
 			return iter.next();
 		}
 
 	}
 
 	/**
 	 * 
 	 * @author kgilmer
 	 * 
 	 */
 	private class ServletInputStreamImpl extends ServletInputStream {
 
 		private InputStream _is;
 
 		public ServletInputStreamImpl(InputStream is) {
 			_is = is;
 		}
 
 		public int read() throws IOException {
 			return _is.read();
 		}
 
 		public int read(byte[] b, int off, int len) throws IOException {
 			return _is.read(b, off, len);
 		}
 	}
 
 	/**
 	 * An ADT to parse request header.
 	 * 
 	 * @author kgilmer
 	 * 
 	 */
 	private class HeaderStack {
 		int[] elements = { 0, 0, 0, 0 };
 
 		int index = 0;
 
 		boolean isFull = false;
 
 		public void push(int i) {
 			elements[index] = i;
 
 			index++;
 
 			if (index > 3) {
 				index = 0;
 				isFull = true;
 			}
 		}
 
 		public boolean isTerminator() {
 			if (!isFull) {
 				return false;
 			}
 
 			int count = 0;
 			int vindex = index;
 
 			while (count < 4) {
 
 				if (terminator[count] != elements[vindex]) {
 					return false;
 				}
 				vindex--;
 				if (vindex < 0) {
 					vindex = 3;
 				}
 				count++;
 			}
 
 			return true;
 		}
 	}
 
 	public String getLocalAddr() {
 		throw new RuntimeException("This feature is not implmemented: getLocalAddr()");
 	}
 
 	public String getLocalName() {
 		throw new RuntimeException("This feature is not implmemented: getLocalName()");
 	}
 
 	public int getLocalPort() {
 		throw new RuntimeException("This feature is not implmemented: getLocalPort()");
 	}
 
 	public int getRemotePort() {
 		throw new RuntimeException("This feature is not implmemented: getRemotePort()");
 	}
 
 }
