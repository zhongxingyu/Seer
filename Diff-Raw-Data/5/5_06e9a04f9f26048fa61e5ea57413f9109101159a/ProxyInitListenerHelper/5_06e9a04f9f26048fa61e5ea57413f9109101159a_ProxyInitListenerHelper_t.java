 package org.italiangrid.voms.clients.impl;
 
 import java.io.File;
 import java.security.cert.X509Certificate;
 import java.util.Arrays;
 import java.util.List;
 
 import org.italiangrid.voms.VOMSAttribute;
 import org.italiangrid.voms.ac.VOMSValidationResult;
 import org.italiangrid.voms.clients.util.MessageLogger;
 import org.italiangrid.voms.clients.util.MessageLogger.MessageLevel;
 import org.italiangrid.voms.clients.util.VOMSAttributesPrinter;
 import org.italiangrid.voms.error.VOMSValidationErrorMessage;
 import org.italiangrid.voms.request.VOMSACRequest;
 import org.italiangrid.voms.request.VOMSErrorMessage;
 import org.italiangrid.voms.request.VOMSServerInfo;
 import org.italiangrid.voms.request.VOMSWarningMessage;
 import org.italiangrid.voms.store.LSCInfo;
 
 import eu.emi.security.authn.x509.ValidationError;
 import eu.emi.security.authn.x509.impl.X500NameUtils;
 import eu.emi.security.authn.x509.proxy.ProxyCertificate;
 
 /**
  * Helper to manage messages related to a voms-proxy-init execution
  * 
  * @author andreaceccanti
  *
  */
 public class ProxyInitListenerHelper implements InitListenerAdapter {
 
 	MessageLogger logger;
 	
 	public enum WARNING_POLICY {
 		printWarnings,
 		failOnWarnings,
 		ignoreWarnings
 	}
 	
 	WARNING_POLICY warningPolicy = WARNING_POLICY.printWarnings;
 	
 	public ProxyInitListenerHelper(MessageLogger logger) {
 		this.logger = logger;
 	}
 	
 	public ProxyInitListenerHelper(MessageLogger logger, WARNING_POLICY warnPolicy) {
 		this.logger = logger;
 		this.warningPolicy = warnPolicy;
 	}
 
 	@Override
 	public void notifyVOMSRequestFailure(VOMSACRequest request,
 			VOMSServerInfo endpoint, Throwable error) {
 		if (endpoint != null)
 			logger.error("Error contacting %s:%d for VO %s: %s\n", endpoint
 					.getURL().getHost(), endpoint.getURL().getPort(), endpoint
 					.getVoName(), error.getMessage());
 		else
 			logger.error(
 					"None of the contacted servers for %s were capable of returning a valid AC for the user.\n",
 					request.getVoName());
 	}
 
 	@Override
 	public void notifyVOMSRequestStart(VOMSACRequest request, VOMSServerInfo si) {
 		logger.info("Contacting %s:%d [%s] \"%s\"...\n", si.getURL().getHost(),
 				si.getURL().getPort(), si.getVOMSServerDN(), si.getVoName());
 	}
 
 	@Override
 	public void notifyVOMSRequestSuccess(VOMSACRequest request,
 			VOMSServerInfo endpoint) {
 		logger.info("Remote VOMS server contacted succesfully.\n");
 	}
 
 	@Override
 	public void notifyValidationResult(VOMSValidationResult result,
 			VOMSAttribute attributes) {
 
 		if (!result.isValid()) {
			logger.error("\nWARNING: VOMS AC validation for VO %s failed for the following reasons:", attributes.getVO());
 			for (VOMSValidationErrorMessage m : result.getValidationErrors())
 				logger.error("\t%s\n", m.getMessage());
 		} else {
			logger.trace("VOMS AC validation for VO %s succeded.\n", attributes.getVO());
 			VOMSAttributesPrinter.printVOMSAttributes(logger,
 					MessageLevel.TRACE, attributes);
 		}
 	}
 
 	@Override
 	public void proxyCreated(String proxyPath, ProxyCertificate cert) {
 		logger.info("\nCreated proxy in %s.\n\n", proxyPath);
 		logger.info("Your proxy is valid until %s\n", cert.getCredential()
 				.getCertificateChain()[0].getNotAfter());
 	}
 
 	@Override
 	public boolean onValidationError(ValidationError error) {
 		logger.warning("Certificate validation error: %s\n", error.getMessage());
 		return false;
 	}
 
 	@Override
 	public void notifyCertficateLookupEvent(String dir) {
 		logger.trace("Looking for VOMS AA certificates in %s...\n", dir);
 
 	}
 
 	@Override
 	public void notifyCertificateLoadEvent(X509Certificate cert, File file) {
 		String certSubject = X500NameUtils.getReadableForm(cert
 				.getSubjectX500Principal());
 		logger.trace(
 				"Loaded VOMS AA certificate with subject %s from file %s\n",
 				certSubject, file.getAbsolutePath());
 	}
 
 	@Override
 	public void notifyLSCLoadEvent(LSCInfo info, File file) {
 		logger.trace("Loaded LSC information from file %s: %s\n",
 				file.getAbsolutePath(), info.toString());
 
 	}
 
 	@Override
 	public void notifyLSCLookupEvent(String dir) {
 		logger.trace("Looking for LSC information in %s...\n", dir);
 	}
 
 	@Override
 	public void notifyCredentialLookup(String... locations) {
 		logger.trace("Looking for user credentials in %s...\n", Arrays.toString(locations));
 
 	}
 
 	@Override
 	public void notifyLoadCredentialSuccess(String... locations) {
 		logger.trace("Credentials loaded successfully %s\n", Arrays.toString(locations));
 	}
 
 	@Override
 	public void notifyLoadCredentialFailure(Throwable error,
 			String... locations) {
 
 		logger.trace("Credentials couldn't be loaded %s: %s\n",
 				Arrays.toString(locations), error.getMessage());
 
 	}
 
 	@Override
 	public void notifyErrorsInVOMSReponse(VOMSACRequest request,
 			VOMSServerInfo si, VOMSErrorMessage[] errors) {
 
 		for (VOMSErrorMessage e : errors)
 			logger.error("ERROR: VOMS server error %d: %s\n", e.getCode(),
 					e.getMessage());
 	}
 
 	@Override
 	public void notifyWarningsInVOMSResponse(VOMSACRequest request,
 			VOMSServerInfo si, VOMSWarningMessage[] warnings) {
 		
 		if (warningPolicy.equals(WARNING_POLICY.printWarnings)){
 			for (VOMSWarningMessage e : warnings)
 				logger.warning("WARNING: VOMS server warning %d: %s\n", e.getCode(),
 						e.getMessage());
 			
 		}else if (warningPolicy.equals(WARNING_POLICY.failOnWarnings)){
 			for (VOMSWarningMessage e : warnings)
 				logger.warning("WARNING: VOMS server warning %d: %s\n", e.getCode(),
 						e.getMessage());
 			System.exit(1);
 		}
 	}
 
 	@Override
 	public void notifyNoValidVOMSESError(List<String> searchedPaths) {
 		logger.info("No valid VOMSES information found locally while looking in: "
 				+ searchedPaths);
 	}
 
 	@Override
 	public void notifyVOMSESlookup(String vomsesPath) {
 		logger.trace("Looking for VOMSES information in %s...\n", vomsesPath);
 	}
 
 	@Override
 	public void notifyVOMSESInformationLoaded(String vomsesPath,
 			VOMSServerInfo info) {
 		if (vomsesPath != null)
 			logger.trace("Loaded vomses information '%s' from %s.\n", info,
 					vomsesPath);
 		else
 			logger.trace("Loaded vomses information '%s'\n", info);
 	}
 
 	@Override
 	public void loadingNotification(String location, String type,
 			Severity level, Exception cause) {
 		if (location.startsWith("file:"))
 			location = location.substring(5, location.length());
 		
 		if (level.equals(Severity.ERROR)){
 			logger.error("Error for %s %s: %s.\n", type, location, cause.getMessage());
 		}else if (level.equals(Severity.WARNING)){
 			logger.trace("Warning for %s %s: %s.\n", type, location, cause.getMessage());
 		}else if (level.equals(Severity.NOTIFICATION)){
 			logger.trace("Loading %s %s.\n", type, location);
 		}
 		
 	}
 }
