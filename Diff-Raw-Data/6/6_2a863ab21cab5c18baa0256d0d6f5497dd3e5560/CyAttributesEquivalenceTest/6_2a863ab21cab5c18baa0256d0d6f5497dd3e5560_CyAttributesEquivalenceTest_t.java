 package cytoscape.data.unitTests;
 
 import junit.framework.TestCase;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.CyAttributesImpl;
 import cytoscape.data.GraphObjAttributes;
 import cytoscape.data.GraphObjAttributesImpl;
 
 import javax.swing.*;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Tests that attributes set via CyAttributes are accessible via
  * GraphObjAttributes, and vice versa.  This is an important test
  * of backward compatibility.
  */
 public class CyAttributesEquivalenceTest extends TestCase {
     private CyAttributes cyAttributes;
     private GraphObjAttributes graphAttributes;
     private static final String DUMMY_ID_1 = "id_1";
     private static final String DUMMY_ID_2 = "id_2";
     private static final String DUMMY_BOOLEAN_ATTRIBUTE = "attribute1";
     private static final String DUMMY_STRING_ATTRIBUTE = "attribute2";
     private static final String DUMMY_LIST_ATTRIBUTE = "attribute3";
     private static final String DUMMY_MAP_ATTRIBUTE = "attribute4";
     private static final String DUMMY_BUTTON_ATTRIBUTE = "attribute5";
 
     /**
      * Set things up.
      * @throws Exception All Exceptions.
      */
     protected void setUp() throws Exception {
         cyAttributes = new CyAttributesImpl();
         graphAttributes = new GraphObjAttributesImpl (cyAttributes);
     }
 
     /**
      * Tests Boolean Values.
      */
     public void testBooleanValue() {
         //  Verify that attribute does not (yet) exist in either
         //  CyAttributes or GraphObjAttributes.
         boolean exists = cyAttributes.hasAttribute(DUMMY_ID_1,
                 DUMMY_BOOLEAN_ATTRIBUTE);
         assertEquals (false, exists);
 
         exists = graphAttributes.hasAttribute(DUMMY_BOOLEAN_ATTRIBUTE,
                 DUMMY_ID_1);
         assertEquals (false, exists);
 
         //  Since the attribute does not yet exist, this should be null in
         //  both CyAttribues and GraphObjAttribues.
         Boolean value = cyAttributes.getBooleanAttribute
                 (DUMMY_ID_1, DUMMY_BOOLEAN_ATTRIBUTE);
         assertEquals (null, value);
         Object o = graphAttributes.get(DUMMY_BOOLEAN_ATTRIBUTE, DUMMY_ID_1);
         assertEquals (null, o);
 
         //  Set a Boolean Attribute Value;  should work
         cyAttributes.setAttribute(DUMMY_ID_1, DUMMY_BOOLEAN_ATTRIBUTE,
                 new Boolean (true));
 
         //  Verify that attribute now exists in CyAttributes.
         exists = cyAttributes.hasAttribute(DUMMY_ID_1, DUMMY_BOOLEAN_ATTRIBUTE);
         assertEquals (true, exists);
 
         //  Verify that attribute now exists in GraphObjAttributes
         exists = graphAttributes.hasAttribute(DUMMY_BOOLEAN_ATTRIBUTE,
                 DUMMY_ID_1);
         assertEquals (true, exists);
 
         //  Verify # of Attributes in GraphObjAttributes
         int num = graphAttributes.numberOfAttributes();
         assertEquals (1, num);
 
         //  Verify value stored in CyAttributes
         value = cyAttributes.getBooleanAttribute(DUMMY_ID_1,
                 DUMMY_BOOLEAN_ATTRIBUTE);
         assertEquals (true, value.booleanValue());
 
         //  Verify value stored in GraphObjAttributes
         value = (Boolean) graphAttributes.get(DUMMY_BOOLEAN_ATTRIBUTE,
                 DUMMY_ID_1);
         assertEquals (true, value.booleanValue());
 
         //  Check out the attribute names in CyAttributes
         String attribs[] = cyAttributes.getAttributeNames();
         assertEquals (1, attribs.length);
         assertEquals (DUMMY_BOOLEAN_ATTRIBUTE, attribs[0]);
 
         //  Check out the attribute names in GraphObjAttributes
         attribs = graphAttributes.getAttributeNames();
         assertEquals (1, attribs.length);
         assertEquals (DUMMY_BOOLEAN_ATTRIBUTE, attribs[0]);
 
         //  Now delete the attribute
         boolean success = cyAttributes.deleteAttribute(DUMMY_ID_1,
                 DUMMY_BOOLEAN_ATTRIBUTE);
         assertEquals (true, success);
 
         //  Verify that attribute no longer exists in CyAttributes
         exists = cyAttributes.hasAttribute(DUMMY_ID_1, DUMMY_BOOLEAN_ATTRIBUTE);
         assertEquals (false, exists);
 
         //  Verify that attribute no longer exists in GraphObjAttributes
         exists = graphAttributes.hasAttribute(DUMMY_BOOLEAN_ATTRIBUTE,
                 DUMMY_ID_1);
         assertEquals (false, exists);
 
         //  Try setting an Integer value;  this should fail
         try {
             graphAttributes.set(DUMMY_BOOLEAN_ATTRIBUTE, DUMMY_ID_2, new
                 Integer (5));
             fail ("Illegal Argument Exception should have been thrown.");
         } catch (IllegalArgumentException e) {
             assertTrue (e != null);
         }
     }
 
     /**
      * Tests Boolean Values, Take 2.
      */
     public void testBooleanValues2() {
         //  Try setting a value via GraphObjAttributes
         graphAttributes.set(DUMMY_BOOLEAN_ATTRIBUTE, DUMMY_ID_2,
                 new Boolean(false));
 
         //  Verify value stored in CyAttributes
         //  Due to workaround, single values stored via graphObjAttributes
         //  are stored as lists.
         List list = cyAttributes.getAttributeList(DUMMY_ID_2,
                 DUMMY_BOOLEAN_ATTRIBUTE);
         assertEquals (1, list.size());
         Boolean value = (Boolean) list.get(0);
         assertEquals (false, value.booleanValue());
 
         //  Verify value stored in GraphObjAttributes
         value = (Boolean) graphAttributes.get(DUMMY_BOOLEAN_ATTRIBUTE,
                 DUMMY_ID_2);
         assertEquals (false, value.booleanValue());
 
         //  Try setting an Integer value;  this should fail
         try {
             graphAttributes.set(DUMMY_BOOLEAN_ATTRIBUTE, DUMMY_ID_2, new
                 Integer (5));
             fail ("Illegal Argument Exception should have been thrown.");
         } catch (IllegalArgumentException e) {
             assertTrue (e != null);
         }
 
     }
 
     /**
      * Tests String Values.
      */
     public void testStringValue() {
         //  Set a String Attribute Value via CyAttributes
         cyAttributes.setAttribute(DUMMY_ID_1, DUMMY_STRING_ATTRIBUTE,
                 new String ("Cytoscape"));
 
         //  Verify that attribute now exists in CyAttributes
         boolean exists = cyAttributes.hasAttribute(DUMMY_ID_1,
                 DUMMY_STRING_ATTRIBUTE);
         assertEquals (true, exists);
 
         //  Verify that attribute now exists in GraphObjAttributes
         exists = graphAttributes.hasAttribute(DUMMY_STRING_ATTRIBUTE,
                 DUMMY_ID_1);
         assertEquals (true, exists);
 
         //  Verify value stored in CyAttributes
         String value = cyAttributes.getStringAttribute(DUMMY_ID_1,
                 DUMMY_STRING_ATTRIBUTE);
         assertEquals ("Cytoscape", value);
 
         //  Verify value stored in GraphObjAttributes
         value = (String) graphAttributes.get(DUMMY_STRING_ATTRIBUTE,
                 DUMMY_ID_1);
         assertEquals ("Cytoscape", value);
         value = graphAttributes.getStringValue(DUMMY_STRING_ATTRIBUTE,
                 DUMMY_ID_1);
         assertEquals ("Cytoscape", value);
 
         //  Try getting as a Double;  should result in a null;
         Double d = graphAttributes.getDoubleValue(DUMMY_STRING_ATTRIBUTE,
                 DUMMY_ID_1);
         assertTrue (d==null);
     }
 
     /**
      * Tests Simple Lists, Take 1
      */
     public void testSimpleLists1() {
         //  Set a list via CyAttributes
         List list = new ArrayList();
         list.add(new Integer (5));
         list.add(new Integer (6));
         cyAttributes.setAttributeList(DUMMY_ID_1, DUMMY_LIST_ATTRIBUTE, list);
 
         //  Verify value exists in CyAttributes
         boolean exists = cyAttributes.hasAttribute(DUMMY_ID_1,
                 DUMMY_LIST_ATTRIBUTE);
         assertEquals (true, exists);
 
         //  Verify value exists in GraphObjAttributes
         exists = graphAttributes.hasAttribute(DUMMY_LIST_ATTRIBUTE, DUMMY_ID_1);
         assertEquals (true, exists);
 
         //  Get the list back from CyAttributes, and verify its contents
         List storedList = cyAttributes.getAttributeList(DUMMY_ID_1,
                 DUMMY_LIST_ATTRIBUTE);
         assertEquals (2, storedList.size());
         Integer int0 = (Integer) storedList.get(0);
         Integer int1 = (Integer) storedList.get(1);
         assertEquals (5, int0.intValue());
         assertEquals (6, int1.intValue());
 
         //  Get the list back from GraphObjAttributes, and verify its contents
         storedList = graphAttributes.getList(DUMMY_LIST_ATTRIBUTE, DUMMY_ID_1);
         assertEquals (2, storedList.size());
         int0 = (Integer) storedList.get(0);
         int1 = (Integer) storedList.get(1);
         assertEquals (5, int0.intValue());
         assertEquals (6, int1.intValue());
     }
 
     /**
      * Tests Simple Lists, Take 2
      */
     public void testSimpleLists2() {
         //  Set a list via GraphObjAttributes
         graphAttributes.append(DUMMY_LIST_ATTRIBUTE, DUMMY_ID_1,
                 new Integer(5));
         graphAttributes.append(DUMMY_LIST_ATTRIBUTE, DUMMY_ID_1,
                 new Integer(6));
 
         //  Verify value exists in CyAttributes
         boolean exists = cyAttributes.hasAttribute(DUMMY_ID_1,
                 DUMMY_LIST_ATTRIBUTE);
         assertEquals (true, exists);
 
         //  Verify value exists in GraphObjAttributes
         exists = graphAttributes.hasAttribute(DUMMY_LIST_ATTRIBUTE, DUMMY_ID_1);
         assertEquals (true, exists);
 
         //  Get the list back from CyAttributes, and verify its contents
         List storedList = cyAttributes.getAttributeList(DUMMY_ID_1,
                 DUMMY_LIST_ATTRIBUTE);
         assertEquals (2, storedList.size());
         Integer int0 = (Integer) storedList.get(0);
         Integer int1 = (Integer) storedList.get(1);
         assertEquals (5, int0.intValue());
         assertEquals (6, int1.intValue());
 
         //  Get the list back from GraphObjAttributes, and verify its contents
         storedList = graphAttributes.getList(DUMMY_LIST_ATTRIBUTE, DUMMY_ID_1);
         assertEquals (2, storedList.size());
         int0 = (Integer) storedList.get(0);
         int1 = (Integer) storedList.get(1);
         assertEquals (5, int0.intValue());
         assertEquals (6, int1.intValue());
     }
 
     /**
      * Tests that we can still store arbitary objects in GraphObjAttributes.
      */
     public void testArbitraryObjects() {
         //  Create an arbitary object
         JButton button = new JButton ("Hello World!");
 
         //  Store it in GraphObjAttributes
         graphAttributes.set(DUMMY_BUTTON_ATTRIBUTE, DUMMY_ID_1, button);
 
         //  Get it back
         button = (JButton) graphAttributes.get(DUMMY_BUTTON_ATTRIBUTE,
                 DUMMY_ID_1);
        assertEquals ("Hello World!", button.getText());
     }
 
     /**
      * Runs just this one unit test.
      */
     public static void main(String[] args) {
         junit.textui.TestRunner.run(CyAttributesEquivalenceTest.class);
     }
}
