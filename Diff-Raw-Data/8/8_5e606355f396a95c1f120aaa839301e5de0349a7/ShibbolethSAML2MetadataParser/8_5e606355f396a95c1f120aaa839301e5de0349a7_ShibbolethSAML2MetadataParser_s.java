 /* CVS Header
    $
    $
 */
 
 package org.guanxi.common.job;
 
 import org.apache.log4j.Logger;
 import org.apache.xml.security.signature.XMLSignature;
 import org.apache.xml.security.signature.XMLSignatureException;
 import org.apache.xml.security.keys.KeyInfo;
 import org.apache.xml.security.exceptions.XMLSecurityException;
 import org.apache.xml.security.Init;
 import org.apache.xmlbeans.XmlException;
 import org.guanxi.xal.saml_2_0.metadata.EntitiesDescriptorDocument;
 import org.guanxi.xal.saml_2_0.metadata.EntityDescriptorType;
 import org.guanxi.xal.saml_2_0.metadata.ExtensionsType;
 import org.guanxi.xal.w3.xmldsig.SignatureType;
 import org.guanxi.xal.w3.xmldsig.KeyInfoType;
 import org.guanxi.xal.w3.xmldsig.X509DataType;
 import org.guanxi.xal.shibboleth_1_0.metadata.KeyAuthorityDocument;
 import org.guanxi.common.Utils;
 import org.guanxi.common.GuanxiException;
 import org.guanxi.common.entity.EntityManager;
 import org.guanxi.common.entity.EntityFarm;
 import org.guanxi.common.trust.TrustUtils;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.security.cert.X509Certificate;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.io.IOException;
 import java.io.ByteArrayInputStream;
 
 public abstract class ShibbolethSAML2MetadataParser {
   /** Our logger */
   protected Logger logger = null;
   protected SAML2MetadataParserConfig config = null;
   protected EntitiesDescriptorDocument doc = null;
   protected EntityDescriptorType[] entityDescriptors = null;
 
   static {
     // Initialise xml-security library
     Init.init();
   }
 
   /**
    * Initialises the SAML2 parsing operation
    */
   public void init() {
     logger = Logger.getLogger(config.getJobClass());
 
     try {
       // Load the metadata from the URL
       doc = Utils.parseSAML2Metadata(config.getMetadataURL());
     }
     catch(GuanxiException ge) {
       logger.error("Error parsing metadata. Loading from cache", ge);
       try {
         // Load the metadata from the cache
         doc = Utils.parseSAML2Metadata("file:///" + config.getMetadataCacheFile());
       }
       catch(GuanxiException gex) {
         logger.error("Could not load metadata from cache : " + config.getMetadataCacheFile(), gex);
       }
     }
   }
 
   /**
    * Loads the SAML2 entities from the metadata and caches them locally
    */
   protected void loadAndCacheEntities() {
     entityDescriptors = doc.getEntitiesDescriptor().getEntityDescriptorArray();
 
     // Cache the metadata locally
     try {
       Utils.writeSAML2MetadataToDisk(doc, config.getMetadataCacheFile());
     }
     catch(GuanxiException ge) {
       logger.error("Could not cache metadata to : " + config.getMetadataCacheFile(), ge);
     }
   }
 
   /**
    * Verifies the fingerprint of the signing certificate in the metadata
    * with a known certificate fingerprint.
    *
    * @return true if the fingerprints match, otherwise false
    */
   protected boolean verifyMetadataFingerprint() {
     X509Certificate metadataCert = getX509FromMetadataSignature();
 
     if (metadataCert != null) {
       try {
         return TrustUtils.checkCertfingerprints(metadataCert, TrustUtils.pem2x509(config.getPemLocation()));
       }
       catch(GuanxiException ge) {
         logger.error("error checking metadata cert fingerprint", ge);
       }
     }
     else {
       logger.error("no X509 in metadata signature");
     }
 
     return false;
   }
 
   /**
    * Verifies the signature on the SAML2 metadata
    *
    * @return true if the signature verifies, otherwise false
    */
   protected boolean verifyMetadataSignature() {
     SignatureType sig = getSignatureFromMetadata();
     X509Certificate metadataCert = getX509FromMetadataSignature();
 
     if (metadataCert != null) {
       try {
         XMLSignature signature = new XMLSignature((Element)sig.getDomNode(),"");
         return signature.checkSignatureValue(metadataCert);
       }
       catch(XMLSignatureException xse) {
         logger.error("Failed to get signature of metadata", xse);
       }
       catch(XMLSecurityException xse) {
         logger.error("Problem with the signature of metadata", xse);
       }
     }
     else {
       logger.error("no X509 in metadata signature");
     }
 
     return false;
   }
 
   /**
    * Extracts the signature block from the metadata
    *
    * @return Signature block from the metadata
    */
   protected SignatureType getSignatureFromMetadata() {
     return doc.getEntitiesDescriptor().getSignature();
   }
 
   /**
    * Extracts the X509Certificate wrapping the public key to be used
    * to verify the signature on SAML2 metadata.
    *
    * @return X509Certificate of the signing public key
    */
   protected X509Certificate getX509FromMetadataSignature() {
     SignatureType sig = getSignatureFromMetadata();
     if (sig == null) {
       logger.error("Metadata is not signed");
     }
     else {
       try {
         XMLSignature signature = new XMLSignature((Element)sig.getDomNode(),"");
         KeyInfo keyInfo = signature.getKeyInfo();
         if (keyInfo != null) {
           if(keyInfo.containsX509Data()) {
             return signature.getKeyInfo().getX509Certificate();
           }
           else {
             logger.error("no x509 data in metadata signature");
           }
         }
         else {
           logger.error("no key info in metadata signature");
         }
       }
       catch(XMLSignatureException xse) {
         logger.error("Failed to get signature of metadata", xse);
       }
       catch(XMLSecurityException xse) {
         logger.error("Problem with the signature of metadata", xse);
       }
     }
 
     return null;
   }
 
   /**
    * Loads all the shibmeta:KeyAuthority nodes from the SAML2 metadata
    *
    * @param manager EntityManager instance for this metadata
    * @return true if the CA lists was loaded, otherwise false
    */
   protected boolean loadCAListFromMetadata(EntityManager manager) {
     try {
       CertificateFactory certFactory = CertificateFactory.getInstance("x.509");
       ExtensionsType extensions = doc.getEntitiesDescriptor().getExtensions();
 
       /* Find the shibmeta:KeyAuthority node. This lists all the root CAs
        * that we trust.
        */
       Node keyAuthorityNode = null;
       NodeList nodes = extensions.getDomNode().getChildNodes();
       for (int c=0; c < nodes.getLength(); c++) {
         if (nodes.item(c).getLocalName() != null) {
           if (nodes.item(c).getLocalName().equals("KeyAuthority")) {
             keyAuthorityNode = nodes.item(c);
           }
         }
       }
 
       // Load all the root CAs into the trust engine
       if (keyAuthorityNode != null) {
         KeyAuthorityDocument keyAuthDoc = KeyAuthorityDocument.Factory.parse(keyAuthorityNode);
         KeyInfoType[] keyInfos = keyAuthDoc.getKeyAuthority().getKeyInfoArray();
         for (KeyInfoType keyInfo : keyInfos) {
           X509DataType[] x509Datas = keyInfo.getX509DataArray();
           for (X509DataType x509Data : x509Datas) {
             byte[][] x509Certs = x509Data.getX509CertificateArray();
             for (byte[] x509CertBytes : x509Certs) {
               ByteArrayInputStream certByteStream = new ByteArrayInputStream(x509CertBytes);
               manager.getTrustEngine().addCACert((X509Certificate)certFactory.generateCertificate(certByteStream));
               certByteStream.close();
              return true;
             }
           }
         }
       }
       else {
         logger.error("Could not find shibmeta:KeyAuthority in metadata");
       }
     }
     catch(CertificateException ce) {
       logger.error("Could not prepare certificate factory", ce);
     }
     catch(IOException ioe) {
       logger.error("Could not close byte stream", ioe);
     }
     catch(XmlException xe) {
       logger.error("Could not load shibboleth extensions from metadata", xe);
     }
 
    return false;
   }
 
   /**
    * Loads the appropriate EntityManager for the current metadata source and
    * empties it of any previous metadata.
    *
    * @param contextKey The key in the servlet context under which the manager is hiding
    * @return EntityManager for the current metadata source
    */
   protected EntityManager loadEmptyEntityManager(String contextKey) {
     EntityFarm farm = (EntityFarm)config.getServletContext().getAttribute(contextKey);
     EntityManager manager = farm.getEntityManagerForSource(config.getMetadataURL());
     manager.removeMetadata();
     return manager;
   }
 }
