 /*---------------------------------------------------------------------------*\
   $Id$
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn.htmloutput;
 
 import org.clapper.curn.OutputHandler;
 import org.clapper.curn.FeedException;
 import org.clapper.curn.Util;
 import org.clapper.curn.Version;
 import org.clapper.curn.FeedInfo;
 import org.clapper.curn.ConfigFile;
 import org.clapper.curn.parser.RSSChannel;
 import org.clapper.curn.parser.RSSItem;
 
 import org.clapper.util.text.Unicode;
 import org.clapper.util.text.TextUtils;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import java.util.Date;
 import java.util.Collection;
 import java.util.Iterator;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import java.net.URL;
 
 import org.w3c.dom.Node;
 
 import org.w3c.dom.html.HTMLTableRowElement;
 import org.w3c.dom.html.HTMLTableCellElement;
 import org.w3c.dom.html.HTMLAnchorElement;
 
 /**
  * Provides an output handler that produces HTML output.
  *
  * @see OutputHandler
  * @see org.clapper.curn.curn
  * @see org.clapper.curn.parser.RSSChannel
  *
  * @version <tt>$Revision$</tt>
  */
 public class HTMLOutputHandler implements OutputHandler
 {
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/
 
     private static final DateFormat OUTPUT_DATE_FORMAT =
                              new SimpleDateFormat ("dd MMM, yyyy, HH:mm:ss");
 
     /*----------------------------------------------------------------------*\
                            Private Instance Data
     \*----------------------------------------------------------------------*/
 
     private PrintWriter           out                 = null;
     private RSSOutputHTML         doc                 = null;
     private String                oddItemRowClass     = null;
     private String                oddChannelClass     = null;
     private int                   rowCount            = 0;
     private int                   channelCount        = 0;
     private ConfigFile   config              = null;
     private HTMLTableRowElement   channelRow          = null;
     private HTMLTableRowElement   channelSeparatorRow = null;
     private Node                  channelRowParent    = null;
     private HTMLAnchorElement     itemAnchor          = null;
     private HTMLTableCellElement  itemTitleTD         = null;
     private HTMLTableCellElement  itemDescTD          = null;
     private HTMLTableCellElement  channelTD           = null;
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Construct a new <tt>HTMLOutputHandler</tt>.
      */
     public HTMLOutputHandler()
     {
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Initializes the output handler for another set of RSS channels.
      *
      * @param writer  the <tt>PrintWriter</tt> where the handler should send
      *                output
      * @param config  the parsed <i>curn</i> configuration data
      *
      * @throws FeedException  initialization error
      */
     public void init (PrintWriter         writer,
                       ConfigFile config)
         throws FeedException
     {
         this.doc                 = new RSSOutputHTML();
         this.config              = config;
         this.out                 = writer;
         this.oddChannelClass     = doc.getElementChannelTD().getClassName();
         this.oddItemRowClass     = doc.getElementItemTitleTD().getClassName();
         this.channelRow          = doc.getElementChannelRow();
         this.channelSeparatorRow = doc.getElementChannelSeparatorRow();
         this.channelRowParent    = channelRow.getParentNode();
         this.itemAnchor          = doc.getElementItemAnchor();
         this.itemTitleTD         = doc.getElementItemTitleTD();
         this.itemDescTD          = doc.getElementItemDescTD();
         this.channelTD           = doc.getElementChannelTD();
     }
 
     /**
      * Display the list of <tt>RSSItem</tt> news items to whatever output
      * is defined for the underlying class.
      *
      * @param channel  The channel containing the items to emit. The method
      *                 should emit all the items in the channel; the caller
      *                 is responsible for clearing out any items that should
      *                 not be seen.
      * @param feedInfo Information about the feed, from the configuration
      *
      * @throws FeedException  unable to write output
      */
     public void displayChannel (RSSChannel  channel,
                                 FeedInfo    feedInfo)
         throws FeedException
     {
         Collection items = channel.getItems();
 
         if (items.size() == 0)
             return;
 
         int       i = 0;
         Iterator  it;
         Date      date;
 
         channelCount++;
 
         // Insert separator row first.
 
         channelRowParent.insertBefore (channelSeparatorRow.cloneNode (true),
                                        channelSeparatorRow);
         // Do the rows of output.
 
         doc.getElementChannelDate().removeAttribute ("id");
         doc.getElementItemDate().removeAttribute ("id");
 
         for (i = 0, it = items.iterator(); it.hasNext(); i++, rowCount++)
         {
             RSSItem item = (RSSItem) it.next();
 
             if (i == 0)
             {
                 // First row in channel has channel title and link.
 
                 doc.setTextChannelTitle (channel.getTitle());
 
                 date = null;
                 if (config.showDates())
                     date = channel.getPublicationDate();
                 if (date != null)
                     doc.setTextChannelDate (OUTPUT_DATE_FORMAT.format (date));
                 else
                     doc.setTextChannelDate ("");
 
                 itemAnchor.setHref (item.getLink().toExternalForm());
             }
 
             else
             {
                 doc.setTextChannelTitle ("");
                 doc.setTextChannelDate ("");
                 itemAnchor.setHref ("");
             }
 
             String title = item.getTitle();
             doc.setTextItemTitle ((title == null) ? "(No Title)" : title);
 
             String desc = null;
             if (! feedInfo.summarizeOnly())
             {
                 desc = item.getSummary();
                 if (TextUtils.stringIsEmpty (desc))
                 {
                     // Hack for feeds that have no summary but have
                     // content. If the content is small enough, use it as
                     // the summary.
 
                     desc = item.getFirstContentOfType (new String[]
                                                        {
                                                            "text/plain",
                                                            "text/html"
                                                        });
                     if (! TextUtils.stringIsEmpty (desc))
                     {
                         desc = desc.trim();
                         if (desc.length() > CONTENT_AS_SUMMARY_MAXSIZE)
                             desc = null;
                     }
                 }
             }
 
             else
             {
                if (TextUtils.stringIsEmpty (desc))
                    desc = null;
                else
                    desc = desc.trim();
             }
 
             if (desc == null)
                 desc = String.valueOf (Unicode.NBSP);
 
             doc.setTextItemDescription (desc);
 
             itemAnchor.setHref (item.getLink().toExternalForm());
 
             date = null;
             if (config.showDates())
                 date = item.getPublicationDate();
             if (date != null)
                 doc.setTextItemDate (OUTPUT_DATE_FORMAT.format (date));
             else
                 doc.setTextItemDate ("");
 
             itemTitleTD.removeAttribute ("class");
             itemDescTD.removeAttribute ("class");
 
             if ((rowCount % 2) == 1)
             {
                 // Want to use the "odd row" class to distinguish the
                 // rows. For the description, though, only do that if
                 // if's not empty.
 
                 itemTitleTD.setAttribute ("class", oddItemRowClass);
 
                 if (desc != null)
                     itemDescTD.setAttribute ("class", oddItemRowClass);
             }
 
             if ((channelCount % 2) == 1)
                 channelTD.setClassName (oddChannelClass);
             else
                 channelTD.setClassName ("");
 
             channelRowParent.insertBefore (channelRow.cloneNode (true),
                                            channelSeparatorRow);
         }
 
     }
     
     /**
      * Flush any buffered-up output. <i>curn</i> calls this method
      * once, after calling <tt>displayChannelItems()</tt> for all channels.
      *
      * @throws FeedException  unable to write output
      */
     public void flush() throws FeedException
     {
         // Remove the cloneable row.
 
         removeElement (doc.getElementChannelRow());
 
         // Add configuration info, if available.
 
         doc.setTextVersion (Version.VERSION);
 
         URL configFileURL = config.getConfigurationFileURL();
         if (configFileURL == null)
             removeElement (doc.getElementConfigFileRow());
         else
             doc.setTextConfigURL (configFileURL.toString());
 
         // Write the document.
 
         out.print (doc.toDocument());
         out.flush();
 
         // Kill the document.
 
         doc = null;
     }
 
     /**
      * Get the content (i.e., MIME) type for output produced by this output
      * handler.
      *
      * @return the content type
      */
     public String getContentType()
     {
         return "text/html";
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Remove an element from the document.
      *
      * @param element the <tt>Node</tt> representing the element in the DOM
      */
     private void removeElement (Node element)
     {
         Node parentNode = element.getParentNode();
 
         if (parentNode != null)
             parentNode.removeChild (element);
     }
 }
