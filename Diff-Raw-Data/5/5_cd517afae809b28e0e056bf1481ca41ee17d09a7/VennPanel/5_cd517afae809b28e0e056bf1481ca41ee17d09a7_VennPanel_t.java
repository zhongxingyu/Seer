 /*
  * VennMaster/geometry/VennPanel.java
  * 
  * Created on 30.06.2004
  * 
  * 
  */
 package venn.gui;
 
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import junit.framework.Assert;
 
 import org.apache.batik.dom.GenericDOMImplementation;
 import org.apache.batik.svggen.SVGGraphics2D;
 import org.apache.batik.svggen.SVGGraphics2DIOException;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import venn.AllParameters;
 import venn.VennArrangementsOptimizer;
 import venn.db.IVennDataModel;
 import venn.db.ManualFilter;
 import venn.db.VennDataSplitter;
 import venn.db.VennFilteredDataModel;
 import venn.diagram.IVennDiagramView;
 import venn.diagram.IVennObject;
 import venn.diagram.VennArrangement;
 import venn.diagram.VennDiagramView;
 import venn.diagram.VennErrorFunction;
 import venn.diagram.VennObjectFactory;
 import venn.event.DuplFilterChainSucc;
 import venn.event.IFilterChainSucc;
 import venn.event.IVennPanelHasDataListener;
 import venn.event.ResultAvailableListener;
 import venn.geometry.AffineTransformer;
 import venn.geometry.DragLabel;
 import venn.geometry.FPoint;
 import venn.optim.IOptimizer;
 
 import com.sun.image.codec.jpeg.JPEGCodec;
 import com.sun.image.codec.jpeg.JPEGEncodeParam;
 import com.sun.image.codec.jpeg.JPEGImageEncoder;
 
 /**
  * This panel can load and optimize sets.
  * 
  * @author muellera
  */
 public class VennPanel extends JPanel
 implements ChangeListener, ResultAvailableListener, HasLabelsListener
 { 
 	/**
      * 
      */
     private static final long serialVersionUID = 1L;
 
 	
 	private IVennDataModel 	        	sourceDataModel;		//!< The current data model to be used.
     private final VennFilteredDataModel filteredModel;
     private final ManualFilter 			manualFilter;
     private final VennDataSplitter      dataSplitter;
 		
 	private LinkedList 				changeListeners,
 									actionListeners;
 
 	// parameters
 	private AllParameters 		   	params;
     private VennArrangement[]     	arrangements;   // every independent subproblem is an arrangement
     private IVennDiagramView[]      views;          // a view shows an arrangement on the screen
 
 
     private IVennDiagramView[] 							unfilteredViews;
     private final VennDataSplitter 						unfilteredDataSplitter;
     private VennArrangement[] 							unfilteredArrangements;
     private final VennArrangementsOptimizer 			vennArrsOptim;
     private final LinkedList<IVennPanelHasDataListener> vennPanelHasDataListener;
     private final LinkedList<HasLabelsListener> 		hasLabelsListeners;
     private final List<DragLabel> 						labels;
     private Map<BitSet, Color>							pathColors;
     private int 										zoomLevel = -1;
 	private JTextArea		inconsistencyInfo;      // show inconsistencies (not fulfilled intersections)
 
  	public VennPanel(VennArrangementsOptimizer opt)
 	{          
  		vennArrsOptim = opt;
  		vennArrsOptim.addResultAvailableListener(this); // => resultAvailable
  		
  		sourceDataModel = null;
  	    
         unfilteredDataSplitter = new VennDataSplitter();
         unfilteredDataSplitter.setSucc(new IFilterChainSucc() {
 //        	@Override
         	public void predChanged() {
             	updateUnfiltered();
                 fireChangeEvent();
         	}
         });
         unfilteredDataSplitter.setSuccFinal();
 
         dataSplitter = new VennDataSplitter();
         dataSplitter.setSucc(new IFilterChainSucc() {
 //        	@Override
         	public void predChanged() { // groups were activated or deactivated
         		update();
 
                 updateUnfilteredFromFilteredSelection();
                 fireChangeEvent();
         	}
         });
         dataSplitter.setSuccFinal();
         
         manualFilter = new ManualFilter();
 
         filteredModel = new VennFilteredDataModel();
         
 		params = new AllParameters();
 		
 		setLayout(null);
 				
 		// data
 		changeListeners = new LinkedList();
 		actionListeners = new LinkedList();
 		vennPanelHasDataListener = new LinkedList<IVennPanelHasDataListener>();
 		hasLabelsListeners = new LinkedList<HasLabelsListener>();
     	labels = new LinkedList<DragLabel>();
     	pathColors = new HashMap<BitSet, Color>();
 		
 		setToolTipText("");
 		setOpaque(true);
 		setAutoscrolls(true); 
 		setPreferredSize(new Dimension(400,400));
         setBackground( Color.WHITE );
 		//setFocusable(true);
 	}
 	 	
  	public void setInconsistencyJTextArea(JTextArea inconsistencyInfo) {
  		this.inconsistencyInfo = inconsistencyInfo;
  	}
  	
  	private void updateInconsistencyInfo() {
  		if (inconsistencyInfo != null) {
  			inconsistencyInfo.setText(getInconsistencies());
  			inconsistencyInfo.moveCaretPosition(0);
  		}
  	}
  	
 // 	@Override
  	public void resultAvailable(boolean isFinalResult) {
  		VennArrangement[] vas = vennArrsOptim.getOptArrangements();
  		if (vas.length != arrangements.length) {
  			throw new IllegalStateException();
  		}
  		
  		for (int i = 0; i < arrangements.length; i++) {
  			arrangements[i].assignState(vas[i]);
  		}
  		
  		if (SwingUtilities.isEventDispatchThread()) {
  			invalidateView();
  			updateCostValues();
  			updateInconsistencyInfo();
  			repaint();
  		} else {
  			SwingUtilities.invokeLater(new Runnable() {
 // 				@Override
  				public void run() {
  					invalidateView();
  					updateCostValues();
  					updateInconsistencyInfo();
  					repaint();
  				}
  			});
  		}
  	}
  	
  	private void updateCostValues() {
 		final IVennDiagramView[] views = getViews();
 		if( views != null )
 		{
 			for( int i=0; i<views.length; ++i )
 			{
 				VennErrorFunction errf = new VennErrorFunction( views[i].getTree(), params.errorFunction );
 
 				DecimalFormat format = new DecimalFormat("0.000");
 				views[i].setInfoText("cost = "+format.format(-errf.getOutput()));
 			}
 		}
 
  	}
  	
  	public synchronized void addVennPanelHasDataListener(IVennPanelHasDataListener listener) {
  		vennPanelHasDataListener.add(listener);
  	}
  	
  	private synchronized void notifyVennPanelHasDataListeners() {
  		for (IVennPanelHasDataListener listener : vennPanelHasDataListener) {
  			listener.vennPanelHasDataChanged(hasData());
  		}
  	}
  	
  	private void clear()
 	{
         if( views != null )
         {
             for(int i=0; i<views.length; ++i ) {
                 views[i].removeChangeListener( this );
                 
                 if (views[i] instanceof VennDiagramView) {
                 	((VennDiagramView)views[i]).removeHasLabelsListener(this);
                 }
             }
         }
 
         removeAll();
 
 		views = null;
         arrangements = null;
                 
 //        Runtime.getRuntime().gc();
 	}
  	
  	private void clearUnfiltered() {
         unfilteredViews = null;
         unfilteredArrangements = null;
  	}
 	
 	/**
 	 * Sets the data set of the venn panel. Creates views etc.
 	 * The panel will be repainted.
 	 * (check if saveLabels() before update() is necessary)
 	 */
 	private synchronized void update()
 	{
 		clear();
 
 		if( sourceDataModel == null || sourceDataModel.getNumGroups() == 0 )
 		{
 			setVisible(false);
 		    repaint();
 			return;
 		}
         
         // Create one VennDiagramView for each sub-problem
         IVennDataModel models[] = dataSplitter.getModels();
         
         
         // TODO: encapsulate the whole generation process in a factory method
         // Compute scaling factor depending on the maximum cardinality
         int maxCard = 0;
         for( int i=0; i<sourceDataModel.getNumGroups(); ++i )
         {
             int card = sourceDataModel.getGroupElements(i).cardinality();
             if( card > maxCard )
                 maxCard = card;
         }
         
         int maxNum = 0;
         for( int i=0; i<models.length; ++i )
         {
             if( models[i].getNumGroups() > maxNum )
                 maxNum = models[i].getNumGroups(); 
         }
 
         double radius = params.sizeFactor*0.5/Math.max(2.0,Math.sqrt((double)maxNum));
         
         double factor = 2.0*(double)maxCard /
                         ((double)params.numEdges*
                         Math.sin(2.0*Math.PI/(double)params.numEdges))/(radius*radius);
 
         VennObjectFactory factory = new VennObjectFactory();
         factory.setPolygonParameters( params.numEdges, factor );
         
         
         setLayout(new GridLayout(1,models.length));
         
         arrangements = new VennArrangement[models.length];
         views = new IVennDiagramView[models.length];
         
         for( int i=0; i<models.length; ++i )
         {
         	models[i].setSucc(null); // because we make new VennArrangements
             arrangements[i] = new VennArrangement( models[i], factory );
     		arrangements[i].setParameters(params);
             VennDiagramView v = new VennDiagramView( arrangements[i], 
                                                      params.errorFunction.maxIntersections, params.logNumElements );
             views[i] = v;
             add( v );
             v.addChangeListener( this ); // => stateChanged
             v.addHasLabelsListener(this); // => hasLabelsChanged
         }
         copyColorsFromUnfiltered();
 		restoreLabels();
 		restoreManuallySetColors();
 
         vennArrsOptim.setArrangements(arrangements);
         notifyHasLabelsChanged();
         validate();
         setVisible(true);
         repaint();
 	}
     
 	private synchronized void updateUnfiltered()
 	{
 		clearUnfiltered();
 
 		if( sourceDataModel == null || sourceDataModel.getNumGroups() == 0 )
 		{
 			return;
 		}
         
         // Create one VennDiagramView for each sub-problem
         IVennDataModel models[] = unfilteredDataSplitter.getModels();
         
         
         // TODO: encapsulate the whole generation process in a factory method
         // Compute scaling factor depending on the maximum cardinality
         int maxCard = 0;
         for( int i=0; i<sourceDataModel.getNumGroups(); ++i )
         {
             int card = sourceDataModel.getGroupElements(i).cardinality();
             if( card > maxCard )
                 maxCard = card;
         }
         
         int maxNum = 0;
         for( int i=0; i<models.length; ++i )
         {
             if( models[i].getNumGroups() > maxNum )
                 maxNum = models[i].getNumGroups(); 
         }
 
         double radius = params.sizeFactor*0.5/Math.max(2.0,Math.sqrt((double)maxNum));
         
         double factor = 2.0*(double)maxCard /
                         ((double)params.numEdges*
                         Math.sin(2.0*Math.PI/(double)params.numEdges))/(radius*radius);
 
         VennObjectFactory factory = new VennObjectFactory();
         factory.setPolygonParameters( params.numEdges, factor );
         
         
         unfilteredArrangements = new VennArrangement[models.length];
         unfilteredViews = new IVennDiagramView[models.length];
         
         for( int i=0; i<models.length; ++i )
         {
         	models[i].setSucc(null); // because we make new VennArrangements
             unfilteredArrangements[i] = new VennArrangement( models[i], factory );
     		unfilteredArrangements[i].setParameters(params);
             VennDiagramView v = new VennDiagramView( unfilteredArrangements[i], 
                                                      params.errorFunction.maxIntersections, params.logNumElements );
             unfilteredViews[i] = v;
 //            add( v );
 //            v.addChangeListener( this );
         }
 	}
     
 
 	public boolean hasData()
 	{
 		return( sourceDataModel != null && sourceDataModel.getNumGroups() > 0 );
 	}
 	
 	public void setParameters(AllParameters params)
 	{		
 		this.params = params;
 		if( hasData() )
 		{
 			Assert.assertNotNull( sourceDataModel );
 			saveLabels();
 			saveManuallySetColors();
 			updateUnfiltered(); // perhaps color mode changed
 			update();
             fireChangeEvent();
 		}
 	}
 	
 	public AllParameters getParameters()
 	{
 		return params;
 	}
 	
 
 	/**
 	 * Notify all listeners about a state change
 	 *
 	 */
 	private synchronized void fireChangeEvent()
 	{
 		ChangeEvent event = new ChangeEvent(this);
 		
 		Iterator iter = changeListeners.iterator();
 		while(iter.hasNext())
 		{
 			((ChangeListener)iter.next()).stateChanged(event);
 		}
 	}
 	
 	public synchronized void addChangeListener(ChangeListener listener)
 	{
 		changeListeners.add( listener );
 	}
 	
 	public synchronized void addActionListeners(ActionListener listener)
 	{
 		actionListeners.add( listener );
 	}
 		
 	
 	/**
 	 * 
 	 * @return Information string with the number of keys/groups etc.
 	 */
 	public String getGlobalInfo()
 	{
 		if( sourceDataModel == null )
             return "";
 			
 		StringBuffer buf = new StringBuffer();
 		
 //		buf.append("elements : "+sourceDataModel.getNumElements()+"\n");
 //		buf.append("groups   : "+sourceDataModel.getNumGroups()+"\n");
 		buf.append("categories: "+sourceDataModel.getNumGroups()+"\n");
 		buf.append("elements: "+sourceDataModel.getNumElements()+"\n");
 		
 		return buf.toString();
 	}
 	
 	
     /*
     if( venn.saveAnimation )
     { // save snapshot
         // TODO : move configuration of this thing to the VennPanel.Parameters
         venn.saveSnapshotToFile("/tmp/venn-" + igen + "-" + df.format(animFrame) + ".jpg");
         ++animFrame;
     }
     */
     
 	public void saveSnapshotToFile(String file)
 	{ // save as JPEG
 		BufferedImage image = new BufferedImage(getWidth(),getHeight(),
 												BufferedImage.TYPE_3BYTE_BGR);
 									
 		Graphics g = image.getGraphics();
 		g.setColor(Color.WHITE); 
 		g.fillRect(0,0,image.getWidth(),image.getHeight());
 
 
         int minw = Math.min(image.getWidth(),image.getHeight());         
 		getViews()[0].directPaint(  g,
                                     new AffineTransformer(new FPoint(0,0), new FPoint(minw,minw) ));
                 
 									
 		try
 		{
 			FileOutputStream os = new FileOutputStream(file);
 			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
 			
 			JPEGEncodeParam jpegParam = JPEGCodec.getDefaultJPEGEncodeParam(image);
 			jpegParam.setQuality(1.0f,false);
 			encoder.encode(image,jpegParam);
 			os.close();
 		}
 		catch(IOException e)
 		{
 			JOptionPane.showMessageDialog(	this, 
 											"Error while writing file\r\n" + file,
 											"Error",
 											JOptionPane.ERROR_MESSAGE	);
 			return;					
 		}
 	}
     
 
 	/**
 	 * @return the number of categories
 	 */
 	public int numOfCategories()
 	{
         if( sourceDataModel == null )
             return 0;
         return sourceDataModel.getNumGroups();
 	}
 	
 	
 	/**
 	 * Sets the actual dataset to the given data model.
 	 * 
 	 * @param model
 	 * @see IVennDataModel
 	 */
 	public synchronized void setDataModel(IVennDataModel model)
 	{
 		clear();
 		clearUnfiltered();
 		labels.clear();
 		pathColors.clear();
 		
 	    if( sourceDataModel != null)
 	    {
 	    	sourceDataModel.setSucc(null);
 	    }
 	    sourceDataModel = model;
         
         
         manualFilter.reset();
 
         unfilteredDataSplitter.setDataModel(sourceDataModel);
         if (sourceDataModel != null) {
         	sourceDataModel.setSucc(null);
         }
         
         filteredModel.setDataModel( sourceDataModel, manualFilter );
         if (sourceDataModel != null) {
         	sourceDataModel.setSucc(null);
         }
 
         if (sourceDataModel != null) {
         	DuplFilterChainSucc dupl = new DuplFilterChainSucc(unfilteredDataSplitter, filteredModel);
         	sourceDataModel.setSucc(dupl);
         }
 
         dataSplitter.setDataModel( filteredModel );
         
         notifyVennPanelHasDataListeners();
         notifyHasLabelsChanged();
         
         setZoomLevel(zoomLevel);
 	}
 
 	public IVennDataModel getDataModel()
 	{
 	    return sourceDataModel;
 	}
 	
 
     public void stateChanged(ChangeEvent e) 
     {
 		assert e.getSource() instanceof VennDiagramView;
 
         updateUnfilteredFromFilteredSelection();
         updateCostValues();
         updateInconsistencyInfo();
 
 		fireChangeEvent();
     }
 
     private void updateUnfilteredFromFilteredSelection() {
     	BitSet fs = getFilteredSelection();
     	fs = filteredModel.localToGlobalGroupID(fs);
     	setUnfilteredSelection(fs);
     }
     
     private void updateFilteredFromUnfilteredSelection() {
     	BitSet fs = getUnfilteredSelection();
     	fs = filteredModel.globalToLocalGroupID(fs);
     	setFilteredSelection(fs);
     }
     
     /* (non-Javadoc)
      * @see venn.VennDiagramView#selectGroups(java.util.Set)
      */
     public void selectGroups(BitSet groups)
     {
         // TODO
     }
 
 
     /* (non-Javadoc)
      * @see venn.VennDiagramView#highlightGroups(java.util.Set)
      */
     public void highlightGroups(BitSet groups) 
     {
         // TODO Auto-generated method stub
         
     }
 
 
     /* (non-Javadoc)
      * @see venn.VennDiagramView#findGroups(java.awt.Point, java.awt.Rectangle)
      */
     public BitSet findGroups(Point p, Rectangle bounds) 
     {
         // TODO Auto-generated method stub
         return null;
     }
     
 
     private IVennDiagramView[] getViews() 
     {
         return views; 
     }
     
     private int getNumViews()
     {
         if( views == null )
             return 0;
         return views.length;
     }
     
     private String getInconsistencies() 
     {
         StringBuffer buf = new StringBuffer();
         if( views != null )
         {
             for( int i=0; i<views.length; ++i )
             {
                 buf.append( views[i].getInconsistencies() );
             }
         }
         return buf.toString();
     }
     
     public boolean existsFilteredVennObject(int unfilteredGid) {
     	if (! hasData()) {
     		return false;
     	}
     	
     	return ! manualFilter.getFiltered(unfilteredGid);
     }
     
     public IVennObject getUnfilteredVennObject( int unfilteredGid )
     {
       int idx = unfilteredDataSplitter.findModelByGroupID( unfilteredGid );
       int Lgid = unfilteredDataSplitter.getModels()[idx].globalToLocalGroupID( unfilteredGid );
       
       return unfilteredArrangements[idx].getVennObjects()[Lgid];
     }
     
     public Color getVennObjectColor(int unfilteredGid) {
         int idx = unfilteredDataSplitter.findModelByGroupID( unfilteredGid );
         int Lgid = unfilteredDataSplitter.getModels()[idx].globalToLocalGroupID( unfilteredGid );
 
         saveManuallySetColors();
     	for (Map.Entry<BitSet, Color> entr : pathColors.entrySet()) {
     		BitSet path = entr.getKey();
     		if (path.cardinality() != 1) continue;
     		if (path.get(Lgid)) {
     			return entr.getValue();
     		}
     	}
     	// no manual set color found, return standard color
         return unfilteredArrangements[idx].getVennObjects()[Lgid].getFillColor();
     }
     
     private IVennObject getFilteredVennObject( int unfilteredGid )
     {
       int idx = dataSplitter.findModelByGroupID( unfilteredGid );
       int Lgid = dataSplitter.getModels()[idx].globalToLocalGroupID( unfilteredGid );
       
       return arrangements[idx].getVennObjects()[Lgid];
     }
     
     public BitSet getUnfilteredSelection()
     {
         BitSet sel = new BitSet();
         if( unfilteredViews != null )
         {
             for( int i =0; i<unfilteredViews.length; ++i )
             {
                 sel.or( unfilteredDataSplitter.getModels()[i].localToGlobalGroupID(unfilteredViews[i].getSelectedGroups()) );
             }
         }
         return sel;
     }
     
     private BitSet getFilteredSelection()
     {
         BitSet sel = new BitSet();
         if( views != null )
         {
             for( int i =0; i<views.length; ++i )
             {
                 sel.or( dataSplitter.getModels()[i].localToGlobalGroupID(views[i].getSelectedGroups()) );
             }
         }
         return sel;
     }
     
     /**
      * 
      * @param unfilteredSet A set of global groupIDs.
      */
     public void setUnfilteredSelection(BitSet unfilteredSet) 
     {
         if( unfilteredSet == null )
             unfilteredSet = new BitSet();
         
         if( unfilteredViews != null )
         {
         	for( int i=0; i<unfilteredViews.length; ++i )
             {
                 unfilteredViews[i].selectGroups( unfilteredDataSplitter.getModels()[i].globalToLocalGroupID( unfilteredSet ) );
             }
         }
         updateFilteredFromUnfilteredSelection();
     }
     
     /**
      * 
      * @param unfilteredSet A set of global groupIDs.
      */
     private void setFilteredSelection(BitSet unfilteredSet) 
     {
         if( unfilteredSet == null )
             unfilteredSet = new BitSet();
         
         if( views != null )
         {
         	for( int i=0; i<views.length; ++i )
             {
                 views[i].selectGroups( dataSplitter.getModels()[i].globalToLocalGroupID( unfilteredSet ) );
             }
         }
     }
     
     /**
      * 
      * @return A string describing the currently selected groups.
      */
     public String getUnfilteredSelectionInfo() 
     {
     	StringBuffer buf = new StringBuffer();
 
     	if( unfilteredViews != null )
     	{
     		for( int i=0; i<unfilteredViews.length; ++i )
     		{
     			buf.append( unfilteredViews[i].getSelectionInfo() );
     		}
     	}
 
     	return buf.toString();
     }
     
 
     public void removeLabels() 
     {
     	labels.clear();
 
     	if (views != null) {
     		for( int i=0; i<views.length; ++i )
     		{
     			views[i].removeLabels();
     		}
     	}
     	
     	notifyHasLabelsChanged();
     }
     
     public boolean hasLabels() {
 
     	if (labels.size() > 0) {
     		return true;
     	}
     	
     	if (views == null) {
     		return false;
     	}
     	
     	for (IVennDiagramView view : views) {
     		if (view.hasLabels()) {
     			return true;
     		}
     	}
     	
     	return false;
     }
 
 //    @Override
     public synchronized void hasLabelsChanged() {
     	notifyHasLabelsChanged();
     }
     
     public synchronized void addHasLabelsListener(HasLabelsListener listener) {
     	if (hasLabelsListeners.contains(listener)) {
     		throw new IllegalStateException();
     	}
     	hasLabelsListeners.add(listener);
     }
     
     public synchronized void removeHasLabelsListener(HasLabelsListener listener) {
     	if (! hasLabelsListeners.contains(listener)) {
     		throw new IllegalStateException();
     	}
     	hasLabelsListeners.remove(listener);
     }
     
     private synchronized void notifyHasLabelsChanged() {
     	for (HasLabelsListener listener : hasLabelsListeners) {
     		listener.hasLabelsChanged();
     	}
     }
     
 
     /**
      * 
      * @param rowIndex
      * @return true if the given group is active.
      */
     public boolean getActivated(int rowIndex) 
     {
         if( manualFilter == null )
             return true;
         
         return !manualFilter.getFiltered( rowIndex );
     }
 
     /**
      * Activates/deactivates a group.
      * @param rowIndex
      * @param b
      */
     public void setActivated(int rowIndex, boolean b) 
     {
     	if (manualFilter.getFiltered(rowIndex) == !b) {
     		return;
     	}
     	
     	vennArrsOptim.stopForRestart();
 
     	saveLabels();
     	saveManuallySetColors();
     	manualFilter.setFiltered( rowIndex, !b );
 
     	vennArrsOptim.restart();
     }
 
 	/**
 	 * 
 	 */
 	private void saveLabels() {
 		if (views == null) {
 			return;
 		}
 		for (int i = 0; i < views.length; i++) {
     		((VennDiagramView) views[i]).removeLabelListeners();
     		
     		Component[] comps = ((VennDiagramView) views[i]).getComponents();
     		for (Component comp : comps) {
     			if (comp instanceof DragLabel) {
     				final DragLabel label = (DragLabel) comp;
     				final BitSet path = label.getPath();
     				final BitSet filteredModelPath = dataSplitter.getModels()[i].localToGlobalGroupID(path);
     				final BitSet sourceModelPath = filteredModel.localToGlobalGroupID(filteredModelPath);
     				
     				label.setPath(sourceModelPath);
     				label.setTransformer(null);
     				assert ! labels.contains(label);
     				labels.add(label);
     			}
     		}
     	}
 	}
 
 	private void saveManuallySetColors() {
 		if (views == null) {
 			return;
 		}
 		for (int i = 0; i < views.length; i++) {
 
 			Map<BitSet, Color> viewPathColors = ((VennDiagramView) views[i]).getManuallySetColors();
 			for (Map.Entry<BitSet, Color> entr : viewPathColors.entrySet()) {
 				final BitSet path = entr.getKey();
 				final Color color = entr.getValue();
 				final BitSet filteredModelPath = dataSplitter.getModels()[i].localToGlobalGroupID(path);
 				final BitSet sourceModelPath = filteredModel.localToGlobalGroupID(filteredModelPath);
 				this.pathColors.put(sourceModelPath, color);
 			}
 		}
 	}
 
     public void directPaint(Graphics g, Dimension dim)
     {
         if( views == null || views.length == 0 )
             return;
         
         int deltax = dim.width/views.length;
         
         for( int i=0; i<views.length; ++i )
         {
             AffineTransformer trans = 
                     new AffineTransformer(  new FPoint(i*deltax,0.0), 
                                             new FPoint(deltax,deltax));
             
             if( views[i] instanceof VennDiagramView )
             {
             		VennDiagramView vv = (VennDiagramView)views[i];
             		vv.setDoubleBuffered( false );
                  vv.paintComponent( g );
                  vv.setDoubleBuffered( true ); 
             }
             else
             {
             		views[i].directPaint( g, trans );
             } 
         }
     }
 
     //TODO
     public synchronized void directPaintsvg(Graphics g, Dimension dim, List<String> paintLog)
     {
         if( views == null || views.length == 0 )
             return;
         
         int deltax = dim.width/views.length;
         
         for( int i=0; i<views.length; ++i )
         {
             AffineTransformer trans = 
                     new AffineTransformer(  new FPoint(i*deltax,0.0), 
                                             new FPoint(deltax,deltax));
             
             if( views[i] instanceof VennDiagramView )
             {
             		VennDiagramView vv = (VennDiagramView)views[i];
             		vv.setDoubleBuffered( false );
                  vv.paintComponent( g, trans, paintLog );
                  vv.setDoubleBuffered( true ); 
             }
             else
             {
             		views[i].directPaint( g, trans );
             } 
         }
     }
 
     private void invalidateView() 
     {
         if( views == null )
             return;
         
         for( int i=0; i<views.length; ++i )
         {
             views[i].invalidateView();
         }
     }
 
     /**
      * Sets the zoom level of the Venn diagram viewer.
      * 
      * @param level
      */
 	public void setZoomLevel(int level)
 	{
 		if (level == -1) {
 			return;
 		}
 		zoomLevel = level;
 	
 		Dimension dim = new Dimension((400*Math.max(getNumViews(),1)*level)/100,(400*level)/100);
 
         setPreferredSize(dim);
         setSize(dim);
 		invalidate();
 	}
 
     /**
      * Writes the current Venn diagram to an SVG file.
      * 
      * @param os
      * @throws UnsupportedEncodingException 
      * @throws SVGGraphics2DIOException 
      */
 	public void writeSVGFile(OutputStream os,int width, int height) throws UnsupportedEncodingException, SVGGraphics2DIOException
     {
            // Get a DOMImplementation
         DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
 
         // Create an instance of org.w3c.dom.Document
         Document document = domImpl.createDocument(null, "svg", null);
 
         // Create an instance of the SVG Generator
         final SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
         int mx = Math.min(height,width);
         final Dimension dim = new Dimension(mx*getNumViews(),mx);
         
         svgGenerator.setSVGCanvasSize( dim );   
 
         final List<String> paintLog = new ArrayList<String>();
         
         // Ask the test to render into the SVG Graphics2D implementation
         if (SwingUtilities.isEventDispatchThread()) {
         	directPaintsvg(svgGenerator, dim, paintLog);
         } else {
         	try {
         		SwingUtilities.invokeAndWait(new Runnable() { // TODO necessary?
 //        			@Override
         			public void run() {
         				directPaintsvg( svgGenerator, dim, paintLog );
         			}
         		});
         	} catch (InterruptedException e) {
         		e.printStackTrace();
         		System.exit(1);
         	} catch (InvocationTargetException e) {
         		e.printStackTrace();
         		System.exit(1);
         	}
         }
 
         Element root = document.getDocumentElement();
         svgGenerator.getRoot(root);
         NodeList polygons = root.getElementsByTagNameNS("*", "polygon");
         assert polygons.getLength() >= paintLog.size();
         for (int i = 0; i < paintLog.size(); i++) {
         	if (i % 2 == 0) {
         		((Element) polygons.item(i)).setAttributeNS(null, "id", "fill:" + paintLog.get(i));
         	} else {
         		((Element) polygons.item(i)).setAttributeNS(null, "id", "outline:" + paintLog.get(i));
         	}
         }
         
         // Finally, stream out SVG to the standard output using UTF-8
         // character to byte encoding
         boolean useCSS = true; // we want to use CSS style attribute
         Writer out;
         out = new OutputStreamWriter(os, "UTF-8");
         svgGenerator.stream(root, out, useCSS);
     }
     
 	public void fileSave()
 	{
 		JFileChooser dialog = new JFileChooser();
 		CommonFileFilter filter;
 		
 		dialog.setAcceptAllFileFilterUsed(false);
 		
 		filter = new CommonFileFilter("JPEG Image (.jpg,.jpeg)");
 		filter.addExtension("jpg");
 		filter.addExtension("jpeg");
 		dialog.addChoosableFileFilter(filter);
 		
 		filter = new CommonFileFilter("SVG Image (.svg)");
 		filter.addExtension("svg");
 		dialog.addChoosableFileFilter(filter);		
 				
 		if( dialog.showSaveDialog(this) == JFileChooser.APPROVE_OPTION )
 		{
 			File file = dialog.getSelectedFile();
 			
 			String ext = CommonFileFilter.getExtension(file);
 			if( ext == null )
 			{
 				int idx=-1;
 				for(int i=0; idx<dialog.getChoosableFileFilters().length; ++i)
 				{
 					if( dialog.getChoosableFileFilters()[i] == dialog.getFileFilter() )
 					{
 						idx = i;
 						break;
 					}
 				}
 				if( idx >= 0 )
 				{
 					switch(idx)
 					{
 						case 0:
 							ext = "jpg";
 							break;
 						case 1:
 							ext = "svg";
 							break;
 						default:
 							ext = "jpg";
 					}
 					
 				}
 				else
 				{
 					ext = "jpg";
 				}
 				file = new File( file.getAbsolutePath() + "." + ext ); 
 			}
 			
 			if( file.exists() )
 			{
 				// overwrite file??
 				int res = JOptionPane.showConfirmDialog(this, "File '"+ file.getName().toString()
 						+"'already exists! Do you want to replace the existing file?", "", JOptionPane.YES_NO_OPTION);
 				if( res != JOptionPane.YES_OPTION )
 					return;
 			}
 			
 			// open output stream
 			FileOutputStream os;
 			String path = file.getAbsolutePath();
 			try 
 			{
 				os = new FileOutputStream(path);
 			}
 			catch(FileNotFoundException e)
 			{
 				JOptionPane.showMessageDialog(	this,
 												"Cannot open file\r\n"+path,
 												"Error",
 												JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			if( os == null )
 			{
 				JOptionPane.showMessageDialog(	this,
 						"Cannot open file\r\n"+path,
 						"Error",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 		
 			if( ext.compareToIgnoreCase("jpg") == 0 || ext.compareToIgnoreCase("jpeg") == 0 )
 			{ // save as JPEG
 				BufferedImage image = new BufferedImage(getWidth(),getHeight(),
 														BufferedImage.TYPE_3BYTE_BGR);
 											
 				Graphics g = image.getGraphics();
 				g.setColor(Color.WHITE); 
 				g.fillRect(0,0,image.getWidth(),image.getHeight());
 		
 				directPaint( g, new Dimension(image.getWidth(),image.getHeight()) );
 											
 				JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
 				
 				try
 				{
 					JPEGEncodeParam jpegParam = JPEGCodec.getDefaultJPEGEncodeParam(image);
 					jpegParam.setQuality(1.0f,false);
 					encoder.encode(image,jpegParam);
 					os.close();
 				}
 				catch(IOException e)
 				{
 					JOptionPane.showMessageDialog(	this, 
 													"Error while writing file\r\n"+path,
 													"Error",
 													JOptionPane.ERROR_MESSAGE	);
 					return;					
 				}
 			}
 			else
 			{ // write SVG file
			
                 try {
                     //writeSVGFile(os,venn.getWidth(),venn.getHeight());
                    writeSVGFile(os,getWidth(),getHeight());
                     os.close();
                 }
                 catch (UnsupportedEncodingException e) 
                 {
                     e.printStackTrace();
                     JOptionPane.showMessageDialog(  this,
                             "Cannot write file \r\n"+path,
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                     
                     return;
                 }                
                 catch (SVGGraphics2DIOException e) 
                 {
                     e.printStackTrace();
                     JOptionPane.showMessageDialog(  this,
                             "Error while creating SVG file\r\n"+path+"\r\n"+
                             e.getLocalizedMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);                 
                 } 
                 catch (IOException e) 
                 {
                     e.printStackTrace();
                     JOptionPane.showMessageDialog(  this,
                             "Error while creating SVG file\r\n"+path+"\r\n"+
                             e.getLocalizedMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);                                     
                 }
 			}
 		}
 	}
 	
     /**
      * Exports the error function profile to an ASCII file
      *
      */
     public void actionExportProfile()
     {
         JFileChooser dialog = new JFileChooser();
         CommonFileFilter filter;
         
         dialog.setAcceptAllFileFilterUsed(false);
         
         filter = new CommonFileFilter("Text File (.txt)");
         filter.addExtension("txt");
         
         dialog.addChoosableFileFilter(filter);
                         
         if( dialog.showSaveDialog(this) != JFileChooser.APPROVE_OPTION )
         {
             return;
         }
         
         File file = dialog.getSelectedFile();        
         if( file.exists() )
         {
             // overwrite file??
             int res = JOptionPane.showConfirmDialog(this,"File '"+ file.getName().toString()
             		+"'already exists! Do you want to replace the existing file?", "", JOptionPane.YES_NO_OPTION);
             if( res != JOptionPane.YES_OPTION )
                 return;
         }
             
         // open output stream
         FileWriter os = null;
 
         try 
         {
             os = new FileWriter(file);
         }
         catch(FileNotFoundException e)
         {
             JOptionPane.showMessageDialog(  this,
                                             "Cannot open file\r\n"+file.getAbsolutePath(),
                                             "Error",
                                             JOptionPane.ERROR_MESSAGE);
             return;
         } catch (IOException e) {
             e.printStackTrace();
             JOptionPane.showMessageDialog(  this,
                     "Cannot open file\r\n"+file.getAbsolutePath(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
             
         }
         if( os == null )
         {
             JOptionPane.showMessageDialog(  this,
                     "Cannot open file\r\n"+file.getAbsolutePath(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         try {
             writeProfile(os);
             os.close();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             JOptionPane.showConfirmDialog( this, "I/O error while saving file "+file.getAbsolutePath());
         }        
     }
     
     /**
      * Writes the profiles for all generations to a file.
      * @param os
      * @throws IOException 
      */
     public void writeProfile(OutputStreamWriter os) throws IOException
     {
         if( ! hasData() )
             return ;
         
         VennErrorFunction[] errFunc = this.vennArrsOptim.getErrFunc();
         IOptimizer[] optim = this.vennArrsOptim.getOptim();
         
         Assert.assertNotNull( errFunc );
         Assert.assertNotNull( optim );
         
         IVennDiagramView[] views = getViews();
        
         for( int i=0; i<errFunc.length; ++i )
         {
             os.write("SUB-PROBLEM "+i+"\n");
             
             // output group names
             IVennDataModel data = views[i].getArrangement().getDataModel();
             
             for( int j=0; j<data.getNumGroups(); ++j )
             {
                 os.write("GROUP["+j+"] ");
                 os.write(data.getGroupName(j));
                 os.write("\n");
             }
             
             double[] opt = optim[i].getOptimum();
             Assert.assertNotNull( opt );
             
             os.write("cost = "+(-optim[i].getValue()));
             
             errFunc[i].setInput( opt );
             
             os.write("\n\nPROFILE:\n");
             errFunc[i].writeProfile( os );
             os.write("\n\n");
         }
     }
 
 	/**
 	 * 
 	 */
 	private void restoreLabels() {
 		if (labels != null) {
 			VennFilteredDataModel[] models = dataSplitter.getModels();
 			for (int i = 0; i < models.length; i++) {
 				VennFilteredDataModel model = models[i];
 
 				final BitSet groups = model.getGroups();
 				final BitSet ggroups = filteredModel.localToGlobalGroupID(groups);
 
 				//search label
 				for (int k = 0; k < labels.size(); k++) {
 					DragLabel label = labels.get(k);
 					if (label == null) continue;
 
 					BitSet ggroupsAndLabelPath = (BitSet) ggroups.clone();
 					ggroupsAndLabelPath.and(label.getPath());
 					if (label.getPath().cardinality() == ggroupsAndLabelPath.cardinality()) {
 						//label found for this model (label path has no groups which don't exist in model)
 						
 						BitSet glabelPath = filteredModel.globalToLocalGroupID(label.getPath());
 						label.setPath(model.globalToLocalGroupID(glabelPath));
 
 						((VennDiagramView) views[i]).add(label);
 						labels.set(k, null);
 					}
 				}
 				((VennDiagramView) views[i]).labelsSetTransformerAndListeners();
 				((VennDiagramView) views[i]).validate();
 			}
     		while (labels.remove(null));
 		}
 		notifyHasLabelsChanged();
 	}
 
 	private void restoreManuallySetColors() {
 		if (pathColors != null) {
 			VennFilteredDataModel[] models = dataSplitter.getModels();
 			for (int i = 0; i < models.length; i++) {
 				VennFilteredDataModel model = models[i];
 
 				final BitSet groups = model.getGroups();
 				final BitSet ggroups = filteredModel.localToGlobalGroupID(groups);
 
 				Map<BitSet, Color> pathColorsForModel = new HashMap<BitSet, Color>();
 				//search color
 				for (Map.Entry<BitSet, Color> entr : pathColors.entrySet()) {
 					BitSet path = entr.getKey();
 					Color color = entr.getValue();
 
 					BitSet ggroupsAndColorPath = (BitSet) ggroups.clone();
 					ggroupsAndColorPath.and(path);
 					if (path.cardinality() == ggroupsAndColorPath.cardinality()) {
 						//color found for this model (color path has no groups which don't exist in model)
 						
 						assert color != null;
 						BitSet gcolorPath = filteredModel.globalToLocalGroupID(path);
 						pathColorsForModel.put(model.globalToLocalGroupID(gcolorPath), color);
 						entr.setValue(null);
 					}
 				}
 				((VennDiagramView) views[i]).setColors(pathColorsForModel);
 			}
 			Map<BitSet, Color> newPathColors = new HashMap<BitSet, Color>();
 			for (Map.Entry<BitSet, Color> entr : pathColors.entrySet()) {
 				if (entr.getValue() != null) {
 					newPathColors.put(entr.getKey(), entr.getValue());
 				}
 			}
 			pathColors = newPathColors;
 		}
 	}
 
 	/**
 	 * set colors from unfiltered VennObjects
 	 */
 	private void copyColorsFromUnfiltered() {
 		// set colors from unfiltered VennObjects (colors should not change if groups are deactivated)
 		if (unfilteredDataSplitter != null && unfilteredDataSplitter.getModels() != null
 				&& unfilteredDataSplitter.getModels().length > 0) {
 			for (int i = 0; i < filteredModel.getNumGroups(); i++) {
 				final int ggid = filteredModel.localToGlobalGroupID(i);
 				IVennObject unfiltered = getUnfilteredVennObject(ggid);
 				assert unfiltered != null;
 				getFilteredVennObject(i).setFillColor(unfiltered.getFillColor());
 			}
 		}
 	}
 	
 }
