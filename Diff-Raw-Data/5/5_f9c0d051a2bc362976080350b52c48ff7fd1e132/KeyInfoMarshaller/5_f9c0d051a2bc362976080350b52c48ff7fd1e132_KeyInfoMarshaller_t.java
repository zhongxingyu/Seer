 /*
  * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.opensaml.xml.signature;
 
 import java.security.GeneralSecurityException;
 import java.security.cert.X509CRL;
 import java.security.cert.X509Certificate;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.apache.xml.security.Init;
 import org.apache.xml.security.exceptions.XMLSecurityException;
 import org.apache.xml.security.keys.content.KeyName;
 import org.apache.xml.security.keys.content.X509Data;
 import org.opensaml.xml.XMLObject;
 import org.opensaml.xml.io.Marshaller;
 import org.opensaml.xml.io.MarshallingException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * A marshaller for {@link org.opensaml.xml.signature.impl.KeyInfoImpl} objects. This class, along with it's
  * respective builder and unmarshaller use the Apache XMLSec 1.3 APIs to perform signing and verification.
  */
 public class KeyInfoMarshaller implements Marshaller {
 
     /** Logger */
     private static final Logger LOG = Logger.getLogger(KeyInfoMarshaller.class);
 
     /**
      * Constructor
      */
     public KeyInfoMarshaller() {
         if (!Init.isInitialized()) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Initializing XML security library");
             }
             Init.init();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Element marshall(XMLObject xmlObject) throws MarshallingException {
         try {
             Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
             return marshall(xmlObject, document);
         } catch (ParserConfigurationException e) {
             throw new MarshallingException("Unable to create Document to place marshalled elements in", e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Element marshall(XMLObject xmlObject, Element parentElement) throws MarshallingException {
        Element marshalledElement = marshall(xmlObject, parentElement.getOwnerDocument());
        parentElement.appendChild(marshalledElement);
        xmlObject.setDOM(marshalledElement);
        return marshalledElement;
     }
 
     /**
      * {@inheritDoc}
      */
     public Element marshall(XMLObject xmlObject, Document document) throws MarshallingException {
         KeyInfo keyInfoObj = (KeyInfo) xmlObject;
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Starting to marshall " + xmlObject.getElementQName());
         }
 
         org.apache.xml.security.keys.KeyInfo keyInfoElem = new org.apache.xml.security.keys.KeyInfo(document);
 
         for (String keyName : keyInfoObj.getKeyNames()) {
             keyInfoElem.add(new KeyName(document, keyName));
         }
 
         if(keyInfoObj.getPublicKey() != null){
             keyInfoElem.add(keyInfoObj.getPublicKey());
         }
 
         // Add X509 Certificates to X509Data
         X509Data x509Data = null;
         for (X509Certificate cert : keyInfoObj.getCertificates()) {
             try {
                 x509Data = new X509Data(document);
                 x509Data.addCertificate(cert);
             } catch (XMLSecurityException e) {
                 throw new MarshallingException("Error adding X509 certificate " + cert.getSubjectDN() + " to KeyInfo",
                         e);
             }
         }
         // Add X509 CRLs to X509Data
         for (X509CRL crl : keyInfoObj.getCRLs()) {
             try {
                 if (x509Data == null) {
                     x509Data = new X509Data(document);
                 }
                 x509Data.addCRL(crl.getEncoded());
             } catch (GeneralSecurityException e) {
                 throw new MarshallingException("Error adding X509 CRL to KeyInfo", e);
             }
         }
         
         // If we do have some X509Data, add it
         if (x509Data != null) {
             keyInfoElem.add(x509Data);
         }
         
         return keyInfoElem.getElement();
     }
 }
