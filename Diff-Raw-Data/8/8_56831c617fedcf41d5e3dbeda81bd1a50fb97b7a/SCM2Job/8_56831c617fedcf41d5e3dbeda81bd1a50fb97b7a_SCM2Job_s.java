 package com.unitedinternet.jenkins.plugins.scm2job;
 
 import hudson.Extension;
 import hudson.model.Hudson;
 import hudson.model.RootAction;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.eclipse.jgit.transport.RemoteConfig;
 import org.eclipse.jgit.transport.URIish;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.export.ExportedBean;
 
 import hudson.model.AbstractProject;
 
 import hudson.model.Item;
 import hudson.plugins.git.GitSCM;
 
 import hudson.scm.SCM;
 import hudson.scm.SubversionSCM;
 import hudson.scm.SubversionSCM.ModuleLocation;
 
 import java.io.Writer;
 
 import java.util.ArrayList;
 
 
 /**
 * Entry point of SCM2Job plugin.
 *
 * @author Stefan Brausch
 * @plugin
 */
 
 @ExportedBean
 @Extension
 public class SCM2Job implements RootAction {
 
     /**List of existing Hudson jobs.*/
     private ArrayList<Item> list = new ArrayList<Item>();
     
     /**Submitted parameter format.*/
     private Format paramFormat;
     
     /**Enum of possible parameter formats.*/
     private enum Format { TEXT, URL };
 
     /**Meet our logger.*/
     private static final Logger LOGGER = Logger.getLogger(SCM2Job.class.getName());
 
     /**
      * {@inheritDoc}
      */
     public final String getIconFileName() {
         return "/plugin/scm2job/icons/scm2job-32x32.png";
     }
 
     /**
      * {@inheritDoc}
      */
     public final String getDisplayName() {
         return "SCM2Job";
     }
 
     /**
      * {@inheritDoc}
      */
     public final String getUrlName() {
         return "/scm2job";
     }
 
     /**
      * Returns the jobs found.
      * @return list of jobs
      */
     public ArrayList<Item> getResults() {
         return list;
     }
     
     /**
      * Called when the 'Submit' button is pressed.
      * Processes the submitted parameters and writes the results back to a new jelly site.
      * 
      * @param req submitted request
      * @param rsp generated response
      * @throws IOException Exception for Writer and StaplerResponse operations
      */
     public final void doGetJobs(StaplerRequest req, StaplerResponse rsp)
         throws IOException {
         
         list.clear();
 
         String paramPath = req.getParameter("path");
         final String paramString = req.getParameter("format");
                  
         if ("text".equals(paramString)) {
             paramFormat = Format.TEXT;
         } else {
             paramFormat = Format.URL;
         }
             
         if (paramPath != null && !paramPath.trim().isEmpty()) {
             paramPath = addSlashIfMissing(paramPath);
             final List<Item> getitems = Hudson.getInstance().getAllItems(Item.class);
             for (Item item : getitems) {
                 if (checkSCMPath(item, paramPath)) {
                     list.add(item);
                 }
             }
 
             if (paramFormat.equals(Format.TEXT)) {
                 rsp.sendRedirect("showResultsPlain");
             } else {
                 rsp.sendRedirect("showResultsFancy");
             }
             
         } else {
             final Writer writer = rsp.getCompressedWriter(req);
             try {
                 writer.append(getPathMissing() + "\n");              
             } finally {
                 writer.close();
             }
         }
     }
     
     /**
      * Returns internationalized error message.
      * @return error message
      */
     public String getPathMissing() {
         return Messages.pathMissing();
     }
     
     /**
      * Checks whether SCM path of job fits input SCM path.
      * @param item job
      * @param path SCM path of job
      * @return true if job has matching scm path
      */
     private boolean checkSCMPath(Item item, String path) {
         Boolean found = false;
         
         final String[] scmPath = getSCMPath(item);
         if (scmPath != null) {
             for (int i = 0; i < scmPath.length; i++) {
                 final String checkPath = addSlashIfMissing(scmPath[i]);
                LOGGER.fine("check " + path + " against " + scmPath[i]);
                 if (checkPath.length() > 0
                         && checkPath.length() <= path.length()
                         && checkPath.equalsIgnoreCase(path.substring(0,
                             checkPath.length()))) {
                     found = true;
                     LOGGER.fine("Job found!");
                 }
             }
         }
         
         return found;
     }
 
     /**
      * Finds the Git or SVN locations for an item (job).
      * @param item a job
      * @return Array of found SCM paths
      */
     @SuppressWarnings("rawtypes")
     private String[] getSCMPath(Item item) {
 
         String[] scmPath = null;
         final SCM scm = ((AbstractProject<?, ?>) item).getScm();
 
         if (scm instanceof SubversionSCM) {
             final SubversionSCM svn = (SubversionSCM) scm;
             final ModuleLocation[] locs = svn.getLocations();
             scmPath = new String[locs.length];
             for (int i = 0; i < locs.length; i++) {
                 final ModuleLocation moduleLocation = locs[i];
                 scmPath[i] = moduleLocation.remote;
                 LOGGER.fine(scmPath[i] + " added");
             }
         } else if (scm instanceof GitSCM) {
             final GitSCM git = (GitSCM) scm;
             final List<RemoteConfig> repoList = git.getRepositories();
 
             scmPath = new String[repoList.size()];
             for (int i = 0; i < repoList.size(); i++) {
                 final List<URIish> uris = repoList.get(i).getURIs();
                 for (final Iterator iterator = uris.iterator(); iterator.hasNext();) {
                     final URIish urIish = (URIish) iterator.next();
                     scmPath[i] = urIish.toString();
                     LOGGER.fine(scmPath[i] + " added");
                 }
             }
         }
         return scmPath;
     }
     
     private String addSlashIfMissing(String path) {
         if (!path.endsWith("/")) {
             return path + "/";
         } else {
             return path;
         }
     }
 }
