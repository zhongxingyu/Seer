 package edu.ius.robotics.robots;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.lang.Runtime;
 
 import edu.ius.robotics.robots.boards.PMS5005;
 import edu.ius.robotics.robots.boards.PMB5010;
 import edu.ius.robotics.robots.interfaces.IRobot;
 import edu.ius.robotics.robots.interfaces.IRobotEventHandler;
 
 import edu.ius.robotics.robots.sockets.UDPSocket;
 
 //import javax.imageio.plugins.jpeg.*;
 //import java.awt.image.BufferedImage;
 
 public class X80Pro implements IRobot, Runnable
 {
 	/** minimum time step in milliseconds */
 	public static final int MIN_TIME_MILLI = 50;
 	
 	public static final int DEFAULT_ROBOT_PORT = 10001;
 	public static final int NO_CTRL = -32768;
 	
 	public static final int DEFAULT_MOTOR_SENSOR_TIME_PERIOD = 50;
 	public static final int DEFAULT_STANDARD_SENSOR_TIME_PERIOD = 50;
 	public static final int DEFAULT_CUSTOM_SENSOR_TIME_PERIOD = 50;
 	
 	public static final int ACCEPTABLE_ENCODER_DEVIATION = 16;
 	public static final int NO_CONTROL = -32768;
 	
 	public static final int CONTROL_MODE_PWM = 0;
 	public static final int CONTROL_MODE_POSITION = 1;
 	public static final int CONTROL_MODE_VELOCITY = 2;
 	
 	public static final int SENSOR_USAGE_SINGLE_POTENTIOMETER = 0x00;
 	public static final int SENSOR_USAGE_DUAL_POTENTIOMETER = 0x01;
 	public static final int SENSOR_USAGE_ENCODER = 0x02;
 	
 	public static final int INITIAL_TARGET_FINAL = 3;
 	public static final int FINAL = 2;
 	public static final int TARGET = 1;
 	public static final int INITIAL = 0;
 	public static final int TURN_QUAD_T = 4;
 	
 	public static final int MODE_SINGLEPOT = 0;
 	public static final int MODE_DUALPOT = 1;
 	public static final int MODE_QUADRATURE = 2;
 
 	public static final int cFULL_COUNT = 32767;
 	public static final int cWHOLE_RANGE = 1200;
 
 	public static final int DIRECTION = -1;
 	public static final int SERVO0_INI = 3650;
 	public static final int SERVO1_INI = 3300;
 	public static final int MAX_VELOCITY = 1200;
 	public static final int VELOCITY = 1000;
 	
 	public static final int MICRO_DELAY = 50;
 	public static final int MINI_DELAY = 100;
 	public static final int DELAY = 500;
 	public static final int BIG_DELAY = 1000;
 	public static final int UBER_DELAY = 5000;
 	
 	public static final int MAX_PWM_L = 32767;
 	public static final int MAX_PWM_R = 0;
 	
 	public static final int PWM_N = 16383;
 	public static final int PWM_O = 8000;
 	public static final int DUTY_CYCLE_UNIT = 8383;
 	
 	public static final double MAX_IR_DIS = 0.81;
 	public static final double MIN_IR_DIS = 0.09;
 	
 	public static final int L = 0;
 	public static final int R = 1;
 	
 	public static final int X = 0;
 	public static final int Y = 1;
 	
 	public static final int MAX_SPEED = 32767;
 
 //	public static final int PACKAGE_STX0 = 0x5E;
 //	public static final int PACKAGE_STX1 = 0x02;
 //	public static final int PACKAGE_ETX0 = 0x5E;
 //	public static final int PACKAGE_ETX1 = 0x0D;
 //	
 //	public static final int PACKAGE_DATA_HEADER_LENGTH = 6;
 //	public static final int PACKAGE_DATA_FOOTER_LENGTH = 3;
 //	public static final int PACKAGE_DATA_DID_OFFSET = 4;
 //	public static final int PACKAGE_DATA_PAYLOAD_OFFSET = 6;
 //	public static final int PACKAGE_DATA_LENGTH_OFFSET = 5;
 //	public static final int PACKAGE_DATA_METADATA_SIZE = 9;
 //	
 //	public static final int PACKAGE_DATA_NUM_IMAGE_PKGS_OFFSET = 1;
 //	public static final int PACKAGE_DATA_IMAGE_VIDEO_DATA_OFFSET = 2;
 	
 	private class Pkg
 	{
 		public static final int STX0_OFFSET = 0;
 		public static final int STX1_OFFSET = 1;
 		public static final int RID_OFFSET = 2;
 		public static final int RESERVED_OFFSET = 3;
 		public static final int DID_OFFSET = 4;
 		public static final int LENGTH_OFFSET = 5;
 		public static final int DATA_OFFSET = 6;
 		public static final int CHECKSUM_RELATIVE_OFFSET = 0;
 		public static final int ETX0_RELATIVE_OFFSET = 1;
 		public static final int ETX1_RELATIVE_OFFSET = 2;
 		public static final int HEADER_LENGTH = 6;
 		public static final int IMAGE_PKG_COUNT_RELATIVE_OFFSET = 1;
 		public static final int IMAGE_DATA_RELATIVE_OFFSET = 2;
 		
		public static final int MAX_DATA_SIZE = 0xFF;
 		public static final int STX0 = 0x5e;
 		public static final int STX1 = 0x02;
 		public static final int ETX0 = 0x5e;
 		public static final int ETX1 = 0x0d;
 		
 		public int offset;
 		
 		public int stx0;
 		public int stx1;
 		
 		public int destination;
 		public int serial;
 		public int type;
 		public int length;
 		public byte[] data;
 		
 		public int checksum;
 		public int etx0;
 		public int etx1;
 		
 		public void reset()
 		{
 			offset = 0;
 			stx0 = 0;
 			stx1 = 0;
 			destination = 0;
 			serial = 0;
 			type= 0;
 			length = 0;
 			checksum = 0;
 			etx0 = 0;
 			etx1 = 0;
 			
 			for (int i = 0; i < MAX_DATA_SIZE; ++i)
 			{
 				data[i] = 0;
 			}
 		}
 		
 		public Pkg()
 		{
 			data = new byte[MAX_DATA_SIZE];
 			reset();
 		}
 		
 		// This is from PMS5005 and PMB5010 protocol documentation
 		public byte checksum() // could, maybe should, be static (checksum(byte[] buf))
 		{
 			byte shift_reg, sr_lsb, data_bit, v;
 			byte fb_bit;
 			int z;
 			shift_reg = 0; // initialize the shift register
 			z = data.length - 5;
 			for (int i = 0; i < z; ++i) 
 			{
 			    v = (byte) (data[2 + i]); // start from RID
 			    // for each bit
 			    for (int j = 0; j < 8; ++j) 
 			    {
 					// isolate least sign bit
 					data_bit = (byte) ((v & 0x01) & 0xFF);
 					sr_lsb = (byte) ((shift_reg & 0x01) & 0xFF);
 					// calculate the feed back bit
 					fb_bit = (byte) (((data_bit ^ sr_lsb) & 0x01) & 0xFF);
 					shift_reg = (byte) ((shift_reg & 0xFF) >>> 1);
 					if (fb_bit == 1)
 					{
 					    shift_reg = (byte) ((shift_reg ^ 0x8C) & 0xFF);
 					}
 					v = (byte) ((v & 0xFF) >>> 1);
 			    }
 			}
 			return shift_reg;
 		}
 	}
 	
 	private class MotorSensorData
 	{
 		public int[] potentiometerAD;
 		public int[] motorCurrentAD;
 		public int[] encoderPulse;
 		public int[] encoderSpeed;
 		public int[] encoderDirection;
 		
 		public MotorSensorData()
 		{
 			potentiometerAD = new int[Sensors.NUM_POTENTIOMETER_AD_SENSORS];
 			motorCurrentAD = new int[Sensors.NUM_MOTOR_CURRENT_AD_SENSORS];
 			encoderPulse = new int[Sensors.NUM_ENCODER_PULSE_SENSORS];
 			encoderSpeed = new int[Sensors.NUM_ENCODER_SPEED_SENSORS];
 			encoderDirection = new int[Sensors.NUM_ENCODER_DIRECTION_SENSORS];
 		}
 		
 		public void setMotorSensorData(byte[] motorSensorData)
 		{
 			int offset = 0, i;
 			for (i = 0; i < Sensors.NUM_POTENTIOMETER_AD_SENSORS; ++i) 
 			{
 				potentiometerAD[i] = (short) ((motorSensorData[offset + 2*i + 1] & 0x0F) << 8 | motorSensorData[offset + 2*i] & 0xFF);
 			}
 			
 			offset += 2*Sensors.NUM_POTENTIOMETER_AD_SENSORS;
 			for (i = 0; i < Sensors.NUM_MOTOR_CURRENT_AD_SENSORS; ++i) 
 			{
 				motorCurrentAD[i] = (short) ((motorSensorData[offset + 2*i + 1] & 0x0F) << 8 | motorSensorData[offset + 2*i] & 0xFF);
 			}
 			
 			offset += 2*Sensors.NUM_MOTOR_CURRENT_AD_SENSORS;
 			for (i = 0; i < Sensors.NUM_ENCODER_PULSE_SENSORS; ++i) 
 			{
 				encoderPulse[i] = (short) ((motorSensorData[offset + 4*i + 1] & 0xFF) << 8 | motorSensorData[offset + 4*i] & 0xFF);
 			}
 			
 			offset += 2;
 			for (i = 0; i < Sensors.NUM_ENCODER_SPEED_SENSORS; ++i) 
 			{
 				encoderSpeed[i] = (short) ((motorSensorData[offset + 4*i + 1] & 0xFF) << 8 | motorSensorData[offset + 4*i] & 0xFF);
 			}
 			
 			offset += 6;
 			for (i = 0; i < Sensors.NUM_ENCODER_DIRECTION_SENSORS; ++i) 
 			{
 				encoderDirection[i] = (byte) (motorSensorData[offset] & (i+1));
 			}
 		}
 	}
 	
 	private class CustomSensorData
 	{
 		public int[] customAD;
 		
 		public int[] ioPort;
 		public int[] mmDistanceToLeftConstellation;
 		public int[] mmDistanceToRightConstellation;
 		public int[] mmDistanceToRelativeConstellation;
 		
 		public CustomSensorData()
 		{
 			customAD = new int[Sensors.NUM_CUSTOM_AD_SENSORS];
 			ioPort = new int[Sensors.NUM_IO_PORT_SENSORS];
 			mmDistanceToLeftConstellation = new int[Sensors.NUM_LEFT_CONSTELLATION_SENSORS];
 			mmDistanceToRightConstellation = new int[Sensors.NUM_RIGHT_CONSTELLATION_SENSORS];
 			mmDistanceToRelativeConstellation = new int[Sensors.NUM_RELATIVE_CONSTELLATION_SENSORS];
 		}
 		
 		public void setCustomSensorData(byte[] customSensorData)
 		{
 //			System.out.println("CustomSensorData Dump:");
 //			for (int j = 0; j < 60; ++j)
 //			{
 //				System.out.printf("%x ", customSensorData[j]);
 //			}
 //			System.out.println();
 			
 			int offset = 0, i;
 			
 			for (i = 0; i < Sensors.NUM_CUSTOM_AD_SENSORS; ++i)
 			{
 				customAD[i] = (short) ((customSensorData[offset + 2*i + 1] & 0x0F) << 8 | (customSensorData[offset + 2*i] & 0xFF));
 			}
 			
 			offset += 2*Sensors.NUM_CUSTOM_AD_SENSORS;
 			for (i = 0; i < Sensors.NUM_IO_PORT_SENSORS; ++i)
 			{
 				ioPort[i] = (byte) customSensorData[offset] & ((0x01 << i) & 0xFF);
 			}
 			
 			offset += 1;
 			for (i = 0; i < Sensors.NUM_LEFT_CONSTELLATION_SENSORS; ++i)
 			{
 				mmDistanceToLeftConstellation[i] = (short) ((customSensorData[offset + 2*i + 1] & 0x0F) << 8 | customSensorData[offset + 2*i] & 0xFF);
 			}
 			
 			offset += 2*Sensors.NUM_LEFT_CONSTELLATION_SENSORS;
 			for (i = 0; i < Sensors.NUM_RIGHT_CONSTELLATION_SENSORS; ++i)
 			{
 				mmDistanceToRightConstellation[i] = (short) ((customSensorData[offset + 2*i + 1] & 0x0F) << 8 | customSensorData[offset + 2*i] & 0xFF); 
 			}
 			
 			offset += 2*Sensors.NUM_RIGHT_CONSTELLATION_SENSORS;
 			for (i = 0; i < Sensors.NUM_RELATIVE_CONSTELLATION_SENSORS; ++i)
 			{
 				mmDistanceToRelativeConstellation[i] = (short) ((customSensorData[offset + 2*i + 1] & 0x0F) << 8 | customSensorData[offset + 2*i] & 0xFF); 				
 			}
 		}
 	}
 	
 	private class StandardSensorData
 	{
 		public int[] sonarDistance;
 		public int[] humanAlarm;
 		public int[] motionDetect;
 		public int[] tiltingAD;
 		public int[] overheatAD;
 		public int temperatureAD;
 		public int infraredRangeAD;
 		public int[] infraredCommand;
 		public int mainboardBatteryVoltageAD_0to9V;
 		public int motorBatteryVoltageAD_0to24V;
 		public int servoBatteryVoltageAD_0to9V;
 		public int referenceVoltageAD_Vcc;
 		public int potentiometerVoltageAD_Vref;
 		
 		public StandardSensorData()
 		{
 			sonarDistance = new int[Sensors.NUM_SONAR_SENSORS];
 			humanAlarm = new int[Sensors.NUM_HUMAN_SENSORS];
 			motionDetect = new int[Sensors.NUM_HUMAN_SENSORS];
 			tiltingAD = new int[Sensors.NUM_TILTING_SENSORS];
 			overheatAD = new int[Sensors.NUM_TEMPERATURE_SENSORS];
 			temperatureAD = -1;
 			infraredRangeAD = -1;
 			infraredCommand = new int[Sensors.NUM_INFRARED_RECEIVERS];
 			mainboardBatteryVoltageAD_0to9V = -1;
 			motorBatteryVoltageAD_0to24V = -1;
 			servoBatteryVoltageAD_0to9V = -1;
 			referenceVoltageAD_Vcc = -1;
 			potentiometerVoltageAD_Vref = -1;
 		}
 		
 		public void setStandardSensorData(byte[] standardSensorData)
 		{
 			int offset = 0, i;
 			for (i = 0; i < Sensors.NUM_SONAR_SENSORS; ++i)
 			{
 				sonarDistance[i] = (byte) (standardSensorData[offset + i] & 0xFF);
 			}
 			
 			offset += Sensors.NUM_SONAR_SENSORS;
 			for (i = 0; i < Sensors.NUM_HUMAN_SENSORS; ++i)
 			{
 				humanAlarm[i] = (short) ((standardSensorData[offset + 4*i + 1] & 0xF0) << 8 | standardSensorData[offset + 4*i] & 0xFF);
 			}
 			
 			offset += 2;
 			for (i = 0; i < Sensors.NUM_HUMAN_SENSORS; ++i)
 			{
 				motionDetect[i] = (short) ((standardSensorData[offset + 4*i + 1] & 0xF0) << 8 | standardSensorData[offset + 4*i] & 0xFF);
 			}
 			
 			offset += 6;
 			for (i = 0; i < Sensors.NUM_TILTING_SENSORS; ++i)
 			{
 				tiltingAD[i] = (short) ((standardSensorData[offset + 2*i + 1] & 0xF0 ) << 8 | standardSensorData[offset + 2*i] & 0xFF);
 			}
 			
 			offset += 2*Sensors.NUM_TILTING_SENSORS;
 			for (i = 0; i < Sensors.NUM_OVERHEAT_SENSORS; ++i)
 			{
 				overheatAD[i] = (short) ((standardSensorData[offset + 2*i + 1] & 0xF0) << 8 | standardSensorData[offset + 2*i] & 0xFF);
 			}
 			
 			offset += 2*Sensors.NUM_OVERHEAT_SENSORS;
 			temperatureAD = (short) ((standardSensorData[offset + 1] & 0x0F) << 8 | standardSensorData[offset] & 0xFF);
 			
 			offset += 2;
 			infraredRangeAD = (short) ((standardSensorData[offset + 1] & 0x0F) << 8 | standardSensorData[offset] & 0xFF);
 			
 			offset += 2;
 			for (i = 0; i < Sensors.NUM_INFRARED_RECEIVERS; ++i)
 			{
 				infraredCommand[i] = (byte) (standardSensorData[offset + i] & 0xFF);
 			}
 			
 			offset += Sensors.NUM_INFRARED_RECEIVERS;
 			mainboardBatteryVoltageAD_0to9V = (short) ((standardSensorData[offset + 1] & 0x0F) << 8 | standardSensorData[offset & 0xFF]);
 			
 			offset += 2;
 			motorBatteryVoltageAD_0to24V = (short) ((standardSensorData[offset + 1] & 0x0F) << 8 | standardSensorData[offset] & 0xFF);
 			
 			offset += 2;
 			servoBatteryVoltageAD_0to9V = (short) ((standardSensorData[offset + 1] & 0x0F) << 8 | standardSensorData[offset] & 0xFF);
 			
 			offset += 2;
 			referenceVoltageAD_Vcc = (short) ((standardSensorData[offset + 1] & 0x0F) << 8 | standardSensorData[offset] & 0xFF);
 			
 			offset += 2;
 			potentiometerVoltageAD_Vref = (short) ((standardSensorData[offset + 1] & 0x0F) << 8 | standardSensorData[offset] & 0xFF);
 		}
 	}
 	
 	public static class Sensors // enum?
 	{
 		// Standard and Custom sensors
 		public static final int NUM_STANDARD_SENSORS = 6;
 		public static final int NUM_MOTORS = 2;
 		public static final int NUM_INFRARED_SENSORS = 8;
 		public static final int NUM_INFRARED_SENSORS_FRONT = 4;
 		public static final int NUM_CUSTOM_AD_SENSORS = 8;
 		public static final int NUM_SONAR_SENSORS = 6;
 		public static final int NUM_SONAR_SENSORS_FRONT = 3;
 		public static final int NUM_HUMAN_SENSORS = 2;
 		public static final int NUM_IO_PORT_SENSORS = 8;
 		public static final int NUM_LEFT_CONSTELLATION_SENSORS = 4;
 		public static final int NUM_RIGHT_CONSTELLATION_SENSORS = 4;
 		public static final int NUM_RELATIVE_CONSTELLATION_SENSORS = 4;
 		public static final int NUM_TILTING_SENSORS = 2;
 		public static final int NUM_OVERHEAT_SENSORS = 2;
 		public static final int NUM_TEMPERATURE_SENSORS = 2;
 		public static final int NUM_INFRARED_RECEIVERS = 4;
 		
 		// Motor Sensors
 		public static final int NUM_POTENTIOMETER_AD_SENSORS = 6;
 		public static final int NUM_MOTOR_CURRENT_AD_SENSORS = 6;
 		public static final int NUM_ENCODER_PULSE_SENSORS = 2;
 		public static final int NUM_ENCODER_SPEED_SENSORS = 2;
 		public static final int NUM_ENCODER_DIRECTION_SENSORS = 2;
 		
 		public static double[] INFRARED_SENSOR_POSITION;
 		public static double[] SONAR_SENSOR_POSITION;
 		
 		public static int FRONT_FAR_LEFT_INFRARED_SENSOR = 0;
 		public static int FRONT_MID_LEFT_INFRARED_SENSOR_INDEX = 1;
 		public static int FRONT_MID_RIGHT_INFRARED_SENSOR_INDEX = 2;
 		public static int FRONT_FAR_RIGHT_INFRARED_SENSOR = 3;
 		
 		public static int FRONT_LEFT_SONAR_SENSOR = 0;
 		public static int FRONT_MIDDLE_SONAR_SENSOR = 1;
 		public static int FRONT_RIGHT_SONAR_SENSOR = 2;
 		
 		static
 		{
 			INFRARED_SENSOR_POSITION[0] = 0.785;
 			INFRARED_SENSOR_POSITION[1] = 0.35;
 			INFRARED_SENSOR_POSITION[2] = -0.35;
 			INFRARED_SENSOR_POSITION[3] = -0.785; 
 			
 			SONAR_SENSOR_POSITION[0] = 0.8; 
 			SONAR_SENSOR_POSITION[1] = 0;
 			SONAR_SENSOR_POSITION[2] = -0.8; 
 		}
 	}
 	
 	// private ByteArrayInputStream byteArrIn;
 	// private DataInputStream dataIn;
 	// private DataOutputStream dataOut;
 	// private InputStream inputStream;
 	// private BufferedReader reader;
 	// private String command;
 	/** wheel distance */
 	public static final short WHEEL_CIRCUMFERENCE_ENCODER_COUNT = 800; // encoder units
 	public static final double WHEEL_CIRCUMFERENCE = 0.265; // meters
 	public static final double WHEEL_DISPLACEMENT = 0.2897; // meters (default: 0.305)
 	
 	/** wheel radius */
 	public static final double WHEEL_RADIUS = 0.085; // ~0.0825-0.085 meters
 	
 	/** encoder one circle count */
 	public static final int ROTATE_ROBOT_ENCODER_COUNT = 1200;
 	
 //	private int motorSensorTimePeriod;
 //	private int standardSensorTimePeriod;
 //	private int customSensorTimePeriod;
 	
 	//volatile private byte[] motorSensorData;
 	//volatile private byte[] standardSensorData;
 	//volatile private byte[] customSensorData;
 	private MotorSensorData motorSensorData;
 	private CustomSensorData customSensorData;
 	private StandardSensorData standardSensorData;
 	//private int[] powerSensorData;
 	
 	//private boolean[] lockIRRange;
 	byte previousSEQ;
 	
 	private UDPSocket socket;
 	//private X80ProADPCM pcm;
 	private ByteArrayOutputStream audioBuffer;
 	private ByteArrayOutputStream imageBuffer;
 	//private List<Integer> imageSEQs;
 	//private int[] imageSEQs;
 	private IRobotEventHandler iRobotEventHandler;
 	
 //	private byte[] currentPackage;
 //	private int currentPackageLength;
 //	private int currentPackageType;
 //	private int remain;
 //	private int pkgi;
 	private Pkg pkg;
 	
 	private void preInit()
 	{
 		//lockIRRange = new boolean[NUM_IR_SENSORS];
 		this.pkg = new Pkg();
 		
 		this.iRobotEventHandler = null;
 		//this.pcm = new X80ProADPCM();
 		//this.imageSEQs = new LinkedList<Integer>();
 		//this.imageSEQs = new int[16384];
 		
 		this.audioBuffer = new ByteArrayOutputStream();
 		this.imageBuffer = new ByteArrayOutputStream();
 		
 		//this.motorSensorData = new byte[PMS5005.HEADER_LENGTH + PMS5005.MOTOR_SENSOR_DATA_LENGTH];
 		//this.customSensorData = new byte[PMS5005.HEADER_LENGTH + PMS5005.CUSTOM_SENSOR_DATA_LENGTH];
 		//this.standardSensorData = new byte[PMS5005.HEADER_LENGTH + PMS5005.STANDARD_SENSOR_DATA_LENGTH];
 		this.motorSensorData = new MotorSensorData();
 		this.customSensorData = new CustomSensorData();
 		this.standardSensorData = new StandardSensorData();
 		
 		// The following few lines are a bad hack to compare the last two sensor readings from each different type.
 		// This is to find out what the rate of change is, so that we can possibly correct weird sensor behavior.
 		//this.oldMotorSensorData = new byte[2][];
 		//this.oldCustomSensorData = new byte[2][];
 		//this.oldStandardSensorData = new byte[2][];
 		
 		//this.oldMotorSensorData[0] = this.motorSensorData;
 		//this.oldCustomSensorData[0] = this.customSensorData;
 		//this.oldStandardSensorData[0] = this.standardSensorData;
 	}
 	
 	private void postInit()
 	{
 		// Enable collection of all sensor data by default.
 		this.socket.send(PMS5005.enableMotorSensorSending());
 		this.socket.send(PMS5005.enableCustomSensorSending());
 		this.socket.send(PMS5005.enableStandardSensorSending());
 		
 		this.socket.send(PMS5005.setDCMotorSensorUsage((byte) L, (byte) X80Pro.SENSOR_USAGE_ENCODER));
 		this.socket.send(PMS5005.setDCMotorSensorUsage((byte) R, (byte) X80Pro.SENSOR_USAGE_ENCODER));
 		
 		this.socket.send(PMS5005.setDCMotorControlMode((byte) L, (byte) X80Pro.CONTROL_MODE_PWM));
 		this.socket.send(PMS5005.setDCMotorControlMode((byte) R, (byte) X80Pro.CONTROL_MODE_PWM));		
 		
 		// This shutdown hook indicates other threads should terminate as well (e.g. sensor data collection thread).
 		this.attachShutdownHook();
 	}
 	
 	/**
 	 * Creates a new instance of X80Pro
 	 * 
 	 * @param ipAddress
 	 */
 	public X80Pro(String ipAddress) throws IOException
 	{
 		preInit();
 		this.socket = new UDPSocket(this, ipAddress, DEFAULT_ROBOT_PORT);
 		postInit();
 	}
 	
 	/**
 	 * Creates a new instance of X80Pro
 	 * 
 	 * @param robotIp
 	 * @param robotPort
 	 */
 	public X80Pro(String ipAddress, int port) throws IOException
 	{
 		preInit();
 		this.socket = new UDPSocket(this, ipAddress, port);
 		postInit();
 	}
 	
 	public X80Pro(String ipAddress, IRobotEventHandler iRobotEventHandler) throws IOException
 	{
 		preInit();
 		this.iRobotEventHandler = iRobotEventHandler;
 		this.socket = new UDPSocket(this, ipAddress, DEFAULT_ROBOT_PORT);
 		postInit();
 	}
 	
 	public X80Pro(String ipAddress, int port, IRobotEventHandler iRobotEventHandler) throws IOException
 	{
 		preInit();
 		this.iRobotEventHandler = iRobotEventHandler;
 		this.socket = new UDPSocket(this, ipAddress, port);
 		postInit();
 	}
 	
 	/**
 	 * sends a command to the robot
 	 * 
 	 * @param command
 	 */
 	public void sendCommand(byte[] command)
 	{
 		socket.send(command);
 	}
 	
 	private void dispatch(Pkg pkg, String robotIP, int robotPort)
 	{
 		if (PMS5005.GET_MOTOR_SENSOR_DATA == pkg.type)
 		{
 //			System.err.println("-*- Motor Sensor Data Package Received -*-");
 			motorSensorData.setMotorSensorData(pkg.data);
 		}
 		else if (PMS5005.GET_CUSTOM_SENSOR_DATA == pkg.type)
 		{
 //			System.err.println("-*- Custom Sensor Data Package Received -*-");
 			customSensorData.setCustomSensorData(pkg.data);
 		}
 		else if (PMS5005.GET_STANDARD_SENSOR_DATA == pkg.type)
 		{
 //			System.err.println("-*- Standard Sensor Data Package Received -*-");
 			standardSensorData.setStandardSensorData(pkg.data);
 		}
 		else if (PMB5010.ADPCM_RESET == pkg.type)
 		{
 //			System.err.println("-*- ADPCM Reset Command Package Received -*-");
 			//pcm.init();
 			//socket.send(PMB5010.ack((byte) 0)); // TODO insert actual sequence value here
 			if (null != iRobotEventHandler)
 			{
 				iRobotEventHandler.audioCodecResetRequestReceivedEvent(robotIP, robotPort);
 			}
 		}
 		else if (PMB5010.AUDIO_PACKAGE == pkg.type)
 		{
 //			System.err.println("-*- Audio Data Package Received -*-");
 			if (null != iRobotEventHandler)
 			{
 				audioBuffer.reset();
 				audioBuffer.write(pkg.data, 0, pkg.length);
 				iRobotEventHandler.audioSegmentReceivedEvent(robotIP, robotPort, audioBuffer);
 			}
 		}
 		else if (PMB5010.VIDEO_PACKAGE == pkg.type)
 		{
 			System.err.println("-*- Image Data Package Received -*-");
 			if (null != iRobotEventHandler)
 			{
 				// Step 1: Clear buffer sizes if we're receiving first JPEG packet.
 				if (PMB5010.SEQ_BEGIN == (byte) (0xFF & pkg.data[PMB5010.VIDEO_SEQ_OFFSET]))
 				//if (0 == this.numImagePkgs)
 				{
 					System.err.println("Beginning jpeg image assembly");
 					System.err.println();
 					//this.numImagePkgs = 0xFF & currentPackage[X80Pro.PACKAGE_DATA_NUM_IMAGE_PKGS_OFFSET];
 					//System.err.println("numImagePkgs: " + numImagePkgs);
 					imageBuffer.reset();
 					previousSEQ = pkg.data[PMB5010.VIDEO_SEQ_OFFSET];
 				}
 				
 				// Step 2: Assemble complete data in buffer.
 				if (0 == imageBuffer.size() || PMB5010.SEQ_TERMINATE == (byte) (0xFF & pkg.data[PMB5010.VIDEO_SEQ_OFFSET]) || 
 						previousSEQ == pkg.data[PMB5010.VIDEO_SEQ_OFFSET] - 1)
 				{
 					System.err.println("Continuing jpeg image assembly");
 					imageBuffer.write(pkg.data, Pkg.IMAGE_DATA_RELATIVE_OFFSET, pkg.length);
 					previousSEQ = pkg.data[PMB5010.VIDEO_SEQ_OFFSET];
 				}
 				else
 				{
 					System.err.println("Image pieces out of sequence");
 				}
 				
 				// Step 3: Decode complete data from buffer if we have finished receiving.
 				if (PMB5010.SEQ_TERMINATE == (byte) (0xFF & pkg.data[PMB5010.VIDEO_SEQ_OFFSET])) // || this.numImagePkgs - 1 <= pkg[PMB5010.VIDEO_SEQ_OFFSET])
 				{
 					System.err.println("Finished jpeg image assembly");
 					iRobotEventHandler.imageReceivedEvent(robotIP, robotPort, imageBuffer);
 				}
 			}
 			else
 			{
 				System.err.println("iRobotEventHandler is null");
 			}
 			// else if null == iRobotEventHandler (no delegate), we won't do anything with the data.
 		}
 		else if (PMS5005.SETUP_COM == pkg.type)
 		{
 //			System.err.println("-*- SETUP_COM Command Received -*-");
 //			for (int i = 0; i < pkgLen; ++i)
 //			{
 //				System.err.print((pkg[i] & 0xFF) + " ");
 //			}
 //			System.err.println();
 //			socket.send(PMS5005.ack());
 		}
 	}
 	
 	public void sensorEvent(String robotIP, int robotPort, byte[] msg, int len)
 	{
 		System.err.println("-*- new message -*-");
 		System.err.println("-*- message len: " + len + " -*-");
 		int i = 0;
 		while (i < len)
 		{
 			if (i < len && Pkg.STX0_OFFSET == pkg.offset)
 			{
 				if (Pkg.STX0 != (msg[i] & 0xFF)) 
 				{
 					System.err.println("DEBUG: STX0 isn't where it is expected to be");
 				}
 				pkg.stx0 = (byte) (msg[i++] & 0xFF);
 				++pkg.offset;
 			}
 			if (i < len && Pkg.STX1_OFFSET == pkg.offset)
 			{
 				if (Pkg.STX1 != (msg[i] & 0xFF))
 				{
 					System.err.println("DEBUG: STX1 isn't where it is expected to be");
 				}
 				pkg.stx1 = (byte) (msg[i++] & 0xFF);
 				++pkg.offset;
 			}
 			if (i < len && Pkg.RID_OFFSET == pkg.offset)
 			{
 				pkg.destination = (byte) (msg[i++] & 0xFF);
 				++pkg.offset;
 			}
 			if (i < len && Pkg.RESERVED_OFFSET == pkg.offset)
 			{
 				pkg.serial = (byte) (msg[i++] & 0xFF);
 				++pkg.offset;
 			}
 			if (i < len && Pkg.DID_OFFSET == pkg.offset)
 			{
 				pkg.type = (byte) (msg[i++] & 0xFF);
 				++pkg.offset;
 			}
 			if (i < len && Pkg.LENGTH_OFFSET == pkg.offset)
 			{
 				pkg.length = (byte) (msg[i++] & 0xFF);
 				++pkg.offset;
 			}
 			if (i < len && Pkg.DATA_OFFSET <= pkg.offset && pkg.offset < pkg.length)
 			{
				int j = pkg.offset - Pkg.HEADER_LENGTH;
				while (i < len && j < pkg.length)
 				{
					pkg.data[j++] = (byte) (msg[i++] & 0xFF);
 					++pkg.offset;
 				}
 			}
 			if (i < len && pkg.length + Pkg.CHECKSUM_RELATIVE_OFFSET == pkg.offset)
 			{
 				pkg.checksum = pkg.checksum();
 				if (pkg.checksum != (byte) (msg[i++] & 0xFF))
 				{
 					System.err.println("DEBUG: Incorrect package checksum");
 				}
 				++pkg.offset;
 			}
 			if (i < len && pkg.length + Pkg.ETX0_RELATIVE_OFFSET == pkg.offset)
 			{
 				if (Pkg.ETX0 != msg[i])
 				{
 					System.err.println("DEBUG: ETX0 isn't where it is expected to be");
 				}
 				pkg.etx0 = (byte) (msg[i++] & 0xFF);
 				++pkg.offset;
 			}
 			if (i < len && pkg.length + Pkg.ETX1_RELATIVE_OFFSET == pkg.offset)
 			{
 				if (Pkg.ETX1 != msg[i++])
 				{
 					System.err.println("DEBUG: ETX1 isn't where it is expected to be");
 				}
 				pkg.etx1 = (byte) (msg[i++] & 0xFF);
 				
 				dispatch(pkg, robotIP, robotPort);
 				pkg.reset();
 			}
 		}
 	}
 	
 	//public void sensorEvent(String robotIP, int robotPort, byte[] msg, int len)
 //	{
 //        int msgi = 0;
 ////		System.err.println("len: " + len);
 //		// Assuming packets arrive in serial order. Not the best assumption, but it is easy to implement.
 ////		System.err.println("remain: " + remain + " bytes");
 //        if (PACKAGE_DATA_HEADER_LENGTH <= remain) // remain too long (broken on header)
 //        {
 //        	int pkgbp = remain - currentPackageLength; // always negative value
 //			if (0 < pkgbp + X80Pro.PACKAGE_DATA_DID_OFFSET && pkgbp + X80Pro.PACKAGE_DATA_DID_OFFSET < len)
 //			{
 //				currentPackageType = 0xFF & msg[pkgbp + X80Pro.PACKAGE_DATA_DID_OFFSET];
 //				//System.err.println("currentPackageType: " + currentPackageType);
 //			}
 //			// if not out of bounds, read in the length
 //			if (0 < pkgbp + X80Pro.PACKAGE_DATA_LENGTH_OFFSET && pkgbp + X80Pro.PACKAGE_DATA_LENGTH_OFFSET < len)
 //			{
 //				currentPackageLength = (0xFF & msg[pkgbp + X80Pro.PACKAGE_DATA_LENGTH_OFFSET]); // + X80Pro.PACKAGE_DATA_HEADER_LENGTH;
 //				//System.err.println("currentPackageLength: " + currentPackageLength);
 //			}
 //			
 //        	// consume the header
 //        	pkgi = PACKAGE_DATA_HEADER_LENGTH;
 //        	remain = currentPackageLength - PACKAGE_DATA_HEADER_LENGTH;
 //        }
 //		if (0 < remain)
 //		{
 ////			if (pkgi < PACKAGE_DATA_DID_OFFSET)
 ////			{
 ////				System.err.println("Only read " + pkgi + " bytes before, need to read package type");
 ////				currentPackageType = msg[PACKAGE_DATA_DID_OFFSET - pkgi];
 ////				System.err.println("type: " + currentPackageType);
 ////			}
 ////			if (pkgi < PACKAGE_DATA_LENGTH_OFFSET)
 ////			{
 ////				System.err.println("Only read " + pkgi + " bytes previously, need to read package length");
 ////				currentPackageLength = msg[PACKAGE_DATA_LENGTH_OFFSET - pkgi];
 ////				System.err.println("length: " + currentPackageLength);
 ////			}
 //			System.err.println("Concatenating remaining (" + remain + ") bytes to previous package");
 //			for (msgi = 0; msgi < remain; ++msgi)
 //			{
 //				currentPackage[pkgi + msgi] = msg[msgi];
 //				System.err.printf("%x ", 0xFF & currentPackage[pkgi + msgi]);
 //			}
 //			pkgi += msgi;
 //			System.err.println();
 //			remain -= msgi;
 //			if (0 == remain)
 //			{
 //				System.err.println("read all remaining bytes");
 //			}
 //			System.err.println("remain pkgi: " + pkgi + ", remain currentPackageLength: " + currentPackageLength);
 //			System.err.print("postamble: ");
 //			for (int j = 0; j < 12; ++j)
 //			{
 //				System.err.printf("%x ", msg[msgi + j] & 0xFF);
 //			}
 //			System.err.println();
 //			System.err.println("remain " + msgi + " ?<= " + len);
 //			System.err.println("remain Checksum: " + (0xFF & msg[msgi]));
 //			System.err.println("remain ETX0: " + (0xFF & msg[msgi + 1]));
 //			System.err.println("remain ETX1: " + (0xFF & msg[msgi + 2]));
 //			System.err.println("remain len: " + len);
 //			if (currentPackageLength == pkgi && 
 //					msgi + pkgi + 2 <= len - 1 && 
 //					PACKAGE_ETX0 == (0xFF & msg[msgi + 1]) && 
 //					PACKAGE_ETX1 == (0xFF & msg[msgi + 2]))
 //			{
 //				dispatch(currentPackage, currentPackageLength, currentPackageType, robotIP, robotPort);
 //				msgi += PACKAGE_DATA_FOOTER_LENGTH; // discard trailing metadata
 //			}
 //			System.err.println("msgi after remain: " + msgi);
 //		}
 ////		else if (remain <= PACKAGE_DATA_FOOTER_LENGTH) // remain too short (broken on footer)
 ////		{
 ////			//pkgi += currentPackageLength - pkgi;
 ////			// could check checksum here, if it resides here and 
 ////			// consume ETX0, ETX1, if they reside here, then
 ////			// reset current pacakge information.
 ////			pkgi = 0;
 ////			remain = 0;
 ////		}
 //		remain = 0;
 //		pkgi = 0;
 //		// While there are remaining packages...
 //		while (msgi < len)
 //		{
 //			Arrays.fill(currentPackage, (byte) 0);
 //			currentPackageLength = 0;
 //			currentPackageType = 0;
 //			
 //			int pkgbp = msgi; // acts as a base pointer to the start of the pkg
 //			// if not out of bounds, read in the type
 //			if (pkgbp + X80Pro.PACKAGE_DATA_DID_OFFSET < len)
 //			{
 //				currentPackageType = 0xFF & msg[pkgbp + X80Pro.PACKAGE_DATA_DID_OFFSET];
 //				//System.err.println("currentPackageType: " + currentPackageType);
 //			}
 //			// if not out of bounds, read in the length
 //			if (pkgbp + X80Pro.PACKAGE_DATA_LENGTH_OFFSET < len)
 //			{
 //				currentPackageLength = (0xFF & msg[pkgbp + X80Pro.PACKAGE_DATA_LENGTH_OFFSET]); // + X80Pro.PACKAGE_DATA_HEADER_LENGTH;
 //				//System.err.println("currentPackageLength: " + currentPackageLength);
 //			}
 //			// read data payload
 //			// TODO enter loop, while less than msg len and package len, copy bytes from msg into a package, decrement tmp len
 //			msgi += X80Pro.PACKAGE_DATA_HEADER_LENGTH;
 //			if (currentPackageType == 0x09)
 //			{
 //				for (pkgi = 0; pkgbp + pkgi + X80Pro.PACKAGE_DATA_PAYLOAD_OFFSET < len && pkgi < currentPackageLength; ++pkgi)
 //				{
 //					currentPackage[pkgi] = (byte) (0xFF & msg[pkgbp + X80Pro.PACKAGE_DATA_PAYLOAD_OFFSET + pkgi]);
 //					System.err.printf("%x ", 0xFF & currentPackage[pkgi]);
 //					msgi++;
 //				}
 //				System.err.println();
 //			}
 //			else
 //			{
 //				for (pkgi = 0; msgi < len && pkgi < currentPackageLength; ++pkgi) // pkgbp + pkgi + X80Pro.PACKAGE_DATA_PAYLOAD_OFFSET
 //				{
 //					currentPackage[pkgi] = (byte) (0xFF & msg[pkgbp + X80Pro.PACKAGE_DATA_PAYLOAD_OFFSET + pkgi]);
 //					msgi++;
 //				}
 //			}
 ////			System.err.println();
 ////			System.err.println("postamble: ");
 ////			for (int j = 0; j < 3; ++j)
 ////			{
 ////				System.err.print((0xFF & msg[msgi + j]) + " ");
 ////			}
 //			// not always true... checksum, ETX0, ETX1 may be discontinuous (may cross packets), 
 //			// as well as STX0, STX1 may cross packets to next package
 //			if (currentPackageLength == pkgi && msgi + 2 <= len && 
 //					PACKAGE_ETX0 == (0xFF & msg[msgi + 1]) && PACKAGE_ETX1 == (0xFF & msg[msgi + 2]))
 //			{
 //				//int checksum = 0xFF & msg[pkgbp + pkgi];
 ////				System.err.println("Read ETX0 and ETX1, Dispatching...");
 //				dispatch(currentPackage, currentPackageLength, currentPackageType, robotIP, robotPort);
 //				msgi += PACKAGE_DATA_FOOTER_LENGTH; // discard trailing metadata
 //			}
 //			else // we've run out of packet from which to read the package
 //			{
 //				System.err.println("pkgi: " + pkgi + ", currentPackageLength: " + currentPackageLength);
 ////				System.err.print("postamble: ");
 ////				for (int j = 0; j < 12; ++j)
 ////				{
 ////					System.err.printf("%x ", msg[msgi + j] & 0xFF);
 ////				}
 //				System.err.println();
 //				System.err.println(msgi + " ?<= " + len);
 //				System.err.println("Checksum: " + (0xFF & msg[pkgbp + pkgi + X80Pro.PACKAGE_DATA_PAYLOAD_OFFSET]));
 //				System.err.println("ETX0: " + (0xFF & msg[pkgbp + pkgi + 1 + X80Pro.PACKAGE_DATA_PAYLOAD_OFFSET]));
 //				System.err.println("ETX1: " + (0xFF & msg[pkgbp + pkgi + 2 + X80Pro.PACKAGE_DATA_PAYLOAD_OFFSET]));
 //				System.err.println("len: " + len);
 //				System.err.println("Package Overflowed Packet");
 //				System.err.println("currentPackageLength: " + currentPackageLength);
 //				System.err.println("pkgi: " + pkgi);
 //				remain = currentPackageLength - pkgi;
 //				System.err.println("Bytes Remaining: " + remain);
 //			}
 //		}
 //	}
 	
 	public void attachShutdownHook()
 	{
 		Runtime.getRuntime().addShutdownHook(new Thread(this));
 	}
 	
 	public void shutdown()
 	{
 		System.err.println("Shutting down robot");
 		
 		socket.send(PMS5005.suspendDCMotor((byte) 0));
 		socket.send(PMS5005.suspendDCMotor((byte) 1));
 		socket.send(PMS5005.suspendDCMotor((byte) 2));
 		socket.send(PMS5005.suspendDCMotor((byte) 3));
 		socket.send(PMS5005.suspendDCMotor((byte) 4));
 		socket.send(PMS5005.suspendDCMotor((byte) 5));
 		
 		socket.close();
 	}
 	
 	public void run()
 	{
 		this.suspendAllDCMotors();
 		this.lowerHead();
 		socket.close();
 	}
 	
 	/**
 	 * analog to digital conversion
 	 * 
 	 * @param ADValue
 	 * @return distance value in meters
 	 */
 	public static double AD2Distance(int ADValue)
 	{
 		double distance = 0;
 		
 		if (ADValue <= 0 || 4095 < ADValue)
 		{
 			distance = 0;
 		}
 		else
 		{
 			distance = 21.6 / ((double) (ADValue) * 3.0 / 4028 - 0.17);
 			
 			if (80 < distance)
 			{
 				distance = 0.81;
 			}
 			else if (distance < 10)
 			{
 				distance = 0.09;
 			}
 			else
 			{
 				distance = distance / 100.0;
 			}
 		}
 		
 		return distance;
 	}
 	
 	public void resetHead()
 	{
 		System.err.println("Reset Head");
 		socket.send(PMS5005.setAllServoPulses((short) SERVO0_INI, (short) SERVO1_INI, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL));
 	}
 	
 	public void lowerHead()
 	{
 		System.err.println("Lower Head");
 		socket.send(PMS5005.setAllServoPulses((short) (SERVO0_INI/2 + 500), (short) (SERVO1_INI), (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL));
 	}
 	
 	public void resumeAllSensors()
 	{
 		System.err.println("Activating All Sensors");
 		
 		socket.send(PMS5005.enableCustomSensorSending());
 		socket.send(PMS5005.enableStandardSensorSending());
 		socket.send(PMS5005.enableMotorSensorSending());
 		
 		System.err.println("All Sensors Activated");
 	}
 	
 	public void suspendAllSensors()
 	{
 		System.err.println("Deactivating All Sensors");
 		
 		socket.send(PMS5005.disableCustomSensorSending());
 		socket.send(PMS5005.disableStandardSensorSending());
 		socket.send(PMS5005.disableMotorSensorSending());
 		
 		System.err.println("All Sensors Deactivation");
 	}
 	
 	public void motorSensorRequest(int packetNumber)
 	{
 		socket.send(PMS5005.motorSensorRequest((short) packetNumber));
 	}
 	
 	public void standardSensorRequest(int packetNumber)
 	{
 		socket.send(PMS5005.standardSensorRequest((short) packetNumber));
 	}
 	
 	public void customSensorRequest(int packetNumber)
 	{
 		socket.send(PMS5005.customSensorRequest((short) packetNumber));
 	}
 	
 	public void allSensorRequest(int packetNumber)
 	{
 		socket.send(PMS5005.allSensorRequest((short) packetNumber));
 	}
 	
 	public void enableMotorSensorSending()
 	{
 		socket.send(PMS5005.enableMotorSensorSending());
 	}
 	
 	public void enableStandardSensorSending()
 	{
 		socket.send(PMS5005.enableStandardSensorSending());
 	}
 	
 	public void enableCustomSensorSending()
 	{
 		socket.send(PMS5005.enableCustomSensorSending());
 	}
 	
 	public void enableAllSensorSending()
 	{
 		socket.send(PMS5005.enableAllSensorSending());
 	}
 	
 	public void disableMotorSensorSending()
 	{
 		socket.send(PMS5005.disableMotorSensorSending());
 	}
 	
 	public void disableStandardSensorSending()
 	{
 		socket.send(PMS5005.disableStandardSensorSending());
 	}
 	
 	public void disableCustomSensorSending()
 	{
 		socket.send(PMS5005.disableCustomSensorSending());
 	}
 	
 	public void disableAllSensorSending()
 	{
 		socket.send(PMS5005.disableAllSensorSending());
 	}
 	
 	public void setMotorSensorPeriod(int timePeriod)
 	{
 		socket.send(PMS5005.setMotorSensorPeriod((short) timePeriod));
 	}
 	
 	public void setStandardSensorPeriod(int timePeriod)
 	{
 		socket.send(PMS5005.setStandardSensorPeriod((short) timePeriod));
 	}
 	
 	public void setCustomSensorPeriod(int timePeriod)
 	{
 		socket.send(PMS5005.setCustomSensorPeriod((short) timePeriod));
 	}
 	
 	public void setAllSensorPeriod(int timePeriod)
 	{
 		socket.send(PMS5005.setAllSensorPeriod((short) timePeriod));
 	}
 	
 	public double getSonarRange(int channel)
 	{
 		return standardSensorData.sonarDistance[channel] / 100.0;
 	}
 	
 	public double getIRRange(int channel)
 	{
 		double result = -1;
 		if (0 == channel)
 		{
 			result = AD2Distance(standardSensorData.infraredRangeAD);
 		}
 		else if (1 <= channel)
 		{
 			result = AD2Distance(customSensorData.customAD[1 + channel]);
 		}
 		return result;
 	}
 	
 	public int getHumanAlarm(int channel)
 	{
 		return standardSensorData.humanAlarm[channel];
 	}
 	
 	public int getHumanMotion(int channel)
 	{
 		return standardSensorData.motionDetect[channel];
 	}
 	
 	public int getTiltingX()
 	{
 		return standardSensorData.tiltingAD[X];
 	}
 	
 	public int getTiltingY()
 	{
 		return standardSensorData.tiltingAD[Y];
 	}
 	
 	public int getOverheat(int channel)
 	{
 		return standardSensorData.overheatAD[channel];
 	}
 	
 	public int getAmbientTemperature()
 	{
 		return standardSensorData.temperatureAD;
 	}
 	
 	public int getIRCode(int index)
 	{
 		return standardSensorData.infraredCommand[index];
 	}
 	
 	public int getMainboardBatteryVoltageAD()
 	{
 		return standardSensorData.mainboardBatteryVoltageAD_0to9V;
 	}
 	
 	public int getMotorBatteryVoltageAD()
 	{
 		return standardSensorData.motorBatteryVoltageAD_0to24V;
 	}
 	
 	public int getServoBatteryVoltageAD()
 	{
 		return standardSensorData.servoBatteryVoltageAD_0to9V;
 	}
 	
 	public int getReferenceVoltage()
 	{
 		return standardSensorData.referenceVoltageAD_Vcc;
 	}
 	
 	public int getPotentiometerVoltage()
 	{
 		return standardSensorData.potentiometerVoltageAD_Vref;
 	}
 	
 	public int getPotentiometer(int channel)
 	{
 		return motorSensorData.potentiometerAD[channel];
 	}
 	
 	public int getMotorCurrent(int channel)
 	{
 		return motorSensorData.motorCurrentAD[channel];
 	}
 	
 	public int getEncoderDirection(int channel)
 	{
 		return motorSensorData.encoderDirection[channel];
 	}
 	
 	public int getEncoderPulse(int channel)
 	{
 		return motorSensorData.encoderPulse[channel];
 	}
 	
 	public int getEncoderSpeed(int channel)
 	{
 		return motorSensorData.encoderSpeed[channel];
 	}
 	
 	public int getCustomAD(int channel)
 	{
 		return customSensorData.customAD[channel];
 	}
 	
 	public int getCustomDIn(int channel)
 	{
 		return 0;
 	}
 	
 	public void setCustomDOut(int ival)
 	{
 		socket.send(PMS5005.setCustomDOut((byte) ival));
 	}
 	
 	public void setMotorPolarity(int channel, int polarity)
 	{
 		socket.send(PMS5005.setMotorPolarity((byte) channel, (byte) polarity));
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void enableDCMotor(int channel)
 	{
 		socket.send(PMS5005.enableDCMotor((byte) channel));
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void disableDCMotor(int channel)
 	{
 		socket.send(PMS5005.disableDCMotor((byte) channel));
 	}
 	
 	public void resumeDCMotor(int channel)
 	{
 		socket.send(PMS5005.resumeDCMotor((byte) channel));
 	}
 
 	public void resumeAllDCMotors()
 	{
 		socket.send(PMS5005.resumeDCMotor((byte) 0));
 		socket.send(PMS5005.resumeDCMotor((byte) 1));
 		socket.send(PMS5005.resumeDCMotor((byte) 2));
 		socket.send(PMS5005.resumeDCMotor((byte) 3));
 		socket.send(PMS5005.resumeDCMotor((byte) 4));
 		socket.send(PMS5005.resumeDCMotor((byte) 5));
 	}
 
 	public void resumeBothDCMotors()
 	{
 		System.err.println("Resume Both DC Motors");
 		socket.send(PMS5005.resumeDCMotor((byte) 0));
 		socket.send(PMS5005.resumeDCMotor((byte) 1));
 	}
 	
 	public void suspendDCMotor(int channel)
 	{
 		socket.send(PMS5005.suspendDCMotor((byte) channel));
 	}
 	
 	public void suspendAllDCMotors()
 	{
 		socket.send(PMS5005.suspendDCMotor((byte) 0));
 		socket.send(PMS5005.suspendDCMotor((byte) 1));
 		socket.send(PMS5005.suspendDCMotor((byte) 2));
 		socket.send(PMS5005.suspendDCMotor((byte) 3));
 		socket.send(PMS5005.suspendDCMotor((byte) 4));
 		socket.send(PMS5005.suspendDCMotor((byte) 5));
 	}
 	
 	public void suspendBothDCMotors()
 	{
 		System.err.println("Suspend Both DC Motors");
 		socket.send(PMS5005.suspendDCMotor((byte) 0));
 		socket.send(PMS5005.suspendDCMotor((byte) 1));
 	}
 	
 	public void setDCMotorPositionControlPID(int channel, int kp, int kd, int ki_x100)
 	{
 		socket.send(PMS5005.setDCMotorPositionControlPID((byte) channel, (short) kp, (short) kd, (short) ki_x100));
 	}
 	
 	public void setDCMotorSensorFilter(int channel, int filterMethod)
 	{
 		socket.send(PMS5005.setDCMotorSensorFilter((byte) channel, (short) filterMethod));
 	}
 	
 	public void setDCMotorSensorUsage(int channel, int sensorType)
 	{
 		socket.send(PMS5005.setDCMotorSensorUsage((byte) channel, (byte) sensorType));
 	}
 	
 	public void setBothDCMotorSensorUsages(int sensorTypeL, int sensorTypeR)
 	{
 		socket.send(PMS5005.setDCMotorSensorUsage((byte) 0, (byte) sensorTypeL));
 		socket.send(PMS5005.setDCMotorSensorUsage((byte) 0, (byte) sensorTypeR));
 	}
 	
 	public void setBothDCMotorSensorUsages(int sensorType)
 	{
 		socket.send(PMS5005.setDCMotorSensorUsage((byte) 0, (byte) sensorType));
 		socket.send(PMS5005.setDCMotorSensorUsage((byte) 0, (byte) sensorType));
 	}
 	
 	public void setBothDCMotorControlModes(int controlMode)
 	{
 		socket.send(PMS5005.setDCMotorControlMode((byte) 0, (byte) controlMode));
 		socket.send(PMS5005.setDCMotorControlMode((byte) 1, (byte) controlMode));
 	}
 	
 	public void setDCMotorControlMode(int channel, int controlMode)
 	{
 		socket.send(PMS5005.setDCMotorControlMode((byte) channel, (byte) controlMode));
 	}
 	
 	public void setBothDCMotorControlModes(int controlModeL, int controlModeR)
 	{
 		socket.send(PMS5005.setDCMotorControlMode((byte) 0, (byte) controlModeL));
 		socket.send(PMS5005.setDCMotorControlMode((byte) 1, (byte) controlModeR));
 	}
 	
 	public void setDCMotorPosition(int channel, int pos, int timePeriod)
 	{
 		socket.send(PMS5005.setDCMotorPosition((byte) channel, (short) pos, (short) timePeriod));
 	}
 	
 	public void setDCMotorPosition(int channel, int pos)
 	{
 		socket.send(PMS5005.setDCMotorPosition((byte) channel, (short) pos));
 	}
 	
 	public void setDCMotorPulse(int channel, int pulseWidth, int timePeriod)
 	{
 		socket.send(PMS5005.setDCMotorPulse((byte) channel, (short) pulseWidth, (short) timePeriod));
 	}
 	
 	public void setDCMotorPulse(int channel, int pulseWidth)
 	{
 		socket.send(PMS5005.setDCMotorPulse((byte) channel, (short) pulseWidth));
 	}
 	
 	public void setAllDCMotorPositions(int pos0, int pos1, int pos2, int pos3, int pos4, int pos5, int timePeriod)
 	{
 		socket.send(PMS5005.setAllDCMotorPositions((short) pos0, (short) pos1, (short) pos2, (short) pos3, (short) pos4, (short) pos5,
 				(short) timePeriod));
 	}
 	
 	public void setAllDCMotorPositions(int pos0, int pos1, int pos2, int pos3, int pos4, int pos5)
 	{
 		socket.send(PMS5005.setAllDCMotorPositions((short) pos0, (short) pos1, (short) pos2, (short) pos3, (short) pos4, (short) pos5));
 	}
 	
 	public void setAllDCMotorVelocities(int v0, int v1, int v2, int v3, int v4, int v5, int timePeriod)
 	{
 		socket.send(PMS5005.setAllDCMotorVelocities((short) v0, (short) v1, (short) v2, (short) v3, (short) v4, (short) v5,
 				(short) timePeriod));
 	}
 	
 	public void setAllDCMotorVelocities(int v0, int v1, int v2, int v3, int v4, int v5)
 	{
 		socket.send(PMS5005.setAllDCMotorVelocities((short) v0, (short) v1, (short) v2, (short) v3, (short) v4, (short) v5));
 	}
 	
 	public void setAllDCMotorPulses(int p0, int p1, int p2, int p3, int p4, int p5, int timePeriod)
 	{
 		socket.send(PMS5005.setAllDCMotorPulses((short) p0, (short) p1, (short) p2, (short) p3, (short) p4, (short) p5,
 				(short) timePeriod));
 	}
 	
 	public void setAllDCMotorPulses(int p0, int p1, int p2, int p3, int p4, int p5)
 	{
 		socket.send(PMS5005.setAllDCMotorPulses((short) p0, (short) p1, (short) p2, (short) p3, (short) p4, (short) p5));
 	}
 	
 	public void enableServo(int channel)
 	{
 		socket.send(PMS5005.enableServo((byte) channel));
 	}
 	
 	public void disableServo(int channel)
 	{
 		socket.send(PMS5005.disableServo((byte) channel));
 	}
 	
 	public void setServoPulse(int channel, int pos, int timePeriod)
 	{
 		socket.send(PMS5005.setServoPulse((byte) channel, (short) pos, (short) timePeriod));
 	}
 	
 	public void setServoPulse(int channel, int pulseWidth)
 	{
 		socket.send(PMS5005.setServoPulse((byte) channel, (short) pulseWidth));
 	}
 	
 	public void setAllServoPulses(int p0, int p1, int p2, int p3, int p4, int p5, int timePeriod)
 	{
 		socket.send(PMS5005.setAllServoPulses((short) p0, (short) p1, (short) p2, (short) p3, (short) p4, (short) p5, (short) timePeriod));
 	}
 	
 	public void setAllServoPulses(int p0, int p1, int p2, int p3, int p4, int p5)
 	{
 		socket.send(PMS5005.setAllServoPulses((short) p0, (short) p1, (short) p2, (short) p3, (short) p4, (short) p5));
 	}
 	
 	public void setLCDDisplayPMS(String bmpFileName)
 	{
 		/* TODO slice buffer */
 		
 		/* TODO send buffer */
 		
 		/*
 		for (int i = 0; i < 16; ++i)
 		{
 			socket.send(PMS5005.setLCDDisplayPMS(frameNumber, frameContent));
 		}
 		*/
 	}
 	
 	public void setDCMotorVelocityControlPID(byte channel, int kp, int kd, int ki)
 	{
 		socket.send(PMS5005.setDCMotorVelocityControlPID(channel, kp, kd, ki));
 	}
 
 	public void setBothDCMotorPositions(int pos0, int pos1, int timePeriod) 
 	{
 		socket.send(PMS5005.setAllDCMotorPositions((short) pos0, (short) -pos1, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) timePeriod));
 	}
 
 	public void setBothDCMotorPositions(int pos0, int pos1) 
 	{
 		socket.send(PMS5005.setAllDCMotorPositions((short) pos0, (short) -pos1, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL));
 	}
 
 	public void setBothDCMotorVelocities(int v0, int v1, int timePeriod) 
 	{
 		socket.send(PMS5005.setAllDCMotorVelocities((short) v0, (short) -v1, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) timePeriod));
 	}
 
 	public void setBothDCMotorVelocities(int v0, int v1) 
 	{
 		socket.send(PMS5005.setAllDCMotorVelocities((short) v0, (short) -v1, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL));
 	}
 
 	public void setBothDCMotorPulses(int p0, int p1, int timePeriod) 
 	{
 		socket.send(PMS5005.setAllDCMotorVelocities((short) p0, (short) -p1, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) timePeriod));
 	}
 
 	public void setBothDCMotorPulses(int p0, int p1) 
 	{
 		socket.send(PMS5005.setAllDCMotorPulses((short) p0, (short) -p1, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL));
 	}
 
 	public void setBothServoPulses(int p0, int p1, int timePeriod) 
 	{
 		socket.send(PMS5005.setAllDCMotorPulses((short) p0, (short) -p1, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) timePeriod));
 	}
 	
 	public void setBothServoPulses(int p0, int p1) 
 	{
 		socket.send(PMS5005.setAllServoPulses((short) p0, (short) -p1, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL));
 	}
 	
 	public void startAudioRecording(int voiceSegmentLength)
 	{
 		socket.send(PMB5010.startAudioRecording((short) voiceSegmentLength));
 	}
 	
 	public void stopAudioRecording()
 	{
 		socket.send(PMB5010.stopAudioRecording());
 	}
 	
 	public void startAudioPlayback(int sampleLength)
 	{
 		for (byte seq = 0; seq < sampleLength / 0xff; ++seq)
 		{
 			socket.send(PMB5010.startAudioPlayback((short) sampleLength, seq));
 		}
 		
 		socket.send(PMB5010.startAudioPlayback((short) sampleLength, (byte) (0xff & 0xff)));
 	}
 	
 	public void stopAudioPlayback()
 	{
 		socket.send(PMB5010.stopAudioPlayback());
 	}
 	
 	public void continueAudioPlayback(byte[] audioSegmentEncodedInPCM)
 	{
 		socket.send(PMB5010.continueAudioPlayback(audioSegmentEncodedInPCM));
 	}
 	
 	public void takePhoto()
 	{
 		socket.send(PMB5010.takePhoto());
 	}
 	
 	/**
 	 * This method is taken directly from the DrRobot Java Demo Project
 	 * 
 	 * Method sets Position Control Mode, and turns robot through theta radians in time seconds
 	 * @param theta angle to turn through in radians
 	 * @param time time to turn in seconds
 	 */
 	public void turnStep(double theta, int milliseconds)
 	{
 	    socket.send(PMS5005.setDCMotorControlMode((byte) L, (byte) X80Pro.CONTROL_MODE_POSITION));
 	    socket.send(PMS5005.setDCMotorControlMode((byte) R, (byte) X80Pro.CONTROL_MODE_POSITION));
 	    
 	    //int diffEncoder = (int)((1200*0.2875*theta)/(4*Math.PI*0.085));
 	    //int diffEncoder = (int)((WHEEL_ENCODER_CIRCUMFERENCE*WHEEL_DISPLACEMENT*theta)/(4*Math.PI*WHEEL_RADIUS));
 	    int diffEncoder = (int)((X80Pro.ROTATE_ROBOT_ENCODER_COUNT*WHEEL_DISPLACEMENT*theta)/(2*Math.PI*WHEEL_RADIUS));
 	    
 	    int leftPosition = getEncoderPulse(0) - diffEncoder;
 	    if (leftPosition < 0) 
 	    {
 	    	leftPosition = 32767 + leftPosition;
 	    }
 	    else if (32767 < leftPosition)
 	    {
 	    	leftPosition = leftPosition - 32767;
 	    }
 	    
 	    int rightPosition = getEncoderPulse(1) - diffEncoder;
 	    if (rightPosition < 0)
 	    {
 	    	rightPosition = 32767 + rightPosition;
 	    }
 	    else if (32767 < rightPosition)
 	    {
 	    	rightPosition = rightPosition - 32767;
 	    }
 	    
 	    setDCMotorPositionControlPID(L, 1000, 5, 10000);
 	    setDCMotorPositionControlPID(R, 1000, 5, 10000);
 	    
 	    setAllDCMotorPositions((short)leftPosition, (short)rightPosition, 
 	    		(short) NO_CONTROL, (short) NO_CONTROL, (short) NO_CONTROL, (short) NO_CONTROL, (short) milliseconds);
 	}
 	
 	public void runStepPWM(double runDis, int milliseconds)
 	{
         //the robot will go forward the rundistance
         int diffEncoder = (int)((runDis / (2 * Math.PI * WHEEL_RADIUS)) * ROTATE_ROBOT_ENCODER_COUNT);
         
         int LeftTarget = motorSensorData.encoderPulse[L] + diffEncoder;
         if (LeftTarget < 0) 
         {
             LeftTarget = 32767 + LeftTarget;
         }
         else if (32767 < LeftTarget) 
         {
             LeftTarget = LeftTarget - 32767;
         }
         
         int RightTarget = motorSensorData.encoderPulse[R] - diffEncoder;
         if (32767 < RightTarget)
         {
             RightTarget = RightTarget - 32767;
         }
         else if(RightTarget < 0)
         {
             RightTarget = 32767 + RightTarget;
         }
         
         socket.send(PMS5005.setDCMotorControlMode((byte) L, (byte) CONTROL_MODE_PWM));
         socket.send(PMS5005.setDCMotorControlMode((byte) R, (byte) CONTROL_MODE_PWM));
         
         //socket.send(PMS5005.setDCMotorPositionControlPID((byte) L, (short) 1000, (short) 30, (short) 2000));
         //socket.send(PMS5005.setDCMotorPositionControlPID((byte) R, (short) 1000, (short) 30, (short) 2000));
         
         socket.send(PMS5005.setAllDCMotorPulses(
         		(short) (PWM_N + PWM_O + 8*DUTY_CYCLE_UNIT), 
         		(short) -(PWM_N + PWM_O + 8*DUTY_CYCLE_UNIT), 
         		(short) NO_CONTROL, (short) NO_CONTROL, (short) NO_CONTROL, (short) NO_CONTROL, (short) milliseconds));
         while (motorSensorData.encoderPulse[L] < LeftTarget - X80Pro.ACCEPTABLE_ENCODER_DEVIATION || 
         		motorSensorData.encoderPulse[R] < RightTarget - X80Pro.ACCEPTABLE_ENCODER_DEVIATION)
         {
         	// Busy wait
         }
 	}
 	
 	/**
 	 * This method is taken directly from the DrRobot Java Demo Project
 	 * 
 	 * @param runDis
 	 */
     public void runStep(double runDis, int milliseconds) 
     {
         //the robot will go forward the rundistance
         int diffEncoder = (int)((runDis / (2 * Math.PI * WHEEL_RADIUS)) * ROTATE_ROBOT_ENCODER_COUNT);
         
         int LeftTarget = motorSensorData.encoderPulse[L] + diffEncoder;
         if (LeftTarget < 0) 
         {
             LeftTarget = 32767 + LeftTarget;
         }
         else if (32767 < LeftTarget) 
         {
             LeftTarget = LeftTarget - 32767;
         }
         
         int RightTarget = motorSensorData.encoderPulse[R] - diffEncoder;
         if (32767 < RightTarget)
         {
             RightTarget = RightTarget - 32767;
         }
         else if(RightTarget < 0)
         {
             RightTarget = 32767 + RightTarget;
         }
         
         socket.send(PMS5005.setDCMotorControlMode((byte) L, (byte) CONTROL_MODE_POSITION));
         socket.send(PMS5005.setDCMotorControlMode((byte) R, (byte) CONTROL_MODE_POSITION));
         
         socket.send(PMS5005.setDCMotorPositionControlPID((byte) L, (short) 1000, (short) 30, (short) 2000));
         socket.send(PMS5005.setDCMotorPositionControlPID((byte) R, (short) 1000, (short) 30, (short) 2000));
         
         socket.send(PMS5005.setAllDCMotorPositions((short) LeftTarget, (short) RightTarget, 
         		(short) NO_CONTROL, (short) NO_CONTROL, (short) NO_CONTROL, (short) NO_CONTROL, (short) milliseconds));
     }
 	
 	public int turnThetaRadianStep(double theta, int milliseconds)
 	{
 		//@ post: Robot has turned an angle theta in radians
 		
 	    int diffEncoder = (int)((WHEEL_CIRCUMFERENCE_ENCODER_COUNT*WHEEL_DISPLACEMENT*theta)/(4*Math.PI*WHEEL_RADIUS));
 	    
 	    // TODO: cross calling
 	    int leftPulseWidth = getEncoderPulse(L) - diffEncoder;
 		if (leftPulseWidth < 0)
 		{
 			leftPulseWidth = 32768 + leftPulseWidth;
 		}
 		else if (32767 < leftPulseWidth)
 		{
 			leftPulseWidth = leftPulseWidth - 32767;
 		}
 		
 		// TODO: cross calling
 		int rightPulseWidth = getEncoderPulse(R) - diffEncoder;
 		if (rightPulseWidth < 0) 
 		{
 			rightPulseWidth = 32768 + rightPulseWidth;
 		}
 		else if (32767 < rightPulseWidth)
 		{
 			rightPulseWidth = rightPulseWidth - 32767;
 		}
 		
 		socket.send(PMS5005.setDCMotorControlMode((byte) L, (byte) CONTROL_MODE_PWM));
 		socket.send(PMS5005.setDCMotorControlMode((byte) R, (byte) CONTROL_MODE_PWM));
 		
 		socket.send(PMS5005.setDCMotorPositionControlPID((byte) L, (short) 1000, (short) 5, (short) 10000));
 		socket.send(PMS5005.setDCMotorPositionControlPID((byte) R, (short) 1000, (short) 5, (short) 10000));
 		
 		socket.send(PMS5005.setAllDCMotorPulses((short) leftPulseWidth, (short) -rightPulseWidth, 
 				(short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) milliseconds));
 		
 		return leftPulseWidth;
 	}
 	
 	public void setBothDCMotorPulsePercentages(int leftPulseWidth, int rightPulseWidth)
 	{
 		double[] power = new double[2];
 		
 		power[L] = leftPulseWidth/100.0; // 0.0 <= x <= 1.0
 		power[R] = rightPulseWidth/100.0; // 0.0 <= x <= 1.0
 		
 		System.err.println("translatedLeftPulseWidth: " + power[L]);
 		System.err.println("translatedRightPulseWidth: " + power[R]);
 		
 		// if > 100% power output requested, cap the motor power.
 //		if (1 < translatedLeftPulseWidth || 1 < translatedRightPulseWidth)
 //		{
 //			if (translatedLeftPulseWidth <= translatedRightPulseWidth)
 //			{
 //				translatedLeftPulseWidth = translatedLeftPulseWidth/translatedRightPulseWidth; 
 //				translatedRightPulseWidth = 1;
 //			}
 //			else
 //			{
 //				translatedRightPulseWidth = translatedRightPulseWidth/translatedLeftPulseWidth; 
 //				translatedLeftPulseWidth = 1;
 //			}
 //		}
 		
 		if (0 < power[L])
 		{
 			// 16384 + 8000 + (scaled) power[L]: increase left motor velocity.
 			power[L] = PMS5005.PWM_N + PMS5005.PWM_O + power[L]*(PMS5005.DUTY_CYCLE_UNIT/3.0);
 		}
 		else if (power[L] < 0)
 		{
 			// 16384 - 8000 + (scaled) power[L]: reduce left motor velocity.
 			power[L] = PMS5005.PWM_N - PMS5005.PWM_O - power[L]*(PMS5005.DUTY_CYCLE_UNIT/3.0); 
 		}
 		else
 		{
 			// neutral PWM setting, 0% duty cycle, let left motor sit idle
 			power[L] = PMS5005.PWM_N;
 		}
 		
 		//if (power[L] > L_MAX_PWM)
 		//{
 		// L_MAX_PWM = 32767, +100% duty cycle
 		//	power[L] = L_MAX_PWM;
 		//}
 		//else if (power[L] < N_PWM)
 		//{
 		// stand still, 0% duty cycle
 		//	power[L] = N_PWM;
 		//}
 		
 		if (0 < power[R])
 		{
 			power[R] = PMS5005.PWM_N - PMS5005.PWM_O - power[R]*(PMS5005.DUTY_CYCLE_UNIT/3.0); // reverse: 16384 - 8000 - power[R]
 		}
 		else if (power[R] < 0)
 		{
 			power[R] = PMS5005.PWM_N + PMS5005.PWM_O + power[R]*(PMS5005.DUTY_CYCLE_UNIT/3.0); // reverse: 16384 + 8000 + rightPulseWidth
 		}
 		else
 		{
 			power[R] = PMS5005.PWM_N; // neutral PWM setting, 0% duty cycle
 		}
 		//if (power[R] < R_MAX_PWM) power[R] = R_MAX_PWM; // yes, < .. negative threshold @ -100% duty cycle
 		//else if (power[R] > N_PWM) power[R] = N_PWM; // stand still, 0% duty cycle
 		//robot.dcMotorPwmNonTimeCtrlAll((int)power[L], (int)power[R], NO_CONTROL, NO_CONTROL, NO_CONTROL, NO_CONTROL);
 		
 		System.err.println("translatedLeftPulseWidth: " + power[L]);
 		System.err.println("translatedRightPulseWidth: " + power[R]);
 		
 		socket.send(PMS5005.setAllDCMotorPulses((short) power[L], (short) power[R], (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL, (short) NO_CTRL));
 	//} else Robot.DcMotorPwmNonTimeCtrAll((short)N_PWM, (short)N_PWM, NO_CONTROL, NO_CONTROL, NO_CONTROL, NO_CONTROL); Sleep(MICRO_DELAY);
 	}
 }
