 package net.fourbytes.shadow;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 import net.fourbytes.shadow.Input.Key;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.TextureData;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
 import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.utils.IntMap.Entry;
 
 public class Camera implements Input.KeyListener {
 	
 	public Background bg;
 	public OrthographicCamera cam;
 	public BitmapFont fpsFont = Fonts.light_normal;
 	
 	public Camera() {
 		this.cam = new OrthographicCamera(Shadow.vieww, -Shadow.viewh);
 		this.cam.position.set(0, 0, 0);
 		this.cam.update();
 		
 		//Input.debug1.listeners.add(this);
 		//Input.debug2.listeners.add(this);
 	}
 	
 	@Override
 	public void keyDown(Key key) {/*
 		if (key == Input.debug1) {
 			debug = !debug;
 		}
 		if (key == Input.debug2) {
 			if (level) {
 				Level llevel = Shadow.level;
 				if (llevel != null) {
 					llevel.hasvoid = !llevel.hasvoid;
 				}
 			}
 		}*/
 	}
 	@Override
 	public void keyUp(Key key) {
 	}
 	
 	public void resize() {
 		float zoom = cam.zoom;
 		Vector3 pos = cam.position;
 		cam.viewportWidth = Shadow.vieww;
 		cam.viewportHeight = -Shadow.viewh;
 		cam.position.set(pos.x, pos.y, pos.z);
 		//cam.zoom = 2f;
 		//cam.zoom = -zoom; //Dunno why but the '-' speeds stuff up.
 		//oh wait, resize glitch :/
 		cam.update();
 	}
 	
 	protected Player player;
 	
 	public boolean debug = false;
 	public boolean level = true;
 	public boolean firsttick = true;
 	
 	public Rectangle camrec = new Rectangle(0, 0, 0, 0);
 	
 	public void render() {
 		if (Shadow.level == null) {
 			cam.position.set(0, 0, 0);
 			cam.zoom = 2f;
 			cam.update();
 			
 			camrec.set(cam.position.x, cam.position.y, cam.viewportWidth*cam.zoom, -cam.viewportHeight*cam.zoom);
 			camrec.x -= camrec.width/2;
 			camrec.y -= camrec.height/2;
 			
 			Shadow.spriteBatch.setProjectionMatrix(cam.combined);
 			Shadow.spriteBatch.begin();
 			if (bg == null) {
 				bg = Background.getDefault();
 			}
 			Shadow.spriteBatch.disableBlending();
 			bg.render();
 			Shadow.spriteBatch.enableBlending();
 			Image logo = Images.getImage("logo", false);
 			logo.setScale(Shadow.vieww/Shadow.dispw * cam.zoom, -Shadow.viewh/Shadow.disph * cam.zoom);
 			logo.setPosition(0f - (logo.getScaleX()*logo.getWidth())/2f, 0f - (logo.getScaleY()*logo.getHeight())/2f);
 			logo.draw(Shadow.spriteBatch, 1f);
 			Shadow.spriteBatch.end();
 			
 			if (Shadow.loadstate < 2) {
 				Shadow.shapeRenderer.setProjectionMatrix(cam.combined);
 				Shadow.shapeRenderer.begin(ShapeType.Line);
 				Shadow.shapeRenderer.setColor(1, 1, 1, 1);
 				Shadow.shapeRenderer.rect(-Shadow.vieww/2, Shadow.viewh/2+2, Shadow.vieww, 1);
 				Shadow.shapeRenderer.end();
 				Shadow.shapeRenderer.setProjectionMatrix(cam.combined);
 				Shadow.shapeRenderer.begin(ShapeType.Filled);
 				Shadow.shapeRenderer.setColor(1, 1, 1, 1);
 				Shadow.shapeRenderer.rect(-Shadow.vieww/2, Shadow.viewh/2+2, Shadow.vieww*Shadow.loadtick/Shadow.loadticks[0][Shadow.loadticks[0].length-1], 1);
 				Shadow.shapeRenderer.end();
 			}
 			
 			return;
 		}
 		if (!(Shadow.level instanceof MenuLevel)) {
 			player = Shadow.level.player;
 		}
 		if (player == null) {
 			Shadow.level.fillLayer(0);
 			player = new Player(new Vector2(0, 0), Shadow.level.layers.get(0));
 			firsttick = true;
 		}
 		float goalx = player.pos.x + player.rec.width/2f;
 		float goaly = player.pos.y + player.rec.height/2f;
 		if (firsttick) {
 			cam.zoom = 1f;
 			cam.position.x = goalx;
 			cam.position.y = goaly;
 			firsttick = false;
 		}
 		/*cam.zoom = cam.zoom;
 		cam.zoom += (1f+(Math.abs((player.movement.x/2f)+(player.movement.y/5f)))-cam.zoom)/5f;
 		cam.zoom = cam.zoom;*/
 		//cam.zoom = 1f;
 		cam.position.x+=(goalx-cam.position.x)/15f;
 		cam.position.y+=(goaly-cam.position.y)/20f;
 		cam.update();
 		
 		camrec.set(cam.position.x, cam.position.y, cam.viewportWidth*cam.zoom, -cam.viewportHeight*cam.zoom);
 		camrec.x -= camrec.width/2;
 		camrec.y -= camrec.height/2;
 		
 		Shadow.spriteBatch.setProjectionMatrix(cam.combined);
 		Shadow.spriteBatch.begin();
 		
 		Shadow.shapeRenderer.setProjectionMatrix(cam.combined);
 		Shadow.shapeRenderer.begin(ShapeType.Line);
 		
 		if (bg == null) {
 			bg = Background.getDefault();
 		}
 		bg.render();
 		
 		renderLevel(Shadow.level);
 		
 		if (fpsFont == null) {
 			fpsFont = Fonts.light_normal;
 		}
 		if (fpsFont != null) {
 			//fpsFont.setScale(Shadow.vieww/Shadow.dispw * cam.zoom, -Shadow.viewh/Shadow.disph * cam.zoom);
 			//fpsFont.draw(Shadow.spriteBatch, "FPS: "+Shadow.fps, cam.position.x - Shadow.vieww/2 * cam.zoom, cam.position.y - Shadow.viewh/2 * cam.zoom);
 			
 			fpsFont.setScale(Shadow.vieww/Shadow.dispw, -Shadow.viewh/Shadow.disph);
 			
 			String fps = "FPS: "+Shadow.fps;
 			TextBounds tb = fpsFont.getBounds(fps);
 			
 			fpsFont.draw(Shadow.spriteBatch, fps, cam.position.x + Shadow.vieww/2 - tb.width, cam.position.y - Shadow.viewh/2);
 		}
 		
 		if (debug) {
 			Rectangle rect = player.rec;
 			float x1 = player.pos.x;
 			float y1 = player.pos.y;
 			Shadow.shapeRenderer.setColor(0, 1, 0, 1);
 			Shadow.shapeRenderer.rect(x1, y1, rect.width, rect.height);
 			
 		}
 		
 		Shadow.shapeRenderer.end();
 		Shadow.spriteBatch.end();
 	}
 	
 	public void renderLevel(Level level) {
		//for (Layer ll : level.layers.values()) {
		Layer ll = level.mainLayer;
 			renderLayer(ll);
		//}
 		if (this.level) {
 			if (level.hasvoid) {
 				Image levoid = Images.getImage("void", false);
 				objrec.set(camrec.x, level.tiledh - 2, 1024, 1);
 				levoid.setScaleY(-1f);
 				//i.setPosition(pos.x * Shadow.dispw/Shadow.vieww, pos.y * Shadow.disph/Shadow.viewh);
 				levoid.setPosition(objrec.x, objrec.y + objrec.height*2);
 				levoid.setSize(objrec.width, objrec.height + objrec.height);
 				levoid.draw(Shadow.spriteBatch, 1f);
 				
 				float fy = level.tiledh;
 				if (camrec.y > fy) {
 					fy = camrec.y;
 				}
 				objrec.set(camrec.x, fy, 128, 128);
 				Image lewhite = Images.getImage("white", false);
 				lewhite.setColor(0f, 0f, 0f, 1f);
 				lewhite.setPosition(objrec.x, objrec.y);
 				lewhite.setSize(objrec.width, objrec.height + objrec.height);
 				lewhite.draw(Shadow.spriteBatch, 1f);
 			}
 			
 			for (Cursor c : level.cursors) {
 				c.preRender();
 				c.render();
 			}
 			
 			if (level.c != null && !Input.isAndroid) {
 				level.c.preRender();
 				level.c.render();
 			}
 			
 			level.renderImpl();
 		}
 	}
 	
 	protected static Rectangle objrec = new Rectangle();
 	protected static Color origc = new Color();
 	protected static Image white;
 	
 	public void renderLayer(Layer l) {
 		if (l == null) {
 			return;
 		}
 		for (Block block : l.blocks) {
 			if (block == null) continue;
 			objrec.set(block.pos.x + block.renderoffs.x, block.pos.y + block.renderoffs.y, block.rec.width + block.renderoffs.width, block.rec.height + block.renderoffs.height);
 			if (camrec.overlaps(objrec) && level) {
 				block.preRender();
 				Image img = block.tmpimg;
 				origc.set(img.getColor());
 				img.setColor(0f, 0f, 0f, origc.a*0.5f);
 				//img.setColor(0f, 0f, 0f, 0.5f);
 				img.setPosition(img.getX()+0.125f, img.getY()+0.125f);
 				img.draw(Shadow.spriteBatch, 1f);
 				img.setColor(origc);
 			}
 		}
 		for (Entity entity : l.entities) {
 			if (entity == null) continue;
 			objrec.set(entity.pos.x + entity.renderoffs.x, entity.pos.y + entity.renderoffs.y, entity.rec.width + entity.renderoffs.width, entity.rec.height + entity.renderoffs.height);
 			if (camrec.overlaps(objrec) && level) {
 				entity.preRender();
 				Image img = entity.tmpimg;
 				origc.set(img.getColor());
 				img.setColor(0f, 0f, 0f, origc.a*0.5f);
 				//img.setColor(0f, 0f, 0f, 0.5f);
 				img.setPosition(img.getX()+0.125f, img.getY()+0.125f);
 				img.draw(Shadow.spriteBatch, 1f);
 				img.setColor(origc);
 			}
 		}
 		
 		for (Block block : l.blocks) {
 			if (block == null) continue;
 			if (block.rendertop) continue;
 			objrec.set(block.pos.x + block.renderoffs.x, block.pos.y + block.renderoffs.y, block.rec.width + block.renderoffs.width, block.rec.height + block.renderoffs.height);
 			if (camrec.overlaps(objrec) && level) {
 				block.preRender();
 				if (!block.blending) {
 					Shadow.spriteBatch.disableBlending();
 				}
 				block.render();
 				if (!block.blending) {
 					Shadow.spriteBatch.enableBlending();
 				}
 				if (block.highlighted > 0f) {
 					if (white == null) {
 						white = new Image(Images.getTexture("white"));
 					}
 					white.setColor(1f, 1f, 1f, block.highlighted/25f);
 					white.setPosition(objrec.x, objrec.y);
 					white.setSize(1f, 1f);
 					white.setScale(objrec.width, objrec.height);
 					white.draw(Shadow.spriteBatch, 1f);
 				}
 				if (debug) {
 					Shadow.shapeRenderer.setColor(1f, 1f, 0f, 1f);
 					Shadow.shapeRenderer.rect(objrec.x, objrec.y, objrec.width, objrec.height);
 				}
 			}
 		}
 		for (Entity entity : l.entities) {
 			if (entity == null) continue;
 			objrec.set(entity.pos.x + entity.renderoffs.x, entity.pos.y + entity.renderoffs.y, entity.rec.width + entity.renderoffs.width, entity.rec.height + entity.renderoffs.height);
 			if (camrec.overlaps(objrec) && level) {
 				entity.preRender();
 				if (!entity.blending) {
 					Shadow.spriteBatch.disableBlending();
 				}
 				entity.render();
 				if (!entity.blending) {
 					Shadow.spriteBatch.enableBlending();
 				}
 				if (entity.highlighted > 0f) {
 					if (white == null) {
 						white = new Image(Images.getTexture("white"));
 					}
 					white.setColor(1f, 1f, 1f, entity.highlighted/25f);
 					white.setPosition(objrec.x, objrec.y);
 					white.setSize(1f, 1f);
 					white.setScale(objrec.width, objrec.height);
 					white.draw(Shadow.spriteBatch, 1f);
 				}
 				if (debug) {
 					Shadow.shapeRenderer.setColor(1f, 1f, 0f, 1f);
 					Shadow.shapeRenderer.rect(objrec.x, objrec.y, objrec.width, objrec.height);
 				}
 			}
 		}
 		for (Block block : l.blocks) {
 			if (block == null) continue;
 			if (!block.rendertop) continue;
 			objrec.set(block.pos.x + block.renderoffs.x, block.pos.y + block.renderoffs.y, block.rec.width + block.renderoffs.width, block.rec.height + block.renderoffs.height);
 			if (camrec.overlaps(objrec) && level) {
 				block.preRender();
 				if (!block.blending) {
 					Shadow.spriteBatch.disableBlending();
 				}
 				block.render();
 				if (!block.blending) {
 					Shadow.spriteBatch.enableBlending();
 				}
 			}
 			if (block.highlighted > 0f) {
 				if (white == null) {
 					white = new Image(Images.getTexture("white"));
 				}
 				white.setColor(1f, 1f, 1f, block.highlighted/25f);
 				white.setPosition(objrec.x, objrec.y);
 				white.setSize(1f, 1f);
 				white.setScale(objrec.width, objrec.height);
 				white.draw(Shadow.spriteBatch, 1f);
 			}
 			if (debug) {
 				Shadow.shapeRenderer.setColor(1, 0, 0, 1);
 				Shadow.shapeRenderer.rect(objrec.x, objrec.y, objrec.width, objrec.height);
 			}
 		}
 	}
 	
 }
 
