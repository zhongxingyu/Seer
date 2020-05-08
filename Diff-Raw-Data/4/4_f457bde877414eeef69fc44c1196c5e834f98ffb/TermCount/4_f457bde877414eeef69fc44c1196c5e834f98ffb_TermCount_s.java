 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.hd.cl.haas.distributedcrawl.common;
 
 import org.apache.hadoop.io.LongWritable;
 
 /**
  * This class holds a tuple of Term and frequency.
  *
  * It is used in @IndexerMap.java to implement the key-value inversion pattern.
  *
  * It implements Comparable. When compared, first the Term is compared. If both
  * terms are not equal, the frequency is compared.
  *
  * This has the effect that the key arrive in the right order so the index is
  * sorted by key while additionally sorting the postings list for the term.
  * 
  * This is used in conjunction with @MergerPartitioner to ensure that keys
  * with the same term arrive at the same reducer.
  * 
  * If multiple reducers are used, the result files are probably easily concatenated (?!).
  *
  * @author Michael Haas <haas@cl.uni-heidelberg.de>
  */
public class TermCount extends TextLongWritable implements Comparable<TermCount> {
     // TODO: what constraints wrt implementation of Comparable are there? hashcode? equals?
 
     public TermCount(Term t, LongWritable i) {
         super(t, i);
     }
 
     public Term getTerm() {
         return new Term(this.getText());
     }
 
     @Override
     public int compareTo(TermCount o) {
         int c1 = this.getTerm().compareTo(o.getTerm());
         if (c1 != 0) {
             return c1;
         } else {
           return this.getValue().compareTo(o.getValue());  
         }
     }
 }
