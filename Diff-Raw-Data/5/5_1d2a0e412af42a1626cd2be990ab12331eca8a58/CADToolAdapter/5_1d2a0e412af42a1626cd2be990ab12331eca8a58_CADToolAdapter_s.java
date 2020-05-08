 package com.iver.cit.gvsig.gui.cad;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.MemoryImageSource;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Stack;
 import java.util.prefs.Preferences;
 
 import org.cresques.cts.IProjection;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.andami.ui.mdiFrame.MainFrame;
 import com.iver.andami.ui.mdiManager.IWindow;
 import com.iver.andami.ui.mdiManager.MDIManager;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.EditionManager;
 import com.iver.cit.gvsig.FollowGeometryExtension;
 import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
 import com.iver.cit.gvsig.fmap.MapContext;
 import com.iver.cit.gvsig.fmap.MapControl;
 import com.iver.cit.gvsig.fmap.ViewPort;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
 import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
 import com.iver.cit.gvsig.fmap.core.v02.FConstant;
 import com.iver.cit.gvsig.fmap.core.v02.FConverter;
 import com.iver.cit.gvsig.fmap.edition.EditionEvent;
 import com.iver.cit.gvsig.fmap.edition.UtilFunctions;
 import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
 import com.iver.cit.gvsig.fmap.layers.FBitSet;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.fmap.layers.SpatialCache;
 import com.iver.cit.gvsig.fmap.tools.BehaviorException;
 import com.iver.cit.gvsig.fmap.tools.Behavior.Behavior;
 import com.iver.cit.gvsig.fmap.tools.Listeners.ToolListener;
 import com.iver.cit.gvsig.gui.cad.tools.SelectionCADTool;
 import com.iver.cit.gvsig.gui.preferences.SnapConfigPage;
 import com.iver.cit.gvsig.layers.ILayerEdited;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 import com.iver.cit.gvsig.project.documents.view.gui.View;
 import com.iver.cit.gvsig.project.documents.view.snapping.EIELFinalPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.EIELNearestPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.ISnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.ISnapperGeometriesVectorial;
 import com.iver.cit.gvsig.project.documents.view.snapping.ISnapperRaster;
 import com.iver.cit.gvsig.project.documents.view.snapping.ISnapperVectorial;
 import com.iver.cit.gvsig.project.documents.view.snapping.SnapperStatus;
 import com.iver.cit.gvsig.project.documents.view.toolListeners.StatusBarListener;
 import com.iver.utiles.console.JConsole;
 import com.vividsolutions.jts.geom.Envelope;
 
 /**
  * <p>Allows user interact with different CAD tools, on a layer being edited.</p>
  *
  * <p>There are two ways of interacting:
  *  <ul>
  *   <li><b>With the mouse</b> : user selects any {@link CADTool CADTool} that produces mouse events as consequence
  *    of the actions working with the layer being edited.
  *   </li>
  *   <li><b>Writing commands in the edition console</b> : most of the {@link CADTool CADTool} mouse actions can also
  *    be called writing a command or a command's parameter in the associated edition console, and pressing the key <code>Enter</code>.
  *    If the command isn't valid, will notify it.
  *   </li>
  *  </ul>
  * </p>
  *
  * <p>The edition has been implemented as a <i>finite machine</i>, with three kind of transitions between states according
  *  the parameters introduced:
  *  <ul>
  *   <li><i>First transition type: <b>Point</i></b>: if <code>text</code> matches with any pattern of
  *    parameters needed for any kind of point coordinates.<br>
  *    There are eight ways of introducing point 2D coordinates:
  *    <ul>
  *    <li><i>X,Y</i> : absolute cardinal 2D coordinate from the center <i>(0,0)</i> of the CCS <i>Current Coordinate System</i>.</li>
  *    <li><i>@X,Y</i> : relative cardinal 2D distances from the last point added of the CCS. If it's the first point of the geometry,
  *     works like <i>X,Y</i>.</li>
  *    <li><i>length< angle</i> : absolute polar 2D coordinate from the center <i>(0,0)</i> of the CCS <i>Current Coordinate System</i>, using
  *     <i>angle</i> from the <i>X</i> axis of CCS, and <i>length</i> far away.</li>
  *    <li><i>@length< angle</i> : relative polar 2D coordinate from the last point added of the CCS <i>Current Coordinate System</i>, using
  *     <i>angle</i> from the <i>X</i> axis of CCS, and <i>length</i> far away. If it's the first point of the geometry,
  *     works like <i>length< angle</i>.</li>
  *    <li><i>*X,Y</i> : like <i>X,Y</i> but using UCS <i>Universal Coordinate System</i> as reference.</li>
  *    <li><i>@*X,Y</i> : like <i>@X,Y</i> but using UCS <i>Universal Coordinate System</i> as reference.
  *      If it's the first point of the geometry, works like <i>*X,Y</i>.</li>
  *    <li><i>*length< angle</i> : like <i>length< angle</i> but using UCS <i>Universal Coordinate System</i> as reference.</li>
  *    <li><i>@*length< angle</i> : like <i>@length< angle</i> but using UCS <i>Universal Coordinate System</i> as reference.
  *      If it's the first point of the geometry, works like <i>*length< angle</i>.</li>
  *    </ul>
  *   </li>
  *   <li><i>Second transition type: <b>Value</i></b>: if recognizes it as a single number.</li>
  *   <li><i>Third transition type: <b>Option</i></b>: by default, if can't classify the information as a single number
  *    neither as a point. This information will be an <code>String</code> and dealt as an option of the current
  *    tool state. Ultimately, if isn't valid, <code>text</code> will be rewritten in the console notifying the user
  *    that isn't correct.</li>
  *  </ul>
  * </p>
  *
  * @see Behavior
  * @see MapControl
  */
 public class CADToolAdapter extends Behavior {
 	/**
 	 * Stores the CAD tools to edit the layers of the associated <code>MapControl</code>.
 	 *
 	 * @see #addCADTool(String, CADTool)
 	 * @see #getCadTool()
 	 * @see #getCADTool(String)
 	 */
 	private static HashMap namesCadTools = new HashMap();
 
 	/**
 	 * Reference to the object used to manage the edition of the layers of the associated <code>MapControl</code>.
 	 *
 	 * @see EditionManager
 	 * @see #getEditionManager()
 	 */
 	private EditionManager editionManager = new EditionManager();
 
 	/**
 	 * Identifies that the data are absolute coordinates of the new point from the (0, 0) position.
 	 */
 	public static final int ABSOLUTE = 0;
 
 	/**
 	 * Equivalent to {@link CADToolAdapter#ABSOLUTE CADToolAdapter#ABSOLUTE}.
 	 */
 	public static final int RELATIVE_SCP = 1;
 
 	/**
 	 * Identifies that the data are relative distances of the new point from the previous introduced.
 	 */
 	public static final int RELATIVE_SCU = 2;
 
 	/**
 	 * Identifies that the data are relative polar distances (longitude of the line and angle given in degrees)
 	 *  of the new point from the previous introduced.
 	 */
 	public static final int POLAR_SCP = 3;
 
 	/**
 	 * Identifies that the data are relative polar distances (longitude of the line and angle given in radians)
 	 *  of the new point from the previous introduced.
 	 */
 	public static final int POLAR_SCU = 4;
 
 	/**
 	 * Stores the 2D map coordinates of the last point added.
 	 */
 	private double[] previousPoint = null;
 
 	/**
 	 * <i>Stack with CAD tools.</i>
 	 *
 	 * <i>For each CAD tool we use, the last item added in this stack will
 	 *  display a different icon according to the current operation and its status.</i>
 	 */
 	private Stack cadToolStack = new Stack();
 
 	/**
 	 * X coordinate of the last dragging or moving mouse event.
 	 */
 	private int lastX;
 
 	/**
 	 * Y coordinate of the last dragging or moving mouse event.
 	 */
 	private int lastY;
 
 	/**
 	 * Unused attribute.
 	 */
 	private ISymbol symbol = SymbologyFactory.createDefaultSymbolByShapeType(FConstant.SYMBOL_TYPE_POINT, Color.RED);
 
 	/**
 	 * Represents the cursor's point selected in <i>map coordinates</i>.
 	 *
 	 * @see MapControl#toMapPoint
 	 */
 	private Point2D mapAdjustedPoint;
 
 	/**
 	 * Kind of geometry drawn to identify the kind of control point selected by the cursor's mouse.
 	 */
 	private ISnapper usedSnap = null;
 
 	/**
 	 * Determines if has displayed at the edition console, the question for the operations that can do
 	 *  the user with the current CAD tool, in its current state.
 	 */
 	private boolean questionAsked = false;
 
 	/**
 	 * Represents the cursor's point selected in <i>screen coordinates</i>.
 	 *
 	 * @see ViewPort#fromMapPoint(Point2D)
 	 */
 	private Point2D adjustedPoint;
 	
 	/**
 	 *  Point2D Array with points retrieved by  snappers
 	 */	
 	private ArrayList otherMapAdjustedPoints;
 
 	/**
 	 * Determines if the snap tools are enabled or disabled.
 	 *
 	 * @see #isRefentEnabled()
 	 * @see #setRefentEnabled(boolean)
 	 */
 	private boolean bRefent = true;
 
 	/**
 	 * <p>Determines if the position of the snap of the mouse's cursor on the <code>MapControl</code>
 	 * is within the area around a control point of a geometry.</p>
 	 *
 	 * <p>The area is calculated as a circle centered at the control point and with radius the pixels tolerance
 	 *  defined in the preferences.</p>
 	 */
 	private boolean bForceCoord = false;
 
 	/**
 	 * Optional grid that could be applied on the <code>MapControl</code>'s view port.
 	 *
 	 * @see #getGrid()
 	 * @see #setAdjustGrid(boolean)
 	 */
 	private CADGrid cadgrid = new CADGrid();
 
 	/**
 	 * Determines is is enabled or not the <i>Orto</i> mode.
 	 */
 	private boolean bOrtoMode;
 
 	/**
 	 * A light yellow color for the tool tip text box associated to the point indicated by the mouse's cursor.
 	 */
 	private Color theTipColor = new Color(255, 255, 155);
 
 	/**
 	 * Last question asked to the user in the CAD console.
 	 */
 	private Object lastQuestion;
 
 	/**
 	 * Maximum tolerance in the approximation of a curved line by a polyline.
 	 *
 	 * @see #initializeFlatness()
 	 */
 	private static boolean flatnessInitialized=false;
 
 	/**
 	 * Edition preferences.
 	 */
 	private static Preferences prefs = Preferences.userRoot().node( "cadtooladapter" );
 
 	/**
 	 * Listener to display the coordinates in the current application's status bar.
 	 */
 	private StatusBarListener sbl=null;
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#setMapControl(com.iver.cit.gvsig.fmap.MapControl)
 	 */
 	public void setMapControl(MapControl mc) {
 		super.setMapControl(mc);
 		sbl=new StatusBarListener(getMapControl());
 	}
 
 	/**
 	 * <p>Draws the selected geometries to edit. And, if the <i>snapping</i> is enabled,
 	 *  draws also its effect over them.</p>
 	 *
 	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#paintComponent(java.awt.Graphics)
 	 */
 	public void paintComponent(Graphics g) {
 	/*	super.paintComponent(g);
 		if (CADExtension.getCADToolAdapter()!=this)
 			return;
 
 		if (adjustedPoint != null) {
 			Point2D p = null;
 			if (mapAdjustedPoint != null) {
 				p = mapAdjustedPoint;
 			} else {
 				p = getMapControl().getViewPort().toMapPoint(adjustedPoint);
 			}
 			if (!cadToolStack.isEmpty())
 			((CADTool) cadToolStack.peek())
 					.drawOperation(g, p.getX(), p.getY());
 		}
 		drawCursor(g);
 		getGrid().drawGrid(g); */
 		super.paintComponent(g);
 		if (CADExtension.getCADToolAdapter() != this)
 			return;
		drawCursor(g);
		getGrid().drawGrid(g);
 		if (adjustedPoint != null) {
 			Point2D p = null;
 			if (mapAdjustedPoint != null) {
 				p = mapAdjustedPoint;
 			} else {
 				p = getMapControl().getViewPort().toMapPoint(adjustedPoint);
 			}
 
 			if (otherMapAdjustedPoints == null || otherMapAdjustedPoints.size() == 1) {
 				((CADTool) cadToolStack.peek()).drawOperation(g, p.getX(), p.getY());
 			} else {
 				// Calling to the special drawOperation with a list of points
 				((CADTool) cadToolStack.peek()).drawOperation(g, otherMapAdjustedPoints);
 				if (FollowGeometryExtension.isActivated()) {
 				    ((CADTool) cadToolStack.peek()).drawOperation(g, otherMapAdjustedPoints);
 				} else {
 				    ((CADTool) cadToolStack.peek()).drawOperation(g, p.getX(), p.getY());
 				}
 			}
 		}
 	}
 
 	/**
 	 * <p>Responds two kind of mouse click events:
 	 *  <ul>
 	 *   <li><b><i>One click of the third mouse's button</i></b>: displays a popup with edition options.</li>
 	 *   <li><b><i>Two clicks of the first mouse's button</i></b>: ends the last cad tool setting as end transition
 	 *    point the event's one.</li>
 	 *  </ul>
 	 * </p>
 	 *
 	 *
 	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#mouseClicked(java.awt.event.MouseEvent)
 	 * @see CADExtension#showPopup(MouseEvent)
 	 */
 	public void mouseClicked(MouseEvent e) throws BehaviorException {
 		if (e.getButton() == MouseEvent.BUTTON3) {
 			//CADExtension.showPopup(e);
 			boolean deleteButton3Option = prefs.getBoolean("isDeleteButton3", true);
 			if (deleteButton3Option) {
 				//TODO  if SHIFHT is pressed do:
 				// CADExtension.showPopup(e); 
 				transition(e);
 			}else { 
 				CADExtension.showPopup(e);
 			}
 		}else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()==2){
 			questionAsked = true;
 			if (!cadToolStack.isEmpty()) {
 				CADTool ct = (CADTool) cadToolStack.peek();
 				ViewPort vp = getMapControl().getMapContext().getViewPort();
 				Point2D p;
 
 				if (mapAdjustedPoint != null) {
 					p = mapAdjustedPoint;
 				} else {
 					p = vp.toMapPoint(adjustedPoint);
 				}
 				ct.endTransition(p.getX(), p.getY(), e);
 				previousPoint = new double[]{p.getX(),p.getY()};
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#mouseEntered(java.awt.event.MouseEvent)
 	 */
 	public void mouseEntered(MouseEvent e) throws BehaviorException {
 		clearMouseImage();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#mouseExited(java.awt.event.MouseEvent)
 	 */
 	public void mouseExited(MouseEvent e) throws BehaviorException {
 	}
 
 	/**
 	 * Selects the vertex of a geometry at the point selected on the <code>MapControl</code>
 	 * by pressing the first mouse's button.
 	 *
 	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#mousePressed(java.awt.event.MouseEvent)
 	 */
 	public void mousePressed(MouseEvent e) throws BehaviorException {
 		/*if (e.getButton() == MouseEvent.BUTTON1) {
 			ViewPort vp = getMapControl().getMapContext().getViewPort();
 			Point2D p;
 
 			if (mapAdjustedPoint != null) {
 				p = mapAdjustedPoint;
 			} else {
 				p = vp.toMapPoint(adjustedPoint);
 			}
 			transition(new double[] { p.getX(), p.getY() }, e, ABSOLUTE);
 		}*/
 		if (e.getButton() == MouseEvent.BUTTON1) {
 		    System.out.println("multi: " +
 			    getCadTool().isMultiTransition() + " follow: " +
 			    FollowGeometryExtension.isActivated());
 			if (otherMapAdjustedPoints == null
 					|| otherMapAdjustedPoints.size() == 0
 						|| !getCadTool().isMultiTransition()
 						|| !FollowGeometryExtension.isActivated()) {
 					
 
 				ViewPort vp = getMapControl().getMapContext().getViewPort();
 				Point2D p;
 
 				if (mapAdjustedPoint != null) {
 					p = mapAdjustedPoint;
 				} else {
 					p = vp.toMapPoint(adjustedPoint);
 				}
 				transition(new double[] { p.getX(), p.getY() }, e, ABSOLUTE);
 			} else {
 				
 				// Wait cursor, it can take long time...
 				MDIManager manager = PluginServices.getMDIManager();
 				manager.setWaitCursor();
 
 				// Do a transicion for each point
 				//y posiblemente haya que usar el swingworker??
 				for (int i = 0; i < otherMapAdjustedPoints.size(); i++) {
 					Point2D punto = (Point2D) otherMapAdjustedPoints.get(i);
 					transition(new double[] { punto.getX(), punto.getY() }, e,
 							ABSOLUTE);
 				}
 				// PluginServices.cancelableBackgroundExecution(new
 				// EvalOperatorsTask());
 				manager.restoreCursor();
 
 			}
 		}
 	}
 
 	/**
 	 * <p>Adjusts the <code>point</code> to the grid if its enabled, and
 	 *  sets <code>mapHandlerAdjustedPoint</code> with that new value.</p>
 	 *
 	 * <p>The value returned is the distance between those points: the original and
 	 *  the adjusted one.</p>
 	 *
 	 * @param point point to adjust
 	 * @param mapHandlerAdjustedPoint <code>point</code> adjusted
 	 *
 	 * @return distance from <code>point</code> to the adjusted one. If there is no
 	 *  adjustment, returns <code>Double.MAX_VALUE</code>.
 	 */
 	private double adjustToHandler(Point2D point,
             Point2D mapHandlerAdjustedPoint) {
 
         if (!isRefentEnabled())
             return Double.MAX_VALUE;
 
         ILayerEdited aux = CADExtension.getEditionManager().getActiveLayerEdited();
         if (!(aux instanceof VectorialLayerEdited))
             return Double.MAX_VALUE;
         VectorialLayerEdited vle = (VectorialLayerEdited) aux;
 
         ArrayList snappers = SnapConfigPage.getActivesSnappers();
         ArrayList layersToSnap = vle.getLayersToSnap();
 
 
         ViewPort vp = getMapControl().getViewPort();
 
 //        // TODO: PROVISIONAL. PONER ALGO COMO ESTO EN UN CUADRO DE DIALOGO
 //        // DE CONFIGURACIN DEL SNAPPING
 //        FinalPointSnapper defaultSnap = new FinalPointSnapper();
 //        NearestPointSnapper nearestSnap = new NearestPointSnapper();
 //        // PixelSnapper pixSnap = new PixelSnapper();
 //        snappers.clear();
 //        snappers.add(defaultSnap);
 //        snappers.add(nearestSnap);
 //        // snappers.add(pixSnap);
         EIELFinalPointSnapper eielFinalSnap = new EIELFinalPointSnapper();
 		EIELNearestPointSnapper eielNearestSnap = new EIELNearestPointSnapper();
 //		PixelSnapper pixSnap = new PixelSnapper();
 
 		snappers.clear();
 	SnapperStatus snapperStatus = SnapperStatus.getSnapperStatus();
 	if (snapperStatus.isNearLineActivated()) {
 			snappers.add(eielNearestSnap);
 		}
 	if (snapperStatus.isVertexActivated()) {
 			snappers.add(eielFinalSnap);
 		}
 //		snappers.add(pixSnap);
 		
         double mapTolerance = vp.toMapDistance(SelectionCADTool.tolerance);
         double minDist = mapTolerance;
 //        double rw = getMapControl().getViewPort().toMapDistance(5);
         Point2D mapPoint = point;
         Rectangle2D r = new Rectangle2D.Double(mapPoint.getX() - mapTolerance / 2,
                 mapPoint.getY() - mapTolerance / 2, mapTolerance, mapTolerance);
 
         Envelope e = FConverter.convertRectangle2DtoEnvelope(r);
 
         usedSnap = null;
         Point2D lastPoint = null;
         if (previousPoint != null)
         {
             lastPoint = new Point2D.Double(previousPoint[0], previousPoint[1]);
         }
         for (int j = 0; j < layersToSnap.size(); j++)
         {
             FLyrVect lyrVect = (FLyrVect) layersToSnap.get(j);
             SpatialCache cache = lyrVect.getSpatialCache();
             if (lyrVect.isVisible())
             {
                 // La lista de snappers est siempre ordenada por prioridad. Los de mayor
                 // prioridad estn primero.
 		// long t1 = System.currentTimeMillis();
                 List geoms = cache.query(e);
 		// long t2 = System.currentTimeMillis();
 		// System.out.println("T cache snapping = " + (t2-t1) + " numGeoms=" + geoms.size());
 		for (int i = 0; i < snappers.size(); i++) 
 		  {
 		       ISnapper theSnapper = (ISnapper) snappers.get(i);
 		       if (theSnapper instanceof ISnapperVectorial)
 		       {
 		             if (theSnapper instanceof ISnapperGeometriesVectorial){
 		                  ((ISnapperGeometriesVectorial)theSnapper).setGeometries(geoms);
 		             }
 		        }
 		   }   
 		if (snapperStatus.isVertexActivated()
 			|| snapperStatus.isNearLineActivated()
 			||
 			FollowGeometryExtension.isActivated())
                 for (int n=0; n < geoms.size(); n++) {
                     IGeometry geom = (IGeometry) geoms.get(n);
                     for (int i = 0; i < snappers.size(); i++)
                     {
 //                        if (cancel.isCanceled())
 //                            return Double.MAX_VALUE;
                         ISnapper theSnapper = (ISnapper) snappers.get(i);
 
                         if (usedSnap != null)
                         {
                             // Si ya tenemos un snap y es de alta prioridad, cogemos ese. (A no ser que en otra capa encontremos un snapper mejor)
                             //TODO : revisar si es > o <
                             if (theSnapper.getPriority() > usedSnap.getPriority())
                                 break;
                         }
 //                        SnappingVisitor snapVisitor = null;
                         Point2D theSnappedPoint = null;
                         if (theSnapper instanceof ISnapperVectorial)
                         {
 //                        	if (theSnapper instanceof ISnapperGeometriesVectorial){
 //                        		((ISnapperGeometriesVectorial)theSnapper).setGeometries(geoms);
 //                        	}
 //                            snapVisitor = new SnappingVisitor((ISnapperVectorial) theSnapper, point, mapTolerance, lastPoint);
 //                            // System.out.println("Cache size = " + cache.size());
 //                            cache.query(e, snapVisitor);
 //                            theSnappedPoint = snapVisitor.getSnapPoint();
 //                             long t3 = System.currentTimeMillis();
                             theSnappedPoint = ((ISnapperVectorial) theSnapper).getSnapPoint(point, geom, mapTolerance, lastPoint);
 			    //                            long t4 = System.currentTimeMillis();
 			    //                            System.out.println("Tiempo snapping " + theSnapper.getToolTipText() + " " + (t4-t3));
                         }
                         if (theSnapper instanceof ISnapperRaster)
                         {
                             ISnapperRaster snapRaster = (ISnapperRaster) theSnapper;
                             theSnappedPoint = snapRaster.getSnapPoint(getMapControl(), point, mapTolerance, lastPoint);
                         }
 
 
                         if (theSnappedPoint != null) {
                             double distAux = theSnappedPoint.distance(point);
                             if (minDist > distAux)
                             {
                                 minDist = distAux;
                                 usedSnap = theSnapper;
                                 mapHandlerAdjustedPoint.setLocation(theSnappedPoint);
                             }
                         }
                     }
                 } // for n
             } // visible
         }
         /*if (usedSnap != null)
             return minDist;
         return Double.MAX_VALUE; */
 
         if (usedSnap != null) {
 			otherMapAdjustedPoints = usedSnap.getSnappedPoints();
 			return minDist;
 		}
 		otherMapAdjustedPoints = null;
 		return Double.MAX_VALUE;
         
     }
 	/*
 	 * (non-Javadoc)
 	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#mouseReleased(java.awt.event.MouseEvent)
 	 */
 	public void mouseReleased(MouseEvent e) throws BehaviorException {
 		getMapControl().repaint();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#mouseDragged(java.awt.event.MouseEvent)
 	 */
 	public void mouseDragged(MouseEvent e) throws BehaviorException {
 		lastX = e.getX();
 		lastY = e.getY();
 
 		calculateSnapPoint(e.getPoint());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#mouseMoved(java.awt.event.MouseEvent)
 	 */
 	public void mouseMoved(MouseEvent e) throws BehaviorException {
 
 		lastX = e.getX();
 		lastY = e.getY();
 
 		calculateSnapPoint(e.getPoint());
 
 		showCoords(e.getPoint());
 
 		getMapControl().repaint();
 	}
 
 	/**
 	 * Displays the current coordinates of the mouse's cursor on the associated <code>MapControl</code>
 	 *  object, at the status bar of the application's main frame.
 	 *
 	 * @param pPix current 2D mouse's cursor coordinates on the <code>MapControl</code>
 	 */
 	private void showCoords(Point2D pPix)
 	{
 		String[] axisText = new String[2];
 		axisText[0] = "X = ";
 		axisText[1] = "Y = ";
 //		NumberFormat nf = NumberFormat.getInstance();
 		MapControl mapControl = getMapControl();
 		ViewPort vp = mapControl.getMapContext().getViewPort();
 		IProjection iProj = vp.getProjection();
 
 //		if (iProj.getAbrev().equals("EPSG:4326") || iProj.getAbrev().equals("EPSG:4230")) {
 //			axisText[0] = "Lon = ";
 //			axisText[1] = "Lat = ";
 //			nf.setMaximumFractionDigits(8);
 //		} else {
 //			axisText[0] = "X = ";
 //			axisText[1] = "Y = ";
 //			nf.setMaximumFractionDigits(2);
 //		}
 		Point2D p;
 		if (mapAdjustedPoint == null)
 		{
 			p = vp.toMapPoint(pPix);
 		}
 		else
 		{
 			p = mapAdjustedPoint;
 		}
 		sbl.setFractionDigits(p);
 		axisText = sbl.setCoorDisplayText(axisText);
 		MainFrame mF = PluginServices.getMainFrame();
 
 		if (mF != null)
 		{
             mF.getStatusBar().setMessage("units",
             		PluginServices.getText(this, MapContext.getDistanceNames()[vp.getDistanceUnits()]));
             mF.getStatusBar().setControlValue("scale",String.valueOf(mapControl.getMapContext().getScaleView()));
 			mF.getStatusBar().setMessage("projection", iProj.getAbrev());
 
 			String[] coords=sbl.getCoords(p);
 			mF.getStatusBar().setMessage("x",
 					axisText[0] + coords[0]);
 			mF.getStatusBar().setMessage("y",
 					axisText[1] + coords[1]);
 		}
 	}
 
 	/**
 	 * Hides the mouse's cursor.
 	 */
 	private void clearMouseImage() {
 		int[] pixels = new int[16 * 16];
 		Image image = Toolkit.getDefaultToolkit().createImage(
 				new MemoryImageSource(16, 16, pixels, 0, 16));
 		Cursor transparentCursor = Toolkit.getDefaultToolkit()
 				.createCustomCursor(image, new Point(0, 0), "invisiblecursor");
 
 		getMapControl().setCursor(transparentCursor);
 	}
        
        /**
         * Uses like a mouse pointer the image that provides the
         * selected tool.
         */
            private void setToolMouse(){
                    Image cursor = PluginServices.getIconTheme().get("cad-selection-icon").getImage();
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    Cursor c = toolkit.createCustomCursor(cursor , 
                                   new Point(16, 16), "img");
                    getMapControl().setCursor (c);
            }
 
 
 	/**
 	 * <p>Draws a 31x31 pixels cross round the mouse's cursor with an small geometry centered:
 	 *  <ul>
 	 *   <li><i>an square centered</i>: if isn't over a <i>control point</i>.
 	 *   <li><i>an small geometry centered according to the kind of control point</i>: if it's over a control
 	 *    point. In this case, the small geometry is drawn by a {@link ISnapper ISnapper} type object.<br>
 	 *    On the other hand, a light-yellowed background tool tip text with the type of <i>control point</i> will
 	 *     be displayed.</li>
 	 * </p>
 	 *
 	 * @param g <code>MapControl</code>'s graphics where the data will be drawn
 	 */
 	private void drawCursor(Graphics g) {
 		if (adjustedPoint == null){
 			return;
 		}
 		
 		if (usedSnap != null){
 			usedSnap.draw(g, adjustedPoint);
 			clearMouseImage();			
 		}else{
 			setToolMouse();
 		}
 	}
 
 	/**
 	 * <p>Tries to find the nearest geometry or grid control point by the position of the current snap tool.</p>
 	 *
 	 * <p>Prioritizes the grid control points than the geometries ones.</p>
 	 *
 	 * <p>If finds any near, stores the <i>map</i> and <i>pixel</i> coordinates for the snap, and enables
 	 *  the <code>bForceCoord</code> attribute for the next draw of the mouse's cursor.</p>
 	 *
 	 * @param point current mouse 2D position
 	 */
 	private void calculateSnapPoint(Point point) {
 		// Se comprueba el ajuste a rejilla
 
 		Point2D gridAdjustedPoint = getMapControl().getViewPort().toMapPoint(
 				point);
 		double minDistance = Double.MAX_VALUE;
 		if (!cadToolStack.isEmpty()){
 		CADTool ct = (CADTool) cadToolStack.peek();
 		if (ct instanceof SelectionCADTool
 				&& ((SelectionCADTool) ct).getStatus().equals(
 						"Selection.FirstPoint")) {
 			mapAdjustedPoint = gridAdjustedPoint;
 			adjustedPoint = (Point2D) point.clone();
 		} else {
 
 			minDistance = getGrid().adjustToGrid(gridAdjustedPoint);
 			if (minDistance < Double.MAX_VALUE) {
 				adjustedPoint = getMapControl().getViewPort().fromMapPoint(
 						gridAdjustedPoint);
 				mapAdjustedPoint = gridAdjustedPoint;
 			} else {
 				mapAdjustedPoint = null;
 			}
 		}
 		}
 		Point2D handlerAdjustedPoint = null;
 
 		// Se comprueba el ajuste a los handlers
 		if (mapAdjustedPoint != null) {
 			handlerAdjustedPoint = (Point2D) mapAdjustedPoint.clone(); // getMapControl().getViewPort().toMapPoint(point);
 		} else {
 			handlerAdjustedPoint = getMapControl().getViewPort().toMapPoint(
 					point);
 		}
 
 		Point2D mapPoint = new Point2D.Double();
 		double distance = adjustToHandler(handlerAdjustedPoint, mapPoint);
 
 		if (distance < minDistance) {
 			bForceCoord = true;
 			adjustedPoint = getMapControl().getViewPort().fromMapPoint(mapPoint);
 			mapAdjustedPoint = mapPoint;
 			minDistance = distance;
 		}
 
 		// Si no hay ajuste
 		if (minDistance == Double.MAX_VALUE) {
 			adjustedPoint = point;
 			mapAdjustedPoint = null;
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#mouseWheelMoved(java.awt.event.MouseWheelEvent)
 	 */
 	public void mouseWheelMoved(MouseWheelEvent e) throws BehaviorException {
 	}
 
 	/**
 	 * <p>Process the information written by the user about the next point coordinate, determining
 	 *  the kind of <i>transition</i> according the parameters written.</p>
 	 *
 	 * <p>After, invokes one of the three possible <i>transition</i> methods of the <i>finite machine</i> of
 	 *  edition:
 	 *  <ul>
 	 *   <li><i>First transition type: <b>Point</i></b>: if <code>text</code> matches with any pattern of
 	 *    parameters needed for any kind of point coordinates.<br>
 	 *    There are eight ways of introducing point 2D coordinates:
 	 *    <ul>
 	 *    <li><i>X,Y</i> : absolute cardinal 2D coordinate from the center <i>(0,0)</i> of the CCS <i>Current Coordinate System</i>.</li>
 	 *    <li><i>@X,Y</i> : relative cardinal 2D distances from the last point added of the CCS. If it's the first point of the geometry,
 	 *     works like <i>X,Y</i>.</li>
 	 *    <li><i>length< angle</i> : absolute polar 2D coordinate from the center <i>(0,0)</i> of the CCS <i>Current Coordinate System</i>, using
 	 *     <i>angle</i> from the <i>X</i> axis of CCS, and <i>length</i> far away.</li>
 	 *    <li><i>@length< angle</i> : relative polar 2D coordinate from the last point added of the CCS <i>Current Coordinate System</i>, using
 	 *     <i>angle</i> from the <i>X</i> axis of CCS, and <i>length</i> far away. If it's the first point of the geometry,
 	 *     works like <i>length< angle</i>.</li>
 	 *    <li><i>*X,Y</i> : like <i>X,Y</i> but using UCS <i>Universal Coordinate System</i> as reference.</li>
 	 *    <li><i>@*X,Y</i> : like <i>@X,Y</i> but using UCS <i>Universal Coordinate System</i> as reference.
 	 *      If it's the first point of the geometry, works like <i>*X,Y</i>.</li>
 	 *    <li><i>*length< angle</i> : like <i>length< angle</i> but using UCS <i>Universal Coordinate System</i> as reference.</li>
 	 *    <li><i>@*length< angle</i> : like <i>@length< angle</i> but using UCS <i>Universal Coordinate System</i> as reference.
 	 *      If it's the first point of the geometry, works like <i>*length< angle</i>.</li>
 	 *    </ul>
 	 *   </li>
 	 *   <li><i>Second transition type: <b>Value</i></b>: if recognizes it as a single number.</li>
 	 *   <li><i>Third transition type: <b>Option</i></b>: by default, if can't classify the information as a single number
 	 *    neither as a point. This information will be an <code>String</code> and dealt as an option of the current
 	 *    tool state. Ultimately, if isn't valid, <code>text</code> will be rewritten in the console notifying the user
 	 *    that isn't correct.</li>
 	 *  </ul>
 	 * </p>
 	 *
 	 * @param text command written by user in the edition's console
 	 */
 	public void textEntered(String text) {
 		if (text == null) {
 			transition(PluginServices.getText(this,"cancel"));
 		} else {
 			/*
 			 * if ("".equals(text)) { transition("aceptar"); } else {
 			 */
 			text = text.trim();
 			int type = ABSOLUTE;
 			String[] numbers = new String[1];
 			numbers[0] = text;
 			if (text.indexOf(",") != -1) {
 
 				numbers = text.split(",");
 				if (numbers[0].substring(0, 1).equals("@")) {
 					numbers[0] = numbers[0].substring(1, numbers[0].length());
 					type = RELATIVE_SCU;
 					if (numbers[0].substring(0, 1).equals("*")) {
 						type = RELATIVE_SCP;
 						numbers[0] = numbers[0].substring(1, numbers[0]
 								.length());
 					}
 				}
 			} else if (text.indexOf("<") != -1) {
 				type = POLAR_SCP;
 				numbers = text.split("<");
 				if (numbers[0].substring(0, 1).equals("@")) {
 					numbers[0] = numbers[0].substring(1, numbers[0].length());
 					type = POLAR_SCU;
 					if (numbers[0].substring(0, 1).equals("*")) {
 						type = POLAR_SCP;
 						numbers[0] = numbers[0].substring(1, numbers[0]
 								.length());
 					}
 				}
 			}
 
 			double[] values = null;
 
 			try {
 				if (numbers.length == 2) {
 					// punto
 					values = new double[] { Double.parseDouble(numbers[0]),
 							Double.parseDouble(numbers[1]) };
 					transition(values, null, type);
 				} else if (numbers.length == 1) {
 					// valor
 					values = new double[] { Double.parseDouble(numbers[0]) };
 					transition(values[0]);
 				}
 			} catch (NumberFormatException e) {
 				transition(text);
 			} catch (NullPointerException e) {
 				transition(text);
 			}
 			// }
 		}
 		getMapControl().repaint();
 	}
 
 	/**
 	 * If there are options related with the <code>CADTool</code> at the peek of the CAD tool stack,
 	 *  displays them as a popup.
 	 */
 	public void configureMenu() {
 		String[] desc = ((CADTool) cadToolStack.peek()).getDescriptions();
 		// String[] labels = ((CADTool)
 		// cadToolStack.peek()).getCurrentTransitions();
 		CADExtension.clearMenu();
 
 		for (int i = 0; i < desc.length; i++) {
 			if (desc[i] != null) {
 				CADExtension
 						.addMenuEntry(PluginServices.getText(this, desc[i]));// ,
 				// labels[i]);
 			}
 		}
 
 	}
 
 	/**
 	 * <p>One of the three kind of transaction methods of the <i>finite machine</i> of
 	 *  edition.</p>
 	 *
 	 * <p>This one deals <code>values</code> as two numbers that, according <code>type</code>
 	 *  calculate a new point 2D in the current layer edited in the associated <code>MapControl</code>.</p>
 	 *
 	 * <p>There are different ways of calculating the new point 2D coordinates, according the value of <code>type</code>, see
 	 *  {@link #textEntered(String) #textEntered(String)}.</p>
 	 *
 	 * <p>After applying the changes, updates the controls available for managing the current data.</p>
 	 *
 	 * @param values numbers needed to calculate the new point coordinates according <code>type</code>
 	 * @param event event which generated this invocation (a <code>MouseEvent</code> or a <code>KeyEvent</code>)
 	 * @param type kind of information that is <code>values</code>. According this parameter, will calculate the
 	 *  new point in a different way
 	 *
 	 * @see CADTool#transition(double, double, InputEvent)
 	 * @see #transition(double)
 	 * @see #transition(String)
 	 */
 	private void transition(double[] values, InputEvent event, int type) {
 		questionAsked = true;
 		if (!cadToolStack.isEmpty()) {
 			CADTool ct = (CADTool) cadToolStack.peek();
 
 			switch (type) {
 			case ABSOLUTE:
 				ct.transition(values[0], values[1], event);
 				previousPoint = values;
 				break;
 			case RELATIVE_SCU:
 				// Comprobar que tenemos almacenado el punto anterior
 				// y crear nuevo con coordenadas relativas a l.
 				double[] auxSCU = values;
 				if (previousPoint != null) {
 					auxSCU[0] = previousPoint[0] + values[0];
 					auxSCU[1] = previousPoint[1] + values[1];
 				}
 				ct.transition(auxSCU[0], auxSCU[1], event);
 
 				previousPoint = auxSCU;
 				break;
 			case RELATIVE_SCP:
 				// TODO de momento no implementado.
 				ct.transition(values[0], values[1], event);
 				previousPoint = values;
 				break;
 			case POLAR_SCU://Relativo
 				// Comprobar que tenemos almacenado el punto anterior
 				// y crear nuevo con coordenadas relativas a l.
 				double[] auxPolarSCU = values;
 				if (previousPoint != null) {
 					Point2D point = UtilFunctions.getPoint(new Point2D.Double(
 							previousPoint[0], previousPoint[1]), Math
 							.toRadians(values[1]), values[0]);
 					auxPolarSCU[0] = point.getX();
 					auxPolarSCU[1] = point.getY();
 					ct.transition(auxPolarSCU[0], auxPolarSCU[1], event);
 				} else {
 					Point2D point = UtilFunctions.getPoint(new Point2D.Double(
 							0, 0), Math.toRadians(values[1]), values[0]);
 					auxPolarSCU[0] = point.getX();
 					auxPolarSCU[1] = point.getY();
 					ct.transition(auxPolarSCU[0], auxPolarSCU[1], event);
 				}
 				previousPoint = auxPolarSCU;
 				break;
 			case POLAR_SCP://Absoluto
 				double[] auxPolarSCP = values;
 				if (previousPoint != null) {
 					Point2D point = UtilFunctions.getPoint(new Point2D.Double(
 							0, 0),  Math
 							.toRadians(values[1]), values[0]);
 					auxPolarSCP[0] = point.getX();
 					auxPolarSCP[1] = point.getY();
 					ct.transition(auxPolarSCP[0], auxPolarSCP[1], event);
 				} else {
 					Point2D point = UtilFunctions.getPoint(new Point2D.Double(
 							0, 0), values[1], values[0]);
 					auxPolarSCP[0] = point.getX();
 					auxPolarSCP[1] = point.getY();
 					ct.transition(auxPolarSCP[0], auxPolarSCP[1], event);
 				}
 				previousPoint = auxPolarSCP;
 				break;
 			default:
 				break;
 			}
 			askQuestion();
 		}
 		configureMenu();
 		PluginServices.getMainFrame().enableControls();
 	}
 
 	/**
 	 * <p>One of the three kind of transaction methods of the <i>finite machine</i> of
 	 *  edition.</p>
 	 *
 	 * <p>This one deals <code>value</code> as a single number used as a parameter for the current
 	 *    tool state. Ultimately, if isn't valid, <code>number</code> will be rewritten in the
 	 *    console notifying the user that isn't correct.</p>
 	 *
 	 * <p>After applying the changes, updates the controls available for managing the current data.</p>
 	 *
 	 * @param value value for the current tool state
 	 *
 	 * @see CADTool#transition(double)
 	 * @see #transition(double[], InputEvent, int)
 	 * @see #transition(String)
 	 */
 	private void transition(double value) {
 		questionAsked = true;
 		if (!cadToolStack.isEmpty()) {
 			CADTool ct = (CADTool) cadToolStack.peek();
 			ct.transition(value);
 			askQuestion();
 		}
 		configureMenu();
 		PluginServices.getMainFrame().enableControls();
 	}
 
 	/**
 	 * <p>One of the three kind of transaction methods of the <i>finite machine</i> of
 	 *  edition.</p>
 	 *
 	 * <p>This one deals <code>option</code> as an option of the current
 	 *    tool state. Ultimately, if isn't valid, <code>option</code> will be rewritten in the
 	 *    console notifying the user that isn't correct.</p>
 	 *
 	 * @param option option for the current tool state
 	 *
 	 * @see CADTool#transition(String)
 	 * @see #transition(double[], InputEvent, int)
 	 * @see #transition(double)
 	 */
 	public void transition(String option) {
 		questionAsked = true;
 		if (!cadToolStack.isEmpty()) {
 			CADTool ct = (CADTool) cadToolStack.peek();
 			try {
 				ct.transition(option);
 			} catch (Exception e) {
 				IWindow window = PluginServices.getMDIManager().getActiveWindow();
 
 				if (window instanceof View) {
 					((View)window).getConsolePanel().addText(
 							"\n" + PluginServices.getText(this, "incorrect_option")
 							+ " : " + option, JConsole.ERROR);
 				}
 			}
 			askQuestion();
 		}
 		configureMenu();
 		PluginServices.getMainFrame().enableControls();
 	}
 	
 	/**
 	 * [LBD] DOCUMENT ME!
 	 *
 	 * @param text
 	 *            DOCUMENT ME!
 	 * @param source
 	 *            DOCUMENT ME!
 	 * @param sel
 	 *            DOCUMENT ME!
 	 * @param values
 	 *            DOCUMENT ME!
 	 */
 	private void transition(InputEvent event) {
 		questionAsked = true;
 		if (!cadToolStack.isEmpty()) {
 			CADTool ct = (CADTool) cadToolStack.peek();
 			ct.transition(event);
 			askQuestion();
 		}
 		configureMenu();
 		PluginServices.getMainFrame().enableControls();
 	}
 
 	/**
 	 * Shows or hides a grid on the <code>ViewPort</code> of the associated <code>MapControl</code>.
 	 *
 	 * @param value <code>true</code> to make the grid visible; <code>false</code> to make it invisible
 	 */
 	public void setGridVisibility(boolean value) {
 		getGrid().setShowGrid(value);
 		if (getMapControl()!=null){
 			getGrid().setViewPort(getMapControl().getViewPort());
 			getMapControl().repaint();
 		}
 	}
 
 	/**
 	 * Sets the snap tools enabled or disabled.
 	 *
 	 * @param activated <code>true</code> to enable the snap tools; <code>false</code> to disable them
 	 *
 	 * @see #isRefentEnabled()
 	 */
 	public void setRefentEnabled(boolean activated) {
 		bRefent = activated;
 	}
 
 	/**
 	 * Determines if snap tools are enabled or disabled.
 	 *
 	 * @return <code>true</code> to enable the snap tools; <code>false</code> to disable them
 	 *
 	 * @see #setRefentEnabled(boolean)
 	 */
 	public boolean isRefentEnabled()
 	{
 		return bRefent;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.iver.cit.gvsig.fmap.tools.Behavior.Behavior#getListener()
 	 */
 	public ToolListener getListener() {
 		return new ToolListener() {
 			/**
 			 * @see com.iver.cit.gvsig.fmap.tools.Listeners.ToolListener#getCursor()
 			 */
 			public Cursor getCursor() {
 				return null;
 			}
 
 			/**
 			 * @see com.iver.cit.gvsig.fmap.tools.Listeners.ToolListener#cancelDrawing()
 			 */
 			public boolean cancelDrawing() {
 				return false;
 			}
 		};
 	}
 
 	/**
 	 * Returns the {@link CADTool CADTool} at the top of the stack without removing it from the CAD tool stack.
 	 *
 	 * @return the {@link CADTool CADTool} at the top of the stack
 	 *
 	 * @see #pushCadTool(CADTool)
 	 * @see #popCadTool()
 	 * @see #setCadTool(CADTool)
 	 */
 	public CADTool getCadTool() {
 		if (cadToolStack.isEmpty())
 			return null;
 		return (CADTool) cadToolStack.peek();
 	}
 
 	/**
 	 * <p>Pushes a {@link CADTool CADTool} onto the top of the CAD tool stack, and sets it as current.</p>
 	 *
 	 * @param cadTool CAD tool to enable as current
 	 *
 	 * @see #getCadTool()
 	 * @see #popCadTool()
 	 * @see #setCadTool(CADTool)
 	 */
 	public void pushCadTool(CADTool cadTool) {
 		cadToolStack.push(cadTool);
 		cadTool.setCadToolAdapter(this);
 		// cadTool.initializeStatus();
 		// cadTool.setVectorialAdapter(vea);
 		/*
 		 * int ret = cadTool.transition(null, editableFeatureSource, selection,
 		 * new double[0]);
 		 *
 		 * if ((ret & Automaton.AUTOMATON_FINISHED) ==
 		 * Automaton.AUTOMATON_FINISHED) { popCadTool();
 		 *
 		 * if (cadToolStack.isEmpty()) { pushCadTool(new
 		 * com.iver.cit.gvsig.gui.cad.smc.gen.CADTool());//new
 		 * SelectionCadTool());
 		 * PluginServices.getMainFrame().setSelectedTool("selection"); }
 		 *
 		 * askQuestion();
 		 *
 		 * getMapControl().drawMap(false); }
 		 */
 	}
 
 	/**
 	 * Removes the peek of the CAD tool stack.
 	 *
 	 * @see #pushCadTool(CADTool)
 	 * @see #getCadTool()
 	 * @see #setCadTool(CADTool)
 	 */
 	public void popCadTool() {
 		cadToolStack.pop();
 	}
 
 	/**
 	 * <p>Displays at the console associated to the current active view that's being edited, the question of the following
 	 *  operation that user can do with the current <code>CADTool</code>, only if it hasn't just answered.</p>
 	 *
 	 * <p>The format of the question will be according the following pattern:<br>
 	 *   "\n#"<i>{cadtool at CAD tool stack peek}</i>.getQuestion()">"
 	 * </p>
 	 */
 	public void askQuestion() {
 		CADTool cadtool = (CADTool) cadToolStack.peek();
 		/*
 		 * if (cadtool..getStatus()==0){
 		 * PluginServices.getMainFrame().addTextToConsole("\n"
 		 * +cadtool.getName()); }
 		 */
 		if (PluginServices.getMDIManager().getActiveWindow() instanceof View)
 		{
 			View vista = (View) PluginServices.getMDIManager().getActiveWindow();
 			String question=cadtool.getQuestion();
 			if (lastQuestion==null || !(lastQuestion.equals(question)) || questionAsked) {
 			vista.getConsolePanel().addText(
 					"\n" + "#" + question + " > ", JConsole.MESSAGE);
 			// ***PluginServices.getMainFrame().addTextToConsole("\n" +
 			// cadtool.getQuestion());
 			questionAsked = false;
 			}
 			lastQuestion=question;
 		}
 
 	}
 
 	/**
 	 * Empties the CAD tools stack and pushes <code>cadTool</code> in it.
 	 *
 	 * @param cadTool CAD tool to set at the peek of the stack
 	 *
 	 * @see #pushCadTool(CADTool)
 	 * @see #popCadTool()
 	 * @see #getCadTool()
 	 */
 	public void setCadTool(CADTool cadTool) {
 	CADTool previousTool = getCadTool();
 	if (previousTool != null && previousTool != cadTool) {
 	    previousTool.clear();
 	}
 		cadToolStack.clear();
 		pushCadTool(cadTool);
 		// askQuestion();
 	}
 
 	/**
 	 * <p>Removes all geometries selected in the associated <code>MapControl</code>.
 	 */
 	public void delete() {
 		ILayerEdited aux = CADExtension.getEditionManager().getActiveLayerEdited();
 		if (!(aux instanceof VectorialLayerEdited))
 			return;
 		VectorialLayerEdited vle = (VectorialLayerEdited) aux;
 		VectorialEditableAdapter vea = vle.getVEA();
 
 		vea.startComplexRow();
 		try {
 			FBitSet selection = vea.getSelection();
 			int[] indexesToDel = new int[selection.cardinality()];
 			int j = 0;
 			for (int i = selection.nextSetBit(0); i >= 0; i = selection
 					.nextSetBit(i + 1)) {
 				indexesToDel[j++] = i;
 				// /vea.removeRow(i);
 			}
 
 //			  ArrayList selectedRow = vle.getSelectedRow();
 //
 //			  int[] indexesToDel = new int[selectedRow.size()];
 //			  for (int i = 0;i < selectedRow.size(); i++) {
 //				  IRowEdited edRow = (IRowEdited) selectedRow.get(i);
 //				  indexesToDel[i] = vea.getInversedIndex(edRow.getIndex());
 //				  }
 //
 			for (int i = indexesToDel.length - 1; i >= 0; i--) {
 				vea.removeRow(indexesToDel[i], PluginServices.getText(this,
 						"deleted_feature"),EditionEvent.GRAPHIC);
 			}
 			System.out.println("clear Selection");
 			selection.clear();
 			vle.clearSelection(VectorialLayerEdited.NOTSAVEPREVIOUS);
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e.getMessage(),e);
 		} finally {
 			String description=PluginServices.getText(this,"remove_geometry");
 			vea.endComplexRow(description);
 		}
 
 
 		/*
 		 * if (getCadTool() instanceof SelectionCADTool) { SelectionCADTool
 		 * selTool = (SelectionCADTool) getCadTool(); selTool.clearSelection(); }
 		 */
 		refreshEditedLayer();
 	}
 	
 	/**
 	 * [LBD method] Elimina la feature de la lnea indicada
 	 */
 	public void delete(int index) {
 		ILayerEdited aux = CADExtension.getEditionManager()
 				.getActiveLayerEdited();
 		if (!(aux instanceof VectorialLayerEdited))
 			return;
 		VectorialLayerEdited vle = (VectorialLayerEdited) aux;
 		VectorialEditableAdapter vea = vle.getVEA();
 		try {
 			vea.removeRow(index, PluginServices
 					.getText(this, "deleted_feature"), EditionEvent.GRAPHIC);
 
 		} catch (ExpansionFileReadException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ReadDriverException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		refreshEditedLayer();
 	}
 
 	/**
 	 * @see CADGrid#setAdjustGrid(boolean)
 	 */
 	public void setAdjustGrid(boolean b) {
 		getGrid().setAdjustGrid(b);
 	}
 
 	/**
 	 * <p>Responds to actions of writing common key commands for all kind of CAD operations, enabling/disabling after
 	 *  the controls to manage the available information according the tool selected:
 	 *  <ul>
 	 *   <li><i>eliminar</i>: removes the geometries that are now selected.</li>
 	 *   <li><i>escape</i>: executes different actions according to the current CAD tool of the associated <code>MapControl</code>:
 	 *    <ul>
 	 *     <li>If the tool enabled is identified by <i>cadtooladapter</i>: empties the CAD tools stack, changing the current tool by
 	 *      a {@link SelectionCADTool SelectionCADTool}, which is identified by <i>_selection</i> and allows select features of the
 	 *      active vector layer of the associated <code>MapControl</code> instance. </li>
 	 *     <li>Otherwise, that means current associated <code>MapControl</code> instance isn't identified by "<i>cadtooladapter</i>",
 	 *      changes the enabled tool by the previous.</li>
 	 *    </ul>
 	 *   </li>
 	 *  </ul>
 	 * </p>
 	 *
 	 * @param actionCommand identifier of the key action command executed by the user
 	 *
 	 * @see SelectionCADTool
 	 * @see MapControl#setPrevTool()
 	 */
 	public void keyPressed(String actionCommand) {
 		if (CADExtension.getEditionManager().getActiveLayerEdited()== null) {
 			return;
 		}
 		if (actionCommand.equals("eliminar")) {
 			delete();
 		} else if (actionCommand.equals("escape")) {
 			if (getMapControl().getCurrentTool().equals("cadtooladapter")) {
 				CADTool ct = (CADTool) cadToolStack.peek();
 				ct.end();
 				cadToolStack.clear();
 				SelectionCADTool selCad = new SelectionCADTool();
 				selCad.init();
 				VectorialLayerEdited vle = (VectorialLayerEdited) CADExtension
 						.getEditionManager().getActiveLayerEdited();
 				try {
 					vle.clearSelection(VectorialLayerEdited.NOTSAVEPREVIOUS);
 				} catch (ReadDriverException e) {
 					NotificationManager.addError(e.getMessage(),e);
 				}
 
 				pushCadTool(selCad);
 				// getVectorialAdapter().getSelection().clear();
 
 				refreshEditedLayer();
 
 
 				PluginServices.getMainFrame().setSelectedTool("_selection");
 				// askQuestion();
 			} else {
 				getMapControl().setPrevTool();
 			}
 		}else if (actionCommand.equals("espacio")) {
 			CADTool ct = (CADTool) cadToolStack.peek();
 			try {
 				ct.transition(actionCommand);
 				askQuestion();
 				System.out.println("InsertionCADTool");
 //				if (ct instanceof InsertionCADTool) {
 //					if (((InsertionCADTool) ct).getFormState() == InsertionCADTool.FORM_ACCEPTED) {
 //						ct.transition(PluginServices.getText(this,
 //								"accept_form"));
 //						askQuestion();
 //					} else if (((InsertionCADTool) ct).getFormState() == InsertionCADTool.FORM_CANCELLED) {
 //						ct.transition(PluginServices.getText(this,
 //								"cancel_form"));
 //					}
 //				}
 
 					
 			} catch (Exception e) {
 				e.printStackTrace();
 				View vista = (View) PluginServices.getMDIManager()
 						.getActiveWindow();
 				vista.getConsolePanel().addText(
 						"\n" + PluginServices.getText(this, "incorrect_option")
 								+ " : " + actionCommand, JConsole.ERROR);
 			}
 		} else if ((actionCommand.equals("tab"))) { //NACHOV&& (!formOpened)) {
 			CADTool ct = (CADTool) cadToolStack.peek();
 			try {
 				ct.transition(actionCommand);
 				askQuestion();
 			} catch (Exception e) {
 				e.printStackTrace();
 				View vista = (View) PluginServices.getMDIManager()
 						.getActiveWindow();
 				vista.getConsolePanel().addText(
 						"\n" + PluginServices.getText(this, "incorrect_option")
 								+ " : " + actionCommand, JConsole.ERROR);
 
 			}
 		}
 
 		PluginServices.getMainFrame().enableControls();
 
 	}
 
 	/**
 	 * <p>Applies a lightweight repaint of the active layer being edited.</p>
 	 *
 	 * <p>All layers under it won't be drawn, only the upper one and whose are over that layer in the TOC.</p>
 	 *
 	 * @see MapControl#rePaintDirtyLayers()
 	 */
 	public void refreshEditedLayer()
 	{
 		ILayerEdited edLayer = CADExtension.getEditionManager().getActiveLayerEdited();
 		if (edLayer != null)
 		{
 //			edLayer.getLayer().setDirty(true);
 			getMapControl().rePaintDirtyLayers();
 		}
 
 	}
 
 	/**
 	 * Gets the {@link CADGrid CADGrid} that can be drawn on the <code>ViewPort</code> of the associated <code>MapControl</code>.
 	 *
 	 * @return reference to the <i>grid</i> that can be applied on the <code>ViewPort</code>
 	 *
 	 * @see #setGridVisibility(boolean)
 	 */
 	public CADGrid getGrid() {
 		return cadgrid;
 	}
 
 	/**
 	 * Determines if is enabled or not the <i>orto</i> mode.
 	 *
 	 * @return <code>true</code> if is enabled the <i>orto</i> mode; otherwise <code>false</code>
 	 *
 	 * @see #setOrtoMode(boolean)
 	 */
 	public boolean isOrtoMode() {
 		return bOrtoMode;
 	}
 
 	/**
 	 * Enables / disables the <i>orto</i> mode.
 	 *
 	 * @param b the desired value
 	 *
 	 * @see #isOrtoMode()
 	 */
 	public void setOrtoMode(boolean b) {
 		bOrtoMode = b;
 	}
 
 	/**
 	 * Associates and stores the specified name with the specified {@link CADTool CADTool}.
 	 *
 	 * @param name name of the tool
 	 * @param c CAD tool to interactuate editing the layers
 	 *
 	 * @see #getCADTools()
 	 * @see #getCADTool(String)
 	 */
 	public static void addCADTool(String name, CADTool c) {
 		namesCadTools.put(name, c);
 
 	}
 
 	/**
 	 * Gets all CAD tools available to edit layers with this tool listener.
 	 *
 	 * @return CAD tools available to edit layers with this tool listener
 	 *
 	 * @see #addCADTool(String, CADTool)
 	 * @see #getCADTool(String)
 	 */
 	public static CADTool[] getCADTools() {
 		return (CADTool[]) CADToolAdapter.namesCadTools.values().toArray(new CADTool[0]);
 	}
 
 	/**
 	 * Returns the {@link CADTool CADTool} to which the specified name is mapped.
 	 *
 	 * @param text name of the tool
 	 * @return the CAD tool whose associated name is to be returned
 	 *
 	 * @see #addCADTool(String, CADTool)
 	 * @see #getCADTools()
 	 */
 	public CADTool getCADTool(String text) {
 		CADTool ct = (CADTool) namesCadTools.get(text);
 		return ct;
 	}
 
 	/**
 	 * Gets the object used to manage the edition of the layers of the associated <code>MapControl</code>.
 	 *
 	 * @see EditionManager
 	 *
 	 * @return object used to manage the edition of the layers
 	 */
 	public EditionManager getEditionManager() {
 		return editionManager;
 	}
 
 	/**
 	 * <p>Initializes the <i>flatness</i> with the defined in preferences.</p>
 	 *
 	 * <p>The <i>flatness</i> is the maximum tolerance used to approximate curved lines in a <i>shape</i> by polylines.</p>
 	 * <p>The shapes doesn't support primitive like arcs neither other curved lines to draw their geometries, then for drawing any
 	 *  kind of this geometries the curved lines are drawn approximately by a polyline. And for doing more realistic that curves,
 	 *  is used the <i>flatness</i> parameter, that indicates that the difference between each arc and the straight segment that
 	 *  approximates it must be in the worse case, like the <i>flatness</i>.</p>
 	 *
 	 * @see FConverter#FLATNESS
 	 */
 	public void initializeFlatness() {
 		if (!flatnessInitialized){
 			flatnessInitialized=true;
 			Preferences prefs = Preferences.userRoot().node( "cadtooladapter" );
 			double flatness = prefs.getDouble("flatness",FConverter.FLATNESS);
 			FConverter.FLATNESS=flatness;
 		}
 	}
 
 	/**
 	 * <p>Updates the grid on the <code>ViewPort</code> of the associated <code>MapControl</code>
 	 *  object according the values in the {@link com.iver.cit.gvsig.gui.cad.CADToolAdapter.prefs.Preferences com.iver.cit.gvsig.gui.cad.CADToolAdapter.prefs.Preferences}.</p>
 	 *
 	 * <p>The preferences are:
 	 *  <ul>
 	 *   <li>Show/hide the grid.</li>
 	 *   <li>Adjust or not the grid.</li>
 	 *   <li>Horizontal ( X ) line separation.</li>
 	 *   <li>Vertical ( Y ) line separation.</li>
 	 *  </ul>
 	 * </p>
 	 */
 	public void initializeGrid(){
 		boolean showGrid = prefs.getBoolean("grid.showgrid",getGrid().isShowGrid());
 		boolean adjustGrid = prefs.getBoolean("grid.adjustgrid",getGrid().isAdjustGrid());
 
 		double dx = prefs.getDouble("grid.distancex",getGrid().getGridSizeX());
 		double dy = prefs.getDouble("grid.distancey",getGrid().getGridSizeY());
 
 		setGridVisibility(showGrid);
 		setAdjustGrid(adjustGrid);
 		getGrid().setGridSizeX(dx);
 		getGrid().setGridSizeY(dy);
 	}
 
 	/**
 	 * <p>Returns the type of the shape that's the current active and vector layer being edited.</p>
 	 *
 	 * @see FLyrVect#getShapeType()
 	 *
 	 * @return type of the shape that's the current active and vector layer being edited
 	 */
 	public int getActiveLayerType() {
 		int type=FShape.MULTI;
 		try {
 			type=((FLyrVect)CADExtension.getEditionManager().getActiveLayerEdited().getLayer()).getShapeType();
 		} catch (ReadDriverException e) {
 			NotificationManager.addError(e);
 		}
 		return type;
 	}
 	
 //	[LBD comment] con esto limpio el ultimo punto pulsado para reinicializar el seguimiento de
 //	los snappers
 	public void setPreviousPoint(double[] previousPoint) {
 		this.previousPoint = previousPoint;
 	}
 
 	public void setPreviousPoint(Point2D punto) {
 		double puntoPrevio[] = { punto.getX(), punto.getY() };
 		this.previousPoint = puntoPrevio;
 	}
 } // [eiel-gestion-conexiones]
