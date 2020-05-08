 package final_project.view;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.util.Vector;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableRowSorter;
 
 import net.java.balloontip.BalloonTip;
 
 public class SubscriberAdminPanel extends JPanel implements ActionListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
	private MyFocusTraversalPolicy tooltipPolicy;
 	private JTable table;
 	private JSearchTextField searchField;
 	private TableRowSorter<SubscriberTableModel> sorter;
 	BalloonTip addNewSubscriberTip;
 	AddNewSubscriberPanel addNewSubscriberPane;
 
 	/**
 	 * Create the panel.
 	 */
 	public SubscriberAdminPanel() {
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{150, 0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		//Set up table with custom sorter
 		SubscriberTableModel model = new SubscriberTableModel();
 		sorter = new TableRowSorter<SubscriberTableModel>(model);
 		
 		JLabel lblManageSubscribers = new JLabel("Manage Subscribers");
 		GridBagConstraints gbc_lblManageSubscribers = new GridBagConstraints();
 		gbc_lblManageSubscribers.gridwidth = 2;
 		gbc_lblManageSubscribers.insets = new Insets(0, 0, 5, 0);
 		gbc_lblManageSubscribers.gridx = 0;
 		gbc_lblManageSubscribers.gridy = 0;
 		add(lblManageSubscribers, gbc_lblManageSubscribers);
 		
 		searchField = new JSearchTextField();
 		searchField.getDocument().addDocumentListener(
                 new DocumentListener() {
                     public void changedUpdate(DocumentEvent e) {
                         filter();
                     }
                     public void insertUpdate(DocumentEvent e) {
                         filter();
                     }
                     public void removeUpdate(DocumentEvent e) {
                         filter();
                     }
                 });
 		GridBagConstraints gbc_searchTextField = new GridBagConstraints();
 		gbc_searchTextField.insets = new Insets(0, 0, 5, 5);
 		gbc_searchTextField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_searchTextField.gridx = 0;
 		gbc_searchTextField.gridy = 1;
 		add(searchField, gbc_searchTextField);
 		
 		JButton btnAddSubscriber = new JButton("Add Subscriber");
 		btnAddSubscriber.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				addNewSubscriberPane.setNoResults(false);
 				addNewSubscriberPane.getNameTextField().requestFocusInWindow();
 	        	addNewSubscriberPane.getNameTextField().setText("");
 	        	addNewSubscriberPane.getPhoneNumberTextField().setText("");
 				addNewSubscriberTip.setOpacity(0.9f);
 			}
 		});
 		GridBagConstraints gbc_btnAddSubscriber = new GridBagConstraints();
 		gbc_btnAddSubscriber.insets = new Insets(0, 0, 5, 0);
 		gbc_btnAddSubscriber.anchor = GridBagConstraints.EAST;
 		gbc_btnAddSubscriber.gridx = 1;
 		gbc_btnAddSubscriber.gridy = 1;
 		add(btnAddSubscriber, gbc_btnAddSubscriber);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridwidth = 2;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 2;
 		add(scrollPane, gbc_scrollPane);
 		table = new JTable(model);
 		table.setRowSorter(sorter);
 		table.setFillsViewportHeight(true);
 		scrollPane.setViewportView(table);
 		addNewSubscriberPane = new AddNewSubscriberPanel();
 		addNewSubscriberTip = new BalloonTip(btnAddSubscriber, addNewSubscriberPane, new DefaultBalloonStyle(), false);
 		addNewSubscriberTip.setOpacity(0.0f);
 		addNewSubscriberPane.getCancelButton().addActionListener(this);
		
		Vector<Component> order = new Vector<Component>(2);
		order.add(searchField);
		order.add(addNewSubscriberPane.getNameTextField());
		tooltipPolicy = new MyFocusTraversalPolicy(order);
 	}
 	
 	private void filter() {
 		RowFilter<SubscriberTableModel, Object> rf = null;
         //If current expression doesn't parse, don't update.
         try {
             rf = RowFilter.regexFilter("(?i)" + searchField.getText());
         } catch (java.util.regex.PatternSyntaxException e) {
             return;
         }
         sorter.setRowFilter(rf);
 		if (table.getRowCount() == 0) {
 			//Make tooltip visible
         	addNewSubscriberTip.setOpacity(0.9f);
         	//Clear any old text
         	addNewSubscriberPane.setNoResults(true);
         	addNewSubscriberPane.getNameTextField().setText("");
         	addNewSubscriberPane.getPhoneNumberTextField().setText("");
         	//Decide whether entered text is a name or phone number
 			try {
 				long number = Long.parseLong(searchField.getText());
 				addNewSubscriberPane.getPhoneNumberTextField().setText(searchField.getText());
 			} catch (NumberFormatException e) {
 				addNewSubscriberPane.getNameTextField().setText(searchField.getText());
 			}
         } else {
         	addNewSubscriberTip.setOpacity((float)0.0);
         }
 	}
 
 	class SubscriberTableModel extends AbstractTableModel{
 		private static final long serialVersionUID = 1L;
 		
 		private  String[] columnNames = {"Name", "Phone Number", "Subscribed To"};
 		
 		private Object[][] data = {
 			{"John Connuck", "2123007360", ""},
 			{"Bob Dylan", "5556667777", ""},
 			{"Jimi Hendrix", "5554443333", ""},
 			{"The Beatles", "", ""},
 		};
 		
 		@Override
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 		@Override
 		public int getRowCount() {
 			return data.length;
 		}
 		@Override
 		public String getColumnName(int col) {
 			return columnNames[col];
 		}
 		@Override
 		public Object getValueAt(int row, int col) {
 			return data[row][col];
 		}
 		@Override
 		public Class getColumnClass(int c) {
 			return getValueAt(0, c).getClass();
 		}
 		@Override
 		public boolean isCellEditable(int row, int col) {
 			if (col < 2) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 		@Override
 		public void setValueAt(Object value, int row, int col) {
             data[row][col] = value;
             fireTableCellUpdated(row, col);
         }
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == addNewSubscriberPane.getCancelButton()) {
 			addNewSubscriberTip.setOpacity(0.0f);
 		}
 	}
 }
