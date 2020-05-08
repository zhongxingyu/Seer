 package org.yogocodes.bikewars.services.impl;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
 import org.yogocodes.bikewars.model.JobModel;
 import org.yogocodes.bikewars.services.JobService;
 
 @Service("jobService")
 public class JobServiceImpl implements JobService {
 
 	private final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);
 
 	public List<JobModel> getAllJobs(final Integer pageNum, final Integer pageSize) {
 
		return null;
 	}
 }
