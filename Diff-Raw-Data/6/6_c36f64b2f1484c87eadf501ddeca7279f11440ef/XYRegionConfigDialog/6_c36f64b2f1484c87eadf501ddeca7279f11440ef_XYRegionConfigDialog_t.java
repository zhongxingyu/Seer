 package org.dawnsci.plotting.system.dialog;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.csstudio.swt.xygraph.figures.Annotation;
 import org.csstudio.swt.xygraph.figures.XYGraph;
 import org.csstudio.swt.xygraph.toolbar.XYGraphConfigDialog;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.trace.ILineTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.draw2d.swtxy.ImageTrace;
 import org.dawnsci.plotting.draw2d.swtxy.RegionArea;
 import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
 import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
 import org.dawnsci.plotting.system.LineTraceImpl;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 
 public class XYRegionConfigDialog extends XYGraphConfigDialog {
 
 	private List<RegionEditComposite> regionList;
 	private XYRegionGraph         regionGraph;
 	protected Combo imageTraceCombo;
 	protected List<ImageTraceComposite> imageTraceConfigPageList;
 	protected IPlottingSystem     plottingSystem;
 	private boolean isRescale;
 
 	public XYRegionConfigDialog(Shell parentShell, XYGraph xyGraph, boolean isRescale) {
 		super(parentShell, xyGraph);
 		regionList = new ArrayList<RegionEditComposite>();
 		this.regionGraph = (XYRegionGraph)xyGraph;
 	
 		command = new XYRegionConfigCommand(xyGraph);
 		command.savePreviousStates();
         this.isRescale = isRescale;
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 
 		final Composite parent_composite = (Composite)super.createDialogArea(parent, !isRescale);
 		final TabFolder tabFolder = (TabFolder)parent_composite.getChildren()[0];  
 
 		int imageTraceIndex = -1;
 		
 		//Image Trace Configure Page     
 		if (regionGraph.getRegionArea().getImageTraces().size()>0){
 			
 			Composite traceTabComposite = new Composite(tabFolder, SWT.NONE);
 			traceTabComposite.setLayout(new GridLayout(1, false));        	
 			TabItem traceConfigTab = new TabItem(tabFolder, SWT.NONE);
 			traceConfigTab.setText("Image Traces");
 			traceConfigTab.setToolTipText("Configure Image Traces Settings");
 			traceConfigTab.setControl(traceTabComposite);
 			imageTraceIndex = tabFolder.getChildren().length-1;
 
 			Group traceSelectGroup = new Group(traceTabComposite, SWT.NONE);
 			traceSelectGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, false));
 			traceSelectGroup.setText("Select Trace");
 			traceSelectGroup.setLayout(new GridLayout(1, false));    	        
 			this.imageTraceCombo = new Combo(traceSelectGroup, SWT.DROP_DOWN);
 			imageTraceCombo.setLayoutData(new GridData(SWT.FILL, 0, true, false));
 			for(String traceName : regionGraph.getRegionArea().getImageTraces().keySet())
 				imageTraceCombo.add(traceName);	   
 			imageTraceCombo.select(0); // Normally only 1!
 
 			final Composite traceConfigComposite = new Composite(traceTabComposite, SWT.NONE);
 			traceConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 			final StackLayout traceStackLayout = new StackLayout();
 			traceConfigComposite.setLayout(traceStackLayout);  
 			
 			for(String traceName : regionGraph.getRegionArea().getImageTraces().keySet()){
 				
 				final ImageTrace imageTrace = regionGraph.getRegionArea().getImageTraces().get(traceName);
 				ImageTraceComposite traceConfigPage = new ImageTraceComposite(traceConfigComposite, this, plottingSystem, imageTrace);
 				if (imageTraceConfigPageList==null) imageTraceConfigPageList = new ArrayList<ImageTraceComposite>(3);
 				imageTraceConfigPageList.add(traceConfigPage);
 			} 	        
 			traceStackLayout.topControl = imageTraceConfigPageList.get(0);
 			imageTraceCombo.addSelectionListener(new SelectionAdapter(){
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					traceStackLayout.topControl = imageTraceConfigPageList.get(
 							imageTraceCombo.getSelectionIndex());
 					traceConfigComposite.layout(true, true);
 				}
 			}); 
 		}
 
         //Region Configure Page
         if ( ((RegionArea)regionGraph.getPlotArea()).hasUserRegions()){
         	
         	Composite regionComposite = new Composite(tabFolder, SWT.NONE);
         	regionComposite.setLayout(new GridLayout(1, false));        	
         	TabItem regionConfigTab = new TabItem(tabFolder, SWT.NONE);
 		    regionConfigTab.setText("Regions");
 		    regionConfigTab.setToolTipText("Configure Selection Regions");
 		    regionConfigTab.setControl(regionComposite);
 		    
         	Group regionSelectGroup = new Group(regionComposite, SWT.NONE);
         	regionSelectGroup.setLayoutData(new GridData(
         			SWT.FILL, SWT.FILL,true, false));
         	regionSelectGroup.setText("Selection Region");
         	regionSelectGroup.setLayout(new GridLayout(1, false));    	        
         	final Combo regionCombo = new Combo(regionSelectGroup, SWT.DROP_DOWN);
         	regionCombo.setLayoutData(new GridData(SWT.FILL, 0, true, false));
         	
         	
  	        for(String name : ((RegionArea)regionGraph.getPlotArea()).getRegionNames()) {
 	        	if (!regionGraph.getRegion(name).isUserRegion()) continue;
 	        	regionCombo.add(name);
  	        }
  	        regionCombo.select(0);
         	
  	        final Composite regionConfigComposite = new Composite(regionComposite, SWT.NONE);
  	        regionConfigComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
  	        final StackLayout stackLayout = new StackLayout();
  	        regionConfigComposite.setLayout(stackLayout);
  	        
  	        for(AbstractSelectionRegion region : ((RegionArea)regionGraph.getPlotArea()).getRegions()){
 		        
  	        	if (!region.isUserRegion()) continue;
 		        RegionEditComposite regionPage = new RegionEditComposite(regionConfigComposite, plottingSystem, SWT.NONE, (XYRegionGraph)xyGraph, region.getRegionType(), false);
 		        regionList.add(regionPage);
 		        regionPage.setEditingRegion(region);
  	        }
  	        
  	        stackLayout.topControl = regionList.get(0);
  	        regionCombo.addSelectionListener(new SelectionAdapter(){
         		@Override
         		public void widgetSelected(SelectionEvent e) {
         			final int index = regionCombo.getSelectionIndex();
         			stackLayout.topControl = regionList.get(index);
         			regionConfigComposite.layout(true, true);
         		}
         	}); 	
  	        
  	       if (selectedRegion!=null) {
  	    	   tabFolder.setSelection(regionConfigTab);
  	    	   int index = ((RegionArea)regionGraph.getPlotArea()).getRegions().indexOf(selectedRegion);
  	   	       regionCombo.select(index);
 	   	       try {
 	   	   	       stackLayout.topControl = regionList.get(index);
 	   	       } catch (IndexOutOfBoundsException nr) {
 	   	    	   nr.printStackTrace();
 	   	       }
    			   regionConfigComposite.layout(true, true);
 	       }
  	       
 
         }
         
         if (selectedAnnotation!=null) {
         	final int index = xyGraph.getPlotArea().getAnnotationList().indexOf(selectedAnnotation);
         	final TabItem[] items = tabFolder.getItems();
          	for (int i = 0; i < items.length; i++) {
 				if ("Annotations".equalsIgnoreCase(items[i].getText())) {
 					tabFolder.setSelection(i);
 					annotationsCombo.select(index);
 					Composite annoTabComposite = (Composite)items[i].getControl();
 					Composite annoConfigComposite = (Composite)annoTabComposite.getChildren()[1];
 					final StackLayout stackLayout = (StackLayout)annoConfigComposite.getLayout();
 					stackLayout.topControl = annotationConfigPageList.get(index).getComposite();
         			annoConfigComposite.layout(true, true);
         			break;
 				}
 			}
          }
         
         if (selectedTrace!=null) {
         	if (selectedTrace instanceof ILineTrace) {
         		tabFolder.setSelection(2);
         	   	int index = regionGraph.getRegionArea().getTraceList().indexOf(selectedTrace); 
         	   	if (index>0) {
 	        		Composite traceComp = traceConfigPageList.get(index).getComposite(); 		
 		        	setTraceTabSelected(index, tabFolder, traceCombo, traceComp);
         	   	}
 	        	
         	} else {
         		tabFolder.setSelection(imageTraceIndex);
            	   	int index = 0; // FIXME if there is ever more than one image allowed to be plotted.
            	   	if (index>0) {
 	         		Composite traceComp = imageTraceConfigPageList.get(index); 		
 	        		setTraceTabSelected(index, tabFolder, imageTraceCombo, traceComp);
            	   	}
         	}
         }
        
 		return parent_composite;
 	}
 	
 	private static final void setTraceTabSelected(int index, TabFolder tabFolder, Combo combo, Composite composite) {
 
 		combo.select(index);
 
 		final TabItem traces = tabFolder.getItem(index);
 		final Composite traceTabComposite = (Composite)traces.getControl();
 		final Composite traceConfigComposite = (Composite)traceTabComposite.getChildren()[1];
 		final StackLayout sl = (StackLayout)traceConfigComposite.getLayout();
 		sl.topControl = composite;
 		traceTabComposite.layout(true, true);
 
 	}
 
 	protected void applyChanges(){	
  
 		super.applyChanges();
 		if (regionList!=null) for (RegionEditComposite comp : regionList){
 			comp.applyChanges();
 		}
 		if (imageTraceConfigPageList!=null) for (ImageTraceComposite comp : imageTraceConfigPageList){
 			comp.applyChanges();
 		}
 		regionGraph.fireConfigurationPropertyChangeListeners();
 		if (isRescale) regionGraph.performAutoScale();
 		xyGraph.revalidate();
 		xyGraph.repaint();	
 	}
 	@Override
 	protected void cancelPressed() {
 		if (regionList!=null) for (RegionEditComposite comp : regionList){
 			comp.cancelChanges();
 		}
 		super.cancelPressed();
 	}
 
 	private IRegion selectedRegion;
 
 	public IRegion getSelectedRegion() {
 		return selectedRegion;
 	}
 
 	public void setSelectedRegion(IRegion selectedRegion) {
 		this.selectedRegion = selectedRegion;
 	}
 	
 	private Object selectedTrace;
 	@SuppressWarnings("unused")
 	private String selectedTraceName;
 	private Annotation selectedAnnotation;
 
 	public void setSelectedTrace(ITrace trace) {
 		selectedTraceName = trace.getName();
 		if (trace instanceof LineTraceImpl) {
 			selectedTrace = ((LineTraceImpl)trace).getTrace();
 			return;
 		}
 		selectedTrace = trace;
 	}
 
 	public IPlottingSystem getPlottingSystem() {
 		return plottingSystem;
 	}
 
 	public void setPlottingSystem(IPlottingSystem plottingSystem) {
 		this.plottingSystem = plottingSystem;
 	}
 
 	public void setSelectedAnnotation(Annotation annotation) {
 		this.selectedAnnotation = annotation;
 	}
 	
 }
