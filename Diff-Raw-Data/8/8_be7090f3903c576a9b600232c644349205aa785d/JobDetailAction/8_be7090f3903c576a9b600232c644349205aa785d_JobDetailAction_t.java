 package org.jrecruiter.web.actions;
 
 import java.util.Date;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.interceptor.SessionAware;
 import org.jrecruiter.common.ApiKeysHolder;
 import org.jrecruiter.common.CollectionUtils;
 import org.jrecruiter.model.Job;
 import org.jrecruiter.model.Statistic;
 import org.jrecruiter.service.JobService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  * @author Gunnar Hillert
  * @version $Id:UserService.java 128 2007-07-27 03:55:54Z ghillert $
  */
 
 @Result(name="showJobs", location="show-jobs", type="redirectAction")
 public class JobDetailAction extends BaseAction implements SessionAware {
 
 
     /** serialVersionUID. */
     private static final long serialVersionUID = 369806210598096582L;
 
     /** The primary id of the job posting */
     private Long jobId;
 
     /** The unique id (UUID) of the job posting */
     private String id;
 
     private Job job;
 
     private ApiKeysHolder apiKeysHolder;
 
     private transient @Autowired JobService jobService;
     private Map<String, Object>session;
 
     /**
      * Logger Declaration.
      */
     private final static Logger LOGGER = LoggerFactory.getLogger(JobDetailAction.class);
 
     @SuppressWarnings("unchecked")
     public void setSession(Map session) {
         this.session = session;
     }
 
     /* (non-Javadoc)
      * @see com.opensymphony.xwork2.ActionSupport#execute()
      */
     @Override
     @SuppressWarnings("unchecked")
     public String execute() throws Exception {
 
         if (this.jobId == null && this.id == null) {
             super.addActionError("Please provide a valid job id.");
             return "showJobs";
         }
 
         if (this.id != null) {
             this.job = jobService.getJobForUniversalId(this.id);
         } else {
             this.job = jobService.getJobForId(jobId);
         }
         if (this.job==null){
 
             String errorMessage = "Requested jobposting with id " + (this.id == null ? this.jobId : this.id) + " was not found.";
             LOGGER.warn(errorMessage);
             super.addActionMessage("The requested job posting does not exist.");
             return "showJobs";
         } else {
 
              Statistic statistics = job.getStatistic();
 
              if (statistics == null) {
 
                  statistics = new Statistic();
                  statistics.setJob(job);
                  statistics.setCounter(Long.valueOf(0));
                  job.setStatistic(statistics);
              }
 
              Set<Long> viewedPostings = CollectionUtils.getHashSet();
 
              if (session.get("visited") != null){
 
                  viewedPostings = (Set<Long>)session.get("visited");
 
                  if (viewedPostings.contains(jobId)){
 
 
                  } else {
                      long counter = statistics.getCounter().longValue() + 1 ;
                      statistics.setCounter(Long.valueOf(counter));
                      viewedPostings.add(jobId);
                  }
 
              } else {
 
                  long counter;
 
                  if (statistics.getCounter() != null)
                  {
                      counter = statistics.getCounter().longValue() + 1 ;
                  } else {
                      counter = 1;
                  }
 
 
                  statistics.setCounter(Long.valueOf(counter));
 
                  viewedPostings.add(jobId);
                  session.put("visited", viewedPostings);
 
              }
 
              statistics.setLastAccess(new Date());
              jobService.updateJobStatistic(statistics);
 
             }
 
     return SUCCESS;
     }
 
     public Long getJobId() {
         return jobId;
     }
 
     public void setJobId(Long jobId) {
         this.jobId = jobId;
     }
 
     public Job getJob() {
         return job;
     }
 
     public void setJob(Job job) {
         this.job = job;
     }
 
     public ApiKeysHolder getApiKeysHolder() {
         return apiKeysHolder;
     }
 
     public void setApiKeysHolder(ApiKeysHolder apiKeysHolder) {
         this.apiKeysHolder = apiKeysHolder;
     }
 
     /**
      * @return the id
      */
     public String getId() {
         return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(String id) {
         this.id = id;
     }
 
 
 
 }
