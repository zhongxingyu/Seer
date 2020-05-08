 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
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
  */
 package sorcer.resolver;
 
 import org.apache.commons.lang3.StringUtils;
 import sorcer.core.SorcerConstants;
 import sorcer.util.ArtifactCoordinates;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Helper class resolving ArtifactCoordinates to relative or absolute paths
  *
  * @author Rafał Krupiński
  */
 public class Resolver {
 	private static ArtifactResolver resolver = new ArtifactResolverFactory().createResolver();
 
 	/**
 	 * Resolve artifact coordinates to absolute path
 	 *
 	 * @param coords artifact coordinates string {@link ArtifactCoordinates#coords(String)}
 	 * @return absolute path of file denoted bu the artifact coordinates
 	 */
 	public static String resolveAbsolute(String coords) {
 		return resolveAbsolute(ArtifactCoordinates.coords(coords));
 	}
 
 	/**
 	 * Resolve artifact coordinates to absolute path
 	 *
 	 * @return absolute path of file denoted bu the artifact coordinates
 	 */
 	public static String resolveAbsolute(ArtifactCoordinates coords) {
 		return resolver.resolveAbsolute(coords);
 	}
 
 	public static String resolveRelative(ArtifactCoordinates coords){
 		return resolver.resolveRelative(coords).replace(File.separator, "/");
 	}
 
 	public static String resolveRelative(String coords){
 		return resolveRelative(ArtifactCoordinates.coords(coords)).replace(File.separator, "/");
 	}
 
 	public static String resolveAbsolute(URL baseUrl, ArtifactCoordinates coords) {
 		return resolveAbsoluteURL(baseUrl, coords).toExternalForm();
 	}
 
     public static String resolveAbsolute(URL baseUrl, String coords) {
         return resolveAbsoluteURL(baseUrl, ArtifactCoordinates.coords(coords)).toExternalForm();
     }
 
     public static URL resolveAbsoluteURL(URL baseUrl, ArtifactCoordinates coords) {
 		try {
 			return new URL(baseUrl, resolveRelative(coords).replace(File.separator, "/"));
 		} catch (MalformedURLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * This is helper method for use in *.config files. The resulting string is
 	 * passed to SorcerServiceDescriptor constructor as a codebase string.
 	 *
 	 * @param baseUrl URL root of artifacts
 	 * @param coords  array of artifact coordinates
 	 */
 	public static String resolveCodeBase(URL baseUrl, String... coords) {
 		String[] relatives = new String[coords.length];
 		for (int i = 0; i < coords.length; i++) {
            // For compatibility do not resolve against artifacts if coords is a url to a jar
            if (coords[i].contains("http"))
                relatives[i] = coords[i];
            else
			    relatives[i] = resolveAbsolute(baseUrl, ArtifactCoordinates.coords(coords[i]));
 		}
 		return StringUtils.join(relatives, SorcerConstants.CODEBASE_SEPARATOR);
 	}
 
 	/**
 	 * This is helper method for use in *.config files. The resulting string is
 	 * passed to SorcerServiceDescriptor constructor as a codebase string.
 	 *
 	 * @param baseUrl URL root of artifacts
 	 * @param coords  array of artifact coordinates
 	 */
 	public static String resolveCodeBase(URL baseUrl, ArtifactCoordinates... coords) {
 		String[] relatives = new String[coords.length];
 		for (int i = 0; i < coords.length; i++) {
 			relatives[i] = resolveAbsolute(baseUrl, coords[i]);
 		}
 		return StringUtils.join(relatives, SorcerConstants.CODEBASE_SEPARATOR);
 	}
 
 	/**
 	 * Resolve array of artifact coordinates to ${File.pathSeparator}-separated list of absolute paths
 	 */
 	public static String resolveClassPath(ArtifactCoordinates... artifactCoordinatesList) {
 		List<String> result = new ArrayList<String>(artifactCoordinatesList.length);
 		for (ArtifactCoordinates coords : artifactCoordinatesList) {
 			result.add(resolveAbsolute(coords));
 		}
 		return StringUtils.join(result, File.pathSeparator);
 	}
 
 	/**
 	 * Resolve array of artifact coordinates to ${File.pathSeparator}-separated list of absolute paths
 	 */
 	public static String resolveClassPath(String[] artifactCoordinatesList) {
 		List<String> result = new ArrayList<String>(artifactCoordinatesList.length);
 		for (String coords : artifactCoordinatesList) {
 			result.add(resolveAbsolute(coords));
 		}
 		return StringUtils.join(result, File.pathSeparator);
 	}
 
 	/**
 	 * resolve jar by simple name (with extension). Maven version will use the name as artifactId and guess groupId and version. Flattened version will search for that file in all its roots.
 	 * <p/>
 	 * This is intended as a helper for MANIFEST Main-Class entries
 	 */
 	public static File resolveSimpleName(String simpleName) {
 		int i = simpleName.lastIndexOf('.');
 		String packaging;
 		String artifactId;
 		if (i >= 0) {
 			artifactId = simpleName.substring(0, i);
 			packaging = simpleName.substring(i + 1);
 		} else {
 			artifactId = simpleName;
 			packaging = "jar";
 		}
 		return new File(resolver.getRootDir(), resolver.resolveSimpleName(artifactId, packaging));
 	}
 
 	public static String getRootDir() {
         return resolver.getRootDir();
     }
 
     public static String getRepoDir() {
         return resolver.getRepoDir();
     }
 
     public static boolean isMaven() {
         return (resolver instanceof RepositoryArtifactResolver);
     }
 
     public static ArtifactResolver getResolver() {
         return resolver;
     }
 
 }
