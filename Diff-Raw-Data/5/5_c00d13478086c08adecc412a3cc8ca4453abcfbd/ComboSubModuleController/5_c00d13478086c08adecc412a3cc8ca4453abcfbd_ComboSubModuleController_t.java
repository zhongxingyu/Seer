 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.example.client.controllers;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.beans.common.PersonFactory;
 import org.eclipse.riena.beans.common.PersonManager;
 import org.eclipse.riena.example.client.views.ComboSubModuleView;
 import org.eclipse.riena.internal.example.client.beans.PersonModificationBean;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IComboRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 
 /**
  * Controller for the {@link ComboSubModuleView} example.
  */
 public class ComboSubModuleController extends SubModuleController {
 
 	/** Manages a collection of persons. */
 	private final PersonManager manager;
 	/** Holds editable data for a person. */
 	private final PersonModificationBean value;
 	private IComboRidget comboOne;
 	private ITextRidget textFirst;
 	private ITextRidget textLast;
 
 	public ComboSubModuleController() {
 		this(null);
 	}
 
 	public ComboSubModuleController(ISubModuleNode navigationNode) {
 		super(navigationNode);
 		manager = new PersonManager(PersonFactory.createPersonList());
 		manager.setSelectedPerson(manager.getPersons().iterator().next());
 		value = new PersonModificationBean();
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.controllers.SubModuleController#afterBind()
 	 */
 	@Override
 	public void afterBind() {
 		super.afterBind();
 		bindModels();
 	}
 
 	private void bindModels() {
 		comboOne.bindToModel(manager, "persons", String.class, null, manager, "selectedPerson"); //$NON-NLS-1$ //$NON-NLS-2$
 		comboOne.updateFromModel();
 
 		textFirst.bindToModel(value, "firstName"); //$NON-NLS-1$
 		textFirst.updateFromModel();
 
 		textLast.bindToModel(value, "lastName"); //$NON-NLS-1$
 		textLast.updateFromModel();
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.IRidgetContainer#configureRidgets()
 	 */
 	@Override
 	public void configureRidgets() {
 
 		comboOne = (IComboRidget) getRidget("comboOne"); //$NON-NLS-1$
 
 		value.setPerson(manager.getSelectedPerson());
 
 		textFirst = (ITextRidget) getRidget("textFirst"); //$NON-NLS-1$
 		textLast = (ITextRidget) getRidget("textLast"); //$NON-NLS-1$
 
 		comboOne.addPropertyChangeListener(IComboRidget.PROPERTY_SELECTION, new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
				Person selectedPerson = (Person) evt.getNewValue();
				value.setPerson(selectedPerson);
 				textFirst.updateFromModel();
 				textLast.updateFromModel();
 			}
 		});
 
 		final IActionRidget buttonSave = (IActionRidget) getRidget("buttonSave"); //$NON-NLS-1$
 		buttonSave.setText("&Save"); //$NON-NLS-1$
 		buttonSave.addListener(new IActionListener() {
 			public void callback() {
 				value.update();
 				comboOne.updateFromModel();
 			}
 		});
 	}
 }
