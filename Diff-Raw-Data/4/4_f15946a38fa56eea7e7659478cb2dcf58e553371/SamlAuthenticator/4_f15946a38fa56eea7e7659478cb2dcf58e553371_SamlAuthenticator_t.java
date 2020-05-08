 package oauthsample.oauth;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import javax.xml.stream.XMLStreamException;
 
 import org.restlet.Context;
 import org.restlet.Request;
 import org.restlet.data.ClientInfo;
 import org.restlet.data.Form;
 import org.restlet.data.Method;
 import org.restlet.representation.Representation;
 import org.restlet.security.Authenticator;
 import org.restlet.security.Enroler;
 import org.restlet.security.User;
 
 import com.onelogin.AccountSettings;
 import com.onelogin.AppSettings;
 import com.onelogin.saml.AuthRequest;
 import com.onelogin.saml.Response;
 
 public class SamlAuthenticator extends Authenticator {
 
     public SamlAuthenticator(Context context, boolean multiAuthenticating, boolean optional, Enroler enroler) {
 	super(context, multiAuthenticating, optional, enroler);
     }
 
     public SamlAuthenticator(Context context, boolean optional, Enroler enroler) {
 	super(context, optional, enroler);
     }
 
     public SamlAuthenticator(Context context, boolean optional) {
 	super(context, optional);
     }
 
     public SamlAuthenticator(Context context) {
 	super(context);
     }
 
     @Override
     protected boolean authenticate(Request request, org.restlet.Response response) {
 	Representation entity = request.getEntity();
 	Form form = new Form(entity);
 	if (form.getFirst("SAMLResponse") == null) {
 	    // no SAMLResponse parameter found, redirect to SAML-login page
 	    redirectToSamlIdp(response);
 	} else {
 	    String samlResponseEncoded = form.getFirst("SAMLResponse").getValue();
 	    if (samlResponseEncoded != null) {
 		Response samlResponse = consume(samlResponseEncoded);
 
 		// create ClientInfo
 		User user = createUserFromResponse(samlResponse);
 		ClientInfo clientInfo = request.getClientInfo();
 		clientInfo.setUser(user);
 		clientInfo.setAuthenticated(true);
 
 		// Add the roles for the authenticated subject
 		if (getEnroler() != null) {
 		    getEnroler().enrole(clientInfo);
 		}
 
 		// request is incoming via POST
 		request.setMethod(Method.GET);
 
 		return true;
 	    }
 	}
 
 	return false;
     }
 
     /**
      * nameId sample: jvwilge@surfguest.nl
      * 
      * @param consume
      * @return
      */
     private User createUserFromResponse(Response consume)  {
 	try {
 	    User user;
 	    user = new User(consume.getNameId().split("@")[0]);
 	    return user;
 	} catch (Exception e) {
 	    throw new RuntimeException(e);
 	}
     }
 
     private Response consume(String samlResponseEncoded) {
 	String certificateS = "MIIEHjCCAwagAwIBAgILAQAAAAABFg7hy6swDQYJKoZIhvcNAQEFBQAwXzELMAkGA1UEBhMCQkUxEzARBgNVBAoTCkN5YmVydHJ1c3QxFzAVBgNVBAsTDkVkdWNhdGlvbmFsIENBMSIwIAYDVQQDExlDeWJlcnRydXN0IEVkdWNhdGlvbmFsIENBMB4XDTA3MTEwNTA4MTYyN1oXDTEwMTEwNTA4MTYyN1owTTELMAkGA1UEBhMCTkwxEDAOBgNVBAoTB1NVUkZuZXQxETAPBgNVBAsTCFNlcnZpY2VzMRkwFwYDVQQDExBlc3BlZS5zdXJmbmV0Lm5sMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDIcRvAXwUWlE12NCkgPR9V6G90tu9C4gQ3cMGzF3b8fGg5A01/F7TcIMNohFFEee4GiYWmU5+xRZByQw5BXpSlqbDn/G3QSJqOcXzxcelWY8jlJUHx91ved6aSvDXZx5Jkv9wP1ZVOKWRfKOGENqwNQZeUEiKUhrYu5wEBsBpDMwIDAQABo4IBbzCCAWswUAYDVR0gBEkwRzBFBgcqhkixPgEAMDowOAYIKwYBBQUHAgEWLGh0dHA6Ly93d3cuZ2xvYmFsc2lnbi5uZXQvcmVwb3NpdG9yeS9jcHMuY2ZtMA4GA1UdDwEB/wQEAwIFoDAfBgNVHSMEGDAWgBRlZaM91zsRowoHJTfJQkpbdndQ4TAdBgNVHQ4EFgQUHZSyxc2114FHO3aPxNUz0xKzfDgwOgYDVR0fBDMwMTAvoC2gK4YpaHR0cDovL2NybC5nbG9iYWxzaWduLm5ldC9lZHVjYXRpb25hbC5jcmwwTwYIKwYBBQUHAQEEQzBBMD8GCCsGAQUFBzAChjNodHRwOi8vc2VjdXJlLmdsb2JhbHNpZ24ubmV0L2NhY2VydC9lZHVjYXRpb25hbC5jcnQwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMBsGA1UdEQQUMBKCEGVzcGVlLnN1cmZuZXQubmwwDQYJKoZIhvcNAQEFBQADggEBAAojubZKLWWymxYMTmYUxpxm1Me1FPvd8HlmPg5gWdmbw8piO92thM9KROLpG/zK5GKC4ZvrsEPpvToCQpPVyEVf3DpCOJKOOIv3//a5BKuhWLTtzqJsAvIp2BS7e4p1GuKnYhOvlxf3pueAmJAd3Z2/V0VIpHhzCKehI1pvZr/P7auQplTWnbPplAcwvaLxvovpzhXRDXWPVqfjGBKlXvJQEIR8mXvOI/ZzU/5sZH7CAZstly5TVqn1MGodCZEdIMv2I9tj5k+dv3/Z/x2lm9QAmvvqVzzXlAXVg2kJZ5zWzr6qKeyUJsnOwtE2lGjexPGy2H0ezUpfFImKgOl4dWE=";
 
 	AccountSettings accountSettings = new AccountSettings();
 	accountSettings.setCertificate(certificateS);
 
 	try {
 	    Response samlResponse = new Response(accountSettings);
 	    samlResponse.loadXmlFromBase64(samlResponseEncoded);
 
 	    if (samlResponse.isValid()) {
 		String nameId = samlResponse.getNameId();
 		System.out.println(nameId);
 		return samlResponse;
 	    } else {
 		System.out.println("FAIL");
 		return null;
 	    }
 	} catch (Exception e) {
 	    throw new RuntimeException(e);
 	}
 
     }
     
     
     private void redirectToSamlIdp(org.restlet.Response response) {
 	// the appSettings object contain application specific settings used by
 	// the SAML library
 	AppSettings appSettings = new AppSettings();
 
 	// set the URL of the consume.jsp (or similar) file for this app. The
 	// SAML Response will be posted to this URL
	// TODO uitzoeken wat dit doet
	appSettings.setAssertionConsumerServiceUrl("http://statusnet.surfnetlabs.nl:9090/oauth/authorize?response_type=token&client_id=1234567890&redirect_uri=http://statusnet.surfnetlabs.nl:8000/status.html");
 
 	// set the issuer of the authentication request. This would usually be
 	// the URL of the issuing web application
 	appSettings.setIssuer("http://statusnet.surfnetlabs.nl");
 
 	// the accSettings object contains settings specific to the users
 	// account.
 	// At this point, your application must have identified the users origin
 	AccountSettings accSettings = new AccountSettings();
 
 	// The URL at the Identity Provider where to the authentication request
 	// should be sent
 	accSettings.setIdpSsoTargetUrl("https://espee-test.surfnet.nl/federate/saml20");
 
 	// Generate an AuthRequest and send it to the identity provider
 	AuthRequest authReq = new AuthRequest(appSettings, accSettings);
 	try {
 	    String reqString = accSettings.getIdp_sso_target_url() + "?SAMLRequest="
 		    + AuthRequest.getRidOfCRLF(URLEncoder.encode(authReq.getRequest(AuthRequest.base64), "UTF-8"));
 	    response.redirectPermanent(reqString);
 	} catch (Exception e) {
 	    throw new RuntimeException(e);
 	}
     }
 
 }
