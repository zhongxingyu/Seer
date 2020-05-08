 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004 Brian M. Clapper. All rights reserved.
 
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
 
 package org.clapper.curn.output.script;
 
 import org.clapper.curn.ConfigFile;
 import org.clapper.curn.ConfiguredOutputHandler;
 import org.clapper.curn.Curn;
 import org.clapper.curn.CurnException;
 import org.clapper.curn.FeedInfo;
 import org.clapper.curn.Version;
 import org.clapper.curn.output.FileOutputHandler;
 import org.clapper.curn.parser.RSSChannel;
 import org.clapper.curn.parser.RSSItem;
 
 import org.clapper.util.config.ConfigurationException;
 import org.clapper.util.config.NoSuchSectionException;
 import org.clapper.util.io.FileUtil;
 import org.clapper.util.logging.Logger;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.apache.bsf.BSFException;
 import org.apache.bsf.BSFEngine;
 import org.apache.bsf.BSFManager;
 
 /**
  * Provides an output handler calls a script via the Apache Jakarta
  * {@link <a href="http://jakarta.apache.org/bsf/">Bean Scripting Framework</a>}
  * (BSF). This handler supports any scripting language supported by BSF. In
  * addition to the  configuration parameters supported by the
  * {@link FileOutputHandler} base class, this handler supports the
  * following additional configuration variables, which must be specified in
  * the handler's configuration section.
  *
  * <table border="1" align="center">
  *   <tr>
  *     <th>Parameter</th>
  *     <th>Explanation</th>
  *   </tr>
  *
  *   <tr>
  *     <td><tt>Script</tt></td>
  *     <td>Path to the script to be invoked. The script will be called
  *         as if from the command line, except that additional objects will
  *         be available via BSF.
  *     </td>
  *   </tr>
  *
  *   <tr>
  *     <td><tt>Language</tt></td>
  *     <td><p>The scripting language, as recognized by BSF. This handler
  *         supports all the scripting language engines that are registered
  *         with the BSF software. Some of the scripting language engines
  *         are actually bundled with BSF. Some are not. Regardless, of
  *         course, the actual the jar files for the scripting
  *         languages themselves must be in the CLASSPATH at runtime, for those
  *         languages to be available.</p>
  * 
  *         <p>If you want to use a BSF scripting language engine that isn't
  *         one of the above, simply extend this class and override the
  *         {@link #registerAdditionalScriptingEngines} method. In that method,
  *         call <tt>BSFManager.registerScriptingEngine()</tt> for each
  *         additional language you want to support. For example, to provide
  *         a handler that supports
  *         {@link <a href="http://www.judoscript.com/">JudoScript</a>},
  *         you might write an output handler that looks like this:</p>
  * <blockquote><pre>
  * import org.clapper.curn.CurnException;
  * import org.clapper.curn.output.script.ScriptOutputHandler;
  * import org.apache.bsf.BSFManager;
  *
  * public class MyOutputHandler extends ScriptOutputHandler
  * {
  *     public JudoScriptOutputHandler()
  *     {
  *         super();
  *     }
  *
  *     public void registerAdditionalScriptingEngines()
  *         throws CurnException
  *     {
  *         BSFManager.registerScriptingLanguage ("judoscript",
  *                                               "com.judoscript.BSFJudoEngine",
  *                                               new String[] {"judo", "jud"});
  *     }
  * }
  * </pre></blockquote>
  *
  *         Then, simply use your class instead of <tt>ScriptOutputHandler</tt>
  *         in your configuration file.
  *     </td>
  *   </tr>
  * </table>
  *
  * <p>This handler's {@link #displayChannel displayChannel()} method does
  * not invoke the script; instead, it buffers up all the channels so that
  * the {@link #flush} method can invoke the script. That way, the overhead
  * of invoking the script only occurs once. Via the BSF engine, this
  * handler makes available an iterator of special objects that wrap both
  * the {@link RSSChannel} and {@link FeedInfo} objects for a given channel.
  * See below for a more complete description.</p>
  *
  * <p>The complete list of objects bound into the BSF beanspace follows.</p>
  *
  * <table border="0">
  *   <tr valign="top">
  *     <th>Bound name</th>
  *     <th>Java type</th>
  *     <th>Explanation</th>
  *   </tr>
  *
  *   <tr valign="top">
  *     <td>channels</td>
  *     <td><tt>java.util.Collection</tt></td>
  *     <td>An <tt>Collection</tt> of special internal objects that wrap
  *         both {@link RSSChannel} and {@link FeedInfo} objects. The
  *         wrapper objects provide two methods:</td>
  *
  *         <ul>
  *           <li><tt>getChannel()</tt> gets the <tt>RSSChannel</tt> object
  *           <li><tt>getFeedInfo()</tt> gets the <tt>FeedInfo</tt> object
  *         </ul>
  *    </tr>
  *
  *   <tr valign="top">
  *     <td>outputPath</td>
  *     <td><tt>java.lang.String</tt></td>
  *     <td>The path to an output file. The script should write its output
  *         to that file. Overwriting the file is fine. If the script generates
  *         no output, then it can ignore the file.</td>
  *   </tr>
  *
  *   <tr valign="top">
  *     <td>config</td>
  *     <td><tt>{@link ConfigFile}</tt></td>
  *     <td>The <tt>org.clapper.curn.ConfigFile</tt> object that represents
  *         the parsed configuration data. Useful in conjunction with the
  *         "configSection" object, to parse additional parameters from
  *         the configuration.</td>
  *   </tr>
  *
  *   <tr valign="top">
  *     <td>configSection</td>
  *     <td><tt>java.lang.String</tt></td>
  *     <td>The name of the configuration file section in which the output
  *         handler was defined. Useful if the script wants to access
  *         additional script-specific configuration data.</td>
  *   </tr>
  *
  *   <tr valign="top">
  *     <td>mimeType</td>
  *     <td><tt>java.lang.StringBuffer</tt></td>
  *     <td>A <tt>StringBuffer</tt> object to which the script should
  *         append the MIME type that corresponds to the generated output.
  *         If the script generates no output, then it can ignore this
  *         object.</td>
  *   </tr>
  *
  *   <tr valign="top">
  *     <td>logger</td>
  *     <td>{@link Logger org.clapper.util.logging.Logger}</td>
  *     <td>A <tt>Logger</tt> object, useful for logging messages to
  *         the <i>curn</i> log file.</td>
  *   </tr>
  *
  *   <tr valign="top">
  *     <td>version</td>
  *     <td><tt>java.lang.String</tt></td>
  *     <td>Full <i>curn</i> version string, in case the script wants to
  *         include it in the generated output
  *   </tr>
  * </table>
  *
  * <p>For example, the following Jython script can be used as a template
  * for a Jython output handler.</p>
  *
  * <blockquote>
  * <pre>
  * import sys
  *
  * def __init__ (self):
  *     """
  *     Initialize a new TextOutputHandler object.
  *     """
  *     self.__channels    = bsf.lookupBean ("channels")
  *     self.__outputPath  = bsf.lookupBean ("outputPath")
  *     self.__mimeTypeBuf = bsf.lookupBean ("mimeType")
  *     self.__config      = bsf.lookupBean ("config")
  *     self.__sectionName = bsf.lookupBean ("configSection")
  *     self.__logger      = bsf.lookupBean ("logger");
  *     self.__version     = bsf.lookupBean ("version")
  *     self.__message     = None
  *
  * def processChannels (self):
  *     """
  *     Process the channels passed in through the Bean Scripting Framework.
  *     """
  *
  *     out = open (self.__outputPath, "w")
  *     msg = self.__config.getOptionalStringValue (self.__sectionName,
  *                                                 "Message",
  *                                                 None)
  *
  *     totalNew = 0
  *
  *     # First, count the total number of new items
  *
  *     iterator = self.__channels.iterator()
  *     while iterator.hasNext():
  *         channel_wrapper = iterator.next()
  *         channel = channel_wrapper.getChannel()
  *         totalNew = totalNew + channel.getItems().size()
  *
  *     if totalNew > 0:
  *         # If the config file specifies a message for this handler,
  *         # display it.
  *
  *         if msg != None:
  *             out.println (msg)
  *             out.println ()
  *
  *         # Now, process the items
  *
  *         iterator = self.__channels.iterator()
  *         while iterator.hasNext():
  *             channel_wrapper = iterator.next()
  *             channel = channel_wrapper.getChannel()
  *             feed_info = channel_wrapper.getFeedInfo()
  *             self.__process_channel (out, channel, feed_info, indentation)
  *
  *         self.__mimeTypeBuf.append ("text/plain")
  *
  *         # Output a footer
  *
  *         self.__indent (out, indentation)
  *         out.write ("\n")
  *         out.write (self.__version + "\n")
  *         out.close ()
  *
  * def process_channel (channel, feed_info):
  *     item_iterator = channel.getItems().iterator()
  *     while item_iterator.hasNext():
  *         # Do output for item
  *         ...
  *
  * main()
  * </pre>
  * </blockquote>
  *
  * @see org.clapper.curn.OutputHandler
  * @see FileOutputHandler
  * @see org.clapper.curn.Curn
  * @see org.clapper.curn.parser.RSSChannel
  *
  * @version <tt>$Revision$</tt>
  */
 public class ScriptOutputHandler extends FileOutputHandler
 {
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/
 
     /*----------------------------------------------------------------------*\
                                Inner Classes
     \*----------------------------------------------------------------------*/
 
     /**
      * Wraps an RSSChannel object and its FeedInfo object.
      */
     public class ChannelWrapper
     {
         private RSSChannel channel;
         private FeedInfo   feedInfo;
 
         ChannelWrapper (RSSChannel channel, FeedInfo feedInfo)
         {
             this.channel  = channel;
             this.feedInfo = feedInfo;
         }
 
         public RSSChannel getChannel()
         {
             return this.channel;
         }
 
         public FeedInfo getFeedInfo()
         {
             return this.feedInfo;
         }
     }
 
     /**
      * Type alias
      */
     private class ChannelList extends ArrayList<ChannelWrapper>
     {
         ChannelList()
         {
             super();
         }
     }
 
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     private BSFManager                 bsfManager     = null;
     private ConfigFile                 config         = null;
     private Collection<ChannelWrapper> channels       = new ChannelList();
     private String                     scriptPath     = null;
     private String                     scriptString   = null;
     private StringBuffer               mimeTypeBuffer = new StringBuffer();
     private String                     language       = null;
     private Logger                     scriptLogger   = null;
 
     /**
      * For logging
      */
     private static Logger log = new Logger (ScriptOutputHandler.class);
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Construct a new <tt>ScriptOutputHandler</tt>.
      */
     public ScriptOutputHandler()
     {
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Initializes the output handler for another set of RSS channels.
      *
      * @param config     the parsed <i>curn</i> configuration data
      * @param cfgHandler the <tt>ConfiguredOutputHandler</tt> wrapper
      *                   containing this object; the wrapper has some useful
      *                   metadata, such as the object's configuration section
      *                   name and extra variables.
      *
      * @throws ConfigurationException  configuration error
      * @throws CurnException           some other initialization error
      */
     public final void initOutputHandler (ConfigFile              config,
                                          ConfiguredOutputHandler cfgHandler)
         throws ConfigurationException,
                CurnException
     {
         this.config = config;
 
         // Parse handler-specific configuration variables
 
         String section = cfgHandler.getSectionName();
 
         try
         {
             if (section != null)
             {
                 scriptPath = config.getConfigurationValue (section, "Script");
                 language  = config.getConfigurationValue (section, "Language");
 
             }
         }
         
         catch (NoSuchSectionException ex)
         {
             throw new ConfigurationException (ex);
         }
 
         // Verify that the script exists.
 
         File scriptFile = new File (scriptPath);
         if (! scriptFile.exists())
         {
             scriptPath = null;
             throw new ConfigurationException (section,
                                               "Script file \""
                                             + scriptFile.getPath()
                                             + "\" does not exist.");
         }
 
         if (! scriptFile.isFile())
         {
             scriptPath = null;
             throw new ConfigurationException (section,
                                               "Script file \""
                                             + scriptFile.getPath()
                                             + "\" is not a regular file.");
         }
 
         // Call the registerAdditionalScriptingEngines() method, which
         // subclasses can override to provide their own bindings.
 
         registerAdditionalScriptingEngines();
 
         // Allocate a new BSFManager. This must happen after all the extra
         // scripting engines are registered.
 
 	bsfManager = new BSFManager();
 
         // Set up a logger for the script. The logger name can't have dots
         // in it, because the underlying logging API (Jakarta Commons
         // Logging) strips them out, thinking they're class/package
         // delimiters. That means we have to strip the extension.
         // Unfortunately, the extension conveys information (i.e., the
         // language). Add the script language to the stripped name.
 
         StringBuffer scriptLoggerName = new StringBuffer();
         String scriptName = scriptFile.getName();
         scriptLoggerName.append (FileUtil.getFileNameNoExtension (scriptName));
         scriptLoggerName.append ("[" + language + "]");
         scriptLogger = new Logger (scriptLoggerName.toString());
 
         // Register the beans we know about now. The other come after we
         // process the channels.
 
         bsfManager.registerBean ("mimeType", mimeTypeBuffer);
         bsfManager.registerBean ("config", config);
         bsfManager.registerBean ("configSection", section);
         bsfManager.registerBean ("logger", scriptLogger);
         bsfManager.registerBean ("version", Version.getFullVersion());
 
         // Load the contents of the script into an in-memory buffer.
 
         scriptString = loadScript (scriptFile);
 
         channels.clear();
         mimeTypeBuffer.setLength (0);
     }
 
     /**
      * Display the list of <tt>RSSItem</tt> news items to whatever output
      * is defined for the underlying class. This handler simply buffers up
      * the channel, so that {@link #flush} can pass all the channels to the
      * script.
      *
      * @param channel  The channel containing the items to emit. The method
      *                 should emit all the items in the channel; the caller
      *                 is responsible for clearing out any items that should
      *                 not be seen.
      * @param feedInfo Information about the feed, from the configuration
      *
      * @throws CurnException  unable to write output
      */
     public final void displayChannel (RSSChannel  channel,
                                       FeedInfo    feedInfo)
         throws CurnException
     {
         // Do some textual conversion on the channel data.
 
         channel.setTitle (convert (channel.getTitle()));
 
         Collection<RSSItem> items = channel.getItems();
         if ((items != null) && (items.size() > 0))
         {
             for (Iterator it = items.iterator(); it.hasNext(); )
             {
                 RSSItem item = (RSSItem) it.next();
                 item.setTitle (convert (item.getTitle()));
 
                 String s = item.getAuthor();
                 if (s != null)
                     item.setAuthor (convert (s));
 
                 s = item.getSummary();
                 if (s != null)
                     item.setSummary (convert (s));
             }
         }
 
         // Save the channel.
 
         channels.add (new ChannelWrapper (channel, feedInfo));
     }
     
     /**
      * Flush any buffered-up output.
      *
      * @throws CurnException  unable to write output
      */
     public final void flush() throws CurnException
     {
         try
         {
             // Load the scripting engine
 
             BSFEngine scriptEngine = bsfManager.loadScriptingEngine (language);
 
             // Register the various script beans.
 
             bsfManager.registerBean ("channels", channels);
             bsfManager.registerBean ("outputPath", getOutputFile().getPath());
 
             // Run the script
 
             scriptEngine.exec (scriptPath, 0, 0, scriptString);
         }
 
         catch (BSFException ex)
         {
            log.error ("Error interacting with Bean Scripting Framework", ex);
             throw new CurnException (Curn.BUNDLE_NAME,
                                      "ScriptOutputHandler.bsfError",
                                      "Error interacting with Bean Scripting "
                                   + "Framework: {0}",
                                     new Object[] {ex.getMessage()},
                                      ex);
         }
     }
 
     /**
      * Get the content (i.e., MIME) type for output produced by this output
      * handler.
      *
      * @return the content type
      */
     public final String getContentType()
     {
         return mimeTypeBuffer.toString();
     }
 
     /**
      * Register additional scripting language engines that are not
      * supported by this class. By default, this method does nothing.
      * Subclasses that wish to register additional BSF scripting engine
      * bindings should override this method and use
      * <tt>BSFManager.registerScriptingEngine()</tt> to register the
      * engined. See the class documentation, above, for additional details.
      *
      * @throws CurnException on error
      */
     public void registerAdditionalScriptingEngines()
         throws CurnException
     {
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Load the contents of the external script (any file, really) into an
      * in-memory buffer.
      *
      * @param scriptFile    the script file
      *
      * @return the string representing the loaded script
      *
      * @throws CurnException on error
      */
     private String loadScript (File scriptFile)
         throws CurnException
     {
         try
         {
             Reader       r = new BufferedReader (new FileReader (scriptFile));
             StringWriter w = new StringWriter();
             int          c;
 
             while ((c = r.read()) != -1)
                 w.write (c);
 
             r.close();
 
             return w.toString();
         }
 
         catch (IOException ex)
         {
             throw new CurnException (Curn.BUNDLE_NAME,
                                      "ScriptOutputHandler.cantLoadScript",
                                      "Failed to load script \"{0}\" into "
                                    + "memory.",
                                      new Object[] {scriptFile.getPath()},
                                      ex);
         }
     }
 }
