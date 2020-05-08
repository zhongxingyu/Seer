 package org.irmacard.personalisation;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.smartcardio.CardException;
 import javax.smartcardio.CardTerminal;
 import javax.smartcardio.TerminalFactory;
 
 import org.irmacard.credentials.Attributes;
 import org.irmacard.credentials.CredentialsException;
 import org.irmacard.credentials.idemix.IdemixCredentials;
 import org.irmacard.credentials.idemix.IdemixPrivateKey;
 import org.irmacard.credentials.idemix.spec.IdemixIssueSpecification;
 
 import com.ibm.zurich.credsystem.utils.Locations;
 
 
 import service.IdemixService;
 
 import net.sourceforge.scuba.smartcards.CardService;
 import net.sourceforge.scuba.smartcards.CardServiceException;
 import net.sourceforge.scuba.smartcards.TerminalCardService;
 
 public class IrmaIssuer {
 	/** The identifier of the credential on the smartcard
 	 * TODO change?*/
     public static short CRED_NR = (short) 1;
     
     /** Actual location of the files. */
 	/** TODO: keep this in mind, do we need BASE_LOCATION to point to .../parameter/
 	 *  to keep idemix-library happy, i.e. so that it can find gp.xml and sp.xml?
 	 */
     public static final URI BASE_LOCATION = new File(
             System.getProperty("user.dir")).toURI().resolve("irma_configuration/Surfnet/");
     
     /** Actual location of the public issuer-related files. */
     public static final URI ISSUER_LOCATION = BASE_LOCATION;
     
     /** URIs and locations for issuer */
     public static final URI ISSUER_SK_LOCATION = BASE_LOCATION.resolve("private/isk.xml");
     public static final URI ISSUER_PK_LOCATION = ISSUER_LOCATION.resolve("ipk.xml");
     
     /** Credential location */
     public static final String CRED_STRUCT_NAME = "root";
     public static final URI CRED_STRUCT_LOCATION = BASE_LOCATION
             .resolve( "Issues/" + CRED_STRUCT_NAME + "/structure.xml");
 
 	private static final byte[] DEFAULT_PIN = {0x30, 0x30, 0x30, 0x30};
     
     /** Ids used within the test files to identify the elements. */
     public static URI BASE_ID = null;
     public static URI ISSUER_ID = null;
     public static URI CRED_STRUCT_ID = null;
 
 	private IdemixService idemixService;
     static {
         try {
            BASE_ID = new URI("http://www.irmacard.org/credentials/phase1/Surfnet/");
            ISSUER_ID = new URI("http://www.irmacard.org/credentials/phase1/Surfnet/");
            CRED_STRUCT_ID = new URI("http://www.irmacard.org/credentials/phase1/Surfnet/" + CRED_STRUCT_NAME + "/structure.xml");
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
     }
 	
 	public IrmaIssuer() throws CardException{
 		CardService cs = getCardService();
 		
 		idemixService = new IdemixService(cs, CRED_NR);
 		
 		try {
 			idemixService.open();
 			idemixService.generateMasterSecret();
 			idemixService.close();
 		} catch (CardServiceException e) {
 			Logger.log("Setting master secret failed, this is normal except for the first run.");
 		}
 		
 		setupSystem();
 		setupCredentialStructure();
 	}
 
 	private CardService getCardService() throws CardException {
 		CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);
 		return new TerminalCardService(terminal);
 	}
 	
 	public static void setupSystem() {
         Locations.initSystem(BASE_LOCATION, BASE_ID.toString());
 
         // loading issuer public key
         Locations.init(ISSUER_ID.resolve("ipk.xml"), ISSUER_LOCATION.resolve("ipk.xml"));
     }
 	
 	public static void setupCredentialStructure() {
     	Locations.init(CRED_STRUCT_ID, CRED_STRUCT_LOCATION);
     }
 
 	public void issue() throws CardException, CredentialsException, CardServiceException {
 		issue(getIssuanceAttributes());
 	}
 
 	public void issue(Attributes attributes) throws CardException, CredentialsException, CardServiceException {
 		
 		idemixService.open();
 		
 		IdemixIssueSpecification spec = IdemixIssueSpecification
 				.fromIdemixIssuanceSpec(
 						ISSUER_PK_LOCATION,
 						CRED_STRUCT_ID,
 						(short) (CRED_NR));
 
 		IdemixPrivateKey isk = IdemixPrivateKey.fromIdemixPrivateKey(ISSUER_SK_LOCATION);
 
 		CardService cs = getCardService();
 		IdemixCredentials ic = new IdemixCredentials(cs);
 		
 		ic.issue(spec, isk, attributes, null);
 		
 		idemixService.close();
 	}
 	
 	private Attributes getIssuanceAttributes() {
         // Return the attributes that have been revealed during the proof
         Attributes attributes = new Attributes();
 
         attributes.add("attr1", BigInteger.valueOf(1313).toByteArray());
         attributes.add("attr2", BigInteger.valueOf(1314).toByteArray());
         attributes.add("attr3", BigInteger.valueOf(1315).toByteArray());
         attributes.add("attr4", BigInteger.valueOf(1316).toByteArray());
         
         return attributes;
     }
 	
 	public void setPin(byte[] pin) throws CardServiceException {
 		idemixService.open();
 		
 		idemixService.sendPin(DEFAULT_PIN);
 		idemixService.updatePin(pin);
 		
 		idemixService.close();
 	}
 }
