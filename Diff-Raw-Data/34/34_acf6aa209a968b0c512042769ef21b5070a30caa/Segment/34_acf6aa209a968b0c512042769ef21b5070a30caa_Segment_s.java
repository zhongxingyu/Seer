 package net.madz.download.service.metadata;
 
public class Segment {
     
     private int id;
 
     private long startBytes;
     
     private long endBytes;
     
     private long currentBytes;
     
     private Byte state;
 
     
     public int getId() {
         return id;
     }
 
 
     
     public void setId(int id) {
         this.id = id;
     }
 
 
     public long getStartBytes() {
         return startBytes;
     }
 
     
     public void setStartBytes(long startBytes) {
         this.startBytes = startBytes;
     }
 
     
     public long getEndBytes() {
         return endBytes;
     }
 
     
     public void setEndBytes(long endBytes) {
         this.endBytes = endBytes;
     }
 
     
     public long getCurrentBytes() {
         return currentBytes;
     }
 
     
     public void setCurrentBytes(long currentBytes) {
         this.currentBytes = currentBytes;
     }
 
     
     public Byte getState() {
         return state;
     }
 
     
     public void setState(Byte state) {
         this.state = state;
     }
     
     
 }
