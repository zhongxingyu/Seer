 /**
  * 
  */
 
 package org.nuxeo.vocapia.service;
 
 import java.net.URI;
 import java.security.KeyManagementException;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.conn.ssl.TrustStrategy;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.nuxeo.ecm.core.api.DocumentLocation;
 import org.nuxeo.ecm.core.work.api.Work;
 import org.nuxeo.ecm.core.work.api.Work.State;
 import org.nuxeo.ecm.core.work.api.WorkManager;
 import org.nuxeo.ecm.core.work.api.WorkManager.Scheduling;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.model.ComponentContext;
 import org.nuxeo.runtime.model.DefaultComponent;
 
 /**
  * Schedule the transcription of video and audio files and monitor progress. As
  * the processing is asynchronous, the results is stored directly on the
  * document in a dedicated facet's schema.
  */
 public class TranscriptionService extends DefaultComponent {
 
     protected static final String MEDIA_BLOB_PATH = "file:content";
 
     protected HttpClient httpClient;
 
     protected URI serviceUrl;
 
     protected String username;
 
     protected String password;
 
     protected void initHttpClient(URI serviceUrl)
             throws KeyManagementException, UnrecoverableKeyException,
             NoSuchAlgorithmException, KeyStoreException {
 
         // Create and initialize a scheme registry
         SchemeRegistry schemeRegistry = new SchemeRegistry();
         if ("https".equals(serviceUrl.getScheme())) {
             // Trust self signed certificates
             TrustStrategy blindTrust = new TrustStrategy() {
                 @Override
                 public boolean isTrusted(X509Certificate[] chain,
                         String authType) throws CertificateException {
                     return true;
                 }
             };
             SSLSocketFactory sslsf = new SSLSocketFactory(blindTrust,
                     SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
             schemeRegistry.register(new Scheme(serviceUrl.getScheme(),
                     serviceUrl.getPort(), sslsf));
         } else {
             schemeRegistry.register(new Scheme(serviceUrl.getScheme(),
                     serviceUrl.getPort(), PlainSocketFactory.getSocketFactory()));
         }
 
         // Create an HttpClient with the ThreadSafeClientConnManager.
         // This connection manager must be used if more than one thread will
         // be using the HttpClient.
         ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
                 schemeRegistry);
         httpClient = new DefaultHttpClient(cm);
     }
 
     @Override
     public void activate(ComponentContext context) throws Exception {
         String url = System.getenv("NUXEO_VOCAPIA_SERVICE_URL");
         if (url == null || url.isEmpty()) {
             throw new RuntimeException(
                     "NUXEO_VOCAPIA_SERVICE_URL environment variable is undefined");
         }
         serviceUrl = new URI(url);
         username = System.getenv("NUXEO_VOCAPIA_SERVICE_USERNAME");
         password = System.getenv("NUXEO_VOCAPIA_SERVICE_PASSWORD");
         initHttpClient(serviceUrl);
     }
 
     @Override
     public void deactivate(ComponentContext context) throws Exception {
         WorkManager workManager = Framework.getLocalService(WorkManager.class);
         if (workManager != null) {
             workManager.shutdownQueue(
                     workManager.getCategoryQueueId(TranscriptionWork.CATEGORY_SPEECH_TRANSCRIPTION),
                     10, TimeUnit.SECONDS);
         }
         httpClient = null;
        serviceUrl = null;
        username = null;
        password = null;
     }
 
     public void launchTranscription(DocumentLocation docLoc) {
        if (serviceUrl == null) {
            throw new RuntimeException(
                    "TranscriptionService failed to initialize properly.");
        }
         WorkManager workManager = Framework.getLocalService(WorkManager.class);
         workManager.schedule(makeWork(docLoc),
                 Scheduling.IF_NOT_RUNNING_OR_SCHEDULED);
     }
 
     private TranscriptionWork makeWork(DocumentLocation docLoc) {
         return new TranscriptionWork(docLoc, MEDIA_BLOB_PATH, httpClient,
                 serviceUrl, username, password);
     }
 
     public TranscriptionStatus getTranscriptionStatus(DocumentLocation docLoc) {
         WorkManager workManager = Framework.getLocalService(WorkManager.class);
         Work work = makeWork(docLoc);
         int[] pos = new int[1];
         work = workManager.find(work, null, true, pos);
         if (work == null) {
             return null;
         } else if (work.getState() == State.SCHEDULED) {
             String queueId = workManager.getCategoryQueueId(TranscriptionWork.CATEGORY_SPEECH_TRANSCRIPTION);
             int queueSize = workManager.listWork(queueId, State.SCHEDULED).size();
             return new TranscriptionStatus(
                     TranscriptionStatus.STATUS_TRANSCRIPTION_QUEUED,
                     pos[0] + 1, queueSize);
         } else { // RUNNING
             return new TranscriptionStatus(work.getStatus(), 0, 0);
         }
     }
 }
