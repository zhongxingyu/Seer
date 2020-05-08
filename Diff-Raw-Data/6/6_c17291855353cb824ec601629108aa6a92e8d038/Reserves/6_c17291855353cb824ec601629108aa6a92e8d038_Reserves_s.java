 package gipfj;
 
 /*
  * Gipf count: how many are on the board
  * Piece count: how many are in reserve.
  * 
  * What about: piece-on-field count??.
  * not important
  *  
  */
 public class Reserves {
     private final long p1;
     private final long p2;
     private final long g1;
     private final long g2;
 
     public Reserves(long p1, long p2, long g1, long g2) {
         this.p1 = p1;
         this.p2 = p2;
         this.g1 = g1;
         this.g2 = g2;
     }
 
     @Override
     public String toString() {
         return String.format("reserves %d %d : %d %d", p1, p2, g1, g2);
     }
 
     // Statics...
 
     public static Reserves makeReserves(long p, long g) {
         return new Reserves(p, p, g, g);
     }
 
     public static Reserves incReserves(Reserves r, long player) {
         if (player > 0) {
             return new Reserves(r.p1 + 1, r.p2, r.g1, r.g2);
         } else {
             return new Reserves(r.p1, r.p2 + 1, r.g1, r.g2);
         }
     }
 
     public static Reserves decReserves(Reserves r, long player) {
         if (player > 0) {
             return new Reserves(r.p1 - 1, r.p2, r.g1, r.g2);
         } else {
             return new Reserves(r.p1, r.p2 - 1, r.g1, r.g2);
         }
     }
 
     public static long getReserves(Reserves r, long player) {
         if (player > 0) {
             return r.p1;
         } else {
             return r.p2;
         }
     }
 
     public static Reserves decGipfs(Reserves r, long player) {
         if (player > 0) {
             return new Reserves(r.p1, r.p2, r.g1 - 1, r.g2);
         } else {
             return new Reserves(r.p1, r.p2, r.g1, r.g2 - 1);
         }
     }
 
     public static Reserves incGipfs(Reserves r, long player) {
         if (player > 0) {
             return new Reserves(r.p1, r.p2, r.g1 + 1, r.g2);
         } else {
             return new Reserves(r.p1, r.p2, r.g1, r.g2 + 1);
         }
     }
 
     public static long getGipfs(Reserves r, long player) {
         if (player > 0) {
             return r.g1;
         } else {
             return r.g2;
         }
     }
 
     public static boolean losingReserve(Reserves r, long player) {
         if (player > 0) {
             return (r.p1 <= 0) || (r.g1 <= 0);
         } else {
             return (r.p2 <= 0) || (r.g2 <= 0);
         }
     }
 
 }
