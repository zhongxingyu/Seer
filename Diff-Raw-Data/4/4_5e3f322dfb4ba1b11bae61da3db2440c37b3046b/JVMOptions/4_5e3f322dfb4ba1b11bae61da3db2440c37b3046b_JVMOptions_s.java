 package com.github.hvasoares.pageobjects.utils;
 
 import java.lang.management.ManagementFactory;
 import java.lang.management.RuntimeMXBean;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class JVMOptions {
 	 
 	private Map<String, String> arguments = new HashMap<>();
 	
 	public JVMOptions(){
 		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
 		List<String> argumentsList = runtimeMxBean.getInputArguments();
 		parseMap( argumentsList );
 	}
 	
 	private void parseMap(List<String> argumentsList) {
 		for ( String argument : argumentsList ){
 			parseArgument( argument );
 		} 
 	}
 
 	private void parseArgument(String argument) {
 		try{
 			int pos = argument.indexOf('=');		  
 			String value = argument.substring(pos + 1);		  	
 			String opt = argument.substring(0, pos);
 			arguments.put(opt, value);
 		} catch ( Exception e ){
 			// Ignore and debug...
 		}
 	}
 
 	public String getValue( String arg ){		
 		return arguments.get( arg );
 	}
 	public List<String> getValues( String arg ){
 		String value = getValue(arg);
 		if ( value != null ){
 			return Arrays.asList( value.split(",") );
 		}
		return Collections.emptyList();
 			
 	}
 	
 	public boolean exists( String arg ) {
 		return arguments.containsKey(arg);
 	}
 }
