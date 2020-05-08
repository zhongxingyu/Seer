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
 package com.celements.invoice.service;
 
 import groovy.lang.Singleton;
 
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 import org.xwiki.query.Query;
 import org.xwiki.query.QueryException;
 import org.xwiki.query.QueryManager;
 
 import com.celements.invoice.InvoiceClassCollection;
 import com.xpn.xwiki.XWikiContext;
 
 @Component
 @Singleton
 public class InvoiceService implements IInvoiceServiceRole {
 
   private static Log LOGGER = LogFactory.getFactory().getInstance(InvoiceService.class);
 
   @Requirement
   QueryManager query;
 
   @Requirement
   Execution execution;
 
   private XWikiContext getContext() {
     return (XWikiContext)execution.getContext().getProperty("xwikicontext");
   }
 
   synchronized public String getNewInvoiceNumber() {
     int latestInvoiceNumber = getLatestInvoiceNumber() + 1;
     return Integer.toString(latestInvoiceNumber);
   }
 
   private int getLatestInvoiceNumber() {
     Integer latestInvoiceNumberFromDb = getLatestInvoiceNumberFromDb();
     int minInvoiceNumberFromConfig = getContext().getWiki().getXWikiPreferenceAsInt(
         "minInvoiceNumber", "com.celements.invoice.minInvoiceNumber", 1, getContext());
     if ((latestInvoiceNumberFromDb == null)
         || (latestInvoiceNumberFromDb < minInvoiceNumberFromConfig)) {
       return minInvoiceNumberFromConfig;
     } else {
       return latestInvoiceNumberFromDb;
     }
   }
 
   private String getLatestInvoiceNumberXWQL() {
     return "select max(invoice.invoiceNumber) from "
       + InvoiceClassCollection.INVOICE_CLASSES_SPACE + "."
       + InvoiceClassCollection.INVOICE_CLASS_DOC;
   }
 
   private Integer getLatestInvoiceNumberFromDb() {
     try {
      List<String> result = query.createQuery(getLatestInvoiceNumberXWQL(), Query.XWQL
           ).execute();
       if (!result.isEmpty()) {
        return Integer.parseInt(result.get(0)); 
       }
     } catch (QueryException exp) {
       LOGGER.error("Failed to get latest invoice number from db.", exp);
     }
     return null;
   }
 
 }
