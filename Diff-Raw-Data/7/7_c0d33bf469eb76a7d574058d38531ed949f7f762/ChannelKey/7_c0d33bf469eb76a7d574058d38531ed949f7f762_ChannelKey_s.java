 /*
  * Copyright 2012, United States Geological Survey or
  * third-party contributors as indicated by the @author tags.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/  >.
  *
  */
 package asl.metadata;
 
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 public class ChannelKey
 extends Key
 implements Comparable<ChannelKey>
 {
     private static final Logger logger = Logger.getLogger("asl.metadata.ChannelKey");
     private static final int CHANNEL_EPOCH_BLOCKETTE_NUMBER = 52;
 
     private String location = null;
     private String name = null;
 
     // constructor(s)
     public ChannelKey(Blockette blockette)
     throws WrongBlocketteException
     {
         if (blockette.getNumber() != CHANNEL_EPOCH_BLOCKETTE_NUMBER) {
             throw new WrongBlocketteException();
         }
         location = blockette.getFieldValue(3,0);
         name     = blockette.getFieldValue(4,0);
 
         setLocation(location);
         setChannel(name);
     }
 
     public ChannelKey(String location, String name)
     {
         setLocation(location);
         setChannel(name);
     }
     public ChannelKey(Channel channel)
     {
         this(channel.getLocation(), channel.getChannel());
     }
 
 
     private void setLocation(String location) {
 
         String validCodes = "\"--\", \"00\", \"10\", etc.";
 
     // Temp fix for station US_WMOK which has some channel blockettes tagged with location="HR"
         if (location.equals("HR")) {  // Add to this any unruly location code you want to flag ...
             location = "XX";
             logger.severe( String.format("ChannelKey.setLocation: Got location code=HR --> I'll set it to XX and continue parsing dataless") );
         }
 
         if (location == null || location.equals("") ){
             location = "--";
         }
         else {
             if (location.length() != 2) {
                 throw new RuntimeException( String.format("Error: Location code=[%s] is NOT a valid 2-char code (e.g., %s)", location, validCodes) );
             }
             Pattern pattern  = Pattern.compile("^[0-9][0-9]$");
             Matcher matcher  = pattern.matcher(location);
             if (!matcher.matches() && !location.equals("--") && !location.equals("XX") ) {
                 //throw new RuntimeException( String.format("Error: Location code=[%s] is NOT valid (e.g., %s)", location, validCodes) );
             }
         }
         this.location = location;
 
     }
 
     private void setChannel(String channel) {
         if (channel == null) {
             throw new RuntimeException("channel cannot be null");
         }
     // MTH: For now we'll allow either 3-char ("LHZ") or 4-char ("LHND") channels
         if (channel.length() < 3 || channel.length() > 4) { 
             throw new RuntimeException( String.format("Error: Channel code=[%s] is NOT valid (must be 3 or 4-chars long)", channel) );
         }
         this.name = channel;
     }
 
     public Channel toChannel() {
         return new Channel( this.getLocation(), this.getName() );
     }
 
     // identifiers
     public String getLocation()
     {
         return new String(location);
     }
 
     public String getName()
     {
         return new String(name);
     }
 
     // overrides abstract method from Key class
     public String toString()
     {
         return new String(location+ "-" +name);
     }
 
 // Use to Sort ChannelKeys from ---BC0, ---BC1, ..., 10-VHZ, ... 50-LWS, 60-HDF
     @Override public int compareTo( ChannelKey chanKey ) {
       String thisCombo = getLocation() + getName();
       String thatCombo = chanKey.getLocation() + chanKey.getName();
       return thisCombo.compareTo(thatCombo);
    }
 
 
 }
 
