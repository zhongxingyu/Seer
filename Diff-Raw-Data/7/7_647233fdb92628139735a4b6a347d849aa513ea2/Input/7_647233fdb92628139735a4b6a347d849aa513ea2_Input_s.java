 package grige;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 
 import com.jogamp.newt.opengl.GLWindow;
 
 import com.jogamp.newt.event.KeyListener;
 import com.jogamp.newt.event.MouseListener;
 import com.jogamp.newt.event.KeyEvent;
 import com.jogamp.newt.event.MouseEvent;
 
 import de.lessvoid.nifty.NiftyInputConsumer;
 
 import de.lessvoid.nifty.spi.input.InputSystem;
 
 import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;
 
 import de.lessvoid.nifty.tools.resourceloader.NiftyResourceLoader;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 public final class Input implements KeyListener, MouseListener, InputSystem
 {
 	
 	private static HashMap<Short, Boolean> nextInput; //Input currently being executed
 	private static HashMap<Short, Boolean> currentInput; //Input from the latest frame
 	private static HashMap<Short, Boolean> previousInput; //Input from the previous frame
 	private static LinkedList<KeyEvent> keyEventQueue; //Key Event Queue for updating Nifty with what keys were pressed each frame
 	
 	private static boolean[] nextMouseButtons;
 	private static boolean[] currentMouseButtons;
 	private static boolean[] previousMouseButtons;
 	
 	private static int nextMouseWheel;
 	private static int currentMouseWheel;
 	
 	private static Vector2I mouseLoc;
 	
 	private static int screenHeight;
 	private static GLWindow window;
 	
 	private static AwtToNiftyKeyCodeConverter converter;
 	
 	private static Input instance;
 	static Input getInstance()
 	{
 		return instance;
 	}
 	
 	private Input(){}
 	
 	static void initialize(GLWindow screen)
 	{
 		instance = new Input();
 		
 		window = screen;
 		mouseLoc = new Vector2I(0,0);
 		screenHeight = window.getHeight();
 		converter = new AwtToNiftyKeyCodeConverter();
 		
 		//Create the input storage structures
 		currentInput = new HashMap<Short, Boolean>();
 		previousInput = new HashMap<Short, Boolean>();
 		nextInput = new HashMap<Short, Boolean>();
 		keyEventQueue = new LinkedList<KeyEvent>();
 		
 		nextMouseButtons = new boolean[MouseEvent.BUTTON_NUMBER];
 		currentMouseButtons = new boolean[MouseEvent.BUTTON_NUMBER];
 		previousMouseButtons = new boolean[MouseEvent.BUTTON_NUMBER];
 		
 		nextMouseWheel = 0;
 		currentMouseWheel = 0;
 		
 		//Populate each hashmap with all the virtual key fields from KeyEvent via reflection
 		try
 		{
 			Field[] keyEventFields = KeyEvent.class.getFields();
 			
 			for(Field keyField : keyEventFields)
 			{
 				int modifier = keyField.getModifiers();
 				
 				if(Modifier.isStatic(modifier) && Modifier.isPublic(modifier) && Modifier.isFinal(modifier))
 					if(keyField.getType().equals(short.class) && keyField.getName().startsWith("VK_"))
 					{
 						short keyValue = keyField.getShort(null); //We use null here because its a static field
 						nextInput.put(keyValue, false);
 						currentInput.put(keyValue, false);
 						previousInput.put(keyValue, false);
 					}
 			}
 		}
 		catch(IllegalAccessException iae)
 		{
 		}
 	}
 	
 	public static boolean getKey(short keySymbol)
 	{
 		return currentInput.get(keySymbol);
 	}
 	
 	public static boolean getKeyDown(short keySymbol)
 	{
 		return currentInput.get(keySymbol) && !previousInput.get(keySymbol);
 	}
 	
 	public static boolean getKeyUp(short keySymbol)
 	{
 		return !currentInput.get(keySymbol) && previousInput.get(keySymbol);
 	}
 	
 	public static int getMouseX() { return mouseLoc.x; }
 	public static int getMouseY() { return mouseLoc.y; }
 	
 	public static boolean getMouseButton(int buttonID)
 	{
 		return currentMouseButtons[buttonID];
 	}
 	
 	public static boolean getMouseButtonDown(int buttonID)
 	{
 		return currentMouseButtons[buttonID] && !previousMouseButtons[buttonID];
 	}
 	
 	public static boolean getMouseButtonUp(int buttonID)
 	{
 		return !currentMouseButtons[buttonID] && previousMouseButtons[buttonID];
 	}
 	
 	
 	public static int getMouseWheel()
 	{
 		return currentMouseWheel;
 	}
 	
 	protected static void consumeKeyDown(short keySymbol)
 	{
 		previousInput.put(keySymbol, true);
 	}
 	protected static void consumeKeyUp(short keySymbol)
 	{
 		currentInput.put(keySymbol, true);
 	}
 	protected static void consumeMouseButtonDown(int buttonID)
 	{
 		previousMouseButtons[buttonID] = true;
 	}
 	protected static void consumeMouseButtonUp(int buttonID)
 	{
 		previousMouseButtons[buttonID] = false;
 	}
 	protected static void consumeMouseWheel()
 	{
 		currentMouseWheel = 0;
 	}
 	
 	protected static void update()
 	{
 		//Keyboard input
 		for(short key : currentInput.keySet())
 		{
 			previousInput.put(key, currentInput.get(key));
 			currentInput.put(key, nextInput.get(key));
 		}	
 		
 		//Mouse button input
 		for(int i=0; i<MouseEvent.BUTTON_NUMBER; i++)
 		{
 			previousMouseButtons[i] = currentMouseButtons[i];
 			currentMouseButtons[i] = nextMouseButtons[i];
 		}
 		
 		//Mouse wheel input
 		currentMouseWheel = nextMouseWheel;
 		nextMouseWheel = 0;
 	}
 	
 	@Override
 	public void setMousePosition(int x, int y)
 	{
 		Log.info("SET MOUSE LOC TO: "+x+";"+y);
 		window.warpPointer(x, Input.screenHeight - y); //Transform the origin from bottom-left to top-left
 	}
 	
 	@Override
 	public void forwardEvents(NiftyInputConsumer inputConsumer)
 	{
 		//Mouse Events
 		//Mouse Position
 		inputConsumer.processMouseEvent(mouseLoc.x, mouseLoc.y, 0, -1, false);
 		
 		//Mouse Buttons
 		for(int i=0; i<currentMouseButtons.length; i++)
 		{
 			if(getMouseButtonDown(i))
 			{
 				if(inputConsumer.processMouseEvent(mouseLoc.x, mouseLoc.y, 0, i, true))
 				{	consumeMouseButtonDown(i); }
 			}
 			
 			else if(getMouseButtonUp(i))
 			{
 				if(inputConsumer.processMouseEvent(mouseLoc.x, mouseLoc.y, 0, i, false))
 				{	consumeMouseButtonUp(i); }
 			}
 		}
 		
 		//Keyboard events, for this we need the event queue because we need more than just the keyCode or Symbol, we need the entire event
 		while(!keyEventQueue.isEmpty())
 		{
 			KeyEvent kEvt = keyEventQueue.pollLast();
 			boolean pressed = (kEvt.getEventType() == KeyEvent.EVENT_KEY_PRESSED);
 			KeyboardInputEvent newEvent = new KeyboardInputEvent(converter.convertToNiftyKeyCode(kEvt.getKeyCode(), 0), kEvt.getKeyChar(), pressed, kEvt.isShiftDown(), kEvt.isControlDown());
 			
 			if(inputConsumer.processKeyboardEvent(newEvent))
 			{
 				if(pressed)
 					consumeKeyDown(kEvt.getKeyCode());
 				else
 					consumeKeyUp(kEvt.getKeyCode());
 			}
 		}
 	}
 	
 	@Override
 	public void setResourceLoader(NiftyResourceLoader resourceLoader)
 	{
 	}
 	
 	public void keyPressed(KeyEvent evt)
 	{
 		keyEventQueue.push(evt);
		if(!evt.isAutoRepeat())
 			Input.nextInput.put(evt.getKeySymbol(), true);
 	}
 	
 	public void keyReleased(KeyEvent evt)
 	{
 		keyEventQueue.push(evt);
 		if(!evt.isAutoRepeat())
 			Input.nextInput.put(evt.getKeySymbol(), false);
 	}
 	
 	public void mouseWheelMoved(MouseEvent evt)
 	{
 		Input.nextMouseWheel += (int)evt.getRotation()[1];
 	}
 	
 	public void mousePressed(MouseEvent evt)
 	{
 		int button = evt.getButton() - 1; //We minus 1 because for JOGL left mouse is button 1. We want zero-based things dammit!
 		Input.nextMouseButtons[button] = true;
 	}
 	
 	public void mouseReleased(MouseEvent evt)
 	{
 		int button = evt.getButton() - 1;
 		Input.nextMouseButtons[button] = false;
 	}
 	
 	public void mouseMoved(MouseEvent evt)
 	{
 		Input.mouseLoc.x = evt.getX();
 		Input.mouseLoc.y = Input.screenHeight - evt.getY(); //Transform the origin form top-left to bottom-left
 	}
 	
 	public void mouseDragged(MouseEvent evt)
 	{
 		mouseMoved(evt); //We handle mouseDragged events the same way we handle mouseMoved events because for a PC dragged/moved are the same thign
 	}
 	
 	public void mouseClicked(MouseEvent evt){}
 	public void mouseEntered(MouseEvent evt){}
 	public void mouseExited(MouseEvent evt){}
 	
 }
