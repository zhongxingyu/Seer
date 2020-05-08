 package edu.touro.cooptetris;
 
 import java.awt.BorderLayout;
 import java.io.IOException;
 
 import javax.inject.Inject;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 
 import edu.touro.cooptetris.pieces.Piece;
 import edu.touro.cooptetris.pieces.Square;
 import edu.touro.cooptetris.sound.CompleteLineMusicPlayer;
 import edu.touro.cooptetris.sound.HitFloorMusicPlayer;
 import edu.touro.cooptetris.sound.LevelChangeMusicPlayer;
 import edu.touro.cooptetris.sound.RotateMusicPlayer;
 
 public class TetrisMain extends JFrame implements GameStateListener {
 
 	private static final long serialVersionUID = 1L;
 	private GameController gameController;
 	private KeyboardListener keyboardListener;
 	private ScoreLevelNextPieceDisplay scoreLevelDisplay;
 
 	public static void main(String[] args) {
 		Injector injector = Guice.createInjector(new Module[0]);
 		injector.getInstance(TetrisMain.class);
 	}
 
 	@Inject
 	public TetrisMain(final PiecesAndBoardView gameView,
 			ScoreLevelNextPieceDisplay scoreLevelDisplay,
 			final GameController gameController) {
 		this.gameController = gameController;
 		this.keyboardListener = new KeyboardListener(gameController.getBoard());
 		this.scoreLevelDisplay = scoreLevelDisplay;
 		gameView.addKeyListener(keyboardListener);
 		keyboardListener.setGameStateListener(this);
 
 		gameController.setGameStateListener(this);
 		gameController.addNewPiece();
 		scoreLevelDisplay.setPiece(gameController.getNextPiece());
 		int height = scoreLevelDisplay.getHeight() + 30, width = 100
 				+ Board.NUM_COLUMNS * Square.SIDE + 15;
 		setLocationRelativeTo(getRootPane());
 		setResizable(false);
 		setTitle("Single Player Tetris");
 		setSize(width, height);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(new BorderLayout());
 		add(gameView, BorderLayout.CENTER);
 		add(scoreLevelDisplay, BorderLayout.EAST);
 		setVisible(true);
 
 		new Thread() {
 			@Override
 			public void run() {
 				while (true) {
 					gameView.repaint();
 					gameController.movePieces();
 				}
 			}
 		}.start();
 
 	}
 
 	@Override
 	public void onGameOver() {
 		String message = "Game over! Score is " + gameController.getScore()
 				+ ".\n" + "Do you want to play again?";
 		int gameOver = JOptionPane.showConfirmDialog(null, message);
 		if (gameOver == 0) {
 			gameController.setScore(0);
 			gameController.setCurrLevel(1);
 			this.dispose();
 			Injector injector = Guice.createInjector(new Module[0]);
 			injector.getInstance(TetrisMain.class);
 		}
 		if (gameOver == 1) {
 			System.exit(0);
 		}
 	}
 
 	@Override
 	public void onCompleteLine(int numLines) {
 		CompleteLineMusicPlayer completePlayer;
 		try {
 			completePlayer = new CompleteLineMusicPlayer();
 			completePlayer.play();
 		} catch (UnsupportedAudioFileException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (LineUnavailableException e) {
 			e.printStackTrace();
 		}
		gameController.lineCompleted(numLines);
 		scoreLevelDisplay.setScore(gameController.getScore());
 		int currLevel = gameController.getCurrLevel();
 		if (gameController.getScore() > currLevel * 200) {
 			onLevelChange();
 		}
 	}
 
 	@Override
 	public void onLevelChange() {
 		LevelChangeMusicPlayer levelPlayer;
 		try {
 			levelPlayer = new LevelChangeMusicPlayer();
 			levelPlayer.play();
 		} catch (UnsupportedAudioFileException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (LineUnavailableException e) {
 			e.printStackTrace();
 		}
 		int currLevel = gameController.getCurrLevel();
 		gameController.setCurrLevel(currLevel + 1);
 		scoreLevelDisplay.setLevel(currLevel + 1);
 		gameController.increaseSpeed();
 	}
 
 	@Override
 	public void onHitFloor() {
 		HitFloorMusicPlayer hitFloorPlayer;
 		try {
 			hitFloorPlayer = new HitFloorMusicPlayer();
 			hitFloorPlayer.play();
 		} catch (UnsupportedAudioFileException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (LineUnavailableException e) {
 			e.printStackTrace();
 		}
 		scoreLevelDisplay.repaint();
 		scoreLevelDisplay.setPiece(gameController.getNextPiece());
 	}
 
 	@Override
 	public void onNewPiece(Piece piece) {
 		keyboardListener.setPiece(piece);
 		scoreLevelDisplay.setPiece(gameController.getNextPiece());
 		scoreLevelDisplay.repaint();
 	}
 
 	@Override
 	public void onRotate() {
 		RotateMusicPlayer rotatePlayer;
 		try {
 			rotatePlayer = new RotateMusicPlayer();
 			rotatePlayer.play();
 		} catch (UnsupportedAudioFileException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (LineUnavailableException e) {
 			e.printStackTrace();
 		}
 	}
 }
