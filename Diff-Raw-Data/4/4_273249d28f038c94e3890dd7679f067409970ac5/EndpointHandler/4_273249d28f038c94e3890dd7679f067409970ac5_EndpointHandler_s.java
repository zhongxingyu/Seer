 package org.codehaus.xfire.handler;
 
 import org.codehaus.xfire.MessageContext;
 
 /**
  * By virtue of XFire being stream based, a service can not write its
  * response until the very end of processing. So a service which needs
  * to write response headers but do so first before writing the 
  * SOAP Body.  The writeResponse method tells an Endpoint that it is
  * now okay (i.e. there have been no Faults) to write the 
  * response to the OutputStream (if there is an response to the 
  * sender at all) or to another endpoint.
  * <p>
  * If a Service does not wishes to write its response immediately when
  * reading the incoming stream, it may do so and not implement the
  * <code>writeResponse</code> method. The service must then realize that
  * the response Handler pipeline will not be able to outgoing stream.
  *  
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public interface EndpointHandler
 	extends Handler
 {
    public void writeResponse(MessageContext context);
 }
