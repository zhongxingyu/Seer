 package editor.mapitems;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.*;
 import javax.swing.event.*;
 
 import linewars.configfilehandler.ConfigData;
 import linewars.configfilehandler.ConfigFileWriter;
 import linewars.configfilehandler.ParserKeys;
 import linewars.gamestate.mapItems.MapItemState;
 import editor.BigFrameworkGuy;
 import editor.ConfigurationEditor;
 import editor.ListURISelector;
 import editor.ListURISelector.ListSelectorOptions;
 import editor.URISelector;
 import editor.URISelector.SelectorOptions;
 import editor.mapitems.StrategySelector.StrategySelectorCallback;
 
 /**
  * 
  * @author Connor Schenck
  *
  */
 public class MapItemEditor extends JPanel implements ConfigurationEditor, ActionListener {
 	
 	//variable for the name
 	private JTextField name;
 	
 	//variables for the states and corresponding animations
 	private ListURISelector validStates;
 	private URISelector animations;
 	private HashMap<MapItemState, String> animationMap = new HashMap<MapItemState, String>();
 	
 	//variable for the abilities and collision strategies
 	private ListURISelector abilities;
 	private URISelector collisionStrat;
 	
 	//variables for setting the body
 	private JButton bodyButton;
 	private JLabel bodyStatus;
 	private ConfigData bodyConfig;
 	
 	//variables related to the specific type
 	private URISelector mapItemType;
 	private ConfigurationEditor mapItemTypeInfo;
 	
 	private BigFrameworkGuy bfg;
 	
 	public MapItemEditor(BigFrameworkGuy guy)
 	{
 		bfg = guy;
 		
 		//set up the name panel
 		JPanel namePanel = new JPanel();
 		namePanel.add(new JLabel("Name"));
 		name = new JTextField();
 		name.setColumns(20);
 		namePanel.add(name);
 		
 		//set up the states panel
 		validStates = new ListURISelector("Valid States", new MapItemStateSelector());
 		animations = new URISelector("Animation", new AnimationSelector());
 		JPanel statesPanel = new JPanel();
 		statesPanel.add(validStates);
 		statesPanel.add(animations);
 		
 		//set up the abilities and collision strat panel
 		abilities = new ListURISelector("Abilities", new AbilitySelector());
 		JPanel aPanel = new JPanel();
 		aPanel.add(abilities);
 		collisionStrat = new URISelector("Collision Strategy", new CollisionStrategySelector());
 		JPanel cPanel = new JPanel();
 		cPanel.add(collisionStrat);
 		
 		//set up the body panel
 		bodyButton = new JButton("Set the body...");
 		bodyButton.addActionListener(this);
 		bodyStatus = new JLabel("Not Set");
 		JPanel bodyPanel = new JPanel();
 		bodyPanel.add(bodyButton);
 		bodyPanel.add(bodyStatus);
 		
 		//set up the map item type selector
 		mapItemType = new URISelector("Type", new MapItemTypeSelector());
 		
 		//set up the main panel
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		this.add(namePanel);
 		this.add(statesPanel);
 		this.add(aPanel);
 		this.add(cPanel);
 		this.add(bodyPanel);
 		this.add(mapItemType);
 		
 		this.reset();
 		
 		this.setPreferredSize(new Dimension(800, 600));
 	}
 	
 	public String getAnimation(MapItemState key)
 	{
 		return animationMap.get(key);
 	}
 	
 	public void setBody(ConfigData cd)
 	{
 		bodyConfig = cd;
 		bodyStatus.setText("Set");
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2434216613579213750L;
 
 	@Override
 	public void setData(ConfigData cd) {
 		setData(cd, false);
 	}
 
 	@Override
 	public void forceSetData(ConfigData cd) {
 		setData(cd, true);
 	}
 	
 	private void setData(ConfigData cd, boolean force)
 	{
 		//look for the name
 		if(cd.getDefinedKeys().contains(ParserKeys.name))
 			name.setText(cd.getString(ParserKeys.name));
 		else if(force)
 			name.setText("");
 		else
 			throw new IllegalArgumentException("Name not defined");
 		
 		//look for the valid states
 		if(cd.getDefinedKeys().contains(ParserKeys.ValidStates))
 		{
 			if(!cd.getStringList(ParserKeys.ValidStates).contains(MapItemState.Idle.toString()))
 			{
 				if(force)
 					cd.add(ParserKeys.ValidStates, MapItemState.Idle.toString());
 				else
 					throw new IllegalArgumentException("Idle is not defined");
 			}
 				
 			validStates.setSelectedURIs(cd.getStringList(ParserKeys.ValidStates).toArray(new String[0]));
 		}
 		else if(force)
 			validStates.setSelectedURIs(new String[0]);
 		else 
 			throw new IllegalArgumentException("Valid States are not defined");
 		
 		animationMap.clear();
 		//set up the animations
 		for(String state : validStates.getSelectedURIs())
 		{
 			ParserKeys key = ParserKeys.valueOf(state);
 			MapItemState s = MapItemState.valueOf(state);
 			if(cd.getDefinedKeys().contains(key))
 				animationMap.put(s, cd.getString(key));
 			else if(!force)
 				throw new IllegalArgumentException(s.toString() + " has no animation defined");
 		}
 		
 		//set up the abilities
 		if(cd.getDefinedKeys().contains(ParserKeys.abilities))
 			abilities.setSelectedURIs(cd.getStringList(ParserKeys.abilities).toArray(new String[0]));
 		else  //NOTE: the config files do not require abilities to be defined if there are none
 			abilities.setSelectedURIs(new String[0]);
 		
 		//load the collision strategy NOTE: collision strategies atm only define type
 		if(cd.getDefinedKeys().contains(ParserKeys.collisionStrategy))
 			collisionStrat.setSelectedURI(cd.getConfig(ParserKeys.collisionStrategy).getString(ParserKeys.type));
 		else if(force)
 			collisionStrat.setSelectedURI("");
 		else
 			throw new IllegalArgumentException("Collision strategy is not defined");
 		
 		if(cd.getDefinedKeys().contains(ParserKeys.body))
 		{
 			bodyConfig = cd.getConfig(ParserKeys.body);
 			bodyStatus.setText("Set");
 		}
 		else if(force)
 		{
 			bodyConfig = null;
 			bodyStatus.setText("Not Set");
 		}
 		else
 			throw new IllegalArgumentException("The Body is not defined");
 		
 		if(mapItemTypeInfo != null)
 			this.remove(mapItemTypeInfo.getPanel());
 		//now we need to figure out what type of map item this is
 		//first check to see if its a gate, only gates have coefficients :) hi taylor!
 		if(cd.getDefinedKeys().contains(ParserKeys.coefficients))
 		{
 			mapItemTypeInfo = new GateEditor();
 			mapItemType.setSelectedURI("Gate");
 		}
 		else if(cd.getDefinedKeys().contains(ParserKeys.maxHP)) //we're a unit!
 		{
 			mapItemTypeInfo = new UnitEditorPanel();
 			mapItemType.setSelectedURI("Unit");
 		}
 		else if(cd.getDefinedKeys().contains(ParserKeys.cost)) //we're a building!
 		{
 			mapItemTypeInfo = new BuildingEditor();
 			mapItemType.setSelectedURI("Building");
 		}
 		else if(cd.getDefinedKeys().contains(ParserKeys.velocity))
 		{
 			mapItemTypeInfo = new ProjectileEditor();
 			mapItemType.setSelectedURI("Projectile");
 		}
 		else if(force)
 		{
 			mapItemTypeInfo = null;
 			mapItemType.setSelectedURI("");
 		}
 		else
 			throw new IllegalArgumentException("Invalid config data");
 		
 		if(mapItemTypeInfo != null)
 		{
 			if(force)
 				mapItemTypeInfo.forceSetData(cd);
 			else
 				mapItemTypeInfo.setData(cd);
 			
 			mapItemTypeInfo.getPanel().setBorder(BorderFactory.createBevelBorder(1));
 			this.add(mapItemTypeInfo.getPanel());
 		}
 	}
 
 	@Override
 	public void reset() {
 		//reset the name
 		name.setText("");
 		
 		//reset the states and animations
 		validStates.setSelectedURIs(new String[]{MapItemState.Idle.toString()});
 		animationMap.clear();
 		animations.setSelectedURI("");
 		
 		//reset the abilities and collision strat
 		abilities.setSelectedURIs(new String[0]);
 		collisionStrat.setSelectedURI("");
 		
 		//reset the body config
 		bodyConfig = null;
 		bodyStatus.setText("Not Set");
 		
 		//resets the map item type
 		mapItemType.setSelectedURI("");
 		if(mapItemTypeInfo != null)
 			this.remove(mapItemTypeInfo.getPanel());
 	}
 
 	@Override
 	public ConfigData getData() {
 		ConfigData cd = new ConfigData();
 		
 		//if the type is specified, then use the parser from that
 		if(mapItemTypeInfo != null)
 			cd = mapItemTypeInfo.getData();
 		
 		//add the name
 		if(!name.getText().equals(""))
 			cd.set(ParserKeys.name, name.getText());
 		
 		//add the valid states and their animations
 		for(String s : validStates.getSelectedURIs())
 		{
 			cd.add(ParserKeys.ValidStates, s);
 			MapItemState state = MapItemState.valueOf(s);
 			if(animationMap.containsKey(state))
 				cd.set(ParserKeys.valueOf(state.toString()), animationMap.get(state));
 		}
 		
 		//get the abilities
 		if(abilities.getSelectedURIs().length > 0)
 			cd.set(ParserKeys.abilities, abilities.getSelectedURIs());
 		
 		//add the collision strat
 		if(!collisionStrat.getSelectedURI().equals(""))
 		{
 			ConfigData col = new ConfigData();
 			col.set(ParserKeys.type, collisionStrat.getSelectedURI());
 			cd.set(ParserKeys.collisionStrategy, col);
 		}
 		
 		//set the body
 		if(bodyConfig != null)
 			cd.set(ParserKeys.body, bodyConfig);
 		
 		return cd;
 	}
 
 	@Override
 	public ParserKeys getType() {
 		if(mapItemTypeInfo != null)
 			return mapItemTypeInfo.getType();
 		else //use unit as default type
 			return ParserKeys.unitURI;
 	}
 
 	@Override
 	public boolean isValidConfig() {
 		if(name == null)
 			return false;
 		
 		//if the type is specified, then use the parser from that
 		if(mapItemTypeInfo != null)
 		{
 			if(!mapItemTypeInfo.isValidConfig())
 				return false;
 		}
 		else
 			return false;
 		
 		//check the name
 		if(name.getText().equals(""))
 			return false;
 		
 		List<String> vs = new ArrayList<String>();
 		for(String s : validStates.getSelectedURIs())
 			vs.add(s);
 		
 		//make sure idle is defined
 		if(!vs.contains("Idle"))
 			return false;
 		
 		//units and projectiles need dead
 		if((mapItemType.getSelectedURI().equalsIgnoreCase("Unit") 
 				|| mapItemType.getSelectedURI().equalsIgnoreCase("Projectile")
 				|| mapItemType.getSelectedURI().equalsIgnoreCase("Gate"))
 				&& !vs.contains("Dead"))
 				return false;
 		
 		//check to make sure the animations are defined
 		for(String s : validStates.getSelectedURIs())
 		{
 			MapItemState state = MapItemState.valueOf(s);
 			if(!animationMap.containsKey(state))
 				return false;
 		}
 		
 		//add the collision strat
 		if(collisionStrat.getSelectedURI().equals(""))
 			return false;
 		
 		//set the body
 		if(bodyConfig == null)
 			return false;
 		
 		return true;
 	}
 	
 	@Override
 	public JPanel getPanel() {
 		return this;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if(arg0.getSource().equals(bodyButton))
 		{
 			if(animationMap.get(MapItemState.Idle) == null)
 				return;
 			BodyEditor be = new BodyEditor(this);
 			if(bodyConfig != null)
 				be.setData(bodyConfig);
 		}
 	}
 	
 	private class MapItemStateSelector implements ListSelectorOptions {
 
 		@Override
 		public String[] getOptions() {
 			String[] options = new String[MapItemState.values().length];
 			for(int i = 0; i < options.length; i++)
 				options[i] = MapItemState.values()[i].toString();
 			return options;
 		}
 
 		@Override
 		public void uriSelected(String uri) {
 			getPanel().validate();	
 			getPanel().updateUI();
 		}
 
 		@Override
 		public void uriRemoved(String uri) {
 		}
 
 		@Override
 		public void uriHighlightChange(String[] uris) {
 			MapItemState key = null;
 			if(uris.length > 0)
 				key = MapItemState.valueOf(uris[0]);
 			if(uris.length != 1 || animationMap.get(key) == null)
 				animations.setSelectedURI("");
 			else
 				animations.setSelectedURI(animationMap.get(key));
 		}
 		
 	}
 	
 	private class AnimationSelector implements SelectorOptions {
 
 		@Override
 		public String[] getOptions() {
 			return bfg.getAnimationURIs();
 		}
 
 		@Override
 		public void uriSelected(String uri) {
 			String[] highlighted = validStates.getHighlightedURIs();
 			if(highlighted.length != 1)
 				animations.setSelectedURI("");
 			else
 				animationMap.put(MapItemState.valueOf(highlighted[0]), animations.getSelectedURI());
 		}
 		
 	}
 	
 	private class AbilitySelector implements ListSelectorOptions {
 
 		@Override
 		public String[] getOptions() {
 			return bfg.getAbilityURIs();
 		}
 
 		@Override
 		public void uriSelected(String uri) {
 			getPanel().validate();
 			getPanel().updateUI();
 		}
 
 		@Override
 		public void uriRemoved(String uri) {
 		}
 
 		@Override
 		public void uriHighlightChange(String[] uris) {
 		}
 		
 	}
 	
 	private class CollisionStrategySelector implements SelectorOptions {
 
 		@Override
 		public String[] getOptions() {
 			return new String[]{"AllEnemies", "CollidesWithAll", "Ground", "NoCollision", "AllEnemyUnits"};
 		}
 
 		@Override
 		public void uriSelected(String uri) {
 						
 		}
 		
 	}
 	
 	private class MapItemTypeSelector implements SelectorOptions {
 
 		@Override
 		public String[] getOptions() {
 			return new String[]{"Unit", "Building", "Projectile", "Gate"};
 		}
 
 		@Override
 		public void uriSelected(String uri) {
 			if(mapItemTypeInfo != null)
 				MapItemEditor.this.remove(mapItemTypeInfo.getPanel());
 			
 			if(uri.equalsIgnoreCase("Unit"))
 				mapItemTypeInfo = new UnitEditorPanel();
 			else if(uri.equalsIgnoreCase("Building"))
 				mapItemTypeInfo = new BuildingEditor();
 			else if(uri.equalsIgnoreCase("Projectile"))
 				mapItemTypeInfo = new ProjectileEditor();
 			else if(uri.equalsIgnoreCase("Gate"))
 				mapItemTypeInfo = new GateEditor();
 			
 			//make sure the dead state is on the list
 			if(uri.equalsIgnoreCase("Unit") ||
 					uri.equalsIgnoreCase("Projectile") || 
 					uri.equalsIgnoreCase("Gate"))
 			{
 				String[] states = validStates.getSelectedURIs();
 				boolean found = false;
 				for(String s : states)
 					if(s.equals("Dead"))
 						found = true;
 				if(!found)
 				{
 					states = Arrays.copyOf(states, states.length + 1);
 					states[states.length - 1] = "Dead";
 					validStates.setSelectedURIs(states);
 				}
 			}
 			
 			mapItemTypeInfo.getPanel().setBorder(BorderFactory.createBevelBorder(1));
 			MapItemEditor.this.add(mapItemTypeInfo.getPanel());
 			MapItemEditor.this.validate();
 			MapItemEditor.this.updateUI();
 		}
 		
 	}
 
 }
