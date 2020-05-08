 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Scanner;
 import java.io.File;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 
 /**
  * The primary client for the password generator. All user action is driven by this GUI
  * @author gizmo385
  */
 public class PassGenClient extends JFrame implements ActionListener {
 	
 	//Frame components
	private PasswordGenerator pg;
 	private JTextField seedField, outputField;
 	private JButton randomSeed, generate;
 	private JPanel controls, output;
 	private TitledBorder controlsBorder, outputBorder;
 	private JMenuBar menuBar;
 	private JMenu file, about, changeSettings;
 	private JMenuItem exit, howToUse, aboutProgram, editSettings, sourceCode;
 	
 	//Settings
 	private String wordlistDirectoryPath, currentPasswordPattern;
 	private int currentMaxCharLimit, swapFrequency, capitalizationFrequency;
 	private boolean randomlySwappingChars, maxCharLimitFlag, randomCapitalization;
 	
 	/**
 	 * Constructs the window
 	 */
 	public PassGenClient() {
 		super( "Password Generator" );
 		
 		//Methods to set up various components of program
 		setUpPanels();
 		setUpMenuBar();
 		loadSettings();
 		
 		//JFrame settings
 		super.add( this.controls );
 		super.add( this.output );
 		super.setLayout( new FlowLayout() );
 		super.setSize( 300, 180 );
 		super.setLocationRelativeTo( null );
 		super.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 		super.setResizable( false );
 		super.setJMenuBar( this.menuBar );
 		super.setVisible( true );
 		
 		try {
 			super.setIconImage( ImageIO.read( getClass().getResource( "res/lock.png" ) ) );
 		}
 		catch( IOException ioe ) {
 			JOptionPane.showMessageDialog( this, "Could not load application icon!" , "Error!", JOptionPane.ERROR_MESSAGE );
 		}
 	}
 	
 	/**
 	 * Manage program actions
 	 */
 	public void actionPerformed( ActionEvent ae ) {
 		if( ae.getSource() == generate || ae.getSource() == seedField ) {
 		
 			//String seedString, String pattern, int characterSwapFrequency, int capitalizationFrequency, int lengthLimit, 
 			//boolean swappingEnabled, boolean capitalizationEnabled, boolean lengthLimitEnabled
 			this.outputField.setText( pg.generate( this.seedField.getText(), this.currentPasswordPattern, this.swapFrequency, this.capitalizationFrequency,
 													this.currentMaxCharLimit, this.randomlySwappingChars, this.randomCapitalization, this.maxCharLimitFlag ) );
 		}
 		
 		else if( ae.getSource() == randomSeed ) {
 			this.seedField.setText( System.currentTimeMillis() + "" );
 		}
 		
 		else if( ae.getSource() == exit ) {
 			System.exit(0);
 		}
 		
 		else if( ae.getSource() == aboutProgram ) {
 			JOptionPane.showMessageDialog( this, "PassGen was created by Gizmo385 and is freely distributed, along \nwith " + 
 													"source code to any and all who wish to view it.", "About", JOptionPane.INFORMATION_MESSAGE );
 		}
 		
 		else if( ae.getSource() == sourceCode ) {
 			JOptionPane.showMessageDialog( this, "The source code for PassGen is freely accessible to any and all who wish to view it on Github @gizmo385", "Source code information", JOptionPane.INFORMATION_MESSAGE );
 		}
 		
 		else if( ae.getSource() == howToUse ) {
 			HelpDialog hd = new HelpDialog( this );
 		}
 		
 		else if( ae.getSource() == editSettings ) {
 			SettingsDialog sd = new SettingsDialog( this );
 			loadSettings();
 		}
 	}
 	
 	/**
 	 * Restores the default settings and saves them
 	 */
 	private void restoreDefaultSettings() {
 		//default settings
 		this.wordlistDirectoryPath = "default";
 		this.currentPasswordPattern = "sn*sns";
 		this.currentMaxCharLimit = 0;
 		this.swapFrequency = 5;
 		this.capitalizationFrequency = 10;
 		this.randomlySwappingChars = true;
 		this.maxCharLimitFlag = false;
 		this.randomCapitalization = true;
		
		this.pg = new PasswordGenerator();
 	}
 	
 	/**
 	 * Loads program settings
 	 */
 	private void loadSettings() {
 		File saveFile = new File( ".settings.ini" );
 		
 		try {
 			//if no save file can be found, load with default settings and save
 			if( saveFile.exists() == false ) {
 				restoreDefaultSettings();
 			}
 			else {
 				Scanner fileScan = new Scanner( saveFile );
 				this.wordlistDirectoryPath = fileScan.nextLine();
 				this.currentPasswordPattern = fileScan.nextLine();;
 				this.currentMaxCharLimit = Integer.parseInt( fileScan.nextLine() );
 				this.swapFrequency = Integer.parseInt( fileScan.nextLine() );
 				this.capitalizationFrequency = Integer.parseInt( fileScan.nextLine() );
 				this.randomlySwappingChars = Boolean.parseBoolean( fileScan.nextLine() );
 				this.maxCharLimitFlag = Boolean.parseBoolean( fileScan.nextLine() );
 				this.randomCapitalization = Boolean.parseBoolean( fileScan.nextLine() );
 				fileScan.close();
				
				//Load proper wordlist
				this.pg = this.wordlistDirectoryPath.equals("default") ? new PasswordGenerator() : new PasswordGenerator( this.wordlistDirectoryPath );
 			}
 		}
 		catch( Exception e ) {
 			JOptionPane.showMessageDialog( this, "Error loading settings! Staring with default!", "Error!", JOptionPane.ERROR_MESSAGE );
 		}
 	}
 	
 	/**
 	 * Sets up JPanels used in the GUI
 	 */
 	private final void setUpPanels() {
 		//panel components
 		this.seedField = new JTextField( 15 );
 		this.seedField.addActionListener( this );
 		this.seedField.setToolTipText( "Enter the seed for your password: " );
 		
 		this.outputField = new JTextField( 23 );
 		this.outputField.setEditable( false );
 		
 		this.randomSeed = new JButton( "#" );
 		this.randomSeed.addActionListener( this );
 		this.randomSeed.setToolTipText( "Get a random seed, generated by the system time" );
 		
 		this.generate = new JButton( ">" );
 		this.generate.addActionListener( this );
 		this.controlsBorder = BorderFactory.createTitledBorder( "Generator Controls" );
 		this.outputBorder = BorderFactory.createTitledBorder( "Output" );
 		
 		//panels
 		this.output = new JPanel();
 		this.output.add( outputField );
 		this.output.setBorder( outputBorder );
 		
 		this.controls = new JPanel();
 		this.controls.add( seedField );
 		this.controls.add( randomSeed );
 		this.controls.add( generate );
 		this.controls.add( output );
 		this.controls.setLayout( new FlowLayout() );
 		this.controls.setBorder( controlsBorder );
 	}
 	
 	/**
 	 * Sets up the menu bar
 	 */
 	private final void setUpMenuBar() {
 		//menu items
 		this.exit = new JMenuItem( "Exit" );
 		this.exit.addActionListener( this );
 		this.exit.setIcon( new ImageIcon( getClass().getResource( "res/close.png" ) ) );
 		this.howToUse = new JMenuItem( "Help" );
 		this.howToUse.addActionListener( this );
 		this.aboutProgram = new JMenuItem( "About" );
 		this.aboutProgram.addActionListener( this );
 		this.sourceCode = new JMenuItem( "Program source" );
 		this.sourceCode.addActionListener( this );
 		this.editSettings = new JMenuItem( "Change Settings" );
 		this.editSettings.setIcon( new ImageIcon( getClass().getResource( "res/gear.png" ) ) );
 		this.editSettings.addActionListener( this );
 		
 		//menus
 		this.file = new JMenu( "General Settings" );
 		this.file.add( editSettings );
 		this.file.add( exit );
 		
 		this.about = new JMenu( "About & Help" );
 		this.about.add( howToUse );
 		this.about.add( aboutProgram );
 		this.about.add( sourceCode );
 		
 		//menu bar
 		this.menuBar = new JMenuBar();
 		this.menuBar.add( file );
 		this.menuBar.add( about );
 	}
 	
 	public static void main( String[] args ) {
 		PassGenClient pgc = new PassGenClient();
 	}
 }
