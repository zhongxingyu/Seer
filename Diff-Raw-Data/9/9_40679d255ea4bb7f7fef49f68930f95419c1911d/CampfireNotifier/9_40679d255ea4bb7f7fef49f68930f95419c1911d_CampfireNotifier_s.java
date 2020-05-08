 package hudson.plugins.campfire;
 
 import hudson.tasks.Notifier;
 import hudson.tasks.BuildStepMonitor;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.regex.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.xml.sax.SAXException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 public class CampfireNotifier extends Notifier {
 
   private static final Logger LOGGER = Logger.getLogger(CampfireNotifier.class.getName());
   private transient Campfire campfire;
   private Room room;
   private boolean summaryPasteEnabled;
   private String summaryPasteRegexs;
 
   @DataBoundConstructor
   public CampfireNotifier(boolean summaryPasteEnabled, String summaryPasteRegexs) throws IOException {
     this.summaryPasteEnabled = summaryPasteEnabled;
     this.summaryPasteRegexs = summaryPasteRegexs;
     initialize();
   }

   private void initialize() throws IOException {
     DescriptorImpl descriptor = ((DescriptorImpl) getDescriptor());
     campfire = new Campfire(descriptor.getSubdomain(), descriptor.getToken(), descriptor.getSsl());
     try {
       this.room = campfire.findOrCreateRoomByName(descriptor.getRoom());
     } catch (IOException e) {
       throw new IOException("Cannot join room: " + e.getMessage());
     } catch (ParserConfigurationException e) {
       throw new IOException("Cannot join room: " + e.getMessage());
     } catch (XPathExpressionException e) {
       throw new IOException("Cannot join room: " + e.getMessage());
     } catch (SAXException e) {
       throw new IOException("Cannot join room: " + e.getMessage());
     }
   }
 
   public boolean isSummaryPasteEnabled() {
     return summaryPasteEnabled;
   }
 
   public String getSummaryPasteRegexs() {
     return summaryPasteRegexs;
   }
 
   public BuildStepMonitor getRequiredMonitorService() {
     return BuildStepMonitor.BUILD;
   }
 
   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
           BuildListener listener) throws InterruptedException, IOException {
     publish(build);
     return true;
   }
 
   private void publish(AbstractBuild<?, ?> build) throws IOException {
     String message = build.getProject().getName() + " build " + build.getDisplayName() + ": " + build.getResult().toString();
     String hudsonUrl =  ((DescriptorImpl) getDescriptor()).getHudsonUrl();
     if (hudsonUrl != null && hudsonUrl.length() > 1) {
       message = message + " (" + hudsonUrl + build.getUrl() + ")";
     }
     room.speak(message);
     if (summaryPasteEnabled) {
       room.paste(summarizeBuildLog(build));
     }
   }
   private String summarizeBuildLog(AbstractBuild<?, ?> build) {
     final String[] linesToCapture = summaryPasteRegexs.split("[\\r?\\n]+");
     // These use to be hard coded.  The regexes below are what we use to
     // capture rpec, junit and cucumber results
     //  		final String[] linesToCapture={
     //  			"^Commencing build",
     //  			"^\\+ rake spec",
     //  			"^Finished in", 
     //  			"^[0-9][0-9]* examples", 
     //  			"^\\+ script\\/js_specs", 
     //  			"^ran ", 
     //  			"^Finished\\: ",
     //  			"^\\+ rake cucumber",
     //  			"[0-9][0-9]* scenarios",
     //  			"[0-9][0-9]* steps"
     //  		};
     final File logFile = build.getLogFile();
     final StringBuffer summary = new StringBuffer();
     try {
       BufferedReader in = new BufferedReader(new FileReader(logFile.getAbsolutePath()));
       boolean started = false;
       String str;
       while ((str = in.readLine()) != null) {
         for (String s : linesToCapture) {
           Pattern p = Pattern.compile(s);
           Matcher m = p.matcher(str);
           if (m.find()) {
             summary.append(stripColorization(str) + "\n");
             break;
           }
         }
       }
       in.close();
     } catch (IOException e) {
       // oh yeah, well what do I do about it
     }
     return summary.toString();
   }
 
   private String stripColorization(String in) {
     Pattern p = Pattern.compile("\\[[0-9]*m");
     Matcher m = p.matcher(in);
     return m.replaceAll("");
 
   }
 }
