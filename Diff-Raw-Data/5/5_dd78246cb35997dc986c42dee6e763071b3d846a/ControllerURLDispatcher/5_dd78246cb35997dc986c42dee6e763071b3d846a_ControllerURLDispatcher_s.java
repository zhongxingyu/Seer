 package jcube.core.filter.controller;
 
 import java.util.List;
 
 import jcube.core.configuration.ConfigContext;
 import jcube.core.dispatcher.Dispatcher;
 import jcube.core.exception.ConfigException;
 import jcube.core.server.chain.ChainFilter;
 import jcube.core.server.chain.Filter;
 import jcube.core.server.environ.Environ;
 import jcube.core.server.environ.Request;
 
 public class ControllerURLDispatcher implements ChainFilter
 {
 	Environ environ;
 	@Override
 	public void filter(Filter filterOptions) throws Exception
 	{
 		Request request = environ.getRequest();
 		Dispatcher dispatcher = request.getDispatcher();
 		String url = request.getURL();
 		//Setting the initial mapping
 		filterInitialURLS(url, dispatcher);
 	}
 
 	@Override
 	public boolean stopChaining()
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public List<String> skipChainsByName()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	
 	private void filterInitialURLS(String url, Dispatcher dispatcher) throws ConfigException
 	{
 		String controllerKey = ConfigContext.get("jcube.controller.settings.defaultControllerNameKey").getString();
 		String actionKey = ConfigContext.get("jcube.controller.settings.defaultControllerActionKey").getString();
 		
 		String[] splitted = url.split("/"); // Splitting
 		
 		String last_inserted_key = null;
 		
 		Integer position = 1;
 		for (String item : splitted)
 		{
 			
 			switch (position) 
 			{
 				case 0:
 				case 1: 
 				case 2:
 					dispatcher.set(controllerKey, item);
 					break;
 				case 3:
 					dispatcher.set(actionKey, item);
 					break;
 				case 4:
 					dispatcher.set("id", item);
 					break;
 				default:
 					if (last_inserted_key == null) {
						dispatсher.getParams().put(item, null); last_inserted_key = item;
 					} else {
						dispatсher.getParams().put(last_inserted_key, item);	last_inserted_key = null;
 					}
 				
 			}
 			position++;
 		}
 	}
 
 }
