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
 import java.io.*;
 /**
  *
  * @author Bryce McGaw and Jonathan Yau
  * @author Ryan Halbrook and Yun Suk Chang
  * @version CS56 Spring 2013
  * Edited Professor Phill Conrad's code from Lab06
  */
 public class MemoryGameGui {
 
     static final int WINDOW_SIZE = 500;
 
     static JFrame frame = new JFrame("Memory Card Game");
     static MemoryGrid grid = new MemoryGrid(16);
     static MemoryGameComponent mgc = new MemoryGameComponent(grid);
     static JButton reset = new JButton("Reset");
     static JButton pause = new JButton("Pause");
     static boolean isPaused = false;
    static JLabel label = new JLabel("Time Remaining: 75");
     static JFrame instruction = new JFrame("Instruction");
     static JTextArea text = new JTextArea(15,25);
 
     /** main method to open JFrame 
      *
      */
     
     public static void main (String[] args) {
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	instruction.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 	ActionListener pauseListener = new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 			if(isPaused){
 				isPaused=false;
 				mgc.resume();
 				
 			}	
 			else{
 				isPaused=true;
 				mgc.pause();
 			}
 		}
 	};
 	ActionListener resetListener = new ActionListener(){
 		public void actionPerformed(ActionEvent e){
 		    mgc.pause();
 	            JOptionPane popup = new JOptionPane("Warning!");
 	            Object[] options= {"Start Over","Cancel"};
 	
 	            int selection=popup.showOptionDialog(
 	                    null,
 	                    "Do you want to restart this level?",
 	                    "Warning!",
        		             JOptionPane.YES_NO_OPTION,
         	            JOptionPane.INFORMATION_MESSAGE, null,
         	            options, options[0]);
 
         	    if(selection==JOptionPane.YES_OPTION)
         	            mgc.reset();
 		    else
 			mgc.resume();
 		}
 	};
 
 	pause.addActionListener(pauseListener);
 	reset.addActionListener(resetListener);
 	frame.getContentPane().add(mgc);
 	JPanel p = new JPanel(new BorderLayout());
 	p.add(BorderLayout.WEST,label);
 	JPanel p2 = new JPanel(new BorderLayout());
 	p.add(BorderLayout.EAST,p2);
 	p2.add(BorderLayout.WEST,pause);
 	p2.add(BorderLayout.EAST, reset);
 
 
 	frame.getContentPane().add(BorderLayout.SOUTH,p);
 
 
 	// to make sure that grids go left to right
 	mgc.setLabel(label);
 	frame.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 	frame.setSize(WINDOW_SIZE, WINDOW_SIZE);
 
 	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 	frame.setLocation((int)(screenSize.getWidth()/2 - frame.getSize().getWidth()/2), (int)(screenSize.getHeight()/2 - frame.getSize().getHeight()/2));
 	frame.setVisible(true);
 
 	JPanel panel = new JPanel();
 	text.setLineWrap(true);
 	text.setEditable(false);
 	JScrollPane scroller = new JScrollPane(text);
 	scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 	scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 	panel.add(scroller);
 	
 	instruction.getContentPane().add(panel);
         instruction.setSize(350,350);
 	addInstruction();
 	instruction.setLocation((int)(screenSize.getWidth()/2 - instruction.getSize().getWidth()/2), (int)(screenSize.getHeight()/2 - instruction.getSize().getHeight()/2));
 	instruction.setVisible(true);
 
     }
     public static void addInstruction(){
 	File file = new File("instructions.txt");
 	try {
 	    BufferedReader br = new BufferedReader(
 	                        new InputStreamReader(
 	                        new FileInputStream(file)));
 	    String line;
 	    while((line = br.readLine()) != null){
 		text.append(line+"\n");
 	    }
 	    br.close();
 	} catch	(IOException e) {
 	   e.printStackTrace();
 	} 
     }
 }
