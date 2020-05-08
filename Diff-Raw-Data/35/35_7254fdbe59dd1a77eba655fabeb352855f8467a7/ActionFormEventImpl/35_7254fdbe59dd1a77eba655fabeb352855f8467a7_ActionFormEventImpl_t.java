 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.event.jsr.impl;
 
 import org.gridlab.gridsphere.provider.event.jsr.ActionFormEvent;
 import org.gridlab.gridsphere.provider.event.impl.BaseFormEventImpl;
 import org.gridlab.gridsphere.portlet.DefaultPortletAction;
import org.gridlab.gridsphere.portlet.jsrimpl.PortletRequestImpl;
import org.gridlab.gridsphere.portlet.jsrimpl.PortletResponseImpl;
import org.gridlab.gridsphere.portlet.jsrimpl.ActionRequestImpl;
import org.gridlab.gridsphere.portlet.jsrimpl.ActionResponseImpl;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
 
 /**
  * An <code>ActionEvent</code> is sent by the portlet container when an HTTP request is
  * received that is associated with an action.
  */
 public class ActionFormEventImpl extends BaseFormEventImpl implements ActionFormEvent {
 
     private DefaultPortletAction action;
     private ActionRequest request;
     private ActionResponse response;
 
     /**
      * Constructs an instance of ActionEventImpl given an action, request and response
      *
      * @param action a <code>DefaultPortletAction</code>
      * @param request the <code>PortletRequest</code>
      * @param response the <code>PortletResponse</code>
      */
     public ActionFormEventImpl(DefaultPortletAction action, ActionRequest request, ActionResponse response) {
        super((ActionRequestImpl)request, (ActionResponseImpl)response);
         this.action = action;
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
      * Returns the action that this action event carries.
      *
      * @return the portlet action
      */
     public DefaultPortletAction getAction() {
         return action;
     }
 
     /**
      * Returns the action that this action event carries.
      *
      * @return the portlet action
      */
     public String getActionString() {
         return action.getName();
     }
 
     /**
      * Return the action request associated with this action event
      *
      * @return the <code>PortletRequest</code>
      */
     public ActionRequest getActionRequest() {
         return request;
     }
 
     /**
      * Return the action response associated with this action event
      *
      * @return the <code>PortletResponse</code>
      */
     public ActionResponse getActionResponse() {
         return response;
     }
 
 }
