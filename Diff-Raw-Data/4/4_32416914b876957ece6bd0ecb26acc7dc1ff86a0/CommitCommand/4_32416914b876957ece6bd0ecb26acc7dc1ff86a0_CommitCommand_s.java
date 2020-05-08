 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.cli;
 
 import java.io.File;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.meltmedia.cadmium.core.git.GitService;
 import com.meltmedia.cadmium.status.Status;
 
 /**
  * Replaces the content in a git repository branch with the contents of a given directory and tells a Cadmium site to update to it.
  * 
  * @author John McEntire
  * @author Christian Trimble
  * @author Brian Barr
  *
  */
 @Parameters(commandDescription="Commits content from a specific directory into a repo:branch that a site is serving from then updates the site to serve the new content.", separators="=")
 public class CommitCommand extends AbstractAuthorizedOnly implements CliCommand {
   
   @Parameter(description="<dir> <site>", required=true)
   private List<String> params;
   
   @Parameter(names="--repo", description="Overrides the repository url from the server.", required=false)
   private String repo;
   
   @Parameter(names={"--message", "-m"}, description="comment", required=true)  
   private String comment;
   
   @Parameter(names="--quiet-auth", description="Option to skip updating/prompting for new credentials.", required=false)
   private Boolean skipAuth = false;
   
   /**
    * Does the work for this command.
    * 
    * @throws Exception
    */
   public void execute() throws Exception {
       String content = null;
       String siteUrl = null;
       
       if( params.size() == 2 ) {
         content = params.get(0);
         siteUrl = getSecureBaseUrl(params.get(1));
       }
       else if( params.size() == 0 ) {
         System.err.println("The content directory and site must be specifed.");
         System.exit(1);
       }
       else {
         System.err.println("Too many parameters were specified.");
         System.exit(1);
       }
 	  
     GitService git = null;
     try {
       System.out.println("Getting status of ["+siteUrl+"]");
       Status status = StatusCommand.getSiteStatus(siteUrl, token);
   
       if(repo != null) {
         status.setRepo(repo);
       }
       
       status.setRevision(null);
       
       System.out.println("Cloning repository that ["+siteUrl+"] is serving");
       git = CloneCommand.cloneSiteRepo(status);
       
       String revision = status.getRevision();
       String branch = status.getBranch();
       
       if(git.isTag(branch)) {
         throw new Exception("Cannot commit to a tag!");
       }
       System.out.println("Cloning content from ["+content+"] to ["+siteUrl+":"+branch+"]");
       revision = CloneCommand.cloneContent(content, git, comment);
       
       System.out.println("Switching content on ["+siteUrl+"]");
       UpdateCommand.sendUpdateMessage(siteUrl, null, branch, revision, comment, token);
       
     } catch(Exception e) {
       System.err.println("Failed to commit changes to ["+siteUrl+"]: "+e.getMessage());
     } finally {
       if(git != null) {
         git.close();
        FileUtils.forceDelete(new File(git.getBaseDirectory()));
       }
     }
     
   }
 
   /**
    * The command name used to invoke this command.
    */
   @Override
   public String getCommandName() {
     return "commit";
   }
 
   /**
    * @return true when <code>--quiet-auth</code> is passed from the command line.
    */
   @Override
   public boolean isAuthQuiet() {
     return skipAuth;
   }
 }
