 
 package com.wartricks.systems;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.World;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.EntityProcessingSystem;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Vector3;
 import com.wartricks.boards.GameMap;
 import com.wartricks.components.MapPosition;
 import com.wartricks.components.Move;
 import com.wartricks.components.Path;
 import com.wartricks.components.PlayerSelected;
 import com.wartricks.custom.Pair;
 import com.wartricks.lifecycle.WartricksGame;
 import com.wartricks.utils.EntityFactory;
 
 public class PlayerInputSystem extends EntityProcessingSystem implements InputProcessor {
     @Mapper
     ComponentMapper<MapPosition> mpm;
 
     @Mapper
     ComponentMapper<Path> ptm;
 
     private OrthographicCamera camera;
 
     private Vector3 mouseVector;
 
     private GameMap gameMap;
 
     private WartricksGame game;
 
     private State state, lastState;
 
     private Pair moveTarget;
 
     private int selectedEntity;
 
     private enum State {
         DEFAULT, ENTITY_SELECTED, DRAGGING, FIND_PATH,
     };
 
     @SuppressWarnings("unchecked")
     public PlayerInputSystem(OrthographicCamera screenCamera, GameMap map, World gameWorld,
             WartricksGame game) {
         super(Aspect.getAspectForAll(PlayerSelected.class, MapPosition.class));
         camera = screenCamera;
         state = State.DEFAULT;
         lastState = State.DEFAULT;
         gameMap = map;
         this.game = game;
         this.setWorld(gameWorld);
         selectedEntity = -1;
     }
 
     @Override
     protected void process(Entity e) {
         if (state == State.FIND_PATH) {
             state = State.ENTITY_SELECTED;
             lastState = State.FIND_PATH;
             // Get the entity's position
             final Path path = ptm.get(e);
             MapPosition mapPosition = mpm.getSafe(e);
             // Add a Movement component to the entity
             gameMap.entityLocations[mapPosition.x][mapPosition.y] = -1;
             gameMap.entityLocations[moveTarget.x][moveTarget.y] = e.getId();
             final Move movement = new Move(mapPosition.x, mapPosition.y, moveTarget.x, moveTarget.y);
             mapPosition = new MapPosition(moveTarget.x, moveTarget.y);
             path.path.add(movement);
             // TODO uncomment to reset path on click
             // path.path.clear();
             e.removeComponent(MapPosition.class);
             e.addComponent(mapPosition);
             e.changedInWorld();
         }
     }
 
     @Override
     public boolean keyDown(int keycode) {
         return false;
     }
 
     @Override
     public boolean keyUp(int keycode) {
         return false;
     }
 
     @Override
     public boolean keyTyped(char character) {
         return false;
     }
 
     @Override
     public boolean touchDown(int screenX, int screenY, int pointer, int button) {
         return false;
     }
 
     @Override
     public boolean touchUp(int screenX, int screenY, int pointer, int button) {
         if (button == 0) {
             // Are they releasing from dragging?
             if (state == State.DRAGGING) {
                 state = lastState;
                 lastState = State.DRAGGING;
                 return true;
             }
             // Otherwise, get the coordinates they clicked on
             final Pair coords = gameMap.mapTools.window2world(Gdx.input.getX(), Gdx.input.getY(),
                     camera);
            // TODO hardcoded
            if ((coords.x >= 0) && (coords.x <= 9) && (coords.y >= 0) && (coords.y <= 7)) {
                 // Check the entityID of the cell they click on
                 final int entityId = gameMap.getEntityAt(coords.x, coords.y);
                 // If it's an actual entity (not empty) then "select" it (unless it's already
                 // selected)
                 if ((entityId > -1) && (entityId != selectedEntity)) {
                     // If there was previously another entity selected, "deselect" it
                     if (selectedEntity > -1) {
                         final Entity old = world.getEntity(selectedEntity);
                         old.removeComponent(PlayerSelected.class);
                         // TODO
                         // ptm.getSafe(old).path.clear();
                         old.changedInWorld();
                     }
                     // Now select the current entity
                     selectedEntity = entityId;
                     final Entity e = world.getEntity(selectedEntity);
                     e.addComponent(new PlayerSelected());
                     e.changedInWorld();
                     EntityFactory.createClick(world, coords.x, coords.y, 0.4f, 4f, 0.15f)
                             .addToWorld();
                     lastState = state;
                     state = State.ENTITY_SELECTED;
                     return true;
                 }
                 // Are they clicking to find a new path?
                 else if (state == State.ENTITY_SELECTED) {
                     lastState = state;
                     state = State.FIND_PATH;
                     moveTarget = coords;
                     return true;
                 }
             }
         } else if (button == 1) {
             final Vector3 origin = new Vector3(-200, -50, 0);
             camera.zoom = 0.6f;
             camera.setToOrtho(false);
             camera.translate(origin);
         }
         return false;
     }
 
     @Override
     public boolean touchDragged(int screenX, int screenY, int pointer) {
         // if (state != State.DRAGGING) {
         // lastState = state;
         // state = State.DRAGGING;
         // }
         // final Vector2 delta = new Vector2(-camera.zoom * Gdx.input.getDeltaX(), camera.zoom
         // * Gdx.input.getDeltaY());
         // camera.translate(delta);
         return true;
     }
 
     @Override
     public boolean mouseMoved(int screenX, int screenY) {
         return false;
     }
 
     @Override
     public boolean scrolled(int amount) {
         if (((camera.zoom > 0.2f) || (amount == 1)) && ((camera.zoom < 8) || (amount == -1))) {
             camera.zoom += amount * 0.1;
         }
         return false;
     }
 }
