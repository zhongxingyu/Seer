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
 package com.celements.invoice;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.common.classes.AbstractClassCollection;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.classes.BaseClass;
 
 @Component("com.celements.invoice.classcollection")
 public class InvoiceClassCollection extends AbstractClassCollection {
 
   public static final String INVOICE_CLASS_DOC = "InvoiceClass";
   public static final String INVOICE_CLASSES_SPACE = "InvoiceClasses";
 
   public static final String INVOICE_SUBSCRIPTION_ITEM_CLASS_DOC =
     "SubscriptionItemClass";
 
   public static final String INVOICE_ITEM_CLASS_DOC = "InvoiceItemClass";
 
   public static final String INVOICE_ADDRESS_CLASS_DOC = "AddressClass";
 
   private static Log LOGGER = LogFactory.getFactory().getInstance(
       InvoiceClassCollection.class);
 
   @Override
   protected Log getLogger() {
     return LOGGER;
   }
 
   public InvoiceClassCollection() {}
 
   public String getConfigName() {
     return "celInvoice";
   }
 
   @Override
   protected void initClasses() throws XWikiException {
     getInvoiceClass();
     getInvoiceSubscriptionItemClass();
     getInvoiceItemClass();
     getInvoiceAddressClass();
   }
 
   public DocumentReference getInvoiceClassRef(String wikiName) {
     return new DocumentReference(wikiName, INVOICE_CLASSES_SPACE, INVOICE_CLASS_DOC);
   }
 
   BaseClass getInvoiceClass() throws XWikiException {
     DocumentReference classRef = getInvoiceClassRef(getContext().getDatabase());
     XWikiDocument doc;
     XWiki xwiki = getContext().getWiki();
     boolean needsUpdate = false;
     
     try {
       doc = xwiki.getDocument(classRef, getContext());
     } catch (XWikiException exp) {
       LOGGER.error("Failed to get xClass document for [" + classRef + "].", exp);
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(classRef);
     
     needsUpdate |= bclass.addTextField("invoiceNumber", "Invoice Number", 30);
     needsUpdate |= bclass.addTextField("subject", "Subject", 30);
     needsUpdate |= bclass.addTextField("currency", "Currency (iso4217)", 30);
     needsUpdate |= bclass.addTextAreaField("comment", "Comment", 80, 15);
     needsUpdate |= bclass.addDateField("invoiceDate" , "Invoice Date", "dd.MM.yyyy", 0);
     needsUpdate |= bclass.addTextField("orderNumber", "Order number (use for single"
         + " invoice)", 30);
     needsUpdate |= bclass.addNumberField("totalPrice", "Total of Invoice (in smallest"
         + " unit of currency)", 5, "integer");
     needsUpdate |= bclass.addNumberField("totalVATfree", "Total VAT free of Invoice (in"
         + " smallest unit of currency)", 5, "integer");
     needsUpdate |= bclass.addNumberField("totalVATreduced", "Total VAT reduced of Invoice"
         + " (in smallest unit of currency)", 5, "integer");
     needsUpdate |= bclass.addNumberField("totalVATfull", "Total VAT full of Invoice (in"
         + " smallest unit of currency)", 5, "integer");
     needsUpdate |= bclass.addStaticListField("status", "Status", "new|printed|finance");
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
   public DocumentReference getSubscriptionItemClassRef(String wikiName) {
     return new DocumentReference(wikiName, INVOICE_CLASSES_SPACE,
         INVOICE_SUBSCRIPTION_ITEM_CLASS_DOC);
   }
 
   BaseClass getInvoiceSubscriptionItemClass() throws XWikiException {
     DocumentReference classRef = getSubscriptionItemClassRef(getContext().getDatabase());
     XWikiDocument doc;
     XWiki xwiki = getContext().getWiki();
     boolean needsUpdate = false;
     
     try {
       doc = xwiki.getDocument(classRef, getContext());
     } catch (XWikiException exp) {
       LOGGER.error("Failed to get xClass document for [" + classRef + "].", exp);
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(classRef);
     
     needsUpdate |= bclass.addTextField("subscrRef", "Subscription Reference"
         + " (wiki docRef)", 30);
     needsUpdate |= bclass.addDateField("from" , "Invoice Subscription From Date",
         "dd.MM.yyyy", 0);
     needsUpdate |= bclass.addDateField("to" , "Invoice Subscription To Date",
         "dd.MM.yyyy", 0);
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
   public DocumentReference getInvoiceItemClassRef(String wikiName) {
     return new DocumentReference(wikiName, INVOICE_CLASSES_SPACE,
         INVOICE_ITEM_CLASS_DOC);
   }
 
   BaseClass getInvoiceItemClass() throws XWikiException {
     DocumentReference classRef = getInvoiceItemClassRef(getContext().getDatabase());
     XWikiDocument doc;
     XWiki xwiki = getContext().getWiki();
     boolean needsUpdate = false;
     
     try {
       doc = xwiki.getDocument(classRef, getContext());
     } catch (XWikiException exp) {
       LOGGER.error("Failed to get xClass document for [" + classRef + "].", exp);
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(classRef);
     
     needsUpdate |= bclass.addNumberField("amount", "Amount", 5, "integer");
     needsUpdate |= bclass.addTextField("unitOfMeasure", "Unit of measure (UNSPSC-CODE)",
         30);
     needsUpdate |= bclass.addNumberField("unitPrice", "Price Unit (in smallest unit of"
         + " currency)", 5, "integer");
     needsUpdate |= bclass.addNumberField("unitOfPrice", "amount per unit-price", 5,
         "integer");
     needsUpdate |= bclass.addNumberField("vatCode", "VAT Code", 5, "integer");
     needsUpdate |= bclass.addNumberField("vatValue", "VAT Value", 5, "float");
     needsUpdate |= bclass.addTextField("description", "Description", 30);
     needsUpdate |= bclass.addNumberField("position", "Position on invoice", 5, "integer");
     needsUpdate |= bclass.addTextField("articleNr", "Article number", 30);
     needsUpdate |= bclass.addTextField("orderNumber", "Order number (use for collective"
         + " billing)", 30);
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
   public DocumentReference getInoviceAddressClassRef(String wikiName) {
     return new DocumentReference(wikiName, INVOICE_CLASSES_SPACE,
         INVOICE_ADDRESS_CLASS_DOC);
   }
 
   BaseClass getInvoiceAddressClass() throws XWikiException {
     XWikiDocument doc;
     boolean needsUpdate = false;
     DocumentReference classRef = getInoviceAddressClassRef(getContext().getDatabase());
     
     try {
       doc = getContext().getWiki().getDocument(classRef, getContext());
     } catch (XWikiException e) {
       LOGGER.error(e);
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setXClassReference(classRef);
     needsUpdate |= bclass.addTextField("company", "Company", 30);
     needsUpdate |= bclass.addTextField("title", "Title", 30);
     needsUpdate |= bclass.addTextField("first_name", "First Name", 30);
     needsUpdate |= bclass.addTextField("name", "Name", 30);
    needsUpdate |= bclass.addTextField("address", "Address", 30);
    needsUpdate |= bclass.addTextField("addressAddition", "Address Addition", 30);
    needsUpdate |= bclass.addTextField("pobox", "PO Box", 30);
     needsUpdate |= bclass.addTextField("zip", "Zip", 30);
     needsUpdate |= bclass.addTextField("city", "City", 30);
     needsUpdate |= bclass.addTextField("country", "Country", 30);
     needsUpdate |= bclass.addTextField("email", "Email", 30);
     needsUpdate |= bclass.addTextField("phone", "Phone", 30);
     needsUpdate |= bclass.addStaticListField("address_type", "Address Type", 
         "Shipping|Billing");
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 }
