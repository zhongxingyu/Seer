 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.text.completion;
 
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.PreferenceConstants;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 
 public class ScriptMethodCompletionProposal extends
 		LinkedModeScriptCompletionProposal implements
 		IScriptCompletionProposalExtension2 {
 	/**
 	 * Triggers for method proposals without parameters. Do not modify.
 	 */
 	protected final static char[] METHOD_TRIGGERS = new char[] { ';', ',', '.',
 			'\t', '[' };
 
 	/**
 	 * Triggers for method proposals. Do not modify.
 	 */
 	protected final static char[] METHOD_WITH_ARGUMENTS_TRIGGERS = new char[] {
 			'(', '-', ' ' };
 
 	/** Triggers for method name proposals (static imports). Do not modify. */
 	protected final static char[] METHOD_NAME_TRIGGERS = new char[] { ';' };
 
 	private boolean fHasParameters;
 	private boolean fHasParametersComputed = false;
 	private int fContextInformationPosition;
 
 	public ScriptMethodCompletionProposal(CompletionProposal proposal,
 			ScriptContentAssistInvocationContext context) {
 		super(proposal, context);
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public String getName() {
 		return fProposal.getName();
 	}
 
 	@Override
 	public CharSequence getPrefixCompletionText(IDocument document,
 			int completionOffset) {
 		if (hasArgumentList()) {
 			String completion = String.valueOf(fProposal.getName());
 			if (isCamelCaseMatching()) {
 				String prefix = getPrefix(document, completionOffset);
 				return getCamelCaseCompound(prefix, completion);
 			}
 
 			return completion;
 		}
 		return super.getPrefixCompletionText(document, completionOffset);
 	}
 
 	@Override
 	protected IContextInformation computeContextInformation() {
 		// no context information for METHOD_NAME_REF proposals (e.g. for static
 		// imports)
 		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94654
 		if (fProposal.getKind() == CompletionProposal.METHOD_REF
 				&& hasParameters()
 				&& (getReplacementString().endsWith(RPAREN) || getReplacementString()
 						.length() == 0)) {
 			ProposalContextInformation contextInformation = new ProposalContextInformation(
 					fProposal);
 			if (fContextInformationPosition != 0
 					&& fProposal.getCompletion().length() == 0)
 				contextInformation
 						.setContextInformationPosition(fContextInformationPosition);
 			return contextInformation;
 		}
 		return super.computeContextInformation();
 	}
 
 	@Override
 	protected char[] computeTriggerCharacters() {
 		if (fProposal.getKind() == CompletionProposal.METHOD_NAME_REFERENCE)
 			return METHOD_NAME_TRIGGERS;
 
 		if (hasParameters())
 			return METHOD_WITH_ARGUMENTS_TRIGGERS;
 
 		return METHOD_TRIGGERS;
 	}
 
 	/**
 	 * Returns <code>true</code> if the method being inserted has at least one
 	 * parameter. Note that this does not say anything about whether the
 	 * argument list should be inserted. This depends on the position in the
 	 * document and the kind of proposal; see {@link #hasArgumentList() }.
 	 * 
 	 * @return <code>true</code> if the method has any parameters,
 	 *         <code>false</code> if it has no parameters
 	 */
 	protected final boolean hasParameters() {
 		if (!fHasParametersComputed) {
 			fHasParametersComputed = true;
 			String[] findParameterNames = fProposal.findParameterNames(null);
 			fHasParameters = findParameterNames != null
 					&& findParameterNames.length > 0;
 		}
 		return fHasParameters;
 	}
 
 	// private boolean computeHasParameters() throws IllegalArgumentException {
 	// return Signature.getParameterCount(fProposal.getSignature()) > 0;
 	// }
 
 	/**
 	 * Returns <code>true</code> if the argument list should be inserted by the
 	 * proposal, <code>false</code> if not.
 	 * 
 	 * @return <code>true</code> when the proposal is not in javadoc nor within
 	 *         an import and comprises the parameter list
 	 */
 	protected boolean hasArgumentList() {
 		if (CompletionProposal.METHOD_NAME_REFERENCE == fProposal.getKind())
 			return false;
 		IPreferenceStore preferenceStore = DLTKUIPlugin.getDefault()
 				.getPreferenceStore();
 		boolean noOverwrite = preferenceStore
 				.getBoolean(PreferenceConstants.CODEASSIST_INSERT_COMPLETION)
 				^ isToggleEating();
 		String completion = fProposal.getCompletion();
 		return !isInDoc()
 				&& completion.length() > 0
 				&& (noOverwrite || completion.charAt(completion.length() - 1) == ')');
 	}
 
 	@Override
 	protected char getOpenTrigger() {
 		return '(';
 	}
 
 	@Override
 	protected char getExitTrigger() {
 		return ')';
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected void computeReplacement(ReplacementBuffer buffer) {
 		if (!hasArgumentList()) {
			buffer.append(fProposal.getCompletion());
 			return;
 		}
 
 		// we're inserting a method plus the argument list - respect formatter
 		// preferences
 		buffer.append(fProposal.getName());
 
 		// FormatterPrefs prefs= getFormatterPrefs();
 		// if (prefs.beforeOpeningParen)
 		// buffer.append(SPACE);
 		buffer.append(LPAREN);
 
 		if (hasParameters()) {
 			setCursorPosition(buffer.length());
 
 			// if (prefs.afterOpeningParen)
 			// buffer.append(SPACE);
 
 			String[] parameterNames = fProposal.findParameterNames(null);
 			for (int i = 0; i < parameterNames.length; ++i) {
 				if (i != 0) {
 					buffer.append(COMMA);
 				}
 				buffer.addArgument(parameterNames[i]);
 			}
 
 			// don't add the trailing space, but let the user type it in himself
 			// - typing the closing paren will exit
 			// if (prefs.beforeClosingParen)
 			// buffer.append(SPACE);
 		} else {
 			// if (prefs.inEmptyList)
 			// buffer.append(SPACE);
 		}
 
 		buffer.append(RPAREN);
 	}
 
 	@Override
 	protected ProposalInfo computeProposalInfo() {
 		IScriptProject project = fInvocationContext.getProject();
 		if (project != null)
 			return new MethodProposalInfo(project, fProposal);
 		return super.computeProposalInfo();
 	}
 
 	/**
 	 * Overrides the default context information position. Ignored if set to
 	 * zero.
 	 * 
 	 * @param contextInformationPosition
 	 *            the replaced position.
 	 */
 	public void setContextInformationPosition(int contextInformationPosition) {
 		fContextInformationPosition = contextInformationPosition;
 	}
 
 	@Override
 	protected boolean isValidPrefix(String prefix) {
 		if (super.isValidPrefix(prefix))
 			return true;
 
 		String word = getDisplayString();
 		if (isInDoc()) {
 			int idx = word.indexOf("{@link "); //$NON-NLS-1$
 			if (idx == 0) {
 				word = word.substring(7);
 			} else {
 				idx = word.indexOf("{@value "); //$NON-NLS-1$
 				if (idx == 0) {
 					word = word.substring(8);
 				}
 			}
 		}
 		return isPrefix(prefix, word);
 	}
 
 }
