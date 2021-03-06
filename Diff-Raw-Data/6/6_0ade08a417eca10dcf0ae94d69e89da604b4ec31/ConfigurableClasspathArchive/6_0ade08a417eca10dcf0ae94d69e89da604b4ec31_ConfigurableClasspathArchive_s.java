 package org.apache.openejb.config;
 
 import org.apache.openejb.loader.SystemInstance;
 import org.apache.xbean.finder.archive.Archive;
 import org.apache.xbean.finder.archive.ClassesArchive;
 import org.apache.xbean.finder.archive.ClasspathArchive;
 import org.apache.xbean.finder.archive.CompositeArchive;
 import org.apache.xbean.finder.archive.FilteredArchive;
 import org.apache.xbean.finder.filter.Filter;
 import org.apache.xbean.finder.filter.FilterList;
 import org.apache.xbean.finder.filter.PackageFilter;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class ConfigurableClasspathArchive extends CompositeArchive implements ScanConstants {
     public ConfigurableClasspathArchive(final Module module, final URL... urls) {
         this(module, Arrays.asList(urls));
     }
 
     public ConfigurableClasspathArchive(final Module module, final Iterable<URL> urls) {
         this(module, false, urls);
     }
 
     public ConfigurableClasspathArchive(final ClassLoader loader, final Iterable<URL> urls) {
        this(new FakeModule(loader), urls);
     }
 
     public ConfigurableClasspathArchive(final ClassLoader loader, final URL url) {
         this(new FakeModule(loader), Arrays.asList(url));
     }
 
     public ConfigurableClasspathArchive(final Module module, boolean forceDescriptor, final Iterable<URL> urls) {
         super(archive(module, urls, forceDescriptor));
     }
 
     public static List<Archive> archive(final Module module, final Iterable<URL> urls, boolean forceDescriptor) {
         final List<Archive> archives = new ArrayList<Archive>();
         for (URL location : urls) {
             try {
                 archives.add(archive(module, location, forceDescriptor));
             } catch (Exception e) {
                 // ignored
             }
         }
         return archives;
     }
 
     public static Archive archive(final Module module, final URL location, boolean forceDescriptor) {
         final ClassLoader loader = module.getClassLoader();
         try {
             final URL scanXml = (URL) module.getAltDDs().get(SystemInstance.get().getProperty(SCAN_XML_PROPERTY, SCAN_XML_NAME));
             final ScanUtil.ScanHandler scan = ScanUtil.read(scanXml);
             final Archive packageArchive = packageArchive(scan.getPackages(), loader, location);
             final Archive classesArchive = classesArchive(scan.getPackages(), scan.getClasses(), loader);
 
             if (packageArchive != null && classesArchive != null) {
                 return new CompositeArchive(classesArchive, packageArchive);
             } else if (packageArchive != null) {
                 return  packageArchive;
             }
             return classesArchive;
         } catch (IOException e) {
             if (forceDescriptor) {
                 return new ClassesArchive();
             }
             return ClasspathArchive.archive(loader, location);
         }
     }
 
     public static Archive packageArchive(final Set<String> packageNames, final ClassLoader loader, final URL url) {
         if (!packageNames.isEmpty()) {
             return new FilteredArchive(ClasspathArchive.archive(loader, url), filters(packageNames));
         }
         return null;
     }
 
     private static Filter filters(final Set<String> packageNames) {
         final List<Filter> filters = new ArrayList<Filter>();
         for (String packageName : packageNames) {
             filters.add(new PackageFilter(packageName));
         }
         return new FilterList(filters);
     }
 
     public static Archive classesArchive(final Set<String> packages, final Set<String> classnames, final ClassLoader loader) {
         Class<?>[] classes = new Class<?>[classnames.size()];
         int i = 0;
         for (String clazz : classnames) {
             // skip classes managed by package filtering
             if (packages != null && clazzInPackage(packages, clazz)) {
                 continue;
             }
 
             try {
                 classes[i++] = loader.loadClass(clazz);
             } catch (ClassNotFoundException e) {
                 // ignored
             }
         }
 
         if (i != classes.length) { // shouldn't occur
             final Class<?>[] updatedClasses = new Class<?>[i];
             System.arraycopy(classes, 0, updatedClasses, 0, i);
             classes = updatedClasses;
         }
 
         return new ClassesArchive(classes);
     }
 
     private static boolean clazzInPackage(final Collection<String> packagename, final String clazz) {
         for (String str : packagename) {
             if (clazz.startsWith(str)) {
                 return true;
             }
         }
         return false;
     }
 
     protected static class FakeModule extends Module {
         public FakeModule(final ClassLoader loader) {
             this(loader, Collections.EMPTY_MAP);
         }
 
         public FakeModule(final ClassLoader loader, final Map<String, Object> altDD) {
             super(false);
             setClassLoader(loader);
 
             URL scanXml = (URL) altDD.get(SCAN_XML_NAME);
             if (scanXml == null) {
                 scanXml = loader.getResource(SCAN_XML_NAME);
             }
             if (scanXml != null) {
                 getAltDDs().put(SCAN_XML_NAME, scanXml);
             }
         }
     }
 }
