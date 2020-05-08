 package renderer;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.JTextField;
 import javax.swing.Timer;
 
 import playable.TypeOfPlayer;
 
 import dispatcher.Dispatcher;
 import engine.Base;
 import engine.Element;
 
 /**
  * This class display a base.
  * @author Yuki
  *
  */
 @SuppressWarnings("serial")
 public class BaseSprite extends SelectedSprite implements MouseListener, ActionListener{
 	/**
 	 * nbAgents is the JTextField which is used to display the nbAgents of the corresponding base.
 	 */
 	private JTextField nbAgents;
 	/**
 	 * timer and blink are useful to create the blink when the tower is waiting for building.
 	 */
 	private Timer timer; 
 	private boolean blink;
 	/**
 	 * engineBase is a reference to the corresponding base of this sprite.
 	 */
 	private Base engineBase;
 
 	public BaseSprite(Base newBase) {
 		super();
 		this.setLayout(new BorderLayout());
 		
 		this.engineBase = newBase;
 		
 		this.timer = new Timer (500, this);
 	    this.blink = false;
 		
 		this.nbAgents = new JTextField(String.valueOf(newBase.getNbAgents()));
 		this.nbAgents.setPreferredSize(new Dimension(23, 20));
 		this.nbAgents.setDisabledTextColor(new Color(255, 255, 255));
 		this.nbAgents.setEnabled(false);
 		this.nbAgents.setBorder(null);
 		this.nbAgents.setHorizontalAlignment(JTextField.CENTER);
 		this.nbAgents.setOpaque(false);
 		this.nbAgents.setIgnoreRepaint(false); // for better performance
 		this.nbAgents.addMouseListener(this);
 		this.add(this.nbAgents, BorderLayout.CENTER);
 	}
 	
 	@Override
 	protected void paintComponent(Graphics g){
 		super.paintComponent(g);
 		
 		//update the sprite of the base if it's necessary
 		if(this.engineBase.isOwnerChanged()){
 			if(!this.engineBase.isANeutralBase())
 				this.setImage(this.engineBase.getOwner().getType().getImageOfBase());
 			else
 				this.setImage(TypeOfPlayer.NEUTRAL.getImageOfBase());
 			this.engineBase.setOwnerChanged(false);
 		}
 		
 		//update the JTextField which represents the nbAgents
 		this.nbAgents.setText(String.valueOf(this.engineBase.getNbAgents()));
 		
 		//start blinking when the base is full (and stop when it's not)
 		if(this.engineBase.isFull())
 			this.timer.start();
		else{
 			this.timer.stop();
			if(!this.engineBase.isANeutralBase())
				this.setImage(this.engineBase.getOwner().getType().getImageOfBase());
			else
				this.setImage(TypeOfPlayer.NEUTRAL.getImageOfBase());
		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		this.setBorder(BorderFactory.createLineBorder(Color.black));
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		this.setBorder(null);
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		if(this.engineBase.isAPlayerBase()){
 			this.setOpaque(true);
 			this.setBackground(new Color(255, 100, 100));
 		}
 		
 		//set starting base
 		if(!SelectedSprite.isThereAtLeastOneStartingElement() && this.engineBase.isAPlayerBase())			
 			SelectedSprite.startingElements.add(this.engineBase);
 		
 		//check if the player isn't choosing unit on another base
 		if(SelectedSprite.isThereAnEndingElement()){
 			Dispatcher.getRenderer().hideRadialMenus();
 		}
 		
 		//check if the player isn't choosing a tower type
 		if(TowerSprite.isThereOneTowerToBuild()){
 			TowerSprite.resetTowerToBuild();
 			Dispatcher.getRenderer().hideRadialMenus();
 		}
 		
 		//set ending base
 		if (SelectedSprite.isThereAtLeastOneStartingElement() && !(SelectedSprite.startingElements.size()==1 && SelectedSprite.startingElements.contains(this.engineBase)))
 			SelectedSprite.endingElement = this.engineBase;
 
 		//to fix the bug when you can control the IA
 		if(!SelectedSprite.startingElements.isEmpty()){
 			boolean aSeletedBaseIsNotAPlayerBase = false;
 			for(Element element:SelectedSprite.startingElements){
 				if(element.getClass() == Base.class){
 					if(!((Base) element).isAPlayerBase())
 						aSeletedBaseIsNotAPlayerBase = true;
 				}
 			}
 			if(aSeletedBaseIsNotAPlayerBase){
 				SelectedSprite.resetStartingElements();
 				SelectedSprite.resetEndingElement();
 			}
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		this.setOpaque(false);
 		this.setBackground(null); // i don't know why i need to indicate a new background color...
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent arg0) {}
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if (blink){
 			this.setImage(this.engineBase.getOwner().getType().getImageOfBaseFull());
       	  this.blink = false;
 		}
         else{
         	this.setImage(this.engineBase.getOwner().getType().getImageOfBase());
         	this.blink = true;
         }
 	}
 	
 	// GETTERS & SETTERS
 	
 	public Base getEngineBase() {
 		return this.engineBase;
 	}
 	
 	public JTextField getNbAgents() {
 		return this.nbAgents;
 	}
 	
 	public static ArrayList<Base> getStartingBases() {
 		ArrayList<Base> res = new ArrayList<Base>();
 		for(Element el:SelectedSprite.startingElements){
 			if(el.getClass() == Base.class)
 				res.add((Base) el);
 		}
 		return res;
 	}
 	
 	public static void setStartingBases(Point2D.Float startingCorner,Point2D.Float endingCorner){
 		for(Base potentialStartingBase:Dispatcher.getEngine().getBases()){
 			if(potentialStartingBase.isAPlayerBase()){	
 				if(Math.min(startingCorner.x,endingCorner.x) < potentialStartingBase.getCenter().x 
 					&&	potentialStartingBase.getCenter().x < Math.max(startingCorner.x,endingCorner.x)
 					&&	Math.min(startingCorner.y,endingCorner.y) < potentialStartingBase.getCenter().y
 					&&  potentialStartingBase.getCenter().y < Math.max(startingCorner.y,endingCorner.y)
 				){
 					SelectedSprite.startingElements.add(potentialStartingBase);
 				}
 			}
 		}
 	}
 }
