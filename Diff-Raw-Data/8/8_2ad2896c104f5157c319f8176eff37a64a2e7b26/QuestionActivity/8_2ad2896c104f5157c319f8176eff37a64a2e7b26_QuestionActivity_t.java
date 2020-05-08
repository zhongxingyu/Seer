 package net.incredibles.brtaquiz.activity;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import com.google.inject.Inject;
 import net.incredibles.brtaquiz.R;
 import net.incredibles.brtaquiz.controller.QuestionController;
 import net.incredibles.brtaquiz.domain.Answer;
 import net.incredibles.brtaquiz.domain.Question;
 import net.incredibles.brtaquiz.domain.Sign;
 import net.incredibles.brtaquiz.service.TimerServiceManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectResource;
 import roboguice.inject.InjectView;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author sharafat
  * @Created 2/19/12 10:36 PM
  */
 public class QuestionActivity extends RoboActivity {
     private static final Logger LOG = LoggerFactory.getLogger(QuestionActivity.class);
 
     @InjectView(R.id.question_set_details_text_view)
     private TextView questionSetDetailsTextView;
     @InjectView(R.id.time_remaining_text_view)
     private TextView timeRemainingTextView;
     @InjectView(R.id.question_text_view)
     private TextView questionTextView;
     @InjectView(R.id.sign_image)
     private ImageView signImageView;
     @InjectView(R.id.answers_radio_group)
     private RadioGroup answersRadioGroup;
     @InjectView(R.id.prev_btn)
     private Button prevBtn;
     @InjectView(R.id.next_btn)
     private Button nextBtn;
 
     @InjectResource(R.string.what_is_the_following_sign)
     private String questionText;
     @InjectResource(R.string.questions)
     private String questions;
     @InjectResource(R.string.unanswered)
     private String unanswered;
 
     @Inject
     private LayoutInflater layoutInflater;
     @Inject
     private QuestionController questionController;
 
     private JumpToQuestionDialog jumpToQuestionDialog;
     private RadioButton previouslySelectedRadioButton;
     private Handler remainingTimeUpdateHandler;
     private boolean answerMarkedStatusListRefreshingNeeded;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.question);
         jumpToQuestionDialog = new JumpToQuestionDialog();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         remainingTimeUpdateHandler = new RemainingTimeUpdateHandler(this, timeRemainingTextView);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         TimerServiceManager.registerRemainingTimeUpdateHandler(remainingTimeUpdateHandler);
         prepareUI();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.question_options_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_jump_to_question:
                 if (answerMarkedStatusListRefreshingNeeded) {
                     jumpToQuestionDialog.refreshAnswerMarkedStatusList();
                 }
                 answerMarkedStatusListRefreshingNeeded = false;
                 showDialog(Dialogs.ID_JUMP_TO_QUESTION_DIALOG);
                 return true;
             case R.id.menu_question_set:
                 startActivity(new Intent(QuestionActivity.this, QuestionSetListActivity.class));
                 return true;
             case R.id.menu_finish_test:
                 if (questionController.isAllQuestionsAnswered()) {
                     startActivity(new Intent(this, ResultActivity.class));
                     finish();
                 } else {
                     showDialog(Dialogs.ID_FINISHING_WITH_INCOMPLETE_ANSWERS_CONFIRMATION_DIALOG);
                 }
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case Dialogs.ID_REVIEW_OR_SUBMIT_CONFIRMATION_DIALOG:
                 return Dialogs.createReviewOrSubmitConfirmationDialog(this, questionController);
             case Dialogs.ID_TIME_UP_DIALOG:
                 return Dialogs.createTimeUpDialog(this);
             case Dialogs.ID_FINISHING_WITH_INCOMPLETE_ANSWERS_CONFIRMATION_DIALOG:
                 return Dialogs.createFinishingWithIncompleteAnswersConfirmationDialog(this);
             case Dialogs.ID_JUMP_TO_QUESTION_DIALOG:
                 return jumpToQuestionDialog;
             default:
                 return null;
         }
     }
 
     private void prepareUI() {
         previouslySelectedRadioButton = null;
 
         try {
             Question question = questionController.getQuestion();
             displayQuestionSetDetails(question);
             displayQuestionWithSerial(question);
             displaySignImage(question);
             displayAnswers(question);
 
             preparePreviousButton();
             prepareNextButton();
         } catch (NullPointerException ignore) {
             // User has pressed back button from Result activity and came here. Don't worry, he'll be shown the "Time's Up" dialog shortly... :)
             // Update: Back button from Result activity has been handled to do nothing. So, program flow shouldn't come here now.
         }
     }
 
     private void displayQuestionSetDetails(Question question) {
         questionSetDetailsTextView.setText(question.getSignSet().getName()
                 + " (" + questionController.getQuestionCountInCurrentQuestionSet() + " " + questions + ")");
     }
 
     private void displayQuestionWithSerial(Question question) {
         questionTextView.setText(question.getSerialNoInQuestionSet() + ". " + questionText);
     }
 
     private void displaySignImage(Question question) {
         byte[] signImage = question.getSign().getImage();
         signImageView.setImageBitmap(BitmapFactory.decodeByteArray(signImage, 0, signImage.length));
     }
 
     private void displayAnswers(Question question) {
         answersRadioGroup.removeAllViews();
 
         List<Answer> answers = question.getAnswers();
         for (Answer answer : answers) {
             Sign sign = answer.getAnswer();
             addRadioButton(sign.getId(), sign.getDescription(), sign.equals(question.getMarkedSign()));
         }
     }
 
     private void addRadioButton(int id, String label, boolean checked) {
         RadioButton radioButton = new RadioButton(this);
         radioButton.setChecked(checked);
         radioButton.setId(id);
         radioButton.setText(label);
         radioButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 RadioButton clickedRadioButton = (RadioButton) view;
 
                 boolean selectedAnswerReselected = clickedRadioButton == previouslySelectedRadioButton;
                 if (selectedAnswerReselected) {
                     answersRadioGroup.clearCheck();
                     previouslySelectedRadioButton = null;
                     questionController.unMarkAnswer();
                     answerMarkedStatusListRefreshingNeeded = true;
                 } else {
                     questionController.markAnswer(answersRadioGroup.getCheckedRadioButtonId(),
                             previouslySelectedRadioButton != null);
                     if (!questionController.isUserReviewing() && questionController.isAllQuestionsAnswered()) {
                        nextBtn.setText(R.string.finish);
                        nextBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDialog(Dialogs.ID_REVIEW_OR_SUBMIT_CONFIRMATION_DIALOG);
                            }
                        });
                     }
 
                     if (previouslySelectedRadioButton == null) {
                         answerMarkedStatusListRefreshingNeeded = true;
                     }
 
                     previouslySelectedRadioButton = clickedRadioButton;
                 }
             }
         });
 
         answersRadioGroup.addView(radioButton);
         if (checked) {
             previouslySelectedRadioButton = radioButton;
         }
     }
 
     private void preparePreviousButton() {
         prevBtn.setVisibility(View.VISIBLE);
         prevBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 questionController.previousQuestion();
                 prepareUI();
             }
         });
 
         if (questionController.isFirstQuestion()) {
             prevBtn.setVisibility(View.INVISIBLE);
         }
     }
 
     private void prepareNextButton() {
         int buttonLabelTextResourceId = R.string.next;
         View.OnClickListener onClickListener = new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 questionController.nextQuestion();
                 prepareUI();
             }
         };
 
         if (questionController.isLastQuestionInCurrentSet()) {
             buttonLabelTextResourceId = R.string.choose_next_set;
             onClickListener = new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     startActivity(new Intent(QuestionActivity.this, QuestionSetListActivity.class));
                 }
             };
         }
 
         nextBtn.setText(buttonLabelTextResourceId);
         nextBtn.setOnClickListener(onClickListener);
     }
 
 
     private class JumpToQuestionDialog extends Dialog {
         private List<String> jumpToQuestionListItems = new ArrayList<String>();
         private ArrayAdapter<String> listAdapter;
 
         protected JumpToQuestionDialog() {
             super(QuestionActivity.this);
 
             refreshJumpToQuestionListItems();
 
             setTitle(R.string.jump_to_question_dialog_title);
             setCancelable(true);
             setContentView(prepareContentView());
         }
 
         private void refreshJumpToQuestionListItems() {
             jumpToQuestionListItems.clear();
 
             Map<Integer, Boolean> questions = questionController.getQuestionsWithMarkedStatusInCurrentQuestionSet();
 
             int questionSerial = 1;
             for (Integer questionId : questions.keySet()) {
                 Boolean isMarked = questions.get(questionId);
                 LOG.debug("Questions with marked status in current question set - questionId: {}, isMarked: {}"
                         , questionId, isMarked);
                 jumpToQuestionListItems.add(questionSerial++ + "   " + (isMarked ? "" : "(" + unanswered + ")"));
             }
         }
 
         private View prepareContentView() {
             ListView jumpToQuestionListView = new ListView(QuestionActivity.this);
 
             listAdapter = getListAdapter();
             jumpToQuestionListView.setAdapter(listAdapter);
             jumpToQuestionListView.setOnItemClickListener(getListOnItemClickListener());
 
             return jumpToQuestionListView;
         }
 
         private ArrayAdapter<String> getListAdapter() {
             return new ArrayAdapter<String>(QuestionActivity.this,
                     android.R.layout.simple_list_item_1, android.R.id.text1, jumpToQuestionListItems);
         }
 
         private AdapterView.OnItemClickListener getListOnItemClickListener() {
             return new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                     questionController.jumpToQuestion(position + 1);
                     dismissDialog(Dialogs.ID_JUMP_TO_QUESTION_DIALOG);
                     prepareUI();
                 }
             };
         }
 
         @SuppressWarnings("unchecked")
         public void refreshAnswerMarkedStatusList() {
             refreshJumpToQuestionListItems();
             listAdapter.notifyDataSetChanged();
         }
     }
 }
