 /**
  * UTS Remote Labs Wave Tank Rig Client. 
  *
  * @author Michael Diponio (mdiponio)
  * @date 9th Janurary 2012
  * 
  * - 15/5/2013 (DA) - Altered to store the values sent to the CRIO
  * and added getters to retrieve them. 
  */
 
 package au.edu.uts.eng.remotelabs.wavetank;
 
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Implements the TCP interface provided by the cRIO.
  */
 public class CRIOTcp
 {
     /** Number of analogue data channels. */
     public static final int NUM_AIN_CHANS = 17;
     
     /** String IP address of server. */
     private final String ip;
     
     /** Port number of server. */
     private final int port;
     
     /** Socket that connects to server. */
     private Socket socket;
     
     /** Input stream of server connection. */
     private DataInputStream in;
     
     /** Output stream of socket connection. */
     private DataOutputStream out;
     
     /** Analogue channel data. */
     private double ain[];
     
     /** Digital channel data. */
     private byte din;
     
     /** Analogue channel output **/
     private double[] aout;
     
     /** Digital channel output **/
     private boolean[] dout;
     
     /** Pump state **/
     private boolean pump;
     
     /** Inverter state **/
     private boolean inverter;
     
     /** Paddle speed **/
     private double speed;
     
     
     /** Rig state. */
     private byte rigState;
     
     /** Logger. */
     private final ILogger logger;
     
     public CRIOTcp(final String ip, final int port)
     {
         this.logger = LoggerFactory.getLoggerInstance();
                 
         this.ip = ip;
         this.port = port;
         
         this.ain = new double[NUM_AIN_CHANS];
     }
     
     /**
      * Connects to the cRIO server.
      * 
      * @return true if the cRIO write back connected
      * @throws IOException error connecting to cRIO
      */
     public synchronized boolean connect() throws IOException
     {
         this.socket = new Socket(this.ip, this.port);
        this.socket.setSoTimeout(50000);
         
         this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
         this.out = new DataOutputStream(this.socket.getOutputStream());
         
         int len = this.in.readInt();
         char strCh[] = new char[len];
         
         for (int i = 0; i < len; i++) 
         {
             strCh[i] = (char) this.in.readByte();
         }
         return "connected".equals(String.valueOf(strCh));
     }
     
     /**
      * Checks whether a connection to the cRIO server is active.
      * 
      * @return true if connected
      */
     public boolean isConnected()
     {
         return this.socket != null && this.socket.isConnected();
     }
     
     /**
      * Disconnect from the cRIO.
      */
     public synchronized void disconnect() throws IOException
     {
         this.socket.close();
     }
     
     
     /**
      * Login to the cRIO server. Assumes the cRIO is already connected.
      * 
      * @param username login username
      * @param password password corresponding to username
      * @return true if login was successful, false if login rejected
      * @throws IOException error communicating to cRIO
      */
     public synchronized boolean login(String username, String password) throws IOException
     {
         if (!this.isConnected()) throw new IOException("Not connected.");
     
         /* Write the login message. */
         this.writeMessage("login:" + username + ',' + password);
         
         /* Read whether the cRIO accepted login. */
         int len = this.in.readInt();
         char strCh[] = new char[len];
         for (int i = 0; i < len; i++) strCh[i] = (char)this.in.readByte();
         
         String str = String.valueOf(strCh);
         if ("login_ok".equals(str))
         {
             /* Login succeeded. */
             return true;
         }
         else if (str.startsWith("login_fail"))
         {
             /* Login failed for some reason. */
             this.logger.warn("Login failed with reason: " + str.substring(str.indexOf(':') + 1));
             return false;
         }
         else
         {
             this.logger.warn("Unexpected login response: " + str);
             return false;
         }
     }
     
     /**
      * Enable the pump.
      * 
      * @param enabled true to enable the pump
      * @throws IOException error communicating with cRIO
      */
     public synchronized void enablePump(boolean enabled) throws IOException
     {
         if (!this.isConnected()) throw new IOException("Not connected.");
         
         this.writeMessage("rig:enable_pump:" + (enabled ? "on" : "off"));
         this.pump = enabled;
     }
     
     /**
      * Enable the inverter.
      * 
      * @param enabled true to enable the inverter
      * @throws IOException error communicating with cRIO
      */
     public synchronized void enableInverter(boolean enabled) throws IOException
     {
         if (!this.isConnected()) throw new IOException("Not connected.");
         
         this.writeMessage("rig:enable_inverter:" + (enabled ? "on" : "off"));
         this.inverter = enabled;
     }
     
     /**
      * Sets paddle speed.
      * 
      * @param speed speed to set
      * @throws IOException error communicating with cRIO
      */
     public synchronized void setSpeed(double speed) throws IOException
     {
         if (!this.isConnected()) throw new IOException("Not connected.");
         
         this.writeMessage("rig:set_speed:" + String.valueOf(speed));
         this.speed = speed;
     }
     
     /**
      * Sets analogue output.
      * 
      * @param channel channel that is being set
      * @param value value to write
      * @throws IOException error communicating with cRIO
      */
     public synchronized void setAnalogOutput(int channel, double value) throws IOException
     {
         if (!this.isConnected()) throw new IOException("Not connected.");
         
         this.writeMessage("set_AO," + String.valueOf(value) + "," + String.valueOf(channel));
        this.aout[channel] = value;
     }
     
     /**
      * Set digital output.
      * 
      * @param channel channel that is being set
      * @param value value to write
      * @throws IOException error communicating with cRIO
      */
     public synchronized void setDigitalOutput(int channel, boolean value) throws IOException
     {
         if (!this.isConnected()) throw new IOException("Not connected.");
         
         this.writeMessage("set_DO," + (value ? '1' : '0') + "," + String.valueOf(channel));
        this.dout[channel] = value;
     }
     
     /**
      * Clears an alarm.
      * 
      * @throws IOException error communication with cRIO
      */
     public synchronized void acknowledgeAlarm() throws IOException
     {
         if (!this.isConnected()) throw new IOException("Not connected.");
         
         this.writeMessage("rig:ack_alarm");
     }
     
     /**
      * Reboots the cRIO.
      * 
      * @throws IOException error communicating with cRIO
      */
     public synchronized void reboot() throws IOException
     {
         if (!this.isConnected()) throw new IOException("Not connected.");
         
         this.writeMessage("rig:server:reboot");
     }
     
     /**
      * Reads data from the cRIO.
      * 
      * @throws IOException error communicating with the cRIO.
      */
     public synchronized void bufferData() throws IOException
     {
         if (!this.isConnected()) throw new IOException("Not connected.");
         
         /* Send the rig data request. */
         this.writeMessage("rig:data?");
 
         int len = this.in.readInt();
         
         /* Read in rig data response tag. */
         String tag = "";
         for (int i = 0; i < "rig:data:".length(); i++) tag += (char)this.in.readByte();
         len -= "rig:data:".length();
         if (!"rig:data:".equals(tag.toString()))
         {
             /* Unexpected response. */
             this.logger.debug("Unexpected response reading data: " + tag);
             if (len > 0) this.in.read(new byte[len]);
             return;
         }
         
         /* Protocol dictates we should be getting multiples of 19. */
         if (len % 19 != 0)
         {
             /* Unexpected data length. */
             this.logger.debug("Unexpected data response length: " + len);
             return;
         }
         
         /* No data to read. */
         if (len == 0) return;
 
         byte buf[] = new byte[len];
         this.in.read(buf);
         
         int i, scanChan = 0;
         for (i = 0; i < len; i += 8)
         {
             double val = ByteBuffer.wrap(buf, i, 8).getDouble();
  
             if (scanChan < NUM_AIN_CHANS)
             {
                 /* Analog data. */
                 this.ain[scanChan] = val;
                 scanChan++;
             }
             else if (scanChan == NUM_AIN_CHANS)
             {
                 /* Digital data. */
                 this.din = (byte)val;
                 scanChan++;
             }
             else
             {
                 /* Rig state. */
                 this.rigState = (byte)val;
                 scanChan = 0;
             }
         }
     }
 
     /**
      * Writes a message to the cRIO in the format:<br />
      *    &lt;length&gt;&ltchar&gt;....
      * <br />
      * Where length is a 32 bit integer and char is UTF8 character.
      * 
      * @param message message to send
      * @throws IOException error sending message
      */
     private void writeMessage(String message) throws IOException
     {
         char messCh[] = message.toCharArray();        
         
         /* Write message. */
         this.out.writeInt(messCh.length);
         for (int i = 0; i < messCh.length; i++)
         {
             this.out.writeByte(messCh[i]);
         }
     }
     
     /**
      * Gets whether a digital input channel is on or off.
      * 
      * @param chan channel to get
      * @return true if channel on, false if off
      */
     public boolean getDigitalInput(int chan)
     {
         return (this.din & (int)Math.pow(2, chan)) != 0;
     }
     
     /**
      * Gets all digital input channels.
      * 
      * @return all digital inputs.
      */
     public boolean[] getDigitalInputs()
     {
         boolean d[] = new boolean[8];
         d[0] = (this.din & 0x1) != 0;
         d[1] = (this.din & 0x2) != 0;
         d[2] = (this.din & 0x4) != 0;
         d[3] = (this.din & 0x8) != 0;
         d[4] = (this.din & 0x16) != 0;
         d[5] = (this.din & 0x32) != 0;
         d[6] = (this.din & 0x64) != 0;
         d[7] = (this.din & 0x128) != 0;
         return d;
     }
 
     /**
      * Gets the value of an analog input.
      * 
      * @param chan channel to get
      * @return value of channel
      */
     public double getAnalogInput(int chan)
     {
         return this.ain[chan];
     }
     
     /**
      * Gets all the analog channel input values.
      * 
      * @return value of analog inputs
      */
     public double[] getAnalogInputs()
     {   
         return Arrays.copyOf(this.ain, NUM_AIN_CHANS);
     }
     
     /**
      * Gets the digital output values.
      * @return array of digital channel outputs
      */
     
     public boolean[] getDigitalOutputs()
     {
     	return this.dout;
     }
    
     /**
      * Gets the analog output values.
      * @return array of analog channel outputs
      */
     public double[] getAnalogOutputs()
     {
    	return this.aout;
     }
     
     /**
      * Gets the current paddle speed.
      * @return paddle speed
      */
     public double getSpeed()
     {
     	return this.speed;
     }
     
     /**
      * Gets the pump status
      * @return pump status
      */
     
     public boolean getPump()
     {
     	return this.pump;
     }
     
     /**
      * Gets the inverter status
      * @return inverter status
      */
     public boolean getInverter()
     {
     	return this.inverter;
     }
 
   
     /**
      * Returns whether there is an alarm.
      * 
      * @return true if there is an alarm
      */
     public boolean hasAlarm()
     {
         return (this.rigState & 0x01) != 0;
     }
 
     /**
      * Returns whether the rig is at paddle limit 1.
      * 
      * @return true if at paddle limit 1
      */
     public boolean getPaddleLimit1()
     {
         return (this.rigState & 0x02) != 0;
     }
     
     /**
      * Returns whether the rig is at paddle limit 2.
      * 
      * 
      * @return true if at paddle limit 2
      */
     public boolean getPaddleLimit2()
     {
         return (this.rigState & 0x04) != 0;
     }
     
     /**
      * Returns whether the rig is at sump limit.
      * 
      * @return true if at sump limit
      */
     public boolean getSumpLimit()
     {
         return (this.rigState & 0x08) != 0;
     }
 }
