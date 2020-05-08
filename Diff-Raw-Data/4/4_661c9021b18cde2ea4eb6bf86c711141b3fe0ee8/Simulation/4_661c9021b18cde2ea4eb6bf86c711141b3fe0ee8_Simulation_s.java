 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package domain;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.Hashtable;
 /**
  *
  * @author Cody
  */
 public class Simulation {
     
     
     private String configpath;
     private Dispatcher dispatcher;
     private ArrayList<ServiceStation> stations;
     private Config config;
     
     public Simulation(String config) {
         
         try {
             
             this.configpath = config;
            this.dispatcher = new Dispatcher();
            this.config = new Config(this.configpath); 
             this.stations = new ArrayList<ServiceStation>();
             
         } catch (Exception e){
             
              System.out.println(e.getMessage());
         }
             
         
     }
     
     
 
     public void build() {
 //              Hashtable station_types = this.config.stationTypes();
 ////              
 //
 //                Set<String> keys = station_types.keySet();
 //                for(String key: keys){
 //                    System.out.println("KEY: "+key+" Value:"+station_types.get(key));
 //                    if ( key.matches(".*/.id.*") == true){
 //                        
 //                        
 //                    }
 //                }
         
         
                 Hashtable stations = this.config.stations();
                 Set<String> keys3 = stations.keySet();
                 for(String key: keys3){
                     //System.out.println("KEY: "+key+" Value:"+stations.get(key));
                     
                     if ( key.matches(".*.id.*") == true){
                         
                         ServiceStation s = new ServiceStation();
                         this.stations.add(s);
                     }  
                 }
         
              
                System.out.println(this.stations);
 //                
 //                Hashtable queues = this.config.queues();
 ////              
 //
 //                Set<String> keys2 = queues.keySet();
 //                for(String key: keys2){
 //                    System.out.println("KEY: "+key+" Value:"+queues.get(key));
 //                }
 //        
 // 
 //                
 //                Hashtable settings = this.config.settings();
 ////              
 //
 //                Set<String> keys4 = settings.keySet();
 //                for(String key: keys4){
 //                    System.out.println("KEY: "+key+" Value:"+settings.get(key));
 //                }
 //                
                 
         
 //        Integer stationCount = 5;
 //        Integer queueCount = 5;
         
         // custtypes
         // queuetypes
         // 
         
         
         // create queues
         
 
 
        // Create the queues
 //       for(Integer i = 0; i < queueCount; i++){
 //           
 //          Queue q = new Queue();
 //          
 //          this.dispatcher.addQueue(q);
 //  
 //       }
         
     }
     
     // Maybe this could be runnable but i'm not familiar with it
     public void start(){
         
 //        this.dispatcher.run();
 //        
     } 
     
     
     
 }
