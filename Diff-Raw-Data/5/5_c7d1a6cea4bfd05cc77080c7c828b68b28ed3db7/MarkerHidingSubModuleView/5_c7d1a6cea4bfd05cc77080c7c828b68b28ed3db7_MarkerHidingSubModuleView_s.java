 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.example.client.views;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Example for showing / hiding markers.
  * 
  * @see IMarkableRidget
  */
 public class MarkerHidingSubModuleView extends SubModuleView {
 
 	public static final String ID = MarkerHidingSubModuleView.class.getName();
 
 	@Override
 	protected void basicCreatePartControl(final Composite parent) {
 		parent.setBackground(LnfManager.getLnf().getColor(LnfKeyConstants.SUB_MODULE_BACKGROUND));
 		parent.setLayout(new GridLayout(1, false));
 
 		final Group group1 = createGroup(parent);
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(group1);
 	}
 
 	// helping methods
 	// ////////////////
 
 	private Group createGroup(final Composite parent) {
 		final Group group = UIControlsFactory.createGroup(parent, "Marker Options:"); //$NON-NLS-1$
 		group.setLayout(createGridLayout(3));
 
 		UIControlsFactory.createLabel(group, "ITextRidget:"); //$NON-NLS-1$
 		final Text text = UIControlsFactory.createText(group, SWT.BORDER, "textRidget"); //$NON-NLS-1$
 		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(text);
 
 		UIControlsFactory.createLabel(group, ""); //$NON-NLS-1$
 		final Button btnMandatory = UIControlsFactory.createButton(group, "Add/Remove Mandatory", "btnMandatory"); //$NON-NLS-1$ //$NON-NLS-2$
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(btnMandatory);
 		final Button btnError = UIControlsFactory.createButton(group, "Add/Remove ErrorMarker", "btnError"); //$NON-NLS-1$ //$NON-NLS-2$
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(btnError);
 
 		UIControlsFactory.createLabel(group, ""); //$NON-NLS-1$
		final Button btnHM = UIControlsFactory.createButton(group, "Show/Hide ErrorMarker ", "btnHideMandatory"); //$NON-NLS-1$ //$NON-NLS-2$
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(btnHM);
		final Button btnHE = UIControlsFactory.createButton(group, "Show/Hide MandatoryMarker", "btnHideError"); //$NON-NLS-1$ //$NON-NLS-2$
 		GridDataFactory.fillDefaults().grab(true, false).applyTo(btnHE);
 
 		return group;
 	}
 
 	private GridLayout createGridLayout(final int numColumns) {
 		final GridLayout layout = new GridLayout(numColumns, false);
 		layout.marginWidth = 20;
 		layout.marginHeight = 20;
 		return layout;
 	}
 
 }
