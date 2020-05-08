
 import javax.swing.*;

 import java.util.Random;
 import java.awt.image.BufferedImage;
 import java.awt.*;
 import java.awt.image.*;
 import java.awt.event.*;
 //import static java.awt.GraphicsDevice.WindowTranslucency;
 //import com.sun.awt.AWTUtilities;
 public class Hivolts_Display {
 
 	public static void main(String[] args) {
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		GraphicsDevice gd = ge.getDefaultScreenDevice();
 		JFrame.setDefaultLookAndFeelDecorated(true);
 		SwingUtilities.invokeLater(new Runnable() {
 		       	public void run(){
 				showGUI();
 			}});
 		
 
 	}
 	
 	private static void showGUI(){
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		GraphicsDevice gd = ge.getDefaultScreenDevice();
 
 		HivoltsFrame f = new HivoltsFrame();
 		f.setSize(64*12,64*12);
 
 		f.setUndecorated(true);
 		f.setBackground(new Color(0f,0f,0f,1.0f));
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.setVisible(true);
 		
 	}
 }
 
 
 class HivoltsFrame extends JFrame{
 
 	Grid hivoltsgrid = new Grid(12);
 	public	HivoltsFrame(){
 
 		
 		super();
 
 	}
 
 	public void paint(Graphics g){
 
 		
 		hivoltsgrid.drawAll(g);
 
 	}		
 }
 
