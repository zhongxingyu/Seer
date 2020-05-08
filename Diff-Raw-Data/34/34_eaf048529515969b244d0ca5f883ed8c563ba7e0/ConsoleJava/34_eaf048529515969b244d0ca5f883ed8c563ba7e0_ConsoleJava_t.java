 // Copyright Hugh Perkins 2009
 // hughperkins@gmail.com http://manageddreams.com
 //
 // This program is free software; you can redistribute it and/or modify it
 // under the terms of the GNU General Public License as published by the
 // Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 // or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 //  more details.
 //
 // You should have received a copy of the GNU General Public License along
 // with this program in the file licence.txt; if not, write to the
 // Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-
 // 1307 USA
 // You can find the licence also on the web at:
 // http://www.opensource.org/licenses/gpl-license.php
 //
 // ======================================================================================
 //
 
 package hughai.ui;
 
 import java.util.*;
 import java.util.List;
 import java.util.jar.JarOutputStream;
 import java.util.zip.ZipEntry;
 
 import javax.activation.FileTypeMap;
 import javax.swing.*;
 import javax.script.*;
 import javax.xml.transform.stream.StreamResult;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 import hughai.*;
 import hughai.loader.utils.Loader;
 import hughai.utils.*;
 
 // provides a console tab to the gui that lets us execute code
 // on the fly!
 // is that wicked or wot! :-D
 public class ConsoleJava {
    final String classdir = "console-classes";
    final String jarfilename = "Console.jar";
    final String consoletemplatefilename = "JavaConsoleTemplate.txt";
    
 //   final String classpath="$aidir/SkirmishAI.jar:$aidir/UnderlyingAI.jar:$aidir/../../Interfaces/Java/0.1/AIInterface.jar"; 
 
 //   JFrame frame;
    JPanel textpanel;
    GridLayout gridLayout;
    JTextArea textarea;
    JButton gobutton;
    JButton quitbutton;
 
    JTextArea outputTextarea;
    
    PlayerObjects playerObjects;
    
    public ConsoleJava( PlayerObjects playerObjects ) {
       this.playerObjects = playerObjects;
       
       init();
    }
    
    void init () {
       try {
 //      frame = new JFrame("Console");
 //      frame.setSize( 400, 400 );
          
          JPanel outerpanel = new JPanel(new BorderLayout());
 
       gridLayout = new GridLayout( 2, 1 );
       textpanel = new JPanel( gridLayout );
 //      frame.add( panel );
       
       JPanel buttonpanel = new JPanel(new GridLayout(1,2));
       
       outerpanel.add( "Center", textpanel );
       outerpanel.add( "South", buttonpanel );
 
       textarea = new JTextArea();
       JScrollPane scrollPane = new JScrollPane( textarea );
 
       outputTextarea = new JTextArea();
       JScrollPane outputscrollpane = new JScrollPane( outputTextarea );
 
       gobutton = new JButton( "Go" );
       quitbutton = new JButton( "Quit" );
       String templatepath = playerObjects.getCSAI().getAIDirectoryPath() + consoletemplatefilename;
       FileHelper fileHelper = new FileHelper( playerObjects );
       String initialFile = fileHelper.readFile( templatepath );
       if( initialFile != null ) {
          textarea.setText( initialFile );
       } else {
          textarea.setText("<Missing file " + templatepath + ">" );
       }
 
       gobutton.addActionListener( new GoButton() );
       quitbutton.addActionListener( new QuitButton() );
 
       textpanel.add( scrollPane );
       textpanel.add( outputscrollpane );
       buttonpanel.add( gobutton );
       buttonpanel.add( quitbutton );
 
       playerObjects.getMainUI().addPanelToTabbedPanel( "Java Console", outerpanel );
 //      frame.validate();
 //      frame.setVisible( true );
       } catch( Exception e ) {
          playerObjects.getLogFile().WriteLine( Formatting.exceptionToStackTrace( e ) );
          throw new RuntimeException( e );
       }
    }
 
    // kind of hacky quick fix for linux vs Windows
    // there must be a better way of doing this...
    String localizeClassPath( String classpath ) {
       if( File.separator.equals("/") ) { // linux (and mac?)
          return classpath.replace( ";", ":" ).replace( "\\", "/" );
       } else {  // windows
          return classpath.replace( ":", ";" ).replace( "/", "\\" );         
       }
    }
    
    void debug( Object message ) {
       playerObjects.getLogFile().WriteLine( "" + message );
    }
 
    class GoButton implements ActionListener {
       @Override
       public void actionPerformed( ActionEvent event ) {
          try {
 //            String ourdir = "/home/user/persist/workspace/Test/";
             String ourdir = playerObjects.getCSAI().getAIDirectoryPath();
 
             System.out.println( textarea.getText() );
             new File( ourdir + "src-console" + File.separator + "console").mkdirs();
             PrintWriter printWriter = new PrintWriter( 
                   ourdir
                   + "src-console" + File.separator
                   + "console" + File.separator
                   + "ConsoleText.java" );
             printWriter.write( textarea.getText() );
             printWriter.write( "\n" );
             printWriter.close();
 
             new File( ourdir + classdir ).mkdirs();
             //            Process process = 
             //               Runtime.getRuntime().exec( "bash -c pwd", null,
             //                     null );
             
             String classpath = localizeClassPath( 
                   playerObjects.getConfig().getConsoleclasspath() );
             classpath = classpath.replace( "$aidir/", ourdir );
             
             Exec exec = new Exec( playerObjects );
            String result = "";
            try {
               result = exec.exec( "javac -classpath " + classpath
                         + " -d " + ourdir + classdir + 
                         " console" + File.separator + "ConsoleText.java", 
                        ourdir + "src-console" );
               debug( result );
            } catch( Exception e ) {
               outputTextarea.setText( "Error during compilation: " + e.getMessage() );
               return;
            }
 
            try {
               result = exec.exec( "jar -cf " + ourdir + jarfilename 
                         + " console", 
                        ourdir + classdir );
               debug( result );
            } catch( Exception e ) {
               outputTextarea.setText( "Error during jar creation: " + e.getMessage() );
               return;
            }
 
             // this should be moved to some generic class really
             URL[] locations = new URL[] { new File( ourdir + jarfilename )
             .toURI().toURL()};
             ClassLoader baseClassLoader = ConsoleJava.class.getClassLoader();
             if( baseClassLoader == null ) {
                System.out.println("using system classloader as base");
                baseClassLoader = ClassLoader.getSystemClassLoader();
             } else {
                System.out.println("using our classloader as base");         
             }
             URLClassLoader classloader = new URLClassLoader(
                   locations, baseClassLoader );
             Class<?> cls = classloader.loadClass("console.ConsoleText");
             if (!ConsoleEntryPoint.class.isAssignableFrom(cls)) {
                throw new RuntimeException("Invalid class");
             }
             Object newInstance = cls.newInstance(); 
             ConsoleEntryPoint subjar = (ConsoleEntryPoint)newInstance;
            try {
               result = subjar.go( playerObjects );
               outputTextarea.setText( result );
            } catch( Exception e ) {
               String exceptiontrace = Formatting.exceptionToStackTrace( e );
               outputTextarea.setText( exceptiontrace );               
            }
          } catch( Exception e ) {
             playerObjects.getLogFile().WriteLine( Formatting.exceptionToStackTrace( e ) );
          }
       }      
    }
 
    class QuitButton implements ActionListener {
       @Override
       public void actionPerformed( ActionEvent event ) {
          System.exit(0);
       }      
    }
 }
