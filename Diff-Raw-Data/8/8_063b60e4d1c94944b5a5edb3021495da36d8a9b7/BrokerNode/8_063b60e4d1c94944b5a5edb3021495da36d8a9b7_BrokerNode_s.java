 package node;
 
 import java.util.Collection;
 import java.util.Set;
 import java.util.UUID;
 
 import data.Data;
 
 public interface BrokerNode extends Node {
 
 	public static class Transaction
 	{
 		private UUID id;
 		private UUID consumerId;
 		private UUID providerId;
 		private UUID dataId;
 		private int paymentAmount;
 		private Set<UUID> brokerNodeIds;
 		
 		public Transaction(
 				UUID consumerId, 
 				UUID providerId, 
 				UUID dataId, 
 				int paymentAmount, 
 				Set<UUID> brokerNodeIds) {
 			super();
 			this.id = UUID.randomUUID();
 			this.consumerId = consumerId;
 			this.providerId = providerId;
 			this.dataId = dataId;
 			this.paymentAmount = paymentAmount;
 			this.brokerNodeIds = brokerNodeIds;
 		}
 		
 		public UUID getId()
 		{
 			return id;
 		}
 		
 		public UUID getConsumerId() {
 			return consumerId;
 		}
 
 		public UUID getProviderId() {
 			return providerId;
 		}
 
 		public UUID getDataId() {
 			return dataId;
 		}
 
 		public int getPaymentAmount() {
 			return paymentAmount;
 		}
 		
 		public Set<UUID> getBrokerNodeIds()
 		{
 			return brokerNodeIds;
 		}
 	}
 	
 	public static class DecryptionKeySecretShare extends Data
 	{
 		public DecryptionKeySecretShare(byte[] data) {
 			super(data);
 		}
 	}
 
 	/**
	 * Register a transaction.  This method is called by both the data provider
	 * and consumer
 	 */
 	public void registerTransaction(Transaction transaction);
 
 	/**
 	 * Verify that the encrypted data hash matches the expected value. If the
 	 * hash matches forward a share of the payment authorization to the data 
	 * provider and return the decryption key to the consumer.
 	 */
 	public DecryptionKeySecretShare finalizeTransaction(
 		UUID transactionId, Collection<CurrencyUnit> payment, Data encryptedDataHash);	
 }
