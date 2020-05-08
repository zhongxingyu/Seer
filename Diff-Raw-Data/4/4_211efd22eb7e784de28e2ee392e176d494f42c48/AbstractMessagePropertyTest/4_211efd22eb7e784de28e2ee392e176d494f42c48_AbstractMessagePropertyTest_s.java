 package http;
 
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author Karl Bennett
  */
 public abstract class AbstractMessagePropertyTest<P> {
 
     public static final String NAME_ONE = "name_one";
     public static final String VALUE_ONE = "value_one";
     public static final String NAME_TWO = "name_two";
     public static final String VALUE_TWO = "value_two";
     public static final String NAME_THREE = "name_three";
     public static final String VALUE_THREE = "value_three";
 
 
     protected interface MessageExecutor<P> {
 
         public abstract P newProperty(String name, Object value);
 
         public abstract <T> P getProperty(Message<T> message, String name);
 
         public abstract <T> Collection<P> getProperties(Message<T> message);
 
         public abstract <T> void setProperties(Message<T> message, Collection<P> properties);
 
         public abstract <T> void addProperty(Message<T> message, String name, Object value);
 
         public abstract <T> void addProperty(Message<T> message, P property);
     }
 
 
     private MessageExecutor<P> messageExecutor;
     private P propertyOne;
     private P propertyTwo;
     private P propertyThree;
     private Collection<P> properties;
 
 
     protected AbstractMessagePropertyTest(MessageExecutor<P> messageExecutor) {
 
         this.messageExecutor = messageExecutor;
 
         propertyOne = messageExecutor.newProperty(NAME_ONE, VALUE_ONE);
         propertyTwo = messageExecutor.newProperty(NAME_TWO, VALUE_TWO);
         propertyThree = messageExecutor.newProperty(NAME_THREE, VALUE_THREE);
 
         properties = Arrays.asList(propertyOne, propertyTwo, propertyThree);
 
         exposeProperties(propertyOne, propertyTwo, propertyThree, properties);
     }
 
     protected void exposeProperties(P propertyOne, P propertyTwo, P propertyThree, Collection<P> properties) {
     }
 
     @Test
     public void testGetPropertiesWhenNoPropertiesHaveBeenAdded() throws Exception {
 
         Message<Object> message = new Message<Object>();
 
         Collection<P> properties = messageExecutor.getProperties(message);
 
         assertNotNull("a collection of properties should be returned.", properties);
         assertEquals("the collection of properties should be empty.", 0, properties.size());
     }
 
     @Test
     public void testSetProperties() throws Exception {
 
         Message<Object> message = new Message<Object>();
 
         messageExecutor.setProperties(message, properties);
 
         Collection<P> properties = messageExecutor.getProperties(message);
 
         assertEquals("the number of message properties is correct", 3, properties.size());
         assertTrue("the first property has been set correctly", properties.contains(propertyOne));
        assertTrue("the second property has been set correctly", properties.contains(propertyOne));
        assertTrue("the third property has been set correctly", properties.contains(propertyOne));
     }
 
     @Test
     public void testSetEmptyProperties() throws Exception {
 
         addNoPropertiesTest(Collections.<P>emptySet());
     }
 
     @Test
     public void testSetNullProperties() throws Exception {
 
         addNoPropertiesTest(null);
     }
 
     @Test
     public void testGetProperty() throws Exception {
 
         Message<Object> message = new Message<Object>();
         messageExecutor.setProperties(message, properties);
 
         assertEquals("property one is retrieved correctly.", propertyOne,
                 messageExecutor.getProperty(message, NAME_ONE));
         assertEquals("property two is retrieved correctly.", propertyTwo,
                 messageExecutor.getProperty(message, NAME_TWO));
         assertEquals("property three is retrieved correctly.", propertyThree,
                 messageExecutor.getProperty(message, NAME_THREE));
     }
 
     @Test
     public void testGetPropertyThatDoesNotExist() throws Exception {
 
         Message<Object> message = new Message<Object>();
 
         assertNull("retrieving a property when no properties exist should return null.",
                 messageExecutor.getProperty(message, NAME_ONE));
 
         messageExecutor.setProperties(message, properties);
 
         assertNull("retrieving a property that does not exist should return null.",
                 messageExecutor.getProperty(message, "not here"));
     }
 
     @Test
     public void testAddPropertyWithNameAndValue() throws Exception {
 
         new AddPropertyTester<Object>() {
 
             @Override
             public void addProperty(Message<Object> message, String name, Object value) {
 
                 messageExecutor.addProperty(message, name, value);
             }
         };
     }
 
     @Test
     public void testAddPropertyWithNameAndEmptyValue() throws Exception {
 
         new AddNameValuePropertyWithBlankValueTester<Object>("");
     }
 
     @Test
     public void testAddPropertyWithNameAndNullValue() throws Exception {
 
         new AddNameValuePropertyWithBlankValueTester<Object>(null);
     }
 
     @Test
     public void testAddPropertyWithEmptyNameAndValue() throws Exception {
 
        new AddNameValuePropertyWithBlankNameAndValueTester<Object>("", VALUE_ONE);
     }
 
     @Test
     public void testAddPropertyWithNullNameAndValue() throws Exception {
 
         new AddNameValuePropertyWithBlankNameAndValueTester<Object>(null, VALUE_ONE);
     }
 
     @Test
     public void testAddPropertyWithEmptyNameAndEmptyValue() throws Exception {
 
         new AddNameValuePropertyWithBlankNameAndValueTester<Object>("", "");
     }
 
     @Test
     public void testAddPropertyWithNullNameAndNullValue() throws Exception {
 
         new AddNameValuePropertyWithBlankNameAndValueTester<Object>(null, null);
     }
 
     @Test
     public void testAddProperty() throws Exception {
 
         new AddPropertyTester<Object>() {
 
             @Override
             public void addProperty(Message<Object> message, String name, Object value) {
 
                 messageExecutor.addProperty(message, messageExecutor.newProperty(name, value));
             }
         };
     }
 
     @Test
     public void testAddPropertyWithEmptyValue() throws Exception {
 
         new AddObjectPropertyWithBlankValueTester<Object>("");
     }
 
     @Test
     public void testAddPropertyWithNullValue() throws Exception {
 
         new AddObjectPropertyWithBlankValueTester<Object>(null);
     }
 
     @Test
     public void testAddPropertyWithEmptyName() throws Exception {
 
         new AddObjectPropertyWithBlankNameAndValueTester<Object>("", VALUE_ONE);
     }
 
     @Test
     public void testAddPropertyWithNullName() throws Exception {
 
         new AddObjectPropertyWithBlankNameAndValueTester<Object>(null, VALUE_ONE);
     }
 
     @Test
     public void testAddPropertyWithEmptyValues() throws Exception {
 
         new AddObjectPropertyWithBlankNameAndValueTester<Object>("", "");
     }
 
     @Test
     public void testAddPropertyWithNullValues() throws Exception {
 
         new AddObjectPropertyWithBlankNameAndValueTester<Object>(null, null);
     }
 
     @Test
     public void testAddPropertyWithNullProperty() throws Exception {
 
         new AddPropertyWithBlankNameAndValueTester<Object>(null, null) {
 
             @Override
             public void addProperty(Message<Object> message, String name, Object value) {
 
                 messageExecutor.addProperty(message, null);
             }
         };
     }
 
 
     protected interface PropertyAdder<T> {
 
         public abstract void addProperty(Message<T> message, String name, Object value);
     }
 
     private abstract class AddPropertyTester<T> implements PropertyAdder<T> {
 
         protected AddPropertyTester() {
 
             Message<T> message = new Message<T>();
 
             assertEquals("no properties should exist", 0, messageExecutor.getProperties(message).size());
 
             addProperty(message, NAME_ONE, VALUE_ONE);
 
             assertEquals("one property should exist", 1, messageExecutor.getProperties(message).size());
             assertEquals("property one should have been added", propertyOne,
                     messageExecutor.getProperty(message, NAME_ONE));
 
             addProperty(message, NAME_TWO, VALUE_TWO);
 
             assertEquals("two properties should exist", 2, messageExecutor.getProperties(message).size());
             assertEquals("property two should have been added", propertyTwo,
                     messageExecutor.getProperty(message, NAME_TWO));
 
             addProperty(message, NAME_THREE, VALUE_THREE);
 
             assertEquals("three properties should exist", 3, messageExecutor.getProperties(message).size());
             assertEquals("property three should have been added", propertyThree,
                     messageExecutor.getProperty(message, NAME_THREE));
         }
     }
 
     private void addNoPropertiesTest(Collection<P> empty) {
 
         Message<Object> message = new Message<Object>();
         messageExecutor.setProperties(message, empty);
 
         Collection<P> properties = messageExecutor.getProperties(message);
 
         assertNotNull("a collection of properties should be returned.", properties);
         assertEquals("the number of properties should be zero.", 0, properties.size());
     }
 
     private abstract class AddPropertyWithBlankValueTester<T> implements PropertyAdder<T> {
 
         protected AddPropertyWithBlankValueTester(Object blank) {
 
             Message<T> message = new Message<T>();
 
             P property = messageExecutor.newProperty(NAME_ONE, blank);
 
             assertEquals("no properties should exist", 0, messageExecutor.getProperties(message).size());
 
             addProperty(message, NAME_ONE, blank);
 
             assertEquals("one property should exist", 1, messageExecutor.getProperties(message).size());
             assertEquals("property one should have an empty value", property,
                     messageExecutor.getProperty(message, NAME_ONE));
 
             addProperty(message, NAME_ONE, blank);
 
             assertEquals("one property should exist", 1, messageExecutor.getProperties(message).size());
             assertEquals("property one should have an empty value", property,
                     messageExecutor.getProperty(message, NAME_ONE));
         }
     }
 
     private class AddNameValuePropertyWithBlankValueTester<T> extends AddPropertyWithBlankValueTester<T> {
 
         protected AddNameValuePropertyWithBlankValueTester(Object blank) {
             super(blank);
         }
 
         @Override
         public void addProperty(Message<T> message, String name, Object value) {
 
             messageExecutor.addProperty(message, name, value);
         }
     }
 
     private class AddObjectPropertyWithBlankValueTester<T> extends AddPropertyWithBlankValueTester<T> {
 
         protected AddObjectPropertyWithBlankValueTester(Object blank) {
             super(blank);
         }
 
         @Override
         public void addProperty(Message<T> message, String name, Object value) {
 
             messageExecutor.addProperty(message, messageExecutor.newProperty(name, value));
         }
     }
 
     private abstract class AddPropertyWithBlankNameAndValueTester<T> implements PropertyAdder<T> {
 
         protected AddPropertyWithBlankNameAndValueTester(String name, Object value) {
 
             Message<T> message = new Message<T>();
 
             assertEquals("no properties should exist", 0, messageExecutor.getProperties(message).size());
 
             addProperty(message, name, value);
 
             assertEquals("no properties should have been added.", 0, messageExecutor.getProperties(message).size());
         }
     }
 
     private class AddNameValuePropertyWithBlankNameAndValueTester<T> extends AddPropertyWithBlankNameAndValueTester<T> {
 
         protected AddNameValuePropertyWithBlankNameAndValueTester(String name, Object value) {
             super(name, value);
         }
 
         @Override
         public void addProperty(Message<T> message, String name, Object value) {
 
             messageExecutor.addProperty(message, name, value);
         }
     }
 
     private class AddObjectPropertyWithBlankNameAndValueTester<T> extends AddPropertyWithBlankNameAndValueTester<T> {
 
         protected AddObjectPropertyWithBlankNameAndValueTester(String name, Object value) {
             super(name, value);
         }
 
         @Override
         public void addProperty(Message<T> message, String name, Object value) {
 
             messageExecutor.addProperty(message, messageExecutor.newProperty(name, value));
         }
     }
 }
