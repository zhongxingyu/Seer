 import java.util.List;
 
 import play.Application;
 import play.GlobalSettings;
 import play.db.jpa.Transactional;
 
 import common.MailClient;
 import common.MailServerUserManagment;
 import controllers.MailController;
 
 /**
  * This class provides routines which are executed in a global scope.
  * 
  * @author Eugen Meissner
  *
  */
 public class Global extends GlobalSettings{
 
 	private MailServerUserManagment msum = MailServerUserManagment.getInstance();
 	private MailClient mc = MailClient.getInstance();
 		
 	/**
 	 * 
 	 */
 	@Transactional
 	public void onStart(Application app) {
 		super.beforeStart(app);
 		startMailServer();
 		initMailClient();
 		MailController.mailChecker.start();
 	}
 	
 	/**
 	 * 
 	 */
 	public void onStop(Application app){
 		super.onStart(app);
 		stopMailServer();
 		MailController.mailChecker.interrupt();
 	}
 	
 	/**
 	 * 
 	 */
 	private void initMailClient() {
 		mc.setHost(msum.getDomain());
 		mc.setCredentials(msum.getDefaultUsername(), msum.getDefaultPassword());
 	}
 
 	/**
 	 * This method init the domains/host names, on which the mail server should listen.
 	 */
 	private void startMailServer(){
 		List<String> domains = msum.getDomains();
 		domains.add(msum.getDomain());
 		msum.setDomainsOnMailServer(domains);
 		new Thread(){
 			public void run(){
 				msum.startMailServer();
 			}
 		}.start();
 	}
 	
 	/**
 	 * 
 	 */
 	private void stopMailServer(){
 		msum.stopMailServer();
 		msum.clearUsers();
 	}
 	
 }
