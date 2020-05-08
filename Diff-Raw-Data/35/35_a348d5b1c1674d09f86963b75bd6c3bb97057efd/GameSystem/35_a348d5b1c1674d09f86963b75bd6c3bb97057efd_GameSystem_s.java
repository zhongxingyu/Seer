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
 package org.geekygoblin.nedetlesmaki.game.systems;
 
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.ComponentMapper;
 
 import org.geekygoblin.nedetlesmaki.game.components.Pushable;
 import org.geekygoblin.nedetlesmaki.game.components.Pusher;
 import org.geekygoblin.nedetlesmaki.game.components.Position;
 import org.geekygoblin.nedetlesmaki.game.systems.EntityPosIndexSystem;
 import org.geekygoblin.nedetlesmaki.game.utils.PosOperation;
 
 /**
  *
  * @author natir
  */
 public class GameSystem {
 
     @Mapper
     ComponentMapper<Pushable> pushableMapper;
     @Mapper
     ComponentMapper<Pusher> pusherMapper;
     @Mapper
     ComponentMapper<Position> positionMapper;
 
     private EntityPosIndexSystem index;
 
    public GameSystem(EntityPosIndexSystem index) {
 	this.index = index;
     }
 
     public boolean moveEntity(Entity e, Position newP) {
	
 	/*New pos is void*/
 	if(positionIsVoid(newP)) {
	    Position oldP = this.positionMapper.get(e);
 	    index.saveWorld();
 	    index.moveEntity(oldP.getX(), oldP.getY(), newP.getX(), newP.getY());
 	    return true;
 	}
 	else {
 	    /*If Entity move is a pusher Entity*/
 	    if(this.isPusherEntity(e)) {
 		Entity otherE = this.index.getEntity(newP.getX(), newP.getY());
 		if(!otherE.isActive()) {
 		    return false;
 		}
 		/*If Entity in newPos is pushable*/
 		if(this.isPushableEntity(otherE)) {
 		    return this.moveEntity(otherE, PosOperation.sum(PosOperation.deduction(positionMapper.get(e), newP), newP));
 		}
 	    }
 	}
 	
 	return false;
     }
 
     public boolean positionIsVoid(Position p) {
 	Entity tmpE = index.getEntity(p.getX(), p.getY());
	if(!tmpE.isActive()) {
 	    return false;
 	}
 	
 	return true;
     }
 
     public boolean isPushableEntity(Entity e) {
 	Pushable p = this.pushableMapper.getSafe(e);
 	if(p != null) {
 	    if(this.pushableMapper.getSafe(e).isPushable()) {
 		return true;
 	    }
 	}
 
 	return false;
     }
 
     public boolean isPusherEntity(Entity e) {
 	Pusher p = this.pusherMapper.getSafe(e);
 	if(p != null) {
 	    if(this.pusherMapper.getSafe(e).isPusher()) {
 		return true;
 	    }
 	}
 
 	return false;
     }
 
     public Position wayFreeTo(Position begin, Position end) {
 	Position delta = PosOperation.deduction(begin, end);
 	int base, max;
 	if(delta.getX() != 0) {
 	    base = begin.getX();
 	    max = end.getX();
 	    return this.testObjOnWayX(base, max, begin.getY());
        	}
 	else {
 	    base = begin.getY();
 	    max = end.getY();
 	    return this.testObjOnWayX(base, max, begin.getX());	    
 	}
     }
 
     private Position testObjOnWayX(int base, int max, int y) {
 	
 	for(int i = base; i != max; i++) {
 	    Entity e = this.index.getEntity(i, y);
 	    if(e != null) {
 		return new Position(i, y);
 	    }
 	}
 	
 	return new Position(max, y);
     }
 
     private Position testObjOnWayY(int base, int max, int x) {
 	
 	for(int i = base; i != max; i++) {
 	    Entity e = this.index.getEntity(x, i);
 	    if(e != null) {
 		return new Position(x, i);
 	    }
 	}
 	
 	return new Position(x, max);
     }
 }
