 /**
  */
 package statemachine.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
 
 import org.emftext.language.java.references.Reference;
 import statemachine.State;
 import statemachine.StatemachinePackage;
 import statemachine.Transition;
 import org.eclipse.incquery.querybasedfeatures.runtime.IQueryBasedFeatureHandler;
 import org.eclipse.incquery.querybasedfeatures.runtime.QueryBasedFeatureKind;
 import org.emftext.language.java.references.IdentifierReference;
 import org.eclipse.incquery.querybasedfeatures.runtime.QueryBasedFeatureHelper;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Transition</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link statemachine.impl.TransitionImpl#getAction <em>Action</em>}</li>
  *   <li>{@link statemachine.impl.TransitionImpl#getTrigger <em>Trigger</em>}</li>
  *   <li>{@link statemachine.impl.TransitionImpl#getSrc <em>Src</em>}</li>
  *   <li>{@link statemachine.impl.TransitionImpl#getDst <em>Dst</em>}</li>
  *   <li>{@link statemachine.impl.TransitionImpl#getSource <em>Source</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class TransitionImpl extends MinimalEObjectImpl.Container implements Transition {
 	/**
 	 * The default value of the '{@link #getAction() <em>Action</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAction()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ACTION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getAction() <em>Action</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAction()
 	 * @generated
 	 * @ordered
 	 */
 	protected String action = ACTION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getTrigger() <em>Trigger</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTrigger()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String TRIGGER_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getTrigger() <em>Trigger</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTrigger()
 	 * @generated
 	 * @ordered
 	 */
 	protected String trigger = TRIGGER_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getSrc() <em>Src</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSrc()
 	 * @generated
 	 * @ordered
 	 */
 	protected State src;
 
 	/**
 	 * The cached value of the '{@link #getDst() <em>Dst</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDst()
 	 * @generated
 	 * @ordered
 	 */
 	protected State dst;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected TransitionImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return StatemachinePackage.Literals.TRANSITION;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getAction() {
 		return action;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAction(String newAction) {
 		String oldAction = action;
 		action = newAction;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StatemachinePackage.TRANSITION__ACTION, oldAction, action));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getTrigger() {
 		return trigger;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTrigger(String newTrigger) {
 		String oldTrigger = trigger;
 		trigger = newTrigger;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StatemachinePackage.TRANSITION__TRIGGER, oldTrigger, trigger));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public State getSrc() {
 		if (src != null && src.eIsProxy()) {
 			InternalEObject oldSrc = (InternalEObject)src;
 			src = (State)eResolveProxy(oldSrc);
 			if (src != oldSrc) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, StatemachinePackage.TRANSITION__SRC, oldSrc, src));
 			}
 		}
 		return src;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public State basicGetSrc() {
 		return src;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetSrc(State newSrc, NotificationChain msgs) {
 		State oldSrc = src;
 		src = newSrc;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, StatemachinePackage.TRANSITION__SRC, oldSrc, newSrc);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSrc(State newSrc) {
 		if (newSrc != src) {
 			NotificationChain msgs = null;
 			if (src != null)
 				msgs = ((InternalEObject)src).eInverseRemove(this, StatemachinePackage.STATE__OUT, State.class, msgs);
 			if (newSrc != null)
 				msgs = ((InternalEObject)newSrc).eInverseAdd(this, StatemachinePackage.STATE__OUT, State.class, msgs);
 			msgs = basicSetSrc(newSrc, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StatemachinePackage.TRANSITION__SRC, newSrc, newSrc));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public State getDst() {
 		if (dst != null && dst.eIsProxy()) {
 			InternalEObject oldDst = (InternalEObject)dst;
 			dst = (State)eResolveProxy(oldDst);
 			if (dst != oldDst) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, StatemachinePackage.TRANSITION__DST, oldDst, dst));
 			}
 		}
 		return dst;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public State basicGetDst() {
 		return dst;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetDst(State newDst, NotificationChain msgs) {
 		State oldDst = dst;
 		dst = newDst;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, StatemachinePackage.TRANSITION__DST, oldDst, newDst);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDst(State newDst) {
 		if (newDst != dst) {
 			NotificationChain msgs = null;
 			if (dst != null)
 				msgs = ((InternalEObject)dst).eInverseRemove(this, StatemachinePackage.STATE__IN, State.class, msgs);
 			if (newDst != null)
 				msgs = ((InternalEObject)newDst).eInverseAdd(this, StatemachinePackage.STATE__IN, State.class, msgs);
 			msgs = basicSetDst(newDst, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StatemachinePackage.TRANSITION__DST, newDst, newDst));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IdentifierReference getSourceGen() {
 		IdentifierReference source = basicGetSource();
 		return source != null && source.eIsProxy() ? (IdentifierReference)eResolveProxy((InternalEObject)source) : source;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case StatemachinePackage.TRANSITION__SRC:
 				if (src != null)
 					msgs = ((InternalEObject)src).eInverseRemove(this, StatemachinePackage.STATE__OUT, State.class, msgs);
 				return basicSetSrc((State)otherEnd, msgs);
 			case StatemachinePackage.TRANSITION__DST:
 				if (dst != null)
 					msgs = ((InternalEObject)dst).eInverseRemove(this, StatemachinePackage.STATE__IN, State.class, msgs);
 				return basicSetDst((State)otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case StatemachinePackage.TRANSITION__SRC:
 				return basicSetSrc(null, msgs);
 			case StatemachinePackage.TRANSITION__DST:
 				return basicSetDst(null, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case StatemachinePackage.TRANSITION__ACTION:
 				return getAction();
 			case StatemachinePackage.TRANSITION__TRIGGER:
 				return getTrigger();
 			case StatemachinePackage.TRANSITION__SRC:
 				if (resolve) return getSrc();
 				return basicGetSrc();
 			case StatemachinePackage.TRANSITION__DST:
 				if (resolve) return getDst();
 				return basicGetDst();
 			case StatemachinePackage.TRANSITION__SOURCE:
 				if (resolve) return getSource();
 				return basicGetSource();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case StatemachinePackage.TRANSITION__ACTION:
 				setAction((String)newValue);
 				return;
 			case StatemachinePackage.TRANSITION__TRIGGER:
 				setTrigger((String)newValue);
 				return;
 			case StatemachinePackage.TRANSITION__SRC:
 				setSrc((State)newValue);
 				return;
 			case StatemachinePackage.TRANSITION__DST:
 				setDst((State)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case StatemachinePackage.TRANSITION__ACTION:
 				setAction(ACTION_EDEFAULT);
 				return;
 			case StatemachinePackage.TRANSITION__TRIGGER:
 				setTrigger(TRIGGER_EDEFAULT);
 				return;
 			case StatemachinePackage.TRANSITION__SRC:
 				setSrc((State)null);
 				return;
 			case StatemachinePackage.TRANSITION__DST:
 				setDst((State)null);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case StatemachinePackage.TRANSITION__ACTION:
 				return ACTION_EDEFAULT == null ? action != null : !ACTION_EDEFAULT.equals(action);
 			case StatemachinePackage.TRANSITION__TRIGGER:
 				return TRIGGER_EDEFAULT == null ? trigger != null : !TRIGGER_EDEFAULT.equals(trigger);
 			case StatemachinePackage.TRANSITION__SRC:
 				return src != null;
 			case StatemachinePackage.TRANSITION__DST:
 				return dst != null;
 			case StatemachinePackage.TRANSITION__SOURCE:
 				return basicGetSource() != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (action: ");
 		result.append(action);
 		result.append(", trigger: ");
 		result.append(trigger);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @query-based getter created by EMF-IncQuery for query-based feature source
 	 */
 	public IdentifierReference getSource() {
 		if (sourceHandler == null) {
 			sourceHandler = QueryBasedFeatureHelper
 					.getQueryBasedFeatureHandler(
 							this,
 							StatemachinePackageImpl.Literals.TRANSITION__SOURCE,
 							"hu.bme.mit.viatra2.examples.reveng.transitionTrace",
 							"t", "ref", QueryBasedFeatureKind.SINGLE_REFERENCE,
 							true, false);
 		}
 		return (org.emftext.language.java.references.IdentifierReference) sourceHandler
 				.getSingleReferenceValue(this);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IdentifierReference basicGetSourceGen() {
 		// TODO: implement this method to return the 'Source' reference
 		// -> do not perform proxy resolution
 		// Ensure that you remove @generated or mark it @generated NOT
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * EMF-IncQuery handler for query-based feature source
 	 */
 	private IQueryBasedFeatureHandler sourceHandler;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @query-based getter created by EMF-IncQuery for query-based feature source
 	 */
 	public IdentifierReference basicGetSource() {
 		if (sourceHandler == null) {
 			sourceHandler = QueryBasedFeatureHelper
 					.getQueryBasedFeatureHandler(
 							this,
 							StatemachinePackageImpl.Literals.TRANSITION__SOURCE,
							"hu.bme.mit.viatra2.examples.reveng.transitionTrace",
 							"t", "ref", QueryBasedFeatureKind.SINGLE_REFERENCE,
 							true, false);
 		}
 		return (org.emftext.language.java.references.IdentifierReference) sourceHandler
 				.getSingleReferenceValue(this);
 	}
 
 } //TransitionImpl
