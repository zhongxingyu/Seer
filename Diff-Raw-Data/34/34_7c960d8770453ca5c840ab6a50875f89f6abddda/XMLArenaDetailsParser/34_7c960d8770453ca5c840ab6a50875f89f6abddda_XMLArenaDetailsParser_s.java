 package api.parser;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import api.entity.ArenaDetails;
 import api.entity.arenadetails.Arena;
 import api.entity.arenadetails.CurrentCapacity;
 import api.entity.arenadetails.ExtendedCapacity;
 import api.entity.arenadetails.League;
 import api.entity.arenadetails.Region;
 import api.entity.arenadetails.Team;
 
 //FIXME: credit to original author!
 
 /**
  * @author Jan Van Haaren
  */
 public class XMLArenaDetailsParser extends XMLParser {
 
 	protected XMLArenaDetailsParser() {
 		// NOP
 	}
 
 	public static ArenaDetails parseArenaFromString(String string) {
 		return parseArena(XMLParser.parseString(string));
 	}
 
 	private static ArenaDetails parseArena(Document document) {
 
 		ArenaDetails arenaDetails = new ArenaDetails();
 
 		try {
 
 			// <HattrickData>
 			Element rootElement = document.getDocumentElement();
 			arenaDetails.setUserID(getElementValue(rootElement, "UserID"));
 			arenaDetails.setFetchedDate(getElementValue(rootElement, "FetchedDate"));
 
 			// <HattrickData/Arena>
 			Element arenaElement = getChildElement(rootElement, "Arena");
 
 			Arena arena = new Arena();
 			arena.setArenaID(getElementValue(arenaElement, "ArenaID"));
 			arena.setArenaName(getElementValue(arenaElement, "ArenaName"));
 			arenaDetails.setArena(arena);
 
 			// <HattrickData/Arena/Team>
 			Element arenaTeamElement = getChildElement(arenaElement, "Team");
 
 			Team team = new Team();
 			team.setTeamID(getElementValue(arenaTeamElement, "TeamID"));
 			team.setTeamName(getElementValue(arenaTeamElement, "TeamName"));
 			arena.setTeam(team);
 
 			// <HattrickData/Arena/League>
 			Element arenaLeagueElement = getChildElement(arenaElement, "League");
 
 			League league = new League();
 			league.setLeagueID(getElementValue(arenaLeagueElement, "LeagueID"));
 			league.setLeagueName(getElementValue(arenaLeagueElement, "LeagueName"));
 			arena.setLeague(league);
 
 			// <HattrickData/Arena/Region>
 			Element arenaRegionElement = getChildElement(arenaElement, "Region");
 
 			Region region = new Region();
 			region.setRegionID(getElementValue(arenaRegionElement, "RegionID"));
 			region.setRegionName(getElementValue(arenaRegionElement, "RegionName"));
 			arena.setRegion(region);
 
 			// <HattrickData/Arena/CurrentCapacity>
 			Element arenaCurrentCapacityElement = getChildElement(arenaElement, "CurrentCapacity");
 
 			CurrentCapacity currentCapacity = new CurrentCapacity();
 			Element rebuiltDateElement = getChildElement(arenaCurrentCapacityElement, "RebuiltDate");
 			if (hasAttributeValue(rebuiltDateElement, "Available", "True")) {
				currentCapacity.setRebuiltDate(getElementValue(arenaRegionElement,"RebuiltDate"));
 			}
 
 			currentCapacity.setTerraces(getElementValue(arenaCurrentCapacityElement,"Terraces"));
 			currentCapacity.setBasic(getElementValue(arenaCurrentCapacityElement,"Basic"));
 			currentCapacity.setRoof(getElementValue(arenaCurrentCapacityElement,"Roof"));
 			currentCapacity.setVIP(getElementValue(arenaCurrentCapacityElement,"VIP"));
 			currentCapacity.setTotal(getElementValue(arenaCurrentCapacityElement,"Total"));
 			arena.setCurrentCapacity(currentCapacity);
 
 			// <HattrickData/Arena/ExpandedCapacity>
 			Element arenaExpandedCapacityElement = getChildElement(arenaElement, "ExpandedCapacity");
 
 			if (hasAttributeValue(arenaExpandedCapacityElement, "Available", "True")) {
 				ExtendedCapacity extendedCapacity = new ExtendedCapacity();
 				extendedCapacity.setExpansionDate(getElementValue(arenaExpandedCapacityElement,"ExpansionDate"));
 				extendedCapacity.setTerraces(getElementValue(arenaExpandedCapacityElement,"Terraces"));
 				extendedCapacity.setBasic(getElementValue(arenaExpandedCapacityElement,"Basic"));
 				extendedCapacity.setRoof(getElementValue(arenaExpandedCapacityElement,"Roof"));
 				extendedCapacity.setVIP(getElementValue(arenaExpandedCapacityElement,"VIP"));
 				arena.setExtendedCapacity(extendedCapacity);
 			}
 
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// return map;
 		return arenaDetails;
 	}
 
 }
