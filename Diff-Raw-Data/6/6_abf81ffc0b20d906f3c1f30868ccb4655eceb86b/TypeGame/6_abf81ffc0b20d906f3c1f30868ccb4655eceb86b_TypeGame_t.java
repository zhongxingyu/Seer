 package warrenfalk.typegame;
 
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.awt.font.FontRenderContext;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.file.FileSystems;
 import java.nio.file.OpenOption;
 import java.nio.file.StandardOpenOption;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.lwjgl.font.glfont.FTFont;
 import org.lwjgl.font.glfont.FTGLPolygonFont;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.openal.AL;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.PixelFormat;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 public class TypeGame {
 	
 	static String[] challenges;
 	static ByteBuffer logBuffer = ByteBuffer.allocate(8192);
 	
 	static Key[] keys = new Key[] {
 		new Key(Keyboard.KEY_A, 'a', -4.5f, 0f, 1f, 1),
 		new Key(Keyboard.KEY_S, 's', -3.5f, 0f, 1f, 2),
 		new Key(Keyboard.KEY_D, 'd', -2.5f, 0f, 1f, 3),
 		new Key(Keyboard.KEY_F, 'f', -1.5f, 0f, 1f, 4),
 		new Key(Keyboard.KEY_G, 'g', -0.5f, 0f, 1f, 4),
 		new Key(Keyboard.KEY_H, 'h', 0.5f, 0f, 1f, 5),
 		new Key(Keyboard.KEY_J, 'j', 1.5f, 0f, 1f, 5),
 		new Key(Keyboard.KEY_K, 'k', 2.5f, 0f, 1f, 6),
 		new Key(Keyboard.KEY_L, 'l', 3.5f, 0f, 1f, 7),
 		new Key(Keyboard.KEY_SEMICOLON, ';', 4.5f, 0f, 1f, 8),
 		new Key(Keyboard.KEY_APOSTROPHE, '\'', 5.5f, 0f, 1f, 8),
 
 		new Key(Keyboard.KEY_Q, 'q', -4.8f, 1f, 1f, 1),
 		new Key(Keyboard.KEY_W, 'w', -3.8f, 1f, 1f, 2),
 		new Key(Keyboard.KEY_E, 'e', -2.8f, 1f, 1f, 3),
 		new Key(Keyboard.KEY_R, 'r', -1.8f, 1f, 1f, 4),
 		new Key(Keyboard.KEY_T, 't', -0.8f, 1f, 1f, 4),
 		new Key(Keyboard.KEY_Y, 'y', 0.2f, 1f, 1f, 5),
 		new Key(Keyboard.KEY_U, 'u', 1.2f, 1f, 1f, 5),
 		new Key(Keyboard.KEY_I, 'i', 2.2f, 1f, 1f, 6),
 		new Key(Keyboard.KEY_O, 'o', 3.2f, 1f, 1f, 7),
 		new Key(Keyboard.KEY_P, 'p', 4.2f, 1f, 1f, 8),
 
 		new Key(Keyboard.KEY_Z, 'z', -4f, -1f, 1f, 2),
 		new Key(Keyboard.KEY_X, 'x', -3f, -1f, 1f, 3),
 		new Key(Keyboard.KEY_C, 'c', -2f, -1f, 1f, 4),
 		new Key(Keyboard.KEY_V, 'v', -1f, -1f, 1f, 4),
 		new Key(Keyboard.KEY_B, 'b', 0f, -1f, 1f, 4),
 		new Key(Keyboard.KEY_N, 'n', 1f, -1f, 1f, 5),
 		new Key(Keyboard.KEY_M, 'm', 2f, -1f, 1f, 5),
 		new Key(Keyboard.KEY_COMMA, ',', 3f, -1f, 1f, 6),
 		new Key(Keyboard.KEY_PERIOD, '.', 4f, -1f, 1f, 7),
 		
 		new Key(Keyboard.KEY_SPACE, ' ', 0f, -2f, 5f, 0),
 	};
 	
 	final static int[] fingerHomes = new int[] {
 		Keyboard.KEY_SPACE,
 		Keyboard.KEY_A,
 		Keyboard.KEY_S,
 		Keyboard.KEY_D,
 		Keyboard.KEY_F,
 		Keyboard.KEY_J,
 		Keyboard.KEY_K,
 		Keyboard.KEY_L,
 		Keyboard.KEY_SEMICOLON,
 	};
 	
 	final static int[] fingers = new int[fingerHomes.length];
 	
 	final static int STATE_WAIT_FOR_READY = 0;
 	final static int STATE_PLAYING = 1;
 	
 	static class Key {
 		final int key;
 		final char ch;
 		final float x;
 		final float y;
 		final float w;
 		final int finger;
 		
 		final static HashMap<Integer,Key> byKey = new HashMap<Integer,Key>();
 		final static HashMap<Character,Key> byChar = new HashMap<Character,Key>();
 		
 		Key(int key, char ch, float x, float y, float w, int finger) {
 			this.key = key;
 			this.ch = ch;
 			this.x = x;
 			this.y = y;
 			this.w = w;
 			this.finger = finger;
 			byKey.put(key, this);
 			byChar.put(ch, this);
 		}
 	}
 	
 	public static void setupGl() {
 		Exception last = null;
		for (int samples = 2; samples >= -1; samples--) {
			int sampleSq = (samples == -1) ? 0 : 1 << samples;
 			for (int depth = 32; depth > 8; depth -= 8) {
 				for (int i = 0; i < 2; i++) {
 					try {
						PixelFormat pf = new PixelFormat().withDepthBits(depth).withSamples(sampleSq).withSRGB(i == 0);
 						Display.create(pf);
 						return;
 					}
 					catch (Exception e) {
 						System.out.println(e.getMessage());
 						last = e;
 					}
 				}
 			}
 		}
 		throw new RuntimeException(last);
 	}
 	
 	public static void main(String[] args) throws Exception {
 		int width = 1066;
 		int height = 600;
 
 		Display.setDisplayMode(new DisplayMode(width, height));
 		setupGl();
 		Display.setVSyncEnabled(true);
 
 		GL11.glClearColor(1f, 1f, 1f, 0f);
 		
 		GL11.glEnable(GL11.GL_BLEND);
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 
 		GL11.glViewport(0, 0, width, height);
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		float left = -width / 2;
 		float top = -height / 2;
 		float right = width + left;
 		float bottom = height + top;
 		GL11.glOrtho(left, right, top, bottom, 0, 1000);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		GL11.glLoadIdentity();
 
 		Font f = loadFont("/fonts/Comfortaa-Regular.ttf", 60);
 		Font f2 = loadFont("/fonts/NEUROPOL.ttf", 20);
 		FontRenderContext fcontext = FTFont.STANDARDCONTEXT;
 		// FTFont font = new FTGLExtrdFont(f, fcontext);
 		FTFont font = new FTGLPolygonFont(f, fcontext);
 		FTFont statusFont = new FTGLPolygonFont(f2, fcontext);
 		
 		DecimalFormat secondsFormat = new DecimalFormat("###,##0.0");
 		
 		Audio clickEffect = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("sounds/click.wav"));
 		Audio click2Effect = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("sounds/click2.wav"));
 		Audio buzzerEffect = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("sounds/buzzer.wav"));
 		Audio bellEffect = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("sounds/bell.wav"));
 		Audio successEffect = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("sounds/success.wav"));
 		
 		String readyPrompt = "Press F and J simultaneously when ready";
 
 		float cursorPosition;
 		float finishPosition;
 		float startPosition;
 		float idealOffset = -(width * 0.3f);
 		float textLinePosition = idealOffset;
 		int level = readLastLevel();
 		int nextChar = 0;
 		boolean typo = false;
 		float fingerFade = 0f;
 		float fingerAlpha = 0f;
 		float keyboardAlpha = 0f;
 		int keyboardDelay = 120;
 		int state = STATE_WAIT_FOR_READY;
 		boolean readyKeysDown = false;
 		boolean jk = false;
 		boolean fk = false;
 		long inactivity = 0;
 		
 		int tries = 0;
 		long startTime = 0L;
 
 		String challengeText = getChallengeText(level);
 		long msToWin = calcWinTime(challengeText);
 		cursorPosition = calculateCursorPosition(font, challengeText, nextChar);
 		finishPosition = calculateCursorPosition(font, challengeText, challengeText.length() - 1);
 		startPosition = calculateCursorPosition(font, challengeText, 0);
 		while (true) {
 			char cchar = Character.toLowerCase(challengeText.charAt(nextChar));
 			// process input
 			if (state == STATE_PLAYING) {
 				while (Keyboard.next()) {
 					if (Keyboard.getEventKeyState()) {
 						inactivity = 0;
 						char kchar = Keyboard.getEventCharacter();
 						if (kchar != 0) {
 							kchar = Character.toLowerCase(kchar);
 							if (nextChar != 0 || cchar == ' ' || (kchar != ' ' && kchar != '.')) {
 								if (kchar == cchar) {
 									// The correct key was pressed
 									logHit(level, startTime, nextChar, cchar);
 									// if this is the first position, start the clock
 									if (nextChar == 0)
 										startTime = System.currentTimeMillis();
 									// play a different click for space bar
 									if (kchar == ' ')
 										click2Effect.playAsSoundEffect(1f, 0.3f, false);
 									else
 										clickEffect.playAsSoundEffect(1f, 0.3f, false);
 									// advance to the next position
 									nextChar++;
 									if (nextChar == challengeText.length()) {
 										// we're at the end of the level
 										// start at the beginning
 										nextChar = 0;
 										// success is if we did it in time and without typos
 										long elapsed = System.currentTimeMillis() - startTime;
 										boolean success = !typo && elapsed <= msToWin;
 										if (success) {
 											// play success sound, reset tries, go to next level, save progress
 											logWin(level, elapsed);
 											successEffect.playAsSoundEffect(1f, 0.6f, false);
 											tries = 0;
 											level++;
 											saveLevel(level);
 											challengeText = getChallengeText(level);
 											msToWin = calcWinTime(challengeText);
 											finishPosition = calculateCursorPosition(font, challengeText, challengeText.length() - 1);
 											startPosition = calculateCursorPosition(font, challengeText, 0);
 										}
 										else {
 											logFail(level, elapsed, typo);
 											// play "let's try again" sound and increment tries
 											tries++;
 											bellEffect.playAsSoundEffect(1f, 1f, false);
 										}
 										// reset/stop clock and typo flag
 										startTime = 0L;
 										typo = false;
 									}
 									// advance the cursor
 									cursorPosition = calculateCursorPosition(font, challengeText, nextChar);
 									// reset keyboard on success
 									fingerFade = 0;
 									keyboardAlpha = 0f;
 									keyboardDelay = 120;
 								}
 								else if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
 									// reset level
 									typo = false;
 									startTime = 0L;
 									nextChar = 0;
 									cursorPosition = calculateCursorPosition(font, challengeText, nextChar);
 								}
 								else {
 									// wrong key, show the keyboard hint and if this wasn't the first key, set the typo flag
 									logMiss(level, startTime, nextChar, kchar, cchar);
 									buzzerEffect.playAsSoundEffect(1f, 0.7f, false);
 									keyboardDelay = 0;
 									if (nextChar > 0)
 										typo = true;
 								}
 							}
 						}
 					}
 				}
 			}
 			else if (state == STATE_WAIT_FOR_READY) {
 				while (Keyboard.next()) ;
 				jk = Keyboard.isKeyDown(Keyboard.KEY_J);
 				fk = Keyboard.isKeyDown(Keyboard.KEY_F);
 				readyKeysDown = readyKeysDown || jk && fk;
 				if (readyKeysDown == true && jk == false && fk == false) {
 					logWakeup(System.currentTimeMillis());
 					state = STATE_PLAYING;
 					readyKeysDown = false;
 					inactivity = 0;
 					// reset level
 					typo = false;
 					startTime = 0L;
 					nextChar = 0;
 					cursorPosition = calculateCursorPosition(font, challengeText, nextChar);
 				}
 			}
 			
 			// world tick
 			float idealTextLinePosition = idealOffset - cursorPosition;
 			float textLineDiff = idealTextLinePosition - textLinePosition;
 			float textLineMove = textLineDiff * 0.07f;
 			textLinePosition = textLinePosition + textLineMove;
 			if (state == STATE_PLAYING) {
 				if (fingerFade >= 1f)
 					fingerFade = 0f;
 				fingerAlpha = 0.2f + 0.8f * (float)Math.sin(fingerFade * (float)Math.PI);
 				fingerFade = fingerFade + 0.01f;
 				if (keyboardDelay > 0)
 					keyboardDelay--;
 				else if (keyboardAlpha < 1f)
 					keyboardAlpha += 0.02f;
 				inactivity += 1000 / 60;
 				if (inactivity >= 15000) {
 					state = STATE_WAIT_FOR_READY;
 					logInactivity(level, System.currentTimeMillis());
 				}
 			}
 			
 			GL11.glMatrixMode(GL11.GL_MODELVIEW);
 			GL11.glPushMatrix(); // save base view matrix
 			GL11.glTranslatef(textLinePosition, 0, 0);
 			
 			// begin drawing
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 			
 			// gradient background
 			GL11.glMatrixMode(GL11.GL_PROJECTION);
 			GL11.glPushMatrix();
 			GL11.glLoadIdentity();
 			GL11.glMatrixMode(GL11.GL_MODELVIEW);
 			GL11.glPushMatrix();
 			GL11.glLoadIdentity();
 			GL11.glBegin(GL11.GL_QUADS);
 			GL11.glColor4f(1f, 1f, 1f, 1f);
 			GL11.glVertex2f(-1f, -1f);
 			GL11.glVertex2f(1f, -1f);
 			GL11.glColor4f(0.4f, 0.4f, 0.4f, 1f);
 			GL11.glVertex2f(1f, 1f);
 			GL11.glVertex2f(-1f, 1f);
 			GL11.glEnd();
 			GL11.glMatrixMode(GL11.GL_MODELVIEW);
 			GL11.glPopMatrix();
 			GL11.glMatrixMode(GL11.GL_PROJECTION);
 			GL11.glPopMatrix();
 
 			// begin scene
 			GL11.glMatrixMode(GL11.GL_MODELVIEW);
 			
 			// status text
 			GL11.glPushMatrix(); // save view matrix
 			status(statusFont, 0, 0, "Level :", "" + (level + 1));
 			status(statusFont, 1, 0, "To win :", secondsFormat.format((double)msToWin / 1000.0) + " secs");
 			String time;
 			float elapsedFraction = 0f;
 			long elapsed = System.currentTimeMillis() - startTime;
 			elapsedFraction = (float)elapsed / (float)msToWin;
 			if (elapsedFraction > 1f)
 				elapsedFraction = 1f;
 			if (startTime == 0) {
 				time = "0.0 secs";
 			}
 			else if (typo || state != STATE_PLAYING) {
 				time = "--- secs";
 			}
 			else {
 				time = secondsFormat.format((double)elapsed / 1000.0) + " secs";
 			}
 			status(statusFont, 2, 0, "Your time :", time);
 			status(statusFont, 3, 0, "Attempt :", "" + (tries + 1));
 			GL11.glPopMatrix(); // restore view matrix
 			
 			// pace (arrow)
 			GL11.glPushMatrix(); // save view matrix
 			GL11.glTranslatef(startPosition + elapsedFraction * (finishPosition - startPosition), 60f, 0f);
 			GL11.glScalef(5f, -5f, 1f);
 			if (typo)
 				GL11.glColor4f(1f, 0f, 0f, 0.8f);
 			else
 				GL11.glColor4f(0f, 1f, 0f, 0.8f);
 			GL11.glBegin(GL11.GL_QUADS);
 			GL11.glVertex3f(-1f, -13f, 0f);
 			GL11.glVertex3f(-1f, -3f, 0f);
 			GL11.glVertex3f(1f, -3f, 0f);
 			GL11.glVertex3f(1f, -13f, 0f);
 			GL11.glEnd();
 			GL11.glBegin(GL11.GL_TRIANGLES);
 			GL11.glVertex3f(0f, -3f, 0f);
 			GL11.glVertex3f(0f, 0f, 0f);
 			GL11.glVertex3f(3f, -4f, 0f);
 			GL11.glVertex3f(0f, -3f, 0f);
 			GL11.glVertex3f(-3f, -4f, 0f);
 			GL11.glVertex3f(0f, 0f, 0f);
 			GL11.glEnd();
 			GL11.glPopMatrix(); // restore view matrix
 
 			// cursor (arrow)
 			GL11.glPushMatrix(); // save view matrix
 			GL11.glTranslatef(cursorPosition, 60f, 0f);
 			GL11.glScalef(5f, -5f, 1f);
 			GL11.glColor4f(0f, 0f, 1f, 0.8f);
 			GL11.glBegin(GL11.GL_QUADS);
 			GL11.glVertex3f(-1f, -13f, 0f);
 			GL11.glVertex3f(-1f, -3f, 0f);
 			GL11.glVertex3f(1f, -3f, 0f);
 			GL11.glVertex3f(1f, -13f, 0f);
 			GL11.glEnd();
 			GL11.glBegin(GL11.GL_TRIANGLES);
 			GL11.glVertex3f(0f, -3f, 0f);
 			GL11.glVertex3f(0f, 0f, 0f);
 			GL11.glVertex3f(3f, -4f, 0f);
 			GL11.glVertex3f(0f, -3f, 0f);
 			GL11.glVertex3f(-3f, -4f, 0f);
 			GL11.glVertex3f(0f, 0f, 0f);
 			GL11.glEnd();
 			GL11.glPopMatrix(); // restore view matrix
 
 			// text
 			GL11.glPushMatrix(); // save view matrix
 			// whitish completed text
 			GL11.glColor4f(0.8f, 0.8f, 0.8f, 1f);
 			String before = challengeText.substring(0, nextChar);
 			String next = challengeText.substring(nextChar, nextChar + 1);
 			String after = challengeText.substring(nextChar + 1);
 			float advance = font.advance(before);
 			font.render(before);
 			// red current character
 			GL11.glColor4f(1f, 0f, 0f, 1f);
 			GL11.glTranslatef(advance, 0f, 0f);
 			advance = font.advance(next);
 			font.render(next);
 			// black future characters
 			GL11.glColor4f(0f, 0f, 0f, 1f);
 			GL11.glTranslatef(advance, 0f, 0f);
 			font.render(after);
 			GL11.glPopMatrix(); // restore view matrix
 			
 			GL11.glPushMatrix(); // save view matrix
 			float keyStride = 28f;
 			float keySpace = 4f;
 			for (Key key : keys) {
 				float keyAlpha = Keyboard.isKeyDown(key.key) ? 1f : 0.2f;
 				GL11.glColor4f(0f, 0.3f, 0.9f, keyAlpha * keyboardAlpha);
 				GL11.glLoadIdentity();
 				GL11.glTranslatef(key.x * keyStride, -120f + key.y * keyStride, 0f);
 				drawKeyShape(keyStride * key.w - keySpace, keyStride - keySpace);
 			}
 			// reset all fingers to home positions
 			for (int i = 0; i < fingers.length; i++)
 				fingers[i] = fingerHomes[i];
 			// move the finger for the current character
 			Key ckey = Key.byChar.get(cchar);
 			fingers[ckey.finger] = ckey.key;
 			// draw the finger positions
 			for (int i = 0; i < fingers.length; i++) {
 				Key key = Key.byKey.get(fingers[i]);
 				GL11.glLoadIdentity();
 				GL11.glTranslatef(key.x * keyStride, -120f + key.y * keyStride, 0f);
 				float alpha;
 				if (ckey.finger == i) {
 					// this is the finger for the current character
 					alpha = fingerAlpha;
 					// if this is not a home position, draw an arrow
 					if (ckey.key != fingerHomes[i]) {
 						Key homeKey = Key.byKey.get(fingerHomes[i]);
 						float dx = (homeKey.x - ckey.x);
 						float dy = (homeKey.y- ckey.y);
 						float length = (float)Math.sqrt(dx * dx + dy * dy);
 						float ndx = dx / length;
 						float ndy = dy / length;
 						GL11.glLineWidth(3f);
 						GL11.glBegin(GL11.GL_LINES);
 						// start 10 units from center of target key
 						GL11.glVertex3f(ndx * 10f, ndy * 10f, 0f);
 						// draw line to center of home key
 						GL11.glVertex3f(dx * keyStride, dy * keyStride, 0f);
 						GL11.glEnd();
 					}
 				}
 				else {
 					// not the finger for the current character
 					alpha = 0.2f;
 				}
 				GL11.glColor4f(0f, 0f, 0f, alpha * keyboardAlpha);
 				drawFingerShape(keyStride - keySpace - 5f);
 			}
 			GL11.glPopMatrix(); // restore view matrix
 			
 			// show wait-for-ready overlay
 			if (state == STATE_WAIT_FOR_READY) {
 				GL11.glMatrixMode(GL11.GL_PROJECTION);
 				GL11.glPushMatrix();
 				GL11.glLoadIdentity();
 				GL11.glMatrixMode(GL11.GL_MODELVIEW);
 				GL11.glPushMatrix();
 				GL11.glLoadIdentity();
 				GL11.glBegin(GL11.GL_QUADS);
 				GL11.glColor4f(0f, 0f, 0f, 0.87f);
 				GL11.glVertex2f(-1f, -1f);
 				GL11.glVertex2f(1f, -1f);
 				GL11.glColor4f(0f, 0f, 0f, 0.87f);
 				GL11.glVertex2f(1f, 1f);
 				GL11.glVertex2f(-1f, 1f);
 				GL11.glEnd();
 				GL11.glPopMatrix();
 				GL11.glMatrixMode(GL11.GL_PROJECTION);
 				GL11.glPopMatrix();
 				GL11.glMatrixMode(GL11.GL_MODELVIEW);
 				
 				GL11.glPushMatrix();
 				// show F/J key images
 				GL11.glLoadIdentity();
 				GL11.glTranslatef(100f, 0f, 0f);
 				if (jk)
 					GL11.glColor4f(0.5f, 0.7f, 1f, 1f);
 				else
 					GL11.glColor4f(0.6f, 0.6f, 0.6f, 1f);
 				drawKeyShape(90f, 90f);
 				GL11.glTranslatef(-font.advance("J") / 2f, -font.ascender() / 2, 0f);
 				GL11.glColor4f(0f, 0f, 0f, 1f);
 				font.render("J");
 				GL11.glLoadIdentity();
 				GL11.glTranslatef(-100f, 0f, 0f);
 				if (fk)
 					GL11.glColor4f(0.5f, 0.7f, 1f, 1f);
 				else
 					GL11.glColor4f(0.6f, 0.6f, 0.6f, 1f);
 				drawKeyShape(90f, 90f);
 				GL11.glTranslatef(-font.advance("F") / 2f, -font.ascender() / 2, 0f);
 				GL11.glColor4f(0f, 0f, 0f, 1f);
 				font.render("F");
 				
 				// show prompt text
 				GL11.glLoadIdentity();
 				GL11.glScalef(1.5f, 1.5f, 0f);
 				GL11.glTranslatef(-statusFont.advance(readyPrompt) / 2f, 100f, 0);
 				GL11.glColor4f(0.6f, 0.6f, 0.6f, 1f);
 				statusFont.render(readyPrompt);
 				
 				GL11.glPopMatrix();
 			}
 			
 			// end scene
 			GL11.glPopMatrix(); // restore base view matrix
 
 			Display.update();
 			Display.sync(60);
 
 			if (Display.isCloseRequested()) {
 				logFlush();
 				Display.destroy();
 				AL.destroy();
 				System.exit(0);
 			}
 		}
 	}
 	
 	private static void logWakeup(long currentTime) {
 		log((byte)1);
 		log(currentTime);
 	}
 
 	private static void logInactivity(int level, long currentTime) {
 		log((byte)0);
 		log(level);
 		log(currentTime);
 	}
 
 	private static void logMiss(int level, long startTime, int nextChar,
 			char kchar, char cchar) {
 		log((byte)2);
 		log(startTime);
 		log(nextChar);
 		log(kchar);
 		log(cchar);
 	}
 
 	private static void logFail(int level, long elapsed, boolean typo) throws IOException {
 		log((byte)3);
 		log(level);
 		log(elapsed);
 		log(typo);
 		logFlush();
 	}
 
 	private static void logWin(int level, long elapsed) throws IOException {
 		log((byte)4);
 		log(level);
 		log(elapsed);
 		logFlush();
 	}
 
 	private static void logHit(int level, long startTime, int nextChar,
 			char cchar) {
 		log((byte)5);
 		log(level);
 		log(startTime);
 		log(nextChar);
 		log(cchar);
 	}
 	
 	private static void log(byte b) {
 		logBuffer.put(b);
 	}
 	
 	private static void log(int i) {
 		logBuffer.putInt(i);
 	}
 	
 	private static void log(long l) {
 		logBuffer.putLong(l);
 	}
 	
 	private static void log(boolean b) {
 		logBuffer.put(b ? (byte)1 : (byte)0);
 	}
 	
 	private static void logFlush() throws IOException {
 		File logFile = getLogFile();
 		try (FileChannel fc = FileChannel.open(FileSystems.getDefault().getPath(logFile.getAbsolutePath()), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
 			logBuffer.flip();
 			fc.write(logBuffer);
 			logBuffer.compact();
 		}
 	}
 
 	// TODO: create display list or vbo
 	private static void drawFingerShape(float diameter) {
 		float outer = diameter / 2f;
 		float inner = outer - 4f;
 		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
 		GL11.glVertex3f(0f, 0f, 0f);
 		float segments = 16f;
 		float step = 2f * (float)Math.PI / segments;
 		for (float a = 0f; a <= segments; a++) {
 			float ar = a * step;
 			GL11.glVertex3f((float)Math.cos(ar) * outer, -(float)Math.sin(ar) * outer, 0f);
 		}
 		GL11.glEnd();
 	}
 
 	// TODO: create display list or vbo
 	private static void drawKeyShape(float width, float height) {
 		float corner = 3f;
 		float outerx = width / 2f;
 		float outery = height / 2f;
 		float innerx = outerx - corner;
 		float innery = outery - corner;
 		float rx0 = innerx + 2f * (float)Math.cos(0.78539815);
 		float ry0 = innery + 2f * (float)Math.sin(0.78539815);
 		GL11.glBegin(GL11.GL_QUADS);
 		// main square (plus left/right edges)
 		GL11.glVertex3f(-outerx, -innery, 0f);
 		GL11.glVertex3f(-outerx, innery, 0f);
 		GL11.glVertex3f(outerx, innery, 0f);
 		GL11.glVertex3f(outerx, -innery, 0f);
 		// top edge
 		GL11.glVertex3f(-innerx, -outery, 0f);
 		GL11.glVertex3f(-innerx, -innery, 0f);
 		GL11.glVertex3f(innerx, -innery, 0f);
 		GL11.glVertex3f(innerx, -outery, 0f);
 		// bottom edge
 		GL11.glVertex3f(-innerx, innery, 0f);
 		GL11.glVertex3f(-innerx, outery, 0f);
 		GL11.glVertex3f(innerx, outery, 0f);
 		GL11.glVertex3f(innerx, innery, 0f);
 		GL11.glEnd();
 		// corners
 		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
 		GL11.glVertex3f(-innerx, -innery, 0);
 		GL11.glVertex3f(-innerx, -outery, 0f);
 		GL11.glVertex3f(-rx0, -ry0, 0f);
 		GL11.glVertex3f(-outerx, -innery, 0f);
 		GL11.glEnd();
 		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
 		GL11.glVertex3f(-innerx, innery, 0);
 		GL11.glVertex3f(-innerx, outery, 0f);
 		GL11.glVertex3f(-rx0, ry0, 0f);
 		GL11.glVertex3f(-outerx, innery, 0f);
 		GL11.glEnd();
 		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
 		GL11.glVertex3f(innerx, innery, 0);
 		GL11.glVertex3f(innerx, outery, 0f);
 		GL11.glVertex3f(rx0, ry0, 0f);
 		GL11.glVertex3f(outerx, innery, 0f);
 		GL11.glEnd();
 		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
 		GL11.glVertex3f(innerx, -innery, 0);
 		GL11.glVertex3f(innerx, -outery, 0f);
 		GL11.glVertex3f(rx0, -ry0, 0f);
 		GL11.glVertex3f(outerx, -innery, 0f);
 		GL11.glEnd();
 	}
 
 	private static int readLastLevel() throws IOException {
 		File level = getLevelSaveFile();
 		if (!level.isFile())
 			return 0;
 		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(level)));
 		try {
 			String line;
 			while (null != (line = reader.readLine())) {
 				return Integer.parseInt(line);
 			}
 			return 0;
 		}
 		finally {
 			reader.close();
 		}
 	}
 	
 	private static void saveLevel(int levelNum) throws FileNotFoundException {
 		File level = getLevelSaveFile();
 		level.getParentFile().mkdirs();
 		PrintWriter pw = new PrintWriter(level);
 		try {
 			pw.println(levelNum);
 		}
 		finally {
 			pw.close();
 		}
 	}
 	
 	private static File getLevelSaveFile() {
 		File userHome = new File(System.getProperty("user.home"));
 		File settings = new File(userHome, ".touchtype");
 		File level = new File(settings, "level");
 		return level;
 	}
 	
 	private static File getLogFile() {
 		File userHome = new File(System.getProperty("user.home"));
 		File settings = new File(userHome, ".touchtype");
 		File log = new File(settings, "log");
 		return log;
 	}
 
 	private static long calcWinTime(String challengeText) {
 		return (long)(5000f * (float)challengeText.length() / 13f);
 	}
 	
 	private static void status(FTFont statusFont, int row, int col, String label, String value) {
 		GL11.glLoadIdentity(); // back to identity reference frame
 		float labelWidth = statusFont.advance(label);
 		GL11.glTranslatef(-320f + 200f * col - labelWidth, 250f - 24f * row, 0f);
 		GL11.glColor4f(0f, 0f, 0f, 1f);
 		statusFont.render(label);
 		GL11.glTranslated(labelWidth + 13f, 0f, 0f);
 		GL11.glColor4f(0.4f, 1f, 0.4f, 1f);
 		statusFont.render(value);
 	}
 	
 	private static String getChallengeText(int level) throws IOException {
 		if (challenges == null)
 			challenges = loadChallenges();
 		return challenges[level];
 	}
 	
 	private static String[] loadChallenges() throws IOException {
 		InputStream in = ResourceLoader.getResourceAsStream("levels/progression.txt");
 		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 		ArrayList<String> lines = new ArrayList<String>();
 		String line = null;
 		while (null != (line = reader.readLine())) {
 			line = line.trim();
 			if (line.length() == 0)
 				continue;
 			lines.add(line);
 		}
 		return lines.toArray(new String[lines.size()]);
 	}
 
 	private static float calculateCursorPosition(FTFont font, String challengeText, int nextChar) {
 		String completedText = challengeText.substring(0, nextChar);
 		float completedWidth = font.advance(completedText);
 		if (nextChar + 1 > challengeText.length())
 			return completedWidth;
 		String futureCompletedText = challengeText.substring(0, nextChar + 1);
 		float futureCompletedWidth = font.advance(futureCompletedText);
 		return completedWidth + (futureCompletedWidth - completedWidth) / 2f;
 	}
 
 	private static Font loadFont(String fontName, float fontSize)
 			throws FontFormatException, IOException {
 		InputStream s = TypeGame.class.getResourceAsStream(fontName);
 		try {
 			Font awtFont = Font.createFont(Font.TRUETYPE_FONT, s);
 			return awtFont.deriveFont(fontSize);
 		} finally {
 			s.close();
 		}
 	}
 
 }
