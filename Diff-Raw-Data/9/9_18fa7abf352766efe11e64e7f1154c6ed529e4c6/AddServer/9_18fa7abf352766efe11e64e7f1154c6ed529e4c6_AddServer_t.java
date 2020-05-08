 package com.jwm.caronte.actions.connection;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.lang.StringUtils;
 import org.lainsoft.forge.flow.nav.CommandException;
 import org.lainsoft.forge.flow.nav.GenericAction;
 
 public class AddServer extends GenericAction {
 
 	@Override
 	public String execute() throws CommandException {
 		System.out.println("server Added");		
 		if(StringUtils.isNotEmpty(param("server"))){
			getServers().put(param("server").trim(), StringUtils.defaultIfEmpty(param("port"), "15100").trim());
 		}
 		return null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Map<String, String>
 	getServers(){
 		if(application("servers") == null){
 			application("servers", new TreeMap<String, String>());
 		}
 		return (Map<String, String>) application("servers");
 	}
 
 }
