 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Eclipse
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.eclipse.ui.dialogs;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.*;
 
 import de.tuilmenau.ics.fog.eclipse.ui.PropertyGUIFactoryContainer;
 import de.tuilmenau.ics.fog.eclipse.ui.PropertyParameterWidget;
 import de.tuilmenau.ics.fog.eclipse.widget.EmptyPropertyParameterWidget;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.properties.CommunicationTypeProperty;
 import de.tuilmenau.ics.fog.facade.properties.DatarateProperty;
 import de.tuilmenau.ics.fog.facade.properties.DelayProperty;
 import de.tuilmenau.ics.fog.facade.properties.Property;
 import de.tuilmenau.ics.fog.facade.properties.LossRateProperty;
 import de.tuilmenau.ics.fog.facade.properties.OrderedProperty;
 import de.tuilmenau.ics.fog.facade.properties.PriorityProperty;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.facade.properties.PropertyFactoryContainer;
 import de.tuilmenau.ics.fog.facade.properties.TransportProperty;
 import de.tuilmenau.ics.fog.facade.properties.MinMaxProperty.Limit;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 
 public class SelectRequirementsDialog extends Dialog
 {
 	Description mSelectedRequirements;
 	LinkedList<Button> mBtPerProperty = new LinkedList<Button>();
 	LinkedList<PropertyParameterWidget> mPwPerProperty= new LinkedList<PropertyParameterWidget>();
 	
 	private class ModificationListener implements Listener {
 		public ModificationListener(Button pButton) {
 			mButton = pButton;
 		}
 		
 		@Override
 		public void handleEvent(Event event) {
 			mButton.setSelection(true);		
 			event.doit = true; // TODO do we need that?
 		}
 		
 		private Button mButton;
     }
 	
 	public SelectRequirementsDialog(Shell pParent) {
 		// create modal dialog
 		super(pParent);
 	}
 	
 	/**
 	 * Used by callers, which are not using the SWT thread.
 	 */
 	public static Description open(Shell pParent, String pServerName, Description pCapabilities, Description pSelectedRequirements) {
 		Display tDisplay = pParent.getDisplay();
 		
 		if (tDisplay.getThread() != Thread.currentThread())
 		{
			SelectRequirementsDialogThread tDialogThread = new SelectRequirementsDialogThread(pParent, pServerName, pCapabilities, pSelectedRequirements);
			tDisplay.syncExec(tDialogThread);
			return tDialogThread.getSelectedRequirements();
 		}else
 		{
 			SelectRequirementsDialog tDialog = new SelectRequirementsDialog(pParent);
 			tDialog.open(pServerName, pCapabilities, pSelectedRequirements);
 			return tDialog.getSelectedRequirements();
 		}
 	}
 
 	/**
 	 * Shows and executes dialogue.
 	 * 
 	 * @param pServerName Describes the target for the connection.
 	 * @param pCapabilities Describes the set of allowed properties. If "null" is given, every property (including these from the factory) is allowed.
 	 * @param pSelectedRequirements Describes a set of preselected requirements, which should be selected in the dialog.
 	 */
 	public void open(String pServerName, Description pCapabilities, Description pSelectedRequirements) {
 		// Dialog's shell
 		final Shell tShell = new Shell(getParent(), SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
 		if(pServerName != null)	{
 			tShell.setText("Select requirements for connection to " + pServerName + " and allowed requirements" + pSelectedRequirements);
 		}else {
 			tShell.setText("Select requirements");
 		}
 		if(pSelectedRequirements != null)	{
 			Logging.trace(this, "Pre-selected requirements: " + pSelectedRequirements);
 		}
 		tShell.setLayout(new GridLayout(2, true));
 		
 		final GridData tHorizontalFill = new GridData(SWT.FILL, SWT.FILL, false, false);
 
 		// 
 		// type of communication
 		//
 		new Label(tShell, SWT.NONE).setText("Type of communication");
 		final Combo tTypeCombo = new Combo(tShell, SWT.READ_ONLY);
 		tTypeCombo.add(CommunicationTypeProperty.DATAGRAM.toString());
 		tTypeCombo.add(CommunicationTypeProperty.DATAGRAM_STREAM.toString());
 		tTypeCombo.add(CommunicationTypeProperty.STREAM.toString());
 		Property tProp = getProp(pSelectedRequirements, CommunicationTypeProperty.class);
 		if(tProp == null) {
 			tProp = CommunicationTypeProperty.getDefault();
 		}
 		tTypeCombo.setText(tProp.toString());
 		
 		//
 		// non functional
 		//
 		
 		// grouping
 		final GridData tNonFuncGroupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
 		tNonFuncGroupLayoutData.horizontalSpan = 2;
 		
 		Group tGrpNonFunctional = new Group(tShell, SWT.SHADOW_OUT);
 		tGrpNonFunctional.setText("Non-Functional");
 		tGrpNonFunctional.setLayout(new GridLayout(2, true));
 	    tGrpNonFunctional.setLayoutData(tNonFuncGroupLayoutData);
 			
 		/**
 		 * ordered
 		 */
 		final Button tBtOrdered = createButton(tGrpNonFunctional,
 				"packet ordering",
 				null,
 				isEnabled(pCapabilities, OrderedProperty.class),
 				getProp(pSelectedRequirements, OrderedProperty.class) != null,
 				false);
 
 	    /**
 		 * delay
 		 */
 		int tDelayCurrent = DelayProperty.DefaultMaxValueMSec;
 		DelayProperty tDelayProp = (DelayProperty) getProp(pSelectedRequirements, DelayProperty.class);
 		if (tDelayProp != null)
 		{
 			tDelayCurrent = tDelayProp.getMax();
 		}
 		final Button tBtDelay = createButton(tGrpNonFunctional,
 				"max. delay [ms]:",
 				null,
 				isEnabled(pCapabilities, DelayProperty.class),
 				tDelayProp != null,
 				true);
 		final Spinner tSpDelay = createSpinner(tGrpNonFunctional, tBtDelay, 0, tDelayCurrent, 1000, 10, 50);
 		
 		/**
 		 * data rate
 		 */
 		int tDRCurrent = 64;
 		DatarateProperty tDataRateProp = (DatarateProperty) getProp(pSelectedRequirements, DatarateProperty.class);				
 		if (tDataRateProp != null)
 		{
 			tDRCurrent = tDataRateProp.getMin();
 		}
 		final Button tBtDataRate = createButton(tGrpNonFunctional,
 				"min. data rate [Kb/s]:",
 				null,
 				isEnabled(pCapabilities, DatarateProperty.class),
 				tDataRateProp != null,
 				true);
 
 		Composite comp = new Composite(tGrpNonFunctional, SWT.NONE);
 	    GridLayout gridLayout = new GridLayout(3, false);
 	    comp.setLayout(gridLayout);
 	    
 		final Spinner tSpDataRate = createSpinner(comp, tBtDataRate, 0, tDRCurrent, 1000, 10, 50);
 		new Label(comp, SWT.NONE).setText("Minimal required [%]:");
 		final Spinner tSpDataRateVariance = createSpinner(comp, tBtDataRate, 0, 100, 100, 5, 10);
 		
 		/**
 		 * loss rate
 		 */
 		int tLRCurrent = 0;
 		LossRateProperty tLossRate = (LossRateProperty) getProp(pSelectedRequirements, LossRateProperty.class);				
 		if (tLossRate != null)
 		{
 			tLRCurrent = tLossRate.getLossRate();
 		}
 		final Button  tBtLossRate = createButton(tGrpNonFunctional,
 				"max. loss rate [%]:",
 				"auto. retrans.",
 				isEnabled(pCapabilities, LossRateProperty.class),
 				tLossRate != null,
 				true);
 		final Spinner tSpLossRate = createSpinner(tGrpNonFunctional, tBtLossRate, 0, tLRCurrent, 100, 1, 5);
 		
 		/**
 		 * priority
 		 */
 		final Button tBtPrio = createButton(tGrpNonFunctional,
 				"priority",
 				null,
 				isEnabled(pCapabilities, PriorityProperty.class),
 				getProp(pSelectedRequirements, PriorityProperty.class) != null,
 				false);
 
 		/**
 		 * additional non-functional properties from extensions
 		 */
 		addPropertyExtensions(pCapabilities, pSelectedRequirements,	tGrpNonFunctional, true);
 		
 
 		// 
 		// functional
 		// 
 
 		// grouping
 		final GridData tFuncGroupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		tFuncGroupLayoutData.horizontalSpan = 2;
 		
 		Group tGrpFunctional = new Group(tShell, SWT.SHADOW_OUT);
 		tGrpFunctional.setText("Functional");
 		tGrpFunctional.setLayout(new GridLayout(2, false));
 	    tGrpFunctional.setLayoutData(tFuncGroupLayoutData);
 		    
 		/**
 		 * simple TCP
 		 */
 		final Button tBtTcp = createButton(tGrpFunctional,
 				"in order and loss free",
 				"simple TCP",
 				isEnabled(pCapabilities, TransportProperty.class),
 				getProp(pSelectedRequirements, TransportProperty.class) != null,
 				false); 
 
 		/**
 		 * additional functional properties from factory
 		 */
 		addPropertyExtensions(pCapabilities, pSelectedRequirements,	tGrpFunctional, false);
 		
 		/**
 		 * some fancy buttons
 		 */
 	    final Button tButtonOk = new Button(tShell, SWT.PUSH);
 	    tButtonOk.setText("Ok");
 	    tButtonOk.setLayoutData(tHorizontalFill);
 
 	    Button tButtonCancel = new Button(tShell, SWT.PUSH);
 	    tButtonCancel.setText("Cancel");
 	    tButtonCancel.setLayoutData(tHorizontalFill);
 	    	    
 	    tButtonOk.addListener(SWT.Selection, new Listener() {
 
 	    	@Override
 	    	public void handleEvent(Event event) {
 	    		mSelectedRequirements = new Description();
 
 	    		/**
 	    		 *  Time to conclude the requirements from the values from the dialogue elements.
 	    		 *  The order of the properties in the resulting requirements description influences 
 	    		 *  directly the order during gate creation.
 	    		 */
 	    		// type of communication
 	    		CommunicationTypeProperty commType = CommunicationTypeProperty.getType(tTypeCombo.getText());
 	    		if (commType != null) {
 	    			mSelectedRequirements.set(commType);
 	    		}
 	    		
 	    		// non-functional requirements
 	    		if (tBtOrdered.getSelection())
 	    			mSelectedRequirements.set(new OrderedProperty(true));
 	    		if (tBtDelay.getSelection())
 	    			mSelectedRequirements.set(new DelayProperty(tSpDelay.getSelection(), Limit.MAX));
 	    		if (tBtDataRate.getSelection()) {
 	    			double tPercentageMinimal = (double)tSpDataRateVariance.getSelection() / 100.0d;
 	    			int tMaximalDateRate = tSpDataRate.getSelection();
 	    			
 	    			mSelectedRequirements.set(DatarateProperty.createSoftRequirement(tMaximalDateRate, tPercentageMinimal));
 	    		}
 	    		if (tBtLossRate.getSelection())
 	    			mSelectedRequirements.set(new LossRateProperty(tSpLossRate.getSelection(), Limit.MAX));
 	    		if (tBtPrio.getSelection())
 	    			mSelectedRequirements.set(new PriorityProperty(1));
 	    			
 	    		// functional requirements
 	    		if (tBtTcp.getSelection())
 	    			mSelectedRequirements.set(new TransportProperty(true, false));
 	    		
 	    		/**
 	    		 * additional properties from factory
 	    		 */
 	    		PropertyFactoryContainer tPropsFactory = PropertyFactoryContainer.getInstance();
 	    		Iterator<PropertyParameterWidget> tPropPWIt = mPwPerProperty.iterator();
 	    		for(Button tBtProp : mBtPerProperty)
 	    		{
 	    			// add a property of the buttons name (corresponds to the names within the prop. factory)
 	    			if(tBtProp.getSelection())
 	    			{
 	    				if (!tPropPWIt.hasNext())
 	    				{
 	    					Logging.err(this, "Data inconsistency");
 	    					continue;
 	    				}
 	    				PropertyParameterWidget tPropPW = tPropPWIt.next();
 		    			Property tFactProp;
 		    			String tNameProp = tBtProp.getText();
 		    			Logging.trace(this, "Adding to selected requ. a factory based property " + tNameProp + " with parameters \"" + tPropPW.getPropertyParameters() + "\"");
 		    			try {
 		    				tFactProp = tPropsFactory.createProperty(tNameProp, tPropPW.getPropertyParameters());
 		    			} catch (PropertyException tPropExc) {
 		    				Logging.warn(this, "Failed to create property class for property name " + tNameProp, tPropExc);
 		    				continue;
 		    			}
 
 		    			mSelectedRequirements.set(tFactProp);	    				
 	    			}else
 	    				tPropPWIt.next();
 	    		}
 	    		Logging.log(this, "User selected the following requirements: " + mSelectedRequirements.toString());
 	    		tShell.dispose();
 	    	}
 	    });
 
 	    tButtonCancel.addListener(SWT.Selection, new Listener() {
 	    	public void handleEvent(Event event) {
     			mSelectedRequirements = null;
 	    		tShell.dispose();
 	    	}
 	    });
 	    
 	    tShell.addListener(SWT.Traverse, new Listener() {
 	      public void handleEvent(Event pEvent) {
 	        if (pEvent.detail == SWT.TRAVERSE_ESCAPE)
 	          pEvent.doit = false;
 	      }
 	    });
 	    
 	    tShell.pack();
 	    tShell.open();
 	    
 	    // fire up the dialogue and wait for user
 	    Display tDisplay = getParent().getDisplay();
 	    while (!tShell.isDisposed()){
 	    	try{
 		    	if (!tDisplay.readAndDispatch())
 		    		tDisplay.sleep();			
 	    	}catch(Exception tExc) {
 				Logging.warn(this, "Error in dialogue occurred", tExc);
 			}
 		}
 	}
 
 	/**
 	 * Adds requirements from the extensions to the GUI.
 	 * 
 	 * @param pCapabilities Available properties
 	 * @param pSelectedRequirements Selected properties
 	 * @param pGroup GUI group
 	 * @param nonfunctional If it should add functional or non-functional properties.
 	 */
 	private void addPropertyExtensions(Description pCapabilities, Description pSelectedRequirements, Group pGroup, boolean nonfunctional)
 	{
 		PropertyFactoryContainer tPropsFactory = PropertyFactoryContainer.getInstance();
 		Iterable<String> tNamesFactProps = tPropsFactory.getRequirementNames(nonfunctional);
 		Logging.info(this, "Creating selectRequ. widgets for requirements " +tNamesFactProps);
 
 		for(String tNameProp : tNamesFactProps) {
 			String tToolTip = tPropsFactory.getDescription(tNameProp);
 			Property tSelectedProp = getProp(pSelectedRequirements, tNameProp);
 			
 			Button tBtPropActivation = createButton(pGroup,
 					tNameProp,
 					tToolTip,
 					(pCapabilities == null) || (getProp(pCapabilities, tNameProp) != null),
 					(tSelectedProp != null),
 					true);
 			
 			mBtPerProperty.addLast(tBtPropActivation);
 			
 			// Parameter Input Widget
 			try {
 				PropertyParameterWidget tWidget = PropertyGUIFactoryContainer.getInstance().createParameterWidget(tNameProp, tSelectedProp, pGroup, SWT.SHADOW_OUT);
 				Logging.info(this, "Creating selectRequ. widget for requirement " +tNameProp + "/" + tSelectedProp +" with factory resulted in a widget " + tWidget);
 
 				if(tWidget != null) {
 					// dis/enable like button
 					tWidget.setEnabled(tBtPropActivation.isEnabled());
 					
 					// if it is modified, enable button
 					tWidget.addListener(SWT.Verify, new ModificationListener(tBtPropActivation));
 				} else {
 					tWidget = new EmptyPropertyParameterWidget(pGroup, SWT.SHADOW_OUT);
 				}
 				
 				mPwPerProperty.addLast(tWidget);
 			}
 			catch (PropertyException tExc) {
 				Logging.err(this, "Got exception when creating property parameter widget for " + tNameProp);
 				
 				// create a label in order to insert something in the 2. column in the layout 
 				new Label(pGroup, SWT.NONE).setText(tExc.getLocalizedMessage());
 				continue;
 			}
 		}
 	}
 	
 	public Description getSelectedRequirements() {
 		return mSelectedRequirements;
 	}
 	
 	private boolean isEnabled(Description pRequirements, Class<?> pRequirementClass) {
 		if(pRequirements == null)
 		{
 			return true;
 		}else
 		{
 			return (getProp(pRequirements, pRequirementClass) != null);
 		}
 	}
 	
 	private Property getProp(Description pRequirements, Class<?> pRequirementClass) {
 		if(pRequirements != null)
 		{
 			return pRequirements.get(pRequirementClass);
 		}
 		
 		return null;
 	}
 	
 	private Property getProp(Description pRequirements, String pRequirementType) {
 		if(pRequirements != null)
 		{
 			return pRequirements.get(pRequirementType);
 		}
 		
 		return null;
 	}
 	
 	private Button createButton(Composite pParent, String pText, String pToolTipText, boolean pEnabled, boolean pSelected, boolean pHasWidget) {
 		Button tButton = new Button(pParent, SWT.CHECK);
 		tButton.setText(pText);
 		if(pToolTipText != null)
 		{
 			tButton.setToolTipText(pToolTipText);
 		}
 		
 		tButton.setEnabled(pEnabled);
 		tButton.setSelection(pEnabled && pSelected);
 		
 		if(!pHasWidget)
 		{
 			GridData tGridData = new GridData();
 		    tGridData.horizontalSpan = 2;
 		    tButton.setLayoutData(tGridData);
 		}
 		
 		return tButton;
 	}
 	
 	private Spinner createSpinner(Composite pParent, Button pEnableButton, int pMin, int pCurrent, int pMax, int pInc, int pPageInc) {
 		Spinner tSpinner = new Spinner(pParent, SWT.BORDER);
 		tSpinner.setMinimum(pMin);
 		tSpinner.setSelection(pCurrent);
 		tSpinner.setMaximum(pMax);
 		tSpinner.setIncrement(pInc);
 		tSpinner.setPageIncrement(pPageInc);
 		
 		linkToButton(tSpinner, pEnableButton);
 		return tSpinner;
 	}
 	
 	private void linkToButton(Control pElement, Button pEnableButton) {
 		if(pEnableButton != null)
 		{
 			// dis/enable like button
 			pElement.setEnabled(pEnableButton.isEnabled());
 			
 			// if it is modified, enable button
 			pElement.addListener(SWT.Verify, new ModificationListener(pEnableButton));
 		}
 	}
 }
