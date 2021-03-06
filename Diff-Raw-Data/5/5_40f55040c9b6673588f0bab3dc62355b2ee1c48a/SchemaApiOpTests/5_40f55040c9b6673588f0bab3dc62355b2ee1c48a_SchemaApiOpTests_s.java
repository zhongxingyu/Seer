 /*
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  * 
  * U.S. Government Rights - Commercial software. Government users 
  * are subject to the Sun Microsystems, Inc. standard license agreement
  * and applicable provisions of the FAR and its supplements.
  * 
  * Use is subject to license terms.
  * 
  * This distribution may include materials developed by third parties.
  * Sun, Sun Microsystems, the Sun logo, Java and Project Identity 
  * Connectors are trademarks or registered trademarks of Sun 
  * Microsystems, Inc. or its subsidiaries in the U.S. and other
  * countries.
  * 
  * UNIX is a registered trademark in the U.S. and other countries,
  * exclusively licensed through X/Open Company, Ltd. 
  * 
  * -----------
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved. 
  * 
  * The contents of this file are subject to the terms of the Common Development
  * and Distribution License(CDDL) (the License).  You may not use this file
  * except in  compliance with the License. 
  * 
  * You can obtain a copy of the License at
  * http://identityconnectors.dev.java.net/CDDLv1.0.html
  * See the License for the specific language governing permissions and 
  * limitations under the License.  
  * 
  * When distributing the Covered Code, include this CDDL Header Notice in each
  * file and include the License file at identityconnectors/legal/license.txt.
  * If applicable, add the following below this CDDL Header, with the fields 
  * enclosed by brackets [] replaced by your own identifying information: 
  * "Portions Copyrighted [year] [name of copyright owner]"
  * -----------
  */
 package org.identityconnectors.contract.test;
 
 import static org.junit.Assert.*;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.identityconnectors.common.CollectionUtil;
 import org.identityconnectors.common.logging.Log;
 import org.identityconnectors.contract.exceptions.ObjectNotFoundException;
 import org.identityconnectors.framework.api.operations.APIOperation;
 import org.identityconnectors.framework.api.operations.SchemaApiOp;
 import org.identityconnectors.framework.api.operations.ScriptOnConnectorApiOp;
 import org.identityconnectors.framework.api.operations.ScriptOnResourceApiOp;
 import org.identityconnectors.framework.api.operations.TestApiOp;
 import org.identityconnectors.framework.api.operations.ValidateApiOp;
 import org.identityconnectors.framework.common.objects.AttributeInfo;
 import org.identityconnectors.framework.common.objects.AttributeInfoUtil;
 import org.identityconnectors.framework.common.objects.Name;
 import org.identityconnectors.framework.common.objects.ObjectClassInfo;
 import org.identityconnectors.framework.common.objects.Schema;
 import org.identityconnectors.framework.common.objects.Uid;
 import org.identityconnectors.framework.spi.operations.ScriptOnConnectorOp;
 import org.junit.Test;
 
 /**
  * Contract test of {@link SchemaApiOp} operation.
  * 
  * @author Zdenek Louzensky
  *
  */
 public class SchemaApiOpTests extends AbstractSimpleTest {
 
     /**
      * Logging..
      */
     private static final Log LOG = Log.getLog(SchemaApiOpTests.class);
     private static final String TEST_NAME = "Schema";
     
     /*
      * Properties prefixes:
      * it's added .testsuite.${type.name} after the prefix
      */
     private static final String SUPPORTED_OBJECT_CLASSES_PROPERTY_PREFIX = "oclasses";
     private static final String SUPPORTED_OPERATIONS_PROPERTY_PREFIX = "operations";
     private static final String STRICT_CHECK_PROPERTY_PREFIX = "strictCheck";
 
     /*
      * AttributeInfo field names used in property configuration:
      */
     private static final String ATTRIBUTE_FIELD_RETURNED_BY_DEFAULT = "returnedByDefault";
     private static final String ATTRIBUTE_FIELD_MULTI_VALUE = "multiValue";
     private static final String ATTRIBUTE_FIELD_REQUIRED = "required";
     private static final String ATTRIBUTE_FIELD_CREATEABLE = "createable";
     private static final String ATTRIBUTE_FIELD_UPDATEABLE = "updateable";
     private static final String ATTRIBUTE_FILED_READABLE = "readable";
     private static final String ATTRIBUTE_FIELD_TYPE = "type";
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Class<? extends APIOperation> getAPIOperation() {
         return SchemaApiOp.class;
     }
     
     /**
      * Tests that the schema doesn't contain {@link Uid}
      */
     @Test
     public void testUidNotPresent() {
         final Schema schema = getConnectorFacade().schema();
         Set<ObjectClassInfo> ocInfos = schema.getObjectClassInfo();
         for (ObjectClassInfo ocInfo : ocInfos) {
             Set<AttributeInfo> attInfos = ocInfo.getAttributeInfo();
             for (AttributeInfo attInfo : attInfos) {
                 //ensure there is not Uid present
                 assertTrue("Uid can't be present in connector Schema!", !attInfo.is(Uid.NAME));
             }
         }
     }
     
     /**
      * Tests that every object class contains {@link Name} among its attributes.
      */
     @Test
     public void testNamePresent() {
         final Schema schema = getConnectorFacade().schema();
         Set<ObjectClassInfo> ocInfos = schema.getObjectClassInfo();
         for (ObjectClassInfo ocInfo : ocInfos) {
             Set<AttributeInfo> attInfos = ocInfo.getAttributeInfo();
             // ensure there is NAME present
             boolean found = false;            
             for (AttributeInfo attInfo : attInfos) {               
                 if (attInfo.is(Name.NAME)) found = true;
             }
             final String MSG = "Name is not present among attributes of object class '%s'.";
             assertTrue(String.format(MSG, ocInfo.getType()), found);
         }
     }
         
     /**
      * List of all operations which must be supported by all object classes when
      * supported at all.
      */
     private static final List<Class<? extends APIOperation>> opSupportedByAllOClasses = new LinkedList<Class<? extends APIOperation>>();
     static {
         opSupportedByAllOClasses.add(ScriptOnConnectorApiOp.class);
         opSupportedByAllOClasses.add(ScriptOnResourceApiOp.class);
         opSupportedByAllOClasses.add(TestApiOp.class);
         opSupportedByAllOClasses.add(ValidateApiOp.class);
     }
 
     /**
      * Test ensures that following operations are supported by all object
      * classes when supported at all: ScriptOnConnectorApiOp, ScriptOnResourceApiOp,
      * TestApiOp, ValidateApiOp.
      */
     @Test
     public void testOpSupportedByAllOClasses() {
         final Schema schema = getConnectorFacade().schema();
         Set<ObjectClassInfo> ocInfos = schema.getObjectClassInfo();
         for (Class<? extends APIOperation> apiOp : opSupportedByAllOClasses) {
             Set<ObjectClassInfo> suppOClasses = schema.getSupportedObjectClassesByOperation(apiOp);
             if (!suppOClasses.isEmpty()) {
                 // operation is supported for at least one object class
                 // then it must be supported for all object classes
                 final String MSG = "Operation %s must be in the schema supported by all object classes which supports connector.";
                 assertTrue(String.format(MSG, apiOp), CollectionUtil.equals(suppOClasses, ocInfos));
             }
         }
     }
 
     /**
      * Tests that returned schema by connector is the same as expected schema to
      * be returned.
      */
     @Test
     public void testSchemaExpected() {
         final Schema schema = getConnectorFacade().schema();
         String msg = null;
         
         Boolean strictCheck = getStrictCheckProperty();
 
         // list of expected object classes
         List<String> expOClasses = (List<String>) getTestPropertyOrFail(List.class.getName(),
                 SUPPORTED_OBJECT_CLASSES_PROPERTY_PREFIX, true);
 
         // iterate over object classes and check that were expected and check
         // their attributes
         for (ObjectClassInfo ocInfo : schema.getObjectClassInfo()) {
             msg = "Schema returned object class %s that is not expected to be suported.";
             assertTrue(String.format(msg, ocInfo.getType()), expOClasses.contains(ocInfo.getType()));
 
             // list of expected attributes for the object class
             List<String> expAttrs = (List<String>) getTestPropertyOrFail(List.class.getName(),
                     "attributes." + ocInfo.getType() + "."
                             + SUPPORTED_OBJECT_CLASSES_PROPERTY_PREFIX, strictCheck);
             
             // check object class attributes
             for (AttributeInfo attr : ocInfo.getAttributeInfo()) {
                 if (strictCheck) {
                     msg = "Object class %s contains unexpected attribute: %s.";
                     assertTrue(String.format(msg, ocInfo.getType(), attr.getName()), expAttrs
                             .contains(attr.getName()));
                 }
 
                 // expected attribute values
                Map<String, String> expAttrValues = (Map<String, String>) getTestPropertyOrFail(
                         Map.class.getName(), attr.getName() + ".attribute." + ocInfo.getType()
                                 + "." + SUPPORTED_OBJECT_CLASSES_PROPERTY_PREFIX, strictCheck);
 
                 // check attribute's values in case the test is strict or property is provided
                 if (strictCheck || expAttrValues != null) {
                     // check all attribute's fields
                     checkAttributeValues(ocInfo, attr, expAttrValues);
                 }
             }
             
             
             // check that all expected attributes are in schema
             for (String expAttr : expAttrs) {
                 msg = "Schema doesn't contain expected attribute '%s' in object class '%s'.";
                 assertNotNull(String.format(msg, expAttr, ocInfo.getType()), AttributeInfoUtil
                         .find(expAttr, ocInfo.getAttributeInfo()));
             }
             
         }
         
         msg = "Schema returned less supported object classes, expected: %d, returned %d.";
         assertTrue(String.format(msg, expOClasses.size(), schema.getObjectClassInfo().size()),
                 expOClasses.size() == schema.getObjectClassInfo().size());
 
         // expected object classes supported by operations
         Map<String, List<String>> expOperations = (Map<String, List<String>>) getTestPropertyOrFail(
                 Map.class.getName(), SUPPORTED_OPERATIONS_PROPERTY_PREFIX, true);
         Map<Class<? extends APIOperation>, Set<ObjectClassInfo>> supportedOperations = schema
                 .getSupportedObjectClassesByOperation();
 
         // iterate over operations
         for (Class<? extends APIOperation> operation : supportedOperations.keySet()) {
             msg = "Schema returned unexpected operation: %s.";
             assertTrue(String.format(msg, operation.getSimpleName()),
                     expOperations.containsKey(operation.getSimpleName()));
 
             // expected object classes supported by the operation
             List<String> expOClassesByOp = expOperations.get(operation.getSimpleName());
             assertNotNull(expOClassesByOp);
             
             // check that all operations are expected
             for (ObjectClassInfo ocInfo : supportedOperations.get(operation)) {
                 msg = "Operation %s supports unexpected object class: %s."; 
                 assertTrue(String.format(msg, operation.getSimpleName(), ocInfo.getType()),
                         expOClassesByOp.contains(ocInfo.getType()));
             }
             
             msg =  "Schema returned less object classes supported by operation %s, expected %d, returned %d.";
             assertTrue(String.format(msg, operation.getSimpleName(), expOClassesByOp.size(), supportedOperations.get(operation).size()),                    
                     expOClassesByOp.size() == supportedOperations.get(operation).size());
         }
         
         // check if there shouldn't be more opearations
         msg = "Schema returned less operations, expected: %s, returned %s.";
         assertTrue(String.format(msg, expOperations.keySet(), supportedOperations.keySet()),
                 expOperations.size() == supportedOperations.size());
 
     }
 
     /**
      * Checks that attribute values are the same as expectedValues.
      */
     private void checkAttributeValues(ObjectClassInfo ocInfo, AttributeInfo attribute,
            Map<String, String> expectedValues) {        
         // check that all attributes are provided
         String msg = "Missing property definition for field '%s' of attribute '" + attribute.getName()
                         + "' in object class " + ocInfo.getType();
         assertNotNull(String.format(msg, ATTRIBUTE_FIELD_TYPE), 
                 expectedValues.get(ATTRIBUTE_FIELD_TYPE));
         assertNotNull(String.format(msg, ATTRIBUTE_FILED_READABLE), 
                 expectedValues.get(ATTRIBUTE_FILED_READABLE));
         assertNotNull(String.format(msg, ATTRIBUTE_FIELD_CREATEABLE), 
                 expectedValues.get(ATTRIBUTE_FIELD_CREATEABLE));
         assertNotNull(String.format(msg, ATTRIBUTE_FIELD_UPDATEABLE), 
                 expectedValues.get(ATTRIBUTE_FIELD_UPDATEABLE));
         assertNotNull(String.format(msg, ATTRIBUTE_FIELD_REQUIRED), 
                 expectedValues.get(ATTRIBUTE_FIELD_REQUIRED));
         assertNotNull(String.format(msg, ATTRIBUTE_FIELD_MULTI_VALUE), 
                 expectedValues.get(ATTRIBUTE_FIELD_MULTI_VALUE));
         assertNotNull(String.format(msg, ATTRIBUTE_FIELD_RETURNED_BY_DEFAULT), 
                 expectedValues.get(ATTRIBUTE_FIELD_RETURNED_BY_DEFAULT));
 
         msg = "Object class '" + ocInfo.getType() + "', attribute '" + attribute.getName()
                 + "': field '%s' expected value is '%s', but returned '%s'.";        
         assertEquals(String.format(msg, ATTRIBUTE_FIELD_TYPE, expectedValues
                 .get(ATTRIBUTE_FIELD_TYPE), attribute.getType().getName()), 
                 expectedValues.get(ATTRIBUTE_FIELD_TYPE), attribute.getType());
         assertEquals(String.format(msg, ATTRIBUTE_FILED_READABLE, expectedValues
                 .get(ATTRIBUTE_FILED_READABLE), attribute.isReadable()), 
                 expectedValues.get(ATTRIBUTE_FILED_READABLE), attribute.isReadable());
         assertEquals(String.format(msg, ATTRIBUTE_FIELD_CREATEABLE, expectedValues
                 .get(ATTRIBUTE_FIELD_CREATEABLE), attribute.isCreateable()), 
                 expectedValues.get(ATTRIBUTE_FIELD_CREATEABLE), attribute.isCreateable());
         assertEquals(String.format(msg, ATTRIBUTE_FIELD_UPDATEABLE, expectedValues
                 .get(ATTRIBUTE_FIELD_UPDATEABLE), attribute.isUpdateable()), 
                 expectedValues.get(ATTRIBUTE_FIELD_UPDATEABLE), attribute.isUpdateable());
         assertEquals(String.format(msg, ATTRIBUTE_FIELD_REQUIRED, expectedValues
                 .get(ATTRIBUTE_FIELD_REQUIRED), attribute.isRequired()), 
                 expectedValues.get(ATTRIBUTE_FIELD_REQUIRED), attribute.isRequired());
         assertEquals(String.format(msg, ATTRIBUTE_FIELD_MULTI_VALUE, expectedValues
                 .get(ATTRIBUTE_FIELD_MULTI_VALUE), attribute.isMultiValue()), 
                 expectedValues.get(ATTRIBUTE_FIELD_MULTI_VALUE), attribute.isMultiValue());
         assertEquals(String.format(msg, ATTRIBUTE_FIELD_RETURNED_BY_DEFAULT, 
                 expectedValues.get(ATTRIBUTE_FIELD_RETURNED_BY_DEFAULT), attribute.isReturnedByDefault()), 
                 expectedValues.get(ATTRIBUTE_FIELD_RETURNED_BY_DEFAULT), attribute.isReturnedByDefault());
     }
     
     /**
      * Returns strictCheck property value.
      * When property is not defined true is assumed.
      */
     private Boolean getStrictCheckProperty() {
         Boolean strict = true;
         try {
             strict = (Boolean)getDataProvider().getTestSuiteAttribute(Boolean.class.getName(), STRICT_CHECK_PROPERTY_PREFIX, TEST_NAME);
         }
         catch (ObjectNotFoundException ex) {
             // ok - property not defined
         }
         
         return strict;
     }
     
     /**
      * Returns property value or fails test if property is not defined.
      */
     private Object getTestPropertyOrFail(String typeName, String propName, boolean failOnError) {
         Object propValue = null;
 
         try {
             propValue = getDataProvider().getTestSuiteAttribute(typeName, propName, TEST_NAME);
         } catch (ObjectNotFoundException ex) {
             if (failOnError) fail("Property definition not found: " + ex.getMessage());
         }
         if (failOnError) assertNotNull(propValue);
 
         return propValue;
     }
 
 }
