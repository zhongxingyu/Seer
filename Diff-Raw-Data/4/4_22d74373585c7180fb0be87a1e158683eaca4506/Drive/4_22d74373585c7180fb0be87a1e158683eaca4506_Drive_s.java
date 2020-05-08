 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.rmr662.frc2012.component;
 
 import com.rmr662.frc2012.generic.Component;
 import com.rmr662.frc2012.library.RMRRobotDrive;
 import com.rmr662.frc2012.physical.RMRJaguar;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.PIDController;
 
 /**
  *
  * @author mcoffin
  */
 public class Drive extends Component {
     
     private volatile static Drive instance;
     
     private static final int LEFT = 0;
     private static final int RIGHT = 1;
     
     private static final double DISTANCE_PER_PULSE = 0.000465839;
     
     private static final double KP = 0.3;
     private static final double KI = 0.0;
     private static final double KD = 0.0;
     
    private static final double SPEED_MIN = 0.0;
     private static final double SPEED_MAX = 100d;
     
     private static final int[] MOTOR_CHANNELS = {1, 2};
     private static final int[] ENCODER_CHANNELS_A = {3, 5};
     private static final int[] ENCODER_CHANNELS_B = {4, 6};
     
     private RMRJaguar[] motors = new RMRJaguar[MOTOR_CHANNELS.length];
     private Encoder[] encoders = new Encoder[ENCODER_CHANNELS_A.length];
     private PIDController[] controllers = new PIDController[MOTOR_CHANNELS.length];
     private RMRRobotDrive robotDrive;
     private double[] targetValues = {0d, 0d};
     
     private Drive() {
         for (int i = 0; i < MOTOR_CHANNELS.length; i++) {
             motors[i] = new RMRJaguar(MOTOR_CHANNELS[i]);
             encoders[i] = new Encoder(ENCODER_CHANNELS_A[i], ENCODER_CHANNELS_B[i]);
             encoders[i].setDistancePerPulse(DISTANCE_PER_PULSE);
             encoders[i].start();
             encoders[i].setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
             controllers[i] = new PIDController(KP, KI, KD, encoders[i], motors[i]);
             controllers[i].enable();
             controllers[i].setInputRange(SPEED_MIN, SPEED_MAX);
         }
        motors[RIGHT].setInverted(true);
         encoders[LEFT].setReverseDirection(true);
         robotDrive = new RMRRobotDrive(controllers[LEFT], controllers[RIGHT]);
     }
     
     public void update() {
         robotDrive.tankDrive(targetValues[LEFT], targetValues[RIGHT]);
     }
     
     public void reset() {
         for (int i = 0; i < targetValues.length; i++) {
             targetValues[i] = 0d;
         }
     }
     
     public synchronized void setTargetValue(int index, double value) {
         targetValues[index] = value;
     }
     
     public synchronized void setTargetValues(Joystick[] joysticks) {
         for (int i = 0; i < joysticks.length; i++) {
             targetValues[i] = joysticks[i].getY();
         }
     }
     
     public static Drive getInstance() {
         if (instance == null) {
             instance = new Drive();
         }
         return instance;
     }
     
     public String getRMRName() {
         return "Drive";
     }
     
 }
