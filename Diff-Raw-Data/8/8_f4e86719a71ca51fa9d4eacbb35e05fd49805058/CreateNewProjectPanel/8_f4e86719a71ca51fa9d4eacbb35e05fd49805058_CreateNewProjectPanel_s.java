 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.ibpworkbench.comp;
 
 import org.generationcp.ibpworkbench.model.formfieldfactory.ProjectFormFieldFactory;
 import org.generationcp.middleware.pojos.workbench.Project;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.VerticalLayout;
 
 @Configurable
 public class CreateNewProjectPanel extends VerticalLayout implements InitializingBean{
 
     private static final long serialVersionUID = 1L;
 
     private Label newProjectTitle;
 
     private Form form;
 
     private Button cancelButton;
 
     private Button saveButton;
 
     public CreateNewProjectPanel() {
         super();
     }
 
     @Override
     public void afterPropertiesSet() {
         assemble();
 
        form.setVisibleItemProperties(new String[] { "projectName", "targetDueDate", "cropType", "template", "tblLocation", "tblMethods" });
     }
 
     public Button getSaveButton() {
         return saveButton;
     }
 
     public Button getCancelButton() {
         return cancelButton;
     }
 
     public Form getForm() {
         return form;
     }
 
     protected void initializeComponents() {
         newProjectTitle = new Label("Create New Project");
         newProjectTitle.setStyleName("gcp-content-title");
 
         BeanItem<Project> projectBean = new BeanItem<Project>(new Project());
         projectBean.addItemProperty("tblLocation", new Table());
         projectBean.addItemProperty("tblMethods", new Table());
 
         form = new Form();
         form.setItemDataSource(projectBean);
         form.setFormFieldFactory(new ProjectFormFieldFactory());
 
         cancelButton = new Button("Cancel");
         saveButton = new Button("Save");
     }
 
     protected void initializeLayout() {
         setSpacing(true);
         setMargin(true);
 
         newProjectTitle.setSizeUndefined();
         addComponent(newProjectTitle);
 
         form.setSizeFull();
         addComponent(form);
 
         // add the save/cancel buttons
         Component buttonArea = layoutButtonArea();
         addComponent(buttonArea);
         setComponentAlignment(buttonArea, Alignment.TOP_RIGHT);
     }
 
     protected Component layoutButtonArea() {
         HorizontalLayout buttonLayout = new HorizontalLayout();
         buttonLayout.setSpacing(true);
         buttonLayout.setMargin(true, false, false, false);
 
         cancelButton = new Button("Cancel");
         saveButton = new Button("Save");
         buttonLayout.addComponent(cancelButton);
         buttonLayout.addComponent(saveButton);
 
         return buttonLayout;
     }
 
     protected void assemble() {
         initializeComponents();
         initializeLayout();
     }
 }
