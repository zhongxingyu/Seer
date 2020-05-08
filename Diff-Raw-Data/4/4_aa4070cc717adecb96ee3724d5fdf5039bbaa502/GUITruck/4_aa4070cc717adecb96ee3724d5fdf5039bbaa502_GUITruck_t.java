 
 package gui.components;
 
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 
 import javax.swing.ImageIcon;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 /**
  * The truck collects glass that has been taken off of the line collecting three
  * glass parts and emptying itself on the fourth
  */
 @SuppressWarnings("serial")
 public class GUITruck extends GuiComponent
 {
 
 	private int TRUCK_SPEED = 5;//added by monroe. default was 2 before, and hard-coded everywhere.
 
 	private int MAX_TRUCK_SPEED =10;//added by monroe.
 	private int MIN_TRUCK_SPEED = 1;//added by monroe.
 
 
 	/**
 	* Allows changes to be made mid-program to GUITruck speed (perhaps a GUI button).
 	* @author Monroe
 	*/
 	public void changeTruckSpeed(int change){
 		TRUCK_SPEED +=change;
 		if(TRUCK_SPEED > MAX_TRUCK_SPEED){
 			TRUCK_SPEED = MAX_TRUCK_SPEED;
 		}
 		else if(TRUCK_SPEED < MIN_TRUCK_SPEED){
 			TRUCK_SPEED = MIN_TRUCK_SPEED;
 		}
 		//System.out.println("Truck speed changed to: " + TRUCK_SPEED);
 	}
 
 	/**
 	 * Image of default truck
 	 */
 	ImageIcon truckLeft = new ImageIcon("imageicons/truck/truckV3Image.png");
 
 	/**
 	 * Image of truck facing right
 	 */
 	ImageIcon truckRight = new ImageIcon("imageicons/truckRight.png");
 
 	/**
 	 * Trucks original location
 	 */
 	Point truckOriginalLoc;
 
 	/**
 	 * Trucks part
 	 */
 	GUIGlass guiPart;
 
 	enum TruckState
 	{
		LOADING, LEAVING, RETURNING, IDLE
 	};
 
 	TruckState state;
 
 	/**
 	 * Trucks original location
 	 */
 	Point truckOrig;
 	
 	public void setLocation(int x, int y){//added by monroe
 		super.setLocation(x,y);
 		truckOrig.setLocation(getCenterLocation());
 	}
 
 	/** Public constructor for GUITruck */
 	public GUITruck(Transducer t)
 	{
 		setIcon(truckLeft);
 		setSize(getIcon().getIconWidth(), getIcon().getIconHeight());
 		state = TruckState.LOADING;
 		truckOrig = new Point(this.getCenterX(), this.getCenterY());
 
 		transducer = t;
 		transducer.register(this, TChannel.TRUCK);
 	}
 
 	/**
 	 * Moving the stack of glass to and from the truck to the bin, depending on whether
 	 * glass is being taken from bin or bin is being refilled
 	 */
 	public void movePartIn()
 	{
 		if (part.getCenterX() < getCenterX())
 			part.setCenterLocation(part.getCenterX() + 1, part.getCenterY());
 		else if (part.getCenterX() > getCenterX())
 			part.setCenterLocation(part.getCenterX() - 1, part.getCenterY());
 
 		if (part.getCenterY() < getCenterY())
 			part.setCenterLocation(part.getCenterX(), part.getCenterY() + 1);
 		else if (part.getCenterY() > getCenterY())
 			part.setCenterLocation(part.getCenterX(), part.getCenterY() - 1);
 
 		if (part.getCenterX() == getCenterX() && part.getCenterY() == getCenterY())
 		{
 			//part.setVisible(false);//monroe moved to after the part is completely removed
 			//part = null;//monroe moved to after the part is completely removed
			state = TruckState.IDLE;
 			transducer.fireEvent(TChannel.TRUCK, TEvent.TRUCK_GUI_LOAD_FINISHED, null);
 		}
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		if (state == TruckState.LOADING && part != null)
 		{
 			movePartIn();
 		}
 		if (state == TruckState.LEAVING)
 		{
 			moveTruckOut();
 		}
 		if (state == TruckState.RETURNING)
 		{
 			moveTruckIn();
 		}
 	}
 
 	private void moveTruckOut()
 	{
 		setCenterLocation(getCenterX() + TRUCK_SPEED, getCenterY());
 		part.setLocation((int)part.getLocation().getX() + TRUCK_SPEED, (int)part.getLocation().getY());//added by monroe to show part leaving on truck
 		if (getCenterX() > (parent./*getParent().getGuiParent().*/getWidth() + this.getWidth()*3/4))//changed from < to > by monroe and added math to make truck actually leave the right panel
 		{
 			state = TruckState.RETURNING;
 			part.setVisible(false);//moved here by monroe
 			part = null;//moved here by monroe
 		}
 	}
 
 	private void moveTruckIn()
 	{
 		setCenterLocation(getCenterX() - TRUCK_SPEED, getCenterY());//changed from -1 to -TRUCK_SPEED by monroe to speed backing up animation up
 		if (getCenterX() < truckOrig.getX())//changed from > to < by monroe
 		{
 			setCenterLocation(((int)truckOrig.getX()), ((int)truckOrig.getY()));
 			state = TruckState.LOADING;
 			transducer.fireEvent(TChannel.TRUCK, TEvent.TRUCK_GUI_EMPTY_FINISHED, null);
 		}
 	}
 
 	@Override
 	public void addPart(GUIGlass part)
 	{
 		this.part = part;
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args)
 	{
 		if (event == TEvent.TRUCK_DO_EMPTY)
 		{
 			state = TruckState.LEAVING;
 		}
 	}
 }
