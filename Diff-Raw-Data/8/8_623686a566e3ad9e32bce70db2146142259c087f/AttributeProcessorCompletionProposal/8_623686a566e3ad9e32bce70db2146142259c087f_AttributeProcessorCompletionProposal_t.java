 /*
  * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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
 
 package nz.net.ultraq.eclipse.thymeleaf.contentassist;
 
 import nz.net.ultraq.eclipse.thymeleaf.ThymeleafPlugin;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 
 /**
  * A completion proposal for the Thymeleaf attribute processors.
  * 
  * @author Emanuel Rabina
  */
 public class AttributeProcessorCompletionProposal implements ICompletionProposal, ICompletionProposalExtension {
 
 	private final String fullprocessorname;
 	private final String replacementstring;
 	private final int cursorposition;
 
 	private final IContextInformation contextinformation;
 	private final String additionalproposalinfo;
 
 	/**
 	 * Constructor, creates a completion proposal for a Thymeleaf standard
 	 * attribute processor.
 	 * 
 	 * @param processorprefix
 	 * @param processorname
 	 * @param charsentered	  How much of the entire proposal has already been
 	 * 						  entered by the user.
 	 * @param cursorposition
 	 */
 	public AttributeProcessorCompletionProposal(String processorprefix, String processorname,
 		int charsentered, int cursorposition) {
 
 		this.fullprocessorname = processorprefix + ":" + processorname;
 		this.replacementstring = fullprocessorname.substring(charsentered);
 		this.cursorposition    = cursorposition;
 
 		this.contextinformation     = null;
 		this.additionalproposalinfo = null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void apply(IDocument document) {
 
 		apply(document, '\0', cursorposition);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void apply(IDocument document, char trigger, int offset) {
 
 		try {
			document.replace(offset, 0, replacementstring.substring(offset - cursorposition) + "=\"\"");
 		}
 		catch (BadLocationException ex) {
 			// Do nothing?
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getAdditionalProposalInfo() {
 
 		return additionalproposalinfo;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public IContextInformation getContextInformation() {
 
 		return contextinformation;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getContextInformationPosition() {
 
 		return 0;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getDisplayString() {
 
		return fullprocessorname;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Image getImage() {
 
 		return ThymeleafPlugin.getDefault().getAttributeImage();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Point getSelection(IDocument document) {
 
		return new Point(cursorposition + replacementstring.length() + 2, 0);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public char[] getTriggerCharacters() {
 
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isValidFor(IDocument document, int offset) {
 
 		try {
 			return replacementstring.startsWith(document.get(cursorposition, offset - cursorposition));
 		}
 		catch (BadLocationException ex) {
 			return false;
 		}
 	}
 }
