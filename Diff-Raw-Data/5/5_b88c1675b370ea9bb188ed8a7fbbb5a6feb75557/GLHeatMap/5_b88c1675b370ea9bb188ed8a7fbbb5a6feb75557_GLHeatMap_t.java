 package org.caleydo.core.view.opengl.canvas.storagebased;
 
 import static org.caleydo.core.view.opengl.canvas.storagebased.HeatMapRenderStyle.FIELD_Z;
 import static org.caleydo.core.view.opengl.canvas.storagebased.HeatMapRenderStyle.SELECTION_Z;
 import static org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle.MOUSE_OVER_COLOR;
 import static org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle.MOUSE_OVER_LINE_WIDTH;
 import static org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle.SELECTED_COLOR;
 import static org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle.SELECTED_LINE_WIDTH;
 import gleem.linalg.Rotf;
 import gleem.linalg.Vec3f;
 import gleem.linalg.Vec4f;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 import javax.management.InvalidAttributeValueException;
 import javax.media.opengl.GL;
 
 import org.caleydo.core.data.collection.ESetType;
 import org.caleydo.core.data.collection.storage.EDataRepresentation;
 import org.caleydo.core.data.mapping.EIDType;
 import org.caleydo.core.data.selection.ESelectionCommandType;
 import org.caleydo.core.data.selection.ESelectionType;
 import org.caleydo.core.data.selection.IVirtualArray;
 import org.caleydo.core.data.selection.SelectedElementRep;
 import org.caleydo.core.data.selection.SelectionCommand;
 import org.caleydo.core.data.selection.SelectionManager;
 import org.caleydo.core.data.selection.delta.ISelectionDelta;
 import org.caleydo.core.data.selection.delta.IVirtualArrayDelta;
 import org.caleydo.core.data.selection.delta.SelectionDelta;
 import org.caleydo.core.manager.event.view.storagebased.SelectionUpdateEvent;
 import org.caleydo.core.manager.general.GeneralManager;
 import org.caleydo.core.manager.id.EManagedObjectType;
 import org.caleydo.core.manager.picking.EPickingMode;
 import org.caleydo.core.manager.picking.EPickingType;
 import org.caleydo.core.manager.picking.Pick;
 import org.caleydo.core.manager.usecase.EDataDomain;
 import org.caleydo.core.serialize.ASerializedView;
 import org.caleydo.core.util.clusterer.AffinityClusterer;
 import org.caleydo.core.util.clusterer.ClusterState;
 import org.caleydo.core.util.clusterer.EClustererAlgo;
 import org.caleydo.core.util.clusterer.EClustererType;
 import org.caleydo.core.util.clusterer.EDistanceMeasure;
 import org.caleydo.core.util.mapping.color.ColorMapping;
 import org.caleydo.core.util.mapping.color.ColorMappingManager;
 import org.caleydo.core.util.mapping.color.EColorMappingType;
 import org.caleydo.core.view.opengl.camera.IViewFrustum;
 import org.caleydo.core.view.opengl.canvas.AGLEventListener;
 import org.caleydo.core.view.opengl.canvas.EDetailLevel;
 import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
 import org.caleydo.core.view.opengl.canvas.remote.GLRemoteRendering;
 import org.caleydo.core.view.opengl.canvas.storagebased.listener.GLHeatMapKeyListener;
 import org.caleydo.core.view.opengl.mouse.GLMouseListener;
 import org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle;
 import org.caleydo.core.view.opengl.util.hierarchy.RemoteLevelElement;
 import org.caleydo.core.view.opengl.util.overlay.contextmenu.container.ExperimentContextMenuItemContainer;
 import org.caleydo.core.view.opengl.util.overlay.contextmenu.container.GeneContextMenuItemContainer;
 import org.caleydo.core.view.opengl.util.overlay.infoarea.GLInfoAreaManager;
 import org.caleydo.core.view.opengl.util.texture.EIconTextures;
 
 import com.sun.opengl.util.texture.Texture;
 import com.sun.opengl.util.texture.TextureCoords;
 
 /**
  * Rendering the GLHeatMap
  * 
  * @author Alexander Lex
  * @author Marc Streit
  */
 public class GLHeatMap
 	extends AStorageBasedView {
 	private HeatMapRenderStyle renderStyle;
 
 	private ColorMapping colorMapper;
 
 	private EIDType eFieldDataType = EIDType.EXPRESSION_INDEX;
 	private EIDType eStorageDataType = EIDType.EXPERIMENT_INDEX;
 
 	// private boolean bRenderHorizontally = false;
 
 	private Vec4f vecRotation = new Vec4f(-90, 0, 0, 1);
 
 	private Vec3f vecTranslation;
 
 	private float fAnimationTranslation = 0;
 
 	private boolean bIsTranslationAnimationActive = false;
 
 	private float fAnimationTargetTranslation = 0;
 
 	private SelectedElementRep elementRep;
 
 	private ArrayList<Float> fAlXDistances;
 
 	boolean bUseDetailLevel = true;
 
 	int iCurrentMouseOverElement = -1;
 
 	/**
 	 * Determines whether a bigger space between heat map and caption is needed or not. If false no cluster
 	 * info is available and therefore no additional space is needed. Set by remote rendering view (HHM).
 	 */
 	private boolean bClusterVisualizationGenesActive = false;
 
 	private boolean bClusterVisualizationExperimentsActive = false;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param glCanvas
 	 * @param sLabel
 	 * @param viewFrustum
 	 */
 	public GLHeatMap(GLCaleydoCanvas glCanvas, final String sLabel, final IViewFrustum viewFrustum) {
 
 		super(glCanvas, sLabel, viewFrustum);
 		viewType = EManagedObjectType.GL_HEAT_MAP;
 
 		// ArrayList<ESelectionType> alSelectionTypes = new ArrayList<ESelectionType>();
 		// alSelectionTypes.add(ESelectionType.NORMAL);
 		// alSelectionTypes.add(ESelectionType.MOUSE_OVER);
 		// alSelectionTypes.add(ESelectionType.SELECTION);
 
 		contentSelectionManager = new SelectionManager.Builder(EIDType.EXPRESSION_INDEX).build();
 		storageSelectionManager = new SelectionManager.Builder(EIDType.EXPERIMENT_INDEX).build();
 
 		colorMapper = ColorMappingManager.get().getColorMapping(EColorMappingType.GENE_EXPRESSION);
 
 		fAlXDistances = new ArrayList<Float>();
 
 		glKeyListener = new GLHeatMapKeyListener(this);
 	}
 
 	@Override
 	public void init(GL gl) {
 		renderStyle = new HeatMapRenderStyle(this, viewFrustum);
 		super.renderStyle = renderStyle;
 	}
 
 	@Override
 	public void initLocal(GL gl) {
 		bRenderStorageHorizontally = false;
 
 		// Register keyboard listener to GL canvas
 		GeneralManager.get().getGUIBridge().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				parentGLCanvas.getParentComposite().addKeyListener(glKeyListener);
 			}
 		});
 
 		iGLDisplayListIndexLocal = gl.glGenLists(1);
 		iGLDisplayListToCall = iGLDisplayListIndexLocal;
 		init(gl);
 	}
 
 	@Override
 	public void initRemote(final GL gl, final AGLEventListener glParentView,
 		final GLMouseListener glMouseListener, GLInfoAreaManager infoAreaManager) {
 
 		if (glRemoteRenderingView instanceof GLRemoteRendering)
 			renderStyle.disableFishEye();
 
 		// Register keyboard listener to GL canvas
 		glParentView.getParentGLCanvas().getParentComposite().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				glParentView.getParentGLCanvas().getParentComposite().addKeyListener(glKeyListener);
 			}
 		});
 
 		bRenderStorageHorizontally = false;
 
 		this.glMouseListener = glMouseListener;
 
 		iGLDisplayListIndexRemote = gl.glGenLists(1);
 		iGLDisplayListToCall = iGLDisplayListIndexRemote;
 		init(gl);
 	}
 
 	@Override
 	public void setDetailLevel(EDetailLevel detailLevel) {
 		if (bUseDetailLevel) {
 			super.setDetailLevel(detailLevel);
 		}
 		// renderStyle.setDetailLevel(detailLevel);
 		renderStyle.updateFieldSizes();
 	}
 
 	@Override
 	public void displayLocal(GL gl) {
 
 		if (set == null)
 			return;
 
 		if (bIsTranslationAnimationActive) {
 			doTranslation();
 		}
 
 		pickingManager.handlePicking(this, gl);
 
 		if (bIsDisplayListDirtyLocal) {
 			buildDisplayList(gl, iGLDisplayListIndexLocal);
 			bIsDisplayListDirtyLocal = false;
 		}
 		iGLDisplayListToCall = iGLDisplayListIndexLocal;
 
 		display(gl);
 		checkForHits(gl);
 
 		if (eBusyModeState != EBusyModeState.OFF) {
 			renderBusyMode(gl);
 		}
 	}
 
 	@Override
 	public void displayRemote(GL gl) {
 
 		if (set == null)
 			return;
 
 		if (bIsTranslationAnimationActive) {
 			bIsDisplayListDirtyRemote = true;
 			doTranslation();
 		}
 
 		if (bIsDisplayListDirtyRemote) {
 			buildDisplayList(gl, iGLDisplayListIndexRemote);
 			bIsDisplayListDirtyRemote = false;
 			// generalManager.getViewGLCanvasManager().getConnectedElementRepresentationManager().clearTransformedConnections();
 		}
 		iGLDisplayListToCall = iGLDisplayListIndexRemote;
 
 		display(gl);
 		checkForHits(gl);
 
 		// glMouseListener.resetEvents();
 	}
 
 	@Override
 	public void display(GL gl) {
 		processEvents();
 		// clipToFrustum(gl);
 
 		gl.glCallList(iGLDisplayListToCall);
 
 		// buildDisplayList(gl, iGLDisplayListIndexRemote);
 
 		if (!isRenderedRemote())
 			contextMenu.render(gl, this);
 	}
 
 	private void buildDisplayList(final GL gl, int iGLDisplayListIndex) {
 
 		if (bHasFrustumChanged) {
 			bHasFrustumChanged = false;
 		}
 		gl.glNewList(iGLDisplayListIndex, GL.GL_COMPILE);
 
 		if (contentSelectionManager.getNumberOfElements() == 0) {
 			renderSymbol(gl);
 		}
 		else {
 
 			float fSpacing = 0;
 			if (!bRenderStorageHorizontally) {
 				gl.glTranslatef(vecTranslation.x(), viewFrustum.getHeight() - fSpacing, vecTranslation.z());
 				gl.glRotatef(vecRotation.x(), vecRotation.y(), vecRotation.z(), vecRotation.w());
 			}
 
 			gl.glTranslatef(fAnimationTranslation, 0.0f, 0.0f);
 
 			renderHeatMap(gl);
 
 			renderSelection(gl, ESelectionType.MOUSE_OVER);
 			renderSelection(gl, ESelectionType.SELECTION);
 
 			gl.glTranslatef(-fAnimationTranslation, 0.0f, 0.0f);
 
 			if (!bRenderStorageHorizontally) {
 				gl.glRotatef(-vecRotation.x(), vecRotation.y(), vecRotation.z(), vecRotation.w());
 				gl
 					.glTranslatef(-vecTranslation.x(), -viewFrustum.getHeight() + fSpacing, -vecTranslation
 						.z());
 			}
 
 			// gl.glDisable(GL.GL_STENCIL_TEST);
 		}
 		gl.glEndList();
 	}
 
 	/**
 	 * Render the symbol of the view instead of the view
 	 * 
 	 * @param gl
 	 */
 	private void renderSymbol(GL gl) {
 		float fXButtonOrigin = 0.33f * renderStyle.getScaling();
 		float fYButtonOrigin = 0.33f * renderStyle.getScaling();
 		Texture tempTexture = textureManager.getIconTexture(gl, EIconTextures.HEAT_MAP_SYMBOL);
 		tempTexture.enable();
 		tempTexture.bind();
 
 		TextureCoords texCoords = tempTexture.getImageTexCoords();
 
 		gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);
 		gl.glColor4f(1f, 1, 1, 1f);
 		gl.glBegin(GL.GL_POLYGON);
 
 		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
 		gl.glVertex3f(fXButtonOrigin, fYButtonOrigin, 0.01f);
 		gl.glTexCoord2f(texCoords.left(), texCoords.top());
 		gl.glVertex3f(fXButtonOrigin, 2 * fYButtonOrigin, 0.01f);
 		gl.glTexCoord2f(texCoords.right(), texCoords.top());
 		gl.glVertex3f(fXButtonOrigin * 2, 2 * fYButtonOrigin, 0.01f);
 		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
 		gl.glVertex3f(fXButtonOrigin * 2, fYButtonOrigin, 0.01f);
 		gl.glEnd();
 		gl.glPopAttrib();
 		tempTexture.disable();
 	}
 
 	public void renderHorizontally(boolean bRenderStorageHorizontally) {
 
 		this.bRenderStorageHorizontally = bRenderStorageHorizontally;
 		// renderStyle.setBRenderStorageHorizontally(bRenderStorageHorizontally);
 		setDisplayListDirty();
 	}
 
 	@Override
 	protected void initLists() {
 		// todo this is not nice here, we may need a more intelligent way to determine which to use
 		if (contentVAType != EVAType.CONTENT_EMBEDDED_HM) {
 			if (bRenderOnlyContext)
 				contentVAType = EVAType.CONTENT_CONTEXT;
 			else
 				contentVAType = EVAType.CONTENT;
 		}
 
 		contentVA = useCase.getVA(contentVAType);
 		storageVA = useCase.getVA(storageVAType);
 
 		// contentSelectionManager.resetSelectionManager();
 		// storageSelectionManager.resetSelectionManager();
 
 		contentSelectionManager.setVA(contentVA);
 		storageSelectionManager.setVA(storageVA);
 
 		int iNumberOfColumns = contentVA.size();
 		int iNumberOfRows = storageVA.size();
 
 		for (int iRowCount = 0; iRowCount < iNumberOfRows; iRowCount++) {
 			storageSelectionManager.initialAdd(storageVA.get(iRowCount));
 
 		}
 
 		// this for loop executes one per axis
 		for (int iColumnCount = 0; iColumnCount < iNumberOfColumns; iColumnCount++) {
 			contentSelectionManager.initialAdd(contentVA.get(iColumnCount));
 		}
 
 		// FIXME: do we need to do this here?
 		renderStyle = new HeatMapRenderStyle(this, viewFrustum);
 		if (getRemoteRenderingGLCanvas() instanceof GLHierarchicalHeatMap)
 			renderStyle.disableFishEye();
 
 		vecTranslation = new Vec3f(0, renderStyle.getYCenter() * 2, 0);
 
 	}
 
 	@Override
 	public String getShortInfo() {
 		if (contentVA == null)
 			return "Heat Map - 0 " + useCase.getContentLabel(false, true) + " / 0 experiments";
 
 		return "Heat Map - " + contentVA.size() + " " + useCase.getContentLabel(false, true) + " / "
 			+ storageVA.size() + " experiments";
 	}
 
 	@Override
 	public String getDetailedInfo() {
 		StringBuffer sInfoText = new StringBuffer();
 		sInfoText.append("<b>Type:</b> Heat Map\n");
 
 		if (bRenderStorageHorizontally) {
 			sInfoText.append(contentVA.size() + " " + useCase.getContentLabel(false, true)
 				+ " in columns and " + storageVA.size() + " experiments in rows.\n");
 		}
 		else {
 			sInfoText.append(contentVA.size() + " " + useCase.getContentLabel(true, true) + " in rows and "
 				+ storageVA.size() + " experiments in columns.\n");
 		}
 
 		if (bRenderOnlyContext) {
 			sInfoText.append("Showing only " + " " + useCase.getContentLabel(false, true)
 				+ " which occur in one of the other views in focus\n");
 		}
 		else {
 			if (bUseRandomSampling) {
 				sInfoText.append("Random sampling active, sample size: " + iNumberOfRandomElements + "\n");
 			}
 			else {
 				sInfoText.append("Random sampling inactive\n");
 			}
 
 			if (dataFilterLevel == EDataFilterLevel.COMPLETE) {
 				sInfoText.append("Showing all genes in the dataset\n");
 			}
 			else if (dataFilterLevel == EDataFilterLevel.ONLY_MAPPING) {
 				sInfoText.append("Showing all genes that have a known DAVID ID mapping\n");
 			}
 			else if (dataFilterLevel == EDataFilterLevel.ONLY_CONTEXT) {
 				sInfoText
 					.append("Showing all genes that are contained in any of the KEGG or Biocarta pathways\n");
 			}
 		}
 
 		return sInfoText.toString();
 	}
 
 	@Override
 	protected void handlePickingEvents(EPickingType ePickingType, EPickingMode pickingMode, int iExternalID,
 		Pick pick) {
 		if (detailLevel == EDetailLevel.VERY_LOW) {
 			return;
 		}
 
 		ESelectionType eSelectionType;
 		switch (ePickingType) {
 			case HEAT_MAP_LINE_SELECTION:
 				iCurrentMouseOverElement = iExternalID;
 				switch (pickingMode) {
 
 					case CLICKED:
 						eSelectionType = ESelectionType.SELECTION;
 						break;
 					case MOUSE_OVER:
 
 						eSelectionType = ESelectionType.MOUSE_OVER;
 
 						break;
 					case RIGHT_CLICKED:
 						eSelectionType = ESelectionType.SELECTION;
 
 						// Prevent handling of non genetic data in context menu
 						if (generalManager.getUseCase(dataDomain).getDataDomain() != EDataDomain.GENETIC_DATA)
 							break;
 
 						if (!isRenderedRemote()) {
 							contextMenu.setLocation(pick.getPickedPoint(), getParentGLCanvas().getWidth(),
 								getParentGLCanvas().getHeight());
 							contextMenu.setMasterGLView(this);
 						}
 
 						GeneContextMenuItemContainer geneContextMenuItemContainer =
 							new GeneContextMenuItemContainer();
 						geneContextMenuItemContainer.setID(EIDType.EXPRESSION_INDEX, iExternalID);
 						contextMenu.addItemContanier(geneContextMenuItemContainer);
 					default:
 						return;
 
 				}
 
 				createContentSelection(eSelectionType, iExternalID);
 
 				break;
 
 			case HEAT_MAP_STORAGE_SELECTION:
 
 				switch (pickingMode) {
 					case CLICKED:
 						eSelectionType = ESelectionType.SELECTION;
 						break;
 					case MOUSE_OVER:
 						eSelectionType = ESelectionType.MOUSE_OVER;
 						break;
 					case RIGHT_CLICKED:
 						if (!isRenderedRemote()) {
 							contextMenu.setLocation(pick.getPickedPoint(), getParentGLCanvas().getWidth(),
 								getParentGLCanvas().getHeight());
 							contextMenu.setMasterGLView(this);
 						}
 						ExperimentContextMenuItemContainer experimentContextMenuItemContainer =
 							new ExperimentContextMenuItemContainer();
 						experimentContextMenuItemContainer.setID(iExternalID);
 						contextMenu.addItemContanier(experimentContextMenuItemContainer);
 					default:
 						return;
 				}
 
 				createStorageSelection(eSelectionType, iExternalID);
 
 				break;
 			// case LIST_HEAT_MAP_CLEAR_ALL:
 			// switch (pickingMode) {
 			// case CLICKED:
 			// contentSelectionManager.resetSelectionManager();
 			// setDisplayListDirty();
 			// SelectionCommand command = new SelectionCommand(ESelectionCommandType.RESET);
 			//
 			// TriggerPropagationCommandEvent event = new TriggerPropagationCommandEvent();
 			// event.setType(EIDType.EXPRESSION_INDEX);
 			// event.setSelectionCommand(command);
 			// event.setSender(this);
 			// eventPublisher.triggerEvent(event);
 			// break;
 			//
 			// }
 			// break;
 		}
 	}
 
 	private void createContentSelection(ESelectionType selectionType, int contentID) {
 		if (contentSelectionManager.checkStatus(selectionType, contentID))
 			return;
 
 		// check if the mouse-overed element is already selected, and if it is, whether mouse over is clear.
 		// If that all is true we don't need to do anything
 		if (selectionType == ESelectionType.MOUSE_OVER
 			&& contentSelectionManager.checkStatus(ESelectionType.SELECTION, contentID)
 			&& contentSelectionManager.getElements(ESelectionType.MOUSE_OVER).size() == 0)
 			return;
 
 		connectedElementRepresentationManager.clear(EIDType.EXPRESSION_INDEX);
 
 		contentSelectionManager.clearSelection(selectionType);
 		SelectionCommand command = new SelectionCommand(ESelectionCommandType.CLEAR, selectionType);
 		sendSelectionCommandEvent(EIDType.EXPRESSION_INDEX, command);
 
 		// TODO: Integrate multi spotting support again
 		// // Resolve multiple spotting on chip and add all to the
 		// // selection manager.
 		// Integer iRefSeqID =
 		// idMappingManager.getID(EMappingType.EXPRESSION_INDEX_2_REFSEQ_MRNA_INT, iExternalID);
 		//
 		Integer iMappingID = generalManager.getIDManager().createID(EManagedObjectType.CONNECTION);
 		// for (Object iExpressionIndex : idMappingManager.getMultiID(
 		// EMappingType.REFSEQ_MRNA_INT_2_EXPRESSION_INDEX, iRefSeqID)) {
 		// contentSelectionManager.addToType(eSelectionType, (Integer) iExpressionIndex);
 		// contentSelectionManager.addConnectionID(iMappingID, (Integer) iExpressionIndex);
 		// }
 		contentSelectionManager.addToType(selectionType, contentID);
 		contentSelectionManager.addConnectionID(iMappingID, contentID);
 
 		if (eFieldDataType == EIDType.EXPRESSION_INDEX) {
 			SelectionDelta selectionDelta = contentSelectionManager.getDelta();
 
 			// SelectionCommand command = new SelectionCommand(ESelectionCommandType.CLEAR,
 			// eSelectionType);
 			// sendSelectionCommandEvent(EIDType.REFSEQ_MRNA_INT, command);
 
 			handleConnectedElementRep(selectionDelta);
 			SelectionUpdateEvent event = new SelectionUpdateEvent();
 			event.setSender(this);
 			event.setSelectionDelta(selectionDelta);
 			event.setInfo(getShortInfo());
 			eventPublisher.triggerEvent(event);
 		}
 
 		setDisplayListDirty();
 	}
 
 	private void createStorageSelection(ESelectionType selectionType, int storageID) {
 		if (storageSelectionManager.checkStatus(selectionType, storageID))
 			return;
 
 		// check if the mouse-overed element is already selected, and if it is, whether mouse over is clear.
 		// If that all is true we don't need to do anything
 		if (selectionType == ESelectionType.MOUSE_OVER
 			&& storageSelectionManager.checkStatus(ESelectionType.SELECTION, storageID)
 			&& storageSelectionManager.getElements(ESelectionType.MOUSE_OVER).size() == 0)
 			return;
 
 		storageSelectionManager.clearSelection(selectionType);
 		storageSelectionManager.addToType(selectionType, storageID);
 
 		if (eStorageDataType == EIDType.EXPERIMENT_INDEX) {
 
 			// SelectionCommand command = new SelectionCommand(ESelectionCommandType.CLEAR,
 			// eSelectionType);
 			// sendSelectionCommandEvent(EIDType.EXPERIMENT_INDEX, command);
 
 			SelectionDelta selectionDelta = storageSelectionManager.getDelta();
 			SelectionUpdateEvent event = new SelectionUpdateEvent();
 			event.setSender(this);
 			event.setSelectionDelta(selectionDelta);
 			eventPublisher.triggerEvent(event);
 		}
 		setDisplayListDirty();
 	}
 
 	public void upDownSelect(boolean isUp) {
 		IVirtualArray virtualArray = contentVA;
 		if (virtualArray == null)
 			throw new IllegalStateException("Virtual Array is required for selectNext Operation");
 		int selectedElement = cursorSelect(virtualArray, contentSelectionManager, isUp);
 		if (selectedElement < 0)
 			return;
 		createContentSelection(ESelectionType.MOUSE_OVER, selectedElement);
 	}
 
 	public void leftRightSelect(boolean isLeft) {
 		IVirtualArray virtualArray = storageVA;
 		if (virtualArray == null)
 			throw new IllegalStateException("Virtual Array is required for selectNext Operation");
 		int selectedElement = cursorSelect(virtualArray, storageSelectionManager, isLeft);
 		if (selectedElement < 0)
 			return;
 		createStorageSelection(ESelectionType.MOUSE_OVER, selectedElement);
 	}
 
 	private int cursorSelect(IVirtualArray virtualArray, SelectionManager selectionManager, boolean isUp) {
 
 		Set<Integer> elements = selectionManager.getElements(ESelectionType.MOUSE_OVER);
 		if (elements.size() == 0) {
 			elements = selectionManager.getElements(ESelectionType.SELECTION);
 			if (elements.size() == 0)
 				return -1;
 		}
 
 		if (elements.size() == 1) {
 			Integer element = elements.iterator().next();
 			int index = virtualArray.indexOf(element);
 			int newIndex;
 			if (isUp) {
 				newIndex = index - 1;
 				if (newIndex < 0)
 					return -1;
 			}
 			else {
 				newIndex = index + 1;
 				if (newIndex == virtualArray.size())
 					return -1;
 
 			}
 			return virtualArray.get(newIndex);
 
 		}
 		return -1;
 	}
 
 	private void renderHeatMap(final GL gl) {
 		fAlXDistances.clear();
 		renderStyle.updateFieldSizes();
 		float fXPosition = 0;
 		float fYPosition = 0;
 		float fFieldWidth = 0;
 		float fFieldHeight = 0;
 		// renderStyle.clearFieldWidths();
 		// GLHelperFunctions.drawPointAt(gl, new Vec3f(1,0.2f,0));
 		int iCount = 0;
 		ESelectionType currentType;
 		for (Integer iContentIndex : contentVA) {
 			iCount++;
 			// we treat normal and deselected the same atm
 			if (contentSelectionManager.checkStatus(ESelectionType.NORMAL, iContentIndex)
 				|| contentSelectionManager.checkStatus(ESelectionType.DESELECTED, iContentIndex)) {
 				fFieldWidth = renderStyle.getNormalFieldWidth();
 				fFieldHeight = renderStyle.getFieldHeight();
 				currentType = ESelectionType.NORMAL;
 
 			}
 			else if (contentSelectionManager.checkStatus(ESelectionType.SELECTION, iContentIndex)
 				|| contentSelectionManager.checkStatus(ESelectionType.MOUSE_OVER, iContentIndex)) {
 				fFieldWidth = renderStyle.getSelectedFieldWidth();
 				fFieldHeight = renderStyle.getFieldHeight();
 				currentType = ESelectionType.SELECTION;
 			}
 			else {
 				continue;
 			}
 
 			fYPosition = 0;
 
 			for (Integer iStorageIndex : storageVA) {
 
 				renderElement(gl, iStorageIndex, iContentIndex, fXPosition, fYPosition, fFieldWidth,
 					fFieldHeight);
 
 				fYPosition += fFieldHeight;
 
 			}
 
 			float fFontScaling = 0;
 
 			float fColumnDegrees = 0;
 			float fLineDegrees = 0;
 			if (bRenderStorageHorizontally) {
 				fColumnDegrees = 0;
 				fLineDegrees = 25;
 			}
 			else {
 				fColumnDegrees = 60;
 				fLineDegrees = 90;
 			}
 
 			// render line captions
 			if (fFieldWidth > 0.055f) {
 				boolean bRenderRefSeq = false;
 				// if (fFieldWidth < 0.2f)
 				// {
 				fFontScaling = renderStyle.getSmallFontScalingFactor();
 				// }
 				// else
 				// {
 				// bRenderRefSeq = true;
 				// fFontScaling = renderStyle.getHeadingFontScalingFactor();
 				// }
 
 				if (detailLevel == EDetailLevel.HIGH) {
 					// bRenderRefSeq = true;
 					String sContent = null;
 					String refSeq = null;
 
 					if (set.getSetType() == ESetType.GENE_EXPRESSION_DATA) {
 
 						// FIXME: Due to new mapping system, a mapping involving expression index can return a
 						// Set of
 						// values, depending on the IDType that has been specified when loading expression
 						// data.
 						// Possibly a different handling of the Set is required.
 						Set<String> setGeneSymbols =
 							idMappingManager.getIDAsSet(EIDType.EXPRESSION_INDEX, EIDType.GENE_SYMBOL,
 								iContentIndex);
 
 						if ((setGeneSymbols != null && !setGeneSymbols.isEmpty())) {
 							sContent = (String) setGeneSymbols.toArray()[0];
 						}
 
 						if (sContent == null || sContent.equals(""))
 							sContent = "Unkonwn Gene";
 
 						// FIXME: Due to new mapping system, a mapping involving expression index can return a
 						// Set of
 						// values, depending on the IDType that has been specified when loading expression
 						// data.
 						// Possibly a different handling of the Set is required.
 						Set<String> setRefSeqIDs =
 							idMappingManager.getIDAsSet(EIDType.EXPRESSION_INDEX, EIDType.REFSEQ_MRNA,
 								iContentIndex);
 
 						if ((setRefSeqIDs != null && !setRefSeqIDs.isEmpty())) {
 							refSeq = (String) setRefSeqIDs.toArray()[0];
 						}
 						// GeneticIDMappingHelper.get().getRefSeqStringFromStorageIndex(iContentIndex);
 
 						if (bRenderRefSeq) {
 							sContent += " | ";
 							// Render heat map element name
 							sContent += refSeq;
 						}
 					}
 					else if (set.getSetType() == ESetType.UNSPECIFIED) {
 						sContent =
 							generalManager.getIDMappingManager().getID(EIDType.EXPRESSION_INDEX,
 								EIDType.UNSPECIFIED, iContentIndex);
 					}
 					else {
 						throw new IllegalStateException("Label extraction for " + set.getSetType()
 							+ " not implemented yet!");
 					}
 
 					if (sContent == null)
 						sContent = "Unknown";
 
 					textRenderer.setColor(0, 0, 0, 1);
 
 					if (bClusterVisualizationGenesActive)
 						gl.glTranslatef(0, renderStyle.getWidthClusterVisualization(), 0);
 
 					if (currentType == ESelectionType.SELECTION || currentType == ESelectionType.MOUSE_OVER) {
 						renderCaption(gl, sContent, fXPosition + fFieldWidth / 6 * 2.5f, fYPosition + 0.05f,
 							0, fLineDegrees, fFontScaling);
 						if (refSeq != null)
 							renderCaption(gl, refSeq, fXPosition + fFieldWidth / 6 * 4.5f,
 								fYPosition + 0.05f, 0, fLineDegrees, fFontScaling);
 					}
 					else {
 						renderCaption(gl, sContent, fXPosition + fFieldWidth / 6 * 4.5f, fYPosition + 0.05f,
 							0, fLineDegrees, fFontScaling);
 					}
 
 					if (bClusterVisualizationGenesActive)
 						gl.glTranslatef(0, -renderStyle.getWidthClusterVisualization(), 0);
 				}
 
 			}
 			// renderStyle.setXDistanceAt(contentVA.indexOf(iContentIndex),
 			// fXPosition);
 			fAlXDistances.add(fXPosition);
 			fXPosition += fFieldWidth;
 
 			// render column captions
 			if (detailLevel == EDetailLevel.HIGH) {
 				if (iCount == contentVA.size()) {
 					fYPosition = 0;
 
 					if (bClusterVisualizationExperimentsActive)
 						gl.glTranslatef(+renderStyle.getWidthClusterVisualization(), 0, 0);
 
 					for (Integer iStorageIndex : storageVA) {
 						textRenderer.setColor(0, 0, 0, 1);
 						renderCaption(gl, set.get(iStorageIndex).getLabel(), fXPosition + 0.05f, fYPosition
 							+ fFieldHeight / 2, 0, fColumnDegrees, renderStyle.getSmallFontScalingFactor());
 						fYPosition += fFieldHeight;
 					}
 
 					if (bClusterVisualizationExperimentsActive)
 						gl.glTranslatef(-renderStyle.getWidthClusterVisualization(), 0, 0);
 				}
 			}
 		}
 	}
 
 	// public void selectElements() {
 	// ISelectionDelta delta = contentSelectionManager.selectNext(ESelectionType.MOUSE_OVER);
 	// if (delta == null)
 	// return;
 	// SelectionUpdateEvent event = new SelectionUpdateEvent();
 	// event.setSelectionDelta(delta);
 	// event.setSender(this);
 	// eventPublisher.triggerEvent(event);
 	// setDisplayListDirty();
 	// }
 
 	// @Override
 	// public void clear()
 	// {
 	// contentSelectionManager.clearSelections();
 	// storageSelectionManager.clearSelections();
 	// setDisplayListDirty();
 	// }
 
 	private void renderElement(final GL gl, final int iStorageIndex, final int iContentIndex,
 		final float fXPosition, final float fYPosition, final float fFieldWidth, final float fFieldHeight) {
 
 		float fLookupValue = set.get(iStorageIndex).getFloat(EDataRepresentation.NORMALIZED, iContentIndex);
 
 		float fOpacity = 0;
 		if (contentSelectionManager.checkStatus(ESelectionType.DESELECTED, iContentIndex)) {
 			fOpacity = 0.3f;
 		}
 		else {
 			fOpacity = 1.0f;
 		}
 
 		float[] fArMappingColor = colorMapper.getColor(fLookupValue);
 
 		gl.glColor4f(fArMappingColor[0], fArMappingColor[1], fArMappingColor[2], fOpacity);
 
 		gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.HEAT_MAP_STORAGE_SELECTION,
 			iStorageIndex));
 		gl.glPushName(pickingManager.getPickingID(iUniqueID, EPickingType.HEAT_MAP_LINE_SELECTION,
 			iContentIndex));
 		gl.glBegin(GL.GL_POLYGON);
 		gl.glVertex3f(fXPosition, fYPosition, FIELD_Z);
 		gl.glVertex3f(fXPosition + fFieldWidth, fYPosition, FIELD_Z);
 		gl.glVertex3f(fXPosition + fFieldWidth, fYPosition + fFieldHeight, FIELD_Z);
 		gl.glVertex3f(fXPosition, fYPosition + fFieldHeight, FIELD_Z);
 		gl.glEnd();
 
 		gl.glPopName();
 		gl.glPopName();
 	}
 
 	private void renderSelection(final GL gl, ESelectionType eSelectionType) {
 		// content selection
 
 		Set<Integer> selectedSet = contentSelectionManager.getElements(eSelectionType);
 		float fHeight = 0;
 		float fXPosition = 0;
 		float fYPosition = 0;
 
 		switch (eSelectionType) {
 			case SELECTION:
 				gl.glColor4fv(SELECTED_COLOR, 0);
 				gl.glLineWidth(SELECTED_LINE_WIDTH);
 				break;
 			case MOUSE_OVER:
 				gl.glColor4fv(MOUSE_OVER_COLOR, 0);
 				gl.glLineWidth(MOUSE_OVER_LINE_WIDTH);
 				break;
 		}
 
 		int iColumnIndex = 0;
 		for (int iTempColumn : contentVA) {
 			for (Integer iCurrentColumn : selectedSet) {
 
 				if (iCurrentColumn == iTempColumn) {
 					fHeight = storageVA.size() * renderStyle.getFieldHeight();
 					fXPosition = fAlXDistances.get(iColumnIndex);
 
 					fYPosition = 0;
 
 					gl.glBegin(GL.GL_LINE_LOOP);
 					gl.glVertex3f(fXPosition, fYPosition, SELECTION_Z);
 					gl.glVertex3f(fXPosition + renderStyle.getSelectedFieldWidth(), fYPosition, SELECTION_Z);
 					gl.glVertex3f(fXPosition + renderStyle.getSelectedFieldWidth(), fYPosition + fHeight,
 						SELECTION_Z);
 					gl.glVertex3f(fXPosition, fYPosition + fHeight, SELECTION_Z);
 					gl.glEnd();
 
 					fHeight = 0;
 					fXPosition = 0;
 				}
 			}
 			iColumnIndex++;
 		}
 
 		// storage selection
 
 		gl.glEnable(GL.GL_LINE_STIPPLE);
 		gl.glLineStipple(2, (short) 0xAAAA);
 
 		selectedSet = storageSelectionManager.getElements(eSelectionType);
 		int iLineIndex = 0;
 		for (int iTempLine : storageVA) {
 			for (Integer iCurrentLine : selectedSet) {
 				if (iTempLine == iCurrentLine) {
 					// TODO we need indices of all elements
 
 					fYPosition = iLineIndex * renderStyle.getFieldHeight();
 					gl.glBegin(GL.GL_LINE_LOOP);
 					gl.glVertex3f(0, fYPosition, SELECTION_Z);
 					gl.glVertex3f(renderStyle.getRenderHeight(), fYPosition, SELECTION_Z);
 					gl.glVertex3f(renderStyle.getRenderHeight(), fYPosition + renderStyle.getFieldHeight(),
 						SELECTION_Z);
 					gl.glVertex3f(0, fYPosition + renderStyle.getFieldHeight(), SELECTION_Z);
 					gl.glEnd();
 				}
 			}
 			iLineIndex++;
 		}
 
 		gl.glDisable(GL.GL_LINE_STIPPLE);
 	}
 
 	@Override
 	protected void handleConnectedElementRep(ISelectionDelta selectionDelta) {
 		// FIXME: should not be necessary here, incor init.
 		if (renderStyle == null)
 			return;
 
 		renderStyle.updateFieldSizes();
 		fAlXDistances.clear();
 		float fDistance = 0;
 
 		for (Integer iStorageIndex : contentVA) {
 			fAlXDistances.add(fDistance);
 			if (contentSelectionManager.checkStatus(ESelectionType.MOUSE_OVER, iStorageIndex)
 				|| contentSelectionManager.checkStatus(ESelectionType.SELECTION, iStorageIndex)) {
 				fDistance += renderStyle.getSelectedFieldWidth();
 			}
 			else {
 				fDistance += renderStyle.getNormalFieldWidth();
 			}
 
 		}
 		super.handleConnectedElementRep(selectionDelta);
 	}
 
 	@Override
 	protected ArrayList<SelectedElementRep> createElementRep(EIDType idType, int iStorageIndex)
 		throws InvalidAttributeValueException {
 
 		SelectedElementRep elementRep;
 		ArrayList<SelectedElementRep> alElementReps = new ArrayList<SelectedElementRep>(4);
 
 		for (int iContentIndex : contentVA.indicesOf(iStorageIndex)) {
 			if (iContentIndex == -1) {
 				// throw new
 				// IllegalStateException("No such element in virtual array");
 				// TODO this shouldn't happen here.
 				continue;
 			}
 
 			float fXValue = fAlXDistances.get(iContentIndex); // + renderStyle.getSelectedFieldWidth() / 2;
 			// float fYValue = 0;
 			float fYValue = renderStyle.getYCenter();
 
 			// Set<Integer> mouseOver = storageSelectionManager.getElements(ESelectionType.MOUSE_OVER);
 			// for (int iLineIndex : mouseOver)
 			// {
 			// fYValue = storageVA.indexOf(iLineIndex) * renderStyle.getFieldHeight() +
 			// renderStyle.getFieldHeight()/2;
 			// break;
 			// }
 
 			int iViewID = iUniqueID;
 			// If rendered remote (hierarchical heat map) - use the remote view ID
			if (glRemoteRenderingView != null && glRemoteRenderingView instanceof GLHierarchicalHeatMap)
 				iViewID = glRemoteRenderingView.getID();
 
 			if (bRenderStorageHorizontally) {
 				elementRep =
 					new SelectedElementRep(EIDType.EXPRESSION_INDEX, iViewID,
 						fXValue + fAnimationTranslation, fYValue, 0);
 
 			}
 			else {
 				Rotf myRotf = new Rotf(new Vec3f(0, 0, 1), -(float) Math.PI / 2);
 				Vec3f vecPoint = myRotf.rotateVector(new Vec3f(fXValue, fYValue, 0));
 				vecPoint.setY(vecPoint.y() + vecTranslation.y());
 				elementRep =
 					new SelectedElementRep(EIDType.EXPRESSION_INDEX, iViewID, vecPoint.x(), vecPoint.y()
 						- fAnimationTranslation, 0);
 
 			}
 			alElementReps.add(elementRep);
 		}
 		return alElementReps;
 	}
 
 	/**
 	 * Re-position a view centered on a element, specified by the element ID
 	 * 
 	 * @param iElementID
 	 *            the ID of the element that should be in the center
 	 */
 	protected void rePosition(int iElementID) {
 
 		// int iSelection;
 		// if (bRenderStorageHorizontally) {
 		// iSelection = iContentVAID;
 		// }
 		// else {
 		// iSelection = iStorageVAID;
 		// // TODO test this
 		// }
 
 		// float fCurrentPosition =
 		// set.getVA(iSelection).indexOf(iElementID) * renderStyle.getNormalFieldWidth();// +
 		// // renderStyle.getXSpacing(
 		// // );
 		//
 		// float fFrustumLength = viewFrustum.getRight() - viewFrustum.getLeft();
 		// float fLength = (set.getVA(iSelection).size() - 1) * renderStyle.getNormalFieldWidth() + 1.5f; //
 		// MARC
 		// // :
 		// // 1.5
 		// // =
 		// // corion of
 		// // lens effect in
 		// // heatmap
 		//
 		// fAnimationTargetTranslation = -(fCurrentPosition - fFrustumLength / 2);
 		//
 		// if (-fAnimationTargetTranslation > fLength - fFrustumLength) {
 		// fAnimationTargetTranslation = -(fLength - fFrustumLength + 2 * 0.00f);
 		// }
 		// else if (fAnimationTargetTranslation > 0) {
 		// fAnimationTargetTranslation = 0;
 		// }
 		// else if (-fAnimationTargetTranslation < -fAnimationTranslation + fFrustumLength / 2 - 0.00f
 		// && -fAnimationTargetTranslation > -fAnimationTranslation - fFrustumLength / 2 + 0.00f) {
 		// fAnimationTargetTranslation = fAnimationTranslation;
 		// return;
 		// }
 		//
 		// bIsTranslationAnimationActive = true;
 	}
 
 	private void doTranslation() {
 
 		float fDelta = 0;
 		if (fAnimationTargetTranslation < fAnimationTranslation - 0.5f) {
 
 			fDelta = -0.5f;
 
 		}
 		else if (fAnimationTargetTranslation > fAnimationTranslation + 0.5f) {
 			fDelta = 0.5f;
 		}
 		else {
 			fDelta = fAnimationTargetTranslation - fAnimationTranslation;
 			bIsTranslationAnimationActive = false;
 		}
 
 		if (elementRep != null) {
 			ArrayList<Vec3f> alPoints = elementRep.getPoints();
 			for (Vec3f currentPoint : alPoints) {
 				currentPoint.setY(currentPoint.y() - fDelta);
 			}
 		}
 
 		fAnimationTranslation += fDelta;
 	}
 
 	@Override
 	public void renderContext(boolean bRenderOnlyContext) {
 
 		this.bRenderOnlyContext = bRenderOnlyContext;
 
 		if (this.bRenderOnlyContext) {
 			contentVA = useCase.getVA(EVAType.CONTENT_CONTEXT);
 		}
 		else {
 			contentVA = useCase.getVA(EVAType.CONTENT);
 		}
 
 		contentSelectionManager.setVA(contentVA);
 		// renderStyle.setActiveVirtualArray(iContentVAID);
 
 		setDisplayListDirty();
 
 	}
 
 	/*
 	 * *
 	 * @deprecated Use {@link #renderCaption(GL,String,float,float,float,float,float)} instead
 	 */
 	// private void renderCaption(GL gl, String sLabel, float fXOrigin, float
 	// fYOrigin,
 	// float fRotation, float fFontScaling)
 	// {
 	// renderCaption(gl, sLabel, fXOrigin, fYOrigin, 0, fRotation,
 	// fFontScaling);
 	// }
 	private void renderCaption(GL gl, String sLabel, float fXOrigin, float fYOrigin, float fZOrigin,
 		float fRotation, float fFontScaling) {
 		if (isRenderedRemote() && glRemoteRenderingView instanceof GLRemoteRendering)
 			fFontScaling *= 1.5;
 		if (sLabel.length() > GeneralRenderStyle.NUM_CHAR_LIMIT + 1) {
 			sLabel = sLabel.substring(0, GeneralRenderStyle.NUM_CHAR_LIMIT - 2);
 			sLabel = sLabel + "..";
 		}
 
 		// textRenderer.setColor(0, 0, 0, 1);
 		gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);
 		gl.glTranslatef(fXOrigin, fYOrigin, fZOrigin);
 		gl.glRotatef(fRotation, 0, 0, 1);
 		textRenderer.begin3DRendering();
 		textRenderer.draw3D(gl, sLabel, 0, 0, 0, fFontScaling, HeatMapRenderStyle.LABEL_TEXT_MIN_SIZE);
 		textRenderer.end3DRendering();
 		gl.glRotatef(-fRotation, 0, 0, 1);
 		gl.glTranslatef(-fXOrigin, -fYOrigin, -fZOrigin);
 		// textRenderer.begin3DRendering();
 		gl.glPopAttrib();
 	}
 
 	// @Override
 	// public void broadcastElements() {
 	// ISelectionDelta delta = contentSelectionManager.getCompleteDelta();
 	//
 	// SelectionUpdateEvent event = new SelectionUpdateEvent();
 	// event.setSender(this);
 	// event.setSelectionDelta(delta);
 	// event.setInfo(getShortInfo());
 	// eventPublisher.triggerEvent(event);
 	//
 	// setDisplayListDirty();
 	// }
 
 	@Override
 	public void handleVirtualArrayUpdate(IVirtualArrayDelta delta, String info) {
 
 		super.handleVirtualArrayUpdate(delta, info);
 
 		if (delta.getVAType() == EVAType.CONTENT_CONTEXT && contentVAType == EVAType.CONTENT_CONTEXT) {
 			if (contentVA.size() == 0)
 				return;
 			// FIXME: this is only proof of concept - use the cluster manager instead of affinity directly
 			// long original = System.currentTimeMillis();
 			// System.out.println("beginning clustering");
 			AffinityClusterer clusterer = new AffinityClusterer(contentVA.size());
 			ClusterState state =
 				new ClusterState(EClustererAlgo.AFFINITY_PROPAGATION, EClustererType.GENE_CLUSTERING,
 					EDistanceMeasure.EUCLIDEAN_DISTANCE);
 			int contentVAID = contentVA.getID();
 			state.setContentVaId(contentVA.getID());
 			state.setStorageVaId(storageVA.getID());
 			state.setAffinityPropClusterFactorGenes(4.0f);
 			IVirtualArray tempVA = clusterer.getSortedVA(set, state, 0, 2);
 
 			contentVA = tempVA;
 			contentSelectionManager.setVA(contentVA);
 			contentVA.setID(contentVAID);
 			// long result = System.currentTimeMillis() - original;
 			// System.out.println("Clustering took in ms: " + result);
 
 		}
 	}
 
 	@Override
 	public void changeOrientation(boolean defaultOrientation) {
 		renderHorizontally(defaultOrientation);
 	}
 
 	@Override
 	public boolean isInDefaultOrientation() {
 		return bRenderStorageHorizontally;
 	}
 
 	@Override
 	public ASerializedView getSerializableRepresentation() {
 		SerializedHeatMapView serializedForm = new SerializedHeatMapView(dataDomain);
 		serializedForm.setViewID(this.getID());
 		return serializedForm;
 	}
 
 	@Override
 	public void handleUpdateView() {
 		setDisplayListDirty();
 	}
 
 	public void setClusterVisualizationGenesActiveFlag(boolean bClusterVisualizationActive) {
 		this.bClusterVisualizationGenesActive = bClusterVisualizationActive;
 	}
 
 	public void setClusterVisualizationExperimentsActiveFlag(boolean bClusterVisualizationExperimentsActive) {
 		this.bClusterVisualizationExperimentsActive = bClusterVisualizationExperimentsActive;
 	}
 
 	@Override
 	public String toString() {
 		return "Standalone heat map, rendered remote: " + isRenderedRemote() + ", contentSize: "
 			+ contentVA.size() + ", storageSize: " + storageVA.size() + ", contentVAType: " + contentVAType
 			+ ", remoteRenderer:" + getRemoteRenderingGLCanvas();
 	}
 
 	@Override
 	public RemoteLevelElement getRemoteLevelElement() {
 
 		// If the view is rendered remote - the remote level element from the parent is returned
		if (glRemoteRenderingView != null && glRemoteRenderingView instanceof GLHierarchicalHeatMap)
 			return ((AGLEventListener) glRemoteRenderingView).getRemoteLevelElement();
 
 		return super.getRemoteLevelElement();
 	}
 
 }
