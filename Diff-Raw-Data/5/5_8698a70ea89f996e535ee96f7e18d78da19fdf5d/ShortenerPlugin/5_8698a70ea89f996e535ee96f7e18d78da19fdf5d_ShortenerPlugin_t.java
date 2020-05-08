 /*
  * shortener - ShortenerPlugin.java - Copyright © 2010 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package plugin.shortener;
 
 import freenet.pluginmanager.FredPlugin;
 import freenet.pluginmanager.FredPluginFCP;
 import freenet.pluginmanager.FredPluginHTTP;
 import freenet.pluginmanager.FredPluginThreadless;
 import freenet.pluginmanager.PluginHTTPException;
 import freenet.pluginmanager.PluginReplySender;
 import freenet.pluginmanager.PluginRespirator;
 import freenet.support.SimpleFieldSet;
 import freenet.support.api.Bucket;
 import freenet.support.api.HTTPRequest;
 
 /**
  * Main class of the shortener plugin.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class ShortenerPlugin implements FredPlugin, FredPluginHTTP, FredPluginThreadless, FredPluginFCP {
 
 	/**
 	 * {@inheritDoc}
 	 */
	public void runPlugin(PluginRespirator pluginRespirator) {
 		/* TODO - implements */
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void terminate() {
 		/* TODO - implements */
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String handleHTTPGet(HTTPRequest request) throws PluginHTTPException {
 		/* TODO - implements */
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String handleHTTPPost(HTTPRequest request) throws PluginHTTPException {
 		/* TODO - implements */
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
	public void handle(PluginReplySender replySender, SimpleFieldSet parameters, Bucket data, int accessType) {
 		/* TODO - implements */
 	}
 
 }
