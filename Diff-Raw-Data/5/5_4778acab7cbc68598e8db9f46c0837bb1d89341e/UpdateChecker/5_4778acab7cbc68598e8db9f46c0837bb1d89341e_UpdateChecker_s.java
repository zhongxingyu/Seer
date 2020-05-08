 package org.specksensor.applications;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.HashSet;
 import java.util.PropertyResourceBundle;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import edu.cmu.ri.createlab.util.StandardVersionNumber;
 import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpResponseException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.fluent.Request;
 import org.apache.log4j.Logger;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /**
  * <p>
  * <code>UpdateChecker</code> asynchronously checks the server for current version number, providing a means for users
  * to determine whether an update is available.
  * </p>
  *
  * @author Chris Bartley (bartley@cmu.edu)
  */
 public final class UpdateChecker
    {
    private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(UpdateChecker.class.getName());
    private static final String CURRENT_VERSION_NUMBER_URL = RESOURCES.getString("url.version-number");
    private static final String USER_AGENT = RESOURCES.getString("user-agent");
 
    private static final int TIMEOUT_MILLIS = 2000;
 
    private static final Logger LOG = Logger.getLogger(UpdateChecker.class);
 
    public interface UpdateCheckResultListener
       {
       void handleUpdateCheckResult(final boolean wasCheckSuccessful,
                                    final boolean isUpdateAvailable,
                                    @Nullable final StandardVersionNumber versionNumberOfUpdate);
       }
 
    @NotNull
    private final StandardVersionNumber currentVersionNumber;
    private final ExecutorService executorService = Executors.newCachedThreadPool(new DaemonThreadFactory(this.getClass().getSimpleName()));
    private final Set<UpdateCheckResultListener> updateCheckResultListeners = new HashSet<UpdateCheckResultListener>();
 
    public UpdateChecker(@NotNull final StandardVersionNumber currentVersionNumber)
       {
       this.currentVersionNumber = currentVersionNumber;
       }
 
    public void addUpdateCheckResultListener(@Nullable final UpdateCheckResultListener updateCheckResultListener)
       {
       if (updateCheckResultListener != null)
          {
          updateCheckResultListeners.add(updateCheckResultListener);
          }
       }
 
    public void removeUpdateCheckHandler(@Nullable final UpdateCheckResultListener updateCheckResultListener)
       {
       if (updateCheckResultListener != null)
          {
          updateCheckResultListeners.remove(updateCheckResultListener);
          }
       }
 
    public void checkForUpdate()
       {
       if (!updateCheckResultListeners.isEmpty())
          {
          try
             {
             LOG.debug("UpdateChecker.checkForUpdate(): running update check");
             executorService.execute(
                   new Runnable()
                   {
                   @Override
                   public void run()
                      {
                      final StandardVersionNumber latestVersionNumber = getLatestVersionNumber();
                      final boolean wasCheckSuccessful = latestVersionNumber != null;
                      final boolean isUpdateAvailable = wasCheckSuccessful && !currentVersionNumber.equals(latestVersionNumber);
 
                      if (LOG.isDebugEnabled())
                         {
                         LOG.debug("UpdateChecker.checkForUpdate(): wasCheckSuccessful = [" + wasCheckSuccessful + "], isUpdateAvailable = [" + isUpdateAvailable + "], latestVersionNumber = [" + latestVersionNumber + "]");
                         }
 
                      // notify all listeners
                      for (final UpdateCheckResultListener updateCheckResultListener : updateCheckResultListeners)
                         {
                         updateCheckResultListener.handleUpdateCheckResult(wasCheckSuccessful,
                                                                           isUpdateAvailable,
                                                                           latestVersionNumber);
                         }
                      }
                   });
             }
          catch (Exception e)
             {
             LOG.error("Exception while trying to execute the update check", e);
             }
          }
       }
 
    @Nullable
    private StandardVersionNumber getLatestVersionNumber()
       {
       // Execute a GET with timeout settings and return response content as String.
       String versionNumber = null;
       try
          {
          // Taken from http://hc.apache.org/httpcomponents-client-4.3.x/tutorial/html/fluent.html
          versionNumber = Request.Get(CURRENT_VERSION_NUMBER_URL)
                .setCacheControl("no-cache")
                .userAgent(USER_AGENT)
                .connectTimeout(TIMEOUT_MILLIS)
                .socketTimeout(TIMEOUT_MILLIS)
                .execute().handleResponse(
                      new ResponseHandler<String>()
                      {
 
                      public String handleResponse(final HttpResponse response) throws IOException
                         {
                         final StatusLine statusLine = response.getStatusLine();
                         if (statusLine.getStatusCode() >= 300)
                            {
                            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                            }
 
                         final HttpEntity entity = response.getEntity();
                         if (entity == null)
                            {
                            throw new ClientProtocolException("Response contains no content");
                            }
 
                         // Get the content and convert to a string (http://stackoverflow.com/a/309448)
                         final InputStream contentStream = entity.getContent();
                         final StringWriter writer = new StringWriter();
                         IOUtils.copy(contentStream, writer);
                        return writer.toString();
                         }
                      });
          }
       catch (HttpResponseException e)
          {
          LOG.error("UpdateChecker.getLatestVersionNumber(): HttpResponseException while trying to get the latest version number: " + e);
          }
       catch (IOException e)
          {
          LOG.error("UpdateChecker.getLatestVersionNumber(): IOException while trying to get the latest version number: " + e);
          }
       catch (Exception e)
          {
          LOG.error("UpdateChecker.getLatestVersionNumber(): Exception while trying to get the latest version number: " + e);
          }
 
       return StandardVersionNumber.parse(versionNumber);
       }
    }
