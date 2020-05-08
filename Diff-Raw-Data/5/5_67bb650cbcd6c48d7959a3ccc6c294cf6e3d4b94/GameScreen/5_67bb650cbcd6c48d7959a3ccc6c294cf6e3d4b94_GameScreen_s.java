 package com.jumpandrun;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.graphics.Pixmap.Format;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.FrameBuffer;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Array;
 import com.music.AudioEventListener;
 import com.music.BofEvent;
 import com.music.NoteJumper;
 import com.music.RhythmAudio;
 import com.music.TickEvent;
 
 
 public class GameScreen extends DefaultScreen implements InputProcessor {
 	
 	PerspectiveCamera cam;
 
 	SpriteBatch batch;
 	SpriteBatch fontBatch;
 	BitmapFont font;
 
 	public static RhythmAudio ra = new RhythmAudio();
 	
 	float startTime = 0;
 	float enemySpawnTime = MathUtils.random(0, 10);
 	float delta = 0;
 	boolean enemySpawnSwitch = false;
 	
 	//DEBUG values
 	float renderTimeBench = 0;
 	float physicTimeBench = 0;
 	float musicTimeBench = 0;
 	float startTimeBench = 0;
 	float endTimeBench = 0;
 	
 	// GLES20
 	Matrix4 model = new Matrix4().idt();
 	Matrix4 tmp = new Matrix4().idt();
 	private ShaderProgram transShader;
 	private ShaderProgram bloomShader;
 	private ShaderProgram tvShader;
 	Mesh blockModel;
 	Mesh playerModel;
 	Mesh targetModel;
 	Mesh quadModel;
 	Mesh wireCubeModel;
 	Mesh sphereModel;
 	Mesh sphereSliceModel;
 	Mesh torusModel;
 	FrameBuffer frameBuffer;
 	FrameBuffer frameBufferVert;
 	FrameBuffer frameBufferFull;
 	
 	float angleXBack = 0;
 	float angleYBack = 0;
 	float angleXFront = 0;
 	float angleYFront = 0;	
 	Vector3 tmpVector3 = new Vector3();
 	Vector2 tmpVector2 = new Vector2();
 
 	private float accumulator = 0;
 	
 	private float bloomFactor = 0;
 	private float disortFactor = 0;
 	private float highlightTimer = 0;
 	private int highlightCnt = 1000;
 
 	Array<NoteJumper> noteJumpers = new Array<NoteJumper>();
 	
 	private int songcounter = 0;
 	
 	private AudioEventListener audioListener = new AudioEventListener() {
 
 		@Override
 		public void onEvent(TickEvent te) {
 			
 			if(te.isFullNote()) {
 				GameInstance.getInstance().activateJumpBlocks();
 			}
 			
 			if(te.getCustomNote((long)(te.getFullTicks()*4/Math.pow(2,songcounter))) == 0) {
 				//enemySpawnSwitch = ch6;
 				GameInstance.getInstance().addEnemy();
 				
 				int random = MathUtils.random(0,GameInstance.getInstance().blankBlocks.size-1);
 				GameInstance.getInstance().addPowerUp(GameInstance.getInstance().blankBlocks.get(random).position.x,GameInstance.getInstance().blankBlocks.get(random).position.y);
 				
 				if(highlightCnt>500) {
 					highlightCnt = 0;
 				}
 			}
 			
 			
 			if(te.getTick()%3072 == 0) {
 				float ppos = GameInstance.getInstance().player.position.y;
				songcounter = (int) ((ppos + 197)/50);
 				songcounter %= 6;
				System.out.println("songcounter" + songcounter + " tick: " + te.getTick());
 				
 				ra.gotoTick(songcounter*3072);
 				if(songcounter == 0)
 					Resources.getInstance().song01.play();
 				else if(songcounter == 1)
 					Resources.getInstance().song02.play();
 				else if(songcounter == 2)
 					Resources.getInstance().song03.play();
 				else if(songcounter == 3)
 					Resources.getInstance().song04.play();
 				else if(songcounter == 4)
 					Resources.getInstance().song05.play();
 				else if(songcounter == 5)
 					Resources.getInstance().song06.play();
 				
 			}
 		}
 
 		@Override
 		public void onMidiEvent(Array<BofEvent> events, long tick) {
 			for (BofEvent me : events) {
 				if (me.type == BofEvent.NOTE_ON) {
 
 					noteJumpers.add(new NoteJumper(me, tick));
 					
 				}
 			}
 		}
 		
 	};
 
 	private float animateFont = 2.0f;
 
 	private int score = 0;
 
 	private boolean bulletSplash = false;
 	private float shakeCam = 0;
 
 
 	public GameScreen(Game game) {
 		super(game);
 		
 		GameInstance.getInstance().resetGame();
 		
 		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		cam.position.set(23, -15,29f);
 		cam.direction.set(0, 0, -1);
 		cam.up.set(0, 1, 0);
 		cam.near = 1f;
 		cam.far = 1000;
 		
 		Gdx.input.setInputProcessor(this);
 		batch = new SpriteBatch();
 		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		font = new BitmapFont();
 		
 		blockModel = Resources.getInstance().blockModel;
 		playerModel = Resources.getInstance().playerModel;
 		targetModel = Resources.getInstance().targetModel;
 		quadModel = Resources.getInstance().quadModel;
 		wireCubeModel = Resources.getInstance().wireCubeModel;
 		sphereModel = Resources.getInstance().sphereModel;
 		sphereSliceModel = Resources.getInstance().sphereSliceModel;
 		torusModel = Resources.getInstance().torusModel;
 		
 		transShader = Resources.getInstance().transShader;
 		bloomShader = Resources.getInstance().bloomShader;
 		tvShader = Resources.getInstance().tvShader;
 
 		ra.loadMidi("./data/song.mid");
 		ra.registerBeatListener(audioListener);
 		ra.play();
 		//Resources.getInstance().music.play();
 		
 		initRender();
 	}
 	
 	public void initRender() {
 		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 	
 		batch = new SpriteBatch();
 		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		fontBatch = new SpriteBatch();
 		fontBatch.getProjectionMatrix().setToOrtho2D(0, 0,800, 480);
 		font = new BitmapFont();	
 		font.setScale(2.0f);
 
 		frameBuffer = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);		
 		frameBufferVert = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);
 		
 		frameBufferFull = new FrameBuffer(Format.RGB565, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		initRender();
 		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		cam.position.set(21, -15,29f);
 		cam.direction.set(0, 0, -1);
 		cam.up.set(0, 1, 0);
 		cam.near = 1f;
 		cam.far = 1000;
 	}
 
 	@Override
 	public void show() {
 	}
 
 //	@Override
 //	public void render(float deltaTime) {
 //		accumulator += deltaTime;
 //		while(accumulator > 1.0f / 60.0f) { 
 //		fixedTimeStepRender();
 //		accumulator -= 1.0f / 60.0f;
 //		if(accumulator>0) Gdx.app.log("", "framedrop " + accumulator);
 //		}
 //		
 //	}	
 	
 	public void render(float deltaTime) {		
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		
 		ra.update();
 		{	//remove obsolete NoteJumper
 			for(NoteJumper nj: noteJumpers) {
 				if(!nj.bofNote.isPlayedAt(ra.getTick())) {
 					nj.alive = false;
 ////					Gdx.app.log("", nj.bofNote.getNote() + "");
 //					if(nj.bofNote.getNote()<40) {
 //						GameInstance.getInstance().addPowerUp(nj.posA.x,nj.posA.y);
 //					}
 				}
 			}
 			//remove from array
 			boolean found;
 			do {
 				found = false;
 				for(int i = 0; i < noteJumpers.size; i++) {
 					if(noteJumpers.get(i) != null && noteJumpers.get(i).alive == false) {
 						noteJumpers.removeIndex(i);
 						found = true;
 						break;
 					}
 				}
 			}while(found);
 		}
 		
 		startTime+=deltaTime;
 		delta = deltaTime;
 		
 		enemySpawnTime -=deltaTime;
 //		if(enemySpawnTime<0) {
 //			enemySpawnTime = MathUtils.random(0, 10f);
 //			GameInstance.getInstance().addEnemy();
 //		}
 		
 		angleXBack += MathUtils.sin(startTime) * delta * 10f;
 		angleYBack += MathUtils.cos(startTime) * delta * 5f;
 
 		angleXFront += MathUtils.sin(startTime) * delta * 10f;
 		angleYFront += MathUtils.cos(startTime) * delta* 5f;
 		
 		startTimeBench = System.nanoTime();		
 		
 		cam.position.set(cam.position.x, GameInstance.getInstance().player.position.y, 29);
 		if(shakeCam>0) {
 			cam.rotate(MathUtils.sin(shakeCam)/10.f, 0, 0, 1);
 			shakeCam =  Math.max(0, shakeCam - (deltaTime*100f));
 		} else {
 			cam.up.set(0,1,0);
 		}
 		
 		cam.update();
 
 		if (Resources.getInstance().bloomOnOff) {
 			frameBuffer.begin();
 			renderBackground();
 			renderScene();
 			frameBuffer.end();
 
 			// PostProcessing
 			Gdx.gl.glDisable(GL20.GL_CULL_FACE);
 			Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
 			Gdx.gl.glDisable(GL20.GL_BLEND);
 
 			for (int i = 0; i < GameInstance.getInstance().bullets.size; ++i) {
 				Ammo bullet = GameInstance.getInstance().bullets.get(i);
 				bulletSplash  = false;
 				if(bullet instanceof Rocket) {
 					if(((Rocket) bullet).hit) {
 						disortFactor=1;
 						bulletSplash  = true;
 						if(shakeCam==0) {
 							shakeCam = 20;
 						}
 					} 
 				}
 				
 				if(bullet instanceof Mine) {
 					if(((Mine) bullet).hit) {
 						disortFactor=1;
 						bulletSplash  = true;
 						if(shakeCam==0) {
 							shakeCam = 20;
 						}
 					} 
 				}
 			}
 			if(bulletSplash) {
 				disortFactor =  Math.max(0, disortFactor - (deltaTime*5.f));
 			}
 			
 			
 			frameBuffer.getColorBufferTexture().bind(0);
 
 			bloomShader.begin();
 			bloomShader.setUniformi("sTexture", 0);
 			bloomShader.setUniformf("bloomFactor", Helper.map((MathUtils.sin(startTime * 3f) * delta * 50f) + 0.5f, 0, 1, 0.65f, 0.70f + (bloomFactor/5.f)));
 
 			frameBufferVert.begin();
 			bloomShader.setUniformf("TexelOffsetX", Resources.getInstance().m_fTexelOffset);
 			bloomShader.setUniformf("TexelOffsetY", 0.0f);
 			quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
 			frameBufferVert.end();
 
 			frameBufferVert.getColorBufferTexture().bind(0);
 
 			frameBuffer.begin();
 			bloomShader.setUniformf("TexelOffsetX", 0.0f);
 			bloomShader.setUniformf("TexelOffsetY", Resources.getInstance().m_fTexelOffset);
 			quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
 			frameBuffer.end();
 
 			bloomShader.end();
 		}
 
 
 		if(!Resources.getInstance().bloomOnOff) {
 			// render scene again
 			renderScene();
 		} else {
 
 			frameBufferFull.begin();
 			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 			renderScene();
 
 			Gdx.gl.glDisable(GL20.GL_CULL_FACE);
 			Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
 			Gdx.gl.glDisable(GL20.GL_BLEND);	
 			
 			batch.enableBlending();
 			batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
 			batch.begin();
 			batch.draw(frameBuffer.getColorBufferTexture(), 0, 0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),0,0,frameBuffer.getWidth(),frameBuffer.getHeight(),false,true);
 			batch.end();
 			frameBufferFull.end();
 			
 			frameBufferFull.getColorBufferTexture().bind(0);
 			
 			tvShader.begin();
 			tvShader.setUniformf("time", startTime);
 			tvShader.setUniformf("disort", 0.002f+ (disortFactor/400.f));
 			tvShader.setUniformi("sampler0", 0);
 			tvShader.setUniformf("resolution", Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
 			quadModel.render(tvShader,GL20.GL_TRIANGLE_FAN);
 			tvShader.end();			
 		}		
 		
 		endTimeBench = (System.nanoTime() - startTimeBench) / 1000000000.0f;
 		renderTimeBench = endTimeBench;
 		 
 		startTimeBench = System.nanoTime();
 		GameInstance.getInstance().update(deltaTime);	
 		endTimeBench = (System.nanoTime() - startTimeBench) / 1000000000.0f;
 		physicTimeBench = endTimeBench;
 		
 		
 		bloomFactor =  Math.max(0, bloomFactor - deltaTime);
 		disortFactor =  Math.max(0, disortFactor - deltaTime);
 		
 		
 		highlightTimer -= delta;
 		if(highlightTimer<0) {
 			highlightCnt++;
 			highlightTimer = 0.0001f;
 		}
 		
 		
 		fontBatch.begin();
 		if (GameInstance.getInstance().showWeaponTextYAnimate < Gdx.graphics.getHeight()+100) {
 		
 			if (GameInstance.getInstance().player.weapon instanceof MachineGun) {
 				font.draw(fontBatch, "Machinegun", 300, 250
 						+ GameInstance.getInstance().showWeaponTextYAnimate);
 			} else if (GameInstance.getInstance().player.weapon instanceof RocketLauncher) {
 				font.draw(fontBatch, "Rocket Launcher", 300, 250
 						+ GameInstance.getInstance().showWeaponTextYAnimate);
 			} else if (GameInstance.getInstance().player.weapon instanceof MinesLauncher) {
 				font.draw(fontBatch, "Mines Launcher",  300, 250
 						+ GameInstance.getInstance().showWeaponTextYAnimate);
 			}
 			GameInstance.getInstance().showWeaponTextYAnimate = Math.min(Gdx.graphics.getHeight()+100, GameInstance.getInstance().showWeaponTextYAnimate + (deltaTime*300f));
 		}
 		
 		font.setScale(animateFont);
 		if(this.score  != GameInstance.getInstance().score) {
 			animateFont = 3;
 			this.score =  GameInstance.getInstance().score;
 		}
 		animateFont = Math.max(2, animateFont - (deltaTime*10.f));
 		font.draw(fontBatch, score+"",  30, 70);
 		fontBatch.end();		
 	}	
 
 	private void renderScene() {
 
 		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
 		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
 		
 		Gdx.gl20.glEnable(GL20.GL_BLEND);
 		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
 				
 		transShader.begin();
 		transShader.setUniformMatrix("VPMatrix", cam.combined);
 		
 		//render boxes
 		for (int i =0; i<GameInstance.getInstance().blocks.size ; ++i) {
 			Block block = GameInstance.getInstance().blocks.get(i);
 			
 			if(block.id == highlightCnt) {
 				block.highlightAnimate = 0.4f;
 			}
 			
 			block.highlightAnimate =  Math.max(0, block.highlightAnimate - delta);
 			
 			if(cam.frustum.sphereInFrustum(tmpVector3.set(block.position.x, block.position.y, 0),1f)) {
 				model.idt();
 						
 				if(block instanceof JumpBlock) {
 					//TODO quick hack
 					JumpBlock jumbBlock = (JumpBlock)block;
 					jumbBlock.update();
 					
 					tmp.setToTranslation(jumbBlock.position.x, jumbBlock.position.y, 0);
 					model.mul(tmp);
 		
 					tmp.setToScaling(0.95f, 0.95f, 0.95f);
 					model.mul(tmp);
 							
 					transShader.setUniformMatrix("MMatrix", model);
 					
 					transShader.setUniformf("a_color", Resources.getInstance().jumpBlockColor[0], Resources.getInstance().jumpBlockColor[1], Resources.getInstance().jumpBlockColor[2], Resources.getInstance().jumpBlockColor[3] + jumbBlock.jumpAnim + block.highlightAnimate);
 					blockModel.render(transShader, GL20.GL_TRIANGLES);
 		
 					transShader.setUniformf("a_color",Resources.getInstance().jumpBlockEdgeColor[0], Resources.getInstance().jumpBlockEdgeColor[1],Resources.getInstance().jumpBlockEdgeColor[2], Resources.getInstance().jumpBlockEdgeColor[3] + jumbBlock.jumpAnim);
 					wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);			
 				} else if(block instanceof EnemySpawner) {	
 					
 					tmp.setToTranslation(block.position.x, block.position.y, 0);
 					model.mul(tmp);
 		
 					tmp.setToScaling(0.95f, 0.95f, 0.95f);
 					model.mul(tmp);
 							
 					transShader.setUniformMatrix("MMatrix", model);
 					
 					transShader.setUniformf("a_color", Resources.getInstance().enemySpawnerColor[0], Resources.getInstance().enemySpawnerColor[1], Resources.getInstance().enemySpawnerColor[2], Resources.getInstance().enemySpawnerColor[3] + block.highlightAnimate);
 					blockModel.render(transShader, GL20.GL_TRIANGLES);
 		
 					transShader.setUniformf("a_color",Resources.getInstance().enemySpawnerEdgeColor[0], Resources.getInstance().enemySpawnerEdgeColor[1],Resources.getInstance().enemySpawnerEdgeColor[2], Resources.getInstance().enemySpawnerEdgeColor[3]);
 					wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
 				} else {	
 					
 					tmp.setToTranslation(block.position.x, block.position.y, 0);
 					model.mul(tmp);
 		
 					tmp.setToScaling(0.95f, 0.95f, 0.95f);
 					model.mul(tmp);
 							
 					transShader.setUniformMatrix("MMatrix", model);
 					
 					transShader.setUniformf("a_color", Resources.getInstance().blockColor[0], Resources.getInstance().blockColor[1], Resources.getInstance().blockColor[2], Resources.getInstance().blockColor[3] + block.highlightAnimate);
 					blockModel.render(transShader, GL20.GL_TRIANGLES);
 		
 					transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0], Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2], Resources.getInstance().blockEdgeColor[3]);
 					wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
 				}
 			}
 		}
 		
 		// render powerUps
 		for (int i = 0; i < GameInstance.getInstance().powerUps.size; ++i) {
 			PowerUp powerUp = GameInstance.getInstance().powerUps.get(i);
 			if (cam.frustum.sphereInFrustum(tmpVector3.set(powerUp.position.x, powerUp.position.y, 0), 1f)) {
 				model.idt();
 
 				tmp.setToTranslation(powerUp.position.x, powerUp.position.y, 0);
 				model.mul(tmp);
 				
 				tmp.setToRotation(Vector3.Z, MathUtils.radiansToDegrees * powerUp.angle);
 				model.mul(tmp);
 				
 				tmp.setToScaling(0.95f, 0.95f, 0.95f);
 				model.mul(tmp);
 
 				transShader.setUniformMatrix("MMatrix", model);
 
 				transShader.setUniformf("a_color", Resources.getInstance().powerUpColor[0], Resources.getInstance().powerUpColor[1],
 						Resources.getInstance().powerUpColor[2], Math.min(powerUp.depth,Resources.getInstance().powerUpColor[3]));
 				playerModel.render(transShader, GL20.GL_TRIANGLES);
 
 				transShader.setUniformf("a_color", Resources.getInstance().powerUpEdgeColor[0], Resources.getInstance().powerUpEdgeColor[1],
 						Resources.getInstance().powerUpEdgeColor[2], Math.min(powerUp.depth,Resources.getInstance().powerUpEdgeColor[3]));
 				playerModel.render(transShader, GL20.GL_LINE_STRIP);
 			}
 		}
 		
 		// render enemies
 		for (int i = 0; i < GameInstance.getInstance().enemies.size; ++i) {
 			Enemy enemy = GameInstance.getInstance().enemies.get(i);
 			if (cam.frustum.sphereInFrustum(tmpVector3.set(enemy.position.x, enemy.position.y, 0), 1f)) {
 				model.idt();
 
 				tmp.setToTranslation(enemy.position.x, enemy.position.y, 0);
 				model.mul(tmp);
 
 				tmp.setToRotation(Vector3.Z, enemy.angle);
 				model.mul(tmp);
 				
 				tmp.setToScaling(0.95f*enemy.size, 0.95f*enemy.size, 0.95f*enemy.size);
 				model.mul(tmp);
 
 				transShader.setUniformMatrix("MMatrix", model);
 
 				transShader.setUniformf("a_color", Resources.getInstance().enemyColor[0]+(enemy.hitAnimate*2), Resources.getInstance().enemyColor[1]+(enemy.hitAnimate*2),
 						Resources.getInstance().enemyColor[2]+(enemy.hitAnimate*2), Resources.getInstance().enemyColor[3]+(enemy.hitAnimate*2) - enemy.dyingAnimate);
 				playerModel.render(transShader, GL20.GL_TRIANGLES);
 
 				transShader.setUniformf("a_color", Resources.getInstance().enemyEdgeColor[0], Resources.getInstance().enemyEdgeColor[1],
 						Resources.getInstance().enemyEdgeColor[2], Resources.getInstance().enemyEdgeColor[3]- enemy.dyingAnimate);
 				playerModel.render(transShader, GL20.GL_LINE_STRIP);
 
 			}
 		}
 		
 		for (int i = 0; i < GameInstance.getInstance().bullets.size; ++i) {
 			Ammo bullet = GameInstance.getInstance().bullets.get(i);
 			if (cam.frustum.sphereInFrustum(tmpVector3.set(bullet.position.x, bullet.position.y, 0), 1f)) {
 				model.idt();
 
 				tmp.setToTranslation(bullet.position.x, bullet.position.y, 0);
 				model.mul(tmp);
 
 				//tmp.setToRotation(Vector3.Z, MathUtils.radiansToDegrees * bullet.angle);
 				//model.mul(tmp);
 				
 				tmp.setToScaling(bullet.size,bullet.size,bullet.size);
 				model.mul(tmp);
 
 				transShader.setUniformMatrix("MMatrix", model);
 
 				transShader.setUniformf("a_color", Resources.getInstance().bulletColor[0], Resources.getInstance().bulletColor[1],
 						Resources.getInstance().bulletColor[2], Resources.getInstance().bulletColor[3]);
 				playerModel.render(transShader, GL20.GL_TRIANGLES);
 
 				transShader.setUniformf("a_color", Resources.getInstance().bulletEdgeColor[0], Resources.getInstance().bulletEdgeColor[1],
 						Resources.getInstance().bulletEdgeColor[2], Resources.getInstance().bulletEdgeColor[3]);
 				playerModel.render(transShader, GL20.GL_LINE_STRIP);
 
 			}
 		}
 		
 		//render player
 		{
 			tmp.idt();
 			model.idt();
 			
 			tmp.setToScaling(1f, 1f, 1f);
 			model.mul(tmp);
 			
 			tmp.setToTranslation(GameInstance.getInstance().player.position.x + GameInstance.getInstance().player.xdir*0.8f, GameInstance.getInstance().player.position.y-0.8f, 0);
 			model.mul(tmp);
 			
 			tmp.setToRotation(Vector3.Z, GameInstance.getInstance().player.angle);
 			model.mul(tmp);
 			
 			tmp.setToRotation(Vector3.X, angleXBack);
 			model.mul(tmp);
 			tmp.setToRotation(Vector3.Y, angleYBack);
 			model.mul(tmp);
 
 			tmp.setToScaling(0.5f, 0.5f, 0.5f);
 			model.mul(tmp);
 	
 			transShader.setUniformMatrix("MMatrix", model);
 			transShader.setUniformf("a_color",Resources.getInstance().playerColor[0], Resources.getInstance().playerColor[1], Resources.getInstance().playerColor[2], Resources.getInstance().playerColor[3]);
 			playerModel.render(transShader, GL20.GL_TRIANGLES);
 			
 			tmp.idt();
 			model.idt();
 			
 			tmp.setToScaling(1f, 1f, 1f);
 			model.mul(tmp);
 			
 			tmp.setToTranslation(GameInstance.getInstance().player.position.x, GameInstance.getInstance().player.position.y-0.8f, 0);
 			model.mul(tmp);
 			
 			tmp.setToRotation(Vector3.Z, GameInstance.getInstance().player.angle);
 			model.mul(tmp);
 			
 			tmp.setToRotation(Vector3.X, angleXBack);
 			model.mul(tmp);
 			tmp.setToRotation(Vector3.Y, angleYBack);
 			model.mul(tmp);
 
 			tmp.setToScaling(1.0f, 1.0f, 1.0f);
 			model.mul(tmp);
 
 			//render hull			
 			transShader.setUniformMatrix("MMatrix", model);
 			transShader.setUniformf("a_color",Resources.getInstance().playerEdgeColor[0], Resources.getInstance().playerEdgeColor[1], Resources.getInstance().playerEdgeColor[2], Resources.getInstance().playerEdgeColor[3]);
 			playerModel.render(transShader, GL20.GL_LINE_STRIP);
 		}
 		transShader.end();
 	}
 	
 	
 	private void renderBackground() {
 
 		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
 		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
 		
 		Gdx.gl20.glEnable(GL20.GL_BLEND);
 		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
 				
 		transShader.begin();
 		transShader.setUniformMatrix("VPMatrix", cam.combined);
 		
 		for(NoteJumper nj: noteJumpers) {
 			if(!nj.bofNote.isPlayedAt(ra.getTick())) {
 				nj.alive = false;
 				continue;
 			}
 			tmp.idt();
 			model.idt();
 			float move = (float) (1-Math.pow(nj.bofNote.getFraction(ra.getTick())-1, 4));
 			float move2 = (float) (1-Math.pow(nj.bofNote.getFraction(ra.getTick())-1, 2));
 			float length = (1-move);
 			Vector2 pos = new Vector2(nj.posA);
 			pos.x = nj.posA.x*(1-move) + nj.posB.x*(move);
 			pos.y = nj.posA.y*(1-move) + nj.posB.y*(move);
 			
 			tmp.setToTranslation(nj.posA.x,cam.position.y + nj.posA.y, -1-nj.bofNote.getChannel()*0);
 			model.mul(tmp);
 			
 			
 			Vector2 dist = new Vector2(nj.posB);
 			dist = dist.sub(nj.posA);
 			dist = dist.nor();
 			float rotate = (float) (Math.atan2(dist.y, dist.x)/Math.PI*180.0);
 			tmp.setToRotation(new Vector3(0f,0f,1f), rotate);//+(float)county);
 			model.mul(tmp);
 			
 			tmp.setToTranslation(nj.posA.dst(nj.posB)*move+0*length/2f,0, -1);
 			model.mul(tmp);
 			
 			tmp.setToScaling(length, 0.1f+nj.bofNote.getVelocity(), 0.1f);
 
 			model.mul(tmp);
 				
 			
 			transShader.setUniformMatrix("MMatrix", model);
 
 			transShader.setUniformf("a_color", 1,1,1,1);
 			blockModel.render(transShader,  GL20.GL_TRIANGLES);
 			///////////////////////////////////
 			
 			
 			tmp.idt();
 			model.idt();
 			move = nj.bofNote.getFraction(ra.getTick());//(float) (1-Math.pow(nj.bofNote.getFraction(tick)-1, 2));
 			pos = new Vector2(nj.posA);
 			pos.x = nj.posA.x*(1-move) + nj.posB.x*(move);
 			pos.y = nj.posA.y*(1-move) + nj.posB.y*(move);
 			tmp.setToTranslation(nj.posA.x,cam.position.y + nj.posA.y, 10f);
 			model.mul(tmp);
 			tmp.setToRotation(new Vector3(1f,0f, 0f), 90);
 			model.mul(tmp);
 			float factor = (float) (nj.bofNote.getDuration()/150.0);
 			tmp.setToScaling(0.5f+move*2*factor,0.5f+move*2*factor,0.5f+move*2*factor);
 
 			model.mul(tmp);
 
 			transShader.setUniformMatrix("MMatrix", model);
 
 			transShader.setUniformf("a_color", 1,1,1,1-move);//1*(nj.bofNote.getChannel()%2),1*(nj.bofNote.getChannel()%3),0,(1-nj.bofNote.getFraction(ra.getTick()))*nj.bofNote.getVelocity());
 			torusModel.render(transShader, GL20.GL_TRIANGLES);
 
 		}
 		
 		/*
 		// render plane background
 		for (int x = -0; x < 50; x = x +2) {
 			for (int z = -10; z < 10; z = z +2) {
 			model.idt();
 
 			tmp.setToTranslation(x, -25, z);
 			model.mul(tmp);
 
 			tmp.setToScaling(0.95f, 0.95f, 0.95f);
 			model.mul(tmp);
 
 			transShader.setUniformMatrix("MMatrix", model);
 
 			transShader.setUniformf("a_color", Resources.getInstance().blockBackgroundColor[0], Resources.getInstance().blockBackgroundColor[1],
 					Resources.getInstance().blockBackgroundColor[2], Resources.getInstance().blockBackgroundColor[3] - Helper.map(z, 10, -20, 0, 1.0f));
 			blockModel.render(transShader, GL20.GL_TRIANGLES);
 
 			transShader.setUniformf("a_color", Resources.getInstance().blockBackgroundEdgeColor[0], Resources.getInstance().blockBackgroundEdgeColor[1],
 					Resources.getInstance().blockBackgroundEdgeColor[2], Resources.getInstance().blockBackgroundEdgeColor[3] - Helper.map(z, 10, -20, 0, 1.0f));
 			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
 			}
 		}
 		
 		// render static boxes
 		for (int i = 0; i < GameInstance.getInstance().background.backgroundBlocks.size; ++i) {
 			Block block = GameInstance.getInstance().background.backgroundBlocks.get(i);
 
 			model.idt();
 
 			tmp.setToTranslation(block.position.x, block.position.y, 0);
 			model.mul(tmp);
 
 			tmp.setToScaling(0.95f, 0.95f, 0.95f);
 			model.mul(tmp);
 
 			transShader.setUniformMatrix("MMatrix", model);
 
 			transShader.setUniformf("a_color", Resources.getInstance().blockBackgroundColor[0], Resources.getInstance().blockBackgroundColor[1],
 					Resources.getInstance().blockBackgroundColor[2], Resources.getInstance().blockBackgroundColor[3] + block.highlightAnimate);
 			blockModel.render(transShader, GL20.GL_TRIANGLES);
 
 			transShader.setUniformf("a_color", Resources.getInstance().blockBackgroundEdgeColor[0], Resources.getInstance().blockBackgroundEdgeColor[1],
 					Resources.getInstance().blockBackgroundEdgeColor[2], Resources.getInstance().blockBackgroundEdgeColor[3]);
 			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
 
 		}
 		
 		// render move boxes
 		for (int i = 0; i < GameInstance.getInstance().background.backgroundMoveBlocks.size; ++i) {
 			Block block = GameInstance.getInstance().background.backgroundMoveBlocks.get(i);
 
 			model.idt();
 
 			tmp.setToTranslation(block.position.x, block.position.y, 0);
 			model.mul(tmp);
 
 			block.angle += MathUtils.sin(startTime) * delta * 10f;
 
 			tmp.setToRotation(Vector3.Y, block.angle);
 			model.mul(tmp);
 
 			tmp.setToScaling(0.95f, 0.95f, 0.95f);
 			model.mul(tmp);
 
 			transShader.setUniformMatrix("MMatrix", model);
 
 			transShader.setUniformf("a_color", Resources.getInstance().blockBackgroundColor[0], Resources.getInstance().blockBackgroundColor[1],
 					Resources.getInstance().blockBackgroundColor[2], Resources.getInstance().blockBackgroundColor[3] + block.highlightAnimate);
 			blockModel.render(transShader, GL20.GL_TRIANGLES);
 
 			transShader.setUniformf("a_color", Resources.getInstance().blockBackgroundEdgeColor[0], Resources.getInstance().blockBackgroundEdgeColor[1],
 					Resources.getInstance().blockBackgroundEdgeColor[2], Resources.getInstance().blockBackgroundEdgeColor[3]);
 			wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
 		}*/
 			
 		
 		transShader.end();
 	}
 	
 	@Override
 	public boolean keyDown(int keycode) {
 		if(keycode == Keys.W) GameInstance.getInstance().player.jump = true;
 		
 		if (keycode == Keys.ESCAPE) {
 			game.setScreen(new MainMenu(game));
 		}
 		
 		if (keycode == Keys.R) {
 			GameInstance.getInstance().addEnemy();
 		}
 		if (keycode == Keys.S) {
 			ra.saveMidi();
 		}
 		
 		if (keycode == Keys.Z) {
 			int random = MathUtils.random(0,GameInstance.getInstance().blankBlocks.size-1);
 			GameInstance.getInstance().addPowerUp(GameInstance.getInstance().blankBlocks.get(random).position.x,GameInstance.getInstance().blankBlocks.get(random).position.y);
 		}
 				
 		if (keycode == Keys.F1) {
 			Resources.getInstance().prefs.putBoolean("bloom", !Resources.getInstance().prefs.getBoolean("bloom"));
 			Resources.getInstance().bloomOnOff = !Resources.getInstance().prefs.getBoolean("bloom");
 			Resources.getInstance().prefs.flush();			
 		}
 		
 		if (keycode == Keys.T) {
 			GameInstance.getInstance().changeWeapon();
 		}
 		
 		if (keycode == Input.Keys.F) {
 			if(Gdx.app.getType() == ApplicationType.Desktop) {
 				if(!org.lwjgl.opengl.Display.isFullscreen()) {
 					Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);
 				} else {
 					Gdx.graphics.setDisplayMode(800,480, false);
 				}
 			}
 			Resources.getInstance().prefs.putBoolean("fullscreen", !Resources.getInstance().prefs.getBoolean("fullscreen"));
 			Resources.getInstance().fullscreenOnOff = !Resources.getInstance().prefs.getBoolean("fullscreen");
 			Resources.getInstance().prefs.flush();
 		}
 		return false;
 	}
  
 	@Override
 	public boolean keyUp(int keycode) {
 		if(keycode == Keys.W) GameInstance.getInstance().player.jump = false;
 		return false;
 	}
  
 	Vector2 last = null;
 	Vector3 point = new Vector3();
  
 	@Override
 	public boolean touchDown(int x, int y, int pointerId, int button) {
 		return false;
 	}
 
 	@Override
 	public void hide() {
 		System.out.println("dispose game screen");
 		ra.stop();
 	}
 
 	public float MidiToFrequenc(float note) {
 		return (float) (Math.pow(2, (note - 9) / 12.0) * 440);
 	}
 
 	@Override
 	public boolean keyTyped(char arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchMoved(int arg0, int arg1) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
 
 
