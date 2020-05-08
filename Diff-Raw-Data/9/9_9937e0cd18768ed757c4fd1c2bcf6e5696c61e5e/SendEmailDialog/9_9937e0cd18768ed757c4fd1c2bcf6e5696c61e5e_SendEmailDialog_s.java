 package View;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.mail.AuthenticationFailedException;
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import Model.AddressBook;
 import Model.Airport;
 import Model.Contact;
 import Model.Email;
 
 import net.miginfocom.swing.MigLayout;
 
 public class SendEmailDialog extends JDialog {
 
 	private static final long serialVersionUID = 1L;
 
 	@SuppressWarnings("unused")
 	private final JPanel contentPanel = new JPanel();
 
 	private JPanel contentPane;
 	private JPanel buttonPanel;
 	private JScrollPane scrollPane;
 	private JTable table;
 	private JButton btnSend;
 	private JButton btnCancel;
 
 	public SendEmailDialog(AddressBook addressBook, Airport airport) {
 		setTitle("Address Book");
 		setBounds(100, 100, 481, 300);
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
 		contentPane = new JPanel();
 		setContentPane(contentPane);
 		contentPane.setLayout(new MigLayout("", "[grow][grow]", "[grow]"));
 
 		scrollPane = new JScrollPane();
 		contentPane.add(scrollPane, "cell 0 0,grow");
 
 		@SuppressWarnings("unused")
 		JCheckBox jc1 = new JCheckBox();
 		table = new JTable(){
 			private static final long serialVersionUID = 1L;
 
 			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
 				Component component = super.prepareRenderer(renderer, row, column);
 				int rendererWidth = component.getPreferredSize().width;
 				TableColumn tableColumn = getColumnModel().getColumn(column);
 				tableColumn.setPreferredWidth(Math.max(rendererWidth +
 						getIntercellSpacing().width,
 						tableColumn.getPreferredWidth()));
 				return  component;
 			}
 		};
 		//table.setAutoResizeMode(JTable.AUTO_RESIZE_ON); // turns this table into a 'spring' table		
 
 		ContactsTableModel tableModel = new ContactsTableModel();
 
 		table.setModel(tableModel);
 		table.setRowSelectionAllowed(false);
 
 		scrollPane.setViewportView(table);
 
 		for(Contact c : addressBook.getContacts()){
 			tableModel.addContact(c);
 		}
 
 		buttonPanel = new JPanel();
 		contentPane.add(buttonPanel, "cell 1 0,grow");
 
 		btnSend = new JButton("Send");
 		btnSend.addActionListener(new sendButtonListener(tableModel.getSelected(), airport));
 		buttonPanel.setLayout(new MigLayout("", "[65px]", "[23px][grow][23px]"));
 		buttonPanel.add(btnSend, "cell 0 0,growx,aligny top");
 
 		btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 		});
 		buttonPanel.add(btnCancel, "cell 0 2,grow");
 		setAlwaysOnTop(true);
 		setVisible(true);
 	}
 
 	class sendButtonListener implements ActionListener{
 
 		List<Contact> contacts; 
 		Airport airport;
 		String subject;
 		StringBuilder body;
 
 		public sendButtonListener(List<Contact> contacts, Airport airport){
 			this.contacts = contacts;
 			this.airport = airport;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			body = new StringBuilder();
 			
 			if(airport.getCurrentRunway() != null && airport.getCurrentPhysicalRunway() != null){
 				subject = "Calculations for " + airport.getCurrentRunway().getName() +" at " +airport.getName();
 				body.append(airport.getCurrentPhysicalRunway().toDetails(airport.getCurrentRunway().getName()));
 				body.append(airport.getCurrentPhysicalRunway().getRunway(0).getName());
 				body.append(airport.getCurrentPhysicalRunway().toCalculation(airport.getCurrentPhysicalRunway().getRunway(0).getName()));
 				body.append(airport.getCurrentPhysicalRunway().getRunway(1).getName());
 				body.append(airport.getCurrentPhysicalRunway().toCalculation(airport.getCurrentPhysicalRunway().getRunway(1).getName()));
 			}
 			else {
 				JOptionPane.showMessageDialog(null, "No current obstacle!");
 				return;
 			}
 
 			new Thread(){
 				public void run(){
 					for (Contact c : contacts){
 						System.out.println(c.getEmail());
 					}
 					if(contacts.size() != 0){
 						Email email = new Email();
 						email.addRecipients(contacts);
 						email.setSubject(subject);
 						email.setBody(body.toString());
 						try {
 							email.send();
 						} catch (AddressException e) {
 							JOptionPane.showMessageDialog(null, "There is a problem with one or more recipients email address(es).", "", JOptionPane.ERROR_MESSAGE);
 						} catch (MessagingException e) {
 							JOptionPane.showMessageDialog(null, "There has been an error sending the email, please check the settings and try again.", "", JOptionPane.ERROR_MESSAGE);
 						}
 					}
					JOptionPane.showMessageDialog(null, "Emails have been sent", "", JOptionPane.INFORMATION_MESSAGE);
 				}
 			}.start();
 			
 			dispose();
 			
 		}
 	}
 
 	class ContactsTableModel extends AbstractTableModel {
 
 		private static final long serialVersionUID = 1L;
 		private String[] columnNames = {"Send","First name", "Last name" ,"Email"};
 		private List<Contact> contacts = new ArrayList<Contact>();
 		private List<Contact> selected = new ArrayList<Contact>();
 
 		public void addContact(Contact contact){
 			contacts.add(contact);
 		}
 
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 
 		public int getRowCount() {
 			return contacts.size();
 		}
 
 		public String getColumnName(int col) {
 			return columnNames[col];
 		}
 
 		public Object getValueAt(int row, int col) {
 			switch(col){
 			case 0 : return selected.contains(contacts.get(row));
 			case 1 : return contacts.get(row).getFirstName();
 			case 2 : return contacts.get(row).getLastName();
 			case 3 : return contacts.get(row).getEmail();
 			default : return null;
 			}
 		}
 
 		@SuppressWarnings({ "unchecked", "rawtypes" })
 		public Class getColumnClass(int c) {
 			return getValueAt(0, c).getClass();
 		}
 
 
 		public boolean isCellEditable(int row, int col) {
 			//Note that the data/cell address is constant,
 			//no matter where the cell appears onscreen.
 			if (col > 0) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 
 		public void setValueAt(Object value, int row, int col) {
 			if (col == 0){ 
 				Contact contact = contacts.get(row);
 				if(selected.contains(contact)) selected.remove(contact);
 				else selected.add(contact);			
 			}
 			fireTableCellUpdated(row, col);
 		}
 
 		public List<Contact> getSelected(){
 			return selected;
 		}
 	}
 }
