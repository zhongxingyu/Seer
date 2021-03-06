 /*
  * Copyright 2002-2004 the original author or authors.
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
 package org.springframework.richclient.forms;
 
 import javax.swing.JComponent;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import org.springframework.richclient.application.Application;
 import org.springframework.richclient.factory.ComponentFactory;
 import org.springframework.richclient.util.GuiStandardUtils;
 
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * @author Keith Donald
  */
 public class JGoodiesBeanFormBuilder implements BeanFormBuilder {
     private static final String LABEL_PREFIX = "label";
 
     private SwingFormModel formModel;
 
     private JGoodiesFormBuilder formBuilder;
 
     private ComponentFactory componentFactory;
 
     public JGoodiesBeanFormBuilder(SwingFormModel formModel,
             FormLayout formLayout) {
         this(formModel, formLayout, Application.services()
                 .getComponentFactory());
     }
 
     public JGoodiesBeanFormBuilder(SwingFormModel formModel,
             FormLayout formLayout, ComponentFactory componentFactory) {
         this.formModel = formModel;
         this.formBuilder = new JGoodiesFormBuilder(formLayout, componentFactory);
         this.componentFactory = componentFactory;
     }
 
     public JGoodiesFormBuilder getWrappedFormBuilder() {
         return formBuilder;
     }
 
     public DefaultFormBuilder getDefaultFormBuilder() {
         return formBuilder.getDefaultFormBuilder();
     }
 
     public JComponent getForm() {
         return formBuilder.getForm();
     }
 
     public JComponent add(String propertyName) {
         JComponent propertyEditor = formModel.createBoundControl(propertyName);
         formBuilder.add(getPropertyLabelCode(propertyName), propertyEditor);
         return propertyEditor;
     }
 
     public JPasswordField addPasswordField(String propertyName) {
        return (JPasswordField)formModel.bind(new JPasswordField(8),
                 propertyName);
     }
 
     public JTextArea addTextArea(String propertyName) {
         JTextArea textArea = GuiStandardUtils.createStandardTextArea(5, 40);
         formBuilder.add(getPropertyLabelCode(propertyName), "left,top",
                 new JScrollPane(formModel.bind(textArea, propertyName)));
         return textArea;
     }
 
     private String getPropertyLabelCode(String propertyName) {
         return LABEL_PREFIX + "." + propertyName;
     }
 
 }
