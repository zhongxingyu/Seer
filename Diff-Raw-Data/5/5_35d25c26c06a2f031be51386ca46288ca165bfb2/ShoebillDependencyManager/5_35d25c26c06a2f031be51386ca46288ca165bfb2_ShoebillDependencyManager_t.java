 /**
  * Copyright (C) 2012 JoJLlmAn
  * Copyright (C) 2012 MK124
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.gtaun.shoebill.dependency;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.gtaun.shoebill.ResourceConfig;
 import net.gtaun.shoebill.ResourceConfig.RepositoryEntry;
 import net.gtaun.shoebill.ShoebillArtifactLocator;
 import net.gtaun.shoebill.ShoebillConfig;
 
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.artifact.Artifact;
 import org.sonatype.aether.collection.CollectRequest;
 import org.sonatype.aether.collection.DependencyCollectionException;
 import org.sonatype.aether.graph.Dependency;
 import org.sonatype.aether.graph.DependencyNode;
 import org.sonatype.aether.repository.RemoteRepository;
 import org.sonatype.aether.resolution.DependencyRequest;
 import org.sonatype.aether.resolution.DependencyResolutionException;
 import org.sonatype.aether.util.DefaultRepositorySystemSession;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 import org.sonatype.aether.util.graph.PreorderNodeListGenerator;
 
 /**
  * 
  * 
  * @author JoJLlmAn, MK124
  */
 public class ShoebillDependencyManager
 {
 	private static final String SHOEBILL_CONFIG_PATH = "./shoebill/shoebill.yml";
 	
 	private static final String PROPERTY_JAR_FILES = "jarFiles";
 	private static final String SCOPE_RUNTIME = "runtime";
 
 	private static final FilenameFilter JAR_FILENAME_FILTER = new FilenameFilter()
 	{
 		@Override
 		public boolean accept(File dir, String name)
 		{
 			return name.endsWith(".jar");
 		}
 	};
 	
 	
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args) throws Throwable
 	{
 		Map<String, Object> properties = resolveDependencies();
 		List<File> files = List.class.cast(properties.get(PROPERTY_JAR_FILES));
 		for (File file : files) System.out.println(file);
 	}
 	
 	public static Map<String, Object> resolveDependencies() throws Throwable
 	{
 		ShoebillConfig shoebillConfig = new ShoebillConfig(new FileInputStream(SHOEBILL_CONFIG_PATH));
 		ResourceConfig resourceConfig = new ResourceConfig(new FileInputStream(new File(shoebillConfig.getShoebillDir(), "resources.yml")));
 		ShoebillArtifactLocator artifactLocator = new ShoebillArtifactLocator(shoebillConfig, resourceConfig);
 		
 		final File repoDir = shoebillConfig.getRepositoryDir();
 		final File libDir = shoebillConfig.getLibrariesDir();
 		final File pluginsDir = shoebillConfig.getPluginsDir();
 		final File gamemodesDir = shoebillConfig.getGamemodesDir();
 		
 		File[] libJarFiles = libDir.listFiles(JAR_FILENAME_FILTER);
 		File[] pluginsJarFiles = pluginsDir.listFiles(JAR_FILENAME_FILTER);
 		File[] gamemodesJarFiles = gamemodesDir.listFiles(JAR_FILENAME_FILTER);
 		
 		List<File> files = new ArrayList<>();
 		
 		if(libJarFiles != null) files.addAll(Arrays.asList(libJarFiles));
 		if(pluginsJarFiles != null) files.addAll(Arrays.asList(pluginsJarFiles));
 		if(gamemodesJarFiles != null) files.addAll(Arrays.asList(gamemodesJarFiles));
 		
 		if (shoebillConfig.isResolveDependencies())
 		{
 			RepositorySystem repoSystem = AetherUtil.newRepositorySystem();
 			DefaultRepositorySystemSession session = AetherUtil.newRepositorySystemSession(repoSystem, repoDir);
 			CollectRequest collectRequest = new CollectRequest();
 			
 			session.setRepositoryListener(new ShoebillRepositoryListener());
 			session.setUpdatePolicy(resourceConfig.getCacheUpdatePolicy());
 
 			for (RepositoryEntry repo : resourceConfig.getRepositories())
 			{
 				collectRequest.addRepository(new RemoteRepository(repo.getId(), repo.getType(), repo.getUrl()));
 			}
 			
 			// Runtime
 			String runtimeCoord = resourceConfig.getRuntime();
 			if (runtimeCoord.contains(":"))
 			{
 				Artifact runtimeArtifact = new DefaultArtifact(resourceConfig.getRuntime());
 				collectRequest.addDependency(new Dependency(runtimeArtifact, SCOPE_RUNTIME));
 			}
 			else
 			{
 				System.out.println("Skipped artifact " + runtimeCoord + " (Runtime)");
 			}
 			
 			// Plugins
 			for (String coord : resourceConfig.getPlugins())
 			{
 				if (coord.contains(":") == false)
 				{
					System.out.println("Skipped artifact " + coord + " (Plugin)");
 					continue;
 				}
 				Artifact artifact = new DefaultArtifact(coord);
 				collectRequest.addDependency(new Dependency(artifact, SCOPE_RUNTIME));
 			}
 			
 			// Gamemode
 			String gamemodeCoord = resourceConfig.getGamemode();
 			if (gamemodeCoord.contains(":"))
 			{
 				Artifact gamemodeArtifact = new DefaultArtifact(resourceConfig.getGamemode());
 				collectRequest.addDependency(new Dependency(gamemodeArtifact, SCOPE_RUNTIME));
 			}
 			else
 			{
				System.out.println("Skipped artifact " + gamemodeCoord + " (Gamemode)");
 			}
 			
 			DependencyNode node = null;
 			try
 			{
 				node = repoSystem.collectDependencies(session, collectRequest).getRoot();
 			}
 			catch (DependencyCollectionException e)
 			{
 				e.printStackTrace();
 			}
 			DependencyRequest dependencyRequest = new DependencyRequest(node, null);
 			dependencyRequest.setFilter(new ShoebillDependencyFilter(artifactLocator));
 			
 			try
 			{
 				repoSystem.resolveDependencies(session, dependencyRequest);
 			}
 			catch (DependencyResolutionException e)
 			{
 				e.printStackTrace();
 			}
 			
 			PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
 			node.accept(nlg);
 			
 			files.addAll(nlg.getFiles());
 		}
 		else
 		{
 			System.out.println("Skip resolve dependencies." );
 		}
 
 		Map<String, Object> properties = new HashMap<>();
 		properties.put(PROPERTY_JAR_FILES, files);
 		return properties;
 	}
 }
