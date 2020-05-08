 package linewars.gamestate.mapItems;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import utility.Observable;
 
 import configuration.ListConfiguration;
 import configuration.Property;
 import configuration.Usage;
 
 import linewars.gamestate.GameState;
 import linewars.gamestate.Player;
 import linewars.gamestate.Position;
 import linewars.gamestate.Transformation;
 import linewars.gamestate.shapes.configurations.CircleConfiguration;
 import linewars.gamestate.shapes.configurations.ShapeAggregateConfiguration;
 import linewars.gamestate.shapes.configurations.ShapeConfiguration;
 
 public abstract class MapItemAggregateDefinition<T extends MapItemAggregate> extends MapItemDefinition<MapItemAggregate> {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1178398010204822228L;
 	private List<MapItemDefinition<? extends MapItem>> containedItems = new ArrayList<MapItemDefinition<? extends MapItem>>();
 	private List<Transformation> relativeTrans = new ArrayList<Transformation>();
 
 	public MapItemAggregateDefinition() {
 		super();
 		super.setPropertyForName("containedItems", new Property(Usage.CONFIGURATION, 
 				new ListConfiguration<MapItemDefinition<? extends MapItem>>(new ArrayList<MapItemDefinition<? extends MapItem>>(), 
 						new ArrayList<String>(), new ArrayList<Usage>())));
 		super.setPropertyForName("relativeTrans", new Property(Usage.CONFIGURATION, 
 				new ListConfiguration<Transformation>(new ArrayList<Transformation>(), 
 						new ArrayList<String>(), new ArrayList<Usage>())));
 	}
 	
 	@Override
 	public T createMapItem(Transformation t, Player owner, GameState gameState)
 	{
 		T mia = createMapItemAggregate(t, owner, gameState);
 		for(int i = 0; i < containedItems.size(); i++)
 			mia.addMapItem(containedItems.get(i).createMapItem(new Transformation(new Position(0, 0), 0), owner, gameState), relativeTrans.get(i));
 		return mia;
 	}
 	
 	protected abstract T createMapItemAggregate(Transformation t, Player owner, GameState gameState);
 	
 	@Override
 	public ShapeConfiguration getBodyConfig()
 	{
 		//TODO this may need to be changed
 		//return new ShapeAggregateConfiguration();
 		return new CircleConfiguration(10, new Transformation(new Position(0, 0), 0));
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected final void forceSubclassReloadConfiguration()
 	{
 		if(super.getPropertyForName("containedItems") != null)
 			containedItems = ((ListConfiguration<MapItemDefinition<? extends MapItem>>)super.getPropertyForName("containedItems").getValue()).getEnabledSubList();
 		if(super.getPropertyForName("relativeTrans") != null)
 			relativeTrans = ((ListConfiguration<Transformation>)super.getPropertyForName("relativeTrans").getValue()).getEnabledSubList();
 		this.forceAggregateSubReloadConfigData();
 	}
 	
 	@Override
 	public void update(Observable o, Object obj)
 	{
 		if(o == this && obj.equals("containedItems"))
 			((Observable)this.getPropertyForName("containedItems").getValue()).addObserver(this);
 		if(o == this && obj.equals("relativeTrans"))
 			((Observable)this.getPropertyForName("relativeTrans").getValue()).addObserver(this);
		if(o == this.getPropertyForName("relativeTrans").getValue() || 
				o == this.getPropertyForName("containedItems").getValue())
 		{
 			this.setChanged();
 			this.notifyObservers("containedItems");
 		}
 		else
 			super.update(o, obj);
 	}
 	
 	protected abstract void forceAggregateSubReloadConfigData();
 	
 	public List<MapItemDefinition<? extends MapItem>> getContainedItems()
 	{
 		return containedItems;
 	}
 	
 	public List<Transformation> getRelativeTransformations()
 	{
 		return relativeTrans;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<MapItemDefinition<? extends MapItem>> getAllContainedItems()
 	{
 		return ((ListConfiguration<MapItemDefinition<? extends MapItem>>)super.getPropertyForName("containedItems").getValue()).getFullList();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Transformation> getAllRelativeTransformations()
 	{
 		return ((ListConfiguration<Transformation>)super.getPropertyForName("relativeTrans").getValue()).getFullList();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Boolean> getAllEnabledFlags()
 	{
 		return ((ListConfiguration<Boolean>)super.getPropertyForName("containedItems").getValue()).getEnabledFlags();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<String> getAllNames()
 	{
 		return ((ListConfiguration<String>)super.getPropertyForName("containedItems").getValue()).getNames();
 	}
 	
 	public void setFullContainedList(List<MapItemDefinition<? extends MapItem>> items, List<Transformation> relativeTrans, 
 										List<String> names, List<Boolean> enabledFlags)
 	{
 		List<Usage> usages = new ArrayList<Usage>();
 		for(int i = 0; i < items.size(); i++)
 			usages.add(Usage.CONFIGURATION);
 		
 		super.setPropertyForName("containedItems", new Property(Usage.CONFIGURATION, 
 				new ListConfiguration<MapItemDefinition<? extends MapItem>>(items, names, usages, enabledFlags)));
 		
 		usages.clear();
 		for(int i = 0; i < items.size(); i++)
 			usages.add(Usage.TRANSFORMATION);
 		
 		super.setPropertyForName("relativeTrans", new Property(Usage.CONFIGURATION, 
 				new ListConfiguration<Transformation>(relativeTrans, names, usages, enabledFlags)));
 	}
 	
 	@Override
 	public boolean equals(Object obj)
 	{
 		if(obj instanceof MapItemAggregateDefinition<?>)
 		{
 			MapItemAggregateDefinition<?> miad = (MapItemAggregateDefinition<?>) obj;
 			return super.equals(obj) &&
 					containedItems.equals(miad.containedItems) &&
 					relativeTrans.equals(miad.relativeTrans);
 		}
 		else
 			return false;
 	}
 
 }
