 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.view;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.MockNetwork;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementPriority;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.characteristics.RequirementType;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.iterations.IterationModel;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.overview.OverviewTable;
 import edu.wpi.cs.wpisuitetng.modules.requirementmanager.view.requirements.RequirementPanel;
 import edu.wpi.cs.wpisuitetng.network.Network;
 import edu.wpi.cs.wpisuitetng.network.configuration.NetworkConfiguration;
 
 /**
  * @author justinhess
  * @version $Revision: 1.0 $
  */
 public class EditRequirementPanelTest {
 
 	/**
 	 * Method setUp.
 	
 	 * @throws Exception */
 	@Before
 	public void setUp() throws Exception {
 		// Mock network
 		Network.initNetwork(new MockNetwork());
 		Network.getInstance().setDefaultNetworkConfiguration(
 				new NetworkConfiguration("http://wpisuitetng"));
 		
 		// Mock Iteration
 		Iteration iterationTest = new Iteration(0,"Backlog");
 		IterationModel.getInstance().setBacklog(iterationTest);
 		String[] columnNames = {"ID", "Name", "Release #", "Iteration", "Type", "Status", "Priority", "Estimate"};
 		Object[][] data = {};
 		OverviewTable table = new OverviewTable(data, columnNames);		
 		ViewEventController.getInstance().setOverviewTable(table);
 	}
 
 	/**
 	 * check whether the field is enabled or not as default
 	 */
 	@Test 
 	public void defaultEnabilityAndField()
 	{
 		// Create a mock requirement
 		Requirement testRequirement = new Requirement();
 		String testName = "test: Name";
 		String testDescription = "test: Description";
 		String testRelease = "1.0.2";
 		int testEstimate = 0;
 		//int testActualEffort = 0;
 		
 		// Set fields of the mock requirement
 		testRequirement.setName(testName);
 		testRequirement.setDescription(testDescription);
 		testRequirement.setRelease(testRelease);
 		testRequirement.setStatus(RequirementStatus.NEW, true);
 		testRequirement.setPriority(RequirementPriority.LOW,true);
 		testRequirement.setEstimate(testEstimate, false);
 		testRequirement.setType(RequirementType.EPIC);
 		//testRequirement.setEstimate(testActualEffort);
 		
 		// Create an editRequirementPanel
 		RequirementPanel testEdit = new RequirementPanel(testRequirement);
 		
 		// Check enability of all fields
 		assertEquals(true, testEdit.getInfoPanel().getBoxName().isEnabled());
 		assertEquals(true, testEdit.getInfoPanel().getBoxDescription().isEnabled());
 		assertEquals(false, testEdit.getInfoPanel().getBoxIteration().isEnabled());
 		assertEquals(true, testEdit.getInfoPanel().getDropdownType().isEnabled());
 		assertEquals(true, testEdit.getInfoPanel().getDropdownStatus().isEnabled());
 		assertEquals(true, testEdit.getInfoPanel().getPriorityHigh().isEnabled());
 		assertEquals(true, testEdit.getInfoPanel().getPriorityMedium().isEnabled());
 		assertEquals(true, testEdit.getInfoPanel().getPriorityLow().isEnabled());
 		assertEquals(true, testEdit.getInfoPanel().getPriorityBlank().isEnabled());
 		assertEquals(true, testEdit.getInfoPanel().getBoxEstimate().isEnabled());
 		assertEquals(false, testEdit.getButtonPanel().getButtonOK().isEnabled());
 		assertEquals(false, testEdit.getButtonPanel().getButtonClear().isEnabled());
 		assertEquals(true, testEdit.getButtonPanel().getButtonCancel().isEnabled());
 		assertEquals(true, testEdit.getButtonPanel().getButtonDelete().isEnabled());
 		
 		// Check for default values in each field
 		assertEquals(testName,testEdit.getInfoPanel().getBoxName().getText());
 		assertEquals(testDescription,testEdit.getInfoPanel().getBoxDescription().getText());
 		assertEquals(testRelease,testEdit.getInfoPanel().getBoxReleaseNum().getText());
 		assertEquals(RequirementStatus.NEW,testEdit.getInfoPanel().getDropdownStatus().getSelectedItem());
 		assertEquals(true,testEdit.getInfoPanel().getPriorityLow().isSelected());
 		assertEquals("0",testEdit.getInfoPanel().getBoxEstimate().getText());
 		assertEquals(RequirementType.EPIC,testEdit.getInfoPanel().getDropdownType().getSelectedItem());
 		
 	}
 
 	/**
 	 * Check the error messages when invalid fields are filled
 	 */
 	@Test
 	public void errorFieldTest() 
 	{
 		// Create mock requirement
 		Requirement testRequirement = new Requirement();
 		
 		//int testActualEffort = 0;
 		
 		//testRequirement.setEstimate(testActualEffort);
 		
 		RequirementPanel testEdit = new RequirementPanel(testRequirement);
 		
 		String errorMessageNoninterger = "Estimate must be non-negative integer";
 		String errorMessageNoMore100 = "No more than 100 chars";
 		String errorMessageRequiredDescription = "** Description is REQUIRED";
 		
 		String hundredCharText = "0";
 		// Generate Hundred character string
 		for(int i = 0; i<100; i++)
 		{
 			hundredCharText += "0";
 		}
 		
 		// adding invalid fields
 		testEdit.getInfoPanel().getBoxName().setText(hundredCharText);
 		testEdit.getInfoPanel().getBoxEstimate().setText("-134");
 		testEdit.getInfoPanel().getBoxDescription().setText("Desc.");
 		testEdit.getInfoPanel().keyReleased(null);
 		testEdit.getButtonPanel().getButtonOK().doClick();
 		testEdit.getInfoPanel().validateFields(true);
 		
 		// has to be nonnegative, has to have name, has to have description
 		assertEquals(errorMessageNoninterger,testEdit.getInfoPanel().getErrorEstimate().getText());
 		assertEquals(errorMessageNoMore100,testEdit.getInfoPanel().getErrorName().getText());
 
 		// Iteration is unable, Dropdown status is enable
 		assertEquals(false, testEdit.getInfoPanel().getBoxIteration().isEnabled());
 		assertEquals(true, testEdit.getInfoPanel().getDropdownStatus().isEnabled());
 		
 		testEdit.getInfoPanel().getBoxName().setText(hundredCharText);
 		testEdit.getInfoPanel().getBoxEstimate().setText("StringCharacter");
 		testEdit.getInfoPanel().getBoxDescription().setText(null);
 		testEdit.getButtonPanel().getButtonOK().doClick();
 		testEdit.getInfoPanel().validateFields(true);
 
 		assertEquals(errorMessageNoMore100,testEdit.getInfoPanel().getErrorName().getText());
 		assertEquals(errorMessageNoninterger,testEdit.getInfoPanel().getErrorEstimate().getText());
 		assertEquals(errorMessageRequiredDescription,testEdit.getInfoPanel().getErrorDescription().getText());
 		
 	}
 	
 	
 	/**
 	 * Check when required fields are not filled
 	 */
 	@Test
 	public void errorRequiredFieldTest() {
 		
 		// Moc requirement
 		Requirement testRequirement = new Requirement();
 		String testName = "test: Name";
 		String testDescription = "test: Description";
 		String testRelease = "1.0.2";
 		int testEstimate = 0;
 		//int testActualEffort = 0;
 		testRequirement.setName(testName);
 		testRequirement.setDescription(testDescription);
 		testRequirement.setRelease(testRelease);
 		testRequirement.setStatus(RequirementStatus.NEW, true);
 		testRequirement.setPriority(RequirementPriority.HIGH,true);
 		testRequirement.setEstimate(testEstimate, false);
 		testRequirement.setType(RequirementType.EPIC);
 		//testRequirement.setEstimate(testActualEffort)/;
 		
 		RequirementPanel testEdit = new RequirementPanel(testRequirement);
 		// a field is added correctly but both name and description are filled with blanks
 		testEdit.getInfoPanel().getBoxEstimate().setText("-134");
 		testEdit.getInfoPanel().getBoxName().setText("  ");
 		testEdit.getInfoPanel().getBoxDescription().setText("Desc.");
 		// release pressed key
 		testEdit.getInfoPanel().keyReleased(null);
 		
 		assertEquals(false, testEdit.getButtonPanel().getButtonOK().isEnabled());
 		assertEquals(true, testEdit.getButtonPanel().getButtonClear().isEnabled());
 		assertEquals(true, testEdit.getButtonPanel().getButtonCancel().isEnabled());
 		
 		testEdit.getInfoPanel().getBoxName().setText("Name");
 		testEdit.getInfoPanel().getBoxDescription().setText(" ");
 		// release pressed key
 		testEdit.getInfoPanel().keyReleased(null);
 		
 		// can't create because no name/description, but a field has been changed
 		assertEquals(false, testEdit.getButtonPanel().getButtonOK().isEnabled());
 		assertEquals(true, testEdit.getButtonPanel().getButtonClear().isEnabled());
 		assertEquals(true, testEdit.getButtonPanel().getButtonCancel().isEnabled());
 		
 
 	}
 	
 	/**
 	 * Check when undo the change
 	 */
 	@Test
 	public void undoChangeButtonTest() {
 		
 		Requirement testRequirement = new Requirement();
 		String testName = "test: Name";
 		String testDescription = "test: Description";
 		String testRelease = "1.0.2";
 		int testEstimate = 0;
 		//int testActualEffort = 0;
 		
 		testRequirement.setName(testName);
 		testRequirement.setDescription(testDescription);
 		testRequirement.setRelease(testRelease);
 		testRequirement.setStatus(RequirementStatus.NEW, true);
 		testRequirement.setPriority(RequirementPriority.MEDIUM,true);
 		testRequirement.setEstimate(testEstimate, false);
 		testRequirement.setType(RequirementType.EPIC);
 		//testRequirement.setEstimate(testActualEffort);
 		
 		RequirementPanel testEdit = new RequirementPanel(testRequirement);
 		
 		// set to each field random stuffs to test clear functionality
 		
 		testEdit.getInfoPanel().getBoxName().setText(testName);
 		testEdit.getInfoPanel().getBoxDescription().setText(testDescription);
 		testEdit.getInfoPanel().getBoxIteration().addItem("Iteration test");
 		testEdit.getInfoPanel().getBoxIteration().setSelectedItem("Iteration test");
 		testEdit.getInfoPanel().getDropdownType().setSelectedItem(RequirementType.SCENARIO);
 		testEdit.getInfoPanel().getDropdownStatus().setSelectedItem(RequirementStatus.INPROGRESS);
 		testEdit.getInfoPanel().getPriorityMedium().doClick();
 		testEdit.getInfoPanel().getBoxEstimate().setText("4");
 		
 		
 		testEdit.getButtonPanel().getButtonClear().doClick();
 		
 		assertEquals(testName,testEdit.getInfoPanel().getBoxName().getText());
 		assertEquals(testDescription,testEdit.getInfoPanel().getBoxDescription().getText());
 		assertEquals(testRelease,testEdit.getInfoPanel().getBoxReleaseNum().getText());
 		assertEquals(RequirementStatus.NEW,testEdit.getInfoPanel().getDropdownStatus().getSelectedItem());
 		assertEquals(true,testEdit.getInfoPanel().getPriorityMedium().isSelected());
 		assertEquals("0",testEdit.getInfoPanel().getBoxEstimate().getText());
 		assertEquals(RequirementType.EPIC,testEdit.getInfoPanel().getDropdownType().getSelectedItem());
 		
 		
 	}
 	
 	/**
 	 * test fifferent fields
 	 */
	@Test
 	public void updateButtonTest2()
 	{
 		Requirement testRequirement = new Requirement();
 		String testName = "test: Name";
 		String testDescription = "test: Description";
 		String testRelease = "1.0.2";
 		int testEstimate = 0;
 		//int testActualEffort = 0;
 		
 		String updateTestName = "Update test: Name";
 		String updateTestDescription = "Update test: Description";
 		String updateTestRelease = "1.0.3";
 		int updateTestEstimate = 1;
 		
 		testRequirement.setName(testName);
 		testRequirement.setDescription(testDescription);
 		testRequirement.setRelease(testRelease);
 		testRequirement.setStatus(RequirementStatus.NEW, true);
 		testRequirement.setPriority(RequirementPriority.LOW,true);
 		testRequirement.setEstimate(testEstimate, false);
 		testRequirement.setType(RequirementType.EPIC);
 		testRequirement.setIteration("Backlog",true);
 		//testRequirement.setEstimate(testActualEffort);
 		
 		RequirementPanel testEdit = new RequirementPanel(testRequirement);
 		
 		testEdit.getInfoPanel().getBoxName().setText(updateTestName);
 		testEdit.getInfoPanel().getBoxDescription().setText(updateTestDescription);
 		testEdit.getInfoPanel().getDropdownType().setSelectedItem(RequirementType.THEME);
 		testEdit.getInfoPanel().getDropdownStatus().setSelectedItem(RequirementStatus.INPROGRESS);
 		testEdit.getInfoPanel().getPriorityHigh().doClick();
 		testEdit.getInfoPanel().getBoxEstimate().setText("4");
 		testEdit.getInfoPanel().getBoxIteration().addItem("Iteration test");
 		testEdit.getInfoPanel().getBoxIteration().setSelectedItem("Iteration test");
 		testEdit.getInfoPanel().update();
 		
 		// check the result
 		assertEquals(updateTestName,testEdit.getDisplayRequirement().getName());
 		assertEquals(updateTestDescription,testEdit.getDisplayRequirement().getDescription());
 		assertEquals(RequirementType.THEME,testEdit.getDisplayRequirement().getType());
 		assertEquals("Iteration test",testEdit.getDisplayRequirement().getIteration());
 		assertEquals(RequirementStatus.INPROGRESS,testEdit.getDisplayRequirement().getStatus());
 		assertEquals(RequirementPriority.HIGH,testEdit.getDisplayRequirement().getPriority());
 		assertEquals(4,testEdit.getDisplayRequirement().getEstimate());
	}
 	
 }
 	
 	
