 package com.paypal.core;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.paypal.core.credential.CertificateCredential;
 import com.paypal.core.credential.ICredential;
 import com.paypal.core.credential.TokenAuthorization;
 import com.paypal.sdk.exceptions.OAuthException;
 
 /**
  * AbstractCertificateHttpHeaderAuthStrategy is an abstract implementation for
  * certificate based {@link ICredential} to be realized as HTTP headers
  */
 public abstract class AbstractCertificateHttpHeaderAuthStrategy implements
 		AuthenticationStrategy<Map<String, String>, CertificateCredential> {
 
 	/**
 	 * Endpoint url
 	 */
 	protected String endPointUrl;
 
 	/**
 	 * AbstractCertificateHttpHeaderAuthStrategy
 	 * 
 	 * @param endPointUrl
 	 *            Endpoint URL
 	 */
 	public AbstractCertificateHttpHeaderAuthStrategy(String endPointUrl) {
 		this.endPointUrl = endPointUrl;
 	}
 
 	/**
 	 * Returns {@link CertificateCredential} as HTTP headers
 	 */
 	public Map<String, String> generateHeaderStrategy(CertificateCredential credential)
 			throws OAuthException {
 		Map<String, String> headers = null;
 		if (credential.getThirdPartyAuthorization() instanceof TokenAuthorization) {
 			headers = processTokenAuthorization(credential,
 					(TokenAuthorization) credential
 							.getThirdPartyAuthorization());
 
 		} else {
 			headers = new HashMap<String, String>();
 			headers.put(Constants.PAYPAL_SECURITY_USERID_HEADER,
 					credential.getUserName());
 			headers.put(Constants.PAYPAL_SECURITY_PASSWORD_HEADER,
 					credential.getPassword());
 		}
 		return headers;
 	}
 
 	/**
	 * Process TokentAuthorization based on API format
 	 * 
 	 * @param credential
 	 *            Instance of {@link CertificateCredential}
 	 * @param tokenAuth
 	 *            Instance of {@link TokenAuthorization}
 	 * @return Map of HTTP headers
 	 * @throws OAuthException
 	 */
 	protected abstract Map<String, String> processTokenAuthorization(
 			CertificateCredential credential, TokenAuthorization tokenAuth)
 			throws OAuthException;
 
 }
