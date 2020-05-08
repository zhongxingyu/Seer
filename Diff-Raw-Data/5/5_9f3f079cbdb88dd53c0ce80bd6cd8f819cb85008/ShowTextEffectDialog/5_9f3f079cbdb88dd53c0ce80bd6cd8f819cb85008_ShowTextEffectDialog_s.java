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
  * 
  */
 package es.eucm.eadventure.editor.gui.editdialogs.effectdialogs;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.colorchooser.ColorSelectionModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.controllers.EffectsController;
 import es.eucm.eadventure.editor.gui.auxiliar.components.TextPreviewPanel;
 import es.eucm.eadventure.editor.gui.editdialogs.ToolManagableDialog;
 import es.eucm.eadventure.editor.gui.otherpanels.positionimagepanels.TextImagePanel;
 import es.eucm.eadventure.editor.gui.otherpanels.positionpanel.PositionPanel;
 
 /**
  * This class represents a dialog used to add and edit show text effects. It
  * allows the user to type the position directly, or to select it from a
  * position of a scene.
  * 
  */
 public class ShowTextEffectDialog extends EffectDialog {
 
     /**
      * 
      */
     private static final long serialVersionUID = -7405590437950803927L;
 
     /**
      * Combo box with the scenes.
      */
     private JComboBox scenesComboBox;
 
     /**
      * Panel to select and show the position.
      */
     private PositionPanel textPositionPanel;
 
     /**
      * Text Field to introduce the text
      */
     private JTextField text;
 
     /**
      * Panel to display the text in the scene
      */
     private TextPreviewPanel textPreviewPanel;
 
     /**
      * Panel to display the text
      */
     private TextImagePanel imagePanel;
 
     /**
      * The text front color
      */
     private Color frontColor;
 
     /**
      * The text border color
      */
     private Color borderColor;
 
     /**
      * Constructor.
      * 
      * @param currentProperties
      *            Set of initial values
      */
     public ShowTextEffectDialog( HashMap<Integer, Object> currentProperties ) {
 
         // Call the super method
         super( TC.get( "ShowTextEffect.Title" ), true );
 
         // Create the main panel
         JPanel mainPanel = new JPanel( );
         mainPanel.setLayout( new GridBagLayout( ) );
         GridBagConstraints c = new GridBagConstraints( );
 
         // Set the border of the panel with the description
         mainPanel.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 5, 5, 0, 5 ), BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "ShowTextEffect.Border" ) ) ) );
 
         c.gridx = 0;
         c.gridy = 0;
         c.gridwidth = 2;
         c.fill = GridBagConstraints.BOTH;
         // Create the text preview panel
         JPanel previewTextCont = createTextColorPanel( );
         mainPanel.add( previewTextCont, c );
 
         c.gridy++;
         c.gridwidth = 1;
         // Create and add a description for the scenes
         mainPanel.add( new JLabel( TC.get( "SceneLocation.SceneListDescription" ) ), c );
 
         // Create the set of values for the scenes
         List<String> scenesList = new ArrayList<String>( );
         scenesList.add( TC.get( "SceneLocation.NoSceneSelected" ) );
         String[] scenesArray = controller.getIdentifierSummary( ).getSceneIds( );
         for( String scene : scenesArray )
             scenesList.add( scene );
         scenesArray = scenesList.toArray( new String[] {} );
 
         // Create and add the list of scenes
         c.insets = new Insets( 2, 4, 4, 4 );
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 1;
         c.gridwidth = 1;
         c.gridx = 0;
 
        c.gridx++;
         scenesComboBox = new JComboBox( scenesArray );
         scenesComboBox.addActionListener( new ScenesComboBoxListener( ) );
         mainPanel.add( scenesComboBox, c );
 
         c.gridx = 0;
         c.gridy++;
         c.gridwidth = 1;
         // Add label with description of text field
         mainPanel.add( new JLabel( TC.get( "ShowTextEffect.TextLabel" ) ), c );
 
         //Create text field to get the text
         JPanel textContainer = new JPanel( );
         text = new JTextField( 30 );
         text.addKeyListener( new KeyListener( ) {
 
             public void keyPressed( KeyEvent e ) {
 
             }
 
             public void keyReleased( KeyEvent e ) {
 
                 imagePanel.setText( text.getText( ) );
 
             }
 
             public void keyTyped( KeyEvent e ) {
 
             }
         } );
         text.setEditable( true );
         textContainer.add( text );
        c.gridx++;
         mainPanel.add( textContainer, c );
 
         // Create and add the panel to edit the position
         c.fill = GridBagConstraints.BOTH;
         c.gridx = 0;
         c.gridy++;
         c.weightx = 1;
         c.weighty = 1;
         c.gridwidth = 2;
 
         // Set the default values (if present)
         if( currentProperties != null ) {
             int x = 0;
             int y = 0;
             int rgbFront = 0;
             int rgbBorder = 0;
 
             if( currentProperties.containsKey( EffectsController.EFFECT_PROPERTY_TEXT ) )
                 text.setText( (String) currentProperties.get( EffectsController.EFFECT_PROPERTY_TEXT ) );
 
             if( currentProperties.containsKey( EffectsController.EFFECT_PROPERTY_X ) )
                 x = Integer.parseInt( (String) currentProperties.get( EffectsController.EFFECT_PROPERTY_X ) );
 
             if( currentProperties.containsKey( EffectsController.EFFECT_PROPERTY_Y ) )
                 y = Integer.parseInt( (String) currentProperties.get( EffectsController.EFFECT_PROPERTY_Y ) );
 
             if( currentProperties.containsKey( EffectsController.EFFECT_PROPERTY_FRONT_COLOR ) )
                 rgbFront = Integer.parseInt( (String) currentProperties.get( EffectsController.EFFECT_PROPERTY_FRONT_COLOR ) );
 
             if( currentProperties.containsKey( EffectsController.EFFECT_PROPERTY_BORDER_COLOR ) )
                 rgbBorder = Integer.parseInt( (String) currentProperties.get( EffectsController.EFFECT_PROPERTY_BORDER_COLOR ) );
 
             //create front and border colors
             frontColor = new Color( rgbFront );
             borderColor = new Color( rgbBorder );
 
             // Create the panel which will display the background and the position
             imagePanel = new TextImagePanel( text.getText( ), frontColor, borderColor );
             textPreviewPanel.setTextFrontColor( frontColor );
             textPreviewPanel.setTextBorderColor( borderColor );
 
             if( x > 5000 )
                 x = 5000;
             if( x < -2000 )
                 x = -2000;
             if( y > 5000 )
                 y = 5000;
             if( y < -2000 )
                 y = -2000;
 
             textPositionPanel = new PositionPanel( imagePanel, x, y );
 
         }
         else {
             //create front and border colors
             frontColor = Color.white;
             borderColor = Color.yellow;
             // Create the panel which will display the background and the position
             imagePanel = new TextImagePanel( text.getText( ), frontColor, borderColor );
             textPositionPanel = new PositionPanel( imagePanel, 400, 500 );
         }
         mainPanel.add( textPositionPanel, c );
         imagePanel.repaint( );
         // Add the panel to the center
         add( mainPanel, BorderLayout.CENTER );
 
         // Set the dialog
         setResizable( false );
         setSize( 640, 640 );
         Dimension screenSize = Toolkit.getDefaultToolkit( ).getScreenSize( );
         setLocation( ( screenSize.width - getWidth( ) ) / 2, ( screenSize.height - getHeight( ) ) / 2 );
         setVisible( true );
 
     }
 
     private JPanel createTextColorPanel( ) {
 
         JPanel textColorPanel = new JPanel( );
         textColorPanel.setLayout( new GridBagLayout( ) );
         GridBagConstraints c = new GridBagConstraints( );
         c.gridx = 0;
         c.gridy = 0;
         c.weightx = 1.0;
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weighty = 0.1;
 
         textPreviewPanel = new TextPreviewPanel( Color.WHITE, Color.yellow, false, null, null );
         c.gridy++;
         c.weighty = 1.0;
         c.ipady = 40;
         textColorPanel.add( textPreviewPanel, c );
         JButton frontColorButton = new JButton( TC.get( "Player.FrontColor" ) );
         frontColorButton.addActionListener( new ChangeTextColorListener( this, ChangeTextColorListener.FRONT_COLOR ) );
         c.gridy++;
         c.weighty = 0.1;
         c.ipady = 0;
         textColorPanel.add( frontColorButton, c );
         JButton borderColorButton = new JButton( TC.get( "Player.BorderColor" ) );
         borderColorButton.addActionListener( new ChangeTextColorListener( this, ChangeTextColorListener.BORDER_COLOR ) );
         c.gridy++;
         textColorPanel.add( borderColorButton, c );
         textColorPanel.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder( ), TC.get( "Player.TextColor" ) ) );
         return textColorPanel;
     }
 
     /**
      * Listener for the scenes combo box.
      */
     private class ScenesComboBoxListener implements ActionListener {
 
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
          */
         public void actionPerformed( ActionEvent arg0 ) {
 
             int selectedScene = scenesComboBox.getSelectedIndex( );
 
             // If the first option were selected, remove the image
             if( selectedScene == 0 )
                 textPositionPanel.removeImage( );
 
             // If other option were selected, load the image
             else
                 textPositionPanel.loadImage( controller.getSceneImagePath( scenesComboBox.getSelectedItem( ).toString( ) ) );
         }
     }
 
     @Override
     protected void pressedOKButton( ) {
 
         // Create a set of properties, and put the selected value
         properties = new HashMap<Integer, Object>( );
         properties.put( EffectsController.EFFECT_PROPERTY_TEXT, text.getText( ) );
         properties.put( EffectsController.EFFECT_PROPERTY_X, String.valueOf( textPositionPanel.getPositionX( ) ) );
         properties.put( EffectsController.EFFECT_PROPERTY_Y, String.valueOf( textPositionPanel.getPositionY( ) ) );
         properties.put( EffectsController.EFFECT_PROPERTY_FRONT_COLOR, String.valueOf( frontColor.getRGB( ) ) );
         properties.put( EffectsController.EFFECT_PROPERTY_BORDER_COLOR, String.valueOf( borderColor.getRGB( ) ) );
 
     }
 
     private class EffectColorChooser extends ToolManagableDialog {
 
         /**
          * 
          */
         private static final long serialVersionUID = 5119982246314726323L;
 
         JColorChooser colorChooser;
 
         public EffectColorChooser( Window window, Color initColor, ActionListener listener ) {
 
             super( window, "", false );
             this.colorChooser = new JColorChooser( initColor );
             this.add( colorChooser, BorderLayout.CENTER );
             JPanel container = new JPanel( );
             JButton ok = new JButton( "ok" );
             ok.addActionListener( listener );
             ok.addActionListener( new ActionListener( ) {
 
                 public void actionPerformed( ActionEvent e ) {
 
                     EffectColorChooser.this.dispose( );
 
                 }
             } );
             container.add( ok );
             this.add( container, BorderLayout.SOUTH );
             setResizable( false );
             setSize( 450, 400 );
             Dimension screenSize = Toolkit.getDefaultToolkit( ).getScreenSize( );
             setLocation( ( screenSize.width - getWidth( ) ) / 2, ( screenSize.height - getHeight( ) ) / 2 );
 
         }
 
         public void setPreviewPanel( JComponent preview ) {
 
             colorChooser.setPreviewPanel( preview );
         }
 
         public ColorSelectionModel getSelectionModel( ) {
 
             return colorChooser.getSelectionModel( );
         }
 
         public void setColor( Color color ) {
 
             colorChooser.setColor( color );
         }
 
         public Color getColor( ) {
 
             return colorChooser.getColor( );
         }
     }
 
     /**
      * Listener for the change color buttons.
      */
     private class ChangeTextColorListener implements ActionListener {
 
         /**
          * Constant for front color.
          */
         public static final int FRONT_COLOR = 0;
 
         /**
          * Constant for border color.
          */
         public static final int BORDER_COLOR = 1;
 
         private int color;
 
         /**
          * Color chooser.
          */
         private EffectColorChooser colorChooser;
 
         /**
          * Text preview panel.
          */
         private TextPreviewPanel colorPreviewPanel;
 
         /**
          * Constructor.
          * 
          * @param frontColor
          *            Whether the front or border color must be changed
          */
         public ChangeTextColorListener( Window win, int color ) {
 
             this.color = color;
 
             // Create the color chooser
             colorChooser = new EffectColorChooser( win, Color.WHITE, new UpdateColorListener( ) );
 
             // Create and add the preview panel, attaching it to the color chooser
             colorPreviewPanel = new TextPreviewPanel( Color.WHITE, Color.yellow, false, null, null );
             colorPreviewPanel.setPreferredSize( new Dimension( 400, 40 ) );
             colorPreviewPanel.setBorder( BorderFactory.createEmptyBorder( 1, 1, 1, 1 ) );
             colorChooser.setPreviewPanel( colorPreviewPanel );
             colorChooser.getSelectionModel( ).addChangeListener( new UpdatePreviewPanelListener( ) );
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
          */
         public void actionPerformed( ActionEvent e ) {
 
             // Update the color on the color chooser and the preview panel
             if( color == FRONT_COLOR )
                 colorChooser.setColor( frontColor );
             else if( color == BORDER_COLOR )
                 colorChooser.setColor( borderColor );
 
             colorPreviewPanel.setTextFrontColor( frontColor );
             colorPreviewPanel.setTextBorderColor( borderColor );
 
             // Create and show the dialog
             if( color == FRONT_COLOR )
                 colorChooser.setTitle( TC.get( "Player.FrontColor" ) );
             else if( color == BORDER_COLOR )
                 colorChooser.setTitle( TC.get( "Player.BorderColor" ) );
 
             colorChooser.setVisible( true );
 
         }
 
         /**
          * Listener for the "Acept" button of the color chooser dialog.
          */
         private class UpdateColorListener implements ActionListener {
 
             /*
              * (non-Javadoc)
              * 
              * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
              */
             public void actionPerformed( ActionEvent e ) {
 
                 // Update the text color
                 if( color == FRONT_COLOR ) {
                     frontColor = colorChooser.getColor( );
                     textPreviewPanel.setTextFrontColor( colorChooser.getColor( ) );
                     imagePanel.setTextFrontColor( colorChooser.getColor( ) );
                 }
                 else if( color == BORDER_COLOR ) {
                     borderColor = colorChooser.getColor( );
                     textPreviewPanel.setTextBorderColor( colorChooser.getColor( ) );
                     imagePanel.setTextBorderColor( colorChooser.getColor( ) );
                 }
                 imagePanel.repaint( );
                 textPreviewPanel.repaint( );
             }
         }
 
         /**
          * Listener for the color preview panel.
          */
         private class UpdatePreviewPanelListener implements ChangeListener {
 
             /*
              * (non-Javadoc)
              * 
              * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
              */
             public void stateChanged( ChangeEvent e ) {
 
                 if( color == FRONT_COLOR )
                     colorPreviewPanel.setTextFrontColor( colorChooser.getColor( ) );
                 else if( color == BORDER_COLOR )
                     colorPreviewPanel.setTextBorderColor( colorChooser.getColor( ) );
             }
         }
     }
 
 }
