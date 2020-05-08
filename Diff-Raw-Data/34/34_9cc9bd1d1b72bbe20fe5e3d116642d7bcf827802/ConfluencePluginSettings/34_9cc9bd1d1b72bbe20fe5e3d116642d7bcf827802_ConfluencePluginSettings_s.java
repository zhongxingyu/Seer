 package com.atlassian.sal.confluence.pluginsettings;
 
 import com.atlassian.bandana.BandanaManager;
 import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
 import com.atlassian.sal.api.pluginsettings.PluginSettings;
 
 public class ConfluencePluginSettings implements PluginSettings
 {
    private BandanaManager bandanaManager;
    private ConfluenceBandanaContext ctx;
 
    public ConfluencePluginSettings(BandanaManager bandanaManager, ConfluenceBandanaContext ctx)
     {
         this.bandanaManager = bandanaManager;
         this.ctx = ctx;
     }
 
    public Object put(String key, Object val)
     {
        bandanaManager.setValue(ctx, key, val);
        return val;
     }
 
    public Object get(String key)
     {
         return bandanaManager.getValue(ctx, key.toString());
     }
 
    public Object remove(String key)
     {
        Object val = get(key);
         put(key, null);
         return val;
     }
 }
