 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
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
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.sparx.form.schema;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.naming.NamingException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.axiom.DatabasePolicies;
 import com.netspective.axiom.schema.Column;
 import com.netspective.axiom.schema.ColumnValue;
 import com.netspective.axiom.schema.ColumnValues;
 import com.netspective.axiom.schema.Columns;
 import com.netspective.axiom.schema.ForeignKey;
 import com.netspective.axiom.schema.Index;
 import com.netspective.axiom.schema.IndexColumns;
 import com.netspective.axiom.schema.Indexes;
 import com.netspective.axiom.schema.Row;
 import com.netspective.axiom.schema.Schema;
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.schema.constraint.ParentForeignKey;
 import com.netspective.axiom.sql.DbmsSqlText;
 import com.netspective.axiom.sql.QueryResultSet;
 import com.netspective.axiom.sql.dynamic.QueryDefnSelect;
 import com.netspective.axiom.value.source.SqlExpressionValueSource;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.value.Value;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.ValueSources;
 import com.netspective.commons.xml.template.Template;
 import com.netspective.commons.xml.template.TemplateElement;
 import com.netspective.commons.xml.template.TemplateNode;
 import com.netspective.commons.xml.template.TemplateProducer;
 import com.netspective.commons.xml.template.TemplateProducerParent;
 import com.netspective.commons.xml.template.TemplateProducers;
 import com.netspective.sparx.Project;
 import com.netspective.sparx.form.Dialog;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.DialogContextUtils;
 import com.netspective.sparx.form.DialogExecuteException;
 import com.netspective.sparx.form.DialogFlags;
 import com.netspective.sparx.form.DialogPerspectives;
 import com.netspective.sparx.form.DialogsPackage;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFieldFlags;
 import com.netspective.sparx.form.field.DialogFieldStates;
 import com.netspective.sparx.form.field.DialogFields;
 import com.netspective.sparx.form.handler.DialogExecuteHandlers;
 import com.netspective.sparx.panel.editor.PanelEditor;
 import com.netspective.sparx.panel.editor.PanelEditorState;
 import com.netspective.sparx.panel.editor.ReportPanelEditorContentElement;
 
 public class SchemaRecordEditorDialog extends Dialog implements TemplateProducerParent
 {
     public static final String ELEMNAME_CHOOSE = "choose";
     public static final String ELEMNAME_WHEN = "when";
     public static final String ELEMNAME_OTHERWISE = "otherwise";
     public static final String ELEMNAME_IF = "if";
     public static final String ATTRNAME_TEST = "test";
     public static final String ATTRNAME_TEST_VS = "test-vs";
     public static final String ATTRNAME_CONDITION = "_condition";
     public static final String ATTRNAME_PRIMARYKEY_VALUE = "_pk-value";
     public static final String ATTRNAME_AUTOMAP = "_auto-map";
     public static final String ATTRNAME_LOOP = "_loop-column";
 
     protected class ConditionalTemplateResult
     {
         private boolean exprResult;
         private TemplateElement templateElement;
 
         public ConditionalTemplateResult(boolean exprResult, TemplateElement templateElement)
         {
             this.exprResult = exprResult;
             this.templateElement = templateElement;
         }
 
         public boolean isExprResultTrue()
         {
             return exprResult;
         }
 
         public TemplateElement getTemplateElement()
         {
             return templateElement;
         }
     }
 
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
 
             String[] tableNameParts = TextUtils.getInstance().split(templateElement.getElementName(), ".", false);
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
                     getLog().error("Unable to find schema '" + tableNameParts[0] + "' in SchemaRecordEditorDialog '" + getQualifiedName() + "'");
                     return;
                 }
 
                 tableName = tableNameParts[1];
             }
 
             table = schema.getTables().getByNameOrXmlNodeName(tableName);
             if(table == null)
             {
                 getLog().error("Unable to find table '" + tableName + "' in schema '" + schema.getName() + "' for SchemaRecordEditorDialog '" + getQualifiedName() + "'");
                 return;
             }
 
             //TODO: either synchronize this or take care of thread-safety issue (changing a shared resource!)
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
                 String[] columnNames = TextUtils.getInstance().split(autoMapAttrValue, ",", true);
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
                 HttpServletRequest request = dialogContext.getHttpRequest();
                 if(request.getAttribute(PanelEditor.PANEL_EDITOR_REQ_ATTRIBUTE_PREFIX) != null)
                 {
                     PanelEditorState state = (PanelEditorState) request.getAttribute(PanelEditor.PANEL_EDITOR_REQ_ATTRIBUTE_PREFIX);
                     primaryKeyValueSpec = ReportPanelEditorContentElement.getPkValueFromState(state);
                 }
                 else
                 {
                     primaryKeyValueSpec = templateElement.getAttributes().getValue(table.getPrimaryKeyColumns().getSole().getName());
                     if(primaryKeyValueSpec == null || primaryKeyValueSpec.length() == 0)
                         primaryKeyValueSpec = templateElement.getAttributes().getValue(table.getPrimaryKeyColumns().getSole().getXmlNodeName());
                 }
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
 
     private void setColumnSqlExpression(ColumnValue columnValue, ValueSource vs, SchemaRecordEditorDialogContext sredc)
     {
         DbmsSqlText sqlText = columnValue.createSqlExpr();
         DatabasePolicies.DatabasePolicyEnumeratedAttribute dbms = new DatabasePolicies.DatabasePolicyEnumeratedAttribute();
         dbms.setValue(vs.getSpecification().getProcessingInstructions());
         sqlText.setDbms(dbms);
         sqlText.setSql(vs.getTextValue(sredc));
         columnValue.addSqlExpr(sqlText);
     }
 
     /**
      * ***************************************************************************************************************
      * * Conditional Data methods                                                                                   **
      * ***************************************************************************************************************
      */
 
     public boolean isConditionalTestExpressionTrue(SchemaRecordEditorDialogContext sredc, TemplateElement element)
     {
         String testExpr = element.getAttributes().getValue(ATTRNAME_TEST);
         if(testExpr != null && testExpr.length() > 0)
             return sredc.isConditionalExpressionTrue(testExpr, null);
 
         testExpr = element.getAttributes().getValue(ATTRNAME_TEST_VS);
         if(testExpr != null && testExpr.length() > 0)
             return ValueSources.getInstance().getValueSourceOrStatic(testExpr).getValue(sredc).getBooleanValue();
 
         getLog().error("'test' attribute or 'test-vs' attribute with conditional expression is required");
         return false;
     }
 
     public ConditionalTemplateResult getConditionalChoiceTemplate(SchemaRecordEditorDialogContext sredc, TemplateElement template)
     {
         if(!template.getElementName().equals(ELEMNAME_CHOOSE))
             return null;
 
         TemplateElement otherwiseTemplate = null;
 
         List chooseElements = template.getChildren();
         for(int i = 0; i < chooseElements.size(); i++)
         {
             TemplateNode chooseElementChildNode = (TemplateNode) chooseElements.get(i);
             if((chooseElementChildNode instanceof TemplateElement))
             {
                 TemplateElement whenElement = (TemplateElement) chooseElementChildNode;
                 if(whenElement.getElementName().equals(ELEMNAME_WHEN))
                 {
                     if(isConditionalTestExpressionTrue(sredc, whenElement))
                         return new ConditionalTemplateResult(true, whenElement);
                 }
                 else if(whenElement.getElementName().equals(ELEMNAME_OTHERWISE))
                     otherwiseTemplate = whenElement;
                 else
                     getLog().error("Only <when> or <otherwise> elements allowed inside a <choose> tag: " + whenElement.getElementName() + " is not a valid child of <choose>");
             }
         }
 
         // if we get to here and we have an otherwise then go ahead and return that (otherwise it will be null)
         return new ConditionalTemplateResult(false, otherwiseTemplate);
     }
 
     public ConditionalTemplateResult getConditionalIfTemplate(SchemaRecordEditorDialogContext sredc, TemplateElement template)
     {
         if(!template.getElementName().equals(ELEMNAME_IF))
             return null;
 
         String testExpr = template.getAttributes().getValue(ATTRNAME_TEST);
         if(testExpr == null || testExpr.length() == 0)
         {
             getLog().error("Test expression is required");
             return new ConditionalTemplateResult(false, template);
         }
         else
             return new ConditionalTemplateResult(isConditionalTestExpressionTrue(sredc, template), template);
     }
 
     public ConditionalTemplateResult getConditionalTemplate(SchemaRecordEditorDialogContext sredc, TemplateElement template)
     {
         ConditionalTemplateResult result = getConditionalChoiceTemplate(sredc, template);
         if(result != null)
             return result;
 
         return getConditionalIfTemplate(sredc, template);
     }
 
     /**
      * ***************************************************************************************************************
      * * Data population (placing column values into fields) methods                                                  **
      * ****************************************************************************************************************
      */
 
     public void populateFieldValuesUsingRequestParameters(SchemaRecordEditorDialogContext sredc, TemplateElement templateElement)
     {
         // make sure auto-mapping expansion is taken care of
         SchemaTableTemplateElement stte = new SchemaTableTemplateElement(sredc, templateElement);
         if(!stte.isTableFound())
             return;
 
         DialogFieldStates states = sredc.getFieldStates();
         HttpServletRequest request = sredc.getHttpRequest();
 
         Attributes templateAttributes = templateElement.getAttributes();
         for(int i = 0; i < templateAttributes.getLength(); i++)
         {
             String columnName = templateAttributes.getQName(i);
             String columnTextValue = templateAttributes.getValue(i);
 
             // these are private "instructions"
             if(columnName.startsWith("_"))
                 continue;
 
             String columnRequestValue = request.getParameter(columnName);
 
             if(columnRequestValue != null)
             {
                 DialogField.State fieldState = states.getState(columnTextValue, null);
                 if(fieldState != null)
                     fieldState.getValue().setTextValue(columnRequestValue);
             }
         }
     }
 
     public void populateFieldValuesUsingRequestParameters(DialogContext dc, TemplateProducer templateProducer)
     {
         if(templateProducer == null)
             return;
 
         final List templateInstances = templateProducer.getInstances();
         if(templateInstances.size() == 0)
             return;
 
         // make sure to get the last template only because inheritance may have create multiples
         Template template = (Template) templateInstances.get(templateInstances.size() - 1);
         List childTableElements = template.getChildren();
 
         for(int i = 0; i < childTableElements.size(); i++)
         {
             TemplateNode childTableNode = (TemplateNode) childTableElements.get(i);
             if(childTableNode instanceof TemplateElement)
             {
                 TemplateElement childTableElement = (TemplateElement) childTableNode;
                 populateFieldValuesUsingRequestParameters((SchemaRecordEditorDialogContext) dc, childTableElement);
             }
         }
     }
 
     public void populateFieldValuesUsingAttributes(SchemaRecordEditorDialogContext sredc, Row row, TemplateElement templateElement)
     {
         DialogFieldStates states = sredc.getFieldStates();
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
                 getLog().error("Can't populateFieldValuesUsingAttributes -- Table '" + row.getTable().getName() + "' does not have a column named '" + columnName + "'.");
                 continue;
             }
 
             // if the column value is a value source spec, we don't map the value to a field
             ValueSource vs = ValueSources.getInstance().getValueSource(ValueSources.createSpecification(columnTextValue), ValueSources.VSNOTFOUNDHANDLER_NULL, true);
             if(vs == null)
             {
                 DialogField.State fieldState = states.getState(columnTextValue, null);
                 if(fieldState != null)
                     fieldState.getValue().copyValueByReference(columnValue);
                 else
                     getLog().error("Can't populateFieldValuesUsingAttributes -- Table Table '" + row.getTable().getName() + "' is mapping a column called '" + columnName + "' to the field '" + columnTextValue + "' but that field was not found.");
             }
         }
     }
 
     public void populateDataUsingTemplateElement(SchemaRecordEditorDialogContext sredc, TemplateElement templateElement) throws NamingException, SQLException
     {
         // first check to see if we're a conditional template (instead of a table name we are a <choose> or <if> block)
         // in the case we are a <choose> or <if> then the children of the choose's <when> or <otherwise> blocks will be
         // the actual things we want to populate
         ConditionalTemplateResult conditionalTemplateResult = getConditionalTemplate(sredc, templateElement);
         if(conditionalTemplateResult != null)
         {
             if(conditionalTemplateResult.isExprResultTrue())
             {
                 List conditionalChildElements = conditionalTemplateResult.getTemplateElement().getChildren();
                 for(int i = 0; i < conditionalChildElements.size(); i++)
                 {
                     TemplateNode childTableNode = (TemplateNode) conditionalChildElements.get(i);
                     if(childTableNode instanceof TemplateElement)
                         populateDataUsingTemplateElement(sredc, (TemplateElement) childTableNode);
                 }
             }
 
             // we're not really a "populate" block since we're conditional so we leave now
             return;
         }
 
         // if we get to here, it means that we are not a <choose> or <if> block
         SchemaTableTemplateElement stte = new SchemaTableTemplateElement(sredc, templateElement);
         if(!stte.isTableFound())
             return;
 
         // now we have the table we're dealing with for this template element
         Table table = stte.getTable();
 
         ValueSource primaryKeyValueSource = stte.getPrimaryKeyValueSource();
         if(primaryKeyValueSource != null)
         {
             final Value primaryKeyValue = primaryKeyValueSource.getValue(sredc);
             if(primaryKeyValue == null)
             {
                 if(!(sredc.editingData() && getDialogFlags().flagIsSet(SchemaRecordEditorDialogFlags.ALLOW_INSERT_IF_EDIT_PK_NOT_FOUND)))
                     sredc.getValidationContext().addValidationError("Unable to locate primary key using value {0}={1} in table {2}.", new Object[]{
                         primaryKeyValueSource, primaryKeyValueSource.getTextValue(sredc), table.getName()
                     });
             }
             else
             {
                 final Object primaryKeyValueObj = primaryKeyValue.getValue();
                 Row activeRow = table.getRowByPrimaryKeys(sredc.getActiveConnectionContext(), new Object[]{
                     primaryKeyValueObj
                 }, null);
                 if(activeRow != null)
                     populateFieldValuesUsingAttributes(sredc, activeRow, templateElement);
                 else
                 {
                     if(!(sredc.editingData() && getDialogFlags().flagIsSet(SchemaRecordEditorDialogFlags.ALLOW_INSERT_IF_EDIT_PK_NOT_FOUND)))
                         sredc.getValidationContext().addValidationError("Unable to locate primary key using value {0}={1} in table {2}.", new Object[]{
                             primaryKeyValueSource, primaryKeyValueSource.getTextValue(sredc), table.getName()
                         });
                 }
             }
         }
         else
             sredc.getValidationContext().addValidationError("Unable to locate primary key for table {0} because value source is NULL.", new Object[]{
                 table.getName()
             });
     }
 
     public void populateValuesUsingTemplateProducer(DialogContext dc, TemplateProducer templateProducer) throws NamingException, SQLException
     {
         if(templateProducer == null)
             return;
 
         final List templateInstances = templateProducer.getInstances();
         if(templateInstances.size() == 0)
             return;
 
         // make sure to get the last template only because inheritance may have create multiples
         Template template = (Template) templateInstances.get(templateInstances.size() - 1);
         List childTableElements = template.getChildren();
 
         for(int i = 0; i < childTableElements.size(); i++)
         {
             TemplateNode childTableNode = (TemplateNode) childTableElements.get(i);
             if(childTableNode instanceof TemplateElement)
             {
                 TemplateElement childTableElement = (TemplateElement) childTableNode;
                 populateDataUsingTemplateElement((SchemaRecordEditorDialogContext) dc, childTableElement);
             }
         }
     }
 
     public void populateValues(DialogContext dc, int formatType)
     {
         SchemaRecordEditorDialogContext sredc = (SchemaRecordEditorDialogContext) dc;
 
         if(sredc.getDialogState().isInitialEntry())
         {
             if(sredc.addingData() && !getDialogFlags().flagIsSet(SchemaRecordEditorDialogFlags.DONT_POPULATE_USING_REQUEST_PARAMS))
                 populateFieldValuesUsingRequestParameters(dc, insertDataTemplateProducer);
             else
             {
                 ConnectionContext cc = null;
                 try
                 {
                     cc = sredc.getConnection(dataSrc != null ? dataSrc.getTextValue(dc) : null, false);
                     sredc.setActiveConnectionContext(cc);
                     switch((int) dc.getDialogState().getPerspectives().getFlags())
                     {
                         case DialogPerspectives.EDIT:
                         case DialogPerspectives.PRINT:
                         case DialogPerspectives.CONFIRM:
                             populateValuesUsingTemplateProducer(dc, editDataTemplateProducer);
                             break;
 
                         case DialogPerspectives.DELETE:
                             populateValuesUsingTemplateProducer(dc, deleteDataTemplateProducer);
                             break;
                     }
                     sredc.setActiveConnectionContext(null);
                 }
                 catch(SQLException e)
                 {
                     getLog().error("Error in populateValues for perspective " + dc.getDialogState().getPerspectives(), e);
                 }
                 catch(NamingException e)
                 {
                     getLog().error("Error in populateValues for perspective " + dc.getDialogState().getPerspectives(), e);
                 }
                 finally
                 {
                     try
                     {
                         if(cc != null) cc.close();
                     }
                     catch(SQLException e)
                     {
                         getLog().error("Unable to close connection in populateValues()", e);
                     }
                 }
             }
         }
 
         super.populateValues(sredc, formatType);
     }
 
     /**
      * ***************************************************************************************************************
      * * Data insert methods                                                                                        **
      * ***************************************************************************************************************
      */
 
     public void addDataUsingTemplateElement(SchemaRecordEditorDialogContext sredc, TemplateElement templateElement, Row parentRow) throws SQLException
     {
         // first check to see if we're a conditional template (instead of a table name we are a <choose> or <if> block)
         // in the case we are a <choose> or <if> then the children of the choose's <when> or <otherwise> blocks will be
         // the actual things we want to add
         ConditionalTemplateResult conditionalTemplateResult = getConditionalTemplate(sredc, templateElement);
         if(conditionalTemplateResult != null)
         {
             if(conditionalTemplateResult.isExprResultTrue())
             {
                 List conditionalChildElements = conditionalTemplateResult.getTemplateElement().getChildren();
                 for(int i = 0; i < conditionalChildElements.size(); i++)
                 {
                     TemplateNode childTableNode = (TemplateNode) conditionalChildElements.get(i);
                     if(childTableNode instanceof TemplateElement)
                         addDataUsingTemplateElement(sredc, (TemplateElement) childTableNode, parentRow);
                 }
             }
 
             // we're not really an "add" block since we're conditional so we leave now
             return;
         }
 
         // if we get to here, it means that we are not a <choose> or <if> block
         SchemaTableTemplateElement stte = new SchemaTableTemplateElement(sredc, templateElement);
         if(!stte.isTableFound())
             return;
 
         // now we have the table we're dealing with for this template element
         Table table = stte.getTable();
         int insertCount = 1;
 
         // each of the attributes provided in the template are supposed to column-name="column-value" where
         // column-value may be a static string which refers to a dialog field name or a value source specification
         // which refers to some dynamic value
         Attributes templateAttributes = templateElement.getAttributes();
         // if we have an attribute called _loop then it's a column name and the multiple values returned from
         // this column should be used to insert multiple rows
         String loopColumnName = templateAttributes.getValue(ATTRNAME_LOOP);
         String[] loopColumnValues = null;
         if(loopColumnName != null)
         {
             String loopColumnTextValue = templateAttributes.getValue(loopColumnName);
             ValueSource vs = ValueSources.getInstance().getValueSource(ValueSources.createSpecification(loopColumnTextValue), ValueSources.VSNOTFOUNDHANDLER_NULL, true);
             if(vs == null)
             {
                 DialogFieldStates states = sredc.getFieldStates();
                 DialogField.State state = states.getState(loopColumnTextValue);
                 if(state != null)
                 {
                     loopColumnValues = state.getValue().getTextValues();
                     insertCount = loopColumnValues.length;
                 }
                 else
                 {
                     getLog().error("Unable to find fieldName '" + loopColumnTextValue + "' to populate column value with.");
                 }
             }
             else
             {
                 loopColumnValues = vs.getTextValues(sredc);
                 insertCount = loopColumnValues.length;
             }
         }
 
         for(int k = 0; k < insertCount; k++)
         {
             Row activeRow;
             // find the connector from the child table to the parent table if one is available
             Columns parentKeyCols = table.getForeignKeyColumns(ForeignKey.FKEYTYPE_PARENT);
             if(parentKeyCols.size() == 1 && parentRow != null)
             {
                 Column connector = parentKeyCols.getSole();
                 activeRow = table.createRow((ParentForeignKey) connector.getForeignKey(), parentRow);
             }
             else
                 activeRow = table.createRow();
 
             ColumnValues columnValues = activeRow.getColumnValues();
             boolean doInsert = true;
 
             for(int i = 0; i < templateAttributes.getLength(); i++)
             {
                 String columnName = templateAttributes.getQName(i);
                 String columnTextValue = templateAttributes.getValue(i);
 
                 // if we have an attribute called _condition then it's a JSTL-style expression that should return true if
                 // we want to do this insert or false if we don't
                 if(columnName.equalsIgnoreCase(ATTRNAME_CONDITION))
                 {
                     doInsert = sredc.isConditionalExpressionTrue(columnTextValue, null);
                     if(!doInsert)
                         break; // don't bother setting values since we're not inserting
                 }
 
                 if(columnName.equalsIgnoreCase(ATTRNAME_LOOP))
                     continue;
 
                 // these are private "instructions"
                 if(columnName.startsWith("_"))
                     continue;
 
                 ColumnValue columnValue = columnValues.getByNameOrXmlNodeName(columnName);
                 if(columnValue == null)
                 {
                     getLog().error("Table '" + table.getName() + "' does not have a column named '" + columnName + "'.");
                     continue;
                 }
 
                 if(loopColumnName != null && loopColumnName.equals(columnName) && loopColumnValues != null)
                     columnValue.setTextValue(loopColumnValues[k]);
                 else
                     assignColumnValue(sredc, columnValue, columnTextValue);
             }
 
             if(doInsert)
             {
                 table.insert(sredc.getActiveConnectionContext(), activeRow);
                 sredc.getRowsAdded().add(activeRow);
 
                 // now recursively add children if any are available
                 List childTableElements = templateElement.getChildren();
                 for(int i = 0; i < childTableElements.size(); i++)
                 {
                     TemplateNode childTableNode = (TemplateNode) childTableElements.get(i);
                     if(childTableNode instanceof TemplateElement)
                     {
                         TemplateElement childTableElement = (TemplateElement) childTableNode;
                         addDataUsingTemplateElement(sredc, childTableElement, activeRow);
                     }
                 }
             }
         }
     }
 
     /**
      * Assigns a value to the column value object using the text value.
      *
      * @param sredc           schema record editor dialog context
      * @param columnValue     column value object
      * @param columnTextValue the text to be used as the column value
      */
     private void assignColumnValue(SchemaRecordEditorDialogContext sredc, ColumnValue columnValue, String columnTextValue)
     {
         // if the column value is a value source spec, we get the value from the VS otherwise it's a field name in the active dialog
         ValueSource vs = ValueSources.getInstance().getValueSource(ValueSources.createSpecification(columnTextValue), ValueSources.VSNOTFOUNDHANDLER_NULL, true);
         if(vs == null)
             DialogContextUtils.getInstance().populateColumnValueWithFieldValue(sredc, columnValue, columnTextValue);
         else if(vs instanceof SqlExpressionValueSource)
             setColumnSqlExpression(columnValue, vs, sredc);
         else
         {
             Value fieldValue = vs.getValue(sredc);
             if(fieldValue.getValueHolderClass() == columnValue.getValueHolderClass())
                 columnValue.copyValueByReference(fieldValue);
             else
                 columnValue.setTextValue(vs.getTextValue(sredc));
         }
     }
 
     public void addDataUsingTemplate(SchemaRecordEditorDialogContext sredc) throws SQLException
     {
         if(insertDataTemplateProducer == null)
             return;
 
         final List templateInstances = insertDataTemplateProducer.getInstances();
         if(templateInstances.size() == 0)
             return;
 
         // make sure to get the last template only because inheritance may have create multiples
         Template insertDataTemplate = (Template) templateInstances.get(templateInstances.size() - 1);
         List childTableElements = insertDataTemplate.getChildren();
         for(int i = 0; i < childTableElements.size(); i++)
         {
             TemplateNode childTableNode = (TemplateNode) childTableElements.get(i);
             if(childTableNode instanceof TemplateElement)
             {
                 TemplateElement childElement = (TemplateElement) childTableNode;
                 addDataUsingTemplateElement(sredc, childElement, null);
             }
         }
     }
 
     /**
      * ***************************************************************************************************************
      * * Data update/edit methods                                                                                     **
      * ****************************************************************************************************************
      */
 
     public void editDataUsingTemplateElement(SchemaRecordEditorDialogContext sredc, TemplateElement templateElement) throws NamingException, SQLException, DialogExecuteException
     {
         SchemaTableTemplateElement stte = new SchemaTableTemplateElement(sredc, templateElement);
         if(!stte.isTableFound())
             return;
 
         // now we have the table we're dealing with for this template element
         Table table = stte.getTable();
         ValueSource primaryKeyValueSource = stte.getPrimaryKeyValueSource();
 
         if(primaryKeyValueSource == null)
             throw new DialogExecuteException(java.text.MessageFormat.format("Unable to locate primary key for table {0} because value source is NULL.",
                                                                             new Object[]{ table.getName() }));
 
         final Value primaryKeyValue = primaryKeyValueSource.getValue(sredc);
         if(primaryKeyValue == null)
         {
             if(getDialogFlags().flagIsSet(SchemaRecordEditorDialogFlags.ALLOW_INSERT_IF_EDIT_PK_NOT_FOUND))
                 addDataUsingTemplateElement(sredc, templateElement, null);
             else
                 throw new DialogExecuteException(java.text.MessageFormat.format("Unable to locate primary key using value {0}={1} in table {2}.", new Object[]{
                     primaryKeyValueSource, primaryKeyValueSource.getTextValue(sredc), table.getName()
                 }));
             return;
         }
 
         final Object primaryKeyValueObj = primaryKeyValue.getValue();
         Row activeRow = table.getRowByPrimaryKeys(sredc.getActiveConnectionContext(), new Object[]{primaryKeyValueObj}, null);
         if(activeRow == null)
         {
             if(getDialogFlags().flagIsSet(SchemaRecordEditorDialogFlags.ALLOW_INSERT_IF_EDIT_PK_NOT_FOUND))
                 addDataUsingTemplateElement(sredc, templateElement, null);
             else
                 throw new DialogExecuteException(java.text.MessageFormat.format("Unable to locate primary key using value {0}={1} in table {2}.", new Object[]{
                     primaryKeyValueSource, primaryKeyValueSource.getTextValue(sredc), table.getName()
                 }));
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
                 doUpdate = sredc.isConditionalExpressionTrue(columnTextValue, null);
                 if(!doUpdate)
                     break; // don't bother setting values since we're not inserting
             }
 
             // these are private "instructions"
             if(columnName.startsWith("_"))
                 continue;
 
             ColumnValue columnValue = columnValues.getByNameOrXmlNodeName(columnName);
             if(columnValue == null)
             {
                 getLog().error("Table '" + table.getName() + "' does not have a column named '" + columnName + "'.");
                 continue;
             }
 
             assignColumnValue(sredc, columnValue, columnTextValue);
         }
 
         if(doUpdate)
         {
             table.update(sredc.getActiveConnectionContext(), activeRow);
             sredc.getRowsUpdated().add(activeRow);
 
             // now recursively add children if any are available
             List childTableElements = templateElement.getChildren();
             for(int i = 0; i < childTableElements.size(); i++)
             {
                 TemplateNode childTableNode = (TemplateNode) childTableElements.get(i);
                 if(childTableNode instanceof TemplateElement)
                 {
                     TemplateElement childTableElement = (TemplateElement) childTableNode;
                     editDataUsingTemplateElement(sredc, childTableElement);
                 }
             }
         }
     }
 
     public void editDataUsingTemplate(SchemaRecordEditorDialogContext sredc) throws NamingException, SQLException, DialogExecuteException
     {
         if(editDataTemplateProducer == null)
             return;
 
         final List templateInstances = editDataTemplateProducer.getInstances();
         if(templateInstances.size() == 0)
             return;
 
         // make sure to get the last template only because inheritance may have create multiples
         Template editDataTemplate = (Template) templateInstances.get(templateInstances.size() - 1);
         List childTableElements = editDataTemplate.getChildren();
         for(int i = 0; i < childTableElements.size(); i++)
         {
             TemplateNode childTableNode = (TemplateNode) childTableElements.get(i);
             if(childTableNode instanceof TemplateElement)
             {
                 TemplateElement childTableElement = (TemplateElement) childTableNode;
                 editDataUsingTemplateElement(sredc, childTableElement);
             }
         }
     }
 
     /**
      * ***************************************************************************************************************
      * * Data update/edit methods                                                                                     **
      * ****************************************************************************************************************
      */
 
     public void deleteDataUsingTemplateElement(SchemaRecordEditorDialogContext sredc, TemplateElement templateElement) throws NamingException, SQLException, DialogExecuteException
     {
         SchemaTableTemplateElement stte = new SchemaTableTemplateElement(sredc, templateElement);
         if(!stte.isTableFound())
             return;
 
         // now we have the table we're dealing with for this template element
         Table table = stte.getTable();
 
         ValueSource primaryKeyValueSource = stte.getPrimaryKeyValueSource();
         if(primaryKeyValueSource == null)
             throw new DialogExecuteException(java.text.MessageFormat.format("Unable to locate primary key for table {0}.", new Object[]{
                 table.getName()
             }));
 
         final Object primaryKeyValue = primaryKeyValueSource.getValue(sredc).getValue();
         Row activeRow = table.getRowByPrimaryKeys(sredc.getActiveConnectionContext(), new Object[]{primaryKeyValue}, null);
         if(activeRow == null)
             throw new DialogExecuteException(java.text.MessageFormat.format("Unable to locate primary key using value {0}={1} in table {2}.", new Object[]{
                 primaryKeyValueSource, primaryKeyValueSource.getTextValue(sredc), table.getName()
             }));
 
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
                 doDelete = sredc.isConditionalExpressionTrue(columnTextValue, null);
                 if(!doDelete)
                     break; // don't bother setting values since we're not inserting
             }
 
             // these are private "instructions"
             if(columnName.startsWith("_"))
                 continue;
 
             ColumnValue columnValue = columnValues.getByNameOrXmlNodeName(columnName);
             if(columnValue == null)
             {
                 getLog().error("Table '" + table.getName() + "' does not have a column named '" + columnName + "'.");
                 continue;
             }
 
             assignColumnValue(sredc, columnValue, columnTextValue);
         }
 
         if(doDelete)
         {
             table.delete(sredc.getActiveConnectionContext(), activeRow);
             sredc.getRowsDeleted().add(activeRow);
 
             // now recursively add children if any are available
             List childTableElements = templateElement.getChildren();
             for(int i = 0; i < childTableElements.size(); i++)
             {
                 TemplateNode childTableNode = (TemplateNode) childTableElements.get(i);
                 if(childTableNode instanceof TemplateElement)
                 {
                     TemplateElement childTableElement = (TemplateElement) childTableNode;
                     deleteDataUsingTemplateElement(sredc, childTableElement);
                 }
             }
         }
     }
 
     public void deleteDataUsingTemplate(SchemaRecordEditorDialogContext sredc) throws NamingException, SQLException, DialogExecuteException
     {
         if(deleteDataTemplateProducer == null)
             return;
 
         final List templateInstances = deleteDataTemplateProducer.getInstances();
         if(templateInstances.size() == 0)
             return;
 
         // make sure to get the last template only because inheritance may have create multiples
         Template deleteDataTemplate = (Template) templateInstances.get(templateInstances.size() - 1);
         List childTableElements = deleteDataTemplate.getChildren();
         for(int i = 0; i < childTableElements.size(); i++)
         {
             TemplateNode childTableNode = (TemplateNode) childTableElements.get(i);
             if(childTableNode instanceof TemplateElement)
             {
                 TemplateElement childTableElement = (TemplateElement) childTableNode;
                 deleteDataUsingTemplateElement(sredc, childTableElement);
             }
         }
     }
 
     public boolean duplicateRecordOnUniqueColumnExists(SchemaRecordEditorDialogContext sredc) throws NamingException, SQLException
     {
         boolean duplicateFound = false;
         List templateInstances;
 
         if(sredc.editingData())
         {
             if(editDataTemplateProducer == null) return false;
             templateInstances = editDataTemplateProducer.getInstances();
         }
         else
         {
             if(insertDataTemplateProducer == null) return false;
             templateInstances = insertDataTemplateProducer.getInstances();
         }
 
         if(templateInstances.size() == 0)
             return false;
 
         // make sure to get the last template only because inheritance may have create multiples
         Template editDataTemplate = (Template) templateInstances.get(templateInstances.size() - 1);
         List childTableElements = editDataTemplate.getChildren();
         for(int i = 0; i < childTableElements.size(); i++)
         {
             TemplateNode childTableNode = (TemplateNode) childTableElements.get(i);
             if(childTableNode instanceof TemplateElement)
             {
                 TemplateElement childTableElement = (TemplateElement) childTableNode;
                 SchemaTableTemplateElement stte = new SchemaTableTemplateElement(sredc, childTableElement);
                 if(!stte.isTableFound())
                     return false;
 
                 Table table = stte.getTable();
                 Attributes templateAttributes = childTableElement.getAttributes();
 
                 Indexes indexes = table.getIndexes();
                 for(int j = 0; j < indexes.size(); j++)
                 {   //Iterate through all of the indexes.  This includes all of the single columns indexes that are created when
                     //a columns is marked as unique.
 
                     Index index = indexes.get(j);
                     if(!index.isUnique())
                         continue;
 
 
                     List fields = new ArrayList();
 
                     IndexColumns indexColumns = index.getColumns();
                     List params = new ArrayList();
                     for(int k = 0; k < indexColumns.size(); k++)
                     {   //Iterate through all of the columns in the index
                         Column column = indexColumns.get(k);
                         String columnTextValue = templateAttributes.getValue(column.getName());
                         String columnValue;
                         // if the column value is a value source spec, we get the value from the VS otherwise it's a field name in the active dialog
                         ValueSource vs = ValueSources.getInstance().getValueSource(ValueSources.createSpecification(columnTextValue), ValueSources.VSNOTFOUNDHANDLER_NULL, true);
                         if(vs == null)
                         {
                             DialogField.State fieldState = sredc.getFieldStates().getState(columnTextValue);
                             columnValue = fieldState.getValue().getTextValue();
                             fields.add(fieldState);
                         }
                         else
                             columnValue = vs.getTextValue(sredc);
 
                         params.add(columnValue);
                     }
 
                     QueryDefnSelect indexAccessor;
                     indexAccessor = table.getAccessorByIndexEquality(index);
                     QueryResultSet results = indexAccessor.execute(sredc.getActiveConnectionContext(), params.toArray(), false);
                     ResultSet resultSet = results.getResultSet();
 
                     if(resultSet.next())
                     {
                         if(sredc.editingData())
                         {
                             ValueSource primaryKeyValueSource = stte.getPrimaryKeyValueSource();
 
                             if(primaryKeyValueSource != null)
                             {
                                 Value primaryKeyValue = primaryKeyValueSource.getValue(sredc);
                                 final Column primaryKeyTableColumn = table.getPrimaryKeyColumns().getSole();
                                 final ColumnValue primaryKeyValueFromPotentiallyDuplicateRow = primaryKeyTableColumn.constructValueInstance();
                                 primaryKeyValueFromPotentiallyDuplicateRow.setValueFromSqlResultSet(resultSet, 0, primaryKeyTableColumn.getIndexInRow()+1);
 
                                 if(primaryKeyValue != null && primaryKeyValue.equals(primaryKeyValueFromPotentiallyDuplicateRow));
                                     continue; // This means that the row found was the same as the one we were trying to update so it should be ok
                             }
                         }
 
                         getLog().debug("A Unique constraint violation found for Table: '" + table.getName() + "' Index: '" + index.getName() + "'");
 
                         if(fields.size() == 1)
                         {   //If there is only one field that is violating the unqie constraint then the scenario is simple
                             DialogField.State fieldState = (DialogField.State) fields.get(0);
                             if(fieldState.getStateFlags().flagIsSet(DialogFieldFlags.READ_ONLY | DialogFieldFlags.INPUT_HIDDEN | DialogFieldFlags.UNAVAILABLE))
                                 sredc.getValidationContext().addError("A problem was encountered when validating that the data was able to be persisted. Please re-open the record you were trying to edit and try it again.");
                             else
                                sredc.getValidationContext().addError("The value for field " + fieldState.getField().getCaption().getTextValue(sredc) + " must be unique.  Another record was found with the same value. Please change the value and try again.");
                         }
                         else if(fields.size() > 1)
                         {   //If there are more than one field, then need to construct a list of the field name to show at the top of the dialog
                             String fieldNameList = "";
                             for(int k = 0; k < fields.size(); k++)
                             {   //Looping through each field and invalidating it and adding to the list of fields if the field is visible and available to the user
                                 DialogField.State fieldState = (DialogField.State) fields.get(k);
                                 if(fieldState.getStateFlags().flagIsSet(DialogFieldFlags.READ_ONLY | DialogFieldFlags.INPUT_HIDDEN | DialogFieldFlags.UNAVAILABLE))
                                 {
                                    fieldState.getField().invalidate(sredc, "The value for field: " + fieldState.getField().getCaption().getTextValue(sredc) + " must be unique.  A record was found with the same value.  Please change the value and try again.");
                                     fieldNameList = fieldNameList + (fieldNameList.length() <= 0 ? "" : ", ") + fieldState.getField().getName();
                                 }
                             }
                             sredc.getValidationContext().addError("The combination of the following fields: " + fieldNameList + " must be unique. A record was found with the same set of values. Please change the values and try again.");
 
                         }
                         else
                         {   //In this case, all of the values for the unqiue constrain are not comming from fields, in which case the user can't do anything to resolve the problem.
                             sredc.getValidationContext().addError("A problem was encountered when validating that the data was able to be persisted.  Please re-open the record you were trying to edit and try it again.");
                         }
 
                         duplicateFound = true;
                     }
                     resultSet.close();
                     results.close(false);
                 }
             }
         }
         return duplicateFound;
     }
 
 
     public boolean isValid(DialogContext dc)
     {
         boolean superIsValid = super.isValid(dc);
         if(!superIsValid)
             return false; //No need to continue if there was something invalid with the dialog.
 
         boolean isValid = true;
 
         SchemaRecordEditorDialogContext sredc = ((SchemaRecordEditorDialogContext) dc);
         ConnectionContext cc = null;
         try
         {
             cc = dc.getConnection(dataSrc != null ? dataSrc.getTextValue(dc) : null, true);
             sredc.setActiveConnectionContext(cc);
             switch((int) dc.getDialogState().getPerspectives().getFlags())
             {
                 case DialogPerspectives.ADD:
                 case DialogPerspectives.EDIT:
                     isValid = !duplicateRecordOnUniqueColumnExists(sredc);
                     break;
             }
             sredc.setActiveConnectionContext(null);
         }
         catch(Exception e)
         {
             getLog().error("Error while validating duplicates on unique columns.", e);
         }
         finally
         {
             if(cc != null)
             {
                 try
                 {
                     cc.close();
                 }
                 catch(SQLException e)
                 {
                     getLog().error("Error while validating duplicates on unique columns.", e);                    
                 }
             }
         }
 
         return isValid;
     }
 
     /**
      * ***************************************************************************************************************
      * * Default execute method                                                                                       **
      * ****************************************************************************************************************
      */
     public void execute(Writer writer, DialogContext dc) throws DialogExecuteException, IOException
     {
         if(dc.executeStageHandled())
             return;
         SchemaRecordEditorDialogContext sredc = ((SchemaRecordEditorDialogContext) dc);
 
         ConnectionContext cc;
         DialogExecuteHandlers handlers = getExecuteHandlers();
         try
         {
             cc = dc.getConnection(dataSrc != null ? dataSrc.getTextValue(dc) : null, true);
             sredc.setActiveConnectionContext(cc);
         }
         catch(Exception e)
         {
             handlePostExecuteException(writer, dc, dc.getDialogState().getPerspectives() + ": unable to establish connection", e);
             return;
         }
 
         try
         {
             switch((int) dc.getDialogState().getPerspectives().getFlags())
             {
                 case DialogPerspectives.ADD:
                     addDataUsingTemplate(sredc);
                     break;
 
                 case DialogPerspectives.EDIT:
                     editDataUsingTemplate(sredc);
                     break;
 
                 case DialogPerspectives.DELETE:
                     deleteDataUsingTemplate(sredc);
                     break;
             }
 
             if(handlers.size() > 0)
                 handlers.handleDialogExecute(writer, dc);
 
             sredc.setActiveConnectionContext(null);
             cc.commitAndClose();
             dc.setExecuteStageHandled(true);
             handlePostExecute(writer, dc);
         }
         catch(Exception e)
         {
             try
             {
                 cc.rollbackAndClose();
             }
             catch(SQLException e1)
             {
                 getLog().error("Error while rolling back DML", e1);
             }
             handlePostExecuteException(writer, dc, dc.getDialogState().getPerspectives().getFlagsText(), e);
         }
     }
 }
