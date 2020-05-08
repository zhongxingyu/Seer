 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet.jsrimpl;
 
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 
 import javax.portlet.PortalContext;
 import javax.portlet.PortletURL;
 import javax.portlet.RenderResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Locale;
import java.util.List;
 
 
 /**
  * The <CODE>RenderResponse</CODE> defines an object to assist a portlet in
  * sending a response to the portal.
  * It extends the <CODE>PortletResponse</CODE> interface to provide specific
  * render response functionality to portlets.<br>
  * The portlet container creates a <CODE>RenderResponse</CODE> object and
  * passes it as argument to the portlet's <CODE>render</CODE> method.
  *
  * @see javax.portlet.RenderRequest
  * @see javax.portlet.PortletResponse
  */
 public class RenderResponseImpl extends PortletResponseImpl implements RenderResponse {
 
     protected String contentType = null;  // needed as servlet 2.3 does not have a response.getContentType
     protected boolean hasWriter = false;
     protected boolean hasOutputStream = false;
 
     /**
      * Constructs an instance of SportletResponse using an
      * <code>HttpServletResponse</code> as a proxy
      *
      * @param res the <code>HttpServletRequest</code>
      */
     public RenderResponseImpl(HttpServletRequest req, HttpServletResponse res, PortalContext portalContext) {
         super(req, res, portalContext);
         //contentType = res.getContentType();
     }
 
     // Jakarta Pluto method
     private String stripCharacterEncoding(String type) {
         int xs = type.indexOf(';');
         String strippedType;
         if (xs == -1) {
             strippedType = type;
         } else {
             strippedType = type.substring(0, xs);
         }
         return strippedType.trim();
     }
 
     /**
      * Property to set the expiration time in seconds for this
      * response using the <code>setProperty</code> method.
      * <P>
      * If the expiration value is set to 0, caching is disabled
      * for this portlet; if the value is set to -1,
      * the cache does not expire.
      * <p/>
      * The value is <code>"portlet.expiration-cache"</code>.
      */
     public static final String EXPIRATION_CACHE = "portlet.expiration-cache";
 
     /**
      * Returns the MIME type that can be used to contribute
      * markup to the render response.
      * <p/>
      * If no content type was set previously using the {@link #setContentType} method
      * this method retuns <code>null</code>.
      *
      * @return the MIME type of the response, or <code>null</code>
      *         if no content type is set
      * @see #setContentType
      */
     public String getContentType() {
         // in servlet 2.4 we could simply use this:
         // return this._getHttpServletResponse().getContentType();
         return contentType;
     }
 
 
     /**
      * Creates a portlet URL targeting the portlet. If no portlet mode,
      * window state or security modifier is set in the PortletURL the
      * current values are preserved. If a request is triggered by the
      * PortletURL, it results in a render request.
      * <p/>
      * The returned URL can be further extended by adding
      * portlet-specific parameters and portlet modes and window states.
      * <p/>
      * The created URL will per default not contain any parameters
      * of the current render request.
      *
      * @return a portlet render URL
      */
     public PortletURL createRenderURL() {
         PortletURLImpl portletURL = new PortletURLImpl(req, (HttpServletResponse) super.getResponse(), portalContext, true);
         portletURL.setComponentID((String) req.getAttribute(SportletProperties.COMPONENT_ID));
         return portletURL;
     }
 
     /**
      * Creates a portlet URL targeting the portlet. If no portlet mode,
      * window state or security modifier is set in the PortletURL the
      * current values are preserved. If a request is triggered by the
      * PortletURL, it results in an action request.
      * <p/>
      * The returned URL can be further extended by adding
      * portlet-specific parameters and portlet modes and window states.
      * <p/>
      * The created URL will per default not contain any parameters
      * of the current render request.
      *
      * @return a portlet action URL
      */
     public PortletURL createActionURL() {
         PortletURLImpl portletURL = new PortletURLImpl(req, (HttpServletResponse) super.getResponse(), portalContext, false);
         portletURL.setAction("");
         portletURL.setComponentID((String) req.getAttribute(SportletProperties.COMPONENT_ID));
         return portletURL;
     }
 
     /**
      * The value returned by this method should be prefixed or appended to
      * elements, such as JavaScript variables or function names, to ensure
      * they are unique in the context of the portal page.
      *
      * @return the namespace
      */
     public String getNamespace() {
         return "gridsphere_" + req.getAttribute(SportletProperties.COMPONENT_ID);
     }
 
     /**
      * This method sets the title of the portlet.
      * <p/>
      * The value can be a text String
      *
      * @param title portlet title as text String or resource URI
      */
     public void setTitle(String title) {
         if (title == null) title = "Unknown portlet title";
         req.setAttribute(SportletProperties.PORTLET_TITLE, title);
     }
 
     /**
      * Sets the MIME type for the render response. The portlet must
      * set the content type before calling {@link #getWriter} or
      * {@link #getPortletOutputStream}.
      * <p/>
      * Calling <code>setContentType</code> after <code>getWriter</code>
      * or <code>getOutputStream</code> does not change the content type.
      *
      * @param type the content MIME type
      * @throws java.lang.IllegalArgumentException
      *          if the given type is not in the list returned
      *          by <code>PortletRequest.getResponseContentTypes</code>
      * @see javax.portlet.RenderRequest#getResponseContentTypes
      * @see #getContentType
      */
     public void setContentType(String type) {
         if (type == null) throw new IllegalArgumentException("supplied content type is null!");
         String mimeType = stripCharacterEncoding(type);
        List mimeTypes = (List)req.getAttribute(SportletProperties.MIME_TYPES);
        if (!mimeTypes.contains(type)) {
             throw new IllegalArgumentException(mimeType);
         }
         this.getHttpServletResponse().setContentType(mimeType);
         this.contentType = mimeType;
     }
 
 
     /**
      * Returns the name of the charset used for
      * the MIME body sent in this response.
      * <p/>
      * <p>See <a href="http://ds.internic.net/rfc/rfc2045.txt">RFC 2047</a>
      * for more information about character encoding and MIME.
      *
      * @return		a <code>String</code> specifying the
      * name of the charset, for
      * example, <code>ISO-8859-1</code>
      */
     public String getCharacterEncoding() {
         return this.getHttpServletResponse().getCharacterEncoding();
     }
 
 
     /**
      * Returns a PrintWriter object that can send character
      * text to the portal.
      * <p/>
      * Before calling this method the content type of the
      * render response must be set using the {@link #setContentType}
      * method.
      * <p/>
      * Either this method or {@link #getPortletOutputStream} may be
      * called to write the body, not both.
      *
      * @return a <code>PrintWriter</code> object that
      *         can return character data to the portal
      * @throws java.io.IOException if an input or output exception occurred
      * @throws java.lang.IllegalStateException
      *                             if the <code>getPortletOutputStream</code> method
      *                             has been called on this response,
      *                             or if no content type was set using the
      *                             <code>setContentType</code> method.
      * @see #setContentType
      * @see #getPortletOutputStream
      */
     public java.io.PrintWriter getWriter() throws java.io.IOException {
         if ((contentType == null) || (hasOutputStream)) throw new IllegalStateException("A writer has already been obtained or the content type has not been set!");
         hasWriter = true;
         return this.getHttpServletResponse().getWriter();
     }
 
 
     /**
      * Returns the locale assigned to the response.
      *
      * @return Locale of this response
      */
     public java.util.Locale getLocale() {
         Locale locale = (Locale) this.req.getSession(true).getAttribute(User.LOCALE);
         if (locale != null) return locale;
         locale = this.req.getLocale();
         if (locale != null) return locale;
         return Locale.ENGLISH;
     }
 
 
     /**
      * Sets the preferred buffer size for the body of the response.
      * The portlet container will use a buffer at least as large as
      * the size requested.
      * <p/>
      * This method must be called before any response body content is
      * written; if content has been written, or the portlet container
      * does not support buffering, this method may throw an
      * <code>IllegalStateException</code>.
      *
      * @param size the preferred buffer size
      * @throws java.lang.IllegalStateException
      *          if this method is called after
      *          content has been written, or the
      *          portlet container does not support buffering
      * @see #getBufferSize
      * @see #flushBuffer
      * @see #isCommitted
      * @see #reset
      */
     public void setBufferSize(int size) {
         throw new IllegalStateException("portlet container does not support buffering");
     }
 
 
     /**
      * Returns the actual buffer size used for the response.  If no buffering
      * is used, this method returns 0.
      *
      * @return	 the actual buffer size used
      * @see #setBufferSize
      * @see #flushBuffer
      * @see #isCommitted
      * @see #reset
      */
     public int getBufferSize() {
         return 0;
     }
 
 
     /**
      * Forces any content in the buffer to be written to the client.  A call
      * to this method automatically commits the response.
      *
      * @throws java.io.IOException if an error occured when writing the output
      * @see #setBufferSize
      * @see #getBufferSize
      * @see #isCommitted
      * @see #reset
      */
     public void flushBuffer() throws java.io.IOException {
         this.getHttpServletResponse().flushBuffer();
     }
 
 
     /**
      * Clears the content of the underlying buffer in the response without
      * clearing properties set. If the response has been committed,
      * this method throws an <code>IllegalStateException</code>.
      *
      * @throws java.lang.IllegalStateException
      *          if this method is called after
      *          response is comitted
      * @see #setBufferSize
      * @see #getBufferSize
      * @see #isCommitted
      * @see #reset
      */
     public void resetBuffer() {
         this.getHttpServletResponse().resetBuffer();
     }
 
     /**
      * Returns a boolean indicating if the response has been
      * committed.
      *
      * @return		a boolean indicating if the response has been
      * committed
      * @see #setBufferSize
      * @see #getBufferSize
      * @see #flushBuffer
      * @see #reset
      */
     public boolean isCommitted() {
         return this.getHttpServletResponse().isCommitted();
     }
 
 
     /**
      * Clears any data that exists in the buffer as well as the properties set.
      * If the response has been committed, this method throws an
      * <code>IllegalStateException</code>.
      *
      * @throws java.lang.IllegalStateException
      *          if the response has already been
      *          committed
      * @see #setBufferSize
      * @see #getBufferSize
      * @see #flushBuffer
      * @see #isCommitted
      */
     public void reset() {
         this.getHttpServletResponse().reset();
     }
 
 
     /**
      * Returns a <code>OutputStream</code> suitable for writing binary
      * data in the response. The portlet container does not encode the
      * binary data.
      * <p/>
      * Before calling this method the content type of the
      * render response must be set using the {@link #setContentType}
      * method.
      * <p/>
      * Calling <code>flush()</code> on the OutputStream commits the response.
      * <p/>
      * Either this method or {@link #getWriter} may be called to write the body, not both.
      *
      * @throws java.lang.IllegalStateException
      *                             if the <code>getWriter</code> method
      *                             has been called on this response, or
      *                             if no content type was set using the
      *                             <code>setContentType</code> method.
      * @throws java.io.IOException if an input or output exception occurred
      * @return	a <code>OutputStream</code> for writing binary data
      * @see #setContentType
      * @see #getWriter
      */
     public java.io.OutputStream getPortletOutputStream() throws java.io.IOException {
         if ((contentType == null) || (hasWriter)) throw new IllegalStateException("A writer has already been obtained or the content type has not been set!");
         hasOutputStream = true;
         return this.getHttpServletResponse().getOutputStream();
     }
 
 }
 
 
