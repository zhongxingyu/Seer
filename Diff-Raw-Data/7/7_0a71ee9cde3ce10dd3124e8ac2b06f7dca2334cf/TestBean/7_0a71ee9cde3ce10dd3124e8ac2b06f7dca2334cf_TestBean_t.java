 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Model;
 
 import EJB.PersonRegistry;
 import EJB.SchoolRegistry;
 import EJB.SessionRegistry;
 import EJB.UserRegistry;
 import EJB.WorkerRegistry;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.inject.Named;
 
 /**
  *
  * @author Gustav
  */
 @Named("testbean")
 @SessionScoped
 public class TestBean implements Serializable
 {
   
     
     @EJB
     private WorkerRegistry reg;
     
     @EJB
     private SchoolRegistry schReg;
     
     @EJB
     private SessionRegistry sesReg;
     
     @EJB
     private PersonRegistry pReg;
     
     @EJB
     private UserRegistry uReg;
     
     private Worker Ove;
     private List<Session> sessions;
     private List<Session> sessions2;
     private List<Worker> workerList;
     private Person person;
     private int success;
     private List<Person> contactList;
     private School school;
     private Calendar time;
 
     private Integer progress;  
     
     public Integer getProgress() {  
         if(progress == null || progress>100)  
             progress = 0;
         return progress;  
     } 
     
     public void testAll()
     {
         personRegistryTestAdd();
         personRegistryTestRemove();
         personRegistryTestUpdate();
         schoolRegistryTestAdd();
         schoolRegistryTestRemove();
         schoolRegistryTestUpdate();
         workerRegistryTestAdd();
         workerRegistryTestRemove();
         userRegistryTestAdd();
         userRegistryTestRemove();
         sessionRegistryTestAdd();
         sessionRegistryTestRemove();
         
     }
   
     public void setProgress(Integer progress) {  
         this.progress = progress;  
     }  
     
     public void personRegistryTestAdd()
     {   
         int precount = pReg.getCount();
         int tests=0;
         success=0;
         Long personNumber = Math.round(Math.random()*100000);
         person = new Person(personNumber, "Test Testsson", "test@gmail.com", "070TEST", "testVägen");
         pReg.add(person);
         precount++;
         if(pReg.getCount()==precount)
         {   
             success++;
             tests++;
             progress+=6;
         }
         if(pReg.getByPNumber(person.getIdNumber()).size()==1)
         {
             success++;
             tests++;
             progress+=6;
         }  
     
         postResults(tests);
     }
     
     public void personRegistryTestRemove()
     {
         success=0;
         int tests=0;
         Long personNumber = Math.round(Math.random()*100000);
         person = new Person(personNumber, "Test Testsson", "test@gmail.com", "070TEST", "testVägen");
         pReg.add(person);    
         int precount = pReg.getCount();
         Person p = pReg.getByPNumber(person.getIdNumber()).get(0);
         pReg.remove(p.getId());
         
         if(pReg.getCount()==precount-1)
         {   
             success++;
             tests++;
             progress+=6;
         }
 
         postResults(tests);  
     }
     public void personRegistryTestUpdate()
     {
         success=0;
         int tests=0;
         Long personNumber = Math.round(Math.random()*100000);
         person = new Person(personNumber, "Test Testsson", "test@gmail.com", "070TEST", "testVägen");
         pReg.add(person);
         String newEmail = "newEmail@gmail.com";
         person.setMail(newEmail);
         pReg.update(person);
         if(pReg.getByPNumber(personNumber).get(0).getMail().equals(newEmail))
         {
             success++;
             tests++;
             progress+=6;
         }
 
         postResults(tests);
     }
     
     private void postResults(int tests)
     {
         FacesMessage msg = new FacesMessage("Result: "+success+" of "+ tests+ " passed");
         FacesContext.getCurrentInstance().addMessage(null, msg);
     }
     public void schoolRegistryTestAdd()
     {
         int tests=0;
         success=0;
         Long idKey = Math.round(Math.random()*100000);
         String schoolName = "Chalmers" + String.valueOf(idKey);
         int schoolCount = schReg.getCount();
         school = new School("schoolName", "Gibraltargatan 3", 43351, "Göteborg", sessions, contactList);
         schReg.add(school);
         if(schReg.getCount()==schoolCount+1) {
             success++;
             tests++;
             progress+=6;
         }
         if(schReg.getByName(school.getName()).size()==1) {
             success++;
             tests++;
             progress+=6;
         }
         postResults(tests);
     }
     public void schoolRegistryTestRemove()
     {
         success=0;
         int tests = 0;
         Long idKey = Math.round(Math.random()*100000);
         String schoolName = "Chalmers" + String.valueOf(idKey);
         school = new School("schoolName", "Gibraltargatan 3", 43351, "Göteborg", sessions, contactList);
         schReg.add(school);
         int schoolCount = schReg.getCount();
         School s = schReg.getByName(school.getName()).get(0);
        schReg.remove(school.getId());
         if(schReg.getCount()==schoolCount-1) {
             success++;
             tests++;
             progress+=6;
         }
 
         postResults(tests);      
     }
     public void schoolRegistryTestUpdate()
     {
         success=0;
         int tests=0;
         Long idKey = Math.round(Math.random()*100000);
         String schoolName = "Chalmers" + String.valueOf(idKey);
         school = new School(schoolName, "Gibraltargatan 3", 43351, "Göteborg", sessions, contactList);
         schReg.add(school);
         String newAddress = "slottskogen 45";
         school.setAddress(newAddress);
         schReg.update(school);
         if(schReg.getByName(schoolName).get(0).getAddress().equals(newAddress))
         {
             success++;
             tests++;
             progress+=7;
         }
         postResults(tests);
     }
     public void workerRegistryTestAdd()
     {
         success=0;
         int tests=0;
         int workerCount = reg.getCount();
         Long personNumber = Math.round(Math.random()*100000);
         Worker w = new Worker(personNumber, "Test Gunnar", "testGunnar@gmail.com", "268487", "testgatan 3", 100);
         reg.add(w);
         if(reg.getCount()==workerCount+1)
         {
             success++;
             tests++;
             progress+=7;
         }
         if(!reg.getByPNumber(personNumber).isEmpty())
         {
             success++;
             tests++;
             progress+=7;
         }
         postResults(tests);
     }
     public void workerRegistryTestRemove()
     {
         success=0;
         int tests=0;
         Long personNumber = Math.round(Math.random()*100000);
         Worker w = new Worker(personNumber, "Test Gunnar", "testGunnar@gmail.com", "268487", "testgatan 3", 100);
         reg.add(w);
         int workerCount = reg.getCount();
         Worker worker = reg.getByPNumber(w.getIdNumber()).get(0);
         reg.remove(worker.getId());
         if(reg.getCount()==workerCount-1)
         {
             success++;
             tests++;
             progress+=7;
         }       
 
         postResults(tests);       
     }
     public void userRegistryTestAdd()
     {
         success=0;
         int tests=0;
         int userCount = uReg.getCount();
         Long personNumber = Math.round(Math.random()*100000);
         Worker w = new Worker(personNumber, "Test Gunnar", "testGunnar@gmail.com", "268487", "testgatan 3", 100);
         Account a = new Account(w, "Gunnar", "losen");
         uReg.add(a);
         if(uReg.getCount()==userCount+1)
         {
             success++;
             tests++;
             progress+=7;
         }
         postResults(tests);
     }
     public void userRegistryTestRemove()
     {
         success=0;
         int tests = 0;
         Long personNumber = Math.round(Math.random()*100000);
         Worker w = new Worker(personNumber, "Test Gunnar", "testGunnar@gmail.com", "268487", "testgatan 3", 100);
         Account a = new Account(w, "Gunnar", "losen");
         uReg.add(a);
         int userCount = uReg.getCount();
         Account acc = uReg.getByName(a.getUserName()).get(0);
         uReg.remove(acc.getId());
         if(uReg.getCount()==userCount-1)
         {
             success++;
             tests++;
             progress+=7;
         }
         postResults(tests);
     }
     public void sessionRegistryTestAdd()
     {
         success=0;
         int tests=0;
         time = Calendar.getInstance();
         int sessionCount = sesReg.getCount();
         Long idKey = Math.round(Math.random()*100000);
         String schoolName = "Chalmers" + String.valueOf(idKey);
         school = new School("schoolName", "Gibraltargatan 3", 43351, "Göteborg", sessions, contactList);
         schReg.add(school);
         Session ses = new Session(school, time.getTime(),time.getTime() , 10, workerList, "Barnen slog mig");
         sesReg.add(ses);
         if(sesReg.getCount()==sessionCount+1)
         {
             success++;
             tests++;
            progress+=7;
         }
          postResults(tests);
     }
     public void sessionRegistryTestRemove()
     {
         success=0;
         int tests=0;
         time = Calendar.getInstance();
         Long idKey = Math.round(Math.random()*100000);
         String schoolName = "Chalmers" + String.valueOf(idKey);
         school = new School("schoolName", "Gibraltargatan 3", 43351, "Göteborg", sessions, contactList);
         schReg.add(school);
         Session ses = new Session(school, time.getTime(),time.getTime() , 10, workerList, "Barnen slog mig");
         sesReg.add(ses);
         int sessionCount = sesReg.getCount();
         Session session = sesReg.find(ses.getId());
         sesReg.remove(session.getId());
         if(sesReg.getCount()==sessionCount-1)
         {
             success++;
             tests++;
           progress+=100-progress;
         }
 
         postResults(tests);
     }
             
     
     
 }
