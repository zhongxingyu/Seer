 package jp.knct.di.c6t.ui.exploration;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.Date;
 
 import jp.knct.di.c6t.IntentData;
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.communication.BasicClient;
 import jp.knct.di.c6t.model.Exploration;
 import jp.knct.di.c6t.model.MissionOutcome;
 import jp.knct.di.c6t.model.Quest;
 import jp.knct.di.c6t.model.QuestOutcome;
 import jp.knct.di.c6t.util.ActivityUtil;
 import jp.knct.di.c6t.util.ImageUtil;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Toast;
 
 public class QuestExecutionActivity extends Activity implements OnClickListener {
 
 	private static int REQUEST_CODE_CAPTURE_GROUP_PHOTO = 0x001;
 	private static int REQUEST_CODE_CAPTURE_MISSION_PHOTO = 0x002;
 	public static int REQUEST_CODE_EXECUTION = 0x001;
 
 	private QuestOutcome mQuestOutcome;
 	private MissionOutcome mMissionOutcome;
 	private Exploration mExploration;
 	private int mQuestNumber;
 	private boolean mHasMissionCompleted = false;
 	private boolean mHasPoseCompleted = false;
 	private Uri mGroupPhotoUri;
 	private Uri mMissionPhotoUri;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_quest_execution);
 
 		Intent intent = getIntent();
 		mQuestNumber = intent.getIntExtra(IntentData.EXTRA_KEY_QUEST_NUMBER, -1);
 
 		if (mQuestNumber == 4) {
 			mHasMissionCompleted = true;
 		}
 
 		mExploration = intent.getParcelableExtra(IntentData.EXTRA_KEY_EXPLORATION);
 
		mQuestOutcome = new QuestOutcome(mExploration, mQuestNumber, null, null);
		mMissionOutcome = new MissionOutcome(mExploration, mQuestNumber, null, null);

 		renderQuestData(mQuestNumber, mExploration, getCurrentQuest());
 
 		ActivityUtil.setOnClickListener(this, this, new int[] {
 				R.id.quest_execution_capture_group_photo,
 				R.id.quest_execution_capture_mission_photo,
 
 				R.id.debug_quest_execution_capture_group_photo,
 				R.id.debug_quest_execution_capture_mission_photo,
 		});
 
 		new UpdatingLoopTask().execute(mExploration);
 	}
 
 	private void renderQuestData(int questNumber, Exploration exploration, Quest quest) {
 		ActivityUtil setter = new ActivityUtil(this);
 		setter
 				.setText(R.id.quest_execution_pose, quest.getPose())
 				.setText(R.id.quest_execution_mission, quest.getMission())
 				.setText(R.id.quest_execution_group_photo_state, (exploration.isPhotographed()) ?
 						"Beς" : "NBeĂ܂")
 				.setText(R.id.quest_execution_mission_state,
 						exploration.getMembers().size() + "lA" + exploration.getMissionCompletedNumber() + "lBeĂ܂")
 				.setImageBitmap(R.id.quest_execution_image, quest.decodeImageBitmap(10));
 
 		if (mHasMissionCompleted) {
 			findViewById(R.id.quest_execution_capture_mission_photo).setVisibility(View.INVISIBLE);
 		}
 
 		if (mHasPoseCompleted) {
 			findViewById(R.id.quest_execution_capture_group_photo).setVisibility(View.INVISIBLE);
 		}
 	}
 
 	private void renderCapturedMissionPhoto(MissionOutcome outcome) {
 		ActivityUtil.setImageBitmap(this,
 				R.id.quest_execution_mission_photo,
 				outcome.decodePhotoBitmap(10));
 	}
 
 	private void renderCapturedGroupPhoto(QuestOutcome outcome) {
 		ActivityUtil.setImageBitmap(this,
 				R.id.quest_execution_group_photo,
 				outcome.decodePhotoBitmap(10));
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == REQUEST_CODE_CAPTURE_GROUP_PHOTO && resultCode == RESULT_OK) {
 			completeQuest();
 		}
 
 		if (requestCode == REQUEST_CODE_CAPTURE_MISSION_PHOTO && resultCode == RESULT_OK) {
 			completeMission();
 		}
 
 		renderQuestData(mQuestNumber, mExploration, getCurrentQuest());
 	}
 
 	private void completeQuest() {
 		new PuttingGroupPhotoTask().execute(mExploration);
 		mHasPoseCompleted = true;
		mQuestOutcome.setPhotoedAt(new Date());
		mQuestOutcome.setPhotoPath(mGroupPhotoUri.getPath());
 		renderCapturedGroupPhoto(mQuestOutcome);
 	}
 
 	private void completeMission() {
 		new PuttingMissionPhotoTask().execute(mExploration);
 		mHasMissionCompleted = true;
		mMissionOutcome.setPhotoedAt(new Date());
		mMissionOutcome.setPhotoPath(mMissionPhotoUri.getPath());
 		renderCapturedMissionPhoto(mMissionOutcome);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.quest_execution_capture_group_photo:
 			try {
 				mGroupPhotoUri = ImageUtil.createTempFileUri();
 				Intent intent = ImageUtil.createCaptureImageIntent(mGroupPhotoUri);
 				startActivityForResult(intent, REQUEST_CODE_CAPTURE_GROUP_PHOTO);
 			}
 			catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			break;
 
 		case R.id.quest_execution_capture_mission_photo:
 			try {
 				mMissionPhotoUri = ImageUtil.createTempFileUri();
 				Intent intent = ImageUtil.createCaptureImageIntent(mMissionPhotoUri);
 				startActivityForResult(intent, REQUEST_CODE_CAPTURE_MISSION_PHOTO);
 			}
 			catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			break;
 
 		case R.id.debug_quest_execution_capture_group_photo:
 			mGroupPhotoUri = Uri.parse(ImageUtil.debugGetSampleImagePath(this));
 			completeQuest();
 			renderQuestData(mQuestNumber, mExploration, getCurrentQuest());
 			break;
 
 		case R.id.debug_quest_execution_capture_mission_photo:
 			mMissionPhotoUri = Uri.parse(ImageUtil.debugGetSampleImagePath(this));
 			completeMission();
 			renderQuestData(mQuestNumber, mExploration, getCurrentQuest());
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	private void onExplorationUpdate(Exploration exploration) {
 		mExploration = exploration;
 		if (exploration.getQuestNumber() != mQuestNumber) {
 			finishQuest(exploration, mQuestNumber);
 			return;
 		}
 
 		renderQuestData(mQuestNumber, exploration, getCurrentQuest());
 	}
 
 	private void finishQuest(Exploration exploration, int questNumber) {
 		Toast.makeText(this, "NGXg܂", Toast.LENGTH_SHORT).show();
 		setResult(RESULT_OK, new Intent()
 				.putExtra(IntentData.EXTRA_KEY_EXPLORATION, exploration)
 				.putExtra(IntentData.EXTRA_KEY_QUEST_OUTCOME, mQuestOutcome)
 				.putExtra(IntentData.EXTRA_KEY_MISSION_OUTCOME, mMissionOutcome));
 		finish();
 	}
 
 	private Quest getCurrentQuest() {
 		return mExploration.getRoute().getQuests().get(mQuestNumber);
 	}
 
 	private class PuttingGroupPhotoTask extends AsyncTask<Exploration, String, Void> {
 		@Override
 		protected Void doInBackground(Exploration... exploration) {
 			BasicClient client = new BasicClient();
 			HttpResponse response = null;
 			try {
 				response = client.putGroupPhoto(exploration[0], mQuestNumber);
 			}
 			catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			int statusCode = response.getStatusLine().getStatusCode();
 			if (statusCode >= 300) {
 				publishProgress("error: " + statusCode);
 				cancel(true);
 				return null;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(String... progressText) {
 			Toast.makeText(getApplicationContext(), progressText[0], Toast.LENGTH_SHORT).show();
 		}
 
 		@Override
 		protected void onPostExecute(Void params) {
 		}
 	}
 
 	private class PuttingMissionPhotoTask extends AsyncTask<Exploration, String, Void> {
 		@Override
 		protected Void doInBackground(Exploration... exploration) {
 			BasicClient client = new BasicClient();
 			HttpResponse response = null;
 			try {
 				response = client.putMissionPhoto(exploration[0], mQuestNumber);
 			}
 			catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			int statusCode = response.getStatusLine().getStatusCode();
 			if (statusCode >= 300) {
 				publishProgress("error: " + statusCode);
 				cancel(true);
 				return null;
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(String... progressText) {
 			Toast.makeText(getApplicationContext(), progressText[0], Toast.LENGTH_SHORT).show();
 		}
 
 		@Override
 		protected void onPostExecute(Void params) {
 		}
 	}
 
 	private class UpdatingLoopTask extends AsyncTask<Exploration, String, Exploration> {
 		private static final int interval = 2000;
 
 		@Override
 		protected Exploration doInBackground(Exploration... exploration) {
 			try {
 				Thread.sleep(interval);
 			}
 			catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			try {
 				return new BasicClient().getExploration(exploration[0].getId());
 			}
 			catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Exploration exploration) {
 			if (exploration == null) {
 				throw new AssertionError("pending...");
 			}
 
 			onExplorationUpdate(exploration);
 
 			if (exploration.getQuestNumber() == mQuestNumber) {
 				new UpdatingLoopTask().execute(exploration);
 			}
 		}
 	}
 }
