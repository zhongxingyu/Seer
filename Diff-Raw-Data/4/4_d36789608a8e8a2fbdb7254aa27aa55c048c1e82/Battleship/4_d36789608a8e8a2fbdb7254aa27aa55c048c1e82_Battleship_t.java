 /**
  * This is free and unencumbered software released into the public domain.
  * 
  * Anyone is free to copy, modify, publish, use, compile, sell, or distribute
  * this software, either in source code form or as a compiled binary, for any
  * purpose, commercial or non-commercial, and by any means.
  * 
  * In jurisdictions that recognize copyright laws, the author or authors of this
  * software dedicate any and all copyright interest in the software to the
  * public domain. We make this dedication for the benefit of the public at large
  * and to the detriment of our heirs and successors. We intend this dedication
  * to be an overt act of relinquishment in perpetuity of all present and future
  * rights to this software under copyright law.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  * 
  * For more information, please refer to [http://unlicense.org]
  */
 package ch.bfh.bti7301.w2013.battleship;
 
 import java.util.ResourceBundle;
 
 import ch.bfh.bti7301.w2013.battleship.game.Game;
 import javafx.application.Application;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.paint.Color;
 import javafx.scene.transform.Scale;
 import javafx.stage.Stage;
 import ch.bfh.bti7301.w2013.battleship.game.Board;
 import ch.bfh.bti7301.w2013.battleship.game.Game;
 import ch.bfh.bti7301.w2013.battleship.gui.BoardView;
 
 /**
  * @author Christian Meyer <chrigu.meyer@gmail.com>
  * 
  */
 public class Battleship extends Application {
 	private ResourceBundle labels;
 
 	private Game game;
 
 	public Battleship() {
 		labels = ResourceBundle.getBundle("translations");
 		game = new Game();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		Game g = new Game();
 
 		launch(args);
 	}
 
 	@Override
 	public void start(Stage primaryStage) {
 		primaryStage.setTitle(labels.getString("title"));
 
 		Group root = new Group();
 		Scene scene = new Scene(root, 800, 600, Color.YELLOW);
 		primaryStage.setScene(scene);
 
 		Board playerBoard = game.getLocalPlayer().getBoard();
 		BoardView pbv = new BoardView(playerBoard);
 		pbv.relocate(10, 10);
 		root.getChildren().add(pbv);
 
 		Board opponentBoard = game.getOpponent().getBoard();
 		BoardView obv = new BoardView(opponentBoard);
 		obv.getTransforms().add(new Scale(0.5, 0.5, 0, 0));
 
 		obv.relocate(pbv.getBoundsInParent().getMaxX() + 20, 10);
 		root.getChildren().add(obv);
 
 		primaryStage.show();
 	}
 }
