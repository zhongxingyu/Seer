 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.gui.common;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Stroke;
 import java.awt.event.ActionEvent;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
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
 import java.util.Queue;
 import java.util.concurrent.Callable;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.Behavior;
 import javax.media.j3d.BoundingSphere;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.ColoringAttributes;
 import javax.media.j3d.Font3D;
 import javax.media.j3d.FontExtrusion;
 import javax.media.j3d.Geometry;
 import javax.media.j3d.Group;
 import javax.media.j3d.ImageComponent2D;
 import javax.media.j3d.LineArray;
 import javax.media.j3d.LineAttributes;
 import javax.media.j3d.Material;
 import javax.media.j3d.OrderedGroup;
 import javax.media.j3d.OrientedShape3D;
 import javax.media.j3d.PolygonAttributes;
 import javax.media.j3d.QuadArray;
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
 import javax.swing.ImageIcon;
 import javax.vecmath.AxisAngle4d;
 import javax.vecmath.Color3f;
 import javax.vecmath.Matrix3d;
 import javax.vecmath.Point3d;
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3d;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.sun.j3d.utils.universe.SimpleUniverse;
 import com.sun.j3d.utils.universe.ViewInfo;
 
 import cz.cuni.mff.peckam.java.origamist.exceptions.InvalidOperationException;
 import cz.cuni.mff.peckam.java.origamist.exceptions.PaperStructureException;
 import cz.cuni.mff.peckam.java.origamist.math.Line3d;
 import cz.cuni.mff.peckam.java.origamist.math.MathHelper;
 import cz.cuni.mff.peckam.java.origamist.math.Segment2d;
 import cz.cuni.mff.peckam.java.origamist.math.Segment3d;
 import cz.cuni.mff.peckam.java.origamist.model.DoubleDimension;
 import cz.cuni.mff.peckam.java.origamist.model.Operation;
 import cz.cuni.mff.peckam.java.origamist.model.OperationContainer;
 import cz.cuni.mff.peckam.java.origamist.model.Origami;
 import cz.cuni.mff.peckam.java.origamist.model.Step;
 import cz.cuni.mff.peckam.java.origamist.model.jaxb.Unit;
 import cz.cuni.mff.peckam.java.origamist.model.jaxb.UnitDimension;
 import cz.cuni.mff.peckam.java.origamist.modelstate.Direction;
 import cz.cuni.mff.peckam.java.origamist.modelstate.MarkerRenderData;
 import cz.cuni.mff.peckam.java.origamist.modelstate.ModelSegment;
 import cz.cuni.mff.peckam.java.origamist.modelstate.ModelState;
 import cz.cuni.mff.peckam.java.origamist.utils.ParametrizedCallable;
 
 /**
  * A controller that performs step rendering onto a given Canvas3D.
  * <p>
  * Provided properties:
  * <ul>
  * <li>zoom</li>
  * <li>step (will be called when the setting thread finishes; the old value will always be null).</li>
  * </ul>
  * 
  * @author Martin Pecka
  */
 public class StepViewingCanvasController
 {
     /** The zoom property. */
     public static final String        ZOOM_PROPERTY            = "zoom";
     /** The step property. Will be called when the setting thread finishes; the old value will always be null. */
     public static final String        STEP_PROPERTY            = "step";
 
     /**
      * The origami diagram we are rendering.
      */
     protected Origami                 origami                  = null;
 
     /**
      * The step this renderer is rendering.
      */
     protected Step                    step                     = null;
 
     /** The offscreen canvas used for drawing. */
     protected Canvas3D                canvas;
 
     /** The universe we use. */
     protected SimpleUniverse          universe;
 
     /** The transform computed from the step. */
     protected Transform3D             baseTransform            = new Transform3D();
 
     /** The main transform used to display the step. */
     protected Transform3D             transform                = new Transform3D();
 
     /** The transform group containing the whole step. */
     protected TransformGroup          tGroup;
 
     /** The group containing the whole model. */
     protected Group                   model;
 
     /** The branch graph to be added to the scene. */
     protected BranchGroup             branchGraph              = null;
 
     /** The zoom of the step renderer. */
     protected double                  zoom                     = 100d;
 
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
 
     /** The transform used for initial zooming when a new origami is set. */
     protected Transform3D             initialViewTransform     = null;
 
     protected PropertyChangeSupport   support                  = new PropertyChangeSupport(this);
 
     /** The OSD panel displaying image operations. */
     protected OSDPanel                imageOverlayPanel        = null;
 
     /**
      * @param canvas
      */
     public StepViewingCanvasController(Canvas3D canvas)
     {
         this.canvas = canvas;
 
         // The following resize listener performs scaling of the view as if the offscreen canvas were a square if its
         // width is longer than its height.
 
         // Why is this needed? The canvas adjusts the scale of view according to the canvas' width, but doesn't handle
         // the canvas' height, so the scale of the rendered image is totally dependent on the canvas' width. This
         // listener makes it depend on both width and height.
         canvas.addComponentListener(new ComponentAdapter() {
             @Override
             public void componentResized(ComponentEvent e)
             {
                 adjustSize();
             }
         });
 
         universe = new SimpleUniverse(canvas);
 
         MouseListener listener = new MouseListener();
         addMouseWheelListener(listener);
         addMouseMotionListener(listener);
         addMouseListener(listener);
 
         maxAnisotropyLevel = (Float) canvas.queryProperties().get("textureAnisotropicFilterDegreeMax");
     }
 
     /**
      * @param canvas
      * @param origami
      * @param step
      */
     public StepViewingCanvasController(Canvas3D canvas, Origami origami, Step step)
     {
         this(canvas);
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
         initialViewTransform = null;
 
         if (origami != null) {
             createColorManager(origami.getModel().getPaper().getBackgroundColor(), origami.getModel().getPaper()
                     .getForegroundColor());
             setZoom(100);
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
      * @param step the step to set
      */
     public void setStep(final Step step)
     {
         this.setStep(step, null);
     }
 
     /**
      * @param step the step to set
      * @param afterSetCallback The callback to call after the step is changed. Will be run outside EDT.
      */
     public void setStep(final Step step, final Runnable afterSetCallback)
     {
         setStep(step, afterSetCallback, null);
     }
 
     /**
      * @param step the step to set
      * @param afterSetCallback The callback to call after the step is changed. Will be run outside EDT.
      * @param exceptionCallback The callback to call if the setting thread encounters an
      *            {@link InvalidOperationException} or {@link PaperStructureException}. Will be run outside EDT.
      */
     public void setStep(final Step step, final Runnable afterSetCallback,
             final ParametrizedCallable<?, ? super Exception> exceptionCallback)
     {
         this.step = step;
 
         if (step != null && step.getAttachedTo() == null) {
             return;
         }
 
         new Thread(new Runnable() {
             @Override
             public void run()
             {
                 Exception exception = null;
 
                 synchronized (StepViewingCanvasController.this) {
 
                     try {
                         if (step != null)
                             setupUniverse();
                     } catch (InvalidOperationException e) {
                         Logger.getLogger("application").l7dlog(
                                 Level.ERROR,
                                 "StepRenderer.InvalidOperationException",
                                 new Object[] { StepViewingCanvasController.this.step.getId(),
                                         e.getOperation().toString() }, e);
                         exception = e;
                     } catch (PaperStructureException e) {
                         Logger.getLogger("application").error(e.getMessage(), e);
                         exception = e;
                     } finally {
                         topTexture = null;
                         bottomTexture = null;
                     }
                 }
 
                 support.firePropertyChange(STEP_PROPERTY, null, step);
 
                 if (exception == null) {
                     afterSetStep();
                     if (afterSetCallback != null)
                         afterSetCallback.run();
                 } else if (exceptionCallback != null) {
                     exceptionCallback.call(exception);
                 }
             }
         }).start();
     }
 
     /**
      * This method is called after the thread run by a {@link #setStep(Step)} call is about to finish and it didn't end
      * due to an exception.
      */
     protected void afterSetStep()
     {
         adjustSize();
     }
 
     /**
      * This method makes sure the scale of the rendered image is appropriate with respect to the renderer's size.
      */
     public void adjustSize()
     {
         Dimension size = canvas.getSize();
         if (size.width > size.height) {
             Transform3D trans = computeInitialViewTransform(origami);
             Transform3D scale = new Transform3D();
             scale.set((double) size.width / size.height);
             trans.mul(scale, trans);
             universe.getViewingPlatform().getViewPlatformTransform().setTransform(trans);
         }
     }
 
     /**
      * @return The model state of the current step.
      */
     protected ModelState getModelState()
     {
         return step != null ? step.getModelState(false) : null;
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
      * Return the rectangle the current origami will really ocuppy on the given buffer (assumed you want to scale it to
      * be the largest possible).
      * 
      * @param buffer The buffer the origami will be drawn on.
      * @return The rectangle the origami will ocuppy on the buffer.
      */
     protected Rectangle getUsedBufferPart(BufferedImage buffer)
     {
         DoubleDimension paperDim = origami.getModel().getPaper().getRelativeDimensions();
 
         double horizRatio = buffer.getWidth() / paperDim.getWidth();
         double vertRatio = buffer.getHeight() / paperDim.getHeight();
         double ratio = Math.min(horizRatio, vertRatio);
 
         int usedWidth = (int) (ratio * paperDim.getWidth());
         int usedHeight = (int) (ratio * paperDim.getHeight());
 
         return new Rectangle(0, 0, usedWidth, usedHeight);
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
 
         Rectangle usedPart = getUsedBufferPart(buffer);
 
         graphics.setColor(bgColor);
         graphics.setBackground(bgColor);
         graphics.clearRect(usedPart.x, usedPart.y, usedPart.width, usedPart.height);
 
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
 
             drawTopTextureToBuffer(buffer);
 
             topTexture = createTextureFromBuffer(buffer);
         }
         return topTexture;
     }
 
     /**
      * Draw the top texture onto the specified buffer.
      * 
      * @param buffer The buffer to draw to.
      */
     protected void drawTopTextureToBuffer(BufferedImage buffer)
     {
         Graphics2D graphics = initTextureGraphics(buffer, getColorManager().getForeground());
 
         Rectangle usedPart = getUsedBufferPart(buffer);
         int x = usedPart.x, y = usedPart.y;
         int w = usedPart.width, h = usedPart.height;
 
         // usedPart contains the really used part of buffer, but we need to compensate that for the shorter side, its
         // most distant point isn't generally 1, but something less; so we take the inverse ratio and multiply it with
         // the shorter dimension to compensate this effect
         DoubleDimension paperDim = origami.getModel().getPaper().getRelativeDimensions();
         if (paperDim.getWidth() >= paperDim.getHeight()) {
             h = (int) (h * paperDim.getWidth() / paperDim.getHeight());
         } else {
             w = (int) (w * paperDim.getHeight() / paperDim.getWidth());
         }
 
         ModelSegment segment;
         for (LineArray array : getModelState().getLineArrays()) {
             segment = (ModelSegment) array.getUserData();
             graphics.setStroke(strokeFactory.getForDirection(segment.getDirection(),
                     step.getId() - segment.getOriginatingStepId()));
 
             Segment2d seg = segment.getOriginal();
             graphics.drawLine(x + (int) (seg.getP1().x * w), y + (int) (h - seg.getP1().y * h), x
                     + (int) (seg.getP2().x * w), y + (int) (h - seg.getP2().y * h));
         }
     }
 
     /**
      * @return Return (and generate if it doesn't exist) the texture for the bottom side of the paper.
      */
     protected Texture getBottomTexture()
     {
         if (bottomTexture == null) {
             BufferedImage buffer = createTextureBuffer();
 
             drawBottomTextureToBuffer(buffer);
 
             bottomTexture = createTextureFromBuffer(buffer);
         }
         return bottomTexture;
     }
 
     /**
      * Draw the bottom texture onto the specified buffer.
      * 
      * @param buffer The buffer to draw to.
      */
     protected void drawBottomTextureToBuffer(BufferedImage buffer)
     {
         Graphics2D graphics = initTextureGraphics(buffer, getColorManager().getBackground());
 
         Rectangle usedPart = getUsedBufferPart(buffer);
         int x = usedPart.x, y = usedPart.y;
         int w = usedPart.width, h = usedPart.height;
 
         // usedPart contains the really used part of buffer, but we need to compensate that for the shorter side, its
         // most distant point isn't generally 1, but something less; so we take the inverse ratio and multiply it with
         // the shorter dimension to compensate this effect
         DoubleDimension paperDim = origami.getModel().getPaper().getRelativeDimensions();
         if (paperDim.getWidth() >= paperDim.getHeight()) {
             h = (int) (h * paperDim.getWidth() / paperDim.getHeight());
         } else {
             w = (int) (w * paperDim.getHeight() / paperDim.getWidth());
         }
 
         ModelSegment segment;
         for (LineArray array : getModelState().getLineArrays()) {
             segment = (ModelSegment) array.getUserData();
             graphics.setStroke(strokeFactory.getForDirection(segment.getDirection() == null ? null : segment
                     .getDirection().getOpposite(), step.getId() - segment.getOriginatingStepId()));
 
             Segment2d seg = segment.getOriginal();
             graphics.drawLine(x + (int) (seg.getP1().x * w), y + (int) (h - seg.getP1().y * h), x
                     + (int) (seg.getP2().x * w), y + (int) (h - seg.getP2().y * h));
         }
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
 
         addPropertyChangeListener(ZOOM_PROPERTY, new PropertyChangeListener() {
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
      * @return The current normal of the screen in vworld coordinates.
      */
     public Vector3d getCurrentScreenNormal()
     {
         ViewInfo vi = new ViewInfo(canvas.getView());
         Transform3D imagePlateToVworld = new Transform3D();
         vi.getImagePlateToVworld(canvas, imagePlateToVworld, null);
 
         Point3d eyePos = new Point3d();
         canvas.getCenterEyeInImagePlate(eyePos);
         imagePlateToVworld.transform(eyePos);
 
         Point3d centerPos = new Point3d();
         canvas.getCenterEyeInImagePlate(centerPos);
         centerPos.z = 0;
         imagePlateToVworld.transform(centerPos);
 
         Vector3d screenNormal = new Vector3d(eyePos);
         screenNormal.sub(centerPos);
         screenNormal.normalize();
 
         return screenNormal;
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
         ModelState state = getModelState();
 
         ViewInfo vi = new ViewInfo(canvas.getView());
         Transform3D imagePlateToVworld = new Transform3D();
         vi.getImagePlateToVworld(canvas, imagePlateToVworld, null);
 
         Point3d centerPos = new Point3d();
         canvas.getCenterEyeInImagePlate(centerPos);
         centerPos.z = 0;
         imagePlateToVworld.transform(centerPos);
 
         Point3d screenLeftCenter = new Point3d();
         canvas.getCenterEyeInImagePlate(screenLeftCenter);
         screenLeftCenter.x = 0;
         screenLeftCenter.z = 0;
         imagePlateToVworld.transform(screenLeftCenter);
 
         Vector3d screenDirection = new Vector3d(screenLeftCenter);
         screenDirection.sub(centerPos);
 
         Point3d axis = new Point3d(screenDirection);
         Transform3D viewingAngleRotation = new Transform3D();
         viewingAngleRotation.set(new AxisAngle4d(new Vector3d(axis), state.getViewingAngle() - Math.PI / 2.0));
 
         transform.set(viewingAngleRotation);
 
         Vector3d screenNormal = getCurrentScreenNormal();
 
         Transform3D rotation = new Transform3D();
         rotation.set(new AxisAngle4d(screenNormal, state.getRotation()));
 
         transform.mul(rotation);
 
         Transform3D scale = new Transform3D();
         scale.setScale(getCompositeZoom() / 100d);
         transform.mul(scale);
 
         if (model != null && model.getBounds() != null) {
             Point3d modelCenter = new Point3d();
             ((BoundingSphere) model.getBounds()).getCenter(modelCenter);
             modelCenter.negate();
 
             Transform3D translation = new Transform3D();
             translation.setTranslation(new Vector3d(modelCenter));
             transform.mul(translation);
         }
 
         baseTransform = new Transform3D(transform);
 
         return transform;
     }
 
     /**
      * @return The transform groups containing nodes for displaying markers.
      */
     protected Group getMarkerGroups()
     {
         ModelState state = getModelState();
         final BranchGroup result = new BranchGroup();
 
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
             // we use Text3D here bacause Text2D looks very, very blurry (even if it isn't zoomed)
             Text3D textGeom = new Text3D(font, marker.getText());
 
             Transform3D transform = new Transform3D();
             transform.setScale(scale);
             Vector3d translation = new Vector3d(marker.getPoint3d());
             translation.scale(oneRelInMeters);
             transform.setTranslation(translation);
 
             // TODO add a behavior for fine-positioning colliding markers
 
             result.addChild(createBillboard(textGeom, textAp, transform));
         }
 
         return result;
     }
 
     /**
      * @return The node containing all signs of current step's operations.
      */
     protected Group getOperationSignsGroup()
     {
         final BranchGroup result = new BranchGroup();
 
         final Matrix3d rot = new Matrix3d();
         baseTransform.get(rot, new Vector3d());
         rot.invert();
         // inverted rotation component of baseTransform
         Transform3D baseRotInv = new Transform3D(rot, new Vector3d(), 1);
 
         double oneRelInMeters = origami.getModel().getPaper().getOneRelInMeters();
 
         // width and height of the shape
         double width = 0.2, height = 0.2;
 
         // used for auto-placing signs with no explicit location
         int usedCorners = 0;
 
         Queue<Operation> operations = new LinkedList<Operation>(getStep().getOperations());
         while (!operations.isEmpty()) {
             Operation o = operations.poll();
 
             // do not add signs for operations hidden in the step by a repeat operation
             if (o instanceof OperationContainer) {
                 OperationContainer oc = (OperationContainer) o;
                 if (oc.areContentsVisible()) {
                     operations.addAll(oc.getOperations());
                 }
             }
 
             ImageIcon image = o.getIcon();
 
             if (image == null)
                 continue;
 
             Point3d startPoint;
             Segment3d markerSegment = o.getMarkerPosition();
             if (markerSegment != null) {
                 startPoint = new Point3d(markerSegment.getP1());
             } else {
                 double x, y;
                 int mod = usedCorners % 4;
                 double radius = ((BoundingSphere) model.getBounds()).getRadius() * Math.sqrt(2) / oneRelInMeters;
                 x = (mod == 0 || mod == 3 ? -width / 2 : radius + width / 2)
                         + ((usedCorners / 4) * width * (mod == 0 || mod == 3 ? -1 : 1));
                 y = (mod > 1 ? radius + height / 2 : -height / 2) + ((usedCorners / 4) * height * (mod > 1 ? 1 : -1));
                 startPoint = new Point3d(x, y, 0);
                 usedCorners++;
             }
 
             float w = image.getIconWidth(), h = image.getIconHeight();
             float hRatio = 1, vRatio = 1; // aspect ratio of the image to draw
             if (w < h)
                 hRatio = w / h;
             else
                 vRatio = h / w;
 
             // textures need power-of-2 long sides
             Dimension textureSize = getTextureSize((int) w, (int) h);
 
             // horizontal and vertical scale of the real image in the texture
             float hScale = w / textureSize.width, vScale = h / textureSize.height;
 
             // the quadrilateral for displaying the image
             QuadArray geom = new QuadArray(4, QuadArray.COORDINATES | QuadArray.TEXTURE_COORDINATE_2);
             geom.setCoordinates(0, new double[] { -width / 2 * hRatio, -height / 2 * vRatio, 0, width / 2 * hRatio,
                     -height / 2 * vRatio, 0, width / 2 * hRatio, height / 2 * vRatio, 0, -width / 2 * hRatio,
                     height / 2 * vRatio, 0 });
             geom.setTextureCoordinates(0, 0, new float[] { 0, 1 - vScale, hScale, 1 - vScale, hScale, 1, 0, 1 });
 
             // setup the texture and other appearance
             BufferedImage buffer = new BufferedImage(textureSize.width, textureSize.height, BufferedImage.TYPE_INT_ARGB);
             Graphics2D g = buffer.createGraphics();
             g.setBackground(new Color(0, 0, 0, 0));
             g.clearRect(0, 0, textureSize.width, textureSize.height);
             g.drawImage(image.getImage(), 0, 0, null);
 
             ImageComponent2D tImage = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA8, buffer);
             Texture2D texture = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, textureSize.width,
                     textureSize.height);
             texture.setImage(0, tImage);
             texture.setMinFilter(Texture.NICEST);
             texture.setMagFilter(Texture.NICEST);
 
             Appearance app = new Appearance();
             app.setRenderingAttributes(new RenderingAttributes());
             app.getRenderingAttributes().setDepthTestFunction(RenderingAttributes.ALWAYS);
             app.getRenderingAttributes().setDepthBufferEnable(false);
             app.getRenderingAttributes().setDepthBufferWriteEnable(false);
             app.setPolygonAttributes(new PolygonAttributes());
             app.getPolygonAttributes().setCullFace(PolygonAttributes.CULL_NONE);
             app.setTextureAttributes(new TextureAttributes());
             app.setColoringAttributes(new ColoringAttributes(new Color3f(Color.yellow), ColoringAttributes.NICEST));
             app.getTextureAttributes().setPerspectiveCorrectionMode(TextureAttributes.NICEST);
             app.getTextureAttributes().setTextureMode(TextureAttributes.REPLACE);
             app.setTransparencyAttributes(new TransparencyAttributes());
             app.getTransparencyAttributes().setTransparencyMode(TransparencyAttributes.BLENDED);
 
             app.setTexture(texture);
 
             // the transform of our shape
             Transform3D transform = new Transform3D();
 
             if (markerSegment != null) {
                 // now we want to align the shape's initial x axis with the line we have found for this operation
                 Vector3d dir = new Vector3d(1, 0, 0);
                 baseRotInv.transform(dir);
                 // if the x axis isn't parallel to the found line, create a rotation matrix between them
                 Double quotient = MathHelper.vectorQuotient3d(markerSegment.getVector(), dir);
                 if (quotient == null) {
                     // take the cross product of the two vectors and compute the angle between them - then we can
                     // construct an AxisAngle4d
                     Vector3d cross = new Vector3d();
                     cross.cross(markerSegment.getVector(), dir);
                     double angle = markerSegment.getVector().angle(dir);
 
                     // angle is cropped to [0,PI], but it can be larger, so detect if we need the larger angle
                     Vector3d v = new Vector3d(markerSegment.getVector());
                     v.normalize();
                     Point3d p1 = new Point3d(markerSegment.getP2());
                     p1.add(v);
                     Point3d p2 = new Point3d(markerSegment.getP2());
                     p2.add(dir);
                     if (!MathHelper.rotate(p1, new Line3d(markerSegment.getP1(), cross), angle).epsilonEquals(p2,
                             1000 * MathHelper.EPSILON)) {
                         angle = -angle;
                     }
 
                     // let the shape's normal point to the screen
                     Vector3d screenNormal = step.getModelState(false).getScreenNormal();
                     transform.set(new AxisAngle4d(cross, angle));
                     Vector3d shapeNormal = new Vector3d(0, 0, 1);
                     baseRotInv.transform(shapeNormal);
                     transform.transform(shapeNormal);
                     transform.transform(dir);
                     Transform3D additionalTransform = new Transform3D();
                     additionalTransform.set(new AxisAngle4d(dir, shapeNormal.angle(screenNormal)));
                     transform.mul(additionalTransform, transform);
 
                 } else if (quotient < 0) {
                     Vector3d axis = new Vector3d(0, 0, 1);
                     baseRotInv.transform(axis);
                     transform.set(new AxisAngle4d(axis, Math.PI));
                 }
             }
 
             startPoint.scale(oneRelInMeters);
 
             // scale of the shape - if we have found a line for this operation, scale this shape to stretch along whole
             // line
             double scale;
             if (markerSegment != null)
                 scale = 2 * markerSegment.getLength() / (width * hRatio) * oneRelInMeters;
             else
                 scale = oneRelInMeters;
 
             transform.setScale(transform.getScale() * scale);
             transform.setTranslation(new Vector3d(startPoint));
 
             // cancel the rotation imposed by baseTransform
             transform.mul(baseRotInv);
 
             // let the upper-left corner match the start of the found segment
             Transform3D initialTranslation = new Transform3D();
             initialTranslation.setTranslation(new Vector3d(width / 2 * hRatio, height / 2 * vRatio, 0));
             transform.mul(initialTranslation);
 
             // create the necessary nodes
             TransformGroup group = new TransformGroup();
 
             Shape3D shape = new Shape3D(geom, app);
             group.addChild(shape);
 
             group.setTransform(transform);
 
             result.addChild(group);
         }
 
         return result;
     }
 
     /**
      * Create a billboard node from the given geometry, appearance and transform. The billboard will rotate about point
      * (0,0,0) in the local coordiantes given by transform.
      * 
      * @param geometry Geometry of the billboard.
      * @param appearance Appearance of the billboard.
      * @param transform Transform of the billboard.
      * @return The node representing the billboard.
      */
     protected Group createBillboard(Geometry geometry, Appearance appearance, Transform3D transform)
     {
         TransformGroup group = new TransformGroup();
         group.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
         group.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 
         OrientedShape3D billboard = new OrientedShape3D(geometry, appearance, OrientedShape3D.ROTATE_ABOUT_POINT,
                 new Point3f());
         group.addChild(billboard);
 
         group.setTransform(transform);
 
         return group;
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
             ModelState state = getModelState();
 
             tGroup = new TransformGroup();
             tGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
             tGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
 
             OrderedGroup og = new OrderedGroup();
 
             model = new TransformGroup();
 
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
 
             setupTransform();
             tGroup.setTransform(transform);
 
             og.addChild(getOperationSignsGroup());
 
             og.addChild(getMarkerGroups());
 
             tGroup.addChild(og);
 
             return tGroup;
         } catch (InvalidOperationException e) {
             tGroup = new TransformGroup();
             tGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
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
 
             createAndAddBranchGraphChildren();
 
             if (imageOverlayPanel != null) {
                 imageOverlayPanel.detachFromUniverse(universe);
                 imageOverlayPanel = null;
             }
 
             if (getModelState().getOverlayImage() != null) {
                 imageOverlayPanel = new OSDPanel(canvas, 0, 0, canvas.getWidth(), canvas.getHeight(), false, false) {
                     @Override
                     protected void paint(Graphics2D graphics)
                     {
                         BufferedImage image = getModelState().getOverlayImage();
                         double h, v;
                        h = (double) canvas.getWidth() / image.getWidth();
                        v = (double) canvas.getHeight() / image.getHeight();
                         double ratio = Math.min(h, v);
 
                         int width = (int) (image.getWidth() * ratio);
                         int height = (int) (image.getHeight() * ratio);
                         graphics.drawImage(image, (canvas.getWidth() - width) / 2, (canvas.getHeight() - height) / 2,
                                 width, height, null);
                     }
                 };
                 imageOverlayPanel.attachToUniverse(universe);
                 branchGraph.removeChild(tGroup);
             }
 
             branchGraph.compile(); // may cause unexpected problems - any consequent change of contents
             // (or even reading of them) will produce an error if you don't set the proper capability
 
             return branchGraph;
         } catch (InvalidOperationException e) {
             branchGraph = new BranchGroup();
             branchGraph.setCapability(BranchGroup.ALLOW_DETACH);
             branchGraph.addChild(tGroup);
             branchGraph.compile(); // may cause unexpected problems - any consequent change of contents
             // (or even reading of them) will produce an error if you don't set the proper capability
             throw e;
         }
     }
 
     /**
      * Create and attach all desired branchGraph children.
      * 
      * @throws InvalidOperationException If a child cannot be created due to an invalid operation in the model.
      */
     protected void createAndAddBranchGraphChildren() throws InvalidOperationException
     {
         setupTGroup();
 
         branchGraph.addChild(tGroup);
 
         Behavior rotate = new CenteredMouseRotate(tGroup) {
             @Override
             public void transformChanged(Transform3D transform)
             {
                 StepViewingCanvasController.this.transform = transform;
             }
 
         };
         rotate.setSchedulingBounds(new BoundingSphere(new Point3d(), 1000000d));
         branchGraph.addChild(rotate);
     }
 
     /**
      * Set a new universe corresponding to the current step to this.universe.
      * 
      * @throws InvalidOperationException If the universe cannot be setup due to an invalid operation in the model.
      */
     protected void setupUniverse() throws InvalidOperationException
     {
         if (universe == null) {
             universe = new SimpleUniverse(canvas);
             canvas.getView().setFrontClipDistance(0);
         }
 
         BranchGroup oldBranchGraph = branchGraph;
         try {
             createBranchGraph();
         } finally {
             if (oldBranchGraph != null)
                 universe.getLocale().removeBranchGraph(oldBranchGraph);
             universe.addBranchGraph(branchGraph);
 
             if (initialViewTransform == null && origami != null) {
                 initialViewTransform = computeInitialViewTransform(origami);
                 universe.getViewingPlatform().getViewPlatformTransform().setTransform(initialViewTransform);
             }
         }
     }
 
     /**
      * Compute the transform to be set to view platform in order the given origami's paper to be well zoomed.
      * 
      * @param origami The origami to compute transform for.
      * @return The transform for viewing platform.
      */
     protected Transform3D computeInitialViewTransform(Origami origami)
     {
         Transform3D result = new Transform3D();
         if (origami == null)
             return result;
         UnitDimension dimension = origami.getModel().getPaper().getSize().convertTo(Unit.M);
         double length = Math.sqrt(Math.pow(dimension.getWidth(), 2) + Math.pow(dimension.getHeight(), 2));
         // inspired in ViewingPlatform#setNominalViewingTransform()
         // length specifies (in vworld meters) half of the visible part of the origami
         result.setTranslation(new Vector3d(0, 0, (length /* / 1d */) / Math.tan(Math.PI / 8d)));
         return result;
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
      * @return The overall zoom of the displayed object (as percentage - 0 to 100).
      */
     public double getCompositeZoom()
     {
         if (step != null)
             return step.getZoom() * zoom / 100d;
         else
             return zoom;
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
         if (zoom < 25d)
             return;
 
         double oldZoom = this.zoom;
         this.zoom = zoom;
         support.firePropertyChange("zoom", oldZoom, zoom);
 
         if (step != null && getModelState() != null && model != null) {
             Transform3D additional = new Transform3D(transform);
             Transform3D baseInverted = new Transform3D(baseTransform);
             baseInverted.invert();
             additional.mul(baseInverted);
 
             setupTransform();
 
             transform.mul(additional, transform);
             if (tGroup != null)
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
             } catch (Exception e) {
                 Logger.getLogger(getClass()).error("Listener removal callback threw exception.", e);
             }
         }
     }
 
     /**
      * @return The canvas.
      */
     public Canvas3D getCanvas()
     {
         return canvas;
     }
 
     /**
      * Return the smallest possible texture size so that the whole rectangle of width and height fits into it.
      * 
      * @param width The minimum width of the texture.
      * @param height The minimum height of the texture.
      * @return Size of texture (both dimensions are powers of 2).
      */
     protected Dimension getTextureSize(int width, int height)
     {
         return new Dimension(getSmallestPower(width), getSmallestPower(height));
     }
 
     /**
      * Return the smallest power of 2 greater than value.
      * 
      * @param value The value to find the smallest non-less power of.
      * @return The smallest power of 2 greater than value.
      */
     protected int getSmallestPower(int value)
     {
         int n = 1;
         while (n < value)
             n <<= 1;
 
         return n;
     }
 
     /**
      * @param l
      * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
      */
     public void addKeyListener(KeyListener l)
     {
         canvas.addKeyListener(l);
     }
 
     /**
      * @param l
      * @see java.awt.Component#addMouseListener(java.awt.event.MouseListener)
      */
     public void addMouseListener(java.awt.event.MouseListener l)
     {
         canvas.addMouseListener(l);
     }
 
     /**
      * @param l
      * @see java.awt.Component#addMouseMotionListener(java.awt.event.MouseMotionListener)
      */
     public void addMouseMotionListener(MouseMotionListener l)
     {
         canvas.addMouseMotionListener(l);
     }
 
     /**
      * @param l
      * @see java.awt.Component#addMouseWheelListener(java.awt.event.MouseWheelListener)
      */
     public void addMouseWheelListener(MouseWheelListener l)
     {
         canvas.addMouseWheelListener(l);
     }
 
     /**
      * @param l
      * @see java.awt.Component#addInputMethodListener(java.awt.event.InputMethodListener)
      */
     public void addInputMethodListener(InputMethodListener l)
     {
         canvas.addInputMethodListener(l);
     }
 
     /**
      * @param listener
      * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
      */
     public void addPropertyChangeListener(PropertyChangeListener listener)
     {
         support.addPropertyChangeListener(listener);
     }
 
     /**
      * @param listener
      * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
      */
     public void removePropertyChangeListener(PropertyChangeListener listener)
     {
         support.removePropertyChangeListener(listener);
     }
 
     /**
      * @param propertyName
      * @param listener
      * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String,
      *      java.beans.PropertyChangeListener)
      */
     public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
     {
         support.addPropertyChangeListener(propertyName, listener);
     }
 
     /**
      * @param propertyName
      * @param listener
      * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String,
      *      java.beans.PropertyChangeListener)
      */
     public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
     {
         support.removePropertyChangeListener(propertyName, listener);
     }
 
     /**
      * @param propertyName
      * @return
      * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
      */
     public boolean hasListeners(String propertyName)
     {
         return support.hasListeners(propertyName);
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
                     for (int i = 0; i < steps; i++)
                         incZoom();
                 } else {
                     for (int i = steps; i < 0; i++)
                         decZoom();
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
