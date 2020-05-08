 package j15r.xna.platformer.core;
 
 import forplay.core.Keyboard;
 
 // TODO: lots
 public class KeyboardState implements Keyboard.Listener {
 
   private static KeyboardState state = new KeyboardState();
 
   public static KeyboardState GetState() {
     return state;
   }
 
   private boolean[] keys = new boolean[256];
 
   public boolean IsKeyDown(int keyCode) {
     assert (keyCode >= 0) && (keyCode < 256);
     return keys[keyCode];
   }
 
   @Override
   public void onKeyDown(int keyCode) {
    if (keyCode < 256) {
      keys[keyCode] = true;
    }
   }
 
   @Override
   public void onKeyUp(int keyCode) {
    if (keyCode < 256) {
      keys[keyCode] = false;
    }
   }
 }
