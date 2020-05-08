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
        System.out.format("%1$s [start]", this.name);
        System.out.println();
         long start = System.nanoTime();
         try {
             execute();
         } catch (Exception e) {
             throw e;
         } finally {
             long duration = System.nanoTime() - start;
            System.out.format("%1$s [end in %2$.9f sec]", this.name,
                     duration / 1.0e9);
            System.out.println();
         }
     }
 
     protected abstract void execute() throws Exception;
 }
