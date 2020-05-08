 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.aidger.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import de.aidger.model.models.Activity;
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Contract;
 import de.aidger.model.models.Course;
 import de.aidger.model.models.Employment;
 import de.aidger.model.models.FinancialCategory;
 import de.aidger.model.models.HourlyWage;
 import de.aidger.model.validators.ValidationException;
 
 /**
  * Tests the AbstractModel class.
  * 
  * @author aidGer Team
  */
 public class AbstractModelTest {
 
     @BeforeClass
     public static void beforeClassSetUp() {
         Runtime.getInstance().initialize();
         new Employment().clearTable();        
         new Activity().clearTable();
         new Assistant().clearTable();
         new Course().clearTable();
         new HourlyWage().clearTable();
         new FinancialCategory().clearTable();
         new Contract().clearTable();
     }
     
     @Rule
     public ExpectedException exception = ExpectedException.none();
 
     /**
      * Test of getAll method, of class AbstractModel.
      */
     @Test
     public void testGetAll() {
         System.out.println("getAll");
 
         HourlyWage h = new HourlyWage();
         h.clearTable();
 
         h.setMonth((byte) 10);
         h.setQualification("g");
         h.setWage(200.0);
         h.setYear((short) 2010);
         h.save();
 
         HourlyWage g = new HourlyWage();
         g.setMonth((byte) 10);
         g.setQualification("u");
         g.setWage(200.0);
         g.setYear((short) 2010);
         g.save();
 
         List<HourlyWage> list = h.getAll();
 
         assertNotNull(list);
         assertEquals(2, list.size());
         assertEquals(h, new HourlyWage(list.get(0)));
         assertEquals(g, new HourlyWage(list.get(1)));
     }
 
     /**
      * Test of getById method, of class AbstractModel.
      */
     @Test
     public void testGetById() {
         System.out.println("getById");
 
         Assistant a = new Assistant();
         a.clearTable();
 
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
         a.save();
 
         Assistant result = a.getById(a.getId());
 
         assertNotNull(result);
         assertEquals(a.getId(), result.getId());
     }
 
     /**
      * Test of getByKey(s) method, of class AbstractModel.
      */
     @Test
     public void testGetByKeys() {
         System.out.println("getByKeys");
 
         HourlyWage h = new HourlyWage();
         h.clearTable();
 
         h.setQualification("g");
         h.setMonth((byte) 10);
         h.setYear((short) 2010);
         h.setWage(200.0);
         h.save();
 
         HourlyWage result = h.getByKeys("g", (byte) 10, (short) 2010);
 
         assertNotNull(result);
         assertEquals(h, result);
 
         Assistant a = new Assistant();
         a.clearTable();
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
         a.save();
 
         Assistant res = a.getByKey(a.getId());
         assertNotNull(res);
         assertEquals(a, res);
     }
 
     /**
      * Test of size method, of class AbstractModel.
      */
     @Test
     public void testSize() {
         System.out.println("size");
 
         Assistant a = new Assistant();
 
         int size = a.size();
 
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
         a.save();
 
         assertEquals(size + 1, a.size());
     }
 
     /**
      * Test of isEmpty method, of class AbstractModel.
      */
     @Test
     public void testIsEmpty() {
         System.out.println("isEmpty");
 
         Assistant a = new Assistant();
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
         a.save();
 
         assertTrue(!a.isEmpty());
 
         a.clearTable();
         assertTrue(a.isEmpty());
     }
 
     /**
      * Test of isInDatabase method, of class AbstractModel.
      */
     @Test
     public void testIsInDatabase() {
         System.out.println("isInDatabase");
 
         Assistant a = new Assistant();
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
 
         assertTrue(!a.isInDatabase());
 
         a.save();
         assertTrue(a.isInDatabase());
     }
 
     /**
      * Test of clearTable method, of class AbstractModel.
      */
     @Test
     public void testClearTable() {
         System.out.println("clearTable");
 
         Assistant a = new Assistant();
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
         a.save();
 
         assertTrue(a.size() > 0);
 
         a.clearTable();
         assertTrue(a.size() == 0);
     }
 
     /**
      * Test of save method, of class AbstractModel.
      */
     @Test
     public void testSave() {
         System.out.println("save");
 
         /* Test of adding a model */
         Assistant a = new Assistant();
         a.clearTable();
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
         a.save();
 
         List<Assistant> a_ = new Assistant().all().filter("firstName", "Test").filter("lastName", "Tester").fetch();
         assertTrue(a_.size() == 1);	
         assertTrue(a.getId() > 0);
         assertEquals(a, a.getById(a.getId()));
 
         /* Test of updating a model */
         a.setFirstName("Tester");
         a.setLastName("Test");
         a.save();
 
         a_ = new Assistant().all().filter("firstName", "Tester").filter("lastName", "Test").fetch();
         assertTrue(a_.size() == 1);	
         assertEquals(a, a.getById(a.getId()));
 
         /* Test fail of doValidate */
         exception.expect(ValidationException.class);
         exception.expectMessage("Validation failed.");
         a.setFirstName(null);
         a.save();
         a.setFirstName("Tester");
         exception = ExpectedException.none();
 
         /* Test fail with errors */
         exception.expect(ValidationException.class);
         exception.expectMessage("The model was not saved because the error list is not empty.");
         a.resetErrors();
         a.addError("error message");
         a.save();
         exception = ExpectedException.none();
 
         /* Test saving when editing a primary key */
         HourlyWage h = new HourlyWage();
         h.clearTable();
         h.setMonth((byte) 10);
         h.setQualification("g");
         h.setWage(200.0);
         h.setYear((short) 2010);
 
         HourlyWage g = h.clone();
 
         h.save();
         List<HourlyWage> h_ = new HourlyWage().all().filter("qualification", "g").filter("month", "10").filter("wage", "200").filter("year", "2010").fetch();
         assertTrue(h_.size() == 1);	
 
         h.setQualification("u");
         h_ = new HourlyWage().all().filter("qualification", "u").filter("month", "10").filter("wage", "200").filter("year", "2010").fetch();
         assertTrue(h_.size() == 1);
         assertTrue(h.getByKeys(g.getQualification(), g.getMonth(), g.getYear()) == null);
     }
 
     /**
      * Test of remove method, of class AbstractModel.
      */
     @Test
     public void testRemove() {
         System.out.println("remove");
 
         Assistant a = new Assistant();
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
 
         a.remove();
        assertNull(a.getId());
 
         a.save();
         Long id = a.getId();
         a.remove();
 
         assertNull(a.getById(id));
         assertNull(a.getId());
     }
 
     /**
      * Test of addError method, of class AbstractModel.
      */
     @Test
     public void testAddError() {
         System.out.println("addError");
 
         Employment e = new Employment();
         e.addError("error message");
 
         List<String> result = e.getErrors();
         assertTrue(result.size() == 1);
         assertTrue(result.get(0).equals("error message"));
     }
 
     /**
      * Test of addError method, of class AbstractModel.
      */
     @Test
     public void testAddError_field() {
         System.out.println("addError_field");
 
         Employment e = new Employment();
         e.addError("field", "field", "error message");
         e.addError("other-field", "other-field", "other error message");
 
         List<String> result = e.getErrorsFor("field");
 
         assertTrue(result.size() == 1);
         assertTrue(e.getErrors().size() == 2);
         assertTrue(result.get(0).equals("field error message"));
     }
 
     /**
      * Test of resetErrors method, of class AbstractModel.
      */
     @Test
     public void testResetErrors() {
         System.out.println("resetErrors");
 
         Employment e = new Employment();
         e.addError("error message");
         e.addError("field", "field", "error message");
 
         assertTrue(e.getErrors().size() == 2);
         assertTrue(e.getErrorsFor("field").size() == 1);
 
         e.resetErrors();
 
         assertTrue(e.getErrors().isEmpty());
         assertNull(e.getErrorsFor("field"));
     }
 
     /**
      * Test of toString method, of class AbstractModel.
      */
     @Test
     public void testToString() {
         System.out.println("toString");
 
         Assistant a = new Assistant();
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
 
         String result = a.toString();
 
         assertTrue(result.contains("Email: " + a.getEmail()));
         assertTrue(result.contains("FirstName: " + a.getFirstName()));
         assertTrue(result.contains("LastName: " + a.getLastName()));
         assertTrue(result.contains("Qualification: " + a.getQualification()));
     }
 
     /**
      * Test of validateEmailOf method, of class AbstractModel.
      */
     @Test
     public void testValidateEmailOf() {
         System.out.println("validateEmailOf");
 
         Assistant a = new Assistant();
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
 
         exception.expect(ValidationException.class);
         a.setEmail(null);
         a.save();
         a.resetErrors();
 
         a.setEmail("");
         a.save();
         a.resetErrors();
 
         a.setEmail("a@example.com");
         a.save();
         a.resetErrors();
 
         a.setEmail("email@example");
         a.save();
         a.resetErrors();
 
         a.setEmail("email@example.c");
         a.save();
         a.resetErrors();
 
         a.setEmail("münchen@überälles.de");
         a.save();
         a.resetErrors();
 
         exception = ExpectedException.none();
         a.setEmail("email@example.com");
         a.save();
         List<Assistant> a_ = new Assistant().all().filter("firstName", "Test")
         		.filter("lastName", "Tester").filter("qualification", "g").filter("email", "email@example.com").fetch();
         assertTrue(a_.size() == 1);	
 
         a.setEmail("test@über-älles.de");
         a.save();
         a_ = new Assistant().all().filter("firstName", "Test")
         		.filter("lastName", "Tester").filter("qualification", "g").filter("email", "test@über-älles.de").fetch();
         assertTrue(a_.size() == 1);	
     }
 
 }
