 package net.paguo.trafshow.backend.snmp.summary.model;
 
 import java.util.Date;
 
 /**
  * @author Reyentenko
  */
 public class RouterSummaryTraffic {
     private String router;
 
     private String iface;
 
     private Long totalInput = 0l;
 
     private Long totalOutput = 0l;
 
     private Date date;
 
     private TrafficRecord lastProcessed;
 
     public RouterSummaryTraffic(){
 
     }
 
     public void processRecord(TrafficRecord record) {
         if (lastProcessed != null) {
             // Check for router restart
             if (record.getUptime() >= lastProcessed.getUptime()){
                totalInput += findDifference(lastProcessed.getInput(), record.getInput());
                totalOutput += findDifference(lastProcessed.getOutput(), record.getOutput());
             }else{
                 totalInput += record.getInput();
                 totalOutput += record.getOutput();
             }
         }else{
            totalInput += record.getInput();
            totalOutput += record.getOutput();
         }
         this.lastProcessed = record;
     }
 
     private Long findDifference(Long input, Long input1) {
         return input1 >= input ?
                 input1 - input :
                 ((long) Math.pow(2, 32) - input) + input1;
     }
 
     /**
      * Test-only method.
      * @param lastProcessed last processed record
      */
     void setLastProcessed(TrafficRecord lastProcessed) {
         this.lastProcessed = lastProcessed;
     }
 
     public String getRouter() {
         return router;
     }
 
     public void setRouter(String router) {
         this.router = router;
     }
 
     public String getIface() {
         return iface;
     }
 
     public void setIface(String iface) {
         this.iface = iface;
     }
 
     public Long getTotalInput() {
         return totalInput;
     }
 
     public void setTotalInput(Long totalInput) {
         this.totalInput = totalInput;
     }
 
     public Long getTotalOutput() {
         return totalOutput;
     }
 
     public void setTotalOutput(Long totalOutput) {
         this.totalOutput = totalOutput;
     }
 
     public Date getDate() {
         return date;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 }
