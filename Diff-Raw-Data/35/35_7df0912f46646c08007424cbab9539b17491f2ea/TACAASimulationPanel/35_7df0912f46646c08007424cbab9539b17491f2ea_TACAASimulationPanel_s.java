 package edu.umich.eecs.tac.viewer;
 
 import se.sics.tasim.viewer.TickListener;
 import se.sics.tasim.viewer.ViewerPanel;
 import se.sics.isl.util.ConfigManager;
 import se.sics.isl.transport.Transportable;
 
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 import javax.swing.*;
 
 import java.awt.*;
 
 import edu.umich.eecs.tac.TACAAConstants;
 import edu.umich.eecs.tac.viewer.role.*;
 
 /**
  * @author Patrick Jordan
  */
 public class TACAASimulationPanel extends JPanel implements TickListener, ViewListener {
     private Object lock;
 
    private TACAAAgentView[] agentViews = new TACAAAgentView[10];
    private int participants;    
 
     private JTabbedPane tabbedPane;
 
     private ViewerPanel viewerPanel;
 
     private boolean isRunning;
 
     private List<ViewListener> viewListeners;
     private List<TickListener> tickListeners;
 
     public TACAASimulationPanel(ViewerPanel viewerPanel) {
         super(null);
         this.viewerPanel = viewerPanel;
         viewListeners = new CopyOnWriteArrayList<ViewListener>();
         tickListeners = new CopyOnWriteArrayList<TickListener>();
         lock = new Object();
         initialize();
     }
 
     protected void initialize() {
         setLayout(new BorderLayout());
         tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
         setBackground(Color.WHITE);
         add(tabbedPane, BorderLayout.CENTER);
         tabbedPane.setBackground(Color.WHITE);        
     }
 
 
 
     protected void createTabs() {
         tabbedPane.addTab("Dashboard", null, new MainTabPanel(this), "Click to view main dashboard");
         tabbedPane.addTab("Advertisers", null, new AdvertiserTabPanel(this),
                 "Click to view Advertisers");
         tabbedPane.addTab("Publisher", null, new PublisherTabPanel(this),
                 "Click to view Publisher");
     }
 
     public TACAAAgentView getAgentView(int agentID) {
        return agentID < participants ? agentViews[agentID] : null;
     }
 
     public String getAgentName(int agentIndex) {
         TACAAAgentView view = getAgentView(agentIndex);
         return view != null ? view.getName() : Integer.toString(agentIndex);
     }
 
     public int getHighestAgentIndex() {
        return participants;
     }
 
     public void addAgentView(TACAAAgentView view, int index, String name,
                              int role, String roleName, int container) {
     }
 
     public void removeAgentView(TACAAAgentView view) {
     }
 
     /**
      * ****************************************************************** setup
      * and time handling
      * ********************************************************************
      */
 
     public void simulationStarted(long startTime, long endTime,
                                   int timeUnitCount) {
         // Clear any old items before start a new simulation
         clear();
 
         createTabs();
 
         if (!isRunning) {
             viewerPanel.addTickListener(this);
             isRunning = true;
         }
     }
 
     public void simulationStopped() {
         isRunning = false;
         viewerPanel.removeTickListener(this);
 
         repaint();
     }
 
     public void clear() {
        int participants = this.participants;
        this.participants = 0;
        for (int i = 0, n = participants; i < n; i++) {
            agentViews[i] = null;
        }
 
         tabbedPane.removeAll();
 
         clearViewListeners();
         clearTickListeners();
 
         // This must be done with event dispatch thread. FIX THIS!!!
         repaint();
     }
 
     public void nextTimeUnit(int timeUnit) {
 
     }
 
     /**
      * ******************************************************************
      * TickListener interface
      * ********************************************************************
      */
 
     public void tick(long serverTime) {
         fireTick(serverTime);
     }
 
     public void simulationTick(long serverTime, int timeUnit) {
         fireSimulationTick(serverTime, timeUnit);
     }
 
     /**
      * ****************************************************************** API
      * towards the agent views
      * ********************************************************************
      */
 
     ConfigManager getConfig() {
         return viewerPanel.getConfig();
     }
 
     Icon getIcon(String name) {
         return viewerPanel.getIcon(name);
     }
 
     void showDialog(JComponent dialog) {
         viewerPanel.showDialog(dialog);
     }
 
     public void addViewListener(ViewListener listener) {
         synchronized (lock) {
             viewListeners.add(listener);
         }
     }
 
     public void removeViewListener(ViewListener listener) {
         synchronized (lock) {
             viewListeners.remove(listener);
         }
     }
 
     protected void clearViewListeners() {
         synchronized (lock) {
             viewListeners.clear();
         }
     }
 
     public void addTickListener(TickListener listener) {
         synchronized (lock) {
             tickListeners.add(listener);
         }
     }
 
     public void removeTickListener(TickListener listener) {
         synchronized (lock) {
             tickListeners.remove(listener);
         }
     }
 
     protected void clearTickListeners() {
         synchronized (lock) {
             tickListeners.clear();
         }
     }
 
     public void dataUpdated(int agent, int type, int value) {
         fireDataUpdated(agent, type, value);
     }
 
     public void dataUpdated(int agent, int type, long value) {
         fireDataUpdated(agent, type, value);
     }
 
     public void dataUpdated(int agent, int type, float value) {
         fireDataUpdated(agent, type, value);
     }
 
     public void dataUpdated(int agent, int type, double value) {
         fireDataUpdated(agent, type, value);
     }
 
     public void dataUpdated(int agent, int type, String value) {
         fireDataUpdated(agent, type, value);
     }
 
     public void dataUpdated(int agent, int type, Transportable value) {
         fireDataUpdated(agent, type, value);
     }
 
     public void dataUpdated(int type, Transportable value) {
         fireDataUpdated(type, value);
     }
 
     public void participant(int agent, int role, String name, int participantID) {
         fireParticipant(agent, role, name, participantID);
     }
 
     protected void fireParticipant(int agent, int role, String name,
                                    int participantID) {
         synchronized (lock) {
             for (ViewListener listener : viewListeners) {
                 listener.participant(agent, role, name, participantID);
             }
         }
     }
 
     protected void fireDataUpdated(int agent, int type, int value) {
         synchronized (lock) {
             for (ViewListener listener : viewListeners) {
                 listener.dataUpdated(agent, type, value);
             }
         }
     }
 
     protected void fireDataUpdated(int agent, int type, long value) {
         synchronized (lock) {
             for (ViewListener listener : viewListeners) {
                 listener.dataUpdated(agent, type, value);
             }
         }
     }
 
     protected void fireDataUpdated(int agent, int type, float value) {
         synchronized (lock) {
             for (ViewListener listener : viewListeners) {
                 listener.dataUpdated(agent, type, value);
             }
         }
     }
 
     protected void fireDataUpdated(int agent, int type, double value) {
         for (ViewListener listener : viewListeners) {
             listener.dataUpdated(agent, type, value);
         }
     }
 
     protected void fireDataUpdated(int agent, int type, String value) {
         synchronized (lock) {
             for (ViewListener listener : viewListeners) {
                 listener.dataUpdated(agent, type, value);
             }
         }
     }
 
     protected void fireDataUpdated(int agent, int type, Transportable value) {
         synchronized (lock) {
             for (ViewListener listener : viewListeners) {
                 listener.dataUpdated(agent, type, value);
             }
         }
     }
 
     protected void fireDataUpdated(int type, Transportable value) {
         synchronized (lock) {
             for (ViewListener listener : viewListeners) {
                 listener.dataUpdated(type, value);
             }
         }
 
     }
 
     protected void fireTick(long serverTime) {
         synchronized (lock) {
             for (TickListener listener : new CopyOnWriteArrayList<TickListener>(tickListeners)) {
                 listener.tick(serverTime);
             }
         }
     }
 
     protected void fireSimulationTick(long serverTime, int timeUnit) {
         synchronized (lock) {
             for (TickListener listener : new CopyOnWriteArrayList<TickListener>(tickListeners)) {
                 listener.simulationTick(serverTime, timeUnit);
             }
         }
     }
 }
