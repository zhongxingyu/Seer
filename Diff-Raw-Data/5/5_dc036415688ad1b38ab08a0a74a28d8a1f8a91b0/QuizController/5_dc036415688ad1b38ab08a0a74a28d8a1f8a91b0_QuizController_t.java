 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.web;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 import no.hials.muldvarpweb.domain.Alternative;
 import no.hials.muldvarpweb.domain.Question;
 import no.hials.muldvarpweb.domain.Quiz;
 import no.hials.muldvarpweb.service.QuizService;
 
 /**
  * This is the controller for the QuizService.
  * 
  * @author johan
  */
 @Named
 @SessionScoped
 public class QuizController implements Serializable{
     
     @Inject QuizService service;
     
     List<Quiz> quizList;
     Quiz newQuiz;
     Quiz selected;
     Quiz quizForDeletion;
     Question selectedQuestion;
     Question newQuestion;
     List<Question> newQuestionList;
     List<Alternative> newAlternativeList;
     String filterString;
     
     
     /**
      * This function returns the selectedQuiz variable.
      * 
      * @return The selected Quiz
      */
     public Quiz getSelected(){        
         //Check if the selectedQuiz variable is null, and set new Quiz if it is
         if (selected == null) {            
             selected = new Quiz();
         }                
         return selected;
     }
     
     public void setSelected(Quiz selected) {
         if(selected == null) {
            this.selected = getQuiz();
         }
         this.selected = selected;
     }
 
     public Quiz getQuiz() {
         if(newQuiz == null){
             newQuiz = new Quiz();
         }
         return newQuiz;
     }
 
     public Question getNewQuestion(){
         if(newQuestion == null){
             newQuestion = new Question();
         }
         return newQuestion;
     }
     
     public void removeQuestion(Question q){
         newQuiz.removeQuestion(q);
     }
     
     public void addQuestion(){
         if(newQuiz == null){
             newQuiz = new Quiz();
         } if(newQuestion == null){
             newQuestion = new Question();
         }
         newQuiz.addQuestion(newQuestion);
         newQuestion = null;
     }
     
     public void addQuestionToSelectedQuiz(){
         if(newQuestion == null){
             newQuestion = new Question();
         }
         selected.addQuestion(newQuestion);
         newQuestion = null;
     }
 
     public void setQuiz(Quiz newQuiz) {
         this.newQuiz = newQuiz;
     }
     
     public String editQuiz() {
         if(selected != null) {
             service.editQuiz(selected);
         }
        return "listQuiz?faces-redirect=true";
     }
     
     public String removeQuiz(Quiz q) {
 //        if(selected != null ) {
             service.removeQuiz(q);
 //        }
         return "listQuiz?faces-redirect=true";
     }
     
     /**
      * This function returns a List of Quizzes from the injected QuizService.
      * 
      * @return List of Quizzes
      */
     public List<Quiz> getQuizzes() {
         quizList = service.findQuizzes();
         return quizList;
     }
     
     /**
      * This function returns a List of Videos from VideoService based on a
      * global variable in the VideoController class.
      * 
      * @return List of Video
      */
     public List<Quiz> getNameFilteredQuizzes(){        
         //Make sure the filterString is not null but has a value, even if it's empty
         if(filterString == null){
             filterString = "";
         }        
         return service.findQuizzesByName(filterString);
     }
 
     public void setQuiz(List<Quiz> quizList) {
         this.quizList = quizList;
     }
     
     /**
      * This function makes a call to the QuizService instantiation and adds the supplied Quiz.
      * 
      * @param newQuiz The Quiz to be added.
      * @return 
      */
     public Quiz addQuiz() {
         service.addQuiz(newQuiz);
         clearData();
         return newQuiz;
     }
     
     public void updateQuiz(){
         service.editQuiz(selected);
         clearData();
     }
     
     public void addInfo(int i) {  
         switch(i) {
             case 1:
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "INFO: ", "Changes saved"));
                 break;
             case 2:
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "INFO: ", "Quiz deleted"));
                 break;
             case 3:
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "INFO: ", "New revision created"));
                 break;
          }
     }
     
     public void setupQuizForDeletion(Quiz quiz){
         this.quizForDeletion = quiz;
     }
     
     public Quiz getQuizForDeletion(){
         return quizForDeletion;
     }
     
     public void deleteQuizForDeletion(){        
         if(quizForDeletion != null){
             service.removeQuiz(quizForDeletion);
         }
     }
     
     public void setSelectedQuestion(Question q){
         selectedQuestion = q;
     }
     
     public List<Question> getQuestions(){
         return newQuestionList;
     }
     
     public List<Alternative> getAlternatives(){
         return newAlternativeList;
     }
     
     public String makeTestData(){
         service.makeTestData();
         return "listQuiz?faces-redirect=true";
     }
     
     public String getFilterString() {
         return filterString;
     }
 
     public void setFilterString(String filterString) {
         this.filterString = filterString;
     }
     
     
     public void clearData(){
         newQuiz = null;
         newQuestion = null;
         selected = null;
     }
 }
