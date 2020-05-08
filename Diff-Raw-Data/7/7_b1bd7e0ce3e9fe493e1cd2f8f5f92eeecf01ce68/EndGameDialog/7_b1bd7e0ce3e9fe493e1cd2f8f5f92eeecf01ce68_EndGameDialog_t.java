 package com.chess.genesis;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import java.util.HashMap;
 import java.util.Map;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 class EndGameDialog extends Dialog implements OnClickListener
 {
 	private static final String[] won_check = {"You Won", "Checkmate"};
 	private static final String[] lost_check = {"You Lost", "Checkmate"};
 	private static final String[] lost_resign = {"You Lost", "Resigned"};
 	private static final String[] won_resign = {"You Won", "Resigned"};
 	private static final String[] tied_imp = {"Game Tied", "Imposibility of Checkmate"};
 	private static final String[] tied_stale = {"Game Tied", "Stalemate"};
 
 	private static final Map<Integer,String[]> statusMap = createMap();
 	
 	private static Map<Integer, String[]> createMap()
 	{
 		Map<Integer, String[]> map = new HashMap<Integer, String[]>();
 		map.put(Piece.WHITE * Enums.WHITEMATE, won_check);
 		map.put(Piece.WHITE * Enums.BLACKMATE, lost_check);
 		map.put(Piece.WHITE * Enums.WHITERESIGN, lost_resign);
 		map.put(Piece.WHITE * Enums.BLACKRESIGN, won_resign);
 		map.put(Piece.WHITE * Enums.IMPOSSIBLE, tied_imp);
 		map.put(Piece.WHITE * Enums.STALEMATE, tied_stale);
 
 		map.put(Piece.BLACK * Enums.WHITEMATE, lost_check);
 		map.put(Piece.BLACK * Enums.BLACKMATE, won_check);
 		map.put(Piece.BLACK * Enums.WHITERESIGN, won_resign);
 		map.put(Piece.BLACK * Enums.BLACKRESIGN, lost_resign);
 		map.put(Piece.BLACK * Enums.IMPOSSIBLE, tied_imp);
 		map.put(Piece.BLACK * Enums.STALEMATE, tied_stale);
 		return map;
 	}
 
 	private String title;
 	private String opponent;
 	private String result;
 	private String psr_type;
 	private String psr_score;
 	private int diff;
 
 	public EndGameDialog(Context context, JSONObject json)
 	{
 		super(context);
 
 		String[] statusArr = null;
 		String gametype = null, gameid = null, sign = null;
 		int ycol = 0, w_from = 0, w_to = 0, b_from = 0, b_to = 0;;
 	
 		try {
 			gameid = json.getString("gameid");
 			ycol = json.getInt("yourcolor");
 			statusArr = statusMap.get(json.getInt("status") * ycol);
 			gametype = json.getString("gametype");
			gametype = gametype.substring(0,1).toUpperCase() + gametype.substring(1);
 
 			if (ycol == Piece.WHITE)
 				opponent = json.getString("black_name");
 			else
 				opponent = json.getString("white_name");
 
 			w_from = json.getJSONObject("white").getInt("from");
 			w_to = json.getJSONObject("white").getInt("to");
 
 			b_from = json.getJSONObject("black").getInt("from");
 			b_to = json.getJSONObject("black").getInt("to");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		int to = (ycol == Piece.WHITE)? w_to : b_to;
 		diff = (ycol == Piece.WHITE)? (w_to - w_from) : (b_to - b_from);
 		sign = (diff >= 0)? "+" : "-";
 
 		title = statusArr[0];
 		result = statusArr[1];
 		psr_type = gametype + " PSR :";
 		psr_score = sign + String.valueOf(Math.abs(diff)) + " (" + String.valueOf(to) + ")";
 
 		GameDataDB db = new GameDataDB(context);
 		db.archiveNetworkGame(gameid, w_from, w_to, b_from, b_to);
 		db.close();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		setTitle(title);
 
 		setContentView(R.layout.endgame);
 
 		Button close = (Button) findViewById(R.id.close);
 		close.setOnClickListener(this);
 
 		TextView msg = (TextView) findViewById(R.id.opponent);
 		msg.setText(opponent);
 
 		msg = (TextView) findViewById(R.id.result);
 		msg.setText(result);
 
 		msg = (TextView) findViewById(R.id.psr_type);
 		msg.setText(psr_type);
 
 		msg = (TextView) findViewById(R.id.psr_score);
 		msg.setText(psr_score);
 		if (diff >= 0)
 			msg.setTextColor(0xFF00FF00);
 		else
 			msg.setTextColor(0xFFFF0000);
 	}
 
 	public void onClick(View v)
 	{
 		dismiss();
 	}
 }
