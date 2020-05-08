 package it.chalmers.tendu.screens;
 
 import it.chalmers.tendu.Tendu;
 import it.chalmers.tendu.controllers.InputController;
 import it.chalmers.tendu.controllers.ShapeGameModelController;
 import it.chalmers.tendu.defaults.Constants;
 import it.chalmers.tendu.gamemodel.GameState;
 import it.chalmers.tendu.gamemodel.MiniGame;
 import it.chalmers.tendu.gamemodel.Player;
 import it.chalmers.tendu.gamemodel.shapesgame.Shape;
 import it.chalmers.tendu.gamemodel.shapesgame.ShapesGame;
 import it.chalmers.tendu.gamemodel.shapesgame.ShapesGameSound;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.math.Vector3;
 
 public class ShapesGameScreen extends GameScreen {
 
 	public final String TAG = this.getClass().getName();
 
 	private int player_num;
 	private ShapeRenderer shapeRenderer; // used to render vector graphics
 	// private ShapesGame model;
 	private List<GraphicalShape> shapes;
 	private List<GraphicalShape> locks;
 
 	private GraphicalShape movingShape;
 
 	private ShapeGameModelController controller;
 	
 	private ShapesGameSound sound;
 
 	// For debug
 	int count = 0;
 
 	public ShapesGameScreen(Tendu game, MiniGame model) {
 		super(game, model);
 		controller = new ShapeGameModelController((ShapesGame) model);
 		this.shapeRenderer = new ShapeRenderer();
 		
 		player_num = controller.getModel().getplayerNbr();
 		sound = new ShapesGameSound();
 		
 		shapes = new ArrayList<GraphicalShape>();
 		int x = 150;
 		for (Shape s : controller.getModel().getAllInventory().get(player_num)) {
 			GraphicalShape sgs = new GraphicalShape(s);
 			sgs.moveShape(x, 150);
 			shapes.add(sgs);
 			x = x + 151;
 		}
 
 		locks = new ArrayList<GraphicalShape>();
 		x = 150;
 		for (Shape s : controller.getModel().getLock(player_num)
 				.getLockSequence()) {
 			GraphicalShape sgs = new GraphicalShape(s);
 			sgs.moveShape(x, 300);
 			locks.add(sgs);
 			x = x + 151;
 		}
 
 	}
 
 	/** All graphics are drawn here */
 	@Override
 	public void render() {
 		super.render();
 		if (model.checkGameState() == GameState.RUNNING) {
 			shapeRenderer.setProjectionMatrix(tendu.getCamera().combined);
 
 			// Renders locks
 			for (GraphicalShape sgs : locks) {
 				sgs.renderShape(shapeRenderer);
 			}
 
 			// Renders shapes
 			for (GraphicalShape sgs : shapes) {
 				sgs.renderShape(shapeRenderer);
 			}
 
 			// if (Gdx.input.isTouched()) {
 			// Vector3 touchPos = new Vector3(Gdx.input.getX(),
 			// Gdx.input.getY(), +0);
 			// tendu.getCamera().unproject(touchPos);
 			// for (GraphicalShape s : shapes) {
 			// // TODO: Should not prio the shape that is first by index.
 			// if (s.getBounds().contains(touchPos.x, touchPos.y)
 			// && !s.getShape().isLocked()) {
 			// Collections.swap(shapes, 0, shapes.indexOf(s));
 			// s.moveShape(touchPos.x - s.getBounds().width / 2,
 			// touchPos.y - s.getBounds().height / 2);
 			// for (GraphicalShape lock : locks) {
 			// if (snapIntoPlace(s, lock)) {
 			// // TODO: is game completed?
 			// }
 			// }
 			// if (s.getBounds().x <= 10) {
 			// Gdx.app.log("SENT!!", s.toString()
 			// + "sent to player 2");
 			// EventBus.INSTANCE.broadcast(new EventMessage(
 			// C.Tag.TO_SELF, C.Msg.SHAPE_SENT,
 			// messageContentFactory(2, s.getShape())));
 			// }
 			// if (s.getBounds().x >= Constants.SCREEN_WIDTH - 60) {
 			// Gdx.app.log("SENT!!", s.toString()
 			// + "sent to player 3");
 			// EventBus.INSTANCE.broadcast(new EventMessage(
 			// C.Tag.TO_SELF, C.Msg.SHAPE_SENT,
 			// messageContentFactory(3, s.getShape())));
 			//
 			// }
 			// if (s.getBounds().y >= Constants.SCREEN_HEIGHT - 60) {
 			// Gdx.app.log("SENT!!", s.toString()
 			// + "sent to player 1");
 			//
 			// EventBus.INSTANCE.broadcast(new EventMessage(
 			// C.Tag.TO_SELF, C.Msg.SHAPE_SENT,
 			// messageContentFactory(1, s.getShape())));
 			// }
 			// break;
 			// }
 			//
 			// }
 		} else {
 			showGameResult();
 		}
 		model.checkGame();
 	}
 
 	/**
 	 * 
 	 * @param player
 	 *            That will receive the shape
 	 * @param shape
 	 *            shape to be sent
 	 * @return
 	 */
 	private List<Object> messageContentFactory(int player, Shape shape) {
 
 		List<Object> l = new ArrayList<Object>();
 		l.add(player);
 		l.add(shape);
 		return l;
 
 	}
 
 	/** All game logic goes here (within the model...) */
 	@Override
 	public void tick(InputController input) {
 
 		updateShapesFromModel();
 		
 		Vector3 touchPos = new Vector3(input.x, input.y, +0);
 		tendu.getCamera().unproject(touchPos);
 
 		// TODO nullpointer movingShape
 		if (input.isTouchedDown()) {
 			for (GraphicalShape s : shapes) {
 				if (s.getBounds().contains(touchPos.x, touchPos.y)
 						&& !s.getShape().isLocked()) {
 					movingShape = s;
 				}
 			}
 		}
 
 		if (input.isTouchedUp()) {
 			if (movingShape != null) {
 				for (GraphicalShape lock : locks) {
 					snapIntoPlace(movingShape, lock);
 				}
 				movingShape = null;
 			}
 		}
 
 		if (input.isDragged()) {
 			if (movingShape != null) {
 				//Gdx.app.log(TAG, "Locked in screen: " + movingShape.getShape().isLocked());
 				Gdx.app.log(TAG, movingShape.getShape().geometricShape + " " + movingShape.getShape().color);
 				if (!movingShape.getShape().isLocked()) {
 					movingShape.moveShape(touchPos.x
 							- movingShape.getBounds().width / 2, touchPos.y
 							- movingShape.getBounds().height / 2);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void removed() {
 		super.removed();
 		sound.unRegister();
 	}
 
 	// TODO : Adds a new shape if any shape has changed color.
 	private void updateShapesFromModel() {
 		// Adds shapes to the gui that are no longer part
 		// of the model.
 		for (Shape s : controller.getModel().getAllInventory().get(player_num)) {
 			if (!shapes.contains(new GraphicalShape(s))) {
 				shapes.add(new GraphicalShape(s));
 				Gdx.app.log(TAG, "new Shape!");
 			}
 		}
 
 		// Removes shapes that are no longer parts of the
 		// model.
 		List<GraphicalShape> removeList = new ArrayList<GraphicalShape>();
 		for (GraphicalShape gs : shapes) {
 			if (!controller.getModel().getAllInventory().get(player_num)
 					.contains(gs.getShape())) {
 				removeList.add(gs);
 				Gdx.app.log(TAG, "Shape removed!");
 			}
 		}
 
 		for (GraphicalShape gs : removeList)
 			shapes.remove(gs);
 		
		Gdx.app.log(TAG, "form 0 " + controller.getModel().getAllInventory().get(player_num).get(0).isLocked());
		Gdx.app.log(TAG, "form 1 " + controller.getModel().getAllInventory().get(player_num).get(1).isLocked());
		Gdx.app.log(TAG, "form 2 " + controller.getModel().getAllInventory().get(player_num).get(2).isLocked());
		Gdx.app.log(TAG, "form 3 " + controller.getModel().getAllInventory().get(player_num).get(3).isLocked());
		
		Gdx.app.log(TAG, "moving shape " +movingShape.getShape().isLocked());
 
 	}
 
 	public boolean snapIntoPlace(GraphicalShape shape, GraphicalShape lock) {
 		boolean result = false;
 		if (shape.getBounds().overlaps(lock.getBounds())) {
 			if (controller.getModel().shapeFitIntoLock(player_num,
 					shape.getShape(), lock.getShape())) {
 				shape.moveShape(lock.getBounds().x, lock.getBounds().y);
 				result = true;
 				Gdx.app.log(TAG, "Animated" + "x=" + lock.getBounds().x + "y="
 						+ lock.getBounds().getY());
 
 			}
 			List<Object> content = new ArrayList<Object>();
 			content.add(player_num);
 			content.add(lock.getShape());
 			content.add(shape.getShape());
 			EventBus.INSTANCE.broadcast(new EventMessage(Player.getInstance()
 					.getMac(), C.Tag.TO_SELF, C.Msg.LOCK_ATTEMPT, controller
 					.getModel().getGameId(), content));
 		}
 		return result;
 
 	}
 
 }
