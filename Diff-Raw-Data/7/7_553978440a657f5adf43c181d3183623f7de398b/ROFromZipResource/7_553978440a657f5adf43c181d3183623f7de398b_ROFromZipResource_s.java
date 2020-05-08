 package pl.psnc.dl.wf4ever.zip;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 
 import pl.psnc.dl.wf4ever.auth.RequestAttribute;
 import pl.psnc.dl.wf4ever.db.ResearchObjectId;
 import pl.psnc.dl.wf4ever.db.dao.ResearchObjectIdDAO;
 import pl.psnc.dl.wf4ever.exceptions.BadRequestException;
 import pl.psnc.dl.wf4ever.job.Job;
 import pl.psnc.dl.wf4ever.job.JobStatus;
 import pl.psnc.dl.wf4ever.job.JobsContainer;
 import pl.psnc.dl.wf4ever.model.Builder;
 
 import com.sun.jersey.api.NotFoundException;
 
 /**
  * A list of research objects REST APIs.
  * 
  * @author piotrhol
  * 
  */
 @Path("zip/")
 public class ROFromZipResource implements JobsContainer {
 
     /** logger. */
     @SuppressWarnings("unused")
     private static final Logger LOGGER = Logger.getLogger(ROFromZipResource.class);
 
     /** URI info. */
     @Context
     UriInfo uriInfo;
 
     /** Resource builder. */
     @RequestAttribute("Builder")
     private Builder builder;
 
     /** Maximum number of concurrent jobs. */
     public static final int MAX_JOBS = 100;
 
     /** Maximum number of finished jobs kept in memory. */
     public static final int MAX_JOBS_DONE = 100000;
 
     /** Running jobs. */
     private static Map<UUID, Job> jobs = new ConcurrentHashMap<>(MAX_JOBS);
 
     /** Statuses of finished jobs. */
     @SuppressWarnings("serial")
     private static Map<UUID, JobStatus> finishedJobs = Collections
             .synchronizedMap(new LinkedHashMap<UUID, JobStatus>() {
 
                 protected boolean removeEldestEntry(Map.Entry<UUID, JobStatus> eldest) {
                     return size() > MAX_JOBS_DONE;
                 };
             });
 
 
     /**
      * Create a new RO based on a ZIP sent in the request.
      * 
      * @param researchObjectId
      *            slug header
      * @param zipStream
      *            ZIP input stream
      * @return 201 Created
      * @throws BadRequestException
      *             the ZIP content is not a valid RO
      */
     @POST
     @Path("upload")
     @Consumes("application/zip")
     @Produces(MediaType.APPLICATION_JSON)
     public Response storeROFromGivenZip(@HeaderParam("Slug") String researchObjectId, InputStream zipStream)
             throws BadRequestException {
         ArrayList<String> missingParamters = new ArrayList<String>();
         if (zipStream == null) {
             missingParamters.add("Zip file");
         }
         if (researchObjectId == null || researchObjectId.isEmpty()) {
             missingParamters.add("research object id");
         }
         if (!missingParamters.isEmpty()) {
             String errorMessage = "Missing paramters:";
             for (String missingParamter : missingParamters) {
                 errorMessage += "\n" + missingParamter;
             }
             throw new BadRequestException(errorMessage);
         }
         UUID jobUUID = UUID.randomUUID();
         ResearchObjectIdDAO idDAO = new ResearchObjectIdDAO();
        ResearchObjectId uri = idDAO.firstFree(new ResearchObjectId(uriInfo.getAbsolutePath().resolve("/ROs/")
                 .resolve(researchObjectId + "/")));
         ROFromZipJobStatus jobStatus = new ROFromZipJobStatus();
         jobStatus.setProcessedResources(0);
         jobStatus.setSubmittedResources(0);
         jobStatus.setTarget(uri.getId());
         //copy input stream
         File tmpFile = createTmpZip(zipStream);
         StoreROFromGivenZipOperation operation = new StoreROFromGivenZipOperation(new Builder(builder.getUser()),
                 tmpFile, uriInfo);
         Job job = new Job(jobUUID, jobStatus, this, operation);
         jobs.put(jobUUID, job);
         job.start();
         Response result = Response.created(uriInfo.getAbsolutePath().resolve(jobUUID.toString()))
                 .entity(job.getStatus()).build();
         return result;
 
     }
 
 
     /**
      * Create a RO and aggregate resources aggregated in zip.
      * 
      * @param researchObjectId
      *            the id (slug) of new RO
      * @param zipStream
      *            the given zip
      * @return classical response
      * @throws BadRequestException
      *             the given zip is broken.
      */
     @POST
     @Path("create")
     @Consumes("application/zip")
     //@Produces(MediaType.APPLICATION_JSON)
     public Response createROFromGivenZip(@HeaderParam("Slug") String researchObjectId, InputStream zipStream)
             throws BadRequestException {
         ArrayList<String> missingParamters = new ArrayList<String>();
         if (zipStream == null) {
             missingParamters.add("Zip file");
         }
         if (researchObjectId == null || researchObjectId.isEmpty()) {
             missingParamters.add("research object id");
         }
         if (!missingParamters.isEmpty()) {
             String errorMessage = "Missing paramters:";
             for (String missingParamter : missingParamters) {
                 errorMessage += "\n" + missingParamter;
             }
             throw new BadRequestException(errorMessage);
         }
         UUID jobUUID = UUID.randomUUID();
         ResearchObjectIdDAO idDAO = new ResearchObjectIdDAO();
         ResearchObjectId uri;
         if (!researchObjectId.endsWith("/")) {
             researchObjectId += "/";
         }
        uri = idDAO
                .firstFree(new ResearchObjectId(uriInfo.getAbsolutePath().resolve("/ROs/").resolve(researchObjectId)));
 
         ROFromZipJobStatus jobStatus = new ROFromZipJobStatus();
         jobStatus.setTarget(uri.getId());
         jobStatus.setProcessedResources(0);
         jobStatus.setSubmittedResources(0);
         //copy input stream 
         File tmpFile = createTmpZip(zipStream);
         CreateROFromGivenZipOperation operation = new CreateROFromGivenZipOperation(builder, tmpFile, uriInfo);
         Job job = new Job(jobUUID, jobStatus, this, operation);
         jobs.put(jobUUID, job);
         job.start();
         Response result = Response.created(uriInfo.getAbsolutePath().resolve(jobUUID.toString()))
                 .entity(job.getStatus()).build();
         return result;
     }
 
 
     /**
      * Retrieve the job status.
      * 
      * @param uuid
      *            job id
      * @return job status
      */
     @GET
     @Path("{id}")
     public ROFromZipJobStatus getJob(@PathParam("id") UUID uuid) {
         if (jobs.containsKey(uuid)) {
             return (ROFromZipJobStatus) jobs.get(uuid).getStatus();
         }
         if (finishedJobs.containsKey(uuid)) {
             return (ROFromZipJobStatus) finishedJobs.get(uuid);
         }
         throw new NotFoundException("Job not found: " + uuid);
     }
 
 
     //helpers
 
     public static Map<UUID, JobStatus> getFinishedJobs() {
         return finishedJobs;
     }
 
 
     public static void setFinishedJobs(Map<UUID, JobStatus> finishedJobs) {
         ROFromZipResource.finishedJobs = finishedJobs;
     }
 
 
     public static Map<UUID, Job> getJobs() {
         return jobs;
     }
 
 
     public static void setJobs(Map<UUID, Job> jobs) {
         ROFromZipResource.jobs = jobs;
     }
 
 
     //JobsContainerInterface
 
     @Override
     public void onJobDone(Job job) {
         finishedJobs.put(job.getUUID(), job.getStatus());
         jobs.remove(job.getUUID());
     }
 
 
     /**
      * Create an tmp file from a zip given in a request.
      * 
      * @param is
      *            processed zip as a request input stream
      * @return zip written in a tmp file
      */
     private File createTmpZip(InputStream is) {
         try {
             File tmpFile = File.createTempFile("tmp_ro", UUID.randomUUID().toString());
             BufferedInputStream inputStream = new BufferedInputStream(is);
             FileOutputStream fileOutputStream;
             fileOutputStream = new FileOutputStream(tmpFile);
             IOUtils.copy(inputStream, fileOutputStream);
             inputStream.close();
             fileOutputStream.close();
             return tmpFile;
         } catch (IOException e) {
             LOGGER.error("Can't process given zip input stream");
         }
         return null;
     }
 }
