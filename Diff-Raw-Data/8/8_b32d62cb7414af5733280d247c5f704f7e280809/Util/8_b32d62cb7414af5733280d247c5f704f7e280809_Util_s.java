 /**
  *
  * SIROCCO
  * Copyright (C) 2013 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  * USA
  *
  */
 package org.ow2.sirocco.cloudmanager;
 
 import org.ow2.sirocco.cloudmanager.core.api.ICloudProviderManager;
 import org.ow2.sirocco.cloudmanager.core.api.exception.CloudProviderException;
 import org.ow2.sirocco.cloudmanager.model.cimi.extension.CloudProvider;
 import org.ow2.sirocco.cloudmanager.model.cimi.extension.CloudProviderLocation;
 import org.vaadin.teemu.wizards.Wizard;
 import org.vaadin.teemu.wizards.WizardStep;
 
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.server.Sizeable;
 import com.vaadin.server.Sizeable.Unit;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 
 public class Util {
     public static String printKibibytesValue(final int val) {
         String result;
         if (val < 1024) {
             result = val + " KB";
         } else if (val < 1024 * 1024) {
             result = String.format("%.0f MB", val / 1024.0f);
         } else {
             result = String.format("%.0f GB", ((float) val) / (1024 * 1024));
         }
         return result;
     }
 
     public static String printKilobytesValue(final int val) {
         String result;
         if (val < 1000) {
             result = val + " KB";
         } else if (val < 1000 * 1000) {
             result = String.format("%.0f MB", val / 1000.0f);
         } else {
             result = String.format("%.0f GB", ((float) val) / (1000 * 1000));
         }
         return result;
     }
 
     @SuppressWarnings("serial")
     public static class StateColumnGenerator implements Table.ColumnGenerator {
         @Override
         public com.vaadin.ui.Component generateCell(final Table source, final Object itemId, final Object columnId) {
             Property<?> prop = source.getItem(itemId).getItemProperty(columnId);
             String state = (String) prop.getValue();
             Label label = new Label();
             String iconFile;
             if (state.endsWith("ING")) {
                 iconFile = "loading.gif";
             } else if (state.equals("STARTED")) {
                 iconFile = "ball_green.gif";
             } else {
                 iconFile = "ball_red.gif";
             }
             label.setContentMode(ContentMode.HTML);
             label.setValue("<img src=\"" + "VAADIN/themes/mytheme/img/" + iconFile + "\" /> " + state);
             return label;
         }
     }
 
     @SuppressWarnings("serial")
     public static class LocationColumnGenerator implements Table.ColumnGenerator {
         public com.vaadin.ui.Component generateCell(final Table source, final Object itemId, final Object columnId) {
             Property<?> prop = source.getItem(itemId).getItemProperty(columnId);
             String country = (String) prop.getValue();
             Label label = new Label();
            String iconFile = country.toLowerCase() + "Flag.png";
            label.setContentMode(ContentMode.HTML);
            label.setValue("<img src=\"" + "VAADIN/themes/mytheme/img/" + iconFile + "\" /> " + country);
             return label;
         }
     }
 
     public static class PlacementStep implements WizardStep {
         FormLayout content;
 
         ComboBox providerBox;
 
         ComboBox locationBox;
 
         private ICloudProviderManager providerManager;
 
         PlacementStep(final Wizard wizard) {
             this.content = new FormLayout();
             this.content.setSizeFull();
             this.content.setHeight(Sizeable.SIZE_UNDEFINED, Unit.CM);
             this.content.setMargin(true);
 
             this.providerBox = new ComboBox("Provider");
             this.providerBox.setTextInputAllowed(false);
             this.providerBox.setNullSelectionAllowed(false);
             this.providerBox.setInputPrompt("select provider");
             this.providerBox.setImmediate(true);
             this.providerBox.addValueChangeListener(new Property.ValueChangeListener() {
 
                 @Override
                 public void valueChange(final ValueChangeEvent event) {
                     PlacementStep.this.locationBox.removeAllItems();
                     if (PlacementStep.this.providerBox.getValue() != null) {
                         try {
                             Integer id = (Integer) PlacementStep.this.providerBox.getValue();
                             CloudProvider provider = PlacementStep.this.providerManager.getCloudProviderById(id.toString());
                             for (CloudProviderLocation location : provider.getCloudProviderLocations()) {
                                 PlacementStep.this.locationBox.addItem(location.getCountryName());
                             }
                             if (PlacementStep.this.locationBox.getItemIds().size() == 1) {
                                 PlacementStep.this.locationBox.setValue(provider.getCloudProviderLocations().iterator().next()
                                     .getCountryName());
                             }
                         } catch (CloudProviderException e) {
                             e.printStackTrace();
                         }
                     }
                     wizard.updateButtons();
                 }
             });
             this.content.addComponent(this.providerBox);
 
             this.locationBox = new ComboBox("Location");
             this.locationBox.setTextInputAllowed(false);
             this.locationBox.setNullSelectionAllowed(false);
             this.locationBox.setInputPrompt("select location");
             this.locationBox.setImmediate(true);
             this.content.addComponent(this.locationBox);
             this.locationBox.addValueChangeListener(new Property.ValueChangeListener() {
 
                 @Override
                 public void valueChange(final ValueChangeEvent event) {
                     wizard.updateButtons();
                 }
             });
             Label spacer = new Label();
             spacer.setHeight("100%");
             this.content.addComponent(spacer);
             this.content.setExpandRatio(spacer, 1.0f);
         }
 
         void setProviderManager(final ICloudProviderManager providerManager) {
             this.providerManager = providerManager;
         }
 
         @Override
         public String getCaption() {
             return "Placement";
         }
 
         @Override
         public Component getContent() {
             return this.content;
         }
 
         @Override
         public boolean onAdvance() {
             return this.providerBox.getValue() != null && this.locationBox.getValue() != null;
         }
 
         @Override
         public boolean onBack() {
             return true;
         }
 
     }
 
     public static class MetadataStep implements WizardStep {
         FormLayout content;
 
         TextField nameField;
 
         TextArea descriptionField;
 
         MetadataStep(final Wizard wizard) {
             this.content = new FormLayout();
             this.content.setSizeFull();
             this.content.setMargin(true);
 
             this.nameField = new TextField("Name");
             this.nameField.setWidth("80%");
             this.nameField.setRequired(true);
             this.nameField.setRequiredError("Please provide a name");
             this.nameField.setImmediate(true);
             this.content.addComponent(this.nameField);
             this.nameField.addValueChangeListener(new Property.ValueChangeListener() {
 
                 @Override
                 public void valueChange(final ValueChangeEvent event) {
                     wizard.updateButtons();
                 }
             });
 
             this.descriptionField = new TextArea("Description");
             this.descriptionField.setWidth("80%");
             this.content.addComponent(this.descriptionField);
         }
 
         @Override
         public String getCaption() {
             return "Metadata";
         }
 
         @Override
         public Component getContent() {
             return this.content;
         }
 
         @Override
         public boolean onAdvance() {
             return !this.nameField.getValue().isEmpty();
         }
 
         @Override
         public boolean onBack() {
             return true;
         }
 
     }
 
 }
