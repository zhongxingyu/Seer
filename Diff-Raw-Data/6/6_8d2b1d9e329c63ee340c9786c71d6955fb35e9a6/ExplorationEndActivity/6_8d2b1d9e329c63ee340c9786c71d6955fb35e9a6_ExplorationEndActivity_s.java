 package jp.knct.di.c6t.ui.exploration;
 
 import java.text.ParseException;
 import java.util.Date;
 import java.util.List;
 
 import jp.knct.di.c6t.IntentData;
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.communication.OutcomesClient;
 import jp.knct.di.c6t.model.Exploration;
 import jp.knct.di.c6t.model.MissionOutcome;
 import jp.knct.di.c6t.model.Outcome;
 import jp.knct.di.c6t.model.QuestOutcome;
 import jp.knct.di.c6t.model.Trophy;
 import jp.knct.di.c6t.ui.HomeActivity;
 import jp.knct.di.c6t.util.ActivityUtil;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Toast;
 
 public class ExplorationEndActivity extends Activity implements OnClickListener {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_exploration_end);
 
 		ActivityUtil.setOnClickListener(this, this, new int[] {
 				R.id.exploration_end_end,
 		});
 
 		Intent intent = getIntent();
 		Exploration exploration = intent.getParcelableExtra(IntentData.EXTRA_KEY_EXPLORATION);
 		List<Outcome> missionOutcomes = intent.getParcelableArrayListExtra(IntentData.EXTRA_KEY_MISSION_OUTCOME_LIST);
 		List<Outcome> questOutcomes = intent.getParcelableArrayListExtra(IntentData.EXTRA_KEY_QUEST_OUTCOME_LIST);
 		List<MissionOutcome> missionOutcomeList = MissionOutcome.convertOutcomes(missionOutcomes);
 		List<QuestOutcome> questOutcomeList = QuestOutcome.convertOutcomes(questOutcomes);
 
 		QuestOutcome lastQuestOutcome = questOutcomeList.get(questOutcomeList.size() - 1);
 		String lastGroupPhotoPath = lastQuestOutcome.getPhotoPath();
 		saveTrophy(exploration, lastGroupPhotoPath);
 		saveOutcomes(exploration, missionOutcomeList, questOutcomeList);
 	}
 
 	private void saveTrophy(Exploration exploration, String lastGroupPhotoPath) {
 		OutcomesClient client = new OutcomesClient();
 		try {
 			client.addTrophy(this, new Trophy(exploration, new Date(), lastGroupPhotoPath));
 			Toast.makeText(this, "gtB[ۑ܂", Toast.LENGTH_SHORT).show();
 		}
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void saveOutcomes(Exploration exploration, List<MissionOutcome> missionOutcomeList, List<QuestOutcome> questOutcomeList) {
 		OutcomesClient client = new OutcomesClient();
 		try {
			client.addMissionOutcome(this, (MissionOutcome[]) missionOutcomeList.toArray());
			client.addQuestOutcome(this, (QuestOutcome[]) questOutcomeList.toArray());
 			Toast.makeText(this, "ʂۑ܂", Toast.LENGTH_SHORT).show();
 		}
 		catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.exploration_end_end:
 			Intent intent = new Intent(this, HomeActivity.class)
 					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			break;
 
 		default:
 			break;
 		}
 
 	}
 }
