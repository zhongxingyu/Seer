 /**
  * Copyright 2012 Jami Couch
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * This project uses:
  * 
  * LibGDX
  * Copyright 2011 see LibGDX AUTHORS file
  * Licensed under Apache License, Version 2.0 (see above).
  * 
  */
 package com.ahsgaming.valleyofbones.screens;
 
 import com.ahsgaming.valleyofbones.GameController;
 import com.ahsgaming.valleyofbones.GameObject;
 import com.ahsgaming.valleyofbones.Player;
 import com.ahsgaming.valleyofbones.TextureManager;
 import com.ahsgaming.valleyofbones.VOBGame;
 import com.ahsgaming.valleyofbones.map.HexMap;
 import com.ahsgaming.valleyofbones.network.Attack;
 import com.ahsgaming.valleyofbones.network.Build;
 import com.ahsgaming.valleyofbones.network.Command;
 import com.ahsgaming.valleyofbones.network.EndTurn;
 import com.ahsgaming.valleyofbones.network.Move;
 import com.ahsgaming.valleyofbones.network.Upgrade;
 import com.ahsgaming.valleyofbones.screens.panels.BuildPanel;
 import com.ahsgaming.valleyofbones.screens.panels.InfoPanel;
 import com.ahsgaming.valleyofbones.screens.panels.Panel;
 import com.ahsgaming.valleyofbones.screens.panels.ScorePanel;
 import com.ahsgaming.valleyofbones.units.Prototypes;
 import com.ahsgaming.valleyofbones.units.Unit;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Buttons;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.ObjectMap;
 
 /**
  * @author jami
  *
  */
 public class LevelScreen extends AbstractScreen {
 	public String LOG = "LevelScreen";
 
     protected static LevelScreen instance;
 
 	private Group grpLevel;
 	
 	private GameController gController = null;
 	
 	// input stuff
 	private Vector2 boxOrigin = null, boxFinal = null;
 	private boolean rightBtnDown = false;
 	
 	ShapeRenderer shapeRenderer;
 	
 	
 	// camera 'center' position - this will always remain within the bounds of the map
 	private Vector2 posCamera = new Vector2();
 	
 	// UX stuff
 	Group grpTurnPane = new Group();
 	Label lblTurnTimer;
 	TextButton btnTurnDone;
 	
 	Group grpPreviews = new Group();
 	Array<Command> commandsPreviewed = new Array<Command>();
 
     Panel buildPanel;
     Panel upgradePanel;
     InfoPanel selectionPanel;
     ScorePanel scorePanel;
     ScorePanel.PlayerScore playerScore;
 
 
 	private boolean vKeyDown = false, bKeyDown = false;
 
     boolean buildMode = false;
     Prototypes.JsonProto buildProto = null;
     Image buildImage = null;
 
     GameObject lastSelected = null;
 
     Player lastCurrentPlayer = null;
 	
 	/**
 	 * @param game
 	 */
 	public LevelScreen(VOBGame game, GameController gController) {
 		super(game);
 		this.gController = gController;
 		shapeRenderer = new ShapeRenderer();
         instance = this;
 	}
 	
 	/**
 	 * Methods
 	 */
 	
 	private void clampCamera() {
 		HexMap map = gController.getMap();
 		
 		if (posCamera.x < 0) posCamera.x = 0;
 		if (posCamera.x > map.getWidth() * map.getTileWidth()) posCamera.x = map.getWidth() * map.getTileWidth();
 		
 		if (posCamera.y < 0) posCamera.y = 0;
 		if (posCamera.y > map.getHeight() * map.getTileHeight() * 0.75) posCamera.y = map.getHeight() * map.getTileHeight() * 0.75f;
 	}
 	
 	private void dimUnits() {
 		for (GameObject o: gController.getGameObjects()) {
             if (o instanceof Unit) {
                 Unit u = (Unit)o;
 
                 if (u.getOwner() != null && u.getOwner().getPlayerId() == game.getPlayer().getPlayerId()) {
                     u.setVisible(true);
                 } else {
                     u.setVisible(gController.getMap().isBoardPositionVisible(u.getBoardPosition()));
                 }
             }
         }
 	}
 	
 	private void drawUnitBoxes() {
 		if (gController.getSelectedObject() != null) {
 			GameObject obj = gController.getSelectedObject();
 			shapeRenderer.begin(ShapeType.Line);
 			shapeRenderer.setColor((obj.getOwner() != null ? obj.getOwner().getPlayerColor() : new Color(1, 1, 1, 1)));
 			Vector2 start = gController.getMap().boardToMapCoords(obj.getBoardPosition().x, obj.getBoardPosition().y);
 			start = mapToScreenCoords(start.x, start.y);

            shapeRenderer.setProjectionMatrix(stage.getCamera().combined); // BUGFIX: rescaling the window threw off the selection drawings

 			Vector2 base = new Vector2(start.x, start.y);
 			shapeRenderer.line(base.x + gController.getMap().getTileWidth() * 0.5f, base.y, base.x, base.y + gController.getMap().getTileHeight() * 0.25f);
 			shapeRenderer.line(base.x, base.y + gController.getMap().getTileHeight() * 0.25f, base.x, base.y + gController.getMap().getTileHeight() * 0.75f);
 			shapeRenderer.line(base.x, base.y + gController.getMap().getTileHeight() * 0.75f, base.x + gController.getMap().getTileWidth() * 0.5f, base.y + gController.getMap().getTileHeight());
 			shapeRenderer.line(base.x + gController.getMap().getTileWidth() * 0.5f, base.y + gController.getMap().getTileHeight(), base.x + gController.getMap().getTileWidth(), base.y + gController.getMap().getTileHeight() * 0.75f);
 			shapeRenderer.line(base.x + gController.getMap().getTileWidth(), base.y + gController.getMap().getTileHeight() * 0.75f, base.x + gController.getMap().getTileWidth(), base.y + gController.getMap().getTileHeight() * 0.25f);
 			shapeRenderer.line(base.x + gController.getMap().getTileWidth(), base.y + gController.getMap().getTileHeight() * 0.25f, base.x + gController.getMap().getTileWidth() * 0.5f, base.y);
 			shapeRenderer.end();

 		}
 	}
 	
 	private void doCameraMovement(float delta) {
 		if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.getY() <= game.getMouseScrollSize()) {
 			// move 'up'
 			posCamera.y += game.getKeyScrollSpeed() * delta;
 		} else if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.getY() >= stage.getHeight() - game.getMouseScrollSize()) {
 			// move 'down'
 			posCamera.y -= game.getKeyScrollSpeed() * delta;
 		}
 		
 		if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.getX() <= game.getMouseScrollSize()) {
 			// move 'left'
 			posCamera.x -= game.getKeyScrollSpeed() * delta;
 		} else if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.getX() >= stage.getWidth() - game.getMouseScrollSize()) {
 			// move 'right'
 			posCamera.x += game.getKeyScrollSpeed() * delta;
 		}
 		
 		clampCamera();
 	}
 	
 	private void doProcessInput(float delta) {
 		Vector2 mapPos = screenToMapCoords(Gdx.input.getX(), stage.getHeight() - Gdx.input.getY());
 		Vector2 boardPos = gController.getMap().mapToBoardCoords(mapPos.x, mapPos.y);
 		
 		if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
 
 			if (buildMode){
                 if (isCurrentPlayer()) {
                     if (game.getPlayer().canBuild(buildProto.id, gController) && gController.isBoardPosEmpty(boardPos) && gController.getMap().isBoardPositionVisible(boardPos)) {
                         Build bld = new Build();
                         bld.owner = game.getPlayer().getPlayerId();
                         bld.turn = gController.getGameTurn();
                         bld.building = buildProto.id;
                         bld.location = boardPos;
                         game.sendCommand(bld);
                         if (!(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))) unsetBuildMode();
                     }
                 }
 
 			} else {						
 				gController.selectObjAtBoardPos(boardPos);
 				if (gController.getSelectedObject() != null && gController.getSelectedObject() instanceof Unit) {
 					Unit u = (Unit)gController.getSelectedObject();
 					Gdx.app.log(LOG, String.format("Selected: %s (%d/%d)", u.getProtoId(), u.getCurHP(), u.getMaxHP()));
                     gController.getMap().highlightArea(u.getBoardPosition(), u.getMovesLeft(), true);
 				}
 			}
 		}
 		
 		if (Gdx.input.isButtonPressed(Buttons.RIGHT)){
 			rightBtnDown = true;
 		} else {
 
             if (rightBtnDown && buildMode) {
                 unsetBuildMode();
             } else if (rightBtnDown && gController.getSelectedObject() != null && gController.getSelectedObject() instanceof Unit) {
 
 				if (isCurrentPlayer()) {
                     // TODO issue context-dependent commands
                     Array<GameObject> objsUnderCursor = null;
                     GameObject target = null;
 
                     Unit unit = (Unit)gController.getSelectedObject();
                     if (objsUnderCursor == null) {
                         objsUnderCursor = gController.getObjsAtPosition(screenToMapCoords(Gdx.input.getX(), stage.getHeight() - Gdx.input.getY()));
 
                         for (GameObject cur: objsUnderCursor) {
                             if (cur.getOwner() != game.getPlayer()) {
                                 // TODO find the object with the highest 'threat'?
                                 target = cur;
                             }
                         }
                     }
 
                     if (target != null) {
                         // attack this target!
                         Attack at = new Attack();
                         at.owner = game.getPlayer().getPlayerId();
                         at.turn = gController.getGameTurn();
                         at.unit = unit.getObjId();
                         at.target = target.getObjId();
 
                         game.sendCommand(at);
                     } else {
                         // move to this location
                         if (gController.isBoardPosEmpty(boardPos) && gController.getMap().getMapDist(unit.getBoardPosition(), boardPos) <= unit.getMovesLeft()) {
                             Move mv = new Move();
                             mv.owner = game.getPlayer().getPlayerId();
                             mv.turn = gController.getGameTurn();
                             mv.unit = unit.getObjId();
                             mv.toLocation = boardPos;
                             game.sendCommand(mv);
                         }
                     }
 
                 }
 
 			}
             rightBtnDown = false;
 		}
 		
 		if (!Gdx.input.isButtonPressed(Buttons.RIGHT) && !Gdx.input.isButtonPressed(Buttons.LEFT)) {
 
             if (Gdx.input.isKeyPressed(Keys.V)) {
                 if (!vKeyDown) upgradePanel.toggle();
 
                 vKeyDown = true;
 			} else {
                 vKeyDown = false;
             }
 
             if (Gdx.input.isKeyPressed(Keys.B)) {
                 if (!bKeyDown) buildPanel.toggle();
 
                 bKeyDown = true;
             } else {
                 bKeyDown = false;
             }
 
             if (Gdx.input.isKeyPressed(Keys.A) && isCurrentPlayer() && game.getPlayer().canBuild("marine-base", gController)) {
                 setBuildMode(Prototypes.getProto("marine-base"));
             } else if (Gdx.input.isKeyPressed(Keys.S) && isCurrentPlayer() && game.getPlayer().canBuild("saboteur-base", gController)) {
                 setBuildMode(Prototypes.getProto("saboteur-base"));
             } else if (Gdx.input.isKeyPressed(Keys.D) && isCurrentPlayer() && game.getPlayer().canBuild("tank-base", gController)) {
                 setBuildMode(Prototypes.getProto("tank-base"));
             }
 		}
 		
 		
 		// TODO make a list of flags or something rather than creating fields for every key that might get pressed...
 		// TODO reset keys 'n' things
 		if (vKeyDown && !Gdx.input.isKeyPressed(Keys.V)) {
 			vKeyDown = false;
 		}
 	}
 
     public void setBuildMode(Prototypes.JsonProto proto) {
         if (buildMode) unsetBuildMode();
         if (isCurrentPlayer() && game.getPlayer().canBuild(proto.id, gController)) {
             buildMode = true;
             buildProto = proto;
             buildImage = new Image(TextureManager.getSpriteFromAtlas("assets", proto.image));
             buildImage.setColor(1, 1, 1, 0.5f);
         }
     }
 
     public void unsetBuildMode() {
         buildMode = false;
         buildProto = null;
         if (buildImage != null) {
             buildImage.remove();
             buildImage = null;
         }
     }
 	
 	
 	public Vector2 screenToMapCoords(float x, float y) {
 		return new Vector2(x + (posCamera.x - stage.getWidth() * 0.5f), y + (posCamera.y - stage.getHeight() * 0.5f));  
 	}
 	
 	public Vector2 mapToScreenCoords(float x, float y) {
 		return new Vector2(x - (posCamera.x - stage.getWidth() * 0.5f), y - (posCamera.y - stage.getHeight() * 0.5f));
 	}
 	
 	public void showCommandPreviews() {
 		// TODO optimize this
 		grpPreviews.clear();
 		grpPreviews.remove();
 		grpLevel.addActor(grpPreviews);
 		
 		
 		for (Command c: gController.getCommandQueue()) {
 			if (c.owner == game.getPlayer().getPlayerId()) {
 				
 				if (c instanceof Build || c instanceof Move) {
 					Image img = null;
 					Vector2 pos = new Vector2();
 					if (c instanceof Build) {
 						img = new Image(TextureManager.getSpriteFromAtlas("assets", Prototypes.getProto(((Build)c).building).image));
 						pos = ((Build)c).location;
 					} else if (c instanceof Move) {
 						img = new Image(gController.getObjById(((Move)c).unit).getImage());
 						pos = ((Move)c).toLocation;
 					}
 					Vector2 sPos = gController.getMap().boardToMapCoords(pos.x, pos.y);
 					img.setPosition(sPos.x, sPos.y);
 					img.setColor(1, 1, 1, 0.5f);
 					grpPreviews.addActor(img);
 				}
 			}
 		}
 	}
 
     public boolean isCurrentPlayer() {
         return gController.getCurrentPlayer() == game.getPlayer();
     }
 
 	
 	/**
 	 * Implemented methods
 	 */
 	
 	@Override
 	public void show() {
 		super.show();
 		Gdx.app.log(VOBGame.LOG, "LevelScreen#show");
 		
 		grpLevel = gController.getGroup();
 
 		posCamera.set(gController.getSpawnPoint(game.getPlayer().getPlayerId()));
 
         buildPanel = new BuildPanel(game, this, "gear-hammer", getSkin());
         upgradePanel = new Panel(game, this, "tinker", getSkin());
         selectionPanel = new InfoPanel(game, this, "invisible", getSkin());
         scorePanel = new ScorePanel(game, this, "scores", getSkin(), gController.getPlayers());
 
         playerScore = new ScorePanel.PlayerScore(getSkin(), game.getPlayer().getPlayerName(), game.getPlayer().getPlayerColor(), true);
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 		
 		stage.addActor(grpLevel);
 
 		// generate the turn info
 		grpTurnPane = new Group();
 		lblTurnTimer = new Label(" ", new LabelStyle(getLargeFont(), new Color(1,1,1,1)));
 		
 		btnTurnDone = new TextButton("END TURN", getSkin(), "large");
 		btnTurnDone.setSize(350, 150);
 
 		lblTurnTimer.setY(btnTurnDone.getHeight());
 		
 		grpTurnPane.addActor(btnTurnDone);
 		grpTurnPane.addActor(lblTurnTimer);
 		
 		grpTurnPane.setSize(btnTurnDone.getWidth(), lblTurnTimer.getTop());
 		
 		stage.addActor(grpTurnPane);
 		
 		// panels
         // TODO add back upgrade panel
         //stage.addActor(upgradePanel);
         upgradePanel.setPosition(0, 64);
 
         stage.addActor(buildPanel);
         buildPanel.setPosition(0, 0);
 
         stage.addActor(selectionPanel);
         selectionPanel.setPosition(stage.getWidth() * 0.5f, 0);
 
         stage.addActor(scorePanel);
         scorePanel.setPosition(stage.getWidth(), lblTurnTimer.getTop());
 
         stage.addActor(playerScore);
 
 
 		btnTurnDone.addListener(new ClickListener() {
 
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				super.clicked(event, x, y);
 				
 				EndTurn et = new EndTurn();
 				et.owner = game.getPlayer().getPlayerId();
 				et.turn = gController.getGameTurn();
 				
 				game.sendCommand(et);
 			}
 			
 		});
 	}
 
 	public void updateTurnPane() {
 		lblTurnTimer.setText(String.format("TIME LEFT %02d:%02d", (int)Math.floor(gController.getTurnTimer() / 60), (int)gController.getTurnTimer() % 60));
 		grpTurnPane.setX(stage.getWidth() - grpTurnPane.getWidth());
         grpTurnPane.setSize(btnTurnDone.getWidth(), grpTurnPane.getTop());
 
         btnTurnDone.setDisabled(!isCurrentPlayer());
         btnTurnDone.setColor((isCurrentPlayer() ? new Color(1, 1, 1, 1) : new Color(0.5f, 0.5f, 0.5f, 1)));
         btnTurnDone.setText((isCurrentPlayer() ? "End Turn" : "Please Wait"));
 	}
 	
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 
         if (buildMode && !isCurrentPlayer()) unsetBuildMode();
 
 		// draw a debug map
 		//gController.getMap().drawDebug(new Vector2(grpLevel.getX(), grpLevel.getY()));
 		gController.getMap().update(game.getPlayer(), gController);
 
         // dim units based on whether the player can see them
         dimUnits();
 
         // clear highlighting if necessary
         gController.getMap().clearHighlightAndDim();
         lastSelected = gController.getSelectedObject();
 
         selectionPanel.setSelected(null);
         if (lastSelected != null && lastSelected instanceof Unit) {
             gController.getMap().highlightArea(lastSelected.getBoardPosition(), ((Unit)lastSelected).getMovesLeft(), true);
             selectionPanel.setSelected((Unit)lastSelected);
         }
 
 		// DRAW BOXES
 		drawUnitBoxes();
 
 		// move the camera around
 		doCameraMovement(delta);
 		
 		// get input
 		doProcessInput(delta);
 		
 		// update level position
         if (VOBGame.DEBUG_LOCK_SCREEN)
             posCamera.set(grpLevel.getWidth() * 0.5f, grpLevel.getHeight() * 0.5f - stage.getHeight() * 0.125f);
 
             grpLevel.setPosition(-1 * posCamera.x + stage.getWidth() * 0.5f, -1 * posCamera.y + stage.getHeight() * 0.5f);
 
 		updateTurnPane();
 
         buildPanel.update(delta);
         upgradePanel.update(delta);
         selectionPanel.update(delta);
         scorePanel.update(delta, gController.getPlayers(), gController.getCurrentPlayer());
         playerScore.update(game.getPlayer().getPlayerName(), game.getPlayer().getCurFood(), game.getPlayer().getMaxFood(), (int)game.getPlayer().getBankMoney());
         playerScore.setPosition(stage.getWidth() - playerScore.getWidth(), stage.getHeight() - playerScore.getHeight());
 
 		showCommandPreviews();
 
         if (buildImage != null) buildImage.remove();
         if (buildMode) {
             grpLevel.addActor(buildImage);
 
             Vector2 loc = screenToMapCoords(Gdx.input.getX(), stage.getHeight() - Gdx.input.getY());
             loc = gController.getMap().mapToBoardCoords(loc.x, loc.y);
             loc = gController.getMap().boardToMapCoords(loc.x, loc.y);
 
             buildImage.setPosition(loc.x, loc.y);
 
             if (!isCurrentPlayer()) unsetBuildMode();
         }
 		
 		// easy exit for debug purposes
 		if (Gdx.input.isKeyPressed(Keys.ESCAPE) && VOBGame.DEBUG) {
 			game.quitGame();
 		}
 		
 	}
 	
 	public void addFloatingLabel(String text, float x, float y) {
 		Gdx.app.log(LOG, "Floating Label!");
 		Label lbl = new Label(text, new LabelStyle(getSmallFont(), new Color(1,1,1,1)));
 		lbl.setPosition(x - lbl.getWidth() * 0.5f, y - lbl.getHeight() * 0.5f);
 		lbl.addAction(Actions.parallel(Actions.fadeOut(1f), Actions.moveBy(0, 64f, 1f)));
 		grpLevel.addActor(lbl);
 	}
 
     /*
      * Static methods
      */
 
     public static LevelScreen getInstance() {
         return instance;
     }
 }
