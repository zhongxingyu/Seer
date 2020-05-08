 package warrenfalk.typegame;
 
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.awt.font.FontRenderContext;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.lwjgl.font.glfont.FTFont;
 import org.lwjgl.font.glfont.FTGLPolygonFont;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.PixelFormat;
 
 public class TypeGame {
 
 	public static void main(String[] args) throws Exception {
 		int width = 1066;
 		int height = 600;
 
		PixelFormat pf = new PixelFormat().withDepthBits(24).withSamples(4).withSRGB(true);
 		Display.setDisplayMode(new DisplayMode(width, height));
 		Display.create(pf);
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
 		FontRenderContext fcontext = FTFont.STANDARDCONTEXT;
 		// FTFont font = new FTGLExtrdFont(f, fcontext);
 		FTFont font = new FTGLPolygonFont(f, fcontext);
 
 		float cursorPosition = 0f;
 		float idealOffset = -(width * 0.3f);
 		float textLinePosition = idealOffset;
 		String challengeText = "Better to remain silent and be thought a fool than to speak and remove all doubt.";
 		int nextChar = 0;
 
 		cursorPosition = calculateCursorPosition(font, challengeText, nextChar);
 		while (true) {
 			// process input
 			while (Keyboard.next()) {
 				if (Keyboard.getEventKeyState()) {
 					char kchar = Character.toLowerCase(Keyboard.getEventCharacter());
 					char cchar = Character.toLowerCase(challengeText.charAt(nextChar));
 					if (kchar == cchar) {
 						nextChar++;
 						if (nextChar == challengeText.length()) {
 							nextChar = 0;
 							// TODO: advance to next challenge text
 						}
 						cursorPosition = calculateCursorPosition(font, challengeText, nextChar);
 					}
 				}
 			}
 			
 			// world tick
 			float idealTextLinePosition = idealOffset - cursorPosition;
 			float textLineDiff = idealTextLinePosition - textLinePosition;
 			float textLineMove = textLineDiff * 0.07f;
 			textLinePosition = textLinePosition + textLineMove;
 			
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
 			GL11.glColor4f(0.7f, 0.7f, 0.7f, 1f);
 			GL11.glVertex2f(1f, 1f);
 			GL11.glVertex2f(-1f, 1f);
 			GL11.glEnd();
 			GL11.glMatrixMode(GL11.GL_MODELVIEW);
 			GL11.glPopMatrix();
 			GL11.glMatrixMode(GL11.GL_PROJECTION);
 			GL11.glPopMatrix();
 
 			// begin scene
 			GL11.glMatrixMode(GL11.GL_MODELVIEW);
 			
 			// cursor (arrow)
 			GL11.glPushMatrix(); // save view matrix
 			GL11.glTranslatef(cursorPosition, 60f, 0f);
 			GL11.glScalef(5f, -5f, 1f);
 			GL11.glColor4f(1f, 0f, 0f, 0.8f);
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
 			
 			// cursor -> current character 
 			GL11.glPushMatrix(); // save view matrix
 			String nextCharString = challengeText.substring(nextChar, nextChar + 1);
 			float nextCharScale = 2f;
 			if (" ".equals(nextCharString)) {
 				nextCharString = "[space]";
 				nextCharScale = 0.5f;
 			}
 			float nextCharWidth = font.advance(nextCharString);
 			GL11.glTranslatef(cursorPosition - (nextCharWidth * nextCharScale / 2f), -40f - 40f * nextCharScale, 0f);
 			GL11.glScalef(nextCharScale, nextCharScale, 1f);
 			GL11.glColor4f(1f, 0f, 0f, 0.8f);
 			font.render(nextCharString);
 			GL11.glPopMatrix(); // restore view matrix
 
 			
 			// text
 			GL11.glPushMatrix(); // save view matrix
 			GL11.glColor4f(0f, 0f, 0f, 1f);
 			font.render(challengeText);
 			GL11.glPopMatrix(); // restore view matrix
 			
 			// end scene
 			GL11.glPopMatrix(); // restore base view matrix
 
 			Display.update();
 			Display.sync(60);
 
 			if (Display.isCloseRequested()) {
 				Display.destroy();
 				System.exit(0);
 			}
 		}
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
