 /**
  * my package yay :P
  */
 package com.wissamyoussef.imagelib;
 
 /**
  * Imports my dear...obvious eh?
  */
 
 import org.apache.commons.cli.*;
 
 /**
  * @author Wissam Youssef
  *
  */
public class CmdLineInterface 
 
 	/**
 	 * @param args
 	 * @throws ParseException 
 	 */
 	public static void main(String[] args) throws ParseException {
 		// This is gonna be fun :)
		Options options = new Options()
 		
 		//Adding proper options
 		options.addOption("url", true, "URL!");
 		CommandLineParser parser = new PosixParser();
 		CommandLine cmd = parser.parse( options, args);
 		if(cmd.hasOption("url")) {
 		    System.out.println("bleh");
 		}
 		else {
 		    System.out.println("didn't work");
 		}
 	}
 
 }
