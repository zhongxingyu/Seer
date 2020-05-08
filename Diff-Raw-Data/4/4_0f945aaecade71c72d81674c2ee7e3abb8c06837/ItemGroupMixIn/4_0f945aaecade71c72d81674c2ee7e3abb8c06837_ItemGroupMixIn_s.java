 /*******************************************************************************
  *
  * Copyright (c) 2004-2009 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 
  *    Kohsuke Kawaguchi, CloudBees, Inc.
  *
  *
  *******************************************************************************/ 
 
 package hudson.model;
 
 import hudson.Util;
 import hudson.model.listeners.ItemListener;
 import hudson.security.AccessControlled;
 import hudson.util.CopyOnWriteMap;
 import hudson.util.Function1;
 import hudson.util.IOUtils;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 import org.eclipse.hudson.security.team.Team;
 import org.eclipse.hudson.security.team.TeamManager;
 import org.eclipse.hudson.security.team.TeamManager.TeamNotFoundException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Defines a bunch of static methods to be used as a "mix-in" for
  * {@link ItemGroup} implementations. Not meant for a consumption from outside
  * {@link ItemGroup}s.
  *
  * @author Kohsuke Kawaguchi
  */
 public abstract class ItemGroupMixIn {
 
     private transient Logger logger = LoggerFactory.getLogger(ItemGroupMixIn.class);
     
     /**
      * {@link ItemGroup} for which we are working.
      */
     private final ItemGroup parent;
     private final AccessControlled acl;
 
     protected ItemGroupMixIn(ItemGroup parent, AccessControlled acl) {
         this.parent = parent;
         this.acl = acl;
     }
 
     /*
      * Callback methods to be implemented by the ItemGroup implementation.
      */
     /**
      * Adds a newly created item to the parent.
      */
     protected abstract void add(TopLevelItem item);
 
     /**
      * Assigns the root directory for a prospective item.
      */
     protected abstract File getRootDirFor(String name);
 
 
     /*
      * The rest is the methods that provide meat.
      */
     /**
      * Loads all the child {@link Item}s.
      *
      * @param modulesDir Directory that contains sub-directories for each child
      * item.
      */
     public static <K, V extends Item> Map<K, V> loadChildren(ItemGroup parent, File modulesDir, Function1<? extends K, ? super V> key) {
         modulesDir.mkdirs(); // make sure it exists
 
         File[] subdirs = modulesDir.listFiles(new FileFilter() {
             public boolean accept(File child) {
                 return child.isDirectory();
             }
         });
         CopyOnWriteMap.Tree<K, V> configurations = new CopyOnWriteMap.Tree<K, V>();
         for (File subdir : subdirs) {
             try {
                 V item = (V) Items.load(parent, subdir, false);
                 configurations.put(key.call(item), item);
             } catch (IOException e) {
                 e.printStackTrace(); // TODO: logging
             }
         }
 
         return configurations;
     }
     /**
      * {@link Item} -> name function.
      */
     public static final Function1<String, Item> KEYED_BY_NAME = new Function1<String, Item>() {
         public String call(Item item) {
             return item.getName();
         }
     };
 
     /**
      * Creates a {@link TopLevelItem} from the submission of the
      * '/lib/hudson/newFromList/formList' or throws an exception if it fails.
      */
     public synchronized TopLevelItem createTopLevelItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
         acl.checkPermission(Job.CREATE);
 
         TopLevelItem result;
 
         String requestContentType = req.getContentType();
         if (requestContentType == null) {
             throw new Failure("No Content-Type header set");
         }
 
         boolean isXmlSubmission = requestContentType.startsWith("application/xml") || requestContentType.startsWith("text/xml");
 
         String name = req.getParameter("name");
         if (name == null) {
             throw new Failure("Query parameter 'name' is required");
         }
 
         String team = null;
 
         {   // check if the name looks good
             Hudson hudson = Hudson.getInstance();
             Hudson.checkGoodName(name);
             name = name.trim();
             
             if (hudson.isTeamManagementEnabled() && (name.indexOf(TeamManager.TEAM_SEPARATOR) != -1)) {
                 throw new Failure("The job name cannot contain" + TeamManager.TEAM_SEPARATOR + "when team management is enabled. ");
             }
             
             // see if team requested
             Team requestedTeam = null;
             if (hudson.isTeamManagementEnabled()) {
                 team = req.getParameter("team");
                 if (team != null){
                     String teamName = team.trim();
                     if (teamName.length() > 0) {
                         try {
                             requestedTeam = hudson.getTeamManager().findTeam(teamName);
                             team = teamName;
                         } catch (TeamManager.TeamNotFoundException ex) {
                             throw new Failure("Requested team " + teamName + " not found");
                         }
                     } else {
                         team = null;
                     }
                 }
             }
             
             String existingJobName = name;
             if (hudson.isTeamManagementEnabled()){
                 existingJobName = requestedTeam == null
                         ? hudson.getTeamManager().getRawTeamQualifiedJobName(name)
                         : hudson.getTeamManager().getRawTeamQualifiedJobName(requestedTeam, name);
             }
             if (parent.getItem(existingJobName) != null) {
                 throw new Failure(Messages.Hudson_JobAlreadyExists(existingJobName));
             }
         }
         
         String mode = req.getParameter("mode");
         if (mode != null && mode.equals("copy")) {
             String from = req.getParameter("from");
 
             // resolve a name to Item
             Item src = parent.getItem(from);
             if (src == null) {
                 src = Hudson.getInstance().getItemByFullName(from);
             }
 
             if (src == null) {
                 if (Util.fixEmpty(from) == null) {
                     throw new Failure("Specify which job to copy");
                 } else {
                     throw new Failure("No such job: " + from);
                 }
             }
             if (!(src instanceof TopLevelItem)) {
                 throw new Failure(from + " cannot be copied");
             }
 
             result = copy((TopLevelItem) src, name, team);
         } else {
             if (isXmlSubmission) {
                 result = createProjectFromXML(name, team, req.getInputStream());
                 rsp.setStatus(HttpServletResponse.SC_OK);
                 return result;
             } else {
                 if (mode == null) {
                     throw new Failure("No mode given");
                 }
 
                 // create empty job and redirect to the project config screen
                 result = createProject(Items.getDescriptor(mode), name, team, true);
             }
         }
         
         rsp.sendRedirect2(redirectAfterCreateItem(req, result));
         return result;
     }
 
     /**
      * Computes the redirection target URL for the newly created
      * {@link TopLevelItem}.
      */
     protected String redirectAfterCreateItem(StaplerRequest req, TopLevelItem result) throws IOException {
         return req.getContextPath() + '/' + result.getUrl() + "configure";
     }
     
     /**
      * Copies an existing {@link TopLevelItem} to a new name.
      *
      * The caller is responsible for calling
      * {@link ItemListener#fireOnCopied(Item, Item)}. This method cannot do that
      * because it doesn't know how to make the newly added item reachable from
      * the parent.
      */
     @SuppressWarnings({"unchecked" })
     public synchronized <T extends TopLevelItem> T copy(T src, String name) throws IOException {
         return copy(src, name, null);
     }
 
     /**
      * Copies an existing {@link TopLevelItem} to a new name.
      *
      * The caller is responsible for calling
      * {@link ItemListener#fireOnCopied(Item, Item)}. This method cannot do that
      * because it doesn't know how to make the newly added item reachable from
      * the parent.
      */
     @SuppressWarnings({"unchecked" })
     public synchronized <T extends TopLevelItem> T copy(T src, String name, String teamName) throws IOException {
         acl.checkPermission(Job.CREATE);
 
        String jobName = createInTeam(name, teamName);
        
        T result = (T) createProject(src.getDescriptor(), jobName, false);
 
         // copy config
         Util.copyFile(Items.getConfigFile(src).getFile(), Items.getConfigFile(result).getFile());
 
         // reload from the new config
         result = (T) Items.load(parent, result.getRootDir());
         result.onCopiedFrom(src);
 
         add(result);
         
         ItemListener.fireOnCopied(src, Hudson.getInstance().getItem(result.getName()));
 
         return result;
     }
 
     private String createInTeam(String name, String teamName) throws IOException {
         // To be created in a specific team, a job must first be added
         // to the team, ensuring that Hudson will find the correct rootDir.
         TeamManager teamManager = Hudson.getInstance().getTeamManager();
         if (!teamManager.isTeamManagementEnabled()) {
             if (teamName != null) {
                 throw new IOException("Team management is not enabled");
             }
             return name;
         }
         Team team;
         if (teamName == null) {
             try {
                 team = teamManager.findCurrentUserTeamForNewJob();
             } catch (TeamNotFoundException ex) {
                 // Shouldn't happen, as user is already confirmed for Job.CREATE
                 return name;
             }
         } else {
             try {
                 team = teamManager.findTeam(teamName);
             } catch (TeamNotFoundException e) {
                 throw new IOException("Team "+teamName+" does not exist");
             }
         }
         // addJob does the necessary name assembly and returns qualified job name
         return teamManager.addJob(name, team);
     }
 
     public synchronized TopLevelItem createProjectFromXML(String name, InputStream xml) throws IOException {
         return createProjectFromXML(name, null, xml);
     }
     
     public synchronized TopLevelItem createProjectFromXML(String name, String teamName, InputStream xml) throws IOException {
         acl.checkPermission(Job.CREATE);
 
         String jobName = createInTeam(name, teamName);
         
         // place it as config.xml
         File configXml = Items.getConfigFile(getRootDirFor(jobName)).getFile();
         configXml.getParentFile().mkdirs();
         try {
             IOUtils.copy(xml, configXml);
 
             // load it
             TopLevelItem result = (TopLevelItem) Items.load(parent, configXml.getParentFile(), false);
             add(result);
             
             assert(result.getName().equals(jobName));
             
             ItemListener.fireOnCreated(Hudson.getInstance().getItem(jobName));
             Hudson.getInstance().rebuildDependencyGraph();
 
             return result;
         } catch (IOException e) {
             // if anything fails, delete the config file to avoid further confusion
             Hudson.getInstance().getTeamManager().removeJob(jobName);
             Util.deleteRecursive(configXml.getParentFile());
             throw e;
         }
     }
 
     public synchronized TopLevelItem createProject(TopLevelItemDescriptor type, String name, boolean notify) throws IOException {
         return createProject(type, name, null, notify);
     }
 
     public synchronized TopLevelItem createProject(TopLevelItemDescriptor type, String name, String teamName, boolean notify)
             throws IOException {
         acl.checkPermission(Job.CREATE);
         
         name = createInTeam(name, teamName);
 
         Hudson hudson = Hudson.getInstance();
         String existingJobName = name;
         if (hudson.isTeamManagementEnabled() && teamName == null) {
             existingJobName = hudson.getTeamManager().getTeamQualifiedJobName(name);
         }
         if (parent.getItem(existingJobName) != null) {
             throw new IllegalArgumentException("Job with name " + name + " already exists");
         }
 
         TopLevelItem item;
         try {
             item = type.newInstance(parent, name);
         } catch (Exception e) {
             throw new IllegalArgumentException(e);
         }
         item.onCreatedFromScratch();
         item.save();
         add(item);
 
         if (notify) {
             ItemListener.fireOnCreated(item);
         }
 
         return item;
     }
 }
