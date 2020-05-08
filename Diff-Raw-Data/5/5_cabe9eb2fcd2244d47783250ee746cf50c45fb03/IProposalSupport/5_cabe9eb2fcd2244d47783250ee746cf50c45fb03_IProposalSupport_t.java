 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.func;
 
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 
 /**
  *
  */
 public interface IProposalSupport {
 
 	/**
 	 * This value will be used if the cell editor is a combo box. This
 	 * functionality only applies to TYPE_DROPDOWN.
 	 * 
 	 * @param context
 	 *            the context
 	 * @return the possible values for the combo box.
 	 */
 	IProposal[] getPossibleValues(IDirectEditingContext context);
 
 	/**
 	 * This proposals will be used for the completion list of a simple text cell
 	 * editor. This functionality only applies to TYPE_TEXT.
 	 * 
 	 * @param value
 	 *            current value
 	 * @param caretPosition
 	 *            current cursor position
 	 * @param context
 	 *            the context
 	 * @return the proposed values
 	 */
 	IProposal[] getValueProposals(String value, int caretPosition, IDirectEditingContext context);
 
 	/**
 	 * Framework calls this method to let the feature calculate the new value.
 	 * 
 	 * @param value
 	 *            current value
 	 * @param caretPosition
 	 *            current cursor position
 	 * @param choosenValue
 	 *            value choosen by user
 	 * @param context
 	 *            the context
 	 * @return the new value
 	 */
 	String completeValue(String value, int caretPosition, IProposal choosenValue, IDirectEditingContext context);
 
 	/**
 	 * This method will be called by clients many times to see if current value
 	 * is valid and could be set.
 	 * 
 	 * @param text
 	 *            the value as text
 	 * @param proposal
 	 *            the value as proposal
 	 * @param context
 	 *            the context
 	 * @return null if value is okay and could be set; any text means value is
 	 *         not valid; text is reason for invalidality
 	 */
 	String checkValueValid(String text, IProposal proposal, IDirectEditingContext context);
 
 	/**
 	 * Set the new value. The value comes from the text editing UI component.
 	 * 
 	 * @param value
 	 *            the value
	 * @param proposal
	 *            the choosen proposal
 	 * @param context
 	 *            the context
 	 */
	void setValue(String value, IProposal proposal, IDirectEditingContext context);
 }
