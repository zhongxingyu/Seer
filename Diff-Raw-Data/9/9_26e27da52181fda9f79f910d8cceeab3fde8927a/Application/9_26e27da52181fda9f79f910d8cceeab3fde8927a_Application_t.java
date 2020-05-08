package controller;
 
 import javax.swing.UIManager;
 
 public class Application
 {
 	
 	public static void main(String [ ] args)
 	{
 		//Use system specific GUI
 		try
         {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (Exception _) { } // Just ignore this.
 		
 		System.out.println("Starting program");
 	}
 }
