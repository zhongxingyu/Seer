 package linewars.gamestate.mapItems;
 
 import java.util.ArrayList;
 import java.util.List;
 import utility.Observable;
 import utility.Observer;
 
 import linewars.display.DisplayConfiguration;
 import linewars.gamestate.GameState;
 import linewars.gamestate.Player;
 import linewars.gamestate.Transformation;
 import linewars.gamestate.mapItems.abilities.AbilityDefinition;
 import linewars.gamestate.mapItems.strategies.collision.CollisionStrategyConfiguration;
 import linewars.gamestate.shapes.ShapeConfiguration;
 import configuration.Configuration;
 import configuration.ListConfiguration;
 import configuration.Property;
 import configuration.Usage;
 
 /**
  * 
  * @author , Connor Schenck
  *
  * This class represents a definition for a map item. It is used
  * to create the map items it defines (similar to the way a class
  * is used to define how to create objects of its type). For the
  * map items it creates, it knows what states they are allowed to
  * be in, what their name is, what parser they use, what abilities
  * they are allowed to use, who owns them, and what collision strategy
  * they use.
  */
 public strictfp abstract class MapItemDefinition<T extends MapItem> extends Configuration implements Observer {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1578880084450994552L;
 	private ArrayList<MapItemState> validStates;
 	private String name;
 	protected ArrayList<AbilityDefinition> abilities;
 	protected CollisionStrategyConfiguration cStrat;
 	protected ShapeConfiguration body;
 	
 	private Configuration displayConfig;
 	
 	/**
 	 * Creates a map item definition.
 	 */
 	public MapItemDefinition()
 	{
 		super.setPropertyForName("validStates", 
 				new Property(Usage.CONFIGURATION, 
 						new ListConfiguration<MapItemState>(new ArrayList<MapItemState>(), 
 								new ArrayList<String>(), new ArrayList<Usage>())));
 		super.setPropertyForName("name", new Property(Usage.STRING, ""));
 		super.setPropertyForName("abilities", new Property(Usage.CONFIGURATION,
 				new ListConfiguration<AbilityDefinition>(new ArrayList<AbilityDefinition>(), 
 						new ArrayList<String>(), new ArrayList<Usage>())));
 		super.setPropertyForName("cStrat", new Property(Usage.CONFIGURATION, null));
 		super.setPropertyForName("body", new Property(Usage.CONFIGURATION, null));
 		super.setPropertyForName("displayConfig", new Property(Usage.CONFIGURATION));
 		this.addObserver(this);
 	}
 
 	/**
 	 * Checks to see if the given state is valid for this type of map item
 	 * 
 	 * @param m	the state
 	 * @return	true if its valid, false otherwise
 	 */
 	public boolean isValidState(MapItemState m)
 	{
 		return validStates.contains(m);
 	}
 	
 	/**
 	 * 
 	 * @return	the name of the map items
 	 */
 	public String getName()
 	{
 		return name;
 	}
 	
 	/**
 	 * 
 	 * @return	the list of availabel ability definitions
 	 */
 	public List<AbilityDefinition> getAbilityDefinitions()
 	{
 		return abilities;
 	}
 	
 	/**
 	 * 
 	 * @return	the collision strategy associated with this type of map item
 	 */
 	public CollisionStrategyConfiguration getCollisionStrategyConfig()
 	{
 		return cStrat;
 	}
 	
 	/**
 	 * 
 	 * @return	the shape aggregate associated with this map item
 	 */
 	public ShapeConfiguration getBodyConfig()
 	{
 		return body;
 	}
 	
 	public List<MapItemState> getValidStates()
 	{
 		return validStates;
 	}
 	
 	public Configuration getDisplayConfiguration()
 	{
 		return displayConfig;
 	}
 	
 	public void setName(String name)
 	{
 		super.setPropertyForName("name", new Property(Usage.STRING, name));
 	}
 	
 	public void setValidStates(List<MapItemState> states)
 	{
 		ArrayList<MapItemState> validStates = new ArrayList<MapItemState>(states); 
 		ArrayList<String> names = new ArrayList<String>();
 		ArrayList<Usage> usages = new ArrayList<Usage>();
 		for(MapItemState mis : states)
 		{
 			names.add(mis.toString());
 			usages.add(Usage.IMMUTABLE);
 		}
 		super.setPropertyForName("validStates", new Property(
 				Usage.CONFIGURATION, new ListConfiguration<MapItemState>(
 						validStates, names, usages)));
 	}
 	
 	public void setAbilities(List<AbilityDefinition> abilities)
 	{
		ArrayList<AbilityDefinition> validAbilites = new ArrayList<AbilityDefinition>(abilities); 
 		ArrayList<String> names = new ArrayList<String>();
 		ArrayList<Usage> usages = new ArrayList<Usage>();
		for(AbilityDefinition ad : abilities)
 		{
 			String name = ad.getName();
 			while(names.contains(name))
 				name += "_";
 			names.add(name);
 			usages.add(Usage.CONFIGURATION);
 		}
 		super.setPropertyForName("abilities", new Property(
 				Usage.CONFIGURATION, new ListConfiguration<AbilityDefinition>(
						validAbilites, names, usages)));
 	}
 	
 	public void setCollisionStrategy(CollisionStrategyConfiguration csc)
 	{
 		super.setPropertyForName("cStrat", new Property(Usage.CONFIGURATION, csc));
 	}
 	
 	public void setBody(ShapeConfiguration sc)
 	{
 		super.setPropertyForName("body", new Property(Usage.CONFIGURATION, sc));
 	}
 	
 	public void setDisplayConfiguration(DisplayConfiguration dc)
 	{
 		super.setPropertyForName("displayConfig", new Property(Usage.CONFIGURATION, dc));
 	}
 	
 	public abstract T createMapItem(Transformation t, Player owner, GameState gameState);
 	
 	@Override
 	public void update(Observable obs, Object o)
 	{
 		if(obs == this)
 			this.forceReloadConfiguration();
 		else if(obs instanceof ShapeConfiguration)
 			update(this, "body");
 	}
 	
 	/**
 	 * Forces this definition to reload itself from its config
 	 */
 	@SuppressWarnings("unchecked")
 	public void forceReloadConfiguration()
 	{
 		validStates = ((ListConfiguration<MapItemState>)super.getPropertyForName("validStates").getValue()).getEnabledSubList();
 		name = (String)super.getPropertyForName("name").getValue();
 		abilities = ((ListConfiguration<AbilityDefinition>)super.getPropertyForName("abilities").getValue()).getEnabledSubList();
 		cStrat = (CollisionStrategyConfiguration)super.getPropertyForName("cStrat").getValue();
 		body = (ShapeConfiguration)super.getPropertyForName("body").getValue();
 		displayConfig = (Configuration)super.getPropertyForName("displayConfig").getValue();
 		
 		if(body != null)
 			body.addObserver(this);
 		
 		this.forceSubclassReloadConfiguration();
 	}
 	
 	/**
 	 * Forces any subclass of MapItemDefinition to reload itself from its config
 	 */
 	protected abstract void forceSubclassReloadConfiguration();
 	
 	@Override
 	public boolean equals(Object obj)
 	{
 		if(obj instanceof MapItemDefinition<?>)
 		{
 			MapItemDefinition<?> mid = (MapItemDefinition<?>) obj;
 			try {
 				return validStates.equals(mid.validStates) &&
 						name.equals(mid.name) &&
 						abilities.equals(mid.abilities) &&
 						cStrat.equals(mid.cStrat) &&
 						body.equals(mid.body);
 			} catch(NullPointerException e) {
 				return false;
 			}
 		}
 		else
 			return false;
 	}
 	
 }
