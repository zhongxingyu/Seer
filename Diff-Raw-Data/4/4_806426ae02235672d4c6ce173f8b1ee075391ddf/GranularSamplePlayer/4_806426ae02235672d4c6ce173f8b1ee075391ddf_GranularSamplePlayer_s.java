 /*
  * This file is part of Beads. See http://www.beadsproject.net for all information.
  */
 package net.beadsproject.beads.ugens;
 
 import java.util.ArrayList;
 import net.beadsproject.beads.core.AudioContext;
 import net.beadsproject.beads.core.UGen;
 import net.beadsproject.beads.data.Buffer;
 import net.beadsproject.beads.data.Sample;
 import net.beadsproject.beads.data.SampleManager;
 import net.beadsproject.beads.data.buffers.HanningWindow;
 
 /**
  * GranularSamplePlayer plays back a {@link Sample} using granular synthesis. GranularSamplePlayer inherits its main behaviour from {@link SamplePlayer} but replaces the direct {@link Sample} lookup with a granular process. 
  * {@link UGen}s can be used to control playback rate, pitch, loop points, grain size, grain interval, grain randomness and position (this last case assumes that the playback rate is zero). 
  * 
  * @see SamplePlayer Sample
  * 
  * @author ollie
  */
 public class GranularSamplePlayer extends SamplePlayer {
 
 	/** The pitch envelope. */
 	private UGen pitchEnvelope;
 
 	/** The grain interval envelope. */
 	private UGen grainIntervalEnvelope;
 
 	/** The grain size envelope. */
 	private UGen grainSizeEnvelope;
 
 	/** The randomness envelope. */
 	private UGen randomnessEnvelope;
 
 	/** The time in milliseconds since the last grain was activated. */
 	private float timeSinceLastGrain;
 
 	/** The length of one sample in milliseconds. */
 	private double msPerSample;
 
 	/** The pitch, bound to the pitch envelope. */
 	protected float pitch;
 
 	/** The list of current grains. */
 	private ArrayList<Grain> grains;
 
 	/** A list of free grains. */
 	private ArrayList<Grain> freeGrains;
 
 	/** A list of dead grains. */
 	private ArrayList<Grain> deadGrains;
 
 	/** The window used by grains. */
 	private Buffer window;
 
 	/** The current frame. */
 	private float[] frame;
 
 	/** Flag to determine whether, looping occurs within individual grains. */
 	private boolean loopInsideGrains;
 
 	/**
 	 * The nested class Grain. Stores information about the start time, current position, age, and grain size of the grain.
 	 */
 	private static class Grain {
 
 		/** Flag to indicate whether the grain is free. */
 		boolean free;
 
 		/** The start time in milliseconds. */
 		double startTime;
 
 		/** The position in millseconds. */
 		double position;
 
 		/** The age of the grain in milliseconds. */
 		double age;
 
 		/** The grain size of the grain. Fixed at instantiation. */
 		double grainSize;
 	}
 
 	/**
 	 * Instantiates a new GranularSamplePlayer.
 	 * 
 	 * @param context the AudioContext.
 	 * @param outs the number of outputs.
 	 */
 	public GranularSamplePlayer(AudioContext context, int outs) {
 		super(context, outs);
 		grains = new ArrayList<Grain>();
 		freeGrains = new ArrayList<Grain>();
 		deadGrains = new ArrayList<Grain>();
 		pitchEnvelope = new Static(context, 1f);
 		setGrainIntervalEnvelope(new Static(context, 70.0f));
 		setGrainSizeEnvelope(new Static(context, 100.0f));
 		setRandomnessEnvelope(new Static(context, 0.0f));
 		window = new HanningWindow().getDefault();
 		msPerSample = context.samplesToMs(1f);
 		loopInsideGrains = false;
 	}
 
 	/**
 	 * Instantiates a new GranularSamplePlayer.
 	 * 
 	 * @param context the AudioContext.
 	 * @param buffer the Sample played by the GranularSamplePlayer.
 	 */
 	public GranularSamplePlayer(AudioContext context, Sample buffer) {
 		this(context, buffer.getNumChannels());
 		setBuffer(buffer);
 		loopStartEnvelope = new Static(context, 0.0f);
 		loopEndEnvelope = new Static(context, buffer.getLength());
 	}
 
 	/**
 	 * Gets the pitch envelope.
 	 * 
 	 * @return the pitch envelope.
 	 */
 	public UGen getPitchEnvelope() {
 		return pitchEnvelope;
 	}
 
 	/**
 	 * Sets the pitch envelope.
 	 * 
 	 * @param pitchEnvelope
 	 *            the new pitch envelope.
 	 */
 	public void setPitchEnvelope(UGen pitchEnvelope) {
 		this.pitchEnvelope = pitchEnvelope;
 	}
 
 	/**
 	 * Gets the grain interval envelope.
 	 * 
 	 * @return the grain interval envelope.
 	 */
 	public UGen getGrainIntervalEnvelope() {
 		return grainIntervalEnvelope;
 	}
 
 	/**
 	 * Sets the grain interval envelope.
 	 * 
 	 * @param grainIntervalEnvelope
 	 *            the new grain interval envelope.
 	 */
 	public void setGrainIntervalEnvelope(UGen grainIntervalEnvelope) {
 		this.grainIntervalEnvelope = grainIntervalEnvelope;
 	}
 
 	/**
 	 * Gets the grain size envelope.
 	 * 
 	 * @return the grain size envelope.
 	 */
 	public UGen getGrainSizeEnvelope() {
 		return grainSizeEnvelope;
 	}
 
 	/**
 	 * Sets the grain size envelope.
 	 * 
 	 * @param grainSizeEnvelope the new grain size envelope.
 	 */
 	public void setGrainSizeEnvelope(UGen grainSizeEnvelope) {
 		this.grainSizeEnvelope = grainSizeEnvelope;
 	}
 
 	/**
 	 * Gets the randomness envelope.
 	 * 
 	 * @return the randomness envelope.
 	 */
 	public UGen getRandomnessEnvelope() {
 		return randomnessEnvelope;
 	}
 
 	/**
 	 * Sets the randomness envelope.
 	 * 
 	 * @param randomnessEnvelope the new randomness envelope.
 	 */
 	public void setRandomnessEnvelope(UGen randomnessEnvelope) {
 		this.randomnessEnvelope = randomnessEnvelope;
 	}
 	
 	public synchronized void setBuffer(Sample buffer) {
 		super.setBuffer(buffer);
 		grains.clear();
 		timeSinceLastGrain = 0f;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.olliebown.beads.core.UGen#start()
 	 */
 	@Override
 	public void start() {
 		super.start();
 		timeSinceLastGrain = 0;
 	}
 
 	/**
 	 * Sets the given Grain to start immediately.
 	 * 
 	 * @param g
 	 *            the g
 	 * @param time
 	 *            the time
 	 */
 	private void resetGrain(Grain g, int time) {
 		g.startTime = (float)position + (grainSizeEnvelope.getValue(0, time) * randomnessEnvelope.getValue(0, time) * (float)(Math.random() * 2.0f - 1.0f));
 		g.position = g.startTime;
 		g.age = 0f;
 		g.grainSize = grainSizeEnvelope.getValue(0, time);
 	}   
 
 	/** Flag to indicate special case for the first grain. */
 	private boolean firstGrain = true;
 
 	/** Special case method for playing first grain. */
 	private void firstGrain() {
 		if(firstGrain) {
 			Grain g = new Grain();
 			g.startTime = -position / 2f;
 			g.position = position;
 			g.age = grainSizeEnvelope.getValue() / 2f;
 			grains.add(g);
 			firstGrain = false;
 			timeSinceLastGrain = grainIntervalEnvelope.getValue() / 2f;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.olliebown.beads.ugens.SamplePlayer#calculateBuffer()
 	 */
 	@Override
 	public synchronized void calculateBuffer() {
 		//special condition for first grain
 		//update the various envelopes
 		if(buffer != null) {
 			rateEnvelope.update();
			positionEnvelope.update();
 			loopStartEnvelope.update();
 			loopEndEnvelope.update();
 			pitchEnvelope.update();
 			grainIntervalEnvelope.update();
 			grainSizeEnvelope.update();
 			randomnessEnvelope.update();
 			firstGrain();
 			//now loop through the buffer
 			for (int i = 0; i < bufferSize; i++) {
 				//determine if we need a new grain
 				if (timeSinceLastGrain > grainIntervalEnvelope.getValue(0, i)) {
 					if(freeGrains.size() > 0) {
 						Grain g = freeGrains.get(0);
 						freeGrains.remove(0);
 						resetGrain(g, i);
 						grains.add(g);
 					} else {
 						Grain g = new Grain();
 						resetGrain(g, i);
 						grains.add(g);
 					}
 					timeSinceLastGrain = 0f;
 				}
 				//for each channel, start by resetting current output frame
 				for (int j = 0; j < outs; j++) {
 					bufOut[j][i] = 0.0f;
 				}
 				//gather the output from each grain
 				for(int gi = 0; gi < grains.size(); gi++) {
 					Grain g = grains.get(gi);
 					//calculate value of grain window
 					float windowScale = window.getValueFraction((float)(g.age / g.grainSize));
 					//get position in sample for this grain
 					double samplePosition = buffer.msToSamples((float)g.position);		//TODO doubles/floats sort out the mess
 					int currentSample = (int)samplePosition;
 					float fractionOffset = (float)(samplePosition - currentSample);
 					//get the frame for this grain
 					switch (interpolationType) {
 					case LINEAR:
 						frame = buffer.getFrameLinear(currentSample, fractionOffset);
 						break;
 					case CUBIC:
 						frame = buffer.getFrameCubic(currentSample, fractionOffset);
 						break;
 					}
 					//add it to the current output frame
 					for (int j = 0; j < outs; j++) {
 						bufOut[j][i] += windowScale * frame[j % buffer.getNumChannels()];
 					}
 				}
 				//increment time and stuff
 				calculateNextPosition(i);
 				pitch = Math.abs(pitchEnvelope.getValue(0, i));
 				for(int gi = 0; gi < grains.size(); gi++) {
 					Grain g = grains.get(gi);
 					calculateNextGrainPosition(g);
 				}
 				if (isPaused()) {
 					//make sure to zero the remaining outs
 					while(i < bufferSize) {
 						for (int j = 0; j < outs; j++) {
 							bufOut[j][i] = 0.0f;
 						}
 						i++;
 					}
 					break;
 				}
 				//increment timeSinceLastGrain
 				timeSinceLastGrain += msPerSample;
 				//finally, see if any grains are dead
 				for(int gi = 0; gi < grains.size(); gi++) {
 					Grain g = grains.get(gi);
 					if(g.age > g.grainSize) {
 						freeGrains.add(g);
 						deadGrains.add(g);
 					}
 				}
 				for(int gi = 0; gi < deadGrains.size(); gi++) {
 					Grain g = deadGrains.get(gi);
 					grains.remove(g);
 				}
 				deadGrains.clear();
 			}
 		}
 	}
 
 	/**
 	 * Calculate next position for the given Grain.
 	 * 
 	 * @param g the Grain.
 	 */
 	private void calculateNextGrainPosition(Grain g) {
 		int direction = rate > 0 ? 1 : -1;
 		g.age += msPerSample;
 		if(loopInsideGrains) {
 			switch(loopType) {
 			case NO_LOOP_FORWARDS:
 				g.position += direction * positionIncrement * pitch;
 				break;
 			case NO_LOOP_BACKWARDS:
 				g.position -= direction * positionIncrement * pitch;
 				break;
 			case LOOP_FORWARDS:
 				g.position += direction * positionIncrement * pitch;
 				if(rate > 0 && g.position > Math.max(loopStart, loopEnd)) {
 					g.position = Math.min(loopStart, loopEnd);
 				} else if(rate < 0 && g.position < Math.min(loopStart, loopEnd)) {
 					g.position = Math.max(loopStart, loopEnd);
 				}
 				break;
 			case LOOP_BACKWARDS:
 				g.position -= direction * positionIncrement * pitch;
 				if(rate > 0 && g.position < Math.min(loopStart, loopEnd)) {
 					g.position = Math.max(loopStart, loopEnd);
 				} else if(rate < 0 && g.position > Math.max(loopStart, loopEnd)) {
 					g.position = Math.min(loopStart, loopEnd);
 				}
 				break;
 			case LOOP_ALTERNATING:
 				g.position += direction * (forwards ? positionIncrement * pitch : -positionIncrement * pitch);
 				if(forwards ^ (rate < 0)) {
 					if(g.position > Math.max(loopStart, loopEnd)) {
 						g.position = 2 * Math.max(loopStart, loopEnd) - g.position;
 					}
 				} else if(g.position < Math.min(loopStart, loopEnd)) {
 					g.position = 2 * Math.min(loopStart, loopEnd) - g.position;
 				}
 				break;
 			}   
 		} else {
 			g.position += direction * positionIncrement * pitch;
 		}
 	}
 
 	/**
 	 * Calculates the average number of Grains given the current grain size and grain interval.
 	 * @return the average number of Grains.
 	 */
 	public float getAverageNumberOfGrains() {
 		return grainSizeEnvelope.getValue() / grainIntervalEnvelope.getValue();
 	}
 
 }
