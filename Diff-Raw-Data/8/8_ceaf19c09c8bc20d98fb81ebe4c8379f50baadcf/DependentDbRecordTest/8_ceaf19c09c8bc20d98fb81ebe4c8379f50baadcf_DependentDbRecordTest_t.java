 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package oscar.test;
 
 //~--- non-JDK imports --------------------------------------------------------
 import java.util.ArrayList;
 import java.util.HashMap;
 import oscar.model.Person;
 import oscar.model.Staff;
 import oscar.MVC.DbRecord;
 import oscar.util.Utility;
 
 /**
  *
  * @author sujan
  */
 public class DependentDbRecordTest extends BaseTestCase {
 
     public void testShouldAdd() {
 
         Person person = new Person();
         person.setName("Sujan");
         person.setSurname("Dhakal");
         person.setDateOfBirth("2011-1-1");
         person.setPhone("2312312");
         person.setAddress("This is address");
         person.setEmail("sujan@dhakal.com");
        
        assertEquals(true, person.add());
 
         /*Staff staff = new Staff();
         staff.setAddress("test@test.com");
         staff.setName("Sujan");
          * 
          */
         // staff.setDependentTable("person");
 
 
         //printHashMap(person.toHashMap());
         // HashMap<String,String> record = new HashMap<String, String>();
         //record.put("email", "test@test.com");
         //Utility.fill(record, person);
       // System.out.println(person.addPk());
         //assertEquals(true, person.add() );
     }
 
     public void testStaffAdd() {
         Staff person = new Staff();
         //person.setPrimaryKey("personId");
         
         person.setName("My Name");
         person.setSurname("Dhakal");
         person.setDateOfBirth("2011-1-1");
         person.setPhone("2312312");
         person.setAddress("This is address");
         person.setEmail("sujan@dhakal.com");
 
         person.setUsername("sujan");
         person.setIsAdmin("1");
         person.setPassword("test");
 
         //HashMap<String, HashMap<String, String>> data = ;
         try {
           assertEquals(true, person.addDependent());
         } catch (Exception e) {
             e.printStackTrace();
         }
         /*
         for(String s: data.keySet()){
         System.out.println(s);
         HashMap<String,String> map = data.get(s);
         this.printHashMap(map);
         
         }*/
 
 
 
     }
     
     public void disabledTestShouldReturnStaffRecords(){
         
         Staff staff = new Staff();
         ArrayList<HashMap<String,String>> records = (ArrayList<HashMap<String,String>>) staff.findDependentBy("*", "*");
         System.out.println("Records: "+records.size());
         for(HashMap<String,String> record:records){
             this.printHashMap(record);
             System.out.println("---------------------------------------");
         }
     }
     
     
     public void disabledTestShouldDisplayStaffRecords(){
         
         ArrayList<HashMap<String,String>> dependencies = new ArrayList<HashMap<String, String>>();
         
         HashMap<String,String> personDep = new HashMap<String, String>();
         
         personDep.put("table", "person");
         personDep.put("pk", "personId");
         personDep.put("joinType", "inner join");
         
         //personDep.put("joinTo", "");
         personDep.put("fk", "personId");
         
          /*HashMap<String,String> rentalDep = new HashMap<String, String>();
         
         rentalDep.put("table", "customer");
         rentalDep.put("pk", "id");
         rentalDep.put("joinType", "inner join");
         
         rentalDep.put("fk", "customerIdFK");*/
         
         
         dependencies.add(personDep);
        // dependencies.add(rentalDep);
         System.out.println("--------------START TESTING-------------------------");
         Staff staff = new Staff();
         ArrayList<HashMap<String,String>> records = staff.queryDependent(dependencies, "*", "*");
         for(HashMap<String,String> record:records){
             this.printHashMap(record);
             System.out.println("---------------------------------------");
         }
         
         
         
     }
     
 }
