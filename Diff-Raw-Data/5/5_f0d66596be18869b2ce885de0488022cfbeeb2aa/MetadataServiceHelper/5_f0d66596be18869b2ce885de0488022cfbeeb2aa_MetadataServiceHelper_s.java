 /**
  * 
  */
 package ecologylab.bigsemantics.service.metadata;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import ecologylab.bigsemantics.collecting.DownloadStatus;
 import ecologylab.bigsemantics.downloaders.controllers.DownloadControllerType;
 import ecologylab.bigsemantics.filestorage.FileSystemStorage;
 import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
 import ecologylab.bigsemantics.metadata.Metadata;
 import ecologylab.bigsemantics.metadata.MetadataBase;
 import ecologylab.bigsemantics.metadata.builtins.Document;
 import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
 import ecologylab.bigsemantics.metadata.builtins.Image;
 import ecologylab.bigsemantics.metadata.output.DocumentLogRecord;
 import ecologylab.bigsemantics.metadata.scalar.MetadataParsedURL;
 import ecologylab.bigsemantics.metametadata.ClassAndCollectionIterator;
 import ecologylab.bigsemantics.metametadata.MetaMetadata;
 import ecologylab.bigsemantics.service.SemanticServiceErrorCodes;
 import ecologylab.bigsemantics.service.SemanticServiceScope;
 import ecologylab.bigsemantics.service.logging.ServiceLogRecord;
 import ecologylab.concurrent.DownloadableLogRecord;
 import ecologylab.generic.Continuation;
 import ecologylab.generic.Debug;
 import ecologylab.logging.ILogger;
 import ecologylab.logging.ILoggerFactory;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.formatenums.StringFormat;
 
 /**
  * helper class for metadata.xml and metadata.json
  * 
  * @author ajit
  * 
  */
 public class MetadataServiceHelper extends Debug implements Continuation<DocumentClosure>
 {
 
   public static int                   CONTINUATION_TIMOUT_MILLI = 60000;
 
   static ILogger                      serviceLog;
 
   static ILogger                      servicePerfLog;
 
   private static SemanticServiceScope semanticsServiceScope;
 
   /**
    * used by document iterator
    */
   private static HashSet<Metadata>    visitedMetadata;
 
   static
   {
     ILoggerFactory loggerFactory = SemanticServiceScope.get().getLoggerFactory();
     serviceLog = loggerFactory.getLogger(MetadataServiceHelper.class);
     servicePerfLog = loggerFactory.getLogger("ecologylab.bigsemantics.service.PERF");
     semanticsServiceScope = SemanticServiceScope.get();
     visitedMetadata = new HashSet<Metadata>();
   }
 
   private StringFormat                format;
 
   private int                         span;
 
   /**
    * main document to be returned corresponding to request url
    */
   private Document                    document;
 
   private Document                    origDoc                   = null;
 
   private ServiceLogRecord            logRecord;
   
   private boolean											reload;
 
   /**
    * url to graph level map
    */
   private HashMap<String, Integer>    urlSpanMap;
 
   public MetadataServiceHelper(StringFormat format)
   {
     this.format = format;
     this.urlSpanMap = new HashMap<String, Integer>();
     this.logRecord = new ServiceLogRecord();
   }
 
   public Response getMetadata(ParsedURL url, int span, boolean reload)
   {
     this.span = span;
     this.reload = reload;
     Response resp = null;
     
     long beginTime = System.currentTimeMillis();
     logRecord.setBeginTime(new Date(beginTime));
     
     synchronized (urlSpanMap)
     {
       // asynchronous metadata request
       requestMetadata(url, 0);
       try
       {
         // notified when the graph span is completed
         urlSpanMap.wait(CONTINUATION_TIMOUT_MILLI);
       }
       catch (InterruptedException e)
       {
         e.printStackTrace();
       }
     }
 
     if (document != null)
     {
       try
       {
         long millis = System.currentTimeMillis();
         String responseBody = SimplTypesScope.serialize(document, format).toString();
         logRecord.setmSecInSerialization(System.currentTimeMillis() - millis);
         resp = Response.status(Status.OK).entity(responseBody).build();
       }
       catch (SIMPLTranslationException e)
       {
         e.printStackTrace();
         serviceLog.error("exception while serializing document");
         resp = Response.status(Status.INTERNAL_SERVER_ERROR)
             .entity(SemanticServiceErrorCodes.INTERNAL_ERROR).type(MediaType.TEXT_PLAIN).build();
       }
     }
     else
     {
 	    serviceLog.error("metadata couldn't be obtained");
 	    resp = Response.status(Status.NOT_FOUND).entity(SemanticServiceErrorCodes.METADATA_NOT_FOUND)
 	        .type(MediaType.TEXT_PLAIN).build();
     }
     
     logRecord.setMsTotal(System.currentTimeMillis() - beginTime);
     logRecord.setResponseCode(resp.getStatus());
     try
     {
     	servicePerfLog.debug(SimplTypesScope.serialize(logRecord, StringFormat.JSON).toString());
     } 
     catch (SIMPLTranslationException e) 
     {
     	serviceLog.error("exception serializing perf log");
     }    
     
     return resp;
   }
 
   private void requestMetadata(ParsedURL thatPurl, int level)
   {
     DocumentLogRecord docLogRecord = new DocumentLogRecord();
 
     logRecord.setRequestUrl(thatPurl);
     Document document = semanticsServiceScope.getOrConstructDocument(thatPurl);
     
     boolean noCache = false;
     if (document != null)
     {
     	MetaMetadata mmd = ((MetaMetadata)document.getMetaMetadata());
     	if (mmd != null)
     		noCache = mmd.isNoCache();
     	else
     		serviceLog.warn("mmd no_cache couldn't be determined for document");
     	
     	serviceLog.debug("Download status of %s: %s", document, document.getDownloadStatus());
     	
     	ParsedURL docUrl = document.getLocation();
     	if (document.isRecycled() || noCache || reload)
       {
     	  //do we need to remove both?
     	  serviceLog.debug("removing document [%s] from service global collection", thatPurl);
         semanticsServiceScope.getGlobalCollection().removed(thatPurl);
     	  serviceLog.debug("removing document [%s] from service global collection", docUrl);
         semanticsServiceScope.getGlobalCollection().removed(docUrl);
         if (reload)
         {
       	  serviceLog.debug("removing document [%s] from caches", docUrl);
         	semanticsServiceScope.getDBDocumentProvider().removeDocument(docUrl);
         	FileSystemStorage.getStorageProvider().removeFileAndMetadata(docUrl);
         }
         document = semanticsServiceScope.getOrConstructDocument(thatPurl);
       }
     	
       logRecord.setDocumentUrl(docUrl);
       serviceLog.debug("Document received from the service scope for URL[%s]: %s",
                        thatPurl,
                        document);
 
       // add entry to hashmap
       urlSpanMap.put(decodeUrl(thatPurl.toString()), level);
    	}
     
     // document might already be present in GlobalCollection
    if (document != null && document.isDownloadDone() && !document.isRecycled())
     {
       final Document theDoc = document;
       logRecord.setDocumentCollectionCacheHit(true);
       serviceLog.debug("Cache hit for document[%s]", document.getLocation());
       // metadataCacheLog.debug("Cache hit: " + document +
       // " document obtained from global document collection");
 
       new Thread(new Runnable()
       {
         @Override
         public void run()
         {
           generateSpan(theDoc);
         }
       }).start();
     }
     else if (document != null)
     {
       // we can remove this synchronized after careful consideration
       // but nonetheless it prevents generateSpan occurring twice with
       // addContinuation callback and queueDownload fail
       synchronized (this)
       {
         DocumentClosure documentClosure;
         if (noCache)
         	documentClosure = document.getOrConstructClosure(DownloadControllerType.DEFAULT);
         else
         	documentClosure = document.getOrConstructClosure(DownloadControllerType.OODSS);
 
         documentClosure.addContinuation(this);
         documentClosure.setLogRecord(docLogRecord);
         origDoc = document;
         if (document.getDownloadStatus() == DownloadStatus.UNPROCESSED)
         {
           documentClosure.setLogRecord(logRecord);
           serviceLog.debug("Queueing document[%s] for downloading", document.getLocation());
           documentClosure.queueDownload();
         }
         // semanticsServiceScope.getDownloadMonitors().requestStops();
         // metadataCacheLog.debug("Uncached doc: " + document + " document queued for download at "
         // + (new Date()));
       }
     }
     else
     {
       serviceLog.error("getOrConstructDocument() returns null -- this should never happen!");
     }
   }
 
   @Override
   public synchronized void callback(DocumentClosure incomingClosure)
   {
     // semanticsServiceScope.getDownloadMonitors().stop(false);
     Document newDoc = incomingClosure.getDocument();
     if (origDoc != newDoc)
     {
       semanticsServiceScope.getGlobalCollection().remap(origDoc, newDoc);
     }
     generateSpan(newDoc);
 
     DownloadableLogRecord docLogRecord = incomingClosure.getLogRecord();
     if (docLogRecord != null)
       try
       {
         serviceLog.info(SimplTypesScope.serialize(docLogRecord, StringFormat.JSON).toString());
       }
       catch (SIMPLTranslationException e)
       {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
   }
 
   private void generateSpan(Document document)
   {
     // locallocation not required in response
     document.setLocalLocationMetadata(null);
 
     int level = -1;
     String loc = getLocationInMap(document);
     if (loc == null)
       return;
     else
       level = urlSpanMap.get(loc);
 
     serviceLog.debug("span: " + level + " document: " + document);
 
     // set the document that is to be returned
     if (level == 0)
       this.document = document;
 
     if (level++ < this.span)
     {
       MetadataBase md;
       ClassAndCollectionIterator iter;
 
       iter = document.metadataIterator(visitedMetadata);
       while (iter.hasNext())
       {
         md = iter.next();
         // System.out.println("metadata: " + md);
         if (md instanceof Document)
         {
           Document doc1 = (Document) md;
           if (doc1.getLocation() != null)
             requestMetadata(doc1.getLocation(), level);
           else
             serviceLog.warn("location is null for document: " + doc1);
           // don't track linked documents as they get added to GlobalDocumentCollection
         }
       }
     }
 
     // notify main thread to return response if queue is empty
     synchronized (urlSpanMap)
     {
       urlSpanMap.remove(loc);
       if (urlSpanMap.isEmpty())
         urlSpanMap.notify();
     }
   }
 
   private String getLocationInMap(Document document)
   {
     // check level by getting span from the hashmap
     String loc = (document instanceof Image) ? ((Image) document).getInternetLocation().toString()
         : document.getLocation().toString();
     loc = decodeUrl(loc);
     if (!urlSpanMap.containsKey(loc))
     {
       List<MetadataParsedURL> additionalLocations = document.getAdditionalLocations();
       if (additionalLocations != null)
       {
         for (MetadataParsedURL additionalLocation : additionalLocations)
         {
           loc = additionalLocation.getValue().toString();
           loc = decodeUrl(loc);
           if (urlSpanMap.containsKey(loc))
           {
             return loc;
           }
         }
       }
 
       StringBuilder sb = StringBuilderUtils.acquire();
       for (String existingUrl : urlSpanMap.keySet())
         sb.append("    ").append(existingUrl).append("\n");
       String content = sb.toString();
       StringBuilderUtils.release(sb);
       serviceLog.warn(document
           + " location doesn't match with the queued location! recorded locations:\n"
           + content);
       return null;
     }
     else
       return loc;
   }
 
   String decodeUrl(String url)
   {
     if (url != null)
       try
       {
         return URLDecoder.decode(url, "UTF-8");
       }
       catch (UnsupportedEncodingException e)
       {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
     return url;
   }
 
 }
