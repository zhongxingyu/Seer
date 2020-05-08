 package com.tedit.engine.entity;
 
 import java.util.ArrayList;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 import android.util.Log;
 
 import com.tedit.engine.GameRunner;
 import com.tedit.engine.graphics.Sprite;
import com.tedit.engine.resource.ResourceManager;
 
 //Should be developed to handler multiple scenes
 //persistance in entities
 //And handling of an entity' lifetime
 public class EntityManager
 {
     private ArrayList<Entity> entities = new ArrayList<Entity>();
     private GameRunner game;
     public EntityManager(GameRunner game)
     {
         this.game=game;
     }
     public void createTestEntity()
     {
         
         String path = game.externalPath;
         Log.d("IO", "PATH TO SPRITE"+path+"/skate1.png");
         Bitmap bm = BitmapFactory.decodeFile(path+"/skate1.png");
         
         Sprite testSprite = new Sprite(bm,1);
        Entity testEntity = new Entity(138,ResourceManager.getInstance().LoadSprite(path+"/skate1.png"));
         game.getEventHandler().subscribe(14096, testEntity);
         entities.add(testEntity);
     }
     public void updateWorld(float deltaTime)
     {
         for(Entity e : entities)
         {
             e.update(deltaTime);
         }
     }
     public void renderWorld()
     {
         for(Entity e : entities)
         {
             e.draw(game.getRenderer());
         }
     }
 }
