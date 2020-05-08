 package net.madz.download.service.metadata;
 
import java.io.Serializable;

public class Segment implements Serializable {
     
    private static final long serialVersionUID = -5065005410835889264L;

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
