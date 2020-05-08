 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *         research group.
  *  
  *   Copyright 2005-2010 <e-UCM> research group.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *         http://e-adventure.e-ucm.es/contributors
  * 
  *   <e-UCM> is a research group of the Department of Software Engineering
  *         and Artificial Intelligence at the Complutense University of Madrid
  *         (School of Computer Science).
  * 
  *         C Profesor Jose Garcia Santesmases sn,
  *         28040 Madrid (Madrid), Spain.
  * 
  *         For more info please visit:  <http://e-adventure.e-ucm.es> or
  *         <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  *     <e-Adventure> is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     <e-Adventure> is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU Lesser General Public License for more details.
  * 
  *     You should have received a copy of the GNU Lesser General Public License
  *     along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.common.auxiliar;
 
 import java.awt.BorderLayout;
import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.InvalidPropertiesFormatException;
 
 import javax.mail.AuthenticationFailedException;
 import javax.mail.MessagingException;
 import javax.sound.midi.InvalidMidiDataException;
 import javax.sound.midi.MidiUnavailableException;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.text.BadLocationException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import es.eucm.eadventure.common.gui.TC;
 
 /**
  * This class has methods used to generate and send an error or comments report
  * to the e-adventure server.
  * 
  * @author Eugenio Marchiori
  */
 public class ReportDialog extends JDialog {
 
     /**
      * Default serial version UID
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * The width of the panel
      */
     private static int PANEL_WIDTH = 500;
 
     /**
      * The height of the panel
      */
     private static int PANEL_HEIGHT = 500;
 
     /**
      * The text field for the name
      */
     private JTextField nameTextField;
 
     /**
      * The text field for the email
      */
     private JTextField emailTextField;
 
     /**
      * The area where to write the description
      */
     private JTextArea descriptionTextArea;
 
     /**
      * The send button
      */
     private JButton sendButton;
 
     /**
      * The don't send or cancel button
      */
     private JButton dontSendButton;
 
     /**
      * Boolean indicating if the panel should ask for the name
      */
     private boolean askName;
 
     /**
      * The string with the exception
      */
     private String exception = "";
 
     /**
      * The string with the message
      */
     private String message = "";
 
     /**
      * The string with the os
      */
     private String os;
 
     /**
      * The string with the java version
      */
     private String java;
 
     /**
      * The string with the release
      */
     private String release;
 
     /**
      * Generate an error report panel from an exception.
      * 
      * @param e
      *            The exception
      * @param askName
      *            if true, the name and email will be asked
      * @param message
      *            A message with more information
      */
     public static void GenerateErrorReport( Exception e, boolean askName, String message ) {
 
         if( isInterestingException( e ) )
             new ReportDialog( e, askName, message );
         else
             e.printStackTrace( );
     }
 
     /**
      * Generate a comments report panel
      */
     public static void GenerateCommentsReport( ) {
 
         new ReportDialog( );
     }
 
     /**
      * Filter for the interesting exceptions and the known and managed ones.
      * 
      * @param e
      *            The exception
      * @return True if the exception is interesting (should be reported)
      */
     private static boolean isInterestingException( Exception e ) {
 
         if( e instanceof NullPointerException )
             return true;
         if( e instanceof IndexOutOfBoundsException )
             return true;
         if( e instanceof IOException )
             return false;
         if( e instanceof AuthenticationFailedException )
             return false;
         if( e instanceof MessagingException )
             return true;
         if( e instanceof MalformedURLException )
             return true;
         if( e instanceof FileNotFoundException )
             return true;
         if( e instanceof ParserConfigurationException )
             return true;
         if( e instanceof SAXException )
             return true;
         if( e instanceof InvalidPropertiesFormatException )
             return true;
         if( e instanceof SAXParseException )
             return true;
         if( e instanceof ParseException )
             return true;
         if( e instanceof UnsupportedEncodingException )
             return true;
         if( e instanceof TransformerConfigurationException )
             return true;
         if( e instanceof TransformerException )
             return true;
         if( e instanceof InterruptedException )
             return true;
         if( e instanceof java.awt.HeadlessException )
             return true;
         if( e instanceof InvalidMidiDataException )
             return false;
         if( e instanceof MidiUnavailableException )
             return false;
         if( e instanceof LineUnavailableException )
             return false;
         if( e instanceof UnsupportedAudioFileException )
             return false;
         if( e instanceof BadLocationException )
             return false;
         if( e instanceof RuntimeException )
             return true;
         if( e instanceof NumberFormatException )
             return true;
         if( e instanceof java.util.MissingResourceException )
             return true;
         if( e instanceof javax.media.NoPlayerException )
             return true;
         if( e instanceof java.util.zip.ZipException )
             return true;
         if( e instanceof SecurityException )
             return true;
         if( e instanceof es.eucm.eadventure.engine.core.data.SaveGameException )
             return true;
         if( e instanceof java.awt.FontFormatException )
             return true;
 
         //		if (e instanceof StackOverflowException)
         //			return true;
 
         return true;
     }
 
     /**
      * Constructor for the error report dialog.
      * 
      * @param e
      *            The exception
      * @param askName
      *            True if the name and email should be asked
      * @param message
      *            A message with more information on the error
      */
     private ReportDialog( Exception e, boolean askName, String message ) {
 
         super( );
 
         this.setSize( PANEL_WIDTH, PANEL_HEIGHT );
         this.setResizable( false );
        this.setModalityType( Dialog.ModalityType.TOOLKIT_MODAL );
 
         String title = TC.get( "ErrorReport.Title" );
         this.setTitle( ( !title.equals( "Error" ) ? title : "Error Report" ) );
 
         this.setLayout( new GridBagLayout( ) );
         this.askName = askName;
         this.message = message;
 
         double screenWidth = Toolkit.getDefaultToolkit( ).getScreenSize( ).getWidth( );
         double screenHeight = Toolkit.getDefaultToolkit( ).getScreenSize( ).getHeight( );
         int locX = Math.round( ( (int) screenWidth - PANEL_WIDTH ) / 2.0f );
         int locY = Math.round( ( (int) screenHeight - PANEL_HEIGHT ) / 2.0f );
         this.setLocation( locX, locY );
 
         GridBagConstraints c = new GridBagConstraints( );
 
         c.gridx = 0;
         c.gridy = 0;
         c.weightx = 0.9;
         c.weighty = 0.1;
 
         c.fill = GridBagConstraints.HORIZONTAL;
         JTextArea topMessage = new JTextArea( message );
         topMessage.setLineWrap( true );
         topMessage.setEditable( false );
         this.add( topMessage, c );
         c.gridy++;
 
         c.fill = GridBagConstraints.NONE;
         if( askName ) {
             this.add( createNamePanel( ), c );
             c.gridy++;
         }
 
         os = "OS: " + System.getProperty( "os.name" );
         JLabel los = new JLabel( os );
         this.add( los, c );
         c.gridy++;
 
         java = "JAVA: " + System.getProperty( "java.version" ) + " from " + System.getProperty( "java.vendor" );
         JLabel ljava = new JLabel( java );
         this.add( ljava, c );
         c.gridy++;
 
         File moreinfo = new File( "RELEASE" );
         release = null;
         if( moreinfo.exists( ) ) {
             try {
                 FileInputStream fis = new FileInputStream( moreinfo );
                 BufferedInputStream bis = new BufferedInputStream( fis );
                 int nextChar = -1;
                 while( ( nextChar = bis.read( ) ) != -1 ) {
                     if( release == null )
                         release = "RELEASE: " + (char) nextChar;
                     else
                         release += (char) nextChar;
                 }
 
                 if( release != null ) {
                     JLabel lrelease = new JLabel( release );
                     this.add( lrelease, c );
                     c.gridy++;
                 }
             }
             catch( Exception ex ) {
             }
         }
         else {
         }
 
         JPanel descriptionPanel = new JPanel( );
         descriptionPanel.setLayout( new BorderLayout( ) );
         String shortDesc = TC.get( "ErrorReport.ShortDescription" );
         descriptionPanel.add( new JLabel( ( !shortDesc.equals( "Error" ) ? shortDesc : "Short description" ) ), BorderLayout.NORTH );
 
         descriptionTextArea = new JTextArea( );
         descriptionTextArea.setLineWrap( true );
         JScrollPane scrollPaneDescription = new JScrollPane( descriptionTextArea );
         scrollPaneDescription.setSize( PANEL_WIDTH, PANEL_HEIGHT / 2 - 100 );
 
         descriptionPanel.add( scrollPaneDescription, BorderLayout.CENTER );
         c.weighty = 1.0f;
         c.weightx = 1.0f;
         c.fill = GridBagConstraints.BOTH;
         this.add( descriptionPanel, c );
         c.gridy++;
 
         JPanel exceptionPanel = new JPanel( );
         exceptionPanel.setLayout( new BorderLayout( ) );
         String foundException = TC.get( "ErrorReport.FoundException" );
         exceptionPanel.add( new JLabel( ( !foundException.equals( "Error" ) ? foundException : "Found exception" ) ), BorderLayout.NORTH );
 
         JTextArea exceptionTextArea = new JTextArea( );
         if( e != null ) {
             StringWriter sw = new StringWriter( );
             PrintWriter pw = new PrintWriter( sw, true );
             e.printStackTrace( pw );
             pw.flush( );
             sw.flush( );
             exception = sw.toString( );
         }
         exceptionTextArea.setText( exception );
         exceptionTextArea.setEditable( false );
 
         JScrollPane scrollPaneException = new JScrollPane( exceptionTextArea );
         exceptionPanel.add( scrollPaneException, BorderLayout.CENTER );
 
         this.add( exceptionPanel, c );
         c.gridy++;
 
         c.weighty = 0.1;
         c.fill = GridBagConstraints.NONE;
         this.add( createButtonPanel( true ), c );
 
         this.setVisible( true );
     }
 
     /**
      * Constructor for the comments report dialog
      */
     private ReportDialog( ) {
 
         super( );
 
         this.setSize( PANEL_WIDTH, PANEL_HEIGHT );
         this.setResizable( false );
 
         this.setTitle( TC.get( "ErrorReport.TitleComments" ) );
 
         this.setLayout( new GridBagLayout( ) );
 
         double screenWidth = Toolkit.getDefaultToolkit( ).getScreenSize( ).getWidth( );
         double screenHeight = Toolkit.getDefaultToolkit( ).getScreenSize( ).getHeight( );
         int locX = Math.round( ( (int) screenWidth - PANEL_WIDTH ) / 2.0f );
         int locY = Math.round( ( (int) screenHeight - PANEL_HEIGHT ) / 2.0f );
         this.setLocation( locX, locY );
 
         GridBagConstraints c = new GridBagConstraints( );
 
         c.gridx = 0;
         c.gridy = 0;
         c.weightx = 0.9;
         c.weighty = 0.1;
 
         c.fill = GridBagConstraints.NONE;
         this.add( createNamePanel( ), c );
         c.gridy++;
 
         os = "OS: " + System.getProperty( "os.name" );
         JLabel los = new JLabel( os );
         this.add( los, c );
         c.gridy++;
 
         java = "JAVA: " + System.getProperty( "java.version" ) + " from " + System.getProperty( "java.vendor" );
         JLabel ljava = new JLabel( java );
         this.add( ljava, c );
         c.gridy++;
 
         File moreinfo = new File( "RELEASE" );
         release = null;
         if( moreinfo.exists( ) ) {
             try {
                 FileInputStream fis = new FileInputStream( moreinfo );
                 BufferedInputStream bis = new BufferedInputStream( fis );
                 int nextChar = -1;
                 while( ( nextChar = bis.read( ) ) != -1 ) {
                     if( release == null )
                         release = "RELEASE: " + (char) nextChar;
                     else
                         release += (char) nextChar;
                 }
 
                 if( release != null ) {
                     JLabel lrelease = new JLabel( release );
                     this.add( lrelease, c );
                     c.gridy++;
                 }
             }
             catch( Exception ex ) {
             }
         }
         else {
         }
 
         JPanel descriptionPanel = new JPanel( );
         descriptionPanel.setLayout( new BorderLayout( ) );
         descriptionPanel.add( new JLabel( TC.get( "ErrorReport.Comments" ) ), BorderLayout.NORTH );
 
         descriptionTextArea = new JTextArea( );
         descriptionTextArea.setLineWrap( true );
         JScrollPane scrollPaneDescription = new JScrollPane( descriptionTextArea );
         scrollPaneDescription.setSize( PANEL_WIDTH, PANEL_HEIGHT / 2 - 100 );
 
         descriptionPanel.add( scrollPaneDescription, BorderLayout.CENTER );
         c.weighty = 1.0f;
         c.weightx = 1.0f;
         c.fill = GridBagConstraints.BOTH;
         this.add( descriptionPanel, c );
         c.gridy++;
 
         c.weighty = 0.1;
         c.fill = GridBagConstraints.NONE;
         this.add( createButtonPanel( false ), c );
 
         this.setVisible( true );
     }
 
     /**
      * Creates and returns the button panel (send and cancel)
      * 
      * @param error
      *            Indicates if the buttons are of an error or comments
      * @return The new JPanel
      */
     private JPanel createButtonPanel( final boolean error ) {
 
         JPanel buttonPanel = new JPanel( );
         String send = TC.get( "ErrorReport.Send" );
         sendButton = new JButton( ( !send.equals( "Error" ) ? send : "Send" ) );
         sendButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 ReportDialog.this.sendReport( error );
                 ReportDialog.this.setVisible( false );
             }
         } );
         buttonPanel.add( sendButton );
 
         String cancel = TC.get( "ErrorReport.Cancel" );
         dontSendButton = new JButton( ( !cancel.equals( "Error" ) ? cancel : "Cancel" ) );
         dontSendButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 ReportDialog.this.setVisible( false );
             }
         } );
 
         buttonPanel.add( dontSendButton );
         return buttonPanel;
     }
 
     /**
      * Generate the actual report and send it using php to the e-adventure
      * server
      * 
      * @param error
      *            Indicates if it is an error or comments report
      */
     protected void sendReport( boolean error ) {
 
         String report;
         if( error ) {
             report = "ERROR REPORT\n" + message + "\n\n";
 
             if( askName ) {
                 report += "USER: " + nameTextField.getText( ) + "\n";
                 report += "EMAIL: " + emailTextField.getText( ) + "\n\n";
             }
 
             report += os + "\n";
             report += java + "\n";
             if( release != null )
                 report += release + "\n";
             report += "\n";
 
             report += "DESCRIPTION:\n" + descriptionTextArea.getText( ) + "\n\n\n";
             report += "STACK TRACE:\n" + exception + "\n";
         }
         else {
             report = "USER COMMENTS\n\n";
 
             report += "USER: " + nameTextField.getText( ) + "\n";
             report += "EMAIL: " + emailTextField.getText( ) + "\n\n";
 
             report += os + "\n";
             report += java + "\n";
             if( release != null )
                 report += release + "\n";
             report += "\n";
 
             report += "COMMENTS AND SUGGESTIONS:\n" + descriptionTextArea.getText( ) + "\n";
         }
 
         try {
             URL url = new URL( "http://backend-ea.e-ucm.es/reports.php" );
             HttpURLConnection con = (HttpURLConnection) url.openConnection( );
             con.setRequestMethod( "POST" );
             con.setDoInput( true );
             con.setDoOutput( true );
 
             con.setUseCaches( false );
             con.setAllowUserInteraction( true );
             HttpURLConnection.setFollowRedirects( true );
             con.setInstanceFollowRedirects( true );
             DataOutputStream out = new DataOutputStream( con.getOutputStream( ) );
             String content = "type=" + ( error ? "bug" : "comment" ) + "&version=" + release + "&file=" + report;
             out.writeBytes( content );
             out.flush( );
             out.close( );
             BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream( ) ) );
             in.readLine( );
         }
         catch( Exception e ) {
             //e.printStackTrace();
         }
 
     }
 
     //TODO: eliminar este mtodo, se aadi para las pruebas de medicina
     public static void sendReport(String comment) {
         try {
             URL url = new URL( "http://backend-ea.e-ucm.es/reports.php" );
             HttpURLConnection con = (HttpURLConnection) url.openConnection( );
             con.setRequestMethod( "POST" );
             con.setDoInput( true );
             con.setDoOutput( true );
 
             con.setUseCaches( false );
             con.setAllowUserInteraction( true );
             HttpURLConnection.setFollowRedirects( true );
             con.setInstanceFollowRedirects( true );
             DataOutputStream out = new DataOutputStream( con.getOutputStream( ) );
             String content = "type=comment" + "&version=medicina_informe" + "&file=" + comment;
             out.writeBytes( content );
             out.flush( );
             out.close( );
             BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream( ) ) );
             in.readLine( );
         }
         catch( Exception e ) {
             //e.printStackTrace();
         }
 
     }
     /**
      * Create the panel where the name and email are inputed
      * 
      * @return the new JPanel
      */
     private JPanel createNamePanel( ) {
 
         JPanel temp = new JPanel( );
         temp.setLayout( new GridBagLayout( ) );
         GridBagConstraints c = new GridBagConstraints( );
 
         c.gridx = 0;
         c.gridy = 0;
         c.fill = GridBagConstraints.NONE;
         String name = TC.get( "ErrorReport.Name" );
         temp.add( new JLabel( ( !name.equals( "Error" ) ? name : "Name" ) ), c );
 
         nameTextField = new JTextField( 25 ) {
 
             private static final long serialVersionUID = 1L;
 
             @Override
             public Dimension getMinimumSize( ) {
 
                 return getPreferredSize( );
             }
         };
 
         c.gridx++;
         c.fill = GridBagConstraints.HORIZONTAL;
         temp.add( nameTextField, c );
 
         c.gridy++;
         c.gridx = 0;
         c.fill = GridBagConstraints.NONE;
         String email = TC.get( "ErrorReport.Email" );
         temp.add( new JLabel( ( !email.equals( "Error" ) ? email : "e-mail" ) ), c );
 
         emailTextField = new JTextField( 200 );
         c.gridx++;
         c.fill = GridBagConstraints.HORIZONTAL;
         temp.add( emailTextField, c );
 
         return temp;
     }
 
     /**
      * Test method
      * 
      * @param args
      */
     public static void main( String[] args ) {
 
         ReportDialog.GenerateErrorReport( new Exception( ), false, "ERROR MESSAGE" );
         //ReportDialog.GenerateCommentsReport();
     }
 }
