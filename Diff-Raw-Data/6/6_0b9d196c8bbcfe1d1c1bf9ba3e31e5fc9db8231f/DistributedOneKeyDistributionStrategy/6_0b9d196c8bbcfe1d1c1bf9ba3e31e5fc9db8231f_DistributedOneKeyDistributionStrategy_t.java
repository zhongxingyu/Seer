 package de.uzl.decentsparqle.p2p;
 
 import com.google.common.util.concurrent.ListenableFuture;
 import lupos.datastructures.items.Triple;
 import lupos.engine.operators.tripleoperator.TriplePattern;
 import net.tomp2p.peers.Number160;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Random;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  * @author Oliver Kleine
  */
 public class DistributedOneKeyDistributionStrategy extends AbstractDistributionStrategy {
 
     public static int MAX_HASH_DEPTH = 10;
     private static Random random = new Random(System.currentTimeMillis());
     private ExecutorService executorService = Executors.newFixedThreadPool(30);
 
     @Override
     public ListenableFuture<Iterable<Triple>> get(TriplePattern pattern) throws UnsupportedTriplePatternException {
         if (!containsAtLeastOneLiteral(pattern)) {
             throw new UnsupportedTriplePatternException(this, pattern);
         }
 
         return get(getKeys(pattern));
     }
 
     @Override
     public ListenableFuture<Void> add(Triple triple) {
         return combinedFuture(
                 add(createHashKey(triple.getSubject().toString()), triple),
                 add(createHashKey(triple.getPredicate().toString()), triple),
                 add(createHashKey(triple.getObject().toString()), triple)
         );
     }
 
     @Override
     public ListenableFuture<Void> remove(Triple triple){
         ArrayList<ListenableFuture<Void>> futures = new ArrayList<ListenableFuture<Void>>(3 * MAX_HASH_DEPTH);
         for(int i = 0; i < MAX_HASH_DEPTH; i++){
             futures.add(i, remove(createHashKey(triple.getSubject().toString(), i), triple));
             futures.add(2 * MAX_HASH_DEPTH + i, remove(createHashKey(triple.getPredicate().toString(), i), triple));
             futures.add(3 * MAX_HASH_DEPTH + i, remove(createHashKey(triple.getObject().toString(), i), triple));
         }
 
         return combinedFuture(futures);
     }
 
     @Override
     public ListenableFuture<Boolean> contains(Triple triple){
         ArrayList<ListenableFuture<Boolean>> futures = new ArrayList<ListenableFuture<Boolean>>(3 * MAX_HASH_DEPTH);
         for(int i = 0; i < MAX_HASH_DEPTH; i++){
             futures.add(i, contains(createHashKey(triple.getSubject().toString(), i), triple));
             futures.add(2 * MAX_HASH_DEPTH + i, contains(createHashKey(triple.getPredicate().toString(), i), triple));
             futures.add(3 * MAX_HASH_DEPTH + i, contains(createHashKey(triple.getObject().toString(), i), triple));
         }
         return combinedBooleanFuture(futures);
     }
 
     @Override
     protected Iterable<Number160> getKeys(TriplePattern pattern) {
         ArrayList<Number160> keys = new ArrayList<Number160>();
 
         if(hasSubject(pattern))
             return createHashKeys(pattern.getPos(0).toString());
         if(hasPredicate(pattern))
             return createHashKeys(pattern.getPos(1).toString());
         if(hasObject(pattern))
             return createHashKeys(pattern.getPos(2).toString());
 
         return new ArrayList<Number160>(0);
     }
 
     private Collection<Number160> createHashKeys(String value){
         ArrayList<Number160> hashes = new ArrayList<Number160>();
         String nextValue = value;
         for(int i = 0; i < MAX_HASH_DEPTH; i++){
             hashes.add(Number160.createHash(nextValue));
             nextValue = hashes.get(hashes.size() - 1).toString();
         }
         return hashes;
     }
 
 
     private String createHashKey(String key){
         int hashDepth = random.nextInt(MAX_HASH_DEPTH);
         return createHashKey(key, hashDepth);
     }
 
     private String createHashKey(String key, int hashDepth){
        for(int i = 0; i < hashDepth; i++) {
             key = Number160.createHash(key).toString();
         }
        return key;
     }
 
 
 }
