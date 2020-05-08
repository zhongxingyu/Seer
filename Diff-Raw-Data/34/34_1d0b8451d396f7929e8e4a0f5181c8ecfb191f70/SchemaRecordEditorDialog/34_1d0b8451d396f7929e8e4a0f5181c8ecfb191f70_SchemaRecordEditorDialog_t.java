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
 * $Id: SchemaRecordEditorDialog.java,v 1.6 2003-10-20 22:21:49 shahid.shah Exp $
  */
 
 package com.netspective.sparx.form.schema;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Map;
 import javax.naming.NamingException;
 
 import org.apache.commons.jexl.Expression;
 import org.apache.commons.jexl.ExpressionFactory;
 import org.apache.commons.jexl.JexlContext;
 import org.apache.commons.jexl.JexlHelper;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.axiom.schema.Column;
 import com.netspective.axiom.schema.Columns;
 import com.netspective.axiom.schema.ForeignKey;
 import com.netspective.axiom.schema.Row;
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.schema.ColumnValue;
 import com.netspective.axiom.schema.ColumnValues;
 import com.netspective.axiom.schema.Schema;
 import com.netspective.axiom.schema.constraint.ParentForeignKey;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.ValueSources;
 import com.netspective.commons.xml.template.TemplateProducerParent;
 import com.netspective.commons.xml.template.TemplateProducer;
 import com.netspective.commons.xml.template.TemplateProducers;
 import com.netspective.commons.xml.template.Template;
 import com.netspective.commons.xml.template.TemplateElement;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.sparx.form.Dialog;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.DialogContextUtils;
 import com.netspective.sparx.form.DialogFlags;
 import com.netspective.sparx.form.DialogPerspectives;
 import com.netspective.sparx.form.DialogsPackage;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFields;
 import com.netspective.sparx.form.handler.DialogExecuteHandlers;
 import com.netspective.sparx.Project;
 
 public class SchemaRecordEditorDialog extends Dialog implements TemplateProducerParent
 {
     private static final String ATTRNAME_CONDITION = "_condition";
     private static final String ATTRNAME_PRIMARYKEY_VALUE = "_pk-value";
     private static final String ATTRNAME_AUTOMAP = "_auto-map";
 
     protected class InsertDataTemplate extends TemplateProducer
     {
         public InsertDataTemplate()
         {
             super("/dialog/" + getQualifiedName() + "/on-add-data", "on-add-data", null, null, false, false);
         }
 
         public String getTemplateName(String url, String localName, String qName, Attributes attributes) throws SAXException
         {
             return getName();
         }
     }
 
     protected class EditDataTemplate extends TemplateProducer
     {
         public EditDataTemplate()
         {
             super("/dialog/" + getQualifiedName() + "/on-edit-data", "on-edit-data", null, null, false, false);
         }
 
         public String getTemplateName(String url, String localName, String qName, Attributes attributes) throws SAXException
         {
             return getName();
         }
     }
 
     protected class DeleteDataTemplate extends TemplateProducer
     {
         public DeleteDataTemplate()
         {
             super("/dialog/" + getQualifiedName() + "/on-delete-data", "on-delete-data", null, null, false, false);
         }
 
         public String getTemplateName(String url, String localName, String qName, Attributes attributes) throws SAXException
         {
             return getName();
         }
     }
 
     protected class SchemaTableTemplateElement
     {
         private SchemaRecordEditorDialogContext dialogContext;
         private TemplateElement templateElement;
         private String tableName;
         private Schema schema;
         private Table table;
 
         public SchemaTableTemplateElement(SchemaRecordEditorDialogContext sredc, TemplateElement templateElement)
         {
             this.dialogContext = sredc;
             this.templateElement = templateElement;
 
             String[] tableNameParts = TextUtils.split(templateElement.getElementName(), ".", false);
             if(tableNameParts.length == 1)
             {
                 schema = sredc.getProject().getSchemas().getDefault();
                 tableName = tableNameParts[0];
             }
             else
             {
                 schema = sredc.getProject().getSchemas().getByNameOrXmlNodeName(tableNameParts[0]);
                 if(schema == null)
                 {
                     getLog().error("Unable to find schema '"+ tableNameParts[0] +"' in SchemaRecordEditorDialog '"+ getQualifiedName() +"'");
                     return;
                 }
 
                 tableName = tableNameParts[1];
             }
 
             table = schema.getTables().getByNameOrXmlNodeName(tableName);
             if(table == null)
             {
                 getLog().error("Unable to find table '"+ tableName +"' in schema '"+ schema.getName() +"' for SchemaRecordEditorDialog '"+ getQualifiedName() +"'");
                 return;
             }
 
             autoMapColumnNamesToFieldNames();
         }
 
         private void autoMapColumnNamesToFieldNames()
         {
             String autoMapAttrValue = templateElement.getAttributes().getValue(ATTRNAME_AUTOMAP);
             if(autoMapAttrValue == null || autoMapAttrValue.length() == 0)
                 return;
 
             AttributesImpl attrs = new AttributesImpl(templateElement.getAttributes());
             attrs.removeAttribute(attrs.getIndex(ATTRNAME_AUTOMAP));
 
             if(autoMapAttrValue.equals("*"))
             {
                 Columns columns = table.getColumns();
                 DialogFields fields = getFields();
                 for(int i = 0; i < columns.size(); i++)
                 {
                     Column column = columns.get(i);
                     DialogField field = fields.getByName(column.getName());
 
                     // make sure this dialog has the given column and add the column
                     if(field != null)
                        attrs.addAttribute(null, null, column.getName(), "CDATA", column.getName());
                 }
             }
             else
             {
                 String[] columnNames = TextUtils.split(autoMapAttrValue, ",", true);
                 for(int i = 0; i < columnNames.length; i++)
                     attrs.addAttribute(null, null, columnNames[i], "CDATA", columnNames[i]);
             }
 
             templateElement.setAttributes(attrs);
         }
 
         public ValueSource getPrimaryKeyValueSource()
         {
             // see if a primary key value is provided -- if it is, we're going to populate using the primary key value
             String primaryKeyValueSpec = templateElement.getAttributes().getValue(ATTRNAME_PRIMARYKEY_VALUE);
             if(primaryKeyValueSpec == null || primaryKeyValueSpec.length() == 0)
             {
                primaryKeyValueSpec = templateElement.getAttributes().getValue(table.getPrimaryKeyColumns().getSole().getName());
                 if(primaryKeyValueSpec == null || primaryKeyValueSpec.length() == 0)
                    primaryKeyValueSpec = templateElement.getAttributes().getValue(table.getPrimaryKeyColumns().getSole().getXmlNodeName());
             }
 
             return ValueSources.getInstance().getValueSourceOrStatic(primaryKeyValueSpec);
         }
 
         public SchemaRecordEditorDialogContext getDialogContext()
         {
             return dialogContext;
         }
 
         public TemplateElement getTemplateElement()
         {
             return templateElement;
         }
 
         public String getTableName()
         {
             return tableName;
         }
 
         public Schema getSchema()
         {
             return schema;
         }
 
         public Table getTable()
         {
             return table;
         }
 
         public boolean isTableFound()
         {
             return table != null;
         }
 
     }
 
     private ValueSource dataSrc;
     private InsertDataTemplate insertDataTemplateProducer;
     private EditDataTemplate editDataTemplateProducer;
     private DeleteDataTemplate deleteDataTemplateProducer;
     private TemplateProducers templateProducers;
 
     public SchemaRecordEditorDialog(Project project)
     {
         super(project);
         setDialogContextClass(SchemaRecordEditorDialogContext.class);
     }
 
     public SchemaRecordEditorDialog(Project project, DialogsPackage pkg)
     {
         super(project, pkg);
         setDialogContextClass(SchemaRecordEditorDialogContext.class);
     }
 
     public TemplateProducers getTemplateProducers()
     {
         if(templateProducers == null)
         {
             templateProducers = new TemplateProducers();
             insertDataTemplateProducer = new InsertDataTemplate();
             editDataTemplateProducer = new EditDataTemplate();
             deleteDataTemplateProducer = new DeleteDataTemplate();
             templateProducers.add(insertDataTemplateProducer);
             templateProducers.add(editDataTemplateProducer);
             templateProducers.add(deleteDataTemplateProducer);
         }
         return templateProducers;
     }
 
     public DialogFlags createDialogFlags()
     {
         return new SchemaRecordEditorDialogFlags();
     }
 
     public ValueSource getDataSrc()
     {
         return dataSrc;
     }
 
     public void setDataSrc(ValueSource dataSrc)
     {
         this.dataSrc = dataSrc;
     }
 
     public boolean isConditionalExpressionTrue(SchemaRecordEditorDialogContext tdc, String exprText)
     {
         Object result = null;
         try
         {
             Expression e = ExpressionFactory.createExpression(exprText);
             JexlContext jc = JexlHelper.createContext();
             Map vars = new HashMap();
             vars.put("vc", tdc);
             jc.setVars(vars);
             result = e.evaluate(jc);
 
             if(result instanceof Boolean)
                 return ((Boolean) result).booleanValue();
             else
                 return false;
         }
         catch (Exception e)
         {
             getLog().error("Unable to evaluate '"+ exprText +"': ", e);
             return false;
         }
     }
 
     /******************************************************************************************************************
      ** Data population (placing column values into fields) methods                                                  **
      ******************************************************************************************************************/
 
     public void populateFieldValuesUsingAttributes(SchemaRecordEditorDialogContext tdc, Row row, TemplateElement templateElement)
     {
         DialogContext.DialogFieldStates states = tdc.getFieldStates();
         ColumnValues columnValues = row.getColumnValues();
 
         Attributes templateAttributes = templateElement.getAttributes();
         for(int i = 0; i < templateAttributes.getLength(); i++)
         {
             String columnName = templateAttributes.getQName(i);
             String columnTextValue = templateAttributes.getValue(i);
 
             // these are private "instructions"
             if(columnName.startsWith("_"))
                 continue;
 
             ColumnValue columnValue = columnValues.getByNameOrXmlNodeName(columnName);
             if(columnValue == null)
             {
                 getLog().error("Can't populateFieldValuesUsingAttributes -- Table '"+ row.getTable().getName() +"' does not have a column named '"+ columnName +"'.");
                 continue;
             }
 
             // if the column value is a value source spec, we don't map the value to a field
             ValueSource vs = ValueSources.getInstance().getValueSource(ValueSources.createSpecification(columnTextValue), ValueSources.VSNOTFOUNDHANDLER_NULL);
             if(vs == null)
             {
                 DialogField.State fieldState = states.getState(columnTextValue, null);
                 if(fieldState != null)
                     fieldState.getValue().copyValueByReference(columnValue);
                 else
                     getLog().error("Can't populateFieldValuesUsingAttributes -- Table Table '"+ row.getTable().getName() +"' is mapping a column called '"+ columnName +"' to the field '"+ columnTextValue +"' but that field was not found.");
             }
         }
     }
 
     public void populateDataUsingTemplateElement(SchemaRecordEditorDialogContext sredc, ConnectionContext cc, TemplateElement templateElement) throws NamingException, SQLException
     {
         SchemaTableTemplateElement stte = new SchemaTableTemplateElement(sredc, templateElement);
         if(! stte.isTableFound())
             return;
 
         // now we have the table we're dealing with for this template element
         Table table = stte.getTable();
 
         ValueSource primaryKeyValueSource = stte.getPrimaryKeyValueSource();
         if(primaryKeyValueSource != null)
         {
             final Object primaryKeyValue = primaryKeyValueSource.getValue(sredc).getValue();
             Row activeRow = table.getRowByPrimaryKeys(cc, new Object[] { primaryKeyValue }, null);
             if(activeRow != null)
                 populateFieldValuesUsingAttributes(sredc, activeRow, templateElement);
             else
             {
                if(! (sredc.editingData() && getDialogFlags().flagIsSet(SchemaRecordEditorDialogFlags.ALLOW_INSERT_IF_EDIT_PK_NOT_FOUND)))
                     sredc.getValidationContext().addValidationError("Unable to locate primary key using value {0}={1} in table {2}.", new Object[] { primaryKeyValueSource, primaryKeyValueSource.getTextValue(sredc), table.getName() });
             }
         }
         else
             sredc.getValidationContext().addValidationError("Unable to locate primary key for table {0} because value source is NULL.", new Object[] { table.getName() });
     }
 
     public void populateValuesUsingTemplateProducer(DialogContext dc, ConnectionContext cc, TemplateProducer templateProducer) throws NamingException, SQLException
     {
         if(templateProducer == null)
             return;
 
         final List templateInstances = templateProducer.getInstances();
         if(templateInstances.size() == 0)
             return;
 
         // make sure to get the last template only because inheritance may have create multiples
         Template template = (Template) templateInstances.get(templateInstances.size()-1);
         List childTableElements = template.getChildren();
 
         for(int i = 0; i < childTableElements.size(); i++)
         {
             TemplateElement childTableElement = (TemplateElement) childTableElements.get(i);
             populateDataUsingTemplateElement((SchemaRecordEditorDialogContext) dc, cc, childTableElement);
         }
     }
 
     public void populateValues(DialogContext dc, int formatType)
     {
         if(dc.isInitialEntry() && ! dc.addingData())
         {
             ConnectionContext cc = null;
             try
             {
                 cc = dc.getConnection(dataSrc != null ? dataSrc.getTextValue(dc) : null, false);
                 switch((int) dc.getPerspectives().getFlags())
                 {
                     case DialogPerspectives.EDIT:
                     case DialogPerspectives.PRINT:
                     case DialogPerspectives.CONFIRM:
                         populateValuesUsingTemplateProducer(dc, cc, editDataTemplateProducer);
                         break;
 
                     case DialogPerspectives.DELETE:
                         populateValuesUsingTemplateProducer(dc, cc, deleteDataTemplateProducer);
                         break;
                 }
             }
             catch (SQLException e)
             {
                 getLog().error("Error in populateValues for perspective " + dc.getPerspectives(), e);
             }
             catch (NamingException e)
             {
                 getLog().error("Error in populateValues for perspective " + dc.getPerspectives(), e);
             }
             finally
             {
                 try
                 {
                     if(cc != null) cc.close();
                 }
                 catch (SQLException e)
                 {
                     getLog().error("Unable to close connection in populateValues()", e);
                 }
             }
         }
 
         super.populateValues(dc, formatType);
     }
 
     /******************************************************************************************************************
      ** Data insert methods                                                                                          **
      ******************************************************************************************************************/
 
     public void addDataUsingTemplateElement(SchemaRecordEditorDialogContext sredc, ConnectionContext cc, TemplateElement templateElement, Row parentRow) throws SQLException
     {
         SchemaTableTemplateElement stte = new SchemaTableTemplateElement(sredc, templateElement);
         if(! stte.isTableFound())
             return;
 
         // now we have the table we're dealing with for this template element
         Table table = stte.getTable();
 
         Row activeRow = null;
 
         // find the connector from the child table to the parent table if one is available
         Columns parentKeyCols = table.getForeignKeyColumns(ForeignKey.FKEYTYPE_PARENT);
         if(parentKeyCols.size() == 1)
         {
             Column connector = parentKeyCols.getSole();
             activeRow = table.createRow((ParentForeignKey) connector.getForeignKey(), parentRow);
         }
         else
             activeRow = table.createRow();
 
         ColumnValues columnValues = activeRow.getColumnValues();
         boolean doInsert = true;
 
         // each of the attributes provided in the template are supposed to column-name="column-value" where
         // column-value may be a static string which refers to a dialog field name or a value source specification
         // which refers to some dynamic value
         Attributes templateAttributes = templateElement.getAttributes();
         for(int i = 0; i < templateAttributes.getLength(); i++)
         {
             String columnName = templateAttributes.getQName(i);
             String columnTextValue = templateAttributes.getValue(i);
 
             // if we have an attribute called _condition then it's a JSTL-style expression that should return true if
             // we want to do this insert or false if we don't
             if(columnName.equalsIgnoreCase(ATTRNAME_CONDITION))
             {
                 doInsert = isConditionalExpressionTrue(sredc, columnTextValue);
                 if(! doInsert)
                     break; // don't bother setting values since we're not inserting
             }
 
             // these are private "instructions"
             if(columnName.startsWith("_"))
                 continue;
 
             ColumnValue columnValue = columnValues.getByNameOrXmlNodeName(columnName);
             if(columnValue == null)
             {
                 getLog().error("Table '"+ table.getName() +"' does not have a column named '"+ columnName +"'.");
                 continue;
             }
 
             // if the column value is a value source spec, we get the value from the VS otherwise it's a field name in the active dialog
             ValueSource vs = ValueSources.getInstance().getValueSource(ValueSources.createSpecification(columnTextValue), ValueSources.VSNOTFOUNDHANDLER_NULL);
             if(vs == null)
                 DialogContextUtils.getInstance().populateColumnValueWithFieldValue(sredc, columnValue, columnTextValue);
             else
                 columnValue.setTextValue(vs.getTextValue(sredc));
         }
 
         if(doInsert)
         {
             table.insert(cc, activeRow);
             sredc.getRowsAdded().add(activeRow);
 
             // now recursively add children if any are available
             List childTableElements = templateElement.getChildren();
             for(int i = 0; i < childTableElements.size(); i++)
             {
                 TemplateElement childTableElement = (TemplateElement) childTableElements.get(i);
                 addDataUsingTemplateElement(sredc, cc, childTableElement, activeRow);
             }
         }
     }
 
     public void addDataUsingTemplate(SchemaRecordEditorDialogContext sredc, ConnectionContext cc) throws SQLException
     {
         if(insertDataTemplateProducer == null)
             return;
 
         final List templateInstances = insertDataTemplateProducer.getInstances();
         if(templateInstances.size() == 0)
             return;
 
         // make sure to get the last template only because inheritance may have create multiples
         Template insertDataTemplate = (Template) templateInstances.get(templateInstances.size()-1);
         List childTableElements = insertDataTemplate.getChildren();
         for(int i = 0; i < childTableElements.size(); i++)
         {
             TemplateElement childTableElement = (TemplateElement) childTableElements.get(i);
             addDataUsingTemplateElement(sredc, cc, childTableElement, null);
         }
     }
 
     /******************************************************************************************************************
      ** Data update/edit methods                                                                                     **
      ******************************************************************************************************************/
 
     public void editDataUsingTemplateElement(SchemaRecordEditorDialogContext sredc, ConnectionContext cc, TemplateElement templateElement) throws NamingException, SQLException
     {
         SchemaTableTemplateElement stte = new SchemaTableTemplateElement(sredc, templateElement);
         if(! stte.isTableFound())
             return;
 
         // now we have the table we're dealing with for this template element
         Table table = stte.getTable();
 
         ValueSource primaryKeyValueSource = stte.getPrimaryKeyValueSource();
         if(primaryKeyValueSource == null)
             sredc.getValidationContext().addValidationError("Unable to locate primary key for table {0} because value source is NULL.", new Object[] { table.getName() });
 
         final Object primaryKeyValue = primaryKeyValueSource.getValue(sredc).getValue();
         Row activeRow = table.getRowByPrimaryKeys(cc, new Object[] { primaryKeyValue }, null);
         if(activeRow == null)
         {
             if(getDialogFlags().flagIsSet(SchemaRecordEditorDialogFlags.ALLOW_INSERT_IF_EDIT_PK_NOT_FOUND))
                 addDataUsingTemplateElement(sredc, cc, templateElement, null);
             else
                 sredc.getValidationContext().addValidationError("Unable to locate primary key using value {0}={1} in table {2}.", new Object[] { primaryKeyValueSource, primaryKeyValueSource.getTextValue(sredc), table.getName() });
             return;
         }
 
         ColumnValues columnValues = activeRow.getColumnValues();
         boolean doUpdate = true;
 
         // each of the attributes provided in the template are supposed to column-name="column-value" where
         // column-value may be a static string which refers to a dialog field name or a value source specification
         // which refers to some dynamic value
         Attributes templateAttributes = templateElement.getAttributes();
         for(int i = 0; i < templateAttributes.getLength(); i++)
         {
             String columnName = templateAttributes.getQName(i);
             String columnTextValue = templateAttributes.getValue(i);
 
             // if we have an attribute called _condition then it's a JSTL-style expression that should return true if
             // we want to do this update or false if we don't
             if(columnName.equalsIgnoreCase(ATTRNAME_CONDITION))
             {
                 doUpdate = isConditionalExpressionTrue(sredc, columnTextValue);
                 if(! doUpdate)
                     break; // don't bother setting values since we're not inserting
             }
 
             // these are private "instructions"
             if(columnName.startsWith("_"))
                 continue;
 
             ColumnValue columnValue = columnValues.getByNameOrXmlNodeName(columnName);
             if(columnValue == null)
             {
                 getLog().error("Table '"+ table.getName() +"' does not have a column named '"+ columnName +"'.");
                 continue;
             }
 
             // if the column value is a value source spec, we get the value from the VS otherwise it's a field name in the active dialog
             ValueSource vs = ValueSources.getInstance().getValueSource(ValueSources.createSpecification(columnTextValue), ValueSources.VSNOTFOUNDHANDLER_NULL);
             if(vs == null)
                 DialogContextUtils.getInstance().populateColumnValueWithFieldValue(sredc, columnValue, columnTextValue);
             else
                 columnValue.setTextValue(vs.getTextValue(sredc));
         }
 
         if(doUpdate)
         {
             table.update(cc, activeRow);
             sredc.getRowsUpdated().add(activeRow);
 
             // now recursively add children if any are available
             List childTableElements = templateElement.getChildren();
             for(int i = 0; i < childTableElements.size(); i++)
             {
                 TemplateElement childTableElement = (TemplateElement) childTableElements.get(i);
                 editDataUsingTemplateElement(sredc, cc, childTableElement);
             }
         }
     }
 
     public void editDataUsingTemplate(SchemaRecordEditorDialogContext sredc, ConnectionContext cc) throws NamingException, SQLException
     {
         if(editDataTemplateProducer == null)
             return;
 
         final List templateInstances = editDataTemplateProducer.getInstances();
         if(templateInstances.size() == 0)
             return;
 
         // make sure to get the last template only because inheritance may have create multiples
         Template editDataTemplate = (Template) templateInstances.get(templateInstances.size()-1);
         List childTableElements = editDataTemplate.getChildren();
         for(int i = 0; i < childTableElements.size(); i++)
         {
             TemplateElement childTableElement = (TemplateElement) childTableElements.get(i);
             editDataUsingTemplateElement(sredc, cc, childTableElement);
         }
     }
 
     /******************************************************************************************************************
      ** Data update/edit methods                                                                                     **
      ******************************************************************************************************************/
 
     public void deleteDataUsingTemplateElement(SchemaRecordEditorDialogContext sredc, ConnectionContext cc, TemplateElement templateElement) throws NamingException, SQLException
     {
         SchemaTableTemplateElement stte = new SchemaTableTemplateElement(sredc, templateElement);
         if(! stte.isTableFound())
             return;
 
         // now we have the table we're dealing with for this template element
         Table table = stte.getTable();
 
         ValueSource primaryKeyValueSource = stte.getPrimaryKeyValueSource();
         if(primaryKeyValueSource == null)
             sredc.getValidationContext().addValidationError("Unable to locate primary key for table {0}.", new Object[] { table.getName() });
 
         final Object primaryKeyValue = primaryKeyValueSource.getValue(sredc).getValue();
         Row activeRow = table.getRowByPrimaryKeys(cc, new Object[] { primaryKeyValue }, null);
         if(activeRow == null)
         {
             sredc.getValidationContext().addValidationError("Unable to locate primary key using value {0}={1} in table {2}.", new Object[] { primaryKeyValueSource, primaryKeyValueSource.getTextValue(sredc), table.getName() });
             return;
         }
 
         ColumnValues columnValues = activeRow.getColumnValues();
         boolean doDelete = true;
 
         // each of the attributes provided in the template are supposed to column-name="column-value" where
         // column-value may be a static string which refers to a dialog field name or a value source specification
         // which refers to some dynamic value
         Attributes templateAttributes = templateElement.getAttributes();
         for(int i = 0; i < templateAttributes.getLength(); i++)
         {
             String columnName = templateAttributes.getQName(i);
             String columnTextValue = templateAttributes.getValue(i);
 
             // if we have an attribute called _condition then it's a JSTL-style expression that should return true if
             // we want to do this delete or false if we don't
             if(columnName.equalsIgnoreCase(ATTRNAME_CONDITION))
             {
                 doDelete = isConditionalExpressionTrue(sredc, columnTextValue);
                 if(! doDelete)
                     break; // don't bother setting values since we're not inserting
             }
 
             // these are private "instructions"
             if(columnName.startsWith("_"))
                 continue;
 
             ColumnValue columnValue = columnValues.getByNameOrXmlNodeName(columnName);
             if(columnValue == null)
             {
                 getLog().error("Table '"+ table.getName() +"' does not have a column named '"+ columnName +"'.");
                 continue;
             }
 
             // if the column value is a value source spec, we get the value from the VS otherwise it's a field name in the active dialog
             ValueSource vs = ValueSources.getInstance().getValueSource(ValueSources.createSpecification(columnTextValue), ValueSources.VSNOTFOUNDHANDLER_NULL);
             if(vs == null)
                 DialogContextUtils.getInstance().populateColumnValueWithFieldValue(sredc, columnValue, columnTextValue);
             else
                 columnValue.setTextValue(vs.getTextValue(sredc));
         }
 
         if(doDelete)
         {
             table.delete(cc, activeRow);
             sredc.getRowsDeleted().add(activeRow);
 
             // now recursively add children if any are available
             List childTableElements = templateElement.getChildren();
             for(int i = 0; i < childTableElements.size(); i++)
             {
                 TemplateElement childTableElement = (TemplateElement) childTableElements.get(i);
                 deleteDataUsingTemplateElement(sredc, cc, childTableElement);
             }
         }
     }
 
     public void deleteDataUsingTemplate(SchemaRecordEditorDialogContext sredc, ConnectionContext cc) throws NamingException, SQLException
     {
         if(deleteDataTemplateProducer == null)
             return;
 
         final List templateInstances = deleteDataTemplateProducer.getInstances();
         if(templateInstances.size() == 0)
             return;
 
         // make sure to get the last template only because inheritance may have create multiples
         Template deleteDataTemplate = (Template) templateInstances.get(templateInstances.size()-1);
         List childTableElements = deleteDataTemplate.getChildren();
         for(int i = 0; i < childTableElements.size(); i++)
         {
             TemplateElement childTableElement = (TemplateElement) childTableElements.get(i);
             deleteDataUsingTemplateElement(sredc, cc, childTableElement);
         }
     }
 
     /******************************************************************************************************************
      ** Default execute method                                                                                       **
      ******************************************************************************************************************/
 
     public void execute(Writer writer, DialogContext dc) throws IOException
     {
         if(dc.executeStageHandled())
             return;
 
         SchemaRecordEditorDialogContext sredc = ((SchemaRecordEditorDialogContext) dc);
 
         ConnectionContext cc = null;
         DialogExecuteHandlers handlers = getExecuteHandlers();
         try
         {
             cc = dc.getConnection(dataSrc != null ? dataSrc.getTextValue(dc) : null, true);
         }
         catch (Exception e)
         {
             handlePostExecuteException(writer, dc, dc.getPerspectives() + ": unable to establish connection", e);
             return;
         }
 
         try
         {
             switch((int) dc.getPerspectives().getFlags())
             {
                 case DialogPerspectives.ADD:
                     addDataUsingTemplate(sredc, cc);
                     break;
 
                 case DialogPerspectives.EDIT:
                     editDataUsingTemplate(sredc, cc);
                     break;
 
                 case DialogPerspectives.DELETE:
                     deleteDataUsingTemplate(sredc, cc);
                     break;
             }
 
             if(handlers.size() > 0)
                 handlers.handleDialogExecute(writer, dc);
 
             cc.commitAndClose();
             dc.setExecuteStageHandled(true);
             handlePostExecute(writer, dc);
         }
         catch (Exception e)
         {
             e.printStackTrace();
             try
             {
                 cc.rollbackAndClose();
             }
             catch (SQLException e1)
             {
                 getLog().error("Error while rolling back DML", e1);
             }
             handlePostExecuteException(writer, dc, dc.getPerspectives().getFlagsText(), e);
         }
     }
 }
