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
 
 package org.clapper.curn;
 
 import java.io.InputStream;
 
 import org.clapper.curn.parser.RSSChannel;
 
 import org.clapper.util.config.ConfigurationException;
 
 /**
  * This interface defines the methods that must be supported by a class
  * that is to be plugged into <i>curn</i> as an output handler. It is
  * responsible for writing any channel headers, item headers, and item
  * information. It will only be called with items that should be displayed;
  * any channel items that are cached and should be skipped are not handed
  * to the output handler. <i>curn</i> models output in this manner to make
  * it simpler to substitute different kinds of output handlers.
  *
  * @see Curn
  * @see OutputHandlerFactory
  * @see org.clapper.curn.parser.RSSChannel
  * @see org.clapper.curn.parser.RSSItem
  *
  * @version <tt>$Revision$</tt>
  */
 public interface OutputHandler
 {
     /*----------------------------------------------------------------------*\
                                  Constants
     \*----------------------------------------------------------------------*/
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Initializes the output handler for another set of RSS channels.
      *
      * @param config     the parsed <i>curn</i> configuration data. The
      *                   output handler is responsible for retrieving its
      *                   own parameters from the configuration, by calling
      *                   <tt>config.getOutputHandlerSpecificVariables()</tt>
      * @param cfgHandler the <tt>ConfiguredOutputHandler</tt> wrapper
      *                   containing this object; the wrapper has some useful
      *                   metadata, such as the object's configuration section
      *                   name and extra variables.
      *
      * @throws ConfigurationException configuration error
      * @throws CurnException          some other initialization error
      *
      * @see ConfigFile
      * @see ConfiguredOutputHandler
      */
     public void init (ConfigFile config, ConfiguredOutputHandler cfgHandler)
         throws ConfigurationException,
                CurnException;
 
     /**
      * Display the list of <tt>RSSItem</tt> news items to whatever output
      * is defined for the underlying class. Output should be written to the
      * <tt>PrintWriter</tt> that was passed to the {@link #init init()} method.
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
         throws CurnException;
 
     /**
     * Flush any buffered-up output and close the underlying output
     * stream(s), if any. <i>curn</i> calls this method once, after calling
      * <tt>displayChannelItems()</tt> for all channels. If the output
      * handler doesn't need to flush any output, it can simply return
      * without doing anything.
      *
      * @throws CurnException  unable to write output
      */
     public void flush()
         throws CurnException;
 
     /**
      * Get the content (i.e., MIME) type for output produced by this output
      * handler.
      *
      * @return the content type
      *
      * @see #hasGeneratedOutput
      * @see #getGeneratedOutput
      */
     public String getContentType();
 
     /**
      * Get an <tt>InputStream</tt> that can be used to read the output data
      * produced by the handler, if applicable.
      *
      * @return an open input stream, or null if no suitable output was produced
      *
      * @throws CurnException an error occurred
      *
      * @see #hasGeneratedOutput
      * @see #getContentType
      */
     public InputStream getGeneratedOutput()
         throws CurnException;
 
     /**
      * Determine whether this handler has produced any actual output (i.e.,
      * whether {@link #getGeneratedOutput()} will return a non-null
      * <tt>InputStream</tt> if called).
      *
      * @return <tt>true</tt> if the handler has produced output,
      *         <tt>false</tt> if not
      *
      * @see #getGeneratedOutput
      * @see #getContentType
      */
     public boolean hasGeneratedOutput();
 }
