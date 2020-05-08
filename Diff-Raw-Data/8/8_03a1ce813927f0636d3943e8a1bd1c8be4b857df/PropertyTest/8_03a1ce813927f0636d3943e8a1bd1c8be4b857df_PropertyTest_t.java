 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.test.testcases;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 import org.osgi.framework.InvalidSyntaxException;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.pax.test.impl.deviceSwitch.PropertyChangeNotificationSwitch;
 import fr.imag.adele.apam.pax.test.implS1.S1Impl;
 import fr.imag.adele.apam.tests.helpers.Constants;
 import fr.imag.adele.apam.tests.helpers.ExtensionAbstract;
 
 @RunWith(JUnit4TestRunner.class)
 public class PropertyTest extends ExtensionAbstract {
 
 	/**
 	 * Ensures that inherited properties cannot be changed and inherited
 	 * definitions can change
 	 */
 	@Test
 	public void PropertyInheritedCannotBeChanged_tc001() {
 
 		Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 				"SamsungSwitch");
 		final Instance samsungInst = samsungImpl.createInstance(null, null);
 
 		Implementation s1Impl = CST.apamResolver.findImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 		// this should be updated correctly
 		samsungInst.setProperty("currentVoltage", "999");
 		// this should stay with the old value
 		samsungInst.setProperty("made", "deutschland");
 
 		// this property should be updated since its not inherited
 		Assert.assertTrue("Non-inherited properties shall be updateable",
 				samsungInst.getProperty("currentVoltage").equals("999"));
 
 		Assert.assertTrue("Inherited property shall not be changed",
 				samsungInst.getProperty("made").equals("china"));
 
 	}
 
 	/**
 	 * Ensures that initial properties are configured in the instance properly
 	 */
 	@Test
 	public void PropertyConfiguredWithInitialParameter_tc002() {
 
 		Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 				"SamsungSwitch");
 
 		Map<String, String> initialProperties = new HashMap<String, String>() {
 			{
 				put("property-01", "configured");
 				put("property-02", "configured");
 				put("property-03", "configured");
 				put("property-04", "configured");
 				put("property-05", "configured");
 			}
 		};
 
 		Instance samsungInst = samsungImpl.createInstance(null,
 				initialProperties);
 
 		Assert.assertNotNull("Instance could not be create through the API",
 				samsungInst);
 
 		// all the initial properties should be inside of the instance
 		for (String key : initialProperties.keySet()) {
 
 			Assert.assertNotNull(
 					"Instance did not receive the initial property",
 					samsungInst.getAllProperties().containsKey(key));
 
 			Assert.assertNotNull(
 					"Instance did not receive the initial property",
 					samsungInst.getAllProperties().get(key));
 
 			Assert.assertTrue(samsungInst.getAllProperties().get(key)
 					.equals(initialProperties.get(key)));
 		}
 	}
 
 	/**
 	 * Ensures that initial properties are configured in the instance properly
 	 */
 	@Test
 	public void PropertyConfiguredWithSetProperty_tc003() {
 
 		Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 				"SamsungSwitch");
 
 		Map<String, String> initialProperties = new HashMap<String, String>() {
 			{
 				put("property-01", "configured-01");
 				put("property-02", "configured-02");
 				put("property-03", "configured-03");
 				put("property-04", "configured-04");
 				put("property-05", "configured-05");
 
 			}
 		};
 
 		Instance samsungInst = samsungImpl.createInstance(null, null);
 
 		samsungInst.setProperty("property-01", "configured-01");
 		samsungInst.setProperty("property-02", "configured-02");
 		samsungInst.setProperty("property-03", "configured-03");
 		samsungInst.setProperty("property-04", "configured-04");
 		samsungInst.setProperty("property-05", "configured-05");
 
 		final String message = "Instance did not receive the property defined by setProperty method call";
 
 		for (String key : initialProperties.keySet()) {
 
 			System.out.println(key + ":"
 					+ samsungInst.getAllProperties().get(key));
 
 			Assert.assertTrue(message, samsungInst.getAllProperties()
 					.containsKey(key));
 			Assert.assertTrue(
 					message,
 					samsungInst.getProperty(key).equals(
 							initialProperties.get(key)));
 		}
 	}
 
 	@Test
 	public void PropertyDefinitionInternalAndNotInternalAreAPIVisible_tc004() {
 
 		Implementation s1Impl = CST.apamResolver.findImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 		s1Inst.setProperty("stateInternal", "default");
 		s1Inst.setProperty("stateNotInternal", "default");
 
 		for (String key : s1Inst.getAllProperties().keySet()) {
 			System.out.println(key + "="
 					+ s1Inst.getAllProperties().get(key.toString()));
 		}
 
 		Assert.assertTrue("Internal property not visible through API", s1Inst
 				.getAllProperties().get("stateInternal") != null);
 		Assert.assertTrue("Non-Internal property not visible through API",
 				s1Inst.getAllProperties().get("stateNotInternal") != null);
 
 		Assert.assertTrue(
 				"Internal property not visible through API with the right value",
 				s1Inst.getAllProperties().get("stateInternal")
 						.equals("default"));
 		Assert.assertTrue(
 				"Non-Internal property not visible through API with the right value",
 				s1Inst.getAllProperties().get("stateNotInternal")
 						.equals("default"));
 
 		s1Inst.setProperty("stateInternal", "changed");
 		s1Inst.setProperty("stateNotInternal", "changed");
 
 		Assert.assertTrue(
 				"Internal property shall not be changeble through API",
 				s1Inst.getAllProperties().get("stateInternal")
 						.equals("default"));
 		Assert.assertTrue(
 				"Non-Internal property shall be changeble through API",
 				s1Inst.getAllProperties().get("stateNotInternal")
 						.equals("changed"));
 
 		s1.setStateInternal("changed2");
 		s1.setStateNotInternal("changed2");
 
 		Assert.assertTrue(
 				"Internal property shall be changeble through the application",
 				s1Inst.getAllProperties().get("stateInternal")
 						.equals("changed2"));
 		Assert.assertTrue(
 				"Non-Internal property shall be changeble through the application",
 				s1Inst.getAllProperties().get("stateNotInternal")
 						.equals("changed2"));
 
 	}
 
 	@Test
 	public void PropertyDefinitionIsVisibleWithValPropertySetXML_tc005() {
 
 		Implementation s1Impl = CST.apamResolver.findImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 		Assert.assertTrue("Internal property not visible through API", s1Inst
 				.getAllProperties().get("stateInternal") != null);
 		Assert.assertTrue("Non-Internal property not visible through API",
 				s1Inst.getAllProperties().get("stateNotInternal") != null);
 
 		Assert.assertTrue("Internal property value not visible through API",
 				s1Inst.getAllProperties().get("stateInternal")
 						.equals("default"));
 		Assert.assertTrue(
 				"Non-Internal property value not visible through API",
 				s1Inst.getAllProperties().get("stateNotInternal")
 						.equals("default"));
 
 	}
 
 	@Test
 	public void PropertiesDataTypeAndLDAPFilteringForIntegers_tc006()
 			throws InvalidSyntaxException {
 
 		Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 				"SamsungSwitch");
 		final Instance samsungInst = samsungImpl.createInstance(null,
 				new HashMap<String, String>() {
 					{
 						put("currentVoltage", "95");
 					}
 				});
 
 		apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 		auxListProperties("\t", samsungInst);
 
 		// int
 
 		String templateMessage = "Calling match method with filter = %s, should result in True since currentVoltage is %n";
 		String message = "";
 
 		message = String.format(templateMessage, "(currentVoltage>=95)",
 				samsungInst.getProperty("currentVoltage"));
 
 		Assert.assertTrue(message, samsungInst.match("(currentVoltage>=95)"));
 
 		message = String.format(templateMessage, "(currentVoltage<=95)",
 				samsungInst.getProperty("currentVoltage"));
 
 		Assert.assertTrue(message, samsungInst.match("(currentVoltage<=95)"));
 
 		message = String.format(templateMessage, "(currentVoltage<=101)",
 				samsungInst.getProperty("currentVoltage"));
 
 		Assert.assertTrue(message, samsungInst.match("(currentVoltage<=101)"));
 
 		message = String.format(templateMessage, "(currentVoltage<=96)",
 				samsungInst.getProperty("currentVoltage"));
 
 		Assert.assertTrue(message, samsungInst.match("(currentVoltage<=96)"));
 
 		message = String.format(templateMessage, "(currentVoltage>=94)",
 				samsungInst.getProperty("currentVoltage"));
 
 		Assert.assertTrue(message, samsungInst.match("(currentVoltage>=94)"));
 
 	}
 
 	@Test
 	public void PropertiesDataTypeAndLDAPFilteringForBoolean_tc007()
 			throws InvalidSyntaxException {
 
 		Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 				"SamsungSwitch");
 		final Instance samsungInst = samsungImpl.createInstance(null,
 				new HashMap<String, String>() {
 					{
 						put("currentVoltage", "95");
 					}
 				});
 
 		apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 		auxListProperties("\t", samsungInst);
 
 		String message = "";
 
 		message = String
 				.format("Calling match method with filter = %s, should result in True since hasDisplay is %b",
 						"(hasDisplay=false)",
 						samsungInst.getProperty("hasDisplay"));
 
 		Assert.assertTrue(message, samsungInst.match("(hasDisplay=false)"));
 
 		message = String
 				.format("Calling match method with filter = %s, should result in False since hasDisplay is %b",
 						"(hasDisplay=true)",
 						samsungInst.getProperty("hasDisplay"));
 
 		Assert.assertFalse(message, samsungInst.match("(hasDisplay=true)"));
 
 	}
 
 	@Test
 	public void PropertiesDataTypeAndLDAPFilteringForString_tc008()
 			throws InvalidSyntaxException {
 
 		Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 				"SamsungSwitch");
 		final Instance samsungInst = samsungImpl.createInstance(null,
 				new HashMap<String, String>() {
 					{
 						put("currentVoltage", "95");
 					}
 				});
 
 		apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 		auxListProperties("\t", samsungInst);
 
 		String templateMessage = "Calling match method with filter = %s, should result in True since impl-name is %s";
 		String message = "";
 
 		message = String.format(templateMessage, "(impl-name=Samsung*)",
 				samsungInst.getProperty("impl-name"));
 
 		Assert.assertTrue(message, samsungInst.match("(impl-name=Samsung*)"));
 
 		message = String.format(templateMessage, "(impl-name=*amsungSwitch)",
 				samsungInst.getProperty("impl-name"));
 
 		Assert.assertTrue(message,
 				samsungInst.match("(impl-name=*amsungSwitch)"));
 
 		message = String.format(templateMessage, "(impl-name=*amsung*)",
 				samsungInst.getProperty("impl-name"));
 
 		Assert.assertTrue(message, samsungInst.match("(impl-name=*amsung*)"));
 
 		templateMessage = "Calling match method with filter = %s, should result in False since impl-name is %s";
 
 		message = String.format(templateMessage, "(impl-name=SamsunG*)",
 				samsungInst.getProperty("impl-name"));
 
 		Assert.assertFalse(message, samsungInst.match("(impl-name=SamsunG*)"));
 
 	}
 
 	@Test
 	public void PropertiesDataTypeListInt_tc053() {
 
 		final String propertyName = "setInt";
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecFilterSwitch");
 
		Object RawType = implementation.getPropertyObject(propertyName);
 
 		String message = "Retrieving a set of (%s) from the properties, but the return do not correspond to a type that represents a set of elements(List,Set nor array).";
 
 		Assert.assertTrue(String.format(message, "Int"),
				RawType instanceof Integer[] || RawType instanceof Collection);
 
 		String messageCollection = "Retrieving a set of (%s) from the properties, the return correspond to a Collection type but not a Collection containing the type %s.";
 
 		if (RawType instanceof Collection) {
 			Object sample = ((Collection) RawType).iterator().next();
 			Class properType = Integer.class;
 			Assert.assertTrue(
 					String.format(messageCollection, propertyName, properType),
 					properType.isInstance(sample));
 		}
 
 	}
 
 	@Test
 	public void PropertiesDataTypeListInteger_tc054() {
 
 		final String propertyName = "setInteger";
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecFilterSwitch");
 
 		Object RawType = implementation.getPropertyObject(propertyName);
 
 		String message = "Retrieving a set of (%s) from the properties, but the return do not correspond to a type that represents a set of elements(List,Set nor array).";
 
 		Assert.assertTrue(String.format(message, "Integer"),
				RawType instanceof Integer[] || RawType instanceof Collection);
 
 		String messageCollection = "Retrieving a set of (%s) from the properties, the return correspond to a Collection type but not a Collection containing the type %s.";
 
 		if (RawType instanceof Collection) {
 			Object sample = ((Collection) RawType).iterator().next();
 			Class properType = Integer.class;
 			Assert.assertTrue(
 					String.format(messageCollection, propertyName, properType),
 					properType.isInstance(sample));
 		}
 
 	}
 
 	@Test
 	public void PropertiesDataTypeListString_tc055() {
 
 		final String propertyName = "setString";
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecFilterSwitch");
 
 		Object RawType = implementation.getPropertyObject(propertyName);
 
 		String message = "Retrieving a set of (%s) from the properties, but the return do not correspond to a type that represents a set of elements(List,Set nor array).";
 
 		Assert.assertTrue(String.format(message, "String"),
 				RawType instanceof String[] || RawType instanceof Collection);
 
 		String messageCollection = "Retrieving a set of (%s) from the properties, the return correspond to a Collection type but not a Collection containing the type %s.";
 
 		if (RawType instanceof Collection) {
 			Object sample = ((Collection) RawType).iterator().next();
 			Class properType = String.class;
 			Assert.assertTrue(
 					String.format(messageCollection, propertyName, properType),
 					properType.isInstance(sample));
 		}
 
 	}
 
 	@Test
 	public void PropertyFilterOSGiImplementationSuperSet_Integer_tc057() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecFilterSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String messageTemplate = "%s [expanded expression: %s %s %s] should be %b. By definition the A superset('*>') B operator means that A must contain all B elements, although it may contain more.";
 
 		String expression = "(setInt *> {12,15,254, 0})";
 		String message = String.format(messageTemplate, expression,inst.getProperty("setInt"), "*>", inst.getProperty("setInt"),true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setInt *> {254,15,12,0})";
 		message = String.format(messageTemplate, expression,inst.getProperty("setInt"), "*>",inst.getProperty("setIntUnordered"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setInt *> {12,15, 0})";
 		message = String.format(messageTemplate, expression,inst.getProperty("setInt"), "*>",inst.getProperty("setIntLessElements"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setInt *> {12,15,254, 0,27})";
 		message = String.format(messageTemplate, expression,inst.getProperty("setInt"), "*>",inst.getProperty("setIntMoreElements"), false);
 		Assert.assertTrue(message, !inst.match(expression));
 
 	}
 	
 	@Test
 	public void PropertyFilterOSGiImplementationSuperSet_Enum_tc058() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecFilterSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String messageTemplate = "%s [expanded expression: %s %s %s] should be %b. By definition the A superset('*>') B operator means that A must contain all B elements, although it may contain more.";
 
 		String expression = "(OS *> {Linux, Windows, Android, IOS})";
 		String message = String.format(messageTemplate, expression,inst.getProperty("OS"), "*>", inst.getProperty("OS"),true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(OS *> {IOS, Windows, Linux,Android})";
 		message = String.format(messageTemplate, expression,inst.getProperty("OS"), "*>",inst.getProperty("OSUnordered"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(OS *> {Linux, Windows, IOS})";
 		message = String.format(messageTemplate, expression,inst.getProperty("OS"), "*>",inst.getProperty("OSLessElements"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(OS *> {Linux, Windows, Android,IOS,AmigaOS})";
 		message = String.format(messageTemplate, expression,inst.getProperty("OS"), "*>",inst.getProperty("OSMoreElements"), false);
 		Assert.assertTrue(message, !inst.match(expression));
 
 	}
 	
 	@Test
 	public void PropertyFilterOSGiImplementationSuperSet_String_tc059() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecFilterSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String messageTemplate = "%s [expanded expression: %s %s %s] should be %b. By definition the A superset('*>') B operator means that A must contain all B elements, although it may contain more.";
 
 		String expression = "(setString *> {doubt,grows,with,knowledge})";
 		String message = String.format(messageTemplate, expression,inst.getProperty("setString"), "*>", inst.getProperty("setString"),true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setString *> {with,doubt,knowledge,grows})";
 		message = String.format(messageTemplate, expression,inst.getProperty("setString"), "*>",inst.getProperty("setStringUnordered"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setString *> {doubt,grows,knowledge})";
 		message = String.format(messageTemplate, expression,inst.getProperty("setString"), "*>",inst.getProperty("setStringLessElements"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setString *> {doubt,and,uncertainties,grows,with,knowledge})";
 		message = String.format(messageTemplate, expression,inst.getProperty("setString"), "*>",inst.getProperty("setStringMoreElements"), false);
 		Assert.assertTrue(message, !inst.match(expression));
 
 	}
 				
 	@Test
 	public void PropertyFilterOSGiImplementationSubSet_Integer_tc060() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecFilterSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String messageTemplate = "%s [expanded expression: %s %s %s] should be %b. By definition the A subset('<*') B means that all A elements must be in B.";
 
 		String expression = "(setInt <* {12,15,254, 0})";
 		String message = String.format(messageTemplate, expression,inst.getProperty("setInt"), "<*", inst.getProperty("setInt"),true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setInt <* {254,15,12,0})";
 		message = String.format(messageTemplate, expression,inst.getProperty("setInt"), "<*",inst.getProperty("setIntUnordered"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setInt <* {12,15, 0})";
 		message = String.format(messageTemplate, expression,inst.getProperty("setInt"), "<*",inst.getProperty("setIntLessElements"), false);
 		Assert.assertTrue(message, !inst.match(expression));
 
 		expression = "(setInt <* {12,15,254, 0,27})";
 		message = String.format(messageTemplate, expression,inst.getProperty("setInt"), "<*",inst.getProperty("setIntMoreElements"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 	}
 	
 	@Test
 	public void PropertyFilterOSGiImplementationSubSet_Enum_tc061() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecFilterSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		
 		String messageTemplate = "%s [expanded expression: %s %s %s] should be %b. By definition the A subset('<*') B means that all A elements must be in B.";
 
 		String expression = "(OS <* {Linux, Windows, Android, IOS})";
 		String message = String.format(messageTemplate, expression,inst.getProperty("OS"), "<*", inst.getProperty("OS"),true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(OS <* {IOS, Windows, Linux,Android})";
 		message = String.format(messageTemplate, expression,inst.getProperty("OS"), "<*",inst.getProperty("OSUnordered"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(OS <* {Linux, Windows, IOS})";
 		message = String.format(messageTemplate, expression,inst.getProperty("OS"), "<*",inst.getProperty("OSLessElements"), false);
 		Assert.assertTrue(message, !inst.match(expression));
 
 		expression = "(OS <* {Linux, Windows, Android,IOS,AmigaOS})";
 		message = String.format(messageTemplate, expression,inst.getProperty("OS"), "<*",inst.getProperty("OSMoreElements"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 	}
 	
 	@Test
 	public void PropertyFilterOSGiImplementationSubSet_String_tc062() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecFilterSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String messageTemplate = "%s [expanded expression: %s %s %s] should be %b. By definition the A subset('<*') B means that all A elements must be in B.";
 
 		String expression = "(setString <* {doubt,grows,with,knowledge})";
 		String message = String.format(messageTemplate, expression,inst.getProperty("setString"), "<*", inst.getProperty("setString"),true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setString <* {with,doubt,knowledge,grows})";
 		message = String.format(messageTemplate, expression,inst.getProperty("setString"), "<*",inst.getProperty("setStringUnordered"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setString <* {doubt,grows,knowledge})";
 		message = String.format(messageTemplate, expression,inst.getProperty("setString"), "<*",inst.getProperty("setStringLessElements"), false);
 		Assert.assertTrue(message, !inst.match(expression));
 
 		expression = "(setString <* {doubt,and,uncertainties,grows,with,knowledge})";
 		message = String.format(messageTemplate, expression,inst.getProperty("setString"), "<*",inst.getProperty("setStringMoreElements"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 
 	}
 	
 	@Test
 	public void PropertyChangeNoticationCallback_tc063() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"PropertyChangeNotification");
 		Instance inst = implementation.createInstance(null, null);
 
 		final String PROPERTY_NAME="state";
 		final String VALUE_NEW="new value";
 		final String message_fail_set="Property being declared but after using instance.setAttribute the value inside the component did not correspont to the configured value";
 		final String message_fail_callback="The callback for property change notifications(using 'method' attribute) was not called";
 		
 		inst.setProperty(PROPERTY_NAME, VALUE_NEW);
 
 		PropertyChangeNotificationSwitch switchdevice=(PropertyChangeNotificationSwitch)inst.getServiceObject();
 
 		Assert.assertTrue(message_fail_set,switchdevice.getState().equals(VALUE_NEW));
 		
 		Assert.assertTrue(message_fail_callback,switchdevice.getStateChangedCounter()>0);
 		
 	}
 	
 	@Test
 	public void PropertyChangeNoticationCallbackCalledOnce_tc064() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"PropertyChangeNotification");
 		Instance inst = implementation.createInstance(null, null);
 
 		final String PROPERTY_NAME="state";
 		final String VALUE_NEW="new value";
 		final String message_fail_set="Property being declared but after using instance.setAttribute the value inside the component did not correspont to the configured value";
 		final String message_fail_callback="The callback for property change notifications(using 'method' attribute) was called %s times, and should have been called only once";
 		
 		inst.setProperty(PROPERTY_NAME, VALUE_NEW);
 
 		PropertyChangeNotificationSwitch switchdevice=(PropertyChangeNotificationSwitch)inst.getServiceObject();
 
 		Assert.assertTrue(message_fail_set,switchdevice.getState().equals(VALUE_NEW));
 		
 		Assert.assertTrue(String.format(message_fail_callback,switchdevice.getStateChangedCounter()),switchdevice.getStateChangedCounter()==1);
 		
 	}
 	
 	@Test
 	public void PropertySetTypeBracesNoCommaInTheEnd_tc065() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 		
 		String propertyName="fooSetValuedSimple";
 		String propertyValue=inst.getProperty(propertyName);
 		String expression="(fooSetValuedSimple *> {Android, Windows})";
 		boolean result=inst.match(expression);
 		boolean expected=true;
 		
 		String message=String.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",expression,result,expected,propertyName,propertyValue);
 		
 		if(expected)
 			Assert.assertTrue(message,result);
 		else
 			Assert.assertFalse(message,result);
 	}	
 	
 	@Test
 	public void PropertySetTypeBracesCommaInTheEnd_tc066() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 		
 		String propertyName="fooSetValuedSimple";
 		String propertyValue=inst.getProperty(propertyName);
 		String expression="(fooSetValuedSimple *> {Android, Windows,})";
 		boolean result=inst.match(expression);
 		boolean expected=true;
 		
 		String message=String.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",expression,result,expected,propertyName,propertyValue);
 		
 		if(expected)
 			Assert.assertTrue(message,result);
 		else
 			Assert.assertFalse(message,result);
 	}
 	
 	@Test
 	public void PropertySetTypeBorderElements_tc067() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 		
 		String propertyName="fooSetValuedSimple";
 		String propertyValue=inst.getProperty(propertyName);
 		String expression="(fooSetValuedSimple *> {Linux, IOS})";
 		boolean result=inst.match(expression);
 		boolean expected=true;
 		
 		String message=String.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",expression,result,expected,propertyName,propertyValue);
 		
 		if(expected)
 			Assert.assertTrue(message,result);
 		else
 			Assert.assertFalse(message,result);
 	}	
 	
 	@Test
 	public void PropertySetTypeMiddleElements_tc068() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 		
 		String propertyName="fooSetValuedSimple";
 		String propertyValue=inst.getProperty(propertyName);
 		String expression="(fooSetValuedSimple *> {Android, Windows})";
 		boolean result=inst.match(expression);
 		boolean expected=true;
 		
 		String message=String.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",expression,result,expected,propertyName,propertyValue);
 		
 		if(expected)
 			Assert.assertTrue(message,result);
 		else
 			Assert.assertFalse(message,result);
 	}	
 	
 	@Test
 	public void PropertySetTypeOneMiddleOneBorderElement_tc069() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 		
 		String propertyName="fooSetValuedSimple";
 		String propertyValue=inst.getProperty(propertyName);
 		String expression="(fooSetValuedSimple *> {Android, IOS})";
 		boolean result=inst.match(expression);
 		boolean expected=true;
 		
 		String message=String.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",expression,result,expected,propertyName,propertyValue);
 		
 		if(expected)
 			Assert.assertTrue(message,result);
 		else
 			Assert.assertFalse(message,result);
 	}	
 	
 	@Test
 	public void PropertySetTypeNoBracesComma_tc070() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 		
 		String propertyName="fooSetValuedSimple";
 		String propertyValue=inst.getProperty(propertyName);
 		String expression="(fooSetValuedSimple *> Android, Windows,)";
 		boolean result=inst.match(expression);
 		boolean expected=true;
 		
 		String message=String.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",expression,result,expected,propertyName,propertyValue);
 		
 		if(expected)
 			Assert.assertTrue(message,result);
 		else
 			Assert.assertFalse(message,result);
 	}
 	
 	@Test
 	public void PropertySetTypeNoBracesNoComma_tc071() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 		
 		String propertyName="fooSetValuedSimple";
 		String propertyValue=inst.getProperty(propertyName);
 		String expression="(fooSetValuedSimple *> Android, Windows)";
 		boolean result=inst.match(expression);
 		boolean expected=true;
 		
 		String message=String.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",expression,result,expected,propertyName,propertyValue);
 		
 		if(expected)
 			Assert.assertTrue(message,result);
 		else
 			Assert.assertFalse(message,result);
 	}
 	
 	@Test
 	public void PropertyEnumTypeSimpleValueNoTrick_tc072() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 		
 		String propertyName="barEnumValuedSimple";
 		String propertyValue=inst.getProperty(propertyName);
 		String expression="(barEnumValuedSimple=Linux)";
 		boolean result=inst.match(expression);
 		boolean expected=true;
 		
 		String message=String.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",expression,result,expected,propertyName,propertyValue);
 		
 		if(expected)
 			Assert.assertTrue(message,result);
 		else
 			Assert.assertFalse(message,result);
 	}
 	
 	@Test
 	public void PropertyEnumTypeSimpleValueSpaceAfter_tc073() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 		
 		String propertyName="barEnumValuedSimple";
 		String propertyValue=inst.getProperty(propertyName);
 		String expression="(barEnumValuedSimple=Linux )";
 		boolean result=inst.match(expression);
 		boolean expected=true;
 		
 		String message=String.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",expression,result,expected,propertyName,propertyValue);
 		
 		if(expected)
 			Assert.assertTrue(message,result);
 		else
 			Assert.assertFalse(message,result);
 	}
 	
 	@Test
 	public void PropertyEnumTypeSimpleValueSpaceBefore_tc074() {
 
 		Implementation implementation = CST.apamResolver.findImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 		
 		String propertyName="barEnumValuedSimple";
 		String propertyValue=inst.getProperty(propertyName);
 		String expression="(barEnumValuedSimple= Linux)";
 		boolean result=inst.match(expression);
 		boolean expected=true;
 		
 		String message=String.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",expression,result,expected,propertyName,propertyValue);
 		
 		if(expected)
 			Assert.assertTrue(message,result);
 		else
 			Assert.assertFalse(message,result);
 	}	
 	
 }
