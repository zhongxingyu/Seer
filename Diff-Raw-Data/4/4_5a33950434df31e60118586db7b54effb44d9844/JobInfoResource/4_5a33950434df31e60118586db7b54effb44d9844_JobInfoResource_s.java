 package de.otto.jobstore.web;
 
 import de.otto.jobstore.common.JobInfo;
 import de.otto.jobstore.repository.api.JobInfoRepository;
 import de.otto.jobstore.service.api.JobInfoService;
 import de.otto.jobstore.service.api.JobService;
 import de.otto.jobstore.service.exception.*;
 import de.otto.jobstore.web.representation.JobInfoRepresentation;
 import de.otto.jobstore.web.representation.JobNameRepresentation;
 import org.apache.abdera.Abdera;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.Feed;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import java.io.StringWriter;
 import java.net.URI;
 import java.util.*;
 
 
 /**
  *
  */
 @Path("/jobs")
 public final class JobInfoResource {
 
     public static final String OTTO_JOBS_XML = "application/vnd.otto.jobs+xml";
     public static final String OTTO_JOBS_JSON = "application/vnd.otto.jobs+json";
     public static final String OTTO_JOB_XML = "application/vnd.otto.job+xml";
     public static final String OTTO_JOB_JSON = "application/vnd.otto.job+json";
 
     private final JobService jobService;
 
     private final JobInfoService jobInfoService;
 
     public JobInfoResource(JobService jobService, JobInfoService jobInfoService) {
         this.jobService = jobService;
         this.jobInfoService = jobInfoService;
     }
 
     /**
      * Returns an atom feed with the available job names. Each entry contains a link to retrieve all jobs for a given name
      *
      * @param uriInfo The uriInfo injected by Jax-RS
      * @return The atom feed with the available job names
      */
     @GET
     @Produces(MediaType.APPLICATION_ATOM_XML)
     public Response getJobs(@Context final UriInfo uriInfo) {
         final Abdera abdera = new Abdera();
         final Feed feed = createFeed(abdera, "Job Names", "A list of the available distinct job names",
                 uriInfo.getBaseUriBuilder().path(JobInfoResource.class).build());
         try {
             final JAXBContext ctx = JAXBContext.newInstance(JobNameRepresentation.class);
             final Marshaller marshaller = ctx.createMarshaller();
             for (String name : jobService.listJobNames()) {
                 final URI uri = uriInfo.getBaseUriBuilder().path(JobInfoResource.class).path(name).build();
                 final StringWriter writer = new StringWriter();
                 marshaller.marshal(new JobNameRepresentation(name), writer);
                 final Entry entry = abdera.newEntry();
                 entry.addLink(uri.getPath(), "self");
                 entry.setContent(writer.toString(), OTTO_JOBS_XML);
                 feed.addEntry(entry);
             }
         } catch (JAXBException e) {
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
         return Response.ok(feed).build();
     }
 
     /**
      * Executes a job and its content location.
      *
      * @param name The name of the job to execute
      * @param uriInfo The uriInfo injected by Jax-RS
      * @return The content location of the job
      */
     @POST
     @Path("{name}")
     public Response executeJob(@PathParam("name") final String name, @Context final UriInfo uriInfo)  {
         try {
             final String jobId = jobService.executeJob(name, true);
             final JobInfo jobInfo = jobInfoService.getById(jobId);
             final URI uri = uriInfo.getBaseUriBuilder().path(JobInfoResource.class).path(jobInfo.getName()).path(jobId).build();
             return Response.created(uri).build();
         } catch (JobNotRegisteredException e) {
             return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
         } catch (JobExecutionNotNecessaryException e) {
             return Response.status(Response.Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
         } catch (JobExecutionDisabledException e) {
             return Response.status(Response.Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
         } catch (JobAlreadyQueuedException e) {
             return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
         } catch (JobAlreadyRunningException e) {
             return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
         }
     }
 
     /**
      * Returns an atom feed the latest jobs of the given name
      *
      * @param name The name of the jobs to return
      * @param size The number of jobs to return, default value is 10
      * @param uriInfo The uriInfo injected by Jax-RS
      * @return An atom with with the latest jobs
      */
     @GET
     @Path("/{name}")
     @Produces(MediaType.APPLICATION_ATOM_XML)
     public Response getJobsByName(@PathParam("name") final String name, @QueryParam("size") @DefaultValue("10") final int size,
                                   @Context final UriInfo uriInfo) {
         final Abdera abdera = new Abdera();
         final Feed feed = createFeed(abdera, "JobInfo Objects", "A list of the " + size + " most recent jobInfo objects with name " + name,
                 uriInfo.getBaseUriBuilder().path(JobInfoResource.class).path(name).build());
         try {
             final JAXBContext ctx = JAXBContext.newInstance(JobInfoRepresentation.class);
             final Marshaller marshaller = ctx.createMarshaller();
             for (JobInfo jobInfo : jobInfoService.getByName(name, size)) {
                 final URI uri = uriInfo.getBaseUriBuilder().path(JobInfoResource.class).path(name).path(jobInfo.getId()).build();
                 final StringWriter writer = new StringWriter();
                 marshaller.marshal(JobInfoRepresentation.fromJobInfo(jobInfo), writer);
                 final Entry entry = abdera.newEntry();
                 entry.addLink(uri.getPath(), "self");
                 entry.setContent(writer.toString(), OTTO_JOBS_XML);
                 feed.addEntry(entry);
             }
         } catch (JAXBException e) {
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
         return Response.ok(feed).build();
     }
 
     /**
      * Returns the job with the given name and id
      *
      * @param name The name of the job to return
      * @param id The id of the job to return
      * @return The job
      */
     @GET
     @Path("/{name}/{id}")
     @Produces({ OTTO_JOBS_JSON, OTTO_JOBS_XML
     /* The next two media types will be removed on 01/12/2012 */ , OTTO_JOB_XML, OTTO_JOB_JSON })
     public Response getJob(@PathParam("name") final String name, @PathParam("id") final String id) {
         final JobInfo jobInfo = jobInfoService.getById(id);
         if (jobInfo == null || !jobInfo.getName().equals(name)) {
             return Response.status(Response.Status.NOT_FOUND).build();
         } else {
             return Response.ok(JobInfoRepresentation.fromJobInfo(jobInfo)).build();
         }
     }
 
     /**
      * <b>INTERNAL API, DO NOT USE</b>
      * Returns a map with the distinct job names as the key and the jobs with the given name as their values.
      *
      * @param hours The hours the jobs go back into the past
      * @return The map of distinct names with their jobs as values
      */
     @GET
     @Path("/history")
     public Response getJobsHistory(@QueryParam("hours") @DefaultValue("12") final int hours) {
         final Set<String> jobNames = jobService.listJobNames();
         final Map<String, List<JobInfoRepresentation>> jobs = new HashMap<String, List<JobInfoRepresentation>>();
        final Date dt = new Date(new Date().getTime() - 1000 * 60 * hours);
         for (String jobName : jobNames) {
             final List<JobInfo> jobInfoList = jobInfoService.getByNameAndTimeRange(jobName, dt);
             final List<JobInfoRepresentation> jobInfoRepresentations = new ArrayList<JobInfoRepresentation>();
             for (JobInfo jobInfo : jobInfoList) {
                 jobInfoRepresentations.add(JobInfoRepresentation.fromJobInfo(jobInfo));
             }
             jobs.put(jobName, jobInfoRepresentations);
         }
         return Response.ok(jobs).build();
     }
 
     private Feed createFeed(final Abdera abdera, String title, final String subTitle, final URI feedLink) {
         final Feed feed = abdera.newFeed();
         feed.setId("urn:uuid:" + UUID.randomUUID().toString());
         feed.setTitle(title);
         feed.setSubtitle(subTitle);
         feed.setUpdated(new Date());
         feed.addLink(feedLink.getPath(),"self");
         return feed;
     }
 
 }
