 package org.agora;
 
 import org.agora.utils.*;
 
 import java.util.Arrays;
 import netscape.javascript.JSObject;
 import java.applet.Applet;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.net.URLDecoder;
 import java.net.URLConnection;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 
 import javax.swing.JDialog;
 import javax.swing.Action;
 import javax.swing.AbstractAction;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.BoxLayout;
 import javax.swing.BorderFactory;
 import javax.swing.JOptionPane;
 import javax.swing.JPasswordField;
 import java.awt.Frame;
 import java.awt.*;
 import java.awt.event.*;
 
 import org.apache.commons.codec.binary.Base64;
 
 import java.security.Key;
 import java.security.PrivilegedAction;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.Provider;
 import java.security.Security;
 import java.security.Signature;
 import java.security.MessageDigest;
 import java.security.AccessController;
 import java.util.Enumeration;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 
 import verificatum.eio.ByteTree;
 import verificatum.eio.ByteTreeBasic;
 import verificatum.eio.ByteTreeReader;
 import verificatum.arithm.ModPGroup;
 import verificatum.arithm.PGroup;
 import verificatum.arithm.PGroupElement;
 import verificatum.arithm.PGroupElementArray;
 import verificatum.arithm.PPGroup;
 import verificatum.arithm.PPGroupElement;
 import verificatum.arithm.PRing;
 import verificatum.arithm.PFieldElement;
 import verificatum.arithm.PRingElement;
 import verificatum.arithm.PRingElementArray;
 import verificatum.crypto.RandomOracle;
 import verificatum.crypto.CryptoKeyGen;
 import verificatum.crypto.CryptoKeyGenCramerShoup;
 import verificatum.crypto.CryptoKeyPair;
 import verificatum.crypto.Hashfunction;
 import verificatum.crypto.HashfunctionHeuristic;
 import verificatum.crypto.PRG;
 import verificatum.crypto.PRGHeuristic;
 import verificatum.crypto.RandomSource;
 import verificatum.protocol.mixnet.MixNetElGamalInterface;
 
 
 public class VotingApplet extends Applet {
     protected VotingDelegate mVotingDelegate = new VotingDelegate();
     protected String mAppletInfo = "Agora Ciudadana v0.1";
     protected SimpleLock mLock = new SimpleLock();
     public String mVendor = System.getProperty("java.vendor");
     public String mURL = System.getProperty("java.vendor.url");
     public String mVersion = System.getProperty("java.version");
     public String mOSArch = System.getProperty("os.arch");;
     public String mOSVersion = System.getProperty("os.version");
 
     class PinCancelledByUser extends Exception {
         public PinCancelledByUser(String message) {
             super(message);
         }
     }
 
     class BallotCastingError extends Exception {
         public BallotCastingError(String message) {
             super(message);
         }
     }
 
     class SignatureCertificateNotFoundError extends Exception {
         public SignatureCertificateNotFoundError(String message) {
             super(message);
         }
     }
 
     class CertificateWithoutPrivateKeyError extends Exception {
         public CertificateWithoutPrivateKeyError(String message) {
             super(message);
         }
     }
 
     class InitTimeoutError extends Exception {
         public InitTimeoutError(String message) {
             super(message);
         }
     }
 
     class PinTimeoutError extends Exception {
         public PinTimeoutError(String message) {
             super(message);
         }
     }
 
     class UserInputTimeoutError extends Exception {
         public UserInputTimeoutError(String message) {
             super(message);
         }
     }
 
     class LoadCardDataTimeoutError extends Exception {
         public LoadCardDataTimeoutError(String message) {
             super(message);
         }
     }
 
     class SignatureTimeoutError extends Exception {
         public SignatureTimeoutError(String message) {
             super(message);
         }
     }
 
     class SendBallotsTimeoutError extends Exception {
         public SendBallotsTimeoutError(String message) {
             super(message);
         }
     }
 
     class IncompatibleJavaVersion extends Exception {
         public IncompatibleJavaVersion(String message) {
             super(message);
         }
     }
 
     static String encode(byte[] bytes) throws Exception {
         byte[] encoded = Base64.encodeBase64(bytes);
         return new String(encoded, "ASCII");
     }
 
     static byte[] decode(String str) throws Exception {
         byte[] bytes = str.getBytes("ASCII");
         return Base64.decodeBase64(bytes);
     }
 
     public String getAppletInfo()
     {
         return mAppletInfo;
     }
 
     /**
      * Terminates the java applet process.
      * Usually called via Javascript when something went so wrong that in order
      * to recover from that problem, the applet needs to be relaunched.
      */
     public void terminateApplet()
     {
         AccessController.doPrivileged(new PrivilegedAction() {
             public Object run() {
                 System.exit(0);
                 return null;
             }
         });
     }
 
     protected void checkJavaVersionCompatibility()
     {
         asyncUpdate("DEBUG_INFO", mVendor + ", " + mVersion);
         if (!mVersion.startsWith("1.6") && !mVersion.startsWith("1.7")) {
             asyncException(new IncompatibleJavaVersion("Vendor: " + mVendor +
                 ", Version: " + mVendor));
         }
     }
 
     public void init()
     {
         System.out.println("automatically init applet");
         super.init();
 
         asyncUpdate("INITIALIZED", "applet is up and running");
 
         final VotingApplet appletFinal = this;
         // Threads that checks every once in a while that the dnie card reader
         // and card are still there
         Thread t = new Thread(new Runnable() {
             public void run()
             {
                 // Doing it in the thread so that we know that the applet
                 // has returned already the control to javascript and
                 // asyncException can be called
                 checkJavaVersionCompatibility();
                 while (true) {
                     System.out.println("checking card inside reader");
                     AccessController.doPrivileged(new PrivilegedAction() {
                         public Object run() {
                             final long startTime = System.nanoTime();
                             appletFinal.mLock.lock();
 
                             try {
                                 appletFinal.mVotingDelegate.setVotingApple(appletFinal);
                                 appletFinal.mVotingDelegate.init();
                                 final long endTime = System.nanoTime();
                                 final long duration = endTime - startTime;
                                 appletFinal.asyncUpdate("CARD_INSIDE_READER", "took " + duration);
                             } catch (Exception e) {
 //                                 e.printStackTrace();
                                 appletFinal.asyncException(e);
                             } finally {
                                 appletFinal.mLock.unlock();
                             }
 
                             return null;
                         }
                     });
                     try {
                         Thread.sleep(4000);
                     } catch (Exception e) {
                         // Should never happen
                         e.printStackTrace();
                         appletFinal.asyncException(e);
                     }
                 }
             }
         });
         t.start();
     }
 
     public void asyncUpdate(String code, String description)
     {
         JSObject win = JSObject.getWindow(this);
         Object params[] = new Object[2];
         params[0] = code;
         params[1] = description;
         System.out.println("update with code = " + code);
         win.call("async_update", params);
     }
 
     public void asyncException(Exception e)
     {
         JSObject win = JSObject.getWindow(this);
         Object params[] = new Object[2];
         params[0] = e.getClass().getName();
         params[1] = e.getMessage();
 
         System.out.println("asyncError: ");
         System.out.println(e.getClass().getName() + ": " + e.getMessage());
         win.call("async_exception", params);
     }
 
     public void paint(Graphics g)
     {
         super.paint(g);
         g.drawString(getAppletInfo(), 5, 15);
     }
 
     /**
      * Processes and cast a vote.
      *
      * @param ballot ballot string. The format should be:
      *              "<vote 1 id>,<proposal 1 id>,<proposal 1 public key>,
      *              [<vote n id>,<proposal n id>,<proposal n public key> ...]"
      * @param baseUrl base url to use for the web server, for example
      *                https://localhost:3000 (with no ending slash character)
      *
      * @return "SUCCESS" via asyncUpdate or some kind of error via asyncException.
      */
     public void vote(String ballot, String baseUrl)
     {
         final String ballotFinal = ballot;
         final String baseUrlFinal = baseUrl;
         final VotingApplet appletFinal = this;
         Thread t = new Thread(new Runnable() {
             public void run()
             {
                 asyncUpdate("VOTING", "Starting to send a vote..");
                 appletFinal.mLock.lock();
                 try {
                     mVotingDelegate.setBallot(ballotFinal);
                     mVotingDelegate.setBaseUrl(baseUrlFinal);
                     mVotingDelegate.setVotingApple(appletFinal);
                     AccessController.doPrivileged(mVotingDelegate);
                 } catch (Exception e) {
                     e.printStackTrace();
                     asyncException(e);
                 } finally {
                     appletFinal.mLock.unlock();
                 }
             }
         });
         t.start();
     }
 
     /**
      * Test method to be able to execute for testing purposses. Example:
      *    java -classpath deps/apache-commons-codec-1.4.jar:\
      *              deps/bcprov-1.45.jar:deps/verificatum.jar:\
      *              dist/lib/agora-applet.jar \
      *          org.Agora.VotingApplet \
      *          "http://localhost:8000"
      */
     public static void main(String[] args) throws Exception {
         VotingApplet applet = new VotingApplet();
         applet.init();
         String publicKeyString = "00000000020000000002010000001a766572696669636174756d2e61726974686d2e505047726f7570000000000200000000010000000002010000001c766572696669636174756d2e61726974686d2e4d6f645047726f7570000000000401000001010188cb0fcd7f00ffd629ee7c0426036c09cd1ae4576e3cde79680733bd13b0b1ef6ace0082d1cc0839d8d8f89d302570dcd4b7178fd8edfd54166d891b5f5b435c0cba214c471dda545897a12a9b53956fb76a6d647295011bec650e0609b50f97ffa16201ffd5b6368083965abf0a2f73ae0c2a9dc315ca63253df511176551961440aebbee1495c5bf318b9228c77938c4508063048161e9e4d83cbec3a3c16d856d1cf4d5f5c10d979476b39cd3541786fbc22d12326d8c66119a847c360e36294d4e1d14b1f80757f9fbfec49146e86b60130ecda55ac889aa733198a849946656c37cb5cbe3fd60d92900476ee6a9dac4825d89296fb5601a357de2121537010000010100c46587e6bf807feb14f73e021301b604e68d722bb71e6f3cb40399de89d858f7b567004168e6041cec6c7c4e9812b86e6a5b8bc7ec76feaa0b36c48dafada1ae065d10a6238eed2a2c4bd0954da9cab7dbb536b2394a808df632870304da87cbffd0b100ffeadb1b4041cb2d5f8517b9d706154ee18ae531929efa888bb2a8cb0a20575df70a4ae2df98c5c91463bc9c622840318240b0f4f26c1e5f61d1e0b6c2b68e7a6afae086cbca3b59ce69aa0bc37de116891936c63308cd423e1b071b14a6a70e8a58fc03abfcfdff6248a37435b0098766d2ad6444d53998cc5424ca332b61be5ae5f1feb06c948023b77354ed62412ec494b7dab00d1abef1090a9b010000010100e25212162cb0c7247fe38c94ee63c2f28af46f64aee6b967e6f77d3bee673db34346132755152d283644b7c9a7a97764862fa53466d1da6b24ccc105240829c8dfbf923e34bc01e49c1642cea67c183e13fccbe213eb2668cca873a44f2b43a76708b158ab785400750a132a754bf50eb290b84118b895837d1016e6bf957287cdb5bfbb360d2a9e4235be011f116be0bdd40423f60e22a65f3e323f5bb0eebc9e6f30fd3883ea9e9e8f7709df6aa6ad1cc3a834438e835c3f348c6a9f2b4c92dfc058f3bf375ec9bc5d9a4cf447f5a15e44632284d44b74901001e6b9fc30ea5e49ee15ac823205318a5af618d471fc7c8918d061043b58d2513bd3f13038be01000000040000000100000000020100000004000000000100000004000000000000000002010000010100e25212162cb0c7247fe38c94ee63c2f28af46f64aee6b967e6f77d3bee673db34346132755152d283644b7c9a7a97764862fa53466d1da6b24ccc105240829c8dfbf923e34bc01e49c1642cea67c183e13fccbe213eb2668cca873a44f2b43a76708b158ab785400750a132a754bf50eb290b84118b895837d1016e6bf957287cdb5bfbb360d2a9e4235be011f116be0bdd40423f60e22a65f3e323f5bb0eebc9e6f30fd3883ea9e9e8f7709df6aa6ad1cc3a834438e835c3f348c6a9f2b4c92dfc058f3bf375ec9bc5d9a4cf447f5a15e44632284d44b74901001e6b9fc30ea5e49ee15ac823205318a5af618d471fc7c8918d061043b58d2513bd3f13038be010000010100a5fa8159068f2fcba2b6e773091ed4c255b5510a217211905356ff96f3404279c4bd2dcdf5ccc0edf3ba6e32277d2807876d6118d49685d97e6fd535550919157146322e6d79cfa5df6800fe80cccf91d18ff336adbf41593cedc8eb3ae55f89b150433bfacc475e42fe2bd547c5fb1350304153ebea56d947afb81899a9ee78ab57602eb22f1391f5714f4f1d134056e9b0f6963c14f93bfc76523cf4021637ec423af2e6c8370533acf2cbafc31c00c71b1fe66ed5b8d623983b0ab49ac8193d9a4d59dda9438bca339a8245a9e12fd5ce669aa1494c2755efffb3a319a97fa0deb2ae35e2643e663843725ec0885e1783a8dfc5a2a3497e6cdf1cc538f638";
         String ballotStr = "0,1," + publicKeyString;
         applet.vote(ballotStr, args[0]);
         return;
     }
 
     public class VotingDelegate implements PrivilegedAction {
         protected static final String interfaceName = "native";
         protected static final String sendBallotsURLStr = "/votes";
         protected static final int certainty = 100;
         protected static final String confLinux=
             "name=OpenSC-OpenDNIe\nlibrary=/usr/lib/opensc-pkcs11.so\n";
         protected static final String confWindows=
             "name=OpenSC-OpenDNIe\r\nlibrary=C:\\WINDOWS\\system32\\opensc-pkcs11.dll\r\n";
         protected static final String confMac=
             "name=OpenSC-OpenDNIe\nlibrary=/Library/OpenSC/lib/opensc-pkcs11.so\n";
         protected static final String certAlias="CertFirmaDigital";
 
         protected RandomSource mRandomSource = null;
         protected VotingApplet mApplet = null;
 
         protected KeyStore mKeyStore = null;
         protected Certificate mCertificate = null;
         protected PrivateKey mPrivateKey = null;
         protected String mPin = null;
         protected String mBaseURLStr = null;
         protected String mVotesSignature = null;
         protected Provider mProvider = null;
 
         protected String mBallot = null;
         protected String mBaseUrl = null;
         protected String mReturnValue = null;
         protected boolean mInsideInit = false;
 
         void setBallot(String ballot) {
             mBallot = ballot;
         }
 
         void setBaseUrl(String baseUrl) {
             mBaseUrl = baseUrl;
         }
 
         void setVotingApple(VotingApplet applet) {
             mApplet = applet;
         }
 
         String returnValue() {
             return mReturnValue;
         }
 
         public void init() throws Exception
         {
             SimpleTimeout timeout = new SimpleTimeout(4000) {
                 public void timeout()
                 {
                     mApplet.asyncException(new InitTimeoutError("call to init took too long"));
                 }
             };
             timeout.start();
             try {
                 // Create PKCS#11 provider
                 String config = "";
                 String osName = System.getProperty("os.name").toLowerCase();
                 if (osName.startsWith("win")) {
                     config = confWindows;
                 } else if (osName.startsWith("lin")) {
                     config = confLinux;
                 } else if (osName.startsWith("mac")) {
                     config = confMac;
                 }
 
                 mProvider = new sun.security.pkcs11.SunPKCS11(
                     new ByteArrayInputStream(config.getBytes()));
                 Security.addProvider(mProvider);
 
                 // Create the keyStore
                 mKeyStore = KeyStore.getInstance("PKCS11", mProvider);
                 mApplet.asyncUpdate("CARD_FOUND", "Loading the DNIe certificate... (second part)");
             } catch (Exception e) {
                 throw e;
             } finally {
                 timeout.finish();
             }
         }
 
         /**
         * Initialize the applet.
         */
         protected void initialize() throws Exception {
             if (mKeyStore == null) {
                 init();
             }
             mRandomSource = new PRGHeuristic();
 
             // need to call this to be able to access to mKeyStore
             obtainPin();
 
             SimpleTimeout timeout = new SimpleTimeout(60*1000) {
                 public void timeout()
                 {
                     mApplet.asyncException(new LoadCardDataTimeoutError("call to initialize() took too long"));
                 }
             };
             timeout.start();
             try {
                 // Find signing cert in the cert list
                 mCertificate = null;
                 for (Enumeration<String> e = mKeyStore.aliases(); e.hasMoreElements();) {
                     String alias =e.nextElement();
                     System.out.println("alias = " + alias);
                     if (alias.equals(certAlias)) {
                         mCertificate = mKeyStore.getCertificate(alias);
                     }
                 }
                 if (mCertificate == null) {
                     throw new SignatureCertificateNotFoundError("Signature certificate not found");
                 }
                 String subject = ((X509Certificate)mCertificate).getSubjectX500Principal().toString();
                 System.out.println("certsubject = '" + subject + "'");
 
 
                 String serializedCertificate = encode(mCertificate.getEncoded());
                 CertificateFactory factory = CertificateFactory.getInstance("X.509");
                 X509Certificate certificate = (X509Certificate)factory.generateCertificate(
                     new ByteArrayInputStream(decode(serializedCertificate)));
 
                 // initialize the keystore with the PIN
                 Key key = mKeyStore.getKey(certAlias, mPin.toCharArray());
                 mPin = null;
 
                 if(!(key instanceof PrivateKey)) {
                     throw new CertificateWithoutPrivateKeyError("The certificate has no associated private key");
                 }
                 mPrivateKey = (PrivateKey)key;
 
                 timeout.finish();
             } catch (Exception e) {
                 timeout.finish();
                 throw e;
             }
         }
 
         /**
         * Asks the user for the dni-e pin and load the keystore.
         */
         protected void obtainPin() throws Exception {
             // ask for the user PIN three times at most, then show a dialog
             // saying the pin cannot be entered more than three times and
             // throw an Exception
 
             for (int i = 0; i < 3; i++) {
                 PinDialog dialog = new PinDialog();
                 SimpleTimeout timeout = new SimpleTimeout(60*1000) {
                     public void timeout()
                     {
                         mApplet.asyncException(new UserInputTimeoutError("User took too long to write pin"));
                     }
                 };
                 timeout.start();
                 try {
                     mPin = dialog.getPin();
                     timeout.finish();
                 } catch (IOException e) {
                     timeout.finish();
                     throw e;
                 }
 
                 timeout = new SimpleTimeout(60*1000) {
                     public void timeout()
                     {
                         mApplet.asyncException(new PinTimeoutError("Pin processing took too long in the card"));
                     }
                 };
                 timeout.start();
                 try {
                     mKeyStore.load(null, mPin.toCharArray());
                     timeout.finish();
                     System.out.println("Pin loaded");
                     return;
                 } catch (IOException e) {
                     timeout.finish();
                     // PIN failed trying again..
                     e.printStackTrace();
                     System.out.println("Pin not correctly loaded");
                     JOptionPane.showMessageDialog(VotingApplet.this,
                         "Incorrect PIN, please enter your PIN and try again",
                         "Error", JOptionPane.ERROR_MESSAGE);
                 }
             }
 
             // If after three tries PIN authentication failed, throw an exception
             throw new Exception("User's PIN auhentication failed");
         }
 
         public Object run() {
             String ballot = mBallot;
             String baseUrl = mBaseUrl;
 
             // The default value is ret, and if something goes wrong, it must be
             // "FAIL"
             String ret = "FAIL";
             try {
 
                 System.out.println("1. initialize the applet");
                 mBaseURLStr = baseUrl;
                 // 1. initialize the applet
                 mApplet.asyncUpdate("INIT_DNI", "Loading the DNIe certificate");
                 initialize();
 
                 // 2. Obtain the ballots
                 System.out.println("2. Obtain the ballots");
                 mApplet.asyncUpdate("FORGING_BALLOTS", "Creating and signing the ballots");
                 Vote[] votes = parseBallotString(ballot);
                 sign(votes);
 
                 // 3. Send the ballots to the agora server
                 System.out.println("3. Send the ballots to the agora server");
                 mApplet.asyncUpdate("SENDING_BALLOTS", "Sending the ballots to the server");
                 sendBallots(votes);
 
                 // 4. create return value
                 System.out.println("4. create return value");
                 String hashes = "";
                 for (int i = 0; i < votes.length; i++) {
                     hashes = hashes + votes[i].getHash() + ",";
                 }
                 // remove last ',' char at the end
                 hashes = hashes.substring(0, hashes.length() - 1);
 
                 ret = hashes;
             } catch (Exception e) {
                 e.printStackTrace();
                 mReturnValue = "FAIL";
                 mApplet.asyncException(e);
             } finally {
                 // close everything
                 mRandomSource = null;
                 mKeyStore = null;
                 mCertificate = null;
                 mPrivateKey = null;
                 mPin = null;
             }
             mReturnValue = ret;
             if (mReturnValue != "FAIL") {
                 mApplet.asyncUpdate("SUCCESS", mReturnValue);
             }
             return null;
         }
 
         /**
         * Joins the encrypted text of the votes and sign all of them together using
         * the dnie. This way the user is only asked once to sign the votes.
         */
         protected void sign(Vote []votes) throws Exception {
             SimpleTimeout timeout = new SimpleTimeout(60*1000) {
                 public void timeout()
                 {
                     mApplet.asyncException(new SignatureTimeoutError("call to sign() took too long"));
                 }
             };
             timeout.start();
             try {
                 Signature sig = Signature.getInstance("SHA256withRSA");
                 sig.initSign(mPrivateKey);
 
                 ByteArrayOutputStream concatenatedVotes = new ByteArrayOutputStream();
                 for (Vote vote : votes) {
                     concatenatedVotes.write(vote.getEncryptedVote().getBytes());
                 }
                 sig.update(concatenatedVotes.toByteArray());
                 mVotesSignature = encode(sig.sign());
                 // Not needed anymore
                 mPrivateKey = null;
                 timeout.finish();
             } catch (Exception e) {
                 timeout.finish();
                 throw e;
             }
         }
 
         /**
         * Sends the ballots to the agora server. Throws an exception if it fails.
         */
         protected void sendBallots(Vote []votes) throws Exception {
             SimpleTimeout timeout = new SimpleTimeout(60*1000) {
                 public void timeout()
                 {
                     mApplet.asyncException(new SendBallotsTimeoutError("call to sendBallots() took too long"));
                 }
             };
             timeout.start();
             try {
                 // Needs to send three things:
                 // 1. The DNIe certificate, serialized
                 // 2. For each vote, the encrypted vote
                 // 3. For each vote, the signature of the encrypted vote
 
                 // Serialize the certificate
                 String serializedCertificate = encode(mCertificate.getEncoded());
 
                 // 1. Generate the POST data
                 String data = URLEncoder.encode("dnie_certificate", "UTF-8") + "="
                             + URLEncoder.encode(serializedCertificate, "UTF-8");
                 data += "&" + URLEncoder.encode("votes_signature", "UTF-8") + "="
                         + URLEncoder.encode(mVotesSignature, "UTF-8");
                 for (Vote vote : votes) {
                     data += "&" + URLEncoder.encode("voting_id[]", "UTF-8") + "="
                         + URLEncoder.encode(vote.getProposal()+"", "UTF-8");
                     data += "&" + URLEncoder.encode("encrypted_vote[]", "UTF-8") + "="
                         + URLEncoder.encode(vote.getEncryptedVote(), "UTF-8");
                     data += "&" + URLEncoder.encode("a_factor[]", "UTF-8") + "="
                         + URLEncoder.encode(vote.getAFactor(), "UTF-8");
                     data += "&" + URLEncoder.encode("d_factor[]", "UTF-8") + "="
                         + URLEncoder.encode(vote.getDFactor(), "UTF-8");
                     data += "&" + URLEncoder.encode("u_factor[]", "UTF-8") + "="
                         + URLEncoder.encode(vote.getUFactor(), "UTF-8");
                 }
 
                 // 2. Send the request
                 System.out.println("Send the request");
                 URL sendBallotsURL = new URL(mBaseURLStr + sendBallotsURLStr);
                 HttpURLConnection con = (HttpURLConnection)sendBallotsURL.openConnection();
                 con.setDoOutput(true);
                 OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                 wr.write(data);
                 wr.flush();
 
                 // 3. Get the response
                 System.out.println("Get the response for data = " + data);
                 wr.close();
 
                 System.out.println("Process the response");
                 timeout.finish();
 
                 // 4. Process the response
                 if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                     return;
                 } else {
                     System.out.println("response code = '" + con.getResponseCode() + "' vs '" +
                         HttpURLConnection.HTTP_OK + "'");
                     throw new BallotCastingError("There was a problem casting the ballot");
                 }
             } catch (Exception e) {
                 timeout.finish();
                 throw e;
             }
         }
 
         /**
         * Converts the ballot string into an array of Vote class instances
         */
         private Vote[] parseBallotString(String ballot) throws Exception {
             String[] items = ballot.split(",");
             Vote[] votes = new Vote[items.length/3];
             for(int i = 0; i < items.length/3; i++) {
                 int vote = Integer.parseInt(items[i*3]);
                 int proposal = Integer.parseInt(items[i*3 + 1]);
                 String publicKeyString = items[i*3 + 2];
                 votes[i] = new Vote(vote, proposal, publicKeyString);
             }
             return votes;
         }
 
         /**
         * Contains a vote clear text information in the vote and proposal properties.
         */
         public class Vote {
             protected int mVote = -1;
             protected int mProposal = -1;
             protected PGroupElement mFullPublicKey = null;
             protected String mFullPublicKeyString = null;
             protected String mEncryptedVote = null;
             protected String mHash = null;
             protected String mAFactor = null;
             protected String mDFactor = null;
             protected String mUFactor = null;
 
             public Vote(int vote, int proposal, String publicKeyString) throws Exception {
                 System.out.println("creating vote for " + vote + " and proposal " + proposal);
                 mVote = vote;
                 mProposal = proposal;
                 mFullPublicKeyString = publicKeyString;
 
                 obtainPublicKey();
                 encrypt();
             }
 
             // Obtain the public key for this proposal/voting
             protected void obtainPublicKey() throws Exception {
                 mFullPublicKey = MixNetElGamalInterface.stringToPublicKey(
                     interfaceName, mFullPublicKeyString, mRandomSource, certainty);
             }
 
             protected void encrypt() throws Exception {
                 String plaintext = "" + mVote;
                 // Recover key from input
                 PGroupElement basicPublicKey =
                     ((PPGroupElement)mFullPublicKey).project(0);
                 PGroupElement publicKey =
                     ((PPGroupElement)mFullPublicKey).project(1);
 
                 PGroup basicPublicKeyPGroup = basicPublicKey.getPGroup();
                 PGroup publicKeyPGroup = publicKey.getPGroup();
 
                 // Get interface
                 MixNetElGamalInterface mixnetInterface =
                     MixNetElGamalInterface.getInterface(interfaceName);
 
                 // Generate plaintext
                 byte[] iBytes = plaintext.getBytes();
                 PGroupElement a_plaintext = publicKeyPGroup.encode(iBytes, 0, iBytes.length);
 
                 // Encrypt the result.
                 PRG prg = new PRGHeuristic(); // this uses SecureRandom internally
                 PRing randomizerPRing = basicPublicKeyPGroup.getPRing();
 
                 PRingElement r = randomizerPRing.randomElement(prg, 20);
 
                 PGroupElement u = basicPublicKey.exp(r);
                 PGroupElement v = publicKey.exp(r).mul(a_plaintext);
 
                 PGroupElement ciph =
                     ((PPGroup)mFullPublicKey.getPGroup()).product(u, v);
 
                 // set ciphertext using the format of the interface.
                 mEncryptedVote = mixnetInterface.ciphertextToString(ciph);
 
                 // Calculate hash and convert it to readable hex String
                 RandomOracle ro = new RandomOracle(
                     new HashfunctionHeuristic("SHA-256"), 2048);
                 String HEXES = "0123456789abcdef";
                 byte[] raw = ro.hash(mEncryptedVote.getBytes());
                 StringBuilder hex = new StringBuilder(2 * raw.length);
                 for (byte b : raw) {
                     hex.append(HEXES.charAt((b & 0xF0) >> 4))
                     .append(HEXES.charAt((b & 0x0F)));
                 }
                 mHash = hex.toString();
 
 
                 // Create a verifiable proof of knowledge of the cleartext
                 PRingElement s = randomizerPRing.randomElement(prg, 20);
                 PGroupElement a = basicPublicKey.exp(s);
                 // c = hash(prefix, g, u*v, a)
                 ByteTree cTree = new ByteTree(
                     new ByteTree(basicPublicKeyPGroup.toByteTree().toByteArray()),
                     new ByteTree(ciph.toByteTree().toByteArray()),
                     new ByteTree(a.toByteTree().toByteArray())
                 );
                 ro = new RandomOracle(new HashfunctionHeuristic("SHA-256"), 2048,
                     ByteTree.intToByteTree(mProposal));
                 byte[] cHash = ro.hash(cTree.toByteArray());
                 // d = cr+s
                 prg.setSeed(cHash);
                 PRingElement c = randomizerPRing.randomElement(prg, 20);
                 PRingElement d = c.mul(r).add(s);
 
                 mAFactor = encode(a.toByteTree().toByteArray());
                 mDFactor = encode(d.toByteTree().toByteArray());
                 mUFactor = encode(u.toByteTree().toByteArray());
             }
 
             public String getAFactor() {
                 return mAFactor;
             }
 
             public String getDFactor() {
                 return mDFactor;
             }
 
             public String getUFactor() {
                 return mUFactor;
             }
 
             public int getVote() {
                 return mVote;
             }
 
             public int getProposal() {
                 return mProposal;
             }
 
             public String getEncryptedVote() {
                 return mEncryptedVote;
             }
 
             public String getHash() {
                 return mHash;
             }
 
             public String toString() {
                 return "vote id = " + mVote + ", proposal id = " + mProposal;
             }
         }
 
         public class PinDialog extends JDialog implements ActionListener, KeyListener {
             protected boolean mSuccess = false;
             protected JButton mOkButton, mCancelButton = null;
             protected JPasswordField mPasswordField = null;
             protected String mPin = null;
 
             PinDialog() {
                 // Set the dialog owner, title and make it modal
                 super((Frame)null, "Enter PIN", true);
 
                 // Make the password field
                 mPasswordField = new JPasswordField(14);
                 mPasswordField.requestFocus();
                 final PinDialog thisPtr = this;
                 mPasswordField.addKeyListener(this);
 
                 // Make the text panel
                 JPanel panel = new JPanel();
                 panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                 panel.add(new JLabel("Please enter your DNI-e PIN to sign your vote(s):"));
                 panel.add(mPasswordField);
 
                 // Make the button panel
                 JPanel p = new JPanel();
                 p.setLayout(new FlowLayout());
                 p.add(mOkButton = new JButton("OK"));
                 mOkButton.addActionListener(this);
                 p.add(mCancelButton = new JButton("Cancel"));
                 mCancelButton.addActionListener(this);
                 p.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
 
                 //Put everything together, using the content pane's BorderLayout.
                 Container contentPane = getContentPane();
                 contentPane.add(panel, BorderLayout.CENTER);
                 contentPane.add(p, BorderLayout.PAGE_END);
                     pack();
 
                 setLocationRelativeTo(null);
 
                 // Make it visible
                 setVisible(true);
             }
 
             public void keyPressed(KeyEvent e) {}
             public void keyTyped(KeyEvent e) {}
 
             public void keyReleased(KeyEvent e) {
                 if (new String(mPasswordField.getPassword()).length() == 0) {
                     return;
                 }
                 int key = e.getKeyCode();
                 if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_INSERT) {
                     System.out.println("ENTER pressed");
                     enterPressed();
                 }
             }
 
             public void enterPressed() {
                 mPin = new String(mPasswordField.getPassword());
                 mPasswordField.setText("");
 
                 // Check that the pin is in valid format, i.e. 4 digits and
                 // nothing else
                 Pattern pattern = Pattern.compile("^.{8,16}$");
                 Matcher matcher = pattern.matcher(mPin);
                 if (!matcher.find()) {
                     mPin = null;
                     JOptionPane.showMessageDialog(PinDialog.this,
                     "Invalid PIN, please enter your PIN and try again",
                     "Error", JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 mSuccess = true;
                 setVisible(false);
             }
 
             public void actionPerformed(ActionEvent ae) {
                 if (ae.getSource() == mOkButton) {
                     enterPressed();
                 }
                 setVisible(false);
             }
 
             /**
             * Returns the PIN entered by the user, or throws an Exception if
             * user pressed Cancel.
             */
             public String getPin() throws Exception {
                 if(!mSuccess) {
                     throw new PinCancelledByUser("User Cancelled the PIN Dialog");
                 }
                 mPasswordField.setText("");
                 return mPin;
             }
         }
 
     }
 }
