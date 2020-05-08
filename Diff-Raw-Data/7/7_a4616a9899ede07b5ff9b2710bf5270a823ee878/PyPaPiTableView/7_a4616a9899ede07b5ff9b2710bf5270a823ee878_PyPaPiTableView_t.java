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
 package com.axiastudio.pypapi.ui.widgets;
 
 import com.axiastudio.pypapi.Register;
 import com.axiastudio.pypapi.Resolver;
 import com.axiastudio.pypapi.db.Controller;
 import com.axiastudio.pypapi.db.IController;
 import com.axiastudio.pypapi.ui.Form;
 import com.axiastudio.pypapi.ui.PickerDialog;
 import com.axiastudio.pypapi.ui.TableModel;
 import com.axiastudio.pypapi.ui.Util;
 import com.trolltech.qt.core.*;
 import com.trolltech.qt.gui.*;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Tiziano Lattisi <tiziano at axiastudio.it>
  *
  * PyPaPiTableView is a custom widget implementing info, open, add and delete
  * of rows in the bag list.
  *
  */
 public class PyPaPiTableView extends QTableView{
     
     private final String STYLE="QTableView {"
             + "image: url(classpath:com/axiastudio/pypapi/ui/resources/cog.png);"
             + "image-position: right; border: 1px solid #999999; }";
     private QAction actionAdd, actionDel, actionOpen, actionInfo;
     private QMenu menuPopup;
     private QToolBar toolBar;
     private Boolean refreshConnected=false;
 
     public PyPaPiTableView(){
         this(null);
     }
 
     public PyPaPiTableView(QWidget parent){
         /*
          *  init
          */
         super(parent);
         this.setStyleSheet(this.STYLE);
         this.setSelectionBehavior(SelectionBehavior.SelectRows);
         this.setSortingEnabled(true);
         this.horizontalHeader().setResizeMode(QHeaderView.ResizeMode.Interactive);
         this.verticalHeader().hide();
         this.initializeMenu();
 
     }
     
     private void initializeMenu(){
         this.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu);
         this.customContextMenuRequested.connect(this, "contextMenu(QPoint)");
         this.menuPopup = new QMenu(this);
         this.toolBar = new QToolBar(this);
         this.toolBar.setOrientation(Qt.Orientation.Vertical);
         this.toolBar.setIconSize(new QSize(16, 16));
         this.toolBar.move(1, 22);
         this.toolBar.hide();
 
         this.actionInfo = new QAction("Info", this);
         QIcon iconInfo = new QIcon("classpath:com/axiastudio/pypapi/ui/resources/toolbar/information.png");
         this.actionInfo.setIcon(iconInfo);
         this.menuPopup.addAction(actionInfo);
         this.toolBar.addAction(actionInfo);
         this.actionInfo.triggered.connect(this, "actionInfo()");
 
         this.actionOpen = new QAction("Open", this);
         QIcon iconOpen = new QIcon("classpath:com/axiastudio/pypapi/ui/resources/open.png");
         this.actionOpen.setIcon(iconOpen);
         this.menuPopup.addAction(actionOpen);
         this.toolBar.addAction(actionOpen);
         this.actionOpen.triggered.connect(this, "actionOpen()");
 
         this.actionAdd = new QAction("Add", this);
         QIcon iconAdd = new QIcon("classpath:com/axiastudio/pypapi/ui/resources/toolbar/add.png");
         this.actionAdd.setIcon(iconAdd);
         this.menuPopup.addAction(this.actionAdd);
         this.toolBar.addAction(this.actionAdd);
         this.actionAdd.triggered.connect(this, "actionAdd()");
 
         this.actionDel = new QAction("Delete", this);
         QIcon iconDel = new QIcon("classpath:com/axiastudio/pypapi/ui/resources/toolbar/delete.png");
         this.actionDel.setIcon(iconDel);
         this.menuPopup.addAction(actionDel);
         this.toolBar.addAction(actionDel);
         this.actionDel.triggered.connect(this, "actionDel()");
 
     }
     
 
     private void contextMenu(QPoint point){
         this.refreshButtons();
         QAction action = this.menuPopup.exec(this.mapToGlobal(point));
     }
     
     @Override
     protected void enterEvent(QEvent event){
         if( !this.refreshConnected ){
             this.selectionModel().selectionChanged.connect(this, "refreshButtons()");
             this.refreshConnected = true;
         }
         this.refreshButtons();
         this.toolBar.show();
     }
 
     @Override
     protected void leaveEvent(QEvent event){
         this.toolBar.hide();
     }
 
     
     private void refreshButtons(){
         List<QModelIndex> rows = this.selectionModel().selectedRows();
         Boolean selected = !rows.isEmpty();
         this.actionInfo.setEnabled(selected);
         this.actionOpen.setEnabled(selected);
         this.actionDel.setEnabled(selected);
     }
     
     private void actionOpen(){
         TableModel model = (TableModel) this.model();
         List<QModelIndex> rows = this.selectionModel().selectedRows();
         Object reference = Register.queryRelation(this, "reference");
         for (QModelIndex idx: rows){
             Object entity = model.getEntityByRow(idx.row());
             if ( reference != null ){
                 entity = Resolver.entityFromReference(entity, (String) reference);
             }
             Form form = Util.formFromEntity(entity);
             if( form == null ){
                 return;
             }
             QMdiArea workspace = Util.findParentMdiArea(this);
             if( workspace != null ){
                 workspace.addSubWindow(form);
             }
             form.show();
         }
     }
     
     private void actionInfo(){
         TableModel model = (TableModel) this.model();
         List<QModelIndex> rows = this.selectionModel().selectedRows();
         for (QModelIndex idx: rows){
             Object entity = model.getEntityByRow(idx.row());
             Form form = Util.formFromEntity(entity);
             if( form == null ){
                 return;
             }
             QMdiArea workspace = Util.findParentMdiArea(this);
             if( workspace != null ){
                 workspace.addSubWindow(form);
             }
             form.dialogize();
             form.show();
         }
     }
     
     private void actionDel(){
         // TODO: action del
     }
     
     private void actionAdd(){
         TableModel model = (TableModel) this.model();
         Class rootClass = model.getContextHandle().getRootClass();
         String entityName = (String) this.property("entity");
         Class collectionClass = Resolver.collectionClassFromReference(rootClass, entityName.substring(1));
         Object reference = Register.queryRelation(this, "reference");
         if ( reference != null ){
             String name = (String) reference;
             String className = Resolver.entityClassFromReference(collectionClass, (String) reference).getName();
             Controller controller = (Controller) Register.queryUtility(IController.class, className, true);
             PickerDialog pd = new PickerDialog(this, controller);
             int res = pd.exec();
             if ( res == 1 ){
                for( int i=0; i<pd.getSelection().size(); i++ ){
                    Object entity = pd.getSelection().get(i);
                     Object adapted = null;
                     Class<?> classFrom = entity.getClass();
                     Class<?> classTo = collectionClass;
 
                     // from class to class
                     Method adapter = (Method) Register.queryAdapter(classFrom, classTo);
                     String fromTo;
                     if( adapter == null ){
                         // from iface to class
                         Class<?> ifaceFrom = Resolver.interfaceFromEntityClass(entity.getClass());
                         adapter = (Method) Register.queryAdapter(ifaceFrom, classTo);
                         if( adapter == null ){
                             // form class to iface
                             Class<?> ifaceTo = Resolver.interfaceFromEntityClass(collectionClass);
                             adapter = (Method) Register.queryAdapter(classFrom, ifaceTo);
                             if( adapter == null ){
                                 // from iface to iface
                                 adapter = (Method) Register.queryAdapter(ifaceFrom, ifaceTo);
                             }
                         }
                     }

                     if( adapter != null ){
                         try {
                             adapted = adapter.invoke(null, entity);
                         } catch (IllegalAccessException ex) {
                             Logger.getLogger(PyPaPiTableView.class.getName()).log(Level.SEVERE, null, ex);
                         } catch (IllegalArgumentException ex) {
                             Logger.getLogger(PyPaPiTableView.class.getName()).log(Level.SEVERE, null, ex);
                         } catch (InvocationTargetException ex) {
                             Logger.getLogger(PyPaPiTableView.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     } else {
                         List<Method> setters = Resolver.settersFromEntityClass(classTo, classFrom);
                         if(setters.size()==1){
                             try {
                                 adapted = classTo.newInstance();
                                 setters.get(0).invoke(adapted, entity);
                             } catch (IllegalArgumentException ex) {
                                 Logger.getLogger(PyPaPiTableView.class.getName()).log(Level.SEVERE, null, ex);
                             } catch (InvocationTargetException ex) {
                                 Logger.getLogger(PyPaPiTableView.class.getName()).log(Level.SEVERE, null, ex);
                             } catch (InstantiationException ex) {
                                 Logger.getLogger(PyPaPiTableView.class.getName()).log(Level.SEVERE, null, ex);
                             } catch (IllegalAccessException ex) {
                                 Logger.getLogger(PyPaPiTableView.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         }
                     }
 
                     if( adapted != null ){
                         model.getContextHandle().insertElement(adapted);
                     } else {
                         String title = "Adapter warning";
                         String description = "Unable to find an adapter from "+classFrom+" to "+classTo+".";
                         Util.warningBox(this, title, description);
                     }
                 }
             }
         } else {
             try {
                 Object notAdapted = collectionClass.newInstance();
                 model.getContextHandle().insertElement(notAdapted);
             } catch (InstantiationException ex) {
                 Logger.getLogger(PyPaPiTableView.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(PyPaPiTableView.class.getName()).log(Level.SEVERE, null, ex);
             }
 
         }
         /* select and open info for the last row */
         this.selectRow(this.model().rowCount()-1);
         this.actionInfo();
     }
     
 
 }
