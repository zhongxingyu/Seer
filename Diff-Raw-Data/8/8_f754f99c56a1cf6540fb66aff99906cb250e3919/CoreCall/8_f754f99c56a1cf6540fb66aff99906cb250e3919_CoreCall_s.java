 package com.me.ifly6;
 
 import java.awt.Font;
 
 public class CoreCall extends ConInfClass {
 	// Name: Core Caller (from Core, Calls necessary functions)
 	
	public static void caller(String args){
 		preoperand = input.getText();
 		ConInfClass.append(computername + "~ $ " + preoperand);
 		input.setText(null);
 		operand = preoperand.split(" ");
 
 		// Sub-commands
 		if (operand[0].equals("changelog")) {
 			TextComm.changelog(null);
 		}
 		if (operand[0].equals("copyright")) {
 			ConInfClass.append(Info.copyright);
 			log("\nCopyright Processing Trigger Invoked");
 		}
 		if (operand[0].equals("help")) {
 			TextComm.help(null);
 			log("\nHelp Processing Trigger Invoked");
 		}
 		if (operand[0].equals("/clear")) {
 			Console.output.setText(starter);
 			log("\nCommand to Clear Screen Invoked");
 		}
 		if (operand[0].equals("acknowledgements")) {
 			TextComm.acknowledgements(null);
 		}
 		if (operand[0].equals("/font")){
 			int tmp;
 			if (operand[2].equals(null)){
 				tmp = 11;
 			}
 			tmp = java.lang.Integer.parseInt(operand[2]);
 			Font font = new Font(operand[1], 0, tmp);
 			output.setFont(font);
 		}
 		if (operand[0].equals("/api")){
 			if (preoperand.equals(operand[0])){
 				append("This is the current list of Plugins:");
 				append(Info.plugins);
 			} else {
				api(null);
 			}
 		}
 	}
 }
