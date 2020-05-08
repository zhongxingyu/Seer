 package rtsjcomponents.utils;
 
 import javax.realtime.LTMemory;
 import javax.realtime.ScopedMemory;
 
 /**
  * Implements a
  * 
  * @author juancol
  */
 public class ScopedMemoryPool
 {
     //  TODO Constants can be assign from a property file.
     private static final int NUM_OF_MEM_SCOPES = 50;
 
    private static final long MIN_SCOPE_SIZE = 100 * 1024;
 
    private static final long MAX_SCOPE_SIZE = 100 * 1024;
 
     private static Queue unusedMemoryScopes;
 
     static
     {
         try
         {
             unusedMemoryScopes = Queue.fromImmortal();
             for (int i = 0; i < NUM_OF_MEM_SCOPES; i++)
             {
                 unusedMemoryScopes.enqueue(new LTMemory(MIN_SCOPE_SIZE, MAX_SCOPE_SIZE));
             }
         }
         catch (Exception e)
         {
             e.printStackTrace();
             System.exit(-1);
         }
     }
 
     /**
      * Obtains a scoped memory area from the pool.
      * 
      * @return a scoped memory instance from the pool; <code>null</code> if there is no instance
      *         available.
      */
     public static ScopedMemory getInstance()
     {
         return (ScopedMemory) unusedMemoryScopes.dequeue();
     }
 
     /**
      * Puts the specified scoped memory area into the pool.
      * 
      * @param sm
      */
     public static void freeInstance(ScopedMemory sm)
     {
         unusedMemoryScopes.enqueue(sm);
     }
 }
