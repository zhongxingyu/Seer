 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: EnumerationIdRefColumn.java,v 1.3 2003-06-21 21:38:48 shahid.shah Exp $
  */
 
 package com.netspective.axiom.schema.column.type;
 
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.schema.ColumnValue;
 import com.netspective.axiom.schema.table.type.EnumerationTable;
 import com.netspective.axiom.schema.table.type.EnumerationTableRows;
 import com.netspective.axiom.schema.table.type.EnumerationTableRow;
 import com.netspective.axiom.schema.column.ColumnValueException;
 import com.netspective.commons.text.TextUtils;
 
 public class EnumerationIdRefColumn extends IntegerColumn
 {
     public class InvalidEnumerationValueException extends ColumnValueException
     {
         private EnumerationTable table;
        private EnumerationIdRefValue value;
 
        public InvalidEnumerationValueException(EnumerationTable table, EnumerationIdRefValue value)
         {
             super("Column '"+ getQualifiedName() +"' value is invalid -- enumeration '"+ table.getName() +"' does not contain an id, caption, or abbrevation called ["+ value +"]. Available: " + TextUtils.join(table.getEnums().getValidValues(), ", "));
             this.table = table;
             this.value = value;
         }
 
         public EnumerationTable getTable()
         {
             return table;
         }
 
        public EnumerationIdRefValue getValue()
         {
             return value;
         }
     }
 
     public class EnumerationIdRefValue extends BasicColumnValue
     {
         public Class getValueHolderClass()
         {
             return Integer.class;
         }
 
         // we never really append id refs, we replace them
         public void appendText(String text)
         {
             setTextValue(text);
         }
 
         public void setTextValue(String value) throws ColumnValueException
         {
             EnumerationTable enumTable = (EnumerationTable) getForeignKey().getReferencedColumns().getFirst().getTable();
             EnumerationTableRow row = enumTable.getEnums().getByIdOrCaptionOrAbbrev(value);
             if(row != null)
                 super.setValue(row.getIdAsInteger());
             else
                throw new InvalidEnumerationValueException(enumTable, this);
         }
 
         public void setValue(Object value) throws ColumnValueException
         {
             if(value instanceof Integer)
             {
                 EnumerationTable enumTable = (EnumerationTable) getForeignKey().getReferencedColumns().getFirst().getTable();
                 EnumerationTableRow row = ((EnumerationTableRows) enumTable.getData()).getById((Integer) value);
                 if(row != null)
                     super.setValue(value);
                 else
                    throw new InvalidEnumerationValueException(enumTable, this);
             }
             else if(value != null)
                 setTextValue(value.toString());
         }
     }
 
     public EnumerationIdRefColumn(Table table)
     {
         super(table);
     }
 
     public ColumnValue constructValueInstance()
     {
         return new EnumerationIdRefValue();
     }
 }
