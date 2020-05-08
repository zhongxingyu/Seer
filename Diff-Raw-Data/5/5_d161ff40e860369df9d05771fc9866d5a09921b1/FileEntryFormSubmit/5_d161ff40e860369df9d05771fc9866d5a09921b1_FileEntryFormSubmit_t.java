 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.component.fileentry;
 
 import org.icefaces.util.EnvUtils;
 import org.icefaces.impl.event.FormSubmit;
import org.icefaces.impl.context.ICEFacesContextFactory;
 
 import javax.faces.event.SystemEventListener;
 import javax.faces.event.SystemEvent;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIOutput;
 import javax.faces.component.UIForm;
 import java.util.Iterator;
 import java.util.Map;
 import java.io.IOException;
 
 public class FileEntryFormSubmit implements SystemEventListener {
     static final String IFRAME_ID = "hiddenIframe";
     private static final String ID_SUFFIX = "_captureFileOnsubmit";
     private static final String AJAX_FORCED_VIEWS = 
            ICEFacesContextFactory.AJAX_FORCED_VIEWS;
     private boolean partialStateSaving;
 
     public FileEntryFormSubmit()  {
         partialStateSaving = EnvUtils.isPartialStateSaving(
             FacesContext.getCurrentInstance() );
     }
 
     public void processEvent(SystemEvent event) throws AbortProcessingException {
 //System.out.println("FileEntryFormSubmit.processEvent()  event: " + event);
         FacesContext context = FacesContext.getCurrentInstance();
 //System.out.println("FileEntryFormSubmit.processEvent()  phase: " + context.getCurrentPhaseId());
 
         UIForm form = (UIForm) event.getSource();
 //System.out.println("FileEntryFormSubmit.processEvent()  form.clientId: " + form.getClientId(context));
 
         if (!partialStateSaving)  {
             for (UIComponent child : form.getChildren())  {
                 String id = child.getId();
                 if ((null != id) && id.endsWith(ID_SUFFIX))  {
                     return;
                 }
             }
         }
 
         // See if there is at least one FileEntry component in the form,
         // which should alter the form submission method.
         if (!foundFileEntry(form)) {
 //System.out.println("FileEntryFormSubmit  !foundFileEntry");
             return;
         }
 //System.out.println("FileEntryFormSubmit  foundFileEntry!");
 
         forceAjaxOnView(context);
         form.getAttributes().put(FormSubmit.DISABLE_CAPTURE_SUBMIT, "true");
 
         FormScriptWriter scriptWriter = new FormScriptWriter(
             null, "_captureFileOnsubmit");
         form.getChildren().add(0, scriptWriter);
 
         UIOutput output = new UIOutput() {
             public void encodeBegin(FacesContext context) throws IOException {
                 String clientId = getClientId(context);
 //System.out.println("RENDER IFRAME  clientId: " + clientId);
                 ResponseWriter writer = context.getResponseWriter();
                 writer.startElement("iframe", this);
                 writer.writeAttribute("id", clientId, "clientId");
                 writer.writeAttribute("name", clientId, "clientId");
                 writer.writeAttribute("style", "display:none;", "style");
                 writer.writeAttribute("src", "about:blank", "src");
                 writer.endElement("iframe");
             }
             public void encodeEnd(FacesContext context) throws IOException {
             }
         };
         output.setId(IFRAME_ID);
         output.setTransient(true);
         form.getChildren().add(1, output);
     }
     
     private static boolean foundFileEntry(UIComponent parent) {
         Iterator<UIComponent> kids = parent.getFacetsAndChildren();
         while (kids.hasNext()) {
             UIComponent kid = kids.next();
             if (kid instanceof FileEntry) {
                 return true;
             }
             if (foundFileEntry(kid)) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isListenerForSource(Object source) {
         return source instanceof UIForm;
     }
     
     private void forceAjaxOnView(FacesContext facesContext)  {
         //ideally we would force this only for certain views
         //unfortunately the JSF view determinateion logic is not exposed
         //so we can only enable for a given session
         //Once the FileEntry component is used, all subsequent multipart
         //posts will have "Faces-Request: partial/ajax" set 
         ExternalContext externalContext = facesContext.getExternalContext();
         Map sessionMap = externalContext.getSessionMap();
         sessionMap.put(AJAX_FORCED_VIEWS, AJAX_FORCED_VIEWS);
     }
 }
