 /*
  * Copyright 2013 Mobile Helix, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.primefaces.mobile.filters;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.PhaseEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.event.PhaseListener;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.primefaces.mobile.model.LoadCommandAction;
 
 /**
  *
  * @author shallem
  */
 public class LoadCommandListener implements PhaseListener {
     @Override
     public PhaseId getPhaseId() {
         return PhaseId.RESTORE_VIEW;
     }
 
     @Override
     public void afterPhase(PhaseEvent event) {
         
     }
 
     @Override
     public void beforePhase(PhaseEvent event) {
         FacesContext fc = event.getFacesContext();
         ExternalContext ectx = fc.getExternalContext();
         if (ectx.isResponseCommitted()) {
             // response is done ...
             return;
         }
         
         HttpServletRequest req = (HttpServletRequest)ectx.getRequest();
         HttpServletResponse response = (HttpServletResponse)ectx.getResponse();
         
         String loadKey = req.getParameter("__hxLoadKey");
         if (loadKey != null) {
             LoadCommandAction lca = (LoadCommandAction)req.getServletContext().getAttribute(loadKey);
             
             /* Run the load command, then get the result. */
             //Object thisObj = ectx.getRequestMap().get(lca.getBeanName()); 
                     //lca.doLoad();
             Object thisObj = fc.getApplication().evaluateExpressionGet(fc, "#{" + lca.getBeanName() + "}", lca.getBeanClass());
             try {
                 lca.doLoad(thisObj);
                 String jsonToReturn = lca.getAndSerialize(thisObj);
                 response.getWriter().write(jsonToReturn);
                 response.flushBuffer();
             } catch(IOException ioe) {
                 try {
                     response.getWriter().write("{ 'error' : '" + ioe.getMessage() + "' }");
                     response.flushBuffer();
                 } catch (IOException ex) {
                     Logger.getLogger(LoadCommandListener.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
            
            response.setContentType("application/json");
             fc.responseComplete();
         }
     }
 
 }
