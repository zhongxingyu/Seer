 package gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.io.File;
 
 /**
  * The Interface GuiInterface holds application settings we want to be implemented to all
  * components
  */
 public interface GuiInterface {
 	
 
 	// Background Colors & Shadows
 	static final Color FRAME_BACKGROUND = new Color(155, 185, 210); // Light Blue
 	static final Color PANEL_BACKGROUND = new Color(230, 220, 200); // Tan
 	static final Color STATUSTIP_BACKGROUND = new Color(75, 75, 75); // Yellow (243, 255, 159)
 	static final Color SURVEY_SHADOW = new Color(128, 128, 128); // Grey
 	static final Color BORDER_COLOR = new Color(35, 75, 125); // Dark Blue
 	static final Color HOVER_COLOR = new Color(0, 0, 255); // Dark Blue
 	static final Font BORDER_FONT = new Font ("Tahoma", 0, 12);
 	
 	// Sizes
 	static final int FRAME_WIDTH = 850;
 	static final int FRAME_HEIGHT = 625;
 	static final int SIDEBAR_WIDTH = 250;
 	static final int SIDEBAR_HEIGHT = 600;
 	static final int SURVEY_WIDTH = 750;
 	static final int SURVEY_HEIGHT = 680;
 	static final int EDIT_SURVEY_WIDTH = 650;
 	static final int EDIT_SURVEY_HEIGHT = 725;
 	static final int WELCOME_WIDTH = 750;
 	static final int WELCOME_HEIGHT = 450;
 	
 	// Fonts
 	static final Font FNT_SURVEY_TEXT = new Font("Segoi UI", Font.TRUETYPE_FONT, 32);
 	static final Color CLR_SURVEY_TEXT = new Color(26, 69, 147);
 
 	// Images/Icons
 	static final File IMG_APP_ICON = new File(System.getProperty("user.dir") + "\\img\\cis_house32x32.png");
 	static final File IMG_GROUP_LOGO = new File(System.getProperty("user.dir") + "\\img\\group_logo.png");
 	static final File IMG_SURVEY_LOGO = new File(System.getProperty("user.dir") + "\\img\\survey_logo.png");
 	static final File IMG_WELCOME_LOGO = new File(System.getProperty("user.dir") + "\\img\\welcome_logo.png");
 	static final File IMG_WELCOME_TEXT = new File(System.getProperty("user.dir") + "\\img\\welcome_text.png");
 	static final File IMG_DELETE = new File(System.getProperty("user.dir") + "\\img\\delete16x16.png");
 	static final File IMG_EXCEPTION_64 = new File(System.getProperty("user.dir") + "\\img\\exception64x64.png");
 	static final File IMG_EXCEPTION_16 = new File(System.getProperty("user.dir") + "\\img\\exception16x16.png");
 	static final File IMG_OPEN = new File(System.getProperty("user.dir") + "\\img\\open16x16.png");
 	static final File IMG_EXIT_16 = new File(System.getProperty("user.dir") + "\\img\\exit16x16.png");
 	static final File IMG_EXIT_64 = new File(System.getProperty("user.dir") + "\\img\\exit_64x64.png");
 	static final File IMG_SAVE = new File(System.getProperty("user.dir") + "\\img\\save16x16.png");
 	static final File IMG_SAVE_ALL = new File(System.getProperty("user.dir") + "\\img\\save_all16x16.png");
 	static final File IMG_EDIT = new File(System.getProperty("user.dir") + "\\img\\edit16x16.png");
 	static final File IMG_GROUP = new File(System.getProperty("user.dir") + "\\img\\group16x16.png");
 	static final File IMG_NEW_GROUP_64 = new File(System.getProperty("user.dir") + "\\img\\new_group_64x64.png");
 	static final File IMG_OPEN_GROUP_64 = new File(System.getProperty("user.dir") + "\\img\\open_group_64x64.png");
 	static final File IMG_SURVEY = new File(System.getProperty("user.dir") + "\\img\\student16x16.png");
 	static final File IMG_PRINT = new File(System.getProperty("user.dir") + "\\img\\print16x16.png");
 	static final File IMG_PROCESS_16 = new File(System.getProperty("user.dir") + "\\img\\process_16x16.png");
 	static final File IMG_CUT = new File(System.getProperty("user.dir") + "\\img\\cut16x16.png");
 	static final File IMG_COPY = new File(System.getProperty("user.dir") + "\\img\\copy16x16.png");
 	static final File IMG_PASTE = new File(System.getProperty("user.dir") + "\\img\\paste16x16.png");
 	static final File IMG_SETTINGS_16 = new File(System.getProperty("user.dir") + "\\img\\settings16x16.png");
 	static final File IMG_SETTINGS_64 = new File(System.getProperty("user.dir") + "\\img\\settings64x64.png");
 	static final File IMG_VIEW = new File(System.getProperty("user.dir") + "\\img\\view16x16.png");
 	static final File IMG_HELP_16 = new File(System.getProperty("user.dir") + "\\img\\help16x16.png");
 	static final File IMG_HELP_64 = new File(System.getProperty("user.dir") + "\\img\\help64x64.png");
 
 	// Form Field Arrays
 	static final String[] ARR_PERIOD = { "1", "2", "3", "4", "5", "6", "7", "8" };
 	static final String[] ARR_MARITAL_STATUS = {"Single", "Married", "Divorced"};
 	static final String[] ARR_EDUCATION = { "High School",
 			"On-the-Job Training", "Community College", "Technical School",
 			"Some College, Bachelor's Degree", "College + Graduate School" };
	static final String[] ARR_GPA = { "Select GPA", "Under 1.5", "1.5 - 1.9 ", "2.0 - 2.4",
			"2.5 - 2.9", "3.0 - 3.4", "3.5 - 4.0" };
 	static final String[] ARR_CHILDREN_COUNT = {"1", "2", "3", "4", "5" };
 	static final String[] ARR_CCARD_USES = {"Emergencies", "Non-Emergency"};
 	
 	// Miscellaneous
 	static final String FRAME_TITLE = "RealityU Surveyor";
 }
