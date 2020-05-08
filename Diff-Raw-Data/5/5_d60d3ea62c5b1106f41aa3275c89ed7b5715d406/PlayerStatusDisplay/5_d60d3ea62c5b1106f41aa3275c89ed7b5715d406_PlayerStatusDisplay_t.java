 package com.rachum.amir.skyhiking.android;
 
 import com.rachum.amir.skyhiking.Move;
 import com.rachum.amir.skyhiking.android.R;
 import com.rachum.amir.skyhiking.players.Player;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class PlayerStatusDisplay extends LinearLayout {
 	private final TextView playerName;
 	private final ImageView statusImage;
 	private final Player player;
 	
 	public PlayerStatusDisplay(Context context, Player player) {
 		super(context);
 		this.player = player;
 		playerName = new TextView(context);
 		this.setPadding(10, 20, 10, 0);
 		playerName.setPadding(0, 0, 20, 0);
 		playerName.setTextSize(20);
 		statusImage = new ImageView(context);
 		setPlaying(true);
 		unsetStatus();
 		updateScore();
 		addView(playerName);
 		addView(statusImage);
 	}
 	
 	public void setMove(Move move) {
 		statusImage.setVisibility(VISIBLE);
 		switch (move) {
 		case LEAVE:
 			statusImage.setImageResource(R.drawable.leave);
 			break;
 		case STAY:
 			statusImage.setImageResource(R.drawable.stay);
 			break;
 		}
 		statusImage.getLayoutParams().height = 40;
 		statusImage.getLayoutParams().width = 40;
 	}
 	
 	public void setPlaying(boolean playing) {
 		if (playing) {
 			playerName.setTextColor(Color.BLACK);
 		} else {
 			playerName.setTextColor(Color.GRAY);
 		}
 	}
 	
 	public void setPilot() {
 		statusImage.setVisibility(VISIBLE);
 		statusImage.setImageResource(R.drawable.pilot); //TODO: change to pilot image
 		statusImage.getLayoutParams().height = 40;
 		statusImage.getLayoutParams().width = 40;
 	}
 	
 	public void unsetStatus() {
 		statusImage.setVisibility(INVISIBLE);
 	}
 	
 	public void updateScore() {
		int cardCount = player.getHand().getCards().size();
		int score = player.getScore();
		playerName.setText(player.getName() + " (" + cardCount + "C / " + 
						   score + "P)");
 	}
 }
