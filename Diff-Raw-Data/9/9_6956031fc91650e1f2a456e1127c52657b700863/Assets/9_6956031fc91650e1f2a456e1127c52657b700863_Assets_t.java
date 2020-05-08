 package com.vgdc.merge.assets;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.utils.Json;
import com.vgdc.merge.Ability;
 import com.vgdc.merge.Controller;
 import com.vgdc.merge.EntityData;
 
 public class Assets {
 	public AssetManager manager = new AssetManager();
 	public HashMap<String, EntityData> entityDataMap = new HashMap<String, EntityData>();
 	public HashMap<String, Animation> animationMap = new HashMap<String, Animation>();
 
 	private Json json = new Json();
 
 	public <T> T createObjectFromJson(Class<T> type, String path) {
 		return json.fromJson(type, Gdx.files.internal(path));
 	}
 
 	public void loadAssets(String path) {
 		AssetList assets = createObjectFromJson(AssetList.class, path);
 
 		for (String soundPath : assets.sounds) {
 			manager.load(soundPath, Sound.class);
 		}
 
 		for (String texturePath : assets.textures) {
 			manager.load(texturePath, Texture.class);
 		}
 
 		for (String animationPath : assets.animations) {
 			loadAnimation(animationPath);
 		}
 
 		for (String name : assets.entities.keySet()) {
 			loadEntityData(name, assets.entities.get(name));
 		}
 	}
 
 	public void dispose() {
 		manager.dispose();
 	}
 
 	private void loadEntityData(String name, String path) {
 		EntityTemplate template = createObjectFromJson(EntityTemplate.class, path);
 
 		EntityData data = new EntityData();
 		data.maxHealth = template.maxHealth;
 		data.jumpHeight = template.jumpHeight;
 		data.moveSpeed = template.moveSpeed;
 		
 		ArrayList<Animation> animations = new ArrayList<Animation>();
 		for(String animationName: template.animations){
 			animations.add(animationMap.get(animationName));
 		}
 		
		data.defaultAbilities = new ArrayList<Ability>(template.abilities);
		
		data.controller = template.controller;
		
 		entityDataMap.put(name, data);
 	}
 
 	private void loadAnimation(String path) {
 		AnimationData data = json.fromJson(AnimationData.class,
 				Gdx.files.internal(path));
 
 		Texture sheet = manager.get(data.path, Texture.class);
 
 		// break sheet into individual frames
 		TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth()
 				/ data.cols, sheet.getHeight() / data.rows);
 
 		// pack into 1D array
 		TextureRegion[] frames = new TextureRegion[data.cols * data.rows];
 		int index = 0;
 		for (int i = 0; i < data.rows; i++) {
 			for (int j = 0; j < data.cols; j++) {
 				frames[index++] = tmp[i][j];
 			}
 		}
 
 		animationMap.put(data.name, new Animation(data.frameDuration, frames));
 	}
 
 	private class AssetList {
 		public ArrayList<String> sounds; // path
 		public ArrayList<String> textures; // path
 		public ArrayList<String> animations; // path
 		public Map<String, String> entities; // name, path
 	}
 	
 	private class EntityTemplate{
 		public int maxHealth;
 		public float jumpHeight;
 		public float moveSpeed;
 		public Controller controller;
		public ArrayList<Ability> abilities;
 		public ArrayList<String> animations;
 	}
 }
