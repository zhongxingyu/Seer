 package org.italiangrid.voms.clients;
 
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import org.italiangrid.voms.clients.strategies.ProxyInitStrategy;
 
 import eu.emi.security.authn.x509.proxy.ProxyType;
 
 /**
  * This class represents the parameters that drive the
  * {@link ProxyInitStrategy} when creating a VOMS proxy.
  * 
  * @author Andrea Ceccanti
  * 
  */
 public class ProxyInitParams {
 
 	public static final int DEFAULT_PROXY_LIFETIME = (int) TimeUnit.HOURS.toSeconds(12); 
 	public static final int DEFAULT_AC_LIFETIME = (int) TimeUnit.HOURS.toSeconds(12);
 	public static final int DEFAULT_KEY_SIZE = 1024;
 	
 	public static final int DEFAULT_CONNECT_TIMEOUT_IN_SECONDS = 2;
 	
 	private String certFile;
 	private String keyFile;
 	
 	private int keySize = DEFAULT_KEY_SIZE;
 	
 	private String trustAnchorsDir;
 	
 	private String generatedProxyFile;
 
 	private String policyFile;
 
 	private String policyLanguage;
 
	private int pathLenConstraint;
 
 	private ProxyType proxyType = ProxyType.LEGACY;
 
 	private int timeoutInSeconds = DEFAULT_CONNECT_TIMEOUT_IN_SECONDS;
 
 	private boolean verifyAC = true;
 
 	private boolean readPasswordFromStdin = false;
 	
 	private boolean limited = false;
 
 	private List<String> vomsCommands;
 
 	private List<String> targets;
 	
 	private List<String> fqanOrder;
 	
 	private int proxyLifetimeInSeconds = DEFAULT_PROXY_LIFETIME;
 	
 	private int acLifetimeInSeconds = DEFAULT_AC_LIFETIME;
 	
 	private boolean validateUserCredential = false;
 	
 	private boolean noRegen = false;
 	
 	private List<String> vomsesLocations;
 	
 	private boolean enforcingChainIntegrity = true;
 	
 	private String vomsdir;
 	
 	
 	/**
 	 * @return the certFile
 	 */
 	public String getCertFile() {
 		return certFile;
 	}
 
 	/**
 	 * @param certFile
 	 *            the certFile to set
 	 */
 	public void setCertFile(String certFile) {
 		this.certFile = certFile;
 	}
 
 	/**
 	 * @return the keyFile
 	 */
 	public String getKeyFile() {
 		return keyFile;
 	}
 
 	/**
 	 * @param keyFile
 	 *            the keyFile to set
 	 */
 	public void setKeyFile(String keyFile) {
 		this.keyFile = keyFile;
 	}
 
 	/**
 	 * @return the generatedProxyFile
 	 */
 	public String getGeneratedProxyFile() {
 		return generatedProxyFile;
 	}
 
 	/**
 	 * @param generatedProxyFile
 	 *            the generatedProxyFile to set
 	 */
 	public void setGeneratedProxyFile(String generatedProxyFile) {
 		this.generatedProxyFile = generatedProxyFile;
 	}
 
 	
 
 	/**
 	 * @return the policyFile
 	 */
 	public String getPolicyFile() {
 		return policyFile;
 	}
 
 	/**
 	 * @param policyFile
 	 *            the policyFile to set
 	 */
 	public void setPolicyFile(String policyFile) {
 		this.policyFile = policyFile;
 	}
 
 	/**
 	 * @return the policyLanguage
 	 */
 	public String getPolicyLanguage() {
 		return policyLanguage;
 	}
 
 	/**
 	 * @param policyLanguage
 	 *            the policyLanguage to set
 	 */
 	public void setPolicyLanguage(String policyLanguage) {
 		this.policyLanguage = policyLanguage;
 	}
 
 	/**
 	 * @return the pathLenConstraint
 	 */
 	public int getPathLenConstraint() {
 		return pathLenConstraint;
 	}
 
 	/**
 	 * @param pathLenConstraint
 	 *            the pathLenConstraint to set
 	 */
 	public void setPathLenConstraint(int pathLenConstraint) {
 		this.pathLenConstraint = pathLenConstraint;
 	}
 
 	/**
 	 * @return the proxyType
 	 */
 	public ProxyType getProxyType() {
 		return proxyType;
 	}
 
 	/**
 	 * @param proxyType
 	 *            the proxyType to set
 	 */
 	public void setProxyType(ProxyType proxyType) {
 		this.proxyType = proxyType;
 	}
 
 	/**
 	 * @return the timeoutInSeconds
 	 */
 	public int getTimeoutInSeconds() {
 		return timeoutInSeconds;
 	}
 
 	/**
 	 * @param timeoutInSeconds
 	 *            the timeoutInSeconds to set
 	 */
 	public void setTimeoutInSeconds(int timeoutInSeconds) {
 		this.timeoutInSeconds = timeoutInSeconds;
 	}
 
 	/**
 	 * @return the verifyAC
 	 */
 	public boolean verifyAC() {
 		return verifyAC;
 	}
 
 	/**
 	 * @param verifyAC
 	 *            the verifyAC to set
 	 */
 	public void setVerifyAC(boolean verifyAC) {
 		this.verifyAC = verifyAC;
 	}
 
 	/**
 	 * @return the vomsCommands
 	 */
 	public List<String> getVomsCommands() {
 		return vomsCommands;
 	}
 
 	/**
 	 * @param vomsCommands
 	 *            the vomsCommands to set
 	 */
 	public void setVomsCommands(List<String> vomsCommands) {
 		this.vomsCommands = vomsCommands;
 	}
 
 	/**
 	 * @return the readPasswordFromStdin
 	 */
 	public boolean isReadPasswordFromStdin() {
 		return readPasswordFromStdin;
 	}
 
 	/**
 	 * @param readPasswordFromStdin
 	 *            the readPasswordFromStdin to set
 	 */
 	public void setReadPasswordFromStdin(boolean readPasswordFromStdin) {
 		this.readPasswordFromStdin = readPasswordFromStdin;
 	}
 
 	/**
 	 * @return the targets
 	 */
 	public List<String> getTargets() {
 		return targets;
 	}
 
 	/**
 	 * @param targets
 	 *            the targets to set
 	 */
 	public void setTargets(List<String> targets) {
 		this.targets = targets;
 	}
 
 	/**
 	 * @return the limited
 	 */
 	public boolean isLimited() {
 		return limited;
 	}
 
 	/**
 	 * @param limited the limited to set
 	 */
 	public void setLimited(boolean limited) {
 		this.limited = limited;
 	}
 
 	/**
 	 * @return the proxyLifetimeInSeconds
 	 */
 	public int getProxyLifetimeInSeconds() {
 		return proxyLifetimeInSeconds;
 	}
 
 	/**
 	 * @param proxyLifetimeInSeconds the proxyLifetimeInSeconds to set
 	 */
 	public void setProxyLifetimeInSeconds(int proxyLifetimeInSeconds) {
 		this.proxyLifetimeInSeconds = proxyLifetimeInSeconds;
 	}
 
 	/**
 	 * @return the acLifetimeInSeconds
 	 */
 	public int getAcLifetimeInSeconds() {
 		return acLifetimeInSeconds;
 	}
 
 	/**
 	 * @param acLifetimeInSeconds the acLifetimeInSeconds to set
 	 */
 	public void setAcLifetimeInSeconds(int acLifetimeInSeconds) {
 		this.acLifetimeInSeconds = acLifetimeInSeconds;
 	}
 
 	/**
 	 * @return the noRegen
 	 */
 	public boolean isNoRegen() {
 		return noRegen;
 	}
 
 	/**
 	 * @param noRegen the noRegen to set
 	 */
 	public void setNoRegen(boolean noRegen) {
 		this.noRegen = noRegen;
 	}
 
 	public String getTrustAnchorsDir() {
 		return trustAnchorsDir;
 	}
 
 	public void setTrustAnchorsDir(String trustAnchorsDir) {
 		this.trustAnchorsDir = trustAnchorsDir;
 	}
 
 	public boolean validateUserCredential() {
 		return validateUserCredential;
 	}
 
 	public void setValidateUserCredential(boolean validateUserCredential) {
 		this.validateUserCredential = validateUserCredential;
 	}
 
 	/**
 	 * @return the keySize
 	 */
 	public int getKeySize() {
 		return keySize;
 	}
 
 	/**
 	 * @param keySize the keySize to set
 	 */
 	public void setKeySize(int keySize) {
 		this.keySize = keySize;
 	}
 
 	/**
 	 * @return the fqanOrder
 	 */
 	public List<String> getFqanOrder() {
 		return fqanOrder;
 	}
 
 	/**
 	 * @param fqanOrder the fqanOrder to set
 	 */
 	public void setFqanOrder(List<String> fqanOrder) {
 		this.fqanOrder = fqanOrder;
 	}
 
 	public List<String> getVomsesLocations() {
 		return vomsesLocations;
 	}
 
 	public void setVomsesLocations(List<String> vomsesLocations) {
 		this.vomsesLocations = vomsesLocations;
 	}
 
 	public boolean isEnforcingChainIntegrity() {
 		return enforcingChainIntegrity;
 	}
 
 	public void setEnforcingChainIntegrity(boolean enforcingChainIntegrity) {
 		this.enforcingChainIntegrity = enforcingChainIntegrity;
 	}
 
 	public String getVomsdir() {
 		return vomsdir;
 	}
 
 	public void setVomsdir(String vomsdir) {
 		this.vomsdir = vomsdir;
 	}
 	
 }
