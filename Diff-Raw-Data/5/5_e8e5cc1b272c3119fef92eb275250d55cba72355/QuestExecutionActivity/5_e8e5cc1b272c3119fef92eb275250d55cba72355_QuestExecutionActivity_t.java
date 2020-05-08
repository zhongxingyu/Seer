 package jp.knct.di.c6t.ui.exploration;
 
 import java.io.IOException;
 import java.util.Date;
 
 import jp.knct.di.c6t.IntentData;
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.model.Exploration;
 import jp.knct.di.c6t.model.MissionOutcome;
 import jp.knct.di.c6t.model.Quest;
 import jp.knct.di.c6t.model.QuestOutcome;
 import jp.knct.di.c6t.util.ActivityUtil;
 import jp.knct.di.c6t.util.ImageUtil;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class QuestExecutionActivity extends Activity implements OnClickListener {
 
 	private static int REQUEST_CODE_CAPTURE_GROUP_PHOTO = 0x001;
 	private static int REQUEST_CODE_CAPTURE_MISSION_PHOTO = 0x002;
 	public static int REQUEST_CODE_EXECUTION = 0x001;
 
 	private QuestOutcome mQuestOutcome;
 	private MissionOutcome mMissionOutcome;
 	private Quest mQuest;
 	private boolean mHasMissionCompleted = false;
 	private boolean mHasPoseCompleted = false;
 	private Uri mGroupPhotoUri;
 	private Uri mMissionPhotoUri;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_quest_execution);
 
 		Intent intent = getIntent();
 		int questNumber = intent.getIntExtra(IntentData.EXTRA_KEY_QUEST_NUMBER, -1);
 		Exploration exploration = intent.getParcelableExtra(IntentData.EXTRA_KEY_EXPLORATION);
 		mQuest = exploration.getRoute().getQuests().get(questNumber);
 
 		mQuestOutcome = new QuestOutcome(exploration, questNumber, null, null);
 		mMissionOutcome = new MissionOutcome(exploration, questNumber, null, null);
 
 		putQuestData(questNumber, mQuest);
 
 		// TODO: case last quest
 
 		ActivityUtil.setOnClickListener(this, this, new int[] {
 				R.id.quest_execution_capture_group_photo,
 				R.id.quest_execution_capture_mission_photo,
 		});
 	}
 
 	private void putQuestData(int questNumber, Quest quest) {
 		new ActivityUtil(this)
 				.setText(R.id.quest_execution_pose, quest.getPose())
 				.setText(R.id.quest_execution_mission, quest.getMission())
 				.setImageBitmap(R.id.quest_execution_image, quest.decodeImageBitmap(10));
 	}
 
 	private void putImage(int id, Uri imageUri) {
 		Bitmap image = ImageUtil.decodeBitmap(imageUri.getPath(), 10);
 		((ImageView) findViewById(id)).setImageBitmap(image);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == REQUEST_CODE_CAPTURE_GROUP_PHOTO && resultCode == RESULT_OK) {
 			// TODO: notify c6t server that group photo is taken
 			mHasPoseCompleted = true;
 			mQuestOutcome.setPhotoedAt(new Date());
 			mQuestOutcome.setPhotoPath(mGroupPhotoUri.getPath());
 			ActivityUtil.setImageBitmap(this,
					R.id.quest_execution_group_photo,
 					mQuestOutcome.decodePhotoBitmap(10));
 		}
 
 		if (requestCode == REQUEST_CODE_CAPTURE_MISSION_PHOTO && resultCode == RESULT_OK) {
 			// TODO: notify c6t server that mission photo is taken
 			mHasMissionCompleted = true;
 			mMissionOutcome.setPhotoedAt(new Date());
 			mMissionOutcome.setPhotoPath(mMissionPhotoUri.getPath());
 			ActivityUtil.setImageBitmap(this,
					R.id.quest_execution_mission_photo,
 					mMissionOutcome.decodePhotoBitmap(10));
 		}
 
 		// FIXME: move to continuous update process
 		checkQuestCompletion();
 	}
 
 	private void checkQuestCompletion() {
 		// FIXME: change to confirm the server side quest state
 		if (mHasMissionCompleted && mHasPoseCompleted) {
 			Toast.makeText(this, "NGXg܂", Toast.LENGTH_SHORT).show();
 			setResult(RESULT_OK, new Intent()
 					.putExtra(IntentData.EXTRA_KEY_QUEST_OUTCOME, mQuestOutcome)
 					.putExtra(IntentData.EXTRA_KEY_MISSION_OUTCOME, mMissionOutcome));
 			finish();
 		}
 
 		// TODO: save captured photos to the app local collection
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
 
 		default:
 			break;
 		}
 
 	}
 }
