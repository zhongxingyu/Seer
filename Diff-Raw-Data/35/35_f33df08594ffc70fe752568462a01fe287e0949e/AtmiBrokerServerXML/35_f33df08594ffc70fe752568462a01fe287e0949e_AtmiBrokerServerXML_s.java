 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat, Inc., and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.jboss.blacktie.jatmibroker.core.conf;
 
 import java.util.Properties;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 public class AtmiBrokerServerXML {
 	private static final Logger log = LogManager
 			.getLogger(AtmiBrokerServerXML.class);
 	private Properties prop;
 	String serverName;
 
 	public AtmiBrokerServerXML(String serverName) {
 		if (serverName == null) {
 			serverName = "default";
 		}
 		this.serverName = serverName;
 		prop = new Properties();
 	}
 
 	public AtmiBrokerServerXML(String serverName, Properties prop) {
 		if (serverName == null) {
 			serverName = "default";
 		}
 		this.serverName = serverName;
 		this.prop = prop;
 	}
 
 	public Properties getProperties() throws Exception {
 		return getProperties(null);
 	}
 
 	public Properties getProperties(String configDir)
 			throws ConfigurationException {
 		String envXML;
 		if (configDir == null) {
 			configDir = System.getenv("BLACKTIE_CONFIGURATION_DIR");
 		}
 
 		if (configDir != null && !configDir.equals("")) {
 			envXML = configDir + "/" + "Environment.xml";
 		} else {
 			envXML = "Environment.xml";
 		}
 
 		XMLEnvHandler env = new XMLEnvHandler(configDir, prop);
 		XMLParser xmlenv = new XMLParser(env, "Environment.xsd");
		xmlenv.parse(envXML);
 
 		return prop;
 	}
 }
