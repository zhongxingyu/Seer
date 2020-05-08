 /*******************************************************************************
  * Copyright (c) 2011 consiliens (consiliens@gmail.com).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  ******************************************************************************/
 package com.github.consiliens.harv.util;
 
 import static com.github.consiliens.harv.util.Invoke.invoke;
 import static com.github.consiliens.harv.util.Invoke.invokeStatic;
 
 import java.io.File;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.RequestLine;
 import org.apache.http.StatusLine;
 import org.jboss.netty.handler.codec.http.Cookie;
 import org.jboss.netty.handler.codec.http.CookieDateFormat;
 import org.jboss.netty.handler.codec.http.CookieDecoder;
 import org.jboss.netty.handler.codec.http.QueryStringDecoder;
 
 import com.subgraph.vega.api.model.IModel;
 import com.subgraph.vega.api.model.IWorkspace;
 import com.subgraph.vega.api.model.IWorkspaceEntry;
 import com.subgraph.vega.api.model.requests.IRequestLogRecord;
 import com.subgraph.vega.internal.model.Model;
 import com.subgraph.vega.internal.model.WorkspaceEntry;
 
 import edu.umass.cs.benchlab.har.HarCache;
 import edu.umass.cs.benchlab.har.HarContent;
 import edu.umass.cs.benchlab.har.HarCookie;
 import edu.umass.cs.benchlab.har.HarCookies;
 import edu.umass.cs.benchlab.har.HarEntry;
 import edu.umass.cs.benchlab.har.HarEntryTimings;
 import edu.umass.cs.benchlab.har.HarHeader;
 import edu.umass.cs.benchlab.har.HarHeaders;
 import edu.umass.cs.benchlab.har.HarPostData;
 import edu.umass.cs.benchlab.har.HarQueryParam;
 import edu.umass.cs.benchlab.har.HarQueryString;
 import edu.umass.cs.benchlab.har.HarRequest;
 import edu.umass.cs.benchlab.har.HarResponse;
 import edu.umass.cs.benchlab.har.ISO8601DateFormatter;
 
 public class Utils {
 
     public static void p(final Object o) {
         if (o == null) {
             System.out.println("null");
             return;
         }
 
         System.out.println(o.toString());
     }
 
     /** Create a fake object until Vega adds support for timings. **/
     public static HarEntryTimings getFakeHarEntryTimings() {
         final long blocked = 0L;
         final long dns = 0L;
         final long connect = 0L;
         final long send = 0L;
         final long wait = 0L;
         final long receive = 0L;
         final long ssl = 0L;
         final String comment = null;
 
         return new HarEntryTimings(blocked, dns, connect, send, wait, receive, ssl, comment);
     }
 
     public static long extractHeadersAndCookies(final Header[] allHeaders, final HarHeaders harHeaders,
             final HarCookies harCookies) {
 
         long headersSize = 0;
         final CookieDecoder decoder = new CookieDecoder();
         final CookieDateFormat format = new CookieDateFormat();
 
         for (final Header header : allHeaders) {
             final String headerName = header.getName();
             final String headerValue = header.getValue();
 
             headersSize += headerName.getBytes().length + headerValue.getBytes().length;
 
             harHeaders.addHeader(new HarHeader(headerName, headerValue));
 
             if (headerValue != null && headerName.equalsIgnoreCase("Cookie")) {
                 final Set<Cookie> cookies = decoder.decode(headerValue);
 
                 for (final Cookie cookie : cookies) {
                     // Is it correct to set expires to
                     // "1969-12-31T16:59:59.000-07:00" if there's no maxAge?
                     Date expires = null;
                     try {
                         expires = format.parse(format.format(cookie.getMaxAge()));
                     } catch (final ParseException e) {
                         e.printStackTrace();
                     }
                     harCookies.addCookie(new HarCookie(cookie.getName(), cookie.getValue(), cookie.getPath(), cookie
                             .getDomain(), expires, cookie.isHttpOnly(), cookie.isSecure(), cookie.getComment()));
                 }
             }
         }
 
         return headersSize;
     }
 
     /** Request. **/
     public static HarRequest createHarRequest(final HttpRequest httpRequest) {
         final RequestLine line = httpRequest.getRequestLine();
 
         final String method = line.getMethod();
         final String uri = line.getUri();
         final String httpVersion = line.getProtocolVersion().toString();
 
         final HarHeaders headers = new HarHeaders();
         final HarCookies cookies = new HarCookies();
         final long headersSize = extractHeadersAndCookies(httpRequest.getAllHeaders(), headers, cookies);
 
         final HarQueryString queryString = new HarQueryString();
         final QueryStringDecoder decoder = new QueryStringDecoder(uri);
         final List<HarQueryParam> harParamsList = new ArrayList<HarQueryParam>();
 
         for (final Map.Entry<String, List<String>> param : decoder.getParameters().entrySet()) {
             final String name = param.getKey();
             // Create separate params for multiple values with the same key.
             // /test?t=1&t=2
             for (final String value : param.getValue()) {
                 harParamsList.add(new HarQueryParam(name, value));
             }
         }
 
         queryString.setQueryParams(harParamsList);
        harParamsList.clear();
 
         final HarPostData postData = null;
         final long bodySize = -1;
         final String comment = null;
 
         return new HarRequest(method, uri, httpVersion, cookies, headers, queryString, postData, headersSize, bodySize,
                 comment);
     }
 
     /** Response. **/
     public static HarResponse createHarResponse(final HttpResponse httpResponse) {
         final StatusLine responseStatus = httpResponse.getStatusLine();
         final int status = responseStatus.getStatusCode();
         final String statusText = responseStatus.getReasonPhrase();
         final String responseHttpVersion = httpResponse.getProtocolVersion().toString();
 
         final HarCookies cookies = new HarCookies();
         final HarHeaders headers = new HarHeaders();
 
         final long headersSize = extractHeadersAndCookies(httpResponse.getAllHeaders(), headers, cookies);
 
         long size = 0;
         String mimeType = "";
         String encoding = "";
 
         final HttpEntity contentEntity = httpResponse.getEntity();
         if (contentEntity != null) {
             size = contentEntity.getContentLength();
 
             final Header contentType = contentEntity.getContentType();
             if (contentType != null) {
                 final String contentTypeValue = contentType.getValue();
                 mimeType = contentTypeValue == null ? "" : contentTypeValue;
             }
 
             final Header contentEnconding = contentEntity.getContentEncoding();
             if (contentEnconding != null) {
                 final String contentEncondingValue = contentEnconding.getValue();
                 encoding = contentEncondingValue == null ? "" : contentEncondingValue;
             }
         }
 
         // Not implemented.
         final long compression = 0;
         final String text = null;
         final String comment = null;
         final String redirectURL = "";
         final long bodySize = -1;
 
         final HarContent content = new HarContent(size, compression, mimeType, text, encoding, comment);
 
         return new HarResponse(status, statusText, responseHttpVersion, cookies, headers, content, redirectURL,
                 headersSize, bodySize, comment);
 
     }
 
     public static void convertRecordsToHAR(final List<IRequestLogRecord> recordsList, final HarManager har) {
         for (final IRequestLogRecord record : recordsList) {
 
             final String pageRef = null;
             Date startedDateTime = null;
 
             try {
                 startedDateTime = ISO8601DateFormatter.parseDate(ISO8601DateFormatter.format(new Date(record
                         .getTimestamp())));
             } catch (final ParseException e) {
                 e.printStackTrace();
             }
 
             final long time = record.getRequestMilliseconds();
             final HarRequest request = createHarRequest(record.getRequest());
             final HarResponse response = createHarResponse(record.getResponse());
             final HarCache cache = null;
             final HarEntryTimings timings = getFakeHarEntryTimings();
             final String serverIPAddress = null;
             final String connection = String.valueOf(record.getRequestId());
             final String comment = null;
 
             // Har entry is now complete.
             har.addEntry(new HarEntry(pageRef, startedDateTime, time, request, response, cache, timings,
                     serverIPAddress, connection, comment));
         }
     }
 
     public static IWorkspace openWorkspaceByNumber(final String wsNumber) {
         final File ws = getDefaultWorkspace(wsNumber);
         p("Workspace: " + ws);
 
         final IWorkspaceEntry entry = (IWorkspaceEntry) invokeStatic(WorkspaceEntry.class, "createFromPath", ws);
 
         final IModel model = new Model();
         invoke(model, "openWorkspaceEntry", entry);
 
         final IWorkspace openWorkspace = model.getCurrentWorkspace();
 
         if (openWorkspace == null) {
             p("open workspace failed");
             return null;
         }
 
         return openWorkspace;
     }
 
     public static File getDefaultWorkspace(final String wsNumber) {
         final File ws = new File(System.getProperty("user.home"), ".vega" + File.separator + "workspaces"
                 + File.separator + wsNumber);
         return ws;
     }
}
