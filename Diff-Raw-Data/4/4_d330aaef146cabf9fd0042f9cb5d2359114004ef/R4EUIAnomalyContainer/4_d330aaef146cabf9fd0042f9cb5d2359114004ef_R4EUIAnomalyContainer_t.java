 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the Anomaly Container element of the UI model
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.model;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.frame.core.model.Location;
 import org.eclipse.mylyn.reviews.frame.core.model.Topic;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomaly;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomalyState;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomalyTextPosition;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EContent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EDecision;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFormalReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.RModelFactory;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.RFSRegistryFactory;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.ReviewsFileStorageException;
 import org.eclipse.mylyn.reviews.r4e.core.utils.ResourceUtils;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewVersionsException;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewsVersionsIF;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewsVersionsIF.FileVersionInfo;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewsVersionsIFFactory;
 import org.eclipse.mylyn.reviews.r4e.ui.Activator;
 import org.eclipse.mylyn.reviews.r4e.ui.dialogs.AnomalyInputDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.navigator.ReviewNavigatorContentProvider;
 import org.eclipse.mylyn.reviews.r4e.ui.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.R4EUIConstants;
 
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EUIAnomalyContainer extends R4EUIModelElement {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Field fAnomalyContainerFile.
 	 * (value is ""icons/obj16/anmlycont_obj.gif"")
 	 */
 	private static final String ANOMALY_CONTAINER_ICON_FILE = "icons/obj16/anmlycont_obj.gif";
 	  
 	/**
 	 * Field ADD_ELEMENT_ACTION_NAME.
 	 * (value is ""Add Anomaly"")
 	 */
 	private static final String ADD_CHILD_ELEMENT_COMMAND_NAME = "Add Anomaly";
 	
     /**
      * Field ADD_ELEMENT_ACTION_TOOLTIP.
      * (value is ""Add a new global anomaly to the current review item"")
      */
     private static final String ADD_CHILD_ELEMENT_COMMAND_TOOLTIP = "Add a New Global Anomaly to the Current Review Item";
 	
 	
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
     
 	/**
 	 * Field fAnomalies.
 	 */
 	private final List<R4EUIAnomalyBasic> fAnomalies;
 	
 	
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Constructor for AnomalyContainerElement.
 	 * @param aParent IR4EUIModelElement
 	 * @param aName String
 	 */
 	public R4EUIAnomalyContainer(IR4EUIModelElement aParent, String aName) {
 		super(aParent, aName, null);
 		fAnomalies = new ArrayList<R4EUIAnomalyBasic>();
 		setImage(ANOMALY_CONTAINER_ICON_FILE);
 	}
 
 	
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	
 	//Attributes
 	
 	/**
 	 * Create a serialization model element object
 	 * @return the new serialization element object
 	 */
 	@Override
 	public R4EReviewComponent createChildModelDataElement() {
 		//Get comment from user and set it in model data
 		R4EAnomaly tempAnomaly = null;
 		R4EUIModelController.setDialogOpen(true);
 		final AnomalyInputDialog dialog = new AnomalyInputDialog(R4EUIModelController.getNavigatorView(). // $codepro.audit.disable methodChainLength
 				getSite().getWorkbenchWindow().getShell());
     	final int result = dialog.open();
     	if (result == Window.OK) {
     		tempAnomaly = RModelFactory.eINSTANCE.createR4EAnomaly();
     		tempAnomaly.setTitle(dialog.getAnomalyTitleValue());
     		tempAnomaly.setDescription(dialog.getAnomalyDescriptionValue());
     	}
     	// else Window.CANCEL
 		R4EUIModelController.setDialogOpen(false);
     	return tempAnomaly;
 	}
 	
 	
 	//Hierarchy
 	
 	/**
 	 * Method getChildren.
 	 * @return IR4EUIModelElement[]
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#getChildren()
 	 */
 	@Override
 	public IR4EUIModelElement[] getChildren() { // $codepro.audit.disable
 		return fAnomalies.toArray(new R4EUIAnomalyBasic[fAnomalies.size()]);
 	}
 	
 	/**
 	 * Method hasChildren.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#hasChildren()
 	 */
 	@Override
 	public boolean hasChildren() {
 		if (fAnomalies.size() > 0) return true;
 	    return false;
 	}
 	
 	/**
 	 * Close the model element (i.e. disable it)
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#close()
 	 */
 	@Override
 	public void close() {
 		//Remove all children references
 		R4EUIAnomalyBasic anomaly = null;
 		final int anomaliesSize = fAnomalies.size();
 		for (int i = 0; i < anomaliesSize; i++) {
 			
 			anomaly = fAnomalies.get(i);
 			anomaly.close();
 			//fireRemove(anomaly);
 		}
 		fAnomalies.clear();
 		fOpen = false;
 		removeListener();
 	}
 	
 	/** // $codepro.audit.disable blockDepth
 	 * Method loadModelData.
 	 * 		Load the serialization model data into UI model
 	 */
 	@Override
 	public void open() {
 
 		R4EUIAnomalyBasic uiAnomaly = null;
 		final IR4EUIModelElement parentElement = getParent();
 		if (parentElement instanceof R4EUIFileContext) {
 			
 			//get anomalies that are specific to a file
 			final List<R4EAnomaly> anomalies = ((R4EUIFileContext)parentElement).getAnomalies();
 			R4EUITextPosition position = null;
 			final int anomaliesSize = anomalies.size();
 			R4EAnomaly anomaly = null;
 			for (int i = 0; i < anomaliesSize; i++) {
 				anomaly = anomalies.get(i);
 				if (anomaly.isEnabled() || Activator.getDefault().getPreferenceStore().
 						getBoolean(PreferenceConstants.P_SHOW_DISABLED)) {
 					//Do not set position for global EList<E>lies
 					position = null;
 					EList<Location> locations = anomalies.get(i).getLocation(); // $codepro.audit.disable variableDeclaredInLoop
 					if (null != locations) {
 						if (null != locations.get(0)) {
 							int locationsSize = locations.size(); // $codepro.audit.disable variableDeclaredInLoop
 							for (int j = 0; j < locationsSize; j++) {
 								position = new R4EUITextPosition(
 										((R4EContent)anomalies.get(i).getLocation().get(j)).getLocation());  // $codepro.audit.disable methodChainLength
 					    		if (((R4EUIReviewBasic)getParent().getParent().getParent()).getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 					    			uiAnomaly = new R4EUIAnomalyBasic(this, anomalies.get(i), position);
 					    		} else {
 					    			uiAnomaly = new R4EUIAnomalyExtended(this, anomalies.get(i), position);
 					    			uiAnomaly.setName(R4EUIAnomalyExtended.getStateString(anomalies.get(i).getState()) + ": " + uiAnomaly.getName());
 					    		}
 								
 								addChildren(uiAnomaly);
 								uiAnomaly.open();
 							}
 						} else {
 							uiAnomaly = new R4EUIAnomalyBasic(this, anomalies.get(i), null);
 							addChildren(uiAnomaly);
 							uiAnomaly.open();
 						}
 					}
 				}
 			}
 		} else if (parentElement instanceof R4EUIReviewBasic) {
 			
 			//Get anomalies that do not have any location.  These are global anomalies
 			
 			final EList<Topic> anomalies =((R4EUIReviewBasic)parentElement).getReview().getTopics();
 			if (null != anomalies) {
 				final int anomaliesSize = anomalies.size();
 				R4EAnomaly anomaly = null;
 				for (int i = 0; i < anomaliesSize; i++) {
 					anomaly = (R4EAnomaly) anomalies.get(i);
 					if (anomaly.isEnabled() || Activator.getDefault().getPreferenceStore().
 							getBoolean(PreferenceConstants.P_SHOW_DISABLED)) {
 						if (0 == anomaly.getLocation().size()) {
 				    		if (((R4EUIReviewBasic)getParent()).getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 								uiAnomaly = new R4EUIAnomalyBasic(this, anomaly, null);
 				    		} else {
 				    			uiAnomaly = new R4EUIAnomalyExtended(this, anomaly, null);
 				    			uiAnomaly.setName(R4EUIAnomalyExtended.getStateString(anomaly.getState()) + ": " + uiAnomaly.getName());
 				    		}
 							addChildren(uiAnomaly);
 							uiAnomaly.open();
 						}
 					}
 				}
 			}
 			
 		}
 		fOpen = true;
 	}
 	
 	/**
 	 * Method isEnabled.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#isEnabled()
 	 */
 	@Override
 	public boolean isEnabled() {
 		if (getParent().isEnabled()) {
 			if (0 == fAnomalies.size()) return true;
 			for (R4EUIAnomalyBasic anomaly : fAnomalies) {
 				if (anomaly.isEnabled()) return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Method addChildren.
 	 * @param aChildToAdd IR4EUIModelElement
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#addChildren(IR4EUIModelElement)
 	 */
 	@Override
 	public void addChildren(IR4EUIModelElement aChildToAdd) {
 		fAnomalies.add((R4EUIAnomalyBasic) aChildToAdd);
 		aChildToAdd.addListener((ReviewNavigatorContentProvider) R4EUIModelController.getNavigatorView().getTreeViewer().getContentProvider());
 		fireAdd(aChildToAdd);
 	}
 
 	/**
 	 * Method createChildren
 	 * @param aModelComponent - the serialization model component object
 	 * @return IR4EUIModelElement
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#createChildren(ReviewNavigatorContentProvider)
 	 */
 	@Override
 	public IR4EUIModelElement createChildren(R4EReviewComponent aModelComponent) throws ResourceHandlingException, OutOfSyncException {
 		final String user = R4EUIModelController.getReviewer();
 		final R4EAnomaly anomaly = R4EUIModelController.FModelExt.createR4EAnomaly(((R4EUIReviewBasic)getParent()).getParticipant(user, true));
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(anomaly, 
 				R4EUIModelController.getReviewer());
 		anomaly.setTitle(((R4EAnomaly)aModelComponent).getTitle());  //This is needed as the global anomaly title is displayed in the navigator view
     	R4EUIModelController.FResourceUpdater.checkIn(bookNum);
     	R4EUIAnomalyBasic addedChild = null;
     	if (R4EUIModelController.getActiveReview().getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
     		addedChild = new R4EUIAnomalyBasic(this, anomaly, null);
 		} else {
 			addedChild = new R4EUIAnomalyExtended(this, anomaly, null);
 			if (R4EUIModelController.getActiveReview().getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_FORMAL)) {
 				((R4EUIAnomalyExtended)addedChild).updateState(R4EAnomalyState.R4E_ANOMALY_STATE_CREATED);
 			} else {  //R4EReviewType.R4E_REVIEW_TYPE_INFORMAL
 				((R4EUIAnomalyExtended)addedChild).updateState(R4EAnomalyState.R4E_ANOMALY_STATE_ASSIGNED);
 			}
 		}
 		addedChild.setModelData(aModelComponent);
 		addChildren(addedChild);
 		return addedChild;
 	}
 	
 	/**
 	 * Method createAnomaly
 	 * @param aUiPosition - the position of the anomaly to create
 	 * @return R4EUIAnomaly
 	 * @throws ResourceHandlingException
 	 * @throws OutOfSyncException 
 	 */
 	public R4EUIAnomalyBasic createAnomaly(R4EUITextPosition aUiPosition) throws ResourceHandlingException, OutOfSyncException {
 		
 		R4EUIAnomalyBasic uiAnomaly = null;
 		
 		//Get anomaliy details from user
 		R4EUIModelController.setDialogOpen(true);
 		final AnomalyInputDialog dialog = new AnomalyInputDialog(R4EUIModelController.getNavigatorView(). // $codepro.audit.disable methodChainLength
 				getSite().getWorkbenchWindow().getShell());
     	final int result = dialog.open();
     	
     	if (result == Window.OK) {
     		
     		// Get handle to local storage repository. No need to continue in case of failure.
     		IRFSRegistry revRepo = null;
     		try {
     			revRepo = RFSRegistryFactory.getRegistry(R4EUIModelController.getActiveReview().getReview());
     		} catch (ReviewsFileStorageException e1) {
     			Activator.Ftracer.traceWarning("Exception while obtaining handle to local repo: " + e1.toString() + " ("
     					+ e1.getMessage() + ")");
     			Activator.getDefault().logWarning("Exception: " + e1.toString(), e1);
     			final ErrorDialog errorDialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
     					"Error detected while adding File Context element."
     							+ " Cannot get to interface to the local reviews repository", new Status(
     							IStatus.WARNING, Activator.PLUGIN_ID, 0, e1.getMessage(), e1), IStatus.WARNING);
     			errorDialog.open();
     			return null;
     		}
     		
     		//Create anomaly model element
     		final R4EUIReviewBasic uiReview = R4EUIModelController.getActiveReview();
     		final R4EParticipant participant = uiReview.getParticipant(R4EUIModelController.getReviewer(), true);
     		final R4EAnomaly anomaly = R4EUIModelController.FModelExt.createR4EAnomaly(participant);
     		
     		Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(anomaly, R4EUIModelController.getReviewer());
     		anomaly.setTitle(dialog.getAnomalyTitleValue());
     		anomaly.setDescription(dialog.getAnomalyDescriptionValue());
         	R4EUIModelController.FResourceUpdater.checkIn(bookNum);
     		
     		//Set data in the anomaly created
     		final R4EAnomalyTextPosition position = R4EUIModelController.FModelExt.createR4EAnomalyTextPosition(
     				R4EUIModelController.FModelExt.createR4ETextContent(anomaly));
     		final R4EFileVersion anomalyFileVersion = R4EUIModelController.FModelExt.createR4EFileVersion(position);
     		
     		bookNum = R4EUIModelController.FResourceUpdater.checkOut(anomalyFileVersion, R4EUIModelController.getReviewer());
     		final IFile anomalyFile = ((R4EUIFileContext)getParent()).getTargetFile();
     		anomalyFileVersion.setResource(anomalyFile);
     		anomalyFileVersion.setPlatformURI(ResourceUtils.toPlatformURI(anomalyFile).toString());
     		
     		try {
         		final IProject project = anomalyFile.getProject();
     			final ReviewsVersionsIF versionsIf = ReviewsVersionsIFFactory.instance.getVersionsIF(project);
     			
     			//File is version-controlled
     			final FileVersionInfo versionInfo = versionsIf.getFileVersionInfo(anomalyFile);
     			anomalyFileVersion.setName(versionInfo.getName());
     			anomalyFileVersion.setRepositoryPath(versionInfo.getRepositoryPath());
     			anomalyFileVersion.setVersionID(versionInfo.getId());
     		} catch (ReviewVersionsException e) {
     			Activator.Ftracer.traceInfo("Exception: " + e.toString() + " (" + e.getMessage() + ")");
     			Activator.getDefault().logInfo("Exception: " + e.toString(), e);
    			/* TODO removed because it is erroneously called this is a bug that needs to be fixed (see bug 340530)
     			final ErrorDialog warningDialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_INFO, 
 						"Take note that the anomaly you are trying to add is on a review item that not in source control.",
         				new Status(IStatus.INFO, Activator.PLUGIN_ID, 0, e.getMessage(), e), IStatus.INFO);
     			warningDialog.open();
    			*/
     			//File is not version-controlled
     			anomalyFileVersion.setName(anomalyFile.getName());
     			anomalyFileVersion.setRepositoryPath(anomalyFile.getFullPath().toOSString());
     			anomalyFileVersion.setVersionID(R4EUIConstants.FILE_NOT_IN_VERSION_CONTROL_MSG);	
     		}
     		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
     		
 			//Register Target file to the local storage space
 			InputStream is = null;
 			try {
 				is = anomalyFile.getContents(false);
 				final String locTargetFileId = revRepo.registerReviewBlob(is);
 				anomalyFileVersion.setLocalVersionID(locTargetFileId);
 			} catch (CoreException e) {
 				Activator.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				Activator.getDefault().logWarning("Exception: " + e.toString(), e);
 				final ErrorDialog errorDialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR, 
 						"Unable to extract contents from target IFile while adding anomaly target file version." +
 						((null != anomalyFile.getLocationURI()) ? ", IFile path: " + anomalyFile.getLocationURI().getPath() : ""),
 						new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, e.toString(), e), IStatus.WARNING);
 				errorDialog.open();
 			} catch (ReviewsFileStorageException e) {
 				Activator.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				Activator.getDefault().logWarning("Exception: " + e.toString(), e);
 				final ErrorDialog errorDialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR, 
 						"Local File repository error detected while adding File Context. Cannot register files in the reviews repository",
 						new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, e.toString(), e), IStatus.WARNING);
 				errorDialog.open();
 			} finally {
 				if (null != is) {
 					try {
 						is.close();
 					} catch (IOException e) {
 						Activator.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 						Activator.getDefault().logWarning("Exception: " + e.toString(), e);
 					}
 				}
 			}
 		
     		//Create and set UI model element
     		if (uiReview.getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
     			uiAnomaly = new R4EUIAnomalyBasic(this, anomaly, aUiPosition);	
     		} else {
     			uiAnomaly = new R4EUIAnomalyExtended(this, anomaly, aUiPosition);
     			if (uiReview.getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_FORMAL)) {
     				((R4EUIAnomalyExtended)uiAnomaly).updateState(R4EAnomalyState.R4E_ANOMALY_STATE_CREATED);
     			} else {  //R4EReviewType.R4E_REVIEW_TYPE_INFORMAL
     				((R4EUIAnomalyExtended)uiAnomaly).updateState(R4EAnomalyState.R4E_ANOMALY_STATE_ASSIGNED);
     			}
     		}
     		aUiPosition.setPositionInModel(position);
     		uiAnomaly.setToolTip(R4EUIAnomalyBasic.buildAnomalyToolTip(anomaly));   //Also set UI tooltip immediately
     		addChildren(uiAnomaly);
     		
     	}
     	// else Window.CANCEL
 		R4EUIModelController.setDialogOpen(false);
 		return uiAnomaly;
 	}
 	
 	
 	
 	/**
 	 * Method removeChildren.
 	 * @param aChildToRemove IR4EUIModelElement
 	 * @param aFileRemove - also remove from file (hard remove)
 	 * @throws OutOfSyncException 
 	 * @throws ResourceHandlingException 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#removeChildren(IR4EUIModelElement)
 	 */
 	@Override
 	public void removeChildren(IR4EUIModelElement aChildToRemove, boolean aFileRemove) throws ResourceHandlingException, OutOfSyncException {
 		final R4EUIAnomalyBasic removedElement = fAnomalies.get(fAnomalies.indexOf(aChildToRemove));
 		
 		//Also recursively remove all children 
 		removedElement.removeAllChildren(aFileRemove);
 
 		/* TODO uncomment when core model supports hard-removing of elements
 		if (aFileRemove) removedElement.getAnomaly().remove());
 		else */ 
 		final R4EAnomaly modelAnomaly = removedElement.getAnomaly();
 		final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelAnomaly, R4EUIModelController.getReviewer());
 		modelAnomaly.setEnabled(false);
 		R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 		//Remove element from UI if the show disabled element option is off
 		if (!(Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_SHOW_DISABLED))) {
 			fAnomalies.remove(removedElement);
 			aChildToRemove.removeListener();
 			fireRemove(aChildToRemove);
 		} else {
 			R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 		}
 	}
 	
 	/**
 	 * Method removeAllChildren.
 	 * @param aFileRemove boolean
 	 * @throws OutOfSyncException 
 	 * @throws ResourceHandlingException 
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#removeAllChildren(boolean)
 	 */
 	@Override
 	public void removeAllChildren(boolean aFileRemove) throws ResourceHandlingException, OutOfSyncException {
 		//Recursively remove all children
 		for (R4EUIAnomalyBasic anomaly : fAnomalies) {
 			removeChildren(anomaly, aFileRemove);
 		}
 	}
 	
 	//Listeners
 	
 	/**
 	 * Method addListener.
 	 * @param aProvider ReviewNavigatorContentProvider
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#addListener(ReviewNavigatorContentProvider)
 	 */
 	@Override
 	public void addListener(ReviewNavigatorContentProvider aProvider) {
 		fListener = aProvider;
 		if (null != fAnomalies) {
 			R4EUIAnomalyBasic element = null;
 			for (final Iterator<R4EUIAnomalyBasic> iterator = fAnomalies.iterator(); iterator.hasNext();) {
 			    element = iterator.next();
 				element.addListener(aProvider);
 			}
 		}
 	}
 	
 	/**
 	 * Method removeListener
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#removeListener()
 	 */
 	@Override
 	public void removeListener() {
 		fListener = null;
 		if (null != fAnomalies) {
 			R4EUIAnomalyBasic element = null;
 			for (final Iterator<R4EUIAnomalyBasic> iterator = fAnomalies.iterator(); iterator.hasNext();) {
 				element = iterator.next();
 				element.removeListener();
 			}
 		}
 	}
 	
 	
 	//Commands
 	
 	/**
 	 * Method isAddChildElementCmd.
 	 * @return boolean
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#isAddChildElementCmd()
 	 */
 	@Override
 	public boolean isAddChildElementCmd() {
 		//If this is a formal review, we need to be in the preparation phase
 		if (R4EUIModelController.getActiveReview().getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_FORMAL)) {
 			if (!(((R4EFormalReview)R4EUIModelController.getActiveReview().getReview()).getCurrent().getType().
 					equals(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION))) {
 				return false;
 			}
 		}
 		if (getParent().isEnabled() && !(R4EUIModelController.getActiveReview().isReviewed())) return true;
 		return false;
 	}
 	
 	/**
 	 * Method getAddChildElementCmdName.
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#getAddChildElementCmdName()
 	 */
 	@Override
 	public String getAddChildElementCmdName() {
 		return ADD_CHILD_ELEMENT_COMMAND_NAME;
 	}
 	
 	/**
 	 * Method getAddChildElementCmdTooltip.
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement#getAddChildElementCmdTooltip()
 	 */
 	@Override
 	public String getAddChildElementCmdTooltip() {
 		return ADD_CHILD_ELEMENT_COMMAND_TOOLTIP; 
 	}
 	
 	/**
 	 * Method checkCompletionStatus.
 	 * @return boolean
 	 */
 	public boolean checkCompletionStatus() { // $codepro.audit.disable booleanMethodNamingConvention
 		for (R4EUIAnomalyBasic anomaly : fAnomalies) {
 			if (anomaly.getAnomaly().getState().equals(R4EAnomalyState.R4E_ANOMALY_STATE_CREATED) ||
 				anomaly.getAnomaly().getState().equals(R4EAnomalyState.R4E_ANOMALY_STATE_ASSIGNED) ||	
 				anomaly.getAnomaly().getState().equals(R4EAnomalyState.R4E_ANOMALY_STATE_ACCEPTED)) {
 				return false;
 			} else if (anomaly.getAnomaly().getState().equals(R4EAnomalyState.R4E_ANOMALY_STATE_FIXED)) {
 				if (null == anomaly.getAnomaly().getFixedByID() || ("").equals(anomaly.getAnomaly().getFixedByID())) {
 					return false;
 				}
 				if (R4EUIModelController.getActiveReview().getReview().getDecision().getValue().equals(R4EDecision.R4E_REVIEW_DECISION_ACCEPTED_FOLLOWUP)) {
 					return false;
 				}
 			} else if (anomaly.getAnomaly().getState().equals(R4EAnomalyState.R4E_ANOMALY_STATE_VERIFIED)) {
 				if (null == anomaly.getAnomaly().getFollowUpByID() || ("").equals(anomaly.getAnomaly().getFollowUpByID())) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Method checkCompletionStatus.
 	 * @return boolean
 	 */
 	public boolean checkReworkStatus() { // $codepro.audit.disable booleanMethodNamingConvention
 		for (R4EUIAnomalyBasic anomaly : fAnomalies) {
 			if (anomaly.getAnomaly().getState().equals(R4EAnomalyState.R4E_ANOMALY_STATE_CREATED)) {
 				return false;
 			}
 			if (null == anomaly.getAnomaly().getDecidedByID() || ("").equals(anomaly.getAnomaly().getDecidedByID())) {
 				return false;
 			}
 		}
 		return true;
 	}
 }
