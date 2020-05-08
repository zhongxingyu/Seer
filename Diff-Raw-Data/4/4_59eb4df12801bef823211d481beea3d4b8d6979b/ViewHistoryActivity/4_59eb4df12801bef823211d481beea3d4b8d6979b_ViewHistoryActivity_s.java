 package com.example.sicbogameexample;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import sicbo.components.HistoryComponent;
 import sicbo_networks.ConnectionHandler;
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.sicbogameexample.GameEntity.PatternType;
 public class ViewHistoryActivity extends BaseActivity implements OnClickListener {
 
 	TableLayout tblHistory;
 	RelativeLayout rela;
 	boolean isSpace = false;
 	int size,nextIndexHistory,backIndexHistory,indexLastDate,countLoad,countBetDate;
 	List<HistoryComponent> historyGame;
 	ArrayList<PatternType> winPattern;
 	ImageButton imgBack;
 	Button btnLoadHistory;
 	Resources res;
 	ConnectionAsync connectionAsync;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.table_history);
 		res=getResources();
 		tblHistory = (TableLayout) findViewById(R.id.tbl_history);
 		rela=(RelativeLayout)findViewById(R.id.rela_history);
 		historyGame = GameEntity.getInstance().userComponent.historyList;
 		backIndexHistory=historyGame.size();
 		nextIndexHistory=backIndexHistory;
 		imgBack=(ImageButton)findViewById(R.id.btn_back);
 		btnLoadHistory=(Button)findViewById(R.id.btn_load_history);
 		
 		btnLoadHistory.setOnClickListener(this);
 		//createTapLoad();
 		imgBack.setOnClickListener(this);
 		size = historyGame.size();
 		//createTapLoad();
 		initializeHeaderRow(tblHistory);
 		fillRow();
 		indexLastDate=size;
 		countLoad=1;
 		countBetDate=size;
 	}
 
 	String convertDice(String textDice) {
 		String[] splits = textDice.split("\\|");
 		String s = "";
 		int lenght = splits.length;
 		for (int i = 0; i < lenght; i++) {
 			s = s + " " + splits[i];
 		}
 		return s;
 	}
 	private void fillRow() {
 		int textColor = res.getColor(R.color.history_color);
 		float textSize = 10f;
 		String[] betSpot;
 		int lenght;
 		String result;
 		TableRow headerRow;
 		for (int i = indexLastDate; i < size; i++) {
 			if (historyGame.get(i).isWin)
 				result = "Win";
 			else
 				result = "lose";
 
 			headerRow = new TableRow(this);
 			addTextToRowWithValues(
 					headerRow,
 					convertSecondsToDate(
 							Long.parseLong(historyGame.get(i).betDate),
 							"dd/MM/yyyy hh:mm:ss"), textColor, textSize);
 			addTextToRowWithValues(headerRow, result, textColor, textSize);
 			addTextToRowWithValues(headerRow,
 					String.valueOf(historyGame.get(i).balance), textColor,
 					textSize);
 			addTextToRowWithValues(headerRow,
 					convertDice(historyGame.get(i).dices), Color.RED, textSize);
			winPattern=GameEntity.getInstance().currentGame.convertStringtoArrayList(historyGame.get(indexLastDate).betSpot);
 			addTextToRowWithValues(headerRow,winPattern.get(0).toString(), textColor, textSize);
 			tblHistory.addView(headerRow);
 			
 			lenght=winPattern.size();
 			Log.d("Lenght patter",String.valueOf(lenght));
 			for(int j=0;j<lenght-1;j++)
 			{
 			headerRow = new TableRow(this);
 			addTextToRowWithValues(headerRow, "", textColor, textSize);
 			addTextToRowWithValues(headerRow, "", textColor, textSize);
 			addTextToRowWithValues(headerRow, "", textColor, textSize);
 			addTextToRowWithValues(headerRow, "", textColor, textSize);
 			addTextToRowWithValues(headerRow, winPattern.get(j+1).toString(), textColor, textSize);
 			tblHistory.addView(headerRow);
 			}
 
 		}
 
 	}
 
 	
 
 	private void initializeHeaderRow(TableLayout scoreTable) {
 		// Create the Table header row
 		TableRow headerRow = new TableRow(this);
 
 		int textColor = Color.WHITE;
 		float textSize = 10f;
 
 		headerRow.setBackgroundColor(Color.BLUE);
 		addTextToRowWithValues(headerRow, "Date Time", textColor, textSize);
 		addTextToRowWithValues(headerRow, "Result", textColor, textSize);
 		addTextToRowWithValues(headerRow, "Balance", textColor, textSize);
 		addTextToRowWithValues(headerRow, "Dices", textColor, textSize);
 		addTextToRowWithValues(headerRow, "Bet", textColor, textSize);
 
 		scoreTable.addView(headerRow);
 	}
 
 	private void addTextToRowWithValues(final TableRow tableRow, String text,
 			int textColor, float textSize) {
 		TextView textView = new TextView(this);
 		textView.setTextSize(textSize);
 		textView.setTextColor(textColor);
 		textView.setText(text);
 		tableRow.addView(textView);
 	}
 
 	
 
 	public String convertSecondsToDate(long seconds, String dateFormat) {
 		long dateInMillis = seconds * 1000;
 
 		// Create a DateFormatter object for displaying date in specified
 		// format.
 		DateFormat formatter = new SimpleDateFormat(dateFormat);
 
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTimeInMillis(dateInMillis);
 
 		return formatter.format(calendar.getTime());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 
 		// getMenuInflater().inflate(R.menu.activity_view_history, menu);
 		return true;
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 
 		switch(arg0.getId())
 		{
 		case R.id.btn_back:
 			this.finish();
 			break;
 		case R.id.btn_load_history:
 		{   
 			Log.d("countBetDate",String.valueOf(countBetDate));
 			if(countBetDate>19)
 			{
 			connectionAsync = new ConnectionAsync();
 			String[] paramsName = { "last_date" };
 			String[] paramsValue = { historyGame.get(size-1).betDate};
 			Object[] params = { GameEntity.getInstance().connectionHandler, this,
 					GameEntity.VIEW_HISTORY, paramsName, paramsValue };
 			connectionAsync.execute(params);
 			break;
 			}
 			else
 			{
 				Toast.makeText(getApplicationContext(),"No more data to load", Toast.LENGTH_LONG).show();
 			}
 		
 		}
 		
 		
 		}
 	}
 	void createTapLoad()
     {
    	 
    	
    	 RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
    	 params.addRule(RelativeLayout.BELOW,R.id.btn_load_history);
    	 rela.setLayoutParams(params);
    	 
    	 
     }
 	public void onReceiveViewHistory(JSONObject result, Activity activity)
 			throws JSONException {
 		int numOfItem = result.getInt("num_of_item");
 		Log.d("numOfItem",String.valueOf(numOfItem));
 		countBetDate=numOfItem;
 		
 		if (numOfItem > 0) {
 			for (int i = 0; i < numOfItem ; i++) {
 				historyGame.add(new HistoryComponent(result
 						.getJSONObject(i + "").getBoolean("iswin"), result
 						.getJSONObject(i + "").getString("betdate"), result
 						.getJSONObject(i + "").getDouble("balance"), result
 						.getJSONObject(i + "").getString("dices"), result
 						.getJSONObject(i + "").getString("bet_spots")));
 
 			}
 		}
 		size =historyGame.size();
 	
 	}
 	class ConnectionAsync extends AsyncTask<Object, String, Integer> {
 		ConnectionHandler connectionHandler;
 		Activity activity;
         @Override
         protected void onPreExecute() {
         	// TODO Auto-generated method stub
         	createProgressDialog();
         	super.onPreExecute();
         	
         }
 		@Override
 		protected Integer doInBackground(Object... params) {
 			// TODO Auto-generated method stub
 			connectionHandler = (ConnectionHandler) params[0];
 			activity = (Activity) params[1];
 			try {
 				connectionHandler.requestToServer((String) params[2],
 						(String[]) params[3], (Object[]) params[4]);
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 			return null;
 		}
      
 		@Override
 		protected void onPostExecute(Integer value) {
 			try {
 				// dataList = connectionHandler.parseData(responseName);
 				JSONObject result = connectionHandler.getResult();
                  
 				// Create user and move to game scene
 				if(result!=null)
 				{
 					onReceiveViewHistory(result, activity);
 					
 				     indexLastDate=20*countLoad;
 					
 					progressDialog.dismiss();
 					
 					fillRow();
 					countLoad++;
 				} 
 				
 				else
 				{
 					
 					progressDialog.dismiss();
 				}
 
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 }
