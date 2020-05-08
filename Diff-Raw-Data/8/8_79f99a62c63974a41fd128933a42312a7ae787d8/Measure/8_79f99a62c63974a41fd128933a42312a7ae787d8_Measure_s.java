 package net.kaoriya.qb.similarity_ranker.benchmark;
 
 public abstract class Measure
 {
     private final String name;
 
     public Measure(String name)
     {
         this.name = name;
     }
 
     public final void run() throws Exception
     {
        System.out.format("%1$s [start]\n", this.name);
         long start = System.nanoTime();
         try {
             execute();
         } catch (Exception e) {
             throw e;
         } finally {
             long duration = System.nanoTime() - start;
            System.out.format("%1$s [end in %2$.9f sec]\n", this.name,
                     duration / 1.0e9);
         }
     }
 
     protected abstract void execute() throws Exception;
 }
