 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package heartsim;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Lee Boynton
  */
 public class Stimulus
 {
     private boolean started = true;
     private int stimRow;
     private int stimCol;
     private CellularAutomaton ca;
     private int waitTime = 2000;
     private int additionalDelayTime = 0;
     private Thread stimulusThread;
 
     public Stimulus(CellularAutomaton ca)
     {
         this.ca = ca;
     }
 
     public void setCa(CellularAutomaton ca)
     {
         this.ca = ca;
     }
 
     public void setStimulatedCell(int row, int col)
     {
         this.stimRow = row;
         this.stimCol = col;
     }
 
     public void run()
     {
         stimulusThread = new Thread(new StimulusRunnable());
         stimulusThread.setName("Stimulus");
         stimulusThread.start();
     }
 
     public void stop()
     {
         started = false;
     }
 
     public void addDelay(int milliseconds)
     {
         additionalDelayTime += milliseconds;
     }
 
     public void setWaitTime(int waitTime)
     {
         Application.getInstance().output("Wait time set to " + waitTime);
         this.waitTime = waitTime;
     }
 
     public class StimulusRunnable implements Runnable
     {
         public void run()
         {
             while (started)
             {
                 ca.stimulate(stimRow, stimCol);
                 
                 try
                 {
                     Thread.sleep(waitTime);
                     Thread.sleep(additionalDelayTime);
                 }
                 catch (InterruptedException ex)
                 {
                     Logger.getLogger(Stimulus.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
                 additionalDelayTime = 0;
             }
         }
     }
 }
