 /*
  * $Id: $
  *
  * Copyright (c) 2009 Fujitsu Denmark
  * All rights reserved.
  */
 package dk.fujitsu.issuecheck;
 
 import dk.fujitsu.issuecheck.ims.ImsService;
 import dk.fujitsu.issuecheck.scm.ScmService;
 import org.apache.log4j.Logger;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author Claus Brøndby Reimer (dencbr) / Fujitsu Denmark a|s
  * @version $Revision: $ $Date: $
  */
 public class CommitCheck {
     private static final Logger LOGGER = Logger.getLogger(CommitCheck.class);
     private String[] arguments;
     private String message;
     private int status;
 
     public CommitCheck(String... arguments) {
         this.arguments = arguments;
     }
 
 
     public void execute() {
         List<String> messages;
         ScmService scm;
         ImsService ims;
         String issue;
 
         scm = Config.getInstance("scm");
         ims = Config.getInstance("ims");
 
         messages = scm.getMessages(arguments);
         
         for (String message : messages) {
             issue = getIssue(message);
 
             LOGGER.debug("searching for issue " + issue);
 
             if (issue == null) {
                 this.message = "commit message:\n" + message + "\n>> has no issue number <<";
                 status = 1;
                 return;
             }
 
             if (!ims.isIssue(issue)) {
                 this.message = "commit message:\n" + message + "\n>> contains unknown issue id \"" + issue + "\" <<";
                 status = 2;
                 return;
             }
 
             if (!ims.isIssueOpen(issue)) {
                 this.message = "commit message:\n" + message + "\n>> refers to an issue where no commits are allowed <<";
                 status = 3;
                 return;
             }
 
             LOGGER.debug("accepted " + message);
         }
     }
 
     public int getStatus() {
         return status;
     }
 
     public String getMessage() {
         return message;
     }
 
     private String getIssue(String message) {
         Pattern pattern;
         Matcher matcher;
 
         pattern = Pattern.compile(Config.get("ims.rally.issuePattern"));
         matcher = pattern.matcher(message);
 
         return matcher.find() ? matcher.group(1) : null;
     }
 
 }
