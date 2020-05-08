 package at.ac.tuwien.ldsc.group1.application;
 
 import at.ac.tuwien.ldsc.group1.domain.CloudOverallInfo;
 import at.ac.tuwien.ldsc.group1.domain.Event;
 import at.ac.tuwien.ldsc.group1.domain.EventType;
 import at.ac.tuwien.ldsc.group1.domain.components.Application;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeSet;
 
 /**
  * This is our Energy-Efficient Cloud Elasticity Manager (E2CEM).
  * <br/>
  * It will initialise a scheduler and then control when an application is
  * added (based on the timestamp) and when it needs to be removed (based on the
  * duration).
  */
 public class E2CElasticityManager {
     List<Scheduler> schedulers;
     CsvParser csvParser;
     TreeSet<Event> events;
     List<CloudOverallInfo> infoList = new ArrayList<>();
     List<Application> appList;
     private String scenario;
 
     public E2CElasticityManager(CsvParser parser, List<Scheduler> schedulers) {
         this.csvParser = parser;
         this.schedulers = schedulers;
     }
 
     public void startSimulations() {
     	//1. Get list of application from parser
         appList = csvParser.parse();
         for(Scheduler scheduler : this.schedulers) {
             startSimulation(scheduler);
         }
     }
 
     public void startSimulation(Scheduler scheduler) {
         //2. Build interval list that transforms the list of applications from the parser
         //   into events
         events = new TreeSet<>();
 
         for (Application app : appList) {
             // For each Application there is one Event when the app STARTS, a stop event will be created by
             // the scheduler, when the application is started.
             long startTime = app.getTimeStamp();
             Event startEvent = new Event(startTime, EventType.START, app);
             events.add(startEvent);
         }
 
         scheduler.handleEvents(events);
 
         //close streams
         scheduler.finalize();
         CloudOverallInfo overAllInfo = scheduler.getOverAllInfo();
         overAllInfo.setScenario(scenario);
         infoList.add(overAllInfo);
     }
 
 
     public List<CloudOverallInfo> getCloudOverAllInfo() {
         return this.infoList;
     }
     
    public void startSpecificSimulation(int num){
         appList = csvParser.parse();
        scenario = csvParser.getFilename();
         startSimulation(this.schedulers.get(num));
     }
 
 }
 
 
