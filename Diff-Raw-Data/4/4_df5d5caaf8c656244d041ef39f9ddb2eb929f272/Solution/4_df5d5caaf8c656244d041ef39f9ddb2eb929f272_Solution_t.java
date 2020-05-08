 public class Solution {
 
     public void nextPermutation(int[] num) {
        if(num == null || num.length<=1)
             return;
 
         boolean hasReverse = false;
         int src = 0;
         int dest = num.length-1;
         for(int j = num.length-2; j >=0 && !hasReverse; --j)
             for(int i= num.length-1; i >j; --i) {
                 if(num[i] > num[j])
                 {
                     hasReverse = true;
                     src = j;
                     dest = i;
                     break;
                 }
             }
         if(hasReverse) {
             int tmp = num[src];
             num[src] = num[dest];
             num[dest] = tmp;
             src++;
         }
        dest = num.length-1;
         for(;src<dest;src++,dest--)
         {
             int tmp = num[src];
             num[src] = num[dest];
             num[dest] = tmp;
         }
 
     }
 }
