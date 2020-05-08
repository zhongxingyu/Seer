 /**
  * IdemixService.java
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright (C) Pim Vullers, Radboud University Nijmegen, June 2011.
  */
 
 package service;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import net.sourceforge.scuba.smartcards.CommandAPDU;
 import net.sourceforge.scuba.smartcards.IResponseAPDU;
 import net.sourceforge.scuba.smartcards.ISO7816;
 
 import com.ibm.zurich.idmx.dm.Values;
 import com.ibm.zurich.idmx.dm.structure.AttributeStructure;
 import com.ibm.zurich.idmx.dm.structure.CredentialStructure;
 import com.ibm.zurich.idmx.issuance.IssuanceSpec;
 import com.ibm.zurich.idmx.issuance.Message;
 import com.ibm.zurich.idmx.issuance.Message.IssuanceProtocolValues;
 import com.ibm.zurich.idmx.key.IssuerPublicKey;
 import com.ibm.zurich.idmx.showproof.Identifier;
 import com.ibm.zurich.idmx.showproof.Proof;
 import com.ibm.zurich.idmx.showproof.ProofSpec;
 import com.ibm.zurich.idmx.showproof.predicates.CLPredicate;
 import com.ibm.zurich.idmx.showproof.predicates.Predicate;
 import com.ibm.zurich.idmx.showproof.predicates.Predicate.PredicateType;
 import com.ibm.zurich.idmx.showproof.sval.SValue;
 import com.ibm.zurich.idmx.showproof.sval.SValuesProveCL;
 import com.ibm.zurich.idmx.utils.StructureStore;
 import com.ibm.zurich.idmx.utils.SystemParameters;
 
 /**
  * Idemix Smart Card Interface based on a SCUBA Card Service.
  *  
  * @author Pim Vullers
  * @version $Revision: 554 $ by $Author: pim $
  *          $LastChangedDate: 2011-04-28 16:31:47 +0200 (Thu, 28 Apr 2011) $
  */
 public class IdemixSmartcard {
     /**
      * Universal version identifier to match versions during deserialisation.
      */
     private static final long serialVersionUID = -6317383635196413L;
 
     /**
      * AID of the Idemix applet: ASCII encoding of "idemix".
      */
     private static final byte[] AID = {0x69, 0x64, 0x65, 0x6D, 0x69, 0x78};
 
     /**
      * INStruction to select an applet.
      */
     private static final byte INS_SELECT = (byte) 0xA4;
 
     /**
      * P1 parameter for select by name.
      */
     private static final byte P1_SELECT = 0x04;
 
     /**
      * CLAss to be used for Idemix APDUs.
      */
     private static final byte CLA_IDEMIX = (byte) 0x80;
 
     /**
      * INStruction to select a credential on the card.
      */
     @SuppressWarnings("unused")
 	private static final byte INS_SELECT_CREDENTIAL = 0x00;
     
     /**
      * INStruction to generate the master secret on the card.
      */
     private static final byte INS_GENERATE_SECRET = 0x01;
 
     /**
      * INStruction to start issuing a credential (and to set the corresponding
      * context).
      */
     private static final byte INS_ISSUE_CREDENTIAL = 0x10;
 
     /**
      * INStruction to issue the n value from the issuer public key. 
      */
     private static final byte INS_ISSUE_PUBLIC_KEY_N = 0x11;
 
     /**
      * INStruction to issue the z value from the issuer public key. 
      */
     private static final byte INS_ISSUE_PUBLIC_KEY_Z = 0x12;
     
     /**
      * INStruction to issue the s value from the issuer public key. 
      */
     private static final byte INS_ISSUE_PUBLIC_KEY_S = 0x13;
     
     /**
      * INStruction to issue the R values from the issuer public key. 
      */
     private static final byte INS_ISSUE_PUBLIC_KEY_R = 0x14;
     
     /**
      * INStruction to issue the attributes. 
      */
     private static final byte INS_ISSUE_ATTRIBUTES = 0x15;
 
     /**
      * INStruction to send the first nonce (n_1) and compute/receive the 
      * combined hidden attributes (U).
      */
     private static final byte INS_ISSUE_NONCE_1 = 0x16;
 
     /**
      * INStruction to receive the zero-knowledge proof for correct construction 
      * of U (c, v^', s_A). 
      */
     private static final byte INS_ISSUE_PROOF_U = 0x17;
 
     /**
      * INStruction to receive the second nonce (n_2). 
      */
     private static final byte INS_ISSUE_NONCE_2 = 0x18;
 
     /**
      * INStruction to send the blind signature (A, e, v''). 
      */
     private static final byte INS_ISSUE_SIGNATURE = 0x19;
 
     /**
      * INStruction to send the zero-knowledge proof for correct construction of 
      * the signature (s_e, c'). 
      */
     private static final byte INS_ISSUE_PROOF_A = 0x1A;
 
     /**
      * INStruction to start proving aattributes from a credential (and to set 
      * the corresponding context).
      */
     private static final byte INS_PROVE_CREDENTIAL = 0x20;
 
     /**
      * INStruction to send the list of attributes to be disclosed.
      */
     private static final byte INS_PROVE_SELECTION = 0x21;
 
     /**
      * INStruction to send the challenge (m) to be signed in the proof and 
      * receive the commitment for the proof (a). 
      */
     private static final byte INS_PROVE_NONCE = 0x22;
 
     /**
      * INStruction to receive the values A', e^ and v^. 
      */
     private static final byte INS_PROVE_SIGNATURE = 0x23;
 
     /**
      * INStruction to receive the disclosed attributes (A_i).
      */
     private static final byte INS_PROVE_ATTRIBUTE = 0x24;
 
     /**
      * INStruction to receive the values for the undisclosed attributes (r_i). 
      */
     private static final byte INS_PROVE_RESPONSE = 0x25;
 
     /**
      * P1 parameter for the PROOF_U data instructions. 
      */
     private static final byte P1_PROOF_U_C = 0x00;
     private static final byte P1_PROOF_U_VPRIMEHAT = 0x01;
     private static final byte P1_PROOF_U_S_A = 0x02;
 
     /**
      * P1 parameters for SIGNATURE data instructions.
      */
     private static final byte P1_SIGNATURE_A = 0x00;
     private static final byte P1_SIGNATURE_E = 0x01;
     private static final byte P1_SIGNATURE_V = 0x02;
     private static final byte P1_SIGNATURE_VERIFY = 0x03;
 
     /**
      * P1 parameters for PROOF_A data instructions. 
      */
     private static final byte P1_PROOF_A_C = 0x00;
     private static final byte P1_PROOF_A_S_E = 0x01;
     private static final byte P1_PROOF_A_VERIFY = 0x02;
     
 
     /**
      * Produces an unsigned byte-array representation of a BigInteger.
      *
      * <p>BigInteger adds an extra sign bit to the beginning of its byte
      * array representation.  In some cases this will cause the size
      * of the byte array to increase, which may be unacceptable for some
      * applications. This function returns a minimal byte array representing
      * the BigInteger without extra sign bits.
      *
      * <p>This method is taken from the Network Security Services for Java (JSS)
      * currently maintained by the Mozilla Foundation and originally developed 
      * by the Netscape Communications Corporation.
      * 
      * @return unsigned big-endian byte array representation of a BigInteger.
      */
     public static byte[] BigIntegerToUnsignedByteArray(BigInteger big) {
         byte[] ret;
 
         // big must not be negative
         assert(big.signum() != -1);
 
         // bitLength is the size of the data without the sign bit.  If
         // it exactly fills an integral number of bytes, that means a whole
         // new byte will have to be added to accommodate the sign bit. In
         // this case we need to remove the first byte.
         if(big.bitLength() % 8 == 0) {
             byte[] array = big.toByteArray();
             // The first byte should just be sign bits
             assert( array[0] == 0 );
             ret = new byte[array.length-1];
             System.arraycopy(array, 1, ret, 0, ret.length);
         } else {
             ret = big.toByteArray();
         }
         return ret;
     }
     
     /**
      * Fix the length of array representation of BigIntegers put into the APDUs.
      * 
      * @param integer of which the length needs to be fixed.
      * @param the new length of the integer in bits
      * @return an array with a fixed length.
      */
     public static byte[] fixLength(BigInteger integer, int length_in_bits) {
         byte[] array = BigIntegerToUnsignedByteArray(integer);
         int length;
         
         length = length_in_bits/8;
         if (length_in_bits % 8 != 0){
         	length++;
         }
         
         assert (array.length <= length);
         
         int padding = length - array.length;
         byte[] fixed = new byte[length];
         Arrays.fill(fixed, (byte) 0x00);
         System.arraycopy(array, 0, fixed, padding, array.length);
         return fixed;
     }
 
     /**************************************************************************/
     /* Idemix Smart Card commands                                           */
     /**************************************************************************/
 
     public static ProtocolCommand selectAppletCommand =
     		new ProtocolCommand(
     				"selectapplet", 
     				"Select idemix applet",
     				 new CommandAPDU(ISO7816.CLA_ISO7816,  
     			                INS_SELECT, P1_SELECT, 0x00, AID, 256)); // LE == 0 is required.
 
     
     /**
      * Get the APDU commands for setting the specification of
      * a certificate issuance:
      * <ul>
      *   <li> issuer public key, and 
      *   <li> context. 
      * </ul>
      * 
      * @param spec the specification to be set
      * @param id
      * @return
      */
     public static ArrayList<ProtocolCommand> setIssuanceSpecificationCommands(IssuanceSpec spec, short id) {
     	ArrayList<ProtocolCommand> commands = new ArrayList<ProtocolCommand>();
     	
     	commands.add(startIssuanceCommand(spec, id));
     	
     	commands.addAll(setPublicKeyCommands(
     				spec.getPublicKey(),
     				spec.getCredentialStructure().getAttributeStructs().size() + 1));
     	return commands;
     }
     
     
     /**
      * Get the APDU commands for setting the public key on 
      * the card.
      *  
      * @param spec Issuance spec to get the public key from.
      * @return
      */
     public static ArrayList<ProtocolCommand> setPublicKeyCommands(IssuerPublicKey pubKey, int pubKeyElements) {
     	int l_n = pubKey.getGroupParams().getSystemParams().getL_n();
 
     	ArrayList<ProtocolCommand> commands = new ArrayList<ProtocolCommand>();
     	commands.add(
     			new ProtocolCommand(
     					"publickey_n",
     					"Set public key (n)",
     					new CommandAPDU(
     			        		CLA_IDEMIX, INS_ISSUE_PUBLIC_KEY_N, 0x00, 0x00, 
     			                fixLength(pubKey.getN(), l_n))));
     	
     	commands.add(
     			new ProtocolCommand(
     					"publickey_z",
     					"Set public key (Z)",
     					new CommandAPDU(
     			        		CLA_IDEMIX, INS_ISSUE_PUBLIC_KEY_Z, 0x00, 0x00, 
     			                fixLength(pubKey.getCapZ(), l_n))));
     	
     	BigInteger[] pubKeyElement = pubKey.getCapR();
     	for (int i = 0; i < pubKeyElements; i++) {
         	commands.add(
         			new ProtocolCommand(
         					"publickey_element" + i, 
         					"Set public key element (R@index " + i + ")",
         					new CommandAPDU(
         		            		CLA_IDEMIX, INS_ISSUE_PUBLIC_KEY_R, i, 0x00, 
         		            		fixLength(pubKeyElement[i], l_n))));
     	}
 
     	return commands;    	
     }
 
     
     /**
      * Get the APDU commands to start issuance.
      * 
      * @param spec Issuance specification
      * @param id id of credential
      * @return
      */
     public static ProtocolCommand startIssuanceCommand(IssuanceSpec spec, short id) {
     	int l_H = spec.getPublicKey().getGroupParams().getSystemParams().getL_H();
     	return
     			new ProtocolCommand(
     					"start_issuance", 
     					"Start credential issuance.",
     					new CommandAPDU(
     			        		CLA_IDEMIX, INS_ISSUE_CREDENTIAL, id >> 8, id & 0xff, 
     			        		fixLength(spec.getContext(), l_H)),
     					new HashMap<Integer,String>() {{
     						put(0x00006986,"Credential already issued.");
     					}});
     }
 
     /**
      * Get the APDU commands to start proof.
      * 
      * @param spec Proof specification
      * @param id id of credential
      * @return
      */
     public static ProtocolCommand startProofCommand(ProofSpec spec, short id) {
     	int l_H = spec.getGroupParams().getSystemParams().getL_H();
     	
     	return
     			new ProtocolCommand(
     					"startprove", 
     					"Start credential proof.", 
     					new CommandAPDU(
     			        		CLA_IDEMIX, INS_PROVE_CREDENTIAL, id >> 8, id & 0xff, 
     			        		fixLength(spec.getContext(), l_H)),
     			        new HashMap<Integer,String>() {{
     			        	put(0x00006A88,"Credential not found.");
     			        }});
     }
 
 
 
     public static ProtocolCommand generateMasterSecretCommand =
     			new ProtocolCommand(
     					"generatesecret",
     					"Generate master secret",
     					new CommandAPDU(
     			        		CLA_IDEMIX, INS_GENERATE_SECRET, 0x00, 0x00),
     			        new HashMap<Integer,String>() {{
     			        	put(0x00006986,"Master secret already set.");
     			        }});
     
 
     
     public static ProtocolCommand sendPinCommand(byte[] pin) {
     	return
     			new ProtocolCommand(
     					"sendpin",
     					"Authorize using PIN",
     					new CommandAPDU(
     			        		ISO7816.CLA_ISO7816, ISO7816.INS_VERIFY, 0x00, 0x00, pin)
     					);
     }
     
     /**
      * Get the APDU commands for setting the attributes:
      * 
      * <pre>
      *   m_1, ..., m_l
      * </pre>
      * 
      * @param spec the issuance specification for the ordering of the values.
      * @param values the attributes to be set.
      * @return
      */
     public static ArrayList<ProtocolCommand> setAttributesCommands(IssuanceSpec spec, Values values) {
     	ArrayList<ProtocolCommand> commands = new ArrayList<ProtocolCommand>();
         Vector<AttributeStructure> structs = spec.getCredentialStructure().getAttributeStructs();
         int L_m = spec.getPublicKey().getGroupParams().getSystemParams().getL_m();
         int i = 1;
         for (AttributeStructure struct : structs) {
         	BigInteger attr = (BigInteger) values.get(struct.getName()).getContent();
         	commands.add(
         			new ProtocolCommand(
         					"setattr"+i,
         					"Set attribute (m@index" + i + ")",
         					new CommandAPDU(
         		            		CLA_IDEMIX, INS_ISSUE_ATTRIBUTES, i, 0x00, 
         		                    fixLength(attr, L_m))));
         	i += 1;
         }
     	return commands;
     }
     
     
     public static ArrayList<ProtocolCommand> round1Commands(IssuanceSpec spec, final Message msg) {
     	ArrayList<ProtocolCommand> commands = new ArrayList<ProtocolCommand>();
         BigInteger theNonce1 = msg.getIssuanceElement(
                IssuanceProtocolValues.nonce);
         int L_Phi = spec.getPublicKey().getGroupParams().getSystemParams().getL_Phi();
     	commands.add(
     			new ProtocolCommand(
     					"nonce_n1",
     					"Issue nonce n1",
     					new CommandAPDU(
     		            		CLA_IDEMIX, INS_ISSUE_NONCE_1, 0x00, 0x00, 
     		                    fixLength(theNonce1,L_Phi))));
     	commands.add(
     			new ProtocolCommand(
     					"proof_c",
     					"Issue proof c",
     					new CommandAPDU(
     		            		CLA_IDEMIX, INS_ISSUE_PROOF_U, P1_PROOF_U_C, 0x00)));
     	
     	commands.add(
     			new ProtocolCommand(
     					"vHatPrime",
     					"Issue proof v^'",
     					new CommandAPDU(
     		            		CLA_IDEMIX, INS_ISSUE_PROOF_U, P1_PROOF_U_VPRIMEHAT, 0x00)));
     	commands.add(
     			new ProtocolCommand(
     					"proof_s_A",
     					"Issue proof s_A",
     					new CommandAPDU(
     		            		CLA_IDEMIX, INS_ISSUE_PROOF_U, P1_PROOF_U_S_A, 0x00)));
     	commands.add(
     			new ProtocolCommand(
     					"nonce_n2",
     					"Issue nonce n2",
     					new CommandAPDU(
     		            		CLA_IDEMIX, INS_ISSUE_NONCE_2, 0x00, 0x00)));
     	return commands;
     }
 
     public static Message processRound1Responses(HashMap<String,IResponseAPDU> responses) {
     	HashMap<IssuanceProtocolValues, BigInteger> issuanceProtocolValues = 
                 new HashMap<IssuanceProtocolValues, BigInteger>();
         TreeMap<String, BigInteger> additionalValues = 
                 new TreeMap<String, BigInteger>();
         HashMap<String, SValue> sValues = new HashMap<String, SValue>();
     	
     	issuanceProtocolValues.put(IssuanceProtocolValues.capU, 
                 new BigInteger(1, responses.get("nonce_n1").getData()));
     	
     	BigInteger challenge = new BigInteger(1, responses.get("proof_c").getData());
     	
         additionalValues.put(IssuanceSpec.vHatPrime, 
                 new BigInteger(1, responses.get("vHatPrime").getData()));
         
         sValues.put(IssuanceSpec.MASTER_SECRET_NAME,
                 new SValue(new BigInteger(1, responses.get("proof_s_A").getData())));
         
        issuanceProtocolValues.put(IssuanceProtocolValues.nonce,
                 new BigInteger(1, responses.get("nonce_n2").getData()));
         
         // Return the next protocol message
         return new Message(issuanceProtocolValues, 
                 new Proof(challenge, sValues, additionalValues));        
     }
     
 
     public static ArrayList<ProtocolCommand> round3Commands(IssuanceSpec spec, final Message msg) {
     	ArrayList<ProtocolCommand> commands = new ArrayList<ProtocolCommand>();
     	SystemParameters sysPars = spec.getPublicKey().getGroupParams().getSystemParams();
     	
     	BigInteger A = msg.getIssuanceElement(IssuanceProtocolValues.capA);
     	commands.add(
     			new ProtocolCommand(
     					"signature_A",
     					"Issue signature A",
     					new CommandAPDU(
     		            		CLA_IDEMIX, INS_ISSUE_SIGNATURE, P1_SIGNATURE_A, 0x00, 
     		                    fixLength(A, sysPars.getL_n()))));
     	BigInteger e = msg.getIssuanceElement(IssuanceProtocolValues.e);
     	commands.add(
     			new ProtocolCommand(
     					"signature_e",
     					"Issue signature e",
     					new CommandAPDU(
     		            		CLA_IDEMIX, INS_ISSUE_SIGNATURE, P1_SIGNATURE_E, 0x00, 
     		                    fixLength(e, sysPars.getL_e()))));
     	BigInteger v = msg.getIssuanceElement(IssuanceProtocolValues.vPrimePrime);
     	commands.add(
     			new ProtocolCommand(
     					"vPrimePrime",
     					"Issue signature v''",
     					new CommandAPDU(
     		            		CLA_IDEMIX, INS_ISSUE_SIGNATURE, P1_SIGNATURE_V, 0x00, 
     		                    fixLength(v, sysPars.getL_v()))));
     	commands.add(
     			new ProtocolCommand(
     					"verify",
     					"Verify issued signature",
     					new CommandAPDU(
     		            		CLA_IDEMIX, INS_ISSUE_SIGNATURE, P1_SIGNATURE_VERIFY, 0x00)));
     	BigInteger c = msg.getProof().getChallenge();
     	commands.add(
     			new ProtocolCommand(
     					"proof_c",
     					"Issue proof c'",
     					new CommandAPDU(
     		            		CLA_IDEMIX, INS_ISSUE_PROOF_A, P1_PROOF_A_C, 0x00, 
     		                    fixLength(c, sysPars.getL_H()))));
     	BigInteger s_e = 
         		(BigInteger) msg.getProof().getSValue(IssuanceSpec.s_e).getValue();
     	commands.add(
     			new ProtocolCommand(
     					"proof_s_e",
     					"Issue proof s_e",
     					new CommandAPDU(
     		            		CLA_IDEMIX, INS_ISSUE_PROOF_A, P1_PROOF_A_S_E, 0x00, 
     		                    fixLength(s_e, sysPars.getL_n()))));
     	commands.add(
     			new ProtocolCommand(
     					"proof_verify",
     					"Verify proof",
     					new CommandAPDU(
     		            		CLA_IDEMIX, INS_ISSUE_PROOF_A, P1_PROOF_A_VERIFY, 0x00)));
     	return commands;
     }
     
 
     
     public static ArrayList<ProtocolCommand> buildProofCommands(final BigInteger nonce, final ProofSpec spec, short id) {
     	ArrayList<ProtocolCommand> commands = new ArrayList<ProtocolCommand>();
         // Set the system parameters 
     	SystemParameters sysPars = spec.getGroupParams().getSystemParams();
     	
         List<Integer> disclosed = new Vector<Integer>();        
 
         Predicate predicate = spec.getPredicates().firstElement();
         if (predicate.getPredicateType() != PredicateType.CL) {
             throw new RuntimeException("Unimplemented predicate.");
         }
         CLPredicate pred = ((CLPredicate) predicate);
         StructureStore store = StructureStore.getInstance();
         CredentialStructure cred = (CredentialStructure) store.get(
                pred.getCredStructLocation());
 
         for (AttributeStructure attribute : cred.getAttributeStructs()) {
             String attName = attribute.getName();
             Identifier identifier = pred.getIdentifier(attName);
             if (identifier.isRevealed()) {
                 disclosed.add(attribute.getKeyIndex());
             }
         }
         
         // translate the List to a byte[] 
         Collections.sort(disclosed);
         byte[] D = new byte[disclosed.size()];
         for (int i = 0; i < disclosed.size(); i++) {
             D[i] = disclosed.get(i).byteValue();
         }
 
         commands.add(
         		startProofCommand(spec, id));
         commands.add(
         		new ProtocolCommand(
         				"disclosure_d",
         				"Attribute disclosure selection",
         				new CommandAPDU(CLA_IDEMIX, INS_PROVE_SELECTION, 0x00, 0x00, D)));
         commands.add(
         		new ProtocolCommand(
         				"challenge_c",
         				"Send challenge n1",
         				new CommandAPDU(CLA_IDEMIX, INS_PROVE_NONCE, 0x00, 0x00, 
         	                    fixLength(nonce, sysPars.getL_Phi()))));
         commands.add(
         		new ProtocolCommand(
         				"signature_A",
         				"Get random signature A",
         				new CommandAPDU(CLA_IDEMIX, INS_PROVE_SIGNATURE, P1_SIGNATURE_A, 0x00)));
         commands.add(
         		new ProtocolCommand(
         				"signature_e",
         				"Get random signature e^",
         				new CommandAPDU(CLA_IDEMIX, INS_PROVE_SIGNATURE, P1_SIGNATURE_E, 0x00)));
         commands.add(
         		new ProtocolCommand(
         				"signature_v", 
         				"Get random signature v^",
             				new CommandAPDU(CLA_IDEMIX, INS_PROVE_SIGNATURE, P1_SIGNATURE_V, 0x00)));
         commands.add(
         		new ProtocolCommand(
         				"master", 
         				"Get random value (@index 0).",
         				new CommandAPDU(CLA_IDEMIX, INS_PROVE_RESPONSE, 0x00, 0x00)));
         
         // iterate over all the identifiers
         for (AttributeStructure attribute : cred.getAttributeStructs()) {
         	
             String attName = attribute.getName();
             Identifier identifier = pred.getIdentifier(attName);
             int i = attribute.getKeyIndex();
             if (identifier.isRevealed()) {
             	commands.add(
                 		new ProtocolCommand(
                 				"attr_"+attName,
                 				"Get disclosed attribute (@index " + i + ")",
                 				new CommandAPDU(CLA_IDEMIX, INS_PROVE_ATTRIBUTE, i, 0x00)));
 
             } else {
             	commands.add(
                 		new ProtocolCommand(
                 				"attr_"+attName, 
                 				"Get random value (@index " + i + ").",
                 				new CommandAPDU(CLA_IDEMIX, INS_PROVE_RESPONSE, i, 0x00)));
             }
 
         }        
     	return commands;
     }
     
     public static Proof processBuildProofResponses(HashMap<String,IResponseAPDU> responses, final ProofSpec spec) {
         HashMap<String, SValue> sValues = new HashMap<String, SValue>();
         TreeMap<String, BigInteger> commonList = new TreeMap<String, BigInteger>();
         
         Predicate predicate = spec.getPredicates().firstElement();
         if (predicate.getPredicateType() != PredicateType.CL) {
             throw new RuntimeException("Unimplemented predicate.");
         }
         CLPredicate pred = ((CLPredicate) predicate);
         StructureStore store = StructureStore.getInstance();
         CredentialStructure cred = (CredentialStructure) store.get(
                pred.getCredStructLocation());
 
 
         BigInteger challenge = new BigInteger(1, responses.get("challenge_c").getData());
 
         commonList.put(pred.getTempCredName(),
         				new BigInteger(1, responses.get("signature_A").getData()));             
         
         sValues.put(pred.getTempCredName(), 
         		new SValue(
         				new SValuesProveCL(
         						new BigInteger(1, responses.get("signature_e").getData()), 
         						new BigInteger(1, responses.get("signature_v").getData())
         						)));
 
         sValues.put(IssuanceSpec.MASTER_SECRET_NAME, 
         		new SValue(new BigInteger(1, responses.get("master").getData())));
                     
         for (AttributeStructure attribute : cred.getAttributeStructs()) {
         	String attName = attribute.getName();
             Identifier identifier = pred.getIdentifier(attName);
             sValues.put(identifier.getName(), 
             		new SValue(new BigInteger(1, responses.get("attr_" + attName).getData())));
         }
         
         // Return the generated proof, based on the proof specification
         return new Proof(challenge, sValues, commonList);
     }
     
     public static ArrayList<ProtocolCommand> singleCommand(ProtocolCommand command) {
     	ArrayList<ProtocolCommand> commands = new ArrayList<ProtocolCommand>();
     	commands.add(command);
     	return commands;
     }  
     
 
 }
