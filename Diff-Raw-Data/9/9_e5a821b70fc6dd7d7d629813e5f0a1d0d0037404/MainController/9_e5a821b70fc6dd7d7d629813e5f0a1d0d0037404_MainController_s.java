 package gui.controller;
 
 import gui.AboutFrame;
 import gui.FileFrame;
 import gui.HelpFrame;
 import gui.MainFrame;
 import gui.ParameterFrame;
 import gui.RandomTestFrame;
 import gui.SettingsFrame;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import misc.Editor;
 import misc.ExecutionHandler;
 import misc.Help;
 import misc.MessageSystem;
 import misc.Settings;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 
 /**
  * This class is the most important part of the GUI component. It initializes
  * all of the other controllers and delegates user actions to their respective
  * receiver controllers. It is also responsible for the view @see{MainFrame} 
  * and uses @see{ExecutionHandler} as model.
  */
 public class MainController implements SelectionListener {
 	/**
 	 * model and connection with other components
 	 */
 	private ExecutionHandler executionHandler;
 	/**
 	 * main view for displaying all other views
 	 */
 	private MainFrame mainframe;
 	/**
 	 * controller for settings
 	 */
 	private SettingsController settingsController;
 	/**
 	 * controller for help
 	 */
 	private HelpController helpController;
 	/**
 	 * controller for parameter input
 	 */
 	private ParameterController parameterContoller;
 	/**
 	 * controller for editor
 	 */
 	private EditorController editorController;
 	/**
 	 * controller for variables and breakpoints
 	 */
 	private TableViewController tableController;
 
 	/**
 	 * Construct a main controller and initializes all other controllers.
 	 */
 	public MainController() {
 		MessageSystem messagesystem = new MessageSystem();
 		this.executionHandler = new ExecutionHandler(messagesystem);
 		Editor editor = new Editor();
 		this.mainframe = new MainFrame(this, editor, messagesystem);
 		this.editorController = new EditorController(editor, this.mainframe.getEditor());
 		this.settingsController = new SettingsController(Settings.getInstance());
 		this.helpController = new HelpController(Help.getInstance(), this.mainframe.getHelpBox());
 		this.parameterContoller = new ParameterController(this.executionHandler, messagesystem);
 		this.tableController = new TableViewController(this.mainframe.getBreakpointView(), 
 				this.mainframe.getVarView(), this.executionHandler);
 
 		// Has to be the last call in the constructor
 		initMainFrame();
 	}
 
 	/**
 	 * Open the main frame.
 	 */
 	private void initMainFrame() {
 		/*
 		 * Very important to call this in a separated method because SWT uses an
 		 * infinite loop for its window management and we need the instance of
 		 * MainFrame.
 		 */
 		this.mainframe.openWindow();
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		//menu bar button events
 		if (e.getSource() == this.mainframe.getMenuBar().getMenuBarItemExit()) {
 			//Save modified settings
 			if(Settings.getInstance().settingsChanged()) {
 				try {
 					Settings.getInstance().saveSettings();
 				} catch (IOException ioe) {
 					//The default settings will be loaded at the next program start
 				}
 			}
 			System.exit(0);
 		} else if (e.getSource() == mainframe.getMenuBar().getMenuBarItemLoad()) {
 			FileFrame openFileFrame = new FileFrame(this.mainframe.getShell(), SWT.OPEN);
 			if (openFileFrame.getChosenFile() != null) {
 				String fileText = getFileAsString(openFileFrame.getChosenFile());
 				if (fileText != null) {
 					this.mainframe.getEditor().setText(fileText);
 				}
 			}
 		} else if (e.getSource() == mainframe.getMenuBar().getMenuBarItemSave()) {
 			FileFrame saveFileFrame = new FileFrame(this.mainframe.getShell(), SWT.SAVE);
 			if (saveFileFrame.getChosenFile() != null) {
 				writeStringToFile(this.mainframe.getEditor().getText(), saveFileFrame.getChosenFile());
 			}
 		} else if (e.getSource() == mainframe.getMenuBar().getMenuBarItemNewFile()) {
 			this.editorController.getEditor().setSource("");
 		} else if (e.getSource() == mainframe.getMenuBar().getMenuBarItemSettings()) {
 			new SettingsFrame(this.mainframe.getShell(), this.settingsController);
 		} else if (e.getSource() == mainframe.getMenuBar().getMenuBarItemRandomTest()) {
 			RandomTestFrame randomtestframe = new RandomTestFrame(this.mainframe.getShell());
 			this.parameterContoller.addRandomTestFrame(randomtestframe, this.editorController.getEditor().getSource());
 		} else if (e.getSource() == mainframe.getMenuBar().getMenurBarItemAbout()) {
 			new AboutFrame(this.mainframe.getShell());
 		} else if (e.getSource() == mainframe.getMenuBar().getMenuBarItemHelp()) {
 			new HelpFrame(this.mainframe.getShell(), this.helpController);
 		} else if (e.getSource() == mainframe.getMenuBar().getMenurBarItemUndo()) {
 			this.editorController.getEditor().undo();
 		} else if (e.getSource() == mainframe.getMenuBar().getMenuBarItemRedo()) {
 			this.editorController.getEditor().redo();
 		} else if (e.getSource() == mainframe.getMenuBar().getMenuBarItemCut()) {
 			Point selection = this.mainframe.getEditor().getSelection();
 			String source = this.mainframe.getEditor().getText();
 			String cutSource = source.substring(0, selection.x) + source.substring(selection.y);
 			this.editorController.getEditor().setSource(cutSource);
 			this.mainframe.getEditor().getTextField().setSelection(selection.x);
 			
 			String cut = source.substring(selection.x, selection.y);
			this.mainframe.getClipboard().setContents(new Object[]{cut}, new Transfer[]{TextTransfer.getInstance()});
 		} else if (e.getSource() == mainframe.getMenuBar().getMenuBarItemCopy()) {
 			Point selection = this.mainframe.getEditor().getSelection();
 			String source = this.mainframe.getEditor().getText();
 			String copy = source.substring(selection.x, selection.y);
			this.mainframe.getClipboard().setContents(new Object[]{copy}, new Transfer[]{TextTransfer.getInstance()});
 		} else if (e.getSource() == mainframe.getMenuBar().getMenuBarItemPaste()) {
 			String paste = (String) this.mainframe.getClipboard().getContents(TextTransfer.getInstance());
 			if(paste != null && !paste.equals("")) {
 				Point selection = this.mainframe.getEditor().getSelection();
 				String source = this.mainframe.getEditor().getText();
 				String pastedSource = source.substring(0, selection.x) + paste + source.substring(selection.y); 
 				this.editorController.getEditor().setSource(pastedSource);
 				this.mainframe.getEditor().getTextField().setSelection(selection.x + paste.length());
 			}
 			//String pastedSource = source.substring(0, selection.x) + 
 		}
 		//button events
 		else if (e.getSource() == this.mainframe.getRunButton() 
 				|| e.getSource() == this.mainframe.getMenuBar().getMenuBarItemRun()) {
 			assert editorController != null;
 			this.executionHandler.setPaused(false);
 			if (this.executionHandler.getAST() == null) {
 				this.tableController.getVarView().getVarTree().removeAll();
 				this.executionHandler.parse(this.editorController.getEditor().getSource());
 				if (this.executionHandler.getAST() != null) {
 					ParameterFrame parameterframe = new ParameterFrame(this.mainframe.getShell());
 					parameterContoller.addParameterFrame(parameterframe);
 				} else {
 					return;
 				}
 			}			
 			this.runView();
 			
 			//Execution
 			new Thread() {
 				public void run() {
 					while (!parameterContoller.getParameterframe().getShell().isDisposed()
 							&& !(executionHandler.getAST().getMainFunction().getParameters() == null
 							|| executionHandler.getAST().getMainFunction().getParameters().length == 0)) {
 						try {
 							sleep(100);
 						} catch (InterruptedException ignored) {
 						}
 					}
 					executionHandler.run(editorController.getEditor().getStatementBreakpoints(),
 							executionHandler.getGlobalBreakpoints());
 					mainframe.getDisplay().asyncExec(new Runnable() {
 						public void run() {
 							if (executionHandler.getAssertionFailureMessage() != null) {
 								executionHandler.printAssertionFailureMessage();
 								stopView();
 								return;
 							}
 							if (executionHandler.getPaused()) {
 								pauseView();
 							}
 							else {
 								stopView();
 								executionHandler.addSuccessMessage("Program execution successful!");
 							}
 						}
 					});
 				}
 			}.start();
 		} else if (e.getSource() == this.mainframe.getStepButton() 
 				|| e.getSource() == this.mainframe.getMenuBar().getMenuBarItemStep()) {
 			//Functions
 			assert editorController != null;
 			if (this.executionHandler.getAST() == null) {
 				this.tableController.getVarView().getVarTree().removeAll();
 				this.executionHandler.parse(this.editorController.getEditor().getSource());
 				if (this.executionHandler.getAST() != null) {
 					ParameterFrame parameterframe = new ParameterFrame(this.mainframe.getShell());
 					this.parameterContoller.addParameterFrame(parameterframe);
 				} else {
 					return;
 				}
 			}
 			
 			//Execution
 			new Thread() {
 				public void run() {
 					while (!parameterContoller.getParameterframe().getShell().isDisposed()
 							&& !(executionHandler.getAST().getMainFunction().getParameters() == null
 							|| executionHandler.getAST().getMainFunction().getParameters().length == 0)) {
 						try {
 							sleep(100);
 						} catch (InterruptedException ignored) {
 						}
 					}
 					executionHandler.singleStep(editorController.getEditor().getStatementBreakpoints(),
 							executionHandler.getGlobalBreakpoints());
 					mainframe.getDisplay().asyncExec(new Runnable() {
 						public void run() {
 							pauseView();
 							if (executionHandler.getAssertionFailureMessage() != null) {
 								executionHandler.printAssertionFailureMessage();
 								stopView();
 								return;
 							}
 							tableController.updateVarView();
 							if (executionHandler.getProgramExecution() != null
 									&& executionHandler.getProgramExecution().getCurrentState().getCurrentStatement() == null) {
 								stopView();
 								executionHandler.addSuccessMessage("Program execution successful!");
 							}
 						}
 					});
 				}
 			}.start();
 		} else if (e.getSource() == mainframe.getPauseButton()) {
 			this.executionHandler.setPaused(true);
 		} else if (e.getSource() == mainframe.getStopButton()) {
 			this.stopView();
 		} else if (e.getSource() == mainframe.getCheckSyntaxButton()) {
 			//Functions
 			assert editorController != null;
 			this.executionHandler.parse(this.editorController.getEditor().getSource());
 			if (this.executionHandler.getAST() != null) {
 				this.executionHandler.addSuccessMessage("Syntax is correct!");
 			}
 			this.executionHandler.setAST(null);
 		} else if (e.getSource() == mainframe.getValidateButton() || e.getSource() == mainframe.getMenuBar().getVerItem()) {
             this.executionHandler.verify(this.editorController.getEditor().getSource());
 			this.executionHandler.setAST(null);
 		}
 	}
 
 	/**
 	 * Update the main frame when changed to running state.
 	 */
 	private void runView() {
 		//Functions
 		this.editorController.removeMark();
 		//Images
 		Image image = new Image(this.mainframe.getDisplay(), 
 				MainFrame.class.getResourceAsStream("image/run2.png"));
 		Image image2 = new Image(this.mainframe.getDisplay(),
 				MainFrame.class.getResourceAsStream("image/pause1.png"));
 		this.mainframe.switchIcon(image, image2);
 		//(De-)activations
 		this.mainframe.getPauseButton().setEnabled(true);
 		this.mainframe.getStepButton().setEnabled(false);
 		this.mainframe.getStopButton().setEnabled(true);
 		this.mainframe.getCheckSyntaxButton().setEnabled(false);
 		this.tableController.activateVarView();
 		this.tableController.deactivateBreakpointView();
 		this.tableController.getVarView().getVarTree().removeAll();
 		this.editorController.deactivateView();
 	}
 	
 	/**
 	 * Update the main frame when changed to paused state.
 	 */
 	private void pauseView() {
 		//Functions
 		this.tableController.updateVarView();
 		this.editorController.removeMark();
 		if (this.executionHandler.getProgramExecution() != null
 				&& this.executionHandler.getProgramExecution().getCurrentState().getCurrentStatement() != null) {
 			int line = this.executionHandler.getProgramExecution().getCurrentState().getCurrentStatement().getPosition().getLine();
 			this.editorController.markCurrentLine(line - 1);
 		}
 		//Images
 		Image image = new Image(this.mainframe.getDisplay(), 
 				MainFrame.class.getResourceAsStream("image/run1.png"));
 		Image image2 = new Image(this.mainframe.getDisplay(),
 				MainFrame.class.getResourceAsStream("image/pause2.png"));
 		//(De-)activations
 		this.mainframe.switchIcon(image, image2);
 		this.mainframe.getStepButton().setEnabled(true);
 		this.mainframe.getPauseButton().setEnabled(false);
 		this.mainframe.getStopButton().setEnabled(true);
 		this.mainframe.getCheckSyntaxButton().setEnabled(false);
 		this.editorController.deactivateView();
 		this.tableController.activateVarView();
 		this.tableController.deactivateBreakpointView();
 	}
 	
 	/**
 	 * Update the view when changed to idle state.
 	 */
 	private void stopView() {
 		//Functions
 		this.editorController.removeMark();
 		this.tableController.updateVarView();
 		this.executionHandler.destroyProgramExecution();
 		this.executionHandler.setParameterValues(null);
 		this.executionHandler.setAST(null);
 		//Images
 		Image image = new Image(this.mainframe.getDisplay(), 
 				MainFrame.class.getResourceAsStream("image/run1.png"));
 		Image image2 = new Image(this.mainframe.getDisplay(), 
 				MainFrame.class.getResourceAsStream("image/pause1.png"));
 		this.mainframe.switchIcon(image, image2);
 		//(De-)activations
 		this.mainframe.getStopButton().setEnabled(false);
 		this.mainframe.getPauseButton().setEnabled(false);
 		this.mainframe.getStepButton().setEnabled(true);
 		this.mainframe.getCheckSyntaxButton().setEnabled(true);
 		this.tableController.deactivateVarView();
 		this.tableController.activateBreakpointView();
 		this.editorController.activateView();
 	}
 
 	private static String getFileAsString(File file) {
 		BufferedReader fileReader = null;
 		try {
 			fileReader = new BufferedReader(new FileReader(file));
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		}
 		String line = null;
 		String fileText = new String();
 		try {
 			while ((line = fileReader.readLine()) != null) {
 				fileText += line + '\n';
 			}
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		return fileText;
 	}
 
 	private static void writeStringToFile(String content, File file) {
 		BufferedWriter fileWriter = null;
 		try {
 			fileWriter = new BufferedWriter(new FileWriter(file));
 			fileWriter.write(content);
 			fileWriter.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 	}
 }
