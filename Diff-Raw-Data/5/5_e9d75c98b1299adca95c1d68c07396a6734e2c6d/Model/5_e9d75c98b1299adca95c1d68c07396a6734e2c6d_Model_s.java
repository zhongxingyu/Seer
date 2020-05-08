 package com.updatecontrols.correspondence;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.UUID;
 
 import android.util.Log;
 
 import com.mallardsoft.query.Predicate;
 import com.mallardsoft.query.QuerySpec;
 import com.updatecontrols.correspondence.memento.CorrespondenceFactType;
 import com.updatecontrols.correspondence.memento.FactID;
 import com.updatecontrols.correspondence.memento.FactMemento;
 import com.updatecontrols.correspondence.memento.FactTreeMemento;
 import com.updatecontrols.correspondence.memento.IdentifiedFactBase;
 import com.updatecontrols.correspondence.memento.IdentifiedFactMemento;
 import com.updatecontrols.correspondence.memento.IdentifiedFactRemote;
 import com.updatecontrols.correspondence.memento.MessageMemento;
 import com.updatecontrols.correspondence.memento.PredecessorMemento;
 import com.updatecontrols.correspondence.memento.TimestampID;
 import com.updatecontrols.correspondence.memento.UnpublishMemento;
 import com.updatecontrols.correspondence.query.Condition;
 import com.updatecontrols.correspondence.query.Join;
 import com.updatecontrols.correspondence.query.QueryDefinition;
 import com.updatecontrols.correspondence.query.QueryInvalidator;
 import com.updatecontrols.correspondence.serialize.FieldSerializer;
 import com.updatecontrols.correspondence.serialize.FieldSerializerByte;
 import com.updatecontrols.correspondence.serialize.FieldSerializerByteArray;
 import com.updatecontrols.correspondence.serialize.FieldSerializerDate;
 import com.updatecontrols.correspondence.serialize.FieldSerializerInt;
 import com.updatecontrols.correspondence.serialize.FieldSerializerString;
 import com.updatecontrols.correspondence.serialize.FieldSerializerUUID;
 import com.updatecontrols.correspondence.strategy.StorageStrategy;
 
 public class Model {
 
 	public static final int MAX_DATA_LENGTH = 1024;
 
 	private static final long ClientDatabaseId = 0;
 	
 	private Community community;
 	private StorageStrategy storageStrategy;
 	private HashMap<CorrespondenceFactType, CorrespondenceFactFactory> factoryByType = new HashMap<CorrespondenceFactType, CorrespondenceFactFactory>();
 	private HashMap<CorrespondenceFactType, ArrayList<QueryInvalidator>> queryInvalidatorsByType = new HashMap<CorrespondenceFactType, ArrayList<QueryInvalidator>>();
 	
 	private HashMap<FactID, CorrespondenceFact> factById = new HashMap<FactID, CorrespondenceFact>(); 
 	private HashMap<FactMemento, CorrespondenceFact> factByMemento = new HashMap<FactMemento, CorrespondenceFact>();
 
 	private HashMap<Class<?>, FieldSerializer> fieldSerializerByType = new HashMap<Class<?>, FieldSerializer>();
 
 	public Model(
 			Community community,
 			StorageStrategy storageStrategy) {
 		super();
 		this.community = community;
 		this.storageStrategy = storageStrategy;
 		
 		// Add the default serializers.
 		fieldSerializerByType.put(Byte.class, new FieldSerializerByte());
 		fieldSerializerByType.put(byte.class, new FieldSerializerByte());
 		fieldSerializerByType.put(Integer.class, new FieldSerializerInt());
 		fieldSerializerByType.put(int.class, new FieldSerializerInt());
 		fieldSerializerByType.put(UUID.class, new FieldSerializerUUID());
 		fieldSerializerByType.put(String.class, new FieldSerializerString());
 		fieldSerializerByType.put(Date.class, new FieldSerializerDate());
 		fieldSerializerByType.put(byte[].class, new FieldSerializerByteArray());
 	}
 
 	public Community getCommunity() {
 		return community;
 	}
 
 	public void addType(CorrespondenceFactType type, CorrespondenceFactFactory factory, FactMetadata factMetadata) {
 		factoryByType.put(type, factory);
 	}
 
 	public void addType(String typeName, int version, CorrespondenceFactFactory correspondenceFactFactory) {
 		factoryByType.put(new CorrespondenceFactType(typeName, version), correspondenceFactFactory);
 	}
 
 	public void addQuery(CorrespondenceFactType type, QueryDefinition queryDefinition) {
 		QueryDefinition invalidQuery = queryDefinition;
 		invertQuery(type, queryDefinition, new QueryDefinition(), invalidQuery);
 	}
 
 	public void addFieldSerializer(Class<?> fieldType, FieldSerializer fieldSerializer) {
 		fieldSerializerByType.put(fieldType, fieldSerializer);
 	}
 
 	public CorrespondenceFact getFactById(FactID id) {
 		// Check for null.
 		if (id.equals(FactID.Null))
 			return null;
 
 		CorrespondenceFact fact = null;
 		synchronized (this) {
 			// See if the object is already loaded.
 			fact = factById.get(id);
 			if (fact != null)
 				return fact;
 			
 			// If not, load it from storage.
 			try {
 				FactMemento factMemento = storageStrategy.load(id);
 				fact = createFactFromMemento(id, factMemento);
 			} catch (CorrespondenceException e) {
 				// Ignore objects that we can't load.
 				community.factLoadFailed(e);
 			}
 		}
 		
 		return fact;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends CorrespondenceFact> T getFact(T prototype) {
 		if (prototype.getCommunity() != null)
 			throw new IllegalStateException("An object may belong to only one community.");
 		
 		try {
 			// See if we already have a matching object.
 			prototype.setCommunity(community);
 			CorrespondenceFactFactory[] factory = new CorrespondenceFactFactory[1];
 			FactMemento prototypeMemento = createMementoFromFact(prototype, factory);
 			
 			synchronized (this) {
 				CorrespondenceFact fact = factByMemento.get(prototypeMemento);
 				if (fact != null) {
 					return (T) fact;
 				}
 				else {
 					// Check storage for the object.
 					FactID factId = storageStrategy.findExistingFact(prototypeMemento);
 					if (factId != null) {
 						prototype.setId(factId);
 						return prototype;
 					}
 					else {
 						return null;
 					}
 				}
 			}
 		} catch (CorrespondenceException e) {
 			throw new IllegalStateException("Failed to store correspondence object.", e);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T extends CorrespondenceFact> T addFact(T prototype) {
 		if (prototype.getCommunity() != null)
 			throw new IllegalStateException("An object may belong to only one community.");
 		
 		try {
 			// See if we already have a matching object.
 			prototype.setCommunity(community);
 			CorrespondenceFactFactory[] factory = new CorrespondenceFactFactory[1];
 			FactMemento prototypeMemento = createMementoFromFact(prototype, factory);
 			
 			// TODO All inserts are serialized.
 			synchronized (this) {
 				CorrespondenceFact fact = factByMemento.get(prototypeMemento);
 				if (fact != null) {
 					return (T) fact;
 				}
 				
 				// Set the ID and add the object to the community.
 				HashSet<InvalidatedQuery> invalidatedQueries = new HashSet<InvalidatedQuery>();
 				int peerId = 0;
 				FactID factId = addFactMemento(peerId, prototypeMemento, invalidatedQueries);
 				prototype.setId(factId);
 				
 				// Invalidate all of the queries affected by the new fact.
 				for (InvalidatedQuery invalidatedQuery : invalidatedQueries) {
 					invalidatedQuery.invalidate();
 				}
 				
 				factById.put(prototype.getId(), prototype);
 				factByMemento.put(prototypeMemento, prototype);
 				invariantFactMapsMatch();
 			}
 			
 			return prototype;
 		} catch (CorrespondenceException e) {
 			throw new IllegalStateException("Failed to store correspondence object.", e);
 		}
 	}
 
 	public FactTreeMemento getMessageBodies(TimestampID timestamp, int peerId,
 			ArrayList<UnpublishMemento> arrayList) {
 		FactTreeMemento result = new FactTreeMemento(ClientDatabaseId);
         Iterable<MessageMemento> recentMessages = storageStrategy.loadRecentMessagesForServer(peerId, timestamp);
 		for (MessageMemento message : recentMessages) {
 			if (message.getFactId().getKey() > timestamp.getKey())
 				timestamp = new TimestampID(ClientDatabaseId, message.getFactId().getKey());
             FactMemento newFact = addToFactTree(result, message.getFactId(), peerId);
 		}
 		return result;
 	}
 
 	public FactMemento addToFactTree(FactTreeMemento messageBody, FactID factId, int peerId) {
 	
         try {
 			if (!messageBody.contains(factId)) {
 			    CorrespondenceFact fact = getFactById(factId);
 			    if (fact != null) {
 			        FactID remoteId = storageStrategy.getRemoteId(factId, peerId);
 			        if (remoteId != null) {
 			            messageBody.add(new IdentifiedFactRemote(factId, remoteId));
 			        }
 			        else {
 			        	CorrespondenceFactFactory[] factoryVariable = new CorrespondenceFactFactory[1];
 			            FactMemento factMemento = createMementoFromFact(fact, factoryVariable);
 			            for (PredecessorMemento predecessor : factMemento.getPredecessors())
 			                addToFactTree(messageBody, predecessor.getId(), peerId);
 			            messageBody.add(new IdentifiedFactMemento(factId, factMemento));
 
 			            return factMemento;
 			        }
 			    }
 			    return null;
 			}
 			else {
 			    IdentifiedFactBase identifiedFact = messageBody.getFactById(factId);
 			    if (identifiedFact instanceof IdentifiedFactMemento)
 			        return ((IdentifiedFactMemento)identifiedFact).getMemento();
 			    else
 			        return null;
 			}
 		} catch (CorrespondenceException e) {
 			// If the fact is already in the model, then we cannot fail to create a memento from it.
 			return null;
 		}
 	}
 
 	public void receiveMessage(FactTreeMemento messageBody, int peerId) {
         HashSet<InvalidatedQuery> invalidatedQueries = new HashSet<InvalidatedQuery>();
 
         synchronized (this) {
             Hashtable<FactID, FactID> localIdByRemoteId = new Hashtable<FactID, FactID>();
             for (IdentifiedFactBase identifiedFact : messageBody.getFacts()) {
                 FactID localId = receiveFact(identifiedFact, peerId, invalidatedQueries, localIdByRemoteId);
                 FactID remoteId = identifiedFact.getId();
                 localIdByRemoteId.put(remoteId, localId);
             }
         }
 
         for (InvalidatedQuery invalidatedQuery : invalidatedQueries)
             invalidatedQuery.invalidate();
 	}
 
 	private FactID receiveFact(IdentifiedFactBase identifiedFact, int peerId,
 			HashSet<InvalidatedQuery> invalidatedQueries,
 			Hashtable<FactID, FactID> localIdByRemoteId) {
         if (identifiedFact instanceof IdentifiedFactMemento) {
             IdentifiedFactMemento identifiedFactMemento = (IdentifiedFactMemento)identifiedFact;
             
             Log.d("Correspondence", "Received " +
             		identifiedFactMemento.getMemento().getType().getName() + "." +
             		identifiedFactMemento.getMemento().getType().getVersion());
             
             FactMemento translatedMemento = new FactMemento(identifiedFactMemento.getMemento().getType());
             translatedMemento.setData(identifiedFactMemento.getMemento().getData());
             for (PredecessorMemento remote : identifiedFactMemento.getMemento().getPredecessors()) {
                 FactID localFactId = localIdByRemoteId.get(remote.getId());
                 if (localFactId != null) {
                 	translatedMemento.addPredecessor(remote.getRole(), localFactId, remote.isPivot());
                 }
             }
 
             FactID localId = addFactMemento(peerId, translatedMemento, invalidatedQueries);
             storageStrategy.saveShare(peerId, identifiedFact.getId(), localId);
 
             return localId;
         }
         else
         {
             IdentifiedFactRemote identifiedFactRemote = (IdentifiedFactRemote)identifiedFact;
             return storageStrategy.getFactIDFromShare(peerId, identifiedFactRemote.getRemoteId());
         }
 	}
 
 	public UUID getClientDatabaseGuid() {
 		return storageStrategy.getClientGuid();
 	}
 
 	public void executeQuery(ArrayList<CorrespondenceFact> facts,
 			FactID id, QueryDefinition queryDefinition) {
 		// Load the mementos from storage.
 		for (IdentifiedFactMemento memento: storageStrategy.queryForFacts(queryDefinition, id)) {
 			try {
 				CorrespondenceFact fact = null;
 				synchronized (this) {
 					// See if the object is already loaded.
 					fact = factById.get(memento.getId());
 					if (fact == null) {
 						fact = createFactFromMemento(memento.getId(), memento.getMemento());
 					}
 				}
 				
 				facts.add(fact);
 			} catch (CorrespondenceException e) {
 				// Ignore objects that cannot be loaded.
 				community.factLoadFailed(e);
 			}
 		}
 	}
 	
 	public int getCacheSize() {
 		synchronized (this) {
 			invariantFactMapsMatch();
 			return factById.size();
 		}
 	}
 
 	private FactMemento createMementoFromFact(CorrespondenceFact correspondenceFact, CorrespondenceFactFactory[] factoryVariable)
 			throws CorrespondenceException {
 		// Find the object's factory and have it write the data.
 		CorrespondenceFactType type = correspondenceFact.getCorrespondenceFactType();
 		CorrespondenceFactFactory factory = factoryByType.get(type);
 		if (factory == null)
 			throw new CorrespondenceException(String.format("Type %s not registered.", type));
 		if (factoryVariable != null)
 			factoryVariable[0] = factory;
 
 		// Record the type name.
 		FactMemento factMemento = new FactMemento(type);
 		
 		// Record the predecessor IDs.
 		correspondenceFact.addPredecessorsToMemento(factMemento);
 		
 		try {
 			// Record the data.
 			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
 			DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
 			try {
 				try {
 					factory.writeFactData(correspondenceFact, out, fieldSerializerByType);
 				} catch (IllegalArgumentException e) {
 					throw new CorrespondenceException(
 						"Failed to write fact.", e);
 				} catch (IllegalAccessException e) {
 					throw new CorrespondenceException(
 						"Failed to write fact.", e);
 				} catch (IOException e) {
 					throw new CorrespondenceException(
 						"Failed to write fact.", e);
 				}
 
 				out.flush();
 				if (byteArrayOutputStream.size() > MAX_DATA_LENGTH)
 					throw new CorrespondenceException("Exceeded the maximum data size for one object.");
 				factMemento.setData(byteArrayOutputStream.toByteArray());
 			}
 			finally {
 				out.close();
 			}
 		} catch (IOException e) {
 			throw new CorrespondenceException("Failed to write correspondence object.", e);
 		}
 		
 		return factMemento;
 	}
 
 	private CorrespondenceFact createFactFromMemento(FactID id, FactMemento factMemento)
 			throws CorrespondenceException {
 		// Find the factory for this type.
 		CorrespondenceFactFactory factory = factoryByType.get(factMemento.getType());
 		if (factory == null)
 			throw new CorrespondenceException(
 				String.format(
 					"Unknown correspondence object type %s version %d.",
 					factMemento.getType().getName(),
 					factMemento.getType().getVersion()));
 		
 		// Construct the object.
 		CorrespondenceFact fact;
 		try {
 			fact = factory.createFact(factMemento, fieldSerializerByType);
 		} catch (CorrespondenceException ce) {
 			throw new CorrespondenceException(String.format("Failed to load object %1s. %2s", id, ce.getMessage()), ce);
 		}
 		fact.setId(id);
 		
 		// Store the object so it can be found by ID or memento.
 		fact.setCommunity(community);
 		factById.put(fact.getId(), fact);
 		factByMemento.put(factMemento, fact);
 		invariantFactMapsMatch();
 		return fact;
 	}
 
 	private void invariantFactMapsMatch() throws AssertionError {
 		if (factById.size() != factByMemento.size()) {
 			StringBuffer errors = new StringBuffer();
 			if (factById.size() > factByMemento.size()) {
 				for (CorrespondenceFact fact : factById.values()) {
 					if (!QuerySpec.from(factByMemento.values()).where(factEquals(fact)).any()) {
 						errors.append(String.format("  ObjectByMemento does not contain %1s %2s.", fact.getClass().getName(), fact.getId()));
 						errors.append("\n");
 					}
 				}
 			}
 			else {
 				for (CorrespondenceFact fact : factByMemento.values()) {
 					if (!QuerySpec.from(factById.values()).where(factEquals(fact)).any()) {
 						errors.append(String.format("  ObjectById does not contain %1s %2s.", fact.getClass().getName(), fact.getId()));
 						errors.append("\n");
 					}
 				}
 			}
 			throw new AssertionError("ID and memento caches do not match.\n" + errors.toString());
 		}
 	}
 
 	private Predicate<CorrespondenceFact> factEquals(final CorrespondenceFact fact) {
 		return new Predicate<CorrespondenceFact>() {
 			
 			@Override
 			public boolean where(CorrespondenceFact row) {
 				return row == fact;
 			}
 		};
 	}
 
 	private void invertQuery(
 			CorrespondenceFactType type,
 			QueryDefinition queryDefinition,
 			QueryDefinition inverse,
 			QueryDefinition invalidQuery) {
 		CorrespondenceFactType priorType = type;
 		List<Condition> conditions = null;
 		
 		for (Join join : queryDefinition.getJoins()) {
 			if (join.isSuccessor()) {
 				// Predecessors cannot arrive later, so don't record an invalidator.
 				priorType = join.getRole().getDeclaringType();
 			}
 			else {
 				// Record an invalidator for the partial inverse query because we
 				// are stepping up to predecessors.
 				recordInvalidator(priorType, inverse.copy(), invalidQuery);
 				
 				priorType = join.getRole().getTargetType();
 			}
 			
 			// Build up the inverse.
 			inverse.prependInverse(join, conditions);
 			
 			// Recursively invert the sub queries.
 			if (conditions != null) {
 				for (Condition condition : conditions) {
 					invertQuery(priorType, condition.getSubQuery(), inverse.copy(), invalidQuery);
 				}
 			}
			
			conditions = join.getConditions();
 		}
 		
 		// Record an invalidator for the full inverse.
 		recordInvalidator(priorType, inverse, invalidQuery);
 	}
 	
 	private void recordInvalidator(CorrespondenceFactType type, QueryDefinition inverse, QueryDefinition invalidQuery) {
 		ArrayList<QueryInvalidator> invalidators = queryInvalidatorsByType.get(type);
 		if (invalidators == null) {
 			invalidators = new ArrayList<QueryInvalidator>();
 			queryInvalidatorsByType.put(type, invalidators);
 		}
 		invalidators.add(new QueryInvalidator(inverse, invalidQuery));
 	}
 
 	private FactID addFactMemento(int peerId, FactMemento prototypeMemento,
 			HashSet<InvalidatedQuery> invalidatedQueries) {
 		FactID factId = storageStrategy.save(prototypeMemento);
 		invalidateQueries(prototypeMemento, factId, invalidatedQueries);
 		return factId;
 	}
 	
 	private void invalidateQueries(FactMemento memento, FactID id, HashSet<InvalidatedQuery> invalidatedQueries) {
 		ArrayList<QueryInvalidator> queryInvalidators = queryInvalidatorsByType.get(memento.getType());
 		if (queryInvalidators != null) {
 			for (QueryInvalidator queryInvalidator : queryInvalidators) {
 				Iterable<FactID> targetFactIds = null;
 				if (queryInvalidator.getTargetFacts().canExecuteFromMemento()) {
 					targetFactIds = queryInvalidator.getTargetFacts().executeFromMemento(memento);
 				}
 				else {
 					targetFactIds = storageStrategy.queryForIds(queryInvalidator.getTargetFacts(), id);
 				}
 				
 				for (FactID targetFactId : targetFactIds) {
 					CorrespondenceFact targetFact = factById.get(targetFactId);
 					if (targetFact != null) {
 						invalidatedQueries.add(new InvalidatedQuery(targetFact, queryInvalidator.getInvalidQuery()));
 					}
 				}
 			}
 		}
 	}
 	
 }
