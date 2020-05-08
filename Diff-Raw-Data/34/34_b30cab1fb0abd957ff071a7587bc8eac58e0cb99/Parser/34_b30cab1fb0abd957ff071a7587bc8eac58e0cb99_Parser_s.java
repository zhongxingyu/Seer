package scheduler;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 public class Parser {
     private BufferedReader br; 
     private int [] cache; /* stores the id and priority read 
                              from input (for push and change operations) */
     
     public Parser(String filename) throws FileNotFoundException {
         try {
             this.br = new BufferedReader(new FileReader(filename));
         } catch (FileNotFoundException e) {
             System.out.println(e.getMessage());
         }
         
         this.cache = new int [2];
         this.resetCache();
     }
     
     public BufferedReader getBr() {
         return this.br;
     }
     
     private void resetCache() {
         this.cache[0] = -1;
         this.cache[1] = -1;
     }
     
     private void setCache(int id, int priority) {
         this.cache[0] = id;
         this.cache[1] = priority;
     }
     
     public int [] getCache() {
         return this.cache;
     }
 
     public void writeCache() {
         System.out.println("Cache[" + this.cache[0] + "][" + this.cache[1] + "]");
     }
     
     public int getNextOrder(String s) {
        /* Gets the next line (order) from the input file, returns 1 if it's jobpush,
         * 2 if it's jobpop, 3 if it's jobchange.
         * If its either push or change id and priority are stored in this.cache[]
         */
         StringTokenizer st = new StringTokenizer(s);
             
         String [] tmp = new String [3];
         int i = 0;
         while (st.hasMoreTokens()) {
             tmp[i] = st.nextToken();
             i++;
         }
             
         if (tmp[0].equals("jobpush")) {
             //System.out.println("Push: " + tmp[1] + ", " + tmp[2]);
             this.setCache(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
             return 1;
         } else if (tmp[0].equals("jobpop")) {
             //System.out.println("Pop");
             this.resetCache();
             return 2;
         } else if (tmp[0].equals("jobchange")) {
             //System.out.println("Change: " + tmp[1] + ", " + tmp[2]);
             this.setCache(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
             return 3;
         }
         return 0;    
     }
     
     public static void main(String [] args) throws FileNotFoundException, IOException {
         Parser p = new Parser("test.lol");
         String s;
                 
         while ((s = p.br.readLine()) != null && !s.equals(""))
             p.getNextOrder(s);
         
         p.br.close();
     }
 }
