 import java.awt.event.*;
 import java.awt.*;
 import javax.swing.*;
 import java.util.*;
 
 /**
  * Represents a Good that is traded in the game.
  * 
  * @author TeamTroll
  * @version 1.0
  */
 @SuppressWarnings("serial")
 public class Good extends JPanel {
 	private final Random rand = new Random();
 	private Planet planet;
 	private Transaction trans;
 	private final JLabel nameLabel, priceLabel, quantityLabel;
 	private final JButton plusButton, minusButton;
 	private String name;
	private int price, quantity;
 
 	private final int MTLP, MTLU, TTP, BP, IPL, VAR, MTL, MTH;
 	private boolean IE;
 
 	/**
 	 * Creates the Good.
 	 * 
 	 * @param Planet
 	 *            planet The planet of the game.
 	 * @param Market
 	 *            market The market of the game.
 	 * @param Transaction
 	 *            trans The transaction of the game.
 	 * @param String
 	 *            name The name of the good.
 	 * @param int MTLP The minimum level of the planet needed to buy the good.
 	 * @param int MTLU The minimum level of the planet needed to sell the good.
 	 * @param int TTP
 	 * @param int BP The base price of the good.
 	 * @param int IPL
 	 * @param int VAR The variance in price of the good.
 	 * @param int MTL
 	 * @param int MTH
 	 * IE	CR	ER
 	 */
 	public Good(Planet planet, final Market market, final Transaction trans,
 			final String name, int MTLP, int MTLU, int TTP, int BP, int IPL,
 			int VAR, int MTL, int MTH, 
 			boolean IE) {
 		
 		this.planet = planet;
 		this.setTrans(trans);
 
 		this.name = name;
 		this.MTLP = MTLP;
 		this.MTLU = MTLU;
 		this.TTP = TTP;
 		this.BP = BP;
 		this.IPL = IPL;
 		this.VAR = VAR;
 		this.MTL = MTL;
 		this.MTH = MTH;
 		this.IE = IE;
 		
 		nameLabel = new JLabel(name);
 
 		quantity = 0;
 
 		price = price();
 		priceLabel = new JLabel("$" + price);
 		quantityLabel = new JLabel("0");
 
 		plusButton = new JButton("+");
 		minusButton = new JButton("-");
 
 		plusButton.addActionListener(new ActionListener() {
 			/**
 			 * Increases the amount of a good being bought or sold.
 			 * 
 			 * @param ActionEvent
 			 *            e The created action event.
 			 */
 			public void actionPerformed(ActionEvent e) {
 				if (market.buy())
 					quantityLabel.setText("" + trans.add(name, getBuyPrice()));
 				else
 					quantityLabel.setText(""
 							+ (-1 * trans.add(name, getSellPrice())));
 			}
 		});
 
 		minusButton.addActionListener(new ActionListener() {
 			/**
 			 * Decrease the amount of a good being bought or sold.
 			 * 
 			 * @param ActionEvent
 			 *            e The created action event.
 			 */
 			public void actionPerformed(ActionEvent e) {
 				if (market.buy())
 					quantityLabel.setText(""
 							+ trans.remove(name, getBuyPrice()));
 				else
 					quantityLabel.setText(""
 							+ (-1 * trans.remove(name, getSellPrice())));
 			}
 		});
 
 		setPreferredSize(new Dimension(518, 40));
 
 		add(nameLabel);
 		add(priceLabel);
 		add(quantityLabel);
 
 		add(plusButton);
 		add(minusButton);
 	}
 
 	/**
 	 * Helper method to calculate the price of the good based on the stats.
 	 * passed into the class.
 	 */
 	private int price() {
 		int goodPrice = (BP + (IPL * (planet.getTechLevel() - MTLP)) + (rand
 				.nextInt(2 * VAR - 1) - VAR));
 		if(IE){
 			goodPrice *= (rand.nextInt(5)+10);
 			
 		}
 		return goodPrice;
 	}
 
 	/**
 	 * Getter for the buying price.
 	 * 
 	 * @return int The buying price of the good.
 	 */
 	public int getBuyPrice() {
 		return price;
 	}
 
 	/**
 	 * Getter for the selling price.
 	 * 
 	 * @return int The selling price of the good.
 	 */
 	public int getSellPrice() {
 		return (int) (price * 0.75);
 	}
 
 	/**
 	 * Disable the plus and minus buttons of a good that is too high level to be
 	 * sold in the current market.
 	 */
 	public void disable() {
 		plusButton.setEnabled(false);
 		minusButton.setEnabled(false);
 	}
 
 	/**
 	 * Updates the transaction that the good currently deals with.
 	 * 
 	 * @param Transaction
 	 *            trans The current transaction occurring in the market.
 	 */
 	public void reset(Transaction trans) {
 		this.setTrans(trans);
 		quantity = 0;
 		quantityLabel.setText(quantity + "");
 	}
 
 	/**
 	 * Getter for the transaction.
 	 * 
 	 * @return Transaction The current transaction.
 	 */
 	public Transaction getTrans() {
 		return trans;
 	}
 
 	/**
 	 * Setter for the transaction.
 	 * 
 	 * @param Transaction
 	 *            trans The transaction to set
 	 */
 	public void setTrans(Transaction trans) {
 		this.trans = trans;
 	}
 }
