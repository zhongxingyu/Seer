 package sensorapp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * This thread will wait until OutputThread completes its operation. It will
  * then read the information in the Sense class and output it accordingly.
  * 
  * @author Alex Breskin
  */
 public class InputThread extends Thread {
     private Sense senseData;
     private List<Integer> _lightList;
     private List<Integer> _tempList;
     private List<Integer> _humidList;
     
     //Timestep
     private long timeStep = 1;
     
     //Since this thread talks directly to the process thread, we need a livelock guard
     private boolean bSentLightList = true;
     private boolean bSentTempList = true;
     private boolean bSentHumidList = true;
     
     public InputThread(Sense data, long n)
     {
         senseData = data;   //Tell it yes, we're dealing with the Sensor class.
         
         /**
          * We must set up the ArrayLists so that they do not fail due to
          * synchronization issues.
          */
         _lightList = Collections.synchronizedList(new ArrayList());
         _tempList = Collections.synchronizedList(new ArrayList());
         _humidList = Collections.synchronizedList(new ArrayList());
         
         timeStep = n;
     }
     
     @Override
     public void run()
     {
         // We take the information and store them into an ArrayList
         //Start the process thread
         (new Thread(new ProcessThread(timeStep))).start();
     }
     
     /**
      * Get methods for Light, Temperature and Humidity to be used by the Process
      * Thread. Thus, they need to be synchronized.
      */
     public synchronized List<Integer> getLight()
     {
         while (!bSentLightList)
         {
             try {
                     wait();
             } catch (InterruptedException e){}
         }
         //Toggle that light information has been sent
         bSentLightList = false;
         notifyAll();
         return _lightList;
     }
     
     public synchronized List<Integer> getTemp()
     {
         while (!bSentTempList)
         {
             try {
                     wait();
             } catch (InterruptedException e){}
         }
        //Toggle that temperature information has been sent
         bSentTempList = false;
         notifyAll();
         return _tempList;
     }
     
     public synchronized List<Integer> getHumidity()
     {
         while (!bSentHumidList)
         {
             try {
                     wait();
             } catch (InterruptedException e){}
         }
        //Toggle that humidity information has been sent
         bSentHumidList = false;
         notifyAll();
         return _humidList;
     }
     /**
      * This method is just for inputThread's use. It should be private but
      * whatever.
      * 
      * @param light to be put into _lightList
      * @param temp to be put into _tempList
      * @param humid to be put into _humidList
      */
     public synchronized void setLists(int light, int temp, int humid)
     {
         
     }
 }
