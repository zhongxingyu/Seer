 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.ui.text.completion;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.javascript.internal.ui.templates.JavaScriptTemplateCompletionProcessor;
 import org.eclipse.dltk.ui.text.completion.ContentAssistInvocationContext;
 import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposal;
 import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposalCollector;
 import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposalComputer;
 import org.eclipse.dltk.ui.text.completion.ScriptContentAssistInvocationContext;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
 
public class JavaScriptTypeCompletionProposalComputer extends
		ScriptCompletionProposalComputer {
 
 	public JavaScriptTypeCompletionProposalComputer() {
 	}
 
 	public List computeCompletionProposals(
 			ContentAssistInvocationContext context, IProgressMonitor monitor) {
 		List types = super.computeCompletionProposals(context, monitor);
 		return types;
 	}

 	protected TemplateCompletionProcessor createTemplateProposalComputer(
 			ScriptContentAssistInvocationContext context) {
 		return new JavaScriptTemplateCompletionProcessor(context);
 	}
 
 	public List computeContextInformation(
 			ContentAssistInvocationContext context, IProgressMonitor monitor) {
 		System.out.println("Offset: " + context.getInvocationOffset());
 
 		if (DLTKCore.DEBUG) {
			System.out
					.println("TclTypeCompletionProposalComputer.computeContextInformation()");
 		}
 		// if (context instanceof ScriptContentAssistInvocationContext) {
 		// ScriptContentAssistInvocationContext scriptContext=
 		// (ScriptContentAssistInvocationContext) context;
 		//			
 		// int contextInformationPosition=
 		// guessContextInformationPosition(scriptContext);
 		// List result= addContextInformations(scriptContext,
 		// contextInformationPosition, monitor);
 		// return result;
 		// }
 		// return Collections.EMPTY_LIST;
 
 		List types = computeCompletionProposals(context, monitor);
 		if (DLTKCore.DEBUG) {
 			System.out.println("!!! Proposals: " + types.size());
 		}
 		Iterator iter = types.iterator();
 
 		List list = new ArrayList();
 		while (iter.hasNext()) {
			Object next = iter.next();
			if (!(next instanceof IScriptCompletionProposal))
				continue;
			IScriptCompletionProposal proposal = (IScriptCompletionProposal) next;
			IContextInformation contextInformation = proposal
					.getContextInformation();
			if (contextInformation == null) {
 				continue;
 			}
 			if (DLTKCore.DEBUG) {
				System.out.println("Proposal: " + proposal + ", info: "
						+ contextInformation.getInformationDisplayString());
 			}
 			list.add(contextInformation);
 		}
 		return list;
 	}
 
 	public String getErrorMessage() {
 		return null;
 	}
 
 	public void sessionEnded() {

 	}
 
 	public void sessionStarted() {

 	}
 
 	protected ScriptCompletionProposalCollector createCollector(
 			ScriptContentAssistInvocationContext context) {
		return new JavaScriptCompletionProposalCollector(context
				.getSourceModule());
 	}
 
 }
