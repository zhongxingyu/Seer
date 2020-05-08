 package no.feide.moria.directory;
 
 import junit.framework.*;
 
 /**
  * JUnit tests for the <code>UserAttribute</code> class.
  */
 public class UserAttributeTest
 extends TestCase {
     
     /** Legal attribute name. */
     private static final String legalName = "legalName";
     
     /** Attribute values. */
     private static final String[] legalValues = {"value1", "", "value3", null, "value4"};
     
     /** Internal representation of the user attribute. */
     private UserAttribute attribute;
 
     /**
      * Returns the full test suite.
      * @return The test suite.
      */
     public static Test suite() {
 
         return new TestSuite(UserAttributeTest.class);
         
     }
     
     /**
      * Prepare.
      */
     public void setUp() {
 
         // Create a few illegal attributes.
         String[] illegalNames = {"", null};
         for (int i=0; i<illegalNames.length; i++)
 	        try {
 	            Assert.assertNull("Created an illegal attribute", new UserAttribute(illegalNames[i], new String[] {}));
 	        } catch (IllegalAttributeException e) {
 	            // Normal.
 	        }
         
         // Create a legal attribute.
         try {
             attribute = new UserAttribute(legalName, legalValues);
             Assert.assertNotNull("Failed to create a legal attribute", attribute);
         } catch (IllegalAttributeException e) {
             Assert.fail("Failed to create a legal attribute");
         }
 
     }
 
 
     /**
      * Clean up.
      */
     public void tearDown() {
 
         attribute = null;
 
     }
 
     
     /**
      * Test <code>getName()</code>.
      */
     public void testGetName() {
 
        Assert.assertEquals("Attribute name is incorrect", legalName, attribute.getName());
 
     }
     
     
     /**
      * Test <code>getAttributes()</code>.
      */
     public void testGetAttributes() {
         
         String[] values = attribute.getValues();
        Assert.assertEquals("Returned attribute array of different length", legalValues.length, values.length);
         for (int i=0; i<values.length; i++)
            Assert.assertEquals("Value "+i+" differs", values[i], legalValues[i]);
                 
         
     }
 
 }
