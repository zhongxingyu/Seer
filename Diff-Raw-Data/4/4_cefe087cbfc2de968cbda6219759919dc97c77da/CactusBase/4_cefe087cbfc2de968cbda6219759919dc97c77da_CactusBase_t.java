 package api;
 
 import com.ridgesoft.intellibrain.IntelliBrain;
 import com.ridgesoft.io.DisplayOutputStream;
 import com.ridgesoft.io.Speaker;
 import com.ridgesoft.robotics.AnalogInput;
 import com.ridgesoft.robotics.DirectionListener;
 import com.ridgesoft.robotics.RangeFinder;
 import com.ridgesoft.robotics.Servo;
 import com.ridgesoft.robotics.sensors.SharpGP2D12;
 
 /**
  * The base abstraction for all code to be built onto Cactus the robot.
  * @author Jacob Van Buren
  * @version 2.0.0
  * @since 2.0.0
  */
 public abstract class CactusBase {
     /** The port number of {@code leftWheelInput}. */
     public static final int LEFT_WHEEL_INPUT_PORT = 4;
     /** The port number of {@code rightWheelInput}. */
     public static final int RIGHT_WHEEL_INPUT_PORT = 5;
     /** The port number of {@code leftIR}. */
     public static final int LEFT_IR_PORT = 1;
     /** The port number of {@code rightIR}. */
     public static final int RIGHT_IR_PORT = 2;
     /** The value to return when a sensor reading fails */
     public static final float SENSOR_FAILURE = Float.POSITIVE_INFINITY;
 
     /** The Infrared proximity sensor that tracks Cactus' left wheel. */
     public static final AnalogInput leftWheelInput
             = IntelliBrain.getAnalogInput(LEFT_WHEEL_INPUT_PORT);
 
     /** The Infrared proximity sensor that tracks Cactus' right wheel. */
     public static final AnalogInput rightWheelInput
             = IntelliBrain.getAnalogInput(RIGHT_WHEEL_INPUT_PORT);
 
     /** The display screen on the top of Cactus. */
     public static final DisplayOutputStream display
             = new DisplayOutputStream(IntelliBrain.getLcdDisplay());
 
     /** Cactus' left continuous servomotor. */
     public static final Motor leftMotor = new Motor(1, Motor.LEFT);
 
     /** Cactus' right continuous servomotor. */
     public static final Motor rightMotor = new Motor(2, Motor.RIGHT);
 
     /** The Infrared Range Sensor that detects objects to Cactus' left. */
     public static final RangeFinder leftIR
             = new SharpGP2D12(IntelliBrain.getAnalogInput(LEFT_IR_PORT), null);
 
     /** The Infrared Range Sensor that detects objects to Cactus' right. */
     public static final RangeFinder rightIR
             = new SharpGP2D12(IntelliBrain.getAnalogInput(RIGHT_IR_PORT), null);
 
     /** The "Buzzer" on the IntelliBrain PCB. */
     public static final Speaker buzzer = IntelliBrain.getBuzzer();
 
     /**
      * The display to which data from the print methods will go.
      * Defaults to {@code display}.
      */
     static volatile DisplayOutputStream stdout = display;
 
     /** A constructor that disallows instantiation by default. */
     CactusBase() {
         throw new InstantiationError(
                 "Instantiation of a base program should be disallowed");
     }
     
     /**
      * Gets the distance to the nearest object as determined by leftIR, or 
      * {@code SENSOR_FAILURE} if no valid reading can be made.
      * @return The distance (in cm) to the nearest object on the left or 
      * {@code SENSOR_FAILURE} if no valid reading can be made.
      */
     public static float leftCM() {
         leftIR.ping();
         float ret = leftIR.getDistanceCm();
         // SENSOR_FAILURE if failed reading
         return ret == -1 ? SENSOR_FAILURE : ret;
     }
     
     /**
      * Gets the distance to the nearest object as determined by rightIR, or 
      * {@code SENSOR_FAILURE} if no valid reading can be made.
      * @return The distance (in cm) to the nearest object on the right or 
      * {@code SENSOR_FAILURE} if no valid reading can be made.
      */
     public static float rightCM() {
         rightIR.ping();
         float ret = rightIR.getDistanceCm();
         // SENSOR_FAILURE if failed reading
         return ret == -1 ? SENSOR_FAILURE : ret;
     }
 
     /**
      * Plays a sequence of notes.
      * @param notes The frequencies to play
      * @param dur The durations of the notes
      */
     public static void play(int[] notes, int[] dur) {
         if (notes.length != dur.length)
             throw new IllegalArgumentException(
                     "Arrays must be of the same size");
         for (int cactus = 0; cactus < notes.length; ++cactus)
             buzzer.play(notes[cactus], dur[cactus]);
     }
 
     /**
      * Tells the main thread to sleep for a certain duration.
      * Accurate to within a couple milliseconds
      * Will not stop on {@code InterruptedException}.
      * @param milliseconds The number of milliseconds to sleep for.
      */
     public static void sleepFor(long milliseconds) {
         // The time at which to return from this method
         long end = System.currentTimeMillis() + milliseconds;
 
         while (System.currentTimeMillis() < end)
             try {
                 Thread.sleep(end - System.currentTimeMillis());
             } catch (InterruptedException e)
                 { /* Do Nothing. */ }
     }
 
     /**
      * Prints the specified string to {@code stdout} followed by end.
      * @param s The String to print.
      * @param end The Character to append to the end.
      */
     public static void print(String s, Character end) {
         boolean flushBuffer = false;
         try {
             if (s != null) {
                 flushBuffer = true;
                 stdout.write(s.getBytes());
             }
             if (end != null) {
                 flushBuffer = true;
                 stdout.write((int) end.charValue());
             }
         } catch (java.io.IOException ex) {
             throw new RuntimeException(ex.getMessage());
         } finally {
             if (flushBuffer)
                 stdout.flush();
         }
     }
 
     /**
      * Prints the specified string to {@code stdout} followed by a newline.
      * @param s The String to print.
      */
     public static void print(String s) {
         try {
             if (s != null)
                 stdout.write(s.getBytes());
             stdout.write((int) '\n');
         } catch (java.io.IOException ex) {
             throw new RuntimeException(ex.getMessage());
         } finally {
             if (s != null)
                 stdout.flush();
         }
     }
 
     /** Prints a newline to {@code stdout}. */
     public static void print() {
         stdout.write((int) '\n');
         stdout.flush();
     }
 
     /** Prints a welcome message to {@code stdout}. */
     public static void printWelcome() {
         // Greet the user
         print("  Hello World,");
         print("  I am Cactus!");
     }
 
     /**
      * An abstraction of a continuous servomotor.
      * @author Jacob Van Buren
      * @version 1.0.0
      * @since 1.0.0
      */
     public static class Motor {
         /** Indicates the motor is on the left side of Cactus. */
         public static final boolean LEFT = false;
 
         /** Indicates the motor is on the left side of Cactus. */
         public static final boolean RIGHT = true;
 
 
         /**
          * The maximum value that the internal Servo object can take
          * (minimum is zero).
          */
         static final int SERVO_MAX_VALUE = 100;
 
         /**
          * The value halfway in between zero and the maximum value.
          * Represents the value at which the servo is stopped.
          */
         static final int MIDPOINT_VALUE = SERVO_MAX_VALUE / 2;
 
 
         /** The internal Servo object. */
         protected final Servo s;
 
         /** Indicates whether to mirror the direction of this servo. */
         private final boolean invert;
 
         /** The DirectionListener to notify when the direction changes. */
         private DirectionListener directionListener = null;
 
         /** The position that the Servo is currently at. */
         private int currentPosition = MIDPOINT_VALUE;
 
         /**
          * Creates a Motor object from the specified servo.
          * Defaults to the left side.
          * @param servoNumber Which servomotor to use.
          */
         public Motor(int servoNumber) {
             this(servoNumber, Motor.LEFT);
         }
 
         /**
          * Create a Motor object representing a servo on the specified side of
          * Cactus.
          * @param servoNumber Which servomotor to use.
          * @param side The side on which the servo is mounted 
          * (see {@code LEFT} and {@code RIGHT}).
          */
         public Motor(int servoNumber, boolean side) {
             this.s = IntelliBrain.getServo(servoNumber);
             this.invert = side;
 
             // Stop the motor initially
             this.move(MIDPOINT_VALUE);
             s.off();
         }
 
         /**
          * Sets a DirectionLister for this Motor.
          * @param listener The DirectionListener to notify when the
          * Motor's direction changes.
          */
         public void addDirectionListener(DirectionListener listener) {
             this.directionListener = listener;
         }
 
         /** Moves the servo forward at its maximum rotational speed. */
         public void forward() {
             this.move(SERVO_MAX_VALUE);
         }
 
         /**
          * Moves the servo forward at the specified speed.
          * @param percent The speed to spin the motor at (0-100)%.
          */
         public void forward(byte percent) {
             if (percent > 100)
                 throw new IllegalArgumentException(
                         "percent must be <= 100\n(recieved: " + percent + ")");
             if (percent < 0)
                 throw new IllegalArgumentException(
                         "percent must be >= 0\n(recieved: " + percent + ")");
 
             this.move(MIDPOINT_VALUE + MIDPOINT_VALUE * percent / 100);
         }
 
         /** Moves the servo backward at its maximum rotational speed. */
         public void backward() {
             this.move(0);
         }
 
         /**
          * Moves the servo backward at the specified speed.
          * @param percent The speed to spin the motor at (0-100)%.
          */
         public void backward(byte percent) {
             if (percent > 100)
                 throw new IllegalArgumentException(
                         "percent must be <= 100\n(recieved: " + percent + ")");
             if (percent < 0)
                 throw new IllegalArgumentException(
                         "percent must be >=0\n(recieved: " + percent + ")");
 
             this.move(MIDPOINT_VALUE - MIDPOINT_VALUE * percent / 100);
         }
 
         /**
          * Sets the internal Servo object to the specified position, accounting
          * for the physical position of the servo.
          * @param value The value to set the Servo to.
          */
         private synchronized void move(int value) {
             if (value == currentPosition)
                 return;
             if (directionListener != null) {
                 boolean currentDirection = currentPosition >= MIDPOINT_VALUE;
                 boolean newDirection = value >= MIDPOINT_VALUE;
  
                 if (currentDirection != newDirection)
                     directionListener.updateDirection(true);
                 else if (value == MIDPOINT_VALUE)
                     directionListener.updateDirection(currentDirection);
             }
             currentPosition = value;
             s.setPosition(invert ? SERVO_MAX_VALUE - value : value);
         }
 
        /** Kills power to the Motor (active braking not supported). */
         public void stop() {
             this.move(MIDPOINT_VALUE);
             s.off();
         }
     }
 }
