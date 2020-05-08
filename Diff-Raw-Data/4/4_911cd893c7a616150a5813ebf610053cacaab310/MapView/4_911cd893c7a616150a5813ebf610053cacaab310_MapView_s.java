 package ikm.views;
 
 import ikm.ViewManager;
 import ikm.db.Base;
 
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 
 import com.nokia.maps.common.GeoCoordinate;
 import com.nokia.maps.map.MapCanvas;
 import com.nokia.maps.map.MapDisplayState;
 import com.nokia.maps.map.MapStandardMarker;
 
 public class MapView extends MapCanvas implements CommandListener {
 	private Command back = new Command("Back", Command.BACK, 1);
 	private ViewManager viewManager;
 	private Base base;
 	private MapDisplayState[] states;
 	
 	public MapView(Display display, ViewManager viewManager, Base base) {
 		super(display);
 		this.viewManager = viewManager;
 		this.base = base;
 		generateMarkers();
 		
 		addCommand(back);
 		setCommandListener(this);
 	}
 
 	public void onMapContentComplete() {
 	}
 
 	public void onMapUpdateError(String description, Throwable detail,
 			boolean critical) {
 	}
 
 	public void commandAction(Command c, Displayable d) {
 		if (c == back) {
 			viewManager.goBack();
 		}
 	}
 	
 	private void generateMarkers() {
 		states = new MapDisplayState[base.getSize()];
 		for (int i = 0; i < base.getSize(); i++) {
 			String lat = base.get(i, 3);
 			String lon = base.get(i, 4);
 			GeoCoordinate coord = new GeoCoordinate(Double.parseDouble(lat), Double.parseDouble(lon), 0);
 			states[i] = new MapDisplayState(coord, 10);
 			MapStandardMarker marker = getMapFactory().createStandardMarker(coord);
 			getMapDisplay().addMapObject(marker);
 		}
 	}
 	
 	public void centerOn(int idx) {
 		getMapDisplay().setState(states[idx]);
 	}
 }
