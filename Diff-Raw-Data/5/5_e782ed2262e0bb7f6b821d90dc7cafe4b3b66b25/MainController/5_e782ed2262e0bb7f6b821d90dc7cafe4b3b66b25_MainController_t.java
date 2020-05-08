 package controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 
 import model.CompetitionLogging;
 import model.ConfigFile;
 import model.Logging;
 import model.Map;
 import model.TaskServer;
 import model.TaskSpec;
 import model.TaskTimer;
 import model.TaskTriplet;
 import model.ValidTripletElements;
 import view.DialogType;
 import view.FileType;
 import view.MainGUI;
 
 /**
  * @author BRSU-MAS-ASTT-SoSe2012
  * 
  */
 public class MainController implements TimerListener {
 	private MainGUI mG;
 	private TaskSpec tS;
 	private ConfigFile cfgFile;
 	private TaskServer tServer;
 	private Logging logg;
 	private String triplets = "Triplets";
 	private boolean unsavedChanges = false;
 	private boolean competitionMode = false;
 	private TaskTimer taskTimer;
 	private CompetitionLogging compLogging;
 	private final String unsavedWarningMsg = "Warning: Unsaved data will be lost. Proceed? ";
 	private final String exitNotAllowedMsg = "System is in Competition Mode. To exit, press Competition Finished button.";
 
 	private WindowAdapter windowAdapter = new WindowAdapter() {
 		public void windowClosing(WindowEvent evt) {
 			// redirect to the exit action from file menu
 			exit.actionPerformed(null);
 		}
 	};
 
 	private Action save = new AbstractAction("Save") {
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent arg0) {
 
 			File file = mG.showFolderDialog(FileType.FILETYPE_TSP, DialogType.DIALOG_SAVE);
 			if (file != null) {
 				if (tS.saveTaskSpec(file)
 						&& Map.saveTaskSpecMap(file, mG.getMapArea())) {
 					mG.setStatusLine("saved actual task specification in >"
 							+ file.getName() + "<");
 					setSavedChanges();
 					CompetitionLogging.setFilePath(file);
 				} else {
 					mG.setStatusLine("<html><FONT COLOR=RED>Something went wrong!"
 							+ "</FONT> No map saved </html>");
 				}
 			} else {
 				mG.setStatusLine("save command cancelled by user");
 			}
 		}
 	};
 
 	private Action open = new AbstractAction("Open") {
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent arg0) {
 			if (unsavedChanges && warningIgnored("Open")) {
 				return;
 			}
 			File file = mG.showFolderDialog(FileType.FILETYPE_TSP, DialogType.DIALOG_OPEN);
 			if (file != null) {
 
 				if (tS.openTaskSpec(file)) {
 					mG.setStatusLine("Opened task specification >"
 							+ file.getName() + "<");
 					setSavedChanges();
 					CompetitionLogging.setFilePath(file);
 				} else {
 					mG.setStatusLine("<html><FONT COLOR=RED>Something went wrong!"
 							+ "</FONT> No task spec file opened </html>");
 				}
 			} else {
 				mG.setStatusLine("Open command cancelled by user.");
 			}
 		}
 	};
 
 	private Action loadConfig = new AbstractAction("Load Config") {
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent arg0) {
 			if (unsavedChanges && warningIgnored("Load Config")) {
 				return;
 			}
 			File file = mG.showFolderDialog(FileType.FILETYPE_ALL,  DialogType.DIALOG_OPEN);
 			if (file != null) {
 				loadConfigurationFile(file);
 			} else {
 				mG.setStatusLine("Load Config command cancelled by user.");
 			}
 		}
 	};
 
 	private Action exit = new AbstractAction("Exit") {
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent arg0) {
 			if (competitionMode) {
 				mG.showMessageDialog(exitNotAllowedMsg, "Competition Running");
 				return;
 			}
 			if (unsavedChanges && warningIgnored("Exit")) {
 				return;
 			}
 			System.exit(0);
 		}
 	};
 
 	private Action upTriplet = new AbstractAction("Triplet up") {
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent arg0) {
 
 			if (mG.getTripletsTable().getSelectedRowCount() > 0) {
 				int pos = mG.getTripletsTable().getSelectedRow();
 				TaskTriplet t = tS.moveUpTriplet(pos);
 				if (t != null) {
 					mG.getTripletsTable().changeSelection(pos - 1, -1, false,
 							false);
 					mG.setStatusLine("Triplet (" + t.getPlace() + ", "
 							+ t.getOrientation() + ", " + t.getPause()
 							+ ") moved up.");
 					unsavedChanges = true;
 				} else
 					mG.setStatusLine("Triplet already at the beginning of the list.");
 			} else {
 				mG.setStatusLine("No triplet selected for moving up.");
 			}
 		}
 	};
 
 	private Action downTriplet = new AbstractAction("Triplet down") {
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent arg0) {
 
 			if (mG.getTripletsTable().getSelectedRowCount() > 0) {
 				int pos = mG.getTripletsTable().getSelectedRow();
 				TaskTriplet t = tS.moveDownTriplet(pos);
 				if (t != null) {
 					mG.getTripletsTable().changeSelection(pos + 1, -1, false,
 							false);
 					mG.setStatusLine("Triplet (" + t.getPlace() + ", "
 							+ t.getOrientation() + ", " + t.getPause()
 							+ ") moved down.");
 					unsavedChanges = true;
 				} else
 					mG.setStatusLine("Triplet already at the end of the list.");
 			} else {
 				mG.setStatusLine("No triplet selected for moving down.");
 			}
 		}
 	};
 
 	private Action updateTriplet = new AbstractAction("Update Triplet") {
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent arg0) {
 
 			if (mG.getTripletsTable().getSelectedRowCount() > 0) {
 				int pos = mG.getTripletsTable().getSelectedRow();
 				TaskTriplet tt = new TaskTriplet();
 				tt.setPlace((String) mG.getPlacesBox().getSelectedItem());
 				tt.setOrientation((String) mG.getOrientationsBox()
 						.getSelectedItem());
 				if (tt.setPause((String) mG.getPausesBox().getSelectedItem()
 						.toString())) {
 					TaskTriplet t = tS.editTriplet(pos, tt);
 					if (t != null) {
 						mG.setStatusLine("Updated Triplet (" + t.getPlace()
 								+ ", " + t.getOrientation() + ", "
 								+ t.getPause() + ").");
 						setUnsavedChanges();
 					} else
 						mG.setStatusLine("Triplet could not be updated.");
 				}
 			} else {
 				mG.setStatusLine("No triplet selected for updating.");
 			}
 		}
 	};
 
 	private Action addTriplet = new AbstractAction("Add Triplet") {
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent arg0) {
 
 			TaskTriplet t = new TaskTriplet();
 			t.setPlace((String) mG.getPlacesBox().getSelectedItem());
 			t.setOrientation((String) mG.getOrientationsBox().getSelectedItem());
 			if (t.setPause((String) mG.getPausesBox().getSelectedItem()
 					.toString())) {
 				tS.addTriplet(t);
 				mG.setStatusLine("added triplet (" + t.getPlace() + ", "
 						+ t.getOrientation() + ", " + t.getPause() + ")");
 				unsavedChanges = true;
 			} else {
 				mG.setStatusLine("error triplet");
 			}
 		}
 	};
 
 	private Action deleteTriplet = new AbstractAction("Delete Triplet") {
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent arg0) {
 
 			if (mG.getTripletsTable().getSelectedRowCount() > 0) {
 				int pos = mG.getTripletsTable().getSelectedRow();
 				String msg = "Do you want to delete the triplet "
 						+ tS.getTaskTripletList().get(pos)
 								.getTaskTripletString() + "?";
 				if (mG.getUserConfirmation(msg, "Confirm Triplet Deletion") == 0) {
 					TaskTriplet t = tS.deleteTriplet(pos);
 					mG.setStatusLine("Deleted triplet (" + t.getPlace() + ", "
 							+ t.getOrientation() + ", " + t.getPause() + ")");
 					setUnsavedChanges();
 				} else {
 					mG.setStatusLine("Triplet not deleted.");
 				}
 			} else {
 				mG.setStatusLine("No triplet selected for deletion.");
 			}
 		}
 	};
 
 	private Action sendTriplets = new AbstractAction("Send Triplets") {
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent arg0) {
 			tServer.sendTaskSpecToClient(tS);
 			mG.setStatusLine("Task specification sent to the team.");
 		}
 	};
 
 	private Action disconnect = new AbstractAction("Disconnect") {
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent arg0) {
 			String teamName = tServer.getTeamName();
 			tServer.restartServer();
 			mG.setStatusLine("Team " + teamName + " disconnected. Listening for new connection.....");
 		}
 	};
 
 	private Action help = new AbstractAction("Help") {
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent arg0) {
 			mG.setStatusLine("<html><FONT COLOR=RED>not implemented yet! </FONT> Sorry, no help available </html>");
 		}
 	};
 
 	public ItemListener timerListener = new ItemListener() {
 		public void itemStateChanged(ItemEvent evt) {
 			if (evt.getStateChange() == ItemEvent.SELECTED) {
 				long configTime = cfgFile.getConfigurationTime();
 				long runTime = cfgFile.getRunTime();
 				taskTimer.startNewTimer(configTime, runTime);
				setEnteredCompetitionMode(true);
 				CompetitionLogging.setTaskTripletListLength(tS);
 				tS.addTripletListener(compLogging);
 			} else {
 				taskTimer.stopTimer();
 				tServer.disconnectClient();
 				mG.setTimerStartStopButtonText("Timer Start");
 				mG.getTimerStartStopButton().setEnabled(false);
 				mG.getCompetitionFinishedButton().setEnabled(true);
 				mG.getSendTripletsButton().setEnabled(false);
 			}
 		}
 	};
 
 	public Action competitionFinished = new AbstractAction(
 			"competitionFinished") {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 				String teamName = tServer.getTeamName();
 				CompetitionLogging.setTeamName(teamName);
 				CompetitionLogging.storeParams();
 				CompetitionLogging.resetParams();
 				mG.setCompetitionMode(false);
 				mG.getCompetitionFinishedButton().setEnabled(false);
 				setEnteredCompetitionMode(false);
 				taskTimer.resetTimer();
 				mG.getTimerStartStopButton().setEnabled(true);
 				tS.resetStates();
 				tServer.listenForConnection();
 				mG.setStatusLine("Competition finished. Listening for next team.");
 			}
 	};
 
 	public MouseListener tripletTableListener = new MouseListener() {
 
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			int selectedRow = mG.getTripletsTable().getSelectedRow();
 			if (selectedRow >= 0) {
 				TaskTriplet tT = tS.getTaskTripletAtIndex(selectedRow);
 				mG.setPlacesBoxSelected(tT.getPlace());
 				mG.setOrientationsBoxSelected(tT.getOrientation());
 				mG.setPausesBoxSelected(tT.getPause());
 				mG.setStatusLine("Selected triplet (" + tT.getPlace() + ", "
 						+ tT.getOrientation() + ", " + tT.getPause() + ").");
 			}
 
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mouseExited(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mousePressed(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent arg0) {
 			int selectedRow = mG.getTripletsTable().getSelectedRow();
 			int selectedColumn = mG.getTripletsTable().getSelectedColumn();
 			if (selectedColumn > 0) {
 				TaskTriplet tT = tS
 						.setTripletState(selectedRow, selectedColumn);
 				mG.setStatusLine("Updated triplet state (" + tT.getPlace()
 						+ ", " + tT.getOrientation() + ", " + tT.getPause()
 						+ ").");
 				mG.setTableCellCorrected(selectedRow, selectedColumn);
 				mG.getTripletsTable().clearSelection();
 			}
 		}
 	};
 
 	public MainController(String[] args) {
 		tS = new TaskSpec();
 		mG = new MainGUI();
 		cfgFile = new ConfigFile();
 		tServer = new TaskServer();
 		taskTimer = new TaskTimer();
 		triplets = "Triplets";
 		unsavedChanges = false;
 		competitionMode = false;
 		logg = Logging.getInstance();
 		compLogging = new CompetitionLogging();
 		init();
 		if (args.length > 0) {
 			File file = new File(System.getProperty("user.home")
 					+ System.getProperty("file.separator") + args[0]);
 			loadConfigurationFile(file);
 		}
 	}
 
 	private void init() {
 		save.putValue(
 				Action.SMALL_ICON,
 				new ImageIcon(getClass().getResource(
 						"/view/resources/icons/Save16.gif")));
 		open.putValue(
 				Action.SMALL_ICON,
 				new ImageIcon(getClass().getResource(
 						"/view/resources/icons/Open16.gif")));
 		loadConfig.putValue(Action.SMALL_ICON, new ImageIcon(getClass()
 				.getResource("/view/resources/icons/Import16.gif")));
 		exit.putValue(
 				Action.SMALL_ICON,
 				new ImageIcon(getClass().getResource(
 						"/view/resources/icons/exit.png")));
 		help.putValue(
 				Action.SMALL_ICON,
 				new ImageIcon(getClass().getResource(
 						"/view/resources/icons/Help16.gif")));
 		upTriplet.putValue(Action.SMALL_ICON, new ImageIcon(getClass()
 				.getResource("/view/resources/icons/Up16.gif")));
 		downTriplet.putValue(Action.SMALL_ICON, new ImageIcon(getClass()
 				.getResource("/view/resources/icons/Down16.gif")));
 		updateTriplet.putValue(Action.SMALL_ICON, new ImageIcon(getClass()
 				.getResource("/view/resources/icons/Edit16.gif")));
 		addTriplet.putValue(Action.SMALL_ICON, new ImageIcon(getClass()
 				.getResource("/view/resources/icons/Back16.gif")));
 		deleteTriplet.putValue(Action.SMALL_ICON, new ImageIcon(getClass()
 				.getResource("/view/resources/icons/Delete16.gif")));
 		sendTriplets.putValue(Action.SMALL_ICON, new ImageIcon(getClass()
 				.getResource("/view/resources/icons/Export16.gif")));
 		mG.connectSaveAction(save);
 		mG.connectOpenAction(open);
 		mG.connectLoadConfigAction(loadConfig);
 		mG.connectExitAction(exit);
 		mG.connectHelpAction(help);
 		mG.connectSendTriplets(sendTriplets);
 		mG.connectAddTriplet(addTriplet);
 		mG.connectDownTriplet(downTriplet);
 		mG.connectUpTriplet(upTriplet);
 		mG.connectEditTriplet(updateTriplet);
 		mG.connectDeleteTriplet(deleteTriplet);
 		mG.connectDisconnet(disconnect);
 		mG.connectCompetitionFinished(competitionFinished);
 		mG.setButtonDimension();
 		tS.addTripletListener(mG);
 		tS.addTripletListener(mG.getMapArea());
 		tServer.addConnectionListener(mG);
 		tServer.listenForConnection();
 		mG.addWindowListener(windowAdapter);
 		mG.addtripletTableListener(tripletTableListener);
 		mG.addTimerListener(timerListener);
 		taskTimer.addTimerListener(mG);
 		taskTimer.addTimerListener(this);
 		tServer.addConnectionListener(taskTimer);
 		mG.pack();
 	}
 
 	private void initializeValidTriplets() {
 		ValidTripletElements vte = ValidTripletElements.getInstance();
 		try {
 			if (vte.readFromConfigFile(cfgFile)) {
 				mG.setValidPositions(vte.getValidPositions());
 				mG.setValidOrientations(vte.getValidOrientations());
 				mG.setValidPauses(vte.getValidPauses());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private boolean initializeBackgroundMap(String pathPrefix) {
 		BufferedImage map = Map.loadBackgroundMap(cfgFile, pathPrefix);
 		if (map != null) {
 			mG.getMapArea().setBackgroundMap(map);
 			return true;
 		}
 		return false;
 	}
 
 	public void showView() {
 		mG.setVisible(true);
 	}
 
 	private boolean warningIgnored(String actionName) {
 		return (mG.getUserConfirmation(unsavedWarningMsg, "Confirm "
 				+ actionName) != 0);
 	}
 
 	private void setUnsavedChanges() {
 		unsavedChanges = true;
 		mG.getTimerStartStopButton().setEnabled(false);
 	}
 
 	private void setSavedChanges() {
 		unsavedChanges = false;
 		mG.getTimerStartStopButton().setEnabled(true);
 	}
 
	private void setEnteredCompetitionMode(boolean mode) {
 		competitionMode = mode;
 		mG.setCompetitionMode(mode);
 	}
 
 	private void loadConfigurationFile(File file) {
 		if (cfgFile.setConfigFile(file)) {
 			try {
 				cfgFile.loadProperties();
 			} catch (Exception e) {
 				System.out
 						.println("Exception while loading config properties: "
 								+ e);
 			}
 
 			initializeValidTriplets();
 			if (initializeBackgroundMap(file.getParent())) {
 				mG.pack();
 				mG.configFileLoaded();
 				mG.setStatusLine("Loaded configuration file >" + file.getName()
 						+ "<");
 				// not the right place
 				logg.LoggingFile(triplets,
 						"Loaded configuration file >" + file.getName() + "<");
 			} else {
 				mG.setStatusLine("<html><FONT COLOR=RED>Something went wrong!"
 						+ "</FONT> No background file loaded. </html>");
 			}
 		} else {
 			mG.setStatusLine("<html><FONT COLOR=RED>Something went wrong!"
 					+ "</FONT> No config file loaded. </html>");
 		}
 	}
 
 	@Override
 	public void timerTick(String currentTime, boolean inTime) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void timerReset(String resetTime) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void timerSetMaximumTime(String maxTime) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void timerOverrun() {
 		mG.setTimerStartStopButtonText("Timer Start");
 		mG.getTimerStartStopButton().setSelected(false);
 		mG.getCompetitionFinishedButton().setEnabled(true);
 		tServer.disconnectClient();
 		mG.setStatusLine("Timeout. Team disconnected");
 	}
 }
