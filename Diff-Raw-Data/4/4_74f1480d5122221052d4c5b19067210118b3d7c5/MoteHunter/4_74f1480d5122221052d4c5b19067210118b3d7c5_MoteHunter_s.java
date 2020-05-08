 /*
  * Copyright (c) 2006 Intel Corporation
  * Copyright (c) 2012 Csaba Kiraly
  * All rights reserved.
  *
  * This file is distributed under the terms of The GNU Affero General
  * Public License, version 3 or later. It is based on code distribted
  * as part of TinyOS, which was licensed under the terms of the INTEL
  * LICENSE.
  */
 
 import net.tinyos.message.*;
 import net.tinyos.util.*;
 import java.io.*;
 
 public class MoteHunter implements MessageListener
 {
   MoteIF mote;
   Data data;
   Window window;
   UsbIssCmps09 compass;
 
   int counter;
 
   int interval = 50;            //ms
 
   public void setCompass (UsbIssCmps09 c)
   {
     compass = c;
   }
 
   public MoteHunter ()
   {
     data = new Data (this);
     window = new Window (this);
     mote = new MoteIF (PrintStreamMessenger.err);
   }
 
   /* Main entry point */
   void run (String compassID)
   {
     try {
       compass = new UsbIssCmps09 (compassID);
     } catch (Exception e) {
       window.error ("Error connecting to compass at " + compassID +"! Going on without direction data ...");
     }
     window.setCompass (compass);
     window.setup ();
     mote.registerListener (new RssiMsg (), this);
     mote.registerListener (new RssiAckMsg (), this);
     mote.registerListener (new RssiRxMsg (), this);
     counter = 0;
   }
 
   /* The data object has informed us that nodeId is a previously unknown
      mote. Update the GUI. */
   void newNode (int nodeId)
   {
     window.newNode (nodeId);
   }
 
   public synchronized void messageReceived (int dest_addr, Message msg)
   {
     int timestamp = 0;
 
     if (msg instanceof RssiRxMsg) {
       RssiRxMsg rmsg = (RssiRxMsg) msg;
 
       /* Update mote data */
       timestamp = (int) (rmsg.get_timestamp () / interval);
       data.update (Data.MeasureType.RCV, rmsg.get_id (), timestamp, rmsg.get_rssi ());
       /* Inform the GUI that new data showed up */
       System.out.print (System.currentTimeMillis () + " " + timestamp + " " + "RX" + " " +
                         rmsg.get_id () + " " + rmsg.get_rssi () + " " + "NAN");
       window.newData ();
     } else if (msg instanceof RssiAckMsg) {
       RssiAckMsg rmsg = (RssiAckMsg) msg;
 
       /* Update mote data */
       timestamp = (int) (rmsg.get_timestamp () / interval);
       data.update (Data.MeasureType.ACK, rmsg.get_id (), timestamp, rmsg.get_rssi ());
       /* Inform the GUI that new data showed up */
       System.out.print (System.currentTimeMillis () + " " + timestamp + " " + "ACK" + " " +
                         rmsg.get_id () + " " + rmsg.get_rssi () + " " + "NAN");
       window.newData ();
     } else if (msg instanceof RssiMsg) {
       RssiMsg rmsg = (RssiMsg) msg;
 
       /* Update mote data */
       timestamp = (int) (rmsg.get_timestamp () / interval);
       data.update (Data.MeasureType.RXPOWER_AVG, 0, timestamp, rmsg.get_rssi_avg ());
       data.update (Data.MeasureType.RXPOWER_MAX, 0, timestamp, rmsg.get_rssi_max ());
       /* Inform the GUI that new data showed up */
       System.out.print (System.currentTimeMillis () + " " + timestamp + " " + "RXPOWER" + " " +
                         "ALL" + " " + rmsg.get_rssi_avg () + " " + rmsg.get_rssi_max ());
       window.newData ();
     }
     //handle direction as a fictive node
     if (timestamp > 0 && compass != null) {
       int yaw, roll, pitch;
       yaw = compass.getYaw ();
       data.update (Data.MeasureType.YAW, 0, timestamp, yaw);
 
       roll = compass.getRoll () + 90;
       data.update (Data.MeasureType.ROLL, 0, timestamp, roll);
 
       pitch = compass.getPitch () + 90;
       data.update (Data.MeasureType.PITCH, 0, timestamp, pitch);
      System.out.println (" " + yaw + " " + pitch + " " + roll);
     }
 
   }
 
   /* The user wants to set the interval to newPeriod. Refuse bogus values
      and return false, or accept the change, broadcast it, and return
      true */
   synchronized boolean setInterval (int newPeriod)
   {
     if (newPeriod < 1 || newPeriod > 65535) {
       return false;
     }
     interval = newPeriod;
     return true;
   }
 
   /* The user wants to set a new target ID.
      checks might be implemented later */
   synchronized boolean setPingerTarget (int nodeId)
   {
     if (nodeId < 0) {
       return false;
     }
     sendPingerTarget (nodeId);
     return true;
   }
 
   /* The user wants to set a new radio channel. */
   synchronized boolean setChannel (int channel)
   {
     if (channel < 11 || channel > 26) {
       return false;
     }
     sendChannel ((short) channel);
     return true;
   }
 
   /* Send command to Pinger module */
   void sendPingerTarget (int nodeId)
   {
     SetPingerMsg msg = new SetPingerMsg ();
 
     msg.set_target_id (nodeId);
     try {
       mote.send (MoteIF.TOS_BCAST_ADDR, msg);
     }
     catch (IOException e) {
       window.error ("Cannot send message to mote");
     }
   }
 
   /* Send radio channel change command to the mote */
   void sendChannel (short channel)
   {
     SetChannelMsg msg = new SetChannelMsg ();
 
     msg.set_channel (channel);
     try {
       mote.send (MoteIF.TOS_BCAST_ADDR, msg);
     }
     catch (IOException e) {
       window.error ("Cannot send message to mote");
     }
   }
 
   /* User wants to clear all data. */
   void clear ()
   {
     data = new Data (this);
   }
 
   public static void main (String[]args) throws IOException,
     net.tinyos.comm.UnsupportedCommOperationException
   {
     MoteHunter me = new MoteHunter ();
     me.run (args[0]);
   }
 }
