 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import com.community.xanadu.components.table.BeanReaderJTable;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Contact;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Database;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Group;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.*;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableRowSorter;
 
 /**
  * Panel with contacts
  *
  * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
  */
class ContactsPanel extends JPanel {
 	static final long serialVersionUID = 0;
 
 	private JPopupMenu contextMenu = new JPopupMenu();
 	private MainWindow mainWindowHandle;
 
 	private static final Database db = Database.getInstance();
 	private static final BeanReaderJTable<Contact> table =
 					new BeanReaderJTable<Contact>(new String[] {"FullName", "AllEmails", "AllPhones"},
 					                              new String[] {"Name", "Emails", "Phones"});
 
 	private static final TableRowSorter<BeanReaderJTable.GenericTableModel> sorter =
 					new TableRowSorter<BeanReaderJTable.GenericTableModel>(table.getModel());
 
 	/**
 	 * Constructor
 	 */
 	public ContactsPanel(MainWindow handle, ListSelectionListener listSelectionListener) {
 		this.mainWindowHandle = handle;
 
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		JLabel label = new JLabel("Contacts");
 		label.setAlignmentX(CENTER_ALIGNMENT);
 		add(label);
 
 		fillTable();
 		this.mainWindowHandle.handleContactActionsVisibility();
 
 		table.getSelectionModel().addListSelectionListener(listSelectionListener);
 		table.setRowSorter(sorter);
 		table.setDefaultRenderer(Object.class, new TableRowColorRenderer());
 		JScrollPane scrollPane = new JScrollPane(table);
 		filter("");
 		add(scrollPane);

 		this.initContextMenu();
 	}
 
 	/**
 	 * Fill table with data from list
 	 */
 	static void fillTable() {
 		final RowFilter filter = sorter.getRowFilter();	//Warnings!
 
 		Contact selectedContact = getSelectedContact();
 
 		Group[] groups = GroupsPanel.getSelectedGroups();
 		List<Group> selectedGroups = Arrays.asList(groups);
 		final List<Contact> contacts = db.getAllContactsFromGroup(selectedGroups);
 
 		sorter.setRowFilter(null);
 		table.clear();
 		table.addRow(contacts);
 		sorter.setRowFilter(filter);
 	}
 
 	/**
 	 * Filter contacts
 	 *
 	 * @param f String to filter
 	 */
 	void filter(String f) {
 		sorter.setRowFilter(RowFilter.regexFilter("(?i)" + f));
 	}
 
 	/**
 	 * Get selected contacts
 	 */
 	static Contact getSelectedContact() {
 		return table.getSelectedObject();
 	}
 
 	/**
 	 * Get all selected contacts.
 	 * 
 	 * @return
 	 */
 	static Contact[] getSelectedContacts() {
 		Object[] objects =  table.getSelectedObjects();
 		Contact[] contacts = new Contact[objects.length];
 		for (int i = 0; i < objects.length; i++) {
 			contacts[i] = (Contact) objects[i];
 		}
 		return contacts;
 	}
 
 	/**
 	 * Method for removing contact
 	 */
 	static boolean removeContact() {
 		final Contact contact = getSelectedContact();
 
 		final int delete = JOptionPane.showConfirmDialog(
 			null,
 			"Delete contact " + contact.getFullName() + "?",
 			"Delete contact",
 			JOptionPane.YES_NO_OPTION,
 			JOptionPane.QUESTION_MESSAGE,
 			new ImageIcon(ContactsPanel.class.getResource("/res/minus.png"), "-")
 		);
 
 		if (delete == 0) {
 			List<Contact> contactToRemove = new ArrayList<Contact>();
 			contactToRemove.add(contact);
 			db.removeContacts(contactToRemove);
 			ContactsPanel.fillTable();
 			GroupsPanel.fillList();
 			return true;
 		}
 
 		return false;
 	}
 
 	private void initContextMenu() {
 
 		this.contextMenu.add(this.mainWindowHandle.actions.actionNewContact);
 		this.contextMenu.add(this.mainWindowHandle.actions.actionEditContact);
 		this.contextMenu.add(this.mainWindowHandle.actions.actionDeleteContact);
 		this.contextMenu.addSeparator();		
 		this.contextMenu.add(this.mainWindowHandle.actions.actionManageContactGroups);		
 		this.contextMenu.addSeparator();		
 		this.contextMenu.add(this.mainWindowHandle.actions.actionImport);
 		this.contextMenu.add(this.mainWindowHandle.actions.actionExport);
 
 		MouseListener popupListener = new PopupListener();
 		table.addMouseListener(popupListener);
 	}
 
 
 	private class PopupListener extends MouseAdapter {
 		@Override
 		public void mouseClicked(MouseEvent e){
 			if (e.getClickCount() == 2){
 				new ContactWindow(getSelectedContact());
 				}
 			}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			showPopup(e);
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			showPopup(e);
 		}
 
 		private void showPopup(MouseEvent e) {
 			if (e.isPopupTrigger()) {
 				contextMenu.show(e.getComponent(), e.getX(), e.getY());
 			}
 		}
 	}
 }
