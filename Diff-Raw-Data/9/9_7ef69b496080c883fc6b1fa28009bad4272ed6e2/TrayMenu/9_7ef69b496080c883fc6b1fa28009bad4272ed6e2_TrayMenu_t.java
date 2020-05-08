 package gui;
 
 import java.awt.CheckboxMenuItem;
 import java.awt.Menu;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import providers.Provider;
 import core.RadioScrobbler;
 
 /**
  * menu to control the RadioScrobbler
  * 
  * @author Bart Mesuere
  * 
  */
 public class TrayMenu extends PopupMenu implements ChangeListener {
 
 	// the scrobbler it controls
 	private final RadioScrobbler rs;
 
 	// some menu items
 	private final MenuItem nowPlaying;
 	private final CheckboxMenuItem scrobbleItem;
 	private final MenuItem quitButton;
 
 	public TrayMenu(RadioScrobbler rs) {
 		super();
 		this.rs = rs;
 
 		// now playing button
 		nowPlaying = new MenuItem("Now Playing");
 		nowPlaying.setEnabled(false);
 
 		// scrobbleItem
 		scrobbleItem = new CheckboxMenuItem("Scrobble");
 		scrobbleItem.setState(true);
 
 		// quitbutton
 		quitButton = new MenuItem("Quit");
 
 		init();
 	}
 
 	private void init() {
 		add(nowPlaying);
 		addSeparator();
 
 		add(scrobbleItem);
 		scrobbleItem.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				rs.setRunning(scrobbleItem.getState());
 			}
 		});
 
 		Menu m = new Menu("Change station");
 		for (final Provider p : Provider.getProviders()) {
 			MenuItem mi = new MenuItem(p.getName());
 			mi.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					rs.setProvider(p);
 				}
 			});
 			m.add(mi);
 		}
 		add(m);
 
 		addSeparator();
 		add(quitButton);
 		quitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		RadioScrobbler rs = (RadioScrobbler) e.getSource();
 		if (rs.isRunning()) {
 			nowPlaying.setLabel(rs.getProvider().getName() + ": "
 					+ rs.getCurrentTrack());
 		} else {
 			nowPlaying.setLabel("Paused");
			if (scrobbleItem.getState())
				scrobbleItem.setState(false);
 		}
 	}
 
 }
