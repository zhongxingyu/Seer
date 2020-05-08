 package combat;
 /** 
  * @author DevC
  * @version $Id: KeyDialog.java,v 1.5 2012/04/08 03:50:18 DevA Exp $
  *
  * KeyDialog allows users the option to map key commands to their liking
  *
  * Revision History:
  *   $Log: KeyDialog.java,v $
  *   Revision 1.5  2012/04/08 03:50:18  DevA
  *   Cleaned up the code to run with Java 1.6: removed unused imports,
  *   fixed some UI focus issues (introduced by new focus "features" in Java since
  *   our original implementation), and made the CommandInterpreter not a Singleton
  *
  *   Revision 1.4  2003/05/30 18:46:45  DevB
  *   Reformatted.
  *
  *   Revision 1.3  2000/05/09 04:14:16  DevC
  *   worked further; replaced textFields w/ specialized
  *   keyBoxes, and implemented the listeners
  *
  *   Revision 1.2  2000/05/09 02:22:35  DevC
  *   implemented the createDialog method to build the dialog;
  *   set up the stubs for the button listeners
  *
  *   Revision 1.1  2000/05/09 01:33:39  DevC
  *   Initial revision
  *
  */
 
 import java.awt.event.*;
 import java.awt.*;
 import javax.swing.*;
 
 public class KeyDialog extends JDialog
 {
 	private static final long serialVersionUID = -1;
     KeyBox p1Up;
     KeyBox p1Down;
     KeyBox p1Right;
     KeyBox p1Left;
     KeyBox p1Fire;
     
     KeyBox p2Up;
     KeyBox p2Down;
     KeyBox p2Right;
     KeyBox p2Left;
     KeyBox p2Fire;
     private KeyBinding playerOneKeys;
     private KeyBinding playerTwoKeys;
     
     JPanel mainPanel;
     private final CommandInterpreter theCi;
     
     /**
      * Creates this dialog.
      * @param   mainFrame   The frame of the game (which I am part of).
      */
     public KeyDialog( JFrame mainFrame, CommandInterpreter ci)
     {
         //call the parent constructor 
         super( mainFrame, "Key Mapping" );
         
         this.theCi = ci;
         
         //fill the frame with the fields, and then construct it
         this.createDialog();
     }
 
     /**
      * Handles building the dialog box.
      */
     private void createDialog(  )
     {                   
         //create the components and add them to the panel
         mainPanel = new JPanel( );
         
         //Loads the current key bindings into the key dialog
         setupInitialKeys(this.theCi.getKeyBindings(1), this.theCi.getKeyBindings(2));
         
         mainPanel.setLayout( new GridLayout( 6, 3 ) );
         
         //add the test fields to the panel
         mainPanel.add( new JLabel( "" ) );
         mainPanel.add( new JLabel( "Player 1" ) );
         mainPanel.add( new JLabel( "Player 2" ) );
         mainPanel.add( new JLabel( "Up" ) );
         mainPanel.add( p1Up );
         mainPanel.add( p2Up );
         mainPanel.add( new JLabel( "Down" ) );      
         mainPanel.add( p1Down );
         mainPanel.add( p2Down );
         mainPanel.add( new JLabel( "Left" ) );
         mainPanel.add( p1Left );
         mainPanel.add( p2Left );
         mainPanel.add( new JLabel( "Right" ) );
         mainPanel.add( p1Right );
         mainPanel.add( p2Right );
         mainPanel.add( new JLabel( "Fire" ) );
         mainPanel.add( p1Fire );
         mainPanel.add( p2Fire );
         
         getContentPane().add( mainPanel, BorderLayout.CENTER );
       
         JButton okButton = new JButton( "OK" );
         JButton cancelButton = new JButton( "Cancel" );
         ActionListener cbl = new CancelButtonListener();
         ActionListener obl = new OKButtonListener();
         
         okButton.addActionListener( obl );
         cancelButton.addActionListener( cbl );
         
         JPanel buttonPanel = new JPanel();
         
         buttonPanel.setLayout( new FlowLayout() );
         buttonPanel.add( cancelButton );
         buttonPanel.add( okButton );
         
         getContentPane().add( buttonPanel, BorderLayout.SOUTH );
         
         pack();
         paint( getGraphics() ); 
     }
 
     /**
      * Sets the KeyBox to the key bindings being used in the game.
      * @param playerOneKeys 
      * @param playerTwoKeys 
      */
     private void setupInitialKeys(KeyBinding playerOneKeys, KeyBinding playerTwoKeys)
     {
         this.playerOneKeys = playerOneKeys;
         this.playerTwoKeys = playerTwoKeys;
         
         p1Up = new KeyBox();
         p1Up.setKeyCode(playerOneKeys.getUpKey());
         
         p1Down = new KeyBox();
         p1Down.setKeyCode(playerOneKeys.getDownKey());
         
         p1Right = new KeyBox();
         p1Right.setKeyCode(playerOneKeys.getRightKey());
         
         p1Left = new KeyBox();
         p1Left.setKeyCode(playerOneKeys.getLeftKey());
         
         p1Fire = new KeyBox();
         p1Fire.setKeyCode(playerOneKeys.getFireKey());
         
         p2Up = new KeyBox();
         p2Up.setKeyCode(playerTwoKeys.getUpKey());
         
         p2Down = new KeyBox();
         p2Down.setKeyCode(playerTwoKeys.getDownKey());
         
         p2Right = new KeyBox();
         p2Right.setKeyCode(playerTwoKeys.getRightKey());
         
         p2Left = new KeyBox();
         p2Left.setKeyCode(playerTwoKeys.getLeftKey());
         
         p2Fire = new KeyBox();
         p2Fire.setKeyCode(playerTwoKeys.getFireKey());
     }
 
     public class OKButtonListener implements ActionListener
     {    
         public OKButtonListener(){}
         
         public void actionPerformed( ActionEvent e )
         {
             System.out.println( "ok button pressed " );
 
             //Set the player one key bindings from the key boxes components
             playerOneKeys.setUpKey(p1Up.getKeyCode()); 
             playerOneKeys.setDownKey(p1Down.getKeyCode());
             playerOneKeys.setRightKey(p1Right.getKeyCode());
             playerOneKeys.setLeftKey(p1Left.getKeyCode());
             playerOneKeys.setFireKey(p1Fire.getKeyCode());
 
             //Set the player two key bindings from the boxes components 
             playerTwoKeys.setUpKey(p2Up.getKeyCode());
             playerTwoKeys.setDownKey(p2Down.getKeyCode());
             playerTwoKeys.setRightKey(p2Right.getKeyCode());
             playerTwoKeys.setLeftKey(p2Left.getKeyCode());
             playerTwoKeys.setFireKey(p2Fire.getKeyCode());
             
             //Set the players bindings back to the player managers
             theCi.register(1, playerOneKeys);
             theCi.register(2, playerTwoKeys);
            
            dispose();
         }
     }
 
     public class CancelButtonListener implements ActionListener 
     {
         public CancelButtonListener() {}
         
         public void actionPerformed( ActionEvent e ){
            dispose();
         }
     }
 
 }
