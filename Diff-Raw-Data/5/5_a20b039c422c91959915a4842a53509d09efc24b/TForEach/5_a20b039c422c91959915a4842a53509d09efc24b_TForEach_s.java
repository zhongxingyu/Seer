 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.component;
 
 import java.lang.reflect.Array;
 
 import javax.faces.component.NamingContainer;
 import javax.faces.component.UIComponentBase;
 import javax.faces.context.FacesContext;
 import javax.faces.internal.ComponentStates;
 import javax.faces.internal.NamingContainerUtil;
 
 import org.seasar.framework.beans.BeanDesc;
 import org.seasar.framework.beans.PropertyDesc;
 import org.seasar.framework.beans.factory.BeanDescFactory;
 import org.seasar.framework.container.S2Container;
 import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
 import org.seasar.framework.util.ClassUtil;
 import org.seasar.teeda.extension.ExtensionConstants;
 
 /**
  * @author higa
  * @author manhole
  */
 public class TForEach extends UIComponentBase implements NamingContainer {
 
     public static final String COMPONENT_TYPE = "org.seasar.teeda.extension.ForEach";
 
     public static final String COMPONENT_FAMILY = "org.seasar.teeda.extension.ForEach";
 
    private static final String DEFAULT_RENDERERTYPE = "org.seasar.teeda.extension.ForEach";
 
     private static final String INDEX_SUFFIX = "Index";
 
     private String pageName;
 
     private String itemsName;
 
     private ComponentStates componentStates = new ComponentStates();
 
     public TForEach() {
        setRendererType(DEFAULT_RENDERERTYPE);
     }
 
     public void setId(String id) {
         super.setId(id);
         NamingContainerUtil.refreshDescendantComponentClientId(this);
     }
 
     public String getClientId(FacesContext context) {
         String clientId = super.getClientId(context);
         int rowIndex = getRowIndex();
         if (rowIndex == INITIAL_ROW_INDEX) {
             return clientId;
         }
         return clientId + NamingContainer.SEPARATOR_CHAR + rowIndex;
     }
 
     private static final int INITIAL_ROW_INDEX = -1;
 
     private int rowIndex = INITIAL_ROW_INDEX;
 
     private int rowSize;
 
     private int getRowIndex() {
         return rowIndex;
     }
 
     public void setRowIndex(int rowIndex) {
         this.rowIndex = rowIndex;
     }
 
     public String getItemsName() {
         return itemsName;
     }
 
     public void setItemsName(String itemsName) {
         if (itemsName != null
                 && !itemsName.endsWith(ExtensionConstants.ITEMS_SUFFIX)) {
             throw new IllegalArgumentException(itemsName);
         }
         this.itemsName = itemsName;
     }
 
     public String getItemName() {
         if (itemsName == null) {
             return null;
         }
         return itemsName.substring(0, itemsName.length()
                 - ExtensionConstants.ITEMS_SUFFIX.length());
     }
 
     public String getIndexName() {
         String itemName = getItemName();
         if (itemName == null) {
             return null;
         }
         return itemName + INDEX_SUFFIX;
     }
 
     public String getPageName() {
         return pageName;
     }
 
     public void setPageName(String pageName) {
         this.pageName = pageName;
     }
 
     /**
      * @see javax.faces.component.UIComponent#getFamily()
      */
     public String getFamily() {
         return COMPONENT_FAMILY;
     }
 
     public void processValidators(FacesContext context) {
         if (context == null) {
             throw new NullPointerException("context");
         }
         if (!isRendered()) {
             return;
         }
         Object page = getPage();
         BeanDesc beanDesc = BeanDescFactory.getBeanDesc(page.getClass());
         if (beanDesc.hasPropertyDesc(getItemName())) {
             PropertyDesc itemPd = beanDesc.getPropertyDesc(getItemName());
             final Class itemType = itemPd.getPropertyType();
             final Object item = ClassUtil.newInstance(itemType);
             itemPd.setValue(page, item);
         }
         for (int i = 0; i < rowSize; ++i) {
             setRowIndex(i);
             restoreDescendantState(context);
             super.processValidators(context);
             saveDescendantComponentStates(context);
         }
     }
 
     public void processUpdates(FacesContext context) {
         if (context == null) {
             throw new NullPointerException("context");
         }
         if (!isRendered()) {
             return;
         }
         final Object page = getPage();
         final BeanDesc pageBeanDesc = BeanDescFactory.getBeanDesc(page
                 .getClass());
         final PropertyDesc itemsPd = pageBeanDesc
                 .getPropertyDesc(getItemsName());
         final Class itemsClass = itemsPd.getPropertyType();
         final Object[] items = (Object[]) Array.newInstance(itemsClass
                 .getComponentType(), rowSize);
         if (pageBeanDesc.hasPropertyDesc(getItemName())) {
             final PropertyDesc itemPd = pageBeanDesc
                     .getPropertyDesc(getItemName());
             final Class itemClass = itemPd.getPropertyType();
             for (int i = 0; i < rowSize; ++i) {
                 setRowIndex(i);
                 final Object item = ClassUtil.newInstance(itemClass);
                 itemPd.setValue(page, item);
                 restoreDescendantState(context);
                 super.processUpdates(context);
                 saveDescendantComponentStates(context);
                 items[i] = item;
             }
         } else {
             final Class itemClass = itemsClass.getComponentType();
             if (itemClass == null) {
                 // TODO 
                 throw new IllegalStateException();
             }
             final BeanDesc itemBeanDesc = BeanDescFactory
                     .getBeanDesc(itemClass);
             for (int i = 0; i < rowSize; ++i) {
                 setRowIndex(i);
                 restoreDescendantState(context);
                 super.processUpdates(context);
                 saveDescendantComponentStates(context);
                 final Object item = ClassUtil.newInstance(itemClass);
                 pageToItem(page, pageBeanDesc, item, itemBeanDesc);
                 items[i] = item;
             }
         }
         itemsPd.setValue(page, items);
     }
 
     private void pageToItem(final Object page, final BeanDesc pageBeanDesc,
             final Object item, final BeanDesc itemBeanDesc) {
         for (int i = 0; i < itemBeanDesc.getPropertyDescSize(); i++) {
             final PropertyDesc itemPd = itemBeanDesc.getPropertyDesc(i);
             final String propertyName = itemPd.getPropertyName();
             if (!pageBeanDesc.hasPropertyDesc(propertyName)) {
                 continue;
             }
             final PropertyDesc pagePd = pageBeanDesc
                     .getPropertyDesc(propertyName);
             final Object value = pagePd.getValue(page);
             itemPd.setValue(item, value);
         }
     }
 
     public void saveDescendantComponentStates(FacesContext context) {
         componentStates.saveDescendantComponentStates(context, this);
     }
 
     public void restoreDescendantState(FacesContext context) {
         componentStates.restoreDescendantState(context, this);
     }
 
     public Object getPage() {
         S2Container container = SingletonS2ContainerFactory.getContainer();
         return container.getComponent(getPageName());
     }
 
     public Object saveState(FacesContext context) {
         Object[] values = new Object[3];
         values[0] = super.saveState(context);
         values[1] = pageName;
         values[2] = itemsName;
         return values;
     }
 
     public void restoreState(FacesContext context, Object state) {
         Object values[] = (Object[]) state;
         super.restoreState(context, values[0]);
         pageName = (String) values[1];
         itemsName = (String) values[2];
     }
 
     public int getRowSize() {
         return rowSize;
     }
 
     public void setRowSize(int rowCount) {
         this.rowSize = rowCount;
     }
 
 }
