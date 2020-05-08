 package com.swoop.example;
 
 import com.swoop.util.random.Randomizer;
import com.swoop.util.random.WeightedChoiceIterator;
 import com.swoop.util.random.WeightedChoiceIteratorBase;
 import java.util.*;
 
 //
 // Generate some silly, random, English, lorem ipsoid text.
 //
 public class SentenceRandomizer
 {
 	private final static int LINE_LIMIT = 80;
 	private final static int SENTENCE_LIMIT = 8;
 
 	// Data.
 	private StringChoice[] sentenceStructures;
 	private Map<String,StringChoice[]> wordChoiceSets;
 
 	// A randomizer capable of making a weighted choice selection.
 	private Randomizer randomizer = new Randomizer();
 
 	// A reusable adapter of a StringChoice array to weighted choice input.
 	private WeightedChoiceIteratorBase<StringChoice> iterator =
 		new WeightedChoiceIteratorBase<StringChoice>() {
 			@Override
 			protected double getWeight(StringChoice choice)
 			{
 				return choice.weight;
 			}
 		};
 
 	// Output buffer.
 	private StringBuilder buf = new StringBuilder();
 
 	public SentenceRandomizer(
 		StringChoice[] sentenceStructures,
 		Map<String,StringChoice[]> wordChoiceSets)
 	{
 		this.sentenceStructures = sentenceStructures;
 		this.wordChoiceSets = wordChoiceSets;
 	}
 
 	public void run()
 	{
 		for (int sCount = 0; sCount < SENTENCE_LIMIT; ++sCount) {
 			String ss = select(sentenceStructures);
 			for (int i = 0; i < ss.length(); ++i) {
 				String wordChoiceKey = ss.substring(i, i + 1);
 				if (wordChoiceSets.containsKey(wordChoiceKey)) {
 					String word = select(wordChoiceSets.get(wordChoiceKey));
 					if (i == 0) {
 						word = Character.toUpperCase(word.charAt(0)) + word.substring(1);
 					}
 					if (i == ss.length() - 1) {
 						word += ".";
 					}
 					if (buf.length() + 1 + word.length() > LINE_LIMIT) {
 						flush();
 					}
 					else if (buf.length() > 0) {
 						buf.append(' ');
 					}
 					buf.append(word);
 				}
 			}
 		}
 		if (buf.length() > 0) {
 			flush();
 		}
 	}
 
 	private String select(StringChoice[] choices)
 	{
 		iterator.reset(choices);
 		return randomizer.select(iterator).downgrade().value;
 	}
 
 	private void flush()
 	{
 		System.out.println(buf);
 		buf.setLength(0);
 	}
 }
