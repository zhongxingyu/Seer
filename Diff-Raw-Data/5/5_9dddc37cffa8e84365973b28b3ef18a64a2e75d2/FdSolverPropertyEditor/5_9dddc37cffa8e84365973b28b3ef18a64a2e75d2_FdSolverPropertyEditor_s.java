 package org.eclipse.stem.solvers.fd.presentation;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.stem.core.solver.Solver;
 import org.eclipse.stem.solvers.fd.FdFactory;
 import org.eclipse.stem.solvers.fd.FdPackage;
 import org.eclipse.stem.ui.adapters.propertystrings.PropertyStringProvider;
 import org.eclipse.stem.ui.adapters.propertystrings.PropertyStringProviderAdapter;
 import org.eclipse.stem.ui.adapters.propertystrings.PropertyStringProviderAdapterFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 public class FdSolverPropertyEditor extends org.eclipse.stem.core.solver.SolverPropertyEditor {
 
 	/**
 	 * @param parent
 	 * @param style
 	 * @param diseaseModel
 	 * @param projectValidator
 	 */
 	public FdSolverPropertyEditor(Composite parent,
 			int style, Solver solver,
 			ModifyListener projectValidator) {
 		super(parent, style);
 		final GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		setLayout(gridLayout);
 
 		// Get the adapter that will provide NLS'd names for the
 		// properties of the disease model
 		final PropertyStringProviderAdapter pspa = (PropertyStringProviderAdapter) PropertyStringProviderAdapterFactory.INSTANCE
 				.adapt(solver, PropertyStringProvider.class);
 
 		final ComposedAdapterFactory itemProviderFactory = new ComposedAdapterFactory(
 				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 		
 		final IItemPropertySource propertySource = (IItemPropertySource) itemProviderFactory
 				.adapt(solver, IItemPropertySource.class);
 		
 		final List<IItemPropertyDescriptor> properties = propertySource
 				.getPropertyDescriptors(solver);
 
 		for (final IItemPropertyDescriptor descriptor : properties) {
 			final EStructuralFeature feature = (EStructuralFeature) descriptor
 					.getFeature(null);
 			// Is this a disease model property that the user should specify?
 			if (isUserSpecifiedSolverProperty(feature)) {
 				// Yes
 				final Label label = new Label(this, SWT.NONE);
 				label.setText(pspa.getPropertyName(descriptor));
 
 				final GridData labelGD = new GridData(GridData.BEGINNING);
 				labelGD.grabExcessHorizontalSpace = true;
 				labelGD.horizontalAlignment = SWT.FILL;
 				labelGD.horizontalIndent = 0;
 				label.setLayoutData(labelGD);
 
 				// Get a string value for the default value of the feature
 
 				final String defaultValueString = getPropertyDefaultValueString(descriptor);
 
 				final Text text = new Text(this, SWT.BORDER | SWT.TRAIL);
 				text.setText(defaultValueString);
 				text.setToolTipText(pspa.getPropertyToolTip(descriptor));
 				map.put(feature, text);
 
 				final GridData textGD = new GridData(GridData.END);
 				textGD.grabExcessHorizontalSpace = true;
 				textGD.horizontalAlignment = SWT.FILL;
 				text.setLayoutData(textGD);
 
 				text.addModifyListener(projectValidator);
 
 				final Label unitLabel = new Label(this, SWT.NONE);
 				unitLabel.setText(pspa.getPropertyUnits(descriptor));
 				final GridData unitLabelGD = new GridData(GridData.END);
 				unitLabelGD.verticalAlignment = GridData.CENTER;
 				unitLabel.setLayoutData(unitLabelGD);
 
 			} // if user specified
 		} // for each solver property
 	}
 	
 	/**
 	 * @param feature
 	 * @return <code>true</code> if the feature is a dublin core feature 
 	 * that is specified by a user.
 	 */
 	public static boolean isUserSpecifiedSolverProperty(final EStructuralFeature feature) {
 		boolean retValue = false;
 		final EClass containingClass = feature.getEContainingClass();
 		// Is it a disease model property?
 		if (containingClass.equals(FdPackage.eINSTANCE.getFiniteDifference())) {
 			retValue = true;
 		} // if a disease model property
 		return retValue;
 	} // isUserSpecifiedDiseaseModelProperty
 
 	protected final Map<EStructuralFeature, Text> map = new HashMap<EStructuralFeature, Text>();
 	protected String errorMessage;
 
 	public FdSolverPropertyEditor(Composite parent, int style) {
 		super(parent,style);
 	}
 
 	/**
 	 * @see org.eclipse.stem.ui.wizards.StandardDiseaseModelPropertyEditor#populate(org.eclipse.stem.diseasemodels.standard.DiseaseModel)
 	 */
 	@Override
 	public void populate(Solver solver) {
 		super.populate(solver);
 		for (final Map.Entry<EStructuralFeature, Text> entry : map.entrySet()) {
 		
 			switch (entry.getKey().getFeatureID()) {
 			default:
 				break;
 			} // switch
 		} // for each Map.entry
 	}
 
 	@Override
 	public boolean validate() {
 		boolean retValue = true;
 		return retValue;
 	} // validate
 
 	
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents sub-classing of SWT components
 	}
 
 	@Override
 	public Solver createAndPopulateSolver() {
 		Solver retValue = FdFactory.eINSTANCE.createFiniteDifference();
 		this.populate(retValue);
 		return retValue;
 	}
 
 
 } // RkSolverPropertyEditor
