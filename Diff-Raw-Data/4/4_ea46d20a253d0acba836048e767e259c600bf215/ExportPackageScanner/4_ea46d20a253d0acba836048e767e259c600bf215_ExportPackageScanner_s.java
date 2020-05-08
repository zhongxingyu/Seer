 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package eu.gentech.osgi.packagescanner;
 
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.felix.utils.version.VersionTable;
 import org.osgi.framework.Version;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.gentech.osgi.ExportPackage;
 import eu.gentech.osgi.packagescanner.filter.ExportPackageFilter;
 import eu.gentech.osgi.packagescanner.strategy.ExportPackageScanningStrategy;
 import eu.gentech.osgi.packagescanner.utils.PackageNameMatcher;
 import eu.gentech.osgi.packagescanner.utils.PackageNameValidator;
 
 /**
  * 
  * @author genious87
  */
 public class ExportPackageScanner
 {
 
 	private static final Logger logger = LoggerFactory.getLogger(ExportPackageScanner.class);
 
 	private final Set<ExportPackageScanningStrategy> strategies = new LinkedHashSet<ExportPackageScanningStrategy>();
 	private final Set<ExportPackageFilter> filters = new LinkedHashSet<ExportPackageFilter>();
 	private final Map<String, String> versionMappings = new HashMap<String, String>();
 
 	public static final ExportPackageScanner start()
 	{
 		return new ExportPackageScanner();
 	}
 
 	public Set<ExportPackage> scan()
 	{
 		final Result result = new Result();
 		scan(result);
 		return result.getPackages();
 	}
 
 	protected void scan(final ExportPackageScanningResult result)
 	{
 		for (final ExportPackageScanningStrategy strategy : strategies)
 		{
 			strategy.scan(result);
 		}
 	}
 
 	public ExportPackageScanner with(final ExportPackageScanningStrategy strategy)
 	{
 		strategies.add(strategy);
 		return this;
 	}
 
 	public ExportPackageScanner with(final ExportPackageScanningStrategy... strategies)
 	{
 		if (ArrayUtils.isNotEmpty(strategies))
 		{
 			this.strategies.addAll(Arrays.asList(strategies));
 		}
 		return this;
 	}
 
 	public ExportPackageScanner with(final Collection<ExportPackageScanningStrategy> strategies)
 	{
 		if (CollectionUtils.isNotEmpty(strategies))
 		{
 			this.strategies.addAll(strategies);
 		}
 		return this;
 	}
 
 	public ExportPackageScanner filter(final ExportPackageFilter filter)
 	{
 		filters.add(filter);
 		return this;
 	}
 
 	public ExportPackageScanner filter(final ExportPackageFilter... filters)
 	{
 		if (ArrayUtils.isNotEmpty(filters))
 		{
 			this.filters.addAll(Arrays.asList(filters));
 		}
 		return this;
 	}
 
 	public ExportPackageScanner filter(final Collection<ExportPackageFilter> filters)
 	{
 		if (CollectionUtils.isNotEmpty(filters))
 		{
 			this.filters.addAll(filters);
 		}
 		return this;
 	}
 
 	public ExportPackageScanner withMapping(final String name, final String version)
 	{
 		versionMappings.put(name, version);
 		return this;
 	}
 
 	public ExportPackageScanner withMappings(final Map<String, String> mappings)
 	{
 		if (mappings != null)
 		{
 			versionMappings.putAll(mappings);
 		}
 		return this;
 	}
 
 	private class Result implements ExportPackageScanningResult
 	{
 
 		private final Set<ExportPackage> packages = new TreeSet<ExportPackage>();
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see eu.gentech.osgi.packagescanner.ExportPackageScanningResult#add(java.lang.String, java.lang.String,
 		 * java.net.URL)
 		 */
 		@Override
 		public void add(final String name, final String version, final URL location)
 		{
 
 			Version mappedVersion = mappedVersion(name);
 			if (mappedVersion == null)
 			{
 				mappedVersion = VersionTable.getVersion(version);
 			}
 
 			doAdd(name, mappedVersion, location);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see eu.gentech.osgi.packagescanner.ExportPackageScanningResult#add(java.lang.String,
 		 * org.osgi.framework.Version, java.net.URL)
 		 */
 		@Override
 		public void add(final String name, final Version version, final URL location)
 		{
 
 			Version mappedVersion = mappedVersion(name);
 			if (mappedVersion == null)
 			{
 				mappedVersion = version;
 			}
 
 			doAdd(name, mappedVersion, location);
 
 		}
 
 		private Version mappedVersion(final String name)
 		{
 			if (MapUtils.isNotEmpty(versionMappings))
 			{
 				for (final Entry<String, String> mapping : versionMappings.entrySet())
 				{
 					if (PackageNameMatcher.match(mapping.getKey(), name))
 					{
 						return VersionTable.getVersion(mapping.getValue());
 					}
 				}
 			}
 			return null;
 		}
 
 		private void doAdd(final String name, final Version version, final URL location)
 		{
 
 			if (!PackageNameValidator.isValid(name))
 			{
 				logger.warn("Invalid package name '{}', ignore it.", name);
 				return;
 			}
 
 			final ExportPackage exportPackage = new ExportPackage(name, version, location);
 
 			if (CollectionUtils.isNotEmpty(filters))
 			{
 				for (final ExportPackageFilter filter : filters)
 				{
 					if (!filter.accept(exportPackage))
 					{
 						if (logger.isDebugEnabled())
 						{
							logger.info("Package '{}' with version '{}' excluded from location '{}'", new Object[] {
									name, version, location });
 						}
 						return;
 					}
 				}
 			}
 
 			packages.add(exportPackage);
 
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see eu.gentech.osgi.packagescanner.ExportPackageScanningResult#getPackages()
 		 */
 		@Override
 		public Set<ExportPackage> getPackages()
 		{
 			return packages;
 		}
 
 	}
 
 }
