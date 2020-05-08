 // CyMain.java
 
 /** Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute
  **
  ** This library is free software; you can redistribute it and/or modify it
  ** under the terms of the GNU Lesser General Public License as published
  ** by the Free Software Foundation; either version 2.1 of the License, or
  ** any later version.
  **
  ** This library is distributed in the hope that it will be useful, but
  ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  ** documentation provided hereunder is on an "as is" basis, and the
  ** Institute for Systems Biology and the Whitehead Institute
  ** have no obligations to provide maintenance, support,
  ** updates, enhancements or modifications.  In no event shall the
  ** Institute for Systems Biology and the Whitehead Institute
  ** be liable to any party for direct, indirect, special,
  ** incidental or consequential damages, including lost profits, arising
  ** out of the use of this software and its documentation, even if the
  ** Institute for Systems Biology and the Whitehead Institute
  ** have been advised of the possibility of such damage.  See
  ** the GNU Lesser General Public License for more details.
  **
  ** You should have received a copy of the GNU Lesser General Public License
  ** along with this library; if not, write to the Free Software Foundation,
  ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  **/
 
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------------------
 package cytoscape;
 //-------------------------------------------------------------------------------------
 import java.awt.*;
 import java.awt.geom.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.*;
 
 import java.util.*;
 import java.util.logging.*;
 import javax.swing.Timer;
 
 import cytoscape.data.*;
 import cytoscape.data.readers.GMLReader;
 import cytoscape.data.readers.InteractionsReader;
 import cytoscape.data.readers.GraphReader;
 import cytoscape.data.servers.*;
 import cytoscape.view.CyWindow;
 import cytoscape.view.CytoscapeDesktop;
 import cytoscape.util.shadegrown.WindowUtilities;
 
 
 import com.jgoodies.plaf.FontSizeHints;
 import com.jgoodies.plaf.LookUtils;
 import com.jgoodies.plaf.Options;
 import com.jgoodies.plaf.plastic.Plastic3DLookAndFeel;
 
 //------------------------------------------------------------------------------
 /**
  * This is the main startup class for Cytoscape. It creates a CytoscapeConfig
  * object using the command-line arguments, uses the information in that config
  * to create other data objects, and then constructs the first CyWindow.
  * Construction of that class triggers plugin loading, and after that control
  * passes to the UI thread that responds to user input.<P>
  *
  * This class monitors the set of windows that exist and exits the application
  * when the last window is closed.
  */
 public class CyMain implements WindowListener {
   protected Vector windows = new Vector ();
   protected CyWindow cyWindow;
   protected CytoscapeVersion version = new CytoscapeVersion();
   protected Logger logger;
    
   /**
    * Primary Method for Starting Cytoscape. Use the passed
    * args to create a CytoscapeConfig object.
    */
   public CyMain ( String [] args ) throws Exception {
     
 
     // setup the Splash Screen
    ImageIcon image = new ImageIcon( getClass().getResource("images/cytoSplash.gif") );
     WindowUtilities.showSplash( image, 8000 );
 
     //parse args and config files into config object
     CytoscapeConfig config = new CytoscapeConfig(args);
    
     //handle special cases of arguments
     if ( config.helpRequested() )
       displayHelp( config );
     else if ( config.inputsError() ) 
       inputError( config );
     else if ( config.displayVersion() )
       displayHelp( config );
 
     //set up the logger
     setupLogger(config);
     logger.info(config.toString());
          
 
     //create the global CytoscapeObj object
     CytoscapeObj cytoscapeObj = new CytoscapeObj(this, config, logger, null);
     Cytoscape.setCytoscapeObj( cytoscapeObj );
     BioDataServer bioDataServer = Cytoscape.getCytoscapeObj().getBioDataServer();
     //try to create a bioDataServer
     //String bioDataDirectory = config.getBioDataDirectory();
     //BioDataServer bioDataServer = Cytoscape.loadBioDataServer( bioDataDirectory );// null;
 //     if ( bioDataDirectory != null ) {
 //       try {
 //         bioDataServer = new BioDataServer( bioDataDirectory );
 //       } catch ( Exception e ) {
 //         logger.severe( "Unable to load bioDataServer from '" + bioDataDirectory + "'" );
 //         logger.severe( e.getMessage() );
 //         e.printStackTrace();
 //       }
 //     }
 
   
     // general setup
     cytoscapeObj.setViewThreshold( config.getViewThreshold() );
 
 
 
     //get some standard fields for doing name resolution
     boolean canonicalize = Semantics.getCanonicalize( cytoscapeObj );
     String defaultSpecies = Semantics.getDefaultSpecies( null, cytoscapeObj );
   
 
     //String cp =  System.getProperty("java.class.path",".");
     //System.out.println( "User classpath: "+cp );
     
 
 
     //TODO: make CytoscapeDesktop find out about everything loaded when it starts up.
     //CytoscapeDesktop cd = new CytoscapeDesktop();
     Cytoscape.getDesktop();
     Cytoscape.getDesktop().setupPlugins();
 
     // Load all requested networks
     Iterator gi = config.getGeometryFilenames().iterator();
     Iterator ii = config.getInteractionsFilenames().iterator();
     while ( gi.hasNext() ) {
       CyNetwork network = Cytoscape.createNetwork( (String)gi.next(),
                                                    Cytoscape.FILE_GML,
                                                    false,
                                                    null,
                                                    null );
       if ( network.getNodeCount() < cytoscapeObj.getViewThreshold() )
         Cytoscape.createNetworkView( network );
     }
     while ( ii.hasNext() ) {
       CyNetwork network = Cytoscape.createNetwork( (String)ii.next(),
                                Cytoscape.FILE_SIF,
                                canonicalize,
                                bioDataServer,
                                defaultSpecies );
       
       if ( network.getNodeCount() < cytoscapeObj.getViewThreshold() )
         Cytoscape.createNetworkView( network );
     }
 
      
     //TODO: move to Cytoscape?
     //add the semantics we usually expect
     //Semantics.applyNamingServices(network, cytoscapeObj);
 
 
     //load any specified data attribute files
     logger.info("reading attribute files");
     Cytoscape.loadAttributes( config.getNodeAttributeFilenames(),
                               config.getEdgeAttributeFilenames(),
                               canonicalize, 
                               bioDataServer, 
                               defaultSpecies);
     logger.info(" done");
 
 
     
 
     // load expression data if specified
     String expDataFilename = config.getExpressionFilename();
     if (expDataFilename != null) {
        logger.info("reading " + expDataFilename + "...");
        try {
          Cytoscape.loadExpressionData( expDataFilename, config.getWhetherToCopyExpToAttribs() );
        } catch (Exception e) {
          logger.severe("Exception reading expression data file '" + expDataFilename + "'");
          logger.severe(e.getMessage());
          e.printStackTrace();
        }
        logger.info("  done");
     }
 
 
     WindowUtilities.hideSplash();
 
   } // ctor
   
 
   protected void displayHelp ( CytoscapeConfig config ) {
     System.out.println(version);
     System.out.println(config.getUsage());
     exit(0);
   }
 
   protected void inputError ( CytoscapeConfig config  ) {
     System.out.println(version);
     System.out.println("------------- Inputs Error");
     System.out.println(config.getUsage ());
     System.out.println(config);
     exit(1);
   }
 
   /**
    * configure logging:  cytoscape.props specifies what level of logging
    * messages are written to the console; by default, only SEVERE messages
    * are written.  in time, more control of logging (i.e., optional logging
    * to a file, disabling console logging, per-window or per-plugin logging)
    * can be provided
    */
   protected void setupLogger (CytoscapeConfig config) {
     logger = Logger.getLogger("global");
     Properties properties = config.getProperties();
     String level = properties.getProperty("logging", "SEVERE");
 
     if (level.equalsIgnoreCase("severe")) {
       logger.setLevel(Level.SEVERE);
     } else if (level.equalsIgnoreCase("warning")) {
       logger.setLevel(Level.WARNING);
     } else if (level.equalsIgnoreCase("info")) {
       logger.setLevel(Level.INFO);
     } else if (level.equalsIgnoreCase("config")) {
       logger.setLevel(Level.CONFIG);
     } else if (level.equalsIgnoreCase("all")) {
       logger.setLevel(Level.ALL);
     } else if (level.equalsIgnoreCase("none")) {
       logger.setLevel(Level.OFF);
     } else if (level.equalsIgnoreCase("off")) {
       logger.setLevel(Level.OFF);
     }
   }
  
   public void windowActivated   (WindowEvent e) {
    
   }
   //------------------------------------------------------------------------------
   /**
    * on linux (at least) a killed window generates a 'windowClosed' event; trap that here
    */
   public void windowClosing     (WindowEvent e) {windowClosed (e);}
   public void windowDeactivated (WindowEvent e) {}
   public void windowDeiconified (WindowEvent e) {}
   public void windowIconified   (WindowEvent e) {}
 
   //------------------------------------------------------------------------------
   public void windowOpened      (WindowEvent e) {
     windows.add (e.getWindow ());
   }
   //------------------------------------------------------------------------------
   public void windowClosed     (WindowEvent e) {
     Window window = e.getWindow();
     if (windows.contains(window)) {windows.remove (window);}
 
     if (windows.size () == 0) {
       logger.info("all windows closed, exiting...");
       exit(0);
     }
   }
 
   public CyWindow getMainWindow()
   {
     return cyWindow;
   }
   //------------------------------------------------------------------------------
   public void exit(int exitCode) {
     for (int i=0; i < windows.size (); i++) {
       Window w = (Window) windows.elementAt(i);
       w.dispose();
     }
     System.exit(exitCode);
   }
   //------------------------------------------------------------------------------
   public static void main(String args []) throws Exception {
 
     UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
     Options.setGlobalFontSizeHints(FontSizeHints.MIXED);
     Options.setDefaultIconSize(new Dimension(18, 18));
 
     try {
       if ( LookUtils.isWindowsXP() ) {
         // use XP L&F
         UIManager.setLookAndFeel( Options.getSystemLookAndFeelClassName() );
       } else if ( System.getProperty("os.name").startsWith( "Mac" ) ) {
         // do nothing, I like the OS X L&F
       } else {
         // this is for for *nix
         // I happen to like this color combo, there are others
       
         // GTK
         //UIManager.setLookAndFeel( "com.sun.java.swing.plaf.gtk.GTKLookAndFeel" );
 
 
         // jgoodies
         Plastic3DLookAndFeel laf = new Plastic3DLookAndFeel();
         laf.setTabStyle( Plastic3DLookAndFeel.TAB_STYLE_METAL_VALUE );
         laf.setHighContrastFocusColorsEnabled(true);
         laf.setMyCurrentTheme( new com.jgoodies.plaf.plastic.theme.ExperienceBlue() );
         UIManager.setLookAndFeel( laf );
         
       }
     } catch (Exception e) {
       System.err.println("Can't set look & feel:" + e);
     }
 
 
 
     CyMain app = new CyMain(args);
   } // main
   //------------------------------------------------------------------------------
 }
 
