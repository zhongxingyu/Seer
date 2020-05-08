 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.event.jsr.impl;
 
 import org.gridlab.gridsphere.provider.event.jsr.RenderFormEvent;
 import org.gridlab.gridsphere.provider.event.impl.BaseFormEventImpl;
 
 import javax.portlet.RenderResponse;
 import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * An <code>ActionEvent</code> is sent by the portlet container when an HTTP request is
  * received that is associated with an action.
  */
 public class RenderFormEventImpl extends BaseFormEventImpl implements RenderFormEvent {
 
     private RenderRequest request;
     private RenderResponse response;
 
     /**
      * Constructs an instance of RenderFormEventImpl given a render request and response
      *
      * @param request the <code>RenderRequest</code>
      * @param response the <code>RenderResponse</code>
      * @param tagBeans a collection of tag beans
      */
     public RenderFormEventImpl(RenderRequest request, RenderResponse response, Map tagBeans) {
        super((HttpServletRequest)request, (HttpServletResponse)response);
         this.request = request;
         this.response = response;
         this.tagBeans = tagBeans;
             // Unless tagBeans is null, don't recreate them
             if (tagBeans == null) {
                 tagBeans = new HashMap();
                createTagBeans((HttpServletRequest)request);
             }
             printRequestParameters();
 
             printTagBeans();
     }
 
     /**
      * Return the render request associated with this render event
      *
      * @return the <code>RenderRequest</code>
      */
     public RenderRequest getRenderRequest() {
         return request;
     }
 
     /**
      * Return the render response associated with this render event
      *
      * @return the <code>RenderResponse</code>
      */
     public RenderResponse getRenderResponse() {
         return response;
     }
 
 }
