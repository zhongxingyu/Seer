 /**
  * <copyright>
  *******************************************************************************
  * Copyright (c) 2004 Eteration Bilisim A.S.
  * All rights reserved.  This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL ETERATION A.S. OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Eteration Bilisim A.S.  For more
  * information on eteration, please see
  * <http://www.eteration.com/>.
  ***************************************************************************
  * </copyright>
  *
 * $Id: ServerRuntimeImpl.java,v 1.2 2004/12/02 17:56:53 ndai Exp $
  */
 package org.eclipse.jst.server.generic.servertype.definition.impl;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 
 import org.eclipse.emf.ecore.util.BasicFeatureMap;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 import org.eclipse.jst.server.generic.internal.xml.Resolver;
 import org.eclipse.jst.server.generic.servertype.definition.Classpath;
 import org.eclipse.jst.server.generic.servertype.definition.LaunchConfiguration;
 import org.eclipse.jst.server.generic.servertype.definition.Module;
 import org.eclipse.jst.server.generic.servertype.definition.Project;
 import org.eclipse.jst.server.generic.servertype.definition.Publisher;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.jst.server.generic.servertype.definition.ServerTypePackage;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Server Runtime</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getGroup <em>Group</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getProperty <em>Property</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getGroup1 <em>Group1</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getPort <em>Port</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getGroup2 <em>Group2</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getModule <em>Module</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getProject <em>Project</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getStart <em>Start</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getStop <em>Stop</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getGroup3 <em>Group3</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getPublisher <em>Publisher</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getGroup4 <em>Group4</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getClasspath <em>Classpath</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getName <em>Name</em>}</li>
  *   <li>{@link org.eclipse.jst.server.generic.servertype.definition.impl.ServerRuntimeImpl#getVersion <em>Version</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ServerRuntimeImpl extends EObjectImpl implements ServerRuntime {
 	/**
 	 * The cached value of the '{@link #getGroup() <em>Group</em>}' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getGroup()
 	 * @generated
 	 * @ordered
 	 */
 	protected FeatureMap group = null;
 
 	/**
 	 * The cached value of the '{@link #getGroup1() <em>Group1</em>}' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getGroup1()
 	 * @generated
 	 * @ordered
 	 */
 	protected FeatureMap group1 = null;
 
 	/**
 	 * The cached value of the '{@link #getGroup2() <em>Group2</em>}' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getGroup2()
 	 * @generated
 	 * @ordered
 	 */
 	protected FeatureMap group2 = null;
 
 	/**
 	 * The cached value of the '{@link #getProject() <em>Project</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getProject()
 	 * @generated
 	 * @ordered
 	 */
 	protected Project project = null;
 
 	/**
 	 * The cached value of the '{@link #getStart() <em>Start</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getStart()
 	 * @generated
 	 * @ordered
 	 */
 	protected LaunchConfiguration start = null;
 
 	/**
 	 * The cached value of the '{@link #getStop() <em>Stop</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getStop()
 	 * @generated
 	 * @ordered
 	 */
 	protected LaunchConfiguration stop = null;
 
 	/**
 	 * The cached value of the '{@link #getGroup3() <em>Group3</em>}' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getGroup3()
 	 * @generated
 	 * @ordered
 	 */
 	protected FeatureMap group3 = null;
 
 	/**
 	 * The cached value of the '{@link #getGroup4() <em>Group4</em>}' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getGroup4()
 	 * @generated
 	 * @ordered
 	 */
 	protected FeatureMap group4 = null;
 
 	/**
 	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String name = NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String VERSION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected String version = VERSION_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getGroup() <em>Group</em>}' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getGroup()
 	 * @generated NOT
 	 * @ordered
 	 */
 	protected String filename = null;
 	
 
 	/**
 	 * The cached value of the '{@link #getGroup() <em>Group</em>}' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getGroup()
 	 * @generated NOT
 	 * @ordered
 	 */
 	protected Resolver resolver = new Resolver(this);
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ServerRuntimeImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return ServerTypePackage.eINSTANCE.getServerRuntime();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FeatureMap getGroup() {
 		if (group == null) {
 			group = new BasicFeatureMap(this, ServerTypePackage.SERVER_RUNTIME__GROUP);
 		}
 		return group;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getProperty() {
 		return ((FeatureMap)getGroup()).list(ServerTypePackage.eINSTANCE.getServerRuntime_Property());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FeatureMap getGroup1() {
 		if (group1 == null) {
 			group1 = new BasicFeatureMap(this, ServerTypePackage.SERVER_RUNTIME__GROUP1);
 		}
 		return group1;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getPort() {
 		return ((FeatureMap)getGroup1()).list(ServerTypePackage.eINSTANCE.getServerRuntime_Port());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FeatureMap getGroup2() {
 		if (group2 == null) {
 			group2 = new BasicFeatureMap(this, ServerTypePackage.SERVER_RUNTIME__GROUP2);
 		}
 		return group2;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getModule() {
 		return ((FeatureMap)getGroup2()).list(ServerTypePackage.eINSTANCE.getServerRuntime_Module());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Project getProject() {
 		return project;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetProject(Project newProject, NotificationChain msgs) {
 		Project oldProject = project;
 		project = newProject;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ServerTypePackage.SERVER_RUNTIME__PROJECT, oldProject, newProject);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setProject(Project newProject) {
 		if (newProject != project) {
 			NotificationChain msgs = null;
 			if (project != null)
 				msgs = ((InternalEObject)project).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ServerTypePackage.SERVER_RUNTIME__PROJECT, null, msgs);
 			if (newProject != null)
 				msgs = ((InternalEObject)newProject).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ServerTypePackage.SERVER_RUNTIME__PROJECT, null, msgs);
 			msgs = basicSetProject(newProject, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ServerTypePackage.SERVER_RUNTIME__PROJECT, newProject, newProject));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public LaunchConfiguration getStart() {
 		return start;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetStart(LaunchConfiguration newStart, NotificationChain msgs) {
 		LaunchConfiguration oldStart = start;
 		start = newStart;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ServerTypePackage.SERVER_RUNTIME__START, oldStart, newStart);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setStart(LaunchConfiguration newStart) {
 		if (newStart != start) {
 			NotificationChain msgs = null;
 			if (start != null)
 				msgs = ((InternalEObject)start).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ServerTypePackage.SERVER_RUNTIME__START, null, msgs);
 			if (newStart != null)
 				msgs = ((InternalEObject)newStart).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ServerTypePackage.SERVER_RUNTIME__START, null, msgs);
 			msgs = basicSetStart(newStart, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ServerTypePackage.SERVER_RUNTIME__START, newStart, newStart));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public LaunchConfiguration getStop() {
 		return stop;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetStop(LaunchConfiguration newStop, NotificationChain msgs) {
 		LaunchConfiguration oldStop = stop;
 		stop = newStop;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ServerTypePackage.SERVER_RUNTIME__STOP, oldStop, newStop);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setStop(LaunchConfiguration newStop) {
 		if (newStop != stop) {
 			NotificationChain msgs = null;
 			if (stop != null)
 				msgs = ((InternalEObject)stop).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ServerTypePackage.SERVER_RUNTIME__STOP, null, msgs);
 			if (newStop != null)
 				msgs = ((InternalEObject)newStop).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ServerTypePackage.SERVER_RUNTIME__STOP, null, msgs);
 			msgs = basicSetStop(newStop, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ServerTypePackage.SERVER_RUNTIME__STOP, newStop, newStop));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FeatureMap getGroup3() {
 		if (group3 == null) {
 			group3 = new BasicFeatureMap(this, ServerTypePackage.SERVER_RUNTIME__GROUP3);
 		}
 		return group3;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getPublisher() {
 		return ((FeatureMap)getGroup3()).list(ServerTypePackage.eINSTANCE.getServerRuntime_Publisher());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FeatureMap getGroup4() {
 		if (group4 == null) {
 			group4 = new BasicFeatureMap(this, ServerTypePackage.SERVER_RUNTIME__GROUP4);
 		}
 		return group4;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getClasspath() {
 		return ((FeatureMap)getGroup4()).list(ServerTypePackage.eINSTANCE.getServerRuntime_Classpath());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setName(String newName) {
 		String oldName = name;
 		name = newName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ServerTypePackage.SERVER_RUNTIME__NAME, oldName, name));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getVersion() {
 		return version;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setVersion(String newVersion) {
 		String oldVersion = version;
 		version = newVersion;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ServerTypePackage.SERVER_RUNTIME__VERSION, oldVersion, version));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case ServerTypePackage.SERVER_RUNTIME__GROUP:
 					return ((InternalEList)getGroup()).basicRemove(otherEnd, msgs);
 				case ServerTypePackage.SERVER_RUNTIME__PROPERTY:
 					return ((InternalEList)getProperty()).basicRemove(otherEnd, msgs);
 				case ServerTypePackage.SERVER_RUNTIME__GROUP1:
 					return ((InternalEList)getGroup1()).basicRemove(otherEnd, msgs);
 				case ServerTypePackage.SERVER_RUNTIME__PORT:
 					return ((InternalEList)getPort()).basicRemove(otherEnd, msgs);
 				case ServerTypePackage.SERVER_RUNTIME__GROUP2:
 					return ((InternalEList)getGroup2()).basicRemove(otherEnd, msgs);
 				case ServerTypePackage.SERVER_RUNTIME__MODULE:
 					return ((InternalEList)getModule()).basicRemove(otherEnd, msgs);
 				case ServerTypePackage.SERVER_RUNTIME__PROJECT:
 					return basicSetProject(null, msgs);
 				case ServerTypePackage.SERVER_RUNTIME__START:
 					return basicSetStart(null, msgs);
 				case ServerTypePackage.SERVER_RUNTIME__STOP:
 					return basicSetStop(null, msgs);
 				case ServerTypePackage.SERVER_RUNTIME__GROUP3:
 					return ((InternalEList)getGroup3()).basicRemove(otherEnd, msgs);
 				case ServerTypePackage.SERVER_RUNTIME__PUBLISHER:
 					return ((InternalEList)getPublisher()).basicRemove(otherEnd, msgs);
 				case ServerTypePackage.SERVER_RUNTIME__GROUP4:
 					return ((InternalEList)getGroup4()).basicRemove(otherEnd, msgs);
 				case ServerTypePackage.SERVER_RUNTIME__CLASSPATH:
 					return ((InternalEList)getClasspath()).basicRemove(otherEnd, msgs);
 				default:
 					return eDynamicInverseRemove(otherEnd, featureID, baseClass, msgs);
 			}
 		}
 		return eBasicSetContainer(null, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object eGet(EStructuralFeature eFeature, boolean resolve) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case ServerTypePackage.SERVER_RUNTIME__GROUP:
 				return getGroup();
 			case ServerTypePackage.SERVER_RUNTIME__PROPERTY:
 				return getProperty();
 			case ServerTypePackage.SERVER_RUNTIME__GROUP1:
 				return getGroup1();
 			case ServerTypePackage.SERVER_RUNTIME__PORT:
 				return getPort();
 			case ServerTypePackage.SERVER_RUNTIME__GROUP2:
 				return getGroup2();
 			case ServerTypePackage.SERVER_RUNTIME__MODULE:
 				return getModule();
 			case ServerTypePackage.SERVER_RUNTIME__PROJECT:
 				return getProject();
 			case ServerTypePackage.SERVER_RUNTIME__START:
 				return getStart();
 			case ServerTypePackage.SERVER_RUNTIME__STOP:
 				return getStop();
 			case ServerTypePackage.SERVER_RUNTIME__GROUP3:
 				return getGroup3();
 			case ServerTypePackage.SERVER_RUNTIME__PUBLISHER:
 				return getPublisher();
 			case ServerTypePackage.SERVER_RUNTIME__GROUP4:
 				return getGroup4();
 			case ServerTypePackage.SERVER_RUNTIME__CLASSPATH:
 				return getClasspath();
 			case ServerTypePackage.SERVER_RUNTIME__NAME:
 				return getName();
 			case ServerTypePackage.SERVER_RUNTIME__VERSION:
 				return getVersion();
 		}
 		return eDynamicGet(eFeature, resolve);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eSet(EStructuralFeature eFeature, Object newValue) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case ServerTypePackage.SERVER_RUNTIME__GROUP:
 				getGroup().clear();
 				getGroup().addAll((Collection)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__PROPERTY:
 				getProperty().clear();
 				getProperty().addAll((Collection)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__GROUP1:
 				getGroup1().clear();
 				getGroup1().addAll((Collection)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__PORT:
 				getPort().clear();
 				getPort().addAll((Collection)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__GROUP2:
 				getGroup2().clear();
 				getGroup2().addAll((Collection)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__MODULE:
 				getModule().clear();
 				getModule().addAll((Collection)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__PROJECT:
 				setProject((Project)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__START:
 				setStart((LaunchConfiguration)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__STOP:
 				setStop((LaunchConfiguration)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__GROUP3:
 				getGroup3().clear();
 				getGroup3().addAll((Collection)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__PUBLISHER:
 				getPublisher().clear();
 				getPublisher().addAll((Collection)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__GROUP4:
 				getGroup4().clear();
 				getGroup4().addAll((Collection)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__CLASSPATH:
 				getClasspath().clear();
 				getClasspath().addAll((Collection)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__NAME:
 				setName((String)newValue);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__VERSION:
 				setVersion((String)newValue);
 				return;
 		}
 		eDynamicSet(eFeature, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eUnset(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case ServerTypePackage.SERVER_RUNTIME__GROUP:
 				getGroup().clear();
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__PROPERTY:
 				getProperty().clear();
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__GROUP1:
 				getGroup1().clear();
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__PORT:
 				getPort().clear();
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__GROUP2:
 				getGroup2().clear();
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__MODULE:
 				getModule().clear();
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__PROJECT:
 				setProject((Project)null);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__START:
 				setStart((LaunchConfiguration)null);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__STOP:
 				setStop((LaunchConfiguration)null);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__GROUP3:
 				getGroup3().clear();
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__PUBLISHER:
 				getPublisher().clear();
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__GROUP4:
 				getGroup4().clear();
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__CLASSPATH:
 				getClasspath().clear();
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__NAME:
 				setName(NAME_EDEFAULT);
 				return;
 			case ServerTypePackage.SERVER_RUNTIME__VERSION:
 				setVersion(VERSION_EDEFAULT);
 				return;
 		}
 		eDynamicUnset(eFeature);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean eIsSet(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case ServerTypePackage.SERVER_RUNTIME__GROUP:
 				return group != null && !group.isEmpty();
 			case ServerTypePackage.SERVER_RUNTIME__PROPERTY:
 				return !getProperty().isEmpty();
 			case ServerTypePackage.SERVER_RUNTIME__GROUP1:
 				return group1 != null && !group1.isEmpty();
 			case ServerTypePackage.SERVER_RUNTIME__PORT:
 				return !getPort().isEmpty();
 			case ServerTypePackage.SERVER_RUNTIME__GROUP2:
 				return group2 != null && !group2.isEmpty();
 			case ServerTypePackage.SERVER_RUNTIME__MODULE:
 				return !getModule().isEmpty();
 			case ServerTypePackage.SERVER_RUNTIME__PROJECT:
 				return project != null;
 			case ServerTypePackage.SERVER_RUNTIME__START:
 				return start != null;
 			case ServerTypePackage.SERVER_RUNTIME__STOP:
 				return stop != null;
 			case ServerTypePackage.SERVER_RUNTIME__GROUP3:
 				return group3 != null && !group3.isEmpty();
 			case ServerTypePackage.SERVER_RUNTIME__PUBLISHER:
 				return !getPublisher().isEmpty();
 			case ServerTypePackage.SERVER_RUNTIME__GROUP4:
 				return group4 != null && !group4.isEmpty();
 			case ServerTypePackage.SERVER_RUNTIME__CLASSPATH:
 				return !getClasspath().isEmpty();
 			case ServerTypePackage.SERVER_RUNTIME__NAME:
 				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
 			case ServerTypePackage.SERVER_RUNTIME__VERSION:
 				return VERSION_EDEFAULT == null ? version != null : !VERSION_EDEFAULT.equals(version);
 		}
 		return eDynamicIsSet(eFeature);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (group: ");
 		result.append(group);
 		result.append(", group1: ");
 		result.append(group1);
 		result.append(", group2: ");
 		result.append(group2);
 		result.append(", group3: ");
 		result.append(group3);
 		result.append(", group4: ");
 		result.append(group4);
 		result.append(", name: ");
 		result.append(name);
 		result.append(", version: ");
 		result.append(version);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Publisher getPublisher(String id) {
		Iterator iterator = this.getPublisher().iterator();
 		while (iterator.hasNext()) {
 			Publisher publisher = (Publisher) iterator.next();
 			if(id.equals(publisher.getId()))
 				return publisher;
 		}
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Module getModule(String type) {
 		Iterator iterator = this.getModule().iterator();
 		while (iterator.hasNext()) {
 			Module module = (Module) iterator.next();
 			if(type.equals(module.getType()))
 				return module;
 		}
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Classpath getClasspath(String ref) {
 		Iterator iterator = this.getClasspath().iterator();
 		while (iterator.hasNext()) {
 			Classpath cp = (Classpath) iterator.next();
 			if(ref.equals(cp.getId()))
 				return cp;
 		}
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getFilename() {
 		return filename;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setFilename(String fn) {
 		this.filename = fn;		
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setPropertyValues(Map properties) {
 		this.resolver.setPropertyValues(properties);
 		
 	}
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public Resolver getResolver() {
 		return this.resolver;
 	}
 	
 	
 } //ServerRuntimeImpl
