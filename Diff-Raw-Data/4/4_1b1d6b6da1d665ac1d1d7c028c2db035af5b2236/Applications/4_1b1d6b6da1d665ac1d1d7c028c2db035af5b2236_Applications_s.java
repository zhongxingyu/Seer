 /**
  *** This software is licensed under the GNU General Public License, version 3.
  *** See http://www.gnu.org/licenses/gpl.html for full details of the license terms.
  *** Copyright 2012 Andrew Heald.
  */
 
 package uk.org.sappho.applications.transcript.service.registry;
 
 import com.google.inject.Inject;
 import uk.org.sappho.applications.transcript.service.TranscriptException;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.Arrays;
 import java.util.regex.Pattern;
 
 public class Applications {
 
     private WorkingCopy workingCopy;
 
     private static final Pattern ALL = Pattern.compile("^[^\\.].*$");
 
     @Inject
     public Applications(WorkingCopy workingCopy) {
 
         this.workingCopy = workingCopy;
     }
 
     public String[] getApplicationNames(String environment) throws TranscriptException {
 
         return getApplicationNames(environment, ALL);
     }
 
     public String[] getApplicationNames(String environment, final Pattern pattern) throws TranscriptException {
 
         String[] applications = workingCopy.getUpToDatePath(environment).list(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                boolean found = new File(dir, name).isFile();
                if (found && name.endsWith(".json")) {
                     name = name.substring(0, name.length() - 5);
                     found = pattern.matcher(name).matches();
                 }
                 return found;
             }
         });
         if (applications != null) {
             for (int i = 0; i < applications.length; i++) {
                 String name = applications[i];
                 applications[i] = name.substring(0, name.length() - 5);
             }
             Arrays.sort(applications);
         } else {
             applications = new String[0];
         }
         return applications;
     }
 }
