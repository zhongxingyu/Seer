 package controller;
 
 import java.util.Calendar;
 import java.util.Timer;
 
 import model.Deck;
 import model.Flashcard;
 import model.WindowTitleDataStore;
 import view.FlashcardGui;
 
 public class InterruptionDecision {
 
 	DeckHandler deckHan = new DeckHandler();
 	// change this to private from public
 	public static Thread windowDataThread = new Thread(new FocusWindowName());
 	Timer timer = new Timer();
 	FocusWindowName focusWName;
 	boolean threadWaiting = false;
 	SuperMemo2 superMemo = new SuperMemo2();
 	private static WindowTitleDataStore windowTitleData = new WindowTitleDataStore();
 	String windowTitle;
	Thread keyboardListener = new Thread(new KeyboardListener());
	Thread mouseListener = new Thread(new MouseListener());
 
 	public static void main(String[] args) {
 
 	}
 
 	/**
 	 * Sets the minimum time before a new interrupt occurs.
 	 */
 	public void startThread() {
 		windowDataThread.start();
 		keyboardListener.start();
 		mouseListener.start();
 	}
 
 	public void resumeThread() {
 		FocusWindowName.resumeThread();
 		//KeyboardListener.resumeThread();
 		MouseListener.resumeThread();
 	}
 
 	public void pauseThread() {
 		FocusWindowName.suspendThread();
 		//KeyboardListener.suspendThead();
 		MouseListener.resumeThread();
 	}
 
 	public void stopThread() {
 		FocusWindowName.stopThread();
 		//KeyboardListener.stopThread();
 		MouseListener.stopThread();
 	}
 
 	/**
 	 * 
 	 * @param windowTitle
 	 */
 	public void updateWindowTitle(String windowTitle) {
 		this.windowTitle = windowTitle;
 		interruptNow();
 	}
 
 	public InterruptionDecision() {
 	}
 
 	public void userFlashcardResponse(int flashcardId, int rank) {
 		superMemo.increaseCount(flashcardId);
 		superMemo.ScoreCardAndCalculateInterval(flashcardId, rank);
 	}
 
 	public boolean checkWindowTitle(String windowTitle) {
 		return windowTitleData.checkWindowTitle(windowTitle);
 	}
 
 	public void addWindowTitle(String windowTitle) {
 		windowTitleData.addWindowTitle(windowTitle);
 	}
 
 	/**
 	 * public void readWindowDataStore() {
 	 * windowTitleData.readWindowTitleDataStore(); }
 	 * 
 	 * public void writeWindowDataStore() {
 	 * windowTitleData.writeWindowTitleDataStore(); }
 	 **/
 	public Integer getWindowDataStoreSize() {
 		return windowTitleData.getWindowDataSize();
 	}
 
 	public String getWindowTitle() {
 		System.out.println("getWindowTitle = " + windowTitle);
 		return windowTitle;
 	}
 
 	public boolean getKeyPressed() {
 		Calendar cal = Calendar.getInstance();
 		System.out.println(cal.getTimeInMillis() - KeyboardListener.getKeyPressed() > 1000 * 3);
 		if(cal.getTimeInMillis() - KeyboardListener.getKeyPressed() > 1000 * 3)
 		{
 			return true;
 		} else {
 			return false;
 		}	
 	}
 	/**
 	 * 
 	 * if no mouse clicks occur for 3 seconds return true. 
 	 * if mouse click within the last 3 seconds return false
 	 * 
 	 * 
 	 * @return
 	 */
 	public boolean getLastMouseClick() {
 		Calendar cal = Calendar.getInstance();
 		MouseListener.getLastClick();
 		
 		if(cal.getTimeInMillis() - MouseListener.getLastClick() > 1000* 3) {
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 
 	/**
 	 * 
 	 * str1.toLowerCase().contains(str2.toLowerCase())`
 	 * 
 	 */
 	public void interruptNow() {
 		System.out.println(getWindowDataStoreSize());
 		System.out.println(windowTitle + ": Is in set = "
 				+ checkWindowTitle(windowTitle));
 		System.out.println("Window Title = " + windowTitle);
 		// System.out.println("leyboardNotInUse = " + keyboardNotInUse());
 		if (!checkWindowTitle(windowTitle) && getKeyPressed() && getLastMouseClick()) {
 			Flashcard flashcard = Deck.deckDatabase.get(2);
 			FlashcardGui fg = new FlashcardGui(flashcard.getReverseText(),
 					flashcard.getFrontText(), flashcard.getId(), windowTitle);
 			pauseThread();
 		}
 	}
 
 }
