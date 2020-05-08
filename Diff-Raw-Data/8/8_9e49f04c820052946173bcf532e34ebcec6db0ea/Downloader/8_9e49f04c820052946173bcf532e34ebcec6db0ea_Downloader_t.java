 package ecologylab.bigsemantics.downloaderpool;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ecologylab.bigsemantics.httpclient.BasicResponseHandler;
 import ecologylab.bigsemantics.httpclient.HttpClientFactory;
 import ecologylab.bigsemantics.httpclient.ModifiedHttpClientUtils;
 import ecologylab.concurrent.DownloadMonitor;
 import ecologylab.concurrent.Site;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.formatenums.StringFormat;
 
 /**
  * A downloader that actively retrieves tasks from the controller, downloads pages, and sends
  * results back to the controller. The downloader gets all the necessary information, such as the
  * task URL, user agent, and time to wait between requests.
  * 
  * @author quyin
  * 
  */
 public class Downloader extends Routine implements DownloaderConfigNames
 {
 
   private static Logger logger = LoggerFactory.getLogger(Downloader.class);
 
   // configs:
 
   private String        name;
 
   private String        controllerBaseUrl;
 
   private int           numDownloadThreads;
 
   private int           maxTaskCount;
 
   // collaborating objects:
 
   DownloadMonitor<Page> downloadMonitor;
 
   SimpleSiteTable       siteTable;
 
   HttpClientFactory     clientFactory;
 
   public Downloader(Configuration configs)
   {
     super();
 
     this.name = configs.getString(NAME, null);
     String[] baseUrls = configs.getStringArray(CONTROLLER_BASE_URL);
     this.controllerBaseUrl = findWorkingControllerBaseUrl(baseUrls);
     this.numDownloadThreads = configs.getInt(NUM_DOWNLOADING_THREADS, 4);
     this.maxTaskCount = configs.getInt(MAX_TASK_COUNT, 10);
 
     downloadMonitor =
         new DownloadMonitor<Page>("DownloadMonitor[" + name + "]", numDownloadThreads);
     siteTable = new SimpleSiteTable();
     clientFactory = new HttpClientFactory();
 
     setReady();
     logger.info("Downloader[{}] is constructed and ready.", name);
   }
 
   protected String findWorkingControllerBaseUrl(String... baseUrls)
   {
     for (String baseUrl : baseUrls)
     {
       String echoUrl = baseUrl + "echo/get";
       if (tryEcho(echoUrl))
      {
        logger.info("Connected to controller using " + echoUrl);
         return baseUrl;
      }
      else
      {
        logger.info("No controller found using " + echoUrl);
      }
     }
     return null;
   }
 
   private boolean tryEcho(String echoUrl)
   {
     Map<String, String> params = new HashMap<String, String>();
     String ts = String.valueOf(System.currentTimeMillis());
     String msg = DPoolUtils.base64urlEncode(DPoolUtils.hashToBytes(ts));
     params.put("msg", msg);
     HttpGet get = ModifiedHttpClientUtils.generateGetRequest(echoUrl, params);
     HttpClient client = new DefaultHttpClient();
     BasicResponse resp = null;
     try
     {
       resp = client.execute(get, new BasicResponseHandler());
     }
     catch (ClientProtocolException e)
     {
       logger.info("Protocol error when trying " + echoUrl);
     }
     catch (IOException e)
     {
       logger.info("I/O error when trying " + echoUrl);
     }
     if (resp != null
         && resp.getHttpRespCode() == HttpStatus.SC_OK
         && resp.getContent().contains(msg))
     {
       return true;
     }
     return false;
   }
 
   public String getName()
   {
     return name;
   }
 
   /**
    * Request tasks from the controller. The request will consider ongoing work, in order not to hit
    * the same website too frequently.
    * 
    * @return The list of retrieved tasks. If failed, null.
    */
   public List<Task> requestTasks()
   {
     DownloaderRequest req = formRequest();
 
     Map<String, String> params = new HashMap<String, String>();
     params.put("id", this.name);
     String blist = req.getBlacklistString();
     if (blist != null)
     {
       params.put("blacklist", blist);
     }
     params.put("ntask", String.valueOf(this.maxTaskCount));
     String assignUrl = controllerBaseUrl + "task/assign.xml";
     HttpGet get = ModifiedHttpClientUtils.generateGetRequest(assignUrl, params);
     // logger.info("request for tasks: " + get.getURI()); // -- hated!
 
     int status = -1;
     try
     {
       AbstractHttpClient client = clientFactory.get();
       BasicResponse result = client.execute(get, new BasicResponseHandler());
       status = result.getHttpRespCode();
       String content = result.getContent();
       if (status == HttpStatus.SC_OK)
       {
         AssignedTasks assignedTasks = (AssignedTasks) DPoolUtils.deserialize(content,
                                                                         MessageScope.get(),
                                                                         StringFormat.XML);
         return assignedTasks.getTasks();
       }
       else
       {
         logger.error("Status message: " + result.getHttpRespMsg());
         logger.error("Content:\n" + content);
       }
     }
     catch (ClientProtocolException e)
     {
       logger.error("Exception when accessing " + get.getURI(), e);
       e.printStackTrace();
       get.abort();
     }
     catch (IOException e)
     {
       logger.error("Exception when accessing " + get.getURI(), e);
       e.printStackTrace();
       get.abort();
     }
 
     logger.error("Empty response [status code: {}] from {}", status, get.getURI());
     return null;
   }
 
   /**
    * Form a request to the controller to retrieve tasks.
    * 
    * @return The formed DownloaderRequest object.
    */
   public DownloaderRequest formRequest()
   {
     DownloaderRequest req = new DownloaderRequest();
     req.setMaxTaskCount(maxTaskCount);
     Set<Site> busySites = siteTable.getBusySites();
     for (Site site : busySites)
     {
       req.addToBlacklist(site.domain());
     }
     return req;
   }
 
   public void routineBody()
   {
     if (downloadMonitor.toDownloadSize() <= 0)
     {
       List<Task> tasks = requestTasks();
       updateSiteIntervals(tasks);
       createAndQueuePages(tasks);
     }
   }
 
   /**
    * Update waiting interval info for retrieved tasks.
    * 
    * @param tasks
    *          The retrieved tasks.
    */
   protected void updateSiteIntervals(List<Task> tasks)
   {
     if (tasks != null)
     {
       for (Task task : tasks)
       {
         ParsedURL purl = task.getPurl();
         if (purl != null)
         {
           String domain = purl.domain();
           siteTable.updateSiteDownloadInterval(domain, task.getDomainInterval());
         }
       }
     }
   }
 
   /**
    * Create Page objects, which represent the pages to download, from retrieved tasks, and queue
    * them to the DownloadMonitor for downloading.
    * 
    * @param tasks
    */
   protected void createAndQueuePages(List<Task> tasks)
   {
     if (tasks != null)
     {
       for (Task task : tasks)
       {
         if (task.getId() != null && task.getPurl() != null)
         {
           logger.info("creating Page object for task[{}][{}]...", task.getId(), task.getPurl());
           Page pageToDownload = createPage(task);
           queuePageToDownload(pageToDownload, task);
         }
       }
     }
   }
 
   /**
    * Page object factory method.
    * 
    * @param task
    * @return
    */
   protected Page createPage(Task task)
   {
     Page page = new Page(task.getId(), task.getPurl(), task.getUserAgent());
     page.clientFactory = this.clientFactory;
     page.siteTable = this.siteTable;
     return page;
   }
 
   /**
    * Queue the given Page object to DownloadMonitor.
    * 
    * @param pageToDownload
    * @param associatedTask
    */
   protected void queuePageToDownload(Page pageToDownload, Task associatedTask)
   {
     DownloaderResponder responder = createDownloaderResponder(associatedTask);
     downloadMonitor.download(pageToDownload, responder);
   }
 
   /**
    * DownloadResponder object factory method.
    * 
    * @param associatedTask
    * @return
    */
   protected DownloaderResponder createDownloaderResponder(Task associatedTask)
   {
     String reportUrl = controllerBaseUrl + "page/report";
     DownloaderResponder downloaderResponder = new DownloaderResponder(reportUrl, associatedTask);
     downloaderResponder.downloader = this;
     return downloaderResponder;
   }
 
   @Override
   public void stop()
   {
     downloadMonitor.stop();
     super.stop();
   }
 
   public static void main(String[] args) throws ConfigurationException, InterruptedException
   {
     Configuration configs = new PropertiesConfiguration("dpool.properties");
     Downloader d = new Downloader(configs);
     d.start();
     d.join();
   }
 
 }
