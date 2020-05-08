 package com.weclay.ksearch2;
 
 import java.lang.Math;
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.lucene.analysis.TokenFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
 import org.snu.ids.ha.ma.CharSetType;
 import org.snu.ids.ha.ma.MorphemeAnalyzer;
 import org.snu.ids.ha.ma.MExpression;
 import org.snu.ids.ha.ma.MCandidate;
 import org.snu.ids.ha.ma.Morpheme;
 import org.snu.ids.ha.ma.Token;
 import org.snu.ids.ha.ma.Tokenizer;
 
 
 public final class KoreanStemFilter extends TokenFilter {
 
 	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
 	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
 	private char[] curTermBuffer;
 	private int curTermLength;
 	private int tokStart;
 	private MExpression mePrev = null;
 	private MCandidate curCandidate = null;
 	private int curCandidateOffsetStart = 0;
 	private int curCandidateTermLength = 0;
 	private int curCandidateIdx = 0;
 	private List<MExpression> meBuffer;
 	private List<Integer> meBufferOffsetStart;
 	private List<Integer> meBufferTermLength;
 	private MorphemeAnalyzer ma;
 
 	/**
	 * Build a filter that analyzes Korean language.
 	 */
 	public KoreanStemFilter(TokenStream in, String dicRoot)
 	{
 		super(in);
 		meBuffer = new ArrayList<MExpression>();
 		meBufferOffsetStart = new ArrayList<Integer>();
 		meBufferTermLength = new ArrayList<Integer>();
 		System.setProperty("dicRoot", dicRoot);
 		ma = new MorphemeAnalyzer();
 		//ma.createLogger("/tmp/KoreanMorphemeAnalyzer.log");
 		//termAtt = addAttribute(TermAttribute.class);
 	}
 
 	/**
	 * Returns the next input Token whose term() is the stem of the word
 	 */
 	public final boolean incrementToken() throws IOException
 	{
 		while (true) {
 			// There is mophemes left in the candidate buffer
 			if (curCandidate != null) {
 				while (curCandidate.size() != curCandidateIdx) {
 					Morpheme m = curCandidate.get(curCandidateIdx);
 					String string = m.getString();
 					// Sometimes the Morpheme Analyzer returns a SPACE " " morpheme
 					// Pass that morpheme
 					if (string.equals(" ")) {
 						curCandidateIdx++;
 						// Candidate buffer is depleted
 						if (curCandidate.size() == curCandidateIdx)
 							break;
 						continue;
 					}
 					Morpheme firstM = curCandidate.get(0);
 					int start = m.getIndex() - firstM.getIndex();
 					int end = Math.min(curCandidateTermLength, start + string.length());
 					//clearAttributes();
 					//TODO(serialx): Do a specific offset calculation.
 					//offsetAtt.setOffset(curCandidateOffsetStart + start, curCandidateOffsetStart + end);
 					// Skip Js and Es
 					char tag = m.getTag().charAt(0);
 					if (tag == 'J' || tag == 'E') {
 						curCandidateIdx++;
 						// Candidate buffer is depleted
 						if (curCandidate.size() == curCandidateIdx)
 							break;
 						continue;
 					}
 					offsetAtt.setOffset(curCandidateOffsetStart, curCandidateOffsetStart + curCandidateTermLength);
 					termAtt.setEmpty().append(string);
 					curCandidateIdx++;
 					// Candidate buffer is depleted
 					if (curCandidate.size() == curCandidateIdx)
 						curCandidate = null;
 					return true;
 				}
 			}
 
 			// If there's MExpression we can use in the buffer
 			// We need to preserve at least two elements
 			// So that the MorphemeAnalyzer uses the previous element
 			// to prune the tree
 			if (meBuffer.size() >= 2) {
 				curCandidate = meBuffer.remove(0).get(0);
 				curCandidateOffsetStart = meBufferOffsetStart.remove(0);
 				curCandidateTermLength = meBufferTermLength.remove(0);
 				curCandidateIdx = 0;
 				continue;
 			}
 
 			if (curTermBuffer == null) {
 				if (!input.incrementToken()) {
 					if (meBuffer.size() == 0) {
 						return false;
 					} else {
 						curCandidate = meBuffer.remove(0).get(0);
 						curCandidateOffsetStart = meBufferOffsetStart.remove(0);
 						curCandidateTermLength= meBufferTermLength.remove(0);
 						curCandidateIdx = 0;
 						continue;
 					}
 				} else {
 					curTermBuffer = termAtt.buffer().clone();
 					curTermLength = termAtt.length();
 					tokStart = offsetAtt.startOffset();
 				}
 			}
 			// Tokenize the string internally
 			String string = new String(curTermBuffer, 0, curTermLength);
 
 			try {
 				List<Token> tokenList = Tokenizer.tokenize(string);
 				// MExpression - MCandidate - Morpheme
 
 				for (Token token: tokenList) {
 					if (token.isCharSetOf(CharSetType.SPACE)) continue;
 					List<MExpression> meList = ma.analyze(mePrev, token);
 					// Prune results with new results
 					for (MExpression meCur: meList) {
 						if( mePrev != null ) mePrev.pruneWithNext(meCur);
 						meBuffer.add(meCur);
 						meBufferOffsetStart.add(tokStart);
 						meBufferTermLength.add(curTermLength);
 						mePrev = meCur;
 					}
 				}
 			}
 			catch (Exception e) {
 			}
 			curTermBuffer = null;
 		}
 	}
 }
