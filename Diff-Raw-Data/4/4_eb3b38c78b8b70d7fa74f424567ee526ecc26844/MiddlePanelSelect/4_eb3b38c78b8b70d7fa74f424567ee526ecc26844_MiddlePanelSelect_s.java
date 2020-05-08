 package ar.proyecto.gui;
 
 import java.awt.CardLayout;
 import java.awt.FlowLayout;
 import java.text.NumberFormat;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import ar.proyecto.controller.ActionMiddlePanelSelectCbox;
 import ar.proyecto.controller.ActionMiddlePanelSelectOk;
 
 public class MiddlePanelSelect extends MiddlePanel {
 	private JLabel from;
 	private JComboBox cbTable;
 	private JLabel whereNum;
 	private JFormattedTextField nroMulta;
 	private JPanel centralPanel;
 	private JPanel centralPanelEmpty;
 	private JPanel centralPanelFilled;
 	private final static String sPaneEmpty = new String("Empty Pannel");
 	private final static String sPaneFilled = new String("Filled Pannel");
 	private int widthpc = 400;
 		
 	private JButton ok;
 	
 	//constructor
 	public MiddlePanelSelect(MainWindow gui) {
 		super(gui);
 		this.setLayout(new FlowLayout(FlowLayout.LEFT));
 		
 		from = new JLabel("FROM TABLE :");
 		
 		centralPanel = new JPanel();
 		centralPanel.setLayout(new CardLayout());
 		
 		whereNum = new JLabel("WHERE nro_multa =");
		nroMulta = new JFormattedTextField(NumberFormat.getIntegerInstance());
 		nroMulta.setColumns(5);
 		initCentralPanelEmpty();
 		initCentralPanelFilled();
 		
 		//Adding both centralPanel one on the top of the other
 		centralPanel.add(centralPanelEmpty,sPaneEmpty);
 		centralPanel.add(centralPanelFilled,sPaneFilled);
 		
 		String[] cb = {"Infraccion","Multa"};
 		cbTable = new JComboBox(cb);
 		cbTable.setAction(new ActionMiddlePanelSelectCbox(this,centralPanel));
 		
 		ok = new JButton(new ActionMiddlePanelSelectOk(gui,nroMulta,cbTable,"OK"));
 		
 		this.add(from);
 		this.add(cbTable);
 		this.add(centralPanel);
 		this.add(ok);
 	}
 
 	private void initCentralPanelEmpty() {
 		centralPanelEmpty = new JPanel();
 		centralPanelEmpty.setBackground(this.getBackground());
 	}
 	private void initCentralPanelFilled() {
 		centralPanelFilled = new JPanel();
 		centralPanelFilled.setBackground(this.getBackground());
 		centralPanelFilled.add(whereNum);
 		centralPanelFilled.add(nroMulta);
 	}
 
 	public String getSPaneEmpty() {
 		return sPaneEmpty;
 	}
 
 	public String getSPaneFilled() {
 		return sPaneFilled;
 	}	
 }
