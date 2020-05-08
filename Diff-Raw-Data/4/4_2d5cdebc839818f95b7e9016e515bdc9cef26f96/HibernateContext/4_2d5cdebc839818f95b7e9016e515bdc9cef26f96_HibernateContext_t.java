 /*
  * Copyright (c) 2010
  *
  * This file is part of HibernateJConsole.
  *
  *     HibernateJConsole is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     HibernateJConsole is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with HibernateJConsole.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.sf.hibernate.jconsole.hibernate;
 
 import net.sf.hibernate.jconsole.AbstractStatisticsContext;
 import net.sf.hibernate.jconsole.stats.Names;
 import net.sf.hibernate.jconsole.util.ClasspathUtil;
 import net.sf.hibernate.jconsole.util.HibernateJmxBinding;
 import net.sf.hibernate.jconsole.util.JMXUtil;
 
 import javax.management.Attribute;
 import javax.management.MBeanServerConnection;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Is a local context that is used to cache and exchange statistical information.
  *
  * @author Juergen_Kellerer, 2009-11-19
  * @version 1.0
  */
 public class HibernateContext extends AbstractStatisticsContext {
 
 	private static final long serialVersionUID = -7295609157873741739L;
 
 	static final String HIBERNATE_VALIDATION_CLASS = System.getProperty(
 			"hibernate.class", "org.hibernate.stat.CollectionStatistics");
 
 	static final String HIBERNATE_CLASSPATH = System.getProperty("hibernate.classpath");
 	static final String[] HIBERNATE_DEFAULT_SEARCHPATH = System.getProperty(
 			"hibernate.searchpath", ".;lib;lib/3rdParty").split(";+");
 
 	static final FilenameFilter HIBERNATE_FILTER = new FilenameFilter() {
 		public boolean accept(File dir, String name) {
 			name = name.toLowerCase();
 			return (!name.contains("hibernate-jconsole") && name.contains("hibernate") && name.endsWith(".jar")) ||
 					new File(dir, name).isDirectory();
 		}
 	};
 
 	private boolean classPathInitialized, instanceNotFoundReported;
 	private Map<Names, Object> attributes = new HashMap<Names, Object>();
 
 	boolean findAndAddHibernateJars(String[] searchPath) {
 		for (String s : searchPath) {
 			File file = new File(s);
 			if (ClasspathUtil.addJars(file, false, HIBERNATE_FILTER))
 				return true;
 		}
 		return false;
 	}
 
 	List<File> collectReachableJarsFromRemoteJVM() {
 		try {
 			List<File> jars = new ArrayList<File>();
 			Properties properties = JMXUtil.getSystemProperties(getConnection());
 
 			String cwd = properties.getProperty("user.dir");
 			String[] cp = properties.getProperty("java.class.path").
 					split(properties.getProperty("path.separator", ";"));
 
 			for (String path : cp) {
				path = path.trim();
				if (path.isEmpty())
					continue;

 				File f = new File(path);
 				if (!f.isFile())
 					f = new File(cwd + File.separator + path);
 				if (f.isFile() && !jars.contains(f))
 					jars.add(f);
 			}
 
 			return jars;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	void initializeClasspath() {
 		if (classPathInitialized)
 			return;
 
 		boolean success = false;
 
 		if (HIBERNATE_CLASSPATH != null)
 			success = findAndAddHibernateJars(HIBERNATE_CLASSPATH.split(";+"));
 
 		if (!success) {
 			List<File> list = collectReachableJarsFromRemoteJVM();
 			success = ClasspathUtil.addJars(list.toArray(new File[list.size()]), false, HIBERNATE_FILTER);
 		}
 
 		if (!success)
 			success = findAndAddHibernateJars(HIBERNATE_DEFAULT_SEARCHPATH);
 
 		classPathInitialized = success;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setConnection(MBeanServerConnection connection) {
 		super.setConnection(connection);
 		initializeClasspath();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected boolean isEnabled() {
 		MBeanServerConnection c = getConnection();
 		if (c == null)
 			return true;
 
 		try {
 			// Lookup the hibernate classes inside the system class loader, making them
 			// available to the plugin class loader is not enough!
 			Class.forName(HIBERNATE_VALIDATION_CLASS, true, ClassLoader.getSystemClassLoader());
 		} catch (ClassNotFoundException ignore) {
 			if (!instanceNotFoundReported) {
 				System.err.println("Didn't find class '" + HIBERNATE_VALIDATION_CLASS +
 						"', the hibernate environment is not correctly set.");
 				instanceNotFoundReported = true;
 			}
 			return false;
 		}
 
 		try {
 			return c.isRegistered(HibernateJmxBinding.HIBERNATE_STATISTICS);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Object getEntityStatisticsFor(String name) throws Exception {
 		return Methods.getEntityStatistics.invoke(getConnection(), name);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Object getCollectionStatisticsFor(String name) throws Exception {
 		return Methods.getCollectionStatistics.invoke(getConnection(), name);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Object getQueryStatisticsFor(String name) throws Exception {
 		return Methods.getQueryStatistics.invoke(getConnection(), name);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Object getCacheStatisticsFor(String cacheRegion) throws Exception {
 		return Methods.getSecondLevelCacheStatistics.invoke(getConnection(), cacheRegion);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Map<Names, Object> getAttributes(List<Names> attributeNames) throws Exception {
 		String[] names = new String[attributeNames.size()];
 		Iterator<Names> iN = attributeNames.iterator();
 		for (int i = 0; i < names.length; i++)
 			names[i] = iN.next().name();
 
 		List<Attribute> attributeList = getConnection().
 				getAttributes(HibernateJmxBinding.HIBERNATE_STATISTICS, names).asList();
 		for (Attribute attribute : attributeList)
 			attributes.put(Names.valueOf(attribute.getName()), attribute.getValue());
 
 		return attributes;
 	}
 }
