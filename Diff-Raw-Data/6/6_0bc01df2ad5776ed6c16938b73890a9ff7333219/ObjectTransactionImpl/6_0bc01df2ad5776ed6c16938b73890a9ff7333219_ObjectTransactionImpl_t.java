 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.objecttransaction.impl;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.internal.objecttransaction.Activator;
 import org.eclipse.riena.objecttransaction.IObjectId;
 import org.eclipse.riena.objecttransaction.IObjectTransaction;
 import org.eclipse.riena.objecttransaction.IObjectTransactionExtract;
 import org.eclipse.riena.objecttransaction.ITransactedObject;
 import org.eclipse.riena.objecttransaction.InvalidTransactionFailure;
 import org.eclipse.riena.objecttransaction.ObjectTransactionFailure;
 import org.eclipse.riena.objecttransaction.ObjectTransactionManager;
 import org.eclipse.riena.objecttransaction.delta.AbstractBaseChange;
 import org.eclipse.riena.objecttransaction.delta.MultipleChange;
 import org.eclipse.riena.objecttransaction.delta.MultipleChangeEntry;
 import org.eclipse.riena.objecttransaction.delta.SingleChange;
 import org.eclipse.riena.objecttransaction.delta.TransactionDelta;
 import org.eclipse.riena.objecttransaction.state.Action;
 import org.eclipse.riena.objecttransaction.state.State;
 import org.eclipse.riena.objecttransaction.state.StateMachine;
 
 /**
  * Implements the IObjectTransaction interface. Contains all the logic for
  * maintaining property values, references between object, state of objects.
  * 
  */
 public class ObjectTransactionImpl implements IObjectTransaction {
 
 	// Map of the TransactionDelta entries in this objectTransaction, key is
 	// IObjectId
 	private Map<IObjectId, TransactionDelta> changesInTransaction;
 	// Map of the ITransactedObjects registered in this objectTransaction key is
 	// IObjectId
 	private Map<IObjectId, ITransactedObject> involvedTransactedObjects;
 	// set to true if this objectTransaction is the root transaction
 	private boolean rootTransaction = true;
 	/**
 	 * contains a reference to the next higher object transaction. Usually this
 	 * field is null, if this is a object transaction. However for root
 	 * transactions this field is used to reference a hidden transaction that
 	 * contains all the changes that were committed against the database.
 	 */
 	private ObjectTransactionImpl parentTransaction;
 	// is this objectTransaction is cleanModus ? then changes do not get
 	// recorded in this objectTransaction
 	private boolean cleanModus;
 	// is this objectTransaction invalid ? (because it was committed) then no
 	// calls are possible
 	private boolean invalidTransaction = false;
 	/**
 	 * do we have preRegistered ITransactedObjects ? The application program
 	 * tried to register them at a time where no IObjectId was set in the
 	 * business object. The preregistered objects are registered as soon as
 	 * their IObjectId is set.
 	 */
 	private List<ITransactedObject> preRegisteredCleanObjects = null;
 	private boolean strictModus = false;
 	private boolean allowRegister = true;
 
 	private static final Logger LOGGER = Log4r.getLogger(Activator.getDefault(), ObjectTransactionImpl.class);
 
 	/**
 	 * constructor that creates a new objectTransaction
 	 */
 	public ObjectTransactionImpl() {
 		super();
 		changesInTransaction = new HashMap<IObjectId, TransactionDelta>();
 		involvedTransactedObjects = new HashMap<IObjectId, ITransactedObject>();
 		this.cleanModus = false;
 	}
 
 	/**
 	 * constructor that creates a new objectTransaction with setting a
 	 * difference objectTransaction as parent
 	 * 
 	 * @param parentTransaction
 	 */
 	public ObjectTransactionImpl(final IObjectTransaction parentTransaction) {
 		this();
 		this.parentTransaction = (ObjectTransactionImpl) parentTransaction;
 		this.rootTransaction = false;
 	}
 
 	/**
 	 * create a new objectTransaction as interface method
 	 * 
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#createSubObjectTransaction()
 	 */
 	public IObjectTransaction createSubObjectTransaction() {
 		final IObjectTransaction objectTransaction = new ObjectTransactionImpl(this);
 		ObjectTransactionManager.getInstance().setCurrent(objectTransaction);
 		return objectTransaction;
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#register(org.eclipse.riena.objecttransaction.ITransactedObject)
 	 */
 	public void register(final ITransactedObject object) {
 		if (!allowRegister) {
 			return;
 		}
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		// objects can only be registered once, so check if is already
 		// registered
 		Assert.isTrue((object.getObjectId() != null && !isRegistered(object)) || object.getObjectId() == null,
 				"object cannot be registered a second time"); //$NON-NLS-1$
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 
 		keepReferenceOf(object);
 		// if ObjectId is not set, preregister, otherwise set object state to
 		// CLEAN (unchanged)
 		if (object.getObjectId() == null) {
 			addPreRegisteredClean(object);
 		} else {
 			setObjectState(object, State.CLEAN);
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#registerNew(org.eclipse.riena.objecttransaction.ITransactedObject)
 	 */
 	public void registerNew(final ITransactedObject object) {
 		if (!allowRegister) {
 			return;
 		}
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isTrue(object.getObjectId() != null, "ObjectId of Transacted Object must not be null"); //$NON-NLS-1$
 		Assert.isTrue(!isRegistered(object), "Transacted Object must not be already registered"); //$NON-NLS-1$
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 
 		notifyObjectChange(object, Action.NEW);
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#registerAsDeleted(org.eclipse.riena.objecttransaction.ITransactedObject)
 	 */
 	public void registerAsDeleted(final ITransactedObject object) {
 		if (!allowRegister) {
 			return;
 		}
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 		notifyObjectChange(object, Action.DELETE);
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#allowRegister(boolean)
 	 */
 	public void allowRegister(final boolean parmAllowRegister) {
 		this.allowRegister = parmAllowRegister;
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#exportExtract()
 	 */
 	public IObjectTransactionExtract exportExtract() {
 		return exportExtractInternal(true);
 	}
 
 	/*
 	 * @seeorg.eclipse.riena.objecttransaction.IObjectTransaction#
 	 * exportOnlyModifedObjectsToExtract()
 	 */
 	public IObjectTransactionExtract exportOnlyModifedObjectsToExtract() {
 		// first export regular modified objects
 		final IObjectTransactionExtract extract = exportExtractInternal(false);
 		// then include all targets of a reference change in the extract
 		for (final TransactionDelta delta : extract.getDeltas()) {
 			final Collection<?> changes = delta.getChanges().values();
 			for (final Object change : changes) {
 				if (change instanceof SingleChange) {
 					final SingleChange sChange = (SingleChange) change;
 					Object child = sChange.getChildObject();
 					if (child instanceof IObjectId) {
 						child = lookupObjectById((IObjectId) child);
 					}
 					if (child instanceof ITransactedObject
 							&& !(extract.contains(((ITransactedObject) child).getObjectId()))) {
 						extract.addCleanTransactedObject((ITransactedObject) child);
 					}
 				} else {
 					if (change instanceof MultipleChange) {
 						final MultipleChange mChange = (MultipleChange) change;
 						for (final MultipleChangeEntry mEntry : mChange.getEntries()) {
 							Object mChild = mEntry.getChildObject();
 							if (mChild instanceof IObjectId) {
 								mChild = lookupObjectById((IObjectId) mChild);
 							}
 							if (mChild instanceof ITransactedObject) {
 								if (!extract.contains(((ITransactedObject) mChild).getObjectId())) {
 									extract.addCleanTransactedObject((ITransactedObject) mChild);
 								}
 							} else {
 								throw new ObjectTransactionFailure("non ITransactionObject in multi reference ???"); //$NON-NLS-1$
 							}
 						}
 					} else {
 						throw new ObjectTransactionFailure(
 								"unknown change typ (not single and not multi reference change)"); //$NON-NLS-1$
 					}
 				}
 			}
 		}
 		return extract;
 	}
 
 	private IObjectTransactionExtract exportExtractInternal(final boolean exportClean) {
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 		// copies the TransactionDeltas into an Array
 		final ObjectTransactionExtractImpl extract = new ObjectTransactionExtractImpl();
 		try {
 			for (TransactionDelta temp : changesInTransaction.values()) {
 				if (!(temp.getState().equals(State.VANISHED))) {
 					if (exportClean || !(temp.getState().equals(State.CLEAN))) {
 						temp = (TransactionDelta) temp.clone();
 						extract.addDelta(temp);
 					}
 				}
 			}
 		} catch (final CloneNotSupportedException e) {
 			throw new ObjectTransactionFailure("error cloning TransactionDelta ", e); //$NON-NLS-1$
 		}
 		// and create an objectTransactionextract from it
 		return extract;
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#importExtract(org.eclipse.riena.objecttransaction.IObjectTransactionExtract)
 	 */
 	public void importExtract(final IObjectTransactionExtract extract) throws InvalidTransactionFailure {
 		checkForRegisteredObjects();
 		importExtractInternal(extract, true);
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#importOnlyModifedObjectsFromExtract(org.eclipse.riena.objecttransaction.IObjectTransactionExtract)
 	 */
 	public void importOnlyModifedObjectsFromExtract(final IObjectTransactionExtract extract)
 			throws InvalidTransactionFailure {
 		checkForRegisteredObjects();
 		// check that all targets of reference changes are registered
 		for (final TransactionDelta delta : extract.getDeltas()) {
 			final Collection<?> changes = delta.getChanges().values();
 			for (final Object change : changes) {
 				if (change instanceof SingleChange) {
 					final SingleChange sChange = (SingleChange) change;
 					final Object child = sChange.getChildObject();
 					boolean isRegistered = true;
 					if (child instanceof IObjectId) {
 						isRegistered = this.isRegistered((IObjectId) child);
 					} else {
 						if (child instanceof ITransactedObject) {
 							isRegistered = this.isRegistered((ITransactedObject) child);
 						}
 					}
 					if (!isRegistered) {
 						throw new InvalidTransactionFailure("reference target object " + child //$NON-NLS-1$
 								+ " must be registered before import"); //$NON-NLS-1$
 					}
 				} else {
 					if (change instanceof MultipleChange) {
 						final MultipleChange mChange = (MultipleChange) change;
 						for (final MultipleChangeEntry mEntry : mChange.getEntries()) {
 							final Object mChild = mEntry.getChildObject();
 							boolean isRegistered = true;
 							if (mChild instanceof IObjectId) {
 								isRegistered = isRegistered((IObjectId) mChild);
 							} else {
 								if (mChild instanceof ITransactedObject) {
 									isRegistered = isRegistered((ITransactedObject) mChild);
 								} else {
 									throw new InvalidTransactionFailure("invalid object in multi-ref " + mChild); //$NON-NLS-1$
 								}
 							}
 							if (!isRegistered) {
 								throw new InvalidTransactionFailure("reference target object " + mChild //$NON-NLS-1$
 										+ " must be registered before import"); //$NON-NLS-1$
 							}
 						}
 					} else {
 						throw new ObjectTransactionFailure(
 								"unknown change typ (not single and not multi reference change)"); //$NON-NLS-1$
 					}
 				}
 			}
 		}
 		importExtractInternal(extract, false);
 	}
 
 	/**
 	 * Check that objects that own a delta record are registered in the object
 	 * transaction
 	 * 
 	 * @throws InvalidTransactionFailure
 	 */
 	private void checkForRegisteredObjects() throws InvalidTransactionFailure {
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 		if (!isRootTransaction()) {
 			throw new InvalidTransactionFailure("object transaction must be the root transaction"); //$NON-NLS-1$
 		}
 		// import means adding changes to the objectTransaction and cannot
 		// happen in clean modus
 		if (isCleanModus()) {
 			throw new InvalidTransactionFailure("object transaction must NOT be in clean modus when importing"); //$NON-NLS-1$
 		}
 
 		// this will add all preregistered objects with ObjectIds to the
 		// objectTransaction
 		checkPreRegisteredClean();
 		if (preRegisteredCleanObjects != null && preRegisteredCleanObjects.size() > 0) {
 			for (final ITransactedObject object : preRegisteredCleanObjects) {
 				if (object.getObjectId() != null) {
 					throw new InvalidTransactionFailure("internal error, preregistered object found with ObjectId " //$NON-NLS-1$
 							+ object);
 				} else {
 					throw new InvalidTransactionFailure("missing object id for object " + object); //$NON-NLS-1$
 				}
 			}
 		}
 
 	}
 
 	private void importExtractInternal(final IObjectTransactionExtract extract, final boolean importClean)
 			throws InvalidTransactionFailure {
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 		if (!isRootTransaction()) {
 			throw new InvalidTransactionFailure("object transaction must be the root transaction"); //$NON-NLS-1$
 		}
 		// import means adding changes to the objectTransaction and cannot
 		// happen in clean modus
 		if (isCleanModus()) {
 			throw new InvalidTransactionFailure("object transaction must NOT be in clean modus when importing"); //$NON-NLS-1$
 		}
 
 		// check that all object ids referenced in the deltas, exist in the
 		// transaction, if they exist copy them over
 		final TransactionDelta[] deltas = extract.getDeltas();
 		final ObjectTransactionImpl tempSubTransaction = (ObjectTransactionImpl) createSubObjectTransaction();
 		for (final TransactionDelta delta : deltas) {
 			if (importClean || delta.getState() != State.CLEAN) {
 				IObjectId objectId = delta.getObjectId();
 				IObjectId newObjectId = null;
 				String versionChange = null;
 				// check if the extract contains an object id change
 				if (delta.getSingleRefObject("sys::oldoid") != null) { //$NON-NLS-1$
 					objectId = (IObjectId) delta.getSingleRefObject("sys::oldoid"); //$NON-NLS-1$
 					newObjectId = (IObjectId) delta.getSingleRefObject("sys::oid"); //$NON-NLS-1$
 				}
 				if (delta.getSingleRefObject("sys::version") != null) { //$NON-NLS-1$
 					versionChange = (String) delta.getSingleRefObject("sys::version"); //$NON-NLS-1$
 				}
 				if (this.involvedTransactedObjects.get(objectId) == null) {
 					throw new ObjectTransactionFailure(
 							"ObjectIds for all imported deltas must exist in objectTransaction, not found=" + delta); //$NON-NLS-1$
 				} else {
 					if (newObjectId != null) {
 						changeObjectId(objectId, newObjectId);
 						objectId = newObjectId;
 					}
 					if (versionChange != null) {
 						final ITransactedObject transObject = this.involvedTransactedObjects.get(objectId);
 						transObject.setVersion(versionChange);
 					}
 					// copy involved objects from this transaction and add
 					// deltas to subtransaction
 					tempSubTransaction.involvedTransactedObjects.put(objectId,
 							this.involvedTransactedObjects.get(objectId));
 					tempSubTransaction.changesInTransaction.put(objectId, delta);
 				}
 			}
 		}
 
 		// commit the temporary sub transaction and forget it. this way the
 		// extract is merged into the current transaction
 		tempSubTransaction.commit();
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#isRootTransaction()
 	 */
 	public boolean isRootTransaction() {
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 		return rootTransaction;
 	}
 
 	/**
 	 * sets the object state of a transacted object
 	 * 
 	 * @param object
 	 *            transacted object for which to set the state
 	 * @param state
 	 *            state that should be set
 	 */
 	private void setObjectState(final ITransactedObject object, final State state) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 
 		keepReferenceOf(object);
 
 		// check if a delta exists and if not create one, then set the state
 		TransactionDelta delta = changesInTransaction.get(object.getObjectId());
 		if (delta == null) {
 			// we have to set the version at the end, because retrieving the
 			// version
 			// (sometimes) requires that the object is registered so it has to
 			// have
 			// a changesInTransaction entry
 			delta = new TransactionDelta(object.getObjectId(), state, object.getVersion());
 			changesInTransaction.put(object.getObjectId(), delta);
 		} else {
 			delta.setState(state);
 		}
 		return;
 	}
 
 	/**
 	 * return the object state for a given transacted object
 	 * 
 	 * @param object
 	 *            transacted object for which to retrieve the object state
 	 * @return state of the transacted objcet
 	 */
 	private State getObjectState(final ITransactedObject object) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 		// find the delta for an oid
 		final TransactionDelta delta = changesInTransaction.get(object.getObjectId());
 		// if not found
 		if (delta == null) {
 			// if a parenttransaction exist, ask the parent transaction for the
 			// state
 			if (parentTransaction != null) {
 				return parentTransaction.getObjectState(object);
 			}
 			return null;
 		}
 		return delta.getState();
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#setReference(org.eclipse.riena.objecttransaction.ITransactedObject,
 	 *      java.lang.String, java.lang.Object)
 	 */
 	public void setReference(final ITransactedObject object, final String refName, final Object newValue) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
		if (newValue instanceof ITransactedObject) {
			setReference(object, refName, (ITransactedObject) newValue);
			return;
		}
 		checkPreRegisteredClean();
 		if (!isCleanModus()) {
 			Assert.isTrue(isRegistered(object), "object must be registered"); //$NON-NLS-1$
 		}
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 
 		// change nothing if in clean modus
 		if (isCleanModus()) {
 			return;
 		}
 		// find the delta entry for this transacted objects ObjectId, create new
 		// entry if it does not exist
 		TransactionDelta delta = changesInTransaction.get(object.getObjectId());
 		if (delta == null) {
 			delta = new TransactionDelta(object.getObjectId(), State.CREATED, object.getVersion());
 			changesInTransaction.put(object.getObjectId(), delta);
 		} else {
 			// if the object was clean, set it to modified
 			if (delta.getState().equals(State.CLEAN)) {
 				delta.setState(State.MODIFIED);
 			}
 		}
 
 		// set the newValue for the refName
 		delta.setSingleRefObject(refName, newValue);
 		return;
 	}
 
 	public Object getReference(final ITransactedObject object, final String refName, final Object defaultValue) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
 		Assert.isTrue(!(defaultValue instanceof ITransactedObject),
 				"getReference for Object must not pass a \"hidden\" ITransactedObject for defaultValue"); //$NON-NLS-1$
 		checkPreRegisteredClean();
 		if (isCleanModus() || (!isStrictModus() && !isRegistered(object))) {
 			return defaultValue;
 		}
 		Assert.isTrue(isRegistered(object), "object must be registered"); //$NON-NLS-1$
 
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 
 		// find delta entry, look for a single change entry with the requested
 		// refName
 		// if it is a property (not instanceof IObjectId) return the value,
 		// if is a IObjectId, lookup the original transactedobject and return it
 		final TransactionDelta delta = changesInTransaction.get(object.getObjectId());
 		if (delta != null) {
 			if (delta.hasSingleRefObject(refName)) {
 				final Object propValue = delta.getSingleRefObject(refName);
 				if (propValue instanceof IObjectId) {
 					return lookupObjectById((IObjectId) propValue);
 				} else {
 					return propValue; // includes the case where propValue is
 					// null
 				}
 			}
 		}
 		// if I can't find an entry, ask the parenttransaction if it exists
 		if (parentTransaction != null) {
 			return parentTransaction.getReference(object, refName, defaultValue);
 		}
 		// if nothing is found, return the defaultValue
 		return defaultValue;
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#setReference(org.eclipse.riena.objecttransaction.ITransactedObject,
 	 *      java.lang.String,
 	 *      org.eclipse.riena.objecttransaction.ITransactedObject)
 	 */
 	public void setReference(final ITransactedObject object, final String relationName,
 			final ITransactedObject refObject) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
 		checkPreRegisteredClean();
 		if (!isCleanModus()) {
 			Assert.isTrue(isRegistered(object), "object must be registered"); //$NON-NLS-1$
 			Assert.isTrue(refObject == null || (refObject.getObjectId() != null && isRegistered(refObject)),
 					"invalid refObject"); //$NON-NLS-1$
 		}
 
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 
 		// do nothing if in clean modus
 		if (isCleanModus()) {
 			return;
 		}
 
 		// find current delta entry, create one if it does not exist
 		TransactionDelta delta = changesInTransaction.get(object.getObjectId());
 		if (delta == null) {
 			delta = new TransactionDelta(object.getObjectId(), State.CREATED, object.getVersion());
 			changesInTransaction.put(object.getObjectId(), delta);
 		} else {
 			// if it was clean, change it to modified
 			if (delta.getState().equals(State.CLEAN)) {
 				delta.setState(State.MODIFIED);
 			}
 		}
 		// add a reference to the ObjectId object of the referenced
 		// transactedobject
 		IObjectId id2 = null;
 		if (refObject != null) {
 			id2 = refObject.getObjectId();
 		}
 		delta.setSingleRefObject(relationName, id2);
 		return;
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#getReference(org.eclipse.riena.objecttransaction.ITransactedObject,
 	 *      java.lang.String,
 	 *      org.eclipse.riena.objecttransaction.ITransactedObject)
 	 */
 	public ITransactedObject getReference(final ITransactedObject object, final String relationName,
 			final ITransactedObject defaultRefObject) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
 		checkPreRegisteredClean();
 		if (isCleanModus() || (!isStrictModus() && !isRegistered(object))) {
 			return defaultRefObject;
 		}
 		Assert.isTrue(isRegistered(object), "object must be registered"); //$NON-NLS-1$
 		Assert.isTrue(defaultRefObject == null
 				|| (defaultRefObject.getObjectId() != null && isRegistered(defaultRefObject)),
 				"invalid defaultRefObject (is null, has no ObjectId or is not registered)"); //$NON-NLS-1$
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 
 		// find the delta entry for this transacted object, look for a single
 		// ref object with that reference name
 		// if is instanceof IObjectId, lookup the transacted object and return
 		// it, otherwise throw an exception
 		final TransactionDelta delta = changesInTransaction.get(object.getObjectId());
 		if (delta != null) {
 			if (delta.hasSingleRefObject(relationName)) {
 				final Object refObject = delta.getSingleRefObject(relationName);
 				if (refObject != null) {
 					if (refObject instanceof IObjectId) {
 						return lookupObjectById((IObjectId) refObject);
 					} else {
 						throw new InvalidTransactionFailure("how did I ever get here ???"); //$NON-NLS-1$
 					}
 				} else {
 					return null;
 				}
 			}
 		}
 		// if no delta entry was found, ask the parentTransaction, if it exists
 		if (parentTransaction != null) {
 			return parentTransaction.getReference(object, relationName, defaultRefObject);
 		}
 		// nothing found, return the default
 		return defaultRefObject;
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#addReference(org.eclipse.riena.objecttransaction.ITransactedObject,
 	 *      java.lang.String,
 	 *      org.eclipse.riena.objecttransaction.ITransactedObject)
 	 */
 	public void addReference(final ITransactedObject object, final String referenceName,
 			final ITransactedObject refObject) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
 		checkPreRegisteredClean();
 		if (!isCleanModus()) {
 			Assert.isTrue(isRegistered(object), "object must be registered"); //$NON-NLS-1$
 		}
 		Assert.isNotNull(refObject, "refObject must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(refObject.getObjectId(), "ObjectId of refObject must not be null"); //$NON-NLS-1$
 		if (!isCleanModus()) {
 			Assert.isTrue(isRegistered(refObject), "refObject must be registered"); //$NON-NLS-1$
 		}
 
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 		// do nothing if in clean modus
 		if (isCleanModus()) {
 			return;
 		}
 		// find delta entry for the transacted object, add one if it was not
 		// found
 		TransactionDelta delta = changesInTransaction.get(object.getObjectId());
 		if (delta == null) {
 			delta = new TransactionDelta(object.getObjectId(), State.CREATED, object.getVersion());
 			changesInTransaction.put(object.getObjectId(), delta);
 		} else {
 			// if state was clean, change it to modified
 			if (delta.getState().equals(State.CLEAN)) {
 				delta.setState(State.MODIFIED);
 			}
 		}
 		// add a reference to the refname into the MultiRefObject (a set of
 		// multiobjects for one refName)
 		delta.addMultiRefObject(referenceName, refObject.getObjectId());
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#removeReference(org.eclipse.riena.objecttransaction.ITransactedObject,
 	 *      java.lang.String,
 	 *      org.eclipse.riena.objecttransaction.ITransactedObject)
 	 */
 	public void removeReference(final ITransactedObject object, final String referenceName,
 			final ITransactedObject refObject) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
 		checkPreRegisteredClean();
 		if (!isCleanModus()) {
 			Assert.isTrue(isRegistered(object), "object must be registered"); //$NON-NLS-1$
 		}
 		Assert.isNotNull(refObject, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(refObject.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
 		if (!isCleanModus() || isStrictModus()) {
 			Assert.isTrue(isRegistered(refObject), "refObject must be registered"); //$NON-NLS-1$
 		}
 
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 		// if in clean modus don't do anything in the transaction
 		if (isCleanModus()) {
 			return;
 		}
 		// find delta entry for transacted object, if not found, create one
 		TransactionDelta delta = changesInTransaction.get(object.getObjectId());
 		if (delta == null) {
 			delta = new TransactionDelta(object.getObjectId(), State.CREATED, object.getVersion());
 			changesInTransaction.put(object.getObjectId(), delta);
 		} else {
 			// if the state of the transacted object was clean, change it to
 			// modified
 			if (delta.getState().equals(State.CLEAN)) {
 				delta.setState(State.MODIFIED);
 			}
 		}
 		// add a "remove" entry to the multirefobject
 		delta.removeMultiRefObject(referenceName, refObject.getObjectId());
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#listReference(org.eclipse.riena.objecttransaction.ITransactedObject,
 	 *      java.lang.String, java.util.Set)
 	 */
 	public <T> Set<T> listReference(final ITransactedObject object, final String referenceName, final Set<T> initialSet) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
 		checkPreRegisteredClean();
 		if (isCleanModus() || (!isStrictModus() && !isRegistered(object))) {
 			return initialSet;
 		}
 		Assert.isTrue(isRegistered(object), "object must be registered"); //$NON-NLS-1$
 		Set<T> newSet;
 		// if the passed Set is not a LinkedHashSet, make it one
 		if (initialSet == null) {
 			newSet = new LinkedHashSet<T>();
 		} else {
 			newSet = new LinkedHashSet<T>(initialSet);
 		}
 
 		fillReference(object, referenceName, newSet);
 		return newSet;
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#listReference(org.eclipse.riena.objecttransaction.ITransactedObject,
 	 *      java.lang.String, java.util.List)
 	 */
 	public <T> List<T> listReference(final ITransactedObject object, final String referenceName,
 			final List<T> initialList) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
 		checkPreRegisteredClean();
 		if (isCleanModus() || (!isStrictModus() && !isRegistered(object))) {
 			return initialList;
 		}
 		Assert.isTrue(isRegistered(object), "object must be registered"); //$NON-NLS-1$
 
 		List<T> newList;
 		if (initialList == null) {
 			newList = new ArrayList<T>();
 		} else {
 			newList = new ArrayList<T>(initialList);
 		}
 
 		fillReference(object, referenceName, newList);
 		return newList;
 	}
 
 	@SuppressWarnings({ "unchecked" })
 	// TODO SuppressWarnings: Collection<T> is not Type safety
 	private <T> void fillReference(final ITransactedObject object, final String referenceName,
 			final Collection<T> initialCollection) {
 
 		// if this transaction has a parentTransaction, ask it first to add or
 		// remove their entries
 		if (parentTransaction != null) {
 			parentTransaction.fillReference(object, referenceName, initialCollection);
 		}
 
 		// find the delta object
 		final TransactionDelta delta = changesInTransaction.get(object.getObjectId());
 		if (delta != null) {
 			// iterate over the changes
 			for (final AbstractBaseChange cEntry : delta.getChanges().values()) {
 				// check if is a MultiSetChangeEntry and the referenceName
 				// matches
 				if (cEntry.getRelationName().equals(referenceName) && cEntry instanceof MultipleChange) {
 					// get the set of changes
 					final List<MultipleChangeEntry> changes = ((MultipleChange) cEntry).getEntries();
 					for (final MultipleChangeEntry singleEntry : changes) {
 						final Object tObject = lookupObjectById((IObjectId) singleEntry.getChildObject());
 						// if added, add it to the result set
 						if (singleEntry.getState().equals(State.ADDED)) {
 							initialCollection.add((T) tObject);
 							// if removed, remove it from the result set
 						} else if (singleEntry.getState().equals(State.REMOVED)) {
 							initialCollection.remove(tObject);
 						} else {
 							throw new InvalidTransactionFailure("state was not ADDED or REMOVED for " + singleEntry); //$NON-NLS-1$
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#setVersionUpdate(org.eclipse.riena.objecttransaction.ITransactedObject,
 	 *      java.lang.String)
 	 */
 	public void setVersionUpdate(final ITransactedObject object, final String version) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "object ObjectId must not be null"); //$NON-NLS-1$
 		Assert.isTrue(!isInvalid(), "must not be an invalid transaction"); //$NON-NLS-1$
 		Assert.isTrue(!isCleanModus(), "must not be in clean modus"); //$NON-NLS-1$
 		this.setReference(object, "sys::version", version); //$NON-NLS-1$
 		object.setVersion(version);
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#getVersionUpdate(org.eclipse.riena.objecttransaction.ITransactedObject,
 	 *      java.lang.String)
 	 */
 	// private String getVersionUpdate( ITransactedObject object, String
 	// defaultVersion ) {
 	// Assert.isNotNull( "object must not be null", object );
 	// Assert.isNotNull( "object ObjectId must not be null",
 	// object.getObjectId() );
 	// Assert.isTrue( "must not be an invalid transaction",
 	// !isInvalid() );
 	// if ( !isRegistered( object ) ) {
 	// return defaultVersion;
 	// }
 	// return (String) this.getReference( object, "sys::version", defaultVersion
 	// );
 	// }
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#setObjectIdUpdate(org.eclipse.riena.objecttransaction.IObjectId,
 	 *      org.eclipse.riena.objecttransaction.IObjectId)
 	 */
 	public void setObjectIdUpdate(final IObjectId oldObjectId, final IObjectId newObjectId) {
 		Assert.isTrue(oldObjectId != null && newObjectId != null, "old and new ObjectId must not be null"); //$NON-NLS-1$
 		Assert.isTrue(oldObjectId != newObjectId, "instance of oldObjectId and newObjectId must not be the same"); //$NON-NLS-1$
 		checkPreRegisteredClean();
 		changeObjectId(oldObjectId, newObjectId);
 	}
 
 	private void changeObjectId(final IObjectId oldObjectId, final IObjectId newObjectId) {
 		Assert.isNotNull(oldObjectId, "oldObjectId must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(newObjectId, "newObjectId must not be null"); //$NON-NLS-1$
 		final ITransactedObject transObject = lookupObjectById(oldObjectId);
 		if (transObject == null) {
 			throw new ObjectTransactionFailure("oldObjectId is not registered"); //$NON-NLS-1$
 		}
 		// change the ObjectId in the transactedobject
 		transObject.setObjectId(newObjectId);
 		involvedTransactedObjects.remove(oldObjectId);
 		// change the ObjectId in the list of involvedobjects
 		involvedTransactedObjects.put(newObjectId, transObject);
 		// change the delta record for this ObjectId (if exist)
 		final TransactionDelta delta = changesInTransaction.get(oldObjectId);
 		if (delta != null) {
 			changesInTransaction.remove(oldObjectId);
 			changesInTransaction.put(newObjectId, delta);
 			delta.setObjectId(newObjectId);
 		}
 		// search deltas and find reference to changed ObjectId
 		for (final TransactionDelta delta2 : changesInTransaction.values()) {
 			for (final AbstractBaseChange cEntry : delta2.getChanges().values()) {
 				if (cEntry instanceof SingleChange) {
 					final Object child = ((SingleChange) cEntry).getChildObject();
 					if (child instanceof ITransactedObject) {
 						if (((ITransactedObject) child).getObjectId().equals(oldObjectId)) {
 							((ITransactedObject) child).setObjectId(newObjectId);
 						}
 					}
 				} else {
 					final List<MultipleChangeEntry> changes = ((MultipleChange) cEntry).getEntries();
 					for (final MultipleChangeEntry singleEntry : changes) {
 						if (singleEntry.getChildObject() instanceof ITransactedObject) {
 							if (((ITransactedObject) singleEntry.getChildObject()).getObjectId().equals(oldObjectId)) {
 								((ITransactedObject) singleEntry.getChildObject()).setObjectId(newObjectId);
 							}
 						}
 					}
 				}
 			}
 		}
 		// add change record so that extract contain a notification of the
 		// change
 		this.setReference(transObject, "sys::oid", newObjectId); //$NON-NLS-1$
 		this.setReference(transObject, "sys::oldoid", oldObjectId); //$NON-NLS-1$
 	}
 
 	/**
 	 * 
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#isRegistered(org.eclipse.riena.objecttransaction.ITransactedObject)
 	 */
 	public boolean isRegistered(final ITransactedObject object) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "object ObjectId must not be null"); //$NON-NLS-1$
 		Assert.isTrue(!isInvalid(), "must not be an invalid transaction"); //$NON-NLS-1$
 		checkPreRegisteredClean();
 		// check if a delta entry was found
 		if (changesInTransaction.get(object.getObjectId()) != null) {
 			return true;
 		}
 		// look for delta entry in parent and add business object to this transaction's references
 		if (parentTransaction != null) {
 			keepReferenceOf(object);
 			return parentTransaction.isRegistered(object);
 		}
 		// nothing was found -> object was not registered
 		return false;
 	}
 
 	private boolean isRegistered(final IObjectId object) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isTrue(!isInvalid(), "must not be an invalid transaction"); //$NON-NLS-1$
 		checkPreRegisteredClean();
 		// check if a delta entry was found
 		if (changesInTransaction.get(object) != null) {
 			return true;
 		}
 		// look for delta entry in parent and add business object to this transaction's references
 		if (parentTransaction != null) {
 			return parentTransaction.isRegistered(object);
 		}
 		// nothing was found -> object was not registered
 		return false;
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#notifyObjectChange(org.eclipse.riena.objecttransaction.IObjectId,
 	 *      int)
 	 */
 	private void notifyObjectChange(final ITransactedObject object, final Action action) {
 		Assert.isNotNull(object, "object must not be null"); //$NON-NLS-1$
 		Assert.isNotNull(object.getObjectId(), "ObjectId of object must not be null"); //$NON-NLS-1$
 
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 		keepReferenceOf(object);
 		// don't do anything if in clean modus
 		if (isCleanModus()) {
 			return;
 		}
 		// find the delta entry, create one if it does not exist, apply the
 		// action using the StateMachine
 		TransactionDelta delta = changesInTransaction.get(object.getObjectId());
 		if (delta == null) {
 			delta = new TransactionDelta(object.getObjectId(), StateMachine.initAction(action), object.getVersion());
 			changesInTransaction.put(object.getObjectId(), delta);
 		} else {
 			delta.setState(StateMachine.processAction(delta.getState(), action));
 		}
 		return;
 	}
 
 	/**
 	 * add object to a hashmap of involvedObjects in this objectTransaction
 	 * 
 	 * @param object
 	 *            object of which to keep the reference
 	 */
 	private void keepReferenceOf(final ITransactedObject object) {
 		if (object == null || object.getObjectId() == null) {
 			return;
 		}
 		involvedTransactedObjects.put(object.getObjectId(), object);
 	}
 
 	/**
 	 * add a new object to the list of preregistered transacted objects (those
 	 * with no IObjectId)
 	 * 
 	 * @param object
 	 * @pre isCleanModus()
 	 * @pre object.getObjectId()==null
 	 */
 	private void addPreRegisteredClean(final ITransactedObject object) {
 		Assert.isTrue(isCleanModus(), "can only register objects with no IObjectId in clean modus"); //$NON-NLS-1$
 		Assert.isTrue(object.getObjectId() == null, "can only preregister object where the IObjectId is null"); //$NON-NLS-1$
 		if (preRegisteredCleanObjects == null) {
 			preRegisteredCleanObjects = new ArrayList<ITransactedObject>();
 		}
 		preRegisteredCleanObjects.add(object);
 		return;
 	}
 
 	/**
 	 * preregistered transactedobjects are those that called in a register call,
 	 * but dont had a IObjectId yet this method checks all entries, whether they
 	 * now have a IObjectId. if so they are removed and added into the normal
 	 * hashmaps (which index by IObjectId)
 	 */
 	private void checkPreRegisteredClean() {
 		if (preRegisteredCleanObjects == null || preRegisteredCleanObjects.size() == 0) {
 			return;
 		}
 		for (int i = 0; i < preRegisteredCleanObjects.size(); i++) {
 			final ITransactedObject object = preRegisteredCleanObjects.get(i);
 			if (object.getObjectId() != null) {
 				preRegisteredCleanObjects.remove(object);
 				changesInTransaction.put(object.getObjectId(), new TransactionDelta(object.getObjectId(), State.CLEAN,
 						object.getVersion()));
 				keepReferenceOf(object);
 				i = i - 1;
 			}
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#setCleanModus(boolean)
 	 */
 	public void setCleanModus(final boolean cleanModus) {
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 		checkPreRegisteredClean();
 		this.cleanModus = cleanModus;
 		if (!cleanModus) {
 			if (preRegisteredCleanObjects != null && preRegisteredCleanObjects.size() > 0) {
 				LOGGER.log(
 						LogService.LOG_WARNING,
 						"The cleanModus is set to false in objectTransaction with the consequence that 'preregistered' object (objects with a 'null' oid) are removed and no longe registered. Your ObjectTransaction has " //$NON-NLS-1$
 								+ preRegisteredCleanObjects.size() + " of such objects."); //$NON-NLS-1$
 				for (final ITransactedObject transObject : preRegisteredCleanObjects) {
 					LOGGER.log(LogService.LOG_INFO, "removing " + transObject); //$NON-NLS-1$
 				}
 				preRegisteredCleanObjects = new ArrayList<ITransactedObject>();
 			}
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#isCleanModus()
 	 */
 	public boolean isCleanModus() {
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 		return cleanModus;
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#setStrictModus(boolean)
 	 */
 	public void setStrictModus(final boolean strictModus) {
 		checkPreRegisteredClean();
 		this.strictModus = strictModus;
 	}
 
 	public boolean isStrictModus() {
 		return strictModus;
 	}
 
 	/**
 	 * internal implementation of the commit functionality. There is an internal
 	 * method that performs the actual task, so that it can be used internally
 	 * for other features
 	 */
 	private void internalCommit() {
 		for (final ITransactedObject transObject : involvedTransactedObjects.values()) {
 			if (!(parentTransaction.involvedTransactedObjects.get(transObject.getObjectId()) != null)) {
 				parentTransaction.involvedTransactedObjects.put(transObject.getObjectId(), transObject);
 			}
 			if (!(parentTransaction.changesInTransaction.get(transObject.getObjectId()) != null)) {
 				parentTransaction.changesInTransaction.put(transObject.getObjectId(),
 						new TransactionDelta(transObject.getObjectId(), State.CLEAN, transObject.getVersion()));
 			}
 		}
 		for (final TransactionDelta delta : changesInTransaction.values()) {
 			final ITransactedObject usedObject = lookupObjectById(delta.getObjectId());
 			// if not registered in parent, register and set same state as in
 			// this transaction
 			if (!parentTransaction.isRegistered(usedObject)) {
 				parentTransaction.setObjectState(usedObject, getObjectState(usedObject));
 			}
 			for (final AbstractBaseChange cEntry : delta.getChanges().values()) {
 				if (cEntry instanceof SingleChange) {
 					parentTransaction.setReference(usedObject, ((SingleChange) cEntry).getRelationName(),
 							((SingleChange) cEntry).getChildObject());
 				} else {
 					final List<MultipleChangeEntry> changes = ((MultipleChange) cEntry).getEntries();
 					for (final MultipleChangeEntry singleEntry : changes) {
 						if (singleEntry.getState().equals(State.ADDED)) {
 							parentTransaction.addReference(usedObject, ((MultipleChange) cEntry).getRelationName(),
 									lookupObjectById((IObjectId) singleEntry.getChildObject()));
 						} else if (singleEntry.getState().equals(State.REMOVED)) {
 							parentTransaction.removeReference(usedObject, ((MultipleChange) cEntry).getRelationName(),
 									lookupObjectById((IObjectId) singleEntry.getChildObject()));
 						} else {
 							throw new InvalidTransactionFailure("state is not ADD and not REMOVED for " + singleEntry); //$NON-NLS-1$
 						}
 					}
 				}
 			}
 			if (!(delta.getState().equals(State.CLEAN))) {
 				parentTransaction.setObjectState(usedObject,
 						StateMachine.mergeStates(parentTransaction.getObjectState(usedObject), delta.getState()));
 			}
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#commit()
 	 */
 	public void commit() {
 		Assert.isTrue(!isInvalid(), "must not be an invalid transaction"); //$NON-NLS-1$
 		Assert.isTrue(!isRootTransaction(), "must not be root transaction to commit"); //$NON-NLS-1$
 		// call the internal commit method
 		internalCommit();
 		// clean the deltas, this mainly is good so that it releases all
 		// references to external objects
 		// however the objectTransaction becomes invalid anyway and cannot be
 		// used further on
 		// UPDATE: no longer true since the objecttransaction is no longer
 		// invalidated
 		clearChanges();
 		// make the transaction invalid (UPDATE: no longer !!!)
 		// invalidate();
 		// make the current transaction the parent transaction of this
 		// transaction
 		ObjectTransactionManager.getInstance().setCurrent(parentTransaction);
 	}
 
 	private Method findMethod(final Class<?> clazz, final String name, final String methodPrefix, final Object arg) {
 		final String methodName = methodPrefix + name.substring(0, 1).toUpperCase() + name.substring(1);
 
 		// find methods in this class
 		for (final Method method : clazz.getDeclaredMethods()) {
 			if (method.getName().equals(methodName)) {
 				final Class<?>[] parmType = method.getParameterTypes();
 				if (parmType.length == 1) {
 					if ((arg != null && parmType[0].isAssignableFrom(arg.getClass())) || arg == null) {
 						method.setAccessible(true);
 						return method;
 					}
 				} else {
 					if (parmType.length == 0 && arg == null) {
 						method.setAccessible(true);
 						return method;
 					}
 				}
 			}
 		}
 		// if no matching methods where found try superclass
 		final Class<?> superClass = clazz.getSuperclass();
 		if (!superClass.equals(Object.class)) {
 			return findMethod(superClass, name, methodPrefix, arg);
 		}
 		throw new ObjectTransactionFailure("ITransactedObject " + clazz + " must have method " + methodName //$NON-NLS-1$ //$NON-NLS-2$
 				+ " but lookup fails."); //$NON-NLS-1$
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#commitToObjects()
 	 */
 	public void commitToObjects() {
 		Assert.isTrue(!isInvalid(), "must not be an invalid transaction"); //$NON-NLS-1$
 		Assert.isTrue(isRootTransaction(), "must be rootTransaction"); //$NON-NLS-1$
 
 		final boolean savedCleanModus = cleanModus;
 		cleanModus = false;
 
 		// iterate through all changes in the objectTransaction
 		for (final TransactionDelta delta : changesInTransaction.values()) {
 			final ITransactedObject usedObject = lookupObjectById(delta.getObjectId());
 			// iterate through all properties or relation changes for one
 			// transactedobject
 			for (final AbstractBaseChange cEntry : delta.getChanges().values()) {
 				final String refName = cEntry.getRelationName();
 				try {
 					if (cEntry instanceof SingleChange) {
 						if (refName.equals("sys::oid") || refName.equals("sys::oldoid") || refName.equals("sys::version")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 							continue;
 						}
 						applySingleChangeToBean(usedObject, cEntry, refName);
 					} else {
 						applyMultipleChangeToBean(usedObject, cEntry, refName);
 					}
 				} catch (final IllegalAccessException e) {
 					throw new ObjectTransactionFailure("access to field blocked field " + refName + " in object " //$NON-NLS-1$ //$NON-NLS-2$
 							+ usedObject, e);
 				} catch (final InvocationTargetException e2) {
 					throw new ObjectTransactionFailure("problem while accessing field blocked field " + refName //$NON-NLS-1$
 							+ " in object " + usedObject, e2); //$NON-NLS-1$
 				}
 			}
 		}
 
 		// do a second iterate and remove deleted objects from involved object
 		// list
 		for (final TransactionDelta delta : changesInTransaction.values()) {
 			if (delta.getState().equals(State.DELETED)) {
 				final ITransactedObject usedObject = lookupObjectById(delta.getObjectId());
 				involvedTransactedObjects.remove(usedObject.getObjectId());
 			}
 		}
 		cleanModus = savedCleanModus;
 
 		// only clear the delta changes, keep the referenced transacted objects
 		changesInTransaction = new HashMap<IObjectId, TransactionDelta>();
 		final Collection<ITransactedObject> transactedObject = involvedTransactedObjects.values();
 		if (transactedObject != null) {
 			final ITransactedObject[] arrayValues = transactedObject.toArray(new ITransactedObject[transactedObject
 					.size()]);
 			for (final ITransactedObject object : arrayValues) {
 				setObjectState(object, State.CLEAN);
 			}
 		}
 	}
 
 	private void applyMultipleChangeToBean(final ITransactedObject usedObject, final AbstractBaseChange cEntry,
 			final String refName) throws IllegalAccessException, InvocationTargetException {
 		cleanModus = true;
 		Method addMethod = null;
 		Method removeMethod = null;
 		for (final MultipleChangeEntry singleEntry : ((MultipleChange) cEntry).getEntries()) {
 			if (singleEntry.getState().equals(State.ADDED)) {
 				final Object value = lookupObjectById((IObjectId) singleEntry.getChildObject());
 				if (addMethod == null) {
 					addMethod = findMethod(usedObject.getClass(), refName, "add", value); //$NON-NLS-1$
 				}
 				addMethod.invoke(usedObject, new Object[] { value });
 			} else if (singleEntry.getState().equals(State.REMOVED)) {
 				final Object value = lookupObjectById((IObjectId) singleEntry.getChildObject());
 				if (removeMethod == null) {
 					removeMethod = findMethod(usedObject.getClass(), refName, "remove", value); //$NON-NLS-1$
 				}
 				removeMethod.invoke(usedObject, new Object[] { value });
 			}
 		}
 		cleanModus = false;
 	}
 
 	private void applySingleChangeToBean(final ITransactedObject usedObject, final AbstractBaseChange cEntry,
 			final String refName) {
 		try {
 			// get the current value from the bean, which should ask
 			// the objectTransaction
 			// Method getMethod = findMethod( usedObject.getClass(),
 			// refName, "get", null );
 			// Object value = getMethod.invoke( usedObject, new
 			// Object[] {} );
 			Object value = ((SingleChange) cEntry).getChildObject();
 			if (value instanceof IObjectId) {
 				value = lookupObjectById((IObjectId) value);
 			}
 			// set it into the bean using clean modus, so no
 			// objectTransaction is involved
 			final Method setMethod = findMethod(usedObject.getClass(), refName, "set", value); //$NON-NLS-1$
 			cleanModus = true;
 			setMethod.invoke(usedObject, new Object[] { value });
 			cleanModus = false;
 		} catch (final IllegalAccessException e) {
 			throw new ObjectTransactionFailure("access to field blocked field " + refName + " in object " //$NON-NLS-1$ //$NON-NLS-2$
 					+ usedObject, e);
 		} catch (final InvocationTargetException e2) {
 			throw new ObjectTransactionFailure("problem while accessing field blocked field " + refName //$NON-NLS-1$
 					+ " in object " + usedObject, e2); //$NON-NLS-1$
 		}
 	}
 
 	public void rollback() {
 		if (isInvalid()) {
 			throw new InvalidTransactionFailure("object transaction is invalid"); //$NON-NLS-1$
 		}
 		clearChanges();
 		if (!isRootTransaction()) {
 			// UPDATE: no longer invalidated so that it can be reused
 			// invalidate();
 			ObjectTransactionManager.getInstance().setCurrent(parentTransaction);
 		}
 	}
 
 	/**
 	 * clear all changes in this transaction
 	 * 
 	 */
 	private void clearChanges() {
 		changesInTransaction = new HashMap<IObjectId, TransactionDelta>();
 		involvedTransactedObjects = new HashMap<IObjectId, ITransactedObject>();
 	}
 
 	/**
 	 * find a transacted object instance by its IObjectId, also check
 	 * parentTransactions
 	 * 
 	 * @param objectId
 	 *            object id of the transacted object that we are looking for
 	 * @return ITransactedObject of the found object or null if no entry was
 	 *         found
 	 */
 	public ITransactedObject lookupObjectById(final IObjectId objectId) {
 		checkPreRegisteredClean();
 		// find object by oid
 		ITransactedObject tObject = involvedTransactedObjects.get(objectId);
 		// if not found, ask parentTransaction, if found there add to this
 		// transactions list of involvedobjects
 		if (tObject == null && parentTransaction != null) {
 			tObject = parentTransaction.lookupObjectById(objectId);
 			keepReferenceOf(tObject);
 		}
 		return tObject;
 	}
 
 	/**
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#replaceRegisteredObject(org.eclipse.riena.objecttransaction.IObjectId,
 	 *      org.eclipse.riena.objecttransaction.ITransactedObject)
 	 */
 	public void replaceRegisteredObject(final IObjectId objectId, final ITransactedObject transactedObject) {
 		Assert.isTrue(objectId.equals(transactedObject.getObjectId()),
 				"oldObjectId and new transactedobject must have the an 'equal' OID"); //$NON-NLS-1$
 		final ITransactedObject tObject = involvedTransactedObjects.get(objectId);
 		Assert.isTrue(
 				transactedObject != tObject,
 				"object instances of the existing registered transacted object and the new object must not be the same or this call is meaningless"); //$NON-NLS-1$
 		if (tObject != null) {
 			involvedTransactedObjects.put(tObject.getObjectId(), transactedObject);
 		}
 		if (parentTransaction != null) {
 			parentTransaction.replaceRegisteredObject(objectId, transactedObject);
 		}
 
 	}
 
 	/**
 	 * invalidate this objectTransaction
 	 */
 	@SuppressWarnings("unused")
 	// currently unused thats why it is private but maybe we need it later
 	private void invalidate() {
 		invalidTransaction = true;
 	}
 
 	/**
 	 * 
 	 * @see org.eclipse.riena.objecttransaction.IObjectTransaction#isInvalid()
 	 */
 	public boolean isInvalid() {
 		return invalidTransaction;
 	}
 
 	/**
 	 * Internal method for testcases to test if the state is correct
 	 * 
 	 * @param object
 	 * @param state
 	 * @return
 	 */
 	public boolean isState(final ITransactedObject object, final State state) {
 		final TransactionDelta delta = changesInTransaction.get(object.getObjectId());
 		if (delta == null) {
 			return false;
 		}
 		return delta.getState().equals(state);
 	}
 
 	/**
 	 * pretty printing of the content of this object transaction
 	 * 
 	 * @return String
 	 */
 	@Override
 	public String toString() {
 		final StringBuilder sb = new StringBuilder();
 		sb.append("----------transaction----------------\n"); //$NON-NLS-1$
 		if (!isInvalid()) {
 			for (final TransactionDelta delta : this.exportExtract().getDeltas()) {
 				sb.append(delta);
 			}
 		} else {
 			sb.append("Transaction is invalid and cannot be displayed."); //$NON-NLS-1$
 		}
 		sb.append("----------transaction----------------\n"); //$NON-NLS-1$
 		return sb.toString();
 	}
 
 }
