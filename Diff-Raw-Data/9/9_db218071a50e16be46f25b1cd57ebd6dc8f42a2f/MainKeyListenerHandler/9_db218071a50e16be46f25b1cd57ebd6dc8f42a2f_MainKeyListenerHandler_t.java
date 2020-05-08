 package handlers;
 
 import handleds.Actor;
 import handleds.Handled;
 
 import java.util.ArrayList;
 
 import listeners.KeyListener;
 
 /**
  * This class unites the actor and keyListening interfaces so that keyevents are 
  * only called once in a step.
  *
  * @author Gandalf.
  *         Created 2.12.2012.
  */
 public class MainKeyListenerHandler extends LogicalHandler implements Actor
 {
 	// ATTRIBUTES	------------------------------------------------------
 	
 	private ArrayList<Integer> keysDown;
 	private ArrayList<Integer> codesDown;
 	private ArrayList<Integer> keysPressed;
 	private ArrayList<Integer> codesPressed;
 	private ArrayList<Integer> keysReleased;
 	private ArrayList<Integer> codesReleased;
 	
 	
 	// CONSTRUCTOR	------------------------------------------------------
 	
 	/**
 	 * Simply creates a new KeyListenerHandler. Keylistenerhandler does not 
 	 * die automatically so it must be killed with the kill method. Also, 
 	 * listeners must be added manually later.
 	 * 
 	 * @param actorhandler The handler that will handle this handler (optional)
 	 */
 	public MainKeyListenerHandler(ActorHandler actorhandler)
 	{
 		super(false, actorhandler);
 		
 		// Initializes the attributes
 		this.keysDown = new ArrayList<Integer>();
 		this.codesDown = new ArrayList<Integer>();
 		this.keysPressed = new ArrayList<Integer>();
 		this.keysReleased = new ArrayList<Integer>();
 		this.codesPressed = new ArrayList<Integer>();
 		this.codesReleased = new ArrayList<Integer>();
 	}
 	
 	
 	// IMPLEMENTED METHODS	----------------------------------------------
 
 	@Override
 	public void act()
 	{
 		// Informs all listeners of the last changes
 		for (int i = 0; i < getHandledNumber(); i++)
 		{
 			KeyListener listener = getListener(i);
 			
 			// Informs if a key was pressed
 			for (int ik = 0; ik < this.keysPressed.size(); ik++)
 			{
 				listener.onKeyPressed(this.keysPressed.get(ik), 0, false);
				//System.out.println("Pressed! (" + listener.getClass().getName() + ")");
 			}
 			
 			// Informs if a coded key was pressed
 			for (int ik = 0; ik < this.codesPressed.size(); ik++)
 			{
 				// TODO: This throws nullpointers every now and then
 				if (listener != null && this.codesPressed != null)
 					listener.onKeyPressed(0, this.codesPressed.get(ik), true);
 			}
 			
 			// Informs if a key was released
 			for (int ik = 0; ik < this.keysReleased.size(); ik++)
 			{
 				listener.onKeyReleased(this.keysReleased.get(ik), 0, false);
 				//System.out.println("Released!");
 			}
 			
 			// Informs if a coded key was released
 			for (int ik = 0; ik < this.codesReleased.size(); ik++)
 			{
 				listener.onKeyReleased(0, this.codesReleased.get(ik), true);
 				//System.out.println("CodeReleased!");
 			}
 			
 			// Informs if a key is down
 			for (int ikd = 0; ikd < this.keysDown.size(); ikd++)
 			{
 				listener.onKeyDown(this.keysDown.get(ikd), 0, false);
 			}
 			
 			// Informs if a coded key is down
 			for (int icd = 0; icd < this.codesDown.size(); icd++)
 			{
 				// For some reason, this check is needed...?
 				if (icd < this.codesDown.size())
 					listener.onKeyDown(0, this.codesDown.get(icd), true);
 			}
 		}
 		
 		// Negates some of the changes (pressed & released)
 		this.keysPressed.clear();
 		this.keysReleased.clear();
 		this.codesPressed.clear();
 		this.codesReleased.clear();
 	}
 	
 	@Override
 	protected Class<?> getSupportedClass()
 	{
 		return KeyListener.class;
 	}
 	
 	
 	// OTHER METHODS	--------------------------------------------------
 	
 	/**
 	 * 
 	 * This method should be called at each keyPressed -event
 	 *
 	 * @param key The key that was pressed
 	 * @param code The key's keycode
 	 * @param coded Does the key use it's keycode
 	 */
 	public void onKeyPressed(int key, int code, boolean coded)
 	{
 		if (coded)
 		{
 			// Checks whether the key was just pressed instead of being already down
 			if (!this.codesDown.contains(code))
 			{
 				// If so, marks the key as pressed
 				if (!this.codesPressed.contains(code))
 					this.codesPressed.add(code);
 			
 				// Sets the key down
 				this.codesDown.add(code);
 			}
 		}
 		else
 		{
 			if (!this.keysDown.contains(key))
 			{
 				if (!this.keysPressed.contains(key))
 					this.keysPressed.add(key);
 				
 				this.keysDown.add(key);
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * This method should be called at each keyReleased -event
 	 *
 	 * @param key The key that was pressed
 	 * @param code The key's keycode
 	 * @param coded Does the key use it's keycode
 	 */
 	public void onKeyReleased(int key, int code, boolean coded)
 	{
 		if (coded)
 		{
 			// Marks the key as released
 			if (!this.codesReleased.contains(code))
 				this.codesReleased.add(code);
 			
 			// Sets the key up (= not down)
 			if(this.codesDown.contains(code))
 				this.codesDown.remove(this.codesDown.indexOf(code));
 		}
 		else
 		{
 			if (!this.keysReleased.contains(key))
 				this.keysReleased.add(key);
 			
 			this.keysDown.remove(this.keysDown.indexOf(key));
 		}
 	}
 	
 	/**
 	 * Adds a new keylistener to the informed keylistners
 	 *
 	 * @param k The KeyListener to be added
 	 */
 	public void addListener(KeyListener k)
 	{
 		super.addHandled(k);
 	}
 	
 	private KeyListener getListener(int index)
 	{
 		Handled maybeListener = getHandled(index);
 		if (maybeListener instanceof KeyListener)
 			return (KeyListener) maybeListener;
 		else
 			return null;
 	}
 }
