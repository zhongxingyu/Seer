 package net.bioclipse.xws4j;
 
 import org.eclipse.core.runtime.FileLocator; 
 
 import org.osgi.framework.BundleContext;
 
 import java.io.File;
 
 import java.net.URL;
 
 import java.util.Enumeration;
 
 import net.bioclipse.xws.binding.BindingDefinitions;
 
 /**
  * 
  * This file is part of the Bioclipse xws4j Plug-in.
  * 
  * Copyright (C) 2008 Johannes Wagener
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, see <http://www.gnu.org/licenses>.
  * 
  * @author Johannes Wagener
  */
 public class DefaultBindingDefinitions extends BindingDefinitions {
 	
 	private static String getTargetDirectory(BundleContext context) {
 		
 		String target_dir = "c:/test123-ws";
 		
 		XwsConsole.writeToConsole("Target directory for bindings is: " + target_dir);
 		
 		return target_dir;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static String getClasspathString(BundleContext context) {
 		
 		StringBuilder builder = new StringBuilder();
 		
		String sep = System.getProperty("line.separator");
		
 		try {
 			File plugin_dir = FileLocator.getBundleFile(context.getBundle());
 			
 			// List all .jar files in the jars directory and below
 			Enumeration elements = context.getBundle().findEntries("jars", "*.jar", true);
 			
 			while (elements.hasMoreElements()) {
 			 	URL url = (URL)elements.nextElement();
 			 	File jar_file_location = new File(plugin_dir, File.separator + url.getFile());
			 	builder.append(jar_file_location.getAbsoluteFile() + sep);
 			}
 			
 			// try to add rt.jar (hope that it is on the classpath.)
 			builder.append("rt.jar");
 			
 			XwsConsole.writeToConsole("Classpath for binding compiler is: " + builder.toString());
 		} catch (Exception e) {
 			XwsConsole.writeToConsoleRed("Error, could not construct classpath for binding compiler.");
 		}
 		
 		return builder.toString();
 	}
 	
 	public DefaultBindingDefinitions(BundleContext context) {
 		
 		/**
 		 *  TODO: update this! the target directory must point to
 		 *  a XMPP Web Services Binding folder within the local resources.
 		 */
 		
 		super(getTargetDirectory(context), getClasspathString(context));
 	}
 }
