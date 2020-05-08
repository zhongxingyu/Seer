 package main.java;
 
 /**
  * Created with IDEA.
  * User: Alan Derrick
  * Date: 10/13/12
  * Time: 5:10 PM
  */
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 
 public class GetQuote extends JFrame {

    String symbol;

     public JPanel createContentPane (){
 
         // Create a bottom JPanel to place everything on.
         JPanel totalGUI = new JPanel();
         totalGUI.setLayout(null);
 
 
         // Creation of a Panel to contain the "Enter Ticker Symbol:" label
         JPanel titlePanel = new JPanel();
         titlePanel.setLayout(null);
         titlePanel.setLocation(10, 0);
         titlePanel.setSize(250, 30);
         totalGUI.add(titlePanel);
 
         JLabel redLabel = new JLabel("Enter Ticker Symbol:");
         redLabel.setLocation(0, 0);
         redLabel.setSize(150, 40);
         redLabel.setHorizontalAlignment(0);
         redLabel.setForeground(Color.red);
         titlePanel.add(redLabel);
 
 
         // Creation of a Panel to contain the user-entered text field.
         JPanel textPanel = new JPanel();
         textPanel.setLayout(null);
         textPanel.setLocation(10, 40);
         textPanel.setSize(250, 30);
         totalGUI.add(textPanel);
 
         final JTextField inputField = new JTextField(16);
         inputField.setLocation(0, 0);
         inputField.setSize(100, 30);
         inputField.setHorizontalAlignment(0);
         textPanel.add(inputField);
 
 
         // Creation of a Panel to contain all the JButtons.
         JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(null);
         buttonPanel.setLocation(10, 80);
         buttonPanel.setSize(250, 70);
         totalGUI.add(buttonPanel);
 
 
         // Create buttons using the syntax used before.
         JButton priceButton = new JButton("Get Price");
         priceButton.setLocation(0, 0);
         priceButton.setSize(100, 30);
         priceButton.setToolTipText("Pulls stock quote");
         priceButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                symbol = inputField.getText();
                 System.out.println(symbol); // demonstrates getText() worked
             }
         });
         buttonPanel.add(priceButton);
 
         JButton quitButton = new JButton("Quit");
         quitButton.setLocation(120, 0);
         quitButton.setSize(100, 30);
         quitButton.setToolTipText("Exits the application");
         quitButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 System.exit(0);
             }
         });
         buttonPanel.add(quitButton);
 
 
         totalGUI.setOpaque(true);
         return totalGUI;
     } // end createContentPane
 
     private static void createAndShowGUI() {
 
         JFrame.setDefaultLookAndFeelDecorated(true);
         JFrame frame = new JFrame("StockQuote v1.0");
 
         //Create and set up the content pane.
         GetQuote demo = new GetQuote();
         frame.setContentPane(demo.createContentPane());
 
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(250, 190);
         frame.setVisible(true);
     } // end createAndShowGUI
 
     public static void main(String[] args) {
         //Schedule a job for the event-dispatching thread:
         //creating and showing this application's GUI.
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGUI();
             }
         });
     } // end main
 } // end class GetQuote
