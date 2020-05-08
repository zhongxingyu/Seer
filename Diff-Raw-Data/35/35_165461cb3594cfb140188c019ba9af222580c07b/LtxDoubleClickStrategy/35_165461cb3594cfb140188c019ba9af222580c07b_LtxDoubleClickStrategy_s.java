 /*******************************************************************************
  * Copyright (c) 2009-2012 WalWare/StatET-Project (www.walware.de/goto/statet)
  * and others. All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Stephan Wahlbrink - initial API and implementation
  *******************************************************************************/
 
 package de.walware.docmlet.tex.ui.text;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.ITypedRegion;
 import org.eclipse.jface.text.TextUtilities;
 
 import de.walware.ecommons.text.BasicHeuristicTokenScanner;
 import de.walware.ecommons.text.PairMatcher;
 
 import de.walware.docmlet.tex.core.text.ITexDocumentConstants;
 import de.walware.docmlet.tex.core.text.LtxHeuristicTokenScanner;
 
 
 /**
  * If matching pairs found, selection of content inside matching brackets,
  * otherwise default word selection.
  */
 public class LtxDoubleClickStrategy extends DefaultTextDoubleClickStrategy {
 	
 	
 	private final String fPartitioning;
 	private final PairMatcher fPairMatcher;
 	private final BasicHeuristicTokenScanner fScanner;
 	
 	
 	public LtxDoubleClickStrategy(final LtxHeuristicTokenScanner scanner) {
 		super();
 		fPartitioning = scanner.getPartitioningConfig().getPartitioning();
 		fPairMatcher = new LtxBracketPairMatcher(scanner);
 		fScanner = scanner;
 	}
 	
 	
 	@Override
 	public void doubleClicked(final ITextViewer textViewer) {
 		final int offset = textViewer.getSelectedRange().x;
 		
 		if (offset < 0) {
 			return;
 		}
 		
 		final IDocument document = textViewer.getDocument();
 		try {
 			ITypedRegion partition = TextUtilities.getPartition(document, fPartitioning, offset, true);
 			String type = partition.getType();
 			
 			// Bracket-Pair-Matching in Code-Partitions
			if (ITexDocumentConstants.LTX_DEFAULT_CONTENT_TYPE.equals(type)
					|| ITexDocumentConstants.LTX_DEFAULT_EXPL_CONTENT_TYPE.equals(type)) {
 				final IRegion region = fPairMatcher.match(document, offset);
 				if (region != null && region.getLength() >= 2) {
 					textViewer.setSelectedRange(region.getOffset() + 1, region.getLength() - 2);
 					return;
 				}
 			}
 			
 			// For other partitions, use prefere new partitions (instead opened)
 			partition = TextUtilities.getPartition(document, fPartitioning, offset, false);
 			type = partition.getType();
 			
 			if (type == ITexDocumentConstants.LTX_MATH_CONTENT_TYPE) {
 				final int partitionOffset = partition.getOffset();
 				final int partitionEnd = partitionOffset + partition.getLength();
 				if (partitionEnd - partitionOffset >= 4 && (
 						offset == partitionOffset || offset == partitionOffset+1
 						|| offset == partitionEnd || offset == partitionEnd-1)) {
 					final char c0 = document.getChar(partitionOffset);
 					final char c1 = document.getChar(partitionOffset+1);
 					int start = -1;
 					char[] endPattern = null;
 					if (c0 == '$') {
 						if (c1 == '$') {
 							start = partitionOffset + 2;
 							endPattern = "$$".toCharArray();
 						}
 						else {
 							start = partitionOffset + 1;
 							endPattern = "$".toCharArray();
 						}
 					}
 					else if (c0 == '\\') {
 						if (c1 == '[') {
 							start = partitionOffset + 2;
 							endPattern = "\\]".toCharArray();
 						}
 						else if (c1 == '(') {
 							start = partitionOffset + 2;
 							endPattern = "\\)".toCharArray();
 						}
 					}
 					if (start >= 0) {
 						textViewer.setSelectedRange(start, getEndOffset(document, partitionEnd, endPattern) - start);
 					}
 				}
 				else {
					IRegion region = fPairMatcher.match(document, offset);
					if (region != null && region.getLength() >= 2) {
						textViewer.setSelectedRange(region.getOffset() + 1, region.getLength() - 2);
						return;
					}
					fScanner.configure(document);
					region = fScanner.findCommonWord(offset);
					if (region != null) {
						textViewer.setSelectedRange(region.getOffset(), region.getLength());
					}
					else {
						textViewer.setSelectedRange(offset, 0);
					}
 				}
 				return;
 			}
 			if (type == ITexDocumentConstants.LTX_VERBATIM_CONTENT_TYPE) {
 				final int partitionOffset = partition.getOffset();
 				final int partitionEnd = partitionOffset + partition.getLength();
 				final int start = partitionOffset+6;
 				if (partitionEnd - partitionOffset >= 7 && (
 						offset == start-1 || offset == start
 						|| offset == partitionEnd || offset == partitionEnd-1)) {
 					final String text = document.get(partitionOffset, 7);
 					if (text.startsWith("\\verb")) {
 						textViewer.setSelectedRange(start,
 								getEndOffset(document, partitionEnd, new char[] { text.charAt(5) }) - start);
 					}
 					return;
 				}
 			}
 			// Start in Comment-Partitions
 			if (type == ITexDocumentConstants.LTX_COMMENT_CONTENT_TYPE
 					|| type == ITexDocumentConstants.LTX_MATHCOMMENT_CONTENT_TYPE) {
 				final int partitionOffset = partition.getOffset();
 				if (offset == partitionOffset || offset == partitionOffset+1) {
 					final IRegion lineInfo = document.getLineInformationOfOffset(partitionOffset);
 					final int end = Math.min(partitionOffset + partition.getLength(),
 							lineInfo.getOffset() + lineInfo.getLength() );
 					textViewer.setSelectedRange(partitionOffset, end - partitionOffset);
 					return;
 				}
 			}
 			
 			super.doubleClicked(textViewer);
 			return;
 		}
 		catch (final BadLocationException e) {
 		}
 		catch (final NullPointerException e) {
 		}
 		// else
 		textViewer.setSelectedRange(offset, 0);
 	}
 	
 	private int getEndOffset(final IDocument document, int end, final char[] endPattern) throws BadLocationException {
 		int i = endPattern.length-1;
 		while (--end >= 0 && i >= 0) {
 			if (document.getChar(end) != endPattern[i--]) {
 				break;
 			}
 		}
 		return end+1;
 	}
 	
 }
