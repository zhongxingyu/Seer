 package core;
 
 import gui.MenuIcon;
 import gui.PasswordDialog;
 import gui.TrayMenu;
 
 import java.io.IOException;
 
 import providers.Provider;
 import providers.implementations.StuBruProvider;
 import util.AbstractModel;
 import ch.rolandschaer.ascrblr.scrobbler.AudioscrobblerService;
 import ch.rolandschaer.ascrblr.scrobbler.TrackInfo;
 import ch.rolandschaer.ascrblr.util.ServiceException;
 
 /**
  * Main class for scrobbling VRT radio station NOA tracks. Run the main method
  * to start the application.
  * 
  * @author Bart Mesuere
  * 
  */
 public class RadioScrobbler extends AbstractModel {
 
 	// interval for checking the now playing info
 	private final static int INTERVAL = 10000;
 
 	// GUI elements
 	private final MenuIcon menuIcon;
 	private final TrayMenu menu;
 
 	// is the scrobbler running?
 	private boolean running = false;
 
 	// credentials
 	private String username;
 	private String password;
 
 	// the current content provider
 	private Provider provider;
 
 	// the track currently playing
 	private TrackInfo laatsteTI;
 	private String laatsteA;
 	private String laatsteT;
 
 	/**
 	 * RadioScrobbler constructor
 	 */
 	public RadioScrobbler() {
 		// create the popup menu
 		menu = new TrayMenu(this);
 		addChangeListener(menu);
 
 		// create the tray icon and attach the menu
 		menuIcon = new MenuIcon();
 		menuIcon.setPopupMenu(menu);
 		addChangeListener(menuIcon);
 
 		// add the default provider
 		// TODO: get this from settings
 		provider = new StuBruProvider();
 
 		// ask for the username and password
 		PasswordDialog pwd = new PasswordDialog();
 		if (pwd.getResult()) {
 			username = pwd.getUsername();
 			password = pwd.getPassword();
 		} else
 			// exit when user presses cancel
 			System.exit(0);
 
 		// start scrobbling
 		go();
 	}
 
 	/**
 	 * Start scrobbling you should run this in a new thread
 	 */
 	private void go() {
 		// if already running, don't do anything
 		if (running)
 			return;
 
 		// let the listeners know we started
 		running = true;
 		fireStateChanged();
 
 		try {
 			// perform handshake
 			// TODO: catch BADAUTH error
 			AudioscrobblerService service = new AudioscrobblerService();
 			service.setCredentials(username, password);
 
 			// reset the last played artist
 			laatsteA = "";
 			laatsteT = "";
 			laatsteTI = null;
 
 			// keep on running
 			while (running) {
 				// get the currently playing song
 				String[] temp = provider.getNOA();
 
 				// if we've got a new track
 				if (!temp[0].equals(laatsteA) && !temp[1].equals(laatsteT)) {
 					// submit the old track
 					if (laatsteTI != null) {
 						service.submit(laatsteTI);
 					}
 
 					// save the new track
 					laatsteA = temp[0];
 					laatsteT = temp[1];
 
 					// if the new track is an actual track: save it
 					if (!laatsteT.equals("")) {
 						// create a new TrackInfo only when there's info
 						laatsteTI = new TrackInfo(
 								laatsteA,
 								laatsteT,
 								// no overlapping tracks
 								System.currentTimeMillis() - INTERVAL,
 								ch.rolandschaer.ascrblr.scrobbler.TrackInfo.SourceType.R);
 						fireStateChanged();
 					} else {
 						laatsteTI = null;
 					}
 				} else {
 					// keep submitting the now playing
 					if (laatsteTI != null) {
 						service.notifyNew(laatsteTI);
 					}
 				}
 
 				// sleep a while
 				Thread.sleep(INTERVAL);
 			}
 			// TODO do something with these exceptions
 		} catch (ServiceException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * just create the a new radioscrobbler object
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new RadioScrobbler();
 	}
 
 	/**
 	 * returns the TrackInfo object of the track currently playing
 	 * 
 	 * @return the current track
 	 */
 	public TrackInfo getCurrentTrackInfo() {
 		return laatsteTI;
 	}
 
 	/**
 	 * returns a string containing the track currently playing 'artist - track'
 	 * of "Geen muziekinfo" when no song is playing.
 	 * 
 	 * @return the current track
 	 */
 	public String getCurrentTrack() {
 		if (laatsteTI == null)
 			return "Geen muziekinfo";
 		else
 			return laatsteA + " - " + laatsteT;
 	}
 
 	/**
 	 * sets a new NOA provider. It's used on the next fetch when the scrobbler
 	 * is running
 	 * 
 	 * @param p
 	 *            the new provider
 	 */
 	public void setProvider(Provider p) {
 		provider = p;
 	}
 
 	/**
 	 * returns the current NOA provider
 	 * 
 	 * @return the current provider
 	 */
 	public Provider getProvider() {
 		return provider;
 	}
 
 	/**
 	 * returns true when the scrobbler is running, returns false when the
 	 * scrobbler is paused
 	 * 
 	 * @return running information
 	 */
 	public boolean isRunning() {
 		return running;
 	}
 
 	/**
 	 * Starts or pauses the scrobbler and notifies all listeners of this event.
 	 * When starting the scrobbler, run it in a new thread.
 	 * 
 	 * @param state
 	 *            true starts the scrobbler, false pauses it
 	 */
 	public void setRunning(boolean state) {
 		if (state) {// start
 			(new Thread() {
 				public void run() {
 					go();
 				}
 			}).start();
 		} else {// pause
 			running = false;
 			fireStateChanged();
 		}
 	}
 }
