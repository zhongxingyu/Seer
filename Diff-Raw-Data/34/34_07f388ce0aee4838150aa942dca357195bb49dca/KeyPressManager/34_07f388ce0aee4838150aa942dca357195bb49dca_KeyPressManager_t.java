 /**
  * Copyright (c) Scott Killen, 2012
  * 
  * This mod is distributed under the terms of the Minecraft Mod Public
  * License 1.0, or MMPL. Please check the contents of the license
  * located in /MMPL-1.0.txt
  */
 
 package bunyan;
 
 import net.minecraft.src.BaseMod;
 import net.minecraft.src.KeyBinding;
 import net.minecraft.src.ModLoader;
 
 import org.lwjgl.input.Mouse;
 
 public enum KeyPressManager {
 	INSTANCE;
 
 	private static KeyBinding	modeKey	= new KeyBinding(
												"key.logTurner", 37);
 
 	public static boolean isModeKeyPressed() {
 		return isPressed(modeKey.keyCode);
 	}
 
 	public static boolean isPressed(int keycode) {
 		return ModLoader.getMinecraftInstance().inGameHasFocus
 				&& (keycode < 0 ? Mouse.isButtonDown(keycode + 100)
 						: org.lwjgl.input.Keyboard.isKeyDown(keycode));
 	}
 
 	public static void registerKeys(BaseMod baseMod) {
		Proxy.registerKey(baseMod, modeKey, true);
		Proxy.addLocalization("key.logTurner", "Log Turner Mode");
 	}
 
 }
