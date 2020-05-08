 package epfl.sweng.tasks;
 
 
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import epfl.sweng.globals.Globals;
 import epfl.sweng.quizquestions.QuizQuestion;
 import epfl.sweng.tasks.interfaces.IQuestionPersonalRatingReloadedCallback;
 import epfl.sweng.tasks.interfaces.IQuestionRatingReloadedCallback;
 import epfl.sweng.tasks.interfaces.IQuizQuestionVerdictSubmittedCallback;
 import epfl.sweng.tasks.interfaces.IQuizServerCallback;
 
 /**
  * QuizServerTask realization that fetches the rating of a question
  */
 public class SubmitQuestionVerdict extends QuizServerTask {
     
 	private QuizQuestion mQuestion;
 	
 	/**
 	 * Constructor
 	 * @param callback interface defining the methods to be called
 	 * for the outcomes of success (onSuccess) or error (onError)
 	 */
 	public SubmitQuestionVerdict(final IQuizQuestionVerdictSubmittedCallback callback, final QuizQuestion question) {
 		super(new IQuizServerCallback() {
 			
 			@Override
 			public void onSuccess(final JSONTokener response) {
 				if (getLastStatusCode() != Globals.STATUSCODE_OK && getLastStatusCode() != Globals.STATUSCODE_CREATED) {
 					onError();
 				} else {
 					
 					try {
 						JSONObject responseJSON = (JSONObject) response.nextValue();
 						
						if (responseJSON.getString("verdict") != question.getVerdict()) {
							onError();
 						} else {
 							
 							callback.onSubmitSuccess(question);
 							new ReloadPersonalRating(new IQuestionPersonalRatingReloadedCallback() {
 								
 								@Override
 								public void onReloadedSuccess(QuizQuestion question) {
 									callback.onReloadedSuccess(question);
 									new ReloadQuestionRating(new IQuestionRatingReloadedCallback() {
 										
 										@Override
 										public void onReloadedSuccess(QuizQuestion question) {
 											callback.onReloadedSuccess(question);
 										}
 										
 										@Override
 										public void onError() {
 											callback.onReloadedError();
 										}
 									}, question).execute();
 								}
 								
 								@Override
 								public void onError() {
 									callback.onReloadedError();
 								}
 							}, question).execute();
 							
 						}
 					} catch (ClassCastException e) {
 						onError();
 					} catch (JSONException e) {
 						onError();
 					}
 					
 				}
 			}
 			
 			@Override
 			public void onError() {
 				callback.onSubmitError();
 			}
 		});
 		mQuestion = question;
 	}
 	
 
 	/**
 	 * Method submitting the new question verdict
 	 * 
 	 * @param question the Question containing the new verdict to be submitted
 	 */
 	@Override
 	protected JSONTokener doInBackground(Object... args) {
 		 
 		HttpPost post = new HttpPost(Globals.QUESTION_BY_ID_URL + mQuestion.getId() + "/rating");
 		try {
 			JSONObject verdictJson = new JSONObject();
 			verdictJson.put("verdict", mQuestion.getVerdict());
 			post.setEntity(new StringEntity(verdictJson.toString()));
 		} catch (UnsupportedEncodingException e) {
 			cancel(false);
 		} catch (JSONException e) {
 			cancel(false);
 		}
 		post.setHeader("Content-type", "application/json");
 		
 		return handleQuizServerRequest(post);
 	}
 }
