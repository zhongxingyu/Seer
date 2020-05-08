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
 
 package org.generationcp.ibpworkbench.actions;
 
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.commons.vaadin.util.MessageNotifier;
 import org.generationcp.ibpworkbench.Message;
 import org.generationcp.ibpworkbench.comp.project.create.ProjectBasicDetailsComponent;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.api.WorkbenchDataManager;
 import org.generationcp.middleware.pojos.workbench.CropType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.ui.ComboBox;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.ui.AbstractSelect.NewItemHandler;
 
 @Configurable
 public class CropTypeComboAction implements ValueChangeListener, NewItemHandler{
 
     private static final long serialVersionUID = 1L;
     private static final Logger LOG = LoggerFactory.getLogger(CropTypeComboAction.class);
 
     @Autowired
     private WorkbenchDataManager workbenchDataManager;
 
     @Autowired
     private SimpleResourceBundleMessageSource messageSource;
 
     private ProjectBasicDetailsComponent sourceComponent;
     
     private ComboBox cropTypeComboBox;
 
     // Used to keep track if the selected item is the last item added to crop type combo box
     private boolean cropTypeComboBoxLastAdded;
 
     // Used to keep track of the last value selected
     private String lastValue;
 
     public CropTypeComboAction() {
         cropTypeComboBoxLastAdded = false;
         lastValue = null;
     }
 
     public CropTypeComboAction(ComboBox cropTypeComboBox) {
         this();
         setCropTypeComboBox(cropTypeComboBox);
     }
     
     public CropTypeComboAction(ProjectBasicDetailsComponent sourceComponent, ComboBox cropTypeComboBox) {
         this(cropTypeComboBox);
         setSourceComponent(sourceComponent);
     }
 
     public ProjectBasicDetailsComponent getSourceComponent() {
         return sourceComponent;
     }
 
     public void setSourceComponent(ProjectBasicDetailsComponent sourceComponent) {
         this.sourceComponent = sourceComponent;
     }
 
     public ComboBox getCropTypeComboBox() {
         return cropTypeComboBox;
     }
 
     public void setCropTypeComboBox(ComboBox cropTypeComboBox) {
         this.cropTypeComboBox = cropTypeComboBox;
     }
 
     @Override
     public void valueChange(ValueChangeEvent event) {
        
        String value = null;        
        if ((CropType) event.getProperty().getValue() != null) {
           value = ((CropType) event.getProperty().getValue()).getCropName();
        }
        
         boolean sameAsLastValue = lastValue == null ? value == null : lastValue.equals(value);
 
         if (sameAsLastValue || cropTypeComboBoxLastAdded) {
             return;
         } else {
             lastValue = value;
             cropTypeComboBoxLastAdded = false;
         }
 
         // set the visible properties again, so that all fields gets renewed
         if (sourceComponent != null){
             sourceComponent.refreshVisibleItems();
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void addNewItem(String newItemCaption) {
 
         try {
             // if not yet in the database
             if (!cropTypeComboBox.containsId(workbenchDataManager.getCropTypeByName(newItemCaption))) {
 
                 // add crop to database
                 CropType cropType = new CropType(newItemCaption);
                 workbenchDataManager.addCropType(cropType);
                 sourceComponent.getWindow().showNotification("Added crop " + newItemCaption);
 
                 // add the item to the combo box
                 CropType newCropType = workbenchDataManager.getCropTypeByName(newItemCaption);
                 lastValue = newCropType.toString();
                 cropTypeComboBoxLastAdded = true;
                 ((BeanItemContainer<CropType>) cropTypeComboBox.getContainerDataSource()).addBean(newCropType);
 
                 // set the combo box value to the newly added crop
                 cropTypeComboBox.setValue(newCropType);
                 
                 sourceComponent.setCropType(newCropType);
                 
                 if (sourceComponent != null){
                     sourceComponent.refreshVisibleItems();
                 }
             }
 
         } catch (MiddlewareQueryException e) {
             LOG.error("Error encountered while trying to add crop type.", e);
             MessageNotifier.showError(sourceComponent.getWindow(), messageSource.getMessage(Message.DATABASE_ERROR),
                     messageSource.getMessage(Message.ADD_CROP_TYPE_ERROR_DESC));
             return;
         }
 
     }
 
 }
