 package gipfj;
 
 /**
  * A depth two transposition table with depth-based replacement scheme - new
  * nodes are always accepted, and nodes that were searched deeper replace
  * short-depth nodes. (this idea comes from <i>Replacement Schemes for
  * Transposition Tables</i>)
  * 
  * @author msto
  * 
  */
 public class DTable {
     private final int size;
     private final int shift_cut;
     private int count_null;
     private int count_first;
     private int count_second;
 
     private DTable(int sexp) {
         size = 1 << sexp;
         shift_cut = 32 - sexp;
         store = new Entry[size];
 
         count_null = 0;
         count_first = 0;
         count_second = 0;
     }
 
     private Entry[] store;
 
     private void add(CompressedSGS in, int depth, int rank) {
         // what about in.hashCode() >>> shift_cut;
         int index = in.hashCode() >>> shift_cut;
         // if (index < 0) {
         // index = -2 * index - 1;
         // } else {
         // index = 2 * index;
         // }
         Entry ff = store[index];
         Entry n = new Entry(in, depth, rank);
         if (ff == null) {
             store[index] = n;
         } else {
             // first entry always has greater depth;
             // second entry is pushed out
             if (n.depth > ff.depth) {
                 store[index] = n;
                 n.second = ff;
             } else {
                 ff.second = n;
             }
 
         }
     }
 
     private void load() {
         if (store == null) {
             store = new Entry[size];
         }
     }
 
     private void empty() {

         count_null = 0;
         count_first = 0;
         count_second = 0;
         store = null;
     }
 
     private void analyze() {
         int empty = 0;
         int single = 0;
         int full = 0;
 
         for (Entry e : store) {
             if (e == null) {
                 empty++;
             } else if (e.second == null) {
                 single++;
             } else {
                 full++;
             }
         }
         System.out.println("== Depth 2 table analysis results:");
         System.out.format("== Search: fail - %d; first %d; second %d\n",
                 count_null, count_first, count_second);
         System.out.format("== State: empty %d; single entry %d; full %d\n",
                 empty, single, full);
     }
 
     private Long get(CompressedSGS in, int depth) {
         int index = in.hashCode() >>> shift_cut;
         Entry f = store[index];
         if (f == null) {
             count_null++;
             return null;
         }
 
         if (f.equals(in, depth)) {
             count_first++;
             return (long) f.rank;
         } else if (f.second != null && f.second.equals(in, depth)) {
             count_second++;
             return (long) f.second.rank;
         }
         return null;
     }
 
     private void change(CompressedSGS in, int depth, int rank) {
         int index = in.hashCode() >>> shift_cut;
         Entry ff = store[index];
         if (ff == null) {
             add(in, depth, rank);
         }
 
         if (ff.equals(in, depth)) {
             ff.rank = rank;
        } else if (ff.second.equals(in, depth)) {
             ff.second.rank = rank;
         } else {
            System.out.println("Changing value that does not exist.");
         }
     }
 
     public static DTable dmake(long size) {
         return new DTable((int) size);
     }
 
     public static void dadd(DTable d, CompressedSGS gs, long depth, long rank) {
         d.add(gs, (int) depth, (int) rank);
     }
 
     public static Long dget(DTable d, CompressedSGS gs, long depth) {
         return d.get(gs, (int) depth);
     }
 
     public static void dchange(DTable d, CompressedSGS gs, long depth, long rank) {
         d.change(gs, (int) depth, (int) rank);
     }
 
     public static void dclear(DTable d) {
         d.empty();
         d.load();
     }
 
     public static void dempty(DTable d) {
         d.empty();
     }
 
     public static void dload(DTable d) {
         d.load();
     }
 
     public static void danalyze(DTable d) {
         d.analyze();
     }
 
     private class Entry {
         public int hc;
         public int rank;
         public byte depth;
         // this will be null for the second depth entry - we could remove it and
         // make a 2ndEntry class
         public Entry second;
 
         public byte d4;
         public byte d5;
         public byte d6;
         public byte d7;
         public byte d8;
         public byte d9;
         public byte d10;
         public byte d11;
         public byte d12;
         public byte d13;
         public byte d14;
         public byte d15;
         public byte d16;
         public byte d17;
         public byte d18;
         public byte d19;
         public byte d20;
         public byte d21;
         public byte d22;
         public byte d23;
         public byte d24;
         public byte d25;
         public byte d26;
         public byte d27;
         public byte d28;
         public byte d29;
         public byte d30;
 
         public Entry(CompressedSGS d, int depth, int rank) {
             byte[] q = d.getData();
             hc = d.hashCode();
             this.depth = (byte) depth;
             this.rank = rank;
             // o yay! 27 entries
 
             // regexes!
             d4 = q[4];
             d5 = q[5];
             d6 = q[6];
             d7 = q[7];
             d8 = q[8];
             d9 = q[9];
             d10 = q[10];
             d11 = q[11];
             d12 = q[12];
             d13 = q[13];
             d14 = q[14];
             d15 = q[15];
             d16 = q[16];
             d17 = q[17];
             d18 = q[18];
             d19 = q[19];
             d20 = q[20];
             d21 = q[21];
             d22 = q[22];
             d23 = q[23];
             d24 = q[24];
             d25 = q[25];
             d26 = q[26];
             d27 = q[27];
             d28 = q[28];
             d29 = q[29];
             d30 = q[30];
         }
 
         public boolean equals(CompressedSGS in, int depth2) {
             if (depth2 != depth || in.hashCode() != hc)
                 return false;
 
             byte[] q = in.getData();
             return (d4 == q[4]) && (d5 == q[5]) && (d6 == q[6]) && (d7 == q[7])
                     && (d8 == q[8]) && (d9 == q[9]) && (d10 == q[10])
                     && (d11 == q[11]) && (d12 == q[12]) && (d13 == q[13])
                     && (d14 == q[14]) && (d15 == q[15]) && (d16 == q[16])
                     && (d17 == q[17]) && (d18 == q[18]) && (d19 == q[19])
                     && (d20 == q[20]) && (d21 == q[21]) && (d22 == q[22])
                     && (d23 == q[23]) && (d24 == q[24]) && (d25 == q[25])
                     && (d26 == q[26]) && (d27 == q[27]) && (d28 == q[28])
                     && (d29 == q[29]) && (d30 == q[30]);
         }
     }
 }
