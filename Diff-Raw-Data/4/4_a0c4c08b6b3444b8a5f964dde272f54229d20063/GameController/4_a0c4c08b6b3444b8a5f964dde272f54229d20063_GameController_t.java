 package at.ac.uibk.akari.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.andengine.engine.camera.ZoomCamera;
 import org.andengine.entity.scene.Scene;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.sat4j.specs.ContradictionException;
 
 import android.util.Log;
 import at.ac.uibk.akari.MainActivity;
 import at.ac.uibk.akari.core.Puzzle;
 import at.ac.uibk.akari.listener.GameListener;
 import at.ac.uibk.akari.listener.MenuItemSeletedEvent;
 import at.ac.uibk.akari.listener.MenuItemSeletedEvent.ItemType;
 import at.ac.uibk.akari.listener.MenuListener;
 import at.ac.uibk.akari.puzzleSelector.controller.PuzzleSelectionController;
 import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionEvent;
 import at.ac.uibk.akari.puzzleSelector.listener.PuzzleSelectionListener;
 import at.ac.uibk.akari.utils.BackgroundLoader;
 import at.ac.uibk.akari.utils.BackgroundLoader.BackgroundType;
 import at.ac.uibk.akari.view.menu.AbstractMenuScene;
 import at.ac.uibk.akari.view.menu.MainMenuScene;
 import at.ac.uibk.akari.view.menu.PopupMenuScene;
 
 public class GameController extends AbstractController implements GameListener, MenuListener, PuzzleSelectionListener {
 
 	private List<Puzzle> puzzles;
 	private PuzzleController puzzleController;
 	private PuzzleSelectionController puzzleSelectionController;
 
 	private int currentPuzzleIndex;
 
 	private ZoomCamera gameCamera;
 
 	private Scene gameScene;
 	private Scene puzzleSelectionScene;
 	private PopupMenuScene winninMenuScene;
 	private AbstractMenuScene mainMenuScene;
 
 	private VertexBufferObjectManager vertexBufferObjectManager;
 
 	public GameController(final ZoomCamera gameCamera, final Scene gameScene, final VertexBufferObjectManager vertexBufferObjectManager, final List<Puzzle> puzzles) {
 		this.puzzles = puzzles;
 		this.gameCamera = gameCamera;
 		this.gameScene = gameScene;
 		this.vertexBufferObjectManager = vertexBufferObjectManager;
 		this.init();
 	}
 
 	private void init() {
 		// initialize puzzle controller
 		this.puzzleController = new PuzzleController(this.gameCamera, this.gameScene, this.vertexBufferObjectManager);
 
 		// initialize main-menu-scene
 		List<ItemType> mainMenuItems = new ArrayList<ItemType>();
 		mainMenuItems.add(ItemType.RANDOM_PUZZLE);
 		mainMenuItems.add(ItemType.SELECT_PUZZLE);
 		mainMenuItems.add(ItemType.QUIT);
 		this.mainMenuScene = new MainMenuScene(this.gameCamera, this.vertexBufferObjectManager, mainMenuItems);
 
 		// initialize game-scene
 		this.gameScene.setBackgroundEnabled(true);
 		this.gameScene.setBackground(BackgroundLoader.getInstance().getBackground(BackgroundType.GAME_FIELD_BACKGROUND));
 
 		// initialize winning-menu-scene
 		List<ItemType> winningMenuItems = new ArrayList<ItemType>();
 		winningMenuItems.add(ItemType.NEXT);
 		winningMenuItems.add(ItemType.REPLAY);
 		winningMenuItems.add(ItemType.MAIN_MENU);
 		this.winninMenuScene = new PopupMenuScene(this.gameCamera, this.vertexBufferObjectManager, winningMenuItems);
 
 		// initialize puzzle-selection-scene and controller
 		this.puzzleSelectionScene = new Scene();
 		this.puzzleSelectionScene.setBackgroundEnabled(true);
 		this.puzzleSelectionScene.setBackground(BackgroundLoader.getInstance().getBackground(BackgroundType.GAME_FIELD_BACKGROUND));
 		this.puzzleSelectionController = new PuzzleSelectionController(this.puzzleSelectionScene, this.gameCamera, this.vertexBufferObjectManager);
 	}
 
 	@Override
 	public boolean start() {
 		this.puzzleController.addGameListener(this);
 		this.winninMenuScene.addMenuListener(this);
 		this.mainMenuScene.addMenuListener(this);
 		this.puzzleSelectionController.addPuzzleSelectionListener(this);
 
 		this.setCurrentGameScene(this.mainMenuScene);
 		return true;
 	}
 
 	@Override
 	public boolean stop() {
 		this.puzzleController.removeGameListener(this);
 		this.winninMenuScene.removeMenuListener(this);
 		this.mainMenuScene.removeMenuListener(this);
 		this.puzzleSelectionController.removePuzzleSelectionListener(this);
 
 		return true;
 	}
 
 	@Override
 	public void puzzleSolved(final PuzzleController source, final long timeMs) {
 		if (source.equals(this.puzzleController)) {
 			this.puzzleController.stop();
 			// display game-winning-menu
 			this.gameScene.setChildScene(this.winninMenuScene, false, true, true);
 		}
 	}
 
 	public void startLevel(final Puzzle puzzle) {
 		try {
 			this.puzzleController.setPuzzle(puzzle);
 			this.puzzleController.start();
 		} catch (ContradictionException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void menuItemSelected(final MenuItemSeletedEvent event) {
 		// source was the winning-menu-scene
 		if (event.getSource() == this.winninMenuScene) {
 			this.puzzleController.stop();
 			this.winninMenuScene.back();
 
 			switch (event.getItemType()) {
 			case REPLAY:
 				Log.i(this.getClass().getName(), "REPLAY-Game pressed");
 				this.startLevel(this.puzzles.get(this.currentPuzzleIndex));
 				break;
 			case NEXT:
 				Log.i(this.getClass().getName(), "NEXT-Game pressed");
 				this.resetGameCamera();
 				this.startLevel(this.puzzles.get(++this.currentPuzzleIndex));
 				break;
 			case MAIN_MENU:
 				Log.i(this.getClass().getName(), "MAIN_MENU pressed");
 				this.setCurrentGameScene(this.mainMenuScene);
 				break;
 			default:
 				break;
 			}
 		}
 		// source was the main-menu-scene
 		else if (event.getSource() == this.mainMenuScene) {
 			switch (event.getItemType()) {
 			case RANDOM_PUZZLE:
 				this.setCurrentGameScene(this.gameScene);
 				this.currentPuzzleIndex = 0;
 				this.startLevel(this.puzzles.get(this.currentPuzzleIndex));
 				break;
 			case SELECT_PUZZLE:
 				this.setCurrentGameScene(this.puzzleSelectionScene);
 				this.puzzleSelectionController.setPuzzles(this.puzzles);
 				this.puzzleSelectionController.start();
 				break;
 			case QUIT:
 				MainActivity.quit();
 			default:
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void puzzleStopped(final PuzzleController source) {
 		if (source == this.puzzleController) {
 			this.puzzleController.stop();
 			this.winninMenuScene.back();
 			this.setCurrentGameScene(this.mainMenuScene);
 		}
 	}
 
 	private void resetGameCamera() {
 		this.gameCamera.setZoomFactor(1);
 		this.gameCamera.setCenter(this.gameCamera.getWidth() / 2, this.gameCamera.getHeight() / 2);
 	}
 
 	private void setCurrentGameScene(final Scene scene) {
 		this.gameCamera.setHUD(null);
 		this.resetGameCamera();
 		MainActivity.setCurrentScene(scene);
 	}
 
 	@Override
 	public void puzzleSelectionCanceled(final PuzzleSelectionEvent event) {
 		if (event.getSource().equals(this.puzzleSelectionController)) {
 			Log.i(this.getClass().getName(), "Puzzle-selection cancelled");
 			this.setCurrentGameScene(this.mainMenuScene);
 		}
 	}
 
 	@Override
 	public void puzzleSelected(final PuzzleSelectionEvent event) {
 		if (event.getSource().equals(this.puzzleSelectionController)) {
 			this.setCurrentGameScene(this.gameScene);
			Puzzle selectedPuzzle = event.getPuzzle();
			this.currentPuzzleIndex = this.puzzles.indexOf(selectedPuzzle);
			this.startLevel(selectedPuzzle);
 		}
 	}
 }
