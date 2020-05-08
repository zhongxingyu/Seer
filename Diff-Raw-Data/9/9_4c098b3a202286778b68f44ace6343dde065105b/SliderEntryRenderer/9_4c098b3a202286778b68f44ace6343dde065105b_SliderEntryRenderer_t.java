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
  * Code Modification 2: [ADD BRIEF DESCRIPTION HERE]
  * Contributors: ______________________
  * Contributors: ______________________
  */
 package org.icefaces.ace.component.sliderentry;
 
 import java.io.IOException;
 import java.util.Map;
 import javax.el.MethodExpression;
 import javax.faces.FacesException;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.icefaces.ace.event.SlideEndEvent;
 
 import org.icefaces.ace.renderkit.CoreRenderer;
 import org.icefaces.ace.util.ComponentUtils;
 import org.icefaces.render.MandatoryResourceComponent;
 
 import javax.faces.event.ValueChangeEvent;
 
 @MandatoryResourceComponent(tagName="sliderEntry", value="org.icefaces.ace.component.sliderentry.SliderEntry")
 public class SliderEntryRenderer extends CoreRenderer{
 
     @Override
     public void decode(FacesContext context, UIComponent component) {
         Map<String,String> requestParameterMap = context.getExternalContext().getRequestParameterMap();
         SliderEntry slider = (SliderEntry) component;
         String clientId = slider.getClientId(context);
 		
         boolean foundInMap = false;
         if (requestParameterMap.containsKey(clientId+"_hidden")) {
         	foundInMap = true;
         }
         
         //"ice.event.captured" should be holding the event source id
         if (requestParameterMap.containsKey("ice.event.captured")
         		|| foundInMap) {
 
             String source = String.valueOf(requestParameterMap.get("ice.event.captured"));
 
             if ("ice.ser".equals(requestParameterMap.get("ice.submit.type"))) {
                 context.renderResponse();
                 return; 
             }
             Object hiddenValue = requestParameterMap.get(clientId+"_hidden");
             if (hiddenValue == null) return;
 			int submittedValue = 0;
             try {
                 submittedValue = Float.valueOf(hiddenValue.toString()).intValue(); //Integer.valueOf(hiddenValue.toString());
                 //log.finer("Decoded slider value [id:value] [" + clientId + ":" + hiddenValue + "]" );
             } catch (NumberFormatException nfe) {
                 //log.warning("NumberFormatException  Decoding value for [id:value] [" + clientId + ":" + hiddenValue + "]");
             } catch (NullPointerException npe) {
                 //log.warning("NullPointerException  Decoding value for [id:value] [" + clientId + ":" + hiddenValue + "]");
             }
 
             //If I am a source of event?
             if (clientId.equals(source) || foundInMap) {
                 try {
                     int value = slider.getValue();
                     //if there is a value change, queue a valueChangeEvent    
                     if (value != submittedValue) { 
                         component.queueEvent(new ValueChangeEvent (component, 
                                        new Integer(value), new Integer(submittedValue)));
                     }
                 } catch (NumberFormatException e) {
                     e.printStackTrace();
                 }
             }
         }
 		
 		decodeBehaviors(context, slider);
     }
 
     @Override
 	public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException {
 		SliderEntry slider = (SliderEntry) component;
 		
 		encodeMarkup(facesContext, slider);
 		encodeScript(facesContext, slider);
 	}
 	
 	protected void encodeMarkup(FacesContext context, SliderEntry slider) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		String clientId = slider.getClientId(context);
 		
 		writer.startElement("div", slider);
 		writer.writeAttribute("id", clientId , "id");
 		String style = slider.getStyle();
 		style = style == null ? "" : style;
 		String length = slider.getLength();
 		if (length.toLowerCase().indexOf("px") == -1) {
 			length += "px";
 		}
		String dimension = "y".equals(slider.getAxis()) ? "height" : "width";
		style += ";" + dimension + ":" + length + ";";
 		writer.writeAttribute("style", style , null);
 		if(slider.getStyleClass() != null) writer.writeAttribute("class", slider.getStyleClass(), null);
 		
 		writer.endElement("div");
 		writer.startElement("input", slider);
 		writer.writeAttribute("id", clientId + "_hidden" , "id");
 		writer.writeAttribute("name", clientId + "_hidden" , "name");
 		writer.writeAttribute("type", "hidden" , "type");
 		writer.writeAttribute("value", slider.getValue() , "value");
 		writer.endElement("input");
 	}
 
 	protected void encodeScript(FacesContext context, SliderEntry slider) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		String clientId = slider.getClientId(context);
 
 		writer.startElement("script", slider);
 		writer.writeAttribute("type", "text/javascript", null);
 
 		writer.write(this.resolveWidgetVar(slider) + " = new ice.ace.Slider('" + clientId + "', {");
 		writer.write("value:" + slider.getValue());
 		writer.write(",input:'" + clientId + "_hidden" + "'");
 		int min = slider.getMin();
 		writer.write(",min:" + min);
 		int max = slider.getMax();
 		writer.write(",max:" + max);
 		writer.write(",animate:" + slider.isAnimate());
 		float stepPercent = slider.getStepPercent();
 		float step = ((max - min) * stepPercent) / (float) 100;
 		writer.write(",step:" + step);
 		String orientation = "y".equals(slider.getAxis()) ? "vertical" : "horizontal";
 		writer.write(",orientation:'" + orientation + "'");
		String length = slider.getLength();
		if (length.toLowerCase().indexOf("px") == -1) {
			length += "px";
		}
		writer.write(",length:'" + length + "'");
 		
 		Integer tabindex = slider.getTabindex();
 		if (tabindex != null) {
 			writer.write(",tabindex:" + tabindex.toString());
 		}
 		writer.write(",clickableRail:" + slider.isClickableRail());
 		
 		if(slider.isDisabled()) writer.write(",disabled:true");
         if(slider.getOnSlideStart() != null) writer.write(",onSlideStart:function(event, ui) {" + slider.getOnSlideStart() + "}");
         if(slider.getOnSlide() != null) writer.write(",onSlide:function(event, ui) {" + slider.getOnSlide() + "}");
         if(slider.getOnSlideEnd() != null) writer.write(",onSlideEnd:function(event, ui) {" + slider.getOnSlideEnd() + "}");
 		
 		encodeClientBehaviors(context, slider);
 		
 		writer.write("});");
 	
 		writer.endElement("script");
 	}
 }
