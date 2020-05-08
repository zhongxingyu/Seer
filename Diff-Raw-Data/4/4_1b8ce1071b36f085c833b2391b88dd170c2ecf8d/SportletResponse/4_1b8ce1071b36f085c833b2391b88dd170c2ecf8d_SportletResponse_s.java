 /*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
 package org.gridlab.gridsphere.portlet.impl;
 
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.portlet.PortletURI;
 import org.gridlab.gridsphere.portlet.PortletWindow;
 import org.gridlab.gridsphere.portletcontainer.GridSphereProperties;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Locale;
 
 /**
  * A <code>SportletResponse</code> provides an implementation of the
  * <code>PortletResponse</code> by following the decorator patterm.
  * A HttpServletresponse object is used in composition to perform most of
  * the required methods.
  */
 public class SportletResponse implements PortletResponse {
 
     private HttpServletResponse res = null;
     private HttpServletRequest req = null;
 
     /**
      * Cannot instantiate uninitialized SportletResponse
      */
     private SportletResponse() {
     }
 
     /**
      * Constructs an instance of SportletResponse using an
      * <code>HttpServletResponse</code> and a <code>PortletRequest</code>
      *
      * @param res the <code>HttpServletRequest</code>
      * @param req the <code>PortletRequest</code>
      */
     public SportletResponse(HttpServletResponse res, PortletRequest req) {
         this.res = res;
         this.req = req;
        String mimeType = req.getClient().getMimeType();
        res.setContentType(mimeType);
     }
 
     /**
      * Adds the specified cookie to the response.
      *
      * @param cookie the cookie to be added
      */
     public void addCookie(Cookie cookie) {
         res.addCookie(cookie);
     }
 
     /**
      * Adds a response header with the given name and date-value.
      *
      * @param name the name to be added
      * @param date the date-value
      */
     public void addDateHeader(String name, long date) {
         res.addDateHeader(name, date);
     }
 
     /**
      * Adds a response header with the given name and value.
      *
      * @param name the name of the header
      * @param value the additional header value
      */
     public void addHeader(String name, String value) {
         res.addHeader(name, value);
     }
 
     /**
      * Adds a response header with the given name and integer value.
      *
      * @param name the name of the header
      * @param value the additional header value
      */
     public void addIntHeader(String name, int value) {
         res.addIntHeader(name, value);
     }
 
     /**
      * Returns a boolean indicating whether the named response header has
      * already been set.
      *
      * @return <code>true</code> if response header name has been sent,
      * <code>false</code> otherwise
      */
     public boolean containsHeader(String name) {
         return res.containsHeader(name);
     }
 
     /**
      * Creates a portlet URI pointing at the referrer of the portlet.
      *
      * @return the portletURI
      */
     public PortletURI createReturnURI() {
         SportletURI sportletURI = new SportletURI(res, req.getContextPath());
         addURIParameters(sportletURI);
         sportletURI.setReturn(true);
         return sportletURI;
     }
 
 
     /**
      * Creates a portlet URI pointing to the current portlet mode.
      *
      * @return the portlet URI
      */
     public PortletURI createURI() {
         SportletURI sportletURI = new SportletURI(res, req.getContextPath());
         addURIParameters(sportletURI);
         sportletURI.setReturn(false);
         return sportletURI;
     }
 
     /**
      * Creates a portlet URI pointing to the current portlet mode and given portlet window state.
      *
      * @param state the portlet window state
      * @see #addURIParameters
      */
     public PortletURI createURI(PortletWindow.State state) {
         SportletURI sportletURI = new SportletURI(res, req.getContextPath());
         addURIParameters(sportletURI);
         sportletURI.setWindowState(state);
         return sportletURI;
     }
 
     /**
      * Add any additional parameters to the URI:
      * <ul><li>
      * GridSphereProperties.COMPONENT_ID
      * </li></ul>
      */
     protected void addURIParameters(PortletURI sportletURI) {
         sportletURI.addParameter(GridSphereProperties.COMPONENT_ID, (String) req.getAttribute(GridSphereProperties.COMPONENT_ID));
     }
 
     /**
      * Maps the given string value into this portlet's namespace.
      *
      * @param aValue the string value
      */
     public String encodeNamespace(String aValue) {
         return null;
     }
 
     /**
      * Returns the encoded URI of the resource at the given path.
      *
      * @param path the given path
      * @return the encoded URI
      */
     public String encodeURL(String path) {
         return res.encodeURL(path);
     }
 
     /**
      * Returns the name of the charset used for the MIME body sent in this response.
      *
      * @return the character encoding
      */
     public String getCharacterEncoding() {
         return res.getCharacterEncoding();
     }
 
     /**
      * Returns the content type that can be used to contribute markup to the portlet response.
      *
      * @return the content type
      */
     public String getContentType() {
         return "html";
     }
 
     /**
      * Returns the writer object that can be used to contribute markup to the portlet response.
      *
      * @return the writer
      * @throws IOException if an I/O error occurs
      */
     public PrintWriter getWriter() throws IOException {
         return res.getWriter();
     }
 
     /**
      * Sets a response header with the given name and date-value.
      *
      * @param name the header name
      * @param date the header date-value
      */
     public void setDateHeader(String name, long date) {
         res.setDateHeader(name, date);
     }
 
     /**
      * Sets a response header with the given name and value.
      *
      * @param name the header name
      * @param value the header value
      */
     public void setHeader(String name, String value) {
         res.setHeader(name, value);
     }
 
     public void setIntHeader(String name, int value) {
         res.setIntHeader(name, value);
     }
 
     public ServletOutputStream getOutputStream() throws IOException {
         return res.getOutputStream();
     }
 
     public void setContentLength(int i) {
         res.setContentLength(i);
     }
 
     public void setContentType(String s) {
         res.setContentType(s);
     }
 
     public void setBufferSize(int i) throws IllegalStateException {
         res.setBufferSize(i);
     }
 
     public int getBufferSize() {
         return res.getBufferSize();
     }
 
     public void flushBuffer() throws IOException {
         res.flushBuffer();
     }
 
 
     public void resetBuffer() throws IllegalStateException {
         //res.resetBuffer();
     }
 
 
     public boolean isCommitted() {
         return res.isCommitted();
     }
 
     public void reset() throws IllegalStateException {
         res.reset();
     }
 
     public void setLocale(Locale locale) {
         res.setLocale(locale);
     }
 
     public Locale getLocale() {
         return res.getLocale();
     }
 
     public String encodeRedirectURL(String s) {
         return res.encodeRedirectURL(s);
     }
 
     public String encodeRedirectUrl(String s) {
         return res.encodeRedirectUrl(s);
     }
 
     public String encodeUrl(String s) {
         return res.encodeUrl(s);
     }
 
     public void sendError(int i) throws IOException {
         res.sendError(i);
     }
 
     public void sendError(int i, String s) throws IOException {
         res.sendError(i, s);
     }
 
     public void sendRedirect(String s) throws IOException {
         res.sendRedirect(s);
     }
 
     public void setStatus(int i) {
         res.setStatus(i);
     }
 
     public void setStatus(int i, String s) {
         res.setStatus(i, s);
     }
 
 
 }
 
