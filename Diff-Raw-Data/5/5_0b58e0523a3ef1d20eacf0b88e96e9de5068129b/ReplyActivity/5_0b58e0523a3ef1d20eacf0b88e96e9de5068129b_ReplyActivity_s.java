 package activity.newsfeed;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.client.ClientProtocolException;
 
 import util.AudioUtil;
 import util.MultipartEntityUtil;
 import activity.buy.BuyActivity;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 
 import com.ebay.ebayfriend.R;
 
 public class ReplyActivity extends Activity {
 	private NewsFeedItem newsFeedItem;
 	private static final int TEXT_INPUT = 0;
 	private static final int AUDIO_INPUT = 1;
 	private int barStatus = TEXT_INPUT;
 	private String recordName;
 	private AudioUtil audioUtility;
 	private ReplyAdapter replyAdapter;
 	private ListView replyList;
 	private ImageButton buyButton;
 	private Activity activity;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		activity = this;
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.reply);
 		recordName = this.getFilesDir().toString() + File.separator
 				+ "MyRecord";
 		audioUtility = new AudioUtil();
 		this.newsFeedItem = (NewsFeedItem) getIntent().getSerializableExtra(
 				"currentNewsFeed");
 		replyList = (ListView) findViewById(R.id.replyList);
 		replyAdapter = new ReplyAdapter(this, newsFeedItem, replyList);
 		replyList.setAdapter(replyAdapter);
 		buyButton = (ImageButton) findViewById(R.id.buy);
 
 		// Set input bar listener
 		ImageButton swapButton = (ImageButton) findViewById(R.id.swapInputForm);
 		ImageButton sendButton = (ImageButton) findViewById(R.id.sendReplyButton);
 		final EditText textInput = (EditText) findViewById(R.id.textInput);
 		final Button audioInput = (Button) findViewById(R.id.audioInput);
 
 		swapButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View view) {
 				if (barStatus == ReplyActivity.TEXT_INPUT) {
 					textInput.setVisibility(View.GONE);
 					audioInput.setVisibility(View.VISIBLE);
 					barStatus = AUDIO_INPUT;
 					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
 				} else if (barStatus == ReplyActivity.AUDIO_INPUT) {
 					textInput.setVisibility(View.VISIBLE);
 					audioInput.setVisibility(View.GONE);
 					barStatus = TEXT_INPUT;
 				}
 			}
 		});
 		audioInput.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					audioUtility.recordVoice(recordName);
 				} else if (event.getAction() == MotionEvent.ACTION_UP) {
 					audioUtility.storeVoice();
 				}
 				return false;
 			}
 		});
 		buyButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(activity, BuyActivity.class);
 				intent.putExtra("goodsId", newsFeedItem.getGoodURL().split("=")[1]);
 				startActivity(intent);
 			}
 		});
 		sendButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// Collect data
 				File audioFile = null;
 				String textComment = "";
 				if (barStatus == ReplyActivity.AUDIO_INPUT) {
 					audioFile = new File(recordName);
 				} else {
 					textComment = textInput.getText().toString();
 				}
 				final String sendCommentURL = Constants.SEND_COMMENT_URL;
 				final Map<String, String> stringMap = new HashMap<String, String>();
 				stringMap.put("comment_url", newsFeedItem.getComments());
 				stringMap.put("content", textComment);
 				final Map<String, File> fileMap = new HashMap<String, File>();
 				fileMap.put("voice", audioFile);
 				// Send data
 				if (!(barStatus == ReplyActivity.TEXT_INPUT && textComment.length() == 0)){
 					new AsyncTask<Void, Void, Void>() {
 
 						@Override
 						protected Void doInBackground(Void... params) {
 							try {
 								MultipartEntityUtil.post(sendCommentURL, fileMap,
 										stringMap);
 							} catch (ClientProtocolException e) {
 								Log.e("ReplyActivity", "ClientProtocolException");
 							} catch (IOException e) {
 								Log.e("ReplyActivity", "I/O Error");
 							}
 							Log.e("reply", "ready to return");
 							return null;
 						}
 
 						@Override
 						protected void onPostExecute(Void result) {
 							super.onPostExecute(result);
 							Log.e("replya", "post");
 							// Clear text
 							if (barStatus == ReplyActivity.TEXT_INPUT) {
 								textInput.setText("");
 							}
 							// Update replyList
 							replyAdapter.updateReplyList();
 							// Scroll to the end of list
 							replyList.setSelection(replyAdapter.getCount() - 1);
 							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 							imm.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
 						}
 
 					}.execute();
 				}
 			}
 		});
 	}
 }
