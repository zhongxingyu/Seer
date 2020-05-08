 /*
  * Copyright (c) 2007 Zauber S.A.  -- All rights reserved
  */
 package ar.com.zauber.commons.gis.street.model.results;
 
 import ar.com.zauber.commons.gis.Result;
 
 import com.vividsolutions.jts.geom.Point;
 
 /**
  * Representa el punto intermedio de una calle
  * @author Christian Nardi
  * @since Oct 10, 2007
  */
 public class StreetResult implements Result {
     private final String name;
     private final Point point;
     private final String city;
     private final String countryCode;
     /**
      * Creates the StreetResult.
      *
      * @param name
      * @param point
      * @param city
      * @param countryCode
      */
     public StreetResult(String name, Point point, String city,
             String countryCode) {
         super();
         this.name = name;
         this.point = point;
         this.city = city;
         this.countryCode = countryCode;
     }
         /**
      * Returns the name.
      * 
      * @return <code>String</code> with the name.
      */
     public final String getName() {
         return name;
     }
     /**
      * Returns the point.
      * 
      * @return <code>Point</code> with the point.
      */
     public final Point getPoint() {
         return point;
     }
     /**
      * Returns the city.
      * 
      * @return <code>String</code> with the city.
      */
     public final String getCity() {
         return city;
     }
     /* (non-Javadoc)
      * @see ar.com.zauber.commons.gis.Result#getDescription()
      */
     public String getDescription() {
        return "Calle " + name + " en " + city;
     }
     /**
      * Returns the countryCode.
      * 
      * @return <code>String</code> with the countryCode.
      */
     public final String getCountryCode() {
         return countryCode;
     }
     
 }
