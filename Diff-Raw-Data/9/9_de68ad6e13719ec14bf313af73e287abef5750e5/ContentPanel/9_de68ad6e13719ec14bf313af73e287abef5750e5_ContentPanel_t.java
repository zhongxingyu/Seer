 package gui;
 
 import java.awt.Dimension;
 import java.util.Stack;
 import sound.NotePlayerChannel;
 
 public class ContentPanel{
 	
 	private BackgroundPanel _background;
 	private Dimension _dimension;
 	private NotePlayerChannel _noteChannel;
 	
 	private Stack<ScreenType> _screenStack = new Stack<ScreenType>(); 
 	private Screen _currentDisplay, _oldDisplay;
 	private SplashScreen _splashScreen;
 	private MenuScreen _menuScreen;
 	private SettingsScreen _settingsScreen;
 	private ThereminScreen _thereminScreen;
 	private StringsScreen _stringsScreen;
 	private LibraryScreen _libraryScreen;
 	private InstrumentsScreen _instrumentsScreen;
 		
 	public ContentPanel(BackgroundPanel background,Dimension dimension){
 		_noteChannel = new NotePlayerChannel();		
 		_background = background;
 		_dimension = dimension;
 		_splashScreen = new SplashScreen(this,_dimension);
 		_menuScreen = new MenuScreen(this,_dimension);
 		_settingsScreen = new SettingsScreen(this,_dimension);
 		_thereminScreen = new ThereminScreen(this,_dimension);
 		_stringsScreen = new StringsScreen(this,_dimension);
 		_libraryScreen = new LibraryScreen(this,_dimension);
 		_instrumentsScreen = new InstrumentsScreen(this,_dimension);
 		this.pushScreen(ScreenType.SPLASH);
 	}
 	
 	/**
 	 * Adds a new screen on top of the current screen.
 	 * @param screen : the type of the new screen to be displayed
 	 */
 	public void pushScreen(ScreenType screen) {
 		_screenStack.push(screen);
 		switchDisplay(screen);
 	}
 	
 	/**
 	 * Removes the current screen and returns to the previous one.
 	 */
 	public void popScreen() {
 		if (_screenStack.size() > 1) {
 			_screenStack.pop();
 			switchDisplay(_screenStack.peek());
 		} else
 			System.out.println("ERROR: tried to pop last screen from screen stack");
 	}
 	
 	/**
 	 * Switches the currently displayed screen to the given ScreenType.
 	 * @param screen : new screen type to be displayed
 	 */
 	private void switchDisplay(ScreenType screen){
 		_oldDisplay = _currentDisplay;
 
 		switch(screen) {
 		case SPLASH:
 			_currentDisplay = _splashScreen;
 			break;
 		case MENU:
 			_currentDisplay = _menuScreen;
 			break;
 		case SETTINGS:
 			_currentDisplay = _settingsScreen;
 			break;
 		case THEREMIN:
 			_currentDisplay = _thereminScreen;
 			break;
 		case STRINGS:
 			_currentDisplay = _stringsScreen;
 			break;
 		case LIBRARY:
 			_currentDisplay = _libraryScreen;
 			break;
 		case INSTRUMENTS:
 			_currentDisplay = _instrumentsScreen;
 			break;
 		default:
 			break;
 		}
 		
 		if(_oldDisplay == null){
 			_background.add(_currentDisplay);
 			_currentDisplay.transitionIn();
 		} else {
 			_oldDisplay.transitionOut();
 			_currentDisplay.transitionIn();
 			_background.add(_currentDisplay);
 		}
 	}
 	
 	public void continueTransition() {
 		_background.remove(_oldDisplay);
 		_background.repaint();
 	}
 	
 	public NotePlayerChannel getNoteChannel() {
 		return _noteChannel;
 	}
 }
