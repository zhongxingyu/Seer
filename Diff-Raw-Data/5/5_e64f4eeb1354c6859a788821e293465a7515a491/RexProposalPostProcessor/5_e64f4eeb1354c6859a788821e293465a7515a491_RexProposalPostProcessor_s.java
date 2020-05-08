 /*******************************************************************************
  * Copyright (c) 2006-2011
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany 
  *      - initial API and implementation
  ******************************************************************************/
 package org.reuseware.comogen.reuseextension.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.reuseware.coconut.resource.ReuseResources;
 import org.reuseware.coconut.reuseextension.AddressablePointDerivationRule;
 import org.reuseware.coconut.reuseextension.CompositionAssociation2CompositionLinkBinding;
 import org.reuseware.coconut.reuseextension.ReuseExtension;
 import org.reuseware.coconut.reuseextension.RuleContext;
 import org.reuseware.coconut.reuseextension.evaluator.EvaluatorUtil;
 
 /**
  * This post processor extends the completion proposal list with proposals for
  * embedded expressions.
  */
 public class RexProposalPostProcessor {
 	
 	/**
 	 * @param proposals list of proposals computed so far (by generated code).
 	 * @return extended proposal list.
 	 */
 	public List<RexCompletionProposal> process(List<RexCompletionProposal> proposals) {
 		// the default implementation does returns the proposals as they are
 		if (!proposals.isEmpty()) {
 			RexCompletionProposal placeholderProposal = proposals.get(0);
 			EObject container = placeholderProposal.getContainer();
 			if (placeholderProposal.getStructuralFeature() != null && placeholderProposal.getStructuralFeature().getName().endsWith("Expression")) {
 				List<RexCompletionProposal> newProposals = new ArrayList<RexCompletionProposal>(proposals);
 				RuleContext ruleContext = null;
 				EClass eClass = null;
 				while (container.eContainer() != null) {
 					if (container instanceof RuleContext) {
 						ruleContext = (RuleContext) container;
 						break;
 					}
 					container = container.eContainer();
 				}
 				ReuseExtension reuseExtension = (ReuseExtension) EcoreUtil.getRootContainer(container);
 				if (ruleContext == null && !reuseExtension.getRootElementContexts().isEmpty()) {
 					ruleContext = reuseExtension.getRootElementContexts().get(0);
 				}
 				if (ruleContext != null) {
 					eClass = ruleContext.getEBoundClass();
 				} else {
 					eClass = EcorePackage.eINSTANCE.getEObject();
 				}
 				if (ruleContext instanceof CompositionAssociation2CompositionLinkBinding) {
 					CompositionAssociation2CompositionLinkBinding binding =
 						(CompositionAssociation2CompositionLinkBinding) ruleContext;
 					if (placeholderProposal.getStructuralFeature().getName().contains("Instance1") && binding.getForEach1Expression() != null) {
 						eClass = EvaluatorUtil.getResultType(
 								binding.getEBoundClass(), binding.getForEach1Expression(), reuseExtension.getParameters());
 					} else if (placeholderProposal.getStructuralFeature().getName().contains("Instance2") && binding.getForEach2Expression() != null) {
 						eClass = EvaluatorUtil.getResultType(
 								binding.getEBoundClass(), binding.getForEach2Expression(), reuseExtension.getParameters());
 					}
 				}
 				if (ruleContext instanceof AddressablePointDerivationRule) {
 					AddressablePointDerivationRule derivationRule =
 						(AddressablePointDerivationRule) ruleContext;
 					if (placeholderProposal.getStructuralFeature().getName().contains("NameExpression") && derivationRule.getForEachExpression() != null) {
 						eClass = EvaluatorUtil.getResultType(
 								derivationRule.getEBoundClass(), derivationRule.getForEachExpression(), reuseExtension.getParameters());
 					}
 				}
 				
 				String expressionStart = placeholderProposal.getPrefix();
 				
 				if (expressionStart.length() > 1 && expressionStart.endsWith("$")) {
 					return Collections.emptyList();
 				}
 				
 				boolean addEscape = false;
 				if (expressionStart.startsWith("$")) {
 					expressionStart = expressionStart.substring(1);
 				} else {
 					addEscape = true;
 				}
 				for (String proposalString : EvaluatorUtil.getCompletionProposals(
 						eClass, expressionStart, reuseExtension.getParameters())) {
 					if (addEscape) {
 						proposalString = "$" + proposalString + "$";
 					}
					newProposals.add(new RexCompletionProposal(proposalString, expressionStart, true, 
 							placeholderProposal.getStructuralFeature(), ruleContext));
 				}
 				
 				newProposals.remove(placeholderProposal);
 				return newProposals;
 			} else if (placeholderProposal.getInsertString().equals("someFracolNamespace")) {
 				List<RexCompletionProposal> newProposals = new ArrayList<RexCompletionProposal>();
 				for (List<String> rexID : ReuseResources.INSTANCE.getAllFragmentCollaborationIDs()) {
 					String insertString = null;
 					for (String segment : rexID) {
 						if (insertString == null) {
 							insertString = "";
 						} else {
 							insertString += ".";
 						}
 						insertString = insertString + segment;
 					}
 					insertString = insertString.substring(0, insertString.length() - ".fracol".length());
 					
 					RexCompletionProposal proposal = new RexCompletionProposal(
							insertString, placeholderProposal.getPrefix(), true, 
 							placeholderProposal.getStructuralFeature(), placeholderProposal.getContainer());
 					newProposals.add(proposal);
 				}
 				return newProposals;
 			}
 		}
 		return proposals;
 	}
 	
 }
