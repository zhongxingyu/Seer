 package org.vpac.grisu.frontend.examples;
 
 import java.util.Date;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.bushe.swing.event.EventBus;
 import org.bushe.swing.event.annotation.AnnotationProcessor;
 import org.bushe.swing.event.annotation.EventSubscriber;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.frontend.control.login.LoginParams;
 import org.vpac.grisu.frontend.control.login.ServiceInterfaceFactory;
 import org.vpac.grisu.frontend.model.StatusObject;
 import org.vpac.grisu.frontend.model.events.BatchJobEvent;
 import org.vpac.grisu.frontend.model.events.SystemOutStatusLogger;
 import org.vpac.grisu.frontend.model.job.JobObject;
 import org.vpac.grisu.frontend.model.job.JobsException;
 import org.vpac.grisu.frontend.model.job.BatchJobObject;
 import org.vpac.grisu.model.GrisuRegistry;
 import org.vpac.grisu.model.GrisuRegistryManager;
 import org.vpac.grisu.model.dto.DtoActionStatus;
 import org.vpac.grisu.model.dto.DtoBatchJob;
 
 import au.org.arcs.jcommons.constants.Constants;
 
 public class MultiJobSubmit {
 	
 	public MultiJobSubmit() {
 		AnnotationProcessor.process(this);
 	}
 
 	public static void main(final String[] args) throws Exception {
 
 
 		ExecutorService executor = Executors.newFixedThreadPool(10);
 
 		String username = args[0];
 		char[] password = args[1].toCharArray();
 
 		LoginParams loginParams = new LoginParams(
 //				"http://localhost:8080/xfire-backend/services/grisu",
 //				"https://ngportal.vpac.org/grisu-ws/soap/EnunciateServiceInterfaceService",
 //				 "https://ngportal.vpac.org/grisu-ws/services/grisu",
 //				"https://ngportal.vpac.org/grisu-ws/soap/GrisuService",
 //				"http://localhost:8080/enunciate-backend/soap/GrisuService",
 				 "Local",
 //				 "ARCS_DEV",
 //				"Dummy",
 				username, password);
 
 		final ServiceInterface si = ServiceInterfaceFactory
 				.createInterface(loginParams);
 
 
 		final GrisuRegistry registry = GrisuRegistryManager.getDefault(si);
 		
 		registry.getApplicationInformation("java").getAvailableSubmissionLocationsForFqan("/ARCS/NGAdmin");
 
 		final int numberOfJobs = 10;
 		
 		Date start = new Date();
 		final String multiJobName = "10jobs2";
 		try {
 			si.kill(multiJobName, true);
 
 			StatusObject status = new StatusObject(si, multiJobName, StatusObject.Listener.STDOUT);
 			status.waitForActionToFinish(3, true, true);
 			
 		} catch (Exception e) {
 			// doesn't matter
 			e.printStackTrace();
 		}
 		
 		
 //		System.out.println("Start: "+start.toString());
 //		System.out.println("End: "+new Date().toString());
 //		System.exit(1);
 
 //		SystemOutStatusLogger ssoes = new SystemOutStatusLogger(multiJobName);
 
		BatchJobObject multiPartJob = new BatchJobObject(si, multiJobName, "/ARCS/NGAdmin", "Java", Constants.NO_VERSION_INDICATOR_STRING);
 			
 		
 
 		
 		String pathToInputFiles = multiPartJob.pathToInputFiles();
 		
 		for (int i=0; i<numberOfJobs; i++) {
 
 			final int frameNumber = i;
 				
 				JobObject jo = new JobObject(si);
 				jo.setJobname(multiJobName+"_" + frameNumber );
//				jo.setApplication("java");
 //				jo.setCommandline("java -version");
 				jo.setCommandline("cat singleJobFile.txt "+pathToInputFiles+"/multiJobFile.txt");
 				jo.setWalltimeInSeconds(60*21);
 				jo.addInputFileUrl("/home/markus/test/singleJobFile.txt");
 
 				multiPartJob.addJob(jo);
 						
 		}
 
 		multiPartJob.addInputFile("/home/markus/test/multiJobFile.txt");
 //		multiPartJob.setDefaultApplication("java");
 		multiPartJob.setSitesToExclude(new String[]{"tpac"});
 		
 //		multiPartJob.setSitesToExclude(new String[]{"vpac", "massey", "uq", "canterbury", "sapac", "ivec", "otago"});
 		
 		multiPartJob.setDefaultNoCpus(1);
 		multiPartJob.setDefaultWalltimeInSeconds(60*21);
 		
		multiPartJob.setSitesToExclude(new String[]{"tpac"});
		
 
 //		multiPartJob.fillOrOverwriteSubmissionLocationsUsingMatchmaker();
 		
 		try {
 			multiPartJob.prepareAndCreateJobs(true);
 		} catch (JobsException e) {
 			for ( JobObject job : e.getFailures().keySet() ) {
 				System.out.println("Creation "+job.getJobname()+" failed: "+e.getFailures().get(job).getLocalizedMessage());
 			}
 			System.exit(1);
 		}
 		
 		System.out.println(si.getJobProperty(multiPartJob.getBatchJobname(), Constants.OPTIMIZE_STATS));
 		
 		multiPartJob.submit();
 		
 
 		System.out.println("Submission finished: "+new Date());
 		
 
 		while ( ! multiPartJob.isFinished(true) ) {
 			System.out.println("Not finished yet...");
 			multiPartJob.getJobs().size();
 
 			Thread.sleep(2000);
 		}
 		
 		for ( JobObject job : multiPartJob.getJobs() ) {
 			System.out.println("-------------------------------");
 			System.out.println(job.getJobname()+": "+job.getStatusString(false));
 			System.out.println(job.getStdOutContent());
 			System.out.println("-------------------------------");
 			System.out.println(job.getStdErrContent());
 			System.out.println("-------------------------------");
 			System.out.println();
 		}
 
 		
 //		if ( HibernateSessionFactory.HSQLDB_DBTYPE.equals(HibernateSessionFactory.usedDatabase) ) {
 //			// for hqsqldb
 //			Thread.sleep(10000);
 //		}
 		
 //		MultiPartJobObject newObject = new MultiPartJobObject(si, multiJobName);
 //		
 //		newObject.monitorProgress();
 //		
 //		newObject.downloadResults("logo");
 
 		
 	}
 
 	   @EventSubscriber(eventClass=BatchJobEvent.class)
 	   public void onMultiPartJobEvent(BatchJobEvent event) {
 
 		   System.out.println("Event: "+event.getMessage());
 		   
 	   }
 
 
 }
