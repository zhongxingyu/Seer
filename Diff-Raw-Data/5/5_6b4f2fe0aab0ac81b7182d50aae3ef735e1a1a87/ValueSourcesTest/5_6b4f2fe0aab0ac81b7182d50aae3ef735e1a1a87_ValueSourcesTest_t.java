 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: ValueSourcesTest.java,v 1.8 2004-04-28 16:51:20 shahid.shah Exp $
  */
 
 package com.netspective.commons.value;
 
 import java.util.Set;
 import java.util.List;
 import junit.framework.TestCase;
 
 import com.netspective.commons.value.ValueSources;
 import com.netspective.commons.value.ValueSourceSpecification;
 import com.netspective.commons.value.exception.ValueSourceInitializeException;
 import com.netspective.commons.value.source.*;
 import com.netspective.commons.metric.Metrics;
 
 public class ValueSourcesTest extends TestCase
 {
     public ValueSourcesTest(String name)
     {
         super(name);
     }
 
 	public void testGenericValues()
 	{
 		Value valueOne = new GenericValue(new Float(3.14159));
 		assertTrue(valueOne.hasValue());
 		assertFalse(valueOne.isListValue());
 		assertEquals(Value.VALUELISTTYPE_NONE, valueOne.getListValueType());
 
 		assertEquals(Object.class, valueOne.getValueHolderClass());
 		assertEquals(Object.class, valueOne.getBindParamValueHolderClass());
 
 		assertEquals(new Float(3.14159), valueOne.getValue());
 		assertEquals(new Float(3.14159), valueOne.getValueForSqlBindParam());
 		assertEquals("3.14159", valueOne.getTextValueOrDefault("default value"));
 		assertTrue(Math.abs(3.14159 - valueOne.getDoubleValue()) < 0.00001);
 
 		String[] valuesOne = valueOne.getTextValues();
 		assertEquals(1, valuesOne.length);
 		assertEquals("3.14159", valuesOne[0]);
 
 		List listValuesOne = valueOne.getListValue();
 		assertEquals(1, listValuesOne.size());
 		assertEquals("3.14159", listValuesOne.get(0));
 
 		valueOne.appendText(" is the value of Pi");
 		assertEquals("3.14159 is the value of Pi", valueOne.getTextValue());
 
 		valueOne.setTextValue("Pi = 3.14159");
 		assertEquals("Pi = 3.14159", valueOne.getTextValue());
 
 		// Testing String[] routines/sections
 		String[] inputValues = new String[] { "Apple", "Pi", "are", "squared" };
 		Value valueTwo = new GenericValue(inputValues);
 		assertTrue(valueTwo.hasValue());
 		assertTrue(valueTwo.isListValue());
 		assertEquals(Value.VALUELISTTYPE_STRINGARRAY, valueTwo.getListValueType());
 
 		List valuesTwoList = valueTwo.getListValue();
 
 		assertEquals(inputValues.length, valuesTwoList.size());
 		for (int i = 0; i < inputValues.length; i ++)
 			assertTrue(valuesTwoList.contains(inputValues[i]));
 
 		assertEquals(inputValues[0], valueTwo.getTextValue());
 
 		String[] valuesTwoArray = valueTwo.getTextValues();
 		assertEquals(inputValues.length, valuesTwoArray.length);
 		for (int i = 0; i < inputValues.length; i ++)
 			assertEquals(inputValues[i], valuesTwoArray[i]);
 
 		// Testing List routines/sections using AbstractValue instead of the Value interface
 		List valueThreeInput = valuesTwoList;
 		AbstractValue valueThree = new GenericValue(valueThreeInput);
 
 		assertTrue(valueThree.hasValue());
 		assertTrue(valueThree.isListValue());
 		assertEquals(Value.VALUELISTTYPE_LIST, valueThree.getListValueType());
 
 		List valueThreeList = valueTwo.getListValue();
 
 		assertEquals(inputValues.length, valueThreeList.size());
 		for (int i = 0; i < inputValues.length; i ++)
 			assertTrue(valueThreeList.contains(inputValues[i]));
 
 		assertEquals(inputValues[0], valueThree.getTextValue());
 
 		String[] valueThreeArray = valueThree.getTextValues();
 		assertEquals(inputValues.length, valueThreeArray.length);
 		for (int i = 0; i < inputValues.length; i ++)
 			assertEquals(inputValues[i], valueThreeArray[i]);
 
 	}
 
 	public void testCachedValues()
 	{
 		CachedValue cValue = new CachedValue(new GenericValue(new Float (3.14159)), 5);
 		assertTrue(cValue.isValid());
 		assertTrue(cValue.getCreationTime() <= System.currentTimeMillis());
 		assertTrue(cValue.isValid());
 		assertEquals(5, cValue.getTimeoutValue());
 		assertTrue(cValue.isValid());
 		assertEquals("3.14159", cValue.getValue().getTextValue());
 		assertTrue(cValue.isValid());
 
         // Sleep for 6 seconds
 //		assertFalse(cValue.isValid());
 	}
 
 	public void testReadOnlyValues ()
 	{
 		// Testing Readonly Values
 /*
 		ReadOnlyValue readOnlyValue = new GenericValue(new String("Testing Read Only"));
 
 		assertTrue(readOnlyValue.hasValue());
 		assertFalse(readOnlyValue.isListValue());
 		assertEquals("Testing Read Only", readOnlyValue.getTextValue());
 
 		boolean exceptionThrown = true;
 
 		try {
 			readOnlyValue.setTextValue("Another Test");
 			exceptionThrown = false;
 		} catch (ValueException e) {
 			assertTrue(exceptionThrown);
 		}
 
 		assertTrue(exceptionThrown);
 		assertEquals("Testing Read Only", readOnlyValue.getTextValue());
 
 		try {
 			readOnlyValue.setValue(new Float(3.14159));
 			exceptionThrown = false;
 		} catch (ValueException e) {
 			assertTrue(exceptionThrown);
 		}
 
 		assertTrue(exceptionThrown);
 		assertEquals("Testing Read Only", readOnlyValue.getTextValue());
 
 */
 	}
 
 	public void testValueErrors()
 	{
 		Value valueOne = new GenericValue(new Float(3.14159));
 		assertTrue(valueOne.hasValue());
 		assertFalse(valueOne.isListValue());
 
 		boolean exceptionThrown = true;
 
 		try {
 			assertEquals(3, valueOne.getIntValue());
 			exceptionThrown = false;
 		} catch (Exception e) {
 			assertTrue(exceptionThrown);
 		}
 
 		assertTrue(exceptionThrown);
 	}
 
 	public void testValueSources()
 	{
 		ValueSources vs = ValueSources.getInstance();
 
 		Set srcClassesMapKeySet = vs.getValueSourceClassesMap().keySet();
 		String[] expectedClassesMapKeySet = new String[] {
 			"vs-expr", "simple-expr",
 			"filesystem-entries",
 			"guid", "generate-id",
 			"static", "text", "string",
 			"text-list", "strings",
 			"system-property", "java-expr", "java",
            "redirect", "script"
 		};
 
         assertEquals(expectedClassesMapKeySet.length, srcClassesMapKeySet.size());
 
 		for (int i = 0; i < expectedClassesMapKeySet.length; i ++)
 			assertTrue(srcClassesMapKeySet.contains(expectedClassesMapKeySet[i]));
 
 		Set srcClassesSet = vs.getValueSourceClassesSet();
 		Class[] expectedClassesSet = new Class[] {
 			ValueSrcExpressionValueSource.class,
 			FilesystemEntriesValueSource.class,
 			GloballyUniqueIdValueSource.class,
 			StaticValueSource.class,
 			StaticListValueSource.class,
 			SystemPropertyValueSource.class
 		};
 
 		for (int i = 0; i < expectedClassesSet.length; i ++)
 			assertTrue(srcClassesMapKeySet.contains(expectedClassesMapKeySet[i]));
 	}
 
     public void testValueSourceTokens()
     {
         ValueSourceSpecification vss = ValueSources.createSpecification("test-id:abc");
         assertTrue(vss.isValid());
         assertTrue(vss.isEscaped() == false);
 	    assertEquals("test-id:abc", vss.toString());
         assertEquals("test-id", vss.getIdOrClassName());
         assertEquals("abc", vss.getParams());
 	    assertFalse(vss.isCustomClass());
 	    assertEquals(ValueSourceSpecification.class, vss.getClass());
 	    assertEquals(7, vss.getIdDelimPos());
 
         // since the ':' is escaped, this is not a valid value source specification
         vss = ValueSources.createSpecification("test-id\\:abc");
         assertFalse(vss.isValid());
         assertTrue(vss.isEscaped());
 	    assertEquals(8, vss.getIdDelimPos());
 	    assertEquals("test-id\\:abc", vss.getSpecificationText());
 
         // very basic expression that should not be seen as a value source
         vss = ValueSources.createSpecification("this is a simple expression");
         assertFalse(vss.isValid());
         assertFalse(vss.isEscaped());
 	    assertEquals(-1, vss.getIdDelimPos());
 	    assertEquals("this is a simple expression", vss.getSpecificationText());
 
         // xyz should be treated as a processing instruction since it's in []
         vss = ValueSources.createSpecification("test-id:[xyz]abc");
         assertTrue(vss.isValid());
         assertFalse(vss.isEscaped());
         assertEquals("xyz", vss.getProcessingInstructions());
 	    assertEquals(7, vss.getIdDelimPos());
 	    assertEquals("abc", vss.getParams());
 
         // xyz should NOT be treated as a processing instruction since the first [ is escaped
         vss = ValueSources.createSpecification("test-id:\\[xyz]abc");
         assertTrue(vss.isValid());
         assertFalse(vss.isEscaped());
 	    assertNull(vss.getProcessingInstructions());
         assertEquals("[xyz]abc", vss.getParams());
 
         // this is an invalid specification since the [ is not closed
         vss = ValueSources.createSpecification("test-id:[xyzabc");
         assertFalse(vss.isValid());
         assertFalse(vss.isEscaped());
     }
 
     public void testGetSingleValueSource() throws ValueSourceInitializeException
     {
         ValueSource svs = ValueSources.getInstance().getValueSource("simple-expr:this is ${static:my world}", ValueSources.VSNOTFOUNDHANDLER_THROW_EXCEPTION);
         assertNotNull(svs);
         assertEquals(ValueSrcExpressionValueSource.class, svs.getClass());
 	    assertEquals("this is my world", svs.getTextValue(null));
 
 	    ValueContext vc = ValueSources.getInstance().createDefaultValueContext();
 	    assertEquals("this is my world", svs.getTextValue(vc));
 
         svs = ValueSources.getInstance().getValueSource("simple-expr\\:this is ${my.expr}", ValueSources.VSNOTFOUNDHANDLER_THROW_EXCEPTION);
         assertNull(svs);
 
 	    ValueSource staticVS = ValueSources.getInstance().getValueSource("static:This is static text", ValueSources.VSNOTFOUNDHANDLER_THROW_EXCEPTION);
 	    assertNotNull(staticVS);
 	    assertEquals(StaticValueSource.class, staticVS.getClass());
 		assertTrue(staticVS.hasValue(null));
 	    assertEquals("This is static text", staticVS.getTextValue(null));
 
 		Value staticValue = new GenericValue("This is static text");
 	    assertEquals(staticValue.getTextValue(), staticVS.getPresentationValue(null).getTextValue());
     }
 
     public void testGetSingleOrStaticValueSource()
     {
         /* test a simple expression that should return the same string */
         String simpleExpr = "This is a string";
         ValueSource svs = ValueSources.getInstance().getValueSourceOrStatic(simpleExpr);
         assertNotNull(svs);
         assertEquals(StaticValueSource.class, svs.getClass());
         assertEquals(simpleExpr, svs.getValue(null).getTextValue());
 
         /* test an expression that has an escaped colon and should return string without the backslash */
         String escapedExpr = "This is a string with an escaped colon (\\:)";
         String escapeRemovedExpr = "This is a string with an escaped colon (:)";
         svs = ValueSources.getInstance().getValueSourceOrStatic(escapedExpr);
         assertNotNull(svs);
         assertEquals(StaticValueSource.class, svs.getClass());
         assertEquals(escapeRemovedExpr, svs.getValue(null).getTextValue());
 
         /* test a simple expression that should return the same string */
         String configExpr = "vs-expr:this is ${my.expr}";
         svs = ValueSources.getInstance().getValueSourceOrStatic(configExpr);
         assertNotNull(svs);
         assertEquals(ValueSrcExpressionValueSource.class, svs.getClass());
     }
 
     public void testDumpMetrics()
     {
         Metrics metrics = new Metrics(null, "Test");
         ValueSources.getInstance().produceMetrics(metrics);
         //System.out.println(metrics);
     }
 }
