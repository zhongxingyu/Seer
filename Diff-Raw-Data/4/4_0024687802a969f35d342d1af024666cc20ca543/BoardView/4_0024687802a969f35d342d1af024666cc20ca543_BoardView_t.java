 package com.ioabsoftware.gameraven.views;
 
 import com.ioabsoftware.gameraven.AllInOneV2;
 import com.ioabsoftware.gameraven.R;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class BoardView extends LinearLayout {
 	
 	public static enum BoardViewType {
 		NORMAL, SPLIT, LIST
 	}
 	
 	protected String url;
 	
 	private BoardViewType type;
 	
 	public BoardView(Context context, String nameIn, String descIn, String lastPostIn, 
 					 String tCount, String mCount, String urlIn, BoardViewType typeIn) {
 		super(context);
 		
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         inflater.inflate(R.layout.boardview, this);
 
     	TextView desc = (TextView) findViewById(R.id.bvDesc);
     	TextView lastPost = (TextView) findViewById(R.id.bvLastPost);
     	TextView tpcMsgDetails = (TextView) findViewById(R.id.bvTpcMsgDetails);
 
     	((TextView) findViewById(R.id.bvName)).setText(nameIn);
     	
     	if (descIn != null)
     		desc.setText(descIn);
     	else
     		desc.setVisibility(View.INVISIBLE);
     	
     	switch (typeIn) {
 		case NORMAL:
             lastPost.setText("Last Post: " + lastPostIn);
             tpcMsgDetails.setText("Tpcs: " + tCount + "; Msgs: " + mCount);
 			break;
 		case SPLIT:
 			lastPost.setText("--Split List--");
             tpcMsgDetails.setVisibility(View.INVISIBLE);
 			break;
 		case LIST:
             lastPost.setText("--Board List--");
             tpcMsgDetails.setVisibility(View.INVISIBLE);
 			break;
     	}
         
         url = urlIn;
         type = typeIn;
         
         findViewById(R.id.bvSep).setBackgroundColor(AllInOneV2.getAccentColor());
         
         setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
 	}
 	
 	public BoardView(Context context, String platform, String name, String urlIn) {
 		super(context);
 		
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         inflater.inflate(R.layout.gamesearchview, this);
         
         ((TextView) findViewById(R.id.gsPlatform)).setText("Platform: " + platform);
         ((TextView) findViewById(R.id.gsName)).setText(name);
         
 		url = urlIn;
 		type = BoardViewType.NORMAL;
 		
		findViewById(R.id.gsSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
 	}
 	
 	public String getUrl() {
 		return url;
 	}
 	
 	public BoardViewType getType() {
 		return type;
 	}
 }
