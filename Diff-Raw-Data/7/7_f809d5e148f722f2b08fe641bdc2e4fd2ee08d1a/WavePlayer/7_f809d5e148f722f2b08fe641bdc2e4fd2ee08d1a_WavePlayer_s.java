 /*
  * This file is part of Beads. See http://www.beadsproject.net for all information.
  */
 package net.beadsproject.beads.ugens;
 
 import net.beadsproject.beads.core.AudioContext;
 import net.beadsproject.beads.core.UGen;
 import net.beadsproject.beads.data.Buffer;
 import net.beadsproject.beads.data.buffers.SawBuffer;
 import net.beadsproject.beads.data.buffers.SineBuffer;
 import net.beadsproject.beads.data.buffers.SquareBuffer;
 
 /**
  * WavePlayer iterates over wave data stored in a {@link Buffer}. The frequency of the WavePlayer is controlled by a {@link @UGen}, meaning that WavePlayers can easily be combined to perform FM synthesis or ring modulation. 
  *
  * @see Buffer {@link SineBuffer} {@link SawBuffer} {@link SquareBuffer}
  * 
  * @author ollie
  */
 public class WavePlayer extends UGen {
 
     /** The playback point in the Buffer, expressed as a fraction. */
     private double point;
     
     /** The frequency envelope. */
     private UGen frequencyEnvelope;
     
     /** The phase envelope. */
     private UGen phaseEnvelope;
     
     /** The Buffer. */
     private Buffer buffer;
     
     /**
 	 * Instantiates a new WavePlayer with given frequency envelope and Buffer.
 	 * 
 	 * @param context
 	 *            the AudioContext.
 	 * @param frequencyEnvelope
 	 *            the frequency envelope.
 	 * @param buffer
 	 *            the Buffer.
 	 */
     public WavePlayer(AudioContext context, UGen frequencyEnvelope, Buffer buffer) {
         super(context, 1);
         this.frequencyEnvelope = frequencyEnvelope;
         this.buffer = buffer;
     }
     
     /**
 	 * Instantiates a new WavePlayer with given static frequency and Buffer.
 	 * 
 	 * @param context
 	 *            the AudioContext.
 	 * @param frequency
 	 *            the frequency in Hz.
 	 * @param buffer
 	 *            the Buffer.
 	 */
     public WavePlayer(AudioContext context, float frequency, Buffer buffer) {
         super(context, 1, 1);
        frequencyEnvelope = new Envelope(context);
        ((Envelope)frequencyEnvelope).setValue(frequency);
         this.buffer = buffer;
     }
     
     /* (non-Javadoc)
      * @see com.olliebown.beads.core.UGen#start()
      */
     public void start() {
         super.start();
         point = 0;
     }
     
     /* (non-Javadoc)
      * @see com.olliebown.beads.core.UGen#calculateBuffer()
      */
     @Override
     public void calculateBuffer() {
     	frequencyEnvelope.update();
    	phaseEnvelope.update();
         float sr = context.getSampleRate();
         if(phaseEnvelope == null) {
 	        for(int i = 0; i < bufferSize; i++) {
 	            float frequency = Math.abs(frequencyEnvelope.getValue(0, i));
 	            point += (frequency / sr);
 	            point = point % 1.0f;
 	            bufOut[0][i] = buffer.getValueFraction((float)point);
 	        }
         } else {
         	for(int i = 0; i < bufferSize; i++) {
         		bufOut[0][i] = buffer.getValueFraction(phaseEnvelope.getValue(0, i));
         	}
         }
     }
 
 	/**
 	 * Gets the frequency envelope.
 	 * 
 	 * @return the frequency envelope.
 	 */
 	public UGen getFrequencyEnvelope() {
 		return frequencyEnvelope;
 	}
 
 	/**
 	 * Sets the frequency envelope. Note, if the phase envelope is not null, the frequency envelope will have no effect.
 	 * 
 	 * @param frequencyEnvelope
 	 *            the new frequency envelope.
 	 */
 	public void setFrequencyEnvelope(UGen frequencyEnvelope) {
 		this.frequencyEnvelope = frequencyEnvelope;
 	}
 
 	/**
 	 * Gets the phase envelope.
 	 * 
 	 * @return the phase envelope.
 	 */
 	public UGen getPhaseEnvelope() {
 		return phaseEnvelope;
 	}
 
 	/**
 	 * Sets the phase envelope.
 	 * 
 	 * @param phaseEnvelope
 	 *            the new phase envelope.
 	 */
 	public void setPhaseEnvelope(UGen phaseEnvelope) {
 		this.phaseEnvelope = phaseEnvelope;
 	}
 	
 	/**
 	 * Sets the Buffer.
 	 * 
 	 * @param b the new Buffer.
 	 */
 	public void setBuffer(Buffer b) {
 		this.buffer = b;
 	}
 	
 }
