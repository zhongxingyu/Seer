 /*
  * Jan 22, 2006
  */
 package com.thinkparity.ophelia.browser.application.browser;
 
 import java.awt.Color;
 import java.awt.Font;
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public final class BrowserConstants {
 
 	/** The default dialogue background colour. */
     public static final Color DIALOGUE_BACKGROUND = new Color(225, 228, 231, 255);  // A colour within the browser gradient
 
     /** Browser Colours */
     public static final class Colours {
 
         /** The colour of the separator for the cells in the main list. */
         public static final Color MAIN_CELL_DEFAULT_BORDER = new Color(207, 207, 207, 255);
 
         /** The colour of the separator for the child cells in the main list. */
         public static final Color MAIN_CELL_DEFAULT_BORDER_CHILD = Color.WHITE;
 
         /** The colour used as a separator between groups. */
         public static final Color MAIN_CELL_DEFAULT_BORDER_GROUP = new Color(207, 207, 207, 255);
 
         /** The error text colour. */
        public static final Color DIALOG_ERROR_TEXT_FG = new Color(255, 0, 0, 255);
 
         /** The standard text colour. */
         public static final Color DIALOG_TEXT_FG = Color.BLACK;
     }
 
     /** Browser Fonts */
     public static final class Fonts {
 
         /** The dialog font family name. */
         private static final String DIALOG_FONT_NAME = "Tahoma";
 
         /** The font family name. */
         private static final String FONT_NAME = "Tahoma";
 
         /** The default font. */
         public static final Font DefaultFont =
             new Font(FONT_NAME, Font.PLAIN, 11);
 
         /** The default bold font. */
         public static final Font DefaultFontBold =
             new Font(FONT_NAME, Font.BOLD, 11);
 
         /** The dialog font. */
         public static final Font DialogFont =
             new Font(DIALOG_FONT_NAME, Font.PLAIN, 11);
 
         /** The dialog bold font. */
         public static final Font DialogFontBold =
             new Font(DIALOG_FONT_NAME, Font.BOLD, 11);
         
         /** The dialog button font. */
         public static final Font DialogButtonFont =
             new Font(DIALOG_FONT_NAME, Font.PLAIN, 11);
         
         /** The dialog text entry font. */
         public static final Font DialogTextEntryFont =
             new Font(DIALOG_FONT_NAME, Font.PLAIN, 11);
 
         /** The dialog title font. */
         public static final Font DialogTitle =
             new Font(DIALOG_FONT_NAME, Font.BOLD, 13);
 
         /** The small font. */
         public static final Font SmallFont =
             new Font(FONT_NAME, Font.PLAIN, 10);
 
         /** The status bar font. */
         public static final Font StatusBar =
             new Font(FONT_NAME, Font.PLAIN, 11);
     }
 }
