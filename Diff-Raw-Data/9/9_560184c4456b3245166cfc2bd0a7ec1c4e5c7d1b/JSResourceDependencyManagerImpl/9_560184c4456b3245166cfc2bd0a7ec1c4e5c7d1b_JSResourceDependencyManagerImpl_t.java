 package net.dynamic_tools.service;
 
 import net.dynamic_tools.exception.CircularDependencyException;
 import net.dynamic_tools.model.JSResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Peter
  * Date: 4/11/11
  * Time: 4:16 PM
  * To change this template use File | Settings | File Templates.
  */
 @Component
 public class JSResourceDependencyManagerImpl implements ResourceDependencyManager<JSResource> {
     private static final Logger logger = LoggerFactory.getLogger(JSResourceDependencyManagerImpl.class);
 
     private final List<File> paths = new ArrayList<File>();
     private final List<File> mockPaths = new ArrayList<File>();
     private final Map<String, JSResource> jsResourceMap = new HashMap<String, JSResource>();
     private final Map<String, JSResource> jsMockResourceMap = new HashMap<String, JSResource>();
 
     private FileFinder fileFinder;
     private JSDependencyReader jsDependencyReader;
 
     private static final String JS_EXTENSION = ".js";
 
     @Override
     public void addPaths(File... paths) {
         this.paths.addAll(Arrays.asList(paths));
     }
 
     @Override
     public void removePaths(File... paths) {
         this.paths.removeAll(Arrays.asList(paths));
     }
 
     @Override
     public void addMockPaths(File... paths) {
         this.mockPaths.addAll(Arrays.asList(paths));
     }
 
     @Override
     public void removeMockPaths(File... paths) {
         this.mockPaths.removeAll(Arrays.asList(paths));
     }
 
       public List<JSResource> getResourceByName(boolean useMocks, String... names) throws IOException, CircularDependencyException {
         Map<String, JSResource> jsResourceMap = new HashMap<String, JSResource>();
         List<JSResource> jsResources = new ArrayList<JSResource>();
         for (String name : names) {
             String fileName = name.replace('.', File.separatorChar) + JS_EXTENSION;
             File jsResourceFile = fileFinder.getFile(fileName, paths);
 
             JSResource jsResource = new JSResource(jsResourceFile, name);
             jsResourceMap.put(name, jsResource);
             jsResources.add(jsResource);
         }
         for (JSResource jsResource : jsResources) {
             Set<String> dependencies = jsDependencyReader.readDependencies(jsResource.getJsResourceFile());
             for (String dependencyName : dependencies) {
                 if (useMocks) {
                     List<File> mockFirstPaths = new ArrayList<File>(paths);
                     mockFirstPaths.addAll(0, mockPaths);
 
                     jsResource.addDependency(getResourceByName(dependencyName, jsResourceMap, mockFirstPaths));
                 }
                 else {
                     jsResource.addDependency(this.getResourceByName(dependencyName, jsResourceMap, paths));
                 }
             }
         }
         return jsResources;
     }
 
     public JSResource getResourceByName(String name, Map<String, JSResource> jsResourceMap, List<File> paths) throws IOException, CircularDependencyException {
         JSResource jsResource = jsResourceMap.get(name);
 
         if (jsResource == null) {
                 String fileName = name.replace('.', File.separatorChar) + JS_EXTENSION;
 
                 File jsResourceFile = fileFinder.getFile(fileName, paths);
 
                if (jsResourceFile == null) {
                    List<File> attemptedPaths = new ArrayList<File>();
                    for (File path : paths) {
                        attemptedPaths.add(new File(path, fileName));
                    }

                    throw new RuntimeException("Unable to find resource with name : " + name + ". Locations searched : " + attemptedPaths);
                }

                 jsResource = new JSResource(jsResourceFile, name);
 
                 jsResourceMap.put(name, jsResource);
 
                 Set<String> dependencies = jsDependencyReader.readDependencies(jsResourceFile);
                 for (String dependencyName : dependencies) {
                     jsResource.addDependency(getResourceByName(dependencyName, jsResourceMap, paths));
                 }
         }
         return jsResource;
     }
 
     @Override
     public List<JSResource> getResourcesFor(String... names) throws IOException, CircularDependencyException {
         return getResourcesFor(getResourceByName(false, names));
     }
 
     @Override
     public List<JSResource> getResourcesFor(JSResource... namedResources) {
         return getResourcesFor(Arrays.asList(namedResources));
     }
 
     public List<JSResource> getResourcesFor(List<JSResource> jsResources) {
         Map<JSResource, Set<JSResource>> dependencyMap = new HashMap<JSResource, Set<JSResource>>();
         addResourcesToDependencyMap(jsResources, dependencyMap);
 
         List<JSResource> orderedResources = new ArrayList<JSResource>();
         while (dependencyMap.size() > 0) {
             List<JSResource> jsResourcesWithSatisfiedDependencies = new ArrayList<JSResource>();
             Set<JSResource> jsResourcesKeys = new HashSet<JSResource>(dependencyMap.keySet());
             for (JSResource jsResource : jsResourcesKeys) {
                 Set<JSResource> remainingDependencies = dependencyMap.get(jsResource);
                 if (remainingDependencies.size() == 0) {
                     jsResourcesWithSatisfiedDependencies.add(jsResource);
                     dependencyMap.remove(jsResource);
                 }
             }
 
             if (jsResourcesWithSatisfiedDependencies.size() == 0) {
                 throw new RuntimeException("Unable to order resources due to unsatisfied dependencies");
             }
 
             Collections.sort(jsResourcesWithSatisfiedDependencies, new Comparator<JSResource>() {
                 @Override
                 public int compare(JSResource o1, JSResource o2) {
                     return o1.getName().compareTo(o2.getName());
                 }
             });
 
             orderedResources.addAll(jsResourcesWithSatisfiedDependencies);
 
             for (Set<JSResource> jsResourceDependencies : dependencyMap.values()) {
                 jsResourceDependencies.removeAll(orderedResources);
             }
         }
 
         return orderedResources;
     }
 
     private void addResourcesToDependencyMap(Collection<JSResource> jsResources, Map<JSResource, Set<JSResource>> dependencyMap) {
         for (JSResource jsResource : jsResources) {
             if (!dependencyMap.containsKey(jsResource)) {
                 Set<JSResource> dependencies = jsResource.getDependencies();
                 dependencyMap.put(jsResource, new HashSet<JSResource>(dependencies));
                 addResourcesToDependencyMap(dependencies, dependencyMap);
             }
         }
     }
 
     @Override
     public List<JSResource> getTestResourcesFor(String... names) throws IOException, CircularDependencyException {
         List<JSResource> jsResources = getResourceByName(true, names);
         return getResourcesFor(jsResources);
     }
 
     private void addJSResourceAndDependenciesToMap(Map<String, JSResource> jsResourceMap, JSResource jsResource) {
         if (!jsResourceMap.containsKey(jsResource.getName())) {
             jsResourceMap.put(jsResource.getName(), jsResource);
             for (JSResource jsResourceDependency : jsResource.getDependencies()) {
                 addJSResourceAndDependenciesToMap(jsResourceMap, jsResourceDependency);
             }
         }
     }
 
     @Override
     public boolean contains(String name) throws IOException, CircularDependencyException {
         return getResourceByName(false, name) != null;
     }
 
     @Override
     public List<JSResource> getAllResources() throws IOException, CircularDependencyException {
         Map<String, JSResource> jsResourceMap = new HashMap<String, JSResource>();
         Map<String, Set<String>> dependencyMap = new HashMap<String, Set<String>>();
         for (File path : paths) {
             List<File> jsFiles = fileFinder.getAllFilesWithExtension(path, JS_EXTENSION);
             for (File jsFile : jsFiles) {
                 JSResource jsResource = new JSResource(jsFile, getNameFromLocation(path, jsFile));
                 String resourceName = jsResource.getName();
                 if (jsResourceMap.containsKey(resourceName)) {
                     logger.warn("Original resource {} will be overwritten by {}", jsResourceMap.get(resourceName).getJsResourceFile(), jsFile);
                     dependencyMap.remove(resourceName);
                 }
 
                 jsResourceMap.put(resourceName, jsResource);
                 dependencyMap.put(resourceName, jsDependencyReader.readDependencies(jsFile));
             }
         }
 
         for (String jsResourceName : dependencyMap.keySet()) {
             JSResource jsResource = jsResourceMap.get(jsResourceName);
             for (String dependencyName : dependencyMap.get(jsResourceName)) {
                 JSResource dependency = jsResourceMap.get(dependencyName);
                 if (dependency == null) {
                     throw new RuntimeException("Cannot resolve the dependency of " + jsResourceName + " on " + dependencyName);
                 }
                 jsResource.addDependency(dependency);
             }
         }
 
         return getResourcesFor(new ArrayList<JSResource>(jsResourceMap.values()));
     }
 
     public String getNameFromLocation(File rootPath, File jsFile) {
 		String jsFileName = jsFile.getName();
 		String name = jsFileName.substring(0, jsFileName.length() - 3);
 		File parent = jsFile.getParentFile();
 		while (!parent.equals(rootPath)) {
 			name = parent.getName() + "." + name;
 			parent = parent.getParentFile();
 		}
 		return name;
 	}
 
     public void setFileFinder(FileFinder fileFinder) {
         this.fileFinder = fileFinder;
     }
 
     public void setJsDependencyReader(JSDependencyReader jsDependencyReader) {
         this.jsDependencyReader = jsDependencyReader;
     }
 }
