 package nz.ac.auckland.netlogin.negotiation;
 
 import nz.ac.auckland.cs.des.C_Block;
 import nz.ac.auckland.netlogin.NetLoginPreferences;
 import javax.security.auth.login.LoginException;
 import java.io.*;
 import java.util.Arrays;
 import java.util.List;
 
 public abstract class AbstractGSSAuthenticator implements Authenticator {
 
     public static final byte[] MAGIC_GSS = "gss:".getBytes();
     public static final byte[] MAGIC_PAYLOAD = "msg:".getBytes();
    public static final List<String> allowedRealms = Arrays.asList("UOA.AUCKLAND.AC.NZ", "UOATEST.AUCKLAND.AC.NZ", "AD.EC.AUCKLAND.AC.NZ");
 
     protected abstract String getUserName() throws LoginException;
 
     protected abstract void initializeContext() throws LoginException;
 
     protected abstract byte[] initSecContext(byte[] gssToken) throws LoginException;
 
     protected abstract byte[] unwrap(byte[] wrapper) throws LoginException;
 
     public static String getServicePrincipalName() {
         String serverName = NetLoginPreferences.getInstance().getServer();
         String realmName = NetLoginPreferences.getInstance().getRealm();
         return "netlogin/" + serverName + "@" + realmName;
     }
 
     public AuthenticationRequest startAuthentication(CredentialsCallback callback) throws LoginException, IOException {
         cleanup();
 
         initializeContext();
         byte[] outToken = initSecContext(null);
         if (outToken == null) throw new LoginException("No authentication token produced");
 
         ByteArrayOutputStream outStream = new ByteArrayOutputStream(outToken.length + 4);
         outStream.write(MAGIC_GSS);
         outStream.write(outToken);
 
         String userPrincipal = getUserName();
         String[] userPrincipalParts = userPrincipal.split("@", 2);
 		String userName = userPrincipalParts[0];
         String realm = userPrincipalParts[1].toUpperCase();
 
        if (!allowedRealms.contains(realm)) throw new LoginException("Realm not supported for authentication");
 
         return new AuthenticationRequest(userName, outStream.toByteArray());
     }
 
     public LoginComplete validateResponse(byte[] serverResponse) throws LoginException, IOException {
         DataInputStream in = new DataInputStream(new ByteArrayInputStream(serverResponse));
 
         byte[] magic = new byte[MAGIC_GSS.length];
         in.readFully(magic);
         if (!Arrays.equals(magic, MAGIC_GSS)) throw new IOException("Malformed packet");
 
         int gssTokenSize = in.readInt();
         if (gssTokenSize > 1024) throw new IOException("Malformed packet");
         // System.err.println("GSS token is " + gssTokenSize + " bytes");
         byte[] gssToken = new byte[gssTokenSize];
         in.readFully(gssToken);
 
         int payloadWrappedSize = in.readInt();
         if (payloadWrappedSize > 1024) throw new IOException("Malformed packet");
         // System.err.println("Wrapped payload is " + payloadWrappedSize + " bytes");
         byte[] payloadWrapped = new byte[payloadWrappedSize];
         in.readFully(payloadWrapped);
 
         if (gssToken.length != 0) initSecContext(gssToken);
         byte[] payload = unwrap(payloadWrapped);
         // System.err.println("Payload is " + payload.length + " bytes");
 
         DataInputStream payloadIn = new DataInputStream(new ByteArrayInputStream(payload));
         int serverNonce = payloadIn.readInt();
         int sessionKeySize = payloadIn.readInt();
         if (sessionKeySize > 1024) throw new IOException("Malformed packet");
         byte[] sessionKey = new byte[sessionKeySize];
         payloadIn.readFully(sessionKey);
 
 		cleanup();
 
         return new LoginComplete(serverNonce, new C_Block(sessionKey));
     }
 
     protected void cleanup() {
         // do nothing by default
     }
 
     protected void finalize() throws Throwable {
         cleanup();
         super.finalize();
     }
 
 }
