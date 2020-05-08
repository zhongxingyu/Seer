 package de.otto.jobstore.service;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import de.otto.jobstore.common.RemoteJob;
 import de.otto.jobstore.common.RemoteJobStatus;
 import de.otto.jobstore.service.exception.JobException;
 import de.otto.jobstore.service.exception.JobExecutionException;
 import de.otto.jobstore.service.exception.RemoteJobAlreadyRunningException;
 import de.otto.jobstore.service.exception.RemoteJobNotRunningException;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.codehaus.jettison.json.JSONException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.core.MediaType;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.nio.charset.Charset;
 
 /**
  * This class triggers the execution of jobs on a remote server.
  * The scripts for execution are sent within the request body.
  *
  * The remote server has to expose a rest endpoint.
  * The multipart request sent to the endpoint consists of two parts:
  *
  * 1) A binary part containing the tar file:
  *     Content-Disposition: form-data; name="scripts"; filename="scripts.tar.gz"
  *     Content-Type: application/octet-stream
  *     Content-Transfer-Encoding: binary
  * 2) A part containing the JSON formatted parameters:
  *     Content-Disposition: form-data; name="params"
  *     Content-Type: application/json; charset=UTF-8
  *     Content-Transfer-Encoding: 8bit
  */
 public class RemoteJobExecutorWithScriptTransferService implements RemoteJobExecutor {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(RemoteJobExecutorWithScriptTransferService.class);
     private static final String JOB_SCRIPT_DIRECTORY = "/jobs";
     private final RemoteJobExecutorStatusRetriever remoteJobExecutorStatusRetriever;
     private String jobExecutorUri;
     private Client client;
     private HttpClient httpclient =  new DefaultHttpClient();
     private ScriptArchiver scriptArchiver = new ScriptArchiver();
 
     public RemoteJobExecutorWithScriptTransferService(String jobExecutorUri) {
         this.jobExecutorUri = jobExecutorUri;
 
         // since Flask (with WSGI) does not suppport HTTP 1.1 chunked encoding, turn it off
         //    see: https://github.com/mitsuhiko/flask/issues/367
         final ClientConfig cc = new DefaultClientConfig();
         cc.getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, null);
         this.client = Client.create(cc);
         remoteJobExecutorStatusRetriever = new RemoteJobExecutorStatusRetriever(client);
     }
 
     public URI startJob(final RemoteJob job) throws JobException {
         final String startUrl = jobExecutorUri + job.name + "/start";
         HttpResponse response = null;
         try {
             LOGGER.info("ltag=RemoteJobExecutorService.startJob Going to start job: {} ...", startUrl);
 
             byte[] tarAsByteArray = createTar(job);
 
             HttpPost httpPost = createRemoteExecutorMultipartRequest(job, startUrl, tarAsByteArray);
 
             response = executeRequest(httpPost);
 
             int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 201 && statusCode != 303) {
                throw new JobExecutionException("Unable to contact remote executor. Server returned status " + statusCode);
            }
             String link = response.getFirstHeader("Link").getValue();
             if (statusCode == 201) {
                 return createJobUri(link);
             } else if (statusCode == 303) {
                 throw new RemoteJobAlreadyRunningException("Remote job is already running, url=" + startUrl, createJobUri(link));
             }
             throw new JobExecutionException("Unable to start remote job: url=" + startUrl + " rc=" + statusCode);
         } catch (JSONException e) {
             throw new JobExecutionException("Could not create JSON object: " + job, e);
         } catch (UniformInterfaceException | ClientHandlerException  e) {
             throw new JobExecutionException("Problem while starting new job: url=" + startUrl, e);
         } finally {
             closeResponseConnection(response);
         }
     }
 
     private void closeResponseConnection(HttpResponse response) {
         if (response != null) {
             try {
                 EntityUtils.consume(response.getEntity());
             } catch (IOException e) {
                 LOGGER.warn("Could not close response connection", e);
             }
         }
     }
 
     private HttpResponse executeRequest(HttpPost httpPost) throws JobExecutionException {
         HttpResponse response;
         try {
             response = httpclient.execute(httpPost);
         } catch (IOException e) {
             throw new JobExecutionException("Could not post scripts", e);
         }
         return response;
     }
 
     private byte[] createTar(RemoteJob job) throws JobExecutionException {
         byte[] tarAsByteArray;
         try {
             tarAsByteArray = scriptArchiver.createArchive(JOB_SCRIPT_DIRECTORY, job.name);
         } catch (Exception e) {
             throw new JobExecutionException("Could not create tar with job scripts (folder: " + job.name + ")", e);
         }
         return tarAsByteArray;
     }
 
     public HttpPost createRemoteExecutorMultipartRequest(RemoteJob job, String startUrl, byte[] tarByteArray) throws JSONException, JobExecutionException {
         HttpPost httpPost = new HttpPost(startUrl);
 
         ByteArrayBody tarBody = new ByteArrayBody(tarByteArray, "scripts.tar.gz");
         MultipartEntity multipartEntity = new MultipartEntity();
         multipartEntity.addPart("scripts", tarBody);
         try {
             multipartEntity.addPart("params", new StringBody(job.toJsonObject().toString(), MediaType.APPLICATION_JSON, Charset.forName("UTF-8")));
         } catch (UnsupportedEncodingException e) {
             throw new JobExecutionException("Could not generate json", e);
         }
         httpPost.setEntity(multipartEntity);
         httpPost.setHeader("Connection", "close");
         httpPost.setHeader("User-Agent", "RemoteJobExecutorService");
         return httpPost;
     }
 
     public void stopJob(URI jobUri) throws JobException {
         final String stopUrl = jobUri + "/stop";
         try {
             LOGGER.info("ltag=RemoteJobExecutorService.stopJob Going to stop job: {} ...", stopUrl);
             client.resource(stopUrl).header("Connection", "close").post();
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getStatus() == 403) {
                 throw new RemoteJobNotRunningException("Remote job is not running: url=" + stopUrl);
             }
             throw e;
         }
     }
 
     public RemoteJobStatus getStatus(final URI jobUri) {
         return remoteJobExecutorStatusRetriever.getStatus(jobUri);
     }
 
     public boolean isAlive() {
         return remoteJobExecutorStatusRetriever.isAlive(jobExecutorUri);
     }
 
     // ~
 
     private URI createJobUri(String path) {
         return URI.create(jobExecutorUri).resolve(path);
     }
 
 }
