 /*
  * MultipleJobPanel.java
  *
  * Copyright (c) 2001 The European DataGrid Project - IST programme, all rights reserved.
  * Contributors are mentioned in the code where appropriate.
  *
  */
 
 package org.glite.wmsui.guij;
 
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.Color;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.border.*;
 import javax.swing.table.*;
 import javax.swing.SwingUtilities;
 import javax.swing.filechooser.FileFilter;
 
 import java.beans.VetoableChangeListener;
 import java.beans.PropertyVetoException;
 
 import org.glite.wms.jdlj.*;
 import org.glite.wmsui.apij.*;
 
 import org.apache.log4j.*;
 
import org.edg.info.Consumer;
import org.edg.info.ResultSet;
 import org.edg.info.CanonicalProducer;
 import org.edg.info.ServletConnection;
 
 
 /**
  * Implementation of the MultipleJobPanel class.
  *
  *
  * @ingroup gui
  * @brief
  * @version 1.0
  * @date 8 may 2002
  * @author Giuseppe Avellino <giuseppe.avellino@datamat.it>
  */
 public class MultipleJobPanel extends JPanel {
   static Logger logger = Logger.getLogger(GUIUserCredentials.class.getName());
 
   static final boolean THIS_CLASS_DEBUG = false;
   boolean isDebugging = THIS_CLASS_DEBUG || Utils.GLOBAL_DEBUG;
 
   static final int JOB_MONITOR_MAIN = 0;
   static final int MULTIPLE_JOB_PANEL = 1;
 
   int sortingColumn = Utils.NO_SORTING;
   boolean ascending = false;
 
   //static final String STATE_DONE = "Done";
 
   // Max number of jobs that can be simultaneously monitored
   static protected int maxMonitoredJobNumber = Utils.
       MAX_MONITORED_JOB_NUMBER_DEF_VAL; // Read this value from a configuration file??
 
   static final int JOB_ID_COLUMN_INDEX = 0;
   static final int JOB_TYPE_COLUMN_INDEX = 1;
   static final int JOB_STATUS_COLUMN_INDEX = 2;
   static final int SUBMISSION_TIME_COLUMN_INDEX = 3;
   static final int DESTINATION_COLUMN_INDEX = 4;
   //static final int NETWORK_SERVER_COLUMN_INDEX = 4;
 
   static final String[] stringHeader = {
       "Job Id", "Job Type", "Status", "Submitted", "Destination" //, "Network Server"
   };
 
   // Vector used to store the one returned by jobStatus().
   protected Vector jobVector;
 
   // Collection of jobs actually monitorated.
   protected JobCollection jobCollection = new JobCollection();
 
   // Hash map used to store job status. It is usefull when you ask for details
   // (the shown details will be read from this hash map without call jobStatus() again).
   protected HashMap jobStatusHashMap = new HashMap();
 
   static boolean isSingleJobDialogShown = false;
   static boolean isLogInfoJDialogShown = false;
   static StatusDetailsFrame singleJobDialog;
   static LogInfoFrame logInfoJDialog;
   JobMonitor jobMonitorJFrame;
   //MultipleJobFrame multipleJobFrame;
 
   Vector vectorHeader = new Vector();
 
   UpdateThread updateThread = new UpdateThread(this);
   CurrentTimeThread currentTimeThread = new CurrentTimeThread(this);
 
   protected JPopupMenu jPopupMenuTable = new JPopupMenu();
   JMenuItem jMenuItemRemove = new JMenuItem("Remove");
   JMenuItem jMenuItemClear = new JMenuItem("Clear");
   JMenuItem jMenuItemSelectAll = new JMenuItem("Select All");
   JMenuItem jMenuItemSelectNone = new JMenuItem("Select None");
   JMenuItem jMenuItemInvertSelection = new JMenuItem("Invert Selection");
   JMenuItem jMenuItemDetails = new JMenuItem("Details");
   JMenuItem jMenuItemLogInfo = new JMenuItem("Log Info");
   JMenuItem jMenuItemUpdate = new JMenuItem("Update");
   JMenuItem jMenuItemJobCancel = new JMenuItem("Job Cancel");
   JMenuItem jMenuItemJobOutput = new JMenuItem("Job Output");
   JMenuItem jMenuItemInteractiveConsole = new JMenuItem("Interactive Console");
   JMenuItem jMenuItemRetrieveCheckpointState = new JMenuItem(
       "Retrieve Checkpoint State");
   JMenuItem jMenuItemDagNodes = new JMenuItem("Dag Nodes");
   JMenuItem jMenuItemDagMonitor = new JMenuItem("Dag Monitor");
   JMenuItem jMenuItemSortAddingOrder = new JMenuItem("Sort by Adding Order");
 
   JobTableModel jobTableModel;
   JTable jTableJobs;
   JScrollPane jScrollPaneJobTable = new JScrollPane();
   JLabel jLabelTotalDisplayed = new JLabel();
   JLabel jLabelTotalDisplayedJobs = new JLabel();
   JTextField jTextFieldUserJobId = new JTextField();
   JButton jButtonClearJobId = new JButton();
   JButton jButtonAddJobId = new JButton();
   JPanel jPanelJobId = new JPanel();
   JPanel jPanelJobStatusTable = new JPanel();
   JButton jButtonUpdate = new JButton();
   JButton jButtonDetails = new JButton();
 
   JButton jButtonDagNodes = new JButton();
   //JButton jButtonDagMon = new JButton();
 
   JButton jButtonBack = new JButton();
   JLabel jLabelLastUp = new JLabel();
   JLabel jLabelLastUpdate = new JLabel();
   JLabel jTextFieldCurrentTime = new JLabel();
   JLabel jLabelCurrentTime = new JLabel();
   JButton jButtonLogInfo = new JButton();
   JButton jButtonGetOutput = new JButton();
   JButton jButtonCancel = new JButton();
 
   JPanel jPanelButton = new JPanel();
   JPanel jPanelJobIdButton = new JPanel();
   JPanel jPanelTime = new JPanel();
   JPanel jPanelJobStatusTableLabel = new JPanel();
   JPanel jPanelJobStatusTableButton = new JPanel();
   JPanel jPanelNorth = new JPanel();
   JPanel jPanelMain = new JPanel();
 
   /**
    * Constructor.
    */
   public MultipleJobPanel(JobMonitor jobMonitorJFrame,
       JobCollection jobCollection) {
     this.jobCollection = jobCollection;
     this.jobMonitorJFrame = jobMonitorJFrame;
     enableEvents(AWTEvent.WINDOW_EVENT_MASK);
     try {
       jbInit();
     } catch (Exception e) {
       if (isDebugging) {
         e.printStackTrace();
       }
     }
   }
 
   /**
    * Constructor.
    */
   public MultipleJobPanel(JobMonitor jobMonitorJFrame) {
     this.jobMonitorJFrame = jobMonitorJFrame;
     enableEvents(AWTEvent.WINDOW_EVENT_MASK);
     try {
       jbInit();
     } catch (Exception e) {
       if (isDebugging) {
         e.printStackTrace();
       }
     }
   }
 
   private void jbInit() throws Exception {
     isDebugging |= (logger.getRootLogger().getLevel() == Level.DEBUG) ? true : false;
 
     currentTimeThread.start();
 
     createJPopupMenu();
 
     for (int i = 0; i < stringHeader.length; i++) {
       vectorHeader.addElement(stringHeader[i]);
     }
     jobTableModel = new JobTableModel(vectorHeader, 0);
 
     Date date = new Date();
     String timeText = date.toString();
     jLabelLastUpdate.setText(timeText);
     jTableJobs = new JTable(jobTableModel);
     jTableJobs.getTableHeader().setReorderingAllowed(false);
     jTableJobs.setAutoCreateColumnsFromModel(false);
 
     TableColumn col = jTableJobs.getColumnModel().getColumn(JOB_ID_COLUMN_INDEX);
     //TableCellRenderer tableRenderer = col.getHeaderRenderer();
     //col.setHeaderRenderer(new RendererDecorator(tableRenderer));
     col.setCellRenderer(new GUITableTooltipCellRenderer());
 
     col = jTableJobs.getColumnModel().getColumn(JOB_TYPE_COLUMN_INDEX);
     col.setCellRenderer(new GUITableTooltipCellRenderer());
     col = jTableJobs.getColumnModel().getColumn(JOB_STATUS_COLUMN_INDEX);
     col.setCellRenderer(new GUITableCellRenderer(this));
     col = jTableJobs.getColumnModel().getColumn(DESTINATION_COLUMN_INDEX);
     col.setCellRenderer(new GUITableTooltipCellRenderer());
     //col = jTableJobs.getColumnModel().getColumn(NETWORK_SERVER_COLUMN_INDEX);
     col = jTableJobs.getColumnModel().getColumn(SUBMISSION_TIME_COLUMN_INDEX);
     col.setCellRenderer(new GUITableTooltipCellRenderer());
 
     jLabelTotalDisplayedJobs.setText(Integer.toString(jobTableModel.getRowCount()));
     jLabelTotalDisplayedJobs.setPreferredSize(new Dimension(50, 18));
     jScrollPaneJobTable.setHorizontalScrollBarPolicy(JScrollPane.
         HORIZONTAL_SCROLLBAR_NEVER);
     jScrollPaneJobTable.getViewport().setBackground(Color.white);
     jScrollPaneJobTable.setBorder(BorderFactory.createEtchedBorder());
     //setSize(new Dimension(680, 600));
 
     this.setLayout(new BorderLayout());
 
     jLabelTotalDisplayed.setHorizontalAlignment(SwingConstants.RIGHT);
     jLabelTotalDisplayed.setText("Total Displayed Jobs");
 
     jLabelTotalDisplayedJobs.setBorder(BorderFactory.createLoweredBevelBorder());
     jLabelTotalDisplayedJobs.setHorizontalAlignment(SwingConstants.RIGHT);
 
     jTextFieldUserJobId.addFocusListener(new java.awt.event.FocusAdapter() {
       public void focusLost(FocusEvent e) {
         jTextFieldUserJobIdFocusLost(e);
       }
     });
 
     jButtonClearJobId.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonClearJobIdEvent(e);
       }
     });
     jButtonClearJobId.setText("Clear");
     jButtonAddJobId.setText("Add");
 
     jButtonAddJobId.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonAddJobIdEvent(e);
       }
     });
     jPanelJobId.setBorder(new TitledBorder(new EtchedBorder(), " Job Id ", 0, 0,
         null, GraphicUtils.TITLED_ETCHED_BORDER_COLOR));
 
     jPanelJobId.setLayout(new BorderLayout());
     jPanelJobStatusTable.setBorder(new TitledBorder(new EtchedBorder(),
         " Job Status Table ", 0, 0,
         null, GraphicUtils.TITLED_ETCHED_BORDER_COLOR));
 
     jPanelJobStatusTable.setLayout(new BorderLayout());
 
     jButtonUpdate.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonUpdateEvent(e);
       }
     });
     jButtonUpdate.setText("Update");
 
     jButtonDetails.setText(" Details ");
     jButtonDetails.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonDetailsEvent(e);
       }
     });
 
     jButtonDagNodes.setText("Dag Nodes");
     jButtonDagNodes.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonDagNodesEvent(e);
       }
     });
 
     jButtonBack.setText("Back");
     jButtonBack.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonBackEvent(e);
       }
     });
 
     jLabelLastUp.setText("Last Table Update");
     jLabelLastUp.setHorizontalAlignment(SwingConstants.RIGHT);
 
     jLabelLastUpdate.setHorizontalAlignment(SwingConstants.RIGHT);
     jLabelLastUpdate.setBorder(BorderFactory.createLoweredBevelBorder());
     jTextFieldCurrentTime.setBorder(BorderFactory.createLoweredBevelBorder());
     jTextFieldCurrentTime.setHorizontalAlignment(SwingConstants.RIGHT);
 
     jLabelCurrentTime.setHorizontalAlignment(SwingConstants.RIGHT);
     jLabelCurrentTime.setText("Current Time");
 
     jButtonLogInfo.setText("Log Info");
     jButtonLogInfo.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonLogInfoEvent(e);
       }
     });
 
     jButtonGetOutput.setText("Job Output");
     jButtonGetOutput.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonGetOutputEvent(e);
       }
     });
 
     jButtonCancel.setText("Job Cancel");
     jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonCancelEvent(e);
       }
     });
 
     jPanelJobIdButton.setLayout(new BoxLayout(jPanelJobIdButton,
         BoxLayout.X_AXIS));
     jPanelJobIdButton.setBorder(GraphicUtils.SPACING_BORDER);
     jPanelJobIdButton.add(jButtonClearJobId, null);
     jPanelJobIdButton.add(Box.createGlue());
     jPanelJobIdButton.add(jButtonAddJobId, null);
 
     jPanelJobId.setLayout(new BorderLayout());
     jPanelJobId.add(jTextFieldUserJobId, BorderLayout.NORTH);
     jPanelJobId.add(Box.createHorizontalStrut(GraphicUtils.STRUT_GAP));
     jPanelJobId.add(jPanelJobIdButton, BorderLayout.SOUTH);
 
     jPanelButton.setLayout(new BoxLayout(jPanelButton, BoxLayout.X_AXIS));
     jPanelButton.setBorder(GraphicUtils.SPACING_BORDER);
     jPanelButton.add(jButtonUpdate, null);
     jPanelButton.add(Box.createHorizontalStrut(GraphicUtils.STRUT_GAP));
     jPanelButton.add(Box.createGlue());
     jPanelButton.add(jButtonBack, null);
 
     ((FlowLayout) jPanelTime.getLayout()).setAlignment(FlowLayout.RIGHT);
     jPanelTime.add(jLabelCurrentTime, null);
     jPanelTime.add(jTextFieldCurrentTime, null);
 
     jPanelJobStatusTableLabel.setLayout(new BoxLayout(jPanelJobStatusTableLabel,
         BoxLayout.X_AXIS));
     jPanelJobStatusTableLabel.setBorder(GraphicUtils.SPACING_BORDER);
     jPanelJobStatusTableLabel.add(jLabelTotalDisplayed, null);
     jPanelJobStatusTableLabel.add(Box.createHorizontalStrut(GraphicUtils.STRUT_GAP));
     jPanelJobStatusTableLabel.add(jLabelTotalDisplayedJobs, null);
     jPanelJobStatusTableLabel.add(Box.createHorizontalStrut(GraphicUtils.STRUT_GAP));
     jPanelJobStatusTableLabel.add(Box.createGlue());
     jPanelJobStatusTableLabel.add(jLabelLastUp, null);
     jPanelJobStatusTableLabel.add(Box.createHorizontalStrut(GraphicUtils.STRUT_GAP));
     jPanelJobStatusTableLabel.add(jLabelLastUpdate, null);
 
     jScrollPaneJobTable.getViewport().add(jTableJobs, null);
 
     jPanelJobStatusTableButton.setLayout(new BoxLayout(
         jPanelJobStatusTableButton, BoxLayout.X_AXIS));
     jPanelJobStatusTableButton.setBorder(GraphicUtils.SPACING_BORDER);
     jPanelJobStatusTableButton.add(jButtonDetails, null);
     jPanelJobStatusTableButton.add(Box.createHorizontalStrut(GraphicUtils.STRUT_GAP));
     jPanelJobStatusTableButton.add(jButtonDagNodes, null);
     jPanelJobStatusTableButton.add(Box.createHorizontalStrut(GraphicUtils.STRUT_GAP));
     //jPanelJobStatusTableButton.add(jButtonDagMon, null);
     //jPanelJobStatusTableButton.add(Box.createHorizontalStrut(GraphicUtils.STRUT_GAP));
     jPanelJobStatusTableButton.add(Box.createGlue());
     jPanelJobStatusTableButton.add(jButtonLogInfo, null);
     jPanelJobStatusTableButton.add(Box.createHorizontalStrut(GraphicUtils.STRUT_GAP));
     jPanelJobStatusTableButton.add(jButtonCancel, null);
     jPanelJobStatusTableButton.add(Box.createHorizontalStrut(GraphicUtils.STRUT_GAP));
     jPanelJobStatusTableButton.add(jButtonGetOutput, null);
 
     jPanelJobStatusTable.add(jPanelJobStatusTableLabel, BorderLayout.NORTH);
     jPanelJobStatusTable.add(jScrollPaneJobTable, BorderLayout.CENTER);
     jPanelJobStatusTable.add(jPanelJobStatusTableButton, BorderLayout.SOUTH);
     jPanelNorth.setLayout(new BorderLayout());
 
     jPanelNorth.add(jPanelTime, BorderLayout.NORTH);
     jPanelNorth.add(jPanelJobId, BorderLayout.SOUTH);
 
     jPanelMain.setLayout(new BorderLayout());
     jPanelMain.setBorder(GraphicUtils.SPACING_BORDER);
     jPanelMain.add(jPanelNorth, BorderLayout.NORTH);
     jPanelMain.add(jPanelJobStatusTable, BorderLayout.CENTER);
     jPanelMain.add(jPanelButton, BorderLayout.SOUTH);
 
     this.add(jPanelMain, BorderLayout.CENTER);
     this.setVisible(true);
     setPreferredSize(new Dimension(660, 540));
 
     jTableJobs.addMouseListener(new MouseAdapter() {
       public void mouseClicked(MouseEvent e) {
         if (e.getClickCount() == 2) {
           Point p = e.getPoint();
           int row = jTableJobs.rowAtPoint(p);
           int column = jTableJobs.columnAtPoint(p);
           String selectedJobId = (String) jTableJobs.getValueAt(row,
               JOB_ID_COLUMN_INDEX);
           JobStatus jobStatus = getStoredJobStatus(selectedJobId);
           if (jobStatus != null) {
             if (!isSingleJobDialogShown) {
               singleJobDialog = new StatusDetailsFrame(MultipleJobPanel.this,
                   jobStatus);
               GraphicUtils.screenCenterWindow(singleJobDialog);
               singleJobDialog.show();
               isSingleJobDialogShown = true;
             } else {
               // Just change current data with new job data.
               singleJobDialog.setJobStatus(jobStatus);
               singleJobDialog.setVisible(false);
               GraphicUtils.deiconifyFrame(singleJobDialog);
               singleJobDialog.setVisible(true);
             }
             singleJobDialog.setJLabelLastUpdate(jLabelLastUpdate.getText());
           }
         }
       }
     });
 
     jTableJobs.addMouseListener(new MouseAdapter() {
       public void mousePressed(MouseEvent e) {
         showJPopupMenuTable(e);
       }
 
       public void mouseReleased(MouseEvent e) {
         showJPopupMenuTable(e);
       }
     });
 
     jScrollPaneJobTable.addMouseListener(new MouseAdapter() {
       public void mousePressed(MouseEvent e) {
         if (jTableJobs.getRowCount() != 0) {
           jMenuSelectNone();
         }
         showJPopupMenuTable(e);
       }
 
       public void mouseReleased(MouseEvent e) {
         if (jTableJobs.getRowCount() != 0) {
           jMenuSelectNone();
         }
         showJPopupMenuTable(e);
       }
     });
 
     JTableHeader tableHeader = jTableJobs.getTableHeader();
     tableHeader.setUpdateTableInRealTime(true);
     tableHeader.addMouseListener(new MouseAdapter() {
       public void mouseClicked(MouseEvent me) {
         TableColumnModel columnModel = jTableJobs.getColumnModel();
         int columnIndex = columnModel.getColumnIndexAtX(me.getX());
         int modelIndex = columnModel.getColumn(columnIndex).getModelIndex();
         if (modelIndex < 0) {
           return;
         }
         ascending = (sortingColumn == columnIndex) ? !ascending : true;
         sortingColumn = columnIndex;
 
         sortBy(jTableJobs, modelIndex, ascending);
       }
     });
 
   }
 
   void setJobCollection(JobCollection jobCollection) {
     this.jobCollection = jobCollection;
   }
 
   void jButtonBackEvent(ActionEvent e) {
     if (!isSingleJobDialogShown && !isLogInfoJDialogShown) {
       updateThread.stopThread();
     }
     currentTimeThread.stopThread();
     jobMonitorJFrame.setMenuBar(JOB_MONITOR_MAIN);
     jobMonitorJFrame.displayJPanelMain();
   }
 
   void jButtonDagNodesEvent(ActionEvent ae) {
     final SwingWorker worker = new SwingWorker() {
       public Object construct() {
 
         // Standard code
         int[] selectedRows = jTableJobs.getSelectedRows();
         if (selectedRows.length == 0) {
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               "Please select a Dag",
               Utils.INFORMATION_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.INFORMATION_MESSAGE,
               null, null, null);
         } else if (selectedRows.length == 1) {
           String selectedJobId = jTableJobs.getValueAt(selectedRows[0],
               JOB_ID_COLUMN_INDEX).toString().trim();
           String jobType = jTableJobs.getValueAt(selectedRows[0],
               JOB_TYPE_COLUMN_INDEX).toString().trim();
 
           if (jobType.equals(Jdl.TYPE_DAG)) {
             if (!MultipleJobPanel.this.jobMonitorJFrame.dagMonitorMap.
                 containsKey(selectedJobId)) {
               String state = jTableJobs.getValueAt(selectedRows[0],
                   JOB_STATUS_COLUMN_INDEX).toString().trim();
               String submissionTime = jTableJobs.getValueAt(selectedRows[0],
                   SUBMISSION_TIME_COLUMN_INDEX).toString().trim();
               try {
                 MultipleJobFrame multipleJobFrame =
                     new MultipleJobFrame(MultipleJobPanel.this.jobMonitorJFrame,
                     selectedJobId, state, submissionTime);
                 GraphicUtils.screenCenterWindow(multipleJobFrame);
                 if (!MultipleJobPanel.this.jobMonitorJFrame.dagMonitorMap.
                     containsKey(selectedJobId)) {
                   MultipleJobPanel.this.jobMonitorJFrame.dagMonitorMap.put(
                       selectedJobId, multipleJobFrame);
                   multipleJobFrame.setVisible(true);
                 } else {
                   multipleJobFrame.dispose();
                 }
               } catch (Exception e) {
                 JOptionPane.showOptionDialog(MultipleJobPanel.this,
                     e.getMessage(),
                     Utils.ERROR_MSG_TXT,
                     JOptionPane.DEFAULT_OPTION,
                     JOptionPane.ERROR_MESSAGE,
                     null, null, null);
               }
             } else {
               MultipleJobFrame multipleJobFrame =
                   (MultipleJobFrame) MultipleJobPanel.this.jobMonitorJFrame.
                   dagMonitorMap.get(selectedJobId);
               multipleJobFrame.setVisible(false);
               GraphicUtils.deiconifyFrame(multipleJobFrame);
               multipleJobFrame.setVisible(true);
             }
           } else {
             JOptionPane.showOptionDialog(MultipleJobPanel.this,
                 "Please select a Dag",
                 Utils.INFORMATION_MSG_TXT,
                 JOptionPane.DEFAULT_OPTION,
                 JOptionPane.INFORMATION_MESSAGE,
                 null, null, null);
           }
         } else {
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               "Please select a single Dag",
               Utils.INFORMATION_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.INFORMATION_MESSAGE,
               null, null, null);
         }
         // END Standard code
 
         return "";
       }
     };
     worker.start();
   }
 
   void jButtonDagMonEvent(ActionEvent ae) {
     final SwingWorker worker = new SwingWorker() {
       public Object construct() {
 
         // Standard code
         int[] selectedRows = jTableJobs.getSelectedRows();
         if (selectedRows.length == 0) {
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               "Please select a Dag",
               Utils.INFORMATION_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.INFORMATION_MESSAGE,
               null, null, null);
         } else if (selectedRows.length == 1) {
           String selectedJobId = jTableJobs.getValueAt(selectedRows[0],
               JOB_ID_COLUMN_INDEX).toString().trim();
           String jobType = jTableJobs.getValueAt(selectedRows[0],
               JOB_TYPE_COLUMN_INDEX).toString().trim();
 
           if (jobType.equals(Jdl.TYPE_DAG)) {
             if (!MultipleJobPanel.this.jobMonitorJFrame.dagMonThreadMap.
                 containsKey(selectedJobId)) {
               String state = jTableJobs.getValueAt(selectedRows[0],
                   JOB_STATUS_COLUMN_INDEX).toString().trim();
 
               String dagMonLocation = Api.getEnv(GUIFileSystem.EDG_XDAGMON_LOCATION);
               String topologyLocation = Api.getEnv(GUIFileSystem.EDG_TOPOLOGY_LOCATION);
               String repLocation = Api.getEnv(GUIFileSystem.EDG_REPOSITORY_LOCATION);
               String wlLocation = Api.getEnv(GUIFileSystem.EDG_WL_LOCATION);
               if (dagMonLocation == null) {
                 if (repLocation != null) {
                   dagMonLocation = repLocation + "/edg/workload/logging/test";
                 } else {
                   dagMonLocation = wlLocation + "/bin";
                 }
               }
               if (topologyLocation == null) {
                 if (repLocation != null) {
                   topologyLocation = repLocation + "/edg/workload/common/test";
                 } else {
                   topologyLocation = wlLocation + "/bin";
                 }
               }
 
               Toolkit toolkit = getToolkit();
               Dimension screenSize = toolkit.getScreenSize();
               String command = dagMonLocation + "/xdagmon -tparse "
                   + topologyLocation + "/edg-wl-dag-topology-test"
                   //+ " -geometry " + ((int) (screenSize.width * Utils.SCREEN_WIDTH_PROPORTION))
                   + " -geometry " + ((int) (screenSize.width * 0.75))
                   //+ "x" + ((int) (screenSize.height * Utils.SCREEN_HEIGHT_PROPORTION))
                   + "x" + ((int) (screenSize.height * 0.7))
                   + " " + selectedJobId;
               logger.debug("Command: " + command);
               int code = Api.shadow(command);
               if ((code != 256) && (code != 13)) {
                 JOptionPane.showOptionDialog(MultipleJobPanel.this,
                     "Unable to show Dag Monitor",
                     Utils.ERROR_MSG_TXT,
                     JOptionPane.DEFAULT_OPTION,
                     JOptionPane.ERROR_MESSAGE,
                     null, null, null);
               }
               logger.debug("EXIT CODE: " + code);
             } else {
               JOptionPane.showOptionDialog(MultipleJobPanel.this,
                   "A Dag Monitor is already shown for the Dag:/n"
                   + selectedJobId,
                   Utils.INFORMATION_MSG_TXT,
                   JOptionPane.DEFAULT_OPTION,
                   JOptionPane.INFORMATION_MESSAGE,
                   null, null, null);
             }
           } else {
             JOptionPane.showOptionDialog(MultipleJobPanel.this,
                 "Please select a Dag",
                 Utils.INFORMATION_MSG_TXT,
                 JOptionPane.DEFAULT_OPTION,
                 JOptionPane.INFORMATION_MESSAGE,
                 null, null, null);
           }
         } else {
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               "Please select a single Dag",
               Utils.INFORMATION_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.INFORMATION_MESSAGE,
               null, null, null);
         }
         // END Standard code
 
         return "";
       }
     };
     worker.start();
   }
 
   void jButtonDetailsEvent(ActionEvent e) {
     int[] selectedRows = jTableJobs.getSelectedRows();
     if (selectedRows.length == 0) {
       JOptionPane.showOptionDialog(MultipleJobPanel.this,
           "Please select a job",
           Utils.INFORMATION_MSG_TXT,
           JOptionPane.DEFAULT_OPTION,
           JOptionPane.INFORMATION_MESSAGE,
           null, null, null);
     } else if (selectedRows.length == 1) {
       String selectedJobId = jTableJobs.getValueAt(selectedRows[0],
           JOB_ID_COLUMN_INDEX).toString().trim();
       String jobType = jTableJobs.getValueAt(selectedRows[0],
           JOB_TYPE_COLUMN_INDEX).toString().trim();
 
       JobStatus jobStatus = getStoredJobStatus(selectedJobId);
       if (jobStatus != null) {
         if (!isSingleJobDialogShown) {
           singleJobDialog = new StatusDetailsFrame(this, jobStatus);
           logger.debug("jobMonitorJFrame: " + jobMonitorJFrame);
           logger.debug("singleJobDialog: " + singleJobDialog);
           GraphicUtils.windowCenterWindow(jobMonitorJFrame, singleJobDialog);
           singleJobDialog.show();
           isSingleJobDialogShown = true;
         } else {
           // Just change current data with new job data.
           singleJobDialog.setJobStatus(jobStatus);
           singleJobDialog.setVisible(false);
           GraphicUtils.deiconifyFrame(singleJobDialog);
           singleJobDialog.setVisible(true);
         }
         singleJobDialog.setJLabelLastUpdate(jLabelLastUpdate.getText());
       }
     } else {
       JOptionPane.showOptionDialog(MultipleJobPanel.this,
           "Please select a single job",
           Utils.INFORMATION_MSG_TXT,
           JOptionPane.DEFAULT_OPTION,
           JOptionPane.INFORMATION_MESSAGE,
           null, null, null);
     }
   }
 
   void setIsSingleJobDialogShown(boolean bool) {
     isSingleJobDialogShown = bool;
   }
 
   void setIsLogInfoJDialogShown(boolean bool) {
     isLogInfoJDialogShown = bool;
   }
 
   // Add an inserted job id in the table getting the job status
   void jButtonAddJobIdEvent(ActionEvent ae) {
     String insertedJobId = jTextFieldUserJobId.getText().trim();
     if (!insertedJobId.equals("")) {
       if (!jobTableModel.isElementPresentInColumn(insertedJobId,
           JOB_ID_COLUMN_INDEX)) {
         if (jobTableModel.getRowCount() < maxMonitoredJobNumber) {
           JobCollection jobCollectionToAdd = new JobCollection();
           try {
             jobCollectionToAdd.insertId(new JobId(insertedJobId));
             jTextFieldUserJobId.setText("");
             //GUIGlobalVars.currentMonitoredJobCount++;
           } catch (Exception e) {
             JOptionPane.showOptionDialog(MultipleJobPanel.this,
                 e.getMessage(),
                 Utils.ERROR_MSG_TXT,
                 JOptionPane.DEFAULT_OPTION,
                 JOptionPane.ERROR_MESSAGE,
                 null, null, null);
             return;
           }
           addJobStatusTableJobs(jobCollectionToAdd);
         } else {
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               "Max simultaneously monitored job number reached\n"
               + "First remove a job before adding",
               "Job Monitor - Add Job Id",
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.WARNING_MESSAGE,
               null, null, null);
         }
         jTextFieldUserJobId.selectAll();
         jTextFieldUserJobId.grabFocus();
       } else {
         JOptionPane.showOptionDialog(MultipleJobPanel.this,
             "Inserted Job Id is already present",
             "Job Monitor - Add Job Id",
             JOptionPane.DEFAULT_OPTION,
             JOptionPane.WARNING_MESSAGE,
             null, null, null);
       }
     }
   }
 
   void jButtonClearJobIdEvent(ActionEvent e) {
     jTextFieldUserJobId.setText("");
     jTextFieldUserJobId.grabFocus();
   }
 
   void jButtonUpdateEvent(ActionEvent e) {
     final SwingWorker worker = new SwingWorker() {
       public Object construct() {
 
         // Standard code
         logger.debug("Call to jButtonUpdateEvent()");
         if ((MultipleJobPanel.this.jobCollection != null)
             && (MultipleJobPanel.this.jobCollection.size() != 0)) {
           updateJobStatusTableJobs();
           String timeText = jLabelLastUpdate.getText();
           restartUpdateThread();
           if ((singleJobDialog != null) && singleJobDialog.isVisible()) {
             String jobIdShown = singleJobDialog.getJobIdShown();
             JobStatus jobStatus = getJobStatus(jobIdShown);
             singleJobDialog.setJLabelLastUpdate(timeText);
             singleJobDialog.setJobStatus(jobStatus);
           }
           if ((logInfoJDialog != null) && logInfoJDialog.isVisible()) {
             String jobIdShown = logInfoJDialog.getJobIdShown();
             Vector eventVector = getEventVector(jobIdShown);
             if (eventVector.size() != 0) {
               logInfoJDialog.logInfoJPanel.setTableEvents(eventVector);
               logInfoJDialog.logInfoJPanel.setTableEventAttributes(0);
             }
             logInfoJDialog.setJLabelLastUpdate(timeText);
           }
         }
         // END Standard code
 
         return "";
       }
     };
     worker.start();
   }
 
   void threadUpdateEvent() {
     logger.debug("Call to threadUpdateEvent()");
     if ((MultipleJobPanel.this.jobCollection != null)
         && (MultipleJobPanel.this.jobCollection.size() != 0)) {
       updateJobStatusTableJobs();
       String timeText = jLabelLastUpdate.getText();
       if ((singleJobDialog != null) && singleJobDialog.isVisible()) {
         String jobIdShown = singleJobDialog.getJobIdShown();
         JobStatus jobStatus = getJobStatus(jobIdShown);
         singleJobDialog.setJLabelLastUpdate(timeText);
         singleJobDialog.setJobStatus(jobStatus);
       }
       if ((logInfoJDialog != null) && logInfoJDialog.isVisible()) {
         String jobIdShown = logInfoJDialog.getJobIdShown();
         Vector eventVector = getEventVector(jobIdShown);
         if (eventVector.size() != 0) {
           logInfoJDialog.logInfoJPanel.setTableEvents(eventVector);
         }
         logInfoJDialog.setJLabelLastUpdate(timeText);
       }
     }
   }
 
   /**
    * Returns the job status of the job identified by the jobId provided.
    * The job status will be obtained by the getStatus() API method.
    */
   JobStatus getJobStatus(String jobIdText) {
     try {
       JobId jobId = new JobId(jobIdText);
       Job job = new Job(jobId);
       Result jobResult = null;
       UserCredential userCredential = new UserCredential(new File(GUIGlobalVars.
           proxyFilePath));
       if (userCredential.getX500UserSubject().equals(GUIGlobalVars.proxySubject)) {
         jobResult = job.getStatus();
       } else {
         JOptionPane.showOptionDialog(MultipleJobPanel.this,
             Utils.FATAL_ERROR + "Proxy file user subject has changed"
             + "\nApplication will be terminated",
             Utils.ERROR_MSG_TXT,
             JOptionPane.DEFAULT_OPTION,
             JOptionPane.ERROR_MESSAGE,
             null, null, null);
         System.exit( -1);
       }
       int resultCode = jobResult.getCode();
       if ((resultCode != Result.STATUS_FAILURE) &&
           (resultCode != Result.STATUS_FORBIDDEN)) {
         JobStatus jobStatus = (JobStatus) jobResult.getResult();
         if (jobStatus != null) {
           if (this.jobStatusHashMap.containsKey(jobIdText)) {
             this.jobStatusHashMap.remove(jobIdText);
           }
           this.jobStatusHashMap.put(jobIdText, jobStatus);
         }
         return jobStatus;
       } else {
         JOptionPane.showOptionDialog(MultipleJobPanel.this,
             jobResult.getResult(),
             Utils.ERROR_MSG_TXT,
             JOptionPane.DEFAULT_OPTION,
             JOptionPane.ERROR_MESSAGE,
             null, null, null);
       }
     } catch (Exception e) {
       if (isDebugging) {
         e.printStackTrace();
       }
     }
     return null;
   }
 
   /**
    * Returns the job status of the job identified by the jobId provided.
    * The job status is the old one stored in the hash map structure.
    */
   JobStatus getStoredJobStatus(String jobIdText) {
     try {
       JobStatus jobStatus = (JobStatus)this.jobStatusHashMap.get(jobIdText);
       return jobStatus;
     } catch (Exception e) {
       if (isDebugging) {
         e.printStackTrace();
       }
     }
     return null;
   }
 
   void setJobStatusTableJobs() {
     String jobId;
     Job job;
     Iterator jobIterator = MultipleJobPanel.this.jobCollection.jobs();
     while (jobIterator.hasNext()) {
       job = (Job) jobIterator.next();
       try {
         jobId = job.getJobId().toString();
       } catch (Exception e) {
         if (isDebugging) {
           e.printStackTrace();
         }
         continue;
       }
       if (!jobTableModel.isElementPresentInColumn(jobId, JOB_ID_COLUMN_INDEX)) {
         jobTableModel.addRow(makeTempRowToAddVector(job));
         jLabelTotalDisplayedJobs.setText(Integer.toString(jobTableModel.
             getRowCount()));
       }
     }
     jLabelTotalDisplayedJobs.setText(Integer.toString(jobTableModel.getRowCount()));
     updateJobStatusTableJobs();
   }
 
 //---------------------------------------------------------------------
   static int count = 0;
   void updateJobStatusTableJobs() {
     /*JOptionPane.showOptionDialog(MultipleJobPanel.this,
                                    "Calling: " + (++count) + "\n",
                                    Utils.ERROR_MSG_TXT,
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.ERROR_MESSAGE,
                                    null, null, null);*/
 
     logger.debug("setJobStatusTableJobs() - jobCollection.size(): "
         + MultipleJobPanel.this.jobCollection.size());
     try {
       UserCredential userCredential = new UserCredential(new File(GUIGlobalVars.
           proxyFilePath));
       if (userCredential.getX500UserSubject().equals(GUIGlobalVars.proxySubject)) {
         MultipleJobPanel.this.jobVector = new Vector();
         /*
                 JobCollection coll = new JobCollection();
                 Iterator iterator = MultipleJobPanel.this.jobCollection.jobs();
                 while (iterator.hasNext()) {
                   coll.insert((Job) iterator.next());
                 }
                 MultipleJobPanel.this.jobCollection = coll;
          */
         logger.debug("----- CALLING JOBSTATUS");
         logger.debug(MultipleJobPanel.this.jobCollection);
         MultipleJobPanel.this.jobVector = MultipleJobPanel.this.jobCollection.
             getStatus();
       } else {
         JOptionPane.showOptionDialog(MultipleJobPanel.this,
             Utils.FATAL_ERROR + "Proxy file user subject has changed"
             + "\nApplication will be terminated",
             Utils.ERROR_MSG_TXT,
             JOptionPane.DEFAULT_OPTION,
             JOptionPane.ERROR_MESSAGE,
             null, null, null);
         System.exit( -1);
       }
     } catch (InterruptedException ie) {
       // Thread has been interrupted, maybe during update event from user command
       // button "Back", do nothing.
       if (isDebugging) {
         ie.printStackTrace();
         /* JOptionPane.showOptionDialog(MultipleJobPanel.this,
                                     ie.getMessage(),
                                     Utils.ERROR_MSG_TXT,
                                     JOptionPane.DEFAULT_OPTION,
                                     JOptionPane.ERROR_MESSAGE,
                                     null, null, null);
          */
       }
       return;
     } catch (Exception e) {
       if (isDebugging) {
         e.printStackTrace();
       }
       JOptionPane.showOptionDialog(MultipleJobPanel.this,
           //"HERE: " + (++count) + "\n" +
           e.getMessage(),
           Utils.ERROR_MSG_TXT,
           JOptionPane.DEFAULT_OPTION,
           JOptionPane.ERROR_MESSAGE,
           null, null, null);
       return;
     }
 
     logger.debug("----- MultipleJobPanel.this.jobVector: " +
         MultipleJobPanel.this.jobVector);
 
     String warningMsg = "";
     try {
       String jobIdText = "";
       String JobIdTextStatus;
       Result jobResult = null;
       JobStatus jobStatus;
       int resultCode = 0;
       int index = -1;
       for (int i = 0; i < MultipleJobPanel.this.jobVector.size(); i++) {
         logger.debug("----- INSIDE CYCLE");
         jobResult = (Result) MultipleJobPanel.this.jobVector.get(i);
 
         logger.debug("--------------- jobResult");
         logger.debug(new String(new Integer(jobResult.hashCode()).toString()));
         logger.debug("-------------------------");
 
         if (jobResult != null) {
           //logger.debug("setJobStatusTableJobs() - jobResult: " + jobResult);
           resultCode = jobResult.getCode();
           //logger.debug("setJobStatusTableJobs() - resultCode: " + resultCode);
           jobIdText = jobResult.getId().trim();
 
           // Checks if jobCollection contains jobs after update event. User could remove some jobs
           // from table during update event. In this case jobStatus must not be shown, the job is
           // removed from table.
           if (!MultipleJobPanel.this.jobCollection.contains(new Job(new JobId(
               jobIdText)))) {
             continue;
           }
 
           index = jobTableModel.getIndexOfElementInColumn(jobIdText,
               JOB_ID_COLUMN_INDEX);
           if ((resultCode != Result.STATUS_FAILURE)
               && (resultCode != Result.STATUS_FORBIDDEN)) {
             jobStatus = (JobStatus) jobResult.getResult();
 
             // Stores job status information in hash map structure.
             // This hash map is used when user asks for details, in this case you
             // don't recall getStatus() API (it will be done when you ask for an update).
             if (MultipleJobPanel.this.jobStatusHashMap.containsKey(jobIdText)) {
               MultipleJobPanel.this.jobStatusHashMap.remove(jobIdText);
             }
             MultipleJobPanel.this.jobStatusHashMap.put(jobIdText, jobStatus);
 
             if (index != -1) {
               Vector rowToAddVector = makeRowToAddVector(jobStatus);
               if (rowToAddVector.size() != 0) {
                 jobTableModel.setValueAt(rowToAddVector.get(1), index,
                     JOB_TYPE_COLUMN_INDEX);
                 jobTableModel.setValueAt(rowToAddVector.get(2), index,
                     JOB_STATUS_COLUMN_INDEX);
                 jobTableModel.setValueAt(rowToAddVector.get(3), index,
                     SUBMISSION_TIME_COLUMN_INDEX);
                 jobTableModel.setValueAt(rowToAddVector.get(4), index,
                     DESTINATION_COLUMN_INDEX);
               }
             } else {
               logger.debug(
                   "setJobStatusTableJobs() - Job Id not present in column: " +
                   jobIdText);
             }
           } else {
             if (MultipleJobPanel.this.jobStatusHashMap.containsKey(jobIdText)) {
               MultipleJobPanel.this.jobStatusHashMap.remove(jobIdText);
             }
 
             if (index != -1) {
               if (jobTableModel.getValueAt(index,
                   JOB_STATUS_COLUMN_INDEX).toString().equals(Utils.
                   COLLECTING_STATE)) {
                 jobTableModel.setValueAt(Utils.UNABLE_TO_GET_STATUS, index,
                     JOB_STATUS_COLUMN_INDEX);
               }
             }
             warningMsg += ((Exception) jobResult.getResult()).getMessage() +
                 "\n";
             logger.debug("----- warningMsg: " + warningMsg);
           }
           jLabelTotalDisplayedJobs.setText(Integer.toString(jobTableModel.
               getRowCount()));
         } else {
           logger.debug("setJobStatusTableJobs() - Job result is null");
         }
       }
 
       sortBy(jTableJobs, sortingColumn, true);
 
       Date date = new Date();
       String timeText = date.toString(); // look timeText below
       jLabelLastUpdate.setText(timeText);
     } catch (Exception e) {
       if (isDebugging) {
         e.printStackTrace();
       }
     }
 
     warningMsg = warningMsg.trim();
     if (!warningMsg.equals("")) {
       GraphicUtils.showOptionDialogMsg(MultipleJobPanel.this, warningMsg,
           Utils.ERROR_MSG_TXT,
           JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
           Utils.MESSAGE_LINES_PER_JOPTIONPANE,
           null, null);
     }
   }
 
   void addJobStatusTableJobs(Vector jobVector) {
     if ((jobVector == null) || (jobVector.size() == 0)) {
       return;
     }
     JobCollection jobCollection = new JobCollection();
     for (int i = 0; i < jobVector.size(); i++) {
       try {
         jobCollection.insertId(new JobId(jobVector.get(i).toString().trim()));
       } catch (Exception e) {
         if (isDebugging) {
           e.printStackTrace();
           // Do nothing.
         }
       }
     }
     if (jobCollection.size() != 0) {
       addJobStatusTableJobs(jobCollection);
     }
   }
 
   void addJobStatusTableJobs(final JobCollection addJobCollection) {
     final SwingWorker worker = new SwingWorker() {
       public Object construct() {
 
         // Standard code
         logger.debug("addJobStatusTableJobs() addJobCollection.size(): " +
             addJobCollection.size());
         String warningMsg = "";
         JobCollection tempJobCollection = new JobCollection();
         Job job;
         Iterator addJobCollectionIterator = addJobCollection.jobs();
         while (addJobCollectionIterator.hasNext()) {
           job = (Job) addJobCollectionIterator.next();
           if (!MultipleJobPanel.this.jobCollection.contains(job)) {
             try {
               tempJobCollection.insert(job);
               MultipleJobPanel.this.jobCollection.insert(job);
               jobTableModel.addRow(makeTempRowToAddVector(job));
               jLabelTotalDisplayedJobs.setText(Integer.toString(jobTableModel.
                   getRowCount()));
             } catch (Exception e) {
               if (isDebugging) {
                 e.printStackTrace();
               }
             }
           }
         }
 
         if (tempJobCollection.size() == 0) {
           return "";
         }
 
         Vector addJobVector = new Vector();
         try {
           //!!! TO REMOVE test only
           //tempJobCollection.setMaxThreadNumber(1);
 
           addJobVector = tempJobCollection.getStatus();
         } catch (Exception e) {
           if (isDebugging) {
             e.printStackTrace();
           }
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               e.getMessage(),
               Utils.ERROR_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.ERROR_MESSAGE,
               null, null, null);
           return "";
         }
         logger.debug("addJobCollection.getStatus().size(): " +
             addJobVector.size());
         logger.debug("jobCollection.getStatus(): " +
             MultipleJobPanel.this.jobVector);
         logger.debug("STATUS_FAILURE: " + Result.STATUS_FAILURE);
         logger.debug("STATUS_FORBIDDEN: " + Result.STATUS_FORBIDDEN);
         try {
           String jobIdText = "";
           Result jobResult;
           JobStatus jobStatus;
           int resultCode = 0;
           int index = -1;
           for (int i = 0; i < addJobVector.size(); i++) {
             jobResult = (Result) addJobVector.get(i);
             logger.debug("jobResult: " + jobResult);
             resultCode = jobResult.getCode();
             logger.debug("resultCode: " + resultCode);
             if ((resultCode != Result.STATUS_FAILURE) &&
                 (resultCode != Result.STATUS_FORBIDDEN)) {
               jobStatus = (JobStatus) jobResult.getResult();
               jobIdText = jobStatus.getValString(JobStatus.JOB_ID);
               jobIdText = jobIdText.trim();
               logger.debug("addJTableJobs() Job Id: " + jobIdText);
 
               // Store job status information in hash map structure.
               // This has map is used when user ask for details, in this case you
               // don't recall getStatus() API (it will be done when you ask for an update).
               if (MultipleJobPanel.this.jobStatusHashMap.containsKey(jobIdText)) {
                 MultipleJobPanel.this.jobStatusHashMap.remove(jobIdText);
               }
               MultipleJobPanel.this.jobStatusHashMap.put(jobIdText, jobStatus);
 
               // Check it to avoid double inserting in case of some Exception, during
               // update whitout user interaction; an exception occurs and a dialog is shown,
               // but user don't close dialog and another update event occurs.
               index = jobTableModel.getIndexOfElementInColumn(jobIdText,
                   JOB_ID_COLUMN_INDEX);
               if (index != -1) {
                 Vector rowToAddVector = makeRowToAddVector(jobStatus);
                 if (rowToAddVector.size() != 0) {
                   jobTableModel.setValueAt(rowToAddVector.get(1), index,
                       JOB_TYPE_COLUMN_INDEX);
                   jobTableModel.setValueAt(rowToAddVector.get(2), index,
                       JOB_STATUS_COLUMN_INDEX);
                   jobTableModel.setValueAt(rowToAddVector.get(3), index,
                       SUBMISSION_TIME_COLUMN_INDEX);
                   jobTableModel.setValueAt(rowToAddVector.get(4), index,
                       DESTINATION_COLUMN_INDEX);
                 }
               } else {
                 logger.debug("Job Id not present in table: " + jobIdText);
               }
 
             } else {
               jobIdText = jobResult.getId().trim();
               index = jobTableModel.getIndexOfElementInColumn(jobIdText,
                   JOB_ID_COLUMN_INDEX);
               if (MultipleJobPanel.this.jobStatusHashMap.containsKey(jobIdText)) {
                 MultipleJobPanel.this.jobStatusHashMap.remove(jobIdText);
               }
               if (index != -1) {
                 if (jobTableModel.getValueAt(index,
                     JOB_STATUS_COLUMN_INDEX).toString()
                     .equals(Utils.COLLECTING_STATE)) {
                   jobTableModel.setValueAt(Utils.UNABLE_TO_GET_STATUS, index,
                       JOB_STATUS_COLUMN_INDEX);
                 }
               }
               warningMsg += ((Exception) jobResult.getResult()).getMessage() +
                   "\n";
             }
             jLabelTotalDisplayedJobs.setText(Integer.toString(jobTableModel.
                 getRowCount()));
           }
           //jTableJobs.repaint();
           //if (GUIGlobalVars.getMultipleJobPanelSortColumn() != Utils.NO_SORTING) {
           if (sortingColumn != Utils.NO_SORTING) {
             sortBy(jTableJobs, sortingColumn, true);
           }
         } catch (Exception e) {
           if (isDebugging) {
             e.printStackTrace();
           }
         }
         warningMsg = warningMsg.trim();
         if (!warningMsg.equals("")) {
           GraphicUtils.showOptionDialogMsg(MultipleJobPanel.this, warningMsg,
               Utils.ERROR_MSG_TXT,
               JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
               Utils.MESSAGE_LINES_PER_JOPTIONPANE, null, null);
         }
         jLabelTotalDisplayedJobs.setText(Integer.toString(jobTableModel.
             getRowCount()));
         // END Standard code
 
         return "";
       }
     };
     worker.start();
   }
 
   Vector makeTempRowToAddVector(Job job) {
     Vector tempRowToAddVector = new Vector();
     try {
       tempRowToAddVector.add(job.getJobId().toString());
       tempRowToAddVector.add("");
       tempRowToAddVector.add(Utils.COLLECTING_STATE);
       tempRowToAddVector.add("");
       tempRowToAddVector.add("");
     } catch (Exception e) {
       if (isDebugging) {
         e.printStackTrace();
       }
     }
     return tempRowToAddVector;
   }
 
   Vector makeRowToAddVector(JobStatus jobStatus) {
     Vector rowToAddVector = new Vector();
     String jobType = "";
     String jdlText = jobStatus.getValString(JobStatus.JDL).trim();
     int type = jobStatus.getValInt(JobStatus.JOBTYPE);
     if ((jdlText != null) && !jdlText.equals("")) {
       if (type == JobStatus.JOBTYPE_JOB) { // Not a Dag.
         logger.debug("TYPE: " + type);
         try {
           JobAd jobAd = new JobAd();
           jobAd.fromString(jdlText);
           Vector valuesVector = jobAd.getStringValue(Jdl.JOBTYPE);
           if (valuesVector.size() == 1) {
             jobType = valuesVector.get(0).toString();
           } else if (valuesVector.contains(Jdl.JOBTYPE_INTERACTIVE)
               && valuesVector.contains(Jdl.JOBTYPE_CHECKPOINTABLE)) {
             jobType = Jdl.JOBTYPE_CHECKPOINTABLE + Utils.JOBTYPE_LIST_SEPARATOR +
                 Jdl.JOBTYPE_INTERACTIVE;
           } else if (valuesVector.contains(Jdl.JOBTYPE_INTERACTIVE)
               && valuesVector.contains(Jdl.JOBTYPE_MPICH)) {
             jobType = Jdl.JOBTYPE_CHECKPOINTABLE + Utils.JOBTYPE_LIST_SEPARATOR +
                 Jdl.JOBTYPE_MPICH;
           }
         } catch (JobAdException jae) {
           // Unespected exception.
           // Ignore it, jobType equals blank string.
           if (isDebugging) {
             jae.printStackTrace();
           }
         } catch (IllegalArgumentException iae) {
           // Unespected exception.
           // Ignore it, jobType equals blank string.
           if (isDebugging) {
             iae.printStackTrace();
           }
         } catch (NoSuchFieldException nsfe) {
           // Unespected exception.
           // Ignore it, jobType equals blank string.
           if (isDebugging) {
             nsfe.printStackTrace();
           }
         } catch (Exception e) {
           if (isDebugging) {
             e.printStackTrace();
           }
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               e.getMessage(),
               Utils.ERROR_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.ERROR_MESSAGE,
               null, null, null);
         }
       }
     }
     rowToAddVector.addElement(jobStatus.getValString(JobStatus.JOB_ID));
     if ((type == JobStatus.JOBTYPE_JOB) && (jobType.equals(""))) {
       jobType = Jdl.JOBTYPE_NORMAL;
     } else if (type == JobStatus.JOBTYPE_DAG) {
       jobType = Jdl.TYPE_DAG;
     }
     rowToAddVector.addElement(jobType);
     String jobStatusName = jobStatus.name().trim();
     //if(jobStatusName.equals(STATE_DONE)) {
     if (jobStatusName.indexOf(JobStatus.code[JobStatus.DONE]) != -1) {
       if (jobStatus.getValInt(JobStatus.DONE_CODE) == 1) {
         rowToAddVector.addElement(jobStatusName + Utils.STATE_FAILED);
       } else if (jobStatus.getValInt(JobStatus.DONE_CODE) == 2) {
         rowToAddVector.addElement(jobStatusName + Utils.STATE_CANCELLED);
       } else { // DONE_CODE = 0.
         int exitCode = jobStatus.getValInt(JobStatus.EXIT_CODE);
         if (exitCode != 0) {
           rowToAddVector.addElement(jobStatusName +
               Utils.STATE_EXIT_CODE_NOT_ZERO);
         } else {
           rowToAddVector.addElement(jobStatusName);
         }
       }
     } else {
       if ((jobStatus.getValInt(JobStatus.CANCELLING) == 1)
           && (jobStatus.code() != JobStatus.CANCELLED)) {
         rowToAddVector.addElement(jobStatusName + Utils.STATE_CANCELLING);
       } else {
         rowToAddVector.addElement(jobStatusName);
       }
     }
     Vector stateEnterTimesVector = (Vector) jobStatus.get(JobStatus.
         STATE_ENTER_TIMES);
     logger.debug("State Enter Times (Vector): " + stateEnterTimesVector);
     if (stateEnterTimesVector != null) {
       logger.debug("State Enter Time (Vector.get(1)): " +
           stateEnterTimesVector.get(1).toString().trim());
       //rowToAddVector.addElement(Utils.toTime(stateEnterTimesVector.get(1).toString().trim()));
       rowToAddVector.addElement(Utils.toDate(stateEnterTimesVector.get(1).
           toString().trim()));
     } else {
       //rowToAddVector.addElement("");
       rowToAddVector.addElement(null);
     }
     String destination = jobStatus.getValString(JobStatus.DESTINATION);
     if (destination != null) {
       rowToAddVector.addElement(destination);
     } else {
       rowToAddVector.addElement("");
     }
     //rowToAddVector.addElement(jobStatus.getValString(JobStatus.NETWORK_SERVER));
     return rowToAddVector;
   }
 
   void jTextFieldUserJobIdFocusLost(FocusEvent e) {
     jTextFieldUserJobId.select(0, 0);
   }
 
   /**
    * Cancels selected job(s) from table.
    */
   void jButtonCancelEvent(ActionEvent e) {
     final SwingWorker worker = new SwingWorker() {
       public Object construct() {
 
         // Standard code
         int[] selectedRows = jTableJobs.getSelectedRows();
         if (selectedRows.length > 0) {
           if (JOptionPane.showOptionDialog(MultipleJobPanel.this,
               "Do you really want to cancel selected job(s)?",
               "Job Monitor - Confirm Job Cancel",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.QUESTION_MESSAGE,
               null, null, null) == 0) {
             //String result, title;
             String cancelErrorMsg = "";
             Job job = null;
             String jobIdText = "";
             String status = "";
             for (int i = 0; i < selectedRows.length; i++) {
               status = jTableJobs.getValueAt(selectedRows[i],
                   JOB_STATUS_COLUMN_INDEX).toString().trim();
               if (status.indexOf(Utils.STATE_CANCELLING) != -1) {
                 continue; // The job is already in Cancel Req.
               }
               try {
                 jobIdText = jTableJobs.getValueAt(selectedRows[i],
                     JOB_ID_COLUMN_INDEX).toString().trim();
                 job = new Job(new JobId(jobIdText));
                 job.setLoggerLevel(GUIGlobalVars.getGUIConfVarNSLoggerLevel());
                 UserCredential userCredential = new UserCredential(new File(
                     GUIGlobalVars.proxyFilePath));
                 if (userCredential.getX500UserSubject().equals(GUIGlobalVars.
                     proxySubject)) {
                   job.cancel();
                 } else {
                   JOptionPane.showOptionDialog(MultipleJobPanel.this,
                       Utils.FATAL_ERROR + "Proxy file user subject has changed"
                       + "\nApplication will be terminated",
                       Utils.ERROR_MSG_TXT,
                       JOptionPane.DEFAULT_OPTION,
                       JOptionPane.ERROR_MESSAGE,
                       null, null, null);
                   System.exit( -1);
                 }
                 jTableJobs.setValueAt(status + Utils.STATE_CANCELLING,
                     selectedRows[i], JOB_STATUS_COLUMN_INDEX);
                 //jTableJobs.setValueAt(STATE_CANCELLING, selectedRows[i], JOB_STATUS_COLUMN_INDEX);
               } catch (Exception exc) {
                 if (isDebugging) {
                   exc.printStackTrace();
                 }
                 cancelErrorMsg += "- " + jobIdText + "\n"
                     + "   (" + exc.getMessage() + ")\n";
               }
             }
             if (!cancelErrorMsg.equals("")) {
               GraphicUtils.showOptionDialogMsg(MultipleJobPanel.this, cancelErrorMsg,
                   "Job Monitor - Job Cancel",
                   JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                   Utils.MESSAGE_LINES_PER_JOPTIONPANE,
                   "Unable to cancel the job(s):", null);
             }
           }
         } else {
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               "Please select at least a job",
               Utils.INFORMATION_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.INFORMATION_MESSAGE,
               null, null, null);
         }
         // END Standard code
 
         return "";
       }
     };
     worker.start();
   }
 
   void jButtonGetOutputEvent(ActionEvent e) {
     final SwingWorker worker = new SwingWorker() {
       public Object construct() {
 
         // Standard code
         int[] selectedRow = jTableJobs.getSelectedRows();
         if (selectedRow.length > 0) {
           JFileChooser outputDir = new JFileChooser();
           outputDir.setDialogTitle(
               "Job Output file(s) storing directory selection");
           outputDir.setApproveButtonToolTipText(
               "Retrieve the output file(s) of the selected job in the directory");
           outputDir.setApproveButtonText("Retrieve");
           outputDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
           outputDir.setMultiSelectionEnabled(false);
           int r = outputDir.showOpenDialog(MultipleJobPanel.this);
           if (r != JFileChooser.APPROVE_OPTION) {
             return "";
           }
           File selectedDir = outputDir.getSelectedFile();
           if (selectedDir.isDirectory()) {
             Job jobs[] = new Job[selectedRow.length];
             for (int i = 0; i < selectedRow.length; i++) {
               try {
                 jobs[i] = new Job(new JobId((String) jTableJobs.getValueAt(
                     selectedRow[i],
                     JOB_ID_COLUMN_INDEX)));
               } catch (Exception exc) {
                 if (isDebugging) {
                   exc.printStackTrace();
                 }
               }
             }
             try {
               JobCollection jobCollection = new JobCollection(jobs);
               jobCollection.setLoggerLevel(GUIGlobalVars.
                   getGUIConfVarNSLoggerLevel());
 
               //!!! TO REMOVE test only
               //jobCollection.setMaxThreadNumber(1);
               Vector output = new Vector();
               UserCredential userCredential = new UserCredential(new File(
                   GUIGlobalVars.proxyFilePath));
               if (userCredential.getX500UserSubject().equals(GUIGlobalVars.
                   proxySubject)) {
                 output = jobCollection.getOutput(selectedDir.toString());
               } else {
                 JOptionPane.showOptionDialog(MultipleJobPanel.this,
                     Utils.FATAL_ERROR + "Proxy file user subject has changed"
                     + "\nApplication will be terminated",
                     Utils.ERROR_MSG_TXT,
                     JOptionPane.DEFAULT_OPTION,
                     JOptionPane.ERROR_MESSAGE,
                     null, null, null);
                 System.exit( -1);
               }
               String errorMsg = "";
               String infoMsg = "";
               Result result;
               int statusCode;
               int index = 0;
               String jobIdText = "";
               for (int i = 0; i < output.size(); i++) {
                 result = (Result) output.get(i);
                 jobIdText = jobs[i].getJobId().toString();
                 if (result != null) {
                   statusCode = result.getCode();
                   if ((statusCode == Result.GETOUTPUT_FAILURE) ||
                       (statusCode == Result.GETOUTPUT_FORBIDDEN)) {
                     logger.debug("result: " + result);
                     logger.debug("result.getResult(): " + result.getResult());
                     errorMsg += "- " +
                         ((Exception) result.getResult()).getMessage()
                         + "\n   Job: " + jobIdText + "\n";
                   } else {
                     logger.debug("jobs[i].getJobId().getUnique(): " +
                         jobs[i].getJobId().getUnique());
                     infoMsg += "- " + selectedDir.toString() + File.separator
                         + System.getProperty("user.name") + "_"
                         + jobs[i].getJobId().getUnique() + "\n   " + "Job: " +
                         jobIdText + "\n";
                   }
                 } else {
                   errorMsg += "Unespected error getting job output" +
                       "\n   Job: " + jobIdText + "\n";
                 }
               }
               errorMsg = errorMsg.trim();
               if (!errorMsg.equals("")) {
                 GraphicUtils.showOptionDialogMsg(MultipleJobPanel.this, errorMsg,
                     "Job Monitor - Job Output",
                     JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                     Utils.MESSAGE_LINES_PER_JOPTIONPANE,
                     "Unable to get output:", null);
               }
               infoMsg = infoMsg.trim();
               if (!infoMsg.equals("")) {
                 GraphicUtils.showOptionDialogMsg(MultipleJobPanel.this, infoMsg,
                     "Job Monitor - Output Retrieval Success",
                     JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                     Utils.MESSAGE_LINES_PER_JOPTIONPANE,
                     "The Output File(s) have been successfully retrieved and stored in:",
                     null);
               }
             } catch (Exception exc) {
               exc.printStackTrace();
               JOptionPane.showOptionDialog(MultipleJobPanel.this,
                   exc.getMessage(),
                   "Job Monitor - Job Output",
                   JOptionPane.DEFAULT_OPTION,
                   JOptionPane.ERROR_MESSAGE,
                   null, null, null);
             }
           } else {
             JOptionPane.showOptionDialog(MultipleJobPanel.this,
                 "Unable to find selected directory: " + selectedDir,
                 Utils.ERROR_MSG_TXT,
                 JOptionPane.DEFAULT_OPTION,
                 JOptionPane.ERROR_MESSAGE,
                 null, null, null);
           }
         } else {
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               "Please select at least a job",
               Utils.INFORMATION_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.INFORMATION_MESSAGE,
               null, null, null);
         }
         // END Standard code
 
         return "";
       }
     };
     worker.start();
   }
 
   Vector getEventVector(String jobIdText) {
     Vector eventVector = new Vector();
     JobId jobId;
     Job job;
     Result result;
     try {
       jobId = new JobId(jobIdText);
     } catch (Exception ex) {
       if (isDebugging) {
         ex.printStackTrace();
       }
       return eventVector;
     }
     try {
       job = new Job(jobId);
     } catch (Exception ex) {
       if (isDebugging) {
         ex.printStackTrace();
       }
       return eventVector;
     }
     try {
       result = job.getLogInfo();
       if (result.getCode() == Result.SUCCESS) {
         eventVector = (Vector) result.getResult();
       }
     } catch (Exception ex) {
       if (isDebugging) {
         ex.printStackTrace();
       }
     }
     return eventVector;
   }
 
   void jButtonLogInfoEvent(ActionEvent e) {
     final SwingWorker worker = new SwingWorker() {
       public Object construct() {
 
         // Standard code
         int[] selectedRows = jTableJobs.getSelectedRows();
         String selectedJobId = "";
         JobId jobId = null;
         Job job = null;
         Result result = null;
         if (selectedRows.length == 0) {
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               "Please select a job",
               Utils.INFORMATION_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.INFORMATION_MESSAGE,
               null, null, null);
         } else if (selectedRows.length == 1) {
           selectedJobId = (String) jTableJobs.getValueAt(selectedRows[0],
               JOB_ID_COLUMN_INDEX);
           try {
             jobId = new JobId(selectedJobId);
           } catch (Exception ex) {
             if (isDebugging) {
               ex.printStackTrace();
               //SW return;
             }
             return "";
           }
           try {
             job = new Job(jobId);
           } catch (Exception ex) {
             if (isDebugging) {
               ex.printStackTrace();
               //SW return;
             }
             return "";
           }
           try {
             UserCredential userCredential = new UserCredential(new File(
                 GUIGlobalVars.proxyFilePath));
             if (userCredential.getX500UserSubject().equals(GUIGlobalVars.
                 proxySubject)) {
               result = job.getLogInfo();
             } else {
               JOptionPane.showOptionDialog(MultipleJobPanel.this,
                   Utils.FATAL_ERROR + "Proxy file user subject has changed"
                   + "\nApplication will be terminated",
                   Utils.ERROR_MSG_TXT,
                   JOptionPane.DEFAULT_OPTION,
                   JOptionPane.ERROR_MESSAGE,
                   null, null, null);
               System.exit( -1);
             }
           } catch (Exception ex) {
             if (isDebugging) {
               ex.printStackTrace();
             }
             JOptionPane.showOptionDialog(MultipleJobPanel.this,
                 ex.getMessage(),
                 Utils.ERROR_MSG_TXT,
                 JOptionPane.DEFAULT_OPTION,
                 JOptionPane.ERROR_MESSAGE,
                 null, null, null);
             return "";
           }
           String logInfoText = "";
           Vector eventVector = new Vector();
           if ((result == null) || (result.getCode() != Result.SUCCESS)) {
             //SW return;
             return "";
           } else {
             eventVector = (Vector) result.getResult();
             if (!isLogInfoJDialogShown) {
               logInfoJDialog = new LogInfoFrame(MultipleJobPanel.this,
                   eventVector);
               GraphicUtils.screenCenterWindow(logInfoJDialog);
               logInfoJDialog.show();
               isLogInfoJDialogShown = true;
             } else {
               logInfoJDialog.setTableEvents(eventVector);
               logInfoJDialog.setVisible(false);
               GraphicUtils.deiconifyFrame(logInfoJDialog);
               logInfoJDialog.setVisible(true);
             }
             Date date = new Date();
             logInfoJDialog.setJLabelLastUpdate(date.toString());
           }
         } else {
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               "Please select a single job",
               Utils.INFORMATION_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.INFORMATION_MESSAGE,
               null, null, null);
         }
         // END Standard code
 
         return "";
       }
     };
     worker.start();
   }
 
   void disposeStatusDetails() {
     if (isSingleJobDialogShown) {
       singleJobDialog.dispose();
       isSingleJobDialogShown = false;
     }
   }
 
   void disposeLogInfo() {
     if (isLogInfoJDialogShown) {
       logInfoJDialog.dispose();
       isLogInfoJDialogShown = false;
     }
   }
 
   protected void renewJPopupMenu() {
     jPopupMenuTable.add(jMenuItemRemove);
     jPopupMenuTable.add(jMenuItemClear);
 
     jPopupMenuTable.addSeparator();
 
     jPopupMenuTable.add(jMenuItemSelectAll);
     jPopupMenuTable.add(jMenuItemSelectNone);
     jPopupMenuTable.add(jMenuItemInvertSelection);
 
     jPopupMenuTable.addSeparator();
 
     jPopupMenuTable.add(jMenuItemDetails);
     jPopupMenuTable.add(jMenuItemLogInfo);
     jPopupMenuTable.add(jMenuItemUpdate);
 
     jPopupMenuTable.addSeparator();
 
     jPopupMenuTable.add(jMenuItemJobCancel);
     jPopupMenuTable.add(jMenuItemJobOutput);
 
     jPopupMenuTable.addSeparator();
 
     jPopupMenuTable.add(jMenuItemInteractiveConsole);
     jPopupMenuTable.add(jMenuItemRetrieveCheckpointState);
 
     jPopupMenuTable.addSeparator();
 
     jPopupMenuTable.add(jMenuItemDagNodes);
     jPopupMenuTable.add(jMenuItemDagMonitor);
 
     jPopupMenuTable.addSeparator();
     jPopupMenuTable.add(jMenuItemSortAddingOrder);
   }
 
   protected void createJPopupMenu() {
     ActionListener alst = null;
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jMenuRemove();
       }
     };
     jMenuItemRemove.addActionListener(alst);
 
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jMenuClear();
       }
     };
     jMenuItemClear.addActionListener(alst);
 
     jPopupMenuTable.add(jMenuItemRemove);
     jPopupMenuTable.add(jMenuItemClear);
 
     jPopupMenuTable.addSeparator();
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jMenuSelectAll();
       }
     };
     jMenuItemSelectAll.addActionListener(alst);
 
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jMenuSelectNone();
       }
     };
     jMenuItemSelectNone.addActionListener(alst);
 
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jMenuInvertSelection();
       }
     };
     jMenuItemInvertSelection.addActionListener(alst);
 
     jPopupMenuTable.add(jMenuItemSelectAll);
     jPopupMenuTable.add(jMenuItemSelectNone);
     jPopupMenuTable.add(jMenuItemInvertSelection);
 
     jPopupMenuTable.addSeparator();
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonDetailsEvent(null);
       }
     };
     jMenuItemDetails.addActionListener(alst);
 
     jPopupMenuTable.add(jMenuItemDetails);
 
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonLogInfoEvent(null);
       }
     };
     jMenuItemLogInfo.addActionListener(alst);
 
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonUpdateEvent(null);
       }
     };
     jMenuItemUpdate.addActionListener(alst);
 
     jPopupMenuTable.add(jMenuItemDetails);
     jPopupMenuTable.add(jMenuItemLogInfo);
     jPopupMenuTable.add(jMenuItemUpdate);
 
     jPopupMenuTable.addSeparator();
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonCancelEvent(null);
       }
     };
     jMenuItemJobCancel.addActionListener(alst);
 
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonGetOutputEvent(null);
       }
     };
     jMenuItemJobOutput.addActionListener(alst);
 
     jPopupMenuTable.add(jMenuItemJobCancel);
     jPopupMenuTable.add(jMenuItemJobOutput);
 
     jPopupMenuTable.addSeparator();
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jMenuInteractiveConsole(e);
       }
     };
     jMenuItemInteractiveConsole.addActionListener(alst);
 
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonRetrieveCheckpointStateEvent(null);
       }
     };
     jMenuItemRetrieveCheckpointState.addActionListener(alst);
 
     jPopupMenuTable.add(jMenuItemInteractiveConsole);
     jPopupMenuTable.add(jMenuItemRetrieveCheckpointState);
 
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonDagNodesEvent(null);
       }
     };
     jMenuItemDagNodes.addActionListener(alst);
 
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         jButtonDagMonEvent(null);
       }
     };
     jMenuItemDagMonitor.addActionListener(alst);
 
     alst = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         sortBy(jTableJobs, Utils.NO_SORTING, true);
       }
     };
     jMenuItemSortAddingOrder.addActionListener(alst);
 
     jPopupMenuTable.addSeparator();
     jPopupMenuTable.add(jMenuItemDagNodes);
     jPopupMenuTable.add(jMenuItemDagMonitor);
 
     jPopupMenuTable.addSeparator();
     jPopupMenuTable.add(jMenuItemSortAddingOrder);
 
   }
 
   void showJPopupMenuTable(MouseEvent e) {
     if (e.isPopupTrigger()) {
       Point point = e.getPoint();
       int row = jTableJobs.rowAtPoint(point);
       int column = jTableJobs.columnAtPoint(point);
       if ((row != -1) && !jTableJobs.isRowSelected(row)) {
         jTableJobs.setRowSelectionInterval(row, row);
       }
 
       if (jTableJobs.getRowCount() != 0) {
         if (jTableJobs.getRowCount() >= 2) {
           jMenuItemSortAddingOrder.setEnabled(true);
         } else {
           jMenuItemSortAddingOrder.setEnabled(false);
         }
         int selectedRowCount = jTableJobs.getSelectedRowCount();
         if (selectedRowCount == 0) {
           jMenuItemRemove.setEnabled(false);
           jMenuItemDetails.setEnabled(false);
           jMenuItemLogInfo.setEnabled(false);
           jMenuItemJobCancel.setEnabled(false);
           jMenuItemJobOutput.setEnabled(false);
           jMenuItemInteractiveConsole.setEnabled(false);
           jMenuItemRetrieveCheckpointState.setEnabled(false);
           jMenuItemDagNodes.setEnabled(false);
           jMenuItemDagMonitor.setEnabled(false);
         } else {
           if (selectedRowCount == 1) {
             int selectedRow = jTableJobs.getSelectedRow();
             String jobType = jobTableModel.getValueAt(selectedRow,
                 JOB_TYPE_COLUMN_INDEX).toString().trim();
             String jobState = jobTableModel.getValueAt(selectedRow,
                 JOB_STATUS_COLUMN_INDEX).toString().trim();
             jMenuItemDetails.setEnabled(true);
             jMenuItemLogInfo.setEnabled(true);
             if ((jobType.indexOf(Jdl.JOBTYPE_INTERACTIVE) != -1)
                 && (jobState.indexOf(JobStatus.code[JobStatus.DONE]) == -1)) {
               jMenuItemInteractiveConsole.setEnabled(true);
             } else {
               jMenuItemInteractiveConsole.setEnabled(false);
             }
             if (jobType.indexOf(Jdl.JOBTYPE_CHECKPOINTABLE) != -1) {
               jMenuItemRetrieveCheckpointState.setEnabled(true);
             } else {
               jMenuItemRetrieveCheckpointState.setEnabled(false);
             }
             if (jobType.equals(Jdl.TYPE_DAG)) {
               jMenuItemDagNodes.setEnabled(true);
               jMenuItemDagMonitor.setEnabled(true);
             } else {
               jMenuItemDagNodes.setEnabled(false);
               jMenuItemDagMonitor.setEnabled(false);
             }
           } else {
             jMenuItemDetails.setEnabled(false);
             jMenuItemLogInfo.setEnabled(false);
             jMenuItemInteractiveConsole.setEnabled(false);
             jMenuItemRetrieveCheckpointState.setEnabled(false);
             jMenuItemDagNodes.setEnabled(false);
             jMenuItemDagMonitor.setEnabled(false);
           }
           jMenuItemRemove.setEnabled(true);
           jMenuItemJobCancel.setEnabled(true);
           jMenuItemJobOutput.setEnabled(true);
         }
       } else {
         jMenuItemSortAddingOrder.setEnabled(false);
         jMenuItemRemove.setEnabled(false);
         jMenuItemClear.setEnabled(false);
         jMenuItemSelectAll.setEnabled(false);
         jMenuItemSelectNone.setEnabled(false);
         jMenuItemInvertSelection.setEnabled(false);
         jMenuItemDetails.setEnabled(false);
         jMenuItemLogInfo.setEnabled(false);
         jMenuItemUpdate.setEnabled(false);
         jMenuItemJobCancel.setEnabled(false);
         jMenuItemJobOutput.setEnabled(false);
         jMenuItemInteractiveConsole.setEnabled(false);
         jMenuItemRetrieveCheckpointState.setEnabled(false);
         jMenuItemDagNodes.setEnabled(false);
         jMenuItemDagMonitor.setEnabled(false);
       }
       //!!! REMOVE FOLLOWING LINE TEST ONLY
       //jMenuItemInteractiveConsole.setEnabled(true);
 
       jPopupMenuTable = new JPopupMenu();
       renewJPopupMenu();
       jPopupMenuTable.show(e.getComponent(), e.getX(), e.getY());
     }
   }
 
   void jMenuSelectAll() {
     jTableJobs.selectAll();
   }
 
   void jMenuSelectNone() {
     jTableJobs.clearSelection();
   }
 
   void jMenuInvertSelection() {
     int rowCount = jTableJobs.getRowCount();
     int selectedRowCount = jTableJobs.getSelectedRowCount();
     if (selectedRowCount != 0) {
       ListSelectionModel listSelectionModel = jTableJobs.getSelectionModel();
       for (int i = 0; i < rowCount; i++) {
         if (listSelectionModel.isSelectedIndex(i)) {
           listSelectionModel.removeSelectionInterval(i, i);
         } else {
           listSelectionModel.addSelectionInterval(i, i);
         }
       }
     } else {
       jTableJobs.selectAll();
     }
   }
 
   void jMenuClear() {
     int choice = JOptionPane.showOptionDialog(MultipleJobPanel.this,
         "Clear job table?",
         "Job Monitor - Confirm Clear",
         JOptionPane.YES_NO_OPTION,
         JOptionPane.QUESTION_MESSAGE,
         null, null, null);
     if (choice == 0) {
       int rowCount = jobTableModel.getRowCount();
       String jobIdText;
       String errorMsg = "";
       String informationMsg = "";
       for (int i = rowCount - 1; i >= 0; i--) {
         jobIdText = jobTableModel.getValueAt(i, JOB_ID_COLUMN_INDEX).toString();
         try {
           this.jobCollection.remove(new Job(new JobId(jobIdText)));
           jobTableModel.removeRow(i);
 
           if ((isSingleJobDialogShown) &&
               (singleJobDialog.getJobIdShown().equals(jobIdText))) {
             disposeStatusDetails();
           }
           if ((isLogInfoJDialogShown) &&
               (logInfoJDialog.getJobIdShown().equals(jobIdText))) {
             disposeLogInfo();
           }
           if (GUIGlobalVars.openedListenerFrameMap.containsKey(jobIdText)) {
             informationMsg += "- " + jobIdText;
             GUIGlobalVars.openedListenerFrameMap.remove(jobIdText);
           }
         } catch (NoSuchFieldException nsfe) {
           if (isDebugging) {
             nsfe.printStackTrace();
           }
           errorMsg += "- " + jobIdText;
         } catch (Exception e) {
           if (isDebugging) {
             e.printStackTrace();
           }
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               e.getMessage(),
               Utils.ERROR_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.ERROR_MESSAGE,
               null, null, null);
         }
       }
       jLabelTotalDisplayedJobs.setText(Integer.toString(jobTableModel.
           getRowCount()));
       if (!errorMsg.equals("")) {
         GraphicUtils.showOptionDialogMsg(MultipleJobPanel.this, errorMsg,
             "Job Monitor - Clear",
             JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
             Utils.MESSAGE_LINES_PER_JOPTIONPANE,
             "Unable to remove the Job(s):", null);
       }
       if (!informationMsg.equals("")) {
         GraphicUtils.showOptionDialogMsg(MultipleJobPanel.this, informationMsg,
             "Job Monitor - Clear",
             JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
             Utils.MESSAGE_LINES_PER_JOPTIONPANE,
             "An interactive window is opened for the Job(s):", null);
       }
     }
   }
 
   void jMenuRemove() {
     int[] selectedRows = jTableJobs.getSelectedRows();
     int selectedRowCount = selectedRows.length;
     int choice = JOptionPane.showOptionDialog(MultipleJobPanel.this,
         "Remove selected Job(s) from table?",
         "Job Monitor - Confirm Remove",
         JOptionPane.YES_NO_OPTION,
         JOptionPane.QUESTION_MESSAGE,
         null, null, null);
     if (choice == 0) {
       String jobIdText;
       String informationMsg = "";
       String errorMsg = "";
       int index = -1;
       for (int i = selectedRowCount - 1; i >= 0; i--) {
         jobIdText = jobTableModel.getValueAt(selectedRows[i],
             JOB_ID_COLUMN_INDEX).toString();
         try {
           Job job = new Job();
           job.setJobId(new JobId(jobIdText));
           //this.jobCollection.remove(new Job(new JobId(jobIdText)));
           this.jobCollection.remove(job);
           jobTableModel.removeRow(selectedRows[i]);
           //!!!index = jobMonitorJFrame.jobTableModel.getIndexOfElementInColumn(jobIdText, JobMonitor.JOB_ID_COLUMN_INDEX);
           //if (index != -1) {
           //jobMonitorJFrame.jobTableModel.removeRow(index);
           //}
 
           if ((isSingleJobDialogShown) &&
               (singleJobDialog.getJobIdShown().equals(jobIdText))) {
             disposeStatusDetails();
           }
           if ((isLogInfoJDialogShown) &&
               (logInfoJDialog.getJobIdShown().equals(jobIdText))) {
             disposeLogInfo();
           }
           if (GUIGlobalVars.openedListenerFrameMap.containsKey(jobIdText)) {
             informationMsg += "- " + jobIdText;
             GUIGlobalVars.openedListenerFrameMap.remove(jobIdText);
           }
         } catch (NoSuchFieldException nsfe) {
           if (isDebugging) {
             nsfe.printStackTrace();
           }
           errorMsg += "- " + jobIdText;
           /*
                    } catch (JobCollectionException jce) {
             if (isDebugging) jce.printStackTrace();
             errorMsg += "- " + jobIdText;
            */
         } catch (Exception e) {
           if (isDebugging) {
             e.printStackTrace();
           }
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               e.getMessage(),
               Utils.ERROR_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.ERROR_MESSAGE,
               null, null, null);
         }
       }
       jLabelTotalDisplayedJobs.setText(Integer.toString(jobTableModel.
           getRowCount()));
       if (!errorMsg.equals("")) {
         GraphicUtils.showOptionDialogMsg(MultipleJobPanel.this, errorMsg,
             "Job Monitor - Remove",
             JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
             Utils.MESSAGE_LINES_PER_JOPTIONPANE,
             "Unable to remove the Job(s):", null);
       }
       if (!informationMsg.equals("")) {
         GraphicUtils.showOptionDialogMsg(MultipleJobPanel.this, informationMsg,
             "Job Monitor - Remove",
             JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
             Utils.MESSAGE_LINES_PER_JOPTIONPANE,
             "An interactive window is opened for the Job(s):", null);
       }
     }
   }
 
   void jMenuInteractiveConsole(ActionEvent ae) {
     final SwingWorker worker = new SwingWorker() {
       public Object construct() {
 
         // Standard code
         int[] selectedRows = jTableJobs.getSelectedRows();
         if (selectedRows.length == 1) {
           String jobIdText = jTableJobs.getValueAt(selectedRows[0],
               JOB_ID_COLUMN_INDEX).toString().trim();
           if (!GUIGlobalVars.openedListenerFrameMap.containsKey(jobIdText)) {
             ListenerFrame listener = new ListenerFrame();
             listener.setJobIdTextField(jobIdText);
             GUIGlobalVars.openedListenerFrameMap.put(jobIdText, listener);
             GraphicUtils.screenCenterWindow(listener);
             try {
               Job job = new Job(new JobId(jobIdText));
               logger.debug("job: " + job + " listener: " + listener);
               job.attach(listener);
             } catch (Exception e) {
               if (isDebugging) {
                 e.printStackTrace();
               }
             }
           } else {
             ListenerFrame listener = (ListenerFrame) GUIGlobalVars.
                 openedListenerFrameMap.get(jobIdText);
             GraphicUtils.deiconifyFrame(listener);
             listener.setVisible(true);
           }
         }
         // END Standard code
 
         return "";
       }
     };
     worker.start();
   }
 
   void jButtonRetrieveCheckpointStateEvent(ActionEvent ae) {
     final SwingWorker worker = new SwingWorker() {
       public Object construct() {
 
         // Standard code
         int[] selectedRows = jTableJobs.getSelectedRows();
         //SW if (selectedRows.length != 1) return;
         if (selectedRows.length != 1) {
           return "";
         }
         String jobId = jobTableModel.getValueAt(selectedRows[0],
             JOB_ID_COLUMN_INDEX).toString();
         RetrieveCheckpointStateDialog retrieveCheckpointState =
             new RetrieveCheckpointStateDialog(jobMonitorJFrame, jobId, false);
         retrieveCheckpointState.setModal(true);
         GraphicUtils.windowCenterWindow(jobMonitorJFrame, retrieveCheckpointState);
         retrieveCheckpointState.show();
         int state = retrieveCheckpointState.getState();
         logger.debug("jobId: " + jobId + " state: " + Integer.toString(state));
         Job job = new Job();
         JobState jobState = new JobState();
         try {
           job = new Job(new JobId(jobId));
         } catch (IllegalArgumentException iae) {
           if (isDebugging) {
             iae.printStackTrace();
           }
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               iae.getMessage(),
               "Job Monitor - Retrieve Checkpoint State",
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.ERROR_MESSAGE,
               null, null, null);
           //SW return;
           return "";
         } catch (Exception e) {
           if (isDebugging) {
             e.printStackTrace();
           }
         }
 
         try {
           UserCredential userCredential = new UserCredential(new File(
               GUIGlobalVars.proxyFilePath));
           if (userCredential.getX500UserSubject().equals(GUIGlobalVars.
               proxySubject)) {
             jobState = job.getState(state);
           } else {
             JOptionPane.showOptionDialog(MultipleJobPanel.this,
                 Utils.FATAL_ERROR + "Proxy file user subject has changed"
                 + "\nApplication will be terminated",
                 Utils.ERROR_MSG_TXT,
                 JOptionPane.DEFAULT_OPTION,
                 JOptionPane.ERROR_MESSAGE,
                 null, null, null);
             System.exit( -1);
           }
           if (jobState != null) {
             JFileChooser fileChooser = new JFileChooser();
             fileChooser.setDialogTitle("Save Checkpoint State - " + jobId);
             fileChooser.setCurrentDirectory(new File(GUIGlobalVars.
                 getFileChooserWorkingDirectory()));
 
             String[] extensions = {
                 "CHKPT"};
             GUIFileFilter classadFileFilter = new GUIFileFilter("chkpt",
                 extensions);
             fileChooser.addChoosableFileFilter(classadFileFilter);
 
             int choice = fileChooser.showSaveDialog(MultipleJobPanel.this);
 
             if (choice != JFileChooser.APPROVE_OPTION) {
               //SW return;
               return "";
             } else {
               GUIGlobalVars.setFileChooserWorkingDirectory(fileChooser.
                   getCurrentDirectory().toString());
               File file = fileChooser.getSelectedFile();
               String selectedFile = file.toString().trim();
               String extension = GUIFileSystem.getFileExtension(file).toUpperCase();
               FileFilter selectedFileFilter = fileChooser.getFileFilter();
               if (!extension.equals("CHKPT") &&
                   selectedFileFilter.getDescription().equals("chkpt")) {
                 selectedFile += ".chkpt";
               }
 
               choice = 0;
               if (new File(selectedFile).isFile()) {
                 choice = JOptionPane.showOptionDialog(MultipleJobPanel.this,
                     "Output file exists. Overwrite?",
                     "Job Monitor - Confirm Save",
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.QUESTION_MESSAGE,
                     null, null, null);
               }
               if (choice == 0) {
                 GUIFileSystem.saveTextFile(selectedFile, jobState.toString(true, true));
               }
             }
           }
         } catch (Exception e) {
           if (isDebugging) {
             e.printStackTrace();
           }
           JOptionPane.showOptionDialog(MultipleJobPanel.this,
               e.getMessage(),
               Utils.ERROR_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.ERROR_MESSAGE,
               null, null, null);
         }
         // END Standard code
 
         return "";
       }
     };
     worker.start();
   }
 
   public int getJobCollectionSize() {
     return this.jobCollection.size();
   }
 
   static public void setMaxMonitoredJobNumber(int number) {
     if ((Utils.MAX_MONITORED_JOB_NUMBER_MIN_VAL <= number) &&
         (number <= Utils.MAX_MONITORED_JOB_NUMBER_MAX_VAL)) {
       maxMonitoredJobNumber = number;
     }
   }
 
   static public int getMaxMonitoredJobNumber() {
     return maxMonitoredJobNumber;
   }
 
   HashMap getJobStatusHashMap() {
     return (HashMap)this.jobStatusHashMap.clone();
   }
 
   void clearJobCollection() {
     this.jobCollection.clear();
   }
 
   void startUpdateThread() {
     updateThread.startThread();
   }
 
   void restartUpdateThread() {
     updateThread.restartThread();
   }
 
   void stopUpdateThread() {
     updateThread.stopThread();
   }
 
   void exitUpdateThread() {
     updateThread.exitThread();
   }
 
   void startCurrentTimeThread() {
     currentTimeThread.startThread();
   }
 
   void stopCurrentTimeThread() {
     currentTimeThread.stopThread();
   }
 
   void exitCurrentTimeThread() {
     currentTimeThread.exitThread();
   }
 
   void sortBy(JTable table, int columnIndex, boolean ascending) {
     int[] selectedRows = table.getSelectedRows();
     Vector selectedJobIdVector = new Vector();
     JobTableModel jobTableModel = (JobTableModel) table.getModel();
     for (int i = 0; i < selectedRows.length; i++) {
       selectedJobIdVector.add(jobTableModel.getValueAt(selectedRows[i],
           JOB_ID_COLUMN_INDEX).toString().trim());
     }
 
     TableColumnModel columnModel = table.getColumnModel();
     if (columnIndex == Utils.NO_SORTING) { // Table Adding Order.
       this.sortingColumn = columnIndex;
       TableColumn column = null;
       for (int i = 0; i < table.getColumnCount(); i++) {
         column = columnModel.getColumn(i);
         String name = column.getHeaderValue().toString();
         int index = name.lastIndexOf("");
         int index2 = name.lastIndexOf("");
         if (index2 != -1) {
           index = index2;
         }
         if (index != -1) {
           name = name.substring(0, index).trim();
         }
         column.setHeaderValue(name);
       }
       table.getTableHeader().repaint();
 
       Iterator jobIterator = this.jobCollection.jobs();
       String jobIdText = "";
       int index = -1;
       int counter = 0;
       while (jobIterator.hasNext()) {
         try {
           jobIdText = ((Job) jobIterator.next()).getJobId().toString();
         } catch (Exception e) {
           e.printStackTrace();
           return;
         }
         index = jobTableModel.getIndexOfElementInColumn(jobIdText,
             JOB_ID_COLUMN_INDEX);
         if (index != -1) {
           jobTableModel.moveRow(index, index, counter);
           counter++;
         }
       }
     } else {
       jobTableModel.sortBy(jTableJobs, columnIndex, ascending);
     }
     jobTableModel.fireTableStructureChanged();
     table.repaint();
     int index = 0;
     for (int i = 0; i < selectedJobIdVector.size(); i++) {
       index = jobTableModel.getIndexOfElementInColumn(selectedJobIdVector.get(i)
           .toString(), JOB_ID_COLUMN_INDEX);
       if (index != -1) {
         table.addRowSelectionInterval(index, index);
       }
     }
   }
 
 }
 
 
 
 /*
  ********************
   CLASS GUIThread
  ********************
  */
 /*
  class GUIThread extends Thread {
    static Logger logger = Logger.getLogger(JobSubmitter.class);
    static final boolean THIS_CLASS_DEBUG = false;
    private boolean isDebugging = THIS_CLASS_DEBUG || Utils.GLOBAL_DEBUG;
    Component component;
    private volatile boolean active = true;
    ServerSocket serverSocket;
    String temporaryDirectory = "";
    int port;
    public GUIThread(Component component, String temporaryDirectory) {
   this.component = component;
   setDaemon(true); // Thread will be killed when calling application will die.
   boolean isSuccess = false;
   int port = Utils.GUI_THREAD_INITIAL_PORT;
   int lastPort = Utils.GUI_THREAD_INITIAL_PORT + Utils.GUI_THREAD_RETRY_COUNT;
   this.temporaryDirectory = temporaryDirectory;
   try {
     ServerSocket serverSocket = new ServerSocket();
   } catch(Exception e) {
     if (isDebugging) e.printStackTrace();
   }
   while (!isSuccess && (port < lastPort)) {
     logger.info("GUI Thread trying at Port: " + Integer.toString(port));
     this.port = port;
     try {
       serverSocket = new ServerSocket(port);
       isSuccess = true;
       // Writing file containing port number.
       GUIFileSystem.saveTextFile(Utils.getThreadConfigurationFilePath(), Integer.toString(port));
       logger.info("GUI Thread Success at port: " + Integer.toString(port));
     } catch (IOException ioe) {
       // The port is already in use, do nothing, you will try another port.
     } catch (Exception e) {
       if (isDebugging) e.printStackTrace();
       JOptionPane.showOptionDialog(component,
               Utils.FATAL_ERROR
                 + "Some problems occured initialising thread socket\nApplication will be terminated",
               Utils.ERROR_MSG_TXT,
               JOptionPane.DEFAULT_OPTION,
               JOptionPane.ERROR_MESSAGE,
               null, null, null);
       System.exit(-1);
     }
     port++;
   }
   if(!isSuccess) {
     JOptionPane.showOptionDialog(component,
    Utils.FATAL_ERROR
                 + "Some problems occured initialising thread socket\nApplication will be terminated",
    Utils.ERROR_MSG_TXT,
    JOptionPane.DEFAULT_OPTION,
    JOptionPane.ERROR_MESSAGE,
    null, null, null);
     System.exit(-1);
   }
    }
    public void run() {
   Socket clientSocket = null;
   while (active) {
     try {
       clientSocket = serverSocket.accept();
     } catch (Exception e) {
       e.printStackTrace();
     }
     try {
       PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
       BufferedReader in = new BufferedReader(new InputStreamReader(
           clientSocket.getInputStream()));
       String inputLine = "";
       String outputLine = "";
       //serverSocket.setSoTimeout(2000);
       inputLine = in.readLine();
       if (inputLine.equals(Utils.GUI_SOCKET_HANDSHAKE_MSG)) {
         outputLine = Utils.GUI_SOCKET_HANDSHAKE_MSG;
         out.println(outputLine);
         out.println(temporaryDirectory);
       }
       out.close();
       in.close();
       clientSocket.close();
       //serverSocket.close();
     } catch(Exception e) {
       if (isDebugging) e.printStackTrace();
     }
   }
    }
    public boolean setTemporaryDirectory(String temporaryDirectory, String vo) {
   logger.debug("setTemporaryDirectory() - port: " + this.port);
   this.temporaryDirectory = temporaryDirectory;
   logger.debug("setTemporaryDirectory() - temporaryDirectory: " + this.temporaryDirectory);
   try {
     Utils.copyFile(new File(Utils.getThreadConfigurationFilePath(vo)),
                    new File(Utils.getThreadConfigurationFilePath()));
     logger.debug("setTemporaryDirectory() - Source file: " + Utils.getThreadConfigurationFilePath(vo));
     logger.debug("setTemporaryDirectory() - Target file: " + Utils.getThreadConfigurationFilePath());
   } catch (Exception e) {
     e.printStackTrace();
     return false;
   }
   logger.debug("setTemporaryDirectory() - Delete file: " + Utils.getThreadConfigurationFilePath(vo));
   boolean outgoing = (new File(Utils.getThreadConfigurationFilePath(vo))).delete();
   // if (!outgoing) Do nothing. Unable to delete the file.
   return true;
    }
    public void stopThread() {
   active = false;
   interrupt();
    }
  } // END CLASS GUIThread
  */
 
 /*
  ***************************
   CLASS CurrentTimeThread
  ***************************
  */
 
 class CurrentTimeThread extends Thread {
   static Logger logger = Logger.getLogger(JobMonitor.class);
 
   static final boolean THIS_CLASS_DEBUG = false;
   private boolean isDebugging = THIS_CLASS_DEBUG || Utils.GLOBAL_DEBUG;
 
   MultipleJobPanel multipleJobPanel;
   private volatile boolean active = true;
   private volatile boolean run = true;
 
   public CurrentTimeThread(MultipleJobPanel multipleJobPanel) {
     this.multipleJobPanel = multipleJobPanel;
   }
 
   public void run() {
     while (run) {
       while (active) {
         multipleJobPanel.jTextFieldCurrentTime.setText((new Date()).toString());
         try {
           this.sleep(1000);
         } catch (InterruptedException ie) {
         // Do nothing.
         }
         //logger.debug("CurrentTimeThread - WHILE ACTIVE");
       }
       //logger.debug("CurrentTimeThread - WHILE RUN");
       try {
         this.sleep(1000);
       } catch (InterruptedException ie) {
       // Do nothing.
       }
     }
   }
 
   public void stopThread() {
     logger.debug("CurrentTimeThread - stopThread()");
     active = false;
     interrupt();
   }
 
   public void exitThread() {
     logger.debug("CurrentTimeThread - exitThread()");
     run = false;
     active = false;
     interrupt();
   }
 
   public void startThread() {
     logger.debug("CurrentTimeThread - startThread()");
     active = true;
     if (!this.isAlive()) {
       this.start();
     }
   }
 
 } // END CLASS CurrentTimeThread
 
 
 
 /*
  **********************
   CLASS UpdateThread
  **********************
  */
 
 /*// R-GMA Notification Service.
 class UpdateThread extends Thread {
 static Logger logger = Logger.getLogger(JobSubmitter.class);
 static final boolean THIS_CLASS_DEBUG = false;
 private boolean isDebugging = THIS_CLASS_DEBUG || Utils.GLOBAL_DEBUG;
 private long updateRate;
 private Date startTime = new Date();
 private long startTimeLong = startTime.getTime();
 private volatile boolean active = true;
 private int updateCount = 0;
 private MultipleJobPanel multipleJobPanel;
 private String selectStatement = "select * from JobStatusRaw";
 private String whereClause = "";
 private final int consumerType = Consumer.CONTINUOUS;
 private Consumer consumer = null;
 private ServletConnection producer;
 public UpdateThread(MultipleJobPanel multipleJobPanel) {
 this.multipleJobPanel = multipleJobPanel;
 }
 public void run() {
 ResultSet resultSet;
 while (multipleJobPanel.jTableJobs.getRowCount() == 0) {}
 int rowCount = multipleJobPanel.jTableJobs.getRowCount();
 //whereClause = " where Job_id='" + multipleJobPanel.jobTableModel.getValueAt(0, 0) + "'";
 //for (int i = 1; i < rowCount; i++) {
 //whereClause += " OR Job_id='" + multipleJobPanel.jobTableModel.getValueAt(i, 0) + "'";
 //}
 try {
 UserCredential userCredential = new UserCredential(new File(GUIGlobalVars.proxyFilePath));
 whereClause = " where Owner='" + userCredential.getX500UserSubject() + "'";
 } catch (Exception e) {
 // whereClause is set to "".
 if (isDebugging) e.printStackTrace();
 logger.debug("Unable to get User Subject");
 }
 selectStatement += whereClause;
 try {
 logger.debug("selectStatement: " + selectStatement);
 logger.debug("consumerType: " + consumerType);
 consumer = new Consumer(selectStatement, consumerType);
 consumer.start();
 } catch (Exception e) {
 logger.debug("Consumer Constructor (or start()) Exception");
 e.printStackTrace();
 }
 logger.debug("consumer: " + consumer);
 while(active) {
 try {
  if (consumer != null) {
    resultSet = consumer.popIfPossible();
    if (resultSet != null) {
      logger.debug("resultSet.size(): " + resultSet.size());
      String jobIdText = "";
      int index = -1;
      //resultSet.next();
      int count = 1;
      String oldState = "";
      String newState = "";
      while (resultSet.next()) {
        jobIdText = resultSet.getString(1).trim();
        logger.debug("JOB " + count + ": " + jobIdText);
        count++;
 index = multipleJobPanel.jobTableModel.getIndexOfElementInColumn(jobIdText,
            multipleJobPanel.JOB_ID_COLUMN_INDEX);
        if (index != -1) {
          oldState = multipleJobPanel.jobTableModel.getValueAt(
              index, multipleJobPanel.JOB_STATUS_COLUMN_INDEX).toString();
          newState = resultSet.getString(2).trim();
          if ((oldState.indexOf(Utils.STATE_CANCELLING) != -1)
 && (!newState.equals(JobStatus.code[JobStatus.CANCELLED]))) {
            newState += Utils.STATE_CANCELLING;
          }
          multipleJobPanel.jobTableModel.setValueAt(newState,
              index, multipleJobPanel.JOB_STATUS_COLUMN_INDEX);
        }
      }
      logger.debug("\n--- resultSet: " + resultSet.toString() + " ---\n");
    } else {
      logger.debug("\n--- null resultSet ---\n");
    }
  } else {
    logger.debug("Consumer null");
  }
  this.sleep(Utils.AUTO_UPDATE_RATE);
  // If consumer == null try to create it again.
  if (consumer == null) {
    try {
      consumer = new Consumer(selectStatement, consumerType);
      consumer.start();
    } catch (Exception e) {
      logger.debug("Consumer Constructor (or start()) Exception");
      e.printStackTrace();
    }
  }
 } catch (InterruptedException ie) {
  // interrupt() method called from restartThread() do nothing.
 } catch (Exception e) {
  logger.debug("popIfPossible() Exception: " + e.getMessage());
  e.printStackTrace();
  System.exit(-1);
 }
 }
 //consumer.abort();
 }
 public void restartThread() {
 logger.debug("UpdateThread - restartThread()");
 active = true;
 interrupt();
 }
 public void stopThread() {
 logger.debug("UpdateThread - stopThread()");
 active = false;
 interrupt();
 }
 */
 
 class UpdateThread extends Thread {
   static Logger logger = Logger.getLogger(JobMonitor.class);
 
   static final boolean THIS_CLASS_DEBUG = false;
   private boolean isDebugging = THIS_CLASS_DEBUG || Utils.GLOBAL_DEBUG;
 
   private long updateRate;
   private Date startTime = new Date();
   private long startTimeLong = startTime.getTime();
   private volatile boolean active = true;
   private volatile boolean run = true;
   private int updateCount = 0;
   private MultipleJobPanel multipleJobPanel;
 
   // R-GMA update.
   private String selectStatement = "select * from JobStatusRaw";
   private String whereClause = "";
   private final int consumerType = Consumer.CONTINUOUS;
   private Consumer consumer = null;
 
   public UpdateThread(MultipleJobPanel multipleJobPanel) {
     this.multipleJobPanel = multipleJobPanel;
   }
 
   public void run() {
     ResultSet resultSet;
     while (multipleJobPanel.jTableJobs.getRowCount() == 0) {}
     int rowCount = multipleJobPanel.jTableJobs.getRowCount();
     try {
       UserCredential userCredential = new UserCredential(new File(
           GUIGlobalVars.proxyFilePath));
       whereClause = " where Owner='" + userCredential.getX500UserSubject() +
           "'";
     } catch (Exception e) {
       // whereClause is set to "".
       if (isDebugging) {
         e.printStackTrace();
       }
       logger.debug("Unable to get User Subject");
     }
     selectStatement += whereClause;
     while (run) {
       while (active) {
         try {
           switch (GUIGlobalVars.getUpdateMode()) {
             case Utils.LB_MODE:
               updateRate = 1000 * 60 * GUIGlobalVars.getJobMonitorUpdateRate();
               logger.debug("UpdateThread - run() - updateRate: " +
                   GUIGlobalVars.getJobMonitorUpdateRate());
               this.sleep(updateRate);
               multipleJobPanel.threadUpdateEvent();
               break;
             case Utils.RGMA_MODE:
               this.sleep(Utils.RGMA_UPDATE_RATE);
               if (consumer == null) {
                 try {
                   consumer = new Consumer(selectStatement, consumerType);
                   consumer.start();
                 } catch (Exception e) {
                   logger.debug("Consumer Constructor (or start()) Exception");
                   e.printStackTrace();
                 }
                 logger.debug("consumer: " + consumer);
               }
               resultSet = consumer.popIfPossible();
               if (resultSet != null) {
                 logger.debug("resultSet.size(): " + resultSet.size());
                 String jobIdText = "";
                 int index = -1;
                 //resultSet.next();
                 int count = 1;
                 String oldState = "";
                 String newState = "";
                 while (resultSet.next()) {
                   jobIdText = resultSet.getString(1).trim();
                   logger.debug("JOB " + count + ": " + jobIdText);
                   count++;
                   index = multipleJobPanel.jobTableModel.
                       getIndexOfElementInColumn(jobIdText,
                       multipleJobPanel.
                       JOB_ID_COLUMN_INDEX);
                   if (index != -1) {
                     oldState = multipleJobPanel.jobTableModel.getValueAt(
                         index, multipleJobPanel.JOB_STATUS_COLUMN_INDEX).
                         toString();
                     newState = resultSet.getString(2).trim();
                     if ((oldState.indexOf(Utils.STATE_CANCELLING) != -1)
                         && (!newState.equals(JobStatus.code[JobStatus.CANCELLED]))) {
                       newState += Utils.STATE_CANCELLING;
                     }
                     multipleJobPanel.jobTableModel.setValueAt(newState,
                         index, multipleJobPanel.JOB_STATUS_COLUMN_INDEX);
                   }
                 }
                 logger.debug("\n--- resultSet: " + resultSet.toString() +
                     " ---\n");
               } else {
                 logger.debug("\n--- null resultSet ---\n");
               }
               break;
           }
           updateCount++;
           logger.debug("UpdateThread - run() - updateCount: " + updateCount);
         } catch (InterruptedException ie) {
         // interrupt() method called from restartThread() do nothing.
         } catch (Exception e) {
           logger.debug("popIfPossible() Exception: " + e.getMessage());
           e.printStackTrace();
         }
         //logger.debug("UpdateThread - WHILE ACTIVE");
       }
       try {
         this.sleep(1000);
       } catch (InterruptedException ie) {
       // Do nothing.
       }
       //logger.debug("UpdateThread - WHILE RUN");
     }
   }
 
   public void restartThread() {
     logger.debug("UpdateThread - restartThread()");
     active = true;
     interrupt();
   }
 
   public void exitThread() {
     logger.debug("UpdateThread - exitThread()");
     run = false;
     active = false;
     interrupt();
   }
 
   public void stopThread() {
     logger.debug("UpdateThread - stopThread()");
     active = false;
     interrupt();
   }
 
   public void startThread() {
     logger.debug("UpdateThread - startThread()");
     active = true;
     if (!this.isAlive()) {
       this.start();
     }
   }
 
 } // END CLASS UpdateThread
 
 
 
 class CommandThread extends Thread {
   private String command;
   private int exitCode = 0;
 
   public CommandThread(String command) {
     this.command = command;
     this.setDaemon(true);
   }
 
   public void run() {
     this.exitCode = Api.shadow(this.command);
   }
 
   public int getExitCode() {
     return this.exitCode;
   }
 }
 
 
 
 /*
  class UpdateThread extends Thread {
   static Logger logger = Logger.getLogger(JobSubmitter.class);
   static final boolean THIS_CLASS_DEBUG = false;
   private boolean isDebugging = THIS_CLASS_DEBUG || Utils.GLOBAL_DEBUG;
   private long updateRate;
   private Date startTime = new Date();
   private long startTimeLong = startTime.getTime();
   private volatile boolean active = true;
   private int updateCount = 0;
   private MultipleJobPanel multipleJobPanel;
   public UpdateThread(MultipleJobPanel multipleJobPanel) {
     this.multipleJobPanel = multipleJobPanel;
   }
   public void run() {
     while(active) {
       updateRate = 1000 * 60 * GUIGlobalVars.getJobMonitorUpdateRate();
       logger.debug("UpdateThread - run() - updateRate: " + GUIGlobalVars.getJobMonitorUpdateRate());
       try {
         this.sleep(updateRate);
         multipleJobPanel.threadUpdateEvent();
         updateCount++;
         logger.debug("UpdateThread - run() - updateCount: " + updateCount);
       } catch(InterruptedException ie) {
         // interrupt() method called from restartThread() do nothing.
       }
     }
   }
   public void restartThread() {
     logger.debug("UpdateThread - restartThread()");
     active = true;
     interrupt();
   }
   public void stopThread() {
     logger.debug("UpdateThread - stopThread()");
     active = false;
     interrupt();
   }
  } // END CLASS UpdateThread
  */
 
 /*
  *******************************
   CLASS GUITableCellRenderer
  *******************************
  */
 class GUITableCellRenderer extends DefaultTableCellRenderer {
   MultipleJobPanel multipleJobPanel;
 
   public GUITableCellRenderer(Component component) {
     this.multipleJobPanel = (MultipleJobPanel) component;
   }
 
   // This method is called each time a cell in a column
   // using this renderer needs to be rendered.
   public Component getTableCellRendererComponent(JTable table, Object value,
       boolean isSelected, boolean hasFocus, int row, int column) {
 
     super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
         column);
 
     setToolTipText(null);
     if (value instanceof String) {
       String text = value.toString().trim();
       if (!value.equals("")) {
         setToolTipText(text);
       }
     }
 
     if (isSelected) {
       setForeground(table.getSelectionForeground());
       super.setBackground(table.getSelectionBackground());
       if (column == MultipleJobPanel.JOB_STATUS_COLUMN_INDEX) {
         setFont(GraphicUtils.BOLD_FONT);
       }
     } else {
       setForeground(table.getForeground());
       if (column == MultipleJobPanel.JOB_STATUS_COLUMN_INDEX) {
         setBackground(table.getBackground());
         String jobIdText = table.getValueAt(row,
             MultipleJobPanel.JOB_ID_COLUMN_INDEX).toString();
         setHorizontalAlignment(SwingConstants.CENTER);
         setFont(GraphicUtils.BOLD_FONT);
 
         String state = table.getValueAt(row,
             MultipleJobPanel.JOB_STATUS_COLUMN_INDEX).toString();
         //System.out.println("RED" + Color.lightGray.getRed());
         //System.out.println("GREEN" + Color.lightGray.getGreen());
         //System.out.println("BLUE" + Color.lightGray.getBlue());
         if (state.indexOf(JobStatus.code[JobStatus.ABORTED]) != -1) {
           setBackground(new Color(255, 80, 100)); // 204, 50, 75; 225, 50, 75
         } else if (state.indexOf(JobStatus.code[JobStatus.SUBMITTED]) != -1) {
           setBackground(new Color(255, 212, 136)); // 255, 212, 136
         } else if (state.indexOf(JobStatus.code[JobStatus.WAITING]) != -1) {
           setBackground(new Color(255, 247, 178)); // 255, 255, 220
         } else if (state.indexOf(JobStatus.code[JobStatus.READY]) != -1) {
           setBackground(new Color(150, 200, 200)); // 255, 153, 0; 90, 250, 180
         } else if (state.indexOf(JobStatus.code[JobStatus.RUNNING]) != -1) {
           setBackground(new Color(140, 228, 185)); // 0, 208, 188
         } else if (state.indexOf(JobStatus.code[JobStatus.SCHEDULED]) != -1) {
           //setBackground(new Color(255, 247, 178));
           setBackground(new Color(255, 179, 128));
         } else if (state.indexOf(JobStatus.code[JobStatus.DONE]) != -1) {
           setBackground(new Color(204, 255, 255)); // 0, 162, 232
           if (state.indexOf(Utils.STATE_EXIT_CODE_NOT_ZERO) != -1) {
             //setForeground(new Color(255, 0, 0));
             setForeground(new Color(128, 0, 128));
           } else {
             setForeground(Color.black);
           }
         } else if (state.indexOf(JobStatus.code[JobStatus.CANCELLED]) != -1) {
           //setBackground(Color.lightGray);
           setBackground(new Color(176, 201, 201));
         } else if (state.indexOf(JobStatus.code[JobStatus.CLEARED]) != -1) {
           //setBackground(Color.lightGray);
           setBackground(new Color(153, 182, 204));
         } else if (state.indexOf(JobStatus.code[JobStatus.PURGED]) != -1) {
           //setBackground(Color.lightGray);
           setBackground(new Color(223, 223, 227));
         } else if (state.indexOf(JobStatus.code[JobStatus.UNDEF]) != -1) {
           setBackground(Color.lightGray);
         } else if (state.indexOf(JobStatus.code[JobStatus.UNKNOWN]) != -1) {
           //setBackground(Color.lightGray);
           setBackground(new Color(105, 109, 109));
         } else {
           setBackground(Color.white);
         }
       } else {
         setBackground(table.getBackground());
       }
     }
     return this;
   }
 }
