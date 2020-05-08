 package edu.acu.wip.quartz.guice;
 
 import org.quartz.Job;
 import org.quartz.SchedulerException;
 import org.quartz.spi.JobFactory;
 import org.quartz.spi.TriggerFiredBundle;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 public class GuiceJobFactory implements JobFactory {
 
 	private final Injector injector;
 
 	@Inject
 	public GuiceJobFactory(Injector inj) {
 		injector = inj;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Job newJob(TriggerFiredBundle triggerFiredBundle)
 			throws SchedulerException {
 
 		Job job = null;
 
 		try {
 			job = (Job) injector.getInstance((Class<? extends Job>)triggerFiredBundle.getJobDetail().getJobClass());
 		} catch (Exception ex) {
 			throw new SchedulerException(ex);
 		}
 
 		return job;
 	}
 
 }
