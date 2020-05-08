 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.tests.metamer.bean;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.event.ValueChangeEvent;
 import org.richfaces.component.UICalendar;
 
 import org.richfaces.tests.metamer.Attributes;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Managed bean for rich:calendar.
  *
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 @ManagedBean(name = "richCalendarBean")
 @ViewScoped
 public class RichCalendarBean implements Serializable {
 
     private static final long serialVersionUID = -1L;
     private static Logger logger;
     private Attributes attributes;
     private Date date = new Date();
     private TimeZone timeZone = TimeZone.getTimeZone("UTC");
 
     /**
      * Initializes the managed bean.
      */
     @PostConstruct
     public void init() {
         logger = LoggerFactory.getLogger(getClass());
         logger.debug("initializing bean " + getClass().getName());
 
         attributes = Attributes.getComponentAttributesFromFacesConfig(UICalendar.class, getClass());
 
         attributes.setAttribute("datePattern", "MMM d, yyyy HH:mm");
        attributes.setAttribute("direction", "bottomRight");
        attributes.setAttribute("jointPoint", "bottomLeft");
         attributes.setAttribute("popup", true);
         attributes.setAttribute("rendered", true);
         attributes.setAttribute("showApplyButton", true);
         attributes.setAttribute("showHeader", true);
         attributes.setAttribute("showFooter", true);
         attributes.setAttribute("showInput", true);
         attributes.setAttribute("showWeeksBar", true);
         attributes.setAttribute("showWeekDaysBar", true);
 
         // TODO has to be tested in another way
         attributes.remove("converter");
         attributes.remove("dataModel");
         attributes.remove("validator");
         attributes.remove("valueChangeListener");
         attributes.remove("timeZone");
     }
 
     public Attributes getAttributes() {
         return attributes;
     }
 
     public void setAttributes(Attributes attributes) {
         this.attributes = attributes;
     }
 
     public Date getDate() {
         return date;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public TimeZone getTimeZone() {
         return timeZone;
     }
 
     public void setTimeZone(TimeZone timeZone) {
         this.timeZone = timeZone;
     }
 
     /**
      * A value change listener that logs to the page old and new value.
      *
      * @param event
      *            an event representing the activation of a user interface component
      */
     public void valueChangeListener(ValueChangeEvent event) {
         SimpleDateFormat sdf = new SimpleDateFormat(attributes.get("datePattern").getValue().toString());
         sdf.setTimeZone(timeZone);
 
         String oldDate = "null";
         String newDate = "null";
 
         if (event.getOldValue() != null) {
             oldDate = sdf.format((Date) event.getOldValue());
         }
         if (event.getNewValue() != null) {
             newDate = sdf.format((Date) event.getNewValue());
         }
 
         RichBean.logToPage("* value changed: " + oldDate + " -> " + newDate);
     }
 }
