 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import no.hials.muldvarpweb.domain.Alternative;
 import no.hials.muldvarpweb.domain.Question;
 import no.hials.muldvarpweb.domain.Quiz;
 
 /**
  * Service class for the Quiz entities.
  * 
  * @author johan
  */
 @Stateless
 @Path("quiz")
 public class QuizService {
     
     @PersistenceContext
     EntityManager em;
     
     /**
      * This function merges and persists a Programme item .
      * 
      * @param newQuiz The Programme to be added.
      */
     public void addQuiz(Quiz newQuiz){
         newQuiz = em.merge(newQuiz);
         em.persist(newQuiz);
     }
     
     /**
      * Function that returns all Quizzes 
      * 
      * @return List<Quiz> List of Quiz items
      */
     @GET
     @Produces({MediaType.APPLICATION_JSON})
     public List<Quiz> findQuizzes() {
         List retval =em.createQuery("SELECT q from Quiz q", Quiz.class).getResultList();
         return retval;
     }
         
     /**
      * Function that returns all Quizzes corresponding to one ID.
      * 
      * @param id 
      * @return Quiz
      */
     @GET
     @Path("{id}")
     @Produces({MediaType.APPLICATION_JSON})
     public Quiz getQuizzes(@PathParam("id") Integer id) {        
         TypedQuery<Quiz> q = em.createQuery("Select q from Quiz q where q.id = :id", Quiz.class);
         q.setParameter("id", id);        
         return q.getSingleResult();
     }
 
     public void editQuiz(Quiz selected) {
         selected = em.merge(selected);
         em.persist(selected);
     }
 
     public void removeQuiz(Quiz q) {
         q = em.merge(q);
         em.remove(q);
     }
     
     /**
      * This method returns a List of Quiz objects based on the name of the Quiz.
      * @param name
      * @return 
      */
     public List<Quiz> findQuizzesByName(String name){        
         TypedQuery<Quiz> query = em.createQuery("SELECT q from Quiz q where q.name LIKE :name", Quiz.class);
         query.setParameter("name", "%" + name + "%");        
         return query.getResultList();
     }
     
     public void makeTestData(){
         ArrayList alternatives = new ArrayList();
         alternatives.add(new Alternative("5", false));
         alternatives.add(new Alternative("4x²", false));
         alternatives.add(new Alternative("4", true));
         alternatives.add(new Alternative("0", false));
         
         ArrayList questions = new ArrayList();
         Question questionTest = new Question("Den deriverte av 4x er:", alternatives, "Single");
         questionTest.setShuffleAlternatives(true);
         questions.add(questionTest);
         
         alternatives = new ArrayList();
         alternatives.add(new Alternative("4", false));
         alternatives.add(new Alternative("9", true));
         alternatives.add(new Alternative("8", false));
         alternatives.add(new Alternative("7", false));
         alternatives.add(new Alternative("0", false));
         
         questions.add(new Question("Kvadratroten av 81 er:", alternatives, "Single"));
         
         alternatives = new ArrayList();
         alternatives.add(new Alternative("2/5", false));
         alternatives.add(new Alternative("1/5", false));
         alternatives.add(new Alternative("1/25", true));
         alternatives.add(new Alternative("4/100", true));
         alternatives.add(new Alternative("0", false));
         alternatives.add(new Alternative("0,04", true));
         
         questions.add(new Question("(1/5) x (1/5) =", alternatives, "Multiple"));
         Quiz quiz = new Quiz("Mattequiz 1", questions, "feedback");
         quiz.setDescription("Dette er en matte-quiz med tre spørsmål.");
         quiz.setShuffleQuestions(true);
         addQuiz(quiz);
         //        Alternative alt = new Alternative();
         //        alt.setName("hurr");
         //        List<Alternative> alts = new ArrayList<Alternative>();
         //        alts.add(alt);
         //        Question question = new Question();
         //        question.setName("dur");
         //        question.setAlternatives(alts);
         //        List<Question> questions = new ArrayList<Question>();
         //        question.addAlternative(alt);
         //        Quiz quiz = new Quiz();
         //        quiz.setName("test");
         //        quiz.setDescription("teeeeststst");
         //        quiz.setQuestions(questions);
         //        addQuiz(quiz);
     }
 }
