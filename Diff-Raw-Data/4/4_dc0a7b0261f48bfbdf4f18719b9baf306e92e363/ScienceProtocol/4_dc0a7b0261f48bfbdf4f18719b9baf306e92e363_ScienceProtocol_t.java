 package org.anhonesteffort.sciencebox.serial;
 
 /**
  * Programmer: rhodey
  * Date: 9/28/13
  */
 public class ScienceProtocol {
 
   private static final char CONTROL_CHANNEL_OFF = '0';
   private static final char CONTROL_CHANNEL_ON  = '1';
   private static final char CONTROL_COMMAND_END = 0x0A;
 
   public static final char CONTROL_CHANNEL_BLOWER         = '1';
   public static final char CONTROL_CHANNEL_HUMIDIFIER     = '0';
   public static final char CONTROL_CHANNEL_PELTIER_COOLER = '3';
   public static final char CONTROL_CHANNEL_PELTIER_HEATER = '2';
 
   protected static final char SENSOR_READ_SEPARATOR = ',';
   protected static final char SENSOR_READ_END       = 0x0A;
 
  public static final byte SENSOR_CHANNEL_TEMPERATURE = 0x01;
  public static final byte SENSOR_CHANNEL_HUMIDIFIER  = 0x00;
 
   public static byte[] turnOffChannel(char channel) {
     return new byte[] {(byte) channel, CONTROL_CHANNEL_OFF, CONTROL_COMMAND_END};
   }
 
   public static byte[] turnOnChannel(char channel) {
     return new byte[] {(byte) channel, CONTROL_CHANNEL_ON, CONTROL_COMMAND_END};
   }
 
 }
