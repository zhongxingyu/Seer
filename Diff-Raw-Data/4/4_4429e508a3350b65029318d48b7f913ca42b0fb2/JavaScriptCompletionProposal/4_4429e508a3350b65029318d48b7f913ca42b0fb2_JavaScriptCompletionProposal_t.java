 package org.eclipse.dltk.javascript.internal.ui.text.completion;
 
 import org.eclipse.dltk.ui.text.completion.ProposalInfo;
 import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposal;
 import org.eclipse.jface.text.contentassist.ContextInformation;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.swt.graphics.Image;
 
 public class JavaScriptCompletionProposal extends ScriptCompletionProposal {
 
 	public JavaScriptCompletionProposal(String replacementString,
 			int replacementOffset, int replacementLength, Image image,
 			String displayString, int relevance) {
 		super(replacementString, replacementOffset, replacementLength, image,
 				displayString, relevance);
 		;
 		ProposalInfo proposalInfo = new ProposalInfo(null);
 		proposalInfo.setHackMessage("<h1>Fuck</h1>");
 		this.setProposalInfo(proposalInfo);
 	}
 
 	public JavaScriptCompletionProposal(String replacementString,
 			int replacementOffset, int replacementLength, Image image,
 			String displayString, int relevance, boolean isInDoc) {
 		super(replacementString, replacementOffset, replacementLength, image,
 				displayString, relevance, isInDoc);
 		ProposalInfo proposalInfo = new ProposalInfo(null);
 		proposalInfo.setHackMessage("<h1>Fuck</h1>");
 		this.setProposalInfo(proposalInfo);
 	}
 
 	protected boolean isSmartTrigger(char trigger) {
 		if (trigger == '$') {
 			return true;
 		}
 		return false;
 	}
 }
