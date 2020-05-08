 package grisu.backend.model.job.gt5;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.log4j.Logger;
 import org.globus.gram.Gram;
 import org.globus.gram.GramException;
 import org.globus.gram.GramJob;
 import org.globus.gram.internal.GRAMConstants;
 import org.ietf.jgss.GSSCredential;
 import org.ietf.jgss.GSSException;
 
 public class Gram5Client { 
 
 	static final Logger myLogger = Logger
 			.getLogger(Gram5Client.class.getName());
 
 	public Gram5Client() {
 	}
 
 	private String getContactString(String handle) {
 		try {
 			final URL url = new URL(handle);
 			myLogger.debug("job handle is " + handle);	
 			myLogger.debug("returned handle is " + url.getHost());
 			return url.getHost();
 		} catch (final MalformedURLException ex1) {
 			myLogger.error(ex1.getLocalizedMessage());
 			return null;
 		}
 	}
 
 	public int[] getJobStatus(String handle, GSSCredential cred) {
 
 		final int[] results = new int[2];
 		final Gram5JobListener l = Gram5JobListener.getJobListener();
 
 		// we need this to catch quick failure
 		//Integer status = l.getStatus(handle);
 		Integer status = null;
 		
 		myLogger.debug("job status is " + status);
 		if (status != null) {
 			results[0] = status;
 			results[1] = l.getError(handle);
 			return results;
 		} 
 
 		final String contact = getContactString(handle);
 		GramJob job = new GramJob(null);
 		GramJob restartJob = new GramJob(null);
 		
 		try {
 			// lets try to see if gateway is working first...
 			Gram.ping(cred,contact);
 		} catch (final GramException ex) {
 			myLogger.info(ex);
 			// have no idea what the status is, gateway is down:
 			return new int[] { GRAMConstants.STATUS_UNSUBMITTED, 0 };
 		} catch (final GSSException ex) {
 			myLogger.error(ex);
 		}
 
 		try {
 			job.setID(handle);
 			job.setCredentials(cred);
 			job.bind();
 			job.addListener(l);
 			Gram.jobStatus(job);
 			myLogger.debug("job status is " + job.getStatusAsString());
 			myLogger.debug("job error is " + job.getError());
 		} catch (final GramException ex) {
 			myLogger.debug("ok, normal method of getting exit status is not working. need to restart job.");
 			if (ex.getErrorCode() == 156  || ex.getErrorCode() == 12/* job contact not found*/) {
 				// maybe the job finished, but maybe we need to kick job manager
 
 				myLogger.debug("restarting job");
 				final String rsl = "&(restart=" + handle + ")";
 				restartJob = new GramJob(rsl);
 				restartJob.setCredentials(cred);
 				//restartJob.addListener(this);
 				try {
 
 					restartJob.request(contact, false);
 				} catch (final GramException ex1) {
 					// ok, now we are really done
 					return new int[] { GRAMConstants.STATUS_DONE, 0 };
 				} catch (final GSSException ex1) {
 					throw new RuntimeException(ex1);
 				}
 
 				// nope, not done yet.
 				return getJobStatus(handle, cred);
 			} else {
 				myLogger.error("something else is wrong. error code is " + ex.getErrorCode());
 				myLogger.error(ex);
 			}
 			
 		} catch (final GSSException ex) {
 			myLogger.error(ex);
 		} catch (final MalformedURLException ex) {
 			myLogger.error(ex);
 		}
 
 		status = job.getStatus();
 
 		final int error = job.getError();
 		return new int[] { status, error };
 	}
 	
 	private void unbindJob(GramJob job){
 		try {
 			job.unbind();
 		} catch (Exception e) {
 			// don't care
 		}
 	}
 
 	public int kill(String handle, GSSCredential cred) {
 		final GramJob job = new GramJob(null);
 		try {			
 			job.setID(handle);
 			job.setCredentials(cred);
 			try {
 				new Gram();
 				Gram.cancel(job);
 				// job.signal(job.SIGNAL_CANCEL);
 			} catch (final GramException ex) {
 				myLogger.error(ex.getLocalizedMessage());
 			} catch (final GSSException ex) {
 				myLogger.error(ex.getLocalizedMessage());
 			}
 			final int status = job.getStatus();
 			return status;
 		} catch (final MalformedURLException ex) {
 			throw new RuntimeException(ex);
 		} finally {
 			unbindJob(job);
 		}
 	}
 
 	/** public void statusChanged(GramJob job) {
 		myLogger.debug("job status changed  " + job.getStatusAsString());
 		statuses.put(job.getIDAsString(), job.getStatus());
 		errors.put(job.getIDAsString(), job.getError());
 		myLogger.debug("the job is : " + job.toString());
 	} **/
 
 	public String submit(String rsl, String endPoint, GSSCredential cred) {
 		final GramJob job = new GramJob(rsl);
 		final Gram5JobListener l = Gram5JobListener.getJobListener();	
 		job.setCredentials(cred);
 		job.addListener(l);
 		try {
 			job.request(endPoint, false);
 			job.bind();
 			//Gram.jobStatus(job);
 			return job.getIDAsString();
 		} catch (final GramException ex) {
 			myLogger.error(ex.getLocalizedMessage());
 			return null;
 		} catch (final GSSException ex) {
 			myLogger.error(ex.getLocalizedMessage());
 			return null;
 		} 
 	}
 }
