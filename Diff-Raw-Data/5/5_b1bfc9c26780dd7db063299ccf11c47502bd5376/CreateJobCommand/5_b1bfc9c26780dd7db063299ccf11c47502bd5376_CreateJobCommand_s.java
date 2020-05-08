 package com.globalsight.tools;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 
 /**
  * Create a job.  By default, use all the target locales
  * indicated by the file profile (selected by name or id).  
  * Generate a job name based on the name of the first file uploaded,
  * unless one is specified on the command line.
  */
 @SuppressWarnings("static-access")
// XXX Wow, there is problem with webservices, the absolute file path isn't stripping
// the '../..' out?
 // TODO: I should assume the fileprofile target locale by default, but allow overrides
 // via --target
 public class CreateJobCommand extends WebServiceCommand {
 
     @Override
     protected void execute(CommandLine command, UserData userData,
             WebService webService) throws Exception {
         // Make sure we have at least one file to upload
         if (command.getArgs().length == 0) {
             die("Must specify at least one file to import.");
         }
         
         if (command.hasOption(FILEPROFILE) && 
             command.hasOption(FILEPROFILEID)) {
             usage("Can't specify both " + FILEPROFILE + 
                   " and " + FILEPROFILEID + " options.");
         }
         if (!(command.hasOption(FILEPROFILE) || 
               command.hasOption(FILEPROFILEID))) {
             usage("Must specify either " + FILEPROFILE +
                   " or " + FILEPROFILEID + " option.");
         }
         FileProfile fp = null;
         if (command.hasOption(FILEPROFILE)) {
             String fpName = command.getOptionValue(FILEPROFILE);
             fp = findByName(webService.getFileProfiles(), fpName);
             if (fp == null) {
                 die("No such file profile: '" + fpName + "'");
             }
         }
         else {
             String fpId = command.getOptionValue(FILEPROFILEID);
             fp = findById(webService.getFileProfiles(), fpId);
             if (fp == null) {
                 die("No such file profile id: '" + fpId + "'");
             }
         }
         
         // TODO target locale overrides
         
         // Convert all remaining arguments to files
         List<File> files = new ArrayList<File>();
         for (String path : command.getArgs()) {
             File f = new File(path);
             if (!f.exists() || f.isDirectory()) {
                 die("Not a file: " + f);
             }
            files.add(f);
         }
 
         // Get a job name either from command line or first file
         // uploaded, then uniquify
         String baseJobName = files.get(0).getName();
         if (command.hasOption(JOBNAME)) {
             command.getOptionValue(JOBNAME);
         }
         String jobName = webService.getUniqueJobName(baseJobName);
         verbose("Got unique job name: " + jobName);
         List<String> filePaths = new ArrayList<String>();
         for (File f : files) {
             filePaths.add(uploadFile(f, jobName, fp, webService));
         }
         webService.createJob(jobName, filePaths, fp);
     }
     
     FileProfile findByName(List<FileProfile> fileProfiles, String name) {
         for (FileProfile fp : fileProfiles) {
             if (fp.getName().equalsIgnoreCase(name)) {
                 return fp;
             }
         }
         return null;
     }
     
     FileProfile findById(List<FileProfile> fileProfiles, String id) {
         for (FileProfile fp : fileProfiles) {
             if (fp.getId().equals(id)) {
                 return fp;
             }
         }
         return null;
     }
     
     private static long MAX_SEND_SIZE = 5 * 1000 * 1024; // 5M
     
     // Returns the filepath that was sent to the server
     String uploadFile(File file, String jobName, FileProfile fileProfile,
                     WebService webService) throws Exception {
         String filePath = file.getAbsolutePath();
         // XXX This is so janky - why do we have to do this?
         filePath = filePath.substring(filePath.indexOf(File.separator) + 1);     
         verbose("Uploading " + filePath + " to job " + jobName);
         InputStream is = null;
         try {
             long bytesRemaining = file.length();
             is = new BufferedInputStream(new FileInputStream(file));
             while (bytesRemaining > 0) {
                 // Safe cast because it's bounded by MAX_SEND_SIZE
                 int size = (int)Math.min(bytesRemaining, MAX_SEND_SIZE);
                 byte[] bytes = new byte[size];
                 int count = is.read(bytes);
                 if (count <= 0) {
                     break;
                 }
                 bytesRemaining -= count;
                 verbose("Uploading chunk 1: " + size + " bytes");
                 webService.uploadFile(filePath, jobName, fileProfile.getId(), bytes);
             }
             verbose("Finished uploading " + filePath);
         }
         catch (IOException e) {
             throw new RuntimeException(e);
         }      
         finally {
             if (is != null) {
                 is.close();
             }
         }
         return filePath;
     }
     
     static final String TARGET = "target",
                         FILEPROFILE = "fileprofile",
                         FILEPROFILEID = "fileprofileid",
                         JOBNAME = "name";
     static final Option TARGET_OPT = OptionBuilder
         .withArgName("targetLocale")
         .hasArg()
         .withDescription("target locale code")
         .create(TARGET);
     static final Option FILEPROFILE_OPT = OptionBuilder
         .withArgName("fileProfile")
         .hasArg()
         .withDescription("file profile to use")
         .create(FILEPROFILE);
     static final Option FILEPROFILEID_OPT = OptionBuilder
         .withArgName("fileProfileId")
         .hasArg()
         .withDescription("numeric ID file profile to use")
         .create(FILEPROFILEID);
     static final Option JOBNAME_OPT = OptionBuilder
         .withArgName("jobName")
         .hasArg()
         .withDescription("job name")
         .create(JOBNAME);
 
     @Override
     public Options getOptions() {
         Options opts = getDefaultOptions();
         opts.addOption(TARGET_OPT);
         opts.addOption(FILEPROFILE_OPT);
         opts.addOption(FILEPROFILEID_OPT);
         opts.addOption(JOBNAME_OPT);
         return opts;
     }
     
     @Override
     public String getDescription() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public String getName() {
         return "create-job";
     }
 
 }
