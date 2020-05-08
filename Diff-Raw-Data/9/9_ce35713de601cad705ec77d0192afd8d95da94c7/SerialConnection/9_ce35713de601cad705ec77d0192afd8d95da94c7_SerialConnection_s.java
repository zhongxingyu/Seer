 /*
  * Andrew Darwin
  * www.adarwin.com
  * github.com/adarwin
  * SUNY Oswego
  */
 
 package com.adarwin.edrum;
 
 import gnu.io.CommPortIdentifier;
 import gnu.io.SerialPort;
 import gnu.io.SerialPortEvent;
 import gnu.io.SerialPortEventListener;
 import gnu.io.UnsupportedCommOperationException;
 import gnu.io.PortInUseException;
 import gnu.io.NoSuchPortException;
 import java.util.Enumeration;
 import java.io.BufferedReader;
 import java.util.TooManyListenersException;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.InputStream;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 class SerialConnection {
     protected static final Logger logger = Logger.getLogger(
                                              SerialConnection.class.getName());
     private static final String validPortNames[] = {
         "/dev/tty.usbserial-A9007UX1",
         "/dev/ttyUSB0",
         "COM3"
     };
     private int dataRate;
     private SerialPort serialPort;
     private CommPortIdentifier portIdentifier;
     private InputStream inputStream;
     private OutputStream outputStream;
     private boolean inGraphMode = false;
 
     private static int NORMAL_STROKE = 128;
     protected static int GRAPH_STROKE = 129;
     protected static int GRAPH_DATA = 130;
     private static int SET_GRAPH_MODE = 131;
     private static int SET_THRESHOLD = 132;
     private static int SET_SENSITIVITY = 133;
     private static int SET_TIMEOUT = 134;
 
 
     protected boolean setPortIdentifier(String portName) {
         try {
             logger.log(Level.INFO, "Setting port identifier to " + portName);
             portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
             openSerialPort();
         } catch (NoSuchPortException ex) {
             logger.log(Level.SEVERE, "No such port exists");
             return false;
         }
         return true;
     }
     protected List<CommPortIdentifier> getPortList() {
         List<CommPortIdentifier> portList = new ArrayList<CommPortIdentifier>();
         @SuppressWarnings("unchecked")
         Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
         // First, find an instance of serial port as set in portNames.
         while (portEnum.hasMoreElements()) {
             CommPortIdentifier currentPortID = portEnum.nextElement();
             portList.add(currentPortID);
         }
         return portList;
     }
     protected boolean setTimeout(int drum, int value) {
         return setValueOnArduino(SET_TIMEOUT, drum, value);
     }
     protected boolean setThreshold(int drum, int value) {
         return setValueOnArduino(SET_THRESHOLD, drum, value);
     }
     protected boolean setSensitivity(int drum, int value) {
         return setValueOnArduino(SET_SENSITIVITY, drum, value/10);
     }
     protected boolean requestGraphMode(int drum, boolean value) {
        logger.log(Level.INFO, "Requesting graph mode for drum: " + drum);
         byte graphModeValue;
         if (value) {
             graphModeValue = 0x01;
         } else {
             graphModeValue = 0x00;
         }
         boolean successful = setValueOnArduino(SET_GRAPH_MODE, drum,
                                                graphModeValue);
         if (successful) {
             inGraphMode = value;
         } else {
             logger.log(Level.WARNING, "Failed to set graph mode on arduino");
         }
         return successful;
     }
     private boolean setValueOnArduino(int variable, int drum, int value) {
         boolean successful = false;
         if (outputStream != null) {
             if (value > 255) {
                 // Split into two bytes
             }
             byte[] output = new byte[3];
             output[0] = (byte)variable;
             output[1] = (byte)drum;
             output[2] = (byte)value;
             successful = sendByteArrayToArduino(output);
         }
         return successful;
     }
     private boolean sendByteArrayToArduino(byte[] byteArray) {
         boolean successful = false;
         if (outputStream != null) {
             try {
                 outputStream.write(byteArray);
                 successful = true;
             } catch (IOException ex) {
                 logger.log(Level.SEVERE, "Failed to write data to serial port");
             }
         }
         return successful;
     }
     private boolean openSerialPort() {
         try {
             // Open serial port and use class name for the appName
             serialPort = (SerialPort) portIdentifier.open(
                                                  this.getClass().getName(),
                                                  2000);
             logger.log(Level.INFO, "Made successfully opened portIdentifier");
             // Set port parameters
             serialPort.setSerialPortParams(dataRate, SerialPort.DATABITS_8,
                                            SerialPort.STOPBITS_1,
                                            SerialPort.PARITY_NONE);
             logger.log(Level.INFO, "Successfully set serial port params");
             // Open the streams
             inputStream = serialPort.getInputStream();
             logger.log(Level.INFO, "Got input Stream");
             outputStream = serialPort.getOutputStream();
             logger.log(Level.INFO, "Got output stream");
 
 
             // Add event listeners
             serialPort.addEventListener(new SerialPortEventListener() {
                 public void serialEvent(SerialPortEvent event) {
                     if (event.getEventType() ==
                         SerialPortEvent.DATA_AVAILABLE) {
                         try {
                             if (inGraphMode) {
                                 int currentByte;
                                 while (inputStream.available() > 0) {
                                     currentByte = inputStream.read();
                                     if (currentByte == GRAPH_STROKE) {
                                         /*
                                         Read the next 3
                                         Index 0 = articulation / MIDI note
                                         Index 1 = velocity
                                         Index 2 = time since last datum
                                         */
                                         int[] data = readSerialData(inputStream, new int[3], false);
                                         System.out.println("Adding graph stroke with value: " + data[1]);
                                         AceDrums.reportStroke((byte)data[0],
                                                               (byte)data[1]);
                                         AceDrums.reportNewDatum(GRAPH_STROKE,
                                                                 data[1],
                                                                 data[2]);
                                     } else if (currentByte == GRAPH_DATA) {
                                         /*
                                         Read the next 2
                                         Index 0 = velocity
                                         Index 1 = time since last datum
                                         */
                                         // Read the next 2
                                         int[] data = readSerialData(inputStream, new int[2], true);
                                         AceDrums.reportNewDatum(GRAPH_DATA, data[0], data[1]);
                                     }
                                 }
                             } else {
                                 int[] serialData = readSerialData(inputStream,
                                                                   new int[4], false);
                                 if (serialData[0] == NORMAL_STROKE) {
                                     AceDrums.reportStroke((byte)serialData[1],
                                                           (byte)serialData[2]);
                                 } else if (serialData[0] == GRAPH_STROKE) {
                                     logger.log(Level.WARNING, "Encountered a "+
                                                "graph stroke when it should "+
                                                "have been a normal stroke");
                                 } else if (serialData[0] == GRAPH_DATA) {
                                     logger.log(Level.WARNING, "Encountered " +
                                                "graph data when it should " +
                                                "have been a normal stroke");
                                 }
                             }
                         } catch (IOException ex) {
                             logger.log(Level.SEVERE, "Failed to read data " +
                                                      "from serial port");
                         }
                     }
                 }
             });
             logger.log(Level.INFO, "Added serial event listener");
             serialPort.notifyOnDataAvailable(true);
             logger.log(Level.INFO, "Set serial port to notify when data is " +
                                    "available");
         } catch (PortInUseException ex) {
             logger.log(Level.SEVERE, "Port is currently in use");
             return false;
         } catch (UnsupportedCommOperationException ex) {
             logger.log(Level.SEVERE, "Unsuppored Comm Operation. Make sure " +
                                      "you are using a valid data rate");
             return false;
         } catch (IOException ex) {
             logger.log(Level.SEVERE, "IO Exception");
             return false;
         } catch (TooManyListenersException ex) {
             logger.log(Level.SEVERE, "Too many listeners");
             return false;
         }
         return true;
     }
     private int[] readSerialData(InputStream inputStream, int[] dataTargetArray, boolean zeroIsValid) {
         int lastIndex = dataTargetArray.length - 1;
         int dataIndex = 0;
         int currentDatum;
         try {
             while (inputStream.available() > 0 && dataIndex <= lastIndex) {
                 currentDatum = inputStream.read();
                 if (zeroIsValid || currentDatum != 0) {
                     dataTargetArray[dataIndex++] = currentDatum;
                 }
             }
         } catch (IOException ex) {
             logger.log(Level.SEVERE, "Failed to read data from serial port");
         }
         return dataTargetArray;
     }
     protected void closeSerialPort() {
         logger.log(Level.INFO, "Attempting to close serial port: " +
                                 serialPort);
         if (serialPort != null) {
             serialPort.removeEventListener();
             logger.log(Level.INFO, "Removed event listener from serial port: "
                                     + serialPort);
             serialPort.close();
             logger.log(Level.INFO, "Closed serial port: " + serialPort);
         } else {
             logger.log(Level.INFO, "Didn't need to close serial port: " +
                                     serialPort + " because it was null");
         }
     }
     SerialConnection() {
         dataRate = 57600;
         System.out.println("Constructing a serial connection...");
         List<CommPortIdentifier> portList = getPortList();
         // Obtain the correct port identifier
 
         if (portIdentifier == null) {
             // Failed to identify port
         } else {
             openSerialPort();
         }
 
     }
 }
