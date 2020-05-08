 package com.cyanoryx.uni.enigma.net.server;
 
 import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
 import java.util.prefs.Preferences;
import java.util.zip.DataFormatException;
 
 import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
 
 import com.cyanoryx.uni.common.Base64;
 import com.cyanoryx.uni.crypto.aes.Key;
 import com.cyanoryx.uni.crypto.aes.KeySize;
 import com.cyanoryx.uni.crypto.cert.Certificate;
 import com.cyanoryx.uni.crypto.cert.CertificateAuthority;
 import com.cyanoryx.uni.crypto.cert.KeyAlgorithm;
 import com.cyanoryx.uni.crypto.rsa.PrivateKey;
 import com.cyanoryx.uni.crypto.rsa.PublicKey;
 import com.cyanoryx.uni.crypto.rsa.RSA_OAEP;
 import com.cyanoryx.uni.enigma.net.protocol.Session;
 import com.cyanoryx.uni.enigma.net.protocol.xml.Packet;
 import com.cyanoryx.uni.enigma.net.protocol.xml.PacketListener;
 import com.cyanoryx.uni.enigma.utils.AppPrefs;
 
 public class AuthHandler implements PacketListener {
 	private SessionIndex index;
 	
 	public AuthHandler(SessionIndex i) {
 		this.index=i;
 	}
 	
 	@Override
 	public void notify(Packet packet) {
 		Preferences p = new AppPrefs().getPrefs();
 		
 		Session stored_session = index.getSession(packet.getAttribute("id"));
 		if (stored_session==null) return;
 		
 		try {
 			String stage = packet.getAttribute("stage");
 			String type = packet.getType();
 			
 			if (stage.equalsIgnoreCase("agreement")) {
 				// FOR: <auth stage="agreement" type="method">method of key agreement</auth>
 				if (type.equalsIgnoreCase("method")) {
 					String agreement_method = packet.getChildValue("method");
 					
 					if (agreement_method.equalsIgnoreCase("RSA")) stored_session.setAgreementType(KeyAlgorithm.RSA);
 				} else if (type.equalsIgnoreCase("cert")) {
 					// FOR: <auth stage="agreement" type="cert">Base64 encoded Enigma certificate</auth>
 					if (stored_session.getAgreementType()==KeyAlgorithm.RSA) {
 						
 						Certificate cert = new Certificate(new String(Base64.decode(packet.getChildValue("cert"))));
 						CertificateAuthority ca = new CertificateAuthority(new PublicKey(new File(new AppPrefs().getPrefs().get("ca_key_location","./cert_id_rsa.pub"))));
 						
 						stored_session.setPublicKey(new PublicKey(new String(cert.getSubject_key())));
 						
 						System.out.println(ca.verify(cert));
 						
 						RSA_OAEP rsa = new RSA_OAEP(stored_session.getPublicKey());
 						
 						byte[] key_bytes = new Key(KeySize.K256).getKey();
 						String key 		 = Base64.encodeBytes(rsa.encrypt(key_bytes));
 						
 						stored_session.setStatus(Session.AUTHENTICATED);
 						
 						stored_session.sendAuth("key", "agreement", key, stored_session.getID());
 						stored_session.setCipherKey(key_bytes);
 					}
 				} else if (type.equalsIgnoreCase("key")) {
 					// FOR: <auth stage="agreement" type="key">Base64 encoded key</auth>
 					if (stored_session.getAgreementType()==KeyAlgorithm.RSA) {
 						byte[] key = Base64.decode(packet.getChildValue("key"));
 						
 						RSA_OAEP rsa = new RSA_OAEP(new PrivateKey(new File(new AppPrefs().getPrefs().get("priv_key_location","./id_rsa"))));
 						key = rsa.decrypt(key);
 						
 						stored_session.setCipherKey(key);
 						stored_session.setStatus(Session.AUTHENTICATED);
 					}
 				}
 			} else if (stage.equalsIgnoreCase("streaming")) {
 				// FOR: <auth stage="streaming" type="toggle">turn encryption on/off</auth>
 				if (type.equalsIgnoreCase("toggle")) {
 					if (packet.getChildValue("toggle").equalsIgnoreCase("off")) {
 						if (!p.getBoolean("allow_unauthenticated_conversations", false)) {
 							// If the user's preferences don't allow unauth'd convos, ask if they want to continue
 							int result = JOptionPane.showConfirmDialog(stored_session.getWindow().getFrame().getContentPane(), "The remote user is asking to turn off encryption,\nhowever your preferences do not allow this. Continue?", "Error", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
 							
 							// If not, close the session
 							if (result!=0) stored_session.closeStream();
 						}
 						stored_session.setAuthenticated(false);
 						stored_session.setStatus(Session.STREAMING);
 						stored_session.getWindow().update(stored_session.getUser().getName()+" turned off encryption...");
 					} else {
 						stored_session.setAuthenticated(true);
 						stored_session.getWindow().update(stored_session.getUser().getName()+" turned on encryption...");
 					}
 				}
 			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DataFormatException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
