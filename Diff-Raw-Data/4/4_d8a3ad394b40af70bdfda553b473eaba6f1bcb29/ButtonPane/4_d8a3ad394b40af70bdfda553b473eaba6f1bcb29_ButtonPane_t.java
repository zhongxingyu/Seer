 package com.ricex.rpi.server.player;
 
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.scene.control.Button;
 import javafx.scene.layout.VBox;
 
 import com.ricex.rpi.common.PlayerModule;
 
 /** Will probally need to change this in future, but temporary for now
  * 
  *  Contains the buttons on the side for interacting with the movie list
  *  
  * @author Mitchell
  *
  */
 
 public class ButtonPane extends VBox implements EventHandler<ActionEvent> {
 	
 	/** Button to start playing the selected movie in the list view */
 	private Button butPlay;	
 	
 	/** Button to pause / resume the playing movie */
 	private Button butPause;
 	
 	/** Button to stop playing the current movie */
 	private Button butStop;
 	
 	/** Button for seeking to the left 30 seconds */
 	private Button butSeekLeft;
 	
 	/** Button for seeking to the left 600 seconds */
 	private Button butSeekLeftFast;
 	
 	/** Button for seeking to the right 30 seconds */
 	private Button butSeekRight;
 	
 	/** Button for seeking to the right 600 secconds */
 	private Button butSeekRightFast;
 	
 	/** The player module interface to complete the given actions */
 	private PlayerModule playerModule;
 	
 	/** The list view representing the movies */
 	private MovieListView movieListView;
 	
 	/** Creates a new ButtonPane for controlling the movies */
 	
	public ButtonPane(PlayerModule playerModule, MovieListView movieListView) {
 		this.playerModule = playerModule;		
		this.movieListView = movieListView;
 		
 		//create the buttons
 		butPlay = new Button("Play");
 		butPause = new Button("Pause");
 		butStop = new Button("Stop");
 		butSeekLeft = new Button("<");
 		butSeekLeftFast = new Button("<<");
 		butSeekRight = new Button(">");
 		butSeekRightFast = new Button(">>");		
 		
 		//set the width
 		butPlay.setPrefWidth(75);
 		butPause.setPrefWidth(75);
 		butStop.setPrefWidth(75);
 		butSeekLeft.setPrefWidth(75);
 		butSeekLeftFast.setPrefWidth(75);
 		butSeekRight.setPrefWidth(75);
 		butSeekRightFast.setPrefWidth(75);
 		
 		//add the listeners
 		butPlay.setOnAction(this);
 		butPause.setOnAction(this);
 		butStop.setOnAction(this);
 		butSeekLeft.setOnAction(this);
 		butSeekLeftFast.setOnAction(this);
 		butSeekRight.setOnAction(this);
 		butSeekRightFast.setOnAction(this);
 		
 		setPrefSize(100, 10);
 		setPadding(new Insets(15, 12, 15, 12));
 		setSpacing(10); // spacing between the children
 		
 		getChildren().add(butPlay);
 		getChildren().add(butPause);
 		getChildren().add(butStop);
 		getChildren().add(butSeekLeft);
 		getChildren().add(butSeekLeftFast);
 		getChildren().add(butSeekRight);
 		getChildren().add(butSeekRightFast);		
 		
 	}
 
 	public void handle(ActionEvent e) {
 		Object source = e.getSource();		
 		
 		if (source.equals(butPlay)) {
 			
 		}
 		else if (source.equals(butPause)) {
 			playerModule.pause();
 		}
 		else if (source.equals(butStop)) {
 			playerModule.stop();
 		}
 		else if (source.equals(butSeekLeft)) {
 			playerModule.seekBackwardSlow();
 		}
 		else if (source.equals(butSeekLeftFast)) {
 			playerModule.seekBackwardFast();
 		}
 		else if (source.equals(butSeekRight)) {
 			playerModule.seekForwardSlow();
 		}
 		else if (source.equals(butSeekRightFast)) {
 			playerModule.seekForwardFast();
 		}
 		
 	}
 }
