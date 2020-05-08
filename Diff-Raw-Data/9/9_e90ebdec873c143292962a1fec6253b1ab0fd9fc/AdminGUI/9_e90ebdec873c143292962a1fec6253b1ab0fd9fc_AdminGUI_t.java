 package sbc.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.text.NumberFormat;
 
 import javax.swing.JButton;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableColumnModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.text.NumberFormatter;
 
 import org.apache.log4j.Logger;
 
 import sbc.gui.tablemodels.*;
 import sbc.model.Nest;
 
 /**
  * represents the GUI
  * 
  * @author ja
  *
  */
 public class AdminGUI extends Thread {
 	
 	private static final long serialVersionUID = -2488511541453249373L;
 
 	private static Logger log = Logger.getLogger(AdminGUI.class);
 
 	// global gui vars
 	private JFrame frame;
 	private Insets insets;
 	private Container pane;
 	
 	// overview table
 	private JTable table;
 	// detail table for one nest
 	private JTable detailTable;
 	// live info table
 	private JTable infoTable;
 	// table for error nests
 	private JTable errortable;
 	
 	// textfields for producer generation
 	private JFormattedTextField chocoElementsTextField;
 	private JFormattedTextField chocoProducersTextField;
 	private JFormattedTextField chickenElementsTextField;
 	private JFormattedTextField chickenProducersTextField;
 	private ProducerInterface producerCallback;
 
 	private DefaultTableModel infoTableModel;
 
 
 	/**
 	 * constructer,
 	 * 	expects a callback
 	 * @param prod
 	 */
 	public AdminGUI(ProducerInterface prod)	{
 		this.producerCallback = prod;
 	}
 
 	
 	/**
 	 * called when thread is started
 	 */
 	public void run()	{
 		frame = new JFrame();
 		pane = frame.getContentPane();
 		this.createGUI();
 	}
 	
 	/**
 	 * create the GUI
 	 */
 	private void createGUI()	{
 
 		// GLOBAL GUI SETTINGS
 		frame.setTitle("SBC JMS Osterhasenfabrik");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 
 		Toolkit tk = Toolkit.getDefaultToolkit();
 		Dimension screenSize = tk.getScreenSize();
 		int screenHeight = screenSize.height;
 		int screenWidth = screenSize.width;
 		frame.setLocation(screenWidth / 4, screenHeight / 4);
 
 		frame.setSize(800, 500);
 		insets = frame.getInsets();
 
 		frame.setSize(800 + insets.left + insets.right, 500 + insets.top + insets.bottom);
 		
 		// use absolut positioning
 		this.pane.setLayout(null);
 
 		// generate HEADER
 		JLabel header = new JLabel("EasterBunny Company");
 		header.setFont(new Font("Arial", Font.BOLD, 20));
 
 		pane.add(header);
 		header.setBounds(10 + insets.left, 10 + insets.top, 350, 25);
 
 		// create the overview table (all nests are listed)
 		this.createTable();
 		// create the table for the detail view
 		this.createDetailView();
 		// create live info
 		this.createInfo();
 		// create produce functionality
 		this.createProduce();
 		
 		this.createErrorTable();
 		
 		// show it
 		frame.setVisible(true);
 	}
 
 	/**
 	 * creates (and adds) the overview table (containing all NESTS)
 	 */
 	private void createTable()	{
 
 		// header
 		JLabel tableHeader = new JLabel("Nests:");
 		tableHeader.setFont(new Font("Arial", Font.BOLD, 15));
 
 		pane.add(tableHeader);
 		tableHeader.setBounds(10 + insets.left, 40 + insets.top, 350, 25);
 
 		// table model (adminTableModel)
 		AdminTableModel model = new AdminTableModel();
 		Object[] ci = {"Nest ID",
 						"Egg1 ID",
 						"Egg2 ID",
 						"Choco ID",
 						"status"};
 
 		model.setColumnIdentifiers(ci);
 		
 		// table
 		table = new JTable();
 		// create resized table
 		table = autoResizeColWidth(table, model);
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		table.getSelectionModel().addListSelectionListener(new RowListener());
 		
 		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
 		table.setFillsViewportHeight(true);
 
 		//Create the scroll pane and add the table to it.
 		JScrollPane scrollPane = new JScrollPane(table);
 
 		pane.add(scrollPane);
 		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 		scrollPane.setBounds(10 + insets.left, 65 + insets.top, 400, 150);
 	}
 
 	/**
 	 * creates the table for the detail view (specific information about a nest)
 	 */
 	private void createDetailView() {
 		// header
 		JLabel detailHeader = new JLabel("Detailansicht:");
 		detailHeader.setFont(new Font("Arial", Font.BOLD, 15));
 
 		pane.add(detailHeader);
 		detailHeader.setBounds(430 + insets.left, 40 + insets.top, 100, 20);
 		
 		
 		// default model (adopted)
 		DefaultTableModel model = new DefaultTableModel();
 		
 		model.addColumn("Titel");
 		model.addColumn("Value");
 		
 		// add columns
 		model.addRow(new Object[]{"Nest"});
 		model.addRow(new Object[]{"  ID"});
 		model.addRow(new Object[]{"	 defect", ""});
 		model.addRow(new Object[]{"Egg1", ""});
 		model.addRow(new Object[]{"	 ID", ""});
 		model.addRow(new Object[]{"	 Chicken", ""});
 		model.addRow(new Object[]{"	 Colors", ""});
 		model.addRow(new Object[]{"	 Color", ""});
 		model.addRow(new Object[]{"	 ColorRabbit", ""});
 		model.addRow(new Object[]{"	 defect", ""});
 		model.addRow(new Object[]{"Egg2", ""});
 		model.addRow(new Object[]{"	 ID", ""});
 		model.addRow(new Object[]{"	 Chicken", ""});
 		model.addRow(new Object[]{"	 Colors", ""});
 		model.addRow(new Object[]{"	 Color", ""});
 		model.addRow(new Object[]{"	 ColorRabbit", ""});
 		model.addRow(new Object[]{"	 defect", ""});
 		model.addRow(new Object[]{"ChocoRabbit", ""});
 		model.addRow(new Object[]{"	 ID", ""});
 		model.addRow(new Object[]{"	 Producer", ""});
 		model.addRow(new Object[]{"	 defect", ""});
 		
 		// create table
 		detailTable = new JTable();
 		detailTable = autoResizeColWidth(detailTable, model);
 		detailTable.setShowGrid(true);
 		detailTable.setGridColor(Color.BLACK);
 		
 		detailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		
 		// hide header
 		detailTable.setTableHeader(null);
 		
 		JScrollPane scrollPane = new JScrollPane(detailTable);
 
 		pane.add(scrollPane);
 //		detailTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
 		detailTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		detailTable.getColumnModel().getColumn(0).setPreferredWidth(110);
 		detailTable.getColumnModel().getColumn(1).setPreferredWidth(170);
 		scrollPane.setBounds(430 + insets.left, 65 + insets.top, 300, 150);
 	}
 	
 	/**
 	 * creates the live info table
 	 */
 	private void createInfo() {
 		// header
 		JLabel infoHeader = new JLabel("Systeminformationen:");
 		infoHeader.setFont(new Font("Arial", Font.BOLD, 15));
 
 		pane.add(infoHeader);
 		infoHeader.setBounds(10 + insets.left, 235 + insets.top, 170, 20);
 		
 		// default table model
 		DefaultTableModel model = new DefaultTableModel();
 		
 		model.addColumn("Titel");
 		model.addColumn("Value");
 		
 		model.addRow(new Object[]{"Eggs:", 0});
 		model.addRow(new Object[]{"Eggs Colored:", 0});
 		model.addRow(new Object[]{"Choco Rabbits:", 0});
 		model.addRow(new Object[]{"Nests:", 0});
 		model.addRow(new Object[]{"Completed Nests:", 0});
 		model.addRow(new Object[]{"Error Nests:", 0});
 		
 		infoTable = new JTable();
 		infoTable = autoResizeColWidth(infoTable, model);
 		infoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		// hide headers
 		infoTable.setTableHeader(null);
 		
 		JScrollPane scrollPane = new JScrollPane(infoTable);
 		pane.add(scrollPane);
 
 		infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 		scrollPane.setBounds(10 + insets.left, 260 + insets.top, 170, 110);
 		
 		infoTableModel = model;
 		
 	}
 	
 	
 	/**
 	 * creates (and adds) the overview table (containing all NESTS)
 	 */
 	private void createErrorTable()	{
 
 		// header
 		JLabel tableHeader = new JLabel("Defect Nests:");
 		tableHeader.setFont(new Font("Arial", Font.BOLD, 15));
 
 		pane.add(tableHeader);
 		tableHeader.setBounds(230 + insets.left, 235 + insets.top, 160, 20);
 
 		// table model (adminTableModel)
 		ErrorTableModel model = new ErrorTableModel();
 		Object[] ci = {"Nest ID",
 						"status"};
 
 		model.setColumnIdentifiers(ci);
 		
 		// table
 		errortable = new JTable();
 		// create resized table
 		errortable = autoResizeColWidth(errortable, model);
 		errortable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		errortable.getSelectionModel().addListSelectionListener(new ErrorRowListener());
 		
 		errortable.setPreferredScrollableViewportSize(new Dimension(500, 70));
 		errortable.setFillsViewportHeight(true);
 
 		//Create the scroll pane and add the table to it.
 		JScrollPane scrollPane = new JScrollPane(errortable);
 
 		pane.add(scrollPane);
 		errortable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 		scrollPane.setBounds(230 + insets.left, 260 + insets.top, 180, 110);
 	}
 	
 	/**
 	 * create the producer section (chickens and choco rabbits can be produced)
 	 */
 	private void createProduce() {
 		// header
 		JLabel producerHeader = new JLabel("Produce:");
 		producerHeader.setFont(new Font("Arial", Font.BOLD, 15));
 		
 		pane.add(producerHeader);
 		producerHeader.setBounds(430 + insets.left, 235 + insets.top, 150, 20);
 		
 		JLabel headerProducers = new JLabel("producers");
 		headerProducers.setFont(new Font("Arial", Font.ITALIC, 10));
 		
 		pane.add(headerProducers);
 		headerProducers.setBounds(580 + insets.left, 250 + insets.top, 50, 20);
 		
 		JLabel headerElements = new JLabel("elements per producer");
 		headerElements.setFont(new Font("Arial", Font.ITALIC, 10));
 		
 		pane.add(headerElements);
 		headerElements.setBounds(640 + insets.left, 250 + insets.top, 110, 20);
 		
 		
 		// LEFT COLUMN
 		JLabel produceChickenHeader = new JLabel("Eggs:");
 		produceChickenHeader.setFont(new Font("Arial", Font.BOLD, 12));
 
 		pane.add(produceChickenHeader);
 		produceChickenHeader.setBounds(430 + insets.left, 275 + insets.top, 150, 20);
 
 		JLabel produceChocoHeader = new JLabel("Choco Bunnies:");
 		produceChocoHeader.setFont(new Font("Arial", Font.BOLD, 12));
 
 		pane.add(produceChocoHeader);
 		produceChocoHeader.setBounds(430 + insets.left, 300 + insets.top, 150, 20);
 		
 		
 		// set number format, so that input can just be numeric
 		NumberFormat format = NumberFormat.getInstance();
 		format.setMaximumFractionDigits(0);
 		format.setMaximumIntegerDigits(2);
 		
 		
 		// chicken textfields
 		chickenProducersTextField = new JFormattedTextField(format);
         ((NumberFormatter)chickenProducersTextField.getFormatter()).setAllowsInvalid(false);
         
         chickenProducersTextField.setSize(50, 20);
         chickenProducersTextField.setText("0");
         
         pane.add(chickenProducersTextField);
         chickenProducersTextField.setBounds(580 + insets.left, 275 + insets.top, 50, 20);
                 
         chickenElementsTextField = new JFormattedTextField(format);
         ((NumberFormatter)chickenElementsTextField.getFormatter()).setAllowsInvalid(false);
         
         chickenElementsTextField.setSize(50, 20);
         chickenElementsTextField.setText("0");
         
         pane.add(chickenElementsTextField);
         chickenElementsTextField.setBounds(700 + insets.left, 275 + insets.top, 50, 20);
         
         
         // choco rabbit textfields
         chocoProducersTextField = new JFormattedTextField(format);
         ((NumberFormatter)chocoProducersTextField.getFormatter()).setAllowsInvalid(false);
         
         chocoProducersTextField.setSize(50, 20);
         chocoProducersTextField.setText("0");
         
         pane.add(chocoProducersTextField);
         chocoProducersTextField.setBounds(580 + insets.left, 300 + insets.top, 50, 20);
         
         chocoElementsTextField = new JFormattedTextField(format);
         ((NumberFormatter)chocoElementsTextField.getFormatter()).setAllowsInvalid(false);
         
         chocoElementsTextField.setSize(50, 20);
         chocoElementsTextField.setText("0");
         
         pane.add(chocoElementsTextField);
         chocoElementsTextField.setBounds(700 + insets.left, 300 + insets.top, 50, 20);
         
         
         // create button
         JButton produce = new JButton("create");
         produce.setSize(70, 20);
         
         pane.add(produce);
         produce.setBounds(680 + insets.left, 345 + insets.top, 70, 20);
         
         // action listener
         produce.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// call callback to produce producers
 				try	{
 					producerCallback.createProducers(
 							Integer.parseInt(chickenProducersTextField.getText()),
 							Integer.parseInt(chickenElementsTextField.getText()), 
 							Integer.parseInt(chocoProducersTextField.getText()),
 							Integer.parseInt(chocoElementsTextField.getText()));
 					
 					chickenElementsTextField.setText("0");
 					chickenProducersTextField.setText("0");
 					chocoElementsTextField.setText("0");
 					chocoProducersTextField.setText("0");
 					
 				} catch (Exception e1)	{
 					log.error("producer could not be called");
 					e1.printStackTrace();
 				}
 			}
 		});
         
 	}
 	
 	
 	/**
 	 * updates the live info
 	 * 
 	 * @deprecated should not be used in assignment2! instead use direct update and add methods
 	 * @param eggCount
 	 * @param eggColoredCount
 	 * @param chocoCount
 	 * @param nestCount
 	 * @param nestCompletedCount
 	 */
 	public void updateInfoData(int eggCount, int eggColoredCount, int chocoCount, int nestCount, int nestCompletedCount, int nestErrorCount)	{
 		
 		int tmp;
 		// egg count
 		tmp = (Integer)infoTableModel.getValueAt(0, 1) + eggCount;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 0, 1);
 		// egg color count
 		tmp = (Integer)infoTableModel.getValueAt(1, 1) + eggColoredCount;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 1, 1);
 		// choco count
 		tmp = (Integer)infoTableModel.getValueAt(2, 1) + chocoCount;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 2, 1);
 		// nest count
 		tmp = (Integer)infoTableModel.getValueAt(3, 1) + nestCount;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 3, 1);
 		// nest completed count
 		tmp = (Integer)infoTableModel.getValueAt(4, 1) + nestCompletedCount;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 4, 1);
 		// nest error count
 		tmp = (Integer)infoTableModel.getValueAt(5, 1) + nestErrorCount;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 5, 1);
 	}
 	
 	
 	public void updateEgg(int count)	{
 		// egg count
 		int tmp = (Integer)infoTableModel.getValueAt(0, 1) + count;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 0, 1);
 	}
 
 	public void updateColoredEgg(int count)	{
 		// egg color count
 		int tmp = (Integer)infoTableModel.getValueAt(1, 1) + count;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 1, 1);
 	}
 	
 	public void updateChoco(int count)	{
 		// choco count
 		int tmp = (Integer)infoTableModel.getValueAt(2, 1) + count;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 2, 1);
 	}
 	
 	private void updateNestCount(int count)	{
 		// nest count
 		int tmp = (Integer)infoTableModel.getValueAt(3, 1) + count;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 3, 1);
 	}
 	
 	private void updateCompletedNestCount(int count)	{
 		// nest completed count
 		int tmp = (Integer)infoTableModel.getValueAt(4, 1) + count;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 4, 1);
 	}
 	
 	private void updateErrorNestCount(int count)	{
 		// nest error count
 		int tmp = (Integer)infoTableModel.getValueAt(5, 1) + count;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 5, 1);
 	}
 	
 	/**
 	 * adds colored eggs (amount of count) and adopts the live statistics (removes it from eggs)
 	 * @param count
 	 */
 	public void addColoredEgg(int count)	{
 		this.updateEgg(-count);
 		this.updateColoredEgg(count);
 	}
 	
 	/**
 	 * adds a nest and adopts the live statistics
 	 * @param nest
 	 */
 	public void addNest(Nest nest)	{
 		// add 1 to nest count
 		int tmp = (Integer)infoTableModel.getValueAt(3, 1) + 1;
 		infoTableModel.setValueAt((tmp < 0 ? 0 : tmp), 3, 1);
 		// remove 2 eggs
 		this.updateColoredEgg(-2);
 		this.updateChoco(-1);
 		this.updateNest(nest);
 	}
 	
 	/**
 	 * adds a nest with an error and adopts the live statistics
 	 * @param nest
 	 */
 	public void addErrorNest(Nest nest)	{
 		if(!nest.hasError())	{
 			log.error("GIVEN NEST HAS NO ERROR");
 			return;
 		}
 		// remove 1 nest
 		this.updateNestCount(-1);
 		this.updateErrorNestCount(1);
 		this.updateNest(nest);
 		this.updateErrorNest(nest);
 	}
 	
 	/**
 	 * adds a completed nest and adopts the live statistics
 	 * @param nest
 	 */
 	public void addCompletedNest(Nest nest)	{
 		// remove 1 nest
 		this.updateNestCount(-1);
 		this.updateCompletedNestCount(1);
 		this.updateNest(nest);
 	}
 	
 	/**
 	 * updates a nest or sets a new nest
 	 * @param object
 	 */
 	public void updateNest(Nest object) {
 		if(object.hasError())	{
 			((AdminTableModel)table.getModel()).removeRow(object);
 		} else	{
 			((AdminTableModel)table.getModel()).addRow(object);
 		}
 	}
 	
 	/**
 	 * updates a errornest or sets a new errornest
 	 * @param object
 	 */
 	public void updateErrorNest(Nest object) {
 		((ErrorTableModel)errortable.getModel()).addRow(object);
 	}
 	
 	/**
 	 * load detail of a nest in the overview
 	 * @param nest
 	 */
 	private void addDataToDetailView(Nest nest) {
 		DefaultTableModel model = ((DefaultTableModel)detailTable.getModel());
 		
 		// delete all rows
 //		model.setRowCount(0);
 		
 		if(nest == null)
 			return;
 		
 		int pos = 1;
 		
 		if(nest.hasId())	{
 			model.setValueAt(nest.getId(), pos++, 1);
 		}
 		model.setValueAt(nest.hasError(), pos++, 1);
 		
 		pos++;
 		if(nest.getEgg1() != null)	{
 			model.setValueAt(nest.getEgg1().getId(), pos++, 1);
 			model.setValueAt(nest.getEgg1().getProducer_id(),pos++,1);
 			model.setValueAt(nest.getEgg1().getColorCount(),pos++,1);
 			model.setValueAt(nest.getEgg1().getColor(),pos++,1);
 			model.setValueAt(nest.getEgg1().getColorer_id(),pos++,1);
 			model.setValueAt(nest.getEgg1().isError(),pos++,1);
 		}
 		
 		pos++;
 		if(nest.getEgg1() != null)	{
 			model.setValueAt(nest.getEgg2().getId(),pos++,1);
 			model.setValueAt(nest.getEgg2().getProducer_id(),pos++,1);
 			model.setValueAt(nest.getEgg2().getColorCount(),pos++,1);
 			model.setValueAt(nest.getEgg2().getColor(),pos++,1);
 			model.setValueAt(nest.getEgg2().getColorer_id(),pos++,1);
 			model.setValueAt(nest.getEgg2().isError(),pos++,1);
 		}
 		
 		pos++;
 		if(nest.getRabbit() != null)	{
 			model.setValueAt(nest.getRabbit().getId(),pos++,1);
 			model.setValueAt(nest.getRabbit().getProducer_id(),pos++,1);
 			model.setValueAt(nest.getRabbit().isError(),pos++,1);
 		}
 	}
 	
 	private class RowListener implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent event) {
 			if (event.getValueIsAdjusting()) {
 				return;
 			}
 			addDataToDetailView(((DefaultNestTableModel) table.getModel()).getNestForRow(event.getFirstIndex()));
 			table.removeRowSelectionInterval(0, table.getRowCount() -1);
 		}
 	}
 	
 	private class ErrorRowListener implements ListSelectionListener {
 		public void valueChanged(ListSelectionEvent event) {
 			if (event.getValueIsAdjusting()) {
 				return;
 			}
 			
 			addDataToDetailView(((DefaultNestTableModel) errortable.getModel()).getNestForRow(event.getFirstIndex()));
 			errortable.removeRowSelectionInterval(0, errortable.getRowCount() -1);
 		}
 	}
 
 	/**
 	 * resize columns of table
 	 * @param table
 	 * @param model
 	 * @return
 	 */
 	public JTable autoResizeColWidth(JTable table, DefaultTableModel model) {
 		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		table.setModel(model);
 
 		int margin = 5;
 
 		for (int i = 0; i < table.getColumnCount(); i++) {
 			int                     vColIndex = i;
 			DefaultTableColumnModel colModel  = (DefaultTableColumnModel) table.getColumnModel();
 			TableColumn             col       = colModel.getColumn(vColIndex);
 			int                     width     = 0;
 
 			// Get width of column header
 			TableCellRenderer renderer = col.getHeaderRenderer();
 
 			if (renderer == null) {
 				renderer = table.getTableHeader().getDefaultRenderer();
 			}
 
 			Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
 
 			width = comp.getPreferredSize().width;
 
 			// Get maximum width of column data
 			for (int r = 0; r < table.getRowCount(); r++) {
 				renderer = table.getCellRenderer(r, vColIndex);
 				comp     = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false,
 						r, vColIndex);
 				width = Math.max(width, comp.getPreferredSize().width);
 			}
 
 			// Add margin
 			width += 2 * margin;
 
 			// Set the width
 			col.setPreferredWidth(width);
 		}
 
 		((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(
 				SwingConstants.LEFT);
 
 		// table.setAutoCreateRowSorter(true);
 		table.getTableHeader().setReorderingAllowed(false);
 		return table;
 	}
 	
 }
