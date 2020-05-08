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
 
 package org.icefaces.impl.renderkit;
 
 import org.icefaces.application.ProductInfo;
 import org.icefaces.impl.context.DOMResponseWriter;
import org.icefaces.impl.event.BridgeFormsSetup;
 import org.icefaces.impl.event.BridgeSetup;
 import org.icefaces.impl.event.MainEventListener;
 import org.icefaces.impl.util.FormEndRendering;
 import org.icefaces.render.MandatoryResourceComponent;
 import org.icefaces.util.EnvUtils;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.render.RenderKit;
 import javax.faces.render.RenderKitWrapper;
 import javax.faces.render.Renderer;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class DOMRenderKit extends RenderKitWrapper {
     private static Logger log = Logger.getLogger(DOMRenderKit.class.getName());
     private MainEventListener mainEventListener = new MainEventListener();
     private RenderKit delegate;
     private boolean deltaSubmit;
     private Renderer modifiedMessageRenderer = null;
     private static final String MESSAGE = "javax.faces.Message";
     private static final String MESSAGE_CLASS =
             "org.icefaces.impl.renderkit.html_basic.MessageRenderer";
     private Renderer modifiedMessagesRenderer = null;
     private static final String MESSAGES = "javax.faces.Messages";
     private static final String MESSAGES_CLASS =
             "org.icefaces.impl.renderkit.html_basic.MessagesRenderer";
     private ArrayList<MandatoryResourceComponent> mandatoryResourceComponents = new ArrayList<MandatoryResourceComponent>();
     public static final String ACE_THEME_PARAM = "org.icefaces.ace.theme";
     public static final String ACE_HEAD_RENDERER_CLASSNAME = "org.icefaces.ace.renderkit.HeadRenderer";
 
     //Announce ICEfaces
     static {
         if (log.isLoggable(Level.INFO)) {
             log.info(new ProductInfo().toString());
         }
     }
 
     public DOMRenderKit(RenderKit delegate) {
         this.delegate = delegate;
         FacesContext facesContext = FacesContext.getCurrentInstance();
         deltaSubmit = EnvUtils.isDeltaSubmit(facesContext);
 /*
         try {
             modifiedMessageRenderer = 
                     (Renderer) Class.forName(MESSAGE_CLASS).newInstance();
         } catch (Throwable t)  {
             log.fine("No override for Message Renderer " + t.toString());
         }
         try {
             modifiedMessagesRenderer = 
                     (Renderer) Class.forName(MESSAGES_CLASS).newInstance();
         } catch (Throwable t)  {
             log.fine("No override for Messages Renderer " + t.toString());
         }
 */
     }
 
     public RenderKit getWrapped() {
         return delegate;
     }
 
     private boolean useAceHeadRenderer() {
 
         FacesContext facesContext = FacesContext.getCurrentInstance();
         String aceThemeParam = facesContext.getExternalContext().getInitParameter(ACE_THEME_PARAM);
         if (aceThemeParam != null) {
             if (aceThemeParam.trim().equalsIgnoreCase("none")) return false;
         }
         return true;
     }
 
     /**
      * Check if renderer has an annotation for adding custom scripts
      *
      * @param family
      * @param rendererType
      * @param r
      */
     public void addRenderer(String family, String rendererType, Renderer r) {
         Class clazz = r.getClass();
 
         if (ACE_HEAD_RENDERER_CLASSNAME.equals(clazz.getName())) {
             if (!useAceHeadRenderer()) return; // see if org.icefaces.ace.theme context param is set to none
         }
 
         MandatoryResourceComponent mrc = (MandatoryResourceComponent)
                 clazz.getAnnotation(MandatoryResourceComponent.class);
         if (mrc != null) {
             String compClassName = mrc.value();
             if (compClassName != null && compClassName.length() > 0) {
                 if (!mandatoryResourceComponents.contains(mrc)) {
                     mandatoryResourceComponents.add(mrc);
                 }
             }
         }
 
         Renderer renderer = "javax.faces.Form".equals(family) ? new FormBoost(r) : r;
         if ("javax.faces.Message".equals(family) && "javax.faces.Message".equals(rendererType)) {
             renderer = new MessageRenderer(r);
         } else if ("javax.faces.Messages".equals(family) && "javax.faces.Messages".equals(rendererType)) {
             renderer = new MessagesRenderer(r);
         }
         super.addRenderer(family, rendererType, renderer);
     }
 
     public Renderer getRenderer(String family, String type) {
         Renderer renderer = delegate.getRenderer(family, type);
         if (renderer == null) {
             return renderer;
         }
 /*
         String className = renderer.getClass().getName();
         if (className.equals("com.sun.faces.renderkit.html_basic.MessageRenderer"))  {
             return modifiedMessageRenderer;
         }
         if (className.equals("com.sun.faces.renderkit.html_basic.MessagesRenderer"))  {
             return modifiedMessagesRenderer;
         }
 */
         return renderer;
     }
 
     public ResponseWriter createResponseWriter(Writer writer, String contentTypeList, String encoding) {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ResponseWriter parentWriter = delegate.createResponseWriter(writer, contentTypeList, encoding);
         if (facesContext.getPartialViewContext().isPartialRequest()) {
             return parentWriter;
         }
         if (!EnvUtils.isICEfacesView(facesContext)) {
             return parentWriter;
         }
 
         return new DOMResponseWriter(parentWriter, parentWriter.getCharacterEncoding(), parentWriter.getContentType());
     }
 
     public List<MandatoryResourceComponent> getMandatoryResourceComponents() {
         return mandatoryResourceComponents;
     }
 
     private class FormBoost extends RendererWrapper {
         private FormBoost(Renderer renderer) {
             super(renderer);
         }
 
         public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
 
            if (component instanceof BridgeFormsSetup.ShortIdForm) {
                 //do not augment BridgeSetup form
                 super.encodeEnd(context, component);
                 return;
             }
 
             FormEndRendering.renderIntoForm(context, component);
 
             if (EnvUtils.isICEfacesView(context)) {
                 ResponseWriter writer = context.getResponseWriter();
 
                 if (deltaSubmit) {
                     writer.startElement("script", component);
                     writer.writeAttribute("type", "text/javascript", null);
                     writer.writeText("ice.calculateInitialParameters('", null);
                     writer.writeText(component.getClientId(context), null);
                     writer.writeText("');", null);
                     writer.endElement("script");
                 }
 
                 super.encodeEnd(context, component);
 
                 //render BridgeSetup immediately after form if missing body
                 if (!EnvUtils.hasHeadAndBodyComponents(context)) {
                     List<UIComponent> bodyResources = BridgeSetup.getBridgeSetup(context).getBodyResources(context);
                     for (UIComponent bodyResource : bodyResources) {
                         bodyResource.encodeBegin(context);
                         bodyResource.encodeEnd(context);
                     }
                 }
             } else {
                 super.encodeEnd(context, component);
             }
 
         }
     }
 
     private class MessageRenderer extends RendererWrapper {
         private MessageRenderer(Renderer renderer) {
             super(renderer);
         }
 
         public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
             ResponseWriter writer = context.getResponseWriter();
             writer.startElement("span", component);
             writer.writeAttribute("id", component.getClientId(context), "id");
             super.encodeEnd(context, component);
             writer.endElement("span");
         }
     }
 
     private class MessagesRenderer extends RendererWrapper {
         private MessagesRenderer(Renderer renderer) {
             super(renderer);
         }
 
         public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
             ResponseWriter writer = context.getResponseWriter();
             writer.startElement("div", component);
             writer.writeAttribute("id", component.getClientId(context), "id");
             super.encodeEnd(context, component);
             writer.endElement("div");
         }
     }
 }
