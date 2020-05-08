 package gui;
 
 
 import java.awt.Dimension;
 
 import sound.NotePlayerChannel;
 
 import com.leapmotion.leap.Controller;
 
 
 public class ContentPanel{
 	private BackgroundPanel _background;
 	private Dimension _dimension;
 	private SplashScreen _splashScreen;
 	private MenuScreen _menuScreen;
 	private SettingsScreen _settingsScreen;
 	private ThereminScreen _thereminScreen;
 	private StringsScreen _stringsScreen;
 	private LibraryScreen _libraryScreen;
 	private Screen _currentDisplay, _oldDisplay;
 	private NotePlayerChannel _noteChannel;
 	
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
 		this.switchDisplay(ScreenType.SPLASH);
 		_noteChannel = new NotePlayerChannel();	
 
 	}
 	
 	public void switchDisplay(ScreenType screen){
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

 			_background.repaint();
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
