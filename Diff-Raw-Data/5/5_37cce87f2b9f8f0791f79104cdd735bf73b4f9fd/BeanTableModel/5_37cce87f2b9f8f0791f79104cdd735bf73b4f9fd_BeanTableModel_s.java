 /*
  * Copyright 2010 Raffael Herzog
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
 *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ch.raffael.util.swing.beans;
 
 import java.beans.BeanInfo;
 import java.beans.EventSetDescriptor;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableColumn;
 
 import org.slf4j.Logger;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import ch.raffael.util.beans.BeanException;
 import ch.raffael.util.common.logging.LogUtil;
 
 
 /**
  * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  */
 public class BeanTableModel<T> extends AbstractTableModel {
 
     private final Logger logger = LogUtil.getLogger(this);
 
     private final Class<?> beanClass;
     private final Map<String, PropertyDescriptor> propertyDescriptors;
     private final EventSetDescriptor propertyChangeEventSet;
     private boolean followChanges;
     
     private final List<Column> columns = new ArrayList<Column>();
 
     private final List<T> beans;
 
     private boolean editable;
 
     private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
         @SuppressWarnings({ "SuspiciousMethodCalls" })
         @Override
         public void propertyChange(final PropertyChangeEvent evt) {
             if ( !SwingUtilities.isEventDispatchThread() ) {
                 SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                         propertyChange(evt);
                     }
                 });
             }
             else {
                 int col = indexOfColumn(evt.getPropertyName());
                 if ( col >= 0 ) {
                     int row = beans.indexOf(evt.getSource());
                     if ( row >= 0 ) {
                         fireTableCellUpdated(row, col);
                     }
                     else {
                         // this warning won't always be logged; but oh, well ...
                         // there are probably much more beans than columns, so I check the columns first
                         logger.warn("Property change event received from unknown source {}", evt.getSource());
                     }
                 }
             }
         }
     };
 
     public BeanTableModel(@NotNull Class<T> beanClass) {
         this(beanClass, null);
     }
 
     public BeanTableModel(@NotNull Class<T> beanClass, @Nullable Collection<T> beans) {
         this.beanClass = beanClass;
         try {
             Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<String, PropertyDescriptor>();
             BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
             for ( PropertyDescriptor prop : beanInfo.getPropertyDescriptors() ) {
                 propertyDescriptors.put(prop.getName(), prop);
             }
             this.propertyDescriptors = Collections.unmodifiableMap(propertyDescriptors);
             EventSetDescriptor propertyChangeEventSet = null;
             for ( EventSetDescriptor evt : beanInfo.getEventSetDescriptors() ) {
                 if ( evt.getName().equals("propertyChange") ) {
                     propertyChangeEventSet = evt;
                     break;
                 }
             }
             this.propertyChangeEventSet = propertyChangeEventSet;
             this.followChanges = propertyChangeEventSet != null;
         }
         catch ( IntrospectionException e ) {
             throw new BeanException("Cannot introspect bean class " + beanClass.getName(), e);
         }
         if ( beans == null || beans.isEmpty() ) {
             this.beans = new ArrayList<T>();
         }
         else {
             this.beans = new ArrayList<T>(beans);
             for ( T bean : beans ) {
                 registerBean(bean);
             }
         }
     }
 
     @NotNull
     public static <T> BeanTableModel<T> create(@NotNull Class<T> beanClass) {
         return new BeanTableModel<T>(beanClass);
     }
 
     @NotNull
     public static <T> BeanTableModel<T> create(@NotNull Class<T> beanClass, @Nullable Collection<T> beans) {
         return new BeanTableModel<T>(beanClass, beans);
     }
 
     @NotNull
     public Class<?> getBeanClass() {
         return beanClass;
     }
 
     public void addColumn(@NotNull String propertyName) {
         addColumn(-1, propertyName, null);
     }
 
     public void addColumn(@NotNull String propertyName, @Nullable String title) {
         addColumn(-1, propertyName, title);
     }
     
     public void addColumn(int index, @NotNull String propertyName) {
         addColumn(index, propertyName, null);
     }
 
     public void addColumn(int index, @NotNull String propertyName, @Nullable String title) {
         PropertyDescriptor property = getDescriptor(propertyName);
         if ( title == null ) {
             title = property.getDisplayName();
         }
         Column col = new Column(title, property);
         if ( index >= 0 ) {
             columns.add(index, col);
         }
         else {
             columns.add(col);
         }
     }
 
     @NotNull
     public List<Column> getColumns() {
         return Collections.unmodifiableList(columns);
     }
 
     public int indexOfColumn(@NotNull String propertyName) {
         for ( int i = 0; i < columns.size(); i++ ) {
             if ( columns.get(i).property.getName().equals(propertyName) ) {
                 return i;
             }
         }
         return -1;
     }
 
     @NotNull
     public TableColumn getColumn(@NotNull JTable table, @NotNull String propertyName) {
         int index = indexOfColumn(propertyName);
         if ( index <= 0 ) {
             throw new IllegalArgumentException("No column for property '" + propertyName + "'");
         }
        return table.getColumn(index);
     }
 
     private PropertyDescriptor getDescriptor(@NotNull String propertyName) {
         PropertyDescriptor descriptor = propertyDescriptors.get(propertyName);
         if ( descriptor == null ) {
             throw new BeanException("Property " + beanClass.getName() + "::" + propertyName + " does not exist");
         }
         if ( descriptor.getReadMethod() == null ) {
             throw new BeanException("Property " + beanClass.getName() + "::" + propertyName + " is not readable");
         }
         return descriptor;
     }
 
     public boolean isFollowChanges() {
         return followChanges;
     }
 
     public void setFollowChanges(boolean followChanges) {
         if ( propertyChangeEventSet != null ) {
             if ( this.followChanges != followChanges ) {
                 if ( this.followChanges ) {
                     for ( T bean : beans ) {
                         removePropertyChangeListener(bean);
                     }
                 }
                 this.followChanges = followChanges;
                 if ( this.followChanges ) {
                     for ( T bean : beans ) {
                         addPropertyChangeListener(bean);
                     }
                 }
             }
         }
         else {
             if ( followChanges ) {
                 logger.warn("Class {} does not support property change events; keeping followChanges false", beanClass.getName());
             }
         }
     }
 
     public boolean isEditable() {
         return editable;
     }
 
     public void setEditable(boolean editable) {
         this.editable = editable;
     }
 
     public void addBean(@NotNull T bean) {
         addBean(beans.size(), bean);
     }
 
     public void addBean(int index, @NotNull T bean) {
         beans.add(index, bean);
         registerBean(bean);
         fireTableRowsInserted(index, index);
     }
 
     public void addBeans(@NotNull Collection<T> beans) {
         if ( !beans.isEmpty() ) {
             int start = beans.size();
             beans.addAll(beans);
             for ( T bean : beans ) {
                 registerBean(bean);
             }
             fireTableRowsInserted(start, beans.size() - 1);
         }
     }
 
     @NotNull
     public T removeBean(int index) {
         T bean = beans.remove(index);
         unregisterBean(bean);
         fireTableRowsDeleted(index, index);
         return bean;
     }
 
     public boolean removeBean(@NotNull T bean) {
         int index = beans.indexOf(bean);
         if ( index >= 0 ) {
             removeBean(index);
             return true;
         }
         else {
             return false;
         }
     }
 
     public void clearBeans() {
         if ( !beans.isEmpty() ) {
             for ( T bean : beans ) {
                 unregisterBean(bean);
             }
             int size = beans.size();
             beans.clear();
             fireTableRowsDeleted(0, size - 1);
         }
     }
 
     public void setBeans(@NotNull Collection<T> beans) {
         clearBeans();
         addBeans(beans);
     }
 
     @NotNull
     public List<T> getBeans() {
         return Collections.unmodifiableList(beans);
     }
 
     @NotNull
     public T getRow(int index) {
         return beans.get(index);
     }
 
     @Override
     public int getRowCount() {
         return beans.size();
     }
 
     @Override
     public int getColumnCount() {
         return columns.size();
     }
 
     @Override
     public String getColumnName(int column) {
         return columns.get(column).title;
     }
 
     @Override
     @NotNull
     public Class<?> getColumnClass(int columnIndex) {
         return columns.get(columnIndex).property.getPropertyType();
     }
 
     @Override
     @Nullable
     public Object getValueAt(int rowIndex, int columnIndex) {
         T bean = getRow(rowIndex);
         Column col = columns.get(columnIndex);
         try {
             return col.property.getReadMethod().invoke(bean);
         }
         catch ( InvocationTargetException e ) {
             throw new BeanException("Error reading property " + beanClass.getName() + "::" + col.property.getName() + " from " + bean, e);
         }
         catch ( IllegalAccessException e ) {
             throw new BeanException("Error reading property " + beanClass.getName() + "::" + col.property.getName() + " from " + bean, e);
         }
     }
 
     @Override
     public boolean isCellEditable(int rowIndex, int columnIndex) {
         if ( !isEditable() ) {
             return false;
         }
         Column col = columns.get(columnIndex);
         return col.property.getWriteMethod() != null;
     }
 
     @Override
     public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
         T bean = getRow(rowIndex);
         Column col = columns.get(columnIndex);
         if ( col.property.getWriteMethod() == null ) {
             throw new BeanException("Property " + beanClass.getName() + "::" + col.property.getName() + " is not writable");
         }
         try {
             col.property.getWriteMethod().invoke(bean, aValue);
         }
         catch ( InvocationTargetException e ) {
             throw new BeanException("Error writing property " + beanClass.getName() + "::" + col.property.getName() + " from " + bean, e);
         }
         catch ( IllegalAccessException e ) {
             throw new BeanException("Error writing property " + beanClass.getName() + "::" + col.property.getName() + " from " + bean, e);
         }
     }
 
     protected void registerBean(@NotNull T bean) {
         if ( isFollowChanges() ) {
             addPropertyChangeListener(bean);
         }
     }
 
     protected void addPropertyChangeListener(@NotNull T bean) {
         try {
                 propertyChangeEventSet.getAddListenerMethod().invoke(bean, propertyChangeListener);
             }
             catch ( InvocationTargetException e ) {
                 throw new BeanException("Error adding property change listener to " + bean, e);
             }
             catch ( IllegalAccessException e ) {
                 throw new BeanException("Error adding property change listener to " + bean, e);
             }
     }
 
     protected void unregisterBean(@NotNull T bean) {
         if ( isFollowChanges() ) {
             removePropertyChangeListener(bean);
         }
     }
 
     protected void removePropertyChangeListener(@NotNull T bean) {
         try {
             propertyChangeEventSet.getRemoveListenerMethod().invoke(bean, propertyChangeListener);
         }
         catch ( InvocationTargetException e ) {
             throw new BeanException("Error removing property change listener from " + bean, e);
         }
         catch ( IllegalAccessException e ) {
             throw new BeanException("Error removing property change listener from " + bean, e);
         }
     }
 
     private class Column {
         private final String title;
         private final PropertyDescriptor property;
         private Column(String title, PropertyDescriptor property) {
             this.title = title;
             this.property = property;
         }
     }
 
 }
