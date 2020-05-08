 package org.eclipse.stem.diseasemodels.standard.tests;
 
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
 
 import junit.framework.TestCase;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.stem.definitions.labels.PopulationLabel;
 import org.eclipse.stem.diseasemodels.standard.AggregatingSIDiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelState;
 import org.eclipse.stem.diseasemodels.standard.Infector;
 
 /**
  * <!-- begin-user-doc --> A test case for the model object '<em><b>Disease Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following operations are tested:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#getAdjustedBackgroundMortalityRate(long) <em>Get Adjusted Background Mortality Rate</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#createDiseaseModelLabel() <em>Create Disease Model Label</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#createDiseaseModelLabelValue() <em>Create Disease Model Label Value</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#createDiseaseModelState() <em>Create Disease Model State</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelState, org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel) <em>Initialize Disease State</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel) <em>Initialize Disease State</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#createInfector() <em>Create Infector</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#getAdjustedBackgroundBirthRate(long) <em>Get Adjusted Background Birth Rate</em>}</li>
  * </ul>
  * </p>
  * @generated
  */
 public abstract class DiseaseModelTest extends TestCase {
 
 	/**
 	 * The population identifier to use for testing disease models.
 	 */
 	final public static String TEST_POPULATION_IDENTIFIER = PopulationLabel.HUMAN;
 
 	/**
 	 * This is the rate at which population members (on average) transition from
 	 * all of the other states (except <em>Pre-Born</em>) to the state
 	 * <em>Dead</em>, per time period. Units: population members
 	 * 
 	 * @see StandardDiseaseModel#getBackgroundMortalityRate()
 	 */
 	public static final double MORTALITY_RATE = 0.01;
 
 	/**
 	 * The fixture for this Disease Model test case.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected DiseaseModel fixture = null;
 
 	/**
 	 * Constructs a new Disease Model test case with the given name. <!--
 	 * begin-user-doc -->
 	 * 
 	 * @param name
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DiseaseModelTest(String name) {
 		super(name);
 	}
 
 	/**
 	 * Sets the fixture for this Disease Model test case.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void setFixture(DiseaseModel fixture) {
 		this.fixture = fixture;
 	}
 
 	/**
 	 * Returns the fixture for this Disease Model test case.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected DiseaseModel getFixture() {
 		return fixture;
 	}
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#getAdjustedBackgroundMortalityRate(long) <em>Get Adjusted Background Mortality Rate</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#getAdjustedBackgroundMortalityRate(long)
 	 * @generated NOT
 	 */
 	public void testGetAdjustedBackgroundMortalityRate__long() {
 		final DiseaseModel fixture = getFixture();
 		assertTrue(DiseaseModelTestUtil.closeEnough(
 				fixture.getAdjustedBackgroundMortalityRate(2 * fixture
 						.getTimePeriod()), (2 * fixture
 						.getBackgroundMortalityRate())));
 	} // testGetAdjustedBackgroundMortalityRate__long
 
 	/**
 	 * test the sanity of the fixture
 	 */
 	public void testFixtureSanity() {
 		final DiseaseModel fixture = getFixture();
 		assertTrue(fixture.sane());
 	} // testFixtureSanity
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#createDiseaseModelLabel() <em>Create Disease Model Label</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModel#createDiseaseModelLabel()
 	 * @generated NOT
 	 */
 	public void testCreateDiseaseModelLabel() {
 		final DiseaseModel diseaseModel = getFixture();
 		if (!(diseaseModel instanceof AggregatingSIDiseaseModel)) {
 		final DiseaseModelLabel dml = diseaseModel.createDiseaseModelLabel();
 		assertTrue(dml.eClass().equals(getDiseaseModelLabelClass()));
 		}
 	} // testCreateDiseaseModelLabel
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#createDiseaseModelLabelValue() <em>Create Disease Model Label Value</em>}' operation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModel#createDiseaseModelLabelValue()
 	 * @generated NOT
 	 */
 	public void testCreateDiseaseModelLabelValue() {
 		// TODO: implement this operation test method
 		assertTrue(true);
 	}
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#createDiseaseModelState() <em>Create Disease Model State</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModel#createDiseaseModelState()
 	 * @generated NOT
 	 */
 	public void testCreateDiseaseModelState() {
 		final DiseaseModel diseaseModel = getFixture();
 		if (!(diseaseModel instanceof AggregatingSIDiseaseModel)) {
 
 			final DiseaseModelState dms = diseaseModel
 					.createDiseaseModelState();
 			assertTrue(dms.eClass().equals(getDiseaseModelStateClass()));
 		}
 	} // testCreateDiseaseModelState
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelState, org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel) <em>Initialize Disease State</em>}' operation.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModel#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelState, org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel)
 	 * @generated
 	 */
 	public void testInitializeDiseaseState__DiseaseModelState_DiseaseModelLabel() {
 		// TODO: implement this operation test method
 		// Ensure that you remove @generated or mark it @generated NOT
 		fail();
 	}
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel) <em>Initialize Disease State</em>}' operation.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModel#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel)
 	 * @generated
 	 */
 	public void testInitializeDiseaseState__DiseaseModelLabel() {
 		// TODO: implement this operation test method
 		// Ensure that you remove @generated or mark it @generated NOT
 		fail();
 	}
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#createInfector() <em>Create Infector</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModel#createInfector()
 	 * @generated NOT
 	 */
 	public void testCreateInfector() {
 		final DiseaseModel diseaseModel = getFixture();
 		if (!(diseaseModel instanceof AggregatingSIDiseaseModel)) {
 			final Infector infector = diseaseModel.createInfector();
 			assertNotNull(infector);
 			assertTrue(infector.getDiseaseName().equals(
 					fixture.getDiseaseName()));
 			assertTrue(infector.getPopulationIdentifier().equalsIgnoreCase(
 					fixture.getPopulationIdentifier()));
 		}
 
 	} // testCreateInfector
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#getAdjustedBackgroundBirthRate(long) <em>Get Adjusted Background Birth Rate</em>}' operation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModel#getAdjustedBackgroundBirthRate(long)
 	 * @generated
 	 */
 	public void testGetAdjustedBackgroundBirthRate__long() {
 		// TODO: implement this operation test method
		// Ensure that you remove @generated or mark it @generated NOT
		fail();
 	}
 
 	/**
 	 * @return the EClass of the class this disease model updates
 	 */
 	protected abstract EClass getDiseaseModelLabelClass();
 
 	/**
 	 * @return the EClass of the class this disease model uses to store
 	 *         computation state
 	 */
 	protected abstract EClass getDiseaseModelStateClass();
 
 } // DiseaseModelTest
