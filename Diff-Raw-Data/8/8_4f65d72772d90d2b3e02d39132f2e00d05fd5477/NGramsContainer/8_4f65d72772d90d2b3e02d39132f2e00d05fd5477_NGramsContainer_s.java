 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.aperigeek.jlad.detector.ngrams;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author Vivien Barousse
  */
 public class NGramsContainer {
     
    private int count;
     
    private Map<String, Integer> ngrams = new HashMap<String, Integer>();
     
    public void put(String ngram, int count) {
         ngrams.put(ngram, count);
         this.count += count;
     }
     
     public double getNGram(String ngram) {
         if (!ngrams.containsKey(ngram)) {
             return 0.0d;
         }
         return ngrams.get(ngram).doubleValue() / count;
     }
     
 }
