 package org.cagrid.security.ssl.proxy.trust;
 
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateParsingException;
 import java.security.cert.X509Certificate;
 import java.util.Arrays;
 import java.util.Set;
 
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.TrustManagerFactory;
 import javax.net.ssl.X509TrustManager;
 import javax.security.auth.x500.X500Principal;
 
 import org.bouncycastle.asn1.DERObjectIdentifier;
 
 /*
  * Trying to follow http://www.ietf.org/rfc/rfc3820.txt
  */
 public class ProxyTrustManager implements X509TrustManager {
 
 	public final static String PROXY_CERT_INFO_OID = "1.3.6.1.5.5.7.1.14";
 	public final static String BASIC_CONSTRAINTS_OID = "2.5.29.19";
 	public final static String ISSUER_ALT_NAME_OID = "2.5.29.18";
 	public final static String SUBJECT_ALT_NAME_OID = "2.5.29.17";
 	public final static String KEY_USAGE_OID = "2.5.29.15";
 	public final static String EXTENDED_KEY_USAGE_OID = "2.5.29.37";
 
 	public final static int KEY_USAGE_DIGITAL_SIGNATURE = 0;
 
 	public final static int ASN1_OCTET_STRING_TAG = 4;
 
 	private final static X509Certificate[] EMPTY_ACCEPTED_ISSUERS = new X509Certificate[0];
 
 	private final X509TrustManager[] delegateTrustManagers;
 
 	public ProxyTrustManager(KeyStore trustStore)
 			throws NoSuchAlgorithmException, KeyStoreException {
 
 		TrustManagerFactory trustManagerFactory = TrustManagerFactory
 				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
 		trustManagerFactory.init(trustStore);
 		TrustManager[] plainTrustManagers = trustManagerFactory
 				.getTrustManagers();
 		int nTrustManagers = 0;
 		for (TrustManager tm : plainTrustManagers) {
 			if (tm instanceof X509TrustManager)
 				nTrustManagers++;
 		}
 		delegateTrustManagers = new X509TrustManager[nTrustManagers];
 		int iTrustManager = 0;
 		for (TrustManager tm : plainTrustManagers) {
 			if (tm instanceof X509TrustManager)
 				delegateTrustManagers[iTrustManager++] = (X509TrustManager) tm;
 		}
 	}
 
 	/*
 	 * Validate the proxy certificates until the first end-certificate. If OK,
 	 * delegate the end-certificate validation. The RFC says the end-certificate
 	 * must be validated first, but why?
 	 */
 	@Override
 	public void checkClientTrusted(X509Certificate[] chain, String authType)
 			throws CertificateException {
 
 		int currentProxyPathLength = -1;
 
 		int endCertIX = 0;
 		for (; endCertIX < chain.length; endCertIX++) {
 			X509Certificate cert = chain[endCertIX];
 
 			// Is this a proxy cert?
 			byte[] proxyCertInfoBytes = cert
 					.getExtensionValue(PROXY_CERT_INFO_OID);
 			if (proxyCertInfoBytes == null)
 				break;
 
 			/*
 			 * The extension value is probably wrapped as a primitive octet
 			 * string.
 			 */
 			proxyCertInfoBytes = unwrapASN1Octets(proxyCertInfoBytes);
 			ProxyCertInfo proxyCertInfo = ProxyCertInfo
 					.getInstance(proxyCertInfoBytes);
 			currentProxyPathLength++;
 
 			/*
 			 * 4.1.3.(a)(2) -- Is the certificate valid now?
 			 */
 			cert.checkValidity();
 
 			/*
 			 * Must handle all the critical extensions.
 			 */
 			Set<String> criticalOIDs = cert.getCriticalExtensionOIDs();
 
 			/*
 			 * 3.8. -- The proxyCertInfo must be critical.
 			 */
 			if (!criticalOIDs.remove(PROXY_CERT_INFO_OID)) {
 				throw new CertificateException(
 						"proxyCertInfo extension must be critical");
 			}
 
 			/*
 			 * 3.2. / 3.5. -- Issuer / subject alternative names may not be
 			 * present.
 			 */
 			if ((cert.getIssuerAlternativeNames() != null)
 					|| (cert.getSubjectAlternativeNames() != null)) {
 				throw new CertificateException(
 						"issuerAltName / subjectAltName may not be present");
 			}
 			criticalOIDs.remove(ISSUER_ALT_NAME_OID);
 			criticalOIDs.remove(SUBJECT_ALT_NAME_OID);
 
 			/*
 			 * 3.6. -- If key usage is present, digital signature must be
 			 * asserted.
 			 */
 			boolean[] keyUsage = cert.getKeyUsage();
 			if (keyUsage != null) {
 				if ((keyUsage.length <= KEY_USAGE_DIGITAL_SIGNATURE)
 						|| !keyUsage[KEY_USAGE_DIGITAL_SIGNATURE]) {
 					throw new CertificateException(
 							"keyUsage must assert digitalSignature");
 				}
 			}
 			criticalOIDs.remove(KEY_USAGE_OID);
 
 			/*
 			 * 3.7. -- Basic constraints may not assert CA.
 			 */
 			int basicConstraints = cert.getBasicConstraints();
 			if (basicConstraints != -1) {
 				throw new CertificateException(
 						"basicConstraints may not assert CA");
 			}
 			criticalOIDs.remove(BASIC_CONSTRAINTS_OID);
 
 			/*
 			 * 3.8.1. -- The current path length may not exceed the signing
 			 * constraint.
 			 */
 			if (currentProxyPathLength > proxyCertInfo.getPathLenConstraint()) {
 				throw new CertificateException("pCPathLenConstraint exceeded");
 			}
 
 			/*
 			 * 3.8.2 -- Check policy vs. language.
 			 */
 			ProxyPolicy proxyPolicy = proxyCertInfo.getProxyPolicy();
 			DERObjectIdentifier proxyLanguage = proxyPolicy.getPolicyLanguage();
 			if (ProxyPolicy.IMPERSONATION.equals(proxyLanguage)
 					|| ProxyPolicy.INDEPENDENT.equals(proxyLanguage)) {
 				if (proxyPolicy.getPolicy() != null) {
 					throw new CertificateException(
 							"proxyLanguage does not allow policy");
 				}
 			}
 
 			/*
 			 * Not checking extended key usage.
 			 */
 			criticalOIDs.remove(EXTENDED_KEY_USAGE_OID);
 
 			/*
 			 * Any critical extensions left?
 			 */
 			if (!criticalOIDs.isEmpty()) {
 				throw new CertificateException(
 						"Unhandled critical extensions: " + criticalOIDs);
 			}
 
 			/*
 			 * 4.1.3.(a)(4) -- Subject must be issuer plus a CN.
 			 */
 			X500Principal issuerPrincipal = cert.getIssuerX500Principal();
 			Set<Attribute> issuerAttributes = getPrincipalAttributes(
 					issuerPrincipal, "issuer");
 
 			X500Principal subjectPrincipal = cert.getSubjectX500Principal();
 			Set<Attribute> subjectAttributes = getPrincipalAttributes(
 					subjectPrincipal, "subject");
 
 			boolean goodSubject = subjectAttributes
 					.containsAll(issuerAttributes);
 			if (goodSubject) {
 				goodSubject = false;
 				subjectAttributes.removeAll(issuerAttributes);
 				if (subjectAttributes.size() == 1) {
 					Attribute cnAtt = subjectAttributes.iterator().next();
 					if ("CN".equals(cnAtt.getID()))
 						goodSubject = true;
 				}
 			}
 			if (!goodSubject) {
 				throw new CertificateException("subject must be issuer plus CN");
 			}
 
 			/*
 			 * There must be an issuer certificate next in the chain.
 			 */
 			int issuerCertIX = endCertIX + 1;
 			if (issuerCertIX >= chain.length) {
 				throw new CertificateException(
 						"no issuing certificate for proxy");
 			}
 			X509Certificate issuerCert = chain[issuerCertIX];
 
 			/*
 			 * 4.1.3.(a)(3) -- Issuer must match issuing certificate subject.
 			 */
 			X500Principal issuerSubjectPrincipal = issuerCert
 					.getSubjectX500Principal();
 			Set<Attribute> issuerSubjectAttributes = getPrincipalAttributes(
 					issuerSubjectPrincipal, "issuer subject");
 			if (!issuerAttributes.equals(issuerSubjectAttributes)) {
 				throw new CertificateException(
 						"issuer must match issuer subject");
 			}
 
 			/*
 			 * Finally, verify signature.
 			 */
 			try {
 				cert.verify(issuerCert.getPublicKey());
 			} catch (Exception e) {
 				throw new CertificateException("Invalid signature", e);
 			}
 		}
 
 		X509Certificate[] endChain = new X509Certificate[chain.length
 				- endCertIX];
 		for (int i = 0, j = endCertIX; j < chain.length; j++, i++) {
 			endChain[i] = chain[j];
 		}
 		for (X509TrustManager tm : delegateTrustManagers) {
 			tm.checkClientTrusted(endChain, authType);
 		}
 	}
 
 	@Override
 	public void checkServerTrusted(X509Certificate[] chain, String authType)
 			throws CertificateException {
 		// No proxy checking here, really shouldn't be used in this case.
 		for (X509TrustManager tm : delegateTrustManagers) {
 			tm.checkServerTrusted(chain, authType);
 		}
 	}
 
 	@Override
 	public X509Certificate[] getAcceptedIssuers() {
 		if (delegateTrustManagers.length > 0)
 			return delegateTrustManagers[0].getAcceptedIssuers();
 		return EMPTY_ACCEPTED_ISSUERS;
 	}
 
 	private Set<Attribute> getPrincipalAttributes(X500Principal principal,
 			String type) throws CertificateException {
 		if (principal == null) {
 			throw new CertificateException("no " + type + " principal");
 		}
 		Set<Attribute> principalAttributes = null;
 		try {
 			principalAttributes = DNDissector.dissect(principal);
 		} catch (NamingException ne) {
 			throw new CertificateParsingException("Could not dissect " + type
 					+ " principal", ne);
 		}
 		return principalAttributes;
 	}
 
 	private byte[] unwrapASN1Octets(byte[] octets) {
 		if (octets[0] == ASN1_OCTET_STRING_TAG) {
 			int offset = 2;
 			int length1 = octets[1];
			if (length1 < 0)
				offset = 3;
 			octets = Arrays.copyOfRange(octets, offset, octets.length);
 		}
 		return octets;
 	}
 }
