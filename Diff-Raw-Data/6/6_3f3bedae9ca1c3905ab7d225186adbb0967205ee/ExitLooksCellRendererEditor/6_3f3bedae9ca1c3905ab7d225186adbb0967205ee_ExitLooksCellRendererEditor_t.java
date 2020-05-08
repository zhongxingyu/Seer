 /*******************************************************************************
  * eAdventure (formerly <e-Adventure> and <e-Game>) is a research project of the e-UCM
  *          research group.
  *   
  *    Copyright 2005-2012 e-UCM research group.
  *  
  *     e-UCM is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://e-adventure.e-ucm.es> or
  *          <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  * This file is part of eAdventure, version 1.5.
  * 
  *   You can access a list of all the contributors to eAdventure at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       eAdventure is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      eAdventure is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with Adventure.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.editor.gui.elementpanels.general.tables;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.AbstractCellEditor;
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 
 import es.eucm.eadventure.common.data.HasSound;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.AssetsController;
 import es.eucm.eadventure.editor.control.controllers.general.ExitLookDataControl;
 import es.eucm.eadventure.editor.control.tools.Tool;
 import es.eucm.eadventure.editor.control.tools.generic.ChangeStringValueTool;
 import es.eucm.eadventure.editor.control.tools.listeners.SelectSimpleSoundListener;
 
 public class ExitLooksCellRendererEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
 
     private static final long serialVersionUID = 8128260157985286632L;
 
     private ExitLookDataControl value;
 
     private JLabel label = null;
 
     private String tooltip;
 
     private JPanel panel;
 
     private GridBagConstraints c;
 
     public Object getCellEditorValue( ) {
 
         return value;
     }
 
     public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int col ) {
 
         if( value != null )
             this.value = (ExitLookDataControl) value;
         else
             this.value = null;
         return newPanel( isSelected, table );
     }
 
     public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
 
         if( !isSelected ) {
             if( value != null ) {
                 Component c;
                 
                 this.value = (ExitLookDataControl) value;
                 String text = ( this.value.getCustomizedText( ) != null ? this.value.getCustomizedText( ) : "" );
                 
                 ImageIcon icon = new ImageIcon( );
                 if( this.value.isCursorCustomized( ) ) {
                     Image image = AssetsController.getImage( this.value.getCustomizedCursor( ) );
                     // the case of a sound is selected and image is not selected
                     if (image == null)
                         image = AssetsController.getImage( Controller.getInstance( ).getDefaultExitCursorPath( ) );
                     icon.setImage( image );
                 }
                 else {
                     Image image = AssetsController.getImage( Controller.getInstance( ).getDefaultExitCursorPath( ) );
                     icon.setImage( image );
                 }
                 
                 // Check audio path
                 if (this.value.getSoundPath( )!= null && !this.value.getSoundPath( ).equals( "" )){
                     JLabel cursorAndText = new JLabel( text, icon, SwingConstants.LEADING );
                     ImageIcon iconAudio = new ImageIcon( "img/icons/audio.png" );
                     JLabel audioPath = new JLabel ( /*this.value.getSoundPath( ),*/ iconAudio, SwingConstants.LEFT );
                     JPanel toReturn = new JPanel();
                     toReturn.setOpaque( false );
                     GridBagLayout gbl = new GridBagLayout();
                     GridBagConstraints gbc = new GridBagConstraints();
                     gbc.anchor=GridBagConstraints.WEST;
                     gbc.fill=GridBagConstraints.HORIZONTAL;
                     toReturn.setLayout( gbl );
                     toReturn.add( cursorAndText, gbc );
                     gbc.anchor = GridBagConstraints.EAST;
                     gbc.gridx=1;
                     toReturn.add( audioPath, gbc );
                     c=toReturn;
                 } else {
                     c=new JLabel( text, icon, SwingConstants.LEFT );
                 }
 
                 return c;
                 //return new JLabel( text, icon, SwingConstants.LEFT );
             }
             else {
                 this.value = null;
                 return new JLabel( "" );
             }
         }
         else {
             if( value != null )
                 this.value = (ExitLookDataControl) value;
             else
                 this.value = null;
             return newPanel( isSelected, table );
         }
     }
 
     private JPanel newPanel( final boolean isSelected, JTable table ) {
 
         panel = new JPanel( );
         panel.setLayout( new GridBagLayout( ) );
         c = new GridBagConstraints( );
 
         final JTextField text = new JTextField( );
         text.setText( value.getCustomizedText( ) != null ? value.getCustomizedText( ) : "" );
         text.setEnabled( isSelected );
         text.getDocument( ).addDocumentListener( new DocumentListener( ) {
 
             public void changedUpdate( DocumentEvent arg0 ) {
             }
 
             public void insertUpdate( DocumentEvent arg0 ) {
                 Tool tool = new ChangeStringValueTool( value, text.getText( ), "getCustomizedText", "setExitText" );
                 Controller.getInstance( ).addTool( tool );
             }
 
             public void removeUpdate( DocumentEvent arg0 ) {
                 Tool tool = new ChangeStringValueTool( value, text.getText( ), "getCustomizedText", "setExitText" );
                 Controller.getInstance( ).addTool( tool );
             }
         } );
 
         JPanel soundPanel = null;
         if (value!=null){
             soundPanel = createSoundPanel ( value);
         }
         
         Icon deleteContentIcon = new ImageIcon( "img/icons/deleteContent.png" );
         JButton deleteContentButton = new JButton( deleteContentIcon );
         deleteContentButton.setPreferredSize( new Dimension( 20, 20 ) );
         deleteContentButton.setMaximumSize( new Dimension( 20, 20 ) );
         deleteContentButton.setToolTipText( TC.get( "Resources.DeleteAsset" ) );
 
         ImageIcon cursorPreview = new ImageIcon( );
         if( value.isCursorCustomized( ) ) {
             Image image = AssetsController.getImage( value.getCustomizedCursor( ) );
             tooltip = value.getCustomizedCursor( );
          // the case of a sound is selected and image is not selected
             if (image == null)
                 image = AssetsController.getImage( Controller.getInstance( ).getDefaultExitCursorPath( ) );
             cursorPreview.setImage( image );
         }
         else {
             Image image = AssetsController.getImage( Controller.getInstance( ).getDefaultExitCursorPath( ) );
             tooltip = Controller.getInstance( ).getDefaultExitCursorPath( );
             cursorPreview.setImage( image );
         }
 
         final JButton button = new JButton( TC.get( "Buttons.Select" ) );
         button.setFocusable( false );
         button.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 value.editCursorPath( );
                 ImageIcon cursorPreview = new ImageIcon( );
                 Image image = AssetsController.getImage( value.getCustomizedCursor( ) );
                 tooltip = value.getCustomizedCursor( );
                 cursorPreview.setImage( image );
                 panel.remove( label );
                 label = new JLabel( cursorPreview );
                 panel.add( label, c );
                 panel.validate( );
             }
         } );
 
         deleteContentButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 if( value.isCursorCustomized( ) ) {
                     Controller.getInstance( ).addTool( new ChangeStringValueTool( value, null, "getCustomizedCursor", "setCursorPath" ) );
 
                     //				value.setCursorPath(null);
                     Image image = AssetsController.getImage( Controller.getInstance( ).getDefaultExitCursorPath( ) );
                     tooltip = Controller.getInstance( ).getDefaultExitCursorPath( );
                     ImageIcon cursorPreview = new ImageIcon( image );
                     panel.remove( label );
                     label = new JLabel( cursorPreview );
                     panel.add( label, c );
                     panel.validate( );
                 }
             }
         } );
 
         c.gridx = 0;
         c.gridy = 0;
         panel.add( button, c );
         c.gridx = 2;
         panel.add( deleteContentButton, c );
         c.gridx = 1;
         c.weightx = 2.0;
         c.weighty = 2.0;
         c.fill = GridBagConstraints.BOTH;
         label = new JLabel( cursorPreview );
         label.setToolTipText( tooltip );
         panel.add( label, c );
         panel.validate( );
 
         JPanel temp = new JPanel( );
         temp.setLayout( new BorderLayout( ) );
         
         if (soundPanel!=null){
             JPanel firstRow = new JPanel();
             firstRow.setLayout( new GridBagLayout() );
             GridBagConstraints gc=new GridBagConstraints();
           // text.setColumns( 20 );
            gc.weighty = 1.0;
             gc.fill = GridBagConstraints.HORIZONTAL;
             firstRow.add( text, gc );
           // gc.fill = GridBagConstraints.NONE;
             gc.gridx = 1;
             firstRow.add( soundPanel, gc );
             temp.add( firstRow, BorderLayout.NORTH );
         } else {
             temp.add( text, BorderLayout.NORTH );    
         }
         
         temp.add( panel, BorderLayout.CENTER );
 
         temp.setBorder( BorderFactory.createMatteBorder( 2, 0, 2, 0, table.getSelectionBackground( ) ) );
 
         return temp;
     }
 
     
     /**
      * Max of characters to be shown in the label with the path of the audio path
      */
     private static final int PATH_MAX_LONG_TO_SHOW = 10;
     
     private JPanel createSoundPanel( HasSound descriptionSound ){
         
         JPanel soundpanel = new JPanel( );        
         //soundPanels[type] = soundpanel;
         soundpanel.setLayout( new GridBagLayout( )  );
         GridBagConstraints cPanel = new GridBagConstraints( );
         cPanel.gridx = 0;
         cPanel.gridy = 0;
         cPanel.ipadx = 10;
         cPanel.weightx = 0;
         
         JLabel label = new JLabel();
         label.setPreferredSize( new Dimension(1,1) );
         
      
         // prepare the label for the sound panel
         String soundPath = descriptionSound.getSoundPath( );
         if (soundPath!=null && !soundPath.equals( "" )){
             ImageIcon icon = new ImageIcon( "img/icons/audio.png" );
             String[] temp = soundPath.split( "/" );
             label = new JLabel( cutLongAudioPath( temp[temp.length - 1]), icon, SwingConstants.LEFT );
         }
         else {
             ImageIcon icon = new ImageIcon( "img/icons/noAudio.png" );
             label = new JLabel( TC.get( "Conversations.NoAudio" ), icon, SwingConstants.LEFT );
         }   
         
         //soundLabels[type] = label;
         
         label.setOpaque( false );
         soundpanel.add( label, cPanel );
         
         JPanel buttonPanel = new JPanel( );
         buttonPanel.setLayout( new GridBagLayout( ) );
         GridBagConstraints cButtons = new GridBagConstraints( );
         cButtons.gridx = 0;
         cButtons.gridy = 0;
         //cButtons.anchor = GridBagConstraints.NONE; 
         
         
         // prepare the buttons
         JButton selectButton = new JButton( TC.get( "Conversations.Select" ) );
         selectButton.setFocusable( false );
         selectButton.setEnabled( true );
         selectButton.addActionListener(new SelectSimpleSoundListener(descriptionSound, false)  );
         selectButton.setOpaque( false );
         buttonPanel.add( selectButton, cButtons );
 
         cButtons.gridx = 1;
         JButton deleteButton = new JButton( new ImageIcon( "img/icons/deleteContent.png" ) );
         //soundDeleteButtons[type] = deleteButton ;
         deleteButton.setToolTipText( TC.get( "Conversations.DeleteAudio" ) );
         deleteButton.setContentAreaFilled( false );
         deleteButton.setMargin( new Insets( 0, 0, 0, 0 ) );
         deleteButton.setFocusable( false );
         deleteButton.setEnabled( soundPath != null &&  !soundPath.equals( "" ));
         deleteButton.addActionListener( new SelectSimpleSoundListener(descriptionSound, true)  );
         deleteButton.setOpaque( false );
         buttonPanel.add( deleteButton, cButtons );
         buttonPanel.setOpaque( false );
         
         
         cPanel.gridx = 1;
         cPanel.anchor = GridBagConstraints.EAST;
         soundpanel.add( buttonPanel, cPanel );
         
         return soundpanel;
     }
 
     private String cutLongAudioPath(String audioPath){
         
         String cutAudioPath = new String();
         
         if (audioPath!=null){
             if (audioPath.length( ) < PATH_MAX_LONG_TO_SHOW )
                 return audioPath;
             else 
                 return "..." +  audioPath.substring( audioPath.length( ) - PATH_MAX_LONG_TO_SHOW, audioPath.length( ));
         }
         
         return cutAudioPath;
     }
 }
