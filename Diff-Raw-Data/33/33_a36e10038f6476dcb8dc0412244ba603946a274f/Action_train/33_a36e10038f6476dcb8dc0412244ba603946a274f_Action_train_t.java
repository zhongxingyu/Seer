 /**
  * 
  */
 package de.thm.mni.nn.ui.actions;
 
 import de.thm.mni.nn.model.DataStore;
 import de.thm.mni.nn.perceptron.impl.Pattern;
 import de.thm.mni.nn.perceptron.impl.Perceptron;
 import de.thm.mni.nn.ui.Action;
 import de.thm.mni.nn.ui.UserInterface;
 
 /**
  * 
  * @author Alexander Schulz
  *
  */
 public class Action_train extends Action {
 	
 	/**
 	 * @param ds
 	 * @param ui
 	 */
 	public Action_train(DataStore ds, UserInterface ui) {
 		super(ds, ui);
 	}
 
 	
 	@Override
 	public void callAction(String args) {
 		String perceptronName;
 		String patternName;
 		int training_count;
 		System.out.print("Name of perceptron to train: ");
 		perceptronName = ui.inputToString();
 		Perceptron perceptron = ds.getPerceptron(perceptronName);
 		if(perceptron == null) {
 			ui.printToConsole("The given Perceptron could not be found!");
 			return;
 		}
 		
 		System.out.print("Name of the Pattern to use: ");
 		patternName = ui.inputToString();
 		Pattern pattern = ds.getPattern(patternName);
 		if(pattern == null) {
 			ui.printToConsole("Aborting... There is no Pattern named '" + patternName + "'.");
 			return;
 		}		
 		
 		System.out.println("How many times should be trained?:");
 		training_count = ui.inputToInt();
 		for (int i = 0; i < training_count; i++){
 			perceptron.train(pattern);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see de.thm.mni.nn.ui.IUIAction#getDescription()
 	 */
 	@Override
 	public String getDescription() {
 		return "Trains a Perceptron with a given Pattern";
 	}
 
 }
