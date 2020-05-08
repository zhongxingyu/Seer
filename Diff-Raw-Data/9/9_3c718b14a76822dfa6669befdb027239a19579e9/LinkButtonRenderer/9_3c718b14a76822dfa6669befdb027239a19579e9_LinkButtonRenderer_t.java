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
 
 package org.icefaces.ace.component.linkbutton;
 
 import java.io.IOException;
 import java.util.*;
 import javax.faces.event.ActionEvent;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIParameter;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 
 import org.icefaces.ace.util.*;
 
 import org.icefaces.util.EnvUtils;
 import org.icefaces.render.MandatoryResourceComponent;
 
 import org.icefaces.ace.renderkit.CoreRenderer;
 
 @MandatoryResourceComponent(tagName="linkButton", value="org.icefaces.ace.component.linkbutton.LinkButton")
 public class LinkButtonRenderer extends CoreRenderer {
 	private static String[] excludedAttributes = {"onclick", "onkeydown", "hreflang", "href", "target", "style"};
 
     public void decode(FacesContext facesContext, UIComponent uiComponent) {
         Map requestParameterMap = facesContext.getExternalContext().getRequestParameterMap();
 		LinkButton link  = (LinkButton) uiComponent;
         if (requestParameterMap.containsKey("ice.event.captured")) {
             String source = String.valueOf(requestParameterMap.get("ice.event.captured"));
             String clientId = link.getClientId();
             if (clientId.equals(source)) {
                 try {
                     uiComponent.queueEvent(new ActionEvent(uiComponent));
                 } catch (Exception e) {}
             }
         }
 		
 		decodeBehaviors(facesContext, link);
     }
 
     public void encodeBegin(FacesContext facesContext, UIComponent uiComponent)
             throws IOException {
         LinkButton linkButton = (LinkButton) uiComponent;
         ResponseWriter writer = facesContext.getResponseWriter();
 
         List <UIParameter> uiParams = Utils.captureParameters(linkButton);
         String clientId = uiComponent.getClientId(facesContext);
         String value = (String)linkButton.getValue();
         Integer tabindex = linkButton.getTabindex();
         boolean disabled = linkButton.isDisabled();
         boolean ariaEnabled = EnvUtils.isAriaEnabled(facesContext);
         boolean doAction = (linkButton.getActionListeners().length > 0 ||
                 (linkButton.getActionExpression() != null));
 
         if (ariaEnabled && tabindex == null) tabindex = 0;
 
         writer.startElement(HTML.DIV_ELEM, uiComponent );
         writer.writeAttribute(HTML.ID_ATTR, clientId, null);
         ComponentUtils.enableOnElementUpdateNotify(writer, clientId);
 
         encodeRootStyle(linkButton, writer);
 
         // first span
         writer.startElement(HTML.SPAN_ELEM, uiComponent);
 
         String styleClass = "yui-button yui-link-button ui-button ui-widget";
         if (disabled)
             styleClass += " yui-button-disabled yui-link-button-disabled";
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, null);
 
         // second span but "first-child"- ugh.
         writer.startElement(HTML.SPAN_ELEM, uiComponent);
         writer.writeAttribute(HTML.CLASS_ATTR, "first-child", null);
         if (ariaEnabled) encodeAriaAttributes(writer, linkButton, doAction);
 
         // button element
         writer.startElement(HTML.ANCHOR_ELEM, uiComponent);
 
         if (tabindex != null)
             writer.writeAttribute(HTML.TABINDEX_ATTR, tabindex, null);
 
         renderPassThruAttributes(facesContext, linkButton, HTML.LINK_ATTRS, excludedAttributes);
 
         if (!disabled) {
             encodeScript(facesContext, writer, HTML.ONFOCUS_ATTR,
                          linkButton, uiParams, clientId, doAction);
             encodeHref(linkButton, writer, uiParams);
         } else
            writer.writeAttribute(HTML.CLASS_ATTR, "ui-state-disabled", null);
 
         writer.write(value);
 
         writer.endElement(HTML.ANCHOR_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
         if (!disabled) encodeScript(facesContext, writer, HTML.ONMOUSEOVER_ATTR,
                                     linkButton, uiParams, clientId, doAction);
         writer.endElement(HTML.DIV_ELEM);
     }
 
     private void encodeHref(LinkButton linkButton, ResponseWriter writer, List<UIParameter> uiParams) throws IOException {
         String target = linkButton.getTarget();
         String hrefLang = linkButton.getHrefLang();
         String href = linkButton.getHref();
         String paramString = uiParams != null
                 ? "?"+ Utils.asParameterString(uiParams)
                 : "";
 
         if (href != null) {
             href += paramString;
             writer.writeAttribute(HTML.HREF_ATTR, href, null );
         }
 
         if (hrefLang != null) {
             writer.writeAttribute(HTML.HREFLANG_ATTR, hrefLang , null );
         }
 
         if (target != null) {
             writer.writeAttribute(HTML.TARGET_ATTR, target, null );
         }
     }
 
     private void encodeAriaAttributes(ResponseWriter writer, LinkButton button, boolean doAction) throws IOException {
         writer.writeAttribute(HTML.ROLE_ATTR, doAction ? "button" : "link", null);
         writer.writeAttribute(HTML.ARIA_LABELLED_BY_ATTR, (String)button.getValue(), null);
 
         if (button.isDisabled()) {
             writer.writeAttribute(HTML.ARIA_DISABLED_ATTR, true, null);
         }
 
         if (doAction) {
             writer.writeAttribute(HTML.ARIA_DESCRIBED_BY_ATTR, "JSF action event source", null);
         } else {
             writer.writeAttribute(HTML.ARIA_DESCRIBED_BY_ATTR, "Standard HTML anchor", null);
         }
 
     }
 
     private void encodeRootStyle(LinkButton linkButton, ResponseWriter writer) throws IOException {
         String styleClass = linkButton.getStyleClass();
         String styleClassVal = "";
         if (styleClass != null && styleClass.trim().length() > 0) {
             styleClassVal = " " + styleClass;
         }
         writer.writeAttribute(HTML.CLASS_ATTR, "ice-linkbutton" + styleClassVal, null);
         String style = linkButton.getStyle();
         if (style != null && style.trim().length() > 0) {
             writer.writeAttribute(HTML.STYLE_ATTR, style, HTML.STYLE_ATTR);
         }
     }
 
     private void encodeScript(FacesContext facesContext, ResponseWriter writer,
                               String eventAttr, LinkButton linkButton,
                               List<UIParameter> uiParams, String clientId,
                               boolean doAction) throws IOException {
         JSONBuilder json = JSONBuilder.create()
                                       .beginFunction("ice.ace.lazy")
                                       .item("linkButton")
                                       .beginArray()
                                       .item(clientId)
                                       .beginMap()
                                       .entry("hasAction", doAction);
 
         encodeClientBehaviors(facesContext, linkButton, json);
 
         if (doAction && uiParams != null) {
             json.beginMap("uiParams");
             for (UIParameter param : uiParams)
                 json.entry(param.getName(), param.getValue().toString());
             json.endMap();
         }
 
         json.endMap().endArray().endFunction();
 
         writer.writeAttribute(eventAttr, json.toString(), null);
     }
 }
