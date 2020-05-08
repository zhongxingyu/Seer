 package edu.ucsb.cs56.S12.issues.issue0000805;
 import java.awt.GridLayout;
 import javax.swing.JComponent;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Font;
 import java.awt.*;
 import java.util.ArrayList;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTextArea;
 import javax.swing.*;
 import java.awt.ComponentOrientation;
 
 /**
  *
  * @author Bryce McGaw and Jonathan Yau
  * Edited Professor Phill Conrad's code from Lab06
  */
 public class MemoryGameGui {
 
     static final int WINDOW_SIZE = 500;
 
     static JFrame frame = new JFrame("Memory Card Game");
     static MemoryGrid grid = new MemoryGrid(16);
     static MemoryGameComponent mgc = new MemoryGameComponent(grid);
     //static JButton restartB = new JButton("Restart");
     static RestartButtonHandler RBHandler;
     static JLabel label = new JLabel("0");
 
     /** main method to open JFrame 
      *
      */
     
     public static void main (String[] args) {
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 	//restartB.addActionListener(RBHandler);
 	
	frame.getContentPane().add(BorderLayout.NORTH,mgc);
 	//frame.getContentPane().add(restartB);
 	frame.getContentPane().add(BorderLayout.SOUTH,label);
 	// to make sure that grids go left to right
 	
 	frame.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 	frame.setSize(WINDOW_SIZE, WINDOW_SIZE);
 	frame.setVisible(true);
         Thread time = new Thread(new MemoryGameTimer(mgc,label));
 	time.start();
     }
     private class RestartButtonHandler implements ActionListener{
 
 	public void actionPerformed(ActionEvent e){
 
 	    grid = new MemoryGrid(16);
 	    mgc = new MemoryGameComponent(grid);
 	    
 	    frame.getContentPane().add(mgc);
 	    //frame.getContentPane().add(restartB);
 	    
 	}
     }
 
 }
