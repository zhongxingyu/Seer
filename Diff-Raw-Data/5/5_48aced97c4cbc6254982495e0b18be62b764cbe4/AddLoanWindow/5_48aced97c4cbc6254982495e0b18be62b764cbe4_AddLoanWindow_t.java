 package view;
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableRowSorter;
 
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.RowSpec;
 
 import domain.Customer;
 import domain.Library;
 
 import java.awt.GridLayout;
 import java.awt.GridBagLayout;
 
 import javax.swing.JLabel;
 
 import java.awt.GridBagConstraints;
 
 import javax.swing.JTextField;
 
 import java.awt.Insets;
 
 import javax.swing.JComboBox;
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JSplitPane;
 import javax.swing.RowFilter;
 import javax.swing.RowSorter;
 
 import tablemodel.AddLoanWindowCustomerTableModel;
 import tablemodel.AddLoanWindowLoanTableModel;
 import tablemodel.DetailWindowTableModel;
 
 import java.awt.FlowLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Observable;
 import java.util.Observer;
 
 
 public class AddLoanWindow extends JFrame implements Observer{
 
 	private JPanel contentPane;
 	private JTextField textField_1;
 	private JTextField textField_2;
 	private JTable table;
 	private JTextField txtKundeSuchen;
 	private JTable table_1;
 	private TableRowSorter<? extends AbstractTableModel> sorter;
 	private String kundeSuchenText = "Kunde suchen";
 	private JPanel panel_2; 
 	
 	
 
 	
 	public AddLoanWindow(final Library library) {
 		setTitle("Ausleihe hinzufügen");
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 800, 450);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
 		gbc_panel.anchor = GridBagConstraints.NORTH;
 		gbc_panel.insets = new Insets(0, 0, 5, 5);
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 0;
 	
 		panel.setBorder(new TitledBorder(null, "Kundenauswahl", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel_4 = new JPanel();
 		FlowLayout flowLayout = (FlowLayout) panel_4.getLayout();
 		flowLayout.setAlignment(FlowLayout.LEFT);
 		panel.add(panel_4, BorderLayout.NORTH);
 		 
 		txtKundeSuchen = new JTextField();
 		txtKundeSuchen.setText(kundeSuchenText);
 		txtKundeSuchen.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (txtKundeSuchen.getText().equals(kundeSuchenText)) {
 					txtKundeSuchen.setText("");
 				} 
 			}
 		});
 		panel_4.add(txtKundeSuchen);
 		txtKundeSuchen.setColumns(10);
 		txtKundeSuchen.getDocument().addDocumentListener(  
 				  new DocumentListener()  
 				   {  
 				      public void changedUpdate(DocumentEvent e)  
 				      {  
 				    	  sorter.setRowFilter(StartWindow.getTextFilter(txtKundeSuchen));
 				      }  
 				      public void insertUpdate(DocumentEvent e)  
 				      {  
 				    	  sorter.setRowFilter(StartWindow.getTextFilter(txtKundeSuchen));
 				      }  
 				      public void removeUpdate(DocumentEvent e)  
 				      {  
 				    	  sorter.setRowFilter(StartWindow.getTextFilter(txtKundeSuchen));
 				      }  
 				   }  
 				);
 		
 		JPanel panel_5 = new JPanel();
 		panel_5.setBorder(new TitledBorder(null, "Kunde ausw\u00E4hlen", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel.add(panel_5, BorderLayout.CENTER);
 		panel_5.setLayout(new BorderLayout(0, 0));
 		
 		JScrollPane scrollPane_1 = new JScrollPane();
 		panel_5.add(scrollPane_1);
 		
 		AddLoanWindowCustomerTableModel custTableModel = new AddLoanWindowCustomerTableModel(library);
 		table_1 = new JTable();
 		table_1.setModel(custTableModel);
 		table_1.setFillsViewportHeight(true);
 		scrollPane_1.add(table_1);
 		scrollPane_1.setViewportView(table_1);
 		sorter = new TableRowSorter<> (custTableModel);
 		table_1.setRowSorter(sorter);
 		
 		table_1.addMouseListener(new MouseAdapter() {
 			   public void mouseClicked(MouseEvent e) {
 				  JTable target = (JTable)e.getSource();
 			      if (e.getClickCount() == 1) {			         
 			         int row = target.getSelectedRow();
 			         if (row > 0) {
 				         Customer cust = library.getCustomers().get(table_1.convertRowIndexToModel(row));			         
 				         table.setModel(new AddLoanWindowLoanTableModel(library, cust));
 				         table.setEnabled(true);
 				         panel_2.setBorder(new TitledBorder(null, "Ausleihen von " + cust.getSurname() + " " + cust.getName() , TitledBorder.LEADING, TitledBorder.TOP, null, null));
 			         }    
 			      }
 			   }   
 		});
 		
 		JPanel panel_6 = new JPanel();
 		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
 		gbc_panel_6.insets = new Insets(0, 0, 5, 0);
 		gbc_panel_6.fill = GridBagConstraints.HORIZONTAL;
 		gbc_panel_6.anchor = GridBagConstraints.NORTH;
 		gbc_panel_6.gridx = 1;
 		gbc_panel_6.gridy = 0;
 		contentPane.setLayout(new BorderLayout(0, 0));
 	
 		panel_6.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel_3 = new JPanel();
 		panel_6.add(panel_3, BorderLayout.NORTH);
 		panel_3.setLayout(new BorderLayout(0, 0));
 		
 		JPanel panel_1 = new JPanel();
 		panel_3.add(panel_1, BorderLayout.SOUTH);
 		panel_1.setBorder(new TitledBorder(null, "Neues Exemplar ausleihen", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GridBagLayout gbl_panel_1 = new GridBagLayout();
 		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0};
 		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
 		gbl_panel_1.columnWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
 		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 		panel_1.setLayout(gbl_panel_1);
 		
 		JLabel lblExemplarid = new JLabel("Exemplar-ID");
 		GridBagConstraints gbc_lblExemplarid = new GridBagConstraints();
 		gbc_lblExemplarid.insets = new Insets(0, 0, 5, 5);
 		gbc_lblExemplarid.anchor = GridBagConstraints.WEST;
 		gbc_lblExemplarid.gridx = 0;
 		gbc_lblExemplarid.gridy = 0;
 		panel_1.add(lblExemplarid, gbc_lblExemplarid);
 		
 		textField_1 = new JTextField();
 		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
 		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
 		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_1.gridx = 1;
 		gbc_textField_1.gridy = 0;
 		panel_1.add(textField_1, gbc_textField_1);
 		textField_1.setColumns(10);
 		
 		JButton btnAnzeigen = new JButton("Hinzufügen");
 		GridBagConstraints gbc_btnAnzeigen = new GridBagConstraints();
 		gbc_btnAnzeigen.insets = new Insets(0, 0, 5, 0);
 		gbc_btnAnzeigen.gridx = 2;
 		gbc_btnAnzeigen.gridy = 0;
 		panel_1.add(btnAnzeigen, gbc_btnAnzeigen);
 		
 		JLabel lblZurckAm = new JLabel("Zurück am");
 		GridBagConstraints gbc_lblZurckAm = new GridBagConstraints();
 		gbc_lblZurckAm.anchor = GridBagConstraints.WEST;
 		gbc_lblZurckAm.insets = new Insets(0, 0, 0, 5);
 		gbc_lblZurckAm.gridx = 0;
 		gbc_lblZurckAm.gridy = 1;
 		panel_1.add(lblZurckAm, gbc_lblZurckAm);
 		
 		textField_2 = new JTextField();
 		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
 		gbc_textField_2.insets = new Insets(0, 0, 0, 5);
 		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_2.gridx = 1;
 		gbc_textField_2.gridy = 1;
 		panel_1.add(textField_2, gbc_textField_2);
 		textField_2.setColumns(10);
 		
 		panel_2 = new JPanel();
 		panel_6.add(panel_2);
 		panel_2.setBorder(new TitledBorder(null,"Ausleihen von...", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel_2.setLayout(new BorderLayout(0, 0));
 		
 		JScrollPane scrollPane = new JScrollPane();
 		panel_2.add(scrollPane, BorderLayout.CENTER);
 		
 		table = new JTable();
 		table.setEnabled(false);
 		table.setFillsViewportHeight(true);
 		scrollPane.add(table);
 		scrollPane.setViewportView(table);
 		
 		JSplitPane splitPane = new JSplitPane();
 		contentPane.add(splitPane);	
 		
 		splitPane.setLeftComponent(panel);
 		splitPane.setRightComponent(panel_6);
 		
 		splitPane.setDividerLocation(350);
 	}
 
 
 	@Override
 	public void update(Observable o, Object arg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
