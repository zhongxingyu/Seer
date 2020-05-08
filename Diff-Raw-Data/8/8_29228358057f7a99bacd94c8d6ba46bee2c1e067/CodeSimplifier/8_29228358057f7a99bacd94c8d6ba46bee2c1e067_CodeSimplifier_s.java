 /*===========================================================================
   Copyright (C) 2010-2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common.resource;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import net.sf.okapi.common.StringUtil;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 
 /**
  * @author Fredrik Liden
  * @author Sergei Vasilyev
  *
  */
 public class CodeSimplifier {
 
 	protected static final int MAX = 10;
 	
 	class CodeNode {
 
 		int offset;
 		int intIndex;
 		char charIndex;
 		Code code;
 		String marker;
 		String markerFlag;
 		boolean adjacentPrev = false;
 		boolean adjacentNext = false;
 		String gapToNext = "";
 		
 		public CodeNode (int offset, int intIndex, char charIndex, Code code) {
 			this.offset = offset;
 			this.intIndex = intIndex;
 			this.charIndex = charIndex;
 			this.code = code;
 		}
 		
 		public void setMergedData(CodeNode node1, CodeNode node2) {
 			String data1 = node1.code.getData();
 			String data2 = node2.code.getData();
 			
 			String odata1 = node1.code.getOuterData();
 			String odata2 = node2.code.getOuterData();
 			
 			String gap = node1.gapToNext;
 			
			this.code.setData(data1 + gap + data2);
			this.code.setOuterData(odata1 + gap + odata2);
 		}
 	}
 	
 	class StartCodeNode extends CodeNode {
 		
 		EndCodeNode endNode;
 		
 		StartCodeNode (int offset, int intIndex, char charIndex, Code code) {
 			super(offset, intIndex, charIndex, code);
 			marker = new String("" + (char)TextFragment.MARKER_OPENING + charIndex);
 			markerFlag = new String("" + (char)TextFragment.MARKER_OPENING);
 		}
 	}
 	
 	class EndCodeNode extends CodeNode {
 
 		StartCodeNode beginNode;
 		
 		EndCodeNode (int offset, int intIndex, char charIndex, Code code) {
 			super(offset, intIndex, charIndex, code);
 			marker = new String("" + (char)TextFragment.MARKER_CLOSING + charIndex);
 			markerFlag = new String("" + (char)TextFragment.MARKER_CLOSING);
 		}
 	}
 	
 	class PhCodeNode extends CodeNode {
 
 		PhCodeNode (int offset, int intIndex, char charIndex, Code code) {
 			super(offset, intIndex, charIndex, code);
 			marker = new String("" + (char)TextFragment.MARKER_ISOLATED + charIndex);
 			markerFlag = new String("" + (char)TextFragment.MARKER_ISOLATED);
 		}
 	}
 	
 	private LinkedList<CodeNode> codeNodesList;
 	private String codedText;
 	
 	/*
 	 * Prepare simplifier for simplifying
 	 */
 	private void prepare (String pCodedText, List<Code> pCodes) {
 
 		codeNodesList = new LinkedList<CodeNode>();
 		codedText = pCodedText;
 		
 		Stack<StartCodeNode> codeNodesStack = new Stack<StartCodeNode>();
 		
 		for (int i = 0; i < codedText.length(); i++){ 
 			
 		    int c = codedText.codePointAt(i);
 		    
 		    if(c == TextFragment.MARKER_OPENING){
 		    	
 		    	StartCodeNode cn = new StartCodeNode(i, TextFragment.toIndex(codedText.charAt(i+1)), codedText.charAt(i+1), pCodes.get(TextFragment.toIndex(codedText.charAt(i+1))));
 
 		    	codeNodesList.add(cn);
 		    	codeNodesStack.push(cn);
 
 		    }else if(c == TextFragment.MARKER_CLOSING){
 		    	
 		    	EndCodeNode cn = new EndCodeNode(i, TextFragment.toIndex(codedText.charAt(i+1)), codedText.charAt(i+1), pCodes.get(TextFragment.toIndex(codedText.charAt(i+1))));
 		    	
 		    	codeNodesList.add(cn);
 		    	codeNodesStack.pop().endNode = cn;
 		    	
 		    }else if(c == TextFragment.MARKER_ISOLATED){
 		    	
 		    	PhCodeNode cn = new PhCodeNode(i, TextFragment.toIndex(codedText.charAt(i+1)), codedText.charAt(i+1), pCodes.get(TextFragment.toIndex(codedText.charAt(i+1))));
 		    	
 		    	codeNodesList.add(cn);
 		    	
 		    }
 		}
 		updateAdjacentFlags(); // codeNodesList.get(2)
 	}
 	
 	/*
 	 * Update the adjacent flags of all the code nodes 
 	 */
 	private void updateAdjacentFlags () {
 		
 		CodeNode peekCn;
 		
 		for(int i=0; i< codeNodesList.size(); i++){
 		
 			CodeNode cn = codeNodesList.get(i);
 			
 			if(i+1 < codeNodesList.size()){
 		
 				peekCn = codeNodesList.get(i+1);
 				
 				if(adjacentMarkers(cn, peekCn)){
 
 					cn.adjacentNext = true;
 					peekCn.adjacentPrev = true;
 
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Simplifies all possible tags in a given text fragment.
 	 * @param tf the text fragment to modify.
 	 * @param maxIterations maxIterations.
 	 * @param removeLeadingTrailingCodes true to remove the leading and/or the trailing code
 	 * of the fragment and place their text in the result.
 	 * This works for isolated codes only for now.
 	 * <b>It is the responsibility of the caller to put the leading/trailing data into the skeleton.</b>
 	 * @return Null (no leading or trailing code removal was) or a string array with the
 	 * original data of the codes removed. The first string if there was a leading code, the second string
 	 * if there was a trailing code. Both or either can be null.
 	 */
 	public String[] simplifyAll (TextFragment tf,
 		int maxIterations,
 		boolean removeLeadingTrailingCodes)
 	{
 		int isolatedMerges=0;
 		int openCloseMerges=0;
 		int emptyOpenCloseMerges=0;
 		int iteration = 0;
 		
 		try {
 			do {
 				iteration++;
 				
 				// TODO check if codes can be combined. Codes can have references to different
 				// resources (parent is different), in this case cannot combine them even if
 				// they both are placeholders
 				
 				//int n1 = tf.getCodedText().length();
 				//System.out.println("--- 1:" + n1);
 				prepare(tf.getCodedText(), tf.getCodes());
 				//System.out.println(TextUnitUtil.toText(tf));
 				isolatedMerges = simplifyIsolated();
 				//System.out.println(TextUnitUtil.toText(getCodedText(), getCodes()));
 				tf.setCodedText(getCodedText(), getCodes());
 	
 				//int n2 = tf.getCodedText().length();
 				//System.out.println("--- 2:" + n2);
 				prepare(tf.getCodedText(), tf.getCodes());
 				//System.out.println("--- 2a:" + getCodedText().length());
 				//System.out.println(TextUnitUtil.toText(tf));
 				openCloseMerges = simplifyOpeningClosing();
 				//System.out.println(TextUnitUtil.toText(getCodedText(), getCodes()));
 				tf.setCodedText(getCodedText(), getCodes());				
 				
 				//int n3 = tf.getCodedText().length();
 				//System.out.println("--- 3:" + n3);
 				prepare(tf.getCodedText(), tf.getCodes());
 				emptyOpenCloseMerges = simplifyEmptyOpeningClosing();
 				tf.setCodedText(getCodedText(), getCodes());
 				
 //				System.out.println("--- 1:" + n1);
 //				System.out.println("--- 2:" + n2);
 //				System.out.println("--- 3:" + n3);
 //				System.out.println("--- 4:" + tf.getCodedText().length());
 			}
 			while ((iteration < maxIterations) && (isolatedMerges + openCloseMerges + emptyOpenCloseMerges) > 0);
 			
 			// Check leading and trailing codes if requested
 			if ( removeLeadingTrailingCodes ) {
 				return removeLeadingTrailingCodes(tf, maxIterations);
 			}
 			else {
 				return null;
 			}
 		}
 		catch ( Throwable e ) {
 			throw new RuntimeException("Error simplifiying codes.\n"+e.getMessage(), e);
 		}
 	}
 	
 	
 	/**
 	 * Simplifies all possible tags in a given possibly segmented text container.
 	 * @param tc the given text container to modify
 	 * @param removeLeadingTrailingCodes true to remove the leading and/or the trailing code
 	 * of the fragment and place their text in the result.
 	 * <b>It is the responsibility of the caller to put the leading/trailing data into the skeleton.</b>
 	 * @return Null (no leading or trailing code removal was) or a string array with the
 	 * original data of the codes removed. The first string if there was a leading code, the second string
 	 * if there was a trailing code. Both or either can be null.
 	 */
 	public String[] simplifyAll (TextContainer tc, boolean removeLeadingTrailingCodes) {
 		// Cannot simplify individual segments as segmentation can change later. 
 		// Store and remove segmentation, simplify the source as the whole, then re-apply initial segmentation,
 		// moving segment boundaries outside codes.
 		
 		TextFragment tf = TextUnitUtil.storeSegmentation(tc);
 		//System.out.println(TextUnitUtil.toText(tf));
 		String[] res = simplifyAll(tf, removeLeadingTrailingCodes);
 		//System.out.println(TextUnitUtil.toText(tf));
 		
 		if (removeLeadingTrailingCodes && res != null) {
 			TextFragment leadingMarkers = new TextFragment();
 			TextFragment trailingMarkers = new TextFragment();
 			
 			res[0] = TextUnitUtil.extractSegMarkers(leadingMarkers, res[0], true);
 			res[1] = TextUnitUtil.extractSegMarkers(trailingMarkers, res[1], true);
 			
 			tf.insert(0, leadingMarkers);
 			tf.append(trailingMarkers);
 		}
 		//System.out.println(TextUnitUtil.toText(tf));
 		TextUnitUtil.restoreSegmentation(tc, tf);
 //		System.out.println(res[0]);
 //		System.out.println(res[1]);
 		
 		return res;		
 	}
 	
 	private String[] removeLeadingTrailingCodes (TextFragment tf, int maxIterations) {
 		String ctext;
 		List<Code> codes;
 		Code code = null;		
 		int startPos;
 		int endPos;
 		StringBuilder leadingSb = new StringBuilder();
 		StringBuilder trailingSb = new StringBuilder();
 		StringBuilder sb;		
 		int iteration = 0;
 		boolean removed;
 		
 		do {
 			// Iterations are needed to catch pairs of opening/closing tags at the edges, only if both are there, then remove the pair
 			iteration++;
 			removed = false;
 			
 			// Remove leading isolated codes and spaces
 			ctext = tf.getCodedText();
 			codes = tf.getCodes();
 			startPos = 0;
 			endPos = 0;
 			sb = new StringBuilder();
 					
 			for (int i = 0; i < ctext.length(); i++){
 				if ( TextFragment.isMarker(ctext.charAt(i)) ) {				
 					int codeIndex = TextFragment.toIndex(ctext.charAt(i + 1));
 					code = codes.get(codeIndex);
 					//if (code.getTagType() != TagType.PLACEHOLDER) break;
 					if (ctext.codePointAt(i) != TextFragment.MARKER_ISOLATED) break;
 					
 					sb.append(code.getOuterData());					
 					endPos = i + 2;					
 					i++; // Skip the pair
 				}
 				// For the 1-st iteration mind spaces only after a code not to trim-head the string
 				else if (Character.isWhitespace(ctext.charAt(i)) && (endPos > 0 || iteration > 1)) {
 						sb.append(ctext.charAt(i));
 						endPos = i + 1;
 				}
 				else {
 					break; // If came across a non-space and non-code, fall off
 				}			
 			}
 			
 			if (startPos < endPos) {
 				tf.remove(startPos, endPos);
 				leadingSb.append(sb);
 				removed = true;
 			}		
 			
 			// Remove trailing isolated codes and spaces
 			ctext = tf.getCodedText();
 			codes = tf.getCodes();
 			startPos = ctext.length();
 			endPos = startPos;
 			sb = new StringBuilder();
 			
 			for (int i = ctext.length() - 1; i > 0; i--){
 				if ( TextFragment.isMarker(ctext.charAt(i - 1)) ) {				
 					int codeIndex = TextFragment.toIndex(ctext.charAt(i));
 					code = codes.get(codeIndex);
 					//if (code.getTagType() != TagType.PLACEHOLDER) break;
 					if (ctext.codePointAt(i - 1) != TextFragment.MARKER_ISOLATED) break;
 					
 					sb.insert(0, code.getOuterData());
 									
 					i--; // Skip the pair
 					startPos = i;
 				}
 				// For the 1-st iteration mind spaces only before a code not to trim-tail the string
 				else if (Character.isWhitespace(ctext.charAt(i)) && (startPos < ctext.length() - 1 || iteration > 1)) {
 						sb.insert(0, ctext.charAt(i));
 						startPos = i;
 				}
 				else {
 					break; // If came across a non-space and non-code, and the previous char is not a code marker, fall off
 				}
 			}
 			
 			if (startPos < endPos) {
 				tf.remove(startPos, endPos);
 				trailingSb.insert(0, sb);
 				removed = true;
 			}
 			
 			Code leadingCode = null;
 			Code trailingCode = null;
 			
 			ctext = tf.getCodedText();
 			codes = tf.getCodes();
 			
 			if ( ctext.length() > 1 && !Util.isEmpty(codes) ) {
 				// Check leading code
 				ctext = tf.getCodedText();
 				codes = tf.getCodes();
 				if ( TextFragment.isMarker(ctext.charAt(0)) ) {				
 					int codeIndex = TextFragment.toIndex(ctext.charAt(1));
 					code = codes.get(codeIndex);
 					if (ctext.codePointAt(0) == TextFragment.MARKER_OPENING) {
 						leadingCode = code;
 					}
 				}
 				
 				// Check trailing code
 				ctext = tf.getCodedText();
 				codes = tf.getCodes();
 				if ( TextFragment.isMarker(ctext.charAt(ctext.length() - 2)) ) {				
 					int codeIndex = TextFragment.toIndex(ctext.charAt(ctext.length() - 1));
 					code = codes.get(codeIndex);
 					if (ctext.codePointAt(ctext.length() - 2) == TextFragment.MARKER_CLOSING) {
 						trailingCode = code;
 					}
 				}
 				
 				if (leadingCode != null && trailingCode != null && leadingCode.getId() == trailingCode.getId()) {
 					tf.remove(0, 2);
 					leadingSb.append(leadingCode.getOuterData());
 				
 					ctext = tf.getCodedText();
 					codes = tf.getCodes();
 					
 					tf.remove(ctext.length() - 2, ctext.length());
 					trailingSb.insert(0, trailingCode.getOuterData());
 					
 					removed = true;
 				}
 			}			
 			
 			if (!removed) break; // 1 exceeding iteration at maximum
 		} while (iteration < maxIterations);
 						
 		String res0 = leadingSb.toString();
 		String res1 = trailingSb.toString();
 		
 		return new String[] {
 				Util.isEmpty(res0)? null : res0, 
 				Util.isEmpty(res1)? null : res1};
 	}
 		
 	/**
 	 * Simplifies all possible tags in a given text fragment.
 	 * @param tf the text fragment to modify.
 	 * @param removeLeadingTrailingCodes true to remove the leading and/or the trailing code
 	 * of the fragment and place their text in the result.
 	 * <b>It is the responsibility of the caller to put the leading/trailing data into the skeleton.</b>
 	 * @return Null (no leading or trailing code removal was) or a string array with the
 	 * original data of the codes removed. The first string if there was a leading code, the second string
 	 * if there was a trailing code. Both or either can be null.
 	 */
 	public String[] simplifyAll (TextFragment tf,
 		boolean removeLeadingTrailingCodes)
 	{
 		return simplifyAll(tf, MAX, removeLeadingTrailingCodes);
 	}
 
 	/**
 	 * Simplifies the place-holders in a given text fragment.
 	 * @param tf the text fragment to modify.
 	 */
 	public void simplifyIsolated (TextFragment tf) {
 		prepare(tf.getCodedText(), tf.getCodes());
 		simplifyIsolated();
 		tf.setCodedText(getCodedText(), getCodes());
 	}
 	
 	private int simplifyIsolated () {
 		int merges = 0;
 		CodeNode peekCn;
 		
 		for(int i=0; i< codeNodesList.size(); i++){
 		
 			CodeNode cn = codeNodesList.get(i);
 			
 			if(i+1 < codeNodesList.size()){
 		
 				peekCn = codeNodesList.get(i+1);
 				
 				if(cn.adjacentNext && peekCn.adjacentPrev){
 					
 					if(cn.code.getTagType() == TagType.PLACEHOLDER || peekCn.code.getTagType() == TagType.PLACEHOLDER ){
 						
 						//TODO: Possibly update it to direct where the PH should be added, open or closing. 
 						//      Possibly do two runs one forward and one backwards.
 
 						mergeNodes(cn,peekCn);
 						merges++;
 						i--;
 						continue;
 					}
 				}
 			}
 		}
 		renumberMarkerIndexes();
 		updateCodeIds();
 		
 		return merges++;
 	}
 		
 	/*
 	 * Simplifies the isolated tags
 	 */
 	public void simplifyOpeningClosing (TextFragment tf) {
 		prepare(tf.getCodedText(), tf.getCodes());
 		simplifyOpeningClosing();
 		tf.setCodedText(getCodedText(), getCodes());
 	}
 	
 	/*
 	 * Merges the Start tags
 	 */
 	private int simplifyOpeningClosing () {
 		
 		int merges = 0;
 		CodeNode peekCn;
 		
 		for(int i=0; i< codeNodesList.size(); i++){
 		
 			CodeNode cn = codeNodesList.get(i);
 			
 			if(i+1 < codeNodesList.size()){
 		
 				peekCn = codeNodesList.get(i+1);
 				
 				if(cn.adjacentNext && peekCn.adjacentPrev){
 					
 					if(cn.code.getTagType() == TagType.OPENING && 
 							peekCn.code.getTagType() == TagType.OPENING &&
 							cn instanceof StartCodeNode &&
 							peekCn instanceof StartCodeNode){
 						
 						StartCodeNode scn1 = (StartCodeNode)cn;
 						StartCodeNode scn2 = (StartCodeNode)peekCn;
 
 						EndCodeNode ecn2 = scn2.endNode;
 						EndCodeNode ecn1 = scn1.endNode;
 
 						if (adjacentMarkers(ecn2, ecn1)){
 
 							mergeEndNodes(ecn2, ecn1);
 							mergeNodes(scn1, scn2);
 							
 							merges++;
 							i--;
 							continue;
 						}
 					}
 				}
 			}
 		}
 		
 		renumberMarkerIndexes();
 		updateCodeIds();
 		
 		return merges;
 	}
 	
 	/*
 	 * Simplify the isolated tags
 	 */
 	public void simplifyEmptyOpeningClosing (TextFragment tf) {
 		prepare(tf.getCodedText(), tf.getCodes());
 		simplifyEmptyOpeningClosing();
 		tf.setCodedText(getCodedText(), getCodes());
 	}
 	
 	/*
 	 * Merges the Start tags
 	 */
 	private int simplifyEmptyOpeningClosing(){
 		
 		int merges = 0;
 		CodeNode peekCn;
 		
 		for(int i=0; i< codeNodesList.size(); i++){
 		
 			CodeNode cn = codeNodesList.get(i);
 			
 			if(i+1 < codeNodesList.size()){
 		
 				peekCn = codeNodesList.get(i+1);
 				
 				if(cn.adjacentNext && peekCn.adjacentPrev){
 					
 					if(cn.code.getTagType() == TagType.OPENING && 
 							peekCn.code.getTagType() == TagType.CLOSING && 
 							cn.intIndex == (peekCn.intIndex-1) &&
 							cn instanceof StartCodeNode &&
 							peekCn instanceof EndCodeNode){
 						
 						StartCodeNode scn = (StartCodeNode)cn;
 						EndCodeNode ecn = (EndCodeNode)peekCn;
 
 						mergeEmptyNodes(scn, ecn);
 
 						merges++;
 						i--;
 						continue;
 					}
 				}
 			}
 		}
 		renumberMarkerIndexes();
 		updateCodeIds();
 		
 		return merges++;
 	}
 	
 	/*
 	 * Renumber marker indexes
 	 */
 	private void renumberMarkerIndexes () {
 		
 		for (int i=0; i< codeNodesList.size(); i++) {
 			CodeNode cn = codeNodesList.get(i);
 			char newCharIndex = TextFragment.toChar(i);
 			String newMarker = new String(cn.markerFlag+newCharIndex);
 			codedTextReplace(cn.marker, newMarker);
 			cn.intIndex = i;
 			cn.charIndex = newCharIndex;
 			cn.marker = newMarker;
 		}
 	}
 		
 	/*
 	 * Generates the list of codes from the code node list
 	 */
 	private List<Code> getCodes () {
 		List<Code> codes = new ArrayList<Code>(); 
 		for(CodeNode cn : codeNodesList){
 			codes.add(cn.code);
 		}
 		return codes;
 	}
 	
 	/*
 	 * Return codedText
 	 */
 	private String getCodedText () {
 		return codedText;
 	}
 
 	/*
 	 * Update the code ids  
 	 */
 	private void updateCodeIds () {
 		
 		for(int i=0; i< codeNodesList.size(); i++){
 		
 			CodeNode cn = codeNodesList.get(i);
 
 			if(cn.code.getTagType() == TagType.OPENING){
 				
 				if (cn instanceof StartCodeNode) {
 					StartCodeNode scn = (StartCodeNode)cn;
 					scn.code.setId(i+1);
 					scn.endNode.code.setId(i+1);
 				}
 				else {
 					cn.code.setId(i+1);
 				}
 				
 			}else if(cn.code.getTagType() == TagType.PLACEHOLDER){
 
 				cn.code.setId(i+1);
 			}
 		}
 	}
 	
 	/*
 	 * Check if markers of two codes are adjacent
 	 */
 	private boolean adjacentMarkers (CodeNode node1, CodeNode node2) {
 		if (( node1.offset+2 ) == node2.offset ) {
 			return true;
 		}
 		else {
 			String gap = codedText.substring(node1.offset+2, node2.offset);
 			boolean gapIsWhitespace = StringUtil.isWhitespace(gap);
 			
 			if (gapIsWhitespace) 
 				node1.gapToNext = gap;
 			
 			return gapIsWhitespace;
 		}
 	}
 	
 	/*
 	 * merges codedText and codes for start and isolated nodes
 	 */
 	private void mergeNodes (CodeNode node1, CodeNode node2) {
 		String gap = node1.gapToNext;
 		String cst = node1.marker + gap + node2.marker; 
 						
 		// PH before Start/End merges to the Start/End		
 		if (node1 instanceof PhCodeNode && 
 				(node2 instanceof StartCodeNode || node2 instanceof EndCodeNode)) {
 			codedTextReplace(cst, node2.marker);
 //			node2.code.setData(node1.code.getData() + gap + node2.code.getData());
 //			node2.code.setOuterData(node1.code.getOuterData() + gap + node2.code.getOuterData());
 			node2.setMergedData(node1, node2);
 			codeNodesList.remove(node1);
 		}
 		// PH after Start/End merges to the Start/End
 		else if (node2 instanceof PhCodeNode && 
 				(node1 instanceof StartCodeNode || node1 instanceof EndCodeNode)) {
 			codedTextReplace(cst, node1.marker);
 //			node1.code.setData(node1.code.getData() + gap + node2.code.getData());
 //			node1.code.setOuterData(node1.code.getOuterData() + gap + node2.code.getOuterData());
 			node1.setMergedData(node1, node2);
 			node1.gapToNext = node2.gapToNext;
 			codeNodesList.remove(node2);
 		}		
 		// PH + PH
 		else {
 			codedTextReplace(cst, node1.marker);
 //			node1.code.setData(node1.code.getData() + gap + node2.code.getData());
 //			node1.code.setOuterData(node1.code.getOuterData() + gap + node2.code.getOuterData());
 			node1.setMergedData(node1, node2);
 			node1.gapToNext = node2.gapToNext;
 			codeNodesList.remove(node2);
 		}
 	}
 	
 	private void updateOffsets(int start, int delta) {
 		for (CodeNode node : codeNodesList) {
 			if (node.offset > start) {
 				//System.out.println(node.offset);
 				node.offset += delta;
 //				System.out.println(node.offset);				
 //				System.out.println();
 			}
 		}
 	}
 	
 	/*
 	 * merges codedText and codes for ending nodes
 	 */
 	private void mergeEndNodes (CodeNode node1, CodeNode node2) {
 		String gap = node1.gapToNext;
 		String cst = node1.marker + gap + node2.marker;
 		
 		codedTextReplace(cst, node2.marker);
 //		node2.code.setData(node1.code.getData() + gap + node2.code.getData());
 //		node2.code.setOuterData(node1.code.getOuterData() + gap + node2.code.getOuterData());
 		node2.setMergedData(node1, node2);
 		node2.offset = node1.offset;
 		codeNodesList.remove(node1);
 		
 //		System.out.println("----- " + cst.length());
 //		updateOffsets(node1.offset, cst.length() - 2);
 	}
 
 	/*
 	 * merges codes for empty start/end tags
 	 */
 	private void mergeEmptyNodes(CodeNode node1, CodeNode node2){
 		String gap = node1.gapToNext;
 		String cst = node1.marker + gap + node2.marker;
 		
 		//codedText = codedTextReplace(node1.marker+node2.marker, "\ue103"+node1.charIndex);
 		codedTextReplace(cst, new String("" + (char)TextFragment.MARKER_ISOLATED + node1.charIndex));
 		
 //		node1.code.setData(node1.code.getData() + gap + node2.code.getData());
 //		node1.code.setOuterData(node1.code.getOuterData() + gap + node2.code.getOuterData());
 		node1.setMergedData(node1, node2);
 		node1.code.setTagType(TagType.PLACEHOLDER);
 
 		PhCodeNode pcn = new PhCodeNode(node1.offset,node1.intIndex, node1.charIndex, node1.code);
 		
 		int i = codeNodesList.indexOf(node2);
 		
 		codeNodesList.add(i, pcn);
 		
 		codeNodesList.remove(node1);
 		codeNodesList.remove(node2);		
 	}
 
 	private void codedTextReplace(String findWhat, String replaceWith) {
 		int startLen = codedText.length();
 		int startIndex = codedText.indexOf(findWhat);
 		codedText = codedText.replace(findWhat, replaceWith);
 		int endLen = codedText.length();
 		int delta = endLen - startLen;
 		
 		if (delta != 0) {
 			updateOffsets(startIndex, delta);
 		}
 	}	
 }
 
 //TextFragment oriFrag;
 //ArrayDeque<Slice> slices;
 //
 //class Slice {
 //	
 //	public Slice (boolean isString) {
 //		this.isString = isString;
 //		if ( isString ) buffer = new StringBuffer();
 //	}
 //	
 //	boolean isString;
 //	StringBuffer buffer;
 //	
 //}
 //
 //public void simplify (TextFragment fragment) {
 //	oriFrag = fragment;
 //
 //	int state = 0;
 //	Stack<String> left = new Stack<String>();
 //	ArrayList<String> right = new ArrayList<String>();
 //	slices = new ArrayDeque<Slice>();
 //	String ctext = oriFrag.getCodedText();
 //	
 //	for ( int i=0; i<ctext.length(); i++ ) {
 //		switch ( ctext.charAt(i) ) {
 //		case TextFragment.MARKER_OPENING:
 //		case TextFragment.MARKER_CLOSING:
 //		case TextFragment.MARKER_ISOLATED:
 //			char m = ctext.charAt(i);
 //			if ( state == 0 ) {
 //				if ( left.isEmpty() ) state = 1; // Left side
 //				else state = 2; // Right side of the text
 //			}
 //			Code code = oriFrag.getCode(ctext.charAt(++i));
 //			String sig = String.format("%c%d", m, code.getId());
 //			if ( state == 1 ) { // Left mode: Stack the signatures
 //				left.push(sig);
 //			}
 //			else { // state = 2, right side
 //				if ( m != TextFragment.MARKER_ISOLATED ) {
 //					if ( !left.isEmpty() && right.isEmpty() ) {
 //						if ((( m == TextFragment.MARKER_CLOSING ) && ( left.peek().charAt(0) == TextFragment.MARKER_OPENING ))
 //							&& ( sig.substring(1).equals(left.peek().substring(1) )))
 //						{ // If we have open/close of the same code: pop it and go to the next
 //							left.pop();
 //							continue;
 //						}
 //						// Else: fall thru to fill right list
 //					}
 //					// Else: fall thru to fill right list
 //				}
 //				// Else: No more right/left matches, start right list
 //				right.add(sig);
 //			}
 //			break;
 //		default: // Text content
 //			if ( state == 2 ) { // End of right
 //				// left stack and right list have the non-matching codes
 //				//TODO: create slices
 //				// Clear the sides for next time
 //				left.clear();
 //				right.clear();
 //			}
 //			if ( state != 0 ) {
 //				state = 0; // Back to text mode
 //				slices.add(new Slice(true));
 //			}
 //			if ( state == 0 ) {
 //				slices.peekLast().buffer.append(ctext.charAt(i));
 //			}
 //		}
 //	}
 //	
 //	// Treat the possible leftover
 //	if ( state == 2 ) {
 //		
 //	}
 //	
 //}
