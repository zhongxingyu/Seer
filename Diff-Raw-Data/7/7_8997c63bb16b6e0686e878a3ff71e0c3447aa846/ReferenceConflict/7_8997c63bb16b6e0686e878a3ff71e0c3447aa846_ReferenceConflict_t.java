 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.model.changeTracking.merging.conflict.conflicts;
 
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.emfstore.client.model.changeTracking.merging.conflict.Conflict;
 import org.eclipse.emf.emfstore.client.model.changeTracking.merging.conflict.ConflictContext;
 import org.eclipse.emf.emfstore.client.model.changeTracking.merging.conflict.ConflictDescription;
 import org.eclipse.emf.emfstore.client.model.changeTracking.merging.conflict.ConflictOption;
 import org.eclipse.emf.emfstore.client.model.changeTracking.merging.conflict.ConflictOption.OptionType;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 
 /**
  * Container for {@link MultiReferenceConflict} and {@link SingleReferenceConflict}.
  * 
  * @author wesendon
  */
 public class ReferenceConflict extends Conflict {
 
 	private final Conflict conflict;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param conflict underlying conflict, {@link MultiReferenceConflict} or {@link SingleReferenceConflict}
 	 * @param myOps list of my operations
 	 * @param theirOps list of their operations
 	 * @param leftOperation the operation representing all left operations
 	 * @param rightOperation the operation representing all right operations
 	 */
 	public ReferenceConflict(Conflict conflict, Set<AbstractOperation> myOps, Set<AbstractOperation> theirOps,
 		AbstractOperation leftOperation, AbstractOperation rightOperation) {
 		super(myOps, theirOps, leftOperation, rightOperation, conflict.getDecisionManager(), conflict.isLeftMy(), false);
 		if (!(conflict instanceof SingleReferenceConflict || conflict instanceof MultiReferenceConflict)) {
 			throw new IllegalStateException("Only reference conflicts allowed.");
 		}
 		this.conflict = conflict;
 		init();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected boolean allowOtherOptions() {
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected ConflictContext initConflictContext() {
 		return conflict.getConflictContext();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected ConflictDescription initConflictDescription(ConflictDescription desc) {
 		return conflict.getConflictDescription();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void initConflictOptions(List<ConflictOption> options) {
 		for (ConflictOption option : conflict.getOptions()) {
 			if (option.getType() == OptionType.MyOperation) {
				option.addOperations(getLeftOperations());
 			} else if (option.getType() == OptionType.TheirOperation) {
				option.addOperations(getRightOperations());
 			}
 			options.add(option);
 		}
 	}
 
 }
