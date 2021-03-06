 /**
  * openHAB, the open Home Automation Bus.
  * Copyright (C) 2011, openHAB.org <admin@openhab.org>
  *
  * See the contributors.txt file in the distribution for a
  * full listing of individual contributors.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation; either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see <http://www.gnu.org/licenses>.
  *
  * Additional permission under GNU GPL version 3 section 7
  *
  * If you modify this Program, or any covered work, by linking or
  * combining it with Eclipse (or a modified version of that library),
  * containing parts covered by the terms of the Eclipse Public License
  * (EPL), the licensors of this Program grant you additional permission
  * to convey the resulting work.
  */
 
 package org.openhab.binding.http.internal;
 
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.openhab.binding.http.HttpBindingProvider;
 import org.openhab.core.binding.AbstractActiveBinding;
 import org.openhab.core.items.Item;
 import org.openhab.core.items.ItemNotFoundException;
 import org.openhab.core.items.ItemNotUniqueException;
 import org.openhab.core.items.ItemRegistry;
 import org.openhab.core.library.types.StringType;
 import org.openhab.core.types.State;
 import org.openhab.core.types.TypeParser;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * An active binding which requests a given URL frequently.
  * 
  * @author Thomas.Eichstaedt-Engelen
  * @since 0.6.0
  */
 public class HttpInBinding extends AbstractActiveBinding<HttpBindingProvider> implements ManagedService {
 
 	static final Logger logger = LoggerFactory.getLogger(HttpInBinding.class);
 	
 	/** the timeout to use for connecting to a given host (defaults to 5000 milliseconds) */
 	private static int timeout = 5000;
 
 	/** the interval to find new refresh candidates (defaults to 1000 milliseconds)*/ 
 	private static int granularity = 1000;
 	
 	private Map<String, Long> lastUpdateMap = new HashMap<String, Long>();
 	
 	private ItemRegistry itemRegistry;
 	
 	
 	public void setItemRegistry(ItemRegistry itemRegistry) {
 		this.itemRegistry = itemRegistry;
 	}
 	
 	public void unsetItemRegistry(ItemRegistry itemRegistry) {
 		this.itemRegistry = null;
 	}
 
 
 	/**
 	 * @{inheritDoc}
 	 */
 	@Override
 	public void execute() {
 		
 		for (HttpBindingProvider provider : providers) {
 			for (String itemName : provider.getInBindingItemNames()) {
 				
 				String url = provider.getUrl(itemName);
 				int refreshInterval = provider.getRefreshInterval(itemName);
 				String transformation = provider.getTransformation(itemName);
 				
 				Long lastUpdateTimeStamp = lastUpdateMap.get(itemName);
 				if (lastUpdateTimeStamp == null) {
 					lastUpdateTimeStamp = 0L;
 				}
 				
 				long age = System.currentTimeMillis() - lastUpdateTimeStamp;
 				boolean needsUpdate = age >= refreshInterval;
 				
 				if (needsUpdate) {
 					
 					logger.debug("item '{}' is about to be refreshed now", itemName);
 					
 					String response = HttpUtil.executeUrl("GET", url, timeout);
 					String transformedResponse = transformResponse(transformation, response);
 					String abbreviatedResponse = StringUtils.abbreviate(transformedResponse, 256);
 					
 					logger.debug("transformed response is '{}'", transformedResponse);
 					
 					Item item = getItemFromItemName(itemName);
 					State state = createState(item, abbreviatedResponse);
					
					if (state != null) {
						eventPublisher.postUpdate(itemName, state);
					}
 					
 					lastUpdateMap.put(itemName, System.currentTimeMillis());
 				}					
 
 			}
 		}
 		
 	}
 	
 	/**
 	 * Returns the {@link Item} for the given <code>itemName</code> or 
 	 * <code>null</code> if there is no or to many corresponding Items
 	 * 
 	 * @param itemName
 	 * 
 	 * @return the {@link Item} for the given <code>itemName</code> or 
 	 * <code>null</code> if there is no or to many corresponding Items
 	 */
 	private Item getItemFromItemName(String itemName) {
 		try {
 			return itemRegistry.getItem(itemName);
 		} catch (ItemNotFoundException e) {
 			logger.error("couldn't find item for itemName '" + itemName + "'", e);
 		} catch (ItemNotUniqueException e) {
 			logger.error("itemName '" + itemName + "' is not unique", e);
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Returns a {@link State} which is inherited from the {@link Item}s
 	 * accepted DataTypes. The call is delegated to the  {@link TypeParser}. If
 	 * <code>item</code> is <code>null</code> the {@link StringType} is used.
 	 *  
 	 * @param item
 	 * @param abbreviatedResponse
 	 * 
 	 * @return a {@link State} which type is inherited by the {@link TypeParser}
 	 * or a {@link StringType} if <code>item</code> is <code>null</code> 
 	 */
 	private State createState(Item item, String abbreviatedResponse) {
 		if (item != null) {
 			return TypeParser.parseState(item.getAcceptedDataTypes(), abbreviatedResponse);
 		}
 		else {
 			return StringType.valueOf(abbreviatedResponse);
 		}
 	}
 
     protected String transformResponse(String transformation, String response) {
 		
     	String regex = extractRegexTransformation(transformation);
 		
 		Matcher matcher = Pattern.compile("^" + regex + "$", Pattern.DOTALL).matcher(response.trim());
 		if (!matcher.matches() && matcher.groupCount() != 1) {
 			logger.warn("the given regex must contain exactly one group");
 			return null;
 		}
 		matcher.reset();
 		
 		String result = "";
 		
 		while (matcher.find()) {
 			
 			if (matcher.groupCount() == 1) {
 				result = matcher.group(1);
 			}
 			else {
 				logger.warn("the given regular expression '" + transformation + "' must contain exactly one group -> couldn't compute transformation");
 			}
 		}
 		
 		return result;
 	}
     
     protected String extractRegexTransformation(String transformation) {
     	
 		Matcher matcher = Pattern.compile("REGEX\\((.*)\\)").matcher(transformation);
 		
 		if (!matcher.matches() || matcher.groupCount() != 1) {
 			new UnsupportedOperationException("given transformation '" + transformation + "' is unsupported");
 		}
 		matcher.reset();
 		
 		// default regex (if we couldn't find anything better) is '(.*)'
 		String regex = "(.*)";
 		
 		while (matcher.find()) {
     		regex = matcher.group(1);
     	}
     	
 		return regex;
     }
     
     /**
      * @{inheritDoc}
      */
     @Override
     protected long getRefreshInterval() {
     	return HttpInBinding.granularity;
     }
     
     @Override
     protected String getName() {
     	return "HTTP Refresh Service";
     }
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@SuppressWarnings("rawtypes")
 	@Override
 	public void updated(Dictionary config) throws ConfigurationException {
 		
 		if (config != null) {
 			String timeoutString = (String) config.get("timeout");
 			if (StringUtils.isNotBlank(timeoutString)) {
 				HttpInBinding.timeout = Integer.parseInt(timeoutString);
 			}
 			
 			String granularityString = (String) config.get("granularity");
 			if (StringUtils.isNotBlank(granularityString)) {
 				HttpInBinding.granularity = Integer.parseInt(granularityString);
 			}
 		}
 
 	}
 
 }
