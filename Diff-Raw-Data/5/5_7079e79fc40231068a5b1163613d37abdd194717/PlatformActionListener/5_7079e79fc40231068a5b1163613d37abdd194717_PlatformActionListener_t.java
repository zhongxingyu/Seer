 package org.bh.platform;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.WindowConstants;
 import javax.swing.tree.TreePath;
 
 import org.apache.log4j.Logger;
 import org.bh.controller.IDataExchangeController;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOProject;
 import org.bh.data.DTOScenario;
 import org.bh.data.IDTO;
 import org.bh.data.types.StringValue;
 import org.bh.gui.swing.BHComboBox;
 import org.bh.gui.swing.BHContent;
 import org.bh.gui.swing.BHDataExchangeDialog;
 import org.bh.gui.swing.BHMainFrame;
 import org.bh.gui.swing.BHOptionDialog;
 import org.bh.gui.swing.BHStatusBar;
 import org.bh.gui.swing.BHTreeNode;
 import org.bh.gui.swing.IBHAction;
 import org.bh.platform.PlatformController.BHTreeModel;
 import org.bh.platform.i18n.BHTranslator;
import org.bh.platform.i18n.ITranslator;
 
 /**
  * The PlatformActionListener handles all actions that are fired by a button
  * etc. of the platform.
  */
 class PlatformActionListener implements ActionListener {
 
 	private static final Logger log = Logger
 			.getLogger(PlatformActionListener.class);
 
 	BHMainFrame bhmf;
 	ProjectRepositoryManager projectRepoManager;
 	PlatformController pC;
 
 	IDataExchangeController dataExchangeCntrl;
 
 	public PlatformActionListener(BHMainFrame bhmf,
 			ProjectRepositoryManager projectRepoManager,
 			PlatformController platformController) {
 		this.bhmf = bhmf;
 		this.projectRepoManager = projectRepoManager;
 		this.pC = platformController;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent aEvent) {
 
 		// get actionKey of fired action
 		PlatformKey actionKey = ((IBHAction) aEvent.getSource())
 				.getPlatformKey();
 
 		// do right action...
 		switch (actionKey) {
 
 		/*
 		 * Clear current workspace
 		 * 
 		 * @author Michael Löckelt
 		 */
 		case FILENEW:
 			log.debug("handling FILENEW event");
 			this.fileNew();
 			break;
 
 		/*
 		 * Open a workspace
 		 * 
 		 * @author Michael Löckelt
 		 */
 		case FILEOPEN:
 			log.debug("handling FILEOPEN event");
 			this.fileOpen();
 			break;
 
 		/*
 		 * Save the whole workspace using the already defined filepath or ask
 		 * for new path
 		 * 
 		 * @author Michael Löckelt
 		 */
 		case FILESAVE:
 			log.debug("handling FILESAVE event");
 			Services.firePlatformEvent(new PlatformEvent(
 					PlatformActionListener.class, PlatformEvent.Type.SAVE));
 			break;
 
 		/*
 		 * Save the whole workspace - incl. filepath save dialog
 		 * 
 		 * @author Michael Löckelt
 		 */
 		case FILESAVEAS:
 			log.debug("handling FILESAVEAS event");
 			Services.firePlatformEvent(new PlatformEvent(
 					PlatformActionListener.class, PlatformEvent.Type.SAVEAS));
 			break;
 
 		/*
 		 * Clear workspace - same like filenew
 		 * 
 		 * @author Michael Löckelt
 		 */
 		case FILECLOSE:
 			log.debug("handling FILECLOSE event");
 			this.fileNew();
 			break;
 
 		case FILEQUIT:
 			log.debug("handling FILEQUIT event");
 			bhmf.dispose();
 			break;
 
 		case PROJECTCREATE:
 			this.createProject();
 			break;
 
 		case PROJECTDUPLICATE:
 			this.duplicateProject();
 			break;
 
 		// TODO Katzor.Marcus
 		case PROJECTIMPORT:
 
 			final String projectImportPanelKey = "DXMLProjectImport";
 
 			// Get data exchange controller
 			dataExchangeCntrl = Services.getDataExchangeController("XML");
 			BHDataExchangeDialog importDialog = new BHDataExchangeDialog(bhmf,
 					true);
 			importDialog.setTitle(BHTranslator.getInstance().translate(
 					projectImportPanelKey));
 			importDialog.add(dataExchangeCntrl.getImportPanel(
 					projectImportPanelKey, importDialog), BorderLayout.CENTER);
 			importDialog.setVisible(true);
 
 			break;
 
 		// TODO Katzor.Marcus
 
 		case PROJECTEXPORT:
 
 			final String projectExportPanelKey = "DXMLProjectExport";
 
 			if (bhmf.getBHTree().getSelectionPath() != null) {
 				BHTreeNode selectedNode = (BHTreeNode) bhmf.getBHTree()
 						.getSelectionPath().getLastPathComponent();
 				if (selectedNode.getUserObject() instanceof DTOProject) {
 					// Get data exchange controller
 					/*
 					 * dataExchangeCntrl =
 					 * Services.getDataExchangeController("XML");
 					 * dataExchangeCntrl.setModel((IDTO<?>)
 					 * selectedNode.getUserObject());
 					 * BHProjectDataExchangeDialog exportDialog = new
 					 * BHProjectDataExchangeDialog(bhmf, true);
 					 * exportDialog.setTitle
 					 * (BHTranslator.getInstance().translate
 					 * (projectExportPanelKey));
 					 * exportDialog.add(dataExchangeCntrl
 					 * .getExportPanel(projectExportPanelKey, exportDialog),
 					 * BorderLayout.CENTER); exportDialog.setVisible(true);
 					 */
 
 					BHDataExchangeDialog dialog = new BHDataExchangeDialog(
 							bhmf, true);
 					dialog.setAction(IImportExport.EXP_PROJECT);
 					dialog.setModel((IDTO<?>) selectedNode.getUserObject());
 					
 					//dialog.setDescription("This is just a test");
 					dialog.setVisible(true);
 
 				} else {
 					// TODO Katzor.Marcus Show Message
 				}
 			} else {
 				// TODO Katzor.Marcus Show Message
 			}
 
 			break;
 
 		case PROJECTREMOVE:
 			TreePath currentRemoveProjectSelection = bhmf.getBHTree()
 					.getSelectionPath();
 			if (currentRemoveProjectSelection != null) {
 				BHTreeNode removeProjectNode = (BHTreeNode) bhmf.getBHTree()
 						.getSelectionPath().getLastPathComponent();
 				if (removeProjectNode.getUserObject() instanceof DTOProject) {
 					((BHTreeModel) bhmf.getBHTree().getModel())
 							.removeNodeFromParent(removeProjectNode);
 					projectRepoManager
 							.removeProject((DTOProject) removeProjectNode
 									.getUserObject());
 				} else {
 					BHStatusBar.getInstance().setHint(
 							BHTranslator.getInstance().translate(
 									"EisSelectProject"), true);
 				}
 			}
 			break;
 
 		case SCENARIOCREATE:
 			this.createScenario();
 			break;
 
 		case SCENARIODUPLICATE:
 			this.duplicateScenario();
 			break;
 
 		case SCENARIOMOVE:
 			// TODO Drag&Drop
 
 			break;
 
 		case SCENARIOREMOVE:
 			TreePath currentRemoveScenarioSelection = bhmf.getBHTree()
 					.getSelectionPath();
 			if (currentRemoveScenarioSelection != null) {
 				BHTreeNode removeScenarioNode = (BHTreeNode) bhmf.getBHTree()
 						.getSelectionPath().getLastPathComponent();
 				if (removeScenarioNode.getUserObject() instanceof DTOScenario) {
 					((BHTreeModel) bhmf.getBHTree().getModel())
 							.removeNodeFromParent(removeScenarioNode);
 					((DTOScenario) ((BHTreeNode) removeScenarioNode.getParent())
 							.getUserObject())
 							.removeChild((DTOPeriod) removeScenarioNode
 									.getUserObject());
 				} else {
 					BHStatusBar.getInstance().setHint(
 							BHTranslator.getInstance().translate(
 									"EisSelectScenario"), true);
 				}
 			}
 			break;
 
 		case BILANZGUVSHOW:
 			// TODO Prüfen und ggf. implementieren!
 			break;
 
 		case BILANZGUVCREATE:
 			// TODO Prüfen und ggf. implementieren!
 			break;
 
 		case BILANZGUVIMPORT:
 			// TODO Prüfen und ggf. implementieren!
 			break;
 
 		case BILANZGUVREMOVE:
 			// TODO Prüfen und ggf. implementieren!
 			break;
 
 		case OPTIONSCHANGE:
 			new BHOptionDialog();
 
 			break;
 
 		case HELPUSERHELP:
 
 			openUserHelp("userhelp");
 
 		case HELPMATHHELP:
 
 			openUserHelp("mathhelp");
 
 		case HELPINFO:
 			// TODO Prüfen und ggf. implementieren!
 			break;
 
 		case TOOLBAROPEN:
 			log.debug("handling TOOLBAROPEN event");
 			this.fileOpen();
 			break;
 
 		case TOOLBARSAVE:
 			log.debug("handling TOOLBARSAVE event");
 			Services.firePlatformEvent(new PlatformEvent(
 					PlatformActionListener.class, PlatformEvent.Type.SAVE));
 			break;
 
 		case TOOLBARADDPRO:
 			this.createProject();
 			break;
 
 		case TOOLBARADDS:
 			this.createScenario();
 			break;
 
 		case TOOLBARADDPER:
 			this.createPeriod();
 			break;
 
 		case TOOLBARREMOVE:
 
 			TreePath currentSelection = bhmf.getBHTree().getSelectionPath();
 			// is a node selected?
 			if (currentSelection != null) {
 				// find out current selected node
 				BHTreeNode currentNode = (BHTreeNode) bhmf.getBHTree()
 						.getSelectionPath().getLastPathComponent();
 
 				// remove node from data model...
 				if (currentNode.getUserObject() instanceof DTOProject) {
 					projectRepoManager.removeProject((DTOProject) currentNode
 							.getUserObject());
 
 				} else if (currentNode.getUserObject() instanceof DTOScenario) {
 					((DTOProject) ((BHTreeNode) currentNode.getParent())
 							.getUserObject())
 							.removeChild((DTOScenario) currentNode
 									.getUserObject());
 
 				} else if (currentNode.getUserObject() instanceof DTOPeriod) {
 					((DTOScenario) ((BHTreeNode) currentNode.getParent())
 							.getUserObject())
 							.removeChild((DTOPeriod) currentNode
 									.getUserObject());
 				}
 
 				// ... and from GUI and select other node or empty screen
 				TreePath tp = new TreePath(currentNode.getPreviousNode()
 						.getPath());
 				bhmf.getBHTree().setSelectionPath(tp);
 				if (bhmf.getBHTree().getSelectionPath().getPathCount() == 1)
 					bhmf.setContentForm(new BHContent());
 
 				((BHTreeModel) bhmf.getBHTree().getModel())
 						.removeNodeFromParent(currentNode);
 
 			}
 
 			break;
 
 		default:
 			// TODO implementieren?
 			break;
 		}
 	}
 
 	/*
 	 * new file
 	 * 
 	 * @author Loeckelt.Michael
 	 */
 	private void fileNew() {
 		if (ProjectRepositoryManager.isChanged()) {
 
 			int i = JOptionPane.showConfirmDialog(bhmf, Services
					.getTranslator().translate("Psave", ITranslator.LONG), Services
					.getTranslator().translate("Psave"), JOptionPane.YES_NO_CANCEL_OPTION);
 
 			if (i == JOptionPane.YES_OPTION || i == JOptionPane.NO_OPTION) {
 				if (i == JOptionPane.YES_OPTION)
 					Services.firePlatformEvent(new PlatformEvent(
 							BHMainFrame.class, PlatformEvent.Type.SAVE));
 
 				if (i == JOptionPane.NO_OPTION)
 					Logger
 							.getLogger(getClass())
 							.debug(
 									"Existing changes but no save wish - clear project list");
 
 				projectRepoManager.clearProjectList();
 				pC.setupTree(bhmf, projectRepoManager);
 				PlatformController.preferences.remove("path");
 
 				ProjectRepositoryManager.setChanged(false);
 				bhmf.resetTitle();
 
 			} else if (i == JOptionPane.CANCEL_OPTION) {
 
 			}
 
 		} else {
 			Logger.getLogger(getClass()).debug(
 					"No changes - clear project list");
 			projectRepoManager.clearProjectList();
 			pC.setupTree(bhmf, projectRepoManager);
 			PlatformController.preferences.remove("path");
 
 			ProjectRepositoryManager.setChanged(false);
 			bhmf.resetTitle();
 		}
 
 	}
 
 	/*
 	 * new file
 	 * 
 	 * @author Loeckelt.Michael
 	 */
 	protected void fileOpen() {
 		// create a open-dialog
 		int returnVal = bhmf.getChooser().showOpenDialog(bhmf);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			log.debug("You chose to open this file: "
 					+ bhmf.getChooser().getSelectedFile().getName());
 
 			// open already provided file
 			PlatformController.platformPersistenceManager.openFile(bhmf
 					.getChooser().getSelectedFile());
 
 			// rebuild Tree
 			pC.setupTree(bhmf, projectRepoManager);
 
 			bhmf.getBHTree().expandAll();
 		}
 	}
 
 	protected void createProject() {
 		// Create new project
 		DTOProject newProject = new DTOProject();
 		// TODO hardgecodeder String raus! AS
 		newProject.put(DTOProject.Key.NAME, new StringValue("neues Projekt"));
 		// add it to DTO-Repository and Tree
 		PlatformController.getInstance().addProject(newProject);
 
 	}
 
 	protected void createScenario() {
 		// If a path is selected...
 		if (bhmf.getBHTree().getSelectionPath() != null) {
 
 			// ...create new scenario
 			DTOScenario newScenario = new DTOScenario();
 			// TODO hardgecodeder String raus! AS
 			newScenario.put(DTOScenario.Key.NAME, new StringValue(
 					"neues Scenario"));
 
 			// ...set Basis (IDENTIFIER) of scenario -> naming of periods
 			newScenario.put(DTOScenario.Key.IDENTIFIER, new StringValue(""
 					+ Calendar.getInstance().get(Calendar.YEAR)));
 
 			// ...add it to DTO-Repository
 			((DTOProject) ((BHTreeNode) bhmf.getBHTree().getSelectionPath()
 					.getPathComponent(1)).getUserObject())
 					.addChild(newScenario);
 
 			// ceck kind of scenario: deterministic or stochastic?
 			// TODO Schmalzhaf.Alexander: String raus!
 			ArrayList<BHComboBox.Item> itemsList = new ArrayList<BHComboBox.Item>();
 			itemsList.add(new BHComboBox.Item("deterministic", new StringValue(
 					"deterministisch")));
 			itemsList.add(new BHComboBox.Item("stochastic", new StringValue(
 					"stochastisch")));
 			BHComboBox.Item res = (BHComboBox.Item) JOptionPane
 					.showInputDialog(bhmf,
 							"Bitte gewünschten Szenariotyp auswählen:",
 							"Szenariotyp auswählen",
 							JOptionPane.QUESTION_MESSAGE, null, itemsList
 									.toArray(), null);
 
 			if (res == null)
 				return;
 
 			if (res.getKey().equalsIgnoreCase("STOCHASTIC"))
 				newScenario.put(DTOScenario.Key.STOCHASTIC_PROCESS,
 						new StringValue("true"));
 
 			// ...and insert it into GUI-Tree
 			BHTreeNode newScenarioNode = bhmf.getBHTree()
 					.addScenarioAtCurrentPos(newScenario);
 
 			// last steps: unfold tree to new element, set focus and start
 			// editing
 			bhmf.getBHTree().scrollPathToVisible(
 					new TreePath(newScenarioNode.getPath()));
 			bhmf.getBHTree().startEditingAtPath(
 					new TreePath(newScenarioNode.getPath()));
 		} else {
 			BHStatusBar.getInstance().setHint(
 					BHTranslator.getInstance().translate("EisSelectPeriod"),
 					true);
 		}
 
 	}
 
 	protected void createPeriod() {
 		// If a scenario or a period is selected...
 		if (bhmf.getBHTree().getSelectionPath() != null
 				&& bhmf.getBHTree().getSelectionPath().getPathCount() > 2) {
 
 			DTOScenario scenario = ((DTOScenario) ((BHTreeNode) bhmf
 					.getBHTree().getSelectionPath().getPathComponent(2))
 					.getUserObject());
 
 			// ...create new period
 			DTOPeriod newPeriod = new DTOPeriod();
 			// TODO hardgecodeder String raus! AS
 
 			// ...set name of period
 			String periodName = "";
 			if (scenario.getChildren().isEmpty()) {
 				try {
 					periodName = BHTranslator.getInstance().translate("period")
 							+ " "
 							+ scenario.get(DTOScenario.Key.IDENTIFIER)
 									.toString();
 				} catch (Exception e) {
 					// Do nothing;
 				}
 			} else {
 				DTOPeriod lastPeriod = scenario.getLastChild();
 
 				// distinguish between stoachstic and deterministic periods
 				int periodDifference = scenario.isDeterministic() ? 1 : -1;
 
 				try {
 					// get number of last Period and add 1.
 					periodName = ""
 							+ (Integer.parseInt(((StringValue) lastPeriod
 									.get(DTOPeriod.Key.NAME)).getString()) + periodDifference);
 				} catch (Exception e) {
 					try {
 						// get number and Text of last Period and add 1.
 						String lastPeriodName = ((StringValue) lastPeriod
 								.get(DTOPeriod.Key.NAME)).getString();
 						int tempNum = Integer.parseInt(lastPeriodName
 								.substring(getNumPos(lastPeriodName)));
 
 						periodName = lastPeriodName.substring(0,
 								getNumPos(lastPeriodName))
 								+ (tempNum + periodDifference);
 
 					} catch (Exception e1) {
 						// TODO Schmalzhaf.Alexander harter String raus!
 						periodName = "neue Periode";
 					}
 				}
 			}
 			newPeriod.put(DTOPeriod.Key.NAME, new StringValue(periodName));
 
 			// ...add it to DTO-Repository
 			((DTOScenario) ((BHTreeNode) bhmf.getBHTree().getSelectionPath()
 					.getPathComponent(2)).getUserObject()).addChild(newPeriod);
 
 			// ...and insert it into GUI-Tree
 			BHTreeNode newPeriodNode = bhmf.getBHTree().addPeriodAtCurrentPos(
 					newPeriod);
 
 			// last steps: unfold tree to new element, set focus and start
 			// editing
 			bhmf.getBHTree().scrollPathToVisible(
 					new TreePath(newPeriodNode.getPath()));
 			bhmf.getBHTree().startEditingAtPath(
 					new TreePath(newPeriodNode.getPath()));
 
 			Services.startPeriodEditing(newPeriod);
 		}
 	}
 
 	private void duplicateProject() {
 		TreePath currentDuplicateProjectSelection = bhmf.getBHTree()
 				.getSelectionPath();
 		if (currentDuplicateProjectSelection != null) {
 			// Access to selected project
 			BHTreeNode duplicateProjectNode = (BHTreeNode) bhmf.getBHTree()
 					.getSelectionPath().getLastPathComponent();
 
 			// zu kopierendes Project in eigene Variable
 			DTOProject duplicateProject = (DTOProject) duplicateProjectNode
 					.getUserObject();
 
 			// neues DTOProject mit Referenz auf den Klon
 
 			DTOProject newProject = (DTOProject) duplicateProject.clone();
 
 			bhmf.getBHTree().addProject(newProject);		
 
 		} else {
 			BHStatusBar.getInstance().setHint(
 					BHTranslator.getInstance().translate("EisSelectProject"),
 					true);
 		}
 	}
 
 	private void duplicateScenario() {
 		TreePath currentDuplicateProjectSelection = bhmf.getBHTree()
 				.getSelectionPath();
 		if (currentDuplicateProjectSelection != null) {
 			// Access to selected project
 			BHTreeNode duplicateScenarioNode = (BHTreeNode) bhmf.getBHTree()
 					.getSelectionPath().getLastPathComponent();
 
 			// zu kopierendes Project in eigene Variable
 			DTOScenario duplicateScenario = (DTOScenario) duplicateScenarioNode
 					.getUserObject();
 
 			// neues DTOProject mit Referenz auf den Klon
 			DTOScenario newScenario = (DTOScenario) duplicateScenario.clone();
 
 			bhmf.getBHTree().addScenarioAtCurrentPos(newScenario);
 
 
 		} else {
 			BHStatusBar.getInstance().setHint(
 					BHTranslator.getInstance().translate("EisSelectScenario"),
 					true);
 		}
 	}
 
 	protected void openUserHelp(String help) {
 		log.debug("HELPUSERHELP gefeuert");
 		JDialog frame = new JDialog();
 		frame.setTitle(BHTranslator.getInstance().translate("MuserHelpDialog"));
 		frame.setSize(610, 600);
 		frame.getContentPane().add(new BHHelpSystem(help));
 		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 
 	/**
 	 * Method to get Position on numeric value in a string Necessary for naming
 	 * of Periods
 	 * 
 	 * @param s
 	 * @return
 	 */
 	private int getNumPos(String s) {
 		int[] numbers = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
 
 		int start = s.length();
 
 		for (int i : numbers) {
 			if (s.indexOf("" + i) < start && s.indexOf("" + i) > -1)
 				start = s.indexOf("" + i);
 		}
 		System.out.println("start " + start);
 		return start;
 
 	}
 }
