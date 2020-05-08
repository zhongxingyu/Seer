 package controllers.server;
 
 import controllers.WorkFlow;
 import model.Player;
 import org.junit.Before;
 import org.junit.Test;
 import views.server.GameView;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.stub;
 import static org.mockito.Mockito.verify;
 
 public class NightGameControllerTest {
 
     private WorkFlow workFlow;
     private Game game;
     private GameView view;
     private NightGameController controller;
     private List<Player> players;
 
     @Before
     public void setup(){
         workFlow=mock(WorkFlow.class);
         game=mock(Game.class);
         view=mock(GameView.class);
         players=new ArrayList<Player>();
         controller=NightGameController.createController(players,workFlow);
         controller.bind(view,game);
     }
 
     @Test
    public void start_will_call_display(){
         controller.start();
         verify(view).updatePlayersList (game.getPlayersChart ());
        verify (view).updateLog ("Night Arrived.");
     }
 
     @Test
     public void stop_server_will_go_to_home_screen(){
         controller.stopServer();
         verify(workFlow).goHome("");
     }
 
     @Test
     public void onVoteArrived_will_call_updateLog_in_view_and_sendNightVoteInfoMessage_in_Game(){
         final String votedTo = "Player2";
         final String voterName = "Player1";
         controller.onVoteArrived (voterName, votedTo);
         verify (view).updateLog (voterName + votedTo);
         verify (game).sendNightVotesInfoToMafias (voterName, votedTo);
     }
 
     @Test
     public void isEveryoneConfirmedThereVote_will_call_updateLog_in_GameView(){
         final String votedTo = "Player2";
         final String voterName = "Player1";
         controller.isEveryoneConfirmedThereVotes (voterName,votedTo);
         verify (view).updateLog (voterName+votedTo);
     }
 
     @Test
     public void sendEliminatedPlayerInfo_will_call_updateLog_in_GameView_and_sends_dayArrivedMessage_EliminatedPlayerInfo(){
         final String name = "Player";
         stub (game.removeEliminatedPlayer ()).toReturn (name);
         controller.sendEliminatedPlayerInfo ();
         verify (view).updateLog (name+" is dead.");
         verify (game).sendDayArrivedMessage ();
         verify (game).sendEliminatedPlayerInfo (name);
     }
 }
