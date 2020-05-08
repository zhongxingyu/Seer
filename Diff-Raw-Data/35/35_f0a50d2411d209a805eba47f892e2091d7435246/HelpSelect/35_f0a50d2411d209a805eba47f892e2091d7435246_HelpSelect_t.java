 /**
  * 
  */
 package com.mimpidev.podsalinan.cli.options.help;
 
 import com.mimpidev.podsalinan.DataStorage;
 import com.mimpidev.podsalinan.cli.CLIOption;
 import com.mimpidev.podsalinan.cli.ReturnObject;
 
 /**
  * @author sbell
  *
  */
 public class HelpSelect extends CLIOption {
 
 	public HelpSelect(DataStorage newData) {
 		super(newData);
 	}
 
 	@Override
 	public ReturnObject execute(String command) {
 		System.out.println("select is used to traverse around the system, when not using the menu");
 		System.out.println("");
 		System.out.println("   select podcast <podcast name>       this will select the podcast");
 		System.out.println("            If the podcast name is not exact, the system will try to guess the podcast.");
 		System.out.println("   select podcast <podcast number>     this will select the podcast");
 		System.out.println("   select episode <episode number>     this will select the episode");
 		System.out.println("            If the podcast is not selected this will tell you to select a podcast first.");
 		System.out.println("   select download <download number>   this will select the download");
		returnObject.execute=false;
		return returnObject;
 	}
 
 }
