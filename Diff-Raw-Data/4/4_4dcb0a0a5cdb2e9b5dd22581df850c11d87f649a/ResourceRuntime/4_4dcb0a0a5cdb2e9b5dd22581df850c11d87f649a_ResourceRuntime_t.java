 package org.unidal.webres.resource.runtime;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.unidal.webres.helper.Servlets;
 
 public enum ResourceRuntime {
    INSTANCE;
 
    private Map<String, ResourceRuntimeConfig> m_map = new HashMap<String, ResourceRuntimeConfig>();
 
    public ResourceRuntimeConfig findConfigByWarName(String warName) {
       for (ResourceRuntimeConfig config : m_map.values()) {
          if (warName != null && warName.equals(config.getWarName())) {
             return config;
          }
       }
 
       return null;
    }
 
    public ResourceRuntimeConfig getConfig(String contextPath) {
       String path = Servlets.forContext().normalizeContextPath(contextPath);
       ResourceRuntimeConfig config = m_map.get(path);
 
       if (config == null) {
          throw new RuntimeException(String.format("Can't find resource runtime config with context path(%s)!", contextPath));
       }
 
       return config;
    }
 
    public boolean hasConfig(String contextPath) {
       return m_map.get(contextPath) != null;
    }
 
    ResourceRuntimeConfig loadConfig(String contextPath, File warRoot) {
       ResourceRuntimeConfig config = new ResourceRuntimeConfig(contextPath, warRoot);
 
       config.loadResourceProperties(ResourceRuntimeConfig.RESOURCE_PROPERTIES);
       config.loadResourceProfile(ResourceRuntimeConfig.RESOURCE_PROFILE);
       config.loadResourceVariations(ResourceRuntimeConfig.RESOURCE_VARIATIONS);
 
       synchronized (m_map) {
          if (m_map.get(contextPath) != null) {
             String message = "ResourceRuntimeConfig with contextPath(%s) has already been registered!";
 
             throw new RuntimeException(String.format(message, contextPath));
          }
 
          m_map.put(contextPath, config);
       }
 
       return config;
    }
 
    public void removeConfig(String contextPath) {
		if (contextPath != null && contextPath.length() == 0) {
			contextPath = null;
		}
   	
       synchronized (m_map) {
          m_map.remove(contextPath);
       }
    }
 
    public void reset() {
       m_map.clear();
    }
 }
