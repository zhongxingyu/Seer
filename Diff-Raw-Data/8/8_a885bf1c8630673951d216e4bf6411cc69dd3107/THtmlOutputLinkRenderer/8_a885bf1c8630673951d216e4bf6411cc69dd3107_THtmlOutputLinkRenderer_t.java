 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
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
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIParameter;
 import javax.faces.component.html.HtmlOutputLink;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.internal.WindowIdUtil;
 import javax.servlet.ServletContext;
 
 import org.seasar.framework.log.Logger;
 import org.seasar.framework.util.DateConversionUtil;
 import org.seasar.framework.util.InputStreamUtil;
 import org.seasar.framework.util.StringUtil;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.render.html.HtmlOutputLinkRenderer;
 import org.seasar.teeda.core.render.html.support.PortletUrlBuilder;
 import org.seasar.teeda.core.render.html.support.UrlBuilder;
 import org.seasar.teeda.core.util.PortletUtil;
 import org.seasar.teeda.core.util.ServletContextUtil;
 import org.seasar.teeda.core.util.ValueHolderUtil;
 import org.seasar.teeda.extension.ExtensionConstants;
 import org.seasar.teeda.extension.html.HtmlSuffix;
 
 /**
  * @author shot
  * @author higa
  */
 public class THtmlOutputLinkRenderer extends HtmlOutputLinkRenderer {
 
     public static final String COMPONENT_FAMILY = "javax.faces.Output";
 
     public static final String RENDERER_TYPE = "org.seasar.teeda.extension.Link";
 
     private HtmlSuffix htmlSuffix;
 
     private static final Logger logger = Logger
             .getLogger(THtmlOutputLinkRenderer.class);
 
     protected String buildHref(FacesContext context,
             HtmlOutputLink htmlOutputLink, String encoding) throws IOException {
         final String base = ValueHolderUtil.getValueForRender(context,
                 htmlOutputLink);
         // PortletSupport
         if (PortletUtil.isPortlet(context)) {
             if (htmlOutputLink.getId().startsWith(ExtensionConstants.GO_PREFIX)
                     || htmlOutputLink.getId().startsWith(
                             ExtensionConstants.JUMP_PREFIX)) {
                 PortletUrlBuilder urlBuilder = new PortletUrlBuilder();
                 urlBuilder.setBase(base);
                 for (Iterator it = htmlOutputLink.getChildren().iterator(); it
                         .hasNext();) {
                     UIComponent child = (UIComponent) it.next();
                     if (child instanceof UIParameter) {
                         UIParameter parameter = (UIParameter) child;
                        String name = parameter.getName();
                        String value = toBlankIfNull(parameter.getValue());
                        urlBuilder.add(name, value);
                     }
                 }
                 return context.getExternalContext().encodeResourceURL(
                         urlBuilder.build());
             } else {
                 return super.buildHref(context, htmlOutputLink, encoding);
             }
         } else {
             UrlBuilder urlBuilder = new UrlBuilder();
             final String sufiixedBase = getSuffixedBase(context, base);
             urlBuilder.setBase(sufiixedBase);
             for (Iterator it = htmlOutputLink.getChildren().iterator(); it
                     .hasNext();) {
                 UIComponent child = (UIComponent) it.next();
                 if (child instanceof UIParameter) {
                     UIParameter parameter = (UIParameter) child;
                     String convertedValue = convertDateIfNeed(parameter
                             .getValue(), context, encoding);
                     urlBuilder.add(parameter.getName(), convertedValue);
                 }
             }
             if (WindowIdUtil.isNewWindowTarget(htmlOutputLink.getTarget())) {
                 urlBuilder.add(WindowIdUtil.NEWWINDOW, JsfConstants.TRUE);
             }
             return context.getExternalContext().encodeResourceURL(
                     urlBuilder.build());
 
         }
     }
 
     protected void renderHref(final FacesContext context,
             final ResponseWriter writer, final String href) throws IOException {
         writer.writeAttribute(JsfConstants.HREF_ATTR, href, null);
     }
 
     protected String convertDateIfNeed(final Object value,
             final FacesContext context, final String encoding) {
         if (value == null) {
             return "";
         }
         String ret = null;
         try {
             if (value instanceof Date) {
                 Locale locale = context.getViewRoot().getLocale();
                 SimpleDateFormat dateFormat = DateConversionUtil
                         .getY4DateFormat(locale);
                 ret = URLEncoder.encode(dateFormat.format((Date) value),
                         encoding);
             } else if (value instanceof String) {
                 ret = URLEncoder.encode(value.toString(), encoding);
             } else {
                 ret = value.toString();
             }
         } catch (UnsupportedEncodingException e) {
             logger.info(e);
             ret = value.toString();
         }
         return ret;
     }
 
     public void setHtmlSuffix(HtmlSuffix htmlSuffix) {
         this.htmlSuffix = htmlSuffix;
     }
 
     protected String getSuffixedBase(FacesContext context, String base) {
         if (htmlSuffix == null) {
             return base;
         }
         String value = base;
         final String suffix = htmlSuffix.getSuffix(context);
         final int lastIndexOf = base.lastIndexOf(".");
         if (lastIndexOf >= 0) {
             final String postfix = base.substring(lastIndexOf);
             final String viewId = context.getViewRoot().getViewId();
             final String viewIdPrefix = viewId.substring(0, viewId
                     .lastIndexOf("/") + 1);
             if (base.startsWith("../")) {
                 final String[] split = StringUtil.split(viewIdPrefix, "/");
                 String s = base;
                 for (int i = 0; i < split.length; i++) {
                     if (s.indexOf("../") >= 0) {
                         s = s.replaceFirst("\\.\\./", split[i] + "/");
                     }
                 }
                 final String prefix = "/" + s.substring(0, s.lastIndexOf("."));
                 value = prefix + suffix + postfix;
             } else if (base.startsWith("./")) {
                 String s = StringUtil.replace(base, "./", viewIdPrefix);
                 value = s.substring(0, s.lastIndexOf(".")) + suffix + postfix;
             } else {
                 String prefix = base.substring(0, lastIndexOf);
                 if (!prefix.startsWith("/")) {
                     prefix = "/" + prefix;
                 }
                 value = prefix + suffix + postfix;
             }
             if (hasActualResource(context, value)) {
                 value = getResourceURL(context, value);
             } else {
                 value = base;
             }
         }
         return value;
     }
 
     protected boolean hasActualResource(final FacesContext context, String value) {
         final ExternalContext externalContext = context.getExternalContext();
         final ServletContext servletContext = (ServletContext) externalContext
                 .getContext();
         InputStream is = ServletContextUtil.getResourceAsStream(servletContext,
                 value);
         try {
             return (is != null);
         } finally {
             InputStreamUtil.close(is);
         }
     }
 
     protected String getResourceURL(final FacesContext context,
             final String path) {
         return context.getApplication().getViewHandler().getResourceURL(
                 context, path);
     }
 }
