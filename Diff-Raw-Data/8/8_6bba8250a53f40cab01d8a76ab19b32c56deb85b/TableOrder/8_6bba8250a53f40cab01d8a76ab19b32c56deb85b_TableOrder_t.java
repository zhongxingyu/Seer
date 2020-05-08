 package snookerTables;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.NumberFormat;
 import javax.swing.*;
 
 public class TableOrder extends JPanel implements ActionListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private Table table;
 	private NumberFormat formatter;
 	private JLabel hirePriceLabel, drinkPriceLabel, foodPriceLabel, extraPriceLabel, totalPriceLabel, membershipPriceLabel;
 	private boolean shown;
 	
 	 /** Stroke size. it is recommended to set it to 1 for better view */
     protected int strokeSize = 1;
     /** Color of shadow */
     protected Color shadowColor = Color.black;
     /** Sets if it drops shadow */
     protected boolean shady = true;
     /** Sets if it has an High Quality view */
     protected boolean highQuality = true;
     /** Double values for Horizontal and Vertical radius of corner arcs */
     protected Dimension arcs = new Dimension(20, 20);
     /** Distance between shadow border and opaque panel border */
     protected int shadowGap = 5;
     /** The offset of shadow.  */
     protected int shadowOffset = 4;
     /** The transparency value of shadow. ( 0 - 255) */
     protected int shadowAlpha = 150;
 
 	
 	public TableOrder(Table table) {
 
 		super();
         setOpaque(false);
 		
 		formatter = NumberFormat.getCurrencyInstance();
 //		this.setVisible(false);
 //		this.setSize(new Dimension(260,320));
 		this.table=table;
 		Container container = new Container();
 		container.setLayout(new BorderLayout());
 		this.add(container);
 		JLabel name = new JLabel(table.getTableName(), JLabel.CENTER);
 		name.setFont(new Font(null, Font.BOLD, 20));
 		container.add(name, BorderLayout.NORTH);
 		
 		JPanel central = new JPanel();
 		central.setLayout(new BoxLayout(central, BoxLayout.Y_AXIS)); 
 		container.add(central, BorderLayout.CENTER);
 		
 		hirePriceLabel=new JLabel("Hire Cost: £0.00");
 		central.add(hirePriceLabel);
 		
 		drinkPriceLabel=new JLabel("Drink Cost: £0.00");
 		central.add(drinkPriceLabel);
 		
 		foodPriceLabel=new JLabel("Food Cost: £0.00");
 		central.add(foodPriceLabel);
 		
 		extraPriceLabel=new JLabel("Extra Cost: £0.00");
 		central.add(extraPriceLabel);
 		
 		membershipPriceLabel=new JLabel("Membership Cost: £0.00");
 		central.add(membershipPriceLabel);
 		
 		totalPriceLabel=new JLabel("Total: £0.00");
 		central.add(totalPriceLabel);
 		totalPriceLabel.setForeground(Color.RED);
 		totalPriceLabel.setFont(new Font(null, Font.BOLD, 15));
 		
 		JButton close  = new JButton("Close");
 		close.setActionCommand("close");
 		close.addActionListener(this);
 		container.add(close, BorderLayout.SOUTH);
 		
 	}
 	
 	public void setShown(boolean shown){
 		this.shown=shown;
 	}
 	
 	public boolean isShown(){
 		return shown;
 	}
 	
 	public void updatePrice(){
 		double hirePrice = table.getHirePrice();
 		hirePriceLabel.setText("Hire Cost: "+formatter.format(hirePrice));
 		
 		double drinkPrice = table.getDrinkPrice();
 		drinkPriceLabel.setText("Drink Cost: "+formatter.format(drinkPrice));
 		
 		double foodPrice = table.getFoodPrice();
 		foodPriceLabel.setText("Food Cost: "+formatter.format(foodPrice));
 		
 		double extraPrice = table.getExtraPrice();
 		extraPriceLabel.setText("Extra Cost: "+formatter.format(extraPrice));
 		
 		double membershipPrice = table.getMembershipPrice();
 		membershipPriceLabel.setText("Membership Cost: "+formatter.format(membershipPrice));
 		
		double totalPrice = hirePrice+drinkPrice+foodPrice+extraPrice+membershipPrice;
 		totalPriceLabel.setText("Total: "+formatter.format(totalPrice));
 		
 		
 		
 	}
 
 	
 //	public void toggleVisible(){
 //		this.setVisible(true);
 //	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if("close".equals(e.getActionCommand())){
 			table.getMain().toggleOrderVisibility(table);
 		}
 		
 	}
 	
 	@Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         int width = getWidth();
         int height = getHeight();
         int shadowGap = this.shadowGap;
         Color shadowColorA = new Color(shadowColor.getRed(), 
 	shadowColor.getGreen(), shadowColor.getBlue(), shadowAlpha);
         Graphics2D graphics = (Graphics2D) g;
 
         //Sets antialiasing if HQ.
         if (highQuality) {
             graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
 			RenderingHints.VALUE_ANTIALIAS_ON);
         }
 
         //Draws shadow borders if any.
         if (shady) {
             graphics.setColor(shadowColorA);
             graphics.fillRoundRect(
                     shadowOffset,// X position
                     shadowOffset,// Y position
                     width - strokeSize - shadowOffset, // width
                     height - strokeSize - shadowOffset, // height
                     arcs.width, arcs.height);// arc Dimension
         } else {
             shadowGap = 1;
         }
 
         //Draws the rounded opaque panel with borders.
         graphics.setColor(getBackground());
         graphics.fillRoundRect(0, 0, width - shadowGap, 
 		height - shadowGap, arcs.width, arcs.height);
         graphics.setColor(getForeground());
         graphics.setStroke(new BasicStroke(strokeSize));
         graphics.drawRoundRect(0, 0, width - shadowGap, 
 		height - shadowGap, arcs.width, arcs.height);
 
         //Sets strokes to default, is better.
         graphics.setStroke(new BasicStroke());
     }
 
 }
