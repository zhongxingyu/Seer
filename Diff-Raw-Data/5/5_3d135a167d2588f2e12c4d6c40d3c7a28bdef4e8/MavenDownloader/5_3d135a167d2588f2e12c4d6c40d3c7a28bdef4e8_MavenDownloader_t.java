 /*
  * Jabox Open Source Version
  * Copyright (C) 2009-2010 Dimitris Kapanidis                                                                                                                          
  * 
  * This file is part of Jabox
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.gnu.org/licenses/.
  */
 package org.jabox.maven.helper;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
 import org.apache.maven.artifact.repository.DefaultArtifactRepositoryFactory;
 import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.codehaus.classworlds.ClassWorld;
 import org.codehaus.plexus.PlexusContainerException;
 import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
 import org.codehaus.plexus.embed.Embedder;
 import org.jabox.environment.Environment;
 
 public class MavenDownloader {
 
 	private static final String MAVEN_REPO = "http://repo1.maven.org/maven2/";
 
 	private static ArtifactRepositoryPolicy ARTIFACT_REPOSITORY_POLICY = new ArtifactRepositoryPolicy(
 			true, ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER,
 			ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);
 
 	private static ArtifactRepository ARTIFACT_REPOSITORY = new DefaultArtifactRepositoryFactory()
 			.createArtifactRepository("remote", MAVEN_REPO,
 					new DefaultRepositoryLayout(), ARTIFACT_REPOSITORY_POLICY,
 					ARTIFACT_REPOSITORY_POLICY);;
 
 	private static ArtifactRepository LOCAL_ARTIFACT_REPOSITORY = new DefaultArtifactRepositoryFactory()
 			.createArtifactRepository("local", "file://"
					+ Environment.getCustomMavenHomeDir() + "/repository/",
 					new DefaultRepositoryLayout(), ARTIFACT_REPOSITORY_POLICY,
					ARTIFACT_REPOSITORY_POLICY);
 
 	/**
 	 * @param groupId
 	 * @param artifactId
 	 * @param version
 	 * @param type
 	 */
 	public static File downloadArtifact(final String groupId,
 			final String artifactId, final String version, final String type) {
 		System.out.println("Downloading: " + groupId + ":" + artifactId + ":"
 				+ version + ":" + type);
 		try {
 			return retrieveArtifact(groupId, artifactId, version, type);
 		} catch (ArtifactResolutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ArtifactNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public static File getArtifactFile(final String groupId,
 			final String artifactId, final String version, final String type) {
 		String m2Home = Environment.getCustomMavenHomeDir().getAbsolutePath();
 		assert m2Home != null;
 		StringBuffer sb = new StringBuffer(m2Home);
 		sb.append(File.separator);
 		sb.append(".m2");
 		sb.append(File.separator);
 		sb.append("repository");
 		sb.append(File.separator);
 		sb.append(groupId.replace('.', File.separatorChar));
 		sb.append(File.separator);
 		sb.append(artifactId);
 		sb.append(File.separator);
 		sb.append(version);
 		sb.append(File.separator);
 		sb.append(artifactId);
 		sb.append("-");
 		sb.append(version);
 		sb.append(".");
 		sb.append(type);
 		File file = new File(sb.toString());
 		assert file.exists();
 		return file;
 	}
 
 	private static File retrieveArtifact(final String groupId,
 			final String artifactId, final String version,
 			final String packaging) throws ArtifactResolutionException,
 			ArtifactNotFoundException {
 		try {
 			Embedder embedder = new Embedder();
 			ClassWorld classWorld = new ClassWorld();
 
 			try {
 				embedder.start(classWorld);
 			} catch (PlexusContainerException e) {
 				throw new RuntimeException(
 						"Unable to start the embedded plexus container", e);
 			}
 
 			Artifact artifact = ((ArtifactFactory) embedder
 					.lookup(ArtifactFactory.ROLE)).createBuildArtifact(groupId,
 					artifactId, version, packaging);
 
 			ArtifactResolver artifactResolver = (ArtifactResolver) embedder
 					.lookup(ArtifactResolver.ROLE);
 			List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();
 			remoteRepositories.add(ARTIFACT_REPOSITORY);
 			ARTIFACT_REPOSITORY.getBasedir();
 			// XXX local Repo is wrong.
 			artifactResolver.resolve(artifact, remoteRepositories,
 					LOCAL_ARTIFACT_REPOSITORY);
 			return artifact.getFile();
 		} catch (ComponentLookupException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
