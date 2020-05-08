 /**
  * Copyright (C) 2012 SINTEF <franck.fleurey@sintef.no>
  *
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.gnu.org/licenses/lgpl-3.0.txt
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.thingml.chestbelt.desktop;
 
 import org.thingml.chestbelt.driver.ChestBeltListener;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.thingml.chestbelt.driver.ChestBelt;
 
 /**
  *
  * @author ffl
  */
 public class ChestBeltFileLogger implements ChestBeltListener {
     
     private SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm:ss.SSS");
     private String SEPARATOR = "\t";
     
     protected File folder;
     protected boolean logging = false;
     protected boolean request_start = false;
     protected long startTime = 0;
     protected long cbStartTime = 0;
     protected PrintWriter log;
     protected PrintWriter ecg;
     protected PrintWriter imu;
     protected PrintWriter phi;
     protected PrintWriter emg;
     protected PrintWriter rms;
     
     
     protected boolean eCGEpoch = false;
 
     public boolean iseCGEpoch() {
         return eCGEpoch;
     }
     
     private ChestBelt belt;
     
     public ChestBeltFileLogger(File folder, ChestBelt belt) {
         this.belt = belt;
         this.folder = folder;
         this.eCGEpoch = false;
         //numFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
         //imuFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
     }
     
      public ChestBeltFileLogger(File folder, ChestBelt belt, boolean eCGEpoch) {
         this.belt = belt;
         this.folder = folder;
         this.eCGEpoch = eCGEpoch;
         //numFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
         //imuFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
     }
     
     public boolean isLogging() {
         return logging;
     }
     
     public void startLogging() {
        String sName = createSessionName(); 
        File sFolder = new File(folder, sName);
       
       // To avoid overwriting an exiting folder (in case several logs are created at the same time)
       int i=1;
       while (sFolder.exists()) {
           sFolder = new File(folder, sName + "-" + i);
           i++;
       }
       
        sFolder.mkdir();
        imu_data_reset();
        try {
            log = new PrintWriter(new FileWriter(new File(sFolder, "Chestbelt_log.txt")));
            log.println("# This file contains one line per message received from the Chest Belt.");
            
            ecg = new PrintWriter(new FileWriter(new File(sFolder, "Chestbelt_ecg.txt")));
            //ecg.println("# ECG Data, Raw 12bits ADC values, 250Hz.");
            ecg.println("Value" + SEPARATOR + "PC_EPOC" + SEPARATOR + "CU_EPOC" + SEPARATOR + "CU_MS_TICK" + SEPARATOR + "Update");
            
            emg = new PrintWriter(new FileWriter(new File(sFolder, "Chestbelt_emg.txt")));
            emg.println("# EMG Data, Raw 12bits ADC values, 1kHz.");
            
            rms = new PrintWriter(new FileWriter(new File(sFolder, "Chestbelt_emg_rms.txt")));
            rms.println("# EMG RMS Values for channel A and B, 12bits values, 10Hz.");
            
            imu = new PrintWriter(new FileWriter(new File(sFolder, "Chestbelt_imu.txt")));
            imu.println("RXTime" + SEPARATOR + "CorrTime" + SEPARATOR + "RawTime" + SEPARATOR + "AX" + SEPARATOR + "AY" + SEPARATOR + "AZ" + SEPARATOR + "GX" + SEPARATOR + "GY" + SEPARATOR + "GZ");
            
            phi = new PrintWriter(new FileWriter(new File(sFolder, "Chestbelt_phi.txt")));
            phi.println("RXTime" + SEPARATOR + "CorrTime" + SEPARATOR + "RawTime" + SEPARATOR + "Heart Rate (BPM)" + SEPARATOR + "Temperature (°C)");
            
        } catch (IOException ex) {
            Logger.getLogger(ChestBeltFileLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        temperature = 0;
        request_start = true;
     }
     
     public void stopLogging() {
         if (logging || request_start) {
             logging = false;
             request_start = false;
             log.close();
             ecg.close();
             imu.close();
             phi.close();
             rms.close();
             emg.close();
             log = null;
             ecg = null;
             imu = null;
             phi = null;
             rms = null;
             emg = null;
         }
     }
     
     public String createSessionName() {
         SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
         return timestampFormat.format( Calendar.getInstance().getTime());
     }
     
     public String currentTimeStamp() {
         //return timestampFormat.format( Calendar.getInstance().getTime());
         //return timestampFormat.format( Calendar.getInstance().getTime()) + SEPARATOR + (System.currentTimeMillis()-startTime);
         return "" + System.currentTimeMillis();
     }
     
     public String calculatedAndRawTimeStamp(int belt_timestamp) {
         //return timestampFormat.format( Calendar.getInstance().getTime());
         //return (t+refTime-cbStartTime)*4;
         //long delta = System.currentTimeMillis() - belt.getEpochTimestamp(belt_timestamp);
         //if ((delta > 2000) || (delta < -2000)) System.out.println("Large delta detected: " + delta);
         return "" + belt.getEpochTimestamp(belt_timestamp) + SEPARATOR + belt_timestamp*4;
     }
 
     @Override
     public void cUSerialNumber(long value, int timestamp) {
         if (logging) log.println("[SerialNumber]" + SEPARATOR + currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + value);
     }
 
     @Override
     public void cUFWRevision(String value, int timestamp) {
         if (logging) log.println("[FWRevision]" + SEPARATOR + currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + value);
     }
 
     @Override
     public void batteryStatus(int value, int timestamp) {
         if (logging) log.println("[Battery]" + SEPARATOR + currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + value);
     }
 
     @Override
     public void indication(int value, int timestamp) {
         if (logging) log.println("[Indication]" + SEPARATOR + currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + value);
     }
 
     @Override
     public void status(int value, int timestamp) {
         if (logging) log.println("[Status]" + SEPARATOR + currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + value);
     }
 
     @Override
     public void messageOverrun(int value, int timestamp) {
         if (logging) log.println("[MsgOverrun]" + SEPARATOR + currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + value);
     }
     
     protected long refTime = 0;
     protected boolean inSeconds = true;
 
     @Override
     public void referenceClockTime(long value, boolean seconds) {
         // This function needs to be rewritten. 
         //   Do we need the old clock system?
         //   Apart from the old clock there is only the handling of "logging" and printing to log.
         refTime = value;
         inSeconds = seconds;
         if (request_start) {
             request_start = false;
             startTime = System.currentTimeMillis();
             // Removed to avoid interference with old time signals. ecg_timestamp = 0;
             // Removed to avoid interference with old time signals. emg_timestamp = 0;
             cbStartTime = refTime;
             logging = true;
         }
         if (logging) log.println("[RefClock]" + SEPARATOR + currentTimeStamp() + SEPARATOR + value);
     }
 
     @Override
     public void fullClockTimeSync(long value, boolean seconds) {
         if (logging) log.println("[FullClock]" + SEPARATOR + currentTimeStamp() + SEPARATOR + value);
     }
 
     private DecimalFormat numFormat = new DecimalFormat("##.0");
     @Override
     public void heartRate(int value, int timestamp) {
         double hr = value/10.0;
         double temp = temperature/10.0;
         if (logging) phi.println(currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + numFormat.format(hr) + SEPARATOR + numFormat.format(temp));
     }
 
     @Override
     public void heartRateConfidence(int value, int timestamp) {
         if (logging) log.println("[HeartRateConfidence]" + SEPARATOR + currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + value);
     }
 
     private int ecg_timestamp = 0;
     @Override
     public void eCGData(int value) {
         ecg_timestamp += 4;
         if (logging) {
             if(!eCGEpoch) {
                 // This can be used to log without timestamp for each sample to keep the file smaller.
                 ecg.println(value);
             } else {
                 // This can be used to log the timestamp for each sample but it makes the file really big.
                 long ts = belt.getEpochTimestampFromMs(ecg_timestamp);
                 ecg.println(value + SEPARATOR + currentTimeStamp() + SEPARATOR + ts + SEPARATOR + ecg_timestamp + SEPARATOR + 0);
             }
         }
 
     }
 
     @Override
     public void eCGSignalQuality(int value, int timestamp) {
         if (logging) log.println("[ECGSignalQuality]" + SEPARATOR + currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + value);
     }
 
     @Override
     public void eCGRaw(int value, int timestamp) {
         ecg_timestamp = timestamp*4;
         if (logging) {
             long ts = belt.getEpochTimestampFromMs(ecg_timestamp);
             ecg.println(value + SEPARATOR + currentTimeStamp() + SEPARATOR + ts + SEPARATOR + ecg_timestamp + SEPARATOR + 1);
         }
     }
     
     
     int ax, ay, az, gx, gy, gz;
   
     private void imu_data_reset() {
         //System.out.println("reset");
         ax = Integer.MIN_VALUE;
         ay = Integer.MIN_VALUE;
         az = Integer.MIN_VALUE;
         gx = Integer.MIN_VALUE;
         gy = Integer.MIN_VALUE;
         gz = Integer.MIN_VALUE;
     }
     
     private boolean imu_data_ready() {
         return ax != Integer.MIN_VALUE && ay != Integer.MIN_VALUE && az != Integer.MIN_VALUE && 
                gx != Integer.MIN_VALUE && gy != Integer.MIN_VALUE && gz != Integer.MIN_VALUE;
     }
     
     @Override
     public void gyroPitch(int value, int timestamp) {
         if (logging) {
              //System.out.println("gy");
             if (gy == Integer.MIN_VALUE) {
                 gy = value;
                 if (imu_data_ready()) {
                     imu.println(currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + A(ax) + SEPARATOR + A(ay) + SEPARATOR + A(az) + SEPARATOR + G(gx) + SEPARATOR + G(gy) + SEPARATOR + G(gz));
                     imu_data_reset();
                 }
             }
             else {
                 imu_data_reset();
                 gy = value;
             }
         }
     }
 
     @Override
     public void gyroRoll(int value, int timestamp) {
         if (logging) {
             //System.out.println("gx");
             if (gx == Integer.MIN_VALUE) {
                 gx = value;
                 if (imu_data_ready()) {
                     imu.println(currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + A(ax) + SEPARATOR + A(ay) + SEPARATOR + A(az) + SEPARATOR + G(gx) + SEPARATOR + G(gy) + SEPARATOR + G(gz));
                     imu_data_reset();
                 }
             }
             else {
                 imu_data_reset();
                 gx = value;
             }
         }
     }
 
     @Override
     public void gyroYaw(int value, int timestamp) {
         if (logging) {
              //System.out.println("gz");
             if (gz == Integer.MIN_VALUE) {
                 gz = value;
                 if (imu_data_ready()) {
                     imu.println(currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + A(ax) + SEPARATOR + A(ay) + SEPARATOR + A(az) + SEPARATOR + G(gx) + SEPARATOR + G(gy) + SEPARATOR + G(gz));
                     imu_data_reset();
                 }
             }
             else {
                 imu_data_reset();
                 gz = value;
             }
         }
     }
 
     @Override
     public void accLateral(int value, int timestamp) {
         if (logging) {
              //System.out.println("ay");
             if (ay == Integer.MIN_VALUE) {
                 ay = value;
                 if (imu_data_ready()) {
                     imu.println(currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + A(ax) + SEPARATOR + A(ay) + SEPARATOR + A(az) + SEPARATOR + G(gx) + SEPARATOR + G(gy) + SEPARATOR + G(gz));
                     imu_data_reset();
                 }
             }
             else {
                 imu_data_reset();
                 ay = value;
             }
         }
     }
 
     @Override
     public void accLongitudinal(int value, int timestamp) {
         if (logging) {
              //System.out.println("az");
             if (az == Integer.MIN_VALUE) {
                 az = value;
                 if (imu_data_ready()) {
                     imu.println(currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + A(ax) + SEPARATOR + A(ay) + SEPARATOR + A(az) + SEPARATOR + G(gx) + SEPARATOR + G(gy) + SEPARATOR + G(gz));
                     imu_data_reset();
                 }
             }
             else {
                 imu_data_reset();
                 az = value;
             }
         }
     }
 
     @Override
     public void accVertical(int value, int timestamp) {
         if (logging) {
             //System.out.println("ax");
             if (ax == Integer.MIN_VALUE) {
                 ax = value;
                 if (imu_data_ready()) {
                     imu.println(currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + A(ax) + SEPARATOR + A(ay) + SEPARATOR + A(az) + SEPARATOR + G(gx) + SEPARATOR + G(gy) + SEPARATOR + G(gz));
                     imu_data_reset();
                 }
             }
             else {
                 imu_data_reset();
                 ax = value;
             }
         }
     }
 
     @Override
     public void rawActivityLevel(int value, int timestamp) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
     
     private DecimalFormat imuFormat = new DecimalFormat("0.00000");
     
     protected String A(int v) {
         return imuFormat.format(v * 0.004);
     }
     protected String G(int v) {
         return imuFormat.format(v * 0.069565);
     }
     
 
     @Override
     public void combinedIMU(int ax, int ay, int az, int gx, int gy, int gz, int timestamp) {
         if (logging) {
             imu.println(currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + A(ax) + SEPARATOR + A(ay) + SEPARATOR + A(az) + SEPARATOR + G(gx) + SEPARATOR + G(gy) + SEPARATOR + G(gz));
         }
     }
 
     private int temperature = 0;
     @Override
     public void skinTemperature(int value, int timestamp) {
         temperature = value;
     }
 
     @Override
     public void connectionLost() {
        
     }
 
     @Override
     public void eMGData(int value) {
         emg_timestamp +=1;
         if (logging) {
             if (!eCGEpoch) {
                 // This can be used to log without timestamp for each sample to keep the file smaller.
                 emg.println(value);
             } else {
                 // This can be used to log the timestamp for each sample but it makes the file really big.
                 long ts = belt.getEpochTimestampFromMs(emg_timestamp); 
                 emg.println(value + SEPARATOR + currentTimeStamp() + SEPARATOR + ts + SEPARATOR + emg_timestamp + SEPARATOR + 0);
             }
         }
     }
 
     private int emg_timestamp = 0;
     
     @Override
     public void eMGSignalQuality(int value, int timestamp) {
         
     }
 
     /**
      *
      * @param value
      * @param timestamp
      */
     @Override
     public void eMGRaw(int value, int timestamp) {
         emg_timestamp = timestamp*4;
         if (logging) {
             long ts = belt.getEpochTimestampFromMs(emg_timestamp);
             emg.println(value + SEPARATOR + currentTimeStamp() + SEPARATOR + ts + SEPARATOR + emg_timestamp + SEPARATOR + 1);
         }
     }
 
     @Override
     public void eMGRMS(int channelA, int channelB, int timestamp) {
         if (logging) {
             rms.println(currentTimeStamp() + SEPARATOR + calculatedAndRawTimeStamp(timestamp) + SEPARATOR + channelA + SEPARATOR + channelB);
         }
     }
     
     
 }
