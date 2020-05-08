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
 package com.celements.course.classcollections;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.common.classes.CelementsClassCollection;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.classes.BaseClass;
 
 @Component("CelCourseClasses")
 public class CourseClasses extends CelementsClassCollection {
   private static Log mLogger = LogFactory.getFactory().getInstance(CourseClasses.class);
 
   @Override
   protected Log getLogger() {
     return mLogger;
   }
 
   @Override
   protected void initClasses(XWikiContext context) throws XWikiException {
     getCourseTypeClass(context);
     getCourseClass(context);
     getCourseParticipantClass(context);
   }
 
   public String getConfigName() {
     return "course";
   }
   
   protected BaseClass getCourseTypeClass(XWikiContext context) throws XWikiException {
     DocumentReference docRef = new DocumentReference(context.getDatabase(), 
         "Classes", "CourseTypeClass");
     XWikiDocument doc;
     XWiki xwiki = context.getWiki();
     boolean needsUpdate = false;
     
     try {
       doc = xwiki.getDocument(docRef, context);
     } catch (Exception e) {
       doc = new XWikiDocument(docRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(docRef);
     needsUpdate |= bclass.addTextField("prefix", "Prefix", 30);
     needsUpdate |= bclass.addTextAreaField("details", "Details", 80, 15);
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     String content = doc.getContent();
     if ((content == null) || (content.equals(""))) {
       needsUpdate = true;
       doc.setContent(" ");
     }
     
     if (needsUpdate){
       xwiki.saveDocument(doc, context);
     }
     return bclass;
   }
   
   protected BaseClass getCourseClass(XWikiContext context) throws XWikiException {
     DocumentReference docRef = new DocumentReference(context.getDatabase(), 
         "Classes", "CourseClass");
     XWikiDocument doc;
     XWiki xwiki = context.getWiki();
     boolean needsUpdate = false;
     
     try {
       doc = xwiki.getDocument(docRef, context);
     } catch (Exception e) {
       doc = new XWikiDocument(docRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(docRef);
     needsUpdate |= bclass.addDBListField("type", "Type", 3, false, "select distinct " +
         "doc.fullName,doc.title from XWikiDocument as doc, BaseObject as obj, " +
         "StringProperty as str where doc.translation=0 and doc.space='Kurstypen' and " +
         "doc.fullName=obj.name and obj.id=str.id.id and obj.className='Celements2." +
         "PageType' and str.id.name='page_type' and str.id.value='CourseType' order by " +
         "doc.title");
    needsUpdate |= bclass.addTextField("number", "Number", 30);
    needsUpdate |= bclass.addNumberField("seats", "Seats", 10, "integer");
     needsUpdate |= bclass.addTextAreaField("info", "Info", 80, 15);
     //Teacher is not mapped since a DBList can not be mapped to a String. If a mapping is
     //    needed later, this field could be changed to a String, or maybe XWiki could be 
     //    changed to be able to save it alternatively as a string
    needsUpdate |= bclass.addTextField("price", "Price", 30);
     needsUpdate |= bclass.addDBListField("teacher", "Teacher", 3, true, "select " +
         "distinct doc.fullName, doc.title from XWikiDocument as doc where doc.space=" +
         "'Teachers' and doc.name <> 'WebPreferences'");
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     String content = doc.getContent();
     if ((content == null) || (content.equals(""))) {
       needsUpdate = true;
       doc.setContent(" ");
     }
     
     if (needsUpdate){
       xwiki.saveDocument(doc, context);
     }
     return bclass;
   }
   
   protected BaseClass getCourseParticipantClass(XWikiContext context) throws XWikiException {
     DocumentReference docRef = new DocumentReference(context.getDatabase(), 
         "Classes", "CourseParticipantClass");
     XWikiDocument doc;
     XWiki xwiki = context.getWiki();
     boolean needsUpdate = false;
     
     try {
       doc = xwiki.getDocument(docRef, context);
     } catch (Exception e) {
       doc = new XWikiDocument(docRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(docRef);
 
     needsUpdate |= bclass.addTextField("title", "Title", 30);
     needsUpdate |= bclass.addTextField("firstname", "Firstname", 30);
     needsUpdate |= bclass.addTextField("lastname", "Lastname", 30);
     needsUpdate |= bclass.addTextField("address", "Address", 30);
     needsUpdate |= bclass.addTextField("zip", "ZIP", 30);
     needsUpdate |= bclass.addTextField("city", "City", 30);
     needsUpdate |= bclass.addTextField("phone", "Phone", 30);
     needsUpdate |= bclass.addTextField("email", "Email", 30);
     needsUpdate |= bclass.addDateField("dob", "Day of Birth", null, 0);
     needsUpdate |= bclass.addTextField("registrationNumber", "Registration Number", 30);
     needsUpdate |= bclass.addDateField("registrationExpiry", "Registration Expiry", null,
         0);
     needsUpdate |= bclass.addStaticListField("status", "Status", 1, false, 
         "unconfirmed|confirmed|cancelled", "select", ",|");
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     String content = doc.getContent();
     if ((content == null) || (content.equals(""))) {
       needsUpdate = true;
       doc.setContent(" ");
     }
     
     if (needsUpdate){
       xwiki.saveDocument(doc, context);
     }
     return bclass;
   }
 }
