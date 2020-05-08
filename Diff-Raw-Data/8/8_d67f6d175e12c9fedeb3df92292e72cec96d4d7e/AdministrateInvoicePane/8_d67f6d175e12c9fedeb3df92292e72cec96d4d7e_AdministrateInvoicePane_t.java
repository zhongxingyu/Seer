 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.DefaultTableModel;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.apache.log4j.Logger;
 
 import services.InvoiceService;
 
 import entities.Invoice;
 
 public class AdministrateInvoicePane extends BasePane {
 
 	private static final long serialVersionUID = 3523355764154359405L;
 	private Logger logger = Logger.getLogger("gui.AdministrateInvoicePane.class");
 	private InvoiceService is;
 	private JButton search;
 	private JLabel inr;
 	private JLabel datefrom;
 	private JLabel datetill;
 	private JLabel waiter;
 	private JLabel dateFormat;
 	private JTextField inrField;
 	private JTextField datefromField;
 	private JTextField datetillField;
 	private JTextField waiterField;
 	private DefaultTableModel invoiceTableModel;
 	private JScrollPane resultTablePane;
 	private JTable results;
 	
 	public AdministrateInvoicePane(InvoiceService is){
 		super();
 		
 		this.is = is;
 		createButtons();
 		createLabels();
 		createTextFields();
 		initialiseTableModel();
 		createResultPane();
 		addEverythingToInterface();
 		
 	}
 
 	private void createButtons() {
 		search = new JButton("Suchen");
 		search.addActionListener(new searchListener());
 		
 	}
 
 	private void createLabels() {
 		inr = new JLabel("Rechnungsnummer: ");
 		datefrom = new JLabel("Rechnugsdatum von: ");
 		datetill = new JLabel("bis: ");
 		waiter = new JLabel("Kellner: ");
 		dateFormat = new JLabel("(JJJJ-MM-TT)");
 	}
 
 	private void createTextFields() {
 		inrField = new JTextField();
 		datefromField = new JTextField();
 		datetillField = new JTextField();
 		waiterField = new JTextField();
 	}	
 	private void initialiseTableModel(){
 		logger.info("initialising TableModel");
 		createUneditableTableModel();
 		setColumnNames();
 	}
 
 	@SuppressWarnings("serial")
 	private void createUneditableTableModel() {
 		invoiceTableModel = new DefaultTableModel(){
 			@Override
 			public boolean isCellEditable(int row, int column) {
 				return false;
 			}
 		};
 	}
 
 	private void setColumnNames() {
 		Object[] columnNames = new Object[5];
 		columnNames[0] = "Rechnungsnummer";
 		columnNames[1] = "Summe";
 		columnNames[2] = "Datum";
 		columnNames[3] = "Uhrzeit";
 		columnNames[4] = "Kellner";
 		
 		invoiceTableModel.setColumnIdentifiers(columnNames);
 	}
 
 	private void createResultPane() {
 		logger.info("Creating ResultPane");
 		results = new JTable(invoiceTableModel);
 		resultTablePane = new JScrollPane(results);
 		
 	}
 
 	private void addEverythingToInterface() {
 		JPanel wf = super.westField;
 		JPanel eb = super.eastButtons;
 		
 		reconfigureButtonPanelLayout();
 		
 		eb.add(search);
 		
 		wf.add(inr);
 		wf.add(inrField, "w 100, wrap");
 		wf.add(datefrom);
 		wf.add(datefromField,"w 100");
 		wf.add(dateFormat);
 		wf.add(datetill);
 		wf.add(datetillField,"w 100, wrap");
 		wf.add(waiter);
 		wf.add(waiterField,"w 300, span 3, wrap");
 		wf.add(resultTablePane,"span 4, grow");
 		
 	}
 
 	private void reconfigureButtonPanelLayout() {
 		super.eastButtons.setLayout(new MigLayout("","grow",":push[]"));
 	}
 	
 	public void updateResultsOfInvoiceSearch(List<Invoice> invoices){
 		logger.info("Updating Result Table");
 		resetTableModel();
 		if(invoices == null || invoices.isEmpty()){}
 		else{
 			fillTableWithNewEntries(invoices);
 		}
 		updateDisplayWithNewData();
 	}
 
 	private void resetTableModel() {
 		invoiceTableModel.getDataVector().removeAllElements();
 	}
 
 	private void fillTableWithNewEntries(List<Invoice> invoices) {
 		Object[] newRow = new Object[3];
 		Iterator<Invoice> invoiceIterator = invoices.iterator();
 		while(invoiceIterator.hasNext()){
 			Invoice i = invoiceIterator.next();
 			newRow[0] = i.getId(); 
 			newRow[1] = i.getSum();
 			//TODO check if to string is needed here
 			newRow[2] = i.getDate();
 			newRow[3] = i.getTime();
 			newRow[4] = i.getWaiter();
 			invoiceTableModel.addRow(newRow);
 		}
 	}
 	
 	private void updateDisplayWithNewData() {
 		results.revalidate();
 	}
 	
 	protected searchListener getSearchListener(){
 		return new searchListener();
 	}
 	
 	protected class searchListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String iid = inrField.getText();
 			String datefrom = datefromField.getText();
 			String datetill = datetillField.getText();
 			String waiter = waiterField.getText();
 			
 			if(iid.isEmpty() && datefrom.isEmpty() && datetill.isEmpty() && waiter.isEmpty()){
 				List<Invoice> result = is.getAllInvoices();
 				updateResultsOfInvoiceSearch(result);
 				return;
 			}
 			if(iid.isEmpty()){
 				if(datefrom.isEmpty() && datetill.isEmpty() && !waiter.isEmpty()){
 					searchByWaiter(waiter);
 					return;
 				}
 				if(!datefrom.isEmpty() && !datetill.isEmpty() && waiter.isEmpty()){
 					try{
 						checkDateFormat(datefrom);
 						checkDateFormat(datetill);
 					}
 					catch(Exception e1){
 						logger.warn("Got an illegal date format");
						JOptionPane.showMessageDialog(westField, "Bitte Überprüfen Sie Ihr Datumsformat");
						return;
 					}
 					searchByDates(datefrom, datetill);
 					return;
 				}
 			}
 			if(!iid.isEmpty()){
				if(!datefrom.isEmpty() || !datetill.isEmpty() || !waiter.isEmpty()){
					JOptionPane.showMessageDialog(westField, "Bitte füllen Sie genau eine Zeile der Suchmaske aus.");
					return;
				}
 				int id = 0;
 				try{
 					id = Integer.parseInt(iid);
 					if(id < 1){throw new Exception();}
 				}
 				catch(Exception e2){
 					JOptionPane.showMessageDialog(westField, "Bitte geben Sie eine Rechnungsnummer > 0 ein.");
 				}
 				List<Invoice> result = new LinkedList<Invoice>();
 				result.add(is.getInvoiceById(id));
 				updateResultsOfInvoiceSearch(result);
 				return;
 			}
 			JOptionPane.showMessageDialog(westField, "Bitte füllen Sie genau eine Zeile der Suchmaske aus.");
 		}
 
 		private void searchByDates(String datefrom, String datetill) {
 			List<Invoice> result = is.getInvoicesByDates(datefrom, datetill);
 			updateResultsOfInvoiceSearch(result);
 			return;
 		}
 
 		private void searchByWaiter(String waiter) {
 			List<Invoice> result = is.getInvoicesByWaiter(waiter);
 			updateResultsOfInvoiceSearch(result);
 			return;
 		}
 		
 		protected void checkDateFormat(String s) throws IllegalArgumentException{
 			String dateRegex = "(19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])";
 			if(s.matches(dateRegex)){
 				return;
 			}
 			else{
 				throw new IllegalArgumentException();
 			}
 		}
 		
 	}
 
 
 }
