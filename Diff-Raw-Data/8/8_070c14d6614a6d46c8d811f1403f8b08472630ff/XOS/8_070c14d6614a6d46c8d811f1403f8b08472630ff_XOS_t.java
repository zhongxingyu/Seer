 /*
  * Copyright 2004 - 2013 Herb Bowie
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.powersurgepub.xos2;
 
   // import com.apple.mrj.*;
 	import com.apple.eawt.*;
   import com.apple.eio.*;
   import java.awt.*;
   import java.awt.event.*;
   import java.io.*;
   import java.lang.reflect.*;
   import java.net.*;
   import java.util.prefs.*;
   import javax.swing.*;
 
 /**
   A class that will provide cross-platform support.<p>
  
   Following is typical code that would be used with XOS and related classes. <p>
  
    <pre><code>
     import com.powersurgepub.xos2.*; 
     
     public class MyClass 
         extends javax.swing.JFrame 
           implements XHandler { 
     
       XOS xos = XOS.getShared(); 
  
       public MyClass () {
         xos.setDomainLevel1 ("powersurgepub");
         xos.setDomainLevel2 ("com");
         xos.setProgramName ("Two Due");
         xos.initialize();
  
         xos.setMainWindow (this);
         xos.setXHandler (this);
         xos.setFileMenu (fileMenu);
         xos.setHelpMenu (helpMenu);
         xos.setHelpMenuItem (helpUserGuideMenuItem);
       }
  
       public void handleAbout() {
         // Display About Box
       }
  
       public void handleOpenFile (File inFile) {
         // Open the passed file
       }
  
       public void handleQuit() {
         System.exit(0);
       }
     }
    </code></pre>
    
    Version History: <ul><li>
       2004/08/07 - Originally written. 
     </ul>
    @author Herb Bowie of PowerSurge Publishing
   
    @version 0.9 - 2004/08/07 - Originally written.
  */
 public class XOS 
     implements XHandler {
       
   /**
     User's directory. 
    */
   public  final static String   USER_DIR                = "user.dir";
   
   /** 
    Key used with system properties to determine Mac Runtime for Java presence. 
    */
   public  final static String   MRJ_VERSION             = "mrj.version";
   
   public  final static String   OS_NAME                 = "os.name";
   
   /** 
    Key used to set system properties to display program name under Mac OS. 
    */
   public  final static String   ABOUT_NAME               
       = "com.apple.mrj.application.apple.menu.about.name";
   
   /** 
    Key used to set system properties to show/ignore file packages in 
    open/save dialogs on Mac OS X.
    */
   public  final static String   USE_FILE_DIALOG_PACKAGES 
       = "com.apple.macos.use-file-dialog-packages";
   
   /** 
    Older key for setting system properties to use a screen menu bar instead of
    a window menu bar on Mac OS X. 
    */
   public  final static String   USE_SCREEN_MENU_BAR_1
       = "com.apple.macos.useScreenMenuBar";
   
   /**
    Newer key for setting system properties to use a screen menu bar instead
    of a window menu bar on Mac OS X.
    */
   public  final static String   USE_SCREEN_MENU_BAR_2
       = "apple.laf.useScreenMenuBar";
   
   /**
     String to be used when setting the Brushed Metal look on, when
     running on a Mac. 
    */
   public  final static String   BRUSHED_METAL 
       = "apple.awt.brushMetalLook";
   
   /**
     Preferences key for brushed metal look.
    */
   public  final static String   BRUSHED_METAL_KEY = "brushedmetal";
   
   /**
     Constant String for property turning on data modified indicator.
    */
   public  final static String   WINDOW_MODIFIED         = "windowModified";
   
   /**
    Value for setting a system property to yes/true. 
    */
   public  final static String   TRUE                    = "true";
   
   /**
    Value for setting a system property to no/false.
    */
   public  final static String   FALSE                   = "false";
   
   /** A forward slash. */
   public  final static String   SLASH                   = "/";
   
   /** 
    Key used with user preferences to set preferred look and feel.
    */
   public  final static String   LOOK_AND_FEEL_KEY       = "lookandfeel";
   
   /**
    Key used with user preferences to set preferred menu location on Mac OS X.
    */
   public  final static String   MENU_LOCATION_KEY       = "menulocation";
   
   /**
    Value used to set user preferences for MENU_LOCATION_KEY to indicate
    that the user would prefer to see menus at the top of the computer screen.
    */
   public  final static String   MENU_AT_TOP_OF_SCREEN   = "topofscreen";
   
   /**
    Value used to set user preferences for MENU_LOCATION_KEY to indicate
    that the user would prefer to see menus at the top of the program window.
    */
   public  final static String   MENU_AT_TOP_OF_WINDOW   = "topofwindow";
   
   /**
    Key for obtaining system properties indicating preferred 
    line ending characters.
    */
   public  final static String   LINE_SEP                = "line.separator";
   
   /** The string used as a user preference key for preferred line endings. */
   public  final static String   LINE_SEP_KEY            = "linesep";
   
   /** 
    The string used to express a preference for traditional mac line endings
    (CR).
    */
   public  final static String   LINE_SEP_PLATFORM_MAC   = "mac";
   
   /** 
    The string used to express a preference for Unix (including Mac OS X) 
    line endings (LF).
    */
   public  final static String   LINE_SEP_PLATFORM_UNIX  = "unix";
   
   /** 
    The string used to express a preference for DOS/PC/Windows  
    line endings (CR/LF).
    */
   public  final static String   LINE_SEP_PLATFORM_DOS   = "dos";
   
   /**
    The preferred line ending characters (CR) typical on a traditional
    Macintosh system.
    */
   public  final static String   LINE_SEP_STRING_MAC     = "\r";
   
   /**
    The preferred line ending characters (LF) typical on a Unix system
    (including Mac OS X).
    */
   public  final static String   LINE_SEP_STRING_UNIX    = "\n";
   
   /**
    The preferred line ending characters (CR/LF) typical on a PC/DOS/Windows
    system.
    */
   public  final static String   LINE_SEP_STRING_DOS     = "\r\n";
   
   private       static XOS      xos                     = null;
  
   private String              domainLevel1 = "";
   private String              domainLevel2 = "com";
   private String              programName = "";  
   private String              programNameLower = "";
   private String              programNameNoSpace = "";
   
   private XHandler            xHandler;
   
   private Preferences         userRoot;
   private Preferences         userPreferences           = null;;
   private Preferences         systemRoot;
   private Preferences         systemPreferences         = null;
   
   private String              mrjVersion                = "";
   private String              osName                    = "";
   private boolean             runningOnMacOS            = false;
   private String              sysLineSep                = null;
   private String              sysLineSepPlatform        = "";
   private String              prefLineSep               = null;
   private String              prefLineSepPlatform       = "";
   private String              userDirString             = "";
   
   private String              menuloc                   = MENU_AT_TOP_OF_WINDOW;
   
   private boolean             unsavedChanges            = false;
 
   private boolean             preferencesAvailable      = false;
 
   private JFrame              mainWindow                = null;
   private JMenuItem           helpMenuItem              = null;
   private JMenu               fileMenu                  = null;
   private JMenuItem           fileExitMenuItem          = null;
   private JMenu               helpMenu                  = null;
   private JMenuItem           helpAboutMenuItem         = null;
   
   private Application         macApp;
   
   /** 
     Returns a single instance of XOS that can be shared by many classes. This
     is the only way to obtain an instance of XOS, since the constructor is
     protected.
    
     @return A single, shared instance of XOS.
    */  
   public static XOS getShared() {
 
     if (xos == null) {
       xos = new XOS();
     }
     return xos;
   }
   
   /** 
     Creates a new instance of XOS. The constructor should not be called 
     directly, but instead invoked indirectly via a call to method getShared.
    */
   protected XOS () {
     
     osName = System.getProperty(OS_NAME);
     if (osName != null
         && osName.equalsIgnoreCase("Mac OS X")) {
       runningOnMacOS = true;
       macApp = Application.getApplication();
     /* }
     mrjVersion = System.getProperty (MRJ_VERSION);
     if (mrjVersion != null) {
       runningOnMacOS = true;
       macApp = Application.getApplication();
       System.out.println("Running on Mac OS"); */
     } else {
       // System.out.println("Not running on Mac OS");
     }
     
     // Set system default line separator characters
     sysLineSep = System.getProperty (LINE_SEP);
     sysLineSepPlatform = getLineSepPlatform (sysLineSep);
     prefLineSep = sysLineSep;
     prefLineSepPlatform = sysLineSepPlatform;
     
   } // end constructor
   
   /**
     Sets the first level domain name. This will be used as part of the 
     identification of user preferences. 
     
     @param domainLevel1 first level domain name (google, netscape, etc.)
    */
   public void setDomainLevel1 (String domainLevel1) {
     this.domainLevel1 = domainLevel1;
   }
   
   /**
     Sets the second level domain name. This will be used as part of the 
     identification of user preferences.
     
     @param domainLevel2 second level domain name (com, net, etc.)
    */
   public void setDomainLevel2 (String domainLevel2) {
     this.domainLevel2 = domainLevel2;
   } 
   
   /** 
     Sets the program name. This will be used as part of the 
     identification of user preferences.
     
     @param programName  Name of the current program as it should be displayed to
                         the user, possibly with mixed case (upper and lower) 
                         and embedded spaces.
    */
   public void setProgramName (String programName) {
     
     this.programName = programName.trim();
     programNameLower = this.programName.toLowerCase();
     programNameNoSpace = wordDemarcation
         (programNameLower, "", -1, -1, -1);
   } // end constructor
   
   /**
     Do startup tasks that should not be performed until after key 
     fields (domain name, program name) have been supplied. User preferences
     will be retrieved, and look and feel and menu locations will be set
     based on any established preferences.
    */
   public void initialize () {
     
     // Get nodes for Preferences
     userRoot = Preferences.userRoot();
     systemRoot = Preferences.systemRoot();
     userPreferences = userRoot.node (getPreferencesPath());
     systemPreferences = systemRoot.node (getPreferencesPath());
     userDirString = System.getProperty (USER_DIR);
     
     if (runningOnMacOS) {
       System.setProperty (ABOUT_NAME, programName);
       System.setProperty (USE_FILE_DIALOG_PACKAGES, TRUE);
       if (getBrushedMetal()) {
         System.setProperty (BRUSHED_METAL, TRUE);
       }
       menuloc = getPref (MENU_LOCATION_KEY, MENU_AT_TOP_OF_SCREEN);
       setMenuAtTopOfScreen (menuloc.equalsIgnoreCase (MENU_AT_TOP_OF_SCREEN));
     } // end if running on a Mac
     
     // Set Look and Feel if user has expressed a preference
     String lookAndFeelClassName = getLookAndFeelClassName();
     if ((! lookAndFeelClassName.equals ("null"))
         // Nimbus l&f sometimes causes null pointer exception 
         && (! lookAndFeelClassName.contains("Nimbus"))) {
       try {
         javax.swing.UIManager.setLookAndFeel (lookAndFeelClassName);
       } catch (Exception e) {
         // System.out.println ("Exception caught trying to set look and feel");
       } // end catch exception
     } // end if user has specified a preferred look and feel
     
     // See if user has expressed a preference that would override
     // the system default for line separators
     getLineSepPlatform();
     
   } // end method
   
   
   /**
     Pass the frame that will be the primary window for the application. 
    
     @param mainWindow JFrame containing the primary program window.
    */
   public void setMainWindow (JFrame mainWindow) {
     this.mainWindow = mainWindow;
 	  mainWindow.getRootPane().putClientProperty
         ("defeatSystemEventQueueCheck", Boolean.TRUE);
 		mainWindow.addWindowListener (new WindowAdapter()
 		  {
 		    public void windowClosing (WindowEvent e) {
           handleQuit();
 		    } // end WindowClosing
 		  } // end window listener 
 		); 
   }
   
   public JFrame getMainWindow () {
     return mainWindow;
   }
   
   /**
     When running on a Mac, a value of true will cause the main window
     to display a modification dot.
    
     @param unsavedChanges True if there are changes not yet saved to disk, 
                           false otherwise. 
    */
   public void setUnsavedChanges (boolean unsavedChanges) {
     this.unsavedChanges = unsavedChanges;
     if (runningOnMacOS) {
       if (mainWindow != null) {
         if (unsavedChanges) {
           mainWindow.getRootPane().putClientProperty 
               (WINDOW_MODIFIED, Boolean.TRUE);
         } else {
           mainWindow.getRootPane().putClientProperty 
               (WINDOW_MODIFIED, Boolean.FALSE);
         } // end if all changes saved
       } // end if we have a main window defined
     } // end if running on a Mac
   } // end method
   
   /**
     When running on a Mac, a value of true will cause the main window
     to display a modification dot.
    
     @return unsavedChanges  True if there are changes not yet saved to disk, 
                             false otherwise. 
    */
   public boolean getUnsavedChanges () {
     return unsavedChanges;
   } // end method
   
   /**
     Pass the class that implements the XHandler interface.
    
     @param xHandler Class that will handle About, Quit and openFile events.
    */
   public void setXHandler (XHandler xHandler) {
 		this.xHandler = xHandler;
     if (runningOnMacOS) {
       try {
         Object [] args = { this, macApp };
         Class [] arglist = { 
           Class.forName ("com.powersurgepub.xos2.XHandler"),
           Class.forName ("com.apple.eawt.Application")
         };
         // Class twodue = (Class)arglist[0];
         Class mac_class = Class.forName ("com.powersurgepub.xos2.MacHandler");
         java.lang.reflect.Constructor new_one = mac_class.getConstructor (arglist);
         new_one.newInstance (args);
       } catch (ClassNotFoundException cnf) { 
         System.out.println ("Class Not Found " + cnf.getMessage());
       } catch (NoSuchMethodException nsm) {
         System.out.println ("No Such Method " + nsm.getMessage());
       } catch (InstantiationException ins) {
         System.out.println ("Instantion Problem " + ins.getMessage());
       } catch (IllegalAccessException ill) {
         System.out.println ("Illegal Access " + ill.getMessage());
       } catch (InvocationTargetException it) {
         System.out.println ("Illegal Access " + it.getMessage());
       }
     } // end if running on a Mac
   } // end method
   
   public void enablePreferences () {
     preferencesAvailable = true;
   }
 
   public boolean preferencesAvailable() {
     return preferencesAvailable;
   }
   
   /**
     Pass the JMenu item acting as the File menu. If not running on a Mac,
     then an Exit menu item will be added to this menu.
    
     @param fileMenu the JMenu acting as the File menu.
    */
   public void setFileMenu (JMenu fileMenu) {
     this.fileMenu = fileMenu;
     if (! runningOnMacOS) {
       fileExitMenuItem = new javax.swing.JMenuItem();
       fileExitMenuItem.setText("Exit");
       fileExitMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_Q,
         Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       fileExitMenuItem.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
           handleQuit();
         }
       });
       fileMenu.add(fileExitMenuItem);
     }
   } // end method
   
   /**
     Pass the JMenu item acting as the Help menu. If not running on a Mac, then
     an About menu item will be added. 
    
     @param helpMenu JMenu acting as the Help menu. 
    */
   public void setHelpMenu (JMenu helpMenu) {
     this.helpMenu = helpMenu;
     if (! runningOnMacOS) {
       helpAboutMenuItem = new javax.swing.JMenuItem();
       helpAboutMenuItem.setText("About " + programName);
       helpAboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
           handleAbout();
         }
       });
       helpMenu.addSeparator();
       helpMenu.add(helpAboutMenuItem);
     }
   } // end method
   
   /**
     The passed menu item will have the ctrl-H key set as an accelerator key, if
     we are not running on a Mac. 
    
     @param helpMenuItem JMenuItem acting as the Help menu.
    */
   public void setHelpMenuItem (JMenuItem helpMenuItem) {
     
     this.helpMenuItem = helpMenuItem;
     
     // If not running on a Mac, then use ctrl-H as an accelerator key for Help
     // (On Mac OS X, H hides the current application)
     if (! runningOnMacOS) {
       helpMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_H,
           Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
     } // end if not running on a Mac
     
   } // end method
   
   /** 
     Standard way to respond to an application launch on a Mac. 
    */
   public void handleOpenApplication (File inFile) {
     if (xHandler != null) {
       // xHandler.handleOpenApplication ();
     }
   }
   
   /**
      Standard way to respond to an About Menu Item Selection on a Mac.
    */
   public void handleAbout() {
     if (xHandler != null) {
       xHandler.handleAbout();
     }
   }
   
   /**
      Standard way to respond to a Preferences Item Selection on a Mac.
    */
   public void handlePreferences() {
     if (xHandler != null) {
       xHandler.handlePreferences();
     }
   }
   
   /**
     Standard way to respond to a document being passed to this application 
     on a Mac.
    
     @param inFile File to be opened. 
    */
   public void handleOpenFile (File inFile) {
     if (xHandler != null) {
       xHandler.handleOpenFile (inFile);
     }
   }
 
   /**
    Standard way to respond to a URI being passed to this application on a Mac.
 
    @param inURI The URI to be opened or otherwise processed. 
    */
   public void handleOpenURI (URI inURI) {
     if (xHandler != null) {
       xHandler.handleOpenURI (inURI);
     }
   }
 
   /**
     Standard way to respond to a document print request being passed to this 
     application on a Mac.
    
     @param inFile File to be opened. 
    */
   public void handlePrintFile (File inFile) {
     if (xHandler != null) {
       xHandler.handlePrintFile (inFile);
     }
   }
   
   /**
      Standard way to respond to a Quit Menu Item on a Mac.
    */
   public void handleQuit() {	
     if (xHandler != null) {
       xHandler.handleQuit();
     } else {
       System.exit (0);
     }
   }  // end handleQuit method
   
   /**
     See if we are running on a Macintosh.
    
     @return True if we are running on a Macintosh.
    */
   public boolean isRunningOnMacOS () {
     return runningOnMacOS;
   }
 
   /**
     Set the user's preferences for where the menu bar is displayed. On a 
     Mac, it can be at the top of the screen, in addition to the standard
     position at the top of the main program window.
    
     @param menuAtTop True if user wants the menu at the top of the screen,
                      false if the user prefers it at the top of the window.
    */
   public void setMenuAtTopOfScreen (boolean menuAtTop) {
     if (runningOnMacOS) {
       if (menuAtTop) {
         setPref (MENU_LOCATION_KEY, MENU_AT_TOP_OF_SCREEN);
         System.setProperty (USE_SCREEN_MENU_BAR_1, TRUE);
         System.setProperty (USE_SCREEN_MENU_BAR_2, TRUE);
         modifyInfoPlist ("apple.laf.useScreenMenuBar", true);
       } else {
         setPref (MENU_LOCATION_KEY, MENU_AT_TOP_OF_WINDOW);
         System.setProperty (USE_SCREEN_MENU_BAR_1, FALSE);
         System.setProperty (USE_SCREEN_MENU_BAR_2, FALSE);
         modifyInfoPlist ("apple.laf.useScreenMenuBar", false);
       }
     } // end if running on a Mac
   }
   
   /**
     Set a preference for brushed metal.
    
     @param brushedMetal True if user wants brushed metal, false otherwise.
    */
   public void setBrushedMetal (boolean brushedMetal) {
     if (isRunningOnMacOS()) {
       if (brushedMetal) {
         System.setProperty (BRUSHED_METAL, TRUE);
         setPref (BRUSHED_METAL_KEY, TRUE);
       } else {
         System.setProperty (BRUSHED_METAL, FALSE);
         setPref (BRUSHED_METAL_KEY, FALSE);
       }
       
       modifyInfoPlist ("apple.awt.brushMetalLook", brushedMetal);
 
     } // end if running on a Mac
   } // end method
   
   public boolean modifyInfoPlist (String name, boolean value) {
     boolean ok = false;
     File infoPlistIn = new File (userDirString + "/" + programNameNoSpace 
         + ".app" + "/Contents", "Info.plist");
     if (infoPlistIn.exists()) {
       if (infoPlistIn.isFile()) {
         File infoPlistOut = new File (userDirString + "/" + programNameNoSpace 
             + ".app" + "/Contents", "Info_new.plist");
         File infoPlistOld = new File (userDirString + "/" + programNameNoSpace 
             + ".app" + "/Contents", "Info_old.plist");
         XTextFile in = new XTextFile (infoPlistIn); 
         XTextFile out = new XTextFile (infoPlistOut); 
         ok = true;
         try {
           in.openForInput(); 
           out.openForOutput(); 
         } catch (Exception e) {
           ok = false;
         }
         if (ok) {
           String line = "";;
           boolean nextLine = false;
           try {
             line = in.readLine(); 
           } catch (Exception e) {
             ok = false;
           }
           while ((! in.isAtEnd()) && ok) { 
             if (nextLine) {
               StringBuffer modLine = new StringBuffer(line);
               int valueIndex = modLine.indexOf ("true");
               int valueLength = 4;
               if (valueIndex < 0) {
                 valueIndex = modLine.indexOf ("false");
                 valueLength = 5;
               }
               if (valueIndex >= 0) {
                 modLine.delete (valueIndex, valueIndex + valueLength);
                 if (value) {
                   modLine.insert (valueIndex, TRUE);
                 } else {
                   modLine.insert (valueIndex, FALSE);
                 }
                 line = modLine.toString();
               } // end if we found true or false
             } // end if this is the line after the brushMetalLook key line
             nextLine = false;
             if (line.indexOf ("<key>" + name + "</key>") >= 0) {
               nextLine = true;
             }
             try {
               out.writeLine(line); 
             } catch (Exception e) {
               ok = false;
             }
             try {
               line = in.readLine(); 
             } catch (Exception e) {
               ok = false;
             }
           } 
           try {
             in.close(); 
             out.close();
           } catch (Exception e) {
             ok = false;
           }
           if (ok) {
             if (infoPlistOld.exists()) {
               infoPlistOld.delete();
             }
             infoPlistIn.renameTo (infoPlistOld);
             infoPlistIn = new File (userDirString + "/" + programNameNoSpace 
                 + ".app" + "/Contents", "Info.plist");
             infoPlistOut.renameTo (infoPlistIn);
           } else {
             System.out.println 
                 ("File/IO Exception while modifying Info.plist file");
           } // end if not ok
         } // end if not ok earlier
       } // end if Info.plist is a file
     } // end if Info.plist exists
     return ok;
   }
   
   public File getMacAppBundleContents () {
     if (isRunningOnMacOS() && userDirString.length() > 0) {
       return new File (userDirString + "/" + programNameNoSpace 
             + ".app" + "/Contents");
     } else {
       return null;
     }
   }
   
   /**
     Get the user's preference for a brushed metal appearance.
    
     @return True if user has expressed a preference for brushed metal,
             false otherwise. 
    */
   public boolean getBrushedMetal () {
     String pref = getPref(BRUSHED_METAL_KEY, FALSE).toLowerCase();
     return (pref.startsWith ("t") || pref.startsWith("y"));
   }
   
   /**
     Get the user preferred look and feel class name.
    
     @return Look and Feel Class Name preferred by user for this program,
             if one has been specified, otherwise the literal "null".
    */
   public String getLookAndFeelClassName () {
     return getPref (LOOK_AND_FEEL_KEY, "null");
   }
   
   /**
     Set the user preferred look and feel class name.
    
     @param lookAndFeelClassName Look and Feel Class Name preferred by user 
                                 for this program, if one has been specified, 
                                 otherwise the literal "null".
    */
   public void setLookAndFeelClassName (String lookAndFeelClassName) {
     setPref (LOOK_AND_FEEL_KEY, lookAndFeelClassName);
   }
   
   /**
     Get the user preferred line separators to be used.
    
     @return String containing the line separators 
             to be used.
    */
   public String getLineSep () {
     return prefLineSep;
   }
   
   /**
     Get the user preferred line separator platform to be used.
    
     @return String identifying the platform for which line separators 
             should be used.
    */
   public String getLineSepPlatform () {
     prefLineSepPlatform = getPref (LINE_SEP_KEY, sysLineSep);
     prefLineSep = getLineSep (prefLineSepPlatform);
     return prefLineSepPlatform;
   }
   
   /**
     Get the line separators based on a passed platform identifier.
    
     @return String containing the line separators 
             to be used.
     @param  lineSepPlatform String identifying the platform for which line separators
             are desired.
    */
   public static String getLineSep (String lineSepPlatform) {
     if (lineSepPlatform.equals (LINE_SEP_PLATFORM_MAC)) {
       return LINE_SEP_STRING_MAC;
     }
     else
     if (lineSepPlatform.equals (LINE_SEP_PLATFORM_UNIX)) {
       return LINE_SEP_STRING_UNIX;
     }
     else
     if (lineSepPlatform.equals (LINE_SEP_PLATFORM_DOS)) {
       return LINE_SEP_STRING_DOS;
     }
     else {
       return LINE_SEP_STRING_UNIX;
     }
   }
   
   /**
     Set the user preferred line separator platform.
    
     @param lineSepPlatform The platform for which line separators are
                            desired.
    */
   public void setLineSepPlatform (String lineSepPlatform) {
     if (lineSepPlatform.equals (LINE_SEP_PLATFORM_MAC)
         || lineSepPlatform.equals (LINE_SEP_PLATFORM_UNIX)
         || lineSepPlatform.equals (LINE_SEP_PLATFORM_DOS)) {
       setPref (LINE_SEP_KEY, lineSepPlatform);
       prefLineSepPlatform = lineSepPlatform;
       prefLineSep = getLineSep (lineSepPlatform);
     }
   }
   
   /**
     Based on the passed line separator string, return a string indicating
     the typical platform on which these will be found.
    
     @return String naming the typical platform for these characters.
     @param         lineSep The line separator character(s) used on a platform.
    */
   public static String getLineSepPlatform (String lineSep) {
     if (lineSep.equals (LINE_SEP_STRING_MAC)) {
       return LINE_SEP_PLATFORM_MAC;
     }
     else
     if (lineSep.equals (LINE_SEP_STRING_UNIX)) {
       return LINE_SEP_PLATFORM_UNIX;
     }
     else
     if (lineSep.equals (LINE_SEP_STRING_DOS)) {
       return LINE_SEP_PLATFORM_DOS;
     }
     else {
       return "???";
     }
   }
   
   /**
     Return the Preferences node for the system.
    
     @return Preferences node for the system.
    */
   public Preferences getSystemPreferences () {
     return systemPreferences;
   }
   
   /**
     Return the Preferences node for the user.
    
     @return Preferences node for the user.
    */
   public Preferences getUserPreferences () {
     return userPreferences;
   }
   
   /**
     Return the Preferences path for this program.
    
     @return Preferences path for the program.
    */
   public String getPreferencesPath () {
     return (SLASH + domainLevel2 
         + SLASH + domainLevel1
         + SLASH + programNameNoSpace);
   }
   
   /**
     Return the name of the executing program.
    
     @return Program name.
    */
   public String getProgramName () {
     return programName;
   }
   
   /**
     Return the name of the executing program.
    
     @return Program name in all lower-case letters.
    */
   public String getProgramNameLower () {
     return programNameLower;
   }
   
   /**
     Return the name of the executing program.
    
     @return Program name in all lower-case letters and with any spaces
             removed.
    */
   public String getProgramNameNoSpace () {
     return programNameNoSpace;
   }
   
   /** 
     Returns a user preference string, based on the passed key. 
    
     @param key The string to be used as a key to identify this value.
     @return The desired value, if found.
    */  
   public String getPref (String key) {
     String pref = "";
     if (userPreferences != null) {
       pref = userPreferences.get(key, "");
     }
     return pref;
   }
   
   /** 
     Returns a user preference string, based on the passed key and
     default value. 
    
     @param key The string to be used as a key to identify this value.
     @param defaultValue Value to be returned if the specified key cannot 
                         be found. 
     @return The desired value, if found.
    */  
   public String getPref (String key, String defaultValue) {
     String pref = defaultValue;
     if (userPreferences != null) {
       pref = userPreferences.get(key, defaultValue);
     }
     return pref;
   }
   
   /** 
     Returns a user preference string, based on the passed key and
     default value. 
    
     @param key The string to be used as a key to identify this value.
     @param defaultValue Value to be returned if the specified key cannot 
                         be found. 
     @return The desired value, if found.
    */  
   public int getPrefAsInt (String key, int defaultValue) {
     int prefInt = defaultValue;
     if (userPreferences != null) {
       prefInt = userPreferences.getInt (key, defaultValue);
     }
     return prefInt;
   }
   
   /** 
     Stores a user preference string, based on the passed key. 
    
     @param key  The key of the data to be stored.
     @param data The data to be associated with the passed key.
    */  
   public void setPref (String key, String data) {
     userPreferences.put (key, data);
   }
   
   /** 
     Stores a user preference string, based on the passed key. 
    
     @param key  The key of the data to be stored.
     @param data The data to be associated with the passed key, in the
                 form of an integer.
    */ 
   public void setPref (String key, int data) {
     userPreferences.putInt (key, data);
   }
   
   /**
      Replaces a from String, when found in an input String, with a to
      String. All occurrences of the from String will be replaced. The from
      String and the to String may be different lengths. 
     
      @return Input string, but with any occurrences of the from string
              replaced with the to string.
     
      @param inString   String to be converted.
     
      @param fromString String to be replaced. 
     
      @param toString   Replacement string. 
    */
   public static String replaceString 
       (String inString, String fromString, String toString) {
     int i = 0;
     int fromLength = fromString.length();
     int toLength = toString.length();
 		StringBuffer s = new StringBuffer (inString);
     while (i <= (s.length() - fromLength)) {
       // System.out.println ("i = " + String.valueOf(i));
       // System.out.println ("fromLength = " + String.valueOf(fromLength));
       // System.out.println ("fromString = " + fromString);
       // System.out.println ("inString = " + inString);
       if (fromString.equals (s.substring (i, i + fromLength))) {
         s.replace (i, i + fromLength, toString);
         i = i + toLength;
       } else {
         i++;
       }
     }
 		return s.toString();
 	} // end of method replaceString
   
   /**
      Modifies the demarcation of words within a string.
     
      @return            A String with modified demarcation between words.
     
      @param  inStr      The input string. It will be broken up into words by 
                         looking for punctuation or white space between words, 
                         or transitions from lower- to upper-case. 
     
      @param  delimiter  Something to be inserted between words. May contain 
                         zero, one or more characters. 
     
      @param  firstCase  Indicates whether first letter of first word should be
                         forced to upper-case, forced to lower-case, or left as-is. <ul> <li>
                         1 = Forced to upper. <li>
                         0 = Left as-is. <li>
                        -1 = Forced to lower. </ul>
     
      @param  leadingCase  Indicates whether first letters of remaining words should be
                           forced to upper-case, forced to lower-case, or left as-is. <ul> <li>
                           1 = Forced to upper. <li>
                           0 = Left as-is. <li>
                          -1 = Forced to lower. </ul>
     
      @param  normalCase   Indicates whether remaining letters of words should be
                           forced to upper-case, forced to lower-case, or left as-is. <ul> <li>
                           1 = Forced to upper. <li>
                           0 = Left as-is. <li>
                          -1 = Forced to lower. </ul>
    */
   public static String wordDemarcation 
       (String inStr, String delimiter, int firstCase, int leadingCase, int normalCase) {
     StringBuffer workBuf = new StringBuffer ();
     char c;
     boolean charUpper = false;
     boolean lastCharUpper = true;
     boolean newWord = true;
     int wordCount = 0;
     int newCase = 0;
     int length = inStr.length();
     for (int i = 0; i < length; i++) {
       lastCharUpper = charUpper;
       c = inStr.charAt (i);
       newCase = normalCase;
       charUpper = false;
       if (Character.isUpperCase (c)) {
         charUpper = true;
       }
       if (Character.isLetterOrDigit (c)) {
         if (charUpper && (! lastCharUpper)) {
           newWord = true;
         }
         if (newWord) {
           wordCount++;          
           if (wordCount == 1) {
             newCase = firstCase;
           } else {
             newCase = leadingCase;
             workBuf.append (delimiter);
           }
           newWord = false;
         } // end if new word
         if (newCase > 0) {
           workBuf.append (Character.toUpperCase (c));
         }
         else
         if (newCase < 0) {
           workBuf.append (Character.toLowerCase (c));
         } else {
           workBuf.append (c);
         }
       } else {
         newWord = true;
       }
     } // end for every character in input string
     return workBuf.toString();
   } // end of wordDemarcation method
   
   /**
     Ensures that the operating system will treat a file as a standard
     text file. When running on a Mac, the file will be given an appropriate
     file type and creator ("ttxt").
    
     @return       True if everything went ok, false if there were any problems.
     @param file   A File that has already been created. 
    */
   public boolean designateAsTextFile (File file) {  
     if (runningOnMacOS) {
       boolean typeOK = setFileTypeText (file);
       boolean creatorOK = setFileCreator (file, 1128484686);
       return (typeOK && creatorOK);
     } else {
       return true;
     }
   } // end method
   
   /**
     Sets the file type to that of a text file when running on a Mac. 
    
     @return       True if everything went ok, false if there were any problems.
     @param file   A File that has already been created. 
    */
   public boolean setFileTypeText (File file) {  
     boolean ok = true;
     if (runningOnMacOS) {
       try {
         FileManager.setFileType (file.getPath(), 1413830740);
       } catch (IOException e) {
         ok = false;
       }
     } // end if Mac
     return ok;
   } // end method
   
   /**
     Sets the file type when running on a Mac. 
    
     @return       True if everything went ok, false if there were any problems.
     @param file   A File that has already been created. 
     @param type   A standard Mac OS File Type.
    */
   public boolean setFileType (File file, int type) {  
     boolean ok = true;
     if (runningOnMacOS) {
       try {
         FileManager.setFileType (file.getPath(), type);
       } catch (IOException e) {
         ok = false;
       }
     } // end if Mac
     return ok;
   } // end method
   
   /**
     Sets the file creator when running on a Mac. 
    
     @return         True if everything went ok, false if there were any problems.
     @param file     A File that has already been created. 
     @param creator  A standard Mac OS File creator.
    */
   public boolean setFileCreator (File file, int creator) {  
     boolean ok = true;
     if (runningOnMacOS) {
       try {
         FileManager.setFileCreator (file.getPath(), creator);
       } catch (IOException e) {
         ok = false;
       }
     } // end if Mac
     return ok;
   } // end method
   
   /**
     Explicityly set button type when running on a Mac. 
    
     @param button Button whose type is to be set. 
     @param buttonType May be "toolbar", "icon" or "text".
    */
   public void setButtonType (JButton button, String buttonType) {
     if (runningOnMacOS) {
       button.putClientProperty("JButton.buttonType",buttonType);
     }
   }
   
 } // end class
 
 
