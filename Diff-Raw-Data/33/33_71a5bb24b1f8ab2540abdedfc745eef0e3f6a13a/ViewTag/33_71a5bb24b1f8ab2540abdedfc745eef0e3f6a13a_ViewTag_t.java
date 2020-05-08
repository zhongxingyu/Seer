 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
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
 package org.seasar.teeda.core.taglib.core;
 
 import java.io.IOException;
 import java.util.Locale;
 
 import javax.faces.application.StateManager;
 import javax.faces.application.ViewHandler;
 import javax.faces.application.StateManager.SerializedView;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.internal.PageContextUtil;
 import javax.faces.internal.ValueBindingUtil;
 import javax.faces.internal.WebAppUtil;
 import javax.faces.webapp.UIComponentBodyTag;
 import javax.servlet.http.HttpSession;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.jstl.core.Config;
 import javax.servlet.jsp.tagext.BodyContent;
 
 import org.seasar.framework.util.AssertionUtil;
 import org.seasar.framework.util.LocaleUtil;
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.util.ContentTypeUtil;
 
 /**
  * @author yone
  * @author shot
  */
 public class ViewTag extends UIComponentBodyTag {
 
     private static final String COMPONENT_TYPE = UIViewRoot.COMPONENT_TYPE;
 
     protected String locale = null;
 
     public ViewTag() {
         super();
     }
 
     public void setLocale(final String locale) {
         this.locale = locale;
     }
 
     public String getLocale() {
         return locale;
     }
 
     public String getComponentType() {
         return COMPONENT_TYPE;
     }
 
     public String getRendererType() {
         return null;
     }
 
     public int doStartTag() throws JspException {
         final int rc = super.doStartTag();
         final FacesContext context = FacesContext.getCurrentInstance();
         AssertionUtil.assertNotNull("FacesContext", context);
         final String encoding = PageContextUtil
                 .getCharacterEncoding(pageContext);
         pageContext.getResponse().setLocale(context.getViewRoot().getLocale());
         final String acceptContentTypes = WebAppUtil.getAcceptHeader(context);
         final String contentType = ContentTypeUtil
                 .getContentType(acceptContentTypes);
         pageContext.getResponse().setContentType(
                 contentType + "; charset=" + encoding);
         final ResponseWriter writer = context.getResponseWriter();
         AssertionUtil.assertNotNull("ResponseWriter", writer);
         try {
             writer.startDocument();
         } catch (final IOException e) {
             throw new JspException(e.getMessage());
         }
         return rc;
     }
 
     public int doAfterBody() throws JspException {
         final FacesContext context = FacesContext.getCurrentInstance();
         AssertionUtil.assertNotNull("FacesContext", context);
         ResponseWriter responseWriter = context.getResponseWriter();
         final StateManager stateManager = context.getApplication()
                 .getStateManager();
 
         responseWriter = responseWriter.cloneWithWriter(getPreviousOut());
 
         context.setResponseWriter(responseWriter);
         final BodyContent bodyContent = getBodyContent();
         if (bodyContent == null) {
             throw new JspException(
                     "BodyContent is null for tag with handler class:"
                             + getClass().getName());
         }
         final String content = bodyContent.getString();
 
         final SerializedView view;
         try {
             view = stateManager.saveSerializedView(context);
         } catch (final IllegalStateException ise) {
             throw new JspException(ise);
         } catch (final Exception e) {
             throw new JspException("Error while saving state in session:"
                     + e.getMessage(), e);
         }
         try {
             if (view == null) {
                 getPreviousOut().write(content);
             } else {
                 int beginIndex = 0;
                 int markerIndex = 0;
                 final int markerLen = JsfConstants.STATE_MARKER.length();
                 final int contentLen = content.length();
                 // see ViewHandlerImpl#writeState
                 do {
                     markerIndex = content.indexOf(JsfConstants.STATE_MARKER,
                             beginIndex);
                     if (markerIndex == -1) {
                         responseWriter.write(content.substring(beginIndex));
                     } else {
                         responseWriter.write(content.substring(beginIndex,
                                 markerIndex));
                         stateManager.writeState(context, view);
                         beginIndex = markerIndex + markerLen;
                     }
                 } while ((-1 != markerIndex) && (beginIndex < contentLen));
             }
         } catch (final IOException ioe) {
             throw new JspException("Error while saving state in client:"
                     + ioe.getMessage(), ioe);
         }
         return EVAL_PAGE;
     }
 
     public int doEndTag() throws JspException {
         final int rc = super.doEndTag();
         final FacesContext context = FacesContext.getCurrentInstance();
         AssertionUtil.assertNotNull("FacesContext", context);
         final ResponseWriter writer = context.getResponseWriter();
         AssertionUtil.assertNotNull("ResponseWriter", writer);
         try {
             writer.endDocument();
         } catch (final IOException e) {
             throw new JspException(e.getMessage(), e);
         }
 
         HttpSession session = null;
         if ((session = pageContext.getSession()) != null) {
             session.setAttribute(ViewHandler.CHARACTER_ENCODING_KEY,
                     pageContext.getResponse().getCharacterEncoding());
         }
         return rc;
     }
 
     protected void setProperties(final UIComponent component) {
         super.setProperties(component);
         final String localeStr = getLocale();
         final FacesContext context = FacesContext.getCurrentInstance();
         Locale locale = (Locale) ValueBindingUtil.getValue(context, localeStr);
        if (locale == null && localeStr != null) {
             locale = LocaleUtil.getLocale(localeStr);
         }
        if (locale != null) {
            ((UIViewRoot) component).setLocale(locale);
        }
         Config.set(pageContext.getRequest(), Config.FMT_LOCALE, locale);
     }
 
 }
