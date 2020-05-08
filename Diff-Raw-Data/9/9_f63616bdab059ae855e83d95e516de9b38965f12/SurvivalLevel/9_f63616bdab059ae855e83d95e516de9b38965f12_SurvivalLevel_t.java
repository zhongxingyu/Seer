 package pl.spaceshooters.level;
 
 import spaceshooters.aurora.level.LevelType;
 
 public class SurvivalLevel extends NormalLevel {
 	
 	private int time;
 	
 	@Override
 	public void update(int delta) {
 		super.update(delta);
 		time += delta;
 		if (time % 1000 == 0)
 			player.setScore(time / 1000);
 	}
 	
 	@Override
 	public void render() {
		super.render();
 		// FIXME: Draw stuff to the screen...
 		// g.drawString(Translator.getTranslator().getTranslated("player.time") + ": " + time / 1000, 0, 0);
 	}
 	
 	@Override
 	public LevelType getLevelType() {
 		return LevelType.SURVIVAL;
 	}
 }
