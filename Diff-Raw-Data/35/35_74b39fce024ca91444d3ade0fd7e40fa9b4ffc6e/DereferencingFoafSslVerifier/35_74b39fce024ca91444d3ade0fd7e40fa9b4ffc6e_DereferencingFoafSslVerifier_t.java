 /*
 New BSD license: http://opensource.org/licenses/bsd-license.php
 
 Copyright (c) 2008-2009 Sun Microsystems, Inc.
 901 San Antonio Road, Palo Alto, CA 94303 USA. 
 All rights reserved.
 
 
 Redistribution and use in source and binary forms, with or without 
 modification, are permitted provided that the following conditions are met:
 
 - Redistributions of source code must retain the above copyright notice, 
 this list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice, 
 this list of conditions and the following disclaimer in the documentation 
 and/or other materials provided with the distribution.
 - Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 may be used to endorse or promote products derived from this software 
 without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 POSSIBILITY OF SUCH DAMAGE.
  */
 package net.java.dev.sommer.foafssl.verifier;
 
 import java.io.InputStream;
 import java.net.URLConnection;
 import java.security.PublicKey;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateParsingException;
 import java.security.cert.X509Certificate;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.HttpURLConnection;
 import java.net.URISyntaxException;
 import java.net.URI;
 import java.net.URL;
 import java.security.interfaces.DSAPublicKey;
 import java.security.interfaces.RSAPublicKey;
 import java.util.Date;
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import javax.net.ssl.HttpsURLConnection;
 
 import net.java.dev.sommer.foafssl.principals.DereferencedFoafSslPrincipal;
 import net.java.dev.sommer.foafssl.principals.FoafSslPrincipal;
 
 import net.java.dev.sommer.foafssl.util.SafeInputStream;
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.*;
 import org.openrdf.query.Binding;
 import org.openrdf.query.BindingSet;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.TupleQuery;
 import org.openrdf.query.TupleQueryResult;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.turtle.TurtleWriter;
 import org.openrdf.sail.memory.MemoryStore;
 
 /**
  * This class verifies FOAF+SSL certificates by dereferencing the FOAF file at
  * the given Web ID URI.
  *
  * @author Henry Story.
  * @author Bruno Harbulot.
  */
 public class DereferencingFoafSslVerifier implements FoafSslVerifier {
     final static String cert = "http://www.w3.org/ns/auth/cert#";
     final static String xsd = "http://www.w3.org/2001/XMLSchema#";
 
     static transient Logger log = Logger.getLogger(DereferencingFoafSslVerifier.class.getName());
     private static final int MAX_LENGTH = 256 * 1024; // 1/4 MB max length of foaf files read.
 
     /**
      * Verifies an X.509 certificate built for FOAF+SSL and returns a Collection
      * of verified FoafSslPrincipals. The verification is done by getting the
      * FOAF file at the Web ID URI.
      *
      * @param clientCert an X.509 cerificate, expected to contain a FOAF+SSL Web ID in
      *                   the subject alternative name extension.
      * @return a collection of verified Principals.
      * @throws org.openrdf.OpenRDFException
      * @throws java.io.IOException
      */
     // todo: do not throw OpenRDFExceptions. I think that creates unnecessary
     // dependencies on this module
     public Collection<? extends FoafSslPrincipal> verifyFoafSslCertificate(
             X509Certificate clientCert) throws OpenRDFException, IOException {
         List<DereferencedFoafSslPrincipal> verifiedUris = new ArrayList<DereferencedFoafSslPrincipal>();
         List<URI> candidateUris = getAlternativeURIName(clientCert);
 
         for (URI candidateUri : candidateUris) {
             DereferencedFoafSslPrincipal principal = verifyByDereferencing(candidateUri, clientCert
                     .getPublicKey(), clientCert.getNotBefore(), clientCert.getNotAfter());
             if (principal != null) {
                 verifiedUris.add(principal);
             }
         }
         return verifiedUris;
     }
 
     /**
      * Verifies a claimed Web ID and its public key against the public key
      * available by dereferencing this Web ID.
      *
      * @param claimedIdUri  claimed Web ID.
      * @param certPublicKey public key provided in the X.509 certificate.
      * @param notBeforeDate date before which the certificate is not valid (although this
      *                      may not be null in an X.509 certificate, this may be null when
      *                      using this method).
      * @param notAfterDate  date after which the certificate is not valid (although this
      *                      may not be null in an X.509 certificate, this may be null when
      *                      using this method).
      * @return a DereferencedFoafSslPrincipal built from the claimed Web ID in
      *         case of success, null otherwise.
      * @throws OpenRDFException
      * @throws IOException
      */
     public DereferencedFoafSslPrincipal verifyByDereferencing(URI claimedIdUri,
                                                               PublicKey certPublicKey, Date notBeforeDate, Date notAfterDate)
             throws OpenRDFException, IOException {
         return verifyByDereferencing(claimedIdUri, certPublicKey, notBeforeDate, notAfterDate,
                 new Date());
     }
 
     /**
      * Verifies a claimed Web ID and its public key against the public key
      * available by dereferencing this Web ID.
      *
      * @param claimedIdUri  claimed Web ID.
      * @param certPublicKey public key provided in the X.509 certificate.
      * @param notBeforeDate date before which the certificate is not valid (although this
      *                      may not be null in an X.509 certificate, this may be null when
      *                      using this method).
      * @param notAfterDate  date after which the certificate is not valid (although this
      *                      may not be null in an X.509 certificate, this may be null when
      *                      using this method).
      * @param currentDate
      * @return a DereferencedFoafSslPrincipal built from the claimed Web ID in
      *         case of success, null otherwise.
      * @throws OpenRDFException
      * @throws IOException
      */
     public DereferencedFoafSslPrincipal verifyByDereferencing(URI claimedIdUri,
                                                               PublicKey certPublicKey, Date notBeforeDate, Date notAfterDate, Date currentDate)
             throws OpenRDFException, IOException {
         URL foafname = claimedIdUri.toURL();
         URLConnection conn = foafname.openConnection();
         if (conn instanceof HttpURLConnection) {
             HttpURLConnection hconn = (HttpURLConnection) conn;
             // set by default to True, but might as well override instances
             // here, in case a default is set somewhere else in the code.
             hconn.setInstanceFollowRedirects(true);
         }
 
         if (currentDate != null) {
             if ((notBeforeDate != null) && (currentDate.before(notBeforeDate))) {
                 return null;
             }
             if ((notAfterDate != null) && (currentDate.after(notAfterDate))) {
                 return null;
             }
         }
 
         conn.addRequestProperty("Accept:",
                 "application/rdf+xml; q=1.0, text/html; q=0.7; application/xhtml+xml;q=0.8");
         conn.connect();
 
         InputStream is = conn.getInputStream();
         try {
             boolean dereferencedSecurely = false;
             Certificate[] foafServerCertificates = null;
             if (conn instanceof HttpsURLConnection) {
                 dereferencedSecurely = true;
                 foafServerCertificates = ((HttpsURLConnection) conn).getServerCertificates();
             }
             String mimeType = mimeType(conn.getContentType());
             return verifyByDereferencing(claimedIdUri, certPublicKey, conn.getURL(), is, mimeType,
                     dereferencedSecurely, foafServerCertificates);
         } finally {
             is.close();
         }
     }
 
     /**
      * Verifies a claimed Web ID and its public key against the public key
      * available by dereferencing this Web ID.
      *
      * @param claimedIdUri       claimed Web ID.
      * @param certPublicKey      public key provided in the X.509 certificate.
      * @param actualUrl          Actual URL of the FOAF document (perhaps different to URI if
      *                           redirections).
      * @param foafDocInputStream FOAF document input stream.
      * @param foafMediaType      Media type of the FOAF document representation in the input
      *                           stream.
      * @return a DereferencedFoafSslPrincipal built from the claimed Web ID in
      *         case of success, null otherwise.
      * @throws OpenRDFException
      * @throws IOException
      */
     public DereferencedFoafSslPrincipal verifyByDereferencing(URI claimedIdUri,
                                                               PublicKey certPublicKey, URL actualUrl, InputStream foafDocInputStream,
                                                               String foafMediaType) throws OpenRDFException, IOException {
         return verifyByDereferencing(claimedIdUri, certPublicKey, actualUrl, foafDocInputStream,
                 foafMediaType, false, null);
     }
 
     /**
      * Verifies a claimed Web ID and its public key against the public key
      * available by dereferencing this Web ID.
      *
      * @param claimedIdUri           claimed Web ID.
      * @param certPublicKey          public key provided in the X.509 certificate.
      * @param actualUrl              Actual URL of the FOAF document (perhaps different to URI if
      *                               redirections).
      * @param foafDocInputStream     FOAF document input stream.
      * @param foafMediaType          Media type of the FOAF document representation in the input
      *                               stream.
      * @param dereferencedSecurely   whether the FOAF document was dereferenced securely.
      * @param foafServerCertificates certificate chain of the server hosting the FOAF document, may
      *                               be null.
      * @return a DereferencedFoafSslPrincipal built from the claimed Web ID in
      *         case of success, null otherwise.
      * @throws OpenRDFException
      * @throws IOException
      */
     public DereferencedFoafSslPrincipal verifyByDereferencing(URI claimedIdUri,
                                                               PublicKey certPublicKey, URL actualUrl, InputStream foafDocInputStream,
                                                               String foafMediaType, boolean dereferencedSecurely, Certificate[] foafServerCertificates)
             throws OpenRDFException, IOException {
         RDFFormat rdfFormat = RDFFormat.forMIMEType(foafMediaType);
 
         URL base = new URL(actualUrl.getProtocol(), actualUrl.getHost(), actualUrl.getPort(),
                 actualUrl.getFile()); // all of this needs
 
         MemoryStore mem = new MemoryStore();
         mem.initialize();
         SailRepository sail = new SailRepository(mem);
         RepositoryConnection rep = sail.getConnection();
         ValueFactory vf = sail.getValueFactory();
 
         // to be better
         org.openrdf.model.URI foafdocUri = vf.createURI(base.toString());
         rep.add(new SafeInputStream(foafDocInputStream, MAX_LENGTH), actualUrl.toString(), rdfFormat, foafdocUri);
        rep.export(new TurtleWriter(System.out));
         if (certPublicKey instanceof RSAPublicKey) {
             RSAPublicKey certRsakey = (RSAPublicKey) certPublicKey;
             TupleQuery query = rep.prepareTupleQuery(QueryLanguage.SPARQL,
                     "PREFIX cert: <http://www.w3.org/ns/auth/cert#>"
                             + "PREFIX rsa: <http://www.w3.org/ns/auth/rsa#>"
                             + "SELECT ?m ?e ?mod ?exp "
                             + "WHERE { "
                             + "   ?sig cert:identity ?person ."
                             + "   ?sig a rsa:RSAPublicKey;"
                             + "        rsa:modulus ?m ;"
                             + "        rsa:public_exponent ?e ."
                             + "   OPTIONAL { ?m cert:hex ?mod . }"
                             + "   OPTIONAL { ?e cert:decimal ?exp . }"
                             + "}");
             // TODO: allow optional different ways of encoding the
             // modulus and exponent integers
             // this would just require passing the relations and the
             // value to a function.
             query.setBinding("person", vf.createURI(claimedIdUri.toString()));
             TupleQueryResult answer = query.evaluate();
             while (answer.hasNext()) {
                 BindingSet bindingSet = answer.next();
 
                 //1. find the exponent
                 BigInteger exp = toInteger(bindingSet.getBinding("e"), cert + "decimal", bindingSet.getBinding("exp"));
                 if (exp == null || !exp.equals(certRsakey.getPublicExponent())) {
                     continue;
                 }
 
                 //2. Find the modulus
                 BigInteger mod = toInteger(bindingSet.getBinding("m"), cert + "hex", bindingSet.getBinding("mod"));
                 if (mod == null || !mod.equals(certRsakey.getModulus())) {
                     continue;
                 }
 
                 // success!
                 return new DereferencedFoafSslPrincipal(claimedIdUri, dereferencedSecurely,
                         foafServerCertificates);
             }
         } else if (certPublicKey instanceof DSAPublicKey) {
         } else {
             // what else ?
         }
         return null;
     }
 
     /**
      * Transform an RDF representation of a number into a BigInteger
      * <p/>
      * Passes a statement as two bindings and the relation between them.
      * The subject is the number.
      * If num is already a literal number, that is returned, otherwise if
      * enough information from the relation to optstr exists, that is used.
      *
      * @param num    the number node
      * @param optRel name of the relation to the literal
      * @param optstr the literal representation if it exists
      * @return the big integer that num represents, or null if undetermined
      */
     static BigInteger toInteger(Binding num, String optRel, Binding optstr) {
         if (null == num) return null;
         Value numVal = num.getValue();
         if (numVal instanceof Literal) {  //we do in fact have "ddd"^^type
             Literal ln = (Literal) numVal;
             String type = ln.getDatatype().toString();
             return toInteger_helper(ln.getLabel(), type);
         } else if (numVal instanceof Resource) { //we had _:n type "ddd" .
             Value strVal = optstr.getValue();
             if (strVal != null && strVal instanceof Literal) {
                 Literal ls = (Literal) strVal;
                 return toInteger_helper(ls.getLabel(), optRel);
             }
         }
         return null;
     }
 
     /**
      * This transforms a literal into a number if possible
      * ie, it returns the BigInteger of "ddd"^^type
      *
      * @param num  the string representation of the number
      * @param type the type of the string representation
      * @return the number
      */
     private static BigInteger toInteger_helper(String num, String type) {
         if (type.equals(cert + "decimal") || type.equals(cert + "int") ||
                 type.equals(xsd + "integer") || type.equals(xsd + "int") ||
                 type.equals(xsd + "nonNegativeInteger")) { //cert:decimal is deprecated
             return new BigInteger(num.trim(), 10);
         } else if (type.equals(cert + "hex")) {
             String strval = cleanHex(num);
             return new BigInteger(strval, 16);
         } else {
             //it could be some other encoding - one should really write a special literal transformation class
         }
         return null;
     }
 
     /**
      * Extracts the URIs in the subject alternative name extension of an X.509
      * certificate (perhaps others such as email addresses could also be
      * useful).
      *
      * @param cert X.509 certificate from which to extract the URIs.
      * @return list of java.net.URIs built from the URIs in the subjectAltName
      *         extension.
      */
     public static List<URI> getAlternativeURIName
             (X509Certificate
                     cert) {
         ArrayList<URI> answers = new ArrayList<URI>();
         try {
             if (cert == null) {
                 return answers;
             }
             Collection<List<?>> names = cert.getSubjectAlternativeNames();
             if (names == null) {
                 return answers;
             }
             for (Iterator<List<?>> it = names.iterator(); it.hasNext();) {
                 List<?> altNameList = it.next();
                 Integer id = (Integer) altNameList.get(0);
                 if (id == 6) { // see X509 spec, section 8.3.2.1 these are the
                     // URIs!
                     Object uristr = altNameList.get(1);
                     if (uristr instanceof String) {
                         try {
                             URI foafid = new URI((String) uristr);
                             answers.add(foafid);
                         } catch (URISyntaxException e) {
                             e.printStackTrace();
                         }
                     } else {
                         // if we are to return other things, such as email and
                         // so we would need a more
                         // complex structure in the return, perhaps even a class
                         // for the X509 cert with
                         // more helpful return methods
                     }
                 }
             }
         } catch (CertificateParsingException e) {
             // TODO: decide what exception to throw
             log.log(Level.WARNING,
                     "Unable to parse certificate for extracting the subjectAltNames.", e);
         }
         return answers;
     }
 
     static final private char[] hexchars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
             'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f'};
 
     static {
         Arrays.sort(hexchars);
     }
 
     /**
      * This takes any string and returns in order only those characters that are
      * part of a hex string
      *
      * @param strval any string
      * @return a pure hex string
      */
 
     private static String cleanHex(String strval) {
         StringBuffer cleanval = new StringBuffer();
         for (char c : strval.toCharArray()) {
             if (Arrays.binarySearch(hexchars, c) >= 0) {
                 cleanval.append(c);
             }
         }
         return cleanval.toString();
     }
 
     private String mimeType(String contentType) {
         int i = contentType.indexOf(';');
         if (i > 0) {
             contentType = contentType.substring(0, i);
         }
         return contentType.trim();
     }
 }
