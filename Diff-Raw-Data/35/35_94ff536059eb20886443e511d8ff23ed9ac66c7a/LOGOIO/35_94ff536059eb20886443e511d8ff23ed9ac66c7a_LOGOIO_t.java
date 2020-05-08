 import java.util.Scanner;
import java.awt.*;
import javax.swing.text.*; 
import javax.swing.*;
 
 public class LOGOIO {
 	private Scanner scanner;
 	public LOGOIO() {
 		scanner = new Scanner(System.in);
 	}
 	
 	public void out(String str) {
		SimpleAttributeSet set = new SimpleAttributeSet(); 
		StyleConstants.setForeground(set, Color.black);
		Document doc = LOGOPP.prev.getStyledDocument(); 
		try { 
			doc.insertString(doc.getLength(), str + "\n", set);
		} 
		catch (BadLocationException e) { 
		} 
 	}
 
 	public void debug(String str) {
 		System.out.println(str);
 	}
 
 	public void err(String str) {
		SimpleAttributeSet set = new SimpleAttributeSet(); 
		StyleConstants.setForeground(set, Color.red);
		Document doc = LOGOPP.prev.getStyledDocument(); 
		try { 
			doc.insertString(doc.getLength(), "Error:" + str + "\n", set);
		} 
		catch (BadLocationException e) { 
		} 
 	}
 	
 	public String in() {
 		return scanner.nextLine();
 	}
 }
