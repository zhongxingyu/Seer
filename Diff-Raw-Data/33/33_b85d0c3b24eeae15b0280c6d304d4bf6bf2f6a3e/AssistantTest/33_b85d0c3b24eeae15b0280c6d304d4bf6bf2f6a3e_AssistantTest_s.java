 package de.aidger.model.models;
 
 import org.junit.Test;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import static org.junit.Assert.*;
 
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 
 
 /**
  * Tests the Assistant class.
  *
  * @author aidGer Team
  */
 public class AssistantTest {
 
     protected Assistant assistant = null;
 
     @BeforeClass
     public static void beforeClassSetUp() {
         de.aidger.model.Runtime.getInstance().initialize();
     }
 
     @Before
     public void setUp() {
         assistant = new Assistant();
         assistant.setId(1);
         assistant.setEmail("test@example.com");
         assistant.setFirstName("Test");
         assistant.setLastName("Tester");
         assistant.setQualification("g");
     }
 
     /**
      * Test of constructor, of class Assistant.
      */
     @Test
     public void testConstructor() throws AdoHiveException {
         System.out.println("Constructor");
 
         assistant.setNew(true);
         assistant.save();
 
         Assistant result = new Assistant(assistant.getById(
                 assistant.getId()));
 
         assertNotNull(result);
         assertEquals(assistant, result);
     }
 
     /**
      * Test of validation methods, of class Assistant.
      */
     @Test
     public void testValidation() throws AdoHiveException {
         System.out.println("Validation");
 
         assistant.setNew(true);
         assertTrue(assistant.save());
 
         assistant.setEmail(null);
         assertFalse(assistant.save());
         assistant.resetErrors();
 
         assistant.setEmail("test");
         assertFalse(assistant.save());
         assistant.resetErrors();
         assistant.setEmail("test@example.com");
 
         assistant.setFirstName(null);
         assertFalse(assistant.save());
         assistant.resetErrors();
 
         assistant.setFirstName("");
         assertFalse(assistant.save());
         assistant.resetErrors();
         assistant.setFirstName("Test");
 
         assistant.setLastName(null);
         assertFalse(assistant.save());
         assistant.resetErrors();
 
         assistant.setLastName("");
         assertFalse(assistant.save());
         assistant.resetErrors();
         assistant.setLastName("Test");
 
         assistant.setQualification(null);
         assertFalse(assistant.save());
         assistant.resetErrors();
 
         assistant.setQualification("Q");
         assertFalse(assistant.save());
     }
 
     /**
      * Test of validateOnRemove, of class Assistant.
      */
     @Test
     public void testValidateOnRemove() throws AdoHiveException {
         System.out.println("validateOnRemove");
 
         assertTrue(assistant.save());
         assertTrue(assistant.remove());
 
         assistant.save();
         Activity activity = new Activity();
         activity.setAssistantId(assistant.getId());
         activity.setContent("New assistant");
         activity.setCourseId(-1);
         activity.setDate(new java.sql.Date(100));
         activity.setDocumentType("Test Type");
         activity.setProcessor("T");
         activity.setRemark("Remark");
         activity.setSender("Test Sender");
         activity.setType("Test Type");
         activity.save();
 
         assertFalse(assistant.remove());
         assistant.resetErrors();
         activity.remove();
         activity = null;
 
         Employment employment = new Employment();
         employment.setAssistantId(assistant.getId());
         employment.setContractId(-1);
         employment.setCourseId(-1);
         employment.setCostUnit("0711");
         employment.setFunds(1);
         employment.setHourCount(40.0);
         employment.setMonth((byte) 10);
         employment.setQualification("g");
         employment.setRemark("Remark");
         employment.setYear((short) 2010);
         employment.save();
 
         assertFalse(assistant.remove());
         assistant.resetErrors();
         employment.remove();
 
         assertTrue(assistant.remove());
     }
 
     /**
      * Test of clone method, of class Assistant.
      */
     @Test
     public void testClone() {
         System.out.println("clone");
 
         Assistant result = assistant.clone();
 
         assertEquals(assistant.getId(), result.getId());
         assertEquals(assistant.getEmail(), result.getEmail());
         assertEquals(assistant.getFirstName(), result.getFirstName());
         assertEquals(assistant.getLastName(), result.getLastName());
         assertEquals(assistant.getQualification(), result.getQualification());
     }
 
     /**
      * Test of equals method, of class Assistant.
      */
     @Test
     public void testEquals() {
         System.out.println("equals");
         Assistant result = assistant.clone();
 
         assertEquals(assistant, result);
         assertFalse(assistant.equals(new Object()));
     }
 
     /**
      * Test of hashCode method, of class Assistant.
      */
     @Test
     public void testHashCode() {
         System.out.println("hashCode");
         Assistant result = assistant.clone();
 
         assertEquals(assistant.hashCode(), result.hashCode());
     }
 
 }
