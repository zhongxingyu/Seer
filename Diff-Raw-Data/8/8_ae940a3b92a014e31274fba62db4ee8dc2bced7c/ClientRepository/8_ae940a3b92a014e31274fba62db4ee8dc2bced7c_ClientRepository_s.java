 /*
  * Copyright 2008-2010 Amazon Technologies, Inc. or its affiliates.
  * Amazon, Amazon.com and Carbonado are trademarks or registered trademarks
  * of Amazon Technologies, Inc. or its affiliates.  All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.amazon.carbonado.repo.dirmi;
 
 import java.io.IOException;
 
 import java.rmi.RemoteException;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.cojen.dirmi.Pipe;
 import org.cojen.dirmi.UnimplementedMethodException;
 
 import org.cojen.dirmi.util.Wrapper;
 
 import com.amazon.carbonado.MalformedTypeException;
 import com.amazon.carbonado.Repository;
 import com.amazon.carbonado.RepositoryException;
 import com.amazon.carbonado.Storable;
 import com.amazon.carbonado.Storage;
 import com.amazon.carbonado.SupportException;
 
 import com.amazon.carbonado.capability.Capability;
 import com.amazon.carbonado.capability.RemoteProcedure;
 import com.amazon.carbonado.capability.RemoteProcedureCapability;
 import com.amazon.carbonado.capability.ResyncCapability;
 
 import com.amazon.carbonado.layout.Layout;
 
 import com.amazon.carbonado.sequence.SequenceValueProducer;
 
 import com.amazon.carbonado.spi.AbstractRepository;
 
 import com.amazon.carbonado.txn.TransactionManager;
 import com.amazon.carbonado.txn.TransactionMonitor;
 import com.amazon.carbonado.txn.TransactionScope;
 
 /**
  * Creates a client repository from a {@link RemoteRepository}, as served by
  * {@link RemoteRepositoryServer}.
  * 
  * If the connection is lost, the repository can be reconnected using 'reconnect'
  * and the Storage and SequenceValueProducer references will be retained.
  * All transactions will be invalid following a disconnect
  *
  * @author Brian S O'Neill
  * @author Olga Kuznetsova
  */
 public class ClientRepository extends AbstractRepository<RemoteTransaction>
     implements RemoteProcedureCapability
 {
     /**
      * Returns client access to a remote repository server.
      *
      * @return ClientRepository instance wrapping the remote repository
      */
     public static ClientRepository from(RemoteRepository remote) throws RepositoryException {
         return from(null, remote, null);
     }
     
     /**
      * Returns client access to a remote repository server.
      *
      * @param name name of repository; pass null to retrieve name from remote endpoint
      * @return ClientRepository instance wrapping the remote repository
      */
     public static ClientRepository from(String name, RemoteRepository remote)
         throws RepositoryException
     {
         return from(name, remote, null);
     }
 
     /**
      * Returns client access to a remote repository server.
      *
      * @param name name of repository; pass null to retrieve name from remote endpoint
      * @param monitor optional transaction monitor
      * @return ClientRepository instance wrapping the remote repository
      */
     public static ClientRepository from(String name, RemoteRepository remote,
                                         TransactionMonitor monitor)
         throws RepositoryException
     {
         if (name == null) {
             name = remote.getName();
         }
         return new ClientRepository(name, remote, monitor);
     }
 
     /**
      * Reconnects the repository in case of a disconnect. 
      * Storage and SequenceValueProducer references will be retained. 
      * All transactions that were in process will be broken invalid after the disconnect.
      */
    public void reconnect(RemoteRepository remote) throws RepositoryException {
         for (Storage s : allStorage()) {
             if (s != null) {
                 ClientStorage curr = (ClientStorage) storageFor(s.getStorableType());
                 curr.reconnect(remoteStorageFor(remote, s.getStorableType()));
             }
         }
         for (String p : mSequenceNames.keySet()) {
             if (p != null) {
                 RemoteSequenceValueProducer producer = remote.getSequenceValueProducer(p);
                 try {
                     ClientSequenceValueProducer currProducer =
                         (ClientSequenceValueProducer) getSequenceValueProducer(p);
                     currProducer.reconnect(producer);
                 } catch (RepositoryException e) {
                     mSequenceNames.remove(p);
                     throw e;
                 }
             }
         }
         mRepository = remote;
     }
 
     private volatile RemoteRepository mRepository;
     private final TransactionManager<RemoteTransaction> mTxnMgr;
     private final ConcurrentHashMap<String, String> mSequenceNames;
 
     private volatile RemoteProcedureExecutor mProcedureExecutor;
 
     RemoteRepository getRemoteRepository() {
         return mRepository;
     }
 
     private ClientRepository(String name, RemoteRepository remote, TransactionMonitor monitor) {
         super(name);
         mRepository = remote;
         mSequenceNames = new ConcurrentHashMap<String, String>();
         mTxnMgr = new ClientTransactionManager(this, monitor);
     }
 
     @Override
     public <R, D> RemoteProcedure.Call<R, D> beginCall(RemoteProcedure<R, D> proc)
         throws RepositoryException
     {
         if (proc == null) {
             throw new IllegalArgumentException("RemoteProcedure cannot be null");
         }
 
         RemoteProcedureExecutor executor = mProcedureExecutor;
         if (executor == null) {
             synchronized (this) {
                 executor = mProcedureExecutor;
                 if (executor == null) {
                     RemoteStorageRequestor requestor = new RemoteStorageRequestor() {
                         public RemoteStorage serverStorageFor(Class<? extends Storable> type)
                             throws RepositoryException
                         {
                             return ((ClientStorage) storageFor(type)).remoteStorage();
                         }
                     };
                     executor = mRepository.newRemoteProcedureExecutor(requestor);
                     mProcedureExecutor = executor;
                 }
             }
         }
 
         RemoteTransaction txn;
         try {
             txn = localTransactionScope().getTxn();
         } catch (Exception e) {
             if (e instanceof RepositoryException) {
                 throw (RepositoryException) e;
             }
             throw new RepositoryException(e);
         }
 
         return new ProcedureCall<R, D>(this, executor.remoteCall(txn, proc, null), txn != null);
     }
 
     @Override
     public <C extends Capability> C getCapability(Class<C> capabilityType) {
         if (ResyncCapability.class.equals(capabilityType)) {
             try {
                 RemoteResyncCapability rrc = mRepository.getResyncCapability();
                 if (rrc != null) {
                     return (C) new ClientResyncCapability(rrc);
                 } else {
                     return null;
                 }
             } catch (RemoteException e) {
                 return null;
             }
         } else {
             return super.getCapability(capabilityType);
         }
     }
 
     protected org.apache.commons.logging.Log getLog() {
         return null;
     }
 
     @Override
     protected <S extends Storable> Storage<S> createStorage(Class<S> type)
         throws RepositoryException
     {
         return new ClientStorage<S>(type, this, remoteStorageFor(mRepository, type));
     }
 
     @Override
     protected SequenceValueProducer createSequenceValueProducer(String name)
         throws RepositoryException
     {
         RemoteSequenceValueProducer producer = mRepository.getSequenceValueProducer(name);
         mSequenceNames.put(name, "");
         SequenceValueProducer wrapper = new ClientSequenceValueProducer(producer);
         return wrapper;
     }
 
     @Override
     protected final TransactionManager<RemoteTransaction> transactionManager() {
         return mTxnMgr;
     }
 
     @Override
     protected final TransactionScope<RemoteTransaction> localTransactionScope() {
         return mTxnMgr.localScope();
     }
 
     private RemoteStorageTransport remoteStorageFor(final RemoteRepository remote,
                                                     final Class<? extends Storable> type)
         throws RepositoryException
     {
         Layout localLayout = ReconstructedCache.THE.layoutFor(type);
         StorableTypeTransport transport = new StorableTypeTransport(type, localLayout);
         try {
             return remote.storageFor(transport);
         } catch (MalformedTypeException e) {
             MalformedTypeException e2 = new MalformedTypeException
                 (type, e.getMessage() + ", or server doesn't have Storable definition");
             e2.setStackTrace(e.getStackTrace());
             throw e2;
         }
     }
 }
