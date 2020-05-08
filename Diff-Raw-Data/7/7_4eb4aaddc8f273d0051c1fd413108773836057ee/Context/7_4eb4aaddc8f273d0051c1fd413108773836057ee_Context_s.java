 /*
  * Copyright (C) 2012 AXIA Studio (http://www.axiastudio.com)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.axiastudio.pypapi.ui;
 
 import com.axiastudio.pypapi.Register;
 import com.axiastudio.pypapi.Resolver;
 import com.axiastudio.pypapi.db.*;
 import com.trolltech.qt.core.QModelIndex;
 import com.trolltech.qt.core.QObject;
 import com.trolltech.qt.gui.QDataWidgetMapper;
 import com.trolltech.qt.gui.QWidget;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  */
 public final class Context extends QObject {
 
     private TableModel model;
     private QDataWidgetMapper mapper;
     private Class rootClass;
     private String name;
     private Object currentEntity;
     private Boolean isDirty;
     private Boolean atBof=true;
     private Boolean atEof=true;
     private Context primaryDc;
     private IForm parent;
 
     public Context(IForm parent, Class rootClass, String name, List columns){
         this(parent, rootClass, name, columns, null);
     }
 
     public Context(IForm parent, Class rootClass, String name, List columns, Store store){
         Logger.getLogger(Context.class.getName()).log(Level.INFO, "Create {0} context", rootClass.toString()+name);
         this.parent = parent;
         this.rootClass = rootClass;
         this.name = name;
 
         this.model = this.createModel(store);
         this.mapper = new QDataWidgetMapper(this);
         this.mapper.setSubmitPolicy(QDataWidgetMapper.SubmitPolicy.AutoSubmit);
         this.mapper.setModel(this.model);
         this.model.setColumns(columns);
         this.initializeContext();
         this.isDirty = false;
         if( ".".equals(this.name) && this.model.getStore().isEmpty() ){
             this.insertElement();
         }
     }
 
     public Class getRootClass() {
         return rootClass;
     }
 
     private void initializeContext(){
         this.mapper.currentIndexChanged.connect(this, "indexChanged(int)");
         if(".".equals(this.name)){
             this.primaryDc = this;
         } else {
             this.primaryDc = (Context) Register.queryRelation((QWidget) this.parent, ".");
             this.primaryDc.mapper.currentIndexChanged.connect(this, "parentIndexChanged(int)");
         }
         this.model.dataChanged.connect(primaryDc, "modelDataChanged(QModelIndex, QModelIndex)");
         this.model.rowsRemoved.connect(primaryDc, "modelDataChanged(QModelIndex, int, int)");
         this.model.rowsInserted.connect(primaryDc, "modelDataChanged(QModelIndex, int, int)");
     }
 
 
     private TableModel createModel(Store store){
         TableModel tableModel;
 
         /* resolve entity class */
         if(".".equals(this.name)){
             Database db = (Database) Register.queryUtility(IDatabase.class);
             Controller controller = (Controller) Register.queryUtility(IController.class, this.rootClass.getName());
             if( store == null ){
                 store = controller.createFullStore();
             }
             tableModel = new TableModel(store, null);
         } else {
             tableModel = new TableModel(null, null);
         }
         tableModel.setContextHandle(this);
         
         return tableModel;
     }
 
     private void IndexChanged() {
         this.indexChanged(0);
     }
     private void indexChanged(int row){
         int cnt = this.model.rowCount();
         if(".".equals(this.name)){
             if(cnt==0){
                 this.atBof = true;
                 this.atEof = true;
             } else {
                 this.atBof = (row<1);
                 this.atEof = (row>=cnt-1);
             }
         }
         if(cnt>0){
             this.currentEntity = this.model.getEntityByRow(row);
         }
     }
 
     /*
      *  When the parent index changed, all the child contexts have to reset
      *  their stores.
      * 
      */
     private void parentIndexChanged(int row) throws Exception {
         Method getter = Resolver.getterFromFieldName(this.primaryDc.currentEntity.getClass(), this.name.substring(1));
         Method setter = Resolver.setterFromFieldName(this.primaryDc.currentEntity.getClass(), this.name.substring(1), Collection.class);
         List result = (List) getter.invoke(this.primaryDc.currentEntity);
         if( result == null ){
             result = new ArrayList();
             setter.invoke(this.primaryDc.currentEntity, result);
         }
         Store store = new Store(result);
         this.model.setStore(store);
         this.firstElement();
     }
 
     private void modelDataChanged(QModelIndex topLeft, QModelIndex bottomRight){
         this.isDirty = true;
     }
 
     private void modelDataChanged(QModelIndex topLeft, int start, int end){
         this.isDirty = true;
         this.model.dataChanged.emit(topLeft, topLeft);
     }
     
     public void firstElement(){
         this.mapper.toFirst();
     }
 
     public void nextElement(){
         this.mapper.toNext();
     }
 
     public void previousElement(){
         this.mapper.toPrevious();
     }
 
     public void lastElement(){
         this.mapper.toLast();
     }
 
     public void insertElement(){
         this.insertElement(null);
     }
     
     public void insertElement(Object newEntity){
         Object entity = null;
         QModelIndex idx=null;
         if ( newEntity == null ){
             String entityName = this.rootClass.getName();
             Class cls = (Class) Register.queryUtility(IFactory.class, entityName);
             try {
                 entity = cls.newInstance();
             } catch (InstantiationException ex) {
                 Logger.getLogger(TableModel.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(TableModel.class.getName()).log(Level.SEVERE, null, ex);
             }
         } else {
             entity = newEntity;
         }
         List entities = new ArrayList();
         boolean add = entities.add(entity);
         this.model.insertRows(this.model.rowCount(), 1, idx, entities);
         this.mapper.toLast();
         this.isDirty = true;
     }
 
     public void deleteElement(){
         QModelIndex idx=null;
         Controller c = (Controller) Register.queryUtility(IController.class, this.primaryDc.currentEntity.getClass().getName());
         c.delete(this.primaryDc.currentEntity);
         int row = this.mapper.currentIndex();
         this.mapper.toPrevious();
         this.model.removeRows(row, 1, idx);
         this.isDirty = false;
         this.mapper.currentIndexChanged.emit(this.mapper.currentIndex());
     }
 
     public void commitChanges(){
         Controller c = (Controller) Register.queryUtility(IController.class, this.primaryDc.currentEntity.getClass().getName());
         Validation val = c.commit(this.primaryDc.currentEntity);
         if( val.getResponse() == false ){
             Util.warningBox((QWidget) this.parent, "Error", val.getMessage());
             this.refreshElement();
         } else {
             this.isDirty = false;
             this.primaryDc.currentEntity = val.getEntity();
             this.model.replaceEntity(this.mapper.currentIndex(), this.primaryDc.currentEntity);
             this.mapper.currentIndexChanged.emit(this.mapper.currentIndex());
             this.mapper.revert();
         }
     }
 
     public void cancelChanges(){
         this.refreshElement();
     }
     
     public void refreshElement(){
         Controller c = (Controller) Register.queryUtility(IController.class, this.primaryDc.currentEntity.getClass().getName());
         this.primaryDc.currentEntity = c.refresh(this.primaryDc.currentEntity);
         this.model.replaceEntity(this.mapper.currentIndex(), this.primaryDc.currentEntity);
         this.isDirty = false;
         this.mapper.currentIndexChanged.emit(this.mapper.currentIndex());
         this.mapper.revert();
     }
     
     public void search(){
         Controller controller = (Controller) Register.queryUtility(IController.class, this.rootClass.getName());
         PickerDialog pd = new PickerDialog((QWidget) this.parent, controller);
         int res = pd.exec();
         if ( res == 1 ){
             this.model.replaceRows(pd.getSelection());
             this.firstElement();
         }
     }
     
     public void getDirty(){
        // XXX: it's the right way?
        QModelIndex index = this.getModel().index(0, 0);
         this.getModel().dataChanged.emit(index, index);
        this.mapper.currentIndexChanged.emit(0);
     }
     
     public TableModel getModel() {
         return model;
     }
 
     public QDataWidgetMapper getMapper() {
         return mapper;
     }
 
     public Object getCurrentEntity() {
         return currentEntity;
     }
 
     public Boolean getAtBof() {
         return atBof;
     }
 
     public Boolean getAtEof() {
         return atEof;
     }
 
     public Boolean getIsDirty() {
         return isDirty;
     }
     
 }
