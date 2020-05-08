 package net.link.safeonline.sdk.ws;
 
 import static com.lyndir.lhunath.lib.system.util.ObjectUtils.getOrDefault;
 import static net.link.safeonline.sdk.configuration.SDKConfigHolder.config;
 
 import be.fedict.trust.MemoryCertificateRepository;
 import be.fedict.trust.TrustValidator;
 import com.google.common.base.Supplier;
 import com.lyndir.lhunath.lib.system.logging.Logger;
 import com.lyndir.lhunath.lib.system.util.ObjectUtils;
 import java.security.PrivateKey;
 import java.security.cert.CertPathValidatorException;
 import java.security.cert.X509Certificate;
 import java.util.Collection;
 import javax.security.auth.x500.X500Principal;
 import net.link.util.common.CertificateChain;
 import net.link.util.config.KeyProvider;
 import net.link.util.ws.security.AbstractWSSecurityConfiguration;
 import org.jetbrains.annotations.Nullable;
 import org.joda.time.Duration;
 
 
 /**
  * <h2>{@link SDKWSSecurityConfiguration}<br> <sub>[in short] (TODO).</sub></h2>
  *
  * <p> <i>03 31, 2011</i> </p>
  *
  * @author lhunath
  */
 public class SDKWSSecurityConfiguration extends AbstractWSSecurityConfiguration {
 
     static final Logger logger = Logger.get( SDKWSSecurityConfiguration.class );
 
     private final X500Principal trustedDN;
     private final KeyProvider   keyProvider;
 
     public SDKWSSecurityConfiguration() {
 
         this( null, null );
     }
 
     public SDKWSSecurityConfiguration(@Nullable final X500Principal trustedDN, @Nullable final KeyProvider keyProvider) {
 
         this.trustedDN = trustedDN;
         this.keyProvider = keyProvider;
     }
 
     public boolean isCertificateChainTrusted(final CertificateChain aCertificateChain) {
 
         // Manually check whether the end certificate has the correct DN.
         if (!ObjectUtils.isEqual( aCertificateChain.getIdentityCertificate().getSubjectX500Principal(), getTrustedDN() ))
             return false;
 
         MemoryCertificateRepository certificateRepository = new MemoryCertificateRepository();
         Collection<X509Certificate> trustedCertificates = getKeyProvider().getTrustedCertificates();
         for (X509Certificate trustedCertificate : trustedCertificates)
             certificateRepository.addTrustPoint( trustedCertificate );
 
         try {
             new TrustValidator( certificateRepository ).isTrusted( aCertificateChain.getOrderedCertificateChain() );
             return true;
         }
         catch (CertPathValidatorException e) {
             logger.dbg( e, "Couldn't trust certificate chain.\nChain:\n%s\nTrusted Certificates:\n%s", aCertificateChain,
                     trustedCertificates );
             return false;
         }
     }
 
     public X500Principal getTrustedDN() {
 
         return getOrDefault( trustedDN, config().linkID().app().trustedDN() );
     }
 
     public KeyProvider getKeyProvider() {
 
         return getOrDefault( keyProvider, new Supplier<KeyProvider>() {
             public KeyProvider get() {
 
                 return config().linkID().app().keyProvider();
             }
         } );
     }
 
     public CertificateChain getIdentityCertificateChain() {
 
        return keyProvider.getIdentityCertificateChain();
     }
 
     public PrivateKey getPrivateKey() {
 
        return keyProvider.getIdentityKeyPair().getPrivate();
     }
 
     @Override
     public Duration getMaximumAge() {
 
         return config().proto().maxTimeOffset();
     }
 }
