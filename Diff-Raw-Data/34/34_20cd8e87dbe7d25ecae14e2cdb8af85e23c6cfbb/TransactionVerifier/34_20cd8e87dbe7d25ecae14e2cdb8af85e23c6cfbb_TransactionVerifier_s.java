 package com.dustyneuron.bitprivacy.schemas;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.spongycastle.crypto.params.KeyParameter;
 import org.spongycastle.util.Arrays;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItem;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ReferenceType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.SinglePartyData;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Trade;
 import com.dustyneuron.bitprivacy.bitcoin.WalletUtils;
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.AddressFormatException;
 import com.google.bitcoin.core.Block;
 import com.google.bitcoin.core.ECKey;
 import com.google.bitcoin.core.NetworkParameters;
 import com.google.bitcoin.core.Script;
 import com.google.bitcoin.core.ScriptException;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.bitcoin.core.Transaction;
 import com.google.bitcoin.core.TransactionConfidence;
 import com.google.bitcoin.core.TransactionInput;
 import com.google.bitcoin.core.TransactionOutPoint;
 import com.google.bitcoin.core.TransactionOutput;
 import com.google.bitcoin.core.UnsafeByteArrayOutputStream;
 import com.google.bitcoin.core.Wallet;
 import com.google.bitcoin.core.Transaction.SigHash;
 import com.google.bitcoin.core.WrongNetworkException;
 import com.google.common.base.Preconditions;
 
 public class TransactionVerifier {
 	
 	public static Transaction createTransaction(List<SinglePartyData> allData, BlockDownloader blockDownloader, NetworkParameters params) throws Exception {
 		
 		Transaction transaction = new Transaction(params);
 		for (SinglePartyData party : allData) {
 			for (DataItem d : party.getDataList()) {
 				if (d.getReference().getRefType() == ReferenceType.INPUT) {
 					Block b = blockDownloader.downloadBlock(new Sha256Hash(d.getBlockId()));
 					Transaction prevTx = WalletUtils.findTransaction(b, new Sha256Hash(d.getTxId()));
 					transaction.addInput(prevTx.getOutput(d.getOutputIndex()));
 				} else {
 					if (!d.hasValue()) {
 						throw new Exception("unimplemented... different party sets output address to the party that sets the value");
 					}
 					BigInteger outputValue = SchemaUtils.readBigInteger(d.getValue());
 					transaction.addOutput(outputValue, new Address(params, d.getAddress()));
 				}
 			}
 		}
 		return transaction;
 	}
 	
 	private static boolean doesTxMatchPartyData(Transaction signRequest, List<SinglePartyData> partyData, Wallet wallet) throws ScriptException, WrongNetworkException, AddressFormatException {
 		
         List<DataItem> unmatchedInputs = new ArrayList<DataItem>();
         List<DataItem> unmatchedOutputs = new ArrayList<DataItem>();
         for (SinglePartyData party : partyData) {
         	for (DataItem d : party.getDataList()) {
         		if (d.getReference().getRefType() == ReferenceType.INPUT) {
         			unmatchedInputs.add(d);
         		} else if (d.getReference().getRefType() == ReferenceType.OUTPUT){
         			unmatchedOutputs.add(d);
         		}
         	}
         }
         for (TransactionInput input : signRequest.getInputs()) {
             TransactionOutPoint outpoint = WalletUtils.getConnectedOutPoint(input, wallet);
             if (outpoint != null) {
             	DataItem matchingItem = null;
                 for (DataItem d : unmatchedInputs) {
                 	if (outpoint.getHash().equals(new Sha256Hash(d.getTxId()))
                 			&& (outpoint.getIndex() == d.getOutputIndex())) {
                 		matchingItem = d;
                 		break;
                 	}
                 }
                 if (matchingItem != null) {
                 	unmatchedInputs.remove(matchingItem);
                 }
             }
         }
         if (unmatchedInputs.size() != 0) {
         	System.err.println("Transaction didn't contain all the inputs we were going to sign");
         	return false;
         }
         for (TransactionOutput output : signRequest.getOutputs()) {
         	DataItem matchingItem = null;
             for (DataItem d : unmatchedOutputs) {
             	if (output.getScriptPubKey().getToAddress().equals(new Address(wallet.getNetworkParameters(), d.getAddress()))
             			&& (output.getValue().compareTo(SchemaUtils.readBigInteger(d.getValue())) == 0)) {
             		matchingItem = d;
             		break;
             	}
             }
             if (matchingItem != null) {
             	unmatchedOutputs.remove(matchingItem);
             }
         }
         if (unmatchedOutputs.size() != 0) {
         	System.err.println("Transaction didn't contain all our specified outputs");
         	return false;
         }
 		
 		return true;
 	}
 	
 	public static boolean isSignRequestValid(Transaction signRequest, Trade serverTrade, Trade myTradeRequest, Wallet wallet, BlockDownloader blockDownloader) throws Exception {
 		
 		if (!SchemaUtils.areIdentical(serverTrade.getSchema(), myTradeRequest.getSchema())) {
 			System.err.println("Server sent us a trade schema different from our trade schema");
 			return false;
 		}
 		
		if (!SchemaUtils.isInitialListingValid(serverTrade.getSchema(), serverTrade.getAllPartiesDataList())) {
			System.err.println("Server sent us trade data which doesn't pass the schema");
 			return false;
 		}
 		
 		Transaction unsigned = createTransaction(serverTrade.getAllPartiesDataList(), blockDownloader, wallet.getNetworkParameters());
 		
 		Transaction signRequestCopy = new Transaction(wallet.getNetworkParameters());
 		for (TransactionOutput o : signRequest.getOutputs()) {
 			signRequestCopy.addOutput(o);
 		}
 		for (TransactionInput i : signRequest.getInputs()) {
 			signRequestCopy.addInput(new TransactionInput(wallet.getNetworkParameters(), signRequestCopy, null, i.getOutpoint()));
 		}
 		
 		if (!Arrays.areEqual(unsigned.bitcoinSerialize(), signRequestCopy.bitcoinSerialize())) {
 			System.err.println("Transaction didn't match our constructed version based on the server's party data");
 			return false;
 		}
 		
 		// if server also sent us all the party data, we could generate our own transaction trivially,
 		// and confirm the only thing that differed was the signing data.
 		
 		return doesTxMatchPartyData(signRequest, myTradeRequest.getAllPartiesDataList(), wallet);
 	}
 	
 	public static Transaction doSignRequest(Transaction t, Trade myTradeRequest, Wallet wallet) throws ScriptException {
 		
 		final SigHash hashType = Transaction.SigHash.ALL;
 		final KeyParameter aesKey = null;
 		
     	List<TransactionInput> inputs = t.getInputs();
     	List<TransactionOutput> outputs = t.getOutputs();
     	
         Preconditions.checkState(inputs.size() > 0);
         Preconditions.checkState(outputs.size() > 0);
         
         List<DataItem> inputsData = new ArrayList<DataItem>();
         for (SinglePartyData party : myTradeRequest.getAllPartiesDataList()) {
         	for (DataItem d : party.getDataList()) {
         		if (d.getReference().getRefType() == ReferenceType.INPUT) {
         			inputsData.add(d);
         		}
         	}
         }
         if (inputsData.size() == 0) {
         	System.out.println("This trade doesn't actually need us to sign anything, returning the same tx");
         	return t;
         }
         
         final NetworkParameters params = wallet.getNetworkParameters();
 
         byte[][] signatures = new byte[inputs.size()][];
         ECKey[] signingKeys = new ECKey[inputs.size()];
         for (int i = 0; i < inputs.size(); i++) {
             TransactionInput input = inputs.get(i);            
             if (input.getScriptBytes().length == 0) {
 	            TransactionOutPoint outpoint = WalletUtils.getConnectedOutPoint(input, wallet);
 	            if (outpoint == null) {
 	            	System.out.println("null outpoint for input #" + i + ", assuming we don't have key anyway");
 	            }
 	            else {
 	            	DataItem matchingItem = null;
 	                for (DataItem d : inputsData) {
 	                	if (outpoint.getHash().equals(new Sha256Hash(d.getTxId()))
 	                			&& (outpoint.getIndex() == d.getOutputIndex())) {
 	                		matchingItem = d;
 	                		break;
 	                	}
 	                }
 	                
 	            	if (matchingItem != null) {
 	            		inputsData.remove(matchingItem);
 	            		
 			            ECKey key = outpoint.getConnectedKey(wallet);
 			            if (key == null) {
 			            	System.out.println("we do not have the key for input #" + i);
 			            }
 			            else {
 				            // Keep the key around for the script creation step below.
 				            signingKeys[i] = key;
 				            // The anyoneCanPay feature isn't used at the moment.
 				            boolean anyoneCanPay = false;
 				            byte[] connectedPubKeyScript = WalletUtils.getConnectedOutput(input, wallet).getScriptBytes();
 				            Sha256Hash hash = t.hashTransactionForSignature(i, connectedPubKeyScript, hashType, anyoneCanPay);
 				
 				            // Now sign for the output so we can redeem it. We use the keypair to sign the hash,
 				            // and then put the resulting signature in the script along with the public key (below).
 				            try {
 				                // Usually 71-73 bytes.
 				                ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(73);
 				                bos.write(key.sign(hash, aesKey).encodeToDER());
 				                bos.write((hashType.ordinal() + 1) | (anyoneCanPay ? 0x80 : 0));
 				                signatures[i] = bos.toByteArray();
 				                bos.close();
 				            } catch (IOException e) {
 				                throw new RuntimeException(e);  // Cannot happen.
 				            }
 			            }
 	            	}
 	            }
             }
         }
         
         if (inputsData.size() != 0) {
         	System.err.println("We expected to sign more inputs than this... bogus transaction");
         	return null;
         }
 
         // Now we have calculated each signature, go through and create the scripts. Reminder: the script consists:
         // 1) For pay-to-address outputs: a signature (over a hash of the simplified transaction) and the complete
         //    public key needed to sign for the connected output. The output script checks the provided pubkey hashes
         //    to the address and then checks the signature.
         // 2) For pay-to-key outputs: just a signature.
         
         Transaction newTx = new Transaction(params);
         newTx.getConfidence().setSource(TransactionConfidence.Source.SELF);
         for (TransactionOutput o : t.getOutputs()) {
         	newTx.addOutput(o);
         }
         
         for (int i = 0; i < inputs.size(); i++) {
             TransactionInput input = inputs.get(i);
             if (input.getScriptBytes().length == 0) {
 	            ECKey key = signingKeys[i];
 	            if (key == null) {
 	            	newTx.addInput(input);
 	            }
 	            else {
 		            Script scriptPubKey = WalletUtils.getConnectedOutput(input, wallet).getScriptPubKey();
 		            byte[] scriptBytes;
 		            if (scriptPubKey.isSentToAddress()) {
 		            	scriptBytes = Script.createInputScript(signatures[i], key.getPubKey());
 		            } else if (scriptPubKey.isSentToRawPubKey()) {
 		            	scriptBytes = Script.createInputScript(signatures[i]);
 		            } else {
 		                // Should be unreachable - if we don't recognize the type of script we're trying to sign for, we should
 		                // have failed above when fetching the key to sign with.
 		                throw new RuntimeException("Do not understand script type: " + scriptPubKey);
 		            }
 		            //input.setScriptBytes(scriptBytes);
 		            newTx.addInput(new TransactionInput(params, newTx, scriptBytes, input.getOutpoint()));
 	            }
             }
             else {
             	newTx.addInput(input);
             }
         }
         return newTx;
 	}
 	
 	/*
 	 * Danger: this just signs as many inputs as possible
 	 */
     public static Transaction dangerousSignMany(Transaction t, Wallet wallet) throws Exception {
     	
 		final SigHash hashType = Transaction.SigHash.ALL;
 		final KeyParameter aesKey = null;
     	
     	List<TransactionInput> inputs = t.getInputs();
     	List<TransactionOutput> outputs = t.getOutputs();
     	
         Preconditions.checkState(inputs.size() > 0);
         Preconditions.checkState(outputs.size() > 0);
         
         final NetworkParameters params = wallet.getNetworkParameters();
 
         // I don't currently have an easy way to test other modes work, as the official client does not use them.
         Preconditions.checkArgument(hashType == SigHash.ALL, "Only SIGHASH_ALL is currently supported");
 
         // The transaction is signed with the input scripts empty except for the input we are signing. In the case
         // where addInput has been used to set up a new transaction, they are already all empty. The input being signed
         // has to have the connected OUTPUT program in it when the hash is calculated!
         //
         // Note that each input may be claiming an output sent to a different key. So we have to look at the outputs
         // to figure out which key to sign with.
 
         byte[][] signatures = new byte[inputs.size()][];
         ECKey[] signingKeys = new ECKey[inputs.size()];
         for (int i = 0; i < inputs.size(); i++) {
             TransactionInput input = inputs.get(i);
             if (input.getScriptBytes().length == 0) {
             	
 	            TransactionOutPoint outpoint = WalletUtils.getConnectedOutPoint(input, wallet);
 	            if (outpoint == null) {
 	            	System.out.println("null outpoint for input #" + i + ", assuming we don't have key anyway");
 	            }
 	            else {
 		            ECKey key = outpoint.getConnectedKey(wallet);
 		            if (key == null) {
 		            	System.out.println("we do not have the key for input #" + i);
 		            }
 		            else {
 			            // Keep the key around for the script creation step below.
 			            signingKeys[i] = key;
 			            // The anyoneCanPay feature isn't used at the moment.
 			            boolean anyoneCanPay = false;
 			            byte[] connectedPubKeyScript = WalletUtils.getConnectedOutput(input, wallet).getScriptBytes();
 			            Sha256Hash hash = t.hashTransactionForSignature(i, connectedPubKeyScript, hashType, anyoneCanPay);
 			
 			            // Now sign for the output so we can redeem it. We use the keypair to sign the hash,
 			            // and then put the resulting signature in the script along with the public key (below).
 			            try {
 			                // Usually 71-73 bytes.
 			                ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(73);
 			                bos.write(key.sign(hash, aesKey).encodeToDER());
 			                bos.write((hashType.ordinal() + 1) | (anyoneCanPay ? 0x80 : 0));
 			                signatures[i] = bos.toByteArray();
 			                bos.close();
 			            } catch (IOException e) {
 			                throw new RuntimeException(e);  // Cannot happen.
 			            }
 		            }
 	            }
             }
         }
 
         // Now we have calculated each signature, go through and create the scripts. Reminder: the script consists:
         // 1) For pay-to-address outputs: a signature (over a hash of the simplified transaction) and the complete
         //    public key needed to sign for the connected output. The output script checks the provided pubkey hashes
         //    to the address and then checks the signature.
         // 2) For pay-to-key outputs: just a signature.
         
         Transaction newTx = new Transaction(params);
         newTx.getConfidence().setSource(TransactionConfidence.Source.SELF);
         for (TransactionOutput o : t.getOutputs()) {
         	newTx.addOutput(o);
         }
         
         for (int i = 0; i < inputs.size(); i++) {
             TransactionInput input = inputs.get(i);
             if (input.getScriptBytes().length == 0) {
 	            ECKey key = signingKeys[i];
 	            if (key == null) {
 	            	newTx.addInput(input);
 	            }
 	            else {
 		            Script scriptPubKey = WalletUtils.getConnectedOutput(input, wallet).getScriptPubKey();
 		            byte[] scriptBytes;
 		            if (scriptPubKey.isSentToAddress()) {
 		            	scriptBytes = Script.createInputScript(signatures[i], key.getPubKey());
 		            } else if (scriptPubKey.isSentToRawPubKey()) {
 		            	scriptBytes = Script.createInputScript(signatures[i]);
 		            } else {
 		                // Should be unreachable - if we don't recognize the type of script we're trying to sign for, we should
 		                // have failed above when fetching the key to sign with.
 		                throw new RuntimeException("Do not understand script type: " + scriptPubKey);
 		            }
 		            //input.setScriptBytes(scriptBytes);
 		            newTx.addInput(new TransactionInput(params, newTx, scriptBytes, input.getOutpoint()));
 	            }
             }
             else {
             	newTx.addInput(input);
             }
         }
         return newTx;
     }
 }
