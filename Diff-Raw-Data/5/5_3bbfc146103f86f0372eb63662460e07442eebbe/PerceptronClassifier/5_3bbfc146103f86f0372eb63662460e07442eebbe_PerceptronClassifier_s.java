package org.apache.lucene.classification;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.index.AtomicReader;
 import org.apache.lucene.index.MultiFields;
 import org.apache.lucene.index.Terms;
 import org.apache.lucene.index.TermsEnum;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.MatchAllDocsQuery;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.util.BytesRef;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * A perceptron (see <code>http://en.wikipedia.org/wiki/Perceptron</code>) based {@link Classifier}
  */
 public class PerceptronClassifier implements Classifier<BytesRef> {
 
   private Map<String, Double> weights = new HashMap<String, Double>();
   private Terms textTerms;
   private Analyzer analyzer;
   private String textFieldName;
 
   @Override
   public ClassificationResult<BytesRef> assignClass(String text) throws IOException {
     if (textTerms == null) {
       throw new IOException("You must first call Classifier#train()");
     }
     Double output = 0d;
     TokenStream tokenStream = analyzer.tokenStream(textFieldName, new StringReader(text));
     while (tokenStream.incrementToken()) {
       CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
       Double d = weights.get(String.valueOf(charTermAttribute.buffer()));
       if (d != null && d > 0) {
         output += d; // TODO : decide if there should be some other multiplier (e.g. local/global term/doc freq)
       }
     }
     // TODO : decide how to map perceptron output to class field values
     return new ClassificationResult<BytesRef>(new BytesRef(), output);
   }
 
   @Override
   public void train(AtomicReader atomicReader, String textFieldName, String classFieldName, Analyzer analyzer) throws IOException {
     textTerms = MultiFields.getTerms(atomicReader, textFieldName);
     this.analyzer = analyzer;
     this.textFieldName = textFieldName;
 
     IndexSearcher indexSearcher = new IndexSearcher(atomicReader);
     // for each doc
     TermsEnum reuse = null;
     for (ScoreDoc scoreDoc : indexSearcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE).scoreDocs) {
       TermsEnum cte = textTerms.iterator(reuse);
       // get the term vectors
       Terms terms = atomicReader.getTermVector(scoreDoc.doc, textFieldName);
 
       TermsEnum termsEnum = terms.iterator(null);
 
       BytesRef term;
       while ((term = termsEnum.next()) != null) {
         cte.seekExact(term, true);
         ClassificationResult<BytesRef> classificationResult = assignClass(indexSearcher.doc(scoreDoc.doc).getField(textFieldName).stringValue());
         BytesRef assignedClass = classificationResult.getAssignedClass();
         if (assignedClass != null) {
           double sign = calculateModifier(assignedClass, indexSearcher.doc(scoreDoc.doc).getField(classFieldName).binaryValue());
           String termString = cte.term().utf8ToString();
           long termFreqLocal = termsEnum.totalTermFreq();
 //          int docFreqOverall = cte.docFreq();
 //          long termFreqOverall = cte.totalTermFreq();
 //          System.err.println(termString + " : " + docFreqOverall + " - " + termFreqOverall + " - " + termFreqLocal);
           weights.put(termString, weights.get(termString) + sign * termFreqLocal);
         }
       }
       reuse = cte;
     }
   }
 
   private double calculateModifier(BytesRef assignedClass, BytesRef correctOutput) throws IOException {
     double modifier = 0;
     if (!assignedClass.equals(correctOutput)) {
       // TODO : this has to be done in a different way to see if weights should be added or subtracted
 
     }
     return modifier;
   }
 }
