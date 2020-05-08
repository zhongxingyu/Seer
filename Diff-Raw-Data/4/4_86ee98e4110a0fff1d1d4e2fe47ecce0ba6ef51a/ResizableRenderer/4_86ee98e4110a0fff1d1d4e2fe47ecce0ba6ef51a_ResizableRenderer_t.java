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
 package org.icefaces.ace.component.resizable;
 
 import java.io.IOException;
 import java.util.Map;
 import javax.faces.FacesException;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIGraphic;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.icefaces.ace.event.ResizeEvent;
 
 import org.icefaces.ace.renderkit.CoreRenderer;
 import org.icefaces.ace.util.ComponentUtils;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.render.MandatoryResourceComponent;
 
 @MandatoryResourceComponent(tagName="resizable", value="org.icefaces.ace.component.resizable.Resizable")
 public class ResizableRenderer extends CoreRenderer {
 
     @Override
     public void decode(FacesContext context, UIComponent component) {
         Map<String, String> params = context.getExternalContext().getRequestParameterMap();
         Resizable resizable = (Resizable) component;
         String clientId = resizable.getClientId(context);
 
         if(params.containsKey(clientId + "_ajaxResize")) {
            int width = (new Double(params.get(clientId + "_width"))).intValue();;
            int height = (new Double(params.get(clientId + "_height"))).intValue();;
             
             resizable.queueEvent(new ResizeEvent(resizable, width, height));
         }
         decodeBehaviors(context, resizable);
     }
 
     @Override
 	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		Resizable resizable = (Resizable) component;
         String clientId = resizable.getClientId(context);
 		UIComponent target = findTarget(context, resizable);
         String targetId = target.getClientId(context);
 
 		writer.startElement("script", resizable);
 		writer.writeAttribute("type", "text/javascript", null);
 
         //If it is an image wait until the image is loaded
         if(target instanceof UIGraphic)
             writer.write("ice.ace.jq(ice.ace.escapeClientId('" + targetId + "')).load(function(){");
         else
             writer.write("ice.ace.jq(function(){");
 		
 		writer.write(this.resolveWidgetVar(resizable) + " = new ");
 		
 		JSONBuilder jb = JSONBuilder.create();
 		jb.beginFunction("ice.ace.Resizable")
 			.item(clientId)
 			.beginMap()
 				.entry("target", targetId);
 
         //Boundaries
 		int minWidth = resizable.getMinWidth();
         if(minWidth != Integer.MIN_VALUE) jb.entry("minWidth", minWidth);
 		int maxWidth = resizable.getMaxWidth();
         if(maxWidth != Integer.MAX_VALUE) jb.entry("maxWidth", maxWidth);
 		int minHeight = resizable.getMinHeight();
         if(minHeight != Integer.MIN_VALUE) jb.entry("minHeight", minHeight);
 		int maxHeight = resizable.getMaxHeight();
         if(maxHeight != Integer.MAX_VALUE) jb.entry("maxHeight", maxHeight);
 
         //Animation
         if(resizable.isAnimate()) {
             jb.entry("animate", true);
             jb.entry("animateEasing", resizable.getEffect());
             jb.entry("animateDuration", resizable.getEffectDuration());
         }
 
         //Config
         if(resizable.isProxy()) jb.entry("helper", "ui-resizable-proxy");
 		String handles = resizable.getHandles();
         if(handles != null) jb.entry("handles", handles);
 		int grid = resizable.getGrid();
         if(grid != 1) jb.entry("grid", grid);
         if(resizable.isAspectRatio()) jb.entry("aspectRatio", true);
         if(resizable.isGhost()) jb.entry("ghost", true);
         if(resizable.isContainment()) jb.entry("containment", "ice.ace.escapeClientId('" + resizable.getParent().getClientId(context) +"')", true);
 
         //Client side callbacks
 		String onStart = resizable.getOnStart();
         if(onStart != null) jb.entry("onStart", "function(event, ui) {" + onStart + "}", true);
 		String onResize = resizable.getOnResize();
         if(onResize != null) jb.entry("onResize", "function(event, ui) {" + onResize + "}", true);
 		String onStop = resizable.getOnStop();
         if(onStop != null) jb.entry("onStop", "function(event, ui) {" + onStop + "}", true);
 
         //Ajax resize
         if(resizable.getResizeListener() != null) {
             jb.entry("ajaxResize", true);
 
             String onResizeUpdate = resizable.getOnResizeUpdate();
             if(onResizeUpdate != null)
                 jb.entry("onResizeUpdate", ComponentUtils.findClientIds(context, resizable, onResizeUpdate));
         }
 		
         encodeClientBehaviors(context, resizable, jb);
 		jb.endMap().endFunction();
 		writer.write(jb.toString());
 		
 		writer.write("});");
 		
 		writer.endElement("script");
 	}
 
     protected UIComponent findTarget(FacesContext context, Resizable resizable) {
         String _for = resizable.getFor();
 
         if (_for != null) {
             UIComponent component = resizable.findComponent(_for);
             if (component == null)
                 throw new FacesException("Cannot find component \"" + _for + "\" in view.");
             else
                 return component;
         } else {
             return resizable.getParent();
         }
     }
 }
