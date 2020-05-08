 /* Copyright (C) 2010-2011 Christoph Giesel
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package hisqisnoten;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 import hisqisnoten.console.HisqisConsole;
 import hisqisnoten.gui.HisqisGUI;
 
 /**
  * 
  * @author Christoph Giesel
  *
  */
 public class HisqisNoten {
 	
 	public static Options options;
     
     public static void main(String[] args) {
         options = new Options();
         options.addOption("h", "help", false, "this help");
         options.addOption("u", "user", true, "username for hisqis");
         options.addOption("p", "pass", true, "password for hisqis");
         options.addOption("c", "console", false, "console mode instead of gui mode");
         
         CommandLineParser parser = new PosixParser();
         
         try {
 			CommandLine cmdline = parser.parse(options, args);
 			
			if (cmdline.hasOption("help")) {
				printHelp();
				System.exit(0);
			}
			
			if (cmdline.getArgList().size() != 0) {
 				System.err.println("Please use the new commandline options.");
 				System.out.println();
				
 				printHelp();
 				System.exit(1);
 			}
 			
 			String user = cmdline.getOptionValue("user", null);
 			String pass = cmdline.getOptionValue("pass", null);
 			
 			if (cmdline.hasOption("console")) {
 				new HisqisConsole(user, pass);
 			} else {
 				new HisqisGUI(user, pass);
 			}
 		} catch (ParseException e) {
 			printHelp();
 		}
     }
     
     public static void printHelp() {
     	HelpFormatter formatter = new HelpFormatter();
 		formatter.printHelp("java -jar HisqisNoten.jar", options);
     }
 }
