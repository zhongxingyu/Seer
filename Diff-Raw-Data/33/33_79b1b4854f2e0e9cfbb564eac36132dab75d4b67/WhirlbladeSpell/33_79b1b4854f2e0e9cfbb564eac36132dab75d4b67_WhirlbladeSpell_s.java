 package com.soc.game.attacks.spells;
 
 import com.artemis.Entity;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.soc.core.Constants;
 import com.soc.core.EntityFactory;
 import com.soc.core.SoC;
 import com.soc.game.components.Position;
 import com.soc.game.components.State;
 import com.soc.game.components.Stats;
 import com.soc.utils.GraphicsLoader;
 
 public class WhirlbladeSpell extends Spell{
 
 	
 	public WhirlbladeSpell(){
 		this.icon = GraphicsLoader.load("whirlblade-icon.png");
 		this.tooltip = "WHIRLBLADE\n\n50 mana\n\nSpin around yourself, hitting and pushing foes on your way.\nDamage increases with strength and agility";
 		this.cast = 0.0f;
 		this.blocking = Constants.Spells.SPIN_DURATION;
 		this.state = State.SPINNING;
 		this.mana = 50;
 	}
 
 	@Override
 	public void create(Entity source, String group, Position pos, Stats stats) {
		Entity e = EntityFactory.createWhirlblade(SoC.game.positionmapper.get(source), SoC.game.boundsmapper.get(source),(int) (stats.agility*0.7f + stats.strength*0.7));
 	    SoC.game.groupmanager.add(e, group);
 		SoC.game.groupmanager.add(e, Constants.Groups.MAP_BOUND);
 		SoC.game.levelmanager.setLevel(e, Constants.Groups.LEVEL+pos.z);
 	   	e.addToWorld();
 	}
 }
