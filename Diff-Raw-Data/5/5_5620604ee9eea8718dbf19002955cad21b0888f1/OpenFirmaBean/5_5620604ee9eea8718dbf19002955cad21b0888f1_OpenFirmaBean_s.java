    /* 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published b
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.earroyoron.openfirma;
 
 import java.security.MessageDigest;
 import java.util.Date;
 import javax.ejb.Remote;
 import javax.ejb.Stateless;
 import javax.jws.WebService;
 import javax.security.cert.CertificateException;
 import javax.security.cert.CertificateExpiredException;
 import javax.security.cert.CertificateNotYetValidException;
 import javax.security.cert.X509Certificate;
 
 /**
  *
  * @author earroyoron
  */
 
 //@Local(OpenFirmaBean.class)
 @Stateless
 @Remote (OpenFirma.class)
 @WebService(endpointInterface = "com.earroyoron.openfirma.OpenFirma")
 
 public class OpenFirmaBean implements OpenFirma {
 
     /**
      * Este método devuelve el estado de un certificado
      *
      * @param cert
      */
     @Override
     public CertificateInformation getCertificateStatus(byte[] cert, Date fecha) {
         X509Certificate x509 = null;
         CertificateInformation result = new CertificateInformation();
         try {
             if ((cert == null) || (fecha == null)){
                 throw new IllegalArgumentException("Argumento nulo, se esperaba un certificado X509v3 y una fecha");
             }
 
             x509 = X509Certificate.getInstance(cert);
             result.setCertStatus(CertificateStatus.VALID);
             result.setCertificateFingerprint(getThumbPrint(x509));
             x509.checkValidity(fecha);
         }  catch (CertificateExpiredException ex) {
             result.setCertStatus(CertificateStatus.EXPIRED);
         } catch (CertificateNotYetValidException ex) {
             //TODO mal esta asigancion de estado!!!!!
             result.setCertStatus(CertificateStatus.NOT_YET_VALID);
         } catch (CertificateException ex) {
             throw new IllegalArgumentException("Argumento nulo, se esperaba un certificado X509v3");
         }
             return result;
         
     }
     
     @Override
     public String getInformation() {
         return "openfirma 1.0";
     }
 
    
     
     private static String getThumbPrint(X509Certificate cert) {
         try {
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         byte[] der = cert.getEncoded();
         md.update(der);
         byte[] digest = md.digest();
         return hexify(digest);
         } catch (Exception ex) {
             throw new IllegalArgumentException("A fatal error in crypto engine!");
         } 
     }
 
     private static String hexify (byte bytes[]) {
 
         char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', 
                         '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
 
         StringBuilder bufferStringForFingerprint = new StringBuilder(bytes.length * 3);
 
        for (int i = 0; i < bytes.length; ++i) {
             bufferStringForFingerprint.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
             bufferStringForFingerprint.append(hexDigits[bytes[i] & 0x0f]);
            bufferStringForFingerprint.append(":");
         }
 
         return bufferStringForFingerprint.toString();
     }
 
     
 }
