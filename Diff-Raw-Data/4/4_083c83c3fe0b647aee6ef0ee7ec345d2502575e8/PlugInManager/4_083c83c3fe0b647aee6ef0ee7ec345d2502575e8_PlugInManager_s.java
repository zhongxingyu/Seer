 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms are permitted provided
   that: (1) source distributions retain this entire copyright notice and
   comment; and (2) modifications made to the software are prominently
   mentioned, and a copy of the original software (or a pointer to its
   location) are included. The name of the author may not be used to endorse
   or promote products derived from this software without specific prior
   written permission.
 
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 
   Effectively, this means you can do what you want with the software except
   remove this notice or take advantage of the author's name. If you modify
   the software and redistribute your modified version, you must indicate that
   your version is a modification of the original, and you must provide either
   a pointer to or a copy of the original.
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn;
 
 import org.clapper.curn.Curn;
 import org.clapper.curn.CurnException;
 
 import org.clapper.util.logging.Logger;
 
 import org.clapper.util.io.AndFileFilter;
 import org.clapper.util.io.OrFileFilter;
 import org.clapper.util.io.RegexFileFilter;
 import org.clapper.util.io.DirectoryFilter;
 import org.clapper.util.io.FileOnlyFilter;
 import org.clapper.util.io.FileFilterMatchType;
 
 import org.clapper.util.classutil.ClassFinder;
 import org.clapper.util.classutil.ClassFilter;
 import org.clapper.util.classutil.ClassInfo;
 import org.clapper.util.classutil.AndClassFilter;
 import org.clapper.util.classutil.AbstractClassFilter;
 import org.clapper.util.classutil.InterfaceOnlyClassFilter;
 import org.clapper.util.classutil.NotClassFilter;
 import org.clapper.util.classutil.RegexClassFilter;
 import org.clapper.util.classutil.SubclassClassFilter;
 import org.clapper.util.classutil.ClassLoaderBuilder;
 
 import java.io.File;
 import java.io.FileFilter;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import java.util.regex.PatternSyntaxException;
 
 /**
  * Responsible for loading the plug-ins and creating the {@link MetaPlugIn}
  * singleton that's used to run the loaded plug-ins. This functionality is
  * isolated in a separate class to permit implementing a future feature
  * that allows run-time substitution of different implementations of
  * <tt>PlugInManager</tt>.
  *
  * @see PlugIn
  * @see PlugInManager
  * @see MetaPlugIn
  * @see Curn
  *
  * @version <tt>$Revision$</tt>
  */
 public class PlugInManager
 {
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     /**
      * For log messages
      */
     private static Logger log = new Logger (PlugInManager.class);
 
     /**
      * List of located plug-in jars, zips, and subdirectories
      */
     private static Collection<File> plugInLocations = new ArrayList<File>();
 
     /**
      * File filter to use when looking for jars, zip files, and directories.
      */
     private static FileFilter plugInLocFilter = null;
 
     static
     {
         try
         {
             plugInLocFilter = new OrFileFilter
                 (
                  // must be a directory ...
                  new DirectoryFilter(),
 
                  // or, must be a file ending in .jar
 
                  new AndFileFilter (new RegexFileFilter
                                         ("\\.jar$",
                                          FileFilterMatchType.FILENAME),
                                     new FileOnlyFilter()),
 
                  // or, must be a file ending in .zip
 
                  new AndFileFilter (new RegexFileFilter
                                         ("\\.zip$",
                                          FileFilterMatchType.FILENAME),
                                     new FileOnlyFilter())
                 );
         }
 
         catch (PatternSyntaxException ex)
         {
             // Should not happen.
 
             assert (false);
         }
     }    
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Cannot be instantiated.
      */
     private PlugInManager()
     {
     }
 
     /*----------------------------------------------------------------------*\
                           Package-visible Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Find the plug-in jars.
      *
      * @return an unmodifiable collection of <tt>File</tt> objects representing
      *         the jars, zip and subdirectories found in the various plug-in
      *         directories. This collection is also stored internally.
      *
      * @throws CurnException on error
      */
     static Collection<File> findPlugInLocations()
         throws CurnException
     {
         // Find all directories, jars and zip files in the appropriate
         // plug-in directories, and load them into the ClassFinder for
         // searching. Try <install-path>/plugins, then $HOME/curn/plugins,
         // then $HOME/.curn/plugins. Also, add the latter two directories
         // themselves, in case they contain package-less classes.
 
         String curnHome = System.getProperty ("org.clapper.curn.home");
         File dir;
         if (curnHome != null)
         {
             dir = new File (curnHome + File.separator + "plugins");
             preparePlugInSearch (dir, plugInLocations);
             plugInLocations.add (dir);
         }
 
         String userHome = System.getProperty ("user.home");
         dir = new File (userHome
                       + File.separator
                       + "curn"
                       + File.separator
                       + "plugins");
         preparePlugInSearch (dir, plugInLocations);
         plugInLocations.add (dir);
 
         dir = new File (userHome
                       + File.separator
                       + ".curn"
                       + File.separator
                       + "plugins");
         preparePlugInSearch (dir, plugInLocations);
         plugInLocations.add (dir);
 
         return Collections.unmodifiableCollection (plugInLocations);
     }
 
     /**
      * Get a <tt>ClassLoader</tt> that will use the plug-in locations as
      * part of its search path.
      *
      * @param plugInLocations list of plug-in locations previously found
      *                        via a call to {@link findPlugInLocations}
      *
      * @throws CurnException on error
      */
     static ClassLoader getClassLoader (Collection<File> plugInLocations)
         throws CurnException
     {
         ClassLoaderBuilder clb = new ClassLoaderBuilder();
         clb.addClassPath();
         clb.add (plugInLocations);
         return clb.createClassLoader();
     }
 
     /**
      * Load the plug-ins and create the {@link MetaPlugIn} singleton.
      *
      * @param plugInLocations list of plug-in locations previously found
      *                        via a call to {@link findPlugInLocations}
      * @param classLoader     class loader to use
      *
      * @throws CurnException on error
      */
     static void loadPlugIns (Collection<File> plugInLocations,
                              ClassLoader      classLoader)
         throws CurnException
     {
         MetaPlugIn metaPlugIn = MetaPlugIn.createMetaPlugIn (classLoader);
 
         ClassFinder classFinder = new ClassFinder();
         classFinder.add (plugInLocations);
         classFinder.addClassPath();
 
         // Configure the ClassFinder's filter.
 
         ClassFilter classFilter =
             new AndClassFilter
                 (
                  // Must implement org.clapper.curn.PlugIn
 
                  new SubclassClassFilter (PlugIn.class),
 
                  // Must be concrete
 
                  new NotClassFilter (new AbstractClassFilter()),
 
                  // Weed out certain things
 
                  new NotClassFilter (new RegexClassFilter ("^java\\.")),
                  new NotClassFilter (new RegexClassFilter ("^javax\\."))
                 );
 
         Collection<ClassInfo> classes = new ArrayList<ClassInfo>();
         classFinder.findClasses (classes, classFilter);
 
         if (classes.size() == 0)
             log.info ("No plug-ins found.");
         else
             loadPlugInClasses (classes, classLoader);
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Find the jars, zip files and directories under the specified directory
      * and load them into a ClassFinder.
      *
      * @param dir              the directory
      * @parma plugInLocations  where to put them
      */
     private static void preparePlugInSearch (File             dir,
                                              Collection<File> plugInLocations)
     {
         if (! dir.exists())
         {
             log.info ("Plug-in directory \"" + dir.getPath() +
                       "\" does not exist.");
         }
 
         else if (! dir.isDirectory())
         {
             log.info ("Plug-in directory \"" + dir.getPath() +
                       "\" is not a directory.");
         }
 
         else
         {
             log.debug ("Looking for jars, etc., in directory \"" +
                        dir.getPath() + "\"");
 
             for (File f : dir.listFiles (plugInLocFilter))
             {
                 log.debug ("Found " + f.getPath());
                 plugInLocations.add (f);
             }
         }
     }
 
     /**
      * Load the plug-ins.
      *
      * @param classNames  the class names of the plug-ins
      * @param classLoader class loader to use
      */
     private static void loadPlugInClasses (Collection<ClassInfo> classes,
                                            ClassLoader           classLoader)
     {
         MetaPlugIn metaPlugIn = MetaPlugIn.getMetaPlugIn();
 
         for (ClassInfo classInfo : classes)
         {
             String className = classInfo.getClassName();
             try
             {
                 log.info ("Loading plug-in \"" + className + "\"");
                 Class cls = classLoader.loadClass (className);
 
                 // Instantite the plug-in via the default constructor and
                 // add it to the meta-plug-in
 
                 PlugIn plugIn = (PlugIn) cls.newInstance();
                 log.info ("Loaded plug-in \""
                         + plugIn.getName()
                         + "\" plug-in");
                 metaPlugIn.addPlugIn (plugIn);
             }
 
             catch (ClassNotFoundException ex)
             {
                 log.error ("Can't load plug-in \""
                          + className
                          + "\": "
                          + ex.toString());
             }
 
             catch (ClassCastException ex)
             {
                 log.error ("Can't load plug-in \""
                          + className
                          + "\": "
                          + ex.toString());
             }
 
             catch (IllegalAccessException ex)
             {
                log.error ("Plug-in \""
                          + className
                          + "\" has no accessible default constructor.");
             }
 
             catch (InstantiationException ex)
             {
                 log.error ("Can't instantiate plug-in \""
                          + className
                          + "\": "
                          + ex.toString());
             }
 
             catch (ExceptionInInitializerError ex)
             {
                 log.error ("Default constructor for plug-in \""
                          + className
                          + "\" threw an exception.",
                            ex.getException());
             }
 
             catch (Throwable ex)
             {
                 log.error ("Error loading plug-in \""
                          + className
                          + "\"",
                            ex);
             }
         }
     }
 }
