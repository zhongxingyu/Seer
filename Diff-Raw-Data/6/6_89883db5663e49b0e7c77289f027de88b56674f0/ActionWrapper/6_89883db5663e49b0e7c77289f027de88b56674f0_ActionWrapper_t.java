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
 * $Id: ActionWrapper.java,v 1.4 2004-08-09 18:59:00 aye.thu Exp $
  */
 
 package com.netspective.sparx.form.action;
 
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.sql.Connection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import org.apache.commons.lang.exception.NestableRuntimeException;
 
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.axiom.connection.DriverManagerConnectionProvider;
 import com.netspective.commons.value.Value;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.ValueSources;
 import com.netspective.commons.xdm.XdmParseContext;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.commons.xdm.XmlDataModelSchema.ConstructionFinalizeListener;
 import com.netspective.commons.xdm.XmlDataModelSchema.CustomElementAttributeSetter;
 import com.netspective.commons.xdm.exception.DataModelException;
 import com.netspective.sparx.form.field.DialogFieldStates;
 import com.netspective.sparx.form.field.DialogFields;
 import com.netspective.sparx.util.HttpUtils;
 
 public class ActionWrapper implements CustomElementAttributeSetter, ConstructionFinalizeListener
 {
     private final XmlDataModelSchema actionSchema;
     private final ActionDialog actionDialog;
     private final Method setActionDialogContextMethod;
     private final Method setServletEnvironmentMethod;
     private final Method getDataSourceNameMethod;
     private final Method getDataSourceInfoMethod;
     private final Method setConnectionMethod;
     private final Method setConnectionContextMethod;
     private ActionConstructor actionConstructor;
     private ActionExecutor actionExecutor;
     private ActionValidator actionValidator;
     private Map fieldMutators = new HashMap();
     private String assignRequestParams;
     private String executeMethodName = "execute";
     private String getNextActionMethodName = "getNextAction";
     private String isValidMethodName = "isValid";
 
     public ActionWrapper(final ActionDialog actionDialog, final Class actionClass)
     {
         this.actionDialog = actionDialog;
         this.actionSchema = actionClass == null ? null : XmlDataModelSchema.getSchema(actionClass);
         this.setServletEnvironmentMethod = getMethod("setServletEnvironment", new Class[] { Servlet.class, ServletRequest.class, ServletResponse.class });
         this.getDataSourceNameMethod = getMethod("getDataSourceName", (Class[]) null);
         this.getDataSourceInfoMethod = getMethod("getDataSourceInfo", (Class[]) null);
         this.setConnectionMethod = getMethod("setConnection", Connection.class);
         this.setConnectionContextMethod = getMethod("setConnectionContext", ConnectionContext.class);
         this.setActionDialogContextMethod = getMethod("setActionDialogContext", ActionDialogContext.class);
     }
 
     public void initializeBean() throws NoSuchMethodException
     {
         locateConstructor();
         locateMutators();
         locateValidator();
         locateExecutor();
     }
 
     public void finalizeConstruction(XdmParseContext pc, Object element, String elementName) throws DataModelException
     {
         try
         {
             initializeBean();
         }
         catch (NoSuchMethodException e)
         {
             actionDialog.getLog().error(e);
             throw new DataModelException(pc, e);
         }
     }
 
     /**
      * If the <action> tag has any attributes other than 'class' then they are field-name to setter/mutator name
      * maps like <action class='x.y.z' X="XXX" Y="ZZZ"/>. This would map the field name "X" to a setXXX() method
      * instead of setX(). Similarly, the field name "Y" would call setZZZ() instead of setZ(). Any fields not mapped
      * like this would be mapped normally (same as field name).
      */
     public void setCustomDataModelElementAttribute(XdmParseContext pc, XmlDataModelSchema schema, Object parent, String attrName, String attrValue) throws DataModelException, InvocationTargetException, IllegalAccessException, DataModelException
     {
         DialogFields dialogFields = actionDialog.getFields();
 
         if(dialogFields.getByQualifiedName(attrName) != null)
         {
             FieldMutator fieldMutator = locateMutator(attrValue);
             if(fieldMutator != null) fieldMutators.put(attrName, fieldMutator);
         }
         else
             schema.setAttribute(pc, parent, attrName, attrValue, true);
 
     }
 
     public String getAssignRequestParams()
     {
         return assignRequestParams;
     }
 
     public void setAssignRequestParams(String assignRequestParams)
     {
         this.assignRequestParams = assignRequestParams;
     }
 
     public String getExecuteMethodName()
     {
         return executeMethodName;
     }
 
     public void setExecuteMethodName(String executeMethodName)
     {
         this.executeMethodName = executeMethodName;
     }
 
     public String getGetNextActionMethodName()
     {
         return getNextActionMethodName;
     }
 
     public void setGetNextActionMethodName(String getNextActionMethodName)
     {
         this.getNextActionMethodName = getNextActionMethodName;
     }
 
     public ActionValidator getActionValidator()
     {
         return actionValidator;
     }
 
     private Method getMethod(String name, Class paramType)
     {
         try
         {
             return actionSchema.getBean().getMethod(name, new Class[] { paramType });
         }
         catch (NoSuchMethodException e)
         {
             return null;
         }
     }
 
     private Method getMethod(String name, Class[] paramTypes)
     {
         try
         {
             return actionSchema.getBean().getMethod(name, paramTypes );
         }
         catch (NoSuchMethodException e)
         {
             return null;
         }
     }
 
     private void locateConstructor()
     {
         final Class actionClass = actionSchema.getBean();
 
         actionConstructor = new ActionConstructor()
         {
             public Object constructAction() throws Exception
             {
                 return actionClass.newInstance();
             }
         };
     }
 
     public FieldMutator locateMutator(String attributeName)
     {
         try
         {
             final Method method = (Method) actionSchema.getAttributeSetterMethods().get(attributeName);
             if(method != null)
             {
                 Class[] args = method.getParameterTypes();
                 if(args.length == 1)
                 {
                     Class arg = args[0];
                     if(java.lang.String.class.equals(arg) && arg.isArray())
                     {
                         return new FieldMutator()
                         {
                             public void set(Object instance, Value value) throws Exception
                             {
                                 method.invoke(instance, new Object[] { value.getTextValues() });
                             }
                         };
                     }
                     else if(Value.class.equals(arg))
                     {
                         return new FieldMutator()
                         {
                             public void set(Object instance, Value value) throws Exception
                             {
                                 method.invoke(instance, new Object[] { value });
                             }
                         };
                     }
                     else if(Date.class.equals(arg))
                     {
                         return new FieldMutator()
                         {
                             public void set(Object instance, Value value) throws Exception
                             {
                                 // assuming that if a date is expected then the Object of the value.getValue() will be a date
                                 method.invoke(instance, new Object[] { (Date) value.getValue() });
                             }
                         };
                     }
                     else
                     {
                         final XmlDataModelSchema.AttributeSetter as = (XmlDataModelSchema.AttributeSetter) actionSchema.getAttributeSetters().get(attributeName);
                         if(as != null)
                         {
                             return new FieldMutator()
                             {
                                 public void set(Object instance, Value value) throws Exception
                                 {
                                    if (value.getTextValue() != null)
                                        as.set(null, instance, value.getTextValue());
                                 }
                             };
                         }
                     }
                 }
             }
             else
                 actionDialog.getLog().warn("No setter method found for field " + attributeName + " in " + actionSchema.getBean());
         }
         catch (Exception e)
         {
             actionDialog.getLog().error("Unable to create mutator '"+ attributeName +"' for "+ actionSchema.getBean() +": " + e.getMessage(), e);
             throw new NestableRuntimeException(e);
         }
 
         // if we get to here, unable to set the attribute
         actionDialog.getLog().warn("Unable to locate mutator '"+ attributeName +"' for "+ actionSchema.getBean());
 
         return null;
     }
 
     public void locateMutators()
     {
         DialogFields dialogFields = actionDialog.getFields();
 
         for(Iterator i = dialogFields.getFieldsMapByQualifiedName().keySet().iterator(); i.hasNext(); )
         {
             String fieldName = (String) i.next();
             if(! fieldMutators.containsKey(fieldName))
             {
                 FieldMutator fieldMutator = locateMutator(fieldName);
                 if(fieldMutator != null) fieldMutators.put(fieldName, fieldMutator);
             }
         }
     }
 
     public void locateValidator() throws NoSuchMethodException
     {
         final Method isValidMethod = getMethod(isValidMethodName, List.class) == null ? getMethod(isValidMethodName, (Class[]) null) : getMethod(isValidMethodName, List.class);
         if(isValidMethod != null)
         {
             if(isValidMethod.getReturnType() == boolean.class)
             {
                 actionValidator = new ActionValidator()
                 {
                     public boolean isValid(Object instance, List messages) throws Exception
                     {
                         Boolean result = (Boolean) isValidMethod.invoke(instance, new Object[] { messages });
                         return result.booleanValue();
                     }
                 };
             }
         }
         else
             actionValidator = null;
    }
 
     public void locateExecutor() throws NoSuchMethodException
     {
         final Method execMethod = getMethod(executeMethodName, Writer.class) == null ? getMethod(executeMethodName, (Class[]) null) : getMethod(executeMethodName, Writer.class);
         if(execMethod == null)
             throw new NoSuchMethodException("Unable to find " + executeMethodName + "() or " + executeMethodName + "(Writer) method in " + actionSchema.getBean());
 
         if(execMethod.getParameterTypes().length > 0)
         {
             if(execMethod.getReturnType() == String.class)
             {
                 actionExecutor = new ActionExecutor()
                 {
                     public String execute(Writer writer, Object instance) throws Exception
                     {
                         return (String) execMethod.invoke(instance, new Object[] { writer });
                     }
                 };
             }
             else
             {
                 actionExecutor = new ActionExecutor()
                 {
                     public String execute(Writer writer, Object instance) throws Exception
                     {
                         execMethod.invoke(instance, new Object[] { writer });
                         return null;
                     }
                 };
             }
         }
         else
         {
             if(execMethod.getReturnType() == String.class)
             {
                 actionExecutor = new ActionExecutor()
                 {
                     public String execute(Writer writer, Object instance) throws Exception
                     {
                         return (String) execMethod.invoke(instance, null);
                     }
                 };
             }
             else
             {
                 actionExecutor = new ActionExecutor()
                 {
                     public String execute(Writer writer, Object instance) throws Exception
                     {
                         execMethod.invoke(instance, null);
                         return null;
                     }
                 };
             }
         }
     }
 
     protected void initInstance(Object instance, ActionDialogContext dc) throws Exception
     {
         if(setActionDialogContextMethod != null) setActionDialogContextMethod.invoke(instance, new Object[] { dc });
         if(setServletEnvironmentMethod != null) setServletEnvironmentMethod.invoke(instance, new Object[] { dc.getServlet(), dc.getRequest(), dc.getResponse() });
     }
 
     protected void assignInstanceValues(Object instance, ActionDialogContext dc) throws Exception
     {
         DialogFieldStates dfs = dc.getFieldStates();
 
         for(Iterator i = fieldMutators.entrySet().iterator(); i.hasNext(); )
         {
             Map.Entry entry = (Map.Entry) i.next();
             String fieldName = (String) entry.getKey();
             FieldMutator actionMutator = (FieldMutator) entry.getValue();
 
             actionMutator.set(instance, dfs.getState(fieldName).getValue());
         }
 
         if(assignRequestParams != null)
             HttpUtils.assignParamsToInstance(dc.getHttpRequest(), instance, assignRequestParams);
     }
 
     protected void assignDatabaseConnection(Object instance, ActionDialogContext dc) throws Exception
     {
         if(setConnectionContextMethod != null || setConnectionMethod != null)
         {
             String dataSourceId = dc.getDefaultDataSource();
             if(getDataSourceNameMethod != null)
                 dataSourceId = (String) getDataSourceNameMethod.invoke(instance, null);
 
             if(dataSourceId != null)
             {
                 ValueSource dataSourceIdValueSource = ValueSources.getInstance().getValueSourceOrStatic(dataSourceId);
                 dataSourceId = dataSourceIdValueSource.getTextValue(dc);
 
                 if(dataSourceId.equals("DEFAULT"))
                     dataSourceId = null;
 
                 ConnectionContext cc = dc.openActionConnection(dataSourceId);
                 if(setConnectionContextMethod != null) setConnectionContextMethod.invoke(instance, new Object[] { cc });
                 if(setConnectionMethod != null) setConnectionMethod.invoke(instance, new Object[] { cc.getConnection() });
             }
             else
             {
                 if(getDataSourceInfoMethod != null)
                 {
                     Object dataSourceInfo = getDataSourceInfoMethod.invoke(instance, null);
                     if(dataSourceInfo != null)
                     {
                         DriverManagerConnectionProvider.DataSourceInfo dsInfo = DriverManagerConnectionProvider.PROVIDER.createDataSource();
                         if(dataSourceInfo instanceof List)
                         {
                             if(! dsInfo.setInfo((List) dataSourceInfo))
                                 dc.getDialog().getLog().error("Found getDataSourceInfo() method in action class and it returns type List but has fewer than two entries. Need at least driver class and URL.");
                         }
                         else if(dataSourceInfo instanceof String[])
                         {
                             if(! dsInfo.setInfo((String[]) dataSourceInfo))
                                 dc.getDialog().getLog().error("Found getDataSourceInfo() method in action class and it returns type String[] but has fewer than two entries. Need at least driver class and URL.");
                         }
                         else if(dataSourceInfo instanceof Map)
                         {
                             if(! dsInfo.setInfo((Map) dataSourceInfo, null))
                                 dc.getDialog().getLog().error("Found getDataSourceInfo() method in action class and it returns type Map but does not have 'jdbc-driver-class' or 'jdbc-connection-url'.");
                         }
                         else
                             dc.getDialog().getLog().error("Found getDataSourceInfo() method in action class but it returns an object ("+ dataSourceInfo +") that NEFS doesn't understand.");
 
                         ConnectionContext cc = dc.openActionConnection(dsInfo);
                         if(setConnectionContextMethod != null) setConnectionContextMethod.invoke(instance, new Object[] { cc });
                         if(setConnectionMethod != null) setConnectionMethod.invoke(instance, new Object[] { cc.getConnection() });
                     }
                 }
             }
         }
     }
 
     public Object constructInstance(ActionDialogContext dc) throws Exception
     {
         Object result;
         try
         {
             result = actionConstructor.constructAction();
         }
         catch (Exception e)
         {
             actionDialog.getLog().error(e);
             throw new NestableRuntimeException(e);
         }
 
         initInstance(result, dc);
         assignInstanceValues(result, dc);
         assignDatabaseConnection(result, dc);
 
         return result;
     }
 
     public ActionExecutor getActionExecutor()
     {
         return actionExecutor;
     }
 
     protected interface ActionConstructor
     {
         public Object constructAction() throws Exception;
     }
 
     protected interface ActionValidator
     {
         public boolean isValid(Object instance, List messages) throws Exception;
     }
 
     protected interface ActionExecutor
     {
         public String execute(Writer writer, Object instance) throws Exception;
     }
 
     protected interface FieldMutator
     {
         public void set(Object instance, Value value) throws Exception;
     }
 
 }
