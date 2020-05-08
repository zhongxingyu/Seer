 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import com.community.xanadu.components.table.BeanReaderJTable;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Contact;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Database;
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
 	private static final Database db = new Database();
	private static final BeanReaderJTable<Contact> table = 
						new BeanReaderJTable<Contact>(new String[] {"FullName", "AllEmails", "AllPhones"},
																					new String[] {"Name", "Emails", "Phone"});																									
																				
 	private static final TableRowSorter<BeanReaderJTable.GenericTableModel> sorter = new TableRowSorter<BeanReaderJTable.GenericTableModel>(table.getModel());
 
 	/**
 	 * Constructor
 	 */
 	public ContactsPanel(ListSelectionListener listSelectionListener) {
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		JLabel label = new JLabel("Contacts");
 		label.setAlignmentX(CENTER_ALIGNMENT);
 		add(label);
 		fillTable(db.getAllContacts());
 		table.getSelectionModel().addListSelectionListener(listSelectionListener);
 		table.setRowSorter(sorter);
 		JScrollPane scrollPane = new JScrollPane(table);
 		filter("");
 		add(scrollPane);
 	}
 
 	/**
 	 * Fill table with data from list
 	 */
 	void fillTable(List<Contact> contacts) {
 		final RowFilter filter = sorter.getRowFilter();	//Warnings!
 		sorter.setRowFilter(null);
 		table.clear();
 		table.addRow(contacts);
 		//System.out.println(model.getDataVector());
 		sorter.setRowFilter(filter);
 	}
 
 	/**
 	 * Filter contacts
 	 *
 	 * @param f String to filter
 	 */
 	void filter(String f) {
 		//System.out.println("Filtering: " + f);
 		sorter.setRowFilter(RowFilter.regexFilter("(?i)" + f));
 	}
 
 	Contact getSelectedContact() {
 		return table.getSelectedObject();
 	}
 }
