 package org.dawnsci.plotting.system.dialog;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.linearscale.Range;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawnsci.common.widgets.gda.roi.ROIEditTable;
 import org.dawnsci.plotting.api.region.IROIListener;
 import org.dawnsci.plotting.api.region.ROIEvent;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.draw2d.swtxy.AspectAxis;
 import org.dawnsci.plotting.draw2d.swtxy.RegionArea;
 import org.dawnsci.plotting.draw2d.swtxy.RegionCoordinateSystem;
 import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
 import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
 import org.eclipse.jface.preference.ColorSelector;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 
 public class RegionComposite extends Composite {
 
 	private XYRegionGraph xyGraph;
 
 	private Text   nameText;
 	private CCombo regionType;
 	private CCombo xCombo, yCombo;
 	private Button showPoints;
 	private AbstractSelectionRegion editingRegion;
 	private ColorSelector colorSelector;
 	private Spinner alpha;
 	private Button mobile;
 	private Button visible;
 	private Button showLabel;
 
 	private ROIEditTable roiViewer;
 
 	private Label symmetryLabel;
 
 	private CCombo symmetry;
 
 	private boolean isImplicit;
 
 	/**
 	 * Throws exception if not LightWeightPlottingSystem
 	 * 
 	 * Can be used to edit regions from outside if required.
 	 * 
 	 * @param parent
 	 * @param style
 	 * @param sys
 	 * @param defaultRegion
 	 * @param isImplicit
 	 */
 	public RegionComposite(final Composite parent, final int style, final AbstractPlottingSystem sys, final RegionType defaultRegion, final boolean isImplicit) {
      
 		this(parent,style, (XYRegionGraph)sys.getAdapter(XYRegionGraph.class), defaultRegion, isImplicit);
 	}
 	
 	/**
 	 * Used internally
 	 * @param parent
 	 * @param style
 	 * @param xyGraph
 	 * @param defaultRegion
 	 * @param isImplicit Flag to tell whether the RegionComposite is used in a specific window with an apply button or part of a view where the changes are implicit
 	 */
 	public RegionComposite(final Composite parent, final int style, final XYRegionGraph xyGraph, final RegionType defaultRegion, final boolean isImplicit) {
 		
 		super(parent, SWT.NONE);
 		
 		this.setImplicit(isImplicit);
 
 		this.xyGraph = xyGraph;
 		
 		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		setLayout(new org.eclipse.swt.layout.GridLayout(2, false));
 
 		final Label nameLabel = new Label(this, SWT.NONE);
 		nameLabel.setText("Name   ");
 		nameLabel.setLayoutData(new GridData());
 
 		nameText = new Text(this, SWT.BORDER | SWT.SINGLE);
 		nameText.setToolTipText("Region name");
 		nameText.setLayoutData(new GridData(SWT.FILL, 0, true, false));
 		if(isImplicit()){
 			nameText.addSelectionListener(new SelectionAdapter(){
 				@Override
 				public void widgetDefaultSelected(SelectionEvent e) {
 					AbstractSelectionRegion region = getEditingRegion();
 					region.repaint();
 				}
 			});
 		}
 		
 		final Label horiz = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
 		horiz.setLayoutData(new GridData(SWT.FILL, 0, true, false, 2, 1));
 
 		final Label typeLabel = new Label(this, SWT.NONE);
 		typeLabel.setText("Type");
 		typeLabel.setLayoutData(new GridData());
 
		regionType = new CCombo(this, SWT.BORDER |SWT.NONE);
 		regionType.setEditable(false);
 		regionType.setToolTipText("Region type");
 		regionType.setLayoutData(new GridData(SWT.FILL, 0, true, false));
 		for (RegionType type : RegionType.ALL_TYPES) {
 			regionType.add(type.getName());
 		}
 		regionType.select(defaultRegion.getIndex());
 
 		xCombo = createAxisChooser(this, "X Axis", xyGraph.getXAxisList());	
 		yCombo = createAxisChooser(this, "Y Axis", xyGraph.getYAxisList());	
 		
 		Label colorLabel = new Label(this, 0);
 		colorLabel.setText("Selection Color");		
 		colorLabel.setLayoutData(new GridData());
 		
 		colorSelector = new ColorSelector(this);
 		colorSelector.getButton().setLayoutData(new GridData());		
 		colorSelector.setColorValue(defaultRegion.getDefaultColor().getRGB());
 		if(isImplicit()){
 			colorSelector.addListener(new IPropertyChangeListener() {
 				@Override
 				public void propertyChange(PropertyChangeEvent event) {
 					AbstractSelectionRegion region = getEditingRegion();
 					region.repaint();
 				}
 			});
 		}
 		
 		final Label horiz2 = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
 		horiz2.setLayoutData(new GridData(SWT.FILL, 0, true, false, 2, 1));
 
 		Label alphaLabel = new Label(this, 0);
 		alphaLabel.setText("Alpha level");		
 		alphaLabel.setLayoutData(new GridData());
 		
 		alpha = new Spinner(this, SWT.NONE);
 		alpha.setToolTipText("Alpha transparency level of the shaded shape");
 		alpha.setLayoutData(new GridData());		
 		alpha.setMinimum(0);
 		alpha.setMaximum(255);
 		alpha.setSelection(80);
 		if(isImplicit()){
 			alpha.addSelectionListener(new SelectionAdapter(){
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					AbstractSelectionRegion region = getEditingRegion();
 					region.repaint();
 				}
 			});
 		}
 
 		this.mobile = new Button(this, SWT.CHECK);
 		mobile.setText("   Mobile   ");		
 		mobile.setToolTipText("When true, this selection can be resized and moved around the graph.");
 		mobile.setLayoutData(new GridData(0, 0, false, false, 2, 1));
 		mobile.setSelection(true);
 		if(isImplicit()){
 			mobile.addSelectionListener(new SelectionAdapter(){
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					AbstractSelectionRegion region = getEditingRegion();
 					region.repaint();
 				}
 			});
 		}
 		
 		this.showPoints = new Button(this, SWT.CHECK);
 		showPoints.setText("   Show vertex values");		
 		showPoints.setToolTipText("When on this will show the actual value of the point in the axes it was added.");
 		showPoints.setLayoutData(new GridData(0, 0, false, false, 2, 1));
 		if(isImplicit()){
 			showPoints.addSelectionListener(new SelectionAdapter(){
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					AbstractSelectionRegion region = getEditingRegion();
 					region.repaint();
 				}
 			});
 		}
 
 		this.visible = new Button(this, SWT.CHECK);
 		visible.setText("   Show region");		
 		visible.setToolTipText("You may turn off the visibility of this region.");
 		visible.setLayoutData(new GridData(0, 0, false, false, 2, 1));
 		visible.setSelection(true);
 		if(isImplicit()){
 			visible.addSelectionListener(new SelectionAdapter(){
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					AbstractSelectionRegion region = getEditingRegion();
 					region.repaint();
 				}
 			});
 		}
 		
 		
 		this.showLabel = new Button(this, SWT.CHECK);
 		showLabel.setText("   Show name");		
 		showLabel.setToolTipText("Turn on to show the name of a selection region.");
 		showLabel.setLayoutData(new GridData(0, 0, false, false, 2, 1));
 		showLabel.setSelection(true);
 		if(isImplicit()){
 			showLabel.addSelectionListener(new SelectionAdapter(){
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					AbstractSelectionRegion region = getEditingRegion();
 					region.repaint();
 				}
 			});
 		}
 		
 		this.symmetryLabel = new Label(this, SWT.NONE);
 		symmetryLabel.setText("Symmetry");		
 		symmetryLabel.setToolTipText("Set the symmetry of the region.");
 		symmetryLabel.setLayoutData(new GridData(0, 0, false, false, 1, 1));
 		
		this.symmetry = new CCombo(this, SWT.BORDER |SWT.NONE);
 		for (int index : SectorROI.getSymmetriesPossible().keySet()) {
 			symmetry.add(SectorROI.getSymmetryText(index));
 		}
 		symmetry.setLayoutData(new GridData(SWT.FILL, 0, true, false));
 	
 		final Label spacer = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
 		spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 		
 		// We add a composite for setting the region location programmatically.
 		final Label location = new Label(this, SWT.NONE);
 		location.setText("Region Location");
 		location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 		
 		this.roiViewer = new ROIEditTable();
 		roiViewer.createPartControl(this);
 
 		// Should be last
 		nameText.setText(getDefaultName(defaultRegion.getIndex()));
 
 	}
 
 	private CCombo createAxisChooser(RegionComposite parent,
 			                         String label, List<Axis> xAxisList) {
 		
 		final Label xAxisLabel = new Label(parent, SWT.NONE);
 		xAxisLabel.setText(label);
 		xAxisLabel.setLayoutData(new GridData());
 
		CCombo combo = new CCombo(this, SWT.BORDER |SWT.NONE);
 		combo.setEditable(false);
 		combo.setToolTipText("Existing axis on the graph ");
 		combo.setLayoutData(new GridData(SWT.LEFT, 0, false, false));
 
 		for(Axis axis : xAxisList)  combo.add(axis.getTitle());
 		
 		combo.select(0);		
 		
 		return combo;
 	}
 
 	private static Map<Integer,Integer> countMap;
 	private String getDefaultName(int sel) {
 		if (countMap==null) countMap = new HashMap<Integer,Integer>(5);
 		if (!countMap.containsKey(sel)) {
 			countMap.put(sel, 0);
 		}
 		int count = countMap.get(sel);
 		count++;
 		return regionType.getItem(sel)+" "+count;
 	}
 
 
 	public AbstractSelectionRegion createRegion() throws Exception {
 		
 		final AspectAxis xAxis = getAxis(xyGraph.getXAxisList(), xCombo.getSelectionIndex());
 		final AspectAxis yAxis = getAxis(xyGraph.getYAxisList(), yCombo.getSelectionIndex());
 		
 		AbstractSelectionRegion region=null;
 		
 		final String txt = nameText.getText();
 		final Pattern pattern = Pattern.compile(".* (\\d+)");
 		final Matcher matcher = pattern.matcher(txt);
 		if (matcher.matches()) {
 			final int count = Integer.parseInt(matcher.group(1));
 			countMap.put(regionType.getSelectionIndex(), count);
 		}
 		
 		region = xyGraph.createRegion(txt, xAxis, yAxis, RegionType.getRegion(regionType.getSelectionIndex()), true);
 		
 		this.editingRegion = region;
 		getEditingRegion();
 		
         return region;
 	}
 	
 	private IROIListener roiListener;
 	
 	public void setEditingRegion(final AbstractSelectionRegion region) {
 		
         this.editingRegion = region;
         this.roiViewer.setRegion(region.getROI(), region.getRegionType(), region.getCoordinateSystem());
         this.roiListener = new IROIListener.Stub() {			
 			@Override
 			public void roiChanged(ROIEvent evt) {
 				region.setROI(evt.getROI());
 			}
 		};
         roiViewer.addROIListener(roiListener);
        
         Range range = xyGraph.primaryXAxis.getRange();
         roiViewer.setXLowerBound(Math.min(range.getUpper(), range.getLower()));
         roiViewer.setXUpperBound(Math.max(range.getUpper(), range.getLower()));
 		
         range = xyGraph.primaryYAxis.getRange();
         roiViewer.setYLowerBound(Math.min(range.getUpper(), range.getLower()));
         roiViewer.setYUpperBound(Math.max(range.getUpper(), range.getLower()));
 
         nameText.setText(region.getName());
 		regionType.select(region.getRegionType().getIndex());
 		regionType.setEnabled(false);
 		regionType.setEditable(false);
 		
 		int index = xyGraph.getXAxisList().indexOf(region.getCoordinateSystem().getX());
 		xCombo.select(index);
 		
 		index = xyGraph.getYAxisList().indexOf(region.getCoordinateSystem().getY());
 		yCombo.select(index);
 		
 		colorSelector.setColorValue(region.getRegionColor().getRGB());
 		alpha.setSelection(region.getAlpha());
 		mobile.setSelection(region.isMobile());
 		showPoints.setSelection(region.isShowPosition());
 		visible.setSelection(region.isVisible());
 		showLabel.setSelection(region.isShowLabel());
 		
 		if (region.getRegionType()!=RegionType.SECTOR) {
 			GridUtils.setVisible(this.symmetryLabel, false);
 			GridUtils.setVisible(this.symmetry,      false);
 		} else {
 			SectorROI sroi = (SectorROI)editingRegion.getROI();
 			if (sroi!=null) {
 				int sym = sroi.getSymmetry();
 				symmetry.select(sym);
 			}
 		}
 	}
 	
 	public void dispose() {
 		if (roiListener!=null) {
 			try {
 				roiViewer.removeROIListener(roiListener);
 			} catch (Exception ne) {
 				logger.error("Cannot remove roi listener", ne);
 			}
 		}
 		super.dispose();
 	}
 	
 	public AbstractSelectionRegion getEditingRegion() {
 		
 		final String txt = nameText.getText();
 		xyGraph.renameRegion(editingRegion, txt);
 		editingRegion.setName(txt);
 		
 		final AspectAxis x = getAxis(xyGraph.getXAxisList(), xCombo.getSelectionIndex());
 		final AspectAxis y = getAxis(xyGraph.getYAxisList(), yCombo.getSelectionIndex());
 		RegionCoordinateSystem sys = new RegionCoordinateSystem(getImageTrace(), x, y);
 		editingRegion.setCoordinateSystem(sys);
 		editingRegion.setShowPosition(showPoints.getSelection());
 		editingRegion.setRegionColor(new Color(getDisplay(), colorSelector.getColorValue()));
 		editingRegion.setAlpha(alpha.getSelection());
 		editingRegion.setMobile(mobile.getSelection());
 		editingRegion.setVisible(visible.getSelection());
 		editingRegion.setShowLabel(showLabel.getSelection());
 		
 		if (editingRegion.getRegionType()==RegionType.SECTOR) {
 			SectorROI sroi = (SectorROI)editingRegion.getROI();
 			if (sroi!=null) {
 				sroi = sroi.copy();
 				sroi.setSymmetry(symmetry.getSelectionIndex());
 				editingRegion.setROI(sroi);
 			}
 		}
 		
         return editingRegion;
 	}
 
 
 	private AspectAxis getAxis(List<Axis> xAxisList, int selectionIndex) {
 		if (selectionIndex<0) selectionIndex=0;
 		return (AspectAxis)xAxisList.get(selectionIndex);
 	}
 
     public IImageTrace getImageTrace() {
     	return ((RegionArea)xyGraph.getPlotArea()).getImageTrace();
     }
 
 	public void applyChanges() {
 //		this.roiViewer.cancelEditing();
 		AbstractSelectionRegion region = getEditingRegion();
 		region.repaint();
 	}
 	private static final Logger logger = LoggerFactory.getLogger(RegionComposite.class);
 	public void cancelChanges() {
 		try {
 		    editingRegion.setROI(roiViewer.getOriginalRoi());
 		} catch (Throwable ne) {
 			logger.error("Problem reverting region  "+editingRegion.getName(), ne);
 		}
 	}
 
 
 	public void disposeRegion(AbstractSelectionRegion region) {
 		 xyGraph.disposeRegion(region);
 	}
 
 	public boolean isImplicit() {
 		return isImplicit;
 	}
 
 	public void setImplicit(boolean isImplicit) {
 		this.isImplicit = isImplicit;
 	}
 }
