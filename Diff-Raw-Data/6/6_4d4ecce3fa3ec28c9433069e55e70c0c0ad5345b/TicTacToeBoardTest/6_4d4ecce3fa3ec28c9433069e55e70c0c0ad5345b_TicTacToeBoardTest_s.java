 package TicTacToe;
 
 import TicTacToe.TicTacToeBoard;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 import static TicTacToe.BoardMarker.*;
 
 public class TicTacToeBoardTest {
     TicTacToeBoard board;
     BoardMarker[][] boardState;
     TicTacToeBoard board4;
     BoardMarker[][] boardState4;
 
     @Before
     public void initialize() {
         board = new TicTacToeBoard(3, 2);
         boardState = new BoardMarker[][]{{_, _, _}, {_, _, _}, {_, _, _}};
         board4 = new TicTacToeBoard(4, 2);
         boardState4 = new BoardMarker[][]{{_, _, _, _}, {_, _, _, _}, {_, _, _, _}, {_, _, _, _}};
     }
 
     @Test
     public void shouldHaveAnArrayOfNegativeOnesWhenConstructed() {
         assertArrayEquals(boardState, board.getState());
     }
 
     @Test
     public void shouldChangeCell_0_WhenAMoveIsMadeByPlayer_0() {
         boardState[0][0] = X;
         board.makeMove(0, 0, X);
         assertArrayEquals(boardState, board.getState());
     }
 
     @Test
     public void shouldChangeCell_0_WhenAMoveIsMadeByPlayer_1() {
         boardState[0][0] = O;
         board.makeMove(0, 0, O);
         assertArrayEquals(boardState, board.getState());
     }
 
     @Test
     public void shouldChangeCell_1_WhenAMoveIsMadeByPlayer_0() {
         boardState[0][1] = X;
         board.makeMove(0, 1, X);
         assertArrayEquals(boardState, board.getState());
     }
 
     @Test
     public void shouldChangeCell_8_WhenAMoveIsMadeByPlayer_0() {
         boardState[2][2] = X;
         board.makeMove(2, 2, X);
         assertArrayEquals(boardState, board.getState());
     }
 
     @Test
     public void shouldChangeCell_7_WhenAMoveIsMadeByPlayer_1() {
         boardState[2][1] = O;
         board.makeMove(2, 1, O);
         assertArrayEquals(boardState, board.getState());
     }
 
     @Test
     public void shouldChangeCell_048_WhenAMoveIsMadeByPlayer_010() {
         boardState[0][0] = X;
         boardState[1][1] = O;
         boardState[2][2] = X;
         board.makeMove(0, 0, X);
         board.makeMove(1, 1, O);
         board.makeMove(2, 2, X);
         assertArrayEquals(boardState, board.getState());
     }
 
     @Test
     public void shouldReturnTheFirstRow() {
         assertArrayEquals(new BoardMarker[]{_, _, _}, board.getRow(0));
     }
 
     @Test
     public void shouldReturnTheFirstRowAfterMove() {
         board.makeMove(0, 1, X);
         assertArrayEquals(new BoardMarker[]{_, X, _}, board.getRow(0));
     }
 
     @Test
     public void shouldReturnTheSecondRowAfterMove() {
         board.makeMove(1, 1, O);
         assertArrayEquals(new BoardMarker[]{_, O, _}, board.getRow(1));
     }
 
     @Test
     public void shouldReturnTheThirdRowAfterMove() {
         board.makeMove(2, 0, X);
         board.makeMove(2, 2, O);
         assertArrayEquals(new BoardMarker[]{X, _, O}, board.getRow(2));
     }
 
     @Test
     public void shouldReturnTheFirstColumn() {
         assertArrayEquals(new BoardMarker[]{_, _, _}, board.getColumn(0));
     }
 
     @Test
     public void shouldReturnTheFirstColumnAfterMove() {
         board.makeMove(1, 0, X);
         assertArrayEquals(new BoardMarker[]{_, X, _}, board.getColumn(0));
     }
 
     @Test
     public void shouldReturnTheSecondColumnAfterMove() {
         board.makeMove(1, 1, O);
         assertArrayEquals(new BoardMarker[]{_, O, _}, board.getColumn(1));
     }
 
     @Test
     public void shouldReturnTheThirdColumnAfterMove() {
         board.makeMove(0, 2, X);
         board.makeMove(2, 2, O);
         assertArrayEquals(new BoardMarker[]{X, _, O}, board.getColumn(2));
     }
 
     @Test
     public void shouldReturnTheForwardDiagonal() {
         assertArrayEquals(new BoardMarker[]{_, _, _}, board.getForwardDiagonal());
     }
 
     @Test
     public void shouldReturnTheForwardDiagonalAfterMove() {
         board.makeMove(1, 1, X);
         assertArrayEquals(new BoardMarker[]{_, X, _}, board.getForwardDiagonal());
     }
 
     @Test
     public void shouldReturnTheForwardDiagonalAfterAnotherMove() {
         board.makeMove(1, 1, O);
         assertArrayEquals(new BoardMarker[]{_, O, _}, board.getForwardDiagonal());
     }
 
     @Test
     public void shouldReturnTheForwardDiagonalAfterADifferentMove() {
         board.makeMove(0, 0, X);
         board.makeMove(2, 2, O);
         assertArrayEquals(new BoardMarker[]{X, _, O}, board.getForwardDiagonal());
     }
 
     @Test
     public void shouldReturnTheBackwardDiagonal() {
         assertArrayEquals(new BoardMarker[]{_, _, _}, board.getBackwardDiagonal());
     }
 
     @Test
     public void shouldReturnTheBackwardDiagonalAfterMove() {
         board.makeMove(1, 1, X);
         assertArrayEquals(new BoardMarker[]{_, X, _}, board.getBackwardDiagonal());
     }
 
     @Test
     public void shouldReturnTheBackwardDiagonalAfterAnotherMove() {
         board.makeMove(1, 1, O);
         assertArrayEquals(new BoardMarker[]{_, O, _}, board.getBackwardDiagonal());
     }
 
     @Test
     public void shouldReturnTheBackwardDiagonalAfterADifferentMove() {
         board.makeMove(0, 2, X);
         board.makeMove(2, 0, O);
         assertArrayEquals(new BoardMarker[]{X, _, O}, board.getBackwardDiagonal());
     }
 
     @Test
     public void defaultArrayDoesNotHaveWinner() {
         assertFalse(board.hasWinner(new BoardMarker[]{_, _, _}));
     }
 
     @Test
     public void arrayStartingWithNegOneDoesNotHaveWinner() {
         assertFalse(board.hasWinner(new BoardMarker[]{_, X, X}));
     }
 
     @Test
     public void arrayEndingWithNegOneDoesNotHaveWinner() {
         assertFalse(board.hasWinner(new BoardMarker[]{O, O, _}));
     }
 
     @Test
     public void arrayWithZerosAndOnesDoesNotHaveWinner() {
         assertFalse(board.hasWinner(new BoardMarker[]{O, O, X}));
     }
 
     @Test
     public void arrayOfOnesIsWinner() {
         assertTrue(board.hasWinner(new BoardMarker[]{O, O, O}));
     }
 
     @Test
     public void arrayOfZerosIsWinner() {
         assertTrue(board.hasWinner(new BoardMarker[]{X, X, X}));
     }
 
     @Test
     public void initiallyBoardHasNoWinner() {
         assertEquals(_, board.winner());
     }
 
     @Test
     public void boardWithFirstRowZerosHasWinner() {
         board.setState(new BoardMarker[][]{{X, X, X}, {_, _, _}, {_, _, _}});
         assertEquals(X, board.winner());
     }
 
     @Test
     public void boardWithTwoOnesHasNoWinner() {
         board.setState(new BoardMarker[][]{{_, O, O}, {_, _, _}, {_, _, _}});
         assertEquals(_, board.winner());
     }
 
     @Test
     public void boardWithFirstColumnZerosHasWinner() {
         board.setState(new BoardMarker[][]{{X, _, _}, {X, _, _}, {X, _, _}});
         assertEquals(X, board.winner());
     }
 
     @Test
     public void boardWithSecondColumnZerosHasWinner() {
         board.setState(new BoardMarker[][]{{_, X, _}, {_, X, _}, {_, X, _}});
         assertEquals(X, board.winner());
     }
 
     @Test
     public void boardWithSecondRowZerosHasWinner() {
         board.setState(new BoardMarker[][]{{_, _, _}, {X, X, X}, {_, _, _}});
         assertEquals(X, board.winner());
     }
 
     @Test
     public void boardWithThirdColumnOnesHasWinner() {
         board.setState(new BoardMarker[][]{{_, _, O}, {_, _, O}, {_, _, O}});
         assertEquals(O, board.winner());
     }
 
     @Test
     public void boardWithThirdRowOnesHasWinner() {
         board.setState(new BoardMarker[][]{{_, _, _}, {_, _, _}, {O, O, O}});
         assertEquals(O, board.winner());
     }
 
     @Test
     public void boardWithForwardDiagonalOnesHasWinner() {
         board.setState(new BoardMarker[][]{{_, _, O}, {_, O, _}, {O, _, _}});
         assertEquals(O, board.winner());
     }
 
     @Test
     public void boardWithForwardDiagonalZerosHasWinner() {
         board.setState(new BoardMarker[][]{{_, _, X}, {_, X, _}, {X, _, _}});
         assertEquals(X, board.winner());
     }
 
     @Test
     public void boardWithBackwardDiagonalOnesHasWinner() {
         board.setState(new BoardMarker[][]{{O, _, _}, {_, O, _}, {_, _, O}});
         assertEquals(O, board.winner());
     }
 
     @Test
     public void boardWithBackwardDiagonalZerosHasWinner() {
         board.setState(new BoardMarker[][]{{X, _, _}, {_, X, _}, {_, _, X}});
         assertEquals(X, board.winner());
     }
 
     @Test
     public void boardTieHasNoWinner() {
         board.setState(new BoardMarker[][]{{X, X, O}, {O, O, X}, {X, O, X}});
         assertEquals(T, board.winner());
     }
 
     // 4x4 tests
     @Test
     public void _4x4shouldHaveAnArrayOfNegativeOnesWhenConstructed() {
         assertArrayEquals(boardState4, board4.getState());
     }
 
     @Test
     public void _4x4shouldChangeCell_15_WhenAMoveIsMadeByPlayer_0() {
         boardState4[3][3] = X;
         board4.makeMove(3, 3, X);
         assertArrayEquals(boardState4, board4.getState());
     }
 
     @Test
     public void _4x4shouldChangeCell_15_WhenAMoveIsMadeByPlayer_1() {
         boardState4[3][3] = O;
         board4.makeMove(3, 3, O);
         assertArrayEquals(boardState4, board4.getState());
     }
 
     @Test
     public void _4x4shouldReturnTheFirstRow() {
         assertArrayEquals(new BoardMarker[]{_, _, _, _}, board4.getRow(0));
     }
 
     @Test
     public void _4x4shouldReturnTheFirstRowAfterMove() {
         board4.makeMove(0, 1, X);
         assertArrayEquals(new BoardMarker[]{_, X, _, _}, board4.getRow(0));
     }
 
     @Test
     public void _4x4shouldReturnTheSecondRowAfterMove() {
         board4.makeMove(1, 1, O);
         assertArrayEquals(new BoardMarker[]{_, O, _, _}, board4.getRow(1));
     }
 
     @Test
     public void _4x4shouldReturnTheFourthRowAfterMove() {
         board4.makeMove(3, 0, X);
         board4.makeMove(3, 3, O);
         assertArrayEquals(new BoardMarker[]{X, _, _, O}, board4.getRow(3));
     }
 
     @Test
     public void _4x4shouldReturnTheFirstColumn() {
         assertArrayEquals(new BoardMarker[]{_, _, _, _}, board4.getColumn(0));
     }
 
     @Test
     public void _4x4shouldReturnTheFirstColumnAfterMove() {
         board4.makeMove(3, 0, X);
         assertArrayEquals(new BoardMarker[]{_, _, _, X}, board4.getColumn(0));
     }
 
     @Test
     public void _4x4shouldReturnTheSecondColumnAfterMove() {
         board4.makeMove(1, 1, O);
         assertArrayEquals(new BoardMarker[]{_, O, _, _}, board4.getColumn(1));
     }
 
     @Test
     public void _4x4shouldReturnTheFourthColumnAfterMove() {
         board4.makeMove(0, 3, X);
         board4.makeMove(3, 3, O);
         assertArrayEquals(new BoardMarker[]{X, _, _, O}, board4.getColumn(3));
     }
 
     @Test
     public void _4x4shouldReturnTheForwardDiagonal() {
         assertArrayEquals(new BoardMarker[]{_, _, _, _}, board4.getForwardDiagonal());
     }
 
     @Test
     public void _4x4shouldReturnTheForwardDiagonalAfterMove() {
         board4.makeMove(1, 1, X);
         assertArrayEquals(new BoardMarker[]{_, X, _, _}, board4.getForwardDiagonal());
     }
 
     @Test
     public void _4x4shouldReturnTheForwardDiagonalAfterAnotherMove() {
         board4.makeMove(1, 1, O);
         assertArrayEquals(new BoardMarker[]{_, O, _, _}, board4.getForwardDiagonal());
     }
 
     @Test
     public void _4x4shouldReturnTheForwardDiagonalAfterADifferentMove() {
         board4.makeMove(0, 0, X);
         board4.makeMove(3, 3, O);
         assertArrayEquals(new BoardMarker[]{X, _, _, O}, board4.getForwardDiagonal());
     }
 
     @Test
     public void _4x4shouldReturnTheBackwardDiagonal() {
         assertArrayEquals(new BoardMarker[]{_, _, _, _}, board4.getBackwardDiagonal());
     }
 
     @Test
     public void _4x4shouldReturnTheBackwardDiagonalAfterMove() {
         board4.makeMove(1, 2, X);
         assertArrayEquals(new BoardMarker[]{_, X, _, _}, board4.getBackwardDiagonal());
     }
 
     @Test
     public void _4x4shouldReturnTheBackwardDiagonalAfterAnotherMove() {
         board4.makeMove(1, 2, O);
         assertArrayEquals(new BoardMarker[]{_, O, _, _}, board4.getBackwardDiagonal());
     }
 
     @Test
     public void _4x4shouldReturnTheBackwardDiagonalAfterADifferentMove() {
         board4.makeMove(0, 3, X);
         board4.makeMove(3, 0, O);
         assertArrayEquals(new BoardMarker[]{X, _, _, O}, board4.getBackwardDiagonal());
     }
 
     @Test
     public void _4x4defaultArrayDoesNotHaveWinner() {
         assertFalse(board4.hasWinner(new BoardMarker[]{_, _, _, _}));
     }
 
     @Test
     public void _4x4arrayStartingWithNegOneDoesNotHaveWinner() {
         assertFalse(board4.hasWinner(new BoardMarker[]{_, X, X, X}));
     }
 
     @Test
     public void _4x4arrayEndingWithNegOneDoesNotHaveWinner() {
         assertFalse(board4.hasWinner(new BoardMarker[]{O, O, O, _}));
     }
 
     @Test
     public void _4x4arrayWithZerosAndOnesDoesNotHaveWinner() {
         assertFalse(board4.hasWinner(new BoardMarker[]{O, O, X, X}));
     }
 
     @Test
     public void _4x4arrayOfOnesIsWinner() {
         assertTrue(board4.hasWinner(new BoardMarker[]{O, O, O, O}));
     }
 
     @Test
     public void _4x4arrayOfZerosIsWinner() {
         assertTrue(board4.hasWinner(new BoardMarker[]{X, X, X, X}));
     }
 
     @Test
     public void _4x4initiallyBoardHasNoWinner() {
         assertEquals(_, board4.winner());
     }
 
     @Test
     public void _4x4boardWithFirstRowZerosHasWinner() {
         board4.setState(new BoardMarker[][]{{X, X, X, X}, {_, _, _, _}, {_, _, _, _}});
         assertEquals(X, board4.winner());
     }
 
     @Test
     public void _4x4boardWithFourthColumnOnesHasWinner() {
         board4.setState(new BoardMarker[][]{{_, _, _, O}, {_, _, _, O}, {_, _, _, O}, {_, _, _, O}});
         assertEquals(O, board4.winner());
     }
 
     @Test
     public void _4x4boardWithFourthRowOnesHasWinner() {
         board4.setState(new BoardMarker[][]{{_, _, _, _}, {_, _, _, _}, {_, _, _, _}, {O, O, O, O}});
         assertEquals(O, board4.winner());
     }
 
     @Test
     public void _4x4boardWithForwardDiagonalOnesHasWinner() {
         board4.setState(new BoardMarker[][]{{_, _, _, O}, {_, _, O, _}, {_, O, _, _}, {O, _, _, _}});
         assertEquals(O, board4.winner());
     }
 
     @Test
     public void _4x4boardWithBackwardDiagonalOnesHasWinner() {
         board.setState(new BoardMarker[][]{{O, _, _, _}, {_, O, _, _}, {_, _, O, _}, {_, _, _, O}});
         assertEquals(O, board.winner());
     }
 
     @Test
     public void shouldNotHaveFullBoard(){
         assertFalse(board.full());
         board.setState(new BoardMarker[][]{{X, O, X}, {_, O, O}, {O, X, X}});
         assertFalse(board.full());
     }
 
     @Test
     public void shouldHaveFullBoard(){
         board.setState(new BoardMarker[][]{{X, O, X}, {X, O, O}, {O, X, X}});
         assertTrue(board.full());
     }
 
     @Test
     public void shouldConstructEmptyBoardFromString() {
         TicTacToeBoard b = new TicTacToeBoard("_________");
         assertEquals(boardState, b.getState());
     }
 
     @Test
     public void _4x4shouldConstructEmptyBoardFromString() {
         TicTacToeBoard b = new TicTacToeBoard("________________");
         assertEquals(boardState4, b.getState());
     }
 
     @Test
     public void shouldConstructBoardFromString() {
         TicTacToeBoard b = new TicTacToeBoard("Xo__OOx_O");
         boardState = new BoardMarker[][]{{X, O, _}, {_, O, O}, {X, _, O}};
         assertEquals(boardState, b.getState());
     }
 
     @Test
     public void _4x4shouldConstructBoardFromString() {
         TicTacToeBoard b = new TicTacToeBoard("XOo___OOx_xOo_XO");
         boardState4 = new BoardMarker[][]{{X, O, O, _}, {_, _, O, O}, {X, _, X, O}, {O, _, X, O}};
         assertEquals(boardState4, b.getState());
     }
 
     @Test
     public void shouldGetEmptyStateString() {
         assertEquals("_________", board.getBoardStateString());
     }
 
     @Test
     public void _4x4shouldGetEmptyStateString() {
         assertEquals("________________", board4.getBoardStateString());
     }
 
     @Test
     public void shouldGetStateStringForMoreComplicatedState() {
         board.setState(new BoardMarker[][]{{X, O, _}, {_, O, O}, {X, _, O}});
         assertEquals("XO__OOX_O", board.getBoardStateString());
     }
 
     @Test
     public void _4x4shouldGetStateStringForMoreComplicatedState() {
         board4.setState(new BoardMarker[][]{{X, O, O, _}, {_, _, O, O}, {X, _, X, O}, {O, _, X, O}});
         assertEquals("XOO___OOX_XOO_XO", board4.getBoardStateString());
     }
 
     @Test
     public void shouldHaveStringRepresentingBoardState() {
         String s = board.toWebString("X");
         for(int i = 0; i < 9; i++){
            assertThat(s, org.junit.matchers.JUnitMatchers.containsString("id=cell" + i));
         }
     }
 }
