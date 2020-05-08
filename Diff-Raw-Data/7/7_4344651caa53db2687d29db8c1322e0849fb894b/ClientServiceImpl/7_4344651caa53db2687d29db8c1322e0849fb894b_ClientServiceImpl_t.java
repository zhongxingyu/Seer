 package org.openremote.controller.service.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.security.InvalidKeyException;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.PublicKey;
 import java.security.Security;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Hex;
 import org.apache.log4j.Logger;
 import org.bouncycastle.asn1.ASN1Encodable;
 import org.bouncycastle.asn1.ASN1EncodableVector;
 import org.bouncycastle.asn1.ASN1InputStream;
 import org.bouncycastle.asn1.ASN1ObjectIdentifier;
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.ASN1Set;
 import org.bouncycastle.asn1.DERObjectIdentifier;
 import org.bouncycastle.asn1.DEROctetString;
 import org.bouncycastle.asn1.DERSequence;
 import org.bouncycastle.asn1.DERSet;
 import org.bouncycastle.asn1.pkcs.Attribute;
 import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
 import org.bouncycastle.asn1.util.ASN1Dump;
 import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
 import org.bouncycastle.asn1.x509.X509Extension;
 import org.bouncycastle.asn1.x509.X509Extensions;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
 import org.bouncycastle.pkcs.PKCS10CertificationRequest;
 import org.bouncycastle.util.encoders.Base64;
 import org.bouncycastle.x509.X509V2AttributeCertificate;
 import org.openremote.controller.Constants;
 import org.openremote.controller.ControllerConfiguration;
 import org.openremote.controller.service.ClientService;
 import org.openremote.controller.service.DatabaseService;
 
 import sun.misc.BASE64Decoder;
 import sun.nio.cs.ext.PCK;
 
 /**
  * Get client information out the (request) certificates
  * 
  * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
  */
 
 public class ClientServiceImpl implements ClientService {
    static {
       Security.addProvider(new BouncyCastleProvider());
    }
    private final static Logger logger = Logger.getLogger(Constants.REST_ALL_PANELS_LOG_CATEGORY);
 
    private static final String openssl = "openssl";
    private static final String CRTDir = "certs";
    private static final String CSRDir = "csr";
    private static final String KEYSTORE_PASSWORD = "password";
    
    private static String selectClientQuery = "SELECT * FROM client WHERE client_id = ";
    private static String selectAllClientsQuery = "SELECT * FROM client ORDER BY client_creation_timestamp ASC";
    private static String insertClientQuery = "INSERT INTO client (client_serial, client_pincode, client_device_name, client_email, client_alias, client_active, client_creation_timestamp, client_modification_timestamp) VALUES ";
    private static String limitByOne = " LIMIT 1";
 
    private DatabaseService database;
    private ControllerConfiguration configuration;
    private String rootCADir = "";
    private String serial = "";
    private String pin;
    private String email;
    private String deviceName;
 
    /**
     * Get all clients.
     * 
     * @return The result set from the database with all the information from every client
     */
    @Override
    public ResultSet getClients() {
       ResultSet returnValue = null;
       if (database != null) {
          returnValue = database.doSQL(selectAllClientsQuery);
       } else {
          logger.error("Database is not yet set (null)");
       }
       return returnValue;
    }
 
    /**
     * Add new client to the database.
     * 
     * @param alias clients alias
     * @return int 0 = error with select or insert, 1 insert query went successfully, 2 user already exists
     */
    @Override
    public int addClient(String alias) {
       this.parseCSRFile(alias);
 
       return this.addClient(this.getPin(), this.getDeviceName(), this.getEmail(), alias);
    }
 
    /**
     * Add new client to the database.
     * 
     * @param pinCode
     *           the client pin
     * @param deviceName
     *           the client device name
     * @param email
     *           the client e-mail address
     * @param alias
     *           the client alias
     * @return int 0 = error with select or insert, 1 insert query went successfully, 2 user already exists
     */
    @Override
    public int addClient(String pin, String deviceName, String email, String alias) {
       int returnValue = 0;
       int resultValue = -1;
       int numRows = -1;
 
       if (database != null) {
          database.doSQL("SELECT client_pincode, client_device_name FROM PUBLIC.client WHERE client_pincode = '" + pin
                + "' AND client_device_name = '" + deviceName + "' LIMIT 1");
          numRows = database.getNumRows();
       }
 
       // Check if client doesn't exist in the database
       if (numRows == 0) {
          if (database != null) {
             resultValue = database.doUpdateSQL(insertClientQuery + "('', '" + pin + "', '" + deviceName + "', '"
                   + email + "', '" + alias + "', FALSE, NOW, NOW);");
          } else {
             logger.error("Database is not yet set (null)");
          }
 
          if (resultValue >= 1) {
             returnValue = 1;
          }
       } else {
          // ignore a second user with the same device name and pin
          returnValue = 2;
       }
       return returnValue;
    }
 
    /**
     * Get one client result set from the database.
     * 
     * @param clientID
     *           id from the client
     * @return ResultSet the result from the database with client information
     */
    @Override
    public ResultSet getClient(int clientID) {
       ResultSet returnValue = null;
       if (database != null) {
          returnValue = database.doSQL(selectClientQuery + clientID + limitByOne);
       } else {
          logger.error("Database is not yet set (null)");
       }
       return returnValue;
    }
 
    /**
     * Updates the client active boolean flag in the database.
     * 
     * @param clientID
     *           is the client id
     * @param active
     *           boolean true is active false is non-active
     * @return int value -1 or 0 is incorrect, 1 is action succeed
     */
    @Override
    public int updateClientStatus(int clientID, boolean active) {
       int resultValue = -1;
 
       if (database != null) {
          resultValue = database.doUpdateSQL("UPDATE client SET client_active = " + active + " WHERE client_id = "
                + clientID);
       }
       return resultValue;
    }
 
    /**
     * Update client serial number.
     * 
     * @param clientID
     *           id client
     * @return value -1 or 0 is , 1 is correct
     */
    @Override
    public int updateClientSerial(int clientID, String serial) {
       int resultValue = -1;
 
       if (database != null && !serial.isEmpty()) {
          resultValue = database.doUpdateSQL("UPDATE client SET client_serial = '" + serial + "' WHERE client_id = "
                + clientID);
       }
       return resultValue;
    }  
    
    @Override
    public int clearClientSerial(int clientID) {
       int resultValue = -1;
 
       if (database != null) {
          resultValue = database.doUpdateSQL("UPDATE client SET client_serial = '' WHERE client_id = " + clientID);
       }
       return resultValue;
    }
    
    @Override
    public String getSerial() {
       return serial;
    }
 
    /**
     * Get the X509 Certificate from the client key store file via username alias
     * @param alias is the certificate alias name
     */
    @Override
    public X509Certificate getClientCertificate(String alias)
    {
       if (rootCADir.isEmpty()) {
          this.rootCADir = configuration.getCaPath();
       }
       KeyStore clientKS;
       X509Certificate certificate = null;
       String client_key_store = this.rootCADir + "/../client_certificates.jks";
       
       try
       {
          clientKS = KeyStore.getInstance("JKS");
 
          // Load the key store to memory.
          FileInputStream fis = new FileInputStream(client_key_store);  
          clientKS.load(fis, KEYSTORE_PASSWORD.toCharArray()); 
          
          certificate = (X509Certificate) clientKS.getCertificate(alias);
          logger.error("Client certificate test: " + certificate.getIssuerDN());
       } catch (NoSuchAlgorithmException e) {
          logger.error("Client certificate: " + e.getMessage());
       } catch (CertificateException e) {
          logger.error("Client certificate: " + e.getMessage());
       } catch (IOException e) {
          logger.error("Client certificate: " + e.getMessage());
       } catch (KeyStoreException e) {
          logger.error("Client certificate: " + e.getMessage());
       }
       return certificate;
    }   
    
    /**
     * Close the result set.
     */
    @Override
    public void free() {
       database.free();
    }
 
    /**
     * Returns the number of clients. Note: You should use getClients() first and directly a getNumClients()
     * 
     * @see #getClients()
     * 
     * @return int of the number of clients
     */
    @Override
    public int getNumClients() {
       int newNum = -1;
       if (database != null) {
          newNum = database.getNumRows();
       }
       return newNum;
    }
 
    /**
     * Sets the database.
     * 
     * @param database
     *           service
     */
 
    public void setDatabase(DatabaseService database) {
       this.database = database;
    }
 
    /**
     * Sets the configuration.
     * 
     * @param configuration
     *           the new configuration
     */
    public void setConfiguration(ControllerConfiguration configuration) {
       this.configuration = configuration;
    }
 
    private void parseCSRFile(String alias)
    {
       // init
       pin = "";
       email = "";
       deviceName = "";
       PKCS10CertificationRequest certificationRequest = null;
       try {
          certificationRequest = this.getCertificationRequest(alias);
       } catch (IOException e) {
          logger.error("Parse CSR error: " + e.getMessage());
       }
 
       if(certificationRequest != null)
       {      
          // Get pin
          try {
             ASN1Sequence seqkey = ASN1Sequence.getInstance(certificationRequest.getSubjectPublicKeyInfo().getPublicKey());
             RSAPublicKeyStructure publicKey = new RSAPublicKeyStructure(seqkey);
            
            pin = generateMD5Sum(publicKey.getModulus().toByteArray());
            logger.error("Public modulus: "  +publicKey.getModulus());
 
             logger.error("MD5 key: " + pin);
             pin = pin.substring(pin.length() - 4);
          } catch (IOException e) {
             logger.error("Can't get public key.");
          }
          
          
          Attribute[] attributes = certificationRequest.getAttributes(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest);
          if(attributes != null && attributes.length >= 1)
          {
             
             ASN1Set attributeSet = attributes[0].getAttrValues();
             for (int i = 0; i != attributeSet.size(); i++)
             {
                ASN1Encodable object = attributeSet.getObjectAt(i);
                X509Extensions extensions = X509Extensions.getInstance(object);
                
                X509Extension ext = extensions.getExtension(X509Extension.subjectAlternativeName);
               
                email = new String(ext.getValue().getOctets()).substring(4);               
             }
             // Get device name
             deviceName = certificationRequest.getSubject().toString();
             deviceName = deviceName.substring(deviceName.indexOf("CN=") + 3);
             //deviceName = deviceName.substring(0, deviceName.indexOf("\n"));
          }
          else
          {
             logger.error("Certification request couldn't be decoded.");
          }
       }
 
    }
 
    private PKCS10CertificationRequest getCertificationRequest(String alias) throws IOException {
       if (rootCADir.isEmpty()) {
          this.rootCADir = configuration.getCaPath();
       }
 
       File file = new File(rootCADir + "/" + CSRDir + "/" + alias + ".csr");
       String data = "";
 
       FileInputStream fis = new FileInputStream(file);
       data = convertStreamToString(fis);
 
       BASE64Decoder decoder = new BASE64Decoder();
       byte[] decodedBytes = decoder.decodeBuffer(data);
 
       return new PKCS10CertificationRequest(decodedBytes);
    }
 
    private String convertStreamToString(InputStream is) throws IOException {
       if (is != null) {
          Writer writer = new StringWriter();
 
          char[] buffer = new char[1024];
          try {
             Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             int n;
             while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
             }
          } finally {
             is.close();
          }
          return writer.toString();
       } else {
          return "";
       }
    }
 
    private String getPin() {
       return pin;
    }
 
    private String getEmail() {
       return email;
    }
 
    private String getDeviceName() {
       return deviceName;
    }
 
    /*
    private String executeOpenSSLCommand(String path, String fileName, boolean isCert) {
       List<String> command = new ArrayList<String>();
       command.add(openssl); // command
       if (isCert) // CRT
       {
          command.add("x509");
          command.add("-subject");
          command.add("-enddate");
          command.add("-serial");
          command.add("-noout");
       } else // CSR
       {
          command.add("req");
          command.add("-subject");
          command.add("-noout");
          command.add("-pubkey");
          command.add("-text");
       }
       command.add("-in"); // input file
       command.add(path + "/" + fileName); // file path
 
       if (rootCADir.isEmpty()) {
          this.rootCADir = configuration.getCaPath();
       }
 
       ProcessBuilder pb = new ProcessBuilder(command);
       pb.directory(new File(rootCADir));
 
       Process p = null;
       StringBuffer buffer = new StringBuffer();
       try {
          p = pb.start();
          p.waitFor();
 
          BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
          String line = null;
          while ((line = br.readLine()) != null) {
             buffer.append(line).append("\n");
          }
 
       } catch (IOException e) {
          logger.error(e.getMessage());
       } catch (InterruptedException e) {
          logger.error(e.getMessage());
       }
 
       return buffer.toString();
    }
    
    */
 
    private String generateMD5Sum(String message) {
       byte[] resultByte = null;
 
       try {
          final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
          messageDigest.reset();
          messageDigest.update(message.getBytes(Charset.forName("UTF8")));
          resultByte = messageDigest.digest();
       } catch (NoSuchAlgorithmException e) {
          logger.error(e.getMessage());
       }
       return new String(Hex.encodeHex(resultByte));
    }
    
    private String generateMD5Sum(byte[] message) {
       byte[] resultByte = null;
 
       try {
          final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
          messageDigest.reset();
          messageDigest.update(message);
          resultByte = messageDigest.digest();
       } catch (NoSuchAlgorithmException e) {
          logger.error(e.getMessage());
       }
       return new String(Hex.encodeHex(resultByte));
    }
 }
