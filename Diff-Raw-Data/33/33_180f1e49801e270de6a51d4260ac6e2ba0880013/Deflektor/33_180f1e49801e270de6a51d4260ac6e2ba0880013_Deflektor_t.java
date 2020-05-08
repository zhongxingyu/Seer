 package pro.oneredpixel.deflektorclassic;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 public class Deflektor {
 
 	private static Deflektor deflektor_instance;
 	
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
 	
     Sprite spr;
     Bitmap bm=null;
     DefThreads t;
     static DeflektorApp dApp;
 	
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
 	
 	private Deflektor() {
 		create();
 	};
 
 	public static Deflektor getInstance() {
 		return deflektor_instance;
 	}
 	
 	public static void initInstance(DeflektorApp a) {
 		if (deflektor_instance == null) {
 			dApp = a;
 			deflektor_instance = new Deflektor();
 		}
 	}
 
 	public void create() {
 		
 		t = new DefThreads();
 		t.create();
 		
 		
 		bm=Bitmap.createBitmap(Deflektor.field_width*16, Deflektor.field_height*16, Bitmap.Config.ARGB_8888);
 		spr=new Sprite(R.drawable.defsprites);
 		
 		field=new int[field_width*field_height];
 		
 		field[0+0*field_width]=FLD_MIRROR|6;
 		field[2+2*field_width]=FLD_MIRROR|FLD_AUTOROTATING;
 		field[2+5*field_width]=FLD_MIRROR;
 		field[0+7*field_width]=FLD_MIRROR|12;
 		field[8+0*field_width]=FLD_MIRROR;
 		field[8+8*field_width]=FLD_MIRROR;
 		field[14+3*field_width]=FLD_MIRROR;
 		field[14+5*field_width]=FLD_MIRROR;
 
 		
 		field[1+1*field_width]=FLD_CELL;
 		field[1+2*field_width]=FLD_CELL;
 		field[1+3*field_width]=FLD_CELL;
 		field[2+1*field_width]=FLD_CELL;
 		field[2+3*field_width]=FLD_CELL;
 		field[3+1*field_width]=FLD_CELL;
 		field[3+2*field_width]=FLD_CELL;
 		field[3+3*field_width]=FLD_CELL;
 		
 		field[5+0*field_width]=FLD_CELL;
 		field[6+0*field_width]=FLD_CELL;
 		field[5+1*field_width]=FLD_CELL;
 		field[6+1*field_width]=FLD_CELL;
 		field[11+0*field_width]=FLD_CELL;
 		field[12+0*field_width]=FLD_CELL;
 		field[11+1*field_width]=FLD_CELL;
 		field[9+4*field_width]=FLD_CELL;
 		field[10+7*field_width]=FLD_CELL;
 		field[10+8*field_width]=FLD_CELL;
 		field[12+7*field_width]=FLD_CELL;
 		
 		field[11+6*field_width]=FLD_MINE;
 		field[11+7*field_width]=FLD_MINE;
 		
 		field[5+2*field_width]=FLD_PRISM;
 		
 		field[7+4*field_width]=FLD_SLIT_A|4|FLD_AUTOROTATING;
 		field[10+0*field_width]=FLD_SLIT_B|7|FLD_AUTOROTATING;
 		field[12+2*field_width]=FLD_SLIT_B|7;
 		field[12+6*field_width]=FLD_SLIT_B|7;
 		
 		field[10+3*field_width]=FLD_WARPBOX|1;
 		field[14+7*field_width]=FLD_WARPBOX|1;
 		
 		field[4+0*field_width]=FLD_WALL_B|0x0a;
 		field[4+1*field_width]=FLD_WALL_B|0x0a;
 		field[4+2*field_width]=FLD_WALL_B|0x0f;
 		field[4+3*field_width]=FLD_WALL_B|0x0f;
 		field[4+4*field_width]=FLD_WALL_B|0x0f;
 		field[1+4*field_width]=FLD_WALL_B|0x0c;
 		field[2+4*field_width]=FLD_WALL_B|0x0c;
 		field[3+4*field_width]=FLD_WALL_B|0x0c;
 		
 		field[2+8*field_width]=FLD_WALL_A|0x0a;
 		field[4+8*field_width]=FLD_WALL_A|0x05|FLD_EXPLODEONEND;
 		field[4+7*field_width]=FLD_WALL_A|0x05;
 		field[6+2*field_width]=FLD_WALL_A|0x0c;
 		field[7+2*field_width]=FLD_WALL_A|0x0c;
 		field[7+1*field_width]=FLD_WALL_A|0x05;
 		field[7+0*field_width]=FLD_WALL_A|0x05;
 		field[13+0*field_width]=FLD_WALL_A|0x05;
 		field[14+0*field_width]=FLD_WALL_A|0x0b;
 		field[14+1*field_width]=FLD_WALL_A|0x05;
 		field[14+8*field_width]=FLD_WALL_A|0x02;
 		field[13+8*field_width]=FLD_WALL_A|0x03;
 		field[12+8*field_width]=FLD_WALL_A|0x03;
 		field[9+8*field_width]=FLD_WALL_A|0x05;
 		field[9+7*field_width]=FLD_WALL_A|0x0d;
 		
 		field[3+7*field_width]=FLD_LASER_GUN|3;
 		field[3+8*field_width]=FLD_RECEIVER|1;
 		
 		//drawField();
 		//animateField();
 	}
 	
 	void animateField() {
 		animateField(-1,-1);
 	}
 	
 	void animateField(int x, int y) {
 		int f=0;
 		boolean needToExplodeBarrier=true;
 		for (int i=0;i<field_width;i++) 
 			for (int j=0;j<field_height;j++) {
 				f=field[j*field_width+i];
 				if (((f&FLD_AUTOROTATING)!=0) && ((x!=i) || (y!=j)) ) rotateThing(i,j);
 				if ((f&0xf00)==FLD_EXPLODE) {
 					f++;
 					if ((f&0xf)>4) f=FLD_NULL;
 					else needToExplodeBarrier = false;
 					field[j*field_width+i]=f;
 				};
 				if ((f&0xF00)==FLD_CELL) needToExplodeBarrier = false;
 			};
 		if (needToExplodeBarrier)
 			for (int i=0;i<field_width*field_height;i++)
 				if ((field[i]&FLD_EXPLODEONEND)!=0) field[i]=FLD_EXPLODE;
 	}
 	
 	void rotateThing(int x, int y) {
 		int f=field[y*field_width+x];
 		field[y*field_width+x]=(++f)&0xFFFFFF1F;
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
 				spr.putRegion(bm, i*16, j*16, 8, 8, 7*16, 5*16+8);
 				spr.putRegion(bm, i*16+8, j*16, 8, 8, 7*16, 5*16+8);
 				spr.putRegion(bm, i*16, j*16+8, 8, 8, 7*16, 5*16+8);
 				spr.putRegion(bm, i*16+8, j*16+8, 8, 8, 7*16, 5*16+8);
 			};
 		
 		drawBeam(beam_x,beam_y,beam_angle);
 			
 		for (int i=0;i<field_width;i++) 
 			for (int j=0;j<field_height;j++) {
 				f_angle=field[j*field_width+i]&0x1f;
 				switch (field[j*field_width+i]&0xf00) {
 				case FLD_NULL:
 					break;
 				case FLD_LASER_GUN:
 					spr.putRegion(bm, i*16, j*16, 16, 16, ((f_angle&3)*16), 4*16);
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
 					spr.putRegion(bm, i*16, j*16, 16, 16, ((f_angle&7)*16), 6*16);
 					break;
 				}
 			};
 			
 	};
 
 	//angle=0..31
 	void putMirror(int x, int y, int angle) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, (angle&7)*16, ((angle>>3)&1)*16);
 	}
 	
 	//angle=0..3
 	void putLaser(int x,int y, int angle) {
 
 	}
 	
 	void putReceiver(int x,int y, int angle) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, (((angle&3)+4)*16), 4*16);
 	}
 	
 	void putCell(int x, int y) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, 0, 5*16);
 	}
 	
 	void putMine(int x, int y) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, 16, 5*16);
 	}
 	
 	void putPrism(int x, int y) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, 6*16, 5*16);
 	}
 	
 	void putWarpbox(int x, int y, int type) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, (((type&3)+2)*16), 5*16);
 	}
 	
 	void putSlitA(int x, int y, int angle) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, ((angle&7)*16), 3*16);
 	}
 
 	void putSlitB(int x, int y, int angle) {
 		spr.putRegion(bm, x*16, y*16, 16, 16, ((angle&7)*16), 2*16);
 	}
 	
 	void putWallA(int x, int y, int type) {
 		if ((type&8)!=0)	spr.putRegion(bm, x*16, y*16, 8, 8, 7*16+8, 5*16);
 		//else				spr.putRegion(bm, x*16, y*16, 8, 8, 7*16, 5*16+8);
 		if ((type&4)!=0)	spr.putRegion(bm, x*16+8, y*16, 8, 8, 7*16+8, 5*16);
 		//else				spr.putRegion(bm, x*16+8, y*16, 8, 8, 7*16, 5*16+8);
 		if ((type&2)!=0)	spr.putRegion(bm, x*16, y*16+8, 8, 8, 7*16+8, 5*16);
 		//else				spr.putRegion(bm, x*16, y*16+8, 8, 8, 7*16, 5*16+8);
 		if ((type&1)!=0)	spr.putRegion(bm, x*16+8, y*16+8, 8, 8, 7*16+8, 5*16);
 		//else				spr.putRegion(bm, x*16+8, y*16+8, 8, 8, 7*16, 5*16+8);
 	}
 	
 	void putWallB(int x, int y, int type) {
 		if ((type&8)!=0)	spr.putRegion(bm, x*16, y*16, 8, 8, 7*16, 5*16);
 		//else				spr.putRegion(bm, x*16, y*16, 8, 8, 7*16, 5*16+8);
 		if ((type&4)!=0)	spr.putRegion(bm, x*16+8, y*16, 8, 8, 7*16, 5*16);
 		//else				spr.putRegion(bm, x*16+8, y*16, 8, 8, 7*16, 5*16+8);
 		if ((type&2)!=0)	spr.putRegion(bm, x*16, y*16+8, 8, 8, 7*16, 5*16);
 		//else				spr.putRegion(bm, x*16, y*16+8, 8, 8, 7*16, 5*16+8);
 		if ((type&1)!=0)	spr.putRegion(bm, x*16+8, y*16+8, 8, 8, 7*16, 5*16);
 		//else				spr.putRegion(bm, x*16+8, y*16+8, 8, 8, 7*16, 5*16+8);
 	}
 	
 	void drawBeam(int beamX, int beamY, int beamAngle) {
 		
 		int newBeamX;
 		int newBeamY;
 		int oldBeamAngle;
 		boolean endBeam=false;
 		
 		while (!endBeam) {
 
 			newBeamX = beamX+angleNodeSteps[beamAngle][0];
 			newBeamY = beamY+angleNodeSteps[beamAngle][1];
 			oldBeamAngle = beamAngle;
 			if (newBeamX>=field_width*4 || newBeamX<0 || newBeamY>=field_height*4 || newBeamY<0) {
 				endBeam=true;
 				continue;
 			};
 			
 			//drawline (bm,beamX*4,beamY*4,newBeamX*4,newBeamY*4,0xFFFFFFFF);
			drawSpriteLine(beamX, beamY, newBeamX, newBeamY);
			
 						
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
 					//TODO:   
 					endBeam=true;
 					continue;
 				case FLD_RECEIVER:
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
 					endBeam=true;
 					continue;
 				case FLD_MINE:
 					//TODO:   
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
 					-1,	//0,0,1,0000	  
 					-1,	//0,0,1,0001	  
 					-1,	//0,0,1,0010	 
 					4,	//0,0,1,0011	4
 					-1,	//0,0,1,0100	  
 					0,	//0,0,1,0101	0
 					-1,	//0,0,1,0110	  
 					-2,	//0,0,1,0111	2
 					-1,	//0,0,1,1000	  
 					-1,	//0,0,1,1001	  
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
 			if (Math.abs(beamAngle-oldBeamAngle)==8) endBeam=true;			
 			
 		}
 		
 		
 		
 		
 	};
 	
 	void drawSpriteLine(int x0, int y0, int x1, int y1) {
 		int lx0, lx1, ly0, ly1;
 		if (x0<x1) { lx0=x0; lx1=x1; ly0=y0; ly1=y1;  }
 		else { lx0=x1; lx1=x0; ly0=y1; ly1=y0;  };
 		int sx=lx1-lx0;
 		int sy=ly1-ly0;
		if (sx==0 && sy==-2)					spr.putRegionSafe(bm, lx0*4-4, ly0*4-8, 8, 8, 0, 112);
		if (sx==1 && sy==-2 && ((lx0&1)==0))	spr.putRegionSafe(bm, lx0*4-4, ly0*4-8, 16, 8, 8, 120);
		if (sx==1 && sy==-2 && ((lx0&1)==1))	spr.putRegionSafe(bm, lx0*4-4, ly0*4-8, 16, 8, 12, 112);
		if (sx==2 && sy==-2)					spr.putRegionSafe(bm, lx0*4-4, ly0*4-12, 16, 16, 24, 112);
		if (sx==2 && sy==-1 && ((ly0&1)==0))	spr.putRegionSafe(bm, lx0*4,   ly0*4-4, 16, 8, 40, 120);
		if (sx==2 && sy==-1 && ((ly0&1)==1))	spr.putRegionSafe(bm, lx0*4-8, ly0*4-8, 16, 8, 40, 112);
		if (sx==2 && sy==0)						spr.putRegionSafe(bm, lx0*4,   ly0*4-4, 8, 8, 56, 112);
		if (sx==2 && sy==1 && ((ly0&1)==0))		spr.putRegionSafe(bm, lx0*4,   ly0*4-4, 16, 8, 64, 112);
		if (sx==2 && sy==1 && ((ly0&1)==1)) 	spr.putRegionSafe(bm, lx0*4-8, ly0*4, 16, 8, 64, 120);
		if (sx==2 && sy==2)						spr.putRegionSafe(bm, lx0*4-4, ly0*4-4, 16, 16, 80, 112);
		if (sx==1 && sy==2 && ((lx0&1)==0))		spr.putRegionSafe(bm, lx0*4-4, ly0*4, 16, 8, 96, 112);
		if (sx==1 && sy==2 && ((lx0&1)==1))		spr.putRegionSafe(bm, lx0*4-4, ly0*4, 16, 8, 100, 120);
		if (sx==0 && sy==2)						spr.putRegionSafe(bm, lx0*4-4, ly0*4, 8, 8, 0, 112);
 		
 		
 	}
 	
 	void drawline(Bitmap bitmap, int x1, int y1, int x2, int y2, int colour1)
 	{
 		int x0;
 		int y0;
 		int sx = Math.abs(x2-x1);
 		int sy = Math.abs(y2-y1);
 		int zx = ((x2>x1)?1:(-1));
 		int zy = ((y2>y1)?1:(-1));
 		int err=0;
 		
 		if (sx>=sy) {
 			y0=y1;
 			for (x0=x1; x0!=x2; x0+=zx) {
 				bitmap.setPixel(x0, y0, colour1);
 				err+=sy;
 				if (2*err >= sx) {
 					y0+=zy;
 					err-=sx;
 				};
 			};
 			//     .
 			bitmap.setPixel(x2, y2, colour1);
 		} else if (sy>sx) {
 			x0=x1;
 			for (y0=y1; y0!=y2; y0+=zy) {
 				bitmap.setPixel(x0, y0, colour1);
 				err+=sx;
 				if (2*err >= sy) {
 					x0+=zx;
 					err-=sy;
 				};
 			};
 			//     .
 			bitmap.setPixel(x2, y2, colour1);
 		};
 	};
 	
 	void touch(int x, int y) {
 		int f=field[y*field_width+x];
 		if ((f&0xFF00)==FLD_MIRROR) {
 			rotateThing(x,y);
 		};
 		//drawField();
 		//animateField();
 	};
 	
 	public Bitmap getBitmap() {
 		return bm;
 	}
 	
 	class Sprite {
 		int width=0;
 		int height=0;
 		int data[]=null;
 		
 		Sprite (int res_id) {
 			Bitmap b=BitmapFactory.decodeResource(dApp.getResources(), res_id);
 			if (b!=null) { 
 				width = b.getWidth();
 				height = b.getHeight();
 				data = new int[width*height];
 				b.getPixels(data, 0, width, 0, 0, width, height);
 			};
 		};
 		
 		//x,y,sx,sy - space in bitmap to put sprite in,
 		//bx,by - coordinates in sprite where begins region
 		void putRegion(Bitmap bitmap, int x, int y, int sx, int sy, int bx, int by) {
 			if (width>0 && height>0 && data!=null) {
 				bitmap.setPixels(data, by*width+bx, width, x, y, sx, sy);
 			};
 		};
 		
 		//x,y,sx,sy - space in bitmap to put sprite in,
 		//bx,by - coordinates in sprite where begins region
 		void putRegionSafe(Bitmap bitmap, int x, int y, int sx, int sy, int bx, int by) {
 			int bmWidth = bitmap.getWidth();
 			int bmHeight = bitmap.getHeight();
 			if (x<0) { bx=bx-x; sx=sx+x; x=0; };
 			if (y<0) { 	by=by-y; sy=sy+y; y=0; };
 			if ((x+sx) >= bmWidth) { sx = sx-(x+sx-bmWidth+1); };
 			if ((y+sy) >= bmHeight) { sy = sy-(y+sy-bmHeight+1); };
			if (width>0 && height>0 && sx>0 && sy>0 && data!=null) {
 				bitmap.setPixels(data, by*width+bx, width, x, y, sx, sy);
 			};
 		};
 		
 	}
 
 	
 	boolean DoPeriodGfxTask() {
 		drawField();
 		animateField();
 		return true;
 	}
 }
