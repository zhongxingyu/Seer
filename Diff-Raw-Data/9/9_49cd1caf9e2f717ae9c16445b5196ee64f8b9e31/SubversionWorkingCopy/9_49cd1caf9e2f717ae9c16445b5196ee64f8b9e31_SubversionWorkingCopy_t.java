 /**
  *** This software is licensed under the GNU Affero General Public License, version 3.
  *** See http://www.gnu.org/licenses/agpl.html for full details of the license terms.
  *** Copyright 2012 Andrew Heald.
  */
 
 package uk.org.sappho.applications.configuration.service.vcs;
 
 import uk.org.sappho.applications.configuration.service.ConfigurationException;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SubversionWorkingCopy {
 
     private final static Map<String, Pattern> patterns = new HashMap<String, Pattern>();
 
     static {
         patterns.put("vcs.revision", Pattern.compile("^Revision: ([0-9]*)$"));
         patterns.put("vcs.last.changed.revision", Pattern.compile("^Last Changed Rev: ([0-9]*)$"));
         patterns.put("vcs.last.changed.date", Pattern.compile("^Last Changed Date: (.+)$"));
         patterns.put("vcs.last.changed.author", Pattern.compile("^Last Changed Author: (.+)$"));
         patterns.put("vcs.repository.location", Pattern.compile("^URL: (.+)$"));
     };
 
     public void update(String workingCopyPath, String filename, Map<String, String> workingCopyProperties) throws ConfigurationException {
 
         try {
             File directory = new File(workingCopyPath);
             Process process = Runtime.getRuntime().exec("svn update --non-interactive --quiet --accept theirs-full " + filename, null, directory);
            if (process.waitFor() != 0) {
                throw new ConfigurationException("Unable to update from Subversion server");
            }
             process.destroy();
             if (workingCopyProperties != null) {
                 process = Runtime.getRuntime().exec("svn info --non-interactive " + filename, null, directory);
                if (process.waitFor() != 0) {
                    throw new ConfigurationException("Unable to get status from Subversion server");
                }
                 BufferedReader info = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 String line;
                 while ((line = info.readLine()) != null) {
                     for (String propertyKey : patterns.keySet()) {
                         Matcher matcher = patterns.get(propertyKey).matcher(line);
                         if (matcher.matches()) {
                             workingCopyProperties.put(propertyKey, matcher.group(1));
                             break;
                         }
                     }
                 }
                 process.destroy();
             }
         } catch (Exception exception) {
             throw new ConfigurationException("Unable to update from Subversion server", exception);
         }
     }
 }
