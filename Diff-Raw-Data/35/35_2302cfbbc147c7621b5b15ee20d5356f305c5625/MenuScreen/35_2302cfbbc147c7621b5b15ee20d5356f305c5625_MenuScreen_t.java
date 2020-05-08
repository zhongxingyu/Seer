 package main.java.com.owfg.facade.bb.StoreManagement.gui;
 
 import main.java.com.owfg.facade.bb.StoreManagement.app.MyApp;
 
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.container.MainScreen;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 
 /**
  * A class extending the MainScreen class, which provides default standard
  * behavior for BlackBerry GUI applications.
  */
 public final class MenuScreen extends MainScreen {
 	public MenuScreen() {
 		super(DEFAULT_MENU | DEFAULT_CLOSE);
 
 		LabelField title = new LabelField("Barcode Scanner",
 				LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
 		// Set the displayed title of the screen
 		setTitle(title);
 		setChangeListener(null);
 		VerticalFieldManager vfm = new VerticalFieldManager(USE_ALL_WIDTH);
 		FieldChangeListener buttonListener = new ButtonListener();
 
 		Field scanButton = new ButtonField("Scan", FIELD_HCENTER
 				| ButtonField.CONSUME_CLICK | USE_ALL_WIDTH);
 		scanButton.setChangeListener(buttonListener);
 		vfm.add(scanButton);
 
 		Field historyButton = new ButtonField("History", FIELD_HCENTER
 				| ButtonField.CONSUME_CLICK);
 		historyButton.setChangeListener(buttonListener);
 		vfm.add(historyButton);
 
 		Field helpButton = new ButtonField("Help", FIELD_HCENTER
 				| ButtonField.CONSUME_CLICK);
 		helpButton.setChangeListener(buttonListener);
 		vfm.add(helpButton);
 
 		Field quitButton = new ButtonField("Quit", FIELD_HCENTER
 				| ButtonField.CONSUME_CLICK);
 		quitButton.setChangeListener(buttonListener);
 		vfm.add(quitButton);
 
 		vfm.setChangeListener(null);
 		add(vfm);
 
 		MyApp.app = (MyApp) UiApplication.getUiApplication();
 	}
 
 	/**
 	 * Listens for selected buttons and starts the required screen.
 	 */
 	final class ButtonListener implements FieldChangeListener {
 		public void fieldChanged(Field field, int context) {
 			switch (field.getIndex()) {
 			case 0: // camera
 				MyApp.app.pushScreen(MyApp.cScreen);
 				//MyApp.cScreen.startThread();
 				break;
 
 			case 1: // history
 				MyApp.app.pushScreen(new HistoryScreen());
 				break;
 
 			case 2: // help
 				//MyApp.app.pushScreen(new ResultScreen());
 				break;
 
 			case 3: // quit
 				if (Dialog.ask(Dialog.D_YES_NO, "Exit?") == 4) {
 					System.exit(0);
 				}
 				break;
 			}
 		}
 	}
 	public boolean onSavePrompt() {
 	    return true;
 	}
 	
 	
 	public void makeMenu(Menu menu, int instance) {
 		menu.add(WeatherExample);
 	}
 	
     public MenuItem WeatherExample = new MenuItem("WebService Example",100, 1) {
 		public void run() {
 			//MyApp.app.popScreen(getScreen());
 			MyApp.app.pushScreen(new WebServiceScreen());
 		}
 	};  
 }
