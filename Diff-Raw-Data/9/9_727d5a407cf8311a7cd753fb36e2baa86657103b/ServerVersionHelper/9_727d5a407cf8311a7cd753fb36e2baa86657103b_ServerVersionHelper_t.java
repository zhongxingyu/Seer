 /*******************************************************************************
 * Copyright (c) 2009 - 2012 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.runtime.internal.core;
 
 import org.eclipse.virgo.ide.runtime.core.IServerVersionHandler;
 
 /**
  * Utility that loads {@link IServerVersionHandler}s based on given version identifiers.
  * @author Christian Dupuis
  * @since 1.0.0
  */
 public class ServerVersionHelper {
 
 	/** The runtime identifier for dm Server version 1.0 */
 	public static final String SERVER_10 = "com.springsource.server.runtime.10";
 
 	/** The runtime identifier for dm Server version 2.0 */
 	public static final String SERVER_20 = "com.springsource.server.runtime.20";
 	
 	public static final String SERVER_VIRGO = "org.eclipse.virgo.server.runtime.virgo";
	
	public static final String SERVER_VIRGO_35 = "org.eclipse.virgo.server.runtime.virgo.35";
 
 	public static IServerVersionHandler getVersionHandler(String id) {
 		if (SERVER_10.equals(id)) {
 			return new Server10Handler();
 		}
 		if (SERVER_20.equals(id)) {
 			return new Server20Handler();
 		}
 		if (SERVER_VIRGO.equals(id)) {
 			return new ServerVirgoHandler();
 		}
		if (SERVER_VIRGO_35.equals(id)) {
			return new ServerVirgoHandler();
		}
 		else {
 			return null;
 		}
 	}
 
 }
