 package fr.xebia.usiquizz.core.game;
 
 public interface QuestionLongpollingCallback {
     void startSendAll();
 
     void initNewQuestion(byte currentQuestionIndex);

    void reset();
 }
 
