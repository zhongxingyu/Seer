 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package de.spektrumprojekt.informationextraction.extractors;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.apache.commons.collections.Bag;
 import org.apache.commons.collections.bag.HashBag;
 import org.apache.commons.lang3.StringUtils;
 
 import de.spektrumprojekt.commons.chain.Command;
 import de.spektrumprojekt.datamodel.common.Property;
 import de.spektrumprojekt.datamodel.message.ScoredTerm;
 import de.spektrumprojekt.datamodel.message.Term;
 import de.spektrumprojekt.datamodel.subscription.SubscriptionStatus;
 import de.spektrumprojekt.helper.MessageHelper;
 import de.spektrumprojekt.informationextraction.InformationExtractionContext;
 
 /**
  * <p>
  * A simple tag extractor which can work using a controlled vocabulary with tags to assign.
  * </p>
  * 
  * @author Philipp Katz
  */
 public class KeyphraseExtractorCommand implements Command<InformationExtractionContext> {
 
     /** Allowed characters for a token, everything else will be filtered out. */
     private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z0-9\\.]+");
 
     /** Marker to indicate an undesired token. */
     private static final String IGNORED_TOKEN = "###";
 
     /** The similarity threshold when matching strings, this migh need to be fine-tuned. */
     private static final float SIMILARITY_THRESHOLD = 0.85f;
 
     /** The tag source provides a controlled vocabulary with terms to assign. */
     private final TagSource tagSource;
 
     /**
      * only if the subscription has a Property with this key and value true the Keyphrases will be
      * extracted
      **/
     public static final String ENABLE_PROPERTY_KEY = "tag_extraction_enabled";
 
     /**
      * <p>
      * Create a new {@link KeyphraseExtractorCommand} <i>without</i> controlled vocabulary.
      * </p>
      */
     public KeyphraseExtractorCommand() {
         this(null);
     }
 
     /**
      * <p>
      * Create a new {@link KeyphraseExtractorCommand} with the specified {@link TagSource} providing
      * a controlled vocabulary of tags to assign.
      * </p>
      * 
      * @param tagSource
      *            The TagSource providing tags to assign, <code>null</code> to use no controlled
      *            vocabulary.
      */
     public KeyphraseExtractorCommand(TagSource tagSource) {
         this.tagSource = tagSource;
     }
 
     private KeyphraseCandidates createCandidates(String language, Bag nGramBag) {
         KeyphraseCandidates candidates = new KeyphraseCandidates();
         for (Object nGramObj : nGramBag) {
             String nGram = (String) nGramObj;
             String stemmedNGram = ExtractionUtils.stem(language, nGram);
             candidates.addCandidate(stemmedNGram, nGram, nGramBag.getCount(nGram));
         }
         candidates.removeOverlaps();
         return candidates;
     }
 
     /**
      * <p>
      * Filter tokens which are stop words, or which are not matched by the {@link #TOKEN_PATTERN},
      * or with a length of one and no character.
      * </p>
      * 
      * @param tokens
      * @return
      */
     private List<String> filterTokens(String language, List<String> tokens) {
         List<String> filteredTokens = new ArrayList<String>();
         for (String token : tokens) {
             if (ExtractionUtils.isStopword(language, token)) {
                 filteredTokens.add(IGNORED_TOKEN);
             } else if (!TOKEN_PATTERN.matcher(token).matches()) {
                 filteredTokens.add(IGNORED_TOKEN);
             } else if (token.length() == 1 && !Character.isLetter(token.charAt(0))) {
                 filteredTokens.add(IGNORED_TOKEN);
             } else {
                 filteredTokens.add(token);
             }
         }
         return filteredTokens;
     }
 
     /**
      * <p>
      * Get complete text concatenation of a message (title plus text content).
      * </p>
      * 
      * @param context
      * @return
      */
     private String getCompleteText(InformationExtractionContext context) {
         StringBuilder builder = new StringBuilder();
         String title = MessageHelper.getTitle(context.getMessage());
         if (StringUtils.isNotBlank(title)) {
             builder.append(title).append('\n');
         }
         String text = context.getCleanText();
         if (StringUtils.isNotBlank(text)) {
             builder.append(text);
         }
         return builder.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getConfigurationDescription() {
         return this.getClass().getSimpleName();
     }
 
     /**
      * <p>
      * Check, whether the provided term occurs in the in the controlled vocabulary.
      * </p>
      * 
      * @param term
      *            The term to check.
      * @return The value of the matched term from the vocabulary.
      */
     private String getFromVocabulary(String term) {
         if (tagSource == null) {
             return null;
         }
         String lowercaseTerm = term.toLowerCase();
         for (Term currentTerm : tagSource.getTags()) {
             String lowercaseCandidate = currentTerm.getValue().toLowerCase();
             // XXX most likely, we have to consider the string's lengths here, and maybe have a
             // special treatment for very short tags. But this needs to be determined empirically.
             float sim = ExtractionUtils.getLevenshteinSimilarity(lowercaseCandidate, lowercaseTerm);
             if (sim > SIMILARITY_THRESHOLD) {
                 return currentTerm.getValue();
             }
         }
         return null;
     }
 
     @Override
     public void process(InformationExtractionContext context) {
         String subscriptionId = context.getMessage().getSubscriptionGlobalId();
        SubscriptionStatus subscriptionStatus = context.getPersistence()
                .getAggregationSubscription(subscriptionId);
        if (subscriptionStatus == null || subscriptionStatus.getSubscription() == null) {
            return;
        }
        Property enabledProperty = subscriptionStatus.getSubscription().getAccessParameter(
                 ENABLE_PROPERTY_KEY);
         if (enabledProperty == null || !Boolean.valueOf(enabledProperty.getPropertyValue())) {
             return;
         }
 
         String language = LanguageDetectorCommand.getAnnotatedLanguage(context.getMessage());
 
         String text = getCompleteText(context);
         if (StringUtils.isEmpty(text)) {
             return;
         }
 
         text = text.toLowerCase();
         List<String> tokens = ExtractionUtils.tokenize(text);
         tokens = filterTokens(language, tokens);
 
         List<String> nGrams = new ArrayList<String>();
         nGrams.addAll(tokens);
         nGrams.addAll(ExtractionUtils.createNGrams(tokens, 2));
         nGrams.addAll(ExtractionUtils.createNGrams(tokens, 3));
         nGrams.addAll(ExtractionUtils.createNGrams(tokens, 4));
 
         Bag nGramBag = new HashBag();
         for (String nGram : nGrams) {
             if (nGram.contains(IGNORED_TOKEN) || nGram.length() < 3) {
                 continue;
             }
 
             // check occurrence in controlled vocabulary
             String termFromVocabulary = getFromVocabulary(nGram);
             if (termFromVocabulary != null) {
                 nGramBag.add(termFromVocabulary);
             } else if (tagSource == null) {
                 // if we have no controlled vocabulary, accept them all for now
                 nGramBag.add(nGram);
             }
         }
 
         KeyphraseCandidates candidates = createCandidates(language, nGramBag);
 
         for (KeyphraseCandidate candidate : candidates) {
 
             // TODO how to normalize the score ?
             context.getMessagePart().addScoredTerm(
                     new ScoredTerm(context.getPersistence().getOrCreateTerm(
                             Term.TermCategory.KEYPHRASE, candidate.getShortestUnstemmedValue()),
                             candidate.getCount()));
         }
     }
 }
