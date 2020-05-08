 package edu.illinois.cs.mapreduce;
 
 import javax.swing.*;
 import java.awt.event.*;
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.Enumeration;
 
 import javax.swing.tree.*;
 import javax.swing.event.*;
 import edu.illinois.cs.mapreduce.Node.NodeServices;
 
 public class StatusUI extends JFrame implements TreeSelectionListener {
 
     private static final long serialVersionUID = 6732880434775381175L;
     
     private JLabel treeLabel, labelID, labelState, labelDetail, labelMessage;
     private JTree tree;
     private DefaultMutableTreeNode root;
     private DefaultTreeModel treeModel;
     static NodeServices services;
     
     @SuppressWarnings("rawtypes")
     private void BuildTree() {
         JobID[] jobIds = services.getJobIDs();
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
                 
                for (TaskStatus task : job.getMapTaskStatuses()) {
                     DefaultMutableTreeNode tasknode = null;
                     boolean taskfound = false;
                     for (Enumeration e = jobnode.children(); e.hasMoreElements() ;) {
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
                         treeModel.insertNodeInto(tasknode, jobnode, jobnode.getChildCount());
                     }
                     
                     for (TaskAttemptStatus attempt : task.getAttemptStatuses()) {
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
             } catch(IOException e) { }
         }
     }
     
     private void DrawSelectedData(DefaultMutableTreeNode node) {
         String title = (String) node.getUserObject();
         
         String type = title.split(" ")[0];
         String id = title.split(" ")[1];
         
         labelID.setText(type + " ID: \t" + id);
         labelID.setVisible(true);
         
         try {
             if(type.startsWith("Job")) {
                 JobStatus js = services.getJobStatus(JobID.fromQualifiedString(id));
                 labelState.setText("State: \t" + js.getState());
                 labelState.setVisible(true);
                 labelDetail.setText("Phase: \t" + js.getPhase());
                 labelDetail.setVisible(true);
                 labelMessage.setVisible(false);
             }
             else if(type.startsWith("Task")) {
                 DefaultMutableTreeNode jobnode = (DefaultMutableTreeNode)node.getParent();
                 String jobid = ((String)jobnode.getUserObject()).split(" ")[1];
                 JobStatus js = services.getJobStatus(JobID.fromQualifiedString(jobid));
                for (TaskStatus task : js.getMapTaskStatuses()) {
                     if(task.getId().getValue() == Integer.parseInt(id)) {
                         labelState.setText("State: \t" + task.getState());
                         labelState.setVisible(true);
                         labelMessage.setVisible(false);
                         labelDetail.setVisible(false);
                         break;
                     }    
                 }
             }
             else if(type.startsWith("Attempt")) {
                 DefaultMutableTreeNode tasknode = (DefaultMutableTreeNode)node.getParent();
                 String taskid = ((String)tasknode.getUserObject()).split(" ")[1];
                 DefaultMutableTreeNode jobnode = (DefaultMutableTreeNode)tasknode.getParent();
                 String jobid = ((String)jobnode.getUserObject()).split(" ")[1];
                 JobStatus js = services.getJobStatus(JobID.fromQualifiedString(jobid));
                for (TaskStatus task : js.getMapTaskStatuses()) {
                     if(task.getId().getValue() == Integer.parseInt(taskid)) {
                         for (TaskAttemptStatus attempt : task.getAttemptStatuses()) {
                             if(attempt.getId().getValue() == Integer.parseInt(id)) {
                                 labelState.setText("State: \t" + attempt.getState());
                                 labelState.setVisible(true);
                                 labelDetail.setText("On Node: \t" + attempt.getTargetNodeID());
                                 labelDetail.setVisible(true);
                                 if (attempt.getMessage() != null) {
                                     labelMessage.setText("Message: \t" + attempt.getMessage());
                                     labelMessage.setVisible(true);
                                 }
                                 return;
                             }
                         }
                     }    
                 }
             }
         } catch(IOException ioe) { }
     }
 
     public static void main(String[] args) {
         StatusUI frame = new StatusUI();
         String host;
         int port;
         if (args.length > 2 && args[1].equals("-n")) {
             String[] target = args[2].split(":");
             host = target[0];
             port = Integer.parseInt(target[1]);
         } else {
             host = "localhost";
             port = 60001;
         }
         try {
         services = RPC.newClient(host, port, NodeServices.class);
         } catch(IOException ioe) {
             System.out.println("Error connecting to node.\n");
             System.exit(2);
         } 
         
         frame.setVisible(true);
     }
 
     public StatusUI() {
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
             BufferedImage icon = ImageIO.read(this.getClass().getResource("/src/main/resources/icon.png"));
             this.setIconImage(icon);
         } catch (Exception e) { }
     }
   
     private void SetupGUIElements() {
         JPanel pane = new JPanel(null);
         
         treeLabel = new JLabel("Jobs Created on this Node:");
         treeLabel.setLocation(12, 10);
         treeLabel.setSize(300,20);
         pane.add(treeLabel);
         
         root = new DefaultMutableTreeNode("Jobs");
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
         labelState.setSize(300, 20);
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
         
         this.add(pane);
     }
         
     private void ClearData() {
         labelID.setVisible(false);
         labelState.setVisible(false);
         labelDetail.setVisible(false);
         labelMessage.setVisible(false);
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
