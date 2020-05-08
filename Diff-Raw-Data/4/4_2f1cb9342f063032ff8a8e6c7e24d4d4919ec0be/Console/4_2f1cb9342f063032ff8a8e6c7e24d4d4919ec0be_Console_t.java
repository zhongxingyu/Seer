 package chalmers.TDA367.B17.console;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 
 /** A simple graphical text output console */
 public class Console {
 	private final static int TIMESTAMP_OFFSET = 8;
 	private final static int MESSAGE_OFFSETY = 5;
 	private final static int MESSAGE_OFFSETX = 93;
 	private final static int ROW_HEIGHT = 18;
 	private static final int HIDE_TIMER = 2500;
 	
 	private int posX;
 	private int posY;
 	private int width;
 	private int height;
 	private int totalY;
 	private int maxMessages;
 	private ArrayList<ConsoleMessage> messages;
 	private OutputLevel outputLevel;
 	private boolean border;
 	private boolean visible;
 	private boolean active;
 	private boolean timerHide;
 	private int timeSinceLastActive;
 	
 	/**
	 * Create a new console at the given position and size with a starting output level and an optional border
 	 * @param posX
 	 * @param posY
 	 * @param width
 	 * @param height
 	 * @param initialOutputLevel
	 * @param border
 	 */
 	public Console(int posX, int posY, int width, int height, OutputLevel initialOutputLevel, boolean border){
 		messages = new ArrayList<ConsoleMessage>();
 		this.posX = posX;
 		this.posY = posY;
 		this.width = width;
 		this.height = height;
 		outputLevel = initialOutputLevel;
 		this.border = border;
 		visible = true;
 		
 		maxMessages = (height-2*MESSAGE_OFFSETY) / (ROW_HEIGHT);
 		totalY = posY + height;
 	}
 	
 	/**
 	 * Message level - different levels give different text colors
 	 */
 	public static enum MsgLevel{
 		/**	Red text color	 */
 		ERROR, 
 		/**	Yellow text color	 */
 		INFO, 
 		/**	White text color	 */
 		STANDARD
 	}
 	
 	/**
 	 * Output level
 	 */
 	public static enum OutputLevel{
 		/** Only outputs error messages */
 		ERROR,
 		/** Only outputs error and info messages */
 		INFO,
 		/** Outputs  all messages */
 		ALL
 	}
 		
 	/**
 	 * Add a console message to the console and remove the oldest message if the max number of messages is reached 
 	 * @param conMessage The console message to add
 	 */
 	public void addMsg(ConsoleMessage conMessage){
 		setActive(true);
 		// remove of the oldest message if the number of messages exceeds max
 
 		MsgLevel msgLevel = conMessage.getMessageLevel();
 		
 		if((outputLevel == OutputLevel.ALL) || ((outputLevel == OutputLevel.INFO) && (msgLevel != MsgLevel.STANDARD)) || ((outputLevel == OutputLevel.ERROR) && (msgLevel == MsgLevel.ERROR))){
 			if(messages.size() >= maxMessages){
 				messages.remove(0);
 			}
 			messages.add(conMessage);
 		}
 	}
 	
 	/**
 	 * Add a standard message to the console and remove the oldest message if the max number of messages is reached 
 	 * @param message The message to add
 	 */
 	public void addMsg(String message){
 		addMsg(new ConsoleMessage(message, MsgLevel.STANDARD));
 	}
 		
 	/**
 	 * Add a message with a message level to the console and remove the oldest message if the max number of messages is reached
 	 * @param message The message
 	 * @param msgLevel The message level
 	 */
 	public void addMsg(String message, MsgLevel msgLevel){
 		addMsg(new ConsoleMessage(message, msgLevel));
 	}
 	
 	/**
 	 * Set the output level for the console
 	 * @param outputLevel The output level to set to
 	 */
 	public void setOutputLevel(OutputLevel outputLvl){
 		outputLevel = outputLvl;
 	}
 	
 	/**
 	 * Updates the console and determines if it should hide
 	 * @param delta
 	 */
 	public void update(int delta){
 		if(active){
 			timeSinceLastActive += delta;
 			if(timerHide){
 				if(timeSinceLastActive >= HIDE_TIMER){
 					setVisible(false);
 					setActive(false);
 					timeSinceLastActive = 0;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Render the messages on the given Graphics object
 	 * @param graphics The graphics object to render the messages on
 	 */
 	public void renderMessages(Graphics graphics) {	
 		if(visible){
 			
 			if(border){
 				// draw the console "window"
 				graphics.setColor(Color.white);
 				graphics.drawRect(posX, posY, width, height);
 			}
 			
 			if(!messages.isEmpty()){
 				// draw the oldest message above the newer messages
 				for(int i = 0; i < messages.size(); i++){
 					ConsoleMessage currentMsg = messages.get(i);
 					MsgLevel msgLevel = currentMsg.getMessageLevel();
 			
 					// draw the timestamp of the message
 					graphics.setColor(Color.white);
 					graphics.drawString("[" + currentMsg.getTimestamp() + "] ", posX + TIMESTAMP_OFFSET, totalY - ROW_HEIGHT*(messages.size()-i) - 10 + MESSAGE_OFFSETY);
 					
 					// set the color of the text depending on the message level
 					if(msgLevel == MsgLevel.ERROR)
 						graphics.setColor(Color.red);
 					if(msgLevel == MsgLevel.INFO)
 						graphics.setColor(Color.yellow);
 					if(msgLevel == MsgLevel.STANDARD)
 						graphics.setColor(Color.white);
 					
 					// draw the message
 					graphics.drawString(currentMsg.getMessage(), posX + TIMESTAMP_OFFSET + MESSAGE_OFFSETX, totalY - ROW_HEIGHT*(messages.size()-i) - MESSAGE_OFFSETY);
 				}
 				graphics.setColor(Color.white);
 			}
 		}
     }
 	
 	/**
 	 * Clear the consoles messages
 	 */
 	public void clearMessages(){
 		messages = new ArrayList<ConsoleMessage>();
 	}
 	
 	/**
 	 * Return a list of the messages
 	 * @return The messages
 	 */
 	public ArrayList<ConsoleMessage> getMessages(){
 		return messages;
 	}
 	
 	/**
 	 * Set the visibility status of the console window
 	 * @param visible
 	 */
 	public void setVisible(boolean visible){
 		this.visible = visible;
 	}
 
 	/**
 	 * Set if the console should hide after a certain amount of time
 	 * @param timerHide
 	 */
 	public void setTimerHide(boolean timerHide) {
 	    this.timerHide = timerHide;
     }
 
 	/**
 	 * Set if the console should be active. The console will not hide if active.
 	 * @param active
 	 */
 	public void setActive(boolean active) {
 	    this.active = active;
 	    timeSinceLastActive = 0;
 	    if(active)
 	    	setVisible(true);
     }
 }
