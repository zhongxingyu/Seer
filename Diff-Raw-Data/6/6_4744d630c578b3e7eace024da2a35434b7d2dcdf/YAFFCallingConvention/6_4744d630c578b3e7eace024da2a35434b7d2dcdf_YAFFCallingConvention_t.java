 // See the COPYRIGHT file for copyright and license information
 package org.znerd.yaff;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.text.TextUtils;
import org.xins.common.xml.Element;
 import org.xins.server.CustomCallingConvention;
 import org.xins.server.FunctionNotSpecifiedException;
 import org.xins.server.FunctionRequest;
 import org.xins.server.FunctionResult;
 import org.xins.server.InvalidRequestException;
 
 /**
  * Integration point between XINS and YAFF: the YAFF calling convention. XINS 
  * passes control to this class when it determines that an incoming HTTP 
  * request can be and should be handled by YAFF.
  *
  * <p>To determine if this calling convention can handle a request, all the
  * respective method {@link #matches(HttpServletRequest)} does is look at the
  * <code>_function</code> HTTP parameter; if that is set, this calling
  * convention will not handle the request, since it is expected to be meant
  * for the XINS standard calling convention.
  *
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public class YAFFCallingConvention extends CustomCallingConvention {
 
    /* TODO: Do this somewhere in this class, if it's not done already:
       if (realmName != null && appCenter.getContext().getSession().getLogin(realmName) == null) {
          httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
       }
    */
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Key used to store the wrapper around the original HTTP request.
     */
    private final static String WRAPPED_REQUEST = "WRAPPED_REQUEST";
 
    /**
     * Key used to store a reference to the XINS FunctionRequest object as an
     * attribute with an HttpServletRequest.
     */
    private final static String XINS_REQUEST_ATTR = "XINS_FUNCTION_REQUEST";
          
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>YAFFCallingConvention</code> instance.
     */
    public YAFFCallingConvention() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    @Override
    protected boolean matches(HttpServletRequest httpRequest) {
 
       // The request should not contain the _function parameter,
       // those are handled by the XINS standard calling convention
       if (! TextUtils.isEmpty(httpRequest.getParameter("_function"))) {
          return false;
       }
 
       return true;
    }
 
    @Override
    protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
    throws InvalidRequestException,
           FunctionNotSpecifiedException {
 
       // Clear the context for this thread, just to be on the safe side
       AppCenter appCenter = AppCenter.get();
       appCenter.setContext(null);
 
       // Determine the applicable virtual host (can be null)
       VirtualHostHandler vhostHandler = appCenter.getVirtualHostHandler(httpRequest);
 
       // No matching virtual host found, return 404 page (NotFound),
       FunctionRequest xinsRequest;
       if (vhostHandler == null) {
         xinsRequest = new FunctionRequest("NotFound", new BasicPropertyReader(), (Element) null);
 
       // Matching virtual host found
       } else {
 
          // In case of a file upload, convert the HTTP request
          if (ServletFileUpload.isMultipartContent(httpRequest)) {
             MultipartServletRequestWrapper wrapper;
             try {
                wrapper = new MultipartServletRequestWrapper(httpRequest);
             } catch (IOException cause) {
                throw new InvalidRequestException("Failed to analyze RFC 1867 multipart request.", cause);
             }
             httpRequest.setAttribute(WRAPPED_REQUEST, wrapper);
             httpRequest = wrapper;
          }
 
          // Make the virtual host handler process the request
          xinsRequest = vhostHandler.convertRequest(httpRequest);
       }
       
       // Store the FunctionRequest, so convertResultImpl can use it
       httpRequest.setAttribute(XINS_REQUEST_ATTR, xinsRequest);
 
       return xinsRequest;
    }
 
    @Override
    protected void convertResultImpl(FunctionResult      xinsResult,
                                     HttpServletResponse httpResponse,
                                     HttpServletRequest  httpRequest)
    throws IOException {
 
       // XXX: Consider changing the approach with the AppCenter class to an
       //      approach where ServletRequest.setAttribute(...) is used, see:
       //      http://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/ServletRequest.html
 
       // Check preconditions
       MandatoryArgumentChecker.check("xinsResult",   xinsResult,
                                      "httpResponse", httpResponse,
                                      "httpRequest",  httpRequest);
 
       // Find the (required) XINS FunctionRequest reference
       FunctionRequest xinsRequest = (FunctionRequest) httpRequest.getAttribute(XINS_REQUEST_ATTR);
       if (xinsRequest == null) {
          throw new IOException("Unable to find org.xins.server.FunctionRequest instance associated with the javax.servlet.HttpServletRequest object.");
       }
 
       // Possibly the HTTP request is wrapped
       Object wrapper = httpRequest.getAttribute(WRAPPED_REQUEST);
       if (wrapper != null) {
          httpRequest = (HttpServletRequest) wrapper;
       }
 
       // TODO: Refactor this. Perhaps split in 2 methods, one for redirects
       //       and one for site-specific requests.
 
       // Handle redirects
       if ("Redirect".equals(xinsRequest.getFunctionName())) {
          httpResponse.setStatus(HttpServletResponse.SC_SEE_OTHER);
          httpResponse.setHeader("Location", xinsRequest.getParameters().get("target"));
          return;
 
       // Handle page not found
       } else if ("NotFound".equals(xinsRequest.getFunctionName())) {
          httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
          return;
 
       // Handle CSV form submissions
       } else if ("GetFormSubmissions".equals(xinsRequest.getFunctionName())) {
          httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
          return;
       }
 
       // Get a reference to the AppCenter
       AppCenter appCenter = AppCenter.get();
 
       // TODO: Assert httpRequest  in the context equals httpRequest
       // TODO: Assert httpResponse in the context equals httpResponse
       // TODO: Assert xinsResult   in the context equals xinsResult
 
       AccessContext   context = appCenter.getContext();
       SiteHandler siteHandler = null;
       String     functionName = xinsRequest.getFunctionName();
 
       // Determine the account
       PropertyReader params = xinsRequest.getParameters();
       String      realmName = params.get("realmName");
       Account       account = null;
       if (realmName != null) {
          Login login = appCenter.getContext().getSession().getLogin(realmName);
          if (login != null) {
             account = login.getAccount();
          }
       }
       Site        site = context.getSite();
       Realm      realm = (account == null) ? null : account.getRealm();
       String accountID = (account == null) ? null : account.getID();
 
       try {
          // Determine the function name
          siteHandler = context.getSiteHandler();
 
          // Serve a static file
          if ("GetFile".equals(functionName)) {
             String path = params.get("path"); // TODO: Assert path matches a certain pattern
 
             // Determine data context (site/realm/account) 
             DataContext dataContext = site;
 
             // Account-specific
             if (account != null && "true".equals(params.get("accountSpecific"))) {
                dataContext = account;
             } else if (account != null) {
                dataContext = realm;
             }
 
             Log.log_8136(path);
             siteHandler.serveFile(httpRequest, httpResponse, dataContext, path);
 
          } else {
             context.set(xinsResult, httpResponse);
             
             // Return form field validation result
             if ("ValidateFormField".equals(functionName)) {
                siteHandler.serveFormFieldValidationResult();
 
             // Serve a dynamic page (possibly transformed)
             } else {
                siteHandler.servePage(httpRequest, httpResponse);
             }
          }
          
       // Handle any exception, mostly ContentAccessException and IOException
       } catch (Exception cause) {
          if (siteHandler != null && siteHandler.getSite().getProperty("ErrorPage") != null) {
             Log.log_8137(cause);
             siteHandler.serveError(httpRequest, httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          } else {
             Utils.logError("Caught exception in YAFFCallingConvention.convertResultImpl method.", cause);
             throw new IOException("Failed to convert result.", cause);
          }
 
       // Clear the context
       } finally {
          appCenter.setContext(null);
       }
    }
 }
