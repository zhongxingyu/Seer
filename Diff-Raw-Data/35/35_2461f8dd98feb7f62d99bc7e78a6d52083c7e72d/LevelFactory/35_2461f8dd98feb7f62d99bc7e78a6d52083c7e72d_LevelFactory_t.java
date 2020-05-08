 package edu.chl.codenameg.model.levels;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.tiled.TiledMap;
 
 import edu.chl.codenameg.model.Entity;
 import edu.chl.codenameg.model.Hitbox;
 import edu.chl.codenameg.model.Position;
 import edu.chl.codenameg.model.entity.Block;
 import edu.chl.codenameg.model.entity.GoalBlock;
 import edu.chl.codenameg.model.entity.LethalBlock;
 import edu.chl.codenameg.model.entity.LethalMovingBlock;
 import edu.chl.codenameg.model.entity.MovableBlock;
 import edu.chl.codenameg.model.entity.MovingBlock;
 import edu.chl.codenameg.model.entity.Water;
 
 public class LevelFactory {
 
 	private static LevelFactory instance;
 
 	TiledMap tiledmap;
 
 	private LevelFactory() {
 
 	}
 
 	public static LevelFactory getInstance() {
 		if (instance == null) {
 			instance = new LevelFactory();
 		}
 		return instance;
 
 	}
 
 	public Level getLevel(int i) throws IllegalArgumentException {
 		Level l = loadLevelFromFile(getLevelFilePath(i));
 		if (l != null) {
 			return l;
 		} else {
 			throw new IllegalArgumentException("The level does not exist");
 		}
 	}
 
 	public Level loadLevelFromFile(String path) {
 		// if(tiledmap != null) {
 		// for(Object o : tiledmap.getObjectGroups()) {
 		// System.out.println(o);
 		// if(o instanceof ObjectGroup) {
 		// GroupObject go = (GroupObject)o;
 		//
 		// System.out.println(go.index);
 		// }
 		// }
 		// }
 
 		List<Entity> entities = new ArrayList<Entity>();
 		Position spawnposition = new Position(0,0);
 		
 		for(int groupID=0; groupID<tiledmap.getObjectGroupCount(); groupID++) {
 //		int groupID = 0;
 //		System.out.println(tiledmap.getLayerCount());
 //			String blocktype = tiledmap.getLayerProperty(groupID,"layertype","fff");
 //			System.out.println(blocktype);
 		
 			
 			for(int objectID=0; objectID<tiledmap.getObjectCount(groupID); objectID++) {
 				
 				String name = tiledmap.getObjectName(groupID, objectID); 
 				
 				if(name.equals("Water")) {
 					Hitbox hitbox = new Hitbox(tiledmap.getObjectWidth(groupID, objectID)-1,tiledmap.getObjectHeight(groupID, objectID)-1);
 					Position position = new Position(tiledmap.getObjectX(groupID, objectID), tiledmap.getObjectY(groupID, objectID));
 					Entity water = new Water(position,hitbox);
 					entities.add(water);
 				}
 				if(name.equals("Block")) {
 					Hitbox hitbox = new Hitbox(tiledmap.getObjectWidth(groupID, objectID)-1,tiledmap.getObjectHeight(groupID, objectID)-1);
 					Position position = new Position(tiledmap.getObjectX(groupID, objectID), tiledmap.getObjectY(groupID, objectID));
 					Entity block = new Block(position,hitbox);
 					entities.add(block);
 				}
 				if(name.equals("LethalBlock")) {
 					Hitbox hitbox = new Hitbox(tiledmap.getObjectWidth(groupID, objectID)-1,tiledmap.getObjectHeight(groupID, objectID)-1);
 					Position position = new Position(tiledmap.getObjectX(groupID, objectID), tiledmap.getObjectY(groupID, objectID));
 					Entity lethalblock = new LethalBlock(position,hitbox);
 					entities.add(lethalblock);
 				}
 				if (name.equals("MovableBlock")) {
 					Hitbox hitbox = new Hitbox(tiledmap.getObjectWidth(groupID,
 							objectID) - 1, tiledmap.getObjectHeight(groupID,
 							objectID) - 1);
 					Position position = new Position(tiledmap.getObjectX(
 							groupID, objectID), tiledmap.getObjectY(groupID,
 							objectID));
 					Entity movableblock = new MovableBlock(position, hitbox);
 					entities.add(movableblock);
 				}
 				if (name.equals("MovingBlock") || name.equals("LethalMovingBlock")) {
 					String direction = tiledmap.getObjectProperty(groupID,
 							objectID, "Direction", "down");
 					Position endPosition = new Position(0, 0);
 					Position startPosition = new Position(0, 0);
 					Entity movingblock;
 					if (name.equals(("MovingBlock"))) {
 						movingblock = new MovingBlock();
 					} else {
 						movingblock = new LethalMovingBlock();
 					}
 					if (direction.equals("up")) {
 						endPosition = new Position(tiledmap.getObjectX(groupID,
 								objectID), tiledmap.getObjectY(groupID,
 								objectID));
 						startPosition = new Position(tiledmap.getObjectX(
 								groupID, objectID), tiledmap.getObjectY(
 								groupID, objectID)
 								+ tiledmap.getObjectHeight(groupID, objectID)
 								- movingblock.getHitbox().getHeight());
 					} else if (direction.equals("down")) {
 						startPosition = new Position(tiledmap.getObjectX(
 								groupID, objectID), tiledmap.getObjectY(
 								groupID, objectID));
 						endPosition = new Position(tiledmap.getObjectX(groupID,
 								objectID), tiledmap.getObjectY(groupID,
 								objectID)
 								+ tiledmap.getObjectHeight(groupID, objectID)
 								- movingblock.getHitbox().getHeight());
 					} else if (direction.equals("right")) {
 						startPosition = new Position(tiledmap.getObjectX(
 								groupID, objectID), tiledmap.getObjectY(
 								groupID, objectID));
 						endPosition = new Position(tiledmap.getObjectX(groupID,
 								objectID)
 								+ tiledmap.getObjectWidth(groupID, objectID)
 								- movingblock.getHitbox().getWidth(),
 								tiledmap.getObjectY(groupID, objectID));
 					} else if (direction.equals("left")) {
 						endPosition = new Position(tiledmap.getObjectX(groupID,
 								objectID), tiledmap.getObjectY(groupID,
 								objectID));
 
 						startPosition = new Position(tiledmap.getObjectX(
 								groupID, objectID)
 								+ tiledmap.getObjectWidth(groupID, objectID)
 								- movingblock.getHitbox().getWidth(),
 								tiledmap.getObjectY(groupID, objectID));
 					}
 					movingblock = new MovingBlock(startPosition, endPosition,
 							1000);
 					entities.add(movingblock);
 				}
 				if (name.equals("GoalBlock")) {
 					Hitbox hitbox = new Hitbox(tiledmap.getObjectWidth(groupID,
 							objectID) - 1, tiledmap.getObjectHeight(groupID,
 							objectID) - 1);
 					Position position = new Position(tiledmap.getObjectX(
 							groupID, objectID), tiledmap.getObjectY(groupID,
 							objectID));
 					Entity lethalblock = new GoalBlock(position, hitbox);
 					entities.add(lethalblock);
 				}
 				if (name.equals("Spawn")) {
 					spawnposition = new Position(tiledmap.getObjectX(groupID,
 							objectID), tiledmap.getObjectY(groupID, objectID));
 				}
 				if (name.equals("CheckPoint")){
 					spawnposition = new Position(tiledmap.getObjectX(groupID,
 							objectID), tiledmap.getObjectY(groupID, objectID));
 					
 				}
 			}
 		}
 
 		Level level = new GeneratedLevel(entities, spawnposition, 1);
 
 		return level;
 
 	}
 
 	public String getLevelFilePath(int i) {
 		return "levels/level" + i + ".tmx";
 	}
 
 	public void setTiledMap(TiledMap tm) {
 		this.tiledmap = tm;
 	}
 
 	private class GeneratedLevel implements Level {
 
 		List<Entity> entities;
 		Position spawnposition;
 		int numberPlayers = 1;
 
 		public GeneratedLevel(List<Entity> entities, Position spawnposition,
 				int numberPlayers) {
 			this.entities = entities;
 			this.spawnposition = spawnposition;
 			this.numberPlayers = numberPlayers;
 		}
 
 		@Override
		public List<Entity> getListOfEntities() {
 			// TODO Auto-generated method stub
 			return entities;
 		}
 		
 		@Override
 		public Position getStartPosition() {
 			return spawnposition;
 		}
 
 		@Override
 		public int getAmountOfPlayers() {
 			return numberPlayers;
 		}
 
 	}
 
 }
