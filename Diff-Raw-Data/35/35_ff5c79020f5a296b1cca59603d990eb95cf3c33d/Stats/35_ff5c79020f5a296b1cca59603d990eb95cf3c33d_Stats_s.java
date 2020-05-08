 package net.pixelcop.monstrous;
 
 public class Stats {
 
     private long successCount;
     private long errorCount;
     
     public Stats() {
         successCount = 0L;
         errorCount = 0L;
     }
     
     public void incrSuccessCount() {
         successCount++;
     }
     
     public void incrSuccessCount(long amount) {
         successCount = successCount + amount;
     }
     
     public void incrErrorCount() {
         errorCount++;
     }
     
     public void incrErrorCount(long amount) {
         errorCount = errorCount + amount;
     }
     
     /**
      * Add totals from another stats object to this one
      * 
      * @param stats
      */
     public void add(Stats stats) {
         incrSuccessCount(stats.getSuccessCount());
         incrErrorCount(stats.getErrorCount());
     }
     
     public void print(long startTime) {
         
         long elapse = System.currentTimeMillis() - startTime;
         float throughput = ((float) Math.round(((float)getSuccessCount() / ((float)elapse/1000)) * 100F)) / 100F;
         
         System.out.println("Elapsed time: " + elapse + "ms");
         System.out.println("Throughput: " + throughput + " req/s");
         
         System.out.println("Success: " + getSuccessCount());
         System.out.println("Error: " + getErrorCount());
         
     }
 
     public long getSuccessCount() {
         return successCount;
     }
     
     public void setSuccessCount(long successCount) {
         this.successCount = successCount;
     }
 
     public long getErrorCount() {
         return errorCount;
     }
     
     public void setErrorCount(long errorCount) {
         this.errorCount = errorCount;
     }
 
 }
