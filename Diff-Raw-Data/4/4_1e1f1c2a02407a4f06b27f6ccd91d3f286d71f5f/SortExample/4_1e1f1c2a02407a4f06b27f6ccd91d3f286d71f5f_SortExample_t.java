 package crunch.samples;
 
 import org.apache.crunch.DoFn;
 import org.apache.crunch.Emitter;
 import org.apache.crunch.PCollection;
 import org.apache.crunch.Pipeline;
 import org.apache.crunch.impl.mr.MRPipeline;
import org.apache.crunch.lib.Sort;
 import org.apache.crunch.types.writable.Writables;
 
 import crunch.samples.WordCount.Tokenizer;
 
 /**
  * Sample input args: <i>src/main/resources/hadoopIssues.txt
  * target/sorted-text</i>
  * <P>
  * This would first split all words and then converts them to lowercase. It then
  * applies sorting and the result would be written back to the output
  */
 public class SortExample {
 
   static class Lowecase extends DoFn<String, String> {
 
     @Override
     public void process(String input, Emitter<String> emitter) {
       emitter.emit(input.toLowerCase());
     }
   }
 
   public static void main(String[] args) throws Exception {
     Pipeline pipeline = new MRPipeline(SortExample.class);
     PCollection<String> lines = pipeline.readTextFile(args[0]);
     PCollection<String> words = lines.parallelDo(new Tokenizer(), Writables.strings()).parallelDo(new Lowecase(),
         Writables.strings());
    PCollection<String> wordInAscendingOrder = Sort.sort(words);
     pipeline.writeTextFile(wordInAscendingOrder, args[1]);
     pipeline.done();
   }
 
 }
