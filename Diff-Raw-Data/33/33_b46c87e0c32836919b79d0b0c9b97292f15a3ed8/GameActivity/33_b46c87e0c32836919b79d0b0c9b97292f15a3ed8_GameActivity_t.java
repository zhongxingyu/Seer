 package com.hmml.knightswatch_android;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import com.hmml.api.ServerAsyncTask;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.Toast;
 
 
 public class GameActivity extends Activity {
 	
	final private static String DEFAULT_HOST = "192.168.1.116"; // "10.0.2.2";/
 	final private static int DEFAULT_PORT = 9999;
 	
 	private ArrayList<BoardCell> boardArrayList = new ArrayList<BoardCell>();
 	private GridView gridView;
 	private Boolean isPieceSelected = false;
 	private int selectedPosition;
 	private BoardCell.ECellPlayer player = BoardCell.ECellPlayer.Black;
 	private BoardCell.ECellPlayer oppositePlayer = BoardCell.ECellPlayer.White;
 	private BoardCell.ECellPlayer winningPlayer = BoardCell.ECellPlayer.None;
 	private ArrayList<Integer> selectedPieceMovePositions = new ArrayList<Integer>(); // Stores the movable positions 
 	private boolean isWatcher;
 	private Socket socket;
 	private ServerAsyncTask serverComms;
 	private ServerAsyncTask serverReceiver;
 	
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
 
         // Checks the orientation of the screen for landscape and portrait
         if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
             //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
         } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
             //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
         }
     }
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game);
 		
 		String pType = getIntent().getExtras().getString("type");
 		if(pType != null && pType.equalsIgnoreCase("watcher")){
 			isWatcher = true;
 		}else{
 			isWatcher = false;
 		}
 		
 		// init socket, 1 socket per GameActivity

 			
 			// create asynctask
 			serverComms = new ServerAsyncTask(this, socket);
 			
 			// start with requesting player type
 			serverComms.execute("first", pType);
 			
 			// set up receiver
 			serverReceiver = new ServerAsyncTask(this, socket);
 			serverReceiver.execute("receiver");
 			

 				
 	}
 
 	public void sendMoveToServer(int player, int move) {
 		// TODO : make sure move is validated before sending it
 		// both int player and int move are dummy fillers, implement as you wish
 		serverComms.execute("send", ""+move);
 	}
 	
 	public void receiveMoveFromServer(String result) {
 		// sync the move from the server
 		// if the move that we receive here is the same as the last move we sent to server, it means the server received our request
 		//   and we should not apply the move
 	}
 	
 	private void showNetworkError(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(R.string.network_error_text)
 		        .setCancelable(false)
 		        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 		            public void onClick(DialogInterface dialog, int id) {
 		            	dialog.dismiss();
 		            }
 	        	});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	
 	public void initGame(){
 		initBoard();
 		gridView = (GridView) findViewById(R.id.board_grid);
 		gridView.setAdapter(new GridViewBoardAdapter(this, boardArrayList));
 		gridView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				if (isPieceSelected){
 					makeMove(position);
 				}else{
 					v.setSelected(true);
 					checkMovesOnSelectedPiece(position);
 					highlightAvailableMoves();
 				}
 			}
 	    });
 	}
 	
 	private void initBoard(){		
 		BoardCell.ECellPlayer eCellPlayer = BoardCell.ECellPlayer.White;
 		for (int i = 0; i < 36; i++){
 			
 			if ( i > 16)
 				eCellPlayer = BoardCell.ECellPlayer.Black;
 			
 			if ( i == 1 || i == 4 || i == 31 || i == 34)
 				boardArrayList.add(i, new BoardCell( eCellPlayer, BoardCell.ECellType.Knight ));
 			else if ((i >= 6 && i <= 11) || (i >= 24 && i <= 29))
 				boardArrayList.add(i, new BoardCell( eCellPlayer , BoardCell.ECellType.Pawn ));
 			else
 				boardArrayList.add(i, new BoardCell( BoardCell.ECellPlayer.None , BoardCell.ECellType.None ));		
 		}
 	}
 	private void makeMove(int position) 
 	{
 		for (Integer i: selectedPieceMovePositions)
 		{
 			if (i == position) // If we find the position in the array then we know it's an acceptable position for the piece
 			{	
 				boardArrayList.set(position, boardArrayList.get(selectedPosition)); 
 				boardArrayList.set(selectedPosition, new BoardCell(BoardCell.ECellPlayer.None , BoardCell.ECellType.None)); // Set the new position of the piece to the one selected
 				checkIfPlayerWon(position);
 				isPieceSelected = false;
 				gridView.setAdapter(new GridViewBoardAdapter(this, boardArrayList));
 				break;
 			}
 			else if (boardArrayList.get(position).getPlayer() == player ) // if the player selects his own piece then we change the selected item if it has valid moves.
 			{
 				isPieceSelected = false;
 				checkMovesOnSelectedPiece(position);
 				break;
 			}
 		}
 		
 	}
 	private void checkIfPlayerWon(int position) 
 	{
 		if ( player ==  BoardCell.ECellPlayer.Black && position < 6 && boardArrayList.get(position).getType() == BoardCell.ECellType.Pawn )
 		{
 			winningPlayer =  BoardCell.ECellPlayer.Black;
 			Toast.makeText(getBaseContext(), winningPlayer + " has won!", Toast.LENGTH_SHORT).show();
 		}
 		else if ( player ==  BoardCell.ECellPlayer.White && position > 29 && boardArrayList.get(position).getType() == BoardCell.ECellType.Pawn )
 		{
 			winningPlayer =  BoardCell.ECellPlayer.White;
 			Toast.makeText(getBaseContext(), winningPlayer + " has won!", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	private void highlightAvailableMoves(){
 		// sets selected on all available positions
 		for(int i=0;i<selectedPieceMovePositions.size();i++){
 			gridView.getChildAt(selectedPieceMovePositions.get(i)).setActivated(true);
 		}
 	}
 	
 	private void checkMovesOnSelectedPiece(int position){
 		BoardCell currentCell = boardArrayList.get(position);
 		selectedPieceMovePositions.clear();
 		if (currentCell.getPlayer() == player)
 		{
 			if(currentCell.getType() == BoardCell.ECellType.Pawn)
 			{
 				int blackLeftOrWhiteRight;
 				int blackRightOrWhiteLeft;
 				int blackForwardOrWhiteForward;
 				if (currentCell.getPlayer() == BoardCell.ECellPlayer.Black)
 				{
 					blackLeftOrWhiteRight = -7; // Since the array goes from 0-35 we must go back in the array to check the cells as black is on the bottom,
 					blackRightOrWhiteLeft = -5; // 
 					blackForwardOrWhiteForward = -6;
 				}
 				else
 				{
 					blackLeftOrWhiteRight = 5; // Since white is on top we must go check cells ahead of the current one in the array
 					blackRightOrWhiteLeft = 7;
 					blackForwardOrWhiteForward = 6;
 				}		
 					if (position % 6 != 0 ) // Check if the pawn is at the first column
 					{
 						BoardCell temp = boardArrayList.get(position + blackLeftOrWhiteRight);
 						if (temp.getPlayer() == oppositePlayer) // checks if there is an enemy to the diagonal left of a black pawn or the diagonal right of a white pawn
 							selectedPieceMovePositions.add(position + blackLeftOrWhiteRight); // Store the positions so we can validate if the piece can move there when the player clicks a cell
 					}
 					if ((position + 1) % 6 != 0 ) // Checks if the pawn is at the last column
 					{
 						BoardCell temp =boardArrayList.get(position + blackRightOrWhiteLeft);
 						if (temp.getPlayer() == oppositePlayer) // checks if there is an enemy to the diagonal right of a black pawn or the diagonal left of a white pawn
 							selectedPieceMovePositions.add(position + blackRightOrWhiteLeft);
 					}
 					if (boardArrayList.get(position +  blackForwardOrWhiteForward).getType() == BoardCell.ECellType.None)
 						selectedPieceMovePositions.add(position + blackForwardOrWhiteForward); 
 					
 					if (selectedPieceMovePositions.size() > 0)// If there is something in the ray that means we can select the piece because it has a valid move
 					{
 						isPieceSelected = true;
 						selectedPosition = position;
 					}
 			}
 			else if (currentCell.getType() == BoardCell.ECellType.Knight)
 			{
 				
 				if ( position > 11 ) // Checks if the knight can move up 2
 				{
 					if (position % 6 != 0 && (boardArrayList.get(position -13 ).getType() == BoardCell.ECellType.None || boardArrayList.get(position-13).getPlayer() == oppositePlayer )) // checks if the knight can move up 2 left 1
 						selectedPieceMovePositions.add(position - 13);
 					if ((position + 1) % 6 != 0 && (boardArrayList.get(position -11 ).getType() == BoardCell.ECellType.None || boardArrayList.get(position-11).getPlayer() == oppositePlayer )) // checks if the knight can move up 2 right 1
 						selectedPieceMovePositions.add(position - 11);
 				}
 				if  (position < 24) // Checks if the knight can move down 2
 				{
 					if (position % 6 != 0 && (boardArrayList.get(position + 11 ).getType() == BoardCell.ECellType.None || boardArrayList.get(position+11).getPlayer() == oppositePlayer )) // checks if the knight can move down 2 left 1
 						selectedPieceMovePositions.add(position + 11);
 					if ((position + 1) % 6 != 0 && (boardArrayList.get(position + 13 ).getType() == BoardCell.ECellType.None || boardArrayList.get(position+13).getPlayer() == oppositePlayer )) // checks if the knight can move down 2 right 1
 						selectedPieceMovePositions.add(position + 13);
 				}
 				if (position % 6 != 0 && (position + 5 ) % 6 != 0) // Checks if the knight can move left 2
 				{
 					if (position >= 6  && (boardArrayList.get(position - 8 ).getType() == BoardCell.ECellType.None || boardArrayList.get(position- 8).getPlayer() == oppositePlayer )) // checks if the knight can move left 2 up 1
 						selectedPieceMovePositions.add(position - 8);
 					if (position <= 29  && (boardArrayList.get(position + 4 ).getType() == BoardCell.ECellType.None || boardArrayList.get(position + 4).getPlayer() == oppositePlayer )) // checks if the knight can move left 2 down 1
 						selectedPieceMovePositions.add(position + 4);
 				}
 				if ((position + 2) % 6 != 0 && (position + 1 ) % 6 != 0) // Checks if the knight can move right 2
 				{
 					if (position >= 6  && (boardArrayList.get(position - 4 ).getType() == BoardCell.ECellType.None || boardArrayList.get(position- 4).getPlayer() == oppositePlayer )) // checks if the knight can move right 2 up 1
 						selectedPieceMovePositions.add(position - 4);
 					if (position <= 29  && (boardArrayList.get(position + 8 ).getType() == BoardCell.ECellType.None || boardArrayList.get(position + 8).getPlayer() == oppositePlayer )) // checks if the knight can move right 2 down 1
 						selectedPieceMovePositions.add(position + 8);
 				}
 				if (selectedPieceMovePositions.size() > 0)// If there is something in the ray that means we can select the piece because it has a valid move
 				{
 					isPieceSelected = true;
 					selectedPosition = position;
 				}
 				
 			}
 		}
 				
 	}
 	
 }
