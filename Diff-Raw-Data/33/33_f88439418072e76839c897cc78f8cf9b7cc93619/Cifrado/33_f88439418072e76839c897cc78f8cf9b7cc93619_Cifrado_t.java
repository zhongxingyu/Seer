 package Negocios.Cifrado;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.*;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.PKCS8EncodedKeySpec;
 import java.util.Date;
 import org.apache.commons.codec.DecoderException;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.binary.Hex;
 import org.bouncycastle.asn1.ASN1InputStream;
 import org.bouncycastle.asn1.ASN1OctetString;
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
 import org.bouncycastle.asn1.pkcs.PBES2Parameters;
 import org.bouncycastle.asn1.pkcs.PBKDF2Params;
 import org.bouncycastle.crypto.BufferedBlockCipher;
 import org.bouncycastle.crypto.CipherParameters;
 import org.bouncycastle.crypto.PBEParametersGenerator;
 import org.bouncycastle.crypto.engines.DESedeEngine;
 import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
 import org.bouncycastle.crypto.modes.CBCBlockCipher;
 import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
 import org.bouncycastle.crypto.params.ParametersWithIV;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 
 /**
  * Clase que se encarga de la funcionalidad del cifrado/descifrado de la FIEL y
  * el CSD durante el proceso de facturación electrónica de ISFE
  * @author Trabajo Terminal 20110020 Implementación del Servicio de Facturación Electrónica acorde a la reforma de enero de 2011
  */
 public class Cifrado {
     static{
         if(Security.getProvider("BC")==null)
             Security.addProvider(new BouncyCastleProvider());
     }
     /**
      * Método encargado de descifrar la llave privada de la FIEL
      * @param LlavePrivadaCifrada obtenida de la FIEL del Usuario
      * @param Password obtenida para el descifrado de la llave
      * @return LLave Privada de la FIEL descifrada
      * @throws IOException 
      * @throws SecurityException 
      */
     private static byte[] descifrarLlavePrivada(byte[] LlavePrivadaCifrada,String Password) throws IOException,SecurityException{
        if(Security.getProvider("BC")==null)
            Security.addProvider(new BouncyCastleProvider());
         ASN1InputStream ais=new ASN1InputStream(new ByteArrayInputStream(LlavePrivadaCifrada));
         ASN1Sequence as=(ASN1Sequence)ais.readObject();
         EncryptedPrivateKeyInfo epki=new EncryptedPrivateKeyInfo(as);
         ais.close();
         BufferedBlockCipher bbc=new PaddedBufferedBlockCipher(new CBCBlockCipher(new DESedeEngine()));
         bbc.init(false, getParametrosCifrado(epki,Password.toCharArray()));
         byte[] datosCifrados=epki.getEncryptedData();
         int tdc=datosCifrados.length;
         byte[] datosDescifrados=new byte[bbc.getOutputSize(tdc)];
         int tam=bbc.processBytes(datosCifrados, 0, tdc,datosDescifrados,0);
         try{
             bbc.doFinal(datosDescifrados, tam);
         }catch(Exception ex){
             System.err.println("ERROR: No se realizo el descifrado de la llave privada");
         }
         return datosDescifrados;
     }
     /**
      * Método encargado de obtener los parametros de cifrado de la llave privada
      * de la FIEL
      * @param epki información de la llave privada
      * @param Password de la llave privada
      * @return Parametros de cifrado necesarias para descifrar la llave privada
      */
     private static CipherParameters getParametrosCifrado(EncryptedPrivateKeyInfo epki, char[] Password){
        if(Security.getProvider("BC")==null)
            Security.addProvider(new BouncyCastleProvider());
         ASN1Sequence as=(ASN1Sequence)epki.getEncryptionAlgorithm().getParameters();
         PBES2Parameters pbe2p=new PBES2Parameters(as);
         PBKDF2Params pbkdf2p=PBKDF2Params.getInstance(pbe2p.getKeyDerivationFunc().getParameters());
         byte[] salt=pbkdf2p.getSalt();
         int contador=pbkdf2p.getIterationCount().intValue();
         byte[] pass=PBEParametersGenerator.PKCS5PasswordToBytes(Password);
         PBEParametersGenerator pbepg=new PKCS5S2ParametersGenerator();
         pbepg.init(pass, salt, contador);
         byte[] iv=((ASN1OctetString)pbe2p.getEncryptionScheme().getObject()).getOctets();
         ParametersWithIV pwiv=new ParametersWithIV(pbepg.generateDerivedMacParameters(192),iv);
         return pwiv;
     }
     /**
      * Método que obtiene la llave privada de la FIEL del usuario
      * @param LlavePrivadaCifrada de la FIEL
      * @param Password de la llave privada
      * @return Llave privada descifrada de la FIEL
      * @throws IOException
      * @throws SecurityException 
      */
     public static PrivateKey getLlavePrivada(byte[] LlavePrivadaCifrada,String Password) throws IOException,SecurityException, NoSuchProviderException{
        if(Security.getProvider("BC")==null)
            Security.addProvider(new BouncyCastleProvider());
         try{
             KeyFactory kf=KeyFactory.getInstance("RSA", "BC");
             PKCS8EncodedKeySpec p8ekp=new PKCS8EncodedKeySpec(descifrarLlavePrivada(LlavePrivadaCifrada,Password));
             PrivateKey pk=kf.generatePrivate(p8ekp);
             return pk;
         }catch(InvalidKeySpecException ex){
             System.err.println("ERROR: No se puede generar una llave RSA con los datos actuales");
             return null;
         }catch(NoSuchAlgorithmException ex){
             System.err.println("ERROR: El proveedor BC no es capaz de soportar el algoritmo RSA");
             return null;
         }catch(NoSuchProviderException ex){
             System.err.println("ERROR: No es posible encontrar el proveedor BC");
             return null;
         }
     }
     /**
      * Método que permite eliminar la llave privada de la FIEL
      * @param LlavePrivada descifrada de la FIEL
      */
     public static void eliminarLlavePrivada(PrivateKey LlavePrivada){
        if(Security.getProvider("BC")==null)
            Security.addProvider(new BouncyCastleProvider());
         byte[] datosLlave=LlavePrivada.getEncoded();
         try{
             SecureRandom sr=SecureRandom.getInstance("RSA");
             for(int i=0;i<4;i++){
                 sr.setSeed(new Date().getTime());
                 sr.nextBytes(datosLlave);
             }
         }catch(NoSuchAlgorithmException ex){
             System.err.println(ex.getMessage());
         }
     }
     /**
      * Método que se encarga de generar la firma del CFDI del usuario
      * @param LlavePrivada descifrada de la FIEL
      * @param datos con los que se generará la firma
      * @return firma o sello del CFDI que se genere
      * @throws SecurityException 
      */
     public static String firmar(PrivateKey LlavePrivada,byte[] datos) throws SecurityException{
        if(Security.getProvider("BC")==null)
            Security.addProvider(new BouncyCastleProvider());
         try{
             Signature s=Signature.getInstance("SHA1withRSA", "BC");
             s.initSign(LlavePrivada);
             s.update(datos);
             return codificarBase64(s.sign());
         }catch(SignatureException ex){
             System.err.println("ERROR: No se puede crear la firma");
             return null;
         }catch(InvalidKeyException ex){
             System.err.println("ERROR: La llave no es válida");
             return null;
         }catch(NoSuchAlgorithmException ex){
             System.err.println("ERROR: El proveedor BC no es capaz de soportar SHA1withRSA");
             return null;
         }catch(NoSuchProviderException ex){
             System.err.println("ERROR: No se encuentra ningún proveedor");
             return null;
         }
     }
     /**
      * Método que se encargar de generar el digesto con los datos que se 
      * proporcionan mediante SHA-1
      * @param datos con los que se generará el digesto
      * @return digesto de los datos recibidos
      */
     public static String digerir(byte[] datos){
        if(Security.getProvider("BC")==null)
            Security.addProvider(new BouncyCastleProvider());
         try{
             MessageDigest digesto=MessageDigest.getInstance("SHA1","BC");
             digesto.update(datos);
             return codificarHex(digesto.digest());
         }catch(NoSuchAlgorithmException ex){
             System.err.println("ERROR: ");
             return null;
         }catch(NoSuchProviderException ex){
             System.err.println("ERROR: ");
             return null;
         }
     }
     /**
      * Método encargado de codificar a Base64 una cadena de caracteres
      * @param datos a codificar
      * @return datos codificados a Base64
      */
     public static String codificarBase64(String datos){
         return codificarBase64(datos.getBytes());
     }
     /**
      * Método encargado de codificar a Base64 un arreglo de bytes
      * @param datos a codificar
      * @return datos codificados a Base64
      */
     public static String codificarBase64(byte[] datos){
         return new String(Base64.encodeBase64(datos));
     }
     /**
      * Método encargado de decodificar de Base64 una cadena de caracteres
      * @param datos a decodificar de Base64
      * @return datos decodificados
      */
     public static String decodificarBase64(String datos){
         return decodificarBase64(datos.getBytes());
     }
     /**
      * Método encargado de decodificar de Base64 un arreglo de bytes
      * @param datos a decodificar de Base64
      * @return datos decodificados
      */
     public static String decodificarBase64(byte[] datos){
         return new String(Base64.decodeBase64(datos));
     }
     /**
      * Método encargado de codificar a Hexadecimal una 
      * cadena de caracteres
      * @param datos a codificar a Hexadecimal
      * @return datos codificados
      */
     public static String codificarHex(String datos){
         return codificarHex(datos.getBytes());
     }
     /**
      * Método encargado de codificar a Hexadecimal un arreglo de bytes
      * @param datos a codificar a Hexadecimal
      * @return datos codificados
      */
     public static String codificarHex(byte[] datos){
         return new String(Hex.encodeHex(datos));
     }
     /**
      * Método encargado de decodificar de Hexadecimal una cadena de
      * caracteres 
      * @param datos a decodificar de Hexadecimal
      * @return datos decodificados
      */
     public static String decodificarHex(String datos){
          return decodificarHex(datos.toCharArray());
     }
     /**
      * Método encargado de decodificar a Hexadecimal un arreglo de 
      * caracteres
      * @param datos a decodificar de Hexadecimal
      * @return datos decodificados
      */
     public static String decodificarHex(char[] datos){
         String d="";
         try{
             d=new String(Hex.decodeHex(datos));
         }catch(DecoderException ex){
             System.err.println("ERROR: Número impar o ilegal de caracteres");
         }
         return d;
     }
     /**
      * Método encargado de obtener el certificado a party de un arreglo de bytes
      * para el proceso de la facturación electrónica
      * @param Certificado csd del contribuyente
      * @return csd en formato estandarizado para el manejo de certificados en java
      * @throws CertificateException
      * @throws IOException 
      */
     public static X509Certificate obtenerCertificado(byte[] Certificado) throws CertificateException,IOException{
         CertificateFactory factory = CertificateFactory.getInstance("X.509");
         InputStream certificado=new ByteArrayInputStream(Certificado);
         X509Certificate cer=(X509Certificate)factory.generateCertificate(certificado);
         return cer;
     }
     /**
      * Método encargado de obtener el número de certificado del CSD del 
      * contribuyente
      * @param Certificado csd del contribuyente
      * @return Número del certificado en un String para su uso en la facturación
      * electrónica
      * @throws CertificateException
      * @throws IOException 
      */
     public static String obtenerNumeroCertificado(byte[] Certificado) throws CertificateException,IOException{
         CertificateFactory factory = CertificateFactory.getInstance("X.509");
         InputStream certificado=new ByteArrayInputStream(Certificado);
         X509Certificate cer=(X509Certificate)factory.generateCertificate(certificado);
         byte[] arregloNoCertificado=cer.getSerialNumber().toByteArray();
         String noCertificado=new String(arregloNoCertificado);
         return noCertificado;
     }
 }
