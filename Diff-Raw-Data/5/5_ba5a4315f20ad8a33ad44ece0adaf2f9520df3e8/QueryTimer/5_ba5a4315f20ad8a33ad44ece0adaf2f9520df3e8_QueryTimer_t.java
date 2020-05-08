 package org.ut.biolab;
 
 /**
  * Measurer of query times (nanoseconds).
  * 
  * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
  * 
  */
 public class QueryTimer {
     private long start = 0;
     private long end = 0;
 
    public void start() {
         start = System.nanoTime();
     }
 
    public void finish() {
         end = System.nanoTime();
     }
 
     public long getStart() {
         return start;
     }
 
     public long getEnd() {
         return end;
     }
 
     public long getDuration() {
         return end - start;
     }
 
 }
