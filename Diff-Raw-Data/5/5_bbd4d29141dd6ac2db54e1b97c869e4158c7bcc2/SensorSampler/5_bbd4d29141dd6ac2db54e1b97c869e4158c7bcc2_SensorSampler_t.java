 /*
  * SensorSampler.java
  * Modified by Romaric Drigon as 11/01/2012
  * Based on SendDataDemo by Syn Microsystems, Inc. original Copyright retained
  *
  * Copyright (c) 2008-2010 Sun Microsystems, Inc.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal in the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  */
 
 package org.sunspotworld.client;
 
 import com.sun.spot.io.j2me.radiogram.*;
 import com.sun.spot.resources.Resources;
 import com.sun.spot.resources.transducers.ITriColorLED;
 import com.sun.spot.resources.transducers.ILightSensor;
 import com.sun.spot.resources.transducers.ITemperatureInput;
 import com.sun.spot.util.Utils;
 import javax.microedition.io.*;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 
 /*
 
 
         
         while (true) {
             try {
 
 
 
                 System.out.println("Light value = " + brightness);
                 
 
                 Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - now));
             } catch (Exception e) {
                 System.err.println("Caught " + e + " while collecting/sending sensor sample.");
             }
         }
     }
     
     protected void pauseApp() {
         // Commentaire d'origine : This will never be called by the Squawk VM
     }
     
     protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
         // Commentaire d'origine : Only called if startApp throws any exception other than MIDletStateChangeException
     }
 }
 */
 
 /*
  * SensorSampler.java
  *
  * Copyright (c) 2008-2010 Sun Microsystems, Inc.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal in the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  */
 
 /*
  * Application tournant sur le SunSPOT
  * Retourne toutes les 10 secondes la luminosit ambiante et la temprature
  */
 public class SensorSampler extends MIDlet {
 
     private static final int HOST_PORT = 67;
    private static final int SAMPLE_PERIOD = 3 * 1000; // 60 secondes, que l'on passe en millisecondes
     
     protected void startApp() throws MIDletStateChangeException {
         RadiogramConnection rCon = null;
         Datagram dg = null;
         String ourAddress = System.getProperty("IEEE_ADDRESS");
 
         // on accde aux ressources matrielles
         ILightSensor lightSensor = (ILightSensor)Resources.lookup(ILightSensor.class);
         ITemperatureInput tempSensor = (ITemperatureInput) Resources.lookup(ITemperatureInput.class);
        ITriColorLED led = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED7");
         
         System.out.println("Starting new sensor sampler (brightness and temperature) application on " + ourAddress + " ...");
 
 	// Commentaire d'origine : Listen for downloads/commands over USB connection
 	new com.sun.spot.service.BootloaderListenerService().getInstance().start();
 
         try {
             // Commentaire d'origine : Open up a broadcast connection to the host port
             // where the 'on Desktop' portion of this demo is listening
             rCon = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
             dg = rCon.newDatagram(50);  // Commentaire d'origine : only sending 12 bytes of data
         } catch (Exception e) {
             System.err.println("Caught " + e + " in connection initialization.");
             notifyDestroyed();
         }
         
         while (true) {
             try {
                 // Commentaire d'origine : Get the current time and sensor reading
                 long now = System.currentTimeMillis();
                 int brightness = lightSensor.getValue();
                 double temperature = tempSensor.getCelsius(); // rajout : temprature
 
                 // Commentaire d'origine : Package the time and sensor reading into a radio datagram and send it.
                 dg.reset();
                 dg.writeLong(now);
                 dg.writeInt(brightness);
                 dg.writeDouble(temperature);
                 rCon.send(dg);
 
                 System.out.print("Light value = " + brightness);
                 System.out.println(" - Temperature = " + temperature);
                 
                 // Commentaire d'origine : Go to sleep to conserve battery
                 Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - now));
             } catch (Exception e) {
                 System.err.println("Caught " + e + " while collecting/sending sensor sample.");
             }
         }
     }
     
     protected void pauseApp() {
         // Commentaire d'origine : This will never be called by the Squawk VM
     }
     
     protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
         // Commentaire d'origine : Only called if startApp throws any exception other than MIDletStateChangeException
     }
 }
