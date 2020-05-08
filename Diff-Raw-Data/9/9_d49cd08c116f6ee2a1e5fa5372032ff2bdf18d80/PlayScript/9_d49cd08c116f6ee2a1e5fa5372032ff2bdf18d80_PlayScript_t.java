 package uk.co.mdjcox.sagetvcatchup.plugins;
 
 import com.google.inject.assistedinject.Assisted;
 import com.google.inject.assistedinject.AssistedInject;
 import uk.co.mdjcox.logger.LoggerInterface;
 import uk.co.mdjcox.model.Episode;
 import uk.co.mdjcox.model.Recording;
 import uk.co.mdjcox.model.Source;
 import uk.co.mdjcox.sagetvcatchup.Recorder;
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
 public class PlayScript extends Script {
 
     @AssistedInject
    public PlayScript(LoggerInterface logger, @Assisted String base, HtmlUtilsInterface htmlUtils, DownloadUtilsInterface downloadUtils, OsUtilsInterface osUtils) {
         super(logger, base + File.separator + "playEpisode.groovy", htmlUtils, downloadUtils, osUtils);
     }
 
     public void play(Recording recording) {
         try {
             getLogger().info("Starting playback of " + recording);
             call("recording", recording, "", "logger", getLogger());
             getLogger().info("Playing episode " + recording);
         } catch (Throwable e) {
             getLogger().severe("Unable to playback: " + recording, e);
         }
     }
 
 }
