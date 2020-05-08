 package com.blastedstudios.crittercaptors.ui.battle;
 
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider;
 import com.badlogic.gdx.scenes.scene2d.ui.Window;
 import com.blastedstudios.crittercaptors.CritterCaptors;
 import com.blastedstudios.crittercaptors.creature.Creature;
 
 public class CreatureInfoWindow extends Window {
 	private final Creature creature;
 	private final Label levelLabel, hpRatio;
 	private final Slider hpSlider;
 	
 	public CreatureInfoWindow(final CritterCaptors game, final Skin skin, 
 			final Creature creature, final int x, final int y) {
 		super(skin);
 		this.touchable = false;
 		this.creature = creature;
 		levelLabel = new Label(creature.getLevel()+"", skin);
 		hpRatio = new Label(creature.getHPCurrent() + "/" + creature.getHPMax(), skin);
 		hpSlider = new Slider(0, creature.getHPMax(), 1, skin);
 		hpSlider.touchable = false;
 		add(new Label(creature.getName(), skin));
 		add(new Label("Lvl: ",skin));
 		add(levelLabel);
 		row();
 		add(new Label("HP: ",skin));
 		add(hpSlider);
 		add(hpRatio);
 		pack();
 		this.x = x;
 		this.y = y;
 	}
 	
 	public void update(){
 		levelLabel.setText(creature.getLevel()+"");
 		hpRatio.setText(creature.getHPCurrent() + "/" + creature.getHPMax());
 		hpSlider.setValue(creature.getHPCurrent());
		hpSlider.setRange(0, creature.getHPMax());
		hpSlider.invalidate();
 	}
 }
