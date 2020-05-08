 /* Copyright (c) 2007 Bug Labs, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.buglabs.osgi.concierge.runtime;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 
 /**
  * The activator class controls the plug-in life cycle
  * 
  * Angel Roman - roman@mdesystems.com
  */
 public class ConciergeRuntime extends AbstractUIPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "com.buglabs.osgi.concierge.runtime";
 
 	// The shared instance
 	private static ConciergeRuntime plugin;
 
 	private BundleContext context;
 
 	/**
 	 * The constructor
 	 */
 	public ConciergeRuntime() {
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		this.context = context;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static ConciergeRuntime getDefault() {
 		return plugin;
 	}
 
 	public List getConciergeJars() {
 		ArrayList jars = new ArrayList();
 
 		String loc = "";
 
 		try {
 			loc = getJarsLocation();
 			
 		} catch (Exception e) {
			//TODO: Handle exception, log it.
 		}
 
 		if(!loc.equals("")){
 			File bugKernelLoc = new File(loc);
 
 			if(bugKernelLoc.exists()) {
 
 				File[] libraries = bugKernelLoc.listFiles(new FilenameFilter(){
 
 					public boolean accept(File dir, String name) {
 						if(name.endsWith(".jar") && !name.startsWith("org.eclipse.swt")) {
 							return true;
 						}
 						return false;
 					}});
 
 				jars.addAll(Arrays.asList(libraries));
 			}
 		}
 
 		return jars;
 	}
 
 	public String getJarsLocation() throws IOException, URISyntaxException {
 		return getFileSystemLocation("/jars");
 	}
 	
 	/**
 	 * Returns the absolute file system path pertaining to the relative bundle path.
 	 * 
 	 * @param path a bundle relative path of the filesystem resource.
 	 * @return
 	 * @throws IOException 
 	 * @throws URISyntaxException 
 	 */
 	private String getFileSystemLocation(String path) throws IOException, URISyntaxException {
 		URL locURL = context.getBundle().getEntry(path);
 		URL fileURL = FileLocator.toFileURL(locURL);
 		File locDir = new File(fileURL.getFile());
 		return locDir.getAbsolutePath();
 	}
 
 	public File getBundlesLocation() {
 		return getStateLocation().append("/bundles").toFile();
 	}
 }
