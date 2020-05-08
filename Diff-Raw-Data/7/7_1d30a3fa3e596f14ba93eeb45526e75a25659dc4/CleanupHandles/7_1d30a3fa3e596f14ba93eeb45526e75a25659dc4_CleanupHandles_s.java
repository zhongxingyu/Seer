 package fi.jawsy.jawwa.frp;
 
 import java.io.Serializable;
 
 public final class CleanupHandles {
 
     private CleanupHandles() {
     }
 
     static class NoopCleanupHandle implements CleanupHandle, Serializable {
         private static final long serialVersionUID = -3868657478362679243L;
 
         @Override
         public void cleanup() {
         }
     }
 
     public static final CleanupHandle NOOP = new NoopCleanupHandle();
 
     public static CleanupHandle merge(final CleanupHandle first, final CleanupHandle second) {
         class MergedCleanupHandle implements CleanupHandle, Serializable {
             private static final long serialVersionUID = 586544697512629591L;
 
             @Override
             public void cleanup() {
                 first.cleanup();
                 second.cleanup();
             }
         }
         return new MergedCleanupHandle();
     }
 
 }
