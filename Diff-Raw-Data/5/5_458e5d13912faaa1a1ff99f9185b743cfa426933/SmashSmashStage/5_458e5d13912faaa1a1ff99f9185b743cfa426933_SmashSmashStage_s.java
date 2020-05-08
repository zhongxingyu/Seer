 package com.nullsys.smashsmash.screen;
 
 import java.util.ArrayList;
 
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.equations.Linear;
 import aurelienribon.tweenengine.equations.Quad;
 import aurelienribon.tweenengine.equations.Sine;
 
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.noobs2d.tweenengine.utils.DynamicAnimation;
 import com.noobs2d.tweenengine.utils.DynamicCallback.RemoveFromCollectionOnEnd;
 import com.noobs2d.tweenengine.utils.DynamicScreen;
 import com.noobs2d.tweenengine.utils.DynamicSprite;
 import com.noobs2d.tweenengine.utils.DynamicText;
 import com.nullsys.smashsmash.Art;
 import com.nullsys.smashsmash.Coin;
 import com.nullsys.smashsmash.Fonts;
 import com.nullsys.smashsmash.GoldBar;
 import com.nullsys.smashsmash.Session;
 import com.nullsys.smashsmash.Settings;
 import com.nullsys.smashsmash.Sounds;
 import com.nullsys.smashsmash.User;
 import com.nullsys.smashsmash.alien.Alien;
 import com.nullsys.smashsmash.alien.Alien.AlienState;
 import com.nullsys.smashsmash.alien.Bomb;
 import com.nullsys.smashsmash.alien.Diabolic;
 import com.nullsys.smashsmash.alien.Fluff;
 import com.nullsys.smashsmash.alien.Golem;
 import com.nullsys.smashsmash.alien.HammerTimeJelly;
 import com.nullsys.smashsmash.alien.InvulnerabilityJelly;
 import com.nullsys.smashsmash.alien.Jelly;
 import com.nullsys.smashsmash.alien.Ogre;
 import com.nullsys.smashsmash.alien.ScoreFrenzyJelly;
 import com.nullsys.smashsmash.alien.Sorcerer;
 import com.nullsys.smashsmash.alien.Tortoise;
 import com.nullsys.smashsmash.bonuseffect.BonusEffect;
 import com.nullsys.smashsmash.hammer.HammerEffect;
 import com.nullsys.smashsmash.hammer.HammerEffectPool;
 
 /**
  * @author MrUseL3tter
  */
 public class SmashSmashStage extends DynamicScreen implements SmashSmashStageCallback {
 
     /** maximum duration before the combo expires. */
     public static final int COMBO_MAX_DURATION = 3;
     /** seconds before being able to smash again after being puked on */
     public static final int RECOVERY_DISABILITY_DURATION = 10;
 
     public ArrayList<HammerEffect> hammerEffects = new ArrayList<HammerEffect>();
     public ArrayList<DynamicAnimation> coinsAndGoldBars = new ArrayList<DynamicAnimation>();
     public ArrayList<DynamicSprite> pukes = new ArrayList<DynamicSprite>();
     public ArrayList<Alien> aliens = new ArrayList<Alien>();
 
     public DynamicSprite bonusEffectBlackFill;
     public DynamicSprite bonusEffectPinwheel;
 
     public boolean[] pointers = new boolean[4];
 
     protected UserInterface ui;
     protected Session session;
 
     private boolean showUI = true;
     private boolean allowSpawn = true;
     private boolean paused = false;
 
     /**
      * becomes RECOVERY_DISABILITY_DURATION and continuously decremented when an alien attacks the
      * player successfully. player can only smash when recoverDelay is below 1.
      */
     private float recoveryDelay = 0;
     protected float sorcererSpawnDelay = 0;
     protected int spawnDelay = 0;
     protected int spawnRate = 0;
     protected int streaks = 0;
 
     private static final float COIN_DURATION = 7f;
 
     public SmashSmashStage(Game game) {
 	super(game, Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT);
 	session = new Session();
 	initAliens();
 	initHUD();
 
 	bonusEffectBlackFill = new DynamicSprite(new TextureRegion(Art.blackFill), Settings.SCREEN_WIDTH / 2, Settings.SCREEN_HEIGHT / 2);
 	bonusEffectBlackFill.scale.set(15f, 15f);
 	bonusEffectBlackFill.setColor(1f, 1f, 1f, .5f);
 	bonusEffectPinwheel = new DynamicSprite(new TextureRegion(Art.pinwheel), Settings.SCREEN_WIDTH / 2, Settings.SCREEN_HEIGHT / 2);
 	bonusEffectPinwheel.scale.set(3f, 3f);
 	bonusEffectPinwheel.interpolateRotation(360, Linear.INOUT, 3000, false).repeat(Tween.INFINITY, 0).start(bonusEffectPinwheel.tweenManager);
 
 	Gdx.input.setCatchBackKey(true);
 	Gdx.input.setCatchMenuKey(true);
 
 	User.init();
 	ui.showReadyPrompt();
     }
 
     public int getComboMultiplier() {
 	int multiplier = 1;
 	if (session.combosCurrent > 4 && session.combosCurrent < 25)
 	    multiplier = 2;
 	else if (session.combosCurrent > 24 && session.combosCurrent < 40)
 	    multiplier = 3;
 	else if (session.combosCurrent > 39 && session.combosCurrent < 60)
 	    multiplier = 4;
 	else if (session.combosCurrent > 59 && session.combosCurrent < 100)
 	    multiplier = 5;
 	else if (session.combosCurrent > 99 && session.combosCurrent < 255)
 	    multiplier = 7;
 	else if (session.combosCurrent > 254)
 	    multiplier = 10;
 	return multiplier;
     }
 
     public int getSpawnRate() {
 	return spawnRate;
     }
 
     @Override
     public boolean isAttackAllowed() {
 	return recoveryDelay <= RECOVERY_DISABILITY_DURATION / 4f;
     }
 
     public boolean isPaused() {
 	return paused;
     }
 
     public boolean isSpawnAllowed() {
 	return allowSpawn;
     }
 
     public boolean isUIVisible() {
 	return showUI;
     }
 
     @Override
     public boolean keyUp(int keycode) {
 	if (keycode == Keys.BACK && Gdx.app.getType() == ApplicationType.Android)
 	    game.setScreen(new PauseScreen(game, this));
 	else if (keycode == Keys.BACKSPACE)
 	    game.setScreen(new PauseScreen(game, this));
 	return false;
     }
 
     @Override
     public void onAlienAttack(Alien alien) {
 	session.combosCurrent = 0;
 	camera.shake();
 	if (!(alien instanceof Bomb)) {
 	    recoveryDelay = RECOVERY_DISABILITY_DURATION;
 	    // add 3-5 puke splashes into the screen
 	    int count = (int) (3 + 1 * Math.random() * 3);
 	    for (int i = 0; i < count; i++) {
 		float x = (float) (300 + Math.random() * 680);
 		float y = (float) (200 + Math.random() * 400);
 		float targetScale = (float) (0.75f + Math.random() * 1.5f);
 		DynamicSprite puke = new DynamicSprite(Art.pukes.findRegion("PUKE_GREEN"), x, y);
 		puke.setScale(0f, 0f);
 		puke.setRotation((float) (360 * Math.random()));
 		puke.interpolateScaleXY(1f * targetScale, 1f * targetScale, 250, true).delay(i * 100);
 		puke.interpolateAlpha(0f, RECOVERY_DISABILITY_DURATION * 1000, true).delay(i * 100);
 		pukes.add(puke);
 	    }
 	}
     }
 
     @Override
     public void onAlienEscaped(Alien alien) {
 	// TODO Auto-generated method stub
 	session.escapedAliens++;
     }
 
     @Override
     public void onAlienSmashed(Alien alien) {
 	addScore(alien);
 	session.smashedAliens++;
 	// Add coin as per percentage
 	int random = (int) (Math.random() * 1000);
 	if (random <= 4)
 	    addGoldBar(alien.position.x, alien.position.y);
 	else if (random > 4 && random <= 25)
 	    addCoin(alien.position.x, alien.position.y);
     }
 
     @Override
     public void onBonusEffectTrigger(int... bonusEffects) {
 	for (int i = 0; i < bonusEffects.length; i++)
 	    switch (bonusEffects[i]) {
 		case BonusEffect.HAMMER_TIME:
 		    addBonusEffect(BonusEffect.HAMMER_TIME);
 		    break;
 		case BonusEffect.INVULNERABILITY:
 		    addBonusEffect(BonusEffect.INVULNERABILITY);
 		    break;
 		case BonusEffect.SCORE_FRENZY:
 		    addBonusEffect(BonusEffect.SCORE_FRENZY);
 		    break;
 		default:
 		    assert false;
 		    break;
 	    }
     }
 
     public void onSmashMissed(float x, float y) {
 	session.smashMissed++;
 	if (session.combosCurrent > 1)
 	    ui.showMissPrompt(x, y);
 	ui.showComboPrompt(session.combosCurrent);
 	session.combosMax = session.combosCurrent > session.combosMax ? session.combosCurrent : session.combosMax;
 	session.combosCurrent = 0;
     }
 
     @Override
     public void onTouchDown(float x, float y, int pointer, int button) {
 	Vector2 position = new Vector2(x, y);
 	position.x *= (float) Settings.SCREEN_WIDTH / Gdx.graphics.getWidth();
 	position.y = (Gdx.graphics.getHeight() * camera.zoom - position.y) * Settings.SCREEN_HEIGHT / Gdx.graphics.getHeight();
 
 	boolean touchedACoin = inputToCoinsAndGoldBars(position.x, position.y, pointer);
 	boolean touchedAnAlien = isAttackAllowed() && !touchedACoin ? inputToAliens(position.x, position.y, pointer) : false; // We will only test collisions with the aliens if
 	// there are no collisions with any coin
 
 	if (isAttackAllowed() && !touchedACoin) { // Hammer effects will only be added if a coin is not tapped
 	    session.smashLanded++;
 	    if (User.hasEffect(BonusEffect.HAMMER_TIME))
 		camera.shake();
 	    addHammerEffect(position.x, position.y);
 	    if (Settings.soundEnabled)
 		Sounds.hammerIceFlakes.play();
 	}
 
 	if (!touchedAnAlien && !touchedACoin && !User.hasEffect(BonusEffect.HAMMER_TIME))
 	    onSmashMissed(position.x, position.y);
 
 	pointers[pointer] = touchedAnAlien;
     }
 
     @Override
     public void onTouchDrag(float x, float y, float pointer) {
 	super.onTouchMove(x, y);
 	recoveryDelay -= 0.025f;
 	for (int i = 0; i < pukes.size(); i++)
 	    pukes.get(i).tweenSpeed += .015f;
     }
 
     @Override
     public void pause() {
 	paused = true;
 	for (int i = 0; i < coinsAndGoldBars.size(); i++)
 	    coinsAndGoldBars.get(i).pause();
 	for (int i = 0; i < pukes.size(); i++)
 	    pukes.get(i).pause();
 	for (int i = 0; i < aliens.size(); i++)
 	    aliens.get(i).pause();
     }
 
     @Override
     public void render(float delta) {
 	getCamera().update();
 	spriteBatch.setProjectionMatrix(camera.projection);
 
 	delta = paused ? 0 : delta;
 
 	sorcererSpawnDelay -= delta;
 	session.stageSecondsElapsed += delta;
 
 	spriteBatch.begin();
 	renderStage(spriteBatch, delta);
 	renderAliens(spriteBatch, delta);
 	renderCoinsAndGoldBars(spriteBatch, delta);
 	renderHammerEffects(spriteBatch, delta);
 	renderPukes(spriteBatch, delta);
 	renderStageEffects(spriteBatch, delta);
 	if (showUI) {
 	    ui.render(spriteBatch);
 	    ui.update(delta);
 	}
 	spriteBatch.end();
 	spriteBatch.setColor(1f, 1f, 1f, 1f);
 
 	// Check if 3 sec. has passed since the last successful hit. If so, the combos are cancelled.
 	if (session.stageSecondsElapsed - session.combosLastDelta >= COMBO_MAX_DURATION && session.combosCurrent > 0) {
 	    ui.showComboPrompt(session.combosCurrent);
 	    session.combosMax = session.combosCurrent > session.combosMax ? session.combosCurrent : session.combosMax;
 	    session.combosCurrent = 0;
 	}
 
 	// update streaks
 	streaks = 0;
 	for (int i = 0; i < pointers.length; i++)
 	    if (pointers[i])
 		streaks++;
 	if (streaks >= 2) {
 	    ui.showStreakPrompt(streaks);
 	    for (int i = 0; i < pointers.length; i++)
 		pointers[i] = false;
 	}
 
 	try {
 	    if (session.stageSecondsElapsed > 0 && Integer.parseInt(("" + session.stageSecondsElapsed).split(".")[1]) % 2 == 0) // .2 seconds has passed
 		for (int i = 0; i < pointers.length; i++)
 		    pointers[i] = false;
 	} catch (ArrayIndexOutOfBoundsException e) {
 	    //	    System.out.println("[SmashSmashStage#render(float): ArrayOutOfBoundsException");
 	}
 	//	System.out.println("[SmashSmashStage#render(float)] blackFill.color.a: " + bonusEffectBlackFill.getColor().a);
     }
 
     @Override
     public void resume() {
 	paused = false;
 	for (int i = 0; i < coinsAndGoldBars.size(); i++)
 	    coinsAndGoldBars.get(i).resume();
 	for (int i = 0; i < pukes.size(); i++)
 	    pukes.get(i).resume();
 	for (int i = 0; i < aliens.size(); i++)
 	    aliens.get(i).resume();
     }
 
     public void setAliensHostile(boolean hostile) {
 	for (int i = 0; i < aliens.size(); i++)
 	    aliens.get(i).setHostile(hostile);
     }
 
     public void setAllowSpawn(boolean allowSpawn) {
 	this.allowSpawn = allowSpawn;
     }
 
     public void setUIVisible(boolean showUI) {
 	this.showUI = showUI;
     }
 
     protected void addBonusEffect(int bonusEffect) {
 	switch (bonusEffect) {
 	    case BonusEffect.HAMMER_TIME:
 		break;
 	    case BonusEffect.INVULNERABILITY:
 		bonusEffectBlackFill.color.a = 0f;
 		bonusEffectBlackFill.interpolateAlpha(.35f, Linear.INOUT, 500, true);
 		bonusEffectBlackFill.interpolateAlpha(0f, Linear.INOUT, 500, true).delay(9500);
 		break;
 	    case BonusEffect.SCORE_FRENZY:
 		bonusEffectPinwheel.color.a = 0f;
 		bonusEffectPinwheel.interpolateAlpha(1f, Linear.INOUT, 500, true);
 		bonusEffectPinwheel.interpolateAlpha(0f, Linear.INOUT, 500, true).delay(9500);
 		break;
 	    default:
 		assert false;
 		break;
 	}
     }
 
     protected void addCoin(float x, float y) {
 	Coin coin = new Coin(Art.coins, 0, 0, 64, 64, 8, 8, .125f);
 	coin.position.set(x, y);
 	coin.interpolateXY(x, y + 100, Quad.OUT, 200, true);
 	coin.interpolateXY(x, y, Quad.IN, 200, true).delay(200);
 	coinsAndGoldBars.add(coin);
     }
 
     protected void addGoldBar(float x, float y) {
 	GoldBar g = new GoldBar(1f, Art.goldbar.findRegion("GOLDBAR"));
 	g.setPosition(x, y);
 	g.interpolateXY(x, y + 100, Quad.OUT, 200, true);
 	g.interpolateXY(x, y, Quad.IN, 200, true).delay(200);
 	coinsAndGoldBars.add(g);
     }
 
     protected void addHammerEffect(float x, float y) {
 	//	Vector2 position = new Vector2(x, y);
 	//	float duration = User.hammer.getEffect().getEmitters().get(0).getDuration().getLowMax() / 1000;//User.hammer.getEffect().getEmitters().get(0).duration / 2;
 	//	HammerEffect hammerEffect = new HammerEffect(User.hammer.getEffect(), position, duration, 0f);
 	HammerEffect he = HammerEffectPool.obtain();
 	he.setPosition(x, y);
 	hammerEffects.add(he);
 	//	System.out.println(duration);
 	//	Array<ParticleEmitter> a = User.hammer.getEffect().getEmitters();
 	//	for (ParticleEmitter p : a)
 	//	    System.out.println(p.getDuration().getLowMax());
     }
 
     protected void addScore(Alien alien) {
 	int multiplier = getComboMultiplier();
 	multiplier *= User.hasEffect(BonusEffect.SCORE_FRENZY) ? 3 : 1;
 	session.score += alien.getScore() * multiplier * (streaks >= 1 ? streaks : 1);
     }
 
     protected int getVisibleAliens() {
 	int visibles = 0;
 	for (int i = 0; i < aliens.size(); i++)
 	    if (aliens.get(i).isVisible())
 		visibles++;
 	return visibles;
     }
 
     protected void initAliens() {
 	aliens.add(new Bomb(this));
 	aliens.add(new Bomb(this));
 	aliens.add(new Bomb(this));
 	aliens.add(new Diabolic(this));
 	aliens.add(new Diabolic(this));
 	aliens.add(new Diabolic(this));
 	aliens.add(new Fluff(this));
 	aliens.add(new Fluff(this));
 	aliens.add(new Fluff(this));
 	aliens.add(new Golem(this));
 	aliens.add(new Golem(this));
 	aliens.add(new Golem(this));
 	aliens.add(new Jelly(this));
 	aliens.add(new Jelly(this));
 	aliens.add(new Jelly(this));
 	aliens.add(new Ogre(this));
 	aliens.add(new Ogre(this));
 	aliens.add(new Ogre(this));
 	aliens.add(new Tortoise(this));
 	aliens.add(new Tortoise(this));
 	aliens.add(new Tortoise(this));
 	aliens.add(new Sorcerer(this));
 	aliens.add(new InvulnerabilityJelly(this));
 	aliens.add(new HammerTimeJelly(this));
 	aliens.add(new ScoreFrenzyJelly(this));
 	for (int i = 0; i < aliens.size(); i++)
 	    aliens.get(i).setVisible(false);
     }
 
     protected void initHUD() {
 	ui = new UserInterface(this);
     }
 
     protected boolean inputToAliens(float x, float y, int pointer) {
 	int diameter = User.hammer.getDiameter();
 	Rectangle bounds = new Rectangle(x - diameter / 2, y - diameter / 2, diameter, diameter);
 	int hitCount = 0;
 	for (int i = 0; i < aliens.size(); i++)
 	    if (aliens.get(i).isVisible() && aliens.get(i).getBounds().overlaps(bounds) && aliens.get(i).state != AlienState.SMASHED) {
 		//		onAlienSmashed(aliens.get(i));
 		hitCount++;
 		session.combosCurrent++;
 		session.combosLastDelta = session.stageSecondsElapsed;
 		ui.shakeCombos();
 		aliens.get(i).smash();
 		break;
 	    }
 	// Display a "x + 1!" message
 	if (hitCount >= 2) {
 	    DynamicText text = new DynamicText(Fonts.bdCartoonShoutx23orange, hitCount + " in 1!", HAlignment.CENTER);
 	    text.position.set(x, y - 50);
 	    text.color.a = 0f;
 	    text.interpolateXY(x, y + 50, Sine.OUT, 250, true);
 	    text.interpolateAlpha(1f, Sine.OUT, 250, true);
 	    text.interpolateAlpha(0f, Sine.OUT, 250, true).delay(1000);
 	    ui.textPool.add(text);
 	}
 	return hitCount > 0;
     }
 
     protected boolean inputToCoinsAndGoldBars(float x, float y, int pointer) {
 	Rectangle bounds = new Rectangle(x - 25, y - 25, 50, 50);
 	boolean hit = false;
 	for (int i = coinsAndGoldBars.size() - 1; i >= 0; i--)
 	    if (coinsAndGoldBars.get(i).getBounds().overlaps(bounds)) {
 		coinsAndGoldBars.get(i).tweenManager.killAll();
 		coinsAndGoldBars.get(i).interpolateXY(1280, 800, Linear.INOUT, 350, true);
 		coinsAndGoldBars.get(i).tween.setCallback(new RemoveFromCollectionOnEnd(coinsAndGoldBars, coinsAndGoldBars.get(i)));
 		if (coinsAndGoldBars.get(i) instanceof GoldBar)
 		    User.gold += 1000;
 		else
 		    User.gold += 10;
 		hit = true;
 		i = -1;
 	    }
 	return hit;
     }
 
     protected void renderAliens(SpriteBatch batch, float delta) {
 	// Sort the aliens first according to their y-coordinate. Aliens with lowest y are rendered last.
 	for (int i = 0; i < aliens.size(); i++)
 	    for (int j = i + 1; j < aliens.size(); j++)
 		if (aliens.get(j).position.y < aliens.get(i).position.y) {
 		    Alien alien = aliens.get(j);
 		    aliens.remove(j);
 		    aliens.add(aliens.get(i));
 		    aliens.remove(i);
 		    aliens.add(i, alien);
 		}
 	for (int i = aliens.size() - 1; i > -1; i--) {
 	    aliens.get(i).render(batch);
 	    aliens.get(i).update(delta);
 	}
 
 	// FIXME set to delay before the intro prompt ends
 	if (session.stageSecondsElapsed > 1) {
 	    setSpawnRate();
 	    setSpawnPositions();
 	    int visibles = getVisibleAliens();
 	    boolean overlaps = false;
 	    boolean sorcererShouldAppear = !User.hasEffect(BonusEffect.HAMMER_TIME) && !User.hasEffect(BonusEffect.INVULNERABILITY);
 	    sorcererShouldAppear = sorcererSpawnDelay <= 0 && sorcererShouldAppear && !User.hasEffect(BonusEffect.SCORE_FRENZY);
 	    for (int i = 0; visibles < spawnRate - 1 && i < aliens.size() && i < spawnRate - 1; i++) {
 		for (int j = 0; j < aliens.size(); j++)
 		    if (i != j && !aliens.get(i).isVisible() && aliens.get(i).getBounds().overlaps(aliens.get(j).getBounds())) {
 			overlaps = true;
 			j = aliens.size(); //break this loop
 		    }
 		spawnDelay = i * (int) (Math.random() * 250);
 		// we only show an alien if it doesn't collide with other ones or if it is a sorcerer 
 		// and it is allowed to be spawn and doesn't collide to others
 		if (!(aliens.get(i) instanceof Sorcerer) && !overlaps) {
 		    float volume = visibles > 0 ? 1.1f - visibles / aliens.size() : 1f;
 		    aliens.get(i).rise(spawnDelay, volume / 2);
 		} else if (aliens.get(i) instanceof Sorcerer && sorcererShouldAppear && !overlaps) {
 		    sorcererSpawnDelay = (float) (3f + Math.random() * 11f);
 		    float volume = visibles > 0 ? 1.1f - visibles / aliens.size() : 1f;
 		    aliens.get(i).rise(spawnDelay, volume / 2);
 		}
 		overlaps = false;
 	    }
 	}
     }
 
     protected void renderCoinsAndGoldBars(SpriteBatch batch, float delta) {
 	for (int i = 0; i < coinsAndGoldBars.size(); i++) {
 	    coinsAndGoldBars.get(i).render(batch);
 	    if (coinsAndGoldBars.get(i).timeElapsed > COIN_DURATION)
 		coinsAndGoldBars.remove(i);
 	    else
 		coinsAndGoldBars.get(i).update(delta);
 	}
     }
 
     protected void renderHammerEffects(SpriteBatch batch, float delta) {
 	for (int i = 0; i < hammerEffects.size(); i++) {
 	    hammerEffects.get(i).render(batch);
 	    if (hammerEffects.get(i).isComplete())//elapsed >= hammerEffects.get(i).duration)
 		hammerEffects.get(i).free(hammerEffects, i);
 	    //		hammerEffects.remove(hammerEffects.get(i));
 	    else
 		hammerEffects.get(i).update(delta);
 	}
 	//	System.out.println("[SmashSmashStage#renderHammerEffects(SpriteBatch,float)] HammerEffectPool.size(): " + hammerEffects.size());
     }
 
     protected void renderPukes(SpriteBatch batch, float delta) {
 	for (int i = 0; i < pukes.size(); i++) {
 	    pukes.get(i).render(batch);
 	    pukes.get(i).update(delta);
 	    if (pukes.get(i).tweenManager.getRunningTweensCount() == 0)
 		pukes.remove(i);
 	}
 	recoveryDelay -= delta;
 	//	System.out.println("[SurvivalStageScreen#renderPukes(SpriteBatch,float)] recoveryDelay: " + recoveryDelay);
 	//	System.out.println("[SurvivalStageScreen#renderPukes(SpriteBatch,float)] Pukes count: " + pukes.size());
     }
 
     protected void renderStage(SpriteBatch spriteBatch, float delta) {
 	spriteBatch.draw(Art.lawnBackground1, 0, -(1024 - 800));
 	spriteBatch.draw(Art.lawnBackground2, 1280 - 256, -(1024 - 800));
     }
 
     protected void renderStageEffects(SpriteBatch batch, float delta) {
 	if (User.hasEffect(BonusEffect.SCORE_FRENZY))
 	    bonusEffectPinwheel.render(batch);
 	if (User.hasEffect(BonusEffect.INVULNERABILITY))
 	    bonusEffectBlackFill.render(batch);
 	bonusEffectBlackFill.update(delta);
 	bonusEffectPinwheel.update(delta);
 	for (int itemEffectIndex = 0; itemEffectIndex < User.bonusEffects.size(); itemEffectIndex++)
 	    User.bonusEffects.get(itemEffectIndex).update(delta);
     }
 
     protected void setSpawnPositions() {
 	for (int i = 0; i < aliens.size(); i++) {
 	    float targetWidth = aliens.get(i).waitingState.getLargestAreaDisplay().getKeyFrame().getRegionWidth();
 	    float targetHeight = aliens.get(i).waitingState.getLargestAreaDisplay().getKeyFrame().getRegionHeight();
 	    aliens.get(i).getBounds().width = targetWidth;
 	    aliens.get(i).getBounds().height = targetHeight;
 	    if (!aliens.get(i).isVisible()) {
 		float randomX = (float) (targetWidth / 2 + Math.random() * (Settings.SCREEN_WIDTH - targetWidth * 2));
 		float randomY = (float) (Math.random() * (Settings.SCREEN_HEIGHT - targetHeight));
 		aliens.get(i).position.set(randomX, randomY);
 		aliens.get(i).getBounds().x = randomX - targetWidth / 2;
 		aliens.get(i).getBounds().y = randomY;
 	    }
 	}
     }
 
     protected void setSpawnRate() {
 	boolean hasRampage = User.hasEffect(BonusEffect.INVULNERABILITY) && User.hasEffect(BonusEffect.HAMMER_TIME) && User.hasEffect(BonusEffect.SCORE_FRENZY);
 	if (allowSpawn && hasRampage)
 	    spawnRate = aliens.size();
 	else if (!allowSpawn)
 	    spawnRate = 0;
 	else if (session.combosCurrent > 4 && session.combosCurrent < 25)
 	    spawnRate = 4;
 	else if (session.combosCurrent > 24 && session.combosCurrent < 40)
 	    spawnRate = 5;
 	else if (session.combosCurrent > 39 && session.combosCurrent < 60)
 	    spawnRate = 6;
 	else if (session.combosCurrent > 59 && session.combosCurrent < 100)
 	    spawnRate = 8;
 	else if (session.combosCurrent > 99 && session.combosCurrent < 255)
 	    spawnRate = 10;
 	else if (session.combosCurrent > 254)
 	    spawnRate = aliens.size();
 	else
 	    spawnRate = 4;
 	//	alienAppearanceRate = aliens.size();
     }
 }
