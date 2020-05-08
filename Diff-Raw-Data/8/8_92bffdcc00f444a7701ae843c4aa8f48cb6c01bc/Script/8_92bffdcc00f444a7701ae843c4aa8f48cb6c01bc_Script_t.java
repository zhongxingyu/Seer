 package core;
 
 import static core.VergeEngine.*;
 import static core.Controls.*;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Polygon;
 import java.util.List;
 import java.awt.color.ColorSpace;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.awt.image.ColorConvertOp;
 import java.awt.image.RescaleOp;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Random;
 
 import audio.VMusic;
 import audio.VSound;
 
 import domain.Entity;
 import domain.Map;
 import domain.VFont;
 import domain.VImage;
 
 import static core.SinCos.*;
 
 public class Script {
 
 	//public static final int VCFILES		=		51;
 	public static final int  VC_READ	=			1;
 	public static final int  VC_WRITE		=	2;
 	public static final int  VC_WRITE_APPEND	=	3; // Overkill (2006-07-05): Append mode added.	
 	
 
 	public static final int CF_GRAY = 1;
 	public static final int CF_INV_GRAY = 2;
 	public static final int CF_INV = 3;
 	public static final int CF_RED = 4;
 	public static final int CF_GREEN = 5;
 	public static final int CF_BLUE = 6;
 	public static final int CF_CUSTOM = 7;	
 	
 	// VERGE ENGINE VARIABLES: Moved to Script for easy of use
 	/**
 	 * This is a hardcoded image handle for the screen. It is a pointer to a
 	 * bitmap of the screen's current dimensions (set in v3.cfg or by
 	 * SetResolution() function at runtime). Anything you want to appear in the
 	 * verge window should be blitted here with one of the graphics functions.
 	 * When ShowPage() is called the screen bitmap is transfered to the display.
 	 */
 	public static VImage screen;
 
 	/** read-only timer variable constantly increasing*/
 	public static int systemtime;
 
 	/** read/write timer variable*/
 	public static int timer;
 
 	// internal use only
 	static int vctimer = 0; // [Rafael, the Esper]
 	static int hooktimer = 0;
 
 	public static List<Entity> entity = new ArrayList<Entity>();
 	public static Entity myself = null;
 
 	/**
 	 * The number of entities currently on the map. Use this an the upper bound
 	 * any time you need to loop through and check entites for something.
 	 */
 	public static int numentities;
 
 	public static int player;
 	public static int playerstep = 1;
 	public static boolean playerdiagonals = true;
 	public static boolean smoothdiagonals = true; // [Rafael, the Esper]
 
 	public static int xwin, ywin;
 	public static Map current_map = null;
 
 	public static int cameratracking = 1;
 	public static int cameratracker = 0;
 	public static int lastplayerdir = 0;
 
 	public static boolean entitiespaused = false;
 	
 	// END OF VERGE ENGINE VARIABLES
 	
 	public static String renderfunc, timerfunc; // was VergeCallback (struct)
 
 	public static DefaultPalette palette = new DefaultPalette();
 	public static int transcolor = -65281; // Color(255, 0, 255);
 	public static int currentLucent = 255;
 	
 	public static int event_tx;
 	public static int event_ty;
 	public static int event_zone;
 	public static int event_entity;
 	public static int event_param;
 	public static int event_sprite;
 	public static int event_entity_hit;
 
 	public static int __grue_actor_index;
 	
 	private static VMusic musicplayer; // [Rafael, the Esper] 
 	private static VSound soundplayer; // [Rafael, the Esper]
 	
 	public static int invc;
 
 	public static String _trigger_onStep = "", _trigger_afterStep = "";
 	public static String _trigger_beforeEntityScript = "", _trigger_afterEntityScript = "";
 	public static String _trigger_onEntityCollide = "";
 	public static String _trigger_afterPlayerMove = "";
 
 	public static int vc_GetYear()
 	{
 		  Calendar cal=Calendar.getInstance();
 		  return cal.get(Calendar.YEAR);
 	}
 
 	public static int vc_GetMonth()
 	{
 		  Calendar cal=Calendar.getInstance();
 		  return cal.get(Calendar.MONTH);
 
 	}
 
 	public static int vc_GetDay()
 	{
 		  Calendar cal=Calendar.getInstance();
 		  return cal.get(Calendar.DAY_OF_MONTH);
 	}
 
 	public static int vc_GetDayOfWeek()
 	{
 		  Calendar cal=Calendar.getInstance();
 		  return cal.get(Calendar.DAY_OF_WEEK);
 	}
 
 	public static int vc_GetHour()
 	{
 		  Calendar cal=Calendar.getInstance();
 		  return cal.get(Calendar.HOUR);
 	}
 
 	public static int vc_GetMinute()
 	{
 		  Calendar cal=Calendar.getInstance();
 		  return cal.get(Calendar.MINUTE);
 	}
 
 	public static int vc_GetSecond()
 	{
 		  Calendar cal=Calendar.getInstance();
 		  return cal.get(Calendar.SECOND);
 	}
 
 
 	public static void error(String str) { 
 	  	System.err.println(str);
 	}
 
 	static void hooktimer()
 	{
 		// To prevent hooktimer from happening before the script engine is loaded.
 		//if(se == null) return;
 
 		while (hooktimer != 0)
 		{
 			callfunction(timerfunc);
 			hooktimer--;
 		}
 	}
 
 	public static void hooktimer(String cb) {
 		hooktimer = 0;
 		timerfunc = cb;
 	}
 	
 	public static void hookretrace()
 	{
 		if(renderfunc != null) {
 			callfunction(renderfunc);
 		}
 	}
 
 	public static void hookretrace(String cb) {
 		renderfunc = cb;
 	}
 
 	
 	// Rafael: Changed to ExecuteFunctionString 
 	/*public void ExecuteCallback(String function, boolean callingFromLibrary) */
 	
 	public static void exit(String message) { 
 		System.err.println(message);
 		System.exit(-1); 
 	}
 
 	/* Rafael: TODO Implement this.
 	 public static void SetButtonJB(int b, int jb) {
 		switch (b)
 		{
 			case 1: j_b1 = jb; break;
 			case 2: j_b2 = jb; break;
 			case 3: j_b3 = jb; break;
 			case 4: j_b4 = jb; break;
 		}
 	}*/
 
 	// Overkill (2007-08-25): HookButton is supposed to start at 1, not 0.
 	// It's meant to be consistent with Unpress().
 	public static void hookbutton(int b, String s) {
 		if (b<1 || b>4) return;
 		bindbutton[b-1] = s;
 	}
 
 	public static void hookkey(int k, String s) {
 		if (k<0 || k>127) return;
 		bindarray[k] = s;
 	}
 
 	public static void log(String s) { 
 		System.out.println(s); 
 	}
 
 	/*
 	static void MessageBox(String msg) { showMessageBox(msg); }*/
 
 	public static int random(int min, int max) { 
 		Random r = new Random();
 		return r.nextInt(max+1-min) + min; 
 	}
 	
 	
 	public static void setappname(String s) { 
 		getGUI().setTitle(s);
 	}
 
 	public static void unpress(int n) {
 		switch (n)
 		{
 			case 0: 
 				if (b1) UnB1(); 
 				if (b2) UnB2(); 
 				if (b3) UnB3(); 
 				if (b4) UnB4(); 	
 				break;
 			case 1: if (b1) UnB1(); break;
 			case 2: if (b2) UnB2(); break;
 			case 3: if (b3) UnB3(); break;
 			case 4: if (b4) UnB4(); break;
 			case 5: if (up) UnUp(); break;
 			case 6: if (down) UnDown(); break;
 			case 7: if (left) UnLeft(); break;
 			case 8: if (right) UnRight(); break;
 			case 9: 
 				if (b1) UnB1(); 
 				if (b2) UnB2(); 
 				if (b3) UnB3(); 
 				if (b4) UnB4();
 				if (up) UnUp();
 				if (down) UnDown();
 				if (left) UnLeft();
 				if (right) UnRight();
 				break;
 		}
 	}
 
 	public static void updatecontrols() { 
 		Controls.UpdateControls(); 
 	}
 
 	public static int asc(String s) { 
 		if(s.length() == 0) 
 			return 0; 
 		else 
 			return (int)s.charAt(0); 
 	}
 	
 	public static String chr(int c) { 
 		return Character.toString((char) c);
 	}
 	
 	public static String gettoken(String s, String d, int i) { // Reimplemented by [Rafael, the Esper]
 		String[] retorno = s.split(d);
 		if(retorno.length <= i)
 			return "";
 		
 		return retorno[i];
 	}
 	public static String left(String str, int len) { 
 		return str.substring(0, str.length()>len?len:str.length());
 	}
 	
 	public static int len(String s) { 
 		return s.length(); 
 	}
 	
 	public static String mid(String str, int pos, int len) { 
 		return str.substring(pos, pos+len);
 	}
 	
 	public static String right(String str, int len) { 
 		return len > str.length() ? str : str.substring(str.length() - len);
 	}
 	
 	public static String str(int d) { 
 		return Integer.toString(d);
 	}
 	
 	public static boolean strcmp(String s1, String s2) { 
 		return s1.equals(s2); // ? 1 : 0;
 	}
 	
 	public static String capitalize(String s) { // [Rafael, the Esper]
 		if (s.length() == 0) return s;
 		return s.substring(0, 1).toUpperCase() + s.substring(1);		
 	}
 	
 	public static String strdup(String s, int times) {
 		String ret = "";
 		for (int i=0; i<times; i++)
 			ret = ret.concat(s);
 		return ret;
 	}
 	
 	public static int tokencount(String s, String d) {
 		String[] retorno = s.split(d);
 		return retorno.length;
 	}
 	
 	public static String trim(String s) { 
 		return s.trim(); 
 	}
 	
 	public static String tolower(String str) { 
 		return str.toLowerCase();
 	}
 
 	public static String toupper(String str) {
 		return str.toUpperCase();
 	}
 
 	public static int val(String s) { 
 		if(s==null || s.isEmpty() || s.trim().equals("-"))
 			return 0;
 		
 		return Integer.valueOf(s.replace('+', ' ').trim());
 	}
 
 	//VI.d. Map Functions
 	public static boolean getobs(int x, int y) { 
 		if(current_map == null) 
 			return false; 
 		else 
 			return current_map.obstruct(x, y);}
 	
 	public static boolean getobspixel(int x, int y) { 
 		if(current_map == null) 
 			return false; 
 		else 
 			return current_map.obstructpixel(x, y);}
 	
 	public static int gettile(int x, int y, int i) { 
 		if(current_map == null) 
 			return 0; 
 		if(i>=current_map.layers.length) 
 			return 0; 
 		return current_map.layers[i].GetTile(x,y); 
 	}
 	
 	public static int getzone(int x, int y) { 
 		if(current_map == null) 
 			return 0; 
 		else return current_map.zone(x,y); 
 	}
 	
 	public static void map(String map) {
 		mapname = map;
 		die = true;
 		done = true;
 
 		/* Hookretrace carries over between maps!
 		/ According to http://verge-rpg.com/docs/the-verge-3-manual/general-utility-functions/hookretrace/
 		  hookretrace(""); */ 
 	}
 	
 	public static void render() {
 		TimedProcessEntities(); 
 		VergeEngine.RenderMap();
 	}
 	
 	public static void rendermap(int x, int y, VImage dest) {
 		if (current_map==null) 
 			return;
 		current_map.render(x, y, dest);
 	}
 	
 	public static void setobs(int x, int y, int t) { 
 		if(current_map == null) 
 			return; 
 		else 
 			current_map.SetObs(x,y,t); 
 	}
 	
 	public static void settile(int x, int y, int i, int z) { 
 		if(current_map == null) 
 			return; 
 		else if(i>=current_map.layers.length) 
 			return; 
 		else current_map.layers[i].SetTile(x,y,z); 
 	}
 	
 	public static void setzone(int x, int y, int z) { 
 		if(current_map == null) 
 			return; 
 		else if(z>=current_map.zones.length) 
 			return; 
 		else current_map.SetZone(x,y,z); 
 	}
 
 	//VI.e. Entity Functions
 	public static void changeCHR(int e, String c) {
 		if (e<0 || e >= numentities) return;
 		else entity.get(e).set_chr(c);
 	}
 	public static void entitymove(int e, String s) {
 		if (e<0 || e >= numentities) return;
 		else entity.get(e).SetMoveScript(s);
 	}
 	public static void entitysetwanderdelay(int e, int d) {
 		if (e<0 || e >= numentities) return;
 		else entity.get(e).SetWanderDelay(d);
 	}
 	public static void entitysetwanderrect(int e, int x1, int y1, int x2, int y2) {
 		if (e<0 || e >= numentities) return;
 		else entity.get(e).SetWanderBox(x1, y1, x2, y2);
 	}
 	public static void entitysetwanderzone(int e) {
 		if (e<0 || e >= numentities) return;
 		else entity.get(e).SetWanderZone();
 	}
 	public static int entityspawn(int x, int y, String s) { 
 		return AllocateEntity(x*16,y*16,s); 
 	}
 	public static void entitystalk(int stalker, int stalkee) {
 		if (stalker<0 || stalker>=numentities)
 			return;
 		if (stalkee<0 || stalkee>=numentities)
 		{
 			entity.get(stalker).clear_stalk();
 			return;
 		}
 		entity.get(stalker).setx(entity.get(stalkee).getx()); // [Rafael, the Esper]
 		entity.get(stalker).sety(entity.get(stalkee).gety()); // [Rafael, the Esper]
 		entity.get(stalker).stalk(entity.get(stalkee));
 	}
 	public static void entitystop(int e) {
 		if (e<0 || e >= numentities) return;
 		else entity.get(e).SetMotionless();
 	}
 	public static void hookentityrender(int i, String s) {
 		if (i<0 || i>=numentities) 
 			System.err.printf("vc_HookEntityRender() - no such entity %d", i);
 		entity.get(i).hookrender = s;
 	}
 	
 	public static void playermove(String s) {
 		if (myself==null) 
 			return;
 		myself.SetMoveScript( s );
 
 		while(myself.movecode != 0 )
 		{
 			render();
 			showpage();
 		}
 
 		playerentitymovecleanup();
 	}
 
 	public static void playerentitymovecleanup() {
 		if (myself==null) return;
 
 		myself.movecode = 0;
 		//[Rafael, the Esper] implementar afterPlayerMove();
 	}
 
 	public static void pauseplayerinput() { // [Rafael, the Esper]
 		invc = 1;
 	}
 	public static void unpauseplayerinput() { // [Rafael, the Esper]
 		invc = 0;
 	}
 	
 	public static void setentitiespaused(boolean b) {
 		entitiespaused = b;
 		if (!entitiespaused)
 			lastentitythink = systemtime;
 	}
 	
 	public static void setplayer(int e) {
 		if (e<0 || e>=numentities)
 		{
 			player = -1;
 			myself = null;
 			System.err.println("invalid Player.");
 			return;
 		}
 		myself = entity.get(e);
 		player = e;
 		myself.SetMotionless();
 		myself.obstructable = true;
 	}
 
 	public static int getplayer()
 	{
 		return player;
 	}
 	
 	//VI.f. Graphics Functions
 	/*static void AdditiveBlit(int x, int y, int src, int dst) {
 		image *s = ImageForHandle(src);
 		image *d = ImageForHandle(dst);
 		AdditiveBlit(x, y, s, d);
 	}*/
 	public static void alphablit(int x, int y, VImage src, VImage alpha, VImage dst) {
 		// [Rafael, the Esper] TODO Implement
 		//AlphaBlit(x, y, s, a, d);
 		//error("Non implemented function");
 		tblit(x, y, src, dst);
 	}
 	
 	public static void blitentityframe(int x, int y, int e, int f, VImage dst) {
 		if (current_map==null || e<0 || e >= numentities) return;
 		entity.get(e).chr.render(x, y, f, dst);
 	}
 
 	// Overkill (2007-08-25): src and dest were backwards. Whoops!
 	public static void blitlucent(int x, int y, int lucent, VImage src, VImage dst) {
 		int oldalpha = currentLucent;
 		setlucent(lucent);
 		blit(x, y, src, dst);
 		setlucent(oldalpha);
 	}
 	
 	public static void blittile(int x, int y, int t, VImage dst) {
 		if (current_map != null) {
 			current_map.tileset.UpdateAnimations();
 			current_map.tileset.Blit(x, y, t, dst);
 		}
 	}
 
 	public static void blit(int x, int y, VImage src, VImage dst) {
 		blit(x,y,src.getImage(), dst.getImage());
 	}
 	public static void blit(int x, int y, Image src, Image dst) { // [Rafael, the Esper] Always opaque
 		if(currentLucent < 255) {
 			Graphics2D g2d = (Graphics2D) dst.getGraphics();
 			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(currentLucent)/255));
 			g2d.drawImage(src, x, y, Color.BLACK, null);
 		}
 		else {
 			dst.getGraphics().drawImage(src, x, y, Color.BLACK, null);
 		}
 	}
 	public static void tblit(int x, int y, VImage src, VImage dst) {
 		tblit(x, y, src.getImage(), dst.getImage());
 	}
 	public static void tblit(int x, int y, Image src, Image dst) {
 		if(currentLucent < 255) {
 			Graphics2D g2d = (Graphics2D) dst.getGraphics();
 			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)(currentLucent)/255));
 			g2d.drawImage(src, x, y, null);
 		}
 		else {
 			dst.getGraphics().drawImage(src,x,y,null);
 		}
 	}
 	
 	/*static void BlitWrap(int x, int y, int src, int dst) {
 		image *s = ImageForHandle(src);
 		image *d = ImageForHandle(dst);
 		BlitWrap(x, y, s, d);
 	}*/
 	
 	public static void circle(int x1, int y1, int xr, int yr, Color c, VImage dst) { // [Rafael, the Esper]
 		dst.g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), currentLucent));
 		dst.g.drawOval(x1-xr, y1-yr, xr*2, yr*2);
 	}
 	
 	// TODO Note: different from Java fillOval, the circle is centered in (x1, y1)
 	public static void circlefill(int x1, int y1, int xr, int yr, int c, VImage dst) {
 		if(c==transcolor) {
 			Graphics2D g2d = (Graphics2D) dst.g;
 			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
 			g2d.setColor(new Color(0, 0, 0, 0));
 			g2d.fillOval(x1-xr, y1-yr, xr*2, yr*2);			
 			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
 		}
 		else {
 			circlefill(x1, y1, xr, yr, palette.getColor(c, currentLucent), dst);
 		}
 	}
 	public static void circlefill(int x1, int y1, int xr, int yr, Color c, VImage dst) { // [Rafael, the Esper]
 		dst.g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), currentLucent));
 		dst.g.fillOval(x1-xr, y1-yr, xr*2, yr*2);
 	}
 	
 	private static BufferedImageOp op = null;
 	
 	public static void graycolorfilter(BufferedImage img) {
 		if(op==null)
 			op = new ColorConvertOp (ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
 		img = op.filter(img, img);
 		return;
 	}
 	
 	
 	public static void colorfilter(int filter, VImage img) { 
 		if(filter>6) return;
 		if(filter==1) {
 			if(op==null)
 				op = new ColorConvertOp (ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
 			op.filter(img.getImage(), img.getImage());
 			return;
 		}
 		
 		int rr, gg, bb, z; Color c = null;
 
 		int x1,x2,y1,y2;
 		// [Rafael, the Esper] img.GetClip(x1,y1,x2,y2);
 		x1 = y1 = 0;
 		x2 = img.width;
 		y2 = img.height;
 
 		//PT ptr = (PT)img.data;
 		//PT data = (PT)&ptr[(y1 * img.pitch) + x1];
 
 		for (int y=y1; y<y2; y++)
 		{
 			//int* data_end = data+x2+1;
 			for(int x=x1;x<x2;x++) {
 				int rgb = img.getImage().getRGB(x, y);
 				//Color col = new Color(img.getImage().getRGB(x, y));
 				if (rgb == transcolor) continue; // Overkill (2006-07-27): Ignore trans pixels
 				rr = (rgb >> 16) & 0x000000FF;				
 				gg = (rgb >>8 ) & 0x000000FF;
 				bb = (rgb) & 0x000000FF;
 				//GetColor(col, rr, gg, bb);
 				//if(filter==2) System.out.printf("%d %d %d %d\n", rr, gg, bb, 255-((rr+gg+bb)/3));
 				switch (filter)
 				{
 					case 0: 
 					case 1: z = (rr+gg+bb)/3; c = new Color(z,z,z); break; // GRAY
 					case 2: z = 255-((rr+gg+bb)/3); c = new Color(z,z,z); break;
 					case 3: c = new Color(255-rr, 255-gg, 255-bb); break;
 					case 4: z = (rr+gg+bb)/3; c = new Color(z, 0, 0); break; // RED
 					case 5: z = (rr+gg+bb)/3; c = new Color(0, z, 0); break; // GREEN
 					case 6: z = (rr+gg+bb)/3; c = new Color(0, 0, z); break; // BLUE
 					// [Rafael, the Esper] Custom color filter case 7: z = (rr+gg+bb)/3; c = new Color(cf_r1+((cf_rr*z)>>8), cf_g1+((cf_gr*z)>>8), cf_b1+((cf_br*z)>>8)).getRGB(); break;
 				}
 				setpixel(x, y, c, img);
 			}
 		}
 	}
 	
 	public static void copyimagetoclipboard(VImage src) {
 		src.copyImageToClipboard();
 		//clipboard_putImage(src);
 	}
 	
 	public static VImage duplicateimage(VImage s) { // TODO Test
 		if(s==null)
 			return new VImage(1,1);
 		VImage img = new VImage(s.image.getWidth(), s.image.getHeight());
 		img.g.drawImage(s.getImage(), 0, 0, null);
 		return img;
 	}
 	
 	// FIXME
 	public enum FlipType{FLIP_HORIZONTALLY, FLIP_VERTICALLY, FLIP_BOTH};
 	//public static void flipblit(int x, int y, boolean fx, boolean fy, VImage src, VImage dest) {
 	public static void flipblit(int x, int y, FlipType type, VImage src, VImage dest) {
 		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
         tx.translate(-src.width, 0);
         AffineTransformOp op = new AffineTransformOp(tx, 
                                 AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
         VImage flippedImage = duplicateimage(src);
         //BufferedImage flippedImage =  new BufferedImage(src.width, 
           //      src.height, BufferedImage.TYPE_INT_RGB);
         //flippedImage = op.filter(src.image, null);		
         flippedImage.image = op.filter(flippedImage.image, null);
         //blit(x, y, flippedImage, dest.image);
        	blit(x,y,flippedImage,dest);
 	}
 
 	public static int getB(int c) {
 		return palette.getColor(c, currentLucent).getBlue();
 	}
 	public static int getG(int c) {
 		return palette.getColor(c, currentLucent).getGreen();
 	}
 	public static int getR(int c) {
 		return palette.getColor(c, currentLucent).getRed(); 
 	}
 	
 	/*static int GetImageFromClipboard() {
 		image *t = clipboard_getImage();
 		if (!t) return 0;
 		else return HandleForImage(t);
 	}*/
 	
 	/*public static int GetPixel(int x, int y, VImage src) {
 		WritableRaster wr = src.image.getRaster();
 		wr.getPixel(x, y, arg2);
 		
 		return ReadPixel(x, y, s);
 	}*/
 	
 
 	
 	/*static void GrabRegion(int sx1, int sy1, int sx2, int sy2, int dx, int dy, int src, int dst) {
 		image *s = ImageForHandle(src);
 		image *d = ImageForHandle(dst);
 
 		int dcx1, dcy1, dcx2, dcy2;
 		d.GetClip(dcx1, dcy1, dcx2, dcy2);
 
 		if (sx1>sx2) SWAP(sx1, sx2);
 		if (sy1>sy2) SWAP(sy1, sy2);
 		int grabwidth = sx2 - sx1;
 		int grabheight = sy2 - sy1;
 		if (dx+grabwidth<0 || dy+grabheight<0) return;
 		d.SetClip(dx, dy, dx+grabwidth, dy+grabheight);
 		Blit(dx-sx1, dy-sy1, s, d);
 
 		d.SetClip(dcx1, dcy1, dcx2, dcy2);
 	}*/
 	
 	/** This extremely powerful function will allow you to take an image, define a rectangle 
 	 * within it, and get an image handle referencing that rectangle within the original image. 
 	 * xofs, yofs, width, height indicate the position and dimensions of the rectangle within 
 	 * the source image. Clipping rectangles for the two images are completely independent. 
 	 * Rendering into one will render into the other.* If that is what you want to do, then this 
 	 * is the function you want to use. 
 	 */
 	public static VImage imageshell(int x, int y, int w, int h, VImage src) {
 		if (w+x > src.width || y+h > src.height)
 			System.err.printf(
 				"ImageShell() - Bad arguments. x/y+w/h greater than original image dimensions\n\nx:%d,w:%d (%d),y:%d,h:%d (%d), orig_x:%d, orig_y:%d",
 				x,w,x+w,y,h,y+h,src.width,src.height
 			);
 
 		VImage dst = new VImage(w, h);
 		//dst.delete_data();
 		//dst.shell = true;
 
 		//dst.data = ((quad *)src.data + (y*src.pitch)+x);
 		//dst.pitch = src.pitch;
 		
 		// TODO Implement this mechanism!
 		error("Non implemented function: imageshell");
 		return dst;
 	}
 	
 	/*static int ImageValid(int handle) {
 		if (handle <= 0 || handle >= Handle::getHandleCount(HANDLE_TYPE_IMAGE) || (Handle::getPointer(HANDLE_TYPE_IMAGE,handle) == NULL) )
 			return 0;
 		else return 1;
 	}*/
 	
 	public static int imagewidth(VImage src) { return src.width; }
 	public static int imageheight(VImage src) { return src.height; }
 	
 	public static void line(int x1, int y1, int x2, int y2, Color c, VImage dst) { // [Rafael, the Esper]
 		dst.g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), currentLucent));
 		dst.g.drawLine(x1, y1, x2, y2);
 	}
 	
 	public static Color mixcolor(Color c1, Color c2, int p) {
 		if (p>255) p=255;
 		if (p<0) p=0;
 
 		int r1 = c1.getRed();
 		int g1 = c1.getGreen();
 		int b1 = c1.getBlue();
 		int r2 = c2.getRed();
 		int g2 = c2.getGreen();
 		int b2 = c2.getBlue();
 
 		return new Color((r1*(255-p)/255)+(r2*p/255), (g1*(255-p)/255)+(g2*p/255), (b1*(255-p)/255)+(b2*p/255));
 	}	
 	
 	/*static void Mosaic(int xgran, int ygran, int dst) {
 		image *dest = ImageForHandle(dst);
 		Mosaic(xgran, ygran, dest);
 	}*/
 	
 	public static void rect(int x1, int y1, int x2, int y2, int c, VImage dst) {
 		rect(x1, y1, x2, y2, palette.getColor(c, currentLucent), dst);
 	}
 	public static void rect(int x1, int y1, int x2, int y2, Color c, VImage dst) { // [Rafael, the Esper]
 		dst.g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), currentLucent));
 		if(x1>x2) {	int temp = x1;	x1 = x2;	x2 = temp;	} // swap x1,x2
 		if(y1>y2) {	int temp = y1;	y1 = y2;	y2 = temp;	} // swap y1,y2
 		dst.g.drawRect(x1, y1, x2-x1, y2-y1);
 	}
 
 	public static void rectfill(int x1, int y1, int x2, int y2, int c, VImage dst) {
 		if(c==transcolor) {
 			Graphics2D g2d = (Graphics2D) dst.g;
 			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
 			g2d.setColor(new Color(0, 0, 0, 0));
 			g2d.fillRect(x1, y1, x2, y2);			
 			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
 		}
 		else
 			rectfill(x1, y1, x2, y2, palette.getColor(c, currentLucent), dst);
 	}
 	
 	public static void rectfill(int x1, int y1, int x2, int y2, Color c, VImage dst) { // [Rafael, the Esper]
 		if(c.getAlpha()==255)
 			c = new Color(c.getRed(), c.getGreen(), c.getBlue(), currentLucent);
 
 		dst.g.setColor(c);
 		if(x1>x2) {	int temp = x1;	x1 = x2;	x2 = temp;	} // swap x1,x2
 		if(y1>y2) {	int temp = y1;	y1 = y2;	y2 = temp;	} // swap y1,y2
 		dst.g.fillRect(x1, y1, x2-x1, y2-y1);
 	}
 	
 	
 	public static void rotscale(int x, int y, int angle, int scale, VImage src, VImage dst) {
 		//TODO [Rafael, the Esper] Implement
 		blit(x, y, src, dst);
 		//RotScale(x, y,  angle*(float)3.14159/(float)180.0, scale/(float)1000.0, s, d);
 	}
 	public static void scaleblit(int x, int y, int dw, int dh, VImage src, VImage dst) {
 		//ScaleBlit(x, y, dw, dh, s, d);
 		blit(x, y, src, dst); // TODO [Rafael, the Esper] Implement scaling		
 	}
 	
 	/* Draws a scaled image. A bit more complex than the other blitters to use. 
 	 * The x,y values give the upper-left corner of where the blit will start. 
 	 * iw,ih are the width and height of the *source* image. 
 	 * dw, dh are the width and height that the image should appear on screen. 
 	 * (ie, the end result bounding box of the image would be, x, y, x+dw, y+dh) 
 	 * Image is, as with the other blit routines, a pointer to the image graphic.
 	 */
 	public static void scalesprite(int x, int y, int iw, int ih, int dw, int dh, VImage image) {
 		screen.g.drawImage(image.getImage(), x, y, x+dw, y+dh, 0, 0, iw, ih, null);
 	}
 	
 	public static void lightfilter(int scalefactor, VImage vimage) {
 		RescaleOp op = new RescaleOp((float)scalefactor/100, 0, null);
 		op.filter(vimage.image, vimage.image);
 	}
 	
 	
 	
 	public static void setclip(int x1, int y1, int x2, int y2, VImage img) {
 		//img.SetClip(x1, y1, x2, y2);
 		// TODO [Rafael, the Esper] Implement this mechanism in VImage
 		//error("Non implemented function: setclip");
 	}
 	
 	public static void setcustomcolorfilter(Color c1, Color c2) {
 		/*GetColor(c1, cf_r1, cf_g1, cf_b1);
 		GetColor(c2, cf_r2, cf_g2, cf_b2);
 		cf_rr = cf_r2 - cf_r1;
 		cf_gr = cf_g2 - cf_g1;
 		cf_br = cf_b2 - cf_b1;*/
 		// TODO [Rafael, the Esper] Implement this
 		graycolorfilter(screen.getImage());
 		error("Non implemented function: setcustomcolorfilter");
 	}
 	
 	public static void setlucent(int p) { 
 		if(p < 0 || p > 100)
 			return;
 		p = 100 - p;
 		currentLucent = p * 255 / 100;
 		getGUI().setAlpha ((float)p / 100);
 	}
 
 	static int lastchangetime = 0;
 	
 	public static void showpage() {
 		//flipblit(0,0,true, true,screen,screen);
 		//System.out.println("showpage");
 		Controls.UpdateControls();
 		
 		// Check if the player pressed a special key
 		VergeEngine.checkFunctionKeys();
 		
 		//VEngine.updateGUI();
 		DefaultTimer();//[Rafael, the Esper]
 		GUI.paintFrame();
 		//VEngine.synchFramerate();
 		//VergeEngine.PaintToScreen();
 		//getGUI().getCanvas().setCanvas_screen(screen.getImage()); //[Rafael, the Esper]
 	}
 	
 	public static void silhouette(int x, int y, Color c, VImage src, VImage dst) {
 		int x1,x2,y1,y2;
 		
 		//WritableRaster wr = dst.getImage().getRaster();
 		x1 = y1 = 0;
 		x2 = src.width;
 		y2 = src.height;
 
 		for (int j=y1; j<y2; j++)
 		{
 			for(int i=x1;i<x2;i++) {
 				if(src.getImage().getRGB(i, j)==transcolor || src.getImage().getRGB(i, j)==0) // black 
 					setpixel(x+i, y+j, new Color(0,0,0,0), dst.getImage());
 				else
 					setpixel(x+i, y+j, c, dst.getImage());
 			}
 		}		
 	}
 	/*
 	static void SubtractiveBlit(int x, int y, int src, int dst) {
 		image *s = ImageForHandle(src);
 		image *d = ImageForHandle(dst);
 		SubtractiveBlit(x, y, s, d);
 	}
 	static void TAdditiveBlit(int x, int y, int src ,int dst) {
 		image *s = ImageForHandle(src);
 		image *d = ImageForHandle(dst);
 		TAdditiveBlit(x, y, s, d);
 	}*/
 	
 	public static void grabregion(int sx1, int sy1, int sx2, int sy2, int dx, int dy, VImage src, VImage dst) {
 		grabregion(sx1, sy1, sx2, sy2, dx, dy, src.image, dst.image);
 	}
 	
 	public static void grabregion(int sx1, int sy1, int sx2, int sy2, int dx, int dy, BufferedImage src, BufferedImage dst) {
 		
 			// Getclip
 			//int dcx1 = dst.cx1;
 			//int dcy1 = dst.cy1;
 			//int dcx2 = dst.cx2;
 			//int dcy2 = dst.cy2;
 		
 			if (sx1>sx2) { // swap sx1, sx2
 				int temp = sx1;
 				sx1 = sx2;
 				sx2 = temp;
 			}
 			if (sy1>sy2) { // swap sy1, sy2
 				int temp = sy1;
 				sy1 = sy2;
 				sy2 = temp;				
 			}
 			
 			Color color = null;
 			for(int j=0; j<sy2-sy1; j++)
 			for(int i=0; i<sx2-sx1; i++) {
 				if(sx1+i >= src.getWidth() || sy1+j >= src.getHeight()
 				   || dx+i >= dst.getWidth() || dy+j >= dst.getHeight())		
 					break;
 				color = new Color(src.getRGB(sx1+i, sy1+j));
 				if(color.getRed() + color.getGreen() + color.getBlue() == 0) // TODO [Rafael, the Esper] Probably move it to tgrabregion?
 					color = new Color(0,0,0,0); //color.getRed(), color.getGreen(), color.getBlue(), 0);
 				setpixel(i+dx, j+dy, color, dst);
 
 			}
 			/*int grabwidth = sx2 - sx1;
 			int grabheight = sy2 - sy1;
 			if (dx+grabwidth<0 || dy+grabheight<0) return;
 			dst.SetClip(dx, dy, dx+grabwidth, dy+grabheight);
 			Blit(dx-sx1, dy-sy1, src, dst);
 		
 			dst.SetClip(dcx1, dcy1, dcx2, dcy2);*/
 	}
 
 	public static void setpixel(int x, int y, Color color, BufferedImage dst) {
 		//if(color.getRGB() == transcolor) // Transparent
 			//dst.setRGB(x, y, 0);
 		//else
 			dst.setRGB(x, y, color.getRGB());
 	}
 	public static void setpixel(int x, int y, Color color, VImage dst) {
 		setpixel(x,y,color,dst.image);
 	}	
 	
 	public static int readpixel(int x, int y, VImage workingimage) {
 		if(workingimage.image != null)
 			return workingimage.image.getRGB(x, y);
 		return 0;
 	}
 	
 	public static Color RGB(int r, int g, int b) {
 		return new Color(r, g, b);
 	}
 	
 	
 	// Overkill (2007-08-25): src and dest were backwards. Whoops!
 	public static void tblitlucent(int x, int y, int lucent, VImage src, VImage dst) {
 		int oldalpha = currentLucent;
 		setlucent(lucent);
 		tblit(x, y, src, dst);
 		setlucent(oldalpha);
 	}
 
 	public static void tblittile(int x, int y, int t, VImage dst) {
 		if (current_map!=null) 
 			current_map.tileset.TBlit(x, y, t, dst);
 	}
 	/*
 	static void TGrabRegion(int sx1, int sy1, int sx2, int sy2, int dx, int dy, int src, int dst) {
 		image *s = ImageForHandle(src);
 		image *d = ImageForHandle(dst);
 
 		int dcx1, dcy1, dcx2, dcy2;
 		d.GetClip(dcx1, dcy1, dcx2, dcy2);
 
 		if (sx1>sx2) SWAP(sx1, sx2);
 		if (sy1>sy2) SWAP(sy1, sy2);
 		int grabwidth = sx2 - sx1;
 		int grabheight = sy2 - sy1;
 		if (dx+grabwidth<0 || dy+grabheight<0) return;
 		d.SetClip(dx, dy, dx+grabwidth, dy+grabheight);
 		TBlit(dx-sx1, dy-sy1, s, d);
 
 		d.SetClip(dcx1, dcy1, dcx2, dcy2);
 	}*/
 
 	// Note: it's a filled triangle. A non-filled triangle can be draw with lines.
 	public static void triangle(int x1, int y1, int x2, int y2, int x3, int y3, Color c, VImage dst) { // [Rafael, the Esper]
 		Polygon p = new Polygon();
 		p.addPoint(x1, y1);
 		p.addPoint(x2, y2);
 		p.addPoint(x3, y3);
 		dst.g.setColor(c);
 		dst.g.fillPolygon(p);
 	}
 	
 	
 	public static void tscaleblit(int x, int y, int dw, int dh, VImage src, VImage dst) {
 		//TScaleBlit(x, y, dw, dh, s, d);
 		tblit(x, y, src, dst); // TODO [Rafael, the Esper] Implement scaling
 		
 	}/*
 	static void TSubtractiveBlit(int x, int y, int src, int dst) {
 		image *s = ImageForHandle(src);
 		image *d = ImageForHandle(dst);
 		TSubtractiveBlit(x, y, s, d);
 	}*/
 	public static void twrapblit(int x, int y, VImage src, VImage dst) {
 		// TODO [Rafael, the Esper] Implement
 		//TWrapBlit(x, y, s, d);
 		error("Non implemented function: twrapblit");
 	}
 	public static void wrapblit(int x, int y, VImage src, VImage dst) {
 		// TODO [Rafael, the Esper] Implement
 		//WrapBlit(x, y, s, d);
 		error("Non implemented function: wrapblit");
 	}
 /*
 	//VI.g. Sprite Functions
 	static int GetSprite() { return GetSprite(); }
 	static void ResetSprites() { return ResetSprites(); }
 
 	//VI.h. Sound/Music Functions
 	static void FreeSong(int handle) { FreeSong(handle); }
 	static void FreeSound(int slot) { FreeSample((void*)slot); }
 	static int GetSongPos(int handle) { return GetSongPos(handle); }
 	static int GetSongVolume(int handle) { return GetSongVol(handle); }
 	static int LoadSong(String fn) { return LoadSong(fn); }
 	static int LoadSound(String fn) { return (int)LoadSample(fn); }*/
 	public static void playsound(URL fn) {
 		playsound(fn, 100);
 	}
 	public static void playsound(URL fn, int volume) {
 		if(fn==null || VergeEngine.config.isNosound())
 			return;
 
 		if (volume < 0)
 			volume = 0;
 		else if (volume > 100)
 			volume = 100;
 		soundplayer = new VSound();
 		soundplayer.start(fn, volume);
 	}
 
 	public static void playmusic(URL fn) { 
 		if(fn==null || VergeEngine.config.isNosound())
 			return;
 		
 		if(musicplayer!=null) {
 			musicplayer.stop();
 		}
 		try {
 			musicplayer = new VMusic();
 			log("Playing..." + fn);
 			musicplayer.start(fn);
 		}
 		catch(Exception e) {
 			System.err.println("Error when playing " + fn);
 		}
 	}
 	
 	/*
 	static void PlaySong(int handle) { PlaySong(handle); }*/
 	/*public static int playsound(String name, int volume) { 
 		return 0;
 		//return PlaySample((void*) slot, volume * 255 / 100); 
 	}*/
 	
 	public static void setmusicvolume(int v) { 
 		// TODO Implement
 	}
 	
 	/*static void SetSongPaused(int h, int p) { SetPaused(h,p); }
 	static void SetSongPos(int h, int p) { SetSongPos(h,p); }
 	static void SetSongVolume(int h, int v) { SetSongVol(h,v); } */
 	public static void stopmusic() { 
 		if(musicplayer!=null) {
 			musicplayer.stop();
 		}
 	}
 	
 	/*static void StopSong(int handle) { StopSong(handle); }
 	static void StopSound(int chan) { StopSound(chan); }
 */
 	//VI.i. Font Functions
 	public static void enablevariablewidth(VFont font) {
 		if(font != null)
 			font.EnableVariableWidth();
 	}
 	public static int fontheight(VFont f) {
 		if (f==null) return 7;
 		else return f.height;
 	}
 	/*
 	static void FreeFont(int f) {
 		Font *font = (Font*) f;
 		if (font) delete font;
 	}
 	static int LoadFont(String filename, int width, int height) {
 		return (int) new Font(filename, width, height);
 	}
 	static int LoadFontEx(String filename) { return (int) new Font(filename); }
 */
 	public static void printcenter(int x, int y, VImage d, VFont font, String text) { 
 		if(font!=null)
 			font.PrintCenter(text, x, y, d);
 	}
 	
 	public static void printright(int x, int y, VImage d, VFont font, String text) { 
 		if(font!=null)
 			font.PrintRight(text, x, y, d);
 	}
 	
 	public static void printstring(int x, int y, VImage dest, VFont font, String text) {
 		if(font!=null)
 			font.PrintString(text, x, y, dest);
 	}
 	
 	public static void printstring(int x, int y, VImage dest, Font font, String text) {
 		dest.g.setFont(font);
 		dest.g.setColor(Color.WHITE);
 		dest.g.drawString(text, x, y);
 	}
 
 	
 	public static int textwidth(VFont fh, String text) {
 		return fh.Pixels(text);
 		//if (font == 0) return pixels(text);
 		//else return font.Pixels(text);
 	}
 
 	//VI.j. Math Functions
 	//helper:
 	public static int abs(int i) {
 		return Math.abs(i);
 	}
 	public static int sgn(int i) {
 		return (int) Math.signum(i);
 	}
 	
 	
 	public static int mydtoi(double d) { return (int)Math.floor(d + 0.5); }
 	static int acos(int val) {
 		double dv = (double) val / 65535;
 		double ac = Math.acos(dv);
 		ac = ac * 180 / 3.14159265358979; // convert radians to degrees
 		return mydtoi(ac);
 	}
 	public static int facos(int val) {
 		double dv = (double) val / 65535;
 		double ac = Math.acos(dv);
 		ac *= 65536; // Convert to 16.16 fixed point
 		return mydtoi(ac);
 	}
 	public static int asin(int val) {
 		double dv = (double) val / 65535;
 		double as = Math.asin(dv);
 		as = as * 180 / 3.14159265358979; // convert radians to degrees
 		return mydtoi(as);
 	}
 	public static int fasin(int val) {
 		double dv = (double) val / 65535;
 		double as = Math.asin(dv);
 		as *= 65536; // Convert to 16.16 fixed point
 		return mydtoi(as);
 	}
 	public static int atan(int val) {
 		double dv = (double) val / 65535;
 		double at = Math.atan(dv);
 		at = at * 180 / 3.14159265358979; // convert radians to degrees
 		return mydtoi(at);
 	}
 	public static int fatan(int val) {
 		double dv = (double) val / 65535;
 		double at = Math.atan(dv);
 		at *= 65536; // Convert to 16.16 fixed point
 		return mydtoi(at);
 	}
 	public static int atan2(int y, int x) {
 		float f = (float) Math.atan2((float)y,(float)x);
 		return (int)(f/2.0/3.14159265358979*360.0);
 	}
 	public static int fatan2(int y, int x) {
 		double theta = Math.atan2((double) y, (double) x);
 		return mydtoi(theta * 65536);
 	}
 	public static int sin(int n) {
 	    while (n < 0) n += 360;
 	    while (n >= 360) n -= 360;
 		return sintbl[n];
 	}
 	public static int cos(int n) {
 	    while (n < 0) n += 360;
 	    while (n >= 360) n -= 360;
 		return costbl[n];
 	}
 	public static int tan(int n) {
 	    while (n < 0) n += 360;
 	    while (n >= 360) n -= 360;
 		return tantbl[n];
 	}
 	public static int fsin(int val) {
 		double magnitude = Math.sin((double) val / 65536);
 		return mydtoi(magnitude * 65536);
 	}
 	public static int fcos(int val) {
 		double magnitude = Math.cos((double) val / 65536);
 		return mydtoi(magnitude * 65536);
 	}
 	public static int ftan(int val) {
 		double magnitude = Math.tan((double) val / 65536);
 		return mydtoi(magnitude * 65536);
 	}
 	public static int pow(int a, int b) {
 		return (int) Math.pow((double)a, (double)b);
 	}
 	public static int sqrt(int val) {
 		return (int) (float) Math.sqrt((float) val);
 	}
 /*
 	//VI.k. File Functions
 	static void FileClose(int handle) {
 		if (!handle) return;
 		if (handle > VCFILES)
 			se.Error("FileClose() - uhh, given file handle is not a valid file handle.");
 		if (!vcfiles[handle].active)
 			se.Error("FileClose() - given file handle is not open.");
 
 		switch (vcfiles[handle].mode)
 		{
 			case VC_READ:
 				vclose(vcfiles[handle].vfptr);
 				vcfiles[handle].vfptr = 0;
 				vcfiles[handle].mode = 0;
 				vcfiles[handle].active = false;
 				break;
 			case VC_WRITE:
 				fclose(vcfiles[handle].fptr);
 				vcfiles[handle].fptr = 0;
 				vcfiles[handle].mode = 0;
 				vcfiles[handle].active = false;
 				break;
 			default:
 				se.Error("FileClose() - uhhh. file mode is not valid?? you did something very bad!");
 		}
 	}
 
 	static int FileCurrentPos(int handle) {
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("FileCurrentPos() - file handle is either invalid or file is not open.");
 
 		switch (vcfiles[handle].mode)
 		{
 			case VC_READ:
 				return vtell(vcfiles[handle].vfptr);
 				break;
 			case VC_WRITE:
 				return ftell(vcfiles[handle].fptr);
 				break;
 			default:
 				se.Error("FileCurentPos() - File mode not valid! That's bad!");
 		}
 		return 0;
 	}
 
 	static boolean FileEOF(int handle) {
 		if (!handle) se.Error("FileEOF() - File is not open.");
 		if (handle > VCFILES) se.Error("FileEOF() - given file handle is not a valid file handle.");
 		if (!vcfiles[handle].active) se.Error("FileEOF() - given file handle is not open.");
 		if (vcfiles[handle].mode != VC_READ) se.Error("FileEOF() - given file handle is a write-mode file.");
 
 		return veof(vcfiles[handle].vfptr)!=0;
 	}
 
 	static int FileOpen(String fname, int filemode) {
 		int index;
 
 		for (index=1; index<VCFILES; index++)
 			if (!vcfiles[index].active)
 				break;
 		if (index == VCFILES)
 			se.Error("FileOpen() - Out of file handles! \nTry closing files you're done with, or if you really need more, \nemail vecna@verge-rpg.com and pester me!");
 		
 		const char* cpfname = fname;
 
 //	[Rafael, the Esper]	#ifdef __APPLE__
 		// swap backslashes in path for forward slashes
 		// (windows . unix/max)
 		string converted = fname.str();
 		boost::algorithm::replace_all(converted, "\\", "/");
 		cpfname = converted;
 //		#endif
 
 		switch (filemode)
 		{
 			case VC_READ:
 				vcfiles[index].vfptr = vopen(cpfname);
 				if (!vcfiles[index].vfptr)
 				{
 					//log("opening of %s for reading failed.", cpfname);
 					return 0;
 				}
 				vcfiles[index].active = true;
 				vcfiles[index].mode = VC_READ;
 				break;
 			case VC_WRITE:
 			case VC_WRITE_APPEND: // Overkill (2006-07-05): Append mode added.
 				if(filemode == VC_WRITE)
 					vcfiles[index].fptr = fopen(cpfname, "wb");
 				else vcfiles[index].fptr = fopen(cpfname, "ab");
 				if (!vcfiles[index].fptr)
 				{
 					//log("opening of %s for writing/appending failed.", cpfname);
 					return 0;
 				}
 				vcfiles[index].active = true;
 				vcfiles[index].mode = VC_WRITE;
 				break;
 			default:
 				se.Error("FileOpen() - not a valid file mode!");
 		}
 		return index;
 	}
 
 	static int FileReadByte(int handle) {
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("FileReadByte() - file handle is either invalid or file is not open.");
 		if (vcfiles[handle].mode != VC_READ)
 			se.Error("FileReadByte() - given file handle is a write-mode file.");
 
 		int ret=0;
 		vread(&ret, 1, vcfiles[handle].vfptr);
 		return ret;
 	}
 
 	static String FileReadln(int handle) {
 		if (!handle) se.Error("FileReadln() - File is not open.");
 		if (handle > VCFILES) se.Error("FileReadln() - given file handle is not a valid file handle.");
 		if (!vcfiles[handle].active) se.Error("FileReadln() - given file handle is not open.");
 		if (vcfiles[handle].mode != VC_READ) se.Error("FileReadln() - given file handle is a write-mode file.");
 
 
 		char buffer[255]; 	// buffer for each read
 		string result = ""; // all the text so far
 		int eol = 0;        // flag for when we've hit the end of a line
 		do {
 			vgets(buffer, 255, vcfiles[handle].vfptr); // read it
 
 			if(buffer[0] == '\0')  {
 				eol = 1; // we didn't read anything, this is eof
 			} else if(buffer[strlen(buffer)-1] == 10 || buffer[strlen(buffer)-1] == 13) {
 				// last character is a EOL character, so it's the end of a line
 				eol = 1;
 			}
 
 			strclean(buffer);
 			result += buffer;
 		} while(!eol);
 
 		return result;
 	}
 	static int FileReadQuad(int handle) {
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("FileReadQuad() - file handle is either invalid or file is not open.");
 		if (vcfiles[handle].mode != VC_READ)
 			se.Error("FileReadQuad() - given file handle is a write-mode file.");
 
 		int ret=0;
 		vread(&ret, 4, vcfiles[handle].vfptr);
 		return ret;
 	}
 	static String FileReadString(int handle){
 		int len = 0;
 		char *buffer;
 
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("FileReadString() - file handle is either invalid or file is not open.");
 		if (vcfiles[handle].mode != VC_READ)
 			se.Error("FileReadString() - given file handle is a write-mode file.");
 
 		vread(&len, 2, vcfiles[handle].vfptr);
 		buffer = new char[len+1];
 		vread(buffer, len, vcfiles[handle].vfptr);
 		buffer[len]=0;
 		String ret = buffer;
 		delete[] buffer;
 		return ret;
 	}
 	static String FileReadToken(int handle) {
 		if (!handle) se.Error("FileReadToken() - File is not open.");
 		if (handle > VCFILES) se.Error("FileReadToken() - given file handle is not a valid file handle.");
 		if (!vcfiles[handle].active) se.Error("FileReadToken() - given file handle is not open.");
 		if (vcfiles[handle].mode != VC_READ) se.Error("FileReadToken() - given file handle is a write-mode file.");
 
 		char buffer[255];
 		buffer[0] = '\0'; // ensure sending back "" on error
 		vscanf(vcfiles[handle].vfptr, "%s", buffer);
 		strclean(buffer);
 		return buffer;
 	}
 	static int FileReadWord(int handle) {
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("FileReadWord() - file handle is either invalid or file is not open.");
 		if (vcfiles[handle].mode != VC_READ)
 			se.Error("FileReadWord() - given file handle is a write-mode file.");
 
 		int ret = 0;
 		vread(&ret, 2, vcfiles[handle].vfptr);
 		return ret;
 	}
 	static void FileSeekLine(int handle, int line) {
 		if (!handle) se.Error("FileSeekLine() - File is not open.");
 		if (handle > VCFILES) se.Error("FileSeekLine() - given file handle is not a valid file handle.");
 		if (!vcfiles[handle].active) se.Error("FileSeekLine() - given file handle is not open.");
 		if (vcfiles[handle].mode != VC_READ) se.Error("FileSeekLine() - given file handle is a write-mode file.");
 
 		vseek(vcfiles[handle].vfptr, 0, SEEK_SET);
 		char temp[256+1];
 		while (line-.0)
 	        vgets(temp, 256, vcfiles[handle].vfptr);
 	}
 	static void FileSeekPos(int handle, int offset, int mode) {
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("FileSeekPos() - file handle is either invalid or file is not open.");
 
 		switch (vcfiles[handle].mode)
 		{
 			case VC_READ:
 				vseek(vcfiles[handle].vfptr, offset, mode);
 				break;
 			case VC_WRITE:
 				fseek(vcfiles[handle].fptr, offset, mode);
 				break;
 			default:
 				se.Error("SFileeekPos() - File mode not valid! That's bad!");
 		}
 	}
 	static void FileWrite(int handle, String s) {
 		if (!handle) se.Error("FileWrite() - Yo, you be writin' to a file that aint open, foo.");
 		if (handle > VCFILES) se.Error("FileWrite() - given file handle is not a valid file handle.");
 		if (!vcfiles[handle].active) se.Error("FileWrite() - given file handle is not open.");
 		if (vcfiles[handle].mode != VC_WRITE) se.Error("FileWrite() - given file handle is a read-mode file.");
 
 		fwrite(s, 1, s.length(), vcfiles[handle].fptr);
 	}
 	static void FileWriteByte(int handle, int var) {
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("FileWriteByte() - file handle is either invalid or file is not open.");
 		if (vcfiles[handle].mode != VC_WRITE)
 			se.Error("FileWriteByte() - given file handle is a read-mode file.");
 		flip(&var, sizeof(var)); // ensure little-endian writing
 		fwrite(&var, 1, 1, vcfiles[handle].fptr);
 	}
 	static void FileWriteln(int handle, String s) {
 		if (!handle) se.Error("FileWriteln() - Yo, you be writin' to a file that aint open, foo.");
 		if (handle > VCFILES) se.Error("FileWriteln() - given file handle is not a valid file handle.");
 		if (!vcfiles[handle].active) se.Error("FileWriteln() - given file handle is not open.");
 		if (vcfiles[handle].mode != VC_WRITE) se.Error("FileWriteln() - given file handle is a read-mode file.");
 
 		fwrite(s, 1, s.length(), vcfiles[handle].fptr);
 		fwrite("\r\n", 1, 2, vcfiles[handle].fptr);
 	}
 	static void FileWriteQuad(int handle, int var) {
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("FileWriteQuad() - file handle is either invalid or file is not open.");
 		if (vcfiles[handle].mode != VC_WRITE)
 			se.Error("FileWriteQuad() - given file handle is a read-mode file.");
 		flip(&var, sizeof(var)); // ensure little-endian writing
 		fwrite(&var, 1, 4, vcfiles[handle].fptr);
 	}
 	static void FileWriteString(int handle, String s) {
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("FileWriteString() - file handle is either invalid or file is not open.");
 		if (vcfiles[handle].mode != VC_WRITE)
 			se.Error("FileWriteString() - given file handle is a read-mode file.");
 
 		int l = s.length();
 		int writeLength = l;
 
 		flip(&writeLength, sizeof(writeLength)); // ensure little-endian writing
 		fwrite(&writeLength, 1, 2, vcfiles[handle].fptr);
 		fwrite(s, 1, l, vcfiles[handle].fptr);
 	}
 	static void FileWriteWord(int handle, int var) {
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("vc_FileWriteWord() - file handle is either invalid or file is not open.");
 		if (vcfiles[handle].mode != VC_WRITE)
 			se.Error("vc_FileWriteWord() - given file handle is a read-mode file.");
 		flip(&var, sizeof(var)); // ensure little-endian writing
 		fwrite(&var, 1, 2, vcfiles[handle].fptr);
 	}
 	String ListFilePattern(String pattern) {
 		vector<string> result;
 		listFilePattern(result, pattern);
 		string ret;
 
 		for(vector<string>::iterator i = result.begin();
 			i != result.end();
 			i++)
 				ret += *i + "|";
 		return ret;
 	}
 
 	// Overkill (2006-07-20):
 	// Saves a CHR file, using an open file handle, saving the specified entity.
 	static void FileWriteCHR(int handle, int ent) {
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("FileWriteCHR() - file handle is either invalid or file is not open.");
 		if (vcfiles[handle].mode != VC_WRITE)
 			se.Error("FileWriteCHR() - given file handle is a read-mode file.");
 		if (ent < 0 || ent >= entities)
 			se.Error("Tried saving an invalid or inactive ent index (%d).", ent);
 
 		entity[ent].chr.save(vcfiles[handle].fptr);	
 	}
 
 	// Overkill (2006-07-20):
 	// Saves a MAP file, using an open file handle, saving the current map.
 	static void FileWriteMAP(int handle) {
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("FileWriteMAP() - file handle is either invalid or file is not open.");
 		if (vcfiles[handle].mode != VC_WRITE)
 			se.Error("FileWriteMAP() - given file handle is a read-mode file.");
 		if (!current_map)
 			se.Error("FileWriteMAP() - There is no active map, therefore making it not possible to save this map.");
 
 		current_map.save(vcfiles[handle].fptr);	
 	}
 	// Overkill (2006-07-20):
 	// Saves a VSP file, using an open file handle, saving the current map's VSP.
 	static void FileWriteVSP(int handle) {
 		if (!handle || handle > VCFILES || !vcfiles[handle].active)
 			se.Error("FileWriteVSP() - file handle is either invalid or file is not open.");
 		if (vcfiles[handle].mode != VC_WRITE)
 			se.Error("FileWriteVSP() - given file handle is a read-mode file.");
 		if (!current_map)
 			se.Error("FileWriteVSP() - There is no active map, therefore making it not possible to save the map's vsp.");
 
 		current_map.tileset.save(vcfiles[handle].fptr);	
 	}
 
 	//VI.l. Window Managment Functions
 	//helper"
 	static void checkhandle(char *func, int handle, AuxWindow *auxwin) {
 		if(!handle)
 			se.Error("%s() - cannot access a null window handle!",func);
 		if(!auxwin)
 			se.Error("%s() - invalid window handle!",func);
 	}
 	static void WindowClose(int win) {
 		if(win == 1) se.Error("WindowClose() - cannot close gameWindow");
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowClose",win,auxwin);
 		auxwin.dispose();
 	}
 	static int WindowCreate(int x, int y, int w, int h, String s) {
 		AuxWindow *auxwin = vid_createAuxWindow();
 		auxwin.setTitle(s);
 		auxwin.setPosition(x,y);
 		auxwin.setResolution(w,h);
 		auxwin.setSize(w,h);
 		auxwin.setVisibility(true);
 		return auxwin.getHandle();
 	}
 	static int WindowGetHeight(int win) {
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowGetHeight",win,auxwin);
 		return auxwin.getHeight();
 	}
 	static int WindowGetImage(int win) {
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowGetImage",win,auxwin);
 		return auxwin.getImageHandle();
 	}
 	static int WindowGetWidth(int win) {
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowGetWidth",win,auxwin);
 		return auxwin.getWidth();
 	}
 	static int WindowGetXRes(int win) {
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowGetXRes",win,auxwin);
 		return auxwin.getXres();
 	}
 	static int WindowGetYRes(int win) {
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowGetYRes",win,auxwin);
 		return auxwin.getYres();
 	}
 	static void WindowHide(int win) {
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowHide",win,auxwin);
 		auxwin.setVisibility(false);
 	}
 	static void WindowPositionCommand(int win, int command, int arg1, int arg2) {
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowPositionCommand",win,auxwin);
 		auxwin.positionCommand(command,arg1,arg2);
 	}
 	static void WindowSetPosition(int win, int x, int y) {
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowSetPosition",win,auxwin);
 		auxwin.setPosition(x,y);
 	}
 	static void WindowSetResolution(int win, int w, int h) {
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowSetResolution",win,auxwin);
 		auxwin.setResolution(w,h);
 		auxwin.setSize(w,h);
 	}
 	static void WindowSetSize(int win, int w, int h) {
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowSetSize",win,auxwin);
 		auxwin.setSize(w,h);
 	}
 	static void WindowSetTitle(int win, String s) {
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowSetTitle",win,auxwin);
 		auxwin.setTitle(s);
 	}
 	static void WindowShow(int win) {
 		AuxWindow *auxwin = vid_findAuxWindow(win);
 		checkhandle("WindowShow",win,auxwin);
 		auxwin.setVisibility(true);
 	}
 	//VI.m. Movie Playback Functions
 	static void AbortMovie() { win_movie_abortSimple(); }
 	static void MovieClose(int m) { win_movie_close(m); }
 	static int MovieGetCurrFrame(int m) { return win_movie_getCurrFrame(m); }
 	static int MovieGetFramerate(int m) { return win_movie_getFramerate(m); }
 	static int MovieGetImage(int m) { return win_movie_getImage(m); }
 	static int MovieLoad(String s, bool mute) { return win_movie_load(s, mute); }
 	static void MovieNextFrame(int m) { win_movie_nextFrame(m); }
 	static void MoviePlay(int m, bool loop) { win_movie_play(m, loop?1:0); }
 	static void MovieRender(int m) { win_movie_render(m); }
 	static void MovieSetFrame(int m, int f) { win_movie_setFrame(m,f); }
 	static int PlayMovie(String s){ return win_movie_playSimple(s); }
 */
 	//VI.n. Netcode Functions
 	static ServerSocket vcserver = null;
 
 	// Overkill (2008-04-17): Socket port can be switched to something besides 45150.
 	static int vcsockport = 45150;
 	static void SetConnectionPort(int port)
 	{
 		vcsockport = port;
 	}
 
 	// Overkill (2008-04-17): Socket port can be switched to something besides 45150.
 	static Socket Connect(String ip) {
 		Socket s;
 		try
 		{
 			s = new Socket(ip, vcsockport);
 		}
 		catch (Exception ne) {
 			return null;
 		}
 		return s;
 	}
 
 	// Overkill (2008-04-17): Socket port can be switched to something besides 45150.
 	// Caveat: The server currently may not switch listen ports once instantiated.
 	static Socket GetConnection() {
 		try {
 			if (vcserver != null)
 				vcserver = new ServerSocket(vcsockport);
 			Socket s = vcserver.accept();
 			return s;
 		}
 		catch(Exception e) {
 			return null;
 	    }
 	}
 
 	public static VImage geturlimage(String url) { 
 		// TODO Implement this mechanism!
 		error("Non implemented function: geturlimage");
 		return new VImage(10, 10);
 		//return getUrlImage(url); 
 	}
 	
 	public static String geturltext(String url) { 
 		error("Non implemented function: geturltext");
 		return "";
 		//return getUrlText(url); 
 	}
 /*	static void SocketClose(int sh) { delete ((Socket *)sh); }
 	static boolean SocketConnected(int sh) { return ((Socket*)sh).connected()!=0; }
 	static String SocketGetFile(int sh, String override) {
 		static char stbuf[4096];
 		Socket *s = (Socket *) sh;
 		String retstr;
 
 		EnforceNoDirectories(override);
 
 		int stlen = 0, ret;
 		ret = s.blockread(2, &stlen);
 		if (!ret)
 			return String();
 
 		ret = s.blockread(stlen, stbuf);
 		stbuf[stlen] = 0;
 
 		String fn = stbuf;
 		EnforceNoDirectories(fn);
 
 		int fl;
 		s.blockread(4, &fl);
 
 		char *buf = new char[fl];
 		s.blockread(fl, buf);
 
 		FILE *f;
 		if (override.length())
 		{
 			retstr = override;
 			f = fopen(override, "wb");
 		}
 		else
 		{
 			retstr = fn;
 			f = fopen(fn, "wb");
 		}
 		if (!f)
 			err("SocketGetFile: couldn't open file for writing!");
 		fwrite(buf, 1, fl, f);
 		fclose(f);
 		delete[] buf;
 
 		return retstr;
 	}
 	static int SocketGetInt(int sh) {
 		Socket *s = (Socket *) sh;
 		int ret;
 		char t;
 		ret = s.blockread(1, &t);
 		if (t != '1')
 			err("SocketGetInt() - packet being received is not an int");
 		int temp;
 		ret = s.blockread(4, &temp);
 		return temp;
 	}
 	static String SocketGetString(int sh) {
 		static char buf[4096];
 		Socket *s = (Socket *) sh;
 		int stlen = 0, ret;
 		char t;
 		ret = s.blockread(1, &t);
 		if (t != '3')
 			err("SocketGetString() - packet being received is not a string");
 		ret = s.blockread(2, &stlen);
 		if (!ret)
 			return String();
 
 	//[Rafael, the Esper] #ifdef __BIG_ENDIAN__
 	//	stlen >>= 16;
 	//#endif
 
 		if (stlen>4095) err("yeah uh dont send such big strings thru the network plz0r");
 		ret = s.blockread(stlen, buf);
 		buf[stlen] = 0;
 		return buf;
 	}
 	bool SocketHasData(int sh) { return ((Socket*)sh).dataready()!=0; }
 	static void SocketSendFile(int sh, String fn) {
 		Socket *s = (Socket *) sh;
 
 		EnforceNoDirectories(fn);
 
 		VFILE *f = vopen(fn);
 		if (!f)
 			err("ehhhhhh here's a tip. SocketSendFile can't send a file that doesnt exist (you tried to send %s)", fn);
 
 		int i = fn.length();
 		s.write(2, &i);
 		s.write(i, fn);
 
 		int l = filesize(f);
 		s.write(4, &l);
 		char *buf = new char[l];
 		vread(buf, l, f);
 		s.write(l, buf);
 		delete[] buf;
 		vclose(f);
 	}
 	static void SocketSendInt(int sh, int i) {
 		Socket *s = (Socket *) sh;
 		char t = '1';
 		s.write(1, &t);
 		s.write(4, &i);
 	}
 	static void SocketSendString(int sh, String str) {
 		Socket *s = (Socket *) sh;
 		int len = str.length();
 		if (len>4095) err("yeah uh dont send such big strings thru the network plz0r");
 		char t = '3';
 		s.write(1, &t);
 
 	//[Rafael, the Esper] #ifdef __BIG_ENDIAN__
 	//	len <<= 16;
 	//#endif
 
 		s.write(2, &len);
 
 	//[Rafael, the Esper] #ifdef __BIG_ENDIAN__
 	//	len >>= 16;
 	//#endif
 
 		s.write(len, str);
 	}
 
 	// Overkill (2008-04-17): Sockets can send and receive raw length-delimited strings
 	static String SocketGetRaw(int sh, int len)
 	{
 		static char buf[4096];
 		Socket *s = (Socket *) sh;
 		if (len > 4095)
 		{
 			err("SocketGetRaw() - can only receive a maximum of 4095 characters at a time. You've tried to get %d", len);
 		}
 		int ret = s.nonblockread(len, buf);
 		buf[ret] = 0;
 		return buf;
 	}
 
 	// Overkill (2008-04-17): Sockets can send and receive raw length-delimited strings
 	static void SocketSendRaw(int sh, String str)
 	{
 		Socket *s = (Socket *) sh;
 		int len = str.length();
 		s.write(len, str);
 	}
 
 	// Overkill (2008-04-20): Peek at how many bytes are in buffer. Requested by ustor.
 	static int SocketByteCount(int sh)
 	{
 		Socket *s = (Socket *) sh;
 		return s.byteCount();
 	}
 
 	//XX: unsorted functions and variables, mostly newly added and undocumented
 	static String Get_EntityChr(int arg) {
 		if(arg >= 0 && arg < entities && entity[arg].chr != 0)
 			return entity[arg].chr.name;
 		else
 			return "";
 	}
 	static void Set_EntityChr(int arg, String chr) {
 		if(arg >= 0 && arg < entities)
 			entity[arg].set_chr(chr);
 	}
 	static int Get_EntityFrameW(int ofs) {
 		if (ofs>=0 && ofs<entities) return entity[ofs].chr.fxsize; else return 0;
 	}
 	static int Get_EntityFrameH(int ofs) {
 		if (ofs>=0 && ofs<entities) return entity[ofs].chr.fysize; else return 0;
 	}
 
 	static String Get_EntityDescription(int arg) {
 		if(arg >= 0 && arg < entities)
 			return entity[arg].description;
 		else
 			return "";
 	}
 	static void Set_EntityDescription(int arg, String val) { 
 		if(arg >= 0 && arg < entities)
 			entity[arg].description = val;
 	}
 
 	static void Set_EntityActivateScript(int arg, String val)
 	{
 		if(arg >= 0 && arg < entities)
 			entity[arg].script = val;
 	}
 
 
 	static boolean SoundIsPlaying(int chn) { return SoundIsPlaying(chn); }
 	static void RectVGrad(int x1, int y1, int x2, int y2, int c, int c2, int d)
 	{
 		image *id = ImageForHandle(d);
 		RectVGrad(x1, y1, x2, y2, c, c2, id);
 	}
 	static void RectHGrad(int x1, int y1, int x2, int y2, int c, int c2, int d)
 	{
 		image *id = ImageForHandle(d);
 		RectHGrad(x1, y1, x2, y2, c, c2, id);
 	}
 	static void RectRGrad(int x1, int y1, int x2, int y2, int c, int c2, int d)
 	{
 		image *id = ImageForHandle(d);
 		RectRGrad(x1, y1, x2, y2, c, c2, id);
 	}
 	static void Rect4Grad(int x1, int y1, int x2, int y2, int c1, int c2, int c3, int c4, int d)
 	{
 		image *id = ImageForHandle(d);
 		Rect4Grad(x1, y1, x2, y2, c1, c2, c3, c4, id);
 	}
 */
 	// Overkill: 2005-12-28
 	// Helper function for WrapText.
 	static int textwidth(VFont font, String text, int pos, int len)
 	{
 		if (font == null)
 		{
 			return 0; //pixels(text,text+len);
 		}
 		else
 		{
 			return font.Pixels(text.substring(pos, pos+len));
 		}
 	}
 
 	private static boolean isLetterDigitOrSignal(char c) {
 		if(Character.isLetterOrDigit(c) || c=='+' || c=='-')
 			return true;
 		return false;
 	}
 	
 	// Split list of words into rows 
 	public static List<String> splitTextIntoRows(String text, int maxperrow) {
 		
 		List<String> words = splitTextIntoWords(text);
 		List<String> rows = new ArrayList<String>();
 		int i = 0;
 		String str;
 		while (i < words.size()) {
 			str = words.get(i);
 		    while (i < words.size()-1 && str.length()+ 1 + words.get(i+1).length() <= maxperrow) {
 		       str = str.concat(" " + words.get(i+1));
 		       i += 1;
 			}
 		    rows.add(str);
 		    str = "";i+=1;
 		}
 		return rows;
 	}
 	
 	// Split String in trimmed words
 	public static List<String> splitTextIntoWords(String text) { 
 		int initial = 0;
 		List<String> words = new ArrayList<String>();
 		if(text==null) 
 			return words;
 		
 		for(int i=0; i<text.length(); i++) {
 			while(i<text.length() && (isLetterDigitOrSignal(text.charAt(i)) || text.charAt(i) == '\'')) {
 				i++;
 			}
 			while(i<text.length() && !isLetterDigitOrSignal(text.charAt(i))) {
 				i++;
 			}
 			words.add(text.substring(initial, i).trim());
 			initial = i;
 		}
 		return words;
 	}	
 	
 	
 	// Rafael: changed the implementation
 	// Split list of words into rows 
 	public static List<String> wraptext(VFont wt_font, String wt_s, int wt_linelen) {
 		List<String> words = splitTextIntoWords(wt_s);
 		List<String> rows = new ArrayList<String>();
 		int i = 0;
 		String str;
 		while (i < words.size()) {
 			str = words.get(i);
 		    while (i < words.size()-1 && textwidth(wt_font, str) + textwidth(wt_font, words.get(i+1)) <= wt_linelen) {
 		       str = str.concat(" " + words.get(i+1));
 		       i += 1;
 			}
 		    rows.add(str); //System.out.println(str);
 		    str = "";i+=1;
 		}
 		return rows;
 
 	}
 /*
 	static int strpos(String sub, String source, int start) {
 		return source.str().find(sub.str(), start);
 	}
 
 	static int HSV(int h, int s, int v) { return HSVtoColor(h,s,v); }
 	static int GetH(int col) {
 		int h, s, v;
 		GetHSV(col, h, s, v);
 		return h;
 	}
 	static int GetS(int col) {
 		int h, s, v;
 		GetHSV(col, h, s, v);
 		return s;
 	}
 	static int GetV(int col) {
 		int h, s, v;
 		GetHSV(col, h, s, v);
 		return v;
 	}
 	static void HueReplace(int hue_find, int hue_tolerance, int hue_replace, int image) {
 		HueReplace(hue_find, hue_tolerance, hue_replace, ImageForHandle(image));
 	}
 	static void ColorReplace(int find, int replace, int image)
 	{
 		ColorReplace(find, replace, ImageForHandle(image));
 	}
 */
 	public static boolean up, down, left, right;
 	public static boolean b1, b2, b3, b4;
 	
 	public static final int SCAN_A = java.awt.event.KeyEvent.VK_A;
 	public static final int SCAN_B = java.awt.event.KeyEvent.VK_B;
 	public static final int SCAN_C = java.awt.event.KeyEvent.VK_C;
 	public static final int SCAN_D = java.awt.event.KeyEvent.VK_D;
 	public static final int SCAN_E = java.awt.event.KeyEvent.VK_E;
 	public static final int SCAN_F = java.awt.event.KeyEvent.VK_F;
 	public static final int SCAN_G = java.awt.event.KeyEvent.VK_G;
 	public static final int SCAN_H = java.awt.event.KeyEvent.VK_H;
 	public static final int SCAN_I = java.awt.event.KeyEvent.VK_I;
 	public static final int SCAN_J = java.awt.event.KeyEvent.VK_J;
 	public static final int SCAN_K = java.awt.event.KeyEvent.VK_K;
 	public static final int SCAN_L = java.awt.event.KeyEvent.VK_L;
 	public static final int SCAN_M = java.awt.event.KeyEvent.VK_M;
 	public static final int SCAN_N = java.awt.event.KeyEvent.VK_N;
 	public static final int SCAN_O = java.awt.event.KeyEvent.VK_O;
 	public static final int SCAN_P = java.awt.event.KeyEvent.VK_P;
 	public static final int SCAN_Q = java.awt.event.KeyEvent.VK_Q;
 	public static final int SCAN_R = java.awt.event.KeyEvent.VK_R;
 	public static final int SCAN_S = java.awt.event.KeyEvent.VK_S;
 	public static final int SCAN_T = java.awt.event.KeyEvent.VK_T;
 	public static final int SCAN_U = java.awt.event.KeyEvent.VK_U;
 	public static final int SCAN_V = java.awt.event.KeyEvent.VK_V;
 	public static final int SCAN_W = java.awt.event.KeyEvent.VK_W;
 	public static final int SCAN_X = java.awt.event.KeyEvent.VK_X;
 	public static final int SCAN_Y = java.awt.event.KeyEvent.VK_Y;
 	public static final int SCAN_Z = java.awt.event.KeyEvent.VK_Z;
 	public static final int SCAN_0 = java.awt.event.KeyEvent.VK_0;
 	public static final int SCAN_1 = java.awt.event.KeyEvent.VK_1;
 	public static final int SCAN_2 = java.awt.event.KeyEvent.VK_2;
 	public static final int SCAN_3 = java.awt.event.KeyEvent.VK_3;
 	public static final int SCAN_4 = java.awt.event.KeyEvent.VK_4;
 	public static final int SCAN_5 = java.awt.event.KeyEvent.VK_5;
 	public static final int SCAN_6 = java.awt.event.KeyEvent.VK_6;
 	public static final int SCAN_7 = java.awt.event.KeyEvent.VK_7;
 	public static final int SCAN_8 = java.awt.event.KeyEvent.VK_8;
 	public static final int SCAN_9 = java.awt.event.KeyEvent.VK_9;	
 	
 	public static boolean getkey(int key) {
 		return Controls.getKey(key);
 	}
 
 	/*
 	// Overkill (2006-06-30): Gets the contents of the key buffer.
 	// TODO: Implement for other platforms.
 	static String GetKeyBuffer()
 	{
 		//#ifdef __WIN32__
 			return keybuffer;
 		//#else 
 			//err("The function GetKeyBuffer() is not defined for this platform.");
 			//return String();
 		//#endif
 	}
 
 	// Overkill (2006-06-30): Clears the contents of the key buffer.
 	// TODO: Implement for other platforms.
 	static void FlushKeyBuffer()
 	{
 		//#ifdef __WIN32__
 			FlushKeyBuffer();
 		//#else 
 			//err("The function FlushKeyBuffer() is not defined for this platform.");
 		//#endif
 	}
 
 	// Overkill (2006-06-30): Sets the delay in centiseconds before key repeat.
 	// TODO: Implement for other platforms.
 	static void SetKeyDelay(int d)
 	{
 		if (d < 0)
 		{
 			d = 0;
 		}
 		//#ifdef __WIN32__
 			key_input_delay = d;
 		//#else 
 		//	err("The function SetKeyDelay() is not defined for this platform.");
 		//#endif
 	}	
 	*/
 
 	// Handy code by [Rafael, the Esper]
 	public static void fadeout(int delay) {
 		unpress(9);
 		for(int i=0; i<=delay; i++) {
 			 //set the opacity
 		    getGUI().setAlpha((float) 1-((float)i/delay));
 		    showpage();
 		}
 	}
 	
 	public static void fadein(int delay, boolean rendermap) {
 		unpress(9);
 		for(int i=0; i<=delay; i++) {
 			//setlucent(i/delay);
 			//set the opacity
 			if(rendermap) 
 				render();
 			getGUI().setAlpha((float)i/delay);
 		    showpage();
 		}
 	}
 	
 	public static void fade(int delay, boolean black) { // fade in and out
 		fadeout(delay);
 		if(black) {
 			rectfill(0,0,screen.width, screen.height, Color.BLACK, screen);
 			fadein(delay, false);
 		}
 		else {
 			fadein(delay, true);
 		}
 		
 	}
 	
 	
 	// Function (method) calling
 	
 	public static boolean functionexists(String function) {
 		return executefunction(function, true);
 	}
 	
 	/** Check methods in the following order:
 	 * 
 	 * 1. Direct Class-method (ex: sully.vc.v1_menu.Menu_System.DrawMenu)
 	 * 2. System Lib (executed class, ex: Sully.class + method)
 	 * 3. Loaded Map Class (ex: Bumsville.class + method)
 	 *
 	 * The called function must be public and without parameters.
 	 * The capitalized version is also checked (ex: "entStart" checks also for "EntStart") 
 	 * If the function is not found, nothing happens
 	 * 	 
 	 */
 	public static void callfunction(String function) {
 		executefunction(function, false);
 	}
 	
 	private static boolean executefunction(String function, boolean justCheck) {
 		
 		if(function==null || function.isEmpty()) 
 			return false;
 
 		Class path = null;
 		// This means that it is a direct class-method
 		if (function.lastIndexOf(".") != -1) {
 			String s = function.substring(function.lastIndexOf(".") + 1);
 			String t = function.substring(0, function.lastIndexOf("."));
 			try { 
 				path = systemclass.forName(t);
 			}
 			catch(ClassNotFoundException cnfe) {
 				error("Class " + path + " not found for direct execution (" + function + ")");
 				return false;
 			}
 			invokeMethod(path, s, justCheck);
 			return true;
 		}
 		else { // Try to find the class in the current_map
 			 boolean notFoundInMap = false;
 			 StringBuilder cName = new StringBuilder();
 			 if(current_map != null && current_map.filename != null) {
 				 	cName.append(systemclass.getPackage().getName() + ".");
 				 	
 				 	int pos = current_map.filename.lastIndexOf('\\');
 				 	if(pos==-1)
 				 		pos = 0;
 				 	
 			 		StringBuilder b = new StringBuilder(current_map.filename.toLowerCase());
 			 		b.replace(pos, pos+1, String.valueOf(Character.toUpperCase(b.charAt(pos))));
 			 		String s = b.toString().substring(0, b.indexOf(".map")).replace('\\', '.');
 			 		cName.append(s);
 			 		
 			 		try {
 			 			path = systemclass.forName(cName.toString());
 			 		}
 					catch(ClassNotFoundException cnfe) {
 						error("Class " + path + " not found for map execution.");
 						notFoundInMap = true; //return;
 					}
 					if(path!=null) {
 				 		if (!invokeMethod(path, function, justCheck))
 							notFoundInMap = true;
 						else
 							return true; // Success
 					}
 			 }
 			
 			 // Try to find the method directly in the System class
 			 if(current_map == null || current_map.filename == null || notFoundInMap) {
 			 
 				 path = systemclass;
 				 if (invokeMethod(path, function, justCheck)) {
 					 return true; // Success
 				 }
 				 else {
 					 error("Error invoking " + function + " in path " + path);
 				 }
 			 }
 		}
 		return false;
 	}
 	
 	private static boolean invokeMethod(Class c, String function, boolean justCheck) { 
 
 		Method[] allMethods = c.getDeclaredMethods();
 		for (Method m : allMethods) {
 			String mname = m.getName();
 			if(mname.equals(function) || mname.equals(capitalize(function))) {
 
 				if(justCheck)
 					return true;
 				
 				try {
 					//log("Found method " + mname + " in path " + c); // just for debug
 					m.invoke(null);
 					return true;
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				} catch (InvocationTargetException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Method for loading resources from the classpath, like images, fonts, sounds, etc 
 	 */
 	public static URL load(String url) {
 		log("(" + systemclass + ")" + ", reading: " + url);
 		URL resource = systemclass.getResource(url);
 		
 		// Optional code, a little robustness to avoid case-sensitive issues
 		if(resource == null) { // try to capitalize
 			String newUrl;
 			if(url.lastIndexOf('/') != -1) 
				newUrl = url.substring(0, url.lastIndexOf('/')+1) +
 					capitalize(url.substring( url.lastIndexOf('/')+1));
 			else
 				newUrl = capitalize(url);
 			log("WARNING! Resource not found. Trying to read: " + newUrl);
 			resource = systemclass.getResource(newUrl);
 			
 			if(resource==null) { // try uppercase 
 				if(url.lastIndexOf('/') != -1) 
					newUrl = url.substring(0,  url.lastIndexOf('/')+1) +
 						url.substring( url.lastIndexOf('/')+1).toUpperCase();
 				else
 					newUrl = url.toUpperCase();
 				log("WARNING! Resource not found. Trying to read: " + newUrl);
 				resource = systemclass.getResource(newUrl);				
 			}
 			
 			if(resource==null) { // try lowercase 
 				if(url.lastIndexOf('/') != -1) 
					newUrl = url.substring(0,  url.lastIndexOf('/')+1) +
 						url.substring( url.lastIndexOf('/')+1).toLowerCase();
 				else
 					newUrl = url.toLowerCase();
 				log("WARNING! Resource not found. Trying to read: " + newUrl);
 				resource = systemclass.getResource(newUrl);				
 			}			
 			
 			if(resource==null) {
 				error("ERROR! Resource not found: " + url);
 			}
 			
 		}
 		
 		return resource;
 	}
 	public static void setSystemPath(Class c) {
 		systemclass = c;
 	}
 	
 
 	
 }
