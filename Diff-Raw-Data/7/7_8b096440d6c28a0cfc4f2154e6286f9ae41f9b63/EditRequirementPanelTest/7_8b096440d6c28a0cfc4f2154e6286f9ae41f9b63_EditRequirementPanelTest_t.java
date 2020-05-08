 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.view;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.MockNetwork;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.RequirementPriority;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.RequirementType;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.TransactionHistory;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.Requirements.EditRequirementPanel;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.Requirements.NewRequirementPanel;
 import edu.wpi.cs.wpisuitetng.network.Network;
 import edu.wpi.cs.wpisuitetng.network.configuration.NetworkConfiguration;
 
 public class EditRequirementPanelTest {
 
 	@Before
 	public void setUp() throws Exception {
 		Network.initNetwork(new MockNetwork());
 		Network.getInstance().setDefaultNetworkConfiguration(
 				new NetworkConfiguration("http://wpisuitetng"));
 
 	}
 
 
 	@Test
 	public void errorFieldTest() {
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
 		testRequirement.setPriority(RequirementPriority.LOW,true);
 		testRequirement.setEstimate(testEstimate);
 		testRequirement.setType(RequirementType.EPIC);
 		//testRequirement.setEstimate(testActualEffort);
 		
 		EditRequirementPanel testEdit = new EditRequirementPanel(testRequirement);
 		
 		String errorMessageNoninterger = "** Please enter a non-negative integer";
 		String errorMessageNoMore100 = "No more than 100 chars";
 		String errorMessageRequiredName = "** Name is REQUIRED";
 		String errorMessageRequiredDescription = "** Description is REQUIRED";
 
 		
 		// initial edit field
 		assertEquals(testName,testEdit.getBoxName().getText());
 		assertEquals(testDescription,testEdit.getBoxDescription().getText());
 		assertEquals(testRelease,testEdit.getBoxReleaseNum().getText());
 		assertEquals(RequirementStatus.NEW,testEdit.getDropdownStatus().getSelectedItem());
 		assertEquals(true,testEdit.getPriorityLow().isSelected());
 		assertEquals("0",testEdit.getBoxEstimate().getText());
 		assertEquals(RequirementType.EPIC,testEdit.getDropdownType().getSelectedItem());
 		
 		String hundredCharText = "0";
 		
 		for(int i = 0; i<100; i++)
 		{
 			hundredCharText = hundredCharText +"0";
 		}
 		
 		testEdit.getBoxName().setText("  ");
 		testEdit.getBoxEstimate().setText("-134");
 		testEdit.getBoxDescription().setText("   ");
 		
 		testEdit.getButtonUpdate().doClick();
 		
 		// has to be nonnegative, has to have name, has to have description
 		assertEquals(errorMessageNoninterger,testEdit.getErrorEstimate().getText());
 		assertEquals(errorMessageRequiredName,testEdit.getErrorName().getText());
 		assertEquals(errorMessageRequiredDescription,testEdit.getErrorDescription().getText());
 		// Iteration is unable, Dropdown status is enable
 		assertEquals(false, testEdit.getBoxIteration().isEnabled());
 		assertEquals(true, testEdit.getDropdownStatus().isEnabled());
 		
 		testEdit.getBoxName().setText(hundredCharText);
 		testEdit.getBoxEstimate().setText("StringCharacter");
 		testEdit.getBoxDescription().setText(null);
 		testEdit.getButtonUpdate().doClick();
 		
 		assertEquals(errorMessageNoMore100,testEdit.getErrorName().getText());
 		assertEquals(errorMessageNoninterger,testEdit.getErrorEstimate().getText());
 		assertEquals(errorMessageRequiredDescription,testEdit.getErrorDescription().getText());
 		
 	}
 	
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
 		testRequirement.setPriority(RequirementPriority.LOW,true);
 		testRequirement.setEstimate(testEstimate);
 		testRequirement.setType(RequirementType.EPIC);
 		//testRequirement.setEstimate(testActualEffort);
 		
 		EditRequirementPanel testEdit = new EditRequirementPanel(testRequirement);
 		
 		String errorMessageNoninterger = "** Please enter a non-negative integer";
 		String errorMessageNoMore100 = "No more than 100 chars";
 		String errorMessageRequiredName = "** Name is REQUIRED";
 		String errorMessageRequiredDescription = "** Description is REQUIRED";
 		
 		// set to each field random stuffs to test clear functionality
 		
 		testEdit.getBoxName().setText(testName);
 		testEdit.getBoxDescription().setText(testDescription);
 		testEdit.getBoxIteration().setText("Iteration test");
 		testEdit.getDropdownType().setSelectedItem(RequirementType.SCENARIO);
 		testEdit.getDropdownStatus().setSelectedItem(RequirementStatus.INPROGRESS);
 		testEdit.getPriorityMedium().doClick();
 		testEdit.getBoxEstimate().setText("4");
 		
 		
 		testEdit.getButtonClear().doClick();
 		
 		assertEquals(testName,testEdit.getBoxName().getText());
 		assertEquals(testDescription,testEdit.getBoxDescription().getText());
 		assertEquals(testRelease,testEdit.getBoxReleaseNum().getText());
 		assertEquals(RequirementStatus.NEW,testEdit.getDropdownStatus().getSelectedItem());
 		assertEquals(true,testEdit.getPriorityLow().isSelected());
 		assertEquals("0",testEdit.getBoxEstimate().getText());
 		assertEquals(RequirementType.EPIC,testEdit.getDropdownType().getSelectedItem());
 		
 		
 	}
 	
 	@Test
 	public void updateButtonTest()
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
 		testRequirement.setEstimate(testEstimate);
 		testRequirement.setType(RequirementType.EPIC);
		testRequirement.setIteration("Backlog",true);
 		//testRequirement.setEstimate(testActualEffort);
 		
 		EditRequirementPanel testEdit = new EditRequirementPanel(testRequirement);
 		
 		testEdit.getBoxName().setText(updateTestName);
 		testEdit.getBoxDescription().setText(updateTestDescription);
 		testEdit.getDropdownType().setSelectedItem(RequirementType.THEME);
 		testEdit.getDropdownStatus().setSelectedItem(RequirementStatus.COMPLETE);
 		testEdit.getPriorityHigh().doClick();
 		testEdit.getBoxEstimate().setText("4");
 		testEdit.getBoxIteration().setText("Iteration test");
 		testEdit.update();
 		
 		assertEquals(updateTestName,testEdit.getRequirementBeingEdited().getName());
 		assertEquals(updateTestDescription,testEdit.getRequirementBeingEdited().getDescription());
 		assertEquals(RequirementType.THEME,testEdit.getRequirementBeingEdited().getType());
		assertEquals("Iteration test",testEdit.getRequirementBeingEdited().getIteration());
 		assertEquals(RequirementStatus.COMPLETE,testEdit.getRequirementBeingEdited().getStatus());
 		assertEquals(RequirementPriority.HIGH,testEdit.getRequirementBeingEdited().getPriority());
 		assertEquals(4,testEdit.getRequirementBeingEdited().getEstimate());
 	}
 	
 	
 }
