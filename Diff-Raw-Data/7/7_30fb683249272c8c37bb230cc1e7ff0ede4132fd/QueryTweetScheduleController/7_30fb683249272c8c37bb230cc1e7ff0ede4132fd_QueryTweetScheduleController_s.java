 package edu.nccu.floodfire.controller;
 
 import static org.quartz.CronScheduleBuilder.cronSchedule;
 import static org.quartz.JobBuilder.newJob;
 import static org.quartz.TriggerBuilder.newTrigger;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.quartz.CronTrigger;
 import org.quartz.JobDataMap;
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.SimpleTrigger;
 import org.quartz.impl.StdSchedulerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import edu.nccu.floodfire.batch.TwitterJob;
 import edu.nccu.floodfire.constants.SystemConstants;
 import edu.nccu.floodfire.entity.Event;
 import edu.nccu.floodfire.entity.Job;
 import edu.nccu.floodfire.service.ManagementService;
 import edu.nccu.floodfire.util.DateUtil;
 import edu.nccu.floodfire.util.JobseqUtil;
 import edu.nccu.floodfire.util.TwitterConfiguration;
 
 public class QueryTweetScheduleController implements Controller {
 	private String viewPage;
 
 	/**
 	 * 1.使用者設定好Job資訊後，送出後會到此controller
 	 * 2.此controller會啟動一隻scheduler Job，參看TwitterJob.java
 	 * 3.TwitterJob.java需要tokenId當參數，2013/8/4修改規格為系統任取一個token
 	 * 4.系統會回傳此job的jobSeq和還剩下多少token可以使用回畫面
 	 */
 	@SuppressWarnings({ "rawtypes"})
 	public ModelAndView handleRequest(HttpServletRequest req,
 			HttpServletResponse res) throws Exception {
 		req.setCharacterEncoding("UTF-8");
 	
 		ApplicationContext applicationContext =WebApplicationContextUtils.getWebApplicationContext(req.getSession().getServletContext());
 		ManagementService managementService = (ManagementService)applicationContext.getBean("managementService");
 		
 		String queryType = req.getParameter("queryType");
 		String queryFunc = req.getParameter("queryFunc");
 		String addJobEventName = req.getParameter("addJobEventName");
 
 		Event event = new Event();
 		event.setEventName(addJobEventName);
 		String user = (String)req.getSession().getAttribute("user");
 		
 		String thisDate = DateUtil.getCurrentYYYYMMDD();
 										
 	    String jobSeq = "";
 	    String keyword = "";
 		if("search".equals(queryType))
 		{
 			if("byKeyword".equals(queryFunc))
 			{
 				keyword = req.getParameter("keyword");
 				jobSeq = thisDate + "?" + "queryType" + "=" + queryType +"&" + "keyword" + "=" + keyword +"&" + "user"+"="+user;				
 				JobseqUtil.setSearchJobseq(jobSeq);
 				Scheduler scheduler = createScheduler(jobSeq);				
 				createSearchJob(jobSeq,queryType,queryFunc,keyword, managementService, applicationContext,scheduler);
 				putSchedulerPool(scheduler);
 				addJobToDatabase(jobSeq,user,queryType,queryFunc,event,keyword,managementService);
 			}
 			
 			
 		}
 		else if("stream".equals(queryType))
 		{
 			if("byKeyword".equals(queryFunc))
 			{
 				keyword = req.getParameter("keyword");	
 				jobSeq = thisDate + "?" + "queryType" + "=" + queryType +"&" + "keyword" + "=" + keyword +"&" + "user"+"="+user;
 				JobseqUtil.setStreamJobseq(jobSeq);
 				Scheduler scheduler = createScheduler(jobSeq);
 				createStreamJob(jobSeq,queryType,queryFunc,keyword,managementService,applicationContext,scheduler);
 				putSchedulerPool(scheduler);
 				addJobToDatabase(jobSeq,user,queryType,queryFunc,event,keyword,managementService);
 			}
 			
 		}
 		else if("mix".equals(queryType))
 		{
 			if("byKeyword".equals(queryFunc))
 			{
 				keyword = req.getParameter("keyword");
 				String jobSeq1 = thisDate + "?" + "queryType" + "=" + "search" +"&" + "keyword" + "=" + keyword +"&" + "user"+"="+user;
 				JobseqUtil.setSearchJobseq(jobSeq1);
				Scheduler scheduler = createScheduler(jobSeq);
 				createSearchJob(jobSeq1,"search",queryFunc,keyword, managementService, applicationContext,scheduler);
 				putSchedulerPool(scheduler);
 				addJobToDatabase(jobSeq1,user,"search",queryFunc,event,keyword,managementService);
 				
 				String jobSeq2 = thisDate + "?" + "queryType" + "=" + "stream" +"&" + "keyword" + "=" + keyword +"&" + "user"+"="+user;
 				JobseqUtil.setStreamJobseq(jobSeq2);
				scheduler = createScheduler(jobSeq);
				createStreamJob(jobSeq,queryType,queryFunc,keyword,managementService,applicationContext,scheduler);
 				putSchedulerPool(scheduler);
 				addJobToDatabase(jobSeq2,user,"stream",queryFunc,event,keyword,managementService);
 				jobSeq = jobSeq1 +";<br>"+ jobSeq2;
 			}
 		}
 		
 		
 		List<?> list = managementService.queryAllToken();
 		int tokenUsable =0;
 		for(int i=0;i<list.size();i++)
 		{
 			List sublist = (List)list.get(i);
 			if("0".equals(sublist.get(2)))
 			{
 				tokenUsable ++;
 			}
 		}
 		Object[] o =new Object[2];
 		o[0] = tokenUsable;
 		o[1] = jobSeq;
 		return new ModelAndView(viewPage,"o",o);
 	}
 	
 	@SuppressWarnings({ "rawtypes"})
 	private void createSearchJob(String jobSeq,String queryType,String queryFunc,String keyword,ManagementService managementService,ApplicationContext applicationContext,Scheduler scheduler)
 	{
 		
 			try {
 				String[] rtn = managementService.lockRandomTokenAndFrequencyToUse(jobSeq);	
 				String tokenId = rtn[0];
 				String frequency = rtn[1];
 				
 				
 				List list = (List)managementService.getTokenByWildCard(null, tokenId);
 				List subList = (List)list.get(0);
 				TwitterConfiguration twitterConfiguration = new TwitterConfiguration(""+subList.get(4),""+subList.get(5),
 						""+subList.get(2), ""+subList.get(3));
 				
 				JobDataMap jobDataMap = new JobDataMap();
 				jobDataMap.put("twitterConfiguration", twitterConfiguration);
 				jobDataMap.put("applicationContext", applicationContext);
 				jobDataMap.put("jobSeq", jobSeq);
 				
 				
 				
 				JobDetail job = newJob(TwitterJob.class)
 				   .withIdentity("myJob", "group1") // name "myJob", group "group1"
 				   .usingJobData("queryType", "search")
 				   .usingJobData("keyword", keyword)
 				   .usingJobData(jobDataMap)
 				   .build();
 				
 				CronTrigger trigger = newTrigger()
 				   .withIdentity("trigger1", "group1")
 				   .withSchedule(cronSchedule("0/"+frequency+" * * * * ?"))
 				   .build();
 				
 				
 				scheduler.scheduleJob(job, trigger);
 				scheduler.start();
 			} catch (SchedulerException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 									
 	}
 	
 	@SuppressWarnings({ "rawtypes"})
 	private void createStreamJob(String jobSeq,String queryType,String queryFunc,String keyword,ManagementService managementService,ApplicationContext applicationContext,Scheduler scheduler)
 	{
 		
 			try {
 				String[] rtn = managementService.lockRandomTokenAndFrequencyToUse(jobSeq);	
 				String tokenId = rtn[0];
 				
 				
 				List list = (List)managementService.getTokenByWildCard(null, tokenId);
 				List subList = (List)list.get(0);
 				TwitterConfiguration twitterConfiguration = new TwitterConfiguration(""+subList.get(4),""+subList.get(5),
 						""+subList.get(2), ""+subList.get(3));
 				
 				JobDataMap jobDataMap = new JobDataMap();
 				jobDataMap.put("twitterConfiguration", twitterConfiguration);
 				jobDataMap.put("applicationContext", applicationContext);
 				jobDataMap.put("jobSeq", jobSeq);
 				
 				
 				
 				JobDetail job = newJob(TwitterJob.class)
 				.withIdentity("job1", "group1") // name "myJob", group "group1"
 				.usingJobData("queryType", "stream")
 				.usingJobData("keyword", keyword)
 				.usingJobData(jobDataMap)
 				.build();
 				
 				SimpleTrigger trigger = (SimpleTrigger) newTrigger() 
 			    .withIdentity("trigger1", "group1")
 			    .startAt(new Date()) // some Date 
 			    .forJob("job1", "group1") // identify job with name, group strings
 			    .build();
 
 				scheduler.scheduleJob(job, trigger);
 				scheduler.start();
 				
 			} catch (SchedulerException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 									
 	}
 	
 	
 	
 	private void addJobToDatabase(String jobSeq,String user,String queryType,String queryFunc,Event event,String keyword,ManagementService managementService)
 	{
 		Job job = new Job();
 		job.setJob_Seq(jobSeq);
 		job.setJobStatus("1");
 		job.setCreateId(user);
 		if("search".equals(queryType))
 		{
 			job.setQueryType("1");
 		}
 		else if("stream".equals(queryType))
 		{
 			job.setQueryType("2");
 		}
 		
 		job.setQueryFunction(queryFunc);
 		job.setStartTime(new Date()+"");
 		job.setEvent(event);
 		job.setKeyword(keyword);
 		managementService.addJob(job);
 	}
 
 	public void setViewPage(String viewPage) {
 		this.viewPage = viewPage;
 	}
 
 	public Scheduler createScheduler(String jobSeq)
 	{
 
 		StdSchedulerFactory sf = new StdSchedulerFactory(); 
 		Properties props = new Properties(); 
 		props.put("org.quartz.scheduler.instanceName", jobSeq); 
 		props.put("org.quartz.threadPool.threadCount", "3"); 
 		Scheduler scheduler = null;
 		try {
 			sf.initialize(props); 
 			scheduler = sf.getScheduler();
 		} catch (SchedulerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		return scheduler;
 
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void putSchedulerPool(Scheduler scheduler)
 	{
 		try {
 			SystemConstants.SCHEDULER_POOL.put(scheduler.getSchedulerName(), scheduler);
 		} catch (SchedulerException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
