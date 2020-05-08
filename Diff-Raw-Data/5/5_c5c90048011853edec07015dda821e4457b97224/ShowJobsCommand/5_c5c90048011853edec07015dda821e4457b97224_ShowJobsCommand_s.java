 package com.globalsight.tools;
 
 import java.util.ArrayList;
 import java.util.Formatter;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 
 // TODO: add filters
 public class ShowJobsCommand extends WebServiceCommand {
 
     @Override
     protected void execute(CommandLine command, UserData userData,
         WebService webService) throws Exception {
        List<String> args = command.getArgList();
         List<Job> jobs = webService.getJobs();
        if (args.size() == 0) {
             showAllJobs(jobs);
         }
         else {
             List<Job> jobsToShow = new ArrayList<Job>();
             for (String arg: args) {
                 Long id = parseId(arg);
                 if (id == null) {
                     System.out.println("Skipping '" + arg + "': not an id");
                     continue;
                 }
                 Job j = Job.byId(jobs, arg);
                 if (j != null) {
                     jobsToShow.add(j);
                     continue;
                 }
                 System.out.println("Skipping '" + arg + "': no such job");
             }
             if (jobsToShow.size() == 0) {
                 return;
             }
             for (Job j : jobsToShow) {
                 System.out.println("Job " + j.getId() + ": " + j.getName());
                 for (Workflow f : j.getWorkflows()) {
                     Task t = webService.getCurrentTask(f);
                     System.out.println("\t" + f.getTargetLocale() + 
                                 " - Task " + t.getId() + " " + t.getName() 
                                 + " " + t.getState());
                 }
             }
         }
     }
     
     private Long parseId(String s) {
         try {
             return Long.valueOf(s);
         }
         catch (NumberFormatException e) {
             return null;
         }
     }
 
     void showAllJobs(List<Job> jobs) throws Exception {
         printHeader();
         for (Job job : jobs) {
             printShort(job);
         }
     }
     void printHeader() {
         Formatter f = new Formatter(System.out);
         f.format("%-8s%-8s%-12s%s\n", "ID", "WFID", "State", "Name");
     }
     void printShort(Job job) {
         Formatter f = new Formatter(System.out);
         // XXX Hack: just displaying the first WFId
         f.format("%-8s%-4s%-14s%s\n", job.getId(), 
         		job.getWorkflows().get(0).getId(), job.getDisplayState(), 
                  job.getName());
     }
     
     @Override
     public String getDescription() {
         return "List jobs for this company";
     }
 }
