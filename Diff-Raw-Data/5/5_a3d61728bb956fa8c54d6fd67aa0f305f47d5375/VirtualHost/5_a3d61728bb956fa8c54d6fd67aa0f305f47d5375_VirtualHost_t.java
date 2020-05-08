 package com.emergentideas.webhandle.assumptions.acorn;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.emergentideas.logging.Logger;
 import com.emergentideas.logging.SystemOutLogger;
 import com.emergentideas.webhandle.AppLocation;
 import com.emergentideas.webhandle.Constants;
 import com.emergentideas.webhandle.Location;
 import com.emergentideas.webhandle.Name;
 import com.emergentideas.webhandle.Type;
 import com.emergentideas.webhandle.WebAppLocation;
 import com.emergentideas.webhandle.assumptions.oak.AppLoader;
 import com.emergentideas.webhandle.bootstrap.ConfigurationAtom;
 import com.emergentideas.webhandle.bootstrap.FlatFileConfigurationParser;
 import com.emergentideas.webhandle.bootstrap.FocusAndPropertiesAtom;
 import com.emergentideas.webhandle.bootstrap.FocusAndPropertiesConfigurationAtom;
 import com.emergentideas.webhandle.bootstrap.Loader;
 import com.emergentideas.webhandle.bootstrap.URLFocusAndPropertiesAtomizer;
 import com.emergentideas.webhandle.output.Respondent;
 
 @Name("request-handler")
 @Type("com.emergentideas.webhandle.assumptions.acorn.VirtualHost")
 public class VirtualHost implements Respondent {
 
 	protected List<ConfigurationAtom> currentAtoms = new ArrayList<ConfigurationAtom>();
 	protected Loader loader;
 	protected Location location;
 	protected File configurationsLocation;
 	
 	protected Map<String, Pattern> configValuesToPatterns = new HashMap<String, Pattern>();
 	protected Map<Pattern, Respondent> hostHandlers = Collections.synchronizedMap(new HashMap<Pattern, Respondent>());
 	
 	protected Logger log = SystemOutLogger.get(VirtualHost.class);
 	
 	
 	public void respond(ServletContext servletContext,
 			HttpServletRequest request, HttpServletResponse response) {
 		for(Pattern p : hostHandlers.keySet()) {
 			if(p.matcher(request.getServerName()).matches()) {
 				hostHandlers.get(p).respond(servletContext, request, response);
 				return;
 			}
 		}
 		
 	}
 
 	public void reload() {
 		
 		if(configurationsLocation == null) {
 			log.error("Configuration location was null.");
 			return;
 		}
 		
 		URLFocusAndPropertiesAtomizer atomizer = new URLFocusAndPropertiesAtomizer();
 		
 		try {
 			currentAtoms.clear();
 			List<ConfigurationAtom> atoms = new FlatFileConfigurationParser().parse(new FileInputStream(configurationsLocation));
 			List<String> newValues = new ArrayList<String>();
 			
 			// Remove any respondent that has a configuration that doesn't match
 			for(ConfigurationAtom atom : atoms) {
 				newValues.add(atom.getValue());
 			}
 			
 			for(String key : configValuesToPatterns.keySet()) {
 				if(newValues.contains(key) == false) {
 					removeHandlersForConfig(key);
 				}
 			}
 			
 			// Add new configurations
 			for(ConfigurationAtom atom : atoms) {
				FocusAndPropertiesAtom parsed = atomizer.atomize(atom.getType(), atom.getValue());
				currentAtoms.add(parsed);
 				if(configValuesToPatterns.containsKey(atom.getValue())) {
 					continue;
 				}
 				load(parsed);
 			}
 			
 		}
 		catch(Exception e) {
 			log.error("Could not reload file: " + configurationsLocation.getAbsolutePath());
 		}
 	}
 	
 	protected void removeHandlersForConfig(String conf) {
 		Pattern pat = configValuesToPatterns.get(conf);
 		configValuesToPatterns.remove(conf);
 		hostHandlers.remove(pat);
 	}
 	
 	public void load(FocusAndPropertiesAtom conf) {
 		try {
 			
 			removeHandlersForConfig(conf.getValue());
 			
 			AppLocation appLocation = new AppLocation(location);
 			WebAppLocation webApp = new WebAppLocation(appLocation);
 			webApp.init();
 			AppLoader loader = new AppLoader(appLocation);
 			appLocation.put(Constants.APP_LOCATION, appLocation);
 			loader.load(new File(conf.getFocus()));
 			Pattern hostPattern = Pattern.compile(conf.getProperties().get("hosts"));
 			configValuesToPatterns.put(conf.getValue(), hostPattern);
 			
 			Respondent respond = (Respondent)webApp.getServiceByName("request-handler");
 
 			hostHandlers.put(hostPattern, respond);
 		}
 		catch(Exception e) {
 			log.error("Could not load virtual host: " + conf.getValue());
 		}
 	}
 
 	public List<ConfigurationAtom> getCurrentAtoms() {
 		return currentAtoms;
 	}
 
 	public void setCurrentAtoms(List<ConfigurationAtom> currentAtoms) {
 		this.currentAtoms = currentAtoms;
 	}
 
 	public Loader getLoader() {
 		return loader;
 	}
 
 	public void setLoader(Loader loader) {
 		this.loader = loader;
 	}
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public void setLocation(Location location) {
 		location.put(Constants.ENV_LOCATION, location);
 		this.location = location;
 	}
 
 	public File getConfigurationsLocation() {
 		return configurationsLocation;
 	}
 
 	public void setConfigurationsLocation(File configurationsLocation) {
 		this.configurationsLocation = configurationsLocation;
 	}
 
 	public Map<String, Pattern> getConfigValuesToPatterns() {
 		return configValuesToPatterns;
 	}
 
 	public Map<Pattern, Respondent> getHostHandlers() {
 		return hostHandlers;
 	}
 
 }
