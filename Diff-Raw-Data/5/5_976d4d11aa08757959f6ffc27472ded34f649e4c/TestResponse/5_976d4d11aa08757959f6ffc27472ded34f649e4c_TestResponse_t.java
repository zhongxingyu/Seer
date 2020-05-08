 /*
  * xTest
  * Copyright (C) 2012 Stefano Fornari
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
  * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  */
 package ste.xtest.jetty;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Locale;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author ste
  */
 public class TestResponse
 extends HashMap<String,Object>
 implements HttpServletResponse {
 
     public final static String RES_WRITER = "writer";
     public final static String RES_STATUS = "status";
    public final static String RES_STATUS_MESSAGE = "statusMessage";
     public final static String RES_REDIRECTION = "redirtection";
    public final static String RES_CONTENT_TYPE = "contentType";
 
     public String statusMessage;
 
     public TestResponse() {
         super();
 
         put(RES_WRITER, new PrintWriter(new ByteArrayOutputStream()));
     }
 
     @Override
     public PrintWriter getWriter() {
         return (PrintWriter)get(RES_WRITER);
     }
 
     @Override
     public void setStatus(final int status) {
         put(RES_STATUS, status);
     }
 
     @Override
     public int getStatus() {
         return (int)get(RES_STATUS);
     }
 
     @Override
     public void sendError(int status, String msg) {
         setStatus(status);
         put(RES_STATUS_MESSAGE, msg);
     }
 
     @Override
     public void addCookie(Cookie cookie) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public boolean containsHeader(String string) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String encodeURL(String string) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String encodeRedirectURL(String string) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String encodeUrl(String string) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String encodeRedirectUrl(String string) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void sendError(int i) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void sendRedirect(String url) throws IOException {
         put(RES_REDIRECTION, url);
     }
 
     @Override
     public void setDateHeader(String string, long l) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void addDateHeader(String string, long l) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setHeader(String string, String string1) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void addHeader(String string, String string1) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setIntHeader(String string, int i) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void addIntHeader(String string, int i) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setStatus(int i, String string) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String getHeader(String string) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Collection<String> getHeaders(String string) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Collection<String> getHeaderNames() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String getCharacterEncoding() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String getContentType() {
         return (String)get(RES_CONTENT_TYPE);
     }
 
     @Override
     public ServletOutputStream getOutputStream() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setCharacterEncoding(String string) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setContentLength(int i) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setContentType(String string) {
         put(RES_CONTENT_TYPE, string);
     }
 
     @Override
     public void setBufferSize(int i) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public int getBufferSize() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void flushBuffer() throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void resetBuffer() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public boolean isCommitted() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void reset() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setLocale(Locale locale) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Locale getLocale() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 }
