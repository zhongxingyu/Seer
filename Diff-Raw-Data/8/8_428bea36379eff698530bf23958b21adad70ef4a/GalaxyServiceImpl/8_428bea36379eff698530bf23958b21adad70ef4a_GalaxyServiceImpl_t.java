 package org.mule.galaxy.web.server;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.extension.Extension;
 import org.mule.galaxy.security.AccessControlManager;
 import org.mule.galaxy.security.Permission;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.security.UserManager;
 import org.mule.galaxy.util.SecurityUtils;
 import org.mule.galaxy.web.GwtFacet;
 import org.mule.galaxy.web.WebManager;
 import org.mule.galaxy.web.client.RPCException;
 import org.mule.galaxy.web.rpc.ApplicationInfo;
 import org.mule.galaxy.web.rpc.GalaxyService;
 import org.mule.galaxy.web.rpc.PluginTabInfo;
 import org.mule.galaxy.web.rpc.WExtensionInfo;
 import org.mule.galaxy.web.rpc.WUser;
 
 public class GalaxyServiceImpl implements GalaxyService {
 
     private Registry registry;
     private WebManager webManager;
     private UserManager userManager;
     private AccessControlManager accessControlManager;
     
     public ApplicationInfo getApplicationInfo() throws RPCException {
         ApplicationInfo info = new ApplicationInfo();
         info.setPluginTabs(getPluginTabs());
         info.setUser(getUserInfo());
         info.setUserManagementSupported(userManager.isManagementSupported());
         info.setExtensions(getExtensions());
         return info;
     }
 
     protected Collection<PluginTabInfo> getPluginTabs() {
         Collection<GwtFacet> facets = webManager.getGwtFacets();
         ArrayList<PluginTabInfo> wPlugins = new ArrayList<PluginTabInfo>();
         for (GwtFacet p : facets) {
             if (!p.getName().equals("core")) {
                 PluginTabInfo wp = new PluginTabInfo();
                 wp.setName(p.getName());
                 wp.setToken(p.getToken());
                 wPlugins.add(wp);
             }
         }
         return wPlugins;
     }
 
     public List<WExtensionInfo> getExtensions() throws RPCException {
         ArrayList<WExtensionInfo> exts = new ArrayList<WExtensionInfo>();
         for (Extension e : registry.getExtensions()) {
             exts.add(new WExtensionInfo(e.getId(), e.getName(), e.getPropertyDescriptorConfigurationKeys(), e.isMultivalueSupported()));
         }
         return exts;
     }
 
     public WUser getUserInfo() throws RPCException {
         User user = SecurityUtils.getCurrentUser();
         WUser w = SecurityServiceImpl.createWUser(user);
 
         List<String> perms = new ArrayList<String>();
 
         for (Permission p : accessControlManager.getGrantedPermissions(user)) {
             perms.add(p.toString());
         }
         w.setPermissions(perms);
 
         Map<String,String> properties = new HashMap<String,String>();
        if (user.getProperties() != null) {
            for (Map.Entry<String, Object> entry : user.getProperties().entrySet()) {
                if (entry.getValue() instanceof String) {
                    properties.put(entry.getKey(), (String)entry.getValue());
                }
             }
         }
         w.setProperties(properties);
         
         return w;
     }
 
     public void setWebManager(WebManager webManager) {
         this.webManager = webManager;
     }
 
     public void setRegistry(Registry registry) {
         this.registry = registry;
     }
 
     public void setUserManager(UserManager userManager) {
         this.userManager = userManager;
     }
 
     public void setAccessControlManager(AccessControlManager accessControlManager) {
         this.accessControlManager = accessControlManager;
     }
     
 
 }
