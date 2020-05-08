 package editor.tech.modifierEditors;
 
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import linewars.gamestate.Race;
 import linewars.gamestate.tech.ModifierConfiguration;
 import linewars.gamestate.tech.TechConfiguration;
 import configuration.Configuration;
 import configuration.Property;
 import configuration.Usage;
 import editor.BigFrameworkGuy;
 import editor.BigFrameworkGuy.ConfigType;
 import editor.ConfigurationEditor;
 import editor.GenericSelector;
import editor.GenericSelector.CustomToString;
 
 /**
  * Wraps the top-level ModificationEditor, asking the player to choose a race before allowing them to start setting up the guts of the Tech.
  * 
  * @author Knexer
  *
  */
 public class RaceChooser implements ConfigurationEditor {
 	
 	private Race selectedRace;
 	private NewModifierEditor subEditor;
 	
 	private JPanel myPanel;
 	
 	private BigFrameworkGuy bfg;
 	
 	public RaceChooser(BigFrameworkGuy bfg){
 		this.bfg = bfg;
 	}
 
 	@Override
 	public void setData(Configuration cd) {
 		//cast
 		if(!(cd instanceof TechConfiguration)){
 			throw new IllegalArgumentException("The provided Configuration object is not a TechConfiguration object.");
 		}
 		TechConfiguration toCopy = (TechConfiguration) cd;
 		
 		//get the race being modified
 		selectedRace = toCopy.getRace();
 		
 		//if no race is defined for this tech
 		if(selectedRace == null){
 			//set up a panel so the user can pick a race
 			myPanel = constructRaceChooserPanel();
 		}else{
 			//otherwise make this transparent
 			myPanel = constructSubEditorPanel(toCopy.getModification());
 		}
 	}
 
 	private JPanel constructSubEditorPanel(ModifierConfiguration modification) {
 		//If no modification has yet been defined (This probably shouldn't happen, but just in case...
 		if(modification == null){
 			//define an empty modification that can contain multiple sub-modifications
 			modification = new MultipleSubModificationModification();
 		}
 		Class<? extends NewModifierEditor> editorType = NewModifierEditor.getEditorForModifier(modification.getClass());
 		try {
 			subEditor = editorType.getConstructor(Property.class).newInstance(new Property(Usage.CONFIGURATION, selectedRace));
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException("Something went wrong invoking the one-argument constructor of " + editorType + " that should exist but might not.");
 		}
 		
 		subEditor.setData(modification);
 		
 		return subEditor.getPanel();
 	}
 
 	private JPanel constructRaceChooserPanel() {
 		JPanel ret = new JPanel();
 		
		GenericSelector<Race> selector = new GenericSelector<Race>("Choose a race for this Tech to modify.", new GenericSelector.SelectConfigurations<Race>(bfg, ConfigType.race),
				new GenericSelector.ShowBFGName<Race>());
 		ret.add(selector);
 		selector.addSelectionChangeListener(new GenericSelector.SelectionChangeListener<Race>() {
 
 			@Override
 			public void selectionChanged(Race newSelection) {
 				selectedRace = newSelection;
 				myPanel = constructSubEditorPanel(null);
 			}
 		});
 		return ret;
 	}
 	
 	public void resetEditor()
 	{
 		myPanel = constructRaceChooserPanel();
 	}
 
 	@Override
 	public Configuration instantiateNewConfiguration() {
 		return null;
 	}
 
 	@Override
 	public ConfigType getData(Configuration toSet) {
 		if(!(toSet instanceof TechConfiguration)){
 			throw new IllegalArgumentException("The provided Configuration object is not a TechConfiguration object.");
 		}
 		TechConfiguration target = (TechConfiguration) toSet;
 		target.setRace(selectedRace);
 		if(subEditor != null){
 			target.setModification(subEditor.getData());
 		}else{
 			target.setModification(null);
 		}
 		return ConfigType.tech;
 	}
 
 	@Override
 	public List<ConfigType> getAllLoadableTypes() {
 		throw new UnsupportedOperationException("This isn't supposed to be used directly by the BFG.");
 	}
 
 	@Override
 	public JPanel getPanel() {
 		return myPanel;
 	}
 }
