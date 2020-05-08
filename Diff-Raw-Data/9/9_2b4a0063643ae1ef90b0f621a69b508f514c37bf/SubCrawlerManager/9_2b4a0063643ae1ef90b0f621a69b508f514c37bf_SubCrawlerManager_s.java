 package org.paxle.crawler.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.service.metatype.AttributeDefinition;
 import org.osgi.service.metatype.MetaTypeProvider;
 import org.osgi.service.metatype.ObjectClassDefinition;
 import org.paxle.crawler.ISubCrawler;
 import org.paxle.crawler.ISubCrawlerManager;
 
 public class SubCrawlerManager implements ISubCrawlerManager, MetaTypeProvider, ManagedService {
 	public static final String PID = ISubCrawlerManager.class.getName();
 	
 	/* ==============================================================
 	 * CM properties
 	 * ============================================================== */
 	private static final String ENABLED_PROTOCOLS = PID + "." + "enabledProtocols";
 	
 	/**
 	 * For logging
 	 */
 	private final Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * A {@link HashMap} containing the protocol that is supported by a sub-crawler as key and
 	 * the {@link ISubCrawler} as value.
 	 */
 	private final HashMap<String, ISubCrawler> subCrawlerList = new HashMap<String, ISubCrawler>();
 
 	/**
 	 * A list of enabled crawling protocols
 	 */
 	private final Set<String> enabledProtocols = new HashSet<String>();
 	
 	/**
 	 * The CM configuration that belongs to this component
 	 */
 	private final Configuration config;
 	
 	/**
 	 * A list of {@link Locale} for which a {@link ResourceBundle} exists
 	 * @see MetaTypeProvider#getLocales()
 	 */
 	private String[] locales;
 	
 	/**
 	 * @param config the CM configuration that belongs to this component
 	 * @throws IOException
 	 * @throws ConfigurationException
 	 */
 	public SubCrawlerManager(Configuration config, String[] locales) throws IOException, ConfigurationException {
 		if (config == null) throw new NullPointerException("The CM configuration is null");
 		if (locales == null) throw new NullPointerException("The locale array is null");
 		
 		this.config = config;
 		this.locales = locales;
 		
 		// initialize CM values
 		if (config.getProperties() == null) {
 			config.update(this.getCMDefaults());
 		}
 		
 		// update configuration of this component
 		this.updated(config.getProperties());
 	}
 	
 	/**
 	 * Adds a newly detected {@link ISubCrawler} to the {@link Activator#subCrawlerList subcrawler-list}
 	 * @param protocols the protocols supported by the crawler
 	 * @param reference the reference to the deployed {@link ISubCrawler subcrawler-service}
 	 */
 	public void addSubCrawler(String[] protocols, ISubCrawler subCrawler) {
 		if (protocols == null) throw new NullPointerException("The protocol array must not be null.");
 		for (String protocol : protocols) this.addSubCrawler(protocol, subCrawler);
 	}
 	
 	private void addSubCrawler(String protocol, ISubCrawler subCrawler) {
 		if (protocol == null) throw new NullPointerException("The protocol must not be null");
 		if (subCrawler == null) throw new NullPointerException("The crawler object must not be null");		
 		protocol = protocol.toLowerCase();
 		
 		this.subCrawlerList.put(protocol, subCrawler);		
 		this.logger.info(String.format(
 				"Crawler '%s' for protocol '%s' was installed.",
 				subCrawler.getClass().getName(),
 				protocol
 		));
 	}
 	
 	/**
 	 * Removes a uninstalled {@link ISubCrawler} from the {@link Activator#subCrawlerList subcrawler-list}
 	 * @param protocols the protocols supported by the crawler that should be uninstalled
 	 */
 	public void removeSubCrawler(String[] protocols) {
 		if (protocols == null) throw new NullPointerException("The protocol array must not be null.");
 		for (String protocol : protocols) this.removeSubCrawler(protocol);
 	}	
 	
 	public void removeSubCrawler(String protocol) {
 		if (protocol == null) throw new NullPointerException("The protocol must not be null");		
 		protocol = protocol.toLowerCase();
 		
 		ISubCrawler subCrawler = this.subCrawlerList.remove(protocol);
 		this.logger.info(String.format(
				"Crawler for protocol '%s' was uninstalled.",
 				subCrawler==null?"unknown":subCrawler.getClass().getName(),
 				protocol
 		));
 	}		
 	
 	/**
 	 * Getting a {@link ISubCrawler} which is capable to handle
 	 * the given network-protocol
 	 * @param protocol
 	 * @return the requested sub-crawler or <code>null</code> if no crawler for
 	 *         the specified protocol is available
 	 */
 	public ISubCrawler getSubCrawler(String protocol) {
 		if (protocol == null) return null;
 		protocol = protocol.toLowerCase();
 		
 		if (!this.enabledProtocols.contains(protocol)) return null;
 		return this.subCrawlerList.get(protocol);
 	}	
 	
 	/**
 	 * Determines if a given protocol is supported by one of the registered
 	 * {@link ISubCrawler sub-crawlers}.
 	 * @param protocol the protocol
 	 * @return <code>true</code> if the given protocol is supported or <code>false</code> otherwise
 	 */
 	public boolean isSupported(String protocol) {
 		if (protocol == null) return false;
 		protocol = protocol.toLowerCase();
 		
 		if (!this.enabledProtocols.contains(protocol)) return false; 
 		return this.subCrawlerList.containsKey(protocol);
 	}
 
 	/**
 	 * @see ISubCrawlerManager#getSubCrawlers()
 	 */
 	public Collection<ISubCrawler> getSubCrawlers() {
 		return Collections.unmodifiableCollection(subCrawlerList.values());
 	}
 	
 	/**
 	 * @see ISubCrawler#getProtocols()
 	 */
 	public Collection<String> getProtocols() {
 		Set<String> keySet = this.subCrawlerList.keySet();
 		String[] keyArray = keySet.toArray(new String[keySet.size()]);
 		return Collections.unmodifiableCollection(Arrays.asList(keyArray));
 	}
 
 	/**
 	 * @see ISubCrawlerManager#disableProtocol(String)
 	 */
 	@SuppressWarnings("unchecked")
 	public void disableProtocol(String protocol) {
 		try {
 			if (protocol == null) return;
 			protocol = protocol.toLowerCase();
 
 			// update enabled protocol list
 			this.enabledProtocols.remove(protocol);
 			
 			// updating CM
 			Dictionary<String,Object> props = this.config.getProperties();			
 			props.put(ENABLED_PROTOCOLS, this.enabledProtocols.toArray(new String[this.enabledProtocols.size()]));
 			this.config.update(props);
 		} catch (IOException e) {
 			this.logger.error(e);
 		}
 	}
 
 	/**
 	 * @see ISubCrawlerManager#enableProtocol(String)
 	 */
 	@SuppressWarnings("unchecked")
 	public void enableProtocol(String protocol) {
 		try {
 			if (protocol == null) return;
 			protocol = protocol.toLowerCase();
 
 			// updating enabled protocol list
 			this.enabledProtocols.add(protocol);
 
 			// updating CM
 			Dictionary<String,Object> props = this.config.getProperties();			
 			props.put(ENABLED_PROTOCOLS, this.enabledProtocols.toArray(new String[this.enabledProtocols.size()]));
 			this.config.update(props);
 		} catch (IOException e) {
 			this.logger.error(e);
 		}
 	}
 
 	/**
 	 * @see ISubCrawlerManager#disabledProtocols()
 	 */
 	public Set<String> disabledProtocols() {
 		// get all available protocols and remove enabled protocols
 		HashSet<String> protocols = new HashSet<String>(this.subCrawlerList.keySet());
 		protocols.removeAll(this.enabledProtocols);		
 		return protocols;
 	}
 
 	/**
 	 * @see MetaTypeProvider#getLocales()
 	 */
 	public String[] getLocales() {
 		return this.locales;
 	}
 
 	/**
 	 * @see MetaTypeProvider#getObjectClassDefinition(String, String)
 	 */
 	@SuppressWarnings("unchecked")
 	public ObjectClassDefinition getObjectClassDefinition(String id, String localeStr) {
 		final HashMap<String, ISubCrawler> crawlers = (HashMap<String, ISubCrawler>) this.subCrawlerList.clone();	
 		
 		Locale locale = (localeStr==null) ? Locale.ENGLISH : new Locale(localeStr);
 		final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10n/" + ISubCrawlerManager.class.getSimpleName(), locale);		
 		
 		return new ObjectClassDefinition() {
 			public AttributeDefinition[] getAttributeDefinitions(int filter) {
 				return new AttributeDefinition[]{
 					// Attribute definition for ENABLED_PROTOCOLS
 					new AttributeDefinition(){
 						private String[] getSupportedProtocols() {
 							// get all supported protocols and sort them
 							String[] protocols = crawlers.keySet().toArray(new String[crawlers.size()]);
 							Arrays.sort(protocols);							
 							return protocols;							
 						}
 						
 						public int getCardinality() {
 							return crawlers.size();
 						}
 
 						public String[] getDefaultValue() {
 							return this.getSupportedProtocols();
 						}
 
 						public String getDescription() {
 							return rb.getString("subcrawlerManager.enabledProtocols.desc");
 						}
 
 						public String getID() {
 							return ENABLED_PROTOCOLS;
 						}
 
 						public String getName() {
 							return rb.getString("subcrawlerManager.enabledProtocols.name");
 						}
 
 						public String[] getOptionLabels() {
 							return this.getSupportedProtocols();
 						}
 
 						public String[] getOptionValues() {
 							return this.getSupportedProtocols();
 						}
 
 						public int getType() {
 							return AttributeDefinition.STRING;
 						}
 
 						public String validate(String value) {
 							return null;
 						}						
 					}	
 				};
 			}
 
 			public String getDescription() {
 				return rb.getString("subcrawlerManager.desc");
 			}
 
 			public String getID() {
 				return ISubCrawlerManager.class.getName();
 			}
 
 			public InputStream getIcon(int size) throws IOException {
 				return (size == 16) 
 				? this.getClass().getResourceAsStream("/OSGI-INF/images/network.png")
 				: null;
 			}
 
 			public String getName() {				
 				return rb.getString("subcrawlerManager.name");
 			}			
 		};
 	}
 
 	private Hashtable<String,Object> getCMDefaults() {
 		final Hashtable<String,Object> defaults = new Hashtable<String,Object>();
 		
 		// per default http and https should be enabled
 		defaults.put(ENABLED_PROTOCOLS, new String[]{"http","https"});
 		
 		return defaults;
 	}
 	
 	/**
 	 * @see ManagedService#updated(Dictionary)
 	 */
 	public void updated(@SuppressWarnings("unchecked") Dictionary properties) throws ConfigurationException {
 		if (properties == null ) {
 			logger.warn("updated configuration is null");
 			/*
 			 * Generate default configuration
 			 */
 			properties = this.getCMDefaults();
 		}
 		
 		// configuring enabled protocols
 		String[] enabledProtocols = (String[]) properties.get(ENABLED_PROTOCOLS);
 		if (enabledProtocols != null) {
 			this.enabledProtocols.clear();
 			this.enabledProtocols.addAll(Arrays.asList(enabledProtocols));
 		}
 	}
 }
