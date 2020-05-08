 package li.rudin.rt.core;
 
 import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
 
 import li.rudin.rt.api.RTApi;
 import li.rudin.rt.api.config.RTConfig;
 import li.rudin.rt.api.handler.RTHandler;
 import li.rudin.rt.api.resource.ResourceMapping;
 import li.rudin.rt.core.handler.RTHandlerImpl;
 import li.rudin.rt.core.script.ScriptResourceMapping;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RT
 {
 	
 	/**
 	 * Local logger
 	 */
 	private static final Logger logger = LoggerFactory.getLogger(RT.class);
 
 	static
 	{
 		//Show version
 		logger.info("RT Version {}", RTVersion.getVersion());
 		
 		//Add rt.js
 		ResourceMapping.add(new ScriptResourceMapping());
 	}
 	
 	/**
 	 * Handler -> id map
 	 */
	private static final Map<String, RTHandler> map = new ConcurrentHashMap<>();
 		
 	/**
 	 * Returns the default handler
 	 * @param path
 	 * @return
 	 */
 	public static RTHandler getInstance()
 	{
 		return getInstance(RTApi.DEFAULT_ID);
 	}
 	
 	/**
 	 * Returns the handler for the given id
 	 * @param path
 	 * @return
 	 */
 	public static RTHandler getInstance(String id)
 	{
 		return getInstance(id, null);
 	}
 	
 	/**
 	 * Returns the handler for the given id and config
 	 * @param path
 	 * @return
 	 */
	public static RTHandler getInstance(String id, RTConfig config)
 	{
 		if (id == null)
 			return getInstance();
 		
 		RTHandler handler = map.get(id);
 		
 		if (handler == null)
 		{
 			if (config == null)
 				config = new RTConfig(60000);
 			
 			handler = new RTHandlerImpl(id, config);
 			map.put(id, handler);
 		}
 		
 		return handler;
 	}
 	
 
 }
