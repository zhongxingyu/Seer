 package org.paxle.crawler.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.service.metatype.AttributeDefinition;
 import org.osgi.service.metatype.MetaTypeProvider;
 import org.osgi.service.metatype.ObjectClassDefinition;
 import org.paxle.core.metadata.IMetaData;
 import org.paxle.core.metadata.IMetaDataProvider;
 import org.paxle.crawler.ISubCrawler;
 import org.paxle.crawler.ISubCrawlerManager;
 
 public class SubCrawlerManager implements ISubCrawlerManager, MetaTypeProvider, ManagedService, IMetaDataProvider {
 	public static final String PID = ISubCrawlerManager.class.getName();
 	private static final char SUBCRAWLER_PID_SEP = '#';	
 	
 	/* ==============================================================
 	 * CM properties
 	 * ============================================================== */
 	private static final String ENABLED_PROTOCOLS = PID + "." + "enabledProtocols";
 	private static final String ENABLE_DEFAULT = PID + "." + "enableDefault";
 	
 	/* ==============================================================
 	 * Property keys
 	 * ============================================================== */	
 	private static final String PROPS_KNOWN_CRAWLER_PIDS = PID + "." + "knownCrawerPids";
 	
 	/**
 	 * For logging
 	 */
 	private final Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * A {@link HashMap} containing the protocol that is supported by a sub-crawler as key and
 	 * the {@link ISubCrawler} as value.
 	 */
 	private final HashMap<String,TreeSet<ServiceReference>> subCrawlerList = new HashMap<String,TreeSet<ServiceReference>>();
 	
 	private final HashMap<String,ServiceReference> services = new HashMap<String,ServiceReference>();
 	
 	/**
 	 * A list of enabled crawling protocols
 	 */
 	private final Set<String> enabledServices = new HashSet<String>();
 	
 	private final Set<String> knownServicePids = new HashSet<String>();
 	
 	/**
 	 * The CM configuration that belongs to this component
 	 */
 	private final Configuration config;
 	
 	/**
 	 * A list of {@link Locale} for which a {@link ResourceBundle} exists
 	 * @see MetaTypeProvider#getLocales()
 	 */
 	private final String[] locales;
 	
 	private boolean enableDefault = true;
 	
 	private final BundleContext context;
 	private final Properties props;
 	
 	/**
 	 * @param config the CM configuration that belongs to this component
 	 * @throws IOException
 	 * @throws ConfigurationException
 	 */
 	public SubCrawlerManager(Configuration config, String[] locales, final BundleContext context, final Properties props) throws IOException, ConfigurationException {
 		if (config == null) throw new NullPointerException("The CM configuration is null");
 		if (locales == null) throw new NullPointerException("The locale array is null");
 		
 		this.config = config;
 		this.locales = locales;
 		this.context = context;
 		this.props = props;
 		if (props.get(PROPS_KNOWN_CRAWLER_PIDS) != null) {
 			final String knownStr = props.getProperty(PROPS_KNOWN_CRAWLER_PIDS);
 			if (knownStr.length() > 2)
 				for (final String parserPid : knownStr.substring(1, knownStr.length() - 1).split(","))
 					this.knownServicePids.add(parserPid.trim());
 		}
 		
 		// initialize CM values
 		if (config.getProperties() == null) {
 			config.update(this.getCMDefaults());
 		}
 		
 		// update configuration of this component
 		this.updated(config.getProperties());
 	}
 	
 	public void close() {
 		props.put(PROPS_KNOWN_CRAWLER_PIDS, this.knownServicePids.toString());
 	}
 	
 	private String[] getProtocols(final ServiceReference ref) {
 		final Object protocolsObj = ref.getProperty(ISubCrawler.PROP_PROTOCOL);
 		if (protocolsObj instanceof String) {
 			return ((String)protocolsObj).split(";|,");
 		} else if (protocolsObj instanceof String[]) {
 			return (String[])protocolsObj;
 		} else {
 			final ISubCrawler p = (ISubCrawler)context.getService(ref);
 			logger.warn(String.format("Parser '%s' registered with no mime-types to the framework", p.getClass().getName()));
 			final String[] protocols = p.getProtocols();
 			if (protocols == null || protocols.length == 0) {
 				logger.error(String.format("Parser '%s' does not provide support for any mime-types", p.getClass().getName()));
 				return null;
 			}
 			context.ungetService(ref);
 			return protocols;
 		}
 	}
 	
 	private String keyFor(final String protocol, final ServiceReference ref) {
 		final String bundle = (String)ref.getBundle().getHeaders().get(Constants.BUNDLE_SYMBOLICNAME);
 		String pid = (String)ref.getProperty(Constants.SERVICE_PID);
 		if (pid == null) pid = context.getService(ref).getClass().getName();
 		
 		final StringBuilder key = new StringBuilder(protocol.length() + bundle.length() + pid.length() + 2);
 		key.append(bundle).append(SUBCRAWLER_PID_SEP)
 		   .append(pid).append(SUBCRAWLER_PID_SEP)
 		   .append(protocol);
 		
 		return key.toString().intern();
 	}
 	
 	private String extractProtocol(String servicePID) {
 		return servicePID.substring(servicePID.lastIndexOf(SUBCRAWLER_PID_SEP) + 1);
 	}
 	
 	private boolean isEnabled(final String protocol, final ServiceReference ref) {
 		return this.enabledServices.contains(keyFor(protocol, ref));
 	}
 	
 	private void setEnabled(final String protocol, final ServiceReference ref, final boolean enabled) {
 		if (enabled) {
 			this.enabledServices.add(keyFor(protocol, ref));
 		} else {
 			this.enabledServices.remove(keyFor(protocol, ref));
 		}
 	}
 	
 	/**
 	 * Adds a newly detected {@link ISubCrawler} to the {@link #subCrawlerList subcrawler-list}
 	 * @param ref the reference to the deployed {@link ISubCrawler subcrawler-service}
 	 */
 	@SuppressWarnings("unchecked")
 	public void addSubCrawler(final ServiceReference ref) {
 		final String[] protocols = getProtocols(ref);
 		if (protocols == null)
 			return;
 		for (String protocol : protocols) {
 			protocol = protocol.trim();
 			TreeSet<ServiceReference> refs = this.subCrawlerList.get(protocol);
 			if (refs == null)
 				this.subCrawlerList.put(protocol, refs = new TreeSet<ServiceReference>());
 			refs.add(ref);
 			final String parserPid = keyFor(protocol, ref);
 			this.services.put(parserPid, ref);
 			if (!this.knownServicePids.contains(parserPid)) {
 				this.knownServicePids.add(parserPid);
 				setEnabled(protocol, ref, this.enableDefault);
 			}
 			this.logger.info(String.format(
 					"Crawler for protocol '%s' was installed.",
 					protocol
 			));
 		}
 		try {
 			final Dictionary props = config.getProperties();
 			props.put(ENABLED_PROTOCOLS, this.enabledServices.toArray(new String[this.enabledServices.size()]));
 			config.update(props);
 		} catch (IOException e) { logger.error("error updating configuration", e); }
 	}
 	
 	/**
 	 * Removes a uninstalled {@link ISubCrawler} from the {@link Activator#subCrawlerList subcrawler-list}
 	 * @param ref the reference to the {@link ISubCrawler subcrawler-service} to be removed
 	 */
 	@SuppressWarnings("unchecked")
 	public void removeSubCrawler(final ServiceReference ref) {
 		final String[] protocols = getProtocols(ref);
 		if (protocols == null)
 			return;
 		for (String protocol : protocols) {
 			protocol = protocol.trim();
 			TreeSet<ServiceReference> refs = this.subCrawlerList.get(protocol);
 			if (refs == null)
 				continue;
 			this.services.remove(keyFor(protocol, ref));
 			refs.remove(ref);
 			this.logger.info(String.format(
 					"Crawler for protocol '%s' was uninstalled.",
 					protocol
 			));
 		}
 		try {
 			final Dictionary props = config.getProperties();
 			props.put(ENABLED_PROTOCOLS, this.enabledServices.toArray(new String[this.enabledServices.size()]));
 			config.update(props);
 		} catch (IOException e) { logger.error("error updating configuration", e); }
 	}
 	
 	/**
 	 * Getting a {@link ISubCrawler} which is capable to handle
 	 * the given network-protocol
 	 * @param protocol
 	 * @return the requested sub-crawler or <code>null</code> if no crawler for
 	 *         the specified protocol is available
 	 */
 	public ISubCrawler getSubCrawler(String protocol) {
 		if (protocol == null)
 			return null;
 		protocol = protocol.trim();
 		final TreeSet<ServiceReference> refs = this.subCrawlerList.get(protocol);
 		if (refs == null)
 			return null;
 		for (final ServiceReference ref : refs)
 			if (isEnabled(protocol, ref))
 				return (ISubCrawler)context.getService(ref);
 		return null;
 	}
 	
 	public Collection<ISubCrawler> getSubCrawlers(String protocol) {
 		if (protocol == null)
 			return null;
 		protocol = protocol.trim();
 		final TreeSet<ServiceReference> refs = this.subCrawlerList.get(protocol);
 		if (refs == null)
 			return null;
 		final ArrayList<ISubCrawler> list = new ArrayList<ISubCrawler>(refs.size());
 		for (final ServiceReference ref : refs)
 			if (isEnabled(protocol, ref))
 				list.add((ISubCrawler)context.getService(ref));
 		return list;
 	}
 	
 	/**
 	 * Determines if a given protocol is supported by one of the registered
 	 * {@link ISubCrawler sub-crawlers}.
 	 * @param protocol the protocol
 	 * @return <code>true</code> if the given protocol is supported or <code>false</code> otherwise
 	 */
 	public boolean isSupported(String protocol) {
 		if (protocol == null)
 			return false;
 		protocol = protocol.trim();
 		final TreeSet<ServiceReference> refs = this.subCrawlerList.get(protocol);
 		if (refs == null)
 			return false;
 		for (final ServiceReference ref : refs)
 			if (isEnabled(protocol, ref))
 				return true;
 		return false;
 	}
 	
 	/**
 	 * @see ISubCrawlerManager#getSubCrawlers()
 	 */
 	public Collection<ISubCrawler> getSubCrawlers() {
 		final ArrayList<ISubCrawler> list = new ArrayList<ISubCrawler>();
 		for (final TreeSet<ServiceReference> refs : this.subCrawlerList.values())
 			for (final ServiceReference ref : refs)
 				list.add((ISubCrawler)context.getService(ref));
 		return Collections.unmodifiableCollection(list);
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
 			final TreeSet<ServiceReference> refs = this.subCrawlerList.get(protocol.trim());
 			if (refs != null)
 				for (final ServiceReference ref : refs)
 					setEnabled(protocol, ref, false);
 			
 			// updating CM
 			Dictionary<String,Object> props = this.config.getProperties();			
 			props.put(ENABLED_PROTOCOLS, this.enabledServices.toArray(new String[this.enabledServices.size()]));
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
 			final TreeSet<ServiceReference> refs = this.subCrawlerList.get(protocol.trim());
 			if (refs != null)
 				for (final ServiceReference ref : refs)
 					setEnabled(protocol, ref, true);
 			
 			// updating CM
 			Dictionary<String,Object> props = this.config.getProperties();			
 			props.put(ENABLED_PROTOCOLS, this.enabledServices.toArray(new String[this.enabledServices.size()]));
 			this.config.update(props);
 		} catch (IOException e) {
 			this.logger.error(e);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void enableCrawler(final String service) {
 		if (service == null) return;
 		try {
 			final String mimeType = this.extractProtocol(service);
 			setEnabled(mimeType, this.services.get(service), true);
 			
 			// updating CM
 			Dictionary<String,Object> props = this.config.getProperties();
 			props.put(ENABLED_PROTOCOLS, enabledServices.toArray(new String[enabledServices.size()]));
 			this.config.update(props);
 		} catch (IOException e) {
 			this.logger.error(e);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void disableCrawler(final String service) {
 		if (service == null) return;
 		try {
 			final String mimeType = this.extractProtocol(service);
 			setEnabled(mimeType, this.services.get(service), false);
 			
 			// updating CM
 			Dictionary<String,Object> props = this.config.getProperties();
 			props.put(ENABLED_PROTOCOLS, enabledServices.toArray(new String[enabledServices.size()]));
 			this.config.update(props);
 		} catch (IOException e) {
 			this.logger.error(e);
 		}
 	}
 	
 	/**
 	 * @see ISubCrawlerManager#disabledProtocols()
 	 */
 	
 	public Set<String> enabledCrawlers() {
 		final String[] services = this.enabledServices.toArray(new String[this.enabledServices.size()]);
 		return new HashSet<String>(Arrays.asList(services));
 	}
 	
 	public Map<String,Set<String>> getCrawlers() {
 		final HashMap<String,Set<String>> r = new HashMap<String,Set<String>>();
 		for (final Map.Entry<String,ServiceReference> entry : this.services.entrySet()) {
 			final String bundleName = (String)entry.getValue().getBundle().getHeaders().get(Constants.BUNDLE_NAME);
 			Set<String> keys = r.get(bundleName);
 			if (keys == null)
 				r.put(bundleName, keys = new HashSet<String>());
 			keys.add(entry.getKey());
 		}
 		return Collections.unmodifiableMap(r);
 	}
 	
 	public Set<String> enabledProtocols() {
 		HashSet<String> protocols = new HashSet<String>();
 		for (final String enabledService : this.enabledServices) {
 			final String protocol = this.extractProtocol(enabledService);
 			if (this.subCrawlerList.containsKey(protocol))
 				protocols.add(protocol);
 		}
 		
 		return protocols;
 	}
 	
 	/**
 	 * @see ISubParserManager#disabledMimeType()
 	 */
 	public Set<String> disabledProtocols() {
 		// get all available mime-types and remove enabled mime-types
 		HashSet<String> mimeTypes = new HashSet<String>(this.subCrawlerList.keySet());
 		mimeTypes.removeAll(enabledProtocols());
 		return mimeTypes;
 	}
 	
 	/**
 	 * @see MetaTypeProvider#getLocales()
 	 */
 	public String[] getLocales() {
 		return this.locales;
 	}
 	
 	private final class OCD implements ObjectClassDefinition, IMetaData {
 		
 		@SuppressWarnings("unchecked")
 		private final HashMap<String,TreeSet<ServiceReference>> crawlers = (HashMap<String,TreeSet<ServiceReference>>)subCrawlerList.clone();
 		private final Locale locale;
 		private final ResourceBundle rb;
 		
 		public OCD(final Locale locale) {
 			this.locale = locale;
 			this.rb = ResourceBundle.getBundle("OSGI-INF/l10n/" + ISubCrawlerManager.class.getSimpleName(), locale);
 		}
 		
 		public AttributeDefinition[] getAttributeDefinitions(int filter) {
 			return new AttributeDefinition[] {
 					// Attribute definition for ENABLE_DEFAULT
 					new AttributeDefinition() {
 						public int getCardinality() 		{ return 0; }
 						public String[] getDefaultValue() 	{ return new String[] { Boolean.TRUE.toString() }; }
 						public String getDescription() 		{ return rb.getString("subcrawlerManager.enableDefault.desc"); }
 						public String getID() 				{ return ENABLE_DEFAULT; }
 						public String getName() 			{ return rb.getString("subcrawlerManager.enableDefault.name"); }
 						public String[] getOptionLabels() 	{ return null; }
 						public String[] getOptionValues() 	{ return null; }
 						public int getType() 				{ return BOOLEAN; }
 						public String validate(String value) { return null; }
 					},
 					
 					// Attribute definition for ENABLED_PROTOCOLS
 					new AttributeDefinition() {
 						
 						private final String[] optionValues, optionLabels; {
 							
 							final TreeMap<String,String> options = new TreeMap<String,String>();
 							for (final Map.Entry<String,TreeSet<ServiceReference>> entry : crawlers.entrySet())
 								for (final ServiceReference ref : entry.getValue()) {
 									final String key = keyFor(entry.getKey(), ref);
 									
 									final Object service = context.getService(ref);
 									IMetaData metadata = null; 
 									if (service instanceof IMetaDataProvider)
 										metadata = ((IMetaDataProvider)service).getMetadata(locale);
 									
 									String name = null;
 									if (metadata != null)
 										name = metadata.getName();
 									if (name == null)
 										name = service.getClass().getName();
 									context.ungetService(ref);
 									
 									options.put(key, name + " (" + entry.getKey() + ")");
 								}
 							
 							optionValues = options.keySet().toArray(new String[options.size()]);
 							optionLabels = options.values().toArray(new String[options.size()]);
 						}
 						
 						public int getCardinality() {
 							return crawlers.size();
 						}
 						
 						public String[] getDefaultValue() {
 							return this.optionValues;
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
 							return this.optionLabels;
 						}
 						
 						public String[] getOptionValues() {
 							return this.optionValues;
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
 		
 		public String getVersion() {
 			return null;
 		}
 	}
 	
 	/**
 	 * @see MetaTypeProvider#getObjectClassDefinition(String, String)
 	 */
 	public ObjectClassDefinition getObjectClassDefinition(String id, String localeStr) {
 		return new OCD((localeStr==null) ? Locale.ENGLISH : new Locale(localeStr));
 	}
 	
 	public IMetaData getMetadata(Locale locale) {
 		return new OCD(locale);
 	}
 	
 	private Hashtable<String,Object> getCMDefaults() {
 		final Hashtable<String,Object> defaults = new Hashtable<String,Object>();
 		
 		// per default http and https should be enabled
		defaults.put(ENABLED_PROTOCOLS, new String[] {
				"org.paxle.CrawlerHttp" + SUBCRAWLER_PID_SEP + "org.paxle.crawler.http.impl.HttpCrawler" + SUBCRAWLER_PID_SEP + "http",
				"org.paxle.CrawlerHttp" + SUBCRAWLER_PID_SEP + "org.paxle.crawler.http.impl.HttpCrawler" + SUBCRAWLER_PID_SEP + "https"
		});
 		defaults.put(ENABLE_DEFAULT, Boolean.TRUE);
 		
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
 			this.enabledServices.clear();
 			this.enabledServices.addAll(Arrays.asList(enabledProtocols));
 		}
 		
 		final Object enableDefault = properties.get(ENABLE_DEFAULT);
 		if (enableDefault != null)
 			this.enableDefault = ((Boolean)enableDefault).booleanValue();
 	}
 }
