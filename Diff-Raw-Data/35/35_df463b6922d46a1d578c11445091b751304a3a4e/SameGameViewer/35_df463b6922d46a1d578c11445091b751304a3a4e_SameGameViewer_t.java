 package de.tu_darmstadt.gdi1.samegame.view;
 
 import java.awt.Color;
 import java.io.File;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import javax.swing.filechooser.FileFilter;
 import java.util.Locale;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import de.tu_darmstadt.gdi1.samegame.exceptions.InternalFailureException;
 import de.tu_darmstadt.gdi1.samegame.exceptions.ParameterOutOfRangeException;
 
 import de.tu_darmstadt.gdi1.samegame.controller.AbstractController;
 import de.tu_darmstadt.gdi1.samegame.controller.GameController;
 
 import de.tu_darmstadt.gdi1.samegame.view.gameframes.*;
 
 import de.tu_darmstadt.gdi1.samegame.model.Level;
 
 public class SameGameViewer implements ChangeListener{
 	
 	/**
 	 * the default language
 	 */
 	public static final Locale DEFAULT_LOCALE = new Locale("en", "US");
 	
 	/**
 	 * the currently language
 	 */
 	private Locale currentLocale;
 	
 	/**
 	 * the level information
 	 */
 	private Level level;
 	
 	/**
 	 * the abstract controller
 	 */
 	private AbstractController controller;
 	
 	/**
 	 * the Main Frame
 	 */
 	private MainFrame mainFrame;
 	
 	/**
 	 * the Main Panel, where the stones are
 	 */
 	private MainPanel mainPanel;
 	
 	/**
 	 * Frame to add a highscore to the highscore list
 	 */
 	private AddHighscoreFrame addHighscoreFrame;
 	
 	/**
 	 * The frame, which shows the highscore-list
 	 */
     private HighscoreFrame highscoreFrame;
     
     /**
      * the frame, which contains information about the authors
      */
     private AboutFrame aboutFrame;
     /**
      * the frame, which allows custom Levels
      */
     private CustomizeFrame customFrame;
     /**
      * the currently choosed skin
      */
     private String current_Skin = "defaultskin";
 
     /**
      * the background color
      */
 	private Color current_BColor = Color.black;
 	
 	/**
 	 * the font color
 	 */
 	private Color current_FColor = Color.white;
 	
 	
 	
 	private ResourceBundle messages;
 
 	/**
 	 * class constructor which sets the default locale as current locale
 	 */
 	public SameGameViewer(){
 		currentLocale = (Locale) DEFAULT_LOCALE.clone();
 		try{
 			messages = 
 				ResourceBundle.getBundle(
 						"de.tu_darmstadt.gdi1.samegame.view.ViewBundle", 
 						this.currentLocale, 
 						this.getClass().getClassLoader()); 
 		}catch(MissingResourceException e){
 			this.currentLocale = new Locale("de", "DE");
 
 			messages = 
 				ResourceBundle.getBundle(
 						"de.tu_darmstadt.gdi1.samegame.view.ViewBundle", 
 						this.currentLocale,
 						this.getClass().getClassLoader()); 
 		}
 	}
 
 	/**
 	 * sets the level
 	 * @param level 
 	 * 			the level to be set
 	 */
 	public void setLevel(Level level){
 		this.level = level;
 	}
 
 	/**
 	 * sets the controller
 	 * @param controller 
 	 * 			the abstract controller to be setted
 	 */
 	public void setController(AbstractController controller){
 		this.controller = controller;
 	}
 	
 	/**
 	 * sets the skin
 	 * @param skin 
 	 * 			the name of the skin
 	 * @param BColor 
 	 * 			the background color
 	 * @param FColor 
 	 * 			the font color
 	 * @param MColor 
 	 * 			the mark color
 	 */
 	public void setSkin(String skin, Color BColor, Color FColor, Color MColor){
 		mainFrame.setSkin(skin, BColor, FColor);
 		mainPanel.setMarkColor(MColor);
 		mainPanel.setBGColor(BColor);
 		current_BColor = BColor;
 		current_FColor = FColor;
 		current_Skin = skin;
 		
 	}
 	
 	/**
 	 * redraws the mainPanel when the state has changed
 	 */
 	// implements method from interface javax.swing.event.ChangeListener
 	public void stateChanged(ChangeEvent e){
 		if(mainFrame != null){
 			try{
 				mainPanel.redraw();
 			}catch(InternalFailureException ex){
 				ex.printStackTrace();
 			}
 			if(level.isFinished()){
 				showAlertFrame(messages.getString("Finish_Title"), messages.getString("Finish_Message"));
 				showAddHighscoreFrame();
 				String name = addHighscoreFrame.getPlayersName();
 				level.insertHighscore(name);
 				showHighscoreFrame();
 			}
 		}
 	}
 	
 	/**
 	 * marks the field which is focused
 	 * @param row 
 	 * 			the row of the field
 	 * @param col 
 	 * 			the column of the field
 	 */
 	public void markField(int row, int col){
 		if(mainFrame != null)
 			mainPanel.markField(row, col);
 	}
 	
 	/**
 	 * getter for the mainPanel
 	 * @return 
 	 * 		the main Panel
 	 */
 	public MainPanel getMainPanel(){
 		return this.mainPanel;
 	}
 
 	/**
 	 * getter for the row of the marked field
 	 * @return 
 	 * 			the row of the marked field
 	 */
 	public int getMarkedFieldRow(){
 		if(mainFrame != null)
 			return mainPanel.getMarkedFieldRow();
 		else
 			return -1;
 	}
 
 	/**
 	 * getter for the column of the marked field
 	 * @return 
 	 * 			the column of the marked field
 	 */
 	public int getMarkedFieldCol(){
 		if(mainFrame != null)
 			return mainPanel.getMarkedFieldCol();
 		else
 			return -1;
 	}
 
 	/**
 	 * checks if the main Panel is in animation
 	 * @return 	true  if the Panel is in motion
 	 * 			false if the Panel is not in motion
 	 */
 	public boolean duringAnimation(){
 		if(mainFrame != null)
 			return mainPanel.duringAnimation();
 		else return false;
 	}
 
 	/**
 	 * starts the animation of the falling stones in the mainPanel
 	 * @param row 
 	 * 			the row where the animation starts
 	 * @param col 
 	 * 			the column where the animation starts
 	 * @param animationSpeed 
 	 * 			the speed of the animation
 	 */
 	public void startAnimation(int row, int col, long animationSpeed){
 		mainPanel.startAnimation(row, col, animationSpeed);
 	}
 	
 	/**
 	 * sets the language of the frame Labels
 	 * sets currentLocale to this language
 	 * @param locale 
 	 * 			the chosen language 
 	 */
 	public void setLanguage(Locale locale){
 		mainFrame.setLanguage(locale);
 		currentLocale = locale;
 		try{
 			messages = 
 				ResourceBundle.getBundle(
 						"de.tu_darmstadt.gdi1.samegame.view.ViewBundle", 
 						this.currentLocale, 
 						this.getClass().getClassLoader()); 
 		}catch(MissingResourceException e){
 			this.currentLocale = new Locale("de", "DE");
 
 			messages = 
 				ResourceBundle.getBundle(
 						"de.tu_darmstadt.gdi1.samegame.view.ViewBundle", 
 						this.currentLocale,
 						this.getClass().getClassLoader()); 
 		}
 	}
 	
 	/**
 	 * gets the chosen language
 	 * @return 
 	 * 			the actual language Locale
 	 */
 	public Locale getLanguage(){
 		return mainFrame.getLanguage();
 	}
 	
 	/**
 	 * gets the dimensions of a new level which is loaded
 	 */
 	public void notifyLevelLoaded(){
 		if(mainFrame != null){
 			try{
 				mainFrame.notifyLevelLoaded(level.getFieldWidth(), level.getFieldHeight());
 			}catch(ParameterOutOfRangeException e){
 				e.printStackTrace();
 			}catch(InternalFailureException e){
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * initializes the mainFrame
 	 */
 	public void showMainFrame(){
 		this.mainFrame = new MainFrame(level, controller, currentLocale, current_Skin, current_BColor, current_FColor);
 		this.mainFrame.setVisible(true);
 		this.mainPanel = mainFrame.getMainPanel();
 		Thread timeUpdate = new Thread(mainFrame);
 		timeUpdate.start();
 	}
 
 	/**
 	 * filechooser to save or load a level
 	 * @param source
 	 * 				chooses the chooser for load or save
 	 * 			
 	 */
 	public File showFileChooseDialog(String source){
 		JFileChooser chooser = new JFileChooser(source);
 		int returnValue;
 		if(source.equals("SaveLevel") || source.equals("SaveGameState"))
 			returnValue = chooser.showSaveDialog(mainFrame);
 		else if(source.equals("LoadLevel") || source.equals("LoadGameState"))
 			returnValue = chooser.showOpenDialog(mainFrame);
 		else
 			returnValue = -1;
 
 		if(returnValue == JFileChooser.APPROVE_OPTION)
 			return chooser.getSelectedFile();
 		else return null;
 
 	}
 
 	/**
 	 * initializes the Frame to add a highscore to the highscorelist
 	 */
 	public void showAddHighscoreFrame(){
 		addHighscoreFrame = new AddHighscoreFrame(currentLocale);
 	}
 	
 	/**
 	 * initializes the Frame which shows the highscorelist
 	 */
 	public void showHighscoreFrame(){
 		highscoreFrame = new HighscoreFrame(currentLocale, level);
 	}
 
 	/**
 	 * shows the frame which contains the author information
 	 */
 	public void showAboutFrame(){
 		aboutFrame = new AboutFrame(currentLocale);
 	}
 	public void showCustomizeFrame(){
 		customFrame = new CustomizeFrame(currentLocale);
		this.closeMainFrame();
		level.generateLevel(
					customFrame.getCustomWidth(),
					customFrame.getCustomHeight(),
					customFrame.getCustomStoneNumber(),
					customFrame.getCustomMinStones());
		this.setLevel(level);
		this.showMainFrame();
		this.notifyLevelLoaded();
 	}
 
 	/**
 	 * opens an alert frame if somewhere an exception is thrown
 	 * @param alerttitle 
 	 * 			the title of the frame
 	 * @param alertstring 
 	 * 			the message in the frame
 	 */
 	public static void showAlertFrame(String alerttitle, String alertstring){
 		JFrame alertframe = new JFrame();
 		
 		int response = JOptionPane.showConfirmDialog(alertframe, alertstring, alerttitle,
 		   JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
 	
 			if (response == JOptionPane.OK_OPTION){
 				alertframe.setVisible(false);
 			}
 			
 			if (response == JOptionPane.CANCEL_OPTION){
 				alertframe.setVisible(false);
 				System.exit(0);
 			}
 			
 			if (response == JOptionPane.CLOSED_OPTION){
 				alertframe.setVisible(false);
 			}
 	}
 		
 
 	/**
 	 * closes the main frame
 	 */
 	public void closeMainFrame(){
 		if(mainFrame != null){
 			mainFrame.dispose();
 			mainFrame = null;
 		}
 	}
 
 	public void closeAddHighscoreFrame(){
 		addHighscoreFrame = null;
 	}
 
 	public void closeHighscoreFrame(){
 		highscoreFrame = null;
 	}
 
 	public void closeAboutFrame(){
 		aboutFrame = null;
 	}
 
 	public static void main(String args[]){
 		SameGameViewer viewer = new SameGameViewer();
 		Level level = new Level(viewer);
 		GameController controller = new GameController(level, viewer);
 		viewer.setController(controller);
 		viewer.setLevel(level);
 		level.generateLevel(10, 10, 5, 3);
 		viewer.showMainFrame();
 		viewer.notifyLevelLoaded();
 	}
 }
