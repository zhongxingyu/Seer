 package edu.illinois.cs.mr;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Enumeration;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.Timer;
 import javax.swing.border.Border;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import edu.illinois.cs.mr.jm.AttemptStatus;
 import edu.illinois.cs.mr.jm.JobID;
 import edu.illinois.cs.mr.jm.JobStatus;
 import edu.illinois.cs.mr.jm.Phase;
 import edu.illinois.cs.mr.jm.TaskStatus;
 import edu.illinois.cs.mr.util.ImmutableStatus;
 import edu.illinois.cs.mr.util.RPC;
 import edu.illinois.cs.mr.util.Status.State;
 
 public class NodeUI extends JFrame implements TreeSelectionListener {
 
     private static final long serialVersionUID = 6732880434775381175L;
     
     private JLabel treeLabel, labelID, labelState, labelDetail, labelMessage, 
                     labelTotal, labelWaiting, labelRunning;
     private JTree tree;
     private JButton bWrite;
     private DefaultMutableTreeNode root;
     private int nodeId;
     private DefaultTreeModel treeModel;
     private JTextField tfInput, tfJar;
     static NodeService services;
     private double throttleValue;
     private File fJar, fInput, fOutput;
     private JobID selectedJobID;
     
     @SuppressWarnings("rawtypes")
     private void BuildTree() {
         JobID[] jobIds = null;
         try {
             jobIds = services.getJobIDs();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         if(jobIds != null) {
             for(int i=0; i<jobIds.length; i++)
             {
                 boolean jobfound = false;
                 DefaultMutableTreeNode jobnode = null;
                 for (Enumeration e = root.children(); e.hasMoreElements() ;) {
                     jobnode = (DefaultMutableTreeNode) e.nextElement();
                     int nid = Integer.parseInt(((String)jobnode.getUserObject()).split(" ")[1]);
                     if(jobIds[i].getValue() == nid) {
                         jobfound = true;
                         break;
                     }
                 }
                 if(!jobfound)
                 {
                     jobnode = new DefaultMutableTreeNode("Job " + jobIds[i].getValue());
                     treeModel.insertNodeInto(jobnode, root, root.getChildCount());
                 }
                 
                 try {
                     JobStatus job = services.getJobStatus(jobIds[i]);
                     
                     for (Phase phase : Phase.values())
                     {
                         DefaultMutableTreeNode phasenode = null;
                         boolean phasefound = false;
                         for (Enumeration e = jobnode.children(); e.hasMoreElements() ;) {
                             phasenode = (DefaultMutableTreeNode) e.nextElement();
                             String phz = ((String)phasenode.getUserObject()).split(" ")[1];
                             if(phz.equals(phase.name())) {
                                 phasefound = true;
                                 break;
                             }
                         }
                         if(!phasefound) {
                             phasenode = new DefaultMutableTreeNode("Phase " + phase.name());
                             treeModel.insertNodeInto(phasenode, jobnode, jobnode.getChildCount());
                         }
                         
                         for (TaskStatus task : job.getTaskStatuses(phase)) {
                             DefaultMutableTreeNode tasknode = null;
                             boolean taskfound = false;
                             for (Enumeration e = phasenode.children(); e.hasMoreElements() ;) {
                                 tasknode = (DefaultMutableTreeNode) e.nextElement();
                                 int tid = Integer.parseInt(((String)tasknode.getUserObject()).split(" ")[1]);
                                 if(task.getId().getValue() == tid) {
                                     taskfound = true;
                                     break;
                                 }
                             }
                             if(!taskfound)
                             {
                                 tasknode = new DefaultMutableTreeNode("Task " + task.getId().getValue());
                                 treeModel.insertNodeInto(tasknode, phasenode, phasenode.getChildCount());
                             }
                             
                             for (AttemptStatus attempt : task.getAttemptStatuses()) {
                                 DefaultMutableTreeNode attemptnode = null;
                                 boolean attemptfound = false;
                                 for (Enumeration e = tasknode.children(); e.hasMoreElements() ;) {
                                     attemptnode = (DefaultMutableTreeNode) e.nextElement();
                                     int aid = Integer.parseInt(((String)attemptnode.getUserObject()).split(" ")[1]);
                                     if(attempt.getId().getValue() == aid) {
                                         attemptfound = true;
                                         break;
                                     }
                                 }
                                 if(!attemptfound)
                                 {
                                     attemptnode = new DefaultMutableTreeNode("Attempt " + attempt.getId().getValue());
                                     treeModel.insertNodeInto(attemptnode, tasknode, tasknode.getChildCount());
                                 }
                             }
                         }
                     }
                 } catch(IOException e) { }
             }
         }
     }
     
     private String GetTotalTime(ImmutableStatus<?> status) {
         String ret = "n/a";
         if (status.isDone()) {
             long created = status.getCreatedTime();
             long done = status.getDoneTime();
             long total = done - created;
             ret = String.valueOf(total);
         }
         return ret;
     }
     
     private String GetWaitTime(ImmutableStatus<?> status) {
         String ret = "n/a";
         if (status.isDone()) {
             long waiting = status.getBeginWaitingTime();
             long running = status.getBeginRunningTime();
 
             // we may not know each event, due to our heart beat frequency
             // neither known
             if (waiting != -1 && running != -1) {
                 ret = String.valueOf(running - waiting);
             }
         }
         return ret;
     }
     
     private String GetRunTime(ImmutableStatus<?> status) {
         String ret = "n/a";
         if (status.isDone()) {
             long running = status.getBeginRunningTime();
             long done = status.getDoneTime();
 
             // running time known
             if (running != -1) {
                 ret = String.valueOf(done - running);
             }
         }
         return ret;
     }
     
     private void DrawSelectedData(DefaultMutableTreeNode node) {
         String title = (String) node.getUserObject();
         
         String type = title.split(" ")[0];
         String id = title.split(" ")[1];
         
         labelID.setText(type + " ID: \t" + id);
         labelID.setVisible(true);
         
         try {
             if(type.startsWith("Job")) {
                 selectedJobID = JobID.fromQualifiedString(nodeId + "-" + id);
                 JobStatus js = services.getJobStatus(JobID.fromQualifiedString(nodeId + "-" + id));
                 labelState.setText("State: \t" + js.getState());
                 labelState.setVisible(true);
                 labelDetail.setText("Phase: \t" + js.getPhase());
                 labelDetail.setVisible(true);
                 if(js.isDone()) {
                     if(js.getState() == State.SUCCEEDED) {
                         bWrite.setVisible(true); 
                     }
                     labelTotal.setText("Total Time: \t" + GetTotalTime(js));
                     labelTotal.setVisible(true);
                     labelWaiting.setText("Waiting Time: \t" + GetWaitTime(js));
                     labelWaiting.setVisible(true);
                     labelRunning.setText("Running Time: \t" + GetRunTime(js));
                     labelRunning.setVisible(true);
                 } else {
                     labelTotal.setVisible(false);
                     labelWaiting.setVisible(false);
                     labelRunning.setVisible(false);
                     bWrite.setVisible(false);
                 }
                 labelMessage.setVisible(false);
             }
             else if(type.startsWith("Phase")) {
                 DefaultMutableTreeNode jobnode = (DefaultMutableTreeNode)node.getParent();
                 String jobid = ((String)jobnode.getUserObject()).split(" ")[1];
                 String phaseid = ((String)node.getUserObject()).split(" ")[1];
                 JobStatus js = services.getJobStatus(JobID.fromQualifiedString(nodeId + "-" + jobid));
                 ImmutableStatus<JobID> phase = js.getPhaseStatus(Phase.valueOf(phaseid));
                if(phase.getState() !=null)
                    labelState.setText("State: \t" + phase.getState());
                else
                    labelState.setText("State: \tWaiting");
                 labelState.setVisible(true);
                 labelMessage.setVisible(false);
                 labelDetail.setVisible(false);
                 bWrite.setVisible(false);
                 if(phase.isDone()) {
                     labelTotal.setText("Total Time: \t" + GetTotalTime(phase));
                     labelTotal.setVisible(true);
                     labelWaiting.setText("Waiting Time: \t" + GetWaitTime(phase));
                     labelWaiting.setVisible(true);
                     labelRunning.setText("Running Time: \t" + GetRunTime(phase));
                     labelRunning.setVisible(true);
                 } else {
                     labelTotal.setVisible(false);
                     labelWaiting.setVisible(false);
                     labelRunning.setVisible(false);
                 }
             }
             else if(type.startsWith("Task")) {
                 DefaultMutableTreeNode phasenode = (DefaultMutableTreeNode)node.getParent();
                 DefaultMutableTreeNode jobnode = (DefaultMutableTreeNode)phasenode.getParent();
                 String jobid = ((String)jobnode.getUserObject()).split(" ")[1];
                 String phaseid = ((String)phasenode.getUserObject()).split(" ")[1];
                 JobStatus js = services.getJobStatus(JobID.fromQualifiedString(nodeId + "-" + jobid));
                 for (TaskStatus task : js.getTaskStatuses(Phase.valueOf(phaseid))) {
                     if(task.getId().getValue() == Integer.parseInt(id)) {
                         labelState.setText("State: \t" + task.getState());
                         labelState.setVisible(true);
                         labelMessage.setVisible(false);
                         labelDetail.setVisible(false);
                         bWrite.setVisible(false);
                         if(task.isDone()) {
                             labelTotal.setText("Total Time: \t" + GetTotalTime(task));
                             labelTotal.setVisible(true);
                             labelWaiting.setText("Waiting Time: \t" + GetWaitTime(task));
                             labelWaiting.setVisible(true);
                             labelRunning.setText("Running Time: \t" + GetRunTime(task));
                             labelRunning.setVisible(true);
                         } else {
                             labelTotal.setVisible(false);
                             labelWaiting.setVisible(false);
                             labelRunning.setVisible(false);
                         }
                         break;
                     }    
                 }
             }
             else if(type.startsWith("Attempt")) {
                 DefaultMutableTreeNode tasknode = (DefaultMutableTreeNode)node.getParent();
                 String taskid = ((String)tasknode.getUserObject()).split(" ")[1];
                 DefaultMutableTreeNode phasenode = (DefaultMutableTreeNode)tasknode.getParent();
                 String phaseid = ((String)phasenode.getUserObject()).split(" ")[1];
                 DefaultMutableTreeNode jobnode = (DefaultMutableTreeNode)phasenode.getParent();
                 String jobid = ((String)jobnode.getUserObject()).split(" ")[1];
                 JobStatus js = services.getJobStatus(JobID.fromQualifiedString(nodeId + "-" + jobid));
                 bWrite.setVisible(false);
                 for (TaskStatus task : js.getTaskStatuses(Phase.valueOf(phaseid))) {
                     if(task.getId().getValue() == Integer.parseInt(taskid)) {
                         for (AttemptStatus attempt : task.getAttemptStatuses()) {
                             if(attempt.getId().getValue() == Integer.parseInt(id)) {
                                 labelState.setText("State: \t" + attempt.getState());
                                 labelState.setVisible(true);
                                 labelDetail.setText("On Node: \t" + attempt.getTargetNodeID());
                                 labelDetail.setVisible(true);
                                 if (attempt.getMessage() != null) {
                                     labelMessage.setText("Message: \t" + attempt.getMessage());
                                     labelMessage.setVisible(true);
                                 }
                                 if(attempt.isDone()) {
                                     labelTotal.setText("Total Time: \t" + GetTotalTime(attempt));
                                     labelTotal.setVisible(true);
                                     labelWaiting.setText("Waiting Time: \t" + GetWaitTime(attempt));
                                     labelWaiting.setVisible(true);
                                     labelRunning.setText("Running Time: \t" + GetRunTime(attempt));
                                     labelRunning.setVisible(true);
                                 } else {
                                     labelTotal.setVisible(false);
                                     labelWaiting.setVisible(false);
                                     labelRunning.setVisible(false);
                                 }
                                 return;
                             }
                         }
                         // If we got here, the task wasn't found so it must have been deleted
                         labelState.setText("State: \tDeleted");
                         labelState.setVisible(true);
                         labelDetail.setVisible(false);
                         labelMessage.setVisible(false);
                         labelTotal.setVisible(false);
                         labelWaiting.setVisible(false);
                         labelRunning.setVisible(false);
                     }    
                 }
             }
         } catch(IOException ioe) { }
     }
 
     public static void main(String[] args) {
         String host;
         int port;
         if (args.length > 1 && args[0].equals("-n")) {
             String[] target = args[1].split(":");
             host = target[0];
             port = Integer.parseInt(target[1].trim());
         } else {
             host = "localhost";
             port = 60001;
         }
         try {
             services = RPC.newClient(host, port, NodeService.class);
             } catch(Exception e) {
                 System.out.println("Error connecting to node " + host + ":" + port + ".\n");
                 System.exit(2);
             } 
         
         NodeUI frame = new NodeUI();
         
         frame.setVisible(true);
     }
 
     public NodeUI() {
         fJar = null;
         fInput = null;
         fOutput = null;
         selectedJobID = null;
         SetupFrame();
         SetupGUIElements();
         BuildTree();
         
         Action updateData = new AbstractAction() {
             private static final long serialVersionUID = -2391347399892015494L;
             public void actionPerformed(ActionEvent e) {
                 BuildTree();
                 TreePath stp = tree.getSelectionPath();
                 if(stp!=null) {
                     DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)(stp.getLastPathComponent());
                     DrawSelectedData(selectedNode);
                 }
             }
         };
         
         new Timer(1000, updateData).start();
     }
   
     private void SetupFrame() {
         this.setSize(800, 600);
         this.setLocation(7, 7);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setTitle("MP4 Status Console");
         this.setResizable(false);
         
         try {
             BufferedImage icon = ImageIO.read(this.getClass().getResource("/icon.png"));
             this.setIconImage(icon);
         } catch (Exception e) { }
     }
   
     private void SetupGUIElements() {
         JPanel pane = new JPanel(null);
         
         nodeId = services.getId().getValue();
         
         treeLabel = new JLabel("Jobs Created on Node " + nodeId + ":");
         treeLabel.setLocation(12, 10);
         treeLabel.setSize(300,20);
         pane.add(treeLabel);
         
         root = new DefaultMutableTreeNode("Node "+nodeId);
         treeModel = new DefaultTreeModel(root);
         tree = new JTree(treeModel);
         tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         tree.addTreeSelectionListener(this);
         tree.setShowsRootHandles(true);
         JScrollPane scrollPane = new JScrollPane(tree, 
             JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         scrollPane.setLocation(10, 35);
         scrollPane.setSize(300, 525);
         pane.add(scrollPane);
         
         labelID = new JLabel("ID: \t");
         labelID.setSize(300, 20);
         labelID.setLocation(350, 40);
         labelID.setVisible(false);
         pane.add(labelID);
         
         labelState = new JLabel("State: \t");
         labelState.setSize(200, 20);
         labelState.setLocation(350, 80);
         labelState.setVisible(false);
         pane.add(labelState);
         
         labelDetail = new JLabel(": \t");
         labelDetail.setSize(300, 20);
         labelDetail.setLocation(350, 120);
         labelDetail.setVisible(false);
         pane.add(labelDetail);
         
         labelMessage = new JLabel("Message: \t");
         labelMessage.setSize(300, 20);
         labelMessage.setLocation(350, 160);
         labelMessage.setVisible(false);
         pane.add(labelMessage);
         
         labelTotal = new JLabel("Total Time: \t");
         labelTotal.setSize(300, 20);
         labelTotal.setLocation(350, 200);
         labelTotal.setVisible(false);
         pane.add(labelTotal);
         
         labelWaiting = new JLabel("Waiting Time: \t");
         labelWaiting.setSize(300, 20);
         labelWaiting.setLocation(350, 240);
         labelWaiting.setVisible(false);
         pane.add(labelWaiting);
         
         labelRunning = new JLabel("Running Time: \t");
         labelRunning.setSize(300, 20);
         labelRunning.setLocation(350, 280);
         labelRunning.setVisible(false);
         pane.add(labelRunning);
         
         JPanel panelNode = new JPanel(null);
         Border etched = BorderFactory.createEtchedBorder();
         Border titled = BorderFactory.createTitledBorder(etched, "Node " + nodeId + " Options");
         panelNode.setBorder(titled);
         panelNode.setSize(420, 240);
         panelNode.setLocation(350, 320);
         
         JPanel panelThrottle = new JPanel(new BorderLayout());
         Border etched2 = BorderFactory.createEtchedBorder();
         Border titled2 = BorderFactory.createTitledBorder(etched2, "Throttle");
         panelThrottle.setBorder(titled2);
         panelThrottle.setSize(150, 100);
         panelThrottle.setLocation(10, 20);
         throttleValue = 0;
         
         JSlider slideThrottle = new JSlider();
         slideThrottle.setValue((int)throttleValue);
         slideThrottle.setMajorTickSpacing(20);
         slideThrottle.setMinorTickSpacing(10);
         slideThrottle.setMinimum(0);
         slideThrottle.setMaximum(100);
         slideThrottle.setPaintTicks(true);
         slideThrottle.setPaintLabels(true);
         slideThrottle.addChangeListener(new ChangeListener() {
             public void stateChanged (ChangeEvent event)
             {
                 throttleValue = (double)((JSlider)event.getSource()).getValue();
             }
         });
         slideThrottle.setVisible(true);
         panelThrottle.add(slideThrottle, BorderLayout.NORTH);
         
         JButton bThrottle = new JButton("Set");
         bThrottle.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 try {
                     services.setThrottle(throttleValue);
                 } catch(IOException ioe) { }
             }
           });
         bThrottle.setVisible(true);
         panelThrottle.add(bThrottle, BorderLayout.SOUTH);
         
         panelThrottle.setVisible(true);
         panelNode.add(panelThrottle);
         
         JPanel panelSubmit = new JPanel(null);
         Border etched3 = BorderFactory.createEtchedBorder();
         Border titled3 = BorderFactory.createTitledBorder(etched3, "Submit Job");
         panelSubmit.setBorder(titled3);
         panelSubmit.setSize(240, 210);
         panelSubmit.setLocation(170, 20);
                 
         JLabel labelJar = new JLabel("Job Jar:");
         labelJar.setSize(150,20);
         labelJar.setLocation(10,20);
         labelJar.setVisible(true);
         panelSubmit.add(labelJar);
         
         tfJar = new JTextField();
         tfJar.setEditable(false);
         tfJar.setSize(180,25);
         tfJar.setLocation(10,50);
         tfJar.setVisible(true);
         panelSubmit.add(tfJar);
         
         JButton bJar = new JButton("...");
         bJar.setSize(35, 25);
         bJar.setLocation(195, 50);
         bJar.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 final JFileChooser fc = new JFileChooser();
                 int returnVal = fc.showOpenDialog((Component) e.getSource());
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     fJar = fc.getSelectedFile();
                     tfJar.setText(fJar.getName());
                 }
             }
         });
         bJar.setVisible(true);
         panelSubmit.add(bJar);
                 
         JLabel labelInput = new JLabel("Input File:");
         labelInput.setSize(150,20);
         labelInput.setLocation(10,85);
         labelInput.setVisible(true);
         panelSubmit.add(labelInput);
         
         tfInput = new JTextField();
         tfInput.setEditable(false);
         tfInput.setSize(180,25);
         tfInput.setLocation(10,115);
         tfInput.setVisible(true);
         panelSubmit.add(tfInput);
         
         JButton bInput = new JButton("...");
         bInput.setSize(35, 25);
         bInput.setLocation(195, 115);
         bInput.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 final JFileChooser fc = new JFileChooser();
                 int returnVal = fc.showOpenDialog((Component) e.getSource());
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     fInput = fc.getSelectedFile();
                     tfInput.setText(fInput.getName());
                 }
             }
         });
         bInput.setVisible(true);
         panelSubmit.add(bInput);
                 
         JButton bSubmit = new JButton("Submit");
         bSubmit.setSize(140, 30);
         bSubmit.setLocation(50, 170);
         bSubmit.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(fJar == null || fInput == null) {
                     JOptionPane.showMessageDialog((Component) e.getSource(), 
                                   "Invalid Jar and/or Input file(s).", "Error",
                                   JOptionPane.ERROR_MESSAGE);
                 }
                 else {
                     try {    
                         JobID id = services.submitJob(fJar, fInput);
                         JOptionPane.showMessageDialog((Component) e.getSource(), 
                                       "Job " + id.getValue() + " submitted.",
                                       "Job Submitted", JOptionPane.INFORMATION_MESSAGE);
                         fJar= null;
                         fInput = null;
                         tfJar.setText("");
                         tfInput.setText("");
                     }
                     catch(IOException ioe) {
                         JOptionPane.showMessageDialog((Component) e.getSource(),
                                       "Error submitting job.", "Error", 
                                       JOptionPane.ERROR_MESSAGE);
                     }
                 }
             }
         });
         bSubmit.setVisible(true);
         panelSubmit.add(bSubmit);
                 
                 panelSubmit.setVisible(true);
                 panelNode.add(panelSubmit);
         
         JButton bStop = new JButton("Stop Node");
         bStop.setSize(140, 30);
         bStop.setLocation(14, 160);
         bStop.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
               int option = JOptionPane.showConfirmDialog((Component) e.getSource(),
                              "Stop node and exit GUI?", "Stop and Exit?", 
                              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
               if(option == JOptionPane.YES_OPTION) {
                   services.stop();
                   System.exit(0);
               }
             }
           });
         bStop.setVisible(true);
         panelNode.add(bStop);
         
         
         panelNode.setVisible(true);
         pane.add(panelNode);
         
         
         bWrite = new JButton("Save Job Output...");
         bWrite.setSize(170, 30);
         bWrite.setLocation(600, 75);
         bWrite.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 final JFileChooser fc = new JFileChooser();
                 int returnVal = fc.showSaveDialog((Component) e.getSource());
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     fOutput = fc.getSelectedFile();
                     boolean ret = false;
                     try {
                         ret = services.writeOutput(selectedJobID, fOutput);
                     } catch(IOException ioe) { }
                     if(!ret)
                         JOptionPane.showMessageDialog((Component) e.getSource(),
                                           "Error saving job output.", "Error", 
                                           JOptionPane.ERROR_MESSAGE);
                     else
                         JOptionPane.showMessageDialog((Component) e.getSource(),
                                           "Job output saved.", "Success", 
                                           JOptionPane.INFORMATION_MESSAGE);
                 }
             }
           });
         bWrite.setVisible(false);
         this.add(bWrite);
         
         this.add(pane);
     }
         
     private void ClearData() {
         labelID.setVisible(false);
         labelState.setVisible(false);
         labelDetail.setVisible(false);
         labelMessage.setVisible(false);
         labelTotal.setVisible(false);
         labelWaiting.setVisible(false);
         labelRunning.setVisible(false);
         bWrite.setVisible(false);
     }
   
     /**
      * Returns the last path element of the selection.
      */
     public void valueChanged(TreeSelectionEvent e) {
 
         DefaultMutableTreeNode node = 
                 (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
 
         if (node == null)               //Nothing is selected.     
         {
                 ClearData();
                 return;
         }
 
         if (!node.isRoot()) {
                 DrawSelectedData(node);
         }
         else {
                 tree.setSelectionPath(null);
                 ClearData();
         }
     }
 }
