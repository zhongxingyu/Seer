 package cc.explain.server.api;
 
 import cc.explain.server.core.NLPTask;
 import cc.explain.server.core.StanfordNLP;
 import cc.explain.server.exception.TechnicalException;
 import com.google.common.collect.Lists;
 import edu.stanford.nlp.ling.HasWord;
 
 import java.util.List;
 import java.util.concurrent.*;
 
 /**
  * User: kzimnick
  * Date: 12.05.13
  * Time: 13:37
  */
 public class PhrasalVerbTask implements Callable<List<String>>{
 
     private StanfordNLP stanfordNLP;
     private String text;
 
 
     public PhrasalVerbTask(String text){
         this.text = text;
         this.stanfordNLP = new StanfordNLP();
     }
 
     public List<String> getPhrasalVerbs(String text)  { //todo refactor
         List<List<HasWord>> sentences = stanfordNLP.getSentences(text);
         int threadNumber = Math.max( 2, (Runtime.getRuntime().availableProcessors() / 2));
        System.out.println("THREAD NUMBER: "+threadNumber);
         ExecutorService service = Executors.newFixedThreadPool(threadNumber);
         List<Future<List<String>>> futures = Lists.newArrayListWithCapacity(threadNumber);
        List<List<List<HasWord>>> partition = Lists.partition(sentences, (sentences.size()+1)/threadNumber);
         for (List<List<HasWord>> part : partition){
             Future<List<String>> f = service.submit(new NLPTask(part));
             futures.add(f);
         }
         List<String> phrasalVerbs = Lists.newLinkedList();
         try {
             for(Future<List<String>> f : futures){
                 phrasalVerbs.addAll(f.get());
             }
         } catch (InterruptedException e) {
            throw new TechnicalException(e);
         } catch (ExecutionException e) {
             throw new TechnicalException(e);
         }
         service.shutdown();
         return phrasalVerbs;
     }
 
     public List<String> call() throws Exception {
         System.out.println("CALL");
         return getPhrasalVerbs(this.text);
     }
 }
