 package org.optaplanner.examples.projectscheduling.persistence;
 
 import java.io.IOException;
 
 import org.optaplanner.core.impl.solution.Solution;
 import org.optaplanner.examples.common.persistence.AbstractTxtSolutionExporter;
 import org.optaplanner.examples.projectscheduling.domain.Allocation;
 import org.optaplanner.examples.projectscheduling.domain.ExecutionMode;
 import org.optaplanner.examples.projectscheduling.domain.Job;
 import org.optaplanner.examples.projectscheduling.domain.ProjectSchedule;
 import org.optaplanner.examples.projectscheduling.domain.Project;
 
 public class Mista2013SolutionExporter extends AbstractTxtSolutionExporter {
 
     private final class Mista2013TxtOutputBuilder extends TxtOutputBuilder {
 
         private ProjectSchedule solution;
 
         private int getMaxDueDate(final ProjectSchedule solution, final Project p) {
             int max = Integer.MIN_VALUE;
             for (final Allocation a : solution.getAllocations()) {
                 if (a.getJob().getParentProject() != p) {
                     continue;
                 }
                 max = Math.max(max, a.getDueDate());
             }
             return max;
         }
 
         private int getMinStartDate(final ProjectSchedule solution, final Project p) {
             int min = Integer.MAX_VALUE;
             for (final Allocation a : solution.getAllocations()) {
                 if (a.getJob().getParentProject() != p) {
                     continue;
                 }
                 min = Math.min(min, a.getStartDate());
             }
             return min;
         }
 
         @Override
         public void setSolution(@SuppressWarnings("rawtypes") final Solution solution) {
             if (!(solution instanceof ProjectSchedule)) {
                 throw new IllegalArgumentException("Please provide a valid Mista2013 solution instance.");
             }
             this.solution = (ProjectSchedule) solution;
         }
 
         @Override
         public void writeSolution() throws IOException {
             for (final Project p : this.solution.getProblem().getProjects()) {
                 final int id = p.getId();
                 for (final Job j : p.getJobs()) {
                     int startDate = -1;
                    ExecutionMode executionMode = j.getExecutionModes().iterator().next();
                     if (j.isSource()) {
                         startDate = this.getMinStartDate(this.solution, p);
                     } else if (j.isSink()) {
                         startDate = this.getMaxDueDate(this.solution, p) + 1;
                     } else {
                         final Allocation a = this.solution.getAllocation(j);
                         startDate = a.getStartDate();
                         executionMode = a.getExecutionMode();
                     }
                     this.bufferedWriter.write(String.valueOf(id));
                     this.bufferedWriter.write(" ");
                     this.bufferedWriter.write(String.valueOf(j.getId() - 1));
                     this.bufferedWriter.write(" ");
                     this.bufferedWriter.write(String.valueOf(executionMode.getId() - 1));
                     this.bufferedWriter.write(" ");
                     this.bufferedWriter.write(String.valueOf(startDate));
                     this.bufferedWriter.newLine();
                 }
             }
 
         }
 
     }
 
     public Mista2013SolutionExporter() {
         super(new Mista2013DaoImpl());
     }
 
     @Override
     public TxtOutputBuilder createTxtOutputBuilder() {
         return new Mista2013TxtOutputBuilder();
     }
 
 }
