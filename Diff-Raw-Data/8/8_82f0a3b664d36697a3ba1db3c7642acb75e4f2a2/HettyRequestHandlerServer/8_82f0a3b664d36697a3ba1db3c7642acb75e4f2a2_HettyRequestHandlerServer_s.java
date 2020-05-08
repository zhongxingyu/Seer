 package ddth.dasp.hetty.back;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ddth.dasp.common.DaspGlobal;
 import ddth.dasp.common.RequestLocal;
 import ddth.dasp.common.logging.ProfileLogger;
 import ddth.dasp.common.osgi.IOsgiBootstrap;
 import ddth.dasp.hetty.IRequestActionHandler;
 import ddth.dasp.hetty.message.IMessageFactory;
 import ddth.dasp.hetty.message.IRequest;
 import ddth.dasp.hetty.message.IRequestParser;
 import ddth.dasp.hetty.message.IResponse;
 import ddth.dasp.hetty.message.protobuf.ResponseUtils;
 import ddth.dasp.hetty.qnt.IQueueReader;
 import ddth.dasp.hetty.qnt.ITopicPublisher;
 
 public class HettyRequestHandlerServer {
 
     private final Logger LOGGER = LoggerFactory.getLogger(HettyRequestHandlerServer.class);
 
     private IQueueReader queueReader;
     private ITopicPublisher topicPublisher;
     private long readTimeoutMillisecs = 5000, writeTimeoutMillisecs = 5000;
     private IRequestParser requestParser;
     private int numWorkers = Runtime.getRuntime().availableProcessors();
     private Thread[] workerThreads;
     private IMessageFactory messageFactory;
 
     public HettyRequestHandlerServer() {
     }
 
     public IMessageFactory getMessageFactory() {
         return messageFactory;
     }
 
     public HettyRequestHandlerServer setMessageFactory(IMessageFactory messageFactory) {
         this.messageFactory = messageFactory;
         return this;
     }
 
     public IQueueReader getQueueReader() {
         return queueReader;
     }
 
     public HettyRequestHandlerServer setQueueReader(IQueueReader queueReader) {
         this.queueReader = queueReader;
         return this;
     }
 
     public ITopicPublisher getTopicPublisher() {
         return topicPublisher;
     }
 
     public HettyRequestHandlerServer setTopicPublisher(ITopicPublisher topicPublisher) {
         this.topicPublisher = topicPublisher;
         return this;
     }
 
     public long getReadTimeoutMillisecs() {
         return readTimeoutMillisecs;
     }
 
     public HettyRequestHandlerServer setReadTimeoutMillisecs(long readTimeoutMillisecs) {
         this.readTimeoutMillisecs = readTimeoutMillisecs;
         return this;
     }
 
     public long getWriteTimeoutMillisecs() {
         return writeTimeoutMillisecs;
     }
 
     public HettyRequestHandlerServer setWriteTimeoutMillisecs(long writeTimeoutMillisecs) {
         this.writeTimeoutMillisecs = writeTimeoutMillisecs;
         return this;
     }
 
     public IRequestParser getRequestParser() {
         return requestParser;
     }
 
     public HettyRequestHandlerServer setRequestParser(IRequestParser requestParser) {
         this.requestParser = requestParser;
         return this;
     }
 
     public int getNumWorkers() {
         return numWorkers;
     }
 
     public HettyRequestHandlerServer setNumWorkers(int numWorkers) {
         this.numWorkers = numWorkers;
         return this;
     }
 
     protected void handleRequest(IRequest request) throws Exception {
         ProfileLogger.push("lookup_handler");
         IRequestActionHandler handler = null;
         try {
             String module = requestParser.getModule(request);
             String action = requestParser.getAction(request);
             Map<String, String> filter = new HashMap<String, String>();
            filter.put(IRequestActionHandler.FILTER_KEY_MODULE, module != null ? module : "");
            filter.put(IRequestActionHandler.FILTER_KEY_ACTION, action != null ? action : "");
             IOsgiBootstrap osgiBootstrap = DaspGlobal.getOsgiBootstrap();
             handler = osgiBootstrap.getService(IRequestActionHandler.class, filter);
             if (handler == null) {
                 // fallback 1: lookup for wildcard handler
                 // note: do not use "*" for filtering as OSGi will match "any"
                 // service, which is not what we want!
                 filter.put(IRequestActionHandler.FILTER_KEY_ACTION, "_");
                 handler = osgiBootstrap.getService(IRequestActionHandler.class, filter);
             }
             if (handler == null) {
                 // fallback 2: lookup for non-action handler
                 filter.remove(IRequestActionHandler.FILTER_KEY_ACTION);
                 handler = osgiBootstrap.getService(IRequestActionHandler.class, filter);
             }
         } finally {
             ProfileLogger.pop();
         }
         if (handler != null) {
             handler.handleRequest(request, topicPublisher);
         } else {
             IResponse response = ResponseUtils.response404(request);
             topicPublisher.publishToTopic(response, writeTimeoutMillisecs, TimeUnit.MILLISECONDS);
         }
     }
 
     public void destroy() {
         for (Thread workerThread : workerThreads) {
             try {
                 workerThread.interrupt();
             } catch (Exception e) {
             }
         }
     }
 
     public void start() {
         workerThreads = new Thread[numWorkers];
         for (int i = 1; i <= numWorkers; i++) {
             Thread t = new Thread(HettyRequestHandlerServer.class.getName() + " - " + i) {
                 public void run() {
                     while (!interrupted()) {
                         Object obj = queueReader.readFromQueue(readTimeoutMillisecs,
                                 TimeUnit.MILLISECONDS);
                         if (obj != null) {
                             // init the request local
                             RequestLocal requestLocal = RequestLocal.get();
                             if (requestLocal == null) {
                                 requestLocal = new RequestLocal();
                                 RequestLocal.set(requestLocal);
                             }
 
                             RequestLocal.get();
                             ProfileLogger.push("start_request_handler_worker");
                             try {
                                 if (obj instanceof byte[]) {
                                     try {
                                         ProfileLogger.push("deserialize_request");
                                         obj = messageFactory.deserializeRequest((byte[]) obj);
                                     } finally {
                                         ProfileLogger.pop();
                                     }
                                 }
                                 if (obj instanceof IRequest) {
                                     handleRequest((IRequest) obj);
                                 }
                             } catch (Exception e) {
                                 try {
                                     LOGGER.error(e.getMessage(), e);
                                     IResponse response = ResponseUtils.response500((IRequest) obj,
                                             e.getMessage(), e);
                                     topicPublisher.publishToTopic(response);
                                 } catch (Exception ex) {
                                     LOGGER.error(e.getMessage(), e);
                                 }
                             } finally {
                                 try {
                                     ProfileLogger.pop();
                                 } finally {
                                     RequestLocal.remove();
                                 }
                             }
                         }
                     }
                 }
             };
             t.setDaemon(true);
             t.start();
             workerThreads[i - 1] = t;
         }
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Hetty request handler workers: " + numWorkers + " / Read timeout: "
                     + readTimeoutMillisecs + " / Write timeout: " + writeTimeoutMillisecs);
         }
     }
 }
