 package org.agora;
 
 import java.util.Arrays;
 import java.util.Random; 
 import java.applet.Applet;
 import java.io.BufferedReader;
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
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
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
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.Provider;
 import java.security.Security;
 import java.security.Signature;
 import java.security.MessageDigest;
 import java.util.Enumeration;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 
 import verificatum.eio.ByteTree;
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
     protected static final String interfaceName = "native";
     protected static final String publicKeyURLStr = "/votings/";
     protected static final String sendBallotsURLStr = "/votes";
     protected static final int certainty = 100;
     protected static final String confLinux=
         "name=OpenSC-OpenDNIe\nlibrary=/usr/lib/opensc-pkcs11.so\n";
     protected static final String confWindows=
         "name=OpenSC-OpenDNIe\r\nlibrary=C:\\WINDOWS\\system32\\opensc-pkcs11.dll\r\n";
     protected static final String confMac=
         "name=OpenSC-OpenDNIe\nlibrary=/usr/local/lib/opensc-pkcs11.so\n";
     protected static final String certAlias="CertFirmaDigital";
 
     protected RandomSource mRandomSource = null;
 
     protected KeyStore mKeyStore = null;
     protected Certificate mCertificate = null;
     protected PrivateKey mPrivateKey = null;
     protected String mPin = null;
     protected String mBaseURLStr = null;
     protected String mVotesSignature = null;
 
     static String encode(byte[] bytes) throws Exception {
         byte[] encoded = Base64.encodeBase64(bytes);
         return new String(encoded, "ASCII");
     }
 
     static byte[] decode(String str) throws Exception {
         byte[] bytes = str.getBytes("ASCII");
         return Base64.decodeBase64(bytes);
     }
 
     /**
      * Initialize the applet.
      */
     protected void initialize() throws Exception {
         mRandomSource = new PRGHeuristic();
 
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
 
         Provider provider = new sun.security.pkcs11.SunPKCS11(
             new ByteArrayInputStream(config.getBytes()));
         Security.addProvider(provider);
 
         // Create the keyStore and initialize it with the PIN
         mKeyStore = KeyStore.getInstance("PKCS11", provider);
 
         obtainPin();
 
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
             throw new Exception("Signature certificate not found");
         }
         String subject = ((X509Certificate)mCertificate).getSubjectX500Principal().toString();
         System.out.println("certsubject = '" + subject + "'");
 
 
         String serializedCertificate = encode(mCertificate.getEncoded());
         CertificateFactory factory = CertificateFactory.getInstance("X.509");
         X509Certificate certificate = (X509Certificate)factory.generateCertificate(
             new ByteArrayInputStream(decode(serializedCertificate)));
 
         Key key = mKeyStore.getKey(certAlias, mPin.toCharArray());
         if(!(key instanceof PrivateKey)) {
             throw new Exception("The certificate has no associated private key");
         }
         mPrivateKey = (PrivateKey)key;
     }
 
     /**
      * Asks the user for the dni-e pin and load the keystore.
      */
     protected void obtainPin() throws Exception {
         // ask for the user PIN three times at most, then show a dialog
         // saying the pin cannot be entered more than three times and
         // throw an Exception
 
         for (int i = 0; i < 3; i++) {
//             PinDialog dialog = new PinDialog();
//             mPin = dialog.getPin();
            mPin = "Qwtt4f=0";
             try {
                 mKeyStore.load(null, mPin.toCharArray());
                 return;
             } catch (Exception e) {
                 // PIN failed trying again..
                 JOptionPane.showMessageDialog(VotingApplet.this,
                     "Incorrect PIN, please enter your PIN and try again",
                     "Error", JOptionPane.ERROR_MESSAGE);
             }
         }
 
         // If after three tries PIN authentication failed, throw an exception
         throw new Exception("User's PIN auhentication failed");
     }
 
     /**
      * Processes and cast a vote.
      *
      * @param ballot ballot string. The format should be:
      *              "<vote 1 id>,<propossal 1 id>,[<vote n id>,<propossal n id>, ...]"
      * @param baseUrl base url to use for the web server, for example
      *                https://localhost:8080 (with no ending slash character)
      * 
      * @return a list of comma-separated hashes of the votes if the votes were
      *         correctly casted, or "FAIL" otherwise.
      */
     public String vote(String ballot, String baseUrl) {
         String ret = null;
         try {
             System.out.println("1. initialize the applet");
             mBaseURLStr = baseUrl;
             // 1. initialize the applet
             initialize();
 
             // 2. Obtain the ballots
             System.out.println("2. Obtain the ballots");
             Vote[] votes = parseBallotString(ballot);
 
             sign(votes);
 
             // 3. Send the ballots to the agora server
             System.out.println("3. Send the ballots to the agora server");
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
             ret = "FAIL";
         } finally {
             // close everything
             mRandomSource = null;
             mKeyStore = null;
             mCertificate = null;
             mPrivateKey = null;
             mPin = null;
         }
         return ret;
     }
 
     /**
      * Joins the encrypted text of the votes and sign all of them together using
      * the dnie. This way the user is only asked once to sign the votes.
      */
     protected void sign(Vote []votes) throws Exception {
         Signature sig = Signature.getInstance("SHA1withRSA");
         sig.initSign(mPrivateKey);
         ByteArrayOutputStream concatenatedVotes = new ByteArrayOutputStream();
         for (Vote vote : votes) {
             concatenatedVotes.write(vote.getEncryptedVote().getBytes());
         }
         sig.update(concatenatedVotes.toByteArray());
         mVotesSignature = encode(sig.sign());
 
         Signature sig2 = Signature.getInstance("SHA1withRSA");
         CertificateFactory factory = CertificateFactory.getInstance("X.509");
         X509Certificate  certificate = (X509Certificate)factory.generateCertificate(
             new ByteArrayInputStream(mCertificate.getEncoded()));
         sig2.initVerify(mCertificate.getPublicKey());
         sig2.initVerify(certificate.getPublicKey());
     }
 
     /**
      * Sends the ballots to the agora server. Throws an exception if it fails.
      */
     protected void sendBallots(Vote []votes) throws Exception {
         // Needs to send three things:
         // 1. The DNIe certificate, serialized
         // 2. For each vote, the encrypted vote
         // 3. For each vote, the signature of the encrypted vote
 
         // Serialize the certificate
         String serializedCertificate = encode(mCertificate.getEncoded());
 
         // 1. Generate the POST data
        String data = URLEncoder.encode("dnie_certificate" + "a", "UTF-8") + "="
                     + URLEncoder.encode(serializedCertificate, "UTF-8");
         data += "&" + URLEncoder.encode("votes_signature", "UTF-8") + "="
                 + URLEncoder.encode(mVotesSignature, "UTF-8");
         for (Vote vote : votes) {
             data += "&" + URLEncoder.encode("voting_id[]", "UTF-8") + "="
                  + URLEncoder.encode(vote.getPropossal()+"", "UTF-8");
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
         // 4. Process the response
         if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
             return;
         } else {
             System.out.println("response code = '" + con.getResponseCode() + "' vs '" +
                 HttpURLConnection.HTTP_OK + "'");
             throw new Exception("There was a problem casting the ballot");
         }
     }
 
     /**
      * Converts the ballot string into an array of Vote class instances
      */
     private Vote[] parseBallotString(String ballot) throws Exception {
         String[] items = ballot.split(",");
         Vote[] votes = new Vote[items.length/2];
         for(int i = 0; i < items.length/2; i++) {
             int vote = Integer.parseInt(items[i*2]);
             int propossal = Integer.parseInt(items[i*2 + 1]);
             votes[i] = new Vote(vote, propossal);
         }
         return votes;
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
         String ballotStr = "0,1,01,1";
         String hashes = applet.vote(ballotStr, args[0]);
         System.out.println("vote result = " + hashes);
         System.exit(hashes != "FAIL" ? 0 : 1);
         return;
     }
 
     /**
      * Contains a vote clear text information in the vote and propossal properties.
      */
     public class Vote {
         protected int mVote = -1;
         protected int mPropossal = -1;
         protected PGroupElement mFullPublicKey = null;
         protected String mFullPublicKeyString = null;
         protected String mEncryptedVote = null;
         protected String mHash = null;
         protected String mAFactor = null;
         protected String mDFactor = null;
         protected String mUFactor = null;
 
         public Vote(int vote, int propossal) throws Exception {
             System.out.println("creating vote for " + vote + " and propossal " + propossal);
             mVote = vote;
             mPropossal = propossal;
 
             obtainPublicKey();
             encrypt();
         }
 
         // Obtain the public key for this propossal/voting
         protected void obtainPublicKey() throws Exception {
             URL publicKeyURL = new URL(mBaseURLStr + publicKeyURLStr + "" + mPropossal);
             HttpURLConnection con = (HttpURLConnection)publicKeyURL.openConnection();
             // 3. Get the response
             BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
             String publicKeyString = "", line;
             while ((line = rd.readLine()) != null) {
                 publicKeyString += line;
             }
             rd.close();
             System.out.println("publicKeyString = '" + publicKeyString + "'");
             mFullPublicKeyString = publicKeyString;
             mFullPublicKey = MixNetElGamalInterface.stringToPublicKey(
                 interfaceName, publicKeyString, mRandomSource, certainty);
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
             PGroupElement[] m_a = new PGroupElement[1];
             byte[] iBytes = plaintext.getBytes();
             m_a[0] = publicKeyPGroup.encode(iBytes, 0, iBytes.length);
 
             // Encode plaintexts as group elements.
             PGroupElementArray m = publicKeyPGroup.toElementArray(m_a);
 
             // Encrypt the result.
             PRG prg = new PRGHeuristic();
 
             Random random = new Random();
             byte[] seed = new byte[200];
             random.nextBytes(seed);
             prg.setSeed(seed);
             PRing randomizerPRing = basicPublicKeyPGroup.getPRing();
 
             PRingElementArray r =
                 randomizerPRing.randomElementArray(1, prg, 20);
 
             PGroupElementArray u = basicPublicKey.exp(r);
             PGroupElementArray v = publicKey.exp(r).mul(m);
 
             PGroupElementArray ciphs =
                 ((PPGroup)mFullPublicKey.getPGroup()).product(u, v);
 
             PGroupElement[] ciphElements = ciphs.elements();
 
             // set ciphertext using the format of the interface.
             mEncryptedVote = mixnetInterface.ciphertextToString(ciphElements[0]);
 
             // Calculate hash and convert it to readable hex String
             MessageDigest sha1 = MessageDigest.getInstance("SHA1");
             String HEXES = "0123456789abcdef";
             byte[] raw = sha1.digest(mEncryptedVote.getBytes());
             StringBuilder hex = new StringBuilder(2 * raw.length);
             for (byte b : raw) {
                 hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
             }
             mHash = hex.toString();
 
 
             // Create a verifiable proof of knowledge of the cleartext
             PRingElementArray s =
                 randomizerPRing.randomElementArray(1, prg, 20);
             PGroupElementArray a = basicPublicKey.exp(s);
             // c = hash(prefix, g, u*v, a)
             String prefix =  Integer.toString(mPropossal);
             String g = mFullPublicKeyString;
             String uv = mEncryptedVote;
             String aStr = encode(a.get(0).toByteTree().toByteArray());
             String factorsStr = prefix + g + uv + aStr;
             byte[] cHash = sha1.digest(factorsStr.getBytes());
             // d = cr+s
             prg.setSeed(cHash);
             PRingElement c =
                 randomizerPRing.randomElementArray(1, prg, 20).get(0);
             PRingElement d = c.mul(r.get(0)).add(s.get(0));
 
             mAFactor = aStr;
            mDFactor = encode(d.toByteTree().toByteArray());
             mUFactor = encode(u.get(0).toByteTree().toByteArray());
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
 
         public int getPropossal() {
             return mPropossal;
         }
 
         public String getEncryptedVote() {
             return mEncryptedVote;
         }
 
         public String getHash() {
             return mHash;
         }
 
         public String toString() {
             return "vote id = " + mVote + ", propossal id = " + mPropossal;
         }
     }
 
     public class PinDialog extends JDialog implements ActionListener {
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
 
         public void actionPerformed(ActionEvent ae) {
             if (ae.getSource() == mOkButton) {
                 mPin = new String(mPasswordField.getPassword());
 
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
             } else {
                 JOptionPane.showMessageDialog(PinDialog.this, 
                     "You cancelled to write the DNI-e PIN, voting cancelled",
                     "Cancelled", JOptionPane.ERROR_MESSAGE);
             }
             setVisible(false);
         }
 
         /**
          * Returns the PIN entered by theuser, or throws an Exception if
          * user pressed Cancel.
          */
         public String getPin() throws Exception {
             if(!mSuccess) {
                 throw new Exception("User Cancelled the PIN Dialog");
             }
             mPasswordField.setText("");
             return mPin;
         }
     }
 
 }
