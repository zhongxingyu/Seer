 package org.vika.routing;
 
import com.sun.istack.internal.NotNull;
 
 /**
  * @author oleg
  */
 public class Pair<A, B> {
     public final A fst;
     public final B snd;
 
 
    public Pair(@NotNull final A fst, @NotNull final B snd) {
         this.fst = fst;
         this.snd = snd;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Pair)) return false;
 
         Pair pair = (Pair) o;
 
         if (!fst.equals(pair.fst)) return false;
         if (!snd.equals(pair.snd)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = fst.hashCode();
         result = 31 * result + snd.hashCode();
         return result;
     }
 }
