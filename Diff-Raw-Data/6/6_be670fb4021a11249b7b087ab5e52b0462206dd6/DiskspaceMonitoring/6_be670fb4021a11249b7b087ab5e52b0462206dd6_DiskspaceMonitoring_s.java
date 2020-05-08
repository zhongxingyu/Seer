 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.tools.sysmon.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.ResourceBundle;
 
 import org.apache.commons.io.FileSystemUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.service.monitor.Monitorable;
 import org.osgi.service.monitor.StatusVariable;
 
 /**
  * @scr.component name="os.disk" metatype="false"
  * @scr.service interface="org.osgi.service.monitor.Monitorable"
  * @scr.property name="Monitorable-Localization" value="/OSGI-INF/l10n/DiskspaceMonitoring"
  */
 public class DiskspaceMonitoring implements Monitorable {
 	public static final String PID = "os.disk";		
 	public static final String VAR_SPACE_FREE = "disk.space.free";
 	
 	/**
 	 * For logging
 	 */
 	private Log logger = LogFactory.getLog(this.getClass());	
 
 	@SuppressWarnings("serial")
 	private static final HashSet<String> VAR_NAMES = new HashSet<String>() {{
 		add(VAR_SPACE_FREE);
 	}};	
 	
 	/**
 	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
 	 */
 	private final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10n/DiskspaceMonitoring");	
 	
 	public String[] getStatusVariableNames() {
 		return VAR_NAMES.toArray(new String[VAR_NAMES.size()]);
 	}	
 	
 	public String getDescription(String name) throws IllegalArgumentException {
 		if (!VAR_NAMES.contains(name)) {
 			throw new IllegalArgumentException("Invalid Status Variable name " + name);
 		}		
 		
 		return this.rb.getString(name);
 	}
 
 	public StatusVariable getStatusVariable(String name) throws IllegalArgumentException {
 		if (!VAR_NAMES.contains(name)) {
 			throw new IllegalArgumentException("Invalid Status Variable name " + name);
 		}
 		
 		try {
 			/* 
 			 * Query free disk space.
 			 * 
 			 * We need to call this within a separate thread due to the following bug: 
 			 * https://bugs.pxl.li/view.php?id=278
 			 */
 			long freeDisk = -1;
 			FreeSpaceThread queryThread = new FreeSpaceThread();
 			queryThread.start();
 			
 			// waiting for the result
 			queryThread.join(10000);
 	
 			if (queryThread.isAlive()) {
				this.logger.warn("FreeSpace query thread is still alive!");
 			} else {
 				freeDisk = queryThread.freeSpace;
 			}
 			
 			return new StatusVariable(name, StatusVariable.CM_GAUGE, freeDisk);
 		} catch (InterruptedException e) {
 			return null;
 		}
 	}
 
 	public boolean notifiesOnChange(String name) throws IllegalArgumentException {
 		return false;
 	}
 
 	public boolean resetStatusVariable(String name) throws IllegalArgumentException {
 		return false;
 	}
 	
 	private static class FreeSpaceThread extends Thread {
 		/**
 		 * For logging
 		 */
 		private Log logger = LogFactory.getLog(this.getClass());		
 		
 		public long freeSpace = 0;
 		
 		@Override
 		public void run() {
 			try {
 				long freeDisk = FileSystemUtils.freeSpaceKb(new File(System.getProperty("paxle.data")).getCanonicalPath());
 				freeDisk /= 1024;
 				this.freeSpace = freeDisk;
 			} catch (IOException e) {
 				this.logger.error(String.format(
 						"Unexpected '%s' while trying to query free disk-space.",
 						e.getClass().getName()
 				),e);
 				this.freeSpace = -1;
 			}
 		}
 	}
 }
