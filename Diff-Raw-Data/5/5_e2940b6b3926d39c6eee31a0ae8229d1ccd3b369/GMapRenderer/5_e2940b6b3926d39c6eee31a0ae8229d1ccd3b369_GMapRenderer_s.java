 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
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
 
 package org.icefaces.ace.component.gmap;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.PhaseId;
 
 import org.icefaces.ace.component.ajax.AjaxBehavior;
 import org.icefaces.ace.renderkit.CoreRenderer;
 import org.icefaces.ace.util.ComponentUtils;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.render.MandatoryResourceComponent;
 
 @MandatoryResourceComponent(tagName="gMap", value="org.icefaces.ace.component.gmap.GMap")
 public class GMapRenderer extends CoreRenderer {
 
 
 	    public void encodeBegin(FacesContext context, UIComponent component)
 	            throws IOException {
             ResponseWriter writer = context.getResponseWriter();
             String clientId = component.getClientId(context);
             GMap gmap = (GMap) component;
             writer.startElement("div", null);
             writer.writeAttribute("id", clientId + "_wrapper", null);
             writer.writeAttribute("class", "ice-ace-gmap " + gmap.getStyleClass(), null);
             writer.writeAttribute("style", gmap.getStyle(), null);
             writer.startElement("div", null);
             writer.writeAttribute("id", clientId, null);
             writer.writeAttribute("style", "height:100%; width:100%", null);
             writer.endElement("div");
             writer.endElement("div");
 			writer.startElement("span", null);
 			writer.writeAttribute("id", clientId + "_script", null);
 			writer.startElement("script", null);
 			writer.writeAttribute("type", "text/javascript", null);
 			writer.write("ice.ace.jq(function() {");
             if ((gmap.isLocateAddress() || !gmap.isIntialized()) && (gmap.getAddress() != null && gmap.getAddress().length() > 2))
 				writer.write("ice.ace.gMap.locateAddress('" + clientId + "', '" + gmap.getAddress() + "');");
 			else
 				writer.write("ice.ace.gMap.getGMapWrapper('" + clientId +"').getRealGMap().setCenter(new google.maps.LatLng("+ gmap.getLatitude() + "," + gmap.getLongitude() + "));");
             writer.write("ice.ace.gMap.getGMapWrapper('" + clientId +"').getRealGMap().setZoom(" + gmap.getZoomLevel() + ");");
 			writer.write("ice.ace.gMap.setMapType('" + clientId + "','" + gmap.getType().toUpperCase() + "');");
 			if (gmap.getOptions() != null && gmap.getOptions().length() > 1)
 				writer.write("ice.ace.gMap.addOptions('" + clientId +"',\"" + gmap.getOptions() + "\");");
             writer.write("});");
 			writer.endElement("script");
 			writer.endElement("span");
             gmap.setIntialized(true);
 	    }

     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         if (context == null || component == null) {
             throw new NullPointerException();
         }
         if (component.getChildCount() == 0) return;
         Iterator kids = component.getChildren().iterator();
         while (kids.hasNext()) {
             UIComponent kid = (UIComponent) kids.next();
             kid.encodeBegin(context);
             if (kid.getRendersChildren()) {
                 kid.encodeChildren(context);
             }
             kid.encodeEnd(context);
         }
 
     }
 
     private void addHiddenField(FacesContext context,
                                 String clientId,
                                 String name) throws IOException {
         addHiddenField(context, clientId, name, null);
     }
 
 
     private void addHiddenField(FacesContext context,
                                 String clientId,
                                 String name,
                                 String value) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         writer.startElement("div", null);
         writer.writeAttribute("id", clientId + name, null);
         writer.writeAttribute("name", clientId + name, null);
         writer.writeAttribute("type", "hidden", null);
         if (value != null) {
             writer.writeAttribute("value", value, null);
         }
         writer.endElement("div");
     }
 
     @Override
     public boolean getRendersChildren() {
         return true;
     }
 }
