 package org.sonar.ide.idea.utils;
 
 import com.intellij.openapi.project.Project;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.ide.idea.SonarWorkspaceSettingsComponent;
 import org.sonar.ide.shared.SonarProperties;
 import org.sonar.wsclient.Server;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.connectors.HttpClient4Connector;
 
 /**
  * @author Evgeny Mandrikov
  */
 public final class SonarUtils {
   private static final Logger LOG = LoggerFactory.getLogger(SonarUtils.class);
 
   /**
    * Hide utility-class constructor.
    */
   private SonarUtils() {
   }
 
   public static SonarProperties getSonarSettings(Project project) {
     return SonarWorkspaceSettingsComponent.getInstance(project).getSettings();
   }
 
   public static Sonar getSonar(Project project) {
     Server server = getSonarSettings(project).getServer();
     LOG.debug("Sonar server: {}", server.getHost());
     return new Sonar(new HttpClient4Connector(server));
   }
 }
