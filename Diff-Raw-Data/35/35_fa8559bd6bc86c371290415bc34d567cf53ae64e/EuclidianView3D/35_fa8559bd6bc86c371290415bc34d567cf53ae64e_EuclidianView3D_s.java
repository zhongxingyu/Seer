 package geogebra3D.euclidian3D;
 
 
 import geogebra.Matrix.GgbMatrix;
 import geogebra.Matrix.GgbMatrix4x4;
 import geogebra.Matrix.GgbMatrixUtil;
 import geogebra.Matrix.GgbVector;
 import geogebra.euclidian.Drawable;
 import geogebra.euclidian.DrawableND;
 import geogebra.euclidian.EuclidianConstants;
 import geogebra.euclidian.EuclidianController;
 import geogebra.euclidian.EuclidianViewInterface;
 import geogebra.euclidian.Hits;
 import geogebra.euclidian.Previewable;
 import geogebra.kernel.GeoElement;
 import geogebra.kernel.GeoFunctionNVar;
 import geogebra.kernel.GeoList;
 import geogebra.kernel.GeoPolygon;
 import geogebra.kernel.Kernel;
 import geogebra.kernel.View;
 import geogebra.kernel.kernelND.GeoLineND;
 import geogebra.kernel.kernelND.GeoPointND;
 import geogebra.kernel.kernelND.GeoQuadricND;
 import geogebra.kernel.kernelND.GeoRayND;
 import geogebra.kernel.kernelND.GeoSegmentND;
 import geogebra.main.Application;
 import geogebra3D.euclidian3D.opengl.PlotterCursor;
 import geogebra3D.euclidian3D.opengl.Renderer;
 import geogebra3D.euclidian3D.opengl.RendererFreezingPanel;
 import geogebra3D.kernel3D.GeoAxis3D;
 import geogebra3D.kernel3D.GeoConic3D;
 import geogebra3D.kernel3D.GeoCurveCartesian3D;
 import geogebra3D.kernel3D.GeoElement3D;
 import geogebra3D.kernel3D.GeoElement3DInterface;
 import geogebra3D.kernel3D.GeoLine3D;
 import geogebra3D.kernel3D.GeoPlane3D;
 import geogebra3D.kernel3D.GeoPlane3DConstant;
 import geogebra3D.kernel3D.GeoPoint3D;
 import geogebra3D.kernel3D.GeoPolygon3D;
 import geogebra3D.kernel3D.GeoQuadric3D;
 import geogebra3D.kernel3D.GeoRay3D;
 import geogebra3D.kernel3D.GeoSegment3D;
 import geogebra3D.kernel3D.GeoVector3D;
 import geogebra3D.kernel3D.Kernel3D;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.print.PageFormat;
 import java.awt.print.Printable;
 import java.awt.print.PrinterException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.TreeMap;
 
 import javax.swing.JPanel;
 
 
 /**
  * Class for 3D view
  * @author matthieu
  *
  */
 public class EuclidianView3D extends JPanel implements View, Printable, EuclidianConstants, EuclidianViewInterface {
 
 	
 
 	private static final long serialVersionUID = -8414195993686838278L;
 	
 	
 	
 	//private Kernel kernel;
 	private Kernel3D kernel3D;
 	protected Application app;
 	private EuclidianController3D euclidianController3D;
 	private Renderer renderer;
 	private RendererFreezingPanel freezingPanel;
 	
 	//viewing values
 	private double XZero = 0;
 	private double YZero = 0;
 	private double ZZero = 0;
 	
 	private double XZeroOld = 0;
 	private double YZeroOld = 0;
 	
 	//list of 3D objects
 	private boolean waitForUpdate = true; //says if it waits for update...
 	//public boolean waitForPick = false; //says if it waits for update...
 	private Drawable3DLists drawable3DLists;// = new DrawList3D();
 	/** list for drawables that will be added on next frame */
 	private LinkedList<Drawable3D> drawable3DListToBeAdded;// = new DrawList3D();
 	/** list for drawables that will be removed on next frame */
 	private LinkedList<Drawable3D> drawable3DListToBeRemoved;// = new DrawList3D();
 	
 	// Map (geo, drawable) for GeoElements and Drawables
 	private TreeMap<GeoElement,Drawable3D> drawable3DMap = new TreeMap<GeoElement,Drawable3D>();
 	
 	//matrix for changing coordinate system
 	private GgbMatrix4x4 m = GgbMatrix4x4.Identity(); 
 	private GgbMatrix4x4 mInv = GgbMatrix4x4.Identity();
 	private GgbMatrix4x4 undoRotationMatrix = GgbMatrix4x4.Identity();
 	private double a = 0;
 	private double b = 0;//angles (in degrees)
 	private double aOld, bOld;
 	private double aNew, bNew;
 	
 	
 
 	
 
 	//picking and hits
 	private Hits3D hits = new Hits3D(); //objects picked from openGL
 	
 	//base vectors for moving a point
 	/** origin */
 	static public GgbVector o = new GgbVector(new double[] {0.0, 0.0, 0.0,  1.0});
 	/** vx vector */
 	static public GgbVector vx = new GgbVector(new double[] {1.0, 0.0, 0.0,  0.0});
 	/** vy vector */
 	static public GgbVector vy = new GgbVector(new double[] {0.0, 1.0, 0.0,  0.0});
 	/** vz vector */
 	static public GgbVector vz = new GgbVector(new double[] {0.0, 0.0, 1.0,  0.0});
 	
 	/** direction of view */
 	private GgbVector viewDirection = vz.copyVector();
 
 	
 	//axis and xOy plane
 	private GeoPlane3D xOyPlane;
 	private GeoAxis3D[] axis;
 	
 	private DrawPlane3D xOyPlaneDrawable;
 	private DrawAxis3D[] axisDrawable;
 	
 	
 	/** number of drawables linked to this view (xOy plane, Ox, Oy, Oz axis) */
 	static final public int DRAWABLES_NB = 4;
 	/** id of z-axis */
 	static final int AXIS_Z = 2; //AXIS_X and AXIS_Y already defined in EuclidianViewInterface
 
 	//point decorations	
 	private DrawPointDecorations pointDecorations;
 	private boolean decorationVisible = false;
 
 	//preview
 	private Previewable previewDrawable;
 	private GeoPoint3D cursor3D;
 	private GeoElement[] cursor3DIntersectionOf = new GeoElement[2]; 
 	
 	//cursor
 	/** no point under the cursor */
 	public static final int PREVIEW_POINT_NONE = 0;
 	/** free point under the cursor */
 	public static final int PREVIEW_POINT_FREE = 1;
 	/** path point under the cursor */
 	public static final int PREVIEW_POINT_PATH = 2;
 	/** region point under the cursor */
 	public static final int PREVIEW_POINT_REGION = 3;
 	/** dependent point under the cursor */
 	public static final int PREVIEW_POINT_DEPENDENT = 4;
 	/** already existing point under the cursor */
 	public static final int PREVIEW_POINT_ALREADY = 5;
 	
 	
 	
 	private int cursor3DType = PREVIEW_POINT_NONE;
 
 	
 	private static final int CURSOR_DEFAULT = 0;
 	private static final int CURSOR_DRAG = 1;
 	private static final int CURSOR_MOVE = 2;
 	private static final int CURSOR_HIT = 3;
 	private int cursor = CURSOR_DEFAULT;
 	private boolean cursor3DVisible = true;
 	
 
 	//mouse
 	private boolean hasMouse = false;
 	
 	
 	
 	// animation
 	
 	/** tells if the view is under animation for scale */
 	private boolean animatedScale = false;
 	/** starting and ending scales */
 	private double animatedScaleStart, animatedScaleEnd;
 	/** velocity of animated scaling */
 	private double animatedScaleTimeFactor;
 	/** starting time for animated scale */
 	private long animatedScaleTimeStart;
 	
 	
 	/** tells if the view is under continue animation for rotation */
 	private boolean animatedContinueRot = false;
 	/** speed for animated rotation */
 	private double animatedRotSpeed;
 	/** starting time for animated rotation */
 	private long animatedRotTimeStart;
 	
 	/** tells if the view is under animation for rotation */
 	private boolean animatedRot = false;
 
 	
 	
 	
 	
 	
 	/** says if the view is frozen (see freeze()) */
 	private boolean isFrozen = false;
 	
 	
 	
 	/**  selection rectangle  TODO */
 	protected Rectangle selectionRectangle = new Rectangle();
 
 
 	
 	/**
 	 * common constructor
 	 * @param ec controller on this
 	 */
 	public EuclidianView3D(EuclidianController3D ec){
 
 		
 
 		
 		this.euclidianController3D = ec;
 		this.kernel3D = (Kernel3D) ec.getKernel();
 		euclidianController3D.setView(this);
 		app = ec.getApplication();	
 		
 		start();
 	}
 	
 	
 	public Application getApplication() {
 		return app;
 	}
 	
 	private void start(){
 		
 		drawable3DLists = new Drawable3DLists(this);
 		drawable3DListToBeAdded = new LinkedList<Drawable3D>();
 		drawable3DListToBeRemoved = new LinkedList<Drawable3D>();
 		
 		
 		//TODO replace canvas3D with GLDisplay
 		renderer = new Renderer(this);
 		renderer.setDrawable3DLists(drawable3DLists);
 		
 		
 
 		freezingPanel = new RendererFreezingPanel(renderer);
 		//add(BorderLayout.CENTER, freezingPanel);
 		freezingPanel.setVisible(true);
 		
         Canvas canvas = renderer.canvas;
 		
 
         
 
 		setLayout(new BorderLayout());
 		//add(BorderLayout.CENTER, canvas);
 		addRendererCanvas();
 
 		
 		
 		attachView();
 		
 		// register Listener
 		canvas.addMouseMotionListener(euclidianController3D);
 		canvas.addMouseListener(euclidianController3D);
 		canvas.addMouseWheelListener(euclidianController3D);
 		canvas.setFocusable(true);
 		
 		
 		
 		//previewables
 		kernel3D.setSilentMode(true);
 		cursor3D = (GeoPoint3D) kernel3D.getManager3D().Point3D(null, 1, 1, 0);
 		cursor3D.setIsPickable(false);
 		//cursor3D.setLabelOffset(5, -5);
 		cursor3D.setEuclidianVisible(false);
 		kernel3D.setSilentMode(false);
 		
 		
 		
 		
 		initAxisAndPlane();
 		
 		//point decorations
 		initPointDecorations();
 		
 		
 		
 	}
 	
 	
 	private void addRendererCanvas(){
 		Canvas canvas = renderer.canvas;
 
 		//setLayout(new BorderLayout());
 		add(BorderLayout.CENTER, canvas);
 
 		/*
 		attachView();
 
 		// register Listener
 		canvas.addMouseMotionListener(euclidianController3D);
 		canvas.addMouseListener(euclidianController3D);
 		canvas.addMouseWheelListener(euclidianController3D);
 		canvas.setFocusable(true);
 		*/
 	}
 	
 	
 	/**
 	 * causes the 3D view to freeze rendering, and replace the
 	 * heavy-weight 3D canvas by a low-weight image
 	 * @param sw if false, calls back the 3D rendering
 	 */
 	public void setToFrozen(boolean sw){
 		
 		isFrozen = sw;
 
 		if (!sw){
 			//switch off the frozen image
 			remove(freezingPanel);
 			freezingPanel.setVisible(false);
 			
 			//switch on the renderer canvas
 			addRendererCanvas();
 			renderer.canvas.setVisible(true);
 			renderer.canvas.setSize(getSize());
 			
 		}else{			
 			//ask for an image
 			renderer.needExportImage();
 			
 			//switch off the renderer canvas
 			remove(renderer.canvas);
 			renderer.canvas.setVisible(false);
 			
 			//switch on the frozen image
 			add(BorderLayout.CENTER, freezingPanel);
 			freezingPanel.setVisible(true);
 			freezingPanel.setSize(getSize());
 			
 		}
 		
 	}
 	
 	
 	
 	/**
 	 * init the axis and xOy plane
 	 */
 	public void initAxisAndPlane(){
 		
 		
 
 
 		//axis
 		axis = new GeoAxis3D[3];
 		axisDrawable = new DrawAxis3D[3];
 		axis[0] = kernel3D.getXAxis3D();
 		axis[1] = kernel3D.getYAxis3D();
 		axis[2] = kernel3D.getZAxis3D();
 		
 		
 		for(int i=0;i<3;i++){
 			axis[i].setLabelVisible(true);
 			axisDrawable[i] = (DrawAxis3D) createDrawable(axis[i]);
 		}
 		
 		
 		//plane	
 		xOyPlane = kernel3D.getXOYPlane();
 		xOyPlane.setEuclidianVisible(true);
 		xOyPlane.setGridVisible(true);
 		xOyPlane.setPlateVisible(false);
 		xOyPlaneDrawable = (DrawPlane3D) createDrawable(xOyPlane);
 
 		
 		
 			
 	}
 	
 	
 	
 	
 	/** return the 3D kernel
 	 * @return the 3D kernel
 	 */
 	public Kernel3D getKernel(){
 		return kernel3D;
 	}
 	
 	
 	
 	
 	/**
 	 * @return controller
 	 */
 	public EuclidianController getEuclidianController(){
 		return euclidianController3D;
 	}
 	
 	
 	/**
 	 * @return gl renderer
 	 */
 	public Renderer getRenderer(){
 		return renderer;
 	}
 	
 	
 
 	/**
 	 * adds a GeoElement3D to this view
 	 */	
 	public void add(GeoElement geo) {
 		
 		if (geo.hasDrawable3D()){
 			Drawable3D d = null;
 			d = createDrawable(geo);
 			if (d != null) {
 				addToDrawable3DLists(d);//drawable3DLists.add(d);
 				//repaint();			
 			}
 		}
 	}
 	
 	
 	/**
 	 * add the drawable to the lists of drawables
 	 * @param d
 	 */
 	public void addToDrawable3DLists(Drawable3D d){
 		setWaitForUpdate();
 		//drawable3DLists.add(d);
 		drawable3DListToBeAdded.add(d);
 	}
 
 	/**
 	 * Create a {@link Drawable3D} linked to the {@link GeoElement3D}
 	 * 
 	 * <h3> Exemple:</h3>
 	  
 	  For a GeoElement3D called "GeoNew3D", add in the switch the following code:
 	    <p>
 	    <code>
 	    case GeoElement3D.GEO_CLASS_NEW3D: <br> &nbsp;&nbsp;                   
            d = new DrawNew3D(this, (GeoNew3D) geo); <br> &nbsp;&nbsp;
            break; <br> 
         }
         </code>
 
 	 * 
 	 * @param geo GeoElement for which the drawable is created
 	 * @return the drawable
 	 */
 	protected Drawable3D createDrawable(GeoElement geo) {
 		Drawable3D d=null;
 		if (geo.hasDrawable3D()){
 
 			switch (geo.getGeoClassType()) {
 
 			// 2D also shown in 3D
 			case GeoElement3D.GEO_CLASS_LIST:
 				d = new DrawList3D(this, (GeoList) geo);
 				break;				
 
 				// 3D stuff
 			case GeoElement.GEO_CLASS_POINT:
 			case GeoElement3D.GEO_CLASS_POINT3D:
 				d = new DrawPoint3D(this, (GeoPointND) geo);
 				break;									
 
 			case GeoElement3D.GEO_CLASS_VECTOR3D:
 				d = new DrawVector3D(this, (GeoVector3D) geo);
 				break;									
 
 			case GeoElement.GEO_CLASS_SEGMENT:
 			case GeoElement3D.GEO_CLASS_SEGMENT3D:
 				d = new DrawSegment3D(this, (GeoSegmentND) geo);
 				break;									
 
 
 			case GeoElement3D.GEO_CLASS_PLANE3D:
 				if (geo instanceof GeoPlane3DConstant)
 					d = new DrawPlaneConstant3D(this, (GeoPlane3D) geo,
 							axisDrawable[AXIS_X],axisDrawable[AXIS_Y]);
 				else
 					d = new DrawPlane3D(this, (GeoPlane3D) geo);
 
 				break;		
 				
 
 			case GeoElement3D.GEO_CLASS_POLYGON:
 			case GeoElement3D.GEO_CLASS_POLYGON3D:
 				d = new DrawPolygon3D(this, (GeoPolygon) geo);
 				break;									
 
 
 			case GeoElement.GEO_CLASS_LINE:	
 			case GeoElement3D.GEO_CLASS_LINE3D:	
 				d = new DrawLine3D(this, (GeoLineND) geo);	
 				break;									
 
 			case GeoElement.GEO_CLASS_RAY:
 			case GeoElement3D.GEO_CLASS_RAY3D:
 				d = new DrawRay3D(this, (GeoRayND) geo);					
 				break;	
 
 			case GeoElement3D.GEO_CLASS_CONIC3D:					
 				d = new DrawConic3D(this, (GeoConic3D) geo);
 				break;	
 
 			case GeoElement3D.GEO_CLASS_AXIS3D:	
 				d = new DrawAxis3D(this, (GeoAxis3D) geo);	
 				break;	
 
 			case GeoElement3D.GEO_CLASS_CURVECARTESIAN3D:	
 				d = new DrawCurve3D(this, (GeoCurveCartesian3D) geo);	
 				break;									
 
 
 
 
 			case GeoElement3D.GEO_CLASS_QUADRIC:					
 				d = new DrawQuadric3D(this, (GeoQuadric3D) geo);
 				break;									
 
 			case GeoElement.GEO_CLASS_FUNCTION_NVAR:					
 				d = new DrawFunction2Var(this, (GeoFunctionNVar) geo);
 				break;									
 
 			}
 
 		}
 
 		
 		if (d != null) 			
 			drawable3DMap.put(geo, d);
 		
 		
 		return d;
 	}
 	
 	
 	
 	
 	
 	public DrawableND createDrawableND(GeoElement geo) {
 		return createDrawable(geo);
 	}
 	
 	
 	
 	
 	
 	
 	/**
 	 * Converts real world coordinates to screen coordinates.
 	 * 
 	 * @param vInOut
 	 *            input and output array with x, y, z, w coords (
 	 */
 	final public void toScreenCoords3D(GgbVector vInOut) {	
 		changeCoords(m,vInOut);		
 	}
 	
 	/**
 	 * converts the matrix to screen coords
 	 * @param mInOut
 	 */
 	final public void toScreenCoords3D(GgbMatrix mInOut) {		
 		changeCoords(m,mInOut);			
 	}
 	
 	
 	/**
 	 * converts the vector to scene coords
 	 * @param vInOut
 	 */
 	final public void toSceneCoords3D(GgbVector vInOut) {	
 		changeCoords(mInv,vInOut);		
 	}
 	
 	/**
 	 * converts the matrix to scene coords
 	 * @param mInOut
 	 */
 	final public void toSceneCoords3D(GgbMatrix mInOut) {		
 		changeCoords(mInv,mInOut);			
 	}
 	
 	
 	final private void changeCoords(GgbMatrix mat, GgbVector vInOut){
 		GgbVector v1 = vInOut.getCoordsLast1();
 		vInOut.set(mat.mul(v1));		
 	}
 
 	final private void changeCoords(GgbMatrix mat, GgbMatrix mInOut){	
 		GgbMatrix m1 = mInOut.copy();
 		mInOut.set(mat.mul(m1));		
 	}
 	
 	
 	/** return the matrix : screen coords -> scene coords.
 	 * @return the matrix : screen coords -> scene coords.
 	 */
 	final public GgbMatrix4x4 getToSceneMatrix(){
 		
 		return mInv;
 	}
 	
 	/** return the matrix : scene coords -> screen coords.
 	 * @return the matrix : scene coords -> screen coords.
 	 */
 	final public GgbMatrix4x4 getToScreenMatrix(){
 		
 		return m;
 	}	
 	
 	/** return the matrix undoing the rotation : scene coords -> screen coords.
 	 * @return the matrix undoing the rotation : scene coords -> screen coords.
 	 */
 	final public GgbMatrix4x4 getUndoRotationMatrix(){
 		
 		return undoRotationMatrix;
 	}	
 	
 	/**
 	 * set Matrix for view3D
 	 */	
 	public void updateMatrix(){
 		
 		//TODO use Ggb3DMatrix4x4
 		
 		//rotations
 		GgbMatrix m1 = GgbMatrix.Rotation3DMatrix(GgbMatrix.X_AXIS, (this.b-90)*EuclidianController3D.ANGLE_TO_DEGREES);
 		GgbMatrix m2 = GgbMatrix.Rotation3DMatrix(GgbMatrix.Z_AXIS, (-this.a-90)*EuclidianController3D.ANGLE_TO_DEGREES);
 		GgbMatrix m3 = m1.mul(m2);
 
 		undoRotationMatrix.set(m3.inverse());
 
 		//scaling
 		GgbMatrix m4 = GgbMatrix.ScaleMatrix(new double[] {getXscale(),getYscale(),getZscale()});		
 		
 
 		//translation
 		GgbMatrix m5 = GgbMatrix.TranslationMatrix(new double[] {getXZero(),getYZero(),getZZero()});
 		
 		m.set(m5.mul(m3.mul(m4)));	
 		
 		mInv.set(m.inverse());
 		
 		
 		//update view direction
 		viewDirection = vz.copyVector();
 		toSceneCoords3D(viewDirection);	
 		
 		setWaitForUpdate();
 		
 	}
 	
 	/**
 	 * 
 	 * @return direction of the eye
 	 */
 	public GgbVector getViewDirection(){
 		return viewDirection;
 	}
 
 	
 	/**
 	 * sets the rotation matrix
 	 * @param a
 	 * @param b
 	 * @param repaint
 	 */
 	public void setRotXYinDegrees(double a, double b, boolean repaint){
 		
 		//Application.debug("setRotXY");
 		
 		this.a = a;
 		this.b = b;
 		
 		if (this.b>EuclidianController3D.ANGLE_MAX)
 			this.b=EuclidianController3D.ANGLE_MAX;
 		else if (this.b<-EuclidianController3D.ANGLE_MAX)
 			this.b=-EuclidianController3D.ANGLE_MAX;
 		
 		
 		
 		updateMatrix();
 		
 		if (repaint)
 			setWaitForUpdate();
 	}
 	
 	
 	/** Sets coord system from mouse move */
 	final public void setCoordSystemFromMouseMove(int dx, int dy, int mode) {	
 		switch(mode){
 		case EuclidianController3D.MOVE_ROTATE_VIEW:
 			setRotXYinDegrees(aOld - dx, bOld + dy, true);
 			break;
 		case EuclidianController3D.MOVE_VIEW:
 			setXZero(XZeroOld+dx);
 			setYZero(YZeroOld-dy);
 			updateMatrix();
 			update();
 			break;
 		}
 	}
 
 	/**
 	 * add to the rotation matrix
 	 * @param da
 	 * @param db
 	 * @param repaint
 	 */
 	public void addRotXY(int da, int db, boolean repaint){
 		
 		setRotXYinDegrees(a+da,b+db,repaint);
 	}	
 
 	/**
 	 * set the rotation matrix in radians
 	 * @param a
 	 * @param b
 	 * @param repaint
 	 */
 	public void setRotXY(double a, double b, boolean repaint){
 		
 		setRotXYinDegrees(a/EuclidianController3D.ANGLE_TO_DEGREES,b/EuclidianController3D.ANGLE_TO_DEGREES,repaint);
 		
 	}
 	
 	
 
 	/* TODO interaction - note : methods are called by EuclidianRenderer3D.viewOrtho() 
 	 * to re-center the scene */
 	public double getXZero() { return XZero; }
 	public double getYZero() { return YZero; }
 	/** @return the z-coord of the origin */
 	public double getZZero() { return ZZero; }
 
 	/** set the x-coord of the origin 
 	 * @param val */
 	public void setXZero(double val) { XZero=val; }
 	/** set the y-coord of the origin 
 	 * @param val */
 	public void setYZero(double val) { YZero=val; }
 	/** set the z-coord of the origin 
 	 * @param val */
 	public void setZZero(double val) { ZZero=val; }
 	
 	
 
 	/**  @return min-max value for x-axis (linked to grid)*/
 	public double[] getXMinMax(){ return axisDrawable[AXIS_X].getDrawMinMax(); }
 	/**  @return min value for y-axis (linked to grid)*/
 	public double[] getYMinMax(){ return axisDrawable[AXIS_Y].getDrawMinMax(); }
 	/**  @return min value for z-axis */
 	public double[] getZMinMax(){ 
 		return axisDrawable[AXIS_Z].getDrawMinMax(); 
 	}
 
 	
 	//TODO specific scaling for each direction
 	private double scale = 100; 
 
 
 	public double getXscale() { return scale; }
 	public double getYscale() { return scale; }
 	
 	/** @return the z-scale */
 	public double getZscale() { return scale; }
 	
 	/**
 	 * set the all-axis scale
 	 * @param val
 	 */
 	public void setScale(double val){
 		scale = val;
 	}
 	
 	/**
 	 * @return the all-axis scale
 	 */
 	public double getScale(){
 		return scale;
 	}
 
 	
 	/** remembers the origins values (xzero, ...) */
 	public void rememberOrigins(){
 		aOld = a;
 		bOld = b;
 		XZeroOld = XZero;
 		YZeroOld = YZero;
 	}
 
 	
 	
 	
 	
 
 	
 	
 	//////////////////////////////////////
 	// update
 	
 	
 
 
 	/** update the drawables for 3D view */
 	public void update(){
 		
 		if (isAnimated()){
 			animate();
 			setWaitForUpdate();
 		}
 		
 		if (waitForUpdate){
 			//drawList3D.updateAll();
 			drawable3DLists.remove(drawable3DListToBeRemoved);
 			drawable3DLists.add(drawable3DListToBeAdded);
 			drawable3DListToBeAdded.clear();
 			drawable3DLists.viewChanged();
 			
 			viewChangedOwnDrawables();
 			setWaitForUpdateOwnDrawables();
 			
 			waitForUpdate = false;
 		}
 
 
 		// update decorations
 		pointDecorations.update();
 	}
 	
 	
 	/** 
 	 * tell the view that it has to be updated
 	 * 
 	 */
 	public void setWaitForUpdate(){
 		waitForUpdate = true;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	private boolean isStarted = false;
 	
 	/**
 	 * @return if the view has been painted at least once
 	 */
 	public boolean isStarted(){
 		return isStarted;
 	}
 	
 	
 	public void paint(Graphics g){
 		
 		
 		if (!isStarted){
 			//Application.debug("ici");
 			isStarted = true;
 		}
 		
 		
 		//update();
 		//setWaitForUpdate();
 		if (isFrozen)
 			super.paint(g);
 	}
 	
 	
 	
 	
 	//////////////////////////////////////
 	// toolbar and euclidianController3D
 	
 	/** sets EuclidianController3D mode */
 	public void setMode(int mode){
 		euclidianController3D.setMode(mode);
 	}
 	
 	
 	
 	
 	
 
 	
 	
 	//////////////////////////////////////
 	// picking
 	
 	
 	/** (x,y) 2D screen coords -> 3D physical coords 
 	 * @param x 
 	 * @param y 
 	 * @return 3D physical coords of the picking point */
 	public GgbVector getPickPoint(int x, int y){			
 		
 		
 		Dimension d = new Dimension();
 		this.getSize(d);
 		
 		if (d!=null){
 			
 			GgbVector ret = new GgbVector(
 					new double[] {
 							(double) x+renderer.getLeft(),
 							(double) -y+renderer.getTop(),
 							0, 1.0});
 			
 			return ret;
 		}else
 			return null;
 		
 		
 	}
 	
 	
 	/** p scene coords, (dx,dy) 2D mouse move -> 3D physical coords 
 	 * @param p 
 	 * @param dx 
 	 * @param dy 
 	 * @return 3D physical coords  */
 	public GgbVector getPickFromScenePoint(GgbVector p, int dx, int dy){
 		GgbVector point = p.copyVector();
 		toScreenCoords3D(point);
 		GgbVector ret = new GgbVector(
 				new double[] {
 						point.get(1)+dx,
 						point.get(2)-dy,
 						0, 1.0});
 
 		return ret;
 		
 	}
 	
 
 		
 	
 	
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * attach the view to the kernel
 	 */
 	public void attachView() {
 		kernel3D.notifyAddAll(this);
 		kernel3D.attach(this);
 	}
 	
 	
 	public void clearView() {
 		drawable3DLists.clear();
 		
 	}
 
 	/**
 	 * remove a GeoElement3D from this view
 	 */	
 	public void remove(GeoElement geo) {
 
 		if (geo.hasDrawable3D()){
 			//Drawable3D d = ((GeoElement3DInterface) geo).getDrawable3D();
 			Drawable3D d = drawable3DMap.get(geo);
 			//drawable3DLists.remove(d);
 			remove(d);
 			
 			//for GeoList : remove all 3D drawables linked to it
 			if (geo.isGeoList()){
 				for (DrawableND d1 : ((DrawList3D) d).getDrawables3D()){
 					if (d1.createdByDrawList())
 						remove((Drawable3D) d1);
 				}
 			}
 		}
 		
 		drawable3DMap.remove(geo);
 	}
 	
 	/**
 	 * remove the drawable d
 	 * @param d
 	 */
 	public void remove(Drawable3D d) {
 		setWaitForUpdate();
 		drawable3DListToBeRemoved.add(d);
 		
 	}
 
 
 	public void rename(GeoElement geo) {
 		// TODO Raccord de méthode auto-généré
 		
 	}
 
 	public void repaintView() {
 		
 		//reset();
 		
 		//update();
 		//setWaitForUpdate();
 		
 		//Application.debug("repaint View3D");
 		
 	}
 
 	public void reset() {
 		
 		//Application.debug("reset View3D");
 		resetAllDrawables();
 		//updateAllDrawables();
 		viewChangedOwnDrawables();
 		setWaitForUpdate();
 		//update();
 		
 	}
 
 	public void update(GeoElement geo) {
 		if (geo.hasDrawable3D()){
 			Drawable3D d = drawable3DMap.get(geo);
 				//((GeoElement3DInterface) geo).getDrawable3D();
 			if (d!=null){
 				update(d);
 				//update(((GeoElement3DInterface) geo).getDrawable3D());
 			}
 		}
 	}
 	
 	private void updateAllDrawables(){
 		for (Drawable3D d:drawable3DMap.values())
 			update(d);
 		setWaitForUpdateOwnDrawables();
 		
 	}
 	
 	/**
 	 * says this drawable to be updated
 	 * @param d
 	 */
 	public void update(Drawable3D d){
 		d.setWaitForUpdate();
 	}
 
 	public void updateAuxiliaryObject(GeoElement geo) {
 		// TODO Raccord de méthode auto-généré
 		
 	}
 
 	public int print(Graphics arg0, PageFormat arg1, int arg2) throws PrinterException {
 		// TODO Raccord de méthode auto-généré
 		return 0;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 	//////////////////////////////////////////////
 	// EuclidianViewInterface
 
 
 
 
 
 	public Drawable getDrawableFor(GeoElement geo) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public DrawableND getDrawableND(GeoElement geo) {
 		if (geo.hasDrawable3D()){
 
 			return drawable3DMap.get(geo);
 			//return ((GeoElement3DInterface) geo).getDrawable3D();
 		}
 		
 		return null;
 	}
 
 
 
 
 
 
 
 
 
 
 	public double getGridDistances(int i) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public int getGridType() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public Hits getHits() {
 		//return hits;
 		return hits.clone();
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public double getInvXscale() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public double getInvYscale() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public GeoElement getLabelHit(Point p) {
 		
 		//Application.debug("getLabelHit");
 
 		//sets the flag and mouse location for openGL picking
 		//renderer.setMouseLoc(p.x,p.y,EuclidianRenderer3D.PICKING_MODE_LABELS);
 
 		//calc immediately the hits
 		//renderer.display();
 		
 		
 		//Application.debug("end-getLabelHit");			
 
 		//return null;
 		return hits.getLabelHit();
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public int getPointCapturingMode() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public Previewable getPreviewDrawable() {
 		
 		return previewDrawable;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public Rectangle getSelectionRectangle() {
 		return selectionRectangle;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public boolean getShowMouseCoords() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public boolean getShowXaxis() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public boolean getShowYaxis() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 
 
 	public void setShowAxis(int axis, boolean flag, boolean update){
 		this.axis[axis].setEuclidianVisible(flag);
 	}
 
 
 	public void setShowAxes(boolean flag, boolean update){
 		setShowAxis(AXIS_X, flag, false);
 		setShowAxis(AXIS_Y, flag, false);
 		setShowAxis(AXIS_Z, flag, true);
 	}
 
 	
 	
 	/** sets the visibility of xOy plane
 	 * @param flag
 	 */
 	public void setShowPlane(boolean flag){
 		getxOyPlane().setEuclidianVisible(flag);
 	}
 	
 	
 	/** sets the visibility of xOy plane plate
 	 * @param flag
 	 */
 	public void setShowPlate(boolean flag){
 		getxOyPlane().setPlateVisible(flag);
 	}
 
 	/** sets the visibility of xOy plane grid
 	 * @param flag
 	 */
 	public void setShowGrid(boolean flag){
 		getxOyPlane().setGridVisible(flag);
 	}
 	
 	
 
 
 	public int getViewHeight() {
 		return getHeight();
 	}
 
 
 	public int getViewWidth() {
 		return getWidth();
 	}
 
 
 
 
 
 
 
 	
 
 
 
 	public boolean hitAnimationButton(MouseEvent e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public boolean isGridOrAxesShown() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public void repaintEuclidianView() {
 		//Application.debug("repaintEuclidianView");
 		
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public void resetMode() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 
 
 
 
 
 
 
 	//////////////////////////////////////////////////
 	// ANIMATION
 	//////////////////////////////////////////////////
 
 
 	/** tells if the view is under animation */
 	private boolean isAnimated(){
 		return animatedScale || isRotAnimated();
 	}
 	
 	/** tells if the view is under rot animation 
 	 * @return true if there is a rotation animation*/
 	public boolean isRotAnimated(){
 		return  animatedContinueRot || animatedRot;
 	}
 	
 	public void setAnimatedCoordSystem(double ox, double oy, double newScale,
 			int steps, boolean storeUndo) {
 		
 
 		animatedScaleStart = getScale();
 		animatedScaleTimeStart = System.currentTimeMillis();
 		animatedScaleEnd = newScale;
 		animatedScale = true;
 		
 		animatedScaleTimeFactor = 0.005; //it will take about 1/2s to achieve it
 		
 		//this.storeUndo = storeUndo;
 
 		
 	}
 	
 	
 	/** sets a continued animation for rotation
 	 * if delay is too long, no animation
 	 * if speed is too small, no animation
 	 * @param delay delay since last drag
 	 * @param rotSpeed speed of rotation
 	 */
 	public void setRotContinueAnimation(long delay, double rotSpeed){
 		//Application.debug("delay="+delay+", rotSpeed="+rotSpeed);
 
 		//if last drag occured more than 200ms ago, then no animation
 		if (delay>200)
 			return;
 		
 		//if speed is too small, no animation
 		if (Math.abs(rotSpeed)<0.01)
 			return;
 		
 		//if speed is too large, use max speed
 		if (rotSpeed>0.1)
 			rotSpeed=0.1;
 		else if (rotSpeed<-0.1)
 			rotSpeed=-0.1;
 			
 		
 			
 		animatedContinueRot = true;
 		animatedRot = false;
 		animatedRotSpeed = -rotSpeed;
 		animatedRotTimeStart = System.currentTimeMillis() - delay;
 		bOld = b;
 		aOld = a;
 	}
 	
 	
 	/**
 	 * start a rotation animation to be in the vector direction
 	 * @param vn
 	 */
 	public void setRotAnimation(GgbVector vn){
 
 		animatedRot = true;
 		animatedContinueRot = false;
 		animatedRotTimeStart = System.currentTimeMillis();// - 16;
 		aOld = this.a % 360;
 		bOld = this.b % 360;
 		
 		GgbVector spheric;
 		//put the eye in front of the visible side
 		GgbVector eye = GgbMatrixUtil.cartesianCoords(1,aOld*Math.PI/180,bOld*Math.PI/180);
 		//Application.debug("c="+eye.dotproduct(vn));
 		if(eye.dotproduct(vn)>=0)
 			spheric = GgbMatrixUtil.sphericalCoords(vn);
 		else
 			spheric = GgbMatrixUtil.sphericalCoords((GgbVector) vn.mul(-1));
 		
 		//Application.debug("vn\n"+vn+"\nbis\n"+Ggb3DMatrixUtil.cartesianCoords(spheric));
 		
 		aNew = spheric.get(2)*180/Math.PI;
 		bNew = spheric.get(3)*180/Math.PI;
 		
 
 		/*
 		Application.debug(
 				"(a,b)=(" + (this.a) +"�,"+ (this.b)+"�)\n"
 				+
 				"(aOld,bOld)=(" + (this.aOld) +"�,"+ (this.bOld)+"�)\n"
 				+
 				"(aNew,bNew)=(" + (this.aNew) +"�,"+ (this.bNew)+"�)"
 		);
 		*/
 		
 		
 		//if (aNew,bNew)=(0�,90�), then change it to (90�,90�) to have correct xOy orientation
 		if (Kernel.isEqual(aNew, 0, Kernel.STANDARD_PRECISION) &&
 				Kernel.isEqual(Math.abs(bNew), 90, Kernel.STANDARD_PRECISION))
 			aNew=-90;
 		
 		
 		//looking for the smallest path
 		if (aOld-aNew>180)
 			aOld-=360;
 		/*
 		else if (aOld-aNew<-180)
 			aOld+=360;
 			*/
 		else if (Kernel.isEqual(aOld, aNew, Kernel.STANDARD_PRECISION))
 			if (Kernel.isEqual(bOld, bNew, Kernel.STANDARD_PRECISION)){
 				if (!Kernel.isEqual(Math.abs(bNew), 90, Kernel.STANDARD_PRECISION))
 					aNew+=180;
 				bNew*=-1;
 				//Application.debug("ici");
 			}
 		if (bOld>180)
 			bOld-=360;
 
 
 
 		
 
 		/*
 		Application.debug(
 				"bis\n(a,b)=(" + (this.a) +"�,"+ (this.b)+"�)\n"
 				+
 				"(aOld,bOld)=(" + (this.aOld) +"�,"+ (this.bOld)+"�)\n"
 				+
 				"(aNew,bNew)=(" + (this.aNew) +"�,"+ (this.bNew)+"�)"
 		);
 		*/
 		
 	}
 	
 	
 	/**
 	 * stops the rotation animation
 	 */
 	public void stopRotAnimation(){
 		animatedContinueRot = false;
 		animatedRot = false;
 	}
 
 
 	/** animate the view for changing scale, orientation, etc. */
 	private void animate(){
 		if (animatedScale){
 			double t = (System.currentTimeMillis()-animatedScaleTimeStart)*animatedScaleTimeFactor;
 			t+=0.2; //starting at 1/4
 			
 			if (t>=1){
 				t=1;
 				animatedScale = false;
 			}
 			
 			//Application.debug("t="+t+"\nscale="+(startScale*(1-t)+endScale*t));
 			
 			setScale(animatedScaleStart*(1-t)+animatedScaleEnd*t);
 			updateMatrix();
 			
 		}
 		
 		if (animatedContinueRot){
 			double da = (System.currentTimeMillis()-animatedRotTimeStart)*animatedRotSpeed;			
 			setRotXYinDegrees(aOld + da, bOld, true);
 		}
 		
 		if (animatedRot){
 			double t = (System.currentTimeMillis()-animatedRotTimeStart)*0.001;
 			//t+=0.2; //starting at 1/4
 			
 			if (t>=1){
 				t=1;
 				animatedRot = false;
 			}
 			
 			setRotXYinDegrees(aOld*(1-t)+aNew*t, bOld*(1-t)+bNew*t, true);
 		}
 
 			
 		
 		
 	}
 
 
 
 
 
 
 
 
 
 
 	public void setAnimatedRealWorldCoordSystem(double xmin, double xmax,
 			double ymin, double ymax, int steps, boolean storeUndo) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public boolean setAnimationButtonsHighlighted(boolean hitAnimationButton) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public void setCoordSystem(double x, double y, double xscale, double yscale) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	/*
 	Point pOld = null;
 
 
 	public void setHits(Point p) {
 		
 		
 		
 		if (p.equals(pOld)){
 			//Application.printStacktrace("");
 			return;
 		}
 		
 		
 		pOld = p;
 		
 		//sets the flag and mouse location for openGL picking
 		renderer.setMouseLoc(p.x,p.y,Renderer.PICKING_MODE_LABELS);
 
 		//calc immediately the hits
 		renderer.display();
 		
 
 	}
 	
 	*/
 	
 	// empty method : setHits3D() used instead
 	public void setHits(Point p) {
 		
 	}
 	
 	
 	/** sets the 3D hits regarding point location
 	 * @param p point location
 	 */
 	public void setHits3D(Point p) {
 		
 		//sets the flag and mouse location for openGL picking
 		renderer.setMouseLoc(p.x,p.y,Renderer.PICKING_MODE_LABELS);
 
 		//calc immediately the hits
 		//renderer.display();
 		
 
 	}
 	
 
 
 
 	/** add a drawable to the current hits
 	 * (used when a new object is created)
 	 * @param d drawable to add
 	 */
 	public void addToHits3D(Drawable3D d){
 		hits.addDrawable3D(d, false);
 		hits.sort();
 	}
 	
 	/** add the drawable of the geo to the current hits
 	 * (used when a new object is created)
 	 * @param geo
 	 */
 	public void addToHits3D(GeoElement geo){
 
 		DrawableND d = getDrawableND(geo);
 		if (d!=null)
 			addToHits3D((Drawable3D) d);
 		
 	}	
 
 	
 	/** init the hits for this view
 	 * @param hits
 	 */
 	public void setHits(Hits3D hits){
 		this.hits = hits;
 	}
 
 
 
 
 
 
 
 
 	public void setHits(Rectangle rect) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public void setSelectionRectangle(Rectangle selectionRectangle) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public void setShowAxesRatio(boolean b) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public void setShowMouseCoords(boolean b) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public double toRealWorldCoordX(double minX) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public double toRealWorldCoordY(double maxY) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public void updateSize() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	public void zoom(double px, double py, double zoomFactor, int steps,
 			boolean storeUndo) {
 
 		setScale(getXscale()*zoomFactor);
 		updateMatrix();
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/////////////////////////////////////////
 	// previewables
 	
 	
 
 	
 	/** return the point used for 3D cursor
 	 * @return the point used for 3D cursor
 	 */
 	public GeoPoint3D getCursor3D(){
 		return cursor3D;
 	}
 	
 	
 
 	
 	
 	/**
 	 * sets the type of the cursor
 	 * @param v
 	 */
 	public void setCursor3DType(int v){
 		cursor3DType = v;
 	}
 	
 
 	/**
 	 * @return the type of the cursor
 	 */
 	public int getCursor3DType(){
 		return cursor3DType;
 	}
 	
 	
 	
 	/** sets that the current 3D cursor is at the intersection of the two GeoElement parameters
 	 * @param cursor3DIntersectionOf1 first GeoElement of intersection
 	 * @param cursor3DIntersectionOf2 second GeoElement of intersection
 	 */
 	public void setCursor3DIntersectionOf(GeoElement cursor3DIntersectionOf1, GeoElement cursor3DIntersectionOf2){
 		this.cursor3DIntersectionOf[0]=cursor3DIntersectionOf1;
 		this.cursor3DIntersectionOf[1]=cursor3DIntersectionOf2;
 	}
 	
 	/** return the i-th GeoElement of intersection
 	 * @param i number of GeoElement of intersection
 	 * @return GeoElement of intersection
 	 */
 	public GeoElement getCursor3DIntersectionOf(int i){
 		return cursor3DIntersectionOf[i];
 	}
 	
 	
 	
 	/**
 	 * @return the list of 3D drawables
 	 */
 	public Drawable3DLists getDrawList3D(){
 		return drawable3DLists;
 	}
 	
 	
 	@SuppressWarnings("rawtypes")
 	public Previewable createPreviewLine(ArrayList selectedPoints){
 
 		return new DrawLine3D(this, selectedPoints);
 		
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public Previewable createPreviewSegment(ArrayList selectedPoints){
 		return new DrawSegment3D(this, selectedPoints);
 	}	
 	
 	@SuppressWarnings("rawtypes")
 	public Previewable createPreviewRay(ArrayList selectedPoints){
 		return new DrawRay3D(this, selectedPoints);
 	}	
 	
 
 	@SuppressWarnings("rawtypes")
 	public Previewable createPreviewVector(ArrayList selectedPoints){
 		return new DrawVector3D(this, selectedPoints);
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public Previewable createPreviewPolygon(ArrayList selectedPoints){
 		return new DrawPolygon3D(this, selectedPoints);
 	}	
 
 	/**
 	 * @param selectedPoints
 	 * @return a preview sphere (center-point)
 	 */
 	@SuppressWarnings("rawtypes")
 	public Previewable createPreviewSphere(ArrayList selectedPoints){
 		return new DrawQuadric3D(this, selectedPoints, GeoQuadricND.QUADRIC_SPHERE);
 	}	
 
 	
 	
 	public void updatePreviewable(){
 
 		getPreviewDrawable().updatePreview();
 	}
 	
 	
 	/**
 	 * update the 3D cursor with current hits
 	 */
 	public void updateCursor3D(){
 
 		//Application.debug("hits ="+getHits().toString());
 		
 		if (hasMouse){
 			getEuclidianController().updateNewPoint(true, 
 				getHits().getTopHits(), 
 				true, true, true, false, //TODO doSingleHighlighting = false ? 
 				false);
 			
 			
 			updateMatrixForCursor3D();
 		}
 		
 	}
 
 	/**
 	 * update cursor3D matrix
 	 */
 	public void updateMatrixForCursor3D(){		
 		double t;
 
 		GgbMatrix4x4 matrix;
 		GgbMatrix4x4 m2;
 
 		switch(getCursor3DType()){
 		case PREVIEW_POINT_FREE:
 			// use default directions for the cross
 			t = 1/getScale();
 			getCursor3D().getDrawingMatrix().setVx((GgbVector) vx.mul(t));
 			getCursor3D().getDrawingMatrix().setVy((GgbVector) vy.mul(t));
 			getCursor3D().getDrawingMatrix().setVz((GgbVector) vz.mul(t));
 			break;
 		case PREVIEW_POINT_REGION:
 			// use region drawing directions for the cross
 			t = 1/getScale();
 			getCursor3D().getDrawingMatrix().setVx(
 					(GgbVector) ((GeoElement3DInterface) getCursor3D().getRegion()).getDrawingMatrix().getVx().mul(t));
 			getCursor3D().getDrawingMatrix().setVy(
 					(GgbVector) ((GeoElement3DInterface) getCursor3D().getRegion()).getDrawingMatrix().getVy().mul(t));
 			getCursor3D().getDrawingMatrix().setVz(
 					(GgbVector) ((GeoElement3DInterface) getCursor3D().getRegion()).getDrawingMatrix().getVz().mul(t));
 			break;
 		case PREVIEW_POINT_PATH:
 			// use path drawing directions for the cross
 			t = 1/getScale();
 
 			GgbVector v = ((GeoElement)getCursor3D().getPath()).getViewDirection();
 			GgbMatrix m = new GgbMatrix(4, 2);
 			m.set(v, 1);
 			m.set(4, 2, 1);
 			//GgbMatrix4x4 m2 = new GgbMatrix4x4(m);
 
 			matrix = new GgbMatrix4x4(m);
 			
 			/*
 			matrix = new GgbMatrix4x4();
 			matrix.setVx(m2.getVy());
 			matrix.setVy(m2.getVz());
 			matrix.setVz(m2.getVx());
 			matrix.setOrigin(m2.getOrigin());
 			*/
 
 			getCursor3D().getDrawingMatrix().setVx(
 					(GgbVector) matrix.getVx().normalized().mul(t));
 			t *= (10+((GeoElement) getCursor3D().getPath()).getLineThickness());
 			getCursor3D().getDrawingMatrix().setVy(
 					(GgbVector) matrix.getVy().mul(t));
 			getCursor3D().getDrawingMatrix().setVz(
 					(GgbVector) matrix.getVz().mul(t));
 			break;
 		case PREVIEW_POINT_DEPENDENT:
 			//use size of intersection
 			int t1 = getCursor3DIntersectionOf(0).getLineThickness();
 			int t2 = getCursor3DIntersectionOf(1).getLineThickness();
 			if (t1>t2)
 				t2=t1;
 			t = (t2+6)/getScale();
 			getCursor3D().getDrawingMatrix().setVx((GgbVector) vx.mul(t));
 			getCursor3D().getDrawingMatrix().setVy((GgbVector) vy.mul(t));
 			getCursor3D().getDrawingMatrix().setVz((GgbVector) vz.mul(t));
 			break;			
 		case PREVIEW_POINT_ALREADY:
 			//use size of point
 			t = (getCursor3D().getPointSize()/6+2)/getScale();
 
 			if (getCursor3D().hasPath()){
 				v = ((GeoElement)getCursor3D().getPath()).getViewDirection();
 				m = new GgbMatrix(4, 2);
 				m.set(v, 1);
 				m.set(4, 2, 1);
 				m2 = new GgbMatrix4x4(m);
 
 				matrix = new GgbMatrix4x4();
 				matrix.setVx(m2.getVy());
 				matrix.setVy(m2.getVz());
 				matrix.setVz(m2.getVx());
 				matrix.setOrigin(m2.getOrigin());
 
 
				/*
					if (((GeoElement)getCursor3D().getPath()).isGeoElement3D())
						matrix = ((GeoElement3DInterface) getCursor3D().getPath()).getDrawingMatrix();
					else{
						GgbVector v = ((GeoElement)getCursor3D().getPath()).getViewDirection();
						if (v==null)
							matrix = GgbMatrix4x4.Identity();
						else{
							GgbMatrix m = new GgbMatrix(4, 2);
							m.set(v, 1);
							m.set(4, 2, 1);
							matrix = new GgbMatrix4x4(m);
						}

					}
				 */
 			}else if (getCursor3D().hasRegion()){
				//matrix = ((GeoElement3D) getCursor3D().getRegion()).getDrawingMatrix();
 				v = ((GeoElement)getCursor3D().getRegion()).getViewDirection();
 				m = new GgbMatrix(4, 2);
 				m.set(v, 1);
 				m.set(4, 2, 1);
 				m2 = new GgbMatrix4x4(m);
 
 				matrix = new GgbMatrix4x4();
 				matrix.setVx(m2.getVy());
 				matrix.setVy(m2.getVz());
 				matrix.setVz(m2.getVx());
 				matrix.setOrigin(m2.getOrigin());
 			}else
 				matrix = GgbMatrix4x4.Identity();
 
 			getCursor3D().getDrawingMatrix().setVx(
 					(GgbVector) matrix.getVx().normalized().mul(t));
 			getCursor3D().getDrawingMatrix().setVy(
 					(GgbVector) matrix.getVy().mul(t));
 			getCursor3D().getDrawingMatrix().setVz(
 					(GgbVector) matrix.getVz().mul(t));
 			break;
 		}
 
 
 
 
 
 		//Application.debug("getCursor3DType()="+getCursor3DType());
 
 		
 	}
 	
 
 
 
 	public void setPreview(Previewable previewDrawable) {
 		
 		if (this.previewDrawable!=null)
 			this.previewDrawable.disposePreview();
 		
 		if (previewDrawable!=null){
 			addToDrawable3DLists((Drawable3D) previewDrawable);
 			//drawable3DLists.add((Drawable3D) previewDrawable);
 		}
 		
 		//Application.debug("drawList3D :\n"+drawList3D);
 			
 		
 			
 		//setCursor3DType(PREVIEW_POINT_NONE);
 		
 		this.previewDrawable = previewDrawable;
 		
 		
 		
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	/////////////////////////////////////////////////////
 	// 
     // POINT DECORATION 
 	//
 	/////////////////////////////////////////////////////
 	
 	private void initPointDecorations(){
 		//Application.debug("hop");
 		pointDecorations = new DrawPointDecorations(this);
 	}
 	
 	
 	/** update decorations for localizing point in the space
 	 *  if point==null, no decoration will be drawn
 	 * @param point
 	 */
 	public void updatePointDecorations(GeoPoint3D point){
 		
 		if (point==null)
 			decorationVisible = false;
 		else{
 			decorationVisible = true;
 			pointDecorations.setPoint(point);
 		}
 		
 		//Application.debug("point :\n"+point.getDrawingMatrix()+"\ndecorations :\n"+decorationMatrix);
 		
 		
 	}
 	
 	
 
 	
 	
 	
 
 	/////////////////////////////////////////////////////
 	// 
 	// CURSOR
 	//
 	/////////////////////////////////////////////////////
 	
 	
 	
 	/** 
 	 * draws the cursor
 	 * @param renderer
 	 */
 	public void drawCursor(Renderer renderer){
 
 		if (hasMouse && !getEuclidianController().mouseIsOverLabel() && cursor3DVisible){
 			
 			renderer.setMatrix(getCursor3D().getDrawingMatrix());
 			
 			switch(cursor){
 			case CURSOR_DEFAULT:
 				//if(getCursor3DType()!=PREVIEW_POINT_ALREADY)
 				//renderer.drawCursorCross3D();
 				//break;
 			case CURSOR_HIT:
 				//Application.debug("getCursor3DType()="+getCursor3DType());				
 				switch(getCursor3DType()){
 				case PREVIEW_POINT_FREE:
 					renderer.drawCursor(PlotterCursor.TYPE_CROSS2D);
 					break;
 				case PREVIEW_POINT_REGION:
 					renderer.drawCursor(PlotterCursor.TYPE_CROSS2D);
 					break;
 				case PREVIEW_POINT_PATH:
 					renderer.drawCursor(PlotterCursor.TYPE_CYLINDER);
 					break;
 				case PREVIEW_POINT_DEPENDENT:
 					renderer.drawCursor(PlotterCursor.TYPE_DIAMOND);
 					break;
 
 				case PREVIEW_POINT_ALREADY:
 					/*
 					if (getCursor3D().hasPath())
 						renderer.drawCursor(PlotterCursor.TYPE_ALREADY_Z);
 					else if (getCursor3D().hasRegion())
 						renderer.drawCursor(PlotterCursor.TYPE_ALREADY_XY);
 					else{
 					 */
 					//if (((GeoPointND) getEuclidianController().getMovedGeoPoint()).getMoveMode()==GeoPointND.MOVE_MODE_XY)
 					if (getCursor3D().getMoveMode()==GeoPointND.MOVE_MODE_XY)
 						renderer.drawCursor(PlotterCursor.TYPE_ALREADY_XY);
 					else
 						renderer.drawCursor(PlotterCursor.TYPE_ALREADY_Z);
 					//}
 					break;
 				}
 				break;
 			}
 		}
 	}
 	
 	
 	/** sets the visibility of the 3D cursor
 	 * @param shown
 	 */
 	public void setShowCursor3D(boolean shown){
 		cursor3DVisible = shown;
 	}
 	
 	
 	public void setMoveCursor(){
 		
 		// 3D cursor
 		cursor = CURSOR_MOVE;
 	}
 	
 	public void setDragCursor(){
 
 		// 2D cursor is invisible
 		//setCursor(app.getTransparentCursor());
 
 		// 3D cursor
 		cursor = CURSOR_DRAG;
 	}
 	
 	public void setDefaultCursor(){
 		
 		// 2D cursor
 		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 		
 		// 3D cursor
 		cursor = CURSOR_DEFAULT;
 	}
 	
 	public void setHitCursor(){
 		//Application.printStacktrace("setHitCursor");
 		cursor = CURSOR_HIT;
 	}
 	
 	
 	
 	
 	public void mouseEntered(){
 		//Application.debug("mouseEntered");
 		hasMouse = true;
 	}
 	
 	public void mouseExited(){
 		//Application.debug("mouseExited");
 		hasMouse = false;
 	}
 	
 	public boolean hasMouse(){
 		return hasMouse;
 	}
 	
 
 
 	/**
 	 * returns settings in XML format, read by xml handlers
 	 * @see geogebra.io.MyXMLHandler
 	 * @see geogebra3D.io.MyXMLHandler3D
 	 * @return the XML description of 3D view settings
 	 */
 	public String getXML() {
 		
 		//if (true)	return "";
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append("<euclidianView3D>\n");
 		
 		
 		// coord system
 		sb.append("\t<coordSystem");
 		
 		sb.append(" xZero=\"");
 		sb.append(getXZero());
 		sb.append("\"");
 		sb.append(" yZero=\"");
 		sb.append(getYZero());
 		sb.append("\"");
 		sb.append(" zZero=\"");
 		sb.append(getZZero());
 		sb.append("\"");	
 		
 		sb.append(" scale=\"");
 		sb.append(getXscale());
 		sb.append("\"");
 
 		sb.append(" xAngle=\"");
 		sb.append(b);
 		sb.append("\"");
 		sb.append(" zAngle=\"");
 		sb.append(a);
 		sb.append("\"");	
 		
 		sb.append("/>\n");
 		
 		
 		
 		
 		// axis settings
 		for (int i = 0; i < 3; i++) {
 			sb.append("\t<axis id=\"");
 			sb.append(i);
 			sb.append("\" show=\"");
 			sb.append(axis[i].isEuclidianVisible());			
 			sb.append("\" label=\"");
 			sb.append(axis[i].getAxisLabel());
 			sb.append("\" unitLabel=\"");
 			sb.append(axis[i].getUnitLabel());
 			sb.append("\" tickStyle=\"");
 			sb.append(axis[i].getTickStyle());
 			sb.append("\" showNumbers=\"");
 			sb.append(axis[i].getShowNumbers());
 
 			// the tick distance should only be saved if
 			// it isn't calculated automatically
 			/*
 			if (!automaticAxesNumberingDistances[i]) {
 				sb.append("\" tickDistance=\"");
 				sb.append(axesNumberingDistances[i]);
 			}
 			*/
 
 			sb.append("\"/>\n");
 		}
 		
 		
 		// xOy plane settings
 		sb.append("\t<plate show=\"");
 		sb.append(getxOyPlane().isPlateVisible());		
 		sb.append("\"/>\n");
 
 		sb.append("\t<grid show=\"");
 		sb.append(getxOyPlane().isGridVisible());		
 		sb.append("\"/>\n");
 		
 		
 		
 		
 		sb.append("</euclidianView3D>\n");
 		return sb.toString();
 	}
 	
 	
 	
 	/////////////////////////////////////////////////////
 	// 
 	// EUCLIDIANVIEW DRAWABLES (AXIS AND PLANE)
 	//
 	/////////////////////////////////////////////////////
 	
 	
 	/**
 	 * toggle the visibility of axes 
 	 */
 	public void toggleAxis(){
 		
 		boolean flag = axesAreAllVisible();
 		
 		for(int i=0;i<3;i++)
 			axis[i].setEuclidianVisible(!flag);
 		
 	}
 	
 
 	/** says if all axes are visible
 	 * @return true if all axes are visible
 	 */
 	public boolean axesAreAllVisible(){
 		boolean flag = true;
 
 		for(int i=0;i<3;i++)
 			flag = (flag && axis[i].isEuclidianVisible());
 
 		return flag;
 	}
 	
 	
 	/**
 	 * toggle the visibility of xOy plane
 	 */
 	public void togglePlane(){
 		
 		boolean flag = xOyPlane.isPlateVisible();
 		xOyPlane.setPlateVisible(!flag);
 		
 	}	
 	
 	/**
 	 * toggle the visibility of xOy grid
 	 */
 	public void toggleGrid(){
 		
 		boolean flag = xOyPlane.isGridVisible();
 		xOyPlane.setGridVisible(!flag);
 		
 	}
 	
 	
 	/**
 	 * @return the xOy plane
 	 */
 	public GeoPlane3D getxOyPlane()  {
 
 		return xOyPlane;
 		
 	}
 	
 	
 	/**
 	 * says if this geo is owned by the view (xOy plane, ...)
 	 * @param geo
 	 * @return if this geo is owned by the view (xOy plane, ...)
 	 */
 	public boolean owns(GeoElement geo){
 		
 		boolean ret = (geo == xOyPlane);
 		
 		for(int i=0;(!ret)&&(i<3);i++)
 			ret = (geo == axis[i]);
 		
 		return ret;
 		
 	}
 	
 	
 	
 	
 	
 	
 	/** draw transparent parts of view's drawables (xOy plane)
 	 * @param renderer
 	 */
 	public void drawTransp(Renderer renderer){
 		
 		
 		if (xOyPlane.isPlateVisible())
 			xOyPlaneDrawable.drawTransp(renderer);
 				
 	}
 	
 	
 	/** draw hiding parts of view's drawables (xOy plane)
 	 * @param renderer
 	 */
 	public void drawHiding(Renderer renderer){
 		
 		
 		if (xOyPlane.isPlateVisible())
 			xOyPlaneDrawable.drawHiding(renderer);
 				
 				
 		
 	}
 	
 	/** draw not hidden parts of view's drawables (axis)
 	 * @param renderer
 	 */
 	public void draw(Renderer renderer){
 		for(int i=0;i<3;i++)
 			axisDrawable[i].draw(renderer);
 		
 		if (decorationVisible)
 			pointDecorations.draw(renderer);
 	}
 	
 	/** draw hidden parts of view's drawables (axis)
 	 * @param renderer
 	 */
 	public void drawHidden(Renderer renderer){
 		for(int i=0;i<3;i++)
 			axisDrawable[i].drawHidden(renderer);
 		
 		xOyPlaneDrawable.drawHidden(renderer);
 		
 		if (decorationVisible)
 			pointDecorations.drawHidden(renderer);
 		
 	}
 	
 	
 	/** draw for picking view's drawables (plane and axis)
 	 * @param renderer
 	 */
 	public void drawForPicking(Renderer renderer){
 		renderer.pick(xOyPlaneDrawable);
 		for(int i=0;i<3;i++)
 			renderer.pick(axisDrawable[i]);
 	}
 	
 	
 	
 	/** draw ticks on axis
 	 * @param renderer
 	 */
 	public void drawLabel(Renderer renderer){
 		
 		for(int i=0;i<3;i++)
 			axisDrawable[i].drawLabel(renderer);
 		
 
 	}
 	
 	
 	
 	
 	
 	/**
 	 * says all drawables owned by the view that the view has changed
 	 */
 	/*
 	public void viewChangedOwnDrawables(){
 		
 		//xOyPlaneDrawable.viewChanged();
 		xOyPlaneDrawable.setWaitForUpdate();
 		
 		for(int i=0;i<3;i++)
 			axisDrawable[i].viewChanged();
 		
 		
 	}
 	*/
 	
 	/**
 	 * tell all drawables owned by the view to be udpated
 	 */
 	public void setWaitForUpdateOwnDrawables(){
 		
 		xOyPlaneDrawable.setWaitForUpdate();
 		
 		for(int i=0;i<3;i++)
 			axisDrawable[i].setWaitForUpdate();
 		
 		
 	}
 	
 	/**
 	 * says all labels owned by the view that the view has changed
 	 */
 	public void resetOwnDrawables(){
 		
 		xOyPlaneDrawable.setWaitForReset();
 		
 		for(int i=0;i<3;i++){
 			axisDrawable[i].setWaitForReset();
 		}
 				
 		pointDecorations.setWaitForReset();
 	}
 	
 
 	
 	/**
 	 * says all labels to be recomputed
 	 */
 	public void resetAllDrawables(){
 		
 		resetOwnDrawables();
 		drawable3DLists.resetAllDrawables();
 		
 	}
 	
 	
 	
 	private void viewChangedOwnDrawables(){
 		
 		// calc draw min/max for x and y axis
 		for(int i=0;i<2;i++){
 			axisDrawable[i].updateDrawMinMax();
 		}
 		
 		//for z axis, use bottom to top min/max
 		double zmin = (renderer.getBottom()-getYZero())/getScale();
 		double zmax = (renderer.getTop()-getYZero())/getScale();
 		axisDrawable[AXIS_Z].setDrawMinMax(zmin, zmax);
 		
 		//update decorations and wait for update
 		for(int i=0;i<3;i++){
 			axisDrawable[i].updateDecorations();
 			axisDrawable[i].setWaitForUpdate();
 		}
 		/*
 		// sets min/max for the plane and axis
 		double xmin = axisDrawable[AXIS_X].getDrawMin(); 
 		double ymin = axisDrawable[AXIS_Y].getDrawMin();
 		double xmax = axisDrawable[AXIS_X].getDrawMax(); 
 		double ymax = axisDrawable[AXIS_Y].getDrawMax();
 		
 		// update xOyPlane
 		xOyPlane.setGridCorners(xmin,ymin,xmax,ymax);
 		xOyPlane.setGridDistances(axis[AXIS_X].getNumbersDistance(), axis[AXIS_Y].getNumbersDistance());
 
 		 */
 	}
 	
 	
 	/**
 	 * update all drawables now
 	 */
 	public void updateOwnDrawablesNow(){
 		
 		for(int i=0;i<3;i++){
 			axisDrawable[i].update();
 		}
 		
 		// update xOyPlane
 		xOyPlaneDrawable.update();
 
 		
 	}
 	
 	
 	
 	
 	//////////////////////////////////////////////////////
 	// AXES
 	//////////////////////////////////////////////////////
 
 	public String[] getAxesLabels(){
 		return null;
 	}
 	public void setAxesLabels(String[] labels){
 		
 	}
 	
 	public void setAxisLabel(int axis, String axisLabel){
 		
 	}
 	
 	public String[] getAxesUnitLabels(){
 		return null;
 	}
 	public void setShowAxesNumbers(boolean[] showNums){
 		
 	}
 	
 	public void setAxesUnitLabels(String[] unitLabels){
 		
 	}
 	
 	public boolean[] getShowAxesNumbers(){
 		return null;
 	}
 	
 	public void setShowAxisNumbers(int axis, boolean showAxisNumbers){
 		
 	}
 	
 	public void setAxesNumberingDistance(double tickDist, int axis){
 		
 	}
 	
 	public int[] getAxesTickStyles(){
 		return null;
 	}
 
 	public void setAxisTickStyle(int axis, int tickStyle){
 		
 	}
 
 
 
 
 
 	public int toScreenCoordX(double minX) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 
 
 
 	public int toScreenCoordY(double maxY) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 
 
 
 	@SuppressWarnings("unchecked")
 	public Previewable createPreviewParallelLine(ArrayList selectedPoints,
 			ArrayList selectedLines) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
 
 
 	@SuppressWarnings("unchecked")
 	public Previewable createPreviewPerpendicularLine(ArrayList selectedPoints,
 			ArrayList selectedLines) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
 
 
 	@SuppressWarnings("unchecked")
 	public Previewable createPreviewPerpendicularBisector(
 			ArrayList selectedPoints) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
 
 
 	@SuppressWarnings("unchecked")
 	public Previewable createPreviewAngleBisector(ArrayList selectedPoints) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Previewable createPreviewPolyLine(ArrayList selectedPoints) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	
 	
 	
 	public void setAxisCross(int axis, double cross) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setPositiveAxis(int axis, boolean isPositive) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public double[] getAxesCross() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void setAxesCross(double[] axisCross) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public boolean[] getPositiveAxes() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void setPositiveAxes(boolean[] positiveAxis) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
