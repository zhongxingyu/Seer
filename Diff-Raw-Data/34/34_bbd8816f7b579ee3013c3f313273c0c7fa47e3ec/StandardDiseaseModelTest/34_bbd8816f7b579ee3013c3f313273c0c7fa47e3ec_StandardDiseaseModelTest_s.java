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
 
 import java.util.List;
 
 import org.eclipse.stem.core.graph.Edge;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.definitions.labels.PopulationLabel;
 import org.eclipse.stem.definitions.labels.impl.TransportRelationshipLabelImpl;
 import org.eclipse.stem.diseasemodels.standard.AggregatingSIDiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelState;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelState;
 
 /**
  * <!-- begin-user-doc --> A test case for the model object '<em><b>Disease Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following operations are tested:
  * <ul>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#addToTotalPopulationCount(double) <em>Add To Total Population Count</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#computeTotalPopulationCountReciprocal() <em>Compute Total Population Count Reciprocal</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#addToTotalArea(double) <em>Add To Total Area</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#calculateDelta(org.eclipse.stem.core.model.STEMTime, long, org.eclipse.emf.common.util.EList) <em>Calculate Delta</em>}</li>
  *   <li>{@link org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#doModelSpecificAdjustments(org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue) <em>Do Model Specific Adjustments</em>}</li>
  * </ul>
  * </p>
  * @generated
  */
 @SuppressWarnings("all")
 public abstract class StandardDiseaseModelTest extends DiseaseModelTest {
 
 	/**
 	 * This is initialized to contain a disease label,
 	 */
 	protected StandardDiseaseModelLabel labelFixture = null;
 
 	/**
 	 * This is the period of time (milliseconds) for the diease model rates.
 	 * Value: 1 day.
 	 */
 	final public static long TEST_TIME_PERIOD = STEMTime.Units.DAY.getMilliseconds();
 
 	/**
 	 * Constructs a new Disease Model test case with the given name. <!--
 	 * begin-user-doc -->
 	 * 
 	 * @param name
 	 *            the name of the test
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public StandardDiseaseModelTest(String name) {
 		super(name);
 	}
 
 	/**
 	 * Returns the fixture for this Disease Model test case.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected StandardDiseaseModel getFixture() {
 		return (StandardDiseaseModel)fixture;
 	}
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#addToTotalPopulationCount(double) <em>Add To Total Population Count</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#addToTotalPopulationCount(double)
 	 * @generated NOT
 	 */
 	public void testAddToTotalPopulationCount__double() {
 		final StandardDiseaseModel sdm = getFixture();
 		final double totalPopBefore = sdm.getTotalPopulationCount();
 		sdm.addToTotalPopulationCount(1.0);
 		assertTrue(sdm.getTotalPopulationCount() == totalPopBefore + 1.0);
 	} // testAddToTotalPopulationCount__double
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#computeTotalPopulationCountReciprocal() <em>Compute Total Population Count Reciprocal</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#computeTotalPopulationCountReciprocal()
 	 * @generated NOT
 	 */
 	public void testComputeTotalPopulationCountReciprocal() {
 		final StandardDiseaseModel sdm = getFixture();
 		sdm.setTotalPopulationCount(1);
 		sdm.computeTotalPopulationCountReciprocal();
 		assertTrue(sdm.getTotalPopulationCountReciprocal() == 1.0);
 
 		sdm.setTotalPopulationCount(2.0);
 		assertTrue(sdm.getTotalPopulationCountReciprocal() == 1.0);
 
 		sdm.computeTotalPopulationCountReciprocal();
 		assertTrue(sdm.getTotalPopulationCountReciprocal() == 0.5);
 	} // testComputeTotalPopulationCountReciprocal
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#addToTotalArea(double) <em>Add To Total Area</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#addToTotalArea(double)
 	 * @generated NOT
 	 */
 	public void testAddToTotalArea__double() {
 		final StandardDiseaseModel sdm = getFixture();
 		final double totalAreaBefore = sdm.getTotalArea();
 		sdm.addToTotalArea(1.0);
 		assertTrue(sdm.getTotalArea() == totalAreaBefore + 1.0);
 	} // testAddToTotalArea__double
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#calculateDelta(org.eclipse.stem.core.model.STEMTime, long, org.eclipse.emf.common.util.EList) <em>Calculate Delta</em>}' operation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#calculateDelta(org.eclipse.stem.core.model.STEMTime, long, org.eclipse.emf.common.util.EList)
 	 * @generated
 	 */
 	public void testCalculateDelta__STEMTime_long_EList() {
 		// TODO: implement this operation test method
		// Ensure that you remove @generated or mark it @generated NOT
		fail();
 	}
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#doModelSpecificAdjustments(org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue) <em>Do Model Specific Adjustments</em>}' operation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.stem.diseasemodels.standard.StandardDiseaseModel#doModelSpecificAdjustments(org.eclipse.stem.diseasemodels.standard.StandardDiseaseModelLabelValue)
 	 * @generated
 	 */
 	public void testDoModelSpecificAdjustments__StandardDiseaseModelLabelValue() {
 		// TODO: implement this operation test method
		// Ensure that you remove @generated or mark it @generated NOT
		fail();
 	}
 
 	/**
 	 * @return a disease model label value to be used for testing
 	 */
 	private StandardDiseaseModelLabel getLabelFixture() {
 		return labelFixture;
 	} // getLabelFixture
 
 	/**
 	 * @param labelFixture
 	 *            the disease model label value for testing
 	 */
 	protected void setLabelFixture(StandardDiseaseModelLabel labelFixture) {
 		this.labelFixture = labelFixture;
 	} // setLabelFixture
 
 	/**
 	 * Create a label value for testing.
 	 */
 	protected abstract StandardDiseaseModelLabel createLabelFixture();
 
 	/**
 	 * test getTransportEdgesToNode
 	 */
 	public void testGetTransportEdgesToNode() {
 		final Graph fixture = DiseaseModelTestUtil.createLatticeGraph(2, 2,
 				DiseaseModelTestUtil.TEST_POPULATION_COUNT,
 				DiseaseModelTestUtil.TEST_AREA);
 		final Node node = DiseaseModelTestUtil.getRegion(fixture, 1, 0);
 
 		final List<Edge> transportEdges = TransportRelationshipLabelImpl
 				.getTransportEdgesToNode(node,
 						DiseaseModelTestUtil.TEST_POPULATION_IDENTIFIER);
 		// 2 + 2 reflexive edges + 1 directed in to the node
 		assertTrue(transportEdges.size() == 5);
 	} // testGetTransportEdgesToNode
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelState, org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel) <em>Initialize Disease State</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModel#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelState,
 	 *      org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel)
 	 * @generated NOT
 	 */
 	public void testInitializeDiseaseState__DiseaseModelState_DiseaseModelLabel() {
 		final StandardDiseaseModel diseaseModel = getFixture();
 
 		if (!(diseaseModel instanceof AggregatingSIDiseaseModel)) {
 
 			final DiseaseModelLabel dml = getLabelFixture();
 
 			// At this point the fixture has already initialized one disease
 			// model
 			// state so its population and area counts are not zero.
 			assertTrue(diseaseModel.getTotalPopulationCount() == DiseaseModelTestUtil.TEST_POPULATION_COUNT);
 			assertTrue(diseaseModel.getTotalArea() == DiseaseModelTestUtil.TEST_AREA);
 
 			DiseaseModelLabelTest.labelRegion(dml,
 					DiseaseModelTestUtil.TEST_POPULATION_IDENTIFIER,
 					DiseaseModelTestUtil.createPopulationRegionNode());
 
 			DiseaseModelState dms = diseaseModel.createDiseaseModelState();
 			fixture.initializeDiseaseState(dms, dml);
 
 			assertTrue(diseaseModel.getTotalPopulationCount() == 2 * DiseaseModelTestUtil.TEST_POPULATION_COUNT);
 			assertTrue(diseaseModel.getTotalArea() == 2 * DiseaseModelTestUtil.TEST_AREA);
 
 			// Let's do it again...should have twice the accumulated counts in
 			// the
 			// disease model
 			dms = diseaseModel.createDiseaseModelState();
 			fixture.initializeDiseaseState(dms, dml);
 
 			assertTrue(diseaseModel.getTotalPopulationCount() == 3 * DiseaseModelTestUtil.TEST_POPULATION_COUNT);
 			assertTrue(diseaseModel.getTotalArea() == 3 * DiseaseModelTestUtil.TEST_AREA);
 		} // if
 	} // testInitializeDiseaseState__DiseaseModelState_DiseaseModelLabel
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.diseasemodels.standard.DiseaseModel#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel) <em>Initialize Disease State</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.diseasemodels.standard.DiseaseModel#initializeDiseaseState(org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel)
 	 * @generated NOT
 	 */
 	public void testInitializeDiseaseState__DiseaseModelLabel() {
 		final StandardDiseaseModel diseaseModel = getFixture();
 		final DiseaseModelLabel dml = getLabelFixture();
 		if (dml instanceof AggregatingSIDiseaseModel) {
 
 			// At this point the fixture has already initialized one disease
 			// model
 			// state so its population and area counts are not zero.
 			assertTrue(diseaseModel.getTotalPopulationCount() == DiseaseModelTestUtil.TEST_POPULATION_COUNT);
 			assertTrue(diseaseModel.getTotalArea() == DiseaseModelTestUtil.TEST_AREA);
 
 			DiseaseModelLabelTest.labelRegion(dml,
 					DiseaseModelTestUtil.TEST_POPULATION_IDENTIFIER,
 					DiseaseModelTestUtil.createPopulationRegionNode());
 
 			fixture.initializeDiseaseState(dml);
 			StandardDiseaseModelState dms = (StandardDiseaseModelState) dml
 					.getDiseaseModelState();
 			assertTrue(dms.getAreaRatio() == diseaseModel.getTotalArea()
 					/ DiseaseModelTestUtil.TEST_AREA);
 		}
 	} // testInitializeDiseaseState__DiseaseModelLabel
 
 	/**
 	 * In this test we specify an area for the population label that should be
 	 * used instead of the area of the region.
 	 */
 	public void testInitializeDiseaseState2() {
 		final StandardDiseaseModel diseaseModel = getFixture();
 		if (!(diseaseModel instanceof AggregatingSIDiseaseModel)) {
 
 			// At this point the fixture has already initialized one disease
 			// model
 			// state so its population and area counts are not zero.
 			assertTrue(diseaseModel.getTotalPopulationCount() == DiseaseModelTestUtil.TEST_POPULATION_COUNT);
 			assertTrue(diseaseModel.getTotalArea() == DiseaseModelTestUtil.TEST_AREA);
 
 			final DiseaseModelLabel dml = diseaseModel
 					.createDiseaseModelLabel();
 
 			DiseaseModelLabelTest.labelRegion(dml,
 					DiseaseModelTestUtil.TEST_POPULATION_IDENTIFIER,
 					DiseaseModelTestUtil.createPopulationRegionNode());
 
 			// Set the area of the population, this should override the area
 			// being
 			// taken from the region.
 			final PopulationLabel populationLabel = dml.getPopulationLabel();
 			populationLabel
 					.setPopulatedArea(DiseaseModelTestUtil.TEST_AREA / 2.0);
 
 			DiseaseModelState dms = diseaseModel.createDiseaseModelState();
 			fixture.initializeDiseaseState(dms, dml);
 
 			assertTrue(diseaseModel.getTotalPopulationCount() == 2 * DiseaseModelTestUtil.TEST_POPULATION_COUNT);
 			assertTrue(diseaseModel.getTotalArea() == 1.5 * DiseaseModelTestUtil.TEST_AREA);
 		}
 	} // testInitializeDiseaseState2
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see junit.framework.TestCase#tearDown()
 	 * @generated NOT
 	 */
 	protected void tearDown() throws Exception {
 		setFixture(null);
 		setLabelFixture(null);
 	} // tearDown
 
 } // StandardDiseaseModelTest
