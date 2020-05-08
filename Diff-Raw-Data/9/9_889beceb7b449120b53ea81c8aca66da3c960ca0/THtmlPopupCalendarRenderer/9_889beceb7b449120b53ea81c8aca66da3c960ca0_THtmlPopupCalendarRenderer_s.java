 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
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
 import java.io.StringWriter;
 import java.text.DateFormatSymbols;
 import java.util.Calendar;
import java.util.Date;
 import java.util.Locale;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.DateTimeConverter;
 import javax.faces.internal.FacesMessageUtil;
 
 import org.seasar.teeda.core.JsfConstants;
 import org.seasar.teeda.core.context.html.HtmlResponseWriter;
 import org.seasar.teeda.core.render.RenderPreparableRenderer;
 import org.seasar.teeda.extension.component.html.THtmlPopupCalendar;
 import org.seasar.teeda.extension.util.DateFormatSymbolsUtil;
 import org.seasar.teeda.extension.util.VirtualResource;
 
 /**
  * @author higa
  */
 public class THtmlPopupCalendarRenderer extends THtmlInputTextRenderer
         implements RenderPreparableRenderer {
 
     public static final String COMPONENT_FAMILY = "org.seasar.teeda.extension.HtmlPopupCalendar";
 
     public static final String RENDERER_TYPE = "org.seasar.teeda.extension.HtmlPopupCalendar";
 
     private static final String JAVASCRIPT_ENCODED = "org.seasar.teeda.extension.popupcalendar.JAVASCRIPT_ENCODED";
 
     private static final String RESOURCE_ROOT = "org/seasar/teeda/extension/render/html/popupcalendar/";
 
     private static final String TODAY = "org.seasar.teeda.extension.component.TPopupCalendar.TODAY";
 
     private static final String GOTO = "org.seasar.teeda.extension.component.TPopupCalendar.GOTO";
 
     public void encodeBefore(FacesContext context, UIComponent component)
             throws IOException {
         assertNotNull(context, component);
         if (!component.isRendered()) {
             return;
         }
         encodeHtmlPopupCalendarPrepare(context, (THtmlPopupCalendar) component);
     }
 
     protected void encodeHtmlPopupCalendarPrepare(final FacesContext context,
             final THtmlPopupCalendar htmlCalendar) throws IOException {
         if (context.getExternalContext().getRequestMap().containsKey(
                 JAVASCRIPT_ENCODED)) {
             return;
         }
         Locale currentLocale = context.getViewRoot().getLocale();
         Calendar timeKeeper = Calendar.getInstance(currentLocale);
         DateFormatSymbols symbols = new DateFormatSymbols(currentLocale);
         String[] months = DateFormatSymbolsUtil.getMonths(symbols);
         int firstDayOfWeek = timeKeeper.getFirstDayOfWeek();
         VirtualResource
                 .addCssResource(context, RESOURCE_ROOT + "css/theme.css");
         VirtualResource.addJsResource(context, RESOURCE_ROOT +
                 "js/popcalendar_init.js");
         VirtualResource.addJsResource(context, RESOURCE_ROOT +
                 "js/popcalendar.js");
 
         StringBuffer script = new StringBuffer();
         appendImageDirectory(context, script);
         script.append(JsfConstants.LINE_SP);
         script.append(getLocalizedLanguageScript(symbols, months,
                 firstDayOfWeek, htmlCalendar));
         script.append(JsfConstants.LINE_SP);
         script.append("loadPopupScript();");
         VirtualResource.addInlineJsResource(context, JAVASCRIPT_ENCODED, script
                 .toString());
 
         context.getExternalContext().getRequestMap().put(JAVASCRIPT_ENCODED,
                 Boolean.TRUE);
     }
 
     public void encodeEnd(FacesContext context, UIComponent component)
             throws IOException {
         super.encodeEnd(context, component);
         THtmlPopupCalendar htmlCalendar = (THtmlPopupCalendar) component;
 //        Locale currentLocale = context.getViewRoot().getLocale();
 //        Date value = (Date) htmlCalendar.getValue();
 
 //        Calendar timeKeeper = Calendar.getInstance(currentLocale);
 //        timeKeeper.setTime(value != null ? value : new Date());
 
         DateTimeConverter converter = THtmlCalendarRendererUtil.getConverter(
                 context, (UIInput) component);
         if (!htmlCalendar.isDisabled()) {
             ResponseWriter writer = context.getResponseWriter();
             String datePattern = converter.getPattern();
             writer.startElement(JsfConstants.SCRIPT_ELEM, component);
             writer.writeAttribute(JsfConstants.TYPE_ATTR,
                     JsfConstants.TEXT_JAVASCRIPT_VALUE, null);
             writer.writeText(getScriptBtn(context, htmlCalendar, datePattern),
                     null);
             writer.endElement(JsfConstants.SCRIPT_ELEM);
         }
     }
 
     private static void appendImageDirectory(FacesContext context,
             StringBuffer script) {
         script.append("jscalendarSetImageDirectory('");
         script.append(VirtualResource.convertVirtualPath(context,
                 RESOURCE_ROOT + "images/"));
         script.append("');");
     }
 
     private static String getLocalizedLanguageScript(DateFormatSymbols symbols,
             String[] months, int firstDayOfWeek, THtmlPopupCalendar htmlCalendar) {
         int realFirstDayOfWeek = firstDayOfWeek - 1;
         String[] weekDays;
         if (realFirstDayOfWeek == 0) {
             weekDays = DateFormatSymbolsUtil
                     .getWeekdaysStartingWithSunday(symbols);
         } else if (realFirstDayOfWeek == 1) {
             weekDays = DateFormatSymbolsUtil.getWeekdays(symbols);
         } else {
             throw new IllegalStateException(
                     "Week may only start with sunday or monday.");
         }
         StringBuffer script = new StringBuffer();
         defineStringArray(script, "jscalendarMonthName", months);
         defineStringArray(script, "jscalendarMonthName2", months);
         defineStringArray(script, "jscalendarDayName", weekDays);
         setIntegerVariable(script, "jscalendarStartAt", realFirstDayOfWeek);
         setStringVariable(script, "jscalendarGotoString", FacesMessageUtil
                 .getDetail(GOTO, null));
         setStringVariable(script, "jscalendarTodayString", FacesMessageUtil
                 .getDetail(TODAY, null));
         setStringVariable(script, "jscalendarDateFormat", htmlCalendar
                 .getDatePattern());
         return script.toString();
     }
 
     private static void setIntegerVariable(StringBuffer script, String name,
             int value) {
         script.append(name);
         script.append(" = ");
         script.append(value);
         script.append(";\n");
     }
 
     private static void setStringVariable(StringBuffer script, String name,
             String value) {
         script.append(name);
         script.append(" = \"");
         script.append(value);
         script.append("\";\n");
     }
 
     private static void defineStringArray(StringBuffer script,
             String arrayName, String[] array) {
         script.append(arrayName);
         script.append(" = new Array(");
         for (int i = 0; i < array.length; i++) {
             if (i != 0) {
                 script.append(",");
             }
             script.append("\"");
             script.append(array[i]);
             script.append("\"");
         }
         script.append(");");
     }
 
     private String getScriptBtn(FacesContext context, UIComponent component,
             String datePattern) throws IOException {
         HtmlResponseWriter writer = new HtmlResponseWriter();
         writer.setWriter(new StringWriter());
         writer.write("document.write('");
         writer.startElement(JsfConstants.IMG_ELEM, component);
         writer.writeAttribute(JsfConstants.SRC_ATTR, VirtualResource
                 .convertVirtualPath(context, RESOURCE_ROOT +
                         "images/calendar.gif"), null);
         writer.writeAttribute(JsfConstants.STYLE_ATTR,
                 "vertical-align:bottom;", null);
         writeOnclickJsCalendarFunctionCall(writer, context, component,
                 datePattern);
         writer.endElement(JsfConstants.IMG_ELEM);
         writer.write("');\n");
         return writer.toString();
     }
 
     private void writeOnclickJsCalendarFunctionCall(ResponseWriter writer,
             FacesContext facesContext, UIComponent uiComponent,
             String datePattern) throws IOException {
        String id = uiComponent.getId();
 
        String jsCalendarFunctionCall = "jscalendarPopUpCalendar(this,document.getElementById(\\'" +
                id + "\\'),\\'" + datePattern + "\\')";
         writer.writeAttribute(JsfConstants.ONCLICK_ATTR,
                 jsCalendarFunctionCall, null);
     }
 }
