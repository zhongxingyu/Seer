 import quizlet.Quizlet;
 import quizlet.Set;
 import quizlet.Term;
 
 public class Main {
 
     public static void main(String[] args) {
         new Main().downloadSet();
     }
 
     public void downloadSet() {


         Quizlet quizlet = new Quizlet("c29cpp29Gf");
        Set set = quizlet.getSet(417);
         for (Term term : set.terms) {
             System.out.println(term.term);
         }
 
 
     }
 
 }
