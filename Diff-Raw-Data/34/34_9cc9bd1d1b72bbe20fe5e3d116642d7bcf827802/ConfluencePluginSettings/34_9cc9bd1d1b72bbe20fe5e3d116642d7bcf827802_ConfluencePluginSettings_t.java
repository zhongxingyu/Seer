 package com.atlassian.sal.confluence.pluginsettings;
 
import java.util.List;
import java.util.Properties;

 import com.atlassian.bandana.BandanaManager;
 import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
 import com.atlassian.sal.api.pluginsettings.PluginSettings;
 
 public class ConfluencePluginSettings implements PluginSettings
 {
    private final BandanaManager bandanaManager;
    private final ConfluenceBandanaContext ctx;
 
    public ConfluencePluginSettings(final BandanaManager bandanaManager, final ConfluenceBandanaContext ctx)
     {
         this.bandanaManager = bandanaManager;
         this.ctx = ctx;
     }
 
    public Object put(final String key, final Object val)
     {
    	if ((val instanceof Properties) || (val instanceof List)  || (val instanceof String) )
		{
    		bandanaManager.setValue(ctx, key, val);
    		return val;
		} else
		{
            throw new IllegalArgumentException("Property type: "+val.getClass()+" not supported");
		}
     }
 
    public Object get(final String key)
     {
         return bandanaManager.getValue(ctx, key.toString());
     }
 
    public Object remove(final String key)
     {
        final Object val = get(key);
         put(key, null);
         return val;
     }
 }
