 package com.dustyneuron.bitprivacy.schemas;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItem;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItemSpecifier;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.PartyType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.SinglePartyData;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Trade;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.TransactionSchema;
 import com.dustyneuron.bitprivacy.schemas.ConstraintsVerifier.TradeConstraintsStatus;
 import com.dustyneuron.bitprivacy.schemas.ConstraintsVerifier.VariableValues;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.protobuf.ByteString;
 import com.google.protobuf.GeneratedMessage;
 
 public class SchemaUtils {
 	
 	public static Sha256Hash getSchemaKey(TransactionSchema schema) {
 		return Sha256Hash.create(schema.toByteArray());
 	}
 		
 	public static class TradeCombinationResult {
 		public Trade trade;
 		public boolean passedConstraints;
 		public boolean complete;
 	}
 	
 	private static boolean isSchemaSensible(TransactionSchema schema) {
 		// TODO: implement
 		System.out.println("isSchemaSensible not implemented yet");
 		return true;
 	}
 	
	public static boolean isInitialListingValid(TransactionSchema schema, List<SinglePartyData> data) throws Exception {
 		if (!isSchemaSensible(schema)) {
 			System.out.println("Error: schema is not sensible, refusing to list!");
 			return false;
 		}
 		
 		Trade original = Trade.newBuilder()
 				.setSchema(schema)
 				.setTimestamp(new Date().getTime())
 				.build();
 		
 		Trade newTrade = Trade.newBuilder()
 				.setSchema(schema)
 				.setTimestamp(new Date().getTime())
 				.addAllAllPartiesData(data)
 				.build();
 		
 		TradeCombinationResult result = tryAddTrade(original, newTrade);
 		if (result == null) {
 			return false;
 		}
 		if (!result.passedConstraints) {
 			return false;
 		}
		if (result.complete) {
			return false;
 		}
 		return true;
 	}
 
 	public static TradeCombinationResult tryAddTrade(Trade original, Trade newTrade) throws Exception {
 		
 		// we assume that 'original' is valid, within func limits, etc
 		
 		if (!SchemaUtils.areIdentical(original.getSchema(), newTrade.getSchema())) {
 			System.out.println("Cannot add trade, schemas differ");
 			return null;
 		}
 		System.out.println("schemas match, good");
 		
 		List<SinglePartyData> partiesData = newTrade.getAllPartiesDataList();
 		for (SinglePartyData partyData : partiesData) {
 			if (!isPartyDataValid(original.getSchema(), partyData)) {
 				System.out.println("Cannot add party data to trade as it doesn't validate against the schema");
 				return null;
 			}
 		}
 		System.out.println("validated extra party data against the schema ok");
 		
 		Trade.Builder provisionalTradeBuilder = Trade.newBuilder(original);
 		for (SinglePartyData partyData : partiesData) {
 			provisionalTradeBuilder.addAllPartiesData(partyData);
 		}
 		Trade provisionalTrade = provisionalTradeBuilder.build();
 		TradeCombinationResult result = new TradeCombinationResult();
 		result.trade = provisionalTrade;
 		result.complete = false;
 		result.passedConstraints = false;
 		
 		VariableValues variableValues = ConstraintsVerifier.evaluateVariables(provisionalTrade);
 		
 		System.out.println("evaluated variables ok");
 		
 		TradeConstraintsStatus forEachStatus = ConstraintsVerifier.evaluateForEachConstraints(provisionalTrade, variableValues, provisionalTrade.getSchema().getConstraintsList());
 		if (forEachStatus == TradeConstraintsStatus.CONSTRAINTS_BROKEN) {
 			System.out.println("Cannot add party data to trade as it breaks foreach constraints limits");
 			return result;
 		}
 		
 		System.out.println("passed foreach constraints ok");
 		
 		TradeConstraintsStatus constraintsStatus = ConstraintsVerifier.evaluateConstraints(variableValues, provisionalTrade.getSchema().getConstraintsList());
 		
 		if (constraintsStatus == TradeConstraintsStatus.CONSTRAINTS_BROKEN) {
 			System.out.println("Cannot add party data to trade as it breaks standard constraints limits");
 			return result;
 		}
 		System.out.println("passed standard constraints ok");
 		result.passedConstraints = true;
 		
 		TradeConstraintsStatus completeStatus = ConstraintsVerifier.evaluateConstraints(variableValues, provisionalTrade.getSchema().getCompletionRequirementsList());
 		
 		result.complete = (completeStatus == TradeConstraintsStatus.CONSTRAINTS_OK);
 		
 		System.out.println("Completion status = " + result.complete);
 		return result;
 	}
 		
 	public static boolean areIdentical(GeneratedMessage a, GeneratedMessage b) {
 		return Arrays.equals(a.toByteArray(), b.toByteArray());
 	}
 	
 	private static boolean dataItemMatchesSpecifier(DataItem item, DataItemSpecifier specifier) throws Exception {
 		System.out.println("dataItemMatchesSpecifier:");
 		System.out.println(item.toString());		
 		
 		if (!areIdentical(item.getReference(), specifier.getReference())) {
 			System.out.println("data item ref does not match specifier ref");
 			return false;
 		}
 				
 		if (specifier.getFixAddress()) {
 			switch (specifier.getReference().getRefType()) {
 				case INPUT:
 					if (!item.hasTxId() || !item.hasBlockId() || !item.hasOutputIndex()) {
 						System.out.println("data item doesn't have required fields for INPUT");
 						return false;
 					}
 					if (item.hasAddress() || item.hasValue()) {
 						System.out.println("data item has too many fields for INPUT");
 						return false;
 					}
 					break;
 				case OUTPUT:
 					if (!item.hasAddress() || !item.hasValue()) {
 						System.out.println("data item doesn't have required fields for OUTPUT");
 						return false;
 					}
 					if (item.hasTxId() || item.hasBlockId() || item.hasOutputIndex()) {
 						System.out.println("data item has too many fields for OUTPUT");
 						return false;
 					}
 					break;
 				default:
 					throw new Exception("unhandled ref type");
 			}
 		}
 		else {
 			if (item.hasTxId() || item.hasBlockId() || item.hasOutputIndex() || item.hasAddress() || item.hasValue()) {
 				System.out.println("data item has too many fields for value");
 				return false;
 			}
 		}
 
 		return true;
 	}
 	
 	private static boolean isPartyDataValid(TransactionSchema schema, SinglePartyData data) throws Exception {
 		
 		System.out.println("isPartyDataValid:");
 		System.out.println(data.toString());
 		
 		int partyTypeIdx = data.getPartyIdx();
 		if (schema.getPartyTypesCount() <= partyTypeIdx) {
 			System.out.println("party data type idx " + partyTypeIdx + " is invalid");
 			return false;
 		}
 		
 		// This checks that there is a 1-1 match between DataItems and Specifiers
 		//
 		List<DataItem> unvalidatedItems = new ArrayList<DataItem>(data.getDataList());
 		PartyType partyType = schema.getPartyTypes(partyTypeIdx);
 		for (DataItemSpecifier specifier : partyType.getSpecifiersList()) {
 			DataItem match = null;
 			for (DataItem item : unvalidatedItems) {
 				if (dataItemMatchesSpecifier(item, specifier)) {
 					match = item;
 					break;
 				}
 			}
 			if (match == null) {
 				System.out.println("Specifier had no matching DataItem");
 				System.out.println(specifier.toString());
 				return false;
 			} else {
 				System.out.println("Matched specifier to DataItem");
 				System.out.println(match.toString());
 				unvalidatedItems.remove(match);
 			}
 		}
 		if (unvalidatedItems.size() > 0) {
 			System.out.println("not all DataItems validated against the schema PartyType");
 			for (DataItem i : unvalidatedItems) {
 				System.out.println(i.toString());
 			}
 			return false;
 		}
 
 		return true;
 	}
 
 
 	public static BigInteger readBigInteger(TransactionSchemaProtos.BigInteger btcValue) {
 		byte[] orig = btcValue.getData().toByteArray();
 		byte[] list = orig;
 		
 		//System.out.println("readBigInteger " + new String(Hex.encodeHex(orig)));
 		return new BigInteger(list);
 	}
 	
 	public static TransactionSchemaProtos.BigInteger writeBigInteger(BigInteger v) {
 		//System.out.println("writeBigInteger " + new String(Hex.encodeHex(v.toByteArray())));
 		
 		TransactionSchemaProtos.BigInteger btcValue = TransactionSchemaProtos.BigInteger.newBuilder()
 				.setData(ByteString.copyFrom(v.toByteArray()))
 				.build();
 		
 		return btcValue;
 	}
 }
