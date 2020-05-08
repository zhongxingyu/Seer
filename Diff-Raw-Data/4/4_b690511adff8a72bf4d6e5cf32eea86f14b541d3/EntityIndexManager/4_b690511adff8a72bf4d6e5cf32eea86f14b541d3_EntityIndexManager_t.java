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
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import com.artemis.Entity;
 import com.artemis.EntityManager;
 import com.artemis.ComponentMapper;
 import com.artemis.annotations.Mapper;
 import com.artemis.managers.GroupManager;
 import com.artemis.utils.ImmutableBag;
 
 import org.geekygoblin.nedetlesmaki.game.Group;
 import org.geekygoblin.nedetlesmaki.game.utils.Mouvement;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Position;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Square;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Movable;
 
 /**
  *
  * @author natir
  */
 @Singleton
 public class EntityIndexManager extends EntityManager {
     
     private final Square[][] index;
     private final Stack<ArrayList<Mouvement>> oldIndex;
     
     @Mapper
     ComponentMapper<Position> positionMapper;
 
     @Inject
     public EntityIndexManager() {
 	super();
 	this.index = new Square[15][15];
 	this.oldIndex = new Stack();
     }
 
     @Override
     public void added(Entity e) {
 	Position p = e.getComponent(Position.class);
 	
 	if(p != null) {
	    if(this.index[p.getX()][p.getY()] == null) {
                this.index[p.getX()][p.getY()] = new Square();
            }
 	    this.index[p.getX()][p.getY()].add(e);
 	    super.added(e);
 	}
     }
 
     @Override
     public void deleted(Entity e) {
 	Position p = e.getComponent(Position.class);
 	
 	if(p != null) {
 	    this.index[p.getX()][p.getY()].remove(e);
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
 
     public Square[][] getThisWorld() {
 	return this.index;
     }
     
     public ImmutableBag<Entity> getAllPlate() {
         return world.getManager(GroupManager.class).getEntities(Group.PLATE);
     }
 }
