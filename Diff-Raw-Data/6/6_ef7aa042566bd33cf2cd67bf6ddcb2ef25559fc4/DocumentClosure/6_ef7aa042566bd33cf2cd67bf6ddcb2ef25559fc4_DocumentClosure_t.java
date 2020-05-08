 /**
  * 
  */
 package ecologylab.bigsemantics.metadata.builtins;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ecologylab.bigsemantics.actions.SemanticAction;
 import ecologylab.bigsemantics.actions.SemanticActionHandler;
 import ecologylab.bigsemantics.actions.SemanticActionsKeyWords;
 import ecologylab.bigsemantics.collecting.DocumentDownloadedEventHandler;
 import ecologylab.bigsemantics.collecting.DownloadStatus;
 import ecologylab.bigsemantics.collecting.SemanticsDownloadMonitors;
 import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
 import ecologylab.bigsemantics.collecting.SemanticsSite;
 import ecologylab.bigsemantics.documentcache.PersistenceMetadata;
 import ecologylab.bigsemantics.documentcache.PersistentDocumentCache;
 import ecologylab.bigsemantics.documentparsers.DocumentParser;
 import ecologylab.bigsemantics.downloadcontrollers.DownloadController;
 import ecologylab.bigsemantics.html.documentstructure.SemanticInLinks;
 import ecologylab.bigsemantics.metadata.output.DocumentLogRecord;
 import ecologylab.bigsemantics.metametadata.MetaMetadata;
 import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
 import ecologylab.bigsemantics.model.text.ITermVector;
 import ecologylab.bigsemantics.model.text.TermVectorFeature;
 import ecologylab.bigsemantics.seeding.SearchResult;
 import ecologylab.bigsemantics.seeding.Seed;
 import ecologylab.bigsemantics.seeding.SeedDistributor;
 import ecologylab.collections.SetElement;
 import ecologylab.concurrent.Downloadable;
 import ecologylab.concurrent.DownloadableLogRecord;
 import ecologylab.generic.Continuation;
 import ecologylab.io.DownloadProcessor;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.formatenums.StringFormat;
 import ecologylab.serialization.library.geom.PointInt;
 
 /**
  * New Container object. Mostly just a closure around Document. Used as a candidate and wrapper for
  * downloading.
  * 
  * @author andruid
  */
 @SuppressWarnings(
 { "rawtypes", "unchecked" })
 public class DocumentClosure extends SetElement
     implements TermVectorFeature, Downloadable, SemanticActionsKeyWords,
     Continuation<DocumentClosure>
 {
 
   static Logger                               logger;
 
   static
   {
     logger = LoggerFactory.getLogger(DocumentClosure.class);
   }
 
   private SemanticsGlobalScope                semanticsScope;
 
   /**
    * This is tracked mainly for debugging, so we can see what pURL was fed into the meta-metadata
    * address resolver machine.
    */
   private ParsedURL                           initialPURL;
 
   private Document                            document;
 
   private final Object                        DOCUMENT_LOCK        = new Object();
 
   private DownloadStatus                      downloadStatus       = DownloadStatus.UNPROCESSED;
 
   private final Object                        DOWNLOAD_STATUS_LOCK = new Object();
 
   private DocumentParser                      documentParser;
 
   private SemanticInLinks                     semanticInlinks;
 
   private List<Continuation<DocumentClosure>> continuations;
 
   /**
    * Keeps state about the search process, if this is encapsulates a search result;
    */
   private SearchResult                        searchResult;
 
   private DocumentLogRecord                   logRecord;
 
   private PointInt                            dndPoint;
 
   /**
    * If true (the normal case), then any MediaElements encountered will be added to the candidates
    * collection, for possible inclusion in the visual information space.
    */
   private boolean                             collectMedia         = true;
 
   /**
    * If true (the normal case), then hyperlinks encounted will be fed to the web crawler, providing
    * that they are traversable() and of the right mime types.
    */
   private boolean                             crawlLinks           = true;
 
   private final Object                        DOWNLOAD_LOCK        = new Object();
 
   /**
    * @throws IllegalAccessException
    * @throws InstantiationException
    * @throws ClassNotFoundException
    */
   private DocumentClosure(Document document,
                           SemanticsGlobalScope semanticsSessionScope,
                           SemanticInLinks semanticInlinks)
   {
     super();
     this.semanticsScope = semanticsSessionScope;
     this.initialPURL = document.getLocation();
     this.document = document;
     this.semanticInlinks = semanticInlinks;
     this.continuations = new ArrayList<Continuation<DocumentClosure>>();
   }
 
   /**
    * Should only be called by Document.getOrCreateClosure().
    * 
    * @param document
    * @param semanticInlinks
    */
   DocumentClosure(Document document, SemanticInLinks semanticInlinks)
   {
     this(document, document.getSemanticsScope(), semanticInlinks);
   }
 
   /**
    * @return the infoCollector
    */
   public SemanticsGlobalScope getSemanticsScope()
   {
     return semanticsScope;
   }
 
   public ParsedURL getInitialPURL()
   {
     return initialPURL;
   }
 
   /**
    * @return the document
    */
   public Document getDocument()
   {
     synchronized (DOCUMENT_LOCK)
     {
       return document;
     }
   }
 
   public DocumentParser getDocumentParser()
   {
     return documentParser;
   }
 
   /**
    * @param presetDocumentParser
    *          the presetDocumentParser to set
    */
   public void setDocumentParser(DocumentParser presetDocumentParser)
   {
     this.documentParser = presetDocumentParser;
   }
 
   @Override
   public SemanticsSite getSite()
   {
     Document document = this.document;
     return (document == null) ? null : document.getSite();
   }
 
   @Override
   public SemanticsSite getDownloadSite()
   {
     Document document = this.document;
     if (document != null)
     {
       if (document.getDownloadLocation().isFile())
         return null;
     }
     return (document == null) ? null : document.getSite();
   }
 
   public boolean isFromSite(SemanticsSite site)
   {
     return site != null && site == getSite();
   }
 
   @Override
   public ParsedURL location()
   {
     Document document = this.document;
     return (document == null) ? null : document.getLocation();
   }
 
   @Override
   public ParsedURL getDownloadLocation()
   {
     Document document = this.document;
     return (document == null) ? null : document.getDownloadLocation();
   }
 
   /**
    * @return the semanticInlinks
    */
   public SemanticInLinks getSemanticInlinks()
   {
     return semanticInlinks;
   }
 
   /**
    * Keeps state about the search process, if this Container is a search result;
    */
   public SearchResult searchResult()
   {
     return searchResult;
   }
 
   /**
    * 
    * @param resultDistributer
    * @param searchNum
    *          Index into the total number of (seeding) searches specified and being aggregated.
    * @param resultNum
    *          Result number among those returned by google.
    */
   public void setSearchResult(SeedDistributor resultDistributer, int resultNum)
   {
     searchResult = new SearchResult(resultDistributer, resultNum);
   }
 
   public SeedDistributor resultDistributer()
   {
     return (searchResult == null) ? null : searchResult.resultDistributer();
   }
 
   @Override
   public DownloadableLogRecord getLogRecord()
   {
     return logRecord;
   }
 
   public void setLogRecord(DocumentLogRecord logRecord)
   {
     this.logRecord = logRecord;
   }
 
   @Override
   public boolean isImage()
   {
     return document.isImage();
   }
 
   public boolean isSeed()
   {
     return (document != null) && document.isSeed();
   }
 
   public Seed getSeed()
   {
     return document != null ? document.getSeed() : null;
   }
 
   public boolean isDnd()
   {
     return dndPoint != null;
   }
 
   public PointInt getDndPoint()
   {
     return dndPoint;
   }
 
   public void setDndPoint(PointInt dndPoint)
   {
     this.dndPoint = dndPoint;
   }
 
   /**
    * This method is called before we actually hit the website. Thus, it uses the initial URL to test
    * if we need to hit the website. If it returns true, we definitely don't need to hit the website;
    * if it returns false, we need to hit the website, but the actual document might have been cached
    * using another URL.
    */
   @Override
   public boolean isCached()
   {
     return false;
   }
 
   /**
    * @return the downloadStatus
    */
   public DownloadStatus getDownloadStatus()
   {
     synchronized (DOWNLOAD_STATUS_LOCK)
     {
       return downloadStatus;
     }
   }
 
   public boolean isUnprocessed()
   {
     return getDownloadStatus() == DownloadStatus.UNPROCESSED;
   }
 
   /**
    * Test state variable inside of QUEUE_DOWNLOAD_LOCK.
    * 
    * @return true if result has already been queued, connected to, downloaded, ... so it should not
    *         be operated on further.
    */
   public boolean downloadHasBeenQueued()
   {
     return getDownloadStatus() != DownloadStatus.UNPROCESSED;
   }
 
   /**
    * Test and set state variable inside of QUEUE_DOWNLOAD_LOCK.
    * 
    * @return true if this really queues the download, and false if it had already been queued.
    */
   private boolean testAndSetQueueDownload()
   {
     synchronized (DOWNLOAD_STATUS_LOCK)
     {
       if (downloadStatus != DownloadStatus.UNPROCESSED)
         return false;
       setDownloadStatusInternal(DownloadStatus.QUEUED);
       return true;
     }
   }
 
   private void setDownloadStatus(DownloadStatus newStatus)
   {
     synchronized (DOWNLOAD_STATUS_LOCK)
     {
       setDownloadStatusInternal(newStatus);
     }
   }
 
   /**
    * (this method does not lock DOWNLOAD_STATUS_LOCK!)
    * 
    * @param newStatus
    */
   private void setDownloadStatusInternal(DownloadStatus newStatus)
   {
     this.downloadStatus = newStatus;
     if (this.document != null)
    {
       document.setDownloadStatus(newStatus);
    }
   }
 
   public DownloadProcessor<DocumentClosure> downloadMonitor()
   {
     SemanticsDownloadMonitors downloadMonitors = semanticsScope.getDownloadMonitors();
     return downloadMonitors.downloadProcessor(document.isImage(),
                                               isDnd(),
                                               isSeed(),
                                               document.isGui());
   }
 
   /**
    * Download if necessary, using the {@link ecologylab.concurrent.DownloadMonitor DownloadMonitor}
    * if USE_DOWNLOAD_MONITOR is set (it seems it always is), or in a new thread. Control will be
    * passed to {@link #downloadAndParse() downloadAndParse()}. Does nothing if this has been
    * previously queued, if it has been recycled, or if it isMuted().
    * 
    * @return true if this is actually queued for download. false if it was previously, if its been
    *         recycled, or if it is muted.
    */
   public boolean queueDownload()
   {
     if (recycled())
     {
       debugA("ERROR: cant queue download cause already recycled.");
       return false;
     }
     if (this.getDownloadLocation() == null)
       return false;
     final boolean result = !filteredOut(); // for dashboard type on the fly filtering
     if (result)
     {
       if (!testAndSetQueueDownload())
         return false;
       delete(); // remove from candidate pools! (invokes deleteHook as well)
 
       downloadMonitor().download(this, continuations == null ? null : this);
     }
     return result;
   }
 
   /**
    * Connect to the information resource. Figure out the appropriate MetaMetadata and DocumentType.
    * Download the information resource and parse it. Do cleanup afterwards.
    * 
    * This method is typically called by DownloadMonitor.
    * 
    * @throws IOException
    */
   @Override
   public void performDownload() throws IOException
   {
     if (recycled() || document.isRecycled())
     {
       logger.error("Recycled document closure in performDownload(): " + document);
       return;
     }
 
     synchronized (DOWNLOAD_STATUS_LOCK)
     {
       if (!(downloadStatus == DownloadStatus.QUEUED || downloadStatus == DownloadStatus.UNPROCESSED))
       {
         return;
       }
       setDownloadStatusInternal(DownloadStatus.CONNECTING);
     }
 
     ParsedURL location = location();
     MetaMetadata metaMetadata = (MetaMetadata) document.getMetaMetadata();
     boolean noCache = metaMetadata.isNoCache();
     PersistentDocumentCache pCache = semanticsScope.getPersistentDocumentCache();
 
     // Check the persistent cache first
     Document cachedDoc = null;
     if (pCache != null && !noCache)
     {
       cachedDoc = retrieveFromPersistentCache(pCache, location);
     }
 
     // If not in the persistent cache, download the raw page and parse
     if (cachedDoc == null)
     {
       DownloadController downloadController = downloadRawPage(location);
       if (downloadController.isGood())
       {
         handleRedirections(downloadController, location);
         metaMetadata = changeMetaMetadataIfNeeded(downloadController.getMimeType());
 
         findParser(metaMetadata, downloadController);
         if (documentParser != null)
         {
           doParse(metaMetadata);
           if (pCache != null && !noCache)
           {
             doPersist(pCache, downloadController);
           }
         }
       }
       else
       {
         logger.error("Network connection error: " + document);
        setDownloadStatus(DownloadStatus.IOERROR);
         return;
       }
     }
     else
     {
       changeDocument(cachedDoc);
     }
 
     document.downloadAndParseDone(documentParser);
     setDownloadStatus(DownloadStatus.DOWNLOAD_DONE);
   }
 
   private Document retrieveFromPersistentCache(PersistentDocumentCache pCache, ParsedURL location)
   {
     Document cachedDoc = null;
     long t0 = System.currentTimeMillis();
     PersistenceMetadata pMetadata = pCache.getMetadata(location);
     String repoHash = semanticsScope.getMetaMetadataRepositoryHash();
     if (pMetadata != null && repoHash.equals(pMetadata.getRepositoryHash()))
     {
       cachedDoc = pCache.retrieve(location);
     }
     if (logRecord != null)
     {
       logRecord.setMsMetadataCacheLookup(System.currentTimeMillis() - t0);
       if (cachedDoc != null)
       {
         logRecord.setPersisentDocumentCacheHit(true);
       }
     }
     return cachedDoc;
   }
 
   private DownloadController downloadRawPage(ParsedURL location) throws IOException
   {
     String userAgent = document.getMetaMetadata().getUserAgentString();
     DownloadController downloadController = semanticsScope.createDownloadController(this);
     downloadController.setUserAgent(userAgent);
     long t0download = System.currentTimeMillis();
     downloadController.accessAndDownload(location);
     if (logRecord != null)
     {
       logRecord.setMsHtmlDownload(System.currentTimeMillis() - t0download);
     }
     return downloadController;
   }
 
   private void handleRedirections(DownloadController downloadController, ParsedURL location)
   {
     // handle redirections:
     List<ParsedURL> redirectedLocations = downloadController.getRedirectedLocations();
     if (redirectedLocations != null)
     {
       for (ParsedURL redirectedLocation : redirectedLocations)
       {
         if (redirectedLocation != null)
         {
           document.addAdditionalLocation(redirectedLocation);
           Document newDocument = semanticsScope.getOrConstructDocument(redirectedLocation);
           newDocument.addAdditionalLocation(location);
           changeDocument(newDocument);
         }
       }
     }
   }
 
   private MetaMetadata changeMetaMetadataIfNeeded(String mimeType)
   {
     MetaMetadata metaMetadata = (MetaMetadata) document.getMetaMetadata();
     // check for more specific meta-metadata
     if (metaMetadata.isGenericMetadata())
     { // see if we can find more specifc meta-metadata using mimeType
       MetaMetadataRepository repository = semanticsScope.getMetaMetadataRepository();
       MetaMetadata mimeMmd = repository.getMMByMime(mimeType);
       if (mimeMmd != null && !mimeMmd.equals(metaMetadata))
       {
         // new meta-metadata!
         if (!mimeMmd.getMetadataClass().isAssignableFrom(document.getClass()))
         {
           // more specific so we need new metadata!
           Document document = (Document) mimeMmd.constructMetadata(); // set temporary on stack
           changeDocument(document);
         }
         metaMetadata = mimeMmd;
         document.setMetaMetadata(mimeMmd);
       }
     }
     return metaMetadata;
   }
 
   private void findParser(MetaMetadata metaMetadata, DownloadController downloadController)
   {
     if (documentParser == null)
     {
       boolean noParser = false;
 
       // // First check if registered no parser
       // boolean noParser = DocumentParser.isRegisteredNoParser(document.getLocation());
       // List<MetadataParsedURL> additionalLocations = document.getAdditionalLocations();
       // if (additionalLocations != null)
       // {
       // for (int i = 0; i < additionalLocations.size() && !noParser; ++i)
       // {
       // noParser |= DocumentParser.isRegisteredNoParser(additionalLocations.get(i).getValue());
       // }
       // }
 
       if (noParser)
       {
         logger.warn("Registered no parser: " + document);
       }
       else
       {
         // If not registered no parser, try to find one
         documentParser =
             DocumentParser.getByMmd(metaMetadata, semanticsScope, this, downloadController);
         if (documentParser == null)
         {
           logger.warn("No parser found: " + metaMetadata);
         }
       }
     }
   }
 
   private void doParse(MetaMetadata metaMetadata) throws IOException
   {
     // container or not (it could turn out to be an image or some other mime type), parse the baby!
     setDownloadStatus(DownloadStatus.PARSING);
 
     takeSemanticActions(metaMetadata, metaMetadata.getBeforeSemanticActions());
     long t0extraction = System.currentTimeMillis();
     documentParser.parse();
     if (logRecord != null)
     {
       logRecord.setMsExtraction(System.currentTimeMillis() - t0extraction);
     }
     takeSemanticActions(metaMetadata, metaMetadata.getAfterSemanticActions());
 
     addDocGraphCallbacksIfNeeded();
   }
 
   private void takeSemanticActions(MetaMetadata metaMetadata, ArrayList<SemanticAction> actions)
   {
     if (metaMetadata != null && actions != null)
     {
       SemanticActionHandler handler = new SemanticActionHandler(semanticsScope, documentParser);
       handler.takeSemanticActions(metaMetadata, document, actions);
     }
   }
 
   private void addDocGraphCallbacksIfNeeded()
   {
     if (this.getSemanticsScope().ifAutoUpdateDocRefs())
     {
       // add callbacks so that when this document is downloaded and parsed, references to it will
       // be updated automatically.
       Set<DocumentDownloadedEventHandler> listeners =
           semanticsScope.getDocumentDownloadingMonitor().getListenersForDocument(document);
       if (listeners != null && listeners.size() > 0)
       {
         addContinuations(listeners);
       }
     }
   }
 
   private void doPersist(PersistentDocumentCache pCache, DownloadController downloadController)
       throws IOException
   {
     long t0persist = System.currentTimeMillis();
     PersistenceMetadata pMetadata = new PersistenceMetadata();
     pMetadata.setMimeType(downloadController.getMimeType());
     String rawDoc = downloadController.getContent();
     pCache.store(document, rawDoc, pMetadata);
     if (logRecord != null)
     {
       logRecord.setMsMetadataCaching(System.currentTimeMillis() - t0persist);
     }
   }
 
   /**
    * In use cases such as the service, we want to be able to call performDownload() synchronously,
    * and in the same time make sure that the same closure will be downloaded by one thread at a
    * time. This method uses a lock to implement this.
    * 
    * @throws IOException
    */
   public void performDownloadSynchronously() throws IOException
   {
     synchronized (DOWNLOAD_LOCK)
     {
       performDownload();
     }
   }
 
   /**
    * Dispatch all of our registered callbacks.
    */
   @Override
   public void callback(DocumentClosure o)
   {
     if (continuations == null)
       return;
 
     List<Continuation<DocumentClosure>> currentContinuations;
     synchronized (continuations)
     {
       currentContinuations = new ArrayList<Continuation<DocumentClosure>>(continuations);
     }
     if (currentContinuations != null)
     {
       for (Continuation<DocumentClosure> continuation : currentContinuations)
       {
         try
         {
           continuation.callback(o);
         }
         catch (Exception e)
         {
           logger.error("Error calling back: " + o + ": " + continuation, e);
         }
       }
     }
 
     // wait to recycle continuations until after they have been called.
     if (isRecycled())
     {
       continuations.clear();
       continuations = null;
     }
   }
 
   public List<Continuation<DocumentClosure>> getContinuations()
   {
     return continuations;
   }
 
   private List<Continuation<DocumentClosure>> continuations()
   {
     return continuations;
   }
 
   public void addContinuation(Continuation<DocumentClosure> continuation)
   {
     synchronized (continuations)
     {
       continuations().add(continuation);
     }
   }
 
   public void addContinuations(Collection<? extends Continuation<DocumentClosure>> incomingContinuations)
   {
     synchronized (continuations)
     {
       List<Continuation<DocumentClosure>> continuations = continuations();
       for (Continuation<DocumentClosure> continuation : incomingContinuations)
         continuations.add(continuation);
     }
   }
 
   public void addContinuationBefore(Continuation<DocumentClosure> continuation)
   {
     synchronized (continuations)
     {
       continuations().add(0, continuation);
     }
   }
 
   /**
    * Add a continuation to this closure before it is downloaded (i.e. before its performDownload()
    * method finishes).
    * 
    * This gives the client the possibility of making sure the continuation will be called when the
    * closure finishes downloading.
    * 
    * @param continuation
    * @return true if the continuation is added before the closure finishes downloading; false if the
    *         closure is already downloaded.
    */
   public boolean addContinuationBeforeDownloadDone(Continuation<DocumentClosure> continuation)
   {
     if (downloadStatus != DownloadStatus.DOWNLOAD_DONE
         && downloadStatus != DownloadStatus.IOERROR
         && downloadStatus != DownloadStatus.RECYCLED)
     {
       synchronized (DOWNLOAD_STATUS_LOCK)
       {
         if (downloadStatus != DownloadStatus.DOWNLOAD_DONE
             && downloadStatus != DownloadStatus.IOERROR
             && downloadStatus != DownloadStatus.RECYCLED)
         {
           addContinuation(continuation);
           return true;
         }
       }
     }
     return false;
   }
 
   /**
    * Document metadata object must change, because we learned something new about its type.
    * 
    * @param newDocument
    */
   public void changeDocument(Document newDocument)
   {
     synchronized (DOCUMENT_LOCK)
     {
       Document oldDocument = document;
       document = newDocument;
 
       SemanticsSite oldSite = oldDocument.site();
       SemanticsSite newSite = newDocument.site();
       if (oldSite != null && oldSite != newSite)
       {
         // calling changeDocument() because of redirecting?
         if (oldSite.isDownloading())
           oldSite.endDownload(oldDocument.getDownloadLocation());
       }
 
       newDocument.inheritValues(oldDocument);
 
       semanticInlinks = newDocument.getSemanticInlinks(); // probably not needed, but just in case.
       oldDocument.recycle();
     }
   }
 
   /**
    * Close the current connection. Re-open a connection to the same location. Use the same Document
    * object; don't process re-directs, or anything like that. Re-connect simply.
    * 
    * @return PURLConnection for the new connection.
    * @throws IOException
    */
   public DownloadController reConnect() throws IOException
   {
     DownloadController downloadController = semanticsScope.createDownloadController(this);
     downloadController.accessAndDownload(document.getLocation());
     return downloadController;
   }
 
   @Override
   public void recycle()
   {
     recycle(false);
   }
 
   @Override
   public synchronized void recycle(boolean recycleDocument)
   {
     synchronized (DOWNLOAD_STATUS_LOCK)
     {
       if (downloadStatus == DownloadStatus.RECYCLED)
         return;
       setDownloadStatusInternal(DownloadStatus.RECYCLED);
     }
 
     if (documentParser != null)
       documentParser.recycle();
 
     semanticInlinks = null;
 
     initialPURL = null;
 
     // ??? should we recycle Document here -- under what circumstances???
     if (recycleDocument)
       document.recycle();
   }
 
   @Override
   public boolean recycled()
   {
     Document document = this.document;
     return document == null || document.isRecycled();
   }
 
   @Override
   public boolean isRecycled()
   {
     return document == null || document.isRecycled();
   }
 
   /**
    * Resets this closure as if it is newly created.
    */
   public void reset()
   {
     setDownloadStatus(DownloadStatus.UNPROCESSED);
     if (document != null)
     {
       document.resetRecycleStatus();
     }
   }
 
   @Override
   public String toString()
   {
     return super.toString() + "[" + document.getLocation() + "]";
   }
 
   @Override
   public int hashCode()
   {
     return (document == null) ? -1 : document.hashCode();
   }
 
   @Override
   public ITermVector termVector()
   {
     return (document == null) ? null : document.termVector();
   }
 
   /**
    * Called by DownloadMonitor in case a timeout happens.
    */
   @Override
   public void handleIoError(Throwable e)
   {
     setDownloadStatus(DownloadStatus.IOERROR);
     if (documentParser != null)
     {
       documentParser.handleIoError(e);
     }
     recycle();
   }
 
   @Override
   public String message()
   {
     return document == null ? "recycled" : document.getLocation().toString();
   }
 
   public void serialize(OutputStream stream)
   {
     serialize(stream, StringFormat.XML);
   }
 
   public void serialize(OutputStream stream, StringFormat format)
   {
     Document document = getDocument();
     try
     {
       SimplTypesScope.serialize(document, System.out, format);
 
       System.out.println("\n");
     }
     catch (SIMPLTranslationException e)
     {
       error("Could not serialize " + document);
       e.printStackTrace();
     }
   }
 
   public void serialize(StringBuilder buffy)
   {
     Document document = getDocument();
     try
     {
       SimplTypesScope.serialize(document, buffy, StringFormat.XML);
       System.out.println("\n");
     }
     catch (SIMPLTranslationException e)
     {
       error("Could not serialize " + document);
       e.printStackTrace();
     }
   }
 
 }
