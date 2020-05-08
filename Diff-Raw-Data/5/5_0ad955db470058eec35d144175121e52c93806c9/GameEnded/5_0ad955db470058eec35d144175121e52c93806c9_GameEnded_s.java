 package sliceWars.logic.gameStates;
 
 import sliceWars.logic.Board;
 import sliceWars.logic.PlayOutcome;
 import sliceWars.logic.Player;
 import sliceWars.logic.gameStates.GameStateContextImpl.Phase;
 
 public class GameEnded implements GameState {
 
 	private Player _winner;
 
 	public GameEnded(Player winner) {
 		_winner = winner;
 	}
 	
 	@Override
 	public PlayOutcome play(int x, int y, GameStateContext gameStateContext){
 		return null;
 	}
 
 	@Override
 	public String getPhaseName() {
 		return "Player "+_winner.getPlayerNumber()+" won";
 	}
 
 	@Override
 	public Player getWhoIsPlaying() {
 		return _winner;
 	}
 
 	@Override
 	public boolean canPass() {
 		return false;
 	}
 
 	@Override
 	public PlayOutcome pass(GameStateContext gameStateContext){
 		return null;
 	}
 
 	@Override
 	public Phase getPhase(){
 		return Phase.GAME_ENDED;
 	}
 	
 	public static boolean checkIfWon(Player currentPlaying, Board board){
 		boolean allOtherPlayersHaveZeroTerritories = allOtherPlayersHaveZeroTerritories(currentPlaying, board);
 		boolean hasMoreThanFifthPercentOfTheBoard = hasMoreThanFifthPercentOfTheBoard(currentPlaying, board);
 		return allOtherPlayersHaveZeroTerritories || hasMoreThanFifthPercentOfTheBoard;
 	}
 
 	private static boolean hasMoreThanFifthPercentOfTheBoard(Player currentPlaying, Board board) {
		return (board.getCellCountForPlayer(currentPlaying)/currentPlaying.getPlayersCount()) > board.getValidCellsCount();
 	}
 
 	private static boolean allOtherPlayersHaveZeroTerritories(Player currentPlaying, Board board) {
 		Player nextPlayer = currentPlaying.next();
 		while(board.getBiggestLinkedCellCountForPlayer(nextPlayer) == 0){
 			nextPlayer = nextPlayer.next();
 			if(nextPlayer.equals(currentPlaying)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
