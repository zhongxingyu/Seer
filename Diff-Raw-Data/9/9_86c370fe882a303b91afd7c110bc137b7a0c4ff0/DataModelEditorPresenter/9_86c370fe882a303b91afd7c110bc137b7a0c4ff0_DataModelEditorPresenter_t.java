 /**
  * Copyright 2012 JBoss Inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jbpm.datamodeler.editor.client.editors;
 
 import org.jboss.errai.bus.client.api.RemoteCallback;
 import org.jboss.errai.ioc.client.api.Caller;
 import org.jbpm.datamodeler.editor.client.editors.resources.i18n.Constants;
 import org.jbpm.datamodeler.editor.client.validation.ValidatorService;
 import org.jbpm.datamodeler.editor.events.DataModelerEvent;
 import org.jbpm.datamodeler.editor.events.DataObjectSelectedEvent;
 import org.jbpm.datamodeler.editor.model.DataModelTO;
 import org.jbpm.datamodeler.editor.model.PropertyTypeTO;
 import org.jbpm.datamodeler.editor.service.DataModelerService;
 import org.uberfire.backend.vfs.Path;
 import org.uberfire.client.annotations.*;
 import org.uberfire.client.common.BusyPopup;
 import org.uberfire.client.context.WorkbenchContext;
 import org.uberfire.client.mvp.UberView;
 import org.uberfire.client.workbench.widgets.events.NotificationEvent;
 import org.uberfire.client.workbench.widgets.events.PathChangeEvent;
 import org.uberfire.client.workbench.widgets.menu.MenuFactory;
 import org.uberfire.client.workbench.widgets.menu.MenuItem;
 import org.uberfire.client.workbench.widgets.menu.Menus;
 import org.uberfire.client.workbench.widgets.toolbar.IconType;
 import org.uberfire.client.workbench.widgets.toolbar.ToolBar;
 import org.uberfire.client.workbench.widgets.toolbar.ToolBarItem;
 import org.uberfire.client.workbench.widgets.toolbar.impl.DefaultToolBar;
 import org.uberfire.client.workbench.widgets.toolbar.impl.DefaultToolBarItem;
 
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.uberfire.client.workbench.widgets.menu.MenuFactory.newSimpleItem;
 
 //@Dependent
 //@WorkbenchEditor(identifier = "DataModelEditor", supportedTypes = {DataModelResourceType.class })
 @WorkbenchScreen(identifier = "dataModelerScreen")
 public class DataModelEditorPresenter {
 
     public interface DataModelEditorView
             extends
             UberView<DataModelEditorPresenter> {
 
         void setDataModel(DataModelTO dataModel);
 
         void setBaseTypes(List<PropertyTypeTO> baseTypes);
 
         void refreshObjectEditor();
     }
 
     @Inject
     private DataModelEditorView view;
 
     @Inject
     private NewDataObjectPopup newDataObjectPopup;
 
     @Inject
     private Caller<DataModelerService> modelerService;
 
     @Inject
     private ValidatorService validatorService;
 
     private Menus menus;
 
     private ToolBar toolBar;
 
     @Inject
     private DataModelEditorSelectionModel selectionModel;
 
     @Inject
     Event<DataModelerEvent> dataModelerEvent;
 
     @Inject
     private Event<NotificationEvent> notification;
 
     @Inject
     private WorkbenchContext workbenchContext;
 
     private Path currentProject;
 
     @WorkbenchPartTitle
     public String getTitle() {
         return Constants.INSTANCE.modelEditor_screen_name() + " [" + (currentProject != null ? currentProject.getFileName() : "") + "]";
     }
 
     @WorkbenchPartView
     public UberView<DataModelEditorPresenter> getView() {
         return view;
     }
 
     @OnStart
     public void onStart() {
         makeMenuBar();
         makeToolBar();
         processPathChange(workbenchContext.getActivePath());
     }
 
     @IsDirty
     public boolean isDirty() {
         //TODO implement dirty status management
         return false;
     }
 
     @OnMayClose
     public boolean onMayClose() {
         //TODO implement dirty status management
         return true;
     }
 
     @OnSave
     public void onSave() {
 
         BusyPopup.showMessage("Saving project datamodel");
         modelerService.call(new RemoteCallback<Object>() {
                 @Override
                 public void callback(Object response) {
                     BusyPopup.close();
                     restoreModelStatus();
                     notification.fire(new NotificationEvent(Constants.INSTANCE.modelEditor_notification_dataModel_saved()));
                 }
             },
             new DataModelerErrorCallback("An error was produced during data model saving.")
         ).saveModel(getDataModel(), currentProject);
     }
 
     @WorkbenchMenu
     public Menus getMenus() {
         return menus;
     }
 
     @WorkbenchToolBar
     public ToolBar getToolBar() {
         return this.toolBar;
     }
 
     private void loadProjectDataModel(final Path path) {
 
         BusyPopup.showMessage(Constants.INSTANCE.modelEditor_loading());
 
         modelerService.call(
                 new RemoteCallback<List<PropertyTypeTO>>() {
                     @Override
                     public void callback(List<PropertyTypeTO> baseTypes) {
                         view.setBaseTypes(baseTypes);
                     }
                 },
                 new DataModelerErrorCallback("An error was produced when property types was loaded from server.")
         ).getBasePropertyTypes();
 
         modelerService.call(
                 new RemoteCallback<DataModelTO>() {
 
                     @Override
                     public void callback(DataModelTO dataModel) {
                         BusyPopup.close();
                         setDataModel(dataModel);
                         notification.fire(new NotificationEvent(Constants.INSTANCE.modelEditor_notification_dataModel_loaded(path.toString())));
                     }
 
                 },
                 new DataModelerErrorCallback("An error was produced when the requested data model was loaded from server.")
         ).loadModel(path);
 
         currentProject = path;
     }
 
     public DataModelTO getDataModel() {
         return selectionModel.getSelectedModel();
     }
 
     private void setDataModel(DataModelTO dataModel) {
        // Set data model helper before anything else
        if (dataModel != null) {
             dataModel.setHelper(new DataModelHelperImpl(dataModel));
             selectionModel.setSelectedModel(dataModel);
             view.setDataModel(dataModel);
            if (dataModel.getDataObjects().size() > 0) {
                dataModelerEvent.fire(new DataObjectSelectedEvent(DataModelerEvent.DATA_MODEL_BROWSER, getDataModel(), dataModel.getDataObjects().get(0)));
            }
         }
     }
 
     private void onNewDataObject() {
         newDataObjectPopup.setDataModel(getDataModel());
         newDataObjectPopup.show();
     }
 
     private void onPathChange(@Observes final PathChangeEvent event) {
         processPathChange(event.getPath());
     }
 
     private void processPathChange(Path newPath) {
 
         if (newPath != null) {
 
             modelerService.call(
                     new RemoteCallback<Path>() {
                         @Override
                         public void callback(Path projectPath) {
 
                             if (projectPath != null) {
                                 if (!projectPath.equals(currentProject)) {
                                     loadProjectDataModel(projectPath);
                                 }
                             } else {
                                 //TODO
                                 //check what will be the default policy when the user click in a place
                                 //that doesn't belong to a project.
                             }
                         }
                     },
                     new DataModelerErrorCallback("An error was produced during current project path calculation.")
             ).resolveProject(newPath);
 
         } else {
             //TODO check if this is posible. By definition we will always have a path.
         }
     }
 
     private void restoreModelStatus() {
         //when the model is saved without errors
         //clean the deleted dataobjects status, mark all dataobjects as persisted, etc.
         getDataModel().setPersistedStatus();
     }
 
     private void makeMenuBar() {
         menus = MenuFactory
                     .newTopLevelMenu(Constants.INSTANCE.modelEditor_menu_main())
                     .withItems( getItems() )
                     .endMenu().build();
     }
 
     private void makeToolBar() {
         toolBar = new DefaultToolBar( "dataModelerToolbar" );
 
         org.uberfire.client.mvp.Command saveCommand = new org.uberfire.client.mvp.Command() {
             @Override
             public void execute() {
                 onSave();
             }
         };
 
         org.uberfire.client.mvp.Command newDataObjectCommand = new org.uberfire.client.mvp.Command() {
             @Override
             public void execute() {
                 onNewDataObject();
             }
         };
 
         ToolBarItem item = new DefaultToolBarItem( IconType.SAVE, Constants.INSTANCE.modelEditor_menu_save(), saveCommand);
         toolBar.addItem(item);
 
         item = new DefaultToolBarItem( IconType.FILE, Constants.INSTANCE.modelEditor_menu_new_dataObject(), newDataObjectCommand);
         toolBar.addItem(item);
 
     }
 
     private List<MenuItem> getItems() {
 
         final List<MenuItem> menuItems = new ArrayList<MenuItem>();
 
         org.uberfire.client.mvp.Command newDataObjectCommand = new org.uberfire.client.mvp.Command() {
             @Override
             public void execute() {
                 onNewDataObject();
             }
         };
 
         org.uberfire.client.mvp.Command saveCommand = new org.uberfire.client.mvp.Command() {
             @Override
             public void execute() {
                 onSave();
             }
         };
 
         if ( newDataObjectCommand != null ) {
             menuItems.add(newSimpleItem(Constants.INSTANCE.modelEditor_menu_new_dataObject())
                     .respondsWith(newDataObjectCommand)
                     .endMenu().build().getItems().get(0));
         }
 
         if ( saveCommand != null ) {
             menuItems.add(newSimpleItem(Constants.INSTANCE.modelEditor_menu_save())
                     .respondsWith(saveCommand)
                     .endMenu().build().getItems().get(0));
         }
 
         return menuItems;
     }
 
 
     /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
     /*
     TODO remover este metodo cuando ya no lo necesitemos
     lo dejo por si necesitamos hacer copy paste de la parte de las valiaciones.
 
     public Command createAddDataObjectCommand(final String packageName, final String name, final String superClassName) {
         return new Command() {
 
             //TODO validate that the package name is a valid java package name
 
             @Override
             public void execute() {
                 //TODO this method remains here by now until we decide if a data object can be created from different
                 //IU places. (toolbar, menu, and current button)
 
                 validatorService.isValidIdentifier(name, new ValidatorCallback() {
                     @Override
                     public void onFailure() {
                         ErrorPopup.showMessage("Invalid data object identifier: " + name + " is not a valid Java identifier");
                     }
 
                     @Override
                     public void onSuccess() {
 
                         //TODO if whe have a valid packageName and a valid class name
                         //we can proceed to uniqueEntiyName validation
 
                         //TODO dejar esto prolijo
                         validatorService.isUniqueEntityName(packageName, name, getDataModel(), new ValidatorCallback() {
                             @Override
                             public void onFailure() {
                                 ErrorPopup.showMessage("A data object with identifier: " + name + " already exists in the model.");
                             }
 
                             @Override
                             public void onSuccess() {
                                 DataObjectTO dataObject = new DataObjectTO(name);
                                 dataObject.setPackageName(getDataModel().getDefaultPackage());
                                 getDataModel().getDataObjects().add(dataObject);
 
                                 //view.addDataObject(dataObject);
                                 selectionModel.setSelectedObject(dataObject);
 
                                 notification.fire(new NotificationEvent(Constants.INSTANCE.modelEditor_notification_dataObject_created(name)));
                                 dataModelerEventEvent.fire(new DataObjectCreatedEvent("", getDataModel(), dataObject));
 
                             }
                         });
                     }
                 });
             }
         };
     }
     */
 
 }
