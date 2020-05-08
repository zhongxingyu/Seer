 package com.dfgames.lastplanet.screens;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.g2d.ParticleEffect;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Array;
 import com.dfgames.lastplanet.Assets;
 import com.dfgames.lastplanet.Constants;
 import com.dfgames.lastplanet.bonus.*;
 import com.dfgames.lastplanet.model.*;
 
 import java.util.Iterator;
 
 /**
  * Author: Ivan Melnikov
  * Date: 05.11.12 20:30
  */
 public class GameScreen extends AbstractScreen {
     private static final float BACKGROUND_SPEED = 20.0f;
     private static final float SPACESHIP_SPEED = 200.0f;
     private static final float BONUS_SPEED = -100.0f;
 
     private float timeToNextBonus;
 
     private Rectangle screenRect;
 
     private Sprite background;
 
     private Spaceship player;
     private Array<PlayerBullet> playerBullets;
 
     private Array<EnemySpaceship> enemies;
     private Array<EnemyBullet> enemyBullets;
 
     private Array<ParticleEffect> particleEffects;
 
     private Array<Bonus> bonuses;
 
     public GameScreen(Game game) {
         super(game);
 
         screenRect = new Rectangle(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
 
         background = Assets.atlas.createSprite("background");
         background.setSize(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
         background.setPosition(0, 0);
 
         player = new PlayerSpaceship(Constants.SCREEN_WIDTH / 2 - 32, 8);
         playerBullets = new Array<PlayerBullet>();
 
         enemies = new Array<EnemySpaceship>();
 
         for (int i = 0; i <= 6; i++) {
             for (int j = 0; j < 3; j++) {
                 EnemySpaceship enemySpaceship = new EnemySpaceship(Constants.SCREEN_WIDTH / 6 * i + 40, Constants.SCREEN_HEIGHT - 70 - j * 70);
                 enemies.add(enemySpaceship);
             }
         }
 
         enemyBullets = new Array<EnemyBullet>();
 
         particleEffects = new Array<ParticleEffect>();
 
         bonuses = new Array<Bonus>();
         timeToNextBonus = MathUtils.random(10f, 30f);
     }
 
     @Override
     protected void update() {
         processInput();
 
         background.translateY(-BACKGROUND_SPEED * Gdx.graphics.getDeltaTime());
 
         if (Math.abs(background.getY()) >= Constants.SCREEN_HEIGHT) {
             background.setY(0.0f);
         }
 
         timeToNextBonus -= Gdx.graphics.getDeltaTime();
 
         if (timeToNextBonus <= 0) {
             timeToNextBonus = MathUtils.random(10f, 30f);
             bonuses.add(createRandomBonus());
         }
 
         removeCompletedEffects();
         removeObjectsOutsideBounds();
         checkCollisions();
         updateAI();
         updateBonuses();
         checkBonusCollision();
 
         if (System.nanoTime() - player.getLastShootTime() > 400000000.0f) {
             player.setLastShootTime(System.nanoTime());
             playerBullets.add(new PlayerBullet(player.getX() + 18, player.getY() + 50));
             playerBullets.add(new PlayerBullet(player.getX() + 18 + 25, player.getY() + 50));
         }
     }
 
     private void updateBonuses() {
         for(Bonus bonus:bonuses) {
             bonus.translateY(BONUS_SPEED * Gdx.graphics.getDeltaTime());
         }
     }
 
     private void checkBonusCollision() {
         Iterator<Bonus> bonusIterator = bonuses.iterator();
 
         while (bonusIterator.hasNext()) {
             Bonus bonus = bonusIterator.next();
 
             if(player.getBoundingRectangle().overlaps(bonus.getBoundingRectangle()) || !screenRect.contains(bonus.getX(), bonus.getY())) {
                 bonusIterator.remove();
             }
         }
     }
 
     private Bonus createRandomBonus() {
         int value = MathUtils.random(1, 6);
         switch (value) {
             case 1:
                 return new HealthBonus(MathUtils.random(40, 760), 479);
             case 2:
                 return new MoneyBonus(MathUtils.random(40, 760), 479);
             case 3:
                 return new UpLevelWeaponBonus(MathUtils.random(40, 760), 479);
             case 4:
                 return new ShieldBonus(MathUtils.random(40, 760), 479);
             case 5:
                 return new SpeedDownBonus(MathUtils.random(40, 760), 479);
             case 6:
                 return new UpLevelWeaponBonus(MathUtils.random(40, 760), 479);
         }
         return new HealthBonus(MathUtils.random(40, 760), 479);
     }
 
     private void updateAI() {
         for (EnemySpaceship spaceship : enemies) {
             if (spaceship.getDestination().dst(spaceship.getX(), spaceship.getY()) > 5) {
                 float x = spaceship.getDestination().x - spaceship.getX();
                 float y = spaceship.getDestination().y - spaceship.getY();
                 float angleRad = MathUtils.atan2(y, x);
 
                 if (angleRad < 0) {
                     angleRad += MathUtils.PI * 2;
                 }
 
                 float deltaX = Gdx.graphics.getDeltaTime() * spaceship.getSpeed() * MathUtils.cos(angleRad);
                 float deltaY = Gdx.graphics.getDeltaTime() * spaceship.getSpeed() * MathUtils.sin(angleRad);
 
                 spaceship.translate(deltaX, deltaY);
             } else {
                 spaceship.nextWayPoint();
 
                 Vector2 normalizedDirection = new Vector2(spaceship.getX(), spaceship.getY())
                         .sub(spaceship.getDestination())
                         .nor();
 
                 if (Math.abs(normalizedDirection.x) > 0.5) {
                     if (normalizedDirection.x > 0.5) {
                         spaceship.setRegion(Assets.atlas.findRegion("enemies/enemy_1_left"));
                     } else {
                         spaceship.setRegion(Assets.atlas.findRegion("enemies/enemy_1_right"));
                     }
                 } else {
                     spaceship.setRegion(Assets.atlas.findRegion("enemies/enemy_1"));
                 }
             }
         }
     }
 
     private void removeCompletedEffects() {
         Iterator<ParticleEffect> iterator = particleEffects.iterator();
 
         while (iterator.hasNext()) {
             ParticleEffect effect = iterator.next();
 
             if (effect.isComplete()) {
                 iterator.remove();
             }
         }
     }
 
     private void removeObjectsOutsideBounds() {
         Iterator<PlayerBullet> playerBulletIterator = playerBullets.iterator();
 
         while (playerBulletIterator.hasNext()) {
             Bullet bullet = playerBulletIterator.next();
 
             if (screenRect.contains(bullet.getX(), bullet.getY())) {
                 bullet.translateY(bullet.getSpeed() * Gdx.graphics.getDeltaTime());
             } else {
                 playerBulletIterator.remove();
             }
         }
 
         Iterator<EnemyBullet> enemyBulletIterator = enemyBullets.iterator();
 
         while (enemyBulletIterator.hasNext()) {
             Bullet bullet = enemyBulletIterator.next();
 
             if (screenRect.contains(bullet.getX(), bullet.getY())) {
                 bullet.translateY(bullet.getSpeed() * Gdx.graphics.getDeltaTime());
             } else {
                 enemyBulletIterator.remove();
             }
         }
     }
 
     private void checkCollisions() {
         Iterator<EnemySpaceship> spaceships = enemies.iterator();
 
         while (spaceships.hasNext()) {
             EnemySpaceship spaceship = spaceships.next();
 
             Iterator<PlayerBullet> bullets = playerBullets.iterator();
 
             while (bullets.hasNext()) {
                 PlayerBullet bullet = bullets.next();
 
                 if (spaceship.getBoundingRectangle().overlaps(bullet.getBoundingRectangle())) {
                     spaceship.setHealth(spaceship.getHealth() - bullet.getDamage());
                     bullets.remove();
                 }
             }
 
             if (spaceship.getHealth() <= 0) {
                 ParticleEffect explosion = new ParticleEffect();
                 explosion.load(Gdx.files.internal("particles/explosion.p"), Gdx.files.internal("particles"));
                 explosion.setPosition(spaceship.getX() + 25, spaceship.getY() + 25);
                 explosion.start();
                 particleEffects.add(explosion);
                 spaceships.remove();
             }
         }
     }
 
     @Override
     protected void draw(float delta) {
         spriteBatch.setProjectionMatrix(camera.combined);
         spriteBatch.begin();
 
         background.draw(spriteBatch);
        int emptySpaceHeight = (int)Math.ceil(Math.abs(background.getY()));
        spriteBatch.draw(background.getTexture(), 0, Constants.SCREEN_HEIGHT - emptySpaceHeight, 0, Constants.SCREEN_HEIGHT - emptySpaceHeight, Constants.SCREEN_WIDTH, emptySpaceHeight);
 
         player.draw(spriteBatch);
 
         for (Bullet bullet : playerBullets) {
             bullet.draw(spriteBatch);
         }
 
         for (Spaceship enemy : enemies) {
             enemy.draw(spriteBatch);
         }
 
         for (Bullet bullet : enemyBullets) {
             bullet.draw(spriteBatch);
         }
 
         for (ParticleEffect effect : particleEffects) {
            //effect.draw(spriteBatch, delta);
         }
 
         for(Bonus bonus : bonuses) {
             bonus.draw(spriteBatch);
         }
 
         spriteBatch.end();
     }
 
     private void processInput() {
         if (Gdx.input.isTouched()) {
             Vector3 touchPosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.0f);
             camera.unproject(touchPosition);
 
             if (touchPosition.x < Constants.SCREEN_WIDTH / 2) {
                 player.translateX(-SPACESHIP_SPEED * Gdx.graphics.getDeltaTime());
             }
 
             if (touchPosition.x > Constants.SCREEN_WIDTH / 2) {
                 player.translateX(SPACESHIP_SPEED * Gdx.graphics.getDeltaTime());
             }
 
             if (player.getX() < 0.0f) {
                 player.setX(0);
             }
 
             if (player.getX() > Constants.SCREEN_WIDTH - player.getWidth()) {
                 player.setX(Constants.SCREEN_WIDTH - player.getWidth());
             }
         }
 
         if(Gdx.input.isKeyPressed(Input.Keys.BACK) || Gdx.input.isKeyPressed(Input.Keys.BACKSPACE)) {
             game.setScreen(new DifficultyScreen(game));
         }
     }
 }
