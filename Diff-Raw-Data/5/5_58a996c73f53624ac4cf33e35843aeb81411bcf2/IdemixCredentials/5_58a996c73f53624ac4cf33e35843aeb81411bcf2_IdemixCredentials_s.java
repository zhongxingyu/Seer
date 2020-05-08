 /**
  * IdemixCredentials.java
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
  * Copyright (C) Pim Vullers, Radboud University Nijmegen, May 2012,
  * Copyright (C) Wouter Lueks, Radboud University Nijmegen, July 2012.
  */
 
 package org.irmacard.credentials.idemix;
 
 import java.math.BigInteger;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import net.sourceforge.scuba.smartcards.CardService;
 import net.sourceforge.scuba.smartcards.CardServiceException;
 import net.sourceforge.scuba.smartcards.ProtocolCommands;
 import net.sourceforge.scuba.smartcards.ProtocolResponses;
 
 import org.irmacard.credentials.Attributes;
 import org.irmacard.credentials.BaseCredentials;
 import org.irmacard.credentials.CredentialsException;
 import org.irmacard.credentials.Nonce;
 import org.irmacard.credentials.idemix.spec.IdemixIssueSpecification;
 import org.irmacard.credentials.idemix.spec.IdemixVerifySpecification;
 import org.irmacard.credentials.idemix.util.CredentialInformation;
 import org.irmacard.credentials.idemix.util.IdemixVersion;
 import org.irmacard.credentials.info.AttributeDescription;
 import org.irmacard.credentials.info.CredentialDescription;
 import org.irmacard.credentials.info.DescriptionStore;
 import org.irmacard.credentials.info.InfoException;
 import org.irmacard.credentials.keys.PrivateKey;
 import org.irmacard.credentials.spec.IssueSpecification;
 import org.irmacard.credentials.spec.VerifySpecification;
 import org.irmacard.credentials.util.CardVersion;
 import org.irmacard.credentials.util.log.IssueLogEntry;
 import org.irmacard.credentials.util.log.LogEntry;
 import org.irmacard.credentials.util.log.RemoveLogEntry;
 import org.irmacard.credentials.util.log.VerifyLogEntry;
 import org.irmacard.idemix.IdemixService;
 import org.irmacard.idemix.IdemixSmartcard;
 import org.irmacard.idemix.util.IdemixLogEntry;
 
 import com.ibm.zurich.idmx.issuance.Issuer;
 import com.ibm.zurich.idmx.issuance.Message;
 import com.ibm.zurich.idmx.showproof.Proof;
 import com.ibm.zurich.idmx.showproof.Verifier;
 import com.ibm.zurich.idmx.showproof.predicates.CLPredicate;
 import com.ibm.zurich.idmx.showproof.predicates.Predicate;
 import com.ibm.zurich.idmx.showproof.predicates.Predicate.PredicateType;
 import com.ibm.zurich.idmx.utils.Constants;
 import com.ibm.zurich.idmx.utils.SystemParameters;
 
 /**
  * An Idemix specific implementation of the credentials interface.
  */
 public class IdemixCredentials extends BaseCredentials {
 	IdemixService service = null;
 
 	public IdemixCredentials(CardService cs) {
 		super(cs);
 		if (cs instanceof IdemixService) {
 			service = (IdemixService) cs;
 		} else {
 			service = new IdemixService(cs);
 		}
 	}
 
 	public void connect() 
 	throws CredentialsException {
 		try {
 			service.open();
 		} catch (CardServiceException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Issue a credential to the user according to the provided specification
 	 * containing the specified values.
 	 * 
 	 * This method requires the Idemix application to be selected and the card 
 	 * holder to be verified (if this is required by the card).
 	 *
 	 * @param specification
 	 *            of the issuer and the credential to be issued.
 	 * @param values
 	 *            to be stored in the credential.
 	 * @param expires
 	 *            at this date, or after 6 months if null.
 	 * @throws CredentialsException
 	 *             if the issuance process fails.
 	 */
 	@Override
 	public void issue(IssueSpecification specification, PrivateKey sk,
 			Attributes values, Date expiry) throws CredentialsException {
 		IdemixIssueSpecification spec = castIssueSpecification(specification);
 		IdemixPrivateKey isk = castIdemixPrivateKey(sk);
 
 		values.setExpiryAttribute(expiry);
 
 		// Initialize the issuer
 		Issuer issuer = new Issuer(isk.getIssuerKeyPair(), spec.getIssuanceSpec(),
 				null, null, spec.getValues(values));
 
 		try {
 			ProtocolResponses responses = service
 					.execute(requestIssueRound1Commands(specification, values,
 							issuer));
 			service.execute(requestIssueRound3Commands(spec, values, issuer,
 					responses));
 		} catch (CardServiceException e) {
 			throw new CredentialsException("Issuing caused exception", e);
 		}
 	}
 
 	/**
 	 * Get a blank IssueSpecification matching this Credentials provider.
 	 * TODO: Proper implementation or remove it.
 	 * 
 	 * @return a blank specification matching this provider.
 	 */
 	public IssueSpecification issueSpecification() {
 		return null;
 	}
 
 	public void verifyPrepare()
 	throws CredentialsException {
 		try {
 			service = new IdemixService(cs);
 			service.open();
 		} catch (CardServiceException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Verify a number of attributes listed in the specification.
 	 * 
 	 * @param specification
 	 *            of the credential and attributes to be verified.
 	 * @return the attributes disclosed during the verification process or null
 	 *         if verification failed
 	 * @throws CredentialsException
 	 */
 	public Attributes verify(VerifySpecification specification)
 			throws CredentialsException {
 		verifyPrepare();
 
 		// Get a nonce from the verifier
 		Nonce nonce = generateNonce(specification);
 
 		// Run the protocol
 		try {
 			return verifyProofResponses(specification, nonce,
 					service.execute(requestProofCommands(specification, nonce)));
 		} catch (CardServiceException e) {
 			throw new CredentialsException("Verification encountered error", e);
 		}
 	}
 
 	/**
 	 * Get a blank VerifySpecification matching this Credentials provider. TODO:
 	 * proper implementation or remove it
 	 * 
 	 * @return a blank specification matching this provider.
 	 */
 	@Override
 	public VerifySpecification verifySpecification() {
 		return null;
 	}
 
 	@Override
 	public ProtocolCommands requestProofCommands(
 			VerifySpecification specification, Nonce nonce)
 			throws CredentialsException {
 		IdemixVerifySpecification spec = castVerifySpecification(specification);
 		IdemixNonce n = castNonce(nonce);
 		return IdemixSmartcard.buildProofCommands(n.getNonce(),
 				spec.getProofSpec(), spec.getIdemixId());
 	}
 
 	@Override
 	public Attributes verifyProofResponses(VerifySpecification specification,
 			Nonce nonce, ProtocolResponses responses)
 			throws CredentialsException {
 		IdemixVerifySpecification spec = castVerifySpecification(specification);
 		IdemixNonce n = castNonce(nonce);
 
 		// Create the proof
 		Proof proof = IdemixSmartcard.processBuildProofResponses(responses,
 				spec.getProofSpec());
 		if (proof == null) {
 			throw new CredentialsException("Failed to generate proof.");
 		}
 		// Initialize the verifier and verify the proof
 		Verifier verifier = new Verifier(spec.getProofSpec(), proof,
 				n.getNonce());
 		if (!verifier.verify()) {
 			return null;
 		}
 
 		// Return the attributes that have been revealed during the proof
 		Attributes attributes = new Attributes();
 		HashMap<String, BigInteger> values = verifier.getRevealedValues();
 
 		// First determine the prefix that needs to be stripped from the name
 		String prefix = "";
 		for (Predicate pred : spec.getProofSpec().getPredicates()) {
 			if (pred.getPredicateType() == PredicateType.CL) {
 				prefix = ((CLPredicate) pred).getTempCredName() + Constants.DELIMITER;
 				break;
 			}
 		}
 
 		// Store the attributes
 		for (String id : values.keySet()) {
 			String name = id.replace(prefix, "");
 			attributes.add(name, values.get(id).toByteArray());
 		}
 
 		// Verify validity
 		if (!attributes.isValid()) {
 			System.err.println("Credential expired!");
 			throw new CredentialsException("The credential has expired.");
 		}
 
 		return attributes;
 	}
 
 	/**
 	 * First part of issuance protocol. Not yet included in the interface as
 	 * this is subject to change. Most notably
 	 *  - How do we integrate the issuer in this, I would guess the only state
 	 *    in fact the nonce, so we could handle that a bit cleaner. Carrying around
 	 *    the issuer object may not be the best solution
 	 *  - We need to deal with the selectApplet and sendPinCommands better.
 	 * @throws CredentialsException
 	 */
 	public ProtocolCommands requestIssueRound1Commands(
 			IssueSpecification ispec, Attributes attributes, Issuer issuer)
 			throws CredentialsException {
 		ProtocolCommands commands = new ProtocolCommands();
 		IdemixIssueSpecification spec = castIssueSpecification(ispec);
 
 		commands.addAll(IdemixSmartcard.setIssuanceSpecificationCommands(
 				spec.getIssuanceSpec(), spec.getIdemixId()));
 
 		commands.addAll(IdemixSmartcard.setAttributesCommands(
 				spec.getIssuanceSpec(), spec.getValues(attributes)));
 
 		// Issue the credential
 		Message msgToRecipient1 = issuer.round0();
 		if (msgToRecipient1 == null) {
 			throw new CredentialsException("Failed to issue the credential (0)");
 		}
 
 		commands.addAll(IdemixSmartcard.round1Commands(spec.getIssuanceSpec(),
 				msgToRecipient1));
 
 		return commands;
 	}
 
 	/**
 	 * Second part of issuing. Just like the first part still in flux. Note how
 	 * we can immediately process the responses as well as create new commands.
 	 * 
 	 * @throws CredentialsException
 	 */
 	public ProtocolCommands requestIssueRound3Commands(IssueSpecification ispec, Attributes attributes, Issuer issuer, ProtocolResponses responses)
 	throws CredentialsException {
 		return requestIssueRound3Commands(ispec, attributes, issuer, responses, null);
 	}
 	public ProtocolCommands requestIssueRound3Commands(IssueSpecification ispec, Attributes attributes, Issuer issuer, ProtocolResponses responses, BigInteger nonce)
 	throws CredentialsException {
 		IdemixIssueSpecification spec = castIssueSpecification(ispec);
 		Message msgToIssuer = IdemixSmartcard.processRound1Responses(responses);
		Message msgToRecipient2 = nonce == null ? issuer.round2(msgToIssuer) : issuer.round2(nonce, msgToIssuer);
 		return IdemixSmartcard.round3Commands(spec.getIssuanceSpec(), msgToRecipient2);
 	}
 
 	@Override
 	public Nonce generateNonce(VerifySpecification specification)
 			throws CredentialsException {
 		IdemixVerifySpecification spec = castVerifySpecification(specification);
 
 		SystemParameters sp = spec.getProofSpec().getGroupParams()
 				.getSystemParams();
 		BigInteger nonce = Verifier.getNonce(sp);
 
 		return new IdemixNonce(nonce);
 	}
 	
 	/**
 	 * Get the attribute values stored on the card for the given credential.
 	 *  
 	 * @param credential identifier.
 	 * @return attributes for the given credential.
 	 * @throws CardServiceException 
 	 */
 	public Attributes getAttributes(CredentialDescription cd) throws CardServiceException {
 		// FIXME: for now retrieve this here, but this does mean that these files get
 		// loaded over and over again.
 		CredentialInformation ci = new CredentialInformation(cd);
 
 		service.selectCredential(ci.getIdemixIssueSpecification().getIdemixId());
 		HashMap<String, BigInteger> attr_map = service.getAttributes(ci
 				.getIdemixIssueSpecification().getIssuanceSpec());
 
 		// FIXME: it is highly doubtful that this should happen here
 		Attributes attr = new Attributes();
 		for(String k : attr_map.keySet()) {
 			attr.add(k, attr_map.get(k).toByteArray());
 		}
 		return attr;
 	}
 	
 	public void removeCredential(CredentialDescription cd) throws CardServiceException {
 		service.selectCredential(cd.getId());
 		service.removeCredential(cd.getId());
 	}
 
 	/**
 	 * Get a list of credentials available on the card.
 	 * 
 	 * @return list of credential identifiers.
 	 * @throws CardServiceException 
 	 * @throws InfoException 
 	 */
 	public List<CredentialDescription> getCredentials() throws CardServiceException, InfoException {
 		Vector<Integer> credentialIDs = service.getCredentials();
 		
 		List<CredentialDescription> credentialList = new Vector<CredentialDescription>();;
 		DescriptionStore ds = DescriptionStore.getInstance();
 		
 		for(Integer id : credentialIDs) {
 			CredentialDescription cd = ds.getCredentialDescription(id.shortValue());
 			if(cd != null) {
 				credentialList.add(cd);
 			} else {
 				throw new InfoException("Description for credential with ID=" + id + " not found");
 			}
 		}
 		
 		return credentialList;
 	}
 
 	private static IdemixVerifySpecification castVerifySpecification(
 			VerifySpecification spec) throws CredentialsException {
 		if (!(spec instanceof IdemixVerifySpecification)) {
 			throw new CredentialsException(
 					"specification is not an IdemixVerifySpecification");
 		}
 		return (IdemixVerifySpecification) spec;
 	}
 
 	private static IdemixIssueSpecification castIssueSpecification(
 			IssueSpecification spec) throws CredentialsException {
 		if (!(spec instanceof IdemixIssueSpecification)) {
 			throw new CredentialsException(
 					"specification is not an IdemixVerifySpecification");
 		}
 		return (IdemixIssueSpecification) spec;
 	}
 
 	private static IdemixNonce castNonce(Nonce nonce)
 			throws CredentialsException {
 		if (!(nonce instanceof IdemixNonce)) {
 			throw new CredentialsException("nonce is not an IdemixNonce");
 		}
 		return (IdemixNonce) nonce;
 	}
 
 	private IdemixPrivateKey castIdemixPrivateKey(PrivateKey sk)
 			throws CredentialsException {
 		if (!(sk instanceof IdemixPrivateKey)) {
 			throw new CredentialsException(
 					"PrivateKey is not an IdemixPrivateKey");
 		}
 		return (IdemixPrivateKey) sk;
 	}
 
 	@Override
 	public List<LogEntry> getLog() throws CardServiceException, InfoException {
 		List<IdemixLogEntry> idemix_logs = service.getLogEntries();
 		Vector<LogEntry> logs = new Vector<LogEntry>();
 		LogEntry entry = null;
 
 		for(IdemixLogEntry l : idemix_logs) {
 			if(l.getAction() == IdemixLogEntry.Action.NONE)
 				continue;
 
 			DescriptionStore ds = DescriptionStore.getInstance();
 			Date timestamp = l.getTimestamp();
 			CredentialDescription credential = ds.getCredentialDescription(l.getCredential());
 			if(credential == null) {
 				System.out.println("This shouldn't happen, cannot find the description");
 				l.print();
 				continue;
 			}
 
 			switch (l.getAction()) {
 			case ISSUE:
 				entry = new IssueLogEntry(timestamp, credential);
 				break;
 			case REMOVE:
 				entry = new RemoveLogEntry(timestamp, credential);
 				break;
 			case VERIFY:
 				entry = new VerifyLogEntry(timestamp,
 						credential, null, makeAttributeDisclosed(credential,
 								l.getDisclose()));
 				break;
 
 			// These should not happen...
 			case NONE:
 			default:
 				continue;
 			}
 			logs.add(entry);
 		}
 
 		return logs;
 	}
 
 	private HashMap<String, Boolean> makeAttributeDisclosed(CredentialDescription cred, short disclose) {
 		HashMap<String, Boolean> attributeDisclosed = new HashMap<String, Boolean>();
 		List<AttributeDescription> attributes = cred.getAttributes();
 
 		// Start at 2 so we skip the master secret and expiry
 		for (int i = 2; i < attributes.size() + 2; i++) {
 			attributeDisclosed.put(attributes.get(i-2).getName(), new Boolean(
 					(disclose & (1 << i)) != 0));
 		}
 
 		return attributeDisclosed;
 	}
 
 	public CardVersion getCardVersion() {
 		return new IdemixVersion(service.getCardVersion());
 	}
 }
