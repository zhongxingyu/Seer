 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 
 /*
  * A class that represents a Tile on the map
  */
 public abstract	class Tile extends JButton{
 	
 	protected ImageIcon img;
 	protected Point location; 
 	protected boolean isOwned;
 	protected Player owner;
 	protected int cost;
 	protected MapPanel panel;
 	protected ActionListener tileListener;
 	
 	public Tile(Point location){
 		this.location=location;
 		cost = 20;
 		tileListener = new buyListener(this);
 		addActionListener(tileListener);
 		isOwned=false;
 		
 	}
 	
 	public void isBought(Player p){
 		owner = p;
 		isOwned=true;
 	}
 	
 	public boolean isOwned(){
 		return isOwned;
 	}
 	
  class buyListener implements ActionListener{
 	 private Tile tile;
 	 private Player p;
 	 
 	 public buyListener(Tile tile){
 		 this.tile=tile;
 	 }
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		p=GameMain.getCurrPlayer();
 		if(!isOwned){ //note to fix
 			int askBuy = JOptionPane.showConfirmDialog(null,"Would you like to buy this property? It costs " + cost , "Buy this time?", JOptionPane.YES_NO_OPTION);
 			if(askBuy==JOptionPane.YES_OPTION){
 					if(GameMain.getCurrTurns()<=2){
 						if(p.buyProperty(0, tile)){
 							JOptionPane.showMessageDialog(null,
 								    "Congratulations! You got it!");
 							tile.setBackground(p.getColor());
 							isBought(p);
 						}
 					
 					else{
 						if(p.buyProperty(cost, tile)){
 							JOptionPane.showMessageDialog(null,
 								    "Congratulations! You got it!");
 							tile.setBackground(p.getColor());
 							isBought(p);
 						}
 					}
 				}
 				else 
 				{
					JOptionPane.showMessageDialog(null,
 						    "You're too poor for this land.");
 				}
 				
 			}
             if(askBuy==JOptionPane.NO_OPTION){
             	//idk think of something to do
             }
 		}
 		else{
 			JOptionPane.showMessageDialog(null,
 				    "This tile is already owned by or you are too poor to buy it. Better luck next time");
 		}
 		
 	}
 	 
  }
 
 }
