 /**
  * 
  */
 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.models;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 /**
  * Tests the jSON conversion functions and all the getters and setters
  * 
  * @author Benjamin Senecal
  */
 public class RequirementTest {
 
 	@Test
 	  public void jSONConversionTests() {
 	    Requirement object = new Requirement(4, "Test", "A test");
 	    
 	    assertEquals(object.getStatus(),RequirementStatus.NEW);
 	    assertEquals(object.getPriority(),RequirementPriority.BLANK);
 	    
 	    object.setRelease("1.1.01");
 	    object.setStatus(RequirementStatus.INPROGRESS);
 	    object.setPriority(RequirementPriority.MEDIUM);
 	    object.setEffort(10);
 	    object.setEstimate(1);
 	    
 	    Requirement origObject = object;  // change here
 	    String jsonMessage = object.toJSON();
 	    Requirement newObject = Requirement.fromJson(jsonMessage);  // change here...
 	    assertTrue(newObject instanceof Requirement);
 	    
 	    assertEquals(origObject.getId(), 4);
 	    assertEquals(origObject.getName(), "Test");
 	    assertEquals(origObject.getRelease(), "1.1.01");
 	    assertEquals(origObject.getStatus(), RequirementStatus.INPROGRESS);
 	    assertEquals(origObject.getPriority(), RequirementPriority.MEDIUM);
 	    assertEquals(origObject.getDescription(), "A test");
 	    assertEquals(origObject.getEstimate(), 1);
 	    assertEquals(origObject.getEffort(), 10);
 	    
 	    TransactionHistory history = origObject.getHistory();
 	    assertEquals(history.getItem(0).getMessage(),"REQUIREMENT CREATED");
	    assertEquals(history.getItem(0).getTS(),System.currentTimeMillis(), 100);
 	}
 }
