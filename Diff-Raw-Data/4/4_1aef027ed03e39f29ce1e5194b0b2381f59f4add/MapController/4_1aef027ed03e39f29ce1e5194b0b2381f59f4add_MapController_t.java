 package de.dhbw.swe.camping_site_mgt.gui_mgt.map;
 
 import java.util.HashMap;
 
 import javax.swing.JComponent;
 
 import de.dhbw.swe.camping_site_mgt.gui_mgt.Displayable;
 import de.dhbw.swe.camping_site_mgt.place_mgt.*;
 
 public class MapController implements Displayable {
     public MapController(final String mapPath) {
 	final HashMap<String, Area> areas = new MapAreas().getAreas();
 	final HashMap<Integer, PitchInterface> pitches = new HashMap<>();
 	final Site dev278 = new Site(278, "Electircity and Water", "Deliverypoint",
 		"0-24", "Deliverypoint");
 	pitches.put(1, new Pitch(1, "In the west!\nJust one direkt neighbour!",
		dev278, "A", 100, "gras", Pitch_Type.CAMPERPITCH, 100,
		"[7, 22, 40, 25]", "[1354, 1337, 1351, 1370]"));
 	view = new Map(mapPath, areas, pitches);
     }
 
     @Override
     public JComponent getGuiSnippet() {
 	return view;
     }
 
     /** The view. */
     private final JComponent view;
 }
