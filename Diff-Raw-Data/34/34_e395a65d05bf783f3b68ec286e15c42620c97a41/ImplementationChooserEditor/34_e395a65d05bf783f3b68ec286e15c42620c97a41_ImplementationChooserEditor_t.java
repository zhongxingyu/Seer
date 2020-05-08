 package com.objectgraph.gui.editors;
 
 import java.net.URL;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.scene.control.Button;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.ListCell;
 import javafx.scene.control.ListView;
 import javafx.util.Callback;
 
 import com.objectgraph.core.Event;
 import com.objectgraph.core.RootedProperty;
 import com.objectgraph.core.eventtypes.changes.SetProperty;
 import com.objectgraph.gui.EditorPane;
 import com.objectgraph.utils.ClassUtils;
 
 public class ImplementationChooserEditor extends EditorPane {
 	
 	@FXML private ComboBox<Object> implementationBox;
 	@FXML private Button commitButton, cancelButton;
 	@FXML private Button editButton;
 	@FXML private Button loadButton;
 	
 	private List<?> implementations;
 	private static final Object NULLCONTENT = new Object();
 	
 	private class ImplementationListCell extends ListCell<Object> {
 		@Override
 		protected void updateItem(Object item, boolean empty) {
 			super.updateItem(item, empty);
 			if (item == NULLCONTENT || item == null || empty) {
 				setText("<null>");
 				item = null;
 			} else
 				setText(item.getClass().getSimpleName());
 			
 			if (getModel().getValue() == item)
 				setText(getText() + " (current)");
 		}
 	}
 	
 	public ImplementationChooserEditor() {
 		super("ImplementationChooserEditorView.fxml");
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
 		
 		loadButton.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent event) {
 				// TODO loadButton
 			}
 		});
 		
 		editButton.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent event) {
 				// TODO editButton
 			}
 		});
 		
 		implementationBox.setCellFactory(new Callback<ListView<Object>, ListCell<Object>>() {
 			@Override
 			public ListCell<Object> call(ListView<Object> list) {
 				return new ImplementationListCell();
 			}
 		});
 		
 		implementationBox.setButtonCell(new ImplementationListCell());
 	}
 
 	@Override
 	public void updateModel() {
 		if (getModel() != null) {
 			Object newValue = implementationBox.getValue();
 			if (newValue == NULLCONTENT)
 				getModel().setValue(null);
 			else
 				getModel().setValue(newValue);
 		}
 	}
 
 	@Override
 	public void updateView() {
 		if (getModel() != null) {
 			implementations = getModel().getPossibleValues();
 			implementationBox.getItems().clear();
 			Object current = getModel().getValue();
 			if (current != null)
 				implementationBox.getItems().add(getModel().getValue());
 			implementationBox.getItems().add(NULLCONTENT);
 			implementationBox.getItems().addAll(implementations);
 			implementationBox.getSelectionModel().selectFirst();
 		}
 	}
 
 	@Override
 	public boolean requiresViewUpdate(Event event) {
 		if (event.getType() instanceof SetProperty) {
 			if (event.getPath().equals(getModel().getPath())) {
 				if (implementationBox.getSelectionModel().getSelectedIndex() != 0)
 					return true;
 				Object currentInView = implementationBox.getValue();
 				currentInView = currentInView == NULLCONTENT ? null : currentInView;
 				Object currentInModel = getModel().getValue();
 				if (currentInView != currentInModel)
 					return true;
 			}
 		}
 		List<?> possibleValues = getModel().getPossibleValues();
         if (possibleValues.size() != implementations.size())
             return true;
 		for(int i = 0; i < possibleValues.size(); i++) {
 			if (!possibleValues.get(i).getClass().equals(implementations.get(i).getClass()))
 				return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean canEdit(RootedProperty model) {
		return !ClassUtils.isConcrete(model.getValueType(false));
 	}
 
 }
