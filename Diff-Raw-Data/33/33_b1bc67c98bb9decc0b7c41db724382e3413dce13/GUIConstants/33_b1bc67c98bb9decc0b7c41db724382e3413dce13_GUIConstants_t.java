 package gui;
 
 import java.awt.Dimension;
 import java.awt.Color;
 
 import javax.swing.ImageIcon;
 
 /**
  * Holds constants that the GUI needs to use.
  */
 public interface GUIConstants {
 
 	boolean COLORSON = true;
 	Color BACKGROUND_COLOR = new Color(214, 223, 217);
 	Color FOREGROUND_COLOR = Color.black;
 	Color FG_TEXT_COLOR = Color.black;
 	Color BG_TEXT_COLOR = Color.black;
	Color ERROR_COLOR = Color.red;
	Color ERROR1_COLOR = Color.orange;
	Color ERROR2_COLOR = Color.yellow;
 	
 	boolean IMAGESON = true;
 	//	If on laptop:
 	ImageIcon INTROIMAGE = new ImageIcon("src/images/logo.png");
 	ImageIcon TREEOPENIMAGE = new ImageIcon("src/images/openmanhole.png");
 	ImageIcon TREECLOSEDIMAGE = new ImageIcon("src/images/closedmanhole.png");
 	ImageIcon TREELEAFIMAGE = new ImageIcon("src/images/splinter.png");
 	ImageIcon ADDBUTTONIMAGE = new ImageIcon("src/images/raphael.png");
 	ImageIcon EDITBUTTONIMAGE = new ImageIcon("src/images/donatello.png");
 	ImageIcon DELETEBUTTONIMAGE = new ImageIcon("src/images/michelangelo.png");
 	ImageIcon SAVEBUTTONIMAGE = new ImageIcon("src/images/leonardo.png");
 	ImageIcon FRAMEIMAGE = new ImageIcon("src/images/minilogo.png");
 	//	If on filesystem:
 //	ImageIcon INTROIMAGE = new ImageIcon("images/logo.png");
 //	ImageIcon TREEOPENIMAGE = new ImageIcon("images/openmanhole.png");
 //	ImageIcon TREECLOSEDIMAGE = new ImageIcon("images/closedmanhole.png");
 //	ImageIcon TREELEAFIMAGE = new ImageIcon("images/splinter.png");
 //	ImageIcon ADDBUTTONIMAGE = new ImageIcon("images/raphael.png");
 //	ImageIcon EDITBUTTONIMAGE = new ImageIcon("images/donatello.png");
 //	ImageIcon DELETEBUTTONIMAGE = new ImageIcon("images/michelangelo.png");
 //	ImageIcon SAVEBUTTONIMAGE = new ImageIcon("images/leonardo.png");
 //	ImageIcon FRAMEIMAGE = new ImageIcon("images/minilogo.png");
 	
 	String TOURNAMENT_EXTENSION = "tmnt";
 	String CATEGORY_EXTENSION = "csv";
 	
 	int DEFAULT_TABLE_BLANK_ROWS = 10;
 	int ROW_HEIGHT = 25;
 	
 	int DEFAULT_WIDTH = 900;
 	int DEFAULT_HEIGHT = 700;
 	Dimension DEFAULT_SIZE = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
 	
 	int MIN_WIDTH = 400;
 	int MIN_HEIGHT = 300;
 	Dimension MIN_SIZE = new Dimension(MIN_WIDTH, MIN_HEIGHT);
 	
 	int TREE_WIDTH = 200;
 	int TREE_HEIGHT = 400;
 	Dimension TREE_SIZE = new Dimension(TREE_WIDTH, TREE_HEIGHT);
 	
 	int UNITPANEL_WIDTH = 600;
 	int UNITPANEL_HEIGHT = 100;
 	Dimension UNITPANEL_SIZE = new Dimension(UNITPANEL_WIDTH, UNITPANEL_HEIGHT);
 	
 	int ADDINGPANEL_WIDTH = 700;
 	int ADDINGPANEL_HEIGHT = 700;
 	Dimension ADDINGPANEL_SIZE = new Dimension(ADDINGPANEL_WIDTH, ADDINGPANEL_HEIGHT);
 	
 	int INPUTTABLE_WIDTH = 600;
 	int INPUTTABLE_HEIGHT = 300;
 	Dimension INPUTTABLE_SIZE = new Dimension(INPUTTABLE_WIDTH, INPUTTABLE_HEIGHT);
 	
 	int PAIRINGPANEL_WIDTH = 900;
 	int PAIRINGPANEL_HEIGHT = 300;
 	Dimension PAIRINGPANEL_SIZE = new Dimension(PAIRINGPANEL_WIDTH, PAIRINGPANEL_HEIGHT);
 	
 	int TOOLBAR_WIDTH = 900;
 	int TOOLBAR_HEIGHT = 50;
 	Dimension TOOLBAR_SIZE = new Dimension(TOOLBAR_WIDTH, TOOLBAR_HEIGHT);
 	
 	int TEXTFIELD_WIDTH = 150;
 	int TEXTFIELD_HEIGHT = 25;
 	Dimension TEXTFIELD_SIZE = new Dimension(TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
 	
 	int JCOMBOBOX_WIDTH = 150;
 	int JCOMBOBOX_HEIGHT = 25;
 	Dimension JCOMBOBOX_SIZE = new Dimension(JCOMBOBOX_WIDTH, JCOMBOBOX_HEIGHT);
 	
 	int SMALLSPACING_X = 10;
 	int SMALLSPACING_Y = 10;
 	Dimension SMALLSPACING_SIZE = new Dimension(SMALLSPACING_X, SMALLSPACING_Y);
 	
 	int BIGSPACING_X = 50;
 	int BIGSPACING_Y = 50;
 	Dimension BIGSPACING_SIZE = new Dimension(BIGSPACING_X, BIGSPACING_Y);
 }
