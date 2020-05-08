 package btlshp.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.Serializable;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import btlshp.Btlshp;
 import btlshp.BtlshpGame;
 import btlshp.entities.Map;
 import btlshp.enums.AppState;
 
 
 public class MainUI implements Serializable{
 	
 	private static final long serialVersionUID = -3781967036180007188L;
 	private HelpScreen   helpScreen;
 	private JFrame       mainFrame;
 	private JMenuBar     mainMenuBar;
 	private FileMenu     fileMenu;
 	private GameGrid     gameGrid;
 	private JPanel       infoArea;
 	private OutputArea   outputConsole;
 	private JTextArea    status;
 	private Font         proFont;
 	
 	public MainUI() {
 		try {
 			proFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/ProFontWindows.ttf"));
 		} 
 		catch (Exception e) {
 			proFont = null;
 		}
 	}
 	
 	
 	/**
 	 * Used to update what items are available in the file menu when the game state changes.
 	 */
 	public void updateMainMenu() {
 		fileMenu.updateMenuItems();
 	}
 	
 	
 	private void makeMainMenu() {
 		mainMenuBar = new JMenuBar();
 		mainMenuBar.setOpaque(true);
 		mainMenuBar.setPreferredSize(new Dimension(200, 20));
 		
 		fileMenu = new FileMenu();		
 		mainMenuBar.add(fileMenu);
 		
 		mainFrame.setJMenuBar(mainMenuBar);
 		
 		fileMenu.updateMenuItems();
 	}
 	
 	
 	private void makeStatusPane() {
 		status = new JTextArea();
 		status.setPreferredSize(new Dimension(140, 75));
 		status.setMinimumSize(new Dimension(140, 75));
		status.setFont(proFont.deriveFont(Font.PLAIN, 12.0f));
 		status.setForeground(new Color(6, 178, 48));
 		status.setBackground(new Color(0, 0, 0));
 		status.setOpaque(true);
 		status.setEditable(false);
 		status.setLineWrap(true);
 		status.setWrapStyleWord(true);
 		
 		updateStatus();
 	}
 	
 	
 	/**
 	 * Builds the GUI for the main program.
 	 */
 	public void makeGUI() {
 		// Look and feel...
 		try {
 			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 		}
 		catch(Exception e) {
 		}
 		
 		
 		// Main JFrame
 		mainFrame = new JFrame("BtlShp");
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		Dimension dims = new Dimension();
 		dims.width = 640;
 		dims.height = 577;
 		
 		mainFrame.setSize(dims);
 		mainFrame.setResizable(false);
 		
 		if(!(mainFrame.getLayout() instanceof BorderLayout))
 			mainFrame.setLayout(new BorderLayout());
 		
 		// Make the menu
 		makeMainMenu();
 		
 		// Make the game grid
 		gameGrid = new GameGrid();
 		mainFrame.add(gameGrid, BorderLayout.CENTER);
 		
 		// Make the info area:
 		infoArea = new JPanel();
 		if(!(infoArea.getLayout() instanceof BorderLayout))
 			infoArea.setLayout(new BorderLayout());
 				
 		// Right status pane
 		makeStatusPane();
 		infoArea.add(status, BorderLayout.LINE_END);
 		
 		// Bottom console pane.
 		outputConsole = new OutputArea();
 		outputConsole.setFont(status.getFont());
 		outputConsole.addLine("Welcome to BtlShp! Please start or restore a game!");
 		outputConsole.addLine("BTLSHP by TeamTBD");
 		infoArea.add(outputConsole, BorderLayout.CENTER);
 		
 		mainFrame.add(infoArea, BorderLayout.PAGE_END);
 		
 		// Make help screen
 		helpScreen = new HelpScreen();
 		helpScreen.buildUi();
 		
 		// Show everything
 		mainFrame.pack();
 		mainFrame.setVisible(true);
 	}
 	
 	
 	/**
 	 * Forces the UI to refresh
 	 */
 	public void refresh() {
 		mainFrame.repaint();	
 	}
 	
 	
 	
 	/**
 	 * Utility function to display a Yes/No/Cancel dialog for the user.
 	 * 
 	 * @param title    Title of the dialog window
 	 * @param message  Message to be displayed inside the dialog
 	 * @return         Yes, No, or Cancel depending on the users decision.
 	 */
 	public DialogResult yesNoCancelDialog(String title, String message) {
 		int result = JOptionPane.showConfirmDialog(mainFrame, message, title, JOptionPane.YES_NO_CANCEL_OPTION);
 		
 		switch(result) {
 		case 0:
 			return DialogResult.Yes;
 		case 1:
 			return DialogResult.No;
 		case 2:
 			return DialogResult.Cancel;
 		}
 		
 		return DialogResult.Cancel;
 	}
 	
 	
 	/**
 	 * Utility function to display a dialog message popup to the user.
 	 * 
 	 * @param title    Title of the dialog window
 	 * @param message  Message to be displayed inside the dialog
 	 */
 	public void showNotificationDialog(String title, String message) {
 		JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.WARNING_MESSAGE);
 	}
 	
 	
 	/**
 	 * Utility function to show an alert to the player
 	 * 
 	 * @param title    Title of the alert
 	 * @param message  Message inside the alert
 	 */
 	public void showAlert(String title, String message) {
 		JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.INFORMATION_MESSAGE);
 	}
 	
 	
 	/**
 	 * Utility function to display a directory selection dialog and allow the user to select a directory.
 	 * 
 	 * @return    File representing the path the user selected, or null if the user canceled.
 	 */
 	public File selectDiretory(String title) {
 		JFileChooser fc = new JFileChooser();
 		
 		fc.setDialogTitle(title);
 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		fc.setAcceptAllFileFilterUsed(false);
 		
 		if(fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
 			return fc.getSelectedFile();
 		}
 		else 
 			return null;
 	}
 	
 	
 	/**
 	 * Utility function to display a file saving dialog and allow the user to select a directory.
 	 * 
 	 * @return    File representing the path the user selected, or null if the user canceled.
 	 */
 	public File selectSaveFile(String title) {
 		JFileChooser fc = new JFileChooser();
 		
 		fc.setDialogTitle(title);
 		
 		if(fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
 			return fc.getSelectedFile();
 		}
 		else 
 			return null;
 	}
 	
 	
 	/**
 	 * UI action to display the help screen.
 	 */
 	public void showHelp() {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				helpScreen.show();
 			}
 		});
 	}
 	
 	
 	/**
 	 * Handles the UI event when the map changes.
 	 * @param map     New game map.
 	 */
 	public void setMap(Map map) {
 		gameGrid.setMap(map);
 	}
 	
 	public Map getMap(){
 		return gameGrid.getMap();
 	}
 
 	/**
 	 * Outputs a message to the console.
 	 * 
 	 * @param message
 	 */
 	public void outputMessage(String message) {
 		outputConsole.addLine(message);
 	}
 
 
 	public GameGrid getGameGrid() {
 		return gameGrid;
 	}
 	
 	
 	/**
 	 * Updates the status text based on the current game state.
 	 */
 	public void updateStatus() {
 		String newStatus = "";
 		
 		BtlshpGame game = Btlshp.getGame();
 		
 		if(game == null || game.getAppState() == AppState.NoGame)
 			newStatus += "Not in game.\nStart or load a game!\n";
 		else if(game.getAppState() == AppState.LocalTurn){
 			newStatus += "Your turn\n";
 		newStatus += "Mines in inventory: " + game.getLocalPlayer().numberOfMines();
 		}
 		else{
 			newStatus += "Other players turn\n";
 			newStatus += "Mines in inventory: " + game.getLocalPlayer().numberOfMines();
 		}
 		
 		
 		status.setText(newStatus);
 	}
 }
