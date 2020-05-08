 package com.shval.jnpgame;
 
 import static com.shval.jnpgame.Globals.*;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 
 public class BoardConfig {
 
 	int ROWS;
 	int COLS;
 	int LEVELS;
 	int level;
 	static private final String TAG = BoardConfig.class.getSimpleName();
 	private char cells[][];
 	boolean flipped;
 	private static final String SAVE_FILE = "game.save";
 	byte complete[];
 	
 	private String levels[][] = { 
 			{ // level 0 - dev playground\
 
 			"xxxxxxxxxxxxxx",
 			"x   G4brg  rRx",
 			"xd b gygr    x",
 			"xd   0g1yB  Bx",
 			"xw   yry22   x",
 			"x    rybyB  Yx",
 			"x   Y33Rg    x",
 			"xxxxxxxxxxxxxx",
 			
 			/*
 			"y y",
 			"WWW"
 			*/
 			
 			},
 			{ // level 1			
 			"xxxxxxxxxxxxxx",
 //			"x            x",
 			"x            x",
 			"x            x",
 			"x            x",
 			"x       r    x",
 			"x      xx    x",
 			"x  g     r b x",
 			"xxbxxxg xxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 2
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"x            x", // 			
 			"x            x",
 			"x            x",
 			"x     y   y  x",
 			"x   r r   r  x",
 			"xxxxx x x xxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 3
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"x            x", //			
 			"x            x",
 			"x   by  x y  x",
 			"xxx xxxrxxx  x",
 			"x      b     x",
 			"xxx xxxrxxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 4
 			"xxxxxxxxxxxxxx",
 //			"x            x",
 			"x       r    x",
 			"x       b    x",
 			"x       x    x",
 			"x b r        x",
 			"x b r      b x",
 			"xxx x      xxx",
 			"xxxxx xxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 5
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"x            x",
 			"xrg  gg      x",
 			"xxx xxxx xx  x",
 			"xrg          x",
 			"xxxxx  xx   xx",
 			"xxxxxx xx  xxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 6
 			"xxxxxxxxxxxxxx",
 //			"xxxxxxx      x",
 			"xxxxxxx g    x",
 			"x       xx   x",
 			"x r   b      x",
 			"x x xxx x g  x",
 			"x         x bx",
 			"x       r xxxx",
 			"x   xxxxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 7
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"x          r x",
 			"x          w x",
 			"x     b   b  x",
 			"x     w  rr  x",			
 			"x         w  x",
 			"x R  Bw w w  x",
 			"x W  Ww w w  x",			
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 8
 			"xxxxxxxxxxxxxx",
 			"xxxx x  x xxxx",
 			"xxx  g  b  xxx",
 			"xx   W  W   xx",
 			"xx   B  G   xx",
 			"xxg        bxx",
 			"xxxg      bxxx",
 			"xxxx      xxxx",			
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxxxxxxx",			
 			},
 			{ // level 9		
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"x            x",
 			"x            x",
 			"x            x",
 			"x          rbx",
 			"x    w     xxx",
 			"xb        ddxx",
 			"xx  Rx  x xxxx",			
 			"xxxxWxxxxxxxxx",
 			},
 			{ // level 10
 			"xxxxxxxxxxxxxx",
 			"x   gr       x",
 			"x   dd D     x",
 			"x    w w xxxxx",
 			"x            x",
 			"x  w  w      x",
 			"x        w  Rx",
 			"xx   w     GWx",
 			"x          Wxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 11		
 			"xxxxxxxxxxxxxx",
 			"x      YDDY yx",
 			"x       www xx",
 			"x           yx",
 			"xdd         xx",
 			"xxx          x",
 			"x       y    x",
 			"x   x xxx   Yx",
 			"x   xxxxxx xWx",			
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 12
 			"xxxxxxxxxxxxxx",
 			"xxr rr  rr rxx",
 			"xxx  w  w  xxx",
 			"x            x",
 			"xb          Bx",
 			"xx          Wx",
 			"x            x",
 			"x            x",
 			"x   xxxxxx   x",			
 			"xxxxxxxxxxxxxx",
 			},			
 			{ // level 13
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			"xxxxx yr xxxxx",
 			"xxxxx rb xxxxx",
 			"xxxxx yr xxxxx",
 			"xxxxx by xxxxx",
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 14
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxx   rx",
 			"xxxxxxxxx   gx",
 			"xxxxxxxxx   gx",
 			"x2200       gx",
 			"x2200       gx",
 			"x3311      xxx",
 			"x3311      xxx",
 			"xxR x Gxxx xxx",			
 			"xxWxxxWxxxxxxx",
 			},
 			{ // level 15
 			"xxxxxxxxxxxxxx",
 			"xr r r      rx",
 			"xg w w      gx",
 			"xB          bx",
 			"xWxxx     xxxx",
 			"xxxxxx   xxxxx",
 			"xxxxxx   xxxxx",
 			"xxxxxx   xxxxx",
 			"xxxxxWGgGWxxxx",			
 			"xxxxxx   xxxxx",
 			},
 			{ // level 16
 			"xxxxxxxxxxxxxx",
 			"xx   3332100rx",
 			"xx   3422100xx",
 			"xx   444211xxx",
 			"xW     xxxxxxx",
 			"xR     xxxxxxx",
 			"xx     xxxxxxx",
 			"xx     xxxxxxx",
 			"xx     xxxxxxx",			
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 17
 			"xxxxxxxxxxxxxx",
 			"xxxx000xxxgb x",
 			"xxxx0     bg x",
 			"xxxx0    ddxxx",
 			"xxxx000xxxxxxx",
 			"x 111  xxxxWxx",
 			"xxxx     xxGWx",
 			"xxxx   g    Bx",
 			"xxxx   x     x",			
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 18
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"xb23         x",
 			"xb2yy     y  x",
 			"xb210     ydBx",
 			"xxxxx y   xxWx",
 			"xxxxx yy  xxxx",
 			"xxxxx yyy xxxx",
 			"xxxxx yyyyxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 19
 			"xxxxxxxxxxxxxx",
 			"xG2    G0Gx  x",
 			"x 3G    0 x  x",
 			"x444    1 x  x",
 			"xG G   gGg   x",
 			"xxx     www  x",
 			"xxx     www  x",
 			"xxx     www  x",
 			"xxx          x",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 20
 			"xxxxxxxxxxxxxx",
 			"xrrrr   rggxxx",
 			"xxxb    xxxxWx",
 			"xxxx       xBx",
 			"xx           x",
 			"xx           x",
 			"xx     x     x",
 			"xx x         x",
 			"xx        x  x",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 21
 			"xxxxxxxxxxxxxx",
 			"x      x     x",
 			"x      x     x",
 			"x      W     x",
 			"x      G     x",
 			"x        gb  x",
 			"xxxx     xx  x",
 			"xxxr b     r x",
 			"xxxx xxxxxxxxx",
 			"xxxxxxxxxxxxxx",			
 			},
 			{ // level 22
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"x            x",
 			"x            x",
 			"x            x",
 			"x    g  bgr  x",
 			"x x xx  xxx xx",
 			"xbx          x",
 			"xxxxxxxxxxxxxx",
 			},			
 			{ // level 23
 			"xxxxxxxxxxxxxx",
 			"x            x",
 			"x            x",
 			"x    g       x",
 			"x    b       x",
 			"x    x    r  x",
 			"x        xx  x",
 			"x b          x",
 			"xxxx r xxx xgx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 24
 			"xxxxxxxxxxxxxx",
 			"xg   b     xxx",
 			"xr   g     xxx",
 			"xY   b y   yxx",
 			"xW   x x   xxx",
 			"xxxx       xxx",
 			"xxxx       xxx",
 			"xxxxxx xxxxxxx",
 			"xxxxxxGxxxxxxx",
 			"xxxxxxWxxxxxxx",
 			},			
 			{ // level 25
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxx  W  x",
 			"xxxxxxxx  R  x",
 			"xxxxxxxx     x",
 			"xxxxx     r  x",
 			"xx111    222 x",
 			"x 111    222 x",
 			"x g        x x",
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 26
 			"xxxxxxxxxxxxxx",
 			"xx        xxxx",
 			"xx  r     xxxx",
 			"xx33333333xxxx",
 			"xx     r   xxx",
 			"xx22222222 xxx",
 			"xx  r      xxx",
 			"xx11111111xxxx",
 			"xx     r  xxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 27
 			"xxxxxWxWxWxxxx",
 			"xxxxxG Y Rxxxx",
 			"xxxxx     xxxx",
 			"xbyg2     r  x",
 			"xxxxx     xx x",
 			"xxxxx11111xx x",
 			"xxxxx11111 x x",
 			"xxxx 11111Bx x",
 			"xxxx   b     x",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 28
 			"xxxxxxxxxxxxxx",
 			"xxxx x  x xxxx",
 			"xxx gb  gb xxx",
 			"xx  wW  Ww  xx",
 			"xx   B  G   xx",
 			"xx          xx",
 			"xxx        xxx",
 			"xxxxG    Bxxxx",			
 			"xxxxxxxxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 29
 			"xxxxxxxxxxxxxx",
 			"xxx yyrr xxxxx",
 			"xxx yyrr xxxxx",
 			"xx  bbgg  xxxx",
 			"xx  bbgg  xxxx",
 			"xx  ggbb  xxxx",
 			"xx  ggbb  xxxx",
 			"xxx rryy xxxxx",
 			"xxx rryy xxxxx",			
 			"xxxxxxxxxxxxxx",
 			},			
 			{ // level 30
 			"xWxxxxxxxxxxxx",
 			"xR    xxxxxxxx",
 			"xxx        xxx",
 			"xxxx       xxx",
 			"xxxx       xxx",
 			"xxxx       xxx",
 			"xxxx       xxx",
 			"xrrr       xxx",
 			"xxr        bxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 31
 			"xxxxxxxxxxxxxx",
 			"xxb  xxxx  bxx",
 			"xxx  r  r  xxx",
 			"xx   xxxx   xx",
 			"xx xxxxxxxx xx",
 			"x g   xx   g x",
 			"xx11      22xx",
 			"xx11      22xx",
 			"xxxxxR  Rxxxxx",
 			"xxxxxWxxWxxxxx",
 			},
 			{ // level 32
 			"xxxxxxxxxxxxxx",
 			"xG   Y   xR5Bx",
 			"x3   2    Y4Gx",
 			"xB   R11    xx",
 			"xx   xxx   xxx",
 			"x           xx",
 			"x       xx  xx",
 			"xx          xx",
 			"xxx        xxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 33
 			"xxxxxxxxxxxxxx",
 			"xx1122    xxxx",
 			"xr1332    xxxx",
 			"xx5334    xxxx",
 			"xx5544    xxWx",
 			"xxxxxx    xxRx",
 			"xx           x",
 			"xxx          x",
 			"xx     xx  x x",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 34
 			"xxxxxxxxxxxxxx",
 			"xb      R21Rxx",
 			"xx      2211 x",
 			"xx      3344xx",
 			"x       R34Rxx",
 			"x       xxxxxx",
 			"xx     gxxxxxx",
 			"xx     xxxxxxx",
 			"xx     xxxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 35
 			"xxxxxxxxxxxxxx",
 			"x11    bbbbbrx",
 			"x1B        byx",
 			"x11        byx",
 			"xxWyyy     byx",
 			"xxR2B2     xxx",
 			"xx 222     xxx",
 			"xxxxx      xxx",
 			"xxxxxxxx   xxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 36
 			"xxxxxxxxxxxxxx",
 			"x    brgrbg  x",
 			"x  xx222222xxx",
 			"x  xx2y22r2xxx",
 			"x    222211  x",
 			"x    221111  x",
 			"x    111111  x",
 			"x    111111  x",
 			"x    111111  x",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 37
 			"xxxxxxxxxxxxxx",
 			"xrr  rrr  rryx",
 			"xxx    x   xxx",
 			"x           gx",
 			"x  rrr    rrRW",
 			"xx  1        x",
 			"xxx 1        x",
 			"xx  1        x",
 			"xxx 1       xx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 38
 			"xWWWxxxxxxxxxx",
 			"xGGGxxybr    x",
 			"x   xxbyb    x",
 			"xGGGxxxxxxx  x",
 			"x111xx       x",
 			"xx1xxx       x",
 			"xx      xx xxx",
 			"xx       xxxxx",
 			"xxxxx xxxxxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 39
 			"xxxxxxxxxxxxxx",
 			"xxxxxx    xxxx",
 			"xxxx  1111xxxx",
 			"xxxxx    xxxxx",
 			"xxrx      xgxx",
 			"xxb        bxx",
 			"xyr        gyx",
 			"xxxx      xxxx",
 			"xxxxx xx xxxxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // level 40
 			"xxxxxxxxxxxxxx",
 			"x      r r r x",
 			"xx2yWxxx x x x",
 			"xx22R  x x r x",
 			"xxry y x x x x",
 			"xx11 x x x r x",
 			"xx11       x x",
 			"xx1          x",
 			"xxxx     x   x",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // Bonus level *
 			"xxxxxxxxxxxxxx",
 			"xb  bb  bb  bx",
 			"xr  rr  rr  rx",
 			"xx bbb  bbb xx",
 			"x   11  22   x",
 			"x   WB  RW   x",
 			"x            x",
 			"xx    WW    xx",
 			"xxx   RB   xxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // Bonus level **
 			"xxxxxxxxxxxxxx",
 			"xxxxxrrrrxxxxx",
 			"xxx12335587xxx",
 			"xxx11446677xxx",
 			"xxx 999ddd xxx",
 			"xxx D    : xxx",
 			"xxx =    ; xxx",
 			"xxx > rr < xxx",
 			"xxx > rr < xxx",
 			"xxxxxxxxxxxxxx",
 			},
 			{ // Bonus level ***
 			"xxxxxxxxxxxxxx",
 			"xrgrgr    xxxx",
 			"xgrgrg    xxxx",
 			"xrgrgr    xxxx",
 			"xxxxxWB   bbbx",
 			"xxxxxx    xxxx",
 			"xxxxxx    xxxx",
 			"xxxxxx    xxxx",
 			"xxxxxxG  Rxxxx",
 			"xxxxxxWxxWxxxx",
 			},						
 
 	};
 
 	public BoardConfig() {
 		LEVELS = levels.length - 1; // zero level doesn't count
 		initEmerging();
 		Gdx.app.debug(TAG, "Number of levels is " + LEVELS);
 		complete = new byte[LEVELS + 1];
 	}
 	
 	public void setLevel(int level) {
 		Gdx.app.debug(TAG, "Attempting to define level " + level);
 		if (level > LEVELS || level < 0)
 			level = 0; // ha !!! will you pass this one?
 		
 		this.level = level;
 		String board[] = levels[level];
 		ROWS = board.length;
 		COLS = board[0].length();
 		cells = new char[COLS][ROWS];
 		Gdx.app.debug(TAG, "boardsize (" + COLS + "," + ROWS + ")");
 		transposeBoard(board);
 		Gdx.app.debug(TAG, "Level " + level + " defined");
 	}
 
 	int getNumLevels() {
 		return LEVELS;
 	}
 	
 	private void transposeBoard(String board[]) {
 		// in libgdx (0,0) is the lower left corner
 		// we'll stick to that
 		
 		// flip 
 		if (flipped) {
 			Gdx.app.error(TAG, "building flipped board");
 			for (int x = 0; x < COLS; x++) {
 				for (int y = 0; y < ROWS; y++) {
 					cells[x][y] = board[y].charAt(x);
 				}
 			}			
 			flipped = false; // flip is done only once
 		}
 		
 		else {
 			for (int x = 0; x < COLS; x++) {
 				for (int y = 0; y < ROWS; y++) {
 					cells[x][ROWS - 1 - y] = board[y].charAt(x);
 				}
 			}
 		}
 	}
 	
 	Texture getTexture(int x, int y) {
 		int type = getType(x, y);
 		return Assets.getTexture(type, level);
 	}
 	
 
 	/*
 	boolean isFixed(int x, int y) {
 		 char cell = cells[x][y];
 		 boolean ret;
 		 switch(cell) {
 		 	case 'r' :
 		 	case 'b' :
 		 	case 'g' :
 		 	case 'y' :
 		 	case 'd' :		 		
 		 		ret = false;
 		 		break;
 		 	default:
 		 		ret = true;
 		 		break;
 		 }
 		 return ret;
 	}	
 	*/
 	
 	int getAncoredTo(int x, int y) {
 		if (x < 0 || y < 0)
 			return getEmergingAnchordTo(x, y);
 		char cell = cells[x][y];
 		 		 
 		switch(cell) {
 		 	case 'R' :
 		 	case 'G' :		 		
 		 	case 'B' :		 		
 		 	case 'Y' :
 		 		break;
 		 	default :
 		 		return NONE;
 		 }
 		 
 		 // TODO: for now, anchor to W walls or blacks anchor to blacks
 
 		 if (cells[x][y-1] == 'W')
 			 return DOWN;
 
 		 if (cells[x][y+1] == 'W')
 			 return UP;
 
 		 if (cells[x+1][y] == 'W')
 			 return RIGHT;
 		 
 		 if (cells[x-1][y] == 'W')
 			 return LEFT;
 		 
 		 
 		 if (Cell.isBlack(getType(x-1, y)))
 			 return LEFT;
 
 		 if (Cell.isBlack(getType(x, y+1)))
 			 return UP;
 
 		 if (Cell.isBlack(getType(x+1, y)))
 			 return RIGHT;
 
 		 if (Cell.isBlack(getType(x, y-1)))
 			 return DOWN;
 
 		 // should not be here
 		 Gdx.app.error(TAG, "Could notanchor cell at " + x + ", " + y);
 		 return NONE;
 	}
 	
 	int getBlackType(char blackAscii) {
 		return blackAscii + JELLY_BLACK_MIN;
 	}
 	
 	int getType(int x, int y) {
 		 if (x < 0 || y < 0) {
 			 return getEmergingType(x, y);
 		 }
 		 char cell = cells[x][y];
 		 int ret;
 		 switch(cell) {
 		 	case 'W' :
 		 	case 'w' :		 		
 		 	case 'x' :		 		
 		 		ret = WALL;
 		 		break;
 		 	case 'r' :
 		 	case 'R' :
 		 		ret = JELLY_RED;
 		 		break;
 		 	case 'b' :
 		 	case 'B' :
 		 		ret = JELLY_BLUE;
 		 		break;
 		 	case 'g' :
 		 	case 'G' :
 		 		ret = JELLY_GREEN;
 		 		break;
 		 	case 'y' :
 		 	case 'Y' :
 		 		ret = JELLY_YELLOW;
 		 		break;		 		
 		 	case 'd' :
 		 	case 'D' :		 		
 		 	case '0' :
 		 	case '1' :
 		 	case '2' :
 		 	case '3' :
 		 	case '4' :
 		 	case '5' :
 		 	case '6' :
 		 	case '7' :
 		 	case '8' :
 		 	case '9' :		 		
 		 	case '9' + 1 :
 		 	case '9' + 2 :
 		 	case '9' + 3 :
 		 	case '9' + 4 :
 		 	case '9' + 5 :
 		 	case '9' + 6 :
 		 	case '9' + 7 : 		
 		 		ret = getBlackType(cell);
 		 		break;
 		 	default :
 		 		ret = NONE;
 		 		break;
 		 }
 		 return ret;
 	}
 
 	
 	public Texture getResetButtonsTexture() {
 		return Assets.getButtonsTexture();
 	}
 
 	
 	// rocky mountains
 	private void addHighMountLayer(Background background) {
 		int height = (int) (256 * 0.5);
 		int width = (int) (256 * 3 / 8);
 		
 		background.addLayer(Assets.getBgTexture(1), 0 * width, 0, width, height);
 		background.addLayer(Assets.getBgTexture(1), 1 * width, 0, width, height);
 		background.addLayer(Assets.getBgTexture(1), 2 * width, 0, width, height);
 	}
 
 	private void addLowMountLayer(Background background) {
 		int height = (int) (256 * 0.4);
 		int width = (int) (256 * 3 / 10);
 		
 		background.addLayer(Assets.getBgTexture(1), 0 * width, 0, width, height);
 		background.addLayer(Assets.getBgTexture(1), 1 * width, 0, width, height);
 		background.addLayer(Assets.getBgTexture(1), 2 * width, 0, width, height);
 		background.addLayer(Assets.getBgTexture(1), 3 * width, 0, width, height);
 	}
 
 	private void addGreenMountLayer(Background background) {
 	
 		//background.addLayer(texture, x, y, vX, vY, wrapX, wrapY, width, height)
 		float xSpeed = -3f;
 		float overlap = 2;
 		int height = 96;
 		background.addLayer(Assets.getBgTexture(22), 0 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(22), 1 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(22), 2 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(22), 3 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(22), 4 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 	}	
 
 	private void addGrayMountLayer(Background background) {
 		
 		//background.addLayer(texture, x, y, vX, vY, wrapX, wrapY, width, height)
 		float xSpeed = -3f;
 		int overlap = 2;
 		int height = 96;
 		background.addLayer(Assets.getBgTexture(52), 0 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(52), 1 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(52), 2 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(52), 3 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 		background.addLayer(Assets.getBgTexture(52), 4 * (64 - overlap), 0, xSpeed, 0, 54, 0, 64, height);
 	}	
 
 
 	private void addGrassLayer(Background background) {
 		
 		//background.addLayer(texture, x, y, vX, vY, wrapX, wrapY, width, height)
 		float xSpeed = -10f;
 		int overlap = 2;	
 		background.addLayer(Assets.getBgTexture(21), 0 * (96 - overlap), 0, xSpeed, 0, 120, 0, 96, 64);
 		background.addLayer(Assets.getBgTexture(21), 1 * (96 - overlap), 0, xSpeed, 0, 120, 0, 96, 64);
 		background.addLayer(Assets.getBgTexture(21), 2 * (96 - overlap), 0, xSpeed, 0, 120, 0, 96, 64);
 		background.addLayer(Assets.getBgTexture(21), 3 * (96 - overlap) - 256 - 120, 0, xSpeed, 0, 120, 0, 96, 64);
 	}
 
 	private void addSnowLayer(Background background) {
 		
 		//background.addLayer(texture, x, y, vX, vY, wrapX, wrapY, width, height)
 		float xSpeed = -10f;
 		int overlap = 2;	
 		background.addLayer(Assets.getBgTexture(51), 0 * (96 - overlap), 0, xSpeed, 0, 120, 0, 96, 64);
 		background.addLayer(Assets.getBgTexture(51), 1 * (96 - overlap), 0, xSpeed, 0, 120, 0, 96, 64);
 		background.addLayer(Assets.getBgTexture(51), 2 * (96 - overlap), 0, xSpeed, 0, 120, 0, 96, 64);
 		background.addLayer(Assets.getBgTexture(51), 3 * (96 - overlap) - 256 - 120, 0, xSpeed, 0, 120, 0, 96, 64);
 	}
 	
 	private void addStars(Background background, boolean isGoldColor) {
 		int stars = (int) ( 32 * (Math.random() + 2));
 		for (int i=0 ; i < stars; i++) {
 			int wrapX = (int) (16 * (1 + Math.random()));
 			int wrapY = (int) (16 * (1 + Math.random()));
 			float x = (float) (((double) (256 + wrapX)) * Math.random());
 			float y = (float) (((double) (256 + wrapY)) * Math.random());
 			int textureWidth = 16;
 			int textureHeight = 16;
 			float moment = (float) (75 * (4 + Math.random()));
 			float vX = -8f;
 			float vY = -32f;
 			int width = 6;
 			int height = 6;
 			background.addSprite(Assets.getSmallStarTexture(), x, y, textureWidth, textureHeight , vX, vY, wrapX, wrapY, width, height, moment, isGoldColor);
 		}
 	}
 	
 	private void addSunrise(Background background) {
 		int wrapX = 0;
 		int wrapY = 0;
 		float x = 100;
 		float y = 25;
 		int textureWidth = 256;
 		int textureHeight = 256;
 		float moment = 20f;
 		float vX = 0;
 		float vY = 0;
 		int width = 100;
 		int height = 100;
 		background.addSprite(Assets.getSunRiseTexture(), x, y, textureWidth,
 				textureHeight, vX, vY, wrapX, wrapY, width, height, moment, false);
 	}
 	
 	public Background getBackground() {
 		Background background = new Background();
 		int bgLevel = this.level;
 		if (bgLevel > 20 && bgLevel < 40) {
 			bgLevel -= 20;
 		}
 		// background.color.set(Color.blue); TODO: why doesnt it work !!!		
 		switch (bgLevel) {
 		case 1:
 		case 2:
 		case 3:
 		case 4:
 			// blue sky + clouds
 			background.color.set(0.5f, 0.7f, 1f, 0);
 			
 			addHighMountLayer(background);
 			
 			// clouds
 			background.addLayer(Assets.getBgTexture(0), 0, 86, -15, 0, 128, 0, 100, 128);
 			background.addLayer(Assets.getBgTexture(0), 200, 86, -15, 0, 128, 0, 100, 128);
 			break;
 			
 		case 5:
 		case 6:
 		case 7:
 		case 8:
 		case 13:			
 		case 14:
 		case 15:
 		case 16:
 			// stars in the night
 			background.color.set(0.0f, 0.0f, 0f, 0);
 			addStars(background, true);
 			addLowMountLayer(background);			
 			break;
 
 		case 9:
 		case 10:
 		case 11:
 		case 12:
 			// bald alien on grass
 
 			background.color.set(0.5f, 0.7f, 1f, 0);
 			addGreenMountLayer(background);
 			
 			// bald thing
 			background.addLayer(Assets.getBgTexture(30), 0 * 64, 32, -5, 0, 64, 0, 96, 128);
 			background.addLayer(Assets.getBgTexture(30), 3 * 64, 32, -5, 0, 64, 0, 96, 128);
 
 			addGrassLayer(background);
 				
 			break;
 			
 		case 17:
 		case 18:
 		case 19:
 		case 20:			
 			// bald alien on snow
 
 			background.color.set(0.5f, 0.7f, 1f, 0);
 			
 			addGrayMountLayer(background);			
 						
 			// bald thing
 			background.addLayer(Assets.getBgTexture(30), 0 * 64, 32, -5, 0, 128, 0, 96, 128);
 			background.addLayer(Assets.getBgTexture(30), 2 * 64, 32, -5, 0, 128, 0, 96, 128);
 			background.addLayer(Assets.getBgTexture(30), 4 * 64, 32, -5, 0, 128, 0, 96, 128);
 
 			addSnowLayer(background);
 			
 			break;
 		case 40:
 		case 41:			
 		case 42:
 		case 43:			
 			// stars in the night
 			background.color.set(0.0f, 0.0f, 0f, 0);
 			addGrayMountLayer(background);		
 			
 			
 			addStars(background, false);
 			addSunrise(background);
 			addSnowLayer(background);
 						
 		default:
 			
 		}
 		return background;
 	}
 
 	public Sound getSound(int soundID) {
 		return Assets.getSound(soundID);
 
 	}
 
 	public float getSoundVolume() {
 		return 0.5f; // in [0,1], TODO: get it ftom user
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 	public boolean firstLevel() {
 		return (level == 1);
 	}
 
 	public boolean lastLevel() {
 		return (level == LEVELS);
 	}
 	
 	public void setFlipped() {
 		if (level <= 20) // this will make thing simpler with emerging cells
 			flipped = ! flipped;
 	}
 
 	public void setLastPlayedLevel(int level) {
 		FileHandle handle = Gdx.files.local(SAVE_FILE);
 		
		handle.writeBytes(new byte[] {(byte)level}, false);
		handle.writeBytes(complete, true);
 	}
 
 	public int getLastPlayedLevel() {
 		FileHandle handle = Gdx.files.local(SAVE_FILE);
 		
 		if (!handle.exists())
 			return 1;
 		
 		byte[] bytes = handle.readBytes();
 		complete = Arrays.copyOf(bytes, bytes.length);
 		int level = bytes[0];
 		if (level >= 0 && level <= LEVELS) {
 			return bytes[0];
 		} else 
 			return 1;
 	}
 	
 	
 
 	HashMap<Integer, Emerging> emergingMap; // Map[level][x][y] - emerging cell 
 	
 	int key(int level, int x, int y) {
 		return (10000 * level + 100 * x + y); // 2 digits should be enough
 	}
 	
 	private class Emerging {
 		int type;
 		int anchoredTo;
 		int emergingTo;
 		Emerging(int type, int emergingTo, int anchoredTo) {
 			this.type = type;
 			this.emergingTo = emergingTo;
 			this.anchoredTo = anchoredTo;
 		}
 	}
 	
 	private int getEmergingType(int x, int y) {
 
 		Emerging e = emergingMap.get(key(level, -x, -y));
 		if (e == null)
 			return NONE;
 		else
 			return e.type;
 	}
 	
 	private int getEmergingAnchordTo(int x, int y) {
 
 		Emerging e = emergingMap.get(key(level, -x, -y));
 		if (e == null)
 			return NONE;
 		else
 			return e.anchoredTo;
 	}
 
 	int getEmergingTo(int x, int y) {
 		Emerging e = emergingMap.get(key(level, -x, -y));
 		if (e == null)
 			return NONE;
 		else
 			return e.emergingTo;
 	}
 	
 	Sprite getEmergingSprite(int x, int y) {
 	
 		int to = getEmergingTo(x, y);
 		int type = getEmergingType(x, y);
 		int anchoredTo = getEmergingAnchordTo(x, y);
 		
 		if (type == NONE)
 			return null;
 
 		int dx = 0;
 		switch(type) {
 		case JELLY_BLUE:
 			dx = 1 * 48;
 			break;
 		case JELLY_GREEN:
 			dx = 2 * 48;
 			break;
 		case JELLY_YELLOW:
 			dx = 3 * 48;
 			break;
 			
 		}
 
 		int dy = 0;
 		int dw = 0;
 		if (anchoredTo != NONE) {
 			dy = 48;
 			dx -= 16;
 			dw = 16;
 		}
 		
 		int xP = 28 + dx;
 		int yP = 9 + dy;
 		int wP = 8 + dw;
 		int hP = 46;
 		Sprite sp = new Sprite(Assets.getEmergingTexture(), xP, yP, wP, hP);
 		if (to == UP || to == LEFT)
 			sp.flip(true, true);
 		return sp;
 	}
 
 	
 	private void initEmerging() {
 		emergingMap = new HashMap<Integer, Emerging> ();
 		
 		
 		emergingMap.put(key(21, 7, 1), new Emerging(JELLY_RED, UP, NONE));
 		
 		emergingMap.put(key(22, 6, 0), new Emerging(JELLY_RED, UP, DOWN));
 		
 		emergingMap.put(key(23, 8, 1), new Emerging(JELLY_RED, UP, NONE));
 		
 		emergingMap.put(key(24, 4, 2), new Emerging(JELLY_GREEN, UP, NONE));
 		emergingMap.put(key(24, 9, 2), new Emerging(JELLY_RED, UP, DOWN));
 			
 		emergingMap.put(key(25, 4, 1), new Emerging(JELLY_GREEN, UP, NONE));
 		emergingMap.put(key(25, 7, 1), new Emerging(JELLY_GREEN, UP, NONE));
 		emergingMap.put(key(25, 10, 1), new Emerging(JELLY_GREEN, UP, NONE));
 
 		emergingMap.put(key(26, 4, 0), new Emerging(JELLY_RED, UP, NONE));
 		emergingMap.put(key(26, 7, 2), new Emerging(JELLY_RED, UP, NONE));
 		emergingMap.put(key(26, 4, 4), new Emerging(JELLY_RED, UP, NONE));
 		emergingMap.put(key(26, 7, 6), new Emerging(JELLY_RED, UP, NONE));
 		
 		emergingMap.put(key(27, 6, 0), new Emerging(JELLY_BLUE, UP, NONE));
 		emergingMap.put(key(27, 8, 0), new Emerging(JELLY_BLUE, UP, NONE));
 
 		emergingMap.put(key(28, 5, 1), new Emerging(JELLY_BLUE, UP, DOWN));
 		emergingMap.put(key(28, 8, 1), new Emerging(JELLY_GREEN, UP, DOWN));
 
 		emergingMap.put(key(30, 5, 0), new Emerging(JELLY_BLUE, UP, NONE));
 		emergingMap.put(key(30, 6, 0), new Emerging(JELLY_BLUE, UP, NONE));
 		emergingMap.put(key(30, 7, 0), new Emerging(JELLY_BLUE, UP, NONE));
 		emergingMap.put(key(30, 8, 0), new Emerging(JELLY_BLUE, UP, NONE));
 		emergingMap.put(key(30, 9, 0), new Emerging(JELLY_BLUE, UP, NONE));
 		emergingMap.put(key(30, 10, 0), new Emerging(JELLY_BLUE, UP, NONE));
 		emergingMap.put(key(30, 11, 2), new Emerging(JELLY_BLUE, LEFT, NONE));
 		emergingMap.put(key(30, 11, 3), new Emerging(JELLY_BLUE, LEFT, NONE));
 		emergingMap.put(key(30, 11, 4), new Emerging(JELLY_BLUE, LEFT, NONE));
 		emergingMap.put(key(30, 11, 5), new Emerging(JELLY_BLUE, LEFT, NONE));
 		emergingMap.put(key(30, 11, 6), new Emerging(JELLY_BLUE, LEFT, NONE));
 		emergingMap.put(key(30, 11, 7), new Emerging(JELLY_BLUE, LEFT, NONE));
 		
 		emergingMap.put(key(31, 3, 3), new Emerging(JELLY_GREEN, RIGHT, LEFT));
 		emergingMap.put(key(31, 10, 3), new Emerging(JELLY_GREEN, LEFT, RIGHT));
 		emergingMap.put(key(31, 2, 7), new Emerging(JELLY_BLUE, RIGHT, NONE));
 		emergingMap.put(key(31, 11, 7), new Emerging(JELLY_BLUE, LEFT, NONE));
 		
 		emergingMap.put(key(33, 1, 3), new Emerging(JELLY_RED, RIGHT, NONE));
 		
 		emergingMap.put(key(34, 4, 0), new Emerging(JELLY_GREEN, UP, NONE));
 		emergingMap.put(key(34, 0, 5), new Emerging(JELLY_BLUE, RIGHT, LEFT));
 		emergingMap.put(key(34, 13, 7), new Emerging(JELLY_BLUE, LEFT, NONE));
 		
 		emergingMap.put(key(36, 4, 0), new Emerging(JELLY_RED, UP, NONE));
 		
 		emergingMap.put(key(37, 0, 5), new Emerging(JELLY_GREEN, RIGHT, NONE));
 		emergingMap.put(key(37, 0, 6), new Emerging(JELLY_YELLOW, RIGHT, NONE));
 		
 		emergingMap.put(key(38, 2, 1), new Emerging(JELLY_RED, UP, NONE));
 		
 		emergingMap.put(key(39, 3, 2), new Emerging(JELLY_GREEN, UP, NONE));
 		emergingMap.put(key(39, 10, 2), new Emerging(JELLY_RED, UP, NONE));
 
 		emergingMap.put(key(40, 3, 1), new Emerging(JELLY_YELLOW, UP, NONE));
 	}
 
 	public int whichBonusLevel(int level) {
 		// 3 last levels are bonus
 		int bonus = level - (LEVELS - 3);
 		return bonus;
 	}
 
 	public boolean isCurrentLevelCompleted() {
 		return (complete[level] == 1);
 	}
 	
 	
 	public void setCurrentLevelAsComplete() {
 		
 		complete[level] = (byte) 1;
 	}
 
 	
 }
