 package com.dfgames.lastplanet.screens.game;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputAdapter;
 import com.badlogic.gdx.graphics.g2d.ParticleEffect;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Array;
 import com.dfgames.lastplanet.Assets;
 import com.dfgames.lastplanet.Constants;
 import com.dfgames.lastplanet.model.MovingStarsBackground;
 import com.dfgames.lastplanet.model.bonuses.Bonus;
 import com.dfgames.lastplanet.model.bullets.Bullet;
 import com.dfgames.lastplanet.model.bullets.EnemyBullet;
 import com.dfgames.lastplanet.model.bullets.PlayerBullet;
 import com.dfgames.lastplanet.model.spaceships.EnemySpaceship;
 import com.dfgames.lastplanet.model.spaceships.PlayerSpaceship;
 import com.dfgames.lastplanet.model.spaceships.Spaceship;
 import com.dfgames.lastplanet.screens.AbstractScreen;
 import com.dfgames.lastplanet.screens.DifficultyScreen;
 
 import java.util.Iterator;
 
 /**
  * Author: Ivan Melnikov
  * Date: 05.11.12 20:30
  */
 public class GameScreen extends AbstractScreen {
     private static final float SPACESHIP_SPEED = 200.0f;
     private static final float BONUS_SPEED = 100.0f;
 
     private Rectangle screenRect;
     private MovingStarsBackground background;
 
     private Spaceship player;
     private Array<Bullet> playerBullets;
 
     private Array<EnemySpaceship> enemies;
     private Array<EnemyBullet> enemyBullets;
 
     private Array<Bonus> bonuses;
     private float timeToNextBonus;
 
     private Array<ParticleEffect> particleEffects;
 
     public GameScreen(final Game game) {
         super(game);
 
         screenRect = new Rectangle(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
         background = new MovingStarsBackground();
 
         player = new PlayerSpaceship(Constants.SCREEN_WIDTH / 2 - 32, 8);
         playerBullets = new Array<Bullet>();
 
         enemies = new Array<EnemySpaceship>();
         enemyBullets = new Array<EnemyBullet>();
         spawnNextWave();
 
         bonuses = new Array<Bonus>();
         timeToNextBonus = MathUtils.random(10f, 30f);
 
         particleEffects = new Array<ParticleEffect>();
 
         initNavigation();
     }
 
     private void initNavigation() {
         Gdx.input.setInputProcessor(new InputAdapter() {
             @Override
             public boolean keyDown(int keyCode) {
                 if (keyCode == Input.Keys.BACK || keyCode == Input.Keys.BACKSPACE) {
                     openScreen(new DifficultyScreen(game));
                 }
 
                 return false;
             }
         });
     }
 
     private void spawnNextWave() {
         for (int i = 0; i <= 6; i++) {
             for (int j = 0; j < 3; j++) {
                 EnemySpaceship enemySpaceship = new EnemySpaceship(Constants.SCREEN_WIDTH / 6 * i + 40, Constants.SCREEN_HEIGHT - 70 - j * 70);
                 enemies.add(enemySpaceship);
             }
         }
     }
 
     @Override
     protected void update() {
         processInput();
 
         background.update();
 
         removeObjectsOutsideBounds();
         updateAI();
         updateShoots();
         checkBulletCollisions();
 
         updateBonuses();
         updateBonusTimer();
         checkCollisionWithBonuses();
 
         removeCompletedEffects();
     }
 
     private void removeObjectsOutsideBounds() {
         Iterator<Bullet> playerBulletIterator = playerBullets.iterator();
 
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
 
     private void updateShoots() {
         if (System.nanoTime() - player.getLastShootTime() > 400000000.0f) {
             player.setLastShootTime(System.nanoTime());
             playerBullets.add(new PlayerBullet(player.getX() + 19, player.getY() + 50));
             playerBullets.add(new PlayerBullet(player.getX() + 18 + 25, player.getY() + 50));
         }
     }
 
     private void checkBulletCollisions() {
         Iterator<EnemySpaceship> iterator = enemies.iterator();
 
         while (iterator.hasNext()) {
             EnemySpaceship spaceship = iterator.next();
             Iterator<Bullet> bullets = playerBullets.iterator();
 
             while (bullets.hasNext()) {
                 Bullet bullet = bullets.next();
 
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
                 iterator.remove();
             }
         }
     }
 
     private void updateBonuses() {
         for(Bonus bonus : bonuses) {
             bonus.translateY(-BONUS_SPEED * Gdx.graphics.getDeltaTime());
         }
     }
 
     private void updateBonusTimer() {
         timeToNextBonus -= Gdx.graphics.getDeltaTime();
 
         if (timeToNextBonus <= 0) {
             timeToNextBonus = MathUtils.random(10f, 30f);
             bonuses.add(BonusFactory.randomInstance(MathUtils.random(40, 760), 479));
         }
     }
 
     private void checkCollisionWithBonuses() {
         Iterator<Bonus> bonusIterator = bonuses.iterator();
 
         while (bonusIterator.hasNext()) {
             Bonus bonus = bonusIterator.next();
 
             if(player.getBoundingRectangle().overlaps(bonus.getBoundingRectangle()) || !screenRect.contains(bonus.getX(), bonus.getY())) {
                 bonusIterator.remove();
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
 
     @Override
     protected void draw(float delta) {
         spriteBatch.setProjectionMatrix(camera.combined);
         spriteBatch.begin();
 
         background.draw(spriteBatch);
         background.translateY(Constants.SCREEN_HEIGHT);
         background.draw(spriteBatch);
         background.translateY(-Constants.SCREEN_HEIGHT);
 
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
             effect.draw(spriteBatch, delta);
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
     }
 }
