 package uk.co.mdjcox.sagetvcatchup.plugins;
 
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.assistedinject.AssistedInject;
 import uk.co.mdjcox.logger.LoggerInterface;
 import uk.co.mdjcox.model.Recording;
 import uk.co.mdjcox.utils.DownloadUtilsInterface;
 import uk.co.mdjcox.utils.HtmlUtilsInterface;
 import uk.co.mdjcox.utils.OsUtilsInterface;
 
 import java.io.File;
 
 /**
  * Created with IntelliJ IDEA.
  * User: michael
  * Date: 28/03/13
  * Time: 17:44
  * To change this template use File | Settings | File Templates.
  */
 public class StopScript extends Script {
 
     @AssistedInject
    public StopScript(LoggerInterface logger, @Assisted("base") String base, HtmlUtilsInterface htmlUtils, DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils) {
         super(logger, base + File.separator + "stopEpisode.groovy", htmlUtils, downloadUtils, osUtils);
     }
 
     public void stop(Recording recording) {
         try {
             getLogger().info("Stopping playback of " + recording);
             call("recording", recording, "", "logger", getLogger());
             getLogger().info("Stopped episode " + recording);
         } catch (Throwable e) {
             getLogger().severe("Unable to stop: " + recording, e);
         }
     }
 
 }
