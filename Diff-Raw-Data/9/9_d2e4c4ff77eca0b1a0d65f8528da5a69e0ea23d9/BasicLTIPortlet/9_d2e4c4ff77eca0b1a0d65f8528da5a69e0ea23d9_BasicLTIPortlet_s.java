 /**
  * Copyright 2010-2011 The Australian National University
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package au.edu.anu.portal.portlets.basiclti;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.GenericPortlet;
 import javax.portlet.PortletConfig;
 import javax.portlet.PortletException;
 import javax.portlet.PortletMode;
 import javax.portlet.PortletModeException;
 import javax.portlet.PortletPreferences;
 import javax.portlet.PortletRequest;
 import javax.portlet.PortletRequestDispatcher;
 import javax.portlet.PortletSession;
 import javax.portlet.ReadOnlyException;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 import javax.portlet.ValidatorException;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import au.edu.anu.portal.portlets.basiclti.adapters.BasicLTIAdapterFactory;
 import au.edu.anu.portal.portlets.basiclti.adapters.IBasicLTIAdapter;
 import au.edu.anu.portal.portlets.basiclti.support.CollectionsSupport;
 import au.edu.anu.portal.portlets.basiclti.support.HttpSupport;
 import au.edu.anu.portal.portlets.basiclti.support.OAuthSupport;
 import au.edu.anu.portal.portlets.basiclti.utils.Constants;
 import au.edu.anu.portal.portlets.basiclti.utils.Messages;
 
 
 /**
  * BasicLTIPortlet
  * 
  * This is the portlet class.
  * 
  * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
  *
  */
 public class BasicLTIPortlet extends GenericPortlet{
 
 	private final Log log = LogFactory.getLog(getClass().getName());
 	
 	// pages
 	private String viewUrl;
 	private String proxyUrl;
 	private String errorUrl;
 	private String configUrl;
 	private String editUrl;
 	
 	//attribute mappings
 	private String attributeMappingForUsername;
 
 	//adapter classes
 	private Map<String,String> adapterClasses;
 	
 	//cache
 	private Cache cache;
	private final String CACHE_NAME = "au.edu.anu.portal.portlets.cache.BasicLTIPortletCache";
 	
 	public void init(PortletConfig config) throws PortletException {	   
 	   super.init(config);
 	   log.info("Basic LTI Portlet init()");
 	   
 	   //pages
 	   viewUrl = config.getInitParameter("viewUrl");
 	   proxyUrl = config.getInitParameter("proxyUrl");
 	   errorUrl = config.getInitParameter("errorUrl");
 	   configUrl = config.getInitParameter("configUrl");
 	   editUrl = config.getInitParameter("editUrl");
 
 	   //params
 	   attributeMappingForUsername = config.getInitParameter("portal.attribute.mapping.username");
 
 	   //adapter classes
 	   adapterClasses = initAdapters(config);
 
 	   //setup cache
 	   CacheManager manager = new CacheManager();
 	   cache = manager.getCache(CACHE_NAME);
 	}
 	
 	/**
 	 * Delegate to appropriate PortletMode.
 	 */
 	protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException {
 		log.info("Basic LTI doDispatch()");
 
 		if (StringUtils.equalsIgnoreCase(request.getPortletMode().toString(), "CONFIG")) {
 			doConfig(request, response);
 		}
 		else {
 			super.doDispatch(request, response);
 		}
 	}
 	
 	
 	/**
 	 * Render the main view
 	 */
 	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
 		log.info("Basic LTI doView()");
 		
 		//get data
 		Map<String,String> launchData = setupLaunchData(request, response);
 		
 		//catch - errors already handled
 		if(launchData == null) {
 			return;
 		}
 		
 		//setup the params, serialise to a URL
 		StringBuilder proxy = new StringBuilder();
 		proxy.append(request.getContextPath());
 		proxy.append(proxyUrl);
 		proxy.append("?");
 		proxy.append(HttpSupport.serialiseMapToQueryString(launchData));
 		
 		request.setAttribute("proxyContextUrl", proxy.toString());
 		request.setAttribute("preferredHeight", getConfiguredPortletHeight(request));
 		
 		dispatch(request, response, viewUrl);
 	}	
 	
 	
 	
 	/**
 	 * Custom mode handler for CONFIG view
 	 */
 	protected void doConfig(RenderRequest request, RenderResponse response) throws PortletException, IOException {
 		log.info("Basic LTI doConfig()");
 
 		request.setAttribute("configuredPortletHeight", getConfiguredPortletHeight(request));
 		request.setAttribute("configuredPortletTitle", getConfiguredPortletTitle(request));
 		request.setAttribute("configuredProviderType", getConfiguredProviderType(request));
 		request.setAttribute("configuredLaunchData", getConfiguredLaunchData(request));
 		request.setAttribute("key", getBasicLTIKey(request));
 		request.setAttribute("secret", getBasicLTISecret(request));
 
 		dispatch(request, response, configUrl);
 	}
 	
 	/**
 	 * Handler for edit mode
 	 */
 	protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
 		log.info("Basic LTI doEdit()");
 
 		request.setAttribute("configuredPortletHeight", getConfiguredPortletHeight(request));
 		request.setAttribute("configuredPortletTitle", getConfiguredPortletTitle(request));
 
 		dispatch(request, response, editUrl);
 	}
 	
 	
 	
 	/**
 	 * Process any portlet actions. At this stage they are all from the submission of the CONFIG mode.
 	 */
 	public void processAction(ActionRequest request, ActionResponse response) {
 		log.info("Basic LTI processAction()");
 		
 		//check mode and delegate
 		if (StringUtils.equalsIgnoreCase(request.getPortletMode().toString(), "CONFIG")) {
 			processConfigAction(request, response);
 		} else if (StringUtils.equalsIgnoreCase(request.getPortletMode().toString(), "EDIT")) {
 			processEditAction(request, response);
 		} else {
 			log.error("No handler for PortletMode: " + request.getPortletMode().toString());
 		}
 	}
 	
 	/**
 	 * Helper to process CONFIG mode actions
 	 * @param request
 	 * @param response
 	 */
 	private void processConfigAction(ActionRequest request, ActionResponse response) {
 		log.info("Basic LTI processConfigAction()");
 		
 		boolean success = true;
 		//get prefs and submitted values
 		PortletPreferences prefs = request.getPreferences();
 		String portletHeight = request.getParameter("portletHeight");
 		String portletTitle = request.getParameter("portletTitle");
 		String providerType = request.getParameter("providerType");
 		String launchData = request.getParameter("launchData");
 		String key = request.getParameter("key");
 		String secret = request.getParameter("secret");
 		
 		//get version form prefs and increment it.
 		int newVersion = Integer.valueOf(prefs.getValue("version", "0")) + 1;
 		
 		//validate
 		try {
 			prefs.setValue("portlet_height", portletHeight);
 			prefs.setValue("portlet_title", portletTitle);
 			prefs.setValue("provider_type", providerType);
 			prefs.setValue("launch_data", launchData);
 			prefs.setValue("key", key);
 			prefs.setValue("secret", secret);
 			prefs.setValue("version", String.valueOf(newVersion));
 		} catch (ReadOnlyException e) {
 			success = false;
 			response.setRenderParameter("errorMessage", Messages.getString("error.form.readonly.error"));
 			log.error(e);
 		}
 		
 		//save them
 		if(success) {
 			try {
 				prefs.store();
 				response.setPortletMode(PortletMode.VIEW);
 			} catch (ValidatorException e) {
 				response.setRenderParameter("errorMessage", e.getMessage());
 				log.error(e);
 			} catch (IOException e) {
 				response.setRenderParameter("errorMessage", Messages.getString("error.form.save.error"));
 				log.error(e);
 			} catch (PortletModeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Helper to process EDIT mode actions
 	 * @param request
 	 * @param response
 	 */
 	private void processEditAction(ActionRequest request, ActionResponse response) {
 		log.debug("Basic LTI processEditAction()");
 
 		boolean success = true;
 		//get prefs and submitted values
 		PortletPreferences prefs = request.getPreferences();
 		String portletHeight = request.getParameter("portletHeight");
 		String portletTitle = request.getParameter("portletTitle");
 		
 		//validate
 		try {
 			prefs.setValue("portlet_height", portletHeight);
 			prefs.setValue("portlet_title", portletTitle);
 		} catch (ReadOnlyException e) {
 			success = false;
 			response.setRenderParameter("errorMessage", Messages.getString("error.form.readonly.error"));
 			log.error(e);
 		}
 		
 		//save them
 		if(success) {
 			try {
 				prefs.store();
 				response.setPortletMode(PortletMode.VIEW);
 			} catch (ValidatorException e) {
 				response.setRenderParameter("errorMessage", e.getMessage());
 				log.error(e);
 			} catch (IOException e) {
 				response.setRenderParameter("errorMessage", Messages.getString("error.form.save.error"));
 				log.error(e);
 			} catch (PortletModeException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 			
 	/**
 	 * Get the current user's details, exposed via portlet.xml
 	 * @param request
 	 * @return Map<String,String> of info
 	 */
 	@SuppressWarnings("unchecked")
 	private Map<String,String> getUserInfo(RenderRequest request) {
 		return (Map<String,String>)request.getAttribute(PortletRequest.USER_INFO);
 	}
 	
 	
 	/**
 	 * Gets the unique namespace for this portlet
 	 * @param response
 	 * @return
 	 */
 	private String getPortletNamespace(RenderResponse response) {
 		return response.getNamespace();
 	}
 	
 	/**
 	 * Get the appropriate adapter class for this configured portlet instance
 	 * @param request
 	 * @return
 	 */
 	private String getAdapterClassName(RenderRequest request) {
 		return adapterClasses.get(getConfiguredProviderType(request));
 	}
 	
 	/**
 	 * Setup the parameters for the request
 	 * @param request
 	 * @param response
 	 * @return Map of params or null if any required data is missing
 	 */
 	private Map<String,String> setupLaunchData(RenderRequest request, RenderResponse response) {
 		
 		Map<String,String> params = new HashMap<String,String>();
 		
 		//get essential Basic LTI config
 		String key = getBasicLTIKey(request);
 		String secret = getBasicLTISecret(request);
 
 		if(StringUtils.isBlank(key) || StringUtils.isBlank(secret)) {
 			log.error("Basic LTI key/secret was blank. Please configure this portlet.");
 			doError("error.no.basiclti.config", "error.heading.general", request, response);
 			return null;
 		}
 		
 		//check for the version of the preference and see if we need to reset the cache
 		int prefVersion = getPreferenceVersion(request);
 		PortletSession session = request.getPortletSession();
 		Integer cachedVersion  = (Integer)session.getAttribute("version");
 		
 		log.info("preference version: " + prefVersion + ", cached version: " + cachedVersion);
 		
 		//if the pref version is newer or we have no cached version, evict.
 		if(cachedVersion == null || prefVersion > cachedVersion.intValue()) {
 			log.info("Cache is dirty");
 			evictFromCache(getPortletNamespace(response));
 		}
 		
 		
 		//check cache, otherwise form up all of the data
 		String cacheKey = getPortletNamespace(response);
 		Element element = cache.get(cacheKey);
 		if(element != null) {
 			log.info("Fetching data from cache for: " + cacheKey);
 			params = (Map<String, String>) element.getObjectValue();
 		} else {
 		
 			//get the configured launch data
 			String rawLaunchData = getConfiguredLaunchData(request);
 			
 			//process it to a map
 			params = CollectionsSupport.splitStringToMap(rawLaunchData, ";;", "=", true);
 			
 			//setup adapter
 			String adapterClassName = getAdapterClassName(request);
 			String providerType = getConfiguredProviderType(request);
 
 			if(log.isDebugEnabled()) {
 				log.info("Adapter: " + adapterClassName);
 				log.info("ProviderType: " + providerType);
 			}
 	
 			//get user info
 			Map<String,String> userInfo = getUserInfo(request);
 			
 			//add required user fields
 			params.put("user_id", userInfo.get("username"));
 			params.put("lis_person_name_given", userInfo.get("givenName"));
 			params.put("lis_person_name_family", userInfo.get("sn"));
 			params.put("lis_person_name_full", userInfo.get("displayName"));
 			params.put("lis_person_contact_email_primary", userInfo.get("mail"));
 			
 			//add required basic LTI fields
 			params.put("resource_link_id", getPortletNamespace(response));
 			params.put("tool_consumer_instance_guid", key);
 			
 			
 			//process the params according to the adapter in use
 			BasicLTIAdapterFactory factory = new BasicLTIAdapterFactory();
 			IBasicLTIAdapter adapter = factory.newAdapter(getAdapterClassName(request));  
 			params = adapter.processLaunchData(params);
 		
 			//cache the data, must be done before signing
 			log.info("Adding data to cache for: " + cacheKey);
 			cache.put(new Element(cacheKey, params));
 			
 			//also update the portlet session attribute for the preference version
 			//but only if we have a valid version 
 			if(prefVersion > -1) {
 				log.info("Adding version to PortletSession: " + prefVersion);
 				session.setAttribute("version", prefVersion);
 			}
 		}
 		
 		if(log.isDebugEnabled()) {
 			log.debug("Parameter map before OAuth signing");
 			CollectionsSupport.printMap(params);
 		}
 		
 		//sign the properties map
 		params = OAuthSupport.signProperties(params.get("endpoint_url"), params, "POST", key, secret);
 
 		if(log.isDebugEnabled()) {
 			log.debug("Parameter map after OAuth signing");
 			CollectionsSupport.printMap(params);
 		}
 		
 		return params;
 	}
 	
 	
 	/**
 	 * Get the provider type from the preferences/configuration, or default from Constants
 	 * @param request
 	 * @return
 	 */
 	private String getConfiguredProviderType(RenderRequest request) {
 	      PortletPreferences pref = request.getPreferences();
 	      return pref.getValue("provider_type", Constants.DEFAULT_PROVIDER_TYPE);
 	}
 	
 	/**
 	 * Get the preferred portlet title if set, or default from Constants
 	 * @param request
 	 * @return
 	 */
 	private String getConfiguredPortletTitle(RenderRequest request) {
 		PortletPreferences pref = request.getPreferences();
 		return pref.getValue("portlet_title", Constants.PORTLET_TITLE_DEFAULT);
 	}
 	
 	/**
 	 * Get the preferred portlet height if set, or default from Constants
 	 * @param request
 	 * @return
 	 */
 	private int getConfiguredPortletHeight(RenderRequest request) {
 	      PortletPreferences pref = request.getPreferences();
 	      return Integer.parseInt(pref.getValue("portlet_height", String.valueOf(Constants.PORTLET_HEIGHT_DEFAULT)));
 	}
 	
 	/**
 	 * Get the launch data from the preferences/configuration, no default.
 	 * @param request
 	 * @return
 	 */
 	private String getConfiguredLaunchData(RenderRequest request) {
 		PortletPreferences pref = request.getPreferences();
 		return pref.getValue("launch_data", "");
 	}
 	
 	/**
 	 * Get the configured Basic LTI key, no default.
 	 * @param request
 	 * @return
 	 */
 	private String getBasicLTIKey(RenderRequest request) {
 		PortletPreferences pref = request.getPreferences();
 		return pref.getValue("key", "");
 	}
 
 	/**
 	 * Get the configured Basic LTI secret, no default.
 	 * @param request
 	 * @return
 	 */
 	private String getBasicLTISecret(RenderRequest request) {
 		PortletPreferences pref = request.getPreferences();
 		return pref.getValue("secret", "");
 	}
 	
 	/**
 	 * Get the version of this configuration instance used to check if the cache is expired.
 	 * This value is incremented each time the config is updated.
 	 * Default is -1 to signal there is no stored preference
 	 * 
 	 * Do not use this for getting the number that we need to increment.
 	 * 
 	 * @param request
 	 * @return
 	 */
 	private int getPreferenceVersion(RenderRequest request) {
 		PortletPreferences pref = request.getPreferences();
 		return Integer.parseInt(pref.getValue("version", "-1"));
 	}
 		
 	
 	/**
 	 * Get the current username
 	 * @param request
 	 * @return
 	 */
 	private String getAuthenticatedUsername(RenderRequest request) {
 		Map<String,String> userInfo = getUserInfo(request);
 		//return userInfo.get("username");
 		return userInfo.get(attributeMappingForUsername);
 	}
 	
 	
 	/**
 	 * Override GenericPortlet.getTitle() to use the preferred title for the portlet instead
 	 */
 	@Override
 	protected String getTitle(RenderRequest request) {
 		return getConfiguredPortletTitle(request);
 	}
 	
 	/**
 	 * Helper to evict an item from a cache. If we visit the edit mode, we must evict the current data. It will be re-cached later.
 	 * @param cacheKey	the id for the data in the cache
 	 */
 	private void evictFromCache(String cacheKey) {
 		cache.remove(cacheKey);
 		log.info("Evicted data in cache for key: " + cacheKey);
 	}
 	
 	/**
 	 * Processes the init params to get the adapter class map. This method iterates over every init-param, selects the appropriate adapter ones,
 	 * fetches the param value and then strips the 'adapter-class-' prefix from the param name for storage in the map.
 	 * @param config	PortletConfig
 	 * @return	Map of adapter names and classes
 	 */
 	private Map<String,String> initAdapters(PortletConfig config) {
 		
 		Map<String,String> m = new HashMap<String,String>();
 		
 		String ADAPTER_CLASS_PREFIX = "adapter-class-";
 		
 		//get it into a usable form
 	    List<String> paramNames = Collections.list((Enumeration<String>)config.getInitParameterNames());
 		
 	    //iterate over, select the appropriate ones, retrieve the param value then strip param name for storage.
 		for(String paramName: paramNames) {
 			if(StringUtils.startsWith(paramName, ADAPTER_CLASS_PREFIX)) {
 				String adapterName = StringUtils.removeStart(paramName, ADAPTER_CLASS_PREFIX);
 				String adapterClass = config.getInitParameter(paramName);
 				
 				m.put(adapterName, adapterClass);
 				
 				log.info("Registered adapter: " + adapterName + " with class: " + adapterClass);
 			}
 		}
 		
 		log.info("Autowired: " + m.size() + " adapters");
 		
 		return m;
 	}
 
 	
 
 	/**
 	 * Helper to handle error messages
 	 * @param messageKey	Message bundle key
 	 * @param headingKey	optional error heading message bundle key, if not specified, the general one is used
 	 * @param request
 	 * @param response
 	 */
 	private void doError(String messageKey, String headingKey, RenderRequest request, RenderResponse response){
 		
 		//message
 		request.setAttribute("errorMessage", Messages.getString(messageKey));
 		
 		//optional heading
 		if(StringUtils.isNotBlank(headingKey)){
 			request.setAttribute("errorHeading", Messages.getString(headingKey));
 		} else {
 			request.setAttribute("errorHeading", Messages.getString("error.heading.general"));
 		}
 		
 		//dispatch
 		try {
 			dispatch(request, response, errorUrl);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Dispatch to a JSP or servlet
 	 * @param request
 	 * @param response
 	 * @param path
 	 * @throws PortletException
 	 * @throws IOException
 	 */
 	protected void dispatch(RenderRequest request, RenderResponse response, String path)throws PortletException, IOException {
 		response.setContentType("text/html"); 
 		PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(path);
 		dispatcher.include(request, response);
 	}
 
 	
 	
 	public void destroy() {
 		log.info("destroy()");
 	}
 	
 	
 }
