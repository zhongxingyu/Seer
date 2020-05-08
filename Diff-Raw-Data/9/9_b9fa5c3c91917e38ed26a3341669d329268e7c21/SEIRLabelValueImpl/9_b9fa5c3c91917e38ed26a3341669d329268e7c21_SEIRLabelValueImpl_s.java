 package org.eclipse.stem.diseasemodels.standard.impl;
 
 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.stem.core.graph.IntegrationLabelValue;
 import org.eclipse.stem.core.graph.LabelValue;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SEIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.SIRLabelValue;
 //import org.eclipse.stem.diseasemodels.standard.SIRLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.StandardPackage;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>SEIR Label Value</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.impl.SEIRLabelValueImpl#getE <em>E</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class SEIRLabelValueImpl extends SIRLabelValueImpl implements
 		SEIRLabelValue {
 	/**
 	 * The default value of the '{@link #getE() <em>E</em>}' attribute. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getE()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double E_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getE() <em>E</em>}' attribute. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getE()
 	 * @generated
 	 * @ordered
 	 */
 	protected double e = E_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected SEIRLabelValueImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * This is used to create instances for testing purposes.
 	 * 
 	 * @param s
 	 *            the number of susceptible population members
 	 * @param e
 	 *            the number of exposed population members
 	 * @param iR
 	 *            the number of recovering infectious population members
 	 * @param iF
 	 *            the number of fatally infectious population members
 	 *        
 	 * @param incidence
 	 *            the new incidence from the interaction term in the compartment model
 	 * 
 	 * @param r
 	 *            the number of recovered population members
 	 * @param births
 	 *            the number of births that have occurred in the population
 	 * @param deaths
 	 *            the total number of deaths that have occurred in the population
 	 * @param diseaseDeaths
 	 *            the number of deaths due to the disease that have occurred in
 	 *            the population
 	 * 
 	 * <!-- end-user-doc -->
 	 */
 	public SEIRLabelValueImpl(final double s, final double e, final double i, double incidence,
 			final double r, 
 			final double diseaseDeaths) {
 		super(s, i, incidence, r, diseaseDeaths);
 		this.e = e;
 	} // SEIRLabelValueImpl
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * This is used to create instances for testing purposes.
 	 * 
 	 * @param s
 	 *            the number of susceptible population members
 	 * @param e
 	 *            the number of exposed population members
 	 * @param iR
 	 *            the number of recovering infectious population members
 	 * @param iF
 	 *            the number of fatally infectious population members
 	 *
 	 * @param r
 	 *            the number of recovered population members
 	 * @param births
 	 *            the number of births that have occurred in the population
 	 * @param deaths
 	 *            the total number of deaths that have occurred in the population
 	 * @param diseaseDeaths
 	 *            the number of deaths due to the disease that have occurred in
 	 *            the population
 	 * 
 	 * <!-- end-user-doc -->
 	 */
 	public SEIRLabelValueImpl(final double s, final double e, final double i, 
 			final double r, 
 			final double diseaseDeaths) {
 		super(s, i, 0.0, r, diseaseDeaths);
 		this.e = e;
 	} // SEIRLabelValueImpl
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return StandardPackage.Literals.SEIR_LABEL_VALUE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getE() {
 		return e;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param newE
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setE(double newE) {
 		double oldE = e;
 		e = newE;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, StandardPackage.SEIR_LABEL_VALUE__E, oldE, e));
 	}
 
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.SIRLabelValueImpl#getPopulationCount()
 	 */
 	@Override
 	public double getPopulationCount() {
 		return e + super.getPopulationCount();
 	} // getPopulationCount
 
 	
 	@Override
 	public void zeroOutPopulationCount() {
 		super.zeroOutPopulationCount();
 		setE(E_EDEFAULT);
 	} // zeroOutPopulationCount
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.SIRLabelValueImpl#set(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue)
 	 */
 	@Override
 	public DiseaseModelLabelValue set(IntegrationLabelValue value) {
 		super.set(value);
 		setE(((SEIRLabelValue) value).getE());
 		return this;
 	} // set
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.SIRLabelValueImpl#add(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue)
 	 */
 	@Override
 	public DiseaseModelLabelValue add(IntegrationLabelValue value) {
 		super.add(value);
 		setE(getE() + ((SEIRLabelValue) value).getE());
 		return this;
 	} // add
 
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.SIRLabelValueImpl#sub(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue)
 	 */
 	@Override
 	public DiseaseModelLabelValue sub(IntegrationLabelValue value) {
 		super.sub(value);
 		setE(getE() - ((SEIRLabelValue) value).getE());
 		return this;
 	} // sub
 	
 	@Override
 	public DiseaseModelLabelValue scale(double scaleFactor) {
 		super.scale(scaleFactor);
 		setE(getE() *scaleFactor);
 		return this;
 	} // scale
 	
 	public DiseaseModelLabelValue add(double addition) {
 		super.add(addition);
 		setE(getE() +addition);
 		return this;
 	} // scale
 
 	public DiseaseModelLabelValue abs() {
 		super.abs();
 		setE(Math.abs(getE()));
 		return this;
 	} // scale
 	
 	
 	public IntegrationLabelValue divide(IntegrationLabelValue d) {
 		SEIRLabelValue _scale = (SEIRLabelValue)d;
 		double sScaled = Math.abs(s) / Math.abs(_scale.getS());
 		double iScaled = Math.abs(i) / Math.abs(_scale.getI());
 		double rScaled = Math.abs(r) / Math.abs(_scale.getR());
 		double eScaled = Math.abs(e) / Math.abs(_scale.getE());
 		setS(sScaled);
 		setI(iScaled);
 		setR(rScaled);
 		setE(eScaled);
 		return this;
 	}
 	
 	public double max() {
 		double max;
 		if(s > i && s > r && s > e)
 			max = s;
 		else if(i > r && i > e) max = i;
 		else if(r > e) max = r;
 		else max = e;
 		return max;  
 	}
 	
 	/**
 	 * Adjust when a delta value is bad, e.g. forces the 
 	 * target to go negative in any state.
 	 * 
 	 * @return boolean
 	 * @override
 	 */
	public boolean adjustDelta(DiseaseModelLabelValue target) {
 		SEIRLabelValue seirValue = (SEIRLabelValue)target;
 		
 		boolean adjusted = false;
 		double newS = this.getS() + seirValue.getS();
 		double newE = this.getE() + seirValue.getE();
 		double newI = this.getI() + seirValue.getI();
 		double newR = this.getR() + seirValue.getR();
 		
 		double factor = 1.0;
 		if(newS < newE && newS < newI && newS < newR && newS < 0.0) {
 			// Scale using S
 			adjusted = true;
 			factor = -seirValue.getS()/this.getS();
 		} else if(newE < newI && newE < newR && newE < 0.0) {
 			// Scale using E
 			adjusted = true;
 			factor = -seirValue.getE()/this.getE();
 		} else if(newI < newR && newI < 0.0) {
 			// Scale using I
 			adjusted = true;
 			factor = -seirValue.getI()/this.getI();
 		} else if(newR < 0.0) {
 			// Scale using R
 			adjusted = true;
 			factor = -seirValue.getR()/this.getR();
 		}
 		if(adjusted) this.scale(factor);
 		return adjusted;
 	}
 
 	
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.SIRLabelValueImpl#reset()
 	 */
 	@Override
 	public void reset() {
 		super.reset();
 		setE(E_EDEFAULT);
 	} // reset
 	
 	/**
 	 * @see org.eclipse.stem.diseasemodels.standard.impl.SIRLabelValueImpl#sane()
 	 */
 	@Override
 	public boolean sane() {
 		boolean retValue = super.sane();
 
 		retValue = retValue && getE() >= E_EDEFAULT;
 		assert retValue;
 
 		retValue = retValue && getE() <= MAX_POPULATION_VALUE;
 		assert retValue;		
 		
 		retValue = retValue && !Double.isInfinite(getE());
 		assert retValue;
 		
 		retValue = retValue && !Double.isNaN(getE());
 		assert retValue;
 		
 		return retValue;
 	} // sane
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * @param resolve
 	 * @param coreType
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case StandardPackage.SEIR_LABEL_VALUE__E:
 				return new Double(getE());
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * @param newValue
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case StandardPackage.SEIR_LABEL_VALUE__E:
 				setE(((Double)newValue).doubleValue());
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case StandardPackage.SEIR_LABEL_VALUE__E:
 				setE(E_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case StandardPackage.SEIR_LABEL_VALUE__E:
 				return e != E_EDEFAULT;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy())
 			return super.toString();
 
 		StringBuffer result = new StringBuffer();
 		result.append("s:"); //$NON-NLS-1$
 		result.append(getFormatter().format(s));
 		result.append(", e:"); //$NON-NLS-1$
 		result.append(getFormatter().format(e));
 		result.append(", i:"); //$NON-NLS-1$
 		result.append(getFormatter().format(i));
 		result.append(", r:"); //$NON-NLS-1$
 		result.append(getFormatter().format(r));
 		result.append(", DD:"); //$NON-NLS-1$
 		result.append(getFormatter().format(getDiseaseDeaths()));
 		return result.toString();
 	} // toString
 
 } // SEIRLabelValueImpl
