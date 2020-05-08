 /*
  * Copyright 2013 Emanuele Tamponi
  *
  * This file is part of object-graph.
  *
  * object-graph is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * object-graph is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.objectgraph.gui.editors;
 
 import com.objectgraph.core.Event;
 import com.objectgraph.core.eventtypes.changes.SetProperty;
 import com.objectgraph.gui.PropertyEditor;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.scene.control.Button;
 import javafx.scene.control.TextField;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 
 public abstract class TextFieldBasedPropertyEditor<T> extends PropertyEditor {
 
     @FXML
     protected TextField textField;
     @FXML
     protected Button commitButton;
     @FXML
     protected Button cancelButton;
 
     public TextFieldBasedPropertyEditor() {
         super("TextFieldBasedEditorView.fxml");
     }
 
     @Override
     public void initialize(URL location, ResourceBundle resources) {
         commitButton.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent event) {
                 updateModel();
             }
         });
         cancelButton.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent event) {
                 updateView();
             }
         });
         textField.textProperty().addListener(new ChangeListener<String>() {
             @Override
             public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                 updateStyle(newValue);
             }
         });
     }
 
     private void updateStyle(String text) {
         textField.getStyleClass().removeAll("text-field-editor-changed", "text-field-editor-invalid", "text-field-editor");
         if (getModel() != null) {
             if (isValid(text)) {
                if (!getModel().getErrors().isEmpty()) {
                     textField.getStyleClass().add("text-field-editor-invalid");
                 } else {
                     String curValue = fromModelToText((T) getModel().getValue());
                     if (!text.equals(curValue))
                         textField.getStyleClass().add("text-field-editor-changed");
                     else
                         textField.getStyleClass().add("text-field-editor");
                 }
             } else {
                 textField.getStyleClass().add("text-field-editor-invalid");
             }
         }
     }
 
     @Override
     public void updateModel() {
         if (getModel() != null && isValid(textField.getText())) {
             getModel().setValue(fromTextToModel(textField.getText()));
             updateStyle(textField.getText());
         }
     }
 
     protected abstract T fromTextToModel(String text);
 
     protected abstract String fromModelToText(T value);
 
     protected abstract boolean isValid(String text);
 
     @Override
     public void updateView() {
         if (getModel() != null) {
             textField.setText(fromModelToText((T) getModel().getValue()));
         }
     }
 
     @Override
     public boolean requiresViewUpdate(Event event) {
         if (event.getType() instanceof SetProperty) {
             if (event.getPath().equals(getModel().getPath())) {
                 if (!event.getType(SetProperty.class).getNewValue().equals(textField.getText()))
                     return true;
             }
         }
         return false;
     }
 
 }
