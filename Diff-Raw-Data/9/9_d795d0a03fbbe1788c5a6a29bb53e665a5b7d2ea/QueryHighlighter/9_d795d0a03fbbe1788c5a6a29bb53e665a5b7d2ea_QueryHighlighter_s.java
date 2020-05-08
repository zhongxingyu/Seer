 package uk.ac.ebi.arrayexpress.utils.saxon.search;
 
 /*
  * Copyright 2009-2010 Microarray Informatics Group, European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import org.apache.lucene.search.highlight.Highlighter;
 import org.apache.lucene.search.highlight.NullFragmenter;
 import org.apache.lucene.search.highlight.QueryScorer;
 import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class QueryHighlighter implements IQueryHighlighter
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private IndexEnvironment env;
 
     private final String HIT_OPEN_MARK = "\u00ab";
     private final String HIT_CLOSE_MARK = "\u00bb";
 
     public IQueryHighlighter setEnvironment( IndexEnvironment env )
     {
         this.env = env;
         return this;
     }
 
     public String highlightQuery( QueryInfo queryInfo, String fieldName, String text )
     {
         try {
             SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter(HIT_OPEN_MARK, HIT_CLOSE_MARK);
             Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(queryInfo.getQuery(), fieldName, this.env.defaultField));
             highlighter.setTextFragmenter(new NullFragmenter());
 
            String str = highlighter.getBestFragment(this.env.indexAnalyzer, fieldName, text);
 
             return null != str ? str : text;
         } catch (Exception x) {
             logger.error("Caught an exception:", x);
         }
         return text;
     }
 }
