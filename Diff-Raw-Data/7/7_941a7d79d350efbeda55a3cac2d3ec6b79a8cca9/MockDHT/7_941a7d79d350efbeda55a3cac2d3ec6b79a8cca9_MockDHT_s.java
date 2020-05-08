 package com.dustyneuron.bitprivacy.exchanger;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.CombinedTrade;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.TradeListing;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.TransactionSchema;
 import com.dustyneuron.bitprivacy.schemas.SchemaUtils;
 import com.google.bitcoin.core.Sha256Hash;
 
 public class MockDHT implements TradeDHT {
 	MockDHTStore store;
 	byte[] peerId;
 	
 	public MockDHT(MockDHTStore store) {
 		this.store = store;
 		peerId = Double.toString(Math.random()).getBytes();
 	}
 	
 	@Override
 	public void connect(String host) throws Exception {
 	}
 
 	@Override
 	public void disconnect() throws Exception {
 	}
 
 	@Override
 	public byte[] getPeerId() {
 		return peerId;
 	}
 
 	@Override
 	public String peerIdToString(byte[] peerId) {
 		return new String(peerId);
 	}
 	
 	class SimpleTradeListingKey implements TradeListingKey {
 		Sha256Hash h;
 		
 		@Override
 		public int compareTo(TradeListingKey o) {
 			return h.toBigInteger().compareTo(((SimpleTradeListingKey) o).h.toBigInteger());
 		}
 
 		@Override
 		public byte[] toByteArray() {
 			return h.getBytes();
 		}
 	}
 
 	class SimpleCombinedTradeKey implements CombinedTradeKey {
 		Sha256Hash h;
 		
 		@Override
 		public int compareTo(CombinedTradeKey o) {
 			return h.toBigInteger().compareTo(((SimpleCombinedTradeKey) o).h.toBigInteger());
 		}
 
 		@Override
 		public byte[] toByteArray() {
 			return h.getBytes();
 		}
 	}
 
 
 	@Override
 	public TradeListingKey getKey(TradeListing l) {
 		SimpleTradeListingKey k = new SimpleTradeListingKey();
 		k.h = SchemaUtils.getKey(l);
 		return k;
 	}
 
 	@Override
 	public TradeListingKey addTradeListing(TransactionSchema schema,
 			TradeListing listing) throws Exception {
 		SimpleTradeListingKey k = (SimpleTradeListingKey) getKey(listing);
 		store.map.put(k.h, listing);
 		Sha256Hash schemaKey = SchemaUtils.getKey(schema);
 		@SuppressWarnings("unchecked")
 		List<SimpleTradeListingKey> list = (List<SimpleTradeListingKey>) store.map.get(schemaKey);
 		list.add(k);
 		store.map.put(schemaKey, list);
 		return k;
 	}
 
 	@Override
 	public TradeListing getTradeListing(TradeListingKey key) throws Exception {
 		return (TradeListing) store.map.get(((SimpleTradeListingKey)key).h);
 	}
 
 	@Override
 	public List<TradeListingKey> getTradeListingKeys(TransactionSchema schema)
 			throws Exception {
 		Sha256Hash schemaKey = SchemaUtils.getKey(schema);
 		@SuppressWarnings("unchecked")
 		List<SimpleTradeListingKey> list = (List<SimpleTradeListingKey>) store.map.get(schemaKey);
 		return new ArrayList<TradeListingKey>(list);
 	}
 
 	@Override
 	public CombinedTradeKey getKey(CombinedTrade l) {
 		SimpleCombinedTradeKey k = new SimpleCombinedTradeKey();
 		k.h = SchemaUtils.getKey(l);
 		return k;
 	}
 
 	@Override
 	public CombinedTradeKey addCombinedTrade(TransactionSchema schema,
 			CombinedTrade combined) throws Exception {
 		SimpleCombinedTradeKey k = (SimpleCombinedTradeKey) getKey(combined);
 		store.map.put(k.h, combined);
 		Sha256Hash schemaDoubleKey = Sha256Hash.create(SchemaUtils.getKey(schema).getBytes());
 		@SuppressWarnings("unchecked")
 		List<SimpleCombinedTradeKey> list = (List<SimpleCombinedTradeKey>) store.map.get(schemaDoubleKey);
 		list.add(k);
 		store.map.put(schemaDoubleKey, list);
 		return k;
 	}
 
 	@Override
 	public CombinedTrade getCombinedTrade(CombinedTradeKey key)
 			throws Exception {
 		return (CombinedTrade) store.map.get(((SimpleCombinedTradeKey)key).h);
 	}
 
 	@Override
 	public List<CombinedTradeKey> getCombinedTradeKeys(TransactionSchema schema)
 			throws Exception {
 		Sha256Hash schemaDoubleKey = Sha256Hash.create(SchemaUtils.getKey(schema).getBytes());
 		@SuppressWarnings("unchecked")
 		List<SimpleCombinedTradeKey> list = (List<SimpleCombinedTradeKey>) store.map.get(schemaDoubleKey);
 		return new ArrayList<CombinedTradeKey>(list);
 	}
 
 	private Sha256Hash getSignedTxKey(CombinedTradeKey combinedKey, TradeListingKey individualKey) {
 		BigInteger a = ((SimpleCombinedTradeKey)combinedKey).h.toBigInteger();
 		BigInteger b = ((SimpleTradeListingKey)individualKey).h.toBigInteger();
 		return Sha256Hash.create(a.add(b).toByteArray());
 	}
 		
 	@Override
 	public void putSignedTx(CombinedTradeKey combinedKey,
 			TradeListingKey individualKey, byte[] txData) throws Exception {
 		Sha256Hash k = getSignedTxKey(combinedKey, individualKey);
 		store.map.put(k, txData);
 	}
 
 	@Override
 	public byte[] getSignedTx(CombinedTradeKey combinedKey,
 			TradeListingKey individualKey) throws Exception {
 		Sha256Hash k = getSignedTxKey(combinedKey, individualKey);
 		return (byte[]) store.map.get(k);
 	}
 
 }
