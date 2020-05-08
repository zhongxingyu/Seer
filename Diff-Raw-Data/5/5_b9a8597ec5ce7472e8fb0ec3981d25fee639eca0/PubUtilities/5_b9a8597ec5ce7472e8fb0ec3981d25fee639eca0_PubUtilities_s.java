 package se.chalmers.krogkollen.pub;
 
import com.google.android.gms.maps.model.LatLng;

 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * A singleton holding a list of all the pubs, and is used to load data from the server.
  *
  * For now this class only contains hardcoded values, since we don't have support for a server.
  *
  * @author Albin Garpetun
  * Created 2013-09-22
  */
 public class PubUtilities {
 
     private List<IPub> pubList = new LinkedList<IPub>();
     private static PubUtilities instance = null;
 
     protected PubUtilities() {
         // Exists only to defeat instantiation.
     }
 
     /**
      * Creates an instance of this object if there is none, otherwise it simply returns the old one.
      *
      * @return The instance of the object.
      */
     public static PubUtilities getInstance() {
         if(instance == null) {
             instance = new PubUtilities();
         }
         return instance;
     }
 
     /**
      * Loads the pubs from the server and puts them in the list of pubs.
      *
      * For now this method only adds hardcoded pubs into the list.
      */
     public void loadPubList() {
        pubList.add(new Pub("Hubben 2.0", "IT's pub", "22:00-05:00", 18, 0, 0, new LatLng(57.688221, 11.979539), 1));
     }
 
     /**
      * Returns the list of pubs.
      *
      * @return The list of pubs.
      */
     public List<IPub> getPubList() {
         return pubList;
     }
 
 }
