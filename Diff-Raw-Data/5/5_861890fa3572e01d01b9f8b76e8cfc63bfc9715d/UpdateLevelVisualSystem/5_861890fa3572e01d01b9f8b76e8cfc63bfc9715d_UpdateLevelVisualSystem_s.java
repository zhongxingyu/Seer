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
 
 import javax.inject.Inject;
 
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.VoidEntitySystem;
 
 import im.bci.nanim.IAnimationCollection;
 
 import org.geekygoblin.nedetlesmaki.game.Game;
 import org.geekygoblin.nedetlesmaki.game.manager.EntityIndexManager;
 import org.geekygoblin.nedetlesmaki.game.components.EntityPosIndex;
 import org.geekygoblin.nedetlesmaki.game.components.visual.Sprite;
 import org.geekygoblin.nedetlesmaki.game.components.visual.SpritePuppetControls;
 import org.geekygoblin.nedetlesmaki.game.components.EntityPosIndex;
 import org.geekygoblin.nedetlesmaki.game.components.Position;
 import org.geekygoblin.nedetlesmaki.game.utils.PosOperation;
 import org.geekygoblin.nedetlesmaki.game.assets.Assets;
 
 import org.lwjgl.util.vector.Vector3f;
 
 /**
  *
  * @author natir
  */
 public class UpdateLevelVisualSystem extends VoidEntitySystem {
 
     @Mapper
     ComponentMapper<Sprite> spriteMapper;
 
     private final Assets assets;
     private Game game;
     private int nbIndexSaved;
     private EntityIndexManager index;
 
     @Inject
     public UpdateLevelVisualSystem(Assets assets, EntityIndexManager indexSystem) {
         this.assets = assets;
         this.nbIndexSaved = 0;
 	this.index = indexSystem;	
     }
 
     @Override
     protected void processSystem() {
         game = (Game) world;
  
 	if (index.sizeOfStack() != nbIndexSaved) {
 
 	    EntityPosIndex old = this.index.getLastWorld();
 	    for (int i = 0; i != 15; i++) {
                 for (int j = 0; j != 15; j++) {
                     Entity oE = old.getEntityWithPos(i, j);
                     if (oE != null) {
 
 			Position oEP = oE.getComponent(Position.class);
 			Position newP = new Position(i, j);
                         Position diff = PosOperation.deduction(oEP, newP);

                         if (diff.getX() != 0 || diff.getY() != 0) {
 			    if (oE == game.getNed()) {
 				this.moveNed(oE, diff);
                                 this.index.saveWorld();
 				this.nbIndexSaved = index.sizeOfStack();
 			    } else {
                                 this.moveSprite(oE, diff);
                                 this.index.saveWorld();
 				this.nbIndexSaved = index.sizeOfStack();
 			    }
                         }
                     }
                 }
             }
         }
     }
 
     private void moveNed(Entity e, Position diff) {
         Sprite sprite = e.getComponent(Sprite.class);
         IAnimationCollection anims = this.assets.getAnimations("ned.nanim");
         Vector3f pos = sprite.getPosition();
         SpritePuppetControls updatable = new SpritePuppetControls(sprite);
 
         if (diff.getX() > 0) {
             updatable.startAnimation(anims.getAnimationByName("walk_down"))
                     .moveTo(new Vector3f(pos.x, pos.y + 1f, pos.z), 0.5f)
                     .stopAnimation();
             e.addComponent(updatable);
             e.changedInWorld();
         } else if (diff.getX() < 0) {
             updatable.startAnimation(anims.getAnimationByName("walk_up"))
                     .moveTo(new Vector3f(pos.x, pos.y - 1f, pos.z), 0.5f)
                     .stopAnimation();
             e.addComponent(updatable);
             e.changedInWorld();
         } else if (diff.getY() > 0) {
             updatable.startAnimation(anims.getAnimationByName("walk_right"))
                     .moveTo(new Vector3f(pos.x + 1f, pos.y, pos.z), 0.5f)
                     .stopAnimation();
             e.addComponent(updatable);
             e.changedInWorld();
         } else if (diff.getY() < 0) {
             updatable.startAnimation(anims.getAnimationByName("walk_left"))
                     .moveTo(new Vector3f(pos.x - 1f, pos.y, pos.z), 0.5f)
                     .stopAnimation();
             e.addComponent(updatable);
             e.changedInWorld();
         }
     }
 
     private void moveSprite(Entity e, Position diff) {
         Sprite sprite = e.getComponent(Sprite.class);
         Vector3f pos = sprite.getPosition();
         SpritePuppetControls updatable = new SpritePuppetControls(sprite);
 
	updatable.moveTo(new Vector3f(pos.x - diff.getX(), pos.y - diff.getY(), pos.z), 0.5f);
         
 	e.addComponent(updatable);
         e.changedInWorld();
     }
 }
