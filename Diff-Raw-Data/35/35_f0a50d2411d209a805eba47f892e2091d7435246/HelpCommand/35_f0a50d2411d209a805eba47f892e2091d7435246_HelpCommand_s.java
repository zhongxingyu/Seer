 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.ReturnObject;
 import com.mimpidev.podsalinan.cli.options.help.Help;
 import com.mimpidev.podsalinan.cli.options.help.HelpList;
 import com.mimpidev.podsalinan.cli.options.help.HelpSelect;
 import com.mimpidev.podsalinan.cli.options.help.HelpSet;
 
 /**
  * @author sbell
  *
  */
 public class HelpCommand extends CLIOption {
 
 	/**
 	 * @param newData
 	 */
 	public HelpCommand(DataStorage newData) {
 		super(newData);
 
 		options.put("", new Help(newData));
 		options.put("select", new HelpSelect(newData));
 		options.put("list", new HelpList(newData));
 		options.put("set", new HelpSet(newData));
 	}
 
 	@Override
 	public ReturnObject execute(String command) {
 		debug=true;
 
 		if (debug) Podsalinan.debugLog.logInfo("["+getClass().getName()+"] command: "+command);
 		
 		System.out.println("");
 		String subOption;
         if (command.split(" ").length>1)
 		   subOption=command.split(" ", 2)[1];
         else
         	subOption=command;
         
         if (!options.containsKey(subOption.toLowerCase())){
             System.out.println("Error: Invalid Help request.");
     		System.out.println("");
			options.get("").execute(command);
 		} else {
		    options.get(subOption.toLowerCase()).execute(command);
 		}
 		
 		System.out.println("");
 		System.out.println("");
 		return returnObject;
 	}
 }
