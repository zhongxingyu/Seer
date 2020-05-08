 package phonebook;
 import javax.swing.*;
 import java.awt.event.*;
 
 public class RemoveMenu extends JMenuItem implements ActionListener {
 	private PhoneBook phoneBook;
 	private PhoneBookGUI gui;
 	
 	public RemoveMenu(PhoneBook phoneBook, PhoneBookGUI gui) {
 		super("Remove");
 		this.phoneBook = phoneBook;
 		this.gui = gui;
 		addActionListener(this);
 	}
 	
 	 public void actionPerformed(ActionEvent e) {
		String s  = JOptionPane.showInputDialog("Enter name");
		if (s == null || s.isEmpty()) {
			return;
		}
		phoneBook.remove(s);
 	 }
 }
