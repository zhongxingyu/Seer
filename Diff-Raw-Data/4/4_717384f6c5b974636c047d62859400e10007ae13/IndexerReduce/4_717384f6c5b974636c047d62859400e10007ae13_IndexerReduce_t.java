 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.hd.cl.haas.distributedcrawl.Indexer;
 
 import de.hd.cl.haas.distributedcrawl.common.Posting;
 import de.hd.cl.haas.distributedcrawl.common.PostingList;
 import de.hd.cl.haas.distributedcrawl.common.Term;
 import java.io.IOException;
 import java.util.ArrayList;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 
 /**
  *
  * @author haas
  */
 public class IndexerReduce extends Reducer<Term, Posting, Term, PostingList> {
 
     @Override
     protected void reduce(Term key, Iterable<Posting> values, Context context) throws IOException, InterruptedException {
 
         System.out.println("Reducer: Key is: " + key.toString());
         ArrayList<Posting> pl = new ArrayList<Posting>();
         for (Posting p : values) {
             // key is term, value is posting: url,termcount
             //String[] tokens = composite.toString().split(",");
             //String term = tokens[0];
             //String count = tokens[1];
             //context.write(key, composite);
             pl.add(p);
         }
         PostingList result = new PostingList();
         // Ugly. to populate TextArrayWritable, we need to call set()
         // which expects an Array.
         // We need to pass in an array of type Text to get the type right
        result.set(pl.toArray(new Posting[pl.size()]));
         context.write(key, result);
 
     }
     // from http://stackoverflow.com/a/8210025
 }
