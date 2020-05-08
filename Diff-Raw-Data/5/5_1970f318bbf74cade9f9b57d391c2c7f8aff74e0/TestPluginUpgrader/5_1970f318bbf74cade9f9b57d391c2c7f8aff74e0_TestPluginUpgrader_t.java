 package com.atlassian.sal.api.upgrade;
 
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.PluginInformation;
 import com.atlassian.sal.api.pluginsettings.PluginSettings;
 import com.atlassian.sal.api.upgrade.PluginUpgradeTask;
 import com.atlassian.sal.api.upgrade.PluginUpgrader;
 import com.mockobjects.dynamic.C;
 import com.mockobjects.dynamic.Mock;
 import junit.framework.TestCase;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 public class TestPluginUpgrader extends TestCase
 {
     Mock mockPlugin;
     Mock mockPluginSettings;
 
     public void setUp()
     {
         mockPlugin = new Mock(Plugin.class);
         mockPluginSettings = new Mock(PluginSettings.class);
     }
 
     public void testUpgradeFromScratch()
     {
         final UpgradeTaskStub task = new UpgradeTaskStub(10);
         PluginUpgrader pu = new PluginUpgrader((Plugin)mockPlugin.proxy(),
                                                (PluginSettings) mockPluginSettings.proxy(),
                                                new ArrayList<PluginUpgradeTask>() {{ add(task); }});
 
         PluginInformation info = new PluginInformation();
         mockPlugin.matchAndReturn("getKey", "foo.bar");
         mockPlugin.matchAndReturn("getPluginInformation", info);
         mockPluginSettings.expectAndReturn("get", C.args(C.eq("foo.bar:build")), null);
         mockPluginSettings.expectAndReturn("get", C.args(C.eq("foo.bar:build")), null);
        mockPluginSettings.expectAndReturn("get", C.args(C.eq("foo.bar:build")), null);
         mockPluginSettings.expect("put", C.args(C.eq("foo.bar:build"), C.eq("10")));
 
         pu.upgrade();
 
         assertTrue(task.isUpgraded());
         mockPlugin.verify();
         mockPluginSettings.verify();
     }
 
     public void testUpgradeFrom2To5()
     {
     	final UpgradeTaskStub task1 = new UpgradeTaskStub(1);
     	final UpgradeTaskStub task2 = new UpgradeTaskStub(2);
     	final UpgradeTaskStub task3 = new UpgradeTaskStub(3);
     	final UpgradeTaskStub task4 = new UpgradeTaskStub(4);
     	final UpgradeTaskStub task5 = new UpgradeTaskStub(5);
     	
     	PluginSettings pluginSettings = new PluginSettings()
 		{
     		private Map<String, Object> map = new HashMap<String, Object>();
 			public Object remove(String key)
 			{
 				return map.remove(key);
 			}
 			public Object put(String key, Object value)
 			{
 				return map.put(key, value);
 			}
 			public Object get(String key)
 			{
 				return map.get(key);
 			}
 		};
 		
 		pluginSettings.put("foo.bar:build", "2"); // current data version is 2
     	PluginUpgrader pu = new PluginUpgrader((Plugin)mockPlugin.proxy(), pluginSettings, 
     			Arrays.asList(new PluginUpgradeTask[] {task1, task5, task3, task2, task4})); // intentionally not in right order
     	
     	PluginInformation info = new PluginInformation();
     	mockPlugin.matchAndReturn("getKey", "foo.bar");
     	mockPlugin.matchAndReturn("getPluginInformation", info);
     	
     	pu.upgrade();
     	
     	assertFalse(task1.isUpgraded());		// not upgraded because data version was already 2
     	assertFalse(task2.isUpgraded());		// not upgraded because data version was already 2
     	assertTrue(task3.isUpgraded());			// should upgrade
     	assertTrue(task4.isUpgraded());			// should upgrade
     	assertTrue(task5.isUpgraded());			// should upgrade
     	
     	mockPlugin.verify();
     }
 
     public void testUpgradeOldVersion()
     {
         final UpgradeTaskStub task = new UpgradeTaskStub(10);
         PluginUpgrader pu = new PluginUpgrader((Plugin)mockPlugin.proxy(),
                                                (PluginSettings) mockPluginSettings.proxy(),
                                                new ArrayList<PluginUpgradeTask>() {{ add(task); }});
 
         PluginInformation info = new PluginInformation();
         mockPlugin.matchAndReturn("getKey", "foo.bar");
         mockPlugin.matchAndReturn("getPluginInformation", info);
         mockPluginSettings.expectAndReturn("get", C.args(C.eq("foo.bar:build")), "5");
         mockPluginSettings.expectAndReturn("get", C.args(C.eq("foo.bar:build")), "5");
        mockPluginSettings.expectAndReturn("get", C.args(C.eq("foo.bar:build")), "5");
         mockPluginSettings.expect("put", C.args(C.eq("foo.bar:build"), C.eq("10")));
 
         pu.upgrade();
 
         assertTrue(task.isUpgraded());
         mockPlugin.verify();
         mockPluginSettings.verify();
     }
 
     public void testUpgradeNoTasks()
     {
         final UpgradeTaskStub task = new UpgradeTaskStub(10);
         PluginUpgrader pu = new PluginUpgrader((Plugin)mockPlugin.proxy(),
                                                (PluginSettings) mockPluginSettings.proxy(),
                                                new ArrayList<PluginUpgradeTask>() {{ add(task); }});
 
         PluginInformation info = new PluginInformation();
         mockPlugin.matchAndReturn("getKey", "foo.bar");
         mockPlugin.matchAndReturn("getPluginInformation", info);
         mockPluginSettings.expectAndReturn("get", C.args(C.eq("foo.bar:build")), "15");
         pu.upgrade();
 
         assertFalse(task.isUpgraded());
         mockPlugin.verify();
         mockPluginSettings.verify();
     }
 
     public void testUpgrade()
     {
         final UpgradeTaskStub task = new UpgradeTaskStub(10);
         PluginUpgrader pu = new PluginUpgrader((Plugin)mockPlugin.proxy(),
                                                (PluginSettings) mockPluginSettings.proxy(),
                                                new ArrayList<PluginUpgradeTask>() {{ add(task); }});
 
         mockPlugin.matchAndReturn("getKey", "foo.bar");
         mockPlugin.matchAndReturn("getPluginInformation", new PluginInformation());
         mockPluginSettings.expectAndReturn("get", C.args(C.eq("foo.bar:build")), "5");
         mockPluginSettings.expectAndReturn("get", C.args(C.eq("foo.bar:build")), "5");
        mockPluginSettings.expectAndReturn("get", C.args(C.eq("foo.bar:build")), "5");
 
         pu.upgrade();
 
         assertTrue(task.isUpgraded());
         mockPlugin.verify();
         mockPluginSettings.verify();
     }
 
 }
