 /*
  * Copyright 2004-2011 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.render.html;
 
 import java.io.IOException;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.internal.IgnoreAttribute;
 
 import org.seasar.framework.util.StringUtil;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.render.AbstractRenderer;
 import org.seasar.teeda.core.util.RendererUtil;
 import org.seasar.teeda.extension.component.html.THtmlScript;
 import org.seasar.teeda.extension.util.PathUtil;
 import org.seasar.teeda.extension.util.VirtualResource;
 
 /**
  * @author shot
  */
 public class THtmlScriptRenderer extends AbstractRenderer {
 
     public static final String COMPONENT_FAMILY = THtmlScript.COMPONENT_FAMILY;
 
     public static final String RENDERER_TYPE = THtmlScript.DEFAULT_RENDERER_TYPE;
 
     private static final IgnoreAttribute IGNORE_COMPONENT;
 
     static {
         IGNORE_COMPONENT = buildIgnoreComponent();
     }
 
     public void encodeBegin(FacesContext context, UIComponent component)
             throws IOException {
         assertNotNull(context, component);
         if (!component.isRendered()) {
             return;
         }
         encodeTHtmlScriptBegin(context, (THtmlScript) component);
     }
 
     protected void encodeTHtmlScriptBegin(FacesContext context,
             THtmlScript script) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         writer.startElement(JsfConstants.SCRIPT_ELEM, script);
         RendererUtil.renderIdAttributeIfNecessary(writer, script,
                 getIdForRender(context, script));
         String type = script.getType();
         if (!StringUtil.isEmpty(type)) {
             RendererUtil.renderAttribute(writer, JsfConstants.TYPE_ATTR, type,
                     null);
         }
         String lang = script.getLanguage();
         if (!StringUtil.isEmpty(lang)) {
             RendererUtil.renderAttribute(writer, JsfConstants.LANGUAGE_ATTR,
                     lang, null);
         }
         String src = script.getSrc();
         if (!StringUtil.isEmpty(src)) {
             if (VirtualResource.startsWithVirtualPath(src)) {
                 src = context.getExternalContext().getRequestContextPath() +
                         src;
             } else {
                 final String baseViewId = script.getBaseViewId();
                 src = PathUtil.toAbsolutePath(context, src, baseViewId);
             }
             RendererUtil.renderAttribute(writer, JsfConstants.SRC_ATTR, src,
                     null);
         }
         renderRemainAttributes(script, writer, IGNORE_COMPONENT);
     }
 
     public void encodeEnd(FacesContext context, UIComponent component)
             throws IOException {
         assertNotNull(context, component);
         if (!component.isRendered()) {
             return;
         }
         encodeTHtmlScriptEnd(context, (THtmlScript) component);
     }
 
     protected void encodeTHtmlScriptEnd(FacesContext context, THtmlScript script)
             throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         writer.endElement(JsfConstants.SCRIPT_ELEM);
     }
 
     protected static IgnoreAttribute buildIgnoreComponent() {
         IgnoreAttribute ignore = new IgnoreAttribute();
         ignore.addAttributeName(JsfConstants.TYPE_ATTR);
         ignore.addAttributeName(JsfConstants.LANGUAGE_ATTR);
         ignore.addAttributeName(JsfConstants.SRC_ATTR);
         return ignore;
     }
 }
