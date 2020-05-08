 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a BSD-style license:
 
   Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
 
   1.  Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.
 
   2.  The end-user documentation included with the redistribution, if any,
       must include the following acknowlegement:
 
         "This product includes software developed by Brian M. Clapper
         (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
         copyright (c) 2004-2006 Brian M. Clapper."
 
       Alternately, this acknowlegement may appear in the software itself,
       if wherever such third-party acknowlegements normally appear.
 
   3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
       nor any of the names of the project contributors may be used to
       endorse or promote products derived from this software without prior
       written permission. For written permission, please contact
       bmc@clapper.org.
 
   4.  Products derived from this software may not be called "clapper.org
       Java Utility Library", nor may "clapper.org" appear in their names
       without prior written permission of Brian M.a Clapper.
 
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
   NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
   THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn.plugins;
 
 import org.clapper.curn.CurnConfig;
 import org.clapper.curn.CurnException;
 import org.clapper.curn.FeedInfo;
 import org.clapper.curn.MainConfigItemPlugIn;
 import org.clapper.curn.FeedConfigItemPlugIn;
 import org.clapper.curn.PostFeedDownloadPlugIn;
 
 import org.clapper.util.classutil.ClassUtil;
 import org.clapper.util.config.ConfigurationException;
 import org.clapper.util.logging.Logger;
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * <p>The <tt>CommonXMLFixupsPlugIn</tt> attempts to fix some common errors
  * in the downloaded, pre-parsed XML in any feed for which it is enabled.
  * There is some XML badness that is surprisingly common across feeds,
  * including (but not limited to):</p>
  *
  * <ul>
  *   <li>Using a "naked" ampersand (&amp;) without escaping it.
  *   <li>Use of nonexistent entities (e.g., &amp;ouml;, &amp;nbsp;)
  *   <li>Improperly formatted entity escapes
  * </ul>
  *
  * <p>This plug-in attempts to fix those problems.</p>
  *
  * <p>This plug-in intercepts the following configuration parameters:</p>
  *
  * <table border="1">
  *   <tr valign="bottom" align="left">
  *     <th>Section</th>
  *     <th>Parameter</th>
  *     <th>Legal Values</th>
  *     <th>Meaning</th>
  *   </tr>
  *   <tr valign="top">
  *     <td><tt>[curn]</tt></td>
  *     <td><tt>CommonXMLFixups</tt></td>
  *     <td><tt>true</tt>, <tt>false</tt></td>
  *     <td>The global setting, which can be used to enable or disable
  *         this plug-in for all feeds (though the plug-in can still be
  *         disabled or enabled on a per-feed basis). If not specified, this
  *         parameter defaults to <tt>false</tt>.</td>
  *   </tr>
  *   <tr valign="top">
  *     <td><tt>[Feed<i>xxx</i>]</tt></td>
  *     <td><tt>CommonXMLFixups</tt></td>
  *     <td><tt>true</tt>, <tt>false</tt></td>
  *     <td>Enables or disables this plug-in for a specific feed. If not
  *         specified, this parameter defaults to the global setting.</td>
  *   </tr>
  * </table>
  *
  * @version <tt>$Revision$</tt>
  */
 public class CommonXMLFixupsPlugIn
     extends AbstractXMLEditPlugIn
     implements MainConfigItemPlugIn,
                FeedConfigItemPlugIn,
                PostFeedDownloadPlugIn
 {
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/
 
     private static final String VAR_COMMON_XML_FIXUPS = "CommonXMLFixups";
 
     /**
      * The table of edit commands.
      */
     private static final String[] EDITS = new String[]
     {
         // Various forms of unescaped ampersands.
 
         "s/ & / \\&amp; /g",
         "s/&$/\\&amp;/g",
         "s/ &amp;amp; / \\&amp; /g",
         "s/&([^;]+)(\\s)/\\&amp;$1$2/g",
 
         // Remove "&nbsp;" and "nbsp;". The first is legal HTML, but not
        // legal XML. The second is illegal. Also have to handle this:
        //
        //        &amp;nbsp
        //
        // That doesn't have to be removed.
 
         "s/&nbsp;/ /g",
        "s/([^&;])nbsp;/$1 /g",
 
         // Non-existent XML entities
 
         "s/&ouml;/\\&#246;/g",
         "s/&mdash;/\\&#8212;/g",

         // For some reason, no one seems to escape "AT&T" properly...

         "s/AT&T/AT\\&amp;T/g"
     };
 
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     /**
      * Feed save data, by feed
      */
     private Map<FeedInfo,Boolean> perFeedEnabledFlag =
         new HashMap<FeedInfo,Boolean>();
 
     /**
      * Whether globally enabled or not.
      */
     private boolean globallyEnabled = false;
 
     /**
      * For log messages
      */
     private static final Logger log = new Logger (CommonXMLFixupsPlugIn.class);
 
     /*----------------------------------------------------------------------* \
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Default constructor (required).
      */
     public CommonXMLFixupsPlugIn()
     {
         // Nothing to do
     }
 
     /*----------------------------------------------------------------------*\
                Public Methods Required by *PlugIn Interfaces
     \*----------------------------------------------------------------------*/
 
     /**
      * Get a displayable name for the plug-in.
      *
      * @return the name
      */
     public String getName()
     {
         return "Common XML Fixups";
     }
 
     /**
      * Get the sort key for this plug-in.
      *
      * @return the sort key string.
      */
     public String getSortKey()
     {
         return ClassUtil.getShortClassName (getClass().getName());
     }
 
     /**
      * Called immediately after <i>curn</i> has read and processed a
      * configuration item in the main [curn] configuration section. All
      * configuration items are passed, one by one, to each loaded plug-in.
      * If a plug-in class is not interested in a particular configuration
      * item, this method should simply return without doing anything. Note
      * that some configuration items may simply be variable assignment;
      * there's no real way to distinguish a variable assignment from a
      * blessed configuration item.
      *
      * @param sectionName  the name of the configuration section where
      *                     the item was found
      * @param paramName    the name of the parameter
      * @param config       the {@link CurnConfig} object
     *
      * @throws CurnException on error
      *
      * @see CurnConfig
      */
     public void runMainConfigItemPlugIn (String     sectionName,
                                          String     paramName,
                                          CurnConfig config)
        throws CurnException
     {
         try
         {
             if (paramName.equals (VAR_COMMON_XML_FIXUPS))
             {
                 globallyEnabled = config.getRequiredBooleanValue (sectionName,
                                                                   paramName);
             }
         }
 
         catch (ConfigurationException ex)
         {
             throw new CurnException (ex);
         }
     }
 
     /**
      * Called immediately after <i>curn</i> has read and processed a
      * configuration item in a "feed" configuration section. All
      * configuration items are passed, one by one, to each loaded plug-in.
      * If a plug-in class is not interested in a particular configuration
      * item, this method should simply return without doing anything. Note
      * that some configuration items may simply be variable assignment;
      * there's no real way to distinguish a variable assignment from a
      * blessed configuration item.
      *
      * @param sectionName  the name of the configuration section where
      *                     the item was found
      * @param paramName    the name of the parameter
      * @param config       the active configuration
      * @param feedInfo     partially complete <tt>FeedInfo</tt> object
      *                     for the feed. The URL is guaranteed to be
      *                     present, but no other fields are.
     *
      * @return <tt>true</tt> to continue processing the feed,
      *         <tt>false</tt> to skip it
      *
      * @throws CurnException on error
      *
      * @see CurnConfig
      * @see FeedInfo
      * @see FeedInfo#getURL
      */
     public boolean runFeedConfigItemPlugIn (String     sectionName,
                                             String     paramName,
                                             CurnConfig config,
                                             FeedInfo   feedInfo)
        throws CurnException
     {
         try
         {
             if (paramName.equals (VAR_COMMON_XML_FIXUPS))
             {
                 boolean flag = config.getRequiredBooleanValue (sectionName,
                                                                paramName);
                 perFeedEnabledFlag.put (feedInfo, flag);
                 log.debug ("[" + sectionName + "]: " + paramName + "=" +
                            flag);
             }
 
             return true;
         }
 
         catch (ConfigurationException ex)
         {
             throw new CurnException (ex);
         }
     }
 
 
     /**
      * Called immediately after a feed is downloaded. This method can
      * return <tt>false</tt> to signal <i>curn</i> that the feed should be
      * skipped. For instance, a plug-in that filters on the unparsed XML
      * feed content could use this method to weed out non-matching feeds
      * before they are downloaded.
      *
      * @param feedInfo      the {@link FeedInfo} object for the feed that
      *                      has been downloaded
     * @param feedDataFile  the file containing the downloaded, unparsed feed
      *                      XML. <b><i>curn</i> may delete this file after all
      *                      plug-ins are notified!</b>
      * @param encoding      the encoding used to store the data in the file,
      *                      or null for the default
      *
      * @return <tt>true</tt> if <i>curn</i> should continue to process the
      *         feed, <tt>false</tt> to skip the feed. A return value of
      *         <tt>false</tt> aborts all further processing on the feed.
      *         In particular, <i>curn</i> will not pass the feed along to
      *         other plug-ins that have yet to be notified of this event.
      *
      * @throws CurnException on error
      *
      * @see FeedInfo
      */
     public boolean runPostFeedDownloadPlugIn (FeedInfo feedInfo,
                                               File     feedDataFile,
                                               String   encoding)
        throws CurnException
     {
         Boolean enabledBoxed = perFeedEnabledFlag.get (feedInfo);
         boolean enabled = globallyEnabled;
 
         if (enabledBoxed != null)
             enabled = enabledBoxed;
 
         if (enabled)
             editXML (feedInfo, feedDataFile, encoding, Arrays.asList (EDITS));
 
         return true;
     }
 
     /*----------------------------------------------------------------------*\
                              Protected Methods
     \*----------------------------------------------------------------------*/
 
     protected Logger getLogger()
     {
         return log;
     }
 }
