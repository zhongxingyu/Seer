 package io.github.axxiss.places.model;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Axxiss
  */
 public class Place implements Parcelable {
     public List<Event> events = new ArrayList<Event>();
 
     private String formatted_address = null;
 
     private String icon = null;
 
     private String id = null;
 
     private String name = null;
 
     private double rating;
 
     private String reference = null;
 
     private List<String> types = new ArrayList<String>();
 
     private Geometry geometry;
 
     private String vicinity = null;
 
     private OpeningHours opening_hours;
 
     private List<Photo> photos = new ArrayList<Photo>();
 
     private int price_level;
 
     private String formatted_phone_number = null;
 
     private String international_phone_number = null;
 
     private int utc_offset;
 
     /**
      * Place's phone number in its local format. For example, the formatted_phone_number for
      * Google's Sydney, Australia office is (02) 9374 4000.
      *
      * @return
      */
     public String getFormattedPhoneNumber() {
         return formatted_phone_number;
     }
 
     /**
      * Place's phone number in international format. International format includes the country code,
      * and is prefixed with the plus (+) sign. For example, the formatted_phone_number for Google's
      * Sydney, Australia office is +61 2 9374 4000.
      *
      * @return
      */
     public String getInternationalPhoneNumber() {
         return international_phone_number;
     }
 
     /**
     * Returns the number of minutes this Place’s current timezone is offset from UTC. For example,
      * for Places in Sydney, Australia during daylight saving time this would be 660 (+11 hours from
      * UTC), and for Places in California outside of daylight saving time this would be -480 (-8
      * hours from UTC).
      *
      * @return
      */
     public int getUtcOffset() {
         return utc_offset;
     }
 
     /**
      * Returns a feature name of a nearby location. Often this feature refers to a street or
      * neighborhood within the given results. The vicinity property is only returned for a Nearby
      * Search.
      *
      * @return the vicinity.
      */
     public String getVicinity() {
         return vicinity;
     }
 
 
     /**
      * The price level of the Place, on a scale of 0 to 4. The exact amount indicated by a specific
      * value will vary from region to region. Price levels are interpreted as follows:
      *
      * @return
      */
     public int getPrice_level() {
         return price_level;
     }
 
     /**
      * Returns an array of {@link io.github.axxiss.places.model.Photo}s, each containing a reference
      * to an image. A Place Search will return at most one photo object. Performing a Place Details
      * request on the Place may return up to ten photos. More information about Place Photos and how
      * you can use the images in your application can be found in the Place Photos documentation.
      *
      * @return the photos.
      */
     public List<Photo> getPhotos() {
         return photos;
     }
 
     /**
      * Returns the time table of the place.
      *
      * @return the opening hours.
      */
     public OpeningHours getOpening_hours() {
         return opening_hours;
     }
 
     /**
      * Array or one or more {@link io.github.axxiss.places.model.Event} elements provide information
      * about current events happening at this Place. Up to three events are returned for each place,
      * ordered by start time. For information about events, please read Events in the Places API.
      *
      * @return place's events.
      */
     public List<Event> getEvents() {
         return events;
     }
 
     /**
      * Returns a string containing the human-readable address of this place. Often this address is
      * equivalent to the "postal address". The formatted_address property is only returned for a
      * Text Search.
      *
      * @return
      */
     public String getFormattedAddress() {
         return formatted_address;
     }
 
     /**
      * Returns the URL of a recommended icon which may be displayed to the user when indicating this
      * result.
      *
      * @return the url.
      */
     public String getIcon() {
         return icon;
     }
 
     /**
      * Returns a unique stable identifier denoting this place. This identifier may not be used to
      * retrieve information about this place, but is guaranteed to be valid across sessions. It can
      * be used to consolidate data about this Place, and to verify the identity of a Place across
      * separate searches.
      *
      * @return the id.
      */
     public String getId() {
         return id;
     }
 
     /**
      * Returns the human-readable name. For establishment results, this is usually the business
      * name.
      *
      * @return the name.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Returns the Place's rating, from 1.0 to 5.0, based on user reviews.
      *
      * @return
      */
     public double getRating() {
         return rating;
     }
 
     /**
      * Returns a unique token that you can use to retrieve additional information about this place
      * in a Place Details request. You can store this token and use it at any time in future to
      * refresh cached data about this Place, but the same token is not guaranteed to be returned for
      * any given Place across different searches.
      *
      * @return the reference token.
      */
     public String getReference() {
         return reference;
     }
 
     /**
      * Returns an array of feature types describing the given result.
      *
      * @return
      * @see io.github.axxiss.places.enums.PlaceType
      */
     public List<String> getTypes() {
         return types;
     }
 
     /**
      * Returns {@link io.github.axxiss.places.model.Geometry} information about the result,
      * generally including the location (geocode) of the Place and (optionally) the viewport
      * identifying its general area of coverage.
      *
      * @return the geometry
      */
     public Geometry getGeometry() {
         return geometry;
     }
 
     protected Place(Parcel in) {
         formatted_address = in.readString();
         icon = in.readString();
         id = in.readString();
         name = in.readString();
         rating = in.readDouble();
         reference = in.readString();
         geometry = in.readParcelable(Geometry.class.getClassLoader());
         vicinity = in.readString();
         opening_hours = in.readParcelable(OpeningHours.class.getClassLoader());
         price_level = in.readInt();
         formatted_phone_number = in.readString();
         international_phone_number = in.readString();
         utc_offset = in.readInt();
         in.readStringList(types);
         in.readTypedList(events, Event.CREATOR);
         in.readTypedList(photos, Photo.CREATOR);
     }
 
     public int describeContents() {
         return 0;
     }
 
     public void writeToParcel(Parcel dest, int flags) {
         dest.writeString(formatted_address);
         dest.writeString(icon);
         dest.writeString(id);
         dest.writeString(name);
         dest.writeDouble(rating);
         dest.writeString(reference);
         dest.writeParcelable(geometry, 0);
         dest.writeString(vicinity);
         dest.writeParcelable(opening_hours, 0);
         dest.writeInt(price_level);
         dest.writeString(formatted_phone_number);
         dest.writeString(international_phone_number);
         dest.writeInt(utc_offset);
         dest.writeStringList(types);
         dest.writeTypedList(events);
         dest.writeTypedList(photos);
     }
 
     public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
         public Place createFromParcel(Parcel in) {
             return new Place(in);
         }
 
         public Place[] newArray(int size) {
             return new Place[size];
         }
     };
 }
