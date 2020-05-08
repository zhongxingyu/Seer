 package de.abbaddie.wot.fleet.data.fleet;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.MapKey;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.google.common.collect.ImmutableMap;
 
 import de.abbaddie.wot.data.coordinates.AbstractCoordinates;
 import de.abbaddie.wot.data.coordinates.Coordinates;
 import de.abbaddie.wot.data.event.Event;
 import de.abbaddie.wot.data.event.EventImpl;
 import de.abbaddie.wot.data.planet.Planet;
 import de.abbaddie.wot.data.planet.PlanetImpl;
 import de.abbaddie.wot.data.resource.CrystalResource;
 import de.abbaddie.wot.data.resource.DeuteriumResource;
 import de.abbaddie.wot.data.resource.MetalResource;
 import de.abbaddie.wot.data.resource.ResourceMapper;
 import de.abbaddie.wot.data.resource.ResourcePredicate;
 import de.abbaddie.wot.data.resource.ResourceValueSet;
 import de.abbaddie.wot.data.resource.Resources;
 import de.abbaddie.wot.data.spec.SpecMapper;
 import de.abbaddie.wot.data.spec.SpecPredicate;
 import de.abbaddie.wot.data.spec.SpecSet;
 import de.abbaddie.wot.fleet.data.mission.Mission;
 import de.abbaddie.wot.fleet.data.mission.MissionRepository;
 import de.abbaddie.wot.fleet.data.spec.trait.FleetBound;
 
 @Configurable
 @Entity
 @Table(name = "ugml_fleet")
 // TODO besseres decoupling der planeten/events?
 class FleetImpl implements EditableFleet {
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "fleetID")
 	protected int id;
 	
 	@Column(name = "ownerID")
 	protected int ownerId;
 	
 	@Column(name = "ofiaraID")
 	protected Integer ofiaraId;
 	
 	@ManyToOne(fetch = FetchType.LAZY, targetEntity = PlanetImpl.class)
 	@JoinColumn(name = "startPlanetID")
 	protected Planet startPlanet;
 	
 	@ManyToOne(fetch = FetchType.LAZY, targetEntity = PlanetImpl.class)
 	@JoinColumn(name = "targetPlanetID")
 	protected Planet targetPlanet;
 	
 	@OneToMany(cascade = CascadeType.ALL)
 	@MapKey(name = "spec")
 	@JoinColumn(name = "fleetID")
 	protected Map<SpecPredicate, FleetSpec> specs = new HashMap<>();
 	
 	@Column(name = "missionID")
 	protected int missionId;
 	
 	protected long metal;
 	protected long crystal;
 	protected long deuterium;
 	
 	@Embedded
 	protected FleetCoordinates coords;
 	
 	protected int impactTime;
 	protected int returnTime;
 	
 	@OneToOne(targetEntity = EventImpl.class)
 	@JoinColumn(name = "impactEventID")
 	protected Event impactEvent;
 	
 	@OneToOne(targetEntity = EventImpl.class)
 	@JoinColumn(name = "returnEventID")
 	protected Event returnEvent;
 	
 	protected transient SpecSet<FleetBound> specSet;
 	
 	@Autowired
 	protected transient MissionRepository missionRepo;
 	
 	public FleetImpl() {
 	}
 	
 	// getter
 	@Override
 	public int getId() {
 		return id;
 	}
 	
 	@Override
 	public SpecSet<FleetBound> getSpecs() {
 		return specSet;
 	}
 	
 	Map<SpecPredicate, FleetSpec> getSpecLevels() {
 		return specs;
 	}
 	
 	@Override
 	public Planet getStartPlanet() {
 		return startPlanet;
 	}
 	
 	@Override
 	public Planet getTargetPlanet() {
 		return targetPlanet;
 	}
 	
 	@Override
 	public Mission getMission() {
 		return missionRepo.findOne(missionId);
 	}
 	
 	@Override
 	public Coordinates getCoordinates() {
 		return coords;
 	}
 	
 	@Override
 	public ResourcePredicate[] getAllowedResourcePredicates() {
 		return new ResourcePredicate[]{new MetalResource(), new CrystalResource(), new DeuteriumResource()};
 	}
 	
 	@Override
 	public ResourceValueSet getResources() {
 		return Resources.generate(getAllowedResourcePredicates(), new FleetResourceMapper());
 	}
 	
 	@Override
 	public Map<FleetEventType, Event> getEvents() {
 		ImmutableMap.Builder<FleetEventType, Event> builder = new ImmutableMap.Builder<>();
 		
 		if(impactEvent != null) {
 			builder.put(DefaultEventTypes.IMPACT, impactEvent);
 		}
 		builder.put(DefaultEventTypes.RETURN, returnEvent);
 		
 		return builder.build();
 	}
 	
 	// setter
 	@Override
 	public void setSpecs(SpecSet<? extends FleetBound> specs) {
 		specSet.replace(specs);
 	}
 	
 	void setSpecsInternal(SpecSet<FleetBound> specs) {
 		specSet = specs;
 	}
 	
 	@Override
 	public void setCoordinates(Coordinates coords) {
 		this.coords = new FleetCoordinates(coords);
 	}
 	
 	@Override
 	public void setMission(Mission mission) {
 		missionId = mission.getMissionId();
 	}
 	
 	@Override
 	public void setResources(ResourceValueSet resources) {
 		metal = (long) resources.getValue("metal");
 		crystal = (long) resources.getValue("crystal");
 		deuterium = (long) resources.getValue("deuterium");
 	}
 	
 	@Override
 	public void setStartPlanet(Planet planet) {
 		startPlanet = planet;
 		ownerId = planet.getOwner().getId();
 	}
 	
 	@Override
 	public void setTargetPlanet(Planet planet) {
 		targetPlanet = planet;
 		ofiaraId = planet == null ? null : planet.getOwner().getId();
 	}
 	
 	@Override
 	public void setEvent(FleetEventType type, Event event) {
 		if(type == DefaultEventTypes.IMPACT) {
 			impactEvent = event;
 			if(event != null) {
 				impactTime = (int) (event.getTime().getMillis() / 1000);
 			}
 		} else if(type == DefaultEventTypes.RETURN) {
 			returnEvent = event;
 			returnTime = (int) (event.getTime().getMillis() / 1000);
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 	
 	// classes
 	@Embeddable
 	protected static class FleetCoordinates extends AbstractCoordinates {
 		protected int galaxy;
 		protected int system;
 		protected int planet;
 		
 		public FleetCoordinates(int galaxy, int system, int planet) {
 			this.galaxy = galaxy;
 			this.system = system;
 			this.planet = planet;
 		}
 		
 		public FleetCoordinates(Coordinates c) {
 			galaxy = c.getGalaxy();
 			system = c.getSystem();
 			planet = c.getOrbit();
 		}
 		
 		public FleetCoordinates() {
 			
 		}
 		
 		@Override
 		public int getGalaxy() {
 			return galaxy;
 		}
 		
 		@Override
 		public int getSystem() {
 			return system;
 		}
 		
 		@Override
 		public int getOrbit() {
 			return planet;
 		}
 		
 		@Override
 		public int getKind() {
 			return 1;
 		}
 	}
 	
 	protected class FleetResourceMapper implements ResourceMapper {
 		@Override
 		public double getCount(ResourcePredicate predicate) {
 			if(predicate instanceof MetalResource) {
 				return metal;
 			} else if(predicate instanceof CrystalResource) {
 				return crystal;
 			} else if(predicate instanceof DeuteriumResource) {
 				return deuterium;
 			} else {
 				throw new IllegalArgumentException("given predicate is not supported");
 			}
 		}
 		
 		@Override
 		public void setCount(ResourcePredicate predicate, double count) {
 			if(predicate instanceof MetalResource) {
 				metal = (long) count;
 			} else if(predicate instanceof CrystalResource) {
 				crystal = (long) count;
 			} else if(predicate instanceof DeuteriumResource) {
 				deuterium = (long) count;
 			} else {
 				throw new IllegalArgumentException("given predicate is not supported");
 			}
 		}
 	}
 	
 	protected class FleetSpecMapper implements SpecMapper {
 		@Override
 		public long getLevel(SpecPredicate predicate) {
 			FleetSpec spec = specs.get(predicate);
 			
 			if(spec == null) {
 				return 0;
 			} else {
 				return spec.getCount();
 			}
 		}
 		
 		@Override
 		public void setLevel(SpecPredicate predicate, long level) {
 			FleetSpec spec = specs.get(predicate);
 			
 			if(spec == null) {
 				specs.put(predicate, new FleetSpec(FleetImpl.this, predicate, level));
 			} else {
 				specs.get(predicate).setCount(level);
 			}
 		}
 	}
 }
