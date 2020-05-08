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
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.VoidEntitySystem;
 
 import im.bci.nanim.IAnimationCollection;
 
 import org.geekygoblin.nedetlesmaki.game.Game;
 import org.geekygoblin.nedetlesmaki.game.manager.EntityIndexManager;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Plate;
 import org.geekygoblin.nedetlesmaki.game.components.visual.Sprite;
 import org.geekygoblin.nedetlesmaki.game.components.visual.SpritePuppetControls;
 import org.geekygoblin.nedetlesmaki.game.components.gamesystems.Position;
 import org.geekygoblin.nedetlesmaki.game.constants.AnimationType;
 import org.geekygoblin.nedetlesmaki.game.assets.Assets;
 import org.geekygoblin.nedetlesmaki.game.utils.Mouvement;
 
 import org.lwjgl.util.vector.Vector3f;
 
 /**
  *
  * @author natir
  */
 public class UpdateLevelVisualSystem extends VoidEntitySystem {
 
     @Mapper
     ComponentMapper<Sprite> spriteMapper;
     @Mapper
     ComponentMapper<Plate> plateMapper;
 
     private final Assets assets;
     private Game game;
     private int nbIndexSaved;
     private final EntityIndexManager index;
 
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
             nbIndexSaved = index.sizeOfStack();
             ArrayList<Mouvement> change = this.index.getChangement();
 
             if (change != null) {
                 for (int i = 0; i != change.size(); i++) {
 
                     ArrayList<Position> tmpLP = change.get(i).getPositionList();
                     ArrayList<AnimationType> tmpLA = change.get(i).getAnimationList();
                     for (Iterator itP = tmpLP.iterator(), itA = tmpLA.iterator(); itP.hasNext() || itA.hasNext();) {
                         this.moveSprite(change.get(i).getEntity(), (Position) itP.next(), (AnimationType) itA.next());
                     }
                 }
             }
         }
     }
 
     private void moveSprite(Entity e, Position diff, AnimationType a) {
 
         Sprite sprite = e.getComponent(Sprite.class);
         Vector3f pos = sprite.getPosition();
         SpritePuppetControls updatable = new SpritePuppetControls(sprite);
         Position p = new Position((int) pos.x + diff.getY(), (int) pos.y + diff.getX());
         
         IAnimationCollection nedAnim = this.assets.getAnimations("ned.nanim");
         IAnimationCollection makiAnim = this.assets.getAnimations("maki.nanim");
         
         if (a == AnimationType.no) {
             updatable.moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_right) {
             updatable.startAnimation(nedAnim.getAnimationByName("walk_right"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_left) {
             updatable.startAnimation(nedAnim.getAnimationByName("walk_left"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_down) {
             updatable.startAnimation(nedAnim.getAnimationByName("walk_down"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_up) {
             updatable.startAnimation(nedAnim.getAnimationByName("walk_up"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_push_right) {
             updatable.startAnimation(nedAnim.getAnimationByName("push_right"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_push_left) {
             updatable.startAnimation(nedAnim.getAnimationByName("push_left"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_push_down) {
             updatable.startAnimation(nedAnim.getAnimationByName("push_down"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.ned_push_up) {
             updatable.startAnimation(nedAnim.getAnimationByName("push_up"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.box_destroy) {
            sprite = new Sprite();
         } else if (a == AnimationType.maki_green_one) {
             updatable.startAnimation(makiAnim.getAnimationByName("maki_green_one"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.maki_orange_one) {
             updatable.startAnimation(makiAnim.getAnimationByName("maki_orange_one"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         } else if (a == AnimationType.maki_blue_one) {
             updatable.startAnimation(makiAnim.getAnimationByName("maki_blue_one"))
                     .moveTo(new Vector3f(p.getX(), p.getY(), pos.z), 0.5f)
                     .stopAnimation();
         }
 
        sprite.setPosition(new Vector3f(p.getX(), p.getY(), pos.z));

         e.addComponent(updatable);
         e.changedInWorld();
     }
 
 //    ArrayList<Mouvement> compressMouvement(ArrayList<Mouvement> mouv) {
 //        ArrayList<Mouvement> compress = new ArrayList();
 //
 //        if (mouv.size() > 1) {
 //            Mouvement old = mouv.get(0);
 //
 //            for (int i = 1; i != mouv.size(); i++) {
 //                if (this.compareAnimationList(old.getAnimationList(), mouv.get(i).getAnimationList())&& this.comparePositionList(old.getPositionList(), mouv.get(i).getPositionList()) && old.getEntity() == mouv.get(i).getEntity()) {
 //                    compress.add(new Mouvement(old.getAnimationList(), PosOperation.sum(old), null)))
 //                }
 //            }
 //        }
 //
 //        return compress;
 //    }
 //
 //    boolean compareAnimationList(ArrayList<AnimationType> lA, ArrayList<AnimationType> lB) {
 //        if (lA.size() != lB.size()) {
 //            return false;
 //        }
 //
 //        if (lA.size() >= 2) {
 //            Iterator itA = lA.iterator();
 //            Iterator itB = lB.iterator();
 //            AnimationType animA = (AnimationType) itA.next();
 //            AnimationType animB = (AnimationType) itB.next();
 //            
 //            while (itA.hasNext() && itB.hasNext()) {
 //                if (animA != animB) {
 //                    return false;
 //                }
 //                
 //                animA = (AnimationType) itA.next();
 //                animB = (AnimationType) itB.next();    
 //            }
 //            
 //            return true;
 //        }
 //        else if (lA.size() == 1) {
 //            Iterator itA = lA.iterator();
 //            Iterator itB = lB.iterator();
 //            AnimationType animA = (AnimationType) itA.next();
 //            AnimationType animB = (AnimationType) itB.next();
 //            
 //            return animA == animB;
 //        }
 //        
 //        return false;
 //    }
 //    
 //    boolean comparePositionList(ArrayList<Position> lA, ArrayList<Position> lB) {
 //        if (lA.size() != lB.size()) {
 //            return false;
 //        }
 //
 //        if (lA.size() >= 2) {
 //            Iterator itA = lA.iterator();
 //            Iterator itB = lB.iterator();
 //            Position posA = (Position) itA.next();
 //            Position posB = (Position) itB.next();
 //            
 //            while (itA.hasNext() && itB.hasNext()) {
 //                if (!PosOperation.equale(posA, posB)) {
 //                    return false;
 //                }
 //                
 //                posA = (Position) itA.next();
 //                posB = (Position) itB.next();    
 //            }
 //            
 //            return true;
 //        }
 //        else if (lA.size() == 1) {
 //            Iterator itA = lA.iterator();
 //            Iterator itB = lB.iterator();
 //            Position posA = (Position) itA.next();
 //            Position posB = (Position) itB.next();
 //            
 //        }
 //        
 //        return false;
 //    }
 }
