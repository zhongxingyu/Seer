 package no.ntnu.tdt4240.models;
 
 import no.ntnu.tdt4240.views.PlayerView;
 
 public class Player {
 
 	private String name;
 	private long score;
 	private PlayerView view;
 	
 	public Player(String name) {
 		this.name = name;
 		score = 0;
 	}
 	
 	public void setPlayerView(PlayerView view) {
 		this.view = view;
 		if (view.getPlayer() == null)
 			view.setPlayer(this);
 		
 	}
 
 	public PlayerView getPlayerView() {
 		return view;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 }
