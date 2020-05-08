 /*
  * Copyright 2011 Gregory P. Moyer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.syphr.mythtv.protocol;
 
 import java.io.IOException;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.syphr.mythtv.protocol.test.Utils;
 import org.syphr.mythtv.protocol.types.EventLevel;
 import org.syphr.mythtv.test.Settings;
 import org.syphr.prom.PropertiesManager;
 
 public class QueryRemoteEncoderTest
 {
     private static PropertiesManager<Settings> settings;
 
     private static SocketManager socketManager;
     private static Protocol proto;
 
     private static QueryRemoteEncoder queryRemoteEncoder;
 
     @BeforeClass
     public static void setUpBeforeClass() throws IOException, CommandException
     {
         settings = Settings.createSettings();
         socketManager = Utils.connect(settings);
         proto = Utils.announceMonitor(settings, socketManager, EventLevel.NONE);
 
         int recorderId = settings.getIntegerProperty(Settings.RECORDER);
         System.out.println("Interrogating recorder " + recorderId);
         queryRemoteEncoder = proto.queryRemoteEncoder(recorderId);
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws IOException
     {
         proto.done();
         socketManager.disconnect();
     }
 
     @Test
     public void testCancelNextRecording() throws IOException
     {
         // TODO
     }
 
     @Test
     public void testGetCurrentRecording() throws IOException
     {
         System.out.println("Current recording: " + queryRemoteEncoder.getCurrentRecording());
     }
 
     @Test
     public void testGetFlags() throws IOException
     {
         System.out.println("Flags: " + queryRemoteEncoder.getFlags());
     }
 
     @Test
     public void testGetMaxBitrate() throws IOException
     {
         long bps = queryRemoteEncoder.getMaxBitrate();
         long kbps = bps / 1024;
         long mbps = kbps / 1024;
 
        System.out.println(String.format("Max bitrate: %dbps / %dkbps / %dmbps", bps, kbps, mbps));
     }
 
     @Test
     public void testGetRecordingStatus() throws IOException
     {
         System.out.println("Recording status: " + queryRemoteEncoder.getRecordingStatus());
     }
 
     @Test
     public void testGetSleepStatus() throws IOException
     {
         System.out.println("Sleep status: " + queryRemoteEncoder.getSleepStatus());
     }
 
     @Test
     public void testGetState() throws IOException
     {
         System.out.println("State: " + queryRemoteEncoder.getState());
     }
 
     @Test
     public void testIsBusy() throws IOException
     {
         // TODO
     }
 
     @Test
     public void testMatchesRecording() throws IOException
     {
         // TODO
     }
 
     @Test
     public void testRecordPending() throws IOException
     {
         // TODO
     }
 
     @Test
     public void testStartRecording() throws IOException
     {
         // TODO
     }
 
     /*
      * ----------------------------------------------------------------
      * The following unit tests can cause side effects. Use with care.
      */
 
 //    @Test
 //    public void testStopRecording() throws IOException
 //    {
 //        queryRemoteEncoder.stopRecording();
 //    }
 }
