 import java.util.HashMap;
 
 public class Run {
     public Run() {}
 
     private static char MIN_ITEM = 'a';
     private static char MAX_ITEM = 'z';
 
    private int rnd_state = 3;
 
     private int myrandom() {
        rnd_state = (rnd_state * rnd_state) % (11 * 19);
         return rnd_state;
     }
 
     private char item() {
         return (char)(MIN_ITEM + (char)(myrandom() % (MAX_ITEM - MIN_ITEM + 1)));
     }
 
     private int width() {
         return myrandom() % 9 + 1;
     }
 
     private Object generate(int depth) {
         if (depth == 0) {
             return item();
         } else {
             HashMap<Character, Object> result = new HashMap<Character, Object>();
             int twidth = width();
             for (int i = 0; i < twidth; i++) {
                 char it = item();
                 result.put(it, generate(depth - 1));
             }
             return result;
         }
     }
 
     private void count_accum(HashMap<Character, Integer> accum, Object tree) {
         if (tree instanceof Character) {
             accum.put((Character)tree, accum.get(tree) + 1);
         } else {
             HashMap<Character, Object> map = (HashMap<Character, Object>)tree;
             for (Character c: map.keySet()) {
                 accum.put(c, accum.get(c) + 1);
                 count_accum(accum, map.get(c));
             }
         }
     }
 
     private void print(Object tree) {
         print(tree, "");
     }
 
     private void print(Object tree, String prefix) {
         if (tree instanceof Character) {
             System.out.println(tree);
         } else {
             System.out.println();
             HashMap<Character, Object> map = (HashMap<Character, Object>)tree;
             for (Character c: map.keySet()) {
                 System.out.print(prefix + c + " -> ");
                 print(map.get(c), prefix + "\t");
             }
         }
     }
 
     private HashMap<Character, Integer> count(Object tree) {
         HashMap<Character, Integer> result = new HashMap<Character, Integer>();
         for (char c = MIN_ITEM; c <= MAX_ITEM; c++) {
             result.put(c, 0);
         }
         count_accum(result, tree);
         return result;
     }
 
     public void run(int depth) {
         Object tree = generate(depth);
         if (depth < 4) {
             print(tree);
         }
         HashMap<Character, Integer> counts = count(tree);
         for (char c: counts.keySet()) {
             System.out.println("" + c + '\t' + counts.get(c));
         }
     }
 
     public static void main(String[] arguments) {
         int depth = Integer.parseInt(arguments[0]);
         Run run = new Run();
         run.run(depth);
         return;
     }
 }
