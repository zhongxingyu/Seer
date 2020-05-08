 /*******************************************************************************
  * Copyright (c) 2012 Emanuele Tamponi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele Tamponi - initial API and implementation
  ******************************************************************************/
 package game.plugins.editors.configurablelist;
 
 import game.configuration.Change;
 import game.configuration.ConfigurableList;
 import game.editorsystem.ControlledEditor;
 
 import java.util.Observable;
 
 import javafx.scene.Node;
 import javafx.scene.layout.GridPane;
 import javafx.scene.layout.Priority;
 
 public class ConfigurableListEditor extends ControlledEditor {
 	
 	@Override
 	public Node getView() {
 		Node view = super.getView();
 		GridPane.setVgrow(view, Priority.SOMETIMES);
 		return view;
 	}
 
 	@Override
 	public Class getBaseEditableClass() {
 		return ConfigurableList.class;
 	}
 
 	@Override
 	public boolean isInline() {
 		return false;
 	}
 
 	@Override
 	public void update(Observable observed, Object m) {
		super.update(observed, m);
 		if (m instanceof Change) {
			Change change = (Change)m;
			if (change.getPath().matches(getModel().getOptionName() + "\\.\\d+")
					|| change.getPath().matches(getModel().getOptionName() + "\\.\\d+\\.name")) {
				if (change.getSetter() != this)
					updateView();
			}
 		}
 	}
 
 }
