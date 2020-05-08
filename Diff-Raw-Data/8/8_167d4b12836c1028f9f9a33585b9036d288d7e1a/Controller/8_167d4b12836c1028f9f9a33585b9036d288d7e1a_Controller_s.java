 package control;
 
 import instructions.IExecutable;
 import instructions.Instruction;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.util.Scanner;
 
 import javax.activity.InvalidActivityException;
 
 import exceptions.IllegalInstructionException;
 import exceptions.IncorrectFileFormatException;
 
 import simulation.Model;
 import view.View;
 
 /**
  * Passes instructions to the parser and executes those instructions on the model.
  * Saves and loads the state of the model
  * @author Ryan Fishel
  *
  */
 public class Controller {
 
 	private Model myModel;
 	private View myView;
 	private Parser myParser; 
 	private Environment myEnvironment;
 
 	public Controller(Model model, View view) {
 		myModel = model;
 		myView = view;
 		myEnvironment = new Environment();
 		myParser = new Parser(myEnvironment);
 	}
 
 	public void createRunInstruction(String s) {
 		try {
 			IExecutable instruction = myParser.generateInstruction(s);
 			instruction.execute(myModel);
 		} catch (IllegalInstructionException e) {
 			myView.displayText(e.toString());
 		}
 		
 	}
 
 	public void saveState(FileWriter fw){
 		try{
 			BufferedWriter out = new BufferedWriter(fw);
 			// TODO: write the variables that need to be saved
 		}
 		catch(Exception e){
 			myView.displayText(e.toString());
 		}
 	}
 
	public void loadState(File f) {
 		try {
 			Scanner input = new Scanner(f);
 			while (input.hasNext()) {
 				Scanner line = new Scanner(input.nextLine());
 				// TODO: create variables
 			}
 
 			input.close();
 		} catch (FileNotFoundException | IllegalStateException  e) {
			// should not happen because File came from user selection
			throw new IncorrectFileFormatException();
 			myView.displayText(e.toString());
 		}
 
 	}
 
 	public void clear() {
 		myModel.reset();
 	}
 
 
 }
