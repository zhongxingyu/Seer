 /*******************************************************************************
  * Copyright (c) 2013 95A31.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     95A31 - initial API and implementation
  ******************************************************************************/
 import TreeAutomata.*;
 import java.io.*;
 
 public class Main {
 
 	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		if(args.length < 2){
			System.out.println("One or more argument(s) missing.");
			System.out.println("EXAMPLE: java -jar JCEPIT.jar -R RabinExampleAutomata.txt");
			System.out.println("EXAMPLE: java -jar JCEPIT.jar -B BUchiExampleAutomata.txt");
			return;
		}
 
 		if (args[0].matches("-B|-b")) {
 			Buchi BTA = Buchi.fromFile(args[1]);
 			Buchi tmpBTA = BTA.recognizeEmptyLanguage();
 			System.out.println("Language recognized by automata " + (tmpBTA.states.isEmpty() ? "is " : "isn't ") + "empty.");
 			if (!tmpBTA.states.isEmpty()) {
 				tmpBTA.toImageFile(args[1].substring(0, args[1].lastIndexOf('.')) + ".png");
 			}
 		} else if (args[0].matches("-R|-r")) {
 			Rabin tmpRA = Rabin.fromFile(args[1]);
 			TreeAutomata.InputFree.Rabin IFRTA = tmpRA.toInputFree();
 			TreeAutomata.InputFree.Rabin tmpIFRTA = IFRTA.recognizeEmptyLanguage();
 			System.out.println("Language recognized by automata " + (tmpIFRTA.states.isEmpty() ? "is " : "isn't ") + "empty.");
 			if (!tmpIFRTA.states.isEmpty()) {
 				tmpIFRTA.toImageFile(args[1].substring(0, args[1].lastIndexOf('.')) + ".png");
 			}
 		} else {
 			System.out.println("Unknow option: " + args[0]);
 		}
 	}
 }
