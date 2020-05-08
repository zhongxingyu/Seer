 package com.pix.mind.world;
 
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureWrap;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.MapZoom;
 import com.pix.mind.actors.MenuInGame;
 import com.pix.mind.box2d.bodies.PixGuy;
 
 public class PixMindWorldRenderer {
 
 	private PixMindScene2DInitialization scene2D;
 	private PixGuy pixGuy;
 	private Box2DWorldContactListener contactListener;
 	private MapZoom mapZoom;
 	private OrthographicCamera camera;
 	private Image pixGuySkin;
 	private World world;
 	private MenuInGame menuInGame;
 	private Box2DDebugRenderer debugRenderer;
 	private PixMindBox2DInitialization box2D;
 	public PixMindWorldRenderer(PixMindScene2DInitialization scene2D,
 			PixMindBox2DInitialization box2D, PixMindGuiInitialization gui) {
 		this.scene2D = scene2D;
 		this.pixGuy = box2D.getPixGuy().getPixGuy();
 		this.pixGuySkin = box2D.getPixGuy().getPixGuySkin();
 		this.contactListener = box2D.getContactListener();
 		this.mapZoom = gui.getMapZoom();
 		this.menuInGame = gui.getMenuInGame();
 		this.world = box2D.getWorld();
 		this.box2D = box2D;
 		// set up camera for the debugRenderer
 		camera = new OrthographicCamera(PixMindGame.w
 				* PixMindGame.WORLD_TO_BOX, PixMindGame.h
 				* PixMindGame.WORLD_TO_BOX);
 		camera.translate(PixMindGame.w / 2 * PixMindGame.WORLD_TO_BOX,
 				PixMindGame.h / 2 * PixMindGame.WORLD_TO_BOX);
 		debugRenderer = new Box2DDebugRenderer();
 		// make this to bring pixguy to the front if you make 12 elements
 		// platforms and activator this number must be 13
 		// put a high vaulue if you are not sure.
 		box2D.getPixGuy().getPixGuySkin().setZIndex(200);
 	
 	}
 
 	public void render(float delta) {
 		// debugRenderer.render(world, camera.combined);
 		
 		scene2D.getStage().draw();
 		scene2D.getStageGui().draw();
 
	/*	if (pixGuy.body.getLinearVelocity().y > 0) {
 			pixGuy.body.getFixtureList().get(0).setSensor(true);
 		} else {
 			if (!contactListener.isColliding())
 				pixGuy.body.getFixtureList().get(0).setSensor(false);
 		}*/
 
 	
 		if (!mapZoom.isMapActive() && !menuInGame.isActive()) {
 			if (contactListener.getLastPlatformHeight() > pixGuy.getPosY()) {
 
 				contactListener.setLastPlatformHeight(pixGuy.getPosY());
 				scene2D.getStage().getCamera().position.x = pixGuy.getPosX();
 				scene2D.getStage().getCamera().position.y = contactListener
 						.getLastPlatformHeight();
 				camera.position.x = pixGuy.getPosX() * PixMindGame.WORLD_TO_BOX;
 				camera.position.y = contactListener.getLastPlatformHeight()
 						* PixMindGame.WORLD_TO_BOX;
 				camera.update();
 
 			} else {
 				if (contactListener.getLastPlatformHeight() > contactListener
 						.getAnteriorHeight()) {
 					contactListener.setAnteriorHeight(contactListener
 							.getAnteriorHeight() + 3);
 					if (contactListener.getAnteriorHeight() >= contactListener
 							.getLastPlatformHeight()) {
 						contactListener.setAnteriorHeight(contactListener
 								.getLastPlatformHeight());
 					}
 				}
 				scene2D.getStage().getCamera().position.x = pixGuy.getPosX();
 				scene2D.getStage().getCamera().position.y = contactListener
 						.getAnteriorHeight();
 				camera.position.x = pixGuy.getPosX() * PixMindGame.WORLD_TO_BOX;
 				camera.position.y = contactListener.getAnteriorHeight()
 						* PixMindGame.WORLD_TO_BOX;
 				camera.update();
 			}
 			world.step(delta, 6, 2);
 			pixGuy.setActualPosition();
 
 			// eyes of pixguy
 			if (pixGuy.body.getLinearVelocity().x > 0f) {
 
 				/*
 				 * if(pixGuy.body.getLinearVelocity().y<2f &&
 				 * pixGuy.body.getLinearVelocity().y>-2f )
 				 * pixGuySkin.setDrawable
 				 * (PixMindGame.getSkin().getDrawable("pixguy6")); else
 				 */if (pixGuy.body.getLinearVelocity().y < 0f)
 					pixGuySkin.setDrawable(PixMindGame.getSkin().getDrawable(
 							"pixguy9"));
 				else if (pixGuy.body.getLinearVelocity().y > 0f)
 					pixGuySkin.setDrawable(PixMindGame.getSkin().getDrawable(
 							"pixguy3"));
 
 			} else if (pixGuy.body.getLinearVelocity().x < 0f) {
 				/*
 				 * if(pixGuy.body.getLinearVelocity().y<2f &&
 				 * pixGuy.body.getLinearVelocity().y>-2f )
 				 * pixGuySkin.setDrawable
 				 * (PixMindGame.getSkin().getDrawable("pixguy4")); else
 				 */if (pixGuy.body.getLinearVelocity().y < 0f)
 					pixGuySkin.setDrawable(PixMindGame.getSkin().getDrawable(
 							"pixguy7"));
 				else if (pixGuy.body.getLinearVelocity().y > 0f)
 					pixGuySkin.setDrawable(PixMindGame.getSkin().getDrawable(
 							"pixguy1"));
 
 			} else if (pixGuy.body.getLinearVelocity().x == 0) {
 				/*
 				 * if(pixGuy.body.getLinearVelocity().y<2f &&
 				 * pixGuy.body.getLinearVelocity().y>-2f )
 				 * pixGuySkin.setDrawable
 				 * (PixMindGame.getSkin().getDrawable("pixguy5")); else
 				 */if (pixGuy.body.getLinearVelocity().y < 0f)
 					pixGuySkin.setDrawable(PixMindGame.getSkin().getDrawable(
 							"pixguy8"));
 				else if (pixGuy.body.getLinearVelocity().y > 0f)
 					pixGuySkin.setDrawable(PixMindGame.getSkin().getDrawable(
 							"pixguy2"));
 
 			}
 			// death of pixguy
 			if (pixGuy.body.getPosition().y < -1) {
 				menuInGame.showLose();
 			}
 
 		} else {
 
 		}
 		if(!menuInGame.active)
 		scene2D.getStage().act();
 		if(menuInGame.active){
 			scene2D.getStageGui().act();
 		}
 	}
 
 }
