 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004-2005 Brian M. Clapper. All rights reserved.
 
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
 
 package org.clapper.curn.output;
 
 import org.clapper.curn.ConfigFile;
 import org.clapper.curn.ConfiguredOutputHandler;
 import org.clapper.curn.Curn;
 import org.clapper.curn.CurnException;
 import org.clapper.curn.FeedInfo;
 import org.clapper.curn.Version;
 import org.clapper.curn.parser.RSSChannel;
 import org.clapper.curn.parser.RSSItem;
 
 import org.clapper.util.config.ConfigurationException;
 import org.clapper.util.io.WordWrapWriter;
 import org.clapper.util.logging.Logger;
 import org.clapper.util.text.TextUtil;
 
 import java.io.IOException;
 import java.io.FileWriter;
 import java.io.File;
 import java.util.Date;
 import java.util.Collection;
 import java.util.Iterator;
 
 /**
  * Provides an output handler that writes the RSS channel and item summaries
  * as plain text. This handler supports the additional configuration items
  * that its parent {@link FileOutputHandler} class supports. It has no
  * class-specific configuration items of its own. It produces output only
  * if the channels contain
  *
  * @see org.clapper.curn.OutputHandler
  * @see FileOutputHandler
  * @see org.clapper.curn.Curn
  * @see org.clapper.curn.parser.RSSChannel
  *
  * @version <tt>$Revision$</tt>
  */
 public class TextOutputHandler extends FileOutputHandler
 {
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/
 
     private static final String HORIZONTAL_RULE =
                       "---------------------------------------"
                     + "---------------------------------------";
 
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     private WordWrapWriter  out         = null;
     private int             indentLevel = 0;
     private ConfigFile      config      = null;
     private StringBuffer    scratch     = new StringBuffer();
 
     /**
      * For logging
      */
     private static Logger log = new Logger (TextOutputHandler.class);
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Construct a new <tt>TextOutputHandler</tt>
      */
     public TextOutputHandler()
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
     public void initOutputHandler (ConfigFile              config, 
                                    ConfiguredOutputHandler cfgHandler)
         throws ConfigurationException,
                CurnException
     {
         this.config = config;
 
         File outputFile = super.getOutputFile();
         try
         {
             log.debug ("Opening output file \"" + outputFile + "\"");
             out = new WordWrapWriter (new FileWriter (outputFile));
         }
 
         catch (IOException ex)
         {
             throw new CurnException (Curn.BUNDLE_NAME,
                                      "OutputHandler.cantOpenFile",
                                      "Cannot open file \"{0}\" for output",
                                      new Object[] {outputFile.getPath()},
                                      ex);
         }
     }
 
     /**
      * Display the list of <tt>RSSItem</tt> news items to whatever output
      * is defined for the underlying class. Output is written to the
      * <tt>PrintWriter</tt> that was passed to the {@link #init init()}
      * method.
      *
      * @param channel  The channel containing the items to emit. The method
      *                 should emit all the items in the channel; the caller
      *                 is responsible for clearing out any items that should
      *                 not be seen.
      * @param feedInfo Information about the feed, from the configuration
      *
      * @throws CurnException  unable to write output
      */
     public void displayChannel (RSSChannel  channel,
                                 FeedInfo    feedInfo)
         throws CurnException
     {
         Collection items = channel.getItems();
         String     s;
 
         indentLevel = setIndent (0);
 
         if (items.size() != 0)
         {
             // Emit a site (channel) header.
 
             out.println();
             out.println (HORIZONTAL_RULE);
 
             out.println (convert (channel.getTitle()));
 
            URL url = channel.getURL();
             if (url != null)
                 out.println (url.toString());
 
             if (config.showDates())
             {
                 Date date = channel.getPublicationDate();
                 if (date != null)
                     out.println (date.toString());
             }
 
             if (config.showRSSVersion())
             {
                 s = channel.getRSSFormat();
                 if (s != null)
                     out.println ("(Format: " + s + ")");
             }
         }
 
         if (items.size() != 0)
         {
             // Now, process each item.
 
             for (Iterator it = items.iterator(); it.hasNext(); )
             {
                 RSSItem item = (RSSItem) it.next();
 
                 setIndent (++indentLevel);
 
                 out.println ();
 
                 s = item.getTitle();
                 out.println ((s == null) ? "(No Title)" : convert (s));
 
                 if (feedInfo.showAuthors())
                 {
                     Collection<String> authors = item.getAuthors();
                     if ((authors != null) && (authors.size() > 0))
                     {
                         s = TextUtil.join (authors, ", ");
                         out.println ("By " + convert (s));
                     }
                 }
 
                 out.println (item.getURL().toString());
 
                 if (config.showDates())
                 {
                     Date date = item.getPublicationDate();
                     if (date != null)
                         out.println (date.toString());
                 }
 
                 s = item.getSummaryToDisplay (feedInfo,
                                               new String[]
                                               {
                                                   "text/plain",
                                                   "text/html"
                                               });
                 if (s != null)
                 {
                     out.println();
                     setIndent (++indentLevel);
                     out.println (convert (s));
                     setIndent (--indentLevel);
                 }
 
                 setIndent (--indentLevel);
             }
         }
 
         setIndent (0);
     }
 
     /**
      * Flush any buffered-up output.
      *
      * @throws CurnException  unable to write output
      */
     public void flush() throws CurnException
     {
         out.println ();
         out.println (HORIZONTAL_RULE);
 
         if (displayToolInfo())
         {
             out.println (Version.getFullVersion());
             out.println ("Generated " + new Date().toString());
         }
 
         out.flush();
         out.close();
         out = null;
     }
     
     /**
      * Get the content (i.e., MIME) type for output produced by this output
      * handler.
      *
      * @return the content type
      */
     public String getContentType()
     {
         return "text/plain";
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     private int setIndent (int level)
     {
         StringBuffer buf = new StringBuffer();
 
         for (int i = 0; i < level; i++)
             buf.append ("    ");
 
         out.setPrefix (buf.toString());
 
         return level;
     }
 }
