 /*
  * Copyright 2012 Danylo Vashchilenko
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.key2gym.presentation.panels.forms;
 
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 import com.jgoodies.forms.layout.FormLayout;
 import java.util.List;
 import java.util.ResourceBundle;
 import javax.swing.*;
 import org.jdesktop.beansbinding.*;
 import org.key2gym.business.AdSourcesService;
 import org.key2gym.business.dto.AdSourceDTO;
 import org.key2gym.business.dto.ClientProfileDTO;
 import org.key2gym.business.dto.ClientProfileDTO.FitnessExperience;
 import org.key2gym.business.dto.ClientProfileDTO.Sex;
 import org.key2gym.presentation.renderers.AdSourceCellRenderer;
 import org.key2gym.presentation.renderers.FitnessExperienceListCellRenderer;
 import org.key2gym.presentation.util.DateMidnightToStringConverter;
 import org.key2gym.presentation.util.FormBindingListener;
 import org.key2gym.presentation.util.SexListCellRenderer;
 
 /**
  *
  * @author Danylo Vashchilenko
  */
 public class ClientProfileFormPanel extends JPanel {
 
     /**
      * Creates new form ClientProfileFormPanel
      */
     public ClientProfileFormPanel() {
         adSourcesService = AdSourcesService.getInstance();
         adSources = adSourcesService.getAdSources();
 
         initComponents();
         buildForm();
     }
 
     /**
      * Initializes the form's components.
      */
     private void initComponents() {
 
         birthdayTextField = new JTextField();
 
         /*
          * Height
          */
         heightSpinner = new JSpinner();
         heightSpinner.setModel(new SpinnerNumberModel(175, 0, null, 5));
 
         /*
          * Weight
          */
         weightSpinner = new JSpinner();
         weightSpinner.setModel(new SpinnerNumberModel(70, 0, null, 5));
 
         addressTextField = new JTextField();
         telephoneTextField = new JTextField();
         goalTextField = new JTextField();
         possibleAttendanceRateTextField = new JTextField();
         healthRestrictionsTextField = new JTextField();
         favouriteSportTextField = new JTextField();
         specialWishesTextField = new JTextField();
 
         /*
          * Ad sources.
          */
         adSourceComboBox = new JComboBox();
         adSourceComboBox.setRenderer(new AdSourceCellRenderer());
         
         /*
          * Adds "No ad source" option
          */
         adSources.add(0, null);
         
         adSourceComboBox.setModel(new DefaultComboBoxModel(adSources.toArray()));
         adSourceComboBox.setSelectedIndex(0);
 
         /*
          * Sex
          */
         sexComboBox = new JComboBox();
         sexComboBox.setModel(new DefaultComboBoxModel(new Sex[]{Sex.UNKNOWN, Sex.MALE, Sex.FEMALE}));
         sexComboBox.setRenderer(new SexListCellRenderer());
 
         /*
          * Fitness experience
          */
         fitnessExperienceComboBox = new JComboBox();
         fitnessExperienceComboBox.setModel(new DefaultComboBoxModel(new FitnessExperience[]{FitnessExperience.UNKNOWN, FitnessExperience.NO, FitnessExperience.YES}));
         fitnessExperienceComboBox.setRenderer(new FitnessExperienceListCellRenderer());
 
     }
 
     /**
      * Builds the form by placing the components on it.
      */
     private void buildForm() {
         FormLayout layout = new FormLayout("right:default, 4dlu, default:grow", "");
         DefaultFormBuilder builder = new DefaultFormBuilder(layout, bundle, this);
 
         builder.appendI15d("Label.Sex", sexComboBox);
         builder.nextLine();
 
         builder.appendI15d("Label.Birthday", birthdayTextField);
         builder.nextLine();
 
         builder.appendI15d("Label.Address", addressTextField);
         builder.nextLine();
 
         builder.appendI15d("Label.Telephone", telephoneTextField);
         builder.nextLine();
 
         builder.appendI15d("Label.Goal", goalTextField);
         builder.nextLine();
 
         builder.appendI15d("Label.PossibleAttendanceRate", possibleAttendanceRateTextField);
         builder.nextLine();
 
         builder.appendI15d("Label.HealthRestrictions", healthRestrictionsTextField);
         builder.nextLine();
 
         builder.appendI15d("Label.FitnessExperience", fitnessExperienceComboBox);
         builder.nextLine();
 
         builder.appendI15d("Label.SpecialWishes", specialWishesTextField);
         builder.nextLine();
 
         builder.appendI15d("Label.Weight", weightSpinner);
         builder.nextLine();
 
         builder.appendI15d("Label.Height", heightSpinner);
         builder.nextLine();
 
         builder.appendI15d("Label.AdSource", adSourceComboBox);
         builder.nextLine();
     }
 
     /**
      * Tries to save the form to the current profile.
      *
      * @return true, if the form is valid and has been saved
      */
  
     public boolean trySave() {
         for (Binding binding : bindingGroup.getBindings()) {
             binding.saveAndNotify();
         }
         return formBindingListener.getInvalidTargets().isEmpty();
     }
 
     /**
      * Sets current profile.
      *
      * @param profile the new profile
      */
     public void setClientProfile(ClientProfileDTO clientProfile) {
         this.clientProfile = clientProfile;
 
         if (bindingGroup == null) {
             bindingGroup = new BindingGroup();
             formBindingListener = new FormBindingListener();
 
             Binding binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("address"), addressTextField, BeanProperty.create("text"), "address"); //NOI18N
             bindingGroup.addBinding(binding);
 
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("telephone"), telephoneTextField, BeanProperty.create("text"), "telephone"); //NOI18N
             bindingGroup.addBinding(binding);
 
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("sex"), sexComboBox, BeanProperty.create("selectedItem"), "sex"); //NOI18N
             binding.setSourceNullValue(2);
             bindingGroup.addBinding(binding);
 
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("birthday"), birthdayTextField, BeanProperty.create("text"), "birthday"); //NOI18N
             binding.setConverter(new DateMidnightToStringConverter("date", "dd-MM-yyyy")); //NOI18N
             bindingGroup.addBinding(binding);
 
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("goal"), goalTextField, BeanProperty.create("text"), "goal"); //NOI18N
             bindingGroup.addBinding(binding);
 
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("possibleAttendanceRate"), possibleAttendanceRateTextField, BeanProperty.create("text"), "possibleAttendanceRate"); //NOI18N
             bindingGroup.addBinding(binding);
 
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("healthRestrictions"), healthRestrictionsTextField, BeanProperty.create("text"), "healthRestrictions"); //NOI18N
             bindingGroup.addBinding(binding);
 
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("specialWishes"), specialWishesTextField, BeanProperty.create("text"), "specialWishes"); //NOI18N
             bindingGroup.addBinding(binding);
 
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("favouriteSport"), favouriteSportTextField, BeanProperty.create("text"), "favouriteSport"); //NOI18N
             bindingGroup.addBinding(binding);
 
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("fitnessExperience"), fitnessExperienceComboBox, BeanProperty.create("selectedItem"), "fitnessExperience"); //NOI18N
             binding.setSourceNullValue(0);
             bindingGroup.addBinding(binding);
 
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("height"), heightSpinner, BeanProperty.create("value"), "height"); //NOI18N
             binding.setSourceNullValue(175);
             bindingGroup.addBinding(binding);
 
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("weight"), weightSpinner, BeanProperty.create("value"), "weight"); //NOI18N
             binding.setSourceNullValue(70);
             bindingGroup.addBinding(binding);
 
             binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_ONCE, clientProfile,
                     BeanProperty.create("adSourceId"), adSourceComboBox, BeanProperty.create("selectedItem"), "adSource"); //NOI18N
             binding.setSourceNullValue(0);
             binding.setConverter(new Converter<Integer, AdSourceDTO>() {
 
                 @Override
                 public AdSourceDTO convertForward(Integer value) {
                     for (AdSourceDTO adSource : adSources) {
                        if (adSource.getId().equals(value)) {
                             return adSource;
                         }
                     }
 
                     return null;
                 }
 
                 @Override
                 public Integer convertReverse(AdSourceDTO value) {
                     return value.getId();
                 }
             });
 
             bindingGroup.addBinding(binding);
 
             bindingGroup.addBindingListener(formBindingListener);
             bindingGroup.bind();
         } else {
 
             formBindingListener.getInvalidTargets().clear();
 
             /*
              * We take each binding and set the source object.
              */
             for (Binding binding : bindingGroup.getBindings()) {
                 binding.unbind();
                 binding.setSourceObject(clientProfile);
                 binding.bind();
             }
         }
     }
 
     /*
      * Business
      */
     private ClientProfileDTO clientProfile;
     private AdSourcesService adSourcesService;
     /*
      * Presentation
      */
     private List<AdSourceDTO> adSources;
     private BindingGroup bindingGroup;
     private FormBindingListener formBindingListener;
     private ResourceBundle bundle = ResourceBundle.getBundle("org/key2gym/presentation/resources/Strings");
     private JComboBox adSourceComboBox;
     private JTextField addressTextField;
     private JTextField birthdayTextField;
     private JTextField favouriteSportTextField;
     private JComboBox fitnessExperienceComboBox;
     private JTextField goalTextField;
     private JTextField healthRestrictionsTextField;
     private JSpinner heightSpinner;
     private JTextField possibleAttendanceRateTextField;
     private JComboBox sexComboBox;
     private JTextField specialWishesTextField;
     private JTextField telephoneTextField;
     private JSpinner weightSpinner;
 }
