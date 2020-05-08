 package test1;
 
 import javax.swing.JOptionPane;
 /**
  * Main GUI
  * Still working on this
  * @author yamilasusta
  *
  */
 public class Mailer {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Contacts contacts = new Contacts();
 
 		int menu = 0;
 		do {
 			menu = Integer.parseInt(JOptionPane.showInputDialog("1. Add Contact, 2. Remove contact, 3. Send mail, 0. To Exit"));
 			if(menu == 1) {
 				//Make more intuitive
 				int contacttotal = Math.abs(Integer.parseInt(JOptionPane.showInputDialog("Total contacts to be added")));
 				String[] newContact = new String[contacttotal];
 				if(contacttotal > 1)
 					newContact = JOptionPane.showInputDialog("Insert contacts separated by commas").split(",");
 				else
 					newContact[0] = JOptionPane.showInputDialog("Insert contact");
 				contacts.addContacts(newContact);
 			}
 			else if(menu == 2) {
 				String parse = JOptionPane.showInputDialog("Insert emails to delete separated by comma");
 				String[] emails = null;
 				if(parse.contains(","))
 					emails = parse.split(",");
 				else {
 					emails = new String[1];
 					emails[0] = parse;
 				}
 				for (int i = 0; i < emails.length; i++) 
 					contacts.removeContact(emails[i]);
 			}
			else if(menu == 3)
 				MailSender.sendMessage(contacts.getContacts());				
 		} while (menu != 0);
 
 		contacts.exit();
 
 	}
 
 }
