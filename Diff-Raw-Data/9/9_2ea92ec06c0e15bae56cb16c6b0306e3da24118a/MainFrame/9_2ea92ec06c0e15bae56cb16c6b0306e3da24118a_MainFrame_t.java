 import java.awt.*;
 import javax.swing.*;
 import java.awt.event.*;
 
 public class MainFrame extends JFrame {
 	
 	public MainFrame() {
 		super("Statistics UK");
 
         init();
 	}
 	
 	public void init() {
 		setLayout(new BorderLayout());		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		//search field for the search engine
 		SearchField searchField = new SearchField();
		this.getContentPane().add(searchField, BorderLayout.NORTH);		
 		
 		//image panel with the map
 		ImagePanel imagePanel = new ImagePanel();		
		this.getContentPane().add(imagePanel, BorderLayout.CENTER);

        this.pack();
 	}
 
     public static void main(String[] args) {
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new MainFrame().setVisible(true);
             }
         });
     }
 }
