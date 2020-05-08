 /**
  *
  * Copyright 2007-2009 (C) The original author or authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.papoose.core;
 
 import java.io.IOException;
 import java.net.URL;
 import java.security.AccessController;
 import java.security.CodeSource;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.security.cert.Certificate;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.jar.Attributes;
 import java.util.jar.Manifest;
 
 import org.apache.xbean.classloader.NamedClassLoader;
 import org.apache.xbean.classloader.ResourceHandle;
 import org.apache.xbean.classloader.ResourceLocation;
 import org.osgi.framework.BundleException;
 
 import org.papoose.core.descriptions.DynamicDescription;
 import org.papoose.core.descriptions.ImportDescription;
 import org.papoose.core.spi.ArchiveStore;
 
 
 /**
  * @version $Revision$ $Date$
  */
 public class BundleClassLoader extends NamedClassLoader
 {
     private final static URL[] EMPTY_URLS = new URL[0];
     private final Papoose framework;
     private final BundleGeneration bundle;
     private final Set<Wire> wires;
     private final List<Wire> requiredBundles;
     private final String[] exportedPackages;
     private final Set<DynamicDescription> dynamicImports;
     private final List<ResourceLocation> boundClassPath;
     private final Set<ArchiveStore> archiveStores;
 
 
     BundleClassLoader(String name, ClassLoader parent,
                       Papoose framework,
                       BundleGeneration bundle,
                       Set<Wire> wires,
                       List<Wire> requiredBundles,
                       String[] bootDelegates,
                       String[] exportedPackages,
                       Set<DynamicDescription> dynamicImports,
                       List<ResourceLocation> boundClassPath,
                       Set<ArchiveStore> archiveStores) throws BundleException
     {
         super(name, EMPTY_URLS, parent);
 
         assert name != null;
         assert framework != null;
         assert bundle != null;
         assert wires != null;
         assert requiredBundles != null;
         assert boundClassPath != null;
 
         this.framework = framework;
         this.bundle = bundle;
         this.wires = new HashSet<Wire>(wires);
         this.requiredBundles = requiredBundles;
         this.exportedPackages = exportedPackages;
         this.dynamicImports = Collections.synchronizedSet(dynamicImports);
         this.boundClassPath = boundClassPath;
         this.archiveStores = archiveStores;
     }
 
 
     BundleGeneration getBundle()
     {
         return bundle;
     }
 
     public Set<Wire> getWires()
     {
         return Collections.unmodifiableSet(wires);
     }
 
     public String[] getExportedPackages()
     {
         return exportedPackages;
     }
 
     @SuppressWarnings({ "EmptyCatchBlock" })
     public Class<?> loadClass(String className) throws ClassNotFoundException
     {
         if (className.startsWith("java.")) return getParent().loadClass(className);
 
         String packageName = className.substring(0, Math.max(0, className.lastIndexOf('.')));
 
         for (String delegate : framework.getBootDelegates())
         {
             if ((delegate.endsWith(".") && packageName.regionMatches(0, delegate, 0, delegate.length() - 1)) || packageName.equals(delegate))
             {
                 try
                 {
                     return getParent().loadClass(className);
                 }
                 catch (ClassNotFoundException doNothing)
                 {
                 }
             }
         }
 
         return delegateLoadClass(className);
     }
 
     public URL getResource(String resourceName)
     {
         ResourceHandle handle;
 
        if (resourceName.startsWith("/")) resourceName = resourceName.substring(1, resourceName.length());

         for (ResourceLocation location : boundClassPath)
         {
             if ((handle = location.getResourceHandle(resourceName)) != null) return handle.getUrl();
         }
 
         return null;
     }
 
     public Enumeration<URL> findResources(String resourceName) throws IOException
     {
         List<URL> urls = new ArrayList<URL>();
         ResourceHandle handle;
 
         for (ResourceLocation location : boundClassPath)
         {
             if ((handle = location.getResourceHandle(resourceName)) != null) urls.add(handle.getUrl());
         }
 
         return Collections.enumeration(urls);
     }
 
     protected String findLibrary(String libname)
     {
         String path = null;
         for (ArchiveStore store : archiveStores)
         {
             if ((path = store.loadLibrary(libname)) != null) break;
         }
         return path;
     }
 
     @SuppressWarnings({ "EmptyCatchBlock" })
     protected synchronized Class<?> delegateLoadClass(String className) throws ClassNotFoundException
     {
         Class clazz = findLoadedClass(className);
 
         if (clazz != null)
         {
             return clazz;
         }
 
         String packageName = className.substring(0, Math.max(0, className.lastIndexOf('.')));
 
         for (Wire wire : wires)
         {
             if (wire.validFor(className)) return wire.getBundleClassLoader().delegateLoadClass(className);
         }
 
         for (Wire wire : requiredBundles)
         {
             try
             {
                 if (wire.validFor(className)) return wire.getBundleClassLoader().delegateLoadClass(className);
             }
             catch (ClassNotFoundException doNothing)
             {
             }
         }
 
         for (ResourceLocation location : boundClassPath)
         {
             try
             {
                 return findClass(location, className);
             }
             catch (ClassNotFoundException doNothing)
             {
             }
         }
 
         for (String exportedPackage : exportedPackages)
         {
             if (exportedPackage.equals(packageName)) throw new ClassNotFoundException();
         }
 
         synchronized (dynamicImports)
         {
             DynamicDescription used = null;
 
             try
             {
                 for (DynamicDescription dynamicDescription : dynamicImports)
                 {
                     for (String packagePattern : dynamicDescription.getPackagePatterns())
                     {
                         if (packagePattern.length() == 0
                             || (packagePattern.endsWith(".") && packageName.startsWith(packagePattern))
                             || packageName.equals(packagePattern))
                         {
                             BundleController bundleController = bundle.getBundleController();
                             ImportDescription importDescription = new ImportDescription(Collections.singleton(packageName), dynamicDescription.getParameters());
 
                             Wire wire = bundleController.getFramework().getBundleManager().resolve(bundleController, importDescription);
 
                             if (wire != null)
                             {
                                 used = dynamicDescription;
                                 wires.add(wire);
 
                                 return wire.getBundleClassLoader().delegateLoadClass(className);
                             }
                         }
                     }
                 }
             }
             finally
             {
                 if (used != null) assert dynamicImports.remove(used);
             }
 
         }
 
         throw new ClassNotFoundException();
     }
 
     protected Class<?> findClass(final ResourceLocation location, final String className) throws ClassNotFoundException
     {
         try
         {
             return AccessController.doPrivileged(new PrivilegedExceptionAction<Class>()
             {
                 public Class run() throws ClassNotFoundException
                 {
                     // first think check if we are allowed to define the package
                     SecurityManager securityManager = System.getSecurityManager();
                     if (securityManager != null)
                     {
                         int packageEnd = className.lastIndexOf('.');
                         if (packageEnd >= 0)
                         {
                             String packageName = className.substring(0, packageEnd);
                             securityManager.checkPackageDefinition(packageName);
                         }
                     }
 
                     // convert the class name to a file name
                     String resourceName = className.replace('.', '/') + ".class";
 
                     // find the class file resource
                     ResourceHandle resourceHandle = location.getResourceHandle(resourceName);
 
                     if (resourceHandle == null) throw new ClassNotFoundException(className);
 
                     byte[] bytes;
                     Manifest manifest;
                     try
                     {
                         // get the bytes from the class file
                         bytes = resourceHandle.getBytes();
 
                         // get the manifest for defining the packages
                         manifest = resourceHandle.getManifest();
                     }
                     catch (IOException e)
                     {
                         throw new ClassNotFoundException(className, e);
                     }
 
                     // get the certificates for the code source
                     Certificate[] certificates = resourceHandle.getCertificates();
 
                     // the code source url is used to define the package and as the security context for the class
                     URL codeSourceUrl = resourceHandle.getCodeSourceUrl();
 
                     // define the package (required for security)
                     definePackage(className, codeSourceUrl, manifest);
 
                     // this is the security context of the class
                     CodeSource codeSource = new CodeSource(codeSourceUrl, certificates);
 
                     // load the class into the vm
                     return defineClass(className, bytes, 0, bytes.length, codeSource);
                 }
             }, bundle.getBundleController().getFramework().getAcc());
         }
         catch (PrivilegedActionException e)
         {
             throw (ClassNotFoundException) e.getException();
         }
     }
 
     private void definePackage(String className, URL jarUrl, Manifest manifest)
     {
         int packageEnd = className.lastIndexOf('.');
         if (packageEnd < 0)
         {
             return;
         }
 
         String packageName = className.substring(0, packageEnd);
         String packagePath = packageName.replace('.', '/') + "/";
 
         Attributes packageAttributes = null;
         Attributes mainAttributes = null;
         if (manifest != null)
         {
             packageAttributes = manifest.getAttributes(packagePath);
             mainAttributes = manifest.getMainAttributes();
         }
         Package pkg = getPackage(packageName);
         if (pkg != null)
         {
             if (pkg.isSealed())
             {
                 if (!pkg.isSealed(jarUrl))
                 {
                     throw new SecurityException("Package was already sealed with another URL: package=" + packageName + ", url=" + jarUrl);
                 }
             }
             else
             {
                 if (isSealed(packageAttributes, mainAttributes))
                 {
                     throw new SecurityException("Package was already been loaded and not sealed: package=" + packageName + ", url=" + jarUrl);
                 }
             }
         }
         else
         {
             String specTitle = getAttribute(Attributes.Name.SPECIFICATION_TITLE, packageAttributes, mainAttributes);
             String specVendor = getAttribute(Attributes.Name.SPECIFICATION_VENDOR, packageAttributes, mainAttributes);
             String specVersion = getAttribute(Attributes.Name.SPECIFICATION_VERSION, packageAttributes, mainAttributes);
             String implTitle = getAttribute(Attributes.Name.IMPLEMENTATION_TITLE, packageAttributes, mainAttributes);
             String implVendor = getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR, packageAttributes, mainAttributes);
             String implVersion = getAttribute(Attributes.Name.IMPLEMENTATION_VERSION, packageAttributes, mainAttributes);
 
             URL sealBase = null;
             if (isSealed(packageAttributes, mainAttributes))
             {
                 sealBase = jarUrl;
             }
 
             definePackage(packageName, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
         }
     }
 
     private String getAttribute(Attributes.Name name, Attributes packageAttributes, Attributes mainAttributes)
     {
         if (packageAttributes != null)
         {
             String value = packageAttributes.getValue(name);
             if (value != null)
             {
                 return value;
             }
         }
         if (mainAttributes != null)
         {
             return mainAttributes.getValue(name);
         }
         return null;
     }
 
     private boolean isSealed(Attributes packageAttributes, Attributes mainAttributes)
     {
         return "true".equalsIgnoreCase(getAttribute(Attributes.Name.SEALED, packageAttributes, mainAttributes));
     }
 }
