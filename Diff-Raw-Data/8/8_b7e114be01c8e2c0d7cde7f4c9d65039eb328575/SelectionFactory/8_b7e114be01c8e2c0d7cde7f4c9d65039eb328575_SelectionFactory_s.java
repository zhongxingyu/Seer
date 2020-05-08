 package com.g4.java.configuration.factories;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import com.g4.java.selection.BoltzmannSelection;
 import com.g4.java.selection.EliteSelection;
 import com.g4.java.selection.RuletteSelection;
 import com.g4.java.selection.Selection;
 import com.g4.java.selection.TournamentSelection;
 import com.g4.java.selection.UniversalSelection;
 
 public class SelectionFactory {
 
 	private Properties properties;
 	
 	public SelectionFactory(Properties properties) {
 		this.properties = properties;
 	}
 	
 	public List<Selection> loadSelectionMethods(){
 		List<Selection> selected = new ArrayList<Selection>();
 		
 		String [] selections = properties.getProperty("selection").split("/");
 		for (int i = 0; i < selections.length; i++) {
 			String selectedMethod = selections[i].trim().toUpperCase();
 
 			if (selectedMethod.equals(SelectionEnum.ELITE.getName())) {
 				int toSelect = 1;
 				if( properties.getProperty("Elite.toSelect") != null ) 
 					toSelect = Integer.valueOf(properties.getProperty("Elite.toSelect"));
 				selected.add(new EliteSelection(toSelect));
 				
 			} else if (selectedMethod.equals(SelectionEnum.RULETTE.getName())) {
 				int toSelect = 1;
 				if( properties.getProperty("Rulette.toSelect") != null ) 
 					toSelect = Integer.valueOf(properties.getProperty("Rulette.toSelect"));
 				selected.add(new RuletteSelection(toSelect));
 				
 			} else if (selectedMethod.equals(SelectionEnum.TOURNAMENT.getName())) {
 				int toSelect = 1;
 				if( properties.getProperty("Tournament.toSelect") != null ) 
 					toSelect = Integer.valueOf(properties.getProperty("Tournament.toSelect"));
 				int tSize = 1;
 				if( properties.getProperty("Tournament.tSize") != null ) 
 					tSize = Integer.valueOf(properties.getProperty("Tournament.tSize"));
 				selected.add(new TournamentSelection(toSelect, tSize));
 				
 			} else if (selectedMethod.equals(SelectionEnum.UNIVERSAL.getName())) {
 				int toSelect = 1;
 				if( properties.getProperty("Universal.toSelect") != null ) 
 					toSelect = Integer.valueOf(properties.getProperty("Universal.toSelect"));
 				selected.add(new UniversalSelection(toSelect));
 				
 			} else if (selectedMethod.equals(SelectionEnum.BOLTZMAN.getName()) ) {
 				int toSelect = 1;
 				if( properties.getProperty("Boltzman.toSelect") != null ) 
 					toSelect = Integer.valueOf(properties.getProperty("Boltzman.toSelect"));
 				
 				double maxTemperature = 1000;
 				if( properties.getProperty("Boltzman.maxTemperature") != null ) 
					maxTemperature = Integer.valueOf(properties.getProperty("Boltzman.maxTemperature"));
 				
 				double minTemperature = 100;
 				if( properties.getProperty("Boltzman.minTemperature") != null ) 
					minTemperature = Integer.valueOf(properties.getProperty("Boltzman.minTemperature"));
 				
 				double decrement = 5;
 				if( properties.getProperty("Boltzman.decrement") != null ) 
					decrement = Integer.valueOf(properties.getProperty("Boltzman.decrement"));
 				
 				selected.add(new BoltzmannSelection(maxTemperature, minTemperature, decrement, toSelect));
 			}
 		}
 		
 		return selected;
 	}
 }
