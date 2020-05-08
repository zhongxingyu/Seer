 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.stackframe.sarariman.projects.Project;
 import com.stackframe.sarariman.tasks.Task;
 import com.stackframe.sarariman.timesheets.Timesheet;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 /**
  *
  * @author mcculley
  */
 public class TimesheetAudit implements Audit {
 
     private final Sarariman sarariman;

     private final Directory directory;
 
     public TimesheetAudit(Sarariman sarariman, Directory directory) {
         this.sarariman = sarariman;
         this.directory = directory;
     }
 
     public String getDisplayName() {
         return "Timesheets";
     }
 
     private List<Timesheet> timesheets(Week week) {
         List<Timesheet> list = new ArrayList<Timesheet>();
         for (Employee employee : directory.getByUserName().values()) {
             list.add(sarariman.getTimesheets().get(employee, week));
         }
 
         return list;
     }
 
     private Set<Project> projects(Iterable<Timesheet> timesheets) {
         ImmutableSet.Builder<Project> projects = ImmutableSet.<Project>builder();
         for (Timesheet timesheet : timesheets) {
             for (TimesheetEntry entry : timesheet.getEntries()) {
                 Task task = entry.getTask();
                 if (task.isBillable()) {
                    projects.add(task.getProject());
                 }
             }
         }
 
         return projects.build();
     }
 
     private List<AuditResult> reportsDue(List<Timesheet> timesheets, Week week) {
         Set<Project> projects = projects(timesheets);
         List<AuditResult> results = new ArrayList<AuditResult>();
         for (Project project : projects) {
             Collection<EmailLogEntry> logEntries = project.getEmailLogEntries(week);
             if (logEntries.isEmpty()) {
                 try {
                     URL reportsURL = new URL(String.format("%sprojecttimereports?project=%d&week=%s", sarariman.getMountPoint(),
                                                            project.getId(), week.getName()));
                     results.add(new AuditResult(AuditResultType.todo,
                                                 String.format("time reports for project '%s - %s' (%d) need to go out",
                                                               project.getName(), project.getClient().getName(), project.getId()),
                                                 reportsURL));
                 } catch (MalformedURLException e) {
                     throw new RuntimeException(e);
                 }
             }
         }
 
         return results;
     }
 
     public Collection<AuditResult> getResults() {
         ImmutableList.Builder<AuditResult> results = ImmutableList.<AuditResult>builder();
         Week lastWeek = DateUtils.week(DateUtils.now()).getPrevious();
         List<Timesheet> timesheets = timesheets(lastWeek);
         results.addAll(reportsDue(timesheets, lastWeek));
 
         for (Timesheet timesheet : timesheets) {
             Employee employee = timesheet.getEmployee();
             if (timesheet.isSubmitted()) {
                 if (timesheet.isApproved()) {
                     double PTORecorded = timesheet.getPTOHours();
                     if (PTORecorded > 0) {
                         double deducted = PaidTimeOff.getPaidTimeOff(sarariman.getDataSource(), employee, lastWeek,
                                                                      "weeklyPTODeduction");
                         if (PTORecorded != -deducted) {
                             URL timesheetsURL = sarariman.getTimesheets().getURL();
                             try {
                                 timesheetsURL = new URL(timesheetsURL.toString() + "?week=" + lastWeek.getName());
                             } catch (MalformedURLException mue) {
                                 throw new AssertionError(mue);
                             }
 
                             results.add(new AuditResult(AuditResultType.todo,
                                                         String.format("PTO needs to be deducted for %s for week of %s",
                                                                       employee.getDisplayName(), lastWeek.getName()),
                                                         timesheetsURL));
                         }
                     }
                 } else {
                     results.add(new AuditResult(AuditResultType.todo,
                                                 String.format("timesheet for %s needs review", employee.getDisplayName()),
                                                 timesheet.getURL()));
                 }
             } else {
                 if (employee.isActive()) {
                     results.add(new AuditResult(AuditResultType.warning, String.format("timesheet for %s needs to be submitted",
                                                                                        employee.getDisplayName()),
                                                 timesheet.getURL()));
                 }
             }
         }
 
         return results.build();
     }
 
 }
