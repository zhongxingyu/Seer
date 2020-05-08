 /*
  * Copyright © 2013, Pierre Marijon <pierre@marijon.fr>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
  * copies of the Software, and to permit persons to whom the Software is 
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in 
  * all copies or substantial portions of the Software.
  *
  * The Software is provided "as is", without warranty of any kind, express or 
  * implied, including but not limited to the warranties of merchantability, 
  * fitness for a particular purpose and noninfringement. In no event shall the 
  * authors or copyright holders X be liable for any claim, damages or other 
  * liability, whether in an action of contract, tort or otherwise, arising from,
  * out of or in connection with the software or the use or other dealings in the
  * Software.
  */
 package org.geekygoblin.nedetlesmaki.game.manager;
 
 import java.util.Stack;
 import java.util.ArrayList;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import com.artemis.Entity;
 import com.artemis.EntityManager;
 import com.artemis.ComponentMapper;
 import com.artemis.annotations.Mapper;
 import com.artemis.managers.GroupManager;
 import com.artemis.utils.ImmutableBag;
 
 import org.geekygoblin.nedetlesmaki.game.Game;
 import org.geekygoblin.nedetlesmaki.game.Group;
 import org.geekygoblin.nedetlesmaki.game.utils.Mouvement;
 import org.geekygoblin.nedetlesmaki.game.constants.ColorType;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.BlockOnPlate;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Boostable;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Color;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Destroyable;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Destroyer;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Position;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Square;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Movable;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Plate;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Pushable;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Pusher;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Stairs;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.StopOnPlate;
 
 /**
  *
  * @author natir
  */
 @Singleton
 public class EntityIndexManager extends EntityManager {
     
     private Square[][] index;
     private final Stack<ArrayList<Mouvement>> oldIndex;
     
     @Mapper
     ComponentMapper<Pushable> pushableMapper;
     @Mapper
     ComponentMapper<Pusher> pusherMapper;
     @Mapper
     ComponentMapper<Position> positionMapper;
     @Mapper
     ComponentMapper<Movable> movableMapper;
     @Mapper
     ComponentMapper<Plate> plateMapper;
     @Mapper
     ComponentMapper<Color> colorMapper;
     @Mapper
     ComponentMapper<Boostable> boostMapper;
     @Mapper
     ComponentMapper<BlockOnPlate> blockOnPlateMapper;
     @Mapper
     ComponentMapper<StopOnPlate> stopOnPlateMapper;
     @Mapper
     ComponentMapper<Destroyer> destroyerMapper;
     @Mapper
     ComponentMapper<Destroyable> destroyableMapper;
     @Mapper
     ComponentMapper<Stairs> stairsMapper;
     
     @Inject
     public EntityIndexManager() {
 	super();
 	this.index = new Square[15][15];
 	this.oldIndex = new Stack();
     }
 
     @Override
     public void added(Entity e) {
         Position p = this.getPosition(e);
 	
         if(p != null) {
             if(this.index[p.getX()][p.getY()] == null) {
                 this.index[p.getX()][p.getY()] = new Square();
             }
 
             if (this.getPlate(e) != null && !this.index[p.getX()][p.getY()].getWith(Plate.class).isEmpty()) {
                 return ;
             }
             
             this.index[p.getX()][p.getY()].add(e);
             super.added(e);
         }
     }
 
     @Override
     public void deleted(Entity e) {
     Position p = this.getPosition(e);
 	
     if(p != null) {
         Square s = this.index[p.getX()][p.getY()];
         if(s != null) {
             s.remove(e);
         }
         
         super.deleted(e);
         }
     }
 
      public ArrayList<Entity> getEntity(int x, int y) {
 
 	 Square test = this.getSquare(x, y);
 
 	 if(test != null) { return test.getAll(); }
 	 else { return null; }
      }
 
      public Square getSquare(int x, int y) {
 
 	 if(x > 14 || x < 0 || y > 14 || y < 0) {
 	     return null;
 	 }
  
 	 Square test = index[x][y];
 
 	 if(test != null) { return test; }
 	 else { return null; }
      }
 
     public boolean moveEntity(int x, int y, int x2, int y2) {
 	if(index[x][y] == null) {
 	    return false;
 	}
 	
         if(x2 > 14 || x2 < 0 || y2 > 14 || y2 < 0) {
 	    return false;
 	}
         
 	ArrayList<Entity> tmpE = index[x][y].getWith(Movable.class);
         
 	Square newC = this.index[x2][y2];
 	
 	if(newC != null) {
 	    this.index[x2][y2].add(tmpE.get(0));
 	}
 	else {
 	    this.index[x2][y2] = new Square();
 	    this.index[x2][y2].add(tmpE.get(0));
 	}
 
 	this.index[x][y].remove(tmpE.get(0));
 
 	return true;
     }
 
     public boolean addMouvement(ArrayList<Mouvement> vM) {
         return this.oldIndex.add(vM);
     }
     
     public void cleanIndex() {
         this.index = new Square[15][15];
     }
     
     public void cleanStack() {
 	this.oldIndex.clear();
     }
 
     public int sizeOfStack() {
 	return this.oldIndex.size();
     }
 
     public ArrayList<Mouvement> getChangement() {
 	if(!this.oldIndex.empty()) {
 	    ArrayList<Mouvement> o = this.oldIndex.peek();
 	    if(o != null) {
 		return o;
 	    }
 	}
 
 	return null;
     }
     
     public ArrayList<Mouvement> pop() {
         if (this.oldIndex.isEmpty()) {
             return null;
         } 
         
         return this.oldIndex.pop();
     }
     
     public ImmutableBag<Entity> getAllPlate() {
         return world.getManager(GroupManager.class).getEntities(Group.PLATE);
     }
 
     public ImmutableBag<Entity> getAllStairs() {
         return world.getManager(GroupManager.class).getEntities(Group.STAIRS);
     }
 
     public Entity getNed() {
         return ((Game) world).getNed();
     }
     
     //Utills 
     public boolean positionIsVoid(Position p) {
         Square s = this.getSquare(p.getX(), p.getY());
 
         if (s != null) {
             ArrayList<Entity> plate = s.getWith(Plate.class);
             ArrayList<Entity> all = s.getAll();
 
             if (all.size() == plate.size()) {
                 return true;
             } else {
                 return false;
             }
         }
 
         return true;
     }
 
     public boolean isStairs(Position newP) {
         Square s = this.getSquare(newP.getX(), newP.getY());
 
         ArrayList<Entity> stairsEntity = s.getWith(Stairs.class);
 
         if (stairsEntity.isEmpty()) {
             return false;
         }
 
         Entity e = stairsEntity.get(0);
         
         Stairs st = this.getStairs(e);
 
         if (st != null) {
             if (st.isOpen()) {
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean isPushableEntity(Entity e) {
         Pushable p = this.pushableMapper.getSafe(e);
 
         if (p != null) {
             if (p.isPushable()) {
                 return p.isPushable();
             }
         }
 
         return false;
     }
 
     public boolean isPusherEntity(Entity e) {
         Pusher p = this.pusherMapper.getSafe(e);
 
         if (p != null) {
             if (p.isPusher()) {
                 return p.isPusher();
             }
         }
 
         return false;
     }
 
     public boolean isDestroyer(Entity e) {
         Destroyer d = this.destroyerMapper.getSafe(e);
 
         if (d != null) {
             if (d.destroyer()) {
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean isDestroyable(Entity e) {
         Destroyable d = this.destroyableMapper.getSafe(e);
 
         if (d != null) {
             if (d.destroyable()) {
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean stopOnPlate(Entity e) {
         StopOnPlate p = stopOnPlateMapper.getSafe(e);
         
         if(p == null) {
             return false;
         }
         
         return p.stop();
     }
 
     public boolean isBlockOnPlate(Entity e) {
         BlockOnPlate p = blockOnPlateMapper.getSafe(e);
         
         if(p == null) {
             return false;
         }
         
         return p.block();
     }
     
     public int getMovable(Entity e) {
         Movable m = this.movableMapper.getSafe(e);
 
         if (m != null) {
             return m.getNbCase();
         }
 
         return 0;
     }
 
     public int getBoost(Entity e) {
         Boostable b = this.boostMapper.getSafe(e);
 
         if (b != null) {
             return b.getNbCase();
         }
 
         return 20;
     }
 
     public Position getPosition(Entity e) {
         Position p = this.positionMapper.getSafe(e);
 
         if (p != null) {
             return p;
         }
 
         return null;
     }
     
     public Color getColor(Entity e) {
         Color c = this.colorMapper.getSafe(e);
          if (c == null) {
             return null;
         }
         
          return c;
     }
     
     public ColorType getColorType(Entity e) {    
          return this.getColor(e).getColor();
     }
     
     public Plate getPlate(Entity e) {
         Plate p = plateMapper.getSafe(e);
         
         if(p == null) {
             return null;
         }
         
         return p;
     }
     
     public Stairs getStairs(Entity e) {
 
         Stairs st = this.stairsMapper.getSafe(e);
 
         if(st == null) {
             return null;
         }
         
         return st;
     }
     
 }
