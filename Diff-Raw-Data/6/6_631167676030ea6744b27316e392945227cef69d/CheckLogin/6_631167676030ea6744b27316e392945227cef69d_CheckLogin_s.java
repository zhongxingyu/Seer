 package jobmatch.presentation;
 
 import jobmatch.business.provider.account.*;
 import jobmatch.business.util.TimeUtil;
 
 import com.lutris.appserver.server.session.*;
 import com.lutris.appserver.server.httpPresentation.*;
 
 
 /**
  * Checks the login of a CanidateAccount
  **/
 public class CheckLogin implements HttpPresentation {
 
     public final String DEFAULT_GRANT_CANDIDATE = "./CandidateHome.po";
     public final String DEFAULT_GRANT_COMPANY = "./CompanyHome.po";
     public final String DEFAULT_GRANT_PROVIDER = "./admin/ControlCenter.po";
     public final String DEFAULT_DENY = "./Welcome.po";
 
     public static String getLoginURL(int type, String username, String passphrase,
 				     String grantURL, String denyURL) {
 	String url = "./CheckLogin.po?username=" + username + "&passphrase=" +
 	    passphrase + "&grantURL=" + grantURL + "&denyURL=" + denyURL;
 	if (type == Account.TYPE_CANDIDATE) {
 	    url = url + "&type=candidate";
 	}
 	if (type == Account.TYPE_COMPANY) {
 	    url = url + "&type=company";
 	}
 	if (type == Account.TYPE_PROVIDER) {
 	    url = url + "&type=provider";
 	}
 	return url;
     }
 
     public void run(HttpPresentationComms comms) 
         throws HttpPresentationException {
 
 	final String accType = comms.request.getParameter("type");
 	final String username = comms.request.getParameter("username");
 	final String passphrase = comms.request.getParameter("passphrase");
 	String grantURL = comms.request.getParameter("grantURL");
 	String denyURL = comms.request.getParameter("denyURL");
 
 	final AccountManager manager = AccountManager.getUniqueInstance();
 	Account account = null;
 
 	if (denyURL == null) {
 	    denyURL = DEFAULT_DENY;
 	}
 
 	if (accType == null || accType.equals("candidate")) {
 	    if (manager.isValidCandidateLogin(username, passphrase)) {
 		account = manager.getCandidateAccount(username);
 		if (grantURL == null) {
 		    grantURL = DEFAULT_GRANT_CANDIDATE;
 		}
 	    }
 	} else {
 	    if (accType.equals("company")) {
 		if (manager.isValidCompanyLogin(username, passphrase)) {
 		    account = manager.getCompanyAccount(username);
 		    if (grantURL == null) {
 			grantURL = DEFAULT_GRANT_COMPANY;
 		    }
 		}
 	    } else {
 		if (accType.equals("provider")) {
 		    if (manager.isValidProviderLogin(username, passphrase)) {
 			account = manager.getProviderAccount(username);
 			if (grantURL == null) {
 			    grantURL = DEFAULT_GRANT_PROVIDER;
 			}
 		    }
 		}
 	    }
 	}
 	this.setAccount(comms.sessionData, account);       
 	if (account != null) {
 	    account.updateLoginData(TimeUtil.getTimeNow(),
 				    comms.request.getRemoteHost(),
 				    comms.request.getRemoteAddr());
 	    throw new ClientPageRedirectException(
 			  comms.request.getAppFileURIPath(grantURL));
 	} else {
 	    throw new ClientPageRedirectException(
 			  comms.request.getAppFileURIPath(denyURL));
 	}
     }
     
     /**
      * Stores the account in the session data
      **/
     private void setAccount(SessionData sessionData, Account account) {
 	try {
	    sessionData.set("account", account);
 	} catch (Exception e) {
 	    throw new RuntimeException(e.toString());
 	}
     }
 
 } //class
