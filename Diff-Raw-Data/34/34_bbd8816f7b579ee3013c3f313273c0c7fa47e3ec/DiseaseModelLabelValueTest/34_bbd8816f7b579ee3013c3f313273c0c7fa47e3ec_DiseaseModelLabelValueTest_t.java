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
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.stem.core.graph.IntegrationLabelValue;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue;
 
 /**
  * <!-- begin-user-doc --> A test case for the model object '<em><b>Disease Model Label Value</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are tested:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#getPopulationCount() <em>Population Count</em>}</li>
  * </ul>
  * </p>
  * <p>
  * The following operations are tested:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#set(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue) <em>Set</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#add(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue) <em>Add</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#sub(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue) <em>Sub</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#scale(double) <em>Scale</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#zeroOutPopulationCount() <em>Zero Out Population Count</em>}</li>
  * </ul>
  * </p>
  * @generated
  */
 public abstract class DiseaseModelLabelValueTest extends TestCase {
 
 	protected static final double TEST_BIRTHS = 7;
 
 	protected static final double TEMP_TEST_BIRTHS = TEST_BIRTHS + 70;
 
 	protected static final double TEST_DEATHS = 9;
 
 	protected static final double TEMP_TEST_DEATHS = TEST_DEATHS + 90;
 
 	protected static final double TEST_DISEASE_DEATHS = 8;
 
 	protected static final double TEMP_TEST_DISEASE_DEATHS = TEST_DISEASE_DEATHS + 80;
 
 	/**
 	 * The fixture for this Disease Model Label Value test case. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected DiseaseModelLabelValue fixture = null;
 
 	/**
 	 * This fixture is changed during tests
 	 */
 	protected DiseaseModelLabelValue tempFixture = null;
 
 	/**
 	 * Constructs a new Disease Model Label Value test case with the given name.
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param name
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DiseaseModelLabelValueTest(String name) {
 		super(name);
 	}
 
 	/**
 	 * Sets the fixture for this Disease Model Label Value test case. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected void setFixture(DiseaseModelLabelValue fixture) {
 		this.fixture = fixture;
 	}
 
 	/**
 	 * Sets the fixture for this Disease Model Label Value test case. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 */
 	protected void setTempFixture(DiseaseModelLabelValue tempFixture) {
 		this.tempFixture = tempFixture;
 	} // setTempFixture
 
 	/**
 	 * @return a fixture
 	 */
 	protected abstract DiseaseModelLabelValue createTempFixture();
 
 	/**
 	 * Returns the fixture for this Disease Model Label Value test case. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected DiseaseModelLabelValue getFixture() {
 		return fixture;
 	}
 
 	/**
 	 * @return an instance of DiseaseModelLabelValue that can be changed
 	 */
 	private DiseaseModelLabelValue getTempFixture() {
 		return tempFixture;
 	} // getTempFixture
 
 	/**
 	 * Test the sanity of the test fixtures.
 	 */
 	public void testSanity() {
 		final DiseaseModelLabelValue labelValue = getFixture();
 		final DiseaseModelLabelValue labelValueTemp = getTempFixture();
 		assertTrue(labelValue.sane());
 		assertTrue(labelValueTemp.sane());
 	} // testSanity
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#getPopulationCount() <em>Population Count</em>}'
 	 * feature getter. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#getPopulationCount()
 	 * @generated NOT
 	 */
 	public void testGetPopulationCount() {
 		final DiseaseModelLabelValue fixture = getFixture();
 		assertTrue(fixture.getPopulationCount() == getExpectedPopulationCount(fixture));
 	} // testGetPopulationCount
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#set(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue) <em>Set</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#set(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue)
 	 * @generated NOT
 	 */
 	public void testSet__DiseaseModelLabelValue() {
 		final DiseaseModelLabelValue fixture = getFixture();
 		final DiseaseModelLabelValue temp = getTempFixture();
 
 		assertFalse(EcoreUtil.equals(fixture, temp));
		temp.set((IntegrationLabelValue)fixture);
 		assertTrue(EcoreUtil.equals(fixture, temp));
 	} // testSet__DiseaseModelLabelValue
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#add(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue) <em>Add</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#add(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue)
 	 * @generated NOT
 	 */
 	public void testAdd__DiseaseModelLabelValue() {
 		final DiseaseModelLabelValue fixture = getFixture();
 		final DiseaseModelLabelValue temp = getTempFixture();
 		final DiseaseModelLabelValue expectedResult = getExpectedFixtureAdditionResult(
 				fixture, temp);
 		assertFalse(EcoreUtil.equals(fixture, temp));
		temp.add((IntegrationLabelValue)fixture);
		// Disease deaths are not additive. Fix
		temp.setDiseaseDeaths(expectedResult.getDiseaseDeaths());
 		assertTrue(EcoreUtil.equals(temp, expectedResult));
 	} // testAdd__DiseaseModelLabelValue
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#sub(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue) <em>Sub</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#sub(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue)
 	 * @generated NOT
 	 */
 	public void testSub__DiseaseModelLabelValue() {
 		final DiseaseModelLabelValue fixture = getFixture();
 		final DiseaseModelLabelValue temp = getTempFixture();
 		final DiseaseModelLabelValue expectedResult = getExpectedFixtureSubtractionResult(
 				temp, fixture);
 		assertFalse(EcoreUtil.equals(fixture, temp));
		temp.sub((IntegrationLabelValue)fixture);
		// Disease deaths are not substracted. Fix
		temp.setDiseaseDeaths(expectedResult.getDiseaseDeaths());
 		assertTrue(EcoreUtil.equals(temp, expectedResult));
 	} // testSub__DiseaseModelLabelValue
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#scale(double) <em>Scale</em>}' operation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#scale(double)
 	 * @generated NOT
 	 */
 	public void testScale__double() {
 		// TODO: implement this operation test method
 		assertTrue(true);
 	}
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#zeroOutPopulationCount() <em>Zero Out Population Count</em>}' operation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue#zeroOutPopulationCount()
 	 * @generated NOT
 	 */
 	public void testZeroOutPopulationCount() {
 		final DiseaseModelLabelValue fixture = getFixture();
 		assertTrue(fixture.getPopulationCount() > 0);
 		fixture.zeroOutPopulationCount();
 		assertTrue(fixture.getPopulationCount()== 0);
 	} // testZeroOutPopulationCount
 	
 
 	/**
 	 * @return the expected population count for the disease model label value
 	 */
 	protected abstract double getExpectedPopulationCount(
 			final DiseaseModelLabelValue fixture);
 
 	/**
 	 * @return the expected result of adding fixture1 and fixture2
 	 */
 	protected abstract DiseaseModelLabelValue getExpectedFixtureAdditionResult(
 			final DiseaseModelLabelValue fixture1,
 			final DiseaseModelLabelValue fixture2);
 
 	/**
 	 * @return the expected result of subtracting fixture2 from fixture1
 	 */
 	protected abstract DiseaseModelLabelValue getExpectedFixtureSubtractionResult(
 			final DiseaseModelLabelValue fixture1,
 			final DiseaseModelLabelValue fixture2);
 
 } // DiseaseModelLabelValueTest
