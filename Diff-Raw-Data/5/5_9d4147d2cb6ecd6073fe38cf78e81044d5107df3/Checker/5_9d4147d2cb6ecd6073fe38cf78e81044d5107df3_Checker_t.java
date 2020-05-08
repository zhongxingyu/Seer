 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003 - 2004  Lars Khne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //////////////////////////////////////////////////////////////////////////////
 
 package net.sf.clirr;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Enumeration;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipEntry;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.net.URLClassLoader;
 
 import net.sf.clirr.checks.ClassHierarchyCheck;
 import net.sf.clirr.checks.ClassScopeCheck;
 import net.sf.clirr.checks.ClassModifierCheck;
 import net.sf.clirr.checks.GenderChangeCheck;
 import net.sf.clirr.checks.InterfaceSetCheck;
 import net.sf.clirr.checks.FieldSetCheck;
 import net.sf.clirr.checks.MethodSetCheck;
 import net.sf.clirr.event.ApiDifference;
 import net.sf.clirr.event.DiffListener;
 import net.sf.clirr.event.ScopeSelector;
 import net.sf.clirr.event.Severity;
 import net.sf.clirr.framework.ApiDiffDispatcher;
 import net.sf.clirr.framework.ClassChangeCheck;
 import net.sf.clirr.framework.ClassSelector;
 import net.sf.clirr.framework.CoIterator;
 import net.sf.clirr.framework.JavaClassNameComparator;
 import net.sf.clirr.framework.CheckerException;
 
 import org.apache.bcel.classfile.JavaClass;
 import org.apache.bcel.classfile.ClassParser;
 import org.apache.bcel.util.ClassSet;
 import org.apache.bcel.util.Repository;
 import org.apache.bcel.util.ClassLoaderRepository;
 
 /**
  * This is the main class to be used by Clirr frontends,
  * it implements the checking functionality of Clirr.
  * Frontends can create an instance of this class
  * and register themselves as DiffListeners, they are then
  * informed whenever an API change is detected by the
  * reportDiffs method.
  *
  * @author lkuehne
  */
 public final class Checker implements ApiDiffDispatcher
 {
 
     private List listeners = new ArrayList();
 
     private List classChecks = new ArrayList();
 
     private ScopeSelector scopeSelector = new ScopeSelector();
 
     /**
      * Package visible constructor for unit testing.
      */
     Checker(ClassChangeCheck ccc)
     {
         classChecks.add(ccc);
     }
 
     /**
      * Creates a new Checker.
      */
     public Checker()
     {
         classChecks.add(new ClassScopeCheck(this, scopeSelector));
         classChecks.add(new GenderChangeCheck(this));
         classChecks.add(new ClassModifierCheck(this));
         classChecks.add(new InterfaceSetCheck(this));
         classChecks.add(new ClassHierarchyCheck(this));
         classChecks.add(new FieldSetCheck(this, scopeSelector));
         classChecks.add(new MethodSetCheck(this, scopeSelector));
     }
 
     public ScopeSelector getScopeSelector()
     {
         return scopeSelector;
     }
 
     public void addDiffListener(DiffListener listener)
     {
         listeners.add(listener);
     }
 
     private void fireStart()
     {
         for (Iterator it = listeners.iterator(); it.hasNext();)
         {
             DiffListener diffListener = (DiffListener) it.next();
             diffListener.start();
         }
     }
 
     private void fireStop()
     {
         for (Iterator it = listeners.iterator(); it.hasNext();)
         {
             DiffListener diffListener = (DiffListener) it.next();
             diffListener.stop();
         }
     }
 
     public void fireDiff(ApiDifference diff)
     {
         for (Iterator it = listeners.iterator(); it.hasNext();)
         {
             DiffListener diffListener = (DiffListener) it.next();
             diffListener.reportDiff(diff);
         }
     }
 
     /**
      * Compare the classes in the two sets of jars and report any differences
      * to this object's DiffListener object. If the classes in those jars reference
      * third party classes (e.g. as base class, implemented interface or method param),
      * such third party classes must be made available via the xyzThirdPartyLoader
      * classloaders.
      *
      * @param origJars is a set of jars containing the "original" versions of
      * the classes to be compared.
      *
      * @param newJars is a set of jars containing the new versions of the
      * classes to be compared.
      *
      * @param origThirdPartyLoader is a classloader that provides third party classes
      * which are referenced by origJars.
      *
      * @param newThirdPartyLoader is a classloader that provides third party classes
      * which are referenced by newJars.
      *
      * @param classSelector is an object which determines which classes from the
      * old and new jars are to be compared. This parameter may be null, in
      * which case all classes in the old and new jars are compared.
      */
     public void reportDiffs(
             File[] origJars, File[] newJars,
             ClassLoader origThirdPartyLoader, ClassLoader newThirdPartyLoader,
             ClassSelector classSelector)
             throws CheckerException
     {
         if (classSelector == null)
         {
             // create a class selector that selects all classes
             classSelector = new ClassSelector(ClassSelector.MODE_UNLESS);
         }
 
         final ClassSet origClasses = createClassSet(
             origJars, origThirdPartyLoader, classSelector);
 
         final ClassSet newClasses = createClassSet(
             newJars, newThirdPartyLoader, classSelector);
 
         reportDiffs(origClasses, newClasses);
     }
 
     /**
      * Creates a set of classes to check.
      *
      * @param jarFiles a set of jar filed to scan for class files.
      *
      * @param thirdPartyClasses loads classes that are referenced
      * by the classes in the jarFiles
      *
      * @param classSelector is an object which determines which classes from the
      * old and new jars are to be compared. This parameter may be null, in
      * which case all classes in the old and new jars are compared.
      */
     private static ClassSet createClassSet(
             File[] jarFiles,
             ClassLoader thirdPartyClasses,
             ClassSelector classSelector)
             throws CheckerException
     {
         if (classSelector == null)
         {
             // create a class selector that selects all classes
             classSelector = new ClassSelector(ClassSelector.MODE_UNLESS);
         }
 
         ClassLoader classLoader = createClassLoader(jarFiles, thirdPartyClasses);
 
         Repository repository = new ClassLoaderRepository(classLoader);
 
         ClassSet ret = new ClassSet();
 
         for (int i = 0; i < jarFiles.length; i++)
         {
             File jarFile = jarFiles[i];
             ZipFile zip = null;
             try
             {
                 zip = new ZipFile(jarFile, ZipFile.OPEN_READ);
             }
             catch (IOException ex)
             {
                 throw new CheckerException("Cannot open " + jarFile + " for reading", ex);
             }
             Enumeration enumEntries = zip.entries();
             while (enumEntries.hasMoreElements())
             {
                 ZipEntry zipEntry = (ZipEntry) enumEntries.nextElement();
                 if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".class"))
                 {
                     JavaClass clazz = extractClass(zipEntry, zip, repository);
                     if (classSelector.isSelected(clazz))
                     {
                         ret.add(clazz);
                         repository.storeClass(clazz);
                     }
                 }
             }
         }
 
         return ret;
     }
 
     private static JavaClass extractClass(
                 ZipEntry zipEntry, ZipFile zip, Repository repository)
                 throws CheckerException
     {
         String name = zipEntry.getName();
         InputStream is = null;
         try
         {
             is = zip.getInputStream(zipEntry);
 
             ClassParser parser = new ClassParser(is, name);
             JavaClass clazz = parser.parse();
             clazz.setRepository(repository);
             return clazz;
         }
         catch (IOException ex)
         {
             throw new CheckerException("Cannot read " + zipEntry.getName() + " from " + zip.getName(), ex);
         }
         finally
         {
             if (is != null)
             {
                 try
                 {
                     is.close();
                 }
                 catch (IOException ex)
                 {
                     throw new CheckerException("Cannot close " + zip.getName(), ex);
                 }
             }
         }
     }
 
     private static ClassLoader createClassLoader(File[] jarFiles, ClassLoader thirdPartyClasses)
     {
         final URL[] jarUrls = new URL[jarFiles.length];
         for (int i = 0; i < jarFiles.length; i++)
         {
             File jarFile = jarFiles[i];
             try
             {
                 URL url = jarFile.toURL();
                 jarUrls[i] = url;
             }
             catch (MalformedURLException ex)
             {
                 // this should never happen
                 final IllegalArgumentException illegalArgumentException =
                         new IllegalArgumentException("Cannot create classloader with jar file " + jarFile);
                 illegalArgumentException.initCause(ex);
                 throw illegalArgumentException;
             }
         }
         final URLClassLoader jarsLoader = new URLClassLoader(jarUrls, thirdPartyClasses);
 
         return jarsLoader;
     }
 
 
     /**
      * Checks two sets of classes for api changes and reports
      * them to the DiffListeners.
      * @param compatibilityBaseline the classes that form the
      *        compatibility baseline to check against
      * @param currentVersion the classes that are checked for
      *        compatibility with compatibilityBaseline
      */
     private void reportDiffs(ClassSet compatibilityBaseline, ClassSet currentVersion)
     {
         fireStart();
         runClassChecks(compatibilityBaseline, currentVersion);
         fireStop();
     }
 
     private void runClassChecks(ClassSet compatBaseline, ClassSet currentVersion)
     {
         JavaClass[] compat = compatBaseline.toArray();
         JavaClass[] current = currentVersion.toArray();
 
         CoIterator iter = new CoIterator(
             JavaClassNameComparator.COMPARATOR, compat, current);
 
         while (iter.hasNext())
         {
             iter.next();
 
             JavaClass compatBaselineClass = (JavaClass) iter.getLeft();
             JavaClass currentClass = (JavaClass) iter.getRight();
 
             if (compatBaselineClass == null)
             {
                final String className = currentClass.getClassName();
                 final ApiDifference diff = new ApiDifference(
                     "Added " + className, Severity.INFO, className, null, null);
                 fireDiff(diff);
             }
             else if (currentClass == null)
             {
                final String className = compatBaselineClass.getClassName();
                 final ApiDifference diff = new ApiDifference(
                     "Removed " + className, Severity.ERROR, className, null, null);
                 fireDiff(diff);
             }
             else
             {
                 // class is available in both releases
                 boolean continueTesting = true;
                 for (Iterator it = classChecks.iterator(); it.hasNext() && continueTesting;)
                 {
                     ClassChangeCheck classChangeCheck = (ClassChangeCheck) it.next();
                     continueTesting = classChangeCheck.check(compatBaselineClass, currentClass);
                 }
             }
         }
     }
 }
