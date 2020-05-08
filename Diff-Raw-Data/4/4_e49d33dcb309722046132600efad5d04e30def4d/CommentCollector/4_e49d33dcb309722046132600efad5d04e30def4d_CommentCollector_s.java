 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.parser.comments;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.vjet.dsf.jst.meta.IJsCommentMeta;
 import org.eclipse.vjet.dsf.jst.meta.IJsCommentMeta.DIRECTION;
 import org.eclipse.vjet.dsf.jstojava.report.ErrorReporter;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.JstSourceUtil;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
 
 public class CommentCollector  {
 
 	private final static char NEWLINE = '\n';
 	LinkedHashMap<Integer, CommentMetaWrapper> m_commentMetaMap = new LinkedHashMap<Integer, CommentMetaWrapper>();
 	LinkedHashMap<Integer, CommentMetaWrapper> m_annotationMetaMap = new LinkedHashMap<Integer, CommentMetaWrapper>();
 	LinkedHashMap<Integer, String> m_commentMap = new LinkedHashMap<Integer, String>();
 	int[][] commentOffsets;
 	private String m_source = null;
 	private char[] m_sourceChars;
 	private int m_lastcommentOffset = 0;
 	private List<InactiveNeedsWrapper> m_inactiveNeeds = new ArrayList<InactiveNeedsWrapper>(8);
 	private int m_unstructuredCommentLastOffset = -1;
 	
 	public static class InactiveNeedsWrapper{
 		private final String m_needsTypeName;
 		private final int m_beginOffset;
 		private final int m_endOffset;
 		
 		public InactiveNeedsWrapper(final String needsTypeName,
 				final int beginOffset,
 				final int endOffset){
 			m_needsTypeName = needsTypeName;
 			m_beginOffset = beginOffset;
 			m_endOffset = endOffset;
 		}
 
 		public String getNeedsTypeName() {
 			return m_needsTypeName;
 		}
 
 		public int getBeginOffset() {
 			return m_beginOffset;
 		}
 
 		public int getEndOffset() {
 			return m_endOffset;
 		}
 	}
 	
 	class CommentMetaWrapper{
 		private IJsCommentMeta m_meta;
 		private boolean m_used;
 		
 		public CommentMetaWrapper(IJsCommentMeta meta) {
 			// TODO Auto-generated constructor stub
 			m_meta = meta;
 		}
 		
 		public void setUsed(boolean b){
 			m_used = b;
 		}
 		
 		public boolean isUsed(){
 			return m_used;
 		}
 	}
 	
 
 	public void handle(CompilationUnitDeclaration ast, ErrorReporter reporter, JstSourceUtil jstSourceUtil) {
 		commentOffsets = ast.comments;
 		if (commentOffsets == null || commentOffsets.length == 0) {
 			return;
 		}
 		m_source = String.valueOf(ast.compilationResult.compilationUnit
 				.getContents());
 		if (m_source == null) {
 			return;
 		}
 		m_sourceChars = m_source.toCharArray();
 		for (int[] ind : commentOffsets) {
 			int beginOffset = Math.abs(ind[0]);
 			int endOffset = Math.abs(ind[1]);
 			String comment = m_source.substring(beginOffset, endOffset);
 			try {
 				JsCommentMeta commentMeta = null;
 				try {
 					if(VjCommentUtil.isVjetComment(comment)){
 						commentMeta = VjComment.parse(comment);
 					}
 				} catch (ParseException e) {
 					reporter.error("VJET comment error: " + e.getMessage() +
 					" For comment :" + comment, new String(ast.getFileName()),
 					beginOffset, endOffset, jstSourceUtil.line(beginOffset),
 					jstSourceUtil.col(beginOffset));
 				}
 				if (commentMeta!=null) {
 					commentMeta.setBeginOffset(beginOffset);
 					commentMeta.setEndOffset(endOffset);
 					if (commentMeta.getDirection()!=null) {
 						int idx = beginOffset+1;
 						if (commentMeta.getDirection().equals(DIRECTION.BACK)) {
 							idx = endOffset-1;
 						}
 						
 						if(commentMeta.isAnnotation()){
 							m_annotationMetaMap.put(idx, new CommentMetaWrapper(commentMeta)); //collecting meta in annotationMeta map
 							
 							if(commentMeta.getInactiveNeeds().size()>0){
 								for(String inactiveNeedTypeName: commentMeta.getInactiveNeeds()){
 									m_inactiveNeeds.add(new InactiveNeedsWrapper(inactiveNeedTypeName, beginOffset, endOffset));
 								}
 							}
 						
 						}else{
 							m_commentMetaMap.put(idx, new CommentMetaWrapper(commentMeta));
 						}
 					}
 				} else {
 					int start = beginOffset-1;
 					String whiteSpace = "";
 					char c;
 					while(start>=0 && (c = m_source.charAt(start))!=NEWLINE) {
 						whiteSpace += c;
 						start--;
 					}
 					String commentWithWhitespace = (whiteSpace.trim().equals("")) ? whiteSpace + comment : comment;
 					m_commentMap.put(beginOffset, commentWithWhitespace);
 				}
 			} catch (Throwable e) {
 				reporter.error("VJET comment error: " + e.getMessage() +
 						" For comment :" + comment, new String(ast.getFileName()),
 						beginOffset, endOffset, jstSourceUtil.line(beginOffset),
 						jstSourceUtil.col(beginOffset));
 			}
 		}
 	}
 
 
 
 	/**
 	 * @param previousEnd int
 	 * @param nextStart int
 	 * @return List<String>
 	 * 
 	 * Searches for JS comments, given a token location, and 
 	 * the start of a new token.
 	 */
 	public List<String> getComments(int previousEnd, int nextStart) {
 		Set<Integer> keySet = m_commentMap.keySet();
 		ArrayList<String> comments = new ArrayList<String>(3);
 		if (nextStart < previousEnd) {
 			return comments;
 		}
 		for (Integer key : keySet) {
 			String current = m_commentMap.get(key);
 			if (previousEnd<key && nextStart>=key) {
 				comments.add(current);
 			}
 		}
 		return comments;
 	}
 	
 	/**
 	 * @return List<String>
 	 * 
 	 * return inactive needs
 	 */
 	public List<InactiveNeedsWrapper> getInactiveNeeds() {
 		return m_inactiveNeeds;
 	}
 
 	/**
 	 * @param methodStart int
 	 * @param previousEnd int
 	 * @param nextStart int
 	 * @return List<IJsCommentMeta>
 	 * 
 	 * Searches for a VJO comment declaration, given a token location, and 
 	 * tokens before and after it. Returns null if it doesn't find one.
 	 */
 	public List<IJsCommentMeta> getCommentMeta(int exprStart, int previousEnd,
 			int nextStart) {
 		return getCommentMeta(exprStart, exprStart, previousEnd, nextStart);
 	}
 	
 	/**
 	 * @param methodStart int
 	 * @param previousEnd int
 	 * @param nextStart int
 	 * @param ignoreUnsed boolean ignore checking for used comments
 	 * @return List<IJsCommentMeta>
 	 * 
 	 * Searches for a VJO comment declaration, given a token location, and 
 	 * tokens before and after it. Returns null if it doesn't find one.
 	 */
 	public List<IJsCommentMeta> getCommentMeta(int exprStart, int previousEnd,
 			int nextStart, boolean ignoreUnsed) {
 		return getCommentMeta(exprStart, exprStart, previousEnd, nextStart, ignoreUnsed);
 	}
 	
 	public Collection<IJsCommentMeta> getCommentAllMeta(){
 		Collection<IJsCommentMeta> m = new ArrayList<IJsCommentMeta>();
 		for(CommentMetaWrapper wrap: m_commentMetaMap.values()){
 			m.add(wrap.m_meta);
 		}
 		
 		return m;
 	}
 	
 	public List<IJsCommentMeta> getCommentMeta(int exprStart, int exprEnd,
 			int previousEnd, int nextStart) {
 		return getCommentMeta(exprStart, exprEnd, previousEnd, nextStart, false);
 	}
 	
 	public List<IJsCommentMeta> getCommentMeta(int exprStart, int exprEnd,
 			int previousEnd, int nextStart, boolean ignoreUnsed) {
 		ArrayList<IJsCommentMeta> comments = new ArrayList<IJsCommentMeta>();
 		
 		if (nextStart < previousEnd ) {
 			return comments;
 		}
 		Set<Integer> keySet = m_commentMetaMap.keySet();
 		for (Integer key : keySet) {
 			CommentMetaWrapper wrapper = m_commentMetaMap.get(key);
 			IJsCommentMeta meta = wrapper.m_meta;
 			if (!isExists(comments, meta)) {
 				if (key > previousEnd && key < exprStart
 						&& meta.getDirection().equals(DIRECTION.FORWARD)) {
 					if(ignoreUnsed) {
 						comments.add(meta);
 					}
 					else if(!wrapper.isUsed()){
 						wrapper.setUsed(true);
 						comments.add(meta);
 					}
 					
 					
 				} else if (key > exprEnd && key < nextStart
 						&& meta.getDirection().equals(DIRECTION.BACK)) {
 					if(ignoreUnsed) {
 						comments.add(meta);
 					}
 					else if(!wrapper.isUsed()){
 						wrapper.setUsed(true);
 						comments.add(meta);
 					}
 					
 				}
 			}
 		}
 	
 		return comments;
 	}
 
 	public List<IJsCommentMeta> getAnnotationMeta(int exprStart, int previousEnd,
 			int nextStart) {
 		return getAnnotationMeta(exprStart, exprStart, previousEnd, nextStart);
 	}
 
 	public List<IJsCommentMeta> getAnnotationMeta(int exprStart, int exprEnd,
 			int previousEnd, int nextStart) {
 		ArrayList<IJsCommentMeta> comments = new ArrayList<IJsCommentMeta>();
 		if (nextStart < previousEnd) {
 			return comments;
 		}
 		Set<Integer> keySet = m_annotationMetaMap.keySet();
 		for (Integer key : keySet) {
 			CommentMetaWrapper wrapper = m_annotationMetaMap.get(key);
 			IJsCommentMeta meta = wrapper.m_meta;
 			if (!isExists(comments, meta)) {
 				if (key > previousEnd && key < exprStart
 						&& meta.getDirection().equals(DIRECTION.FORWARD)) {
 					comments.add(meta);
 				} else if (key > exprEnd && key < nextStart
 						&& meta.getDirection().equals(DIRECTION.BACK)) {
 					comments.add(meta);
 				}
 			}
 		}
 		return comments;
 	}
 	
 	public List<String> getCommentNonMeta(int methodStartOffset, int previousOffset) {
 		
 		int lastCommentOffset = previousOffset;
 		List<String> comments = new ArrayList<String>();
 		while(lastCommentOffset<=methodStartOffset ){
 			String com = m_commentMap.get(lastCommentOffset);
 			if(com!=null){
 				Collections.addAll(comments,com.split("\n"));
 			}
 			lastCommentOffset++;
 		}
 		return comments;
 		
 		
 	}
 	public String getCommentNonMeta2(int methodStartOffset) {
 		
 		for(int i = methodStartOffset; i>m_unstructuredCommentLastOffset ; i--){
 			
 			CommentMetaWrapper wrapper = m_commentMetaMap.get(i);
 			
 			String com = m_commentMap.get(i);
 			if(wrapper==null && com!=null){
 				m_unstructuredCommentLastOffset = methodStartOffset;
 				return com;
 			}
 		}
 		return null;		
 	}	
 
 	public IJsCommentMeta getLocalVariableCommentMeta( int varStart, int varEnd) {
 		if (m_source == null) {
 			return null;
 		}
 		char[] source = m_sourceChars;
 		int startOffset = getStartOffset(source, varStart);
 		int endOffset = getEndOffset(source, varEnd);
 		Set<Integer> keySet = m_commentMetaMap.keySet();
 		for (Integer key : keySet) {
 			CommentMetaWrapper wrapper = m_commentMetaMap.get(key);
 			IJsCommentMeta jsCommentMeta =wrapper.m_meta;
 			if (key > startOffset && key < varStart
 					&& jsCommentMeta.getDirection().equals(DIRECTION.FORWARD)) {
 				return m_commentMetaMap.get(key).m_meta;
 			} else if (key > varEnd && key < endOffset
 					&& jsCommentMeta.getDirection().equals(DIRECTION.BACK)) {
 				return m_commentMetaMap.get(key).m_meta;
 			}
 		}
 
 		return null;
 	}
 	
 	public int[] getLocalVariableCommentPrevNextOffsets( int varStart, int varEnd) {
 		if (m_source == null) {
 			return new int[]{varStart, varEnd};
 		}
 		char[] source = m_sourceChars;
 		int startOffset = getStartOffset(source, varStart);
 		int endOffset = getEndOffset(source, varEnd);
 
 		return new int[]{startOffset, endOffset};
 	}
 	
 	public int getCommentNonMetaBeginOffset(String comment) {
 		Set<Integer> keySet = m_commentMap.keySet();
 		for (Integer key : keySet) {
 			String current = m_commentMap.get(key);
 			if (current.equals(comment)) {
 				return key;
 			}
 		}
 		return -1;
 	}
 
 	private int getStartOffset(char[] originalSource, int startOffSet) {
 		for (int i = startOffSet; i >= 0; i--) {
 			if (originalSource[i] == '{' && !checkIfInCommented(i)) {// TODO														// line
 				return i;
 			} else if (originalSource[i] == 'r'
 					&& i - 3 >= 0
 					&& originalSource[i - 1] == 'a'
 					&& originalSource[i - 2] == 'v'
 					&& (Character.isWhitespace(originalSource[i - 3])
 							|| originalSource[i - 3] == '{' || originalSource[i - 3] == ';')
 					&& !checkIfInCommented(i)) {
 				return i;
 			} else if (originalSource[i] == 'r' && i == 2
 					&& originalSource[i - 1] == 'a'
 					&& originalSource[i - 2] == 'v' && !checkIfInCommented(i)) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	private int getEndOffset(char[] originalSource, int endOffSet) {
 		for (int i = endOffSet; i < originalSource.length; i++) {
 			final char ch = originalSource[i];
 			if (ch == '}' && !checkIfInCommented(i)) {// TODO													// line
 				return i;
 			} else if (ch == '.' && !checkIfInCommented(i)) {//bugfix by huzhou for early stop when meets some field access or method call statement
 				return i;
 			} else if (i + "var ".length() < originalSource.length
 					&& "var ".equals(String.valueOf(Arrays.copyOfRange(originalSource, i, i + "var ".length())))
 					&& !checkIfInCommented(i)) {
 				return i;
 			} else if (i + "function ".length() < originalSource.length
 				&& "function ".equals(String.valueOf(Arrays.copyOfRange(originalSource, i, i + "function ".length())))
 				&& !checkIfInCommented(i)) {
 			return i;
 		}
 		}
 		return -1;
 	}
 
 	private boolean checkIfInCommented(int i) {
 		if (commentOffsets == null) {
 			return false;
 		}
 		for (int[] ind : commentOffsets) {
 			int beginOffset = Math.abs(ind[0]);
 			int endOffset = Math.abs(ind[1]);
 			if (i >= beginOffset && i <= endOffset) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean isEqual(IJsCommentMeta source, IJsCommentMeta target) {
 		return false;
 //		List<JsParam> sourceParams = source.getParams();
 //		List<JsParam> targetParams = target.getParams();
 //
 //		int sourceParamsCount = sourceParams.size();
 //		int targetParamsCount = targetParams.size();
 //
 //		if(sourceParamsCount==targetParamsCount){
 //			for(int i=0;i<sourceParamsCount;i++){
 //				if(!(sourceParams.get(i).getType().equals(targetParams.get(i).getType()))){
 //					return false;
 //				}
 //			}
 //		}else{
 //			return false;
 //		}
 //		if (source.getType()!=null && target.getType()!=null) {
 //			return source.getType().getType().equals(target.getType().getType());
 //		} else  {
 //			return (source.getType()==null && target.getType()==null);
 //		}
 	}
 	
 	private boolean isExists(ArrayList<IJsCommentMeta> list, IJsCommentMeta comment){
 		for(IJsCommentMeta meta : list){
 			if(isEqual(meta,comment)){
 				return true;
 			}
 		}
 		return false;
 	}	
 }
