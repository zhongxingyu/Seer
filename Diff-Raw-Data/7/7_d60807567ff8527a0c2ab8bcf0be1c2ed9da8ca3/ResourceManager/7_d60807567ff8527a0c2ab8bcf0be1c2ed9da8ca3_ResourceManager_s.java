 /*****************************************************************************
  *                The Virtual Light Company Copyright (c) 1999
  *                               Java Source
  *
  * This code is licensed under the GNU Library GPL. Please read license.txt
  * for the full details. A copy of the LGPL may be found at
  *
  * http://www.gnu.org/copyleft/lgpl.html
  *
  * Project:    URI Class libs
  *
  * Version History
  * Date        TR/IWOR  Version  Programmer
  * ----------  -------  -------  ------------------------------------------
  *
  ****************************************************************************/
 
 package org.ietf.uri;
 
 // Standard imports
 import java.io.*;
 import java.util.*;
 import java.net.URLStreamHandler;
 
 // Application specific imports
 // none
 
 /**
  * The manager of all resources used by a single application or VM instance.
  * <P>
  *
  * The manager deals with the loading and handling of all of the protocol and
  * content resources needed by URIs. There is a centralised management to
  * allow greater efficiency for caching. All factories, except those related
  * to RDS management, that are set by the user and system are collated in this
  * class.
  * <P>
  *
  * Three types of resources are currently managed - Filename maps, content
  * handlers and protocol handlers. The process for dealing with the three
  * are identical. There are three ways, in order of priority, for specifying
  * the class to use. Variations are provided below.
  *
  * <OL>
  * <LI>Using the set handler factory.
  * <LI>Locating the class in the package from the defined system resource
  * <LI>Locating the class from a default package name
  * </OL>
  *
  * The properties for each can be summed up as follows:
  * <TABLE>
  * <TR>
  *   <TH>Access type
  *   <TH>Factory Class
  *   <TH>Package list property
  *   <TH>Default package
  * </TR>
  * <TR>
  *   <TD>Filename map
  *   <TD>FileNameMap
  *   <TD>uri.filename.handler.pkgs
  *   <TD>org.ietf.uri.filemap
  * </TR>
  * <TR>
  *   <TD>Content Handlers
  *   <TD>ContentHandlerFactory
  *   <TD>uri.content.handler.pkgs
  *   <TD>org.ietf.uri.content
  * </TR>
  * <TR>
  *   <TD>Protocol Handlers
  *   <TD>URIResourceStreamFactory
  *   <TD>uri.protocol.handler.pkgs
  *   <TD>org.ietf.uri.protocol
  * </TR>
  * </TABLE>
  *
  * In this implementation, a default handler also works with the standard
  * properties defined by the <CODE>java.net</CODE> package. Where this
  * occurs, wrapper classes are used to make sure of seemless integration.
  * This allows easy movement of old code to the new system with minimal
  * distruption.
  * <P>
  *
  * <B>File Name Maps</B>
  * <P>
  *
  * Filename maps are used to map a file extension to a MIME type. This is
  * sometimes used when the underlying protocol does not provide any hints
  * to the MIME type of the resource.
  * <P>
  *
  * Unlike the standard java implementation, multiple filename maps may be
  * defined using the system property. On lookup, the property is parsed to
  * find the list of packages to examine when loading filename maps. These
  * packages are taken and the class name FileNameMap is added. The system
  * then attempts to load that fully qualified class name.
  * <P>
  *
  * The load process is progressive and only loads maps as needed. If a
  * matching extension to MIME type cannot be loaded from the current
  * collection, then the next class/package on the list is tried. If this
  * does not work, then another is tried until all options are exhausted.
  * <P>
  *
  * Due to bugs in the content handler code from java.net, we also attempt
  * to load the standard content-types.properties file. This can be found
  * at <CODE>&lt;java.home&gt;/lib/content-types.properties</CODE>. This
  * is read in, parsed and the content type and file extension kept.
  * <P>
  *
  * Just to make sure, the following content types are also automatically
  * added:
  * <TABLE>
  * <TR><TD><CODE>image/png</CODE><TD><CODE>.png</CODE>
  * <TR><TD><CODE>image/targa</CODE><TD><CODE>.tga</CODE>
  * <TR><TD><CODE>image/tif</CODE><TD><CODE>.tif, .tiff</CODE>
  * </TABLE>
  *
  * <B>Content Handlers</B>
  * <P>
  *
  * Content handlers are loaded from the given package list. A new content
  * handler from this list is found by taking the MIME type of the content
  * and then replacing '/' with '.' and all other non-alphanumeric
  * characters with the underscore character '_'. This is then appended to
  * the package name and attempted to be loaded. If this fails, then the
  * next package on the list is tried.
  * <P>
  *
  * To cater for backwards compatiblity, the system also handles the system
  * property <CODE>java.content.handler.pkgs</CODE> and also the package
  * <CODE>sun.net.www.content</CODE> using the same search mechanism. These
  * searched only after the other alternatives have been exhausted. If
  * handlers are located then a wrapper class is placed around them and
  * returned through the normal content handler mechanism.
  * <P>
  *
  * <B>Protocol Handlers</B>
  * <P>
  * Content handlers are loaded from the given package list. A new protocol
  * handler from this list is found by taking the protocol name, setting it
  * to lowercase, appending that to the package and then appending the
  * class name <CODE>Handler</CODE>. The result is a class that is
  * <PRE>
  *   &lt;package&gt;.&lt;protocol&gt;.Handler
  * </PRE>
  * <P>
  *
  * Unfortunately, backwards compatibility to the java.net equivalents is
  * not possible. Sun relies on the use of the <CODE>URLStreamHandler</CODE>
  * classes to provide protocol specific capabilities. The
  * <CODE>openConnection()</CODE> method is protected and hence we cannot
  * directly derive any useful benefits from it by using the stream handlers
  * in a seamless way.
  * <P>
  *
  * This implementation does not yet handle and caching optomisations
  * for dynamically changing the system properties while making calls.
  * This will be coming in a future implementation.
  * <P>
  *
  * If the protocol handlers are instances of some of the more derived
  * java.net classes such as HttpURLConnection, these traits are not
  * currently passed through.
  * <P>
  *
  * For details on URIs see the IETF working group:
  * <A HREF="http://www.ietf.org/html.charters/urn-charter.html">URN</A>
  * <P>
  *
  * This softare is released under the
  * <A HREF="http://www.gnu.org/copyleft/lgpl.html">GNU LGPL</A>
  * <P>
  *
  * DISCLAIMER:<BR>
  * This software is the under development, incomplete, and is
  * known to contain bugs. This software is made available for
  * review purposes only. Do not rely on this software for
  * production-quality applications or for mission-critical
  * applications.
  * <P>
  *
  * Portions of the APIs for some new features have not
  * been finalized and APIs may change. Some features are
  * not fully implemented in this release. Use at your own risk.
  * <P>
  *
  * @author  Justin Couch
  * @version 0.7 (27 August 1999)
  */
 final class ResourceManager
 {
   // Constants for filename maps
   private static final String FILE_PKG_PROP = "uri.filename.handler.pkgs";
   private static final String FILE_DEFAULT_PKG = "org.ietf.uri.filemap";
 
   // constants for content handlers
   private static final String CONTENT_PKG_PROP = "uri.content.handler.pkgs";
   private static final String CONTENT_JAVA_PROP = "java.content.handler.pkgs";
 
   private static final String CONTENT_DEFAULT_PKG = "org.ietf.uri.content";
   private static final String CONTENT_JAVA_PKG = "sun.net.www.content";
 
   // constants for protocol handlers
   private static final String PROTOCOL_PKG_PROP = "uri.protocol.handler.pkgs";
 
   private static final String PROTOCOL_DEFAULT_PKG = "org.ietf.uri.protocol";
 
 
 
   // Lots of factory handling variables. Sorted by function
 
   // Filename maps
 
   /** when all else fails map of stuff loaded from content-types.properties */
   private static FileNameMap default_map;
 
   /** default filename map (factory) */
   private static FileNameMap filename_factory = null;
 
   /** The currently loaded filename maps, in load order */
   private static HashMap filename_handlers = new HashMap();
 
   // Content handlers
 
   /** The currently set content factory */
   private static ContentHandlerFactory content_factory = null;
 
   /** Holder of the currently loaded content handlers */
   private static HashMap content_handlers = new HashMap();
 
   /**
    * Map of a Class type to the content handler that supports it. Although
    * multiple content handlers may support a given Class type, only the first
    * one found is actually stored in the map.
    */
   private static HashMap class_handlers = new HashMap();
 
   // Protocol Handlers
 
   /** The currently set content factory */
   private static URIResourceStreamFactory protocol_factory = null;
 
   /** Holder of the currently loaded content handlers */
   private static HashMap protocol_handlers = new HashMap();
 
   /**
    * Static initializer. Loads the contents of the default content types
    * properties file: java_home/lib/content-types.properties.
    */
   static
   {
     File file = new File(System.getProperty("java.home") +
             File.separator +
             "lib" +
             File.separator +
             "content-types.properties");
 
     default_map = parseJavaContentTypes(file);
 
     // if we can, dump in a few more here
     if(default_map instanceof DefaultFileMap)
     {
       DefaultFileMap map = (DefaultFileMap)default_map;
 
       map.addContentType("image/png", "png");
       map.addContentType("image/targa", "tga");
       map.addContentType("image/tiff", "tif");
       map.addContentType("image/tiff", "tiff");
     }
   }
 
   //---------------------------------------------------------------------------
   // Filename Map Code
   //---------------------------------------------------------------------------
 
   /**
    * Add a type to the default mapping. This is used by the ResourceConnection
    * derivatives if they need to insert a default type into filename map to
    * make sure that they get processed correctly.
    *
    * @param mimetype The mime type to add
    * @param ext The file extension used for this mapping
    */
   static void addContentTypeToDefaultMap(String mimetype, String ext)
   {
     DefaultFileMap map = (DefaultFileMap)default_map;
     map.addContentType(mimetype, ext);
   }
 
   /**
    * Set the protocol handler factory. If factories are not allowed to be set
    * then a SecurityException will be generated. The factory may be removed
    * by passing <CODE>null</CODE> as the parameter.
    *
    * @param fac The factory to be set
    * @exception SecurityException Factories are not allowed to be set.
    */
   static void setFileNameMap(FileNameMap map)
     throws SecurityException
   {
     // In the end, we may want to have a specific permission to check rather
     // using the default set factory as this allows many other loop holes into
     // the system.
     SecurityManager security = System.getSecurityManager();
 
     if (security != null)
         security.checkSetFactory();
 
     filename_factory = map;
   }
 
   /**
    * Fetch the current File name map. Useful if you are going to
    * replace the currently set one. If no factory is currently set then a
    * <CODE>null</CODE> is returned.
    *
    * @return The currently set factory
    */
   static FileNameMap getFileNameMap()
   {
     return filename_factory;
   }
 
   /**
    * Fetch the mime type for the given filename. First check the factory
    * (if set), then the list of provided packages. It then checks the default
    * package. If none can be found then it attempts to try the default map
    * that comes as part of the standard java setups.
    *
    * @param filename The name of the file to get the type for
    * @return The appropriate MIME type or null if none can be found
    */
   static String getMIMEType(String filename)
   {
     String ret_val = null;
 
     if(filename_factory != null)
       ret_val = filename_factory.getContentTypeFor(filename);
 
     if(ret_val != null)
       return ret_val;
 
     // Now try looping through all of the currently loaded maps. We
     // read the current system property and dice it up. We look up
     // the currently loaded set based on the package name from the
     // list, not the full qualified name. This allows us to handle
     // dynamically changing property values.
 
     String pkg_list = null;
 
     try
     {
       pkg_list = System.getProperty(FILE_PKG_PROP);
     }
     catch(SecurityException se)
     {
       // if we can't read it, then ignore it. We still have the default pkg
     }
 
     // append the default package to the list and prepare it for use
     pkg_list = (pkg_list == null) ?
                  FILE_DEFAULT_PKG :
                  pkg_list + '|' + FILE_DEFAULT_PKG;
 
     boolean match_found = false;
     StringTokenizer strtok = new StringTokenizer(pkg_list, "|");
 
 
     // Loop through all of the available package names. Trim, splice and dice
     // each package looking for the appropriate class instance.
 
     FileNameMap map;
 
     while(!match_found && strtok.hasMoreTokens())
     {
       String pkg_name = strtok.nextToken().trim();
       pkg_name = pkg_name.toLowerCase();
 
       // check the handler table first
       map = (FileNameMap)filename_handlers.get(pkg_name);
 
       if(map != null)
       {
         ret_val = map.getContentTypeFor(filename);
         if(ret_val != null)
         {
           match_found = true;
           continue;
         }
       }
 
       // make up the class name to load
       StringBuffer buffer = new StringBuffer(pkg_name);
       buffer.append(".FileNameMap");
 
       try
       {
         Class cls = Class.forName(buffer.toString());
         if(cls != null)
         {
           map = (FileNameMap)cls.newInstance();
 
           // add it to the hash table of handlers
           filename_handlers.put(pkg_name, map);
 
           ret_val = map.getContentTypeFor(filename);
 
           if(ret_val != null)
             match_found = true;
         }
       }
       catch(Exception e)
       {
         // don't worry. We can ignore this one and try the next
       }
     }
 
     if(ret_val != null)
       return ret_val;
 
     // OK, well everything else has failed, so lets try the standard java
     // installed system.
     ret_val = default_map.getContentTypeFor(filename);
 
     return ret_val;
   }
 
   /**
    * The reverse lookup of MIME type to filename.
    */
   static String getFileExtension(String type)
   {
     String ret_val = null;
 
     if(filename_factory != null)
       ret_val = filename_factory.getFileExtension(type);
 
     if(ret_val != null)
       return ret_val;
 
     // Now try looping through all of the currently loaded maps. We
     // read the current system property and dice it up. We look up
     // the currently loaded set based on the package name from the
     // list, not the full qualified name. This allows us to handle
     // dynamically changing property values.
 
     String pkg_list = null;
 
     try
     {
       pkg_list = System.getProperty(FILE_PKG_PROP);
     }
     catch(SecurityException se)
     {
       // if we can't read it, then ignore it. We still have the default pkg
     }
 
     // append the default package to the list and prepare it for use
     pkg_list = (pkg_list == null) ?
                  FILE_DEFAULT_PKG :
                  pkg_list + '|' + FILE_DEFAULT_PKG;
 
     boolean match_found = false;
     StringTokenizer strtok = new StringTokenizer(pkg_list, "|");
 
 
     // Loop through all of the available package names. Trim, splice and dice
     // each package looking for the appropriate class instance.
 
     FileNameMap map;
 
     while(!match_found && strtok.hasMoreTokens())
     {
       String pkg_name = strtok.nextToken().trim();
       pkg_name = pkg_name.toLowerCase();
 
       // check the handler table first
       map = (FileNameMap)filename_handlers.get(pkg_name);
 
       if(map != null)
       {
         ret_val = map.getFileExtension(type);
         if(ret_val != null)
         {
           match_found = true;
           continue;
         }
       }
 
       // make up the class name to load
       StringBuffer buffer = new StringBuffer(pkg_name);
       buffer.append(".FileNameMap");
 
       try
       {
         Class cls = Class.forName(buffer.toString());
         if(cls != null)
         {
           map = (FileNameMap)cls.newInstance();
 
           // add it to the hash table of handlers
           filename_handlers.put(pkg_name, map);
 
           ret_val = map.getFileExtension(type);
 
           if(ret_val != null)
             match_found = true;
         }
       }
       catch(Exception e)
       {
         // don't worry. We can ignore this one and try the next
       }
     }
 
     if(ret_val != null)
       return ret_val;
 
     // OK, well everything else has failed, so lets try the standard java
     // installed system.
     ret_val = default_map.getFileExtension(type);
 
     return ret_val;
   }
 
   /**
    * Parse the content-types.properties file. All we need is the mime type
    * and the file extensions from each property defined in this file.  To
    * do this, we look for the first instance of ':' and take the string
    * before that as the content type, then look for the string
    * "file_extensions" in it after that.
    *
    * @param file A reference to the file representing the content types
    * @return A reference to a filename map representation of the file
    */
   private static FileNameMap parseJavaContentTypes(File file)
   {
     TwoWayHashMap ext_map = new TwoWayHashMap();
 
     try
     {
       FileInputStream fis = new FileInputStream(file);
       BufferedInputStream bis = new BufferedInputStream(fis);
 
       Properties prop_list = new Properties();
       prop_list.load(bis);
       bis.close();
       fis.close();
 
       // Now we need to rip the table apart to make it work properly.
       // the table is defined with the mime type as key and extensions
       // as the value where we want it the other way around.
 
       Enumeration key_list = prop_list.keys();
       String key;
       String field;
       String ext;
       StringTokenizer strtok;
       StringTokenizer field_tok;
 
       while(key_list.hasMoreElements())
       {
         key = (String)key_list.nextElement();
 
         strtok = new StringTokenizer((String)prop_list.get(key), ";");
 
         while(strtok.hasMoreTokens())
         {
           field = strtok.nextToken();
           if(((field.charAt(0) == 'f') || (field.charAt(0) == 'F')) &&
              field.startsWith("file_extension"))
           {
             // OK, here's the right field. Now strip the extension part
             // and then rip the list for filenames
             field_tok = new StringTokenizer(field, "=");
 
             // discard the name
             field_tok.nextToken();
             while(field_tok.hasMoreTokens())
             {
              ext = field_tok.nextToken(",");
               ext = ext.substring(1);
               ext_map.put(ext, key);
             }
 
             // so we found and conquered the field for this key, break
             // and on with the next
             break;
           }
         }
       }
     }
     catch(IOException ioe)
     {
     }
 
     // now lets fill the map with other useful stuff, just in case
 
     FileNameMap ret_val = null;
 
     try
     {
       ret_val = new DefaultFileMap(ext_map);
     }
     catch(NullPointerException npe)
     {
     }
 
     return ret_val;
   }
 
   //---------------------------------------------------------------------------
   // Content Handler Code
   //---------------------------------------------------------------------------
 
   /**
    * Set the content handler factory. If factories are not allowed to be set
    * then a SecurityException will be generated. The factory may be removed
    * by passing <CODE>null</CODE> as the parameter.
    *
    * @param fac The factory to be set
    * @exception SecurityException Factories are not allowed to be set.
    */
   static void setContentHandlerFactory(ContentHandlerFactory fac)
     throws SecurityException
   {
     // In the end, we may want to have a specific permission to check rather
     // using the default set factory as this allows many other loop holes into
     // the system.
     SecurityManager security = System.getSecurityManager();
 
     if (security != null)
         security.checkSetFactory();
 
     content_factory = fac;
   }
 
   /**
    * Fetch the current content handler factory. Useful if you are going to
    * replace the currently set one. If no factory is currently set then a
    * <CODE>null</CODE> is returned.
    *
    * @return The currently set factory
    */
   static ContentHandlerFactory getContentHandlerFactory()
   {
     return content_factory;
   }
 
   /**
    * Fetch the content handler for the given mime type. First check the factory
    * (if set), then the list of provided packages. It then checks the default
    * package. If none can be found then it attempts to try the default packages
    * that come as part of the standard java setups. It places a wrapper around
    * these to make it conform with our local stuff.
    *
    * @param contentType The desired MIMEType of the handler
    * @return The appropriate content handler or null if none can be found
    */
   static ContentHandler getContentHandler(String contentType)
   {
     if(contentType == null)
       return null;
 
     // first try is our currently loaded lot
     ContentHandler handler = null;
 
     if(content_handlers != null)
       handler = (ContentHandler)content_handlers.get(contentType);
 
     if(handler != null)
       return handler;
 
     // next we try the factory
     if(content_factory != null)
       handler = content_factory.createContentHandler(contentType);
 
     if(handler != null)
       return handler;
 
     // hmmm... didn't find one in the factory, lets try the system properties.
     String pkg_list = null;
 
     try
     {
       pkg_list = System.getProperty(CONTENT_PKG_PROP);
     }
     catch(SecurityException se)
     {
       // if we can't read it, then ignore it. We still have the default pkg
     }
 
     // append the default package to the list and prepare it for use
     pkg_list = (pkg_list == null) ?
                  CONTENT_DEFAULT_PKG :
                  pkg_list + '|' + CONTENT_DEFAULT_PKG;
 
     boolean handler_found = false;
     char[] content_name = typeToPackageName(contentType);
     StringTokenizer strtok = new StringTokenizer(pkg_list, "|");
 
     // Loop through all of the available package names. Trim, splice and dice
     // each package looking for the appropriate class instance.
     while(!handler_found && strtok.hasMoreTokens())
     {
       String pkg_name = strtok.nextToken().trim();
       pkg_name = pkg_name.toLowerCase();
       StringBuffer buffer = new StringBuffer(pkg_name);
 
       // make up the class name to load
       buffer.append('.');
       buffer.append(content_name);
 
       try
       {
         Class cls = Class.forName(buffer.toString());
         if(cls != null)
         {
           handler = (ContentHandler)cls.newInstance();
           handler_found = true;
         }
       }
       catch(Exception e)
       {
         // don't worry. We can ignore this one and try the next
       }
     }
 
     if(handler != null)
       return handler;
 
     // Hmmm.... so we don't have one here. How about we try the old standard
     // java stuff. If we find one, we'll need to put a wrapper around it.
     // Same as the code above, but this time with different property list and
     // different ContentHandler packaging....
     try
     {
       pkg_list = System.getProperty(CONTENT_JAVA_PROP);
     }
     catch(SecurityException se)
     {
       // if we can't read it, then ignore it. We still have the default pkg
     }
 
     // append the default package to the list and prepare it for use
     pkg_list = (pkg_list == null) ?
                  CONTENT_JAVA_PKG :
                  pkg_list + '|' + CONTENT_JAVA_PKG;
 
     java.net.ContentHandler java_handler = null;
     content_name = typeToPackageName(contentType);
     strtok = new StringTokenizer(pkg_list, "|");
 
     // Loop through all of the available package names. Trim, splice and dice
     // each package looking for the appropriate class instance.
     while(!handler_found && strtok.hasMoreTokens())
     {
       String pkg_name = strtok.nextToken().trim();
       pkg_name = pkg_name.toLowerCase();
       StringBuffer buffer = new StringBuffer(pkg_name);
 
       // make up the class name to load
       buffer.append('.');
       buffer.append(content_name);
 
       try
       {
         Class cls = Class.forName(buffer.toString());
         if(cls != null)
         {
           java_handler = (java.net.ContentHandler)cls.newInstance();
           handler = new JavaNetContentHandlerWrapper(java_handler);
           handler_found = true;
         }
       }
       catch(Exception e)
       {
         // don't worry. We can ignore this one and try the next
       }
     }
 
     return handler;
   }
 
   /**
    * Fetch the content handler for the given mime type and also is capable of
    * delivering it as one of the required set of classes. The rest of the
    * content handler lookup rules apply.
    *
    * @param contentType The desired MIME type
    * @param classes The list of class types you want matched
    * @return The appropriate content handler or null if none can be found
    */
   static ContentHandler getContentHandler(String contentType, Class[] classes)
   {
     // Load the class_handler map.
 
     ContentHandler ret_val = null;
 
     if((classes == null) || (classes.length == 0))
       ret_val = getContentHandler(contentType);
     else
       ret_val = getContentHandlerByClass(contentType, classes);
 
     return ret_val;
   }
 
   /**
    * Load all known content handlers and sort them by the classes they
    * support. Content handlers are loaded in order of preference and stored
    * in the class_handler map. Classes do not overwrite previously registered
    * instances. It does not load the items from the factory because it assumes
    * that they may be dynamically changed.
    */
   private static ContentHandler getContentHandlerByClass(String contentType,
                                                          Class[] wanted)
   {
     if(contentType == null)
       return null;
 
     ContentHandler handler = null;
 
     if(content_handlers != null)
       handler = (ContentHandler)content_handlers.get(contentType);
 
     if(handler != null && checkClassMatch(handler, wanted))
       return handler;
 
     // next we try the factory
     if(content_factory != null)
       handler = content_factory.createContentHandler(contentType);
 
     if(handler != null)
       return handler;
 
     String pkg_list = null;
 
     try
     {
       pkg_list = System.getProperty(CONTENT_PKG_PROP);
     }
     catch(SecurityException se)
     {
       // if we can't read it, then ignore it. We still have the default pkg
     }
 
     // append the default package to the list and prepare it for use
     pkg_list = (pkg_list == null) ?
                  CONTENT_DEFAULT_PKG :
                  pkg_list + '|' + CONTENT_DEFAULT_PKG;
 
     boolean handler_found = false;
     char[] content_name = typeToPackageName(contentType);
     StringTokenizer strtok = new StringTokenizer(pkg_list, "|");
 
     // Loop through all of the available package names. Trim, splice and dice
     // each package to load each handler instance. Only add to the handler map
     // for classes that have not already had a content handler registered.
     while(!handler_found && strtok.hasMoreTokens())
     {
       String pkg_name = strtok.nextToken().trim();
       pkg_name = pkg_name.toLowerCase();
       StringBuffer buffer = new StringBuffer(pkg_name);
 
       // make up the class name to load
       buffer.append('.');
       buffer.append(content_name);
 
       try
       {
         Class cls = Class.forName(buffer.toString());
         if(cls != null)
         {
           handler = (ContentHandler)cls.newInstance();
           handler_found = checkClassMatch(handler, wanted);
         }
       }
       catch(Exception e)
       {
         // don't worry. We can ignore this one and try the next
       }
     }
 
     if(handler != null)
       return handler;
 
     // Hmmm.... so we don't have one here. How about we try the old standard
     // java stuff. If we find one, we'll need to put a wrapper around it.
     // Same as the code above, but this time with different property list and
     // different ContentHandler packaging....
     try
     {
       pkg_list = System.getProperty(CONTENT_JAVA_PROP);
     }
     catch(SecurityException se)
     {
       // if we can't read it, then ignore it. We still have the default pkg
     }
 
     // append the default package to the list and prepare it for use
     pkg_list = (pkg_list == null) ?
                  CONTENT_JAVA_PKG :
                  pkg_list + '|' + CONTENT_JAVA_PKG;
 
     java.net.ContentHandler java_handler = null;
     content_name = typeToPackageName(contentType);
     strtok = new StringTokenizer(pkg_list, "|");
 
     // Loop through all of the available package names. Trim, splice and dice
     // each package looking for the appropriate class instance.
     while(!handler_found && strtok.hasMoreTokens())
     {
       String pkg_name = strtok.nextToken().trim();
       pkg_name = pkg_name.toLowerCase();
       StringBuffer buffer = new StringBuffer(pkg_name);
 
       // make up the class name to load
       buffer.append('.');
       buffer.append(content_name);
 
       try
       {
         Class cls = Class.forName(buffer.toString());
         if(cls != null)
         {
           java_handler = (java.net.ContentHandler)cls.newInstance();
           handler = new JavaNetContentHandlerWrapper(java_handler);
           handler_found = checkClassMatch(handler, wanted);
         }
       }
       catch(Exception e)
       {
         // don't worry. We can ignore this one and try the next
       }
     }
 
     // Now work on the packages defined for the
     return handler;
   }
 
   /**
    * Check to see if the content handler class matches one of the requested
    * types.
    *
    * @param handler The handler to check
    * @param wanted The classes to check for
    * @return true if there is a match between the two
    */
   private static boolean checkClassMatch(ContentHandler handler,
                                          Class[] wanted)
   {
     Class[] supported = handler.getSupportedClasses();
     int size = supported == null ? 0 : supported.length;
     boolean has_match = false;
 
     for(int i = 0; i < size && !has_match; i++)
       for(int j = 0; j < wanted.length && !has_match; j++)
         has_match = wanted[j].equals(supported[i]);
 
     return has_match;
   }
 
   /**
    * Utility function to map a MIME content type into an equivalent
    * pair of class name components.  For example: "text/html" would
    * be returned as "text.html". Any character that is not alpha numeric
    * is converted to an underscore. Classes are always lower case.
    *
    * @param The string describing the MIMEType
    * @return An array of chars with the package name.
    */
   private static char[] typeToPackageName(String contentType)
   {
     // make sure we canonicalize the class name: all lower case
     contentType = contentType.toLowerCase();
     int len = contentType.length();
     char name[] = new char[len];
 
     contentType.getChars(0, len, name, 0);
 
     for (int i = 0; i < len; i++)
     {
       char ch = name[i];
       if (ch == '/')
       {
         name[i] = '.';
       }
       else if (!(('A' <= ch) && (ch <= 'Z') ||
                  ('a' <= ch) && (ch <= 'z') ||
                  ('0' <= ch) && (ch <= '9')))
       {
         name[i] = '_';
       }
     }
     return name;
   }
 
   //---------------------------------------------------------------------------
   // Protocol Handler Code
   //---------------------------------------------------------------------------
 
   /**
    * Set the protocol handler factory. If factories are not allowed to be set
    * then a SecurityException will be generated. The factory may be removed
    * by passing <CODE>null</CODE> as the parameter.
    *
    * @param fac The factory to be set
    * @exception SecurityException Factories are not allowed to be set.
    */
   static void setProtocolHandlerFactory(URIResourceStreamFactory fac)
     throws SecurityException
   {
     // In the end, we may want to have a specific permission to check rather
     // using the default set factory as this allows many other loop holes into
     // the system.
     SecurityManager security = System.getSecurityManager();
 
     if (security != null)
         security.checkSetFactory();
 
     protocol_factory = fac;
   }
 
   /**
    * Fetch the current protocol handler factory. Useful if you are going to
    * replace the currently set one. If no factory is currently set then a
    * <CODE>null</CODE> is returned.
    *
    * @return The currently set factory
    */
   static URIResourceStreamFactory getProtocolHandlerFactory()
   {
     return protocol_factory;
   }
 
   /**
    * Fetch the protocol handler for the given mime type. First check the factory
    * (if set), then the list of provided packages. It then checks the default
    * package. If none can be found then it attempts to try the default packages
    * that come as part of the standard java setups. It places a wrapper around
    * these to make it conform with our local stuff.
    *
    * @param protocol The desired protocol type
    * @return The appropriate protocol handler or null if none can be found
    */
   static URIResourceStream getProtocolHandler(String protocol)
   {
     if(protocol == null)
       return null;
 
     // first try is our currently loaded lot
     URIResourceStream handler = null;
 
     if(protocol_handlers != null)
       handler = (URIResourceStream)protocol_handlers.get(protocol);
 
     if(handler != null)
       return handler;
 
     // next we try the factory
     if(protocol_factory != null)
       handler = protocol_factory.createURIResourceStream(protocol);
 
     if(handler != null)
       return handler;
 
     // hmmm... didn't find one in the factory, lets try the system properties.
     String pkg_list = null;
 
     try
     {
       pkg_list = System.getProperty(PROTOCOL_PKG_PROP);
     }
     catch(SecurityException se)
     {
       // if we can't read it, then ignore it. We still have the default pkg
     }
 
     // append the default package to the list and prepare it for use
     pkg_list = (pkg_list == null) ?
                  PROTOCOL_DEFAULT_PKG :
                  pkg_list + '|' + PROTOCOL_DEFAULT_PKG;
 
     boolean handler_found = false;
     StringTokenizer strtok = new StringTokenizer(pkg_list, "|");
 
     // Loop through all of the available package names. Trim, splice and dice
     // each package looking for the appropriate class instance.
     while(!handler_found && strtok.hasMoreTokens())
     {
       String pkg_name = strtok.nextToken().trim();
       pkg_name = pkg_name.toLowerCase();
       StringBuffer buffer = new StringBuffer(pkg_name);
 
       // make up the class name to load
       buffer.append('.');
       buffer.append(protocol);
       buffer.append(".Handler");
 
       try
       {
         Class cls = Class.forName(buffer.toString());
         if(cls != null)
         {
           handler = (URIResourceStream)cls.newInstance();
           handler_found = true;
         }
       }
       catch(Exception e)
       {
         // don't worry. We can ignore this one and try the next
       }
     }
 
     // we can't try the standard java versions, so exit now.
 
     return handler;
   }
 }
