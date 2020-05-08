 /*
  * Copyright 2012 CoreMedia AG
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
 
 package net.joala.bdd.reference;
 
 import org.junit.Test;
 
 import java.util.Collections;
 import java.util.Map;
 
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 
 /**
  * <p>
  * Test for {@link ReferenceImpl}.
  * </p>
  *
  * @since 6/5/12
  */
@SuppressWarnings("ProhibitedExceptionDeclared")
 public class ReferenceImplTest {
   @Test
   public void should_hold_value() throws Exception {
     final Reference<Map<Object, Object>> reference = new ReferenceImpl<Map<Object, Object>>();
     final Map<Object, Object> referenceValue = Collections.emptyMap();
     reference.set(referenceValue);
     assertSame("Reference value retrieved should hold the original value set.", referenceValue, reference.get());
   }
 
   @Test(expected = ReferenceAlreadyBoundException.class)
   public void should_deny_to_set_value_twice() throws Exception {
     final Reference<String> reference = new ReferenceImpl<String>();
     reference.set("Lorem");
     reference.set("Ipsum");
   }
 
   @Test(expected = ReferenceNotBoundException.class)
   public void should_deny_to_read_from_unbound_reference() throws Exception {
     final Reference<String> reference = new ReferenceImpl<String>();
     reference.get();
   }
 
  @SuppressWarnings("ConstantConditions")
   @Test(expected = NullPointerException.class)
   public void should_fail_for_reference_value_null() throws Exception {
     final Reference<String> reference = new ReferenceImpl<String>();
     reference.set(null);
   }
 
   @Test
   public void should_hold_property_value() throws Exception {
     final String propertyKey = "lorem";
     final Map<Object, Object> propertyValue = Collections.emptyMap();
     final Reference<String> reference = new ReferenceImpl<String>();
     reference.setProperty(propertyKey, propertyValue);
     assertSame("Uncasted property value should be same.", propertyValue, reference.getProperty(propertyKey));
     assertSame("Casted property value should be same.", propertyValue, reference.getProperty(propertyKey, propertyValue.getClass()));
   }
 
   @Test
   public void should_hold_null_property_value() throws Exception {
     final String propertyKey = "lorem";
     final Reference<String> reference = new ReferenceImpl<String>();
     reference.setProperty(propertyKey, null);
     assertNull("Uncasted property value should be same.", reference.getProperty(propertyKey));
     assertNull("Casted property value should be same.", reference.getProperty(propertyKey, String.class));
   }
 
   @Test(expected = PropertyNotSetException.class)
   public void should_fail_reading_unset_property() throws Exception {
     final Reference<String> reference = new ReferenceImpl<String>();
     reference.getProperty("lorem");
   }
 
   @Test(expected = PropertyAlreadySetException.class)
   public void should_fail_setting_property_which_got_already_set() throws Exception {
     final Reference<String> reference = new ReferenceImpl<String>();
     reference.setProperty("lorem", null);
     reference.setProperty("lorem", null);
   }
 
  @SuppressWarnings("ConstantConditions")
   @Test(expected = NullPointerException.class)
   public void should_fail_for_set_with_property_key_null() throws Exception {
     final Reference<String> reference = new ReferenceImpl<String>();
     reference.setProperty(null, "value");
   }
 
 }
