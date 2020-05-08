 /*******************************************************************************
  * Copyright (c) 2010 Oak Ridge National Laboratory.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  ******************************************************************************/
 package org.eclipse.nebula.visualization.xygraph.toolbar;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.nebula.visualization.xygraph.Activator;
 import org.eclipse.nebula.visualization.xygraph.figures.Annotation;
 import org.eclipse.nebula.visualization.xygraph.figures.Axis;
 import org.eclipse.nebula.visualization.xygraph.figures.Trace;
 import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
 import org.eclipse.nebula.visualization.xygraph.undo.XYGraphConfigCommand;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 
 /**The dialog for configuring XYGraph properties.
  * @author Xihui Chen
  *
  */
 public class XYGraphConfigDialog extends Dialog {
 	
 	private GraphConfigPage graphConfigPage;
 	protected List<AnnotationConfigPage> annotationConfigPageList;
 	private List<AxisConfigPage> axisConfigPageList;
 	protected Combo traceCombo;
 	protected Combo annotationsCombo;
 	protected List<TraceConfigPage> traceConfigPageList;
 	protected XYGraph xyGraph;
 	protected XYGraphConfigCommand command;
 	private boolean changed = false;
 	
 	protected XYGraphConfigDialog(Shell parentShell, XYGraph xyGraph) {
 		super(parentShell);	
 		this.xyGraph = xyGraph;
 		graphConfigPage = new GraphConfigPage(xyGraph);
 		annotationConfigPageList = new ArrayList<AnnotationConfigPage>();
 		axisConfigPageList = new ArrayList<AxisConfigPage>();
 		traceConfigPageList = new ArrayList<TraceConfigPage>();
 		command = new XYGraphConfigCommand(xyGraph);
 		command.savePreviousStates();
         // Allow resize
         setShellStyle(getShellStyle() | SWT.RESIZE);
 	}
 	
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setText("Configure Graph Settings");
 	}
 	
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		return createDialogArea(parent, true);
 	}
 	
 	private static final int MAX_TRACE_COUNT = 50; // Otherwise run out of widgets.
 	
 	protected Control createDialogArea(Composite parent, boolean enableAxisRanges) {
 		
 		final Composite parent_composite = (Composite) super.createDialogArea(parent);
 		parent_composite.setLayout(new FillLayout());
         final TabFolder tabFolder = new TabFolder(parent_composite, SWT.NONE);   
         
         Composite graphTabComposite = new Composite(tabFolder, SWT.NONE);        
         graphConfigPage.createPage(graphTabComposite);
         
         TabItem graphConfigTab = new TabItem(tabFolder, SWT.NONE);
         graphConfigTab.setText("Graph");
         graphConfigTab.setToolTipText("Configure General Graph Settings");
         graphConfigTab.setControl(graphTabComposite);
         
         
         //Axis Configure Page
         Composite axisTabComposite = new Composite(tabFolder, SWT.NONE);
     	axisTabComposite.setLayout(new GridLayout(1, false));        	
     	TabItem axisConfigTab = new TabItem(tabFolder, SWT.NONE);
 	    axisConfigTab.setText("Axes");
 	    axisConfigTab.setToolTipText("Configure Axes Settings");
 	    axisConfigTab.setControl(axisTabComposite);
 	    
     	Group axisSelectGroup = new Group(axisTabComposite, SWT.NONE);
     	axisSelectGroup.setLayoutData(new GridData(
     			SWT.FILL, SWT.FILL,true, false));
     	axisSelectGroup.setText("Select Axis");
     	axisSelectGroup.setLayout(new GridLayout(1, false));    	        
    	final Combo axisCombo = new Combo(axisSelectGroup, SWT.DROP_DOWN);
     	axisCombo.setLayoutData(new GridData(SWT.FILL, 0, true, false));
 	    for(Axis axis : xyGraph.getAxisList())
 	        axisCombo.add(axis.getTitle() + (axis.isHorizontal() ? "(X-Axis)" : "(Y-Axis)"));	   
 	    axisCombo.select(0);
     	
 	    final Composite axisConfigComposite = new Composite(axisTabComposite, SWT.NONE);
 	    axisConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 	    final StackLayout axisStackLayout = new StackLayout();
     	axisConfigComposite.setLayout(axisStackLayout);        	
 	    for(Axis axis : xyGraph.getAxisList()){
 	        Group axisConfigGroup = new Group(axisConfigComposite, SWT.NONE); 	        	
 	        axisConfigGroup.setText("Change Settings");
 	        axisConfigGroup.setLayoutData(new GridData(
         			SWT.FILL, SWT.FILL,true, true));
 	        AxisConfigPage axisConfigPage = new AxisConfigPage(xyGraph, axis, enableAxisRanges);
 	        axisConfigPageList.add(axisConfigPage);
 	        axisConfigPage.createPage(axisConfigGroup);   	        
 	    } 	        
 	    axisStackLayout.topControl = axisConfigPageList.get(0).getComposite();
 	    axisCombo.addSelectionListener(new SelectionAdapter(){
 	        @Override
     		public void widgetSelected(SelectionEvent e) {
     			axisStackLayout.topControl = axisConfigPageList.get(
     					axisCombo.getSelectionIndex()).getComposite();
     			axisConfigComposite.layout(true, true);
     		}
     	}); 
         
 	   //Trace Configure Page     
 	   if(xyGraph.getPlotArea().getTraceList().size() > 0){
 	        Composite traceTabComposite = new Composite(tabFolder, SWT.NONE);
 	    	traceTabComposite.setLayout(new GridLayout(1, false));        	
 	    	TabItem traceConfigTab = new TabItem(tabFolder, SWT.NONE);
 		    traceConfigTab.setText("Traces");
 		    traceConfigTab.setToolTipText("Configure Traces Settings");
 		    traceConfigTab.setControl(traceTabComposite);
 		    
 		    final CLabel error = new CLabel(traceTabComposite, SWT.NONE);
 		    error.setText("There are too many traces to edit");
 		    error.setToolTipText("Currently only the first 50 line traces can have their properties manually edited.\nThis is due to a limitation with the current widget design on the configure form.\nPlease contact your support representative to have this issue resolved.");
 		    error.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, false));
 		    error.setImage(Activator.getImageDescriptor("icons/error.png").createImage());
 		    error.setVisible(xyGraph.getPlotArea().getTraceList().size()>MAX_TRACE_COUNT);
 		    
 	    	Group traceSelectGroup = new Group(traceTabComposite, SWT.NONE);
 	    	traceSelectGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, false));
 	    	traceSelectGroup.setText("Select Trace");
 	    	traceSelectGroup.setLayout(new GridLayout(1, false));    	        
	    	this.traceCombo = new Combo(traceSelectGroup, SWT.DROP_DOWN);
 	    	traceCombo.setLayoutData(new GridData(SWT.FILL, 0, true, false));
 		    int count = 0;
 	    	for(Trace trace : xyGraph.getPlotArea().getTraceList()) {
 		    	count++;
 	    		if (count>MAX_TRACE_COUNT) break; // Sorry you just cannot edit more unless 
                                                   // we change this configuration.
 		        traceCombo.add(trace.getName());	   
 		    }
 		    traceCombo.select(0);
 	    	
 		    final Composite traceConfigComposite = new Composite(traceTabComposite, SWT.NONE);
 		    traceConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		    final StackLayout traceStackLayout = new StackLayout();
 	    	traceConfigComposite.setLayout(traceStackLayout);  
 	    	
 	    	count = 0;
 	    	for(Trace trace : xyGraph.getPlotArea().getTraceList()){
 	    		count++;
 	    		if (count>MAX_TRACE_COUNT) break; // Sorry you just cannot edit more unless 
 	    		                                  // we change this configuration.
 	    		Group traceConfigGroup = new Group(traceConfigComposite, SWT.NONE); 	        	
 	    		traceConfigGroup.setText("Change Settings");
 	    		traceConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, true));
 	    		TraceConfigPage traceConfigPage =  new TraceConfigPage(xyGraph, trace);
 	    		traceConfigPageList.add(traceConfigPage);
 	    		traceConfigPage.createPage(traceConfigGroup);   	        
 	    	} 	        
 	    	traceStackLayout.topControl = traceConfigPageList.get(0).getComposite();
 	    	traceCombo.addSelectionListener(new SelectionAdapter(){
 	    		@Override
 	    		public void widgetSelected(SelectionEvent e) {
 	    			traceStackLayout.topControl = traceConfigPageList.get(
 	    					traceCombo.getSelectionIndex()).getComposite();
 	    			traceConfigComposite.layout(true, true);
 	    		}
 	    	}); 
 	   }
 	        
         
         //Annotation Configure Page
         if(xyGraph.getPlotArea().getAnnotationList().size()>0){
         	Composite annoTabComposite = new Composite(tabFolder, SWT.NONE);
         	annoTabComposite.setLayout(new GridLayout(1, false));        	
         	TabItem annoConfigTab = new TabItem(tabFolder, SWT.NONE);
 		    annoConfigTab.setText("Annotations");
 		    annoConfigTab.setToolTipText("Configure Annotation Settings");
 		    annoConfigTab.setControl(annoTabComposite);
 		    
         	Group annoSelectGroup = new Group(annoTabComposite, SWT.NONE);
         	annoSelectGroup.setLayoutData(new GridData(
         			SWT.FILL, SWT.FILL,true, false));
         	annoSelectGroup.setText("Select Annotation");
         	annoSelectGroup.setLayout(new GridLayout(1, false));    	        
         	this.annotationsCombo = new Combo(annoSelectGroup, SWT.DROP_DOWN);
         	annotationsCombo.setLayoutData(new GridData(SWT.FILL, 0, true, false));
  	        for(Annotation annotation : xyGraph.getPlotArea().getAnnotationList())
  	        	annotationsCombo.add(annotation.getName());
  	        annotationsCombo.select(0);
         	
  	        final Composite annoConfigComposite = new Composite(annoTabComposite, SWT.NONE);
  	        annoConfigComposite.setLayoutData(new GridData(
  	        		SWT.FILL, SWT.FILL, true, false));
  	        final StackLayout stackLayout = new StackLayout();
         	annoConfigComposite.setLayout(stackLayout);        	
  	        for(Annotation annotation : xyGraph.getPlotArea().getAnnotationList()){
  	        	Group annoConfigGroup = new Group(annoConfigComposite, SWT.NONE); 	        	
 		        annoConfigGroup.setText("Change Settings");
 		        annoConfigGroup.setLayoutData(new GridData(
 	        			SWT.FILL, SWT.FILL,true, true));
 		        AnnotationConfigPage annotationConfigPage = 
 		        	new AnnotationConfigPage(xyGraph, annotation);
 		        annotationConfigPageList.add(annotationConfigPage);
 		        annotationConfigPage.createPage(annoConfigGroup);   	        
  	        } 	        
  	        stackLayout.topControl = annotationConfigPageList.get(0).getComposite();
  	        annotationsCombo.addSelectionListener(new SelectionAdapter(){
         		@Override
         		public void widgetSelected(SelectionEvent e) {
         			stackLayout.topControl = annotationConfigPageList.get(
         					annotationsCombo.getSelectionIndex()).getComposite();
         			annoConfigComposite.layout(true, true);
         		}
         	}); 	       
         }
         
 		return parent_composite;
 	}
 	
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		((GridLayout) parent.getLayout()).numColumns++;
 		Button button = new Button(parent, SWT.PUSH);
 		button.setText("Apply");
 		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
 		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
 		data.widthHint = Math.max(widthHint, minSize.x);
 		button.setLayoutData(data);
 		button.addSelectionListener(new SelectionAdapter(){
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				applyChanges();
 			}
 		});		
 		super.createButtonsForButtonBar(parent);
 		Shell shell = parent.getShell();
 		if (shell != null) {
 			shell.setDefaultButton(button);
 		}
 	}
 	
 	
 	@Override
 	protected void okPressed() {	
 		applyChanges();
 		command.saveAfterStates();
 		xyGraph.getOperationsManager().addCommand(command);
 		super.okPressed();
 	}
 	
 	protected void applyChanges(){	
 		changed = true;
 		graphConfigPage.applyChanges();
 		for(AxisConfigPage axisConfigPage : axisConfigPageList)
 			axisConfigPage.applyChanges();
 		for(TraceConfigPage traceConfigPage : traceConfigPageList)
 			traceConfigPage.applyChanges();
 		for(AnnotationConfigPage annotationConfigPage : annotationConfigPageList)
 			annotationConfigPage.applyChanges();					
 	}
 	
 	@Override
 	protected void cancelPressed() {
 		if(changed){
 			command.saveAfterStates();
 			xyGraph.getOperationsManager().addCommand(command);
 		}			
 		super.cancelPressed();
 	}
 }
