 package com.blastedstudios.crittercaptors.ui.battle;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
 import com.badlogic.gdx.scenes.scene2d.ui.Window;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.blastedstudios.crittercaptors.CritterCaptors;
 
 public class CaptureFailWindow extends Window {
 	public CaptureFailWindow(final CritterCaptors game, final Skin skin, 
 			final BattleScreen battleScreen, int roll, int targetRoll) {
 		super("Failure", skin);
 		final Button okButton = new TextButton("Ok", skin.getStyle(TextButtonStyle.class), "ok");
 		okButton.setClickListener(new ClickListener() {
 			@Override public void click(Actor actor, float arg1, float arg2) {
 				battleScreen.fight(null, 0);
 				actor.getStage().removeActor(actor.parent);
 			}
 		});
		add(new TextField("Catch failed!\nMust roll >" + roll + "\nYou rolled " + targetRoll, skin));
 		row();
 		add(okButton);
 		pack();
 		x = Gdx.graphics.getWidth() / 2 - width / 2;
 		y = Gdx.graphics.getHeight() / 2 - height / 2;
 	}
 }
