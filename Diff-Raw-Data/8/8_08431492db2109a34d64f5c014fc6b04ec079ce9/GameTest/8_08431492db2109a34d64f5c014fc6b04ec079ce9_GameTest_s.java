 import org.junit.*;
 import static org.junit.Assert.*;
 
 import org.hexgame.core.PieceType;
 import org.hexgame.core.Player;
 import org.hexgame.core.Move;
 import org.hexgame.core.Game;
 
 public class GameTest {
   Game game;
   Player playerA, playerB;
 
   @Before
   public void setUp() {
     playerA = new Player(PieceType.WHITE);
     playerB = new Player(PieceType.BLACK);
     game = new Game(4, playerA, playerB);
   }
 
   @Test
   public void testInitializing() {
     assertEquals(game.getBoard().getSize(), 4);
     assertEquals(game.getPlayerA(), playerA);
     assertEquals(game.getPlayerB(), playerB);
   }
 
   @Test
   public void testPerformingMove() {
     game.performMove(new Move(game.getBoard(), playerA, 1, 1));
     assertEquals(game.getBoard().getPiece(1, 1), PieceType.WHITE);
   }
 
   @Test
   public void testCurrentPlayerAtBeginningOfGame() {
     assertEquals(game.currentPlayer(), playerA);
   }
 
   @Test
   public void testCurrentPlayerAfterOneMove() {
     game.performMove(new Move(game.getBoard(), playerA, 1, 1));
     assertEquals(game.currentPlayer(), playerB);
   }
 
   @Test
   public void testCurrentPlayerAfterSeveralMoves() {
     game.performMove(new Move(game.getBoard(), playerA, 1, 1));
     game.performMove(new Move(game.getBoard(), playerB, 2, 1));
     assertEquals(game.currentPlayer(), playerA);
   }
 
   @Test
   public void testUndoMove() {
     game.performMove(new Move(game.getBoard(), playerA, 1, 1));
     game.performMove(new Move(game.getBoard(), playerB, 2, 1));
     game.performMove(new Move(game.getBoard(), playerA, 2, 2));
    game.undoMove();
     assertEquals(game.getBoard().getPiece(1, 1), PieceType.WHITE);
     assertEquals(game.getBoard().getPiece(2, 1), PieceType.BLACK);
     assertEquals(game.getBoard().getPiece(2, 2), null);
   }
 
   @Test(expected = RuntimeException.class)
   public void testUndoMoveWhenNoMoves() {
    game.undoMove();
   }
 
   @Test
   public void testUndoAndCurrentPlayer() {
     game.performMove(new Move(game.getBoard(), playerA, 1, 1));
     game.performMove(new Move(game.getBoard(), playerB, 2, 1));
     game.performMove(new Move(game.getBoard(), playerA, 2, 2));
    game.undoMove();
     assertEquals(game.currentPlayer(), playerA);
   }
 
   @Test
   public void testIsNotFinished() {
     assertFalse(game.isFinished());
   }
 
   @Test
   public void testIsFinishedWhenBoardIsFull() {
     game.performMove(new Move(game.getBoard(), playerA, 1, 1));
     game.performMove(new Move(game.getBoard(), playerB, 2, 1));
     game.performMove(new Move(game.getBoard(), playerA, 2, 2));
     game.performMove(new Move(game.getBoard(), playerB, 3, 1));
     game.performMove(new Move(game.getBoard(), playerA, 3, 2));
     game.performMove(new Move(game.getBoard(), playerB, 3, 3));
     game.performMove(new Move(game.getBoard(), playerA, 4, 1));
     game.performMove(new Move(game.getBoard(), playerB, 4, 2));
     game.performMove(new Move(game.getBoard(), playerA, 4, 3));
     game.performMove(new Move(game.getBoard(), playerB, 4, 4));
     game.performMove(new Move(game.getBoard(), playerA, 5, 2));
     game.performMove(new Move(game.getBoard(), playerB, 5, 3));
     game.performMove(new Move(game.getBoard(), playerA, 5, 4));
     game.performMove(new Move(game.getBoard(), playerB, 6, 3));
     game.performMove(new Move(game.getBoard(), playerA, 6, 4));
     game.performMove(new Move(game.getBoard(), playerB, 7, 4));
     assertTrue(game.isFinished());
   }
 
   @Test
   public void testWhiteWon() {
     game.performMove(new Move(game.getBoard(), playerA, 3, 1));
     game.performMove(new Move(game.getBoard(), playerB, 1, 1));
     game.performMove(new Move(game.getBoard(), playerA, 3, 2));
     game.performMove(new Move(game.getBoard(), playerB, 4, 2));
     game.performMove(new Move(game.getBoard(), playerA, 4, 3));
     game.performMove(new Move(game.getBoard(), playerB, 5, 3));
     game.performMove(new Move(game.getBoard(), playerA, 5, 4));
     assertTrue(game.isFinished());
     assertEquals(game.getWinner(), playerA);
   }
 
   @Test
   public void testBlackWon() {
     game.performMove(new Move(game.getBoard(), playerA, 2, 2));
     game.performMove(new Move(game.getBoard(), playerB, 1, 1));
     game.performMove(new Move(game.getBoard(), playerA, 3, 3));
     game.performMove(new Move(game.getBoard(), playerB, 2, 1));
     game.performMove(new Move(game.getBoard(), playerA, 4, 4));
     game.performMove(new Move(game.getBoard(), playerB, 3, 2));
     game.performMove(new Move(game.getBoard(), playerA, 4, 1));
     game.performMove(new Move(game.getBoard(), playerB, 4, 3));
     game.performMove(new Move(game.getBoard(), playerA, 4, 2));
     game.performMove(new Move(game.getBoard(), playerB, 5, 3));
     game.performMove(new Move(game.getBoard(), playerA, 6, 4));
     game.performMove(new Move(game.getBoard(), playerB, 6, 3));
     assertTrue(game.isFinished());
     assertEquals(game.getWinner(), playerB);
   }
 }
