 package de.abbaddie.wot.fleet.data.ovent;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 
 import de.abbaddie.wot.data.coordinates.Coordinates;
 import de.abbaddie.wot.data.coordinates.StaticCoordinates;
 import de.abbaddie.wot.data.ovent.OventNature;
 import de.abbaddie.wot.data.ovent.OventNatureComponent;
 import de.abbaddie.wot.data.resource.ResourcePredicate;
 import de.abbaddie.wot.data.resource.ResourceValueSet;
 import de.abbaddie.wot.data.resource.Resources;
 import de.abbaddie.wot.data.spec.Spec;
 import de.abbaddie.wot.data.spec.SpecRepository;
 import de.abbaddie.wot.data.spec.SpecSet;
 import de.abbaddie.wot.data.spec.Specs;
 import de.abbaddie.wot.data.spec.filter.PositiveFilter;
 import de.abbaddie.wot.lib.php.ArrayDataMap;
 import de.abbaddie.wot.lib.php.DataMap;
 import de.abbaddie.wot.lib.php.Parser;
 
 @OventNatureComponent(oventTypeId = 1)
 public class FleetOventNature implements OventNature {
 	private static final Logger logger = LoggerFactory.getLogger(FleetOventNature.class);
 	
 	protected List<FleetOventFleet> fleets;
 	
 	@Autowired
 	protected SpecRepository specRepo;
 	
 	public FleetOventNature() {
 		fleets = new ArrayList<>();
 	}
 	
 	public FleetOventNature(FleetOventFleet fleet) {
 		fleets = new ArrayList<>();
 		fleets.add(fleet);
 	}
 	
 	public List<FleetOventFleet> getFleets() {
 		return fleets;
 	}
 	
 	public void addFleet(FleetOventFleet fleet) {
 		fleets.add(fleet);
 	}
 	
 	@Override
 	public int getOventTypeId() {
 		return 1;
 	}
 	
 	@Override
 	public void unserialize(String dataStr) {
 		DataMap data = (DataMap) Parser.fromString(dataStr.substring(1));
 		
 		this.fleets = new ArrayList<>(data.size());
 		
 		logger.debug("beginning unserialisation of fleetovent");
 		for(Object fleetDataObj : data.values()) {
 			DataMap fleetData = (DataMap) fleetDataObj;
 			
 			FleetOventFleet fleet = new FleetOventFleet();
 			this.fleets.add(fleet);
 			
 			DataMap resourcesData = fleetData.get("resources");
 			Builder<ResourcePredicate, Double> resources = new Builder<>();
 			for(Entry<?, ?> entry : resourcesData.entrySet()) {
 				String name = (String) entry.getKey();
 				Double value;
 				if(entry.getValue() instanceof Integer) {
 					value = (double) (Integer) entry.getValue();
 				} else if(entry.getValue() instanceof Double) {
 					value = (Double) entry.getValue();
 				} else {
 					value = Double.parseDouble((String) entry.getValue());
 				}
 				ResourcePredicate predicate = getPredicateForName(name);
 				
 				resources.put(predicate, value);
 			}
 			
 			DataMap specData = fleetData.get("spec");
 			Builder<Integer, Long> specs = new Builder<>();
 			for(Entry<?, ?> entry : specData.entrySet()) {
 				int specId = (Integer) entry.getKey();
 				long count = Long.parseLong((String) entry.getValue());
 				
 				specs.put(specId, count);
 			}
 			
 			DataMap startCoordsData = fleetData.get("startCoords");
 			int startGalaxy = Integer.parseInt((String) startCoordsData.get(0));
 			int startSystem = Integer.parseInt((String) startCoordsData.get(1));
 			int startOrbit = Integer.parseInt((String) startCoordsData.get(2));
 			int startKind = Integer.parseInt((String) startCoordsData.get(3));
 			Coordinates startCoords = new StaticCoordinates(startGalaxy, startSystem, startOrbit, startKind);
 			
 			DataMap targetCoordsData = fleetData.get("targetCoords");
 			int targetGalaxy = Integer.parseInt((String) targetCoordsData.get(0));
 			int targetSystem = Integer.parseInt((String) targetCoordsData.get(1));
 			int targetOrbit = Integer.parseInt((String) targetCoordsData.get(2));
 			int targetKind;
 			try {
 				targetKind = Integer.parseInt((String) targetCoordsData.get(3));
			} catch(ClassCastException | NumberFormatException e) {
 				targetKind = 1;
 			}
 			Coordinates targetCoords = new StaticCoordinates(targetGalaxy, targetSystem, targetOrbit, targetKind);
 			
 			fleet.fleetId = Integer.parseInt((String) fleetData.get("fleetID"));
 			fleet.ownerId = Integer.parseInt((String) fleetData.get("ownerID"));
 			fleet.startPlanetId = Integer.parseInt((String) fleetData.get("startPlanetID"));
 			fleet.resources = Resources.generateStatic(resources.build());
 			fleet.startCoords = startCoords;
 			fleet.targetCoords = targetCoords;
 			fleet.specs = specRepo.getStaticSpecSet(Specs.convertToHardByLong(specs.build()));
 			fleet.cssClass = (String) fleetData.get("cssClass");
 			fleet.missionId = Integer.parseInt((String) fleetData.get("missionID"));
 			fleet.startPlanetName = (String) fleetData.get("startPlanetName");
 			fleet.passage = (String) fleetData.get("passage");
 			
 			try {
 				fleet.ofiaraId = Integer.parseInt((String) fleetData.get("ofiaraID"));
 				fleet.targetPlanetId = Integer.parseInt((String) fleetData.get("targetPlanetID"));
 				fleet.targetPlanetName = (String) fleetData.get("targetPlanetName");
			} catch(ClassCastException | NumberFormatException e) {
 				fleet.ofiaraId = 0;
 				fleet.targetPlanetId = 0;
 				fleet.targetPlanetName = "unbesiedelter Planet";
 			}
 		}
 		logger.debug("finished unserialisation of fleetovent");
 	}
 	
 	@Override
 	public String serialize() {
 		DataMap all = new ArrayDataMap();
 		
 		int i = 0;
 		for(FleetOventFleet fleet : fleets) {
 			DataMap fleetData = new ArrayDataMap();
 			all.set(i++, fleetData);
 			
 			Coordinates sc = fleet.getStartCoords();
 			Coordinates tc = fleet.getTargetCoords();
 			
 			SpecSet<?> specs = fleet.getSpecs();
 			ImmutableMap.Builder<Integer, String> specDataBuilder = new ImmutableMap.Builder<>();
 			for(Spec spec : specs.filter(new PositiveFilter())) {
 				specDataBuilder.put(spec.getPredicate().getId(), String.valueOf(spec.getCount()));
 			}
 			Map<Integer, String> specData = specDataBuilder.build();
 			
 			fleetData.set("fleetID", String.valueOf(fleet.getFleetId()));
 			fleetData.set("ownerID", String.valueOf(fleet.getOwnerId()));
 			fleetData.set("startPlanetID", String.valueOf(fleet.getStartPlanetId()));
 			fleetData.set("resources", ImmutableMap.of("metal", fleet.getResources().getValue("metal"), "crystal",
 					fleet.getResources().getValue("crystal"), "deuterium", fleet.getResources().getValue("deuterium")));
 			fleetData.set(
 					"startCoords",
 					ImmutableMap.of(0, String.valueOf(sc.getGalaxy()), 1, String.valueOf(sc.getSystem()), 2,
 							String.valueOf(sc.getOrbit()), 3, String.valueOf(sc.getKind())));
 			fleetData.set(
 					"targetCoords",
 					ImmutableMap.of(0, String.valueOf(tc.getGalaxy()), 1, String.valueOf(tc.getSystem()), 2,
 							String.valueOf(tc.getOrbit()), 3, String.valueOf(tc.getKind())));
 			fleetData.set("spec", specData);
 			fleetData.set("cssClass", fleet.getCssClass());
 			fleetData.set("missionID", String.valueOf(fleet.getMissionId()));
 			fleetData.set("startPlanetName", fleet.getStartPlanetName());
 			fleetData.set("passage", fleet.getPassage());
 			
 			fleetData.set("ofiaraID", fleet.getOfiaraId() > 0 ? String.valueOf(fleet.getOfiaraId()) : null);
 			fleetData.set("targetPlanetID", fleet.getTargetPlanetId() > 0 ? String.valueOf(fleet.getTargetPlanetId())
 					: null);
 			fleetData.set("targetPlanetName", fleet.getTargetPlanetName());
 		}
 		
 		return "a" + Parser.toString(all);
 	}
 	
 	protected ResourcePredicate getPredicateForName(String name) {
 		switch(name) {
 			case "metal":
 			case "crystal":
 			case "deuterium":
 				return Resources.getForName(name);
 			default:
 				throw new IllegalStateException();
 		}
 	}
 	
 	public static class FleetOventFleet {
 		private int fleetId;
 		private int ownerId;
 		private int ofiaraId;
 		private int startPlanetId;
 		private int targetPlanetId;
 		private ResourceValueSet resources;
 		private Coordinates startCoords;
 		private Coordinates targetCoords;
 		private SpecSet<?> specs;
 		private String cssClass;
 		private int missionId;
 		private String startPlanetName;
 		private String targetPlanetName;
 		private String passage;
 		
 		public FleetOventFleet() {
 			
 		}
 		
 		public int getFleetId() {
 			return fleetId;
 		}
 		
 		public int getOwnerId() {
 			return ownerId;
 		}
 		
 		public int getOfiaraId() {
 			return ofiaraId;
 		}
 		
 		public int getStartPlanetId() {
 			return startPlanetId;
 		}
 		
 		public int getTargetPlanetId() {
 			return targetPlanetId;
 		}
 		
 		public ResourceValueSet getResources() {
 			return resources;
 		}
 		
 		public Coordinates getStartCoords() {
 			return startCoords;
 		}
 		
 		public Coordinates getTargetCoords() {
 			return targetCoords;
 		}
 		
 		public SpecSet<?> getSpecs() {
 			return specs;
 		}
 		
 		public String getCssClass() {
 			return cssClass;
 		}
 		
 		public int getMissionId() {
 			return missionId;
 		}
 		
 		public String getStartPlanetName() {
 			return startPlanetName;
 		}
 		
 		public String getTargetPlanetName() {
 			return targetPlanetName;
 		}
 		
 		public String getPassage() {
 			return passage;
 		}
 		
 		public void setCssClass(String cssClass) {
 			this.cssClass = cssClass;
 		}
 		
 		public void setFleetId(int fleetId) {
 			this.fleetId = fleetId;
 		}
 		
 		public void setMissionId(int missionId) {
 			this.missionId = missionId;
 		}
 		
 		public void setOfiaraId(int ofiaraId) {
 			this.ofiaraId = ofiaraId;
 		}
 		
 		public void setOwnerId(int ownerId) {
 			this.ownerId = ownerId;
 		}
 		
 		public void setPassage(String passage) {
 			this.passage = passage;
 		}
 		
 		public void setResources(ResourceValueSet resources) {
 			this.resources = resources;
 		}
 		
 		public void setSpecs(SpecSet<?> specs) {
 			this.specs = specs;
 		}
 		
 		public void setStartCoords(Coordinates startCoords) {
 			this.startCoords = startCoords;
 		}
 		
 		public void setStartPlanetId(int startPlanetId) {
 			this.startPlanetId = startPlanetId;
 		}
 		
 		public void setStartPlanetName(String startPlanetName) {
 			this.startPlanetName = startPlanetName;
 		}
 		
 		public void setTargetCoords(Coordinates targetCoords) {
 			this.targetCoords = targetCoords;
 		}
 		
 		public void setTargetPlanetId(int targetPlanetId) {
 			this.targetPlanetId = targetPlanetId;
 		}
 		
 		public void setTargetPlanetName(String targetPlanetName) {
 			this.targetPlanetName = targetPlanetName;
 		}
 	}
 }
