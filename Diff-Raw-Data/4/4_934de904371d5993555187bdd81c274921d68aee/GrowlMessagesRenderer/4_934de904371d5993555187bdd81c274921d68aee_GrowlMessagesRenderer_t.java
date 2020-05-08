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
 package org.icefaces.ace.component.growlmessages;
 
 import org.icefaces.ace.util.ComponentUtils;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.render.MandatoryResourceComponent;
import org.icefaces.impl.util.DOMUtils;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.application.ProjectStage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.render.Renderer;
 import java.io.IOException;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 @MandatoryResourceComponent(tagName = "growlMessages", value = "org.icefaces.ace.component.growlmessages.GrowlMessages")
 public class GrowlMessagesRenderer extends Renderer {
 
     private static final String[] icons = new String[]{"info", "notice", "alert", "alert"};
     private static final String[] states = new String[]{"highlight", "highlight", "error", "error"};
     private static final Set<String> effectSet = new HashSet<String>(Arrays.asList("blind", "bounce", "clip", "drop", "explode", "fade", "fold", "highlight", "puff", "pulsate", "scale", "shake", "size", "slide"));
     private static final Set<String> durationSet = new HashSet<String>(Arrays.asList("slow", "_default", "fast"));
     private static final Set<String> positionSet = new HashSet<String>(Arrays.asList("top-left", "top-right", "bottom-left", "bottom-right"));
     private static final Set<String> glueSet = new HashSet<String>(Arrays.asList("after", "before"));
     private static final Map<String, Integer> severityMap = new HashMap<String, Integer>() {
         {
             put("false", -1);
             put("info", 0);
             put("warn", 1);
             put("error", 2);
             put("fatal", 3);
             put("true", 4);
         }
 
         private static final long serialVersionUID = -8266894485504175957L;
     };
     private static final Logger logger = Logger.getLogger(GrowlMessagesRenderer.class.getName());
 
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
 
         ResponseWriter writer = context.getResponseWriter();
         GrowlMessages messages = (GrowlMessages) component;
         String forId = (forId = messages.getFor()) == null ? "@all" : forId.trim();
         Iterator messageIter;
 //        String style = messages.getStyle();
 //        String styleClass = (styleClass = messages.getStyleClass()) == null ? "" : " " + styleClass;
         String sourceMethod = "encodeEnd";
 
         if (forId.equals("@all")) {
             messageIter = messages.isGlobalOnly() ? context.getMessages(null) : context.getMessages();
         } else {
             UIComponent forComponent = forId.equals("") ? null : messages.findComponent(forId);
             if (forComponent == null) {
                 logger.logp(Level.WARNING, logger.getName(), sourceMethod, "'for' attribute value cannot be empty or non-existent id.");
                 messageIter = Collections.emptyList().iterator();
             } else {
                 messageIter = context.getMessages(forComponent.getClientId(context));
             }
         }
         writer.startElement("span", messages);
         String clientId = messages.getClientId();
         writer.writeAttribute("id", messages.getClientId(), "id");
         ComponentUtils.enableOnElementUpdateNotify(writer, clientId);
 
         writer.startElement("script", component);
         writer.writeAttribute("type", "text/javascript", null);
 
         JSONBuilder jb = JSONBuilder.create();
         jb.beginFunction("ice.ace.create").item("GrowlMessages").beginArray().item(clientId).beginMap();
 
         int pool = messages.getMaxVisibleMessages();
         int life = messages.getDisplayDuration();
 /*
         String showEffect = messages.getShowEffect();
         String hideEffect = messages.getHideEffect();
 */
         String position = messages.getPosition();
         String glue = messages.getMessageOrder();
         jb.entry("pool", pool > 0 ? pool : 0)
                 .entry("header", messages.getHeader())
                 .entry("group", messages.getMessageStyleClass())
                 .entry("position", positionSet.contains(position) ? position : "top-right")
                 .entry("glue", glueSet.contains(glue) ? glue : "after")
                 .entry("life", life > 0 ? life : 3000)
                 .entry("closer", messages.isCloseAll())
 /*
                 .entry("showEffect", effectSet.contains(showEffect) ? showEffect : "fade")
                 .entry("hideEffect", effectSet.contains(hideEffect) ? hideEffect : "fade")
 */
         ;
         durationEntry(jb, "openDuration", messages.getShowEffectDuration());
         durationEntry(jb, "closeDuration", messages.getHideEffectDuration());
 
         jb.beginArray("msgs");
         writer.write(jb.toString());
         jb = JSONBuilder.create();
         while (messageIter.hasNext()) {
             FacesMessage facesMessage = (FacesMessage) messageIter.next();
             if (!facesMessage.isRendered() || messages.isRedisplay()) {
                 encodeMessage(writer, messages, facesMessage, jb);
             }
         }
         jb = JSONBuilder.create();
         jb.endArray().endMap().endArray().endFunction();
         writer.write(jb + "//" + UUID.randomUUID());
         writer.endElement("script");
         writer.endElement("span");
     }
 
     private void encodeMessage(ResponseWriter writer, GrowlMessages messages, FacesMessage facesMessage, JSONBuilder jb) throws IOException {
 
         boolean showSummary = messages.isShowSummary();
         boolean showDetail = messages.isShowDetail();
         String summary = (null != (summary = facesMessage.getSummary())) ? summary : "";
         String detail = (null != (detail = facesMessage.getDetail())) ? detail : ""; // Mojarra defaults to summary. Not good.
         String text = ((showSummary ? summary : "") + " " + (showDetail ? detail : "")).trim();
         int ordinal = (ordinal = FacesMessage.VALUES.indexOf(facesMessage.getSeverity())) > -1 && ordinal < states.length ? ordinal : 0;
         Integer stickyOrdinal = (stickyOrdinal = severityMap.get(messages.getAutoHide())) == null ? 4 : stickyOrdinal;
 
         if (!text.equals("")) {
             if (!jb.toString().equals("")) {
                 writer.write(",");
                 jb = JSONBuilder.create();
             }
             jb.beginMap();
             writer.write(jb + "text:'");
             if (messages.isEscape()) {
                writer.write(DOMUtils.escapeAnsi(text));
             } else {
                 writer.write(text);
             }
             writer.write("',");
             jb = JSONBuilder.create();
             jb.entry("icon", icons[ordinal]).entry("state", states[ordinal]).entry("sticky", (ordinal > stickyOrdinal)).endMap();
             writer.write(jb.toString());
         }
         facesMessage.rendered();
     }
 
     private void writeAttributes(ResponseWriter writer, UIComponent component, String... keys) throws IOException {
         Object value;
         for (String key : keys) {
             value = component.getAttributes().get(key);
             if (value != null) {
                 writer.writeAttribute(key, value, key);
             }
         }
     }
 
     private void log(Level level, String sourceMethod, String message) {
         if (!FacesContext.getCurrentInstance().isProjectStage(ProjectStage.Development)) return;
         logger.logp(level, logger.getName(), sourceMethod, message);
     }
 
     private void logInvalid(Set<String> validSet, String name, String value, String sourceMethod) {
         if (!value.equals("") && !validSet.contains(value)) {
             log(Level.WARNING, sourceMethod, "Invalid " + name + " \"" + value + "\" reset to default. Read TLD doc.");
         }
     }
 
     private void durationEntry(JSONBuilder jb, String durationName, String duration) {
         try {
             jb.entry(durationName, Integer.parseInt(duration));
         } catch (NumberFormatException e) {
             duration = durationSet.contains(duration) ? duration : "_default";
             jb.entry(durationName, duration);
         }
     }
 }
