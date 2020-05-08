 package com.salil.example.springquartz;
 
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.springframework.scheduling.quartz.QuartzJobBean;
 
 public class MyJob extends QuartzJobBean {
 	private MyTask myTask;
 
 	public void setMyTask(MyTask task) {
 		this.myTask = task;
 	}
      @Override
 	protected void executeInternal(JobExecutionContext context)
 			throws JobExecutionException {
         try {
              myTask.printMe();
         }catch (Exception e){
             e.printStackTrace();
         }finally {

         }

 	}
 }
