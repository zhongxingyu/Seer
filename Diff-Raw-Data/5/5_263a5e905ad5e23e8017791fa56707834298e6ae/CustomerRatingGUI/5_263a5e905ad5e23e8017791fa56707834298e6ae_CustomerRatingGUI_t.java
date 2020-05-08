 package mediastore;
 
 /**
  * A class that implements the UI for a customer to rate
  * a media item
  *
  * @author Ryan Smith
  * @version 1.0 Mar 22, 2013
  *
  */
 import java.awt.*;
 import javax.swing.*;
 import java.awt.event.*;
 
 public class CustomerRatingGUI extends JFrame implements ActionListener, ItemListener {
 
     private JRadioButton oneStarButton;
     private JRadioButton twoStarButton;
     private JRadioButton threeStarButton;
     private JRadioButton fourStarButton;
     private JRadioButton fiveStarButton;
     private JButton rateButton;
     private JButton backButton;
     private JLabel headerLabel;
     private ButtonGroup buttons;
     private Font headerFont;
     private JPanel topPanel;
     private JPanel mainPanel;
     private JPanel bottomPanel;
 
     public CustomerRatingGUI() {
 
         addWindowListener( new CustomerRatingGUIExitHandler() );
         setLayout( new BorderLayout() );
 
         headerFont = new Font( "Sans", Font.PLAIN, 36 );
         
         headerLabel = new JLabel( "Rating (Out of 5 stars)" );
         headerLabel.setFont( headerFont );
         
         rateButton = new JButton( "Rate it!" );
         rateButton.addActionListener( this );
         backButton = new JButton( "Back" );
         backButton.addActionListener( this );
         
         
         oneStarButton = new JRadioButton( "1", false );
         oneStarButton.addItemListener( this );
         twoStarButton = new JRadioButton( "2", false );
         twoStarButton.addItemListener( this );
         threeStarButton = new JRadioButton( "3", false );
         threeStarButton.addItemListener( this );
         fourStarButton = new JRadioButton( "4", false );
         fourStarButton.addItemListener( this );
         fiveStarButton = new JRadioButton( "5", false );
         fiveStarButton.addItemListener( this );
 
 
         buttons = new ButtonGroup();
         buttons.add( oneStarButton );
         buttons.add( twoStarButton );
         buttons.add( threeStarButton );
         buttons.add( fourStarButton );
         buttons.add( fiveStarButton );
 
         topPanel = new JPanel();
         mainPanel = new JPanel();
         bottomPanel = new JPanel();
 
         topPanel.add( headerLabel );
         
         mainPanel.add( oneStarButton );
         mainPanel.add( twoStarButton );
         mainPanel.add( threeStarButton );
         mainPanel.add( fourStarButton );
         mainPanel.add( fiveStarButton );
 
         bottomPanel.add( rateButton );
         bottomPanel.add( backButton );
 
         add(topPanel, BorderLayout.NORTH);
         add(mainPanel, BorderLayout.CENTER);
         add( bottomPanel, BorderLayout.SOUTH);
         
         
         
     }
 
     @Override
     public void actionPerformed( ActionEvent ae ) {
         
         if( ae.getSource() == backButton ) {
            MediaStoreGUI.customerPurchaseHistoryScreen(false);
         }
         if( ae.getSource() == rateButton ) {
             //TODO: implement rate system
         }
         
     }
 
     @Override
     public void itemStateChanged( ItemEvent ie ) {
         
     }
 
     private class CustomerRatingGUIExitHandler extends WindowAdapter {
 
         @Override
         public void windowClosing( WindowEvent e ) {
            MediaStoreGUI.customerPurchaseHistoryScreen(false);
         }
     }
 }
