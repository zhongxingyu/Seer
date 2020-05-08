 package edu.depaul.x86azul.dataAccess;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 import edu.depaul.x86azul.geo.*;
 import com.javadocmd.simplelatlng.LatLng;
 import com.javadocmd.simplelatlng.window.CircularWindow;
 import com.javadocmd.simplelatlng.util.LengthUnit;
 
 /**
  * @author Youssuf ElKalay
  * 
  */
 public class DebrisDAO {
 
 	private DataPersister data = DataPersisterInMemory.getInstance();
 
 	public DebrisDAO() {
 
 	}
 
 	public Debris getDebris(String geoHash) {
 		if (data.find(geoHash)) {
 			return data.read(geoHash);
 		}
 
 		return null;
 	}
 
 	public boolean doesDebrisExist(String geoHash) {
 		if (!data.find(geoHash)) {
 			return false;
 		}
 		return true;
 	}
 
 	public boolean isDebrisInRange(Debris center, double radius) {
 		CircularWindow window = new CircularWindow(center.getPoint(), radius,
 				LengthUnit.KILOMETER);
 		Enumeration<Debris> e = data.readAllValues();
 
 		while (e.hasMoreElements()) {
 			if (window.contains(e.nextElement().getPoint())) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public boolean isDebrisInRange(LatLng center, double radius) {
 		CircularWindow window = new CircularWindow(center, radius,
 				LengthUnit.KILOMETER);
 		Enumeration<Debris> e = data.readAllValues();
 
 		while (e.hasMoreElements()) {
 			if (window.contains(e.nextElement().getPoint())) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public ArrayList<LatLng> getAllPointsInRange(Debris center, double radius) {
 		CircularWindow window = new CircularWindow(center.getPoint(), radius,
 				LengthUnit.KILOMETER);
 		Enumeration<Debris> e = data.readAllValues();
 		ArrayList<LatLng> pointList = new ArrayList<LatLng>();
 
 		while (e.hasMoreElements()) {
 			LatLng p = e.nextElement().getPoint();
 			if (window.contains(p)) {
 				pointList.add(p);
 			}
 
 		}
 
 		return pointList;
 	}
 
 	public ArrayList<LatLng> getAllPointsInRange(LatLng center, double radius) {
 		CircularWindow window = new CircularWindow(center, radius,
 				LengthUnit.KILOMETER);
 		Enumeration<Debris> e = data.readAllValues();
 		ArrayList<LatLng> pointList = new ArrayList<LatLng>();
 
 		while (e.hasMoreElements()) {
 			LatLng p = e.nextElement().getPoint();
 			if (window.contains(p)) {
 				pointList.add(p);
 			}
 
 		}
 
 		return pointList;
 	}
 
 	public ArrayList<LatLng> getAllPointsByUID(String uid) {
 		Enumeration<Debris> e = data.readAllValues();
 		ArrayList<LatLng> pointList = new ArrayList<LatLng>();
 		while (e.hasMoreElements()) {
			Debris debris = e.nextElement();
			String id = debris.getUid();
 			if (id.equals(uid)) {
				pointList.add(debris.getPoint());
 			}
 		}
 		return pointList;
 	}
 
 }
