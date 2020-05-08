 package fr.odai.zerozeroduck.model;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 import fr.odai.zerozeroduck.controller.MainController;
 import fr.odai.zerozeroduck.utils.StageInfo;
 
 
 public class World {
 	/** Bump per minutes */
 	public static final int BPM = 85;
 	
 	public enum State {
 		WAVE_IN_PROGRESS, // Not available yet, waiting for RELOAD_TIME seconds
 		WAITING
 	}
 	
 	TextureAtlas atlas;
 		
 	/** Traps **/
 	Array<Trap> traps = new Array<Trap>();
 	/** Startpoints for the units **/
 	Array<Vector2> startpoints = new Array<Vector2>();
 	/** Units in this world **/
 	Array<Unit> units = new Array<Unit>();
 	
 	Array<Patate> wavePatates = new Array<Patate>();	
 	Array<Carrot> waveCarrots = new Array<Carrot>();
 
 	private ArrayList<ArrayList<Float>> floor_pos = new ArrayList<ArrayList<Float>>();
 
 	/** Our player controlled hero **/
 	Duck duck;
 	
 	
 	State state = State.WAITING;
 	
 	int moyPatatesByWave = 3;
 	int patatesByWaveDelta = 1;
 	
 	int moyCarrotsByWave = 0;
 	int carrotsByWaveDelta = 2;
 	
 	static final float waveWaitDelta = 2;
 	static final float waveWaitDuration = 3;
 	static final float inWaveWaitDuration = 0.5f;
 	static final float inWaveWaitDelta = 0.2f;
 	
 	int poolPatates = 20;
 	int poolCarrots = 10;
 	float waveWaitEnd = 3;
 	float inWaveWaitEnd = 0.5f;
 	float stateTime = 0;
 	float inWaveTime = 5;
 	
 	int score = 0;
 
 	// Getters -----------
 	public Duck getDuck() {
 		return duck;
 	}
 	public Array<Trap> getTraps() {
 		return traps;
 	}
 	public Array<Unit> getUnits() {
 		return units;
 	}
 	public int getScore() {
 		return score;
 	}	
 	public void setScore(int score){
 		this.score=score;
 	}
 	public int getTotalPool(){
 		return poolPatates + poolCarrots;
 	}
 	// --------------------
 
 	public TextureAtlas getAtlas() {
 		return atlas;
 	}
 	public ArrayList<ArrayList<Float>> getFloorPos() {
 		return floor_pos;
 	}
 	public void setFloorPos(ArrayList<ArrayList<Float>> floor_pos) {
 		this.floor_pos = floor_pos;
 	}
 	public void setFloorPos(int number, ArrayList<Float> floor_pos) {
 		this.floor_pos.set(number, floor_pos);
 	}
 	
 	public World(StageInfo sgi) {
 		createWorld(sgi);
 	}
 	
 	public void setState(State state) {
 		this.state = state;
 		stateTime = 0;
 	}
 
 	private void createWorld(StageInfo sgi) {
 		atlas = new TextureAtlas(Gdx.files.internal("images/textures.pack"));
 
 		floor_pos = new ArrayList<ArrayList<Float>>(sgi.floor_paths.size());
 		
 		for(int level = 0; level < sgi.floor_paths.size(); level++) {
 			Pixmap floor_pixmap = new Pixmap(Gdx.files.internal(sgi.floor_paths.get(level)));
 			int width = floor_pixmap.getWidth();
 			floor_pos.add(new ArrayList<Float>(width));
 			for(int i = 0; i < width; i++) {
 				int j = 0;
 				Color color = new Color();
 				for(; j < floor_pixmap.getHeight(); j++) {
 					Color.rgba8888ToColor(color, floor_pixmap.getPixel(i, j));
 					if(color.r > 0.8f && color.g > 0.8f && color.b > 0.8f) {
 						break;
 					}
 				}
 				getFloorPos().get(level).add(7.f - j / (float) floor_pixmap.getHeight() * 7.f);
 			}
 			floor_pixmap.dispose();
 			startpoints.add(new Vector2(sgi.starting_points.get(level), getFloorHeight(sgi.starting_points.get(level), level)));
 		}
 		
 		duck = new Duck(new Vector2(sgi.duck_x, getFloorHeight(sgi.duck_x, sgi.duck_level) - 0.4f), this);
 		
 		MainController.Keys[] allKeys = new MainController.Keys[4];
 		allKeys[0] = MainController.Keys.TRAP_S;
 		allKeys[1] = MainController.Keys.TRAP_F;
 		allKeys[2] = MainController.Keys.TRAP_H;
 		allKeys[3] = MainController.Keys.TRAP_K;
 		int lastKeyIndex = 0;
 		
 		for(StageInfo.TrapsInfo tpi: sgi.traps) {
 			if(tpi.type.equals("bruleur")) {
 				Bruleur br = new Bruleur(new Vector2(tpi.x, getFloorHeight(tpi.x, tpi.level)), tpi.level);
 				br.setAssociatedKey(allKeys[lastKeyIndex]);
 				traps.add(br);
 			} else if(tpi.type.equals("salt")) {
 				SaltBarrel sb = new SaltBarrel(new Vector2(tpi.x, getFloorHeight(tpi.x, tpi.level)), tpi.level);
 				sb.setAssociatedKey(allKeys[lastKeyIndex]);
 				traps.add(sb);
 			} else if(tpi.type.equals("pepper")) {
 				PepperBarrel pb = new PepperBarrel(new Vector2(tpi.x, getFloorHeight(tpi.x, tpi.level)), tpi.level);
 				pb.setAssociatedKey(allKeys[lastKeyIndex]);
 				traps.add(pb);
 			}
 			lastKeyIndex++;
 		}
 		
 		startpoints.add(new Vector2(-2, getFloorHeight(0)));
 		
 		this.poolPatates = sgi.poolPatates;
 		this.moyPatatesByWave = sgi.moyPatatesByWave;
 		this.patatesByWaveDelta = sgi.patatesByWaveDelta;
 		
 		this.poolCarrots = sgi.poolCarrots;
 		this.moyCarrotsByWave = sgi.moyCarrotsByWave;
 		this.carrotsByWaveDelta = sgi.carrotsByWaveDelta;
 	}
 	
 	public float getFloorHeight(float x) {
 		return getFloorHeight(x, 0);
 	}
 	
 	public float getFloorHeight(float x, int level) {
 		int index = Math.round(x / 10 * getFloorPos().size());
 		if(index >= getFloorPos().size())
 			return getFloorPos().get(level).get(getFloorPos().size() - 1);
 		else if(index < 0)
 			return getFloorPos().get(level).get(0);
 		return getFloorPos().get(level).get(index);
 	}
 	
 	public void update(float delta) {
 		stateTime += delta;
 		inWaveTime += delta;
 
 		//System.out.println("pat :"+poolPatates + " car :"+poolCarrots +" size :" +units.size);
 		for(Unit unit: units) {
 			if(unit.position.x > 10 || unit.position.x < -2) {
 				units.removeValue(unit, true);
 			}
 		}
 		
 		if(state == State.WAITING && stateTime > waveWaitEnd){
 			if(poolPatates > 0){
 				setState(State.WAVE_IN_PROGRESS);
 				
 				// Patates
 				int nbPatates = moyPatatesByWave;
 				nbPatates += Math.random() * 2 * patatesByWaveDelta - patatesByWaveDelta;
 				nbPatates = Math.min(nbPatates, poolPatates);
 				poolPatates -= nbPatates;
 				for (int i = 0; i < nbPatates; i++) {
					wavePatates.add(new Patate(startpoints.get(0).cpy(), (int) Math.floor(Math.random() * startpoints.size), this, atlas));
 				}
 			}
 			
 			if(poolCarrots > 0) {
 				// Carrots
 				int nbCarrots = moyCarrotsByWave;
 				nbCarrots += Math.random() * 2 * carrotsByWaveDelta - carrotsByWaveDelta;
 				nbCarrots = Math.min(nbCarrots, poolCarrots);
 				poolCarrots -= nbCarrots;
 				for (int i = 0; i < nbCarrots; i++) {
					waveCarrots.add(new Carrot(startpoints.get(0).cpy(), (int) Math.floor(Math.random() * startpoints.size), this, atlas));
 				}
 			}
 		}
 		
 		if(state == State.WAVE_IN_PROGRESS && inWaveTime > inWaveWaitEnd){
 			if(wavePatates.size != 0 || waveCarrots.size != 0) {
 				if(wavePatates.size != 0 && waveCarrots.size == 0) {
 					units.add(wavePatates.pop());
 				} else if(wavePatates.size == 0 && waveCarrots.size != 0) {
 					units.add(waveCarrots.pop());
 				} else {
 					double rd = Math.random() * 10;
 					if(rd > 7) {
 						units.add(waveCarrots.pop());
 					} else {
 						units.add(wavePatates.pop());
 					}
 				}
 				
 				inWaveWaitEnd = inWaveWaitDuration;
 				inWaveWaitEnd += Math.random()*2*inWaveWaitDelta - inWaveWaitDelta;
 				inWaveTime = 0;
 			} else {
 				setState(State.WAITING);
 				waveWaitEnd = waveWaitDuration;
 				waveWaitEnd += Math.random()*2*waveWaitDelta - waveWaitDelta;
 			}
 		}
 	}
 }
