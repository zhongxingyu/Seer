 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cache_controller;
 
 import cache.TwoLayerCache;
 import cache_controller.instruction.Instruction;
 import cache_controller.instruction.InstructionThread;
 import inputreader.InstructionInputFileReader;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  *
  * @author Nathan
  */
 public class Core {
 
     private TwoLayerCache cache;
     
     private long previousCacheMiss1;
     private long previousCacheHits1;
     private long previousCacheMiss2;
     private long previousCacheHits2;
     
     private int index;
     private LinkedList<InstructionThread> threads;
     
     public Core(TwoLayerCache cache) {
         this.cache = cache;
         
         previousCacheMiss1 = 0;
         previousCacheHits1 = 0;
         previousCacheMiss2 = 0;
         previousCacheHits2 = 0;
         
         index = 0;
         threads = new LinkedList<>();
     }
     
     private void increaseIndex() {
         index++;
         if(index == threads.size()) {
             index = 0;
         }
     }
     
     private InstructionThread getExecutingThread() {
         InstructionThread chosenThread = null;
         
         Iterator<InstructionThread> it = threads.listIterator(index);
         int count = 0;
         while(it.hasNext() && chosenThread == null) {
             increaseIndex();
             count++;
             InstructionThread thread = it.next();
             if(thread.getWaitingTime() == 0) {
                 chosenThread = thread;
             }
         }
         if(chosenThread == null && count < threads.size()) {
             it = threads.listIterator(index);
             while(it.hasNext() && chosenThread == null) {
                 increaseIndex();
                 InstructionThread thread = it.next();
                 if(thread.getWaitingTime() == 0) {
                     chosenThread = thread;
                 }
             }
         }
         
         return chosenThread;
     }
     
     public void execute() {
         if(!threads.isEmpty()) {
             for(InstructionThread thread: threads) {
                 thread.decreaseWaitingTime();
             }
             
             InstructionThread thread = getExecutingThread();
             
             if(thread != null) {
                 thread.setNextInstruction();
                 Instruction instr = thread.getInstruction();
                 if(instr == null) {
                     // geen instructies over, thread is klaar
                    int threadIndex = threads.indexOf(thread);
                    if(threadIndex < index) {
                        index--;
                    } else if(threadIndex == index) {
                        increaseIndex();
                    }
                     threads.remove(thread);
                 } else {
                     thread.setWaitingTime(instr.getExecutionTime(cache));
                 }
             }
         }        
     }
     
     public void addThread(long thread, InstructionInputFileReader reader) {
         threads.add(index, new InstructionThread(thread, reader));
     }
     
     public int getThreadCount() {
         return threads.size();
     }
     
     public String print(long id, long cyclus) {
         long missDiff1 = cache.getLayer1().getTotalMisses() - previousCacheMiss1;
         long hitDiff1 = cache.getLayer1().getCacheHits() - previousCacheHits1;
         
         long missDiff2 = cache.getLayer2().getTotalMisses() - previousCacheMiss2;
         long hitDiff2 = cache.getLayer2().getCacheHits() - previousCacheHits2;
         
         String print = "";
         if(missDiff2 != 0) {
             print += "" + 0 + ";" + cyclus + ";" + missDiff2 + System.lineSeparator();
         }
         for(InstructionThread thread: threads) {
             Instruction instr = thread.getInstruction();
             if(instr != null) {
                 print += "" + 1 + ";" + cyclus + ";" + instr.getInstructionAdress() + System.lineSeparator();
             }
         }
         
         previousCacheMiss1 = cache.getLayer1().getTotalMisses();
         previousCacheHits1 = cache.getLayer1().getCacheHits();
         
         previousCacheMiss2 = cache.getLayer2().getTotalMisses();
         previousCacheHits2 = cache.getLayer2().getCacheHits();
         
         return print;
     }
     
 }
