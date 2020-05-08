 package org.svcba.scoreboard.dialog;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.svcba.scoreboard.R;
 import org.svcba.scoreboard.SVCBAApp;
 import org.svcba.scoreboard.model.Action;
 import org.svcba.scoreboard.model.Game;
 import org.svcba.scoreboard.model.PlayerStat;
 import org.svcba.scoreboard.model.Team;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.Window;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 public class StatActivity extends Activity
 {
 	public static final int HOME = 0;
 	public static final int AWAY = 1;
 	public String currentStats;
 	
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		setContentView(R.layout.dlg_stat);
 		
 		Intent intent = getIntent();
 		int side = intent.getIntExtra("side", 0);
 		ListView lv = (ListView)findViewById(R.id.stat_chart);
 		SimpleAdapter sa = new SimpleAdapter(this, getStat(side),R.layout.stat_row,new String[]{"name","1p", "2p", "3p", "p", "rb","ass","stl", "f", "to"},new int[]{R.id.stat_name,R.id.stat_1p,R.id.stat_2p,R.id.stat_3p,R.id.stat_p,R.id.stat_rb,R.id.stat_ass,R.id.stat_stl,R.id.stat_f,R.id.stat_to});
 		lv.setAdapter(sa);
 				
     }
 	
 	private List<Map<String, Object>> getStat(int side)
 	{
 		String finalString = new String();
 		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
 		Game game = ((SVCBAApp)getApplicationContext()).getGame();
 		Team team = null;
 		if (side == HOME)
 		{
 			team = game.getHomeTeam();
 		}
 		else if (side == AWAY)
 		{
 			team = game.getAwayTeam();
 		}
 		List<Action> actions = game.getActions();
 		Map<String, PlayerStat> players = new HashMap<String, PlayerStat>();
 		for (Map<String, Object> player : team.getAllPlayers())
 		{
 			PlayerStat p = new PlayerStat();
 			p.setName((String)player.get("name"));
 			players.put((String)player.get("name"), p);
 		}
 		for (Action action : actions)
 		{
 			
 			switch(action.getAction())
 			{
 				case Action.SHOOT_1:
 					PlayerStat p = players.get((String)action.getPlayer().get("name"));
 					if (p == null)
 						break;
 					if (action.getResult() == Action.MADE)
 					{
 						p.add1PMade();
 					}
 					else if (action.getResult() == Action.MISS)
 					{
 						p.add1PMiss();
 					}
 					break;
 				case Action.SHOOT_2:
 					p = players.get((String)action.getPlayer().get("name"));
 					if (p == null)
 						break;
 					if (action.getResult() == Action.MADE)
 					{
 						p.add2PMade();
 					}
 					else if (action.getResult() == Action.MISS)
 					{
 						p.add2PMiss();
 					}
 					break;
 				case Action.SHOOT_3:
 					p = players.get((String)action.getPlayer().get("name"));
 					if (p == null)
 						break;
 					if (action.getResult() == Action.MADE)
 					{
 						p.add3PMade();
 					}
 					else if (action.getResult() == Action.MISS)
 					{
 						p.add3PMiss();
 					}
 					break;
 				case Action.REBOUND_F:
 					p = players.get((String)action.getPlayer().get("name"));
 					if (p == null)
 						break;
 					p.addRbF();
 					break;
 				case Action.REBOUND_B:
 					p = players.get((String)action.getPlayer().get("name"));
 					if (p == null)
 						break;
 					p.addRbB();
 					break;
 				case Action.FOUL:
 					p = players.get((String)action.getPlayer().get("name"));
 					if (p == null)
 						break;
 					p.addF();
 					break;
 				case Action.TURNOVER:
 					p = players.get((String)action.getPlayer().get("name"));
 					if (p == null)
 						break;
 					p.addTO();
 					break;
 				case Action.ASSIST:
 					p = players.get((String)action.getPlayer().get("name"));
 					if (p == null)
 						break;
 					p.addAssist();
 					break;
 				case Action.STEAL:
 					p = players.get((String)action.getPlayer().get("name"));
 					if (p == null)
 						break;
 					p.addSteal();
 					break;
 				default:
 			}
 		}
 		currentStats = new String();
 		//Title line
 		Resources res = getResources();
 		currentStats += res.getString(R.string.ab_name)+",\t";
		currentStats += res.getString(R.string.ab_1p)+", In ,\t";
		currentStats += res.getString(R.string.ab_2p)+", In ,\t";
		currentStats += res.getString(R.string.ab_3p)+", In ,\t";
 		currentStats += res.getString(R.string.ab_p)+",\t";
		currentStats += res.getString(R.string.ab_rb)+"F,B,\t";
 		currentStats += res.getString(R.string.ab_f)+",\t";
 		currentStats += res.getString(R.string.ab_to)+",\t";
 		currentStats += res.getString(R.string.ab_ass)+",\t";
 		currentStats += res.getString(R.string.ab_steal)+",\n";
 		
 		for (PlayerStat p : players.values())
 		{
 			Map<String, Object> player = new HashMap<String, Object>();
 			player.put("name", p.getName());
 			player.put("1p", p.get1P());
 			player.put("2p", p.get2P());
 			player.put("3p", p.get3P());
 			player.put("p", p.getP());
 			player.put("rb", p.getRB());
 			player.put("f", p.getF());
 			player.put("to", p.getTO());
 			player.put("ass", p.getAssist());
 			player.put("stl", p.getSteal());
 			result.add(player);
 			
 			currentStats+=(p.getName() + ",\t");
 			currentStats+=(p.get1P() + ",\t");
 			currentStats+=(p.get2P() + ",\t");
 			currentStats+=(p.get3P() + ",\t");
 			currentStats+=(p.getP() + ",\t");
 			currentStats+=(p.getRB() + ",\t");
 			currentStats+=(p.getF() + ",\t");
 			currentStats+=(p.getTO() + ",\t");
 			currentStats+=(p.getAssist() + ",\t");
 			
 			currentStats+=(p.getSteal() + ",\n");
 			
 		}
 		
 		return result;
 	}
 	
 	private String getDateTime() {
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         Date date = new Date();
         return dateFormat.format(date);
     }
 	
 	public void onBackPressed()
 	{    
 	    
 		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);		 
         emailIntent.setType("plain/text");
         emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"ztenghui@gmail.com;biomemsgao@gmail.com;greatsvcba@gmail.com"});
 
         emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Game Result " + getDateTime());
 
         emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, currentStats);
 
         startActivity(Intent.createChooser(emailIntent, "Send mail..."));
 		
         finish();
 		super.onBackPressed();
 	}
 }
