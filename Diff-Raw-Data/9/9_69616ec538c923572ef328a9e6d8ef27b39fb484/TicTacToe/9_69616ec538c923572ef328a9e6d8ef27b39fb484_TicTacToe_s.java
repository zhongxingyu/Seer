 package game;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class TicTacToe implements GenGame {
 
 	private int board[][] = new int[3][3];
 	private int whoseTurn = 0;
 
 	public TicTacToe() {
 		System.out.println("TicTacToe Game Running");
 		for (int i = 0; i < 3; i++) {
 			for (int j = 0; j < 3; j++) {
 				board[i][j] = -1;
 			}
 		}
 	}
 
 	@Override
 	public JSONObject getStatus() {
 
 		JSONArray rBoard = new JSONArray();
 		for (int i = 0; i < 3; i++) {
 			JSONArray collum = new JSONArray();
 			for (int j = 0; j < 3; j++) {
 				System.out.print(board[i][j] + " ");
 				collum.put(board[i][j]);
 			}
 			System.out.println("");
 			rBoard.put(collum);
 		}

		return new JSONObject(rBoard);
 	}
 
 	@Override
 	public JSONObject runCommand(JSONObject input) {
 		int id = -1;
 		int x = -1;
 		int y = -1;
 		boolean won = false;
 
 		try {
 			id = Integer.parseInt(input.get("id").toString());
 			x = Integer.parseInt(input.get("x").toString());
 			y = Integer.parseInt(input.get("y").toString());
 		} catch (JSONException e) {
 			// the input is malformed
 			System.out.println("Malformed input JSON");
 		}
 
 		if (whoseTurn == id) {
 			if (checkMove(id, x, y)) {
 				doMove(id, x, y);
 				if (checkWin(id)) {
 					System.out.println("player " + id + " won!");
 					won = true;
 				}
 			}
 		}
 
 		JSONObject rWhat = new JSONObject();
 		try {
 			rWhat.append("error", 0);
 			rWhat.append("won", won);
 			rWhat.append("x", x);
 			rWhat.append("y", y);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			System.out.println("Error creating return object");
 		}
 		return rWhat;
 	}
 
 	private boolean checkWin(int id) {
 		if (board[0][0] == id && board[1][0] == id && board[2][0] == id)
 			return true;
 		if (board[0][1] == id && board[1][1] == id && board[2][1] == id)
 			return true;
 		if (board[0][2] == id && board[1][2] == id && board[2][2] == id)
 			return true;
 
 		if (board[0][0] == id && board[0][1] == id && board[0][2] == id)
 			return true;
 		if (board[1][0] == id && board[1][1] == id && board[1][2] == id)
 			return true;
 		if (board[2][0] == id && board[2][1] == id && board[2][2] == id)
 			return true;
 
 		if (board[0][0] == id && board[1][1] == id && board[2][2] == id)
 			return true;
 		if (board[0][2] == id && board[1][1] == id && board[2][0] == id)
 			return true;
 
 		return false;
 	}
 
 	private void doMove(int id, int x, int y) {
 		board[x][y] = id;
 		if (whoseTurn == 1) {
 			whoseTurn = 0;
 		} else {
 			if (whoseTurn == 0)
 				whoseTurn = 1;
 		}
 	}
 
 	private boolean checkMove(int id, int x, int y) {
 		return (board[x][y] == -1);
 	}
 
 }
