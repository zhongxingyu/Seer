 /*
  * Copyright (c) 2012 Helsinki Institute of Physics All rights reserved.
  * See LICENCE file for licensing information.
  */
 
 package eu.emi.security.canl.tomcat;
 
 import org.apache.tomcat.util.net.SSLImplementation;
 import org.apache.tomcat.util.net.SSLSupport;
 import org.apache.tomcat.util.net.ServerSocketFactory;
 import org.apache.tomcat.util.net.jsse.JSSEImplementation;
 
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.Socket;
 import java.util.Properties;
 
 import javax.net.ssl.SSLSession;
 
 /**
  * The main Tomcat 5 and 6 glue class, tomcat 7 is different, and will need
  * another implementation
  * 
  * Created on 2012-06-13
  * 
  * @author Joni Hahkala
  */
 public class CANLSSLImplementation extends SSLImplementation {
 
     /**
      * The constructor for the class, does nothing except checks that the actual
      * ssl implementation TrustManager is present.
      * 
      * @throws ClassNotFoundException in case the util-java is not installed and
      *             thus ContextWrapper class isn't found.
      */
     public CANLSSLImplementation() throws ClassNotFoundException {
         // Reading resources of JAR file
         InputStream in = null;
         Properties props = null;
         try {
             in = this.getClass().getClassLoader()
                     .getResourceAsStream("META-INF/maven/eu.eu-emi.security/canl-java-tomcat/pom.properties");
             props = new Properties();
             props.load(in);
             String canlTomcatVersion = props.getProperty("version");
             System.out.println("Tomcat pluging version " + canlTomcatVersion + " starting.");
         } catch (Exception e) {
             System.out.println("Canl tomcat plugin starting, version information loading failed. " + in + ", " + props
                     + " exception: " + e + ": " + e.getMessage());
             e.printStackTrace();
         }
         try {
             in = this.getClass().getClassLoader()
                     .getResourceAsStream("META-INF/maven/eu.eu-emi.security/canl/pom.properties");
             props = new Properties();
             props.load(in);
             String canlVersion = props.getProperty("version");
             System.out.println("CANL version " + canlVersion + " starting.");
         } catch (Exception e) {
             boolean oldSuccess = false;
             try {
                 in = this.getClass().getClassLoader()
                         .getResourceAsStream("META-INF/maven/eu.emi.security/canl/pom.properties");
                 props = new Properties();
                 props.load(in);
                 String canlVersion = props.getProperty("version");
                 System.out.println("CANL version " + canlVersion + " starting.");
                 oldSuccess = true;
            } catch (Exception ex) {
                // ignore failure in fallback
             }
             if (!oldSuccess) {
                 System.out.println("Canl tomcat plugin starting, canl version information loading failed. " + in + ", "
                         + props + " exception: " + e + ": " + e.getMessage());
                 e.printStackTrace();
             }
         }
         // Check to see if canl is floating around
         // somewhere, will fail if it is not found throwing
         // an exception, this forces early failure in case there is no hope of
         // it working anyway.
         Class.forName("eu.emi.security.authn.x509.CommonX509TrustManager");
     }
 
     /*
      * The Method that returns the name of the SSL implementation
      * 
      * The string "TM-SSL" is returned (shorthand for TrustManager SSL)
      * 
      * @see org.apache.tomcat.util.net.SSLImplementation#getImplementationName()
      */
     public String getImplementationName() {
         return "CANL-SSL";
     }
 
     /*
      * The method used by Tomcat to get the actual SSLServerSocketFactory to use
      * to create the ServerSockets.
      * 
      * @see
      * org.apache.tomcat.util.net.SSLImplementation#getServerSocketFactory()
      */
     public ServerSocketFactory getServerSocketFactory() {
         return new CANLSSLServerSocketFactory();
     }
 
     /*
      * The method used to get the class that provides the SSL support functions.
      * Current implementation reuses Tomcat's own JSSE SSLSupport class as we
      * use JSSE internally too (with modifications to the certificate path
      * checking of course.
      * 
      * @see
      * org.apache.tomcat.util.net.SSLImplementation#getSSLSupport(java.net.Socket
      * )
      */
     public SSLSupport getSSLSupport(Socket arg0) {
         try {
             JSSEImplementation impl = new JSSEImplementation();
 
             return impl.getSSLSupport(arg0);
         } catch (ClassNotFoundException e) {
             System.out.println("Internal server error, JSSEImplementation class creation failed: " + e.getClass()
                     + e.getMessage());
 
             return null;
         }
     }
 
     /*
      * The method used to get the class that provides the SSL support functions.
      * Current implementation reuses Tomcat's own JSSE SSLSupport class as we
      * use JSSE internally too (with modifications to the certificate path
      * checking of course.
      * 
      * @see
      * org.apache.tomcat.util.net.SSLImplementation#getSSLSupport(java.net.ssl
      * .SSLSession)
      */
     public SSLSupport getSSLSupport(SSLSession arg0) {
         try {
             JSSEImplementation impl = new JSSEImplementation();
             // hack to get past tomcat5 missing this method and tomcat6
             // requiring it.
             java.lang.reflect.Method method;
 
             try {
                 method = impl.getClass().getMethod("getSSLSupport", arg0.getClass());
             } catch (NoSuchMethodException e) {
                 // this is tomcat5, so no action.
                 return null;
             }
 
             try {
                 return (SSLSupport) method.invoke(impl, arg0);
             } catch (IllegalArgumentException e) {
                 System.out.println("Internal server error, JSSEImplementation class creation failed: " + e.getClass()
                         + e.getMessage());
             } catch (IllegalAccessException e) {
                 System.out.println("Internal server error, JSSEImplementation class creation failed: " + e.getClass()
                         + e.getMessage());
             } catch (InvocationTargetException e) {
                 System.out.println("Internal server error, JSSEImplementation class creation failed: " + e.getClass()
                         + e.getMessage());
             }
             return null;
         } catch (ClassNotFoundException e) {
             System.out.println("Internal server error, JSSEImplementation class creation failed: " + e.getClass()
                     + e.getMessage());
 
             return null;
         }
     }
 
 }
