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
 
 package org.clapper.curn.parser;
 
 import org.clapper.util.text.TextUtil;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * This abstract class defines a simplified view of an RSS item, providing
  * only the methods necessary for <i>curn</i> to work. <i>curn</i> uses the
  * {@link RSSParserFactory} class to get a specific implementation of
  * <tt>RSSParser</tt>, which returns <tt>RSSChannel</tt>-conforming objects
  * that, in turn, return item objects that subclass <tt>RSSItem</tt>. This
  * strategy isolates the bulk of the code from the underlying RSS parser,
  * making it easier to substitute different parsers as more of them become
  * available. <tt>RSSItem</tt>. This strategy isolates the bulk of the code
  * from the underlying RSS parser, making it easier to substitute different
  * parsers as more of them become available.
  *
  * @see RSSParserFactory
  * @see RSSParser
  * @see RSSChannel
  *
  * @version <tt>$Revision$</tt>
  */
 public abstract class RSSItem
     extends RSSElement
     implements Cloneable, Comparable
 {
     /*----------------------------------------------------------------------*\
                                  Constants
     \*----------------------------------------------------------------------*/
 
     /**
      * Constant defining the pseudo-MIME type to use for default content.
      */
     public static final String DEFAULT_CONTENT_TYPE = "*";
 
     /**
      * Unlimited summary size
      */
     public static final int NO_SUMMARY_LIMIT = 0;
 
     /*----------------------------------------------------------------------*\
                            Private Instance Data
     \*----------------------------------------------------------------------*/
 
     private Map<String,String>   contentMap = new HashMap<String,String>();
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Clone this channel. This method simply calls the type-safe
      * {@link #makeCopy} method. The clone is a deep-clone (i.e., the items
      * are cloned, too).
      *
      * @return the cloned <tt>RSSChannel</tt>
      *
      * @throws CloneNotSupportedException  doesn't, actually, but the
      *                                     <tt>Cloneable</tt> interface
      *                                     requires that this exception
      *                                     be declared
      *
      * @see #makeCopy
      */
     public Object clone()
         throws CloneNotSupportedException
     {
         return makeCopy (getParentChannel());
     }
 
     /**
      * Make a deep copy of this <tt>RSSItem</tt> object.
      *
      * @param parentChannel  the parent channel to assign to the new instance
      *
      * @return the copy
      */
     public RSSItem makeCopy (RSSChannel parentChannel)
     {
         RSSItem copy = newInstance (parentChannel);
 
         for (String key : this.contentMap.keySet())
             copy.contentMap.put (key, this.contentMap.get (key));
 
         copy.setTitle (this.getTitle());
         copy.setSummary (this.getSummary());
         copy.setLinks (this.getLinks());
         copy.setCategories (this.getCategories());
         copy.setPublicationDate (this.getPublicationDate());
         copy.setID (this.getID());
 
         Collection<String> authors = this.getAuthors();
         if (authors != null)
         {
             for (String author : authors)
                 copy.addAuthor (author);
         }
 
         return copy;
     }
 
     /**
      * Get the item's content, if available. Some feed types (e.g., Atom)
      * support multiple content sections, each with its own MIME type; the
      * <tt>mimeType</tt> parameter specifies the caller's desired MIME
      * type.
      *
      * @param mimeType  the desired MIME type
      *
      * @return the content (or the default content), or null if no content
      *         of the desired MIME type is available
      *
      * @see #clearContent
      * @see #getFirstContentOfType
      * @see #setContent
      */
     public String getContent (String mimeType)
     {
         String result = null;
 
         result = (String) contentMap.get (mimeType);
         if (result == null)
             result = (String) contentMap.get (DEFAULT_CONTENT_TYPE);
 
         return result;
     }
 
     /**
      * Get the first content item that matches one of a list of MIME types.
      *
      * @param mimeTypes  an array of MIME types to match, in order
      *
      * @return the first matching content string, or null if none was found.
      *         Returns the default content (if set), if there's no exact
      *         match.
      *
      * @see #getContent
      * @see #clearContent
      * @see #setContent
      */
     public final String getFirstContentOfType (String[] mimeTypes)
     {
         String result = null;
 
         for (int i = 0; i < mimeTypes.length; i++)
         {
             result = (String) contentMap.get (mimeTypes[i]);
             if (! TextUtil.stringIsEmpty (result))
                 break;
         }
 
         if (result == null)
             result = (String) contentMap.get (DEFAULT_CONTENT_TYPE);
 
         return result;
     }
 
     /**
      * Set the content for a specific MIME type. If the
      * <tt>isDefault</tt> flag is <tt>true</tt>, then this content
      * is served up as the default whenever content for a specific MIME type
      * is requested but isn't available.
      *
      * @param content    the content string
      * @param mimeType   the MIME type to associate with the content
      *
      * @see #getContent
      * @see #getFirstContentOfType
      * @see #clearContent
      */
     public void setContent (String content, String mimeType)
     {
         contentMap.put (mimeType, content);
     }   
 
     /**
      * Clear the stored content for all MIME types, without clearing any
      * other fields. (In particular, the summary is not cleared.)
      *
      * @see #getContent
      * @see #getFirstContentOfType
      * @see #setContent
      */
     public void clearContent()
     {
         contentMap.clear();
     }
 
     /**
      * Utility method to get the summary to display for an
     * <tt>RSSItem</tt>. Strips HTML by default. (Use the
     * {@link #getSummaryToDisplay(boolean,int,String[],boolean) other version}
     * of this method to control the HTML-stripping behavior.
      *
      * @param mimeTypes  desired MIME types; used only if no summary is
      *                   available, and the content field should be used
      *
      * @return the summary string to use, or null if unavailable
      *
      * @see #getSummary
      * @see #getFirstContentOfType
      */
     public String getSummaryToDisplay (String[] mimeTypes)
     {
         String summary = getSummary();
 
         if (TextUtil.stringIsEmpty (summary))
             summary = null;
 
         if (summary == null)
         {
             assert (mimeTypes != null);
             summary = getFirstContentOfType (mimeTypes);
             if (TextUtil.stringIsEmpty (summary))
                 summary = null;
         }
 
         return summary;
     }
 
     /**
      * Compare two items for order. The channels are ordered first by
      * publication date (if any), then by title, then by unique ID,
      * then by hash code (if all else is equal).
      *
      * @param other  the other object
      *
      * @return negative number: this item is less than <tt>other</tt>;<br>
      *         0: this item is equivalent to <tt>other</tt><br>
      *         positive unmber: this item is greater than <tt>other</tt>
      */
     public int compareTo (Object other)
     {
         RSSItem otherItem = (RSSItem) other;
 
         Date otherDate = otherItem.getPublicationDate();
         Date thisDate  = this.getPublicationDate();
         Date now       = new Date();
 
         if (otherDate == null)
             otherDate = now;
 
         if (thisDate == null)
             thisDate = now;
 
         int cmp = thisDate.compareTo (otherDate);
         if (cmp == 0)
         {
             String otherTitle = otherItem.getTitle();
             String thisTitle  = this.getTitle();
 
             if (otherTitle == null)
                 otherTitle = "";
 
             if (thisTitle == null)
                 thisTitle = "";
 
             if ((cmp = thisTitle.compareTo (otherTitle)) == 0)
             {
                 String otherID = otherItem.getID();
                 String thisID  = this.getID();
 
                 if (otherID == null)
                     otherID = "";
 
                 if (thisID == null)
                     thisID = "";
 
                 if ((cmp = thisID.compareTo (otherID)) == 0)
                     cmp = this.hashCode() - other.hashCode();
             }
         }
 
         return cmp;
     }
 
     /**
      * Return the string value of the item (which, right now, is its
      * title).
      *
      * @return the title
      */
     public String toString()
     {
         String title = getTitle();
 
         return (title == null) ? "" : title;
     }
 
     /*----------------------------------------------------------------------*\
                           Public Abstract Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Create a new, empty instance of the underlying concrete
      * class.
      *
      * @param channel  the parent channel
      *
      * @return the new instance
      */
     public abstract RSSItem newInstance (RSSChannel channel);
 
     /**
      * Get the parent channel
      *
      * @return the parent channel
      */
     public abstract RSSChannel getParentChannel();
 
     /**
      * Get the item's title
      *
      * @return the item's title, or null if there isn't one
      *
      * @see #setTitle
      */
     public abstract String getTitle();
 
     /**
      * Set the item's title
      *
      * @param newTitle  the item's title, or null if there isn't one
      *
      * @see #getTitle
      */
     public abstract void setTitle (String newTitle);
 
     /**
      * Get the item's summary (also sometimes called the description or
      * synopsis).
      *
      * @return the summary, or null if not available
      *
      * @see #setSummary
      * @see #getSummaryToDisplay
      */
     public abstract String getSummary();
 
     /**
      * Set the item's summary (also sometimes called the description or
      * synopsis).
      *
      * @param newSummary the summary, or null if not available
      *
      * @see #getSummary
      */
     public abstract void setSummary (String newSummary);
 
     /**
      * Get the item's author list.
      *
      * @return the authors, or null (or an empty <tt>Collection</tt>) if
      *         not available
      *
      * @see #addAuthor
      * @see #clearAuthors
      * @see #setAuthors
      */
     public abstract Collection<String> getAuthors();
 
     /**
      * Add to the item's author list.
      *
      * @param author  another author string to add
      *
      * @see #getAuthors
      * @see #clearAuthors
      * @see #setAuthors
      */
     public abstract void addAuthor (String author);
 
     /**
      * Clear the authors list.
      *
      * @see #getAuthors
      * @see #addAuthor
      * @see #setAuthors
      */
     public abstract void clearAuthors();
 
     /**
      * Get the item's published links.
      *
      * @return the collection of links, or an empty collection
      *
      * @see #setLinks
      */
     public abstract Collection<RSSLink> getLinks();
 
     /**
      * Set the item's published links.
      *
      * @param links the collection of links, or an empty collection (or null)
      *
      * @see #getLinks
      */
     public abstract void setLinks (Collection<RSSLink> links);
 
     /**
      * Get the categories the item belongs to.
      *
      * @return a <tt>Collection</tt> of category strings (<tt>String</tt>
      *         objects) or null if not applicable
      *
      * @see #setCategories
      */
     public abstract Collection<String> getCategories();
 
     /**
      * Set the categories the item belongs to.
      *
      * @param categories a <tt>Collection</tt> of category strings
      *                   or null if not applicable
      *
      * @see #getCategories
      */
     public abstract void setCategories (Collection<String> categories);
 
     /**
      * Get the item's publication date.
      *
      * @return the date, or null if not available
      *
      * @see #getPublicationDate
      */
     public abstract Date getPublicationDate();
 
     /**
      * Set the item's publication date.
      *
      * @see #getPublicationDate
      */
     public abstract void setPublicationDate (Date date);
 
     /**
      * Get the item's ID field, if any.
      *
      * @return the ID field, or null if not set
      *
      * @see #setID
      */
     public abstract String getID();
 
     /**
      * Set the item's ID field, if any.
      *
      * @param id the ID field, or null
      */
     public abstract void setID (String id);
 }
