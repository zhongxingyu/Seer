 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
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
  */
 package com.celements.calendar.api;
 
 import java.util.Date;
 import java.util.List;
 
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.calendar.IEvent;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Api;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.api.Element;
 import com.xpn.xwiki.objects.BaseObject;
 
 public class EventApi extends Api {
   private IEvent event;
   
   public EventApi(IEvent event, XWikiContext context) {
     this(event, context.getLanguage(), context);
   }
 
   public EventApi(IEvent event, String language, XWikiContext context) {
     super(context);
     this.event = event;
     this.event.setLanguage(language);
   }
   
   public CalendarApi getCalendar() {
    return new CalendarApi(event.getCalendar(context), context);
   }
 
   public String getTitle() {
     return event.getTitle();
   }
   
   public String getDescription() {
     return event.getDescription();
   }
   
   public String getLocation() {
     return event.getLocation();
   }
   
   public String getDateString(String dateField, String format) {
     return event.getDateString(dateField, format);
   }
   
   public Date getEventDate() {
     return event.getEventDate();
   }
   
   public Boolean isSubscribable() {
     return event.isSubscribable();
   }
   
   public Document getEventDocument() {
     return event.getEventDocument().newDocument(context);
   }
 
   /**
    * 
    * @return
    * 
    * @deprecated use getDocumentReference instead
    */
   @Deprecated
   public String getDocName() {
     return event.getDocName();
   }
 
   public DocumentReference getDocumentReference() {
     return event.getDocumentReference();
   }
   
   public boolean isFromSubscribableCalendar(String calendarSpace) {
     return event.isFromSubscribableCalendar(calendarSpace);
   }
   
   /**
    * Get the value of a String property (String or TextArea). If the value in
    * the desired language is empty, the value of the default language will be
    * returned.
    * 
    * @param name Name of the property
    * @param lang Desired language (if not available default language will be taken).
    * @return 
    */
   public String getStringPropertyDefaultIfEmpty(String name){
     return event.getStringPropertyDefaultIfEmpty(name, context.getLanguage());
   }
 
   public String getStringProperty(String name) {
     return event.getStringProperty(name, context.getLanguage());
   }
 
   public Date getDateProperty(String name) {
     return event.getDateProperty(name, context.getLanguage());
   }
 
   public Boolean getBooleanProperty(String name, String lang) {
     return event.getBooleanProperty(name, context.getLanguage());
   }
 
   public com.xpn.xwiki.api.Object getObj(String lang) {
     BaseObject obj = event.getObj(lang);
     if (obj != null) {
       return obj.newObjectApi(obj, context);
     }
     return null;
   }
   
   public Element[] getProperties() {
     return event.getProperties(context.getLanguage(), context);
   }
   
   public String displayOverviewField(String name, String link) {
     return event.displayOverviewField(name, link, context);
   }
   
   public String displayField(String name) {
     return event.displayField(name, context);
   }
   
   public List<List<String>> getEditableProperties() throws XWikiException{
     return event.getEditableProperties(context.getLanguage(), context);
   }
   
   public List<String> getNonEmptyFields(List<String> fieldList) {
     return event.getNonEmptyFields(fieldList, context);
   }
   
   public boolean needsMoreLink(){
     return event.needsMoreLink(context);
   }
 
   public String getLanguage() {
     return event.getLanguage();
   }
 
   public void setLanguage(String language) {
     event.setLanguage(language);
   }
 
 }
