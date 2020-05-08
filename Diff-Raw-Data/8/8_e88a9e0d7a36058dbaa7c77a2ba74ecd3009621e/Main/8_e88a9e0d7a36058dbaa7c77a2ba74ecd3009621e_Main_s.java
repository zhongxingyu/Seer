 package com.movile.bin;
 
 import org.apache.log4j.xml.DOMConfigurator;
 
 import com.movile.bean.Person;
 import com.movile.cassandra.EmployeeDAOImpl;
 import com.movile.utils.AppProperties;
 
 /**
  * @author J.P. Eiti Kimura (eiti.kimura@movile.com)
  */
 public final class Main {
     
     private Main() {
         
     }
 
     /**
      * @param args
      */
     public static void main(String[] args) throws Exception {

         // initializing resouces, logs property files and etc...
         DOMConfigurator.configure("conf/log/log4j.xml");
         AppProperties.getDefaultInstance().loadProperties("conf/const.properties");
 
         EmployeeDAOImpl empDAO = new EmployeeDAOImpl();
 
         // inserting data to cluster column family
         System.out.println("Inserting data...");
         Person person = new Person("ekm82", "Eiti Kimura", "boom", "mypassword", "eiti@mail.com");
         empDAO.save(person);
         System.out.println(person);
 
         // the data struture inside cassandra will be:
         // [default@Company] get Employees['ekm82'];
         // => (column=creation, value=1316785090427, timestamp=1316785090452003)
         // => (column=email, value=eiti@mail.com, timestamp=1316785090452000)
         // => (column=login, value=boom, timestamp=1316785090452001)
         // => (column=name, value=Eiti Kimura, timestamp=1316785090444000)
         // => (column=passwd, value=mypassword, timestamp=1316785090452002)
 
         // retrieving data from comlumn family
         System.out.println("Reading data");
         Person personRetrieved = new Person();
         personRetrieved = empDAO.getPerson("ekm82");
         System.out.println(personRetrieved);
 
         // finish the resources
         empDAO.shutdown();
     }
 
 }
