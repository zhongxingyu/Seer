 /*
  * Documentation for Jessy:
  * http://docs.oracle.com/javase/1.4.2/docs/api/javax/net/ssl/package-summary.html
  */
 
 import java.security.*;
 import javax.security.cert.X509Certificate;
 import javax.net.*;
 import javax.net.ssl.*;
 import java.io.*;
 import java.util.logging.*;
 
 public class JournalServer {
     private static final int LENGTH_LENGTH = 4; // length of the length field, bytes
     private KeyStore keyStore;
 	private Logger log;
 
     public static void main(String args[]) {
		new JournalServer().start(8080);
     }
 
     public JournalServer() {
 	try {
 	    keyStore = KeyStore.getInstance("JKS");
 	} catch (KeyStoreException e) {
 	    trace("could not open keystore. exiting (" + e + ")");
 	}		
 
 	FileHandler fileHandler = null;
 	try {
 		fileHandler = new FileHandler("logs/audit.log", 10*1024*1024, 100, true);
 	} catch (IOException ioe) {
 		trace("Could not open log file.");
 	}
 	fileHandler.setFormatter(new SimpleFormatter());
 	log = Logger.getLogger("auditLog");
 	log.addHandler(fileHandler);
     }
 
     private void trace(String msg) {
 		System.out.println("SERVER:\t" + msg);
     }
 
 	private void log(String logmsg) {
 		log.info(logmsg);
 	}
 
     public void start(int port) {
 	SSLServerSocketFactory ssf = null;
 	SSLServerSocket ss;
 
 	// TODO remove these debugging hacks? Maybe the server must be started by personsknowing the passwords?
 	char[] keyStorePasswd = "password1".toCharArray();
 	char[] keyPasswd = "password2".toCharArray();
 	char[] trustPasswd = "password3".toCharArray();
 
 	SSLContext ctx;
 	KeyManagerFactory kmf;
 	KeyStore ks, ts;
 	TrustManagerFactory tmf;
 	try {
 	    ctx = SSLContext.getInstance("TLS");
 	    kmf = KeyManagerFactory.getInstance("SunX509");
 	    ks = KeyStore.getInstance("JKS");
 	    ks.load(new FileInputStream("./keystore"), keyStorePasswd);
 	    kmf.init(ks, keyPasswd);
 
 	    tmf = TrustManagerFactory.getInstance("SunX509");
 	    ts = KeyStore.getInstance("JKS");
 	    ts.load(new FileInputStream("./truststore"), trustPasswd);
 	    tmf.init(ts);
 	    
 	    ctx = SSLContext.getInstance("TLS");
 	    ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
 	    ssf = ctx.getServerSocketFactory();
 	} catch (Exception e) {
 	    trace("shit went south, bailing. (" + e + ")");
 	    System.exit(1);
 	}
 		
 	try {
 	    ss = (SSLServerSocket) ssf.createServerSocket(port);
 	} catch (Exception e) {
 	    trace("could not bind to port. " + e);
 	    return;
 	}
 
 	ss.setNeedClientAuth(true);
 
 	// accept clients, maybe sould be multithreaded later
 	while (true) {
 	    trace("waiting for incomming connection");
 	    SSLSocket sock;
 
 	    try {
 		sock = (SSLSocket) ss.accept();
 	    } catch (java.io.IOException e) {
 		trace("failed to accept connection");
 		continue;
 	    }
 
 	    trace("accepted incomming connection");
 
 	    // String[] suites = {"TLS_DHE_DSS_WITH_AES_256_CBC_SHA"};
 	    // sock.setEnabledCipherSuites(suites);
 	    SSLSession sess = sock.getSession();
 	    X509Certificate cert;
 
 	    try {
 		cert = (X509Certificate)sess.getPeerCertificateChain()[0];
 	    } catch (javax.net.ssl.SSLPeerUnverifiedException e) {
 		trace("client not verified");
 		try {
 		    sock.close();
 		} catch (java.io.IOException e2) {
 		    trace("failed closing socket, w/e");
 		}
 		continue;
 	    }
 
 	    String subj = cert.getSubjectDN().getName();
 	    System.out.println("SERVER:\tclient DN: " + subj);
 
 	    int readBytes = 0;
 	    int tmp, tmp_shift;
 	    int length = 0;
 	    InputStream in;
 	    try {
 		in = sock.getInputStream();
 	    } catch (java.io.IOException e) {
 		trace("failed to get inputstream");
 		try {
 		    sock.close();
 		} catch (java.io.IOException e2) {
 		    trace("failed closing socket, w/e");
 		}
 		continue;
 	    }
 
 	    while (readBytes < LENGTH_LENGTH) {
 		try {
 		    tmp = in.read();
 		} catch (java.io.IOException e) {
 		    continue;
 		}
 		readBytes += 1;
 		tmp_shift = tmp << (LENGTH_LENGTH - readBytes);
 		length |= tmp_shift;
 		System.out.printf("raw:%s shifted:%d addedToLength:%d\n", Integer.toHexString(tmp), tmp_shift, length);
 	    }
 
 	    if (readBytes == LENGTH_LENGTH) {
 		trace("the msg is " + length + " bytes long");
 	    } else {
 		trace("SERVER:\tfailed to read length field");
 		continue;
 	    }
 	    // got length, do work.
 	    InputStreamReader reader = new InputStreamReader(in);
 	    char[] message = new char[length];
 	    int ret;
 	    int offset = 0;
 	    while (offset < length) {
 		try {
 		    ret = reader.read(message, offset, (length - offset));
 		} catch(Exception e) {
 		    trace("got exception while reading message: " + e.toString());
 		    break;
 		}
 
 		if (ret == -1) {
 		    trace("fuck. something went south. breaking the parsing of message.");
 		    break;
 		}
 		offset += ret;
 	    }
 	    if (offset < length) {
 		trace("could not read complete message");
 		break;
 	    }
 	    this.parseCmd(message);
 	}
     }
 
     protected String parseCmd(char[] cmd) {
 	return "You wrote " + cmd + "\n";
     }
 
 }
