 package ru.omsu.diveintoandroid.omskavangard.ui.activities;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
import ru.omsu.diveintoandroid.omskavangard.R;
 import ru.omsu.diveintoandroid.omskavangard.application.Constants;
 import ru.omsu.diveintoandroid.omskavangard.provider.DataContract;
 import ru.omsu.diveintoandroid.omskavangard.services.APICallBuilder;
 import ru.omsu.diveintoandroid.omskavangard.services.APIService.APIResultReceiver;
 import ru.omsu.diveintoandroid.omskavangard.services.APIService.Result;
 import ru.omsu.diveintoandroid.omskavangard.services.protocol.ProtocolUtils;
 import ru.omsu.diveintoandroid.omskavangard.services.protocol.Requests;
 import ru.omsu.diveintoandroid.omskavangard.services.protocol.Responses.GetGamesResponse;
 import ru.omsu.diveintoandroid.omskavangard.services.protocol.Responses.GetGamesResponse.GameModel;
 import ru.omsu.diveintoandroid.omskavangard.ui.adapters.MatchesCursorAdapter;
 import android.annotation.SuppressLint;
 import android.app.ListActivity;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.Toast;
 
 public class MatchesActivity extends ListActivity {
 
 	private MatchesCursorAdapter mListAdapter;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		initListView();
 		updateMatches();
 	}
 
 	@SuppressLint("NewApi")
 	protected void initListView() {
 		getListView().setDividerHeight(0);
 		ContentResolver contentResolver = getContentResolver();
 		final Cursor matchesCursor = contentResolver.query(DataContract.MatchesDetailed.CONTENT_URI, null, null, null, null);
 		mListAdapter = new MatchesCursorAdapter(MatchesActivity.this, matchesCursor, -1);
 		setListAdapter(mListAdapter);
 	}
 
 	protected void updateMatches(){
 		final Intent apiCall = APICallBuilder.makeIntent(
 				this,
 				Requests.Games.getCurrentTourmentGames(),
 				GetGamesResponse.class,
 				new APIResultReceiver() {
 
 					@Override
 					public void onResult(Result result) {
 						if (result.isOk()) {
 							final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
 							final GetGamesResponse response = (GetGamesResponse) result.getResponse();
 							int counter = 0;
 							final Set<ContentValues> teams = new HashSet<ContentValues>();
 							final List<ContentValues> matches = new ArrayList<ContentValues>();
 							for (GameModel game : response.gamesModel.games) {
 								if (game.firstTeam.id == Constants.AVANGARD_ID || game.secondTeam.id == Constants.AVANGARD_ID) {
 									final ContentValues team1 = new ContentValues();
 									team1.put(DataContract.Teams.TEAM_ID, "" + game.firstTeam.id);
 									team1.put(DataContract.Teams.TEAM_NAME, game.firstTeam.name);
 									team1.put(DataContract.Teams.TEAM_LOGO_URL, ProtocolUtils.getLogoUrl(game.firstTeam.logo));
 									teams.add(team1);
 									
 									final ContentValues team2 = new ContentValues();
 									team2.put(DataContract.Teams.TEAM_ID, "" + game.secondTeam.id);
 									team2.put(DataContract.Teams.TEAM_NAME, game.secondTeam.name);
 									team2.put(DataContract.Teams.TEAM_LOGO_URL, ProtocolUtils.getLogoUrl(game.secondTeam.logo));
 									teams.add(team2);
 									
 									final ContentValues match = new ContentValues();
 									match.put(DataContract.Matches.MATCH_ID, "" + game.id);
 									match.put(DataContract.Matches.MATCH_DATE, safeParseDate(game.date, sdf).getTime());
 									match.put(DataContract.Matches.MATCH_FIRST_TEAM_ID, "" + game.firstTeam.id);
 									match.put(DataContract.Matches.MATCH_FIRST_TEAM_SCORES, game.firstTeam.scores);
 									match.put(DataContract.Matches.MATCH_JUDGE, "");
 									match.put(DataContract.Matches.MATCH_JUDGE_L, "");
 									match.put(DataContract.Matches.MATCH_JUDGE_R, "");
 									match.put(DataContract.Matches.MATCH_NUMBER, ++counter);
 									//match.put(DataContract.Matches.MATCH_PROTOCOL, "");
 									match.put(DataContract.Matches.MATCH_STATUS, DataContract.Constants.matchStatus(game.status));
 									match.put(DataContract.Matches.MATCH_SCORES, "");
 									match.put(DataContract.Matches.MATCH_SECOND_TEAM_ID, "" + game.secondTeam.id);
 									match.put(DataContract.Matches.MATCH_SECOND_TEAM_SCORES, "" + game.secondTeam.scores);
 									match.put(DataContract.Matches.MATCH_TOURNAMENT_ID, "" + 1);
 									matches.add(match);
 								}
 							}
 							bulkInsertCollection(DataContract.Teams.CONTENT_URI, teams);
 							bulkInsertCollection(DataContract.Matches.CONTENT_URI, matches);
 							final Cursor matchesCursor = getContentResolver().query(DataContract.MatchesDetailed.CONTENT_URI, null, null, null, null);
 							mListAdapter.swapCursor(matchesCursor);
 						} else if (result.hasNetworkError()){
							Toast.makeText(MatchesActivity.this, R.string.network_error_durind_update_matches, Toast.LENGTH_SHORT).show();
 						} else{
 							//dialog about sent report
 							Toast.makeText(MatchesActivity.this, "Unexpected error.", Toast.LENGTH_SHORT).show();
 						}
 					}
 				});
 		startService(apiCall);
 	}
 	
 	// protected void loadMatches() {
 	// final Intent apiCall = APICallBuilder.makeIntent(
 	// this,
 	// Requests.Games.getCurrentTourmentGames(),
 	// GetGamesResponse.class,
 	// new APIResultReceiver() {
 	//
 	// @Override
 	// public void onResult(Result result) {
 	// if (result.isOk()) {
 	// final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
 	// Locale.getDefault());
 	// final Date now = new Date();
 	// final GetGamesResponse response = (GetGamesResponse)
 	// result.getResponse();
 	// int counter = 0;
 	// final List<Match> matches = new ArrayList<Match>();
 	// for (GameModel game : response.gamesModel.games) {
 	// if (game.firstTeam.id == Constants.AVANGARD_ID || game.secondTeam.id ==
 	// Constants.AVANGARD_ID) {
 	// final Date matchDate = safeParseDate(game.date, sdf);
 	// matches.add(
 	// new Match(
 	// new Team(game.firstTeam.name,
 	// ProtocolUtils.getLogoUrl(game.firstTeam.logo)),
 	// new Team(game.secondTeam.name,
 	// ProtocolUtils.getLogoUrl(game.secondTeam.logo)),
 	// matchDate,
 	// game.firstTeam.scores + ":" + game.secondTeam.scores)
 	// );
 	// if (matchDate.before(now)) {
 	// ++counter;
 	// }
 	// }
 	// }
 	// final ListAdapter adapter = new MatchesListAdapter(MatchesActivity.this,
 	// matches);
 	// setListAdapter(adapter);
 	// getListView().setVisibility(View.INVISIBLE);
 	// final Integer freezedCounter = counter;
 	// getListView().postDelayed(new Runnable() {
 	//
 	// @Override
 	// public void run() {
 	// final int listViewHeight = getListView().getHeight();
 	// final int itemHeight = getListView().getChildAt(0).getHeight();
 	// final float itemsPerScreen = (float) listViewHeight / itemHeight;
 	// getListView().setSelection(Math.max(0, freezedCounter.intValue() - (int)
 	// (itemsPerScreen * 0.5)));
 	// getListView().setVisibility(View.VISIBLE);
 	// }
 	// }, 300);
 	// }
 	// }
 	// });
 	// startService(apiCall);
 	// }
 
 	protected Date safeParseDate(String date, SimpleDateFormat sdf) {
 		try {
 			return sdf.parse(date);
 		} catch (ParseException e) {
 			return new Date();
 		}
 	}
 	
 	protected int bulkInsertCollection(Uri uri, Collection<ContentValues> collection){
 		ContentValues[] cvs = collection.toArray(new ContentValues[collection.size()]);
 		return getContentResolver().bulkInsert(uri, cvs);
 	}
 
 }
