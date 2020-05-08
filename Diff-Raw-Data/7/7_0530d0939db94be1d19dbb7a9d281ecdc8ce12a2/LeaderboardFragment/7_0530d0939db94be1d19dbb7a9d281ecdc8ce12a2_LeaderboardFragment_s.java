 package io.meetme.ui.fragment;
 
 import io.meetme.MeetMeApplication;
 import io.meetme.R;
 import io.meetme.adapter.UsersList;
 import io.meetme.database.DatabaseManager;
 import io.meetme.database.User;
 import io.meetme.restclient.RestClient;
 import io.meetme.restclient.RestClient.RequestMethod;
 import io.meetme.restclient.RestClient.Response;
 import io.meetme.util.AndroidUtils;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class LeaderboardFragment extends Fragment implements OnClickListener {
 
     /**
      * [{"username": "Delfin", "points": 190, "id": "934503aq123312"},
      * {"username": "Ivan", "points": 100, "id": "903aqwe312"}, {"username":
      * "VaIer", "points": 20, "id": "123aqwe312"}]
      */
     private TextView textTeviewPoints;
     private Button buttonPushPoints;
     private PointsLoader pointsLoader;
     private Thread threadPointSender;
     private UsersList usersAdapter;
     private ListView listView;
     private RankingLoader rankingLoader;
 
     /**
      * [{"username": "Delfin", "points": 190, "id": "934503aq123312"},
      * {"username": "Ivan", "points": 100, "id": "903aqwe312"}, {"username":
      * "VaIer", "points": 20, "id": "123aqwe312"}]
      */
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
 	    Bundle savedInstanceState) {
 	return inflateLayout(inflater, container);
     }
 
     private View inflateLayout(LayoutInflater inflater, ViewGroup container) {
 	View layout = inflater.inflate(R.layout.fragment_leaderboard,
 		container, false);
 
 	usersAdapter = new UsersList(getActivity());
 
 	textTeviewPoints = (TextView) layout.findViewById(R.id.textViewPoints);
 	buttonPushPoints = (Button) layout.findViewById(R.id.buttonPushPoints);
 
 	buttonPushPoints.setOnClickListener(this);
 
 	listView = (ListView) layout.findViewById(R.id.list);
 	listView.setAdapter(usersAdapter);
 
 	return layout;
     }
 
     @Override
     public void onResume() {
 	super.onResume();
 	if (rankingLoader == null
 		|| rankingLoader.getStatus() == Status.FINISHED) {
 	    rankingLoader = new RankingLoader();
 	    rankingLoader.execute();
 	}
 
 	pointsLoader = new PointsLoader();
 	pointsLoader.execute();
 
     }
 
     @Override
     public void onPause() {
 	super.onPause();
 	pointsLoader.cancel(true);
     }
 
     private class PointsLoader extends AsyncTask<Void, Void, Integer> {
 
 	@Override
 	protected void onPreExecute() {
 	    super.onPreExecute();
 	    textTeviewPoints.setText("Loading data");
 	}
 
 	@Override
 	protected Integer doInBackground(Void... params) {
 
 	    DatabaseManager databaseManager = DatabaseManager.getInstance();
 
 	    return databaseManager.getPoints();
 	}
 
 	@Override
 	protected void onPostExecute(Integer result) {
 	    super.onPostExecute(result);
 	    textTeviewPoints.setText(getString(R.string.your_points) + result);
 	}
     }
 
     private class RankingLoader extends AsyncTask<Void, Void, ArrayList<User>> {
 
 	@Override
 	protected ArrayList<User> doInBackground(Void... params) {
 
 	    RestClient restClient = new RestClient(
 		    "http://googleio.vador.mydevil.net/getsorted/",
 		    RequestMethod.GET);
 
 	    restClient.AddParam("id",
 		    AndroidUtils.getUUID(MeetMeApplication.instance));
 	    restClient.AddParam("points",
 		    String.valueOf(DatabaseManager.getInstance().getPoints()));
 	    restClient.AddParam("username",
 		    String.valueOf(DatabaseManager.getInstance().getPoints()));
 
 	    try {
 		restClient.buildRequest();
 	    } catch (Exception e) {
 		e.printStackTrace();
 		// TODO error message show
 		return null;
 	    }
 	    restClient.executeRequest();
 
 	    Response response = restClient.getResponse();
 
 	    ArrayList<User> users = new ArrayList<User>();
 	    try {
 		JSONArray jsonArray = new JSONArray(response.content);
 
 		for (int i = 0; i < jsonArray.length(); ++i) {
 		    User user = new User();
 		    user.setName(jsonArray.getJSONObject(i).getString(
 			    "username"));
 		    user.setPoints(jsonArray.getJSONObject(i)
 			    .getInt(("points")));
 		    user.setUserID(jsonArray.getJSONObject(i).getString("id"));
 
 		    users.add(user);
 		}
 
 	    } catch (JSONException e) {
 		e.printStackTrace();
 	    }
 
 	    return users;
 	}
 
 	@Override
 	protected void onPostExecute(ArrayList<User> result) {
 	    super.onPostExecute(result);

 	    usersAdapter.addAll(result);
 	}
     }
 
     @Override
     public void onClick(View v) {
 
 	if (threadPointSender == null || threadPointSender.isAlive() == false) {
 	    threadPointSender = new Thread(new MeetMeApplication.StatSender());
 	    threadPointSender.start();
 	} else {
 	    Toast.makeText(getActivity(), "Yeah i'm sending...",
 		    Toast.LENGTH_SHORT).show();
 	}
 
 	if (rankingLoader == null
 		|| rankingLoader.getStatus() == Status.FINISHED) {
 	    rankingLoader = new RankingLoader();
 	    rankingLoader.execute();
 	}
     }
 
     @Override
     public void onDestroy() {
 	super.onDestroy();
 
 	if (threadPointSender != null) {
 	    threadPointSender.interrupt();
 	    threadPointSender = null;
 	}
 
 	if (rankingLoader != null) {
 	    rankingLoader.cancel(true);
 	    rankingLoader = null;
 	}
     }
 }
