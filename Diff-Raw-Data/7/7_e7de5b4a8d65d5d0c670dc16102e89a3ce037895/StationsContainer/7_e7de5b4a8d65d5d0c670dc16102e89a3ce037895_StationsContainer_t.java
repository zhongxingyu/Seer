 package ru.slavabulgakov.busesspb.Network;
 
 import com.google.android.gms.maps.model.LatLng;
 
 import java.util.ArrayList;
 
 import ru.slavabulgakov.busesspb.model.TransportKind;
 import ru.slavabulgakov.busesspb.paths.Point;
 import ru.slavabulgakov.busesspb.paths.Station;
 
 public class StationsContainer extends LoaderContainer {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	public StationsContainer() {
 		super("http://futbix.ru/busesspb/v1_0/stationsdata/", "stationsNames.txt", "v2_stationsNames.ser");
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void handler(Object obj) {
 		super.handler(obj);
 		
 		ArrayList<String> strings = null;
 		if (obj.getClass() == String.class) {
 			String[] s = ((String)obj).split("\n");
 			strings = new ArrayList<String>();
 			for (int i = 0; i < s.length; i++) {
 				strings.add(s[i]);
 			}
 		} else {
 			strings = (ArrayList<String>)obj;
 		}
 		
 		ArrayList<Object> data = new ArrayList<Object>();
 		strings.remove(0);
 		for (String line : strings) {
 //			17609,17609,"ПР. АВИАКОНСТРУКТОРОВ, 38",60.027851,30.222640,0,2,bus
 			String items[] = line.split(",");
            int stationNameIndex = 5;
            if (items.length - stationNameIndex < 0) {
                continue;
            }
 			Station station = new Station();
 			station.id = items[0];
 			int length = 0;
			for (int i = items.length - stationNameIndex; i < items.length; i++) {
 				length += items[i].length() + 1;
 			}
 			station.name = line.substring(items[0].length() + 1 + items[1].length() + 1, line.length() - length);
 			double lat = Double.parseDouble(items[items.length - 5]);
 			double lng = Double.parseDouble(items[items.length - 4]);
 			station.point = new Point(new LatLng(lat, lng));
 			
 			String kind = items[items.length - 1];
 			if (kind.equals("bus")) {
 				station.kind = TransportKind.Bus;
 			} else if (kind.equals("tram")) {
 				station.kind = TransportKind.Tram;
 			} else if (kind.equals("trolley")) {
 				station.kind = TransportKind.Trolley;
 			} else if (kind.equals("ship")) {
 				station.kind = TransportKind.Ship;
 			} else {
 				station.kind = TransportKind.None;
 			}
 			
 			data.add(station);
 		}
 		_data = data;
 	}
 }
