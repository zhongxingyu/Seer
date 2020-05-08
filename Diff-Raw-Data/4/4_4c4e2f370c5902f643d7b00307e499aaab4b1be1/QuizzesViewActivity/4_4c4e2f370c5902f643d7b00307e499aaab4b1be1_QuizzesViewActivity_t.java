 package com.huskysoft.eduki;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.huskysoft.eduki.data.Quiz;
 import com.huskysoft.eduki.data.QuizContent;
 import com.huskysoft.eduki.data.QuizContent.Problem;
 import com.huskysoft.eduki.data.QuizQuery;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 /**
  * @author Rafael Vertido Class QuizzesViewActivity allows users to take 
  * multiple choice quizzes
  */
 @SuppressLint("DefaultLocale")
 public class QuizzesViewActivity extends Activity implements TaskComplete {
 
     /**
      * private global variables that are initialized after requests to database are
      * completed 
      * 
      * questions - Questions that will be asked 
      * choices - Choices associated with each question 
      * answers - Answers associated with each question 
      * quiz - Quiz chosen by user 
      * quizContent - Structure that holds details of the quiz
      */
     private List<String> questions;
     private List<List<String>> choices;
     private List<String> answers;
     private Quiz quiz;
     private QuizContent quizContent;
     private int course_id;
 
     /**
      * Static variables to keep track of quiz state 
      * questionsAnswered - Total number answered 
      * questionsCorrect  - Number answered correctly
      */
     private static int questionsAnswered = 0;
     private static int questionsCorrect = 0;
 
     /** Constant required to match ASCII 'a' to index 0 */
     private static final int ASCII_NUM = 97;
     
     /** Save 'this' as a Context for popup messages */
     private final Context context = this;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             String quiz_title = extras.getString("quiz_title");
             int quiz_id = extras.getInt("quiz_id");
             course_id = extras.getInt("course_id");
             quiz = new Quiz(quiz_id, course_id, quiz_title);
             QuizQuery.getAllQuestions(this, quiz_id);
         }
         setContentView(R.layout.loading_screen);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public void taskComplete(String data) {
         // Get the list of questions, and set the title
         quizContent = QuizQuery.parseQuestionsList(data);
         List<Problem> problemList = quizContent.getProblems();
 
         // Check if there are questions for this quiz, if so then begin the
         // quiz.
         // Otherwise then display a message that there is no quiz content
         if (problemList.size() == 0) {
             setContentView(R.layout.activity_no_list_found);
             TextView contentView = (TextView) findViewById(R.id.noListText);
             contentView.setText("No questions found for this quiz.");
         } else {
             questions = new ArrayList<String>();
             answers = new ArrayList<String>();
             choices = new ArrayList<List<String>>();
 
             parseQuizContent(problemList);
             setContentView(R.layout.activity_quizzesview);
             ((TextView) findViewById(R.id.title)).setText(quiz.getTitle());
             updateQuiz();
         }
     }
 
     /**
      * Parse the problemList as retrieved as a List of Problem objects. Split
      * each question's potential answer choices delimited by '\n'. Populates the
      * questions, choices, and answers lists required for displaying the quiz
      * content to the user dynamically.
      * 
      * @param problemList List of problems to parse
      */
     public void parseQuizContent(List<Problem> problemList) {
         for (Problem p : problemList) {
             String[] questionContent = p.getQuestion().split("\\n");
             questions.add(questionContent[0]);
             answers.add(p.getAnswer());
 
             List<String> answerChoices = new ArrayList<String>();
             for (int i = 1; i < questionContent.length; i++) {
                 answerChoices.add(questionContent[i]);
             }
             choices.add(answerChoices);
         }
     }
 
     /**
      * Updates the state of the quiz. Manages what is displayed to the user,
      * shows a new problem if there are any unanswered, a message noting there
      * are no problems associated with the quiz if there are none, or displays
      * the quiz results if the user has completed the quiz.
      */
     private void updateQuiz() {
         if (questionsAnswered == questions.size()) { // Finished the quiz
            setContentView(R.layout.activity_quizzesresult);
             ((TextView) findViewById(R.id.title)).setText(R.string.quizResults);
             displayQuizResults();
             addEventListeners();
         } else { // Show the question
             ((TextView) findViewById(R.id.questionNumber)).setText((questionsAnswered + 1) + ".");
             TextView contentView = (TextView) findViewById(R.id.questionText);
             contentView.setText(questions.get(questionsAnswered));
             generateAnswers(questionsAnswered);
         }
     }
 
     /**
      * Dynamically generate the proper choices for the given question that the user 
      * is currently answering.
      * 
      * @param questionNumber Question # that the user is at
      */
     private void generateAnswers(int questionNumber) {
         final RadioGroup answersRadioGroup = (RadioGroup) findViewById(R.id.answerGroup);
         answersRadioGroup.removeAllViews(); // Refresh the display
         answersRadioGroup.clearCheck(); // Refresh the selection
         
         // Get the choices and display them as radio button choices
         List<String> currentChoices = choices.get(questionsAnswered);
         RadioButton[] rb = new RadioButton[currentChoices.size()];
         for (int i = 0; i < currentChoices.size(); i++) {
             rb[i] = new RadioButton(this);
             rb[i].setText(currentChoices.get(i));
             rb[i].setId(i);
             rb[i].setTextColor(Color.parseColor(getResources().getString(R.color.content)));
             answersRadioGroup.addView(rb[i]);
         }
 
         // Set a listener for the submit button
         Button submitButton = (Button) findViewById(R.id.submitButton);
         submitButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 // Get the user's selected answer
                 int answerButtonId = answersRadioGroup.getCheckedRadioButtonId();
                 View radioButton = answersRadioGroup.findViewById(answerButtonId);
                 int answerIndex = answersRadioGroup.indexOfChild(radioButton);
 
                 // Make sure the user has chosen an answer, otherwise display message to do so
                 if (answerIndex >= 0) {
                     // Get the answer from database data
                     char correctAnswer = answers.get(questionsAnswered).toLowerCase().charAt(0);
                     int correctAnswerIndex = ((int) correctAnswer) - ASCII_NUM;
     
                     // Check if the user got it correct
                     if (answerIndex == correctAnswerIndex) {
                         questionsCorrect++;
                     }
     
                     // Update quiz state
                     questionsAnswered++;
                     updateQuiz();
                 } else {
                     // Show a message that the user is required to select an answer before submitting
                     AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                     alertDialogBuilder.setTitle(quiz.getTitle());
                     alertDialogBuilder
                         .setMessage(R.string.noAnswerChosen)
                         .setCancelable(false)
                         .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 // Do nothing, remove message
                             }
                         });
                     AlertDialog alertDialog = alertDialogBuilder.create();
                     alertDialog.show();
                 }
             }
         });
     }
     
     private void displayQuizResults() {
         TextView contentView = (TextView) findViewById(R.id.quizResultText);
         double percentage = ((1.0 * questionsCorrect) / questionsAnswered) * 100; // Get % score
         contentView.setText("Quiz Finished!\n" + "You got "
                             + questionsCorrect + "/" + questionsAnswered + " questions correct.\n"
                             + "Your score is " + (int) percentage + "%!");
     }
     
     private void addEventListeners() {
         ImageButton retakeButton = (ImageButton) findViewById(R.id.retakeButton);
         ImageButton coursesButton = (ImageButton) findViewById(R.id.coursesButton);
         
         // On mouse click event listener for the re-take button
         retakeButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 //TODO: ADD POSTING OF RESULTS
                 // Post results, then restart the quiz
                 questionsAnswered = 0;
                 questionsCorrect = 0;
                 setContentView(R.layout.activity_quizzesview);
                 ((TextView) findViewById(R.id.title)).setText(quiz.getTitle());
                 updateQuiz();
             }
         });
         
         // On mouse click event listener for the courses logo
         coursesButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(context, CoursesListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
             }
         });
     }
 }
