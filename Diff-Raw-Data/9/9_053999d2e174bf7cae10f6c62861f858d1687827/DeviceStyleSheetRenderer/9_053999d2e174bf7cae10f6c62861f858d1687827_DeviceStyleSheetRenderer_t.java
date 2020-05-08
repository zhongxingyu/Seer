 /*
  * Copyright 2004-2011 ICEsoft Technologies Canada Corp. (c)
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
  * See the License for the specific language governing permissions an
  * limitations under the License.
  */
 
 package org.icefaces.component.stylesheet;
 
 import org.icefaces.component.utils.HTML;
 import org.icefaces.component.utils.PassThruAttributeWriter;
 import org.icefaces.component.utils.Utils;
 
 import javax.faces.application.Resource;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ComponentSystemEvent;
 import javax.faces.event.ListenerFor;
 import javax.faces.render.Renderer;
 import java.io.IOException;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * Device style sheet render is mobile specific and by default tries to load
  * a css theme for the device making the initial request.  Devices can be either
  * Android, Blackberry or iPhone as that is the initial component theme offering.
  * <p/>
  * If the developer does not wish to have the component auto detect the calling
  * device the library and name attribute can be set to either specify a specific
  * theme by name or a new theme by library and anme.
  *
  * @since 0.0.1 alpha.
  */
 @ListenerFor(systemEventClass = javax.faces.event.PostAddToViewEvent.class)
 public class DeviceStyleSheetRenderer extends Renderer implements javax.faces.event.ComponentSystemEventListener {
 
     private static Logger log = Logger.getLogger(DeviceStyleSheetRenderer.class.getName());
 
     // empty string
     public static final String EMPTY_STRING = "";
 
     // CSS style extension circa 2011
     private static final String CSS_EXT = ".css";
 
     // iPhone style sheet name found in jar.
     public static final String IPHONE_CSS = Utils.DeviceType.iphone.name() + CSS_EXT;
     // Android style sheet name found in jar.
     public static final String ANDROID_CSS = Utils.DeviceType.android.name() + CSS_EXT;
     // Blackberry style sheet name found in jar.
     public static final String BBERRY_CSS = Utils.DeviceType.bberry.name() + CSS_EXT;
 
     // default resource library for a default themes,  if not specified in
     // component definition this library will be loaded.
     private static final String DEFAULT_LIBRARY = "org.icefaces.component.skins";
     // url of a resource that could not be resolved, danger Will Robertson.
     public static final String RESOURCE_URL_ERROR = "RES_NOT_FOUND";
     // key to store if algorithm to detect device type has run. If a device
     // was detected this key used to store the device name in session scope
     // for later retrial on subsequent component renders.
     public static final String MOBILE_DEVICE_TYPE_KEY = "mobile_device_type";
 
     protected void startElement(ResponseWriter writer, UIComponent component) throws IOException {
         writer.startElement(HTML.STYLE_ELEM, component);
         writer.writeAttribute(HTML.TYPE_ATTR, HTML.STYLE_TYPE_TEXT_CSS, HTML.TYPE_ATTR);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void encodeEnd(FacesContext context, UIComponent uiComponent) throws IOException {
 
         ResponseWriter writer = context.getResponseWriter();
         Map contextMap = context.getAttributes();
         Map attributes = uiComponent.getAttributes();
         DeviceStyleSheet stylesheet = (DeviceStyleSheet) uiComponent;
 
         /**
          * The component has three modes in which it executes.
          * 1.) no attributes - then component tries to detect a mobile device
          *     in from the user-agent.  If a mobile device is discovered, then
          *     it will fall into three possible matches, iphone, android and
          *     blackberry.  If the mobile device is not not know then iphone
          *     is loaded. Library is always assumed to be DEFAULT_LIBRARY.
          *
          * 2.) name attribute - component will default to using a library name
          *     of DEFAULT_LIBRARY.  The name attribute specifies one of the
          *     possible device themes; iphone.css, android.css or bberry.css.
          *     Error will result if named resource could not be resolved.
          *
          * 3.) name and libraries attributes. - component will use the library
          *     and name specified by the user.  Component is fully manual in this
          *     mode. Error will result if name and library can not generate a
          *     value resource.
          */
 
         // check for the existence of the name and library attributes.
         String name = (String) attributes.get(HTML.NAME_ATTR);
         String library = (String) attributes.get(HTML.LIBRARY_ATTR);
 
         // check for empty string on name attribute used for auto mode where
         // name value binding is used.
         name = name != null && name.equals(EMPTY_STRING) ? null : name;
 
         // 1.) full automatic device detection.
         if (name == null && library == null) {
             // check the session context map for the MOBILE_DEVICE_TYPE_KEY, if found
             // there is now point rechecking for for the device type.
             if (contextMap.containsKey(MOBILE_DEVICE_TYPE_KEY)) {
                 name = (String) contextMap.get(MOBILE_DEVICE_TYPE_KEY);
             } else {
                 name = Utils.getDeviceType(context).name();
             }
             name = name.concat(CSS_EXT);
             library = DEFAULT_LIBRARY;
             // store in session map for use later.
             contextMap.put(MOBILE_DEVICE_TYPE_KEY, name);
         }
         // 2.) User has specified a named theme they want to load, no auto detect
         else if (name != null && library == null) {
            // keep the name but apply default library.
             library = DEFAULT_LIBRARY;
         }
         // 3.) User has specified a name and theme of there own, anything goes.
         else {
             // nothing to do, any error will be displayed back to user at runtime.
         }
 
         // create URL that the resource can be loaded from.
         Resource resource = context.getApplication().getResourceHandler()
                 .createResource(name, library);
         String resourceUrl = RESOURCE_URL_ERROR;
         if (resource != null) {
             resourceUrl = context.getExternalContext().encodeResourceURL(resource.getRequestPath());
         } else if (log.isLoggable(Level.WARNING)) {
             log.warning("Warning could not load resource " + library + "/" + name);
         }
 
         // final write out the style element to the component tree.
         writer.startElement(HTML.LINK_ELEM, uiComponent);
         writer.writeAttribute(HTML.TYPE_ATTR, HTML.LINK_TYPE_TEXT_CSS, HTML.TYPE_ATTR);
         writer.writeAttribute(HTML.REL_ATTR, HTML.STYLE_REL_STYLESHEET, HTML.REL_ATTR);
         // pass though
         PassThruAttributeWriter.renderNonBooleanAttributes(
                 writer, uiComponent, stylesheet.getPASS_THOUGH_ATTRIBUTES());
         writer.writeURIAttribute(HTML.HREF_ATTR, resourceUrl, HTML.HREF_ATTR);
         writer.endElement(HTML.LINK_ELEM);
     }
 
     public void processEvent(ComponentSystemEvent event)
             throws AbortProcessingException {
         // http://javaserverfaces.java.net/nonav/docs/2.0/pdldocs/facelets/index.html
         // Finally make sure the component is only rendered in the header of the
         // HTML document.
         UIComponent component = event.getComponent();
         FacesContext context = FacesContext.getCurrentInstance();
         if (log.isLoggable(Level.FINER)) {
             log.finer("processEvent for component = " + component.getClass().getName());
         }
         context.getViewRoot().addComponentResource(context, component, HTML.HEAD_ELEM);
     }
 
 }
