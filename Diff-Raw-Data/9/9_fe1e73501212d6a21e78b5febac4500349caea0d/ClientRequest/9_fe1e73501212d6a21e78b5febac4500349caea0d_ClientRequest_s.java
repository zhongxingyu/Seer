 /* Copyright (c) 2008-2010 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz;
 
 import org.fiz.section.FormSection;
 import java.io.*;
 import java.util.*;
 
 import javax.crypto.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import org.apache.commons.fileupload.*;
 import org.apache.commons.fileupload.disk.*;
 import org.apache.commons.fileupload.servlet.*;
 import org.apache.log4j.*;
 
 /**
  * A ClientRequest object is the master data structure providing access
  * to everything needed to serve an incoming HTTP request.  Each incoming
  * request is allocated its own ClientRequest object, which stays with
  * the request until it has completed.  A ClientRequest provides access
  * to the following things:
  *   * Information about the request itself, such as the HttpServletRequest
  *     object and a Dataset called the "main dataset" that holds the request's
  *     query values and other global information.
  *   * Information about the response generated for the request, including
  *     the HttpServletResponse object and a Fiz Html object.
  *   * Global information about the application, including the HttpServlet
  *     object for the servlet container.
  * Most of the major methods for servicing a request take a ClientRequest
  * object as their first argument; the ClientRequest is normally referred to
  * with a variable named {@code cr}.
  *
  * Individual applications may have additional global data that needs to
  * be accessible throughout the application.  They can achieve this by
  * creating a subclass of ClientRequest that holds the additional data,
  * then override the {@code getRequest} method in the application's
  * Interactor(s) to supply an instance of the ClientRequest subclass.
  * Alternatively, the application can store its data as attributes in the
  * ServletContext ({@code cr.getServletContext.getAttribute()}).
  */
 
 public class ClientRequest {
     /**
      * This enum is used to identify the kind of request being serviced.
      */
     public enum Type {
         /**
          * A traditional request, where we return an HTML document
          * for a completely new page.
          */
         NORMAL,
 
         /**
          * An Ajax request, which allows an existing page to make
          * incremental modifications without replacing the entire page.
          */
         AJAX,
 
         /**
          * A form post: the result will be HTML that is rendered in an
          * invisible iframe.  The goal is to provide the same behaviors as
          * an Ajax request (but we can't use Ajax to submit a form because
          * that would disallow file uploads).
          */
         POST
     }
 
     /**
      * MissingPagePropertyError is thrown when methods such as
      * {@code getPageProperty} cannot find the requested property.  This
      * typically means that the current page is so old that its properties
      * have been discarded.
      */
     public static class MissingPagePropertyError extends Error {
         /**
          * Construct a MissingPagePropertyError with a message describing the
          * property that was not found.
          * @param missingName      Name of the property that was not found;
          *                         used to generate a message in the
          *                         exception
          */
         public MissingPagePropertyError(String missingName) {
             super("couldn't find page property \"" + missingName + "\"");
         }
     }
 
     /**
      * StalePageError is thrown when the page stage for the current page
      * can't be found during an Ajax request or form post.
      */
     public static class StalePageError extends Error {
         /**
          * Construct a StalePageError.
          */
         public StalePageError() {
             super("Stale page: the current page is so old that the " +
                     "server has discarded its data about the page; if " +
                     "you want to keep using this page please " +
                     "click on the refresh button");
         }
     }
 
     // Main dataset key used to map the boolean indicating whether or not the
     // server running the application is Google's AppEngine.
     public static final String GOOGLE_APPENGINE = "googleAppEngine";
 
     // Main dataset key used to map the boolean indicating whether or not the
     // server running the application permits filesystem access.
     public static final String FILE_ACCESS = "serverFileAccess";
 
     // The servlet under which this ClientRequest is being processed.
     protected HttpServlet servlet;
 
     // The HttpServletRequest and HttpServletResponse objects provided by
     // the servlet container.  See documentation for the constructor arguments
     // for more information.
     protected HttpServletRequest servletRequest;
     protected HttpServletResponse servletResponse;
 
     // Used to accumulate information that will eventually be returned as
     // HTML output.
     protected Html html = null;
 
     // Top-level dataset containing global information for this request,
     // such as query values and POST data.  Also contains information about
     // the server running the application, specifically {@code googleAppEngine}
     // will be true if the server is Google's AppEngine, and {@code
     // serverFileAccess} will be true if the server permits filesystem access.
     protected Dataset mainDataset = null;
 
     // Indicates whether or not we have already attempted to process
     // Ajax POST data for this request.  If true, don't try again.
     protected boolean requestDataProcessed = false;
 
     // The following variable keeps track of all of the uploaded files
     // provided to this request.  The key for each entry is the name
     // of a form element, and the value is a handle for the uploaded
     // file provided for that form element.
     protected HashMap<String,FileUpload> uploads = null;
 
     // The kind of request we are currently servicing:
     protected Type requestType = Type.NORMAL;
 
     // The following variable accumulates Javascript code passed to the
     // evalJavascript method, if there is any.
     public StringBuilder jsCode = null;
 
     // If returnFile has been invoked, the following variables record
     // information about the file to be returned at the end of the request.
     protected String fileName = null;
     protected InputStream fileSource = null;
 
     // The following variable is used for log4j-based logging.
     protected static Logger logger = Logger.getLogger("org.fiz.ClientRequest");
 
     // If the following variable is nonzero, then all uploaded files larger
     // than this will be written temporarily to disk.  Used for testing.
     protected int testSizeThreshold = 0;
 
     // Used to generate unique ids. Records last id used for each string
     protected HashMap<String, Integer> idsMap = new HashMap<String, Integer>();
 
     // Page properties for the current page.  Null means no properties have
     // been referenced in the current request.
     public PageState pageState = null;
 
     // Holds the identifier for the current page, which is computed by
     // the getPageId method.  Null means getPageId has not yet been called
     // for this page.
     public String pageId = null;
 
     // True means that we have already set Fiz.auth in the browser; no need
     // to set it again.
     public boolean authTokenSet = false;
 
     // The following variable is set to true during some tests; this
     // causes cryptographic authentication code to be bypassed, using
     // instead a single fixed signature (so that tests don't have to
     // worry about the signature being different every round).
     public boolean testMode = false;
 
     // The following variable is set to true during most tests; this
     // causes automatic checks of the authentication token to be skipped.
     protected static boolean testSkipTokenCheck = false;
 
     /**
      * Constructs a ClientRequest object.  Typically invoked by the
      * getRequest method of an Interactor object.
      * @param servlet              Servlet under which this request is running.
      * @param servletRequest       The HttpServletRequest provided by the
      *                             container.  May be null, in which case
      *                             the corresponding methods will become
      *                             no-ops (e.g. if servletRequest is null
      *                             then getAuthType will return null and
      *                             getCookies will return an empty array).
      *                             Null is used primarily for testing.
      * @param servletResponse      The HttpServletResponse provided by the
      *                             container; if null (primarily for testing),
      *                             the corresponding methods become no-ops.
      */
     public ClientRequest(HttpServlet servlet, HttpServletRequest servletRequest,
             HttpServletResponse servletResponse) {
         this.servletRequest = servletRequest;
         this.servletResponse = servletResponse;
         this.servlet = servlet;
     }
 
     /**
      * Expands a template to generate HTML and adds it to the bulletin.  If
      * this is the first message added to the bulletin for this request,
      * any previous bulletin contents are cleared.  This method can be
      * invoked for any kind of request (Ajax, post, etc.) and does the right
      * thing in each case.
      * @param template             Template to expand to generate the HTML
      *                             for the bulletin message.
      * @param data                 Dataset to use when expanding
      *                             {@code template}.
      */
     public void addMessageToBulletin(String template, Dataset data) {
         getHtml().includeJsFile("static/fiz/Fiz.js");
         evalJavascript("Fiz.addBulletinMessage(\"@1\");\n",
                 Template.expandHtml(template, data));
     }
 
     /**
      * Generate bulletin messages for one or more errors, each described
      * by a dataset.  Typically the errors are the result of a data request.
      * A separate bulletin message is generated for each dataset in
      * {@code errors}, using the {@code bulletin} template from the
      * {@code errors} configuration dataset to generate HTML from the
      * dataset.  The messages will appear in divs with class "bulletinError".
      * @param errors               One or more Datasets, each containing
      *                             information about a particular error;
      */
     public void addErrorsToBulletin(Dataset... errors) {
         for (Dataset error : errors) {
             addMessageToBulletin(Config.getPath("styles", "bulletin.error"),
                     error);
         }
     }
 
     /**
      * This method checks for the presence of an authentication token (a
      * "fiz_auth" entry in the main dataset with a session-specific value),
      * in order to prevent CSRF attacks in form submissions and Ajax
      * requests.
      * @throws AuthenticationError The form did not contain a valid
      *                             authentication token.
      */
     protected void checkAuthToken() throws AuthenticationError {
         String inputToken = getMainDataset().checkString("fiz_auth");
         String sessionToken = getAuthToken();
         if ((inputToken == null) || !(inputToken.equals(sessionToken))) {
             throw new AuthenticationError();
         }
     }
 
     /**
      * Arrange for a Javascript script to be executed in the browser.
      * This method can be used for Ajax requests and form posts in
      * addition to normal HTML requests; it will use the appropriate
      * technique for each case.
      * @param javascript           Javascript code for the browser to
      *                             execute.  Must end with a semi-colon
      *                             and a newline.
      */
     public void evalJavascript(CharSequence javascript) {
         if (requestType == Type.NORMAL) {
             getHtml().evalJavascript(javascript);
             return;
         }
         if (jsCode == null) {
             jsCode = new StringBuilder(javascript);
         } else {
             jsCode.append(javascript);
         }
     }
 
     /**
      * Arrange for a Javascript script to be executed in the browser.
      * This method can be used for Ajax requests and form posts in
      * addition to normal HTML requests; it will use the appropriate
      * technique for each case.
      * @param template             Template for the Javascript code, which
      *                             will be expanded using {@code data}.
      *                             Must end with a semi-colon and a newline.
      * @param data                 Dataset to use for expanding
      *                             {@code template}.
      */
     public void evalJavascript(String template, Dataset data) {
         if (requestType == Type.NORMAL) {
             getHtml().evalJavascript(template, data);
             return;
         }
         if (jsCode == null) {
             jsCode = new StringBuilder(template.length() + 20);
         }
         Template.appendJs(jsCode, template, data);
     }
 
     /**
      * Arrange for a Javascript script to be executed in the browser.
      * This method can be used for Ajax requests and form posts in
      * addition to normal HTML requests; it will use the appropriate
      * technique for each case.
      * @param template             Template for the Javascript code, which
      *                             will be expanded using {@code data}.
      *                             Must end with a semi-colon and a newline.
      * @param indexedData          Values to substitute in {@code template}
      *                             using numeric specifiers such as {@code @1}.
      */
     public void evalJavascript(String template, Object ... indexedData) {
         if (requestType == Type.NORMAL) {
             getHtml().evalJavascript(template, indexedData);
             return;
         }
         if (jsCode == null) {
             jsCode = new StringBuilder(template.length() + 20);
         }
         Template.appendJs(jsCode, template, indexedData);
     }
 
     /**
      * This method is invoked by the Dispatcher at the end of a request
      * to complete the transmission of the response back to the client.
      */
     public void finish() {
         if (pageState != null && getMainDataset().getBool(GOOGLE_APPENGINE)) {
             pageState.flushPageState(this);
         }
 
         // Cleanup any uploaded files (this frees temporary file space
         // allocated for uploads faster than waiting for garbage collection).
         if (uploads != null) {
             for (FileUpload upload: uploads.values()) {
                 if (upload.isServerFileAccessPermitted()) {
                     upload.getFileItem().delete();
                 }
             }
         }
 
         // Handle file download requests.
         if (fileSource != null) {
             ServletOutputStream out;
             try {
                 out = servletResponse.getOutputStream();
             }
             catch (IOException e) {
                 logger.error("I/O error retrieving response output stream in " +
                         "ClientRequest.finish: " +
                         StringUtil.lcFirst(e.getMessage()));
                 return;
             }
             // Get the correct MIME type for this file.
             String mimeType = null;
             if (fileName != null) {
                 // Set the file name if one was specified.
                 servletResponse.setHeader("Content-Disposition",
                         "filename=\"" + fileName + "\"");
                 mimeType = getServletContext().getMimeType(fileName);
             }
             if (mimeType == null) {
                 mimeType = "application/octet-stream";
             }
             servletResponse.setContentType(mimeType);
             try {
                 // Write the contents of fileSource into the servlet response
                 // stream.
                 int length;
                 byte[] buf = new byte[4096];
                 while ((length = fileSource.read(buf)) != -1) {
                     out.write(buf, 0, length);
                 }
             } catch (IOException e) {
                 logger.error("I/O error sending response in " +
                     "ClientRequest.finish");
             }
             try {
                 // Close the input stream.
                 fileSource.close();
             } catch (IOException e) {
                 logger.warn("I/O error closing file input stream in " +
                         "ClientRequest.finish", e);
             }
             try {
                 // Flush the output stream so we find out about any errors
                 // right now.
                 out.flush();
             } catch (IOException e) {
                 logger.error("I/O error flushing output stream in " +
                         "ClientRequest.finish", e);
             }
             return;
         }
 
         // If we get here, this is not a file download request.  First,
         // flush any accumulated Javascript in a fashion appropriate for
         // the kind of request.
         PrintWriter writer = null;
         if ((jsCode != null) || (html != null)) {
             try {
                 writer = servletResponse.getWriter();
             }
             catch (IOException e) {
                 logger.error("I/O error retrieving response writer in " +
                         "ClientRequest.finish: " +
                         StringUtil.lcFirst(e.getMessage()));
                 return;
             }
         }
         if (requestType == Type.AJAX) {
             if (jsCode != null) {
                 writer.append(jsCode);
             }
         } else if (requestType == Type.POST) {
             // For post requests always send *some* Javascript, even if it is
             // an empty string; this is needed to trigger housekeeping code
             // in the browser such as clearing old error messages.
             FormSection.sendFormResponse(this, (jsCode != null) ? jsCode : "");
         }
 
         if ((html != null) && (requestType != Type.AJAX)) {
             // This is an HTML request or a form post.  Transmit the
             // accumulated HTML, if any.
             servletResponse.setContentType("text/html");
             html.print(writer);
         }
         if ((writer != null) && writer.checkError()) {
             logger.error("I/O error sending response in " +
                     "ClientRequest.finish");
         }
     }
 
     /**
      * Return an authentication string unique to this session.  This
      * token is intended to be passed to the browser and returned in
      * various requests to verify that the request was generated by a
      * page that's part of this session.  For example, the token
      * can be used as the value of a hidden form element to prevent
      * cross-site request forgery (CSRF).
      * @return                     A string containing the CSRF
      *                             authenticator.
      */
     public String getAuthToken() {
         // Implementation notes:
         // * We cache the value of the authentication token in the
         //   session so it doesn't need to be recomputed for each form.
         // * There is no need to synchronize over access to the session;
         //   it's possible that multiple requests for the same session
         //   could be running concurrently, and both try to create the
         //   cached token, but they will both create exactly the same
         //   value so there is no race condition.
 
         // First, check to see if we have a cached value.
         HttpSession session = servletRequest.getSession(true);
         Object o = session.getAttribute("fiz.ClientRequest.sessionToken");
         if (o != null) {
             return (String) o;
         }
 
         // No cached value, so we have to generate one.
         byte[] macBytes;
         try {
             if (testMode) {
                 // Special mode for some tests: generate a fixed signature.
                 macBytes = "**fake auth**".getBytes("UTF-8");
             } else {
                 macBytes = getMac().doFinal(session.getId().getBytes(
                         "UTF-8"));
             }
         } catch (UnsupportedEncodingException e) {
             throw new InternalError(
                     "ClientRequest.getAuthToken couldn't convert to UTF-8");
         }
         StringBuilder buffer = new StringBuilder();
         StringUtil.encode3to4(macBytes, buffer);
         String token = buffer.toString();
         session.setAttribute("fiz.ClientRequest.sessionToken", token);
         return token;
     }
 
     /**
      * Returns information about the kind of client request we are
      * currently servicing.
      * @return                     Indicates whether the current request
      *                             is a normal age in the request, Ajax
      *                             request, etc.
      */
     public Type getClientRequestType() {
         return requestType;
     }
 
     /**
      * Returns an object used to generate and buffer the request's HTML
      * output.
      * @return                     Html object for this request.
      */
     public Html getHtml() {
         if (html == null) {
             html = new Html(this);
         }
         return html;
     }
 
     /**
      * Generates a string containing the accumulated Javascript.
      */
     public String getJs() {
         return jsCode.toString();
     }
 
     /**
      * Returns a Mac object specific to the session for this request,
      * which can be used to cryptographically sign data to prevent
      * tampering by this session or replay in other sessions.
      * @return                     An HMAC-SHA256 Mac object.
      */
     public Mac getMac() {
         HttpSession session = servletRequest.getSession(true);
 
         // There could potentially be multiple requests for the same
         // session running concurrently, so synchronization is needed
         // here.
         synchronized(session) {
             try {
                 Mac mac = Mac.getInstance("HmacSHA256");
                 SecretKey sk = null;
 
                 Object o = session.getAttribute("fiz.mac.sk");
                 if (o != null) {
                     sk = (SecretKey) o;
                 } else {
                     // This is the first time we have needed a Mac object in
                     // this session, so we have to create a new secret key to
                     // use.  NOTE: we store a SecretKey in the session instead
                     // of a Mac because SecretKey is Serializable, and because
                     // GoogleAppEngine only permits Serializable objects to be
                     // written to the session.
                     KeyGenerator kg = KeyGenerator.getInstance("HmacSHA256");
                     sk = kg.generateKey();
                     session.setAttribute("fiz.mac.sk", sk);
                 }
 
                 mac.init(sk);
                 return mac;
             } catch (Exception e) {
                 throw new InternalError(
                         "ClientRequest.getMac couldn't create a new Mac: "
                         + e.getMessage());
             }
         }
     }
 
     /**
      * Returns the main dataset for this request.  Initially the dataset
      * contains all of the data that arrived with the request, including
      * query values from the URL and POST data (either URL-encoded or in
      * the special Fiz Ajax format excepted by {@code Ajax.readInputData}.
      * If the same name is used both in a URL query value and in POST data,
      * the query value is used and the POST data is ignored.  In addition
      * to the initial values, requests may add values to the main dataset
      * in cases where the data needs to be used globally across the request.
      * This dataset will be available in (almost?) all template expansions
      * used while processing the request.  The main dataset will also
      * include two fields describing the server running the application:
      * GOOGLE_APPENGINE, which will be true if the server is Google's
      * AppEngine, and FILE_ACCESS, if the server permits filesystem access.
      * @return                     Global dataset for this request.
      */
     public Dataset getMainDataset() {
         if (mainDataset != null) {
             return mainDataset;
         }
 
         // If null, create a new dataset.
         if (mainDataset == null) {
             mainDataset = new Dataset();
         }
         // Add fields describing the server running the application.
         mainDataset.set(GOOGLE_APPENGINE,
                 Config.get("main", GOOGLE_APPENGINE).equals("1"));
         mainDataset.set(FILE_ACCESS,
                 !mainDataset.getBool(GOOGLE_APPENGINE) &&
                 Config.get("main", FILE_ACCESS).equals("1"));
 
 
         // This is the first time someone has asked for the dataset, so we
         // need to build it.  First, if this is an Ajax request read in the
         // POST data for the request, extract the Ajax data from it, and
         // add that to the main dataset.
         if (!requestDataProcessed) {
             readRequestData();
         }
 
         // Next, load any query data provided to the request.
         Enumeration params = servletRequest.getParameterNames();
         while (params.hasMoreElements()) {
             String name = (String) params.nextElement();
             String[] values = servletRequest.getParameterValues(name);
             if (values.length == 1) {
                 mainDataset.set(name, values[0]);
             } else {
                 // This parameter has multiple values: create a nested
                 // dataset for each of them, and store the value with the
                 // name "value".
                 for (String value: values) {
                     mainDataset.add(name, new Dataset("value", value));
                 }
             }
         }
         return mainDataset;
     }
 
     /**
      * Returns a page property associated with the current page.
      * @param name                 Name of the desired property.
      * @return                     If a property named {@code name} has
      *                             been defined for the current page (by
      *                             calling {@code setPageProperty}, return
      *                             it; otherwise generate an errord.
      */
     public Object getPageProperty(String name) {
         if (pageState == null) {
             pageState = PageState.getPageState(this, getPageId(), false);
             if (pageState == null) {
                 if (requestType == Type.NORMAL) {
                     throw new MissingPagePropertyError(name);
                 } else {
                     throw new StalePageError();
                 }
             }
         }
         Object result = pageState.getPageProperty(name);
         if (result == null) {
             throw new MissingPagePropertyError(name);
         }
         return result;
     }
 
     /**
      * Set a page property on the current page, which can be retrieved later
      * by calling getPageProperty.  The property is stored in the session,
      * so it will be available in subsequent Ajax requests and form postings
      * for this page.  Fiz retains properties for the most recently
      * accessed pages in each session, removing old properties on an
      * LRU basis.
      * @param name                 Name of the desired property.
      * @param value                Value to remember for this property.
      */
     public void setPageProperty(String name, Object value) {
         if (!(value instanceof Serializable)) {
             throw new InternalError( "ClientRequest.setPageProperty " +
                     "received non-serializable " +
                     value.getClass().getSimpleName() + " object");
         }
         if (pageState == null) {
             pageState = PageState.getPageState(this, getPageId(), true);
         }
         pageState.setPageProperty(name, value);
     }
 
     /**
      * Returns information about the servlet under which the ClientRequest is
      * being processed.
      * @return                     HttpServlet for this request.
      */
     public HttpServlet getServlet() {
         return servlet;
     }
 
     /**
      * @return                     The ServletContext object associated with
      *                             this request's servlet.
      */
     public ServletContext getServletContext() {
         ServletConfig config = servlet.getServletConfig();
         return config.getServletContext();
     }
 
     /**
      * Returns the HttpServletRequest object associated with this
      * ClientRequest.
      * @return                     HttpServletRequest object managed by the
      *                             underlying servlet container.
      */
     public HttpServletRequest getServletRequest() {
         return servletRequest;
     }
 
     /**
      * Returns the HttpServletResponse object associated with this
      * ClientRequest.
      * @return                     HttpServletResponse object managed by the
      *                             underlying servlet container.
      */
     public HttpServletResponse getServletResponse() {
         return servletResponse;
     }
 
     /**
      * Return the FileUpload object associated with an uploaded file
      * that was received with the current request.  Note: see
      * {@code saveUploadedFile} for a simpler mechanism for dealing with
      * uploads.
      * @param fieldName            Selects which uploaded file to return:
      *                             the parameter value is the {@code id}
      *                             property from the UploadFormElement
      *                             (which is also the {@code name}
      *                             attribute from the {@code <input>} form
      *                             element that caused the desired file to be
      *                             uploaded).
      * @return                     The FileUpload associated with
      *                             {@code fieldName}, or null if the current
      *                             request doesn't include an uploaded file
      *                             corresponding to {@code fieldName}.
      */
     public FileUpload getUploadedFile(String fieldName) {
         if (!requestDataProcessed) {
             readRequestData();
         }
         if (uploads == null) {
             return null;
         }
         return uploads.get(fieldName);
     }
 
     /**
      * Returns true if this client request is an Ajax request, false
      * otherwise.
      * @return                     See above.
      */
     public boolean isAjax() {
         return (requestType == Type.AJAX);
     }
 
     /**
      * Returns true if the server running this application permits
      * filesystem access (not the case for Google AppEngine).
      * @return                     See above.
      */
     public boolean isFileAccessPermitted() {
         return getMainDataset().getBool(FILE_ACCESS);
     }
 
     /**
      * Returns true if the server running this application is Google's
      * AppEngine.
      * @return                     See above.
      */
     public boolean isGoogleAppEngine() {
         return getMainDataset().getBool(GOOGLE_APPENGINE);
     }
 
     /**
      * Returns true if this client request is a form submission, false
      * otherwise.
      * @return                     See above.
      */
     public boolean isPost() {
         return (requestType == Type.POST);
     }
 
     /**
      * Arrange for the browser to switch to display a different page.
      * This method can be used during Ajax requests and form posts in
      * addition to normal HTML requests; it will use the appropriate
      * technique for each case.  Any previously-generated HTML is
      * cleared.
      * @param url                  URL for the new page to display
      */
     public void redirect(CharSequence url) {
         if (requestType == Type.NORMAL) {
             html = null;
             try {
                 servletResponse.sendRedirect(url.toString());
             }
             catch (IOException e) {
                 logger.error("I/O error in ClientRequest.redirect: " +
                         StringUtil.lcFirst(e.getMessage()));
             }
         } else {
             jsCode = null;
             evalJavascript("document.location.href = \"@1\";\n", url);
         }
     }
 
     /**
      * Appropriately adds {@code var} to the end of the current request's body,
      * depending on its class. If {@code var} is a section, then its render
      * method is invoked. Otherwise, its string representation is appended to
      * the output.
     * @param cr              Overall information about the client
     *                        request being serviced.
      * @param var             Object that will be rendered appropriately
      *                        depending on that class it is.
      */
     public void renderVariable(Object var) {
         if (var instanceof Section) {
             ((Section) var).render(this);
         } else {
             getHtml().getBody().append(var.toString());
         }
     }
 
     /**
      * Arrange for the response to this request to consist of the contents
      * of a file, rather than HTML or Ajax actions.  Any other response
      * information, such as HTML, will be ignored once this method has been
      * invoked.
      *
      * @param fileName              The name of the file to send to the
      *                              browser; used to compute an appropriate
      *                              MIME type for the response.
      * @param fileSource            An {@code InputStream} that contains
      *                              the file contents. This input stream will
      *                              be closed by the ClientRequest object in
      *                              its {@code finish} method.  Null means
      *                              cancel any previous call to this method,
      *                              restoring the normal HTML or Ajax
      *                              response.
      */
     public void returnFile(String fileName, InputStream fileSource) {
         this.fileName = fileName;
         this.fileSource = fileSource;
     }
 
     /**
      * Arranged for an uploaded file (received with the current request)
      * to be saved in a given location on disk.  If any errors occur
      * while saving the file an Error is thrown.
      * @param fieldName            Selects which uploaded file to save:
      *                             the parameter value is the {@code id}
      *                             property from the UploadFormElement
      *                             (which is also the {@code name}
      *                             attribute from the {@code <input>} form
      *                             element that caused the file to be
      *                             uploaded).
      * @param dest                 Path name on disk where the upload
      *                             should be saved.
      * @return                     True means the file was successfully
      *                             saved.  False means that there is no
      *                             uploaded file corresponding to
      *                             {@code fieldName}, or filesystem access is
      *                             not permitted.
      */
     public boolean saveUploadedFile(String fieldName, String dest) {
         if (!requestDataProcessed) {
             readRequestData();
         }
         if (uploads == null) {
             return false;
         }
         FileUpload upload = uploads.get(fieldName);
         if (upload == null || !getMainDataset().getBool(FILE_ACCESS)) {
             return false;
         }
         try {
             upload.getFileItem().write(new File(dest));
         }
         catch (Exception e) {
             throw new Error("I/O error saving uploaded file for \"" +
                     fieldName + "\" to \"" + dest + "\": " + e.getMessage());
         }
         return true;
     }
 
     /**
      * This method will generate Javascript code to set a session-specific
      * authentication token in the browser; this token is returned in later
      * requests and checked by the checkAuthToken method.
      */
     public void setAuthToken() {
         if (authTokenSet) {
             return;
         }
         evalJavascript("Fiz.auth = \"@1\";\n", getAuthToken());
         authTokenSet = true;
     }
 
     /**
      * This method is invoked (typically by the dispatcher) to indicate
      * what kind of request this is.  For Ajax requests and form posts
      * this method makes sure the request contains a proper authentication
      * token to prevent CSRF attacks.
      * @param type                 Request type (NORMAL, AJAX, POST).
      * @throws AuthenticationError Type is AJAX or POST, but the request
      *                             does not contain an authentication token
      *                             that matches the current session.
      */
     public void setClientRequestType(Type type) {
         requestType = type;
         if ((requestType != Type.NORMAL) && !testSkipTokenCheck) {
             checkAuthToken();
 
             // No need for this request to set the authentication token,
             // since it already exists in the browser.
             authTokenSet = true;
         }
     }
 
     /**
      * This method is invoked to display appropriate information after an
      * error has occurred.  {@code style} and {@code defaultStyle} are used
      * to select a template from the {@code styles} dataset.  If there exists
      * such a template, it is expanded in the context of {@code errorData}
      * plus the main dataset and the result is appended to the HTML for the
      * page.  Then {@code -bulletin} is appended to the style name; if
      * the {@code styles} configuration dataset contains a template by this
      * name, it is expanded and the result is appended to the bulletin for
      * the page.  If neither of the templates exists, then an error is
      * generated.
      * @param style                Name of a value in the {@code errors}
      *                             dataset, which is a template for generating
      *                             HTML to describe the error.  May be null.
      * @param defaultStyle         If {@code style} is null, then this is used
      *                             as the name of the template.
      * @param errorDatasets        One or more datasets, each describing one
      *                             error; each dataset should contain
      *                             at least a {@code message} value.
      */
     public void showErrorInfo(String style, String defaultStyle,
             Dataset... errorDatasets) {
         for (Dataset errorData: errorDatasets) {
             CompoundDataset compound = new CompoundDataset(errorData,
                     getMainDataset());
             if (style == null) {
                 style = defaultStyle;
             }
             boolean foundTemplate = false;
             Dataset styles = Config.getDataset("styles");
             String template = styles.checkString(style);
             if (template != null) {
                 foundTemplate = true;
                 Template.appendHtml(getHtml().getBody(), template, compound);
             }
             template = styles.checkString(style + "-bulletin");
             if (template != null) {
                 foundTemplate = true;
                 getHtml().includeJsFile("static/fiz/Fiz.js");
                 getHtml().evalJavascript("Fiz.addBulletinMessage(\"@html\");",
                         new Dataset("html", Template.expandHtml(template,
                         compound)));
             }
             if (!foundTemplate) {
                 throw new InternalError("showErrorInfo found no \"" +
                         style + "\" template " +
                         "for displaying error information");
             }
         }
     }
 
     /**
      * Generate HTML for one or more Sections, appending it to the response
      * from this ClientRequest.
      * @param sections             Contents of the page: any number of Sections.
      */
     public void showSections(Section ... sections) {
         for (Section section : sections) {
             section.render(this);
         }
     }
 
     /**
      * This method will arrange for the contents of a given HTML element
      * to be replaced.  This method can be used during Ajax requests and
      * form posts as well as normal HTML requests; it will use the
      * appropriate technique for each case.
      * @param id                   {@code id} attribute for an HTML element
      *                             in the page.
      * @param html                 New HTML for the element given by
      *                             {@code id}; will be assigned to its
      *                             {@code innerHTML} property.
      */
     public void updateElement(String id, String html) {
         evalJavascript("document.getElementById(\"@1\").innerHTML = \"@2\";\n",
                 id, html);
     }
 
     /**
      * Generate Javascript code to replace the innerHTML for one or more
      * Sections.  The javascript code will be included in the response
      * and will execute on the browser.  This method can be used during
      * Ajax requests and form posts as well as normal HTML requests;
      * @param idsAndSections       An even number of arguments, grouped in
      *                             pairs of which the first argument is a
      *                             String containing an HTML element id, and
      *                             the second is a Section.  Each Section's
      *                             HTML will be regenerated and an Ajax
      *                             action will be created that uses the HTML
      *                             to replace the innerHTML of the
      *                             corresponding HTML element.
      */
     public void updateSections(Object... idsAndSections) {
         StringBuilder out = getHtml().getBody();
         int oldLength = out.length();
         int lastId = idsAndSections.length - 2;
 
         for (int i = 0; i <= lastId; i += 2) {
             String id = (String) idsAndSections[i];
             Section section = (Section) idsAndSections[i+1];
             section.render(this);
             updateElement(id, out.substring(oldLength));
             out.setLength(oldLength);
         }
     }
 
     /**
      * Returns a unique string identifying the "current page", which is
      * used to bind page properties with the page.  Fiz ensures that Ajax
      * requests and form posts will use the same page identifier as the
      * request that generated the original page.  Page identifiers are
      * generated lazily: if nothing in a page needs an identifier, then
      * no identifier is generated for the page; if an identifier isn't needed
      * until an Ajax request, then the identifier is assigned at that
      * time and downloaded to the browser so it will be available for future
      * Ajax requests and form posts.
      * @return                     The identifier for the current page,
      *                             which can be used to set and get properties
      *                             for the page.
      */
     protected String getPageId() {
         if (pageId != null) {
             // We've already computed the value; no need to do it again,
             // since it doesn't change during a given request.
             return pageId;
         }
 
         // If this is a form post or Ajax request, then the request may
         // include a page identifier (if one was assigned in some previous
         // request for this page).  If this is the case, use that id.
         pageId = getMainDataset().checkString("fiz_pageId");
         if ((pageId != null) && (pageId.length() > 0)) {
             return pageId;
         }
 
         // This is the first time a page identifier has been needed for this
         // page.  Create a new id for it, using a counter stored in the
         // session. Careful!  There could be other pages being rendered in
         // the same session at the same time, which could be competing for
         // access to the session variable.
         HttpSession session = servletRequest.getSession(true);
         Integer id;
         synchronized(session) {
             Object o = session.getAttribute("fiz.ClientRequest.lastPageId");
             if (o == null) {
                 // This is the first page ever for this session.
                 id = 1;
             } else {
                 id = ((Integer) o) + 1;
             }
             session.setAttribute("fiz.ClientRequest.lastPageId", id);
         }
         pageId = id.toString();
 
         // Download the page id to the browser so that it will be included
         // in future form posts and Ajax requests.
         evalJavascript("Fiz.pageId = \"@1\";\n", pageId);
         return pageId;
     }
 
     /**
      * This method is invoked to parse incoming request data that
      * has MIME type text/fiz, which is used for Ajax requests.  It
      * populates various internal data structures with the contents
      * of the data.
      */
     protected void readAjaxData() {
         StringBuilder postData = new StringBuilder();
         try {
             BufferedReader reader = servletRequest.getReader();
             while (true) {
                 int c = reader.read();
                 if (c == -1) {
                     break;
                 }
                 postData.append((char) c);
             }
         }
         catch (IOException e) {
             throw new IOError("I/O error in ClientRequest.readAjaxData: " +
                     e.getMessage());
         }
 
         // The posted data has the form <type>.<data><type>.<data>...
         // where <type> (a string delimited by ".") indicates the
         // kind of information that follows.  The format of each
         // <data> block depends on its type.
         //
         // Each iteration of the following loop processes one block.
 
         int current = 0;
         int length = postData.length();
         while (current < length) {
             // Extract the next <type> field.
             int start = current;
             while (true) {
                 if (current >= length) {
                     throw new SyntaxError("missing \".\" after type \"" +
                             postData.substring(start, current) +
                             "\" in Fiz browser data");
                 }
                 if (postData.charAt(current) == '.')  {
                     break;
                 }
                 current++;
             }
             String type = postData.substring(start, current);
             current++;
 
             // Check against the known types and handle appropriately.
             if (type.equals("main")) {
                 current = mainDataset.addSerializedData(postData, current);
             } else {
                 throw new SyntaxError("unknown type \"" + type +
                         "\" in Fiz browser data");
             }
         }
     }
 
     /**
      * This method is invoked on a single FileUpload item coming from a
      * multipart form data submission.  Data for basic form fields is copied to
      * the main dataset.  For uploaded files, information is saved as
      * FileUploads in {@code uploads}, for use by other methods.
      * @param item                 The FileUpload to process.
      */
     protected void readFileUpload(FileUpload item) {
         if (item.isFormField()) {
             // This is a simple string-valued field; add it to
             // the main dataset (there can be multiple values for the
             // same name).
             String name = item.getFieldName();
             mainDataset.add(name, item.getString());
         } else {
             // This is an uploaded file.  Save information about
             // the file in {@code uploads}.
             if (uploads == null) {
                 uploads = new HashMap<String,FileUpload>();
             }
             uploads.put(item.getFieldName(), item);
         }
     }
 
     /**
      * This method is invoked to parse multipart form data coming
      * from the browser.  Data for basic form fields is copied to
      * the main dataset.  For uploaded files, information is saved
      * as FileUploads in {@code uploads}, for use by other methods.
      */
     protected void readMultipartFormData() {
         try {
             if (getMainDataset().getBool(FILE_ACCESS)) {
                 // If the server permits filesystem access, create a series
                 // of on-disk FileItems (FileItem representations).
                 DiskFileItemFactory factory = new DiskFileItemFactory();
                 String tempDir = Config.getDataset("main").checkString(
                         "uploadTempDirectory");
                 if (tempDir != null) {
                     factory.setRepository(new File(tempDir));
                 }
                 if (testSizeThreshold != 0) {
                     factory.setSizeThreshold(testSizeThreshold);
                 }
                 ServletFileUpload upload = new ServletFileUpload(factory);
                 String maxSize = Config.getDataset("main").checkString(
                         "uploadMaxSize");
                 if (maxSize != null) {
                     try {
                         upload.setFileSizeMax(new Long(maxSize));
                     }
                     catch (NumberFormatException e) {
                         throw new InternalError(String.format("uploadMaxSize " +
                                 "element in main configuration dataset has " +
                                 "bad value \"%s\": must be an integer",
                                 maxSize));
                     }
                 }
                 try {
                     for (Object o: upload.parseRequest(servletRequest)) {
                         FileItem fileItem = (FileItem) o;
                         // Read and process the content of the FileUpload.
                         readFileUpload(new FileUpload(fileItem));
                     }
                 }
                 catch (FileUploadBase.FileSizeLimitExceededException e) {
                     throw new UserError(String.format("uploaded file " +
                             "exceeded length limit of %d bytes",
                             upload.getFileSizeMax()));
                 }
             } else {
                 // If the server does not permit filesystem access, create a
                 // series of in-memory FileUploads (FileItemStream
                 // representations).
                 try {
                     ServletFileUpload upload = new ServletFileUpload();
                     FileItemIterator iter = upload.getItemIterator(
                             servletRequest);
                     while (iter.hasNext()) {
                         FileItemStream fileItemStream = iter.next();
                         // Read and process the content of the FileUpload.
                         readFileUpload(new FileUpload(fileItemStream));
                     }
                 }
                 catch (IOException e) {
                     throw new InternalError("error iterating over uploaded " +
                             "FileItemStreams: " + StringUtil.lcFirst(
                                     e.getMessage()));
                 }
             }
         }
         catch (FileUploadException e) {
             throw new InternalError("error reading multi-part form data: " +
                     StringUtil.lcFirst(e.getMessage()));
         }
     }
 
     /**
      * This method processes the incoming data in an HTTP request
      * if it is in a form other than application/x-www-form-urlencoded
      * (which is processed automatically by the Servlet).  In
      * particular, this method will handle multipart/form-data
      * (used in Fiz forms to enable file uploads) and text/fiz
      * (used during Ajax requests).  Various internal data structures
      * get populated with the contents of the incoming data.
      * If this method has already been called for the current request,
      * or if there is no data in a known format then this method
      * does nothing.
      */
     protected void readRequestData() {
         if (requestDataProcessed) {
             // We have already processed the data once; no need to
             // do it again.
             return;
         }
         requestDataProcessed = true;
         if (mainDataset == null) {
             mainDataset = new Dataset();
         }
 
         // Process the incoming data according to its MIME type.
         String contentType = (servletRequest.getContentType());
         if (contentType == null) {
             return;
         }
         if (contentType.startsWith("multipart/form-data")) {
             readMultipartFormData();
         }
         if (contentType.startsWith("text/fiz")) {
             readAjaxData();
         }
     }
 
     /**
      * Creates a unique (to the request) id given a base string. For example,
      * repeated calls will return foo0, foo1, etc.
      * @param base                   Base name for Id string
      * @return                       A string containing a unique id.
      */
     public String uniqueId(String base) {
         int lastVal;
 
         if (idsMap.containsKey(base)) {
             lastVal = idsMap.get(base) + 1;
         } else {
             lastVal = 0;
         }
 
         idsMap.put(base, lastVal);
         return base + Integer.toString(lastVal, 10);
     }
 }
