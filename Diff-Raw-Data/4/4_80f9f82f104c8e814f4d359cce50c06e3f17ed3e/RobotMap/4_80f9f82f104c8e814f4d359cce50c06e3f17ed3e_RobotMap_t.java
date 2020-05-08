 package edu.wpi.first.wpilibj.templates;
 
 /**
  * The RobotMap is a mapping from the ports sensors and actuators are wired into
  * to a variable name. This provides flexibility changing wiring, makes checking
  * the wiring easier and significantly reduces the number of magic numbers
  * floating around.
  */
 public class RobotMap {
 
     // Drive Joystick
     public static final int JOYSTICK_DRIVE = 1;
     public static final int BUTTON_BALANCE = 2;
     public static final int BUTTON_DRIVE = 3;
     public static final int BUTTON_BACKWARDS = 4;
     // Ball Joystick
     public static final int JOYSTICK_BALL = 2;
     public static final int BUTTON_SHOOT = 1;
     public static final int BUTTON_SHOOT_MODE = 2;
     public static final int BUTTON_BALL_HANDLING = 3;
     public static final int BUTTON_ARM = 4;
     public static final int BUTTON_AUTOSHOOT = 5;
     public static final int BUTTON_HOOD_UP = 6;
     public static final int BUTTON_HOOD_DOWN = 7;
    public static final int BUTTON_SHOOTER_DOWN = 8;
    public static final int BUTTON_SHOOTER_UP = 9;
     public static final int MANUAL_BALL_DEC = 10;
     public static final int MANUAL_BALL_INC = 11;
     // PWM Bus
     public static final int MOTOR_DRIVE_LEFT = 1;
     public static final int MOTOR_DRIVE_RIGHT = 2;
     public static final int MOTOR_SHOOTER = 3;
     public static final int MOTOR_HOOD = 4;
     public static final int MOTOR_ELEVATOR = 5;
     public static final int MOTOR_LOADER = 6;
     // Relay Bus
     public static final int RELAY_ARM = 2;
     public static final int LED_RING = 1;
     // Ball handling constants
     public static final double SHOOTER_ADJUST_RATE = 1.0;
     public static final double SHOOTER_CMD_SPEED_MAX = 1.0;
     public static final double SHOOTER_CMD_SPEED_MIN = 0.0;
     public static final int SHOOTER_RATE_MAX = 52;
     public static final int SHOOTER_RATE_MIN = 0;
     public static final double SHOOTER_SPEED_DEFAULT = SHOOTER_RATE_MAX * 0.80;
     public static final double SHOOTER_ZERO_THRESHOLD = SHOOTER_RATE_MAX / 10;
     public static final double SHOOTER_P_GAIN = 0.05;
     public static final double SHOOTER_I_GAIN = 0.001;
     public static final double SHOOTER_D_GAIN = 0.0;
     public static final long ENCODER_INTERVAL = 100000;
     public static final double ELEVATOR_SPEED_SHOOT = 1.0;
     public static final double ELEVATOR_SPEED_LOAD = 1.0;
     public static final double LOADER_SPEED = -1.0;
     public static final int MAX_BALLS = 3;
     // Digital Bus
     public static final int ELEVATOR_MID_SWITCH = 2;
     public static final int ELEVATOR_TOP_SWITCH = 3;
     public static final int ENCODER = 5;
     public static final int ELEVATOR_BOTTOM_SWITCH = 8;
     // Analog Bus
     public static final int GYRO = 1;
     public static final int TEMP = 2;
     public static final int RANGEFINDER = 3;
     public static final int HOOD_POTENTIOMETER = 4;
     public static final int HOOD_POT_VIN = 5;
     public static final int VIN = 7;
     // IC2 Bus
     public static final int ACCELEROMETER = 1;
     // Driving Constants
     public static final int DRIVE_SENSITIVITY = 2;
     public static final double BALANCE_P_GAIN = 2.00;
     public static final double BALANCE_I_GAIN = 0.01;
     public static final double BALANCE_D_GAIN = 0.01;
     public static final double BALANCE_MAX_SPEED_LOW = 0.20;
     public static final double BALANCE_MAX_SPEED_HIGH = 0.45;
     public static final double BALANCE_SPEED_THRESHOLD = 0.20;
     public static final double DRIVE_SPEED_SCALE = 0.8;
     public static final double BALANCE_MAX_SETPOINT = 0.1;
     public static final double BALANCE_ZERO_THRESHOLD = 0.025;
     public static final double BALANCE_ZERO_ADJUST = -0.04;
     public static final double BALANCE_FALL_STARTS = 0.20;
     public static final double BALANCE_NEAR_LEVEL = 0.10;
     public static final double BALANCE_NEAR_LEVEL_SPEED = 0.10;
     public static final double TURN_SPEED_MAX = 0.4;
     public static final double TURN_ZERO_THRESHOLD = 1.0;
     public static final double TURN_P_GAIN = TURN_SPEED_MAX / 15.0;
     public static final double TURN_I_GAIN = TURN_P_GAIN / 10.0;
     public static final double TURN_D_GAIN = 0.0;
     public static final int FIND_TARGET_TURN = 15;
     public static final double AUTO_RAMP_KNOCKDOWN_SPEED = 0.75;
     public static final double AUTO_RAMP_TIMEOUT = 2;
     // Hood Angle Constants
     public static final double HOOD_ANGLE_MIN = 3.900;
     public static final double HOOD_ANGLE_MAX = 4.850;
     public static final double HOOD_ADJUST_RATE = 0.05;
     public static final double HOOD_PID_TOLERANCE = 0.01;
     public static final double HOOD_PID_PERIOD = 0.1;
     public static final double HOOD_P_GAIN = -2.0; // Negative due to backwards pot
     public static final double HOOD_I_GAIN = -0.08; // Negative due to backwards pot
     public static final double HOOD_D_GAIN = 0;
     // Arm Constants
     public static final double ARM_TIMEOUT = 0.75;
     public static final double ARMLOCK_MOD = .5;
     // LED ring constants
     //Camera Constants
     public static final double CAMERA_VA = 47; //Horizontal viewing angle in degrees
     //HSL limits for target square
     public static final int HUE_LOW = 40;
     public static final int HUE_HIGH = 150;
     public static final int SAT_LOW = 60;
     public static final int SAT_HIGH = 255;
     public static final int LUM_LOW = 60;
     public static final int LUM_HIGH = 255;
     public static final double TARGET_W = 24;
     public static final double TARGET_H = 18;
     public static final double TOP_TO_BOTTOM_H=88;
     public static final double TARGET_MIN_RATIO=.75;
 }
