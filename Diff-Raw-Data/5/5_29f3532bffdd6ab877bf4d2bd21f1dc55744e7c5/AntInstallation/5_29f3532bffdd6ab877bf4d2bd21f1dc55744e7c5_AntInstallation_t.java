 /*
  * Copyright 2000-2009 JetBrains s.r.o.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.napile.idea.thermit.config.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.util.Collections;
 import java.util.Comparator;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.jdom.Element;
 import org.jetbrains.annotations.NonNls;
 import org.napile.idea.thermit.ThermitBundle;
 import org.napile.idea.thermit.ThermitClasses;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.util.InvalidDataException;
 import com.intellij.openapi.util.WriteExternalException;
 import com.intellij.util.config.AbstractProperty;
 import com.intellij.util.config.ExternalizablePropertyContainer;
 import com.intellij.util.config.Externalizer;
 import com.intellij.util.config.ListProperty;
 import com.intellij.util.config.StringProperty;
 import com.intellij.util.containers.Convertor;
 import com.intellij.util.lang.UrlClassLoader;
 
 public class AntInstallation
 {
 	private static final Logger LOG = Logger.getInstance("#com.intellij.thermit.impl.AntInstallation");
 	public static final StringProperty HOME_DIR = new StringProperty("homeDir", "");
 	public static final AbstractProperty<String> NAME = new StringProperty("name", "");
 	public static final ListProperty<AntClasspathEntry> CLASS_PATH = ListProperty.<AntClasspathEntry>create("classpath");
 	public static final Comparator<AntInstallation> NAME_COMPARATOR = new Comparator<AntInstallation>()
 	{
 		public int compare(AntInstallation antInstallation, AntInstallation antInstallation1)
 		{
 			return String.CASE_INSENSITIVE_ORDER.compare(antInstallation.getName(), antInstallation1.getName());
 		}
 	};
 
 	public static final Convertor<AntInstallation, AntReference> REFERENCE_TO_ANT = new Convertor<AntInstallation, AntReference>()
 	{
 		public AntReference convert(AntInstallation antInstallation)
 		{
 			return antInstallation.getReference();
 		}
 	};
 	public static final AbstractProperty<String> VERSION = new StringProperty("version", ThermitBundle.message("ant.unknown.version.string.presentation"));
 
 	private final ClassLoaderHolder myClassLoaderHolder;
 	@NonNls
	public static final String PATH_TO_ANT_JAR = "lib/thermit.nzip";
 	@NonNls
 	public static final String LIB_DIR = "lib";
 	@NonNls
	public static final String ANT_JAR_FILE = "thermit.nzip";
 
 	public AntReference getReference()
 	{
 		return new AntReference.BindedReference(this);
 	}
 
 	public static final Externalizer<AntInstallation> EXTERNALIZER = new Externalizer<AntInstallation>()
 	{
 		public AntInstallation readValue(Element dataElement) throws InvalidDataException
 		{
 			AntInstallation antInstallation = new AntInstallation();
 			antInstallation.readExternal(dataElement);
 			return antInstallation;
 		}
 
 		public void writeValue(Element dataElement, AntInstallation antInstallation) throws WriteExternalException
 		{
 			antInstallation.myProperties.writeExternal(dataElement);
 		}
 	};
 
 	private void readExternal(Element dataElement) throws InvalidDataException
 	{
 		myProperties.readExternal(dataElement);
 		File antJar = new File(HOME_DIR.get(myProperties), PATH_TO_ANT_JAR);
 		updateVersion(antJar);
 	}
 
 	void updateVersion(File antJar)
 	{
 		if(antJar.exists())
 		{
 			try
 			{
 				VERSION.set(getProperties(), loadVersion(antJar));
 			}
 			catch(Exception e)
 			{
 				LOG.error(e);
 			}
 		}
 	}
 
 	private final ExternalizablePropertyContainer myProperties;
 
 	AntInstallation()
 	{
 		this(new ExternalizablePropertyContainer());
 		registerProperties(myProperties);
 	}
 
 	private AntInstallation(final ExternalizablePropertyContainer properties)
 	{
 		myProperties = properties;
 		myClassLoaderHolder = new AntInstallationClassLoaderHolder(myProperties);
 	}
 
 	public String getName()
 	{
 		return NAME.get(myProperties);
 	}
 
 	public void setName(final String name)
 	{
 		NAME.set(myProperties, name);
 	}
 
 	public String getVersion()
 	{
 		return VERSION.get(myProperties);
 	}
 
 	public String getHomeDir()
 	{
 		return HOME_DIR.get(myProperties);
 	}
 
 	public AbstractProperty.AbstractPropertyContainer getProperties()
 	{
 		return myProperties;
 	}
 
 	public ClassLoader getClassLoader()
 	{
 		return myClassLoaderHolder.getClassloader();
 	}
 
 	public void updateClasspath()
 	{
 		myClassLoaderHolder.updateClasspath();
 	}
 
 	public static AntInstallation fromHome(String homePath) throws ConfigurationException
 	{
 		File antHome = new File(homePath);
 		String antPath = "'" + antHome.getAbsolutePath() + "'";
 		checkExists(antHome, ThermitBundle.message("path.to.ant.does.not.exist.error.message", antPath));
 		File lib = new File(antHome, LIB_DIR);
 		checkExists(lib, ThermitBundle.message("lib.directory.not.found.in.ant.path.error.message", antPath));
 		File antJar = new File(lib, ANT_JAR_FILE);
 		checkExists(antJar, ThermitBundle.message("ant.jar.not.found.in.directory.error.message", lib.getAbsolutePath()));
 		if(antJar.isDirectory())
 		{
 			throw new ConfigurationException(ThermitBundle.message("ant.jar.is.directory.error.message", antJar.getAbsolutePath()));
 		}
 		try
 		{
 			String version = loadVersion(antJar);
 			AntInstallation antInstallation = new AntInstallation();
 			HOME_DIR.set(antInstallation.getProperties(), antHome.getAbsolutePath());
 
 			NAME.set(antInstallation.getProperties(), ThermitBundle.message("apache.ant.with.version.string.presentation", version));
 			VERSION.set(antInstallation.getProperties(), version);
 			antInstallation.addClasspathEntry(new AllNZipsUnderDirEntry(lib));
 			return antInstallation;
 		}
 		catch(MalformedURLException e)
 		{
 			LOG.error(e);
 			return null;
 		}
 	}
 
 	private static String loadVersion(File antJar) throws MalformedURLException, ConfigurationException
 	{
 		String version = null;
 		InputStream stream = null;
 		try
 		{
 			stream = new UrlClassLoader(Collections.singletonList(antJar.toURL()), null, false, false, true).getResourceAsStream(ThermitClasses.MODULE_FILE_NAME);
 
 			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
 			f.setValidating(false);
 
 			DocumentBuilder builder = f.newDocumentBuilder();
 
 			Document doc = builder.parse(stream);
 
 			NodeList nodeList = doc.getChildNodes();
 
 			Node node = nodeList.item(0);
 
 			for(Node it = node.getFirstChild(); it != null; it = it.getNextSibling())
 				if(it.getNodeName().equals("version"))
 					version = it.getChildNodes().item(0).getNodeValue();
 		}
 		catch(SAXException e)
 		{
 			throw new RuntimeException(e);
 		}
 		catch(ParserConfigurationException e)
 		{
 			throw new RuntimeException(e);
 		}
 		catch(IOException e)
 		{
 			throw new ConfigurationException(ThermitBundle.message("cant.read.from.ant.jar.error.message", antJar.getAbsolutePath()));
 		}
 		finally
 		{
 			if(stream != null)
 			{
 				try
 				{
 					stream.close();
 				}
 				catch(IOException e)
 				{
 					LOG.error(e);
 				}
 			}
 		}
 		return version == null ? "unknown" : version;
 	}
 
 	private void addClasspathEntry(AntClasspathEntry entry)
 	{
 		CLASS_PATH.getModifiableList(getProperties()).add(entry);
 	}
 
 	private static void checkExists(File file, String message) throws ConfigurationException
 	{
 		if(!file.exists())
 			throw new ConfigurationException(message);
 	}
 
 	public static class ConfigurationException extends Exception
 	{
 		public ConfigurationException(String message)
 		{
 			super(message);
 		}
 	}
 
 	private static void registerProperties(ExternalizablePropertyContainer container)
 	{
 		container.registerProperty((StringProperty) NAME);
 		container.registerProperty(HOME_DIR);
 		container.registerProperty(CLASS_PATH, "classpathItem", SinglePathEntry.EXTERNALIZER);
 		container.registerProperty((StringProperty) VERSION);
 	}
 }
