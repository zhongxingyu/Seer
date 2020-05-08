 /*
  * Original Code developed and contributed by Prime Technology.
  * Subsequent Code Modifications Copyright 2011 ICEsoft Technologies Canada Corp. (c)
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
  *
  * NOTE THIS CODE HAS BEEN MODIFIED FROM ORIGINAL FORM
  *
  * Subsequent Code Modifications have been made and contributed by ICEsoft Technologies Canada Corp. (c).
  *
  * Code Modification 1: Integrated with ICEfaces Advanced Component Environment.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  *
  * Code Modification 2: (ICE-6978) Used JSONBuilder to add the functionality of escaping JS output.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  */
 package org.icefaces.ace.component.ajax;
 
 import javax.faces.component.ActionSource;
 import javax.faces.component.EditableValueHolder;
 import javax.faces.component.UIComponent;
 import javax.faces.component.behavior.ClientBehavior;
 import javax.faces.component.behavior.ClientBehaviorContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AjaxBehaviorEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.render.ClientBehaviorRenderer;
 import javax.faces.render.FacesBehaviorRenderer;
 
 import org.icefaces.ace.util.ComponentUtils;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.render.MandatoryResourceComponent;
 import org.icefaces.ace.api.IceClientBehaviorHolder;
 
 @MandatoryResourceComponent(tagName="ajax", value="org.icefaces.ace.component.ajax.AjaxBehavior")
 @FacesBehaviorRenderer(rendererType="org.icefaces.ace.component.AjaxBehaviorRenderer")
 public class AjaxBehaviorRenderer extends ClientBehaviorRenderer {
 
     @Override
     public void decode(FacesContext context, UIComponent component, ClientBehavior behavior) {
         AjaxBehavior ajaxBehavior = (AjaxBehavior) behavior;
 
         if(!ajaxBehavior.isDisabled()) {
             AjaxBehaviorEvent event = new AjaxBehaviorEvent(component, behavior);
 
             PhaseId phaseId = isImmediate(component, ajaxBehavior) ? PhaseId.APPLY_REQUEST_VALUES : PhaseId.INVOKE_APPLICATION;
 
             event.setPhaseId(phaseId);
 
             component.queueEvent(event);
         }
     }
 
     @Override
     public String getScript(ClientBehaviorContext behaviorContext, ClientBehavior behavior) {
         AjaxBehavior ajaxBehavior = (AjaxBehavior) behavior;
         if(ajaxBehavior.isDisabled()) {
             return null;
         }
         
         FacesContext fc = behaviorContext.getFacesContext();
         UIComponent component = behaviorContext.getComponent();
         String clientId = component.getClientId(fc);
         String source = behaviorContext.getSourceId();
 
         JSONBuilder jb = JSONBuilder.create();
 		jb.beginFunction("ice.ace.ab");
 
         //source
 		jb.beginMap()
 			.entry("source", source);
 		
         //execute
 		String execute = null;
         if (ajaxBehavior.getExecute() != null) {
 			execute = ajaxBehavior.getExecute();
 		} else {
 			if (component instanceof IceClientBehaviorHolder) {
 				execute = ((IceClientBehaviorHolder) component).getDefaultExecute(behaviorContext.getEventName());
 			}
 		}
 		if (execute == null || "".equals(execute)) {
			jb.entry("execute", "(arguments[1]) ? arguments[1] + ' ' + " + clientId + " : " + clientId);
 		} else {
             String clientIds = ComponentUtils.findClientIds(fc, component, execute);
			jb.entry("execute", "(arguments[1]) ? arguments[1] + ' ' + " + clientIds + " : " + clientIds);
 		}
 
         //render
 		String render = null;
         if (ajaxBehavior.getRender() != null) {
             render = ajaxBehavior.getRender();
         } else {
 			if (component instanceof IceClientBehaviorHolder) {
 				render = ((IceClientBehaviorHolder) component).getDefaultRender(behaviorContext.getEventName());
 			}
 			if (render == null || "".equals(render)) {
 				render = "@all";
 			}
 		}
         String clientIds = ComponentUtils.findClientIds(fc, component, render);
		jb.entry("render", "(arguments[2]) ? arguments[2] + ' ' + " + clientIds + " : " + clientIds);
 
         //behavior event
 		jb.entry("event", behaviorContext.getEventName());
 
         //async
         if(ajaxBehavior.isAsync())
             jb.entry("async", true);
 
         //global
         if(!ajaxBehavior.isGlobal())
             jb.entry("global", false);
 
         //callbacks
         if(ajaxBehavior.getOnstart() != null)
             jb.entry("onstart", "function(xhr){" + ajaxBehavior.getOnstart() + ";}", true);
         if(ajaxBehavior.getOnerror() != null)
             jb.entry("onerror", "function(xhr, status, error){" + ajaxBehavior.getOnerror() + ";}", true);
         if(ajaxBehavior.getOnsuccess() != null)
             jb.entry("onsuccess", "function(data, status, xhr, args){" + ajaxBehavior.getOnsuccess() + ";}", true);
         if(ajaxBehavior.getOncomplete() != null)
             jb.entry("oncomplete", "function(xhr, status, args){" + ajaxBehavior.getOncomplete() + ";}", true);
 
         //params
         jb.entry("params", "arguments[0]", true);
 		
 		jb.endMap().endFunction();
 
         return jb.toString();
     }
 
     private boolean isImmediate(UIComponent component, AjaxBehavior ajaxBehavior) {
         boolean immediate = false;
 
         if(ajaxBehavior.isImmediateSet()) {
             immediate = ajaxBehavior.isImmediate();
         } else if(component instanceof EditableValueHolder) {
             immediate = ((EditableValueHolder)component).isImmediate();
         } else if(component instanceof ActionSource) {
             immediate = ((ActionSource)component).isImmediate();
         }
 
         return immediate;
     }
 }
