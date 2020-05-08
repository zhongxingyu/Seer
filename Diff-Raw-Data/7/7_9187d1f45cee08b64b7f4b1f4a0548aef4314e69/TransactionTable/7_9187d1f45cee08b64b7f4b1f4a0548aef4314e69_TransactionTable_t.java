 package pl.edu.pjwstk.p2pp.transactions;
 
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.log4j.Logger;
 
 import pl.edu.pjwstk.p2pp.OutgoingMessagesListener;
 import pl.edu.pjwstk.p2pp.entities.P2PPEntity;
 import pl.edu.pjwstk.p2pp.messages.Acknowledgment;
 import pl.edu.pjwstk.p2pp.messages.Indication;
 import pl.edu.pjwstk.p2pp.messages.Message;
 import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
 import pl.edu.pjwstk.p2pp.messages.requests.Request;
 import pl.edu.pjwstk.p2pp.messages.responses.Response;
 import pl.edu.pjwstk.p2pp.objects.AddressInfo;
 import pl.edu.pjwstk.p2pp.util.Arrays;
 import pl.edu.pjwstk.p2pp.util.ByteUtils;
 import pl.edu.pjwstk.util.ByteArrayWrapper;
 
 /**
  * <p>
  * A transaction table which keeps track of in-progress transactions. The transaction can be of a request, response, or
  * an indication type. This table remebers also ended (terminated or failed) transactions in case of the same request
  * received once more. Transactions are created by using one of create methods (
  * {@link #createTransaction(pl.edu.pjwstk.p2pp.messages.P2PPMessage, TransactionListener, java.util.Vector, byte[], byte[])}} or
  * {@link #createTransactionAndFill(pl.edu.pjwstk.p2pp.messages.P2PPMessage, TransactionListener, java.util.Vector, byte[], byte[])}}).
  * </p>
  * TODO add handling for indications
  *
  * @author Maciej Skorupka s3874@pjwstk.edu.pl
  * @author Konrad Adamczyk conrad.adamczyk@gmail.com
  * @see Transaction
  */
 public class TransactionTable {
 
     private static Logger LOG = Logger.getLogger(TransactionTable.class);
 
     private final Object lock = new Object();
 
     /**
      * Map of request transactions identified by transactionID [Long transactionID, Transaction transaction]].
      */
     private Map<Long, Transaction> requestTransactionsMap = new ConcurrentHashMap<Long, Transaction>();
 
     /**
      * Map of response and responseACK transactions [ByteArrayWrapper sourceID, Hashtable transactionsMap[Long
      * transactionID, Transaction transaction]]. Keys are IDs of peers for which the transaction is. Value is another
      * map that holds transactionIDs (as Long[because transactionID is 4-bytes long but using int will generate problems
      * during comparison) and Transaction objects as values.
      */
     private Map<ByteArrayWrapper, Hashtable<Long, Transaction>> responseTransactionsMap = new ConcurrentHashMap<ByteArrayWrapper, Hashtable<Long, Transaction>>();
 
     /**
      * Map of indication transactions identified by transactionIDs [Long transactionID, Transaction transaction]].
      */
     private Map<Long, Transaction> indicationTransactionsMap = new ConcurrentHashMap<Long, Transaction>();
 
     /**
      * Map of ended (terminated or failed) response transactions. Kept in a case of the same request received once more.
      * Key is sourceID wrapped in ByteArrayWrapped object. Value is a hashtable with Long (transactionID) as key and
      * Transaction object as value.
      */
     private Map<ByteArrayWrapper, Queue<Long>> historyOfResponseTransactions = new ConcurrentHashMap<ByteArrayWrapper, Queue<Long>>();
 
     /**
      * Counter of transactions started by a local peer. Can be used as transactionID.
      */
     private AtomicInteger previousTransactionID = new AtomicInteger(0);
 
     @Override
     public String toString() {
         StringBuilder strb = new StringBuilder("TransactionTable[requests=");
         strb.append(this.requestTransactionsMap.size()).append(" responses=").append(this.responseTransactionsMap.size());
         strb.append(" indications=").append(this.indicationTransactionsMap.size()).append("history=");
         strb.append(this.historyOfResponseTransactions.size()).append("]");
         return strb.toString();
     }
 
     /**
      * Default constructor.
      */
     public TransactionTable() {}
 
     /**
      * <p>
      * Returns matching transaction from this table. This method is used when a Message was received, so it has that
      * type of parameter. Moreover, if there's no matching transaction in this table, it returns null. Matching is made
      * as defined in P2PP specification (draft 01, chapter 6.1)
      * </p>
      * <p>
      * <i>A transaction is identified by a source-ID, transaction-ID tuple, and a transaction-type triple. This is used
      * to match acknowledgements (ACK) to requests, responses and indications. However, the responses and responseACKs
      * are matched to requests using only source-ID and transaction-ID tuple. </i>
      * </p>
      * <p>
      * <i>The received requests are matched against the response transaction having the same source-ID and
      * transaction-ID where as the received responses are matched against request and response transactions.</i>
      * </p>
      *
      * @param message Message for which a transaction is searched.
      * @return Transaction matching given message. If there's no matching transaction, null is returned
      */
     public Transaction getTransaction(P2PPMessage message) {
 
             Transaction foundTransaction = null;
 
             if (LOG.isTraceEnabled()) {
                 LOG.trace("requestTransactionsMap=" + requestTransactionsMap);
                 LOG.trace("responseTransactionsMap=" + responseTransactionsMap);
             }
 
             // if given message is ACK
             if (message instanceof Acknowledgment) {
                 // TODO I'm not sure if this is how ACK should be processed, because I couldn't find it in spec
 
                 boolean[] messageType = message.getMessageType();
                 byte[] sourceID = message.getSourceID();
                 // byte[] responseID = message.getResponseID();
                 byte[] transactionIDAsBytes = message.getTransactionID();
                 long transactionIDAsLong = ByteUtils.bytesToLong(transactionIDAsBytes[0], transactionIDAsBytes[1],
                         transactionIDAsBytes[2], transactionIDAsBytes[3]);
 
                 // if it is an ACK for request, request transaction is searched
                 if (Arrays.equals(messageType, P2PPMessage.REQUEST_MESSAGE_TYPE)) {
                     if (LOG.isTraceEnabled()) {
                         LOG.trace("Searching for transaction id=" + transactionIDAsLong + "; messageType=Request");
                     }
                     Transaction temp = requestTransactionsMap.get(new Long(transactionIDAsLong));
 
                     if (temp != null) {
                         if (Arrays.equals(temp.getSourceID(), sourceID)) {
                             foundTransaction = temp;
                         }
                     }
 
                 } // if it is an aCK for response or responseACK
                 else if (message.isResponseACK() || message.isResponse()) {
 
                     ByteArrayWrapper wrappedSourceID = new ByteArrayWrapper(message.getSourceID());
                     if (LOG.isTraceEnabled()) {
                         LOG.trace("Searching for transaction id=" + transactionIDAsLong + "; messageType=Response; sourceID=" +
                                 wrappedSourceID);
                     }
                     Hashtable<Long, Transaction> transactionsMap = responseTransactionsMap.get(wrappedSourceID);
                     if (transactionsMap != null) {
                         foundTransaction = transactionsMap.get(new Long(transactionIDAsLong));
                     }
 
                 }
                 // TODO what about indication? is there an ACK for indication?
 
             } // if given message is response or responseACK
             else if (message instanceof Response) {
                 // looks for matching request transaction
                 byte[] transactionID = message.getTransactionID();
                 long transactionIDAsLong = ByteUtils.bytesToLong(transactionID[0], transactionID[1], transactionID[2],
                         transactionID[3]);
                 Transaction transactionWithSameID = requestTransactionsMap.get(new Long(transactionIDAsLong));
 
                 if (LOG.isTraceEnabled()) {
                     LOG.trace("transactionIDAsLong=" + transactionIDAsLong + " TransactionWithSameID="
                             + ((transactionWithSameID != null) ? ByteUtils.byteArrayToHexString(transactionWithSameID.getTransactionID()) : "null"));
                 }
 
                 // if there's transaction for given ID
                 if (transactionWithSameID != null) {
                     // compares sourceID from request and from response
                     byte[] receivedMessageSourceID = message.getSourceID();
                     if (Arrays.equals(transactionWithSameID.getSourceID(), receivedMessageSourceID)) {
                         foundTransaction = transactionWithSameID;
                     }
                     if (LOG.isTraceEnabled()) {
                         LOG.trace("receivedMessageSourceID=" + ByteUtils.byteArrayToHexString(receivedMessageSourceID) + "; foundTransaction=" +
                                 ((foundTransaction != null) ? ByteUtils.byteArrayToHexString(foundTransaction.getTransactionID()) : "null"));
                     }
                 }
 
                 // if request transaction wasn't found, looking for response transaction starts
                 // if (foundTransaction == null) {
                 // TODO
                 // }
 
             } // if given message is request
             else if (message instanceof Request) {
                 Request request = (Request) message;
 
                 ByteArrayWrapper wrappedSourceID = new ByteArrayWrapper(request.getSourceID());
 
                 Hashtable<Long, Transaction> transactionsForSourceID = responseTransactionsMap.get(wrappedSourceID);
 
                 if (transactionsForSourceID != null) {
                     
                     long transactionIDAsLong = request.getTransactionIDAsLong();
                     //if returned value is not a transaction mask
                     if(transactionIDAsLong != Long.MIN_VALUE)
                     foundTransaction = transactionsForSourceID.get(transactionIDAsLong);
                 }
             }
 
             return foundTransaction;
     }
 
     /**
      * Returns true if transaction for given request was already consumed. False otherwise. In other words, this table
      * checks if transaction is in history. History is a place where Transaction objects are stored after being
      * terminated or when they fail.
      *
      * @param request
      * @return
      */
     public boolean wasAlreadyConsumed(Request request) {
         boolean wasAlreadyConsumed = false;
         ByteArrayWrapper wrappedSourceID = new ByteArrayWrapper(request.getSourceID());
 
         // checks if there's in-progress transaction for given request
         Transaction transaction = getTransaction(request);
 
         // if there's in-progress transaction for given request
         if (transaction != null) {
             wasAlreadyConsumed = true;
         } else {
             // checks if history contains any transaction for given sourceID
             Queue<Long> transactionsForSourceID = historyOfResponseTransactions.get(wrappedSourceID);
 
             // if there are any
             if (transactionsForSourceID != null) {
                 // checks if history contains a transaction with ID of given request
                 if (transactionsForSourceID.contains(request.getTransactionIDAsLong())) {
                     wasAlreadyConsumed = true;
                 }
             }
         }
 
         if (LOG.isTraceEnabled()) {
             Long transactionId = (request.getTransactionID() == null) ? null : request.getTransactionIDAsLong();
             if (wasAlreadyConsumed) {
                 LOG.trace("Received request was already consumed. transactionID=" + transactionId
                         + " sourceID=" + ByteUtils.byteArrayToHexString(request.getSourceID()));
             } else {
                 LOG.trace("Received request wasn't consumed yet. transactionID=" + transactionId
                         + " sourceID=" + ByteUtils.byteArrayToHexString(request.getSourceID()));
             }
         }
 
         return wasAlreadyConsumed;
     }
 
     /**
      * Creates transaction for given command (it contains a message). If there's already matching transaction for given
      * command, this method returns null. The transaction is added to this table automatically. Transaction is also
      * started so that its message will be send during time slot ( {@link #onTimeSlot(pl.edu.pjwstk.p2pp.entities.P2PPEntity)}).
      *
      * @param message             Message for which the transaction will be created.
      * @param transactionListener Listener of this transaction.
      * @param addressInfos        Vector of AddressInfo objects with addresses of remote entity with which a transaction is created.
      * @param ownPeerID           Used for creating transaction that wants to know about own peerID to create ACKs.
      * @param remotePeerID        PeerID of a remote peer with which the created transaction will be.
      * @return Transaction created for given message. Null if there's already a transaction for given message.
      */
     public Transaction createTransaction(P2PPMessage message, TransactionListener transactionListener,
                                          Vector<AddressInfo> addressInfos, byte[] ownPeerID, byte[] remotePeerID) {
 
         // if there's a transaction for given message, transaction won't be created
         if (getTransaction(message) != null) {
             if (LOG.isTraceEnabled()) LOG.trace("There already is a transaction for " + message);
             return null;
         }
 
         if (LOG.isTraceEnabled()) LOG.trace("Transaction is being created.");
 
         // there is no matching transaction for given message, so it may be created
         Transaction transaction = null;
 
         synchronized (this.lock) {
 
             ByteArrayWrapper wrappedRemotePeerID = new ByteArrayWrapper(remotePeerID);
             if (message instanceof Request) {
 
                 // generates transactionID for transaction that will be created
                 long transactionID = getNextTransactionID();
 
                 // creates transaction with generated ID
                 transaction = new Transaction(message, transactionListener, addressInfos, ownPeerID, ByteUtils
                         .intToByteArray((int) transactionID));
                // adds transaction to transactions' map                
                 requestTransactionsMap.put(new Long(transactionID), transaction);
 
                 if (LOG.isTraceEnabled()) {
                     LOG.trace("Transaction was created. transactionID=" + transactionID + ". Request transactions map size=" +
                             requestTransactionsMap.size());
                 }
 
             } else if (message instanceof Response) {
 
                 Hashtable<Long, Transaction> transactionsMap = responseTransactionsMap.get(wrappedRemotePeerID);
                 // if given remotePeerID doesn't already exist in requests map
                 if (transactionsMap == null) {
                     // remotepeerid is added with value being new map
                     transactionsMap = new Hashtable<Long, Transaction>();
                     responseTransactionsMap.put(wrappedRemotePeerID, transactionsMap);
                 }
 
                 // transactionID is already generated by request originator (for which this response was created)
                 byte[] transactionIDAsBytes = message.getTransactionID();
                 long transactionIDAsLong = ByteUtils.bytesToLong(transactionIDAsBytes[0], transactionIDAsBytes[1],
                         transactionIDAsBytes[2], transactionIDAsBytes[3]);
 
                 // creates transaction with generated ID
                 transaction = new Transaction(message, transactionListener, addressInfos, ownPeerID, transactionIDAsBytes);
                 // adds transaction to transactions' map
                 transactionsMap.put(new Long(transactionIDAsLong), transaction);
 
             } else if (message instanceof Indication) {
 
                 // generates transactionID for transaction that will be created
                 long transactionID = getNextTransactionID();
 
                 // creates transaction with generated ID
                 transaction = new Transaction(message, transactionListener, addressInfos, ownPeerID, ByteUtils
                         .intToByteArray((int) transactionID));
                 // adds transaction to transactions' map
                 indicationTransactionsMap.put(new Long(transactionID), transaction);
             }
 
         }
 
         return transaction;
     }
 
     /**
      * Returns next transactionID. To be used when local peer is starting a transaction (i.e. is creating a request or
      * indication transaction).
      *
      * @return
      */
     private int getNextTransactionID() {
         return this.previousTransactionID.getAndIncrement();
     }
 
     /**
      * Does the same thing as {@link #createTransaction(pl.edu.pjwstk.p2pp.messages.P2PPMessage, TransactionListener, java.util.Vector, byte[], byte[])}
      * but if the transaction was created, message wrapped in command is filled with transactionID.
      *
      * @param message             Message for which the transaction will be created. It will be filled with transactionID.
      * @param transactionListener Listener of this transaction.
      * @param addressInfos        Vector of AddressInfo objects containing addresses of the remote entity. It will be used
      * @param ownPeerID           Used for creating transaction that wants to know about own peerID to create ACKs.
      * @param remotePeerID
      * @return Transaction to be returned. If null, there's already transaction for this command.
      */
     public Transaction createTransactionAndFill(P2PPMessage message, TransactionListener transactionListener,
                                                 Vector<AddressInfo> addressInfos, byte[] ownPeerID, byte[] remotePeerID) {
 
         Transaction transaction = createTransaction(message, transactionListener, addressInfos, ownPeerID, remotePeerID);
 
         if (transaction != null) {
             message.setTransactionID(transaction.getTransactionID());
         }
         return transaction;
 
     }
 
     /**
      * TODO Invoked when this object has time to send messages from transactions, handle retransmissions etc.
      *
      * @param localEntity
      */
     public void onTimeSlot(P2PPEntity localEntity) {
 
         // TODO check all transactions, remove ended (really? maybe all // completed were deleted somewhere before?),
         // send messages of transactions that didn't start
 
         // different order of methods?
 
         timeSlotsForTransactions(localEntity);
 
         findEndedTransactions();
 
     }
 
     /**
      * Gives time slots for transactions. Transactions may want to send something. They accomplish this by returning a
      * message in their time slot.
      *
      * @param localEntity
      */
     @SuppressWarnings("unchecked")
     private void timeSlotsForTransactions(P2PPEntity localEntity) {
         try {
 
             OutgoingMessagesListener outgoingListener = localEntity.getOutgoingListener();
 
             // iterates over request transactions and gives them time slots
             // Logger.log(LoggerTypeInfo.P2PP, LoggerLevelInfo.DEBUG, "TransactionTable.timeSlotsForTransactions()",
             // "\trequest transactions map size=" + requestTransactionsMap.size());
             //Set<Long> keySet = requestTransactionsMap.keySet();
             //Long[] keySetArray = keySet.toArray(new Long[0]);
 
             for (Map.Entry<Long,Transaction> entry : this.requestTransactionsMap.entrySet()) {
             //for (Long transactionIDAsLong : keySetArray) {
                 long transactionID = entry.getKey();
                 Transaction currentTransaction = entry.getValue();
 
                 if (LOG.isTraceEnabled()) LOG.trace("Giving time slot to transaction " + transactionID);
                 // gives time slot to current transaction
                 Message message = currentTransaction.onTimeSlot(this, localEntity);
                 // if transaction wants to send message, it is passed to outgoing listener
                if (message != null) {       
                    //sometimes returned message does not have set transactionID yet, below line fixed this issue.
                    if(((P2PPMessage)message).getTransactionID()==null)((P2PPMessage)message).setTransactionID(currentTransaction.getTransactionID());
                     outgoingListener.onSend(message);
                 }
             }
 
             // iterates over response transactions and gives them time slot
             Set<ByteArrayWrapper> arrayKeySet = responseTransactionsMap.keySet();
             ByteArrayWrapper[] arrayKeySetArray = arrayKeySet.toArray(new ByteArrayWrapper[0]);
             for (ByteArrayWrapper currentWrappedPeerID : arrayKeySetArray) {
 
                 Hashtable<Long, Transaction> currentTransactionsMap = responseTransactionsMap.get(currentWrappedPeerID);
 
                 // iterates over transactions with current peerID
                 Set<Long> currentKeySet = currentTransactionsMap.keySet();
                 Long[] currentKeySetArray = currentKeySet.toArray(new Long[0]);
                 for (Long currentTransactionID : currentKeySetArray) {
 
                     Transaction currentTransaction = currentTransactionsMap.get(currentTransactionID);
                     // gives time slot to current transaction
                     Message message = currentTransaction.onTimeSlot(this, localEntity);
                     // if transaction wants to send message, it is passed to outgoing listener
                     if (message != null) {
                         outgoingListener.onSend(message);
                     }
                 }
             }
 
             List<Long> indicationTransactionIDs = new ArrayList(this.indicationTransactionsMap.keySet());
             for (Long transactionIDAsLong : indicationTransactionIDs) {
                 if (LOG.isTraceEnabled()) LOG.trace("Giving time slot to transaction " + transactionIDAsLong);
                 Transaction currentTransaction = this.indicationTransactionsMap.get(transactionIDAsLong);
                 Message message = currentTransaction.onTimeSlot(this, localEntity);
                 if (message != null) {
                     outgoingListener.onSend(message);
                 }
             }
 
         } catch (Throwable e) {
             LOG.error("Error while running timeSlotsForTransactions", e);
         }
     }
 
     /**
      * Checks all transactions for being terminated or failed. If it is in one of those states, it is deleted from its
      * container and moved to history container.
      */
     @SuppressWarnings("unchecked")
     private void findEndedTransactions() {
         synchronized (this.lock) {
             try {
 
                 // iterates over request transactions and gives them time slot
                 Set<Long> requestKeySet = requestTransactionsMap.keySet();
                 Long[] requestKeySetArray = requestKeySet.toArray(new Long[requestKeySet.size()]);
                 for (Long transactionIDAsLong : requestKeySetArray) {
                     Transaction currentTransaction = requestTransactionsMap.get(transactionIDAsLong);
 
                     int transactionState = currentTransaction.getState();
 
                     // if transaction has ended
                     if (transactionState == Transaction.FAILURE_STATE || transactionState == Transaction.TERMINATED_STATE) {
                         requestTransactionsMap.remove(transactionIDAsLong);
                     }
                 }
 
                 // iterates over response transactions and gives them time slot
                 Set<ByteArrayWrapper> responseKeySet = responseTransactionsMap.keySet();
                 ByteArrayWrapper[] responseKeySetArray = responseKeySet.toArray(new ByteArrayWrapper[responseKeySet.size()]);
                 for (ByteArrayWrapper currentWrappedPeerID : responseKeySetArray) {
                     Hashtable<Long, Transaction> currentTransactionsMap = responseTransactionsMap.get(currentWrappedPeerID);
 
                     // iterates over transactions with current peerID
                     requestKeySet = currentTransactionsMap.keySet();
                     requestKeySetArray = requestKeySet.toArray(new Long[currentTransactionsMap.size()]);
                     for (Long currentTransactionID : requestKeySetArray) {
                         Transaction currentTransaction = currentTransactionsMap.get(currentTransactionID);
 
                         // if transaction has ended
                         int transactionState = currentTransaction.getState();
                         if (transactionState == Transaction.FAILURE_STATE || transactionState == Transaction.TERMINATED_STATE) {
                             Transaction deletedTransaction = currentTransactionsMap.remove(currentTransactionID);
                             if (LOG.isTraceEnabled()) {
                                 LOG.trace("deleted transaction=" + ByteUtils.byteArrayToHexString(deletedTransaction.getTransactionID()));
                             }
                             addToResponseHistory(currentWrappedPeerID, currentTransactionID);
                         }
                     }
                 }
 
                 List<Long> indicationTransactionIDs = new ArrayList(this.indicationTransactionsMap.keySet());
                 for (Long transactionIDAsLong : indicationTransactionIDs) {
                     Transaction currentTransaction = this.indicationTransactionsMap.get(transactionIDAsLong);
                     int transactionState = currentTransaction.getState();
                     if (transactionState == Transaction.FAILURE_STATE || transactionState == Transaction.TERMINATED_STATE) {
                         this.indicationTransactionsMap.remove(transactionIDAsLong);
                     }
                 }
 
             } catch (Throwable e) {
                 LOG.error("Error while running findEndedTransactions", e);
             }
         }
     }
 
     /**
      * Adds to history of response transactions.
      *
      * @param wrappedPeerID
      * @param transactionID
      */
     private void addToResponseHistory(ByteArrayWrapper wrappedPeerID, Long transactionID) {
 
         Queue<Long> transactionsForSourceID = historyOfResponseTransactions.get(wrappedPeerID);
 
         if (LOG.isTraceEnabled()) {
             LOG.trace("Method invoked for peerID=" + wrappedPeerID.getWrappedArrayAsHexString() + " transactionID=" + transactionID);
         }
 
         // if history has no entry for given peerID
         if (transactionsForSourceID == null) {
             if (LOG.isTraceEnabled()) LOG.trace("transaction with ID=" + transactionID + " copied to history (new peerID).");
             transactionsForSourceID = new ConcurrentLinkedQueue<Long>();
             transactionsForSourceID.add(transactionID);
             this.historyOfResponseTransactions.put(wrappedPeerID, transactionsForSourceID);
         } else {
             if (LOG.isTraceEnabled()) LOG.trace("transaction with ID=" + transactionID + " copied to history.");
             synchronized (transactionsForSourceID) { // Really, you'll never know when cme may strike..
                 if (transactionsForSourceID.size() == 100) {
                     transactionsForSourceID.poll();
                 }
                 transactionsForSourceID.add(transactionID);
             }
         }
 
     }
 
     /**
      * TODO Removes transaction matching given message.
      *
      * @param message P2PPMessage
      * @return boolean
      */
     public boolean removeMatchingTransaction(P2PPMessage message) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public void removeForPeerID(byte[] peerID) {
         ByteArrayWrapper wrappedPeerID = new ByteArrayWrapper(peerID);
         this.historyOfResponseTransactions.remove(wrappedPeerID);
         this.responseTransactionsMap.remove(wrappedPeerID);
         Collection<Long> transactionsToRemove = new LinkedList<Long>();
         for (Map.Entry<Long,Transaction> reqTrans : this.requestTransactionsMap.entrySet()) {
             Long id = reqTrans.getKey();
             Transaction transaction = reqTrans.getValue();
             if (Arrays.equals(transaction.getSourceID(), peerID)) {
                 transactionsToRemove.add(id);
             }
         }
         for (Long id : transactionsToRemove) {
             this.requestTransactionsMap.remove(id);
         }
     }
 
     /**
      * Resets transactions counter. Resets history and response and request transactions. Indication transactions aren't
      * deleted, because ACK for LeaveIndication may arrive.
      */
     public void leaveReset() {
         this.previousTransactionID.set(0);
         this.historyOfResponseTransactions.clear();
         this.requestTransactionsMap.clear();
         this.responseTransactionsMap.clear();
     }
 
 }
