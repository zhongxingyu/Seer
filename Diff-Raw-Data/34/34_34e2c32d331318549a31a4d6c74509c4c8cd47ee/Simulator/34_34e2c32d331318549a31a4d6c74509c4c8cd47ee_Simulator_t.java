 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package heartsim;
 
 import heartsim.cam.CellularAutomataModel;
 import heartsim.gui.BinaryPlotPanelOverlay;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
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
     private int runTime = 500;
 
     private enum State
     {
         STOPPED, STARTED
     };
 
     private State state = State.STOPPED;
 
     public Simulator(BinaryPlotPanelOverlay overlay)
     {
         this.overlay = overlay;
     }
 
     public Simulator(CellularAutomaton ca, BinaryPlotPanelOverlay overlay)
     {
         this.ca = ca;
         this.overlay = overlay;
     }
 
     public void setAutomaton(CellularAutomaton ca)
     {
         this.ca = ca;
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
         ca.stimulate(row, col);
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
             t.start();
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
             }
 
             state = State.STOPPED;
             fireSimulationCompleted();
         }
     }
 }
