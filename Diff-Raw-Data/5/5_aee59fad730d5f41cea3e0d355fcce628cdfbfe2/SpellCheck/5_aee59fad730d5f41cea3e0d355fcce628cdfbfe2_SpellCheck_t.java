 /**
  *
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, NCSA.  All rights reserved.
  *
  * Developed by:
  * The Automated Learning Group
  * University of Illinois at Urbana-Champaign
  * http://www.seasr.org
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal with the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject
  * to the following conditions:
  *
  * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimers.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimers in
  * the documentation and/or other materials provided with the distribution.
  *
  * Neither the names of The Automated Learning Group, University of
  * Illinois at Urbana-Champaign, nor the names of its contributors may
  * be used to endorse or promote products derived from this Software
  * without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
  * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
  *
  */
 
 package org.seasr.meandre.components.transform.text;
 
 import java.io.Reader;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.system.components.ext.StreamDelimiter;
 import org.seasr.datatypes.core.BasicDataTypes.Strings;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.datatypes.core.exceptions.UnsupportedDataTypeException;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.components.transform.text.LevenshteinDistance;
 import org.seasr.meandre.support.generic.io.IOUtils;
 
 import com.swabunga.spell.engine.Configuration;
 import com.swabunga.spell.engine.SpellDictionary;
 import com.swabunga.spell.engine.SpellDictionaryHashMap;
 import com.swabunga.spell.event.SpellCheckEvent;
 import com.swabunga.spell.event.SpellCheckListener;
 import com.swabunga.spell.event.SpellChecker;
 import com.swabunga.spell.event.StringWordTokenizer;
 
 /**
  *
  * @author Boris Capitanu
  *
  */
 
 @Component(
         creator = "Boris Capitanu",
         description = "Performs spell checking on the input and optionally replaces misspelled words " +
             		  "with the top ranked suggestion. The component also produces a list of the misspellings " +
             		  "in the document and a set of transformation rules.",
         name = "Spell Check",
         tags = "dictionary, word, spell check",
         firingPolicy = FiringPolicy.any,
         rights = Licenses.UofINCSA,
         baseURL = "meandre://seasr.org/components/foundry/",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class SpellCheck extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TEXT,
             description = "The text, tokens, or token counts that needs to be spell checked" +
                           "<br>TYPE: java.lang.String" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                           "<br>TYPE: byte[]" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                           "<br>TYPE: java.lang.Object"
     )
     protected static final String IN_TEXT = Names.PORT_TEXT;
 
     @ComponentInput(
             name = "dictionary",
             description = "The word list to be used as dictionary or the location of the word list file" +
                           "<br>TYPE: java.net.URI" +
                           "<br>TYPE: java.net.URL" +
                           "<br>TYPE: java.lang.String" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                           "<br>TYPE: byte[]" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                           "<br>TYPE: java.lang.Object"
     )
     protected static final String IN_DICTIONARY = "dictionary";
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = "replacement_rules",
             description = "The replacement rules for misspelled words in the following format: correctedWord = {badWord1, badWord2, ... }; ..." +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_RULES = "replacement_rules";
 
     @ComponentOutput(
             name = "replacements",
             description = "The replacements suggested for misspelled words" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
     )
     protected static final String OUT_REPLACEMENTS = "replacements";
 
     @ComponentOutput(
             name = Names.PORT_TEXT,
             description = "The original text with corrections applied if the 'do_correction' property was set to 'true'" +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_TEXT = Names.PORT_TEXT;
 
     @ComponentOutput(
             name = "uncorrected_misspellings",
             description = "The list of words that are misspelled and for which a correction/replacement could not be found." +
                           "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_UNCORRECTED_MISSPELLINGS = "uncorrected_misspellings";
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             name = "do_correction",
             description = "True to correct misspelled words, False otherwise",
             defaultValue = "true"
     )
     protected static final String PROP_DO_CORRECTION = "do_correction";
 
     @ComponentProperty(
             name = "enable_levenshtein",
             description = "Use the Levinstein algorithm to filter the list of suggestions considered",
             defaultValue = "true"
     )
     protected static final String PROP_ENABLE_LEVENSHTEIN = "enable_levenshtein";
 
     @ComponentProperty(
             name = "levenshtein_distance",
             description = "The Levenshtein distance is a metric for measuring the amount of difference between two sequences;" +
             		"The value of this property should expressed as a percentage that will depend on the length of the misspelled word. " +
             		"Lower percentages are more restrictive in matching.",
             defaultValue = "0.33"
     )
     protected static final String PROP_LEVENSHTEIN_DISTANCE = "levenshtein_distance";
 
     @ComponentProperty(
             name = "ignore_uppercase",
             description = "Ignore uppercase words? Ex: CIA",
             defaultValue = "true"
     )
     protected static final String PROP_IGNORE_UPPERCASE = "ignore_uppercase";
 
     @ComponentProperty(
             name = "ignore_mixedcase",
             description = "Ignore mixed case words? Ex: SpellCheck",
             defaultValue = "false"
     )
     protected static final String PROP_IGNORE_MIXEDCASE = "ignore_mixedcase";
 
     @ComponentProperty(
             name = "ignore_internetaddresses",
             description = "Ignore internet addresses? Ex: http://www.google.com",
             defaultValue = "true"
     )
     protected static final String PROP_IGNORE_INTERNETADDR = "ignore_internetaddresses";
 
     @ComponentProperty(
             name = "ignore_digitwords",
             description = "Ignore digit words? Ex: mach5",
             defaultValue = "true"
     )
     protected static final String PROP_IGNORE_DIGITWORDS = "ignore_digitwords";
 
 
     //--------------------------------------------------------------------------------------------
 
 
     protected boolean _doCorrection;
     protected SpellChecker _spellChecker;
     protected SpellDictionary _spellDictionary;
     protected boolean _enableLevenshtein;
     protected Float _levenshteinDistance;
     protected boolean _ignoreUppercase;
     protected boolean _ignoreMixedCase;
     protected boolean _ignoreInternetAddr;
     protected boolean _ignoreDigitWords;
 
     // stats
     protected int _countTotalWords;
     protected int _countMisspelledWords;
     protected int _countUncorrectedWords;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _doCorrection = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_DO_CORRECTION, ccp));
         _enableLevenshtein = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_ENABLE_LEVENSHTEIN, ccp));
         _levenshteinDistance = Float.parseFloat(getPropertyOrDieTrying(PROP_LEVENSHTEIN_DISTANCE, ccp));
         if (!_enableLevenshtein) _levenshteinDistance = null;
 
         _ignoreUppercase = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_IGNORE_UPPERCASE, ccp));
         _ignoreMixedCase = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_IGNORE_MIXEDCASE, ccp));
         _ignoreInternetAddr = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_IGNORE_INTERNETADDR, ccp));
         _ignoreDigitWords = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_IGNORE_DIGITWORDS, ccp));
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 
         if (cc.isInputAvailable(IN_DICTIONARY)) {
             Object in_dictionary = cc.getDataComponentFromInput(IN_DICTIONARY);
             if (in_dictionary instanceof StreamDelimiter) {
                 // Forward any stream delimiter received
                 pushStreamDelimiter(in_dictionary);
             } else {
                 _spellDictionary = getDictionary(in_dictionary);
                 _spellChecker = new SpellChecker(_spellDictionary);
                 Configuration configuration = _spellChecker.getConfiguration();
 				configuration.setBoolean(Configuration.SPELL_IGNOREUPPERCASE, _ignoreUppercase);
 				configuration.setBoolean(Configuration.SPELL_IGNOREMIXEDCASE, _ignoreMixedCase);
 				configuration.setBoolean(Configuration.SPELL_IGNOREINTERNETADDRESSES, _ignoreInternetAddr);
 				configuration.setBoolean(Configuration.SPELL_IGNOREDIGITWORDS, _ignoreDigitWords);
             }
         }
 
         componentInputCache.storeIfAvailable(cc, IN_TEXT);
 
         if (isReadyToProcessInputs()) {
             for (int i = 0, iMax = componentInputCache.getDataCount(IN_TEXT); i < iMax; i++) {
                 _countTotalWords = _countMisspelledWords = _countUncorrectedWords = 0;
 
                 Object input = componentInputCache.retrieveNext(IN_TEXT);
 
                 if (input instanceof StreamDelimiter) {
                     // Forward any stream delimiter received
                     pushStreamDelimiter(input);
                     continue;
                 }
 
                 try {
                     // try parsing as token counts
                     Map<String, Integer> tokenCounts = DataTypeParser.parseAsStringIntegerMap(input);
                     processTokenCounts(tokenCounts);
                 }
                 catch (UnsupportedDataTypeException e) {
                     // try parsing as tokenized sentences
                     try {
                         Map<String, String[]> tokenizedSentences = DataTypeParser.parseAsStringStringArrayMap(input);
                         processTokenizedSentences(tokenizedSentences);
                     }
                     catch (UnsupportedDataTypeException e1) {
                         // parse as text
                         String[] text = DataTypeParser.parseAsString(input);
                         processText(text);
                     }
                 }
 
                 // Display stats
                 console.info(String.format("Total number of words (including duplicates): %d", _countTotalWords));
                 console.info(String.format("Number of unique misspelled words: %d", _countMisspelledWords));
                 console.info(String.format("Number of unique misspelled words with no suggested replacement: %d", _countUncorrectedWords));
             }
         }
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         _spellChecker = null;
     }
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void handleStreamInitiators() throws Exception {
         executeCallBack(componentContext);
     }
 
     @Override
     public void handleStreamTerminators() throws Exception {
         executeCallBack(componentContext);
     }
 
     //--------------------------------------------------------------------------------------------
 
     protected void pushStreamDelimiter(Object input) throws ComponentContextException {
         componentContext.pushDataComponentToOutput(OUT_REPLACEMENTS, input);
         componentContext.pushDataComponentToOutput(OUT_RULES, input);
         componentContext.pushDataComponentToOutput(OUT_TEXT, input);
     }
 
     protected boolean isReadyToProcessInputs() throws ComponentContextException {
         return _spellChecker != null && componentInputCache.hasData(IN_TEXT);
     }
 
     protected SpellDictionary getDictionary(Object in_dictionary) throws Exception {
         URI dictUri = DataTypeParser.parseAsURI(in_dictionary);
         Reader dictReader = IOUtils.getReaderForResource(dictUri);
 
         return new SpellDictionaryHashMap(dictReader);
     }
 
     protected SuggestionListener getSuggestionListener() {
         return new SuggestionListener(_doCorrection, _levenshteinDistance, console);
     }
 
     private void processText(String[] text) throws ComponentContextException {
         SuggestionListener listener = getSuggestionListener();
         listener.resetStats();
         _spellChecker.addSpellCheckListener(listener);
 
         for (int i = 0, iMax = text.length; i < iMax; i++) {
             StringWordTokenizer wordTokenizer = new StringWordTokenizer(text[i]);
             _spellChecker.checkSpelling(wordTokenizer);
             StringWordTokenizer wordCounter = new StringWordTokenizer(text[i]);
             while (wordCounter.hasMoreWords()) {
                 _countTotalWords++;
                 wordCounter.nextWord();
             }
             text[i] = wordTokenizer.getContext();
         }
 
         _countMisspelledWords = listener.getCountSpellingErrors();
         _countUncorrectedWords = listener.getCountMissedCorrections();
 
         _spellChecker.removeSpellCheckListener(listener);
 
         String replacementRules = listener.getReplacementRules();
         console.fine("Replacement rules: " + replacementRules);
 
         Map<String,Set<String>> replacements = listener.getReplacements();
 
         componentContext.pushDataComponentToOutput(OUT_UNCORRECTED_MISSPELLINGS, listener.getUncorrectedMisspellings());
         componentContext.pushDataComponentToOutput(OUT_RULES, BasicDataTypesTools.stringToStrings(replacementRules));
         componentContext.pushDataComponentToOutput(OUT_REPLACEMENTS, BasicDataTypesTools.mapToStringMap(getReplacementsMap(replacements)));
         componentContext.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
     }
 
     private void processTokenizedSentences(Map<String, String[]> tokenizedSentences) throws ComponentContextException {
         SuggestionListener listener = getSuggestionListener();
         listener.resetStats();
         _spellChecker.addSpellCheckListener(listener);
 
         Map<String, String[]> correctedTokenizedSentences = new HashMap<String, String[]>(tokenizedSentences.size());
         for (Entry<String, String[]> entry : tokenizedSentences.entrySet()) {
             String sentence = entry.getKey();
             String[] tokens = entry.getValue();
 
             StringWordTokenizer wordTokenizer = new StringWordTokenizer(sentence);
             _spellChecker.checkSpelling(wordTokenizer);
             StringWordTokenizer wordCounter = new StringWordTokenizer(sentence);
             while (wordCounter.hasMoreWords()) {
                 _countTotalWords++;
                 wordCounter.nextWord();
             }
 
             if (_doCorrection) {
                 sentence = wordTokenizer.getContext();
 
                 for (int i = 0, iMax = tokens.length; i < iMax; i++) {
                     wordTokenizer = new StringWordTokenizer(tokens[i]);
                     _spellChecker.checkSpelling(wordTokenizer);
                     tokens[i] = wordTokenizer.getContext();
                 }
 
                 correctedTokenizedSentences.put(sentence, tokens);
             }
         }
 
         if (!_doCorrection)
             correctedTokenizedSentences = tokenizedSentences;
 
         _countMisspelledWords = listener.getCountSpellingErrors();
         _countUncorrectedWords = listener.getCountMissedCorrections();
 
         _spellChecker.removeSpellCheckListener(listener);
 
         String replacementRules = listener.getReplacementRules();
         console.fine("Replacement rules: " + replacementRules);
 
         Map<String,Set<String>> replacements = listener.getReplacements();
 
         componentContext.pushDataComponentToOutput(OUT_UNCORRECTED_MISSPELLINGS, listener.getUncorrectedMisspellings());
         componentContext.pushDataComponentToOutput(OUT_RULES, BasicDataTypesTools.stringToStrings(replacementRules));
         componentContext.pushDataComponentToOutput(OUT_REPLACEMENTS, BasicDataTypesTools.mapToStringMap(getReplacementsMap(replacements)));
         componentContext.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.mapToStringMap(correctedTokenizedSentences));
     }
 
     private void processTokenCounts(Map<String, Integer> tokenCounts) throws ComponentContextException {
         SuggestionListener listener = getSuggestionListener();
         listener.resetStats();
         _spellChecker.addSpellCheckListener(listener);
 
         Map<String, Integer> correctedTokenCounts = new HashMap<String, Integer>(tokenCounts.size());
         for (Entry<String,Integer> entry : tokenCounts.entrySet()) {
             StringWordTokenizer wordTokenizer = new StringWordTokenizer(entry.getKey());
             _spellChecker.checkSpelling(wordTokenizer);
             StringWordTokenizer wordCounter = new StringWordTokenizer(entry.getKey());
             while (wordCounter.hasMoreWords()) {
                 _countTotalWords++;
                 wordCounter.nextWord();
             }
 
             if (_doCorrection) {
                 String token = wordTokenizer.getContext();
                 Integer oldCount = correctedTokenCounts.containsKey(token) ? correctedTokenCounts.get(token) : 0;
                 correctedTokenCounts.put(token,  oldCount + entry.getValue());
             }
         }
 
         if (!_doCorrection)
             correctedTokenCounts = tokenCounts;
 
         _countMisspelledWords = listener.getCountSpellingErrors();
         _countUncorrectedWords = listener.getCountMissedCorrections();
 
         _spellChecker.removeSpellCheckListener(listener);
 
         String replacementRules = listener.getReplacementRules();
         console.fine("Replacement rules: " + replacementRules);
 
         Map<String,Set<String>> replacements = listener.getReplacements();
 
         componentContext.pushDataComponentToOutput(OUT_UNCORRECTED_MISSPELLINGS, listener.getUncorrectedMisspellings());
         componentContext.pushDataComponentToOutput(OUT_RULES, BasicDataTypesTools.stringToStrings(replacementRules));
         componentContext.pushDataComponentToOutput(OUT_REPLACEMENTS, BasicDataTypesTools.mapToStringMap(getReplacementsMap(replacements)));
         componentContext.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.mapToIntegerMap(correctedTokenCounts, false));
     }
 
     private Map<String,String[]> getReplacementsMap(Map<String,Set<String>> map) {
         Map<String,String[]> result = new HashMap<String, String[]>(map.size());
         for (Entry<String,Set<String>> entry : map.entrySet()) {
             String[] arr = new String[entry.getValue().size()];
             result.put(entry.getKey(), entry.getValue().toArray(arr));
         }
 
         return result;
     }
 
     //--------------------------------------------------------------------------------------------
 
     public static class SuggestionListener implements SpellCheckListener {
 
         protected final Map<String, Set<String>> _replacements;
         protected final boolean _doCorrection;
         protected final Float _levenshteinDistance;
         protected final Logger _logger;
         protected final Strings.Builder _uncorrectedMisspellings;
 
         protected int _countSpellingErrors = 0;
         protected int _countMissedCorrections = 0;
 
         public SuggestionListener(boolean doCorrection) {
             this(doCorrection, null);
         }
 
         public SuggestionListener(boolean doCorrection, Logger logger) {
         	this(doCorrection, null, logger);
         }
 
         public SuggestionListener(boolean doCorrection, Float levenshteinDistance, Logger logger) {
             _replacements = new HashMap<String, Set<String>>();
             _doCorrection = doCorrection;
             _levenshteinDistance = levenshteinDistance;
             _uncorrectedMisspellings = Strings.newBuilder();
             _logger = logger;
         }
 
         public void spellingError(SpellCheckEvent event) {
             _countSpellingErrors++;
 
             String invalidWord = event.getInvalidWord();
 			if (_logger != null) _logger.finer("Misspelling: " + invalidWord);
             int nSuggestions = event.getSuggestions().size();
             Set<String> suggestions = nSuggestions > 0 ? new LinkedHashSet<String>(nSuggestions) : new LinkedHashSet<String>();
             for (Object suggestion : event.getSuggestions())
             	suggestions.add(suggestion.toString());
 
             suggestions = getFilteredSuggestions(invalidWord, suggestions);
 
             if (_logger != null && (_logger.getLevel() == Level.FINEST || _logger.getLevel() == Level.ALL)) {
                 StringBuilder sb = new StringBuilder();
                 for (String suggestion : suggestions)
                     sb.append(", ").append(suggestion);
                 if (sb.length() > 0)
                     _logger.finest("Before Levenshtein Suggestions: " + sb.substring(2));
             }
 
             if (_levenshteinDistance != null) {
             	Iterator<String> it = suggestions.iterator();
             	while (it.hasNext()) {
             		String suggestion = it.next();
             		int score = LevenshteinDistance.computeLevenshteinDistance(invalidWord, suggestion);
             		if (score > _levenshteinDistance * invalidWord.length())
             			it.remove();
             	}
             }
 
             if (_logger != null && (_logger.getLevel() == Level.FINER || _logger.getLevel() == Level.ALL)) {
                 StringBuilder sb = new StringBuilder();
                 for (String suggestion : suggestions)
                     sb.append(", ").append(suggestion);
                 if (sb.length() > 0)
                     _logger.finest("Suggestions: " + sb.substring(2));
             }
 
             String topRankedSuggestion = getReplacement(invalidWord, suggestions);
 
             if (topRankedSuggestion != null) {
                 if (_logger != null) _logger.finer("Top suggestion: " + topRankedSuggestion);
 
                 Set<String> misspellings = _replacements.get(topRankedSuggestion);
                 if (misspellings == null) {
                     misspellings = new HashSet<String>();
                     _replacements.put(topRankedSuggestion, misspellings);
                 }
                 misspellings.add(invalidWord);
 
                 if (_doCorrection)
                     event.replaceWord(topRankedSuggestion, true);
                 else
                     event.ignoreWord(true);
             } else {
                 _uncorrectedMisspellings.addValue(invalidWord);
                 _logger.finer("Suggestions: <none>");
                 _countMissedCorrections++;
             }
         }
 
         protected Set<String> getFilteredSuggestions(String invalidWord, Set<String> suggestions) {
         	return suggestions;
         }
 
         protected Strings getUncorrectedMisspellings() {
             return _uncorrectedMisspellings.build();
         }
 
         protected String getReplacement(String invalidWord, Set<String> suggestions) {
             return suggestions.isEmpty() ? null : suggestions.iterator().next();
         }
 
         public String getReplacementRules() {
             StringBuilder sb = new StringBuilder();
             for (Entry<String, Set<String>> entry : _replacements.entrySet()) {
                 StringBuilder sbWord = new StringBuilder();
                 for (String word : entry.getValue())
                     sbWord.append(", ").append(word);
                String rule = String.format("%s = {%s};%n", entry.getKey(), sbWord.substring(2));
                 sb.append(rule);
             }
 
            return sb.toString();
         }
 
         public Map<String, Set<String>> getReplacements() {
             return _replacements;
         }
 
         public void resetStats() {
             _countSpellingErrors = _countMissedCorrections = 0;
         }
 
         public int getCountSpellingErrors() {
             return _countSpellingErrors;
         }
 
         public int getCountMissedCorrections() {
             return _countMissedCorrections;
         }
     }
 }
