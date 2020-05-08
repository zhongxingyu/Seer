 /* Copyright (c) 2011 Danish Maritime Authority
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 package dk.dma.ais.analysis.viewer.rest.json;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import dk.dma.ais.analysis.viewer.handler.TargetSourceData;
 import dk.dma.ais.data.AisClassAPosition;
 import dk.dma.ais.data.AisClassAStatic;
 import dk.dma.ais.data.AisClassBTarget;
 import dk.dma.ais.data.AisTargetDimensions;
 import dk.dma.ais.data.AisVesselPosition;
 import dk.dma.ais.data.AisVesselStatic;
 import dk.dma.ais.data.AisVesselTarget;
 import dk.dma.ais.data.IPastTrack;
 import dk.dma.ais.message.NavigationalStatus;
 
 public class VesselTargetDetails {    
     
     protected long id;
     protected long mmsi;
     protected String vesselClass;
     protected String lastReceived;
     protected long currentTime;
     protected String lat;
     protected String lon;
     protected String cog;
     protected boolean moored;
     protected String vesselType = "N/A";
     protected String length = "N/A";
     protected String width = "N/A";
     protected String sog;
     protected String name = "N/A";
     protected String callsign = "N/A";
     protected String imoNo = "N/A";
     protected String cargo = "N/A";
     protected String country;
     protected String draught = "N/A";
     protected String heading = "N/A";
     protected String rot = "N/A";
     protected String destination = "N/A";
     protected String navStatus = "N/A";
     protected String eta = "N/A";
     protected String posAcc = "N/A";
     protected String sourceType;
     protected String sourceSystem;
     protected String sourceRegion;
     protected String sourceBs;
     protected String sourceCountry;
     protected String pos;
     protected IPastTrack pastTrack;
 
     public VesselTargetDetails(AisVesselTarget target, TargetSourceData sourceData, int anonId, IPastTrack pastTrack) {        
         AisVesselPosition pos = target.getVesselPosition();
         if (pos == null || pos.getPos() == null) return;
         AisClassAPosition classAPos = null;
         if (pos instanceof AisClassAPosition) {
             classAPos = (AisClassAPosition)pos;
         }
         
         this.pastTrack = pastTrack;
                         
         this.currentTime = System.currentTimeMillis();
         this.id = anonId;
         this.mmsi = target.getMmsi();
         this.vesselClass = (target instanceof AisClassBTarget) ? "B" : "A";
         this.lastReceived = formatTime(currentTime - target.getLastReport().getTime());
         this.lat = latToPrintable(pos.getPos().getLatitude());
         this.lon = lonToPrintable(pos.getPos().getLongitude());
         this.cog = formatDouble(pos.getCog(), 0);        
         this.heading = formatDouble(pos.getHeading(), 1);
         this.sog = formatDouble(pos.getSog(), 1);    
         
 
         if (target.getCountry() != null) {
             this.country = target.getCountry().getName();
         } else {
             this.country ="N/A";
         }
         
         
         this.sourceType = sourceData.getSourceType();
         
         this.sourceSystem = sourceData.getTagging().getSourceId();
         if (this.sourceSystem == null) {
             this.sourceSystem = "N/A";
         }
         this.sourceRegion = sourceData.getSourceRegion();
         if (this.sourceRegion == null) {
             this.sourceRegion = "N/A";
         }
         if (sourceData.getTagging().getSourceBs() != null) {
             this.sourceBs = Integer.toString(sourceData.getTagging().getSourceBs());
         } else {
             this.sourceBs = "N/A";
         }
         
         if (sourceData.getTagging().getSourceCountry() != null) {
             this.sourceCountry = sourceData.getTagging().getSourceCountry().getName();
         } else {
             this.sourceCountry = "N/A";
         }
         
         // Class A position
         if (classAPos != null) {
            NavigationalStatus navigationalStatus = NavigationalStatus.get(classAPos.getNavStatus());
             this.navStatus = navigationalStatus.prettyStatus();
             this.moored = (classAPos.getNavStatus() == 1 || classAPos.getNavStatus() == 5);
             this.rot = formatDouble(classAPos.getRot(), 1);
         }
                 
         if (pos.getPosAcc() == 1) {
             this.posAcc = "High";
         } else {
             this.posAcc = "Low";
         }
         
         this.pos = latToPrintable(pos.getPos().getLatitude()) + " - " + lonToPrintable(pos.getPos().getLongitude());
         
         
         AisVesselStatic statics = target.getVesselStatic();
         if (statics == null) return;
         AisClassAStatic classAStatics = null;
         if (statics instanceof AisClassAStatic) {
             classAStatics = (AisClassAStatic)statics;
         }
         
         AisTargetDimensions dim = statics.getDimensions();
         if (dim != null) {
             this.length = Integer.toString(dim.getDimBow() + dim.getDimStern());
             this.width = Integer.toString(dim.getDimPort() + dim.getDimStarboard());
         }
         if (statics.getShipTypeCargo() != null) {
             this.vesselType = statics.getShipTypeCargo().prettyType();
             this.cargo = statics.getShipTypeCargo().prettyCargo();
         }
         this.name = statics.getName();
         this.callsign = statics.getCallsign();        
         // Class A statics
         if (classAStatics != null) {
             if (classAStatics.getImoNo() != null) {
                 this.imoNo = Integer.toString(classAStatics.getImoNo()); 
             } else {
                 this.imoNo = "N/A";
             }                    
             this.destination = (classAStatics.getDestination() != null) ? classAStatics.getDestination() : "N/A";
             this.draught = (classAStatics.getDraught() != null) ? formatDouble((double)classAStatics.getDraught(), 1) : "N/A";
             this.eta = getISO8620(classAStatics.getEta());
         }        
 
     }
     
     public void anonymize() {
         this.name = "N/A";
         this.callsign = "N/A";
         this.imoNo = "N/A";
         this.destination = "N/A";
         this.mmsi = 0;
         this.eta = "N/A";
     }
     
     public static String getISO8620(Date date) {
         if (date == null) {
             return "N/A";
         }
         SimpleDateFormat iso8601gmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
         iso8601gmt.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
         return iso8601gmt.format(date);
     }
     
     public static String formatTime(Long time) {
         if (time == null) {
             return "N/A";
         }
         long secondInMillis = 1000;
         long minuteInMillis = secondInMillis * 60;
         long hourInMillis = minuteInMillis * 60;
         long dayInMillis = hourInMillis * 24;
 
         long elapsedDays = time / dayInMillis;
         time = time % dayInMillis;
         long elapsedHours = time / hourInMillis;
         time = time % hourInMillis;
         long elapsedMinutes = time / minuteInMillis;
         time = time % minuteInMillis;
         long elapsedSeconds = time / secondInMillis;
 
         if (elapsedDays > 0) {
             return String.format("%02d:%02d:%02d:%02d", elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
         } else if (elapsedHours > 0) {
             return String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
         } else {
             return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
         }
     }
     
     public static String latToPrintable(Double lat) {
         if (lat == null) {
             return "N/A";
         }
         String ns = "N";
         if (lat < 0) {
             ns = "S";
             lat *= -1;
         }
         int hours = (int)lat.doubleValue();
         lat -= hours;
         lat *= 60;
         String latStr = String.format(Locale.US, "%3.3f", lat);
         while (latStr.indexOf('.') < 2) {
             latStr = "0" + latStr;
         }        
         return String.format(Locale.US, "%02d %s%s", hours, latStr, ns);
     }
     
     public static String lonToPrintable(Double lon) {
         if (lon == null) {
             return "N/A";
         }
         String ns = "E";
         if (lon < 0) {
             ns = "W";
             lon *= -1;
         }
         int hours = (int)lon.doubleValue();
         lon -= hours;
         lon *= 60;        
         String lonStr = String.format(Locale.US, "%3.3f", lon);
         while (lonStr.indexOf('.') < 2) {
             lonStr = "0" + lonStr;
         }        
         return String.format(Locale.US, "%03d %s%s", hours, lonStr, ns);
     }
     
     public static String formatDouble(Double d, int decimals) {
         if (d == null) {
             return "N/A";
         }
         if (decimals == 0) {
             return String.format(Locale.US, "%d", Math.round(d));
         }
         String format = "%." + decimals + "f";
         return String.format(Locale.US, format, d);
     }
 
     public String getName() {
         return name;
     }
 
     public String getCallsign() {
         return callsign;
     }
 
     public String getImoNo() {
         return imoNo;
     }
 
     public String getCargo() {
         return cargo;
     }
 
     public String getCountry() {
         return country;
     }
 
     public String getDraught() {
         return draught;
     }
 
     public String getRot() {
         return rot;
     }
 
     public String getDestination() {
         return destination;
     }
 
     public String getNavStatus() {
         return navStatus;
     }
 
     public String getEta() {
         return eta;
     }
 
     public String getPosAcc() {
         return posAcc;
     }
 
     public long getMmsi() {
         return mmsi;
     }
     
     public String getVesselClass() {
         return vesselClass;
     }
     
     public String getLat() {
         return lat;
     }
 
     public String getLon() {
         return lon;
     }
 
     public boolean isMoored() {
         return moored;
     }
     
     public String getVesselType() {
         return vesselType;
     }
 
     public String getLength() {
         return length;
     }
 
     public long getCurrentTime() {
         return currentTime;
     }
 
     public String getLastReceived() {
         return lastReceived;
     }
 
     public String getWidth() {
         return width;
     }
 
     public String getSourceType() {
         return sourceType;
     }
     
     public long getId() {
         return id;
     }
 
     public String getCog() {
         return cog;
     }
 
     public String getSog() {
         return sog;
     }
 
     public String getHeading() {
         return heading;
     }
 
     public String getPos() {
         return pos;
     }
     
     public String getSourceSystem() {
         return sourceSystem;
     }
     
     public String getSourceRegion() {
         return sourceRegion;
     }
     
     public String getSourceBs() {
         return sourceBs;
     }
     
     public String getSourceCountry() {
         return sourceCountry;
     }
     
     public IPastTrack getPastTrack() {
         return pastTrack;
     }
     
 }
