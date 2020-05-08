 /* This code is part of Freenet. It is distributed under the GNU General
  * Public License, version 2 (or at your option any later version). See
  * http://www.gnu.org/ for further details of the GPL. */
 package plugins.HelloFCP;
 
 import freenet.pluginmanager.FredPlugin;
 import freenet.pluginmanager.FredPluginFCP;
 import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.PluginNotFoundException;
 import freenet.pluginmanager.PluginReplySender;
 import freenet.pluginmanager.PluginRespirator;
 import freenet.support.SimpleFieldSet;
 import freenet.support.api.Bucket;
 
 /**
  * @author saces
  *
  */
 public class HelloFCP implements FredPlugin, FredPluginThreadless, FredPluginFCP {
 
 	public void runPlugin(PluginRespirator pr) {
 		
 	}
 
 	public void terminate() {
 	
 	}
 	
 	/**
 	 * access flag: FredPluginFCP.ACCESS_DIRECT:         direct call (plugin to plugin);
 	 *              FredPluginFCP.ACCESS_FCP_RESTRICTED: FCP restricted access;
 	 *              FredPluginFCP.ACCESS_FCP_FULL :      FCP full access  
 	 * 
 	 */
 	public void handle(PluginReplySender replysender, SimpleFieldSet params, Bucket data, int accesstype) {
 		// simple echo
		try {
			replysender.send(params, data);
		} catch (PluginNotFoundException e) {
			System.out.println("Connection to request sender lost.");
			e.printStackTrace();
		}
 	}
 
 }
