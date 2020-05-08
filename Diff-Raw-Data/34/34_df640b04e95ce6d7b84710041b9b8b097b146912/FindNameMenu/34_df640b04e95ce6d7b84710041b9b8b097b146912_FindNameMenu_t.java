 package phonebook;
 import javax.swing.*;
 import java.awt.event.*;
 import java.util.List;
 
 @SuppressWarnings("serial")
 public class FindNameMenu extends JMenuItem implements ActionListener {
 	private PhoneBook phoneBook;
 	private PhoneBookGUI gui;
 	
 	public FindNameMenu(PhoneBook phoneBook, PhoneBookGUI gui) {
 		super("Find names(s)");
 		this.phoneBook = phoneBook;
 		this.gui = gui;
 		addActionListener(this);
 	}
 	
 	 public void actionPerformed(ActionEvent e) {
 		String s = JOptionPane.showInputDialog("Enter name");
		if (s == null || s.isEmpty()) {
			return;
 		}
		List<String> numbers = phoneBook.findNumber(s);
		StringBuilder sb = new StringBuilder("Numbers for name " + s + ":\n");
		for (String number : numbers) {
			sb.append(number + "\n");
		}
		gui.setText(sb.toString());
 	 }
 }
