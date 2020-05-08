 import javax.swing.*;
 import java.awt.event.*;
 import java.awt.*;
 import java.io.Serializable;
 import java.util.*;
 // I basically made it so that a few of the references for Game.whatever was changed to
 // screen.game.whatever. Im sorry if this isn't how you planned for it to work but I 
 // couldn't think of anything better. Let me know if you want me to change it.
 
 /**
  * Scene to let user visit the planet's market.
  * 
  * @author TeamTroll
  * @version 1.0
  */
 public class Market implements Scene, Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private GameScreen screen;
 	private Planet planet;
 	private Player player;
 	private JButton planetButton, confirmButton;
 	private JLabel outputLabel, inventoryLabel1, inventoryLabel2, moneyLabel;
 	private int level, money;
 	private Good[] buyGoods, sellGoods;
 	private Hashtable<String, Integer> inventory;
 	private Transaction trans;
 	private JRadioButton buyRadio, sellRadio;
 	private ButtonGroup buttonGroup;
 	/**
 	 * Creates the market scene.
 	 * 
 	 * @param GameScreen
 	 *            screen The screen of the game.
 	 * @param Planet
 	 *            planet THe current planet.
 	 */
 	public Market(final GameScreen screen, Planet planet) {
 		
 		this.screen = screen;
 		this.planet = planet;
 		this.player = screen.game.getPlayer();
 		inventory = player.getInventory();
 		trans = new Transaction(player, this);
 
 		buyRadio = new JRadioButton("Buy", true);
 		sellRadio = new JRadioButton("Sell", false);
 		confirmButton = new JButton("Confirm");
 
 		buyRadio.addActionListener(new PurchaseListener());
 		sellRadio.addActionListener(new PurchaseListener());
 
 		buttonGroup = new ButtonGroup();
 		buttonGroup.add(buyRadio);
 		buttonGroup.add(sellRadio);
 		confirmButton.addActionListener(new PurchaseListener());
 
 		planetButton = new JButton("Planet");
 		planetButton.addActionListener(new PurchaseListener());
 
 		level = planet.getTechLevel();
 		money = player.getMoney();
 
 		outputLabel = new JLabel("      Market Place (" + planet.getName()
 				+ ", Level " + level + ")         ");
 		moneyLabel = new JLabel("Total Money: $" + money
 				+ "      Cargo Space: "
 				+ (player.getShip().getCargoBay() - player.numItems())
 				+ "     ");
 		inventoryLabel1 = new JLabel(player.getInventory1());
 		inventoryLabel2 = new JLabel(player.getInventory2());
 
 		buyGoods = generateBuyMarket();
 		sellGoods = generateSellMarket();
 	}
 
 	/**
 	 * Helper class for switching from buying to selling or vice versa. Also
 	 * used upon confirming a purchase.
 	 */
 	private class PurchaseListener implements ActionListener {
 		/**
 		 * Switches contexts of a transaction or confirms a transaction.
 		 * 
 		 * @param ActionEvent
 		 *            e The created action event.
 		 */
 		public void actionPerformed(ActionEvent e) {
 			if (e.getSource() == confirmButton)
 				trans = trans.confirm();
 
 			refresh();
 			// Again, I set it to screen.game.getPlanet();
 			if (e.getSource() == planetButton)
 				screen.setScene(screen.game.getPlanet());
 		}
 		
 		public String toString(){
 			return super.toString();
 		}
 	}
 
 	/**
 	 * Paints the components onto the screen.
 	 * 
 	 * @param JPanel
 	 *            p The panel to contain the components.
 	 * @param Graphics
 	 *            g The graphics to the corresponding page.
 	 */
 	public void paint(JPanel p, Graphics g) {
 		p.removeAll();
 
 		p.add(outputLabel);
 		p.add(planetButton);
 		p.add(inventoryLabel1);
 		p.add(inventoryLabel2);
 		p.add(moneyLabel);
 		p.add(buyRadio);
 		p.add(sellRadio);
 
 		for (Good good : buyGoods)
 			p.add(good);
 
 		p.add(trans);
 		p.add(confirmButton);
 
 		p.revalidate();
 	}
 
 	/**
 	 * Helper method used to refresh the view of a transaction.
 	 */
 	private void refresh() {
 		// Set to screen.game.getPlayer();
 		player = screen.game.getPlayer();
 		inventoryLabel1.setText(player.getInventory1());
 		inventoryLabel2.setText(player.getInventory2());
 
 		// player.printInventory();
 
 		moneyLabel = new JLabel("Total Money: $" + player.getMoney()
 				+ "      Cargo Space: "
 				+ (player.getShip().getCargoBay() - player.numItems())
 				+ "     ");
 
 		for (Good g : buyGoods) {
 			g.reset(trans);
 		}
 
 		// System.out.println("Market refresh called");
 
 		screen.setScene(this);
 	}
 
 	/**
 	 * Helper method to generate the stats of the goods that can be purchased in
 	 * the market.
 	 * 
 	 * @return Good[] The array of goods that are to be purchased.
 	 */
 	private Good[] generateBuyMarket() {
 		Good[] buyGoods = new Good[10];
 
 		// fix this
 		buyGoods[0] = new Good(planet, this, trans, "Water", 0, 0, 2, 30, 3, 4,
 				30, 50, "DROUGHT".equals(planet.getSpecialEvent()));
 		buyGoods[1] = new Good(planet, this, trans, "Furs", 0, 0, 0, 250, 10,
				10, 230, 280, "COLD".equals(planet.getSpecialEvent()));
 		buyGoods[2] = new Good(planet, this, trans, "Food", 1, 0, 1, 100, 5, 5,
				90, 160, "CROPFAIL".equals(planet.getSpecialEvent()));
 		buyGoods[3] = new Good(planet, this, trans, "Ore", 2, 2, 3, 350, 20,
				10, 350, 420, "WAR".equals(planet.getSpecialEvent()));
 		buyGoods[4] = new Good(planet, this, trans, "Games", 3, 1, 6, 250, -10,
				5, 160, 270, "BOREDOM".equals(planet.getSpecialEvent()));
 		buyGoods[5] = new Good(planet, this, trans, "Firearms", 3, 1, 5, 1250,
				-75, 100, 600, 1100, "WAR".equals(planet.getSpecialEvent()));
 		buyGoods[6] = new Good(planet, this, trans, "Medicine", 4, 1, 6, 650,
				-20, 10, 400, 700, "PLAGUE".equals(planet.getSpecialEvent()));
 		buyGoods[7] = new Good(planet, this, trans, "Machines", 4, 3, 5, 900,
				-30, 5, 600, 800, "LACKOFWORKERS".equals(planet.getSpecialEvent()));
 		buyGoods[8] = new Good(planet, this, trans, "Narcotics", 5, 0, 5, 3500,
				-125, 150, 2000, 3000, "BOREDOM".equals(planet.getSpecialEvent()));
 		buyGoods[9] = new Good(planet, this, trans, "Robots", 6, 4, 7, 5000,
				-150, 100, 3500, 5000, "LACKOFWORKERS".equals(planet.getSpecialEvent()));
 
 //		buyGoods[0] = new Good(planet, this, trans, "Water", 0, 0, 2, 30, 3, 4,
 //				30, 50,true);
 //		buyGoods[1] = new Good(planet, this, trans, "Furs", 0, 0, 0, 250, 10,
 //				10, 230, 280,true);
 //		buyGoods[2] = new Good(planet, this, trans, "Food", 1, 0, 1, 100, 5, 5,
 //				90, 160,true);
 //		buyGoods[3] = new Good(planet, this, trans, "Ore", 2, 2, 3, 350, 20,
 //				10, 350, 420,true);
 //		buyGoods[4] = new Good(planet, this, trans, "Games", 3, 1, 6, 250, -10,
 //				5, 160, 270,true);
 //		buyGoods[5] = new Good(planet, this, trans, "Firearms", 3, 1, 5, 1250,
 //				-75, 100, 600, 1100,true);
 //		buyGoods[6] = new Good(planet, this, trans, "Medicine", 4, 1, 6, 650,
 //				-20, 10, 400, 700,true);
 //		buyGoods[7] = new Good(planet, this, trans, "Machines", 4, 3, 5, 900,
 //				-30, 5, 600, 800,true);
 //		buyGoods[8] = new Good(planet, this, trans, "Narcotics", 5, 0, 5, 3500,
 //				-125, 150, 2000, 3000,true);
 //		buyGoods[9] = new Good(planet, this, trans, "Robots", 6, 4, 7, 5000,
 //				-150, 100, 3500, 5000,true);
 
 		if (level < 6)
 			buyGoods[9].disable();
 		if (level < 5)
 			buyGoods[8].disable();
 		if (level < 4) {
 			buyGoods[7].disable();
 			buyGoods[6].disable();
 		}
 		if (level < 3) {
 			buyGoods[5].disable();
 			buyGoods[4].disable();
 		}
 		if (level < 2)
 			buyGoods[3].disable();
 		if (level < 1)
 			buyGoods[2].disable();
 
 		return buyGoods;
 	}
 
 	/**
 	 * Helper method to generate the stats of the goods that can be sold in the
 	 * market.
 	 * 
 	 * @return Good[] The array of goods that are to be sold.
 	 */
 	private Good[] generateSellMarket() {
 		Good[] sellGoods = new Good[10];
 
 		sellGoods[0] = new Good(planet, this, trans, "Water", 0, 0, 2, 30, 3,
 				4, 30, 50,"DROUGHT".equals(planet.getSpecialEvent()));
 		sellGoods[1] = new Good(planet, this, trans, "Furs", 0, 0, 0, 250, 10,
 				10, 230, 280,"COLD".equals(planet.getSpecialEvent()));
 		sellGoods[2] = new Good(planet, this, trans, "Food", 1, 0, 1, 100, 5,
 				5, 90, 160,"CROPFAIL".equals(planet.getSpecialEvent()));
 		sellGoods[3] = new Good(planet, this, trans, "Ore", 2, 2, 3, 350, 20,
 				10, 350, 420,"WAR".equals(planet.getSpecialEvent()));
 		sellGoods[4] = new Good(planet, this, trans, "Games", 3, 1, 6, 250,
 				-10, 5, 160, 270,"BOREDOM".equals(planet.getSpecialEvent()));
 		sellGoods[5] = new Good(planet, this, trans, "Firearms", 3, 1, 5, 1250,
 				-75, 100, 600, 1100,"WAR".equals(planet.getSpecialEvent()));
 		sellGoods[6] = new Good(planet, this, trans, "Medicine", 4, 1, 6, 650,
 				-20, 10, 400, 700,"PLAGUE".equals(planet.getSpecialEvent()));
 		sellGoods[7] = new Good(planet, this, trans, "Machines", 4, 3, 5, 900,
 				-30, 5, 600, 800,"LACKOFWORKERS".equals(planet.getSpecialEvent()));
 		sellGoods[8] = new Good(planet, this, trans, "Narcotics", 5, 0, 5,
 				3500, -125, 150, 2000, 3000,"BOREDOM".equals(planet.getSpecialEvent()));
 		sellGoods[9] = new Good(planet, this, trans, "Robots", 6, 4, 7, 5000,
 				-150, 100, 3500, 5000,"LACKOFWORKERS".equals(planet.getSpecialEvent()));
 
 		if (level < 4)
 			sellGoods[9].disable();
 		if (level < 3)
 			sellGoods[7].disable();
 		if (level < 2)
 			sellGoods[3].disable();
 		if (level < 1) {
 			sellGoods[6].disable();
 			sellGoods[5].disable();
 			sellGoods[4].disable();
 		}
 
 		return sellGoods;
 	}
 
 	/**
 	 * Checks if the buy radio button is selected. Indicates whether the user is
 	 * currently buying or selling from the market.
 	 * 
 	 * @return boolean A boolean indicating whether or not the user is buying
 	 *         from the market.
 	 */
 	public boolean buy() {
 		return buyRadio.isSelected();
 	}
 	
 	public String toString(){
 		return super.toString();
 	}
 }
