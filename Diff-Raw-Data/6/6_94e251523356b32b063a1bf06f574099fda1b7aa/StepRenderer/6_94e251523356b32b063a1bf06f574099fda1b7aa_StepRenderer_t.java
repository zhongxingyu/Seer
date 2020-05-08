 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.gui.common;
 
 import java.awt.AWTEvent;
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 import java.awt.event.ActionEvent;
 import java.awt.event.InputMethodListener;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.Behavior;
 import javax.media.j3d.BoundingSphere;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.ColoringAttributes;
 import javax.media.j3d.Font3D;
 import javax.media.j3d.FontExtrusion;
 import javax.media.j3d.GraphicsConfigTemplate3D;
 import javax.media.j3d.Group;
 import javax.media.j3d.ImageComponent2D;
 import javax.media.j3d.LineArray;
 import javax.media.j3d.LineAttributes;
 import javax.media.j3d.Material;
 import javax.media.j3d.OrderedGroup;
 import javax.media.j3d.OrientedShape3D;
 import javax.media.j3d.PolygonAttributes;
 import javax.media.j3d.RenderingAttributes;
 import javax.media.j3d.Shape3D;
 import javax.media.j3d.Text3D;
 import javax.media.j3d.Texture;
 import javax.media.j3d.Texture2D;
 import javax.media.j3d.TextureAttributes;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.media.j3d.TransparencyAttributes;
 import javax.media.j3d.TriangleArray;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3d;
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3d;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.sun.j3d.exp.swing.JCanvas3D;
 import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
 import com.sun.j3d.utils.universe.SimpleUniverse;
 
 import cz.cuni.mff.peckam.java.origamist.exceptions.InvalidOperationException;
 import cz.cuni.mff.peckam.java.origamist.math.Segment2d;
 import cz.cuni.mff.peckam.java.origamist.model.Origami;
 import cz.cuni.mff.peckam.java.origamist.model.Step;
 import cz.cuni.mff.peckam.java.origamist.model.UnitDimension;
 import cz.cuni.mff.peckam.java.origamist.model.jaxb.Unit;
 import cz.cuni.mff.peckam.java.origamist.modelstate.Direction;
 import cz.cuni.mff.peckam.java.origamist.modelstate.MarkerRenderData;
 import cz.cuni.mff.peckam.java.origamist.modelstate.ModelSegment;
 import cz.cuni.mff.peckam.java.origamist.modelstate.ModelState;
 
 /**
  * The panel for rendering a step.
  * 
  * Provides the following properties:
  * <ul>
  * <li>pickMode</li>
  * <li>zoom</li>
  * </ul>
  * 
  * @author Martin Pecka
  */
 public class StepRenderer extends JPanel
 {
     /** */
     private static final long         serialVersionUID         = 9198803673578003101L;
 
     /**
      * The origami diagram we are rendering.
      */
     protected Origami                 origami                  = null;
 
     /**
      * The step this renderer is rendering.
      */
     protected Step                    step                     = null;
 
     /**
      * The canvas the model is rendered to.
      */
     protected JCanvas3D               canvas;
 
     /** The offscreen canvas used for drawing. */
     protected Canvas3D                offscreenCanvas;
 
     /** The universe we use. */
     protected SimpleUniverse          universe;
 
     /** The main transform used to display the step. */
     protected Transform3D             transform                = new Transform3D();
 
     /** The transform group containing the whole step. */
     protected TransformGroup          tGroup;
 
     /** The branch graph to be added to the scene. */
     protected BranchGroup             branchGraph              = null;
 
     /** The zoom of the step. */
     protected double                  zoom                     = 100d;
 
     /** The helper for properties. */
     protected PropertyChangeSupport   listeners                = new PropertyChangeSupport(this);
 
     /** The font to use for drawing markers. */
     protected Font                    markerFont               = new Font("Arial", Font.BOLD, 12);
 
     /** The size of the surface texture. */
     protected final static int        TEXTURE_SIZE             = 512;
 
     /** The factory that handles different strokes. */
     protected StrokeFactory           strokeFactory            = new StrokeFactory();
 
     /** Cached textures for top and bottom side of the paper. */
     protected Texture                 topTexture, bottomTexture;
 
     /** The maximum level of anisotropic filter that is supported by the current HW. */
     protected final float             maxAnisotropyLevel;
 
     /** The manager of {@link StepRenderer}'s colors. */
     protected ColorManager            colorManager             = createColorManager(null, null);
 
     /** The manager for changing line appearances. */
     protected LineAppearanceManager   lineAppearanceManager    = createLineAppearanceManager();
 
     /**
      * These callbacks will handle removing unnecessary callbacks. A callback returning true will be removed from this
      * list, too, after being called.
      */
     protected List<Callable<Boolean>> removeListenersCallbacks = new LinkedList<Callable<Boolean>>();
 
     /**
      * 
      */
     public StepRenderer()
     {
         setLayout(new BorderLayout());
 
         // Subclassing JCanvas3D is needed to call the protected method.
         // The call is needed because it is a workaround for the offscreen canvas not being notified of AWT events
         // with no registered listeners.
         canvas = new JCanvas3D(new GraphicsConfigTemplate3D()) {
             /** */
             private static final long serialVersionUID = 1159847610761430144L;
             {
                 enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
                         | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
             }
         };
 
         canvas.setOpaque(false);
         canvas.setResizeMode(JCanvas3D.RESIZE_DELAYED);
         add(canvas, BorderLayout.CENTER);
         canvas.setSize(new Dimension(20, 20));
         canvas.setResizeMode(JCanvas3D.RESIZE_IMMEDIATELY);
         if (getWidth() > 0 && getHeight() > 0)
             canvas.setSize(getWidth(), getHeight());
 
         offscreenCanvas = canvas.getOffscreenCanvas3D();
         universe = new SimpleUniverse(offscreenCanvas);
 
         setOpaque(false);
         MouseListener listener = new MouseListener();
         addMouseWheelListener(listener);
         addMouseMotionListener(listener);
         addMouseListener(listener);
 
         maxAnisotropyLevel = (Float) offscreenCanvas.queryProperties().get("textureAnisotropicFilterDegreeMax");
     }
 
     /**
      * @param origami
      * @param step
      */
     public StepRenderer(Origami origami, Step step)
     {
         this();
         setOrigami(origami);
         setStep(step);
     }
 
     /**
      * @return the origami
      */
     public Origami getOrigami()
     {
         return origami;
     }
 
     /**
      * @param origami the origami to set
      */
     public void setOrigami(Origami origami)
     {
         this.origami = origami;
 
         if (origami != null) {
             setBackground(origami.getPaper().getColor().getBackground());
             createColorManager(origami.getModel().getPaper().getBackgroundColor(), origami.getModel().getPaper()
                     .getForegroundColor());
         } else {
             setBackground(Color.GRAY); // TODO some more convenient behavior
         }
     }
 
     /**
      * @return the step
      */
     public Step getStep()
     {
         return step;
     }
 
     /**
      * @param stepId the step to set
      */
     public void setStep(final Step step)
     {
         Runnable run = new Runnable() {
             @Override
             public void run()
             {
                 synchronized (StepRenderer.this) {
                     StepRenderer.this.step = step;
 
                     if (step == null)
                         return;
 
                     if (getWidth() > 0 && getHeight() > 0)
                         canvas.setSize(getWidth(), getHeight());
 
                     try {
                         setupUniverse();
                     } catch (InvalidOperationException e) {
                         Logger.getLogger("application").l7dlog(Level.ERROR, "StepRenderer.InvalidOperationException",
                                 new Object[] { StepRenderer.this.step.getId(), e.getOperation().toString() }, e);
                         // TODO some more clever handling of invalid operations
                     }
 
                     topTexture = null;
                     bottomTexture = null;
 
                     afterSetStep();
 
                     repaint();
                 }
             }
         };
 
         if (SwingUtilities.isEventDispatchThread()) {
             new Thread(run).start();
         } else {
             run.run();
         }
     }
 
     /**
      * This method is called after the thread run by a {@link #setStep(Step)} call is about to finish.
      */
     protected void afterSetStep()
     {
 
     }
 
     /**
      * @return The common attributes of polygons to use for rendering.
      */
     protected PolygonAttributes createPolygonAttributes()
     {
         PolygonAttributes polyAttribs = new PolygonAttributes();
         polyAttribs.setCullFace(PolygonAttributes.CULL_BACK);
         // DEBUG IMPORTANT: The next line allows switching between wireframe and full filling mode
         polyAttribs.setPolygonMode(PolygonAttributes.POLYGON_FILL);
         return polyAttribs;
     }
 
     /**
      * @return The buffered image used for drawing textures.
      */
     protected BufferedImage createTextureBuffer()
     {
         return new BufferedImage(TEXTURE_SIZE, TEXTURE_SIZE, BufferedImage.TYPE_INT_RGB);
     }
 
     /**
      * Initialize the texture graphics to be able to draw fold lines on it after this method finishes.
      * 
      * @param buffer The buffer to create the graphics from.
      * @param bgColor The background color of the graphics.
      * 
      * @return The initialized graphics object.
      */
     protected Graphics2D initTextureGraphics(BufferedImage buffer, Color bgColor)
     {
         Graphics2D graphics = buffer.createGraphics();
 
         graphics.setColor(bgColor);
         graphics.setBackground(bgColor);
         graphics.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());
 
         graphics.setColor(Color.BLACK);
         graphics.setStroke(new BasicStroke(0.5f));
 
         return graphics;
     }
 
     /**
      * Create a texture with image taken from the given buffer. Also set some desired texture attributes.
      * 
      * @param buffer The buffer to take the image from.
      * @return A texture.
      */
     protected Texture createTextureFromBuffer(BufferedImage buffer)
     {
         Texture texture = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGB, buffer.getWidth(), buffer.getHeight());
 
         ImageComponent2D image = new ImageComponent2D(ImageComponent2D.FORMAT_RGB, buffer);
 
         texture.setMagFilter(Texture.NICEST);
         texture.setMinFilter(Texture.NICEST);
         texture.setAnisotropicFilterMode(Texture.ANISOTROPIC_SINGLE_VALUE);
         texture.setAnisotropicFilterDegree(Math.min(4f, maxAnisotropyLevel));
         texture.setImage(0, image);
 
         return texture;
     }
 
     /**
      * @return Return (and generate if it doesn't exist) the texture for the top side of the paper.
      */
     protected Texture getTopTexture()
     {
         if (topTexture == null) {
             BufferedImage buffer = createTextureBuffer();
             Graphics2D graphics = initTextureGraphics(buffer, getColorManager().getForeground());
 
             int w = buffer.getWidth();
             int h = buffer.getHeight();
             ModelSegment segment;
             for (LineArray array : step.getModelState().getLineArrays()) {
                 segment = (ModelSegment) array.getUserData();
                 graphics.setStroke(strokeFactory.getForDirection(segment.getDirection(),
                         step.getId() - segment.getOriginatingStepId()));
 
                 Segment2d seg = segment.getOriginal();
                 graphics.drawLine((int) (seg.getP1().x * w), (int) (h - seg.getP1().y * h), (int) (seg.getP2().x * w),
                         (int) (h - seg.getP2().y * h));
             }
 
             topTexture = createTextureFromBuffer(buffer);
         }
         return topTexture;
     }
 
     /**
      * @return Return (and generate if it doesn't exist) the texture for the bottom side of the paper.
      */
     protected Texture getBottomTexture()
     {
         if (bottomTexture == null) {
             BufferedImage buffer = createTextureBuffer();
             Graphics2D graphics = initTextureGraphics(buffer, getColorManager().getBackground());
 
             int w = buffer.getWidth();
             int h = buffer.getHeight();
             ModelSegment segment;
             for (LineArray array : step.getModelState().getLineArrays()) {
                 segment = (ModelSegment) array.getUserData();
                 graphics.setStroke(strokeFactory.getForDirection(segment.getDirection() == null ? null : segment
                         .getDirection().getOpposite(), step.getId() - segment.getOriginatingStepId()));
 
                 Segment2d seg = segment.getOriginal();
                 graphics.drawLine((int) (seg.getP1().x * w), (int) (h - seg.getP1().y * h), (int) (seg.getP2().x * w),
                         (int) (h - seg.getP2().y * h));
             }
 
             bottomTexture = createTextureFromBuffer(buffer);
         }
         return bottomTexture;
     }
 
     /**
      * @return The appearance of triangles that is common for both sides of the paper.
      */
     protected Appearance createBaseTrianglesAppearance()
     {
         Appearance appearance = new Appearance();
 
         appearance.setPolygonAttributes(createPolygonAttributes());
 
         ColoringAttributes colAttrs = new ColoringAttributes();
         colAttrs.setShadeModel(ColoringAttributes.NICEST);
         appearance.setColoringAttributes(colAttrs);
 
         appearance.setTextureAttributes(new TextureAttributes());
         appearance.getTextureAttributes().setPerspectiveCorrectionMode(TextureAttributes.NICEST);
         appearance.getTextureAttributes().setTextureMode(TextureAttributes.COMBINE);
 
         appearance.setRenderingAttributes(new RenderingAttributes());
 
         appearance.setTransparencyAttributes(new TransparencyAttributes());
         appearance.getTransparencyAttributes().setTransparencyMode(TransparencyAttributes.NICEST);
 
         return appearance;
     }
 
     /**
      * @return The appearance of triangles that represent the foreground of the paper.
      */
     protected Appearance createNormalTrianglesAppearance()
     {
         Appearance appearance = createBaseTrianglesAppearance();
 
         appearance.getColoringAttributes().setColor(getColorManager().getForeground3f());
 
         appearance.setTexture(getTopTexture());
 
         return appearance;
     }
 
     /**
      * @return The appearance of triangles that represent the background of the paper.
      */
     protected Appearance createInverseTrianglesAppearance()
     {
         Appearance appearance = createBaseTrianglesAppearance();
 
         appearance.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_FRONT);
 
         appearance.getColoringAttributes().setColor(getColorManager().getBackground3f());
 
         appearance.setTexture(getBottomTexture());
 
         return appearance;
     }
 
     /**
      * @return The basic appearance of lines representing folds.
      */
     protected Appearance createBasicLinesAppearance()
     {
         Appearance appearance = new Appearance();
 
         ColoringAttributes colAttrs = new ColoringAttributes(getColorManager().getLine3f(), ColoringAttributes.NICEST);
         appearance.setColoringAttributes(colAttrs);
 
         appearance.setTransparencyAttributes(new TransparencyAttributes());
 
         appearance.setRenderingAttributes(new RenderingAttributes());
 
         final LineAttributes lineAttrs = new LineAttributes();
         lineAttrs.setLineAntialiasingEnable(true);
         lineAttrs.setCapability(LineAttributes.ALLOW_WIDTH_READ);
         lineAttrs.setCapability(LineAttributes.ALLOW_WIDTH_WRITE);
         appearance.setLineAttributes(lineAttrs);
 
         addPropertyChangeListener("zoom", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 double oldZoom = (Double) evt.getOldValue();
                 double newZoom = (Double) evt.getNewValue();
                 lineAttrs.setLineWidth(lineAttrs.getLineWidth() * (float) (newZoom / oldZoom));
             }
         });
 
         return appearance;
     }
 
     /**
      * Set this.transform to a new value.
      * 
      * @return The transform used for the step just after initialization.
      * 
      * @throws InvalidOperationException If the model state cannot be gotten due to invalid operations.
      */
     protected Transform3D setupTransform() throws InvalidOperationException
     {
         ModelState state = step.getModelState();
 
         transform.setEuler(new Vector3d(state.getViewingAngle() - Math.PI / 2.0, 0, state.getRotation()));
         // TODO adjust zoom according to paper size and renderer size - this is placeholder code
         transform.setScale((step.getZoom() / 100d) * (zoom / 100d));
         // transform
         // .setScale(new Vector3d(5 * transform.getScale(), 5 * transform.getScale(), 500 * transform.getScale()));
         UnitDimension paperSize = origami.getModel().getPaper().getSize().convertTo(Unit.M);
         transform.setTranslation(new Vector3d(-paperSize.getWidth() / 2.0, -paperSize.getHeight() / 2.0, 0));
 
         return transform;
     }
 
     /**
      * @return The transform groups containing nodes for displaying markers.
      */
     protected TransformGroup getMarkerGroups()
     {
         ModelState state = step.getModelState();
         final TransformGroup result = new TransformGroup();
 
         double oneRelInMeters = origami.getModel().getPaper().getOneRelInMeters();
         Font3D font = new Font3D(markerFont, new FontExtrusion());
         // scale of the 3D font; this should make 12pt font be 1/10 of the side of the paper large
         double scale = 1d / 12d * oneRelInMeters * 1d / 10d;
 
         Appearance textAp = new Appearance();
         Material m = new Material();
         textAp.setMaterial(m);
         textAp.setColoringAttributes(new ColoringAttributes(getColorManager().getMarker3f(), ColoringAttributes.FASTEST));
         if (textAp.getRenderingAttributes() == null)
             textAp.setRenderingAttributes(new RenderingAttributes());
         // draw markers always on the top
         textAp.getRenderingAttributes().setDepthTestFunction(RenderingAttributes.ALWAYS);
 
         for (MarkerRenderData marker : state.getMarkerRenderData()) {
             TransformGroup group = new TransformGroup();
             group.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
             group.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 
             // we use Text3D here bacause Text2D looks very, very blurry (even if it isn't zoomed)
             Text3D textGeom = new Text3D(font, marker.getText());
 
             OrientedShape3D text = new OrientedShape3D(textGeom, textAp, OrientedShape3D.ROTATE_ABOUT_POINT,
                     new Point3f());
             group.addChild(text);
 
             Transform3D transform = new Transform3D();
             transform.setScale(scale);
             Vector3d translation = new Vector3d(marker.getPoint3d());
             translation.scale(oneRelInMeters);
             transform.setTranslation(translation);
 
             group.setTransform(transform);
 
             // TODO add a behavior for fine-positioning colliding markers
 
             result.addChild(group);
         }
 
         return result;
     }
 
     /**
      * Set this.tGroup to a new value.
      * 
      * @return The transform group that contains all nodes.
      * 
      * @throws InvalidOperationException If the model state cannot be gotten due to invalid operations.
      */
     protected TransformGroup setupTGroup() throws InvalidOperationException
     {
         try {
             setupTransform();
 
             ModelState state = step.getModelState();
 
             tGroup = new TransformGroup();
             tGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
             tGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
             tGroup.setTransform(transform);
 
             OrderedGroup og = new OrderedGroup();
 
             Group model = new TransformGroup();
 
             TriangleArray[] triangleArrays = state.getTrianglesArrays();
             Shape3D top, bottom;
             Appearance appearance;
             Appearance appearance2;
             for (TriangleArray triangleArray : triangleArrays) {
                 appearance = createNormalTrianglesAppearance();
                 appearance2 = createInverseTrianglesAppearance();
 
                 top = new Shape3D(triangleArray, appearance);
                 bottom = new Shape3D(triangleArray, appearance2);
 
                 model.addChild(top);
                 model.addChild(bottom);
             }
 
             LineArray[] lineArrays = state.getLineArrays();
 
             Group lines = new TransformGroup();
 
             Appearance appearance3;
             Shape3D shape;
             for (LineArray lineArray : lineArrays) {
                 ModelSegment segment = (ModelSegment) lineArray.getUserData();
                 appearance3 = createBasicLinesAppearance();
                 getLineAppearanceManager().alterBasicAppearance(appearance3, segment.getDirection(),
                         step.getId() - segment.getOriginatingStepId());
 
                 shape = new Shape3D(lineArray, appearance3);
 
                 lines.addChild(shape);
             }
             model.addChild(lines);
 
             og.addChild(model);
 
             og.addChild(getMarkerGroups());
 
             tGroup.addChild(og);
 
             return tGroup;
         } catch (InvalidOperationException e) {
             tGroup = new TransformGroup();
             // TODO create an ErrorTransformGroup that would signalize to the user that an operation is invalid
             throw e;
         }
     }
 
     /**
      * Return the compiled BranchGroup containing this.tGroup and all defined top-level behaviors. Also save the group
      * to this.branchGraph.
      * 
      * @return The compiled BranchGroup containing this.tGroup and all defined top-level behaviors.
      * 
      * @throws InvalidOperationException If the branch graph cannot be created due to an invalid operation in the model.
      */
     protected BranchGroup createBranchGraph() throws InvalidOperationException
     {
         try {
             branchGraph = new BranchGroup();
             branchGraph.setCapability(BranchGroup.ALLOW_DETACH);
 
             setupTGroup();
 
             branchGraph.addChild(tGroup);
 
             // TODO now these three lines enable rotating. Either make a whole concept of controlling the
             // displayed step, or delete them
             Behavior rotate = new MouseRotate(tGroup) {
                 @Override
                 public void transformChanged(Transform3D transform)
                 {
                     StepRenderer.this.transform = transform;
                 }
             };
             rotate.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000000d));
             branchGraph.addChild(rotate);
 
             branchGraph.compile(); // may cause unexpected problems - any consequent change of contents
             // (or even reading of them) will produce an error if you don't set the proper capability
 
             return branchGraph;
         } catch (InvalidOperationException e) {
             branchGraph = new BranchGroup();
             branchGraph.addChild(tGroup);
             branchGraph.compile(); // may cause unexpected problems - any consequent change of contents
             // (or even reading of them) will produce an error if you don't set the proper capability
             throw e;
         }
     }
 
     /**
      * Set a new universe corresponding to the current step to this.universe.
      * 
      * @throws InvalidOperationException If the universe cannot be setup due to an invalid operation in the model.
      */
     protected void setupUniverse() throws InvalidOperationException
     {
         universe.getViewer().setViewingPlatform(null);
         universe.getViewer().getView().removeAllCanvas3Ds();
 
         universe = new SimpleUniverse(offscreenCanvas);
         universe.getViewingPlatform().setNominalViewingTransform();
 
         try {
             createBranchGraph();
             universe.addBranchGraph(branchGraph);
         } catch (InvalidOperationException e) {
             universe.addBranchGraph(branchGraph);
             throw e;
         }
     }
 
     /**
      * Create a new color manager for the given colors.
      * 
      * @param background The background color (if null, WHITE is used).
      * @param foreground The foreground color (if null, WHITE is used).
      * @return The created color manager.
      */
     protected ColorManager createColorManager(Color background, Color foreground)
     {
         return colorManager = new ColorManager(background == null ? Color.WHITE : background,
                 foreground == null ? Color.WHITE : foreground);
     }
 
     /**
      * @return The current color manager.
      */
     protected ColorManager getColorManager()
     {
         if (colorManager == null)
             createColorManager(null, null);
         return colorManager;
     }
 
     /**
      * Create a new line appearance manager.
      * 
      * @return The created line appearance manager.
      */
     protected LineAppearanceManager createLineAppearanceManager()
     {
         return lineAppearanceManager = new LineAppearanceManager();
     }
 
     /**
      * @return The current line appearance manager.
      */
     protected LineAppearanceManager getLineAppearanceManager()
     {
         if (lineAppearanceManager == null)
             createLineAppearanceManager();
         return lineAppearanceManager;
     }
 
     /**
      * @return the zoom
      */
     public double getZoom()
     {
         return zoom;
     }
 
     /**
      * @param zoom the zoom to set
      */
     public void setZoom(double zoom)
     {
         if (!isEnabled())
             return;
 
         if (zoom < 25d)
             return;
 
         double oldZoom = this.zoom;
         this.zoom = zoom;
         listeners.firePropertyChange("zoom", oldZoom, zoom);
 
        if (step != null) {
            transform.setScale((step.getZoom() / 100d) * (zoom / 100d));
            tGroup.setTransform(transform);
        }
     }
 
     /**
      * Increase zoom by 10%.
      */
     public void incZoom()
     {
         setZoom(getZoom() + 10d);
     }
 
     /**
      * Decrease zoom by 10%.
      */
     public void decZoom()
     {
         setZoom(getZoom() - 10d);
     }
 
     /**
      * @return The font to use for drawing markers.
      */
     Font getMarkerFont()
     {
         return markerFont;
     }
 
     /**
      * @param markerFont The font to use for drawing markers.
      */
     void setMarkerFont(Font markerFont)
     {
         this.markerFont = markerFont;
     }
 
     /**
      * Call all removeListenersCallbacks and remove those that have succeeded.
      */
     protected void removeUnnecessaryListeners()
     {
         for (Iterator<Callable<Boolean>> it = removeListenersCallbacks.iterator(); it.hasNext();) {
             try {
                 if (it.next().call())
                     it.remove();
             } catch (Exception e) {}
         }
     }
 
     /**
      * @param listener
      * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
      */
     public void addPropertyChangeListener(PropertyChangeListener listener)
     {
         listeners.addPropertyChangeListener(listener);
     }
 
     /**
      * @param listener
      * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
      */
     public void removePropertyChangeListener(PropertyChangeListener listener)
     {
         listeners.removePropertyChangeListener(listener);
     }
 
     /**
      * @return
      * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
      */
     public PropertyChangeListener[] getPropertyChangeListeners()
     {
         return listeners.getPropertyChangeListeners();
     }
 
     /**
      * @param propertyName
      * @param listener
      * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String,
      *      java.beans.PropertyChangeListener)
      */
     public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
     {
         listeners.addPropertyChangeListener(propertyName, listener);
     }
 
     /**
      * @param propertyName
      * @param listener
      * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String,
      *      java.beans.PropertyChangeListener)
      */
     public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
     {
         listeners.removePropertyChangeListener(propertyName, listener);
     }
 
     /**
      * @param propertyName
      * @return
      * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
      */
     public PropertyChangeListener[] getPropertyChangeListeners(String propertyName)
     {
         return listeners.getPropertyChangeListeners(propertyName);
     }
 
     /**
      * @param l
      * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
      */
     public synchronized void addKeyListener(KeyListener l)
     {
         canvas.addKeyListener(l);
     }
 
     /**
      * @param l
      * @see java.awt.Component#addMouseListener(java.awt.event.MouseListener)
      */
     public synchronized void addMouseListener(java.awt.event.MouseListener l)
     {
         canvas.addMouseListener(l);
     }
 
     /**
      * @param l
      * @see java.awt.Component#addMouseMotionListener(java.awt.event.MouseMotionListener)
      */
     public synchronized void addMouseMotionListener(MouseMotionListener l)
     {
         canvas.addMouseMotionListener(l);
     }
 
     /**
      * @param l
      * @see java.awt.Component#addMouseWheelListener(java.awt.event.MouseWheelListener)
      */
     public synchronized void addMouseWheelListener(MouseWheelListener l)
     {
         canvas.addMouseWheelListener(l);
     }
 
     /**
      * @param l
      * @see java.awt.Component#addInputMethodListener(java.awt.event.InputMethodListener)
      */
     public synchronized void addInputMethodListener(InputMethodListener l)
     {
         canvas.addInputMethodListener(l);
     }
 
     /**
      * Mouse event and picking handling.
      * 
      * @author Martin Pecka
      */
     protected class MouseListener extends MouseAdapter
     {
         @Override
         public void mouseWheelMoved(MouseWheelEvent e)
         {
             e.consume();
             int steps = e.getWheelRotation();
             if (steps == 0)
                 return;
 
             if (e.isControlDown() || (e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) > 0) {
                 if (steps > 0) {
                     Action action = new ZoomInAction();
                     ActionEvent event = new ActionEvent(StepRenderer.this, ActionEvent.ACTION_FIRST, "zoomIn");
                     for (int i = 0; i < steps; i++)
                         action.actionPerformed(event);
                 } else {
                     Action action = new ZoomOutAction();
                     ActionEvent event = new ActionEvent(StepRenderer.this, ActionEvent.ACTION_FIRST, "zoomOut");
                     for (int i = steps; i < 0; i++)
                         action.actionPerformed(event);
                 }
             }
         }
     }
 
     protected class ZoomInAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = 313512643556762110L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             incZoom();
         }
 
     }
 
     protected class ZoomOutAction extends AbstractAction
     {
 
         /** */
         private static final long serialVersionUID = -5340289900894828612L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             decZoom();
         }
 
     }
 
     /**
      * Manager of all colors used in this {@link StepRenderer}.
      * 
      * @author Martin Pecka
      */
     protected class ColorManager
     {
         /** Color of textual markers' text. */
         protected Color marker = Color.BLACK;
         /** Paper background color. */
         protected Color background;
         /** Paper foreground color. */
         protected Color foreground;
         /** Color of a fold line. */
         protected Color line   = Color.BLACK;
 
         /**
          * @param background Paper background color.
          * @param foreground Paper foreground color.
          */
         public ColorManager(Color background, Color foreground)
         {
             this.background = background;
             this.foreground = foreground;
         }
 
         /**
          * @return Color of textual markers' text.
          */
         public Color getMarker()
         {
             return marker;
         }
 
         /**
          * @return Color of textual markers' text.
          */
         public Color3f getMarker3f()
         {
             return new Color3f(marker);
         }
 
         /**
          * @param marker Color of textual markers' text.
          */
         public void setMarker(Color marker)
         {
             this.marker = marker;
         }
 
         /**
          * @return Paper background color.
          */
         public Color getBackground()
         {
             return background;
         }
 
         /**
          * @return Paper background color.
          */
         public Color3f getBackground3f()
         {
             return new Color3f(background);
         }
 
         /**
          * @param background Paper background color.
          */
         public void setBackground(Color background)
         {
             this.background = background;
         }
 
         /**
          * @return Paper foreground color.
          */
         public Color getForeground()
         {
             return foreground;
         }
 
         /**
          * @return Paper foreground color.
          */
         public Color3f getForeground3f()
         {
             return new Color3f(foreground);
         }
 
         /**
          * @param foreground Paper foreground color.
          */
         public void setForeground(Color foreground)
         {
             this.foreground = foreground;
         }
 
         /**
          * @return Color of a fold line.
          */
         public Color getLine()
         {
             return line;
         }
 
         /**
          * @return Color of a fold line.
          */
         public Color3f getLine3f()
         {
             return new Color3f(line);
         }
 
         /**
          * @param line Color of a fold line.
          */
         public void setLine(Color line)
         {
             this.line = line;
         }
     }
 
     /**
      * A factory that handles different {@link Stroke}s.
      * 
      * @author Martin Pecka
      */
     protected class StrokeFactory
     {
         private final float[] mountainStroke = new float[] { 20f, 5f, 3f, 5f };
         private final float[] valleyStroke   = new float[] { 20f, 10f };
 
         /** Array of strokes - first is fold type, then fold age. */
         protected Stroke[][]  textureStrokes;
 
         public StrokeFactory()
         {
             textureStrokes = new Stroke[Direction.values().length + 1][];
 
             textureStrokes[getIndex(null)] = new Stroke[] { new BasicStroke(3f) };
 
             textureStrokes[getIndex(Direction.MOUNTAIN)] = new Stroke[] {
                     new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, mountainStroke, 0f),
                     new BasicStroke(2f), new BasicStroke(1f), new BasicStroke(0.5f) };
 
             textureStrokes[getIndex(Direction.VALLEY)] = new Stroke[] {
                     new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, valleyStroke, 0f),
                     new BasicStroke(2f), new BasicStroke(1f), new BasicStroke(0.5f) };
         }
 
         protected int getIndex(Direction dir)
         {
             if (dir != null)
                 return dir.ordinal();
             else
                 return Direction.values().length;
         }
 
         /**
          * Get the stroke to paint a fold line with.
          * 
          * @param direction The direction of the fold line.
          * @param age The age of the fold line (in steps, 0 means this step).
          * @return The stroke to paint a fold line with.
          */
         public Stroke getForDirection(Direction direction, int age)
         {
             Stroke[] byAge = textureStrokes[getIndex(direction)];
             if (age < byAge.length)
                 return byAge[age];
             else
                 return byAge[byAge.length - 1];
         }
     }
 
     /**
      * A manager for changing line appearance.
      * 
      * @author Martin Pecka
      */
     protected class LineAppearanceManager
     {
         /**
          * Take a basic appearance and set it up according to the direction and age of the fold it represents.
          * 
          * @param app The appearance to setup.
          * @param dir The direction of the fold.
          * @param age The age of the fold (in steps).
          */
         public void alterBasicAppearance(Appearance app, Direction dir, int age)
         {
             if (app == null)
                 throw new NullPointerException();
 
             app.getLineAttributes().setLineWidth(getLineWidth(dir, age));
 
             if (dir == null) {
                 return;
             }
 
             if (age == 0) {
                 app.getLineAttributes().setLinePattern(LineAttributes.PATTERN_DASH);
                 app.getRenderingAttributes().setVisible(false);
             }
         }
 
         /**
          * Get the width of a line representing a fold.
          * 
          * @param dir The direction of the fold.
          * @param age The age of the fold (in steps).
          */
         protected float getLineWidth(Direction dir, int age)
         {
             if (dir == null) {
                 return 2f;
             }
 
             switch (age) {
                 case 0:
                     return 1.25f;
                 case 1:
                     return 1.25f;
                 case 2:
                     return 0.85f;
                 default:
                     return 0.5f;
             }
         }
     }
 }
