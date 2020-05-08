 package fr.romainpedra.VolcanoEscape;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 
 import fr.romainpedra.VolcanoEscape.Assets;
 
 public class Assets {
 	
 	public Texture perso;
 	public Texture wall, wallLeft, wallRight;
 	public Texture rock;
 	public Texture lave1, lave2, lave3, lave4, lave5, lave6;
 	public Texture laveOverlaid;
 	public Texture background;
 	public Music music1;
 	
 	public void load(){
 		perso = new Texture(Gdx.files.internal("data/enemi.png"));
 		perso.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		lave1 = new Texture(Gdx.files.internal("data/lava1.png"));
 		lave1.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		lave2 = new Texture(Gdx.files.internal("data/lava2.png"));
 		lave2.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		lave3 = new Texture(Gdx.files.internal("data/lava3.png"));
 		lave3.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		lave4 = new Texture(Gdx.files.internal("data/lava4.png"));
 		lave4.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		lave5 = new Texture(Gdx.files.internal("data/lava5.png"));
 		lave5.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		lave6 = new Texture(Gdx.files.internal("data/lava6.png"));
 		lave6.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		wall = new Texture(Gdx.files.internal("data/wall.png"));
 		wall.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
		wallLeft = new Texture(Gdx.files.internal("data/LeftWall.png"));
 		wallLeft.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
		wallRight = new Texture(Gdx.files.internal("data/RightWall.png"));
 		wallRight.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		rock = new Texture(Gdx.files.internal("data/rock.png"));
 		rock.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		music1 = Gdx.audio.newMusic(Gdx.files.internal("data/Sounds/music1.mp3"));
 		
 		laveOverlaid = new Texture(Gdx.files.internal("data/overlay.png"));
 		laveOverlaid.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 	}
 	
 	public void dispose(){
 		
 		if(perso != null){
 			perso.dispose();
 			perso = null;
 		}
 		if(wallLeft != null){
 			wallLeft.dispose();
 			wallLeft = null;
 		}
 		if(wallRight != null){
 			wallRight.dispose();
 			wallRight = null;
 		}
 		if(rock != null){
 			rock.dispose();
 			rock = null;
 		}
 		if(lave1 != null){
 			lave1.dispose();
 			lave1 = null;
 		}
 		if(lave2 != null){
 			lave2.dispose();
 			lave2 = null;
 		}
 		if(lave3 != null){
 			lave3.dispose();
 			lave3 = null;
 		}
 		if(lave4 != null){
 			lave4.dispose();
 			lave4 = null;
 		}
 		if(lave5 != null){
 			lave5.dispose();
 			lave5 = null;
 		}
 		if(lave6 != null){
 			lave6.dispose();
 			lave6 = null;
 		}
 		if(laveOverlaid != null){
 			laveOverlaid.dispose();
 			laveOverlaid = null;
 		}
 		if(background != null){
 			background.dispose();
 			background = null;
 		}
 		if(music1 != null){
 			music1.dispose();
 			music1 = null;
 		}
 	}
 	
 	private static Assets sInstance;
 	
 	//Asset.get().perso
 	public static Assets get(){
 		if(sInstance == null){
 			sInstance = new Assets();
 		}
 		return sInstance;
 	}
 
 }
