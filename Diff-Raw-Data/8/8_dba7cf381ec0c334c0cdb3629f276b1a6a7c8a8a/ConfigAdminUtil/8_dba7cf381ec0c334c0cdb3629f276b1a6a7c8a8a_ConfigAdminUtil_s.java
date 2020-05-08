 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
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
  *******************************************************************************/
 package com.buglabs.util;
 
 import java.io.IOException;
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 
 /**
  * Static helper methods to work with the ConfigurationAdmin
  * 
  * @author kgilmer
  *
  */
 public class ConfigAdminUtil {
 
 	/**
 	 * Get a config dictionary safely based on a ConfigurationAdmin service and a PID.  If configuration or properties do not exist it will be created.
 	 * @param ca
 	 * @return A dictionary
 	 * @throws IOException 
 	 */
 	public static Dictionary getPropertiesSafely(ConfigurationAdmin ca, String pid) throws IOException {
 		if (ca == null || pid == null) {
 			throw new RuntimeException("Client called getPropertiesSafely() with null parameter.");
 		}
 		
 		return getPropertiesSafely(ca.getConfiguration(pid));		
 	}
 	
 	public static Dictionary getPropertiesSafely(Configuration config) throws IOException {
 		if (config == null) {
 			throw new RuntimeException("Client called getPropertiesSafely() with null parameter.");
 		}		
 		
		//According to CM spec, this should never return null.
 		Dictionary properties = config.getProperties();
 		
 		if (properties == null) {
 			properties = new Hashtable();
 			
 			config.update(properties);
 		}
 	
 		return properties;
 	}
 	
 	/**
 	 * @param dict
 	 * @param key
 	 * @return true if dictionary contains key, false otherwise.
 	 */
 	public static boolean containsKey(Dictionary dict, String key) {
 		if (dict == null || key == null) {
 			return false;
 		}
 		
 		return dict.get(key) != null;
 	}
 }
