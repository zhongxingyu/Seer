 /**
  * 
  */
 package de.thm.mni.nn.ui.actions;
 
 import java.util.Set;
 
 import de.thm.mni.nn.model.DataStore;
 import de.thm.mni.nn.ui.Action;
 import de.thm.mni.nn.ui.UserInterface;
 
 /**
  * Lists all Patterns or Perceptrons in the DataStore.
  * @author Alexander Schulz
  *
  */
 public class Action_list extends Action {
 
 	/**
 	 * @param ds
 	 * @param ui
 	 */
 	public Action_list(DataStore ds, UserInterface ui) {
 		super(ds, ui);
 	}
 
 	/**
 	 * Gives out a List of all Patterns or Perceptrons.
 	 */
 	@Override
 	public void callAction(String args) { //list [Pattern, Perceptrons]
		if (args == null) {
			ui.printToConsole("Error: Syntax is list [patterns|perceptrons]");
			return;
		}
 		if (args.equalsIgnoreCase("Patterns")) {
 			Set<String> patternNames = ds.getPatternNames();
 			if (patternNames.size() == 0) {
 				ui.printToConsole("There are no Patterns available in the DataStore.");
 			} else {
 				ui.printToConsole("The following Patterns are available:\n");
 				for(String name : patternNames) {
 					ui.printToConsole(name + "\n");
 				}
 			}
 			
 		} else if (args.equalsIgnoreCase("Perceptrons")) {
 			Set<String> perceptronNames = ds.getPerceptronNames();
 			if (perceptronNames.size() == 0) {
 				ui.printToConsole("There are no Perceptrons available in the DataStore.");
 			} else {
 				ui.printToConsole("The following Perceptrons are available:\n");
 				for(String name : perceptronNames) {
 					ui.printToConsole(name + "\n");
 				}
 			}
 		} else {
 			ui.printToConsole("Syntax Error: usage of list command: list [Pattern|Perceptrons]");
 		}
 
 	}
 
 	/**
 	 * @see de.thm.mni.nn.ui.IUIAction#getDescription()
 	 */
 	@Override
 	public String getDescription() {
 		return "Prints a list of alle Patterns or Perceptrons available.";
 	}
 
 }
