 package de.cgarbs.apsynth.signal.library;
 
 import de.cgarbs.apsynth.internal.Pool;
 import de.cgarbs.apsynth.signal.Signal;
 
 public class SampleClass extends DefaultSignalClass {
 
     public SampleClass() {
 		this.paramCount = 0;
 	}
 	
 	public Signal instantiate(Signal[] s) {
 		checkParams(s);
         return new Sample();
 	}
 	
 	public String getName() {
 		return "Sample";
 	}
 
     public static class Sample implements Signal {
 
         private de.cgarbs.apsynth.Sample sample = null;
 
        public double get(long tick) {
 
         	if (sample == null) {
         		return 0;
         	}
        	return sample.get(tick);
         	
         }
 
     	public void setSample(String name) {
     		this.sample = Pool.getSample(name);
     	}
     	
     }
 }
