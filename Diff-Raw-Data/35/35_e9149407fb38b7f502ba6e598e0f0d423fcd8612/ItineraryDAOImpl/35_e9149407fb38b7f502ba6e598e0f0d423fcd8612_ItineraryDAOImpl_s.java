 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ws.travelgood.domain;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import ws.travelgood.types.Itinerary;
 
 /**
  *
  * @author mkucharek
  */
 public class ItineraryDAOImpl implements ItineraryDAO {
 
     protected List<Itinerary> itineraryList;
     protected int nextId;
     // internal usage only
     private final int ITINERARY_NOT_FOUND = -1;
 
     public ItineraryDAOImpl() {
         this.itineraryList = new ArrayList<Itinerary>();
         this.nextId = 1;
 
     }
 
     public ItineraryDAOImpl(List<Itinerary> initialItineraryList) {
         this();
 
         if (initialItineraryList != null) {
 
             for (Itinerary it : initialItineraryList) {
                 this.createItinerary(it, nextId++);
             }
 
             this.nextId = this.itineraryList.size() + 1;
 
         }
     }
 
     public List<Itinerary> getAllItineraries() {
         return Collections.unmodifiableList(this.itineraryList);
 
     }
 
     public Itinerary getItinerary(Integer id) {
 
         for (Itinerary it : this.itineraryList) {
             if (it.getId().equals(id)) {
                 return new Itinerary(it);
             }
         }
 
         return null;
 
     }
 
     public List<Itinerary> getUserItineraries(String userId) {
 
         List<Itinerary> retList = new ArrayList<Itinerary>();
         for (Itinerary it : this.itineraryList) {
             if (it.getUserId().equals(userId)) {
                 retList.add(it);
             }
         }
 
         return retList;
 
     }
 
     public Itinerary createItinerary(Itinerary itinerary) {
         return this.createItinerary(itinerary, nextId++);
 
     }
 
     public boolean addHotel(Integer itineraryId, String hotelBookingNumber) {
 
         Itinerary it = this.getItinerary(itineraryId);
 
         if (it == null) {
             return false;
         }
 
         if (it.getHotelBooking(hotelBookingNumber) != null) {
             // hotel already added
             return false;
         }
 
         it.addHotel(hotelBookingNumber);
 
         return this.updateItinerary(it);
 
     }
 
     public boolean deleteHotel(Integer itineraryId, String hotelBookingNumber) {
 
         Itinerary it = this.getItinerary(itineraryId);
 
         if (it == null) {
             return false;
         }
 
         it.deleteHotel(hotelBookingNumber);
 
         return this.updateItinerary(it);
     }
 
     public boolean bookItinerary(Integer itineraryId) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean cancelItinerary(Integer itineraryId) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
    protected boolean updateItinerary(Itinerary itinerary) {
 
        int index = this.getItineraryIndex(itinerary);
 
         if (index != ITINERARY_NOT_FOUND) {
 
            this.itineraryList.set(index, itinerary);
             return true;
 


         } else {
             // item must have been deleted - cannot update
             return false;
 
         }
     }
 
     protected boolean deleteItinerary(Integer id) {
         return this.deleteItinerary(this.getItinerary(id));
 
     }
 
     protected boolean deleteItinerary(Itinerary itinerary) {
         if (itinerary.getId() == null || itinerary.getId().compareTo(nextId) >=
                 0) {
             // invalid itinerary passed
 
             return false;
 
         } else {
             // delete
 
             int index = this.getItineraryIndex(itinerary);
 
             if (index != ITINERARY_NOT_FOUND) {
 
                 this.itineraryList.remove(index);
                 return true;
 
 
 
             } else {
                 // item must have been deleted - cannot delete
                 return false;
             }
 
         }
     }
 
     protected Itinerary createItinerary(Itinerary it, int id) {
         it.setId(Integer.valueOf(id));
         this.itineraryList.add(it);
 
         return it;
 
     }
 
     protected int getItineraryIndex(Itinerary it) {
 
         for (int i = 0; i < this.itineraryList.size(); ++i) {
             if (this.itineraryList.get(i).getId().equals(it.getId())) {
                 return i;
 
             }
         }
 
         // not found
         return ITINERARY_NOT_FOUND;
 
     }
 }
