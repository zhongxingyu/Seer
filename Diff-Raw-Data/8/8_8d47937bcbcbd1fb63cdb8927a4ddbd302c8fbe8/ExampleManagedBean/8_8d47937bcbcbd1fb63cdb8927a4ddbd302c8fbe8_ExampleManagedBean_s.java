 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.example;
 
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.cmis.search.CmisResultRow;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.value.FxDate;
 import com.flexive.shared.value.FxHTML;
 import java.util.Date;
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import org.apache.commons.lang.StringUtils;
 
 /**
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 @ManagedBean
 @RequestScoped
 public class ExampleManagedBean {
     private String name;
     private List<CmisResultRow> resultRows;
     private FxHTML htmlValue;
     private FxDate dateValue;
 
     public String getMessage() {
         return "Hello JSF2 world!";
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getReverseName() {
         return name == null ? null : StringUtils.reverse(name);
     }
 
     public List<CmisResultRow> getResultRows() throws FxApplicationException {
         if (resultRows == null) {
             resultRows = EJBLookup.getCmisSearchEngine().search("SELECT * from cmis:document").getRows();
         }
         return resultRows;
     }
 
     public FxPK getUserContactData() {
         return FxContext.getUserTicket().getContactData();
     }
 
     public FxHTML getHtmlValue() {
         if (htmlValue == null) {
             htmlValue = new FxHTML(false, "");
         }
         return htmlValue;
     }
 
     public void setHtmlValue(FxHTML htmlValue) {
         this.htmlValue = htmlValue;
     }
 
     public FxDate getDateValue() {
         if (dateValue == null) {
             dateValue = new FxDate(false, new Date());
         }
         return dateValue;
     }
 
     public void setDateValue(FxDate dateValue) {
         this.dateValue = dateValue;
     }
 
 
 }
