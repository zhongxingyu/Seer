 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package entities;
 
 import data.structs.TimeRepresentation;
 import data.types.DataValue;
 import data.types.Int32;
 import data.types.UInt32;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import timestamp.TimeStamp;
 
 /**
  * Tiene la responsabilidad de generar la 'hora' local, aceptando modificaciones
  * a su offset de tiempo y posiblemente a la rapidez (oscilación) del mismo
  * @author Marcel
  */
 public class LocalClock {
 
     long mNanoOffSet = 0;
     long startTime = 0;
     TimeStamp tStamp;
     private boolean useTicks;
 
     public LocalClock(boolean useTicks) {

         startTime = System.currentTimeMillis();
         this.useTicks = useTicks;
         if (useTicks) {
            tStamp = new TimeStamp();
             tStamp.Start();
         }
     }
 
     /**
      * Sets the clock's offset in nanoseconds
      * @param nanos amount of nanoseconds to set off 
      */
     public void setOffset(long nanos) {
         this.mNanoOffSet = nanos;
     }
 
     /**
      * Gets the offset of this clock in nanoseconds
      * @return the amount of nano seconds the clock is offset
      */
     public long getOffset() {
         return this.mNanoOffSet;
     }
 
     public long getTimeNanos() {
         long time = System.currentTimeMillis() * 1000000L;
         if (this.useTicks) {
             time = ((this.startTime + tStamp.GetElapsedMillis()) * 1000000L) + this.mNanoOffSet;
         }
 
         return time;
     }
 
     public TimeRepresentation getTime() {
         long currTime = this.getTimeNanos();
         TimeRepresentation time = new TimeRepresentation(currTime);
 
         return time;
     }
 
     /**
      * Gets the accuracy in nanoseconds
      * @return nanoseconds of accuracy
      */
     public static long GetAccuracy() {
         try {
             return (1000000000L) / TimeStamp.GetFrequency();
        } catch (NoClassDefFoundError | java.lang.UnsatisfiedLinkError e) {
             Logger.getGlobal().log(Level.WARNING, "Se asume que si no se tiene TimeStamp"
                     + "es porque estamos en el servidor y no se ocupa.");
             // 1000 milisegundos de precisión
             return 1000000L;
         }
     }
 
     public boolean IsHighResolution() {
         return this.useTicks;
     }
 }
