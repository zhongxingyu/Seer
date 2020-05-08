 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.model.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.BasicEMap;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.EcoreEMap;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.emfstore.client.model.CompositeOperationHandle;
 import org.eclipse.emf.emfstore.client.model.Configuration;
 import org.eclipse.emf.emfstore.client.model.EventComposite;
 import org.eclipse.emf.emfstore.client.model.ModelFactory;
 import org.eclipse.emf.emfstore.client.model.ModelPackage;
 import org.eclipse.emf.emfstore.client.model.ModifiedModelElementsCache;
 import org.eclipse.emf.emfstore.client.model.NotificationComposite;
 import org.eclipse.emf.emfstore.client.model.OperationComposite;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.changeTracking.commands.EMFStoreCommandStack;
 import org.eclipse.emf.emfstore.client.model.changeTracking.notification.recording.NotificationRecorder;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.ConnectionManager;
 import org.eclipse.emf.emfstore.client.model.controller.UpdateCallback;
 import org.eclipse.emf.emfstore.client.model.controller.UpdateController;
 import org.eclipse.emf.emfstore.client.model.exceptions.ChangeConflictException;
 import org.eclipse.emf.emfstore.client.model.exceptions.CommitCanceledException;
 import org.eclipse.emf.emfstore.client.model.exceptions.IllegalProjectSpaceStateException;
 import org.eclipse.emf.emfstore.client.model.exceptions.MEUrlResolutionException;
 import org.eclipse.emf.emfstore.client.model.exceptions.NoChangesOnServerException;
 import org.eclipse.emf.emfstore.client.model.exceptions.NoLocalChangesException;
 import org.eclipse.emf.emfstore.client.model.exceptions.PropertyNotFoundException;
 import org.eclipse.emf.emfstore.client.model.filetransfer.FileDownloadStatus;
 import org.eclipse.emf.emfstore.client.model.filetransfer.FileInformation;
 import org.eclipse.emf.emfstore.client.model.filetransfer.FileTransferManager;
 import org.eclipse.emf.emfstore.client.model.notification.NotificationGenerator;
 import org.eclipse.emf.emfstore.client.model.observers.CommitObserver;
 import org.eclipse.emf.emfstore.client.model.observers.ConflictResolver;
 import org.eclipse.emf.emfstore.client.model.observers.LoginObserver;
 import org.eclipse.emf.emfstore.client.model.observers.ShareObserver;
 import org.eclipse.emf.emfstore.client.model.observers.UpdateObserver;
 import org.eclipse.emf.emfstore.client.model.preferences.PropertyKey;
 import org.eclipse.emf.emfstore.client.model.util.ResourceHelper;
 import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.client.properties.PropertyManager;
 import org.eclipse.emf.emfstore.common.model.EMFStoreProperty;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.impl.IdentifiableElementImpl;
 import org.eclipse.emf.emfstore.common.model.impl.ProjectImpl;
 import org.eclipse.emf.emfstore.common.model.impl.PropertyMapEntryImpl;
 import org.eclipse.emf.emfstore.common.model.util.AutoSplitAndSaveResourceContainmentList;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.conflictDetection.ConflictDetector;
 import org.eclipse.emf.emfstore.server.exceptions.BaseVersionOutdatedException;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.FileTransferException;
 import org.eclipse.emf.emfstore.server.model.FileIdentifier;
 import org.eclipse.emf.emfstore.server.model.ProjectId;
 import org.eclipse.emf.emfstore.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACUser;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.OrgUnitProperty;
 import org.eclipse.emf.emfstore.server.model.notification.ESNotification;
 import org.eclipse.emf.emfstore.server.model.url.ModelElementUrlFragment;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.server.model.versioning.events.Event;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.CompositeOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.semantic.SemanticCompositeOperation;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object ' <em><b>Project Container</b></em>'.
  * 
  * @implements LoginObserver <!-- end-user-doc -->
  *             <p>
  *             The following features are implemented:
  *             <ul>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getProject
  *             <em>Project</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getProjectId
  *             <em>Project Id</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getProjectName
  *             <em>Project Name</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getProjectDescription
  *             <em>Project Description</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getEvents
  *             <em>Events</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getUsersession
  *             <em>Usersession</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getLastUpdated
  *             <em>Last Updated</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getBaseVersion
  *             <em>Base Version</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getResourceCount
  *             <em>Resource Count</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#isDirty
  *             <em>Dirty</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getOldLogMessages
  *             <em>Old Log Messages</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getLocalOperations
  *             <em>Local Operations</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getNotifications
  *             <em>Notifications</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getEventComposite
  *             <em>Event Composite</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getNotificationComposite
  *             <em>Notification Composite</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getWaitingUploads
  *             <em>Waiting Uploads</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getProperties
  *             <em>Properties</em>}</li>
  *             <li>
  *             {@link org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl#getChangedSharedProperties
  *             <em>Changed Shared Properties</em>}</li>
  *             </ul>
  *             </p>
  * 
  * @generated
  */
 public class ProjectSpaceImpl extends IdentifiableElementImpl implements ProjectSpace, LoginObserver {
 
 	/**
 	 * The cached value of the '{@link #getProject() <em>Project</em>}'
 	 * containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getProject()
 	 * @generated
 	 * @ordered
 	 */
 	protected Project project;
 
 	/**
 	 * The cached value of the '{@link #getProjectId() <em>Project Id</em>}'
 	 * containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getProjectId()
 	 * @generated
 	 * @ordered
 	 */
 	protected ProjectId projectId;
 
 	/**
 	 * The default value of the '{@link #getProjectName() <em>Project Name</em>} ' attribute. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getProjectName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String PROJECT_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getProjectName() <em>Project Name</em>}'
 	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getProjectName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String projectName = PROJECT_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getProjectDescription()
 	 * <em>Project Description</em>}' attribute. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getProjectDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String PROJECT_DESCRIPTION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getProjectDescription()
 	 * <em>Project Description</em>}' attribute. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getProjectDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected String projectDescription = PROJECT_DESCRIPTION_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getEvents() <em>Events</em>}'
 	 * containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getEvents()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Event> events;
 
 	/**
 	 * The cached value of the '{@link #getUsersession() <em>Usersession</em>}'
 	 * reference. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getUsersession()
 	 * @generated
 	 * @ordered
 	 */
 	protected Usersession usersession;
 
 	/**
 	 * The default value of the '{@link #getLastUpdated() <em>Last Updated</em>} ' attribute. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getLastUpdated()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Date LAST_UPDATED_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getLastUpdated() <em>Last Updated</em>}'
 	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getLastUpdated()
 	 * @generated
 	 * @ordered
 	 */
 	protected Date lastUpdated = LAST_UPDATED_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getBaseVersion() <em>Base Version</em>}'
 	 * containment reference. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getBaseVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected PrimaryVersionSpec baseVersion;
 
 	/**
 	 * The default value of the '{@link #getResourceCount()
 	 * <em>Resource Count</em>}' attribute. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getResourceCount()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int RESOURCE_COUNT_EDEFAULT = 0;
 
 	/**
 	 * The cached value of the '{@link #getResourceCount()
 	 * <em>Resource Count</em>}' attribute. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getResourceCount()
 	 * @generated
 	 * @ordered
 	 */
 	protected int resourceCount = RESOURCE_COUNT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isDirty() <em>Dirty</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #isDirty()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean DIRTY_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isDirty() <em>Dirty</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #isDirty()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean dirty = DIRTY_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getOldLogMessages()
 	 * <em>Old Log Messages</em>}' attribute list. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getOldLogMessages()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<String> oldLogMessages;
 
 	/**
 	 * The cached value of the '{@link #getLocalOperations()
 	 * <em>Local Operations</em>}' containment reference. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @see #getLocalOperations()
 	 * @generated
 	 * @ordered
 	 */
 	protected OperationComposite localOperations;
 
 	/**
 	 * The cached value of the '{@link #getNotifications()
 	 * <em>Notifications</em>}' containment reference list. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @see #getNotifications()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<ESNotification> notifications;
 
 	/**
 	 * The cached value of the '{@link #getEventComposite()
 	 * <em>Event Composite</em>}' containment reference. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getEventComposite()
 	 * @generated
 	 * @ordered
 	 */
 	protected EventComposite eventComposite;
 
 	/**
 	 * The cached value of the '{@link #getNotificationComposite()
 	 * <em>Notification Composite</em>}' containment reference. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getNotificationComposite()
 	 * @generated
 	 * @ordered
 	 */
 	protected NotificationComposite notificationComposite;
 
 	/**
 	 * The cached value of the '{@link #getWaitingUploads()
 	 * <em>Waiting Uploads</em>}' containment reference list. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getWaitingUploads()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<FileIdentifier> waitingUploads;
 
 	/**
 	 * The cached value of the '{@link #getProperties() <em>Properties</em>}'
 	 * map. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getProperties()
 	 * @generated
 	 * @ordered
 	 */
 	protected EMap<String, EMFStoreProperty> properties;
 
 	/**
 	 * The cached value of the '{@link #getChangedSharedProperties()
 	 * <em>Changed Shared Properties</em>}' map. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getChangedSharedProperties()
 	 * @generated
 	 * @ordered
 	 */
 	protected EMap<String, EMFStoreProperty> changedSharedProperties;
 
 	private boolean initCompleted;
 
 	private boolean isTransient;
 
 	private List<CommitObserver> commitObservers;
 
 	private ModifiedModelElementsCache modifiedModelElementsCache;
 
 	private OperationManager operationManager;
 
 	private AutoSplitAndSaveResourceContainmentList<AbstractOperation> operationsList;
 
 	private AutoSplitAndSaveResourceContainmentList<Event> eventList;
 
 	private HashMap<String, OrgUnitProperty> propertyMap;
 
 	private AutoSplitAndSaveResourceContainmentList<ESNotification> notificationList;
 
 	private ArrayList<ShareObserver> shareObservers;
 
 	private FileTransferManager fileTransferManager;
 
 	private OperationRecorder operationRecorder;
 
 	private StatePersister statePersister;
 
 	private PropertyManager propertyManager;
 
 	// begin of custom code
 	/**
 	 * Constructor. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected ProjectSpaceImpl() {
 		super();
 		// TODO remove observer/listeners and use observerbus
 		this.commitObservers = new ArrayList<CommitObserver>();
 		this.shareObservers = new ArrayList<ShareObserver>();
 		this.propertyMap = new HashMap<String, OrgUnitProperty>();
 		modifiedModelElementsCache = new ModifiedModelElementsCache(this);
 
 		this.addCommitObserver(modifiedModelElementsCache);
 		shareObservers.add(modifiedModelElementsCache);
 
 	}
 
 	// end of custom code
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ModelPackage.Literals.PROJECT_SPACE;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Project getProject() {
 		if (project != null && project.eIsProxy()) {
 			InternalEObject oldProject = (InternalEObject) project;
 			project = (Project) eResolveProxy(oldProject);
 			if (project != oldProject) {
 				InternalEObject newProject = (InternalEObject) project;
 				NotificationChain msgs = oldProject.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__PROJECT, null, null);
 				if (newProject.eInternalContainer() == null) {
 					msgs = newProject.eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelPackage.PROJECT_SPACE__PROJECT,
 						null, msgs);
 				}
 				if (msgs != null)
 					msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.PROJECT_SPACE__PROJECT,
 						oldProject, project));
 			}
 		}
 		return project;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Project basicGetProject() {
 		return project;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetProject(Project newProject, NotificationChain msgs) {
 		Project oldProject = project;
 		project = newProject;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				ModelPackage.PROJECT_SPACE__PROJECT, oldProject, newProject);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setProject(Project newProject) {
 		if (newProject != project) {
 			NotificationChain msgs = null;
 			if (project != null)
 				msgs = ((InternalEObject) project).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__PROJECT, null, msgs);
 			if (newProject != null)
 				msgs = ((InternalEObject) newProject).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__PROJECT, null, msgs);
 			msgs = basicSetProject(newProject, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.PROJECT_SPACE__PROJECT, newProject,
 				newProject));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ProjectId getProjectId() {
 		if (projectId != null && projectId.eIsProxy()) {
 			InternalEObject oldProjectId = (InternalEObject) projectId;
 			projectId = (ProjectId) eResolveProxy(oldProjectId);
 			if (projectId != oldProjectId) {
 				InternalEObject newProjectId = (InternalEObject) projectId;
 				NotificationChain msgs = oldProjectId.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__PROJECT_ID, null, null);
 				if (newProjectId.eInternalContainer() == null) {
 					msgs = newProjectId.eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 						- ModelPackage.PROJECT_SPACE__PROJECT_ID, null, msgs);
 				}
 				if (msgs != null)
 					msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.PROJECT_SPACE__PROJECT_ID,
 						oldProjectId, projectId));
 			}
 		}
 		return projectId;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ProjectId basicGetProjectId() {
 		return projectId;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetProjectId(ProjectId newProjectId, NotificationChain msgs) {
 		ProjectId oldProjectId = projectId;
 		projectId = newProjectId;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				ModelPackage.PROJECT_SPACE__PROJECT_ID, oldProjectId, newProjectId);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setProjectId(ProjectId newProjectId) {
 		if (newProjectId != projectId) {
 			NotificationChain msgs = null;
 			if (projectId != null)
 				msgs = ((InternalEObject) projectId).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__PROJECT_ID, null, msgs);
 			if (newProjectId != null)
 				msgs = ((InternalEObject) newProjectId).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__PROJECT_ID, null, msgs);
 			msgs = basicSetProjectId(newProjectId, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.PROJECT_SPACE__PROJECT_ID, newProjectId,
 				newProjectId));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getProjectName() {
 		return projectName;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setProjectName(String newProjectName) {
 		String oldProjectName = projectName;
 		projectName = newProjectName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.PROJECT_SPACE__PROJECT_NAME,
 				oldProjectName, projectName));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getProjectDescription() {
 		return projectDescription;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setProjectDescription(String newProjectDescription) {
 		String oldProjectDescription = projectDescription;
 		projectDescription = newProjectDescription;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.PROJECT_SPACE__PROJECT_DESCRIPTION,
 				oldProjectDescription, projectDescription));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Deprecated
 	public EList<Event> getEvents() {
 		if (events == null) {
 			events = new EObjectContainmentEList.Resolving<Event>(Event.class, this, ModelPackage.PROJECT_SPACE__EVENTS);
 		}
 		return events;
 	}
 
 	// begin of custom code
 	/**
 	 * Get the events that have been logged for this project space.
 	 * 
 	 * @return a list of events ordered by creation time
 	 */
 	public List<Event> getEventsFromComposite() {
 		// check if operation composite exists
 		EventComposite eventComposite = this.getEventComposite();
 		if (isTransient) {
 			if (eventComposite == null) {
 				eventComposite = ModelFactory.eINSTANCE.createEventComposite();
 				this.setEventComposite(eventComposite);
 			}
 			return eventComposite.getEvents();
 		}
 		if (eventComposite == null) {
 			eventComposite = ModelFactory.eINSTANCE.createEventComposite();
 			// migration code: existing events in the event feature are added to
 			// the composite
 			eventList = new AutoSplitAndSaveResourceContainmentList<Event>(eventComposite, eventComposite.getEvents(),
 				this.eResource().getResourceSet(), Configuration.getWorkspaceDirectory() + "ps-" + getIdentifier()
 					+ File.separatorChar + "events", ".eff");
 			this.setEventComposite(eventComposite);
 			if (getEvents().size() > 0) {
 				eventList.addAll(getEvents());
 				saveProjectSpaceOnly();
 			}
 		}
 		if (eventList == null) {
 			eventList = new AutoSplitAndSaveResourceContainmentList<Event>(eventComposite, eventComposite.getEvents(),
 				this.eResource().getResourceSet(), Configuration.getWorkspaceDirectory() + "ps-" + getIdentifier()
 					+ File.separatorChar + "events", ".eff");
 		}
 		return eventList;
 	}
 
 	// end of custom code
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Usersession getUsersession() {
 		if (usersession != null && usersession.eIsProxy()) {
 			InternalEObject oldUsersession = (InternalEObject) usersession;
 			usersession = (Usersession) eResolveProxy(oldUsersession);
 			if (usersession != oldUsersession) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.PROJECT_SPACE__USERSESSION,
 						oldUsersession, usersession));
 			}
 		}
 		return usersession;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Usersession basicGetUsersession() {
 		return usersession;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setUsersession(Usersession newUsersession) {
 		Usersession oldUsersession = usersession;
 		usersession = newUsersession;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.PROJECT_SPACE__USERSESSION,
 				oldUsersession, usersession));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Date getLastUpdated() {
 		return lastUpdated;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setLastUpdated(Date newLastUpdated) {
 		Date oldLastUpdated = lastUpdated;
 		lastUpdated = newLastUpdated;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.PROJECT_SPACE__LAST_UPDATED,
 				oldLastUpdated, lastUpdated));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public PrimaryVersionSpec getBaseVersion() {
 		if (baseVersion != null && baseVersion.eIsProxy()) {
 			InternalEObject oldBaseVersion = (InternalEObject) baseVersion;
 			baseVersion = (PrimaryVersionSpec) eResolveProxy(oldBaseVersion);
 			if (baseVersion != oldBaseVersion) {
 				InternalEObject newBaseVersion = (InternalEObject) baseVersion;
 				NotificationChain msgs = oldBaseVersion.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__BASE_VERSION, null, null);
 				if (newBaseVersion.eInternalContainer() == null) {
 					msgs = newBaseVersion.eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 						- ModelPackage.PROJECT_SPACE__BASE_VERSION, null, msgs);
 				}
 				if (msgs != null)
 					msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.PROJECT_SPACE__BASE_VERSION,
 						oldBaseVersion, baseVersion));
 			}
 		}
 		return baseVersion;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public PrimaryVersionSpec basicGetBaseVersion() {
 		return baseVersion;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetBaseVersion(PrimaryVersionSpec newBaseVersion, NotificationChain msgs) {
 		PrimaryVersionSpec oldBaseVersion = baseVersion;
 		baseVersion = newBaseVersion;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				ModelPackage.PROJECT_SPACE__BASE_VERSION, oldBaseVersion, newBaseVersion);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setBaseVersion(PrimaryVersionSpec newBaseVersion) {
 		if (newBaseVersion != baseVersion) {
 			NotificationChain msgs = null;
 			if (baseVersion != null)
 				msgs = ((InternalEObject) baseVersion).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__BASE_VERSION, null, msgs);
 			if (newBaseVersion != null)
 				msgs = ((InternalEObject) newBaseVersion).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__BASE_VERSION, null, msgs);
 			msgs = basicSetBaseVersion(newBaseVersion, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.PROJECT_SPACE__BASE_VERSION,
 				newBaseVersion, newBaseVersion));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public int getResourceCount() {
 		return resourceCount;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setResourceCount(int newResourceCount) {
 		int oldResourceCount = resourceCount;
 		resourceCount = newResourceCount;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.PROJECT_SPACE__RESOURCE_COUNT,
 				oldResourceCount, resourceCount));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public boolean isDirty() {
 		return dirty;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setDirty(boolean newDirty) {
 		boolean oldDirty = dirty;
 		dirty = newDirty;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.PROJECT_SPACE__DIRTY, oldDirty, dirty));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<String> getOldLogMessages() {
 		if (oldLogMessages == null) {
 			oldLogMessages = new EDataTypeUniqueEList<String>(String.class, this,
 				ModelPackage.PROJECT_SPACE__OLD_LOG_MESSAGES);
 		}
 		return oldLogMessages;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public OperationComposite getLocalOperations() {
 		if (localOperations != null && localOperations.eIsProxy()) {
 			InternalEObject oldLocalOperations = (InternalEObject) localOperations;
 			localOperations = (OperationComposite) eResolveProxy(oldLocalOperations);
 			if (localOperations != oldLocalOperations) {
 				InternalEObject newLocalOperations = (InternalEObject) localOperations;
 				NotificationChain msgs = oldLocalOperations.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS, null, null);
 				if (newLocalOperations.eInternalContainer() == null) {
 					msgs = newLocalOperations.eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 						- ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS, null, msgs);
 				}
 				if (msgs != null)
 					msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE,
 						ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS, oldLocalOperations, localOperations));
 			}
 		}
 		return localOperations;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public OperationComposite basicGetLocalOperations() {
 		return localOperations;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetLocalOperations(OperationComposite newLocalOperations, NotificationChain msgs) {
 		OperationComposite oldLocalOperations = localOperations;
 		localOperations = newLocalOperations;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS, oldLocalOperations, newLocalOperations);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setLocalOperations(OperationComposite newLocalOperations) {
 		if (newLocalOperations != localOperations) {
 			NotificationChain msgs = null;
 			if (localOperations != null)
 				msgs = ((InternalEObject) localOperations).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS, null, msgs);
 			if (newLocalOperations != null)
 				msgs = ((InternalEObject) newLocalOperations).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS, null, msgs);
 			msgs = basicSetLocalOperations(newLocalOperations, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS,
 				newLocalOperations, newLocalOperations));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Deprecated
 	public EList<ESNotification> getNotifications() {
 		if (notifications == null) {
 			notifications = new EObjectContainmentEList.Resolving<ESNotification>(ESNotification.class, this,
 				ModelPackage.PROJECT_SPACE__NOTIFICATIONS);
 		}
 		return notifications;
 	}
 
 	/**
 	 * Get the events that have been logged for this project space.
 	 * 
 	 * @return a list of events ordered by creation time
 	 */
 	public List<ESNotification> getNotificationsFromComposite() {
 		// check if operation composite exists
 		NotificationComposite notificationComposite = this.getNotificationComposite();
 		if (isTransient) {
 			if (notificationComposite == null) {
 				notificationComposite = ModelFactory.eINSTANCE.createNotificationComposite();
 				this.setNotificationComposite(notificationComposite);
 			}
 			return notificationComposite.getNotifications();
 		}
 		if (notificationComposite == null) {
 			notificationComposite = ModelFactory.eINSTANCE.createNotificationComposite();
 			// migration code: existing notifications in the notification
 			// feature are added to the composite
 			notificationList = new AutoSplitAndSaveResourceContainmentList<ESNotification>(notificationComposite,
 				notificationComposite.getNotifications(), this.eResource().getResourceSet(),
 				Configuration.getWorkspaceDirectory() + "ps-" + getIdentifier() + File.separatorChar + "notifications",
 				".nff");
 			this.setNotificationComposite(notificationComposite);
 			if (getNotifications().size() > 0) {
 				notificationList.addAll(getNotifications());
 				saveProjectSpaceOnly();
 			}
 		}
 		if (notificationList == null) {
 			notificationList = new AutoSplitAndSaveResourceContainmentList<ESNotification>(notificationComposite,
 				notificationComposite.getNotifications(), this.eResource().getResourceSet(),
 				Configuration.getWorkspaceDirectory() + "ps-" + getIdentifier() + File.separatorChar + "notifications",
 				".nff");
 		}
 		return notificationList;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EventComposite getEventComposite() {
 		if (eventComposite != null && eventComposite.eIsProxy()) {
 			InternalEObject oldEventComposite = (InternalEObject) eventComposite;
 			eventComposite = (EventComposite) eResolveProxy(oldEventComposite);
 			if (eventComposite != oldEventComposite) {
 				InternalEObject newEventComposite = (InternalEObject) eventComposite;
 				NotificationChain msgs = oldEventComposite.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__EVENT_COMPOSITE, null, null);
 				if (newEventComposite.eInternalContainer() == null) {
 					msgs = newEventComposite.eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 						- ModelPackage.PROJECT_SPACE__EVENT_COMPOSITE, null, msgs);
 				}
 				if (msgs != null)
 					msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE,
 						ModelPackage.PROJECT_SPACE__EVENT_COMPOSITE, oldEventComposite, eventComposite));
 			}
 		}
 		return eventComposite;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EventComposite basicGetEventComposite() {
 		return eventComposite;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetEventComposite(EventComposite newEventComposite, NotificationChain msgs) {
 		EventComposite oldEventComposite = eventComposite;
 		eventComposite = newEventComposite;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				ModelPackage.PROJECT_SPACE__EVENT_COMPOSITE, oldEventComposite, newEventComposite);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setEventComposite(EventComposite newEventComposite) {
 		if (newEventComposite != eventComposite) {
 			NotificationChain msgs = null;
 			if (eventComposite != null)
 				msgs = ((InternalEObject) eventComposite).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__EVENT_COMPOSITE, null, msgs);
 			if (newEventComposite != null)
 				msgs = ((InternalEObject) newEventComposite).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__EVENT_COMPOSITE, null, msgs);
 			msgs = basicSetEventComposite(newEventComposite, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.PROJECT_SPACE__EVENT_COMPOSITE,
 				newEventComposite, newEventComposite));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationComposite getNotificationComposite() {
 		if (notificationComposite != null && notificationComposite.eIsProxy()) {
 			InternalEObject oldNotificationComposite = (InternalEObject) notificationComposite;
 			notificationComposite = (NotificationComposite) eResolveProxy(oldNotificationComposite);
 			if (notificationComposite != oldNotificationComposite) {
 				InternalEObject newNotificationComposite = (InternalEObject) notificationComposite;
 				NotificationChain msgs = oldNotificationComposite.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__NOTIFICATION_COMPOSITE, null, null);
 				if (newNotificationComposite.eInternalContainer() == null) {
 					msgs = newNotificationComposite.eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 						- ModelPackage.PROJECT_SPACE__NOTIFICATION_COMPOSITE, null, msgs);
 				}
 				if (msgs != null)
 					msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE,
 						ModelPackage.PROJECT_SPACE__NOTIFICATION_COMPOSITE, oldNotificationComposite,
 						notificationComposite));
 			}
 		}
 		return notificationComposite;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationComposite basicGetNotificationComposite() {
 		return notificationComposite;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetNotificationComposite(NotificationComposite newNotificationComposite,
 		NotificationChain msgs) {
 		NotificationComposite oldNotificationComposite = notificationComposite;
 		notificationComposite = newNotificationComposite;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				ModelPackage.PROJECT_SPACE__NOTIFICATION_COMPOSITE, oldNotificationComposite, newNotificationComposite);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setNotificationComposite(NotificationComposite newNotificationComposite) {
 		if (newNotificationComposite != notificationComposite) {
 			NotificationChain msgs = null;
 			if (notificationComposite != null)
 				msgs = ((InternalEObject) notificationComposite).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__NOTIFICATION_COMPOSITE, null, msgs);
 			if (newNotificationComposite != null)
 				msgs = ((InternalEObject) newNotificationComposite).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- ModelPackage.PROJECT_SPACE__NOTIFICATION_COMPOSITE, null, msgs);
 			msgs = basicSetNotificationComposite(newNotificationComposite, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.PROJECT_SPACE__NOTIFICATION_COMPOSITE,
 				newNotificationComposite, newNotificationComposite));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<FileIdentifier> getWaitingUploads() {
 		if (waitingUploads == null) {
 			waitingUploads = new EObjectContainmentEList.Resolving<FileIdentifier>(FileIdentifier.class, this,
 				ModelPackage.PROJECT_SPACE__WAITING_UPLOADS);
 		}
 		return waitingUploads;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EMap<String, EMFStoreProperty> getProperties() {
 		if (properties == null) {
 			properties = new EcoreEMap<String, EMFStoreProperty>(
 				org.eclipse.emf.emfstore.common.model.ModelPackage.Literals.PROPERTY_MAP_ENTRY,
 				PropertyMapEntryImpl.class, this, ModelPackage.PROJECT_SPACE__PROPERTIES);
 		}
 		return properties;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EMap<String, EMFStoreProperty> getChangedSharedProperties() {
 		if (changedSharedProperties == null) {
 			changedSharedProperties = new EcoreEMap<String, EMFStoreProperty>(
 				org.eclipse.emf.emfstore.common.model.ModelPackage.Literals.PROPERTY_MAP_ENTRY,
 				PropertyMapEntryImpl.class, this, ModelPackage.PROJECT_SPACE__CHANGED_SHARED_PROPERTIES);
 		}
 		return changedSharedProperties;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @generated NOT
 	 */
 	public PrimaryVersionSpec commit(final LogMessage logMessage) throws EmfStoreException {
 		return commit(logMessage, null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#commit(org.eclipse.emf.emfstore.server.model.versioning.LogMessage,
 	 *      org.eclipse.emf.emfstore.client.model.observers.CommitObserver)
 	 * @generated NOT
 	 */
 	public PrimaryVersionSpec commit(LogMessage logMessage, CommitObserver commitObserver) throws EmfStoreException {
 		ChangePackage changePackage;
 		try {
 			changePackage = prepareCommit(commitObserver);
 			return finalizeCommit(changePackage, logMessage, commitObserver);
 
 		} catch (CommitCanceledException e) {
 			return this.getBaseVersion();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#prepareCommit(org.eclipse.emf.emfstore.client.model.observers.CommitObserver)
 	 * @generated NOT
 	 */
 	public ChangePackage prepareCommit(CommitObserver commitObserver) throws EmfStoreException {
 
 		// check if there are any changes
 		if (!this.isDirty()) {
 			throw new NoLocalChangesException();
 		}
 
 		cleanCutElements();
 
 		// check if we need to update first
 		PrimaryVersionSpec resolvedVersion = resolveVersionSpec(VersionSpec.HEAD_VERSION);
 		if ((!getBaseVersion().equals(resolvedVersion))) {
 			throw new BaseVersionOutdatedException();
 		}
 
 		ChangePackage changePackage = getLocalChangePackage(true);
 		if (changePackage.getOperations().isEmpty()) {
 			for (AbstractOperation operation : getOperations()) {
 				operationManager.notifyOperationUndone(operation);
 			}
 			getOperations().clear();
 			updateDirtyState();
 			throw new NoLocalChangesException();
 		}
 
 		notifyPreCommitObservers(changePackage);
 
 		if (commitObserver != null && !commitObserver.inspectChanges(this, changePackage)) {
 			throw new CommitCanceledException("Changes have been canceld by the user.");
 		}
 
 		return changePackage;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#finalizeCommit(org.eclipse.emf.emfstore.server.model.versioning.ChangePackage,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.LogMessage,
 	 *      org.eclipse.emf.emfstore.client.model.observers.CommitObserver)
 	 * @generated NOT
 	 */
 	public PrimaryVersionSpec finalizeCommit(ChangePackage changePackage, LogMessage logMessage,
 		CommitObserver commitObserver) throws EmfStoreException {
 
 		final ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 
 		PrimaryVersionSpec newBaseVersion = connectionManager.createVersion(getUsersession().getSessionId(),
 			getProjectId(), getBaseVersion(), changePackage, logMessage);
 
 		setBaseVersion(newBaseVersion);
 		getOperations().clear();
 		getEventsFromComposite().clear();
 
 		saveProjectSpaceOnly();
 
 		if (commitObserver != null) {
 			commitObserver.commitCompleted(this, newBaseVersion);
 		}
 
 		fileTransferManager.uploadQueuedFiles(new NullProgressMonitor());
 
 		notifyPostCommitObservers(newBaseVersion);
 
 		updateDirtyState();
 
 		return newBaseVersion;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#getLocalChangePackage()
 	 */
 	public ChangePackage getLocalChangePackage(boolean canonize) {
 		ChangePackage changePackage = VersioningFactory.eINSTANCE.createChangePackage();
 		// copy operations from projectspace
 		for (AbstractOperation abstractOperation : getOperations()) {
 			AbstractOperation copy = EcoreUtil.copy(abstractOperation);
 			changePackage.getOperations().add(copy);
 		}
 		// copy events from projectspace
 		for (Event event : getEventsFromComposite()) {
 			Event copy = EcoreUtil.copy(event);
 			changePackage.getEvents().add(copy);
 		}
 
 		if (canonize) {
 			changePackage.cannonize();
 		}
 		return changePackage;
 	}
 
 	private void notifyPostCommitObservers(PrimaryVersionSpec newBaseVersion) {
 		for (CommitObserver observer : commitObservers) {
 			try {
 				observer.commitCompleted(this, newBaseVersion);
 				// BEGIN SUPRESS CATCH EXCEPTION
 			} catch (RuntimeException e) {
 				// END SUPRESS CATCH EXCEPTION
 				WorkspaceUtil.logException("CommitObserver failed with exception", e);
 			}
 		}
 	}
 
 	private void notifyPreCommitObservers(ChangePackage changePackage) {
 		for (CommitObserver observer : commitObservers) {
 			try {
 				observer.inspectChanges(this, changePackage);
 				// BEGIN SUPRESS CATCH EXCEPTION
 			} catch (RuntimeException e) {
 				// END SUPRESS CATCH EXCEPTION
 				WorkspaceUtil.logException("CommitObserver failed with exception", e);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#getOperations()
 	 */
 	public List<AbstractOperation> getOperations() {
 		// check if operation composite exists
 		OperationComposite operationComposite = this.getLocalOperations();
 		if (operationComposite == null) {
 			this.setLocalOperations(ModelFactory.eINSTANCE.createOperationComposite());
 			operationComposite = getLocalOperations();
 		}
 		if (isTransient) {
 			return operationComposite.getOperations();
 		}
 		if (operationsList == null) {
 			operationsList = new AutoSplitAndSaveResourceContainmentList<AbstractOperation>(operationComposite,
 				operationComposite.getOperations(), this.eResource().getResourceSet(),
 				Configuration.getWorkspaceDirectory() + "ps-" + getIdentifier() + File.separatorChar + "operations",
 				".off");
 		}
 		return operationsList;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#update()
 	 * @generated NOT
 	 */
 	public PrimaryVersionSpec update() throws EmfStoreException {
 		return update(VersionSpec.HEAD_VERSION);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#update(org.eclipse.emf.emfstore.server.model.versioning.VersionSpec)
 	 * @generated NOT
 	 */
 	public PrimaryVersionSpec update(final VersionSpec version) throws EmfStoreException {
 		return update(version, null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @generated NOT
 	 */
 	public List<ChangePackage> getChanges(VersionSpec sourceVersion, VersionSpec targetVersion)
 		throws EmfStoreException {
 		final ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 
 		List<ChangePackage> changes = connectionManager.getChanges(getUsersession().getSessionId(), projectId,
 			sourceVersion, targetVersion);
 		return changes;
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#update(org.eclipse.emf.emfstore.server.model.versioning.VersionSpec,
 	 *      org.eclipse.emf.emfstore.client.model.controller.UpdateCallback, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void update(VersionSpec version, UpdateCallback callback, IProgressMonitor progress) {
 		new UpdateController(this).update(version, callback, progress);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @throws EmfStoreException
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#update(org.eclipse.emf.emfstore.server.model.versioning.VersionSpec)
 	 * @generated NOT
 	 * @deprecated
 	 */
 	@Deprecated
 	public PrimaryVersionSpec update(final VersionSpec version, final UpdateObserver observer) throws EmfStoreException {
 		final ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 		final PrimaryVersionSpec resolvedVersion = resolveVersionSpec(version);
 
 		if (resolvedVersion.compareTo(baseVersion) == 0) {
 			throw new NoChangesOnServerException();
 		}
 
 		List<ChangePackage> changes = new ArrayList<ChangePackage>();
 
 		changes = connectionManager
 			.getChanges(getUsersession().getSessionId(), projectId, baseVersion, resolvedVersion);
 
 		ChangePackage localchanges = getLocalChangePackage(false);
 
 		ConflictDetector conflictDetector = new ConflictDetector();
 		for (ChangePackage change : changes) {
 			if (conflictDetector.doConflict(change, localchanges)) {
 				throw new ChangeConflictException(changes, this, conflictDetector);
 			}
 		}
 
 		// notify updateObserver if there is one
 		if (observer != null && !observer.inspectChanges(this, changes)) {
 			return getBaseVersion();
 		}
 
 		WorkspaceManager.getObserverBus().notify(UpdateObserver.class).inspectChanges(this, changes);
 
 		final List<ChangePackage> cps = changes;
 
 		// revert
 		this.revert();
 
 		// apply changes from repo
 		for (ChangePackage change : cps) {
 			applyOperations(change.getCopyOfOperations(), false);
 		}
 
 		// reapply local changes
 		applyOperations(localchanges.getCopyOfOperations(), true);
 
 		setBaseVersion(resolvedVersion);
 		saveProjectSpaceOnly();
 
 		// create notifications only if the project is updated to a newer
 		// version
 		if (resolvedVersion.compareTo(baseVersion) == 1) {
 			generateNotifications(changes);
 		}
 
 		// TODO Chainsaw. Do we need this anymore?
 		if (observer != null) {
 			observer.updateCompleted(this);
 		}
 		WorkspaceManager.getObserverBus().notify(UpdateObserver.class).updateCompleted(this);
 
 		// check for operations on file attachments: if version has been
 		// increased and file is required offline, add to
 		// pending file transfers
 		// checkUpdatedFileAttachments(changes);
 
 		return resolvedVersion;
 	}
 
 	// private void checkUpdatedFileAttachments(List<ChangePackage> changes) {
 	// List<FileAttachment> attachmentsToDownload = new
 	// LinkedList<FileAttachment>();
 	// for (ChangePackage change : changes) {
 	// EList<AbstractOperation> operations = change.getOperations();
 	// for (AbstractOperation operation : operations) {
 	// if
 	// (!OperationsPackage.eINSTANCE.getAttributeOperation().isInstance(operation))
 	// {
 	// continue;
 	// }
 	// AttributeOperation attributeOperation = (AttributeOperation) operation;
 	// if (!attributeOperation.getFeatureName().equals(
 	// AttachmentPackage.eINSTANCE.getFileAttachment_FileID().getName())) {
 	// continue;
 	// }
 	// ModelElement modelElement =
 	// getProject().getModelElement(operation.getModelElementId());
 	// if
 	// (AttachmentPackage.eINSTANCE.getFileAttachment().isInstance(modelElement))
 	// {
 	// FileAttachment fileAttachment = (FileAttachment)
 	// getProject().getModelElement(
 	// operation.getModelElementId());
 	// if (fileAttachment.isRequiredOffline()) {
 	// attachmentsToDownload.add((FileAttachment) modelElement);
 	// }
 	// }
 	// }
 	// }
 	// for (final FileAttachment fileAttachment : attachmentsToDownload) {
 	// final PendingFileTransfer transfer =
 	// WorkspaceFactoryImpl.eINSTANCE.createPendingFileTransfer();
 	// transfer.setAttachmentId(fileAttachment.getModelElementId());
 	// transfer.setChunkNumber(0);
 	// transfer.setFileVersion(Integer.parseInt(fileAttachment.getFileID()));
 	// transfer.setFileName(fileAttachment.getFileName());
 	// transfer.setPreliminaryFileName(null);
 	// transfer.setUpload(false);
 	// new EMFStoreCommand() {
 	// @Override
 	// protected void doRun() {
 	// fileAttachment.setDownloading(true);
 	// getPendingFileTransfers().add(transfer);
 	// }
 	// }.run();
 	// }
 	// }
 
 	private void generateNotifications(List<ChangePackage> changes) {
 		// generate notifications from change packages, ignore all exception if
 		// any
 		try {
 			List<ESNotification> newNotifications = NotificationGenerator.getInstance(this).generateNotifications(
 				changes, this.getUsersession().getUsername());
 			this.getNotificationsFromComposite().addAll(newNotifications);
 			// BEGIN SUPRESS CATCH EXCEPTION
 		} catch (RuntimeException e) {
 			// END SUPRESS CATCH EXCEPTION
 			WorkspaceUtil.logException("Creating notifications failed!", e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @generated NOT
 	 */
 	public void revert() {
 		while (!getOperations().isEmpty()) {
 			undoLastOperation();
 		}
 		updateDirtyState();
 	}
 
 	/**
 	 * Stops current recording of changes and adds recorded changes to this
 	 * project spaces changes.
 	 * 
 	 * @generated NOT
 	 */
 	public void stopChangeRecording() {
 		this.operationRecorder.stopChangeRecording();
 	}
 
 	/**
 	 * Starts change recording on this workspace, resumes previous recordings if
 	 * there are any.
 	 * 
 	 * @generated NOT
 	 */
 	public void startChangeRecording() {
 		this.operationRecorder.startChangeRecording();
 		updateDirtyState();
 	}
 
 	/**
 	 * Updates the dirty state of the project space.
 	 */
 	public void updateDirtyState() {
 		setDirty(!getOperations().isEmpty());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#init()
 	 * @generated NOT
 	 */
 	public void init() {
 		initCompleted = true;
 
 		// getProject().initCaches();
 
 		this.fileTransferManager = new FileTransferManager(this);
 		// EObjectChangeNotifier changeNotifier = new EObjectChangeNotifier(
 		// this.getProject());
 		this.operationRecorder = new OperationRecorder(this.getProject(),
 			((ProjectImpl) this.getProject()).getChangeNotifier());
 		this.operationManager = new OperationManager(operationRecorder, this);
 		this.operationManager.addOperationListener(modifiedModelElementsCache);
 		statePersister = new StatePersister(operationRecorder.getChangeNotifier(),
 			((EMFStoreCommandStack) Configuration.getEditingDomain().getCommandStack()), this.getProject());
 		// TODO: initialization order important
 		this.getProject().addProjectChangeObserver(this.operationRecorder);
 		this.getProject().addProjectChangeObserver(statePersister);
 
 		if (project instanceof ProjectImpl) {
 			((ProjectImpl) this.getProject()).setUndetachable(operationRecorder);
 			((ProjectImpl) this.getProject()).setUndetachable(statePersister);
 		}
 		if (getUsersession() != null) {
 			getUsersession().addLoginObserver(this);
 			ACUser acUser = getUsersession().getACUser();
 			if (acUser != null) {
 				for (OrgUnitProperty p : acUser.getProperties()) {
 					if (p.getProject() != null && p.getProject().equals(getProjectId())) {
 						propertyMap.put(p.getName(), p);
 					}
 				}
 			}
 		}
 		modifiedModelElementsCache.initializeCache();
 		startChangeRecording();
 		cleanCutElements();
 
 	}
 
 	private void cleanCutElements() {
 		for (EObject cutElement : getProject().getCutElements()) {
 			project.deleteModelElement(cutElement);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#getProjectInfo()
 	 * @generated NOT
 	 */
 	public ProjectInfo getProjectInfo() {
 		ProjectInfo projectInfo = org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE.createProjectInfo();
 		projectInfo.setProjectId(ModelUtil.clone(getProjectId()));
 		projectInfo.setName(getProjectName());
 		projectInfo.setDescription(getProjectDescription());
 		projectInfo.setVersion(ModelUtil.clone(getBaseVersion()));
 		return projectInfo;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#resolveVersionSpec(org.eclipse.emf.emfstore.server.model.versioning.VersionSpec)
 	 * @throws EmfStoreException
 	 * @generated NOT
 	 */
 	public PrimaryVersionSpec resolveVersionSpec(VersionSpec versionSpec) throws EmfStoreException {
 		ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 		return connectionManager.resolveVersionSpec(getUsersession().getSessionId(), getProjectId(), versionSpec);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#initResources(org.eclipse.emf.ecore.resource.ResourceSet)
 	 * @generated NOT
 	 */
 	public void initResources(ResourceSet resourceSet) {
 		initCompleted = true;
 		String projectSpaceFileNamePrefix = Configuration.getWorkspaceDirectory()
 			+ Configuration.getProjectSpaceDirectoryPrefix() + getIdentifier() + File.separatorChar;
		String projectSpaceFileName = projectSpaceFileNamePrefix + this.getIdentifier()
 			+ Configuration.getProjectSpaceFileExtension();
		String operationsCompositeFileName = projectSpaceFileNamePrefix + this.getIdentifier()
 			+ Configuration.getOperationCompositeFileExtension();
 		String projectFragementsFileNamePrefix = projectSpaceFileNamePrefix + Configuration.getProjectFolderName()
 			+ File.separatorChar;
 		URI projectSpaceURI = URI.createFileURI(projectSpaceFileName);
 		URI operationCompositeURI = URI.createFileURI(operationsCompositeFileName);
 
 		setResourceCount(0);
 		String fileName = projectFragementsFileNamePrefix + getResourceCount()
 			+ Configuration.getProjectFragmentFileExtension();
 		URI fileURI = URI.createFileURI(fileName);
 
 		List<Resource> resources = new ArrayList<Resource>();
 		Resource resource = resourceSet.createResource(fileURI);
 		// if resource splitting fails, we need a reference to the old resource
 		resource.getContents().add(this.getProject());
 		resources.add(resource);
 		setResourceCount(getResourceCount() + 1);
 
 		if (Configuration.isResourceSplittingEnabled()) {
 			splitResources(resourceSet, projectFragementsFileNamePrefix, resources, this.getProject());
 		} else {
 			for (EObject modelElement : project.getAllModelElements())
 				((XMIResource) resource).setID(modelElement, getProject().getModelElementId(modelElement).getId());
 		}
 
 		Resource operationCompositeResource = resourceSet.createResource(operationCompositeURI);
 		if (this.getLocalOperations() == null) {
 			this.setLocalOperations(ModelFactory.eINSTANCE.createOperationComposite());
 		}
 		operationCompositeResource.getContents().add(this.getLocalOperations());
 		resources.add(operationCompositeResource);
 
 		Resource projectSpaceResource = resourceSet.createResource(projectSpaceURI);
 		projectSpaceResource.getContents().add(this);
 		resources.add(projectSpaceResource);
 
 		// save all resources that have been created
 		for (Resource currentResource : resources) {
 			try {
 				currentResource.save(Configuration.getResourceSaveOptions());
 			} catch (IOException e) {
 				WorkspaceUtil.logException("Project Space resource init failed!", e);
 
 			}
 		}
 
 		init();
 	}
 
 	private void splitResources(ResourceSet resourceSet, String projectFragementsFileNamePrefix,
 		List<Resource> resources, Project project) {
 		String fileName;
 		URI fileURI;
 
 		Resource resource = project.eResource();
 		int counter = 0;
 		for (EObject modelElement : project.getAllModelElements()) {
 
 			// never split maps
 			if (modelElement instanceof BasicEMap.Entry) {
 				((XMIResource) modelElement.eContainer().eResource()).setID(modelElement, getProject()
 					.getModelElementId(modelElement).getId());
 				continue;
 			}
 
 			if (counter > Configuration.getMaxMECountPerResource()) {
 				fileName = projectFragementsFileNamePrefix + getResourceCount()
 					+ Configuration.getProjectFragmentFileExtension();
 				fileURI = URI.createFileURI(fileName);
 				resource = resourceSet.createResource(fileURI);
 				setResourceCount(getResourceCount() + 1);
 				resources.add(resource);
 				counter = 0;
 			}
 			counter++;
 
 			assignElementToResource(resource, modelElement);
 		}
 	}
 
 	private void assignElementToResource(Resource resource, EObject modelElement) {
 		resource.getContents().add(modelElement);
 		// FIXME: this is not nice!
 		((XMIResource) resource).setID(modelElement, getProject().getModelElementId(modelElement).getId());
 	}
 
 	// end of custom code
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 		case ModelPackage.PROJECT_SPACE__PROJECT:
 			return basicSetProject(null, msgs);
 		case ModelPackage.PROJECT_SPACE__PROJECT_ID:
 			return basicSetProjectId(null, msgs);
 		case ModelPackage.PROJECT_SPACE__EVENTS:
 			return ((InternalEList<?>) getEvents()).basicRemove(otherEnd, msgs);
 		case ModelPackage.PROJECT_SPACE__BASE_VERSION:
 			return basicSetBaseVersion(null, msgs);
 		case ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS:
 			return basicSetLocalOperations(null, msgs);
 		case ModelPackage.PROJECT_SPACE__NOTIFICATIONS:
 			return ((InternalEList<?>) getNotifications()).basicRemove(otherEnd, msgs);
 		case ModelPackage.PROJECT_SPACE__EVENT_COMPOSITE:
 			return basicSetEventComposite(null, msgs);
 		case ModelPackage.PROJECT_SPACE__NOTIFICATION_COMPOSITE:
 			return basicSetNotificationComposite(null, msgs);
 		case ModelPackage.PROJECT_SPACE__WAITING_UPLOADS:
 			return ((InternalEList<?>) getWaitingUploads()).basicRemove(otherEnd, msgs);
 		case ModelPackage.PROJECT_SPACE__PROPERTIES:
 			return ((InternalEList<?>) getProperties()).basicRemove(otherEnd, msgs);
 		case ModelPackage.PROJECT_SPACE__CHANGED_SHARED_PROPERTIES:
 			return ((InternalEList<?>) getChangedSharedProperties()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 		case ModelPackage.PROJECT_SPACE__PROJECT:
 			if (resolve)
 				return getProject();
 			return basicGetProject();
 		case ModelPackage.PROJECT_SPACE__PROJECT_ID:
 			if (resolve)
 				return getProjectId();
 			return basicGetProjectId();
 		case ModelPackage.PROJECT_SPACE__PROJECT_NAME:
 			return getProjectName();
 		case ModelPackage.PROJECT_SPACE__PROJECT_DESCRIPTION:
 			return getProjectDescription();
 		case ModelPackage.PROJECT_SPACE__EVENTS:
 			return getEvents();
 		case ModelPackage.PROJECT_SPACE__USERSESSION:
 			if (resolve)
 				return getUsersession();
 			return basicGetUsersession();
 		case ModelPackage.PROJECT_SPACE__LAST_UPDATED:
 			return getLastUpdated();
 		case ModelPackage.PROJECT_SPACE__BASE_VERSION:
 			if (resolve)
 				return getBaseVersion();
 			return basicGetBaseVersion();
 		case ModelPackage.PROJECT_SPACE__RESOURCE_COUNT:
 			return getResourceCount();
 		case ModelPackage.PROJECT_SPACE__DIRTY:
 			return isDirty();
 		case ModelPackage.PROJECT_SPACE__OLD_LOG_MESSAGES:
 			return getOldLogMessages();
 		case ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS:
 			if (resolve)
 				return getLocalOperations();
 			return basicGetLocalOperations();
 		case ModelPackage.PROJECT_SPACE__NOTIFICATIONS:
 			return getNotifications();
 		case ModelPackage.PROJECT_SPACE__EVENT_COMPOSITE:
 			if (resolve)
 				return getEventComposite();
 			return basicGetEventComposite();
 		case ModelPackage.PROJECT_SPACE__NOTIFICATION_COMPOSITE:
 			if (resolve)
 				return getNotificationComposite();
 			return basicGetNotificationComposite();
 		case ModelPackage.PROJECT_SPACE__WAITING_UPLOADS:
 			return getWaitingUploads();
 		case ModelPackage.PROJECT_SPACE__PROPERTIES:
 			if (coreType)
 				return getProperties();
 			else
 				return getProperties().map();
 		case ModelPackage.PROJECT_SPACE__CHANGED_SHARED_PROPERTIES:
 			if (coreType)
 				return getChangedSharedProperties();
 			else
 				return getChangedSharedProperties().map();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 		case ModelPackage.PROJECT_SPACE__PROJECT:
 			setProject((Project) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__PROJECT_ID:
 			setProjectId((ProjectId) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__PROJECT_NAME:
 			setProjectName((String) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__PROJECT_DESCRIPTION:
 			setProjectDescription((String) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__EVENTS:
 			getEvents().clear();
 			getEvents().addAll((Collection<? extends Event>) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__USERSESSION:
 			setUsersession((Usersession) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__LAST_UPDATED:
 			setLastUpdated((Date) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__BASE_VERSION:
 			setBaseVersion((PrimaryVersionSpec) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__RESOURCE_COUNT:
 			setResourceCount((Integer) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__DIRTY:
 			setDirty((Boolean) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__OLD_LOG_MESSAGES:
 			getOldLogMessages().clear();
 			getOldLogMessages().addAll((Collection<? extends String>) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS:
 			setLocalOperations((OperationComposite) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__NOTIFICATIONS:
 			getNotifications().clear();
 			getNotifications().addAll((Collection<? extends ESNotification>) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__EVENT_COMPOSITE:
 			setEventComposite((EventComposite) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__NOTIFICATION_COMPOSITE:
 			setNotificationComposite((NotificationComposite) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__WAITING_UPLOADS:
 			getWaitingUploads().clear();
 			getWaitingUploads().addAll((Collection<? extends FileIdentifier>) newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__PROPERTIES:
 			((EStructuralFeature.Setting) getProperties()).set(newValue);
 			return;
 		case ModelPackage.PROJECT_SPACE__CHANGED_SHARED_PROPERTIES:
 			((EStructuralFeature.Setting) getChangedSharedProperties()).set(newValue);
 			return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 		case ModelPackage.PROJECT_SPACE__PROJECT:
 			setProject((Project) null);
 			return;
 		case ModelPackage.PROJECT_SPACE__PROJECT_ID:
 			setProjectId((ProjectId) null);
 			return;
 		case ModelPackage.PROJECT_SPACE__PROJECT_NAME:
 			setProjectName(PROJECT_NAME_EDEFAULT);
 			return;
 		case ModelPackage.PROJECT_SPACE__PROJECT_DESCRIPTION:
 			setProjectDescription(PROJECT_DESCRIPTION_EDEFAULT);
 			return;
 		case ModelPackage.PROJECT_SPACE__EVENTS:
 			getEvents().clear();
 			return;
 		case ModelPackage.PROJECT_SPACE__USERSESSION:
 			setUsersession((Usersession) null);
 			return;
 		case ModelPackage.PROJECT_SPACE__LAST_UPDATED:
 			setLastUpdated(LAST_UPDATED_EDEFAULT);
 			return;
 		case ModelPackage.PROJECT_SPACE__BASE_VERSION:
 			setBaseVersion((PrimaryVersionSpec) null);
 			return;
 		case ModelPackage.PROJECT_SPACE__RESOURCE_COUNT:
 			setResourceCount(RESOURCE_COUNT_EDEFAULT);
 			return;
 		case ModelPackage.PROJECT_SPACE__DIRTY:
 			setDirty(DIRTY_EDEFAULT);
 			return;
 		case ModelPackage.PROJECT_SPACE__OLD_LOG_MESSAGES:
 			getOldLogMessages().clear();
 			return;
 		case ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS:
 			setLocalOperations((OperationComposite) null);
 			return;
 		case ModelPackage.PROJECT_SPACE__NOTIFICATIONS:
 			getNotifications().clear();
 			return;
 		case ModelPackage.PROJECT_SPACE__EVENT_COMPOSITE:
 			setEventComposite((EventComposite) null);
 			return;
 		case ModelPackage.PROJECT_SPACE__NOTIFICATION_COMPOSITE:
 			setNotificationComposite((NotificationComposite) null);
 			return;
 		case ModelPackage.PROJECT_SPACE__WAITING_UPLOADS:
 			getWaitingUploads().clear();
 			return;
 		case ModelPackage.PROJECT_SPACE__PROPERTIES:
 			getProperties().clear();
 			return;
 		case ModelPackage.PROJECT_SPACE__CHANGED_SHARED_PROPERTIES:
 			getChangedSharedProperties().clear();
 			return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 		case ModelPackage.PROJECT_SPACE__PROJECT:
 			return project != null;
 		case ModelPackage.PROJECT_SPACE__PROJECT_ID:
 			return projectId != null;
 		case ModelPackage.PROJECT_SPACE__PROJECT_NAME:
 			return PROJECT_NAME_EDEFAULT == null ? projectName != null : !PROJECT_NAME_EDEFAULT.equals(projectName);
 		case ModelPackage.PROJECT_SPACE__PROJECT_DESCRIPTION:
 			return PROJECT_DESCRIPTION_EDEFAULT == null ? projectDescription != null : !PROJECT_DESCRIPTION_EDEFAULT
 				.equals(projectDescription);
 		case ModelPackage.PROJECT_SPACE__EVENTS:
 			return events != null && !events.isEmpty();
 		case ModelPackage.PROJECT_SPACE__USERSESSION:
 			return usersession != null;
 		case ModelPackage.PROJECT_SPACE__LAST_UPDATED:
 			return LAST_UPDATED_EDEFAULT == null ? lastUpdated != null : !LAST_UPDATED_EDEFAULT.equals(lastUpdated);
 		case ModelPackage.PROJECT_SPACE__BASE_VERSION:
 			return baseVersion != null;
 		case ModelPackage.PROJECT_SPACE__RESOURCE_COUNT:
 			return resourceCount != RESOURCE_COUNT_EDEFAULT;
 		case ModelPackage.PROJECT_SPACE__DIRTY:
 			return dirty != DIRTY_EDEFAULT;
 		case ModelPackage.PROJECT_SPACE__OLD_LOG_MESSAGES:
 			return oldLogMessages != null && !oldLogMessages.isEmpty();
 		case ModelPackage.PROJECT_SPACE__LOCAL_OPERATIONS:
 			return localOperations != null;
 		case ModelPackage.PROJECT_SPACE__NOTIFICATIONS:
 			return notifications != null && !notifications.isEmpty();
 		case ModelPackage.PROJECT_SPACE__EVENT_COMPOSITE:
 			return eventComposite != null;
 		case ModelPackage.PROJECT_SPACE__NOTIFICATION_COMPOSITE:
 			return notificationComposite != null;
 		case ModelPackage.PROJECT_SPACE__WAITING_UPLOADS:
 			return waitingUploads != null && !waitingUploads.isEmpty();
 		case ModelPackage.PROJECT_SPACE__PROPERTIES:
 			return properties != null && !properties.isEmpty();
 		case ModelPackage.PROJECT_SPACE__CHANGED_SHARED_PROPERTIES:
 			return changedSharedProperties != null && !changedSharedProperties.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy())
 			return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (projectName: ");
 		result.append(projectName);
 		result.append(", projectDescription: ");
 		result.append(projectDescription);
 		result.append(", lastUpdated: ");
 		result.append(lastUpdated);
 		result.append(", resourceCount: ");
 		result.append(resourceCount);
 		result.append(", dirty: ");
 		result.append(dirty);
 		result.append(", oldLogMessages: ");
 		result.append(oldLogMessages);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @throws EmfStoreException
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#shareProject(org.eclipse.emf.emfstore.client.model.Usersession)
 	 * @generated NOT
 	 */
 	public void shareProject(Usersession usersession) throws EmfStoreException {
 		this.setUsersession(usersession);
 		usersession.addLoginObserver(this);
 		LogMessage logMessage = VersioningFactory.eINSTANCE.createLogMessage();
 		logMessage.setAuthor(usersession.getUsername());
 		logMessage.setClientDate(new Date());
 		logMessage.setMessage("Initial commit");
 		ProjectInfo createdProject;
 
 		stopChangeRecording();
 		statePersister.setAutoSave(false);
 
 		// TODO: PlainEObjectMode: Set user as creator when sharing a project
 		// for (EObject me : this.getProject().getAllModelElements()) {
 		// if (me.getCreator() == null || me.getCreator().equals("")
 		// || me.getCreator().equals(ProjectChangeTracker.UNKOWN_CREATOR)) {
 		// me.setCreator(usersession.getUsername());
 		// changeTracker.save(me);
 		// }
 		// }
 
 		createdProject = WorkspaceManager
 			.getInstance()
 			.getConnectionManager()
 			.createProject(usersession.getSessionId(), this.getProjectName(), this.getProjectDescription(), logMessage,
 				this.getProject());
 		statePersister.setAutoSave(true);
 		statePersister.saveDirtyResources();
 		startChangeRecording();
 		this.setBaseVersion(createdProject.getVersion());
 		this.setLastUpdated(new Date());
 		this.setProjectId(createdProject.getProjectId());
 		this.saveProjectSpaceOnly();
 
 		// If any files have already been added, upload them.
 		fileTransferManager.uploadQueuedFiles(new NullProgressMonitor());
 
 		notifyShareObservers();
 		getOperations().clear();
 		usersession.updateProjectInfos();
 		updateDirtyState();
 
 	}
 
 	private void notifyShareObservers() {
 		for (ShareObserver shareObserver : shareObservers) {
 			try {
 				shareObserver.shareDone();
 				// BEGIN SUPRESS CATCH EXCEPTION
 			} catch (RuntimeException e) {
 				// END SUPRESS CATCH EXCEPTION
 				WorkspaceUtil.logException("ShareObserver failed with exception", e);
 			}
 		}
 	}
 
 	/**
 	 * Saves the project space itself only, no containment children.
 	 */
 	public void saveProjectSpaceOnly() {
 		saveResource(this.eResource());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#exportProject(java.lang.String)
 	 */
 	public void exportProject(String absoluteFileName) throws IOException {
 		WorkspaceManager.getInstance().getCurrentWorkspace().exportProject(this, absoluteFileName);
 	}
 
 	/**
 	 * Save the given resource that is part of the project space resource set.
 	 * 
 	 * @param resource
 	 *            the resource
 	 */
 	public void saveResource(Resource resource) {
 		try {
 			if (resource == null) {
 				if (!isTransient) {
 					WorkspaceUtil.logException("Resources of project space are not properly initialized!",
 						new IllegalProjectSpaceStateException("Resource to save is null"));
 				}
 				return;
 			}
 			resource.save(Configuration.getResourceSaveOptions());
 		} catch (IOException e) {
 			WorkspaceUtil
 				.logException(
 					"An error in the data was detected during save! The safest way to deal with this problem is to delete this project and checkout again.",
 					e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#exportLocalChanges(java.lang.String)
 	 */
 	public void exportLocalChanges(String fileName) throws IOException {
 		ResourceHelper.putElementIntoNewResourceWithProject(fileName, getLocalChangePackage(false), this.project);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#importLocalChanges(java.lang.String)
 	 */
 	public void importLocalChanges(String fileName) throws IOException {
 
 		ResourceSetImpl resourceSet = new ResourceSetImpl();
 		Resource resource = resourceSet.getResource(URI.createFileURI(fileName), true);
 		EList<EObject> directContents = resource.getContents();
 		// sanity check
 
 		if (directContents.size() != 1 && (!(directContents.get(0) instanceof ChangePackage))) {
 			throw new IOException("File is corrupt, does not contain Changes.");
 		}
 
 		ChangePackage changePackage = (ChangePackage) directContents.get(0);
 		applyOperationsWithRecording(changePackage.getOperations(), true);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#undoLastOperation()
 	 */
 	public void undoLastOperation() {
 		if (!this.getOperations().isEmpty()) {
 			List<AbstractOperation> operations = this.getOperations();
 			AbstractOperation lastOperation = operations.get(operations.size() - 1);
 			stopChangeRecording();
 			try {
 				lastOperation.reverse().apply(getProject());
 				operationManager.notifyOperationUndone(lastOperation);
 			} catch (RuntimeException exception) {
 				WorkspaceUtil.handleException(exception);
 			} finally {
 				startChangeRecording();
 			}
 			operations.remove(lastOperation);
 		}
 		updateDirtyState();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#addEvent(org.eclipse.emf.emfstore.server.model.versioning.events.Event)
 	 */
 	public void addEvent(Event event) {
 		if (event.getTimestamp() == null) {
 			event.setTimestamp(new Date());
 		}
 		this.getEventsFromComposite().add(event);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @generated NOT
 	 */
 	public void addTag(PrimaryVersionSpec versionSpec, TagVersionSpec tag) throws EmfStoreException {
 		final ConnectionManager cm = WorkspaceManager.getInstance().getConnectionManager();
 		cm.addTag(getUsersession().getSessionId(), getProjectId(), versionSpec, tag);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @generated NOT
 	 */
 	public void removeTag(PrimaryVersionSpec versionSpec, TagVersionSpec tag) throws EmfStoreException {
 		final ConnectionManager cm = WorkspaceManager.getInstance().getConnectionManager();
 		cm.removeTag(getUsersession().getSessionId(), getProjectId(), versionSpec, tag);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#resolve(org.eclipse.emf.emfstore.server.model.url.ModelElementUrlFragment)
 	 */
 	public EObject resolve(ModelElementUrlFragment modelElementUrlFragment) throws MEUrlResolutionException {
 		ModelElementId modelElementId = modelElementUrlFragment.getModelElementId();
 		EObject modelElement = getProject().getModelElement(modelElementId);
 		if (modelElement == null) {
 			throw new MEUrlResolutionException();
 		}
 		return modelElement;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#makeTransient()
 	 */
 	public void makeTransient() {
 		if (initCompleted) {
 			throw new IllegalAccessError("Project Space cannot be set to transient after init.");
 		}
 		isTransient = true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @deprecated
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#applyMergeResult(java.util.List)
 	 */
 	@Deprecated
 	public void applyMergeResult(List<AbstractOperation> mergeResult, VersionSpec mergeTargetSpec)
 		throws EmfStoreException {
 		revert();
 		update(mergeTargetSpec);
 
 		applyOperations(mergeResult);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#addCommitObserver(org.eclipse.emf.emfstore.client.model.observers.CommitObserver)
 	 */
 	public void addCommitObserver(CommitObserver observer) {
 		this.commitObservers.add(observer);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void loginCompleted(Usersession session) {
 		try {
 			transmitProperties();
 			// BEGIN SUPRESS CATCH EXCEPTION
 		} catch (RuntimeException e) {
 			// END SUPRESS CATCH EXCEPTION
 			WorkspaceUtil.logException("Resuming file transfers or transmitting properties failed!", e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void merge(PrimaryVersionSpec target, ConflictResolver conflictResolver) throws EmfStoreException {
 		// merge the conflicts
 		ChangePackage myCp = this.getLocalChangePackage(true);
 		List<ChangePackage> theirCps = this.getChanges(getBaseVersion(), target);
 		if (conflictResolver.resolveConflicts(project, theirCps, myCp, getBaseVersion(), target)) {
 
 			// revert the local operations and apply all their operations
 			this.revert();
 
 			for (ChangePackage changePackage : theirCps) {
 				applyOperations(changePackage.getOperations(), false);
 			}
 
 			// generate merge result and apply to local workspace
 			List<AbstractOperation> acceptedMine = conflictResolver.getAcceptedMine();
 			List<AbstractOperation> rejectedTheirs = conflictResolver.getRejectedTheirs();
 			List<AbstractOperation> mergeResult = new ArrayList<AbstractOperation>();
 			for (AbstractOperation operationToReverse : rejectedTheirs) {
 				mergeResult.add(0, operationToReverse.reverse());
 			}
 			mergeResult.addAll(acceptedMine);
 
 			applyOperations(mergeResult, true);
 
 			this.setBaseVersion(target);
 
 			saveProjectSpaceOnly();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#getModifiedModelElementsCache()
 	 */
 	public ModifiedModelElementsCache getModifiedModelElementsCache() {
 		return modifiedModelElementsCache;
 	}
 
 	// /**
 	// * {@inheritDoc}
 	// *
 	// * @param operationListener
 	// */
 	// public void addOperationListener(OperationListener operationListener) {
 	// this.operationListeners.add(operationListener);
 	// }
 
 	// private void notifyOperationUndone(AbstractOperation operation) {
 	// for (OperationListener operationListener : operationListeners) {
 	// operationListener.operationUnDone(operation);
 	// }
 	// }
 
 	// /**
 	// * {@inheritDoc}
 	// *
 	// * @param operationListner
 	// */
 	// public void removeOperationListener(OperationListener operationListner) {
 	// this.operationListeners.remove(operationListner);
 	//
 	// }
 
 	/**
 	 * Notify the operation observer that an operation has just completed.
 	 * 
 	 * @param operation
 	 *            the operation
 	 */
 	// void notifyOperationExecuted(AbstractOperation operation) {
 	// for (OperationListener operationListener : operationListeners) {
 	// operationListener.operationExecuted(operation);
 	// }
 	// }
 
 	/**
 	 * Add operation to the project spaces local operations.
 	 * 
 	 * @param operation
 	 *            the operation
 	 */
 	public void addOperation(AbstractOperation operation) {
 		this.getOperations().add(operation);
 		updateDirtyState();
 
 		// do not notify on composite start, wait until completion
 		if (operation instanceof CompositeOperation) {
 			// check of automatic composite if yes then continue
 			if (((CompositeOperation) operation).getMainOperation() == null) {
 				return;
 			}
 		}
 		operationManager.notifyOperationExecuted(operation);
 		// this.notifyOperationExecuted(operation);
 	}
 
 	/**
 	 * Get the current nofitication recorder.
 	 * 
 	 * @return the recorder
 	 */
 	public NotificationRecorder getNotificationRecorder() {
 		return this.operationRecorder.getNotificationRecorder();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#beginCompositeOperation()
 	 */
 	public CompositeOperationHandle beginCompositeOperation() {
 		return this.operationManager.beginCompositeOperation();
 	}
 
 	/**
 	 * Apply a list of operations to the project.
 	 * 
 	 * @param operations
 	 *            the list of operations
 	 */
 	public void applyOperations(List<AbstractOperation> operations) {
 		applyOperations(operations, true);
 	}
 
 	/**
 	 * Applies a list of operations to the project. The change tracking is
 	 * stopped and the operations are added to the projectspace.
 	 * 
 	 * @see #applyOperationsWithRecording(List, boolean)
 	 * @param operations
 	 *            list of operations
 	 * @param addOperation
 	 *            true if operation should be saved in project space.
 	 */
 	public void applyOperations(List<AbstractOperation> operations, boolean addOperation) {
 		stopChangeRecording();
 		try {
 			for (AbstractOperation operation : operations) {
 				try {
 					operation.apply(getProject());
 				} catch (RuntimeException e) {
 					WorkspaceUtil.handleException(e);
 				}
 
 				if (addOperation) {
 					addOperation(operation);
 				}
 			}
 		} finally {
 			startChangeRecording();
 		}
 	}
 
 	/**
 	 * Applies a list of operations to the project. It is possible to force
 	 * import operations. Changetracking isn't deactivated while applying
 	 * changes.
 	 * 
 	 * @param operations
 	 *            list of operations
 	 * @param force
 	 *            if true, no exception is thrown if operation.apply failes
 	 * @param semanticApply
 	 *            when true, does a semanticApply if possible (see {@link SemanticCompositeOperation})
 	 */
 	public void applyOperationsWithRecording(List<AbstractOperation> operations, boolean force, boolean semanticApply) {
 		for (AbstractOperation operation : operations) {
 			try {
 				if (semanticApply && operation instanceof SemanticCompositeOperation) {
 					((SemanticCompositeOperation) operation).semanticApply(getProject());
 				} else {
 					operation.apply(getProject());
 				}
 			} catch (IllegalStateException e) {
 				if (!force) {
 					throw e;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Applies a list of operations to the project. This method is used by {@link #importLocalChanges(String)}. This
 	 * method redirects to {@link #applyOperationsWithRecording(List, boolean, boolean)}, using
 	 * false for semantic apply.
 	 * 
 	 * @param operations
 	 *            list of operations
 	 * @param force
 	 *            if true, no exception is thrown if operation.apply failes
 	 */
 	public void applyOperationsWithRecording(List<AbstractOperation> operations, boolean force) {
 		applyOperationsWithRecording(operations, force, false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public OrgUnitProperty getProperty(PropertyKey name) throws PropertyNotFoundException {
 		return getProperty(name.toString());
 	}
 
 	/**
 	 * getter for a string argument - see {@link #setProperty(OrgUnitProperty)}.
 	 */
 	private OrgUnitProperty getProperty(String name) throws PropertyNotFoundException {
 		// sanity checks
 		if (getUsersession() != null && getUsersession().getACUser() != null) {
 			OrgUnitProperty orgUnitProperty = propertyMap.get(name);
 			if (orgUnitProperty != null) {
 				return orgUnitProperty;
 			}
 		}
 		throw new PropertyNotFoundException();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setProperty(OrgUnitProperty property) {
 		// sanity checks
 		if (getUsersession() != null && getUsersession().getACUser() != null) {
 			try {
 				if (property.getProject() == null) {
 					property.setProject(ModelUtil.clone(getProjectId()));
 				} else if (!property.getProject().equals(getProjectId())) {
 					return;
 				}
 				OrgUnitProperty prop = getProperty(property.getName());
 				prop.setValue(property.getValue());
 			} catch (PropertyNotFoundException e) {
 				getUsersession().getACUser().getProperties().add(property);
 				propertyMap.put(property.getName(), property);
 			}
 			// the properties that have been altered are retained in a separate
 			// list
 			for (OrgUnitProperty changedProperty : getUsersession().getChangedProperties()) {
 				if (changedProperty.getName().equals(property.getName())
 					&& changedProperty.getProject().equals(getProjectId())) {
 					changedProperty.setValue(property.getValue());
 					WorkspaceManager.getInstance().getCurrentWorkspace().save();
 					return;
 				}
 			}
 			getUsersession().getChangedProperties().add(property);
 			WorkspaceManager.getInstance().getCurrentWorkspace().save();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean hasProperty(PropertyKey key) {
 		return propertyMap.containsKey(key.toString());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void transmitProperties() {
 		List<OrgUnitProperty> temp = new ArrayList<OrgUnitProperty>();
 		for (OrgUnitProperty changedProperty : getUsersession().getChangedProperties()) {
 			if (changedProperty.getProject() != null && changedProperty.getProject().equals(getProjectId())) {
 				temp.add(changedProperty);
 			}
 		}
 		ListIterator<OrgUnitProperty> iterator = temp.listIterator();
 		while (iterator.hasNext()) {
 			try {
 				WorkspaceManager
 					.getInstance()
 					.getConnectionManager()
 					.transmitProperty(getUsersession().getSessionId(), iterator.next(), getUsersession().getACUser(),
 						getProjectId());
 				iterator.remove();
 			} catch (EmfStoreException e) {
 				WorkspaceUtil.logException("Transmission of properties failed with exception", e);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isTransient() {
 		return isTransient;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#commit()
 	 */
 	public PrimaryVersionSpec commit() throws EmfStoreException {
 		LogMessage logMessage = VersioningFactory.eINSTANCE.createLogMessage();
 		String commiter = "UNKOWN";
 		if (this.getUsersession().getACUser() != null && this.getUsersession().getACUser().getName() != null) {
 			commiter = this.getUsersession().getACUser().getName();
 		}
 		logMessage.setAuthor(commiter);
 		logMessage.setClientDate(new Date());
 		logMessage.setMessage("");
 		return commit(logMessage);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#removeCommitObserver(org.eclipse.emf.emfstore.client.model.observers.CommitObserver)
 	 */
 	public void removeCommitObserver(CommitObserver observer) {
 		this.commitObservers.remove(observer);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#addFile(java.io.File)
 	 */
 	public FileIdentifier addFile(File file) throws FileTransferException {
 		return fileTransferManager.addFile(file);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#getFile(org.eclipse.emf.emfstore.server.model.FileIdentifier,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public FileDownloadStatus getFile(FileIdentifier fileIdentifier) throws FileTransferException {
 		return fileTransferManager.getFile(fileIdentifier);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#getFileInfo(org.eclipse.emf.emfstore.server.model.FileIdentifier)
 	 */
 	public FileInformation getFileInfo(FileIdentifier fileIdentifier) {
 		return fileTransferManager.getFileInfo(fileIdentifier);
 	}
 
 	// TODO: EM, needed?
 	// @Override
 	public OperationManager getOperationManager() {
 		return operationManager;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.ProjectSpace#getPropertyManager()
 	 */
 	public PropertyManager getPropertyManager() {
 		if (this.propertyManager == null) {
 			this.propertyManager = new PropertyManager(this);
 		}
 
 		return this.propertyManager;
 	}
 
 } // ProjectContainerImpl
