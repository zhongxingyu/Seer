 package pala.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 
 import pala.bean.InputItem;
 import pala.bean.Item;
 import pala.repository.ApplicationContent;
 import pala.repository.InputItemRepositoryImpl;
 import pala.repository.ItemRepositoryImpl;
 import javax.swing.JComboBox;
 import javax.swing.table.DefaultTableModel;
 
 import org.springframework.data.neo4j.conversion.EndResult;
 
 public class MainApp {
 
 	private JFrame frame;
 	private JTextField txtName;
 	private JTextField txtDescription;
 	private JTable table;
 	private JTextField txtConsole;
 	private JComboBox cbxItem;
 	private JTextField txtCost;
 	private JTextField txtSearch;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainApp window = new MainApp();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public MainApp() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 513, 420);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
 		
 		JPanel pnlAdmin = new JPanel();
 		tabbedPane.addTab("Administration", null, pnlAdmin, null);
 		pnlAdmin.setLayout(null);
 		
 		JLabel lblItemName = new JLabel("Item Name:");
 		lblItemName.setBounds(10, 11, 71, 14);
 		pnlAdmin.add(lblItemName);
 		
 		txtName = new JTextField();
 		txtName.setBounds(89, 8, 213, 20);
 		pnlAdmin.add(txtName);
 		txtName.setColumns(10);
 		final ItemRepositoryImpl itemRepo = ApplicationContent.applicationContext.getBean(ItemRepositoryImpl.class);
 		JButton btnAdd = new JButton("Add");
 		btnAdd.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				
 				itemRepo.addItem(txtName.getText(), txtDescription.getText());
 				Item item = itemRepo.findItemNamed(txtName.getText());
 				if(item != null) {
 					txtConsole.setText(item.getName() + item.getDescription());
 					((DefaultTableModel)table.getModel()).addRow(new String[]{item.getId().toString(), item.getName(), item.getDescription()});
 					JOptionPane.showMessageDialog(frame, "Added Item successfully.");
 				} else {
 					JOptionPane.showMessageDialog(frame, "Added Item unsuccessfully.");
 				}
 			}
 		});
 		btnAdd.setBounds(318, 38, 89, 23);
 		pnlAdmin.add(btnAdd);
 		
 		txtDescription = new JTextField();
 		txtDescription.setBounds(89, 39, 213, 20);
 		pnlAdmin.add(txtDescription);
 		txtDescription.setColumns(10);
 		
 		JLabel lblDescription = new JLabel("Description:");
 		lblDescription.setBounds(10, 42, 71, 14);
 		pnlAdmin.add(lblDescription);
 		
 		loadItemTable();
 		// Create the scroll pane and add the table to it.
 		JScrollPane scrollPane = new JScrollPane(table);
 		scrollPane.setBounds(10, 189, 397, 139);
 
 		// Add the scroll pane to this panel.
 		pnlAdmin.add(scrollPane);
 		
 		txtConsole = new JTextField();
 		txtConsole.setBounds(10, 96, 397, 20);
 		pnlAdmin.add(txtConsole);
 		txtConsole.setColumns(10);
 		
 		JPanel pnlInput = new JPanel();
 		tabbedPane.addTab("Input", null, pnlInput, null);
 		pnlInput.setLayout(null);
 		
 		JLabel lblItem = new JLabel("Item:");
 		lblItem.setBounds(10, 11, 46, 14);
 		pnlInput.add(lblItem);
 		
 		cbxItem = new JComboBox();
 		cbxItem = loadCbxItems();
 		cbxItem.setBounds(66, 8, 148, 17);
 		pnlInput.add(cbxItem);
 		
 		txtCost = new JTextField();
 		txtCost.setBounds(66, 39, 148, 20);
 		pnlInput.add(txtCost);
 		txtCost.setColumns(10);
 		
 		JButton btnAdd_1 = new JButton("Add");
 		btnAdd_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				ItemRepositoryImpl itemRepo = ApplicationContent.applicationContext.getBean(ItemRepositoryImpl.class);
 				InputItemRepositoryImpl inputItemRepo = ApplicationContent.applicationContext.getBean(InputItemRepositoryImpl.class);
 				Item item = (Item)cbxItem.getSelectedItem();
 				
 				inputItemRepo.addItem(item, Double.parseDouble(txtCost.getText()));
 				
 				EndResult<InputItem> results = inputItemRepo.findAllItems();
 				Iterator<InputItem> iter = results.iterator();
 				while(iter.hasNext()) {
 					InputItem inputItem = iter.next();
 					System.out.println(inputItem.getCost());
 				}
 				
 			}
 		});
 		btnAdd_1.setBounds(246, 38, 89, 23);
 		pnlInput.add(btnAdd_1);
 		
 		JPanel pnlReport = new JPanel();
 		tabbedPane.addTab("Report", null, pnlReport, null);
 		
 		JLabel lblSearch = new JLabel("Search:");
 		pnlReport.add(lblSearch);
 		
 		txtSearch = new JTextField();
 		pnlReport.add(txtSearch);
 		txtSearch.setColumns(10);
 		
 		JButton btnOk = new JButton("Ok");
 		btnOk.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				ItemRepositoryImpl itemRepo = ApplicationContent.applicationContext.getBean(ItemRepositoryImpl.class);
 				Item item = itemRepo.findItemNamed(txtSearch.getText());
 				for(InputItem inputItem : item.getInputItems()) {
					System.out.println("Report" + inputItem.getCost());
 				}
 			}
 		});
 		pnlReport.add(btnOk);
 	}
 
 	private JComboBox<Item> loadCbxItems() {
 		ItemRepositoryImpl itemRepo = ApplicationContent.applicationContext.getBean(ItemRepositoryImpl.class);
 		Iterator<Item> items = itemRepo.findAllItems().iterator();
 		Vector<Item> data = new Vector<Item>();
 		while(items.hasNext()) {
 			data.add(items.next());
 		}
 		JComboBox<Item> cbxItem = new JComboBox<Item>(data);
 		return cbxItem;
 	}
 
 	private void loadItemTable() {
 		ItemRepositoryImpl itemRepo = ApplicationContent.applicationContext.getBean(ItemRepositoryImpl.class);
 		EndResult<Item> items = itemRepo.findAllItems();
 		String[] columnNames = {"ID", "Name", "Description"};
 		Vector<String> columns = new Vector<String>(Arrays.asList(columnNames));
 		Iterator<Item> iter = items.iterator();
 		Vector<Vector<String>> rows = new Vector<Vector<String>>();
 		while (iter.hasNext()) {
 			Item type = (Item) iter.next();
 			Vector<String> row = new Vector<String>();
 			row.add(type.getId().toString());
 			row.add(type.getName());
 			row.add(type.getDescription());
 			rows.add(row);
 		}
 		
 		table = new JTable(rows, columns);
 	}
 }
