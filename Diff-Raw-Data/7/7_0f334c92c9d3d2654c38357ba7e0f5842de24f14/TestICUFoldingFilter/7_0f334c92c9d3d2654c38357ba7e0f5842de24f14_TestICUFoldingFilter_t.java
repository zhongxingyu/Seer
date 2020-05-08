 package org.apache.lucene.analysis.icu;
 
 /**
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
 
 import java.io.IOException;
 import java.io.Reader;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.BaseTokenStreamTestCase;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.core.WhitespaceTokenizer;
 
 /**
  * Tests ICUFoldingFilter
  */
 public class TestICUFoldingFilter extends BaseTokenStreamTestCase {
   public void testDefaults() throws IOException {
     Analyzer a = new Analyzer() {
       @Override
       public TokenStream tokenStream(String fieldName, Reader reader) {
         return new ICUFoldingFilter(
             new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader));
       }
     };
 
     // case folding
     assertAnalyzesTo(a, "This is a test", new String[] { "this", "is", "a", "test" });
 
     // case folding
     assertAnalyzesTo(a, "Ruß", new String[] { "russ" });
     
     // case folding with accent removal
     assertAnalyzesTo(a, "ΜΆΪΟΣ", new String[] { "μαιοσ" });
     assertAnalyzesTo(a, "Μάϊος", new String[] { "μαιοσ" });
 
     // supplementary case folding
     assertAnalyzesTo(a, "𐐖", new String[] { "𐐾" });
     
     // normalization
     assertAnalyzesTo(a, "ﴳﴺﰧ", new String[] { "طمطمطم" });
 
     // removal of default ignorables
     assertAnalyzesTo(a, "क्‍ष", new String[] { "कष" });
     
     // removal of latin accents (composed)
     assertAnalyzesTo(a, "résumé", new String[] { "resume" });
     
     // removal of latin accents (decomposed)
     assertAnalyzesTo(a, "re\u0301sume\u0301", new String[] { "resume" });
     
     // fold native digits
     assertAnalyzesTo(a, "৭০৬", new String[] { "706" });
     
     // ascii-folding-filter type stuff
     assertAnalyzesTo(a, "đis is cræzy", new String[] { "dis", "is", "craezy" });

    // proper downcasing of Turkish dotted-capital I
    // (according to default case folding rules)
    assertAnalyzesTo(a, "ELİF", new String[] { "elif" });
    
    // handling of decomposed combining-dot-above
    assertAnalyzesTo(a, "eli\u0307f", new String[] { "elif" });
   }
 }
