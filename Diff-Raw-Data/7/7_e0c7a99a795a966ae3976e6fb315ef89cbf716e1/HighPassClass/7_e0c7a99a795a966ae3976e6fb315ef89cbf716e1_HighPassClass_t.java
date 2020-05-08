 package de.cgarbs.apsynth.signal.library;
 
 import de.cgarbs.apsynth.Apsynth;
 import de.cgarbs.apsynth.internal.Pool;
 import de.cgarbs.apsynth.signal.Signal;
 import de.cgarbs.apsynth.signal.library.DataBlockClass.DataBlock;
 import dsp.RemezFIRFilter;
 
 public class HighPassClass extends DefaultSignalClass {
 
 	public HighPassClass() {
 		this.paramCount = 5;
 	}
 	
     /**
      * 0: input signal
      * 1: cutoff frequency [Hz]
      * 2: transition bandwidth [Hz]
      * 3: stopband attenuation [dB]
      * 4: passband ripple [dB]
      */
 	public Signal instantiate(Signal[] s) {
 		checkParams(s);
 
         /* TODO: optimize
         if (s[1].get(0)-s[2].get(0) > Apsynth.samplefreq/2) {
             // cutoff is over nyquist frequency
         }
         */
 		return new HighPass(s[0], s[1], s[2], s[3], s[4]);
 	}
 	
 	public String getName() {
 		return "HighPass";
 	}
 
     public static class HighPass implements Signal {
 
         private Signal filter;
         
         public double get(long t) {
             return filter.get(t);
         }
 
         /**
          * 0: input signal
          * 1: cutoff frequency [Hz]
          * 2: transition bandwidth [Hz]
          * 3: stopband attenuation [dB]
          * 4: passband ripple [dB]
          */
         private HighPass(Signal signal, Signal s_cutoff, Signal s_trband, Signal s_atten, Signal s_ripple) {
 
             // TODO: make filters changeable after initialization (lookout: needs cache)
             
             // taken from RemezFIRFilterDesign.java
             int numBands = 2;
             double[] desired = new double[numBands];
             double[] bands   = new double[2*numBands];
             double[] weights = new double[numBands];
 
             double f1 = s_cutoff.get(0) / Apsynth.samplefreq;
             double trband = s_trband.get(0) / Apsynth.samplefreq;
             
             double atten = s_atten.get(0);
             double ripple = s_ripple.get(0);
             double deltaP = 0.5f*(1.0f - (double)Math.pow(10.0f, -0.05f*ripple));
             double deltaS = (double)Math.pow(10.0f, -0.05f*atten);
             double rippleRatio = deltaP / deltaS;
 
             desired[0] = 0.0;
             desired[1] = 1.0;
             bands[0] = 0.0;
             bands[1] = f1 - trband;
             bands[2] = f1;
             bands[3] = 0.5;
             weights[0] = rippleRatio;
             weights[1] = 1.0;
 
             if (bands[2] > bands[3]) {
                 bands[2] = bands[3];
             }
            if (bands[1] > bands[2]) {
                bands[1] = bands[2];
            }
             
             int numTaps = (int)Math.round((-10.0d*(Math.log(deltaP*deltaS)/Math.log(10.0d)) - 13)/(14.6d*trband));
             
             DataBlock coeff = new DataBlock(
                     new RemezFIRFilter().remez(numTaps, bands, desired, weights, RemezFIRFilter.BANDPASS)
             );
 
             this.filter = Pool.getSignalClass("FiniteImpulseResponse").instantiate(new Signal[]{signal, coeff});
 
         }
         
     }
 }
