 package org.dawb.common.ui.widgets;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.PlottingFactory;
 import org.dawb.common.ui.plot.region.IROIListener;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.roi.AxisPixelROIEditTable;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 
 /**
  * Class to create an {@link AxisPixelROIEditTable} which implements an {@link}IROIListener
  * and shows the sum, minimum and maximum of a Rectangular ROI
  * @author wqk87977
  *
  */
 public class ROIWidget implements IROIListener {
 
 	private final static Logger logger = LoggerFactory.getLogger(ROIWidget.class);
 
 	private Composite parent;
 
 	public String viewName;
 
 	private boolean roiChanged;
 	private Composite regionComposite;
 	private IRegionListener regionListener;
 	private AbstractPlottingSystem plottingSystem;
 
 	private AxisPixelROIEditTable roiViewer;
 
 	private IRegion region;
 
 	private Text nameText;
 
 	private boolean isProfile = false;
 
 	private boolean showName = false;
 	
 	private String regionName;
 
 	private String tableTitle;
 
 	/**
 	 * Constructor
 	 * @param parent
 	 * @param viewName the name of the plottingSystem
 	 */
 	public ROIWidget(Composite parent, AbstractPlottingSystem plottingSystem, String tableTitle) {
 
 		this.parent = parent;
 		this.plottingSystem = plottingSystem;
 		this.tableTitle = tableTitle;
 		this.regionListener = getRegionListener(plottingSystem);
 		this.plottingSystem.addRegionListener(regionListener);
 
 		logger.debug("widget created");
 	}
 
 	/**
 	 * Creates the widget and its controls
 	 */
 	public void createWidget(){
 		regionComposite = new Composite(parent, SWT.NONE);
 		GridData gridData = new GridData(SWT.FILL, SWT.LEFT, true, true);
 		regionComposite.setLayout(new GridLayout(1, false));
 		regionComposite.setLayoutData(gridData);
 
 		Collection<IRegion> regions = plottingSystem.getRegions();
 		if(regions.size()>0){
 			IRegion region = (IRegion)regions.toArray()[0];
 			createRegionComposite(regionComposite, region.getRegionType());
 			region.addROIListener(ROIWidget.this);
 		}else{
 			createRegionComposite(regionComposite, RegionType.PERIMETERBOX);
 		}
 	}
 
 	private void createRegionComposite(Composite regionComposite, RegionType regionType){
 
 		if(showName){
 			Composite nameComp = new Composite(regionComposite, SWT.NONE);
 			nameComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 			nameComp.setLayout(new GridLayout(2, false));
 
 			final Label nameLabel = new Label(nameComp, SWT.NONE);
 			nameLabel.setText("Region Name  ");
 			nameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
 
 			nameText = new Text(nameComp, SWT.BORDER | SWT.SINGLE);
 			nameText.setToolTipText("Region name");
 			nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 			nameText.setEditable(false);
 		}
 
 		Group regionTableGroup = new Group (regionComposite, SWT.NONE);
 		GridData gridData = new GridData(SWT.FILL, SWT.LEFT, true, true);
 		regionTableGroup.setLayout(new GridLayout(1, false));
 		regionTableGroup.setLayoutData(gridData);
 		regionTableGroup.setText(tableTitle);
 		roiViewer = new AxisPixelROIEditTable(regionTableGroup, plottingSystem);
 		roiViewer.setIsProfileTable(isProfile);
 		roiViewer.createControl();
 
 		// Should be last
 		if(showName)
 			nameText.setText(getDefaultName(regionType.getIndex()));
 		regionName = getDefaultName(regionType.getIndex());
 	}
 
 	private static Map<Integer, Integer> countMap;
 
 	private String getDefaultName(int sel) {
 		if (countMap == null)
 			countMap = new HashMap<Integer, Integer>(5);
 		if (!countMap.containsKey(sel)) {
 			countMap.put(sel, 0);
 		}
 		int count = countMap.get(sel);
 		count++;
 		LinkedList<String> regionTypeList = new LinkedList<String> (); 
 		for (RegionType type : RegionType.ALL_TYPES) {
 			regionTypeList.add(type.getName());
 		}
 		return regionTypeList.get(sel) + " " + count;
 	}
 
 	/**
 	 * Method to be used to show the name Text field of the ROI<br>
 	 * FALSE by default
 	 * @param show
 	 */
 	public void showName(boolean show){
 		this.showName  = show;
 	}
 
 	/**
 	 * Update the widget with the correct roi information
 	 */
 	public void update(){
 		this.plottingSystem = (AbstractPlottingSystem)PlottingFactory.getPlottingSystem(viewName);
 		if(plottingSystem != null){
 			Collection<IRegion> regions = plottingSystem.getRegions();
 			if(regions.size()>0){
 				IRegion region = (IRegion)regions.toArray()[0];
 				if(roiViewer == null)
 					createRegionComposite(regionComposite, region.getRegionType());
 				roiViewer.setTableValues(region.getROI());
 
 				if(nameText != null)
 					nameText.setText(region.getName());
 				regionName = region.getName();
 				this.region = region;
 				region.addROIListener(ROIWidget.this);
 			}
 		}
 	}
 
 	@Override
 	public void roiDragged(ROIEvent evt) {}
 
 	@Override
 	public void roiSelected(ROIEvent evt) {}
 
 	@Override
 	public void roiChanged(ROIEvent evt) {
 		// if change occurs on the plot view
 		IRegion region = (IRegion) evt.getSource();
 		if(region!=null){
 			setEditingRegion(region);
 		}
 		roiChanged = true;
 		this.region = region;
 	}
 
 	/**
 	 * Method to set the input of this widget given an IRegion
 	 * @param region
 	 */
 	public void setEditingRegion(IRegion region){
		if(region == null || roiViewer == null) return;
		if(region.getROI() != null)
			roiViewer.setTableValues(region.getROI());
 		if(nameText != null && !nameText.isDisposed())
 			nameText.setText(region.getName());
 		regionName = region.getName();
 	}
 
 	/**
 	 * Method to build a Table Viewer for a main plottingSystem or for a profile plotting System<br>
 	 * FALSE by default.
 	 * @param isProfile
 	 */
 	public void setIsProfile(boolean isProfile){
 		this.isProfile = isProfile;
 	}
 
 	/**
 	 * 
 	 * @return IRegion
 	 */
 	public IRegion getRegion(){
 		return region;
 	}
 
 	public boolean getRoiChanged(){
 		return roiChanged;
 	}
 
 	public void setRoiChanged(boolean value){
 		this.roiChanged = value;
 	}
 
 	public Composite getRegionComposite(){
 		return regionComposite;
 	}
 
 	private IRegionListener getRegionListener(final AbstractPlottingSystem plottingSystem){
 		return new IRegionListener.Stub() {
 			@Override
 			public void regionRemoved(RegionEvent evt) {
 				IRegion region = evt.getRegion();
 				logger.debug("Region removed");
 				if (region!=null) {
 //						roiViewer.disposeRegion((AbstractSelectionRegion) region);
 						if(plottingSystem.getRegions().size()>0){
 							IRegion lastRegion = (IRegion)plottingSystem.getRegions().toArray()[0];
 							roiViewer.setTableValues(lastRegion.getROI());
 						}
 
 					region.removeROIListener(ROIWidget.this);
 					parent.layout();
 					parent.redraw();
 				}
 			}
 
 			@Override
 			public void regionAdded(RegionEvent evt) {
 				logger.debug("Region added");
 				
 				IRegion region = evt.getRegion();
 				if (region!=null) {
 					region.addROIListener(ROIWidget.this);
 					if(roiViewer==null){
 						createRegionComposite(regionComposite, region.getRegionType());
 						roiViewer.setTableValues(region.getROI());
 					}
 					
 					parent.layout();
 					parent.redraw();
 				}
 			}
 
 			@Override
 			public void regionCreated(RegionEvent evt) {
 				logger.debug("Region created");
 				IRegion region = evt.getRegion();
 				if (region!=null) {
 					region.addROIListener(ROIWidget.this);
 					if(roiViewer==null){
 						createRegionComposite(regionComposite, region.getRegionType());
 						roiViewer.setTableValues(region.getROI());
 					}
 				}
 			}
 
 			@Override
 			public void regionsRemoved(RegionEvent evt) {
 				IWorkbenchPage page =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 				if(page != null){
 					String id = page.getActivePart().getSite().getId();
 					if(!id.equals("org.dawb.workbench.editor.H5Editor")){
 						Iterator<IRegion> it = plottingSystem.getRegions().iterator();
 						while(it.hasNext()){
 							IRegion region = it.next();
 							region.removeROIListener(ROIWidget.this);
 						}
 					}
 				}
 			}
 		};
 	}
 
 	private void clearListeners(AbstractPlottingSystem plotSystem, IRegionListener listener) {
 		if (plotSystem==null) return;
 		Collection<IRegion> regions = plotSystem.getRegions();
 		if(regions != null && regions.size() > 0){
 			Iterator<IRegion> it = regions.iterator();
 			while(it.hasNext()){
 				IRegion region = it.next();
 				region.removeROIListener(this);
 			}
 		}
 		plotSystem.removeRegionListener(listener);
 	}
 
 	/**
 	 * Add a region listener
 	 * @param plotSystem
 	 */
 	public void addRegionListener(AbstractPlottingSystem plotSystem){
 		
 		if (plotSystem==null) return;
 		Collection<IRegion> regions = plotSystem.getRegions();
 		if(regions != null && regions.size() > 0){
 			Iterator<IRegion> it = regions.iterator();
 			while(it.hasNext()){
 				IRegion region = it.next();
 				region.addROIListener(this);
 			}
 		}
 		plotSystem.addRegionListener(regionListener);
 	}
 
 	/**
 	 * This method needs to be called to clear the region listeners
 	 */
 	public void dispose(){
 		clearListeners(plottingSystem, regionListener);
 	}
 
 	/**
 	 * Method to add a SelectionChangedListener to the table viewer
 	 * @param listener
 	 */
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		roiViewer.addSelectionChangedListener(listener);
 	}
 
 	/**
 	 * Method to remove a SelectionChangedListener from the table viewer
 	 * @param listener
 	 */
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		roiViewer.removeSelectionChangedListener(listener);
 	}
 
 	/**
 	 * Method that returns the current ROI
 	 * @return ROIBase
 	 */
 	public ROIBase getROI(){
 		return roiViewer.getROI();
 	}
 
 	/**
 	 * Method that returns the region name<br>
 	 * @return String
 	 */
 	public String getRegionName(){
 		return regionName;
 	}
 }
