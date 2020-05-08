 package au.com.sensis.mobile.web.component.clicktocall.showcase.presentation.action;
 
 import java.util.Enumeration;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.apache.struts2.interceptor.ServletRequestAware;
 
 import au.com.sensis.mobile.web.component.clicktocall.model.PhoneOrFax;
 import au.com.sensis.mobile.web.component.clicktocall.showcase.business.PhoneOrFaxFactory;
 import au.com.sensis.mobile.web.component.core.util.XmlHttpRequestDetector;
 import au.com.sensis.mobile.web.testbed.presentation.common.DumbAction;
 
 /**
  * Simple showcase action that indexes into a List of {@link PhoneOrFax} objects
  * and exposes the found object to the current page.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public class CallAction extends DumbAction implements ServletRequestAware {
 
     private static Logger logger = Logger.getLogger(CallAction.class);
 
     /**
      * Result name for a successful ajax request.
      */
     public static final String AJAX_SUCCESS_RESULT = "ajaxSuccess";
 
     private Integer index;
 
     /**
      * JavaScript relies on this being on the request url.
      */
     private String phoneNumber;
 
     private PhoneOrFaxFactory phoneOrFaxFactory;
     private HttpServletRequest httpServletRequest;
     private XmlHttpRequestDetector xmlHttpRequestDetector;
 
 
     /**
      * Returns the {@link PhoneOrFax} corresponding to {@link #getIndex()}.
      *
      * @return the {@link PhoneOrFax} corresponding to {@link #getIndex()}.
      */
     public PhoneOrFax getPhoneOrFax() {
         if (getIndex() != null) {
             return getPhoneOrFaxFactory().createPhoneOrFaxList().get(getIndex());
         } else {
             return getPhoneOrFaxFactory().createDefaultPhone();
         }
     }
 
     /**
      * @return the phoneOrFaxFactory
      */
     private PhoneOrFaxFactory getPhoneOrFaxFactory() {
         return phoneOrFaxFactory;
     }
 
 
     /**
      * @param phoneOrFaxFactory the phoneOrFaxFactory to set
      */
     public void setPhoneOrFaxFactory(final PhoneOrFaxFactory phoneOrFaxFactory) {
         this.phoneOrFaxFactory = phoneOrFaxFactory;
     }
 
     /**
      * @return the index
      */
     public Integer getIndex() {
         return index;
     }
 
     /**
      * @param index the index to set
      */
     public void setIndex(final Integer index) {
         this.index = index;
     }
 
     /**
      * @see {@link ServletRequestAware#setServletRequest(HttpServletRequest)}
      * @param httpServletRequest {@link HttpServletRequest} for the current action
      * instance.
      */
     public void setServletRequest(final HttpServletRequest httpServletRequest) {
         this.httpServletRequest = httpServletRequest;
 
        if (logger.isInfoEnabled()) {
            logger.info("CallAction invoked for request: "
                    + httpServletRequest.getRequestURL() + httpServletRequest.getQueryString());
        }

         if (logger.isDebugEnabled()) {
             logger.debug("headers for request: "
                     + httpServletRequest.getRequestURL());
             final Enumeration<String> headerNames =
                     httpServletRequest.getHeaderNames();
             while (headerNames.hasMoreElements()) {
                 final String headerName = headerNames.nextElement();
                 logger.debug(headerName + ": "
                         + httpServletRequest.getHeader(headerName));
             }
         }
     }
 
     /**
      * @return the httpServletRequest
      */
     public HttpServletRequest getHttpServletRequest() {
         return httpServletRequest;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String execute() {
         if (getXmlHttpRequestDetector().isXmlHttpRequest(getHttpServletRequest())) {
             if (logger.isInfoEnabled()) {
                 logger.info("AJAX reporting call - in a real app, reporting would occur here ...");
             }
             return AJAX_SUCCESS_RESULT;
         } else {
             return super.execute();
         }
     }
 
     /**
      * @return the phoneNumber
      */
     public String getPhoneNumber() {
         return phoneNumber;
     }
 
     /**
      * @param phoneNumber the phoneNumber to set
      */
     public void setPhoneNumber(final String phoneNumber) {
         this.phoneNumber = phoneNumber;
     }
 
     /**
      * Shorthand for {@link #setPhoneNumber(String)}.
      *
      * @param clickToCallPhoneNumber The number being called, in callable format, eg. +61390010001
      * @see #setPhoneNumber(String)
      */
     public void setCtcpn(final String clickToCallPhoneNumber) {
         setPhoneNumber(clickToCallPhoneNumber);
     }
 
     /**
      * @return the xmlHttpRequestDetector
      */
     private XmlHttpRequestDetector getXmlHttpRequestDetector() {
         return xmlHttpRequestDetector;
     }
 
     /**
      * @param xmlHttpRequestDetector the xmlHttpRequestDetector to set
      */
     public void setXmlHttpRequestDetector(
             final XmlHttpRequestDetector xmlHttpRequestDetector) {
         this.xmlHttpRequestDetector = xmlHttpRequestDetector;
     }
 
     /**
      * We only provide this request param setter to avoid Struts warnings. The request
      * param is actually to be implicitly used by {@link #getXmlHttpRequestDetector()}.
      *
      * @param xrw request param set by Struts.
      */
     public void setXrw(final String xrw) {
         // Do nothing.
     }
 }
