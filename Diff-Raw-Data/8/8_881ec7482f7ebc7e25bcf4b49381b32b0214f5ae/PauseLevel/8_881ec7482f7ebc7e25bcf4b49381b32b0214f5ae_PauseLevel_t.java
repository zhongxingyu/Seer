 package net.fourbytes.shadow;
 
 import java.util.Vector;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Array;
 
 import net.fourbytes.shadow.Input.Key;
 import net.fourbytes.shadow.map.ShadowMap;
 
 public class PauseLevel extends TitleLevel {
 
 	public PauseLevel() {
 		this(null);
 	}
 
 	public PauseLevel(MenuLevel parent) {
 		super(parent);
 		
 		Array<MenuItem> items = new Array<MenuItem>();
 		
 		items.add(new MenuItem(this, "Continue", new Runnable(){public void run(){
 			Shadow.level = bglevel;
 			}}));
 		items.add(new MenuItem(this, "Save", new Runnable(){public void run(){
			//TODO
 			ShadowMap map = ShadowMap.createFrom(bglevel);
 			map.save(Gdx.files.local("TROLL.smf"));
 			Shadow.level = bglevel;
 			}}));
 		items.add(new MenuItem(this, "Load", new Runnable(){public void run(){
 			//TODO
 			ShadowMap map = ShadowMap.loadFile(Gdx.files.local("TROLL.smf"));
 			for (Layer layer : bglevel.layers.values()) {
 				for (Block b : layer.blocks) {
 					layer.remove(b);
 				}
 				for (Entity e : layer.entities) {
 					layer.remove(e);
 				}
 			}
 			map.fillLevel(bglevel);
 			Shadow.level = bglevel;
 			}}));
 		items.add(new MenuItem(this, "Main Menu", new Runnable(){public void run(){
 			Shadow.level = new TitleLevel();
 			}}));
 		
 		items.addAll(this.items);
 		for (MenuItem item : this.items) {
 			if (item.text.toLowerCase().startsWith("start ")) {
 				items.removeValue(item, true);
 			}
 		}
 		this.items = items;
 		
 		hasvoid = false;
 		ready = true;
 	}
 	
 }
