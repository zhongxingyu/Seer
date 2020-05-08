 package propinquity;
 
 import java.util.*;
 
 import javax.media.opengl.GL;
 
 import org.jbox2d.collision.FilterData;
 import org.jbox2d.collision.MassData;
 import org.jbox2d.collision.shapes.*;
 import org.jbox2d.common.*;
 import org.jbox2d.dynamics.*;
 import org.jbox2d.testbed.TestSettings;
 
 import controlP5.ControlEvent;
 
 import pbox2d.*;
 
 import processing.core.*;
 import processing.opengl.PGraphicsOpenGL;
 import proxml.*;
 import xbee.XBeeReader;
 
import propinquity.xbee.*;

 public class Propinquity extends PApplet {
 
 	// Unique serialization ID
 	private static final long serialVersionUID = 6340518174717159418L;
 
 	// debug constants
 	final boolean DEBUG = false;
 	final boolean DEBUG_XBEE = false;
 	final boolean DRAW_SHADOWS = false;
 	final boolean DRAW_PARTICLES = true;
 	final int FULL_SCREEN_ID = 0;
 
 	// liquid constants
 	final float PARTICLE_SCALE = 0.8f;
 	final float PARTICLE_SCALE_RANGE = 0.5f;
 	final int TEXTURE_HALF = 32;
 	final int AVG_PTS_PER_STEP = 250;
 	final int AVG_PARTICLE_PER_STEP = 50;
 	final int APPROX_MAX_PARTICLES = 1600;
 	final int MAX_PARTICLES_PER_FRAME = 5;
 	final Integer LIQUID_MAGIC = new Integer(12345);
 	final float EMITTER_RADIUS = 0.14f;
 	final int SHADOW_X = 8;
 	final int SHADOW_Y = 8;
 	final float MIN_RELEASE_FORCE = 0.4f;
 	final float MAX_RELEASE_FORCE = 0.6f;
 	final float WORLD_SIZE = 2f;
 	final float EMITTER_ANGULAR_VELOCITY = 4 * TWO_PI;
 
 	// final int NUM_STEP_PER_PERIOD = 4;
 	final float PUSH_PERIOD_ROT_SPEED = 1f;
 	final float PUSH_DAMPENING = 0.98f;
 
 	// game constants
 	final int END_LEVEL_TIME = 6;
 	final int BOUNDARY_WIDTH = 5;
 	final int[] PLAYER_COLORS = { color(55, 137, 254), color(255, 25, 0) };
 	final int NEUTRAL_COLOR = color(142, 20, 252);
 
 
 	GameState gameState;
 
 	// level select controller
 	LevelSelect levelSelect = null;
 
 	// player list controller
 	PlayerList playerList = null;
 
 	// OpenGL
 	GL gl;
 
 	// Box2D
 	PBox2D box2d;
 	TestSettings settings;
 
 	// Liquid parameters
 	ArrayList<Integer>[][] hash;
 	int hashWidth, hashHeight;
 	float totalMass = 100.0f;
 	float particleRadius = 0.11f; // 0.11
 	float particleViscosity = 0.005f;// 0.027f;
 	float damp = 0.7f; // 0.095
 	float fluidMinX = -WORLD_SIZE / 2f;
 	float fluidMaxX = WORLD_SIZE / 2f;
 	float fluidMinY = -WORLD_SIZE / 2f;
 	float fluidMaxY = WORLD_SIZE / 2f;
 
 	// Particle graphics
 	PImage[] imgParticle;
 	PImage imgShadow;
 	PGraphics[] pgParticle;
 
 	// Level parameters
 	Level level;
 	int ptsPerParticle = 0;
 	LinkedList<Particle>[] particles;
 	int lastPeriodStep = 0;
 	Particle[] lastPeriodParticle;
 	boolean endedLevel = false;
 	long doneTime = -1;
 	boolean groupedParticles = false;
 	int numStepsPerPeriod;
 
 	// XML
 	XMLInOut xmlInOut;
 
 	// HUD (Heads Up Display) -- shows the score.
 	Hud hud;
 
 	// XBees
 	public XBeeManager xbeeManager;
 	
 	Fences fences;
 
 	//Logger
 	Logger logger;
 	Sounds sounds;
 	Graphics graphics;
 
 	UIElement[] ui_elements;
 
 	public void setup() {
 		
 		// Setup graphics and sound
 		sounds = new Sounds(this);
 		graphics = new Graphics(this);
 
 		// initial opengl setup
 		gl = ((PGraphicsOpenGL) g).gl;
 		gl.glDisable(GL.GL_DEPTH_TEST);
 
 		// Load common artwork and sound
 		graphics.loadCommonContent();
 		sounds.loadCommonContent();
 		
 		// Create resources
 		xbeeManager = new XBeeManager(this, DEBUG_XBEE);
 
 		playerList = new PlayerList(this);
 		
 		hud = new Hud(this, sounds, graphics);
 
 		// init logging
 		logger = new Logger(this, "bin/messages.txt");
 
 		ui_elements = new UIElement[] {xbeeManager, playerList};
 
 		changeGameState(GameState.XBeeInit);
	}
 
 	void initBox2D() {
 		// initialize box2d physics and create the world
 		box2d = new PBox2D(this, (float) height / WORLD_SIZE);
 		box2d.createWorld(-WORLD_SIZE / 2f, -WORLD_SIZE / 2f, WORLD_SIZE, WORLD_SIZE);
 		box2d.setGravity(0.0f, 0.0f);
 
 		// load default jbox2d settings
 		settings = new TestSettings();
 	}
 
 	void initTextures() {
 		imgParticle = new PImage[level.getNumPlayers()];
 		for (int i = 0; i < level.getNumPlayers(); i++)
 			imgParticle[i] = loadImage("data/particles/player" + (i + 1) + ".png");
 
 		if (DRAW_SHADOWS)
 			imgShadow = loadImage("data/particles/shadow.png");
 
 		pgParticle = new PGraphics[level.getNumPlayers()];
 		for (int i = 0; i < level.getNumPlayers(); i++) {
 			pgParticle[i] = createGraphics(imgParticle[i].width, imgParticle[i].height, P2D);
 			pgParticle[i].background(imgParticle[i]);
 			pgParticle[i].mask(imgParticle[i]);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	// TODO: Fix this madness
 	void initParticles() {
 		// init box2d
 		initBox2D();
 
 		// load textures
 		initTextures();
 
 		// create the boundary fences
 		fences = new Fences(this);
 
 		// init hash to space sort particles
 		hashWidth = 40;
 		hashHeight = 40;
 		hash = new ArrayList[hashHeight][hashWidth];
 		for (int i = 0; i < hashHeight; ++i) {
 			for (int j = 0; j < hashWidth; ++j) {
 				hash[i][j] = new ArrayList<Integer>();
 			}
 		}
 
 		// init particles
 		// particles = new Particle[level.getNumPlayers()][MAX_PARTICLES /
 		// level.getNumPlayers()];
 		particles = new LinkedList[level.getNumPlayers()];
 		for (int i = 0; i < particles.length; i++)
 			particles[i] = new LinkedList<Particle>();
 
 		ptsPerParticle = (level.getNumSteps() * AVG_PTS_PER_STEP * level.getNumPlayers()) / APPROX_MAX_PARTICLES;
 		// pCount = new int[level.getNumPlayers()];
 		numStepsPerPeriod = round(AVG_PARTICLE_PER_STEP * ptsPerParticle / AVG_PTS_PER_STEP);
 		if (numStepsPerPeriod == 0)
 			++numStepsPerPeriod;
 		lastPeriodParticle = new Particle[level.getNumPlayers()];
 
 		println("Points per particle: " + ptsPerParticle);
 	}
 
 	public void draw() {
 		// clear
 		background(0);
 
 		for(int i = 0;i < ui_elements.length;i++) ui_elements[i].draw();
 
 		switch (gameState) {
 		case LevelSelect:
 			// TODO: To fix next.
 			
 			// init level select UI
 			if (levelSelect == null) {
 				playerList.dispose();
 				levelSelect = new LevelSelect(this, sounds, playerList);
 			}
 			levelSelect.draw();
 			break;
 
 		case Play:
 			drawPlay();
 			break;
 		}
 
 		logger.recordFrame();
 	}
 
 	public void stop() {
 		if (gameState == GameState.Play)
 			level.clear();
 	}
 
 	void drawPlay() {
 		drawInnerBoundary();
 		if (DRAW_PARTICLES)
 			drawParticles();
 		drawMask();
 		drawOuterBoundary();
 
 		if (DEBUG)
 			fences.drawDebugFence();
 
 		hud.draw();
 
 		if (level.isDone()) {
 
 			if (endedLevel) {
 				Player winner = level.getWinner();
 
 				textAlign(CENTER);
 				pushMatrix();
 				translate(width / 2, height / 2);
 				rotate(frameCount * Hud.PROMPT_ROT_SPEED);
 				image(graphics.hudLevelComplete, 0, -25);
 				textFont(Graphics.font, Hud.FONT_SIZE);
 				textAlign(CENTER, CENTER);
 				fill(winner != null ? winner.getColor() : NEUTRAL_COLOR);
 				noStroke();
 				text(winner != null ? winner.getName() + " won!" : "You tied!", 0, 0);
 				image(graphics.hudPlayAgain, 0, 30);
 				popMatrix();
 			} else {
 				// keep track of done time
 				if (doneTime == -1) {
 					level.clear();
 					doneTime = frameCount;
 				}
 
 				// give the last push
 				pushPeriod(true);
 
 				// step through time
 				box2d.step();
 
 				// liquify
 				liquify();
 
 				// snap score in final position
 				hud.snap();
 
 				// pull particles into groups
 				groupParticles();
 
 				// flag as ended
 				if (doneTime != -1 && frameCount > doneTime + Graphics.FPS * END_LEVEL_TIME)
 					endedLevel = true;
 			}
 
 		} else if (level.isRunning()) {
 			// update hud
 			hud.update(hud.getAngle() + HALF_PI, TWO_PI / 10000f, TWO_PI / 2000f);
 
 			// push particles of current period out
 			pushPeriod();
 
 			// release balls
 			updateParticles();
 
 			// step through time
 			box2d.step();
 
 			// liquify
 			liquify();
 
 			// process level
 			level.update();
 
 			// read level data stub
 			// if (USE_STUB) level.processStub();
 
 		} else {
 			gl = ((PGraphicsOpenGL) g).gl;
 			gl.glEnable(GL.GL_BLEND);
 			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 
 			fill(255);
 			textAlign(CENTER);
 			pushMatrix();
 			translate(width / 2, height / 2);
 			rotate(frameCount * Hud.PROMPT_ROT_SPEED);
 			image(graphics.hudPlay, 0, 0);
 			popMatrix();
 		}
 	}
 
 	void resetLevel() {
 		resetLiquid();
 		level.reset();
 		endedLevel = false;
 		doneTime = -1;
 		groupedParticles = false;
 		lastPeriodParticle = new Particle[level.getNumPlayers()];
 
 		hud.reset();
 
 		levelSelect.reset();
 		gameState = GameState.LevelSelect;
 		println("gamestate = " + gameState);
 	}
 
 	void drawInnerBoundary() {
 		gl = ((PGraphicsOpenGL) g).gl;
 		gl.glEnable(GL.GL_BLEND);
 		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 
 		pushMatrix();
 		translate(width / 2 - 1, height / 2);
 		image(graphics.hudInnerBoundary, 0, 0);
 		popMatrix();
 	}
 
 	void drawOuterBoundary() {
 		gl = ((PGraphicsOpenGL) g).gl;
 		gl.glEnable(GL.GL_BLEND);
 		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 
 		pushMatrix();
 		translate(width / 2 - 1, height / 2);
 		image(graphics.hudOuterBoundary, 0, 0);
 		popMatrix();
 	}
 
 	void drawParticles() {
 		gl = ((PGraphicsOpenGL) g).gl;
 		gl.glEnable(GL.GL_BLEND);
 		gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
 
 		for (int i = 0; i < level.getNumPlayers(); i++)
 			drawParticles(i);
 	}
 
 	void drawParticles(int p) {
 		// draw balls
 		noStroke();
 		noFill();
 
 		ListIterator<Particle> it = particles[p].listIterator();
 		while (it.hasNext())
 			(it.next()).draw();
 	}
 
 	void drawMask() {
 		gl = ((PGraphicsOpenGL) g).gl;
 		gl.glEnable(GL.GL_BLEND);
 		gl.glBlendFunc(GL.GL_DST_COLOR, GL.GL_ZERO);
 
 		pushMatrix();
 		translate(width / 2, height / 2);
 		scale(width / 2, height / 2);
 		beginShape(QUADS);
 		texture(hud.hudMask);
 		vertex(-1, -1, 0, 0, 0);
 		vertex(1, -1, 0, 1, 0);
 		vertex(1, 1, 0, 1, 1);
 		vertex(-1, 1, 0, 0, 1);
 		endShape(CLOSE);
 		popMatrix();
 	}
 
 	void updateParticles() {
 		for (int i = 0; i < level.getNumPlayers(); i++)
 			updateParticles(i);
 	}
 
 	void updateParticles(int p) {
 		Player player = level.getPlayer(p);
 		int nParticles;
 
 		// release particles if the player has accumulated period pts
 		nParticles = min(player.getPeriodPts() / ptsPerParticle, MAX_PARTICLES_PER_FRAME);
 		if (nParticles > 0) {
 			// if (pCount[p]+nParticles > MAX_PARTICLES/2)
 			// nParticles = MAX_PARTICLES/2-pCount[p];
 
 			releaseParticles(p, nParticles);
 		}
 
 		// kill particles if the player touched
 		nParticles = min(player.getKillPts() / ptsPerParticle, MAX_PARTICLES_PER_FRAME);
 		if (nParticles > 0) {
 			killParticles(p, nParticles);
 		}
 	}
 
 	void releaseParticles(int p, int nParticles) {
 		Player player = level.getPlayer(p);
 
 		float releaseAngle = level.getTime() * Hud.SCORE_ROT_SPEED / EMITTER_ANGULAR_VELOCITY;
 		if (p % 2 == 1)
 			releaseAngle += PI;
 
 		float massPerParticle = totalMass / APPROX_MAX_PARTICLES;
 
 		CircleDef pd = new CircleDef();
 		pd.filter.categoryBits = p + 1;
 		pd.filter.maskBits = Fences.INNER_MASK | Fences.OUTER_MASK | Fences.PLAYERS_MASK;
 		pd.filter.groupIndex = -(p + 1);
 		pd.density = 1.0f;
 		pd.radius = 0.040f;
 		pd.restitution = 0.1f;
 		pd.friction = 0.0f;
 
 		for (int i = 0; i < nParticles; ++i) {
 			BodyDef bd = new BodyDef();
 			bd.position = new Vec2(cos(releaseAngle) * (EMITTER_RADIUS * random(0.8f, 1)), sin(releaseAngle)
 					* (EMITTER_RADIUS * random(0.8f, 1)));
 			// bd.position = new Vec2(cx, cy);
 			bd.fixedRotation = true;
 			Body b = box2d.createBody(bd);
 			Shape sh = b.createShape(pd);
 			sh.setUserData(LIQUID_MAGIC);
 			MassData md = new MassData();
 			md.mass = massPerParticle;
 			md.I = 1.0f;
 			b.setMass(md);
 			b.allowSleeping(false);
 
 			// particles[p][i] = new Particle(b, sh,
 			// PARTICLE_SCALE*random(1.0-PARTICLE_SCALE_RANGE, 1.0),
 			// pgParticle[p]);
 			particles[p].add(new Particle(this, b, sh, PARTICLE_SCALE * random(1.0f - PARTICLE_SCALE_RANGE, 1.0f),
 					pgParticle[p]));
 		}
 
 		// keep track of the released particles
 		player.subPeriodPts(nParticles * ptsPerParticle);
 		// pCount[p] += nParticles;
 		// totalParticles += nParticles;
 	}
 
 	void killParticles(int p, int nParticles) {
 		// get player
 		Player player = level.getPlayer(p);
 
 		// clear kill pts
 		player.subKillPts(nParticles * ptsPerParticle);
 
 		Particle particle;
 		boolean killedLastPeriodParticle = false;
 		while (nParticles > 0 && particles[p].size() > 0) {
 			particle = particles[p].removeFirst();
 			if (particle == lastPeriodParticle[p])
 				killedLastPeriodParticle = true;
 			box2d.destroyBody(particle.body);
 			nParticles--;
 		}
 
 		// adjust the last period particle push in case
 		// we killed some particles that were within the
 		// inner fence. if we don't do that the new particles
 		// will get trapped in.
 		if (killedLastPeriodParticle) {
 			if (particles[p].isEmpty())
 				lastPeriodParticle[p] = null;
 			else
 				lastPeriodParticle[p] = particles[p].getLast();
 		}
 	}
 
 	void liquify() {
 		for (int i = 0; i < level.getNumPlayers(); i++)
 			liquify(i);
 	}
 
 	void liquify(int p) {
 		float dt = 1.0f / this.settings.hz;
 
 		hashLocations(p);
 		applyLiquidConstraint(p, dt);
 		dampenLiquid(p);
 	}
 
 	void hashLocations(int p) {
 		for (int a = 0; a < hashWidth; a++) {
 			for (int b = 0; b < hashHeight; b++) {
 				hash[a][b].clear();
 			}
 		}
 
 		Particle particle;
 		// for(int a = 0; a < particles[p].size(); a++)
 		// {
 		// particle = particles[p].get(a);
 		int i = 0;
 		ListIterator<Particle> it = particles[p].listIterator();
 		while (it.hasNext()) {
 			particle = it.next();
 			int hcell = hashX(particle.body.m_sweep.c.x);
 			int vcell = hashY(particle.body.m_sweep.c.y);
 			if (hcell > -1 && hcell < hashWidth && vcell > -1 && vcell < hashHeight)
 				hash[hcell][vcell].add(new Integer(i));
 			i++;
 		}
 	}
 
 	int hashX(float x) {
 		float f = PApplet.map(x, fluidMinX, fluidMaxX, 0, hashWidth - .001f);
 		return (int) f;
 	}
 
 	int hashY(float y) {
 		float f = PApplet.map(y, fluidMinY, fluidMaxY, 0, hashHeight - .001f);
 		return (int) f;
 	}
 
 	void applyLiquidConstraint(int p, float deltaT) {
 		//
 		// Unfortunately, this simulation method is not actually scale
 		// invariant, and it breaks down for rad < ~3 or so. So we need
 		// to scale everything to an ideal rad and then scale it back after.
 		//
 		final float idealRad = 50.0f;
 		float multiplier = idealRad / particleRadius;
 
 		int count = particles[p].size();
 		float[] xchange = new float[count];
 		float[] ychange = new float[count];
 		Arrays.fill(xchange, 0.0f);
 		Arrays.fill(ychange, 0.0f);
 
 		float[] xs = new float[count];
 		float[] ys = new float[count];
 		float[] vxs = new float[count];
 		float[] vys = new float[count];
 
 		// for (int i=0; i<count; ++i) {
 		// particle = particles[p][i];
 		Particle particle;
 		ListIterator<Particle> it;
 
 		it = particles[p].listIterator();
 		int i = 0;
 		while (it.hasNext()) {
 			particle = it.next();
 			xs[i] = multiplier * particle.body.m_sweep.c.x;
 			ys[i] = multiplier * particle.body.m_sweep.c.y;
 			vxs[i] = multiplier * particle.body.m_linearVelocity.x;
 			vys[i] = multiplier * particle.body.m_linearVelocity.y;
 			i++;
 		}
 
 		it = particles[p].listIterator();
 		i = 0;
 		while (it.hasNext()) {
 			particle = it.next();
 			// Populate the neighbor list from the 9 proximate cells
 			ArrayList<Integer> neighbors = new ArrayList<Integer>();
 			int hcell = hashX(particle.body.m_sweep.c.x);
 			int vcell = hashY(particle.body.m_sweep.c.y);
 			for (int nx = -1; nx < 2; nx++) {
 				for (int ny = -1; ny < 2; ny++) {
 					int xc = hcell + nx;
 					int yc = vcell + ny;
 					if (xc > -1 && xc < hashWidth && yc > -1 && yc < hashHeight && hash[xc][yc].size() > 0) {
 						for (int a = 0; a < hash[xc][yc].size(); a++) {
 							Integer ne = hash[xc][yc].get(a);
 							if (ne != null && ne.intValue() != i)
 								neighbors.add(ne);
 						}
 					}
 				}
 			}
 
 			// Particle pressure calculated by particle proximity
 			// Pressures = 0 iff all particles within range are idealRad
 			// distance away
 			float[] vlen = new float[neighbors.size()];
 			float pres = 0.0f;
 			float pnear = 0.0f;
 			for (int a = 0; a < neighbors.size(); a++) {
 				Integer n = neighbors.get(a);
 				int j = n.intValue();
 				float vx = xs[j] - xs[i];
 				float vy = ys[j] - ys[i];
 
 				// early exit check
 				if (vx > -idealRad && vx < idealRad && vy > -idealRad && vy < idealRad) {
 					float vlensqr = (vx * vx + vy * vy);
 					// within idealRad check
 					if (vlensqr < idealRad * idealRad) {
 						vlen[a] = (float) Math.sqrt(vlensqr);
 						if (vlen[a] < Settings.EPSILON)
 							vlen[a] = idealRad - .01f;
 						float oneminusq = 1.0f - (vlen[a] / idealRad);
 						pres = (pres + oneminusq * oneminusq);
 						pnear = (pnear + oneminusq * oneminusq * oneminusq);
 					} else {
 						vlen[a] = Float.MAX_VALUE;
 					}
 				}
 			}
 
 			// Now actually apply the forces
 			// System.out.println(p);
 			float pressure = (pres - 5F) / 2.0F; // normal pressure term
 			float presnear = pnear / 2.0F; // near particles term
 			float changex = 0.0F;
 			float changey = 0.0F;
 			for (int a = 0; a < neighbors.size(); a++) {
 				Integer n = neighbors.get(a);
 				int j = n.intValue();
 				float vx = xs[j] - xs[i];
 				float vy = ys[j] - ys[i];
 				
 				if (vx > -idealRad && vx < idealRad && vy > -idealRad && vy < idealRad) {
 					if (vlen[a] < idealRad) {
 						float q = vlen[a] / idealRad;
 						float oneminusq = 1.0f - q;
 						float factor = oneminusq * (pressure + presnear * oneminusq) / (2.0F * vlen[a]);
 						float dx = vx * factor;
 						float dy = vy * factor;
 						float relvx = vxs[j] - vxs[i];
 						float relvy = vys[j] - vys[i];
 						factor = particleViscosity * oneminusq * deltaT;
 						dx -= relvx * factor;
 						dy -= relvy * factor;
 
 						xchange[j] += dx;
 						ychange[j] += dy;
 						changex -= dx;
 						changey -= dy;
 					}
 				}
 			}
 
 			xchange[i] += changex;
 			ychange[i] += changey;
 			i++;
 		}
 		// multiplier *= deltaT;
 		it = particles[p].listIterator();
 		i = 0;
 		while (it.hasNext()) {
 			particle = it.next();
 			particle.body.m_xf.position.x += xchange[i] / multiplier;
 			particle.body.m_xf.position.y += ychange[i] / multiplier;
 			particle.body.m_linearVelocity.x += xchange[i] / (multiplier * deltaT);
 			particle.body.m_linearVelocity.y += ychange[i] / (multiplier * deltaT);
 			i++;
 		}
 	}
 
 	void dampenLiquid(int p) {
 		Particle particle;
 		ListIterator<Particle> it = particles[p].listIterator();
 		while (it.hasNext()) {
 			particle = it.next();
 			particle.body.setLinearVelocity(particle.body.getLinearVelocity().mul(damp));
 		}
 	}
 
 	void resetLiquid() {
 		for (int p = 0; p < level.getNumPlayers(); p++) {
 			// for (int i=0; i<particles[p].size(); ++i) {
 			// box2d.destroyBody((particles[p].removeFirst()).body);
 			// }
 			Particle particle;
 			ListIterator<Particle> it = particles[p].listIterator();
 			while (it.hasNext()) {
 				particle = it.next();
 				box2d.destroyBody(particle.body);
 				it.remove();
 			}
 		}
 	}
 
 	void pushPeriod() {
 		pushPeriod(false);
 	}
 
 	void pushPeriod(boolean override) {
 		int cStep = level.getCurrentStep();
 
 		// go through particles
 		// apply the force from the previous push
 		for (int p = 0; p < level.getNumPlayers(); p++) {
 
 			Particle particle = null;
 			ListIterator<Particle> it = particles[p].listIterator();
 			while (it.hasNext() && particle != lastPeriodParticle[p]) {
 				particle = it.next();
 
 				particle.body.m_linearVelocity.x += particle.push.x;
 				particle.body.m_linearVelocity.y += particle.push.y;
 				particle.push.x *= PUSH_DAMPENING;
 				particle.push.y *= PUSH_DAMPENING;
 			}
 			// println("last period step " + lastPeriodStep);
 			// println("num steps per period: " + numStepsPerPeriod);
 
 			if (!override && (lastPeriodStep == cStep || cStep % numStepsPerPeriod != 0))
 				continue;
 
 			// go through particles
 			// remove collision with inner fence
 			// and apply push outward
 			FilterData filter = new FilterData();
 			filter.groupIndex = -(p + 1);
 			filter.categoryBits = p + 1;
 			filter.maskBits = Fences.OUTER_MASK | Fences.PLAYERS_MASK;
 
 			float angle = level.getTime() * PUSH_PERIOD_ROT_SPEED + TWO_PI / level.getNumPlayers() * p;
 			float force = random(MIN_RELEASE_FORCE, MAX_RELEASE_FORCE);
 
 			while (it.hasNext()) {
 				particle = it.next();
 
 				particle.shape.setFilterData(filter);
 				box2d.world.refilter(particle.shape);
 
 				particle.push.x -= cos(angle) * force;
 				particle.push.y -= sin(angle) * force;
 			}
 
 			lastPeriodParticle[p] = particle;
 		}
 
 		lastPeriodStep = cStep;
 	}
 
 	void groupParticles() {
 		Particle particle;
 
 		if (!groupedParticles) {
 			for (int p = 0; p < level.getNumPlayers(); p++) {
 				FilterData filter = new FilterData();
 				filter.groupIndex = -1;
 				filter.categoryBits = p + 1;
 				filter.maskBits = Fences.OUTER_MASK;
 
 				ListIterator<Particle> it = particles[p].listIterator();
 				while (it.hasNext()) {
 					particle = it.next();
 					particle.shape.setFilterData(filter);
 					box2d.world.refilter(particle.shape);
 				}
 			}
 
 			groupedParticles = true;
 		}
 
 		for (int p = 0; p < level.getNumPlayers(); p++) {
 			ListIterator<Particle> it = particles[p].listIterator();
 			while (it.hasNext()) {
 				particle = it.next();
 
 				Body b = particle.body;
 
 				// if (p == 0 && pos.x < width/2)
 				if (p == 0)
 					b.m_linearVelocity.x += 0.20f;
 				else if (p == 1)
 					b.m_linearVelocity.x -= 0.20f;
 			}
 		}
 	}
 
 	public void changeGameState(GameState new_state) {
 		for(int i = 0;i < ui_elements.length;i++) ui_elements[i].hide();
 
 		switch(new_state) {
 			case XBeeInit:
 				xbeeManager.show();
 				break;
 			case PlayerList:
 				playerList.show();
 				break;
 			case LevelSelect:
 
 				break;
 			case Play:
 
 				break;
 		}
 
 		gameState = new_state;
 
 		println("gamestate = " + gameState);
 	}
 
 	public void xBeeEvent(XBeeReader xbee) {
 
 		switch (gameState) {
 
 		case XBeeInit:
 			xbeeManager.xBeeEvent(xbee);
 			break;
 
 		case LevelSelect:
 			println("xBeeEvent(): sending to level select");
 			levelSelect.xBeeEvent(xbee);
 			break;
 
 		case Play:
 			level.xBeeEvent(xbee);
 			break;
 		}
 
 	}
 
 	public void controlEvent(ControlEvent theEvent) {
 		switch(gameState) {
 			case XBeeInit:
 				xbeeManager.controlEvent(theEvent);
 				break;
 			case PlayerList:
 				playerList.controlEvent(theEvent);
 				break;
 		}
 	}
 
 	public void keyPressed() {
 		switch (gameState) {
 			case XBeeInit:
 				xbeeManager.keyPressed(keyCode);
 				break;
 			case PlayerList:
 				playerList.keyPressed(keyCode);
 				break;
 
 			case LevelSelect:
 				switch (key) {
 					case BACKSPACE:
 						levelSelect.clear();
 						levelSelect = null;
 						playerList = null;
 						// initPlayerListCtrl();
 						changeGameState(GameState.PlayerList);
 						break;
 
 					default:
 						if (levelSelect != null) {
 							// pass the key to the level select controller
 							levelSelect.keyPressed(key, keyCode);
 
 							// check if the level select controller is done
 							// and ready to play
 							if (levelSelect.isDone()) {
 								// init level
 								level = new Level(this, sounds, levelSelect.players, levelSelect.levelFile);
 								graphics.loadLevelContent();
 								
 								while (true)
 									if (level.successfullyRead() > -1)
 										break;
 
 								if (level.successfullyRead() == 0) {
 									level.loadDefaults();
 									System.err.println("I had some trouble reading the level file.");
 									System.err.println("Defaulting to 2 minutes of free play instead.");
 								}
 								
 								// send configuration message here
 								// TODO: send step length to proximity patches
 								
 								delay(50);
 								while (!levelSelect.allAcksIn()) {
 									println("sending again");
 									levelSelect.sendConfigMessages(level.getStepInterval());
 									delay(50);
 								}
 
 								// init liquid particles
 								initParticles();
 
 								// play
 								gameState = GameState.Play;
 								println("gamestate = " + gameState);
 							}
 						}
 						break;
 				}
 			break;
 
 		case Play:
 			switch (key) {
 
 			case ESC:
 				level.clear();
 				exit();
 				break;
 
 			case ENTER:
 			case ' ':
 				if (level.isDone() && endedLevel)
 					resetLevel();
 				else if (!level.isDone() && level.isPaused())
 					level.start();
 				else if (!level.isDone())
 					level.pause();
 				break;
 
 			case BACKSPACE:
 				if (level.isPaused())
 					resetLevel();
 				break;
 
 			case 'i': // info
 				println("Particles: " + (particles[0].size() + particles[1].size()));
 				println("Framerate: " + frameRate);
 				println("Radius: " + particleRadius);
 				println("Viscosity: " + particleViscosity);
 				break;
 
 			case '8':
 				particleRadius += 0.01;
 				break;
 
 			case '2':
 				particleRadius -= 0.01;
 				if (particleRadius < 0)
 					particleViscosity = 0;
 				break;
 
 			case '4':
 				particleViscosity -= 0.001;
 				if (particleViscosity < 0)
 					particleViscosity = 0;
 				break;
 
 			case '6':
 				particleViscosity += 0.001;
 				break;
 
 			case 'e': // play stub
 				level.currentStep = level.numSteps;
 				break;
 
 			case 'f': // flush output and close
 				logger.close();
 				exit();
 				break;
 			}
 			break;
 		}
 
 	}
 
 	static public void main(String args[]) {
 		PApplet.main(new String[] { "--bgcolor=#FFFFFF", "propinquity.Propinquity" });
 	}
 }
