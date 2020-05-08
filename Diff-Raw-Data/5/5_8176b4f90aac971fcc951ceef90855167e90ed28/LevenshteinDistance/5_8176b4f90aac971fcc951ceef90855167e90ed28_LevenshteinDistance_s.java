 package mylib.search.levenshteindistance;
 
 import mylib.math.Math;
 
 /**
  * @author M.Korotin
  *         Date: 10.07.11
  *         Time: 21:11
  */
 public class LevenshteinDistance {
 
     /**
      * @param input проверяемое слово
      * @param pattern слово-образец(из алфавита)
      * @return расстояние Левенштейна
      */
     public int getDistance(String input, String pattern){
         int n = pattern.length() + 1;
         int m = input.length() + 1;
 
         if(n - 1 == 0){
             return m - 1;
         }
 
         if(m - 1 == 0){
             return n - 1;
         }
 
         int[][] matrix = new int[n][m];
 
         for(int i = 0; i < n; i++){
             matrix[i][0] = i;
         }
 
         int c = 0;
         char ch = '\0';
         for(int i = 1; i < m; i++){
             ch = input.charAt(i - 1);
             matrix[0][i] = i;
             for(int j = 1; j < n; j++){
                 c = pattern.charAt(j - 1) == ch ? 0 : 1;
                 matrix[j][i] = Math.min(matrix[j][i - 1] + 1,
                         matrix[j - 1][i] + 1,
                         c + matrix[j - 1][i - 1]);
             }
         }
 
         return matrix[n - 1][m - 1];
     }

    public static void main(String[] args) {
        for(int i = 0; i < 1000000; i++)
            System.out.println(new LevenshteinDistance().getDistance("коротин", "миша"));
    }
 }
