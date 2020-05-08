 package config;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 
 public enum Biome {
 	FOREST(new Color(76, 178, 76), "assets/sounds/Song1.wav"),
 	LAKE(new Color(185, 69, 201), "assets/sounds/Field08.wav"),
	DESERT(new Color(200, 200, 200), "assets/sounds/Field12.wav"),
 	CANYON(new Color(200, 200, 200), "assets/sounds/Field35.wav"),
	HELL(new Color(200, 200, 200), "assets/sounds/Field09.wav");
 	
 	private Color color;
 	private Music music;
 	
 	private Biome(Color color) {
 		this(color, "assets/sounds/Song1.wav");
 	}
 	
 	private Biome(Color color, String music) {
 		this.color = color;
 		try {
 			this.music = new Music(music);
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public Color getColor() {
 		return color;
 	}
 	
 	public Music getMusic() {
 		return music;
 	}
 
 }
