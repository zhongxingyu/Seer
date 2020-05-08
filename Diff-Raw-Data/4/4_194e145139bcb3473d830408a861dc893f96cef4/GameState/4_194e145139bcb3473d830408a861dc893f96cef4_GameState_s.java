 package pro.oneredpixel.deflektorclassic;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.TimeUtils;
 
 public class GameState extends State {
 
 	GameState(Deflektor defl) {
 		super(defl);
 		// TODO Auto-generated constructor stub
 	}
 
 	final int GAMESTATE_ACCUMULATING_ENERGY =0;
 	final int GAMESTATE_GAMING = 1;
 	final int GAMESTATE_CALCULATING_ENERGY = 2;
 	final int GAMESTATE_OVERHEAT = 3;
 	final int GAMESTATE_LEVELCOMPLETED = 4;
 	int gameStateId = GAMESTATE_ACCUMULATING_ENERGY;
 	
 	final int BEAMSTATE_NORMAL = 0;
 	final int BEAMSTATE_OVERHEAT = 1;
 	final int BEAMSTATE_BOMB = 2;
 	final int BEAMSTATE_CONNECTED = 3;
 	int beamState;
 	int prevBeamState;
 	
 	int energy=0;
 	int energySteps = 1024;
 	int overheat=0;
 	final int overheatSteps = 1024;
 	
 	boolean cursorEnabled = false;
 	int cursorX = 0;
 	int cursorY = 0;
 	
 	int playingLevel = 0;
 	
 	void create() {
 		
 	};
 	
 	void destroy() {
 		
 	};
 	
 	//init state for showing
 	void start() {
 		initGame();
 	};
 	
 	//state stoped
 	void stop() {
 		
 	};
 	
 	public void render(SpriteBatch batch) {
 		//game
 		prevBeamState = beamState;
 		
 		batch.setProjectionMatrix(app.camera.combined);
 		batch.begin();
 		drawField();
 		drawGameInfo();
 		batch.end();
 		
 		// check if we need to create a new raindrop
 		if(TimeUtils.nanoTime() - app.lastFrameTime > 100000000) {
 			animateField();
 		} else return;
 		app.lastFrameTime = TimeUtils.nanoTime();
 			
 		switch (beamState) {
 		case BEAMSTATE_NORMAL:
 			app.laserOverheatSound.stop();
 			app.burnBombSound.stop();
 			break;
 		case BEAMSTATE_OVERHEAT:
 			if (beamState!=prevBeamState) {
 				app.burnBombSound.stop();
 				app.laserOverheatSound.loop();
 			}
 			break;
 		case BEAMSTATE_BOMB:
 			if (beamState!=prevBeamState) {
 				app.laserOverheatSound.stop();
 				app.burnBombSound.loop();
 			}
 			break;
 		};
 		// process user input
 		//if(Gdx.input.isTouched()) {
 		//	//Vector3 touchPos = new Vector3();
 		//	//touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
 		//	//camera.unproject(touchPos);
 		//	int x = Gdx.input.getX()-winX;
 		//	int y = Gdx.input.getY()-winY;
 		//	if (x>=0 && x<winWidth && y>=0 && y<winHeight)
 		//		touch(x/(sprSize*2)/sprScale, y/(sprSize*2)/sprScale);
 		//}
 		
 		if(Gdx.input.isKeyPressed(Keys.BACK)) app.gotoAppState(Deflektor.APPSTATE_MENU);
 	};
 	
 	
 	//------
 	//--- controlling
 	//------
 	public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer) {
 		return false;
 	}
 
 	public boolean fling(float arg0, float arg1, int arg2) {
 		return false;
 	}
 
 	public boolean longPress(float arg0, float arg1) {
 		return false;
 	}
 
 	public boolean tap(float x, float y, int tapCount, int button) {
 		setCursor((int)x,(int)y);
 		x=x-app.winX;
 		y=y-app.winY;
 		if (x>=0 && x<app.winWidth && y>=0 && y<app.winHeight)
 			touch(((int)x)/(app.sprSize*2)/app.sprScale, ((int)y)/(app.sprSize*2)/app.sprScale);
 		return false;
 	}
 
 	float touchX=0;
 	float touchY=0;
 	int restDelta = 0;
 	
 	public boolean touchDown(float x, float y, int pointer, int button) {
 		touchX = x;
 		touchY = y;
 		restDelta = 0;
 		setCursor((int)x,(int)y);
 		return false;
 	} 
 	
 	void setCursor(int x, int y) {
 		int newCursorX=(x-app.winX)/(app.sprSize*2)/app.sprScale;
 		int newCursorY=(y-app.winY)/(app.sprSize*2)/app.sprScale;	
 		
 		if (newCursorX>=0 && newCursorX<field_width && newCursorY>=0 && newCursorY<field_height) {
 			int f=field[newCursorX+newCursorY*field_width];
 			if ((f&0xF00)==FLD_MIRROR) {
 				cursorEnabled=true;
 				cursorX=newCursorX;
 				cursorY=newCursorY;
 			};
 		};
 	}
 
 	public boolean pan(float x, float y, float deltaX, float deltaY) {
 		if (cursorEnabled) {
 			int delta=(int)Math.sqrt((deltaX)*(deltaX)+(deltaY)*(deltaY));
 			if (deltaX<(-deltaY)) delta=-delta;
 			delta = delta + restDelta;
 			rotateMirror( cursorX, cursorY, (delta/app.panScale)&0x1f);
 			restDelta=delta-((int)(delta/app.panScale))*app.panScale;
 		};
 		return false;
 	}
 	
 	public boolean zoom(float arg0, float arg1) {
 		return false;
 	}
 	
 	
 	/// game ///
 	public final static int field_width = 15;
 	public final static int field_height = 9;
 	int field[];
 	
 	final int FLD_NULL = 0;
 	final int FLD_LASER_GUN = 0x0100;
 	final int FLD_RECEIVER = 0x0200;
 	final int FLD_MIRROR = 0x0300;
 	final int FLD_WARPBOX = 0x0400;			//(teleport) warpbox will connect you with next warpbox
 	final int FLD_CELL = 0x500;
 	final int FLD_MINE = 0x600;				//Mine causes overload and increases overload meter
 	final int FLD_WALL_A = 0x700;			//reflects laser
 	final int FLD_WALL_B = 0x800;			//stops laser
 	final int FLD_PRISM = 0x900;			//prism turns laser at random
 	final int FLD_SLIT_A = 0xa00;			//reflects laser if angle is different - 
 	final int FLD_SLIT_B = 0xb00;			//if the angle is different the laser will stop - 
 	final int FLD_EXPLODE = 0xc00;			// CELL/WALL/MINE  -  
 	
 	final int FLD_AUTOROTATING = 0x2000;	//autorotate
 	final int FLD_EXPLODEONEND = 0x4000;		//kill this brick when all cells burned off
 	
 	int packedLevels[][] = {
 		//01
 		{
 			1024, //energySteps
 			FLD_MIRROR|6,FLD_NULL|3,FLD_WALL_B|0x0a,FLD_CELL,FLD_CELL,FLD_WALL_A|0x05,FLD_MIRROR,FLD_NULL|1, 
 				FLD_SLIT_B|7|FLD_AUTOROTATING,FLD_CELL,FLD_CELL,FLD_WALL_A|0x05,FLD_WALL_A|0x0b,
 			FLD_NULL|1,FLD_CELL,FLD_CELL,FLD_CELL,FLD_WALL_B|0x0a,FLD_CELL,FLD_CELL,FLD_WALL_A|0x05,
 				FLD_NULL|3,FLD_CELL,FLD_NULL|2,FLD_WALL_A|0x05,
 			FLD_NULL|1,FLD_CELL,FLD_MIRROR|FLD_AUTOROTATING,FLD_CELL,FLD_WALL_B|0x0f,FLD_PRISM,FLD_WALL_A|0x0c,
 				FLD_WALL_A|0x0c,FLD_NULL|4,FLD_SLIT_B|7,FLD_NULL|2,
 			FLD_NULL|1,FLD_CELL,FLD_CELL,FLD_CELL,FLD_WALL_B|0x0f,FLD_NULL|5,FLD_WARPBOX|1,FLD_NULL|3,FLD_MIRROR,
 			FLD_NULL|1,FLD_WALL_B|0x0c,FLD_WALL_B|0x0c,FLD_WALL_B|0x0c,FLD_WALL_B|0x0f,FLD_NULL|2,
 				FLD_SLIT_A|4|FLD_AUTOROTATING,FLD_NULL|1,FLD_CELL,FLD_NULL|5,
 			FLD_NULL|2,FLD_MIRROR,FLD_NULL|11,FLD_MIRROR,
 			FLD_NULL|11,FLD_MINE,FLD_SLIT_B|7, FLD_NULL|2,
 			FLD_MIRROR|12,FLD_NULL|2,FLD_LASER_GUN|3,FLD_WALL_A|0x05,FLD_NULL|4,FLD_WALL_A|0x0d,FLD_CELL,
 				FLD_MINE,FLD_CELL,FLD_NULL|1,FLD_WARPBOX|1,
 			FLD_NULL|2,FLD_WALL_A|0x0a,FLD_RECEIVER|1,FLD_WALL_A|0x05|FLD_EXPLODEONEND,FLD_NULL|3,FLD_MIRROR,
 				FLD_WALL_A|0x05,FLD_CELL,FLD_NULL|1,FLD_WALL_A|0x03,FLD_WALL_A|0x03,FLD_WALL_A|0x02,
 
 		},
 		//02
 		{
 			1024, //energySteps
 			FLD_MIRROR,FLD_CELL,FLD_MINE,FLD_NULL,FLD_CELL,FLD_WALL_A|4,FLD_NULL|3,FLD_CELL,FLD_WALL_A|1,FLD_NULL,FLD_MINE,FLD_NULL,FLD_RECEIVER|2,
 			FLD_NULL,FLD_WALL_A|1,FLD_WALL_A|4,FLD_CELL,FLD_WALL_A|8,FLD_WALL_A|4,FLD_WALL_A|9,FLD_NULL,FLD_MIRROR,FLD_NULL|3,FLD_WALL_A|2,FLD_CELL,FLD_WALL_A|3|FLD_EXPLODEONEND,
 			FLD_WALL_A|8,FLD_NULL,FLD_WALL_A|4,FLD_WALL_A|8,FLD_MINE,FLD_NULL,FLD_WALL_A|2,FLD_WALL_A|2,FLD_NULL,FLD_WALL_A|2,FLD_WALL_A|8,FLD_MINE,FLD_WALL_A|1,FLD_NULL|2,
 			FLD_NULL,FLD_MIRROR,FLD_WALL_A|1,FLD_NULL,FLD_WALL_A|8,FLD_WALL_A|3,FLD_NULL,FLD_MINE,FLD_NULL|2,FLD_CELL,FLD_WALL_A|8,FLD_CELL,FLD_WALL_A|9,FLD_NULL,
 			FLD_MINE,FLD_WALL_A|2,FLD_CELL,FLD_NULL,FLD_WALL_A|2,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_WALL_A|8,FLD_NULL,FLD_WALL_A|9,FLD_NULL|2,FLD_MINE,FLD_NULL,
 			FLD_NULL|2,FLD_WALL_A|2,FLD_NULL,FLD_WALL_A|4,FLD_NULL|2,FLD_WALL_A|6,FLD_NULL,FLD_CELL,FLD_NULL,FLD_WALL_A|1,FLD_WALL_A|4,FLD_WALL_A|4,FLD_WALL_A|5,
 			FLD_LASER_GUN|2,FLD_CELL,FLD_NULL|2,FLD_MIRROR,FLD_NULL,FLD_WALL_A|2,FLD_NULL,FLD_WALL_A|4,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_WALL_A|4,FLD_NULL|2,
 			FLD_NULL,FLD_WALL_A|8,FLD_WALL_A|6,FLD_MINE,FLD_NULL,FLD_WALL_A|1,FLD_CELL,FLD_WALL_A|6,FLD_NULL|2,FLD_WALL_A|2,FLD_WALL_A|4,FLD_MINE,FLD_WALL_A|2,FLD_MIRROR,
 			FLD_MIRROR,FLD_NULL,FLD_WALL_A|1,FLD_NULL,FLD_WALL_A|4,FLD_NULL,FLD_WALL_A|1,FLD_NULL,FLD_MIRROR,FLD_NULL|2,FLD_WALL_A|9,FLD_WALL_A|1,FLD_NULL,FLD_CELL,
 		},
 		//03
 		{
 			1024, //energySteps
 			FLD_CELL,FLD_CELL,FLD_WALL_A|11,FLD_NULL|3,FLD_MIRROR,FLD_NULL,FLD_CELL,FLD_WALL_A|5,FLD_NULL|2,FLD_CELL,FLD_NULL,FLD_MIRROR,
 			FLD_CELL,FLD_CELL,FLD_WALL_A|4,FLD_WALL_A|13,FLD_NULL|5,FLD_WALL_A|5,FLD_NULL|3,FLD_WALL_A|3,FLD_NULL,
 			FLD_CELL,FLD_CELL,FLD_PRISM,FLD_NULL,FLD_MIRROR,FLD_NULL|3,FLD_WALL_A|3,FLD_WALL_A|15,FLD_WALL_A|10,FLD_NULL|2,FLD_WALL_A|10,FLD_SLIT_A,
 			FLD_CELL,FLD_CELL,FLD_WALL_A|1,FLD_WALL_A|7,FLD_NULL|9,FLD_WALL_A|11,FLD_CELL,
 			FLD_CELL,FLD_CELL,FLD_WALL_A|14,FLD_NULL|4,FLD_WALL_A|1,FLD_WALL_A|3,FLD_WALL_A|2,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_WALL_A|4,FLD_WALL_A|12,
 			FLD_WALL_A|12,FLD_WALL_A|12,FLD_WALL_A|8,FLD_NULL,FLD_WALL_A|15,FLD_WALL_A|10,FLD_PRISM,FLD_WALL_B|5|FLD_EXPLODEONEND,FLD_RECEIVER|3,FLD_WALL_A|14,FLD_WALL_A|12,FLD_NULL,FLD_SLIT_A,FLD_NULL|2,
 			FLD_WALL_A|2,FLD_CELL,FLD_NULL|5,FLD_WALL_A|4,FLD_WALL_A|12,FLD_WALL_A|10,FLD_MIRROR,FLD_NULL,FLD_WALL_A|3,FLD_WALL_A|7,FLD_NULL,
 			FLD_WALL_A|10,FLD_NULL|5,FLD_SLIT_A,FLD_NULL|2,FLD_WALL_A|13,FLD_NULL|2,FLD_WALL_A|12,FLD_WALL_A|12,FLD_CELL,
 			FLD_WALL_A|11,FLD_WALL_A|3,FLD_NULL,FLD_MIRROR,FLD_NULL|4,FLD_CELL,FLD_WALL_A|5,FLD_LASER_GUN|0,FLD_WALL_A|11,FLD_WALL_A|3,FLD_NULL|2,
 			
 		},
 		//04
 		{
 			1024, //energySteps
 			FLD_MINE,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_CELL,FLD_CELL,FLD_CELL,FLD_MINE,
 			FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,
 			FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_MIRROR,FLD_CELL,
 			FLD_CELL,FLD_CELL, FLD_MIRROR, FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,
 			FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,
 			FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_MINE,FLD_CELL,FLD_CELL,FLD_MINE,
 			FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_MIRROR,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,
 			FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_MINE,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,
 			FLD_RECEIVER|1,FLD_WALL_B|0x05|FLD_EXPLODEONEND,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_LASER_GUN|3,
 			
 		},
 		//05
 		{
 			1024, //energySteps
 			//FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,
 			//FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,
 			FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_RECEIVER|2,
 			FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_MINE|FLD_EXPLODEONEND,
 			FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_MINE,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,
 			FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_NULL,
 			FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,
 			FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,
 			FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,
 			FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,
 			FLD_LASER_GUN,FLD_NULL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_MINE,FLD_MIRROR,FLD_NULL,FLD_MIRROR,FLD_CELL,FLD_MIRROR,FLD_NULL,FLD_MIRROR,
 			
 		},
 	};
 
 	
 
 	
 	
 	//  
 	int angleNodeSteps[][]={
 		{0,-2}, //0
 		{1,-2}, //1
 		{2,-2}, //2
 		{2,-1}, //3
 		{2,0},  //4
 		{2,1}, //5
 		{2,2}, //6
 		{1,2}, //7
 		{0,2}, //8
 		{-1,2}, //9
 		{-2,2}, //10
 		{-2,1}, //11
 		{-2,0}, //12
 		{-2,-1},//13
 		{-2,-2},//14
 		{-1,-2},//15
 	};
 	
 	public void initGame() {
 		
 		beamState = BEAMSTATE_NORMAL;
 		gameStateId = GAMESTATE_ACCUMULATING_ENERGY;
 		energy=0;
 		overheat=0;
 		cursorEnabled = false;		
 		
 		field=new int[field_width*field_height];
 		
 		unpackLevel(playingLevel);
 	}
 	
 	void unpackLevel(int levelNumber) {
 		int piece;
 		int fieldIndex=0;
 		if (levelNumber>=packedLevels.length) return;
 		
 		energySteps = packedLevels[levelNumber][0];
 		
 		for (int i=1;i<packedLevels[levelNumber].length;i++) {
 			piece = packedLevels[levelNumber][i];
 			if ((piece&0xFF00)==FLD_NULL) {
 				piece&=0xFF;
 				if (piece<1) piece=1;
 				for (int n=0;n<piece;n++) {
 					field[fieldIndex++]=FLD_NULL;
 					if (fieldIndex>=field_width*field_height) return;
 				}
 			} else {
 				field[fieldIndex++] = piece;
 				if (fieldIndex>=field_width*field_height) return;
 			}
 		}
 		while (fieldIndex<field_width*field_height)
 			field[fieldIndex++]=FLD_NULL;
 		
 	}
 	
 	void animateField() {
 		int f=0;
 		boolean needToExplodeBarrier=true;
 		boolean barrierFound = false;
     
 		
 		switch (gameStateId) {
 		case GAMESTATE_ACCUMULATING_ENERGY:
 			if (energy==0) app.laserFillInSound.loop();
 			energy+=energySteps/40;
 			if (energy>=energySteps-1) {
 				energy=energySteps-1;
 				app.laserFillInSound.stop();
 				gameStateId=GAMESTATE_GAMING;
 			}
 
 			break;
 		case GAMESTATE_GAMING:
 			energy++;	//TODO:  -  :)
 			energy--;
 			if (energy<=0) app.gotoAppState(Deflektor.APPSTATE_MENU);
 			
 			if (beamState==BEAMSTATE_OVERHEAT) overheat+=overheatSteps/128;
 			else if (beamState==BEAMSTATE_BOMB) overheat+=overheatSteps/20;
 			else overheat-=overheatSteps/128;
 			
 			if (beamState==BEAMSTATE_CONNECTED) {
 				gameStateId = GAMESTATE_LEVELCOMPLETED;
 				app.levelCompletedSound.play();
 			}
 			
 			if (overheat <=0) overheat =0;
 			if (overheat>=overheatSteps) {
 				overheat=0; //TODO:  -  :)
 				//TODO:       :)
 				//app.laserOverheatSound.stop();
 				//app.gotoAppState(Deflektor.APPSTATE_MENU);
 			}
 			
 			
 			break;
 		case GAMESTATE_CALCULATING_ENERGY:
 			break;
 		case GAMESTATE_OVERHEAT:
 			break;
 		case GAMESTATE_LEVELCOMPLETED:
 			if (playingLevel++<4) initGame();
 			else app.gotoAppState(Deflektor.APPSTATE_MENU);
 			break;
 		};
 
 		for (int i=0;i<field_width;i++) 
 			for (int j=0;j<field_height;j++) {
 				f=field[j*field_width+i];
 				if ((f&FLD_AUTOROTATING)!=0) rotateThing(i,j);
 				if ((f&FLD_EXPLODEONEND)!=0) barrierFound = true;
 				if ((f&0xf00)==FLD_EXPLODE) {
 					f++;
 					if ((f&0xf)>4) f=FLD_NULL;
 					else needToExplodeBarrier = false;
 					field[j*field_width+i]=f;
 				};
 				if ((f&0xF00)==FLD_CELL) needToExplodeBarrier = false;
 			};
 		if (needToExplodeBarrier && barrierFound) {
 			app.exitOpenSound.play();
 			for (int i=0;i<field_width*field_height;i++)
 				if ((field[i]&FLD_EXPLODEONEND)!=0) field[i]=FLD_EXPLODE;
 		};
 		
 	}
 	
 	void rotateThing(int x, int y) {
 		rotateThing(x,y,1);
 		//int f=field[y*field_width+x];
 		//field[y*field_width+x]=(++f)&0xFFFFFF1F;
 	}
 	
 	void rotateThing(int x, int y, int angle) {
 		int f=field[y*field_width+x];
 		field[y*field_width+x]=(f+angle)&0xFFFFFF1F;
 	}
 	
 	void rotateMirror(int x, int y, int angle) {
 		if (x>=field_width || y>=field_height) return;
 		if ((field[y*field_width+x]&0xFF00)==FLD_MIRROR)
 			rotateThing(x,y,angle);
 	}
 
 	void drawGameInfo () {
 		int nrg = (energy *64) /energySteps;
 		int ovh = (overheat * 64) / overheatSteps;
 		if (nrg>63) nrg=63;
 		if (nrg<0) nrg=0;
 		if (ovh>63) ovh=63;
 		if (ovh<0) ovh=0;
 		app.menu_putRegion( 28, field_height*16, 64, 8, 0, 8);
 		if (nrg>0) app.menu_putRegion( 28, field_height*16+8, nrg, 8, 0, 0);
 		if (nrg<63) app.menu_putRegion( 28+nrg, field_height*16+8, 64-nrg, 8, 64+nrg, 0);
 		app.menu_putRegion( 100, field_height*16, 64, 8, 64, 8);
 		if (ovh>0) app.menu_putRegion( 100, field_height*16+8, ovh, 8, 0, 0);
 		if (ovh<63) app.menu_putRegion( 100+ovh, field_height*16+8, 64-ovh, 8, 64+ovh, 0);
 		app.menu_putRegion( 172, field_height*16, 48, 16, 0, 16);
 		
 		
 		//level
 		app.menu_putRegion( 0, field_height*16, 16, 16, 64, 16);
 		//pause button
 		app.menu_putRegion( (field_width-1)*16, field_height*16, 16, 16, 48, 16);
 	}
 	
 	void drawField() {
 		int f_angle;
 		int beam_x=0;
 		int beam_y=0;
 		int beam_angle=0;
 
 		//clear field by null-sprite;
 		for (int i=0;i<field_width;i++) 
 			for (int j=0;j<field_height;j++) {
 				int f=field[j*field_width+i];
 				f_angle = f&0x1f;
 				if ((f&0xf00)==FLD_LASER_GUN) {
 					beam_x=i*4+2+angleNodeSteps[f_angle*4][0];
 					beam_y=j*4+2+angleNodeSteps[f_angle*4][1];
 					beam_angle=(f_angle&3)*4;					
 				} else if ((f&0xf00)==FLD_RECEIVER) {
 					
 				};
 				//TODO:   1 
 				app.spr_putRegion( i*16, j*16, 8, 8, 7*16, 5*16+8);
 				app.spr_putRegion( i*16+8, j*16, 8, 8, 7*16, 5*16+8);
 				app.spr_putRegion( i*16, j*16+8, 8, 8, 7*16, 5*16+8);
 				app.spr_putRegion( i*16+8, j*16+8, 8, 8, 7*16, 5*16+8);
 			};
 		
 		drawBeam(beam_x,beam_y,beam_angle);
 			
 		for (int i=0;i<field_width;i++) 
 			for (int j=0;j<field_height;j++) {
 				f_angle=field[j*field_width+i]&0x1f;
 				switch (field[j*field_width+i]&0xf00) {
 				case FLD_NULL:
 					break;
 				case FLD_LASER_GUN:
 					app.spr_putRegion( i*16, j*16, 16, 16, ((f_angle&3)*16), 4*16);
 					break;
 				case FLD_RECEIVER:
 					putReceiver(i,j,f_angle);
 					break;
 				case FLD_MIRROR:
 					putMirror(i,j,f_angle);
 					break;
 				case FLD_WARPBOX:
 					putWarpbox(i,j,f_angle);
 					break;
 				case FLD_CELL:
 					putCell(i,j);
 					break;
 				case FLD_MINE:
 					putMine(i,j);
 					break;
 				case FLD_WALL_A:
 					putWallA(i,j,f_angle);
 					break;
 				case FLD_WALL_B:
 					putWallB(i,j,f_angle);
 					break;
 				case FLD_PRISM:
 					putPrism(i,j);
 					break;
 				case FLD_SLIT_A:
 					putSlitA(i,j,f_angle);
 					break;
 				case FLD_SLIT_B:
 					putSlitB(i,j,f_angle);
 					break;
 				case FLD_EXPLODE:
 					app.spr_putRegion( i*16, j*16, 16, 16, ((f_angle&7)*16), 6*16);
 					break;
 				}
 			};
 			
 			
 			
 		if (cursorEnabled) {
 			app.spr_putRegion( cursorX*16, cursorY*16, 16, 16, 0*16, 5*16);
 		};
 			
 	};
 
 	//angle=0..31
 	void putMirror(int x, int y, int angle) {
 		app.spr_putRegion( x*16, y*16, 16, 16, (angle&7)*16, ((angle>>3)&1)*16);
 	}
 	
 	//angle=0..3
 	void putLaser(int x,int y, int angle) {
 
 	}
 	
 	void putReceiver(int x,int y, int angle) {
 		app.spr_putRegion( x*16, y*16, 16, 16, (((angle&3)+4)*16), 4*16);
 	}
 	
 	void putCell(int x, int y) {
 		app.spr_putRegion( x*16, y*16, 16, 16, 0, 5*16);
 	}
 	
 	void putMine(int x, int y) {
 		app.spr_putRegion( x*16, y*16, 16, 16, 16, 5*16);
 	}
 	
 	void putPrism(int x, int y) {
 		app.spr_putRegion( x*16, y*16, 16, 16, 6*16, 5*16);
 	}
 	
 	void putWarpbox(int x, int y, int type) {
 		app.spr_putRegion( x*16, y*16, 16, 16, (((type&3)+2)*16), 5*16);
 	}
 	
 	void putSlitA(int x, int y, int angle) {
 		app.spr_putRegion( x*16, y*16, 16, 16, ((angle&7)*16), 3*16);
 	}
 
 	void putSlitB(int x, int y, int angle) {
 		app.spr_putRegion( x*16, y*16, 16, 16, ((angle&7)*16), 2*16);
 	}
 	
 	void putWallA(int x, int y, int type) {
 		if ((type&8)!=0)	app.spr_putRegion( x*16, y*16, 8, 8, 7*16+8, 5*16);
 		if ((type&4)!=0)	app.spr_putRegion( x*16+8, y*16, 8, 8, 7*16+8, 5*16);
 		if ((type&2)!=0)	app.spr_putRegion( x*16, y*16+8, 8, 8, 7*16+8, 5*16);
 		if ((type&1)!=0)	app.spr_putRegion( x*16+8, y*16+8, 8, 8, 7*16+8, 5*16);
 	}
 	
 	void putWallB(int x, int y, int type) {
 		if ((type&8)!=0)	app.spr_putRegion( x*16, y*16, 8, 8, 7*16, 5*16);
 		if ((type&4)!=0)	app.spr_putRegion( x*16+8, y*16, 8, 8, 7*16, 5*16);
 		if ((type&2)!=0)	app.spr_putRegion( x*16, y*16+8, 8, 8, 7*16, 5*16);
 		if ((type&1)!=0)	app.spr_putRegion( x*16+8, y*16+8, 8, 8, 7*16, 5*16);
 	}
 	
 	void drawBeam(int beamX, int beamY, int beamAngle) {
 		
 		int newBeamX;
 		int newBeamY;
 		int oldBeamAngle;
 		boolean endBeam=false;
 		
 		beamState = BEAMSTATE_NORMAL;
 		
 		while (!endBeam) {
 
 			newBeamX = beamX+angleNodeSteps[beamAngle][0];
 			newBeamY = beamY+angleNodeSteps[beamAngle][1];
 			oldBeamAngle = beamAngle;
 			if (newBeamX>field_width*4 || newBeamX<0 || newBeamY>field_height*4 || newBeamY<0) {
 				endBeam=true;
 				continue;
 			};
 			
 			drawSpriteLine(beamX, beamY, newBeamX, newBeamY);
 
 			if (newBeamX>=field_width*4 || newBeamX<0 || newBeamY>=field_height*4 || newBeamY<0) {
 				endBeam=true;
 				continue;
 			};
 			
 			beamX = newBeamX;
 			beamY = newBeamY;
 			
 			int sx=beamX&3;
 			int sy=beamY&3;
 			int fx=beamX/4;
 			int fy=beamY/4;
 			int f=field[fx+fy*field_width];
 			int f_angle=f&0x1f;
 			
 			
 			//      
 			if ((sx==2) && (sy==2)) {
 				switch (f&0x0f00) {
 				case FLD_LASER_GUN:
 					endBeam=true;
 					continue;
 				case FLD_RECEIVER:
 					//if right angle
 					if ((f_angle*4)==((beamAngle+8)&15)) beamState = BEAMSTATE_CONNECTED;
 					endBeam=true;
 					break;
 				case FLD_MIRROR:
 					beamAngle =(((f_angle<<1)-beamAngle-beamAngle)>>1)&0xf;
 					break;
 				case FLD_WARPBOX:
 					for (int i=0;i<field.length;i++) {
 						if ( (field[i]==f) && (i!=(fx+fy*field_width))) {
 							beamY=(i/field_width)*4+2;
 							beamX=(i-(((int)(beamY/4))*field_width))*4+2;
 							break;
 						};
 					};
 					break;
 				case FLD_CELL:
 					field[fx+fy*field_width]=FLD_EXPLODE;
 					app.burnCellSound.play();
 					endBeam=true;
 					continue;
 				case FLD_MINE:
 					beamState = BEAMSTATE_BOMB;
 					endBeam=true;
 					continue;
 				case FLD_PRISM:
					beamAngle= (beamAngle-4+((int)((8*Math.random())+0.5)))&0xf;
 					break;
 				case FLD_EXPLODE:
 					if (f_angle>2) break;
 					endBeam=true;
 					continue;
 				}
 			}
 
 			//      (, ).
 			int mp_beam_x = (beamX+beamX+angleNodeSteps[beamAngle][0])/2;
 			int mp_beam_y = (beamY+beamY+angleNodeSteps[beamAngle][1])/2; 	
 			
 			
 			int f1=field[(mp_beam_x/4)+(mp_beam_y/4)*field_width];
 			switch (f1&0x0F00) {
 			case FLD_WALL_A:
 				int wall_angle=-1;
 				final int wall_a_angle_matrix[]={
 						//x,y;u&3==0 true=1, false=0;walls	u
 						//        
 					-1,	//0,0,0,0000	  
 					2,	//0,0,0,0001	2
 					6,	//0,0,0,0010	6
 					4,	//0,0,0,0011	4
 					6,	//0,0,0,0100	6
 					0,	//0,0,0,0101	0
 					2,	//0,0,0,0110	2
 					2,	//0,0,0,0111	2
 					2,	//0,0,0,1000	2
 					6,	//0,0,0,1001	6
 					0,	//0,0,0,1010	0
 					6,	//0,0,0,1011	6
 					4,	//0,0,0,1100	4
 					6,	//0,0,0,1101	6
 					2,	//0,0,0,1110	2
 					-1,	//0,0,0,1111	  (   )
 					
 						//       
 					-1,	//0,0,1,0000	  
 					-1,	//0,0,1,0001	  
 					-1,	//0,0,1,0010	 
 					4,	//0,0,1,0011	4
 					-1,	//0,0,1,0100	  
 					0,	//0,0,1,0101	0
 					-2,	//0,0,1,0110	 
 					-2,	//0,0,1,0111	 
 					-1,	//0,0,1,1000	  
 					-2,	//0,0,1,1001	 
 					0,	//0,0,1,1010	0
 					-2,	//0,0,1,1011	6
 					4,	//0,0,1,1100	4
 					-2,	//0,0,1,1101	6
 					-2,	//0,0,1,1110	2
 					-1	//0,0,1,1111	  (   )					
 				};
 				int wallsAround=f;
 				int wallUP=0;
 				int wallUPLEFT=0;
 				int wallLEFT=0;
 				
 				//     , -, .
 				if (fx>0) wallLEFT=field[fx-1+fy*field_width];
 				if ((fx>0) && (fy>0)) wallUPLEFT=field[fx-1+(fy-1)*field_width];
 				if (fy>0) wallUP=field[fx+(fy-1)*field_width];
 				
 				if ((wallsAround&0xF00)!=	FLD_WALL_A) wallsAround=0;	else wallsAround&=0xf;
 				if ((wallLEFT&0xF00)!=		FLD_WALL_A) wallLEFT=0;		else wallLEFT&=0xf;
 				if ((wallUPLEFT&0xF00)!=	FLD_WALL_A) wallUPLEFT=0;	else wallUPLEFT&=0xf;
 				if ((wallUP&0xF00)!=		FLD_WALL_A)	wallUP=0;		else wallUP&=0xf;
 				
 				
 				if ((beamY&2)==0) {
 					// :
 					wallsAround = (wallsAround>>2) | ((wallUP<<2)&0xC);
 					wallUP = wallUP>>2;
 					wallLEFT = (wallLEFT>>2) | ((wallUPLEFT<<2)&0xC);
 					wallUPLEFT = wallUPLEFT>>2;
 				};
 				
 				if ((beamX&2)==0) {
 					// :
 					wallsAround = ((wallsAround >>1) &5 ) | ((wallLEFT &5) <<1);
 				};
 				
 				if (((beamX&1)+(beamY&1))==0) {
 					wall_angle=wall_a_angle_matrix[( ((beamAngle&3)==0)?16:0 )|(wallsAround&0xF)];
 					//      
 					if ((beamAngle&3)!=0 && wallsAround!=6 && wallsAround!=9) {
 						// ,           .
 						if ((beamAngle<4) && ((wallsAround&4)==0)) wall_angle=-1;
 						else if ((beamAngle>4) && (beamAngle<8) && ((wallsAround&1)==0)) wall_angle=-1;
 						else if ((beamAngle>8) && (beamAngle<12) && ((wallsAround&2)==0)) wall_angle=-1;
 						else if ((beamAngle>12) && (wallsAround&8)==0) wall_angle=-1;
 					}
 				} else {
 
 				//	1,0,x,x0x0	  
 				//	1,0,x,x0x1	4
 				//	1,0,x,x1x0	4
 				//	1,0,x,x1x1	 	
 				if ((beamX&1)==1) switch (wallsAround&0x5) {
 					case 1: case 4: wall_angle=4; break;
 					case 5: endBeam=true; 
 					};
 					
 				//0,1,x,xx00	  
 				//0,1,x,xx01	0
 				//0,1,x,xx10	0
 				//0,1,x,xx11	 
 				if ((beamY&1)==1) switch (wallsAround&0x3) {
 					case 1: case 2: wall_angle=0; break;
 					case 3: endBeam=true; 
 					};
 					
 				};
 				
 				if (wall_angle>=0) beamAngle=(wall_angle*2-beamAngle)&0xf;
 				else if (wall_angle==-1) break;
 				else if (wall_angle==-2) { beamAngle=(beamAngle+8)&0xf; break; };
 				
 				//continue;
 				break;
 			case FLD_WALL_B:
 				int crd=((mp_beam_x>>1)&1)+((mp_beam_y)&2);
 				if ((crd==0) && ((f1&8)!=0)) {	endBeam=true;	continue; };
 				if ((crd==1) && ((f1&4)!=0)) {	endBeam=true;	continue; };
 				if ((crd==2) && ((f1&2)!=0)) {	endBeam=true;	continue; };
 				if ((crd==3) && ((f1&1)!=0)) {	endBeam=true;	continue; };
 				//(mp_beam_x>1)&1 -    0..1
 				//(mp_beam_y)&2 -    0..1
 				//0,0 && 8
 				//0,1 && 4
 				//1,0 && 2
 				//1,1 && 1
 				break;
 			case FLD_SLIT_A:
 				if ((f1&7)!=(beamAngle&7)) {
 					if ((beamX&3)==0)
 						beamAngle=(0-beamAngle)&0xf;
 					if ((beamY&3)==0)
 						beamAngle=(4*2-beamAngle)&0xf;
 					//continue;
 					break;
 				};
 				break;
 			case FLD_SLIT_B:
 				if ((f1&7)!=(beamAngle&7)) {
 					endBeam=true;
 					//continue;
 					break;
 				};
 				break;
 			}
 			//     
 			if (Math.abs(beamAngle-oldBeamAngle)==8) {
 				endBeam=true;
 				beamState = BEAMSTATE_OVERHEAT;
 			}
 			
 		}
 		
 		
 		
 		
 	};
 	
 	void drawSpriteLine(int x0, int y0, int x1, int y1) {
 		int lx0, lx1, ly0, ly1;
 		if (x0<x1) { lx0=x0; lx1=x1; ly0=y0; ly1=y1;  }
 		else { lx0=x1; lx1=x0; ly0=y1; ly1=y0;  };
 		int sx=lx1-lx0;
 		int sy=ly1-ly0;
 		if (sx==0 && sy==-2)					app.spr_putRegionSafe( lx0*4-4, ly0*4-8, 8, 8, 0, 112);
 		if (sx==1 && sy==-2 && ((lx0&1)==0))	app.spr_putRegionSafe( lx0*4-4, ly0*4-8, 16, 8, 8, 120);
 		if (sx==1 && sy==-2 && ((lx0&1)==1))	app.spr_putRegionSafe( lx0*4-4, ly0*4-8, 16, 8, 12, 112);
 		if (sx==2 && sy==-2)					app.spr_putRegionSafe( lx0*4-4, ly0*4-12, 16, 16, 24, 112);
 		if (sx==2 && sy==-1 && ((ly0&1)==0))	app.spr_putRegionSafe( lx0*4,   ly0*4-4, 16, 8, 40, 120);
 		if (sx==2 && sy==-1 && ((ly0&1)==1))	app.spr_putRegionSafe( lx0*4-8, ly0*4-8, 16, 8, 40, 112);
 		if (sx==2 && sy==0)						app.spr_putRegionSafe( lx0*4,   ly0*4-4, 8, 8, 56, 112);
 		if (sx==2 && sy==1 && ((ly0&1)==0))		app.spr_putRegionSafe( lx0*4,   ly0*4-4, 16, 8, 64, 112);
 		if (sx==2 && sy==1 && ((ly0&1)==1)) 	app.spr_putRegionSafe( lx0*4-8, ly0*4, 16, 8, 64, 120);
 		if (sx==2 && sy==2)						app.spr_putRegionSafe( lx0*4-4, ly0*4-4, 16, 16, 80, 112);
 		if (sx==1 && sy==2 && ((lx0&1)==0))		app.spr_putRegionSafe( lx0*4-4, ly0*4, 16, 8, 96, 112);
 		if (sx==1 && sy==2 && ((lx0&1)==1))		app.spr_putRegionSafe( lx0*4-4, ly0*4, 16, 8, 100, 120);
 		if (sx==0 && sy==2)						app.spr_putRegionSafe( lx0*4-4, ly0*4, 8, 8, 0, 112);
 		
 		
 	}
 	
 	void touch(int x, int y) {
 		if (x<0 || x>= field_width || y< 0 || y>=field_height) return;
 		int f=field[y*field_width+x];
 		if ((f&0xFF00)==FLD_MIRROR) {
 			rotateThing(x,y);
 		};
 	};
 	
 	/*
 	int getMirrorAngle (int x, int y) {
 		if (x<0 || x>= field_width || y< 0 || y>=field_height) return -1;
 		int f=field[y*field_width+x];
 		if ((f&0xFF00)==FLD_MIRROR) {
 			return f&0x1f;
 		};
 		return -1;
 	};
 	
 	void setMirrorAngle (int x, int y, int angle) {
 		if (x<0 || x>= field_width || y< 0 || y>=field_height || angle<0) return;
 		int f=field[y*field_width+x];
 		if ((f&0xFF00)==FLD_MIRROR) {
 			field[y*field_width+x]=(f&0xFFFFFF00)|(angle&0x1f);
 		};
 	};
 	*/
 	
 	
 	
 }
