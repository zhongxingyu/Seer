 package session;
 
 import models.Contact;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.test.Fixtures;
 
 @OnApplicationStart
 public class Startup extends Job
 {
 
 	@Override
 	public void doJob() throws Exception
 	{
		if (Contact.count() == 0)
 		{
 			Fixtures.loadModels("data.yml");
 		}
 	}
 
 }
