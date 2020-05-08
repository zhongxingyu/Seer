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
 import no.hials.muldvarpweb.domain.Quiz.QuizType;
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
     Alternative alternative;
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
     
     /**
      * This method returns an Array of strings which represent the values of the QuizType, an enum in the Quiz class.
      * 
      * @return Array<QuizType>
      */
     public QuizType[] getQuizTypes(){
         return Quiz.QuizType.values();
     }
     
     /**
      * Switches the isCorrect boolean.
      * @param a 
      */
     public void switchBoolean(Alternative a){
         a.setIsCorrect(!a.isIsCorrect());
     }
     
     public void switchSelectedBoolean(){
         alternative.setIsCorrect(!alternative.isIsCorrect());
     }
 
     public Alternative getAlternative() {
         if(alternative == null){
             alternative = new Alternative();
         }
         return alternative;
     }
 
     public Question getSelectedQuestion() {
         return selectedQuestion;
     }
     
     public void setSelected(Quiz selected) {
         if(selected == null) {
             selected = getQuiz();
         }
         this.selected = selected;
     }
 
     public Quiz getQuiz() {
         if(newQuiz == null){
             newQuiz = new Quiz();
            selected = newQuiz;
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
         return "listvQuiz";
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
         clearQuizData();
         return newQuiz;
     }
     
     public void updateQuiz(){
         service.editQuiz(selected);
     }
     
     public void updateAndClearQuiz(){
         service.editQuiz(selected);
         clearQuizData();
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
     
     public String setSelectedQuestion(Question q){
         selectedQuestion = q;
         editQuiz();
         return "editQuestion";
     }
     
     public String makeTestData(){
 //        service.makeTestData();
         return "listQuiz?faces-redirect=true";
     }
     
     public String getFilterString() {
         return filterString;
     }
 
     public void setFilterString(String filterString) {
         this.filterString = filterString;
     }
     
     public void clearQuestionData(){
         alternative = null;
         selectedQuestion = null;
     }
     
     public void clearQuestionDataAndSave(){
 //        setQuestionMode(selectedQuestion);
 //        selected.updateQuestion(selectedQuestion);
         service.editQuiz(selected);
         alternative = null;
         selectedQuestion = null;
     }
     
     public void clearQuizData(){
         newQuiz = null;
         newQuestion = null;
         selected = null;
     }
     
     public void addAlternative(){
         selected.addAlternative(selectedQuestion, alternative);
         setQuestionMode(selectedQuestion);
         alternative = null;
     }
     
     public void removeAlternative(Alternative a){
         selected.removeAlternative(selectedQuestion, a);
     }
     
     public boolean setQuestionMode(Question q){
         int i = 0;
         for(Alternative a : q.getAlternatives()){
             if(a.isIsCorrect() == true){
                 i++;
             }
         }
         if(i<1){return false;}
         else if(i==1){
             selectedQuestion.setQuestionType("single");
             return true;
         }else{
             selectedQuestion.setQuestionType("multiple");
             return true;
         }
     }
 }
