 package de.thm.mni.nn.ui.actions;
 
 import java.io.*;
 
 import de.thm.mni.nn.model.DataStore;
 import de.thm.mni.nn.perceptron.impl.ActivationCalculation;
 import de.thm.mni.nn.perceptron.impl.ENeuronType;
 import de.thm.mni.nn.perceptron.impl.Pattern;
 import de.thm.mni.nn.perceptron.impl.Perceptron;
 import de.thm.mni.nn.ui.Action;
 import de.thm.mni.nn.ui.UserInterface;
 
 public class Action_readfile extends Action {
 
 	/**
 	 * Constructor
 	 * @param ds Datastore to work on.
 	 * @param ui User interface to work on.
 	 */
 	public Action_readfile(DataStore ds, UserInterface ui) {
 		super(ds, ui);
 	}
 
 	/**
 	 * Is called to Execute the Action.
 	 */
 	public void callAction(String args) {
 		Perceptron p = null;
 		Pattern pt = null;
 		String p_name = "";
 		String pt_name = "";
 		int rownr = 0;
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(args));
 			String zeile = null;
 			while ((zeile = in.readLine()) != null) {
 				rownr++;
 				String[] ws = zeile.split(" ");
 				if (ws[0].equals("P") && ws[1].equals("BEGIN")
 						&& ws.length == 6) {
 					p_name = ws[2];
 					int layer = Integer.parseInt(ws[3]);
 					double min = Double.parseDouble(ws[4]);
 					double max = Double.parseDouble(ws[5]);
 					p = new Perceptron(layer, min, max);
 				} else if (ws[0].equals("P") && ws[1].equals("END")
 						&& ws.length == 2) {
 					if (ds.addPerceptron(p_name, p)) {
 						System.out.println("Added perceptron " + p_name);
 					} else {
 						System.out
 								.println("There is already a perceptron named "
 										+ p_name);
 					}
 					p = null;
 				} else if (ws[0].equals("N") && ws.length >= 5 && p != null) {
 					int layer = Integer.parseInt(ws[1]);
 					int count = Integer.parseInt(ws[2]);
 					ENeuronType type;
 					switch (Integer.parseInt(ws[3])) {
 					case 0:
 						type = ENeuronType.Input;
 						break;
 					case 1:
 						type = ENeuronType.Hidden;
 						break;
 					case 2:
 						type = ENeuronType.Output;
 						break;
 					default:
 						throw new IllegalArgumentException("Row " + rownr
 								+ ":Neurontype not supported");
 					}
 					ActivationCalculation calculator  = new ActivationCalculation();
 					switch (Integer.parseInt(ws[4])) {
 					case 0:
 						calculator.setupIdentity();
 						break;
 					case 1:
 						calculator.setupBoundedIdentity(Double.parseDouble(ws[5]), Double.parseDouble(ws[6]));
 						break;
 					case 2:
 						calculator.setupThreshold(Double.parseDouble(ws[5]));
 						break;
 					case 3:
 						calculator.setupLogistic(Double.parseDouble(ws[5]), Double.parseDouble(ws[6]));
 						break;
 					default:
 						throw new IllegalArgumentException(
 								"Activation Function not supported");
 					}
 					
 					p.addNeuron(layer, count, type, calculator);
 				} else if (ws[0].equals("A") && ws.length == 5 && p != null) {
 					int startNeuronLayer = Integer.parseInt(ws[1]);
 					int startNeuronColumn = Integer.parseInt(ws[2]);
 					int endNeuronLayer = Integer.parseInt(ws[3]);
 					int endNeuronColumn = Integer.parseInt(ws[4]);
 
 					p.addAxon(startNeuronLayer, startNeuronColumn,
 							endNeuronLayer, endNeuronColumn);
 					
 				} else if (ws[0].equals("T") && ws.length == 3
 						&& ws[1].equals("BEGIN")) {
 					if (pt != null) {
 						throw new IllegalArgumentException(
 								"There is already an open Pattern");
 					}
 					pt_name = ws[2];
 				} else if (ws[0].equals("T") && ws.length > 2
 						&& ws[1].equals("IN")) {
 					Double[] input = new Double[ws.length - 2];
 					for (int i = 2; i < ws.length; i++) {
 						input[i - 2] = Double.parseDouble(ws[i]);
 					}
 					pt = new Pattern(input);
 				} else if (ws[0].equals("T") && ws.length > 2
 						&& ws[1].equals("OUT")) {
 					if (pt != null) {
 						Double[] input = new Double[ws.length - 2];
 						for (int i = 2; i < ws.length; i++) {
 							input[i - 2] = Double.parseDouble(ws[i]);
 						}
 						pt.addOutputPattern(input);
 					} else
 						throw new IllegalArgumentException(
 								"There is no open Pattern");
 				} else if (ws[0].equals("T") && ws.length == 2
 						&& ws[1].equals("END")) {
 					if (pt != null) {
 						if (ds.addPattern(pt_name, pt)) {
 							System.out.println("Added Pattern " + pt_name);
 						} else {
 							System.out
 									.println("There is already a Pattern named "
 											+ pt_name);
 						}
 					} else
 						throw new IllegalArgumentException(
 								"There is no open Pattern");
 				}
 
 				else if (!ws[0].equals("%"))
 					throw new IllegalArgumentException("Wrong row Format!");
 
 			}
 		} catch (IOException e) {
 			System.out.println("Inputfile not Found!");
 			return;
 		} catch (NumberFormatException e) {
 			System.out.println("Row " + rownr + ": Wrong Data Format.");
 			return;
 		} catch (IllegalArgumentException e) {
 			System.out.println("Row " + rownr + ": " + e.getMessage());
 			return;
 		}
 		System.out.println(args + " loaded successful!");
 	}
 
 	@Override
 	public String getDescription() {
 		String s = "This Function enables the User to read a Perceptron from a file. \r\n "
 				+ " The Function needs to be called \"readfile path\" Test.nn in Eclipse Project.";
 		return s;
 	}
 
 }
