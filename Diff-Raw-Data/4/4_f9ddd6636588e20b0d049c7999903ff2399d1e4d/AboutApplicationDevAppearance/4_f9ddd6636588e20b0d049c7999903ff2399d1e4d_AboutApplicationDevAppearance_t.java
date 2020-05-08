 package org.iplantc.de.client.views.windows;
 
 import org.iplantc.de.client.models.AboutApplicationData;
 import org.iplantc.de.resources.client.messages.I18N;
 
 import com.google.common.base.Strings;
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.user.client.Window;
 
 /**
  * The Dev mode Appearance for the About Window's contents and layout, with additional build info.
  * 
  * @author psarando
  * 
  */
 public class AboutApplicationDevAppearance implements AboutApplicationAppearance {
 
     interface DevTemplate extends SafeHtmlTemplates {
         @SafeHtmlTemplates.Template("<p style='font-style:italic;'> {9} </p>"
                 + "<p>Release: {0}</p>"
                 + "<p>Build: {1}</p>"
                 + "<p>Build #: {2}</p>"
                 + "<p>Build ID: {3}</p>"
                 + "<p>Build Commit: {4}@{5}</p>"
                 + "<p>Build JDK: {6}</p>"
                 + "<p>User Agent: {7}</p>"
                 + "<p style='font-weight:700'> {8} </p>")
         SafeHtml about(String release, String build, String buildNumber, String buildId,
                 String buildBranch, String buildCommit, String buildJdk, String userAgent,
                 SafeHtml copyright, SafeHtml nsfProject);
 
     }
 
     private final DevTemplate template = GWT.create(DevTemplate.class);
 
     @Override
     public SafeHtml about(AboutApplicationData data, String userAgent, SafeHtml copyright,
             SafeHtml nsfProject) {
         return template.about(Strings.nullToEmpty(data.getReleaseVersion()),
                 Strings.nullToEmpty(data.getBuild()), Strings.nullToEmpty(data.getBuildNumber()),
                Strings.nullToEmpty(data.getBuildId()), Strings.nullToEmpty(data.getBuildBranch()),
                Strings.nullToEmpty(data.getBuildCommit()), Strings.nullToEmpty(data.getBuildJdk()),
                 Window.Navigator.getUserAgent(), I18N.DISPLAY.projectCopyrightStatement(),
                 I18N.DISPLAY.nsfProjectText());
     }
 }
