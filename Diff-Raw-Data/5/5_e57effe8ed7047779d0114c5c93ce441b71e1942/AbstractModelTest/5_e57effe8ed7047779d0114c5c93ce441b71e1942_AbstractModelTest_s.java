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
 
 import java.math.BigDecimal;
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
    ExpectedException exception = ExpectedException.none();
 
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
         h.setWage(new java.math.BigDecimal(200));
         h.setYear((short) 2010);
         h.save();
 
         HourlyWage g = new HourlyWage();
         g.setMonth((byte) 10);
         g.setQualification("u");
         g.setWage(new java.math.BigDecimal(200));
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
 
         Assistant result = new Assistant(a.getById(a.getId()));
 
         assertNotNull(result);
         assertEquals(a.getId(), result.getId());
     }
 
     /**
      * Test of getByKeys method, of class AbstractModel.
      */
     @Test
     public void testGetByKeys() {
         System.out.println("getByKeys");
 
         HourlyWage h = new HourlyWage();
         h.clearTable();
 
         h.setQualification("g");
         h.setMonth((byte) 10);
         h.setYear((short) 2010);
         h.setWage(new BigDecimal(200));
         h.save();
 
         //TODO: Rewrite with siena
         HourlyWage result = null;//new HourlyWage(h.getByKeys("g", (byte) 10,
         //    (short) 2010));
 
         assertNotNull(result);
         assertEquals(h, result);
 
         Assistant a = new Assistant();
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
         a.save();
 
        // re-implement
         /*
         Assistant res = new Assistant(a.getByKeys(a.getId()));
 
         assertEquals(a, res);
         */
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
 
         assertTrue(a.size() == size + 1);
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
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
 
         List<Assistant> a_ = new Assistant().all().filter("Vorname", "Test").filter("Nachname", "Tester").fetch();
         assertTrue(a_.size() == 1);	
         assertTrue(a.getId() > 0);
         assertEquals(a, new Assistant(a.getById(a.getId())));
 
         /* Test of updating a model */
         a.setFirstName("Tester");
         a.setLastName("Test");
 
         a_ = new Assistant().all().filter("Vorname", "Tester").filter("Nachname", "Test").fetch();
         assertTrue(a_.size() == 1);	
         assertEquals(a, new Assistant(a.getById(a.getId())));
 
         /* Test fail of doValidate */
         a.setFirstName(null);
         a.save();
         exception.expect(ValidationException.class);
         exception.expectMessage("Validation failed.");
         a.setFirstName("Tester");
 
         /* Test fail with errors */
         a.resetErrors();
         a.addError("error message");
         a.save();
         exception.expect(ValidationException.class);
         exception.expectMessage("The model was not saved because the error list is not empty.");
 
         /* Test saving when editing a primary key */
         HourlyWage h = new HourlyWage();
         h.clearTable();
         h.setMonth((byte) 10);
         h.setQualification("g");
         h.setWage(new BigDecimal(200));
         h.setYear((short) 2010);
 
         HourlyWage g = h.clone();
 
         h.save();
         List<HourlyWage> h_ = new HourlyWage().all().filter("Qualifikation", "g").filter("Monat", "10").filter("Lohn", "200").filter("Jahr", "2010").fetch();
         assertTrue(h_.size() == 1);	
 
         h.setQualification("u");
         h_ = new HourlyWage().all().filter("Qualifikation", "u").filter("Monat", "10").filter("Lohn", "200").filter("Jahr", "2010").fetch();
         assertTrue(h_.size() == 1);	
         //TODO: re-implement
         //assertTrue(h.getByKeys(g.getQualification(), g.getMonth(), g.getYear()) == null);
     }
 
     /**
      * Test of remove method, of class AbstractModel.
      */
     @Test
     public void testRemove() throws Exception {
         System.out.println("remove");
 
         Assistant a = new Assistant();
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
 
         a.remove();
         exception.expect(ValidationException.class);
 
         a.save();
         int id = a.getId();
 
         a.remove();
 
         assertNull(a.getById(id));
         assertTrue(a.getId() <= 0);
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
 
         a.setEmail(null);
         a.save();
         exception.expect(ValidationException.class);
         a.resetErrors();
 
         a.setEmail("");
         a.save();
         exception.expect(ValidationException.class);
         a.resetErrors();
 
         a.setEmail("a@example.com");
         a.save();
         exception.expect(ValidationException.class);
         a.resetErrors();
 
         a.setEmail("email@example");
         a.save();
         exception.expect(ValidationException.class);
         a.resetErrors();
 
         a.setEmail("email@example.c");
         a.save();
         exception.expect(ValidationException.class);
         a.resetErrors();
 
         a.setEmail("münchen@überälles.de");
         a.save();
         exception.expect(ValidationException.class);
         a.resetErrors();
 
         a.setEmail("email@example.com");
         a.save();
         List<Assistant> a_ = new Assistant().all().filter("Vorname", "Test")
         		.filter("Nachname", "Tester").filter("Qualifikation", "g").filter("Email", "email@example.com").fetch();
         assertTrue(a_.size() == 1);	
 
         a.setEmail("test@über-älles.de");
         a.save();
         a_ = new Assistant().all().filter("Vorname", "Test")
         		.filter("Nachname", "Tester").filter("Qualifikation", "g").filter("Email", "test@über-älles.de").fetch();
         assertTrue(a_.size() == 1);	
     }
 
 }
