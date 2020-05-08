 package edu.umich.sbolt;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.event.WindowStateListener;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.Action;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import edu.umich.soar.SoarProperties;
 
 import lcm.lcm.LCM;
 import lcm.lcm.LCMDataInputStream;
 import lcm.lcm.LCMSubscriber;
 import sml.Agent;
 import sml.Agent.OutputEventInterface;
 import sml.Agent.RunEventInterface;
 import sml.FloatElement;
 import sml.Identifier;
 import sml.Kernel;
 import sml.smlRunEventId;
 import sml.WMElement;
 import sun.security.util.Debug;
 import april.lcmtypes.object_data_t;
 import april.lcmtypes.observations_t;
 import april.lcmtypes.robot_command_t;
 
 public class SBolt implements LCMSubscriber, OutputEventInterface,
         RunEventInterface
 {
     private LCM lcm;
 
     private Kernel kernel;
 
     private Agent agent;
 
     private robot_command_t command;
 
     private Timer timer;
 
     private TimerTask timerTask;
 
     private boolean running;
 
     private JFrame chatFrame;
 
     private JTextArea chatArea;
 
     private JTextField chatField;
 
     private List<String> chatMessages;
 
     // Identifiers for input link
 
     // The most recent observations_t received
     private observations_t currentObservation;
 
     // Root identifier for all object oberservations
     private Identifier observationsId;
 
     // Maps observation IDs onto their object-oberservation identifiers
     private Map<Integer, Identifier> observationsMap;
 
     // Root identifier for all sensible observations
     private Identifier sensiblesId;
 
     // Root identifier for all messages the robot recieves
     private Identifier messagesId;
 
     // A counter that is used for the id for each message
     private int messageIdNum;
 
     // A queue of the messages received since the last input phase
     private List<String> chatMessageQueue;
 
     public SBolt(String channel, String agentName)
     {
         currentObservation = null;
 
         // Initialize instance variables
         observationsMap = new HashMap<Integer, Identifier>();
         try
         {
             lcm = new LCM();
             lcm.subscribe(channel, this);
         }
         catch (IOException e)
         {
             e.printStackTrace();
             System.exit(1);
         }
         command = new robot_command_t();
         command.action = "";
 
         kernel = Kernel.CreateKernelInNewThread();
         agent = kernel.CreateAgent(agentName);
         if (agent == null)
         {
             throw new IllegalStateException("Kernel created null agent");
         }
         Boolean loadResult = agent.LoadProductions("agent/simple_agent.soar");
 
         agent.AddOutputHandler("command", this, null);
         agent.AddOutputHandler("message", this, null);
         
         // !!! Important !!!
         // We set AutoCommit to false, and only commit inside of the event
         // handler
         // for the RunEvent right before the next Input Phase
         // Otherwise the system would apparently hang on a commit
         kernel.SetAutoCommit(false);
         agent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE,
                 this, null);
         
         agent.SpawnDebugger(kernel.GetListenerPort(), System.getenv().get("SOAR_HOME"));
 
 
         // Set up input link.
         initInputLink();
         
         // Start broadcasting
         timerTask = new TimerTask()
         {
             @Override
             public void run()
             {
                 SBolt.this.broadcastLcmCommand();
             }
         };
 
         running = false;
 
         // Set up chat frame
         initChatFrame();
     }
 
     public void start()
     {
         if (running)
         {
             return;
         }
         running = true;
         timer = new Timer();
         timer.schedule(timerTask, 1000, 500);
         agent.RunSelfForever();
     }
 
     public void stop()
     {
         if (!running)
         {
             return;
         }
         running = false;
         agent.StopSelf();
         timer.cancel();
     }
 
     private void initInputLink()
     {
         Identifier il = agent.GetInputLink();
         observationsId = il.CreateIdWME("objects");
         sensiblesId = il.CreateIdWME("sensibles");
         messagesId = il.CreateIdWME("messages");
         agent.Commit();
     }
 
     private void initChatFrame()
     {
         messageIdNum = 100;
         chatMessageQueue = new ArrayList<String>();
         chatMessages = new ArrayList<String>();
         chatFrame = new JFrame("SBolt");
         
         chatArea = new JTextArea();
         JScrollPane pane = new JScrollPane(chatArea);
         chatField = new JTextField();
         JButton button = new JButton("Send Message");
         button.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 sendUserChat(chatField.getText());
                 chatField.setText("");
                 chatField.requestFocus();
             }
         });
 
         JSplitPane pane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                 chatField, button);
         JSplitPane pane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pane,
                 pane2);
 
         pane1.setDividerLocation(200);
         pane2.setDividerLocation(450);
 
         chatFrame.add(pane1);
         chatFrame.setSize(600, 300);
         chatField.getRootPane().setDefaultButton(button);
     }
 
     public void showFrame()
     {
         chatFrame.setDefaultCloseOperation(chatFrame.EXIT_ON_CLOSE);
         chatFrame.setVisible(true);
     }
 
     public void hideFrame()
     {
         chatFrame.setVisible(false);
     }
 
     private void sendUserChat(String message)
     {
         chatMessageQueue.add(message);
         addChatMessage(message);
     }
 
     private void sendSoarChat(String message)
     {
         addChatMessage(message);
     }
 
     private void addChatMessage(String message)
     {
         chatMessages.add(message);
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < chatMessages.size(); ++i)
         {
             sb.append(chatMessages.get(i));
             if (i + 1 < chatMessages.size())
             {
                 sb.append('\n');
             }
         }
         chatArea.setText(sb.toString());
     }
 
     @Override
     public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
     {
         observations_t obs = null;
         try
         {
             obs = new observations_t(ins);
         }
         catch (IOException e)
         {
             e.printStackTrace();
             return;
         }
         currentObservation = obs;
     }
 
     /********************************************************************
      * InputLink Handling
      *******************************************************************/
     
     // Called right before the Agent's Input Phase,
     // Update the Input Link Here
     public void runEventHandler(int eventID, Object data, Agent agent, int phase)
     {
        updateInputLinkWorld();
         updateInputLinkMessages();
         agent.Commit();
     }
 
     private void updateInputLinkWorld()
     {
         if (currentObservation == null)
         {
             return;
         }
 
         // Create a set of all the ids of all observations.
         Set<Integer> obsIds = new HashSet<Integer>();
         for (object_data_t objData : currentObservation.observations)
         {
             obsIds.add(objData.id);
         }
 
         // Remove observation wmes that aren't currently being observed.
         List<Integer> toRemove = new ArrayList<Integer>();
         for (Integer key : observationsMap.keySet())
         {
             if (!obsIds.contains(key))
             {
                 toRemove.add(key);
             }
         }
         for (Integer key : toRemove)
         {
             Identifier identifier = observationsMap.get(key);
             identifier.DestroyWME();
             observationsMap.remove(key);
         }
 
         // For each observation, either update it if it exists or create it if
         // it doesn't.
         for (object_data_t obj : currentObservation.observations)
         {
             int id = obj.id;
             Identifier obsId = null;
             if (observationsMap.containsKey(id))
             {
                 obsId = observationsMap.get(id);
                 int numChildren = obsId.GetNumberChildren();
                 List<WMElement> wmesToRemove = new ArrayList<WMElement>();
                 for (int i = 0; i < numChildren; ++i)
                 {
                     WMElement childWme = obsId.GetChild(i);
                     if (childWme.GetAttribute().equals("id"))
                     {
                         continue;
                     }
                     wmesToRemove.add(obsId.GetChild(i));
                 }
                 for (WMElement wme : wmesToRemove)
                 {
                     wme.DestroyWME();
                 }
             }
             else
             {
                 obsId = observationsId.CreateIdWME("object");
                 observationsMap.put(id, obsId);
                 obsId.CreateIntWME("id", id);
             }
             for (String nounjective : obj.nounjective)
             {
                 obsId.CreateStringWME("nounjective", nounjective);
             }
             Identifier positionId = obsId.CreateIdWME("position");
             positionId.CreateFloatWME("x", obj.pos[0]);
             positionId.CreateFloatWME("y", obj.pos[1]);
             positionId.CreateFloatWME("t", obj.pos[2]);
         }
 
         // Remove all sensible WMEs and replace them with new ones
         List<WMElement> sensiblesToRemove = new ArrayList<WMElement>();
         for (int i = 0; i < observationsId.GetNumberChildren(); ++i)
         {
             WMElement child = sensiblesId.GetChild(i);
            if (child == null)
            {
                continue;
            }
            
             if (child.GetAttribute().equals("sensible"))
             {
                 sensiblesToRemove.add(child);
             }
         }
         for (WMElement child : sensiblesToRemove)
         {
             child.DestroyWME();
         }
 
         for (String sensibleStr : currentObservation.sensibles)
         {
             Identifier sensibleId = sensiblesId.CreateIdWME("sensible");
             String[] tokens = sensibleStr.split(",");
             for (String token : tokens)
             {
                 String[] pair = token.split("=");
                 if (pair.length != 2)
                 {
                     throw new IllegalStateException(
                             "Choked on parsing sensible message.");
                 }
                 String key = pair[0];
                 String value = pair[1];
                 Identifier attribute = sensibleId.CreateIdWME("attribute");
                 attribute.CreateStringWME("key", key);
                 attribute.CreateStringWME("value", value);
             }
         }
     }
 
     private void updateInputLinkMessages()
     {
         for (String message : chatMessageQueue)
         {
             String[] c = message.split(" and ");
             for (String m : c)
             {
                 
                 String[] words = m.split(" ");
                
                 Identifier mId = messagesId.CreateIdWME("message");
                 Identifier rest = mId.CreateIdWME("words");
                 mId.CreateIntWME("id", messageIdNum);
              
                 for (String w : words)  
                 {  
                     rest.CreateStringWME("first-word", w);
                
                     rest = rest.CreateIdWME("rest");
                 } 
 	                
                 messageIdNum++;
             }
         }
         chatMessageQueue.clear();
     }
 
     /********************************************************************
      * OutputLink Handling
      *******************************************************************/
     
     @Override
     public void outputEventHandler(Object data, String agentName,
             String attributeName, WMElement wme)
     {
         if (!(wme.IsJustAdded() && wme.IsIdentifier()))
         {
             return;
         }
 
         if (wme.GetAttribute().equals("command"))
         {
             processOutputLinkCommand(wme.ConvertToIdentifier());
         }
         else if (wme.GetAttribute().equals("message"))
         {
             processOutputLinkMessage(wme.ConvertToIdentifier());
         }
         
         if(agent.IsCommitRequired()){
             agent.Commit();
         }
     }
 
     private void processOutputLinkMessage(Identifier messageId)
     {
         if(messageId == null){
             return;
         }
         if (messageId.GetNumberChildren() == 0)
         {
             messageId.CreateStringWME("status", "error");
             throw new IllegalStateException(
                     "Message has no children");
         }
 
         String message = "";
         WMElement wordsWME = messageId.FindByAttribute("words", 0);
         if (wordsWME == null || !wordsWME.IsIdentifier())
         {
             messageId.CreateStringWME("status", "error");
             throw new IllegalStateException(
                     "Message has no words attribute");
         }
         Identifier currentWordId = wordsWME.ConvertToIdentifier();
 
         // Follows the linked list down until it can't find the 'rest' attribute
         // of a WME
         while (currentWordId != null)
         {
             Identifier nextWordId = null;
             for (int i = 0; i < currentWordId.GetNumberChildren(); i++)
             {
                 WMElement child = currentWordId.GetChild(i);
                 if (child.GetAttribute().equals("first-word"))
                 {
                     String currentWord = child.GetValueAsString();
                     if (message == "")
                     {
                         message = currentWord;
                     }
                     else
                     {
                         message += " " + currentWord;
                     }
                 }
                 else if (child.GetAttribute().equals("rest")
                         && child.IsIdentifier())
                 {
                     nextWordId = child.ConvertToIdentifier();
                 }
             }
             currentWordId = nextWordId;
         }
         
         if(message == ""){
             messageId.CreateStringWME("status", "error");
             throw new IllegalStateException(
                     "Message was empty");   
         }
         
         message += ".";
         addChatMessage(message);
         messageId.CreateStringWME("status", "complete");
     }
 
     private void processOutputLinkCommand(Identifier commandId)
     {
         if (commandId == null || commandId.GetNumberChildren() == 0)
         {
             return;
         }
 
         int numChildren = commandId.GetNumberChildren();
         for (int i = 0; i < numChildren; ++i)
         {
             WMElement child = commandId.GetChild(i);
 
             if (child.GetAttribute().equals("action"))
             {
                 processActionCommand(child.ConvertToIdentifier());
             }
             else if (child.GetAttribute().equals("destination"))
             {
                 processDestinationCommand(child.ConvertToIdentifier());
             }
             else if (child.GetAttribute().equals("gripper"))
             {
                 processGripperCommand(child.ConvertToIdentifier());
             }
         }
     }
     
     private void processDestinationCommand(Identifier destId){
         if(destId == null){
             return;
         }
         
         double x = 0.0;
         double y = 0.0;
         double t = 10.0;    //Set to 10 to ignore destination
         
         if(destId.FindByAttribute("None", 0) == null){
             WMElement xWme = destId.FindByAttribute("x", 0);
             WMElement yWme = destId.FindByAttribute("y", 0);
             WMElement tWme = destId.FindByAttribute("t", 0);
             
             if (xWme == null || yWme == null || tWme == null)
             {
                 destId.CreateStringWME("status", "error");
                 throw new IllegalStateException(
                         "Command has destination WME missing x, y, or t");
             }
             
             try{
                 x = Double.valueOf(xWme.GetValueAsString());
                 y = Double.valueOf(yWme.GetValueAsString());
                 t = Double.valueOf(tWme.GetValueAsString());
             } catch (Exception e){
                 destId.CreateStringWME("status", "error");
                 throw new IllegalStateException(
                         "Command has an invalid x, y, or t float");
             }
         }
         
         command.dest = new double[] { x, y, t };
         destId.CreateStringWME("status", "complete");
     }
     
     private void processActionCommand(Identifier actionId){
         if(actionId == null){
             return;
         }
 
         StringBuffer actionBuf = new StringBuffer();
         
         int pairCounter = 0;
         for(int i = 0; i < actionId.GetNumberChildren(); i++){
             WMElement childWME = actionId.GetChild(i);
             if(childWME.GetAttribute().equals("pair")){
                 Identifier pairId = childWME.ConvertToIdentifier();
                 if(pairId == null){
                     continue;
                 }
                 
                 //Get key of pair
                 WMElement keyWME = pairId.FindByAttribute("key", 0);
                 if (keyWME == null || keyWME.GetValueAsString().length() == 0)
                 {
                     actionId.CreateStringWME("status", "error");
                     throw new IllegalStateException(
                             "Action has a pair with no key");
                 }
                 String key = keyWME.GetValueAsString();
                 
                 //Get value of pair
                 WMElement valueWME = pairId.FindByAttribute("value", 0);
                 if (valueWME == null || valueWME.GetValueAsString().length() == 0)
                 {
                     actionId.CreateStringWME("status", "error");
                     throw new IllegalStateException(
                             "Action has a pair with no value");
                 }
                 String value = valueWME.GetValueAsString();
                 
                 actionBuf.append(key + "=" + value + ",");
                 pairCounter++;
             }
         }
 
         if(pairCounter == 0){
             actionId.CreateStringWME("status", "error");
             throw new IllegalStateException("Action is empty");
         }
 
         command.action = actionBuf.toString();
         command.action = command.action.substring(0,
                 command.action.length() - 1);
         actionId.CreateStringWME("status", "complete");
     }
     
     private void processGripperCommand(Identifier gripperId){
         if(gripperId == null){
             return;
         }
         
         WMElement performWME = gripperId.FindByAttribute("perform", 0);
         if(performWME == null){
             gripperId.CreateStringWME("status", "error");
             throw new IllegalStateException("Gripper command does not have a perform WME");
             
         }
         
         String gripperAction = performWME.GetValueAsString();
         if(!gripperAction.equals("open") && !gripperAction.equals("close")){
             gripperId.CreateStringWME("status", "error");
             throw new IllegalStateException("Gripper command is not 'open' or 'close'");
         }
         
         command.gripper_open = gripperAction.equals("open");
         gripperId.CreateStringWME("status", "complete");
     }
 
     private void broadcastLcmCommand()
     {           
         if (command == null)
         {
             return;
         }
         synchronized (command)
         {
             lcm.publish("sbolt_commands", command);
         }
     }
 
     public static void main(String[] args)
     {
         SBolt sbolt = new SBolt("abolt_observations", "sbolt");
         sbolt.showFrame();
         sbolt.start();
     }
 
 }
