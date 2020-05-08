 public class Solution {
 
     class Meta{
         int end;
         int start;
        public Meta(int start, int end){
             this.end = end;
             this.start = start;
         }
     }
     public int longestConsecutive(int[] num) {
         if (num == null)
             return 0;
         int res = 0;
         Map<Integer,Meta> hg = new HashMap<Integer,Meta>();
         for(int val: num) {
             if(hg.containsKey(val)){
                 continue;
             }
             int end =val;
             int start =val;
 
             int below = val-1;
             if(hg.containsKey(below)){
                 Meta belowMeta = hg.get(below);
                 start = belowMeta.start;
             }
             int above = val+1;
             if(hg.containsKey(above)){
                 Meta aboveMeta = hg.get(above);
                 end = aboveMeta.end;
             }
             hg.put(val, new Meta(start, end));
             hg.put(start, new Meta(start, end));
             hg.put(end, new Meta(start, end));
             if(end-start+1 > res)
                 res=end-start+1;
         }
        return res; 
     }
 }
