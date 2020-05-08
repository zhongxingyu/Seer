 package com.infoc.service;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.infoc.domain.Article;
 import com.infoc.domain.SentenceInfo;
 
 public class ContentsAnalysisService {
 	private static final String TITLE_SPLIT_PATTERN = "\\s|\\,|\\[|\\]|\\;|\\'|\\·|\\…|\\!|\\\"|\\“|\\”|\\.\\.";
 	public static Splitter TITLE_SPLITTER = Splitter.onPattern(TITLE_SPLIT_PATTERN).trimResults().omitEmptyStrings();
 
 	public static void createMainSentence(Article article) {
 		if (!Strings.isNullOrEmpty(article.getMainContents())) {
 			return;
 		}
 		
 		// create key words first
 		Set<String> keyWordList = createKeyWorkList(article.getTitle());
 		article.setKeyWordList(keyWordList);
 
 		// create key sentences
 		List<SentenceInfo> sentenceList = createSentenceList(keyWordList, article.getContents());
 		List<SentenceInfo> keySentenceList = createKeySentenceList(sentenceList);
 
 		StringBuilder sb = new StringBuilder();
 		for (SentenceInfo sentence : keySentenceList) {
 			sb.append(sentence.getSentance()).append(". ");
 		}
 
 		article.setMainContents(sb.toString());
 		article.setContents("");
 	}
 
 	private static Set<String> createKeyWorkList(String title) {
 		Set<String> keyWordList = new HashSet<>();
 		if (Strings.isNullOrEmpty(title)) {
 			return keyWordList;
 		}
 
 		// eliminate special characters from title and split it
 		Set<String> titleList = Sets.newHashSet(
 			TITLE_SPLITTER
 				.omitEmptyStrings()
 				.trimResults()
 				.split(
 					title.replaceAll("[^\\p{L}\\p{Z}]", " ")
 				)
 			);
 
 		for (String word : titleList) {
 			if (word.length() > 1 && !isSpecialChar(word)) {
 				keyWordList.add(word);
 			}
 		}
 
 		return keyWordList;
 	}
 
 	/**
 	 * TODO: 테스트 필요
 	 */
 	private static boolean isSpecialChar(String str) {
 		char c;
 		int cint;
 		for (int n = 0; n < str.length(); n++) {
 			c = str.charAt(n);
 			cint = (int)c;
 			if (cint < 48 || (cint > 57 && cint < 65)
 				|| (cint > 90 && cint < 97) || cint > 122) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	private static List<SentenceInfo> createSentenceList(Set<String> keyWordList, String contents) {
 		List<SentenceInfo> sentenceList = new ArrayList<>();
 
 		List<String> sList = Lists.newArrayList(
 			Splitter.onPattern("\\.\\s")
 				.trimResults()
 				.omitEmptyStrings()
 				.split(contents)
 			);
 
 		for (int i = 0; i < sList.size(); i++) {
 			String sentence = sList.get(i);
 
 			SentenceInfo scInfo = new SentenceInfo();
 			scInfo.setIndex(i);
 			scInfo.setLength(sentence.length());
 			scInfo.setSentance(sentence);
 			scInfo.checkKeyword(keyWordList);
 
 			sentenceList.add(scInfo);
 		}
 
 		return sentenceList;
 	}
 
 	private static List<SentenceInfo> createKeySentenceList(List<SentenceInfo> sentenceList) {
 		List<SentenceInfo> keySentenceList = new ArrayList<>();
 
 		List<SentenceInfo> matchedOrderList = Article.matchedOrder.nullsLast().reverse().sortedCopy(sentenceList);
 		int maxKeySentence = matchedOrderList.size() <= 3 ? matchedOrderList.size() : 3;
 
 		for (int i = 0; i < maxKeySentence; i++) {
 			keySentenceList.add(matchedOrderList.get(i));
 		}
 
 		Collections.sort(keySentenceList, Article.indexOrder.nullsFirst());
 
 		return keySentenceList;
 	}
 
 	/**
 	 * ". "를 기준으로 문장을 자르는데 "다.XX"인 경우가 있어 미리 변경
 	 * 기타 잡스런 단어 제거
 	 */
 	public static String clearInvalidWords(String some) {
 		return some.replaceAll("&nbsp;", "")
 			.replaceAll("다\\.", "다\\. ")
 			.replaceAll("(?i)\\【.*?\\】", "")
 			.replaceAll("(?i)\\[.*?\\]", "")
 			.replaceAll("(?i)\\<.*?\\>", "")
 			.replaceAll("(?i)\\(.*?\\)", "");
 	}
 
 }
