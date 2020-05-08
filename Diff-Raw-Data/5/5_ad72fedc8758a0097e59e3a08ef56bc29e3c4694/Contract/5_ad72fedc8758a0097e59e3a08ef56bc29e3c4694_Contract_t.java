 package co.gridport.server.domain;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 
 public class Contract {
 
     protected String name;
     protected List<Integer> endpoints;
     protected List<String> groups = new ArrayList<String>();
     protected long intervalms; 
     protected long last_request; 
     protected int frequency;
 
     protected double decayRate;
     protected double counter;
     private Object lock = new Object();
 
     private List<String> ipFilters;
 
     public Contract(
         final String name,
         final String ipFilters,
         final Long intervalms,
         final Integer frequency,
         final List<String> groups,
         final List<Integer> endpoints
     ) 
     {
         this.name = name;
         this.ipFilters = ipFilters == null 
             ? new ArrayList<String>() 
             : new ArrayList<String>(Arrays.asList(ipFilters.split("[,\n\r]")));
         this.intervalms = intervalms == null ? 0 : intervalms;
         this.frequency = frequency == null ? 0 : frequency;
         this.groups = groups == null ? new ArrayList<String>() : groups;
         this.endpoints = endpoints == null ? new ArrayList<Integer>() :  endpoints;
         last_request = 0L;
         counter = 0.0;
        if (intervalms > 0 ) {
             decayRate = - Math.log(2) /  intervalms;
         }
     }
 
     public String getName() {
         return name;
     }
 
     public List<String> getIpFilters() {
         return Collections.unmodifiableList(ipFilters);
     }
     public void addIpFilter(String ipFilter) {
         ipFilters.add(ipFilter);
     }
     public void removeIpFilter(String ipFilter) {
         ipFilters.remove(ipFilter);
     }
 
     public List<String> getGroups() {
         return Collections.unmodifiableList(groups);
     }
     public void addGroup(String group) {
         groups.add(group);
     }
     public void removeGroup(String group) {
         groups.remove(group);
     }
 
 
     public Long getIntervalMs() {
         return intervalms;
     }
     public void setIntervalMs(Long intervalms) {
         this.intervalms = intervalms;
     }
 
     public Integer getFrequency() {
         return frequency;
     }
     public void setFrequency(Integer frequency) {
         this.frequency = frequency;
     }
 
     public void setEndpoints(List<Integer> endpoints) {
         this.endpoints = endpoints;
     }
 
     public List<Integer> getEndpoints() {
         return endpoints;
     }
 
     @JsonIgnore
     public synchronized boolean hasEndpoint(Integer ID) {
         if (endpoints.size()== 0) {
             return true;
         }
         for(Integer endpointID:endpoints) {
             if (ID.equals(endpointID)) {
                 return true;
             }
         }
         return false;
     }
 
     @JsonIgnore
     public synchronized boolean hasEitherGroup(String[] groups) {
 
         if (groups.length == 0) {
             return true;
         } else for(String gC:groups) {
             if (groups.length ==0) {
                 return true;
             } else for(String gE:groups) {
                 if (gE.equals(gC)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     @JsonIgnore
     public Long consume()  {
         double waited = 0;
         if (frequency>0 && intervalms > 0) while (true) {
             float sleep = 0;
             synchronized(this) {
                 double elapsed = (System.currentTimeMillis() - last_request);
                 counter = counter * Math.pow(Math.E, decayRate * elapsed);
                 if (counter > frequency) {
                     sleep = Math.round(Math.log(frequency / counter) / decayRate);
                     if (sleep <0) {
                         throw new IllegalArgumentException("Exponential decay resulted in negative waiting time.");
                     }
                 } else {
                     break;
                 }
             }
             synchronized(lock) {
                 double millis = Math.floor(sleep);
                 int nanos = (int) Math.round((sleep-millis) * 1000000);
                 try {
                     lock.wait(Math.round(millis), nanos);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
                 waited += sleep;
             }
         }
         counter+=1.0;
         last_request = System.currentTimeMillis(); 
         return Math.round(waited);
     }
 }
