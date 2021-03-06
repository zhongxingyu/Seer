 /*
  * otr4j, the open source java otr library.
  *
  * Distributable under LGPL license.
  * See terms of license at gnu.org.
  */
 
 package net.java.otr4j;
 
 import java.io.*;
 import java.math.*;
 import java.security.*;
 import java.util.*;
 import java.util.logging.*;
 import javax.crypto.interfaces.*;
 
 import net.java.otr4j.context.*;
 import net.java.otr4j.context.auth.*;
 import net.java.otr4j.crypto.*;
 import net.java.otr4j.message.*;
 import net.java.otr4j.message.encoded.*;
 import net.java.otr4j.message.encoded.signature.*;
 import net.java.otr4j.message.unencoded.*;
 import net.java.otr4j.message.unencoded.query.*;
 
 /**
  * 
  * @author George Politis
  * 
  */
 public final class StateMachine {
 	private static Logger logger = Logger.getLogger(StateMachine.class
 			.getName());
 
 	public static String sendingMessage(OTR4jListener listener,
 			UserState userState, String user, String account, String protocol,
 			String msgText) {
 		try {
 			ConnContext ctx = userState.getConnContext(user, account, protocol);
 
 			switch (ctx.getMessageState()) {
 			case PLAINTEXT:
 				return msgText;
 			case ENCRYPTED:
 				logger.info(account + " sends an encrypted message to " + user
 						+ " throught " + protocol + ".");
 
 				// Get encryption keys.
 				SessionKeys encryptionKeys = ctx.getEncryptionSessionKeys();
 				int senderKeyID = encryptionKeys.localKeyID;
 				int receipientKeyID = encryptionKeys.remoteKeyID;
 
 				// Increment CTR.
 				encryptionKeys.incrementSendingCtr();
 				byte[] ctr = encryptionKeys.getSendingCtr();
 
 				// Encrypt message.
 				logger
 						.info("Encrypting message with keyids (localKeyID, remoteKeyID) = ("
 								+ senderKeyID + ", " + receipientKeyID + ")");
 				byte[] encryptedMsg = CryptoUtils.aesEncrypt(encryptionKeys
 						.getSendingAESKey(), ctr, msgText.getBytes());
 
 				// Get most recent keys to get the next D-H public key.
 				SessionKeys mostRecentKeys = ctx.getMostRecentSessionKeys();
 				DHPublicKey nextDH = (DHPublicKey) mostRecentKeys.localPair
 						.getPublic();
 
 				// Calculate T.
 				MysteriousT t = new MysteriousT(senderKeyID, receipientKeyID,
 						nextDH, ctr, encryptedMsg, 2, 0);
 
 				// Calculate T hash.
 				byte[] sendingMACKey = encryptionKeys.getSendingMACKey();
 				byte[] mac = t.hash(sendingMACKey);
 
 				// Get old MAC keys to be revealed.
 				byte[] oldMacKeys = ctx.getOldMacKeys();
 				DataMessage msg = new DataMessage(t, mac, oldMacKeys);
 				return msg.toUnsafeString();
 			case FINISHED:
 				return msgText;
 			default:
 				return msgText;
 			}
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Message sending failed.", e);
 			return msgText;
 		}
 	}
 
 	public static String receivingMessage(OTR4jListener listener,
 			UserState userState, String user, String account, String protocol,
 			String msgText) {
 		ByteArrayInputStream in = null;
 		try {
 			if (Utils.IsNullOrEmpty(msgText))
 				return msgText;
 
 			ConnContext ctx = userState.getConnContext(user, account, protocol);
 			int policy = listener.getPolicy(ctx);
 
 			if (!PolicyUtils.getAllowV1(policy)
 					&& !PolicyUtils.getAllowV2(policy)) {
 				logger
 						.info("Policy does not allow neither V1 not V2, ignoring message.");
 				return msgText;
 			}
 
 			switch (MessageHeader.getMessageType(msgText)) {
 			case MessageType.DATA:
 				logger.info(account + " received a data message from " + user
 						+ ".");
 				DataMessage data = new DataMessage();
 				in = new ByteArrayInputStream(EncodedMessageUtils
 						.decodeMessage(msgText));
 				data.readObject(in);
 				return receivingDataMessage(ctx, listener, data);
 			case MessageType.DH_COMMIT:
 				logger.info(account + " received a D-H commit message from "
 						+ user + " throught " + protocol + ".");
 				DHCommitMessage dhCommit = new DHCommitMessage();
 				in = new ByteArrayInputStream(EncodedMessageUtils
 						.decodeMessage(msgText));
 				dhCommit.readObject(in);
 				receivingDHCommitMessage(ctx, listener, dhCommit);
 				return null;
 			case MessageType.DH_KEY:
 				logger.info(account + " received a D-H key message from "
 						+ user + " throught " + protocol + ".");
 				DHKeyMessage dhKey = new DHKeyMessage();
 				in = new ByteArrayInputStream(EncodedMessageUtils
 						.decodeMessage(msgText));
 				dhKey.readObject(in);
 				receivingDHKeyMessage(ctx, listener, dhKey, account, protocol);
 				return null;
 			case MessageType.REVEALSIG:
 				logger.info(account
 						+ " received a reveal signature message from " + user
 						+ " throught " + protocol + ".");
 				RevealSignatureMessage revealSigMessage = new RevealSignatureMessage();
 				in = new ByteArrayInputStream(EncodedMessageUtils
 						.decodeMessage(msgText));
 				revealSigMessage.readObject(in);
 				receivingRevealSignatureMessage(ctx, listener,
 						revealSigMessage, account, protocol);
 				return null;
 			case MessageType.SIGNATURE:
 				logger.info(account + " received a signature message from "
 						+ user + " throught " + protocol + ".");
 				SignatureMessage sigMessage = new SignatureMessage();
 				in = new ByteArrayInputStream(EncodedMessageUtils
 						.decodeMessage(msgText));
 				sigMessage.readObject(in);
 				receivingSignatureMessage(ctx, listener, sigMessage);
 				return null;
 			case MessageType.ERROR:
 				logger.info(account + " received an error message from " + user
 						+ " throught " + protocol + ".");
 				receivingErrorMessage(ctx, listener, new ErrorMessage(msgText));
 				break;
 			case MessageType.PLAINTEXT:
 				logger.info(account + " received a plaintext message from "
 						+ user + " throught " + protocol + ".");
 				return receivingPlainTextMessage(ctx, listener,
 						new PlainTextMessage(msgText));
 			case MessageType.QUERY:
 				logger.info(account + " received a query message from " + user
 						+ " throught " + protocol + ".");
 				receivingQueryMessage(ctx, listener, new QueryMessage(msgText));
 				logger.info("User needs to know nothing about Query messages.");
 				return null;
 			case MessageType.V1_KEY_EXCHANGE:
 				logger
 						.warning("Received V1 key exchange which is not supported.");
 				throw new UnsupportedOperationException();
 			case MessageType.UKNOWN:
 			default:
 				logger.warning("Unrecognizable OTR message received.");
 				break;
 			}
 
 			return msgText;
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Message receiving failed.", e);
 			return msgText;
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					logger.log(Level.WARNING,
 							"Could not close receiving stream.", e);
 				}
 			}
 		}
 	}
 
 	private static String receivingDataMessage(ConnContext ctx,
 			OTR4jListener listener, DataMessage msg) throws Exception {
 
 		switch (ctx.getMessageState()) {
 		case ENCRYPTED:
 			logger
 					.info("Message state is ENCRYPTED. Trying to decrypt message.");
 			MysteriousT t = msg.t;
 
 			// Find matching session keys.
 			int senderKeyID = t.senderKeyID;
 			int receipientKeyID = t.recipientKeyID;
 			SessionKeys matchingKeys = ctx.findSessionKeysByID(receipientKeyID,
 					senderKeyID);
 
 			if (matchingKeys == null)
 				throw new OtrException("No matching keys found.");
 
 			// Verify received MAC with a locally calculated MAC.
 			if (!msg.verify(matchingKeys.getReceivingMACKey()))
 				throw new OtrException("MAC verification failed.");
 
 			logger.info("Computed HmacSHA1 value matches sent one.");
 
 			// Mark this MAC key as old to be revealed.
 			matchingKeys.setIsUsedReceivingMACKey(true);
 
 			matchingKeys.setReceivingCtr(t.ctr);
 
 			String decryptedMsgContent = t.getDecryptedMessage(matchingKeys
 					.getReceivingAESKey(), matchingKeys.getReceivingCtr());
 			logger.info("Decrypted message: \"" + decryptedMsgContent + "\"");
 
 			// Rotate keys if necessary.
 			ctx.rotateKeys(receipientKeyID, senderKeyID, t.nextDHPublicKey);
 
 			return decryptedMsgContent;
 		case FINISHED:
 		case PLAINTEXT:
 			listener.showWarning("Unreadable encrypted message was received");
 			ErrorMessage errormsg = new ErrorMessage("Oups.");
 			listener.injectMessage(errormsg.toString());
 			break;
 		}
 
 		return null;
 	}
 
 	private static void receivingSignatureMessage(ConnContext ctx,
 			OTR4jListener listener, SignatureMessage msg) throws Exception {
 
 		int policy = listener.getPolicy(ctx);
 		if (!PolicyUtils.getAllowV2(policy)) {
 			logger.info("Policy does not allow OTRv2, ignoring message.");
 			return;
 		}
 
 		AuthenticationInfo auth = ctx.getAuthenticationInfo();
 		switch (auth.getAuthenticationState()) {
 		case AWAITING_SIG:
 
 			// Verify MAC.
 			if (!msg.verify(auth.getM2p()))
 				throw new OtrException(
 						"Signature MACs are not equal, ignoring message.");
 
 			// Decrypt X.
 			byte[] remoteXDecrypted = msg.decrypt(auth.getCp());
 			MysteriousX remoteX = new MysteriousX();
 			remoteX.readObject(remoteXDecrypted);
 
 			// Compute signature.
 			MysteriousM remoteM = new MysteriousM(auth.getRemoteDHPublicKey(),
 					(DHPublicKey) auth.getLocalDHKeyPair().getPublic(), remoteX
 							.getLongTermPublicKey(), remoteX.getDhKeyID());
 
 			// Verify signature.
 			if (!remoteM.verify(auth.getM1p(), remoteX.getLongTermPublicKey(),
 					remoteX.getSignature()))
 				throw new OtrException("Signature verification failed.");
 
 			auth.setRemoteDHPublicKeyID(remoteX.getDhKeyID());
 			ctx.goSecure();
 			break;
 		default:
 			logger.info("We were not expecting a signature, ignoring message.");
 			break;
 		}
 
 	}
 
 	private static void receivingRevealSignatureMessage(ConnContext ctx,
 			OTR4jListener listener, RevealSignatureMessage msg, String account,
 			String protocol) throws Exception {
 
 		int policy = listener.getPolicy(ctx);
 		if (!PolicyUtils.getAllowV2(policy)) {
 			logger.info("Policy does not allow OTRv2, ignoring message.");
 			return;
 		}
 
 		AuthenticationInfo auth = ctx.getAuthenticationInfo();
 		switch (auth.getAuthenticationState()) {
 		case AWAITING_REVEALSIG:
 			auth.setRemoteDHPublicKey(msg.getRevealedKey());
 
 			// Verify received Data.
 			if (!msg.verify(auth.getM2()))
 				throw new OtrException(
 						"Signature MACs are not equal, ignoring message.");
 
 			// Decrypt X.
 			byte[] remoteXDecrypted = msg.decrypt(auth.getC());
 			MysteriousX remoteX = new MysteriousX();
 			remoteX.readObject(remoteXDecrypted);
 
 			// Compute signature.
 			MysteriousM remoteM = new MysteriousM(auth.getRemoteDHPublicKey(),
 					(DHPublicKey) auth.getLocalDHKeyPair().getPublic(), remoteX
 							.getLongTermPublicKey(), remoteX.getDhKeyID());
 
 			// Verify signature.
 			if (!remoteM.verify(auth.getM1(), remoteX.getLongTermPublicKey(),
 					remoteX.getSignature()))
 				throw new OtrException("Signature verification failed.");
 
 			logger.info("Signature verification succeeded.");
 
 			// Compute our own signature.
 			auth
 					.setLocalLongTermKeyPair(listener.getKeyPair(account,
 							protocol));
 
 			// Compute X.
 			MysteriousX x = auth.getLocalMysteriousX(true);
 			SignatureMessage msgSig = new SignatureMessage(2, x.hash,
 					x.encrypted);
 
 			// Go secure, this must be done after X has been calculated.
 			auth.setRemoteDHPublicKeyID(remoteX.getDhKeyID());
 			ctx.goSecure();
 
 			String msgText = msgSig.toUnsafeString();
 			listener.injectMessage(msgText);
 			break;
 		default:
 			break;
 		}
 	}
 
 	private static String receivingPlainTextMessage(ConnContext ctx,
 			OTR4jListener listener, PlainTextMessage msg) throws Exception {
 		Vector<Integer> versions = msg.versions;
 		int policy = listener.getPolicy(ctx);
 		if (versions.size() < 1) {
 			logger
 					.info("Received plaintext message without the whitespace tag.");
 			switch (ctx.getMessageState()) {
 			case ENCRYPTED:
 			case FINISHED:
 				// Display the message to the user, but warn him that the
 				// message was received unencrypted.
 				listener.showWarning("The message was received unencrypted.");
 				return msg.cleanText;
 			case PLAINTEXT:
 				// Simply display the message to the user. If REQUIRE_ENCRYPTION
 				// is set, warn him that the message was received unencrypted.
 				if (PolicyUtils.getRequireEncryption(policy)) {
 					listener
 							.showWarning("The message was received unencrypted.");
 				}
 				break;
 			}
 		} else {
 			logger.info("Received plaintext message with the whitespace tag.");
 			String cleanText = msg.cleanText;
 			switch (ctx.getMessageState()) {
 			case ENCRYPTED:
 			case FINISHED:
 				// Remove the whitespace tag and display the message to the
 				// user, but warn him that the message was received unencrypted.
 				listener.showWarning("The message was received unencrypted.");
 				return cleanText;
 			case PLAINTEXT:
 				// Remove the whitespace tag and display the message to the
 				// user. If REQUIRE_ENCRYPTION is set, warn him that the message
 				// was received unencrypted.
 				if (PolicyUtils.getRequireEncryption(policy)) {
 					listener
 							.showWarning("The message was received unencrypted.");
 				}
 				return cleanText;
 			}
 
 			if (PolicyUtils.getWhiteSpaceStartsAKE(policy)) {
 				logger.info("WHITESPACE_START_AKE is set");
 
 				if (versions.contains(2) && PolicyUtils.getAllowV2(policy)) {
 					logger.info("V2 tag found, starting v2 AKE.");
 					AuthenticationInfo auth = ctx.getAuthenticationInfo();
 					auth.reset();
 
 					DHCommitMessage dhCommitMessage = new DHCommitMessage(2,
 							auth.getLocalDHPublicKeyHash(), auth
 									.getLocalDHPublicKeyEncrypted());
 					auth
 							.setAuthenticationState(AuthenticationState.AWAITING_DHKEY);
 
 					logger.info("Sending D-H Commit.");
 					listener.injectMessage(dhCommitMessage.toUnsafeString());
 				} else if (versions.contains(1)
 						&& PolicyUtils.getAllowV1(policy)) {
 					throw new UnsupportedOperationException();
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private static void receivingQueryMessage(ConnContext ctx,
 			OTR4jListener listener, QueryMessage msg) throws Exception {
 
 		Vector<Integer> versions = msg.versions;
 		int policy = listener.getPolicy(ctx);
 		if (versions.contains(2) && PolicyUtils.getAllowV2(policy)) {
 			logger
 					.info("Query message with V2 support found, starting V2 AKE.");
 			AuthenticationInfo auth = ctx.getAuthenticationInfo();
 			auth.reset();
 
 			DHCommitMessage dhCommitMessage = new DHCommitMessage(2, auth
 					.getLocalDHPublicKeyHash(), auth
 					.getLocalDHPublicKeyEncrypted());
 			auth.setAuthenticationState(AuthenticationState.AWAITING_DHKEY);
 
 			logger.info("Sending D-H Commit.");
 			listener.injectMessage(dhCommitMessage.toUnsafeString());
 		} else if (versions.contains(1) && PolicyUtils.getAllowV1(policy)) {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	private static void receivingErrorMessage(ConnContext ctx,
 			OTR4jListener listener, ErrorMessage msg) {
 
 		listener.showError(msg.error);
 		int policy = listener.getPolicy(ctx);
 		if (PolicyUtils.getErrorStartsAKE(policy)) {
 			logger.info("Error message starts AKE.");
 			Vector<Integer> versions = new Vector<Integer>();
 			if (PolicyUtils.getAllowV1(policy))
 				versions.add(1);
 
 			if (PolicyUtils.getAllowV2(policy))
 				versions.add(2);
 
 			QueryMessage queryMessage = new QueryMessage(versions);
 
 			logger.info("Sending Query");
 			listener.injectMessage(queryMessage.toString());
 		}
 	}
 
 	private enum ReceivingDHCommitMessageActions {
 		RETRANSMIT_OLD_DH_KEY, SEND_NEW_DH_KEY, RETRANSMIT_DH_COMMIT,
 	}
 
 	private static void receivingDHCommitMessage(ConnContext ctx,
 			OTR4jListener listener, DHCommitMessage msg) throws Exception {
 
 		if (!PolicyUtils.getAllowV2(listener.getPolicy(ctx))) {
 			logger.info("ALLOW_V2 is not set, ignore this message.");
 			return;
 		}
 
 		// Set SEND_DH_KEY as default action.
 		ReceivingDHCommitMessageActions action = ReceivingDHCommitMessageActions.SEND_NEW_DH_KEY;
 
 		AuthenticationInfo auth = ctx.getAuthenticationInfo();
 		switch (auth.getAuthenticationState()) {
 		case NONE:
 			action = ReceivingDHCommitMessageActions.SEND_NEW_DH_KEY;
 			break;
 
 		case AWAITING_DHKEY:
			BigInteger ourHash = new BigInteger(auth.getLocalDHPublicKeyHash())
					.abs();
			BigInteger theirHash = new BigInteger(msg.getDhPublicKeyHash())
					.abs();
 
 			if (theirHash.compareTo(ourHash) == -1) {
 				action = ReceivingDHCommitMessageActions.RETRANSMIT_DH_COMMIT;
 			} else {
 				action = ReceivingDHCommitMessageActions.SEND_NEW_DH_KEY;
 			}
 			break;
 
 		case AWAITING_REVEALSIG:
 			action = ReceivingDHCommitMessageActions.RETRANSMIT_OLD_DH_KEY;
 			break;
 		case AWAITING_SIG:
 			action = ReceivingDHCommitMessageActions.SEND_NEW_DH_KEY;
 			break;
 		case V1_SETUP:
 			throw new UnsupportedOperationException();
 		}
 
 		switch (action) {
 		case RETRANSMIT_DH_COMMIT:
 			logger
 					.info("Ignore the incoming D-H Commit message, but resend your D-H Commit message.");
 			DHCommitMessage dhCommit = new DHCommitMessage(2, auth
 					.getLocalDHPublicKeyHash(), auth
 					.getLocalDHPublicKeyEncrypted());
 
 			logger.info("Sending D-H Commit.");
 			listener.injectMessage(dhCommit.toUnsafeString());
 			break;
 		case SEND_NEW_DH_KEY:
 			auth.reset();
 		case RETRANSMIT_OLD_DH_KEY:
 			auth.setRemoteDHPublicKeyEncrypted(msg.getDhPublicKeyEncrypted());
 			auth.setRemoteDHPublicKeyHash(msg.getDhPublicKeyHash());
 
 			DHKeyMessage dhKey = new DHKeyMessage(2, (DHPublicKey) auth
 					.getLocalDHKeyPair().getPublic());
 			auth.setAuthenticationState(AuthenticationState.AWAITING_REVEALSIG);
 
 			logger.info("Sending D-H key.");
 			listener.injectMessage(dhKey.toUnsafeString());
 		default:
 			break;
 		}
 	}
 
 	private static void receivingDHKeyMessage(ConnContext ctx,
 			OTR4jListener listener, DHKeyMessage msg, String account,
 			String protocol) throws Exception {
 
 		if (!PolicyUtils.getAllowV2(listener.getPolicy(ctx))) {
 			logger.info("If ALLOW_V2 is not set, ignore this message.");
 			return;
 		}
 
 		Boolean replyRevealSig = false;
 
 		AuthenticationInfo auth = ctx.getAuthenticationInfo();
 		switch (auth.getAuthenticationState()) {
 		case AWAITING_DHKEY:
 			auth.setRemoteDHPublicKey(msg.getDhPublicKey());
 
 			// Computes MB = MACm1(gx, gy, pubB, keyidB)
 			logger.info("Computing M");
 			KeyPair keyPair = listener.getKeyPair(account, protocol);
 			auth.setLocalLongTermKeyPair(keyPair);
 			replyRevealSig = true;
 			break;
 		case AWAITING_SIG:
 			if (msg.getDhPublicKey().getY().equals(
 					auth.getRemoteDHPublicKey().getY())) {
 				replyRevealSig = true;
 			}
 			break;
 		default:
 			break;
 		}
 
 		if (replyRevealSig) {
 			int protocolVersion = 2;
 
 			MysteriousX x = auth.getLocalMysteriousX(false);
 			RevealSignatureMessage revealSignatureMessage = new RevealSignatureMessage(
 					protocolVersion, auth.getR(), x.hash, x.encrypted);
 
 			auth.setAuthenticationState(AuthenticationState.AWAITING_SIG);
 			logger.info("Sending Reveal Signature.");
 			listener.injectMessage(revealSignatureMessage.toUnsafeString());
 		}
 
 	}
 }
