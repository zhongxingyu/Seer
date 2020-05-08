 package org.optaplanner.examples.projectscheduling.persistence;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.optaplanner.examples.common.persistence.AbstractTxtSolutionImporter;
 import org.optaplanner.examples.projectscheduling.domain.Job;
 import org.optaplanner.examples.projectscheduling.domain.Job.JobType;
 import org.optaplanner.examples.projectscheduling.domain.ExecutionMode;
 import org.optaplanner.examples.projectscheduling.domain.ProjectSchedule;
 import org.optaplanner.examples.projectscheduling.domain.ProblemInstance;
 import org.optaplanner.examples.projectscheduling.domain.Project;
 import org.optaplanner.examples.projectscheduling.domain.Resource;
 import org.optaplanner.examples.projectscheduling.domain.Resource.ResourceType;
 import org.optaplanner.examples.projectscheduling.domain.ResourceRequirement;
 import org.optaplanner.examples.projectscheduling.persistence.parsers.instance.RawInstance;
 import org.optaplanner.examples.projectscheduling.persistence.parsers.instance.RawProject;
 import org.optaplanner.examples.projectscheduling.persistence.parsers.project.Precedence;
 import org.optaplanner.examples.projectscheduling.persistence.parsers.project.RawProjectData;
 import org.optaplanner.examples.projectscheduling.persistence.parsers.project.Request;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Mista2013SolutionImporter extends AbstractTxtSolutionImporter {
     
     private static final Logger LOGGER = LoggerFactory.getLogger(Mista2013SolutionImporter.class);
 
     private class Mista2013TxtInputBuilder extends TxtInputBuilder {
 
        private List<Job> buildJobs(final int projectId, final RawProjectData data, final List<Resource> resources) {
             final List<Request> requests = data.getRequestsAndDurations();
             final List<Precedence> precedence = data.getPrecedences();
             // traverse the jobs backwards; successors need to be created first
             final Map<Integer, Job> jobCache = new HashMap<Integer, Job>(precedence.size());
             for (int jobId = precedence.size(); jobId > 0; jobId--) {
                 // gather successors
                 final List<Integer> successorIds = precedence.get(jobId - 1).getSuccessors();
                 final List<Job> successors = new ArrayList<Job>(successorIds.size());
                 for (final Integer successorId : successorIds) {
                     successors.add(jobCache.get(successorId));
                 }
                 // prepare job modes
                 final List<ExecutionMode> modes = new ArrayList<ExecutionMode>(requests.size());
                 for (final Request r : requests) {
                     if (r.getJobNumber() != jobId) {
                         continue;
                     }
                     // prepare resource consumption data
                     int resourceId = 0;
                     final ArrayList<ResourceRequirement> resourceConsumption = new ArrayList<ResourceRequirement>(resources.size());
                     for (final Resource resource : resources) {
                         final int consumption = r.getResources().get(resourceId);
                         if (consumption > 0) {
                             // only use the resources that are actually being
                             // consumed
                             resourceConsumption.add(new ResourceRequirement(resource, consumption));
                         }
                         resourceId++;
                     }
                     resourceConsumption.trimToSize();
                     // and now make sure that no resources are over-consumed, leaving the EM useless
                     boolean proper = true;
                     for (ResourceRequirement rr: resourceConsumption) {
                         if (rr.getRequirement() > rr.getResource().getCapacity()) {
                             proper = false;
                             break;
                         }
                     }
                     if (proper) {
                         modes.add(new ExecutionMode(r.getMode(), r.getDuration(), resourceConsumption));
                     } else {
                        Mista2013SolutionImporter.LOGGER.info("Ignoring execution mode {} of job {} of project {} because it overconsumes at least one of its resources.", r.getMode(), jobId, projectId);
                     }
                 }
                 JobType jt = null;
                 if (jobId == precedence.size()) {
                     jt = JobType.SINK;
                 } else if (jobId == 1) {
                     jt = JobType.SOURCE;
                 } else {
                     jt = JobType.STANDARD;
                 }
                 final Job job = new Job(jobId, modes, successors, jt);
                 jobCache.put(jobId, job);
             }
             return new ArrayList<Job>(jobCache.values());
         }
 
         private Project buildProject(final RawProject raw, final List<Resource> globalResources) {
             final RawProjectData data = raw.getProjectData();
             // set resource capacities
             final List<Resource> resultingResources = new ArrayList<Resource>(globalResources.size());
             for (int i = 0; i < globalResources.size(); i++) {
                 Resource r = globalResources.get(i);
                 if (r == null) {
                     if (i < 2) {
                         r = new Resource(i + 1, ResourceType.RENEWABLE);
                         r.setCapacity(data.getResourceAvailability().get(i));
                     } else if (i < 4) {
                         r = new Resource(i + 1, ResourceType.NONRENEWABLE);
                         r.setCapacity(data.getResourceAvailability().get(i));
                     } else {
                         r = new Resource(i + 1, ResourceType.DOUBLE_CONSTRAINED);
                         r.setCapacity(data.getResourceAvailability().get(i));
                     }
                 }
                 resultingResources.add(r);
             }
             // build jobs and project
            final List<Job> jobs = this.buildJobs(raw.getId(), data, resultingResources);
             final Project p = new Project(raw.getId(), raw.getCriticalPathDuration(), raw.getReleaseDate(),
                     resultingResources, jobs);
             return p;
         }
 
         @Override
         public ProjectSchedule readSolution() throws IOException {
             final RawInstance instance = RawInstance.parse(this.inputFile);
             // prepare global resources
             final List<Resource> globalResources = new ArrayList<Resource>();
             int i = 1;
             for (final Integer capacity : instance.getResourceCapacities()) {
                 if (capacity < 0) {
                     globalResources.add(null);
                 } else {
                     final Resource r = new Resource(i);
                     r.setCapacity(capacity);
                     globalResources.add(r);
                 }
                 i++;
             }
             // build projects from raw data
             final List<Project> projects = new ArrayList<Project>();
             for (final RawProject rp : instance.getProjects()) {
                 final Project p = this.buildProject(rp, globalResources);
                 projects.add(p);
             }
             return new ProjectSchedule(new ProblemInstance(projects));
         }
 
     }
 
     public Mista2013SolutionImporter() {
         super(new Mista2013DaoImpl());
     }
 
     @Override
     public TxtInputBuilder createTxtInputBuilder() {
         return new Mista2013TxtInputBuilder();
     }
 
 }
