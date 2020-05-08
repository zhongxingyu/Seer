 package de.cgarbs.apsynth.signal.library;
 
 import de.cgarbs.apsynth.signal.Signal;
 
 public class UndersampleClass extends DefaultSignalClass {
 
     /**
      * 1: signal
      * 2: time to keep
      */
 	public UndersampleClass() {
 		this.paramCount = 2;
 	}
 	
 	public Signal instantiate(Signal[] s) {
 		checkParams(s);
         return new Undersample(s[0], s[1]);
 	}
 	
 	public String getName() {
 		return "Undersample";
 	}
 
     public static class Undersample implements Signal {
 
         private Signal signal;
         private Signal step;
         private boolean enveloped;
         private long thisTick, nextTick;
         private double lastStep, lastVal;
         
         public double get(long tick, long local) {
             
             if ( step.get(tick, local) != lastStep ) {
                 lastStep = step.get(tick, local);
             }
             
             if ((tick < thisTick) || (tick >= nextTick)) {
                 long newStep = (long) Math.abs(lastStep);
                 thisTick = (tick / newStep) * newStep;
                 nextTick = thisTick + newStep;
                 lastVal = signal.get(tick, local);
             }
             return lastVal;
         }
 
         /**
          * 1: signal
          * 2: time to keep
          */
         private Undersample(Signal signal, Signal step) {
 
             this.signal = signal;
             this.step   = step;
             this.enveloped = signal.isEnveloped();
             this.thisTick = 0;
             this.nextTick = -1;
             this.lastVal = signal.get(0,0);
             this.lastStep = step.get(0,0);
         }
 
         public boolean isEnveloped() {
             return enveloped;
         }
 
     }
 
 }
