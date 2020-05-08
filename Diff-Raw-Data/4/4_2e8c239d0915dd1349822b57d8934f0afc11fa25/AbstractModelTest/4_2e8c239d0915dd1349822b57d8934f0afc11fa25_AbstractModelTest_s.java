 package de.aidger.model;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 import java.math.BigDecimal;
 
 import org.junit.Test;
 
 import de.aidger.model.models.Employment;
 import de.aidger.model.models.HourlyWage;
 import de.aidger.model.models.Assistant;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IHourlyWage;
 
 /**
  * 
  * @author rmbl
  */
 public class AbstractModelTest {
 
     public AbstractModelTest() {
         Runtime.getInstance().initialize();
     }
 
     /**
      * Test of getAll method, of class AbstractModel.
      */
     @Test
     public void testGetAll() throws AdoHiveException {
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
 
         List<IHourlyWage> list = h.getAll();
 
         assertNotNull(list);
         assertEquals(2, list.size());
         assertEquals(h, new HourlyWage(list.get(0)));
         assertEquals(g, new HourlyWage(list.get(1)));
     }
 
     /**
      * Test of getById method, of class AbstractModel.
      */
     @Test
     public void testGetById() throws AdoHiveException {
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
     public void testGetByKeys() throws AdoHiveException {
         System.out.println("getByKeys");
 
         HourlyWage h = new HourlyWage();
         h.clearTable();
         
         h.setQualification("g");
         h.setMonth((byte) 10);
         h.setYear((short) 2010);
         h.setWage(new BigDecimal(200));
         h.save();
 
         // TODO: Has to fail! AdoHive Bug
        HourlyWage result = new HourlyWage(h.getByKeys("BLA", (byte) 11,
                (short) 2011));
 
         assertNotNull(result);
         assertEquals(h, result);
 
         Assistant a = new Assistant();
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
         a.save();
 
         Assistant res = new Assistant(a.getByKeys(a.getId()));
 
         assertEquals(a, res);
     }
 
     /**
      * Test of size method, of class AbstractModel.
      */
     @Test
     public void testSize() throws AdoHiveException {
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
     public void testIsEmpty() throws AdoHiveException {
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
     public void testIsInDatabase() throws AdoHiveException {
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
     public void testClearTable() throws AdoHiveException {
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
     public void testSave() throws AdoHiveException {
         System.out.println("save");
 
         /* Test of adding a model */
         Assistant a = new Assistant();
         a.setEmail("test@example.com");
         a.setFirstName("Test");
         a.setLastName("Tester");
         a.setQualification("g");
 
         assertTrue(a.save());
         assertTrue(a.getId() > 0);
         assertEquals(a, new Assistant(a.getById(a.getId())));
 
         /* Test of updating a model */
         a.setFirstName("Tester");
         a.setLastName("Test");
 
         assertTrue(a.save());
         assertEquals(a, new Assistant(a.getById(a.getId())));
 
         /* Test fail of doValidate */
         a.setFirstName(null);
         assertFalse(a.save());
 
         /* Test saving when editing a primary key */
         HourlyWage h = new HourlyWage();
         h.clearTable();
         h.setMonth((byte) 10);
         h.setQualification("g");
         h.setWage(new BigDecimal(200));
         h.setYear((short) 2010);
 
         assertTrue(h.save());
 
         h.setQualification("u");
         assertTrue(h.save());
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
         e.addError("field", "error message");
         e.addError("other-field", "other error message");
 
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
         e.addError("field", "error message");
 
         assertTrue(e.getErrors().size() == 2);
         assertTrue(e.getErrorsFor("field").size() == 1);
 
         e.resetErrors();
 
         assertTrue(e.getErrors().isEmpty());
         assertNull(e.getErrorsFor("field"));
     }
 
     /**
      * Test of setNew method, of class AbstractModel.
      */
     @Test
     public void testSetNew() {
         System.out.println("setNew");
 
         Assistant a = new Assistant();
         a.setId(3);
         a.setNew(true);
 
         assertTrue(a.getId() == 0);
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
 
         assertEquals("Assistant [ID: 0, Qualification: g, FirstName: Test, " +
                 "LastName: Tester, Email: test@example.com]", a.toString());
     }
 
 }
