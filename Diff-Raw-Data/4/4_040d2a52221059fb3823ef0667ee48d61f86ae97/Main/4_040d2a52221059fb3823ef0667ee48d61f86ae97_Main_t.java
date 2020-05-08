 package org.jasmine.cli;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import org.jasmine.Failure;
 import org.jasmine.Identifier;
 import org.jasmine.Notifier;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class Main {
     public static void main(String... args){
         new org.jasmine.Runtime(Arrays.asList(args)).execute(new Notifier() {
             private Multimap<Identifier, Failure> failures = HashMultimap.create();
             private Map<Identifier, String> descriptions = new HashMap<>();
 
             @Override
             public void started() {
             }
 
             @Override
             public void pass(Identifier identifier, String description) {
                 descriptions.put(identifier, description);
                 System.out.print(".");
             }
 
             @Override
             public void fail(Identifier identifier, String description, Set<Failure> failures) {
                 descriptions.put(identifier, description);
                 this.failures.putAll(identifier, failures);
                 System.out.print("F");
             }
 
             @Override
             public void finished() {
                 System.out.println();
                 System.out.println();
                 for(Map.Entry<Identifier, Collection<Failure>> entry : failures.asMap().entrySet()){
                     System.out.println(descriptions.get(entry.getKey()));
                     System.out.println();
                     for(Failure failure : entry.getValue()){
                         System.out.println(failure.getStackString().replaceAll("^", "  ").replaceAll("\\n", "\n  "));
                     }
                 }
                 System.out.println(String.format("%s/%s passed.",
                         descriptions.size() - failures.keySet().size(), descriptions.size()));

                if(failures.size() > 0){
                    System.exit(1);
                }
             }
         });
     }
 }
