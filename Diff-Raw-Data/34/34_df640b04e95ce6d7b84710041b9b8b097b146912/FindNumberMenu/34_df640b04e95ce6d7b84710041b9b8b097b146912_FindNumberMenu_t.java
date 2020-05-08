 package phonebook;
 import javax.swing.*;
 import java.awt.event.*;
 import java.util.List;
 
 @SuppressWarnings("serial")
 public class FindNumberMenu extends JMenuItem implements ActionListener {
 	private PhoneBook phoneBook;
 	private PhoneBookGUI gui;
 	
 	public FindNumberMenu(PhoneBook phoneBook, PhoneBookGUI gui) {
 		super("Find numbes(s)");
 		this.phoneBook = phoneBook;
 		this.gui = gui;
 		addActionListener(this);
 	}
 	
 	 public void actionPerformed(ActionEvent e) {
 		String s = JOptionPane.showInputDialog("Enter number");
		if(s == null || !s.isEmpty()){
 			return;
 		}
 		List<String> names = phoneBook.findNames(s);
 		StringBuilder sb = new StringBuilder("Names for number " + s + ":\n");
 		for(String name: names){
 			sb.append(name + "\n");
 		}
 		gui.setText(sb.toString());
 	 }
 }
