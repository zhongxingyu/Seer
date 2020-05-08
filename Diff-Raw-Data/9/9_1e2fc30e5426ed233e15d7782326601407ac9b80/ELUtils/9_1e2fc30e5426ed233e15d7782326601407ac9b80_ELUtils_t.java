 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.extensions.validator.util;
 
 import org.apache.myfaces.extensions.validator.internal.ToDo;
 import org.apache.myfaces.extensions.validator.internal.Priority;
 import org.apache.myfaces.extensions.validator.internal.UsageInformation;
 import org.apache.myfaces.extensions.validator.internal.UsageCategory;
 
 import javax.el.ValueExpression;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 
 /**
  * in order to centralize the jsf version dependency within the core
  *
  * @author Gerhard Petracek
  */
 @ToDo(Priority.MEDIUM)
 @UsageInformation(UsageCategory.INTERNAL)
 public class ELUtils
 {
     public static Class getTypeOfValueBindingForExpression(
         FacesContext facesContext, String valueBindingExpression)
     {
         //due to a restriction with the ri
         Object bean = ELUtils.getValueOfExpression(facesContext,
             valueBindingExpression);
         return (bean != null) ? bean.getClass() : null;
     }
 
     public static Object getBean(String beanName)
     {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         return facesContext.getApplication().getELResolver().getValue(
             facesContext.getELContext(), null, beanName);
     }
 
     @ToDo(value = Priority.MEDIUM, description = "refactor - problem - static values - jsf 1.2 e.g.: ${value}")
     public static Object getBaseObject(String valueBindingExpression, UIComponent uiComponent)
     {
         if (valueBindingExpression.lastIndexOf(".") == -1)
         {
             return uiComponent.getValueExpression("value").getValue(FacesContext.getCurrentInstance().getELContext());
         }
         return getBaseObject(valueBindingExpression);
     }
 
     public static Object getBaseObject(String valueBindingExpression)
     {
         String newExpression = valueBindingExpression.substring(0, valueBindingExpression.lastIndexOf(".")) + "}";
 
         return getValueOfExpression(FacesContext.getCurrentInstance(),
             newExpression);
     }
 
     public static Object getValueOfExpression(FacesContext facesContext, String valueBindingExpression)
     {
         return (valueBindingExpression != null) ? facesContext.getApplication().evaluateExpressionGet(facesContext,
             valueBindingExpression, Object.class) : null;
     }
 
     public static boolean isExpressionValid(FacesContext facesContext, String valueBindingExpression)
     {
         try
         {
             facesContext.getApplication().evaluateExpressionGet(facesContext, valueBindingExpression, Object.class);
         }
         catch (Throwable t)
         {
             return false;
         }
         return true;
     }
 
     public static String getReliableValueBindingExpression(
         UIComponent uiComponent)
     {
         String valueBindingExpression = getValueBindingExpression(uiComponent);
 
         String baseExpression = valueBindingExpression;

        //for input components without value-binding
        //(e.g. for special component libs -> issue with ExtValRendererWrapper#encodeBegin)
        if(baseExpression == null)
        {
            //TODO logging
            return null;
        }

         if (baseExpression.contains("."))
         {
             baseExpression = baseExpression.substring(0, valueBindingExpression.lastIndexOf(".")) + "}";
         }
 
         if (getTypeOfValueBindingForExpression(FacesContext.getCurrentInstance(), baseExpression) == null)
         {
             valueBindingExpression = FaceletsTaglibExpressionUtils.
                 tryToCreateValueBindingForFaceletsBinding(uiComponent);
         }
         return valueBindingExpression;
     }
 
     public static String getValueBindingExpression(UIComponent uiComponent)
     {
         ValueExpression valueExpression = uiComponent
             .getValueExpression("value");
 
         return (valueExpression != null) ? valueExpression
             .getExpressionString() : null;
     }
 
     public static Class getTypeOfValueBindingForComponent(
         FacesContext facesContext, UIComponent uiComponent)
     {
         ValueExpression valueExpression = uiComponent
             .getValueExpression("value");
 
         return (valueExpression != null) ? valueExpression.getType(facesContext
             .getELContext()) : null;
     }
 }
