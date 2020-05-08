 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package es.eucm.eadventure.editor.gui.editdialogs;
 
 import java.awt.BorderLayout;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import es.eucm.eadventure.common.auxiliar.File;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 
 public class HelpDialog extends JDialog implements HyperlinkListener {
 
     /**
      * 
      */
     private static final long serialVersionUID = -1705431401552161788L;
 
     private static final int HELP_WIDTH = 720;
 
     private static final int HELP_HEIGHT = 600;
 
    private JEditorPane pane;
 
     private List<URL> backList;
 
     private List<URL> forwardList;
 
     private JButton goBack;
 
     private JButton goForward;
 
     public HelpDialog( String helpPath ) {
 
         super( Controller.getInstance( ).peekWindow( ), TC.get( "HelpDialog.Title" ), Dialog.ModalityType.TOOLKIT_MODAL );
         backList = new ArrayList<URL>( );
         forwardList = new ArrayList<URL>( );
         setSize( HELP_WIDTH, HELP_HEIGHT );
         setLayout( new BorderLayout( ) );
         addButtonPanel( );
         Dimension size = Toolkit.getDefaultToolkit( ).getScreenSize( );
         setLocation( (int) ( size.getWidth( ) - HELP_WIDTH ) / 2, (int) ( size.getHeight( ) - HELP_HEIGHT ) / 2 );
         String folder = "help/" + Controller.getInstance( ).getLanguage( ) + "/";
         File file = new File( folder + helpPath );
         if( file.exists( ) ) {
            pane = new JEditorPane( );
             try {
                 pane.setPage( file.toURI( ).toURL( ) );
                 
                 backList.add( file.toURI( ).toURL( ) );
                 pane.setEditable( false );
                 pane.setHighlighter( null );
                 pane.addHyperlinkListener( this );
                 add( new JScrollPane( pane, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ), BorderLayout.CENTER );
             }
             catch( MalformedURLException e1 ) {
                 writeFileNotFound( folder + helpPath );
             }
             catch( IOException e1 ) {
                 writeFileNotFound( folder + helpPath );
             }
         }
         else {
             writeFileNotFound( folder + helpPath );
         }
 
         setVisible( true );
 
         addWindowListener( new WindowAdapter( ) {
 
             @Override
             public void windowClosing( WindowEvent e ) {
 
                 dispose( );
             }
         } );
     }
 
     public void hyperlinkUpdate( HyperlinkEvent event ) {
 
         if( event.getEventType( ) == HyperlinkEvent.EventType.ACTIVATED ) {
             try {
                 if( event.getDescription( ).toLowerCase( ).contains( "http:" ) ) {
                     openURI( event.getDescription( ) );
                 }
                 else {
                     pane.setPage( event.getURL( ) );
                     backList.add( event.getURL( ) );
                     forwardList.clear( );
                     updateButtons( );
                 }
             }
             catch( IOException ioe ) {
             }
         }
     }
 
     private void goBack( ) {
 
         if( backList.size( ) > 1 ) {
             try {
                 pane.setPage( backList.get( backList.size( ) - 2 ) );
                 forwardList.add( backList.get( backList.size( ) - 1 ) );
                 backList.remove( backList.size( ) - 1 );
                 updateButtons( );
             }
             catch( IOException e ) {
             }
         }
     }
 
     private void goForward( ) {
 
         if( forwardList.size( ) > 0 ) {
             try {
                 pane.setPage( forwardList.get( forwardList.size( ) - 1 ) );
                 backList.add( forwardList.get( forwardList.size( ) - 1 ) );
                 forwardList.remove( forwardList.size( ) - 1 );
                 updateButtons( );
             }
             catch( IOException e ) {
             }
         }
     }
 
     private void addButtonPanel( ) {
 
         JPanel buttonPanel = new JPanel( );
         goBack = new JButton( "Back" );
         goBack.setEnabled( false );
         goBack.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 goBack( );
             }
         } );
         buttonPanel.add( goBack );
 
         goForward = new JButton( "Forward" );
         goForward.setEnabled( false );
         goForward.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 goForward( );
             }
         } );
         buttonPanel.add( goForward );
         add( buttonPanel, BorderLayout.NORTH );
     }
 
     private void updateButtons( ) {
 
         if( backList.size( ) > 1 )
             goBack.setEnabled( true );
         else
             goBack.setEnabled( false );
         if( forwardList.size( ) > 0 )
             goForward.setEnabled( true );
         else
             goForward.setEnabled( false );
     }
 
     public void writeFileNotFound( String path ) {
 
         add( new JLabel( TC.get( "HelpDialog.FileNotFound" ) + " " + path ) );
     }
 
     public void openURI( String url ) {
 
         if( !java.awt.Desktop.isDesktopSupported( ) ) {
             System.err.println( "Desktop is not supported (fatal)" );
             System.exit( 1 );
         }
 
         java.awt.Desktop desktop = java.awt.Desktop.getDesktop( );
 
         if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
 
             System.err.println( "Desktop doesn't support the browse action (fatal)" );
             System.exit( 1 );
         }
 
         try {
 
             java.net.URI uri = new java.net.URI( url );
             desktop.browse( uri );
         }
         catch( Exception e ) {
 
             System.err.println( e.getMessage( ) );
         }
     }
 }
