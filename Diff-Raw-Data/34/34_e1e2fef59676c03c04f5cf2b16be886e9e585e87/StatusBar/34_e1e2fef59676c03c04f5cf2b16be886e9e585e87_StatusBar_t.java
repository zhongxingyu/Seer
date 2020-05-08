 package com.phaerical.graveblade;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.phaerical.graveblade.entities.Hero;
 
 public class StatusBar extends Table
 {
 	private Hero hero;
 	private BitmapFont font;
 	private FreeTypeFontGenerator generator;
 	
 	private TextButton levelBar;
 	private Slider healthBar;
 	private Slider manaBar;
 	private Slider expBar;
 	
 	public StatusBar (Hero hero)
 	{
 		this.hero = hero;
 		
 		generator = new FreeTypeFontGenerator (Gdx.files.internal ("fonts/pixel_font.ttf"));
 		font = generator.generateFont (16);
 		
 		TextureAtlas atlas = new TextureAtlas (Gdx.files.internal ("skins/ui-skin.atlas"));
 		
 		Skin skin = new Skin (Gdx.files.internal ("skins/ui-skin.json"));
 		skin.addRegions (atlas);
 		
 		skin.getDrawable ("gray-bar").setMinHeight (45);
 		skin.getDrawable ("red-bar").setMinHeight (45);
 		skin.getDrawable ("blue-bar").setMinHeight (45);
 		skin.getDrawable ("yellow-bar").setMinHeight (45);
 		
 		setBackground (skin.getTiledDrawable ("status-bar"));
 		setSize (1024, 60);
 		left ();
 		padLeft (8);
 		
 		levelBar = new TextButton ("LEVEL", skin);
 		levelBar.setDisabled (true);
 		
 		healthBar = new Slider (0, 100, 0.1f, false, skin, "hp-bar");
 		healthBar.setTouchable (Touchable.disabled);
 		
 		manaBar = new Slider (0, 100, 0.1f, false, skin, "mp-bar");
 		manaBar.setTouchable (Touchable.disabled);
 		manaBar.setValue (100);
 		
 		expBar = new Slider (0, 100, 0.1f, false, skin, "exp-bar");
 		expBar.setTouchable (Touchable.disabled);
 		
 		ImageButton btnStats = new ImageButton (skin, "person");
 		ImageButton btnEquipment = new ImageButton (skin, "sword");
 		ImageButton btnSkills = new ImageButton (skin, "magic");
 		ImageButton btnSettings = new ImageButton (skin, "gear");
 		
 		add (levelBar).width(100).height(45).spaceRight (4);
 		add (healthBar).width(250).spaceRight (4);
 		add (manaBar).width(200).spaceRight (4);
 		add (expBar).width(250).spaceRight (4);
 		add (btnStats).width(45).height(45).spaceRight (4);
 		add (btnEquipment).width(45).height(45).spaceRight (4);
 		add (btnSkills).width(45).height(45).spaceRight (4);
 		add (btnSettings).width(45).height(45).spaceRight (4);
 	}
 	
 	@Override
 	public void act (float delta)
 	{
 		levelBar.setText ("LEVEL " + hero.getLevel ());
		healthBar.setValue ((float) hero.getHealth () / hero.getMaxHealth () * 100);
 		expBar.setValue ((float) hero.getExp() / hero.getMaxExp() * 100);
 	}
 	
 	@Override
 	public void draw (SpriteBatch batch, float parentAlpha)
 	{
 		super.draw(batch, parentAlpha);
         
         font.setColor (Color.WHITE);
         
         font.draw (batch, "HP", healthBar.getX()+20, 33);
         font.drawWrapped (batch, hero.getHealth() + "/" + hero.getMaxHealth(), healthBar.getX(), 33, healthBar.getWidth() - 20, HAlignment.RIGHT);
         
         font.draw (batch, "MP", manaBar.getX()+20, 33);
         font.drawWrapped (batch, "100/100", manaBar.getX(), 33, manaBar.getWidth() - 20, HAlignment.RIGHT);
         
         font.draw (batch, "XP", expBar.getX()+20, 33);
         font.drawWrapped (batch, Math.round (((double) hero.getExp () / hero.getMaxExp () * 100) * 100) / 100.00 + "%",
         		expBar.getX(), 33, expBar.getWidth() - 20, HAlignment.RIGHT);
 	}
 }
