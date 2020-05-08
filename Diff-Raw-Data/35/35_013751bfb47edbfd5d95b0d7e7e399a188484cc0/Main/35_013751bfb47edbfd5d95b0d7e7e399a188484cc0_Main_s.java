 import quizlet.Quizlet;
import quizlet.SearchResult;
 import quizlet.Set;
 import quizlet.Term;
 
 public class Main {
 
     public static void main(String[] args) {
         new Main().downloadSet();
     }
 
     public void downloadSet() {
         Quizlet quizlet = new Quizlet("c29cpp29Gf");
        SearchResult result = quizlet.search("q=spanish");
        int id = result.sets.get(0).id;
        Set set = quizlet.getSet(id);
         for (Term term : set.terms) {
             System.out.println(term.term);
         }
 
 
     }
 
 }
