 /*
  * @author <a href="wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.tags.event.impl ;
 
 import org.gridlab.gridsphere.tags.event.FormEvent;
 import org.gridlab.gridsphere.event.ActionEvent;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.tags.web.element.Element;
 import org.gridlab.gridsphere.tags.web.element.NameValueDisableBean;
 import org.gridlab.gridsphere.tags.web.element.CheckBoxBean;
 import org.gridlab.gridsphere.tags.web.element.NameBean;
 
 
 import javax.servlet.http.HttpSession;
 import java.util.*;
 
 public class FormEventImpl implements FormEvent {
 
     protected ActionEvent event;
     protected PortletRequest request;
 
     public FormEventImpl(PortletRequest request) {
         this.request = request;
     }
 
     public FormEventImpl(ActionEvent evt) {
         event = evt;
         request = evt.getPortletRequest();
     }
 
     /**
      * Returns the name of the pressed submit button. To use this for form has to follow the convention
      * that the names of all submit-type buttons start with 'submit:' (and only those and no other elements)
      * @parameter event the actionevent
      * @return name of the button which was pressed
      */
     public String getPressedSubmitButton() {
         String result = new String();
 
         PortletRequest req = event.getPortletRequest();
         Enumeration enum = req.getParameterNames();
         while(enum.hasMoreElements()) {
             String name = (String)enum.nextElement();
             if (name.startsWith("gssubmit:")) {
                 String button = req.getParameter(name);
                 if (button!=null) {
                     result = name.substring(9);
                 }
             }
         }
         return result;
     }
 
 
     private boolean checkParameterName(String name) {
         Enumeration enum = request.getParameterNames();
         while (enum.hasMoreElements()) {
             if ( ((String)enum.nextElement()).equals(name) ) {
                 return true;
             }
         }
         return false;
     }
 
     private Object getBean(String name, PortletRequest request) {
         HttpSession session = request.getSession();
         NameBean bean = (NameBean)session.getAttribute(name);
         return bean;
     }
 
     public Object getElementBean(String name, PortletRequest request) {
         HttpSession session = request.getSession();
         NameBean bean = (NameBean)getBean(name, request);
         System.out.println("Getting Bean "+name+" from Session");
         //if (checkParameterName("gstag:"+bean.getName())) {
             String[] values = request.getParameterValues("gstag:"+bean.getName());
             //if (values.length>0) {
                 System.out.println("Updated bean: "+bean.getName());
                 bean.update(values);
             //}
         //}
         return bean;
     }
 
     public CheckBoxBean getCheckBox(String name) {
         CheckBoxBean checkbox = (CheckBoxBean)getBean(name, request);
         if (checkParameterName("gstag:"+name)) {
             checkbox.setSelected(true);
         } else {
             checkbox.setSelected(false);
         }
         return checkbox;
     }
 
     public Object getElementBean(String name) {
         PortletRequest request = event.getPortletRequest();
         return getElementBean(name, request);
     }
 
     public void printRequestParameter(PortletRequest req) {
         System.out.println("\n\n show request params\n--------------------\n");
         Enumeration enum = req.getParameterNames();
         while (enum.hasMoreElements()) {
             String name = (String)enum.nextElement();
             System.out.println("name :"+name);
             String values[] = req.getParameterValues(name);
             if (values.length == 1) {
                 String pval = values[0];
                 if (pval.length() == 0) {
                     pval = "no value";
                 }
                 System.out.println(" value : "+pval);
             } else {
                 System.out.println(" value :");
                 for (int i=0;i<values.length;i++) {
                     System.out.println("            - "+values[i]);
                 }
 
             }
         }
         System.out.println("--------------------\n");
     }
 }
