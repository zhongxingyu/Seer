 /*---------------------------------------------------------------------------*\
  $Id$
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.clapper.util.config.ConfigurationException;
 import org.clapper.util.io.IOExceptionExt;
 import org.clapper.util.logging.Logger;
 import org.clapper.util.text.TextUtil;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 /**
  * @version <tt>$Revision$</tt>
  */
 public class XMLDataPersister extends DataPersister
 {
     /*----------------------------------------------------------------------*\
                                Private Constants
     \*----------------------------------------------------------------------*/
 
     private static final int DEF_TOTAL_CACHE_BACKUPS = 0;
 
     private static final String VAR_CACHE_FILE        = "CacheFile";
     private static final String VAR_TOTAL_CACHE_BACKUPS = "TotalCacheBackups";
 
     /**
      * Constants for the old XML format
      */
     private static final String OLD_XML_ROOT_ELEMENT           = "curn_cache";
     private static final String OLD_XML_ENTRY_ELEMENT          = "cache_entry";
     private static final String OLD_XML_ENTRY_TIMESTAMP_ATTR   = "timestamp";
     private static final String OLD_XML_ENTRY_CHANNEL_URL_ATTR = "channel_URL";
     private static final String OLD_XML_ENTRY_ENTRY_URL_ATTR   = "entry_URL";
     private static final String OLD_XML_ENTRY_ENTRY_ID_ATTR    = "entry_ID";
     private static final String OLD_XML_ENTRY_PUB_DATE_ATTR    = "pub_date";
 
     /**
      * Constants for the new XML format.
      */
     private static final String XML_ROOT_ELEMENT          = "curn-data";
     private static final String XML_FEED_ELEMENT          = "feed";
     private static final String XML_ITEM_ELEMENT          = "item";
     private static final String XML_ITEM_METADATA_ELEMENT = "item-metadata";
     private static final String XML_FEED_METADATA_ELEMENT = "feed-metadata";
     private static final String XML_EXTRA_METADATA_ELEMENT = "extra-metadata";
     private static final String XML_METADATA_NAMESPACE_ATTR = "namespace";
     private static final String XML_METADATUM_ELEMENT     = "metadatum";
     private static final String XML_METADATUM_NAME_ATTR   = "name";
     private static final String XML_METADATUM_VALUE_ATTR  = "value";
     private static final String XML_TIMESTAMP_ATTR        = "timestamp";
     private static final String XML_URL_ATTR              = "url";
     private static final String XML_ID_ATTR               = "id";
     private static final String XML_PUB_DATE_ATTR         = "pub-date";
 
     /*----------------------------------------------------------------------*\
                              Private Instance Data
     \*----------------------------------------------------------------------*/
 
     private int totalCacheBackups = DEF_TOTAL_CACHE_BACKUPS;
     private File metadataFile = null;
 
     /**
      * Root XML element, used while saving.
      */
     private Element rootElementForSaving = null;
 
     /**
      * For logging
      */
     private static final Logger log = new Logger(XMLDataPersister.class);
 
    /*----------------------------------------------------------------------*\
                                    Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Creates a new instance of XMLDataPersister
      */
     public XMLDataPersister()
     {
     }
 
     /*----------------------------------------------------------------------*\
                                 Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Called when the <tt>DataPersister</tt> is first instantiated. Useful
      * for retrieving configuration values, etc.
      *
      * @param curnConfig  the configuration
      * @throws CurnException on error
      */
     public void init(CurnConfig curnConfig) throws CurnException
     {
         try
         {
             String cacheFileName =
                 curnConfig.getOptionalStringValue(CurnConfig.MAIN_SECTION,
                                                   VAR_CACHE_FILE,
                                                   null);
             if (cacheFileName != null)
             {
                 metadataFile = CurnUtil.mapConfiguredPathName(cacheFileName);
                 if (metadataFile.isDirectory())
                 {
                     throw new CurnException
                         (Constants.BUNDLE_NAME, "XMLDataPersister.cacheIsDir",
                          "Configured XML cache file \"{0}\" is a directory.",
                          new Object[] {metadataFile.getPath()});
                 }
             }
 
             totalCacheBackups =
                 curnConfig.getOptionalCardinalValue(CurnConfig.MAIN_SECTION,
                                                     VAR_TOTAL_CACHE_BACKUPS,
                                                     DEF_TOTAL_CACHE_BACKUPS);
         }
 
         catch (ConfigurationException ex)
         {
             throw new CurnException(ex);
         }
     }
 
     /*----------------------------------------------------------------------*\
                                Protected Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Determine whether the data persister subclass is enabled or not (i.e.,
      * whether or not metadata is to be loaded and saved). The configuration
      * usually determines whether or not the data persister is enabled.
      *
      * @return <tt>true</tt> if enabled, <tt>false</tt> if disabled.
      */
     protected boolean isEnabled()
     {
         return metadataFile != null;
     }
 
     /**
      * Called at the beginning of the actual save operation to initialize
      * the save, etc.
      *
      * @throws CurnException on error
      */
     protected void startSaveOperation() throws CurnException
     {
         assert(isEnabled());
 
         log.debug("Saving feed metadata to \"" + metadataFile.getPath() +
                   "\". Total backups=" + totalCacheBackups);
 
         // Create the DOM's root element.
 
         rootElementForSaving = new Element(XML_ROOT_ELEMENT);
         rootElementForSaving.setAttribute
             (XML_TIMESTAMP_ATTR, String.valueOf(System.currentTimeMillis()));
      }
 
     /**
      * Called at the end of the actual save operation to flush files, clean
      * up, etc.
      *
      * @throws CurnException on error
      */
     protected void endSaveOperation() throws CurnException
     {
         try
         {
             Document document = new Document(rootElementForSaving);
             Format outputFormat = Format.getPrettyFormat();
             outputFormat.setLineSeparator(System.getProperty("line.separator"));
             XMLOutputter xmlOut = new XMLOutputter(outputFormat);
 
             // Open the cache file. For the cache file, the index
             // marker goes at the end of the file (since the extension
             // doesn't matter as much). This allows the file names to
             // sort better in a directory listing.
 
             Writer cacheOut = CurnUtil.openOutputFile
                                          (metadataFile,
                                           null,
                                           CurnUtil.IndexMarker.AFTER_EXTENSION,
                                           totalCacheBackups);
 
             //xmlOut.output(document, new XMLWriter(cacheOut));
             xmlOut.output(document, cacheOut);
         }
 
         catch (IOException ex)
         {
             throw new CurnException("Failed to write XML cache file \"" +
                                     metadataFile.getPath() + "\"",
                                     ex);
         }
 
         catch (IOExceptionExt ex)
         {
             throw new CurnException("Failed to write XML cache file \"" +
                                     metadataFile.getPath() + "\"",
                                     ex);
         }
     }
 
     /**
      * Save the data for one feed, including the items.
      *
      * @param feedData  the feed data to be saved
      *
      * @throws CurnException on error
      */
     protected void saveFeedData(PersistentFeedData feedData) throws CurnException
     {
         FeedCacheEntry feedCacheData = feedData.getFeedCacheEntry();

        if (feedCacheData == null)
            return;

         URL channelURL = feedCacheData.getChannelURL();
 
         Element channelElement = new Element(XML_FEED_ELEMENT);
         channelElement.setAttribute(XML_URL_ATTR, channelURL.toString());
         channelElement.setAttribute
             (XML_TIMESTAMP_ATTR, String.valueOf(feedCacheData.getTimestamp()));
         channelElement.setAttribute(XML_ID_ATTR, feedCacheData.getUniqueID());
         rootElementForSaving.addContent(channelElement);
 
         // Now the feed metadata
 
         fillInMetadata(feedData.getFeedMetadata(),
                        XML_FEED_METADATA_ELEMENT,
                        channelElement);
 
         // Okay, the feed element has been built. Time to add the items to it.
 
         for (PersistentFeedItemData itemData : feedData.getPersistentFeedItems())
         {
             Element itemElement = new Element(XML_ITEM_ELEMENT);
             channelElement.addContent(itemElement);
 
             FeedCacheEntry itemCacheData = itemData.getFeedCacheEntry();
             itemElement.setAttribute
                 (XML_TIMESTAMP_ATTR,
                  String.valueOf(itemCacheData.getTimestamp()));
             itemElement.setAttribute(XML_ID_ATTR, itemCacheData.getUniqueID());
             itemElement.setAttribute(XML_URL_ATTR,
                                      itemCacheData.getEntryURL().toString());
 
             // Only write the publication date if it's present.
 
             Date pubDate = itemCacheData.getPublicationDate();
             if (pubDate != null)
             {
                 itemElement.setAttribute(XML_PUB_DATE_ATTR,
                                          String.valueOf(pubDate.getTime()));
             }
 
             // Fill in the metadata for the item.
 
             fillInMetadata(itemData.getItemMetadata(),
                            XML_ITEM_METADATA_ELEMENT,
                            itemElement);
         }
     }
 
     /**
      * Save any extra metadata (i.e., metadata that isn't attached to a
      * specific feed or a specific item).
      *
      * @param metadata the collection of metadata items
      *
      * @throws CurnException on error
      */
     protected void
     saveExtraMetadata(Collection<PersistentMetadataGroup> metadata)
         throws CurnException
     {
         fillInMetadata(metadata,
                        XML_EXTRA_METADATA_ELEMENT,
                        rootElementForSaving);
     }
 
     /**
      * Called at the beginning of the load operation to initialize
      * the load.
      *
      * @throws CurnException on error
      */
     protected void startLoadOperation()
         throws CurnException
     {
          assert(isEnabled());
          log.debug("Starting load of XML curn data.");
    }
 
     /**
      * Called at the end of the load operation to close files, clean
      * up, etc.
      *
      * @throws CurnException on error
      */
     protected void endLoadOperation()
         throws CurnException
     {
          log.debug("Load of XML curn data complete.");
     }
 
     /**
      * The actual load method; only called if the object is enabled.
      *
      * @param loadedDataHandler object to receive data as it's loaded
      *
      * @throws CurnException on error
      */
     protected void doLoad(LoadedDataHandler loadedDataHandler)
         throws CurnException
     {
         String filePath = metadataFile.getPath();
         if (metadataFile.exists())
         {
             log.debug("Reading feed metadata from \"" + filePath + "\"");
 
             // First, parse the XML file into a DOM.
 
             SAXBuilder builder = new SAXBuilder();
             Document document;
 
             log.info("Attempting to parse \"" + filePath + "\" as XML.");
             try
             {
                 document = builder.build(metadataFile);
             }
 
             catch (Throwable ex)
             {
                 log.error(ex);
                 throw new CurnException(ex);
             }
 
             log.debug("XML parse succeeded.");
 
             // Get the top-level element and verify that it's the one
             // we want.
 
             Element root = document.getRootElement();
             String rootTagName = root.getName();
 
             if (rootTagName.equals(OLD_XML_ROOT_ELEMENT))
             {
                 log.debug("Reading old-style <" + OLD_XML_ROOT_ELEMENT +
                           "> cache file.");
                 readOldXMLCache(document, filePath, loadedDataHandler);
             }
 
             else if (rootTagName.equals(XML_ROOT_ELEMENT))
             {
                 log.debug("Reading new-style <" + XML_ROOT_ELEMENT +
                           "> metadata file.");
                 readNewXMLMetaData(document, loadedDataHandler);
             }
 
             else
             {
                 throw new CurnException
                     (Constants.BUNDLE_NAME,
                      "XMLDataPersister.nonCacheXML",
                      "File \"{0}\" is not a curn XML metadata file. The root " +
                      "XML element is <{1}>, not the expected <{2}> or <{3}>",
                      new Object[]
                      {
                          filePath,
                          rootTagName,
                          OLD_XML_ROOT_ELEMENT,
                          XML_ROOT_ELEMENT
                      });
             }
         }
     }
 
     /*----------------------------------------------------------------------*\
                                 Private Methods
     \*----------------------------------------------------------------------*/
 
     private void fillInMetadata(Collection<PersistentMetadataGroup> metadata,
                                 String elementName,
                                 Element parentElement)
     {
         for (PersistentMetadataGroup metadataGroup : metadata)
         {
             Element metadataElement = new Element(elementName);
             metadataElement.setAttribute(XML_METADATA_NAMESPACE_ATTR,
                                          metadataGroup.getNamespace());
             parentElement.addContent(metadataElement);
 
             Map<String,String> nameValuePairs = metadataGroup.getMetadata();
             for (Map.Entry<String,String> nameValuePair : nameValuePairs.entrySet())
             {
                 Element metadatumElement = new Element(XML_METADATUM_ELEMENT);
                 metadatumElement.setAttribute(XML_METADATUM_NAME_ATTR,
                                               nameValuePair.getKey());
                 metadatumElement.setAttribute(XML_METADATUM_VALUE_ATTR,
                                               nameValuePair.getValue());
                 metadataElement.addContent(metadatumElement);
             }
         }
     }
 
     /**
      * Attempt to parse an old-style XML cache. This method will go away
      * soon.
      *
      * @param document          the parsed XML document
      * @param filePath          the path to the file, for errors
      * @param loadedDataHandler the callback to invoke with loaded data
      *
      * @throws CurnException on error
      */
     private void readOldXMLCache(final Document          document,
                                  final String            filePath,
                                  final LoadedDataHandler loadedDataHandler)
         throws CurnException
     {
         // Get the top-level element and verify that it's the one
         // we want.
 
         Element root = document.getRootElement();
         String rootTagName = root.getName();
         assert(rootTagName.equals(OLD_XML_ROOT_ELEMENT));
 
         // Okay, it's a curn cache. Start traversing the child nodes,
         // parsing each cache entry.
 
         Map<URL,PersistentFeedData> loadedData =
             new HashMap<URL,PersistentFeedData>();
 
         List<?> childNodes = root.getChildren();
         for (Iterator<?> it = childNodes.iterator(); it.hasNext(); )
         {
             Element childNode = (Element) it.next();
 
             // Skip non-element nodes (like text).
 
             String nodeName = childNode.getName();
             if (! nodeName.equals(OLD_XML_ENTRY_ELEMENT))
             {
                 log.warn("Skipping unexpected XML element <" +
                          nodeName + "> in curn XML cache file \"" +
                          filePath + "\".");
                 continue;
             }
 
             try
             {
                 FeedCacheEntry entry = parseOldXMLCacheEntry(childNode);
                 URL feedURL = entry.getChannelURL();
                 PersistentFeedData feedData = loadedData.get(feedURL);
                 log.debug("readOldXMLCache: read entry " + entry.getEntryURL());
                 if (feedData == null)
                 {
                     feedData = new PersistentFeedData();
                     loadedData.put(feedURL, feedData);
                 }
 
                 if (entry.isChannelEntry())
                 {
                     feedData.setFeedCacheEntry(entry);
                 }
 
                 else
                 {
                     feedData.addPersistentFeedItem
                         (new PersistentFeedItemData(entry));
                 }
             }
 
             catch (CurnException ex)
             {
                 // Bad entry. Log the error, but move on.
 
                 log.error("Error parsing feed cache entry", ex);
             }
         }
 
         for (PersistentFeedData feedData : loadedData.values())
             loadedDataHandler.feedLoaded(feedData);
     }
 
     /**
      * Parse an old-style XML feed cache entry. This method will go away
      * soon.
      *
      * @param element  the XML element for the feed cache entry
      *
      * @return the FeedCacheEntry
      *
      * @throws CurnException on error
      */
     private FeedCacheEntry parseOldXMLCacheEntry(final Element element)
         throws CurnException
     {
         FeedCacheEntry result = null;
 
         // Parse out the attributes.
 
         String entryID =
             getRequiredXMLAttribute(element, OLD_XML_ENTRY_ENTRY_ID_ATTR);
         String sChannelURL =
             getRequiredXMLAttribute(element, OLD_XML_ENTRY_CHANNEL_URL_ATTR);
         String sEntryURL =
             getRequiredXMLAttribute(element, OLD_XML_ENTRY_ENTRY_URL_ATTR);
         String sTimestamp =
             getRequiredXMLAttribute(element, OLD_XML_ENTRY_TIMESTAMP_ATTR);
         String sPubDate =
             getOptionalXMLAttribute(element, OLD_XML_ENTRY_PUB_DATE_ATTR, null);
 
 
         if ((entryID != null) &&
             (sChannelURL != null) &&
             (sEntryURL != null) &&
             (sTimestamp != null))
         {
             // Parse the timestamp.
 
             long timestamp = 0;
             try
             {
                 timestamp = Long.parseLong(sTimestamp);
             }
 
             catch (NumberFormatException ex)
             {
                 throw new CurnException
                     ("Bad timestamp value of \"" + sTimestamp +
                      "\" for <" + OLD_XML_ENTRY_ELEMENT +
                      "> with unique ID \"" + entryID + "\". Skipping entry.");
             }
 
             // Parse the publication date, if any
 
             Date publicationDate = null;
             if (sPubDate != null)
             {
                 try
                 {
                     publicationDate = new Date(Long.parseLong(sPubDate));
                 }
 
                 catch (NumberFormatException ex)
                 {
                     log.error("Bad publication date value of \"" + sPubDate +
                               "\" for <" + OLD_XML_ENTRY_ELEMENT +
                               "> with unique ID \"" + entryID +
                               "\". Ignoring publication date.");
                 }
             }
 
             // Parse the URLs.
 
             URL channelURL = null;
             try
             {
                 channelURL = new URL(sChannelURL);
             }
 
             catch (MalformedURLException ex)
             {
                 throw new  CurnException
                     ("Bad channel URL \"" + sChannelURL + "\" for <" +
                      OLD_XML_ENTRY_ELEMENT + "> with unique ID \"" +
                      entryID + "\". Skipping entry.");
             }
 
             URL entryURL = null;
             try
             {
                 entryURL = new URL(sEntryURL);
             }
 
             catch (MalformedURLException ex)
             {
                 throw new  CurnException
                     ("Bad item URL \"" + sChannelURL + "\" for <" +
                      OLD_XML_ENTRY_ELEMENT + "> with unique ID \"" +
                      entryID + "\". Skipping entry.");
             }
 
             result = new FeedCacheEntry(entryID,
                                         channelURL,
                                         entryURL,
                                         publicationDate,
                                         timestamp);
         }
 
         return result;
     }
 
     /**
      * Attempt to parse a new-style XML metadata file.
      *
      * @param document the parsed XML file
      * @param loadedDataHandler the callback to invoke with loaded data
      *
      * @throws CurnException on error
      */
     private void readNewXMLMetaData(final Document          document,
                                     final LoadedDataHandler loadedDataHandler)
         throws CurnException
     {
         // Get the top-level element and verify that it's the one
         // we want.
 
         Element root = document.getRootElement();
         String rootTagName = root.getName();
         assert(rootTagName.equals(XML_ROOT_ELEMENT));
 
         // Get the list of channels.
 
         List<?> channels = root.getChildren(XML_FEED_ELEMENT);
         for (Iterator<?> itChannel = channels.iterator(); itChannel.hasNext(); )
         {
             // Parse the channel element itself.
 
             Element channelElement = (Element) itChannel.next();
             FeedCacheEntry entry = parseXMLFeedElement(channelElement);
             PersistentFeedData feedData = new PersistentFeedData(entry);
             URL channelURL = entry.getChannelURL();
 
             // Parse any metadata in the channel.
 
             List<?> feedMetadata =
                 channelElement.getChildren(XML_FEED_METADATA_ELEMENT);
             feedData.addFeedMetadata(parseMetadata(feedMetadata));
 
             // Get the list of items and process each one.
 
             List<?> items = channelElement.getChildren(XML_ITEM_ELEMENT);
             for (Iterator<?> itItem = items.iterator(); itItem.hasNext(); )
             {
                 // Parse the item element itself.
 
                 Element itemElement = (Element) itItem.next();
                 entry = parseXMLItemElement(itemElement, channelURL);
                 PersistentFeedItemData itemData =
                     new PersistentFeedItemData(entry);
                 feedData.addPersistentFeedItem(itemData);
 
                 // Get and process the item metadata
 
                 List<?> itemMetadata =
                     itemElement.getChildren(XML_ITEM_METADATA_ELEMENT);
                 itemData.addItemMetadata(parseMetadata(itemMetadata));
             }
 
             loadedDataHandler.feedLoaded(feedData);
         }
 
         // Finally, parse any extra metadata.
 
         Collection<PersistentMetadataGroup> extraMetadata =
             parseMetadata(root.getChildren(XML_EXTRA_METADATA_ELEMENT));
         for (PersistentMetadataGroup metadataGroup : extraMetadata)
             loadedDataHandler.extraMetadataLoaded(metadataGroup);
     }
 
     private Collection<PersistentMetadataGroup>
     parseMetadata(List<?> metadataElements)
     {
         Collection<PersistentMetadataGroup> result = new
             ArrayList<PersistentMetadataGroup>();
 
         for (Iterator<?> it = metadataElements.iterator(); it.hasNext(); )
         {
             Element mdElement = (Element) it.next();
             String namespace =
                 getRequiredXMLAttribute(mdElement,
                                         XML_METADATA_NAMESPACE_ATTR);
             List<?> nameValuePairs =
                 mdElement.getChildren(XML_METADATUM_ELEMENT);
             PersistentMetadataGroup metadataGroup =
                 new PersistentMetadataGroup(namespace);
 
             for (Iterator<?> itNV = nameValuePairs.iterator();
                  itNV.hasNext(); )
             {
                 Element nvElement = (Element) itNV.next();
                 String name =
                     getRequiredXMLAttribute(nvElement,
                                             XML_METADATUM_NAME_ATTR);
                  String value =
                     getRequiredXMLAttribute(nvElement,
                                             XML_METADATUM_VALUE_ATTR);
                  metadataGroup.addMetadataItem(name, value);
            }
 
             result.add(metadataGroup);
         }
 
         return result;
    }
 
     /**
      * Parse an XML feed metadata channel element. This method only parses the
      * attributes of the channel element; it does not handle any child
      * elements.
      *
      * @param channelElement the XML element for the channel
      *
      * @return the FeedCacheEntry object for the feed
      *
      * @throws CurnException on error
      */
     private FeedCacheEntry parseXMLFeedElement(final Element channelElement)
         throws CurnException
     {
         // Parse the channel and create an entry for it.
 
         String sChannelURL = getRequiredXMLAttribute(channelElement,
                                                      XML_URL_ATTR);
         String id = getRequiredXMLAttribute(channelElement, XML_ID_ATTR);
         String sTimestamp = getRequiredXMLAttribute(channelElement,
                                                     XML_TIMESTAMP_ATTR);
 
 
         long timestamp = 0;
         try
         {
             timestamp = Long.parseLong(sTimestamp);
         }
 
         catch (NumberFormatException ex)
         {
             throw new CurnException
                 ("Bad timestamp value of \"" + sTimestamp + "\" for <" +
                  XML_FEED_ELEMENT + "> with unique ID \"" + id +
                  "\". Skipping entry.");
         }
 
         URL channelURL;
         try
         {
             channelURL = new URL(sChannelURL);
         }
 
         catch (MalformedURLException ex)
         {
             throw new CurnException
                 ("Bad channel URL \"" + sChannelURL + "\" for <" +
                  XML_FEED_ELEMENT + "> with unique ID \"" + id + "\"");
         }
 
         FeedCacheEntry entry = new FeedCacheEntry(id,
                                                   channelURL,
                                                   channelURL,
                                                   null,
                                                   timestamp);
 
         // Now the feed metadata.
 
         return entry;
     }
 
     /**
      * Parse an XML feed metadata item entry.
      *
      * @param itemElement  the XML element for the item entry
      * @param channelURL   the URL of the parent channel
      *
      * @return the FeedCacheEntry
      *
      * @throws CurnException on error
      */
     private FeedCacheEntry parseXMLItemElement(final Element itemElement,
                                                final URL     channelURL)
         throws CurnException
     {
         FeedCacheEntry result = null;
 
         // Parse out the attributes.
 
         String id = getRequiredXMLAttribute(itemElement, XML_ID_ATTR);
         String sItemURL = getRequiredXMLAttribute(itemElement, XML_URL_ATTR);
         String sTimestamp = getRequiredXMLAttribute(itemElement,
                                                     XML_TIMESTAMP_ATTR);
         String sPubDate =  getOptionalXMLAttribute(itemElement,
                                                    XML_PUB_DATE_ATTR,
                                                    null);
 
 
         if ((id != null) &&
             (sItemURL != null) &&
             (sTimestamp != null))
         {
             // Parse the timestamp.
 
             long timestamp = 0;
             try
             {
                 timestamp = Long.parseLong(sTimestamp);
             }
 
             catch (NumberFormatException ex)
             {
                 throw new CurnException
                     ("Bad timestamp value of \"" + sTimestamp +
                      "\" for <" + XML_ITEM_ELEMENT +
                      "> with unique ID \"" + id + "\". Skipping entry.");
             }
 
             // Parse the publication date, if any
 
             Date publicationDate = null;
             if (sPubDate != null)
             {
                 try
                 {
                     long pubTimestamp = Long.parseLong(sPubDate);
                     if (pubTimestamp > 0)
                         publicationDate = new Date(pubTimestamp);
                 }
 
                 catch (NumberFormatException ex)
                 {
                     log.error("Bad publication date value of \"" + sPubDate +
                               "\" for <" + XML_ITEM_ELEMENT +
                               "> with unique ID \"" + id +
                               "\". Ignoring publication date.");
                 }
             }
 
             // Parse the URL.
 
             URL itemURL = null;
             try
             {
                 itemURL = new URL(sItemURL);
             }
 
             catch (MalformedURLException ex)
             {
                 throw new CurnException
                     ("Bad item URL \"" + sItemURL + "\" for <" +
                      XML_ITEM_ELEMENT + "> with unique ID \"" +
                      id + "\". Skipping entry.");
             }
 
             result = new FeedCacheEntry(id,
                                         channelURL,
                                         itemURL,
                                         publicationDate,
                                         timestamp);
         }
 
         return result;
     }
 
     /**
      * Retrieve an optional XML attribute value from a list of attributes.
      * If the attribute is missing or empty, the default is returned.
      *
      * @param element      the XML element
      * @param defaultValue the default value
      * @param name         the attribute name
      *
      * @return the attribute's value, or null if the attribute wasn't found
      */
     private String getOptionalXMLAttribute(final Element element,
                                            final String  name,
                                            final String  defaultValue)
     {
         String value = element.getAttributeValue(name);
         if ((value != null) && TextUtil.stringIsEmpty(value))
             value = null;
 
         return (value == null) ? defaultValue : value;
     }
 
     /**
      * Retrieve an XML attribute value from a list of attributes. If the
      * attribute is missing, the error is logged (but an exception is not
      * thrown).
      *
      * @param element the element
      * @param name    the attribute name
 *
      * @return the attribute's value, or null if the attribute wasn't found
      */
     private String getRequiredXMLAttribute (final Element element,
                                             final String  name)
     {
         String value = getOptionalXMLAttribute (element, name, null);
 
         if (value == null)
         {
             log.error("<" + element.getName() + "> is missing required " +
                       "\"" + name + "\" XML attribute.");
         }
 
         return value;
     }
 }
