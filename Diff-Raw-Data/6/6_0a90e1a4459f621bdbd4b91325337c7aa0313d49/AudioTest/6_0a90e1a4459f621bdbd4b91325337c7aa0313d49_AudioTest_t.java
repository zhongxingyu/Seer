 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.Scanner;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.openal.AL;
 import org.lwjgl.openal.AL10;
 import org.lwjgl.util.WaveData;
 
 import audio.Audio;
 import audio.OggFloatStream;
 import audio.Sound;
 import org.newdawn.slick.util.Log;
 
 public class AudioTest {
 	public static void main(String[] args) {
 		new AudioTest().execute();
 	}
 
 	public void execute() {
 		try {
 		    Display.setDisplayMode(new DisplayMode(800,600));
 		    Display.create();
 		} catch (LWJGLException e) {
 		    e.printStackTrace();
 		    System.exit(0);
 		}
 
 		Audio audio = new Audio();
 		audio.init();
 		
 //		WaveData waveFile;
 //		try {
 //			waveFile = WaveData.create(new FileInputStream("music_lead2.wav"));
 //			System.out.println(waveFile.data.capacity()/4);
 //			waveFile.dispose();
 //		} catch (FileNotFoundException e) {
 //			e.printStackTrace();
 //		}
 
 
 		Sound sound = audio.newSoundStream("music1_lead2.ogg");
 		sound.setLooping(true);
		sound.seek(2000000);
		sound.play();
 		Sound s = audio.newSound("menumove.ogg");
 
 		while (!Display.isCloseRequested()) {
 			s.play();
 			audio.update(0.0);
			Display.sync(30);
 		    Display.update();
 		}
 			
 		audio.finish();
 		Display.destroy();
 	}
 }
