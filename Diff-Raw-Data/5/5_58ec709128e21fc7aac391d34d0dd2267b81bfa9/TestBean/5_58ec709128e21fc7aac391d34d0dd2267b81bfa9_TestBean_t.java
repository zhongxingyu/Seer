 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Model;
 
 import EJB.PersonRegistry;
 import EJB.SchoolRegistry;
 import EJB.SessionRegistry;
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
 @Named("testBean")
 @SessionScoped
 public class TestBean implements Serializable
 {
   
     
     @EJB
     private WorkerRegistry reg;
     
     @EJB
     private SchoolRegistry schReg;
     
     @EJB
     private SessionRegistry sesReg;
     
     private Worker Ove;
     private List<Session> sessions;
     private List<Session> sessions2;
     private List<Worker> workerList;
     
     public TestBean()
     {
         
     }
     public void addWorker(ActionEvent event)
     {   
         Ove = new Worker(1232312L,"Ove Sundberg", "ove@Sundberg.se", "34324234", "parkbänken 3");
         reg.add(Ove);
         FacesMessage msg = null; 
         if(reg.getCount()==1)
         {
             msg = new FacesMessage("Worker added");
         }
         else
         {
             msg = new FacesMessage("Couldnt add worker");
         }
     }
     /**
      * Adds two schools to the reg
      */
     private void addTwoSchools()
     {
         List<Person> contactPersons = new ArrayList<Person>();
         School firstSchool = new School("BackaSkolan", "34342", contactPersons);
         School secondSchool = new School("Hvitfeldtska gymnasiet", "23341", contactPersons);
         schReg.add(firstSchool);
         schReg.add(secondSchool);
     }
     public void initTwoSchools()
     {
         List<Person> contactPersons = new ArrayList<Person>();
         contactPersons.add(new Person(881003L, "Rikard Eriksson", "rikard@gmail.com", "07011111", "avenyn 5"));     
         School school = new School("BackaSkolan", "41681", contactPersons);
      //   School school2 = new School("Hvitfeldtska", "33232", contactPersons);
         sessions = new ArrayList<Session>();
         List<Worker> wList = new ArrayList<Worker>();
         
         //Add Ove Sundberg to two different sessions
         wList.add(new Worker(851027L, "Ove Sundberg", "ove@gmail.com", "0702222", "ovegatan 5", 113));       
         Session ses1 = new Session(new Date(), new Date(), 5, wList);
         Session ses2 = new Session(new Date(), new Date(), 7, wList);
  
         wList = new ArrayList<Worker>();
         //Then add a random person to a third one
         wList.add(new Worker(821127L, "Inte Ove Sundberg", "notove@gmail.com", "0705555", "inteovegatan 5", 5));
         Session ses3 = new Session(new Date(), new Date(), 1, wList);
         
         sessions.add(ses1);
         sessions.add(ses2);
         sessions.add(ses3);
         Schedule schedule = new Schedule(sessions);
         
         school.setSchedule(schedule);
      //   schReg.add(school2);
         schReg.add(school);
        // Worker oddWorker = new Worker(77777L, "oddOve", "oddOve@gmail.com", "0706666", "oddovegatan 5", 12);
        // oddWorker.setSessions(sessions);
        // reg.add(oddWorker);
     }
     
     public void testSalary()
     {
         initTwoSchools();
         Worker ove = reg.getByName("Ove Sundberg").get(0);
         List<Session> oveList = sesReg.getWorkersSessions(ove.getId());
         if(oveList.size()==2)
         {
             FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "TestSalary", "TestSalary passed!");     
             FacesContext.getCurrentInstance().addMessage(null, msg);  
         }
         
         schReg.remove(schReg.getByName("BackaSkolan").get(0).getId());
         //reg.remove(reg.getByName("Ove Sundberg").get(0).getId());
         //reg.remove(reg.getByName("Inte Ove Sundberg").get(0).getId());
         
     }
     
     public void testAdminSchedule(ActionEvent event)
     {
         sessions = new ArrayList<Session>();
         sessions2 = new ArrayList<Session>();
         workerList = new ArrayList<Worker>();
         Calendar time = Calendar.getInstance();
         Ove = new Worker(1232312L,"Ove Sundberg", "ove@Sundberg.se", "34324234", 
                 "parkbänken utanför konsum");
         workerList.add(Ove);
         Session session = new Session(new Date(time.getTimeInMillis()), new Date(time.getTimeInMillis()+10000), 45, workerList);
         session.setNotation("DET HÄR ÄR EN NOTATION");
         sessions.add(session);
         Schedule schedule = new Schedule(sessions);
         School school = new School("Chalmers", "chalmersgatan 4", 43351, "Göteborg");
 
         school.setSchedule(schedule);
         
   //     schReg.add(school);
         
         Session session2 = new Session(new Date(time.getTimeInMillis()+99999*1000), new Date(time.getTimeInMillis()+99999*1500), 23, workerList);
         sessions2.add(session2);
         Schedule schedule2 = new Schedule(sessions2);
         School school2 = new School("Backaskolan", "Backavägen 64", 23311, "Hisingen");
         school2.setSchedule(schedule2);
         
        schReg.add(school2);
         
         
         
     }
     
     
 }
