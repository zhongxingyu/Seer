 package strategies;
 
 import static robot.Platform.ENGINE;
 import static robot.Platform.HEAD;
 
 import java.util.Arrays;
 
 import sensors.Head;
 import utils.Utils;
 
 /**
  * A strategy to calibrate the light sensor.
  * 
  * Concept: The strategy samples measurments during a fixed time while moving.
  * The largest and the smalles samples will be dropped, a portion of the largest/
  * smalles remaining samples will be used to calculate a mean value of the
  * black-point and white-point.
  * 
  * @author markus
  */
 public class LightCalibrationStrategy extends Strategy {
 
     /** Time to sample light sensor. */
 	private static final int SEARCH_TIME = 2 * 1000;
 	/** Number of samples to collect. */
 	private static final int SAMPLE_SIZE = 200;
 	/** Number of smallest samples to ignore. */
 	private static final int DROPPED_DARK_SAMPLES = 15;
 	/** Number of largest samples to ignore. */
 	private static final int DROPPED_LIGHT_SAMPLES = 5;
     /**
      * Number of largest/smallest remaining samples to use for black-/whitePoint
      * calculation.
      */
 	private static final int ACCEPTED_SAMPLES = 20;
 	/** Time per sample. */
 	private static final int SAMPLE_TIME = SEARCH_TIME / SAMPLE_SIZE;
 	
 	/** Calibration mode */
 	private Mode mode = new ModeDrive();
 	/** Current calibration state */
 	private State state = State.SAMPLE;
 	/** Interval start timestamp */
 	private int startTime = 0;
 	/** The number of currently collected samples */
 	private int sampleCount = 0;
     /** Collected samples */
 	private int samples[] = new int[SAMPLE_SIZE];
 
 	protected void doInit() {
 	    HEAD.setFloodlight(true);
 		// Switch off calibration
 		HEAD.resetLightCalibration();
 		
 		startTime = Utils.getSystemTime();
 		sampleCount = 0;
 		
 		state = State.SAMPLE;
 		mode.reset();
 		mode.update(State.INIT, State.SAMPLE);
 	}
 
 	protected void doRun() {	    
 	    if (elapsedTime() > SAMPLE_TIME) {
 	        resetTime();
 	        
 	        final int sampleValue = HEAD.getRawLightValue();
 	        final State oldState = state;
 	        
 	        switch (state) {
 	            case SAMPLE:
 	                if (sampleCount < SAMPLE_SIZE) {
 	                    samples[sampleCount++] = sampleValue;
 	                }
 	                
 	                if (sampleCount == SAMPLE_SIZE) {	                    
 	                    processData();
 	                    
 	                    // Start driving back
 	                    state = State.DRIVE_BACK;
 	                }
 	                break;
 	            case DRIVE_BACK:
 	                if (mode.finished()) {
 	                    state = State.DONE;
 	                    
 	                    setFinished();
 	                }
 	                break;
 	        }
 	        
 	        mode.update(oldState, state);
 	    }
 	}
 	
 	private void processData() {
 	    Arrays.sort(samples);
         
         /*
         for (int sample : samples) {
             System.out.println(sample);
         }
         */
 
         int blackPoint = 0;
         int whitePoint = 0;
         
         for (int i = DROPPED_DARK_SAMPLES; i < DROPPED_DARK_SAMPLES + ACCEPTED_SAMPLES; i++) {
             blackPoint += samples[i];
         }
         for (int i = DROPPED_LIGHT_SAMPLES; i < DROPPED_LIGHT_SAMPLES + ACCEPTED_SAMPLES; i++) {
             whitePoint += samples[SAMPLE_SIZE - i - 1];
         }
         
         blackPoint /= ACCEPTED_SAMPLES;
         whitePoint /= ACCEPTED_SAMPLES;
         
         HEAD.calibrateLight(blackPoint, whitePoint);
         
         /*
         System.out.println("Min: " + samples[0] + " Max: "
                 + samples[SAMPLE_SIZE - 1] + " bp: " + blackPoint
                 + " wp: " + whitePoint);
         */
 	}
 	
 	/**
 	 * Reset interval timer.
 	 */
 	private void resetTime() {
 	    startTime = Utils.getSystemTime();
 	}
 	
 	/**
 	 * Get the elapsed time since last interval timer reset.
 	 * 
 	 * @return the elapsed time in ms
 	 */
 	private int elapsedTime() {
 	    return Utils.getSystemTime() - startTime;
 	}
 	
 	/**
 	 * Returns the currently used calibration mode.
 	 * 
 	 * @return the calibration mode in use
 	 */
 	public Mode getMode() {
         return mode;
     }
 	
 	/**
 	 * Sets the calibration mode. {@link #init()} must be called after this
 	 * method.
 	 * 
 	 * @param mode the new calibration mode to use
 	 */
 	public void setMode(final Mode mode) {
         this.mode = mode;
     }
 	
 	static abstract class Mode {
 	    abstract void reset();
 	    abstract void update(final State oldState, final State newState);
 	    abstract boolean finished();
 	}
 	
     public static final class ModeDrive extends Mode {
         /** Driving speed while sampling light sensor. */
         private static final int BASE_SPEED = 50;
         /** Driving speed while moving back after sampling. */
         private static final int DRIVE_BACK_SPEED = 75;
         /** Desired distance/position after the calibration. */
         private static final float TARGET_DISTANCE = (float) -30.0;
         
         private float distance = (float) 0.0;
         
         @Override
         void reset() {
             distance = (float) 0.0;
         }
         
         @Override
         void update(final State oldState, final State newState) {
             distance += ENGINE.estimateDistance();
             
             if (oldState == State.INIT && newState == State.SAMPLE) {
                 ENGINE.move(BASE_SPEED);
             } else if (oldState == State.SAMPLE && newState == State.DRIVE_BACK) {
                 ENGINE.move(-DRIVE_BACK_SPEED);
             } else if (oldState == State.DRIVE_BACK && newState == State.DONE) {
                 ENGINE.stop();
             }
         }
         
         @Override
         boolean finished() {
             return distance < TARGET_DISTANCE;
         }
     }
 	
 	
 	public static final class ModeSweep extends Mode {
 	    /** Angle used during sweeping calibration. */
 	    private static final int SWEEP_ANGLE = 30;
 	    /** Sweep speed used for sweeping calibration. */
 	    private static final int SWEEP_SPEED = 100;
 	    
 	    private int sweepTo = -Head.degreeToPosition(SWEEP_ANGLE);
 	    
 	    @Override
 	    void reset() {
 	        sweepTo = -Head.degreeToPosition(SWEEP_ANGLE);
 	    }
 	    
         @Override
         void update(final State oldState, final State newState) {
             switch (newState) {
                 case INIT:
                    HEAD.moveTo(sweepTo, SWEEP_SPEED);
                     sweepTo *= -1;
                     break;
                 case SAMPLE:
                     if (!HEAD.isMoving()) {
                        HEAD.moveTo(sweepTo, SWEEP_SPEED);
                         sweepTo *= -1;
                     }
                     break;
                 case DRIVE_BACK:
                     if (oldState == State.SAMPLE) {
                        HEAD.moveTo(0, 1000);
                     }
                     break;
             }
         }
         
         @Override
         boolean finished() {
             return !HEAD.isMoving();
         }
 	}
 	
 	private static enum State {
 	    INIT,
 	    SAMPLE,
 	    DRIVE_BACK,
 	    DONE
 	}
 }
