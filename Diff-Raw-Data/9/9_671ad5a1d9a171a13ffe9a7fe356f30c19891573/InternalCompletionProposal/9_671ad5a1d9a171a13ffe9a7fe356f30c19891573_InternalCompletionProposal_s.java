 /*******************************************************************************
  * Copyright (c) 2004, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.codeassist;
 
 import org.eclipse.dltk.core.IAccessRule;
 
 public class InternalCompletionProposal {	
 	private int accessibility;
 	
 	private boolean isConstructor;
 	
 	protected InternalCompletionProposal() {
 		accessibility = IAccessRule.K_ACCESSIBLE;
 		isConstructor = false;
 	}
 
 	/**
 	 * Returns the accessibility of the proposal.
 	 * <p>
 	 * This field is available for the following kinds of completion proposals:
 	 * <ul>
 	 * <li><code>TYPE_REF</code> - accessibility of the type</li>
 	 * </ul>
 	 * For these kinds of completion proposals, this method returns
 	 * {@link IAccessRule#K_ACCESSIBLE} or {@link IAccessRule#K_DISCOURAGED} or
 	 * {@link IAccessRule#K_NON_ACCESSIBLE}. By default this method return
 	 * {@link IAccessRule#K_ACCESSIBLE}.
 	 * </p>
 	 * 
 	 * @see IAccessRule
 	 * 
 	 * @return the accessibility of the proposal
 	 * 
 	 */
 	public int getAccessibility() {
 		return this.accessibility;
 	}
 
 	/**
 	 * Returns whether this proposal is a constructor.
 	 * <p>
 	 * This field is available for the following kinds of completion proposals:
 	 * <ul>
 	 * <li><code>METHOD_REF</code> - return <code>true</code> if the
 	 * referenced method is a constructor</li>
 	 * <li><code>METHOD_DECLARATION</code> - return <code>true</code> if
 	 * the declared method is a constructor</li>
 	 * </ul>
 	 * For kinds of completion proposals, this method returns <code>false</code>.
 	 * </p>
 	 * 
 	 * @return <code>true</code> if the proposal is a constructor.
 	 * 
 	 */
 	public boolean isConstructor() {
 		return this.isConstructor;
 	}
 }
