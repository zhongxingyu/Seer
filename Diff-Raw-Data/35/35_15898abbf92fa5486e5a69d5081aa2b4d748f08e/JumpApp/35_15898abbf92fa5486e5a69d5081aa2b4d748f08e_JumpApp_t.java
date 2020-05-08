 /* Source Safe Version information
  * $Header$
  * $Modtime: 12/19/01 6:58p $
  * $NoKeywords: $
  */
  
 package waba.sys;
 import palmos.*;
 import waba.ui.MainWindow;
 import waba.ui.PenEvent;
 import waba.ui.KeyEvent;
 import waba.ui.IKeys;
 
 /**
  * JumpApp is the main for WabaJump applications.<br>
  *
  * The main Jump program. The start() method is called and this will loop throught
  * the Palm Palm.EvtGetEvent, getting events and calling handleMainWinEvent. This also
  * handles any startup and shutdown of the application.
  *
  * The method handleMainWinEvent handles the Palm Window event loop. It examines the
  * events, and then calls the _postEvent method in the Window class to displatch the
  * events to the various controls.
  *
  * @author Pete Carroll
  * @version 11/08/01 RCM - Added timestamp to all events, like Waba controls should have<br>
  * 10/18/01 RCM - Modified the Event.keyDownEvent case of handleMainWinEvent to better handle Palm
  * keys, especially the menu key.
  * Initial WabaJump Beta 4 version
  */
 
 public class JumpApp
 {
 
 	private static final int pageUpChr = 0x000B;
 	private static final int pageDownChr = 0x000C;
	private static final int returnChr = 0x000A;
 	private static final int escapeChr = 0x001B;
 	private static final int backspaceChr = 0x0008;
 	private static final int tabChr = 0x0009;
 	private static final int menuChr = 0x0105;
 	private static final int commandChr = 0x0106;
 	private static final int leftArrowChr = 0x001C;
 	private static final int rightArrowChr = 0x001D;
 	private static final int upArrowChr = 0x001E;
 	private static final int downArrowChr = 0x001F;
 	private static final int keyboardAlphaChr = 0x0110;
 	private static final int keyboardNumericChr = 0x0111;
 	private static final int keyboardChr = 0x0109;
 	private static final int hard1Chr = 0x0204;
 	private static final int hard2Chr = 0x0205;
 	private static final int hard3Chr = 0x0206;
 	private static final int hard4Chr = 0x0207;
 	private static final int calcChr = 0x010B;
 	private static final int findChr = 0x010A;
 	private static final int hardContrastChr = 0x020B;
 
 	private MainWindow mainWindow;
 	  
 	private static boolean   allowKeyboardPopup = false;
 
 	public JumpApp(MainWindow mainWindow)
 	{
     	this.mainWindow=mainWindow;
 	}
 
 	public void handleMainWinEvent(Event e)
 	{
     	switch (e.eType)
 			{
         	case Event.penUpEvent:
         	case Event.penDownEvent:
         	case Event.penMoveEvent:
 				{
 					int type;
 
 					if (e.eType == Event.penDownEvent)
 						type = PenEvent.PEN_DOWN; // PenEvent.PEN_DOWN
 					else if (e.eType == Event.penUpEvent)
 						type = PenEvent.PEN_UP; // PenEvent.PEN_UP
 					else
 						type = PenEvent.PEN_MOVE; // PenEvent.PEN_MOVE
 					mainWindow._postEvent(type, 0, e.screenX, e.screenY, 0, Vm.getTimeStamp());
 					break;
 				}
         	case Event.keyDownEvent:
 				{
 					int type = 100; // KeyEvent.KEY_PRESS
 					short chr = e.data1;
 					int key = 0;
 					switch (chr)
 						{
 							// NOTE: these should go somewhere:
 							//    nextFieldChr
 							//    prevFieldChr
 							//    linefeedChr
 						case pageUpChr:    key = IKeys.PAGE_UP; break; // PAGE_UP
 						case pageDownChr:  key = IKeys.PAGE_DOWN; break; // PAGE_DOWN
 							//        case :             key = 75002; break; // HOME
 							//        case :             key = 75003; break; // END
 						case upArrowChr:   key = IKeys.UP; break; // UP
 						case downArrowChr: key = IKeys.DOWN; break; // DOWN
 						case leftArrowChr: key = IKeys.LEFT; break; // LEFT
 						case rightArrowChr:key = IKeys.RIGHT; break; // RIGHT
 							//        case :             key = 75008; break; // INSERT
 						case returnChr:    key = IKeys.ENTER; break; // ENTER
 						case tabChr:       key = IKeys.TAB; break; // TAB
 						case backspaceChr: key = IKeys.BACKSPACE; break; // BACKSPACE
 						case escapeChr:    key = IKeys.ESCAPE; break; // ESCAPE
 							//        case :             key = 75013; break; // DELETE
 						case menuChr:      key = IKeys.MENU; break; // MENU
 						case commandChr:   key = IKeys.COMMAND; break; // COMMAND
 							//the following keycodes borrowed from SuperWaba
 						case keyboardAlphaChr: key = IKeys.KEYBOARD_ABC; break;
 						case keyboardNumericChr: key = IKeys.KEYBOARD_123; break;
 						case keyboardChr: key = IKeys.KEYBOARD; break;
 						case hard1Chr: key = IKeys.HARD1; break;
 						case hard2Chr: key = IKeys.HARD2; break;
 						case hard3Chr: key = IKeys.HARD3; break;
 						case hard4Chr: key = IKeys.HARD4; break;
 						case calcChr: key = IKeys.CALC; break;
 						case findChr: key = IKeys.FIND; break;
 						case hardContrastChr: key = IKeys.CONTRAST; break; // guich@121: take care of the contrast button bug
 						} // switch
 					if (key==0)
 						{
 							if (chr <= 255)
 								{
 									key = chr;
 								} 
 						}
 
 					mainWindow._postEvent(type,key,0,0,0,Vm.getTimeStamp());
 					break;
 				}
 			}
 	}
 
 	public void timerCheck()
 	{
     	int now = Vm.getTimeStamp();
     	int diff = now - mainWindow._timerStart;
     	if (diff < 0)
 			diff += (1L << 30); // max stamp is (1 << 30)
     	if (diff < mainWindow._timerInterval)
 			return;
     	mainWindow._onTimerTick();
 	}
 
 	public int calcEventTimeout()
 	{
     	int timeout;
 
     	if (mainWindow._timerInterval <= 0)
     		timeout = -1;
     	else
 			{
 				int now=Vm.getTimeStamp();
 				int diff=now-mainWindow._timerStart;
 
 				if (diff < 0)
 					diff += (1L << 30); // max stamp is (1 << 30)
 				timeout = mainWindow._timerInterval - diff;
 				if (timeout <= 0)
 					timeout = 0;
 				timeout = Vm.millisToTicks(timeout);
 				if (timeout <= 0)
 					timeout = 1;
 			}
     	return timeout;
 	}
 
 	public void start()
 	{
 		waba.fx.Graphics.saveState();
     	int iChr;
     	boolean flgDoSysHandleEvent;
     	mainWindow.onStart();   // call the onStart for the main window
     	mainWindow._doPaint(0,0,160,160); // repaint the area
     	Event e = new Event();
     	ShortHolder err = new ShortHolder((short) 0);
     	/* this is our main window loop. It gets events and processes them
 		   until we get an appStopEvent */
 	whileLoop: while (e.eType != e.appStopEvent) {
 		Palm.EvtGetEvent(e, calcEventTimeout());
 
 		/* stop keyboard event unless in edit field to prevent a fatal exception
 		   and don't allow Find key to be received */
 		flgDoSysHandleEvent=true;
 		if(!allowKeyboardPopup && e.eType==Event.keyDownEvent){
 			iChr=e.data1;
 			// if it is the popup keyboard character or find, don't handle it.
 			if(iChr==keyboardChr || iChr==keyboardAlphaChr || iChr==keyboardNumericChr
 			   || iChr==findChr){
 				flgDoSysHandleEvent=false;
 			}
 		}
 
 		if(flgDoSysHandleEvent){
 			if (Palm.SysHandleEvent(e))
 				continue whileLoop;
 			if (Palm.MenuHandleEvent(0, e, err))
 				continue whileLoop;
 		}
 		handleMainWinEvent (e);
 		timerCheck();
 	}
     	mainWindow.onExit();
 		waba.fx.Graphics.restoreState();
     	if (err.value==0)
 			return;
 	}
   
     /**
 	 * Set to allow the virtual keyboard to popup. Only do this if you are an input and 
 	 * have the focus
 	 *
 	 */
     public static void allowKeyboardPopup() {
 		allowKeyboardPopup = true;
     }
  
     /**
 	 * Set to NOT allow the virtual keyboard to popup. Will crash the system unless
 	 * you have the input
 	 *
 	 */
     public static void notAllowKeyboardPopup() {
 		allowKeyboardPopup = false;
     }
 }
