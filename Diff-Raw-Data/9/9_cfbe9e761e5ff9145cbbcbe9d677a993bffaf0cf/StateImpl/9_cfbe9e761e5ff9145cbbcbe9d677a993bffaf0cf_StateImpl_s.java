 /**
  */
 package statemachine.impl;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
 
 import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 import statemachine.State;
 import statemachine.StatemachinePackage;
 import statemachine.Transition;
 import org.eclipse.incquery.querybasedfeatures.runtime.IQueryBasedFeatureHandler;
 import org.eclipse.incquery.querybasedfeatures.runtime.QueryBasedFeatureKind;
 import org.eclipse.incquery.querybasedfeatures.runtime.QueryBasedFeatureHelper;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>State</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link statemachine.impl.StateImpl#getName <em>Name</em>}</li>
  *   <li>{@link statemachine.impl.StateImpl#getOut <em>Out</em>}</li>
  *   <li>{@link statemachine.impl.StateImpl#getIn <em>In</em>}</li>
  *   <li>{@link statemachine.impl.StateImpl#getSource <em>Source</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class StateImpl extends MinimalEObjectImpl.Container implements State {
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
 	 * The cached value of the '{@link #getOut() <em>Out</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOut()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Transition> out;
 
 	/**
 	 * The cached value of the '{@link #getIn() <em>In</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getIn()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Transition> in;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected StateImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return StatemachinePackage.Literals.STATE;
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
 			eNotify(new ENotificationImpl(this, Notification.SET, StatemachinePackage.STATE__NAME, oldName, name));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Transition> getOut() {
 		if (out == null) {
 			out = new EObjectWithInverseResolvingEList<Transition>(Transition.class, this, StatemachinePackage.STATE__OUT, StatemachinePackage.TRANSITION__SRC);
 		}
 		return out;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Transition> getIn() {
 		if (in == null) {
 			in = new EObjectWithInverseResolvingEList<Transition>(Transition.class, this, StatemachinePackage.STATE__IN, StatemachinePackage.TRANSITION__DST);
 		}
 		return in;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public org.emftext.language.java.classifiers.Class getSource() {
 		org.emftext.language.java.classifiers.Class source = basicGetSource();
 		return source != null && source.eIsProxy() ? (org.emftext.language.java.classifiers.Class)eResolveProxy((InternalEObject)source) : source;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public org.emftext.language.java.classifiers.Class basicGetSourceGen() {
 		// TODO: implement this method to return the 'Source' reference
 		// -> do not perform proxy resolution
 		// Ensure that you remove @generated or mark it @generated NOT
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case StatemachinePackage.STATE__OUT:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getOut()).basicAdd(otherEnd, msgs);
 			case StatemachinePackage.STATE__IN:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getIn()).basicAdd(otherEnd, msgs);
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
 			case StatemachinePackage.STATE__OUT:
 				return ((InternalEList<?>)getOut()).basicRemove(otherEnd, msgs);
 			case StatemachinePackage.STATE__IN:
 				return ((InternalEList<?>)getIn()).basicRemove(otherEnd, msgs);
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
 			case StatemachinePackage.STATE__NAME:
 				return getName();
 			case StatemachinePackage.STATE__OUT:
 				return getOut();
 			case StatemachinePackage.STATE__IN:
 				return getIn();
 			case StatemachinePackage.STATE__SOURCE:
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
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case StatemachinePackage.STATE__NAME:
 				setName((String)newValue);
 				return;
 			case StatemachinePackage.STATE__OUT:
 				getOut().clear();
 				getOut().addAll((Collection<? extends Transition>)newValue);
 				return;
 			case StatemachinePackage.STATE__IN:
 				getIn().clear();
 				getIn().addAll((Collection<? extends Transition>)newValue);
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
 			case StatemachinePackage.STATE__NAME:
 				setName(NAME_EDEFAULT);
 				return;
 			case StatemachinePackage.STATE__OUT:
 				getOut().clear();
 				return;
 			case StatemachinePackage.STATE__IN:
 				getIn().clear();
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
 			case StatemachinePackage.STATE__NAME:
 				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
 			case StatemachinePackage.STATE__OUT:
 				return out != null && !out.isEmpty();
 			case StatemachinePackage.STATE__IN:
 				return in != null && !in.isEmpty();
 			case StatemachinePackage.STATE__SOURCE:
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
 		result.append(" (name: ");
 		result.append(name);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * EMF-IncQuery handler for query-based feature source
 	 */
 	private IQueryBasedFeatureHandler sourceHandler;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @query-based getter created by EMF-IncQuery for query-based feature source
 	 */
 	public org.emftext.language.java.classifiers.Class basicGetSource() {
 		if (sourceHandler == null) {
 			sourceHandler = QueryBasedFeatureHelper
 					.getQueryBasedFeatureHandler(this,
 							StatemachinePackageImpl.Literals.STATE__SOURCE,
							"hu.bme.mit.viatra2.examples.reveng.stateTrace",
 							"st", "cl", QueryBasedFeatureKind.SINGLE_REFERENCE,
 							true, false);
 		}
 		return (org.emftext.language.java.classifiers.Class) sourceHandler
 				.getSingleReferenceValue(this);
 	}
 
 } //StateImpl
