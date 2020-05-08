 package com.objectgraph.gui.editors;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.scene.control.Button;
 import javafx.scene.control.TextField;
 
 import com.objectgraph.core.Event;
 import com.objectgraph.core.RootedProperty;
 import com.objectgraph.core.eventtypes.changes.SetProperty;
 import com.objectgraph.gui.EditorPane;
 
 public class StringEditor extends EditorPane {
 	
 	@FXML private TextField textField;
 	@FXML private Button commitButton;
 	@FXML private Button cancelButton;
 	
 	public StringEditor() {
 		super("StringEditorView.fxml");
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
 		// TODO different background color if change is not committed
 	}
 
 	@Override
 	public void updateModel() {
 		if (getModel() == null)
 			return;
 		getModel().setValue(textField.getText());
 	}
 
 	@Override
 	public void updateView() {
 		textField.setText(getModel().getValue(String.class));
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
 
 	@Override
 	public boolean canEdit(RootedProperty model) {
		return model.getValue() instanceof String;
 	}
 
 }
