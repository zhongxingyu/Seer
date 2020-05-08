 /*
  TSAFE Prototype: A decision support tool for air traffic controllers
  Copyright (C) 2003  Gregory D. Dennis
 
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package tsafe.server.parser.asdi;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 /**
  * This class parses the NAS messages from the ASDI feed
  *
  * see http://www.metsci.com/cdm/products/asdidocs/ASDI_Desc4_0Word2000.doc
  * for a description of the ASDI feed
  * *** NOTE: The description seems to either have an error or be dated with respect to the FZ message.
  * *** The FZ message does not currently include the requested altitude as the description states.
  */
 class Message {
 
     // NAS FIELD NUMBERS
     public static final int MESSAGE_TYPE       = 1;
     public static final int FLIGHT_ID          = 2;
     public static final int AIRCRAFT_DATA      = 3;
     public static final int SPEED              = 5;
     public static final int COORDINATION_FIX   = 6;
     public static final int COORDINATION_TIME  = 7;
     public static final int ASSIGNED_ALTITUDE  = 8;
     public static final int REQUESTED_ALTITUDE = 9;
     public static final int ROUTE_DATA         = 10;
     public static final int REMARKS	           = 11;
     public static final int FIELD_REFERENCE    = 12;
     public static final int AMENDMENT_DATA     = 17;
     public static final int TRACK_POSITION     = 23;
     public static final int DEPARTURE          = 26;
     public static final int DESTINATION        = 27;
     public static final int ARRIVAL_TIME       = 28;
     
     // Initializes field names and field numbers used by each message
     private static String[] fieldNames = initFieldNames();
     private static Map fieldsUsedByMessage = initFieldsUsedByMessage();
     
     private static String[] initFieldNames() {
         String[] fieldNames = new String[ARRIVAL_TIME + 1];
         fieldNames[MESSAGE_TYPE]       = "MessageType";
         fieldNames[FLIGHT_ID]          = "FlightId";
         fieldNames[AIRCRAFT_DATA]      = "AircraftData";
         fieldNames[SPEED]              = "Speed";
         fieldNames[COORDINATION_FIX]   = "CoordinationFix";
         fieldNames[COORDINATION_TIME]  = "CoordinationTime";
         fieldNames[ASSIGNED_ALTITUDE]  = "AssignedAltitude";
         fieldNames[REQUESTED_ALTITUDE] = "RequestedAltitude";
         fieldNames[ROUTE_DATA]         = "RouteData";
         fieldNames[REMARKS]            = "Remarks";
         fieldNames[FIELD_REFERENCE]    = "FieldReference";
         fieldNames[AMENDMENT_DATA]     = "AmendmentData";
         fieldNames[TRACK_POSITION]     = "TrackPosition";
         fieldNames[DEPARTURE]          = "Departure";
         fieldNames[DESTINATION]        = "Destination";
         fieldNames[ARRIVAL_TIME]       = "ArrivalTime";
         return fieldNames;
     }
     
     private static Map initFieldsUsedByMessage() {
         Map msgType2Fields = new HashMap();
         
         // AF message
         int vAF[] = {MESSAGE_TYPE, FLIGHT_ID, DEPARTURE, DESTINATION, FIELD_REFERENCE, AMENDMENT_DATA};
         // AZ message
         int vAZ[] = {MESSAGE_TYPE, FLIGHT_ID, DEPARTURE, DESTINATION, ARRIVAL_TIME};
         // DZ message
         int vDZ[] = {MESSAGE_TYPE, FLIGHT_ID, AIRCRAFT_DATA, DEPARTURE, COORDINATION_TIME, DESTINATION, ARRIVAL_TIME};
         // FZ message (note does not include requested altitude as description states)
         int vFZ[] = {MESSAGE_TYPE, FLIGHT_ID, AIRCRAFT_DATA, SPEED, COORDINATION_FIX, COORDINATION_TIME, ASSIGNED_ALTITUDE, ROUTE_DATA};
         // RZ message
         int vRZ[] = {MESSAGE_TYPE, FLIGHT_ID, DEPARTURE, DESTINATION};
         // TZ message
         int vTZ[] = {MESSAGE_TYPE, FLIGHT_ID, SPEED, ASSIGNED_ALTITUDE, TRACK_POSITION};
         // UZ message
         int vUZ[] = {MESSAGE_TYPE, FLIGHT_ID, AIRCRAFT_DATA, SPEED, COORDINATION_FIX, COORDINATION_TIME, ASSIGNED_ALTITUDE, ROUTE_DATA};
         // RT message
         int vRT[] = {MESSAGE_TYPE};
         // TO message - non-NAS message, NOT YET IMPLEMENTED
         int vTO[] = {MESSAGE_TYPE};
         // HB message - non_NAS message
         int vHB[] = {MESSAGE_TYPE};
                 
         // Map message types to array of field numbers
         msgType2Fields.put("AF", vAF);
         msgType2Fields.put("AZ", vAZ);
         msgType2Fields.put("DZ", vDZ);
         msgType2Fields.put("FZ", vFZ);
         msgType2Fields.put("RZ", vRZ);
         msgType2Fields.put("TZ", vTZ);
         msgType2Fields.put("UZ", vUZ);
         msgType2Fields.put("RT", vRT);
         msgType2Fields.put("TO", vTO);
         msgType2Fields.put("HB", vHB);
         
         return msgType2Fields;
     }
 
     // Data present in every message
     private int sequenceNumber;
     private long time;
     private String original, facilityCode, messageType;
     private String fields[] = new String[fieldNames.length];
 
     // must provide the year and the month for the message
     // because that is not in the message's date-time stamp
     public Message(String st, int year, int month)
     {
         this.original = st;
     
         /**
         * Each ASDI message consists of five components. 
          * 1. Sequence Number (4 bytes) 
          * 2. Date-time stamp (8 bytes) 
          * 3. Facility identifier (4 bytes) 
          * 4. NAS or ETMS message (variable) 
          * 5. Line feed (1 byte)
          **/
          
         // Extract the sequence number
         this.sequenceNumber = Integer.parseInt(st.substring(0,4),16);
 
         // Extract the time of the message from the date-time stamp: ddhhmmss
         int day    = Integer.parseInt(st.substring(4,6));
         int hour   = Integer.parseInt(st.substring(6,8));
         int minute = Integer.parseInt(st.substring(8,10));
         int second = Integer.parseInt(st.substring(10,12));
         
         java.util.Calendar cal = java.util.Calendar.getInstance();
         cal.clear();
         cal.set(year, month, day, hour, minute, second);
         this.time = cal.getTime().getTime();
 
         // Extract the facility code
         this.facilityCode = st.substring(12,16);
 
         // Remove the header information already parsed
         st = st.substring(16);
         
         // Extract the message type
         this.messageType = st.substring(0, 2);
 
         // Extract the fields
         StringTokenizer stringTok = new StringTokenizer(st);
         int vAux[] = (int[])fieldsUsedByMessage.get(messageType);
         for (int i = 0; i < vAux.length; i++) {
             fields[vAux[i]] = stringTok.hasMoreTokens() ? stringTok.nextToken() : null;
         }
     }
 
     /** Return the original string */
     public String getOriginalString() {
         return this.original;
     }
 
     /** Return the time of the messages */
     public long getTime() {
         return this.time;
     }
 
     /** Return the faciltiy code */
     public String getFacilityCode() {
         return this.facilityCode;
     }
 
     /** Return the message type */
     public String getType() {
         return this.messageType;
     }
 
     /** Return the value of the given field */
     public String getField(int field) {
         return this.fields[field];
     }
 
     /**
      * Converts message to a String representation
      */
     public String toString()
     {
         String st = "START OF MESSAGE/n";
         st += "Sequence number: 0x" + Integer.toHexString(sequenceNumber) + "/n";
         java.text.DateFormat formatter = new java.text.SimpleDateFormat("HH:mm:ss EEE dd MMM yyyy");
         st += "Time: " + formatter.format(new Date(time)) + "/n";
         st += "Facility code: " + facilityCode + "/n";
 
         int vAux[]=(int[]) fieldsUsedByMessage.get(messageType);
         for (int i=0; i < vAux.length; i++)
             st += "Field " +vAux[i] + " (" + fieldNames[vAux[i]] + ") = " + fields[vAux[i]] + "/n";
 
         st += "END OF MESSAGE/n";
         return st;
     }
 }
