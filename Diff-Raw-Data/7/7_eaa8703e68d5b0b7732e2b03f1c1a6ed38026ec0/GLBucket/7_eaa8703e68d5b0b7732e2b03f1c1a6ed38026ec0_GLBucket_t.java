 package org.caleydo.view.bucket;
 
 import gleem.linalg.Rotf;
 import gleem.linalg.Vec3f;
 import gleem.linalg.Vec4f;
 import gleem.linalg.open.Transform;
 
 import java.awt.Font;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 
 import org.caleydo.core.command.ECommandType;
 import org.caleydo.core.command.view.opengl.CmdCreateView;
 import org.caleydo.core.data.selection.EVAOperation;
 import org.caleydo.core.data.selection.SelectionType;
 import org.caleydo.core.data.selection.delta.ISelectionDelta;
 import org.caleydo.core.data.selection.delta.SelectionDelta;
 import org.caleydo.core.manager.ICommandManager;
 import org.caleydo.core.manager.IEventPublisher;
 import org.caleydo.core.manager.IGeneralManager;
 import org.caleydo.core.manager.ISetBasedDataDomain;
 import org.caleydo.core.manager.IViewManager;
 import org.caleydo.core.manager.datadomain.ASetBasedDataDomain;
 import org.caleydo.core.manager.datadomain.IDataDomainBasedView;
 import org.caleydo.core.manager.event.view.ResetAllViewsEvent;
 import org.caleydo.core.manager.event.view.ViewActivationEvent;
 import org.caleydo.core.manager.event.view.pathway.DisableGeneMappingEvent;
 import org.caleydo.core.manager.event.view.pathway.DisableNeighborhoodEvent;
 import org.caleydo.core.manager.event.view.pathway.DisableTexturesEvent;
 import org.caleydo.core.manager.event.view.pathway.EnableGeneMappingEvent;
 import org.caleydo.core.manager.event.view.pathway.EnableNeighborhoodEvent;
 import org.caleydo.core.manager.event.view.pathway.EnableTexturesEvent;
 import org.caleydo.core.manager.event.view.remote.DisableConnectionLinesEvent;
 import org.caleydo.core.manager.event.view.remote.EnableConnectionLinesEvent;
 import org.caleydo.core.manager.event.view.remote.LoadPathwayEvent;
 import org.caleydo.core.manager.event.view.remote.LoadPathwaysByGeneEvent;
 import org.caleydo.core.manager.event.view.remote.ResetRemoteRendererEvent;
 import org.caleydo.core.manager.event.view.remote.ToggleNavigationModeEvent;
 import org.caleydo.core.manager.event.view.remote.ToggleZoomEvent;
 import org.caleydo.core.manager.event.view.storagebased.SelectionUpdateEvent;
 import org.caleydo.core.manager.general.GeneralManager;
 import org.caleydo.core.manager.id.EManagedObjectType;
 import org.caleydo.core.manager.picking.EPickingMode;
 import org.caleydo.core.manager.picking.EPickingType;
 import org.caleydo.core.manager.picking.Pick;
 import org.caleydo.core.manager.view.ConnectedElementRepresentationManager;
 import org.caleydo.core.manager.view.RemoteRenderingTransformer;
 import org.caleydo.core.serialize.ASerializedView;
 import org.caleydo.core.util.system.SystemTime;
 import org.caleydo.core.util.system.Time;
 import org.caleydo.core.view.IView;
 import org.caleydo.core.view.opengl.camera.EProjectionMode;
 import org.caleydo.core.view.opengl.camera.IViewFrustum;
 import org.caleydo.core.view.opengl.canvas.AGLView;
 import org.caleydo.core.view.opengl.canvas.AStorageBasedView;
 import org.caleydo.core.view.opengl.canvas.EDetailLevel;
 import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
 import org.caleydo.core.view.opengl.canvas.listener.ISelectionUpdateHandler;
 import org.caleydo.core.view.opengl.canvas.listener.ResetViewListener;
 import org.caleydo.core.view.opengl.canvas.listener.SelectionUpdateListener;
 import org.caleydo.core.view.opengl.canvas.remote.AGLConnectionLineRenderer;
 import org.caleydo.core.view.opengl.canvas.remote.ARemoteViewLayoutRenderStyle;
 import org.caleydo.core.view.opengl.canvas.remote.ARemoteViewLayoutRenderStyle.LayoutMode;
 import org.caleydo.core.view.opengl.canvas.remote.GLConnectionLineRendererBucket;
 import org.caleydo.core.view.opengl.canvas.remote.jukebox.GLConnectionLineRendererJukebox;
 import org.caleydo.core.view.opengl.canvas.remote.jukebox.JukeboxLayoutRenderStyle;
 import org.caleydo.core.view.opengl.canvas.remote.list.ListLayoutRenderStyle;
 import org.caleydo.core.view.opengl.mouse.GLMouseListener;
 import org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle;
 import org.caleydo.core.view.opengl.util.drag.GLDragAndDrop;
 import org.caleydo.core.view.opengl.util.hierarchy.RemoteElementManager;
 import org.caleydo.core.view.opengl.util.hierarchy.RemoteLevel;
 import org.caleydo.core.view.opengl.util.hierarchy.RemoteLevelElement;
 import org.caleydo.core.view.opengl.util.overlay.infoarea.GLInfoAreaManager;
 import org.caleydo.core.view.opengl.util.slerp.SlerpAction;
 import org.caleydo.core.view.opengl.util.slerp.SlerpMod;
 import org.caleydo.core.view.opengl.util.texture.EIconTextures;
 import org.caleydo.datadomain.pathway.IPathwayLoader;
 import org.caleydo.datadomain.pathway.graph.PathwayGraph;
 import org.caleydo.datadomain.pathway.listener.LoadPathwaysByGeneListener;
 import org.caleydo.datadomain.pathway.manager.PathwayManager;
 import org.caleydo.rcp.view.listener.AddPathwayListener;
 import org.caleydo.rcp.view.listener.DisableConnectionLinesListener;
 import org.caleydo.rcp.view.listener.DisableGeneMappingListener;
 import org.caleydo.rcp.view.listener.DisableNeighborhoodListener;
 import org.caleydo.rcp.view.listener.DisableTexturesListener;
 import org.caleydo.rcp.view.listener.EnableConnectionLinesListener;
 import org.caleydo.rcp.view.listener.EnableGeneMappingListener;
 import org.caleydo.rcp.view.listener.EnableNeighborhoodListener;
 import org.caleydo.rcp.view.listener.EnableTexturesListener;
 import org.caleydo.rcp.view.listener.IRemoteRenderingHandler;
 import org.caleydo.rcp.view.listener.ToggleNavigationModeListener;
 import org.caleydo.rcp.view.listener.ToggleZoomListener;
 import org.caleydo.view.bookmarking.GLBookmarkManager;
 import org.caleydo.view.pathway.GLPathway;
 import org.caleydo.view.pathway.SerializedPathwayView;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.swt.graphics.Point;
 
 import com.sun.opengl.util.j2d.TextRenderer;
 import com.sun.opengl.util.texture.Texture;
 import com.sun.opengl.util.texture.TextureCoords;
 
 /**
  * Class that is able to remotely rendering views. Subclasses implement the
  * positioning of the views (bucket, jukebox, etc.).
  * 
  * @author Marc Streit
  * @author Alexander Lex
  * @author Werner Puff
  */
 public class GLBucket extends AGLView implements
 		IDataDomainBasedView<ASetBasedDataDomain>, ISelectionUpdateHandler,
 		IGLBucketView, IRemoteRenderingHandler, IPathwayLoader {
 
 	public final static String VIEW_ID = "org.caleydo.view.bucket";
 
 	protected ASetBasedDataDomain dataDomain;
 
 	private ARemoteViewLayoutRenderStyle.LayoutMode layoutMode;
 
 	private static final int SLERP_RANGE = 1000;
 	private static final int SLERP_SPEED = 1400;
 
 	private int iMouseOverObjectID = -1;
 
 	private RemoteLevel focusLevel;
 	private RemoteLevel stackLevel;
 	private RemoteLevel poolLevel;
 	private RemoteLevel transitionLevel;
 	private RemoteLevel spawnLevel;
 	private RemoteLevel externalSelectionLevel;
 
 	private ArrayList<SlerpAction> arSlerpActions;
 
 	private GLBookmarkManager glBookmarkContainer;
 
 	private Time time;
 
 	/**
 	 * Slerp factor: 0 = source; 1 = destination
 	 */
 	private int iSlerpFactor = 0;
 
 	protected AGLConnectionLineRenderer glConnectionLineRenderer;
 
 	private int iNavigationMouseOverViewID_left = -1;
 	private int iNavigationMouseOverViewID_right = -1;
 	private int iNavigationMouseOverViewID_out = -1;
 	private int iNavigationMouseOverViewID_in = -1;
 	private int iNavigationMouseOverViewID_lock = -1;
 
 	private boolean bEnableNavigationOverlay = false;
 
 	private TextRenderer textRenderer;
 
 	private GLDragAndDrop dragAndDrop;
 
 	private ARemoteViewLayoutRenderStyle layoutRenderStyle;
 
 	private BucketMouseWheelListener bucketMouseWheelListener;
 
 	private ArrayList<AGLView> containedGLViews;
 
 	/**
 	 * The current view in which the user is performing actions.
 	 */
 	private int iActiveViewID = -1;
 
 	// private int iGLDisplayList;
 
 	private ISelectionDelta lastSelectionDelta;
 
 	/**
 	 * Used for dragging views to the pool area.
 	 */
 	private int iPoolLevelCommonID = -1;
 
 	private GLOffScreenTextureRenderer glOffScreenRenderer;
 
 	private boolean bUpdateOffScreenTextures = true;
 
 	private boolean connectionLinesEnabled = true;
 
 	/** stores if the gene-mapping should be enabled */
 	private boolean geneMappingEnabled = true;
 
 	/** stores if the pathway textures are enabled */
 	private boolean pathwayTexturesEnabled = true;
 
 	/** stores if the "neighborhood" ist enabled */
 	private boolean neighborhoodEnabled = false;
 
 	private ArrayList<ASerializedView> newViews;
 
 	private GLInfoAreaManager infoAreaManager;
 
 	/**
 	 * Transformation utility object to transform and project view related
 	 * coordinates
 	 */
 	protected RemoteRenderingTransformer selectionTransformer;
 
 	protected AddPathwayListener addPathwayListener;
 	protected LoadPathwaysByGeneListener loadPathwaysByGeneListener;
 	protected EnableGeneMappingListener enableGeneMappingListener;
 	protected DisableGeneMappingListener disableGeneMappingListener;
 	protected EnableTexturesListener enableTexturesListener;
 	protected DisableTexturesListener disableTexturesListener;
 	protected EnableNeighborhoodListener enableNeighborhoodListener;
 	protected DisableNeighborhoodListener disableNeighborhoodListener;
 	protected ToggleNavigationModeListener toggleNavigationModeListener;
 	protected ToggleZoomListener toggleZoomListener;
 	protected EnableConnectionLinesListener enableConnectionLinesListener;
 	protected DisableConnectionLinesListener disableConnectionLinesListener;
 	protected ResetViewListener resetViewListener;
 	protected SelectionUpdateListener selectionUpdateListener;
 
 	private Point upperLeftScreenPos = new Point(0, 0);
 
 	/**
 	 * Constructor.
 	 */
 	public GLBucket(GLCaleydoCanvas glCanvas, final String sLabel,
 			final IViewFrustum viewFrustum,
 			final ARemoteViewLayoutRenderStyle.LayoutMode layoutMode) {
 
 		super(glCanvas, sLabel, viewFrustum, true);
 		viewType = GLBucket.VIEW_ID;
 
 		this.layoutMode = layoutMode;
 
 		if (generalManager.getTrackDataProvider().isTrackModeActive()) {
 			glOffScreenRenderer = new GLOffScreenTextureRenderer();
 		}
 
 		if (layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.BUCKET)) {
 			layoutRenderStyle = new BucketLayoutRenderStyle(viewFrustum);
 			super.renderStyle = layoutRenderStyle;
 
 			bucketMouseWheelListener = new BucketMouseWheelListener(this,
 					(BucketLayoutRenderStyle) layoutRenderStyle);
 
 			// Unregister standard mouse wheel listener
 			parentGLCanvas.removeMouseWheelListener(glMouseListener);
 			// Register specialized bucket mouse wheel listener
 			parentGLCanvas.addMouseWheelListener(bucketMouseWheelListener);
 			// parentGLCanvas.addMouseListener(bucketMouseWheelListener);
 
 		} else if (layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.JUKEBOX)) {
 			layoutRenderStyle = new JukeboxLayoutRenderStyle(viewFrustum);
 		} else if (layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.LIST)) {
 			layoutRenderStyle = new ListLayoutRenderStyle(viewFrustum);
 		}
 
 		focusLevel = layoutRenderStyle.initFocusLevel();
 
 		if (GeneralManager.get().getTrackDataProvider().isTrackModeActive()
 				&& layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.BUCKET)) {
 			stackLevel = ((BucketLayoutRenderStyle) layoutRenderStyle)
 					.initStackLevelWii();
 		} else {
 			stackLevel = layoutRenderStyle.initStackLevel();
 		}
 
 		poolLevel = layoutRenderStyle.initPoolLevel(-1);
 		externalSelectionLevel = layoutRenderStyle.initMemoLevel();
 		transitionLevel = layoutRenderStyle.initTransitionLevel();
 		spawnLevel = layoutRenderStyle.initSpawnLevel();
 
 		if (layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.BUCKET)) {
 			glConnectionLineRenderer = new GLConnectionLineRendererBucket(focusLevel,
 					stackLevel);
 		} else if (layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.JUKEBOX)) {
 			glConnectionLineRenderer = new GLConnectionLineRendererJukebox(focusLevel,
 					stackLevel, poolLevel);
 		} else if (layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.LIST)) {
 			glConnectionLineRenderer = null;
 		}
 
 		glMouseListener.addGLCanvas(this);
 
 		arSlerpActions = new ArrayList<SlerpAction>();
 
 		containedGLViews = new ArrayList<AGLView>();
 		newViews = new ArrayList<ASerializedView>();
 
 		dragAndDrop = new GLDragAndDrop();
 
 		textRenderer = new TextRenderer(new Font("Arial", Font.PLAIN, 24), false);
 
 		iPoolLevelCommonID = generalManager.getIDManager().createID(
 				EManagedObjectType.REMOTE_LEVEL_ELEMENT);
 	}
 
 	@Override
 	public void initLocal(final GL gl) {
 		// iGLDisplayList = gl.glGenLists(1);
 
 		ArrayList<RemoteLevelElement> remoteLevelElementWhiteList = new ArrayList<RemoteLevelElement>();
 		remoteLevelElementWhiteList.addAll(focusLevel.getAllElements());
 		remoteLevelElementWhiteList.addAll(stackLevel.getAllElements());
 		selectionTransformer = new RemoteRenderingTransformer(iUniqueID,
 				remoteLevelElementWhiteList);
 
 		init(gl);
 	}
 
 	@Override
 	public void initRemote(final GL gl, final AGLView glParentView,
 			final GLMouseListener glMouseListener, GLInfoAreaManager infoAreaManager) {
 
 		throw new IllegalStateException("Not implemented to be rendered remote");
 	}
 
 	@Override
 	public void init(final GL gl) {
 		gl.glClearColor(0.5f, 0.5f, 0.5f, 1f);
 
 		if (glConnectionLineRenderer != null) {
 			glConnectionLineRenderer.init(gl);
 		}
 
 		// iconTextureManager = new TextureManager(gl);
 
 		time = new SystemTime();
 		((SystemTime) time).rebase();
 
 		infoAreaManager = new GLInfoAreaManager();
 		infoAreaManager.initInfoInPlace(viewFrustum);
 
 		createSelectionHeatMap();
 		glBookmarkContainer.initRemote(gl, this, glMouseListener, null);
 
 		if (generalManager.getTrackDataProvider().isTrackModeActive())
 			glOffScreenRenderer.init(gl);
 	}
 
 	private void createSelectionHeatMap() {
 		// Create selection panel
 		CmdCreateView cmdCreateGLView = (CmdCreateView) generalManager
 				.getCommandManager().createCommandByType(ECommandType.CREATE_GL_VIEW);
 		cmdCreateGLView.setViewID("org.caleydo.view.bookmarking");
 		cmdCreateGLView.setAttributes(EProjectionMode.ORTHOGRAPHIC, 0, 0.8f, 0.1f, 4.1f,
 				-20, 20, -1);
 		cmdCreateGLView.setDataDomainType(dataDomain.getDataDomainType());
 		cmdCreateGLView.doCommand();
 		glBookmarkContainer = (GLBookmarkManager) cmdCreateGLView.getCreatedObject();
 		glBookmarkContainer.setRemoteRenderingGLView(this);
 		glBookmarkContainer.initData();
 
 		externalSelectionLevel.getElementByPositionIndex(0)
 				.setGLView(glBookmarkContainer);
 	}
 
 	@Override
 	public void displayLocal(final GL gl) {
 
 		for (AGLView view : containedGLViews)
 			view.processEvents();
 
 		if (glBookmarkContainer != null)
 			glBookmarkContainer.processEvents();
 
 		// if (bIsDisplayListDirtyLocal)
 		// {
 		// buildDisplayList(gl);
 		// bIsDisplayListDirtyLocal = false;
 		// }
 
 		pickingManager.handlePicking(this, gl);
 
 		display(gl);
 		ConnectedElementRepresentationManager cerm = GeneralManager.get()
 				.getViewGLCanvasManager().getConnectedElementRepresentationManager();
 		cerm.doViewRelatedTransformation(gl, selectionTransformer);
 
 		if (eBusyModeState != EBusyModeState.OFF) {
 			renderBusyMode(gl);
 		}
 
 		if (glMouseListener.getPickedPoint() != null) {
 			dragAndDrop.setCurrentMousePos(gl, glMouseListener.getPickedPoint());
 		}
 
 		if (dragAndDrop.isDragActionRunning()) {
 			dragAndDrop.renderDragThumbnailTexture(gl,
 					bucketMouseWheelListener.isZoomedIn());
 		}
 
 		if (glMouseListener.wasMouseReleased() && dragAndDrop.isDragActionRunning()) {
 			int iDraggedObjectId = dragAndDrop.getDraggedObjectedId();
 
 			// System.out.println("over: " +iExternalID);
 			// System.out.println("dragged: " +iDraggedObjectId);
 
 			// Prevent user from dragging element onto selection level
 			if (!RemoteElementManager.get().hasItem(iMouseOverObjectID)
 					|| !externalSelectionLevel.containsElement(RemoteElementManager.get()
 							.getItem(iMouseOverObjectID))) {
 				RemoteLevelElement mouseOverElement = null;
 
 				// Check if a drag and drop action is performed onto the pool
 				// level
 				if (iMouseOverObjectID == iPoolLevelCommonID) {
 					mouseOverElement = poolLevel.getNextFree();
 				} else if (mouseOverElement == null
 						&& iMouseOverObjectID != iDraggedObjectId) {
 					mouseOverElement = RemoteElementManager.get().getItem(
 							iMouseOverObjectID);
 				}
 
 				if (mouseOverElement != null) {
 					RemoteLevelElement originElement = RemoteElementManager.get()
 							.getItem(iDraggedObjectId);
 
 					AGLView mouseOverView = mouseOverElement.getGLView();
 					AGLView originView = originElement.getGLView();
 
 					mouseOverElement.setGLView(originView);
 					originElement.setGLView(mouseOverView);
 
 					if (originView != null) {
 						originView.setRemoteLevelElement(mouseOverElement);
 					}
 
 					if (mouseOverView != null) {
 						mouseOverView.setRemoteLevelElement(originElement);
 					}
 
 					updateViewDetailLevels(originElement);
 					updateViewDetailLevels(mouseOverElement);
 
 					if (mouseOverElement.getGLView() != null) {
 						if (poolLevel.containsElement(originElement)
 								&& (stackLevel.containsElement(mouseOverElement) || focusLevel
 										.containsElement(mouseOverElement))) {
 							mouseOverElement.getGLView().broadcastElements(
 									EVAOperation.APPEND_UNIQUE);
 						}
 
 						if (poolLevel.containsElement(mouseOverElement)
 								&& (stackLevel.containsElement(originElement) || focusLevel
 										.containsElement(originElement))) {
 							mouseOverElement.getGLView().broadcastElements(
 									EVAOperation.REMOVE_ELEMENT);
 						}
 					}
 				}
 			}
 
 			generalManager.getViewGLCanvasManager()
 					.getConnectedElementRepresentationManager()
 					.clearTransformedConnections();
 			dragAndDrop.stopDragAction();
 			bUpdateOffScreenTextures = true;
 		}
 
 		// gl.glCallList(iGLDisplayListIndexLocal);
 	}
 
 	@Override
 	public void displayRemote(final GL gl) {
 		display(gl);
 	}
 
 	@Override
 	public void display(final GL gl) {
 		time.update();
 		// processEvents();
 
 		checkForHits(gl);
 
 		// Update the pool transformations according to the current mouse over
 		// object
 		layoutRenderStyle.initPoolLevel(iMouseOverObjectID);
 		layoutRenderStyle.initFocusLevel();
 
 		// Just for layout testing during runtime
 		// layoutRenderStyle.initStackLevel();
 		// layoutRenderStyle.initMemoLevel();
 
 		if (GeneralManager.get().getTrackDataProvider().isTrackModeActive()
 				&& layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.BUCKET)) {
 
 			// TODO: very performance intensive - better solution needed (only
 			// in reshape)!
 			getParentGLCanvas().getParentComposite().getDisplay()
 					.asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							upperLeftScreenPos = getParentGLCanvas().getParentComposite()
 									.toDisplay(0, 0);
 						}
 					});
 
 			((BucketLayoutRenderStyle) layoutRenderStyle).initFocusLevelTrack(gl,
 					getParentGLCanvas().getBounds(), upperLeftScreenPos);
 
 			((BucketLayoutRenderStyle) layoutRenderStyle).initStackLevelTrack();
 		}
 
 		doSlerpActions(gl);
 		initNewView(gl);
 
 		if (!generalManager.getTrackDataProvider().isTrackModeActive()) {
 			renderRemoteLevel(gl, focusLevel);
 			renderRemoteLevel(gl, stackLevel);
 		} else {
 			if (bUpdateOffScreenTextures) {
 				updateOffScreenTextures(gl);
 			}
 
 			renderRemoteLevel(gl, focusLevel);
 			// renderRemoteLevel(gl, stackLevel);
 
 			glOffScreenRenderer.renderRubberBucket(gl, stackLevel,
 					(BucketLayoutRenderStyle) layoutRenderStyle, this);
 		}
 
 		if (!bucketMouseWheelListener.isZoomActionRunning()) {
 			renderPoolAndMemoLayerBackground(gl);
 			renderRemoteLevel(gl, externalSelectionLevel);
 			renderRemoteLevel(gl, spawnLevel);
 			renderRemoteLevel(gl, transitionLevel);
 			renderRemoteLevel(gl, poolLevel);
 		}
 
 		if (layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.BUCKET)) {
 			bucketMouseWheelListener.render();
 		}
 
 		renderHandles(gl);
 
 		// gl.glCallList(iGLDisplayList);
 
 		// comment here for connection lines
 		// transform-selections here
 		if (glConnectionLineRenderer != null && connectionLinesEnabled) {
 			glConnectionLineRenderer.setActiveViewID(iActiveViewID); // FIXME:
 			// added
 			glConnectionLineRenderer.render(gl);
 		}
 
 		float fZTranslation = 0;
 		if (!bucketMouseWheelListener.isZoomedIn())
 			fZTranslation = 4f;
 
 		gl.glTranslatef(0, 0, fZTranslation);
 		contextMenu.render(gl, this);
 		gl.glTranslatef(0, 0, -fZTranslation);
 	}
 
 	public void renderBucketWall(final GL gl, boolean bRenderBorder,
 			RemoteLevelElement element) {
 		// Highlight potential view drop destination
 		if (dragAndDrop.isDragActionRunning() && element.getID() == iMouseOverObjectID) {
 			gl.glLineWidth(5);
 			gl.glColor4f(0.2f, 0.2f, 0.2f, 1);
 			gl.glBegin(GL.GL_LINE_LOOP);
 			gl.glVertex3f(0, 0, 0.01f);
 			gl.glVertex3f(0, 8, 0.01f);
 			gl.glVertex3f(8, 8, 0.01f);
 			gl.glVertex3f(8, 0, 0.01f);
 			gl.glEnd();
 		}
 
 		if (arSlerpActions.isEmpty()) {
 			gl.glColor4f(1f, 1f, 1f, 1.0f); // normal mode
 		} else {
 			gl.glColor4f(1f, 1f, 1f, 0.3f);
 		}
 
 		if (!newViews.isEmpty()) {
 			gl.glColor4f(1f, 1f, 1f, 0.3f);
 		}
 
 		gl.glBegin(GL.GL_POLYGON);
 		gl.glVertex3f(0, 0, -0.03f);
 		gl.glVertex3f(0, 8, -0.03f);
 		gl.glVertex3f(8, 8, -0.03f);
 		gl.glVertex3f(8, 0, -0.03f);
 		gl.glEnd();
 
 		if (!bRenderBorder)
 			return;
 
 		gl.glColor4f(0.4f, 0.4f, 0.4f, 1f);
 		gl.glLineWidth(1f);
 	}
 
 	private void renderRemoteLevel(final GL gl, final RemoteLevel level) {
 		for (RemoteLevelElement element : level.getAllElements()) {
 			renderRemoteLevelElement(gl, element, level);
 
 			if (!(layoutRenderStyle instanceof ListLayoutRenderStyle)) {
 				renderEmptyBucketWall(gl, element, level);
 			}
 		}
 	}
 
 	private void renderRemoteLevelElement(final GL gl, RemoteLevelElement element,
 			RemoteLevel level) {
 		// // Check if view is visible
 		// if (!level.getElementVisibilityById(iViewID))
 		// return;
 
 		AGLView glView = element.getGLView();
 
 		if (glView == null) {
 			return;
 		}
 
 		gl.glPushName(pickingManager.getPickingID(iUniqueID,
 				EPickingType.REMOTE_LEVEL_ELEMENT, element.getID()));
 		gl.glPushName(pickingManager.getPickingID(iUniqueID,
 				EPickingType.REMOTE_VIEW_SELECTION, glView.getID()));
 
 		gl.glPushMatrix();
 
 		Transform transform = element.getTransform();
 		Vec3f translation = transform.getTranslation();
 		Rotf rot = transform.getRotation();
 		Vec3f scale = transform.getScale();
 		Vec3f axis = new Vec3f();
 		float fAngle = rot.get(axis);
 
 		gl.glTranslatef(translation.x(), translation.y(), translation.z());
 		gl.glRotatef(Vec3f.convertRadiant2Grad(fAngle), axis.x(), axis.y(), axis.z());
 		gl.glScalef(scale.x(), scale.y(), scale.z());
 
 		if (level == poolLevel) {
 			String sRenderText = glView.getShortInfo();
 
 			// Limit pathway name in length
 			int iMaxChars;
 			if (layoutRenderStyle instanceof ListLayoutRenderStyle) {
 				iMaxChars = 80;
 			} else {
 				iMaxChars = 20;
 			}
 
 			if (sRenderText.length() > iMaxChars && scale.x() < 0.03f) {
 				sRenderText = sRenderText.subSequence(0, iMaxChars - 3) + "...";
 			}
 
 			float fTextScalingFactor = 0.09f;
 			float fTextXPosition = 0f;
 
 			if (element.getID() == iMouseOverObjectID) {
 				renderPoolSelection(
 						gl,
 						translation.x() - 0.4f / fAspectRatio,
 						translation.y() * scale.y() + 5.2f,
 
 						(float) textRenderer.getBounds(sRenderText).getWidth() * 0.06f + 23,
 						6f, element);
 				gl.glTranslatef(0.8f, 1.3f, 0);
 
 				fTextScalingFactor = 0.075f;
 				fTextXPosition = 12f;
 			} else {
 				// Render view background frame
 				Texture tempTexture = textureManager.getIconTexture(gl,
 						EIconTextures.POOL_VIEW_BACKGROUND);
 				tempTexture.enable();
 				tempTexture.bind();
 
 				float fFrameWidth = 9.5f;
 				TextureCoords texCoords = tempTexture.getImageTexCoords();
 
 				gl.glColor4f(1, 1, 1, 0.75f);
 
 				gl.glBegin(GL.GL_POLYGON);
 				gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
 				gl.glVertex3f(-0.7f, -0.6f + fFrameWidth, -0.01f);
 				gl.glTexCoord2f(texCoords.left(), texCoords.top());
 				gl.glVertex3f(-0.7f + fFrameWidth, -0.6f + fFrameWidth, -0.01f);
 				gl.glTexCoord2f(texCoords.right(), texCoords.top());
 				gl.glVertex3f(-0.7f + fFrameWidth, -0.6f, -0.01f);
 				gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
 				gl.glVertex3f(-0.7f, -0.6f, -0.01f);
 				gl.glEnd();
 
 				tempTexture.disable();
 
 				fTextXPosition = 9.5f;
 			}
 
 			int iNumberOfGenesSelected = glView
 					.getNumberOfSelections(SelectionType.SELECTION);
 			int iNumberOfGenesMouseOver = glView
 					.getNumberOfSelections(SelectionType.MOUSE_OVER);
 
 			textRenderer.begin3DRendering();
 
 			if (element.getID() == iMouseOverObjectID) {
 				textRenderer.setColor(1, 1, 1, 1);
 			} else {
 				textRenderer.setColor(0, 0, 0, 1);
 			}
 
 			if (iNumberOfGenesMouseOver == 0 && iNumberOfGenesSelected == 0) {
 				textRenderer.draw3D(sRenderText, fTextXPosition, 3f, 0,
 						fTextScalingFactor);
 			} else {
 				textRenderer.draw3D(sRenderText, fTextXPosition, 4.5f, 0,
 						fTextScalingFactor);
 			}
 
 			textRenderer.end3DRendering();
 
 			gl.glLineWidth(4);
 
 			if (element.getID() == iMouseOverObjectID) {
 				gl.glTranslatef(2.2f, 0.5f, 0);
 			}
 
 			if (iNumberOfGenesMouseOver > 0) {
 				if (element.getID() == iMouseOverObjectID) {
 					gl.glTranslatef(-2.5f, 0, 0);
 				}
 
 				textRenderer.begin3DRendering();
 				textRenderer.draw3D(Integer.toString(iNumberOfGenesMouseOver),
 						fTextXPosition + 9, 2.4f, 0, fTextScalingFactor);
 				textRenderer.end3DRendering();
 
 				if (element.getID() == iMouseOverObjectID) {
 					gl.glTranslatef(2.5f, 0, 0);
 				}
 
 				gl.glColor4fv(SelectionType.MOUSE_OVER.getColor(), 0);
 				gl.glBegin(GL.GL_LINES);
 				gl.glVertex3f(10, 2.7f, 0f);
 				gl.glVertex3f(18, 2.7f, 0f);
 				gl.glVertex3f(20, 2.7f, 0f);
 				gl.glVertex3f(29, 2.7f, 0f);
 				gl.glEnd();
 			}
 
 			if (iNumberOfGenesSelected > 0) {
 				if (iNumberOfGenesMouseOver > 0) {
 					gl.glTranslatef(0, -1.8f, 0);
 				}
 
 				if (element.getID() == iMouseOverObjectID) {
 					gl.glTranslatef(-2.5f, 0, 0);
 				}
 
 				textRenderer.begin3DRendering();
 				textRenderer.draw3D(Integer.toString(iNumberOfGenesSelected),
 						fTextXPosition + 9, 2.5f, 0, fTextScalingFactor);
 				textRenderer.end3DRendering();
 
 				if (element.getID() == iMouseOverObjectID) {
 					gl.glTranslatef(2.5f, 0, 0);
 				}
 
 				gl.glColor4fv(SelectionType.SELECTION.getColor(), 0);
 				gl.glBegin(GL.GL_LINES);
 				gl.glVertex3f(10, 2.9f, 0f);
 				gl.glVertex3f(18, 2.9f, 0f);
 				gl.glVertex3f(20, 2.9f, 0f);
 				gl.glVertex3f(29, 2.9f, 0f);
 				gl.glEnd();
 
 				if (iNumberOfGenesMouseOver > 0) {
 					gl.glTranslatef(0, 1.8f, 0);
 				}
 			}
 
 			if (element.getID() == iMouseOverObjectID) {
 				gl.glTranslatef(-2.2f, -0.5f, 0);
 			}
 		}
 
 		// Prevent rendering of view textures when simple list view
 		// if ((layoutRenderStyle instanceof ListLayoutRenderStyle
 		// && (layer == poolLayer || layer == stackLayer)))
 		// {
 		// gl.glPopMatrix();
 		// return;
 		// }
 
 		if (level != externalSelectionLevel && level != poolLevel) {
 			if (level.equals(focusLevel)) {
 				renderBucketWall(gl, false, element);
 			} else {
 				renderBucketWall(gl, true, element);
 			}
 		}
 
 		if (!bEnableNavigationOverlay || !level.equals(stackLevel)) {
 			glView.displayRemote(gl);
 		} else {
 			renderNavigationOverlay(gl, element.getID());
 		}
 
 		gl.glPopMatrix();
 
 		gl.glPopName();
 		gl.glPopName();
 	}
 
 	private void renderEmptyBucketWall(final GL gl, RemoteLevelElement element,
 			RemoteLevel level) {
 		gl.glPushMatrix();
 
 		gl.glPushName(pickingManager.getPickingID(iUniqueID,
 				EPickingType.REMOTE_LEVEL_ELEMENT, element.getID()));
 
 		Transform transform = element.getTransform();
 		Vec3f translation = transform.getTranslation();
 		Rotf rot = transform.getRotation();
 		Vec3f scale = transform.getScale();
 		Vec3f axis = new Vec3f();
 		float fAngle = rot.get(axis);
 
 		gl.glTranslatef(translation.x(), translation.y(), translation.z());
 		gl.glScalef(scale.x(), scale.y(), scale.z());
 		gl.glRotatef(Vec3f.convertRadiant2Grad(fAngle), axis.x(), axis.y(), axis.z());
 
 		if (!level.equals(transitionLevel) && !level.equals(spawnLevel)
 				&& !level.equals(poolLevel) && !level.equals(externalSelectionLevel)) {
 			renderBucketWall(gl, true, element);
 		}
 
 		gl.glPopName();
 
 		gl.glPopMatrix();
 	}
 
 	private void renderHandles(final GL gl) {
 
 		// Bucket stack top
 		RemoteLevelElement element = stackLevel.getElementByPositionIndex(0);
 		if (element.getGLView() != null) {
 
 			if (!bucketMouseWheelListener.isZoomedIn()) {
 				gl.glTranslatef(-2, 0, 4.02f);
 				renderNavigationHandleBar(gl, element, 4, 0.075f, false, 2);
 				gl.glTranslatef(2, 0, -4.02f);
 			} else {
 				renderStackViewHandleBarZoomedIn(gl, element);
 			}
 		}
 
 		// Bucket stack bottom
 		element = stackLevel.getElementByPositionIndex(2);
 		if (element.getGLView() != null) {
 			if (!bucketMouseWheelListener.isZoomedIn()) {
 				gl.glTranslatef(-2, 0, 4.02f);
 				gl.glRotatef(180, 1, 0, 0);
 				renderNavigationHandleBar(gl, element, 4, 0.075f, true, 2);
 				gl.glRotatef(-180, 1, 0, 0);
 				gl.glTranslatef(2, 0, -4.02f);
 			} else {
 				renderStackViewHandleBarZoomedIn(gl, element);
 			}
 		}
 
 		// Bucket stack left
 		element = stackLevel.getElementByPositionIndex(1);
 		if (element.getGLView() != null) {
 			if (!bucketMouseWheelListener.isZoomedIn()) {
 				gl.glTranslatef(-2f / fAspectRatio + 2 + 0.8f, -2, 4.02f);
 				gl.glRotatef(90, 0, 0, 1);
 				renderNavigationHandleBar(gl, element, 4, 0.075f, false, 2);
 				gl.glRotatef(-90, 0, 0, 1);
 				gl.glTranslatef(2f / fAspectRatio - 2 - 0.8f, 2, -4.02f);
 			} else {
 				renderStackViewHandleBarZoomedIn(gl, element);
 			}
 		}
 
 		// Bucket stack right
 		element = stackLevel.getElementByPositionIndex(3);
 		if (element.getGLView() != null) {
 			if (!bucketMouseWheelListener.isZoomedIn()) {
 				gl.glTranslatef(2f / fAspectRatio - 0.8f - 2, 2, 4.02f);
 				gl.glRotatef(-90, 0, 0, 1);
 				renderNavigationHandleBar(gl, element, 4, 0.075f, false, 2);
 				gl.glRotatef(90, 0, 0, 1);
 				gl.glTranslatef(-2f / fAspectRatio + 0.8f + 2, -2, -4.02f);
 			} else {
 				renderStackViewHandleBarZoomedIn(gl, element);
 			}
 		}
 
 		// Bucket center (focus)
 		element = focusLevel.getElementByPositionIndex(0);
 		if (element.getGLView() != null) {
 
 			Transform transform;
 			Vec3f translation;
 			Vec3f scale;
 
 			float fYCorrection = 0f;
 			if (!bucketMouseWheelListener.isZoomedIn()) {
 				fYCorrection = 0f;
 			} else {
 				fYCorrection = 0.145f;
 			}
 
 			transform = element.getTransform();
 			translation = transform.getTranslation();
 			scale = transform.getScale();
 
 			gl.glTranslatef(translation.x(), translation.y() - 2 * 0.075f + fYCorrection,
 					translation.z() + 0.001f);
 
 			gl.glScalef(scale.x() * 4, scale.y() * 4, scale.z());
 			renderNavigationHandleBar(gl, element, 2, 0.075f, false, 2);
 			gl.glScalef(1 / (scale.x() * 4), 1 / (scale.y() * 4), 1 / scale.z());
 
 			gl.glTranslatef(-translation.x(), -translation.y() + 2 * 0.075f
 					- fYCorrection, -translation.z() - 0.001f);
 		}
 	}
 
 	private void renderStackViewHandleBarZoomedIn(final GL gl, RemoteLevelElement element) {
 		Transform transform = element.getTransform();
 		Vec3f translation = transform.getTranslation();
 		Vec3f scale = transform.getScale();
 		// float fZoomedInScalingFactor = 0.1f;
 		float fYCorrection = 0f;
 		if (!bucketMouseWheelListener.isZoomedIn()) {
 			fYCorrection = 0f;
 		} else {
 			fYCorrection = 0.145f;
 		}
 
 		gl.glTranslatef(translation.x(), translation.y() - 2 * 0.075f + fYCorrection,
 				translation.z() + 0.001f);
 		gl.glScalef(scale.x() * 4, scale.y() * 4, scale.z());
 		renderNavigationHandleBar(gl, element, 2, 0.075f, false, 2);
 		gl.glScalef(1 / (scale.x() * 4), 1 / (scale.y() * 4), 1 / scale.z());
 		gl.glTranslatef(-translation.x(), -translation.y() + 2 * 0.075f - fYCorrection,
 				-translation.z() - 0.001f);
 	}
 
 	private void renderNavigationHandleBar(final GL gl, RemoteLevelElement element,
 			float fHandleWidth, float fHandleHeight, boolean bUpsideDown,
 			float fScalingFactor) {
 
 		// Render icons
 		gl.glTranslatef(0, 2 + fHandleHeight, 0);
 		renderSingleHandle(gl, element.getID(), EPickingType.REMOTE_VIEW_DRAG,
 				EIconTextures.NAVIGATION_DRAG_VIEW, fHandleHeight, fHandleHeight);
 		gl.glTranslatef(fHandleWidth - 2 * fHandleHeight, 0, 0);
 		if (bUpsideDown) {
 			gl.glRotatef(180, 1, 0, 0);
 			gl.glTranslatef(0, fHandleHeight, 0);
 		}
 		renderSingleHandle(gl, element.getID(), EPickingType.REMOTE_VIEW_LOCK,
 				EIconTextures.NAVIGATION_LOCK_VIEW, fHandleHeight, fHandleHeight);
 		if (bUpsideDown) {
 			gl.glTranslatef(0, -fHandleHeight, 0);
 			gl.glRotatef(-180, 1, 0, 0);
 		}
 		gl.glTranslatef(fHandleHeight, 0, 0);
 		renderSingleHandle(gl, element.getID(), EPickingType.REMOTE_VIEW_REMOVE,
 				EIconTextures.NAVIGATION_REMOVE_VIEW, fHandleHeight, fHandleHeight);
 		gl.glTranslatef(-fHandleWidth + fHandleHeight, -2 - fHandleHeight, 0);
 
 		// Render background (also draggable)
 		gl.glPushName(pickingManager.getPickingID(iUniqueID,
 				EPickingType.REMOTE_VIEW_DRAG, element.getID()));
 		gl.glColor3f(0.25f, 0.25f, 0.25f);
 		gl.glBegin(GL.GL_POLYGON);
 		gl.glVertex3f(0 + fHandleHeight, 2 + fHandleHeight, 0);
 		gl.glVertex3f(fHandleWidth - 2 * fHandleHeight, 2 + fHandleHeight, 0);
 		gl.glVertex3f(fHandleWidth - 2 * fHandleHeight, 2, 0);
 		gl.glVertex3f(0 + fHandleHeight, 2, 0);
 		gl.glEnd();
 
 		gl.glPopName();
 
 		// Render view information
 		String sText = element.getGLView().getShortInfo();
 
 		int iMaxChars = 50;
 		if (sText.length() > iMaxChars) {
 			sText = sText.subSequence(0, iMaxChars - 3) + "...";
 		}
 
 		float fTextScalingFactor = 0.0027f;
 
 		if (bUpsideDown) {
 			gl.glRotatef(180, 1, 0, 0);
 			gl.glTranslatef(0, -4 - fHandleHeight, 0);
 		}
 
 		textRenderer.setColor(0.7f, 0.7f, 0.7f, 1);
 		textRenderer.begin3DRendering();
 		textRenderer.draw3D(sText, fHandleWidth / fScalingFactor
 				- (float) textRenderer.getBounds(sText).getWidth() / 2f
 				* fTextScalingFactor, 2.02f, 0f, fTextScalingFactor);
 		textRenderer.end3DRendering();
 
 		if (bUpsideDown) {
 			gl.glTranslatef(0, 4 + fHandleHeight, 0);
 			gl.glRotatef(-180, 1, 0, 0);
 		}
 	}
 
 	private void renderSingleHandle(final GL gl, int iRemoteLevelElementID,
 			EPickingType ePickingType, EIconTextures eIconTexture, float fWidth,
 			float fHeight) {
 		gl.glPushName(pickingManager.getPickingID(iUniqueID, ePickingType,
 				iRemoteLevelElementID));
 
 		Texture tempTexture = textureManager.getIconTexture(gl, eIconTexture);
 		tempTexture.enable();
 		tempTexture.bind();
 
 		TextureCoords texCoords = tempTexture.getImageTexCoords();
 		gl.glColor3f(1, 1, 1);
 		gl.glBegin(GL.GL_POLYGON);
 		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
 		gl.glVertex3f(0, -fHeight, 0f);
 		gl.glTexCoord2f(texCoords.left(), texCoords.top());
 		gl.glVertex3f(0, 0, 0f);
 		gl.glTexCoord2f(texCoords.right(), texCoords.top());
 		gl.glVertex3f(fWidth, 0, 0f);
 		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
 		gl.glVertex3f(fWidth, -fHeight, 0f);
 		gl.glEnd();
 
 		tempTexture.disable();
 
 		gl.glPopName();
 	}
 
 	private void renderNavigationOverlay(final GL gl, final int iRemoteLevelElementID) {
 		if (glConnectionLineRenderer != null) {
 			glConnectionLineRenderer.enableRendering(false);
 		}
 
 		RemoteLevelElement remoteLevelElement = RemoteElementManager.get().getItem(
 				iRemoteLevelElementID);
 
 		EPickingType leftWallPickingType = null;
 		EPickingType rightWallPickingType = null;
 		EPickingType topWallPickingType = null;
 		EPickingType bottomWallPickingType = null;
 
 		Vec4f tmpColor_out = new Vec4f(0.9f, 0.9f, 0.9f,
 				ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 		Vec4f tmpColor_in = new Vec4f(0.9f, 0.9f, 0.9f,
 				ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 		Vec4f tmpColor_left = new Vec4f(0.9f, 0.9f, 0.9f,
 				ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 		Vec4f tmpColor_right = new Vec4f(0.9f, 0.9f, 0.9f,
 				ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 		Vec4f tmpColor_lock = new Vec4f(0.9f, 0.9f, 0.9f,
 				ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 
 		// Assign view symbol
 		Texture textureViewSymbol;
 		AGLView view = remoteLevelElement.getGLView();
 		if (view.getViewType().equals("org.caleydo.view.heatmap")) {
 			textureViewSymbol = textureManager.getIconTexture(gl,
 					EIconTextures.HEAT_MAP_SYMBOL);
 		} else if (view.getViewType().equals("org.caleydo.view.parcoords")) {
 			textureViewSymbol = textureManager.getIconTexture(gl,
 					EIconTextures.PAR_COORDS_SYMBOL);
 		} else if (view.getViewType().equals("org.caleydo.view.pathway")) {
 			textureViewSymbol = textureManager.getIconTexture(gl,
 					EIconTextures.PATHWAY_SYMBOL);
 		} 
 //		else if (view.getViewType().equals("org.caleydo.view.glyph")) {
 //			textureViewSymbol = textureManager.getIconTexture(gl,
 //					EIconTextures.GLYPH_SYMBOL);
 //		}
 		// else if (view instanceof GLCell) {
 		// textureViewSymbol = textureManager.getIconTexture(gl,
 		// EIconTextures.GLYPH_SYMBOL);
 		// }
 		else
 			throw new IllegalStateException("Unknown view that has no symbol assigned.");
 
 		Texture textureMoveLeft = null;
 		Texture textureMoveRight = null;
 		Texture textureMoveOut = null;
 		Texture textureMoveIn = null;
 
 		TextureCoords texCoords = textureViewSymbol.getImageTexCoords();
 
 		if (iNavigationMouseOverViewID_lock == iRemoteLevelElementID) {
 			tmpColor_lock.set(1, 0.3f, 0.3f,
 					ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 		}
 
 		if (layoutMode.equals(LayoutMode.JUKEBOX)) {
 			topWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
 			bottomWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
 			leftWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
 			rightWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
 
 			if (iNavigationMouseOverViewID_out == iRemoteLevelElementID) {
 				tmpColor_left.set(1, 0.3f, 0.3f,
 						ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 			} else if (iNavigationMouseOverViewID_in == iRemoteLevelElementID) {
 				tmpColor_right.set(1, 0.3f, 0.3f,
 						ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 			} else if (iNavigationMouseOverViewID_left == iRemoteLevelElementID) {
 				tmpColor_in.set(1, 0.3f, 0.3f,
 						ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 			} else if (iNavigationMouseOverViewID_right == iRemoteLevelElementID) {
 				tmpColor_out.set(1, 0.3f, 0.3f,
 						ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 			}
 
 			textureMoveIn = textureManager.getIconTexture(gl, EIconTextures.ARROW_LEFT);
 			textureMoveOut = textureManager.getIconTexture(gl, EIconTextures.ARROW_DOWN);
 			textureMoveLeft = textureManager.getIconTexture(gl, EIconTextures.ARROW_DOWN);
 			textureMoveRight = textureManager
 					.getIconTexture(gl, EIconTextures.ARROW_LEFT);
 		} else {
 			if (stackLevel.getPositionIndexByElementID(remoteLevelElement) == 0) // top
 			{
 				topWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
 				bottomWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
 				leftWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
 				rightWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
 
 				if (iNavigationMouseOverViewID_out == iRemoteLevelElementID) {
 					tmpColor_out.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				} else if (iNavigationMouseOverViewID_in == iRemoteLevelElementID) {
 					tmpColor_in.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				} else if (iNavigationMouseOverViewID_left == iRemoteLevelElementID) {
 					tmpColor_left.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				} else if (iNavigationMouseOverViewID_right == iRemoteLevelElementID) {
 					tmpColor_right.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				}
 
 				textureMoveIn = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_LEFT);
 				textureMoveOut = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_DOWN);
 				textureMoveLeft = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_DOWN);
 				textureMoveRight = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_LEFT);
 			} else if (stackLevel.getPositionIndexByElementID(remoteLevelElement) == 2) // bottom
 			{
 				topWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
 				bottomWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
 				leftWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
 				rightWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
 
 				if (iNavigationMouseOverViewID_out == iRemoteLevelElementID) {
 					tmpColor_in.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				} else if (iNavigationMouseOverViewID_in == iRemoteLevelElementID) {
 					tmpColor_out.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				} else if (iNavigationMouseOverViewID_left == iRemoteLevelElementID) {
 					tmpColor_right.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				} else if (iNavigationMouseOverViewID_right == iRemoteLevelElementID) {
 					tmpColor_left.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				}
 
 				textureMoveIn = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_LEFT);
 				textureMoveOut = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_DOWN);
 				textureMoveLeft = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_DOWN);
 				textureMoveRight = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_LEFT);
 			} else if (stackLevel.getPositionIndexByElementID(remoteLevelElement) == 1) // left
 			{
 				topWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
 				bottomWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
 				leftWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
 				rightWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
 
 				if (iNavigationMouseOverViewID_out == iRemoteLevelElementID) {
 					tmpColor_left.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				} else if (iNavigationMouseOverViewID_in == iRemoteLevelElementID) {
 					tmpColor_right.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				} else if (iNavigationMouseOverViewID_left == iRemoteLevelElementID) {
 					tmpColor_in.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				} else if (iNavigationMouseOverViewID_right == iRemoteLevelElementID) {
 					tmpColor_out.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				}
 
 				textureMoveIn = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_LEFT);
 				textureMoveOut = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_DOWN);
 				textureMoveLeft = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_DOWN);
 				textureMoveRight = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_LEFT);
 			} else if (stackLevel.getPositionIndexByElementID(remoteLevelElement) == 3) // right
 			{
 				topWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
 				bottomWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
 				leftWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
 				rightWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
 
 				if (iNavigationMouseOverViewID_out == iRemoteLevelElementID) {
 					tmpColor_right.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				} else if (iNavigationMouseOverViewID_in == iRemoteLevelElementID) {
 					tmpColor_left.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				} else if (iNavigationMouseOverViewID_left == iRemoteLevelElementID) {
 					tmpColor_out.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				} else if (iNavigationMouseOverViewID_right == iRemoteLevelElementID) {
 					tmpColor_in.set(1, 0.3f, 0.3f,
 							ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 				}
 
 				textureMoveIn = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_LEFT);
 				textureMoveOut = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_DOWN);
 				textureMoveLeft = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_DOWN);
 				textureMoveRight = textureManager.getIconTexture(gl,
 						EIconTextures.ARROW_LEFT);
 			}
 			// else if
 			// (underInteractionLayer.getPositionIndexByElementID(iViewID) == 0)
 			// // center
 			// {
 			// topWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
 			// bottomWallPickingType =
 			// EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
 			// leftWallPickingType =
 			// EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
 			// rightWallPickingType =
 			// EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
 			//
 			// if (iNavigationMouseOverViewID_out == iViewID)
 			// tmpColor_out.set(1, 0.3f, 0.3f, 0.9f);
 			// else if (iNavigationMouseOverViewID_in == iViewID)
 			// tmpColor_in.set(1, 0.3f, 0.3f, 0.9f);
 			// else if (iNavigationMouseOverViewID_left == iViewID)
 			// tmpColor_left.set(1, 0.3f, 0.3f, 0.9f);
 			// else if (iNavigationMouseOverViewID_right == iViewID)
 			// tmpColor_right.set(1, 0.3f, 0.3f, 0.9f);
 			//
 			// textureMoveIn =
 			// iconTextureManager.getIconTexture(EIconTextures.ARROW_LEFT);
 			// textureMoveOut =
 			// iconTextureManager.getIconTexture(EIconTextures.ARROW_DOWN);
 			// textureMoveLeft = iconTextureManager
 			// .getIconTexture(EIconTextures.ARROW_DOWN);
 			// textureMoveRight = iconTextureManager
 			// .getIconTexture(EIconTextures.ARROW_LEFT);
 			// }
 		}
 		// else if (underInteractionLayer.containsElement(iViewID))
 		// {
 		// topWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
 		// bottomWallPickingType =
 		// EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
 		// leftWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
 		// rightWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
 		// }
 
 		gl.glLineWidth(1);
 
 		float fNavigationZValue = 0f;
 
 		// CENTER - NAVIGATION: VIEW IDENTIFICATION ICON
 		// gl.glPushName(pickingManager.getPickingID(iUniqueID,
 		// EPickingType.BUCKET_LOCK_ICON_SELECTION, iViewID));
 
 		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
 		gl.glBegin(GL.GL_LINE_LOOP);
 		gl.glVertex3f(2.66f, 2.66f, fNavigationZValue);
 		gl.glVertex3f(2.66f, 5.33f, fNavigationZValue);
 		gl.glVertex3f(5.33f, 5.33f, fNavigationZValue);
 		gl.glVertex3f(5.33f, 2.66f, fNavigationZValue);
 		gl.glEnd();
 
 		gl.glColor4f(tmpColor_lock.x(), tmpColor_lock.y(), tmpColor_lock.z(),
 				ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 
 		textureViewSymbol.enable();
 		textureViewSymbol.bind();
 
 		gl.glBegin(GL.GL_POLYGON);
 		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
 		gl.glVertex3f(2.66f, 2.66f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.left(), texCoords.top());
 		gl.glVertex3f(2.66f, 5.33f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.right(), texCoords.top());
 		gl.glVertex3f(5.33f, 5.33f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
 		gl.glVertex3f(5.33f, 2.66f, fNavigationZValue);
 		gl.glEnd();
 
 		textureViewSymbol.disable();
 
 		// gl.glPopName();
 
 		// BOTTOM - NAVIGATION: MOVE IN
 		gl.glPushName(pickingManager.getPickingID(iUniqueID, bottomWallPickingType,
 				iRemoteLevelElementID));
 
 		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
 		gl.glBegin(GL.GL_LINE_LOOP);
 		gl.glVertex3f(0, 0, fNavigationZValue);
 		gl.glVertex3f(2.66f, 2.66f, fNavigationZValue);
 		gl.glVertex3f(5.33f, 2.66f, fNavigationZValue);
 		gl.glVertex3f(8, 0, fNavigationZValue);
 		gl.glEnd();
 
 		gl.glColor4f(tmpColor_in.x(), tmpColor_in.y(), tmpColor_in.z(),
 				ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 
 		textureMoveIn.enable();
 		textureMoveIn.bind();
 		// texCoords = textureMoveIn.getImageTexCoords();
 		// gl.glColor4f(1,0.3f,0.3f,0.9f);
 		gl.glBegin(GL.GL_POLYGON);
 		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
 		gl.glVertex3f(2.66f, 0.05f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
 		gl.glVertex3f(2.66f, 2.66f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.right(), texCoords.top());
 		gl.glVertex3f(5.33f, 2.66f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.left(), texCoords.top());
 		gl.glVertex3f(5.33f, 0.05f, fNavigationZValue);
 		gl.glEnd();
 
 		textureMoveIn.disable();
 
 		gl.glPopName();
 
 		// RIGHT - NAVIGATION: MOVE RIGHT
 		gl.glPushName(pickingManager.getPickingID(iUniqueID, rightWallPickingType,
 				iRemoteLevelElementID));
 
 		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
 		gl.glBegin(GL.GL_LINE_LOOP);
 		gl.glVertex3f(8, 0, fNavigationZValue);
 		gl.glVertex3f(5.33f, 2.66f, fNavigationZValue);
 		gl.glVertex3f(5.33f, 5.33f, fNavigationZValue);
 		gl.glVertex3f(8, 8, fNavigationZValue);
 		gl.glEnd();
 
 		gl.glColor4f(tmpColor_right.x(), tmpColor_right.y(), tmpColor_right.z(),
 				ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 
 		textureMoveRight.enable();
 		textureMoveRight.bind();
 
 		// gl.glColor4f(0,1,0,1);
 		gl.glBegin(GL.GL_POLYGON);
 		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
 		gl.glVertex3f(7.95f, 2.66f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
 		gl.glVertex3f(5.33f, 2.66f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.right(), texCoords.top());
 		gl.glVertex3f(5.33f, 5.33f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.left(), texCoords.top());
 		gl.glVertex3f(7.95f, 5.33f, fNavigationZValue);
 		gl.glEnd();
 
 		textureMoveRight.disable();
 
 		gl.glPopName();
 
 		// LEFT - NAVIGATION: MOVE LEFT
 		gl.glPushName(pickingManager.getPickingID(iUniqueID, leftWallPickingType,
 				iRemoteLevelElementID));
 
 		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
 		gl.glBegin(GL.GL_LINE_LOOP);
 		gl.glVertex3f(0, 0, fNavigationZValue);
 		gl.glVertex3f(0, 8, fNavigationZValue);
 		gl.glVertex3f(2.66f, 5.33f, fNavigationZValue);
 		gl.glVertex3f(2.66f, 2.66f, fNavigationZValue);
 		gl.glEnd();
 
 		gl.glColor4f(tmpColor_left.x(), tmpColor_left.y(), tmpColor_left.z(),
 				ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 
 		textureMoveLeft.enable();
 		textureMoveLeft.bind();
 
 		// gl.glColor4f(0,1,0,1);
 		gl.glBegin(GL.GL_POLYGON);
 		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
 		gl.glVertex3f(0.05f, 2.66f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
 		gl.glVertex3f(0.05f, 5.33f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.right(), texCoords.top());
 		gl.glVertex3f(2.66f, 5.33f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.left(), texCoords.top());
 		gl.glVertex3f(2.66f, 2.66f, fNavigationZValue);
 		gl.glEnd();
 
 		textureMoveLeft.disable();
 
 		gl.glPopName();
 
 		// TOP - NAVIGATION: MOVE OUT
 		gl.glPushName(pickingManager.getPickingID(iUniqueID, topWallPickingType,
 				iRemoteLevelElementID));
 
 		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
 		gl.glBegin(GL.GL_LINE_LOOP);
 		gl.glVertex3f(0, 8, fNavigationZValue);
 		gl.glVertex3f(8, 8, fNavigationZValue);
 		gl.glVertex3f(5.33f, 5.33f, fNavigationZValue);
 		gl.glVertex3f(2.66f, 5.33f, fNavigationZValue);
 		gl.glEnd();
 
 		gl.glColor4f(tmpColor_out.x(), tmpColor_out.y(), tmpColor_out.z(),
 				ARemoteViewLayoutRenderStyle.NAVIGATION_OVERLAY_TRANSPARENCY);
 
 		textureMoveOut.enable();
 		textureMoveOut.bind();
 
 		// gl.glColor4f(0,1,0,1);
 		gl.glBegin(GL.GL_POLYGON);
 		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
 		gl.glVertex3f(2.66f, 7.95f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
 		gl.glVertex3f(5.33f, 7.95f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.right(), texCoords.top());
 		gl.glVertex3f(5.33f, 5.33f, fNavigationZValue);
 		gl.glTexCoord2f(texCoords.left(), texCoords.top());
 		gl.glVertex3f(2.66f, 5.33f, fNavigationZValue);
 		gl.glEnd();
 
 		textureMoveOut.disable();
 
 		gl.glPopName();
 	}
 
 	private void renderPoolSelection(final GL gl, float fXOrigin, float fYOrigin,
 			float fWidth, float fHeight, RemoteLevelElement element) {
 		float fPanelSideWidth = 11f;
 
 		gl.glColor3f(0.25f, 0.25f, 0.25f);
 		gl.glBegin(GL.GL_POLYGON);
 
 		gl.glVertex3f(fXOrigin + 1.65f / fAspectRatio + fPanelSideWidth, fYOrigin
 				- fHeight / 2f + fHeight, 0f);
 		gl.glVertex3f(fXOrigin + 1.65f / fAspectRatio + fPanelSideWidth + fWidth,
 				fYOrigin - fHeight / 2f + fHeight, 0f);
 		gl.glVertex3f(fXOrigin + 1.65f / fAspectRatio + fPanelSideWidth + fWidth,
 				fYOrigin - fHeight / 2f, 0f);
 		gl.glVertex3f(fXOrigin + 1.65f / fAspectRatio + fPanelSideWidth, fYOrigin
 				- fHeight / 2f, 0f);
 
 		gl.glEnd();
 
 		Texture tempTexture = textureManager.getIconTexture(gl,
 				EIconTextures.POOL_VIEW_BACKGROUND_SELECTION);
 		tempTexture.enable();
 		tempTexture.bind();
 
 		TextureCoords texCoords = tempTexture.getImageTexCoords();
 
 		gl.glColor4f(1, 1, 1, 0.75f);
 
 		gl.glBegin(GL.GL_POLYGON);
 		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
 		gl.glVertex3f(fXOrigin + 2 / fAspectRatio + fPanelSideWidth, fYOrigin - fHeight,
 				-0.01f);
 		gl.glTexCoord2f(texCoords.left(), texCoords.top());
 		gl.glVertex3f(fXOrigin + 2 / fAspectRatio + fPanelSideWidth, fYOrigin + fHeight,
 				-0.01f);
 		gl.glTexCoord2f(texCoords.right(), texCoords.top());
 		gl.glVertex3f(fXOrigin + 2f / fAspectRatio, fYOrigin + fHeight, -0.01f);
 		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
 		gl.glVertex3f(fXOrigin + 2f / fAspectRatio, fYOrigin - fHeight, -0.01f);
 		gl.glEnd();
 
 		tempTexture.disable();
 
 		gl.glPopName();
 		gl.glPopName();
 
 		int fHandleScaleFactor = 18;
 		gl.glTranslatef(fXOrigin + 2.5f / fAspectRatio, fYOrigin - fHeight / 2f + fHeight
 				- 1f, 1.8f);
 		gl.glScalef(fHandleScaleFactor, fHandleScaleFactor, fHandleScaleFactor);
 		renderSingleHandle(gl, element.getID(), EPickingType.REMOTE_VIEW_DRAG,
 				EIconTextures.POOL_DRAG_VIEW, 0.1f, 0.1f);
 		gl.glTranslatef(0, -0.2f, 0);
 		renderSingleHandle(gl, element.getID(), EPickingType.REMOTE_VIEW_REMOVE,
 				EIconTextures.POOL_REMOVE_VIEW, 0.1f, 0.1f);
 		gl.glTranslatef(0, 0.2f, 0);
 		gl.glScalef(1f / fHandleScaleFactor, 1f / fHandleScaleFactor,
 				1f / fHandleScaleFactor);
 		gl.glTranslatef(-fXOrigin - 2.5f / fAspectRatio, -fYOrigin + fHeight / 2f
 				- fHeight + 1f, -1.8f);
 
 		// gl.glColor3f(0.25f, 0.25f, 0.25f);
 		// gl.glBegin(GL.GL_POLYGON);
 		// gl.glVertex3f(fXOrigin + 3f, fYOrigin - fHeight / 2f + fHeight -
 		// 2.5f, 0f);
 		// gl.glVertex3f(fXOrigin + 5.1f, fYOrigin - fHeight / 2f + fHeight -
 		// 2.5f, 0f);
 		// gl.glVertex3f(fXOrigin + 5.1f, fYOrigin- fHeight / 2f + 1.5f, 0f);
 		// gl.glVertex3f(fXOrigin + 3f, fYOrigin- fHeight / 2f + 1.5f , 0f);
 		// gl.glEnd();
 
 		gl.glPushName(pickingManager.getPickingID(iUniqueID,
 				EPickingType.REMOTE_LEVEL_ELEMENT, element.getID()));
 		gl.glPushName(pickingManager.getPickingID(iUniqueID,
 				EPickingType.REMOTE_VIEW_SELECTION, element.getID()));
 	}
 
 	private void doSlerpActions(final GL gl) {
 		if (arSlerpActions.isEmpty())
 			return;
 
 		SlerpAction tmpSlerpAction = arSlerpActions.get(0);
 
 		if (iSlerpFactor == 0) {
 			tmpSlerpAction.start();
 
 			// System.out.println("Start slerp action " +tmpSlerpAction);
 		}
 
 		if (iSlerpFactor < SLERP_RANGE) {
 			// Makes animation rendering speed independent
 			iSlerpFactor += SLERP_SPEED * time.deltaT();
 
 			if (iSlerpFactor > SLERP_RANGE) {
 				iSlerpFactor = SLERP_RANGE;
 			}
 		}
 
 		slerpView(gl, tmpSlerpAction);
 	}
 
 	private void slerpView(final GL gl, SlerpAction slerpAction) {
 		int iViewID = slerpAction.getElementId();
 
 		SlerpMod slerpMod = new SlerpMod();
 
 		if (iSlerpFactor == 0) {
 			slerpMod.playSlerpSound();
 		}
 
 		Transform transform = slerpMod.interpolate(slerpAction
 				.getOriginRemoteLevelElement().getTransform(), slerpAction
 				.getDestinationRemoteLevelElement().getTransform(), (float) iSlerpFactor
 				/ SLERP_RANGE);
 
 		gl.glPushMatrix();
 
 		slerpMod.applySlerp(gl, transform, true, false);
 
 		generalManager.getViewGLCanvasManager().getGLView(iViewID).displayRemote(gl);
 
 		gl.glPopMatrix();
 
 		// Check if slerp action is finished
 		if (iSlerpFactor >= SLERP_RANGE) {
 			arSlerpActions.remove(slerpAction);
 
 			iSlerpFactor = 0;
 
 			slerpAction.finished();
 
 			RemoteLevelElement destinationElement = slerpAction
 					.getDestinationRemoteLevelElement();
 
 			updateViewDetailLevels(destinationElement);
 
 			bUpdateOffScreenTextures = true;
 		}
 
 		// After last slerp action is done the line connections are turned on
 		// again
 		if (arSlerpActions.isEmpty()) {
 			if (glConnectionLineRenderer != null) {
 				glConnectionLineRenderer.enableRendering(true);
 			}
 
 			generalManager.getViewGLCanvasManager().getInfoAreaManager()
 					.enable(!bEnableNavigationOverlay);
 			generalManager.getViewGLCanvasManager()
 					.getConnectedElementRepresentationManager()
 					.clearTransformedConnections();
 		}
 	}
 
 	private void updateViewDetailLevels(RemoteLevelElement element) {
 		RemoteLevel destinationLevel = element.getRemoteLevel();
 
 		AGLView glActiveSubView = element.getGLView();
 		if (glActiveSubView == null)
 			return;
 
 		glActiveSubView.setRemoteLevelElement(element);
 
 		// Update detail level of moved view when slerp action is finished;
 		if (destinationLevel == focusLevel) {
 			if (bucketMouseWheelListener.isZoomedIn()
 					|| layoutRenderStyle instanceof ListLayoutRenderStyle) {
 				glActiveSubView.setDetailLevel(EDetailLevel.HIGH);
 			} else {
 				glActiveSubView.setDetailLevel(EDetailLevel.MEDIUM);
 			}
 		} else if (destinationLevel == stackLevel) {
 			glActiveSubView.setDetailLevel(EDetailLevel.LOW);
 		} else if (destinationLevel == poolLevel
 				|| destinationLevel == externalSelectionLevel) {
 			glActiveSubView.setDetailLevel(EDetailLevel.VERY_LOW);
 		}
 
 		compactPoolLevel();
 	}
 
 	private void loadViewToFocusLevel(final int iRemoteLevelElementID) {
 		RemoteLevelElement element = RemoteElementManager.get().getItem(
 				iRemoteLevelElementID);
 
 		// Check if other slerp action is currently running
 		if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE)
 			return;
 
 		arSlerpActions.clear();
 
 		AGLView glView = element.getGLView();
 
 		if (glView == null)
 			return;
 
 		// Only broadcast elements if view is moved from pool to bucket
 		if (poolLevel.containsElement(element)) {
 			glView.broadcastElements(EVAOperation.APPEND_UNIQUE);
 		}
 
 		// if (layoutRenderStyle instanceof ListLayoutRenderStyle)
 		// {
 		// // Slerp selected view to under interaction transition position
 		// SlerpAction slerpActionTransition = new
 		// SlerpAction(iRemoteLevelElementID, poolLevel,
 		// transitionLevel);
 		// arSlerpActions.add(slerpActionTransition);
 		//
 		// // Check if focus has a free spot
 		// if (focusLevel.getElementByPositionIndex(0).getContainedElementID()
 		// != -1)
 		// {
 		// // Slerp under interaction view to free spot in pool
 		// SlerpAction reverseSlerpAction = new SlerpAction(focusLevel
 		// .getElementIDByPositionIndex(0), focusLevel, poolLevel);
 		// arSlerpActions.add(reverseSlerpAction);
 		// }
 		//
 		// // Slerp selected view from transition position to under interaction
 		// // position
 		// SlerpAction slerpAction = new SlerpAction(iViewID, transitionLevel,
 		// focusLevel);
 		// arSlerpActions.add(slerpAction);
 		// }
 		// else
 		{
 			// Check if view is already loaded in the stack layer
 			if (stackLevel.containsElement(element)) {
 
 				// Slerp selected view to transition position
 				SlerpAction slerpActionTransition = new SlerpAction(element,
 						transitionLevel.getElementByPositionIndex(0));
 				arSlerpActions.add(slerpActionTransition);
 
 				// Check if focus level is free
 				if (!focusLevel.hasFreePosition()) {
 					// Slerp focus view to free spot in stack
 					SlerpAction reverseSlerpAction = new SlerpAction(focusLevel
 							.getElementByPositionIndex(0).getGLView().getID(),
 							focusLevel.getElementByPositionIndex(0), element);
 					arSlerpActions.add(reverseSlerpAction);
 				}
 
 				// Slerp selected view from transition position to focus
 				// position
 				SlerpAction slerpAction = new SlerpAction(element.getGLView().getID(),
 						transitionLevel.getElementByPositionIndex(0),
 						focusLevel.getElementByPositionIndex(0));
 				arSlerpActions.add(slerpAction);
 			}
 			// Check if focus position is free
 			else if (focusLevel.hasFreePosition()) {
 
 				// Slerp selected view to focus position
 				SlerpAction slerpActionTransition = new SlerpAction(element,
 						focusLevel.getElementByPositionIndex(0));
 				arSlerpActions.add(slerpActionTransition);
 
 			} else {
 				// Slerp selected view to transition position
 				SlerpAction slerpActionTransition = new SlerpAction(element,
 						transitionLevel.getElementByPositionIndex(0));
 				arSlerpActions.add(slerpActionTransition);
 
 				RemoteLevelElement freeStackElement = null;
 				if (!stackLevel.hasFreePosition()) {
 					int iReplacePosition = 1;
 
 					// // Determine non locked stack position for view movement
 					// to pool
 					// for (int iTmpReplacePosition = 0; iTmpReplacePosition <
 					// stackLevel.getCapacity(); iTmpReplacePosition++)
 					// {
 					// if
 					// (stackLevel.getElementByPositionIndex(iTmpReplacePosition).isLocked())
 					// continue;
 					//
 					// iReplacePosition = iTmpReplacePosition + 1; // +1 to
 					// start with left view for outsourcing
 					//
 					// if (iReplacePosition == 4)
 					// iReplacePosition = 0;
 					//
 					// break;
 					// }
 					//
 					// if (iReplacePosition == -1)
 					// throw new
 					// IllegalStateException("All views in stack are locked!");
 
 					freeStackElement = stackLevel
 							.getElementByPositionIndex(iReplacePosition);
 
 					// Slerp view from stack to pool
 					SlerpAction reverseSlerpAction = new SlerpAction(freeStackElement,
 							poolLevel.getNextFree());
 					arSlerpActions.add(reverseSlerpAction);
 
 					// Unregister all elements of the view that is moved out
 					freeStackElement.getGLView().broadcastElements(
 							EVAOperation.REMOVE_ELEMENT);
 				} else {
 					freeStackElement = stackLevel.getNextFree();
 				}
 
 				if (!focusLevel.hasFreePosition()) {
 					// Slerp focus view to free spot in stack
 					SlerpAction reverseSlerpAction2 = new SlerpAction(
 							focusLevel.getElementByPositionIndex(0), freeStackElement);
 					arSlerpActions.add(reverseSlerpAction2);
 				}
 
 				// Slerp selected view from transition position to focus
 				// position
 				SlerpAction slerpAction = new SlerpAction(glView.getID(),
 						transitionLevel.getElementByPositionIndex(0),
 						focusLevel.getElementByPositionIndex(0));
 				arSlerpActions.add(slerpAction);
 			}
 		}
 
 		iSlerpFactor = 0;
 	}
 
 	@Override
 	public void handleSelectionUpdate(ISelectionDelta selectionDelta,
 			boolean scrollToSelection, String info) {
 		lastSelectionDelta = selectionDelta;
 		bUpdateOffScreenTextures = true;
 	}
 
 	@Override
 	public void addInitialRemoteView(ASerializedView serView) {
 		newViews.add(serView);
 	}
 
 	/**
 	 * Add pathway view. Also used when serialized pathways are loaded.
 	 * 
 	 * @param iPathwayID
 	 */
 	public void addPathwayView(final int iPathwayID) {
 
 		if (!PathwayManager.get().isPathwayVisible(
 				PathwayManager.get().getItem(iPathwayID))) {
 			SerializedPathwayView serPathway = new SerializedPathwayView(
 					dataDomain.getDataDomainType());
 			serPathway.setPathwayID(iPathwayID);
 			newViews.add(serPathway);
 		}
 	}
 
 	@Override
 	protected void handlePickingEvents(EPickingType pickingType,
 			EPickingMode pickingMode, int iExternalID, Pick pick) {
 
 		switch (pickingType) {
 		case REMOTE_VIEW_DRAG:
 
 			switch (pickingMode) {
 			case CLICKED:
 
 				if (!dragAndDrop.isDragActionRunning()) {
 					// System.out.println("Start drag!");
 					dragAndDrop.startDragAction(iExternalID);
 				}
 
 				iMouseOverObjectID = iExternalID;
 
 				compactPoolLevel();
 
 				break;
 			}
 			break;
 
 		case REMOTE_VIEW_REMOVE:
 
 			switch (pickingMode) {
 			case CLICKED:
 
 				RemoteLevelElement element = RemoteElementManager.get().getItem(
 						iExternalID);
 
 				AGLView glView = element.getGLView();
 
 				// // Unregister all elements of the view that is
 				// removed
 				// glEventListener.broadcastElements(EVAOperation.REMOVE_ELEMENT);
 
 				removeView(glView);
 				element.setGLView(null);
 				containedGLViews.remove(glView);
 
 				if (element.getRemoteLevel() == poolLevel) {
 					compactPoolLevel();
 				}
 
 				break;
 			}
 			break;
 
 		case REMOTE_VIEW_LOCK:
 
 			switch (pickingMode) {
 			case CLICKED:
 
 				RemoteLevelElement element = RemoteElementManager.get().getItem(
 						iExternalID);
 
 				// Toggle lock flag
 				element.lock(!element.isLocked());
 
 				break;
 			}
 			break;
 
 		case REMOTE_LEVEL_ELEMENT:
 			switch (pickingMode) {
 			case MOUSE_OVER:
 			case DRAGGED:
 				iMouseOverObjectID = iExternalID;
 				break;
 			case CLICKED:
 
 				// Do not handle click if element is dragged
 				if (dragAndDrop.isDragActionRunning()) {
 					break;
 				}
 
 				// Check if view is contained in pool level
 				for (RemoteLevelElement element : poolLevel.getAllElements()) {
 					if (element.getID() == iExternalID) {
 						loadViewToFocusLevel(iExternalID);
 						break;
 					}
 				}
 				break;
 			}
 			break;
 
 		case REMOTE_VIEW_SELECTION:
 			switch (pickingMode) {
 			case MOUSE_OVER:
 
 				// generalManager.getViewGLCanvasManager().getInfoAreaManager()
 				// .setDataAboutView(iExternalID);
 
 				// Prevent update flood when moving mouse over view
 				if (iActiveViewID == iExternalID) {
 					break;
 				}
 
 				iActiveViewID = iExternalID;
 
 				setDisplayListDirty();
 
 				// TODO
 				// generalManager.getEventPublisher().triggerEvent(
 				// EMediatorType.VIEW_SELECTION,
 				// generalManager.getViewGLCanvasManager().getGLEventListener(
 				// iExternalID), );
 
 				break;
 
 			case CLICKED:
 
 				// generalManager.getViewGLCanvasManager().getInfoAreaManager()
 				// .setDataAboutView(iExternalID);
 
 				break;
 			case RIGHT_CLICKED:
 				contextMenu.setLocation(pick.getPickedPoint(), getParentGLCanvas()
 						.getWidth(), getParentGLCanvas().getHeight());
 				contextMenu.setMasterGLView(this);
 				break;
 
 			}
 //			infoAreaManager.setData(iExternalID, dataDomain.get,
 //					pick.getPickedPoint(), 0.3f);// pick.getDepth());
 			break;
 
 		case BUCKET_MOVE_IN_ICON_SELECTION:
 			switch (pickingMode) {
 			case CLICKED:
 				loadViewToFocusLevel(iExternalID);
 				bEnableNavigationOverlay = false;
 				// glConnectionLineRenderer.enableRendering(true);
 				break;
 
 			case MOUSE_OVER:
 				iNavigationMouseOverViewID_left = -1;
 				iNavigationMouseOverViewID_right = -1;
 				iNavigationMouseOverViewID_out = -1;
 				iNavigationMouseOverViewID_in = iExternalID;
 				iNavigationMouseOverViewID_lock = -1;
 
 				break;
 			}
 			break;
 
 		case BUCKET_MOVE_OUT_ICON_SELECTION:
 			switch (pickingMode) {
 			case CLICKED:
 
 				// Check if other slerp action is currently running
 				if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE) {
 					break;
 				}
 
 				// glConnectionLineRenderer.enableRendering(true);
 
 				arSlerpActions.clear();
 
 				RemoteLevelElement element = RemoteElementManager.get().getItem(
 						iExternalID);
 				SlerpAction slerpActionTransition = new SlerpAction(element,
 						poolLevel.getNextFree());
 				arSlerpActions.add(slerpActionTransition);
 
 				bEnableNavigationOverlay = false;
 
 				// Unregister all elements of the view that is moved out
 				element.getGLView().broadcastElements(EVAOperation.REMOVE_ELEMENT);
 
 				break;
 
 			case MOUSE_OVER:
 
 				iNavigationMouseOverViewID_left = -1;
 				iNavigationMouseOverViewID_right = -1;
 				iNavigationMouseOverViewID_out = iExternalID;
 				iNavigationMouseOverViewID_in = -1;
 				iNavigationMouseOverViewID_lock = -1;
 
 				break;
 			}
 			break;
 
 		case BUCKET_MOVE_LEFT_ICON_SELECTION:
 			switch (pickingMode) {
 			case CLICKED:
 				// Check if other slerp action is currently running
 				if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE) {
 					break;
 				}
 
 				// glConnectionLineRenderer.enableRendering(true);
 
 				arSlerpActions.clear();
 
 				RemoteLevelElement selectedElement = RemoteElementManager.get().getItem(
 						iExternalID);
 
 				int iDestinationPosIndex = stackLevel
 						.getPositionIndexByElementID(selectedElement);
 
 				if (iDestinationPosIndex == 3) {
 					iDestinationPosIndex = 0;
 				} else {
 					iDestinationPosIndex++;
 				}
 
 				// Check if destination position in stack is free
 				if (stackLevel.getElementByPositionIndex(iDestinationPosIndex)
 						.getGLView() == null) {
 					SlerpAction slerpAction = new SlerpAction(selectedElement,
 							stackLevel.getElementByPositionIndex(iDestinationPosIndex));
 					arSlerpActions.add(slerpAction);
 				} else {
 					SlerpAction slerpActionTransition = new SlerpAction(selectedElement,
 							transitionLevel.getElementByPositionIndex(0));
 					arSlerpActions.add(slerpActionTransition);
 
 					SlerpAction slerpAction = new SlerpAction(
 							stackLevel.getElementByPositionIndex(iDestinationPosIndex),
 							selectedElement);
 					arSlerpActions.add(slerpAction);
 
 					SlerpAction slerpActionTransitionReverse = new SlerpAction(
 							selectedElement.getGLView().getID(),
 							transitionLevel.getElementByPositionIndex(0),
 							stackLevel.getElementByPositionIndex(iDestinationPosIndex));
 					arSlerpActions.add(slerpActionTransitionReverse);
 				}
 
 				bEnableNavigationOverlay = false;
 
 				break;
 
 			case MOUSE_OVER:
 
 				iNavigationMouseOverViewID_left = iExternalID;
 				iNavigationMouseOverViewID_right = -1;
 				iNavigationMouseOverViewID_out = -1;
 				iNavigationMouseOverViewID_in = -1;
 				iNavigationMouseOverViewID_lock = -1;
 
 				break;
 			}
 			break;
 
 		case BUCKET_MOVE_RIGHT_ICON_SELECTION:
 			switch (pickingMode) {
 			case CLICKED:
 				// Check if other slerp action is currently running
 				if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE) {
 					break;
 				}
 
 				// glConnectionLineRenderer.enableRendering(true);
 
 				arSlerpActions.clear();
 
 				RemoteLevelElement selectedElement = RemoteElementManager.get().getItem(
 						iExternalID);
 
 				int iDestinationPosIndex = stackLevel
 						.getPositionIndexByElementID(selectedElement);
 
 				if (iDestinationPosIndex == 0) {
 					iDestinationPosIndex = 3;
 				} else {
 					iDestinationPosIndex--;
 				}
 
 				// Check if destination position in stack is free
 				if (stackLevel.getElementByPositionIndex(iDestinationPosIndex)
 						.getGLView() == null) {
 					SlerpAction slerpAction = new SlerpAction(selectedElement,
 							stackLevel.getElementByPositionIndex(iDestinationPosIndex));
 					arSlerpActions.add(slerpAction);
 				} else {
 					SlerpAction slerpActionTransition = new SlerpAction(selectedElement,
 							transitionLevel.getElementByPositionIndex(0));
 					arSlerpActions.add(slerpActionTransition);
 
 					SlerpAction slerpAction = new SlerpAction(
 							stackLevel.getElementByPositionIndex(iDestinationPosIndex),
 							selectedElement);
 					arSlerpActions.add(slerpAction);
 
 					SlerpAction slerpActionTransitionReverse = new SlerpAction(
 							selectedElement.getGLView().getID(),
 							transitionLevel.getElementByPositionIndex(0),
 							stackLevel.getElementByPositionIndex(iDestinationPosIndex));
 					arSlerpActions.add(slerpActionTransitionReverse);
 				}
 
 				bEnableNavigationOverlay = false;
 
 				break;
 
 			case MOUSE_OVER:
 
 				iNavigationMouseOverViewID_left = -1;
 				iNavigationMouseOverViewID_right = iExternalID;
 				iNavigationMouseOverViewID_out = -1;
 				iNavigationMouseOverViewID_in = -1;
 				iNavigationMouseOverViewID_lock = -1;
 
 				break;
 			}
 			break;
 		case CONTEXT_MENU_SELECTION:
 			System.out.println("Waa");
 			break;
 		}
 	}
 
 	@Override
 	public String getShortInfo() {
 		AGLView activeView = GeneralManager.get().getViewGLCanvasManager()
 				.getGLView(iActiveViewID);
 		if (activeView == null)
 			return "Bucket";
 		else
 			return "Bucket, active embedded view: " + activeView.getShortInfo();
 	}
 
 	@Override
 	public String getDetailedInfo() {
 		StringBuffer sInfoText = new StringBuffer();
 		sInfoText.append("Bucket / Jukebox");
 		return sInfoText.toString();
 	}
 
 	public void toggleLayoutMode() {
 		if (layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.BUCKET)) {
 			// layoutMode = ARemoteViewLayoutRenderStyle.LayoutMode.LIST;
 			layoutMode = ARemoteViewLayoutRenderStyle.LayoutMode.JUKEBOX;
 		} else {
 			layoutMode = ARemoteViewLayoutRenderStyle.LayoutMode.BUCKET;
 		}
 
 		if (layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.BUCKET)) {
 			layoutRenderStyle = new BucketLayoutRenderStyle(viewFrustum,
 					layoutRenderStyle);
 
 			bucketMouseWheelListener = new BucketMouseWheelListener(this,
 					(BucketLayoutRenderStyle) layoutRenderStyle);
 
 			// Unregister standard mouse wheel listener
 			parentGLCanvas.removeMouseWheelListener(glMouseListener);
 			parentGLCanvas.removeMouseListener(glMouseListener);
 			// Register specialized bucket mouse wheel listener
 			parentGLCanvas.addMouseWheelListener(bucketMouseWheelListener);
 			parentGLCanvas.addMouseListener(bucketMouseWheelListener);
 
 			glConnectionLineRenderer = new GLConnectionLineRendererBucket(focusLevel,
 					stackLevel);
 		} else if (layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.JUKEBOX)) {
 			layoutRenderStyle = new JukeboxLayoutRenderStyle(viewFrustum,
 					layoutRenderStyle);
 
 			// Unregister bucket wheel listener
 			parentGLCanvas.removeMouseWheelListener(bucketMouseWheelListener);
 			// Register standard mouse wheel listener
 			parentGLCanvas.addMouseWheelListener(glMouseListener);
 
 			glConnectionLineRenderer = new GLConnectionLineRendererJukebox(focusLevel,
 					stackLevel, poolLevel);
 		} else if (layoutMode.equals(ARemoteViewLayoutRenderStyle.LayoutMode.LIST)) {
 			layoutRenderStyle = new ListLayoutRenderStyle(viewFrustum, layoutRenderStyle);
 			glConnectionLineRenderer = null;
 
 			// // Copy views from stack to pool
 			// for (Integer iElementID : stackLevel.getElementList())
 			// {
 			// if (iElementID == -1)
 			// continue;
 			//
 			// poolLevel.addElement(iElementID);
 			// // poolLevel.setElementVisibilityById(true, iElementID);
 			// }
 			// stackLevel.clear();
 		}
 
 		focusLevel = layoutRenderStyle.initFocusLevel();
 		stackLevel = layoutRenderStyle.initStackLevel();
 		poolLevel = layoutRenderStyle.initPoolLevel(-1);
 		externalSelectionLevel = layoutRenderStyle.initMemoLevel();
 		transitionLevel = layoutRenderStyle.initTransitionLevel();
 		spawnLevel = layoutRenderStyle.initSpawnLevel();
 
 		viewFrustum.setProjectionMode(layoutRenderStyle.getProjectionMode());
 
 		// Trigger reshape to apply new projection mode
 		// Is there a better way to achieve this? :)
 		parentGLCanvas.setSize(parentGLCanvas.getWidth(), parentGLCanvas.getHeight());
 	}
 
 	/**
 	 * Unregister view from event system. Remove view from GL render loop.
 	 */
 	public void removeView(AGLView glEventListener) {
 		if (glEventListener != null) {
 			glEventListener.destroy();
 		}
 	}
 
 	public void resetView(boolean reinitialize) {
 
 		dataDomain.resetContextVA();
 		if (containedGLViews == null)
 			return;
 
 		enableBusyMode(false);
 		pickingManager.enablePicking(true);
 
 		if (reinitialize) {
 			ArrayList<ASerializedView> removeNewViews = new ArrayList<ASerializedView>();
 			for (ASerializedView view : newViews) {
 				if (!(view.getViewType().equals("org.caleydo.view.heatmap") || view
 						.getViewType().equals("org.caleydo.view.parcoords"))) {
 					removeNewViews.add(view);
 				}
 			}
 			newViews.removeAll(removeNewViews);
 		} else {
 			newViews.clear();
 		}
 
 		IViewManager viewManager = generalManager.getViewGLCanvasManager();
 
 		if (reinitialize) {
 			ArrayList<AGLView> removeView = new ArrayList<AGLView>();
 			for (AGLView glView : containedGLViews) {
 				if (!(glView.getViewType().equals("org.caleydo.view.parcoords") || glView
 						.getViewType().equals("org.caleydo.view.heatmap"))) {
 					removeView.add(glView);
 				}
 			}
 			containedGLViews.removeAll(removeView);
 		} else {
 			containedGLViews.clear();
 		}
 
 		if (reinitialize) {
 			PathwayManager.get().resetPathwayVisiblityState();
 		}
 
 		// Send out remove broadcast for views that are currently slerped
 		for (SlerpAction slerpAction : arSlerpActions) {
 			viewManager.getGLView(slerpAction.getElementId()).broadcastElements(
 					EVAOperation.REMOVE_ELEMENT);
 		}
 		arSlerpActions.clear();
 
 		clearRemoteLevel(focusLevel);
 		clearRemoteLevel(stackLevel);
 		clearRemoteLevel(poolLevel);
 		clearRemoteLevel(transitionLevel);
 
 		if (reinitialize) {
 			// Move heat map and par coords view to its initial position in the
 			// bucket
 			for (AGLView view : containedGLViews) {
 				if (view.getViewType().equals("org.caleydo.view.parcoords")) {
 					stackLevel.getElementByPositionIndex(0).setGLView(view);
 					view.setRemoteLevelElement(stackLevel.getElementByPositionIndex(0));
 				} else if (view.getViewType().equals("org.caleydo.view.heatmap")) {
 					focusLevel.getElementByPositionIndex(0).setGLView(view);
 					view.setRemoteLevelElement(focusLevel.getElementByPositionIndex(0));
 				}
 			}
 		}
 
 		generalManager.getViewGLCanvasManager()
 				.getConnectedElementRepresentationManager().clearAll();
 	}
 
 	@Override
 	public void resetView() {
 		resetView(true);
 	}
 
 	private void clearRemoteLevel(RemoteLevel remoteLevel) {
 
 		AGLView glView = null;
 
 		for (RemoteLevelElement element : remoteLevel.getAllElements()) {
 			glView = element.getGLView();
 
 			if (glView == null) {
 				continue;
 			}
 
 			if (glView.getViewType().equals("org.caleydo.view.heatmap")
 					|| glView.getViewType().equals("org.caleydo.view.parcoords")) {
 				// Remove all elements from heatmap and parallel coordinates
 				((AStorageBasedView) glView).resetView();
 
 				if (!glView.isRenderedRemote()) {
 					glView.enableBusyMode(false);
 				}
 			} else {
 				removeView(glView);
 				glView.broadcastElements(EVAOperation.REMOVE_ELEMENT);
 			}
 
 			element.setGLView(null);
 		}
 	}
 
 	@Override
 	public RemoteLevel getFocusLevel() {
 		return focusLevel;
 	}
 
 	@Override
 	public BucketMouseWheelListener getBucketMouseWheelListener() {
 		return bucketMouseWheelListener;
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
 		super.reshape(drawable, x, y, width, height);
 
 		// Update aspect ratio and reinitialize stack and focus layer
 		layoutRenderStyle.setAspectRatio(fAspectRatio);
 
 		layoutRenderStyle.initFocusLevel();
 		layoutRenderStyle.initStackLevel();
 		layoutRenderStyle.initPoolLevel(iMouseOverObjectID);
 		layoutRenderStyle.initMemoLevel();
 	}
 
 	protected void renderPoolAndMemoLayerBackground(final GL gl) {
 
 		float fXCorrection = 0.07f; // Detach pool level from stack
 
 		float fZ;
 		if (bucketMouseWheelListener.isZoomedIn())
 			fZ = -0.005f;
 		else
 			fZ = 4f;
 
 		float fXScaling = 1;
 		float fYScaling = 1;
 
 		if (fAspectRatio < 1) {
 			fXScaling = 1 / fAspectRatio;
 			fYScaling = 1;
 		} else {
 			fXScaling = 1;
 			fYScaling = fAspectRatio;
 		}
 
 		float fLeftSceneBorder = (-2 - fXCorrection) * fXScaling;
 		float fBottomSceneBorder = -2 * fYScaling;
 
 		if (layoutMode.equals(LayoutMode.BUCKET)) {
 			gl.glPushName(pickingManager.getPickingID(iUniqueID,
 					EPickingType.REMOTE_LEVEL_ELEMENT, iPoolLevelCommonID));
 
 			gl.glColor4fv(GeneralRenderStyle.PANEL_BACKGROUN_COLOR, 0);
 			gl.glLineWidth(1);
 
 			gl.glBegin(GL.GL_POLYGON);
 			gl.glVertex3f(fLeftSceneBorder, fBottomSceneBorder, fZ);
 			gl.glVertex3f(fLeftSceneBorder, -fBottomSceneBorder, fZ);
 			gl.glVertex3f(fLeftSceneBorder + BucketLayoutRenderStyle.SIDE_PANEL_WIDTH,
 					-fBottomSceneBorder, fZ);
 			gl.glVertex3f(fLeftSceneBorder + BucketLayoutRenderStyle.SIDE_PANEL_WIDTH,
 					fBottomSceneBorder, fZ);
 			gl.glEnd();
 
 			if (dragAndDrop.isDragActionRunning()
 					&& iMouseOverObjectID == iPoolLevelCommonID) {
 				gl.glLineWidth(5);
 				gl.glColor4f(0.2f, 0.2f, 0.2f, 1);
 			} else {
 				gl.glLineWidth(1);
 				gl.glColor4f(0.4f, 0.4f, 0.4f, 1);
 			}
 
 			gl.glBegin(GL.GL_LINE_LOOP);
 			gl.glVertex3f(fLeftSceneBorder, fBottomSceneBorder, fZ);
 			gl.glVertex3f(fLeftSceneBorder, -fBottomSceneBorder, fZ);
 			gl.glVertex3f(fLeftSceneBorder + BucketLayoutRenderStyle.SIDE_PANEL_WIDTH,
 					-fBottomSceneBorder, fZ);
 			gl.glVertex3f(fLeftSceneBorder + BucketLayoutRenderStyle.SIDE_PANEL_WIDTH,
 					fBottomSceneBorder, fZ);
 			gl.glEnd();
 
 			gl.glPopName();
 
 			// Render selection heat map list background
 			gl.glColor4fv(GeneralRenderStyle.PANEL_BACKGROUN_COLOR, 0);
 			gl.glLineWidth(1);
 			gl.glBegin(GL.GL_POLYGON);
 			gl.glVertex3f(-fLeftSceneBorder, fBottomSceneBorder, fZ);
 			gl.glVertex3f(-fLeftSceneBorder, -fBottomSceneBorder, fZ);
 			gl.glVertex3f(-fLeftSceneBorder - BucketLayoutRenderStyle.SIDE_PANEL_WIDTH,
 					-fBottomSceneBorder, fZ);
 			gl.glVertex3f(-fLeftSceneBorder - BucketLayoutRenderStyle.SIDE_PANEL_WIDTH,
 					fBottomSceneBorder, fZ);
 			gl.glEnd();
 
 			gl.glColor4f(0.4f, 0.4f, 0.4f, 1);
 			gl.glLineWidth(1);
 			gl.glBegin(GL.GL_LINE_LOOP);
 			gl.glVertex3f(-fLeftSceneBorder, fBottomSceneBorder, fZ);
 			gl.glVertex3f(-fLeftSceneBorder, -fBottomSceneBorder, fZ);
 			gl.glVertex3f(-fLeftSceneBorder - BucketLayoutRenderStyle.SIDE_PANEL_WIDTH,
 					-fBottomSceneBorder, fZ);
 			gl.glVertex3f(-fLeftSceneBorder - BucketLayoutRenderStyle.SIDE_PANEL_WIDTH,
 					fBottomSceneBorder, fZ);
 			gl.glEnd();
 		}
 
 		// Render caption
 		if (textRenderer == null)
 			return;
 
 		String sTmp = "POOL AREA";
 		textRenderer.begin3DRendering();
 		textRenderer.setColor(0.6f, 0.6f, 0.6f, 1.0f);
 		textRenderer.draw3D(sTmp, (-1.9f - fXCorrection) / fAspectRatio, -1.97f,
 				fZ + 0.01f, 0.003f);
 		textRenderer.end3DRendering();
 	}
 
 	@Override
 	public void broadcastElements(EVAOperation type) {
 		// do nothing
 	}
 
 	/**
 	 * Adds new remote-rendered-views that have been queued for displaying to
 	 * this view. Only one view is taken from the list and added for remote
 	 * rendering per call to this method.
 	 * 
 	 * @param GL
 	 */
 	private void initNewView(GL gl) {
 		if (!newViews.isEmpty()
 				&& PathwayManager.get().isPathwayLoadingFinished()
 				&& arSlerpActions.isEmpty()) {
 
 			ASerializedView serView = newViews.remove(0);
 			AGLView view = createView(gl, serView);
 			if (hasFreeViewPosition()) {
 				addSlerpActionForView(gl, view);
 				containedGLViews.add(view);
 			} else {
 				newViews.clear();
 			}
 			if (newViews.isEmpty()) {
 				triggerToolBarUpdate();
 				enableUserInteraction();
 			}
 		}
 	}
 
 	/**
 	 * Triggers a toolbar update by sending an event similar to the view
 	 * activation
 	 * 
 	 * @TODO: Move to remote rendering base class
 	 */
 	private void triggerToolBarUpdate() {
 
 		ViewActivationEvent viewActivationEvent = new ViewActivationEvent();
 		viewActivationEvent.setSender(this);
 		List<AGLView> glViews = getRemoteRenderedViews();
 
 		List<IView> views = new ArrayList<IView>();
 		views.add(this);
 		for (AGLView view : glViews) {
 			views.add(view);
 		}
 
 		viewActivationEvent.setViews(views);
 
 		IEventPublisher eventPublisher = GeneralManager.get().getEventPublisher();
 		eventPublisher.triggerEvent(viewActivationEvent);
 	}
 
 	/**
 	 * Checks if this view has some space left to add at least 1 view
 	 * 
 	 * @return <code>true</code> if some space is left, <code>false</code>
 	 *         otherwise
 	 */
 	public boolean hasFreeViewPosition() {
 		return focusLevel.hasFreePosition()
 				|| (stackLevel.hasFreePosition() && !(layoutRenderStyle instanceof ListLayoutRenderStyle))
 				|| poolLevel.hasFreePosition();
 	}
 
 	/**
 	 * Adds a Slerp-Transition for a view. Usually this is used when a new view
 	 * is added to the bucket or 2 views change its position in the bucket. The
 	 * operation does not always succeed. A reason for this is when no more
 	 * space is left to slerp the given view to.
 	 * 
 	 * @param gl
 	 * @param view
 	 *            the view for which the slerp transition should be added
 	 * @return <code>true</code> if adding the slerp action was successfull,
 	 *         <code>false</code> otherwise
 	 */
 	private boolean addSlerpActionForView(GL gl, AGLView view) {
 
 		RemoteLevelElement origin = spawnLevel.getElementByPositionIndex(0);
 		RemoteLevelElement destination = null;
 
 		if (focusLevel.hasFreePosition()) {
 			destination = focusLevel.getNextFree();
 			view.broadcastElements(EVAOperation.APPEND_UNIQUE);
 		} else if (stackLevel.hasFreePosition()
 				&& !(layoutRenderStyle instanceof ListLayoutRenderStyle)) {
 			destination = stackLevel.getNextFree();
 			view.broadcastElements(EVAOperation.APPEND_UNIQUE);
 		} else if (poolLevel.hasFreePosition()) {
 			destination = poolLevel.getNextFree();
 		} else {
 			GeneralManager
 					.get()
 					.getLogger()
 					.log(new Status(IStatus.WARNING, IGeneralManager.PLUGIN_ID,
 							"No empty space left to add new pathway!"));
 			newViews.clear();
 			return false;
 		}
 
 		origin.setGLView(view);
 		SlerpAction slerpActionTransition = new SlerpAction(origin, destination);
 		arSlerpActions.add(slerpActionTransition);
 
 		view.initRemote(gl, this, glMouseListener, infoAreaManager);
 		view.setDetailLevel(EDetailLevel.MEDIUM);
 
 		return true;
 	}
 
 	/**
 	 * Creates and initializes a new view based on its serialized form. The view
 	 * is already added to the list of event receivers and senders.
 	 * 
 	 * @param gl
 	 * @param serView
 	 *            serialized form of the view to create
 	 * @return the created view ready to be used within the application
 	 */
 	@SuppressWarnings("unchecked")
 	private AGLView createView(GL gl, ASerializedView serView) {
 
 		ICommandManager cm = generalManager.getCommandManager();
 		CmdCreateView cmdView = (CmdCreateView) cm
 				.createCommandByType(ECommandType.CREATE_GL_VIEW);
 		cmdView.setViewID(serView.getViewType());
 		cmdView.setAttributesFromSerializedForm(serView);
 		// cmdView.setSet(set);
 		cmdView.doCommand();
 
 		AGLView glView = cmdView.getCreatedObject();
 		glView.setRemoteRenderingGLView(this);
 
 		if (glView instanceof IDataDomainBasedView<?>) {
 			((IDataDomainBasedView<ISetBasedDataDomain>) glView)
 					.setDataDomain(dataDomain);
 		}
 
 		if (glView instanceof GLPathway) {
 			GLPathway glPathway = (GLPathway) glView;
 
 			glPathway.setPathway(((SerializedPathwayView) serView).getPathwayID());
 			glPathway.enablePathwayTextures(pathwayTexturesEnabled);
 			glPathway.enableNeighborhood(neighborhoodEnabled);
 			glPathway.enableGeneMapping(geneMappingEnabled);
 		}
 
 		triggerMostRecentDelta();
 
 		return glView;
 	}
 
 	/**
 	 * Triggers the most recent user selection to the views. This is especially
 	 * needed to initialize new added views with the current selection
 	 * information.
 	 */
 	private void triggerMostRecentDelta() {
 		// Trigger last delta to new views
 		if (lastSelectionDelta != null) {
 			SelectionUpdateEvent event = new SelectionUpdateEvent();
 			event.setSender(this);
 			event.setSelectionDelta((SelectionDelta) lastSelectionDelta);
 			event.setInfo(getShortInfo());
 			eventPublisher.triggerEvent(event);
 		}
 	}
 
 	/**
 	 * Disables picking and enables busy mode
 	 */
 	public void disableUserInteraction() {
 		IViewManager canvasManager = generalManager.getViewGLCanvasManager();
 		canvasManager.getPickingManager().enablePicking(false);
 		canvasManager.requestBusyMode(this);
 	}
 
 	/**
 	 * Enables picking and disables busy mode
 	 */
 	public void enableUserInteraction() {
 		IViewManager canvasManager = generalManager.getViewGLCanvasManager();
 		canvasManager.getPickingManager().enablePicking(true);
 		canvasManager.releaseBusyMode(this);
 	}
 
 	public void loadDependentPathways(Set<PathwayGraph> newPathwayGraphs) {
 
 		// add new pathways to bucket
 		for (PathwayGraph pathway : newPathwayGraphs) {
 			addPathwayView(pathway.getID());
 		}
 
 		if (!newViews.isEmpty()) {
 			// Zoom out of the bucket when loading pathways
 			if (bucketMouseWheelListener.isZoomedIn()) {
 				bucketMouseWheelListener.triggerZoom(false);
 			}
 			disableUserInteraction();
 		}
 	}
 
 	@Override
 	public void enableBusyMode(boolean busyMode) {
 		super.enableBusyMode(busyMode);
 
 		if (eBusyModeState == EBusyModeState.ON) {
 			parentGLCanvas.removeMouseListener(bucketMouseWheelListener);
 			parentGLCanvas.removeMouseWheelListener(bucketMouseWheelListener);
 		} else {
 			parentGLCanvas.addMouseListener(bucketMouseWheelListener);
 			parentGLCanvas.addMouseWheelListener(bucketMouseWheelListener);
 		}
 	}
 
 	@Override
 	public int getNumberOfSelections(SelectionType SelectionType) {
 		return 0;
 	}
 
 	private void compactPoolLevel() {
 		RemoteLevelElement element;
 		RemoteLevelElement elementInner;
 		for (int iIndex = 0; iIndex < poolLevel.getCapacity(); iIndex++) {
 			element = poolLevel.getElementByPositionIndex(iIndex);
 			if (element.isFree()) {
 				// Search for next element to put it in the free position
 				for (int iInnerIndex = iIndex + 1; iInnerIndex < poolLevel.getCapacity(); iInnerIndex++) {
 					elementInner = poolLevel.getElementByPositionIndex(iInnerIndex);
 
 					if (elementInner.isFree()) {
 						continue;
 					}
 
 					element.setGLView(elementInner.getGLView());
 					elementInner.setGLView(null);
 
 					break;
 				}
 			}
 		}
 	}
 
 	@Override
 	public List<AGLView> getRemoteRenderedViews() {
 		return containedGLViews;
 	}
 
 	private void updateOffScreenTextures(final GL gl) {
 
 		if (glOffScreenRenderer == null)
 			return;
 
 		bUpdateOffScreenTextures = false;
 
 		gl.glPushMatrix();
 
 		int iViewWidth = parentGLCanvas.getWidth();
 		int iViewHeight = parentGLCanvas.getHeight();
 
 		if (stackLevel.getElementByPositionIndex(0).getGLView() != null) {
 			glOffScreenRenderer.renderToTexture(gl,
 					stackLevel.getElementByPositionIndex(0).getGLView().getID(), 0,
 					iViewWidth, iViewHeight);
 		}
 
 		if (stackLevel.getElementByPositionIndex(1).getGLView() != null) {
 			glOffScreenRenderer.renderToTexture(gl,
 					stackLevel.getElementByPositionIndex(1).getGLView().getID(), 1,
 					iViewWidth, iViewHeight);
 		}
 
 		if (stackLevel.getElementByPositionIndex(2).getGLView() != null) {
 			glOffScreenRenderer.renderToTexture(gl,
 					stackLevel.getElementByPositionIndex(2).getGLView().getID(), 2,
 					iViewWidth, iViewHeight);
 		}
 
 		if (stackLevel.getElementByPositionIndex(3).getGLView() != null) {
 			glOffScreenRenderer.renderToTexture(gl,
 					stackLevel.getElementByPositionIndex(3).getGLView().getID(), 3,
 					iViewWidth, iViewHeight);
 		}
 
 		gl.glPopMatrix();
 	}
 
 	@Override
 	public void clearAllSelections() {
 		for (AGLView view : containedGLViews) {
 			view.clearAllSelections();
 		}
 	}
 
 	/**
 	 * FIXME: should be moved to a bucket-mediator registers the event-listeners
 	 * to the event framework
 	 */
 	@Override
 	public void registerEventListeners() {
 		super.registerEventListeners();
 
 		addPathwayListener = new AddPathwayListener();
 		addPathwayListener.setHandler(this);
 		eventPublisher.addListener(LoadPathwayEvent.class, addPathwayListener);
 
 		loadPathwaysByGeneListener = new LoadPathwaysByGeneListener();
 		loadPathwaysByGeneListener.setHandler(this);
 		eventPublisher.addListener(LoadPathwaysByGeneEvent.class,
 				loadPathwaysByGeneListener);
 
 		enableTexturesListener = new EnableTexturesListener();
 		enableTexturesListener.setHandler(this);
 		eventPublisher.addListener(EnableTexturesEvent.class, enableTexturesListener);
 
 		disableTexturesListener = new DisableTexturesListener();
 		disableTexturesListener.setHandler(this);
 		eventPublisher.addListener(DisableTexturesEvent.class, disableTexturesListener);
 
 		enableGeneMappingListener = new EnableGeneMappingListener();
 		enableGeneMappingListener.setHandler(this);
 		eventPublisher.addListener(EnableGeneMappingEvent.class,
 				enableGeneMappingListener);
 
 		disableGeneMappingListener = new DisableGeneMappingListener();
 		disableGeneMappingListener.setHandler(this);
 		eventPublisher.addListener(DisableGeneMappingEvent.class,
 				disableGeneMappingListener);
 
 		enableNeighborhoodListener = new EnableNeighborhoodListener();
 		enableNeighborhoodListener.setHandler(this);
 		eventPublisher.addListener(EnableNeighborhoodEvent.class,
 				enableNeighborhoodListener);
 
 		disableNeighborhoodListener = new DisableNeighborhoodListener();
 		disableNeighborhoodListener.setHandler(this);
 		eventPublisher.addListener(DisableNeighborhoodEvent.class,
 				disableNeighborhoodListener);
 
 		enableConnectionLinesListener = new EnableConnectionLinesListener();
 		enableConnectionLinesListener.setHandler(this);
 		eventPublisher.addListener(EnableConnectionLinesEvent.class,
 				enableConnectionLinesListener);
 
 		disableConnectionLinesListener = new DisableConnectionLinesListener();
 		disableConnectionLinesListener.setHandler(this);
 		eventPublisher.addListener(DisableConnectionLinesEvent.class,
 				disableConnectionLinesListener);
 
 		resetViewListener = new ResetViewListener();
 		resetViewListener.setHandler(this);
 		eventPublisher.addListener(ResetAllViewsEvent.class, resetViewListener);
 
 		eventPublisher.addListener(ResetRemoteRendererEvent.class, resetViewListener);
 
 		selectionUpdateListener = new SelectionUpdateListener();
 		selectionUpdateListener.setHandler(this);
 		eventPublisher.addListener(SelectionUpdateEvent.class, selectionUpdateListener);
 
 		toggleNavigationModeListener = new ToggleNavigationModeListener();
 		toggleNavigationModeListener.setHandler(this);
 		eventPublisher.addListener(ToggleNavigationModeEvent.class,
 				toggleNavigationModeListener);
 
 		toggleZoomListener = new ToggleZoomListener();
 		toggleZoomListener.setHandler(this);
 		eventPublisher.addListener(ToggleZoomEvent.class, toggleZoomListener);
 
 		// resetRemoteRendererListener = new ResetRemoteRendererListener();
 		// resetRemoteRendererListener.setHandler(this);
 		// eventPublisher.addListener(ResetRemoteRendererEvent.class,
 		// resetRemoteRendererListener);
 	}
 
 	/**
 	 * FIXME: should be moved to a bucket-mediator registers the event-listeners
 	 * to the event framework
 	 */
 	@Override
 	public void unregisterEventListeners() {
 
 		super.unregisterEventListeners();
 		if (addPathwayListener != null) {
 			eventPublisher.removeListener(addPathwayListener);
 			addPathwayListener = null;
 		}
 
 		if (loadPathwaysByGeneListener != null) {
 			eventPublisher.removeListener(loadPathwaysByGeneListener);
 			loadPathwaysByGeneListener = null;
 		}
 		if (enableTexturesListener != null) {
 			eventPublisher.removeListener(enableTexturesListener);
 			enableTexturesListener = null;
 		}
 		if (disableTexturesListener != null) {
 			eventPublisher.removeListener(disableTexturesListener);
 			disableTexturesListener = null;
 		}
 		if (enableGeneMappingListener != null) {
 			eventPublisher.removeListener(enableGeneMappingListener);
 			enableGeneMappingListener = null;
 		}
 		if (disableGeneMappingListener != null) {
 			eventPublisher.removeListener(disableGeneMappingListener);
 			disableGeneMappingListener = null;
 		}
 		if (enableNeighborhoodListener != null) {
 			eventPublisher.removeListener(enableNeighborhoodListener);
 			enableNeighborhoodListener = null;
 		}
 		if (disableNeighborhoodListener != null) {
 			eventPublisher.removeListener(disableNeighborhoodListener);
 			disableNeighborhoodListener = null;
 		}
 		if (enableConnectionLinesListener != null) {
 			eventPublisher.removeListener(enableConnectionLinesListener);
 			enableConnectionLinesListener = null;
 		}
 		if (disableConnectionLinesListener != null) {
 			eventPublisher.removeListener(disableConnectionLinesListener);
 			disableConnectionLinesListener = null;
 		}
 		if (resetViewListener != null) {
 			eventPublisher.removeListener(resetViewListener);
 			resetViewListener = null;
 		}
 		if (selectionUpdateListener != null) {
 			eventPublisher.removeListener(selectionUpdateListener);
 			selectionUpdateListener = null;
 		}
 		if (toggleZoomListener != null) {
 			eventPublisher.removeListener(toggleZoomListener);
 			toggleZoomListener = null;
 		}
 	}
 
 	@Override
 	public ASerializedView getSerializableRepresentation() {
 		SerializedBucketView serializedForm = new SerializedBucketView(
 				dataDomain.getDataDomainType());
 		serializedForm.setViewID(this.getID());
 		serializedForm.setPathwayTexturesEnabled(pathwayTexturesEnabled);
 		serializedForm.setNeighborhoodEnabled(neighborhoodEnabled);
 		serializedForm.setGeneMappingEnabled(geneMappingEnabled);
 		serializedForm.setConnectionLinesEnabled(connectionLinesEnabled);
 
 		ArrayList<ASerializedView> remoteViews = new ArrayList<ASerializedView>(
 				focusLevel.getAllElements().size());
 		for (RemoteLevelElement rle : focusLevel.getAllElements()) {
 			if (rle.getGLView() != null) {
 				AGLView remoteView = rle.getGLView();
 				remoteViews.add(remoteView.getSerializableRepresentation());
 			}
 		}
 		serializedForm.setFocusViews(remoteViews);
 
 		remoteViews = new ArrayList<ASerializedView>(stackLevel.getAllElements().size());
 		for (RemoteLevelElement rle : stackLevel.getAllElements()) {
 			if (rle.getGLView() != null) {
 				AGLView remoteView = rle.getGLView();
 				remoteViews.add(remoteView.getSerializableRepresentation());
 			}
 		}
 		serializedForm.setStackViews(remoteViews);
 
 		return serializedForm;
 	}
 
 	@Override
 	public void initFromSerializableRepresentation(ASerializedView ser) {
 		resetView(false);
 
 		SerializedBucketView serializedView = (SerializedBucketView) ser;
 
 		pathwayTexturesEnabled = serializedView.isPathwayTexturesEnabled();
 		neighborhoodEnabled = serializedView.isNeighborhoodEnabled();
 		geneMappingEnabled = serializedView.isGeneMappingEnabled();
 		connectionLinesEnabled = serializedView.isConnectionLinesEnabled();
 
 		for (ASerializedView remoteSerializedView : serializedView.getFocusViews()) {
 			newViews.add(remoteSerializedView);
 		}
 		for (ASerializedView remoteSerializedView : serializedView.getStackViews()) {
 			newViews.add(remoteSerializedView);
 		}
 
 		setDisplayListDirty();
 	}
 
 	@Override
 	public void destroy() {
 		selectionTransformer.destroy();
 		selectionTransformer = null;
 		super.destroy();
 	}
 
 	public boolean isGeneMappingEnabled() {
 		return geneMappingEnabled;
 	}
 
 	public void setGeneMappingEnabled(boolean geneMappingEnabled) {
 		this.geneMappingEnabled = geneMappingEnabled;
 	}
 
 	public boolean isPathwayTexturesEnabled() {
 		return pathwayTexturesEnabled;
 	}
 
 	public void setPathwayTexturesEnabled(boolean pathwayTexturesEnabled) {
 		this.pathwayTexturesEnabled = pathwayTexturesEnabled;
 	}
 
 	public boolean isNeighborhoodEnabled() {
 		return neighborhoodEnabled;
 	}
 
 	public void setNeighborhoodEnabled(boolean neighborhoodEnabled) {
 		this.neighborhoodEnabled = neighborhoodEnabled;
 	}
 
 	public boolean isConnectionLinesEnabled() {
 		return connectionLinesEnabled;
 	}
 
 	public void setConnectionLinesEnabled(boolean connectionLinesEnabled) {
 		this.connectionLinesEnabled = connectionLinesEnabled;
 	}
 
 	public void toggleNavigationMode() {
 		this.bEnableNavigationOverlay = !bEnableNavigationOverlay;
 	}
 
 	public void toggleZoom() {
 		bucketMouseWheelListener.triggerZoom(!bucketMouseWheelListener.isZoomedIn());
 	}
 
 	public RemoteLevel getStackLevel() {
 		return stackLevel;
 	}
 
 	public AGLConnectionLineRenderer getGlConnectionLineRenderer() {
 		return glConnectionLineRenderer;
 	}
 
 	@Override
 	public ASetBasedDataDomain getDataDomain() {
 		return dataDomain;
 	}
 
 	@Override
 	public void setDataDomain(ASetBasedDataDomain dataDomain) {
 		this.dataDomain = dataDomain;
 	}
 }
