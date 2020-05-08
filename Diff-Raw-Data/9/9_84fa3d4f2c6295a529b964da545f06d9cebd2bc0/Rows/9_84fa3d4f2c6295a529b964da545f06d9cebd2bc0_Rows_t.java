 // Oliver Kullmann, 21.11.2011 (Swansea)
 
 class Rows {
 
   public static int[][][] list_rows(final int k, final int m, final int n, final int r) {
     assert(k >= 2);
     assert(m >= 1);
     assert(n >= 1);
     assert(k <= m || k <= n);
     assert(r >= 1);
     int[][][] rows = new int[r][][];
     int row_index = 0;
     // horizontal rows:
     for (int i = 0; i < m; ++i)
       for (int j = 0; j <= n-k; ++j, ++row_index) {
         rows[row_index] = new int[k][2];
         for (int p = 0; p < k; ++p) {
           rows[row_index][p][0] = i;
           rows[row_index][p][1] = j+p;
         }
       }
     // vertical rows:
     for (int j = 0; j < n; ++j)
       for (int i = 0; i <= m-k; ++i, ++row_index) {
         rows[row_index] = new int[k][2];
         for (int p = 0; p < k; ++p) {
           rows[row_index][p][0] = i+p;
           rows[row_index][p][1] = j;
         }
       }
     // diagonal rows, top-left to bottom-right:
     for (int i = 0; i < m+n-1; ++i) {
       final int tl_i = Math.max(m-1-i,0);
       final int tl_j = Math.max(i-m+1,0);
       final int br_i = m-1-Math.max(i-n+1,0);
       final int br_j = Math.min(i,n-1);
       final int length = br_i - tl_i + 1;
       assert(length == br_j - tl_j + 1);
       for (int d = 0; d <= length-k; ++d, ++row_index) {
         rows[row_index] = new int[k][2];
         for (int p = 0; p < k; ++p) {
          rows[row_index][p][0] = tl_i + d + p;
          rows[row_index][p][1] = tl_j + d + p;
         }
       }
     }
     // diagonal rows, bottom-left to top-right:
     for (int i = 0; i < m+n-1; ++i) {
       final int bl_i = Math.min(i,m-1);
       final int bl_j = Math.max(i-m+1,0);
       final int tr_i = Math.max(i-n+1,0);
       final int tr_j = Math.min(i,n-1);
       final int length = bl_i - tr_i + 1;
       assert(length == tr_j - bl_j + 1);
       for (int d = 0; d <= length-k; ++d, ++row_index) {
         rows[row_index] = new int[k][2];
         for (int p = 0; p < k; ++p) {
          rows[row_index][p][0] = bl_i - d - p;
          rows[row_index][p][1] = bl_j + d + p;
         }
       }
     }
     assert(row_index == r);
     return rows;
   }
 
   // performs a formal check on lr, a "list of rows":
   public static boolean check_list_rows(final int[][][] lr, final int[][] field) {
     if (lr == null) return false;
     final int R = lr.length;
     if (R == 0) return false;
     if (lr[0] == null) return false;
     final int K = lr[0].length;
     if (K == 0) return false;
     for (int r = 0; r < R; ++r) {
       if (lr[r] == null) return false;
       if (lr[r].length != K) return false;
       for (int p = 0; p < K; ++p) {
         if (lr[r][p] == null) return false;
         if (lr[r][p].length != 2) return false;
         if (! Field.valid_coordinates(field, lr[r][p][0]+1, lr[r][p][1]+1)) return false;
       }
     }
     return true;
   }
 
 
   // for testing:
   public static void main(final String[] args) {
     final int K = Integer.parseInt(args[0]);
     final int M = Integer.parseInt(args[1]);
     final int N = Integer.parseInt(args[2]);
     final int R = Counting.number_rows(K,M,N);
     final int[][][] rows = list_rows(K,M,N,R);
     for (int r = 0; r < R; ++r) {
       System.out.print(r + ": ");
       for (int p = 0; p < K; ++p)
         System.out.print("(" + rows[r][p][0] + "," + rows[r][p][1] + ")");
       System.out.println();
     }
   }
 
 }
