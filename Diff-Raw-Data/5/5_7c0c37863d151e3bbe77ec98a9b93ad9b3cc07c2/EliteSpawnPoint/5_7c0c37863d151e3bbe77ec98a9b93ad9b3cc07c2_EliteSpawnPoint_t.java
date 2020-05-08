 package data.scripts.uss;
 
 import com.fs.starfarer.api.Global;
 import com.fs.starfarer.api.Script;
 import com.fs.starfarer.api.campaign.CampaignFleetAPI;
 import com.fs.starfarer.api.campaign.FleetAssignment;
 import com.fs.starfarer.api.campaign.LocationAPI;
 import com.fs.starfarer.api.campaign.SectorAPI;
 import com.fs.starfarer.api.campaign.SectorEntityToken;
 import com.fs.starfarer.api.fleet.FleetMemberAPI;
 import com.fs.starfarer.api.fleet.FleetMemberType;
import data.scripts.UsSUtils;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 @SuppressWarnings("unchecked")
 public class EliteSpawnPoint extends GeneralEliteSpawnPoint {
     
         CampaignFleetAPI fleet;
 
 	public EliteSpawnPoint(SectorAPI sector, LocationAPI location, int daysInterval, int maxFleets, SectorEntityToken anchor, String faction, String rnd_faction_fleet, String fleet_name, String fleetType, String range, int startFP, Map variants,
                         Map capitals, Map cruisers, Map destroyers, Map frigates, Map wings, Map specials, String baseship,
                         float CS_chance, float C_chance, float D_chance, float W_chance, float S_chance,
                         String A_focus1, String A_focus2, String A_focus3) {
 		super(sector, location, daysInterval, maxFleets, anchor, faction, rnd_faction_fleet, fleet_name, fleetType, range, startFP, variants,
                         capitals, cruisers, destroyers, frigates, wings, specials, baseship,
                         CS_chance, C_chance, D_chance, W_chance, S_chance,
                         A_focus1, A_focus2, A_focus3);
 	}
 
 @Override
 	public CampaignFleetAPI spawnElite() {
                 
                 fleet = getSector().createFleet(getFaction(), getRndFactionFleet());
 		getLocation().spawnFleet(getAnchor(), 0, 0, fleet);
                 if (getFleetName().equals("merc"))   {
                     fleet.setName(fleet.getCommander().getName().getFullName() + " [xXx]");
                 }
                 else {
                     fleet.setName(getFleetName() + " [xXx]");
                     fleet.getCommander().getName().setFirst(getFleetName());
                     fleet.getCommander().getName().setLast("");
                 }
                 
                 UsSUtils.RemoveFleetMembers(fleet);
                 
                 if (getFlagship().endsWith("_Hull")) {
                     AddShip(getFlagship());
                 } else {
                     AddShip(UsSUtils.getRandomMapMember(getDestroyers()));
                 }
                 
                 UsSUtils.SetLevel(fleet, 5, getAptitudeFocus_1(), getAptitudeFocus_2(), getAptitudeFocus_3());
                 
                 UsSUtils.RandomizeAndSortAndFill(fleet, getVariants(), 5);
                 
                 initAI();
                 
                 return fleet;
 	}
         
         private void initAI () {  
             Script script = EliteAI(fleet, getAnchor());
                fleet.setPreferredResupplyLocation(getAnchor());
                 fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, getAnchor(), 1);
                 fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, getAnchor(), 100, script);                
         }
         
         private Script EliteAI(final CampaignFleetAPI fleet, final SectorEntityToken station) {
 		return new Script() {
                         public void run() {
                                 float lvl = ((fleet.getCommander().getStats().getAptitudeLevel("combat"))
                                       + (fleet.getCommander().getStats().getAptitudeLevel("leadership"))
                                       + (fleet.getCommander().getStats().getAptitudeLevel("technology")));
 
                                 if (lvl < 30)   {
                                     UsSUtils.LevelUp(fleet, getAptitudeFocus_1(), getAptitudeFocus_2(), getAptitudeFocus_3());
 //                                    if (lvl != 5) {
 //                                        Global.getSector().addMessage("" + fleet.getName() + " :" + lvl);
 //                                    }
                                 }
                                 Trading(station);
                                 UsSUtils.RandomizeAndSortAndFill(fleet, getVariants(),(int) lvl);
                                 NewAssignment(station, lvl);
                         }
                 };
 	}
         
         private void  Trading (SectorEntityToken station) {  
             int FP = (int) fleet.getFleetPoints();
             FleetMemberAPI stationMothListMember;
             List station_ships = station.getCargo().getMothballedShips().getMembersListCopy();
             ArrayList Capitals = new ArrayList();
             ArrayList Cruisers = new ArrayList();
             ArrayList Destroyers = new ArrayList();
             ArrayList Frigates = new ArrayList();
             ArrayList Fighters = new ArrayList();
             for (int i = 0; i < station_ships.size(); i++) {
                 stationMothListMember = (FleetMemberAPI)station_ships.get(i);
                 if  (stationMothListMember.isCapital()) {
                     Capitals.add(stationMothListMember);
                 } else if (stationMothListMember.isCruiser()) {
                     Cruisers.add(stationMothListMember);
                 } else if (stationMothListMember.isDestroyer()) {
                     Destroyers.add(stationMothListMember);
                 } else if (stationMothListMember.isFrigate()) {
                     Frigates.add(stationMothListMember);
                 } else if (stationMothListMember.isFighterWing()) {
                     Fighters.add(stationMothListMember);
                 }
             }
             FleetMemberAPI stationMothShip;
             if ((FP > (90 + (15 * Math.random()) - (getCapitalsChance() * 0.30f)))     & (Math.random() < (getCapitalsChance()/100))   & (!Capitals.isEmpty()))   {
                 stationMothShip = (FleetMemberAPI)Capitals.get((int) (Math.random() * (Capitals.size())));
                 AddShip(stationMothShip.getSpecId());
                 station.getCargo().getMothballedShips().removeFleetMember(stationMothShip);
                 UsSUtils.removeRandomWeapon(station.getCargo());
                 UsSUtils.removeRandomWeapon(station.getCargo());
                 UsSUtils.removeRandomWeapon(station.getCargo());
             } else
             if ((FP > (60 + (10 * Math.random()) - (getCruisersChance() * 0.20f)))     & (Math.random() < (getCruisersChance()/100))   & (!Cruisers.isEmpty()))  {
                 stationMothShip = (FleetMemberAPI)Cruisers.get((int) (Math.random() * (Cruisers.size())));
                 AddShip(stationMothShip.getSpecId());
                 station.getCargo().getMothballedShips().removeFleetMember(stationMothShip);
                 UsSUtils.removeRandomWeapon(station.getCargo());
                 UsSUtils.removeRandomWeapon(station.getCargo());
             } else 
             if ((FP > (25 + (5 * Math.random()) -  (getDestroyersChance() * 0.10f)))   & (Math.random() < (getDestroyersChance()/100)) & (!Destroyers.isEmpty()))  {
                 stationMothShip = (FleetMemberAPI)Destroyers.get((int) (Math.random() * (Destroyers.size())));
                 AddShip(stationMothShip.getSpecId());
                 station.getCargo().getMothballedShips().removeFleetMember(stationMothShip);
                 UsSUtils.removeRandomWeapon(station.getCargo());
             } else 
             if (                                                                          (Math.random() < (getWingsChance()/100))      & (!Fighters.isEmpty()))   {
                 stationMothShip = (FleetMemberAPI)Fighters.get((int) (Math.random() * (Fighters.size())));
                 AddShip(stationMothShip.getSpecId());
                 station.getCargo().getMothballedShips().removeFleetMember(stationMothShip);
                 UsSUtils.removeRandomWeapon(station.getCargo());
             } else 
             if (!Frigates.isEmpty())   {
                 stationMothShip = (FleetMemberAPI)Frigates.get((int) (Math.random() * (Frigates.size())));
                 AddShip(stationMothShip.getSpecId());
                 station.getCargo().getMothballedShips().removeFleetMember(stationMothShip);
                 UsSUtils.removeRandomWeapon(station.getCargo());
             }                
         }
         
         private void  NewAssignment (SectorEntityToken station, float lvl) {  
             SectorEntityToken new_station = null;
             float b = (float) Math.random();
             if (getRange().equals("station")) {
                 new_station = station;
             } else if (getRange().equals("friendly")) {
                 new_station = UsSUtils.getRandomStationByRelationship(fleet, "friendly");
             } else if (getRange().equals("neutral")) {
                 if (Math.random() > 0.50) {
                     new_station = UsSUtils.getRandomStationByRelationship(fleet, "neutral"); 
                 } else {
                     new_station = UsSUtils.getRandomStationByRelationship(fleet, "friendly");
                 }
             }
             if (new_station == null) {
                 new_station = station;
                 Global.getSector().addMessage("Elite Assignment NULL new station, REPORT!");
             }
             fleet.setPreferredResupplyLocation(new_station);
             fleet.addAssignment(FleetAssignment.RESUPPLY, station, 100);
             int duration = (int) (Math.random()*3 + lvl/2);
             if ("defender".equals(getFleetType()))   {
                 if (Math.random() > 0.50) {
                     fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, new_station, duration);
                 } else {
                     fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, new_station, duration);
                 }
             } else if ("hunter".equals(getFleetType()))  {                
                 CampaignFleetAPI enemy = UsSUtils.getHostileFleet(fleet);
                 if (enemy != null) {
                     if (fleet.isInCurrentLocation()) {
                         Global.getSector().addMessage(fleet.getNameWithFaction().replace(" [xXx]", "") + " is attacking " + enemy.getNameWithFaction().replace(" [xXx]", "") + "!");
                     }
                     fleet.addAssignment(FleetAssignment.ATTACK_LOCATION, enemy, duration);
                 } else {
                 fleet.addAssignment(FleetAssignment.RAID_SYSTEM, new_station, duration);
                 }
             } else if ("raider".equals(getFleetType()))  {
                 fleet.addAssignment(FleetAssignment.RAID_SYSTEM, UsSUtils.getRandomSystem(getSector()).getStar(), duration);
             } else {
                 fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, new_station, duration);
             }
 
             fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, new_station, 100, EliteAI(fleet, new_station));   
                                 
         }
         
         private void AddShip(String ship) {
                 FleetMemberAPI a = null;
                 if (ship.endsWith("_wing")) {
                     a = Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, ship);
                 } else {
                     a = Global.getFactory().createFleetMember(FleetMemberType.SHIP, ship);
                 }
                 fleet.getFleetData().addFleetMember(a);
         }
 }
 
 
 
 
 
 
