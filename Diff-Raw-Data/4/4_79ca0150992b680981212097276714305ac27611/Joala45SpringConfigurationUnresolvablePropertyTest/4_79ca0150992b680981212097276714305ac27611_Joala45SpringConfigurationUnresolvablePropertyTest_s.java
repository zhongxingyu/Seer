 /*
  * Copyright 2013 CoreMedia AG
  *
  * This file is part of Joala.
  *
  * Joala is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Joala is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.joala.condition.regression;
 
 import org.junit.Test;
 import org.springframework.beans.BeansException;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 /**
  * @since 2013-01-30
  */
 public class Joala45SpringConfigurationUnresolvablePropertyTest {
   @Test
   public void scenario_local_property_placeholder_fails_for_property_from_conditions_context_order_1() throws Exception {
     try {
       new ClassPathXmlApplicationContext("/META-INF/joala/condition/joala-45-context-1.xml");
       fail("Expected BeansException reporting unresolvable placeholder condition.timeout.seconds");
     } catch (BeansException ignored) {
       // we are fine
     }
   }
 
   @Test
   public void scenario_local_property_placeholder_fails_for_property_from_conditions_context_order_2() throws Exception {
     try {
       new ClassPathXmlApplicationContext("/META-INF/joala/condition/joala-45-context-3.xml");
       fail("Expected BeansException reporting unresolvable placeholder condition.timeout.seconds");
     } catch (BeansException ignored) {
       // we are fine
     }
   }
 
   @Test
   public void scenario_joala45_ignore_unresolvable_for_local_properties() throws Exception {
     final ClassPathXmlApplicationContext applicationContext;
     try {
       applicationContext = new ClassPathXmlApplicationContext("/META-INF/joala/condition/joala-45-context-2.xml");
     } catch (BeansException e) {
       throw new AssertionError("Joala 45 Regression: Conditions Context should not try to resolve local properties.", e);
     }
     final Object myString = applicationContext.getBean("myString");
     assertEquals("Local bean should have been correctly filled with properties.", "myString", myString);
   }
 
 }
