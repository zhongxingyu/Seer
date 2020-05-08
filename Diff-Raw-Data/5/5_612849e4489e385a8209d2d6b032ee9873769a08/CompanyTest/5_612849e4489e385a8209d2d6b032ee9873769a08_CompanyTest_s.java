 /**
  * Ian Dimayuga
  * EECS293 HW1
  */
 package edu.cwru.icd3;
 
import static org.junit.Assert.*;
 
import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author ian
  *
  */
 public class CompanyTest {
 
     private Company m_testCompany;
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
         m_testCompany = new Company();
         m_testCompany.add("Daniel", null);
         m_testCompany.add("Jessica", "Daniel");
         m_testCompany.add("Harvey", "Jessica");
         m_testCompany.add("Louis", "Daniel");
         m_testCompany.add("Rachel", "Jessica");
         m_testCompany.add("Donna", "Harvey");
         m_testCompany.add("Mike", "Harvey");
     }
 
     /**
      * Test method for {@link edu.cwru.icd3.Company#Company()}.
      */
     @Test
     public void testCompany() {
         Company emptyCompany = new Company();
 
         assertTrue(emptyCompany.employeeSet().isEmpty());
         assertTrue(emptyCompany.managerSet().isEmpty());
         assertTrue(emptyCompany.managersByDepartmentSize().isEmpty());
     }
 
     /**
      * Test method for {@link edu.cwru.icd3.Company#add(java.lang.String, java.lang.String)}.
      */
     @Test
     public void testAdd() {
         assertFalse(m_testCompany.employeeSet().contains("Norma"));
         assertFalse(m_testCompany.managerSet().contains("Louis"));
 
         m_testCompany.add("Norma", "Louis");
 
         assertTrue(m_testCompany.employeeSet().contains("Norma"));
         assertTrue(m_testCompany.managerSet().contains("Louis"));
         assertTrue(m_testCompany.managerOf("Norma").equals("Louis"));
     }
 
     /**
      * Test method for {@link edu.cwru.icd3.Company#addAll(java.util.Set, java.lang.String)}.
      */
     @Test
     public void testAddAll() {
         assertTrue(m_testCompany.managerOf("Rachel").equals("Jessica"));
         Set<String> associates = new HashSet<String>();
         associates.add("Kyle");
         associates.add("Harold");
         associates.add("Rachel"); // Should not change
 
         m_testCompany.addAll(associates, "Louis");
 
         assertTrue(m_testCompany.employeeSet().containsAll(associates));
         assertTrue(m_testCompany.managerOf("Kyle").equals("Louis"));
         assertTrue(m_testCompany.managerOf("Harold").equals("Louis"));
         assertFalse(m_testCompany.managerOf("Rachel").equals("Louis"));
         assertTrue(m_testCompany.managerOf("Rachel").equals("Jessica"));
     }
 
     /**
      * Test method for {@link edu.cwru.icd3.Company#delete(java.lang.String)}.
      */
     @Test
     public void testDelete() {
         assertTrue(m_testCompany.employeeSet().contains("Donna"));
         assertTrue(m_testCompany.managerSet().contains("Harvey"));
         assertTrue(m_testCompany.managerOf("Donna").equals("Harvey"));
 
         m_testCompany.delete("Donna");
 
         assertFalse(m_testCompany.employeeSet().contains("Donna"));
         assertTrue(m_testCompany.managerSet().contains("Harvey")); // Harvey still manages Mike
     }
 
     /**
      * Test method for {@link edu.cwru.icd3.Company#managerOf(java.lang.String)}.
      */
     @Test
     public void testManagerOf() {
         assertTrue(m_testCompany.managerOf("Donna").equals("Harvey"));
         assertTrue(m_testCompany.managerOf("Mike").equals("Harvey"));
         assertTrue(m_testCompany.managerOf("Harvey").equals("Jessica"));
         assertTrue(m_testCompany.managerOf("Rachel").equals("Jessica"));
         assertTrue(m_testCompany.managerOf("Jessica").equals("Daniel"));
         assertTrue(m_testCompany.managerOf("Louis").equals("Daniel"));
         assertTrue(m_testCompany.managerOf("Daniel") == null);
     }
 
     /**
      * Test method for {@link edu.cwru.icd3.Company#managerSet()}.
      */
     @Test
     public void testManagerSet() {
         Set<String> managers = new HashSet<String>();
         managers.add("Daniel");
         managers.add("Jessica");
         managers.add("Harvey");
 
         assertTrue(m_testCompany.managerSet().equals(managers));
     }
 
     /**
      * Test method for {@link edu.cwru.icd3.Company#employeeSet()}.
      */
     @Test
     public void testEmployeeSet() {
         String[] employeeArray = {"Daniel", "Jessica", "Louis", "Harvey", "Rachel", "Donna", "Mike"};
         Set<String> employees = new HashSet<String>();
         for (String employee : employeeArray) {
             employees.add(employee);
         }
 
         assertTrue(m_testCompany.employeeSet().equals(employees));
     }
 
     /**
      * Test method for {@link edu.cwru.icd3.Company#departmentOf(java.lang.String)}.
      */
     @Test
     public void testDepartmentOf() {
         Company jessicaDept = m_testCompany.departmentOf("Jessica");
 
         assertFalse(jessicaDept.employeeSet().contains("Louis"));
         assertFalse(jessicaDept.employeeSet().contains("Daniel"));
         assertTrue(jessicaDept.managerOf("Harvey").equals("Jessica"));
         assertTrue(jessicaDept.managerOf("Rachel").equals("Jessica"));
         assertTrue(jessicaDept.managerOf("Donna").equals("Harvey"));
         assertTrue(jessicaDept.managerOf("Mike").equals("Harvey"));
         assertTrue(jessicaDept.managerOf("Jessica") == null);
     }
 
     /**
      * Test method for {@link edu.cwru.icd3.Company#managersByDepartmentSize()}.
      */
     @Test
     public void testManagersByDepartmentSize() {
         String[] correctOrder = {"Donna", "Louis", "Mike", "Rachel", "Harvey", "Jessica", "Daniel"};
 
         List<String> testOrder = m_testCompany.managersByDepartmentSize();
 
         assertTrue(correctOrder.length == testOrder.size());
 
         for (int i = 0; i < correctOrder.length; i++) {
             assertTrue(correctOrder[i].equals(testOrder.get(i)));
         }
     }
 
 }
