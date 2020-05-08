 package main.java.master.gui;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 
 import main.java.logic.HeuristicFactory;
 import main.java.master.Job;
 import main.java.master.Slave;
 
 public class ProgramWindow extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private JButton abortJobButton = null;
 	private JFrame frame;
 	private JPanel jContentPane = null;
 	private JMenu jMenu = null;
 	private JMenuBar jMenuBar = null;
 	private JPanel jobsActionPanel = null;
 	private JPanel jobsPanel = null;
 	private JScrollPane jobsScrollPane = null;
 	private JTable jobsTable = null;
 	private JTabbedPane jTabbedPane = null;
 	private JButton killSlaveButton = null;
 	private JButton newJobButton = null;
 	private JMenuItem quitMenuItem = null;
 	private JButton showSlaveInfoButton = null;
 	private JPanel slavesActionPanel = null;
 	private JPanel slavesPanel = null;
 	private JScrollPane slavesScrollPane = null;
 	private JTable slavesTable = null;
 	private JButton startJobButton = null;
 	private JButton viewJobButton = null;
 
 	/**
 	 * This is the default constructor
 	 */
 	public ProgramWindow() {
 		super();
 		if (frame == null) {
 			frame = this;
 		}
 		initialize();
 	}
 
 	/**
 	 * This method initializes abortJobButton
 	 * 
 	 * @return javax.swing.JButton
 	 */
 	private JButton getAbortJobButton() {
 		if (abortJobButton == null) {
 			abortJobButton = new JButton();
 			abortJobButton.setText("Abort Job");
 			abortJobButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					abortSelectedJob();
 				}
 			});
 		}
 		return abortJobButton;
 	}
 
 	/**
 	 * This method initializes jContentPane
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJContentPane() {
 		if (jContentPane == null) {
 			jContentPane = new JPanel();
 			jContentPane.setLayout(new BorderLayout());
 			jContentPane.add(getJTabbedPane(), BorderLayout.NORTH);
 		}
 		return jContentPane;
 	}
 
 	/**
 	 * This method initializes jMenu
 	 * 
 	 * @return javax.swing.JMenu
 	 */
 	private JMenu getJMenu() {
 		if (jMenu == null) {
 			jMenu = new JMenu();
 			jMenu.setText("File");
 			jMenu.add(getQuitMenuItem());
 		}
 		return jMenu;
 	}
 
 	/**
 	 * This method initializes jMenuBar
 	 * 
 	 * @return javax.swing.JMenuBar
 	 */
 	@Override
 	public JMenuBar getJMenuBar() {
 		if (jMenuBar == null) {
 			jMenuBar = new JMenuBar();
 			jMenuBar.add(getJMenu());
 		}
 		return jMenuBar;
 	}
 
 	/**
 	 * This method initializes jobsActionPanel
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJobsActionPanel() {
 		if (jobsActionPanel == null) {
 			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
 			gridBagConstraints6.insets = new Insets(0, 0, 0, 0);
 			gridBagConstraints6.gridwidth = 1;
 			gridBagConstraints6.ipadx = 0;
 			gridBagConstraints6.ipady = 0;
 			gridBagConstraints6.fill = GridBagConstraints.NONE;
 			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
 			gridBagConstraints5.gridx = 3;
 			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
 			gridBagConstraints3.gridx = 7;
 			gridBagConstraints3.gridy = 0;
 			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
 			gridBagConstraints2.gridx = 8;
 			gridBagConstraints2.gridy = 0;
 			jobsActionPanel = new JPanel();
 			jobsActionPanel.setLayout(new GridBagLayout());
 			// jobsActionPanel.setSize(new Dimension(200, 100));
 			jobsActionPanel.add(getNewJobButton(), gridBagConstraints6);
 			jobsActionPanel.add(getViewJobButton(), gridBagConstraints5);
 			jobsActionPanel.add(getStartJobButton(), gridBagConstraints3);
 			jobsActionPanel.add(getAbortJobButton(), gridBagConstraints2);
 		}
 		return jobsActionPanel;
 	}
 
 	/**
 	 * This method initializes jobsPanel
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJobsPanel() {
 		if (jobsPanel == null) {
 			jobsPanel = new JPanel();
 			jobsPanel.setLayout(new BorderLayout());
 			jobsPanel.add(getJobsScrollPane(), BorderLayout.CENTER);
 			jobsPanel.add(getJobsActionPanel(), BorderLayout.SOUTH);
 		}
 		return jobsPanel;
 	}
 
 	/**
 	 * This method initializes jobsScrollPane
 	 * 
 	 * @return javax.swing.JScrollPane
 	 */
 	private JScrollPane getJobsScrollPane() {
 		if (jobsScrollPane == null) {
 			jobsScrollPane = new JScrollPane();
 			jobsScrollPane.setViewportView(getJobsTable());
 		}
 		return jobsScrollPane;
 	}
 
 	/**
 	 * This method initializes jobsTable
 	 * 
 	 * @return javax.swing.JTable
 	 */
 	private JTable getJobsTable() {
 		if (jobsTable == null) {
 			jobsTable = new JTable();
 			jobsTable.setFillsViewportHeight(true);
 			JobsTableModel model = new JobsTableModel();
 			jobsTable.setModel(model);
 			jobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			Job.setTableModel(model);
 		}
 		return jobsTable;
 	}
 
 	/**
 	 * This method initializes jTabbedPane
 	 * 
 	 * @return javax.swing.JTabbedPane
 	 */
 	private JTabbedPane getJTabbedPane() {
 		if (jTabbedPane == null) {
 			jTabbedPane = new JTabbedPane();
 			jTabbedPane.addTab("Jobs", null, getJobsPanel(), null);
 			jTabbedPane.addTab("Slaves", null, getSlavesPanel(), null);
 		}
 		return jTabbedPane;
 	}
 
 	/**
 	 * This method initializes killSlaveButton
 	 * 
 	 * @return javax.swing.JButton
 	 */
 	private JButton getKillSlaveButton() {
 		if (killSlaveButton == null) {
 			killSlaveButton = new JButton();
 			killSlaveButton.setText("Kill Slave");
 			killSlaveButton
 					.addActionListener(new java.awt.event.ActionListener() {
 						public void actionPerformed(java.awt.event.ActionEvent e) {
 							killSelectedSlave();
 						}
 					});
 		}
 		return killSlaveButton;
 	}
 
 	/**
 	 * This method initializes newJobButton
 	 * 
 	 * @return javax.swing.JButton
 	 */
 	private JButton getNewJobButton() {
 		if (newJobButton == null) {
 			newJobButton = new JButton();
 			newJobButton.setText("New Job");
 			newJobButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					Set<String> solvers = Slave.getAllAvaliableSolverIds();
 					Vector<String> heuristics = HeuristicFactory
 							.getAvailableHeuristics();
 					CreateJobDialog dialog = new CreateJobDialog(frame, solvers
 							.toArray(new String[solvers.size()]), heuristics
 							.toArray(new String[heuristics.size()]));
 					dialog.setVisible(true);
 				}
 			});
 		}
 		return newJobButton;
 	}
 
 	/**
 	 * This method initializes quitMenuItem
 	 * 
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getQuitMenuItem() {
 		if (quitMenuItem == null) {
 			quitMenuItem = new JMenuItem();
 			quitMenuItem.setText("Quit");
 		}
 		return quitMenuItem;
 	}
 
 	/**
 	 * This method initializes showSlaveInfoButton
 	 * 
 	 * @return javax.swing.JButton
 	 */
 	private JButton getShowSlaveInfoButton() {
 		if (showSlaveInfoButton == null) {
 			showSlaveInfoButton = new JButton();
 			showSlaveInfoButton.setText("Show Slave Info");
 		}
 		return showSlaveInfoButton;
 	}
 
 	/**
 	 * This method initializes slavesActionPanel
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getSlavesActionPanel() {
 		if (slavesActionPanel == null) {
 			slavesActionPanel = new JPanel();
 			slavesActionPanel.setLayout(new GridBagLayout());
 			slavesActionPanel.add(getKillSlaveButton(),
 					new GridBagConstraints());
 			slavesActionPanel.add(getShowSlaveInfoButton(),
 					new GridBagConstraints());
 		}
 		return slavesActionPanel;
 	}
 
 	/**
 	 * This method initializes slavesPanel
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getSlavesPanel() {
 		if (slavesPanel == null) {
 			slavesPanel = new JPanel();
 			slavesPanel.setLayout(new BorderLayout());
 			slavesPanel.add(getSlavesScrollPane(), BorderLayout.NORTH);
 			slavesPanel.add(getSlavesActionPanel(), BorderLayout.SOUTH);
 		}
 		return slavesPanel;
 	}
 
 	/**
 	 * This method initializes slavesScrollPane
 	 * 
 	 * @return javax.swing.JScrollPane
 	 */
 	private JScrollPane getSlavesScrollPane() {
 		if (slavesScrollPane == null) {
 			slavesScrollPane = new JScrollPane();
 			slavesScrollPane.setViewportView(getSlavesTable());
 		}
 		return slavesScrollPane;
 	}
 
 	/**
 	 * This method initializes slavesTable
 	 * 
 	 * @return javax.swing.JTable
 	 */
 	private JTable getSlavesTable() {
 		if (slavesTable == null) {
 			slavesTable = new JTable();
 			SlavesTableModel model = new SlavesTableModel();
 			slavesTable.setModel(model);
 			slavesTable.setFillsViewportHeight(true);
 			slavesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			Slave.setTableModel(model);
 		}
 		return slavesTable;
 	}
 
 	/**
 	 * This method initializes startJobButton
 	 * 
 	 * @return javax.swing.JButton
 	 */
 	private JButton getStartJobButton() {
 		if (startJobButton == null) {
 			startJobButton = new JButton();
 			startJobButton.setText("Start Job");
 			startJobButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					startSelectedJob();
 				}
 			});
 		}
 		return startJobButton;
 	}
 
 	private void abortSelectedJob() { 
 		int row = getJobsTable().getSelectedRow(); 
		if(row != -1) {
			Job job = Job.getJobs().get(row);
			job.abort(); 
		}
 	}
 	
	private void startSelectedJob() {
 		int row = getJobsTable().getSelectedRow(); 
		if(row != -1) {
			Job job = Job.getJobs().get(row);
			job.start();
		}
 	}	
 
 	/**
 	 * This method initializes viewJobButton
 	 * 
 	 * @return javax.swing.JButton
 	 */
 	private JButton getViewJobButton() {
 		if (viewJobButton == null) {
 			viewJobButton = new JButton();
 			viewJobButton.setText("View Job");
 			viewJobButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					
 				}
 			});
 		}
 		return viewJobButton;
 	}
 
 	/**
 	 * This method initializes this
 	 * 
 	 * @return void
 	 */
 	private void initialize() {
 		// this.setSize(658, 515);
 		this.setJMenuBar(getJMenuBar());
 		this.setContentPane(getJContentPane());
 		this.setTitle("Qpar");
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.pack();
 	}
 
 	private void killSelectedSlave() {
 		int row = getSlavesTable().getSelectedRow();
 		Slave slave = Slave.getSlaves().get(row);
 		slave.kill("User command");
 	}
 
 }
