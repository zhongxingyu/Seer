 package org.dawnsci.plotting.draw2d.swtxy;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.csstudio.swt.widgets.figureparts.ColorMapRamp;
 import org.csstudio.swt.xygraph.figures.Annotation;
 import org.csstudio.swt.xygraph.figures.Axis;
 import org.csstudio.swt.xygraph.figures.PlotArea;
 import org.csstudio.swt.xygraph.figures.Trace;
 import org.csstudio.swt.xygraph.undo.ZoomType;
 import org.dawb.common.ui.image.CursorUtils;
 import org.dawnsci.plotting.api.axis.ClickEvent;
 import org.dawnsci.plotting.api.axis.IAxis;
 import org.dawnsci.plotting.api.axis.IClickListener;
 import org.dawnsci.plotting.api.axis.ICoordinateSystem;
 import org.dawnsci.plotting.api.axis.IPositionListener;
 import org.dawnsci.plotting.api.axis.PositionEvent;
 import org.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.region.IRegionListener;
 import org.dawnsci.plotting.api.region.RegionEvent;
 import org.dawnsci.plotting.api.trace.IImageTrace;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.api.trace.ITraceListener;
 import org.dawnsci.plotting.api.trace.TraceEvent;
 import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
 import org.dawnsci.plotting.draw2d.swtxy.selection.SelectionRegionFactory;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.MouseListener;
 import org.eclipse.draw2d.MouseMotionListener;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.PaletteData;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RegionArea extends PlotArea {
 
 	private static final Logger logger = LoggerFactory.getLogger(RegionArea.class);
 	
 	protected ISelectionProvider                      selectionProvider;
 	private final Map<String,AbstractSelectionRegion> regions;
 	private final Map<String,ImageTrace>              imageTraces;
 	private       Map<String,VectorTrace>             vectorTraces;
 	
 	private Collection<IRegionListener>     regionListeners;
 	private Collection<ITraceListener>      imageTraceListeners;
 	private boolean                         containsMouse=false;
 
 	public RegionArea(XYRegionGraph xyGraph) {
 		super(xyGraph);
 		this.regions     = new LinkedHashMap<String,AbstractSelectionRegion>();
 		this.imageTraces = new LinkedHashMap<String,ImageTrace>();	
 		
 		addMouseMotionListener(new MouseMotionListener.Stub() {
 			@Override
 			public void mouseMoved(MouseEvent me) {
 				firePositionListeners(new PositionEvent(RegionArea.this, 
 						                               (AspectAxis)getRegionGraph().primaryXAxis,
 						                               (AspectAxis)getRegionGraph().primaryYAxis,
 													    me.x, 
 													    me.y));
 				createPositionCursor(me);
 			}
 			/**
 			 * @see org.eclipse.draw2d.MouseMotionListener#mouseEntered(MouseEvent)
 			 */
 			public void mouseEntered(MouseEvent me) {
 				containsMouse = true;
 			}
 
 			/**
 			 * @see org.eclipse.draw2d.MouseMotionListener#mouseExited(MouseEvent)
 			 */
 			public void mouseExited(MouseEvent me) {
 				containsMouse = false;
 			}
 			
 		});
 		
 		addMouseListener(new MouseListener() {
 
 			@Override
 			public void mousePressed(MouseEvent me) {
 				ClickEvent evt = createClickEvent(me);
 				fireClickListeners(evt);
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent me) {
 			}
 
 			@Override
 			public void mouseDoubleClicked(MouseEvent me) {
 				ClickEvent evt = createClickEvent(me);
 				fireDoubleClickListeners(evt);
 			}
 			
 		});
 
 	}
 	
 	
 
 	protected ClickEvent createClickEvent(MouseEvent me) {
 		
 		final double xVal   = getRegionGraph().getSelectedXAxis().getPositionValue(me.x);
 		final double yVal   = getRegionGraph().getSelectedYAxis().getPositionValue(me.y);
 		final int keyCode   = keyEvent!=null ? keyEvent.keyCode   : -1;
 		final int stateMask = keyEvent!=null ? keyEvent.stateMask : -1;
 		final char character= keyEvent!=null ? keyEvent.character : '\0';
 		
 		return new ClickEvent(this, getRegionGraph().getSelectedXAxis(), getRegionGraph().getSelectedYAxis(),
 				             xVal, yVal, isShiftDown(), isControlDown(), 
 				             keyCode, stateMask, character);
 				              
 	}
 
 	protected void firePositionListeners(PositionEvent positionEvent) {
 		if (positionListeners==null) return;
 		for (IPositionListener l : positionListeners) {
 			l.positionChanged(positionEvent);
 		}
 	}
 	
 	private Collection<IPositionListener> positionListeners;
 	public void addPositionListener(IPositionListener l) {
 		if (positionListeners==null) positionListeners = new HashSet<IPositionListener>();
 		positionListeners.add(l);
 	}
 	public void removePositionListener(IPositionListener l) {
 		if (positionListeners==null) return;
 		positionListeners.remove(l);
 	}
 	
 	private Collection<IClickListener> clickListeners;
 	public void addClickListener(IClickListener l) {
 		if (clickListeners==null) clickListeners = new HashSet<IClickListener>();
 		clickListeners.add(l);
 	}
 
 	public void removeClickListener(IClickListener l) {
 		if (clickListeners==null) return;
 		clickListeners.remove(l);
 	}
 
 	protected void fireClickListeners(ClickEvent evt) {
 		if (clickListeners==null) return;
 		for (IClickListener l : clickListeners) {
 			l.clickPerformed(evt);
 		}
 	}
 	protected void fireDoubleClickListeners(ClickEvent evt) {
 		if (clickListeners==null) return;
 		for (IClickListener l : clickListeners) {
 			l.doubleClickPerformed(evt);
 		}
 	}
 	
 	private Point   shiftPoint;
 	private Point   toPoint;
 	private boolean shiftDown;
 	private boolean controlDown;
 
 	@Override
 	protected void paintClientArea(final Graphics graphics) {
 		try {
 			super.paintClientArea(graphics);
 			
 			if (shiftPoint!=null && toPoint!=null && shiftDown && getSelectedCursor()!=null) {
 				graphics.pushState();
 				graphics.setForegroundColor(ColorConstants.white);
 				graphics.setLineDash(new int[]{1,1});
 				graphics.drawLine(shiftPoint.x, shiftPoint.y, toPoint.x, toPoint.y);
 				graphics.setLineDash(new int[]{2,2});
 				graphics.setForegroundColor(ColorConstants.black);
 				graphics.drawLine(shiftPoint.x, shiftPoint.y, toPoint.x, toPoint.y);
 				graphics.popState();
 			}
 		} catch (Throwable ne) {
 			logger.error("Internal error in drawing plot. Please contact your support representative.", ne);
 		}
 	}
 
 	public void setStatusLineManager(final IStatusLineManager statusLine) {
 		
 		if (statusLine==null) return;
 		
 		final NumberFormat format = new DecimalFormat("#0.0##");
 		addPositionListener(new IPositionListener() {
 
 			@Override
 			public void positionChanged(PositionEvent me) {
 				final IImageTrace trace = getImageTrace();
 				if (trace!=null) {
 					try {
 						double[] da = trace.getPointInAxisCoordinates(new double[]{me.x,me.y});
 						statusLine.setMessage(format.format(da[0])+", "+format.format(da[1]));
 						return;
 					} catch (Throwable ignored) {
                         // Normal position
 					}
 				}
 
 				statusLine.setMessage(format.format(me.x)+", "+format.format(me.y));
 			}
 		});
 	}
 	
 	private boolean requirePositionWithCursor=true;
     private Cursor positionCursor;
 	/**
 	 * Whenever cursor is NONE we show intensity info.
 	 * @param me
 	 */
 	protected void createPositionCursor(MouseEvent me) {
 		
 		if (!requirePositionWithCursor) return;
 		if (!containsMouse)  {
 			setCursor(null);
 			return;
 		}
 		if (getSelectedCursor()!=null) return;
 		if (zoomType!=ZoomType.NONE)   return;
 		
 
 		if (positionCursor!=null) positionCursor.dispose();
 		final IAxis  x = getRegionGraph().getSelectedXAxis();
 		final IAxis  y = getRegionGraph().getSelectedYAxis();
 		positionCursor = CursorUtils.getPositionCursor(me, x, y, getImageTrace());
 		setCursor(positionCursor);
 	}
 
 
 	public void addRegion(final AbstractSelectionRegion region) {
 		addRegion(region, true);
 	}
 
 	void addRegion(final AbstractSelectionRegion region, boolean fireListeners) {
 		regions.put(region.getName(), region);
 		region.setXyGraph(xyGraph);
 		region.createContents(this);
 		region.setSelectionProvider(selectionProvider);
 		if (fireListeners) fireRegionAdded(new RegionEvent(region));
 		clearRegionTool();
 		revalidate();
 	}
 
 	public boolean removeRegion(final AbstractSelectionRegion region) {
 		if (region==null) return false;
 	    final AbstractSelectionRegion gone = regions.remove(region.getName());
 		if (gone!=null){
 			gone.remove(); // Clears up children (you can live without this
 			fireRegionRemoved(new RegionEvent(gone));
 			revalidate();
 		}
 		return gone!=null;
 	}
 	
 	public void renameRegion(final AbstractSelectionRegion region, String name) {
 		
 		// Fix http://jira.diamond.ac.uk/browse/SCI-1056, do not loose order on rename		
 		final Map<String, AbstractSelectionRegion> sameOrder = new LinkedHashMap<String, AbstractSelectionRegion>(regions.size());
 
 		final Set<Entry<String,AbstractSelectionRegion>> entries = regions.entrySet();
 		for (Entry<String, AbstractSelectionRegion> entry : entries) {
 			
 			if (entry.getKey().equals(region.getName())) {
 			    region.setName(name);
 			    region.setLabel(name);
 			    sameOrder.put(name, region);				
 			} else {
 				sameOrder.put(entry.getKey(), entry.getValue());
 			}
 		}
 		regions.clear();
 		regions.putAll(sameOrder);
 	}
 	
 	public void clearRegions(boolean force) {
 		clearRegionsInternal(force);
 		revalidate();
 	}
 	
     protected void clearRegionsInternal(boolean force) {
 		clearRegionTool();
 		if (regions==null) return;
 		
 		final Collection<String> deleted = new HashSet<String>(5);
 		for (AbstractSelectionRegion region : regions.values()) {
 			if (!region.isUserRegion() && !force) continue;
 			deleted.add(region.getName());
 			region.remove();
 		}
 		regions.keySet().removeAll(deleted);
 		fireRegionsRemoved(new RegionEvent(this));
 
 	}
 	
 
 	public ImageTrace createImageTrace(String name, Axis xAxis, Axis yAxis, ColorMapRamp intensity) {
 
         if (imageTraces.containsKey(name)) throw new RuntimeException("There is an image called '"+name+"' already plotted!");
         
 		final ImageTrace trace = new ImageTrace(name, xAxis, yAxis, intensity);
 		fireImageTraceCreated(new TraceEvent(trace));
 		
 		return trace;
 	}
 	
 
 	public ImageStackTrace createImageStackTrace(String name, Axis xAxis, Axis yAxis, ColorMapRamp intensity) {
 		
         if (imageTraces.containsKey(name)) throw new RuntimeException("There is an image called '"+name+"' already plotted!");
         
 		final ImageStackTrace trace = new ImageStackTrace(name, xAxis, yAxis, intensity);
 		fireImageTraceCreated(new TraceEvent(trace));
 		
 		return trace;
 	}
 
 
 	/**Add a trace to the plot area.
 	 * @param trace the trace to be added.
 	 */
 	public void addImageTrace(final ImageTrace trace){
 		imageTraces.put(trace.getName(), trace);
 		add(trace);
 		
         toFront();		
 		revalidate();
 		
 		fireImageTraceAdded(new TraceEvent(trace));
 	}
 		
 	void toFront() {
 		for (Annotation a : getAnnotationList()) {
 			a.toFront();
 		}
 		// Move all regions to front again
 		if (regions!=null) for (String name : regions.keySet()) {
 			try {
 				regions.get(name).toFront();
 			} catch (Exception ne) {
 				continue;
 			}
 		}
 	}
 	
 	public boolean removeImageTrace(final ImageTrace trace){
 	    final ImageTrace gone = imageTraces.remove(trace.getName());
 		if (gone!=null){
 			trace.remove();
 			fireImageTraceRemoved(new TraceEvent(trace));
 			revalidate();
 		}
 		return gone!=null;
 	}
 	
 	/**Add a trace to the plot area.
 	 * @param trace the trace to be added.
 	 */
 	public void addVectorTrace(final VectorTrace trace){
 		
 		if (vectorTraces==null) vectorTraces = new LinkedHashMap<String, VectorTrace>();
 		vectorTraces.put(trace.getName(), trace);
 		add(trace);
 		
         toFront();		
 		revalidate();
 		
 		fireImageTraceAdded(new TraceEvent(trace));
 	}
 
 	/**Add a trace to the plot area.
 	 * @param trace the trace to be added.
 	 */
 	public void removeVectorTrace(final VectorTrace trace){
 		
 	    final VectorTrace gone = vectorTraces.remove(trace.getName());
 		if (gone!=null){
 			remove(trace);
 			
 	 		revalidate();
 			
 			fireImageTraceRemoved(new TraceEvent(trace));
 		}
 	}
 
 
 	public void clearImageTraces() {
 		if (imageTraces==null) return;
 		for (ImageTrace trace : imageTraces.values()) {
 			trace.remove();
 			fireImageTraceRemoved(new TraceEvent(trace));
 		}
 		imageTraces.clear();
 		revalidate();
 	}
 
 	
 	@Override
 	protected void layout() {
 		setFigureBounds(imageTraces);		
 		setFigureBounds(vectorTraces);		
         super.layout();
 	}
 		
     private void setFigureBounds(Map<String, ? extends Figure> traces) {
     	if (traces == null) return;
 	    final Rectangle clientArea = getClientArea();
     	for(Figure trace : traces.values()){
 			if(trace != null && trace.isVisible())
 				//Shrink will make the trace has no intersection with axes,
 				//which will make it only repaints the trace area.
 				trace.setBounds(clientArea);//.getCopy().shrink(1, 1));				
 		}
     }
 
 	RegionMouseListener regionListener;
     
     /**
      * Has to be set when plotting system is created.
      */
 	RegionCreationLayer               regionLayer;
 
 	/**
 	 * Create region of interest
 	 * @param name
 	 * @param xAxis
 	 * @param yAxis
 	 * @param regionType
 	 * @param startingWithMouseEvent
 	 * @return region
 	 * @throws Exception
 	 */
 	public AbstractSelectionRegion createRegion(String name, IAxis x, IAxis y, RegionType regionType, boolean startingWithMouseEvent) throws Exception {
 
 		if (regions!=null) {
 			if (regions.containsKey(name)) throw new Exception("The region '"+name+"' already exists.");
 		}
 		
 		ICoordinateSystem       coords  = new RegionCoordinateSystem(getImageTrace(), x, y);
 		AbstractSelectionRegion region  = SelectionRegionFactory.createSelectionRegion(name, coords, regionType);
 		if (startingWithMouseEvent) {
 			xyGraph.setZoomType(ZoomType.NONE);
 		    
 		    // Mouse listener for region bounds
 		    regionListener = new RegionMouseListener(regionLayer, this, region, region.getMinimumMousePresses(), region.getMaximumMousePresses());
 		    regionLayer.setMouseListenerActive(regionListener, true);
 		}
 
 		fireRegionCreated(new RegionEvent(region));
         return region;
 	}
 
 	public void setRegionLayer(RegionCreationLayer regionLayer) {
 		this.regionLayer = regionLayer;
 	}
 
 	public void disposeRegion(AbstractSelectionRegion region) {
 		removeRegion(region);
 		setCursor(null);
 		clearRegionTool();
 	}
 	
 	
 	protected void clearRegionTool() {
 		if (regionListener!=null) {
 		    regionLayer.setMouseListenerActive(regionListener, false);
 			IRegion wasBeingAdded = regionListener.getRegionBeingAdded();
 		    regionListener = null;
 		    setCursor(null);
 		    
 			if (wasBeingAdded!=null) {
 				fireRegionCancelled(new RegionEvent(wasBeingAdded));
 			}
 
 		}
 	}
 	
 	private Cursor specialCursor;
 	
 	public void setSelectedCursor(Cursor cursor) {
 		setZoomType(ZoomType.NONE);
 		setCursor(cursor);
 		specialCursor = cursor;
 	}
 	/**
 	 * Custom cursor if one set, or null
 	 * @return
 	 */
 	public Cursor getSelectedCursor() {
 		return specialCursor;
 	}
 	
 	private Cursor internalCursor;
 
 	public void setCursor(Cursor cursor) {
 		
 		try {
 			if (cursor!=null&&cursor.isDisposed()) cursor = null;
 			if (cursor!=null && this.internalCursor == cursor) return;
 			if (specialCursor!=null && !specialCursor.isDisposed()) {
 				cursor = specialCursor;
 			}
 
 			internalCursor = cursor;
 			if (cursor!=null&&cursor.isDisposed()) cursor = null;
 		    super.setCursor(cursor);
 		} catch (Throwable ignored) {
 			// Intentionally ignore bad cursors.
 		}
 	}
 
 	public void setZoomType(final ZoomType zoomType) {
 		specialCursor = null;
 		clearRegionTool();
 		super.setZoomType(zoomType);
 	}
 
 	/**
 	 * 
 	 * @param l
 	 */
 	public boolean addRegionListener(final IRegionListener l) {
 		if (regionListeners == null) regionListeners = new HashSet<IRegionListener>(7);
 		return regionListeners.add(l);
 	}
 	
 	/**
 	 * 
 	 * @param l
 	 */
 	public boolean removeRegionListener(final IRegionListener l) {
 		if (regionListeners == null) return true;
 		return regionListeners.remove(l);
 	}
 
 	protected void fireRegionCreated(RegionEvent evt) {
 		if (regionListeners==null) return;
 		for (IRegionListener l : regionListeners) {
 			try {
 				l.regionCreated(evt);
 			} catch (Throwable ne) {
 				logger.error("Notifying of region creation", ne);
 				continue;
 			}
 		}
 	}
 	
 	protected void fireRegionCancelled(RegionEvent evt) {
 		if (regionListeners==null) return;
 		for (IRegionListener l : regionListeners) {
 			try {
 				l.regionCancelled(evt);
 			} catch (Throwable ne) {
 				logger.error("Notifying of region add being cancelled", ne);
 				continue;
 			}
 		}
 	}
 
 	protected void fireRegionAdded(RegionEvent evt) {
 		if (regionListeners==null) return;
 		for (IRegionListener l : regionListeners) {
 			try {
 				l.regionAdded(evt);
 			} catch (Throwable ne) {
 				logger.error("Notifying of region add", ne);
 				continue;
 			}
 		}
 	}
 	
 	protected void fireRegionRemoved(RegionEvent evt) {
 		if (regionListeners==null) return;
 		for (IRegionListener l : regionListeners) {
 			try {
 				l.regionRemoved(evt);
 			} catch (Throwable ne) {
 				logger.error("Notifying of region removal", ne);
 				continue;
 			}
 		}
 	}
 	protected void fireRegionsRemoved(RegionEvent evt) {
 		if (regionListeners==null) return;
 		for (IRegionListener l : regionListeners) {
 			try {
 			    l.regionsRemoved(evt);
 			} catch (Throwable ne) {
 				logger.error("Notifying of region removal", ne);
 				continue;
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param l
 	 */
 	public boolean addImageTraceListener(final ITraceListener l) {
 		if (imageTraceListeners == null) imageTraceListeners = new HashSet<ITraceListener>(7);
 		return imageTraceListeners.add(l);
 	}
 	
 	/**
 	 * 
 	 * @param l
 	 */
 	public boolean removeImageTraceListener(final ITraceListener l) {
 		if (imageTraceListeners == null) return true;
 		return imageTraceListeners.remove(l);
 	}
 
 	
 	protected void fireImageTraceCreated(TraceEvent evt) {
 		if (imageTraceListeners==null) return;
 		for (ITraceListener l : imageTraceListeners) l.traceCreated(evt);
 	}
 	
 
 	protected void fireImageTraceAdded(TraceEvent evt) {
 		if (imageTraceListeners==null) return;
 		for (ITraceListener l : imageTraceListeners) l.traceAdded(evt);
 	}
 	
 	protected void fireImageTraceRemoved(TraceEvent evt) {
 		if (imageTraceListeners==null) return;
 		for (ITraceListener l : imageTraceListeners) l.traceRemoved(evt);
 	}
 	public List<AbstractSelectionRegion> getRegions() {
 		List<AbstractSelectionRegion> ret = new ArrayList<AbstractSelectionRegion>(regions.size());
 		for (String key : regions.keySet()) {
 			ret.add(regions.get(key));
 		}
 		return ret;
 	}
 
 	public Collection<String> getRegionNames() {
 		return regions.keySet();
 	}
 
 
 	public void setSelectionProvider(ISelectionProvider provider) {
 		this.selectionProvider = provider;
 	}
 
 
 	public AbstractSelectionRegion getRegion(String name) {
 		if (regions==null) return null;
 		return regions.get(name);
 	}
 
 	public Map<String,ImageTrace> getImageTraces() {
 		return this.imageTraces;
 	}
 
   
 	/**
 	 * Must call in UI thread safe way.
 	 */
 	public void clearTraces() {
 		
 		final List<Trace> traceList = getTraceList();
 		if (traceList!=null) {
 			for (Trace trace : traceList) {
 				remove(trace);
 				if (trace instanceof LineTrace) ((LineTrace)trace).dispose();
 			}
 			traceList.clear();
 	    }
 		
 		if (imageTraces!=null) {
 			final Collection<ImageTrace> its = new HashSet<ImageTrace>(imageTraces.values());
 			for (ImageTrace trace : its) {
 				final ImageTrace gone = imageTraces.remove(trace.getName());
 				if (gone!=null){
 					trace.remove();
 					fireImageTraceRemoved(new TraceEvent(trace));
 				}
 			}
 
 			imageTraces.clear();
 
 		}
 		
 		// Catch all needed for fix to http://jira.diamond.ac.uk/browse/SCI-1318
 		try {
 			List<IFigure> children = getChildren();
 			for (IFigure iFigure : children) {
 				if (iFigure instanceof ITrace) {
 					remove(iFigure);
 				}
 			}
 		} catch (java.util.ConcurrentModificationException ignored) {
 			// Then we don't loop
 		}
 
 	}
 
 
 	public void setPaletteData(PaletteData data) {
 		if (imageTraces!=null) for (ImageTrace trace : imageTraces.values()) {
 			trace.setPaletteData(data);
 		}
 	}
 
 
 	public void setImageOrigin(ImageOrigin origin) {
 		if (imageTraces!=null) for (ImageTrace trace : imageTraces.values()) {
 			trace.setImageOrigin(origin);
 		}
 	}
 
 
 	public ImageTrace getImageTrace() {
 		if (imageTraces!=null && imageTraces.size()>0) return imageTraces.values().iterator().next();
 		return null;
 	}
 
 	public void removeNotify() {
        super.removeNotify();
        dispose();
 	}
 	
 	public void dispose() {
 		
 		clearTraces();
 		clearRegionsInternal(true);
 		if (regionListeners!=null)     regionListeners.clear();
 		if (imageTraceListeners!=null) imageTraceListeners.clear();
 		if (regions!=null)             regions.clear();
 		if (imageTraces!=null)         imageTraces.clear();
		if (vectorTraces!=null)        {
			vectorTraces.clear();
		}
 	}
 
 	/**
 	 * Call to find out of any of the current regions are user editable.
 	 * @return
 	 */
 	public boolean hasUserRegions() {
 		if (regions==null || regions.isEmpty()) return false;
 		for (String regionName : regions.keySet()) {
 			if (regions.get(regionName).isUserRegion()) return true;
 		}
 		return false;
 	}
 
 	XYRegionGraph getRegionGraph() {
 		return (XYRegionGraph)xyGraph;
 	}
 
 	public boolean isRequirePositionWithCursor() {
 		return requirePositionWithCursor;
 	}
 
 	public void setRequirePositionWithCursor(boolean requirePositionWithCursor) {
 		this.requirePositionWithCursor = requirePositionWithCursor;
 	}
 
 
 	public Point getShiftPoint() {
 		if (!shiftDown) return null;
 		return shiftPoint;
 	}
 
 	private MouseMotionListener motionListener;
 	public void setShiftPoint(Point point) {
 		
 		this.shiftPoint = point;
 		
 		if (shiftPoint==null && motionListener!=null) {
 			removeMouseMotionListener(motionListener);
 			motionListener = null;
 			
 		} else if (shiftPoint!=null && motionListener==null) {
 			
 			motionListener = new MouseMotionListener.Stub() {
 				public void mouseExited(MouseEvent me) {
 					toPoint = null;
 				}
 				public void mouseMoved(MouseEvent me) {
 					toPoint = me.getLocation();
 					if (shiftPoint!=null && shiftDown){
 						repaint();
 					}
 				}
 			};
 			addMouseMotionListener(motionListener);
 		}
 	}
 
 	public boolean isShiftDown() {
 		return shiftDown;
 	}
 
 	public void setShiftDown(boolean shiftDown) {
 		this.shiftDown = shiftDown;
 		repaint();
 	}
 
 	public boolean isControlDown() {
 		return controlDown;
 	}
 
 	public void setControlDown(boolean controlDown) {
 		this.controlDown = controlDown;
 	}
 
 	private KeyEvent keyEvent;
 
 	public KeyEvent getKeyEvent() {
 		return keyEvent;
 	}
 
 	public void setKeyEvent(KeyEvent keyEvent) {
 		this.keyEvent = keyEvent;
 	}
 	
 }
