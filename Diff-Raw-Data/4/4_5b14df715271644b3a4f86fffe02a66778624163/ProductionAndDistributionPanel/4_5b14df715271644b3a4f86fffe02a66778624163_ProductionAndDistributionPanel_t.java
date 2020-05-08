 package Client.Presentation;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 
 import Client.Application.CompanyController;
 import Client.Entities.Company;
 import Client.Entities.EmployeeType;
 import Client.Entities.Machine;
 import Client.Entities.MachineType;
 import Client.Entities.PeriodInfo;
 import Client.Entities.Ressource;
 import Client.Entities.RessourceType;
 
 /**
  * Die bersicht der geplanten Produktion und des geplanten Absatzes
  * @author Scrato
  *
  */
 public class ProductionAndDistributionPanel extends JPanel {
 	
 	
 	/**
 	 * Aktualisiert die fehlenden Maschinen
 	 * aufgrund der neugewhlten Kapazitt
 	 * @author Scrato
 	 *
 	 */
 	private class CapacityItemListener implements ItemListener {
 
 		@Override
 		public void itemStateChanged(ItemEvent arg0) {
 			actualCapacity = Integer.parseInt((String) cb_capacities.getSelectedItem());
 			capid = cb_capacities.getSelectedIndex();
 			refreshCount();
 		}
 
 	}
 
 
 	/**
 	 * Aktualisiert die Daten nach dem Verndern der Produktions/Preisdaten
 	 * @author Scrato
 	 *
 	 */
 	private class TextListener implements KeyListener {
 
 		@Override
 		public void keyPressed(KeyEvent arg0) {			
 		
 
 		}
 
 		@Override
 		public void keyReleased(KeyEvent arg0) {
 			try
 			{
 					refreshCount();
 			}
 			catch(NumberFormatException ex1)
 			{
 				JTextPane c = (JTextPane) arg0.getComponent();
 				c.setText(String.valueOf(0));
 				return;
 			}
 			catch(ClassCastException ex2)
 			{
 				return;
 			}
 
 		}
 
 		@Override
 		public void keyTyped(KeyEvent arg0) {
 
 		}
 
 	}
 
 
 	
 	private static int capid = 0;
 	private TextListener tl = new TextListener();
 	private JTextPane amountproduce;
 	private JTextPane pricesell;
 	private JTextPane amountsell;
 	
 	private JLabel l_finishedProducts;
 	private JLabel l_lastPeriodSellingPrice;
 	private JLabel l_maxProducableUnits;
 	
 	private int actualCapacity = 1;
 	private int unitsToProduce;
 	private double priceToSell;
 	private int amountToSell;
 	
 	private final double PRICEMIN = 5.00;
 	private final double PRICEMAX = 20.00;
 	
 	private Company comp = Company.getInstance();
 	
 	
 	
 	private int maxProducableUnits;
 	private int maxSellableUnits;
 
 	private JComboBox cb_capacities;
 
 	private JLabel l_priceRating;
 
 	private JLabel l_missingRessource1;
 
 	private JLabel l_missingEmployee0;
 
 	private JLabel l_missingRessource0;
 
 	private JLabel l_missingEmployee1;
 
 	private JLabel l_missingMachine0;
 
 	private JLabel l_missingMachine1;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 2375380045275272220L;
 
 	public ProductionAndDistributionPanel(){
 		/*public Insets(int top,
       int left,
       int bottom,
       int right)*/
 		GridBagConstraints c = new GridBagConstraints();
 
 		c.gridx = 0;
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.WEST;
 		this.setLayout(new GridBagLayout());
 		
 		createInfoPart(c);
 		
 		createDistributionPart(c);
 		
 		createProductionPart(c);
 		
 		//Startwerte gleich dem vorher gespeicherten
 		amountsell.setText(String.valueOf(comp.getProdAndDistr().getUnitsToSell()));
 		amountproduce.setText(String.valueOf(comp.getProdAndDistr().getUnitsToProduce()));
 		pricesell.setText(String.valueOf(comp.getProdAndDistr().getSellingPrice()));
 		refreshCount();
 		cb_capacities.setSelectedIndex(capid);
 		actualCapacity = Integer.parseInt((String) cb_capacities.getSelectedItem());
 	}
 
 
 	/**
 	 * @param c
 	 * @param hjl
 	 * @param newLine
 	 */
 	private void createProductionPart(GridBagConstraints c) {
 		//ProductionScreen
 		Insets standard = new Insets(0,0,0,0);
 		Insets newLine = new Insets(15,0,5,0);
 		
 		c.ipady = 30;
 		JLabel title2 = new JLabel("Produktionsplanung");
 		title2.setFont(new Font("Serif", Font.BOLD, 14));
 		this.add(title2,c);
 		c.ipady = 1;
 		c.gridy++;
 		
 		this.add(new JLabel("Geplante Produktionsmenge: "),c);
 		c.gridx++;		
 		amountproduce = new JTextPane();
 		amountproduce.setSize(100, 500);
 		amountproduce.addKeyListener(tl);
 		//amountproduce.setText(String.valueOf(comp.getProdAndDistr().getUnitsToProduce()));
 		this.add(amountproduce, c);  
 		c.gridx = 0;
 		c.gridy++;
 		
 		
 
 		maxProducableUnits = comp.getProdAndDistr().getMaxProducableUnits();
 		maxSellableUnits = comp.getFinishedProducts();
 		
 		c.anchor = GridBagConstraints.NORTHWEST;
 		//Neue Zeile
 		c.insets = newLine;
 		this.add(new JLabel("Fehlende Ressourcen : "),c);
 		c.gridy++;
 		c.insets = standard;
 
 		RessourceType t0 = RessourceType.values()[0];
 		this.add(new JLabel(t0.name() + ": "),c);
 		c.gridx++;
 		l_missingRessource0 =new JLabel(getMissingRessource(t0));
 		this.add(l_missingRessource0,c);
 		c.gridx=0;
 		c.gridy ++;
 		
 		RessourceType t1 = RessourceType.values()[1];
 		this.add(new JLabel(t1.name() + ": "),c);
 		c.gridx++;
 		l_missingRessource1 =new JLabel(getMissingRessource(t1));
 		this.add(l_missingRessource1,c);
 		c.gridx=0;
 		c.gridy ++;
 		
 
 		
 		//Neue Zeile
 		c.insets = newLine;
 		this.add(new JLabel("Fehlende Mitarbeiter : "),c);
 		c.gridy++;
 		c.insets = standard;
 		EmployeeType e0 = EmployeeType.values()[0];
 		JLabel le0 = new JLabel(e0.name() + ": ");
 		this.add(le0,c);
 		c.gridx++;
 		l_missingEmployee0 =new JLabel(getMissingEmployee(e0));
 		this.add(l_missingEmployee0,c);
 		c.gridx=0;
 		c.gridy ++;
 		
 		EmployeeType e1 = EmployeeType.values()[1];
 		this.add(new JLabel(e1.name() + ": "),c);
 		c.gridx++;
 		l_missingEmployee1 =new JLabel(getMissingEmployee(e1));
 		this.add(l_missingEmployee1,c);
 		c.gridx=0;
 		c.gridy ++;
 
 		
 		//Neue Zeile
 		c.insets = newLine;
 		this.add(new JLabel("Fehlende Maschinen : "),c);
 		c.gridy++;
 		c.insets = standard;
 		
 		MachineType m0 = MachineType.values()[0];
 		this.add(new JLabel(m0.name() + ": "),c);
 		c.gridx++;
 		l_missingMachine0 =new JLabel(getMissingMachine(m0));
 		this.add(l_missingMachine0,c);
 		c.gridx=0;
 		c.gridy ++;
 		
 		MachineType m1 = MachineType.values()[1];
 		this.add(new JLabel(m1.name() + ": "),c);
 		c.gridx++;
 		l_missingMachine1 =new JLabel(getMissingMachine(m1));
 		this.add(l_missingMachine1,c);
 		c.gridx=0;
 		c.gridy ++;
 		
 		this.add(new JLabel("Maschinenkapazitt: "),c);
 		c.gridx++;
 		cb_capacities = new JComboBox();
 		for(String capa: Machine.capacites){
 			cb_capacities.addItem(capa);
 		}
 		cb_capacities.addItemListener(new CapacityItemListener());
 		this.add(cb_capacities,c);
 		cb_capacities.setEditable(false);
 	}
 
 
 	/**
 	 * @param c
 	 */
 	private void createDistributionPart(GridBagConstraints c) {
 		//SellingScreen
 		c.ipady = 30;
 		JLabel title1 = new JLabel("Absatzplanung");
 		title1.setFont(new Font("Serif", Font.BOLD, 14));
 		this.add(title1,c);
 		c.ipady = 1;
 		c.gridy++;
 		
 		//Verkaufsmenge
 		this.add(new JLabel("Geplante Verkaufsmenge: "),c);
 		
 		c.gridx++;
 		c.fill = GridBagConstraints.BOTH;
 		amountsell = new JTextPane();
 		amountsell.addKeyListener(tl);
 		this.add(amountsell, c);  
 		c.gridy++;
 		c.gridx = 0;
 		
 		//Verkaufspreis
 		this.add(new JLabel("Geplanter Verkaufspreis: "),c);
 		c.gridx++;
 		pricesell = new JTextPane();
 		pricesell.addKeyListener(tl);
 		this.add(pricesell, c);
 		
 		c.gridx++;
 		l_priceRating = new JLabel();
 		this.add(l_priceRating, c);
 		
 		c.gridx = 0;		
 		c.gridy++;
 	}
 
 
 	/**
 	 * @param c
 	 */
 	private void createInfoPart(GridBagConstraints c) {
 		c.ipady = 30;
 		JLabel title3 = new JLabel("Informationen");
 		title3.setFont(new Font("Serif", Font.BOLD, 14));
 		this.add(title3,c);
 		c.ipady = 1;
 		c.gridy++;
 		//Neue Zeile
 		this.add(new JLabel("Fertigprodukte auf Lager: "),c);
 		c.gridx++;
 		l_finishedProducts = new JLabel(String.valueOf(comp.getFinishedProducts()) + " Einheiten");
 		this.add(l_finishedProducts ,c);
 		c.gridx=0;
 		c.gridy++;
 
 		//Neue Zeile
 		this.add(new JLabel("Verkaufspreis der letzen Periode: "),c);
 		c.gridx++;
 		if(PeriodInfo.getNumberOfActPeriod() == 0)
 			l_lastPeriodSellingPrice = new JLabel("Bisher keine Produkte verkauft");
 		else
 			l_lastPeriodSellingPrice = new JLabel(String.valueOf(PeriodInfo.getActualPeriod().getProductPrice()) + "");
 		
 		this.add(l_lastPeriodSellingPrice , c);
 		c.gridx=0;
 		c.gridy++;
 
 		//Neue Zeile
 		this.add(new JLabel("Aktuell produzierbare Fertigprodukte : "),c);
 		c.gridx++;
 		l_maxProducableUnits =new JLabel(String.valueOf(comp.getProdAndDistr().getMaxProducableUnits()) + " Einheiten");
 		this.add(l_maxProducableUnits,c);
 		c.gridx=0;
 		c.gridy++;
 	}
 
 
 	private String getMissingEmployee(EmployeeType type) {
 		return	String.valueOf(CompanyController.missingEmployees(type));	
 
 	}
 
 
 	private String getMissingMachine(MachineType type) {
 		return String.valueOf(CompanyController.missingMachines(type,actualCapacity));
 
 	}
 
 
 	private String getMissingRessource(RessourceType type) {
 			return CompanyController.missingRessources(type) + " " + Ressource.getUnit(type);
 	}
 
 
 	/*public void update(){
 		
 		l_maxProducableUnits.setText(String.valueOf(comp.getProduction().getMaxProducableUnits()) + " Einheiten");
 				
 		if(PeriodInfo.getNumberOfActPeriod() == 0)
 			l_lastPeriodSellingPrice.setText("Bisher keine Produkte verkauft");
 		else
 			l_lastPeriodSellingPrice.setText(String.valueOf(PeriodInfo.getActualPeriod().getProductPrice()) + "");
 		
 		l_finishedProducts.setText(String.valueOf(String.valueOf(comp.getFinishedProducts()) + " Einheiten"));
 		
 		for(Iterator<Entry<RessourceType, Integer>> it = CompanyController.missingRessources().entrySet().iterator(); it.hasNext();){
 			Entry<RessourceType, Integer> next = it.next();
 			missRes += next.getKey().name() + ": " + next.getValue() + " " + Ressource.getUnit(next.getKey()) + "; ";
 		}
 		return missRes;
 	}*/
 	
 	private void refreshCount() {
 		pricesell.setText(cutAndTrim(pricesell.getText()));
 		amountproduce.setText(cutAndValidate(amountproduce.getText()));
		amountsell.setText(cutAndValidate(amountsell.getText()));
 		
 		try{unitsToProduce = Integer.parseInt(amountproduce.getText().trim());}catch(NumberFormatException e){}
 		try{priceToSell = Double.parseDouble(pricesell.getText());}catch(NumberFormatException e){}
 		try{amountToSell = Integer.parseInt(amountsell.getText().trim());}catch(NumberFormatException e){}
 
 		comp.getProdAndDistr().setUnitsToProduce(unitsToProduce);
 		comp.getProdAndDistr().setUnitsToSell(amountToSell);
 		comp.getProdAndDistr().setSellingPrice(priceToSell);
 		
 		if(amountToSell > (maxSellableUnits + unitsToProduce))
 			amountsell.setForeground(Color.RED);
 		else
 			amountsell.setForeground(Color.BLACK);
 		
 		if(unitsToProduce > maxProducableUnits)
 			amountproduce.setForeground(Color.RED);
 		else
 			amountproduce.setForeground(Color.BLACK);
 		
 		
 		
 		l_missingRessource0.setText(getMissingRessource(RessourceType.values()[0]));
 		l_missingRessource1.setText(getMissingRessource(RessourceType.values()[1]));
 		l_missingMachine0.setText(getMissingMachine(MachineType.values()[0]));
 		l_missingMachine1.setText(getMissingMachine(MachineType.values()[1]));
 		l_missingEmployee0.setText(getMissingEmployee(EmployeeType.values()[0]));
 		l_missingEmployee1.setText(getMissingEmployee(EmployeeType.values()[1]));
 		
 		if(priceToSell>PRICEMAX){
 			l_priceRating.setText("Preis ist zu hoch");
 			l_priceRating.setForeground(Color.RED);
 		}else if(priceToSell<PRICEMIN){
 			l_priceRating.setText("Preis ist zu niedrig");
 			l_priceRating.setForeground(Color.BLUE);
 		}else{
 			l_priceRating.setText("Preis liegt im Rahmen");
 			l_priceRating.setForeground(Color.decode("418831"));
 		}
 	}
 
 	private String cutAndValidate(String text) {
		return text.replaceAll("\\D", "");
 	}
 
 
 	private String cutAndTrim(String text) {
 		return cutAndValidate(text.replace(',', '.'));
 	}
 
 
 }
