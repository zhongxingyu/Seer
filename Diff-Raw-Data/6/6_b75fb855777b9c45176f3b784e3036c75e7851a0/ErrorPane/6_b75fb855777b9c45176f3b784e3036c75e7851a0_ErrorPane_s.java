 package org.pillarone.riskanalytics.application.ui.parameterization.view;
 
 import com.ulcjava.base.application.*;
 import com.ulcjava.base.application.util.Color;
 import org.pillarone.riskanalytics.application.ui.parameterization.model.ParameterViewModel;
import org.pillarone.riskanalytics.core.parameterization.ParameterValidationError;
 
 import java.util.Collection;
 
 public class ErrorPane {
 
     private ULCBoxPane content;
     private ParameterViewModel model;
 
     public ErrorPane(ParameterViewModel model) {
         this.model = model;
         content = new ULCBoxPane(1, 0);
     }
 
     public void addError(ParameterValidationError error) {
         content.add(ULCBoxPane.BOX_EXPAND_TOP, createLabel(error));
     }
 
     public void addErrors(Collection<ParameterValidationError> errors) {
         for (ParameterValidationError error : errors) {
             addError(error);
         }
     }
 
     public void clear() {
         content.removeAll();
     }
 
     private ULCComponent createLabel(ParameterValidationError error) {
         ULCBoxPane pane = new ULCBoxPane(0, 2);
         pane.add(ULCBoxPane.BOX_LEFT_CENTER, new ULCLabel("Path:" + model.findNodeForPath(error.getPath()).getDisplayPath()));
         pane.add(ULCBoxPane.BOX_EXPAND_CENTER, ULCFiller.createHorizontalGlue());
 
         ULCLabel label = new ULCLabel();
         label.setForeground(Color.red);
        label.setText(error.getMsg());
 
         pane.add(ULCBoxPane.BOX_LEFT_CENTER, label);
         return pane;
     }
 
     public ULCBoxPane getContent() {
         return content;
     }
 }
