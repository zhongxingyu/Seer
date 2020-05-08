 /*
  * ###
  * Phresco Commons
  *
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.configuration;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.photon.phresco.exception.ConfigurationException;
 
 public class ConfigReader {
 
 	//envname, env dom element
	private static final Map<String, Element> ENV_MAP = new LinkedHashMap<String, Element>();
 	private static String defaultEnvironment = null;
 	private Document document = null;
 	private File configFile = null;
 
 	/**
 	 * ConfigReader single instance created by configuration xml
 	 * @param configXML File type
 	 * @return
 	 * @throws ConfigurationException 
 	 * @throws SAXException
 	 * @throws ParserConfigurationException
 	 * @throws Exception
 	 */
 	public ConfigReader(File configXML) throws ConfigurationException {
 		if (configXML.exists()) {
 			this.configFile = configXML;
 			try {
 				initXML(new FileInputStream(configXML));
 			} catch (FileNotFoundException e) {
 				throw new ConfigurationException(e);
 			} 
 		} else {
 			ENV_MAP.clear();
 		}
 	}
 
 	/**
 	 * ConfigReader single instance created by configuration xml input stream
 	 * @param xmlStream
 	 * @return
 	 * @throws Exception
 	 */
 	public ConfigReader(InputStream xmlStream) throws Exception {
 		initXML(xmlStream);
 	}
 
 	/**
 	 * Returns the defaut environment name
 	 * @return
 	 */
 	public String getDefaultEnvName() {
 		return defaultEnvironment;
 	}
 
 	/**
 	 * Returns the Configurations of given environments
 	 * @param envName - Environment name
 	 * @return
 	 */
 	public List<Configuration> getConfigByEnv(String envName) {
 		List<Configuration> configurations = new ArrayList<Configuration>();
 		Element environment = getEnvironment(envName);
 		if (environment != null) {
 			NodeList configNodes = environment.getChildNodes();
 			for (int i = 0; i < configNodes.getLength(); i++) {
 				if (configNodes.item(i).getNodeType() !=  Element.TEXT_NODE) {
 					Element configNode = (Element) configNodes.item(i);
 					String configType = configNode.getNodeName();
 					String configName = configNode.getAttribute("name");
 					String configDesc = configNode.getAttribute("desc");
 					String configAppliesTo = configNode.getAttribute("appliesTo");
 					Properties properties = getProperties(configNode);
 					Configuration config = new Configuration(configName, configDesc, envName, configType, properties, configAppliesTo);
 					configurations.add(config);
 				}
 			}
 		}
 		return configurations;
 	}
 
 	/**
 	 * Returns the Configurations of given environments by configuration type
 	 * @param envName
 	 * @param configType
 	 * @return
 	 */
 	public List<Configuration> getConfigurations(String envName, String configType) {
 		List<Configuration> configurations = getConfigByEnv(envName);
 		List<Configuration> filterConfigs = new ArrayList<Configuration>(configurations.size());
 		for (Configuration configuration : configurations) {
 			if (configuration.getType().equalsIgnoreCase(configType)) {
 				filterConfigs.add(configuration);
 			}
 		}
 		return filterConfigs;
 	}
 
 	/**
 	 * loads the configuration xml as input stream
 	 * @param xmlStream
 	 * @throws ConfigurationException 
 	 * @throws IOException
 	 * @throws SAXException
 	 * @throws ParserConfigurationException
 	 * @throws Exception
 	 */
 	protected void initXML(InputStream xmlStream) throws ConfigurationException {
 		try {
 			if (xmlStream == null) {
 				xmlStream = this.getClass().getClassLoader().getResourceAsStream("configuration.xml");
 			}
 			parseXML(xmlStream);
 		} finally {
 			try {
 				xmlStream.close();
 			} catch (IOException e) {
 				throw new ConfigurationException(e);
 			}
 		}
 	}
 
 	/**
 	 * Creating Dom object to parse the configuration xml
 	 * @param xmlStream
 	 * @throws ConfigurationException 
 	 * @throws ParserConfigurationException
 	 * @throws IOException
 	 * @throws SAXException
 	 * @throws Exception
 	 */
 	private void parseXML(InputStream xmlStream) throws ConfigurationException  {
 		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
 		domFactory.setNamespaceAware(false);
 		DocumentBuilder builder;
 		try {
 			builder = domFactory.newDocumentBuilder();
 			document = builder.parse(xmlStream);
 		} catch (ParserConfigurationException e) {
 			throw new ConfigurationException(e);
 		} catch (SAXException e) {
 			throw new ConfigurationException(e);
 		} catch (IOException e) {
 			throw new ConfigurationException(e);
 		}
 		parseDocument(document);
 	}
 
 	/**
 	 * parse the configuration xml
 	 * @param document
 	 */
 	private void parseDocument(Document document) {
 		//get a nodelist of environments
 		NodeList environmentList = document.getElementsByTagName("environment");
 		ENV_MAP.clear();
 
 		for(int i = 0 ; i < environmentList.getLength(); i++) {
 
 			//get the environment element
 			Element environment = (Element) environmentList.item(i);
 			String envName = environment.getAttribute("name");
 
 			boolean defaultEnv = Boolean.parseBoolean(environment.getAttribute("default"));
 			if (defaultEnv) {
 				defaultEnvironment = envName;
 			}
 			//add environment element to map
 			ENV_MAP.put(envName, environment);
 		}
 	}
 
 	protected Document getDocument() {
 		return document;
 	}
 
 	/**
 	 * return the environments
 	 * @return
 	 */
 	public Map<String, Element> getEnviroments() {
 		return ENV_MAP;
 	}
 
 	/**
 	 * return the environment element for the given Environment name
 	 * @param envName
 	 * @return
 	 */
 	protected Element getEnvironment(String envName) {
 		return ENV_MAP.get(envName);
 	}
 
 	/**
 	 * return the property of the given configuration
 	 * @param configNode
 	 * @return
 	 */
 	private Properties getProperties(Element configNode) {
 		Properties props = new Properties();
 		NodeList propNodes = configNode.getChildNodes();
 		for(int i = 0 ; i < propNodes.getLength(); i++) {
 			if (propNodes.item(i).getNodeType() !=  Element.TEXT_NODE) {
 				//get the environment element
 				Element propNode = (Element) propNodes.item(i);
 				String propName = propNode.getNodeName();
 				String propValue = propNode.getTextContent();
 				props.put(propName, propValue);
 			}
 		}
 		return props;
 	}
 
 	public File getConfigFile() {
 		return configFile;
 	}
 
 	public String getConfigAsJSON(String envName, String configType) {
 		if (envName == null) {
 			envName = getDefaultEnvName();
 		}
 		List<Configuration> configurations = getConfigByEnv(envName);
 		String json = "";
 		for (Configuration configuration : configurations) {
 			if (configuration.getType().equalsIgnoreCase(configType)) {
 				com.google.gson.Gson gson = new com.google.gson.Gson();
 				Properties properties = configuration.getProperties();
 				json = gson.toJson(properties, Properties.class);
 			}
 		}
 		return json;
 	}
 	
 	public List<Environment> getEnvironments(List<String> environmentNames) {
 		List<Element> elements = new ArrayList<Element>();
 		for (String envName : environmentNames) {
 			elements.add(ENV_MAP.get(envName));
 		}
 		return getEnvironmentsByElements(elements);
 	}
 	
 	private List<Environment> getEnvironmentsByElements(List<Element> elements) {
 		List<Environment> envs = new ArrayList<Environment>(elements.size());
 		for (Element envElement : elements) {
 			String envName = envElement.getAttribute("name");
 			String envDesc = envElement.getAttribute("desc");
 			String defaultEnv = envElement.getAttribute("default");
 			List<Configuration> configurations = getConfigByEnv(envName);
 			Environment environment = new Environment(envName, envDesc, Boolean.parseBoolean(defaultEnv));
 			environment.setConfigurations(configurations);
 			environment.setDelete(canDelete(envElement));
 			envs.add(environment);
 		}
 		return envs;
 	}
 	
 	public boolean canDelete(Element envElement) {
 		if (!envElement.hasChildNodes() || (envElement.getChildNodes().getLength() == 1 &&
 				envElement.getChildNodes().item(0).getNodeType() == Element.TEXT_NODE)) {
 			return true;
 		}
 		return false;
 	}
 	
 	public List<Environment> getAllEnvironments() {
 		Collection<Element> enviroments = ENV_MAP.values();
 		List<Environment> envs = new ArrayList<Environment>(enviroments.size());
 		for (Element envElement : enviroments) {
 			String envName = envElement.getAttribute("name");
 			String envDesc = envElement.getAttribute("desc");
 			String defaultEnv = envElement.getAttribute("default");
 			Environment environment = new Environment(envName, envDesc, Boolean.parseBoolean(defaultEnv));
 			environment.setDelete(canDelete(envElement));
 			environment.setConfigurations(getConfigByEnv(envName));
 			envs.add(environment);
 		}
 		return envs;
 	}
 }
