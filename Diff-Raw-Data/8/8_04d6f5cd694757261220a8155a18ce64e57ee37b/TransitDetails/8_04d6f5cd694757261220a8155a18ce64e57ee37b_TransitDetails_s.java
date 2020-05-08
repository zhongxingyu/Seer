 package com.HuskySoft.metrobike.backend;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 /**
  * Represents the transit details for a single [transit] Step.
  *
  * @author Adrian Laurenzi
  */
 public final class TransitDetails implements Serializable {
     /**
      * Part of serializability, this id tracks if a serialized object can be
      * deserialized using this version of the class.
      *
      * NOTE: Please add 1 to this number every time you change the readObject()
      * or writeObject() methods, so we don't have old-version Location objects
      * (ex: from the log) being made into new-version Location objects.
      */
     private static final long serialVersionUID = 0L;
 
     /**
      * The arrival transit stop location.
      */
     private Location arrivalStop;
 
     /**
      * The departure transit stop location.
      */
     private Location departureStop;
 
     /**
      * The transit arrival time (in 24-hour time).
      */
     private String arrivalTime;
 
     /**
      * The transit departure time (in 24-hour time).
      */
     private String departureTime;
 
     /**
      * The transit agency name.
      */
     private String agencyName;
 
     /**
      * An abbreviated transit route description, eg. one that might be displayed
      * on the sign of a city bus.
      */
     private String headsign;
 
     /**
      * Short version of transit route name.
      */
     private String lineShortName;
 
     /**
      * Transit vehicle type (i.e. bus).
      */
     private String vehicleType;
 
     /**
      * An icon to display for this item.
      */
     private String vehicleIconURL;
 
     /**
      * Number of stops to stay aboard transit.
      */
     private int numStops;
 
     /**
      * The amount to indent the toString call.
      */
     private int indent = 0;
 
     /**
      * The actual indented string.
      */
     private String indentString = "";
 
     /**
      * Constructs a new and empty TransitDetails object.
      */
     public TransitDetails() {
         arrivalStop = null;
         departureStop = null;
         arrivalTime = "";
         departureTime = "";
         agencyName = "";
         headsign = "";
         lineShortName = "";
         vehicleType = "";
         vehicleIconURL = "";
         numStops = 0;
     }
 
     /**
      * Getter for the arrivalStop field.
      *
      * @return the arrival stop
      */
     public Location getArrivalStop() {
         return arrivalStop;
     }
 
     /**
      * Getter for the departureStop field.
      *
      * @return the departure stop
      */
     public Location getDepartureStop() {
         return departureStop;
     }
 
     /**
      * Getter for the getArrivalTime field.
      *
      * @return the arrival time
      */
     public String getArrivalTime() {
         return arrivalTime;
     }
 
     /**
      * Getter for the departureTime field.
      *
      * @return the departure time
      */
     public String getDepartureTime() {
         return departureTime;
     }
 
     /**
      * Getter for the agencyName field.
      *
      * @return the agency name of the transit line
      */
     public String getAgencyName() {
         return agencyName;
     }
 
     /**
      * Getter for the headsign field.
      *
      * @return the headsign of the transit line
      */
     public String getHeadsign() {
         return headsign;
     }
 
     /**
      * Getter for the lineShortName field.
      *
      * @return the short name of the transit line
      */
     public String getLineShortName() {
         return lineShortName;
     }
 
     /**
      * Getter for the vehicleType field.
      *
      * @return the vehicle type (i.e. bus)
      */
     public String getVehicleType() {
         return vehicleType;
     }
 
     /**
      * Getter for the vehicleIconURL field.
      *
      * @return the vehicle icon URL
      */
     public String getVehicleIconURL() {
         return vehicleIconURL;
     }
 
     /**
      * Getter for the numStops field.
      *
      * @return the number of stops
      */
     public int getNumStops() {
         return numStops;
     }
 
     /**
      * Sets the arrival stop location.
      *
      * @param lat
      *            the latitude for the arrival stop Location
      * @param lng
      *            the longitude for the arrival stop Location
      * @return the modified TransitDetails, for Builder pattern purposes
      */
     public TransitDetails setArrivalStop(final double lat, final double lng) {
         this.arrivalStop = new Location(lat, lng);
         return this;
     }
 
     /**
      * Sets the departure stop location.
      *
      * @param lat
      *            the latitude for the departure stop Location
      * @param lng
      *            the longitude for the departure stop Location
      * @return the modified TransitDetails, for Builder pattern purposes
      */
     public TransitDetails setDepartureStop(final double lat, final double lng) {
         this.departureStop = new Location(lat, lng);
         return this;
     }
 
     /**
      * Sets the arrival time.
      *
      * @param newArrivalTime
      *            the transit arrival time
      * @return the modified TransitDetails, for Builder pattern purposes
      */
     public TransitDetails setArrivalTime(final String newArrivalTime) {
         this.arrivalTime = newArrivalTime;
         return this;
     }
 
     /**
      * Sets the departure time.
      *
      * @param newDepartureTime
      *            the transit departure time
      * @return the modified TransitDetails, for Builder pattern purposes
      */
     public TransitDetails setDepartureTime(final String newDepartureTime) {
         this.departureTime = newDepartureTime;
         return this;
     }
 
     /**
      * Sets the transit agency name.
      *
      * @param newAgencyName
      *            the agency name of transit line
      * @return the modified TransitDetails, for Builder pattern purposes
      */
     public TransitDetails setAgencyName(final String newAgencyName) {
         this.agencyName = newAgencyName;
         return this;
     }
 
     /**
      * Sets the transit line headsign.
      *
      * @param newHeadsign
      *            the headsign of transit line
      * @return the modified TransitDetails, for Builder pattern purposes
      */
     public TransitDetails setHeadsign(final String newHeadsign) {
         this.headsign = newHeadsign;
         return this;
     }
 
     /**
      * Sets the transit line short name.
      *
      * @param newLineShortName
      *            the short name of transit line
      * @return the modified TransitDetails, for Builder pattern purposes
      */
     public TransitDetails setLineShortName(final String newLineShortName) {
         this.lineShortName = newLineShortName;
         return this;
     }
 
     /**
      * Sets the transit vehicle type.
      *
      * @param newVehicleType
      *            the vehicle type of transit line
      * @return the modified TransitDetails, for Builder pattern purposes
      */
     public TransitDetails setVehicleType(final String newVehicleType) {
         this.vehicleType = newVehicleType;
         return this;
     }
 
     /**
      * Sets the transit vehicle icon URL.
      *
      * @param newVehicleIconURL
      *            the transit vehicle icon URL
      * @return the modified TransitDetails, for Builder pattern purposes
      */
     public TransitDetails setVehicleIconURL(final String newVehicleIconURL) {
         this.vehicleIconURL = newVehicleIconURL;
         return this;
     }
 
     /**
      * Sets the number of stops.
      *
      * @param newNumStops
      *            the number of stops
      * @return the modified TransitDetails, for Builder pattern purposes
      */
     public TransitDetails setNumStops(final int newNumStops) {
         this.numStops = newNumStops;
         return this;
     }
 
     /**
      * Setter for the indent field. Affects the amount of indentation used in
      * the toString() method.
      *
      * @param newIndent
      *            the new indent value.
      */
     public void setIndent(final int newIndent) {
         this.indent = newIndent;
         indentString = "";
         for (int i = 0; i < indent; i++) {
             indentString = Utility.getIndentString();
         }
     }
 
     /**
      * This is the getter for the indent field.
      *
      * @return the amount to indent.
      */
     public int getIndent() {
         return indent;
     }
 
     @Override
     public String toString() {
         StringBuilder transitDetailsString = new StringBuilder();
         String extraIndent = indentString + Utility.getIndentString();
         transitDetailsString.append(indentString + "Transit Details:\n");
         transitDetailsString.append(extraIndent + "Arrival Stop: "
         + arrivalStop.toString() + "\n");
         transitDetailsString.append(extraIndent + "Departure Stop: "
         + departureStop.toString() + "\n");
         transitDetailsString.append(extraIndent + "Agency Name: "
                 + agencyName + "\n");
         transitDetailsString.append(extraIndent + "Headsign: "
                 + headsign + "\n");
         transitDetailsString.append(extraIndent + "Line Short Name: "
                 + lineShortName + "\n");
         transitDetailsString.append(extraIndent + "Vehicle Type: "
                 + vehicleType + "\n");
         transitDetailsString.append(extraIndent + "Vehicle Icon URL: "
                 + vehicleIconURL + "\n");
         transitDetailsString.append(extraIndent + "Number of Stops: "
                 + numStops + "\n");
         return transitDetailsString.toString();
     }
 
     /**
      * Implements a custom serialization of a TransitDetails object.
      *
      * @param out
      *            the ObjectOutputStream to write to
      * @throws IOException
      *             if the stream fails
      */
     private void writeObject(final ObjectOutputStream out) throws IOException {
         // Write each field to the stream in a specific order.
         // Specifying this order helps shield the class from problems
         // in future versions.
         // The order must be the same as the read order in readObject()
         out.writeObject(arrivalStop);
         out.writeObject(departureStop);
         out.writeObject(agencyName);
         out.writeObject(headsign);
         out.writeObject(lineShortName);
         out.writeObject(vehicleType);
         out.writeObject(vehicleIconURL);
         out.writeInt(numStops);
     }
 
     /**
      * Implements a custom deserialization of a TransitDetails object.
      *
      * @param in
      *            the ObjectInputStream to read from
      * @throws IOException
      *             if the stream fails
      * @throws ClassNotFoundException
      *             if a class is not found
      */
     private void readObject(final ObjectInputStream in)
             throws IOException, ClassNotFoundException {
         // Read each field from the stream in a specific order.
         // Specifying this order helps shield the class from problems
         // in future versions.
         // The order must be the same as the writing order in writeObject()
        arrivalStop = (Location) in.readObject();
         departureStop = (Location) in.readObject();
         agencyName = (String) in.readObject();
         headsign = (String) in.readObject();
         lineShortName = (String) in.readObject();
         vehicleType = (String) in.readObject();
         vehicleIconURL = (String) in.readObject();
         numStops = in.readInt();
     }
 }
