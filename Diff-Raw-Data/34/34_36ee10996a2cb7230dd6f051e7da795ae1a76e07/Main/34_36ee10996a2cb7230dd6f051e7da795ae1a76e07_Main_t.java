 package asrs;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 
 import tspAlgorithm.TSPAlgorithm;
 import bppAlgorithm.BPPAlgorithm;
 
 import listener.XMLUploadedListener;
 import listener.ExecuteButtonPressedListener;;
 
 
 /**
  * 
  */
 
 /**
  * @author Luuk
  *
  */
 public class Main extends JFrame implements XMLUploadedListener, ExecuteButtonPressedListener {
 	/**
 	 * Panels voor elke fieldset
 	 */
 	private XMLPanel xmlPanel = new XMLPanel();
 	private ExecutionPanel executionPanel = new ExecutionPanel();
 	private CustomerPanel customerPanel = new CustomerPanel();
 	private OrderPanel orderPanel = new OrderPanel();
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// zet de look and feel naar windows of osx
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e)
 		{
 			System.out.println("Unable to load look and feel");
 		}
 		
 		// creeer het scherm
 		JFrame main = new Main();
 		// TODO Auto-generated method stub
 	}
 	
 	public Main()
 	{
 		setSize(1200, 700);
 		
 		// sluit het proces als je op kruisje drukt
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		/**
 		 * Het scherm is op te delen in 2 kolommen
 		 * De flowlayout zorgt voor de kolommen en de panels 
 		 * zorgen dat we meerdere componenten in 1 kant kunnen stoppen
 		 */
 		setLayout(new BorderLayout());
 		
 		buildUI();
 		bindListeners();
 		
 		// als laatste, maak hem zichtbaar
 		setVisible(true);
 	}
 	
 	private void buildUI()
 	{
 		/**
 		 * Linker en rechterkant van het scherm
 		 */
 		JPanel leftPanel = new JPanel();
 		JPanel rightPanel = new JPanel();
 		
 		/** 
 		 * zet de leftpanel op een breedte
 		 * de rechter heeft dit niet nodig omdat daar maar 1 panel in zit
 		 */
 		leftPanel.setPreferredSize(new Dimension(500, 700));
 
 		leftPanel.add(xmlPanel);
 		leftPanel.add(executionPanel);
 		leftPanel.add(customerPanel);
 		rightPanel.add(orderPanel);
 	
 		// plaats de panels
 		add(leftPanel, BorderLayout.WEST);
 		add(rightPanel, BorderLayout.CENTER);
 	}
 	
 	private void bindListeners()
 	{
 		// voeg listeners toe
 		xmlPanel.addXMLUploadListener(this);
 		executionPanel.addExecutionListener(this);
 	}
 
 	@Override
 	public void xmlUploaded(String xmlFileLocation) {
//		Order order = XMLLoader.readOrder(xmlFileLocation);
//		
//		customerPanel.setCustomerId(order.getCustomer().getId());
//		customerPanel.setCustomerName(order.getCustomer().getName());
//		customerPanel.setDate(order.getDate());
//		customerPanel.setTotalPrice(order.getTotalPrice());
 	}
 
 	@Override
 	public void simulatePressed(BPPAlgorithm bpp, TSPAlgorithm tsp) {
 		//JOptionPane.showMessageDialog(this, "Simulatie wordt gestart met bpp " + bpp.getName() + " en tsp " + tsp.getName());
 		
 		System.out.println("Open simulation");
 		SimulatorFrame frame = new SimulatorFrame(bpp, tsp);
 		frame.setVisible(true);
 	}
 
 	@Override
 	public void executePressed(BPPAlgorithm bpp, TSPAlgorithm tsp,
 			String com1, String com2) {
 		throw new UnsupportedOperationException();
 		// TODO Auto-generated method stub
 		
 	}
 
 }
