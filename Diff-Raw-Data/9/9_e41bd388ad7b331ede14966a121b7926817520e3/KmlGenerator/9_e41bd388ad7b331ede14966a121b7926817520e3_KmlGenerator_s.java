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
 package dk.dma.ais.analysis.viewer.kml;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import de.micromata.opengis.kml.v_2_2_0.Document;
 import de.micromata.opengis.kml.v_2_2_0.Folder;
 import de.micromata.opengis.kml.v_2_2_0.Icon;
 import de.micromata.opengis.kml.v_2_2_0.ItemIconState;
 import de.micromata.opengis.kml.v_2_2_0.Kml;
 import de.micromata.opengis.kml.v_2_2_0.LineString;
 import de.micromata.opengis.kml.v_2_2_0.ListItemType;
 import de.micromata.opengis.kml.v_2_2_0.ListStyle;
 import de.micromata.opengis.kml.v_2_2_0.Placemark;
 import de.micromata.opengis.kml.v_2_2_0.Style;
 import dk.dma.ais.analysis.viewer.handler.AisTargetEntry;
 import dk.dma.ais.data.AisClassAPosition;
 import dk.dma.ais.data.AisClassAStatic;
 import dk.dma.ais.data.AisClassATarget;
 import dk.dma.ais.data.AisTarget;
 import dk.dma.ais.data.AisVesselPosition;
 import dk.dma.ais.data.AisVesselStatic;
 import dk.dma.ais.data.AisVesselTarget;
 import dk.dma.ais.data.IPastTrack;
 import dk.dma.ais.data.PastTrackPoint;
 import dk.dma.ais.message.NavigationalStatus;
 import dk.dma.ais.message.ShipTypeCargo;
 import dk.dma.ais.message.ShipTypeCargo.ShipType;
 
 public class KmlGenerator {
 
     private final Map<Integer, AisTargetEntry> targetsMap;
     private final Map<Integer, IPastTrack> pastTrackMap;
     private String resourceUrl;
     final Kml kml;
     final Document document;
 
     // static folders
     private final Folder lastKnownPositions;
     private final Folder shipnamefolder;
     private final Folder shiptypesfolder;
     private final Folder tanker;
     private final Folder cargo;
     private final Folder passenger;
     private final Folder fishing;
     private final Folder undefined;
     private final Folder highspeedcraftandWIG;
     private final Folder sailingandpleasure;
     private final Folder pilottugandothers;
     private Folder pickedfolder;
     private final Folder pasttrackfolder;
     private final Folder twentyfourhourfolder;
     private final Folder threedayfolder;
     private Folder sart;
 
     public KmlGenerator(Map<Integer, AisTargetEntry> targetsMap, Map<Integer, IPastTrack> pastTrackMap, String resourceURL) {
         this.targetsMap = targetsMap;
         this.pastTrackMap = pastTrackMap;
         this.resourceUrl = resourceURL;
         kml = new Kml();
         document = kml.createAndSetDocument();
         
         //Add folderstyles
         addFolderStyle("PassengerFolder",  resourceUrl + "vessel_blue.png");
         addFolderStyle("CargoFolder",  resourceUrl + "vessel_green.png");
         addFolderStyle("TankerFolder",  resourceUrl + "vessel_red.png");
         addFolderStyle("HighspeedcraftandWIGFolder",  resourceUrl + "vessel_yellow.png");
         addFolderStyle("FishingFolder",  resourceUrl + "vessel_orange.png");
         addFolderStyle("SailingandpleasureFolder",  resourceUrl + "vessel_purple.png");
         addFolderStyle("PilottugandothersFolder",  resourceUrl + "vessel_turquoise.png");
         addFolderStyle("UndefinedunknownFolder",  resourceUrl + "vessel_gray.png");
         addFolderStyle("sartFolder",  resourceUrl + "SART_red.ico");
 
         
 
         // create the static folders
         lastKnownPositions = document.createAndAddFolder().withName("Last known position");
         shipnamefolder = document.createAndAddFolder().withName("Ship names").withVisibility(false);
         shiptypesfolder = document.createAndAddFolder().withName("Ship types").withVisibility(false);
         tanker = lastKnownPositions.createAndAddFolder().withName("Tanker").withVisibility(false).withStyleUrl("TankerFolder");
         cargo = lastKnownPositions.createAndAddFolder().withName("Cargo").withVisibility(false).withStyleUrl("CargoFolder");
         passenger = lastKnownPositions.createAndAddFolder().withName("Passenger").withVisibility(false).withStyleUrl("PassengerFolder");
         fishing = lastKnownPositions.createAndAddFolder().withName("Fishing").withVisibility(false).withStyleUrl("FishingFolder");
         undefined = lastKnownPositions.createAndAddFolder().withName("Undefined").withVisibility(false).withStyleUrl("UndefinedunknownFolder");
         highspeedcraftandWIG = lastKnownPositions.createAndAddFolder().withName("High speed craft and Wig").withVisibility(false).withStyleUrl("HighspeedcraftandWIGFolder");
         sailingandpleasure = lastKnownPositions.createAndAddFolder().withName("Sailing and pleasure").withVisibility(false).withStyleUrl("SailingandpleasureFolder");
         pilottugandothers = lastKnownPositions.createAndAddFolder().withName("Pilot, TUG and other").withVisibility(false).withStyleUrl("PilottugandothersFolder");
         sart = lastKnownPositions.createAndAddFolder().withName("SART").withVisibility(false).withStyleUrl("sartFolder");
         pickedfolder = undefined;
         pasttrackfolder = document.createAndAddFolder().withName("Tracks").withVisibility(false);
         twentyfourhourfolder = pasttrackfolder.createAndAddFolder().withName("24 hours").withVisibility(false);
         threedayfolder = pasttrackfolder.createAndAddFolder().withName("72 hours").withVisibility(false);
 
         // Add styles
         addStyle("PassengerMoored", resourceUrl + "vessel_blue_moored.png", "ff0000ff", 0.8, "<![CDATA[$[name]$[description]", 0);
         addStyle("CargoMoored", resourceUrl + "vessel_green_moored.png", "ff0000ff", .8, "<![CDATA[$[name]$[description]", 0);
         addStyle("TankerMoored", resourceUrl + "vessel_red_moored.png", "ff0000ff", .8, "<![CDATA[$[name]$[description]", 0);
         addStyle("HighspeedcraftandWIGMoored", resourceUrl + "vessel_yellow_moored.png", "ff0000ff", .8,
                 "<![CDATA[$[name]$[description]", 0);
         addStyle("FishingMoored", resourceUrl + "vessel_orange_moored.png", "ff0000ff", .8, "<![CDATA[$[name]$[description]", 0);
         addStyle("SailingandpleasureMoored", resourceUrl + "vessel_purple_moored.png", "ff0000ff", .8,
                 "<![CDATA[$[name]$[description]", 0);
         addStyle("PilottugandothersMoored", resourceUrl + "vessel_turquoise_moored.png", "ff0000ff", .8,
                 "<![CDATA[$[name]$[description]", 0);
         addStyle("UndefinedunknownMoored", resourceUrl + "vessel_gray_moored.png", "ff0000ff", .8,
                 "<![CDATA[$[name]$[description]", 0);
 //        addStyle("SailingandpleasureMoored", resourceUrl + "vessel_white_moored.png", "ff0000ff", .8,
 //                "<![CDATA[$[name]$[description]", 0);
         addStyle("SART_ACTIVE", resourceUrl + "SART_red.ico", "ff0000ff", .8, "<![CDATA[$[name]$[description]", 0);
         addStyle("SART_TEST", resourceUrl + "SART_grey.ico", "ff0000ff", .8, "<![CDATA[$[name]$[description]", 0);
         addStyle("empty", "", "", .8, "", 0);
 
         for (int i = 0; i <= 360; i++) {
             addStyle(("Passenger-" + i), resourceUrl + "vessel_blue.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]", i + 270);
             addStyle(("Cargo-" + i), resourceUrl + "vessel_green.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]", i + 270);
             addStyle(("Tanker-" + i), resourceUrl + "vessel_red.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]", i + 270);
             addStyle(("HighspeedcraftandWIG-" + i), resourceUrl + "vessel_yellow.png", "ff0000ff", 1,
                     "<![CDATA[$[name]$[description]", i + 270);
             addStyle(("Fishing-" + i), resourceUrl + "vessel_orange.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]", i + 270);
             addStyle(("Sailingandpleasure-" + i), resourceUrl + "vessel_purple.png", "ff0000ff", 1,
                     "<![CDATA[$[name]$[description]", i + 270);
             addStyle(("Pilottugandothers-" + i), resourceUrl + "vessel_turquoise.png", "ff0000ff", 1,
                     "<![CDATA[$[name]$[description]", i + 270);
             addStyle(("Undefinedunknown-" + i), resourceUrl + "vessel_gray.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]",
                     i + 270);
             addStyle(("Sailing-" + i), resourceUrl + "vessel_white.png", "ff0000ff", 1, "<![CDATA[$[name]$[description]", i + 270);
         }
     }
 
     public String generate() {
 
         List<AisTargetEntry> sortedByMMSI = new ArrayList<AisTargetEntry>();
         List<AisVesselTarget> sortedByName = new ArrayList<AisVesselTarget>();
 
         for (AisTargetEntry entry : targetsMap.values()) {
             sortedByMMSI.add(entry);
         }
         Collections.sort(sortedByMMSI, new SortByMMSIComparator());
         // For each AIS target
         for (AisTargetEntry entry : sortedByMMSI) {
 
             // Initial checks. Vessel has to have at least a position
             AisTarget target = entry.getTarget();
             if (!(target instanceof AisVesselTarget)) {
                 continue;
             }
             AisVesselTarget vesselTarget = (AisVesselTarget) target;
             AisVesselPosition vesselPosition = vesselTarget.getVesselPosition();
             if (vesselPosition == null)
                 continue;
             if (vesselPosition.getPos() == null)
                 continue;
 
             // get past track
             IPastTrack pastTrack = pastTrackMap.get(vesselTarget.getMmsi());
             List<PastTrackPoint> trackPoints = null;
             if (pastTrack != null)
                 trackPoints = pastTrack.getPoints();
 
             // Extract ship information
             pickedfolder = undefined;
             String name = "" + vesselTarget.getMmsi();
             String shiptype = "unknown";
             String style = "Undefinedunknown";
             String styleprefix = "Undefinedunknown";
             String description = "";
             Date lastReport = new Date();
             double age = 0.0;
             int mmsi = vesselTarget.getMmsi();
             int imo = 0;
             String callsign = "Unknown";
             String flag = "";
             int length = 0;
             int breadth = 0;
             double draught = 0.0;
             String navstatus = "";
             String destination = "Unknown";
             double heading = 0.0;
             double cog = 0.0;
             double sog = 0.0;
             boolean isMoored = false;
             boolean isSART = false;
             boolean isSARTTEST = false;
             String sartDescription = "";
 
             // Extract information from vesselstatic
             AisVesselStatic vesselStatic = vesselTarget.getVesselStatic();
             if (vesselStatic != null) {
 
                 // add to sortedbyname list
                 sortedByName.add(vesselTarget);
 
                 // Extract name
                 name = vesselStatic.getName();
 
                 // extract ship type
                 ShipType type = null;
                 if (vesselStatic.getShipTypeCargo() != null) {
                     type = vesselStatic.getShipTypeCargo().getShipType();
 
                     if (type != null) {
                         shiptype = type.toString();
                         if (type.equals(ShipTypeCargo.ShipType.PASSENGER)) {
                             styleprefix = "Passenger";
                             pickedfolder = passenger;
                         } else if (type.equals(ShipTypeCargo.ShipType.CARGO)) {
                             styleprefix = "Cargo";
                             pickedfolder = cargo;
                         } else if (type.equals(ShipTypeCargo.ShipType.TANKER)) {
                             styleprefix = "Tanker";
                             pickedfolder = tanker;
                         } else if (type.equals(ShipTypeCargo.ShipType.HSC) || type.equals(ShipTypeCargo.ShipType.WIG)) {
                             styleprefix = "HighspeedcraftandWIG";
                             pickedfolder = highspeedcraftandWIG;
                         } else if (type.equals(ShipTypeCargo.ShipType.FISHING)) {
                             styleprefix = "Fishing";
                             pickedfolder = fishing;
                         } else if (type.equals(ShipTypeCargo.ShipType.PILOT) || type.equals(ShipTypeCargo.ShipType.MILITARY)
                                 || type.equals(ShipTypeCargo.ShipType.SAR) || type.equals(ShipTypeCargo.ShipType.DREDGING)
                                 || type.equals(ShipTypeCargo.ShipType.TUG) || type.equals(ShipTypeCargo.ShipType.TOWING)
                                 || type.equals(ShipTypeCargo.ShipType.TOWING_LONG_WIDE)
                                 || type.equals(ShipTypeCargo.ShipType.ANTI_POLLUTION)
                                 || type.equals(ShipTypeCargo.ShipType.LAW_ENFORCEMENT)
                                 || type.equals(ShipTypeCargo.ShipType.PORT_TENDER)
                                 || type.equals(ShipTypeCargo.ShipType.DIVING)) {
                             styleprefix = "Pilottugandothers";
                             pickedfolder = pilottugandothers;
                         } else if (type.equals(ShipTypeCargo.ShipType.SAILING) || type.equals(ShipTypeCargo.ShipType.PLEASURE)) {
                             styleprefix = "Sailingandpleasure";
                             pickedfolder = sailingandpleasure;
                         } else if (type.equals(ShipTypeCargo.ShipType.UNKNOWN)) {
                         	styleprefix = "Undefinedunknown";
                             pickedfolder = undefined;
                         } else {
                             styleprefix = "Undefinedunknown";
                             pickedfolder = undefined;
                         }
                     }
                 }
                 
 
                 // Extract length and breadth
                 if (vesselStatic.getDimensions() != null) {
                     length = vesselStatic.getDimensions().getDimBow() + vesselStatic.getDimensions().getDimStern();
                     breadth = vesselStatic.getDimensions().getDimPort() + vesselStatic.getDimensions().getDimStarboard();
                 }
             }
 
             // set flag (country)
             if (vesselTarget.getCountry() != null) {
                 flag = vesselTarget.getCountry().getName();
             }
 
             // Extract class A information
             // if ship is an A class ship, set destination, draught, imo number and navigation status
             if (vesselTarget instanceof AisClassATarget) {
                 AisClassATarget classAtarget = (AisClassATarget) vesselTarget;
                 // classAtarget.getClassAPosition().getNavStatus()
                 AisClassAPosition classAPosition = classAtarget.getClassAPosition();
                 AisClassAStatic classAStatic = classAtarget.getClassAStatic();
                 if (classAStatic != null) {
                     if (classAStatic.getDestination() != null) {
                         destination = classAStatic.getDestination();
                     }
                     if (classAStatic.getDraught() != null) {
                         draught = classAStatic.getDraught();
                     }
                     if (classAStatic.getImoNo() != null) {
                         imo = classAStatic.getImoNo();
                     }
                     if (classAPosition != null) {
                        NavigationalStatus navigationalStatus = new NavigationalStatus(classAPosition.getNavStatus());
                         navstatus = navigationalStatus.prettyStatus();
                         if (classAPosition.getNavStatus() == 1 || classAPosition.getNavStatus() == 5) {
                             isMoored = true;
                         }
                         if (mmsi >= 970000000 && mmsi < 980000000) {
                             if (classAPosition.getNavStatus() == 14)
                                 isSART = true;
                             else if (classAPosition.getNavStatus() == 15)
                                 isSARTTEST = true;
                         }
                     }
 
                 }
             }
 
             // Extract more information
             if (vesselTarget.getLastReport() != null) {
                 lastReport = vesselTarget.getLastReport();
                 Date now = new Date();
                 age = ((now.getTime() - lastReport.getTime()) / (1000 * 60 * 60));
             }
             if (vesselStatic != null) {
                 callsign = vesselStatic.getCallsign();
             }
             if (vesselPosition.getHeading() != null) {
                 heading = vesselPosition.getHeading();
             }
             if (vesselPosition.getCog() != null) {
                 cog = vesselPosition.getCog();
             }
             if (vesselPosition.getSog() != null) {
                 sog = vesselPosition.getSog();
             }
 
             // Check if vessel is moored
             // Double sog = vesselPosition.getSog();
             // if(sog != null && sog < 1){
             if (isMoored) {
                 style = styleprefix + "Moored";
             }
             // If target is not moored, set direction
             else {
                 int direction = 0;
                 if (vesselPosition.getCog() != null)
                     direction = (int) Math.round(vesselPosition.getCog());
 
                 style = pickStyle(styleprefix, direction);
             }
             if (isSART) {
                 style = "SART_ACTIVE";
                 pickedfolder = sart;
                 sartDescription = "<td Align=\"Left\"> </td><td Align=\"right\"> </td></tr><tr><td Align=\"Left\"><b>SART: </b></td> <td Align=\"right\"><b>THIS IS AN EMERGENCY!</b></td></tr><tr>";
             } else if (isSARTTEST) {
                 style = "SART_TEST";
                 pickedfolder = sart;
                 sartDescription = "<td Align=\"Left\"> </td><td Align=\"right\"> </td></tr><tr><td Align=\"Left\"><b>SART: </b></td> <td Align=\"right\"><b>This is a test!</b></td></tr><tr>";
             }
 
             // Extract description
             description = "<font size= \"5\" color=\"black\">"
                     + lastReport
                     + " Age "
                     + age
                     + "h </font><table width=\"275\" align=\"centeret\"><tr>"
                     + "<td width=\"100\" Align=\"Left\">"
                     + "Ship name: </td> <td Align=\"right\">"
                     + name
                     + "</td></tr><tr><td Align=\"Left\">"
                     + "mmsi: </td> <td Align=\"right\"><a href=\"http://www.marinetraffic.com/ais/showallphotos.aspx?mmsi="
                     + mmsi
                     + "\"> "
                     + mmsi
                     + "</a></td></tr><tr><td Align=\"Left\"> imo: </td> <td Align=\"right\"><a href=\"http://www.marinetraffic.com/ais/showallphotos.aspx?mmsi="
                     + mmsi + "\">" + imo + "</a></td></tr>" + "<tr><td Align=\"Left\"> Ship type: </td> <td Align=\"right\">"
                     + shiptype + "</td></tr><tr>" + "<td Align=\"Left\"> Call sign: </td> <td Align=\"right\">" + callsign
                     + "</td></tr><tr>" + "<td Align=\"Left\"> Flag: </td><td Align=\"right\">" + flag + "</td></tr><tr>"
                     + "<td Align=\"Left\"> </td><td Align=\"right\"> </td></tr><tr>"
                     + "<td Align=\"Left\"> Length (m): </td> <td Align=\"right\">" + length + "</td></tr><tr>"
                     + "<td Align=\"Left\"> Breadth (m): </td> <td Align=\"right\"> " + breadth + "</td></tr><tr>"
                     + "<td Align=\"Left\"> Draught (m): </td> <td Align=\"right\">" + draught + "</td></tr><tr>"
                     + "<td Align=\"Left\"> </td><td Align=\"right\"> </td></tr><tr>"
                     + "<td Align=\"Left\"> Nav. status: </td><td Align=\"right\">" + navstatus + "</td></tr><tr>"
                     + "<td Align=\"Left\"> Destination: </td><td Align=\"right\">" + destination + "</td></tr><tr>"
                     + "<td Align=\"Left\"> Heading: </td> <td Align=\"right\"> " + heading + "</td></tr><tr>"
                     + "<td Align=\"Left\"> cog: </td> <td Align=\"right\"> " + cog + "</td></tr><tr>"
                     + "<td Align=\"Left\"> sog (knots): </td> <td Align=\"right\">" + sog + "</td></tr><tr>" + sartDescription
                     + "</table>";
 
                     
             addToShipTypeFolder(styleprefix, vesselPosition);
             addVessel(style, styleprefix, name, description, trackPoints, vesselPosition, twentyfourhourfolder, threedayfolder,
                     vesselTarget.getMmsi());
         }
 
         // sort by name and add to name folder
         Collections.sort(sortedByName, new SortByNameComparator());
         for (AisVesselTarget entry : sortedByName) {
             AisVesselPosition vesselPosition = entry.getVesselPosition();
             // if(entry.getVesselStatic()!=null){
             String name = entry.getVesselStatic().getName();
             addToShipNameFolder(name, vesselPosition);
             // }
         }
 
         try {
             return marshall();
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return null;
         }
 
     }
 
     private String pickStyle(String shiptype, int direction) {
         return (shiptype + "-" + direction);
     }
 
     private void addStyle(String stylename, String iconUri, String iconColor, double iconScale, String ballonText, int heading) {
         Style style = document.createAndAddStyle();
         style.withId(stylename);
         style.createAndSetIconStyle()
         // .withColor(iconColor)
                 .withHeading(heading).withScale(iconScale).withIcon(new Icon().withHref(iconUri));
 
         style.createAndSetBalloonStyle().withText(ballonText);
     }
     
     private void addFolderStyle(String stylename, String iconUri)
     {
 //    	Style style = document.createAndAddStyle().createAndSetListStyle().w
     	ListStyle listStyle = document.createAndAddStyle().withId(stylename).createAndSetListStyle().withListItemType(ListItemType.CHECK);
     	listStyle.createAndAddItemIcon().addToState(ItemIconState.CLOSED).withHref(iconUri);
     }
 
     private void addVessel(String stylename, String styleprefix, String name, String description, List<PastTrackPoint> pastTrackPoints,
             AisVesselPosition vesselPosition, Folder twentyfourhour, Folder threeday, int mmsi) {
         // if(pastTrackPoints.isEmpty())
         // return;
 //    	System.out.println(pickedfolder.getName());
 //    	if (pickedfolder.getName().equals("Fishing")) {
 //			System.out.println(stylename);
 //		}
 
         Folder folder = pickedfolder.createAndAddFolder().withName("" + mmsi).withStyleUrl(styleprefix+"Folder");
 
         Folder folder1 = twentyfourhour.createAndAddFolder().withName("" + mmsi).withVisibility(false);
         Folder folder2 = threeday.createAndAddFolder().withName("" + mmsi).withVisibility(false);
 
         Placemark placemark2 = folder1.createAndAddPlacemark();
         LineString linestring1 = placemark2.createAndSetLineString();
         linestring1.withTessellate(new Boolean(true));
 
         Placemark placemark3 = folder2.createAndAddPlacemark();
         LineString linestring2 = placemark3.createAndSetLineString();
         linestring2.withTessellate(new Boolean(true));
 
         // Placemark placemark1 = folder.createAndAddPlacemark();
         // LineString linestring = placemark1.createAndSetLineString();
         // linestring.withTessellate(new Boolean(true));
 
         // add pasttrack path
         if (pastTrackPoints != null) {
 
             // check time of pathtrack points. Only keep points within 24 and 72 hours
             Date now = null;
             if (!pastTrackPoints.isEmpty())
                 now = pastTrackPoints.get(pastTrackPoints.size() - 1).getTime();
 
             for (int i = pastTrackPoints.size() - 1; i >= 0; i--) {
                 // System.out.println(pastTrackPoints.get(i).getTime());
                 int timeDif_Hours = (int) Math.abs((now.getTime() - pastTrackPoints.get(i).getTime().getTime()) / 1000 / 60 / 60);
 
                 PastTrackPoint trackpoint = pastTrackPoints.get(i);
 
                 // Put in 24hour folder
                 if (timeDif_Hours <= 24) {
                     linestring1.addToCoordinates(trackpoint.getLon(), trackpoint.getLat());
                 }
                 // Put in 72hour folder
                 if (timeDif_Hours <= 72) {
                     linestring2.addToCoordinates(trackpoint.getLon(), trackpoint.getLat());
 
                 } else {
                     break;
                 }
                 // System.out.println("time from now: "+timeDif_Hours);
             }
         }
         // if(description != null){
         // placemark1.withDescription(description);
         // }
 
         folder.createAndAddPlacemark().withStyleUrl(stylename).withDescription(description).createAndSetPoint()
                 .addToCoordinates(vesselPosition.getPos().getLongitude(), vesselPosition.getPos().getLatitude());
 
     }
 
     // Add to ship name folder
     private void addToShipNameFolder(String name, AisVesselPosition vesselPosition) {
         shipnamefolder.createAndAddFolder().withName(name).withVisibility(false).createAndAddPlacemark().withName(name)
                 .withVisibility(false).withStyleUrl("empty").createAndSetPoint()
                 .addToCoordinates(vesselPosition.getPos().getLongitude(), vesselPosition.getPos().getLatitude());
     }
 
     // Add to ship type folder
     private void addToShipTypeFolder(String name, AisVesselPosition vesselPosition) {
 
         shiptypesfolder.createAndAddFolder().withName(name).withVisibility(false).createAndAddPlacemark().withName(name)
                 .withVisibility(false).withStyleUrl("empty").createAndSetPoint()
                 .addToCoordinates(vesselPosition.getPos().getLongitude(), vesselPosition.getPos().getLatitude());
 
     }
 
     private String marshall() throws FileNotFoundException {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         kml.marshal(bos);
         return bos.toString();
     }
 
     public class SortByMMSIComparator implements Comparator<AisTargetEntry> {
 
         @Override
         public int compare(AisTargetEntry a1, AisTargetEntry a2) {
             if (a1.getTarget().getMmsi() > a2.getTarget().getMmsi())
                 return 1;
             else if (a1.getTarget().getMmsi() < a2.getTarget().getMmsi())
                 return -1;
             else
                 return 0;
         }
     }
 
     public class SortByNameComparator implements Comparator<AisVesselTarget> {
 
         public int compare(AisVesselTarget a1, AisVesselTarget a2) {
             String s1 = a1.getVesselStatic().getName();
             String s2 = a2.getVesselStatic().getName();
             if (s1 == null)
                 s1 = "";
             if (s2 == null)
                 s2 = "";
 
             return s1.compareTo(s2);
 
         }
     }
 
 }
