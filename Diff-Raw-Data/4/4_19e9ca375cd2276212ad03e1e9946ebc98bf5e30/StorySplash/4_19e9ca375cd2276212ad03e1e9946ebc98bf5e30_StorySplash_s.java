 package client;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 /**
  *  Splash screen for game
  * @author wheelemaxw
  *
  */
 
 public class StorySplash extends JFrame implements ActionListener {
 
 	private static final long serialVersionUID = 1L; //to stop warnings
 	private final String storyText = "You awaken in a strange dungeon with no memory of how you got there. \n" +
 	"All you know is that its kind of dark in here and that something does not " +
 	"seem quite right. " +
 	"\n \n" +
 	"There is a man in the distance, maybe he will know more about " +
 	"what is going on"+
 	"\n \n \n \n Click to Play";
 	
 	public StorySplash(){
 		this.setTitle("Run,Escape");
 		JTextArea textf = new JTextArea();
 		JButton b = new JButton("Click to Play");
 		b.setActionCommand("play");
 		this.add(b, BorderLayout.SOUTH);
 		
 		b.addActionListener(this);
 		
		String text = storyText;
		textf.append(text);
 		
 		this.add(textf, BorderLayout.NORTH);
 		this.pack();
 		this.setAlwaysOnTop(true);
         this.setLocationRelativeTo(null);
         this.setVisible(true);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 	    if (e.getActionCommand().equals("play")) {
 	    	this.dispose();
 	    	}
 	}
 	
 }
