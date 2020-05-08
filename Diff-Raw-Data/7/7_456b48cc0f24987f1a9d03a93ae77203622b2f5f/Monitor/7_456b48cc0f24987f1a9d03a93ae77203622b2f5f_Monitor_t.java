 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.rsna.isn.prepcontent;
 
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.rsna.isn.prepcontent.dao.JobDao;
 import org.rsna.isn.prepcontent.domain.Exam;
 import org.rsna.isn.prepcontent.domain.Job;
 
 /**
  *
  * @author wtellis
  */
 public class Monitor extends Thread
 {
 	private static final Logger logger = Logger.getLogger(Monitor.class.getName());
 
 	private final ThreadGroup group = new ThreadGroup("workers");
 
 	private boolean keepRunning;
 
 	public Monitor()
 	{
 		super("monitor");
 	}
 
 	@Override
 	public void run()
 	{
 		logger.info("Started monitor thread");
 
 		JobDao dao = new JobDao();
 		keepRunning = true;
 		while (keepRunning)
 		{
 			try
 			{
 				if (group.activeCount() < 5)
 				{
 					Set<Job> jobs = dao.getNewJobs();
 
 
 					for (Job job : jobs)
 					{
 						if(group.activeCount() >= 5)
 							break;
 
 						Exam exam = job.getExam();
 						String status = exam.getStatus();
 						long age = System.currentTimeMillis()
 								- exam.getStatusTimestamp().getTime();
 
						if(age < 0)
						    age = 0;

 						long delay = job.getDelay() * 3600000L;
 
						if(delay < 0)
						    delay = 0;

 						if ("FINALIZED".equals(status) && (age >= delay))
 						{
 							dao.updateStatus(job, Job.IN_PROGRESS, "Started prepare content");
 
 							Worker worker = new Worker(group, job);
 							worker.start();
 						}
 					}
 				}
 
 
 				sleep(1000);
 			}
 			catch (InterruptedException ex)
 			{
 				logger.log(Level.SEVERE, "Monitor thread interrupted", ex);
 
 				break;
 			}
 			catch (Exception ex)
 			{
 				logger.log(Level.SEVERE,
 						"Uncaught exception while processing jobs.", ex);
 
 				break;
 			}
 		}
 
 
 
 		logger.info("Stopped monitor thread");
 	}
 
 	public void stopRunning() throws InterruptedException
 	{
 		keepRunning = false;
 
 		join(10 * 1000);
 	}
 
 }
