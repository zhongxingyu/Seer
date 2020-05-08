 package net.beadsproject.beads.ugens;
 
 import net.beadsproject.beads.core.AudioContext;
 import net.beadsproject.beads.data.Buffer;
 
 public class UsingBiquadFilter {
 	
 	public static class Basic {
 		public static void main(String[] args) {
 			
 			// Create our audio context.
 			AudioContext ac = new AudioContext();
 
 			// Start with some white noise as source material.
 			Noise n = new Noise(ac);
 			
 			// Create a 2-channel band-pass filter with constant peak gain.
 			BiquadFilter bf = new BiquadFilter(ac, 2, BiquadFilter.BP_PEAK);
 			
 			// Set the filter's frequency and Q-value.
 			bf.setFrequency(440).setQ(90);
 			
 			// Add the white noise to the filter's inputs.
 			bf.addInput(n);
 			
 			// Send the result to audio out.
			ac.out.addInput(bf);
 			
 			// Don't forget to start the audio running!
 			ac.start();
 		}
 	}
 
 	
 	public static class Moderate {
 		public static void main(String[] args) {
 			
 			AudioContext ac = new AudioContext();
 
 			// Start with white noise.
 			Noise n = new Noise(ac);
 			
 			// Create a 1-channel low-pass filter.
 			BiquadFilter bf = new BiquadFilter(ac, 2, BiquadFilter.LP);
 			
 			// Create a "wave" by using a slow sine wave.
 			WavePlayer sine = new WavePlayer(ac, .3f, Buffer.SINE);
 			Function freq = new Function(sine) {
 				public float calculate() {
 					return x[0] * 600f + 800f; 
 				}
 			};
 			
 			// Set the filter parameters.
 			bf.setFrequency(freq).setQ(1);
 			
 			// Add the white noise to the filter's inputs.
 			bf.addInput(n);
 			
 			// Send the result to audio out.
			ac.out.addInput(bf);
 			
 			// Don't forget to start the audio running!
 			ac.start();
 		}
 	}
 
 }
