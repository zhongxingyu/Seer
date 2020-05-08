 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package heartsim;
 
 import heartsim.cam.speed.Maximum;
 import heartsim.cam.speed.Speed;
 import heartsim.gui.BinaryPlotPanelOverlay;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Lee Boynton
  */
 public class Simulator
 {
     private List<SimulatorListener> listeners = Collections.synchronizedList(new ArrayList<SimulatorListener>());
     private CellularAutomaton ca;
     private BinaryPlotPanelOverlay overlay;
     private boolean initialised = false;
     private int runTime;
     private Stimulus stimulus;
     private Speed speed = new Maximum(); // run simulation at maximum speed by default
 
     private enum State
     {
         STOPPED, STARTED
     };
 
     private State state = State.STOPPED;
 
     public void setSpeed(Speed speed)
     {
         this.speed = speed;
     }
 
     public Simulator(BinaryPlotPanelOverlay overlay)
     {
         this.overlay = overlay;
         stimulus = new Stimulus(ca);
     }
 
     public Simulator(CellularAutomaton ca, BinaryPlotPanelOverlay overlay)
     {
         this.ca = ca;
         this.overlay = overlay;
         stimulus = new Stimulus(ca);
     }
 
     public void setAutomaton(CellularAutomaton ca)
     {
         this.ca = ca;
         stimulus.setCa(ca);
     }
 
     public void setRunTime(int runTime)
     {
         this.runTime = runTime;
     }
 
     public int getRunTime()
     {
         return runTime;
     }
 
     public void setStimulatedCell(int row, int col)
     {
         initialiseCAModel();
         stimulus.setStimulatedCell(row, col);
     }
 
     public void setInitialised(boolean initialised)
     {
         this.initialised = initialised;
     }
 
     private void initialiseCAModel()
     {
         if (!initialised)
         {
             ca.initCells();
         }
 
         initialised = true;
     }
 
     public void setHeartRate(int heartRate)
     {
         // say that the wait time between each stimulus should be the heart rate
         // divided by 1 minute
 
         double time = 60.0 / heartRate;
 
         stimulus.setWaitTime((int) (time * 1000));
     }
 
     public void run(int runTime)
     {
         setRunTime(runTime);
         run();
     }
 
     public void run()
     {
         initialiseCAModel();
 
         if(state == State.STOPPED)
         {
             state = State.STARTED;
             Thread t = new Thread(new SimulatorRunnable());
             t.setName("Simulator");
             t.start();
             stimulus.run();
             fireSimulationStarted();
         }
     }
 
     public void stop()
     {
         state = State.STOPPED;
         initialised = false;
         fireSimulationStopped();
     }
 
     public void pause()
     {
         state = State.STOPPED;
         fireSimulationPaused();
     }
 
     public void addListener(SimulatorListener listener)
     {
         listeners.add(listener);
     }
 
     public void removeListener(SimulatorListener listener)
     {
         if (listeners.contains(listener))
         {
             listeners.remove(listener);
         }
     }
 
     private void fireSimulationUpdated()
     {
         for (SimulatorListener listener : listeners)
         {
             listener.simulationUpdated();
         }
     }
 
     private void fireSimulationStarted()
     {
         for (SimulatorListener listener : listeners)
         {
             listener.simulationStarted();
         }
     }
 
     private void fireSimulationPaused()
     {
         for (SimulatorListener listener : listeners)
         {
             listener.simulationPaused();
         }
     }
 
     private void fireSimulationStopped()
     {
         for (SimulatorListener listener : listeners)
         {
             listener.simulationStopped();
         }
     }
 
     private void fireSimulationCompleted()
     {
         for (SimulatorListener listener : listeners)
         {
             listener.simulationCompleted();
         }
     }
 
     public class SimulatorRunnable implements Runnable
     {
         public void run()
         {
             int[] data = overlay.getBuffer();
 
             int[][] u = ca.getU();
 
             for (int t = 0; t < runTime; t++)
             {
                 if(state == State.STOPPED)
                 {
                     return;
                 }
                 
                 int k = 0;
 
                 for (int i = 0; i < u.length; i++)
                 {
                     for (int j = 0; j < u[0].length; j++)
                     {
                         if (u[i][j] == -1)
                         {
                             data[k] = -1;
                         }
                         // this pixel will be white
                         if (u[i][j] == 0)
                         {
                             data[k] = 1;
                         }
                         // blue
                         if (u[i][j] == 1)
                         {
                             data[k] = 255;
                         }
                         // green
                         if (u[i][j] == 2)
                         {
                             data[k] = 65280;
                         }
                         // yellow
                         if (u[i][j] == 3)
                         {
                             data[k] = 16776960;
                         }
                         // orange
                         if (u[i][j] == 4)
                         {
                             data[k] = 14251783;
                         }
                         // red
                         if (u[i][j] == 5)
                         {
                             data[k] = 16711680;
                         }
                         k++;
                     }
                 }
 
                 ca.step();
                 fireSimulationUpdated();
                 
                 try
                 {
                     stimulus.addDelay(speed.getDelay());
                     Thread.sleep(speed.getDelay());
                 }
                 catch (InterruptedException ex)
                 {
                     Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
 
             state = State.STOPPED;
             fireSimulationCompleted();
         }
     }
 }
